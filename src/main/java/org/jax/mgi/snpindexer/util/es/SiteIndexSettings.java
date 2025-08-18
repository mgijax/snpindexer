package org.jax.mgi.snpindexer.util.es;

import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.indices.IndexSettings;

public class SiteIndexSettings extends Setting {

	// Used for the settings for site_index
//	public void buildSettings() throws IOException {
//		builder.startObject();
//			builder.startObject("index")
//				//.field("max_result_window", "150000")
//				//.field("mapping.total_fields.limit", "25000")
//				.field("number_of_replicas", "0")
//				.field("refresh_interval", "-1")
//				.field("number_of_shards", "4");
//			builder.endObject();
//		builder.endObject();
//	}
	
	@Override
    public IndexSettings getSetting() {
    	return new IndexSettings.Builder()
    			//.maxRescoreWindow(150000)
    			//.mapping(m -> m.totalFields(tf -> tf.limit("25000")))
                .numberOfReplicas("0")
                .refreshInterval(new Time.Builder().time("-1").build())
                .numberOfShards("4")
                .build();
    }		

}
