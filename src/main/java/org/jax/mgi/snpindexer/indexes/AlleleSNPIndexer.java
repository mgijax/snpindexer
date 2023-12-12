package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jax.mgi.snpdatamodel.document.AlleleSNPDocument;
import org.jax.mgi.snpindexer.config.IndexerConfig;
import org.jax.mgi.snpindexer.util.SQLExecutor;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AlleleSNPIndexer extends Indexer {

	private HashMap<String, String> variationMap = new HashMap<String, String>();
	
	private HashMap<String, String> functionMap = new HashMap<String, String>();
	private HashMap<String, String> markerMap = new HashMap<String, String>();
	private HashMap<String, String> strainMap = new HashMap<String, String>();

	private LinkedBlockingQueue<DBChunk> dbWorkQueue = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<ArrayList<AlleleSNPDocument>> jsonWorkQueue = new LinkedBlockingQueue<>(30);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	public AlleleSNPIndexer(IndexerConfig config) {
		super(config);
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public void index() {

		try {

			SQLExecutor sql = new SQLExecutor(config.getCursorSize(), false);
			
			log.info("Starting Load Function Type Map");
			ResultSet set = sql.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 49");
			
			while (set.next()) {
				
				String key = set.getString("_term_key");
				String fc = set.getString("term");
				
				functionMap.put(key, fc);
			}
			set.close();
			log.info("Finished Load Function Type Map");

			log.info("Starting Load Variation Type Map");
			set = sql.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 50");
			while (set.next()) {
				variationMap.put(set.getString("_term_key"), set.getString("term"));
			}
			set.close();
			log.info("Finished Load Variation Type Map");

			log.info("Starting Load Strains Map");
			set = sql.executeQuery("select _mgdstrain_key, strain from snp.snp_strain");
			while (set.next()) {
				strainMap.put(set.getString("_mgdstrain_key"), set.getString("strain"));
			}
			set.close();
			log.info("Finished Load Strains Map");

			log.info("Starting Load Marker Accession Map");
			set = sql.executeQuery("select a.accid, m._marker_key from mgd.mrk_marker m, mgd.acc_accession a where m._marker_key = a._object_key and a._logicaldb_key = 1 and a._mgitype_key = 2 and a.preferred = 1 and m._organism_key = 1 and m._marker_status_key = 1");
			
			while (set.next()) {
				markerMap.put(set.getString("_marker_key"), set.getString("accid"));
			}
			set.close();
			log.info("Finished Load Marker Accession Map");
			
			set = sql.executeQuery("select max(sa._object_key) as maxKey from snp.snp_accession sa where sa._logicaldb_key = 73 and sa._mgitype_key = 30");
			set.next();
			int max = set.getInt("maxKey");
			set.close();

			int chunkSize = config.getChunkSize();
			int chunks = max / chunkSize;
			
			jsonDisplay.startProcess(config.getIndexerName() + " JSON");
			display.startProcess(config.getIndexerName() + "   DB", max);

			for(int i = 0; i <= chunks; i++) {
				int start = i * chunkSize;
				int end = (start + chunkSize);
				
				DBChunk chunk = new DBChunk(start, start + chunkSize);
				
				try {
					dbWorkQueue.put(chunk);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			int dbWorkerCount = config.getDbWorkerCount();
			int jsonWorkerCount = config.getJsonWorkerCount();
			
			ArrayList<JsonQueueWorker> jsonWorkers = new ArrayList<>();
			
			for(int i = 0; i < jsonWorkerCount; i++) {
				JsonQueueWorker worker = new JsonQueueWorker();
				worker.start();
				jsonWorkers.add(worker);
			}
			ArrayList<AlleleSNPDocumentBuilderQueueWorker> dbWorkers = new ArrayList<>();
			
			for(int i = 0; i < dbWorkerCount; i++) {
				AlleleSNPDocumentBuilderQueueWorker worker = new AlleleSNPDocumentBuilderQueueWorker();
				worker.start();
				dbWorkers.add(worker);
			}

			try {
				log.info("Waiting for all workers to start up");
				Thread.sleep(15000);
				
				log.info("Waiting for dbWorkers to finish");
				for (AlleleSNPDocumentBuilderQueueWorker w: dbWorkers) {
					w.join();
				}
				log.info("dbWorkers finished");
	
				log.info("Waiting for json Queue to empty");
				while (!jsonWorkQueue.isEmpty()) {
					Thread.sleep(15000);
				}
				TimeUnit.MILLISECONDS.sleep(15000);
				log.info("json Queue Empty");
				
				log.info("Shutting down jsonWorkers");
				for (JsonQueueWorker w: jsonWorkers) {
					w.interrupt();
					w.join();
				}
				log.info("jsonWorkers shutdown");

			} catch (Exception e) {
				e.printStackTrace();
			}


			sql.cleanup();

			log.info("Finished SNPSearchIndexer query");

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	
	private class JsonQueueWorker extends Thread {
		public void run() {
			log.info("Running JsonQueueWorker");
			try {
				while (!(Thread.currentThread().isInterrupted())) {
					ArrayList<AlleleSNPDocument> alleleDocPList = jsonWorkQueue.take();
					ArrayList<String> docCache = new ArrayList<>();
					for(AlleleSNPDocument doc: alleleDocPList) {
						try {
							String json = mapper.writeValueAsString(doc);
							docCache.add(json);
							jsonDisplay.progressProcess();
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
					}
					alleleDocPList.clear();
					indexJsonDocuments(docCache);
					docCache.clear();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			log.info("JsonQueueWorker Finished");
		}
	}
	
	
	private class AlleleSNPDocumentBuilderQueueWorker extends Thread {

		public void run() {
			log.info("Running AlleleSNPDocumentBuilderQueueWorker");
			try {
				SQLExecutor exec = new SQLExecutor(config.getCursorSize(), false);
				while(!dbWorkQueue.isEmpty()) {
					DBChunk chunk = dbWorkQueue.take();
					ArrayList<AlleleSNPDocument> alleleDocList = getAlleleSNPDocument(exec, chunk.start(), chunk.end());
					display.progressProcess((long)(chunk.end() - chunk.start()));
					jsonWorkQueue.offer(alleleDocList, 1, TimeUnit.DAYS);
				}
				exec.cleanup();
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.info("AlleleSNPDocumentBuilderQueueWorker Finished");
		}
	}
	
	public ArrayList<AlleleSNPDocument> getAlleleSNPDocument(SQLExecutor exec, int start, int end) throws SQLException {
		
		HashMap<String, ArrayList<String>> strainsMap = setupStrainsMap(exec, start, end);
		HashMap<String, ArrayList<String>> functionClassesMap = setupFunctionClassMap(exec, start, end);
		HashMap<String, ArrayList<String>> markersMap = setupMarkersMap(exec, start, end);
		HashMap<String, HashMap<String, ArrayList<String>>> allelesStrainsMap = setupAllelesStrainsMap(exec, start, end);
		
		ResultSet set = exec.executeQuery("select "
				+ "sa.accid as consensussnp_accid, sa._object_key, scc.chromosome, scc.startcoordinate, scc._varclass_key "
				+ "from "
				+ "snp.snp_accession sa, snp.snp_coord_cache scc "
				+ "left join snp.snp_consensussnp_marker scm on "
				+ "scc._consensussnp_key = scm._consensussnp_key "
				+ "where "
				+ "sa._object_key = scc._consensussnp_key and sa._logicaldb_key = 73 and sa._mgitype_key = 30 and "
				+ "scc.ismulticoord = 0 and "
				+ "sa._object_key > " + start + " and sa._object_key <= " + end + " "
				+ "group by sa.accid, sa._object_key, scc.chromosome, scc.startcoordinate, scc._varclass_key "
				+ "order by sa._object_key "
		);

		ArrayList<AlleleSNPDocument> ret = new ArrayList<AlleleSNPDocument>();

		while (set.next()) {

			if(allelesStrainsMap.get(set.getString("_object_key")) != null && allelesStrainsMap.get(set.getString("_object_key")).keySet().size() > 0) {
				for(String allele: allelesStrainsMap.get(set.getString("_object_key")).keySet()) {
					AlleleSNPDocument doc = createDocument(set, strainsMap, functionClassesMap, markersMap, allelesStrainsMap);
					
					doc.setAllele(allele);

					try {
						doc.setSamestrains(allelesStrainsMap.get(set.getString("_object_key")).get(allele));
					} catch (NullPointerException e) {
						log.info(set.getString("_object_key"));
						log.info("Allele: " + allele);
						log.info(allelesStrainsMap.get(set.getString("_object_key")));
						log.info(allelesStrainsMap.get(set.getString("_object_key")).keySet());
					}
					
					// Compute the strains that have a different allele
					HashMap<String, ArrayList<String>> alleles = allelesStrainsMap.get(set.getString("_object_key"));
					
					ArrayList<String> diffStrains = new ArrayList<String>();
					
					for(String loopAllele: alleles.keySet()) {
						if(!loopAllele.equals(allele)) {
							diffStrains.addAll(alleles.get(loopAllele));
						}
					}
					
					doc.setDiffstrains(diffStrains);

					ret.add(doc);
				}
			} else {
				AlleleSNPDocument doc = createDocument(set, strainsMap, functionClassesMap, markersMap, allelesStrainsMap);
				ret.add(doc);
			}

		}
		set.close();
		
		return ret;

	}

	private AlleleSNPDocument createDocument(ResultSet set, HashMap<String, ArrayList<String>> strainsMap, HashMap<String, ArrayList<String>> functionClassesMap, HashMap<String, ArrayList<String>> markersMap, HashMap<String, HashMap<String, ArrayList<String>>> allelesStrainsMap) throws SQLException {

		AlleleSNPDocument doc = new AlleleSNPDocument();
		
		doc.setConsensussnp_accid(set.getString("consensussnp_accid"));
		doc.setChromosome(set.getString("chromosome"));
		doc.setStartcoordinate(set.getDouble("startcoordinate"));
		doc.setVarclass(variationMap.get(set.getString("_varclass_key")));
		
		if(functionClassesMap.containsKey(set.getString("_object_key"))) {
			doc.setFxn(functionClassesMap.get(set.getString("_object_key")));
		}
		if(markersMap.containsKey(set.getString("_object_key"))) {
			doc.setMarker_accid(markersMap.get(set.getString("_object_key")));
		}
		
		doc.setStrains(strainsMap.get(set.getString("_object_key")));
		
		return doc;
	}

	private HashMap<String, ArrayList<String>> setupMarkersMap(SQLExecutor exec, int start, int end) throws SQLException {
		HashMap<String, ArrayList<String>> markersMap = new HashMap<String, ArrayList<String>>();
		
		ResultSet set = exec.executeQuery("select scm._consensussnp_key, scm._marker_key from snp.snp_consensussnp_marker scm where scm._consensussnp_key > " + start + " and scm._consensussnp_key <= " + end + " group by scm._consensussnp_key, scm._marker_key");
		
		while(set.next()) {
			ArrayList<String> list = markersMap.get(set.getString("_consensussnp_key"));
			if(list == null) {
				list = new ArrayList<String>();
				markersMap.put(set.getString("_consensussnp_key"), list);
			}
			list.add(markerMap.get(set.getString("_marker_key")));
		}
		set.close();
		return markersMap;
	}
	
	private HashMap<String, ArrayList<String>> setupFunctionClassMap(SQLExecutor exec, int start, int end) throws SQLException {
		HashMap<String, ArrayList<String>> functionClassesMap = new HashMap<String, ArrayList<String>>();
		
		ResultSet set = exec.executeQuery("select scm._consensussnp_key, scm._fxn_key from snp.snp_consensussnp_marker scm where scm._consensussnp_key > " + start + " and scm._consensussnp_key <= " + end + " group by scm._consensussnp_key, scm._fxn_key");
		
		while(set.next()) {
			ArrayList<String> list = functionClassesMap.get(set.getString("_consensussnp_key"));
			if(list == null) {
				list = new ArrayList<String>();
				functionClassesMap.put(set.getString("_consensussnp_key"), list);
			}
			list.add(functionMap.get(set.getString("_fxn_key")));
		}
		set.close();
		return functionClassesMap;
	}

	private HashMap<String, ArrayList<String>> setupStrainsMap(SQLExecutor exec, int start, int end) throws SQLException {
		HashMap<String, ArrayList<String>> strainsMap = new HashMap<String, ArrayList<String>>();
		
		ResultSet set = exec.executeQuery("select scs._consensussnp_key, scs._mgdstrain_key from snp.snp_consensussnp_strainallele scs where scs._consensussnp_key > " + start + " and scs._consensussnp_key <= " + end + " ");
		
		while (set.next()) {
			ArrayList<String> list = strainsMap.get(set.getString("_consensussnp_key"));
			if(list == null) {
				list = new ArrayList<String>();
				strainsMap.put(set.getString("_consensussnp_key"), list);
			}
			list.add(strainMap.get(set.getString("_mgdstrain_key")));
		}
		set.close();
		return strainsMap;
	}

	private HashMap<String, HashMap<String, ArrayList<String>>> setupAllelesStrainsMap(SQLExecutor exec, int start, int end) throws SQLException {
		HashMap<String, HashMap<String, ArrayList<String>>> allelesStrainsMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
		
		ResultSet set = exec.executeQuery("select scs._consensussnp_key, scs._mgdstrain_key, scs.allele from snp.snp_consensussnp_strainallele scs where scs.allele in ('A', 'C', 'G', 'T') and scs._consensussnp_key > " + start + " and scs._consensussnp_key <= " + end + " ");
		
		while (set.next()) {
			HashMap<String, ArrayList<String>> list = allelesStrainsMap.get(set.getString("_consensussnp_key"));
			if(list == null) {
				list = new HashMap<String, ArrayList<String>>();
				allelesStrainsMap.put(set.getString("_consensussnp_key"), list);
			}
			
			ArrayList<String> strainList = list.get(set.getString("allele"));
			if(strainList == null) {
				strainList = new ArrayList<String>();
				list.put(set.getString("allele"), strainList);
			}
			strainList.add(strainMap.get(set.getString("_mgdstrain_key")));
		}
		set.close();
		return allelesStrainsMap;
	}
	
}
