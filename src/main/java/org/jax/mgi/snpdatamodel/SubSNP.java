package org.jax.mgi.snpdatamodel;

import java.util.List;

import org.jax.mgi.snpdatamodel.visitors.VisitorInterface;

public class SubSNP extends AbstractSNP {
	
	private int subSnpKey;
	private String accid;
	private String variationClass;
	private String alleleSummary;

	private String orientation;
	private String submitterId;
	private boolean exemplar;

	private List<PopulationSNP> populations;
	
	public int getSubSnpKey() {
		return subSnpKey;
	}
	public void setSubSnpKey(int subSnpKey) {
		this.subSnpKey = subSnpKey;
	}
	public String getAccid() {
		return accid;
	}
	public void setAccid(String accid) {
		this.accid = accid;
	}
	public String getVariationClass() {
		return variationClass;
	}
	public void setVariationClass(String variationClass) {
		this.variationClass = variationClass;
	}
	public String getAlleleSummary() {
		return alleleSummary;
	}
	public void setAlleleSummary(String alleleSummary) {
		this.alleleSummary = alleleSummary;
	}
	public String getOrientation() {
		return orientation;
	}
	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}
	public String getSubmitterId() {
		return submitterId;
	}
	public void setSubmitterId(String submitterId) {
		this.submitterId = submitterId;
	}
	public boolean isExemplar() {
		return exemplar;
	}
	public void setExemplar(boolean exemplar) {
		this.exemplar = exemplar;
	}
	public List<PopulationSNP> getPopulations() {
		return populations;
	}
	public void setPopulations(List<PopulationSNP> populations) {
		this.populations = populations;
	}

	@Override
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}

}
