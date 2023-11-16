package org.jax.mgi.snpindexer;

import org.jax.mgi.snpindexer.config.ConfigurationHelper;
import org.jax.mgi.snpindexer.config.IndexerConfig;
import org.jax.mgi.snpindexer.indexes.ConsensusSNPIndexer;



public class MainTest {

	public static void main(String[] args) throws Exception {
		ConfigurationHelper.init();
		
		ConsensusSNPIndexer i = new ConsensusSNPIndexer(IndexerConfig.ConsensusSNPIndexer);
		
//		HashMap<Integer, ConsensusSNP> consensusList = i.getConsensusSNP(2, 4);
//
//		for(ConsensusSNP c: consensusList.values()) {
//			if(c.getConsensusKey() == 3) {
//
//				ObjectMapper mapper = new ObjectMapper();
//				mapper.setSerializationInclusion(Include.NON_NULL);
//				String json = mapper.writeValueAsString(c);
//				System.out.println("Json: " + json);
//			}
//		}
		
//		SQLExecutor sql = new SQLExecutor(50000, false);
//		HashMap<Integer, String> hash = null;
//		
//		if(hash == null) {
//			hash = new HashMap<Integer, String>();
//			
//			// Cache the strings so submitters doesn't blow out memory
//			HashMap<String, String> temp = new HashMap<String, String>();
//			
//			ResultSet set = sql.executeQuery("select _object_key, accid from snp.snp_accession where _logicaldb_key = 74 and _mgitype_key = 31");
//			while(set.next()) {
//				if(!temp.containsKey(set.getString("accid"))) {
//					temp.put(set.getString("accid"), set.getString("accid"));
//				}
//				System.out.println(temp.size());
//				hash.put(set.getInt("_object_key"), temp.get(set.getString("accid")));
//			}
//			set.close();
//		}
		
	}
}