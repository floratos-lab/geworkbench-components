package org.geworkbench.components.masterregulator;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

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
	private HashMap<Integer, DSGeneMarker> Rank2GeneMap;
	private HashMap<DSGeneMarker, Integer> Gene2RankMap;

	private double minValue = 0;
	private double maxValue = 0;

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
		Hashtable<Double, DSGeneMarker> hash = new Hashtable<Double, DSGeneMarker>();
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
		}
	}

	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D) g1;
		g.setColor(Color.red);
		int width = this.getWidth();
		int height = this.getHeight();
		int numMarkers = 0;
		if (mraResultSet != null)
			numMarkers = mraResultSet.getMarkerCount();

		g.drawRect(0, 0, width, height);
		if ((numberOfMarkers > 0) && (mraResultSet != null)) {
		 
			Color maxColor = calculateColor(numMarkers, minValue, maxValue,
					maxValue);
			
			Color minColor = calculateColor(numMarkers, minValue, maxValue,
				(-maxValue));
			
			g.setPaint(new GradientPaint(0,  height * 2 / 3, minColor, getWidth() / 2,  height * 1 / 3,
					Color.WHITE));
			g.fillRect(0, height * 2 / 3, getWidth() / 2, height * 1 / 3);
			
			g.setPaint(new GradientPaint(getWidth() / 2,  height * 2 / 3, Color.WHITE,
					getWidth(),  height * 1 / 3, maxColor));
			g.fillRect(getWidth() / 2,  height * 2 / 3, getWidth(),  height * 1 / 3);
			

			// draw the lines for geneMarkers

			double maxAbsValue = Math.max(Math.abs(minValue), Math.abs(maxValue));

			DSItemList<DSGeneMarker> genesInRegulonList = mraResultSet.getGenesInRegulon(tfA);					 
			if (genesInRegulonList == null) { // if user selected wrong
												// TF, which doesn't have
												// neighbors
				System.out.println("Wrong TF");
				return;//continue;
			} 
			for (int cx = 0; cx < genesInRegulonList.size(); cx++) {
				DSGeneMarker marker = (DSGeneMarker) genesInRegulonList
						.get(cx);
				double value = mraResultSet.getValue(marker);	
					
				SpearmansCorrelation SC = new SpearmansCorrelation();
				double spearCor = 0.0;
				DSMicroarraySet maSet = mraResultSet
						.getMicroarraySet();
				double[] arrayData1 = maSet.getRow(tfA);
				double[] arrayData2 = maSet.getRow(marker);

				spearCor = SC.correlation(arrayData1, arrayData2);

				int center = 0;
				center = (int) ((width / 2) * (1 + value / maxAbsValue));
				if (spearCor >= 0) {							
					g.setColor(Color.RED);
					g.drawLine(center, 0, center, height * 1 / 3);
				} else {							 
					g.setColor(Color.BLUE);
					g.drawLine(center, height * 1 / 3, center,
							height * 2 / 3);
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
