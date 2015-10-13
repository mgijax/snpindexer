package org.jax.mgi.snpindexer.indexes;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.common.SolrInputDocument;
import org.jax.mgi.snpindexer.util.SQLExecutor;

public abstract class Indexer {

	protected ConcurrentUpdateSolrClient client = null;
	protected ConcurrentUpdateSolrClient adminClient = null;
	
	private String solrUrl = "";
	private String coreName = "";
	protected SQLExecutor sql = new SQLExecutor(10000, false);

	public Indexer(String solrUrl, String coreName) {
		this.solrUrl = solrUrl;
		this.coreName = coreName;
		setupServer();
	}

	public abstract void index();

	public void addDocument(SolrInputDocument doc) throws SolrServerException, IOException {
		setupServer();
		client.add(doc);
	}
	
	public void addDocuments(ArrayList<SolrInputDocument> docs) throws SolrServerException, IOException {
		setupServer();
		client.add(docs);
	}
	
	public void resetIndex() {
		deleteIndex();
		createIndex();
	}
	
	private void createIndex() {
		try {
			System.out.println("Creating Core: " + coreName);
			CoreAdminRequest.Create req = new CoreAdminRequest.Create();
			req.setCoreName(coreName);
			req.setInstanceDir(coreName);
			req.setDataDir("data");
			req.setConfigSet(coreName);
			req.setConfigName("solrconfig.xml");
			req.setIsLoadOnStartup(true);
			req.setSchemaName("schema.xml");
			req.setIndexInfoNeeded(false);
			req.process(adminClient);
			//adminClient.commit();
		} catch (Exception e) {
			System.out.println("Unable to Load Core: " + coreName + " Reason: " + e.getMessage());
		}
	}
	
	private void deleteIndex() {
		try {
			System.out.println("Deleting Core: " + coreName);
			CoreAdminRequest.Unload req = new CoreAdminRequest.Unload(true);
			req.setCoreName(coreName);
			req.setDeleteIndex(true);
			req.setDeleteDataDir(true);
			req.setDeleteInstanceDir(true);
			req.process(adminClient);
			//adminClient.commit();
		} catch (Exception e) {
			System.out.println("Unable to Delete Core: " + coreName + " Reason: " + e.getMessage());
		}
	}

	public void setupServer() {
		if(client == null) {
			client = new ConcurrentUpdateSolrClient(solrUrl + coreName, 100000, 2);
		}
		if(adminClient == null) {
			adminClient = new ConcurrentUpdateSolrClient(solrUrl, 1, 1);
		}
	}

}
