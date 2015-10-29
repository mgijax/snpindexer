package org.jax.mgi.snpdatamodel;

import javax.persistence.Transient;

import org.jax.mgi.snpdatamodel.visitors.VisitorInterface;

public abstract class AbstractSNP {

	@Transient
	public abstract void Accept(VisitorInterface pi);
}
