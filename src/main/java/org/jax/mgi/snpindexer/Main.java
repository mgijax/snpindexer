package org.jax.mgi.snpindexer;

import java.util.Date;
import java.util.HashMap;

import org.jax.mgi.snpindexer.indexes.ConsensusSNPIndexer;
import org.jax.mgi.snpindexer.indexes.Indexer;
import org.jax.mgi.snpindexer.indexes.SearchSNPIndexer;

public class Main {
	

	public static void main(String[] args) {
		
		HashMap<String, Indexer> indexers = new HashMap<String, Indexer>();

		boolean threaded = false;
		System.out.println("Start Time: " + new Date());
		
		try {
			indexers.put("ConsensusSNPIndex", new ConsensusSNPIndexer("ConsensusSNPIndex"));
			indexers.put("SearchSNPIndex", new SearchSNPIndexer("SearchSNPIndex"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		for(String name: indexers.keySet()) {
			System.out.println("Starting Index for: " + name);
			if(threaded) {
				indexers.get(name).start();
			} else {
				indexers.get(name).index();
			}
		}

		// Wait will they are all finsihed before exiting
		for(Indexer i: indexers.values()) {
			try {
				i.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("End Time: " + new Date());
		System.exit(0);
	}

}
