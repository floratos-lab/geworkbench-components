package org.geworkbench.components.mindy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyResultRow;
import org.geworkbench.util.pathwaydecoder.mutualinformation.ModulatorInfo;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * MINDY result view main GUI class
 *
 * @author mhall
 * @ch2514
 * @author oshteynb
 * @version $Id$
 */
@SuppressWarnings("serial")
public class MindyPlugin extends JPanel {

	static Log log = LogFactory.getLog(MindyPlugin.class);

	static final int DEFAULT_MODULATOR_LIMIT = 10;

	static final int MIN_CHECKBOX_WIDTH = 10;
	
	static final int MAX_CHECKBOX_WIDTH = 30;

	static final int MIN_MARKER_NAME_WIDTH = 200;
	

	static final int MIN_SCORE_WIDTH = 66;

	static final String NUM_MOD_SELECTED_LABEL = "Modulators Selected: ";

	static final String ENABLE_SELECTION = "Enable Selection";

	private static final String WARNING = "Warning";
	
	static final int MODULATOR_COL=1;
	static final int TARGET_COL=3;
	static final int SCORE_COL=4;

	static enum ModulatorSort {
		Aggregate, Enhancing, Negative;
	}

	ModulatorTargetModel modTargetModel;

	private ModulatorModel modulatorModel;

	private ModulatorHeatMap heatmap;

	private ModulatorHeatMapModel heatmapModel;

	private JTabbedPane tabs;
	MindyTableTab tableTab;

	public MindyTableTab getTableTab() {
		return tableTab;
	}

	JCheckBox selectionEnabledCheckBox;

	private List<DSGeneMarker> modulators;

	private MindyData mindyData;

	MindyData getMindyData() {
		return mindyData;
	}

	private JTable modTable;
	private JTable listTable;

	JCheckBox selectAllModsCheckBox;

	private JCheckBox selectAll /* list tab (used by modulator not list ? )*/;

	JCheckBox selectAllTargetsCheckBox;

	private JButton addToSetButton /* list tab */, addToSetButtonMod;
	private JButton exportTabList, exportTabModulator;

	private JLabel numModSelectedInModTab;

	Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);

	Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);

	private JComponent[] cs = new JComponent[6];

	private JButton screenshotButton = new JButton("  Image Snapshot  ");;

	private JList heatMapModNameList = new JList();;

	private JTextField modFilterField = new JTextField(15);;

	private JRadioButton showSymbol = new JRadioButton("Symbol");;

	private JRadioButton showProbeName = new JRadioButton("Probe Name");;


	// TODO new panel bug 0001718
	private DSPanel<DSGeneMarker> filteringSelectorPanel;

/*	private JComboBox targetsSets = new JComboBox(new DefaultComboBoxModel(
			MindyParamPanel.DEFAULT_SET));
*/
	private static final String ALL_NON_ZERO_MARKERS = "All non-zero markers";
	static final String[] DEFAULT_SET = { ALL_NON_ZERO_MARKERS };
	private JComboBox targetsSets = new JComboBox(new DefaultComboBoxModel(
			DEFAULT_SET));
//	private JTextField targetList = new JTextField("");

	/*  tmp to keep set selection */
	private String selectedSetName = ALL_NON_ZERO_MARKERS;

	MindyVisualComponent mindyVisualComponent = null;

	/**
	 * Constructor.
	 *
	 * @param data -
	 *            MINDY data
	 * @param visualPlugin -
	 *            MINDY component (the class the implements the VisualPlugin
	 *            interface)
	 */

	public MindyPlugin(MindyData data, final MindyVisualComponent visualPlugin) {
		mindyData = data;
		mindyVisualComponent = visualPlugin;
		
		log.debug("\tMindyPlugin::constructor::start::"
				+ System.currentTimeMillis());

		/*   init like in param panel, check for null needed for loading saved workspace */
		MindyParamPanel mindyParamPanel = MindyAnalysis.getParamsPanel();
		if (mindyParamPanel != null){
			filteringSelectorPanel = mindyParamPanel.getSelectorPanel();
			resetTargetSetModel(filteringSelectorPanel);
		}

		targetsSets.setEditable(false);
		targetsSets.setEnabled(true);

		modulators = mindyData.getModulators();

		// for heatmap image rendering cursor
		cs[0] = screenshotButton;
		cs[1] = heatMapModNameList;
		cs[2] = modFilterField;
		cs[3] = showSymbol;
		cs[4] = showProbeName;

		tabs = new JTabbedPane();
		{
			// Modulator Table
			modulatorModel = new ModulatorModel();

			modTable = new JTable(modulatorModel) {
				public Component prepareRenderer(TableCellRenderer renderer,
						int rowIndex, int vColIndex) {
					Component c = super.prepareRenderer(renderer, rowIndex,
							vColIndex);
					if (rowIndex % 2 == 0
							&& !isCellSelected(rowIndex, vColIndex)) {
						c.setBackground(new Color(237, 237, 237));
					} else {
						// If not shaded, match the table's background
						//c.setBackground(getBackground());
					}
					return c;
				}
			};

			JScrollPane scrollPane = new JScrollPane(modTable);
			scrollPane
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			restoreBooleanRenderers(modTable);
			modTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
			renderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
			modTable.getColumnModel().getColumn(5).setCellRenderer(renderer);

//			modTable.getColumnModel().getColumn(0).setMaxWidth(15);
			// 0 hardcoded for consistency, refactor later
			modTable.getColumnModel().getColumn(0).setMinWidth(
					MIN_CHECKBOX_WIDTH);
			modTable.getColumnModel().getColumn(0).setMaxWidth(
					MAX_CHECKBOX_WIDTH);		 
			modTable.setAutoCreateColumnsFromModel(false);
			modTable.getTableHeader().addMouseListener(
					new ColumnHeaderListener(this));

			numModSelectedInModTab = new JLabel(NUM_MOD_SELECTED_LABEL + " 0");

			addToSetButtonMod = new JButton("Add To Set");
			addToSetButtonMod.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					ModulatorModel model = (ModulatorModel) modTable.getModel();
					List<DSGeneMarker> selections = model
							.getSelectedModulators();
					if (selections.size() > 0)
						addToSet(selections);
				}
			});

			exportTabModulator =new JButton("Export");
			exportTabModulator.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					exportTabModulatorPressed();
				}
			});
			
			JLabel ll = new JLabel("List Selections  ", SwingConstants.LEFT);
			ll.setFont(new Font(ll.getFont().getName(), Font.BOLD, 12));
			ll.setForeground(Color.BLUE);
			selectAll = new JCheckBox("Select All");
			
			selectAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					setCursor(hourglassCursor);
					ModulatorModel model = (ModulatorModel) modTable.getModel();
					model.selectAllModulators(selectAll.isSelected());

					setTextNumModSelected(model.getNumberOfModulatorsSelected());

					setCursor(normalCursor);
				}
			});
			
			
			JLabel dl = new JLabel("Marker Display  ", SwingConstants.LEFT);
			dl.setFont(new Font(dl.getFont().getName(), Font.BOLD, 12));
			dl.setForeground(Color.BLUE);
			JRadioButton modShowSymbol = new JRadioButton("Symbol");
			modShowSymbol.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					modulatorModel.setShowProbeName(false);
					modulatorModel.fireTableDataChanged();
				}
			});
			JRadioButton modShowProbeName = new JRadioButton("Probe Name");
			modShowProbeName.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					modulatorModel.setShowProbeName(true);
					modulatorModel.fireTableDataChanged();
				}
			});
			ButtonGroup displayGroup = new ButtonGroup();
			displayGroup.add(modShowSymbol);
			displayGroup.add(modShowProbeName);
			if (this.mindyData.isAnnotated())
				modShowSymbol.setSelected(true);
			else
				modShowProbeName.setSelected(true);

			JPanel taskContainer = new JPanel(new GridLayout(8, 1, 10, 10));
			taskContainer.add(dl);
			taskContainer.add(modShowSymbol);
			taskContainer.add(modShowProbeName);
			taskContainer.add(ll);
			taskContainer.add(selectAll);
			taskContainer.add(numModSelectedInModTab);
			taskContainer.add(addToSetButtonMod);
			taskContainer.add(exportTabModulator);
			JPanel p = new JPanel(new BorderLayout());
			p.add(taskContainer, BorderLayout.NORTH);
			JScrollPane sp = new JScrollPane(p);
			sp
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp,
					scrollPane);
			panel.setResizeWeight(0.055);
			panel.setOneTouchExpandable(false);
			panel.setContinuousLayout(true);
			
			tabs.add("Modulator", panel);
		}

		{
			// Modulator / Target Table
			tableTab = new MindyTableTab();

			tabs.add("Table", tableTab);
		}

		{
			// Modulator / Target list
			JLabel dl = new JLabel("Marker Display  ", SwingConstants.LEFT);
			dl.setFont(new Font(dl.getFont().getName(), Font.BOLD, 12));
			dl.setForeground(Color.BLUE);
			JRadioButton showSymbol = new JRadioButton("Symbol");
			showSymbol.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					modTargetModel.setShowProbeName(false);
					modTargetModel.fireTableDataChanged();
				}
			});
			JRadioButton showProbeName = new JRadioButton("Probe Name");
			showProbeName.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					modTargetModel.setShowProbeName(true);
					modTargetModel.fireTableDataChanged();
				}
			});
			ButtonGroup displayGroup = new ButtonGroup();
			displayGroup.add(showSymbol);
			displayGroup.add(showProbeName);
			if (this.mindyData.isAnnotated())
				showSymbol.setSelected(true);
			else
				showProbeName.setSelected(true);

			JLabel l = new JLabel("Marker Set", SwingConstants.LEFT);
			l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
			l.setForeground(Color.BLUE);

			modTargetModel = new ModulatorTargetModel(this);
			listTable = new JTable(modTargetModel) {
				public Component prepareRenderer(TableCellRenderer renderer,
						int rowIndex, int vColIndex) {
					Component c = super.prepareRenderer(renderer, rowIndex,
							vColIndex);
					if (rowIndex % 2 == 0
							&& !isCellSelected(rowIndex, vColIndex)) {
						c.setBackground(new Color(237, 237, 237));
					} else {
						// If not shaded, match the table's background
						//c.setBackground(getBackground());
					}

					return c;
				}
			};
			JScrollPane scrollPane = new JScrollPane(listTable);

			restoreBooleanRenderers(listTable);

			listTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listTable.setAutoCreateColumnsFromModel(false);
			listTable.getTableHeader().addMouseListener(
					new ColumnHeaderListener(this));
			// 0 hardcoded for consistency, refactor later
			listTable.getColumnModel().getColumn(0).setMinWidth(
					MIN_CHECKBOX_WIDTH);
			listTable.getColumnModel().getColumn(2).setMinWidth(
					MIN_CHECKBOX_WIDTH);

			selectionEnabledCheckBox = new JCheckBox(ENABLE_SELECTION + " 0");
			selectionEnabledCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					boolean selected = selectionEnabledCheckBox.isSelected();
					log.debug("Setting list box visibility to " + selected);
					setListCheckboxesVisibility(selected);
					setListControlVisibility(selected);
					setListTableViewOptions();
					if (!selected) {
						modTargetModel.selectAllModulators(selected);
						modTargetModel.selectAllTargets(selected);
						modTargetModel.fireTableDataChanged();
					}
				}
			});

			setListTableViewOptions();

			selectAllModsCheckBox = new JCheckBox("Select All Modulators");
			selectAllModsCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					ModulatorTargetModel model = (ModulatorTargetModel) listTable
							.getModel();
					model.selectAllModulators(selectAllModsCheckBox
							.isSelected());
					selectionEnabledCheckBox
							.setText(MindyPlugin.ENABLE_SELECTION + " "
									+ model.getNumberOfMarkersSelected());
				}
			});
			selectAllTargetsCheckBox = new JCheckBox("Select All Targets");
			selectAllTargetsCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					ModulatorTargetModel model = (ModulatorTargetModel) listTable
							.getModel();
					model.selectAllTargets(selectAllTargetsCheckBox
							.isSelected());
					selectionEnabledCheckBox
							.setText(MindyPlugin.ENABLE_SELECTION + " "
									+ model.getNumberOfMarkersSelected());
				}
			});

			addToSetButton = new JButton("Add To Set");
			addToSetButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					ModulatorTargetModel model = (ModulatorTargetModel) listTable
							.getModel();
					addToSet(model.getUniqueSelectedMarkers());
				}
			});
			
			exportTabList = new JButton("Export");
			exportTabList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					exportTabListPressed();
				}					
			});
			
			
			setListControlVisibility(true);

			l = new JLabel("Marker Selection", SwingConstants.LEFT);
			l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
			l.setForeground(Color.BLUE);

			selectionEnabledCheckBox.setSelected(true);
			selectAllModsCheckBox.setEnabled(true);
			selectAllTargetsCheckBox.setEnabled(true);
			addToSetButton.setEnabled(true);

			JPanel p = new JPanel(new GridLayout(9, 1, 10, 10));
			p.add(dl);
			p.add(showSymbol);
			p.add(showProbeName);
			p.add(l);
			p.add(selectionEnabledCheckBox);
			p.add(selectAllModsCheckBox);
			p.add(selectAllTargetsCheckBox);
			p.add(addToSetButton);
			p.add(exportTabList);

			JPanel taskContainer = new JPanel();
			taskContainer.setLayout(new BorderLayout(10, 10));
			taskContainer.add(p, BorderLayout.NORTH);
			JScrollPane sp = new JScrollPane(taskContainer);
			sp
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp,
					scrollPane);
			panel.setResizeWeight(0.055);
			panel.setOneTouchExpandable(false);
			panel.setContinuousLayout(true);

			tabs.add("List", panel);
		}

		
		{
			// Heat Map
			// This is modulator just to give us something to generate the heat
			// map with upon first running
			DSGeneMarker modulator = modulators.iterator().next();

			heatmapModel = new ModulatorHeatMapModel(modulator, mindyData);
			heatmap = new ModulatorHeatMap(heatmapModel);

			final JScrollPane heatMapScrollPane = new JScrollPane(heatmap);

			JPanel transFacPane = new JPanel(new BorderLayout());
			JLabel l = new JLabel("Transcription Factor", SwingConstants.LEFT);
			l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
			l.setForeground(Color.BLUE);
			transFacPane.add(l, BorderLayout.NORTH);
			final JLabel transFactorName = new JLabel(heatmap
					.getMarkerDisplayName(mindyData.getTranscriptionFactor()));
			transFacPane.add(transFactorName);

			JPanel modulatorPane = new JPanel(new BorderLayout());
			modFilterField.setEditable(false);
			l = new JLabel("Modulators", SwingConstants.LEFT);
			l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
			l.setForeground(Color.BLUE);
			modulatorPane.add(l, BorderLayout.NORTH);

			heatMapModNameList.setFixedCellWidth(12);
			JScrollPane modListScrollPane = new JScrollPane(heatMapModNameList);
			heatMapModNameList.setModel(new ModulatorListModel(!mindyData.isAnnotated(), modulatorModel));

			screenshotButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					mindyVisualComponent.createImageSnapshot(heatMapScrollPane
							.getViewport().getComponent(0));
				}
			});

			heatMapModNameList
					.addListSelectionListener(new ListSelectionListener() {
						// this is to compensate for a bug? in JList
						// valueChanged() get called 2x with 1 mouse click....
						int clickCount = 0;

						public void valueChanged(
								ListSelectionEvent listSelectionEvent) {
							clickCount++;
							if (clickCount < 2) {
								if( !processCursor() ) {
									return;
								}

								modFilterField.setText(heatMapModNameList
										.getSelectedValue().toString());

								// set selected modulator,
								heatMapSync();

							} else {
								clickCount = 0;
							}

							setCursorFinished();
						}
					});

			JLabel dl = new JLabel("Marker Display  ", SwingConstants.LEFT);
			dl.setFont(new Font(dl.getFont().getName(), Font.BOLD, 12));
			dl.setForeground(Color.BLUE);

			showSymbol.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					if( !processCursor() ) {
						return;
					}

					heatmap.setShowProbeName(false);
					transFactorName.setText(heatmap
							.getMarkerDisplayName(mindyData
									.getTranscriptionFactor()));

					int orig = heatMapModNameList.getSelectedIndex();
					ModulatorListModel m = (ModulatorListModel) heatMapModNameList
							.getModel();
					m.setShowProbeName(false);
					m.refresh();
					heatMapModNameList.setSelectedIndex(orig);
					Object selectedO = heatMapModNameList.getSelectedValue();
					if (selectedO == null)
						heatMapModNameList.setSelectedIndex(0);
					modFilterField.setText(heatMapModNameList
							.getSelectedValue().toString());

					List<DSGeneMarker> l = mindyVisualComponent
								.getSelectedMarkers();
					heatmapModel.limitMarkers(l);
					heatmap.reset();

					setCursorFinished();
				}
			});

			showProbeName.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					if( !processCursor() ) {
						return;
					}

					heatmap.setShowProbeName(true);
					transFactorName.setText(heatmap
							.getMarkerDisplayName(mindyData
									.getTranscriptionFactor()));

					int orig = heatMapModNameList.getSelectedIndex();
					ModulatorListModel m = (ModulatorListModel) heatMapModNameList
							.getModel();
					m.setShowProbeName(true);
					m.refresh();
					heatMapModNameList.setSelectedIndex(orig);
					Object selectedO = heatMapModNameList.getSelectedValue();
					if (selectedO == null)
						heatMapModNameList.setSelectedIndex(0);
					modFilterField.setText(heatMapModNameList
							.getSelectedValue().toString());

					List<DSGeneMarker> l = mindyVisualComponent
								.getSelectedMarkers();
					heatmapModel.limitMarkers(l);
					heatmap.reset();

					setCursorFinished();
				}
			});

			ButtonGroup displayGroup = new ButtonGroup();
			displayGroup.add(showSymbol);
			displayGroup.add(showProbeName);
			if (this.mindyData.isAnnotated())
				showSymbol.setSelected(true);
			else
				showProbeName.setSelected(true);

			JPanel displayPane = new JPanel(new GridLayout(4, 1));
			displayPane.add(dl);
			displayPane.add(showSymbol);
			displayPane.add(showProbeName);

			JPanel p = new JPanel(new BorderLayout(10, 10));
			p.add(modFilterField, BorderLayout.NORTH);
			p.add(modListScrollPane, BorderLayout.CENTER);

			p.add(screenshotButton, BorderLayout.SOUTH);
			modulatorPane.add(p, BorderLayout.CENTER);

			JPanel taskContainer = new JPanel();
			taskContainer.setLayout(new BorderLayout(10, 10));
			taskContainer.add(transFacPane, BorderLayout.NORTH);
			taskContainer.add(modulatorPane, BorderLayout.CENTER);

			JPanel outterTaskContainer = new JPanel(new BorderLayout(10, 10));
			outterTaskContainer.add(displayPane, BorderLayout.NORTH);
			outterTaskContainer.add(taskContainer, BorderLayout.CENTER);

			JScrollPane modTransScroll = new JScrollPane(outterTaskContainer);
			modTransScroll
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					modTransScroll, heatMapScrollPane);
			panel.setResizeWeight(0.055);
			panel.setOneTouchExpandable(false);
			panel.setContinuousLayout(true);

			tabs.add("Heat Map", panel);
		}
		
		// FIXME this is not a real resolution of the issue
		// selectAll and de-selectAll
//		ModulatorModel modulatorModel = (ModulatorModel) modTable.getModel();
//		modelTemp.selectAllModulators(true);
//		modelTemp.selectAllModulators(false);	

		// TODO
		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);

		disableTabs();

		JPanel result = new JPanel(new BorderLayout());
		FormLayout layout = new FormLayout(
				"left:max(100dlu;pref), 10dlu, 100dlu, 10dlu, "
						+ "100dlu, 10dlu, 100dlu, 10dlu, 100dlu", "");

		targetsSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {

				String selectedLabel = ((String) targetsSets.getSelectedItem());

				if (!StringUtils.isEmpty(selectedLabel)) {
					selectedLabel = selectedLabel.trim();
					filterMarkersSet(selectedLabel);

					 selectedSetName = selectedLabel;

				}

				revalidate();
				repaint();
			}

		});

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Displayed targets filter");

		builder.append(targetsSets);
		result.add(builder.getPanel());

		add(result, BorderLayout.PAGE_END);

		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent
						.getSource();
				int index = sourceTabbedPane.getSelectedIndex();
				log.debug("Tab changed to: "
						+ sourceTabbedPane.getTitleAt(index));
				if (index == 3) {
					if( !processCursor() ) {
						return;
					}
				}
			}
		});

		log.debug("\tMindyPlugin::constructor::end::"
				+ System.currentTimeMillis());

		result.setVisible(true);

		tabs.setVisible(true);

	}

	private void exportTabModulatorPressed(){

		ModulatorModel model = (ModulatorModel) modTable.getModel();							
		int row=model.getRowCount();
		int col=model.getColumnCount();
		String str="";
		for(int k=1;k<col;k++){
			str+=model.getColumnName(k)+",";
		}
		str=str.substring(0, str.length()-1);
		str+="\n";
		for(int i=0;i<row;i++){
			for(int j=1;j<col;j++){
				String ss=model.getValueAt(i,j).toString();
				String[] tokens = ss.split(",");
				ss="";
				for(String s:tokens ){
					ss+=s+";";		//replace , with ;
				}
				ss=ss.substring(0,ss.length()-1);	
				str+=ss+",";
			}
			str=str.substring(0, str.length()-1);	//remove the last ,
			str+="\n";
		}
		JFileChooser fc = new JFileChooser(MindyPlugin.getLastDirectory());
		CSVFileFilter filter = new CSVFileFilter();
		fc.setFileFilter(filter);
		fc.setDialogTitle("Save Mindy Modulator Results");
		int returnVal = fc.showSaveDialog(MindyPlugin.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile=fc.getSelectedFile();
			if (!selectedFile.getName().endsWith(".csv")) {
				selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
			}
			
			if (selectedFile.exists()) {
				int n = JOptionPane.showConfirmDialog(
						null,
						"Are you sure you want to overwrite this csv file?",
						"Overwrite?", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION || n == JOptionPane.CLOSED_OPTION) {
					JOptionPane.showMessageDialog(null, "Save cancelled.");
					return;
				}
			}
						
			try {
				PrintWriter out = null;
				String filepath = fc.getCurrentDirectory().getCanonicalPath();
	            setLastDirectory(filepath);						
				out = new PrintWriter(selectedFile);
				out.println(str);
				out.close();
			} catch (FileNotFoundException e) {
				log.error(e);
				JOptionPane.showMessageDialog(
						null,
						"The file is not ready. It may be opened by other applications.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			} catch (IOException e) {										
				log.error(e);
				JOptionPane.showMessageDialog(
						null,
						"saving encountered an error.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}			
		}	
	}
	
	private void exportTabListPressed(){

		ModulatorTargetModel model = (ModulatorTargetModel) listTable
		.getModel();					
		int row=model.getRowCount();
		String s="";
		s+=model.getColumnName(MODULATOR_COL)+ ","
			+model.getColumnName(TARGET_COL)+ ","
			+model.getColumnName(SCORE_COL)+ "\n";		
		for(int i=0;i<row;i++){						
			s+=model.getValueAt(i,MODULATOR_COL)+","+model.getValueAt(i,TARGET_COL)+","
				+model.getValueAt(i,SCORE_COL);						
			s+="\n";
		}
		JFileChooser fc = new JFileChooser(MindyPlugin.getLastDirectory());
		CSVFileFilter filter = new CSVFileFilter();
		fc.setFileFilter(filter);
		fc.setDialogTitle("Save Mindy List Results");
		int returnVal = fc.showSaveDialog(MindyPlugin.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile=fc.getSelectedFile();
			if (!selectedFile.getName().endsWith(".csv")) {
				selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
			}
			
			if (selectedFile.exists()) {
				int n = JOptionPane.showConfirmDialog(
						null,
						"Are you sure you want to overwrite this csv file?",
						"Overwrite?", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION || n == JOptionPane.CLOSED_OPTION) {
					JOptionPane.showMessageDialog(null, "Save cancelled.");
					return;
				}
			}	
			
			PrintWriter out = null;
			try {
				String filepath = fc.getCurrentDirectory().getCanonicalPath();
	            setLastDirectory(filepath);						
				out = new PrintWriter(selectedFile);
				out.println(s);
				out.close();
			} catch (FileNotFoundException e) {
				log.error(e);
				JOptionPane.showMessageDialog(
						null,
						"The file is not ready. It may be opened by other applications.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			} catch (IOException e) {										
				log.error(e);
				JOptionPane.showMessageDialog(
						null,
						"saving encountered an error.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}			
		}	
	}
	
	static String getLastDirectory() {
        String dir = ".";
        try {
            String filename = FilePathnameUtils.getIDEASettingsPath();

            File file = new File(filename);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));

                dir = br.readLine();
                br.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (dir == null) {
            dir = ".";
        }
        return dir;
    }
	
	static void setLastDirectory(String dir) {
        try { //save current settings.
            String outputfile = FilePathnameUtils.getIDEASettingsPath();
            BufferedWriter br = new BufferedWriter(new FileWriter(
                    outputfile));
            br.write(dir);
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
	
	static class CSVFileFilter extends FileFilter {
		private static final String fileExt = ".csv";

		public String getDescription() {
			return "CSV Files";
		}

		public boolean accept(File f) {
			boolean returnVal = false;
			if (f.isDirectory() || f.getName().endsWith(fileExt)) {
				return true;
			}

			return returnVal;
		}

	}
	
	public Cursor getHourglassCursor() {
		return hourglassCursor;
	}


	public Cursor getNormalCursor() {
		return normalCursor;
	}

	// TODO
	/**
	 * used for heatMap,
	 * refactored, extracted from several Listeners
	 * in my tests run just fine with body of this method commented out,
	 * test with real and large data,
	 * is it needed for heatMap, Dopaint has setFinished call  ?
	 * revisit cursor handling later
	 */
	private boolean processCursor() {
		boolean result = true;

		org.geworkbench.util.Cursor cursor = checkCursor();
		if(cursor == null){
			return false;
		}

		result = startCursor(cursor);

		return result;
	}


	private boolean startCursor(org.geworkbench.util.Cursor cursor) {
		boolean result = true;

		try{
			cursor.start();
		} catch (Exception e){
			log.warn(e.getMessage());
			result = false;
		}

		return result;
	}


	private org.geworkbench.util.Cursor checkCursor() {
		org.geworkbench.util.Cursor cursor = org.geworkbench.util.Cursor
				.getCursor();
		if (cursor.isStarted() && !cursor.isFinished()) {
			return null;
		} else {
			cursor.setAssociatedComponent(MindyPlugin.this);
			cursor.linkCursorToComponents(cs);
		}
		return cursor;
	}

	private DSGeneMarker getSelectedModulatorHeatMap() {
		ModulatorListModel m = (ModulatorListModel) heatMapModNameList
		.getModel();
		m.setModulatorModel(modulatorModel);

		int selectedIndex = heatMapModNameList.getSelectedIndex();
//		String modName = heatMapModNameList.getSelectedValue().toString().trim();

		DSGeneMarker modMarker;

		if ((modTargetModel.getEnabledModulators() == null)
				|| (modTargetModel.getEnabledModulators().size() <= 0)) {
			log.warn("No modulators selected.");
			modMarker = null;
		}

		if ((selectedIndex > 0)
				&& (selectedIndex < modTargetModel.getEnabledModulators()
						.size())) {
			modMarker = modTargetModel.getEnabledModulators()
					.get(selectedIndex);
		} else {
			modMarker = modTargetModel.getEnabledModulators().get(0);
			heatMapModNameList.setSelectedIndex(0);
		}

		return modMarker;
	}

	static void restoreBooleanRenderers(JTable table) {
		table.setDefaultEditor(Boolean.class, new DefaultCellEditor(
				new JCheckBox()));
	}

	void setListTableViewOptions() {
		boolean selected = selectionEnabledCheckBox.isSelected();
		if (selected) {
			listTable.getColumnModel().getColumn(0).setMaxWidth(MAX_CHECKBOX_WIDTH);
			listTable.getColumnModel().getColumn(2).setMaxWidth(MAX_CHECKBOX_WIDTH);
		}
		setListCheckboxesVisibility(selected);
	}

	private void setListCheckboxesVisibility(boolean show) {
		if (show) {
			listTable.getColumn(" ").setMaxWidth(30);
			listTable.getColumn("  ").setMaxWidth(30);
			listTable.getColumn(" ").setMinWidth(30);
			listTable.getColumn("  ").setMinWidth(30);
			listTable.getColumn(" ").setWidth(30);
			listTable.getColumn("  ").setWidth(30);
		} else {
			listTable.getColumn(" ").setMaxWidth(0);
			listTable.getColumn("  ").setMaxWidth(0);
			listTable.getColumn(" ").setMinWidth(0);
			listTable.getColumn("  ").setMinWidth(0);
			listTable.getColumn(" ").setWidth(0);
			listTable.getColumn("  ").setWidth(0);
		}
	}

	private void setListControlVisibility(boolean show) {
		selectAllModsCheckBox.setEnabled(show);
		selectAllTargetsCheckBox.setEnabled(show);
		addToSetButton.setEnabled(show);
	}

	/**
	 * Callback method for the MINDY result view GUI when the user changes
	 * marker set selections in the Selection Panel.
	 *
	 * @param -
	 *            list of selected markers
	 */
	private void limitMarkers(List<DSGeneMarker> markers) {
		try{
			/* stats for filtered markers
			 * don't need to keep collection of rows, change later   */
		    HashMap<DSGeneMarker, ModulatorInfo> tmpFilteredModulatorInfoMap;

			if (markers==null){
				/* use stats from mindy run */
				tmpFilteredModulatorInfoMap = mindyData.getModulatorInfoMap();
			} else{
				/* generate stats from limited markers */
			    tmpFilteredModulatorInfoMap = new HashMap<DSGeneMarker, ModulatorInfo>();

				List<DSGeneMarker> modList = mindyData.getModulators();
				for (DSGeneMarker mod : modList) {
		            ModulatorInfo modInfo = new ModulatorInfo();

					List<MindyResultRow> tmpRows = mindyData.getRows(mod, markers);
					for (MindyResultRow mindyResultRow : tmpRows) {
						modInfo.insertRow(mindyResultRow);
					}

					tmpFilteredModulatorInfoMap.put(mod, modInfo);

				}

			}

			mindyData.setFilteredModulatorInfoMap(tmpFilteredModulatorInfoMap);

			// heat map tab
			org.geworkbench.util.Cursor cursor = checkCursor();
			if(cursor == null){
				return;
			}
			setCursor(hourglassCursor);

			heatmapModel.limitMarkers(markers);
			heatmap.updateMaxGeneNameWidth();

			setCursor(normalCursor);

			// target table tab
			tableTab.getAggregateModel().limitMarkers(markers);

			// list table tab
			modTargetModel.limitMarkers(markers);

		} catch (Exception e){
			log.warn(e.getMessage());
		}

	}

	public void rememberTableSelections() {
		// modulator table tab
		modulatorModel.rememberSelections();

		// target table tab
		tableTab.getAggregateModel().rememberSelections();

		// list table tab
		modTargetModel.rememberSelections();
	}


	/**
	 * changed to a static method and argument showProbeName instead of model. os
	 *
	 * Specifies the marker name (probe name vs. gene name) to display on the
	 * table (modulator, targets, or list).
	 *
	 * @param showProbeName -
	 *            if true probe name, if false gene(symbol) name
	 * @param marker -
	 *            gene marker
	 * @return The marker name (probe vs. gene).
	 */
	public static String getMarkerDisplayName(boolean showProbeName, DSGeneMarker marker) {
		String result;

		if (showProbeName) {
			result = marker.getLabel();
		} else{
			result = marker.getGeneName();
		}

		return result;
	}

	@SuppressWarnings("unchecked") 
	void addToSet(List<DSGeneMarker> selections) {
		if (mindyVisualComponent == null) {
			log.error("No plugin from which to add to set.");
			return;
		}

		if ((selections == null) || (selections.size() <= 0)) {
			JOptionPane.showMessageDialog(this, "No markers selected.",
					WARNING, JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Ask user for marker set label
		String tmpLabel = JOptionPane.showInputDialog("Set Label:", "");
		if (tmpLabel == null || tmpLabel.equals(""))
			return;
		else {
			DSPanel<DSGeneMarker> subpanel = null;
			// If Mindy plugin doesn't have access to selector panel and/or
			// marker sets
			if (((mindyVisualComponent.getSelectorPanel() == null) || (mindyVisualComponent
					.getSelectorPanel().panels() == null))
					&& ((mindyVisualComponent.getSelectedMarkers() == null) || (mindyVisualComponent
							.getSelectedMarkers().size() <= 0))) {
				log
						.error("Plugin does not have a viable selector panel/marker set.");
				subpanel = new CSPanel<DSGeneMarker>();
				subpanel.setLabel(tmpLabel);
				for (int i = 0; i < selections.size(); i++) {
					Object o = selections.get(i);
					if (o instanceof DSGeneMarker) {
						subpanel.add((DSGeneMarker) o);
					}
				}
				mindyVisualComponent
						.publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
								DSGeneMarker.class, subpanel,
								SubpanelChangedEvent.NEW));
				return;
			}

			// Checking to see if a subpanel already has the same label
			if ((mindyVisualComponent.getSelectorPanel() != null)
					&& (mindyVisualComponent.getSelectorPanel().panels() != null)) {
				DSPanel<DSGeneMarker> selectorPanel = mindyVisualComponent.getSelectorPanel();
				for (int i = 0; i < selectorPanel.panels().size(); i++) {
					Object o = selectorPanel.panels().get(i);
					if ((o instanceof DSPanel)
							&& ((DSPanel<DSGeneMarker>) o).getLabel().equals(tmpLabel)) {
						subpanel = ((DSPanel<DSGeneMarker>) o);
						for (int j = 0; j < selections.size(); j++) {
							Object oo = selections.get(j);
							if (oo instanceof DSGeneMarker) {
								// Checking to see if marker is already part of
								// the subpanel
								if (subpanel.contains((DSGeneMarker) oo)) {
									continue;
								} else {
									// If not, add the marker to the subpanel
									subpanel.add((DSGeneMarker) oo);
								}
							}
						}
						mindyVisualComponent
								.publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
										DSGeneMarker.class, subpanel,
										SubpanelChangedEvent.SET_CONTENTS));
						return;
					}
				}
			} else {
				// If not, check if Mindy plugin has a selected marker set
				if ((mindyVisualComponent.getSelectedMarkers() != null)
						&& (mindyVisualComponent.getSelectedMarkers().size() > 0)) {
					boolean needToAdd = false;
					subpanel = new CSPanel<DSGeneMarker>();
					subpanel.setLabel(tmpLabel);
					for (int i = 0; i < selections.size(); i++) {
						Object o = selections.get(i);
						if ((o instanceof DSGeneMarker)
								&& (!mindyVisualComponent.getSelectedMarkers()
										.contains((DSGeneMarker) o))) {
							subpanel.add((DSGeneMarker) o);
							needToAdd = true;
						}
					}
					if (needToAdd) {
						for (int i = 0; i < mindyVisualComponent.getSelectedMarkers()
								.size(); i++) {
							Object o = mindyVisualComponent.getSelectedMarkers().get(i);
							if (o instanceof DSGeneMarker) {
								subpanel.add((DSGeneMarker) o);
							}
						}
						mindyVisualComponent
								.publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
										DSGeneMarker.class, subpanel,
										SubpanelChangedEvent.SET_CONTENTS));
						return;
					}
				}
			}

			// If not, create a subpanel and add selected targets/modulators to
			// the subpanel and publish event
			if (subpanel == null) {
				subpanel = new CSPanel<DSGeneMarker>();
				subpanel.setLabel(tmpLabel);
				for (int i = 0; i < selections.size(); i++) {
					Object o = selections.get(i);
					if (o instanceof DSGeneMarker) {
						subpanel.add((DSGeneMarker) o);
					}
				}
				mindyVisualComponent
						.publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
								DSGeneMarker.class, subpanel,
								SubpanelChangedEvent.NEW));
			}

		}
	}

	void refreshModulatorListModel() {
		setModFilterField();

		ModulatorListModel model = (ModulatorListModel) heatMapModNameList
		.getModel();
		model.refresh();
		heatMapSync();

	}

	/* init with the first index for now  */
	void initModulatorListModel() {
		heatMapModNameList.setSelectedIndex(0);
	}

	void setModFilterField(){
	if( heatMapModNameList.getSelectedValue() != null ){
		modFilterField.setText(heatMapModNameList
				.getSelectedValue().toString());
		}
	}


	/**
	 * @param mod - number to dispaly
	 *
	 * eventually will go to the class for modulator tab
	 *
	 */
	public void setTextNumModSelected(int mod) {
		numModSelectedInModTab.setText(MindyPlugin.NUM_MOD_SELECTED_LABEL + " "
				+ mod);
	}

	void setSelectAll(boolean value) {
		selectAll.setSelected(value);
	}

	void disableTabs() {
		tabs.setEnabledAt(1, false);
		tabs.setEnabledAt(2, false);
		tabs.setEnabledAt(3, false);
	}

	void enableTabs() {
		tabs.setEnabledAt(1, true);
		tabs.setEnabledAt(2, true);
		tabs.setEnabledAt(3, true);
	}

	public void addToTargetSetModel(DSPanel<DSGeneMarker> selectorPanel) {
		DefaultComboBoxModel targetsSetModel = (DefaultComboBoxModel) targetsSets
				.getModel();
		targetsSetModel.removeAllElements();
		targetsSetModel.addElement(ALL_NON_ZERO_MARKERS);
		for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
			String label = panel.getLabel().trim();
			targetsSetModel.addElement(label);
		}

		targetsSets.setSelectedItem(selectedSetName);
	}


	public void resetTargetSetModel(DSPanel<DSGeneMarker> selectorPanel) {
		DefaultComboBoxModel targetsSetModel = (DefaultComboBoxModel) targetsSets
				.getModel();
		String tmpSelectedSetName = selectedSetName;

		targetsSetModel.removeAllElements();
		targetsSetModel.addElement(ALL_NON_ZERO_MARKERS);
		for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
			String label = panel.getLabel().trim();
			targetsSetModel.addElement(label);
		}

		selectedSetName = tmpSelectedSetName;
		targetsSets.setSelectedItem(selectedSetName);
	}

	private List<DSGeneMarker> getFilteredMarkers(String selectedLabel) {
		List<DSGeneMarker> markers;
		if (ALL_NON_ZERO_MARKERS.equals(selectedLabel)) {
			markers = null;
		} else {
			markers = new ArrayList<DSGeneMarker>();
			DSPanel<DSGeneMarker> selectedSet = MindyParamPanel
					.chooseMarkersSet(selectedLabel, filteringSelectorPanel);
			if (selectedSet != null) {
				if (selectedSet.size() > 0) {
					selectedSetName = selectedLabel;

					for (Iterator<DSGeneMarker> iterator = selectedSet.iterator(); iterator
							.hasNext();) {
						DSGeneMarker geneMarker = (DSGeneMarker) iterator
								.next();
						markers.add(geneMarker);
					}
				}
			}
		}
		return markers;
	}

	void setFilteringSelectorPanel(DSPanel<DSGeneMarker> ap) {
		filteringSelectorPanel = ap;

		resetTargetSetModel(filteringSelectorPanel);
	}

	private void filterMarkersSet(String selectedLabel) {
		List<DSGeneMarker> markers;
		markers = getFilteredMarkers(selectedLabel);

		limitMarkers(markers);
	}

	private void heatMapSync() {
		DSGeneMarker mod = getSelectedModulatorHeatMap();
		heatmapModel.setModulator(mod);

		List<DSGeneMarker> markers = getFilteredMarkers(selectedSetName);

		heatmapModel.limitMarkers(markers);
		heatmap.reset();
	}

	static public void setCursorFinished() {
		org.geworkbench.util.Cursor cursor = org.geworkbench.util.Cursor
				.getCursor();
		if ((cursor.getAssociatedComponent() != null) && cursor.isStarted()
				&& !cursor.isFinished()) {
			cursor.setFinished(true);
		}
	}

	void populateTableTab() {
		tableTab.setMindyPlugin(this);
	}

	void populateModulatorModel() {
		modulatorModel.setMindyPlugin(this);
		((ModulatorModel) modTable.getModel()).sort(2, false); // default sort by M#
	}
}
