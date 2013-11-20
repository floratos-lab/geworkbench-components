package org.geworkbench.components.hierarchicalclustering.data;

public class HierClusterInput {
	 
	private double[][] matrix;
	private String linkageType;
	private String distanceType;
<<<<<<< HEAD
=======
	private String dimensionType;
>>>>>>> 0ba04f36ba02d9238573496aad71bb60bb642103
	
	public HierClusterInput() {
	};

<<<<<<< HEAD
	public HierClusterInput(double[][] values, String linkageType, String distanceType) {		 
		this.matrix = values;
		this.linkageType = linkageType;
		this.distanceType = distanceType;
=======
	public HierClusterInput(double[][] values, String linkageType, String distanceType, String dimensionType) {		 
		this.matrix = values;
		this.linkageType = linkageType;
		this.distanceType = distanceType;
		this.dimensionType = dimensionType;
>>>>>>> 0ba04f36ba02d9238573496aad71bb60bb642103
		 
	}

	public void setMatrix(double[][] matrix) {
		this.matrix = matrix;
	}

	public double[][] getMatrix() {
		return this.matrix;
	}
	
	public void setLinkageType(String linkageType) {
		this.linkageType = linkageType;
	}

	public String getLinkageType() {
		return this.linkageType;
	}

	public void setDistanceType(String distanceType) {
		this.distanceType = distanceType;
	}

	public String getDistanceType() {
		return this.distanceType;
	} 
<<<<<<< HEAD
=======
	
	public void setDimensionType(String dimensionType) {
		this.dimensionType = dimensionType;
	}

	public String getDimensionType() {
		return this.dimensionType;
	} 
>>>>>>> 0ba04f36ba02d9238573496aad71bb60bb642103

	 
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("HierCluster Input\n");
		sb.append("linkageType:" + linkageType).append("\n");
		sb.append("distanceType:" + distanceType).append("\n");
<<<<<<< HEAD
=======
		sb.append("dimensionType:" + dimensionType).append("\n");
>>>>>>> 0ba04f36ba02d9238573496aad71bb60bb642103
		 
		for (int i=0; i<matrix.length; i++)
		{	sb.append("{");
			for(int j=0; j<matrix[i].length; j++)
			{	if (j == matrix[i].length - 1)
				sb.append(matrix[i][j] + "f").append("}");
				else
					sb.append(matrix[i][j] + "f").append(",");
		    }
			if (i == matrix.length-1)
				sb.append("}");
			else
				sb.append(",\n");
		}
		
		return sb.toString();
	}
	

}
