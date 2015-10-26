package org.jax.mgi.snpindexer;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jax.mgi.snpindexer.indexes.ConsensusSNPIndexer;
import org.jax.mgi.snpindexer.indexes.Indexer;
import org.jax.mgi.snpindexer.indexes.SearchSNPIndexer;
import org.jax.mgi.snpindexer.util.ConfigurationHelper;
import org.jax.mgi.snpindexer.util.SQLExecutor;

public class Main {
	
	public static void main(String[] args) {
		ConfigurationHelper.init();
		

		Logger log = Logger.getLogger(Main.class);
		
		HashMap<String, Indexer> indexers = new HashMap<String, Indexer>();

		boolean threaded = false;
		log.info("Start Time: " + new Date());
		
		try {
			indexers.put("ConsensusSNPIndex", new ConsensusSNPIndexer("ConsensusSNPIndex"));
			indexers.put("SearchSNPIndex", new SearchSNPIndexer("SearchSNPIndex"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		for(String name: indexers.keySet()) {
			log.info("Starting Index for: " + name);
			if(threaded) {
				indexers.get(name).start();
			} else {
				if(args.length > 0 && args[0].equals(name)) {
					indexers.get(name).index();
				}
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
		
		log.info("End Time: " + new Date());
		System.exit(0);
	}

}
