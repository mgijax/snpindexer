package org.jax.mgi.snpindexer;

import java.util.HashMap;

import org.jax.mgi.snpindexer.indexes.ConsensusSNPIndexer;
import org.jax.mgi.snpindexer.indexes.Indexer;
import org.jax.mgi.snpindexer.indexes.SNPSearchIndexer;
import org.jax.mgi.snpindexer.util.ConfigurationHelper;

public class Main {
	

	public static void main(String[] args) {
		
		HashMap<String, Indexer> indexers = new HashMap<String, Indexer>();

		boolean threaded = false;

		
		try {
			indexers.put("ConsensusSNPIndex", new ConsensusSNPIndexer("ConsensusSNPIndex"));
			//indexers.put("SNPSearchIndex", new SNPSearchIndexer("SNPSearchIndex"));
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

		for(Indexer i: indexers.values()) {
			try {
				i.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.exit(0);
	}

}
