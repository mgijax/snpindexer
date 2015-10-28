package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.solr.common.SolrInputDocument;
import org.jax.mgi.snpindexer.entities.ConsensusSNP;
import org.jax.mgi.snpindexer.util.ConsensusDAO;
import org.jax.mgi.snpindexer.visitors.DBVisitor;
import org.jax.mgi.snpindexer.visitors.PrintVisitor;

public class ConsensusSNPIndexer extends Indexer {

	private ConsensusDAO dao = new ConsensusDAO();
	
	public ConsensusSNPIndexer(String coreName) {
		super(coreName);
	}

	@Override
	public void index() {
		
		resetIndex();
		
		try {
			
			//ResultSet set = sql.executeQuery("select _accession_key, _object_key, accid from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 and accid = 'rs3163500'");
			//ResultSet set = sql.executeQuery("select _accession_key, _object_key, accid from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 and accid = 'rs3657285'");
			//ResultSet set = sql.executeQuery("select _accession_key, _object_key, accid from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 and accid = 'rs36238069'");
			//ResultSet set = sql.executeQuery("select _accession_key, _object_key, accid from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 and accid = 'rs49786457'");
			//ResultSet set = sql.executeQuery("select _accession_key, _object_key, accid from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 and accid = 'rs27287906'");
			
			// Multiple populations
			//ResultSet set = sql.executeQuery("select _accession_key, _object_key, accid from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 and accid = 'rs4228732'");
			
			// Multiple transcripts and proteins
			ResultSet set = sql.executeQuery("select _accession_key, _object_key, accid from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 and accid = 'rs29392909'");
			//ResultSet set = sql.executeQuery("select _accession_key, _object_key, accid from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 and accid = 'rs26922505'");
			
			

			//ResultSet set = sql.executeQuery("select _accession_key, _object_key, accid from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 and accid = 'rs27287906'");
			
			
			// First 100 result
			//ResultSet set = sql.executeQuery("select _accession_key, _object_key, accid from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 limit 100");
			
			int counter = 0;
			
			ArrayList<SolrInputDocument> docCache = new ArrayList<SolrInputDocument>();
			
			while (set.next()) {
				if(counter % 1000000 == 0) {
					log.info("Counter: " + counter);
				}
				counter++;
			
				ConsensusSNP snp = dao.getConsensusSNP(set.getInt("_object_key"));

				DBVisitor db = new DBVisitor(dao);
				snp.Accept(db);
				
				
//				PrintVisitor p = new PrintVisitor();
//				snp.Accept(p);
//				p.generateOutput(System.out);
				
				org.jax.mgi.snpdatamodel.visitors.PrintVisitor pi = new org.jax.mgi.snpdatamodel.visitors.PrintVisitor();
				db.getSolrConsensusSNP().Accept(pi);
				pi.generateOutput(System.out);
				
				//SolrInputDocument doc = new SolrInputDocument();
				
				//doc.addField("consensussnp_accid", set.getString("accid"));
				//doc.addField("objectJSONData", snp);
			
			
//				docCache.add(doc);
//				if (docCache.size() >= 10000)  {
//					addDocuments(docCache);
//					docCache.clear();
//				}
			}
			
//			if(!docCache.isEmpty()) {
//				addDocuments(docCache);
//				docCache.clear();
//			}
		
			set.close();
			sql.cleanup();

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		finish();
	}

}
