package org.jax.mgi.snpindexer.indexes;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

public class SNPSearchIndexer extends Indexer {

	public SNPSearchIndexer(String solrUrl, String coreName) {
		super(solrUrl, coreName);
	}

	@Override
	public void index() {
		resetIndex();

		try {
		
		

			ResultSet set = sql.executeQuery("select scc._consensussnp_key, scc.chromosome, scc.startcoordinate, scc.ismulticoord, scc._varclass_key, scm._fxn_key, scm._marker_key, ss.strain from snp.snp_coord_cache scc, snp.snp_consensussnp_marker scm, snp.snp_consensussnp_strainallele scs, snp.snp_strain ss where scc._coord_cache_key = scm._coord_cache_key and scc._consensussnp_key = scs._consensussnp_key and scs._mgdstrain_key = ss._mgdstrain_key");
			
			int counter = 0;
			while (set.next()) {
	
				//HashMap<String, String> m = new HashMap<String, String>();
				//m.put(set.getString("allele"), set.getString("isconflict"));
				
				//bigInt.setBit(set.getInt("_consensussnp_key"));
				
				//map.put( set.getString("_consensussnp_key") + "-" + set.getInt("_mgdstrain_key"), m);
	
				
				if(counter % 1000000 == 0) {
					System.out.println("Counter: " + counter);
					//System.gc();
					//client.commit();
				}
				//System.gc();
				counter++;
				
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField("_consensussnp_key", set.getInt("_consensussnp_key"));
				doc.addField("chromosome", set.getString("chromosome"));
				doc.addField("startcoordinate", set.getDouble("startcoordinate"));
				doc.addField("ismulticoord", set.getInt("ismulticoord"));
				doc.addField("_varclass_key", set.getInt("_varclass_key"));
				doc.addField("_fxn_key", set.getInt("_fxn_key"));
				doc.addField("_marker_key", set.getInt("_marker_key"));
				doc.addField("strain", set.getString("strain"));
				addDocument(doc);
				
			}
			
			client.commit();
			client.close();

			set.close();
			
			sql.cleanup();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
