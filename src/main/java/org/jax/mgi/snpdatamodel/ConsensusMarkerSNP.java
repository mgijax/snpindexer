package org.jax.mgi.snpdatamodel;

import org.jax.mgi.snpdatamodel.visitors.VisitorInterface;

public class ConsensusMarkerSNP extends AbstractSNP {

	private String accid;
	private String symbol;
	private String name;
	private String functionClass;
	private String transcript;
	private String protein;
	private String contigAllele;
	private String residue;
	private String aaPosition;
	private String readingFrame;
	
	public String getAccid() {
		return accid;
	}
	public void setAccid(String accid) {
		this.accid = accid;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFunctionClass() {
		return functionClass;
	}
	public void setFunctionClass(String functionClass) {
		this.functionClass = functionClass;
	}
	public String getTranscript() {
		return transcript;
	}
	public void setTranscript(String transcript) {
		this.transcript = transcript;
	}
	public String getProtein() {
		return protein;
	}
	public void setProtein(String protein) {
		this.protein = protein;
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
	
	@Override
	public void Accept(VisitorInterface pi) {
		pi.Visit(this);
	}
}
