package org.geworkbench.components.sequenceretriever;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;

/**
 * Handle the selections at the MarkerList table.
 * 
 */
class SequenceListSelectionListener implements
		ListSelectionListener {

	final SequenceRetriever sequenceRetriever;
	
	SequenceListSelectionListener(SequenceRetriever sequenceRetriever) {
		this.sequenceRetriever = sequenceRetriever;
	}
	
	public void valueChanged(ListSelectionEvent e) {
		// Ignore extra messages.
		if (e.getValueIsAdjusting()) {
			return;
		}
		
		JList lsm = (JList) e.getSource();

		int[] selectedRows = lsm.getSelectedIndices();

		if(selectedRows.length == 0) {
			selectedRows = new int[lsm.getModel().getSize()];
			for(int i= 0; i<lsm.getModel().getSize(); i++) {
				selectedRows[i] = i;				
			}
		}
		
		sequenceRetriever.initializeRetrievedSequencesPanel();
		DefaultListModel ls2 = sequenceRetriever.getListModel();
		TreeMap<String, ArrayList<String>> currentRetrievedSequences = sequenceRetriever.getCurrentRetrievedSequences();
		HashMap<String, RetrievedSequenceView> currentRetrievedMap = sequenceRetriever.getCurrentRetrievedMap();
		
		CSSequenceSet<CSSequence> displaySequenceDB = new CSSequenceSet<CSSequence>();
		for (int i = 0; i < selectedRows.length; i++) {
			int index = selectedRows[i];

			if (ls2 != null && ls2.size() > index && index > -1) {

				DSGeneMarker marker = (DSGeneMarker) ls2.get(index);

				ArrayList<String> values = currentRetrievedSequences.get(marker
						.toString());
				if (values == null) {
					continue;
				} else {

					for (String o : values) {
						RetrievedSequenceView retrievedSequenceView = currentRetrievedMap
								.get(o);
						if (retrievedSequenceView != null
								&& retrievedSequenceView.getSequence() != null) {
							displaySequenceDB
									.addASequence(retrievedSequenceView
											.getSequence());
						}
					}
					displaySequenceDB.parseMarkers();
					sequenceRetriever.setDisplaySequenceDB(displaySequenceDB);
				}
			}
		}
	}

}