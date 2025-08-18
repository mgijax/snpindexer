package org.jax.mgi.snpindexer.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig.Builder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback;
import org.jax.mgi.snpindexer.config.ConfigurationHelper;
import org.jax.mgi.snpindexer.config.IndexerConfig;
import org.jax.mgi.snpindexer.util.es.Mapping;
import org.jax.mgi.snpindexer.util.es.Setting;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.elasticsearch.indices.PutIndicesSettingsRequest;
import co.elastic.clients.elasticsearch.indices.PutIndicesSettingsResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EsClientFactory {

    private static List<ElasticsearchClient> clusterClients;

    private static List<ElasticsearchClient> getClusterClients() {
        if (clusterClients == null) {
            clusterClients = new ArrayList<>();
            for (List<String> cluster : ConfigurationHelper.getEsUrls()) {

                HttpHost[] clusterHosts = cluster.stream()
                        .map(h -> {
                            String[] parts = h.split(":");
                            if (parts.length == 2) return new HttpHost(parts[0], Integer.parseInt(parts[1]));
                            return new HttpHost(h, 9200);
                        })
                        .toArray(HttpHost[]::new);

                RestClientBuilder restClientBuilder = RestClient.builder(clusterHosts);
                restClientBuilder.setRequestConfigCallback(new RequestConfigCallback() {
                    public Builder customizeRequestConfig(Builder requestConfigBuilder) {
                        int hour = 60 * 60 * 1000;
                        int twoHours = 2 * hour;
                        return requestConfigBuilder
                                .setConnectTimeout(5000)
                                .setSocketTimeout(twoHours)
                                .setConnectionRequestTimeout(twoHours);
                    }
                });

                RestClient restClient = restClientBuilder.build();
                ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
                ElasticsearchClient client = new ElasticsearchClient(transport);

                clusterClients.add(client);
            }
            log.info("Finished Connecting to ES clusters: {}", clusterClients);
        }
        return clusterClients;
    }
    
	public static List<CustomBulkProcessor> getProcessors(IndexerConfig config) {
        List<CustomBulkProcessor> processors = new ArrayList<>();

        for (ElasticsearchClient client : EsClientFactory.getClusterClients()) {
            processors.add(new CustomBulkProcessor(client, config));
        }

        return processors;
	}

    public static void createIndex(String indexName, Setting setting, Mapping mapping) throws IOException {
        for (ElasticsearchClient client : getClusterClients()) {
            CreateIndexRequest.Builder req = new CreateIndexRequest.Builder().index(indexName);
          
            if (setting != null) req.settings(setting.getSetting());
            if (mapping != null) req.mappings(mapping.getMapping());
            client.indices().create(req.build());
        }
    }

    public static void deleteIndex(String indexName) throws IOException {
        for (ElasticsearchClient client : getClusterClients()) {
            client.indices().delete(new DeleteIndexRequest.Builder().index(indexName).build());
        }
    }

    public static void setRefreshInterval(String indexName, String interval) throws IOException {
    	IndexSettings settings = new IndexSettings.Builder()
    		    .refreshInterval(new Time.Builder().time(interval).build())
    		    .build();
        setSetting(indexName, settings);
    }

    public static void setMaxResultWindow(String indexName, String size) throws IOException {
    	IndexSettings settings = new IndexSettings.Builder()
    		    .maxResultWindow(Integer.valueOf(size))
    		    .build();
        setSetting(indexName, settings);
    }

    private static void setSetting(String indexName, IndexSettings settings) throws IOException {
        for (ElasticsearchClient client : getClusterClients()) {
            PutIndicesSettingsRequest req = new PutIndicesSettingsRequest.Builder()
                    .index(indexName)
                    .settings(settings)
                    .build();

            PutIndicesSettingsResponse resp = client.indices().putSettings(req);
            log.info("Settings Change Complete for {}: {}", indexName, resp.acknowledged());
        }
    }

    /**
     * Bulk index using provided BulkOperation list (constructed by caller).
     */
    public static void bulkIndex(List<BulkOperation> operations) throws IOException {
        for (ElasticsearchClient client : getClusterClients()) {
            BulkRequest req = new BulkRequest.Builder().operations(operations).build();
            BulkResponse resp = client.bulk(req);
            if (resp.errors()) {
                log.error("Bulk request had failures");
                resp.items().forEach(item -> {
                    if (item.error() != null) {
                        log.error("Error in item {}: {}", item.index(), item.error().reason());
                    }
                });
            } else {
                log.info("Bulk request successful: {} items", resp.items().size());
            }
        }
    }
    
    // Example replacement for BulkProcessor
    public static class CustomBulkProcessor {
        private final ElasticsearchClient client;
        private final IndexerConfig config;
        private final List<BulkOperation> buffer = new ArrayList<>();
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        public CustomBulkProcessor(ElasticsearchClient client, IndexerConfig config) {
            this.client = client;
            this.config = config;

            // Optional: auto-flush on an interval
            scheduler.scheduleAtFixedRate(this::flushSafely, 5, 5, TimeUnit.SECONDS);
        }

        public synchronized void add(BulkOperation op) {
            buffer.add(op);

            if (buffer.size() >= config.getBulkActions()) {
                flush();
            }
        }

        public synchronized void flush() {
            if (buffer.isEmpty()) return;

            BulkRequest.Builder br = new BulkRequest.Builder();
            buffer.forEach(br::operations);

            try {
                BulkResponse response = client.bulk(br.build());
                if (response.errors()) {
                    log.warn("Bulk request had failures");
                    response.items().forEach(item -> {
                        if (item.error() != null) {
                            log.warn(item.error().reason());
                        }
                    });
                }
            } catch (IOException e) {
                log.error("Bulk request failed", e);
            } finally {
                buffer.clear();
            }
        }

        private void flushSafely() {
            try {
                flush();
            } catch (Exception e) {
                log.error("Error in scheduled bulk flush", e);
            }
        }

        public void close() {
            scheduler.shutdown();
            flush();
        }
    }
}
