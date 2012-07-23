package org.geworkbench.components.idea;

import java.io.Serializable;

import org.geworkbench.components.idea.IdeaEdge.InteractionType;

/**
 * Data structure to hold the network edge information from parameter panel. 
 * 
 * @author zji
 * @version $Id$
 */
public class IdeaNetworkEdge implements Serializable {
	
	private static final long serialVersionUID = -5741396026577511626L;
	private final int geneId1;
	private final int geneId2;
	private final InteractionType interactionType;
	
	IdeaNetworkEdge(int node1, int node2){
		geneId1=node1;
		geneId2=node2;
		interactionType = InteractionType.PROTEIN_DNA; // default
	}

	IdeaNetworkEdge(int node1, int node2, InteractionType interactionType) {
		geneId1 = node1;
		geneId2 = node2;
		this.interactionType = interactionType;
	}
	
	public static IdeaNetworkEdge parseIdeaNetworkEdge(String line) {
		String[] tokens = line.split("\\s");
		int geneId1 = Integer.parseInt(tokens[0]);
		int geneId2 = Integer.parseInt(tokens[1]);
		IdeaNetworkEdge e = new IdeaNetworkEdge(geneId1, geneId2,
				stringToInteractionType(tokens[3]));
		return e;
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
	
	public int getGene1(){
		return geneId1;
	}
	
	public int getGene2(){
		return geneId2;
	}

	@Override
	public boolean equals(Object obj) {
		if(! (obj instanceof IdeaNetworkEdge) ) {
			return false;
		}
		IdeaNetworkEdge edge = (IdeaNetworkEdge)obj;
		if(geneId1==edge.geneId1 && geneId2==edge.geneId2) {
			return true;
		} else if(geneId1==edge.geneId2 && geneId2==edge.geneId1) {
			return true;
		} else {
			return false;
		}
		
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + geneId1;		
		return hash;
	}

	public IdeaEdge.InteractionType getInteractionType() {
		return interactionType;
	}
}
