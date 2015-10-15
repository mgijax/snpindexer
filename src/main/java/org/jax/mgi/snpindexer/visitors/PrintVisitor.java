package org.jax.mgi.snpindexer.visitors;

import java.io.PrintStream;

import org.jax.mgi.snpindexer.entities.Consensus;
import org.jax.mgi.snpindexer.entities.ConsensusSNPAllele;
import org.jax.mgi.snpindexer.entities.CoordCache;
import org.jax.mgi.snpindexer.entities.Flank;
import org.jax.mgi.snpindexer.entities.Population;
import org.jax.mgi.snpindexer.entities.Strain;
import org.jax.mgi.snpindexer.entities.SubSNPStrainAllele;
import org.jax.mgi.snpindexer.entities.SubSnp;
import org.jax.mgi.snpindexer.entities.VOC_Term;

public class PrintVisitor extends PrinterUtil implements VisitorInterface {
	
	@Override
	public void Visit(Consensus consensus) {
		printi("Consensus: {");
		consensus.getVocTerm().Accept(this);
		printiu("Id: " + consensus.getId());
		printiu("AlleleSummary: " + consensus.getAlleleSummary());
		printiu("Iupaccode: " + consensus.getIupaccode());
		printiu("BuildCreated: " + consensus.getBuildCreated());
		printiu("BuildUpdated: " + consensus.getBuildUpdated());
		printi("Flanks: [");
		for(Flank f: consensus.getFlanks()) {
			f.Accept(this);
		}
		printu("]");
		printi("CoordCaches: [");
		for(CoordCache c: consensus.getCoordCaches()) {
			c.Accept(this);
		}
		printu("]");
		
		printi("SubSNPs: [");
		for(SubSnp s: consensus.getSubSNPs()) {
			s.Accept(this);
		}
		printu("]");
		
		printi("ConsensusSNPAlleles: [");
		for(ConsensusSNPAllele c: consensus.getConsensusSNPAlleles()) {
			c.Accept(this);
		}
		printu("]");
		
		printu("}");
	}
	

	@Override
	public void Visit(CoordCache coordCache) {
		printi("Coord: {");
		
		printiu("Chromosome: " + coordCache.getChromosome());
		printiu("seqNum: " + coordCache.getSeqNum());
		printiu("Startcoordinate: " + coordCache.getStartcoordinate());
		printiu("IsMultiCoord: " + coordCache.getIsMultiCoord());
		printiu("Strand: " + coordCache.getStrand());
		coordCache.getVocTerm().Accept(this);
		printiu("AlleleSummary: " + coordCache.getAlleleSummary());
		printiu("Iupaccode: " + coordCache.getIupaccode());
		
		printu("}");
	}
	
	@Override
	public void Visit(SubSnp subSnp) {
		printi("SubSNP: {");
		printiu("SubHandleId: " + subSnp.getSubHandleId());
		subSnp.getVocTerm().Accept(this);
		printiu("Orientation: " + subSnp.getOrientation());
		printiu("Isexemplar: " + subSnp.getIsexemplar());
		printiu("AlleleSummary: " + subSnp.getAlleleSummary());
		printi("SubSNPAlleles: [");
		for(SubSNPStrainAllele s: subSnp.getSubSNPStrainAlleles()) {
			s.Accept(this);
		}
		printu("]");
		printu("}");
		
	}

	@Override
	public void Visit(SubSNPStrainAllele subSNPStrainAllele) {
		printi("SubSNPAllele: {");
		printiu("Allele: " + subSNPStrainAllele.getAllele());
		subSNPStrainAllele.getPopulation().Accept(this);
		subSNPStrainAllele.getStrain().Accept(this);
		printu("}");
	}
	
	@Override
	public void Visit(ConsensusSNPAllele consensusSNPAllele) {
		printi("ConsensusSNPAllele: {");
		printiu("Allele: " + consensusSNPAllele.getAllele());
		printiu("isConflict: " + consensusSNPAllele.getIsconflict());
		consensusSNPAllele.getStrain().Accept(this);
		printu("}");
	}

	@Override
	public void Visit(Population population) {
		printiu("Population: { Id: " + population.getId() + " subhandle: " + population.getSubHandle() + " name: " + population.getName() + "}");
	}

	@Override
	public void Visit(Strain strain) {
		printiu("Strain: { Id: " + strain.getStrainId() + " strain: " + strain.getStrain() + " seqNum: " + strain.getSeqNum() + "}");
	}

	@Override
	public void Visit(VOC_Term voc_Term) {
		printiu("VocTerm: { Id: " + voc_Term.getId() + " term: " + voc_Term.getTerm() + " ab: " + voc_Term.getAbbreviation() + "}");
	}

	@Override
	public void Visit(Flank flank) {
		printiu("Flank: { Id: " + flank.getId() + " flank: " + flank.getFlank() + " Seq: " + flank.getSeqNum() + " is5Prime: " + flank.getIs5Prime() + "}");
	}

}
