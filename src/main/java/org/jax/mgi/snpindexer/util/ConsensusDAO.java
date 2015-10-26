package org.jax.mgi.snpindexer.util;

import javax.persistence.TypedQuery;

import org.jax.mgi.snpindexer.entities.AccessionObject;
import org.jax.mgi.snpindexer.entities.ConsensusSNP;

public class ConsensusDAO extends SQLDAO {

	public ConsensusDAO() {
		super();
	}

	public ConsensusSNP getConsensusSNP(int id) {
		return em.find(ConsensusSNP.class, id);
	}

	public AccessionObject getAccessionObject(int key, int logical_db, int mgi_type) {
		TypedQuery<AccessionObject> query = em.createNamedQuery("accessionobject", AccessionObject.class);
		query.setParameter("key", key);
		query.setParameter("logicalDBKey", logical_db);
		query.setParameter("mgiTypeKey", mgi_type);
		return query.getSingleResult();
	}
}
