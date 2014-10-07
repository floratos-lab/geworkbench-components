package org.geworkbench.components.hierarchicalclustering.service;

import org.geworkbench.components.hierarchicalclustering.computation.HNode;
import org.geworkbench.components.hierarchicalclustering.computation.HierarchicalClustering;
import org.geworkbench.components.hierarchicalclustering.data.HierClusterInput;
import org.geworkbench.components.hierarchicalclustering.data.HierClusterOutput;

public class HierClusterService {

	public HierClusterOutput execute(HierClusterInput input) {		 
		  			 
			HNode node  = new HierarchicalClustering(input).compute();		 
			HierClusterOutput output = new HierClusterOutput(node);
			
			return output;
		 
	}
}
