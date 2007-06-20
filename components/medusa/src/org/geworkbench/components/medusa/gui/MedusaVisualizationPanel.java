package org.geworkbench.components.medusa.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.medusa.MedusaData;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

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

	private String path = "temp/medusa/dataset/output/run1/";

	private String rulesPath = path + "rules/";

	private List<String> rulesFiles = null;

	private Map<String, DSGeneMarker> selectedMarkerMap = null;

	/**
	 * 
	 * @param medusaData
	 */
	public MedusaVisualizationPanel(MedusaData medusaData) {
		super();

		final MedusaData mData = medusaData;

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel motifPanel = new JPanel();

		int i = 0;
		List<DSGeneMarker> targets = medusaData.getTargets();

		List<DSGeneMarker> regulators = medusaData.getRegulators();

		selectedMarkerMap = initSelectedMarkerMap(targets, regulators);

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

		motifPanel.setLayout(new GridLayout(2, 3));

		/* dummy panel at position 0,0 of the grid */
		JPanel dummyPanel = new JPanel();
		motifPanel.add(dummyPanel);

		/* regulator heat map at postion 0,1 */
		DiscreteHeatMapPanel regulatorHeatMap = new DiscreteHeatMapPanel(
				regulatorMatrix, 1, 0, -1, regulatorNames, true);
		motifPanel.add(regulatorHeatMap);

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

					if (checkBox.isSelected())
						selectedMarkerMap.put(markerLabel, reg);

					else
						selectedMarkerMap.remove(markerLabel);
				}
			});
			checkBox.setText(name);
			checkBox.setSelected(true);
			regulatorLabelBuilder.append(checkBox);
			regulatorLabelBuilder.appendRow("10dlu");
		}
		motifPanel.add(regulatorLabelBuilder.getPanel());

		/* discrete hit or miss heat map */
		this.rulesFiles = new ArrayList<String>();

		for (int k = 0; k < medusaData.getMedusaCommand().getIter(); k++) {
			rulesFiles.add("rule_" + k + ".xml");
		}
		DiscreteHitOrMissHeatMapPanel hitOrMissPanel = new DiscreteHitOrMissHeatMapPanel(
				rulesPath, rulesFiles, targetNames, path);
		motifPanel.add(hitOrMissPanel);

		/* target heat map at postion 1,1 */
		DiscreteHeatMapPanel targetHeatMap = new DiscreteHeatMapPanel(
				targetMatrix, 1, 0, -1, targetNames, true, 120);
		motifPanel.add(targetHeatMap);

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

					if (checkBox.isSelected())
						selectedMarkerMap.put(markerLabel, target);

					else
						selectedMarkerMap.remove(markerLabel);
				}
			});
			checkBox.setText(name);
			checkBox.setSelected(true);
			targetLabelBuilder.append(checkBox);
			targetLabelBuilder.appendRow("10dlu");
		}
		motifPanel.add(targetLabelBuilder.getPanel());

		// TODO add back in
		// JScrollPane scrollPane = new JScrollPane(motifPanel,
		// ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
		// ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// tabbedPane.add("Motif", scrollPane);
		tabbedPane.add("Motif", motifPanel);

		JPanel pssmPanel = new JPanel();
		tabbedPane.add("PSSM", pssmPanel);

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
	private Map<String, DSGeneMarker> initSelectedMarkerMap(
			List<DSGeneMarker> targets, List<DSGeneMarker> regulators) {
		Map<String, DSGeneMarker> selectedMarkerMap = new HashMap<String, DSGeneMarker>();
		for (DSGeneMarker reg : regulators) {
			selectedMarkerMap.put(reg.getLabel(), reg);
		}
		for (DSGeneMarker tar : targets) {
			selectedMarkerMap.put(tar.getLabel(), tar);
		}
		return selectedMarkerMap;
	}
}
