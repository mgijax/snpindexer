package org.jax.mgi.snpindexer.indexes;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.jax.mgi.snpdatamodel.document.BaseESDocument;
import org.jax.mgi.snpindexer.util.EsClientFactory;
import org.jax.mgi.snpindexer.util.SQLExecutor;

import net.nilosplace.process_display.ProcessDisplayHelper;

public abstract class Indexer extends Thread {

	protected SQLExecutor sql;
	protected Logger log = Logger.getLogger(getClass());

	protected IndexerConfig config;
	protected Runtime runtime = Runtime.getRuntime();
	protected DecimalFormat df = new DecimalFormat("#.00");

	protected ProcessDisplayHelper display = new ProcessDisplayHelper(10000);
	
	private List<BulkProcessor> documentProcessors;
	//private ObjectMapper mapper = new ObjectMapper();

	public Indexer(IndexerConfig config) {
		this.config = config;
		sql = new SQLExecutor(config.getChunkSize(), false);
		setupServer();
	}

	protected abstract void index();

	public <D extends BaseESDocument> void indexDocuments(Iterable<D> docs) {
		for (D doc : docs) {
			for (BulkProcessor processor : documentProcessors) {
				IndexRequest request = new IndexRequest();
				request.index(config.getIndexName());
				request.source(doc);
				processor.add(request);
			}
			display.progressProcess();
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
//			if(settings != null) {
//				settings.buildSettings();
//				createIndexRequest.settings(settings.getBuilder());
//			}
//			if(mapping != null) {
//				mapping.buildMapping();
//				createIndexRequest.mapping(mapping.getBuilder());
//			}
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
				processor.close();
			}
			display.finishProcess();
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
