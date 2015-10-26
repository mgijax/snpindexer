package org.jax.mgi.snpindexer.indexes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.solr.common.SolrInputDocument;

public class SearchSNPIndexer extends Indexer {

	private int batchNumber = 0;

	public SearchSNPIndexer(String coreName) {
		super(coreName);
	}

	@Override
	public void index() {
		// Delete and Recreate the index
		resetIndex();

		try {

			ResultSet set = sql.executeQuery("select max(sa._object_key) as maxKey from snp.snp_accession sa where sa._logicaldb_key = 73 and sa._mgitype_key = 30");

			set.next();
			int end = set.getInt("maxKey");
			set.close();

			int chunkSize = 10000;
			int chunks = end / chunkSize;
			
			for(int i = 0; i <= chunks; i++) {
				int start = i * chunkSize;
				log.info("Starting Batch: " + i + " of " + chunks);
				batchNumber = i;
				runBatch(start, start + chunkSize);
				progress(i, chunks);
				log.info("");
			}
			
			sql.cleanup();
			
			
			log.info("Finished SNPSearchIndexer query");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		finish();
	}


	private void runBatch(int start, int end) throws SQLException {

		Date startTime = new Date();
		int diff = end - start;

		ResultSet set = sql.executeQuery("select "
				+ "sa.accid as consensussnp_accid, scc.chromosome, scc.startcoordinate, scc.ismulticoord, vt1.term as varclass, vt2.term as fxn, a.accid as marker_accid, ss.strain "
				+ "from "
				+ "snp.snp_accession sa, snp.snp_coord_cache scc, snp.snp_consensussnp_marker scm, snp.snp_consensussnp_strainallele scs, snp.snp_strain ss, mgd.voc_term vt1, mgd.voc_term vt2, mgd.acc_accession a "
				+ "where "
				+ "sa._object_key = scc._consensussnp_key and sa._logicaldb_key = 73 and sa._mgitype_key = 30 and scc._coord_cache_key = scm._coord_cache_key and scc._consensussnp_key = scs._consensussnp_key and "
				+ "scs._mgdstrain_key = ss._mgdstrain_key and scc._varclass_key = vt1._term_key and scm._fxn_key = vt2._term_key and scm._marker_key = a._object_key and a._mgitype_key = 2 and a._logicaldb_key = 1 and "
				+ "sa._object_key > " + start + " and sa._object_key <= " + end);

		ArrayList<SolrInputDocument> docCache = new ArrayList<SolrInputDocument>();
		int counter = 0;
		while (set.next()) {

			counter++;

			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("consensussnp_accid", set.getString("consensussnp_accid"));
			doc.addField("chromosome", set.getString("chromosome"));
			doc.addField("startcoordinate", set.getDouble("startcoordinate"));
			doc.addField("ismulticoord", set.getInt("ismulticoord"));
			doc.addField("varclass", set.getString("varclass"));
			doc.addField("fxn", set.getString("fxn"));
			doc.addField("marker_accid", set.getString("marker_accid"));
			doc.addField("strain", set.getString("strain"));
		
			docCache.add(doc);
			if (docCache.size() >= (diff / 25))  {
				addDocuments(docCache);
				docCache.clear();
			}

		}
		if(!docCache.isEmpty()) addDocuments(docCache);
		if(batchNumber > 0 && batchNumber % 2 == 0) {
			commit();
		}
		Date endTime = new Date();
		long time = (endTime.getTime() - startTime.getTime());
		log.info("Batch took: " + time + "ms to process " + counter + " records at a rate of: " + (counter / time) + "r/ms");
	}
}
