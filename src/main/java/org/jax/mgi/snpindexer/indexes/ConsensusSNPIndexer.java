package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;

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
			
			ResultSet set = sql.executeQuery("select _accession_key from snp.snp_accession where _logicaldb_key = 73 and _mgitype_key = 30 and accid = 'rs3163500'");
			
			int counter = 0;
			while (set.next()) {
				if(counter % 1000000 == 0) {
					log.info("Counter: " + counter);
				}
				counter++;
				
				ConsensusSNP snp = dao.getConsensusSNP(set.getInt("_accession_key"));

				DBVisitor db = new DBVisitor(dao);
				snp.Accept(db);
				
				PrintVisitor p = new PrintVisitor();
				snp.Accept(p);
				p.generateOutput(System.out);
				
			}
		
			set.close();
			sql.cleanup();

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		finish();
	}

}
