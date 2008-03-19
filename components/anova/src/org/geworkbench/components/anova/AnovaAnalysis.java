package org.geworkbench.components.anova;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.anova.gui.AnovaAnalysisPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.impl.OneWayANOVA;
import org.tigr.microarray.mev.cluster.gui.impl.owa.OneWayANOVAInitBox;
import org.tigr.util.FloatMatrix;

import edu.columbia.geworkbench.cagrid.anova.AnovaResult;
import edu.columbia.geworkbench.cagrid.anova.FalseDiscoveryRateControl;
import edu.columbia.geworkbench.cagrid.anova.PValueEstimation;

/**
 * @author yc2480
 * @version $Id: AnovaAnalysis.java,v 1.13 2008-03-19 18:04:26 chiangy Exp $
 */
public class AnovaAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Log log = LogFactory.getLog(this.getClass());
	private final String analysisName = "Anova";
	private int localAnalysisType;

	int[] groupAssignments; // used for MeV's ANOVA algorithm. [3 3 2 2 2 4 4 4
							// 4] means first two microarrays belongs to same
							// group, and microarray 3,4,5 belongs to same
							// group, and last four microarrays belongs to same
							// group.
	double pvalueth = 0.05; // p-value threshold. Fixme: this should get from
							// user input, but we don't have that GUI in use
							// case yet.
	String GroupAndMarkerString; // store text output used in dataset
									// history. Will be refreshed each time
									// execute() been called.

	private AnovaAnalysisPanel anovaAnalysisPanel = new AnovaAnalysisPanel();

	public AnovaAnalysis() {
		localAnalysisType = AbstractAnalysis.TTEST_TYPE;
		setLabel("Anova Analysis");
		setDefaultPanel(anovaAnalysisPanel);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractAnalysis#getAnalysisType()
	 */
	public int getAnalysisType() {
		return localAnalysisType;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		assert (input instanceof DSMicroarraySetView);
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		DSMicroarraySet<DSMicroarray> maSet = view.getMicroarraySet();

/*		
		// testing how to access view
		//DSItemList itemptest = view.getItemPanel();
		//DSItemList itemtest = view.items();
		// above two lines are equal.
		Iterator groups = view.items().iterator(); // microarrays
		while (groups.hasNext()) {
			DSMicroarray temp = (DSMicroarray) groups.next();
			temp.toString();
		}
		//Object test = view.items();
		assert (view.items() instanceof DSPanel);
		DSItemList paneltest = ((DSPanel) view.items()).panels();
		Iterator groups2 = paneltest.iterator(); // groups
		while (groups2.hasNext()) {
			DSPanel temp = (DSPanel) groups2.next();
			System.out.println(temp.toString());
			Iterator groups3 = temp.iterator(); // microarrays in the group
			while (groups3.hasNext()) {
				Object temp2 = groups3.next();
				System.out.println(temp2.toString());
			}
		}
		DSItemList markertest = view.markers();
		// end testing
*/

		if (!isLogNormalized(maSet)) {
			Object[] options = { "Proceed", "Cancel" };
			int n = JOptionPane
					.showOptionDialog(
							anovaAnalysisPanel.getTopLevelAncestor(), // this
																		// make
																		// it
																		// shown
																		// in
																		// the
																		// center
																		// of
																		// our
																		// software
							"The input dataset must be log-transformed; please reenter log-transformed data to run this anlysis.\n\nClick proceed to override and continue the analysis with the input dataset selected.",
							"Log Transformation", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, // do not use a
																// custom Icon
							options, // the titles of buttons
							options[0]); // default button title
			if (n == 1) { // n==1 means canceled
				return null;
			}
		}

		// Get params
		// pvalueth=0.05; //p-value threshold
//		pvalueth = anovaAnalysisPanel.pValueThreshold;
		pvalueth = anovaAnalysisPanel.anovaParameter.getPValueThreshold();
		if ((pvalueth<0)||(pvalueth>1)){
			JOptionPane.showMessageDialog(null,
					"P-Value threshold should be a float number between 0.0 and 1.0.",
					"Please try again.",
					JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		
		Set<String> labelSet = new HashSet<String>();

		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager
				.getCurrentContext(maSet);
		
		//TODO - are these unused? if so, we should remove them
		int numGroups = 0;
		int numLabels = 0;
		int numGenes = 0;
		int numSelectedGroups = 0;
		int numSelectedLabels = 0;
		int numSelectedGenes = 0;

		String[] selectedGroupLabels;
		GroupAndMarkerString = "";

		int nl = context.getNumberOfLabels();
		numGroups = nl;
		for (int i = 0; i < nl; i++) {
			String label = context.getLabel(i);
			DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(label);
			if (panelA.isActive()) {
				numSelectedGroups++;
				labelSet.add(label);
			}
			// log.debug(label);
		}

		numSelectedGroups = labelSet.size();
		if (numSelectedGroups < 3) {
			return new AlgorithmExecutionResults(false,
					"A minimum of 3 array groups must be activated.", null);
		}
		// todo - check that all selected panels have at least two elements
		String[] labels = labelSet.toArray(new String[numSelectedGroups]);
		String[] labels1 = new String[0];
		numGenes = view.markers().size();
		// log.debug("NumGenes:"+numGenes);
		// Create panels and significant result sets to store results
		DSSignificanceResultSet<DSGeneMarker> sigSet = new CSSignificanceResultSet<DSGeneMarker>(
				maSet, "Anova Analysis", labels1, labels, pvalueth);
		CSAnovaResultSet anovaResultSet = new CSAnovaResultSet(view,
				"Anova Analysis Result Set", labels);

		// todo - use a F-test to filter genes prior to finding significant
		// genes with Holm t Test
		// Run tests

		AlgorithmData data = new AlgorithmData();
		
		data.addParam("alpha", String.valueOf(pvalueth));
		
		// FIXME - this is just a quick fix about the "Selection" Panel, should
		// check how many been selected.
		int globleArrayIndex = 0; // use as an index points to all microarrays
									// put in array A
		// int MicroarrayNum=view.getMicroarraySet().size();

		ArrayList markerList=new ArrayList();
		// calculating how many groups selected and arrays inside selected
		// groups
		for (int i = 0; i < numSelectedGroups; i++) {// for each groups
			String labelA = labels[i];
			DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(labelA);
			// put group label into history
			GroupAndMarkerString += labelA + "\n";

			if (panelA.isActive()) {
				int aSize = panelA.size();
				for (int aIndex = 0; aIndex < aSize; aIndex++) { // for each
																	// array in
																	// this
																	// group
					GroupAndMarkerString += "\t" + panelA.get(aIndex) + "\n"; // put
																				// member
																				// of
																				// each
																				// group
																				// into
																				// history
					if (markerList.contains(panelA.get(aIndex)))
						return new AlgorithmExecutionResults(false,
								"Same marker ("+panelA.get(aIndex)+") exists in multiple groups.", null);
					else
						markerList.add(panelA.get(aIndex));
					globleArrayIndex++; // count total arrays in selected
										// groups.
				}
			}
		}
		numSelectedLabels = globleArrayIndex;
		// fill microarray view data into array A, and assign groups
		int[] groupAssignments = new int[globleArrayIndex];
		float[][] A = new float[numGenes][globleArrayIndex];
		globleArrayIndex = 0;
		for (int i = 0; i < numSelectedGroups; i++) {// for each groups
			String labelA = labels[i];
			DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(labelA);
			int aSize = panelA.size();
			for (int aIndex = 0; aIndex < aSize; aIndex++) {// for each array in
															// this group
				for (int k = 0; k < numGenes; k++) {// for each marker
					A[k][globleArrayIndex] = (float) panelA.get(aIndex)
							.getMarkerValue(k).getValue();
					// log.debug(labelA+Integer.toString(i)+","+Integer.toString(k)+"+"+Integer.toString(aIndex));
				}
				groupAssignments[globleArrayIndex] = i + 1;
				globleArrayIndex++;
			}
		}

		// call MeV's interface using their protocols
		FloatMatrix FM = new FloatMatrix(A);

		data.addMatrix("experiment", FM);
		data.addIntArray("group-assignments", groupAssignments);
		data.addParam("numGroups", String.valueOf(numSelectedGroups));

		data
				.addParam(
						"usePerms",
						String
								.valueOf(anovaAnalysisPanel.anovaParameter
										.getPValueEstimation() == PValueEstimation.permutation));
		// log.debug("usePerms:"+String.valueOf(anovaAnalysisPanel.anovaParameter.getPValueEstimation()==PValueEstimation.permutation));

		if (anovaAnalysisPanel.anovaParameter.getPValueEstimation() == PValueEstimation.fdistribution) {

		} else if (anovaAnalysisPanel.anovaParameter.getPValueEstimation() == PValueEstimation.permutation) {
			data.addParam("numPerms", String
					.valueOf(anovaAnalysisPanel.anovaParameter
							.getPermutationsNumber()));
			// log.debug("numPerms:"+String.valueOf(anovaAnalysisPanel.anovaParameter.getPermutationsNumber()));
			if (anovaAnalysisPanel.anovaParameter
					.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.number) {
				data.addParam("falseNum", String.valueOf((new Float(
						anovaAnalysisPanel.anovaParameter
								.getFalseSignificantGenesLimit())).intValue()));
			} else if (anovaAnalysisPanel.anovaParameter
					.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.proportion) {
				data.addParam("falseProp", String
						.valueOf(anovaAnalysisPanel.anovaParameter
								.getFalseSignificantGenesLimit()));
			} else {
				//user didn't select these two (which need to pass extra parameters), so we don't need to do a thing.
			}
		} else {
			System.out
					.println("This shouldn't happen! I don't understand that PValueEstimation");
		}

		// log.debug(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl());
		if (anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.adjbonferroni) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.ADJ_BONFERRONI));
		} else if (anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.bonferroni) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.STD_BONFERRONI));
		} else if (anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.alpha) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.JUST_ALPHA));
		} else if (anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.westfallyoung) {
			log.debug("don't know what to do with WestFallYoung yet.");
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.MAX_T));
		} else if (anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.number) {
			// it seems if it use false discovery control, it disregards false
			// discovery rate
			log.debug("don't know what to do with FDC yet.");
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.FALSE_NUM));
		} else if (anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.proportion) {
			log.debug("don't know what to do with FDC yet.");
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.FALSE_PROP));
		} else {
			System.out
					.println("This shouldn't happen! I don't understand that selection. It should be one of following: Alpha, Boferroni, Adj-Bonferroni, WestfallYoung, FalseNum, FalseProp.");
		}

		AnovaResult anovaResult = new AnovaResult(); // TODO: I use
														// AnovaResult for now,
														// was designed for
														// grid, not local
														// version, I think
														// we'll also need a
														// local version data
														// type.

		OneWayANOVA OWA = new OneWayANOVA();

		ProgressBar pb=null;
        pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
        pb.setTitle("Anova Analysis");
        pb.setMessage("Calculating Anova, please wait...");
        pb.start();
		
		try {
			AlgorithmData result = OWA.execute(data);
			// get p-values in result
			FloatMatrix pFM = result.getMatrix("rawPValues");
			FloatMatrix apFM = result.getMatrix("adjPValues");
			FloatMatrix fFM = result.getMatrix("fValues");
			FloatMatrix mFM = result.getMatrix("geneGroupMeansMatrix");
			FloatMatrix sFM = result.getMatrix("geneGroupSDsMatrix");

			// I need to know how many will pass the threshold to initialize the
			// array
			int significantMarkerIndex = 0;
			for (int i = 0; i < apFM.getRowDimension(); i++) {
				if (apFM.A[i][0] < pvalueth) {
					significantMarkerIndex++;
				}
				;
			}
			;
			int totalSignificantMarkerNum = significantMarkerIndex;
			totalSignificantMarkerNum = result.getCluster("cluster").getNodeList().getNode(0).getFeaturesIndexes().length;
			log.debug("totalSignificantMarkerNum: "+totalSignificantMarkerNum);
			String[] significantMarkerNames = new String[totalSignificantMarkerNum];
			significantMarkerIndex = 0;
			for (int cx=0;cx<totalSignificantMarkerNum;cx++){
				int i = result.getCluster("cluster").getNodeList().getNode(0).getFeaturesIndexes()[cx];
				significantMarkerNames[significantMarkerIndex]=view.markers().get(i).getLabel();
				significantMarkerIndex++;				
			}
/*
			String[] significantMarkerNames = new String[totalSignificantMarkerNum];
			significantMarkerIndex = 0;
			for (int i = 0; i < apFM.getRowDimension(); i++) {
				if (apFM.A[i][0] < pvalueth) {
					significantMarkerNames[significantMarkerIndex] = view
							.markers().get(i).getLabel();
					significantMarkerIndex++;
				}
				;
			}
			;
*/
			// output f-value, p-value, adj-p-value, mean, std
			anovaResult.setSignificantMarkerNameCollection(significantMarkerNames);
			anovaResult.setPValueCollection(new float[totalSignificantMarkerNum]);
			anovaResult.setGroupMeanCollectionForAllMarkers(new float[totalSignificantMarkerNum
					* mFM.getColumnDimension()]);
			anovaResult.setGroupStandardDiviationCollectionForAllMarkers(new float[totalSignificantMarkerNum
					* mFM.getColumnDimension()]);
			anovaResult.setPValueCollection(new float[totalSignificantMarkerNum]);
			anovaResult.setAdjustedPValueCollection(new float[totalSignificantMarkerNum]);
			anovaResult.setFValueCollection(new float[totalSignificantMarkerNum]);
			anovaResult.setGroupNameCollection(labels);
			DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>(
					"Significant Genes");
			// FIXME: only put means&stds where pvalue<pvalueth

			significantMarkerIndex = 0;
			for (int i = 0; i < apFM.getRowDimension(); i++) {
				//check if this marker exist in the significant cluster.
				int[] aList = result.getCluster("cluster").getNodeList().getNode(0).getFeaturesIndexes();
				boolean inTheList=false;
				for (int cx=0;cx<aList.length;cx++){
					if (aList[cx]==i){
						inTheList=true;
					}
				}
				//if this marker exist in the significant cluster, then it's significant.
				if (inTheList){
// following line only valid while not using permutation. If this fix works, following commneted line can be removed.
//				if (apFM.A[i][0] < pvalueth) {
					DSGeneMarker item = view.markers().get(
							view.markers().get(i).getLabel());
					log.debug("SignificantMarker: "+view.markers().get(i).getLabel()+", with apFM: "+apFM.A[i][0]);
					// if you don't want the whole label, you can use
					// getShortName() to only get it's id.
					// log.debug(view.markers().get(i).getShortName());
					panelSignificant.add(item, new Float(apFM.A[i][0]));
					double doubleSignificance=0;
					if (apFM.A[i][0]==(float)pvalueth){//we'll have float and double compare issue in CSSifnificanceResultSet.setSignificance()
						//manually set to pvalueth in double to fix bug 0001239 on Mantis
						doubleSignificance = pvalueth-0.000000001; 
						//minus a number which is less then float can store to let it unequals to pvalue threshold. (so we don't need to change CSSignificanceResultSet.setSignificance() to inclusive.)
					}else{
						doubleSignificance = (double) apFM.A[i][0];
					}
					sigSet.setSignificance(item, doubleSignificance);
					anovaResult.setPValueCollection(significantMarkerIndex, apFM.A[i][0]);
					anovaResult.setAdjustedPValueCollection(significantMarkerIndex,
							apFM.A[i][0]);
					anovaResult.setFValueCollection(significantMarkerIndex, fFM.A[i][0]);
					for (int j = 0; j < mFM.getColumnDimension(); j++) {
						// System.out.print("\tmean:G"+j+":"+new
						// Float(mFM.A[i][j]));
						// System.out.print("±"+new Float(sFM.A[i][j]));
						anovaResult.setGroupMeanCollectionForAllMarkers(j * totalSignificantMarkerNum
								+ significantMarkerIndex, mFM.A[i][j]);
						anovaResult.setGroupStandardDiviationCollectionForAllMarkers(j * totalSignificantMarkerNum
								+ significantMarkerIndex, sFM.A[i][j]);
					}
					significantMarkerIndex++;
					// System.out.print(view.markers().get(i).getLabel()+"\tp-value:
					// "+new Float(pFM.A[i][0])+"\tadj-p-value: "+new
					// Float(apFM.A[i][0])+"\tf-value: "+new
					// Float(fFM.A[i][0]));
				}
				// System.out.print(view.markers().get(i).getLabel()+"\tp-value:
				// "+new Float(pFM.A[i][0])+"\tadj-p-value: "+new
				// Float(apFM.A[i][0])+"\tf-value: "+new Float(fFM.A[i][0]));
				// log.debug();
			}
			publishSubpanelChangedEvent(new SubpanelChangedEvent(
					DSGeneMarker.class, panelSignificant,
					SubpanelChangedEvent.NEW));
			// log.debug(result.toString());
		} catch (AlgorithmException AE) {
			AE.printStackTrace();
		}
        pb.stop();				
		// add to Dataset History
		ProjectPanel.addToHistory(sigSet, generateHistoryString(data));

		// sigSet.sortMarkersBySignificance();
		// AlgorithmExecutionResults results = new
		// AlgorithmExecutionResults(true, "Anova Analysis", sigSet);
		anovaResultSet = new CSAnovaResultSet(view,
				"Anova Analysis Result Set", labels, 
				anovaResult.getSignificantMarkerNameCollection(),
				anovaResult2result2DArray(anovaResult));
		log.debug(anovaResult.getSignificantMarkerNameCollection().length+"Markers added to anovaResultSet.");
		anovaResultSet.getSignificantMarkers().addAll(
				sigSet.getSignificantMarkers());
		log.debug(sigSet.getSignificantMarkers().size()+"Markers added to anovaResultSet.getSignificantMarkers().");
		anovaResultSet.sortMarkersBySignificance();
		// anovaResultSet.setSignificance(item, (double)pFM.A[i][0]);
		// anovaResultSet.sortMarkersBySignificance();

		// add to Dataset History
		ProjectPanel.addToHistory(anovaResultSet, generateHistoryString(data));

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Anova Analysis", anovaResultSet);
		return results;
	}
	
	/**
	 * 
	 * @param anovaResult
	 * @return
	 */
	private double[][] anovaResult2result2DArray(AnovaResult anovaResult) {
		int arrayHeight = anovaResult.getPValueCollection().length;
		int arrayWidth = anovaResult.getGroupNameCollection().length * 2 + 3; // each
																		// group
																		// needs
																		// two
																		// columns,
																		// plus
																		// pval,
																		// adjpval
																		// and
																		// fval.
		log.debug("result2DArray:"+arrayWidth+"*"+arrayHeight);
		double[][] result2DArray = new double[arrayWidth][arrayHeight];
		// fill p-values
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[0][cx] = anovaResult.getPValueCollection()[cx];
		}
		// fill adj-p-values
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[1][cx] = anovaResult.getAdjustedPValueCollection()[cx];
		}
		// fill f-values
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[2][cx] = anovaResult.getFValueCollection()[cx];
		}
		// fill means
		for (int cx = 0; cx < arrayHeight; cx++) {
			for (int cy = 0; cy < anovaResult.getGroupNameCollection().length; cy++) {
				result2DArray[3 + cy * 2][cx] = anovaResult.getGroupMeanCollectionForAllMarkers()[cy
						* arrayHeight + cx];
			}
		}
		// fill stds
		for (int cx = 0; cx < arrayHeight; cx++) {
			for (int cy = 0; cy < anovaResult.getGroupNameCollection().length; cy++) {
				result2DArray[4 + cy * 2][cx] = anovaResult.getGroupStandardDiviationCollectionForAllMarkers()[cy
						* arrayHeight + cx];
			}
		}

		return result2DArray;
	}
	
	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	@SuppressWarnings("unchecked")
	public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent event) {
		return event;
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	private String generateHistoryString(AlgorithmData data) {
		String histStr = "";
		// Header
		histStr += "Generated with ANOVA run with parameters:\n";
		histStr += "----------------------------------------\n";
		// P Value Estimation
		histStr += "P Value estimation: ";
		if (anovaAnalysisPanel.anovaParameter.getPValueEstimation() == PValueEstimation.permutation) {
			histStr += "Permutation\n";
			histStr += "Permutation#: "+ anovaAnalysisPanel.anovaParameter.getPermutationsNumber() +"\n";
		} else {
			histStr += "F-Distribution\n";
		}
		// P Value threshold
		histStr += "P Value threshold: ";
		histStr += anovaAnalysisPanel.anovaParameter.getPValueThreshold() + "\n";

		// Correction type
		histStr += "correction-method: ";
		histStr += anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl().toString()
				+ "\n";
		/*
		 * you can change line above to human readable version below if
		 * (anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.adjbonferroni){
		 * histStr+="ADJ_BONFERRONI"; }else
		 * if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.bonferroni){
		 * histStr+="STD_BONFERRONI"; }else
		 * if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.alpha){
		 * histStr+="JUST_ALPHA"; }else
		 * if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.westfallyoung){
		 * histStr+="WestFallYoung"; }else
		 * if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.number){
		 * histStr+="FALSE_NUM"; }else
		 * if(anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl()==FalseDiscoveryRateControl.proportion){
		 * histStr+="FALSE_PROP"; }else{ log.debug("This shouldn't
		 * happen! I don't understand that selection. It should be one of
		 * following: Alpha, Boferroni, Adj-Bonferroni, WestfallYoung, FalseNum,
		 * FalseProp."); } histStr+="\n"; end of human readable version
		 */

		// group names and markers
		histStr += GroupAndMarkerString;

		return histStr;
	}
	
	/**
	 * 
	 * @param set
	 * @return
	 */
	private boolean isLogNormalized(DSMicroarraySet<DSMicroarray> set) {
		double minValue = Double.POSITIVE_INFINITY;
		double maxValue = Double.NEGATIVE_INFINITY;
		for (DSMicroarray microarray : set) {
			DSMutableMarkerValue[] values = microarray.getMarkerValues();
			double v;
			for (DSMutableMarkerValue value : values) {
				v = value.getValue();
				if (v < minValue) {
					minValue = v;
				}
				if (v > maxValue) {
					maxValue = v;
				}
			}
		}
		return ((maxValue - minValue) < 100); // if the range of the values is
												// small enough, we guess it's
												// lognormalized.
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getAnalysisName()
	 */
	@Override
	public String getAnalysisName() {
		return analysisName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
	 */
	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();
		// put every parameters you need for execute(String encodedInput) in
		// AnovaClient.java
		// microarray data already been added before these parameters in
		// AnalysisPanel.java
		// I'll need to put group information and anovaParameter.
		// parameterMap.put("groups", value);
		parameterMap.put("anovaParameter", anovaAnalysisPanel.anovaParameter);
		return parameterMap;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonReturnType()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Class getBisonReturnType() {
		return CSAnovaResultSet.class;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useMicroarraySetView()
	 */
	@Override
	protected boolean useMicroarraySetView() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useOtherDataSet()
	 */
	@Override
	protected boolean useOtherDataSet() {
		return false;
	}
}
