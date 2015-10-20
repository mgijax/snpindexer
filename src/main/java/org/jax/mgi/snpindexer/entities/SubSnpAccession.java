package org.jax.mgi.snpindexer.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jax.mgi.snpindexer.visitors.VisitorInterface;

@Entity
@Table(name="snp.snp_accession")

@NamedQueries({
	@NamedQuery(name="subsnpaccession", query="select s from SubSnpAccession s where s.subSnp.consensusKey = :key and s.logicalDBKey=74 and s.mgiTypeKey=31")
})

public class SubSnpAccession extends SNPEntity implements Serializable {

	@Id
	@Column(name="_accession_key")
	private int key;
	
	@Column(name="accid")
	private String accid;
	
	@Column(name="_logicaldb_key")
	private int logicalDBKey;
	
	@Column(name="_mgitype_key")
	private int mgiTypeKey;
	
	@Column(name="_object_key", insertable=false, updatable=false)
	private int objectKey;
	
	@OneToOne
	@JoinColumn(name="_object_key", referencedColumnName="_subsnp_key") 
	private SubSnp subSnp;

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
	public int getLogicalDBKey() {
		return logicalDBKey;
	}
	public void setLogicalDBKey(int logicalDBKey) {
		this.logicalDBKey = logicalDBKey;
	}
	public int getMgiTypeKey() {
		return mgiTypeKey;
	}
	public void setMgiTypeKey(int mgiTypeKey) {
		this.mgiTypeKey = mgiTypeKey;
	}
	public SubSnp getSubSnp() {
		return subSnp;
	}
	public void setSubSnp(SubSnp subSnp) {
		this.subSnp = subSnp;
	}
	
	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}

}
