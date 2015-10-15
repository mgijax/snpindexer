package org.jax.mgi.snpindexer;

import java.util.HashMap;

import org.jax.mgi.snpindexer.indexes.Indexer;
import org.jax.mgi.snpindexer.indexes.StrainIndexer;

public class Main {
	

	public static void main(String[] args) {
		
		HashMap<String, Indexer> indexers = new HashMap<String, Indexer>();
		
		String solrUrl = "http://localhost.jax.org:8983/solr/";
		

		indexers.put("StrainIndex", new StrainIndexer(solrUrl, "StrainIndex"));
		

		for(String name: indexers.keySet()) {
			System.out.println("Starting Index for: " + name);
			indexers.get(name).index();
		}


	}

}
