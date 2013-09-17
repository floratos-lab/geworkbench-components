package org.geworkbench.components.microarrays;

import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

/**
 * For EVD panel, only calculate T test without any correction.
 * @version $Id$
 */
public class SimpleTTest {

    private static int CASES = 1;
    private static int CONTROLS = 2;
    private static int NEITHER_GROUP = 3;
    private double maxT;
    private double minT;
    private double[] originalTValues;
    private float[][] expMatrix;
    private int numGenes, numExps;
    private int[] groupAssignments;
    private DSItemList<DSGeneMarker> item;

    public SimpleTTest() {
        maxT = -100;
        minT = 100;
    }

    private void reset() {
        expMatrix = null;
        groupAssignments = null;
        numGenes = numExps = 0;
    }

    @SuppressWarnings("unchecked")
	public double[] execute(Object input) {
		reset();
		if (input == null) {
			return null;
		}

		assert input instanceof DSMicroarraySetView;

		DSMicroarraySetView<DSGeneMarker, ? extends DSMicroarray> data = (DSMicroarraySetView<DSGeneMarker, ? extends DSMicroarray>) input;

		DSPanel<DSGeneMarker> genes = new CSPanel<DSGeneMarker>("");

		genes.addAll(data.markers());

		item = data.markers();

		int markers = genes.size();
		int arrays = data.items().size();
		groupAssignments = new int[arrays];

		expMatrix = new float[markers][arrays];
		originalTValues = new double[markers];
		for (int j = 0; j < arrays; j++) {
			DSMicroarray ma = data.get(j);
			for (int i = 0; i < markers; i++) {
				DSGeneMarker marker = genes.get(i);

				expMatrix[i][j] = (float) ma.getMarkerValue(marker).getValue();
			}
		}

		DSMicroarraySet maSet = (DSMicroarraySet) data.getMicroarraySet();

		DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager
				.getInstance().getCurrentContext(maSet);
		for (int i = 0; i < arrays; i++) {
			DSMicroarray ma = data.items().get(i);
			if (ma instanceof DSMicroarray) {
				String label = context.getClassForItem(ma);
				if (label.equals(CSAnnotationContext.CLASS_CASE)) {
					groupAssignments[i] = CASES;
				} else if (label.equals(CSAnnotationContext.CLASS_CONTROL)) {
					groupAssignments[i] = CONTROLS;
				} else {
					groupAssignments[i] = NEITHER_GROUP;
				}
			} else {
				groupAssignments[i] = NEITHER_GROUP;
			}
		}

		numGenes = genes.size();
		numExps = data.items().size();

		for (int i = 0; i < numGenes; i++) {
			double tValue = getTValue(i);

			originalTValues[i] = tValue;
			if (tValue > maxT) {
				maxT = tValue;
			}
			if (tValue < minT) {
				minT = tValue;
			}
		}
		return originalTValues;

	}

    private float getTValue(int gene) {
        float[] geneValues = new float[numExps];
        for (int i = 0; i < numExps; i++) {
            geneValues[i] = expMatrix[gene][i];
        }

        int groupACounter = 0;
        int groupBCounter = 0;

        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == CASES) {
                groupACounter++;
            } else if (groupAssignments[i] == CONTROLS) {
                groupBCounter++;
            }
        }

        float[] groupAValues = new float[groupACounter];
        float[] groupBValues = new float[groupBCounter];

        groupACounter = 0;
        groupBCounter = 0;

        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == CASES) {
                groupAValues[groupACounter] = geneValues[i];
                groupACounter++;
            } else if (groupAssignments[i] == CONTROLS) {
                groupBValues[groupBCounter] = geneValues[i];
                groupBCounter++;
            }
        }

        float tValue = calculateTValue(groupAValues, groupBValues);
        return tValue;
    }

    private float getMean(float[] group) {
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

    private float calculateTValue(float[] groupA, float[] groupB) {
        int kA = groupA.length;
        int kB = groupB.length;
        float meanA = getMean(groupA);
        float meanB = getMean(groupB);
        float varA = getVar(groupA);
        float varB = getVar(groupB);

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

        float tValue = (float) ((meanA - meanB) / Math.sqrt((varA / kA) + (varB / kB)));

        return tValue;
    }

    private float getVar(float[] group) {
        float mean = getMean(group);
        int n = 0;

        float sumSquares = 0;

        for (int i = 0; i < group.length; i++) {
            if (!Float.isNaN(group[i])) {
                sumSquares = (float) (sumSquares + Math.pow((group[i] - mean), 2));
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

    public double getMaxT() {
        return maxT;
    }

    public double getMinT() {
        return minT;
    }

	public DSItemList<DSGeneMarker> getItem() {
        return item;
    }

}
