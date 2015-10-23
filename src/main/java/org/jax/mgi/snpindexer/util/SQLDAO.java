package org.jax.mgi.snpindexer.util;

import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class SQLDAO {

	protected EntityManagerFactory entityManagerFactory; // = Persistence.createEntityManagerFactory("SNP-Indexer");
	protected EntityManager em;
	protected Logger log = Logger.getLogger(getClass());

	public SQLDAO() {

		HashMap<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put("javax.persistence.jdbc.driver", ConfigurationHelper.getDriver());
		configOverrides.put("javax.persistence.jdbc.url", ConfigurationHelper.getDatabaseUrl());
		configOverrides.put("javax.persistence.jdbc.user", ConfigurationHelper.getUser());
		configOverrides.put("javax.persistence.jdbc.password", ConfigurationHelper.getPassword());
		try {
			entityManagerFactory = Persistence.createEntityManagerFactory("SNP-Indexer", configOverrides);
			em = entityManagerFactory.createEntityManager();
		} catch (Exception e) {
			log.info("DB Connection Error: " + ExceptionUtils.getRootCause(e.getCause()));
			ConfigurationHelper.printProperties();
			System.exit(1);
		}
	}


}
