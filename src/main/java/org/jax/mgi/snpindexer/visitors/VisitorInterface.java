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

public interface VisitorInterface {

	void Visit(ConsensusSNP consensusSNP);
	void Visit(Population population);
	void Visit(VOC_Term voc_Term);
	void Visit(Flank flank);
	void Visit(CoordCache coordCache);
	void Visit(SubSnp subSnp);
	void Visit(SubSNPStrainAllele subSNPStrainAllele);
	void Visit(Strain strain);
	void Visit(ConsensusSNPAllele consensusSNPAllele);
	void Visit(ConsensusMarker consensusMarker);
	void Visit(Marker marker);
	void Visit(AccessionObject accessionObject);
	
}
