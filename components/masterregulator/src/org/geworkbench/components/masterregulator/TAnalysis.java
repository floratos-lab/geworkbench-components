package org.geworkbench.components.masterregulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

public class TAnalysis {
	
	private static final int GROUP_A = 1;
	private static final int GROUP_B = 2;
	private static final int NEITHER_GROUP = 3;

	private static Log log = LogFactory.getLog(TAnalysis.class);

	private float[][] expMatrix;

	private int numGenes, numExps;

	private int[] groupAssignments;

	private int numberGroupA = 0;
	private int numberGroupB = 0;

	static class TAnalysisException extends Exception {

		private static final long serialVersionUID = -3754616005242276495L;

		public TAnalysisException(String string) {
			super(string);
		}
		
	};
	
	@SuppressWarnings("unchecked")
	Map<DSGeneMarker, Double> calculate(Object input) throws TAnalysisException {

		DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> data = (DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray>) input;
		boolean allArrays = !data.useItemPanel();
		log.info("All arrays: " + allArrays);

		numGenes = data.markers().size();
		numExps = data.items().size();

		groupAssignments = new int[numExps];

		DSDataSet<? extends DSBioObject> set = data.getDataSet();

		if (!(set instanceof DSMicroarraySet)) {
			return null;
		}

		DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) set;
		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager
				.getCurrentContext(maSet);

		numberGroupA = 0;
		numberGroupB = 0;
		for (int i = 0; i < numExps; i++) {
			DSMicroarray ma = data.items().get(i);
			String[] labels = context.getLabelsForItem(ma);
			if ((labels.length == 0) && allArrays) {
				groupAssignments[i] = GROUP_B;
				numberGroupB++;
			}
			for (String label : labels) {
				if (context.isLabelActive(label) || allArrays) {
					String v = context.getClassForLabel(label);
					if (v.equals(CSAnnotationContext.CLASS_CASE)) {
						groupAssignments[i] = GROUP_A;
						numberGroupA++;
					} else if (v.equals(CSAnnotationContext.CLASS_CONTROL)) {
						groupAssignments[i] = GROUP_B;
						numberGroupB++;
					} else {
						groupAssignments[i] = NEITHER_GROUP;
					}
				}
			}
		}
		if (numberGroupA == 0 && numberGroupB == 0) {
			throw new TAnalysisException("Please activate at least one set of arrays for \"case\", and one set of arrays for \"control\".");
		}
		if (numberGroupA == 0) {
			throw new TAnalysisException("Please activate at least one set of arrays for \"case\".");
		}
		if (numberGroupB == 0) {
			throw new TAnalysisException("Please activate at least one set of arrays for \"control\".");
		}

		expMatrix = new float[numGenes][numExps];

		for (int i = 0; i < numGenes; i++) {
			for (int j = 0; j < numExps; j++) {
				expMatrix[i][j] = (float) data.getValue(i, j);
			}
		}

		String[][] labels = new String[2][];
		labels[0] = context.getLabelsForClass(CSAnnotationContext.CLASS_CASE);
		labels[1] = context
				.getLabelsForClass(CSAnnotationContext.CLASS_CONTROL);
		HashSet<String> caseSet = new HashSet<String>();
		HashSet<String> controlSet = new HashSet<String>();

		String groupAndChipsString = "";

		// case
		String[] classLabels = labels[0];
		groupAndChipsString += "\t case group(s): \n";
		for (int i = 0; i < classLabels.length; i++) {
			String label = classLabels[i];
			if (context.isLabelActive(label) || !data.useItemPanel()) {
				caseSet.add(label);
				groupAndChipsString += GenerateGroupAndChipsString(context
						.getItemsWithLabel(label));
			}
		}

		// control
		classLabels = labels[1];
		groupAndChipsString += "\t control group(s): \n";
		for (int i = 0; i < classLabels.length; i++) {
			String label = classLabels[i];
			if (context.isLabelActive(label) || !data.useItemPanel()) {
				controlSet.add(label);
				groupAndChipsString += GenerateGroupAndChipsString(context
						.getItemsWithLabel(label));
			}
		}

		int totalSelectedGroup = caseSet.size() + controlSet.size();

		groupAndChipsString = totalSelectedGroup + " groups analyzed:\n"
				+ groupAndChipsString;

		Map<DSGeneMarker, Double> tValues = new HashMap<DSGeneMarker, Double>();
		for (int i = 0; i < numGenes; i++) {
			DSGeneMarker m = data.markers().get(i);
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
			if (groupAssignments[i] == GROUP_A) {
				if (!Float.isNaN(geneValues[i])) {
					numbValidValuesA++;
				}
			} else if (groupAssignments[i] == GROUP_B) {
				if (!Float.isNaN(geneValues[i])) {
					numbValidValuesB++;
				}
			}
		}

		if ((numbValidValuesA < 2) || (numbValidValuesB < 2)) {
			return Float.NaN; // failed case. what to do ? TODO
		}

		getGroupValues(gene);
		return calculateTValue(groupAValues, groupBValues);
	}

	private String GenerateGroupAndChipsString(DSPanel<DSMicroarray> panel) {
		StringBuffer histStr =  new StringBuffer( "\t     " + panel.getLabel() + " (" + panel.size()
				+ " chips)" + ":\n" );

		int aSize = panel.size();
		for (int aIndex = 0; aIndex < aSize; aIndex++)
			histStr.append(  "\t\t" ).append( panel.get(aIndex) ).append( "\n" );

		return histStr.toString();
	}

	String GenerateMarkerString(
			DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> view) {
		StringBuffer histStr = new StringBuffer( view.markers().size()+" markers analyzed:\n" );
		for (DSGeneMarker marker : view.markers()) {
			histStr.append( "\t" ).append( marker.getLabel() ).append( "\n" );
		}

		return histStr.toString();

	}

	private transient float[] groupAValues = null;
	private transient float[] groupBValues = null;;
	private void getGroupValues(int gene) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix[gene][i];
		}

		groupAValues = new float[numberGroupA];
		groupBValues = new float[numberGroupB];

		int groupACounter = 0;
		int groupBCounter = 0;

		for (int i = 0; i < numExps; i++) {
			if (groupAssignments[i] == GROUP_A) {
				groupAValues[groupACounter] = geneValues[i];
				groupACounter++;
			} else if (groupAssignments[i] == GROUP_B) {
				groupBValues[groupBCounter] = geneValues[i];
				groupBCounter++;
			}
		}
	}
}
