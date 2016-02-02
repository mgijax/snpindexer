package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.solr.common.SolrInputDocument;

public class AlleleSNPIndexer extends Indexer {


	private HashMap<Integer, String> strainMap = new HashMap<Integer, String>();
	
	// <snpid, allele, List<StrainIds>>
	private HashMap<Integer, HashMap<String, ArrayList<String>>> allelesStrainsMap = new HashMap<Integer, HashMap<String, ArrayList<String>>>();
	
	public AlleleSNPIndexer(IndexerConfig config) {
		super(config);
	}

	@Override
	public void index() {
		// Delete and Recreate the index
		resetIndex();

		try {

			log.info("Starting Load Strains Map");
			ResultSet set = sql.executeQuery("select _mgdstrain_key, strain from snp.snp_strain");
			while (set.next()) {
				strainMap.put(set.getInt("_mgdstrain_key"), set.getString("strain"));
			}
			set.close();
			log.info("Finished Load Strains Map");

			
			
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
				
				setupAllelesStrainsMap(start, end);
				
				set = sql.executeQuery("select "
						+ "sa.accid as consensussnp_accid, sa._object_key, scs.allele "
						+ "from "
						+ "snp.snp_accession sa, snp.snp_consensussnp_strainallele scs "
						+ "where "
						+ "sa._object_key = scs._consensussnp_key and sa._logicaldb_key = 73 and sa._mgitype_key = 30 and "
						+ "sa._object_key > " + start + " and sa._object_key <= " + end + " group by sa.accid, sa._object_key, scs.allele order by sa._object_key "
				);

				ArrayList<SolrInputDocument> docCache = new ArrayList<SolrInputDocument>();

				
				
				while (set.next()) {

					SolrInputDocument doc = new SolrInputDocument();
					
					doc.addField("consensussnp_accid", set.getString("consensussnp_accid"));
					doc.addField("allele", set.getString("allele"));
					doc.addField("strains", allelesStrainsMap.get(set.getInt("_object_key")).get(set.getString("allele")));

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

	private void setupAllelesStrainsMap(int start, int end) throws SQLException {
		allelesStrainsMap.clear();
		
		ResultSet set = sql.executeQuery("select scs._consensussnp_key, scs._mgdstrain_key, scs.allele from snp.snp_consensussnp_strainallele scs where scs._consensussnp_key > " + start + " and scs._consensussnp_key <= " + end + " ");
		
		while (set.next()) {
			HashMap<String, ArrayList<String>> list = allelesStrainsMap.get(set.getInt("_consensussnp_key"));
			if(list == null) {
				list = new HashMap<String, ArrayList<String>>();
				allelesStrainsMap.put(set.getInt("_consensussnp_key"), list);
			}
			
			ArrayList<String> strainIdList = list.get(set.getString("allele"));
			if(strainIdList == null) {
				strainIdList = new ArrayList<String>();
				list.put(set.getString("allele"), strainIdList);
			}
			strainIdList.add(strainMap.get(set.getInt("_mgdstrain_key")));
		}
		set.close();
	}

}
