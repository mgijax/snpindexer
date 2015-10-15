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
@Table(name="snp.snp_subsnp_strainallele")
public class SubSNPStrainAllele extends SNPEntity implements Serializable {

	@Id
	@Column(name="_subsnp_key")
	private int subSNPId;
	
	@Id
	@ManyToOne
	@JoinColumn(name="_population_key", referencedColumnName="_population_key") 
	private Population population;
	
	@Id
	@ManyToOne
	@JoinColumn(name="_mgdstrain_key", referencedColumnName="_mgdstrain_key") 
	private Strain strain;
	
	@Column(name="allele")
	private String allele;
	
	
	public int getSubSNPId() {
		return subSNPId;
	}
	public void setSubSNPId(int subSNPId) {
		this.subSNPId = subSNPId;
	}
	public Population getPopulation() {
		return population;
	}
	public void setPopulation(Population population) {
		this.population = population;
	}
	public Strain getStrain() {
		return strain;
	}
	public void setStrain(Strain strain) {
		this.strain = strain;
	}
	public String getAllele() {
		return allele;
	}
	public void setAllele(String allele) {
		this.allele = allele;
	}

	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
	
}
