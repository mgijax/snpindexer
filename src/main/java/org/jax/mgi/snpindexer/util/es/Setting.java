package org.jax.mgi.snpindexer.util.es;

import co.elastic.clients.elasticsearch.indices.IndexSettings;

public abstract class Setting {
	
	public abstract IndexSettings getSetting();

}
