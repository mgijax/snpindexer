package org.jax.mgi.snpindexer.util.es;

import java.io.IOException;

public abstract class Setting extends Builder {

	public Setting(Boolean pretty) {
		super(pretty);
	}

	public abstract void buildSettings() throws IOException;

}
