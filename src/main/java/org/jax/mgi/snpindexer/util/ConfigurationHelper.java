package org.jax.mgi.snpindexer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationHelper {
	
	private static String driver = null;
	private static String databaseUrl = null;
	private static String user = null;
	private static String password = null;
	private static String solrBaseUrl = null;
	
	public ConfigurationHelper() {
		
		System.out.println("Loading System Properties via -D paramaters");
		driver = System.getProperty("PG_DBDRIVER");
		databaseUrl = System.getProperty("PG_DBURL");
		user = System.getProperty("PG_DBUSER");
		password = System.getProperty("PG_DBPASS");
		solrBaseUrl = System.getProperty("SOLR_BASEURL");
		
		System.out.println("Loading Properties via config.properties files");
		InputStream in = SQLExecutor.class.getClassLoader().getResourceAsStream("config.properties");
		if(in == null) {
			System.out.println("No config.properties assuming defaults");
		} else {
			try {
				Properties configurationProperties = new Properties();
				configurationProperties.load(in);
				if(driver == null) driver = configurationProperties.getProperty("driver");
				if(databaseUrl == null) databaseUrl = configurationProperties.getProperty("databaseUrl");
				if(user == null) user = configurationProperties.getProperty("user");
				if(password == null) password = configurationProperties.getProperty("password");
				if(solrBaseUrl == null) solrBaseUrl = configurationProperties.getProperty("solrBaseUrl");
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
				System.out.println("Error Loading config.properties file assuming defaults");
			}
		}
		
		System.out.println("Loading Properties via System ENV");
		if(driver == null) driver = System.getenv("PG_DBDRIVER");
		if(databaseUrl == null) databaseUrl = System.getenv("PG_DBURL");
		if(user == null) user = System.getenv("PG_DBUSER");
		if(password == null) password = System.getenv("PG_DBPASS");
		if(solrBaseUrl == null) solrBaseUrl = System.getenv("SOLR_BASEURL");
		
		System.out.println("Setting default values for properties that are null");
		if(driver == null) driver = "org.postgresql.Driver";
		if(databaseUrl == null) databaseUrl = "jdbc:postgresql://localhost/export";
		if(user == null) user = "mgd_public";
		if(password == null) password = "mgdpub";
		if(solrBaseUrl == null) solrBaseUrl = "http://localhost.jax.org:8983/solr";
		
		printProperties();
		
	}
	
	private void printProperties() {
		System.out.println("Effective Properties:");
		System.out.println("\tdriver: " + driver);
		System.out.println("\tdatabaseUrl: " + databaseUrl);
		System.out.println("\tuser: " + user);
		System.out.println("\tpassword: " + password);
		System.out.println("\tsolrBaseUrl: " + solrBaseUrl);
	}

	public static String getDriver() {
		return driver;
	}
	public static String getDatabaseUrl() {
		return databaseUrl;
	}
	public static String getUser() {
		return user;
	}
	public static String getPassword() {
		return password;
	}
	public static String getSolrBaseUrl() {
		return solrBaseUrl;
	}
}
