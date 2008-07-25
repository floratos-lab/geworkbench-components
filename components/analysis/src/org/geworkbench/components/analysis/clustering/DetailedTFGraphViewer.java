package org.geworkbench.components.analysis.clustering;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.JPanel;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRagulatorResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.regression.SimpleRegression;

public class DetailedTFGraphViewer extends JPanel {
	private Log log = LogFactory.getLog(this.getClass());
	DSMasterRagulatorResultSet mraResultSet;
	DSGeneMarker tfA;
	DSItemList<DSGeneMarker> targetGenes;
	DSSignificanceResultSet<DSGeneMarker> sigSet;
	int numberOfSigMarkers;
	HashMap<Integer, DSGeneMarker> Rank2GeneMap;
	HashMap<DSGeneMarker, Integer> Gene2RankMap;
	double pValue = 1;
	double minTValue = 0;
	double maxTValue = 0;
	int numOfPositiveTValues = 0; //included 0

	public void setTFA(DSMasterRagulatorResultSet mraResultSet, DSGeneMarker tfA) {
		this.mraResultSet = mraResultSet;
		this.tfA = tfA;
		targetGenes = new CSItemList();
		if (mraResultSet != null) {
			sigSet = mraResultSet.getSignificanceResultSet();
			sigSet.sortMarkersBySignificance();
			numberOfSigMarkers = sigSet.getSignificantMarkers().size();
		} else {
			sigSet = null;
			numberOfSigMarkers = 0;
		}
		numOfPositiveTValues = 0;
		Hashtable<Double, DSGeneMarker> hash = new Hashtable();
		for (int cx = 0; cx < numberOfSigMarkers; cx++) {
			DSGeneMarker marker = (DSGeneMarker) sigSet.getSignificantMarkers().get(cx);
			double tValue = mraResultSet.getSignificanceResultSet().getTValue(marker);
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
		int numMarkers = numberOfSigMarkers;
		g.drawRect(0, 0, width, height);
		if ((numberOfSigMarkers > 0) && (mraResultSet != null)) {
			// draw the color sections
			for (int i = 0; i < numMarkers; i++) {
				double tValue = mraResultSet.getSignificanceResultSet().getTValue(Rank2GeneMap.get(new Integer(i)));
				g.setColor(calculateColor(numMarkers, minTValue, maxTValue, i, tValue));
				g.fillRect((int) (i * width / numMarkers), height * 2 / 3,
						(int) (width / numMarkers) + 1, height / 3);
			}

			// draw the lines for geneMarkers
			int sectionWidth = width / numMarkers;
			for (int i = 0; i < numMarkers; i++) {
				int geneNum = i;
				int left = sectionWidth * (geneNum);
				int right = sectionWidth * (geneNum + 1);
				int center = (left + right) / 2;
				DSItemList genesInTargetList = mraResultSet
						.getGenesInTargetList(tfA);
				if (genesInTargetList == null)	//if user selected wrong TF, which doesn't have neighbors 
					continue;
				for (int cx = 0; cx < genesInTargetList.size(); cx++) {
					if (Gene2RankMap.get(genesInTargetList.get(cx)).intValue() == i) {
						if (mraResultSet.getPValueOf(tfA,
								((DSGeneMarker) genesInTargetList.get(cx))) <= pValue) {
							SimpleRegression SR = new SimpleRegression();
							DSMicroarraySet maSet = mraResultSet
									.getMicroarraySet();
							DSGeneMarker marker = (DSGeneMarker) genesInTargetList
									.get(cx);
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
