package org.jax.mgi.snpindexer.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jax.mgi.snpindexer.visitors.VisitorInterface;

@Entity
@Table(name="snp.snp_consensussnp")
public class ConsensusSNP extends SNPEntity implements Serializable {

	@Id
	@Column(name="_consensussnp_key")
	private int key;
	
	@ManyToOne
	@JoinColumn(name="_varclass_key", referencedColumnName="_term_key") 
	private VOC_Term vocTerm;

	@Column(name="allelesummary")
	private String alleleSummary;
	@Column(name="iupaccode")
	private String iupaccode;
	@Column(name="buildupdated")
	private String buildUpdated;
	@Column(name="buildcreated")
	private String buildCreated;

	@OneToMany(mappedBy="key")
	private List<Flank> flanks;
	
	@OneToMany(mappedBy="consensusKey")
	private List<CoordCache> coordCaches;
	
	@Transient
	private List<SubSnpAccession> subSnpAccessions;
	
	@OneToMany(mappedBy="key")
	private List<ConsensusSNPAllele> consensusSNPAlleles;
	
	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
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
	public String getBuildUpdated() {
		return buildUpdated;
	}
	public void setBuildUpdated(String buildUpdated) {
		this.buildUpdated = buildUpdated;
	}
	public String getBuildCreated() {
		return buildCreated;
	}
	public void setBuildCreated(String buildCreated) {
		this.buildCreated = buildCreated;
	}
	public List<Flank> getFlanks() {
		return flanks;
	}
	public void setFlanks(List<Flank> flanks) {
		this.flanks = flanks;
	}
	public List<CoordCache> getCoordCaches() {
		return coordCaches;
	}
	public void setCoordCaches(List<CoordCache> coordCaches) {
		this.coordCaches = coordCaches;
	}
	@Transient
	public List<SubSnpAccession> getSubSnpAccessions() {
		return subSnpAccessions;
	}
	@Transient
	public void setSubSnpAccessions(List<SubSnpAccession> subSnpAccessions) {
		this.subSnpAccessions = subSnpAccessions;
	}
	public List<ConsensusSNPAllele> getConsensusSNPAlleles() {
		return consensusSNPAlleles;
	}
	public void setConsensusSNPAlleles(List<ConsensusSNPAllele> consensusSNPAlleles) {
		this.consensusSNPAlleles = consensusSNPAlleles;
	}
	
	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}

}
