package org.jax.mgi.snpindexer.util;

import java.util.List;

import javax.persistence.TypedQuery;

import org.jax.mgi.snpindexer.entities.Consensus;
import org.jax.mgi.snpindexer.entities.SubSnpAccession;

public class ConsensusDAO extends SQLDAO {

	public Consensus getConsensus(int id) {
		return em.find(Consensus.class, id);
	}

	public List<SubSnpAccession> getSubSnpAccessions(int key) {
		TypedQuery<SubSnpAccession> query = em.createNamedQuery("subsnpaccession", SubSnpAccession.class);
		query.setParameter("key", key);
		List<SubSnpAccession> results = query.getResultList();
		return results;

	}
}
