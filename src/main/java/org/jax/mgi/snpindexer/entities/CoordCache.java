package org.jax.mgi.snpindexer.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jax.mgi.snpindexer.visitors.VisitorInterface;

@Entity
@Table(name="snp.snp_coord_cache")
public class CoordCache extends SNPEntity implements Serializable {

	@Id
	@Column(name="_coord_cache_key")
	private int id;
	
	@Column(name="_consensussnp_key")
	private int consensusId;
	
	@Column(name="chromosome")
	private String chromosome;
	
	@Column(name="sequencenum")
	private int seqNum;
	
	@Column(name="startcoordinate")
	private double startcoordinate;
	
	@Column(name="ismulticoord")
	private int isMultiCoord;
	
	@Column(name="strand")
	private String strand;
	
	@ManyToOne
	@JoinColumn(name="_varclass_key", referencedColumnName="_term_key") 
	private VOC_Term vocTerm;
	
	@Column(name="allelesummary")
	private String alleleSummary;
	
	@Column(name="iupaccode")
	private String iupaccode;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getConsensusId() {
		return consensusId;
	}
	public void setConsensusId(int consensusId) {
		this.consensusId = consensusId;
	}
	public String getChromosome() {
		return chromosome;
	}
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}
	public int getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}
	public double getStartcoordinate() {
		return startcoordinate;
	}
	public void setStartcoordinate(double startcoordinate) {
		this.startcoordinate = startcoordinate;
	}
	public int getIsMultiCoord() {
		return isMultiCoord;
	}
	public void setIsMultiCoord(int isMultiCoord) {
		this.isMultiCoord = isMultiCoord;
	}
	public String getStrand() {
		return strand;
	}
	public void setStrand(String strand) {
		this.strand = strand;
	}
	public VOC_Term getVocTerm() {
		return vocTerm;
	}
	public void setVocTerm(VOC_Term vocTerm) {
		this.vocTerm = vocTerm;
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

	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
}
