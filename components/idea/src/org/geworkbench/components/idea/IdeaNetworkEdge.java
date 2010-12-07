package org.geworkbench.components.idea;

import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge.InteractionType;

/**
 * Data structure to hold the network edge information from parameter panel. 
 * 
 * @author zji
 * @version $Id$
 */
public class IdeaNetworkEdge {
	final int geneId1;
	final int geneId2;
	final InteractionType interactionType;
	
	IdeaNetworkEdge(String line) {
		String[] tokens = line.split("\\s");
		geneId1 = Integer.parseInt(tokens[0]);
		geneId2 = Integer.parseInt(tokens[1]);
		interactionType = stringToInteractionType(tokens[3]);
	}

	private static InteractionType stringToInteractionType(String str) {
		int ppiId = Integer.parseInt(str);

		InteractionType interactionType = null;
		if (ppiId == 0)
			interactionType = InteractionType.PROTEIN_DNA;
		else if (ppiId == 1)
			interactionType = InteractionType.PROTEIN_PROTEIN;

		return interactionType;
	}
}
