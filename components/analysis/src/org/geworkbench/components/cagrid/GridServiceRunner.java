package org.geworkbench.components.cagrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.CSSOMClusterDataSet;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;

import edu.columbia.geworkbench.cagrid.aracne.AdjacencyMatrix;
import edu.columbia.geworkbench.cagrid.aracne.AracneParameter;
import edu.columbia.geworkbench.cagrid.aracne.client.AracneClient;
import edu.columbia.geworkbench.cagrid.cluster.client.HierarchicalClusteringClient;
import edu.columbia.geworkbench.cagrid.cluster.client.SomClusteringClient;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalCluster;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter;
import edu.columbia.geworkbench.cagrid.cluster.som.SomCluster;
import edu.columbia.geworkbench.cagrid.cluster.som.SomClusteringParameter;
import edu.columbia.geworkbench.cagrid.converter.CagridBisonConverter;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;

/**
 * 
 * Used to execute grid services.
 * 
 * @author keshav
 * @version $Id: GridServiceRunner.java,v 1.2 2007-04-16 20:04:22 keshav Exp $
 */
public class GridServiceRunner {
	private static Log log = LogFactory.getLog(GridServiceRunner.class);

	private static final String HIERARCHICAL_NAME = "Hierarchical";

	private static final String SOM_NAME = "Som";

	private static final String ARACNE_NAME = "Aracne";

	private static final String GRID_ANALYSIS = "Grid Analysis";

	private static final String HIERARCHICAL_CLUSTERING_GRID = "Hierarchical Clustering (Grid)";

	private static final String SOM_CLUSTERING_GRID = "Som Clustering (Grid)";

	private static final String ARACNE_GRID = "Aracne (Grid)";

	private static final String STATML = "Statml";

	private static final String MAGE = "Mage";

	private CagridBisonConverter cagridBisonConverter = null;

	private ProjectNodeAddedEvent event = null;

	List<String> servicesCache = null;

	public GridServiceRunner() {
		servicesCache = new ArrayList<String>();

		servicesCache.add(HIERARCHICAL_NAME);
		servicesCache.add(SOM_NAME);
		servicesCache.add(ARACNE_NAME);

	}

	/**
	 * 
	 * @param url
	 * @param maSetView
	 * @param selectedAnalysis
	 * @return {@link ProjectNodeAddedEvent}
	 */
	public ProjectNodeAddedEvent executeGridAnalysis(String url,
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			AbstractAnalysis selectedAnalysis) {

		MicroarraySet gridSet = CagridBisonConverter
				.convertFromBisonToCagridMicroarray(maSetView);

		cagridBisonConverter = new CagridBisonConverter();

		ProgressBar pBar = Util.createProgressBar(GRID_ANALYSIS);

		AbstractGridAnalysis selectedGridAnalysis = (AbstractGridAnalysis) selectedAnalysis;

		if (servicesCache.contains(selectedGridAnalysis.getAnalysisName())) {

			String analysisName = selectedGridAnalysis.getAnalysisName();
			log.info("Service is of type " + analysisName);

			if (url.contains(MAGE)) {
				log.info("Mage service detected ...");
				// TODO add hooks to handle this

			} else if (url.contains(STATML)) {
				log.info("Statml service detected ...");
				// TODO add hooks to handle this

			} else {

				if (analysisName.equalsIgnoreCase(HIERARCHICAL_NAME)) {
					Map<String, Object> bisonParameters = ((AbstractGridAnalysis) selectedAnalysis)
							.getBisonParameters();
					HierarchicalClusteringParameter parameters = cagridBisonConverter
							.convertHierarchicalBisonToCagridParameter(bisonParameters);

					HierarchicalCluster hierarchicalCluster;
					try {
						pBar.setMessage("Running "
								+ HIERARCHICAL_CLUSTERING_GRID);
						pBar.start();
						pBar.reset();
						HierarchicalClusteringClient client = new HierarchicalClusteringClient(
								url);
						hierarchicalCluster = client.execute(gridSet,
								parameters);
					} catch (Exception e) {
						throw new RuntimeException(e);
					} finally {
						pBar.stop();
					}
					if (hierarchicalCluster != null) {
						// convert grid to bison hierarchical cluster
						CSHierClusterDataSet dataSet = cagridBisonConverter
								.createBisonHierarchicalClustering(
										hierarchicalCluster, maSetView);
						event = new ProjectNodeAddedEvent(
								HIERARCHICAL_CLUSTERING_GRID, null, dataSet);
					}
				}

				else if (analysisName.equalsIgnoreCase(SOM_NAME)) {
					Map<String, Object> bisonParameters = ((AbstractGridAnalysis) selectedAnalysis)
							.getBisonParameters();

					SomClusteringParameter somClusteringParameters = cagridBisonConverter
							.convertSomBisonToCagridParameter(bisonParameters);

					if (somClusteringParameters == null)
						return null;

					SomCluster somCluster = null;
					try {
						pBar.setMessage("Running " + SOM_CLUSTERING_GRID);
						pBar.start();
						pBar.reset();
						SomClusteringClient client = new SomClusteringClient(
								url);
						somCluster = client.execute(gridSet,
								somClusteringParameters);
					} catch (Exception e) {
						throw new RuntimeException(e);
					} finally {
						pBar.stop();
					}
					if (somCluster != null) {
						// convert grid to bison hierarchical cluster
						CSSOMClusterDataSet dataSet = cagridBisonConverter
								.createBisonSomClustering(somCluster, maSetView);
						event = new ProjectNodeAddedEvent(SOM_CLUSTERING_GRID,
								null, dataSet);

					}
				}

				else if (analysisName.equalsIgnoreCase(ARACNE_NAME)) {

					Map<String, Object> bisonParameters = ((AbstractGridAnalysis) selectedAnalysis)
							.getBisonParameters();

					AracneParameter aracneParameters = cagridBisonConverter
							.convertAracneBisonToCagridParameter(bisonParameters);

					if (aracneParameters == null)
						return null;

					AdjacencyMatrix adjacencyMatrix = null;
					try {
						pBar.setMessage("Running " + ARACNE_GRID);
						pBar.start();
						pBar.reset();
						AracneClient client = new AracneClient(url);
						adjacencyMatrix = client.execute(aracneParameters,
								gridSet);

						if (adjacencyMatrix != null) {
							AdjacencyMatrixDataSet dataSet = cagridBisonConverter
									.createBisonAdjacencyMatrixDataSet(
											adjacencyMatrix, maSetView,
											(Float) bisonParameters
													.get("threshold"));

							event = new ProjectNodeAddedEvent(ARACNE_GRID,
									null, dataSet);
						}
					} catch (Exception e) {
						throw new RuntimeException("Error executing "
								+ ARACNE_GRID + e);
					} finally {
						pBar.stop();
					}

				}

				return event;

			}
		}

		return null;
	}
}
