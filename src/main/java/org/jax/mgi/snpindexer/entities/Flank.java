package org.jax.mgi.snpindexer.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jax.mgi.snpindexer.visitors.VisitorInterface;

@Entity
@Table(name="snp.snp_flank")
public class Flank extends SNPEntity implements Serializable {

	@Id
	@Column(name="_consensussnp_key")
	private int key;
	
	@Column(name="flank")
	private String flank;
	
	@Id
	@Column(name="sequencenum")
	private int seqNum;
	
	@Id
	@Column(name="is5prime")
	private int is5Prime;

	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public String getFlank() {
		return flank;
	}
	public void setFlank(String flank) {
		this.flank = flank;
	}
	public int getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}
	public int getIs5Prime() {
		return is5Prime;
	}
	public void setIs5Prime(int is5Prime) {
		this.is5Prime = is5Prime;
	}

	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}

}
