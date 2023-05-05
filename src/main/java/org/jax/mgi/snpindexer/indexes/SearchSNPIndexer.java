package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jax.mgi.snpdatamodel.document.SearchSNPDocument;

public class SearchSNPIndexer extends Indexer {

	private HashMap<Integer, String> variationMap = new HashMap<Integer, String>();
	
	private HashMap<Integer, String> functionMap = new HashMap<Integer, String>();
	private HashMap<Integer, String> markerMap = new HashMap<Integer, String>();
	private HashMap<Integer, String> strainMap = new HashMap<Integer, String>();
	
	private HashMap<Integer, ArrayList<String>> strainsMap = new HashMap<Integer, ArrayList<String>>();
	private HashMap<Integer, ArrayList<String>> functionClassesMap = new HashMap<Integer, ArrayList<String>>();
	private HashMap<Integer, ArrayList<String>> markersMap = new HashMap<Integer, ArrayList<String>>();

	public SearchSNPIndexer(IndexerConfig config) {
		super(config);
	}

	@Override
	public void index() {

		try {

			log.info("Starting Load Function Type Map");
			ResultSet set = sql.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 49");
			
			while (set.next()) {
				
				Integer key = set.getInt("_term_key");
				String fc = set.getString("term");
				
				functionMap.put(key, fc);
			}
			set.close();
			log.info("Finished Load Function Type Map");

			log.info("Starting Load Variation Type Map");
			set = sql.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 50");
			while (set.next()) {
				variationMap.put(set.getInt("_term_key"), set.getString("term"));
			}
			set.close();
			log.info("Finished Load Variation Type Map");

			log.info("Starting Load Strains Map");
			set = sql.executeQuery("select _mgdstrain_key, strain from snp.snp_strain");
			while (set.next()) {
				strainMap.put(set.getInt("_mgdstrain_key"), set.getString("strain"));
			}
			set.close();
			log.info("Finished Load Strains Map");

			log.info("Starting Load Marker Accession Map");
			set = sql.executeQuery("select a.accid, m._marker_key from mgd.mrk_marker m, mgd.acc_accession a where m._marker_key = a._object_key and a._logicaldb_key = 1 and a._mgitype_key = 2 and a.preferred = 1 and m._organism_key = 1 and m._marker_status_key = 1");
			
			while (set.next()) {
				markerMap.put(set.getInt("_marker_key"), set.getString("accid"));
			}
			set.close();
			log.info("Finished Load Marker Accession Map");
			
			set = sql.executeQuery("select max(sa._object_key) as maxKey from snp.snp_accession sa where sa._logicaldb_key = 73 and sa._mgitype_key = 30");

			set.next();
			int max = set.getInt("maxKey");
			set.close();

			int chunkSize = config.getChunkSize();
			
			int chunks = max / chunkSize;

			for(int i = 0; i <= chunks; i++) {
				int start = i * chunkSize;
				int end = (start + chunkSize);
				
				setupStrainsMap(start, end);
				setupFunctionClassMap(start, end);
				setupMarkersMap(start, end);
				
				set = sql.executeQuery("select "
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

				ArrayList<SearchSNPDocument> docCache = new ArrayList<SearchSNPDocument>();
				
				while (set.next()) {

					SearchSNPDocument doc = new SearchSNPDocument();
					
					doc.setConsensussnp_accid(set.getString("consensussnp_accid"));
					doc.setChromosome(set.getString("chromosome"));
					doc.setStartcoordinate(set.getDouble("startcoordinate"));
					doc.setVarclass(variationMap.get(set.getInt("_varclass_key")));

					if(functionClassesMap.containsKey(set.getInt("_object_key"))) {
						doc.setFxn(functionClassesMap.get(set.getInt("_object_key")));
					}
					if(markersMap.containsKey(set.getInt("_object_key"))) {
						doc.setMarker_accid(markersMap.get(set.getInt("_object_key")));
					}
					doc.setStrains(strainsMap.get(set.getInt("_object_key")));
					
					docCache.add(doc);

				}
				set.close();
				
				indexDocuments(docCache);

			}
			
			sql.cleanup();

			log.info("Finished SNPSearchIndexer query");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void setupMarkersMap(int start, int end) throws SQLException {
		markersMap.clear();
		
		ResultSet set = sql.executeQuery("select scm._consensussnp_key, scm._marker_key from snp.snp_consensussnp_marker scm where scm._consensussnp_key > " + start + " and scm._consensussnp_key <= " + end + " group by scm._consensussnp_key, scm._marker_key");
		
		while(set.next()) {
			ArrayList<String> list = markersMap.get(set.getInt("_consensussnp_key"));
			if(list == null) {
				list = new ArrayList<String>();
				markersMap.put(set.getInt("_consensussnp_key"), list);
			}
			list.add(markerMap.get(set.getInt("_marker_key")));
		}
		set.close();
	}
	
	private void setupFunctionClassMap(int start, int end) throws SQLException {
		functionClassesMap.clear();
		
		ResultSet set = sql.executeQuery("select scm._consensussnp_key, scm._fxn_key from snp.snp_consensussnp_marker scm where scm._consensussnp_key > " + start + " and scm._consensussnp_key <= " + end + " group by scm._consensussnp_key, scm._fxn_key");
		
		while(set.next()) {
			ArrayList<String> list = functionClassesMap.get(set.getInt("_consensussnp_key"));
			if(list == null) {
				list = new ArrayList<String>();
				functionClassesMap.put(set.getInt("_consensussnp_key"), list);
			}
			list.add(functionMap.get(set.getInt("_fxn_key")));
		}
		set.close();
	}

	private void setupStrainsMap(int start, int end) throws SQLException {
		strainsMap.clear();
		
		ResultSet set = sql.executeQuery("select scs._consensussnp_key, scs._mgdstrain_key from snp.snp_consensussnp_strainallele scs where scs._consensussnp_key > " + start + " and scs._consensussnp_key <= " + end + " ");
		
		while (set.next()) {
			ArrayList<String> list = strainsMap.get(set.getInt("_consensussnp_key"));
			if(list == null) {
				list = new ArrayList<String>();
				strainsMap.put(set.getInt("_consensussnp_key"), list);
			}
			list.add(strainMap.get(set.getInt("_mgdstrain_key")));
		}
		set.close();
	}

}
