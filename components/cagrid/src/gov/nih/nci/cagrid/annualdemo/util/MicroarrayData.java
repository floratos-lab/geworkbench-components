/*
 * Created on Jan 27, 2007
 */
package gov.nih.nci.cagrid.annualdemo.util;

import java.util.ArrayList;

public class MicroarrayData
{
	public ArrayList<double[]> data = new ArrayList<double[]>();
	public ArrayList<String> arrayNames = new ArrayList<String>();
	public ArrayList<String> geneNames = new ArrayList<String>();
	
	public void invert()
	{
		double[][] inverted = new double[data.get(0).length][data.size()];
		for (int i = 0; i < inverted.length; i++) {
			for (int j = 0; j < inverted[i].length; j++) {
				inverted[i][j] = data.get(j)[i];
			}
		}
		data = new ArrayList<double[]>(inverted.length);
		for (double[] d : inverted) data.add(d);
		
	}

}
