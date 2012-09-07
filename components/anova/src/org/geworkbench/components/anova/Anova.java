package org.geworkbench.components.anova;
 
import org.geworkbench.components.anova.data.AnovaInput;
import org.geworkbench.components.anova.data.AnovaOutput;
 
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.impl.OneWayANOVA;
import org.tigr.microarray.mev.cluster.gui.impl.owa.OneWayANOVAInitBox;
import org.tigr.util.FloatMatrix;

/**
 * @author my2248
 * @version $Id 
 */
public class Anova   {
	
	final float[][] A;  
	final int[] groupAssignments;
	final int numGenes;
	final int numSelectedGroups;
	final double pvalueth;	
	final int pValueEstimation;
	final int permutationsNumber;
	final int falseDiscoveryRateControl;
	final float falseSignificantGenesLimit;
	
	volatile boolean cancelled;
	
	public OneWayANOVA OWA = new OneWayANOVA();
	
	

	public Anova(AnovaInput input) {
		 this.A = input.getA();
		 this.groupAssignments = input.getGroupAssignments();
		 this.numGenes = input.getNumGenes();
		 this.numSelectedGroups = input.getNumSelectedGroups();
		 this.pvalueth = input.getPvalueth();	
		 this.pValueEstimation = input.getPValueEstimation();
		 this.permutationsNumber = input.getPermutationsNumber();
		 this.falseDiscoveryRateControl = input.getFalseDiscoveryRateControl();
		 this.falseSignificantGenesLimit = input.getFalseSignificantGenesLimit();
	}

	public AnovaOutput execute() throws AnovaException {	 
		 
		cancelled = false;
		AlgorithmData data = new AlgorithmData();
		data.addParam("alpha", String.valueOf(pvalueth));	  

		/* call MeV's interface using their protocols */
		FloatMatrix FM = new FloatMatrix(A);

		data.addMatrix("experiment", FM);
		data.addIntArray("group-assignments", groupAssignments);
		data.addParam("numGroups", String.valueOf(numSelectedGroups));

		data
				.addParam(
						"usePerms",
						String
								.valueOf(pValueEstimation == PValueEstimation.permutation.ordinal()));

		if (pValueEstimation == PValueEstimation.fdistribution.ordinal()) {

		} else if (pValueEstimation == PValueEstimation.permutation.ordinal()) {
			data.addParam("numPerms", String
					.valueOf(permutationsNumber));
			 
			if (falseDiscoveryRateControl == FalseDiscoveryRateControl.number.ordinal()) {
				data.addParam("falseNum", String.valueOf((new Float(
						falseSignificantGenesLimit)).intValue()));
			} else if (falseDiscoveryRateControl == FalseDiscoveryRateControl.proportion.ordinal()) {
				data.addParam("falseProp", String
						.valueOf(falseSignificantGenesLimit));
			} else {
				/*
				 * user didn't select these two (which need to pass extra
				 * parameters), so we don't need to do a thing.
				 */
			}
		} else {
			throw new AnovaException 
					 ("This shouldn't happen! I don't understand that PValueEstimation");
		}

	 
		if (falseDiscoveryRateControl == FalseDiscoveryRateControl.adjbonferroni.ordinal()) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.ADJ_BONFERRONI));
		} else if (falseDiscoveryRateControl == FalseDiscoveryRateControl.bonferroni.ordinal()) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.STD_BONFERRONI));
		} else if (falseDiscoveryRateControl == FalseDiscoveryRateControl.alpha.ordinal()) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.JUST_ALPHA));
		} else if (falseDiscoveryRateControl == FalseDiscoveryRateControl.westfallyoung.ordinal()) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.MAX_T));
		} else if (falseDiscoveryRateControl == FalseDiscoveryRateControl.number.ordinal()) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.FALSE_NUM));
		} else if (falseDiscoveryRateControl == FalseDiscoveryRateControl.proportion.ordinal()) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.FALSE_PROP));
		} else {
			throw new AnovaException 
			 ("This shouldn't happen! I don't understand that selection. It should be one of following: Alpha, Boferroni, Adj-Bonferroni, WestfallYoung, FalseNum, FalseProp.");
		}
 
		try {
			 
			if (cancelled == true)
				return null;
			/* execute the OneWayAnova algorithm */
			AlgorithmData result = OWA.execute(data);
			/* get p-values in result */
			FloatMatrix apFM = result.getMatrix("adjPValues");
			FloatMatrix fFM = result.getMatrix("fValues");
			FloatMatrix mFM = result.getMatrix("geneGroupMeansMatrix");
			FloatMatrix sFM = result.getMatrix("geneGroupSDsMatrix");

			/*
			 * I need to know how many will pass the threshold to initialize the
			 * array
			 */
		 	 
			int[] aList = result.getCluster("cluster").getNodeList()
		      .getNode(0).getFeaturesIndexes();
			int totalSignificantMarkerNum = aList.length;
		   
			/* output f-value, p-value, adj-p-value, mean, std */
			float[] pValueCollection = new float[totalSignificantMarkerNum];
			float[] adjustedPValueCollection = new float[totalSignificantMarkerNum];
			float[] groupMeanCollectionForAllMarkers = new float[totalSignificantMarkerNum
							* mFM.getColumnDimension()];
			float[] groupStandardDiviationCollectionForAllMarkers = new float[totalSignificantMarkerNum
							* mFM.getColumnDimension()];
			float[] fValueCollection = new float[totalSignificantMarkerNum];
			
			double[] doubleSignificances = new double[totalSignificantMarkerNum];
			
			
			for (int i = 0; i < aList.length; i++) {
			 
				    if (cancelled)
				    	return null;
					double doubleSignificance = 0;
					/*
					 * we'll have float and double compare issue in
					 * CSSifnificanceResultSet.setSignificance()
					 */
					if (apFM.A[aList[i]][0] == (float) pvalueth) {
						/*
						 * Manually set to pvalueth in double to fix bug 0001239
						 * on Mantis. Then, minus a number which is less then
						 * float can store to let it unequals to pvalue
						 * threshold. (so we don't need to change
						 * CSSignificanceResultSet.setSignificance() to
						 * inclusive.)
						 */
						doubleSignificance = pvalueth - 0.000000001;
					} else {
						doubleSignificance = (double) apFM.A[aList[i]][0];
					}	
					
					doubleSignificances[i] = doubleSignificance;
					
					pValueCollection[i] = 
							apFM.A[aList[i]][0];
					adjustedPValueCollection[
							i] = apFM.A[aList[i]][0];
					fValueCollection[i] = 
							fFM.A[aList[i]][0];
					for (int j = 0; j < mFM.getColumnDimension(); j++) {
						groupMeanCollectionForAllMarkers[j
								* totalSignificantMarkerNum
								+ i] = mFM.A[aList[i]][j];
						groupStandardDiviationCollectionForAllMarkers[
										j * totalSignificantMarkerNum
												+ i] =
										sFM.A[aList[i]][j];
					}			 
				 
			}
			 
		 
			double[][] result2DArray = anovaResult2result2DArray(pValueCollection, numSelectedGroups,
					adjustedPValueCollection, fValueCollection, groupMeanCollectionForAllMarkers,
					groupStandardDiviationCollectionForAllMarkers);
			
			AnovaOutput output =  null;		
			if (aList.length > 0)
			   output = new AnovaOutput(result2DArray, aList, doubleSignificances);		 
			else
			   output = new AnovaOutput();
			  
			return output;

		} catch (AbortException AE) {
			 
			return null;
					 
		} catch (AlgorithmException AE) {
			 
			 AE.printStackTrace();
			 throw new AnovaException ("Analysis failed for "+AE);
		}

	}
	
 
	

	/**
	 * 
	 * @param anovaResult
	 * @return
	 */
	private double[][] anovaResult2result2DArray(final float[] pValueCollection,
			final int groupNameCollectionLength,
			final float[] adjustedPValueCollection,
			final float[] fValueCollection,
			final float[] groupMeanCollectionForAllMarkers,
			final float[] groupStandardDiviationCollectionForAllMarkers) {
		
		int arrayHeight = pValueCollection.length;

		/*
		 * each group needs two columns, plus pval, adjpval and fval.
		 */
		int arrayWidth = groupNameCollectionLength * 2 + 3; 

		double[][] result2DArray = new double[arrayWidth][arrayHeight];

		/* fill p-values */
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[0][cx] = pValueCollection[cx];
		}

		/* fill adj-p-values */
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[1][cx] = adjustedPValueCollection[cx];
		}
		/* fill f-values */
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[2][cx] = fValueCollection[cx];
		}
		/* fill means */
		for (int cx = 0; cx < arrayHeight; cx++) {
			for (int cy = 0; cy < groupNameCollectionLength; cy++) {
				result2DArray[3 + cy * 2][cx] = groupMeanCollectionForAllMarkers[cy * arrayHeight
						+ cx];
			}
		}
		/* fill stds */
		for (int cx = 0; cx < arrayHeight; cx++) {
			for (int cy = 0; cy < groupNameCollectionLength; cy++) {
				result2DArray[4 + cy * 2][cx] = groupStandardDiviationCollectionForAllMarkers[cy
						* arrayHeight + cx];
			}
		}

		return result2DArray;
	}

	 
}
