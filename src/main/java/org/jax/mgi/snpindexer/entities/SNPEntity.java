package org.jax.mgi.snpindexer.entities;

import javax.persistence.Transient;

import org.jax.mgi.snpindexer.visitors.VisitorInterface;

public abstract class SNPEntity {

	@Transient
	public abstract void Accept(VisitorInterface pi);
}
