package org.jax.mgi.snpindexer.indexes;

import java.text.DecimalFormat;
import java.util.List;

import org.jax.mgi.snpdatamodel.document.BaseESDocument;
import org.jax.mgi.snpindexer.config.IndexerConfig;
import org.jax.mgi.snpindexer.util.EsClientFactory;
import org.jax.mgi.snpindexer.util.EsClientFactory.CustomBulkProcessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;
import net.nilosplace.process_display.ProcessDisplayHelper;

@Slf4j
public abstract class Indexer extends Thread {

	// protected SQLExecutor sql;

	protected IndexerConfig config;
	protected Runtime runtime = Runtime.getRuntime();
	protected DecimalFormat df = new DecimalFormat("#.00");

	protected ProcessDisplayHelper display = new ProcessDisplayHelper(5000);
	protected ProcessDisplayHelper jsonDisplay = new ProcessDisplayHelper(5000);

	private List<CustomBulkProcessor> documentProcessors;
	private ObjectMapper mapper = new ObjectMapper();

	public record DBChunk(int start, int end) {
	}

	public Indexer(IndexerConfig config) {
		this.config = config;
		// sql = new SQLExecutor(config.getChunkSize(), false);
		setupServer();
	}

	protected abstract void index();

	public <D extends BaseESDocument> void indexDocuments(Iterable<D> docs) {
		for (CustomBulkProcessor processor : documentProcessors) {
			for (D doc : docs) {
	            try {
	                String json = mapper.writeValueAsString(doc);

	                JsonNode jsonTree = mapper.readTree(json);
	                BulkOperation op = BulkOperation.of(b -> b
	                    .index(IndexOperation.of(i -> i
	                        .index(config.getIndexName())
	                        .document(JsonData.of(jsonTree))
	                    ))
	                );

	                processor.add(op);
	            } catch (JsonProcessingException e) {
	                log.error("Failed to serialize document", e);
	            }
			}
		}
	}

	public void indexJsonDocuments(List<String> docs) {
		for (CustomBulkProcessor processor : documentProcessors) {
			for (String doc : docs) {
				 BulkOperation op = BulkOperation.of(b -> b
			                .index(IndexOperation.of(i -> i
			                    .index(config.getIndexName())
			                    .document(JsonData.fromJson(doc))
			                ))
			            );
			    processor.add(op);
			}
		}
	}

	public void resetIndex() {
		deleteIndex();
		createIndex();
	}

	private void createIndex() {
		String index = config.getIndexName();
		log.info("Creating index: " + index);
		try {
			EsClientFactory.createIndex(config.getIndexName(), config.getSettings(), config.getMappings());
		} catch (Exception e) {
			log.error("Indexing Failed: " + index);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void deleteIndex() {
		String index = config.getIndexName();

		log.info("Deleting Index: " + index);
		try {
			EsClientFactory.deleteIndex(index);
		} catch (Exception e) {
			log.error("Indexing Failed: " + index + " " + e.getMessage());
		}
	}

	private void refreshIndex() {
		try {
			EsClientFactory.setRefreshInterval(config.getIndexName(), "1s");
			EsClientFactory.setMaxResultWindow(config.getIndexName(), "100000");
		} catch (Exception e) {
			log.error("Refreshing Index Failed: " + config.getIndexName());
			e.printStackTrace();
		}
	}

	public void setupServer() {
		if (documentProcessors == null || documentProcessors.isEmpty()) {	
			documentProcessors = EsClientFactory.getProcessors(config);
		}
	}

	public void runIndex() {
		try {
			resetIndex();
			index();
			log.info("Waiting for bulkProcessors to finish");
			for (CustomBulkProcessor processor : documentProcessors) {
				processor.flush();
				processor.close();
			}
			refreshIndex();
			display.finishProcess();
			jsonDisplay.finishProcess();
		} catch (Exception e) {
			log.error("Indexing Failed: " + config.getIndexerName());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	public void run() {
		super.run();
		runIndex();
	}

}
