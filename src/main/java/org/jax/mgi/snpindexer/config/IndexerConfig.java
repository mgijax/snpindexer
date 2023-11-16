package org.jax.mgi.snpindexer.config;

import org.jax.mgi.snpindexer.indexes.AlleleSNPIndexer;
import org.jax.mgi.snpindexer.indexes.ConsensusSNPIndexer;
import org.jax.mgi.snpindexer.indexes.SearchSNPIndexer;

public enum IndexerConfig {
	
	// (SolrCoreName, IndexerClass, ChunkSize, CommitTimeout, DB Fetch Size)
	
	ConsensusSNPIndexer("ConsensusSNPIndex", "consensus_snp_index", ConsensusSNPIndexer.class,  50_000, 10_000, 8, 10_000, 10, 2),
	SearchSNPIndexer(   "SearchSNPIndex",    "search_snp_index",    SearchSNPIndexer.class,      500,   500, 4, 100_000, 10, 2),
	AlleleSNPIndexer(   "AlleleSNPIndex",    "allele_snp_index",    AlleleSNPIndexer.class,     1000,  1000, 4, 100_000, 10, 2)
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
	private int workerCount;
	// Java Class of the indexer
	private Class<?> clazz;
	// Name of Solr Core
	
	// Number of documents to index in one batch
	private int bulkActions;
	// Max size in MB for the batch target = 10mb
	private long bulkSize;
	// Number of concurrent batch requests to the server at once
	private int concurrentRequests; // Too many will cause the server to 429

	IndexerConfig(String indexerName, String indexName, Class<?> clazz, int cursorSize, int chunkSize, int workerCount, int bulkActions, long bulkSize, int concurrentRequests) {
		this.indexerName = indexerName;
		this.indexName = indexName;
		this.clazz = clazz;
		this.cursorSize = cursorSize;
		this.chunkSize = chunkSize;
		this.workerCount = workerCount;

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
	public int getWorkerCount() {
		return workerCount;
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
	
}
