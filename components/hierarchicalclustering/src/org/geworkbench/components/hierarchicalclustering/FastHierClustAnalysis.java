package org.geworkbench.components.hierarchicalclustering;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.util.CorrelationDistance;
import org.geworkbench.util.Distance;
import org.geworkbench.util.EuclideanDistance;
import org.geworkbench.util.SpearmanRankDistance;
 

/**
 * 
 * @author unattributable
 * @version $Id$
 * 
 */
public class FastHierClustAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {

	private static final long serialVersionUID = 4486758109656693283L;

	private static Log log = LogFactory.getLog(FastHierClustAnalysis.class);

	private int dim = 0;

	private DSMicroarraySetView<DSGeneMarker, DSMicroarray> data;
	
	private String groupAndChipsString = null;

	private final String analysisName = "Hierarchical";

	public FastHierClustAnalysis() {
		setDefaultPanel(new HierClustPanel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		 
		
		if (input == null)
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		assert input instanceof DSMicroarraySetView;
		data = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		DSMicroarraySet maSet = data.getMicroarraySet();
		int numMAs = data.items().size();
		int numMarkers = data.getUniqueMarkers().size();
		if ((numMAs < 3)&&(numMarkers < 3)) {
			return new AlgorithmExecutionResults(false,"Not enough microarrays in the set.  FastHierClustAnalysis requires at least 3 microarrays or at least 3 markers.\n",null);
		}

		if (containsMissingValues(data))
			return new AlgorithmExecutionResults(false,
					"Microarray set contains missing values.\n"
							+ "Remove before proceeding.", null);

		int method = ((HierClustPanel) aspp).getMethod();
		int dimension = ((HierClustPanel) aspp).getDimension();
		int metric = ((HierClustPanel) aspp).getDistanceMetric();
		HierCluster markerCluster = null;
		HierCluster microarrayCluster = null;
		if (dimension == 2) {
			dim = 0;
			markerCluster = hierarchical(metric, dim, method);
			if (markerCluster == null)
				return null;
			dim = 1;
			microarrayCluster = hierarchical(metric, dim, method);
			if (microarrayCluster == null)
				return null;

		} else if (dimension == 1) {
			dim = 1;
			microarrayCluster = hierarchical(metric, dim, method);
			if (microarrayCluster == null)
				return null;

		} else if (dimension == 0) {
			dim = 0;
			markerCluster = hierarchical(metric, dim, method);
			if (markerCluster == null)
				return null;
		}

		HierCluster[] resultClusters = new HierCluster[2];
		resultClusters[0] = markerCluster;
		resultClusters[1] = microarrayCluster;
		CSHierClusterDataSet dataSet = new CSHierClusterDataSet(resultClusters, null, false,
				"Hierarchical Clustering", data);
		
		//added by Min You, for generating data set hist
		Set<String> labelSet = new HashSet<String>();

		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager
				.getCurrentContext(maSet);
				 
		int numSelectedGroups = 0;		 
		groupAndChipsString = "";
		int nl = context.getNumberOfLabels();
		 
		for (int i = 0; i < nl; i++) {
			String label = context.getLabel(i);
			DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(label);
			if (panelA.isActive()) {				 
				labelSet.add(label);
			}		 
		}

		numSelectedGroups = labelSet.size();	 
		String[] labels = labelSet.toArray(new String[numSelectedGroups]);
		 
		groupAndChipsString += numSelectedGroups + " groups analyzed:\n";
		for (int i = 0; i < numSelectedGroups; i++) {// for each groups
			String labelA = labels[i];
			DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(labelA);
			// put group label into history
			groupAndChipsString += "\tGroup " + labelA + " (" + panelA.size() +" chips)"+":\n";;

			if (panelA.isActive()) {
				int aSize = panelA.size();
				for (int aIndex = 0; aIndex < aSize; aIndex++) {  
					groupAndChipsString += "\t\t" + panelA.get(aIndex) + "\n";  }
			}
		}
				
		// add to Dataset History
		HistoryPanel.addToHistory(dataSet, generateHistoryString());
        
		
		return new AlgorithmExecutionResults(true, "No errors.", dataSet);
	}

	/**
	 * Check if the argument <code>MicroarraySet</code> contains missing
	 * values.
	 * 
	 * @param maSet
	 *            <code>MicroarraySet</code>
	 * @return if missing values are present
	 */
	private boolean containsMissingValues(
			DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> maSet) {
        if (maSet == null)
            return true;
        int markerCount = maSet.markers().size();
        DSItemList<? extends DSGeneMarker> uniqueMarkers = maSet.getUniqueMarkers();
        for (int i = 0; i < maSet.items().size(); ++i) {
            DSMicroarray mArray = maSet.get(i);
            for (int j = 0; j < markerCount; ++j)
            	if (mArray.getMarkerValue(uniqueMarkers.get(j)).isMissing())
                    return true;
        }
        return false;
	}

	private HierCluster hierarchical(int metric, int dim, int method) {
		ClusteringAlgorithm.Linkage linkageType = null;
		switch(method) {
		case 0: linkageType = ClusteringAlgorithm.Linkage.SINGLE; break;
		case 1: linkageType = ClusteringAlgorithm.Linkage.AVERAGE; break;
		case 2: linkageType = ClusteringAlgorithm.Linkage.COMPLETE; break;
		default: log.error("error in linkage type");
		}

		final Distance[] distance = { EuclideanDistance.instance,
				CorrelationDistance.instance, SpearmanRankDistance.instance };
		HierClusterFactory[] cluster = {
				new HierClusterFactory.Gene(data.markers()),
				new HierClusterFactory.Microarray(data.items()) };

		double[][][] matrix = { getMatrixWithMarkerAsRow(),
				getMatrixWithMicroarrayAsRow() };
		
		try {
			HierCluster result = new HierarchicalClustering(linkageType).compute(this, matrix[dim],
					cluster[dim], distance[metric]);
			return result;
		} catch (OutOfMemoryError e) {
			log
					.error("OutOfMemoryError: "+e.getMessage()
							+ ". The application is not stable to continue. It is suggested to quit the geWorkbench.");
			// very likely the following dialog will not be able to show up
			JOptionPane.showMessageDialog(null, "Even if you are able to see this message, " +
					"the application is not stable to continue due to Out Of Memory error. It is suggested to quit or kill geWorkbench.");
			return null;
		}

	}
	
	private double[][] getMatrixWithMarkerAsRow() {
		int rows = data.markers().size();
		int cols = data.items().size();
		double[][] array = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				array[i][j] = data.getValue(i, j);
			}
		}
		return array;
	}

	private double[][] getMatrixWithMicroarrayAsRow() {
		int rows = data.items().size();
		int cols = data.markers().size();
		double[][] array = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				array[i][j] = data.getValue(j, i);
			}
		}
		return array;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getAnalysisName()
	 */
	@Override
	public String getAnalysisName() {
		return analysisName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
	 */
	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {

		String METHOD = "Method";
		String DIM = "Dimension";
		String DISTANCE = "Distance";
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();

		int method = ((HierClustPanel) aspp).getMethod();
		if (method == 0) {
			parameterMap.put(METHOD, "single");
		} else if (method == 1) {
			parameterMap.put(METHOD, "average");
		} else if (method == 2) {
			parameterMap.put(METHOD, "complete");
		} else {
			throw new RuntimeException("Unsupported method " + method);
		}

		int dimension = ((HierClustPanel) aspp).getDimension();
		if (dimension == 0) {
			parameterMap.put(DIM, "marker");
		} else if (dimension == 1) {
			parameterMap.put(DIM, "microarray");
		} else if (dimension == 2) {
			parameterMap.put(DIM, "both");
		} else {
			throw new RuntimeException("Unsupported dimension " + dimension);
		}

		int metric = ((HierClustPanel) aspp).getDistanceMetric();
		if (metric == 0) {
			parameterMap.put(DISTANCE, "euclidean");
		} else if (metric == 1) {
			parameterMap.put(DISTANCE, "pearson");
		} else if (metric == 2) {
			parameterMap.put(DISTANCE, "spearman");
		} else {
			throw new RuntimeException("Unsupported distance metric " + metric);
		}

		return parameterMap;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonReturnType()
	 */
	@Override
	public Class<?> getBisonReturnType() {
		return CSHierClusterDataSet.class;
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
	
	/**
	 * 
	 * @param none
	 * @return
	 */
	private String generateHistoryString() {
		String histStr = "";
		histStr += aspp.getDataSetHistory();

		// group names and markers
		histStr += groupAndChipsString;
        
		histStr += data.markers().size() +" markers analyzed:\n";
		for (DSGeneMarker marker : data.markers()){
			histStr+="\t"+marker.getLabel()+"\n";
		}
		
		return histStr;
	}

	@Override
	public ParamValidationResults validInputData(DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
		if (maSetView == null)
			return new ParamValidationResults(false, "Invalid input.");
		assert maSetView instanceof DSMicroarraySetView;

		int numMAs = maSetView.items().size();
		int numMarkers = maSetView.getUniqueMarkers().size();
		if ((numMAs < 3)&&(numMarkers < 3)) {
			return new ParamValidationResults(false,"Not enough microarrays in the set.  FastHierClustAnalysis requires at least 3 microarrays or at least 3 markers.\n");
		}
		if (containsMissingValues(maSetView))
			return new ParamValidationResults(false,
					"Microarray set contains missing values.\n"
							+ "Remove before proceeding.");

		// warning danger of out of memory error
		final int LARGE_SET_SIZE = 2000;
		String setTooLarge = null;
		if(numMAs>LARGE_SET_SIZE) {
			setTooLarge = "Microarray set size "+numMAs;
		}
		if(numMarkers>LARGE_SET_SIZE) {
			setTooLarge = "Gene Marker set size "+numMarkers;
		}
		if(setTooLarge!=null) {
			int n = JOptionPane.showConfirmDialog(null,
					setTooLarge+" is very large and may cause out-of-memory error.\n Do you want to continue?",
				    "Too large set",
				    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (n != JOptionPane.YES_OPTION) {
				return new ParamValidationResults(false, "You chose to cancel because marker set is too large.");
			}
		}

		return new ParamValidationResults(true,"No Error");
	}
    
}
