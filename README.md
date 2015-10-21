# SNP Indexer

## Configuring the snpindexer

### The snpindexer looks in three places for configuration
The order of precedence is the following

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


 

## Building the snpindexer

### Building the snpindexer.jar*

Make any configuration adjustments before running the install see [Configuring the snpindexer](#Configuring-the-snpindexer)

	host:/path/to/snpindexer => cp conf/config.properties.default conf/config.properties
	host:/path/to/snpindexer => vim conf/config.properties
	host:/path/to/snpindexer => ./Install
	
\* Note if the config.properties file will be used then configuration must happen before building the snpindexer

### Dependencies

Dependencies are handled automatically via maven and the pom.xml file inside of eclipse. Dependencies can be upgraded by changing the version required in the pom.xml file, then running following commands to update the lib directory:

	host:/path/to/snpindexer => rm -fr lib/*.jar
	host:/path/to/snpindexer => mvn dependency:copy-dependencies -DoutputDirectory=lib
	host:/path/to/snpindexer => git add -u lib



