package org.geworkbench.components.cagrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.CSSOMClusterDataSet;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.NetBoostDataSet;

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
import edu.columbia.geworkbench.cagrid.netboost.client.*;

/**
 * 
 * Used to execute grid services.
 * 
 * @author keshav
 * @version $Id: GridServiceRunner.java,v 1.5 2007-10-19 00:23:58 hungc Exp $
 */
public class GridServiceRunner {
	private static Log log = LogFactory.getLog(GridServiceRunner.class);

	private static final String HIERARCHICAL_NAME = "Hierarchical";

	private static final String SOM_NAME = "Som";

	private static final String ARACNE_NAME = "Aracne";

    private static final String EI_NAME = "EvidenceIntegration";

    private static final String GRID_ANALYSIS = "Grid Analysis";

	private static final String HIERARCHICAL_CLUSTERING_GRID = "Hierarchical Clustering (Grid)";

	private static final String SOM_CLUSTERING_GRID = "Som Clustering (Grid)";

    private static final String EI_GRID = "Evidence Integration (Grid)";

    private static final String ARACNE_GRID = "Aracne (Grid)";

	private static final String STATML = "Statml";

	private static final String MAGE = "Mage";
	
	private static final String NETBOOST = "NetBoost";
	
	private static final String NETBOOST_GRID = "NetBoost (Grid)";

	private CagridBisonConverter cagridBisonConverter = null;

	private ProjectNodeAddedEvent event = null;

	List<String> servicesCache = null;

	public GridServiceRunner() {
		servicesCache = new ArrayList<String>();

		servicesCache.add(HIERARCHICAL_NAME);
		servicesCache.add(SOM_NAME);
		servicesCache.add(ARACNE_NAME);
        servicesCache.add(EI_NAME);        
		servicesCache.add(NETBOOST);
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
			DSDataSet refOtherSet,
			AbstractAnalysis selectedAnalysis) {

		MicroarraySet gridSet = null;
		
		if(maSetView != null) gridSet = CagridBisonConverter
				.convertFromBisonToCagridMicroarray(maSetView);

		cagridBisonConverter = new CagridBisonConverter();

		ProgressBar pBar = Util.createProgressBar(GRID_ANALYSIS);

		AbstractGridAnalysis selectedGridAnalysis = (AbstractGridAnalysis) selectedAnalysis;

		if (servicesCache.contains(selectedGridAnalysis.getAnalysisName())) {

			String analysisName = selectedGridAnalysis.getAnalysisName();
			log.info("Service is of type " + analysisName);

			Map<String, Object> bisonParameters = selectedGridAnalysis
					.getBisonParameters();

			if (url.contains(MAGE)) {
				log.info("Mage service detected ...");
				// TODO add hooks to handle this

			} else if (url.contains(STATML)) {
				log.info("Statml service detected ...");
				// TODO add hooks to handle this

			} else {

				if (analysisName.equalsIgnoreCase(HIERARCHICAL_NAME)) {

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

				} else if (analysisName.equalsIgnoreCase(EI_NAME)) {


					try {
						pBar.setMessage("Running " + EI_GRID);
						pBar.start();
						pBar.reset();
						SomClusteringClient client = new SomClusteringClient(
								url);

					} catch (Exception e) {
						throw new RuntimeException(e);
					} finally {
						pBar.stop();
					}
//					if (somCluster != null) {
//						// convert grid to bison hierarchical cluster
//						CSSOMClusterDataSet dataSet = cagridBisonConverter
//								.createBisonSomClustering(somCluster, maSetView);
//						event = new ProjectNodeAddedEvent(SOM_CLUSTERING_GRID,
//								null, dataSet);
//
//					}
				}
				
				else if (analysisName.equalsIgnoreCase(NETBOOST)){
					log.info("Initiating NetBoost grid service...: " + System.currentTimeMillis());	
					
					// Compiling parameter descriptions
					types.NetBoostParameters netboostParameters = cagridBisonConverter
					.convertNetBoostBisonToCagridParameter(refOtherSet, bisonParameters);

					if (netboostParameters == null)
						return null;
		
					types.NetBoostResults results = null;
					try {
						pBar.setMessage("Running " + NETBOOST_GRID);
						pBar.start();
						pBar.reset();
						
						NetBoostClient client = new NetBoostClient(url);
						results = client.execute(netboostParameters);
						if (results != null) {
							if(results.getErrorMessage().trim().equals("")){
								// printing out results to debug log
								log.debug("NetBoostClient:results::");
								System.out.println("classscore:\n" + results.getClassScoreTarget());
								System.out.println("confusion matrix:\n" + results.getConfusion());
								System.out.println("traintestloss:\n" + results.getTrainTestLoss());								
								
								// convert results to netboost data set
								NetBoostDataSet dataSet = cagridBisonConverter
										.createNetBoostDataSet(bisonParameters, results, refOtherSet);
															
								// param desc for data set history
								StringBuilder paramDesc = new StringBuilder();
								paramDesc.append("Training Example: ");
								paramDesc.append(bisonParameters.get("trainingExample"));
								paramDesc.append("\n");
								paramDesc.append("Boosting Iterations: ");
								paramDesc.append(bisonParameters.get("boostingIteration"));
								paramDesc.append("\n");
								paramDesc.append("Subgraph Counting Method: ");
								paramDesc.append(bisonParameters.get("subgraphCounting"));
								paramDesc.append("\n");
								paramDesc.append("Cross-validation Folds: ");
								paramDesc.append(bisonParameters.get("crossValidationFolds"));
								paramDesc.append("\nModels: ");
								if(((Boolean) bisonParameters.get("lpa")).booleanValue())
									paramDesc.append("LPA  ");
								if(((Boolean) bisonParameters.get("rdg")).booleanValue())
									paramDesc.append("RDG  ");
								if(((Boolean) bisonParameters.get("rds")).booleanValue())
									paramDesc.append("RDS  ");
								if(((Boolean) bisonParameters.get("dmc")).booleanValue())
									paramDesc.append("DMC  ");
								if(((Boolean) bisonParameters.get("agv")).booleanValue())
									paramDesc.append("AGV  ");
								if(((Boolean) bisonParameters.get("smw")).booleanValue())
									paramDesc.append("SMW  ");
								if(((Boolean) bisonParameters.get("dmr")).booleanValue())
									paramDesc.append("DMR  ");
								log.info("===NetBoost Parameter Description===\n" + paramDesc.toString());
								System.out.println("===NetBoostData===\n" + dataSet.getData().toString());
								
								// broadcast to framework
								org.geworkbench.builtin.projects.ProjectPanel.addToHistory(dataSet, paramDesc.toString());
								event = new ProjectNodeAddedEvent(NETBOOST_GRID, null, dataSet);
							} else {
								log.error("No result from grid service: " + results.getErrorMessage());
								JOptionPane.showMessageDialog(null, NETBOOST_GRID,
										"No result from NetBoost (Grid) remote service: " + results.getErrorMessage()
										, JOptionPane.ERROR_MESSAGE);
							}
						} else {
							log.error("No result from grid service");
							JOptionPane.showMessageDialog(null, "No result from NetBoost (Grid) remote service."
									, NETBOOST_GRID, JOptionPane.ERROR_MESSAGE);
						}
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "No results from NetBoost (Grid): " + e.getMessage()
								, NETBOOST_GRID, JOptionPane.ERROR_MESSAGE);
						throw new RuntimeException("Error executing " + NETBOOST_GRID + e);
					} finally {
						pBar.stop();						
						log.info("NetBoost grid service is done: " + System.currentTimeMillis());
					}
				}

				return event;

			}
		}

		return null;
	}
}
