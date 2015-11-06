package org.jax.mgi.snpindexer;

import java.sql.SQLException;
import java.util.HashMap;

import org.jax.mgi.snpdatamodel.ConsensusSNP;
import org.jax.mgi.snpdatamodel.visitors.PrintVisitor;
import org.jax.mgi.snpindexer.indexes.ConsensusSNPIndexer;
import org.jax.mgi.snpindexer.util.ConfigurationHelper;



public class MainTest {

	public static void main(String[] args) throws SQLException {
		ConfigurationHelper.init();
		
		ConsensusSNPIndexer i = new ConsensusSNPIndexer("ConsensusSNPIndex");
		
		HashMap<Integer, ConsensusSNP> consensusList = i.getConsensusSNP(2, 4);

		for(ConsensusSNP c: consensusList.values()) {
			if(c.getConsensusKey() == 3) {
				PrintVisitor pi = new PrintVisitor();
				c.Accept(pi);
				pi.generateOutput(System.out);
			}
		}
		
	}
}
