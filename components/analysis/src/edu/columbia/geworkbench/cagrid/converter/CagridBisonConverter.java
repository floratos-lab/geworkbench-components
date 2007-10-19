package edu.columbia.geworkbench.cagrid.converter;

import java.util.Map;
import java.util.Set;
import java.math.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.properties.DSSequential;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.CSSOMClusterDataSet;
import org.geworkbench.bison.model.clusters.DSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.DSSOMClusterDataSet;
import org.geworkbench.bison.model.clusters.DefaultSOMCluster;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.LeafSOMCluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
import org.geworkbench.bison.model.clusters.SOMCluster;
import org.geworkbench.engine.management.Script;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.EdgeListDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.EdgeList;
import org.geworkbench.util.pathwaydecoder.mutualinformation.Edge;
import org.geworkbench.util.pathwaydecoder.mutualinformation.NetBoostDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.NetBoostData;
import org.ginkgo.labs.converter.BasicConverter;

import edu.columbia.geworkbench.cagrid.aracne.AdjacencyMatrix;
import edu.columbia.geworkbench.cagrid.aracne.AracneParameter;
import edu.columbia.geworkbench.cagrid.cluster.client.HierarchicalClusteringClient;
import edu.columbia.geworkbench.cagrid.cluster.client.SomClusteringClient;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Dim;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Distance;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalCluster;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Method;
import edu.columbia.geworkbench.cagrid.cluster.som.SomCluster;
import edu.columbia.geworkbench.cagrid.cluster.som.SomClusteringParameter;
import edu.columbia.geworkbench.cagrid.microarray.Marker;
import edu.columbia.geworkbench.cagrid.microarray.Microarray;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;
import edu.duke.cabig.rproteomics.model.statml.Array;
import edu.duke.cabig.rproteomics.model.statml.Data;
import edu.duke.cabig.rproteomics.model.statml.Scalar;

/**
 * Converts to/from cagrid microarray set types from/to geworkbench microarray
 * set types.
 * 
 * @author keshav
 * @version $Id: CagridBisonConverter.java,v 1.9 2007-10-19 00:01:04 hungc Exp $
 */
public class CagridBisonConverter {
	private static final String HIERARCHICAL_CLUSTERING_NAME = "Hierarchical Clustering";

	private static final String SOM_CLUSTERING_NAME = "Som Clustering";

	private static final String ARACNE_NAME = "Aracne";

	private static final String MICROARRAY = "Microarray";

	private static final String MARKER = "Marker";

	private static final String BOTH = "Both";

	private static final String SPEARMAN = "Spearman";

	private static final String PEARSON = "Pearson";

	private static final String EUCLIDEAN = "Euclidean";

	private static final String TOTAL = "Total";

	private static final String AVERAGE = "Average";

	private static final String SINGLE = "Single";
	
	private static final String NETBOOST = "NetBoost";

	private static Log log = LogFactory.getLog(CagridBisonConverter.class);

	/**
	 * Convert to edu.columbia.geworkbench.cagrid.microarray.MicroarraySet from
	 * org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView
	 * 
	 * @param microarraySetView
	 * @return MicroarraySet
	 */
	public static MicroarraySet convertFromBisonToCagridMicroarray(
			DSMicroarraySetView microarraySetView) {

		DSMicroarraySet microarraySet = microarraySetView.getMicroarraySet();

		/* extract microarray info from DSMicroarraySet */
		int numArrays = microarraySetView.size();
		String microarraySetName = "";
		if(microarraySet != null) microarraySetName = microarraySet.getDataSetName();

		Microarray[] gridMicroarrays = new Microarray[numArrays];
		for (int i = 0; i < numArrays; i++) {
			/* geworkbench array */
			DSMicroarray microarray = (DSMicroarray) microarraySetView.get(i);
			float data[] = microarray.getRawMarkerData();
			String name = microarray.getLabel();
			if (name == null || StringUtils.isEmpty(name))
				name = "i";// give array a name

			/* cagrid array */
			Microarray gridMicroarray = new Microarray();
			gridMicroarray.setArrayData(data);
			gridMicroarray.setArrayName(name);
			gridMicroarrays[i] = gridMicroarray;
		}

		/* extract marker info from DSMicroarraySet */
		int numMarkers = 0;
		if(microarraySet != null) numMarkers = ((DSMicroarray) microarraySet.get(0)).getMarkerNo();

		Marker[] gridMarkers = new Marker[numMarkers];
		int i = 0;
		for (DSGeneMarker marker : (DSItemList<DSGeneMarker>) microarraySetView
				.markers()) {
			Marker gridMarker = new Marker();
			gridMarker.setMarkerName(marker.getLabel());
			gridMarkers[i] = gridMarker;
			i++;
		}

		/* cagrid array set */
		MicroarraySet gridMicroarraySet = new MicroarraySet();
		gridMicroarraySet.setName(microarraySetName);
		gridMicroarraySet.setMicroarray(gridMicroarrays);
		gridMicroarraySet.setMarker(gridMarkers);
		// TODO set to get(set)Microarrays and get(set)Markers

		return gridMicroarraySet;
	}

	/**
	 * 
	 * @param microarraySetView
	 * @return Data
	 */
	public static Data convertFromBisonToCagridData(
			DSMicroarraySetView microarraySetView) {

		DSMicroarraySet microarraySet = microarraySetView.getMicroarraySet();

		/* extract microarray info from DSMicroarraySet */
		int numArrays = microarraySetView.size();

		/* extract marker info from DSMicroarraySet */
		int numMarkers = ((DSMicroarray) microarraySet.get(0)).getMarkerNo();

		Array[] arrays = new Array[numArrays];

		for (int i = 0; i < numArrays; i++) {
			/* geworkbench array */
			DSMicroarray microarray = (DSMicroarray) microarraySetView.get(i);
			float data[] = microarray.getRawMarkerData();
			String name = microarray.getLabel();
			if (name == null || StringUtils.isEmpty(name))
				name = "i";// give array a name

			/* cagrid array */
			Array array = new Array();
			String base64Value = BasicConverter.base64Encode(data);
			array.setBase64Value(base64Value);
			array.setName(name);
			array.setType("float");
			array.setDimensions(String.valueOf(numMarkers));
			arrays[i] = array;
		}

		Scalar[] markers = new Scalar[numMarkers];

		int i = 0;
		for (DSGeneMarker marker : (DSItemList<DSGeneMarker>) microarraySetView
				.markers()) {
			Scalar scalar = new Scalar();
			scalar.setName(String.valueOf(i));
			scalar.setValue(marker.getLabel());
			scalar.setType("String");
			markers[i] = scalar;
			i++;
		}

		/* cagrid array set */
		Data dataType = new Data();
		dataType.setArray(arrays);
		dataType.setScalar(markers);

		return dataType;
	}

	/**
	 * Convert from edu.columbia.geworkbench.cagrid.microarray.MicroarraySet to
	 * org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView.
	 * 
	 * @param gridMicroarraySet
	 * @return DSMicroarraySet
	 */
	public static DSMicroarraySetView convertFromCagridMicroarrayToBison(
			MicroarraySet gridMicroarraySet) {

		/* microarray info */
		int numMarkers = gridMicroarraySet.getMicroarray().length;
		String microarraySetName = gridMicroarraySet.getName();
		Microarray[] gridMicroarrays = gridMicroarraySet.getMicroarray();

		DSMicroarraySetView microarraySetView = new CSMicroarraySetView();
		DSMicroarraySet microarraySet = new CSMicroarraySet();
		microarraySet.setLabel(microarraySetName);

		for (int i = 0; i < numMarkers; i++) {
			/* cagrid array */
			float[] arrayData = gridMicroarrays[i].getArrayData();
			String arrayName = gridMicroarrays[i].getArrayName();

			/* bison array */
			DSMicroarray microarray = new CSMicroarray(arrayData.length);
			microarray.setLabel(arrayName);
			for (int j = 0; j < arrayData.length; j++) {
				DSMarkerValue markerValue = new CSExpressionMarkerValue(
						arrayData[j]);
				microarray.setMarkerValue(j, markerValue);
			}
			microarraySet.add(i, microarray);
		}

		// I need to add the marker names
		microarraySetView.setMicroarraySet(microarraySet);

		return microarraySetView;
	}

	/**
	 * 
	 * @param hierarchicalCluster
	 * @param view
	 * @return CSHierClusterDataSet
	 */
	public CSHierClusterDataSet createBisonHierarchicalClustering(
			HierarchicalCluster hierarchicalCluster,
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		log.debug("creating bison hierarchical cluster");
		HierarchicalClusterNode microarrayCluster = hierarchicalCluster
				.getMarkerCluster();
		HierarchicalClusterNode markerCluster = hierarchicalCluster
				.getMicroarrayCluster();
		HierCluster[] resultClusters = new HierCluster[2];
		if (markerCluster != null) {
			resultClusters[0] = convertToMarkerHierCluster(markerCluster, view
					.getMicroarraySet());
		}
		if (microarrayCluster != null) {
			resultClusters[1] = convertToMicroarrayHierCluster(
					microarrayCluster, view.getMicroarraySet());
		}
		CSHierClusterDataSet dataSet = new CSHierClusterDataSet(resultClusters,
				HIERARCHICAL_CLUSTERING_NAME, view);
		return dataSet;
	}

	/**
	 * 
	 * @param microarraySet
	 * @param method
	 * @param dimensions
	 * @param distance
	 * @param url
	 * @return DSHierClusterDataSet
	 * @throws Exception
	 */
	@Script
	public DSHierClusterDataSet doClustering(DSMicroarraySet microarraySet,
			String method, String dimensions, String distance, String url)
			throws Exception {
		log.debug("script method:  do clustering");
		CSMicroarraySetView view = new CSMicroarraySetView(microarraySet);
		MicroarraySet gridSet = CagridBisonConverter
				.convertFromBisonToCagridMicroarray(view);

		Dim dim = null;
		if (dimensions.equalsIgnoreCase(MARKER))
			dim = Dim.marker;
		else if (dimensions.equalsIgnoreCase(MICROARRAY))
			dim = Dim.microarray;
		else
			dim = Dim.both;

		Distance dist = null;
		if (distance.equalsIgnoreCase(EUCLIDEAN))
			dist = Distance.euclidean;
		else if (distance.equalsIgnoreCase(PEARSON))
			dist = Distance.pearson;
		else
			dist = Distance.spearman;

		Method meth = null;
		if (method.equalsIgnoreCase(SINGLE))
			meth = Method.single;
		else if (method.equalsIgnoreCase(AVERAGE))
			meth = Method.average;
		else
			meth = Method.complete;

		HierarchicalClusteringParameter parameters = new HierarchicalClusteringParameter(
				dim, dist, meth);

		HierarchicalClusteringClient client = new HierarchicalClusteringClient(
				url);
		HierarchicalCluster hierarchicalCluster = client.execute(gridSet,
				parameters);
		if (hierarchicalCluster != null) {
			CSHierClusterDataSet dataSet = createBisonHierarchicalClustering(
					hierarchicalCluster, view);
			return dataSet;
		} else {
			return null;
		}
	}

	@Script
	public DSSOMClusterDataSet doSOMClustering(DSMicroarraySet microarraySet,
			double alpha, int dim_x, int dim_y, int function, int iteration,
			double radius, String url) throws Exception {
		log.debug("script method:  do SOM clustering");
		CSMicroarraySetView view = new CSMicroarraySetView(microarraySet);
		MicroarraySet gridSet = CagridBisonConverter
				.convertFromBisonToCagridMicroarray(view);
		SomClusteringParameter parameters = new SomClusteringParameter(
				(float) alpha, dim_x, dim_y, function, iteration,
				(float) radius);
		SomClusteringClient client = new SomClusteringClient(url);
		SomCluster somCluster = client.execute(gridSet, parameters);
		if (somCluster != null) {
			CSSOMClusterDataSet bisonSomClustering = createBisonSomClustering(
					somCluster, view);
			return bisonSomClustering;
		}
		return null;
	}

	/**
	 * @param name
	 * @return DSMicroarray
	 */
	private DSMicroarray getArray(String name,
			DSMicroarraySet<DSMicroarray> microarraySet) {
		for (DSMicroarray array : microarraySet) {
			if (array.getLabel().equals(name)) {
				return array;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param node
	 * @param microarraySet
	 * @return
	 */
	private MicroarrayHierCluster convertToMicroarrayHierCluster(
			HierarchicalClusterNode node,
			DSMicroarraySet<DSMicroarray> microarraySet) {
		log
				.debug("converting hierarchical cluster from bison to grid microarray cluster");
		MicroarrayHierCluster cluster;
		if (node.isLeaf()) {
			cluster = new MicroarrayHierCluster();
			cluster.setMicroarray(getArray(node.getLeafLabel(), microarraySet));
		} else {
			MicroarrayHierCluster left = convertToMicroarrayHierCluster(node
					.getHierarchicalClusterNode(0), microarraySet);
			MicroarrayHierCluster right = convertToMicroarrayHierCluster(node
					.getHierarchicalClusterNode(1), microarraySet);
			cluster = new MicroarrayHierCluster();
			cluster.setDepth(Math.max(left.getDepth(), right.getDepth()) + 1);
			cluster.setHeight(node.getHeight());
			cluster.addNode(left, 0);
			cluster.addNode(right, 0);
		}
		return cluster;
	}

	/**
	 * @param node
	 * @return MarkerHierCluster
	 */
	private MarkerHierCluster convertToMarkerHierCluster(
			HierarchicalClusterNode node,
			DSMicroarraySet<DSMicroarray> microarraySet) {
		log
				.debug("convert hierarchical cluster from bison to grid marker cluster");
		MarkerHierCluster cluster;
		if (node.isLeaf()) {
			cluster = new MarkerHierCluster();
			cluster.setMarkerInfo(microarraySet.getMarkers().get(
					node.getLeafLabel()));
		} else {
			MarkerHierCluster left = convertToMarkerHierCluster(node
					.getHierarchicalClusterNode(0), microarraySet);
			MarkerHierCluster right = convertToMarkerHierCluster(node
					.getHierarchicalClusterNode(1), microarraySet);
			cluster = new MarkerHierCluster();
			cluster.setDepth(Math.max(left.getDepth(), right.getDepth()) + 1);
			cluster.setHeight(node.getHeight());
			cluster.addNode(left, 0);
			cluster.addNode(right, 0);
		}
		return cluster;
	}

	/**
	 * 
	 * @param somCluster
	 * @param view
	 * @return
	 */
	public CSSOMClusterDataSet createBisonSomClustering(SomCluster somCluster,
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		log.debug("creating bison som cluster");

		int width = somCluster.getWidth();
		int height = somCluster.getHeight();
		// Initialize width x height Bison SOM Cluster
		SOMCluster[][] bisonSomCluster = new SOMCluster[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				bisonSomCluster[x][y] = new DefaultSOMCluster();
				bisonSomCluster[x][y].setGridCoordinates(x, y);
			}
		}
		// Assign each marker to its appropriate cluster
		for (int i = 0; i < somCluster.getXCoordinate().length; i++) {
			int x = somCluster.getXCoordinate(i);
			int y = somCluster.getYCoordinate(i);
			DSGeneMarker marker = (DSGeneMarker) view.getMicroarraySet()
					.getMarkers().get(i);
			LeafSOMCluster node = new LeafSOMCluster(marker);
			bisonSomCluster[x][y].addNode(node);
		}

		// Build final result set
		CSSOMClusterDataSet dataSet = new CSSOMClusterDataSet(bisonSomCluster,
				SOM_CLUSTERING_NAME, view);

		return dataSet;
	}

	/**
	 * 
	 * @param adjacencyMatrix
	 * @return {@link AdjacencyMatrixDataSet}
	 */
	public AdjacencyMatrixDataSet createBisonAdjacencyMatrixDataSet(
			AdjacencyMatrix adjacencyMatrix,
			DSMicroarraySetView bisonMicroarraySetView, float threshold) {

		org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix bisonAdjacencyMatrix = new org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix();

		if (adjacencyMatrix.getGeneListA() == null
				|| adjacencyMatrix.getGeneListB() == null
				|| adjacencyMatrix.getMutualInformationValues() == null)

			return null;

		DSMicroarraySet microarraySet = bisonMicroarraySetView
				.getMicroarraySet();
		bisonAdjacencyMatrix.setMicroarraySet(microarraySet);

		DSItemList<DSSequential> markers = bisonMicroarraySetView.allMarkers();

		for (int i = 0; i < adjacencyMatrix.getGeneListA().length; i++) {
			double value = adjacencyMatrix.getMutualInformationValues(i);
			String geneALabel = adjacencyMatrix.getGeneListA(i);
			String geneBLabel = adjacencyMatrix.getGeneListB(i);

			DSSequential markerA = markers.get(geneALabel);
			int idA = markerA.getSerial();

			DSSequential markerB = markers.get(geneBLabel);
			int idB = markerB.getSerial();

			bisonAdjacencyMatrix
					.add(idA, idB, (new Double(value)).floatValue());

			if (log.isDebugEnabled()) {

				log.debug(geneALabel + " with id " + idA);
				log.debug(geneBLabel + " with id " + idB);

				log.debug("A[" + i + "]: " + geneALabel);
				log.debug("B[" + i + "]: " + geneBLabel);
				log.debug("value: " + value);
			}
		}

		AdjacencyMatrixDataSet adjacencyMatrixDataSet = new AdjacencyMatrixDataSet(
				bisonAdjacencyMatrix, -1, 0, 1000, "Adjacency Matrix",
				ARACNE_NAME, microarraySet);

		return adjacencyMatrixDataSet;

	}
	
	public NetBoostDataSet createNetBoostDataSet(Map<String, Object> bisonParams, types.NetBoostResults results, DSDataSet refOtherSet){
		if(results.getErrorMessage().trim().equals("")){					
			NetBoostDataSet ds = new NetBoostDataSet(null
					, "NetBoost Results (Grid)"
					, new NetBoostData(results.getClassScoreTarget()
						, results.getConfusion()
						, results.getTrainTestLoss())
					, ((EdgeListDataSet) refOtherSet).getFilename()
					);
			return ds;	
		} else {
			log.warn("Grid analysis results contained the following error(s): " + results.getErrorMessage());
			return null;
		}
		
	}

	// FIXME refactor this cagrid parameter handling from parameters panel
	/**
	 * 
	 * @param bisonParams
	 * @return HierarchicalClusteringParameter
	 */
	public HierarchicalClusteringParameter convertHierarchicalBisonToCagridParameter(
			Map<String, Object> bisonParams) {

		HierarchicalClusteringParameter hierarchicalClusteringParameter = new HierarchicalClusteringParameter();

		Set<String> keySet = bisonParams.keySet();
		for (String param : keySet) {
			if (StringUtils.equalsIgnoreCase(param, "Method")) {
				hierarchicalClusteringParameter.setMethod(Method
						.fromString((String) bisonParams.get(param)));
			} else if (StringUtils.equalsIgnoreCase(param, "Distance")) {
				hierarchicalClusteringParameter.setDistance(Distance
						.fromString((String) bisonParams.get(param)));
			} else if (StringUtils.equalsIgnoreCase(param, "Dimension")) {
				hierarchicalClusteringParameter.setDim(Dim
						.fromString((String) bisonParams.get(param)));
			} else {
				log.equals("Skipping param " + param);
			}
		}

		return hierarchicalClusteringParameter;
	}

	/**
	 * 
	 * @param bisonParameters
	 * @return SomClusteringParameter
	 */
	public SomClusteringParameter convertSomBisonToCagridParameter(
			Map<String, Object> bisonParameters) {

		SomClusteringParameter somClusteringParameter = new SomClusteringParameter();

		Set<String> keySet = bisonParameters.keySet();

		for (String param : keySet) {
			if (StringUtils.equalsIgnoreCase(param, "dimx")) {
				somClusteringParameter.setDim_x((Integer) bisonParameters
						.get(param));
			}

			else if (StringUtils.equalsIgnoreCase(param, "dimy")) {
				somClusteringParameter.setDim_y((Integer) bisonParameters
						.get(param));
			}

			else if (StringUtils.equalsIgnoreCase(param, "iterations")) {
				somClusteringParameter.setIteration((Integer) bisonParameters
						.get(param));
			}

			else if (StringUtils.equalsIgnoreCase(param, "radius")) {
				somClusteringParameter.setRadius((Float) bisonParameters
						.get(param));
			}

			else if (StringUtils.equalsIgnoreCase(param, "alpha")) {
				somClusteringParameter.setAlpha((Float) bisonParameters
						.get(param));
			}

			else if (StringUtils.equalsIgnoreCase(param, "function")) {

				String func = (String) bisonParameters.get(param);
				log.info(func);
			}

			else {
				log.debug("Skipping param " + param);
			}

		}

		return somClusteringParameter;
	}

	/**
	 * 
	 * @param bisonParameters
	 * @return {@link AracneParameter}
	 */
	public AracneParameter convertAracneBisonToCagridParameter(
			Map<String, Object> bisonParameters) {

		AracneParameter aracneParameter = new AracneParameter();

		for (String param : bisonParameters.keySet()) {

			if (StringUtils.equalsIgnoreCase(param, "dpi")) {
				float dpi = (Float) bisonParameters.get(param);
				aracneParameter.setTolerance(dpi);
			}

			else if (StringUtils.equalsIgnoreCase(param, "threshold")) {
				double threshold = (Float) bisonParameters.get(param);
				aracneParameter.setMutualInformationThreshold(threshold);
			}

			else if (StringUtils.equalsIgnoreCase(param, "hub")) {
				String hub = (String) bisonParameters.get(param);
				aracneParameter.setHub(hub);
			}

			else if (StringUtils.equalsIgnoreCase(param, "kernel")) {
				double kernelWidth = (Float) bisonParameters.get(param);
				aracneParameter.setKernelWidth(kernelWidth);
			} else {
				log.debug("Skipping param " + param);
			}

		}
		return aracneParameter;
	}
	
	public types.NetBoostParameters convertNetBoostBisonToCagridParameter(DSDataSet refOtherSet, Map<String, Object> bisonParameters){
		final String WALK = "w";
		final String SUBGRAPH = "s";
		
	    String selectedModels = null;
	    String subgraphCounting = null;
	    BigInteger[] trainingEx = new BigInteger[1];
	    BigInteger[] boostingIter = new BigInteger[1];
	    BigInteger[] crossValidFolds = new BigInteger[1];
	    
	    String errMsg = "";		
	    
	    // edge list
	    StringBuilder sb = new StringBuilder();
	    EdgeList el = ((EdgeListDataSet) refOtherSet).getData();
	    for(int i = 0; i < el.size(); i++){
	    	Edge e = el.getEdge(i);
	    	sb.append("\t");
	    	sb.append(e.getStartNode());
	    	sb.append("\t");
	    	sb.append(e.getEndNode());
	    	sb.append("\n");
	    }
	    
	    // selected models
	    selectedModels = "";
	    if(((Boolean) bisonParameters.get("lpa")).booleanValue()) selectedModels += "LPA,";	    
	    if(((Boolean) bisonParameters.get("rdg")).booleanValue()) selectedModels += "RDG,";
	    if(((Boolean) bisonParameters.get("rds")).booleanValue()) selectedModels += "RDS,";
	    if(((Boolean) bisonParameters.get("dmc")).booleanValue()) selectedModels += "DMC,";
	    if(((Boolean) bisonParameters.get("agv")).booleanValue()) selectedModels += "AGV,";
	    if(((Boolean) bisonParameters.get("smw")).booleanValue()) selectedModels += "SMW,";
	    if(((Boolean) bisonParameters.get("dmr")).booleanValue()) selectedModels += "DMR";	    
	    
	    // main parameters
	    if(((String) bisonParameters.get("subgraphCounting")).toLowerCase().indexOf("walk") >= 0){
	    	subgraphCounting = WALK;
	    } else {
	    	subgraphCounting = SUBGRAPH;
	    }	    
	    trainingEx[0] = new BigInteger(((Integer) bisonParameters.get("trainingExample")).toString());
	    boostingIter[0] = new BigInteger(((Integer) bisonParameters.get("boostingIteration")).toString());
	    crossValidFolds[0] = new BigInteger(((Integer) bisonParameters.get("crossValidationFolds")).toString());
	    
	    // error checking
		if(el == null)
			errMsg += ":No edge list.";
		
		if((selectedModels == null) || (selectedModels.trim().equals("")))
			errMsg += ":No selected models";
		
		if((subgraphCounting == null) || subgraphCounting.trim().equals("") 
				|| (!subgraphCounting.trim().equalsIgnoreCase(WALK) && !subgraphCounting.trim().equalsIgnoreCase(SUBGRAPH)))
			errMsg += ":No or invalid subgraph counting method.";
		
		if((trainingEx == null) || (trainingEx[0] == null))
			errMsg += ":No training examples.";
		
		if((boostingIter == null) || (boostingIter[0] == null))
			errMsg += ":No boosting iterations.";
		
		if((crossValidFolds == null) || (crossValidFolds[0] == null))
			errMsg += ":No cross validation folds.";
		
		if(!errMsg.trim().equals("")){
			log.warn("Encountered errors when converting netboost bison parameters to cagrid parameters" + errMsg);
			return null;
		}
		
		// creating parameters
		types.NetBoostParameters params = new types.NetBoostParameters(
				  boostingIter
				  , crossValidFolds
				  , sb.toString()
				  , selectedModels
				  , subgraphCounting  // w = walk, s = subgraphs
				  , trainingEx
				  );		
		return params;
	}
}
