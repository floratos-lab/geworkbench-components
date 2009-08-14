package org.geworkbench.components.hierarchicalclustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
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
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.config.PluginRegistry;
import org.geworkbench.util.CorrelationDistance;
import org.geworkbench.util.Distance;
import org.geworkbench.util.EuclideanDistance;
import org.geworkbench.util.FastMatrixModel;
import org.geworkbench.util.SpearmanRankDistance;
 

/**
 * 
 * @author unattributable
 * @version $Id: FastHierClustAnalysis.java,v 1.3 2009-08-14 20:45:54 chiangy Exp $
 * 
 */
class FastHierClustAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {

	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(this.getClass());

	private int localAnalysisType;

	private int dim = 0;

	private DSMicroarraySetView<DSGeneMarker, DSMicroarray> data;
	
	private String groupAndChipsString = null;

	private final String analysisName = "Hierarchical";

	public FastHierClustAnalysis() {
		localAnalysisType = AbstractAnalysis.HIERARCHICAL_CLUSTERING_TYPE;
		String className = this.getClass().getSuperclass().getName();
		String pluginName = PluginRegistry.getNameMap(className);
		setLabel(pluginName);
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
		data = (DSMicroarraySetView) input;
		DSMicroarraySet<DSMicroarray> maSet = data.getMicroarraySet();
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
		CSHierClusterDataSet dataSet = new CSHierClusterDataSet(resultClusters,
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
		ProjectPanel.addToHistory(dataSet, generateHistoryString());
        
		
		return new AlgorithmExecutionResults(true, "No errors.", dataSet);
	}

	/**
	 * TODO remove this - It is not being called, and this class no longer
	 * extends AbstractAnalysis, SLink does. Return a code identifying the type
	 * of the analysis.
	 * 
	 * @return int
	 * @todo Implement this org.geworkbench.analysis.AbstractAnalysis method
	 */
	public int getAnalysisType() {
		return localAnalysisType;
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
		long start = System.currentTimeMillis();

		final HierarchicalClusterAlgorithm[] algo = { new SimpleClustering(HClustering.Linkage.SINGLE),
				new SimpleClustering(HClustering.Linkage.AVERAGE),
				new SimpleClustering(HClustering.Linkage.COMPLETE) };

		final Distance[] distance = { EuclideanDistance.instance,
				CorrelationDistance.instance, SpearmanRankDistance.instance };
		HierClusterFactory[] cluster = {
				new HierClusterFactory.Gene(data.markers()),
				new HierClusterFactory.Microarray(data.items()) };
		/*
		 * 3 matrix models have been experimented, the times for webmatrix.exp
		 * 12600 genes are reported here: MatrixModel.Gene: 7000766
		 * MatrixModelByCopy.Gene: 690922 double[][]: 64844 There are about 10
		 * times speedup for each improvement.
		 */
		// MatrixModel[] matrix = { new MatrixModel.Gene(data),
		// new MatrixModel.Microarray(data) };
		// MatrixModel[] matrix = { new MatrixModelByCopy.Gene(data),
		// new MatrixModelByCopy.Microarray(data) };
		double[][][] matrix = {
				FastMatrixModel.getMatrix(data, FastMatrixModel.Metric.GENE),
				FastMatrixModel.getMatrix(data,
						FastMatrixModel.Metric.MICROARRAY) };

		// Check to make sure we have enough data to support the requested
		// dimension, otherwise fall back to single dimension

		HierCluster result = algo[method].compute(this, matrix[dim],
				cluster[dim], distance[metric]);

		long time = System.currentTimeMillis() - start;
		log.debug("  TIME: " + time + "   ");
		return result;
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
	public Class getBisonReturnType() {
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
	public ParamValidationResults validInputData(DSMicroarraySetView maSetView,
			DSDataSet refMASet) {
		if (maSetView == null)
			return new ParamValidationResults(false, "Invalid input.");
		assert maSetView instanceof DSMicroarraySetView;
		DSMicroarraySet<DSMicroarray> maSet = maSetView.getMicroarraySet();
		int numMAs = maSetView.items().size();
		int numMarkers = maSetView.getUniqueMarkers().size();
		if ((numMAs < 3)&&(numMarkers < 3)) {
			return new ParamValidationResults(false,"Not enough microarrays in the set.  FastHierClustAnalysis requires at least 3 microarrays or at least 3 markers.\n");
		}
		if (containsMissingValues(maSetView))
			return new ParamValidationResults(false,
					"Microarray set contains missing values.\n"
							+ "Remove before proceeding.");

		return new ParamValidationResults(true,"No Error");
	}
    
}
