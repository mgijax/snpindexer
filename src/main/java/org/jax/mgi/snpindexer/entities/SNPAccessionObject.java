package org.jax.mgi.snpindexer.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jax.mgi.snpindexer.visitors.VisitorInterface;


@NamedQueries({
	@NamedQuery(name="snpaccessionobjectprefix", query="select a from SNPAccessionObject a where a.objectKey = :key and a.logicalDBKey = :logicalDBKey and a.mgiTypeKey = :mgiTypeKey and a.prefixPart = :prefixPart"),
	@NamedQuery(name="snpaccessionobject", query="select a from SNPAccessionObject a where a.objectKey = :key and a.logicalDBKey = :logicalDBKey and a.mgiTypeKey = :mgiTypeKey"),
})

@Entity
@Table(name="snp.snp_accession")
public class SNPAccessionObject extends SNPEntity implements Serializable {
	
	@Id
	@Column(name="_accession_key")
	private int key;

	@Column(name="accid")
	private String accid;
	
	@Column(name="prefixpart")
	private String prefixPart;
	
	@Column(name="_logicaldb_key")
	private int logicalDBKey;
	
	@Column(name="_mgitype_key")
	private int mgiTypeKey;
	
	@Column(name="_object_key")
	private int objectKey;

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
	public int getObjectKey() {
		return objectKey;
	}
	public void setObjectKey(int objectKey) {
		this.objectKey = objectKey;
	}
	
	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
}
