package org.jax.mgi.snpindexer.util.es;

import java.io.IOException;

public abstract class Settings extends Builder {

	public Settings(Boolean pretty) {
		super(pretty);
	}

	public abstract void buildSettings() throws IOException;

}
