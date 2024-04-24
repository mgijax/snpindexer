package org.jax.mgi.snpindexer.util.es;

import java.io.IOException;
import java.util.List;

public class ConsensusSNPIndexMappings extends Mapping {

	public ConsensusSNPIndexMappings(Boolean pretty) {
		super(pretty);
	}

	@Override
	public void buildMapping() {
		List<String> keywordFields = List.of(
			"consensussnp_accid"
		);
		
		try {
			builder.startObject().startObject("properties");
			for(String keywordField: keywordFields) {
				new FieldBuilder(builder, keywordField, "keyword").build();
			}
			new FieldBuilder(builder, "objectJSONData", "object").notEnabled().build();
			builder.endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
