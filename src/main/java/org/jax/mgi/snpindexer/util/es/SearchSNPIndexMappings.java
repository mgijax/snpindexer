package org.jax.mgi.snpindexer.util.es;

import java.io.IOException;
import java.util.List;

public class SearchSNPIndexMappings extends Mapping {

	public SearchSNPIndexMappings(Boolean pretty) {
		super(pretty);
	}

	@Override
	public void buildMapping() {
		
		List<String> keywordFields = List.of(
			"strains",
			"chromosome",
			"consensussnp_accid",
			"fxn",
			"marker_accid",
			"varclass"
		);
		
		try {
			builder.startObject().startObject("properties");
			for(String keywordField: keywordFields) {
				new FieldBuilder(builder, keywordField, "keyword").build();
			}
			builder.endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
