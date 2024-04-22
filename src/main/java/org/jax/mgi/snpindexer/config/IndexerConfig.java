package org.jax.mgi.snpindexer.config;

import org.jax.mgi.snpindexer.indexes.AlleleSNPIndexer;
import org.jax.mgi.snpindexer.indexes.ConsensusSNPIndexer;
import org.jax.mgi.snpindexer.indexes.SearchSNPIndexer;
import org.jax.mgi.snpindexer.util.es.AlleleSNPIndexMappings;
import org.jax.mgi.snpindexer.util.es.ConsensusSNPIndexMappings;
import org.jax.mgi.snpindexer.util.es.Mapping;
import org.jax.mgi.snpindexer.util.es.SearchSNPIndexMappings;
import org.jax.mgi.snpindexer.util.es.Setting;
import org.jax.mgi.snpindexer.util.es.SiteIndexSettings;

public enum IndexerConfig {
	
	// (SolrCoreName, IndexerClass, ChunkSize, CommitTimeout, DB Fetch Size)
	
	ConsensusSNPIndexer("ConsensusSNPIndex", "consensus_snp_index", ConsensusSNPIndexer.class, SiteIndexSettings.class, ConsensusSNPIndexMappings.class, 10_000,  5_000, 4, 4, 10_000, 10, 4),
	SearchSNPIndexer(   "SearchSNPIndex",    "search_snp_index",    SearchSNPIndexer.class,    SiteIndexSettings.class, SearchSNPIndexMappings.class, 10_000,  5_000, 8, 4, 10_000, 10, 4),
	AlleleSNPIndexer(   "AlleleSNPIndex",    "allele_snp_index",    AlleleSNPIndexer.class,    SiteIndexSettings.class, AlleleSNPIndexMappings.class, 10_000,  5_000, 8, 4, 10_000, 10, 4)
	;
	
	// Name of the indexer for program args
	private String indexerName;
	// Name of index in ES
	private String indexName;
	
	// Batch size for cursor to the database
	private int cursorSize;
	// Batch size to process from the database
	private int chunkSize;
	// Number of workers connecting to the database
	private int dbWorkerCount;
	// Number of workers converting objects to json
	private int jsonWorkerCount;
	// Java Class of the indexer
	private Class<?> clazz;
	
	// Java Class of the settings for this indexer
	private Class<?> settingClazz;
	// Java Class of the mappings for this indexer
	private Class<?> mappingClazz;

	
	// Number of documents to index in one batch
	private int bulkActions;
	// Max size in MB for the batch target = 10mb
	private long bulkSize;
	// Number of concurrent batch requests to the server at once
	private int concurrentRequests; // Too many will cause the server to 429

	IndexerConfig(String indexerName, String indexName, Class<?> clazz, Class<?> settingClazz, Class<?> mappingClazz, int cursorSize, int chunkSize, int dbWorkerCount, int jsonWorkerCount, int bulkActions, long bulkSize, int concurrentRequests) {
		this.indexerName = indexerName;
		this.indexName = indexName;
		this.clazz = clazz;
		this.settingClazz = settingClazz;
		this.mappingClazz = mappingClazz;
		this.cursorSize = cursorSize;
		this.chunkSize = chunkSize;
		this.dbWorkerCount = dbWorkerCount;
		this.jsonWorkerCount = jsonWorkerCount;
		this.bulkActions = bulkActions;
		this.bulkSize = bulkSize;
		this.concurrentRequests = concurrentRequests;
	}
	
	public int getCursorSize() {
		return cursorSize;
	}
	public int getChunkSize() {
		return chunkSize;
	}
	public int getDbWorkerCount() {
		return dbWorkerCount;
	}
	public int getJsonWorkerCount() {
		return jsonWorkerCount;
	}
	public Class<?> getClazz() {
		return clazz;
	}
	public String getIndexerName() {
		return indexerName;
	}
	public String getIndexName() {
		return indexName;
	}
	public int getBulkActions() {
		return bulkActions;
	}
	public long getBulkSize() {
		return bulkSize * 1024 * 1024;
	}
	public int getConcurrentRequests() {
		return concurrentRequests;
	}
	public Mapping getMappings() {
		try {
			Mapping mapping = (Mapping) mappingClazz.getDeclaredConstructor(Boolean.class).newInstance(true);
			mapping.buildMapping();
			return mapping;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public Setting getSettings() {
		try {
			Setting setting = (Setting) settingClazz.getDeclaredConstructor(Boolean.class).newInstance(true);
			setting.buildSettings();
			return setting;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
