package org.jax.mgi.snpindexer.indexes;

public enum IndexerConfig {
	
	// (SolrCoreName, IndexerClass, ChunkSize, CommitTimeout, DB Fetch Size)
	
	ConsensusSNPIndexer("ConsensusSNPIndex", ConsensusSNPIndexer.class, 25000, 5000, 50000),
	SearchSNPIndexer("SearchSNPIndex", SearchSNPIndexer.class, 10000, 30000, 50000)
	;
	
	// Batch size to process from the database
	private int chunkSize;
	// Java Class of the indexer
	private Class<?> clazz;
	// Name of Solr Core
	private String coreName;
	// Frequency at which to commit documents to solr
	private int	commitFreq;
	// Fetch size for sql records per cursor
	private int sqlFetchSize;

	IndexerConfig(String coreName, Class<?> clazz, int chunkSize, int commitFreq, int sqlFetchSize) {
		this.coreName = coreName;
		this.clazz = clazz;
		this.chunkSize = chunkSize;
		this.commitFreq = commitFreq;
		this.sqlFetchSize = sqlFetchSize;
	}
	
	public int getChunkSize() {
		return chunkSize;
	}
	public Class<?> getClazz() {
		return clazz;
	}
	public String getCoreName() {
		return coreName;
	}
	public long getCommitFreq() {
		return commitFreq;
	}
	public int getSqlFetchSize() {
		return sqlFetchSize;
	}
	
}
