package org.jax.mgi.snpdatamodel;

import java.util.List;

import org.jax.mgi.snpdatamodel.visitors.VisitorInterface;

public class ConsensusCoordinateSNP extends AbstractSNP {
	
	private String chromosome;
	private int startCoordinate;
	private boolean multiCoord;
	private String strand;

	private String variationClass;
	private String alleleSummary;
	private String iupaccode;

	private List<ConsensusMarkerSNP> markers;

	
	public String getChromosome() {
		return chromosome;
	}
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}
	public int getStartCoordinate() {
		return startCoordinate;
	}
	public void setStartCoordinate(int startCoordinate) {
		this.startCoordinate = startCoordinate;
	}
	public boolean isMultiCoord() {
		return multiCoord;
	}
	public void setMultiCoord(boolean multiCoord) {
		this.multiCoord = multiCoord;
	}
	public String getStrand() {
		return strand;
	}
	public void setStrand(String strand) {
		this.strand = strand;
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
	public List<ConsensusMarkerSNP> getMarkers() {
		return markers;
	}
	public void setMarkers(List<ConsensusMarkerSNP> markers) {
		this.markers = markers;
	}
	
	@Override
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
}
