package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

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
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConsensusSNPIndexer extends Indexer {

	private HashMap<Integer, String> strains = null;
	
	private HashMap<Integer, PopulationSNP> populationsByPopulationKey = null;
	private HashMap<Integer, PopulationSNP> populationsBySubHandleKey = null;
	
	private HashMap<Integer, String> functionClasses = null;
	private HashMap<Integer, String> variationClasses = null;
	private HashMap<Integer, Marker> markers = null;
	
	private LinkedBlockingQueue<DBChunk> queue = new LinkedBlockingQueue<>();
	
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
			
			exec.cleanup();

			display.startProcess(config.getIndexerName(), end);

			int chunkSize = config.getChunkSize();
			int chunks = end / chunkSize;

			for(int i = 0; i <= chunks; i++) {
				int start = i * chunkSize;
				
				DBChunk chunk = new DBChunk(start, start + chunkSize);
				
				try {
					queue.put(chunk);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			int workerCount = config.getWorkerCount();
			
			ExecutorService executor = Executors.newFixedThreadPool(workerCount);
			
			for(int i = 0; i < workerCount; i++) {
				executor.execute(new QueueWorker());
			}

			executor.shutdown();
			while (!executor.isTerminated()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
	
	private class QueueWorker implements Runnable {

		@Override
		public void run() {
			log.info("Running QueueWorker");
			try {
				SQLExecutor exec = new SQLExecutor(config.getCursorSize(), false);
				while(!queue.isEmpty()) {
					DBChunk chunk = queue.take();
					HashMap<Integer, ConsensusSNP> consensusList = getConsensusSNP(exec, chunk.start, chunk.end);
					
					ArrayList<ConsensusSNPDocument> docCache = new ArrayList<>();
					
					for(ConsensusSNP snp: consensusList.values()) {
						ConsensusSNPDocument doc = new ConsensusSNPDocument();
						doc.setConsensussnp_accid(snp.getAccid());
						doc.setObjectJSONData(snp);
						docCache.add(doc);
					}
					indexDocuments(docCache);
					docCache.clear();

				}
				exec.cleanup();
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.info("QueueWorker Finished");
		}
	}
	
	public record DBChunk(int start, int end) { }

	public HashMap<Integer, ConsensusSNP> getConsensusSNP(SQLExecutor exec, int start, int end) throws SQLException {
	
		HashMap<Integer, ConsensusSNP> ret = new HashMap<Integer, ConsensusSNP>();

		ResultSet set = exec.executeQuery("select scs._consensussnp_key, sa.accid, scs._varclass_key, scs.allelesummary, scs.iupaccode, scs.buildcreated, scs.buildupdated "
				+ "from snp.snp_accession sa, snp.snp_consensussnp scs where sa._logicaldb_key = 73 and sa._mgitype_key = 30 and "
				+ "scs._consensussnp_key > " + start + " and scs._consensussnp_key <= " + end + " and scs._consensussnp_key = sa._object_key");

		while(set.next()) {
			ConsensusSNP snp = new ConsensusSNP();
			
			snp.setConsensusKey(set.getInt("_consensussnp_key"));
			snp.setAccid(set.getString("accid"));
			snp.setVariationClass(variationClasses.get(set.getInt("_varclass_key")));
			snp.setAlleleSummary(set.getString("allelesummary"));
			snp.setIupaccode(set.getString("iupaccode"));
			snp.setBuildCreated(set.getString("buildcreated"));
			snp.setBuildUpdated(set.getString("buildupdated"));
			
			snp.setConsensusCoordinates(new ArrayList<ConsensusCoordinateSNP>());
			snp.setSubSNPs(new ArrayList<SubSNP>());
			snp.setConsensusAlleles(new ArrayList<ConsensusAlleleSNP>());
			
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

	private void populateFlanks(SQLExecutor exec, HashMap<Integer, ConsensusSNP> consensusSnps, int start, int end, boolean is5Prime) throws SQLException {
		
		LinkedHashMap<Integer, StringBuffer> flanks = new LinkedHashMap<Integer, StringBuffer>();
		
		ResultSet set;
		if(is5Prime) {
			set = exec.executeQuery("select * from snp.snp_flank where _consensussnp_key > " + start + " and _consensussnp_key <= " + end + " and is5prime = 1");
		} else {
			set = exec.executeQuery("select * from snp.snp_flank where _consensussnp_key > " + start + " and _consensussnp_key <= " + end + " and is5prime = 0");
		}
		
		while(set.next()) {
			int key = set.getInt("_consensussnp_key");
			if(!flanks.containsKey(key)) {
				flanks.put(key, new StringBuffer());
			}
			flanks.get(key).append(set.getString("flank"));
		}
		set.close();
		
		for(Integer i: flanks.keySet()) {
			if(is5Prime) {
				consensusSnps.get(i).setFlank5Prime(flanks.get(i).toString());
			} else {
				consensusSnps.get(i).setFlank3Prime(flanks.get(i).toString());
			}
		}
	}

	private void populateSubSnps(SQLExecutor exec, HashMap<Integer, ConsensusSNP> consensusSnps, int start, int end) throws SQLException {
		ResultSet set = exec.executeQuery("select sss._subsnp_key, sss._consensussnp_key, sss._subhandle_key, sa.accid, sss.allelesummary, sss.isexemplar, sss.orientation, sss._varclass_key, "
				+ "sa2.accid as submitter from snp.snp_subsnp sss, snp.snp_accession sa, snp.snp_accession sa2 where sss._subsnp_key = sa._object_key "
				+ "and sss._consensussnp_key > " + start + " and sss._consensussnp_key <= " + end + " and sa._logicaldb_key = 74 and sa._mgitype_key = 31 and "
						+ "sss._subsnp_key = sa2._object_key and sa2._logicaldb_key = 75 and sa2._mgitype_key = 31");

		HashMap<Integer, SubSNP> snps = new HashMap<Integer, SubSNP>();
		
		while(set.next()) {
			SubSNP snp = new SubSNP();
			snp.setSubSnpKey(set.getInt("_subsnp_key"));
			snp.setAccid(set.getString("accid"));
			snp.setAlleleSummary(set.getString("allelesummary"));
			
			snp.setExemplar(set.getInt("isexemplar") == 1);
			snp.setOrientation(set.getString("orientation"));
			snp.setSubmitterId(set.getString("submitter"));
			snp.setVariationClass(variationClasses.get(set.getInt("_varclass_key")));
			//LinkedHashMap<Integer, PopulationSNP> pops = new LinkedHashMap<Integer, PopulationSNP>();
			PopulationSNP p = populationsBySubHandleKey.get(set.getInt("_subhandle_key")).dup();
			snp.populations.put(p.getPopulationKey(), p);
			snps.put(set.getInt("_subsnp_key"), snp);

			consensusSnps.get(set.getInt("_consensussnp_key")).getSubSNPs().add(snp);
		}

		populatePopulations(exec, snps, start, end);
		
	}
	
	private void populateConsensusAlleles(SQLExecutor exec, HashMap<Integer, ConsensusSNP> consensusSnps, int start, int end) throws SQLException {
		ResultSet set = exec.executeQuery("select ssca._consensussnp_key, ssca._mgdstrain_key, ssca.isconflict, ssca.allele from snp.snp_consensussnp_strainallele ssca where ssca._consensussnp_key > " + start + " and ssca._consensussnp_key <= " + end);
		while(set.next()) {
			ConsensusAlleleSNP ca = new ConsensusAlleleSNP();
			ca.setAllele(set.getString("allele"));
			ca.setConflict(set.getInt("isconflict") == 1);
			ca.setStrain(strains.get(set.getInt("_mgdstrain_key")));
			consensusSnps.get(set.getInt("_consensussnp_key")).getConsensusAlleles().add(ca);
		}
		set.close();
	}
	
	private void populateCoordinateCache(SQLExecutor exec, HashMap<Integer, ConsensusSNP> consensusSnps, int start, int end) throws SQLException {
		HashMap<Integer, ConsensusCoordinateSNP> coords = new HashMap<Integer, ConsensusCoordinateSNP>();
		
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
			c.setVariationClass(variationClasses.get(set.getInt("_varclass_key")));
			
			c.setMarkers(new ArrayList<ConsensusMarkerSNP>());
			consensusSnps.get(set.getInt("_consensussnp_key")).getConsensusCoordinates().add(c);
			coords.put(set.getInt("_coord_cache_key"), c);
		}
		set.close();
		
		populateConsensusMarkers(exec, coords, start, end);
	}
	
	private void populateConsensusMarkers(SQLExecutor exec, HashMap<Integer, ConsensusCoordinateSNP> coords, int start, int end) throws SQLException {
		ResultSet set = exec.executeQuery("select scm._coord_cache_key, scm._marker_key, scm._consensussnp_key, scm._fxn_key, scm._consensussnp_marker_key, scm.contig_allele, scm.residue, scm.aa_position, scm.reading_frame, scm.distance_from, scm.distance_direction, stp.transcriptid, stp.proteinid "
				+ "from snp.snp_consensussnp_marker scm left join snp.snp_transcript_protein stp on scm._transcript_protein_key = stp._transcript_protein_key "
				+ "where scm._consensussnp_key > " + start + " and scm._consensussnp_key <= " + end);

		while(set.next()) {
			int key = set.getInt("_marker_key");
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
				
				c.setFunctionClass(functionClasses.get(set.getInt("_fxn_key")));
				
				coords.get(set.getInt("_coord_cache_key")).getMarkers().add(c);
			}

		}
		set.close();
	
	}

	private void populatePopulations(SQLExecutor exec, HashMap<Integer, SubSNP> snps, int start, int end) throws SQLException {
		ResultSet set = exec.executeQuery(
			"select ssa._subsnp_key, ssa._population_key, ssa._mgdstrain_key, ssa.allele "
			+ "from snp.snp_subsnp sss, snp.snp_subsnp_strainallele ssa "
			+ "where sss._consensussnp_key > " + start
			+ " and sss._consensussnp_key <= " + end
			+ " and sss._subsnp_key = ssa._subsnp_key");

		while(set.next()) {
			int snpKey = set.getInt("_subsnp_key");
			int popKey = set.getInt("_population_key");
			
			PopulationSNP p = snps.get(snpKey).populations.get(popKey);
			if(p == null) {
				p = populationsByPopulationKey.get(popKey).dup();
				snps.get(snpKey).populations.put(p.getPopulationKey(), p);
			}
			
			AlleleSNP a = new AlleleSNP();
			a.setAllele(set.getString("allele"));
			a.setStrain(strains.get(set.getInt("_mgdstrain_key")));
			p.getAlleles().add(a);
		}
		set.close();
	}
	
	private void setupStrains(SQLExecutor exec) throws SQLException {
		if(strains == null) {
			log.info("Setting Up Strains: ");
			strains = new HashMap<Integer, String>();
			
			ResultSet set = exec.executeQuery("select * from snp.snp_strain");
			while(set.next()) {
				strains.put(set.getInt("_mgdstrain_key"), set.getString("strain"));
			}
			set.close();
		}
	}
	
	private void setupFunctionClasses(SQLExecutor exec) throws SQLException {
		if(functionClasses == null) {
			log.info("Setting Up Function Classes: ");
			functionClasses = new HashMap<Integer, String>();
			
			ResultSet set = exec.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 49");
			while(set.next()) {
				functionClasses.put(set.getInt("_term_key"), set.getString("term"));
			}
			set.close();
		}
	}
	
	private void setupVariationClasses(SQLExecutor exec) throws SQLException {
		if(variationClasses == null) {
			log.info("Setting Up Variation Classes: ");
			variationClasses = new HashMap<Integer, String>();
			
			ResultSet set = exec.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 50");
			while(set.next()) {
				variationClasses.put(set.getInt("_term_key"), set.getString("term"));
			}
			set.close();
		}
	}
	
	private void setupPopulations(SQLExecutor exec) throws SQLException {
		if(populationsByPopulationKey == null || populationsBySubHandleKey == null) {
			log.info("Setting Up Populations: ");
			populationsByPopulationKey = new HashMap<Integer, PopulationSNP>();
			populationsBySubHandleKey = new HashMap<Integer, PopulationSNP>();
			
			ResultSet set = exec.executeQuery(
				"select sp._population_key, sa.accid, sp.subhandle, sp._subhandle_key, sp.name "
				+ "from snp.snp_population sp "
				+ "left outer join snp.snp_accession sa on ("
				+ " sp._population_key = sa._object_key "
				+ " and sa._logicaldb_key = 76 and sa._mgitype_key = 33)");
			
			while(set.next()) {
				PopulationSNP p = new PopulationSNP();
				p.setPopulationKey(set.getInt("_population_key"));
				p.setAccid(set.getString("accid"));
				p.setPopulationName(set.getString("name"));
				p.setSubHandleName(set.getString("subhandle"));
				p.setAlleles(new ArrayList<AlleleSNP>());
				populationsByPopulationKey.put(set.getInt("_population_key"), p);
				populationsBySubHandleKey.put(set.getInt("_subhandle_key"), p);
			}
			set.close();
		}

	}

	private void setupMarkers(SQLExecutor exec) throws SQLException {
		if(markers == null) {
			log.info("Setting Up Markers: ");
			markers = new HashMap<Integer, Marker>();
			ResultSet set = exec.executeQuery("select a.accid, m.name, m.symbol, m._marker_key from mgd.mrk_marker m, mgd.acc_accession a where m._marker_key = a._object_key and a._logicaldb_key = 1 and a._mgitype_key = 2 and a.preferred = 1 and m._organism_key = 1 and m._marker_status_key = 1");
			
			while(set.next()) {
				Marker m = new Marker();
				m.accid = set.getString("accid");
				m.name = set.getString("name");
				m.symbol = set.getString("symbol");
				markers.put(set.getInt("_marker_key"), m);
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
