package org.geworkbench.components.gpmodule.gsea;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.genepattern.matrix.ClassVector;
import org.genepattern.matrix.DefaultClassVector;
import org.genepattern.webservice.Parameter;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.gsea.CSGSEAResultDataSet;
import org.geworkbench.bison.datastructure.biocollections.gsea.DSGSEAResultDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.gpmodule.GPAnalysis;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.ProgressBar;

/**
 * User: nazaire
 */
public class GSEAAnalysis extends GPAnalysis
{
    private static Log log = LogFactory.getLog(GSEAAnalysis.class);

	private GSEAProgress progress = new GSEAProgress();
    private DSGSEAResultDataSet gsResultDataSet;

    private Task task;
	private boolean error = false;

    public GSEAAnalysis()
    {
        panel = new org.geworkbench.components.gpmodule.gsea.GSEAAnalysisPanel();
        setDefaultPanel(panel);
        //ProjectPanel.setIconForType(DSGSEAResultDataSet.class, GP_ICON);
    }

    public AlgorithmExecutionResults execute(Object input)
    {
		assert (input instanceof DSMicroarraySetView);
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;

		task = new Task(view);
		progress.startProgress();
		task.execute();

		while (!task.isDone()) {
		}

		if (task.isCancelled()) {
			return null;
		} else if (gsResultDataSet == null) {
			if (error)
				return null;

			return new AlgorithmExecutionResults(false,
					"An error occurred when running GSEA.", null);
		} else
			return new AlgorithmExecutionResults(true, "GSEA Results",
                    gsResultDataSet);

	}

	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent event) {
		return event;
	}

	private class GSEAProgress implements Observer {
		private ProgressBar pb = ProgressBar
				.create(ProgressBar.INDETERMINATE_TYPE);

		public GSEAProgress() {
			pb.addObserver(this);
        }

		public void startProgress() {
            pb.setTitle("GSEA Progress");
            pb.setMessage("Running GSEA Analysis");

            pb.start();
		}

		public void stopProgress() {
            pb.setTitle("");
            pb.setMessage("");

            pb.stop();
		}

		public void update(java.util.Observable ob, Object o) {
			if ((task != null) && (!task.isCancelled()) && (!task.isDone())) {
				task.cancel(true);
				log.info("Cancelling GSEA Analysis");
			}
		}
	}

	public List runAnalysis(String analysisName, Parameter[] parameters,
			String password) {
		List result = null;
		try {
			result = super.runAnalysis(analysisName, parameters, password);
			if (gsResultDataSet == null)
				error = true;
		} catch (Exception e) {
			e.printStackTrace();
			task.cancel(true);
			error = true;
		}

		return result;
	}

	private class Task extends SwingWorker<String, Void> {
                  DSMicroarraySetView<DSGeneMarker, DSMicroarray> view;

                  public Task(DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
                      this.view = view;
                  }

                  public String doInBackground() {
                      String reportFile = null;
                      try
                      {
                          String history = generateHistoryString(view);

                          DSMicroarraySet maSet = view.getMicroarraySet();

                          DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);

                          DSPanel arraysByClass = new CSPanel();
                          Collection labels = new ArrayList();
                          for(int l = 0; l < context.getNumberOfLabels(); l++)
                          {
                              String label = context.getLabel(l);
                              if(context.isLabelActive(label))
                              {
                                  DSPanel dsPanel = context.getItemsWithLabel(label);
                                  Collection currentList = new ArrayList(arraysByClass);
                                  currentList.retainAll(dsPanel);
                                  if(currentList.size() != 0)
                                  {
                                      String message = "The following items were found in more than one active label: " + currentList;
                                      log.error(message);
                                      JOptionPane.showMessageDialog(panel, message);
                                      return null;
                                  }

                                  label = label.replaceAll(" ", "_");

                                  labels.addAll(Collections.nCopies(dsPanel.size(), label));
                                  arraysByClass.addAll(dsPanel);
                              }
                          }

                          String gctFileName = view.getMicroarraySet().getFile().getName();
                          gctFileName = gctFileName + System.currentTimeMillis();
                          File gctFile = createGCTFile(gctFileName, view.markers(), arraysByClass);
                          gctFile.deleteOnExit();
                          gctFileName = gctFile.getAbsolutePath();

                          List parameters = new ArrayList();

                          parameters.add(new Parameter("expression.dataset", gctFileName));

                          String clsFileName = gctFileName + System.currentTimeMillis();


                          ClassVector classVec = new DefaultClassVector((String[])labels.toArray(new String[0]));

                          File clsFile = createCLSFile(clsFileName, classVec);
                          
                          parameters.add(new Parameter("phenotype.labels", clsFile.getAbsolutePath()));

                          parameters.add(new Parameter("gene.sets.database", ((org.geworkbench.components.gpmodule.gsea.GSEAAnalysisPanel)panel).getGsDatabase()));
                          parameters.add(new Parameter("chip.platform", ((org.geworkbench.components.gpmodule.gsea.GSEAAnalysisPanel)panel).getChipPlatform()));

                          List<String> results = (List<String>)runAnalysis("GSEA", (Parameter[]) parameters
                              .toArray(new Parameter[0]), panel.getPassword());

                          if(results == null)
                          {
                              return null;
                          }
                          for(String file : results)
                          {
                              if(file.endsWith(".zip"))
                              {
                                    int htmlCount = 0;
                                    ZipFile zipFile = new ZipFile(file);
                                    Enumeration en = zipFile.getEntries();
                                    while(en.hasMoreElements())
                                    {
                                        ZipEntry entry = (ZipEntry)en.nextElement();
                                        if(entry.getName().endsWith(".html"))
                                        {
                                            htmlCount++;
                                        }
                                    }

                                    if(htmlCount == 1)
                                    {
                                        task.cancel(true);
                                    }
                                    reportFile = file;
                                    gsResultDataSet = new CSGSEAResultDataSet(view.getDataSet(), "GSEA Results", reportFile);
                                    ProjectPanel.addToHistory(gsResultDataSet, history);                                    
                              }
                              else
                              {
                                  (new File(file)).deleteOnExit();
                              }
                           }
                      }
                      catch(Exception e)
                      {
                          e.printStackTrace();
                          log.error(e);
                      }

                      return reportFile;
                  }

                  public void done() {
                      if (!this.isCancelled()) {
                          try {
                              log.debug("Transferring GSEA data set back to event thread.");
                          } catch (Exception e) {
                              log.error(
                                      "Exception in finishing up worker thread that called GSEA: "
                                              + e.getMessage(), e);
                          }
                      }
                      progress.stopProgress();
                      log.debug("Closing GSEA progress bar.");
                  }
              }

              private String generateHistoryString(
                      DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
                  String history = "";

                  history = "Generated by GSEA run with parameters: \n";
                  history += "----------------------------------------\n";
                  history += "Gene set Database: " + ((org.geworkbench.components.gpmodule.gsea.GSEAAnalysisPanel) panel).getGsDatabase()
                          + "\n";

                  if (view.useMarkerPanel() && !(view.getMarkerPanel().size() == 0)) {
                      DSAnnotationContext<DSGeneMarker> context = CSAnnotationContextManager
                              .getInstance().getCurrentContext(
                                      view.getMicroarraySet().getMarkers());

                      DSPanel<DSGeneMarker> mp = context.getActiveItems();
                      DSItemList panels = mp.panels();

                      if (!((DSPanel) panels.get(CSAnnotationContext.SELECTION))
                              .isActive()) {
                          panels.remove(panels.get(CSAnnotationContext.SELECTION));
                      }

                      history += "\n" + panels.size() + " marker sets activated: \n";

                      for (int i = 0; i < panels.size(); i++) {
                          DSPanel<DSGeneMarker> panel = (DSPanel) panels.get(i);
                          history += "\t Set " + panel.getLabel() + " (" + panel.size()
                                  + " markers):" + "\n";

                          for (DSGeneMarker marker : panel) {
                              history += "\t\t" + marker.getLabel() + "\n";
                          }
                      }
                  } else {
                      history += view.markers().size() + " markers analyzed:\n";
                      for (DSGeneMarker marker : view.markers()) {
                          history += "\t" + marker.getLabel() + "\n";
                      }
                  }

                  if (view.useItemPanel() && !(view.getItemPanel().size() == 0)) {
                      CSAnnotationContextManager manager = CSAnnotationContextManager
                              .getInstance();
                      CSAnnotationContext<DSMicroarray> context = (CSAnnotationContext) manager
                              .getCurrentContext(view.getMicroarraySet());

                      DSPanel<DSMicroarray> ap = context.getActiveItems();
                      DSItemList panels = ap.panels();

                      if (!((DSPanel) panels.get(CSAnnotationContext.SELECTION))
                              .isActive()) {
                          panels.remove(panels.get(CSAnnotationContext.SELECTION));
                      }

                      history += "\n" + panels.size() + " array sets activated: \n";

                      for (int i = 0; i < panels.size(); i++) {
                          DSPanel<DSMicroarray> panel = (DSPanel) panels.get(i);

                          history += "\t Set " + panel.getLabel() + " (" + panel.size()
                                  + " arrays):" + "\n";

                          for (DSMicroarray array : panel) {
                              history += "\t\t" + array.getLabel() + "\n";
                          }
                      }
                  }

                  else {
                      history += view.items().size() + " arrays analyzed:\n";
                      for (DSMicroarray array : view.items()) {
                          history += "\t" + array.getLabel() + "\n";
                      }
                  }

                  return history;
              }
          }
