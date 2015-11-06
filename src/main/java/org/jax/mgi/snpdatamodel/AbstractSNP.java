package org.jax.mgi.snpdatamodel;

import org.jax.mgi.snpdatamodel.visitors.VisitorInterface;

public abstract class AbstractSNP {

	public abstract void Accept(VisitorInterface pi);
}
