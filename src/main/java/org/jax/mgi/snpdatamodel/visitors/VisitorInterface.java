package org.jax.mgi.snpdatamodel.visitors;

import org.jax.mgi.snpdatamodel.AlleleSNP;
import org.jax.mgi.snpdatamodel.ConsensusCoordinateSNP;
import org.jax.mgi.snpdatamodel.ConsensusMarkerSNP;
import org.jax.mgi.snpdatamodel.ConsensusSNP;
import org.jax.mgi.snpdatamodel.PopulationSNP;
import org.jax.mgi.snpdatamodel.SubSNP;


public interface VisitorInterface {

	void Visit(AlleleSNP alleleSNP);
	void Visit(SubSNP subSNP);
	void Visit(PopulationSNP populationSNP);
	void Visit(ConsensusSNP consensusSNP);
	void Visit(ConsensusMarkerSNP consensusMarkerSNP);
	void Visit(ConsensusCoordinateSNP consensusCoordinateSNP);
	
}
