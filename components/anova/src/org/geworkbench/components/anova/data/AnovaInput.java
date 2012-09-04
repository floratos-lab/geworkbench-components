package org.geworkbench.components.anova.data;

public class AnovaInput {

	private float[][] A;
	private int[] groupAssignments;
	private int numGenes;
	private int numSelectedGroups;
	private double pvalueth;
	private int pValueEstimation;
	private int permutationsNumber;
	private int falseDiscoveryRateControl;
	private float falseSignificantGenesLimit; 
	
	public AnovaInput(){};
	public AnovaInput(float[][] A, int[] groupAssignments, int numGenes,
			int numSelectedGroups, double pvalueth, int pValueEstimation,
			int permutationsNumber, int falseDiscoveryRateControl,
			float falseSignificantGenesLimit) {
		this.A = A;		 
		this.groupAssignments = groupAssignments;
		this.numGenes = numGenes;
		this.numSelectedGroups = numSelectedGroups;
		this.pvalueth = pvalueth;
		this.pValueEstimation = pValueEstimation;
		this.permutationsNumber = permutationsNumber;
		this.falseDiscoveryRateControl = falseDiscoveryRateControl;
		this.falseSignificantGenesLimit = falseSignificantGenesLimit;
	}
	
	public void setA(float[][] A)
	{
		this.A = A;
	}
	public float[][] getA()
	{
		return this.A;
	}
	
	public void setGroupAssignments(int[] groupAssignments)
	{
		this.groupAssignments = groupAssignments;
	}
	public int[] getGroupAssignments()
	{
		return this.groupAssignments;
	}
	
	public void setNumGenes(int numGenes)
	{
		this.numGenes = numGenes;
	}
	public int getNumGenes()
	{
		return this.numGenes;
	}
	
	public void setNumSelectedGroups(int numSelectedGroups)
	{
		this.numSelectedGroups = numSelectedGroups;
	}
	public int getNumSelectedGroups()
	{
		return this.numSelectedGroups;
	}
	
	public void setPvalueth(double pvalueth)
	{
		this.pvalueth = pvalueth;
	}
	public double getPvalueth()
	{
		return this.pvalueth;
	}
	
	public void setPValueEstimation(int pValueEstimation)
	{
		this.pValueEstimation = pValueEstimation;
	}
	public int getPValueEstimation()
	{
		return this.pValueEstimation;
	}
	
	public void setPermutationsNumber(int permutationsNumber)
	{
		this.permutationsNumber = permutationsNumber;
	}
	public int getPermutationsNumber()
	{
		return this.permutationsNumber;
	}
	
	public void setFalseDiscoveryRateControl(int falseDiscoveryRateControl)
	{
		this.falseDiscoveryRateControl = falseDiscoveryRateControl;
	}
	public int getFalseDiscoveryRateControl()
	{
		return this.falseDiscoveryRateControl;
	}
	
	public void setFalseSignificantGenesLimit(float falseSignificantGenesLimit)
	{
		this.falseSignificantGenesLimit = falseSignificantGenesLimit;
	}
	public float getFalseSignificantGenesLimit()
	{
		return this.falseSignificantGenesLimit;
	}
	
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Anova Input\n");
		sb.append("A:").append("\n");
		sb.append("{");
		for (int i=0; i<A.length; i++)
		{	sb.append("{");
			for(int j=0; j<A[i].length; j++)
			{	if (j == A[i].length - 1)
				sb.append(A[i][j] + "f").append("}");
				else
					sb.append(A[i][j] + "f").append(",");
		    }
			if (i == A.length-1)
				sb.append("}");
			else
				sb.append(",\n");
		}
			 
		sb.append("\n");		 
		sb.append("groupAssignments:").append(" \n");
		sb.append("{");
		for(int i=0; i<groupAssignments.length; i++)
		{	if (i== groupAssignments.length - 1)
			sb.append(groupAssignments[i]).append("}");
			else
				sb.append(groupAssignments[i]).append(",");
	    }
		sb.append("\n");
		sb.append("numGenes:" + numGenes);
		sb.append("\n");
		sb.append("numSelectedGroups:" + numSelectedGroups);
		sb.append("\n");
		sb.append("pvalueth:" + pvalueth);
		sb.append("\n");
		sb.append("pValueEstimation:" + pValueEstimation);
		sb.append("\n");
		sb.append("permutationsNumber:" + permutationsNumber);
		sb.append("\n");
		sb.append("falseDiscoveryRateControl:" + falseDiscoveryRateControl);
		sb.append("\n");
		sb.append("falseSignificantGenesLimit:" + falseSignificantGenesLimit);
		sb.append("\n");
		return sb.toString();
	}
	
	
}
