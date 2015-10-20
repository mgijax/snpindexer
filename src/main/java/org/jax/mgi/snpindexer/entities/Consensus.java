package org.jax.mgi.snpindexer.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jax.mgi.snpindexer.visitors.VisitorInterface;

@Entity
@Table(name="snp.snp_accession")
public class Consensus extends SNPEntity implements Serializable {

	@Id
	@Column(name="_accession_key")
	private int key;
	
	@Column(name="accid")
	private String accid;
	
	@OneToOne
	@JoinColumn(name="_object_key", referencedColumnName="_consensussnp_key") 
	private ConsensusSNP consensusSNP;
	
	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public String getAccid() {
		return accid;
	}
	public void setAccid(String accid) {
		this.accid = accid;
	}
	public ConsensusSNP getConsensusSNP() {
		return consensusSNP;
	}
	public void setConsensusSNP(ConsensusSNP consensusSNP) {
		this.consensusSNP = consensusSNP;
	}

	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
}
