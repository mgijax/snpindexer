package org.jax.mgi.snpindexer.util.es;

import java.io.IOException;
import java.util.List;

public class ConsensusSNPIndexMappings extends Mapping {

	private final List<String> keywordFields = List.of(
			"consensussnp_accid"
			);

	@Override
	public void buildMapping() throws IOException {
		for (String field : keywordFields) {
			addKeywordField(field);
		}
	}

}
