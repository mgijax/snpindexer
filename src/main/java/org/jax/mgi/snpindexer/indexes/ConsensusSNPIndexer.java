package org.jax.mgi.snpindexer.indexes;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.solr.common.SolrInputDocument;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.jax.mgi.snpdatamodel.AlleleSNP;
import org.jax.mgi.snpdatamodel.ConsensusCoordinateSNP;
import org.jax.mgi.snpdatamodel.ConsensusMarkerSNP;
import org.jax.mgi.snpdatamodel.ConsensusSNP;
import org.jax.mgi.snpdatamodel.PopulationSNP;
import org.jax.mgi.snpdatamodel.SubSNP;

public class ConsensusSNPIndexer extends Indexer {

	private HashMap<Integer, String> strains = null;
	private HashMap<Integer, String> proteins = null;
	private HashMap<Integer, String> transcripts = null;
	
	private HashMap<Integer, PopulationSNP> populationsByPopulationKey = null;
	private HashMap<Integer, PopulationSNP> populationsBySubHandleKey = null;
	
	private HashMap<Integer, String> functionClasses = null;
	private HashMap<Integer, String> variationClasses = null;
	private HashMap<Integer, Marker> markers = null;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	public ConsensusSNPIndexer(IndexerConfig config) {
		super(config);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
	}

	@Override
	public void index() {
		resetIndex();
	
		try {
			
			//Test Cases rs3163500,rs3657285,rs36238069,rs49786457,rs27287906,rs4228732,rs29392909,rs26922505,rs27287906
			
			setupStrains();
			setupPopulations();
			setupTranscripts();
			setupProteins();
			setupFunctionClasses();
			setupVariationClasses();
			setupMarkers();
			
			int end = getMaxConsensus();

			int chunkSize = config.getChunkSize();
			int chunks = end / chunkSize;
			
			startProcess(chunks, chunkSize, end);
			
			for(int i = 0; i <= chunks; i++) {
				int start = i * chunkSize;

				HashMap<Integer, ConsensusSNP> consensusList = getConsensusSNP(start, (start + chunkSize));
				
				ArrayList<SolrInputDocument> docCache = new ArrayList<SolrInputDocument>();
				for(ConsensusSNP c: consensusList.values()) {
//					PrintVisitor pi = new PrintVisitor();
//					c.Accept(pi);
//					pi.generateOutput(System.out);
					
					SolrInputDocument doc = new SolrInputDocument();
					doc.addField("consensussnp_accid", c.getAccid());
					try {
						String json = mapper.writeValueAsString(c);
						doc.addField("objectJSONData", json);
					} catch (JsonGenerationException e) {
						e.printStackTrace();
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					docCache.add(doc);
				}
				addDocuments(docCache);
				progress(i, chunks, chunkSize);
			}
			
			finishProcess(end);
			
			sql.cleanup();

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		finish();
	}

	public HashMap<Integer, ConsensusSNP> getConsensusSNP(int start, int end) throws SQLException {
	
		HashMap<Integer, ConsensusSNP> ret = new HashMap<Integer, ConsensusSNP>();

		ResultSet set = sql.executeQuery("select scs._consensussnp_key, sa.accid, scs._varclass_key, scs.allelesummary, scs.iupaccode, scs.buildcreated, scs.buildupdated "
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
			snp.setAlleles(new ArrayList<AlleleSNP>());
			
			ret.put(snp.getConsensusKey(), snp);
		}

		set.close();

		populateFlanks(ret, start, end, true);
		populateFlanks(ret, start, end, false);
		populateSubSnps(ret, start, end);
		populateConsensusAlleles(ret, start, end);
		populateCoordinateCache(ret, start, end);
		
		return ret;
	}

	private void populateFlanks(HashMap<Integer, ConsensusSNP> consensusSnps, int start, int end, boolean is5Prime) throws SQLException {
		
		LinkedHashMap<Integer, StringBuffer> flanks = new LinkedHashMap<Integer, StringBuffer>();
		
		ResultSet set;
		if(is5Prime) {
			set = sql.executeQuery("select * from snp.snp_flank where _consensussnp_key > " + start + " and _consensussnp_key <= " + end + " and is5prime = 1");
		} else {
			set = sql.executeQuery("select * from snp.snp_flank where _consensussnp_key > " + start + " and _consensussnp_key <= " + end + " and is5prime = 0");
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

	private void populateSubSnps(HashMap<Integer, ConsensusSNP> consensusSnps, int start, int end) throws SQLException {
		ResultSet set = sql.executeQuery("select sss._subsnp_key, sss._consensussnp_key, sss._subhandle_key, sa.accid, sss.allelesummary, sss.isexemplar, sss.orientation, sss._varclass_key, "
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
			LinkedHashMap<String, PopulationSNP> pops = new LinkedHashMap<String, PopulationSNP>();
			PopulationSNP p = populationsBySubHandleKey.get(set.getInt("_subhandle_key")).dup();
			pops.put(p.getAccid(), p);
			snp.setPopulations(pops);
			snps.put(set.getInt("_subsnp_key"), snp);

			consensusSnps.get(set.getInt("_consensussnp_key")).getSubSNPs().add(snp);
		}

		populatePopulations(snps, start, end);
		
	}
	
	private void populateConsensusAlleles(HashMap<Integer, ConsensusSNP> consensusSnps, int start, int end) throws SQLException {
		ResultSet set = sql.executeQuery("select ssca._consensussnp_key, ssca._mgdstrain_key, ssca.isconflict, ssca.allele from snp.snp_consensussnp_strainallele ssca where ssca._consensussnp_key > " + start + " and ssca._consensussnp_key <= " + end);
		while(set.next()) {
			AlleleSNP a = new AlleleSNP();
			a.setAllele(set.getString("allele"));
			a.setConflict(set.getInt("isconflict") == 1);
			a.setStrain(strains.get(set.getInt("_mgdstrain_key")));
			consensusSnps.get(set.getInt("_consensussnp_key")).getAlleles().add(a);
		}
		set.close();
	}
	
	private void populateCoordinateCache(HashMap<Integer, ConsensusSNP> consensusSnps, int start, int end) throws SQLException {
		HashMap<Integer, ConsensusCoordinateSNP> coords = new HashMap<Integer, ConsensusCoordinateSNP>();
		
		ResultSet set = sql.executeQuery("select scc._coord_cache_key, scc._consensussnp_key, scc.chromosome, scc.startcoordinate, scc.ismulticoord, scc.strand, scc._varclass_key, "
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
		
		populateConsensusMarkers(coords, start, end);
		
	}
	
	private void populateConsensusMarkers(HashMap<Integer, ConsensusCoordinateSNP> coords, int start, int end) throws SQLException {
		ResultSet set = sql.executeQuery("select scm._coord_cache_key, scm._marker_key, scm._consensussnp_key, scm._fxn_key, scm._consensussnp_marker_key, scm.contig_allele, scm.residue, scm.aa_position, scm.reading_frame "
				+ "from snp.snp_consensussnp_marker scm "
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
				
				c.setFunctionClass(functionClasses.get(set.getInt("_fxn_key")));
				c.setProtein(proteins.get(set.getInt("_consensussnp_marker_key")));
				c.setTranscript(transcripts.get(set.getInt("_consensussnp_marker_key")));
				
				coords.get(set.getInt("_coord_cache_key")).getMarkers().add(c);
			}

		}
		set.close();
	
	}

	private void populatePopulations(HashMap<Integer, SubSNP> snps, int start, int end) throws SQLException {
		ResultSet set = sql.executeQuery("select ssa._subsnp_key, ssa._population_key, ssa._mgdstrain_key, ssa.allele from snp.snp_subsnp sss, snp.snp_subsnp_strainallele ssa where sss._consensussnp_key > " + start + " and sss._consensussnp_key <= " + end + " and sss._subsnp_key = ssa._subsnp_key");

		while(set.next()) {
			
			PopulationSNP p = snps.get(set.getInt("_subsnp_key")).getPopulations().get(set.getInt("_population_key"));
			if(p == null) {
				p = populationsByPopulationKey.get(set.getInt("_population_key")).dup();
				snps.get(set.getInt("_subsnp_key")).getPopulations().put(p.getAccid(), p);
			}
			
			AlleleSNP a = new AlleleSNP();
			a.setAllele(set.getString("allele"));
			a.setConflict(false);
			a.setStrain(strains.get(set.getInt("_mgdstrain_key")));
			p.getAlleles().add(a);
		}
		set.close();
	}
	
	private void setupStrains() throws SQLException {
		if(strains == null) {
			strains = new HashMap<Integer, String>();
			
			ResultSet set = sql.executeQuery("select * from snp.snp_strain");
			while(set.next()) {
				strains.put(set.getInt("_mgdstrain_key"), set.getString("strain"));
			}
			set.close();
		}
	}
	
	private void setupTranscripts() throws SQLException {
		if(transcripts == null) {
			transcripts = new HashMap<Integer, String>();
			
			ResultSet set = sql.executeQuery("select _object_key, accid from snp.snp_accession where _logicaldb_key = 27 and _mgitype_key = 32 and prefixpart = 'NM_'");
			while(set.next()) {
				transcripts.put(set.getInt("_object_key"), set.getString("accid"));
			}
			set.close();
		}
	}
	
	private void setupProteins() throws SQLException {
		if(proteins == null) {
			proteins = new HashMap<Integer, String>();
			
			ResultSet set = sql.executeQuery("select _object_key, accid from snp.snp_accession where _logicaldb_key = 27 and _mgitype_key = 32 and prefixpart = 'NP_'");
			while(set.next()) {
				proteins.put(set.getInt("_object_key"), set.getString("accid"));
			}
			set.close();
		}
	}
	
	private void setupFunctionClasses() throws SQLException {
		if(functionClasses == null) {
			functionClasses = new HashMap<Integer, String>();
			
			ResultSet set = sql.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 49");
			while(set.next()) {
				functionClasses.put(set.getInt("_term_key"), set.getString("term"));
			}
			set.close();
		}
	}
	
	private void setupVariationClasses() throws SQLException {
		if(variationClasses == null) {
			variationClasses = new HashMap<Integer, String>();
			
			ResultSet set = sql.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 50");
			while(set.next()) {
				variationClasses.put(set.getInt("_term_key"), set.getString("term"));
			}
			set.close();
		}
	}
	
	private void setupPopulations() throws SQLException {
		if(populationsByPopulationKey == null || populationsBySubHandleKey == null) {
			populationsByPopulationKey = new HashMap<Integer, PopulationSNP>();
			populationsBySubHandleKey = new HashMap<Integer, PopulationSNP>();
			
			ResultSet set = sql.executeQuery("select sp._population_key, sa.accid, sp.subhandle, sp._subhandle_key, sp.name from snp.snp_population sp, snp.snp_accession sa where sp._population_key = sa._object_key and sa._logicaldb_key = 76 and sa._mgitype_key = 33");
			
			while(set.next()) {
				PopulationSNP p = new PopulationSNP();
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

	private void setupMarkers() throws SQLException {
		if(markers == null) {
			markers = new HashMap<Integer, Marker>();
			ResultSet set = sql.executeQuery("select a.accid, m.name, m.symbol, m._marker_key from mgd.mrk_marker m, mgd.acc_accession a where m._marker_key = a._object_key and a._logicaldb_key = 1 and a._mgitype_key = 2 and a.preferred = 1 and m._organism_key = 1 and m._marker_status_key in (1, 3)");
			
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

	public int getMaxConsensus() throws SQLException {
		ResultSet set = sql.executeQuery("select max(scs._consensussnp_key) as maxKey from snp.snp_consensussnp scs");
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
