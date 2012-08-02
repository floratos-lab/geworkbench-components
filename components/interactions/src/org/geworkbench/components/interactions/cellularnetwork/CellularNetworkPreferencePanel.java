package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener; 
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox; 
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListSelectionModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.UnAuthenticatedException;
import org.geworkbench.util.Util;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
 

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Min You
 * @version $Id$
 */

public class CellularNetworkPreferencePanel extends javax.swing.JPanel {
 
	private static final long serialVersionUID = 812513468056544393L;

	private Log log = LogFactory.getLog(this.getClass());

	private boolean isUserSelected = true;
	private boolean isFirstCaught = true;
	
	private Integer currentContextIndex = null;
	private Integer currentVersionIndex = null;

	private JList contextJList;
	private JList versionJList;

	private JTextArea interactomeJTextArea;
	private JTextArea versionJTextArea;

	private JButton addButton;

	private JButton networkAddButton;

	private JList availableInteractionTypeList;

	private JList availableNetworkInteractionTypeList;

	private JButton removeButton;
	private JButton networkRemoveButton;

	private JButton changeButton;
	
	private JButton exportButton;

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

	private List<String> contextList = new ArrayList<String>();
	private List<VersionDescriptor> versionList = new ArrayList<VersionDescriptor>();

	private List<String> allInteractionTypes = new ArrayList<String>();

	private List<String> displayAvailInteractionTypes = new ArrayList<String>();

	private List<String> displaySelectedInteractionTypes = new ArrayList<String>();

	private List<String> networkAvailInteractionTypes = new ArrayList<String>();

	private List<String> networkSelectedInteractionTypes = new ArrayList<String>();	
	
	
	
	private CellularNetworkKnowledgeWidget c;
	
	private HashMap<String, HashMap<String, List<String>>> selectedInteractionTypeMap;

	public static Map<String, String> interactionTypeSifMap = null; 
	public static Map<String, String> interactionEvidenceMap = null; 
	public static Map<String, String> interactionConfidenceTypeMap = null; 
	
	
	/**
	 * Creates new form Interactions
	 */
	public CellularNetworkPreferencePanel(CellularNetworkKnowledgeWidget c) {

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

	public JList getContextJList() {
		return this.contextJList;
	}

	public String getSelectedContext() {
		String context = null;
		if (getContextJList().getSelectedValue() != null) {
			context = getContextJList()
					.getSelectedValue().toString().split(" \\(")[0]
					.trim();
		}
		
		return context;
	}
	
	public JList getVersionJList() {
		return this.versionJList;
	}

	public String getSelectedVersion() {
		String version = null;
		if (getVersionJList().getSelectedValue() != null)
			version = ((VersionDescriptor)getVersionJList().getSelectedValue())
					.getVersion();

		
		return version;
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
		selectedInteractionTypeMap = new HashMap<String,HashMap<String, List<String>>>();
		interactionTypeSifMap = new HashMap<String, String>();
		interactionEvidenceMap = new HashMap<String, String>();
		interactionConfidenceTypeMap = new HashMap<String, String>();
		
		addButton = new javax.swing.JButton();
		removeButton = new javax.swing.JButton();
		networkAddButton = new javax.swing.JButton();
		networkRemoveButton = new javax.swing.JButton();
		changeButton = new javax.swing.JButton();
		exportButton = new javax.swing.JButton();
		
     
		contextJList = new JList();
		contextJList.setSize(80, 10);
		interactomeJTextArea = new JTextArea();

		ListCellRenderer aRenderer = new VersionComboBoxRenderer();
		versionJList = new JList();
		versionJList.setSize(80, 10);
		versionJList.setCellRenderer(aRenderer);

		versionJTextArea = new JTextArea();

		contextJList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				String interactomeDesc = "";

				if (contextJList.getSelectedValue() == null
						|| contextJList.getSelectedValue().toString().trim()
								.equals(""))
					return;
				String selectedCoxtext = contextJList.getSelectedValue()
						.toString().split(" \\(")[0].trim();
				
				if (c.isQueryRuning()) {
					if (isFirstCaught) {
						String theMessage = "You can not change interactome during a query run.";

						JOptionPane.showMessageDialog(null, theMessage,

						"Information", JOptionPane.INFORMATION_MESSAGE);
						isFirstCaught = false;
						contextJList.setSelectedIndex(currentContextIndex);
					    
					} else
						isFirstCaught = true;
					return;
				}
				
				
				if (versionList == null)
					versionList = new ArrayList<VersionDescriptor>();
				versionList.clear();
				versionJTextArea.setText("");

				if (selectedCoxtext != null
						&& !selectedCoxtext.trim().equals("")) {
					currentContextIndex = contextJList.getSelectedIndex();
					InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
					try {
						interactomeDesc = interactionsConnection
								.getInteractomeDescription(selectedCoxtext);
						versionList = interactionsConnection
								.getVersionDescriptor(selectedCoxtext);
					} catch (UnAuthenticatedException uae) {
						JOptionPane
								.showMessageDialog(
										null,
										"Unauthenticated Exception caught. Please check with the administrator of CNKB servlet.",
										"Error", JOptionPane.ERROR_MESSAGE);
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

				interactomeJTextArea.setText(interactomeDesc);
				interactomeJTextArea.setCaretPosition(0);
				versionJList.setModel(new DefaultComboBoxModel(versionList
						.toArray()));

			}

		});

		versionJList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				 
				Object selectedVersion = versionJList.getSelectedValue();
				String context = null;
				String version = null;

				if (selectedVersion != null
						&& selectedVersion instanceof VersionDescriptor) {
					if (c.isQueryRuning()) {
						if (isFirstCaught) {
							String theMessage = "You can not change interactome version during a query run.";

							JOptionPane.showMessageDialog(null, theMessage,

							"Information", JOptionPane.INFORMATION_MESSAGE);
							isFirstCaught = false;
							versionJList.setSelectedIndex(currentVersionIndex);
						    
						} else
							isFirstCaught = true;
						return;
					}
					
					
					context = contextJList.getSelectedValue().toString().split(
							" \\(")[0].trim();
					version = ((VersionDescriptor) selectedVersion)
							.getVersion();
					String versionDesc = ((VersionDescriptor) selectedVersion)
					.getVersionDesc(); 
					if (versionDesc == null || versionDesc.trim().equalsIgnoreCase("null"))
						versionDesc = "";
							 
					versionJTextArea.setText(versionDesc);
					versionJTextArea.setCaretPosition(0);
					currentVersionIndex = versionJList.getSelectedIndex();
				} else
					return;
				InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
				try {
					
					if (CellularNetWorkElementInformation.getConfidenceTypeList() != null)
					   CellularNetWorkElementInformation.clearConfidenceTypes();;
					 
					displayAvailInteractionTypes = interactionsConnection
							.getInteractionTypesByInteractomeVersion(context,
									version);
					Collections.sort(displayAvailInteractionTypes);
					networkAvailInteractionTypes.clear();
					networkAvailInteractionTypes
							.addAll(displayAvailInteractionTypes);

				} catch (UnAuthenticatedException uae) {
					JOptionPane
							.showMessageDialog(
									null,
									"Unauthenticated Exception caught. Please check with the administrator of CNKB servlet.",
									"Error", JOptionPane.ERROR_MESSAGE);
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

				if (isUserSelected == false) {
					removeSelectedInteractionType();
					refreshListModel();					
					isUserSelected = true;
					
					return;
				}
				
				displaySelectedInteractionTypes.clear();
				networkSelectedInteractionTypes.clear();
				HashMap<String, List<String>> selectedInteractionTypeLists = selectedInteractionTypeMap.get(context+Constants.DEL+version);
				if (selectedInteractionTypeLists != null)
				{
					List<String> list1 = selectedInteractionTypeLists.get(Constants.DISPLAYSELECTEDINTERACTIONTYPE);
					if (list1 != null)
						displaySelectedInteractionTypes.addAll(list1);
					List<String> list2 = selectedInteractionTypeLists.get(Constants.NETWORKSELECTEDINTERACTIONTYPE);
					if (list2 != null)
						networkSelectedInteractionTypes.addAll(list2);
					removeSelectedInteractionType();
				}
				refreshListModel();

				if (((VersionDescriptor) selectedVersion).getVersion() != null) {
					Vector<CellularNetWorkElementInformation> hits = c
							.getHits();
					for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits)
					{	
						c.getLegendItems().clear();
						cellularNetWorkElementInformation.setDirty(true);
					    cellularNetWorkElementInformation.reset();
					}
				}
				
				

			}

		});

		networkJCheckBox2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				networkJCheckBox2_actionPerformed(e);
				updateSelectedInteractionTypesMap();
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
					updateSelectedInteractionTypesMap();
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
							updateSelectedInteractionTypesMap();
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
					updateSelectedInteractionTypesMap();
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
					updateSelectedInteractionTypesMap();
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
				updateSelectedInteractionTypesMap();
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
				updateSelectedInteractionTypesMap();
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
				updateSelectedInteractionTypesMap();
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
						updateSelectedInteractionTypesMap();
					}
				});

		changeButton.setText("Change");
		changeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				changeButtonHandler(evt);
			}
		});

		exportButton.setText("Export selected interactome");
		exportButton.setToolTipText("Click the button to export interactions based on the selected Interactome, Version and Network Generation Preference.");
		exportButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exportButtonHandler(evt);
			}
		});
		
		setLayout(new BorderLayout());
		add(buildInteractionsDatabasePanel(), BorderLayout.NORTH);
		add(buildColumnDisplayPreferencesPanel(), BorderLayout.CENTER);
		add(buildNetworkGenerationPreferencesPanel(), BorderLayout.SOUTH);
        
		// displaySelectedInteractionTypes.add(Constants.PROTEIN_DNA);
		// displaySelectedInteractionTypes.add(Constants.PROTEIN_PROTEIN);
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
			PropertiesManager pm = PropertiesManager.getInstance();
			String contextProperty = pm.getProperty(this.getClass(),
					Constants.SELECTCONTEXT, "");
			if (!contextProperty.equals("")
					&& contextList.contains(contextProperty))
				contextJList.setSelectedValue(contextProperty, true);
			else if (!contextProperty.equals("")) {
				String context = contextProperty.split(" \\(")[0].trim();
				boolean needRemove = true;
				for (String cxt : contextList) {
					if (cxt.split(" \\(")[0].trim().equals(context)) {
						contextJList.setSelectedValue(cxt, true);
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

			readInteractionTypesProperties();
			removeSelectedInteractionType();
			String versionProperty = pm.getProperty(this.getClass(),
					Constants.SELECTVERSION, "");
			for (VersionDescriptor vd : versionList) {
				if (versionProperty != null
						&& vd.getVersion().equals(versionProperty)) {
					if (!versionProperty.equals(""))
						isUserSelected = false;
					versionJList.setSelectedValue(vd, true);
					break;
				}
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
			
			updateSelectedInteractionTypesMap();

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
			PropertiesManager pm = PropertiesManager.getInstance();
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

	private void removeSelectedInteractionType() {
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
	
	
	private void refreshListModel()
	{
		
		availableInteractionTypeList.setModel(new DefaultListModel());
		availableInteractionTypeList
		.setModel(availableInteractionTypeModel);
		selectedInteractionTypeList.setModel(new DefaultListModel());
		selectedInteractionTypeList
		.setModel(selectedInteractionTypeModel);
		
		availableNetworkInteractionTypeList.setModel(new DefaultListModel());
		availableNetworkInteractionTypeList
				.setModel(availNetworkInteractionTypeModel);
		selectedNetworkInteractionTypeList.setModel(new DefaultListModel());
		selectedNetworkInteractionTypeList
				.setModel(selectedNetworkInteractionTypeModel);
	
		c.updateColumnPref();
		
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

		String host = ResultSetlUtil.getUrl();

		final JDialog changeServicesDialog = new JDialog();

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
				
				getContextJList().updateUI();

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
	
	private void exportButtonHandler(ActionEvent e) {
	 
		String context = null;
		if (getContextJList().getSelectedValue() != null) {
			context = getContextJList()
					.getSelectedValue().toString().split(" \\(")[0]
					.trim();
		}

		String version = null;
		if (getVersionJList().getSelectedValue() != null)
			version = ((VersionDescriptor)getVersionJList().getSelectedValue())
					.getVersion();

		if (context == null
				|| context.trim().equals("")
				|| context
						.equalsIgnoreCase(Constants.SELECTCONTEXT)) {
			JOptionPane
					.showMessageDialog(
							null,
							"Please go to Preferences window to make sure that you select the correct interactome.",

							"Information",
							JOptionPane.INFORMATION_MESSAGE);
			 return;
			 
		}

		if (version == null
				|| version.trim().equals("")
				|| version
						.equalsIgnoreCase(Constants.SELECTVERSION)) {
			JOptionPane
					.showMessageDialog(
							null,
							"Please go to Preferences window to make sure that you select the correct interactome version.",

							"Information",
							JOptionPane.INFORMATION_MESSAGE);
			 
			return;
		}
	 
		if (networkSelectedInteractionTypes == null || networkSelectedInteractionTypes.size() == 0)
		{
			JOptionPane
			.showMessageDialog(
					null,
					"Please go to Network Generation Preferences window to make sure that you select at least one interaction type.",

					"Information",
					JOptionPane.INFORMATION_MESSAGE);
			return;
	 
		}	 
		
		JDialog exportDialog = new JDialog();
		exportDialog.setTitle("Export selected interactome");
		ExportSelectionPanel exportPanel = new ExportSelectionPanel(c,  exportDialog, context, version, networkSelectedInteractionTypes, isNetworkJCheckBox1Selected());
		 
		exportDialog.add(exportPanel);
		exportDialog.setModal(true);
		exportDialog.pack();
		Util.centerWindow(exportDialog);
		exportDialog.setVisible(true);

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
			PropertiesManager pm = PropertiesManager.getInstance();

			if (allInteractionTypes.size() == 0)
				return;
			pm.setProperty(this.getClass(), "url",
					ResultSetlUtil.getUrl());

			if (contextJList.getSelectedValue() != null)
				pm.setProperty(this.getClass(), Constants.SELECTCONTEXT,
						contextJList.getSelectedValue().toString());

			if (versionJList.getSelectedValue() != null
					&& versionJList.getSelectedValue() instanceof VersionDescriptor) {
				VersionDescriptor v = (VersionDescriptor) versionJList
						.getSelectedValue();
				String version = v.getVersion();
				pm.setProperty(this.getClass(), Constants.SELECTVERSION,
						version);
			}

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
				interactionTypeSifMap = interactionsConnection.getInteractionTypeMap();
				interactionEvidenceMap = interactionsConnection.getInteractionEvidenceMap();
				interactionConfidenceTypeMap = interactionsConnection.getConfidenceTypeMap();
				
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

			if (contextList == null) {
				contextList = new ArrayList<String>();
			}

			if (versionList != null) {
				versionList.clear();
			} else {
				versionList = new ArrayList<VersionDescriptor>();

			}

			contextJList.setModel(contextListModel);
			contextJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			versionJList.setModel(versionListModel);
			versionJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			displaySelectedInteractionTypes.clear();
			networkSelectedInteractionTypes.clear();
			selectedInteractionTypeMap.clear();
			
			if (allInteractionTypes != null && allInteractionTypes.size() > 0) {

				Collections.sort(allInteractionTypes);
				CellularNetWorkElementInformation
						.setAllInteractionTypes(allInteractionTypes);
				displayAvailInteractionTypes.addAll(allInteractionTypes);
				networkAvailInteractionTypes
						.addAll(displayAvailInteractionTypes);
				 
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
			e.printStackTrace();
			log.error(e.getMessage());
		}

	}

	private ListModel contextListModel = new AbstractListModel() {
		 
		private static final long serialVersionUID = 3896674125748294964L;

		public Object getElementAt(int index) {
			return contextList.get(index);
		}

		public int getSize() {
			return contextList.size();
		}
	};

	private ListModel versionListModel = new AbstractListModel() {
		 
		private static final long serialVersionUID = 7368821841064088101L;

		public Object getElementAt(int index) {
			return versionList.get(index);
		}

		public int getSize() {
			return versionList.size();
		}
	};

	private ListModel availableInteractionTypeModel = new AbstractListModel() {
	 
		private static final long serialVersionUID = -5249418316840418790L;

		public Object getElementAt(int index) {
			return displayAvailInteractionTypes.get(index);
		}

		public int getSize() {
			return displayAvailInteractionTypes.size();
		}
	};

	private ListModel availNetworkInteractionTypeModel = new AbstractListModel() {
	 
		private static final long serialVersionUID = -1159977335482478371L;

		public Object getElementAt(int index) {
			return networkAvailInteractionTypes.get(index);
		}

		public int getSize() {
			return networkAvailInteractionTypes.size();
		}
	};

	private ListModel selectedInteractionTypeModel = new AbstractListModel() {
		 
		private static final long serialVersionUID = -1731363979171517441L;

		public Object getElementAt(int index) {
			return displaySelectedInteractionTypes.get(index);
		}

		public int getSize() {
			return displaySelectedInteractionTypes.size();
		}
	};

	private ListModel selectedNetworkInteractionTypeModel = new AbstractListModel() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7890331646056124792L;

		public Object getElementAt(int index) {
			return networkSelectedInteractionTypes.get(index);
		}

		public int getSize() {
			return networkSelectedInteractionTypes.size();
		}
	};

	private class preferenceJCheckBox extends JCheckBox {
 
		private static final long serialVersionUID = -346069507369382002L;

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
				+ ResultSetlUtil.getUrl());
		builder.append(serviceLabel);
		builder.nextLine();

		builder.append(buildJListPanel("Select Interactome", contextJList, 250));
		builder.append(buildJTextPanel("Interactome Description",
				interactomeJTextArea));
		builder.nextLine();
		builder.append(buildJListPanel("Select Version", versionJList, 250));
		builder
				.append(buildJTextPanel("Version Description", versionJTextArea));

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

	private JPanel buildJListPanel(String title, JList aJlist, int width) {
		FormLayout layout = new FormLayout("left:pref", "");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();
		JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
		jScrollPane1
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setPreferredSize(new java.awt.Dimension(width, 90));
		jScrollPane1.setViewportView(aJlist);
		builder.append(new JLabel(title));
		builder.append(jScrollPane1);

		return builder.getPanel();

	}

	private JPanel buildJTextPanel(String title, JTextArea aJText) {
		FormLayout layout = new FormLayout("left:pref", "");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();
		JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
		jScrollPane1
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 90));
		aJText.setLineWrap(true);
		aJText.setWrapStyleWord(true);
		aJText.setEditable(false);		
	 
		jScrollPane1.setViewportView(aJText);
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
				availableInteractionTypeList, 150));
		builder.append(buildAddRemoveButtonPanel(addButton, removeButton));
		builder.append(buildJListPanel("Selected Interaction Types",
				selectedInteractionTypeList, 150));

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
				availableNetworkInteractionTypeList, 150));
		builder.append(buildAddRemoveButtonPanel(networkAddButton,
				networkRemoveButton));
		builder.append(buildJListPanel("Selected Interaction Types",
				selectedNetworkInteractionTypeList, 150));

		builder.nextLine();
		builder.append(exportButton);
		
		
		
		

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
	
	
	private void updateSelectedInteractionTypesMap()
	{
		String context, version;		
        if (contextJList.getSelectedValue() == null || versionJList.getSelectedValue() == null)
        	return;
        context = contextJList.getSelectedValue().toString().split(" \\(")[0].trim();;
        version = ((VersionDescriptor) versionJList.getSelectedValue()).getVersion();
		HashMap<String, List<String>> selectedInteractionTypeLists = new HashMap<String, List<String>>();
        if (displaySelectedInteractionTypes != null && displaySelectedInteractionTypes.size()>0)
        {
        	List<String> list = new ArrayList<String>();
        	list.addAll(displaySelectedInteractionTypes);
        	selectedInteractionTypeLists.put(Constants.DISPLAYSELECTEDINTERACTIONTYPE, list);
        }
        if (networkSelectedInteractionTypes != null && networkSelectedInteractionTypes.size()>0)
        {   
        	List<String> list = new ArrayList<String>();
        	list.addAll(networkSelectedInteractionTypes);
        	selectedInteractionTypeLists.put(Constants.NETWORKSELECTEDINTERACTIONTYPE, list);
        }
        if (selectedInteractionTypeLists.size()>0)
            selectedInteractionTypeMap.put(context+Constants.DEL+version, selectedInteractionTypeLists);
	}

}
