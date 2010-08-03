package org.geworkbench.components.ttest;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRagulatorResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;

public class DetailedTFGraphViewer extends JPanel {
	private static final long serialVersionUID = -9131829017081209680L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	DSGeneMarker tfA;
	
	private DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet;
	private DSMicroarraySet<DSMicroarray> mSet;
	private int numberOfMarkers;
	private HashMap<Integer, DSGeneMarker> Rank2GeneMap;
	private HashMap<DSGeneMarker, Integer> Gene2RankMap;
	private double pValue = 1;
	private double minTValue = 0;
	private double maxTValue = 0;
	private int numOfPositiveTValues = 0; //included 0

	public void setTFA(DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet, DSGeneMarker tfA) {
		this.mraResultSet = mraResultSet;
		this.tfA = tfA;
		if (mraResultSet != null) {
			mSet = mraResultSet.getMicroarraySet();
			numberOfMarkers = mSet.getMarkers().size();
		} else {
			mSet = null;
			numberOfMarkers = 0;
		}
		numOfPositiveTValues = 0;
		Hashtable<Double, DSGeneMarker> hash = new Hashtable<Double, DSGeneMarker>();
		for (int cx = 0; cx < numberOfMarkers; cx++) {
			DSGeneMarker marker = (DSGeneMarker) mSet.getMarkers().get(cx);
			double tValue = mraResultSet.getSignificanceResultSet().getTValue(marker);
			log.debug("t-value for "+marker.getShortName()+" is "+tValue+" (index "+cx+")");
			hash.put(tValue,marker);
			if (minTValue > tValue) minTValue = tValue;
			if (maxTValue < tValue) maxTValue = tValue;
			if (tValue>=0) numOfPositiveTValues++;
		}
		Double[] keys = (Double[]) hash.keySet().toArray(new Double[0]);
		Arrays.sort(keys);
		// hash map to store rank of genes.
		Rank2GeneMap = new HashMap<Integer, DSGeneMarker>();
		Gene2RankMap = new HashMap<DSGeneMarker, Integer>();
		int cx = keys.length-1; //decreasing order
		for (Double key : keys) {
			DSGeneMarker marker = hash.get(key);
			Rank2GeneMap.put(new Integer(cx), marker);
			Gene2RankMap.put(marker, new Integer(cx));
			log.debug(marker.getShortName()+" ranked "+cx);
			cx--;
		}
	}

	public void setPValueFilter(double pValue) {
		this.pValue = pValue;
		repaint();
	};

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.red);
		int width = this.getWidth();
		int height = this.getHeight();
		int numMarkers = 0;
		if(mraResultSet!=null)
			numMarkers = mraResultSet.getMarkerCount();

		g.drawRect(0, 0, width, height);
		if ((numberOfMarkers > 0) && (mraResultSet != null)) {
			// draw the color sections
			for (int i = 0; i < numMarkers; i++) {
				double tValue = mraResultSet.getSignificanceResultSet().getTValue(Rank2GeneMap.get(new Integer(i)));
				g.setColor(calculateColor(numMarkers, minTValue, maxTValue, i, tValue));
				g.fillRect((int) (i * width / numMarkers), height * 2 / 3,
						(int) (width / numMarkers) + 1, height / 3);
			}

			// draw the lines for geneMarkers
			for (int i = 0; i < numMarkers; i++) {
				int geneNum = i;
				int center = (int) (width * (geneNum + 0.5) / numMarkers);
				DSItemList<DSGeneMarker> genesInTargetList = mraResultSet
						.getGenesInTargetList(tfA);
				if (genesInTargetList == null){	//if user selected wrong TF, which doesn't have neighbors
					System.out.println("Wrong TF");
					continue;
				}
				for (int cx = 0; cx < genesInTargetList.size(); cx++) {
					DSGeneMarker marker = (DSGeneMarker) genesInTargetList.get(cx);
					Integer rank = Gene2RankMap.get(marker);
					if (rank!=null && rank.intValue() == i) {
						if (mraResultSet.getPValueOf(tfA, marker) <= pValue) {
							SimpleRegression SR = new SimpleRegression();
							DSMicroarraySet<DSMicroarray> maSet = mraResultSet
									.getMicroarraySet();
							double[] arrayData1 = maSet.getRow(tfA);
							double[] arrayData2 = maSet.getRow(marker);
							double[][] arrayData = new double[2][arrayData1.length];
							arrayData[0] = arrayData1;
							arrayData[1] = arrayData2;
							SR.addData(arrayData);
							Color save = g.getColor();
							if (SR.getR() >= 0) {
								g.setColor(Color.BLACK);
							} else {
								g.setColor(Color.ORANGE);
							}
							g.drawLine(center, 0, center, height * 2 / 3);
							g.setColor(save);
						}
					}
				}
			}
		}
	}
	private Color calculateColor(int numMarkers, double minTValue, double maxTValue, int rank, double tValue){
		int Y = (int)Math.max(minTValue,maxTValue);
		log.debug(numMarkers+","+minTValue+","+maxTValue+","+rank+","+tValue);
		int disToZero = 0;
		Color result = null;
		disToZero = numOfPositiveTValues - rank;
		if (Y!=0){
			int colorindex = (int)(255 * (tValue) / (maxTValue-minTValue));
			if (colorindex < 0){
				colorindex = Math.abs(colorindex);
				if (colorindex > 255) colorindex =255;
				result = (new Color(255-colorindex,255-colorindex,255));
			}else if (colorindex <= 255){
				result= (new Color(255, 255 - colorindex,
						255 - colorindex));
			}else{ // if (colorindex > 255)
				colorindex = 255;
				result= (new Color(255, 255 - colorindex,
						255 - colorindex));
			}
			log.debug("Distance to Zero: "+disToZero);
			log.debug("color index: "+colorindex);
		}
		return result;
	}
}
