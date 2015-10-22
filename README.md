# SNP Indexer


## Building the snpindexer

### Building the snpindexer.jar*

Make any configuration adjustments before running the Install see [Configuring the snpindexer](#configuring-the-snpindexer)

	host:/path/to/snpindexer => cp conf/config.properties.default conf/config.properties
	host:/path/to/snpindexer => vim conf/config.properties
	host:/path/to/snpindexer => ./Install
	
Once the install has completed there will be a new file in the "dist" directory called snpindexer.jar that is a self contained jar that can be run anywhere with the config.properties file built in. See [Running the snpindexer](#running-the-snpindexer) for more info on running it.
	
\* Note if the config.properties file will be used then configuration must happen before building the snpindexer

### Dependencies

Dependencies are handled automatically via maven and the pom.xml file inside of eclipse. Dependencies can be upgraded by changing the version required in the pom.xml file, then running following commands to update the lib directory:

	host:/path/to/snpindexer => rm -fr lib/*.jar
	host:/path/to/snpindexer => mvn dependency:copy-dependencies -DoutputDirectory=lib
	host:/path/to/snpindexer => git add -u lib



## Configuring the snpindexer

### The snpindexer looks in four places for configuration
The order of precedence is the following:

  1. Java -D parameters
    * -DPG_DBDRIVER=org.postgresql.Driver
    * -DPG_DBURL=jdbc:postgresql://localhost/export
    * -DPG_DBUSER=mgd\_public
    * -DPG_DBPASS=mgdpub
    * -DSOLR_BASEURL=http://localhost.jax.org:8983/solr
  2. The configuration file conf/config.properties*
    * driver=org.postgresql.Driver
    * databaseUrl=jdbc:postgresql://localhost/export
    * user=mgd_public
    * passowrd=mgdpub
    * solrBaseUrl=http://localhost.jax.org:8983/solr
  3. The system enviroment
    * export PG\_DBDRIVER=org.postgresql.Driver
    * export PG\_DBURL=jdbc:postgresql://localhost/export
    * export PG\_DBUSER=mgd\_public
    * export PG\_DBPASS=mgdpub
    * export SOLR_BASEURL=http://localhost.jax.org:8983/solr
  4. Defaults if no configuration is provided it will use the following defaults
    * driver=org.postgresql.Driver
    * databaseUrl=jdbc:postgresql://localhost/export
    * user=mgd_public
    * passowrd=mgdpub
    * solrBaseUrl=http://localhost.jax.org:8983/solr


## Running the snpindexer

### Running as a jar file

If one decides to the run the jar file by itself it can be running the following three ways:

  1. Using the built in config.properties file or defaults if the file was empty:

		host:/path/to/snpindexer => cd dist
		host:/path/to/snpindexer => java -jar snpindexer.jar 

  2. Running with -D java parameters: 
	
		host:/path/to/snpindexer => cd dist
		host:/path/to/snpindexer => java \
			-DPG_DBURL=jdbc:postgresql://mgi-testdb3.jax.org/export \
			-DPG_DBUSER=mgd_other \
			-DPG_DBPASS=pasword \
			-DSOLR_BASEURL=http://snpsolr.jax.org:8983/solr \
			-jar snpindexer.jar
			
  3. Running with ENV vars set:
		
		host:/path/to/snpindexer => export PG_DBURL=jdbc:postgresql://mgi-testdb4.jax.org/snp
		host:/path/to/snpindexer => export SOLR_BASEURL=http://solr123.jax.org:8983/solr
		host:/path/to/snpindexer => java -jar snpindexer.jar
		