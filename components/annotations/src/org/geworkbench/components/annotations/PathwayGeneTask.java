package org.geworkbench.components.annotations;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressItem;
import org.geworkbench.util.ProgressTask;

/**
 * The ProgressTask to handle genes that belong to a given pathway.
 * 
 * $Id: ExportTask.java 10756 2013-08-15 17:56:38Z zji $
 */
public class PathwayGeneTask extends ProgressTask<GeneBase[], Void> {
	private static Log log = LogFactory.getLog(PathwayGeneTask.class);

	public static enum TaskType {
		ADD_TO_PROJECT, EXPORT
	};

	final private String pathway;
	final private AnnotationsPanel2 ap;
	final private TaskType taskType;
	final private String label;

	public PathwayGeneTask(String message, AnnotationsPanel2 ap2,
			TaskType taskType, String label, String pw) {
		super(ProgressItem.INDETERMINATE_TYPE, message);
		this.ap = ap2;
		this.pathway = pw;
		this.taskType = taskType;
		this.label = label;
	}

	@Override
	protected GeneBase[] doInBackground() {
		if (isCancelled())
			return null;
		BioDBnetClient client = new BioDBnetClient();
		return client.queryGenesForPathway(pathway);
	}

	@Override
	protected void done() {
		ap.pd.removeTask(this);
		if (isCancelled())
			return;
		GeneBase[] genesInPathway = null;
		try {
			genesInPathway = get();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (genesInPathway == null)
			return;

		switch (taskType) {
		case ADD_TO_PROJECT:
			addToProject(genesInPathway);
			break;
		case EXPORT:
			saveGenesInPathway(label, genesInPathway);
			break;
		}
	}

	private void addToProject(GeneBase[] genesInPathway) {
		DSPanel<DSGeneMarker> selectedMarkers = new CSPanel<DSGeneMarker>(
				label, label);
		for (int i = 0; i < genesInPathway.length; i++) {
			GeneBase gene = genesInPathway[i];
			log.info(gene.getGeneSymbol() + " : " + gene.getGeneName());
			for (Object obj : ap.maSet.getMarkers()) {
				DSGeneMarker marker = (DSGeneMarker) obj;
				if (marker.getShortName()
						.equalsIgnoreCase(gene.getGeneSymbol())) {
					log.debug("Found " + gene.getGeneSymbol() + " in set.");
					selectedMarkers.add(marker);
				}
			}
		}

		selectedMarkers.setActive(true);
		ap.publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
				DSGeneMarker.class, selectedMarkers,
				SubpanelChangedEvent.SET_CONTENTS));
	}

	private static void saveGenesInPathway(String label,
			GeneBase[] genesInPathway) {
		String filename = label;
		if (!filename.endsWith("csv")) {
			filename += ".csv";
		}
		try {
			FileWriter writer = new FileWriter(filename);
			for (int i = 0; i < genesInPathway.length; i++) {
				GeneBase gene = genesInPathway[i];
				writer.write(gene.getGeneSymbol() + ", " + gene.getGeneName()
						+ "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}