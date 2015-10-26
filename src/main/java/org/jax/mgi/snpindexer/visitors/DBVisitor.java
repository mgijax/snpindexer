package org.jax.mgi.snpindexer.visitors;

import org.jax.mgi.snpindexer.entities.AccessionObject;
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
		consensusSNP.setConsensusAccession(dao.getAccessionObject(consensusSNP.getKey(), 73, 30));
	}
	@Override
	public void Visit(Population population) {
	}
	@Override
	public void Visit(VOC_Term voc_Term) {
	}
	@Override
	public void Visit(Flank flank) {
	}
	@Override
	public void Visit(CoordCache coordCache) {
	}
	@Override
	public void Visit(SubSnp subSnp) {
		subSnp.setSubmitterSNPId(dao.getAccessionObject(subSnp.getKey(), 75, 30));
		subSnp.setSubSNPAccessionObject(dao.getAccessionObject(subSnp.getKey(), 74, 31));
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
	}
	@Override
	public void Visit(Marker marker) {
	}
	@Override
	public void Visit(AccessionObject accessionObject) {
	}
}
