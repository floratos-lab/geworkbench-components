package org.geworkbench.components.microarrays;

import java.util.ArrayList;
import java.util.List;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

/**
 * Histogram of values associated with markers.
 */
class Histogram {

	final private int nbins; // number of bins.
	final private int[] basketValues; // the bin values
	final private List<DSGeneMarker>[] al; // associated gene markers.
	final private double maxValue;
	final private double minValue;

	final private double delta;

	/* initialize size and spaces */
	@SuppressWarnings("unchecked")
	private Histogram(final int size, final double min, final double max) {
		nbins = size;
		al = new ArrayList[nbins + 1];
		for (int i = 0; i < nbins + 1; i++) {
			al[i] = new ArrayList<DSGeneMarker>();
		}
		basketValues = new int[nbins + 1];

		maxValue = max;
		minValue = min;

		for (int i = 0; i < nbins + 1; i++) {
			basketValues[i] = 0;
		}

		delta = (maxValue - minValue) / nbins;
	}

	// this version is for t-test case
	public Histogram(final int size, final double min, final double max,
			final double[] tValues, DSItemList<DSGeneMarker> item) {
		this(size, min, max);

		for (int geneCtr = 0; geneCtr < tValues.length; geneCtr++) {

			double value = tValues[geneCtr];
			if (Double.isNaN(value)) {
				continue;
			}

			DSGeneMarker marker = item.get(geneCtr);
			addOneMarker(marker, value);
		}

	}

	public Histogram(final int size, final DSMicroarray ma,
			final double min, final double max,
			final DSItemList<DSGeneMarker> markers) {
		this(size, min, max);

		if (ma == null)
			return;

		for (DSGeneMarker marker : markers) {

			DSMarkerValue markerValue = ma.getMarkerValue(marker);
			if (markerValue == null)
				continue;

			double value = markerValue.getValue();
			addOneMarker(marker, value);
		}

	}

	private void addOneMarker(DSGeneMarker marker, double value) {
		if (Double.isNaN(value) || value < minValue || value > maxValue) {
			return;
		}

		int column = (int) ((value - minValue) / delta);
		try {
			basketValues[column]++;
			al[column].add(marker);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	public int[] getBasketvalues() {
		return basketValues;
	}

	private int getBinPosition(double cuValue) {
		if (cuValue > maxValue) {
			return nbins + 1;
		}
		if (cuValue < minValue) {
			return 0;
		}

		return (int) Math.round((cuValue - minValue) / delta);
	}

	DSPanel<DSGeneMarker> getPanel(double leftValue, double rightValue) {
		int leftBin = getBinPosition(leftValue);
		int rightBin = getBinPosition(rightValue);

		DSPanel<DSGeneMarker> panel = new CSPanel<DSGeneMarker>(
				"Selected from EVD");

		if (leftBin < 0 || rightBin < 0) {
			return null;
		} else {
			for (int i = leftBin; i < rightBin; i++) {
				for (DSGeneMarker marker : al[i]) {
					panel.add(marker);
				}
			}
		}
		return panel;
	}

	int getGeneNumbers(double leftValue, double rightValue) {
		int leftBin = getBinPosition(leftValue);
		int rightBin = getBinPosition(rightValue);
		int total = 0;

		if (leftBin < 0 || rightBin < 0) {
			return 0;
		} else {
			for (int i = leftBin; i < rightBin; i++) {
				total += basketValues[i];
			}
		}
		return total;

	}
}