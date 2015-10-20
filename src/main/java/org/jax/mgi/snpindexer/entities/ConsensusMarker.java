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
@Table(name="snp.snp_consensussnp_marker")
public class ConsensusMarker extends SNPEntity implements Serializable {

	@Id
	@Column(name="_consensussnp_marker_key")
	private int key;
	
	@Column(name="_consensussnp_key")
	private int consensusKey;
	
	@Column(name="_coord_cache_key")
	private int coordCacheKey;
	
	@ManyToOne
	@JoinColumn(name="_fxn_key", referencedColumnName="_term_key") 
	private VOC_Term vocTerm;
	
	@Column(name="contig_allele")
	private String contigAllele;
	
	@Column(name="residue")
	private String residue;
	
	@Column(name="aa_position")
	private String aaPosition;
	
	@Column(name="reading_frame")
	private String readingFrame;
	
	@ManyToOne
	@JoinColumn(name="_marker_key", referencedColumnName="_marker_key") 
	private Marker marker;
	
	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public int getConsensusKey() {
		return consensusKey;
	}
	public void setConsensusKey(int consensusKey) {
		this.consensusKey = consensusKey;
	}
	public int getCoordCacheKey() {
		return coordCacheKey;
	}
	public void setCoordCacheKey(int coordCacheKey) {
		this.coordCacheKey = coordCacheKey;
	}
	public VOC_Term getVocTerm() {
		return vocTerm;
	}
	public void setVocTerm(VOC_Term vocTerm) {
		this.vocTerm = vocTerm;
	}
	public String getContigAllele() {
		return contigAllele;
	}
	public void setContigAllele(String contigAllele) {
		this.contigAllele = contigAllele;
	}
	public String getResidue() {
		return residue;
	}
	public void setResidue(String residue) {
		this.residue = residue;
	}
	public String getAaPosition() {
		return aaPosition;
	}
	public void setAaPosition(String aaPosition) {
		this.aaPosition = aaPosition;
	}
	public String getReadingFrame() {
		return readingFrame;
	}
	public void setReadingFrame(String readingFrame) {
		this.readingFrame = readingFrame;
	}
	public Marker getMarker() {
		return marker;
	}
	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	@Override
	@Transient
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
}
