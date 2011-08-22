package org.geworkbench.components.masterregulator;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;

/**
 * This class supports the calculation of T-value for master regulation analysis.
 * It is trimmed based on the code of t-test component to the part that is relevant here.
 * @author zji
 *
 */
public class TAnalysis {
	
	private static enum Group {CASE, CONTROL, NEITHER};

	private static Log log = LogFactory.getLog(TAnalysis.class);

	final private float[][] expMatrix;

	final private int numGenes, numExps;

	final private Group[] groupAssignments;

	private int numberOfCase = 0;
	private int numberOfControl = 0;

	static class TAnalysisException extends Exception {

		private static final long serialVersionUID = -3754616005242276495L;

		public TAnalysisException(String string) {
			super(string);
		}
		
	};
	
	private final DSMicroarraySetView<DSGeneMarker, DSMicroarray> datasetView;
	TAnalysis(final DSMicroarraySetView<DSGeneMarker, DSMicroarray> datasetView) throws TAnalysisException {
		this.datasetView = datasetView;
		
		boolean allArrays = !datasetView.useItemPanel();
		log.info("All arrays: " + allArrays);

		numGenes = datasetView.markers().size();
		numExps = datasetView.items().size();

		groupAssignments = new Group[numExps];

		DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) datasetView.getDataSet();
		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager
				.getCurrentContext(maSet);

		numberOfCase = 0;
		numberOfControl = 0;
		for (int i = 0; i < numExps; i++) {
			DSMicroarray ma = datasetView.items().get(i);
			String[] labels = context.getLabelsForItem(ma);
			if ((labels.length == 0) && allArrays) {
				groupAssignments[i] = Group.CONTROL;
				numberOfControl++;
			}
			for (String label : labels) {
				if (context.isLabelActive(label) || allArrays) {
					String v = context.getClassForLabel(label);
					if (v.equals(CSAnnotationContext.CLASS_CASE)) {
						groupAssignments[i] = Group.CASE;
						numberOfCase++;
					} else if (v.equals(CSAnnotationContext.CLASS_CONTROL)) {
						groupAssignments[i] = Group.CONTROL;
						numberOfControl++;
					} else {
						groupAssignments[i] = Group.NEITHER;
					}
				}
			}
		}
		if (numberOfCase == 0 && numberOfControl == 0) {
			throw new TAnalysisException("Please activate at least one set of arrays for \"case\", and one set of arrays for \"control\".");
		}
		if (numberOfCase == 0) {
			throw new TAnalysisException("Please activate at least one set of arrays for \"case\".");
		}
		if (numberOfControl == 0) {
			throw new TAnalysisException("Please activate at least one set of arrays for \"control\".");
		}

		expMatrix = new float[numGenes][numExps];

		for (int i = 0; i < numGenes; i++) {
			for (int j = 0; j < numExps; j++) {
				expMatrix[i][j] = (float) datasetView.getValue(i, j);
			}
		}
		
	}
	
	Map<DSGeneMarker, Double> calculateTValues() {

		Map<DSGeneMarker, Double> tValues = new HashMap<DSGeneMarker, Double>();
		for (int i = 0; i < numGenes; i++) {
			DSGeneMarker m = datasetView.markers().get(i);
			tValues.put(m, new Double(getTValue(i)) );
		}

		return tValues;
	} // end of method calculate

	static private float getMean(float[] group) {
		float sum = 0;
		int n = 0;

		for (int i = 0; i < group.length; i++) {
			if (!Float.isNaN(group[i])) {
				sum = sum + group[i];
				n++;
			}
		}
		if (n == 0) {
			return Float.NaN;
		}
		float mean = sum / (float) n;

		if (Float.isInfinite(mean)) {
			return Float.NaN;
		}
		return mean;
	}

	static private float getVar(float[] group, float mean) {
		int n = 0;

		float sumSquares = 0;

		for (int i = 0; i < group.length; i++) {
			if (!Float.isNaN(group[i])) {
				sumSquares = (float) (sumSquares + Math.pow((group[i] - mean),
						2));
				n++;
			}
		}

		if (n < 2) {
			return Float.NaN;
		}

		float var = sumSquares / (float) (n - 1);
		if (Float.isInfinite(var)) {
			return Float.NaN;
		}
		return var;
	}

	private static float calculateTValue(float[] groupA, float[] groupB) {
		int kA = groupA.length;
		int kB = groupB.length;
		float meanA = getMean(groupA);
		float meanB = getMean(groupB);
		float varA = getVar(groupA, meanA);
		float varB = getVar(groupB, meanB);

		int numbValidGroupAValues = 0;
		int numbValidGroupBValues = 0;

		for (int i = 0; i < groupA.length; i++) {
			if (!Float.isNaN(groupA[i])) {
				numbValidGroupAValues++;
			}
		}

		for (int i = 0; i < groupB.length; i++) {
			if (!Float.isNaN(groupB[i])) {
				numbValidGroupBValues++;
			}
		}

		if ((numbValidGroupAValues < 2) || (numbValidGroupBValues < 2)) {
			return Float.NaN;
		}

		float tValue = (float) ((meanA - meanB) / Math.sqrt((varA / kA)
				+ (varB / kB)));

		return tValue;
	}

	private float getTValue(int gene) {

		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix[gene][i];
		}

		int numbValidValuesA = 0;
		int numbValidValuesB = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == Group.CASE) {
				if (!Float.isNaN(geneValues[i])) {
					numbValidValuesA++;
				}
			} else if (groupAssignments[i] == Group.CONTROL) {
				if (!Float.isNaN(geneValues[i])) {
					numbValidValuesB++;
				}
			}
		}

		if ((numbValidValuesA < 2) || (numbValidValuesB < 2)) {
			return Float.NaN; // failed case. what to do ? TODO
		}

		getGroupValues(gene);
		return calculateTValue(groupCaseValues, groupControlValues);
	}

	private transient float[] groupCaseValues = null;
	private transient float[] groupControlValues = null;;
	private void getGroupValues(int gene) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix[gene][i];
		}

		groupCaseValues = new float[numberOfCase];
		groupControlValues = new float[numberOfControl];

		int caseCounter = 0;
		int controlCounter = 0;

		for (int i = 0; i < numExps; i++) {
			if (groupAssignments[i] == Group.CASE) {
				groupCaseValues[caseCounter] = geneValues[i];
				caseCounter++;
			} else if (groupAssignments[i] == Group.CONTROL) {
				groupControlValues[controlCounter] = geneValues[i];
				controlCounter++;
			}
		}
	}
}
