package org.jax.mgi.snpdatamodel;

import org.jax.mgi.snpdatamodel.visitors.VisitorInterface;

public class AlleleSNP extends AbstractSNP {

	private String allele;
	private String strain;
	private boolean conflict;
	
	public String getAllele() {
		return allele;
	}
	public void setAllele(String allele) {
		this.allele = allele;
	}
	public String getStrain() {
		return strain;
	}
	public void setStrain(String strain) {
		this.strain = strain;
	}
	public boolean isConflict() {
		return conflict;
	}
	public void setConflict(boolean conflict) {
		this.conflict = conflict;
	}
	
	@Override
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
}
