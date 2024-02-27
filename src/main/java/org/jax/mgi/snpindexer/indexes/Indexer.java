package org.jax.mgi.snpindexer.indexes;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.jax.mgi.snpdatamodel.document.BaseESDocument;
import org.jax.mgi.snpindexer.config.IndexerConfig;
import org.jax.mgi.snpindexer.util.EsClientFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.nilosplace.process_display.ProcessDisplayHelper;

public abstract class Indexer extends Thread {

	//protected SQLExecutor sql;
	protected Logger log = Logger.getLogger(getClass());

	protected IndexerConfig config;
	protected Runtime runtime = Runtime.getRuntime();
	protected DecimalFormat df = new DecimalFormat("#.00");

	protected ProcessDisplayHelper display = new ProcessDisplayHelper(1000);
	protected ProcessDisplayHelper jsonDisplay = new ProcessDisplayHelper(1000);
	
	private List<BulkProcessor> documentProcessors;
	private ObjectMapper mapper = new ObjectMapper();

	public record DBChunk(int start, int end) { }
	
	public Indexer(IndexerConfig config) {
		this.config = config;
		//sql = new SQLExecutor(config.getChunkSize(), false);
		setupServer();
	}

	protected abstract void index();

	public <D extends BaseESDocument> void indexDocuments(Iterable<D> docs) {
		for (BulkProcessor processor : documentProcessors) {
			for (D doc : docs) {
				try {
					String json = mapper.writeValueAsString(doc);
					IndexRequest request = new IndexRequest();
					request.index(config.getIndexName());
					request.source(json, XContentType.JSON);
					processor.add(request);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				display.progressProcess();
			}
		}
	}
	
	
	public void indexJsonDocuments(List<String> docs) {
		for (BulkProcessor processor : documentProcessors) {
			for (String doc : docs) {
				IndexRequest request = new IndexRequest();
				request.index(config.getIndexName());
				request.source(doc, XContentType.JSON);
				processor.add(request);
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
			EsClientFactory.createIndex(index);
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
			for (BulkProcessor processor: documentProcessors) {
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
