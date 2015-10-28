package org.jax.mgi.snpindexer.util;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.jax.mgi.snpindexer.entities.MGDAccessionObject;
import org.jax.mgi.snpindexer.entities.SNPAccessionObject;
import org.jax.mgi.snpindexer.entities.ConsensusSNP;
import org.jax.mgi.snpindexer.entities.SubSNPStrainAllele;

public class ConsensusDAO extends SQLDAO {

	public ConsensusDAO() {
		super();
	}

	public ConsensusSNP getConsensusSNP(int id) {
		return em.find(ConsensusSNP.class, id);
	}
	
	public SNPAccessionObject getSNPAccessionObject(int key, int logical_db, int mgi_type) {
		return getSNPAccessionObject(key, logical_db, mgi_type, null);
	}

	public SNPAccessionObject getSNPAccessionObject(int key, int logical_db, int mgi_type, String prefix) {
		if(prefix != null) {
			TypedQuery<SNPAccessionObject> query = em.createNamedQuery("snpaccessionobjectprefix", SNPAccessionObject.class);
			query.setParameter("key", key);
			query.setParameter("logicalDBKey", logical_db);
			query.setParameter("mgiTypeKey", mgi_type);
			query.setParameter("prefixPart", prefix);
			try {
				return query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
			
		} else {
			TypedQuery<SNPAccessionObject> query = em.createNamedQuery("snpaccessionobject", SNPAccessionObject.class);
			query.setParameter("key", key);
			query.setParameter("logicalDBKey", logical_db);
			query.setParameter("mgiTypeKey", mgi_type);
			try {
				return query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		}
	}
	
	public MGDAccessionObject getMGDAccessionObject(int key, int logical_db, int mgi_type) {
		TypedQuery<MGDAccessionObject> query = em.createNamedQuery("mgdaccessionobject", MGDAccessionObject.class);
		query.setParameter("key", key);
		query.setParameter("logicalDBKey", logical_db);
		query.setParameter("mgiTypeKey", mgi_type);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<SubSNPStrainAllele> getSubSNPStrainAlleles(int key, int populationKey) {
		TypedQuery<SubSNPStrainAllele> query = em.createNamedQuery("subsnpalleles", SubSNPStrainAllele.class);
		query.setParameter("key", key);
		query.setParameter("populationKey", populationKey);
		return query.getResultList();
	}
}
