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
import org.jax.mgi.snpindexer.util.ConsensusDAO;

public class DBVisitor implements VisitorInterface {

	private ConsensusDAO dao;
	
	public DBVisitor(ConsensusDAO dao) {
		this.dao = dao;
	}

	@Override
	public void Visit(ConsensusSNP consensusSNP) {
		consensusSNP.setConsensusAccession(dao.getSNPAccessionObject(consensusSNP.getKey(), 73, 30));
		consensusSNP.getConsensusAccession().Accept(this);
		consensusSNP.getVocTerm().Accept(this);
		for(Flank f: consensusSNP.getFlanks()) {
			f.Accept(this);
		}
		for(CoordCache c: consensusSNP.getCoordCaches()) {
			c.Accept(this);
		}
		for(SubSnp s: consensusSNP.getSubSnps()) {
			s.Accept(this);
		}
		for(ConsensusSNPAllele c: consensusSNP.getConsensusSNPAlleles()) {
			c.Accept(this);
		}
	}
	@Override
	public void Visit(Population population) {
		population.setPopulationAccession(dao.getSNPAccessionObject(population.getKey(), 76, 33));
	}
	@Override
	public void Visit(VOC_Term voc_Term) {
	}
	@Override
	public void Visit(Flank flank) {
	}
	@Override
	public void Visit(CoordCache coordCache) {
		coordCache.getVocTerm().Accept(this);
		for(ConsensusMarker cm: coordCache.getConsensusMarker()) {
			cm.Accept(this);
		}
	}
	@Override
	public void Visit(SubSnp subSnp) {
		subSnp.setSubmitterSNPId(dao.getSNPAccessionObject(subSnp.getKey(), 75, 31));
		subSnp.setSubSNPAccessionObject(dao.getSNPAccessionObject(subSnp.getKey(), 74, 31));
		for(Population p: subSnp.getPopulationList()) {
			p.setSubSNPStrainAlleles(dao.getSubSNPStrainAlleles(subSnp.getKey(), p.getKey()));
			p.Accept(this);
		}
	}
	@Override
	public void Visit(SubSNPStrainAllele subSNPStrainAllele) {
	}
	@Override
	public void Visit(Strain strain) {
	}
	@Override
	public void Visit(ConsensusSNPAllele consensusSNPAllele) {
	}
	@Override
	public void Visit(ConsensusMarker consensusMarker) {
		consensusMarker.setTranscript(dao.getSNPAccessionObject(consensusMarker.getKey(), 27, 32, "NM_"));
		consensusMarker.setProtein(dao.getSNPAccessionObject(consensusMarker.getKey(), 27, 32, "NP_"));
		consensusMarker.getMarker().Accept(this);
	}
	@Override
	public void Visit(Marker marker) {
		marker.setMarkerAccession(dao.getMGDAccessionObject(marker.getKey(), 1, 2));
	}
	
	@Override
	public void Visit(SNPAccessionObject accessionObject) {
	}
	@Override
	public void Visit(MGDAccessionObject accessionObject) {
	}
}
