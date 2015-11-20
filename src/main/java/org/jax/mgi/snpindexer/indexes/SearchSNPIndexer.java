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
			while (set.next()) {
				functionMap.put(set.getInt("_term_key"), set.getString("term"));
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
			int end = set.getInt("maxKey");
			set.close();

			int chunkSize = config.getChunkSize();
			
			int chunks = end / chunkSize;
			
			startProcess(chunks, chunkSize, end);
			
			for(int i = 0; i <= chunks; i++) {
				int start = i * chunkSize;
				
				
				set = sql.executeQuery("select "
						+ "sa.accid as consensussnp_accid, scc.chromosome, scc.startcoordinate, scc.ismulticoord, scc._varclass_key, scm._fxn_key, scm._marker_key, scm.distance_from, scm.distance_direction, scs._mgdstrain_key "
						+ "from "
						+ "snp.snp_accession sa, snp.snp_coord_cache scc, snp.snp_consensussnp_marker scm, snp.snp_consensussnp_strainallele scs "
						+ "where "
						+ "sa._object_key = scc._consensussnp_key and sa._logicaldb_key = 73 and sa._mgitype_key = 30 and scc._coord_cache_key = scm._coord_cache_key and scc._consensussnp_key = scs._consensussnp_key and "
						+ "sa._object_key > " + start + " and sa._object_key <= " + (start + chunkSize));

				ArrayList<SolrInputDocument> docCache = new ArrayList<SolrInputDocument>();

				while (set.next()) {

					SolrInputDocument doc = new SolrInputDocument();
					
					doc.addField("consensussnp_accid", set.getString("consensussnp_accid"));
					
					doc.addField("chromosome", set.getString("chromosome"));
					doc.addField("startcoordinate", set.getDouble("startcoordinate"));
					doc.addField("ismulticoord", set.getInt("ismulticoord"));
					doc.addField("varclass", variationMap.get(set.getInt("_varclass_key")));
					doc.addField("fxn", functionMap.get(set.getInt("_fxn_key")));
					doc.addField("marker_accid", markerAccessionMap.get(set.getInt("_marker_key")));
					doc.addField("strain", strainMap.get(set.getInt("_mgdstrain_key")));
					doc.addField("distance_from", strainMap.get(set.getInt("distance_from")));
					doc.addField("distance_direction", strainMap.get(set.getString("distance_direction")));

					docCache.add(doc);
				}
				set.close();
				
				addDocuments(docCache);
				progress(i, chunks, chunkSize);
			}
			
			finishProcess(end);
			
			sql.cleanup();

			log.info("Finished SNPSearchIndexer query");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		finish();
	}

}
