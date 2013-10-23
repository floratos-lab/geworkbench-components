package org.geworkbench.components.hierarchicalclustering;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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
import org.geworkbench.components.hierarchicalclustering.computation.DistanceType;
import org.geworkbench.components.hierarchicalclustering.computation.HNode;
import org.geworkbench.components.hierarchicalclustering.computation.HierarchicalClustering;
import org.geworkbench.components.hierarchicalclustering.computation.Linkage;
import org.geworkbench.components.hierarchicalclustering.computation.DimensionType;
import org.geworkbench.components.hierarchicalclustering.data.HierClusterInput;
import org.geworkbench.util.ProgressBar;
 

/**
 * 
 * @author unattributable
 * @version $Id: FastHierClustAnalysis.java 10951 2013-10-17 19:40:14Z youmi $
 * 
 */
public class FastHierClustAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {

	private static final long serialVersionUID = 4486758109656693283L;

	private static Log log = LogFactory.getLog(FastHierClustAnalysis.class);

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
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> data = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
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

		double[][] matrix = geValues(data);
		 
		String distanceType = null;
		String linkageType = null;
		String dimensionType = null;
		
		switch(method) {
		case 0: linkageType = Linkage.SINGLE.name(); break;
		case 1: linkageType = Linkage.AVERAGE.name(); break;
		case 2: linkageType = Linkage.COMPLETE.name(); break;
		default: log.error("error in linkage type");
		}
		
		switch(metric) {
		case 0: distanceType = DistanceType.EUCLIDEAN.name(); break;
		case 1: distanceType = DistanceType.CORRELATION.name(); break;
		case 2: distanceType = DistanceType.SPEARMANRANK.name(); break;
		default: log.error("error in distance type");
		}
		
		switch(dimension) {
		case 0: dimensionType = DimensionType.MARKER.name(); break;
		case 1: dimensionType = DimensionType.ARRAY.name(); break;
		case 2: dimensionType = DimensionType.BOTH.name(); break;
		default: log.error("error in dimension type");
		}
		
		
		HierClusterInput hierClusterInput = new HierClusterInput(matrix, linkageType, distanceType, dimensionType) ;
		final HierarchicalClustering hierarchicalClustering = new HierarchicalClustering(hierClusterInput);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				pb = org.geworkbench.util.ProgressBar
						.create(org.geworkbench.util.ProgressBar.INDETERMINATE_TYPE);
				pb.addObserver(new CancelObserver(hierarchicalClustering));
				pb.setTitle("Hierarchical Clustering");
				pb.setMessage("Computing ...");
				pb.start();
			}
			
		});

		// one for marker; one for array
		HierCluster[] resultClusters = new HierCluster[2];
		
		try {
			if (dimension == 2) {
				
				HierClusterFactory cluster = new HierClusterFactory.Gene(data.markers());
				resultClusters[0] = convertCluster(cluster, hierarchicalClustering.compute());
						 
				cluster = new HierClusterFactory.Microarray(data.items());
				hierarchicalClustering.setMatrix(getTranspose(matrix));
				resultClusters[1] = convertCluster(cluster,hierarchicalClustering.compute());
						 
			} else if (dimension == 1) {
				HierClusterFactory cluster = new HierClusterFactory.Microarray(data.items());			 
				resultClusters[1] = convertCluster(cluster,hierarchicalClustering.compute());
						 
			} else if (dimension == 0) {
				HierClusterFactory cluster = new HierClusterFactory.Gene(data.markers());
				resultClusters[0] = convertCluster(cluster,hierarchicalClustering.compute());
						 
			}

		} catch (OutOfMemoryError e) {
			log
					.error("OutOfMemoryError: "+e.getMessage()
							+ ". The application is not stable to continue. It is suggested to quit the geWorkbench.");
			// very likely the following dialog will not be able to show up
			JOptionPane.showMessageDialog(null, "Even if you are able to see this message, " +
					"the application is not stable to continue due to Out Of Memory error. It is suggested to quit or kill geWorkbench.");
			return null;
		}

		CSHierClusterDataSet dataSet = new CSHierClusterDataSet(resultClusters, null, false,
				"Hierarchical Clustering", data);
		
		//added by Min You, for generating data set hist
		Set<String> labelSet = new HashSet<String>();

		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager
				.getCurrentContext(maSet);
				 
		int nl = context.getNumberOfLabels();
		 
		for (int i = 0; i < nl; i++) {
			String label = context.getLabel(i);
			DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(label);
			if (panelA.isActive()) {				 
				labelSet.add(label);
			}		 
		}

		StringBuffer groupAndChipsString = new StringBuffer(
				labelSet.size() + " groups analyzed:\n" );
		for (String labelA : labelSet) {// for each groups
			DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(labelA);
			// put group label into history
			groupAndChipsString .append( "\tGroup " ).append( labelA ).append( " (" ).append( panelA.size() )
				.append(" chips)").append(":\n");

			if (panelA.isActive()) {
				int aSize = panelA.size();
				for (int aIndex = 0; aIndex < aSize; aIndex++) {  
					groupAndChipsString .append( "\t\t" ).append( panelA.get(aIndex) ).append( "\n" ); }
			}
		}
				
		// add to Dataset History
		HistoryPanel.addToHistory(dataSet, generateHistoryString(data.markers(), groupAndChipsString));
		if(hierarchicalClustering.cancelled) {
			// we don't need to dispose progress bar explicitly because 'cancel' comes from progress bar
			return null;
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if(pb!=null) {
					pb.dispose();
				}
			}
			
		});
		
		return new AlgorithmExecutionResults(true, "No errors.", dataSet);
	}
	
	 

	private transient ProgressBar pb;

	static private class CancelObserver implements Observer {
		final private HierarchicalClustering hierarchicalClustering;
		
		CancelObserver(final HierarchicalClustering hierarchicalClustering) {
			super();
			this.hierarchicalClustering = hierarchicalClustering;
		}
		
		@Override
		public void update(Observable o, Object arg) {
			hierarchicalClustering.cancelled = true;
		}
		
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


	private static double[][] geValues(final DSMicroarraySetView<DSGeneMarker, DSMicroarray> data) {
		int rows = data.markers().size();
		int cols = data.items().size();
		double[][] array = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			array[i] = data.getRow(i);
		}
		return array;
	}

	private static double[][] getTranspose(final double[][] input) {
		double d[][] = new double[input[0].length][input.length];
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++) {
				d[i][j] = input[j][i];
			}
		}
		return d;
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
	 * @param markers 
	 * @param groupAndChipsString
	 * @return
	 */
	private String generateHistoryString(DSItemList<DSGeneMarker> markers, StringBuffer groupAndChipsString) {
		StringBuffer histStr = new StringBuffer( aspp.getDataSetHistory() );

		// group names and markers
		histStr .append( groupAndChipsString );
        
		histStr .append( markers.size() ).append(" markers analyzed:\n");
		for (DSGeneMarker marker : markers){
			histStr.append("\t").append(marker.getLabel()).append("\n");
		}
		
		return histStr.toString();
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
	
	/**
     * 
     * @param analysis
     * @param factory
     * @param node
     * @param pb
     * @return
     */
    private HierCluster convertCluster(HierClusterFactory factory, HNode node) {
        if (node.isLeafNode()) {
            return factory.newLeaf(Integer.parseInt(node.getLeafItem()));
        } else {
        	
        	HierCluster left = convertCluster(factory, node.getLeft());
            HierCluster right = convertCluster(factory, node.getRight());
            HierCluster cluster = factory.newCluster();
            cluster.setDepth(Math.max(left.getDepth(), right.getDepth()) + 1);
            cluster.setHeight(node.getHeight());
            cluster.addNode(left, 0);
            cluster.addNode(right, 0);
            return cluster;
        }
    }
	
    
}
