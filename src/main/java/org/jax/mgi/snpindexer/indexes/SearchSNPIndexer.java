package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.solr.common.SolrInputDocument;

public class SearchSNPIndexer extends Indexer {

	private HashMap<Integer, String> variationMap = new HashMap<Integer, String>();
	private HashMap<Integer, String> functionMap = new HashMap<Integer, String>();
	private HashMap<Integer, String> markerAccessionMap = new HashMap<Integer, String>();
	
	
	private HashMap<Integer, String> strainMap = new HashMap<Integer, String>();
	private HashMap<Integer, ArrayList<String>> strainsMap = new HashMap<Integer, ArrayList<String>>();

	
	public SearchSNPIndexer(IndexerConfig config) {
		super(config);
	}

	@Override
	public void index() {
		// Delete and Recreate the index
		resetIndex();

		try {

			log.info("Starting Load Function Type Map");
			ResultSet set = sql.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 49");
			
			StringBuilder excludeFunctionClasses = new StringBuilder();
			
			while (set.next()) {
				
				Integer key = set.getInt("_term_key");
				String fc = set.getString("term");
				
				if(fc.equals("within coordinates of") || fc.equals("within distance of")) {
					if(excludeFunctionClasses.length() > 0){
						excludeFunctionClasses.append(',');
				    }
					excludeFunctionClasses.append(key);
				}
				
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
			set = sql.executeQuery("select a.accid, m._marker_key from mgd.mrk_marker m, mgd.acc_accession a where m._marker_key = a._object_key and a._logicaldb_key = 1 and a._mgitype_key = 2 and a.preferred = 1 and m._organism_key = 1 and m._marker_status_key in (1, 3)");
			
			while (set.next()) {
				markerAccessionMap.put(set.getInt("_marker_key"), set.getString("accid"));
			}
			set.close();
			log.info("Finished Load Marker Accession Map");
			
			set = sql.executeQuery("select max(sa._object_key) as maxKey from snp.snp_accession sa where sa._logicaldb_key = 73 and sa._mgitype_key = 30");

			set.next();
			int max = set.getInt("maxKey");
			set.close();

			int chunkSize = config.getChunkSize();
			
			int chunks = max / chunkSize;
			
			startProcess(chunks, chunkSize, max);
			
			for(int i = 0; i <= chunks; i++) {
				int start = i * chunkSize;
				int end = (start + chunkSize);
				
				setupStrainsMap(start, end);
				
				set = sql.executeQuery("select "
						+ "sa.accid as consensussnp_accid, sa._object_key, scc.chromosome, scc.startcoordinate, scc._varclass_key, scm._marker_key, scm._fxn_key "
						+ "from "
						+ "snp.snp_accession sa, snp.snp_coord_cache scc "
						+ "left join snp.snp_consensussnp_marker scm on "
						+ "scc._consensussnp_key = scm._consensussnp_key and scm._fxn_key not in (" + excludeFunctionClasses + ") "
						+ "where "
						+ "sa._object_key = scc._consensussnp_key and sa._logicaldb_key = 73 and sa._mgitype_key = 30 and "
						+ "scc.ismulticoord = 0 and "
						+ "sa._object_key > " + start + " and sa._object_key <= " + end + " "
						+ "group by sa.accid, sa._object_key, scc.chromosome, scc.startcoordinate, scc._varclass_key, scm._marker_key, scm._fxn_key "
						+ "order by sa._object_key "
				);

				ArrayList<SolrInputDocument> docCache = new ArrayList<SolrInputDocument>();

				
				
				while (set.next()) {

					SolrInputDocument doc = new SolrInputDocument();
					
					doc.addField("consensussnp_accid", set.getString("consensussnp_accid"));
					
					doc.addField("chromosome", set.getString("chromosome"));
					doc.addField("startcoordinate", set.getDouble("startcoordinate"));
					doc.addField("varclass", variationMap.get(set.getInt("_varclass_key")));
					if(functionMap.containsKey(set.getInt("_fxn_key"))) {
						doc.addField("fxn", functionMap.get(set.getInt("_fxn_key")));
					}
					if(markerAccessionMap.containsKey(set.getInt("_marker_key"))) {
						doc.addField("marker_accid", markerAccessionMap.get(set.getInt("_marker_key")));
					}
					
					doc.addField("strains", strainsMap.get(set.getInt("_object_key")));
					
					//doc.addField("strain", strainMap.get(set.getInt("_mgdstrain_key")));

					docCache.add(doc);

				}
				set.close();
				
				addDocuments(docCache);
				progress(i, chunks, chunkSize);
			}
			
			finishProcess(max);
			
			sql.cleanup();

			log.info("Finished SNPSearchIndexer query");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		finish();
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
