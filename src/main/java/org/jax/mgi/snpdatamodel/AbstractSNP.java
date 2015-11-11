package org.jax.mgi.snpdatamodel;

import java.io.Serializable;

import org.jax.mgi.snpdatamodel.visitors.VisitorInterface;

public abstract class AbstractSNP implements Serializable {

	public abstract void Accept(VisitorInterface pi);
}
