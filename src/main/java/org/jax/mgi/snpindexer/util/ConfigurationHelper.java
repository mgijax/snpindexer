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
	private static String logFilePath = null;
	private static String logFileName = null;
	private static boolean debug = false;
	private static boolean threaded = false;
	private static Logger log;
	
	public static void init() {
		InputStream in = ConfigurationHelper.class.getClassLoader().getResourceAsStream("log4j.properties");
		Properties props = new Properties();
		
		if(in == null) {
			System.out.println("No log4j.properties file. Output going to stdout this is most likely not what you want");
			BasicConfigurator.configure();
		} else {
			try {
				props.load(in);
				logFileName = props.getProperty("log4j.appender.file.File");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		log = Logger.getLogger(ConfigurationHelper.class);
		log.info("Loading System Properties via -D paramaters");
		
		driver = System.getProperty("PG_DBDRIVER");
		if(driver != null) { log.info("Found: -D PG_DBDRIVER=" + driver); }
		
		databaseUrl = System.getProperty("PG_DBURL");
		if(databaseUrl != null) { log.info("Found: -D PG_DBURL=" + databaseUrl); }
		
		user = System.getProperty("PG_DBUSER");
		if(user != null) { log.info("Found: -D PG_DBUSER=" + user); }
		
		password = System.getProperty("PG_DBPASS");
		if(password != null) { log.info("Found: -D PG_DBPASS=" + password); }
		
		solrBaseUrl = System.getProperty("SOLR_BASEURL");
		if(solrBaseUrl != null) { log.info("Found: -D SOLR_BASEURL=" + solrBaseUrl); }
		
		logFilePath = System.getProperty("LOG_DIR");
		if(logFilePath != null) { log.info("Found: -D LOG_DIR=" + logFilePath); }
		
		debug = "true".equals(System.getProperty("DEBUG"));
		if(debug) { log.info("Found: -D DEBUG=" + debug); }
		
		threaded = "true".equals(System.getProperty("THREADED"));
		if(threaded) { log.info("Found: -D THREADED=" + threaded); }
		
		log.info("Loading Properties via conf/config.properties file");
		in = SQLExecutor.class.getClassLoader().getResourceAsStream("config.properties");
		if(in != null) {
			try {
				Properties configurationProperties = new Properties();
				configurationProperties.load(in);
				if(driver == null) {
					driver = configurationProperties.getProperty("driver");
					if(driver != null) { log.info("Config File: driver=" + driver); }
				}
				if(databaseUrl == null) {
					databaseUrl = configurationProperties.getProperty("databaseUrl");
					if(databaseUrl != null) { log.info("Config File: databaseUrl=" + databaseUrl); }
				}
				if(user == null) {
					user = configurationProperties.getProperty("user");
					if(user != null) { log.info("Config File: user=" + user); }
				}
				if(password == null) {
					password = configurationProperties.getProperty("password");
					if(password != null) { log.info("Config File: password=" + password); }
				}
				if(solrBaseUrl == null) {
					solrBaseUrl = configurationProperties.getProperty("solrBaseUrl");
					if(solrBaseUrl != null) { log.info("Config File: solrBaseUrl=" + solrBaseUrl); }
				}
				if(logFilePath == null) {
					logFilePath = configurationProperties.getProperty("logFilePath");
					if(logFilePath != null) { log.info("Config File: logFilePath=" + logFilePath); }
				}
				if(!debug) {
					debug = "true".equals(configurationProperties.getProperty("debug"));
					if(debug) { log.info("Config File: debug=" + debug); }
				}
				if(!threaded) {
					threaded = "true".equals(configurationProperties.getProperty("threaded"));
					if(threaded) { log.info("Config File: threaded=" + threaded); }
				}
			} catch (IOException e) {
				log.info("Error: " + e.getMessage());
				log.info("Error Loading config.properties file assuming defaults");
			}
		}
		
		log.info("Loading Properties via System ENV");
		if(driver == null) {
			driver = System.getenv("PG_DBDRIVER");
			if(driver != null) { log.info("Found Enviroment ENV[PG_DBDRIVER]=" + driver); }
		}
		if(databaseUrl == null) {
			databaseUrl = System.getenv("PG_DBURL");
			if(databaseUrl != null) { log.info("Found Enviroment ENV[PG_DBURL]=" + databaseUrl); }
		}
		if(user == null) {
			user = System.getenv("PG_DBUSER");
			if(user != null) { log.info("Found Enviroment ENV[PG_DBUSER]=" + user); }
		}
		if(password == null) {
			password = System.getenv("PG_DBPASS");
			if(password != null) { log.info("Found Enviroment ENV[PG_DBPASS]=" + password); }
		}
		if(solrBaseUrl == null) {
			solrBaseUrl = System.getenv("SOLR_BASEURL");
			if(solrBaseUrl != null) { log.info("Found Enviroment ENV[SOLR_BASEURL]=" + solrBaseUrl); }
		}
		if(logFilePath == null) {
			logFilePath = System.getenv("LOG_DIR");
			if(logFilePath != null) { log.info("Found Enviroment ENV[LOG_DIR]=" + logFilePath); }
		}
		if(!debug) {
			debug = "true".equals(System.getenv("DEBUG"));
			if(debug) { log.info("Found Enviroment ENV[DEBUG]=" + debug); }
		}
		if(!threaded) {
			threaded = "true".equals(System.getenv("THREADED"));
			if(threaded) { log.info("Found Enviroment ENV[THREADED]=" + threaded); }
		}
		
		log.info("Loading Properties via default values");
		if(driver == null) {
			driver = "org.postgresql.Driver";
			log.info("Setting default: driver=" + driver);
		}
		if(databaseUrl == null) {
			databaseUrl = "jdbc:postgresql://localhost/export";
			log.info("Setting default: databaseUrl=" + databaseUrl);
		}
		if(user == null) {
			user = "mgd_public";
			log.info("Setting default: user=" + user);
		}
		if(password == null) {
			password = "mgdpub";
			log.info("Setting default: password=" + password);
		}
		if(solrBaseUrl == null) {
			solrBaseUrl = "http://localhost.jax.org:8983/solr";
			log.info("Setting default: solrBaseUrl=" + solrBaseUrl);
		}
		if(logFilePath == null) {
			logFilePath = "";
			log.info("Setting default: logFilePath=" + logFilePath);
		}
		
		if(logFilePath.length() > 0) {
			logFilePath = logFilePath + "/" + logFileName;
			props.setProperty("log4j.appender.file.File", logFilePath);
		} else {
			logFilePath = logFileName;
		}
		props.setProperty("log4j.appender.file.File", logFilePath);
		printProperties();
		PropertyConfigurator.configure(props);
	}
	
	public static void printProperties() {
		log.info("Running Indexer with Properties:");
		log.info("\tdriver: " + driver);
		log.info("\tdatabaseUrl: " + databaseUrl);
		log.info("\tuser: " + user);
		log.info("\tpassword: " + password);
		log.info("\tsolrBaseUrl: " + solrBaseUrl);
		log.info("\tlogFilePath: " + logFilePath);
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
	public static String getLogFilePath() {
		return logFilePath;
	}
	public static boolean isDebug() {
		return debug;
	}
	public static boolean isThreaded() {
		return threaded;
	}
}
