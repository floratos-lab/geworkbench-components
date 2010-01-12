package org.geworkbench.components.mindy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyResultRow;
import org.geworkbench.util.pathwaydecoder.mutualinformation.ModulatorInfo;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.solarmetric.ide.ui.CheckboxCellRenderer;

/**
 * MINDY result view main GUI class
 *
 * @author mhall
 * @ch2514
 * @author oshteynb
 * @version $Id: MindyPlugin.java,v 1.88 2009-11-18 18:15:46 oshteynb Exp $
 */
@SuppressWarnings("serial")
public class MindyPlugin extends JPanel {

	private static Log log = LogFactory.getLog(MindyPlugin.class);

	static final int DEFAULT_MODULATOR_LIMIT = 10;

	static final int MIN_CHECKBOX_WIDTH = 10;

	static final int MIN_MARKER_NAME_WIDTH = 200;

	static final int MIN_SCORE_WIDTH = 66;

	private static final int DATA_SIZE_THRESHOLD = 3000;

	private static final int NUMBER_TARGETS_THRESHOLD = 500;

	static final String NUM_MOD_SELECTED_LABEL = "Modulators Selected: ";

	static final String ENABLE_SELECTION = "Enable Selection";

	private static final String WARNING = "Warning";

	static enum ModulatorSort {
		Aggregate, Enhancing, Negative;
	}

	public AggregateTableModel getAggregateModel() {
		return tableTab.getAggregateModel();
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

	private JCheckBox selectionEnabledCheckBox;

	public JCheckBox getSelectionEnabledCheckBoxTarget() {
		return tableTab.getSelectionEnabledCheckBoxTarget();
	}

	private JScrollPane heatMapScrollPane;

	private List<DSGeneMarker> modulators;

	private MindyData mindyData;

	public MindyData getMindyData() {
		return mindyData;
	}

	private JTable modTable;
	private JTable listTable;

	public JTable getTargetTable() {
		return tableTab.getTargetTable();
	}

	private JCheckBox selectAllModsCheckBox;

	public JCheckBox getSelectAllModsCheckBoxTarget() {
		return tableTab.getSelectAllModsCheckBoxTarget();
	}

	private JCheckBox selectAll /* list tab (used by modulator not list ? )*/;

	private JCheckBox selectAllTargetsCheckBox;

	private JCheckBox listAllMarkersCheckBox;

	public JCheckBox getTargetAllMarkersCheckBox() {
		return tableTab.getTargetAllMarkersCheckBox();
	}

	private JButton addToSetButton /* list tab */, addToSetButtonMod;

	private JLabel numModSelectedInModTab;

	public JScrollPane getScrollPane() {
		return tableTab.getScrollPane();
	}

	Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);

	Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);

	private JComponent[] cs = new JComponent[7];

	private int dataSize = 0;

	// Contains the state of selections passed in from the marker panel and
	// overrides via All Markers checkboxes
//	MarkerLimitState globalSelectionState = new MarkerLimitState();

	// For heatmap image rendering cursor
	private JCheckBox heatmapAllMarkersCheckBox = new JCheckBox("All Markers");;

	private JButton screenshotButton = new JButton("  Image Snapshot  ");;

	private JButton refreshButton = new JButton("  Refresh HeatMap  ");;

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

	/**
	 * Receives GeneSelectorEvents from the framework (i.e. the Selector Panel)
	 *
	 * @param e
	 * @param source
	 */
/*	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {
		if (e.getPanel() != null) {
			this.selectorPanel = e.getPanel();
			mindyParamPanel.setSelectorPanel(mindyParamPanel, this.selectorPanel);
		} else {
			log
					.debug("Received Gene Selector Event: Selection panel sent was null");
		}
	}
*/
// end TODO bug 0001718
///////////////////////////////////////////////////////

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
		log.debug("\tMindyPlugin::constructor::start::"
				+ System.currentTimeMillis());

		/*   init like in param panel, check for null needed for loading saved workspace */
		MindyParamPanel mindyParamPanel = MindyAnalysis.getParamsPanel();
		if (mindyParamPanel != null){
			filteringSelectorPanel = mindyParamPanel.getSelectorPanel();
			resetTargetSetModel(filteringSelectorPanel);
		}

//		targetsSets.setSelectedIndex(0);
		targetsSets.setEditable(false);
		targetsSets.setEnabled(true);

		this.mindyData = data;
//		this.dataSize = mindyData.getData().size();
		this.dataSize = mindyData.getDataSize();
		modulators = mindyData.getModulators();

		// for heatmap image rendering cursor
		cs[0] = heatmapAllMarkersCheckBox;
		cs[1] = screenshotButton;
		cs[2] = refreshButton;
		cs[3] = heatMapModNameList;
		cs[4] = modFilterField;
		cs[5] = showSymbol;
		cs[6] = showProbeName;

		tabs = new JTabbedPane();
		{
			// Modulator Table
			modulatorModel = new ModulatorModel(this, mindyData);

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
						c.setBackground(getBackground());
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
						addToSet(selections, visualPlugin);
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

/*					numModSelectedInModTab.setText(NUM_MOD_SELECTED_LABEL
							+ model.getNumberOfModulatorsSelected());
*/
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

			JPanel taskContainer = new JPanel(new GridLayout(7, 1, 10, 10));
			taskContainer.add(dl);
			taskContainer.add(modShowSymbol);
			taskContainer.add(modShowProbeName);
			taskContainer.add(ll);
			taskContainer.add(selectAll);
			taskContainer.add(numModSelectedInModTab);
			taskContainer.add(addToSetButtonMod);
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
			((ModulatorModel) modTable.getModel()).sort(2, false); // default
			// sort by
			// M#

			tabs.add("Modulator", panel);
		}

		{
			// Modulator / Target Table
			tableTab = new MindyTableTab(visualPlugin, this);
			JSplitPane panel = tableTab.getTableTab();

			tabs.add("Table", panel);
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

			modTargetModel = new ModulatorTargetModel(mindyData);
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
						c.setBackground(getBackground());
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
					addToSet(model.getUniqueSelectedMarkers(), visualPlugin);
				}
			});
			setListControlVisibility(true);

			l = new JLabel("Marker Selection", SwingConstants.LEFT);
			l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
			l.setForeground(Color.BLUE);
			listAllMarkersCheckBox = new JCheckBox("All Markers");
			listAllMarkersCheckBox.setSelected(false);
			listAllMarkersCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {

/*					if (listAllMarkersCheckBox.isSelected()
							|| (modTargetModel.limitedModulators == null)
							|| (modTargetModel.limitedModulators.size() <= 0)
							|| (modTargetModel.limitedTargets == null)
							|| (modTargetModel.limitedTargets.size() <= 0)) {
						modTargetModel.showAllMarkers();
					} else {
						modTargetModel.showLimitedMarkers();
					}
*/
				}
			});

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
			p.add(listAllMarkersCheckBox);
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

			heatmap = new ModulatorHeatMap();
			heatmapModel = new ModulatorHeatMapModel(modulator, mindyData);
			heatmap.setModel(heatmapModel);
			heatmapModel.setHeatMap(heatmap);

			heatMapScrollPane = new JScrollPane(heatmap);
			AdjustmentListener scrollBarListener = new AdjustmentListener() {
				public void adjustmentValueChanged(AdjustmentEvent e) {
					if (!e.getValueIsAdjusting()) {
						heatmap.HeatmapChanged();
					}
				}
			};
			heatMapScrollPane.getHorizontalScrollBar().addAdjustmentListener(
					scrollBarListener);
			heatMapScrollPane.getVerticalScrollBar().addAdjustmentListener(
					scrollBarListener);

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
			heatMapModNameList.setModel(new ModulatorListModel(this, !mindyData.isAnnotated(), modulatorModel));

			screenshotButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					visualPlugin.createImageSnapshot(heatMapScrollPane
							.getViewport().getComponent(0));
				}
			});

			heatmapAllMarkersCheckBox.setSelected(false);
			heatmapAllMarkersCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					if( !processCursor() ) {
						return;
					}

					if (heatmapAllMarkersCheckBox.isSelected()) {
						if (dataSize > DATA_SIZE_THRESHOLD) {
							heatmap.prepareGraphics();
						}
						heatmapModel.limitMarkers(null);
					} else {
						List<DSGeneMarker> l = visualPlugin
								.getSelectedMarkers();
						if (l == null) {
							if (dataSize > DATA_SIZE_THRESHOLD) {
								heatmap.prepareGraphics();
							}
						} else {
							if (l.size() > NUMBER_TARGETS_THRESHOLD) {
								heatmap.prepareGraphics();
							}
						}
						heatmapModel.limitMarkers(l);
					}

					setCursorFinished();
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
								if (heatmapAllMarkersCheckBox.isSelected()) {
									if (dataSize > DATA_SIZE_THRESHOLD) {
										heatmap.prepareGraphics();
									}
									heatmapModel.limitMarkers(null);
								} else {
									List<DSGeneMarker> l = visualPlugin
											.getSelectedMarkers();
									if (l == null) {
										if (dataSize > DATA_SIZE_THRESHOLD) {
											heatmap.prepareGraphics();
										}
									} else {
										if (l.size() > NUMBER_TARGETS_THRESHOLD) {
											heatmap.prepareGraphics();
										}
									}
									heatmapModel.limitMarkers(l);
								}

								// TODO do we need all of the above?
								// set selected modulator,
								heatMapSync();

/*								List<DSGeneMarker> l = getSelectedMarkers(visualPlugin);
								heatmapModel.limitMarkers(l);
*/

							} else {
								clickCount = 0;
							}

							setCursorFinished();
						}
					});

			refreshButton.addActionListener(new ActionListener() {
				/* some redundancy after refactoring, clean later  */
				public void actionPerformed(ActionEvent actionEvent) {
					if( !processCursor() ) {
						return;
					}

					modFilterField.setText(heatMapModNameList
							.getSelectedValue().toString());

/*					heatMapModNameList.setSelectedIndex(0);

*/
					List<DSGeneMarker> l = getSelectedMarkers(visualPlugin);

					if (l == null) {
						if (dataSize > DATA_SIZE_THRESHOLD) {
							heatmap.prepareGraphics();
						}
					} else {
						if (l.size() > NUMBER_TARGETS_THRESHOLD) {
							heatmap.prepareGraphics();
						}
					}

					filterHeatMapMarkersSet(selectedSetName);
					heatMapSync();

/*					heatmapModel.limitMarkers(l);
					heatmap.reset();
*/
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

					if (heatmapAllMarkersCheckBox.isSelected()) {
						if (dataSize > DATA_SIZE_THRESHOLD) {
							heatmap.prepareGraphics();
						}
						heatmapModel.limitMarkers(null);
					} else {

						List<DSGeneMarker> l = visualPlugin
								.getSelectedMarkers();
						if (l == null) {
							if (dataSize > DATA_SIZE_THRESHOLD) {
								heatmap.prepareGraphics();
							}
						} else {
							if (l.size() > NUMBER_TARGETS_THRESHOLD) {
								heatmap.prepareGraphics();
							}
						}
						heatmapModel.limitMarkers(l);
					}

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

					if (heatmapAllMarkersCheckBox.isSelected()) {

						if (dataSize > DATA_SIZE_THRESHOLD) {
							heatmap.prepareGraphics();
						}
						heatmapModel.limitMarkers(null);
					} else {
						List<DSGeneMarker> l = visualPlugin
								.getSelectedMarkers();
						if (l == null) {
							if (dataSize > DATA_SIZE_THRESHOLD) {
								heatmap.prepareGraphics();
							}
						} else {
							if (l.size() > NUMBER_TARGETS_THRESHOLD) {
								heatmap.prepareGraphics();
							}
						}
						heatmapModel.limitMarkers(l);
					}

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
			JPanel ps = new JPanel(new GridLayout(3, 1));
			ps.add(refreshButton);
			ps.add(screenshotButton);
			ps.add(heatmapAllMarkersCheckBox);
			p.add(ps, BorderLayout.SOUTH);
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


		// TODO
		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);

//		add(tabs);
		disableTabs();

		// new panel for bug 0001718
//		targetsSets.setSelectedIndex(0);
//		targetsSets.setEditable(false);
//		targetsSets.setEnabled(false);

//		targetsSets.setEnabled(true);

		// markerSetFilteringPanel = new JPanel(new BorderLayout());

/*		markerSetFilteringPanel = new JPanel();
		markerSetFilteringPanel.setLayout(new BoxLayout(markerSetFilteringPanel, BoxLayout.X_AXIS));

		markerSetFilteringPanel.add(targetsSets);
		add(markerSetFilteringPanel);

		markerSetFilteringPanel.setVisible(true);
*/


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

/*
					 if (mindyParamPanel.chooseMarkersFromSet(selectedLabel,
							targetList)) {
						 selectedSetName = selectedLabel.trim();

					}
*/
				}

				 doResizeAndRepaint();
			}

		});

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Displayed targets filter");

//		builder.append("Target List");
		builder.append(targetsSets);
//		builder.append(targetList);
		result.add(builder.getPanel());
//		add(result, BorderLayout.CENTER);

		add(result, BorderLayout.PAGE_END);

		/* change to pub/sub */
/*		DefaultComboBoxModel paramBoxModel = (DefaultComboBoxModel) mindyParamPanel.getTargetsSets().getModel();
		resetTargetSetModel(visualPlugin);

		paramBoxModel.addListDataListener(new ListDataListener(){

			public void contentsChanged(ListDataEvent e) {

				resetTargetSetModel(visualPlugin);

			}

			public void intervalAdded(ListDataEvent e) {
				resetTargetSetModel(visualPlugin);

			}

			public void intervalRemoved(ListDataEvent e) {
				resetTargetSetModel(visualPlugin);

			}

		});
*/
/*		frame.pack();
        frame.setVisible(true);
*/

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

/*					org.geworkbench.util.Cursor cursor = org.geworkbench.util.Cursor
							.getCursor();
					if (cursor.isStarted() && !cursor.isFinished()) {
						return;
					} else {
						cursor.setAssociatedComponent(MindyPlugin.this);
						try{
							cursor.start();
						} catch (Exception e){
							log.warn(e.getMessage());
							return;
						}
					}
*/
				}
			}
		});

		log.debug("\tMindyPlugin::constructor::end::"
				+ System.currentTimeMillis());

		result.setVisible(true);

		tabs.setVisible(true);

		doResizeAndRepaint();
	}

	private void doResizeAndRepaint() {
		revalidate();
		repaint();
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
		table.setDefaultRenderer(Boolean.class, new CheckboxCellRenderer());
	}

	private void setListTableViewOptions() {
		boolean selected = selectionEnabledCheckBox.isSelected();
		if (selected) {
			listTable.getColumnModel().getColumn(0).setMaxWidth(30);
			listTable.getColumnModel().getColumn(2).setMaxWidth(30);
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
	public void limitMarkers(List<DSGeneMarker> markers) {
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
		            ModulatorInfo modInfo = new ModulatorInfo(mod);

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

			setCursor(normalCursor);

			// target table tab
			getAggregateModel().limitMarkers(markers);
			doResizeAndRepaint();

			// list table tab
			modTargetModel.limitMarkers(markers);

		} catch (Exception e){
			log.warn(e.getMessage());
		}

		// probably not needed as it is processed with other tabs
/*		try{
			cursor.start();
		} catch (Exception e){
			log.warn(e.getMessage());
			return;
		}
		if ((!heatmapAllMarkersCheckBox.isSelected()) && (markers != null)
				&& (markers.size() > 0)) {
//			heatmap.setTargetLimits(markers);
			heatmapModel.limitMarkers(markers);
//			rebuildHeatMap(markers);
//			rebuildHeatMap(markers);
		} else {
			//  is it needed ?
			heatmapModel.limitMarkers(null);
//			rebuildHeatMap();
//			rebuildHeatMap(null);
		}

*/
	}

	public void rememberTableSelections() {
		// modulator table tab
		modulatorModel.rememberSelections();

		// target table tab
		getAggregateModel().rememberSelections();

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

	@SuppressWarnings("unchecked") void addToSet(List<DSGeneMarker> selections,
			final MindyVisualComponent visualPlugin) {
		if (visualPlugin == null) {
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
			if (((visualPlugin.getSelectorPanel() == null) || (visualPlugin
					.getSelectorPanel().panels() == null))
					&& ((visualPlugin.getSelectedMarkers() == null) || (visualPlugin
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
				visualPlugin
						.publishSubpanelChangedEvent(new SubpanelChangedEvent(
								DSGeneMarker.class, subpanel,
								SubpanelChangedEvent.NEW));
				return;
			}

			// Checking to see if a subpanel already has the same label
			if ((visualPlugin.getSelectorPanel() != null)
					&& (visualPlugin.getSelectorPanel().panels() != null)) {
				DSPanel selectorPanel = visualPlugin.getSelectorPanel();
				for (int i = 0; i < selectorPanel.panels().size(); i++) {
					Object o = selectorPanel.panels().get(i);
					if ((o instanceof DSPanel)
							&& ((DSPanel) o).getLabel().equals(tmpLabel)) {
						subpanel = ((DSPanel) o);
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
						visualPlugin
								.publishSubpanelChangedEvent(new SubpanelChangedEvent(
										DSGeneMarker.class, subpanel,
										SubpanelChangedEvent.SET_CONTENTS));
						return;
					}
				}
			} else {
				// If not, check if Mindy plugin has a selected marker set
				if ((visualPlugin.getSelectedMarkers() != null)
						&& (visualPlugin.getSelectedMarkers().size() > 0)) {
					boolean needToAdd = false;
					subpanel = new CSPanel<DSGeneMarker>();
					subpanel.setLabel(tmpLabel);
					for (int i = 0; i < selections.size(); i++) {
						Object o = selections.get(i);
						if ((o instanceof DSGeneMarker)
								&& (!visualPlugin.getSelectedMarkers()
										.contains((DSGeneMarker) o))) {
							subpanel.add((DSGeneMarker) o);
							needToAdd = true;
						}
					}
					if (needToAdd) {
						for (int i = 0; i < visualPlugin.getSelectedMarkers()
								.size(); i++) {
							Object o = visualPlugin.getSelectedMarkers().get(i);
							if (o instanceof DSGeneMarker) {
								subpanel.add((DSGeneMarker) o);
							}
						}
						visualPlugin
								.publishSubpanelChangedEvent(new SubpanelChangedEvent(
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
				visualPlugin
						.publishSubpanelChangedEvent(new SubpanelChangedEvent(
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


	// Models and support classes follow

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

	private List<DSGeneMarker> getSelectedMarkers(
			final MindyVisualComponent visualPlugin) {
		List<DSGeneMarker> l;
		if (heatmapAllMarkersCheckBox.isSelected()) {
			l = null;
//						heatmapModel.limitMarkers(null);
		} else {
			l = visualPlugin
					.getSelectedMarkers();

//						heatmapModel.limitMarkers(l);
		}
		return l;
	}

/*	private void resetTargetSetModel(final MindyVisualComponent visualPlugin) {
		mindyParamPanel = visualPlugin.getParams();


		DefaultComboBoxModel paramBoxModel = (DefaultComboBoxModel) mindyParamPanel
				.getTargetsSets().getModel();
		DefaultComboBoxModel targetsSetModel = (DefaultComboBoxModel) targetsSets
				.getModel();

		// tmp solution to keep selection
//		int selection = targetsSets.getSelectedIndex();

		targetsSetModel.removeAllElements();

		targetsSetModel.insertElementAt(ALL_NON_ZERO_MARKERS, 0);
		for (int i = 1; i < paramBoxModel.getSize(); i++) {
			targetsSetModel.insertElementAt(paramBoxModel
					.getElementAt(i), i);
		}

//		int sel = mindyParamPanel.getTargetsSets().getSelectedIndex();
		targetsSets.setSelectedItem(selectedSetName);

		if(targetsSetModel.getSize()>=selection){
			targetsSets.setSelectedIndex(selection);

		}

	}
*/

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
//			selectedSetName = ALL_NON_ZERO_MARKERS;

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

	private void filterHeatMapMarkersSet(String selectedLabel) {
		List<DSGeneMarker> markers;
		markers = getFilteredMarkers(selectedLabel);

		heatmapModel.limitMarkers(markers);
	}

	private void filterMarkersSet(String selectedLabel) {
		List<DSGeneMarker> markers;
		markers = getFilteredMarkers(selectedLabel);

		limitMarkers(markers);
	}

	private void heatMapSync() {
		DSGeneMarker mod = getSelectedModulatorHeatMap();
		heatmapModel.setModulator(mod);

		filterHeatMapMarkersSet(selectedSetName);
	}

	static public void setCursorFinished() {
		org.geworkbench.util.Cursor cursor = org.geworkbench.util.Cursor
				.getCursor();
		if ((cursor.getAssociatedComponent() != null) && cursor.isStarted()
				&& !cursor.isFinished()) {
			cursor.setFinished(true);
		}
	}

	JCheckBox getSelectAllTargetsCheckBoxTarget() {
		return tableTab.getSelectAllTargetsCheckBoxTarget();
	}

	JButton getAddToSetButtonTarget() {
		return tableTab.getAddToSetButtonTarget();
	}

	/**
	 * Compare M#, M+, or M- of two gene markers (for sorting).
	 *
	 * @author mhall
	 * @version $Id: MindyPlugin.java,v 1.88 2009-11-18 18:15:46 oshteynb Exp $
	 */
	class ModulatorStatComparator implements Comparator<DSGeneMarker> {

		private MindyData data;

		private ModulatorSort sortType;

		/**
		 * Constructor.
		 *
		 * @param data -
		 *            MINDY data
		 * @param sortType -
		 *            specifies whether to sort by M#, M+, or M-
		 */
		public ModulatorStatComparator(MindyData data, ModulatorSort sortType) {
			this.data = data;
			this.sortType = sortType;
		}

		/**
		 * Compare two gene markers based on M#, M+, or M-. The choice is
		 * determined by sort type specified in the constructor.
		 *
		 * @param dsGeneMarker -
		 *            the first gene marker to be compared
		 * @param dsGeneMarker1 -
		 *            the second gene marker to be compared
		 * @return A negative integer if the first gene marker precedes the
		 *         second. Zero if the two markers are the same. A positive
		 *         integer if the second marker precedes the first.
		 */
		public int compare(DSGeneMarker dsGeneMarker, DSGeneMarker dsGeneMarker1) {
			if (sortType == ModulatorSort.Aggregate) {
				return data.getStatistics(dsGeneMarker1).getCount()
						- data.getStatistics(dsGeneMarker).getCount();
			} else if (sortType == ModulatorSort.Enhancing) {
				return data.getStatistics(dsGeneMarker1).getMover()
						- data.getStatistics(dsGeneMarker).getMover();
			} else {
				return data.getStatistics(dsGeneMarker1).getMunder()
						- data.getStatistics(dsGeneMarker).getMunder();
			}
		}

	}

	/**
	 * Table data model for the list table.
	 *
	 * @author mhall
	 * @author ch2514
	 * @version $Id: MindyPlugin.java,v 1.88 2009-11-18 18:15:46 oshteynb Exp $
	 */
	class ModulatorTargetModel extends DefaultTableModel {

		private boolean[] modChecks;

		private boolean[] targetChecks;

		private ArrayList<DSGeneMarker> enabledModulators;

		private ArrayList<DSGeneMarker> enabledTargets;

		private List<DSGeneMarker> limitedModulators;

		private List<DSGeneMarker> limitedTargets;

		private ArrayList<DSGeneMarker> selectedModulators;

		private ArrayList<DSGeneMarker> selectedTargets;

		private MindyData mindyData;

		private String[] columnNames = new String[] { " ", "Modulator", "  ",
				"Target", "Score" };

		private ArrayList<MindyResultRow> rows = new ArrayList<MindyResultRow>();

		private boolean[] ascendSortStates;

		private boolean showProbeName = false;

		private boolean limMarkers = false;

		public void setLimitedTargets(List<DSGeneMarker> limitedTargets) {
			this.limitedTargets = limitedTargets;
		}

		/**
		 * Constructor.
		 *
		 * @param mindyData -
		 *            data for the MINDY component
		 */
		public ModulatorTargetModel(MindyData mindyData) {
			this.showProbeName = !mindyData.isAnnotated();
			this.modChecks = new boolean[mindyData.getDataSize()];
			this.targetChecks = new boolean[mindyData.getDataSize()];
			enabledModulators = new ArrayList<DSGeneMarker>(mindyData.getDataSize());
			enabledTargets = new ArrayList<DSGeneMarker>(mindyData.getDataSize());
			limitedModulators = new ArrayList<DSGeneMarker>();
			limitedTargets = new ArrayList<DSGeneMarker>();
			selectedModulators = new ArrayList<DSGeneMarker>();
			selectedTargets = new ArrayList<DSGeneMarker>();

			for (int i = 0; i < mindyData.getDataSize(); i++) {
				this.modChecks[i] = false;
				this.targetChecks[i] = false;
			}
			this.mindyData = mindyData;
			this.ascendSortStates = new boolean[columnNames.length];
			for (int i = 0; i < this.ascendSortStates.length; i++)
				this.ascendSortStates[i] = true;
		}

		/**
		 * Get the enabled modulators.
		 *
		 * @return a list of enabled modulators
		 */
		public ArrayList<DSGeneMarker> getEnabledModulators() {
			return enabledModulators;
		}

		/**
		 * Set the list of enabled modulators.
		 *
		 * @param enabledModulators -
		 *            list of enabled modulators
		 */
		public void setEnabledModulators(
				ArrayList<DSGeneMarker> enabledModulators) {
			this.enabledModulators = enabledModulators;
		}

		public void disableAllModulators() {
			this.enabledModulators.clear();
			this.selectAllModulators(false);
			this.selectAllTargets(false);
		}

		/**
		 * Get a list of enabled targets.
		 *
		 * @return a list of enabled targets.
		 */
		public ArrayList<DSGeneMarker> getEnabledTargets() {
			return enabledTargets;
		}

		/**
		 * Set the list of enabled targets.
		 *
		 * @param enabledTargets -
		 *            list of enabled targets
		 */
		public void setEnabledTargets(ArrayList<DSGeneMarker> enabledTargets) {
			this.enabledTargets = enabledTargets;
		}

		/**
		 * Enable a specified modulator
		 *
		 * @param mod -
		 *            the modulator to enable
		 */
		public void enableModulator(DSGeneMarker mod) {
			if (!enabledModulators.contains(mod)) {
				enabledModulators.add(mod);
				redrawTable();
			}
		}

		/**
		 * Disable a specified modulator
		 *
		 * @param mod -
		 *            the modulator to disable
		 */
		public void disableModulator(DSGeneMarker mod) {
			enabledModulators.remove(mod);
			redrawTable();
		}
/*
		*//**
		 * Enable a specified target
		 *
		 * @param target -
		 *            the target to enable
		 *//*
		public void enableTarget(DSGeneMarker target) {
			if (!enabledTargets.contains(target)) {
				enabledTargets.add(target);
				redrawTable();
			}
		}

		*//**
		 * Disable a specified target
		 *
		 * @param target -
		 *            the target to disable
		 *//*
		public void disableTarget(DSGeneMarker target) {
			enabledTargets.remove(target);
			redrawTable();
		}
*/
		private void recalculateRows() {
			rows.clear();
			if ((this.enabledModulators != null)
					&& (this.enabledModulators.size() > 0)) {
				if (listAllMarkersCheckBox.isSelected()) {
					for (DSGeneMarker modMarker : enabledModulators) {
						rows.addAll(mindyData.getRows(modMarker));
					}
				} else {
					List<DSGeneMarker> mods = this.enabledModulators;
					if ((this.limitedModulators != null)
							&& (this.limitedModulators.size() > 0))
						mods = this.limitedModulators;
					if ((this.limitedTargets != null)
//							&& (this.limitedTargets.size() > 0)
							) {
						for (DSGeneMarker modMarker : mods) {
							rows.addAll(mindyData.getRows(modMarker, limitedTargets));
						}
					} else {
						for (DSGeneMarker modMarker : enabledModulators) {
							rows.addAll(mindyData.getRows(modMarker));
						}
					}
				}
			}
			for (int i = 0; i < rows.size(); i++) {
				MindyResultRow r = rows.get(i);
				if ((r != null) && (r.getScore() == 0)) {
					rows.remove(i);
					i--;
				}
			}
			this.rememberSelections();
		}

		void rememberSelections() {
			modChecks = new boolean[rows.size()];
			targetChecks = new boolean[rows.size()];
			for (int i = 0; i < rows.size(); i++) {
				if (this.selectedModulators
						.contains(rows.get(i).getModulator()))
					modChecks[i] = true;
				else
					modChecks[i] = false;
				if (this.selectedTargets.contains(rows.get(i).getTarget()))
					targetChecks[i] = true;
				else
					targetChecks[i] = false;
			}
		}

		/**
		 * Get the number of columns in the list table.
		 *
		 * @return the number of columns in the list table
		 */
		public int getColumnCount() {
			return columnNames.length;
		}

		// called from MindyVisualComponent
		// i.e. when SelectionPanel changes marker set selections via
		// GeneSelectorEvent
		/**
		 * Callback method for the list table when the user changes marker set
		 * selections in the Selection Panel.
		 *
		 * @param -
		 *            list of selected markers
		 */
		public void limitMarkers(List<DSGeneMarker> limitList) {
			if (limitList == null) {
				limitedModulators = null;
				limitedTargets = null;
				limMarkers = false;
				this.checkSelectedMarkers(true);
				log.debug("Cleared modulator and target limits.");
			} else {

				limitedModulators = new ArrayList<DSGeneMarker>();
				limitedTargets = new ArrayList<DSGeneMarker>();
				limMarkers = true;

//				limitedTargets = limitList;

				for (DSGeneMarker marker : limitList) {
					if (enabledTargets.contains(marker)) {
						limitedTargets.add(marker);
					}
				}

				this.checkSelectedMarkers(false);
				log.debug("Limited list table to " + limitedModulators.size()
						+ " mods. and " + limitedTargets.size() + " targets.");
			}

/*			if (!listAllMarkersCheckBox.isSelected()) {
				redrawTable();
			}
*/
			redrawTable();
			doResizeAndRepaint();
		}

		private void doResizeAndRepaint() {
			revalidate();
			repaint();
		}


		// called from "All Markers" checkbox
		/**
		 * Show only the markers selected in the marker sets on the Selector
		 * Panel. Applies only to the list table.
		 */
		public void showLimitedMarkers() {
			this.limMarkers = true;
			this.checkSelectedMarkers(false);
			redrawTable();
		}

		// called from "All Markers" checkbox
		/**
		 * Show all markers. Applies only to the list table.
		 */
		public void showAllMarkers() {
			this.limMarkers = false;
			this.checkSelectedMarkers(true);
			redrawTable();
		}

		private void checkSelectedMarkers(boolean showAll) {
			if (showAll) {
				if ((this.selectedModulators != null)
						&& (this.selectedModulators.size() > 0)
						&& (this.enabledModulators.size() == this.selectedModulators
								.size())) {
					selectAllModsCheckBox.setSelected(true);
				} else {
					selectAllModsCheckBox.setSelected(false);
				}
				if ((this.selectedTargets != null)
						&& (this.selectedTargets.size() > 0)
						&& (this.enabledTargets.size() == this.selectedTargets
								.size())) {
					selectAllTargetsCheckBox.setSelected(true);
				} else {
					selectAllTargetsCheckBox.setSelected(false);
				}
			} else {
				if ((this.selectedModulators != null)
						&& (this.limitedModulators != null)
						&& (this.selectedModulators.size() > 0)
						&& (this.limitedModulators.size() > 0)
						&& (this.limitedModulators.size() <= this.selectedModulators
								.size())) {
					// need to match items in lim and selected
					boolean allMods = true;
					for (DSGeneMarker m : this.limitedModulators) {
						if (!this.selectedModulators.contains(m)) {
							allMods = false;
							break;
						}
					}

					selectAllModsCheckBox.setSelected(allMods);
				} else {
					selectAllModsCheckBox.setSelected(false);
				}
				if ((this.selectedTargets != null)
						&& (this.limitedTargets != null)
						&& (this.selectedTargets.size() > 0)
						&& (this.limitedTargets.size() > 0)
						&& (this.limitedTargets.size() <= this.selectedTargets
								.size())) {
					// need to match items in lim and selected
					boolean allTargets = true;
					for (DSGeneMarker t : this.limitedTargets) {
						if (!this.selectedTargets.contains(t)) {
							allTargets = false;
							break;
						}
					}
					selectAllTargetsCheckBox.setSelected(allTargets);
				} else {
					selectAllTargetsCheckBox.setSelected(false);
				}
			}
		}

		public void redrawTable() {
			recalculateRows();

			setListTableViewOptions();

			fireTableStructureChanged();
			fireTableDataChanged();
		}

		/**
		 * Get the number of rows on the list table.
		 *
		 * @return number of rows on the table
		 */
		public int getRowCount() {
			if ((listAllMarkersCheckBox != null)
					&& listAllMarkersCheckBox.isSelected()) {
				if (modChecks == null) {
					return 0;
				} else {
					return rows.size();
				}
			} else {
				if (rows != null)
					return rows.size();
				else
					return 0;
			}
		}

		/**
		 * Whether or not the specified list table cell is editable.
		 *
		 * @param rowIndex -
		 *            row index of the table cell
		 * @prarm columnIndex - column index of the table cell
		 * @return true if the table cell is editable, and false otherwise
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if ((columnIndex == 0) || (columnIndex == 2)) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Get the class object representing the specified table column.
		 *
		 * @param columnIndex -
		 *            column index
		 * @return the class object representing the table column
		 */
		@SuppressWarnings("unchecked")
		public Class getColumnClass(int columnIndex) {
			if (columnIndex == 0 || columnIndex == 2) {
				return Boolean.class;
			} else if (columnIndex == columnNames.length - 1) {
				return Float.class;
			} else {
				return String.class;
			}
		}

		/**
		 * Get the values of list table cells.
		 *
		 * @param rowIndex -
		 *            row index of the cell
		 * @param columnIndex -
		 *            column index of the cell
		 * @return the value object of specified table cell
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return modChecks[rowIndex];
			} else if (columnIndex == 1) {
				return getMarkerDisplayName(this.isShowProbeName(), rows.get(rowIndex)
						.getModulator());
			} else if (columnIndex == 2) {
				return targetChecks[rowIndex];
			} else if (columnIndex == 3) {
				return getMarkerDisplayName(this.isShowProbeName(), rows.get(rowIndex)
						.getTarget());
			} else if (columnIndex == 4) {
				return rows.get(rowIndex).getScore();
			} else {
				log.error("Requested unknown column");
				return null;
			}
		}

		/**
		 * Set values of list table cells.
		 *
		 * @param aValue -
		 *            value of the cell
		 * @param rowIndex -
		 *            row index of the cell
		 * @param columnIndex -
		 *            column index of the cell
		 */
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				String marker = getMarkerDisplayName(this.isShowProbeName(), rows.get(rowIndex)
						.getModulator()).trim();
				boolean b = (Boolean) aValue;
				for (int i = 0; i < rows.size(); i++) {
					if (marker.equals(getMarkerDisplayName(this.isShowProbeName(),
							rows.get(i).getModulator()).trim())) {
						modChecks[i] = b;
						DSGeneMarker m = rows.get(rowIndex).getModulator();
						if (modChecks[i] == true) {
							if (!this.selectedModulators.contains(m)) {
								this.selectedModulators.add(m);
							}
						} else {
							this.selectedModulators.remove(m);
						}
					}
				}
				this.fireTableDataChanged();
			} else if (columnIndex == 2) {
				String marker = getMarkerDisplayName(this.isShowProbeName(),
						rows.get(rowIndex).getTarget()).trim();
				boolean b = (Boolean) aValue;
				for (int i = 0; i < rows.size(); i++) {
					if (marker.equals(getMarkerDisplayName(this.isShowProbeName(),
							rows.get(i).getTarget()).trim())) {
						targetChecks[i] = b;
						DSGeneMarker t = rows.get(rowIndex).getTarget();
						if (targetChecks[i] == true) {
							if (!this.selectedTargets.contains(t)) {
								this.selectedTargets.add(t);
							}
						} else {
							this.selectedTargets.remove(t);
						}
					}
				}
				this.fireTableDataChanged();
			}

			selectionEnabledCheckBox.setText(MindyPlugin.ENABLE_SELECTION + " "
					+ this.getNumberOfMarkersSelected());

			if (this.limMarkers) {
				if ((this.selectedModulators.size() > 0)
						&& ((this.limitedModulators.size() > 0) || (this.enabledModulators
								.size() > 0))
						&& ((this.getSelectedModulators().size() == this.limitedModulators
								.size()) || (this.selectedModulators.size() == this.enabledModulators
								.size()))) {
					selectAllModsCheckBox.setSelected(true);
				} else {
					selectAllModsCheckBox.setSelected(false);
				}
				if ((this.selectedTargets.size() > 0)
						&& ((this.limitedTargets.size() > 0) || (this.enabledTargets
								.size() > 0))
						&& ((this.getSelectedTargets().size() == this.limitedTargets
								.size()) || (this.selectedTargets.size() == this.enabledTargets
								.size()))) {
					selectAllTargetsCheckBox.setSelected(true);
				} else {
					selectAllTargetsCheckBox.setSelected(false);
				}
			} else {
				if ((this.selectedModulators.size() > 0)
						&& (this.enabledModulators.size() > 0)
						&& (this.getSelectedModulators().size() == this
								.getEnabledModulators().size())) {
					selectAllModsCheckBox.setSelected(true);
				} else {
					selectAllModsCheckBox.setSelected(false);
				}
				if ((this.selectedTargets.size() > 0)
						&& (this.enabledTargets.size() > 0)
						&& (this.getSelectedTargets().size() == this
								.getEnabledTargets().size())) {
					selectAllTargetsCheckBox.setSelected(true);
				} else {
					selectAllTargetsCheckBox.setSelected(false);
				}
			}
		}

		/**
		 * Get the column name of the specified column index.
		 *
		 * @param columnIndex -
		 *            index of the column
		 * @return name of the column
		 */
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		private void selectAllModulators(boolean select) {
			this.selectedModulators.clear();
			if (select) {
				this.selectedModulators.addAll(this.enabledModulators);
			}
			for (int i = 0; i < modChecks.length; i++) {
				modChecks[i] = select;
			}
			if (!select) {
				// selectionEnabledCheckBox.setText(MindyPlugin.ENABLE_SELECTION
				// + " [" + this.getUniqueSelectedMarkers().size() + "]");
				selectAllModsCheckBox.setSelected(false);
			}
			this.fireTableDataChanged();
		}

		/**
		 * Get the sorting states (ascending or descending) of each column in
		 * the list table.
		 *
		 * @return a list of sorting states (ascending = true, descending =
		 *         false)
		 */
		public boolean[] getAscendSortStates() {
			return this.ascendSortStates;
		}

		/**
		 * Set the sorting states (ascending or descending) of each column in
		 * the list table.
		 *
		 * @param states -
		 *            a list of sorting states (ascending = true, descending =
		 *            false)
		 */
		public void setAscendSortStates(boolean[] states) {
			this.ascendSortStates = states;
		}

		private void selectAllTargets(boolean select) {
			this.selectedTargets.clear();
			if (select) {
				if (this.limitedTargets == null){
					this.selectedTargets.addAll(this.enabledTargets);
				} else {
					this.selectedTargets.addAll(this.limitedTargets);
				}
			}

			for (int i = 0; i < targetChecks.length; i++) {
				targetChecks[i] = select;
			}
			if (!select) {
				// selectionEnabledCheckBox.setText(MindyPlugin.ENABLE_SELECTION
				// + " [" + this.getUniqueSelectedMarkers().size() + "]");
				selectAllTargetsCheckBox.setSelected(false);
			}
			this.fireTableDataChanged();
		}

		/**
		 * Get the list of user selected modulators.
		 *
		 * @return the list of selected modulators
		 */
		public List<DSGeneMarker> getSelectedModulators() {
			return this.selectedModulators;
		}

		/**
		 * Get the list of user selected targets.
		 *
		 * @return the list of selected targets
		 */
		public List<DSGeneMarker> getSelectedTargets() {
			return this.selectedTargets;
		}

		/**
		 * Get the union of selected modulators and targets for the list table.
		 *
		 * @return the union of selected modulators and targets
		 */
		public List<DSGeneMarker> getUniqueSelectedMarkers() {
			if ((this.selectedTargets != null)
					&& (this.selectedModulators != null)) {
				int tsize = this.selectedTargets.size();
				int msize = this.selectedModulators.size();
				ArrayList<DSGeneMarker> result = new ArrayList<DSGeneMarker>(
						tsize + msize);
				if (tsize >= msize) {
					result.addAll(this.selectedTargets);
					for (DSGeneMarker m : this.selectedModulators) {
						if (!this.selectedTargets.contains(m))
							result.add(m);
					}
				} else {
					result.addAll(this.selectedModulators);
					for (DSGeneMarker m : this.selectedTargets) {
						if (!this.selectedModulators.contains(m))
							result.add(m);
					}
				}
				result.trimToSize();
				return result;
			}
			if ((this.selectedTargets == null)
					&& (this.selectedModulators != null))
				return this.selectedModulators;
			if ((this.selectedTargets != null)
					&& (this.selectedModulators == null))
				return this.selectedTargets;

			return null;
		}

		public int getNumberOfMarkersSelected() {
			int tsize = 0;
			int msize = 0;
			if (this.selectedTargets != null)
				tsize = this.selectedTargets.size();
			if (this.selectedModulators != null)
				msize = this.selectedModulators.size();
			if ((tsize > 0) && (msize > 0)) {
				if (tsize >= msize) {
					int result = tsize;
					for (DSGeneMarker m : this.selectedModulators) {
						if (!this.selectedTargets.contains(m))
							result++;
					}
					return result;
				} else {
					int result = msize;
					for (DSGeneMarker m : this.selectedTargets) {
						if (!this.selectedModulators.contains(m))
							result++;
					}
					return result;
				}
			}
			if ((tsize == 0) && (msize > 0)) {
				return msize;
			}

			if ((tsize > 0) && (msize == 0)) {
				return tsize;
			}

			return 0;
		}

		/**
		 * Handles table column sorting for the list table.
		 *
		 * @param col -
		 *            the column index of the column to sort
		 * @param ascending -
		 *            if true, sort the column in ascending order. Otherwise,
		 *            sort in descending order.
		 */
		public void sort(int col, boolean ascending) {
			log.debug("\t\tlist model::sort::start::"
					+ System.currentTimeMillis());
			if ((col == 0) || (col == 2))
				return;
			if (col == 1) {
				setCursor(hourglassCursor);
				Collections.sort(rows, new MindyRowComparator(
						MindyRowComparator.MODULATOR, ascending, mindyData, showProbeName ));
				this.rememberSelections();
				setCursor(normalCursor);
			}
			if (col == 3) {
				setCursor(hourglassCursor);
				Collections.sort(rows, new MindyRowComparator(
						MindyRowComparator.TARGET, ascending, mindyData, showProbeName));
				this.rememberSelections();
				setCursor(normalCursor);
			}
			if (col == 4) {
				setCursor(hourglassCursor);
				Collections.sort(rows, new MindyRowComparator(
						MindyRowComparator.SCORE, ascending, mindyData));
				this.rememberSelections();
				setCursor(normalCursor);
			}
			fireTableStructureChanged();
			log.debug("\t\tlist model::sort::end::"
					+ System.currentTimeMillis());
		}

		/**
		 * Check to see if the list table should display probe names or gene
		 * names.
		 *
		 * @return If true, the list table displays probe names. If not, the
		 *         list table displays gene names.
		 */
		public boolean isShowProbeName() {
			return this.showProbeName;
		}

		/**
		 * Specify whether or not the list table should display probe names or
		 * gene names.
		 *
		 * @param showProbeName -
		 *            if true, the list table displays probe names. If not, the
		 *            list table displays gene names.
		 */
		public void setShowProbeName(boolean showProbeName) {
			this.showProbeName = showProbeName;
		}
	}
}
