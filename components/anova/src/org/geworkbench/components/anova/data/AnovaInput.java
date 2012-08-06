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
	
}
