package org.jax.mgi.snpindexer.util.es;

import java.io.IOException;
import java.util.List;

/**
 * Concrete mapping builder class for Allele SNP index mappings.
 */
public class AlleleSNPIndexMappings extends Mapping {

    private final List<String> keywordFields = List.of(
        "strains",
        "diffstrains",
        "samestrains",
        "allele",
        "chromosome",
        "consensussnp_accid",
        "fxn",
        "marker_accid",
        "varclass"
    );

    @Override
    public void buildMapping() throws IOException {
        for (String field : keywordFields) {
            addKeywordField(field);
        }
    }
}
