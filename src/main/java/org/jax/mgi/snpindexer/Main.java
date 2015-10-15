package org.jax.mgi.snpindexer;

import java.util.HashMap;

import org.jax.mgi.snpindexer.indexes.ConsensusSNPIndexer;
import org.jax.mgi.snpindexer.indexes.Indexer;
import org.jax.mgi.snpindexer.indexes.SNPSearchIndexer;

public class Main {
	

	public static void main(String[] args) {
		
		HashMap<String, Indexer> indexers = new HashMap<String, Indexer>();
		
		String solrUrl = "http://localhost.jax.org:8983/solr/";

		//indexers.put("SNPSearchIndex", new SNPSearchIndexer(solrUrl, "SNPSearchIndex"));
		indexers.put("ConsensusSNPIndex", new ConsensusSNPIndexer(solrUrl, "ConsensusSNPIndex"));
		

		for(String name: indexers.keySet()) {
			System.out.println("Starting Index for: " + name);
			indexers.get(name).index();
		}


	}

}
