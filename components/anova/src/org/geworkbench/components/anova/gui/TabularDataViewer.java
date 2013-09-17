package org.geworkbench.components.anova.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSAnovaResultSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * TabularViewer for ANOVA result.
 * 
 * @author Mark Chiang
 * @version $Id$
 */
@AcceptTypes({ DSAnovaResultSet.class })
public class TabularDataViewer extends JPanel implements VisualPlugin {
	private static final long serialVersionUID = 2021859129692430268L;

	private DSAnovaResultSet<? extends DSGeneMarker> anovaResultSet;
	private TableViewer TV = null;

	// preferences
	private boolean fStat = true;
	private boolean pVal = true;
	private boolean mean = true;
	private boolean std = true;
	private String[] header;
	private DispPref DP = null; // Panel for "Display Preferences", make it
								// global so it won't popup multiple times.

	public TabularDataViewer() {
		this.setLayout(new BorderLayout());

		// add two buttons "Display Preferences" and "Export" on the top
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		add(topPanel, java.awt.BorderLayout.NORTH);
		JButton PrefButton = new JButton("Display Preferences");
		JButton exportButton = new JButton("Export");
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(PrefButton);
		topPanel.add(Box.createRigidArea(new Dimension(35,0)));
		topPanel.add(exportButton);
		topPanel.add(Box.createHorizontalGlue());

		PrefButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (DP == null) {
					DP = new DispPref();
				} else {
					DP.setVisible(true);
					DP.toFront();
					DP.requestFocus();
					DP.requestFocusInWindow();
				}
			}
		});

		exportButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				export();
			}
		});

		TV = new TableViewer();
		add(TV);
	}

	/**
	 * This method fulfills the contract of the {@link VisualPlugin} interface.
	 * It returns the GUI component for this visual plugin.
	 */
	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}

	/**
	 * This is a <b>Subscribe</b> method. The annotation before the method
	 * alerts the engine that it should route published objects to this method.
	 * The type of objects that are routed to this method are indicated by the
	 * first parameter of the method. In this case, it is {@link ProjectEvent}.
	 * 
	 * @param event
	 *            the received object.
	 * @param source
	 *            the entity that published the object.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<? extends DSBioObject> dataSet = event.getDataSet();

		// We will act on this object if it is a DSMicroarraySet
		if (dataSet instanceof DSAnovaResultSet) {
			anovaResultSet = (DSAnovaResultSet) dataSet;
			refreshTableViewer();
		}
	}

	/*
	 * This function fill the array A, and generate a new TableViewer to show in
	 * the UI.
	 */
	private void refreshTableViewer() {
		int groupNum = anovaResultSet.getLabels(0).length;
		int meanStdStartAtIndex = 1 + (fStat ? 1 : 0) + (pVal ? 1 : 0);
		header = new String[meanStdStartAtIndex + groupNum
				* ((mean ? 1 : 0) + (std ? 1 : 0))];
		int fieldIndex = 0;
		header[fieldIndex++] = "Marker Name";
		if (pVal) {
			header[fieldIndex++] = "P-Value";
		}
		if (fStat) {
			header[fieldIndex++] = "F-statistic";
		}
		for (int cx = 0; cx < groupNum; cx++) {
			if (mean) {
				header[meanStdStartAtIndex + cx
						* ((mean ? 1 : 0) + (std ? 1 : 0)) + 0] = anovaResultSet
						.getLabels(0)[cx] + "_Mean";
			}
			if (std) {
				header[meanStdStartAtIndex + cx
						* ((mean ? 1 : 0) + (std ? 1 : 0)) + (mean ? 1 : 0)] = anovaResultSet
						.getLabels(0)[cx] + "_Std";
			}
		}

		Object[][] A = new Object[anovaResultSet.getSignificantMarkers().size()][header.length];

		double[][] result2DArray = anovaResultSet.getResult2DArray();
		int significantMarkerNumbers = anovaResultSet.getSignificantMarkers()
				.size();
		for (int cx = 0; cx < significantMarkerNumbers; cx++) {
			fieldIndex = 0;
			A[cx][fieldIndex++] = ((DSGeneMarker) anovaResultSet
					.getSignificantMarkers().get(cx)).getShortName();
			if (pVal) {
				A[cx][fieldIndex++] = new Float(result2DArray[0][cx]);
			}
			if (fStat) {
				A[cx][fieldIndex++] = result2DArray[2][cx];
			}
			for (int gc = 0; gc < groupNum; gc++) {
				if (mean) {
					A[cx][meanStdStartAtIndex + gc
							* ((mean ? 1 : 0) + (std ? 1 : 0)) + 0] = result2DArray[3 + gc * 2][cx];
				}
				if (std) {
					A[cx][meanStdStartAtIndex + gc
							* ((mean ? 1 : 0) + (std ? 1 : 0)) + (mean ? 1 : 0)] = result2DArray[4 + gc * 2][cx];
				}
			}
			Thread.yield();
		}

		remove(TV);
		TV = new TableViewer(header, A);
		TV.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		add(TV, java.awt.BorderLayout.CENTER);
		TV.updateUI();
	}

	/*
	 * This function popup a file chooser and save the table as a CSV file using
	 * that file name
	 */
	private void export() {
		JFileChooser jFC = new JFileChooser();

		// We remove "all files" from filter, since we only allow CSV format
		FileFilter ft = jFC.getAcceptAllFileFilter();
		jFC.removeChoosableFileFilter(ft);

		TabularFileFilter filter = new TabularFileFilter();
		jFC.setFileFilter(filter);

		int returnVal = jFC.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				String tabFilename;
				tabFilename = jFC.getSelectedFile().getAbsolutePath();
				if (!tabFilename.toLowerCase().endsWith(
						"." + filter.getExtension().toLowerCase())) {
					tabFilename += "." + filter.getExtension();
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(
						tabFilename));
				out.write(this.toCVS());
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String toCVS() {
		String answer = "";

		boolean newLine = true;

		for (int cx = 0; cx < TV.getTable().getColumnCount(); cx++) {
			if (newLine) {
				newLine = false;
			} else {
				answer += ",";
			}
			answer += "\"" + TV.getTable().getColumnName(cx) + "\"";
		}
		answer += "\n";
		newLine = true;

		// print the table
		for (int cx = 0; cx < TV.getTable().getRowCount(); cx++) {
			for (int cy = 0; cy < TV.getTable().getColumnCount(); cy++) {
				if (newLine) {
					newLine = false;
				} else {
					answer += ",";
				}
				answer += "\"" + TV.getTable().getValueAt(cx, cy) + "\"";
			}
			answer += "\n";
			newLine = true;
		}
		return answer;
	}

	/*
	 * This is a JDialog box which shows the options for user to check or
	 * uncheck. When user check a checkbox, preferences variables will be
	 * changed and refreshTableViewer will be called to redraw the table.
	 */
	private class DispPref extends JDialog {
		private static final long serialVersionUID = 7984636352410334067L;

		private JCheckBox bF = new JCheckBox("F-Statistic");
		private JCheckBox bP = new JCheckBox("P-Value");
		private JCheckBox bM = new JCheckBox("Mean");
		private JCheckBox bS = new JCheckBox("Std");

		public DispPref() {
			bF.setSelected(fStat);
			bP.setSelected(pVal);
			bM.setSelected(mean);
			bS.setSelected(std);
			
			FormLayout layout = new FormLayout(
					"right:max(80dlu;pref), 3dlu, max(70dlu;pref), 3dlu, max(70dlu;pref), 3dlu, max(70dlu;pref)",
					"");

			DefaultFormBuilder builder = new DefaultFormBuilder(layout);
			builder.setDefaultDialogBorder();
			builder.appendSeparator("Select the columns to display in the Tabular View");

			ItemListener toggleChangeListener = new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					Object source = e.getItemSelectable();

					boolean newStatus = (e.getStateChange() == ItemEvent.SELECTED);
			        if (source == bF) {
			        	fStat = newStatus;
			        } else if (source == bP) {
			        	pVal = newStatus;
			        } else if (source == bM) {
			        	mean = newStatus;
			        } else if (source == bS) {
			        	std = newStatus;
			        }
					refreshTableViewer();
				}
			};

			bF.addItemListener(toggleChangeListener);
			bP.addItemListener(toggleChangeListener);
			bM.addItemListener(toggleChangeListener);
			bS.addItemListener(toggleChangeListener);

			builder.append(new JLabel());
			builder.append(bF);
			builder.append(bP);
			builder.nextLine();
			builder.append(new JLabel());
			builder.append(bM);
			builder.append(bS);

			setTitle("Display Preferences");
			add(builder.getPanel());
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}
	}

	private static class TabularFileFilter extends FileFilter {
		public String getDescription() {
			return "CSV Files";
		}

		public boolean accept(File f) {
			String name = f.getName();
			boolean tabFile = name.endsWith("csv") || name.endsWith("CSV");
			if (f.isDirectory() || tabFile) {
				return true;
			}

			return false;
		}

		public String getExtension() {
			return "csv";
		}

	}

}
