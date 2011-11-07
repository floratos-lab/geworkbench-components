package org.geworkbench.components.somclustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.clusters.CSSOMClusterDataSet;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.DefaultSOMCluster;
import org.geworkbench.bison.model.clusters.LeafSOMCluster;
import org.geworkbench.bison.model.clusters.SOMCluster;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.util.ProgressBar;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author First Genetic Trust
 * @version $Id$
 */

/**
 * Component to perform the SOM Analysis.
 * <p>
 * <b><u>NOTE:</u></b> The code in this file is based on the SOM clustering
 * algorithm implementation developed by TIGR (The Institute for Genomic
 * Research), in the context of their TMEV project. In particular, we have
 * borrowed and modified for our purposes parts of the source file
 * <code>SOM.java</code> located in the package:
 * <p>
 * <p>
 * &nbsp;&nbsp;&nbsp; org.tigr.microarray.mev.cluster.algorithm.impl
 */
public class SOMAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2630277710658744215L;

	/**
	 * Analysis type
	 */
	private int localAnalysisType;

	private org.geworkbench.util.ProgressBar pb = null;

	private int dim_x;

	private int dim_y;

	private float factor;

	private int number_of_genes;

	private int number_of_samples;

	private float[][] expMatrix;

	private float[][][] somCodes;

	private final String analysisName = "Som";

	/**
	 * Default Constructor
	 */
	public SOMAnalysis() {
		localAnalysisType = AbstractAnalysis.SOM_CLUSTERING_TYPE;
		setDefaultPanel(new SOMPanel());
	}

	/**
	 * <code>AbstractAnalysis</code> method
	 * 
	 * @return Analysis type
	 */
	public int getAnalysisType() {
		return localAnalysisType;
	}

	/**
	 * Initiates the SOM Analysis execution on the given dataset
	 * 
	 * @param input
	 *            input dataset
	 * @return <code>AlgorithmExecutionResults</code> containing the result of
	 *         clustering
	 */
	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		if (input == null) {
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		}
		assert input instanceof DSMicroarraySetView;
		DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> data = (DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray>) input;
		if (data.items().size()<2){
			return new AlgorithmExecutionResults(false,
					"You'll need to select at least two microarrays for SOM analysis.\n"
							+ "Please select more microarrays and try again.", null);			
		}
		if (data.getUniqueMarkers().size()<3){
			return new AlgorithmExecutionResults(false,
					"You'll need to select at least three markers for SOM analysis.\n"
							+ "Please select more markers and try again.", null);			
		}
		if (containsMissingValues(data)) {
			return new AlgorithmExecutionResults(false,
					"Microarray set contains missing values.\n"
							+ "Remove before proceeding.", null);
		}

		factor = 1.0f;

		SOMCluster result_cluster = new DefaultSOMCluster();
		dim_x = ((SOMPanel) aspp).getRows();
		dim_y = ((SOMPanel) aspp).getColumns();
		int iterations = ((SOMPanel) aspp).getIterations();
		boolean adoptType = true;
		boolean is_neighborhood_bubble = ((SOMPanel) aspp).getFunction()
				.equals("Bubble");
		boolean is_random_vector = false;
		float radius = ((SOMPanel) aspp).getRadius();
		float alpha = ((SOMPanel) aspp).getLearningRate();
		number_of_genes = data.markers().size();
		number_of_samples = data.items().size();
		this.expMatrix = new float[number_of_genes][number_of_samples];
		pb = org.geworkbench.util.ProgressBar
				.create(org.geworkbench.util.ProgressBar.BOUNDED_TYPE);
		pb.addObserver(this);
		pb.setTitle("SOM Clustering");
		pb.setMessage("Constructing Input Vector");
		pb.setBounds(new org.geworkbench.util.ProgressBar.IncrementModel(0,
				number_of_genes, 0, number_of_genes, 1));
		pb.start();

		for (int i = 0; i < number_of_genes && !stopAlgorithm; i++) {
			pb.update();
			for (int j = 0; j < number_of_samples && !stopAlgorithm; j++) {
				DSGeneMarker mInfo = data.markers().get(i);
				this.expMatrix[i][j] = (float) data.items().get(j)
						.getMarkerValue(mInfo).getValue();
			}
		}

		if (stopAlgorithm) {
			stopAlgorithm = false;
			pb.stop();
			return null;
		}

		if (is_random_vector) {
			this.somCodes = randomVectorInit();
		} else {
			this.somCodes = randomGeneInit();
		}

		pb.reset();
		pb.setMessage("Adapting Vectors");
		pb.setBounds(new org.geworkbench.util.ProgressBar.IncrementModel(0,
				iterations, 0, iterations, 1));
		int[] winner_info = new int[2];
		float cRadius = radius;
		float cAlpha = alpha;

		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < iterations && !stopAlgorithm; i++) {
			int sample = random.nextInt(number_of_genes);
			pb.update();
			/* Radius decreases linearly to one */
			cRadius = 1.0f + (radius - 1.0f) * (float) (iterations - i)
					/ (float) iterations;
			/* Calculate Learning rate */
			cAlpha = linearAlpha(i, iterations, alpha);
			/* Find the best match */
			findWinnerEuclidean(winner_info, sample);
			/* Adapt the units */
			if (is_neighborhood_bubble) {
				bubbleAdapt(sample, winner_info, cRadius, cAlpha, adoptType);
			} else {
				gaussianAdapt(sample, winner_info, cRadius, cAlpha, adoptType);
			}
			// sample++;
			// if (sample >= number_of_genes) {
			// sample = 0;
			// }
		}

		// clustering...
		// SOMMatrix clusters = new SOMMatrix(dim_x, dim_y, 0);
		ArrayList<ArrayList<ArrayList<Integer>>> clusters = new ArrayList<ArrayList<ArrayList<Integer>>>(dim_y);
		ArrayList<ArrayList<Integer>> list1;
		ArrayList<Integer> list2;
		for (int i = 0; i < dim_y && !stopAlgorithm; i++) {
			list1 = new ArrayList<ArrayList<Integer>>(dim_x);
			for (int j = 0; j < dim_x && !stopAlgorithm; j++) {
				list2 = new ArrayList<Integer>(0);
				for (int k = 0; k < 0; k++) {
					list2.add(new Integer(0));
				}
				list1.add(list2);
			}
			clusters.add(list1);
		}

		float[][] u_matrix = new float[dim_x][dim_y];
		calculateClusters(clusters, u_matrix);
		int dimension;
		// Cluster result_cluster = new Cluster();
		// NodeList nodeList = result_cluster.getNodeList();
		for (int i = 0; i < clusters.size() && !stopAlgorithm; i++) {
			ArrayList<ArrayList<Integer>> c = clusters.get(i);
			for (int j = 0; j < c.size(); j++) {
				Cluster clusterRep = new DefaultSOMCluster();
				result_cluster.addNode(clusterRep);
			}
		}

		SOMCluster[] nodeList = (SOMCluster[]) result_cluster
				.getChildrenNodes();
		SOMCluster[][] results = new SOMCluster[dim_x][dim_y];
		for (int x = 0; x < dim_x && !stopAlgorithm; x++) {
			for (int y = 0; y < dim_y && !stopAlgorithm; y++) {
				dimension = x * dim_y + y;
				ArrayList<Integer> c = clusters.get(y).get(x);
				int size = c.size();
				for (int i = 0; i < size && !stopAlgorithm; i++) {
					int geneIndex = c.get(i).intValue();
					// Transform the following to SOMCluster
					Cluster node = new LeafSOMCluster(data.markers().get(
							geneIndex));
					nodeList[dimension].addNode(node);
				}
				results[x][y] = nodeList[dimension];
			}
		}
		if (stopAlgorithm) {
			stopAlgorithm = false;
			pb.stop();
			return null;
		}
		pb.stop();
		CSSOMClusterDataSet dataSet = new CSSOMClusterDataSet(results,
				"SOM Clusters", data);
		HistoryPanel.addToHistory(dataSet, this.generateDataSetHistory(dim_x, dim_y, iterations, is_neighborhood_bubble, radius, alpha, data));
		return new AlgorithmExecutionResults(true, "SOM Clustering results",
				dataSet);
	}

	/**
	 * <code>AbstractAnalysis</code> method
	 * 
	 * @return Analysis type
	 */
	public String getType() {
		return "SOM";
	}

	private float linearAlpha(long currentIteration, long iterations,
			float alpha) {
		return (alpha * (float) (iterations - currentIteration) / (float) iterations);
	}

	private final float findWinnerEuclidean(int[] winner_info, int sample) {
		winner_info[0] = -1;
		winner_info[1] = -1;
		float winner_distance = -1.0f;
		if (number_of_samples == 1) {
			return winner_distance;
		}
		int x, y, i;
		double difference;
		double diffsf = Double.MAX_VALUE;
		float[][] dummyMatrix = new float[1][number_of_samples];
		for (y = 0; y < dim_y; y++) {
			for (x = 0; x < dim_x; x++) {
				for (i = 0; i < number_of_samples; i++) {
					dummyMatrix[0][i] = somCodes[x][y][i];
				}

				difference = geneEuclidianDistance(expMatrix, dummyMatrix,
						sample, 0, factor);
				if (difference <= diffsf) {
					winner_info[0] = x;
					winner_info[1] = y;
					diffsf = difference;
					winner_distance = (float) difference;
				}

			}

		}
		return winner_distance;
	}

	private void bubbleAdapt(int sample, int[] winner_info, float radius,
			float alpha, boolean rectangular) {

		int x, y;
		for (y = 0; y < dim_y; y++) {
			for (x = 0; x < dim_x; x++) {
				if (rectangular) {
					if (rectangularDistance(winner_info, x, y) <= radius) {
						adaptVector(sample, x, y, alpha);
					}

				} else {
					if (hexagonalDistance(winner_info, x, y) <= radius) {
						adaptVector(sample, x, y, alpha);
					}

				}

			}

		}

	}

	private void gaussianAdapt(int sample, int[] winner_info, float radius,
			float alpha, boolean rectangular) {

		float dd, alp;
		int x, y;
		for (y = 0; y < dim_y; y++) {
			for (x = 0; x < dim_x; x++) {
				if (rectangular) {
					dd = rectangularDistance(winner_info, x, y);
				} else {
					dd = hexagonalDistance(winner_info, x, y);
				}

				alp = alpha
						* (float) (Math
								.exp((float) (-dd * dd / (2.0 * radius * radius)))
								/ radius / Math.sqrt(2 * Math.PI));
				adaptVector(sample, x, y, alp);
			}

		}

	}

	private void calculateClusters(ArrayList<ArrayList<ArrayList<Integer>>> clusters, float[][] u_matrix) {
		pb.reset();
		pb.setMessage("Calculating Clusters");
		pb.setBounds(new org.geworkbench.util.ProgressBar.IncrementModel(0,
				number_of_genes, 0, number_of_genes, 1));
		// SOMMatrix distances = new SOMMatrix(dim_x, dim_y, 0);
		ArrayList<ArrayList<ArrayList<Float>>> distances = new ArrayList<ArrayList<ArrayList<Float>>>(dim_y);
		ArrayList<ArrayList<Float>> list1;
		ArrayList<Float> list2;
		for (int i = 0; i < dim_y && !stopAlgorithm; i++) {
			list1 = new ArrayList<ArrayList<Float>>(dim_x);
			for (int j = 0; j < dim_x && !stopAlgorithm; j++) {
				list2 = new ArrayList<Float>(0);
				for (int k = 0; k < 0; k++) {
					list2.add(new Float(0));
				}
				list1.add(list2);
			}
			distances.add(list1);
		}

		for (int y = 0; y < dim_y && !stopAlgorithm; y++) {
			for (int x = 0; x < dim_x && !stopAlgorithm; x++) {
				u_matrix[x][y] = 0f;
			}
		}

		float winner_distance;
		float max_winner_distance = 0f;
		int[] winner_info = new int[2];
		int counter;
		for (int i = 0; i < number_of_genes && !stopAlgorithm; i++) {
			pb.update();
			winner_distance = findWinnerEuclidean(winner_info, i);
			if (winner_info[1] == -1 || winner_info[1] == -1) {
			} else {
				max_winner_distance = Math.max(winner_distance,
						max_winner_distance);
				if (winner_distance > u_matrix[winner_info[0]][winner_info[1]]) {
					u_matrix[winner_info[0]][winner_info[1]] = winner_distance;
				}
				counter = 0;
				for (int j = 0; j < distances
						.get(winner_info[1]).get(winner_info[0]).size(); j++) {
					if (winner_distance < distances
							.get(winner_info[1]).get(winner_info[0]).get(j)) {
						break;
					}
					counter++;
				}

				distances.get(winner_info[1])
						.get(winner_info[0]).add(counter, new Float(
						winner_distance));
				// distances.insertValue(winner_info[0], winner_info[1],
				// counter,
				// winner_distance);
				clusters.get(winner_info[1])
						.get(winner_info[0]).add(counter, new Integer(i));
				// clusters.insertValue(winner_info[0], winner_info[1], counter,
				// i);
			}
		}
		for (int y = 0; y < dim_y && !stopAlgorithm; y++) {
			for (int x = 0; x < dim_x && !stopAlgorithm; x++) {
				u_matrix[x][y] = u_matrix[x][y] / max_winner_distance;
			}
		}
	}

	private void adaptVector(int sample, int x, int y, float alpha) {
		int i;
		for (i = 0; i < number_of_samples && !stopAlgorithm; i++) {
			if (Float.isNaN(expMatrix[sample][i])) {
				continue;
			} else {
				somCodes[x][y][i] = somCodes[x][y][i] + alpha
						* (expMatrix[sample][i] - somCodes[x][y][i]);
			}
		}

	}

	private float rectangularDistance(int[] winner_info, int tx, int ty) {
		float ret, diff;
		diff = winner_info[0] - tx;
		ret = diff * diff;
		diff = winner_info[1] - ty;
		ret += diff * diff;
		ret = (float) Math.sqrt((float) ret);
		return (ret);
	}

	private float hexagonalDistance(int[] winner_info, int tx, int ty) {
		float ret, diff;
		diff = winner_info[0] - tx;
		if (((winner_info[1] - ty) % 2) != 0) {
			if ((winner_info[1] % 2) == 0) {
				diff -= 0.5;
			} else {
				diff += 0.5;
			}
		}
		ret = diff * diff;
		diff = winner_info[1] - ty;
		ret += 0.75 * diff * diff;
		ret = (float) Math.sqrt((float) ret);
		return (ret);
	}

	private float[][][] randomGeneInit() {
		pb.reset();
		pb.setMessage("Randomizing Markers");
		pb.setBounds(new org.geworkbench.util.ProgressBar.IncrementModel(0,
				dim_y * dim_x, 0, dim_y * dim_x, 1));
		Random random = new Random(System.currentTimeMillis());
		float[][][] somCodes = new float[dim_x][dim_y][number_of_samples];
		int gene;
		for (int y = 0; y < dim_y && !stopAlgorithm; y++) {
			for (int x = 0; x < dim_x && !stopAlgorithm; x++) {
				pb.update();
				gene = (int) (random.nextFloat() * number_of_genes);
				for (int k = 0; k < number_of_samples && !stopAlgorithm; k++) {
					somCodes[x][y][k] = this.expMatrix[gene][k];
				}
			}
		}
		return somCodes;
	}

	private float[][][] randomVectorInit() {
		pb.reset();
		pb.setMessage("Randomizing Vectors");
		pb.setBounds(new ProgressBar.IncrementModel(0, number_of_genes, 0,
				number_of_genes, 1));
		float[][][] somCodes = new float[dim_x][dim_y][number_of_samples];
		float[] maxValue = new float[number_of_samples];
		float[] minValue = new float[number_of_samples];
		int i, j, k;
		for (i = 0; i < number_of_samples && !stopAlgorithm; i++) {
			minValue[i] = Float.MAX_VALUE;
			maxValue[i] = Float.MIN_VALUE;
		}

		float dummy;
		for (i = 0; i < number_of_genes && !stopAlgorithm; i++) {
			pb.update();
			for (j = 0; j < number_of_samples && !stopAlgorithm; j++) {
				dummy = expMatrix[i][j];
				if (Float.isNaN(dummy)) {
					continue;
				}
				if (maxValue[j] < dummy) {
					maxValue[j] = dummy;
				}
				if (minValue[j] > dummy) {
					minValue[j] = dummy;
				}
			}
		}

		float value;
		Random random = new Random(System.currentTimeMillis());
		for (i = 0; i < dim_x && !stopAlgorithm; i++) {
			for (j = 0; j < dim_y && !stopAlgorithm; j++) {
				for (k = 0; k < number_of_samples && !stopAlgorithm; k++) {
					value = minValue[k] + (maxValue[k] - minValue[k])
							* random.nextFloat();
					somCodes[i][j][k] = value;
				}
			}
		}
		return somCodes;
	}

	private float geneEuclidianDistance(float[][] matrix, float[][] M, int g1,
			int g2, float factor) {
		if (M == null) {
			M = matrix;
		}

		int k = number_of_samples;
		double sum = 0.0;
		for (int i = 0; i < k && !stopAlgorithm; i++) {
			if ((!Float.isNaN(matrix[g1][i])) && (!Float.isNaN(M[g2][i]))) {
				sum += Math.pow((matrix[g1][i] - M[g2][i]), 2);
			}
		}
		return (float) (Math.sqrt(sum) * factor);
	}

	/**
	 * Check if the argument <code>MicroarraySet</code> contains missing
	 * values.
	 * 
	 * @param maSet
	 *            <code>MicroarraySet that is to be validated
	 * @return if the input contains any missing values
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
	 */
	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {

		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();

		int alignmentX = ((SOMPanel) aspp).getRows();
		int alignmentY = ((SOMPanel) aspp).getColumns();
		int iterations = ((SOMPanel) aspp).getIterations();

		float radius = ((SOMPanel) aspp).getRadius();
		float alpha = ((SOMPanel) aspp).getLearningRate();
		String function = ((SOMPanel) aspp).getFunction();

		parameterMap.put("dimx", alignmentX);
		parameterMap.put("dimy", alignmentY);
		parameterMap.put("iterations", iterations);
		parameterMap.put("radius", radius);
		parameterMap.put("alpha", alpha);
		parameterMap.put("function", function);

		return parameterMap;
	}

	private String generateDataSetHistory(int dim_x, int dim_y, int iterations, boolean is_neighborhood_bubble, float radius, float alpha, DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> data){
		String answer="";
		answer+=aspp.getDataSetHistory();
		answer+="\n"+data.items().size() +" microarray analyzed:\n";		
		for (DSMicroarray microarray : data.items()){
			answer+="\t"+microarray.getLabel()+"\n";
		}		
		answer+="\n"+data.markers().size() +" markers analyzed:\n";		
		for (DSGeneMarker marker : data.markers()){
			answer+="\t"+marker.getLabel()+"\n";
		}		
		return answer;
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
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonReturnType()
	 */
	@Override
	public Class<?> getBisonReturnType() {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public ParamValidationResults validInputData(DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView, DSDataSet<?> refMASet) {
		if (maSetView == null) {
			return new ParamValidationResults(false, "Invalid input.");
		}
		assert maSetView instanceof DSMicroarraySetView;
		DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> data = maSetView;
		if (data.items().size()<2){
			return new ParamValidationResults(false,
					"You'll need to select at least two microarrays for SOM analysis.\n"
							+ "Please select more microarrays and try again.");			
		}
		if (data.getUniqueMarkers().size()<3){
			return new ParamValidationResults(false,
					"You'll need to select at least three markers for SOM analysis.\n"
							+ "Please select more markers and try again.");			
		}
		if (containsMissingValues(data)) {
			return new ParamValidationResults(false,
					"Microarray set contains missing values.\n"
							+ "Remove before proceeding.");
		}
		return new ParamValidationResults(true,"No Error");
	}
}
