package org.geworkbench.components.anova.data;

public class AnovaOutput {

	private  double[][] result2DArray;
	private int[] featuresIndexes;
	private double[] significances;	
	
	
	public AnovaOutput(){};
	
	public AnovaOutput(double[][] result2DArray, int[] featuresIndexes, double[] significances)  {
		this.result2DArray = result2DArray;		 
		this.featuresIndexes = featuresIndexes;
		this.significances = significances;
	}
 
	public void setResult2DArray(double[][] result2DArray)
	{
		this.result2DArray = result2DArray;
	}
	public double[][] getResult2DArray()
	{
		return this.result2DArray;
	}
	
	public void setFeaturesIndexes(int[] featuresIndexes)
	{
		this.featuresIndexes = featuresIndexes;
	}
	public int[] getFeaturesIndexes()
	{
		return this.featuresIndexes;
	}
	
	public void setSignificances(double[] significances)
	{
		this.significances = significances;
	}
	public double[] getSignificances()
	{
		return this.significances;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Anova Result\n");
		sb.append("featuresIndexes:").append("\n");
		for (int i=0; i<featuresIndexes.length; i++)
			sb.append(featuresIndexes[i] + " ");
		sb.append("\n");
		sb.append("result2DArray:").append(" \n");
		for(int i=0; i< result2DArray.length; i++)
		{	for (int j=0; j<result2DArray[i].length; j++)
				sb.append(result2DArray[i][j] + " ");
		    sb.append("\n");
		}
		sb.append("\n");
		sb.append("significances:");
		sb.append("\n");
		
		for (int i=0; i<significances.length; i++)
			sb.append(significances[i] + " ");
	    
		return sb.toString();
	}
	
	
}
