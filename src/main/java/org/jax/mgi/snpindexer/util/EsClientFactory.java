package org.jax.mgi.snpindexer.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig.Builder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.core.TimeValue;
import org.jax.mgi.snpindexer.config.ConfigurationHelper;
import org.jax.mgi.snpindexer.config.IndexerConfig;
import org.jax.mgi.snpindexer.util.es.SiteIndexSettings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EsClientFactory {

	//public static RequestOptions LARGE_SEARCH_RESPONSE_REQUEST_OPTIONS = RequestOptions.DEFAULT.toBuilder().build();
	private static List<RestHighLevelClient> clusterClients;

	private static List<RestHighLevelClient> getClusterClients() {

		if (clusterClients == null) {

			clusterClients = new ArrayList<>();

			for (List<String> cluster: ConfigurationHelper.getEsUrls()) {

				HttpHost[] clusterHosts = new HttpHost[cluster.size()];
				for(int i = 0; i < cluster.size(); i++) {
					String host = cluster.get(i);
					if(host.contains(":")) {
						String[] array = host.split(":");
						
						clusterHosts[i] = new HttpHost(array[0], Integer.parseInt(array[1]));
					} else {
						clusterHosts[i] = new HttpHost(host, 9200);
					}
				}
				
				RestClientBuilder clusterClient = RestClient.builder(clusterHosts);
				
				clusterClient.setRequestConfigCallback(new RequestConfigCallback() {
					public Builder customizeRequestConfig(Builder requestConfigBuilder) {
						int hour = (60 * 60 * 1000);
						int hours = 2 * hour;
						return requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(hours).setConnectionRequestTimeout(hours);
					}
				});

				
				clusterClients.add(new RestHighLevelClient(clusterClient));
			}

			log.info("Finished Connecting to ES clusters: " + clusterClients);
		}

		return clusterClients;

	}


	public static List<BulkProcessor> getProcessors(IndexerConfig config) {
		List<BulkProcessor> processors = new ArrayList<>();

		for (final RestHighLevelClient clusterClient : getClusterClients()) {
			BulkProcessor bulkProcessor;
			
			BulkProcessor.Listener listener = new BulkProcessor.Listener() {
				@Override
				public void beforeBulk(long executionId, BulkRequest request) {
				}
	
				@Override
				public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
					if(response.hasFailures()) {
						log.info("Size: " + request.requests().size() + " MB: " + request.estimatedSizeInBytes() + " Time: " + response.getTook() + " Bulk Requet Finished");
						log.info(response.buildFailureMessage());
					}
				}
	
				@Override
				public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
					log.error("Bulk Request Failure: " + failure.getMessage());
					log.error("Finished Adding failed requests to bulkProcessor: ");
				}
			};
	
			BulkProcessor.Builder builder = BulkProcessor.builder((request, bulkListener) -> clusterClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener);
			builder.setBulkActions(config.getBulkActions());
			builder.setBulkSize(new ByteSizeValue(config.getBulkSize(), ByteSizeUnit.MB));
			builder.setConcurrentRequests(config.getConcurrentRequests());
			builder.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueSeconds(1L), 60));
			
			bulkProcessor = builder.build();
			
			processors.add(bulkProcessor);
		}

		return processors;

	}
	
	public static void createIndex(String indexName) throws Exception {
		for (final RestHighLevelClient clusterClient : getClusterClients()) {
			CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
			SiteIndexSettings settings = new SiteIndexSettings(true);
			settings.buildSettings();
			//System.out.println(settings);
			createIndexRequest.settings(settings.getBuilder());
			clusterClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
		}
	}
	
	public static void deleteIndex(String indexName) throws Exception {
		for (final RestHighLevelClient clusterClient : getClusterClients()) {
			DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
			clusterClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
		}
	}

}