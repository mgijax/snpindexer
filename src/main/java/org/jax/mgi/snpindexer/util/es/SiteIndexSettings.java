package org.jax.mgi.snpindexer.util.es;

import java.io.IOException;

public class SiteIndexSettings extends Settings {

	public SiteIndexSettings(Boolean pretty) {
		super(pretty);
	}

	// Used for the settings for site_index
	public void buildSettings() throws IOException {
		builder.startObject();
			builder.startObject("index")
				//.field("max_result_window", "150000")
				//.field("mapping.total_fields.limit", "25000")
				.field("number_of_replicas", "0")
				.field("refresh_interval", "-1")
				.field("number_of_shards", "1");
			builder.endObject();
		builder.endObject();
	}

}
