package org.geworkbench.components.alignment.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MicroarraySetViewEvent;
import org.geworkbench.events.SubpanelChangedEvent;

/**
 * The only purpose of this class is to serve as the base class of
 * BlastAppComponent.
 * 
 * @author not attributable
 * @version $Id: CSSequenceSetViewEventBase.java,v 1.8 2009-06-05 15:52:00 jiz Exp $
 */
@SuppressWarnings("unchecked")
public class CSSequenceSetViewEventBase implements VisualPlugin {
	protected DSSequenceSet sequenceDB = null;
	protected CSSequenceSet activeSequenceDB = null;

	protected boolean activateMarkers = true;
	protected JCheckBox chkAllMarkers = new JCheckBox("All Markers");
	protected JPanel mainPanel;
	protected JToolBar displayToolBar;
	protected DSPanel<? extends DSGeneMarker> activatedMarkers = null;

	private JTextField sequenceNumberField;

	public CSSequenceSetViewEventBase() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * getComponent
	 * 
	 * @return Component
	 */
	public Component getComponent() {
		return mainPanel;
	}

	@Publish
	public SubpanelChangedEvent publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent event) {
		return event;
	}

	/**
	 * receiveProjectSelection
	 * 
	 * @param e -
	 *            ProjectEvent
	 */
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
			fireModelChangedEvent(null);
		} else {
			DSDataSet dataSet = e.getDataSet();
			if (dataSet instanceof DSSequenceSet) {
				if (sequenceDB != dataSet) {
					this.sequenceDB = (DSSequenceSet) dataSet;

					activatedMarkers = null;
				}
			}
		}
		refreshMaSetView();
	}

	/**
	 * geneSelectorAction
	 * 
	 * @param e
	 *            GeneSelectorEvent
	 */
	@Subscribe(Asynchronous.class)
	public void receive(GeneSelectorEvent e, Object source) {
		if (e.getPanel() != null && e.getPanel().size() > 0) {
			activatedMarkers = e.getPanel().activeSubset();
		} else {
			activatedMarkers = null;
		}
		refreshMaSetView();
	}

	protected void refreshMaSetView() {
		getDataSetView();
		fireModelChangedEvent(null);
	}

	protected void fireModelChangedEvent(MicroarraySetViewEvent event) {

	}

	private void jbInit() throws Exception {
		mainPanel = new JPanel();

		displayToolBar = new JToolBar();
		displayToolBar
				.setLayout(new BoxLayout(displayToolBar, BoxLayout.X_AXIS));
		chkAllMarkers.setToolTipText("Use All Markers.");
		chkAllMarkers.setSelected(false);
		chkAllMarkers.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				chkActivateMarkers_actionPerformed(e);
			}

		});

		BorderLayout borderLayout2 = new BorderLayout();
		mainPanel.setLayout(borderLayout2);
		sequenceNumberField = new JTextField(20);
		sequenceNumberField.setMaximumSize(sequenceNumberField.getPreferredSize());
		sequenceNumberField.setEditable(false);

		sequenceNumberField.setText("Total Sequence Number:");
		displayToolBar.add(chkAllMarkers, null);
		displayToolBar.add(Box.createHorizontalStrut(5), null);
		displayToolBar.add(sequenceNumberField);
		mainPanel.add(displayToolBar, java.awt.BorderLayout.SOUTH);
		this.activateMarkers = !chkAllMarkers.isSelected();

	}

	void chkActivateMarkers_actionPerformed(ActionEvent e) {
		activateMarkers = !((JCheckBox) e.getSource()).isSelected();
		refreshMaSetView();
	}

	private void getDataSetView() {
		activateMarkers = !chkAllMarkers.isSelected();
		if (activateMarkers) {
			if (activatedMarkers != null && activatedMarkers.size() > 0) {

				if (activateMarkers && (sequenceDB != null)) {
					activeSequenceDB = (CSSequenceSet) ((CSSequenceSet) sequenceDB)
							.getActiveSequenceSet(activatedMarkers);
					sequenceNumberField.setText("Activated Sequence Number: "
							+ activeSequenceDB.size());
				}

			} else if (sequenceDB != null) {
				sequenceNumberField.setText("Total Sequence Number: "
						+ sequenceDB.size());

				activeSequenceDB = (CSSequenceSet) sequenceDB;
			}

		} else if (sequenceDB != null) {
			sequenceNumberField.setText("Total Sequence Number: "
					+ sequenceDB.size());
		}

	}
}
