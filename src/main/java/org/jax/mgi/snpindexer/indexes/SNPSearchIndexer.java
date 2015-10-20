package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.solr.common.SolrInputDocument;

public class SNPSearchIndexer extends Indexer {

	public SNPSearchIndexer(String solrUrl, String coreName) {
		super(solrUrl, coreName);
	}

	@Override
	public void index() {
		resetIndex();

		try {
		
			ResultSet set = sql.executeQuery("select "
					+ "sa.accid as consensussnp_accid, scc.chromosome, scc.startcoordinate, scc.ismulticoord, vt1.term as varclass, vt2.term as fxn, a.accid as marker_accid, ss.strain "
					+ "from "
					+ "snp.snp_accession sa, snp.snp_coord_cache scc, snp.snp_consensussnp_marker scm, snp.snp_consensussnp_strainallele scs, snp.snp_strain ss, mgd.voc_term vt1, mgd.voc_term vt2, mgd.acc_accession a "
					+ "where "
					+ "sa._object_key = scc._consensussnp_key and sa._logicaldb_key = 73 and sa._mgitype_key = 30 and scc._coord_cache_key = scm._coord_cache_key and scc._consensussnp_key = scs._consensussnp_key and "
					+ "scs._mgdstrain_key = ss._mgdstrain_key and scc._varclass_key = vt1._term_key and scm._fxn_key = vt2._term_key and scm._marker_key = a._object_key and a._mgitype_key = 2 and a._logicaldb_key = 1");
			
			int counter = 0;
			while (set.next()) {
	
				//HashMap<String, String> m = new HashMap<String, String>();
				//m.put(set.getString("allele"), set.getString("isconflict"));
				
				//bigInt.setBit(set.getInt("_consensussnp_key"));
				
				//map.put( set.getString("_consensussnp_key") + "-" + set.getInt("_mgdstrain_key"), m);
	
				
				if(counter % 1000000 == 0) {
					System.out.println("Counter: " + counter);
				}
				counter++;
				
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField("consensussnp_accid", set.getInt("consensussnp_accid"));
				doc.addField("chromosome", set.getString("chromosome"));
				doc.addField("startcoordinate", set.getDouble("startcoordinate"));
				doc.addField("ismulticoord", set.getInt("ismulticoord"));
				doc.addField("varclass", set.getInt("varclass"));
				doc.addField("fxn", set.getInt("fxn"));
				doc.addField("marker_accid", set.getInt("marker_accid"));
				doc.addField("strain", set.getString("strain"));
				addDocument(doc);
				
			}
			
			finish();

			set.close();
			
			sql.cleanup();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
