package org.jax.mgi.snpindexer.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jax.mgi.snpindexer.visitors.VisitorInterface;


@Entity
@Table(name="snp.snp_population")
public class Population extends SNPEntity implements Serializable {


	@Id
	@Column(name="_population_key")
	private int id;
	@Column(name="subhandle")
	private String subHandle;
	@Column(name="_subhandle_key")
	private int subHandleId;
	@Column(name="name")
	private String name;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSubHandle() {
		return subHandle;
	}
	public void setSubHandle(String subHandle) {
		this.subHandle = subHandle;
	}
	public int getSubHandleId() {
		return subHandleId;
	}
	public void setSubHandleId(int subHandleId) {
		this.subHandleId = subHandleId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}

}
