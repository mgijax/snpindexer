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
import org.jax.mgi.snpdatamodel.AlleleSNP;
import org.jax.mgi.snpdatamodel.ConsensusCoordinateSNP;
import org.jax.mgi.snpdatamodel.ConsensusMarkerSNP;
import org.jax.mgi.snpdatamodel.ConsensusSNP;
import org.jax.mgi.snpdatamodel.PopulationSNP;
import org.jax.mgi.snpdatamodel.SubSNP;

public class ConsensusSNPIndexer extends Indexer {

	private HashMap<Integer, String> strains = null;
	private HashMap<Integer, PopulationSNP> populations = null;
	private ObjectMapper mapper = new ObjectMapper();
	
	public ConsensusSNPIndexer(String coreName) {
		super(coreName);
		try {
			setupStrains();
			setupPopulations();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void index() {
		
		resetIndex();
		
		try {
			
			int end = getMaxConsensus();

			int chunkSize = 10000;
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

		ResultSet set = sql.executeQuery("select scs._consensussnp_key, sa.accid, vt.term, scs.allelesummary, scs.iupaccode, scs.buildcreated, scs.buildupdated from snp.snp_accession sa, snp.snp_consensussnp scs, mgd.voc_term vt where sa._logicaldb_key = 73 and sa._mgitype_key = 30 and scs._consensussnp_key > " + start + " and scs._consensussnp_key <= " + end + " and scs._consensussnp_key = sa._object_key and scs._varclass_key = vt._term_key");

		while(set.next()) {
			ConsensusSNP snp = new ConsensusSNP();
			
			snp.setConsensusKey(set.getInt("_consensussnp_key"));
			snp.setAccid(set.getString("accid"));
			snp.setVariationClass(set.getString("term"));
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
			set = sql.executeQuery("select * from snp.snp_flank where _consensussnp_key > " + start + " and _consensussnp_key <= " + end + " and is5prime = 1 order by sequencenum");
		} else {
			set = sql.executeQuery("select * from snp.snp_flank where _consensussnp_key > " + start + " and _consensussnp_key <= " + end + " and is5prime = 0 order by sequencenum");
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

		ResultSet set = sql.executeQuery("select sss._subsnp_key, sss._consensussnp_key, sa.accid, sss.allelesummary, sss.isexemplar, sss.orientation, vt1.term as varclass, sa2.accid as submitter from snp.snp_subsnp sss, snp.snp_accession sa, snp.snp_accession sa2, mgd.voc_term vt1 where sss._subsnp_key = sa._object_key and sss._consensussnp_key > " + start + " and sss._consensussnp_key <= " + end + " and sa._logicaldb_key = 74 and sa._mgitype_key = 31 and sss._varclass_key = vt1._term_key and sss._subsnp_key = sa2._object_key and sa2._logicaldb_key = 75 and sa2._mgitype_key = 31");

		HashMap<Integer, SubSNP> snps = new HashMap<Integer, SubSNP>();
		
		while(set.next()) {
			SubSNP snp = new SubSNP();
			snp.setSubSnpKey(set.getInt("_subsnp_key"));
			snp.setAccid(set.getString("accid"));
			snp.setAlleleSummary(set.getString("allelesummary"));
			snp.setExemplar(set.getInt("isexemplar") == 1);
			snp.setOrientation(set.getString("orientation"));
			snp.setSubmitterId(set.getString("submitter"));
			snp.setVariationClass(set.getString("varclass"));
			snp.setPopulations(new ArrayList<PopulationSNP>());
			snps.put(set.getInt("_subsnp_key"), snp);

			consensusSnps.get(set.getInt("_consensussnp_key")).getSubSNPs().add(snp);
		}
		
		populatePopulations(snps, start, end);
		
	}
	
	private void populateConsensusAlleles(HashMap<Integer, ConsensusSNP> consensusSnps, int start, int end) throws SQLException {
		ResultSet set = sql.executeQuery("select ssca._consensussnp_key, ssca._mgdstrain_key, ssca.isconflict, ssca.allele from snp.snp_consensussnp_strainallele ssca where ssca._consensussnp_key > " + start + " and ssca._consensussnp_key <= " + end + " order by ssca._consensussnp_key, ssca._mgdstrain_key");
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
		
		ResultSet set = sql.executeQuery("select scc._coord_cache_key, scc._consensussnp_key, scc.chromosome, scc.startcoordinate, scc.ismulticoord, scc.strand, vt.term, scc.allelesummary, scc.iupaccode from snp.snp_coord_cache scc, mgd.voc_term vt where scc._consensussnp_key > " + start + " and scc._consensussnp_key <= " + end + " and scc._varclass_key = vt._term_key order by scc._coord_cache_key");
		
		while(set.next()) {
			ConsensusCoordinateSNP c = new ConsensusCoordinateSNP();
			c.setAlleleSummary(set.getString("allelesummary"));
			c.setChromosome(set.getString("chromosome"));
			c.setIupaccode(set.getString("iupaccode"));
			c.setMultiCoord(set.getInt("ismulticoord") == 1);
			c.setStartCoordinate(set.getDouble("startcoordinate"));
			c.setStrand(set.getString("strand"));
			c.setVariationClass(set.getString("term"));
			
			c.setMarkers(new ArrayList<ConsensusMarkerSNP>());
			consensusSnps.get(set.getInt("_consensussnp_key")).getConsensusCoordinates().add(c);
			coords.put(set.getInt("_coord_cache_key"), c);
		}
		set.close();
		
		populateConsensusMarkers(coords, start, end);
		
	}
	
	private void populateConsensusMarkers(HashMap<Integer, ConsensusCoordinateSNP> coords, int start, int end) throws SQLException {
		
		ResultSet set = sql.executeQuery("select scm._coord_cache_key, scm._consensussnp_key, a.accid, m.symbol, m.name, vt.term, sa1.accid as transcript, sa2.accid as protein, scm.contig_allele, scm.residue, scm.aa_position, scm.reading_frame "
				+ "from mgd.voc_term vt, mgd.acc_accession a, mgd.mrk_marker m, snp.snp_consensussnp_marker scm "
				+ "left join snp.snp_accession sa1 on sa1._object_key = scm._consensussnp_marker_key and sa1._logicaldb_key = 27 and sa1._mgitype_key = 32 and sa1.prefixpart = 'NM_' "
				+ "left join snp.snp_accession sa2 on sa2._object_key = scm._consensussnp_marker_key and sa2._logicaldb_key = 27 and sa2._mgitype_key = 32 and sa2.prefixpart = 'NP_' "
				+ "where scm._consensussnp_key > " + start + " and scm._consensussnp_key <= " + end + " and scm._fxn_key = vt._term_key and a._object_key = scm._marker_key and "
						+ "a._logicaldb_key = 1 and a._mgitype_key = 2 and a.preferred = 1 and scm._marker_key = m._marker_key order by scm._consensussnp_key, scm._coord_cache_key");

		while(set.next()) {
			ConsensusMarkerSNP c = new ConsensusMarkerSNP();
			c.setAaPosition(set.getString("aa_position"));
			c.setAccid(set.getString("accid"));
			c.setContigAllele(set.getString("contig_allele"));
			c.setFunctionClass(set.getString("term"));
			c.setName(set.getString("name"));
			c.setProtein(set.getString("protein"));
			c.setReadingFrame(set.getString("reading_frame"));
			c.setResidue(set.getString("residue"));
			c.setSymbol(set.getString("symbol"));
			c.setTranscript(set.getString("transcript"));
			
			coords.get(set.getInt("_coord_cache_key")).getMarkers().add(c);
		}
		set.close();
	
	}

	private void populatePopulations(HashMap<Integer, SubSNP> snps, int start, int end) throws SQLException {
		
		ResultSet set = sql.executeQuery("select ssa._subsnp_key, ssa._population_key, ssa._mgdstrain_key, ssa.allele from snp.snp_subsnp sss, snp.snp_subsnp_strainallele ssa where sss._consensussnp_key > " + start + " and sss._consensussnp_key <= " + end + " and sss._subsnp_key = ssa._subsnp_key order by ssa._subsnp_key, ssa._population_key");
	
		int lastPopulation = 0;
		int lastSubSnp = 0;
		PopulationSNP p = null;
		
		while(set.next()) {
			if(lastPopulation != set.getInt("_population_key") || lastSubSnp != set.getInt("_subsnp_key")) {
				p = populations.get(set.getInt("_population_key")).dup();
				p.setAlleles(new ArrayList<AlleleSNP>());
				snps.get(set.getInt("_subsnp_key")).getPopulations().add(p);
				lastPopulation = set.getInt("_population_key");
				lastSubSnp = set.getInt("_subsnp_key");
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
	
	private void setupPopulations() throws SQLException {
		if(populations == null) {
			populations = new HashMap<Integer, PopulationSNP>();
			
			ResultSet set = sql.executeQuery("select sp._population_key, sa.accid, sp.subhandle, sp.name from snp.snp_population sp, snp.snp_accession sa where sp._population_key = sa._object_key and sa._logicaldb_key = 76 and sa._mgitype_key = 33");
			
			while(set.next()) {
				PopulationSNP p = new PopulationSNP();
				p.setAccid(set.getString("accid"));
				p.setPopulationName(set.getString("name"));
				p.setSubHandleName(set.getString("subhandle"));
				p.setAlleles(new ArrayList<AlleleSNP>());
				populations.put(set.getInt("_population_key"), p);
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
	
	

}
