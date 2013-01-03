package org.geworkbench.components.ttest;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.components.ttest.data.TTestInput;
import org.geworkbench.components.ttest.data.TTestOutput;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;

/**
 * <p>geWorkbench</p>
 * <p>Description: Modular Application Framework for Gene Expression, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 */

/**
 * Component to perform the T-Test Analysis.
 * <p>
 * <b><u>NOTE:</u></b> The code in this file is based on the T Test analysis
 * algorithm implementation developed by TIGR (The Institute for Genomic
 * Research), in the context of their TMEV project. In particular, we have
 * borrowed and modified for our purposes parts of the source file
 * <code>Ttest.java</code> located in the package:
 * <p>
 * <p>
 * &nbsp;&nbsp;&nbsp; org.tigr.microarray.mev.cluster.algorithm.impl
 * 
 * @author manjunath at genomecenter dot columbia dot edu
 * @version $Id$
 */
public class TtestAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = 1302806024752128407L;
	
	private static final int GROUP_A = 1;
	private static final int GROUP_B = 2;
	private static final int NEITHER_GROUP = 3;

	private static Log log = LogFactory.getLog(TtestAnalysis.class);

	private int numGenes, numExps;
	private double alpha;
	private int significanceMethod;
	private boolean isPermut, useWelchDf;
	private int[] groupAssignments;
	private int numCombs;
	private boolean useAllCombs;

	private boolean isLogNormalized = false;

	private Vector<Float> tValuesVector = new Vector<Float>();
	private Vector<Float> pValuesVector = new Vector<Float>();

	private int numberGroupA = 0;
	private int numberGroupB = 0;

	public TtestAnalysis() {
		setDefaultPanel(new TtestAnalysisPanel());
	}

	private void reset() {
		if (tValuesVector != null)
		   tValuesVector.clear();
		else
			tValuesVector = new Vector<Float>();
		if (pValuesVector != null)
		   pValuesVector.clear();
		else
			pValuesVector = new Vector<Float>();

		alpha = 0d;
	}

	public int getAnalysisType() {
		return AbstractAnalysis.TTEST_TYPE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AlgorithmExecutionResults execute(Object input) {

		reset();
		if (input == null || !(input instanceof DSMicroarraySetView)) {
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		}

		DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> data = (DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray>) input;

		numGenes = data.markers().size();
		numExps = data.items().size();

		groupAssignments = new int[numExps];

		DSDataSet<? extends DSBioObject> set = data.getDataSet();

		if (!(set instanceof DSMicroarraySet)) {
			return null;
		}

		DSMicroarraySet maSet = (DSMicroarraySet) set;
		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager
				.getCurrentContext(maSet);

		numberGroupA = 0;
		numberGroupB = 0;
		for (int i = 0; i < numExps; i++) {
			DSMicroarray ma = data.items().get(i);
			String[] labels = context.getLabelsForItem(ma);
			for (String label : labels) {
				if (context.isLabelActive(label)) {
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
			return new AlgorithmExecutionResults(
					false,
					"Please activate at least one set of arrays for \"case\", and one set of arrays for \"control\".",
					null);
		}
		if (numberGroupA == 0) {
			return new AlgorithmExecutionResults(false,
					"Please activate at least one set of arrays for \"case\".",
					null);
		}
		if (numberGroupB == 0) {
			return new AlgorithmExecutionResults(
					false,
					"Please activate at least one set of arrays for \"control\".",
					null);
		}

		double[][] caseArray  = new double[numGenes][numberGroupA];
		double[][] controlArray = new double[numGenes][numberGroupB];

		for (int i = 0; i < numGenes; i++) {

			int caseIndex = 0;
			int controlIndex = 0;
			
			for (int j = 0; j < numExps; j++) {
				int a = groupAssignments[j];
				if(a == GROUP_A) {
					caseArray[i][caseIndex++] = data.getValue(i, j);
				} else if(a == GROUP_B) {
					controlArray[i][controlIndex++] = data.getValue(i, j);
				}
			}
		}

		// ///////////////////////////////////////////////////////

		isLogNormalized = ((TtestAnalysisPanel) aspp).isLogNormalized();

		alpha = ((TtestAnalysisPanel) aspp).getAlpha();
		significanceMethod = ((TtestAnalysisPanel) aspp)
				.getSignificanceMethod();
		isPermut = ((TtestAnalysisPanel) aspp).isPermut();
		useWelchDf = ((TtestAnalysisPanel) aspp).useWelchDf();
		numCombs = ((TtestAnalysisPanel) aspp).getNumCombs();
		useAllCombs = ((TtestAnalysisPanel) aspp).useAllCombs();

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
			if (context.isLabelActive(label)) {
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
			if (context.isLabelActive(label)) {
				controlSet.add(label);
				groupAndChipsString += GenerateGroupAndChipsString(context
						.getItemsWithLabel(label));
			}
		}

		String[] caseLabels = caseSet.toArray(new String[0]);
		String[] controlLabels = controlSet.toArray(new String[0]);
		
		int totalSelectedGroup = caseSet.size() + controlSet.size();

		String histMarkerString = GenerateMarkerString(data);

		groupAndChipsString = totalSelectedGroup + " groups analyzed:\n"
				+ groupAndChipsString;

		int m;
		switch(significanceMethod) {
		case TtestAnalysisPanel.JUST_ALPHA: m = SignificanceMethod.JUST_ALPHA; break;
		case TtestAnalysisPanel.STD_BONFERRONI: m = SignificanceMethod.STD_BONFERRONI; break;
		case TtestAnalysisPanel.ADJ_BONFERRONI: m = SignificanceMethod.ADJ_BONFERRONI; break;
		case TtestAnalysisPanel.MIN_P: m = SignificanceMethod.MIN_P; break;
		case TtestAnalysisPanel.MAX_T: m = SignificanceMethod.MAX_T; break;
		default : log.error("error significance method"); return null;
		}
		TTestInput tTestInput = new TTestInput(numGenes, numberGroupA,
				numberGroupB, caseArray, controlArray, m, alpha, isPermut,
				useWelchDf, useAllCombs, numCombs, isLogNormalized);
		DSSignificanceResultSet<DSGeneMarker> sigSet;

		final TTest tTest = new TTest(tTestInput);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				pbTtest = ProgressBar
						.create(ProgressBar.INDETERMINATE_TYPE);
				pbTtest.addObserver(new CancelObserver(tTest));
				pbTtest.setTitle("T Test Analysis");
				pbTtest.setBounds(new ProgressBar.IncrementModel(0, numGenes, 0,
						numGenes, 1));

				pbTtest.setMessage("Calculating TTest, please wait...");
				pbTtest.start();
			}
			
		});

		try {
			TTestOutput output = tTest.execute();
			if(output==null) return null; // cancelled
			
			sigSet = createDSSignificanceResultSet(data, output, caseLabels, controlLabels);
			
			// add data set history.
			HistoryPanel.addToHistory(sigSet, GenerateHistoryHeader() + groupAndChipsString
					+ histMarkerString);

		} catch (TTestException e) {
			e.printStackTrace();
			return new AlgorithmExecutionResults(
					false,
					"Exception happened in t-test computaiton: "+e,
					null);
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if(pbTtest!=null) {
					pbTtest.dispose();
				}
			}
			
		});
		
		return new AlgorithmExecutionResults(true, "Ttest", sigSet);
	} // end of method calculate

	private transient ProgressBar pbTtest;
	
	static private class CancelObserver implements Observer {
		final private TTest tTest;
		
		CancelObserver(final TTest tTest) {
			super();
			this.tTest = tTest;
		}
		
		@Override
		public void update(Observable o, Object arg) {
			tTest.cancelled = true;
		}
		
	}
	
	private DSSignificanceResultSet<DSGeneMarker> createDSSignificanceResultSet(
			DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> data,
			TTestOutput output, String[] caseLabels, String[] controlLabels) {
		DSSignificanceResultSet<DSGeneMarker> sigSet = new CSTTestResultSet<DSGeneMarker>(
				data.getMicroarraySet(), "T-Test", caseLabels, controlLabels,
				alpha, isLogNormalized

		);
		for (int i = 0; i < output.significanceIndex.length; i++) {
			int index = output.significanceIndex[i];
			DSGeneMarker m = data.markers().get(index);
			sigSet.setSignificance(m, output.pValue[index]);
			sigSet.setTValue(m, output.tValue[index]);
			
			sigSet.setFoldChange(m, output.foldChange[index]);
		}
		
		// TODO why is this not done for pmin and tmax in the previous code?
		DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>(
				"Significant Genes");
		for (Integer index : output.significanceIndex) {
			DSGeneMarker item = data.markers().get(index);
			panelSignificant.add( item, new Float(output.pValue[index]) );
			sigSet.addSigGenToPanel(item);
		}
		publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
					DSGeneMarker.class, panelSignificant,
					SubpanelChangedEvent.NEW));

		sigSet.sortMarkersBySignificance();
		
		return sigSet;
	}

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}
	
	protected String GenerateHistoryHeader() {

		String histStr = "";
		// Header
		histStr += "T Test run with the following parameters:\n";
		histStr += "----------------------------------------\n";

		if (useWelchDf)
			histStr += "Group Variances: Unequal(Welch approximation)" + "\n";
		else
			histStr += "Group Variances: Equal" + "\n";

		histStr += "P-Values Parameters:" + "\n";
		if (isPermut){
			histStr += "\t" + "permutation is selected" + "\n";
			if (useAllCombs)
				histStr += "\t" + "Use all permutations is selected" + "\n";
			else {
				histStr += "\t" + "Randomly group experiments is selected" + "\n";
				histStr += "\t" + "#times: " + numCombs + "\n";
			}
		}
		else
			histStr += "\t" + "t-distribution is selected" + "\n";
		
		histStr += "\t" + "critical p-Value: " + alpha + "\n";

		if (isLogNormalized == true)
			histStr += "\t" + "isLogNormalized: true \n";
		else
			histStr += "\t" + "isLogNormalized: false \n";

		if (significanceMethod == TtestAnalysisPanel.JUST_ALPHA)
			histStr += "Alpha Corrections: Just alpha(no correction)" + "\n";
		else if (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI)
			histStr += "Alpha Corrections: Standard Bonferroni" + "\n";
		else if (significanceMethod == TtestAnalysisPanel.ADJ_BONFERRONI)
			histStr += "Alpha Corrections: Adjusted Bonferroni" + "\n";
		else if (significanceMethod == TtestAnalysisPanel.MIN_P)
			histStr += "Alpha Corrections: minP" + "\n";
		else if (significanceMethod == TtestAnalysisPanel.MAX_T)
			histStr += "Alpha Corrections: maxT" + "\n";

		return histStr;
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
}
