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
@Table(name="snp.snp_consensussnp_strainallele")
public class ConsensusSNPAllele extends SNPEntity implements Serializable {
	
	@Id
	@Column(name="_consensussnp_key")
	private int key;
	
	@Id
	@ManyToOne
	@JoinColumn(name="_mgdstrain_key", referencedColumnName="_mgdstrain_key") 
	private Strain strain;
	
	@Column(name="allele")
	private String allele;
	
	@Column(name="isconflict")
	private int isconflict;
	
	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
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
	public int getIsconflict() {
		return isconflict;
	}
	public void setIsconflict(int isconflict) {
		this.isconflict = isconflict;
	}

	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
}
