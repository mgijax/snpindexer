package org.jax.mgi.snpindexer.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jax.mgi.snpindexer.visitors.VisitorInterface;

@Entity
@Table(name="snp.snp_strain")
public class Strain extends SNPEntity implements Serializable {

	@Id
	@Column(name="_snpstrain_key")
	private int strainId;
	
	@Column(name="_mgdstrain_key")
	private int mgdStrainId;
	
	@Column(name="strain")
	private String strain;
	
	@Column(name="sequencenum")
	private int seqNum;

	public int getStrainId() {
		return strainId;
	}
	public void setStrainId(int strainId) {
		this.strainId = strainId;
	}
	public int getMgdStrainId() {
		return mgdStrainId;
	}
	public void setMgdStrainId(int mgdStrainId) {
		this.mgdStrainId = mgdStrainId;
	}
	public String getStrain() {
		return strain;
	}
	public void setStrain(String strain) {
		this.strain = strain;
	}
	public int getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
}
