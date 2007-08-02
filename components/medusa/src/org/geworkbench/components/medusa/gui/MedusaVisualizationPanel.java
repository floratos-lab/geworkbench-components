package org.geworkbench.components.medusa.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.medusa.MedusaData;
import org.geworkbench.components.medusa.MedusaUtil;
import org.geworkbench.events.SubpanelChangedEvent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.columbia.ccls.medusa.io.SerializedRule;

/**
 * This plugin sets the layout for the MEDUSA visualization.
 * 
 * @author keshav
 * @version $Id: MedusaVisualizationPanel.java,v 1.1 2007/06/15 17:12:31 keshav
 *          Exp $
 */
public class MedusaVisualizationPanel extends JPanel {

	private Log log = LogFactory.getLog(MedusaVisualizationPanel.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String defaultPath = "temp/medusa/dataset/output/";
	private String path = defaultPath + "run1/";

	private String rulesPath = path + "rules/";

	private List<String> rulesFiles = null;

	private Map<String, DSGeneMarker> dirtySelectedMarkerMap = null;
	private Map<String, DSGeneMarker> selectedMarkerMap = null;

	private JButton addSelectionsToSetButton = new JButton();
	private String addToSetButtonLabel = "Add To Set ";
	private JButton exportMotifsButton = new JButton();
	private JButton imageSnapshotButton = new JButton();

	// private static final int COLUMN_WIDTH = 80;

	/**
	 * 
	 * @param medusaData
	 */
	public MedusaVisualizationPanel(
			MedusaVisualComponent medusaVisualComponent, MedusaData medusaData) {
		super();

		final MedusaVisualComponent visualComponent = medusaVisualComponent;

		JTabbedPane tabbedPane = new JTabbedPane();

		/* MOTIF PANEL */
		JPanel motifPanel = new JPanel();

		int i = 0;
		List<DSGeneMarker> targets = medusaData.getTargets();

		List<DSGeneMarker> regulators = medusaData.getRegulators();

		initSelectedMarkerMap(targets, regulators);

		addSelectionsToSetButton.setText(addToSetButtonLabel + "["
				+ dirtySelectedMarkerMap.size() + "]");
		addSelectionsToSetButton.setToolTipText(addToSetButtonLabel);

		exportMotifsButton.setText("Export Motifs");
		exportMotifsButton.setToolTipText("Export motifs discovered by Medusa");
		imageSnapshotButton.setText("Image Snapshot");
		imageSnapshotButton.setToolTipText("Image snapshot");

		double[][] targetMatrix = new double[targets.size()][];
		for (DSGeneMarker target : targets) {
			double[] data = medusaData.getArraySet().getRow(target);
			targetMatrix[i] = data;
			i++;
		}

		int j = 0;
		double[][] regulatorMatrix = new double[regulators.size()][];
		for (DSGeneMarker regulator : regulators) {
			double[] data = medusaData.getArraySet().getRow(regulator);
			regulatorMatrix[j] = data;
			j++;
		}

		List<String> targetNames = new ArrayList<String>();
		for (DSGeneMarker marker : targets) {
			targetNames.add(marker.getLabel());
		}

		List<String> regulatorNames = new ArrayList<String>();
		for (DSGeneMarker marker : regulators) {
			regulatorNames.add(marker.getLabel());
		}

		motifPanel.setLayout(new GridLayout(3, 2));

		/* dummy panel at position 0,0 of the grid */
		JPanel dummyPanel0 = new JPanel();
		motifPanel.add(new JScrollPane());

		/* regulator heat map at postion 0,1 */
		DiscreteHeatMapPanel regulatorHeatMap = new DiscreteHeatMapPanel(
				regulatorMatrix, 1, 0, -1, regulatorNames, true);
		// motifPanel.add(regulatorHeatMap);
		regulatorHeatMap.setPreferredSize(new Dimension(200, 100));

		/* regulator labels at position 0,2 */
		FormLayout regulatorLabelLayout = new FormLayout("pref,60dlu", // columns
				"5dlu"); // add rows dynamically
		DefaultFormBuilder regulatorLabelBuilder = new DefaultFormBuilder(
				regulatorLabelLayout);
		regulatorLabelBuilder.nextRow();

		for (String name : regulatorNames) {
			final JCheckBox checkBox = new JCheckBox();
			checkBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					log.info("Regulator " + e.getActionCommand()
							+ " is selected? " + checkBox.isSelected());

					String markerLabel = e.getActionCommand();

					DSGeneMarker reg = selectedMarkerMap.get(markerLabel);

					if (checkBox.isSelected()) {
						dirtySelectedMarkerMap.put(markerLabel, reg);
						addSelectionsToSetButton.setText(addToSetButtonLabel
								+ "[" + dirtySelectedMarkerMap.size() + "]");
					}

					else {
						dirtySelectedMarkerMap.remove(markerLabel);
						addSelectionsToSetButton.setText(addToSetButtonLabel
								+ "[" + dirtySelectedMarkerMap.size() + "]");
					}
				}
			});
			checkBox.setText(name);
			checkBox.setSelected(true);
			regulatorLabelBuilder.append(checkBox);
			regulatorLabelBuilder.appendRow("10dlu");
		}

		JPanel regulatorHeatPanel = new JPanel(new BorderLayout());
		regulatorHeatPanel.add(regulatorHeatMap, BorderLayout.WEST);
		regulatorHeatPanel.add(regulatorLabelBuilder.getPanel(),
				BorderLayout.EAST);

		JScrollPane regulatorHeatScrollPane = new JScrollPane();
		regulatorHeatScrollPane.setPreferredSize(new Dimension(100, 200));
		regulatorHeatScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		regulatorHeatScrollPane.getViewport().add(regulatorHeatPanel);
		regulatorHeatScrollPane.setVisible(true);
		motifPanel.add(regulatorHeatScrollPane);
		// motifPanel.add(new JPanel());

		/* discrete hit or miss heat map at 1,0 */
		this.rulesFiles = new ArrayList<String>();

		for (int k = 0; k < medusaData.getMedusaCommand().getIter(); k++) {
			rulesFiles.add("rule_" + k + ".xml");
		}
		DiscreteHitOrMissHeatMapPanel hitOrMissPanel = new DiscreteHitOrMissHeatMapPanel(
				rulesPath, rulesFiles, targetNames, path);
		// motifPanel.add(hitOrMissPanel);
		hitOrMissPanel.setPreferredSize(new Dimension(200, 100));
		hitOrMissPanel.setParentPanel(tabbedPane);
		JScrollPane hitOrMissScrollPane = new JScrollPane();
		hitOrMissScrollPane.setPreferredSize(new Dimension(100, 200));
		hitOrMissScrollPane.getViewport().add(hitOrMissPanel);
		hitOrMissScrollPane.setVisible(true);
		motifPanel.add(hitOrMissScrollPane);

		/* target heat map at postion 1,1 */
		DiscreteHeatMapPanel targetHeatMap = new DiscreteHeatMapPanel(
				targetMatrix, 1, 0, -1, targetNames, true, 120);
		// motifPanel.add(targetHeatMap);
		targetHeatMap.setPreferredSize(new Dimension(200, 100));

		/* target labels at position 1,2 */
		FormLayout targetLabelLayout = new FormLayout("pref,60dlu", // columns
				"75dlu"); // add rows dynamically
		DefaultFormBuilder targetLabelBuilder = new DefaultFormBuilder(
				targetLabelLayout);
		targetLabelBuilder.nextRow();

		for (String name : targetNames) {
			final JCheckBox checkBox = new JCheckBox();
			checkBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					log.info("Target " + e.getActionCommand()
							+ " is selected? " + checkBox.isSelected());

					String markerLabel = e.getActionCommand();

					DSGeneMarker target = selectedMarkerMap.get(markerLabel);

					if (checkBox.isSelected()) {
						dirtySelectedMarkerMap.put(markerLabel, target);
						addSelectionsToSetButton.setText(addToSetButtonLabel
								+ "[" + dirtySelectedMarkerMap.size() + "]");
					} else {
						dirtySelectedMarkerMap.remove(markerLabel);
						addSelectionsToSetButton.setText(addToSetButtonLabel
								+ "[" + dirtySelectedMarkerMap.size() + "]");
					}
				}
			});
			checkBox.setText(name);
			checkBox.setSelected(true);
			targetLabelBuilder.append(checkBox);
			targetLabelBuilder.appendRow("10dlu");
		}
		// motifPanel.add(targetLabelBuilder.getPanel());
		JPanel targetHeatPanel = new JPanel(new BorderLayout());
		targetHeatPanel.add(targetHeatMap, BorderLayout.WEST);
		targetHeatPanel.add(targetLabelBuilder.getPanel(), BorderLayout.EAST);

		JScrollPane targetHeatScrollPane = new JScrollPane();
		// scrollPane1.setLayout(new BorderLayout());
		targetHeatScrollPane.setPreferredSize(new Dimension(100, 200));
		targetHeatScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		targetHeatScrollPane.getViewport().add(targetHeatPanel);
		targetHeatScrollPane.setVisible(true);
		motifPanel.add(targetHeatScrollPane);
		// motifPanel.add(new JPanel());

		/* dummy panel at 2,0 so we can align the buttons (below) */
		JPanel dummyPanel1 = new JPanel();
		motifPanel.add(dummyPanel1);

		/* add buttons at 2,1 */
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(imageSnapshotButton);
		buttonPanel.add(exportMotifsButton);
		buttonPanel.add(addSelectionsToSetButton);

		imageSnapshotButton.addActionListener(new ActionListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				visualComponent.publishImageSnapshot();
			}

		});

		exportMotifsButton.addActionListener(new ActionListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				ArrayList<SerializedRule> srules = MedusaUtil
						.getSerializedRules(rulesFiles, rulesPath);

				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));

				chooser.showSaveDialog(MedusaVisualizationPanel.this);
				File chosenFile = chooser.getSelectedFile();

				visualComponent.exportMotifs(srules, chosenFile
						.getAbsolutePath());

			}

		});

		addSelectionsToSetButton.addActionListener(new ActionListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				// add all values in selected selectedMarkerMap
				Collection<DSGeneMarker> selectedMarkers = dirtySelectedMarkerMap
						.values();
				DSPanel<DSGeneMarker> panel = new CSPanel<DSGeneMarker>();
				panel.addAll(selectedMarkers);
				visualComponent
						.publishSubpanelChangedEvent(new SubpanelChangedEvent(
								DSGeneMarker.class, panel,
								org.geworkbench.events.SubpanelChangedEvent.NEW));

			}
		});

		motifPanel.add(buttonPanel);

		tabbedPane.add("Motif", motifPanel);

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
		this.revalidate();

	}

	/**
	 * 
	 * @param targets
	 * @param regulators
	 * @return
	 */
	private void initSelectedMarkerMap(List<DSGeneMarker> targets,
			List<DSGeneMarker> regulators) {

		dirtySelectedMarkerMap = new HashMap<String, DSGeneMarker>();
		selectedMarkerMap = new HashMap<String, DSGeneMarker>();
		for (DSGeneMarker reg : regulators) {
			dirtySelectedMarkerMap.put(reg.getLabel(), reg);
			selectedMarkerMap.put(reg.getLabel(), reg);
		}
		for (DSGeneMarker tar : targets) {
			dirtySelectedMarkerMap.put(tar.getLabel(), tar);
			selectedMarkerMap.put(tar.getLabel(), tar);
		}
	}
}
