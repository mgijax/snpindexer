package org.jax.mgi.snpindexer.util;

import org.jax.mgi.snpindexer.entities.Consensus;

public class ConsensusSNPDAO extends SQLDAO {

	public Consensus getConsensusSNP(int id) {

		return em.find(Consensus.class, id);

	}
}
