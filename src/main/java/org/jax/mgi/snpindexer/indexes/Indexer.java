package org.jax.mgi.snpindexer.indexes;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.jax.mgi.snpdatamodel.document.BaseESDocument;
import org.jax.mgi.snpindexer.util.EsClientFactory;
import org.jax.mgi.snpindexer.util.SQLExecutor;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.nilosplace.process_display.ProcessDisplayHelper;

public abstract class Indexer extends Thread {

	protected SQLExecutor sql;
	protected Logger log = Logger.getLogger(getClass());

	protected IndexerConfig config;
	protected Runtime runtime = Runtime.getRuntime();
	protected DecimalFormat df = new DecimalFormat("#.00");
	private SummaryStatistics stats = new SummaryStatistics();
	protected ObjectMapper om = new ObjectMapper();

	private ProcessDisplayHelper display = new ProcessDisplayHelper(10000);

	private List<RestHighLevelClient> clients;
	private List<BulkProcessor> documentProcessors;

	public Indexer(IndexerConfig config) {
		this.config = config;
		om.setSerializationInclusion(Include.NON_NULL);
		sql = new SQLExecutor(config.getChunkSize(), false);
		setupServer();
	}

	protected abstract void index();

	public <D extends BaseESDocument> void indexDocuments(Iterable<D> docs) {
		indexDocuments(docs, null);
	}

	public <D extends BaseESDocument> void indexDocuments(Iterable<D> docs, Class<?> view) {
		for (D doc : docs) {
			try {
				String json = "";
				if (view != null) {
					json = om.writerWithView(view).writeValueAsString(doc);
				} else {
					json = om.writeValueAsString(doc);
				}
				display.progressProcess();
				stats.addValue(json.length());
				for (BulkProcessor processor : documentProcessors) {
					processor.add(new IndexRequest(config.getIndexName()).source(json, XContentType.JSON));
				}

			} catch (JsonProcessingException e) {
				e.printStackTrace();
				log.error(e.getMessage());
				System.exit(-1);
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
			CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
//			if(settings != null) {
//				settings.buildSettings();
//				createIndexRequest.settings(settings.getBuilder());
//			}
//
//			if(mapping != null) {
//				mapping.buildMapping();
//				createIndexRequest.mapping(mapping.getBuilder());
//			}
			for (RestHighLevelClient client : clients) {
				client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
			}
		} catch (Exception e) {
			e.printStackTrace();
			RefreshRequest refreshRequest = new RefreshRequest(index);
			try {
				for (RestHighLevelClient client : clients) {
					client.indices().refresh(refreshRequest, RequestOptions.DEFAULT);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			log.error("Indexing Failed: " + index);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void deleteIndex() {
		String index = config.getIndexName();
		log.info("Deleting Index: " + index);
		DeleteIndexRequest request = new DeleteIndexRequest(index);
		try {
			for (RestHighLevelClient client : clients) {
				client.indices().delete(request, RequestOptions.DEFAULT);
			}
		} catch (Exception e) {
			log.error("Indexing Failed: " + index + " " + e.getMessage());
		}
	}

	public void setupServer() {
		if (clients == null) {
			clients = new ArrayList<>();
		}
		if (clients.isEmpty()) {
			clients = EsClientFactory.getClients();
		}
		if (documentProcessors == null) {
			documentProcessors = new ArrayList<>();
		}
		if (documentProcessors.isEmpty()) {
			documentProcessors = EsClientFactory.getProcessors(config);
		}
	}


	public void runIndex() {
		try {
			display.startProcess(config.getIndexerName());
			resetIndex();
			index();
			log.info("Waiting for bulkProcessors to finish");
			for (BulkProcessor processor : documentProcessors) {
				processor.flush();
				processor.awaitClose(30L, TimeUnit.DAYS);
			}
			display.finishProcess();
			log.info(stats);
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
