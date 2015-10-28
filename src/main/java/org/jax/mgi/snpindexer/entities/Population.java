package org.jax.mgi.snpindexer.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jax.mgi.snpindexer.visitors.VisitorInterface;


@Entity
@Table(name="snp.snp_population")
public class Population extends SNPEntity implements Serializable {


	@Id
	@Column(name="_population_key")
	private int key;
	
	@Column(name="subhandle")
	private String subHandleText;
	
	@ManyToOne
	@JoinColumn(name="_subhandle_key", referencedColumnName="_term_key") 
	private VOC_Term subHandle;
	
	@Column(name="name")
	private String name;
	
	@Transient
	private List<SubSNPStrainAllele> subSNPStrainAlleles;
	
	@Transient
	private SNPAccessionObject populationAccession;

	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public String getSubHandleText() {
		return subHandleText;
	}
	public void setSubHandleText(String subHandleText) {
		this.subHandleText = subHandleText;
	}
	public VOC_Term getSubHandle() {
		return subHandle;
	}
	public void setSubHandle(VOC_Term subHandle) {
		this.subHandle = subHandle;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Transient
	public List<SubSNPStrainAllele> getSubSNPStrainAlleles() {
		return subSNPStrainAlleles;
	}
	@Transient
	public void setSubSNPStrainAlleles(List<SubSNPStrainAllele> subSNPStrainAlleles) {
		this.subSNPStrainAlleles = subSNPStrainAlleles;
	}
	@Transient
	public SNPAccessionObject getPopulationAccession() {
		return populationAccession;
	}
	@Transient
	public void setPopulationAccession(SNPAccessionObject populationAccession) {
		this.populationAccession = populationAccession;
	}
	
	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}

}
