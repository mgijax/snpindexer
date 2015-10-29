package org.jax.mgi.snpdatamodel;

import java.util.List;

import javax.persistence.Transient;

import org.jax.mgi.snpdatamodel.visitors.VisitorInterface;

public class PopulationSNP extends AbstractSNP {

	private String accid;
	private String subHandleName;
	private String populationName;
	private List<AlleleSNP> alleles;
	
	public String getAccid() {
		return accid;
	}
	public void setAccid(String accid) {
		this.accid = accid;
	}
	public String getSubHandleName() {
		return subHandleName;
	}
	public void setSubHandleName(String subHandleName) {
		this.subHandleName = subHandleName;
	}
	public String getPopulationName() {
		return populationName;
	}
	public void setPopulationName(String populationName) {
		this.populationName = populationName;
	}
	public List<AlleleSNP> getAlleles() {
		return alleles;
	}
	public void setAlleles(List<AlleleSNP> alleles) {
		this.alleles = alleles;
	}
	
	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
}
