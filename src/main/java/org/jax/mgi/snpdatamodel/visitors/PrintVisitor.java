package org.jax.mgi.snpdatamodel.visitors;

import org.jax.mgi.snpdatamodel.AlleleSNP;
import org.jax.mgi.snpdatamodel.ConsensusCoordinateSNP;
import org.jax.mgi.snpdatamodel.ConsensusMarkerSNP;
import org.jax.mgi.snpdatamodel.ConsensusSNP;
import org.jax.mgi.snpdatamodel.PopulationSNP;
import org.jax.mgi.snpdatamodel.SubSNP;

public class PrintVisitor extends PrinterUtil implements VisitorInterface {

	@Override
	public void Visit(AlleleSNP alleleSNP) {
		printi("AlleleSNP: {");
		printiu("Allele: " + alleleSNP.getAllele());
		printiu("Strain: " + alleleSNP.getStrain());
		printiu("Conflict: " + alleleSNP.isConflict());
		printu("}");
	}

	@Override
	public void Visit(SubSNP subSNP) {
		printi("SubSNP: {");
		printiu("SubSnpKey: " + subSNP.getSubSnpKey());
		printiu("Accid: " + subSNP.getAccid());
		printiu("AlleleSummary: " + subSNP.getAlleleSummary());
		printiu("Exemplar: " + subSNP.isExemplar());
		printiu("Orientation: " + subSNP.getOrientation());
		printiu("SubmitterId: " + subSNP.getSubmitterId());
		printiu("VariationClass: " + subSNP.getVariationClass());

		if(subSNP.getPopulations() != null) {
			printi("Populations: [");
			for(PopulationSNP p: subSNP.getPopulations()) {
				p.Accept(this);
			}
			printu("]");
		}
		
		printu("}");
	}

	@Override
	public void Visit(ConsensusSNP consensusSNP) {
		printi("ConsensusSNP: {");
		printiu("ConsensusKey: " + consensusSNP.getConsensusKey());
		printiu("Accid: " + consensusSNP.getAccid());
		printiu("Variation Class: " + consensusSNP.getVariationClass());
		printiu("AlleleSummary: " + consensusSNP.getAlleleSummary());
		printiu("Iupaccode: " + consensusSNP.getIupaccode());
		printiu("BuildCreated: " + consensusSNP.getBuildCreated());
		printiu("BuildUpdated: " + consensusSNP.getBuildUpdated());
		
		printiu("Flank 5 Prime: " + consensusSNP.getFlank5Prime());
		printiu("Flank 3 Prime: " + consensusSNP.getFlank3Prime());
		
		
		printi("SubSNPs: [");
		for(SubSNP s: consensusSNP.getSubSNPs()) {
			s.Accept(this);
		}
		printu("]");
		
		if(consensusSNP.getConsensusCoordinates() != null) {
			printi("CoordCaches: [");
			for(ConsensusCoordinateSNP c: consensusSNP.getConsensusCoordinates()) {
				c.Accept(this);
			}
			printu("]");
		}
		
		if(consensusSNP.getAlleles() != null) {
			printi("Alleles: [");
			for(AlleleSNP a: consensusSNP.getAlleles()) {
				a.Accept(this);
			}
			printu("]");
			
			printu("}");
		}
		
	}

	@Override
	public void Visit(PopulationSNP populationSNP) {
		printi("PopulationSNP: {");
		printiu("Accid: " + populationSNP.getAccid());
		printiu("SubHandleName: " + populationSNP.getSubHandleName());
		printiu("PopulationName: " + populationSNP.getPopulationName());
		
		if(populationSNP.getAlleles() != null) {
			printi("Alleles: [");
			for(AlleleSNP a: populationSNP.getAlleles()) {
				a.Accept(this);
			}
			printu("]");
		}
		
		printu("}");
	}

	@Override
	public void Visit(ConsensusMarkerSNP consensusMarkerSNP) {
		printi("ConsensusMarkerSNP: {");
		printiu("Accid: " + consensusMarkerSNP.getAccid());
		printiu("FunctionClass: " + consensusMarkerSNP.getFunctionClass());
		printiu("Name: " + consensusMarkerSNP.getName());
		printiu("Symbol: " + consensusMarkerSNP.getSymbol());
		printiu("Transcript: " + consensusMarkerSNP.getTranscript());
		printiu("Protein: " + consensusMarkerSNP.getProtein());
		printiu("ContigAllele: " + consensusMarkerSNP.getContigAllele());
		printiu("Residue: " + consensusMarkerSNP.getResidue());
		printiu("AaPosition: " + consensusMarkerSNP.getAaPosition());
		printiu("ReadingFrame: " + consensusMarkerSNP.getReadingFrame());
		printu("}");
	}

	@Override
	public void Visit(ConsensusCoordinateSNP consensusCoordinateSNP) {
		printi("ConsensusCoordinateSNP: {");
		printiu("AlleleSummary: " + consensusCoordinateSNP.getAlleleSummary());
		printiu("Chromosome: " + consensusCoordinateSNP.getChromosome());
		printiu("Iupaccode: " + consensusCoordinateSNP.getIupaccode());
		printiu("Strand: " + consensusCoordinateSNP.getStrand());
		printiu("VariationClass: " + consensusCoordinateSNP.getVariationClass());
		printiu("MultiCoord: " + consensusCoordinateSNP.isMultiCoord());
		printiu("StartCoordinate: " + consensusCoordinateSNP.getStartCoordinate());
		
		printi("ConsensusMarkerSNPs: [");
		for(ConsensusMarkerSNP c: consensusCoordinateSNP.getMarkers()) {
			c.Accept(this);
		}
		printu("]");
		
		printu("}");
	}

}
