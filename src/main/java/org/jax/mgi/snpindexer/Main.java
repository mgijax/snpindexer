package org.jax.mgi.snpindexer;

import java.util.Date;
import java.util.HashMap;

import org.jax.mgi.snpindexer.config.ConfigurationHelper;
import org.jax.mgi.snpindexer.config.IndexerConfig;
import org.jax.mgi.snpindexer.indexes.Indexer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	
	public static void main(String[] args) {
		ConfigurationHelper.init();

		HashMap<String, Indexer> indexers = new HashMap<String, Indexer>();

		boolean threaded = ConfigurationHelper.isThreaded();
		log.info("Start Time: " + new Date());

		for(IndexerConfig ic: IndexerConfig.values()) {
			try {
				Indexer i = (Indexer)ic.getClazz().getDeclaredConstructor(IndexerConfig.class).newInstance(ic);
				indexers.put(ic.getIndexerName(), i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for(String name: indexers.keySet()) {
			if(threaded) {
				log.info("Starting in threaded mode for: " + name);
				indexers.get(name).start();
			} else {
				if(args.length > 0 && args[0].equals(name)) {
					log.info("Starting one indexer: " + name);
					indexers.get(name).runIndex();
				} else if(args.length == 0) {
					log.info("Starting indexer sequentially: " + name);
					indexers.get(name).runIndex();
				} else {
					log.info("Not Starting: " + name);
					for(int i = 0; i < args.length; i++) {
						log.info("Args[" + i + "]: " + args[i]);
					}
				}
			}
		}

		// Wait will they are all finished before exiting
		
		for(Indexer i: indexers.values()) {
			try {
				if(i.isAlive()) {
					i.join();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		log.info("Indexer Finished End Time: " + new Date());
		System.exit(0);
	}

}
