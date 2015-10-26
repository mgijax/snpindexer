package org.jax.mgi.snpindexer.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
	private int key;
	
	@Column(name="_consensussnp_key")
	private int consensusKey;
	
	@ManyToOne
	@JoinColumn(name="_varclass_key", referencedColumnName="_term_key") 
	private VOC_Term vocTerm;
	
	@Column(name="orientation")
	private String orientation;
	
	@Column(name="isexemplar")
	private String isexemplar;
	
	@Column(name="allelesummary")
	private String alleleSummary;
	
	@ManyToMany
    @JoinTable(name="snp.snp_subsnp_strainallele",
        joinColumns=@JoinColumn(name="_subsnp_key", referencedColumnName="_subsnp_key"),
        inverseJoinColumns=@JoinColumn(name="_population_key", referencedColumnName="_population_key")
        )
	private Set<Population> populationList;
	
	@OneToMany(mappedBy="key")
	private List<SubSNPStrainAllele> subSNPStrainAlleles;
	
	@Transient
	private AccessionObject submitterSNPId;
	
	@Transient
	private AccessionObject subSNPAccessionObject;

	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public int getConsensusKey() {
		return consensusKey;
	}
	public void setConsensusKey(int consensusKey) {
		this.consensusKey = consensusKey;
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
	public Set<Population> getPopulationList() {
		return populationList;
	}
	public void setPopulationList(Set<Population> populationList) {
		this.populationList = populationList;
	}
	public List<SubSNPStrainAllele> getSubSNPStrainAlleles() {
		return subSNPStrainAlleles;
	}
	public void setSubSNPStrainAlleles(List<SubSNPStrainAllele> subSNPStrainAlleles) {
		this.subSNPStrainAlleles = subSNPStrainAlleles;
	}

	
	@Transient
	public Population getPopulation() {
		return (Population)populationList.toArray()[0];
	}
	@Transient
	public AccessionObject getSubmitterSNPId() {
		return submitterSNPId;
	}
	@Transient
	public void setSubmitterSNPId(AccessionObject submitterSNPId) {
		this.submitterSNPId = submitterSNPId;
	}
	@Transient
	public AccessionObject getSubSNPAccessionObject() {
		return subSNPAccessionObject;
	}
	@Transient
	public void setSubSNPAccessionObject(AccessionObject subSNPAccessionObject) {
		this.subSNPAccessionObject = subSNPAccessionObject;
	}
	
	
	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}


}
