package org.jax.mgi.snpindexer.indexes;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.jax.mgi.snpdatamodel.document.BaseESDocument;
import org.jax.mgi.snpindexer.config.IndexerConfig;
import org.jax.mgi.snpindexer.util.EsClientFactory;
import org.jax.mgi.snpindexer.util.SQLExecutor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.nilosplace.process_display.ProcessDisplayHelper;

public abstract class Indexer extends Thread {

	protected SQLExecutor sql;
	protected Logger log = Logger.getLogger(getClass());

	protected IndexerConfig config;
	protected Runtime runtime = Runtime.getRuntime();
	protected DecimalFormat df = new DecimalFormat("#.00");

	protected ProcessDisplayHelper display = new ProcessDisplayHelper(10000);
	
	private List<BulkProcessor> documentProcessors;
	private ObjectMapper mapper = new ObjectMapper();

	public Indexer(IndexerConfig config) {
		this.config = config;
		sql = new SQLExecutor(config.getChunkSize(), false);
		setupServer();
	}

	protected abstract void index();

	public <D extends BaseESDocument> void indexDocuments(Iterable<D> docs) {
		List<String> jsonDocs = new ArrayList<>();
		for (D doc : docs) {
			for (BulkProcessor processor : documentProcessors) {
				try {
					String json = mapper.writeValueAsString(doc);
					//jsonDocs.add(json);
					IndexRequest request = new IndexRequest();
					request.index(config.getIndexName());
					request.source(json, XContentType.JSON);
					processor.add(request);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			}
			display.progressProcess();
		}

//		try {
//			Date time = new Date();
//			File file = new File("/tmp/data/json/" + config.getIndexName() + "-" + time.getTime() + ".gz");
//			
//			ParallelGZIPOutputStream out = new ParallelGZIPOutputStream(new FileOutputStream(file));
//			mapper.writeValue(out, jsonDocs);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
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
