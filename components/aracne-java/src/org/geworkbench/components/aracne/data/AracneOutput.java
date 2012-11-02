package org.geworkbench.components.aracne.data;

public class AracneOutput {

	private AracneGraphEdge[] graphEdges;
	private String paramterDescription;
	
	public AracneOutput(){};
	
	public AracneOutput(AracneGraphEdge[] graphEdges, String paramterDescription)
	{
		this.graphEdges = graphEdges;		
		this.paramterDescription = paramterDescription;
	}
	
	public AracneGraphEdge[] getGraphEdges() {
		return this.graphEdges;
	}

	public void setGraphEdges(AracneGraphEdge[] graphEdges) {
		this.graphEdges = graphEdges;
	}
	
	public String getParamterDescription() {
		return this.paramterDescription;
	}

	public void setParamterDescription(String paramterDescription) {
		this.paramterDescription = paramterDescription;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Aracne Result\n");
		 
		sb.append("AracneGraphEdges:").append(" \n");
		for(int i=0; i< graphEdges.length; i++)
		{	 
			sb.append(graphEdges[i].getNode1() + " " + graphEdges[i].getNode2() + graphEdges[i].getWeight());
		    sb.append("\n");
		}
		sb.append("\n");
		sb.append("paramterDescription:");
		sb.append("\n");		 
		sb.append(paramterDescription + " ");
	    
		return sb.toString();
	} 
	
	
}
