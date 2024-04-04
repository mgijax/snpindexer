package org.jax.mgi.snpindexer.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationHelper {
	
	private static String driver = null;
	private static String databaseUrl = null;
	private static String user = null;
	private static String password = null;
	private static List<List<String>> esUrls = null;
	private static boolean debug = false;
	private static boolean threaded = false;
	
	public static void init() {
		InputStream in = ConfigurationHelper.class.getClassLoader().getResourceAsStream("log4j.properties");
		//Properties props = new Properties();

		log.info("Loading System Properties via -D paramaters");
		
		// Reads this next set of params from the command line -D params
		driver = System.getProperty("PG_DBDRIVER");
		if(driver != null) { log.info("Found: -D PG_DBDRIVER=" + driver); }
		
		databaseUrl = System.getProperty("PG_DBURL");
		if(databaseUrl != null) { log.info("Found: -D PG_DBURL=" + databaseUrl); }
		
		user = System.getProperty("PG_DBUSER");
		if(user != null) { log.info("Found: -D PG_DBUSER=" + user); }

		password = System.getProperty("PG_DBPASS");
		if(password != null) { log.info("Found: -D PG_DBPASS=" + password); }
		
		esUrls = parseJSONArrayProperty(System.getProperty("ES_URLS"));
		if(esUrls != null) { log.info("Found: -D ES_URL=" + esUrls); }
		
		debug = "true".equals(System.getProperty("DEBUG"));
		if(debug) { log.info("Found: -D DEBUG=" + debug); }
		
		threaded = "true".equals(System.getProperty("THREADED"));
		if(threaded) { log.info("Found: -D THREADED=" + threaded); }
		
		log.info("Loading Properties via conf/config.properties file");

		in = ConfigurationHelper.class.getClassLoader().getResourceAsStream("config.properties");
		if(in != null) {
			try {
				Properties configurationProperties = new Properties();
				configurationProperties.load(in);

				// Reads these setup of params from the configuration file
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
				
				if(esUrls == null) {
					esUrls = parseJSONArrayProperty(configurationProperties.getProperty("esUrls"));
					if(esUrls != null) { log.info("Config File: esUrl=" + esUrls); }
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

		// Reads the next set of params from the ENV
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
		
		if(esUrls == null) {
			esUrls = parseJSONArrayProperty(System.getenv("ES_URLS"));
			if(esUrls != null) { log.info("Found Enviroment ENV[ES_URLS]=" + esUrls); }
		}
		if(!debug) {
			debug = "true".equals(System.getenv("DEBUG"));
			if(debug) { log.info("Found Enviroment ENV[DEBUG]=" + debug); }
		}
		if(!threaded) {
			threaded = "true".equals(System.getenv("THREADED"));
			if(threaded) { log.info("Found Enviroment ENV[THREADED]=" + threaded); }
		}
		
		// Defaults any params that were not set from above
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

		if(esUrls == null) {
			esUrls = new ArrayList<List<String>>();
			ArrayList<String> list = new ArrayList<>();
			list.add("localhost.jax.org:9200");
			esUrls.add(list);
			log.info("Setting default: esUrl=" + esUrls);
		}
		
		printProperties();
	}
	
	private static List<List<String>> parseJSONArrayProperty(String property) {
		if(property == null || property.length() == 0) { 
			return null;
		} else {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.readValue(property, new TypeReference<List<List<String>>>() {});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void printProperties() {
		log.info("Running Indexer with Properties:");
		log.info("\tdriver: " + driver);
		log.info("\tdatabaseUrl: " + databaseUrl);
		log.info("\tuser: " + user);
		log.info("\tpassword: " + password);
		log.info("\tesUrls: " + esUrls);
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
	public static List<List<String>> getEsUrls() {
		return esUrls;
	}
	public static boolean isDebug() {
		return debug;
	}
	public static boolean isThreaded() {
		return threaded;
	}
}
