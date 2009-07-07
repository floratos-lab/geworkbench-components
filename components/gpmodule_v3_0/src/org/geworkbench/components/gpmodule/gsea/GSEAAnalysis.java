package org.geworkbench.components.gpmodule.gsea;

import org.geworkbench.components.gpmodule.GPAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.pca.CSPCADataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.threading.SwingWorker;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genepattern.webservice.Parameter;
import org.genepattern.matrix.ClassVector;
import org.genepattern.matrix.DefaultClassVector;

import java.util.Observer;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;

/**
 * User: nazaire
 */
public class GSEAAnalysis extends GPAnalysis
{
    private static Log log = LogFactory.getLog(GSEAAnalysis.class);

	private GSEAProgress progress = new GSEAProgress();
    private String reportFile = null;

    private Task task;
	private boolean error = false;

    public GSEAAnalysis()
    {
        setLabel("GSEA Analysis");
        panel = new GSEAAnalysisPanel();
        setDefaultPanel(panel);
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
		} else if (reportFile == null) {
			if (error)
				return null;

			return new AlgorithmExecutionResults(false,
					"An error occurred when running GSEA.", null);
		} else
			return new AlgorithmExecutionResults(true, "GSEA Results",
					reportFile);

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
			pb.setTitle("GSEA Progress");
		}

		public void setProgress(int value) {
			pb.setMessage("" + value);
		}

		public void startProgress() {
			pb.start();
		}

		public void stopProgress() {
			pb.stop();
		}

		public void update(java.util.Observable ob, Object o) {
			if ((task != null) && (!task.isCancelled()) && (!task.isDone())) {
				task.cancel(true);
				//log.info("Cancelling GSEA Analysis");
			}
		}
	}

	public List runAnalysis(String analysisName, Parameter[] parameters,
			String password) {
		List result = null;
		try {
			result = super.runAnalysis(analysisName, parameters, password);
			if (result == null)
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
			progress.setProgress(0);
			String history = generateHistoryString(view);

            String reportFile = null;

            DSMicroarraySet maSet = view.getMicroarraySet();
            //DSItemList<DSGeneMarker> markers = view.markers();

            DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);

            DSPanel<DSMicroarray> controlPanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CONTROL);            
            DSPanel<DSMicroarray> casePanel = context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CASE);

            DSPanel maByClass = new CSPanel();
            maByClass.addAll(controlPanel);
            maByClass.addAll(casePanel);

            String gctFileName = createGCTFile("gseaDataset", view.markers(), maByClass).getAbsolutePath();

            List parameters = new ArrayList();

			parameters.add(new Parameter("expression.dataset", gctFileName));

            //Depends on control labeled samples appearing before case labeled samples
            String[] classLabels = new String[maByClass.size()];
            for(int i = 0; i < maByClass.size(); i++)
            {
                if(i < controlPanel.size())
                    classLabels[i] = "Control";
                else
                    classLabels[i] = "Case";
            }

            ClassVector classVec = new DefaultClassVector(classLabels);
            File clsData = createCLSFile("GSEA_Cls", classVec);
            parameters.add(new Parameter("phenotype.labels", clsData.getAbsolutePath()));

            parameters.add(new Parameter("gene.sets.database", ((GSEAAnalysisPanel)panel).getGsDatabase()));
            parameters.add(new Parameter("chip.platform", ((GSEAAnalysisPanel)panel).getChipPlatform()));


            List results = runAnalysis("GSEA", (Parameter[]) parameters
					.toArray(new Parameter[0]), panel.getPassword());

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
			log.debug("Closing PCA progress bar.");
		}
	}

	private String generateHistoryString(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		String history = "";

		history = "Generated by GSEA run with parameters: \n";
		history += "----------------------------------------\n";
		history += "Gene set Database: " + ((GSEAAnalysisPanel) panel).getGsDatabase()
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
