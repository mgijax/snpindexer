package org.jax.mgi.snpindexer.indexes;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.common.SolrInputDocument;
import org.jax.mgi.snpindexer.util.ConfigurationHelper;
import org.jax.mgi.snpindexer.util.SQLExecutor;

public abstract class Indexer extends Thread {

	protected ConcurrentUpdateSolrClient client = null;
	protected ConcurrentUpdateSolrClient adminClient = null;
	
	private String coreName = "";
	protected static ConfigurationHelper config; // This is a static class so the constructor gets run automatically
	protected SQLExecutor sql = new SQLExecutor(10000, false);

	public Indexer(String coreName) {
		this.coreName = coreName;
		setupServer();
	}

	public abstract void index();

	public void addDocument(SolrInputDocument doc) {
		try {
			client.add(doc);
		} catch (SolrServerException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void addDocuments(ArrayList<SolrInputDocument> docs) {
		try {
			client.add(docs);
		} catch (SolrServerException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void commit() {
		try {
			client.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void finish() {
		try {
			client.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		client.close();
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
		} catch (Exception e) {
			System.out.println("Unable to Delete Core: " + coreName + " Reason: " + e.getMessage());
		}
	}

	public void setupServer() {
		if(client == null) {
			System.out.println("Setup Solr Client to use Solr Url: " + config.getSolrBaseUrl() + "/" + coreName);
			client = new ConcurrentUpdateSolrClient(config.getSolrBaseUrl() + "/" + coreName, 100000, 2);
		}
		if(adminClient == null) {
			adminClient = new ConcurrentUpdateSolrClient(config.getSolrBaseUrl(), 1, 1);
		}
	}

	@Override
	public void run() {
		super.run();
		index();
	}

}
