# SNP Indexer

## Building the snpindexer

### Building the snpindexer.jar*

Make any configuration adjustments before running the Install see [Configuring the snpindexer](#configuring-the-snpindexer)

	host:/path/to/snpindexer => cp conf/config.properties.default conf/config.properties
	host:/path/to/snpindexer => vim conf/config.properties
	host:/path/to/snpindexer => cp Configuration.default Configuration
	host:/path/to/snpindexer => ./Install
	
Once the install has completed there will be a new file in the "dist" directory called snpindexer.jar that is a self contained jar that can be run anywhere with the config.properties file built in. See [Running the snpindexer](#running-the-snpindexer) for more info on running it.
	
\* Note if the config.properties file will be used then configuration must happen before building the snpindexer

### Dependencies - Developers note for updating

Dependencies are handled automatically via maven and the pom.xml file inside of eclipse. Dependencies can be upgraded by changing the version required in the pom.xml file, then running following commands to update the lib directory:

	host:/path/to/snpindexer => rm -fr lib/*.jar
	host:/path/to/snpindexer => mvn dependency:copy-dependencies -DoutputDirectory=lib
	host:/path/to/snpindexer => git add -u lib


## Configuring the snpindexer

### The snpindexer looks in four places for configuration
The order of precedence is the following:

  1. Java -D parameters:
    * -DPG_DBDRIVER=org.postgresql.Driver
    * -DPG_DBURL=jdbc:postgresql://localhost/export
    * -DPG_DBUSER=mgd\_public
    * -DPG_DBPASS=mgdpub
    * -DSOLR_BASEURL=http://localhost.jax.org:8983/solr
    * -DDEBUG=false
    * -DTHREADED=false
  2. The configuration file conf/config.properties:
    * driver=org.postgresql.Driver
    * databaseUrl=jdbc:postgresql://localhost/export
    * user=mgd_public
    * passowrd=mgdpub
    * solrBaseUrl=http://localhost.jax.org:8983/solr
    * debug=false
    * threaded=false
  3. The system enviroment:
    * export PG\_DBDRIVER=org.postgresql.Driver
    * export PG\_DBURL=jdbc:postgresql://localhost/export
    * export PG\_DBUSER=mgd\_public
    * export PG\_DBPASS=mgdpub
    * export SOLR_BASEURL=http://localhost.jax.org:8983/solr
    * export DEBUG=false
    * export THREADED=false
  4. Defaults if no configuration is provided it will use the following:
    * driver=org.postgresql.Driver
    * databaseUrl=jdbc:postgresql://localhost/export
    * user=mgd_public
    * passowrd=mgdpub
    * solrBaseUrl=http://localhost.jax.org:8983/solr
    * debug=false
    * threaded=false

### Parameters
#### PG_DBDriver
This is the driver that the java odbc drive will try to us to connect to the database listed in PG_DBURL
#### PG_DBURL
This is the connection URL that the odbc driver will try to use in order to connect to the database server.
#### PG_DBUSER
This is the user that the odbc driver will use to connect to the database.
#### PG_DBPASS
This is the password that the odbc driver will use to connect to the database.
#### SOLR_BASEURL
This is the URL for the solr install. The snpIndexer will try to connect to SOLR_BASEURL/${index} and make inserts and delete there. There is a feature for running the indexer that two urls can used seperated by a comma. An example:

    -DSOLR_BASEURL=http://localhost1.jax.org:8983/solr,http://localhost2.jax.org:8983/solr
    solrBaseUrl=http://localhost1.jax.org:8983/solr,http://localhost2.jax.org:8983/solr
    export SOLR_BASEURL=http://localhost1.jax.org:8983/solr,http://localhost2.jax.org:8983/solr
    
 If adding documents to one of the solr's fail, then the whole indexer will fail.

#### DEBUG
This is used to debug the application, set this to true and re run the application all output will be placed in a logging.log file.
#### THREADED
This is used to have all the indexers run at the same time, due to memory constraints and IO, only faster servers can accommodate this setting to true

## Running the snpindexer

### Running as a jar file

If one decides to the run the jar file by itself it can be running the following three ways:

  1. Running sequentially:

  		host:/path/to/snpindexer => cd bin
  		host:/path/to/snpindexer/bin => ./runSequentially
  		
  2. Running parallel:

  		host:/path/to/snpindexer => cd bin
  		host:/path/to/snpindexer/bin => ./runParallel

  3. Running with -D java parameters: 
	
		host:/path/to/snpindexer => cd dist
		host:/path/to/snpindexer/dist => java \
			-DPG_DBURL=jdbc:postgresql://mgi-testdb3.jax.org/export \
			-DPG_DBUSER=mgd_other \
			-DPG_DBPASS=pasword \
			-DSOLR_BASEURL=http://snpsolr.jax.org:8983/solr \
			-jar snpindexer.jar
			
  4. Running with ENV vars set:
		
		host:/path/to/snpindexer => export PG_DBURL=jdbc:postgresql://mgi-testdb3.jax.org/snp
		host:/path/to/snpindexer => export SOLR_BASEURL=http://solr123.jax.org:8983/solr
		host:/path/to/snpindexer => java -jar snpindexer.jar
	
  5. Running with the built in config.properties file or running with the defaults if config.properties was empty at build:

		host:/path/to/snpindexer => cd dist
		host:/path/to/snpindexer/dist => java -jar snpindexer.jar
		