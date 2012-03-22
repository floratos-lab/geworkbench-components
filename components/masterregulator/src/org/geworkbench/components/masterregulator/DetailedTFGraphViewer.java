package org.geworkbench.components.masterregulator;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import org.apache.commons.math.stat.correlation.SpearmansCorrelation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMasterRagulatorResultSet;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;

public class DetailedTFGraphViewer extends JPanel {
	private static final long serialVersionUID = -9131829017081209680L;

	private Log log = LogFactory.getLog(this.getClass());

	DSGeneMarker tfA;

	private DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet;
	private DSMicroarraySet mSet;
	private int numberOfMarkers;
	/*private HashMap<Integer, DSGeneMarker> Rank2GeneMap;
	private HashMap<DSGeneMarker, Integer> Gene2RankMap;

	double minValue = 0;
	double maxValue = 0;*/

	public void setTFA(DSMasterRagulatorResultSet<DSGeneMarker> mraResultSet,
			DSGeneMarker tfA) {
		this.mraResultSet = mraResultSet;
		this.tfA = tfA;
		if (mraResultSet != null) {
			mSet = mraResultSet.getMicroarraySet();
			numberOfMarkers = mSet.getMarkers().size();
		} else {
			mSet = null;
			numberOfMarkers = 0;
		}
		/*Hashtable<Double, DSGeneMarker> hash = new Hashtable<Double, DSGeneMarker>();
		for (int cx = 0; cx < numberOfMarkers; cx++) {
			DSGeneMarker marker = (DSGeneMarker) mSet.getMarkers().get(cx);
			double value = mraResultSet.getValue(
					marker);
			log.debug("value for " + marker.getShortName() + " is " + value
					+ " (index " + cx + ")");
			hash.put(value, marker);
			if (minValue > value)
				minValue = value;
			if (maxValue < value)
				maxValue = value;
		}
		Double[] keys = (Double[]) hash.keySet().toArray(new Double[0]);
		Arrays.sort(keys);
		// hash map to store rank of genes.
		Rank2GeneMap = new HashMap<Integer, DSGeneMarker>();
		Gene2RankMap = new HashMap<DSGeneMarker, Integer>();
		int cx = 0; // decreasing order
		for (Double key : keys) {
			DSGeneMarker marker = hash.get(key);
			Rank2GeneMap.put(new Integer(cx), marker);
			Gene2RankMap.put(marker, new Integer(cx));
			log.debug(marker.getShortName() + " ranked " + cx);
			cx++;
		}*/
	}

	public class GradientPanel extends JPanel{
		private static final long serialVersionUID = -5220080052150096477L;
		private int numMarkers  = 0;
		private double minValue = 0;
		private double maxValue = 0;
		GradientPanel(){}
		void setParams(int markercnt, double minVal, double maxVal){
			numMarkers = markercnt;
			minValue = minVal;
			maxValue = maxVal;
		}
		public void paint(Graphics g1){
			super.paint(g1);
			Graphics2D g = (Graphics2D)g1;
			int width = this.getWidth();
			int height = this.getHeight();
			if (numMarkers > 0) {
				Color maxColor = calculateColor(numMarkers, minValue, maxValue,
						maxValue);
				
				Color minColor = calculateColor(numMarkers, minValue, maxValue,
					(-maxValue));
				
				g.setPaint(new GradientPaint(0,  0, minColor, width / 2,  height,
						Color.WHITE));
				g.fillRect(0, 0, width / 2, height);
				
				g.setPaint(new GradientPaint(width / 2,  0, Color.WHITE,
						width,  height, maxColor));
				g.fillRect(width / 2,  0, width / 2,  height);
			}
		}
	}

	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D) g1;
		g.setColor(Color.red);
		int width = this.getWidth();
		int height = this.getHeight();

		g.drawRect(0, 0, width, height);
		if ((numberOfMarkers > 0) && (mraResultSet != null)) {	

			// draw the lines for geneMarkers

			//double maxAbsValue = Math.max(Math.abs(minValue), Math.abs(maxValue));

			DSItemList<DSGeneMarker> genesInRegulonList = mraResultSet.getGenesInRegulon(tfA);					 
			if (genesInRegulonList == null) { // if user selected wrong
												// TF, which doesn't have
												// neighbors
				System.out.println("Wrong TF");
				return;//continue;
			} 
			//SC -> center -> ranks copy
			ArrayList<HashMap<Integer, Integer>> lm = new ArrayList<HashMap<Integer, Integer>>();
			lm.add(0, new HashMap<Integer, Integer>()); //SC>=0
			lm.add(1, new HashMap<Integer, Integer>()); //SC<0
			int[] maxcopy = new int[2];
			for (int cx = 0; cx < genesInRegulonList.size(); cx++) {
				DSGeneMarker marker = (DSGeneMarker) genesInRegulonList
						.get(cx);
				//double value = mraResultSet.getValue(marker);	
				int rank = mraResultSet.getRank(marker);
					
				SpearmansCorrelation SC = new SpearmansCorrelation();
				double spearCor = 0.0;
				DSMicroarraySet maSet = mraResultSet
						.getMicroarraySet();
				double[] arrayData1 = maSet.getRow(tfA);
				double[] arrayData2 = maSet.getRow(marker);

				spearCor = SC.correlation(arrayData1, arrayData2);

				int center = 0;
				//center = (int) ((width / 2) * (1 + value / maxAbsValue));
				center = (int) width * rank / numberOfMarkers;

				int arrayindex = spearCor >= 0 ? 0 : 1;
				HashMap<Integer, Integer> hm = lm.get(arrayindex);
				Integer copy = hm.get(center);
				copy = copy==null?1:(copy+1);
				hm.put(center, copy);
				if (maxcopy[arrayindex] < copy) 
					maxcopy[arrayindex] = copy;
			}
			
			for (int i = 0; i < 2; i++){
				HashMap<Integer, Integer> hm = lm.get(i);
				for (int center : hm.keySet()){
					int alpha = 255 * hm.get(center) / maxcopy[i];
					if (i == 0){
						g.setColor(new Color(255, 0, 0, alpha));
						g.drawLine(center, 0, center, height * 1 / 2);
					} else{
						g.setColor(new Color(0, 0, 255, alpha));
						g.drawLine(center, height * 1 / 2, center, height);
					}
				}
			} 
		}
	}

	private Color calculateColor(int numMarkers, double minTValue,
			double maxTValue, double value) {
		int Y = (int) Math.max(Math.abs(minTValue), Math.abs(maxTValue));

		int disToZero = 0;
		Color result = null;
		// disToZero = numOfPositiveTValues - rank;
		if (Y != 0) {
			int colorindex = (int) (255 * value / (Math.abs(Y)));
			if (colorindex < 0) {
				colorindex = Math.abs(colorindex);
				if (colorindex > 255)
					colorindex = 255;
				result = (new Color(255 - colorindex, 255 - colorindex, 255));
			} else if (colorindex <= 255) {
				result = (new Color(255, 255 - colorindex, 255 - colorindex));
			} else { // if (colorindex > 255)
				colorindex = 255;
				result = (new Color(255, 255 - colorindex, 255 - colorindex));
			}
			//
			log.debug("Distance to Zero: " + disToZero);
			log.debug("color index: " + colorindex);
		}
		return result;
	}
}
