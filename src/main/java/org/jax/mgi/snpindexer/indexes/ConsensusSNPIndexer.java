package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jax.mgi.snpdatamodel.AlleleSNP;
import org.jax.mgi.snpdatamodel.ConsensusAlleleSNP;
import org.jax.mgi.snpdatamodel.ConsensusCoordinateSNP;
import org.jax.mgi.snpdatamodel.ConsensusMarkerSNP;
import org.jax.mgi.snpdatamodel.ConsensusSNP;
import org.jax.mgi.snpdatamodel.PopulationSNP;
import org.jax.mgi.snpdatamodel.SubSNP;
import org.jax.mgi.snpdatamodel.document.ConsensusSNPDocument;
import org.jax.mgi.snpindexer.config.IndexerConfig;
import org.jax.mgi.snpindexer.util.SQLExecutor;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsensusSNPIndexer extends Indexer {

	private HashMap<String, String> strains = null;
	
	private HashMap<String, PopulationSNP> populationsByPopulationKey = null;
	private HashMap<String, PopulationSNP> populationsBySubHandleKey = null;
	
	private HashMap<String, String> functionClasses = null;
	private HashMap<String, String> variationClasses = null;
	private HashMap<String, Marker> markers = null;
	
	private LinkedBlockingQueue<DBChunk> dbWorkQueue = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<ArrayList<ConsensusSNPDocument>> jsonWorkQueue = new LinkedBlockingQueue<>(10);

	private ObjectMapper mapper = new ObjectMapper();
	
	public ConsensusSNPIndexer(IndexerConfig config) {
		super(config);
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public void index() {

		try {
			
			//Test Cases rs3163500,rs3657285,rs36238069,rs49786457,rs27287906,rs4228732,rs29392909,rs26922505,rs27287906
			SQLExecutor exec = new SQLExecutor(config.getCursorSize(), false);
			
			setupStrains(exec);
			setupPopulations(exec);
			setupFunctionClasses(exec);
			setupVariationClasses(exec);
			setupMarkers(exec);
			
			int end = getMaxConsensus(exec);
			
			//setupFlanks(exec, end);
			
			exec.cleanup();

			display.startProcess(config.getIndexerName() + "   DB", end);
			jsonDisplay.startProcess(config.getIndexerName() + " JSON", end);

			int chunkSize = config.getChunkSize();
			int chunks = end / chunkSize;

			for(int i = 0; i <= chunks; i++) {
				int start = i * chunkSize;
				
				DBChunk chunk = new DBChunk(start, start + chunkSize);
				
				try {
					dbWorkQueue.put(chunk);
				} catch (InterruptedException e) {
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
			ArrayList<ConsensusSNPDocumentBuilderQueueWorker> dbWorkers = new ArrayList<>();
			
			for(int i = 0; i < dbWorkerCount; i++) {
				ConsensusSNPDocumentBuilderQueueWorker worker = new ConsensusSNPDocumentBuilderQueueWorker();
				worker.start();
				dbWorkers.add(worker);
			}

			try {
				log.info("Waiting for all workers to start up");
				Thread.sleep(15000);
				
				log.info("Waiting for dbWorkers to finish");
				for (ConsensusSNPDocumentBuilderQueueWorker w: dbWorkers) {
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
				System.exit(1);
			}

			
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}


	private class JsonQueueWorker extends Thread {
		public void run() {
			log.info("Running JsonQueueWorker");
			try {
				while (!(Thread.currentThread().isInterrupted())) {
					ArrayList<ConsensusSNPDocument> consensusList = jsonWorkQueue.take();
					ArrayList<String> docCache = new ArrayList<>();
					for(ConsensusSNPDocument doc: consensusList) {
						try {
							String json = mapper.writeValueAsString(doc);
							docCache.add(json);
							jsonDisplay.progressProcess();
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
					}
					consensusList.clear();
					indexJsonDocuments(docCache);
					docCache.clear();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			log.info("JsonQueueWorker Finished");
		}
	}
	
	private class ConsensusSNPDocumentBuilderQueueWorker extends Thread {

		public void run() {
			log.info("Running ConsensusSNPDocumentBuilderQueueWorker");
			try {
				SQLExecutor exec = new SQLExecutor(config.getCursorSize(), false);
				while(!dbWorkQueue.isEmpty()) {
					DBChunk chunk = dbWorkQueue.take();
					HashMap<String, ConsensusSNP> consensusList = getConsensusSNP(exec, chunk.start(), chunk.end());
					
					ArrayList<ConsensusSNPDocument> docCache = new ArrayList<>();
					
					for(ConsensusSNP snp: consensusList.values()) {
						ConsensusSNPDocument doc = new ConsensusSNPDocument();
						doc.setConsensussnp_accid(snp.getAccid());
						doc.setObjectJSONData(snp);
						docCache.add(doc);
						display.progressProcess();
					}
					jsonWorkQueue.offer(docCache, 1, TimeUnit.DAYS);
				}
				exec.cleanup();
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.info("ConsensusSNPDocumentBuilderQueueWorker Finished");
		}
	}

	public HashMap<String, ConsensusSNP> getConsensusSNP(SQLExecutor exec, int start, int end) throws SQLException {
	
		HashMap<String, ConsensusSNP> ret = new HashMap<>();

		ResultSet set = exec.executeQuery("select scs._consensussnp_key, sa.accid, scs._varclass_key, scs.allelesummary, scs.iupaccode, scs.buildcreated, scs.buildupdated "
				+ "from snp.snp_accession sa, snp.snp_consensussnp scs where sa._logicaldb_key = 73 and sa._mgitype_key = 30 and "
				+ "scs._consensussnp_key > " + start + " and scs._consensussnp_key <= " + end + " and scs._consensussnp_key = sa._object_key");

		while(set.next()) {
			ConsensusSNP snp = new ConsensusSNP();
			
			snp.setConsensusKey(set.getString("_consensussnp_key"));
			snp.setAccid(set.getString("accid"));
			snp.setVariationClass(variationClasses.get(set.getString("_varclass_key")));
			snp.setAlleleSummary(set.getString("allelesummary"));
			snp.setIupaccode(set.getString("iupaccode"));
			snp.setBuildCreated(set.getString("buildcreated"));
			snp.setBuildUpdated(set.getString("buildupdated"));
			
			snp.setConsensusCoordinates(new ArrayList<ConsensusCoordinateSNP>());
			snp.setSubSNPs(new ArrayList<SubSNP>());
			snp.setConsensusAlleles(new ArrayList<ConsensusAlleleSNP>());
			
			//snp.setFlank5Prime(flank5primeMap.get(snp.getConsensusKey()));
			//snp.setFlank3Prime(flank3primeMap.get(snp.getConsensusKey()));
			
			ret.put(snp.getConsensusKey(), snp);

		}

		set.close();

		populateFlanks(exec, ret, start, end, true);
		populateFlanks(exec, ret, start, end, false);
		populateSubSnps(exec, ret, start, end);
		populateConsensusAlleles(exec, ret, start, end);
		populateCoordinateCache(exec, ret, start, end);

		return ret;
	}

	private void populateFlanks(SQLExecutor exec, HashMap<String, ConsensusSNP> consensusSnps, int start, int end, boolean is5Prime) throws SQLException {
		
		LinkedHashMap<String, StringBuffer> flanks = new LinkedHashMap<>();
		
		ResultSet set;
		if(is5Prime) {
			set = exec.executeQuery("select * from snp.snp_flank where _consensussnp_key > " + start + " and _consensussnp_key <= " + end + " and is5prime = 1");
		} else {
			set = exec.executeQuery("select * from snp.snp_flank where _consensussnp_key > " + start + " and _consensussnp_key <= " + end + " and is5prime = 0");
		}
		
		while(set.next()) {
			String key = set.getString("_consensussnp_key");
			if(!flanks.containsKey(key)) {
				flanks.put(key, new StringBuffer());
			}
			flanks.get(key).append(set.getString("flank"));
		}
		set.close();
		
		for(String i: flanks.keySet()) {
			if(is5Prime) {
				consensusSnps.get(i).setFlank5Prime(flanks.get(i).toString());
			} else {
				consensusSnps.get(i).setFlank3Prime(flanks.get(i).toString());
			}
		}
	}

	private void populateSubSnps(SQLExecutor exec, HashMap<String, ConsensusSNP> consensusSnps, int start, int end) throws SQLException {
		ResultSet set = exec.executeQuery("select sss._subsnp_key, sss._consensussnp_key, sss._subhandle_key, sa.accid, sss.allelesummary, sss.isexemplar, sss.orientation, sss._varclass_key, "
				+ "sa2.accid as submitter from snp.snp_subsnp sss, snp.snp_accession sa, snp.snp_accession sa2 where sss._subsnp_key = sa._object_key "
				+ "and sss._consensussnp_key > " + start + " and sss._consensussnp_key <= " + end + " and sa._logicaldb_key = 74 and sa._mgitype_key = 31 and "
						+ "sss._subsnp_key = sa2._object_key and sa2._logicaldb_key = 75 and sa2._mgitype_key = 31");

		HashMap<String, SubSNP> snps = new HashMap<String, SubSNP>();
		
		while(set.next()) {
			SubSNP snp = new SubSNP();
			snp.setSubSnpKey(set.getString("_subsnp_key"));
			snp.setAccid(set.getString("accid"));
			snp.setAlleleSummary(set.getString("allelesummary"));
			
			snp.setExemplar(set.getInt("isexemplar") == 1);
			snp.setOrientation(set.getString("orientation"));
			snp.setSubmitterId(set.getString("submitter"));
			snp.setVariationClass(variationClasses.get(set.getString("_varclass_key")));
			//LinkedHashMap<Integer, PopulationSNP> pops = new LinkedHashMap<Integer, PopulationSNP>();
			PopulationSNP p = populationsBySubHandleKey.get(set.getString("_subhandle_key")).dup();
			snp.populations.put(p.getPopulationKey(), p);
			snps.put(set.getString("_subsnp_key"), snp);

			consensusSnps.get(set.getString("_consensussnp_key")).getSubSNPs().add(snp);
		}

		populatePopulations(exec, snps, start, end);
		
	}
	
	private void populateConsensusAlleles(SQLExecutor exec, HashMap<String, ConsensusSNP> consensusSnps, int start, int end) throws SQLException {
		ResultSet set = exec.executeQuery("select ssca._consensussnp_key, ssca._mgdstrain_key, ssca.isconflict, ssca.allele from snp.snp_consensussnp_strainallele ssca where ssca._consensussnp_key > " + start + " and ssca._consensussnp_key <= " + end);
		while(set.next()) {
			ConsensusAlleleSNP ca = new ConsensusAlleleSNP();
			ca.setAllele(set.getString("allele"));
			ca.setConflict(set.getInt("isconflict") == 1);
			ca.setStrain(strains.get(set.getString("_mgdstrain_key")));
			consensusSnps.get(set.getString("_consensussnp_key")).getConsensusAlleles().add(ca);
		}
		set.close();
	}
	
	private void populateCoordinateCache(SQLExecutor exec, HashMap<String, ConsensusSNP> consensusSnps, int start, int end) throws SQLException {
		HashMap<String, ConsensusCoordinateSNP> coords = new HashMap<>();
		
		ResultSet set = exec.executeQuery("select scc._coord_cache_key, scc._consensussnp_key, scc.chromosome, scc.startcoordinate, scc.ismulticoord, scc.strand, scc._varclass_key, "
				+ "scc.allelesummary, scc.iupaccode from snp.snp_coord_cache scc where scc._consensussnp_key > " + start + " and scc._consensussnp_key <= " + end);
		
		while(set.next()) {
			ConsensusCoordinateSNP c = new ConsensusCoordinateSNP();
			c.setAlleleSummary(set.getString("allelesummary"));
			c.setChromosome(set.getString("chromosome"));
			c.setIupaccode(set.getString("iupaccode"));
			c.setMultiCoord(set.getInt("ismulticoord") == 1);
			c.setStartCoordinate((int)set.getDouble("startcoordinate"));
			c.setStrand(set.getString("strand"));
			c.setVariationClass(variationClasses.get(set.getString("_varclass_key")));
			
			c.setMarkers(new ArrayList<ConsensusMarkerSNP>());
			consensusSnps.get(set.getString("_consensussnp_key")).getConsensusCoordinates().add(c);
			coords.put(set.getString("_coord_cache_key"), c);
		}
		set.close();
		
		populateConsensusMarkers(exec, coords, start, end);
	}
	
	private void populateConsensusMarkers(SQLExecutor exec, HashMap<String, ConsensusCoordinateSNP> coords, int start, int end) throws SQLException {
		ResultSet set = exec.executeQuery("select scm._coord_cache_key, scm._marker_key, scm._consensussnp_key, scm._fxn_key, scm._consensussnp_marker_key, scm.contig_allele, scm.residue, scm.aa_position, scm.reading_frame, scm.distance_from, scm.distance_direction, stp.transcriptid, stp.proteinid "
				+ "from snp.snp_consensussnp_marker scm left join snp.snp_transcript_protein stp on scm._transcript_protein_key = stp._transcript_protein_key "
				+ "where scm._consensussnp_key > " + start + " and scm._consensussnp_key <= " + end);

		while(set.next()) {
			String key = set.getString("_marker_key");
			if(markers.containsKey(key)) {
				Marker m = markers.get(key);
				
				ConsensusMarkerSNP c = new ConsensusMarkerSNP();
				
				c.setAccid(m.accid);
				c.setName(m.name);
				c.setSymbol(m.symbol);
				
				c.setAaPosition(set.getString("aa_position"));
				c.setContigAllele(set.getString("contig_allele"));
				c.setReadingFrame(set.getString("reading_frame"));
				c.setResidue(set.getString("residue"));
				
				c.setDistanceFrom(set.getInt("distance_from"));
				c.setDistanceDirection(set.getString("distance_direction"));
				
				c.setProtein(set.getString("proteinid"));
				c.setTranscript(set.getString("transcriptid"));
				
				c.setFunctionClass(functionClasses.get(set.getString("_fxn_key")));
				
				coords.get(set.getString("_coord_cache_key")).getMarkers().add(c);
			}

		}
		set.close();
	
	}

	private void populatePopulations(SQLExecutor exec, HashMap<String, SubSNP> snps, int start, int end) throws SQLException {
		ResultSet set = exec.executeQuery(
			"select ssa._subsnp_key, ssa._population_key, ssa._mgdstrain_key, ssa.allele "
			+ "from snp.snp_subsnp sss, snp.snp_subsnp_strainallele ssa "
			+ "where sss._consensussnp_key > " + start
			+ " and sss._consensussnp_key <= " + end
			+ " and sss._subsnp_key = ssa._subsnp_key");

		while(set.next()) {
			String snpKey = set.getString("_subsnp_key");
			String popKey = set.getString("_population_key");
			
			PopulationSNP p = snps.get(snpKey).populations.get(popKey);
			if(p == null) {
				p = populationsByPopulationKey.get(popKey).dup();
				snps.get(snpKey).populations.put(p.getPopulationKey(), p);
			}
			
			AlleleSNP a = new AlleleSNP();
			a.setAllele(set.getString("allele"));
			a.setStrain(strains.get(set.getString("_mgdstrain_key")));
			p.getAlleles().add(a);
		}
		set.close();
	}
	
	private void setupStrains(SQLExecutor exec) throws SQLException {
		if(strains == null) {
			log.info("Setting Up Strains: ");
			strains = new HashMap<>();
			
			ResultSet set = exec.executeQuery("select * from snp.snp_strain");
			while(set.next()) {
				strains.put(set.getString("_mgdstrain_key"), set.getString("strain"));
			}
			set.close();
		}
	}
	
	private void setupFunctionClasses(SQLExecutor exec) throws SQLException {
		if(functionClasses == null) {
			log.info("Setting Up Function Classes: ");
			functionClasses = new HashMap<>();
			
			ResultSet set = exec.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 49");
			while(set.next()) {
				functionClasses.put(set.getString("_term_key"), set.getString("term"));
			}
			set.close();
		}
	}
	
	private void setupVariationClasses(SQLExecutor exec) throws SQLException {
		if(variationClasses == null) {
			log.info("Setting Up Variation Classes: ");
			variationClasses = new HashMap<String, String>();
			
			ResultSet set = exec.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 50");
			while(set.next()) {
				variationClasses.put(set.getString("_term_key"), set.getString("term"));
			}
			set.close();
		}
	}
	
	private void setupPopulations(SQLExecutor exec) throws SQLException {
		if(populationsByPopulationKey == null || populationsBySubHandleKey == null) {
			log.info("Setting Up Populations: ");
			populationsByPopulationKey = new HashMap<String, PopulationSNP>();
			populationsBySubHandleKey = new HashMap<String, PopulationSNP>();
			
			ResultSet set = exec.executeQuery(
				"select sp._population_key, sa.accid, sp.subhandle, sp._subhandle_key, sp.name "
				+ "from snp.snp_population sp "
				+ "left outer join snp.snp_accession sa on ("
				+ " sp._population_key = sa._object_key "
				+ " and sa._logicaldb_key = 76 and sa._mgitype_key = 33)");
			
			while(set.next()) {
				PopulationSNP p = new PopulationSNP();
				p.setPopulationKey(set.getString("_population_key"));
				p.setAccid(set.getString("accid"));
				p.setPopulationName(set.getString("name"));
				p.setSubHandleName(set.getString("subhandle"));
				p.setAlleles(new ArrayList<>());
				populationsByPopulationKey.put(set.getString("_population_key"), p);
				populationsBySubHandleKey.put(set.getString("_subhandle_key"), p);
			}
			set.close();
		}

	}

	private void setupMarkers(SQLExecutor exec) throws SQLException {
		if(markers == null) {
			log.info("Setting Up Markers: ");
			markers = new HashMap<String, Marker>();
			ResultSet set = exec.executeQuery("select a.accid, m.name, m.symbol, m._marker_key from mgd.mrk_marker m, mgd.acc_accession a where m._marker_key = a._object_key and a._logicaldb_key = 1 and a._mgitype_key = 2 and a.preferred = 1 and m._organism_key = 1 and m._marker_status_key = 1");
			
			while(set.next()) {
				Marker m = new Marker();
				m.accid = set.getString("accid");
				m.name = set.getString("name");
				m.symbol = set.getString("symbol");
				markers.put(set.getString("_marker_key"), m);
			}
			set.close();
		}
	}

	public int getMaxConsensus(SQLExecutor exec) throws SQLException {
		ResultSet set = exec.executeQuery("select max(scs._consensussnp_key) as maxKey from snp.snp_consensussnp scs");
		set.next();
		int end = set.getInt("maxKey");
		set.close();
		return end;
	}
	
	public class Marker {
		public String accid;
		public String name;
		public String symbol;
	}

}
