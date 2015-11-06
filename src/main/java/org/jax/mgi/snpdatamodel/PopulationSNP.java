package org.jax.mgi.snpdatamodel;

import java.util.ArrayList;
import java.util.List;

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
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
	public PopulationSNP dup() {
		PopulationSNP p = new PopulationSNP();
		p.setAccid(accid);
		p.setPopulationName(populationName);
		p.setSubHandleName(subHandleName);
		p.setAlleles(new ArrayList<AlleleSNP>());
		return p;
	}
}
