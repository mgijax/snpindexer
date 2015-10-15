package org.jax.mgi.snpindexer.util;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.jax.mgi.entities.DataProperty;

public class SQLDAO {

	private static final String PERSISTENCE_UNIT_NAME = "SNP-Indexer";
	private static EntityManagerFactory factory;

	public SQLDAO() {
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		EntityManager em = factory.createEntityManager();
		Query q = em.createQuery("select d from DataProperty");
		List<DataProperty> list = q.getResultList();
		for (DataProperty data : list) {
			System.out.println(data);
		}
		System.out.println("Size: " + list.size());

		// create new todo
		em.getTransaction().begin();
		DataProperty data = new DataProperty();
		data.setProperty("This is a test");
		em.persist(data);
		em.getTransaction().commit();

		em.close();
	}

}
