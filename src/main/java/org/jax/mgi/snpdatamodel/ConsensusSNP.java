package org.jax.mgi.snpdatamodel;

import java.util.List;

import org.jax.mgi.snpdatamodel.visitors.VisitorInterface;

public class ConsensusSNP extends AbstractSNP {

	private int consensusKey;
	private String accid;
	private String variationClass;
	private String alleleSummary;
	private String iupaccode;
	private String buildCreated;
	private String buildUpdated;
	private String flank5Prime;
	private String flank3Prime;

	private List<SubSNP> subSNPs;
	private List<ConsensusCoordinateSNP> consensusCoordinates;
	private List<AlleleSNP> alleles;
	
	public int getConsensusKey() {
		return consensusKey;
	}
	public void setConsensusKey(int consensusKey) {
		this.consensusKey = consensusKey;
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
	public String getIupaccode() {
		return iupaccode;
	}
	public void setIupaccode(String iupaccode) {
		this.iupaccode = iupaccode;
	}
	public String getBuildCreated() {
		return buildCreated;
	}
	public void setBuildCreated(String buildCreated) {
		this.buildCreated = buildCreated;
	}
	public String getBuildUpdated() {
		return buildUpdated;
	}
	public void setBuildUpdated(String buildUpdated) {
		this.buildUpdated = buildUpdated;
	}
	public String getFlank5Prime() {
		return flank5Prime;
	}
	public void setFlank5Prime(String flank5Prime) {
		this.flank5Prime = flank5Prime;
	}
	public String getFlank3Prime() {
		return flank3Prime;
	}
	public void setFlank3Prime(String flank3Prime) {
		this.flank3Prime = flank3Prime;
	}
	public List<SubSNP> getSubSNPs() {
		return subSNPs;
	}
	public void setSubSNPs(List<SubSNP> subSNPs) {
		this.subSNPs = subSNPs;
	}
	public List<ConsensusCoordinateSNP> getConsensusCoordinates() {
		return consensusCoordinates;
	}
	public void setConsensusCoordinates(List<ConsensusCoordinateSNP> consensusCoordinates) {
		this.consensusCoordinates = consensusCoordinates;
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
}
