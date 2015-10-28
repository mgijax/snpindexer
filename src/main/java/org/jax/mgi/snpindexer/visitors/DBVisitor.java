package org.jax.mgi.snpindexer.visitors;

import java.util.ArrayList;

import org.jax.mgi.snpdatamodel.AlleleSNP;
import org.jax.mgi.snpdatamodel.ConsensusCoordinateSNP;
import org.jax.mgi.snpdatamodel.ConsensusMarkerSNP;
import org.jax.mgi.snpdatamodel.PopulationSNP;
import org.jax.mgi.snpdatamodel.SubSNP;
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
import org.jax.mgi.snpindexer.util.ConsensusDAO;

public class DBVisitor implements VisitorInterface {

	private ConsensusDAO dao;
	private org.jax.mgi.snpdatamodel.ConsensusSNP solrConsensusSNP;
	private org.jax.mgi.snpdatamodel.ConsensusCoordinateSNP solrConsensusCoordinateSNP;
	private org.jax.mgi.snpdatamodel.SubSNP solrSubSNP;
	private org.jax.mgi.snpdatamodel.AlleleSNP solrAlleleSNP;
	private org.jax.mgi.snpdatamodel.PopulationSNP solrPopulationSNP;
	private org.jax.mgi.snpdatamodel.ConsensusMarkerSNP solrConsensusMarkerSNP;
	
	public DBVisitor(ConsensusDAO dao) {
		this.dao = dao;
		solrConsensusSNP = new org.jax.mgi.snpdatamodel.ConsensusSNP();
	}

	@Override
	public void Visit(ConsensusSNP consensusSNP) {
		consensusSNP.setConsensusAccession(dao.getSNPAccessionObject(consensusSNP.getKey(), 73, 30));
		consensusSNP.getConsensusAccession().Accept(this);
		
		solrConsensusSNP.setAccid(consensusSNP.getConsensusAccession().getAccid());
		solrConsensusSNP.setAlleleSummary(consensusSNP.getAlleleSummary());
		solrConsensusSNP.setBuildCreated(consensusSNP.getBuildCreated());
		solrConsensusSNP.setBuildUpdated(consensusSNP.getBuildUpdated());
		solrConsensusSNP.setIupaccode(consensusSNP.getIupaccode());

		consensusSNP.getVocTerm().Accept(this);
		solrConsensusSNP.setVariationClass(consensusSNP.getVocTerm().getTerm());
		
		for(Flank f: consensusSNP.getFlanks()) {
			f.Accept(this);
		}
		
		// Fix FLanks

		ArrayList<ConsensusCoordinateSNP> consensusCoordinates = new ArrayList<ConsensusCoordinateSNP>();
		for(CoordCache c: consensusSNP.getCoordCaches()) {
			solrConsensusCoordinateSNP = new ConsensusCoordinateSNP();
			c.Accept(this);
			consensusCoordinates.add(solrConsensusCoordinateSNP);
		}
		solrConsensusSNP.setConsensusCoordinates(consensusCoordinates);
		
		ArrayList<SubSNP> subSNPs = new ArrayList<SubSNP>();
		for(SubSnp s: consensusSNP.getSubSnps()) {
			solrSubSNP = new SubSNP();
			s.Accept(this);
			subSNPs.add(solrSubSNP);
		}
		solrConsensusSNP.setSubSNPs(subSNPs);

		ArrayList<AlleleSNP> alleles = new ArrayList<AlleleSNP>();
		for(ConsensusSNPAllele c: consensusSNP.getConsensusSNPAlleles()) {
			solrAlleleSNP = new AlleleSNP();
			c.Accept(this);
			alleles.add(solrAlleleSNP);
		}
		solrConsensusSNP.setAlleles(alleles);
	}
	
	
	@Override
	public void Visit(Population population) {
		population.setPopulationAccession(dao.getSNPAccessionObject(population.getKey(), 76, 33));
		
		solrPopulationSNP.setAccid(population.getPopulationAccession().getAccid());
		solrPopulationSNP.setPopulationName(population.getName());
		solrPopulationSNP.setSubHandleName(population.getSubHandleText());
		
		ArrayList<AlleleSNP> alleles = new ArrayList<AlleleSNP>();
		for(SubSNPStrainAllele s: population.getSubSNPStrainAlleles()) {
			solrAlleleSNP = new AlleleSNP();
			s.Accept(this);
			alleles.add(solrAlleleSNP);
		}
		solrPopulationSNP.setAlleles(alleles);
	}
	

	
	@Override
	public void Visit(CoordCache coordCache) {
		coordCache.getVocTerm().Accept(this);

		solrConsensusCoordinateSNP.setAlleleSummary(coordCache.getAlleleSummary());
		solrConsensusCoordinateSNP.setChromosome(coordCache.getChromosome());
		solrConsensusCoordinateSNP.setMultiCoord(coordCache.getIsMultiCoord() == 1);
		solrConsensusCoordinateSNP.setIupaccode(coordCache.getIupaccode());
		solrConsensusCoordinateSNP.setStartCoordinate(coordCache.getStartcoordinate());
		solrConsensusCoordinateSNP.setStrand(coordCache.getStrand());
		solrConsensusCoordinateSNP.setVariationClass(coordCache.getVocTerm().getTerm());

		ArrayList<ConsensusMarkerSNP> markers = new ArrayList<ConsensusMarkerSNP>();
		for(ConsensusMarker cm: coordCache.getConsensusMarker()) {
			solrConsensusMarkerSNP = new ConsensusMarkerSNP();
			cm.Accept(this);
			markers.add(solrConsensusMarkerSNP);
		}
		solrConsensusCoordinateSNP.setMarkers(markers);
		
	}
	@Override
	public void Visit(SubSnp subSnp) {
		subSnp.setSubmitterSNPId(dao.getSNPAccessionObject(subSnp.getKey(), 75, 31));
		subSnp.setSubSNPAccessionObject(dao.getSNPAccessionObject(subSnp.getKey(), 74, 31));
		
		solrSubSNP.setAccid(subSnp.getSubSNPAccessionObject().getAccid());
		solrSubSNP.setAlleleSummary(subSnp.getAlleleSummary());
		solrSubSNP.setExemplar(subSnp.getIsexemplar() == 1);
		solrSubSNP.setOrientation(subSnp.getOrientation());
		solrSubSNP.setSubmitterId(subSnp.getSubmitterSNPId().getAccid());
		solrSubSNP.setVariationClass(subSnp.getVocTerm().getTerm());
		
		ArrayList<PopulationSNP> populations = new ArrayList<PopulationSNP>();
		for(Population p: subSnp.getPopulationList()) {
			solrPopulationSNP = new PopulationSNP();
			p.setSubSNPStrainAlleles(dao.getSubSNPStrainAlleles(subSnp.getKey(), p.getKey()));
			p.Accept(this);
			populations.add(solrPopulationSNP);
		}
		solrSubSNP.setPopulations(populations);
	}

	@Override
	public void Visit(ConsensusMarker consensusMarker) {
		consensusMarker.setTranscript(dao.getSNPAccessionObject(consensusMarker.getKey(), 27, 32, "NM_"));
		consensusMarker.setProtein(dao.getSNPAccessionObject(consensusMarker.getKey(), 27, 32, "NP_"));
		consensusMarker.getMarker().Accept(this);
		
		solrConsensusMarkerSNP.setAaPosition(consensusMarker.getAaPosition());
		solrConsensusMarkerSNP.setAccid(consensusMarker.getMarker().getMarkerAccession().getAccid());
		solrConsensusMarkerSNP.setContigAllele(consensusMarker.getContigAllele());
		solrConsensusMarkerSNP.setFunctionClass(consensusMarker.getVocTerm().getTerm());
		solrConsensusMarkerSNP.setName(consensusMarker.getMarker().getName());
		if(consensusMarker.getProtein() != null) {
			solrConsensusMarkerSNP.setProtein(consensusMarker.getProtein().getAccid());
		}
		solrConsensusMarkerSNP.setReadingFrame(consensusMarker.getReadingFrame());
		solrConsensusMarkerSNP.setResidue(consensusMarker.getResidue());
		solrConsensusMarkerSNP.setSymbol(consensusMarker.getMarker().getSymbol());
		if(consensusMarker.getTranscript() != null) {
			solrConsensusMarkerSNP.setTranscript(consensusMarker.getTranscript().getAccid());
		}
		
	}
	@Override
	public void Visit(Marker marker) {
		marker.setMarkerAccession(dao.getMGDAccessionObject(marker.getKey(), 1, 2));
	}
	
	
	@Override
	public void Visit(SubSNPStrainAllele subSNPStrainAllele) {
		solrAlleleSNP.setAllele(subSNPStrainAllele.getAllele());
		solrAlleleSNP.setStrain(subSNPStrainAllele.getStrain().getStrain());
	}
	
	@Override
	public void Visit(ConsensusSNPAllele consensusSNPAllele) {
		solrAlleleSNP.setAllele(consensusSNPAllele.getAllele());
		solrAlleleSNP.setStrain(consensusSNPAllele.getStrain().getStrain());
		solrAlleleSNP.setConflict(consensusSNPAllele.getIsconflict() == 1);
	}
	
	@Override
	public void Visit(Strain strain) {}
	@Override
	public void Visit(SNPAccessionObject accessionObject) {}
	@Override
	public void Visit(MGDAccessionObject accessionObject) {}
	@Override
	public void Visit(VOC_Term voc_Term) {}
	@Override
	public void Visit(Flank flank) {}
	
	
	public org.jax.mgi.snpdatamodel.ConsensusSNP getSolrConsensusSNP() {
		return solrConsensusSNP;
	}
}
