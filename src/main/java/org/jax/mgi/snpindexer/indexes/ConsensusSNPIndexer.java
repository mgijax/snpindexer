package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jax.mgi.snpindexer.entities.Consensus;
import org.jax.mgi.snpindexer.util.ConsensusSNPDAO;
import org.jax.mgi.snpindexer.visitors.PrintVisitor;

public class ConsensusSNPIndexer extends Indexer {

	private ConsensusSNPDAO dao = new ConsensusSNPDAO();
	
	public ConsensusSNPIndexer(String solrUrl, String coreName) {
		super(solrUrl, coreName);
	}

	@Override
	public void index() {
		resetIndex();
		try {
			
			
		
			ResultSet set = sql.executeQuery("select _consensussnp_key from snp.snp_consensussnp limit 10");
			
			int counter = 0;
			while (set.next()) {
				if(counter % 1000000 == 0) {
					System.out.println("Counter: " + counter);
				}
				counter++;
				
				Consensus snp = dao.getConsensusSNP(set.getInt("_consensussnp_key"));
				
				PrintVisitor p = new PrintVisitor();
				snp.Accept(p);
				p.generateOutput(System.out);
				

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
