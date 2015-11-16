package org.jax.mgi.snpindexer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ConfigurationHelper {
	
	private static String driver = null;
	private static String databaseUrl = null;
	private static String user = null;
	private static String password = null;
	private static String solrBaseUrl = null;
	private static boolean debug = false;
	private static boolean threaded = false;
	private static Logger log;
	
	public static void init() {
		InputStream in = ConfigurationHelper.class.getClassLoader().getResourceAsStream("log4j.properties");
		if(in == null) {
			System.out.println("No log4j.properties file. Output going to stdout this is most likely not what you want");
			BasicConfigurator.configure();
		} else {
			PropertyConfigurator.configure(in);
		}
		
		log = Logger.getLogger(ConfigurationHelper.class);
		log.info("Loading System Properties via -D paramaters");
		driver = System.getProperty("PG_DBDRIVER");
		databaseUrl = System.getProperty("PG_DBURL");
		user = System.getProperty("PG_DBUSER");
		password = System.getProperty("PG_DBPASS");
		solrBaseUrl = System.getProperty("SOLR_BASEURL");
		debug = "true".equals(System.getProperty("DEBUG"));
		threaded = "true".equals(System.getProperty("THREADED"));
		printProperties();
		
		log.info("Loading Properties via config.properties files");
		in = SQLExecutor.class.getClassLoader().getResourceAsStream("config.properties");
		if(in == null) {
			log.info("No config.properties assuming defaults");
		} else {
			try {
				Properties configurationProperties = new Properties();
				configurationProperties.load(in);
				if(driver == null) driver = configurationProperties.getProperty("driver");
				if(databaseUrl == null) databaseUrl = configurationProperties.getProperty("databaseUrl");
				if(user == null) user = configurationProperties.getProperty("user");
				if(password == null) password = configurationProperties.getProperty("password");
				if(solrBaseUrl == null) solrBaseUrl = configurationProperties.getProperty("solrBaseUrl");
				if(!debug) debug = "true".equals(configurationProperties.getProperty("debug"));
				if(!threaded) threaded = "true".equals(configurationProperties.getProperty("threaded"));
			} catch (IOException e) {
				log.info("Error: " + e.getMessage());
				log.info("Error Loading config.properties file assuming defaults");
			}
		}
		printProperties();
		
		log.info("Loading Properties via System ENV");
		if(driver == null) driver = System.getenv("PG_DBDRIVER");
		if(databaseUrl == null) databaseUrl = System.getenv("PG_DBURL");
		if(user == null) user = System.getenv("PG_DBUSER");
		if(password == null) password = System.getenv("PG_DBPASS");
		if(solrBaseUrl == null) solrBaseUrl = System.getenv("SOLR_BASEURL");
		if(!debug) debug = "true".equals(System.getenv("DEBUG"));
		if(!threaded) threaded = "true".equals(System.getenv("THREADED"));
		printProperties();
		
		log.info("Setting default values for properties that are null");
		if(driver == null) driver = "org.postgresql.Driver";
		if(databaseUrl == null) databaseUrl = "jdbc:postgresql://localhost/export";
		if(user == null) user = "mgd_public";
		if(password == null) password = "mgdpub";
		if(solrBaseUrl == null) solrBaseUrl = "http://localhost.jax.org:8983/solr";
		printProperties();
		
	}
	
	public static void printProperties() {
		log.info("Effective Properties:");
		log.info("\tdriver: " + driver);
		log.info("\tdatabaseUrl: " + databaseUrl);
		log.info("\tuser: " + user);
		log.info("\tpassword: " + password);
		log.info("\tsolrBaseUrl: " + solrBaseUrl);
		log.info("\tdebug: " + debug);
		log.info("\tthreaded: " + threaded);
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
	public static boolean isDebug() {
		return debug;
	}
	public static boolean isThreaded() {
		return threaded;
	}
}
