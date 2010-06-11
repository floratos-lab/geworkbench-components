package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.DefaultListCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.util.Util;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.components.interactions.cellularnetwork.Constants;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Min You
 * @version $Id$
 */

public class CellularNetworkPreferencePanel extends javax.swing.JPanel {
	private Log log = LogFactory.getLog(this.getClass());

	private PropertiesManager pm = null;

	private boolean isUserSelected = true;

	private JComboBox contextComboBox = new JComboBox();
	private JComboBox versionComboBox = new JComboBox();

	private JButton addButton;

	private JButton networkAddButton;

	private JList availableInteractionTypeList;

	private JList availableNetworkInteractionTypeList;

	private JButton removeButton;
	private JButton networkRemoveButton;

	private JButton changeButton;

	private JList selectedInteractionTypeList;
	private JList selectedNetworkInteractionTypeList;

	private preferenceJCheckBox markerJCheckBox = new preferenceJCheckBox(
			Constants.MARKERLABEL, true);
	private preferenceJCheckBox geneTypeLCheckBox = new preferenceJCheckBox(
			Constants.GENETYPELABEL, true);
	private preferenceJCheckBox geneJCheckBox = new preferenceJCheckBox(
			Constants.GENELABEL, true);
	private preferenceJCheckBox goTermJCheckBox = new preferenceJCheckBox(
			Constants.GOTERMCOLUMN, true);

	private JCheckBox networkJCheckBox1 = new JCheckBox(
			"Restrict to genes present in microarray set", false);
	private JCheckBox networkJCheckBox2 = new JCheckBox(
			"Use setting  from Column Display Preferences", true);

	private JLabel serviceLabel = null;

	JDialog changeServicesDialog = null;

	private List<String> contextList = new ArrayList<String>();
	private List<VersionDescriptor> versionList = new ArrayList<VersionDescriptor>();

	private List<String> allInteractionTypes = new ArrayList<String>();

	private List<String> displayAvailInteractionTypes = new ArrayList<String>();

	private List<String> displaySelectedInteractionTypes = new ArrayList<String>();

	private List<String> networkAvailInteractionTypes = new ArrayList<String>();

	private List<String> networkSelectedInteractionTypes = new ArrayList<String>();

	private CellularNetworkKnowledgeWidget c;

	/**
	 * Creates new form Interactions
	 */
	public CellularNetworkPreferencePanel(CellularNetworkKnowledgeWidget c) {

		pm = c.pm;
		this.c = c;
		initComponent();

	}

	public List<String> getAllInteractionTypes() {
		return this.allInteractionTypes;
	}

	public List<String> getDisplayAvailInteractionTypes() {
		return this.displayAvailInteractionTypes;
	}

	public List<String> getDisplaySelectedInteractionTypes() {
		return this.displaySelectedInteractionTypes;
	}

	public List<String> getNetworkAvailInteractionTypes() {
		return this.networkAvailInteractionTypes;
	}

	public List<String> getNetworkSelectedInteractionTypes() {
		return this.networkSelectedInteractionTypes;
	}

	public boolean isMarkerJCheckBoxSelected() {
		return this.markerJCheckBox.isSelected();
	}

	public boolean isGeneJCheckBoxSelected() {
		return this.geneJCheckBox.isSelected();
	}

	public boolean isGeneTypeLCheckBoxSelected() {
		return this.geneTypeLCheckBox.isSelected();
	}

	public boolean isGoTermJCheckBoxSelected() {
		return this.goTermJCheckBox.isSelected();
	}

	public boolean isNetworkJCheckBox1Selected() {
		return this.networkJCheckBox1.isSelected();
	}

	public boolean isNetworkJCheckBox2Selected() {
		return this.networkJCheckBox2.isSelected();
	}

	public JComboBox getContextComboBox() {
		return this.contextComboBox;
	}

	public JComboBox getVersionComboBox() {
		return this.versionComboBox;
	}

	/**
	 * The old method to create the GUI. It was generated by IDE than edited
	 * manually.
	 */

	private void initComponent() {

		availableInteractionTypeList = new javax.swing.JList();
		selectedInteractionTypeList = new javax.swing.JList();
		availableNetworkInteractionTypeList = new javax.swing.JList();
		selectedNetworkInteractionTypeList = new javax.swing.JList();
		addButton = new javax.swing.JButton();
		removeButton = new javax.swing.JButton();
		networkAddButton = new javax.swing.JButton();
		networkRemoveButton = new javax.swing.JButton();
		changeButton = new javax.swing.JButton();
		contextComboBox.setSize(60, 10);

		ListCellRenderer aRenderer = new VersionComboBoxRenderer();
		versionComboBox.setSize(80, 10);
		versionComboBox.setRenderer(aRenderer);

		contextComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {

				if (e.getStateChange() == ItemEvent.DESELECTED)
					return;

				Object selectedVersion = versionComboBox.getSelectedItem();
				String selectedCoxtext = Constants.SELECTCONTEXT;
				if (selectedVersion != null)
					selectedCoxtext = contextComboBox.getSelectedItem()
							.toString().split(" \\(")[0].trim();
				if (versionList == null)
					versionList = new ArrayList<VersionDescriptor>();
				versionList.clear();

				if (selectedCoxtext != Constants.SELECTCONTEXT) {
					InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
					try {
						versionList = interactionsConnection
								.getVersionDescriptor(selectedCoxtext);
					} catch (ConnectException ce) {
						JOptionPane
								.showMessageDialog(
										null,
										"No service running. Please check with the administrator of your service infrastructure.",
										"Error", JOptionPane.ERROR_MESSAGE);
					} catch (SocketTimeoutException se) {
						JOptionPane
								.showMessageDialog(
										null,
										"No service running. Please check with the administrator of your service infrastructure.",
										"Error", JOptionPane.ERROR_MESSAGE);

					} catch (IOException ie) {
						JOptionPane
								.showMessageDialog(
										null,
										"CNKB service has an internal error, Please contact with geWorkbench developer ...",
										"Error", JOptionPane.ERROR_MESSAGE);
					}

				}

				versionList.add(0, new VersionDescriptor(
						Constants.SELECTVERSION, false));
				versionComboBox.setModel(new DefaultComboBoxModel(versionList
						.toArray()));
				versionComboBox.revalidate();

			}

		});

		versionComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED)
					return;
				Object selectedVersion = versionComboBox.getSelectedItem();
				if (isUserSelected == false) {
					isUserSelected = true;
					return;
				}
				if (selectedVersion != null
						&& ((VersionDescriptor) selectedVersion).getVersion() != Constants.SELECTVERSION) {
					Vector<CellularNetWorkElementInformation> hits = c
							.getHits();
					for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits)
						cellularNetWorkElementInformation.setDirty(true);
				}

			}

		});

		networkJCheckBox2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				networkJCheckBox2_actionPerformed(e);
			}
		});

		availableInteractionTypeList.setModel(availableInteractionTypeModel);
		availableInteractionTypeList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					interactionTypeListHandler(evt,

					availableInteractionTypeList, selectedInteractionTypeList,
							displayAvailInteractionTypes,
							displaySelectedInteractionTypes,

							availableInteractionTypeModel,
							selectedInteractionTypeModel);
					if (networkJCheckBox2.isSelected() == true) {
						populatesNetworkPrefFromColumnPref();
					}
					c.updateColumnPref();
				}
			}
		});

		availableNetworkInteractionTypeList
				.setModel(availNetworkInteractionTypeModel);
		availableNetworkInteractionTypeList
				.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent evt) {

						if (evt.getClickCount() == 2
								&& networkJCheckBox2.isSelected() == false) {
							interactionTypeListHandler(evt,
									availableNetworkInteractionTypeList,
									selectedNetworkInteractionTypeList,
									networkAvailInteractionTypes,
									networkSelectedInteractionTypes,
									availNetworkInteractionTypeModel,
									selectedNetworkInteractionTypeModel);

						}

					}
				});

		selectedInteractionTypeList.setModel(selectedInteractionTypeModel);
		selectedInteractionTypeList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					interactionTypeListHandler(evt,
							selectedInteractionTypeList,
							availableInteractionTypeList,
							displaySelectedInteractionTypes,
							displayAvailInteractionTypes,
							selectedInteractionTypeModel,
							availableInteractionTypeModel);
					if (networkJCheckBox2.isSelected() == true) {
						populatesNetworkPrefFromColumnPref();
					}
					c.updateColumnPref();
				}
			}
		});

		selectedNetworkInteractionTypeList
				.setModel(selectedNetworkInteractionTypeModel);
		selectedNetworkInteractionTypeList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2
						&& networkJCheckBox2.isSelected() == false) {
					interactionTypeListHandler(evt,
							selectedNetworkInteractionTypeList,

							availableNetworkInteractionTypeList,
							networkSelectedInteractionTypes,

							networkAvailInteractionTypes,
							selectedNetworkInteractionTypeModel,
							availNetworkInteractionTypeModel);

				}

			}
		});

		addButton.setText(">>>");
		addButton.setToolTipText("Add to selection");
		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addRemoveButtonHandler(evt,

				availableInteractionTypeList, selectedInteractionTypeList,
						displayAvailInteractionTypes,
						displaySelectedInteractionTypes,

						availableInteractionTypeModel,
						selectedInteractionTypeModel);
				if (networkJCheckBox2.isSelected() == true) {
					populatesNetworkPrefFromColumnPref();
				}

				c.updateColumnPref();

			}
		});

		networkAddButton.setText(">>>");
		networkAddButton.setToolTipText("Add to selection");
		networkAddButton.setEnabled(false);
		networkAddButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addRemoveButtonHandler(evt,

				availableNetworkInteractionTypeList,
						selectedNetworkInteractionTypeList,
						networkAvailInteractionTypes,
						networkSelectedInteractionTypes,

						availNetworkInteractionTypeModel,
						selectedNetworkInteractionTypeModel);

			}
		});

		removeButton.setText("<<<");
		removeButton.setToolTipText("Remove From Selection");
		removeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addRemoveButtonHandler(evt, selectedInteractionTypeList,

				availableInteractionTypeList, displaySelectedInteractionTypes,

				displayAvailInteractionTypes, selectedInteractionTypeModel,
						availableInteractionTypeModel);
				if (networkJCheckBox2.isSelected() == true) {
					populatesNetworkPrefFromColumnPref();
				}

				c.updateColumnPref();

			}
		});

		networkRemoveButton.setText("<<<");
		networkRemoveButton.setToolTipText("Remove From Selection");
		networkRemoveButton.setEnabled(false);
		networkRemoveButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						addRemoveButtonHandler(evt,
								selectedNetworkInteractionTypeList,

								availableNetworkInteractionTypeList,
								networkSelectedInteractionTypes,

								networkAvailInteractionTypes,
								selectedNetworkInteractionTypeModel,
								availNetworkInteractionTypeModel);

					}
				});

		changeButton.setText("Change");
		changeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				changeButtonHandler(evt);
			}
		});

		setLayout(new BorderLayout());
		add(buildInteractionsDatabasePanel(), BorderLayout.NORTH);
		add(buildColumnDisplayPreferencesPanel(), BorderLayout.CENTER);
		add(buildNetworkGenerationPreferencesPanel(), BorderLayout.SOUTH);

		displaySelectedInteractionTypes.add(Constants.PROTEIN_DNA);
		displaySelectedInteractionTypes.add(Constants.PROTEIN_PROTEIN);
		readInteractionTypesProperties();

	}

	/**
	 * Respond to the select/unselect of Protein Protein interaction checkbox.
	 * 
	 * @param e
	 */
	public void networkJCheckBox2_actionPerformed(ActionEvent e) {
		if (networkJCheckBox2.isSelected() == true) {
			networkAddButton.setEnabled(false);
			networkRemoveButton.setEnabled(false);
			populatesNetworkPrefFromColumnPref();
		} else {
			networkAddButton.setEnabled(true);
			networkRemoveButton.setEnabled(true);
		}

	}

	/**
	 * 
	 * 
	 */
	private void readPreferences() {

		String isChecked = "";

		try {
			String contextProperty = pm.getProperty(this.getClass(),
					Constants.SELECTCONTEXT, "");
			if (!contextProperty.equals("")
					&& contextList.contains(contextProperty))
				contextComboBox.setSelectedItem(contextProperty);
			else if (!contextProperty.equals("")) {
				String context = contextProperty.split(" \\(")[0].trim();
				boolean needRemove = true;
				for (String cxt : contextList) {
					if (cxt.split(" \\(")[0].trim().equals(context)) {
						contextComboBox.setSelectedItem(cxt);
						needRemove = false;
						break;
					}

				}

				if (needRemove) {
					JOptionPane

							.showMessageDialog(
									null,
									"Database context: "
											+ contextProperty
											+ " is not in current database, so it is deleted from preference setting.",
									"Info", JOptionPane.INFORMATION_MESSAGE);
				}

			}
			String versionProperty = pm.getProperty(this.getClass(),
					Constants.SELECTVERSION, "");
			for (VersionDescriptor vd : versionList) {
				if (vd.getVersion().equals(versionProperty)) {
					if (!versionProperty.equals(Constants.SELECTVERSION))
						isUserSelected = false;
					;
					versionComboBox.setSelectedItem(vd);
					break;
				}
			}

			readInteractionTypesProperties();
			List<String> listNotInDatanase = new ArrayList<String>();
			for (String s : displaySelectedInteractionTypes) {
				if (displayAvailInteractionTypes.contains(s))
					displayAvailInteractionTypes.remove(s);
				else {
					if (!listNotInDatanase.contains(s))
						listNotInDatanase.add(s);

				}
			}

			for (String s : networkSelectedInteractionTypes) {
				if (networkAvailInteractionTypes.contains(s))
					networkAvailInteractionTypes.remove(s);
				else {
					if (!listNotInDatanase.contains(s))
						listNotInDatanase.add(s);

				}
			}
			if (listNotInDatanase.size() > 0) {
				displaySelectedInteractionTypes.removeAll(listNotInDatanase);
				networkSelectedInteractionTypes.removeAll(listNotInDatanase);
				if (listNotInDatanase.size() > 1)
					JOptionPane
							.showMessageDialog(
									null,
									"Interaction Types: "
											+ listNotInDatanase.toString()
											+ " are not in current database, so they are deleted from preference setting.",
									"Info", JOptionPane.INFORMATION_MESSAGE);
				else
					JOptionPane
							.showMessageDialog(
									null,
									"The Interaction Type: "
											+ listNotInDatanase.toString()
											+ " is not in current database, so it is deleted from preference setting.",
									"Info", JOptionPane.INFORMATION_MESSAGE);

			}

			isChecked = pm.getProperty(this.getClass(), Constants.MARKERLABEL,
					"");
			if (isChecked != null && isChecked.equalsIgnoreCase("false")) {
				markerJCheckBox.setSelected(false);
			} else if (isChecked != null && isChecked.equalsIgnoreCase("true")) {
				markerJCheckBox.setSelected(true);
			}
			isChecked = pm
					.getProperty(this.getClass(), Constants.GENELABEL, "");
			if (isChecked != null && isChecked.equalsIgnoreCase("false")) {
				geneJCheckBox.setSelected(false);
			} else if (isChecked != null && isChecked.equalsIgnoreCase("true")) {
				geneJCheckBox.setSelected(true);
			}
			isChecked = pm.getProperty(this.getClass(),
					Constants.GENETYPELABEL, "");
			if (isChecked != null && isChecked.equalsIgnoreCase("false")) {
				geneTypeLCheckBox.setSelected(false);
			} else if (isChecked != null && isChecked.equalsIgnoreCase("true")) {
				geneTypeLCheckBox.setSelected(true);
			}
			isChecked = pm.getProperty(this.getClass(), Constants.GOTERMCOLUMN,
					"");
			if (isChecked != null && isChecked.equalsIgnoreCase("false")) {
				goTermJCheckBox.setSelected(false);
			} else if (isChecked != null && isChecked.equalsIgnoreCase("true")) {
				goTermJCheckBox.setSelected(true);
			}

			isChecked = pm.getProperty(this.getClass(), Constants.GOTERMCOLUMN,
					"");
			if (isChecked != null && isChecked.equalsIgnoreCase("false")) {
				goTermJCheckBox.setSelected(false);
			} else if (isChecked != null && isChecked.equalsIgnoreCase("true")) {
				goTermJCheckBox.setSelected(true);
			}

			isChecked = pm.getProperty(this.getClass(), networkJCheckBox1
					.getText(), "");
			if (isChecked != null && isChecked.equalsIgnoreCase("true")) {
				networkJCheckBox1.setSelected(true);
			} else if (isChecked != null && isChecked.equalsIgnoreCase("false")) {
				networkJCheckBox1.setSelected(false);
			}

			isChecked = pm.getProperty(this.getClass(), networkJCheckBox2
					.getText(), "true");
			if (isChecked != null && isChecked.equalsIgnoreCase("false")) {
				networkJCheckBox2.setSelected(false);
				networkAddButton.setEnabled(true);
				networkRemoveButton.setEnabled(true);
			} else if (isChecked != null && isChecked.equalsIgnoreCase("true")) {
				networkJCheckBox2.setSelected(true);
				networkAddButton.setEnabled(false);
				networkRemoveButton.setEnabled(false);
			}

		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 */
	private void readInteractionTypesProperties() {

		String interactionStr = null;
		try {
			interactionStr = pm.getProperty(this.getClass(),
					Constants.DISPLAYSELECTEDINTERACTIONTYPE, null);
			if (interactionStr != null && !interactionStr.trim().equals("")) {
				displaySelectedInteractionTypes.clear();
				displaySelectedInteractionTypes
						.addAll(processInteractionStr(interactionStr.trim()));
			}
			interactionStr = pm.getProperty(this.getClass(),
					Constants.NETWORKSELECTEDINTERACTIONTYPE, null);
			if (interactionStr != null && !interactionStr.trim().equals("")) {
				networkSelectedInteractionTypes.clear();
				networkSelectedInteractionTypes
						.addAll(processInteractionStr(interactionStr.trim()));
			}

		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void interactionTypeListHandler(MouseEvent evt, JList jList1,
			JList jList2, List<String> types1, List<String> types2, ListModel

			listModel1, ListModel listModel2) {

		int index = jList1.locationToIndex(evt.getPoint());
		if (index >= 0) {
			String type = types1.get(index);
			types2.add(type);
			types1.remove(type);
			Collections.sort(types1);
			Collections.sort(types2);
			jList1.setModel(new DefaultListModel());
			jList1.setModel(listModel1);
			jList2.setModel(new DefaultListModel());
			jList2.setModel(listModel2);

		}

	}

	private void populatesNetworkPrefFromColumnPref() {

		networkAvailInteractionTypes.clear();
		networkAvailInteractionTypes.addAll(displayAvailInteractionTypes);
		networkSelectedInteractionTypes.clear();
		networkSelectedInteractionTypes.addAll(displaySelectedInteractionTypes);

		availableNetworkInteractionTypeList.setModel(new DefaultListModel());
		availableNetworkInteractionTypeList
				.setModel(availNetworkInteractionTypeModel);
		selectedNetworkInteractionTypeList.setModel(new DefaultListModel());
		selectedNetworkInteractionTypeList
				.setModel(selectedNetworkInteractionTypeModel);

	}

	private void addRemoveButtonHandler(ActionEvent e, JList jList1,
			JList jList2, List<String> types1, List<String> types2, ListModel

			listModel1, ListModel listModel2) {
		int[] indices = jList1.getSelectedIndices();
		if (indices != null && indices.length > 0) {
			Vector<String> types = new Vector<String>();
			for (int index : indices) {
				String aType = types1.get(index);
				types2.add(aType);
				types.add(aType);
			}
			for (String type : types) {
				types1.remove(type);
			}

			Collections.sort(types1);
			Collections.sort(types2);
			jList1.setModel(new DefaultListModel());
			jList1.setModel(listModel1);
			jList2.setModel(new DefaultListModel());
			jList2.setModel(listModel2);
		}

	}

	private void changeButtonHandler(ActionEvent e) {
		log.debug("changing url");

		String host = ResultSetlUtil.INTERACTIONS_SERVLET_URL;

		changeServicesDialog = new JDialog();

		DefaultFormBuilder indexServerPanelBuilder = new DefaultFormBuilder(
				new FormLayout("right:20dlu"));

		final JTextField hostField = new JTextField(host);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton submitButton = new JButton("Submit");
		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String urlStr = hostField.getText();
				Boolean isValidUrl = true;
				if (InteractionsConnectionImpl.isValidUrl(urlStr)) {
					ResultSetlUtil.setUrl(urlStr);
					serviceLabel.setText(urlStr);
					reInitPreferences();
				} else {
					isValidUrl = false;
				}

				changeServicesDialog.dispose();

				if (isValidUrl == false) {
					JOptionPane
							.showMessageDialog(
									null,
									"No service running. Please check with the administrator of your service infrastructure.",
									"Error", JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeServicesDialog.dispose();
			}
		});

		/* add to button panel */
		buttonPanel.add(submitButton);
		buttonPanel.add(cancelButton);

		/* the builder */
		indexServerPanelBuilder.appendColumn("5dlu");
		indexServerPanelBuilder.appendColumn("250dlu");

		indexServerPanelBuilder.append("URL", hostField);

		JPanel changeServicesPanel = new JPanel(new BorderLayout());
		changeServicesPanel.add(indexServerPanelBuilder.getPanel());
		changeServicesPanel.add(buttonPanel, BorderLayout.SOUTH);
		changeServicesDialog.add(changeServicesPanel);
		changeServicesDialog.setModal(true);
		changeServicesDialog.pack();
		Util.centerWindow(changeServicesDialog);
		changeServicesDialog.setVisible(true);

	}

	private void reInitPreferences() {

		try {

			savePreferences();

			contextList.clear();
			versionList.clear();
			allInteractionTypes.clear();
			displayAvailInteractionTypes.clear();
			displaySelectedInteractionTypes.clear();
			networkAvailInteractionTypes.clear();
			networkSelectedInteractionTypes.clear();

			initPreferences();
			c.initDetailTable();

		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	public void savePreferences() {

		try {

			if (allInteractionTypes.size() == 0)
				return;
			pm.setProperty(this.getClass(), "url",
					ResultSetlUtil.INTERACTIONS_SERVLET_URL);

			pm.setProperty(this.getClass(), Constants.SELECTCONTEXT,
					contextComboBox.getSelectedItem().toString());
			VersionDescriptor v = (VersionDescriptor) versionComboBox
					.getSelectedItem();
			String version = Constants.SELECTVERSION;
			if (v != null)
				version = v.getVersion();

			pm.setProperty(this.getClass(), Constants.SELECTVERSION, version);

			pm.setProperty(this.getClass(),
					Constants.DISPLAYSELECTEDINTERACTIONTYPE,
					displaySelectedInteractionTypes.toString());
			pm.setProperty(this.getClass(),
					Constants.NETWORKSELECTEDINTERACTIONTYPE,
					networkSelectedInteractionTypes.toString());

			if (!markerJCheckBox.isSelected())
				pm.setProperty(this.getClass(), Constants.MARKERLABEL, String
						.valueOf(false));
			else
				pm.setProperty(this.getClass(), Constants.MARKERLABEL, String
						.valueOf(true));
			if (!geneJCheckBox.isSelected())
				pm.setProperty(this.getClass(), Constants.GENELABEL, String
						.valueOf(false));
			else
				pm.setProperty(this.getClass(), Constants.GENELABEL, String
						.valueOf(true));
			if (!geneTypeLCheckBox.isSelected())
				pm.setProperty(this.getClass(), Constants.GENETYPELABEL, String
						.valueOf(false));
			else
				pm.setProperty(this.getClass(), Constants.GENETYPELABEL, String
						.valueOf(true));
			if (!goTermJCheckBox.isSelected())
				pm.setProperty(this.getClass(), Constants.GOTERMCOLUMN, String
						.valueOf(false));
			else
				pm.setProperty(this.getClass(), Constants.GOTERMCOLUMN, String
						.valueOf(true));

			if (!networkJCheckBox1.isSelected())
				pm.setProperty(this.getClass(), networkJCheckBox1.getText(),
						String.valueOf(false));
			else
				pm.setProperty(this.getClass(), networkJCheckBox1.getText(),
						String.valueOf(true));

			if (!networkJCheckBox2.isSelected())
				pm.setProperty(this.getClass(), networkJCheckBox2.getText(),
						String.valueOf(false));
			else
				pm.setProperty(this.getClass(), networkJCheckBox2.getText(),
						String.valueOf(true));

		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	public void initPreferences() {

		try {

			InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
			if (contextList != null)
				contextList.clear();
			try {
				contextList = interactionsConnection
						.getDatasetAndInteractioCount();
				allInteractionTypes = interactionsConnection
						.getInteractionTypes();
			} catch (ConnectException ce) {
				JOptionPane
						.showMessageDialog(
								null,
								"No service running. Please check with the administrator of your service infrastructure.",
								"Error", JOptionPane.ERROR_MESSAGE);
			} catch (SocketTimeoutException se) {
				JOptionPane
						.showMessageDialog(
								null,
								"No service running. Please check with the administrator of your service infrastructure.",
								"Error", JOptionPane.ERROR_MESSAGE);
			} catch (IOException ie) {
				JOptionPane
						.showMessageDialog(
								null,
								"CNKB service has an internal error, Please contact with geWorkbench developer ...",
								"Error", JOptionPane.ERROR_MESSAGE);

			}

			if (contextList != null) {
				contextList.add(0, Constants.SELECTCONTEXT);
			} else {
				contextList = new ArrayList<String>();
				contextList.add(Constants.SELECTCONTEXT);
			}

			if (versionList != null) {
				versionList.clear();
				versionList.add(0, new VersionDescriptor(
						Constants.SELECTVERSION, false));
			} else {
				versionList = new ArrayList<VersionDescriptor>();
				versionList.add(new VersionDescriptor(Constants.SELECTVERSION,
						false));
			}
			contextComboBox.setModel(new DefaultComboBoxModel(contextList
					.toArray()));

			versionComboBox.setModel(new DefaultComboBoxModel(versionList
					.toArray()));

			displaySelectedInteractionTypes.clear();
			networkSelectedInteractionTypes.clear();
			if (allInteractionTypes != null && allInteractionTypes.size() > 0) {

				Collections.sort(allInteractionTypes);
				CellularNetWorkElementInformation
						.setAllInteractionTypes(allInteractionTypes);
				displayAvailInteractionTypes.addAll(allInteractionTypes);
				networkAvailInteractionTypes
						.addAll(displayAvailInteractionTypes);
				displaySelectedInteractionTypes.add(Constants.PROTEIN_DNA);
				displaySelectedInteractionTypes.add(Constants.PROTEIN_PROTEIN);
				networkSelectedInteractionTypes.add(Constants.PROTEIN_DNA);
				networkSelectedInteractionTypes.add(Constants.PROTEIN_PROTEIN);
				readPreferences();
			}

			availableInteractionTypeList.setModel(new DefaultListModel());
			availableInteractionTypeList
					.setModel(availableInteractionTypeModel);
			availableNetworkInteractionTypeList
					.setModel(new DefaultListModel());
			availableNetworkInteractionTypeList
					.setModel(availNetworkInteractionTypeModel);
			selectedInteractionTypeList.setModel(new DefaultListModel());
			selectedInteractionTypeList.setModel(selectedInteractionTypeModel);
			selectedNetworkInteractionTypeList.setModel(new DefaultListModel());
			selectedNetworkInteractionTypeList
					.setModel(selectedNetworkInteractionTypeModel);

		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	ListModel availableInteractionTypeModel = new AbstractListModel() {
		public Object getElementAt(int index) {
			return displayAvailInteractionTypes.get(index);
		}

		public int getSize() {
			return displayAvailInteractionTypes.size();
		}
	};

	ListModel availNetworkInteractionTypeModel = new AbstractListModel() {
		public Object getElementAt(int index) {
			return networkAvailInteractionTypes.get(index);
		}

		public int getSize() {
			return networkAvailInteractionTypes.size();
		}
	};

	ListModel selectedInteractionTypeModel = new AbstractListModel() {
		public Object getElementAt(int index) {
			return displaySelectedInteractionTypes.get(index);
		}

		public int getSize() {
			return displaySelectedInteractionTypes.size();
		}
	};

	ListModel selectedNetworkInteractionTypeModel = new AbstractListModel() {
		public Object getElementAt(int index) {
			return networkSelectedInteractionTypes.get(index);
		}

		public int getSize() {
			return networkSelectedInteractionTypes.size();
		}
	};

	private class preferenceJCheckBox extends JCheckBox {

		public preferenceJCheckBox(String label, boolean isSelected) {

			super(label, isSelected);

			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					c.updateColumnPref();
				}
			});

		}
	}

	private class VersionComboBoxRenderer implements ListCellRenderer {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			String theText = null;

			JLabel renderer = (JLabel) defaultRenderer
					.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);

			VersionDescriptor v = (VersionDescriptor) value;
			if (v != null) {

				theText = v.getVersion();
				if (theText != null && theText.equalsIgnoreCase("null"))
					theText = " ";
				if (v.getRequiresAuthentication() == true) {
					renderer.setText("<html><font color=red><i>" + theText
							+ "</i></font></html>");
				} else
					renderer.setText(theText);

			}

			return renderer;
		}
	}

	private JPanel buildInteractionsDatabasePanel() {
		FormLayout layout = new FormLayout(
				"right:pref, 20dlu, left:pref, 3dlu, " + "left:pref, 7dlu",

				"");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("Interactions Database");
		builder.append(changeButton);
		// builder.append(urlLabel);
		serviceLabel = new JLabel("Url: "
				+ ResultSetlUtil.INTERACTIONS_SERVLET_URL);
		builder.append(serviceLabel);
		builder.nextLine();

		builder.append(contextComboBox);
		builder.append(versionComboBox);
		builder.nextLine();

		serviceLabel.setForeground(Color.BLUE);

		return builder.getPanel();

	}

	private JPanel buildColumnCheckBoxPanel() {
		FormLayout layout = new FormLayout(
				"left:pref, 20dlu, left:pref, 20dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.append(markerJCheckBox);
		builder.append(geneTypeLCheckBox);
		builder.nextLine();
		builder.append(geneJCheckBox);
		builder.append(goTermJCheckBox);
		builder.nextLine();

		return builder.getPanel();

	}

	private JPanel buildNetworkPrefCheckBoxPanel() {
		FormLayout layout = new FormLayout("left:pref", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.append(networkJCheckBox1);
		builder.nextLine();
		builder.append(networkJCheckBox2);
		builder.nextLine();

		return builder.getPanel();

	}

	private JPanel buildAddRemoveButtonPanel(JButton addButton,
			JButton removeButton) {
		FormLayout layout = new FormLayout("left:pref, 20dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.append(addButton);
		builder.nextLine();
		builder.append(removeButton);

		builder.nextLine();

		return builder.getPanel();

	}

	private JPanel buildJListPanel(String title, JList aJlist) {
		FormLayout layout = new FormLayout("left:pref", "");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();
		JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
		jScrollPane1
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setPreferredSize(new java.awt.Dimension(150, 90));
		jScrollPane1.setViewportView(aJlist);
		builder.append(new JLabel(title));
		builder.append(jScrollPane1);

		return builder.getPanel();

	}

	private JPanel buildColumnDisplayPreferencesPanel() {
		FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref, 3dlu, "
				+ "left:pref, 3dlu, left:pref, 3dlu",

		"");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("Column Display Preferences");
		builder.append(buildColumnCheckBoxPanel());
		builder.append(buildJListPanel("Available Interaction Types",
				availableInteractionTypeList));
		builder.append(buildAddRemoveButtonPanel(addButton, removeButton));
		builder.append(buildJListPanel("Selected Interaction Types",
				selectedInteractionTypeList));

		builder.nextLine();

		return builder.getPanel();

	}

	private JPanel buildNetworkGenerationPreferencesPanel() {
		FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref, 3dlu, "
				+ "left:pref, 3dlu, left:pref, 3dlu",

		"");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("Network Generation Preferences");

		builder.append(buildNetworkPrefCheckBoxPanel());
		builder.append(buildJListPanel("Available Interaction Types",
				availableNetworkInteractionTypeList));
		builder.append(buildAddRemoveButtonPanel(networkAddButton,
				networkRemoveButton));
		builder.append(buildJListPanel("Selected Interaction Types",
				selectedNetworkInteractionTypeList));

		builder.nextLine();

		return builder.getPanel();

	}

	private List<String> processInteractionStr(String interactionStr) {
		List<String> aList = new ArrayList<String>();
		interactionStr = interactionStr.substring(1,
				interactionStr.length() - 1);
		if (!interactionStr.trim().equals("")) {
			String[] tokens = interactionStr.split(",");
			for (int i = 0; i < tokens.length; i++) {
				aList.add(tokens[i].trim());
			}

		}

		return aList;
	}

}
