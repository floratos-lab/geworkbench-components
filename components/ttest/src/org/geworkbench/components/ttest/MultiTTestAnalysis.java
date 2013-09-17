package org.geworkbench.components.ttest;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSTTestResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBarT;

/**
 * @author John Watkinson
 * @version $Id$
 */
public class MultiTTestAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = -940107149844721836L;

	private static class Indexable implements Comparable<Indexable> {

		private double[] data;
		private int index;

		public Indexable(double[] data, int index) {
			this.data = data;
			this.index = index;
		}

		public int compareTo(Indexable other) {
			if (data[index] > data[other.index]) {
				return 1;
			} else if (data[index] < data[other.index]) {
				return -1;
			} else {
				return 0;
			}
		}

	}

	private MultiTTestAnalysisPanel panel;
	boolean useroverride = false;
    boolean isLogNormalized = false;

	public MultiTTestAnalysis() {
		panel = new MultiTTestAnalysisPanel();
		setDefaultPanel(panel);
	}

	public int getAnalysisType() {
		return AbstractAnalysis.TTEST_TYPE;
	}

	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		assert (input instanceof DSMicroarraySetView);
		ProgressBarT pbMTtest = null;
		try {
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
			DSMicroarraySet maSet = view.getMicroarraySet();
			TTest tTest = new TTestImpl();
			// Get params
			Set<String> labelSet = panel.getLabels();
			double alpha = panel.getPValue();
			useroverride = panel.isUseroverride();
	        isLogNormalized = panel.isLogNormalized();
		        
			int m = labelSet.size();
			if (m < 2) {
				return new AlgorithmExecutionResults(false,
						"At least two panels must be selected for comparison.",
						null);
			}
			// todo - check that all selected panels have at least two elements
			int numTests = m * (m - 1) / 2;
			DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager
					.getInstance().getCurrentContext(maSet);
			String[] labels = labelSet.toArray(new String[m]);
			int n = view.markers().size();
			double[][] pValues = new double[n][numTests];
			int testIndex = 0;
			// Create panels and significant result sets to store results
			DSPanel<DSGeneMarker>[] panels = new DSPanel[numTests];
			DSSignificanceResultSet<DSGeneMarker>[] sigSets = new DSSignificanceResultSet[numTests];
			String[] groupAndChipsStringSets = new String[numTests];
			// todo - use a F-test to filter genes prior to finding significant
			// genes with Holm t Test
			// Run tests

			try {
				pbMTtest = ProgressBarT.create(org.geworkbench.util.ProgressBarT.BOUNDED_TYPE);
				pbMTtest.addObserver(this);
				pbMTtest.setTitle("Multi T Test Analysis");
				pbMTtest.setMessage("Processing ... " + numTests + " tests");
				pbMTtest
						.setBounds(new org.geworkbench.util.ProgressBarT.IncrementModel(
								0, numTests, 0, numTests, 1));
				pbMTtest.start();
				this.stopAlgorithm = false;

				pbMTtest.setType(ProgressBarT.BOUNDED_TYPE);
				for (int i = 0; i < m; i++) {
					String labelA = labels[i];
					DSPanel<DSMicroarray> panelA = context
							.getItemsWithLabel(labelA);
					int aSize = panelA.size();
					for (int j = i + 1; j < m; j++) {
						if (!this.stopAlgorithm) {
							String labelB = labels[j];
							DSPanel<DSMicroarray> panelB = context
									.getItemsWithLabel(labelB);
							int bSize = panelB.size();
							for (int k = 0; k < n; k++) {
								double[] a = new double[aSize];
								for (int aIndex = 0; aIndex < aSize; aIndex++) {
									a[aIndex] = panelA.get(aIndex)
											.getMarkerValue(k).getValue();
								}
								double[] b = new double[bSize];
								for (int bIndex = 0; bIndex < bSize; bIndex++) {
									b[bIndex] = panelB.get(bIndex)
											.getMarkerValue(k).getValue();
								}
								pValues[k][testIndex] = tTest.tTest(a, b);
							}
							String label = labelA + " vs. " + labelB;
							panels[testIndex] = new CSPanel<DSGeneMarker>(label);
							sigSets[testIndex] = new CSTTestResultSet<DSGeneMarker>(
									maSet, label, new String[] { labelA },
									new String[] { labelB }, alpha, isLogNormalized);

							groupAndChipsStringSets[testIndex] = label + "\n"
									+ GenerateGroupAndChipsString(panelA)
									+ GenerateGroupAndChipsString(panelB);

							testIndex++;
							pbMTtest.update();
						} else {
							pbMTtest.dispose();
							return null;
						}
					}
				}
				pbMTtest.setType(ProgressBarT.INDETERMINATE_TYPE);
				// Sort each set of pValues and then use Holm method to compute
				// significance
				for (int i = 0; i < n; i++) {
					if (!this.stopAlgorithm) {
						Indexable[] indices = new Indexable[numTests];
						for (int j = 0; j < numTests; j++) {
							indices[j] = new Indexable(pValues[i], j);
						}
						Arrays.sort(indices);
						for (int j = 0; j < numTests; j++) {
							int index = indices[j].index;
							double pValue = pValues[i][index];
							pValue = pValue * (numTests - j);
							// Is this a critical p-Value?
							if (pValue < alpha) {
								DSGeneMarker marker = view.markers().get(i);
								panels[index].add(marker);
								sigSets[index].setSignificance(marker, pValue);
							} else {
								// Consider no more tests after the first one
								// fails
								break;
							}
						}
					} else {
						pbMTtest.dispose();
						return null;
					}
				}

				//if (useroverride == false)             
	               //  guessLogNormalized(maSet);
				
				String histHeader = this.GenerateHistoryHeader(alpha);
				String markerString = GenerateMarkerString(view);
				// Add panels and sigsets
				
				
				for (int i = 0; i < numTests; i++) {
					if (!this.stopAlgorithm) {
						sigSets[i].sortMarkersBySignificance();

						// add to Dataset History
						HistoryPanel.addToHistory(sigSets[i], histHeader
								+ groupAndChipsStringSets[i] + markerString);
						
						setFoldChnage (maSet, sigSets[i]);     
						
						publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
								DSGeneMarker.class, panels[i],
								SubpanelChangedEvent.NEW));
						publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
								"Analysis Result", null, sigSets[i]));
					} else {
						pbMTtest.dispose();
						return null;
					}
				}
				pbMTtest.dispose();
				if (stopAlgorithm) {
					return null;
				}

			} catch (MathException me) {
				me.printStackTrace();
			}
		} catch (ClassCastException cce) {
			return null;
		}

		// todo
		return null;
	}

	@SuppressWarnings({ "rawtypes" })
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet dataSet = event.getDataSet();
		if ((dataSet != null) && (dataSet instanceof DSMicroarraySet)) {
			panel.setMaSet((DSMicroarraySet) dataSet);
			panel.rebuildForm();
		}
	}

	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(PhenotypeSelectorEvent event, Object source) {
		panel.rebuildForm();
	}

	@SuppressWarnings("rawtypes")
	@Publish
	public SubpanelChangedEvent publishSubpanelChangedEvent(
			SubpanelChangedEvent event) {
		return event;
	}

	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent event) {
		return event;
	}

	private String GenerateHistoryHeader(double alpha) {

		String histStr = "";
		// Header
		histStr += "Multi t Test run with parameters:\n";
		histStr += "----------------------------------------\n";

		histStr += "Critical P-Value: " + alpha + "\n";		
		 
		if ( isLogNormalized == true)
			histStr += "\t" + "isLogNormalized: true \n";
		else
			histStr += "\t" + "isLogNormalized: false \n";
		
		// group names and markers

		return histStr;
	}

	private String GenerateGroupAndChipsString(DSPanel<DSMicroarray> panel) {
		String histStr = null;

		histStr = "\tGroup " + panel.getLabel() + " (" + panel.size()
				+ " chips)" + ":\n";
		;

		int aSize = panel.size();
		for (int aIndex = 0; aIndex < aSize; aIndex++)
			histStr += "\t\t" + panel.get(aIndex) + "\n";

		return histStr;
	}

	private String GenerateMarkerString(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		String histStr = null;

		histStr = view.markers().size() + " markers analyzed:\n";
		for (DSGeneMarker marker : view.markers()) {
			histStr += "\t" + marker.getLabel() + "\n";
		}

		return histStr;

	}
	
	private void setFoldChnage(DSMicroarraySet set, DSSignificanceResultSet<DSGeneMarker> resultSet)
	{ 
                      
             String[] caseLabels = resultSet.getLabels(DSTTestResultSet.CASE);
             String[] controlLabels = resultSet.getLabels(DSTTestResultSet.CONTROL);
             DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(set);
             DSPanel<DSMicroarray> casePanel = new CSPanel<DSMicroarray>("Case");
             for (int i = 0; i < caseLabels.length; i++) {
                 String label = caseLabels[i];
                 casePanel.addAll(context.getItemsWithLabel(label));
             }
             casePanel.setActive(true);
             DSPanel<DSMicroarray> controlPanel = new CSPanel<DSMicroarray>("Control");
             for (int i = 0; i < controlLabels.length; i++) {
                 String label = controlLabels[i];
                 controlPanel.addAll(context.getItemsWithLabel(label));
             }
             casePanel.setActive(true);
             
             
             int numMarkers = resultSet.getSignificantMarkers().size();
             
             double minValue = Double.MAX_VALUE;
             for (int i = 0; i < numMarkers; i++) {
            	 DSGeneMarker marker = resultSet.getSignificantMarkers().get(i);
                 for (DSMicroarray microarray : casePanel) {
                     if (microarray.getMarkerValue(marker).getValue() < minValue) {
                         minValue = microarray.getMarkerValue(marker).getValue();
                     }
                 }

                 for (DSMicroarray microarray : controlPanel) {
                     if (microarray.getMarkerValue(marker).getValue() < minValue) {
                         minValue = microarray.getMarkerValue(marker).getValue();
                     }
                 }
                 
             }

             if (minValue < 0) {
                 // Minimum value adjust to get us above 0 values
                 minValue = Math.abs(minValue) + 1;
             } else {
                 minValue = 0;
             }

             
             
             for (int i = 0; i < numMarkers; i++) {
                     
            	    DSGeneMarker marker = resultSet.getSignificantMarkers().get(i);
                     // Calculate fold change
                     double caseMean = 0;
                     for (DSMicroarray microarray : casePanel) {
                         caseMean += microarray.getMarkerValue(marker).getValue();
                     }
                     caseMean = caseMean / casePanel.size() + minValue;

                     double controlMean = 0;
                     for (DSMicroarray microarray : controlPanel) {
                         controlMean += microarray.getMarkerValue(marker).getValue();
                     }
                     controlMean = controlMean / controlPanel.size() + minValue;

                     double fold_change = 0;
                     double ratio =0;
                     if (!isLogNormalized) {
                         ratio = caseMean / controlMean;
                         if (ratio < 0) {
                             
                        	 fold_change = -Math.log(-ratio) / Math.log(2.0);
                         } else {
                        	 fold_change = Math.log(ratio) / Math.log(2.0);
                         }
                     } else {;
                    	 fold_change = caseMean - controlMean;
                     }          
                                
                     
                     resultSet.setFoldChange(marker, fold_change);
                     
                 }
		  
	}
	 
	 
}
