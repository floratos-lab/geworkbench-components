package org.geworkbench.components.idea;

import java.io.Serializable;

import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge.InteractionType;

/**
 * Data structure to hold the network edge information from parameter panel. 
 * 
 * @author zji
 * @version $Id$
 */
public class IdeaNetworkEdge implements Serializable, Comparable<IdeaNetworkEdge>{
	
	private static final long serialVersionUID = -5741396026577511626L;
	final int geneId1;
	final int geneId2;
	final InteractionType interactionType;
	
	IdeaNetworkEdge(String line) {
		String[] tokens = line.split("\\s");
		geneId1 = Integer.parseInt(tokens[0]);
		geneId2 = Integer.parseInt(tokens[1]);
		interactionType = stringToInteractionType(tokens[3]);
	}
	
	IdeaNetworkEdge(int node1, int node2){
		geneId1=node1;
		geneId2=node2;
		interactionType =InteractionType.PROTEIN_DNA;	//defaut setting
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
	public int compareTo(IdeaNetworkEdge o) {
		if(((this.geneId1==o.getGene1())&&(this.geneId2==o.getGene2()))||((this.geneId1==o.getGene2())&&(this.geneId2==o.getGene1()))){
			return 0;
		}
		else return 1;
	}

	
}
