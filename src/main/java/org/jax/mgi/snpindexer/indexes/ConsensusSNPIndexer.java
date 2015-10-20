package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jax.mgi.snpindexer.entities.Consensus;
import org.jax.mgi.snpindexer.util.ConsensusDAO;
import org.jax.mgi.snpindexer.visitors.PrintVisitor;

public class ConsensusSNPIndexer extends Indexer {

	private ConsensusDAO dao = new ConsensusDAO();
	
	public ConsensusSNPIndexer(String solrUrl, String coreName) {
		super(solrUrl, coreName);
	}

	@Override
	public void index() {
		resetIndex();
		try {
			
			
		
			ResultSet set = sql.executeQuery("select _accession_key from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 and accid = 'rs3163500'");
			
			int counter = 0;
			while (set.next()) {
				if(counter % 1000000 == 0) {
					System.out.println("Counter: " + counter);
				}
				counter++;
				
				Consensus snp = dao.getConsensus(set.getInt("_accession_key"));
				
				snp.getConsensusSNP().setSubSnpAccessions(dao.getSubSnpAccessions(snp.getConsensusSNP().getKey()));
				
				PrintVisitor p = new PrintVisitor();
				snp.Accept(p);
				p.generateOutput(System.out);
				

			}
			
			finish();

			set.close();
			
			sql.cleanup();

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	}

}
