package org.jax.mgi.snpindexer.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class SQLDAO {

	EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SNP-Indexer");

	EntityManager em = entityManagerFactory.createEntityManager();


	public SQLDAO() {

	}


}
