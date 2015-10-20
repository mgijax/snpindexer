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
	private int key;
	
	@Column(name="_mgdstrain_key")
	private int mgdStrainKey;
	
	@Column(name="strain")
	private String strain;
	
	@Column(name="sequencenum")
	private int seqNum;

	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public int getMgdStrainKey() {
		return mgdStrainKey;
	}
	public void setMgdStrainKey(int mgdStrainKey) {
		this.mgdStrainKey = mgdStrainKey;
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
