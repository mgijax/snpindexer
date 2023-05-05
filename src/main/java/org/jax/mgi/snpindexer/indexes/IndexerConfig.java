package org.jax.mgi.snpindexer.indexes;

public enum IndexerConfig {
	
	// (SolrCoreName, IndexerClass, ChunkSize, CommitTimeout, DB Fetch Size)
	
	ConsensusSNPIndexer("ConsensusSNPIndex", "consensus_snp_index", ConsensusSNPIndexer.class, 20000, 100000, 10, 2),
	SearchSNPIndexer("SearchSNPIndex", "search_snp_index", SearchSNPIndexer.class, 500, 100000, 10, 2),
	AlleleSNPIndexer("AlleleSNPIndex", "allele_snp_index", AlleleSNPIndexer.class, 1000, 100000, 10, 2)
	;
	
	// Name of the indexer for program args
	private String indexerName;
	// Name of index in ES
	private String indexName;
	
	// Batch size to process from the database
	private int chunkSize;
	// Java Class of the indexer
	private Class<?> clazz;
	// Name of Solr Core
	
	// Number of documents to index in one batch
	private int bulkActions;
	// Max size in MB for the batch target = 10mb
	private int bulkSize;
	// Number of concurrent batch requests to the server at once
	private int concurrentRequests; // Too many will cause the server to 429

	IndexerConfig(String indexerName, String indexName, Class<?> clazz, int chunkSize, int bulkActions, int bulkSize, int concurrentRequests) {
		this.indexerName = indexerName;
		this.indexName = indexName;
		this.clazz = clazz;
		this.chunkSize = chunkSize;

		this.bulkActions = bulkActions;
		this.bulkSize = bulkSize;
		this.concurrentRequests = concurrentRequests;
	}
	
	public int getChunkSize() {
		return chunkSize;
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
	public int getBulkSize() {
		return bulkSize;
	}
	public int getConcurrentRequests() {
		return concurrentRequests;
	}
	
}
