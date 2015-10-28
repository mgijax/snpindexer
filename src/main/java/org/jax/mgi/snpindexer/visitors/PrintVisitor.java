package org.jax.mgi.snpindexer.visitors;

import org.jax.mgi.snpindexer.entities.MGDAccessionObject;
import org.jax.mgi.snpindexer.entities.SNPAccessionObject;
import org.jax.mgi.snpindexer.entities.ConsensusMarker;
import org.jax.mgi.snpindexer.entities.ConsensusSNP;
import org.jax.mgi.snpindexer.entities.ConsensusSNPAllele;
import org.jax.mgi.snpindexer.entities.CoordCache;
import org.jax.mgi.snpindexer.entities.Flank;
import org.jax.mgi.snpindexer.entities.Marker;
import org.jax.mgi.snpindexer.entities.Population;
import org.jax.mgi.snpindexer.entities.Strain;
import org.jax.mgi.snpindexer.entities.SubSNPStrainAllele;
import org.jax.mgi.snpindexer.entities.SubSnp;
import org.jax.mgi.snpindexer.entities.VOC_Term;

public class PrintVisitor extends PrinterUtil implements VisitorInterface {

	@Override
	public void Visit(ConsensusMarker consensusMarker) {
		printi("ConsensusMarker: {");
		
		printiu("Key: " + consensusMarker.getKey());
		
		if(consensusMarker.getContigAllele() != null) {
			printiu("ContigAllele: " + consensusMarker.getContigAllele());
		}
		if(consensusMarker.getResidue() != null) {
			printiu("Residue: " + consensusMarker.getResidue());
		}
		if(consensusMarker.getAaPosition() != null) {
			printiu("AaPosition: " + consensusMarker.getAaPosition());
		}
		if(consensusMarker.getReadingFrame() != null) {
			printiu("ReadingFrame: " + consensusMarker.getReadingFrame());
		}
		
		if(consensusMarker.getTranscript() != null) {
			printi("Transcript: {");
			consensusMarker.getTranscript().Accept(this);
			printu("}");
		}
		if(consensusMarker.getProtein() != null) {
			printi("Protein: {");
			consensusMarker.getProtein().Accept(this);
			printu("}");
		}
		
		printi("Function Class: {");
		consensusMarker.getVocTerm().Accept(this);
		printu("}");
		consensusMarker.getMarker().Accept(this);
		printu("}");
	}
	
	@Override
	public void Visit(ConsensusSNP consensusSNP) {

		printi("ConsensusSNP: {");
		printiu("Key: " + consensusSNP.getKey());
		consensusSNP.getConsensusAccession().Accept(this);
		printi("Variation Class: {");
		consensusSNP.getVocTerm().Accept(this);
		printu("}");
		printiu("AlleleSummary: " + consensusSNP.getAlleleSummary());
		printiu("Iupaccode: " + consensusSNP.getIupaccode());
		printiu("BuildCreated: " + consensusSNP.getBuildCreated());
		printiu("BuildUpdated: " + consensusSNP.getBuildUpdated());
		printi("Flanks: [");
		for(Flank f: consensusSNP.getFlanks()) {
			f.Accept(this);
		}
		printu("]");
		
		printi("SubSnps: [");
		for(SubSnp s: consensusSNP.getSubSnps()) {
			s.Accept(this);
		}
		printu("]");
		
		printi("CoordCaches: [");
		for(CoordCache c: consensusSNP.getCoordCaches()) {
			c.Accept(this);
		}
		printu("]");
		
		printi("ConsensusSNPAlleles: [");
		for(ConsensusSNPAllele c: consensusSNP.getConsensusSNPAlleles()) {
			c.Accept(this);
		}
		printu("]");
		
		printu("}");
	}
	

	@Override
	public void Visit(CoordCache coordCache) {
		printi("CoordCache: {");
		printiu("Key: " + coordCache.getKey());
		printiu("Chromosome: " + coordCache.getChromosome());
		printiu("seqNum: " + coordCache.getSeqNum());
		printiu("Startcoordinate: " + (int)coordCache.getStartcoordinate());
		printiu("IsMultiCoord: " + coordCache.getIsMultiCoord());
		printiu("Strand: " + coordCache.getStrand());
		printi("Variation Class: {");
		coordCache.getVocTerm().Accept(this);
		printu("}");
		printiu("AlleleSummary: " + coordCache.getAlleleSummary());
		printiu("Iupaccode: " + coordCache.getIupaccode());
		printi("Consensus Markers [");
		for(ConsensusMarker cm: coordCache.getConsensusMarker()) {
			cm.Accept(this);
		}
		printu("]");
		printu("}");
	}
	
	@Override
	public void Visit(SubSnp subSnp) {
		printi("SubSNP: {");
		printiu("Key: " + subSnp.getKey());
		printiu("ConsensusKey: " + subSnp.getConsensusKey());
		printi("SubSNPAccessionObject: {");
		subSnp.getSubSNPAccessionObject().Accept(this);
		printu("}");
		
		printi("SubmitterSNPId: {");
		subSnp.getSubmitterSNPId().Accept(this);
		printu("}");

		printi("Variation Class: {");
		subSnp.getVocTerm().Accept(this);
		printu("}");
		
		printiu("Orientation: " + subSnp.getOrientation());
		printiu("Isexemplar: " + subSnp.getIsexemplar());
		printiu("AlleleSummary: " + subSnp.getAlleleSummary());

		printi("Populations: [");
		for(Population p: subSnp.getPopulationList()) {
			p.Accept(this);
		}
		printu("]");
		printu("}");
		
	}

	@Override
	public void Visit(SubSNPStrainAllele subSNPStrainAllele) {
		printi("SubSNPStrainAllele: {");
		printiu("Allele: " + subSNPStrainAllele.getAllele());
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
		printi("Population: {");
		printiu("Key: " + population.getKey());
		printiu("Subhandle Text: " + population.getSubHandleText());
		printiu("Name: " + population.getName());
		//printi("Subhandle: {");
		//population.getSubHandle().Accept(this);
		//printu("}");
		
		printi("PopulationAccession: {");
		population.getPopulationAccession().Accept(this);
		printu("}");
		
		printi("SubSNPAlleles: [");
		for(SubSNPStrainAllele s: population.getSubSNPStrainAlleles()) {
			s.Accept(this);
		}
		printu("]");
		
		printu("}");
	}

	@Override
	public void Visit(Strain strain) {
		printi("Strain: {");
		//printiu("Key: " + strain.getKey());
		printiu("Strain: " + strain.getStrain());
		printiu("SeqNum: " + strain.getSeqNum());
		printu("}");
	}

	@Override
	public void Visit(VOC_Term voc_Term) {
		printi("VOC_Term: {");
		//printiu("Key: " + voc_Term.getKey());
		printiu("Term: " + voc_Term.getTerm());
		if(voc_Term.getAbbreviation() != null) {
			printiu("Ab: " + voc_Term.getAbbreviation());
		}
		printu("}");
	}

	@Override
	public void Visit(Flank flank) {
		printiu("Flank: { Id: " + flank.getKey() + " flank: " + flank.getFlank() + " Seq: " + flank.getSeqNum() + " is5Prime: " + flank.getIs5Prime() + "}");
	}


	@Override
	public void Visit(Marker marker) {
		printi("Marker: {");
		//printiu("Key: " + marker.getKey());
		printiu("Symbol: " + marker.getSymbol());
		printiu("Name: " + marker.getName());
		if(marker.getMarkerAccession() != null) {
			marker.getMarkerAccession().Accept(this);
		}
		printu("}");
	}

	@Override
	public void Visit(SNPAccessionObject accessionObject) {
		printi("SNPAccessionObject: {");
		printiu("Key: " + accessionObject.getKey());
		printiu("Accid: " + accessionObject.getAccid());
		printiu("ObjectKey: " + accessionObject.getObjectKey());
		printiu("LogicalDBKey: " + accessionObject.getLogicalDBKey());
		printiu("MgiTypeKey: " + accessionObject.getMgiTypeKey());
		printu("}");
	}

	@Override
	public void Visit(MGDAccessionObject accessionObject) {
		printi("MGDAccessionObject: {");
		printiu("Key: " + accessionObject.getKey());
		printiu("Accid: " + accessionObject.getAccid());
		printiu("ObjectKey: " + accessionObject.getObjectKey());
		printiu("LogicalDBKey: " + accessionObject.getLogicalDBKey());
		printiu("MgiTypeKey: " + accessionObject.getMgiTypeKey());
		printu("}");
	}

}
