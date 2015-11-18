package org.jax.mgi.snpindexer.indexes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.common.SolrInputDocument;
import org.jax.mgi.snpindexer.util.ConfigurationHelper;
import org.jax.mgi.snpindexer.util.SQLExecutor;

public abstract class Indexer extends Thread {

	protected List<ConcurrentUpdateSolrClient> clients = null;
	protected List<ConcurrentUpdateSolrClient> adminClients = null;

	protected SQLExecutor sql;
	protected Logger log = Logger.getLogger(getClass());

	private Date startTime = new Date();
	private Date lastTime = new Date();
	private Date lastDocTime = new Date();
	protected IndexerConfig config;

	public Indexer(IndexerConfig config) {
		this.config = config;
		sql = new SQLExecutor(config.getSqlFetchSize(), false);
		setupServer();
	}

	public abstract void index();

	public void addDocument(SolrInputDocument doc) {
		ArrayList<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		docs.add(doc);
		addDocuments(docs);
	}

	public void addDocuments(ArrayList<SolrInputDocument> docs) {
		try {
			for(ConcurrentUpdateSolrClient client: clients) {
				client.add(docs);
			}
			Date now = new Date();
			if(now.getTime() - lastDocTime.getTime() > config.getCommitFreq()) {
				log.info("Commit timeout: " + (now.getTime() - lastDocTime.getTime()) + " running commit on current documents");
				commit();
				lastDocTime = now;
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void commit() {
		for(ConcurrentUpdateSolrClient client: clients) {
			try {
				client.commit();
			} catch (Exception e) {
				int trys = 5;
				while(trys-- > 0) {
					try {
						Thread.sleep(5000);
						log.warn("Retrying Commit: ");
						client.commit();
					} catch (Exception e1) {
						log.warn("Problem with Commit: " + ExceptionUtils.getRootCause(e1.getCause()));
						e1.printStackTrace();
					}
				}
				log.error("Could not commit batch to solr exiting");
				System.exit(1);
			}
		}
	}

	public void finish() {
		commit();
		for(ConcurrentUpdateSolrClient client: clients) {
			client.close();
		}
		log.info("Indexer Finished");
	}

	public void resetIndex() {
		deleteIndex();
		createIndex();
		startTime = new Date();
	}

	protected void startProcess(int amount, int size, int total) {
		log.info("Starting Processing: batches: " + amount + " size: " + size + " total: " + total + " at: " + startTime);
		lastTime = new Date();
	}

	protected void progress(int current, int total, int size) {
		double percent = ((double)current / (double)total);
		Date now = new Date();
		long diff = now.getTime() - startTime.getTime();
		long time = (now.getTime() - lastTime.getTime());
		if(percent > 0) {
			int perms = (int)(diff / percent);
			Date end = new Date(startTime.getTime() + perms);
			log.info("Batch: " + current + " of " + total + " took: " + time + "ms to process " + size + " records at a rate of: " + ((size * 1000) / time) + "r/s, Percentage complete: " + (int)(percent * 100) + "%, Estimated Finish: " + end);
		} else {
			log.info("Batch: " + current + " of " + total + " took: " + time + "ms to process " + size + " records at a rate of: " + ((size * 1000) / time) + "r/s");
		}
		lastTime = now;
	}

	protected void finishProcess(int total) {
		Date now = new Date();
		long time = now.getTime() - startTime.getTime();
		log.info("Processing finished: took: " + time + "ms to process " + total + " records at a rate of: " + ((total * 1000) / time) + "r/s");
	}

	private void createIndex() {
		try {
			log.info("Creating Core: " + config.getCoreName());
			CoreAdminRequest.Create req = new CoreAdminRequest.Create();
			req.setCoreName(config.getCoreName());
			req.setInstanceDir(config.getCoreName());
			req.setDataDir("data");
			req.setConfigSet(config.getCoreName());
			req.setConfigName("solrconfig.xml");
			req.setIsLoadOnStartup(true);
			req.setSchemaName("schema.xml");
			req.setIndexInfoNeeded(false);
			for(ConcurrentUpdateSolrClient adminClient: adminClients) {
				req.process(adminClient);
			}
		} catch (Exception e) {
			log.info("Unable to Load Core: " + config.getCoreName() + " Reason: " + e.getMessage());
		}
	}

	private void deleteIndex() {
		try {
			log.info("Deleting Core: " + config.getCoreName());
			CoreAdminRequest.Unload req = new CoreAdminRequest.Unload(true);
			req.setCoreName(config.getCoreName());
			req.setDeleteIndex(true);
			req.setDeleteDataDir(true);
			req.setDeleteInstanceDir(true);
			for(ConcurrentUpdateSolrClient adminClient: adminClients) {
				req.process(adminClient);
			}
		} catch (Exception e) {
			log.info("Unable to Delete Core: " + config.getCoreName() + " Reason: " + e.getMessage());
		}
	}

	public void setupServer() {
		if(clients == null || clients.isEmpty()) {
			for(String solrUrl: ConfigurationHelper.getSolrBaseUrls()) {
				log.info("Setup Solr Client to use Solr Urls: " + solrUrl + "/" + config.getCoreName());

				// Note queue size here is the size of the request that the amount of documents
				// So if adding documents in batches you will have queue * document batch size in
				// memory at any given time
				
				ConcurrentUpdateSolrClient client = new ConcurrentUpdateSolrClient(solrUrl + "/" + config.getCoreName(), 160, 8);
				client.setConnectionTimeout(100000);
				clients.add(client);
			}
			
		}
		if(adminClients == null || adminClients.isEmpty()) {
			for(String solrUrl: ConfigurationHelper.getSolrBaseUrls()) {
				ConcurrentUpdateSolrClient adminClient = new ConcurrentUpdateSolrClient(solrUrl, 1, 1);
				adminClients.add(adminClient);
			}
		}
	}

	public static void size(Map map) {
		try {
			System.out.println("Index Size: " + map.size());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(map);
			oos.close();
			System.out.println("Data Size: " + baos.size());
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		super.run();
		index();
	}

}
