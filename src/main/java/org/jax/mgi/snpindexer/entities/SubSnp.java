package org.jax.mgi.snpindexer.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jax.mgi.snpindexer.visitors.VisitorInterface;

@Entity
@Table(name="snp.snp_subsnp")
public class SubSnp extends SNPEntity implements Serializable {

	@Id
	@Column(name="_subsnp_key")
	private int subSNPId;
	
	@Column(name="_consensussnp_key")
	private int consensusId;
	
	@Column(name="_subhandle_key")
	private int subHandleId;
	
	@ManyToOne
	@JoinColumn(name="_varclass_key", referencedColumnName="_term_key") 
	private VOC_Term vocTerm;
	
	@Column(name="orientation")
	private String orientation;
	
	@Column(name="isexemplar")
	private String isexemplar;
	
	@Column(name="allelesummary")
	private String alleleSummary;
	
	@OneToMany(mappedBy="subSNPId")
	private List<SubSNPStrainAllele> subSNPStrainAlleles;
	
	public int getSubSNPId() {
		return subSNPId;
	}
	public void setSubSNPId(int subSNPId) {
		this.subSNPId = subSNPId;
	}
	public int getConsensusId() {
		return consensusId;
	}
	public void setConsensusId(int consensusId) {
		this.consensusId = consensusId;
	}
	public int getSubHandleId() {
		return subHandleId;
	}
	public void setSubHandleId(int subHandleId) {
		this.subHandleId = subHandleId;
	}
	public VOC_Term getVocTerm() {
		return vocTerm;
	}
	public void setVocTerm(VOC_Term vocTerm) {
		this.vocTerm = vocTerm;
	}
	public String getOrientation() {
		return orientation;
	}
	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}
	public String getIsexemplar() {
		return isexemplar;
	}
	public void setIsexemplar(String isexemplar) {
		this.isexemplar = isexemplar;
	}
	public String getAlleleSummary() {
		return alleleSummary;
	}
	public void setAlleleSummary(String alleleSummary) {
		this.alleleSummary = alleleSummary;
	}
	public List<SubSNPStrainAllele> getSubSNPStrainAlleles() {
		return subSNPStrainAlleles;
	}
	public void setSubSNPStrainAlleles(List<SubSNPStrainAllele> subSNPStrainAlleles) {
		this.subSNPStrainAlleles = subSNPStrainAlleles;
	}

	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}

}
