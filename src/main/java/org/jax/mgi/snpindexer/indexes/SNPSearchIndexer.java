package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.solr.common.SolrInputDocument;

public class SNPSearchIndexer extends Indexer {

	public SNPSearchIndexer(String coreName) {
		super(coreName);
	}

	@Override
	public void index() {
		// Delete and Recreate the index
		resetIndex();

		try {

			HashMap<Integer, String> functionMap = new HashMap<Integer, String>();
			System.out.println("Starting Load Function Type Map");
			ResultSet set = sql.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 49");
			while (set.next()) {
				functionMap.put(set.getInt("_term_key"), set.getString("term"));
			}
			set.close();
			System.out.println("Finished Load Function Type Map");


			HashMap<Integer, String> variationMap = new HashMap<Integer, String>();
			System.out.println("Starting Load Variation Type Map");
			set = sql.executeQuery("select _term_key, term from mgd.voc_term where _vocab_key = 50");
			while (set.next()) {
				variationMap.put(set.getInt("_term_key"), set.getString("term"));
			}
			set.close();
			System.out.println("Finished Load Variation Type Map");


			HashMap<Integer, String> strainMap = new HashMap<Integer, String>();
			System.out.println("Starting Load Strains Map");
			set = sql.executeQuery("select _mgdstrain_key, strain from snp.snp_strain");
			while (set.next()) {
				strainMap.put(set.getInt("_mgdstrain_key"), set.getString("strain"));
			}
			set.close();
			System.out.println("Finished Load Strains Map");


			HashMap<Integer, String> markerAccessionMap = new HashMap<Integer, String>();
			System.out.println("Starting Load Marker Accession Map");
			set = sql.executeQuery("select _object_key, accid from mgd.acc_accession a where a._mgitype_key = 2 and a._logicaldb_key = 1");
			while (set.next()) {
				markerAccessionMap.put(set.getInt("_object_key"), set.getString("accid"));
			}
			set.close();
			System.out.println("Finished Load Marker Accession Map");


//			HashMap<Integer, String> consensussnpAccessionMap = new HashMap<Integer, String>();
//			System.out.println("Starting Load Consensus SNP Accession Map");
//			set = sql.executeQuery("select _object_key, accid from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30");
//			while (set.next()) {
//				consensussnpAccessionMap.put(set.getInt("_object_key"), set.getString("accid"));
//			}
//			set.close();
//			System.out.println("Finished Load Consensus SNP Accession Map");





			System.out.println("Starting SNPSearchIndexer query");

			set = sql.executeQuery("select "
					+ "a.accid, scc.chromosome, scc.startcoordinate, scc.ismulticoord, scc._varclass_key, scm._fxn_key, scm._marker_key, scs._mgdstrain_key "
					+ "from snp.snp_coord_cache scc, snp.snp_consensussnp_marker scm, snp.snp_consensussnp_strainallele scs, snp.snp_accession a "
					+ "where a._object_key = scc._consensussnp_key and a._logicaldb_key = 73 and a._mgitype_key = 30 and scc._consensussnp_key = scs._consensussnp_key and scc._coord_cache_key = scm._coord_cache_key");

			ArrayList<SolrInputDocument> docCache = new ArrayList<SolrInputDocument>();
			int counter = 0;
			while (set.next()) {

				if(counter % 1000000 == 0) {
					System.out.println("Counter: " + counter);
					if(counter % 5000000 == 0) {
						commit();
					}
				}
				counter++;

				SolrInputDocument doc = new SolrInputDocument();
				doc.addField("consensussnp_accid", set.getString("accid"));
				doc.addField("chromosome", set.getString("chromosome"));
				doc.addField("startcoordinate", set.getDouble("startcoordinate"));
				doc.addField("ismulticoord", set.getInt("ismulticoord"));
				doc.addField("varclass", variationMap.get(set.getInt("_varclass_key")));
				doc.addField("fxn", functionMap.get(set.getInt("_fxn_key")));
				doc.addField("marker_accid", markerAccessionMap.get(set.getInt("_marker_key")));
				doc.addField("strain", strainMap.get(set.getInt("_mgdstrain_key")));
			
				docCache.add(doc);
				if (docCache.size() >= 100000)  {
					addDocuments(docCache);
					docCache.clear();
				}

			}
			if(!docCache.isEmpty()) addDocuments(docCache);
			
			

			set.close();
			sql.cleanup();
			finish();
			
			System.out.println("Finished SNPSearchIndexer query");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
