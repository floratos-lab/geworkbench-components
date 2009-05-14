package org.geworkbench.components.mindy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
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
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyGeneMarker;
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
 * @version $Id: MindyPlugin.java,v 1.85 2009-05-14 17:52:17 oshteynb Exp $
 */
@SuppressWarnings("serial")
public class MindyPlugin extends JPanel {

	static Log log = LogFactory.getLog(MindyPlugin.class);

	private static final int DEFAULT_MODULATOR_LIMIT = 10;

	private static final int MIN_CHECKBOX_WIDTH = 10;

	private static final int MIN_MARKER_NAME_WIDTH = 200;

	private static final int MIN_SCORE_WIDTH = 66;

	private static final int DATA_SIZE_THRESHOLD = 3000;

	private static final int NUMBER_TARGETS_THRESHOLD = 500;

	static final String NUM_MOD_SELECTED_LABEL = "Modulators Selected: ";

	private static final String ENABLE_SELECTION = "Enable Selection";

	private static final String WARNING = "Warning";

	private static enum ModulatorSort {
		Aggregate, Enhancing, Negative;
	}

	AggregateTableModel aggregateModel;

	ModulatorTargetModel modTargetModel;

	private ModulatorModel modulatorModel;

	private ModulatorHeatMap heatmap;

	private ModulatorHeatMapModel heatmapModel;

	private JTabbedPane tabs;

	private JCheckBox selectionEnabledCheckBox, selectionEnabledCheckBoxTarget;

	private JScrollPane heatMapScrollPane;

	private List<DSGeneMarker> modulators;

	private MindyData mindyData;

	private JTable modTable, listTable, targetTable;

	private JCheckBox selectAllModsCheckBox,
			selectAllModsCheckBoxTarget;

	private JCheckBox selectAll /* list tab (used by modulator not list ? )*/;

	private JCheckBox selectAllTargetsCheckBox, selectAllTargetsCheckBoxTarget;

	private JCheckBox targetAllMarkersCheckBox,
			listAllMarkersCheckBox;

	private JButton addToSetButton /* list tab */, addToSetButtonTarget,
			addToSetButtonMod;

	private JLabel numModSelectedInModTab;

	private JCheckBox[] boxes;

	private JScrollPane scrollPane;

	private Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);

	private Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);

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

//	private MindyParamPanel params;

	private MindyVisualComponent visualPlugin;

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

		this.visualPlugin = visualPlugin;

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
		this.dataSize = mindyData.getData().size();
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
			modTable.getColumnModel().getColumn(0).setMaxWidth(15);
			modTable.setAutoCreateColumnsFromModel(false);
			modTable.getTableHeader().addMouseListener(
					new ColumnHeaderListener());

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
			JLabel dl = new JLabel("Marker Display  ", SwingConstants.LEFT);
			dl.setFont(new Font(dl.getFont().getName(), Font.BOLD, 12));
			dl.setForeground(Color.BLUE);
			JRadioButton showSymbol = new JRadioButton("Symbol");
			showSymbol.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					aggregateModel.setShowProbeName(false);
					aggregateModel.fireTableStructureChanged();
					MindyPlugin.this
							.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
									.isSelected());
				}
			});
			JRadioButton showProbeName = new JRadioButton("Probe Name");
			showProbeName.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					aggregateModel.setShowProbeName(true);
					aggregateModel.fireTableStructureChanged();
					MindyPlugin.this
							.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
									.isSelected());
				}
			});
			ButtonGroup displayGroup = new ButtonGroup();
			displayGroup.add(showSymbol);
			displayGroup.add(showProbeName);
			if (this.mindyData.isAnnotated())
				showSymbol.setSelected(true);
			else
				showProbeName.setSelected(true);

//			JLabel ls = new JLabel("Sorting", SwingConstants.LEFT);
			JLabel ls = new JLabel("Modulator Sorting", SwingConstants.LEFT);
			ls.setFont(new Font(ls.getFont().getName(), Font.BOLD, 12));
			ls.setForeground(Color.BLUE);

			JRadioButton sortOptionsAggregate = new JRadioButton("Aggregate");
			JRadioButton sortOptionsEnhancing = new JRadioButton("Enhancing");
			JRadioButton sortOptionsNegative = new JRadioButton("Negative");
			ButtonGroup sortGroup = new ButtonGroup();
			sortGroup.add(sortOptionsAggregate);
			sortGroup.add(sortOptionsEnhancing);
			sortGroup.add(sortOptionsNegative);
			ActionListener sortAction = new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					String selected = actionEvent.getActionCommand().toString();
					if (selected.equals("Aggregate")) {
						log.debug("Setting sort to Aggregate");
						aggregateModel
								.setModulatorSortMethod(ModulatorSort.Aggregate);
					} else if (selected.equals("Enhancing")) {
						log.debug("Setting sort to Enhancing");
						aggregateModel
								.setModulatorSortMethod(ModulatorSort.Enhancing);
					} else {
						log.debug("Setting sort to Negative");
						aggregateModel
								.setModulatorSortMethod(ModulatorSort.Negative);
					}
					MindyPlugin.this.clearAllTargetTableModulatorSelections();
				}
			};
			sortOptionsAggregate.addActionListener(sortAction);
			sortOptionsEnhancing.addActionListener(sortAction);
			sortOptionsNegative.addActionListener(sortAction);
			sortOptionsAggregate.setSelected(true);

			JPanel limitControls = new JPanel(new BorderLayout());
			JLabel lm = new JLabel("Modulator Limits", SwingConstants.LEFT);
			lm.setFont(new Font(lm.getFont().getName(), Font.BOLD, 12));
			lm.setForeground(Color.BLUE);
			final JCheckBox modulatorLimits = new JCheckBox("Limit To Top");
			modulatorLimits.setSelected(true);
			limitControls.add(modulatorLimits, BorderLayout.WEST);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
					DEFAULT_MODULATOR_LIMIT, 1, 1000, 1);
			final JSpinner modLimitValue = new JSpinner(spinnerModel);
			limitControls.add(modLimitValue, BorderLayout.CENTER);

			JLabel ld = new JLabel("Display Options", SwingConstants.LEFT);
			ld.setFont(new Font(ld.getFont().getName(), Font.BOLD, 12));
			ld.setForeground(Color.BLUE);
			final JCheckBox colorCheck = new JCheckBox("Color View");
			final JCheckBox scoreCheck = new JCheckBox("Score View");

			JLabel lmp = new JLabel("Marker Selection  ", SwingConstants.LEFT);
			lmp.setFont(new Font(lmp.getFont().getName(), Font.BOLD, 12));
			lmp.setForeground(Color.BLUE);
			targetAllMarkersCheckBox = new JCheckBox("All Markers");
			targetAllMarkersCheckBox.setSelected(false);

			final ColorGradient gradient = new ColorGradient(Color.blue,
					Color.red);
			gradient.addColorPoint(Color.white, 0f);

			aggregateModel = new AggregateTableModel(mindyData);
			aggregateModel.setModLimit(DEFAULT_MODULATOR_LIMIT);
			aggregateModel.setModulatorsLimited(modulatorLimits.isSelected());
			targetTable = new JTable(aggregateModel) {
				public Component prepareRenderer(
						TableCellRenderer tableCellRenderer, int row, int col) {
					Component component = super.prepareRenderer(
							tableCellRenderer, row, col);
					if (row % 2 == 0 && !isCellSelected(row, col)) {
						component.setBackground(new Color(237, 237, 237));
					} else {
						// If not shaded, match the table's background
						component.setBackground(getBackground());
					}
					if (colorCheck.isSelected() && col > 1) {
						float score = aggregateModel.getScoreAt(row, col);
						if (score != 0) {
							// ch2514 -- change to use ColorContext??
							// Color cellColor =
							// colorContext.getMarkerValueColor(((DSMicroarray)
							// mindyData.getArraySet().get(i)).getMarkerValue(modulator),
							// modulator, 1.0f);
							component.setBackground(gradient.getColor(score));
							// component.setBackground(cellColor);
							// display red/blue only
							// component.setBackground(gradient.getColor(Math.signum(score)
							// * 1));
						}
					}
					return component;
				}
			};

			/* do not allow the user to reorder table columns */
			targetTable.getTableHeader().setReorderingAllowed(false);

			targetTable.getTableHeader().setDefaultRenderer(
					new CheckBoxRenderer());
			targetTable.getTableHeader().addMouseListener(
					new ColumnHeaderListener());
			targetTable.setAutoCreateColumnsFromModel(true);
			scrollPane = new JScrollPane(targetTable);
			scrollPane
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			restoreBooleanRenderers(targetTable);

			colorCheck.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					targetTable.invalidate();
					aggregateModel.fireTableDataChanged();
				}
			});

			scoreCheck.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					aggregateModel.setScoreView(scoreCheck.isSelected());
					targetTable.invalidate();
					aggregateModel.fireTableDataChanged();
				}
			});

			modulatorLimits.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					Integer modLimit = (Integer) modLimitValue.getValue();
					log.debug("Limiting modulators displayed to top "
							+ modLimit);
					boolean selected = modulatorLimits.isSelected();
					limitModulators(modLimit, selected, targetTable);
				}
			});

			modLimitValue.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent changeEvent) {
					if (modulatorLimits.isSelected()) {
						limitModulators((Integer) modLimitValue.getValue(),
								true, targetTable);
					}
				}
			});

			selectAllModsCheckBoxTarget = new JCheckBox("Select All Modulators");
			selectAllModsCheckBoxTarget.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					AggregateTableModel model = (AggregateTableModel) targetTable
							.getModel();
					model.selectAllModulators(selectAllModsCheckBoxTarget
							.isSelected());
					selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION
							+ " " + model.getNumberOfMarkersSelected());
				}
			});

			selectAllTargetsCheckBoxTarget = new JCheckBox("Select All Targets");
			selectAllTargetsCheckBoxTarget
					.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionEvent) {
							AggregateTableModel model = (AggregateTableModel) targetTable
									.getModel();
							model
									.selectAllTargets(selectAllTargetsCheckBoxTarget
											.isSelected());
							selectionEnabledCheckBoxTarget
									.setText(ENABLE_SELECTION
											+ " "
											+ model
													.getNumberOfMarkersSelected());
						}
					});

			addToSetButtonTarget = new JButton("Add To Set");
			addToSetButtonTarget.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					AggregateTableModel atm = (AggregateTableModel) targetTable
							.getModel();
					addToSet(atm.getUniqueCheckedTargetsAndModulators(),
							visualPlugin);
				}
			});

			selectionEnabledCheckBoxTarget = new JCheckBox(ENABLE_SELECTION
					+ " 0");
			selectionEnabledCheckBoxTarget
					.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionEvent) {
							boolean selected = selectionEnabledCheckBoxTarget
									.isSelected();
							log.debug("Setting test box visibility to "
									+ selected);
							aggregateModel.fireTableStructureChanged();
							setTargetControlVisibility(selected);
							setTargetTableViewOptions();
							setTargetCheckboxesVisibility(selected);
							if (!selected) {
								selectAllModsCheckBoxTarget.setSelected(false);
								selectAllTargetsCheckBoxTarget
										.setSelected(false);
								aggregateModel.fireTableStructureChanged();
								aggregateModel.selectAllModulators(selected);
								aggregateModel.selectAllTargets(selected);
								setTargetCheckboxesVisibility(selected);
								selectionEnabledCheckBoxTarget
										.setText(ENABLE_SELECTION
												+ " "
												+ aggregateModel
														.getNumberOfMarkersSelected());
							}
						}
					});

			targetAllMarkersCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					selectAllTargetsCheckBoxTarget.setSelected(false);
					selectionEnabledCheckBoxTarget
							.setText(ENABLE_SELECTION
									+ " "
									+ aggregateModel
											.getNumberOfMarkersSelected());
					if (targetAllMarkersCheckBox.isSelected()
							|| (aggregateModel.limitedTargets == null)
							|| (aggregateModel.limitedTargets.size() <= 0)) {
						aggregateModel.showAllMarkers();
					} else {
						aggregateModel.showLimitedMarkers();
					}
				}
			});

			targetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			selectionEnabledCheckBoxTarget.setSelected(true);
			selectAllModsCheckBoxTarget.setEnabled(true);
			selectAllTargetsCheckBoxTarget.setEnabled(true);
			addToSetButtonTarget.setEnabled(true);

			JPanel taskContainer = new JPanel();
			taskContainer.setLayout(new GridLayout(18, 1, 10, 10));
			taskContainer.add(dl);
			taskContainer.add(showSymbol);
			taskContainer.add(showProbeName);
			taskContainer.add(ls);
			taskContainer.add(sortOptionsAggregate);
			taskContainer.add(sortOptionsEnhancing);
			taskContainer.add(sortOptionsNegative);
			taskContainer.add(lm);
			taskContainer.add(limitControls);
			taskContainer.add(ld);
			taskContainer.add(colorCheck);
			taskContainer.add(scoreCheck);
			taskContainer.add(lmp);
			taskContainer.add(selectionEnabledCheckBoxTarget);
			taskContainer.add(selectAllModsCheckBoxTarget);
			taskContainer.add(selectAllTargetsCheckBoxTarget);
			taskContainer.add(addToSetButtonTarget);
			taskContainer.add(targetAllMarkersCheckBox);
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
			listTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listTable.setAutoCreateColumnsFromModel(false);
			listTable.getTableHeader().addMouseListener(
					new ColumnHeaderListener());

			restoreBooleanRenderers(listTable);

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

	/* Table tab   */
	private void columnScrolling() {
//		int w = MIN_CHECKBOX_WIDTH + MIN_MARKER_NAME_WIDTH + boxes.length
		int colCount = targetTable.getColumnCount();
		int w = MIN_CHECKBOX_WIDTH + MIN_MARKER_NAME_WIDTH + colCount
				* MIN_SCORE_WIDTH;
		if (w > scrollPane.getWidth()) {
			targetTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		} else {
			targetTable
					.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		}
	}

	private void limitModulators(Integer modLimit, boolean selected,
			JTable table) {
		aggregateModel.setModLimit(modLimit);
		aggregateModel.setModulatorsLimited(selected);
		MindyPlugin.this
				.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
						.isSelected());

		aggregateModel.fireTableStructureChanged();
		this.columnScrolling();
	}

	private void clearAllTargetTableModulatorSelections() {
		selectAllModsCheckBoxTarget.setSelected(false);
		selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " "
				+ aggregateModel.getNumberOfMarkersSelected());
	}

	private void restoreBooleanRenderers(JTable table) {
		table.setDefaultEditor(Boolean.class, new DefaultCellEditor(
				new JCheckBox()));
		table.setDefaultRenderer(Boolean.class, new CheckboxCellRenderer());
	}

	private void setListTableViewOptions() {
		boolean selected = selectionEnabledCheckBox.isSelected();
		if (selected) {
			listTable.getColumnModel().getColumn(0).setMaxWidth(15);
			listTable.getColumnModel().getColumn(2).setMaxWidth(15);
		}
		setListCheckboxesVisibility(selected);
	}

	private void setTargetTableViewOptions() {
		boolean selected = selectionEnabledCheckBoxTarget.isSelected();
		if (selected) {
			targetTable.getColumnModel().getColumn(0).setMaxWidth(15);
		} else {
			targetTable.getColumnModel().getColumn(0).setMaxWidth(0);
		}
		setTargetCheckboxesVisibility(selected);
	}

	private void setListCheckboxesVisibility(boolean show) {
		if (show) {
			listTable.getColumn(" ").setMaxWidth(15);
			listTable.getColumn("  ").setMaxWidth(15);
			listTable.getColumn(" ").setMinWidth(15);
			listTable.getColumn("  ").setMinWidth(15);
			listTable.getColumn(" ").setWidth(15);
			listTable.getColumn("  ").setWidth(15);
		} else {
			listTable.getColumn(" ").setMaxWidth(0);
			listTable.getColumn("  ").setMaxWidth(0);
			listTable.getColumn(" ").setMinWidth(0);
			listTable.getColumn("  ").setMinWidth(0);
			listTable.getColumn(" ").setWidth(0);
			listTable.getColumn("  ").setWidth(0);
		}
	}

	private void setTargetCheckboxesVisibility(boolean show) {
		if (show) {
			targetTable.getColumn(" ").setMaxWidth(15);
			targetTable.getColumn(" ").setMinWidth(15);
			targetTable.getColumn(" ").setWidth(15);
		} else {
			targetTable.getColumn(" ").setMaxWidth(0);
			targetTable.getColumn(" ").setMinWidth(0);
			targetTable.getColumn(" ").setWidth(0);
		}
	}

	private void setListControlVisibility(boolean show) {
		selectAllModsCheckBox.setEnabled(show);
		selectAllTargetsCheckBox.setEnabled(show);
		addToSetButton.setEnabled(show);
	}

	private void setTargetControlVisibility(boolean show) {
		selectAllModsCheckBoxTarget.setEnabled(show);
		selectAllTargetsCheckBoxTarget.setEnabled(show);
		addToSetButtonTarget.setEnabled(show);
	}

	private void setHeatMap(ModulatorHeatMap heatMap) {
		heatMapScrollPane.getViewport().setView(heatMap);
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
			aggregateModel.limitMarkers(markers);

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
		aggregateModel.rememberSelections();

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

	private JCheckBox[] getHeaderCheckBoxes() {
		return this.boxes;
	}

	@SuppressWarnings("unchecked")
	private void addToSet(List<DSGeneMarker> selections,
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

					for (Iterator iterator = selectedSet.iterator(); iterator
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

	/**
	 * For rendering modulator checkboxes on the targets table column headers.
	 *
	 * @author ch2514
	 * @version $Id: MindyPlugin.java,v 1.85 2009-05-14 17:52:17 oshteynb Exp $
	 */
	private class CheckBoxRenderer extends DefaultTableCellRenderer {
		/**
		 * Specifies how to render targets table column headers.
		 *
		 * @param table -
		 *            targets table
		 * @param value -
		 *            the value of the cell to be rendered
		 * @param isSelected -
		 *            true if the cell is to be rendered with the selection
		 *            highlighted; otherwise false
		 * @param hasFocus -
		 *            true if the header cell has focus, and false otherwise
		 * @param row -
		 *            the row index of the cell being drawn. When drawing the
		 *            header, the value of row is -1
		 * @param column -
		 *            the column index of the cell being drawn
		 * @return
		 */
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
			Object o = table.getModel();
			if (o instanceof AggregateTableModel) {
				AggregateTableModel atm = (AggregateTableModel) o;
				Border loweredetched = BorderFactory
						.createEtchedBorder(EtchedBorder.LOWERED);
				boxes = new JCheckBox[table.getColumnCount()];
				if (column == 0) {
					JPanel blank = new JPanel();
					JLabel blankLabel = new JLabel("  ");
					blankLabel.setBackground(c.getBackground());
					blank.setBorder(loweredetched);
					blank.add(blankLabel);
					blank.setMaximumSize(new Dimension((int) blank.getSize()
							.getWidth(), 10));
					table.getColumnModel().getColumn(column).setMinWidth(
							MIN_CHECKBOX_WIDTH);
					return blank;
				} else if (column == 1) {
					JLabel jl = new JLabel(atm.getColumnName(column),
							SwingConstants.LEFT);
					jl.setBackground(c.getBackground());
					JPanel blank = new JPanel();
					blank.setBorder(loweredetched);
					blank.add(jl);
					blank.setMaximumSize(new Dimension((int) blank.getSize()
							.getWidth(), 10));
					table.getColumnModel().getColumn(column).setMinWidth(
							MIN_MARKER_NAME_WIDTH);
					return blank;
				} else if (column < boxes.length) {
					int w = MIN_CHECKBOX_WIDTH + MIN_MARKER_NAME_WIDTH
							+ boxes.length * MIN_SCORE_WIDTH;
					// TODO resizing
/*					if (w > scrollPane.getWidth()) {
						table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					} else {
						table
								.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
					}
*/
					boxes[column] = new JCheckBox();
					boxes[column].setEnabled(true);
					if (column < atm.getNumberOfModulatorCheckBoxes())
						boxes[column].setSelected(atm
								.getModulatorCheckBoxState(column));
					else
						log
								.error("column ["
										+ column
										+ "] does not have a corresponding checkbox state.");
					JLabel jl = new JLabel("  " + atm.getColumnName(column));
					jl.setBackground(c.getBackground());
					JPanel p = new JPanel(new BorderLayout());
					p.setBorder(loweredetched);
					if (selectionEnabledCheckBoxTarget.isSelected())
						p.add(boxes[column], BorderLayout.WEST);
					p.add(jl, BorderLayout.CENTER);
					p.setSize((int) p.getSize().getWidth(), 50);
					return p;
				}
			}
			return c;
		}
	}

	/**
	 * Table data model for the targets table.
	 *
	 * @author mhall
	 * @author ch2514
	 * @version $Id: MindyPlugin.java,v 1.85 2009-05-14 17:52:17 oshteynb Exp $
	 */
	class AggregateTableModel extends DefaultTableModel {

		private static final int EXTRA_COLS = 2;

		private boolean[] checkedModulators;

//		private List<DSGeneMarker> allModulators;
		private List<DSGeneMarker> selectedModulators;

		private List<DSGeneMarker> enabledModulators;

		private boolean[] checkedTargets;

		private List<DSGeneMarker> activeTargets;

		private List<DSGeneMarker> limitedTargets;

		private List<DSGeneMarker> selectedTargets;


		private MindyData mindyData;

		private boolean scoreView = false;

		private boolean modulatorsLimited = false;

		private int modLimit = DEFAULT_MODULATOR_LIMIT;

		private ModulatorSort modulatorSortMethod = ModulatorSort.Aggregate;

		private boolean[] ascendSortStates;

		private boolean showProbeName = false;

		/**
		 * Constructor.
		 *
		 * @param mindyData
		 */
		public AggregateTableModel(MindyData mindyData) {
			this.showProbeName = !mindyData.isAnnotated();
			this.checkedTargets = new boolean[mindyData.getData().size()];
			this.mindyData = mindyData;
//			allModulators = mindyData.getModulators();
			int allModulatorsSize = mindyData.getModulators().size();
			enabledModulators = new ArrayList<DSGeneMarker>();
			activeTargets = new ArrayList<DSGeneMarker>();
//			ascendSortStates = new boolean[allModulators.size()
			ascendSortStates = new boolean[allModulatorsSize
					+ AggregateTableModel.EXTRA_COLS];
//			this.checkedModulators = new boolean[this.allModulators.size()
			this.checkedModulators = new boolean[allModulatorsSize
					+ AggregateTableModel.EXTRA_COLS];
			this.selectedModulators = new ArrayList<DSGeneMarker>();
			this.selectedTargets = new ArrayList<DSGeneMarker>();
		}

		/**
		 * Whether the targets table shows the actual scores or just -1, 0, and
		 * 1.
		 *
		 * @return true if the table is to show the actual scores, and false
		 *         otherwise
		 */
		public boolean isScoreView() {
			return scoreView;
		}

		/**
		 * Set shether the targets table shows the actual scores or just -1, 0,
		 * and 1.
		 *
		 * @param scoreView -
		 *            true if the table is to show the actual scores, and false
		 *            otherwise
		 */
		public void setScoreView(boolean scoreView) {
			this.scoreView = scoreView;
		}

		/**
		 * Get the modulator sort methods for targets table.
		 *
		 * @return the ModulatorSort object representing the sorting scheme for
		 *         the table columns
		 */
		public ModulatorSort getModulatorSortMethod() {
			return modulatorSortMethod;
		}

		/**
		 * Set the sort scheme for modulators in the targets table.
		 *
		 * @param modulatorSortMethod -
		 *            ModulatorSort object that specifies how to sort table
		 *            columns
		 */
		public void setModulatorSortMethod(ModulatorSort modulatorSortMethod) {
			this.modulatorSortMethod = modulatorSortMethod;
			resortModulators();
			fireTableStructureChanged();
		}

		/**
		 * Whether or not to display only the top modulator(s) in the targets
		 * table.
		 *
		 * @return true if to limit display to the specified number of top
		 *         modulator(s), and false otherwise.
		 */
		public boolean isModulatorsLimited() {
			return modulatorsLimited;
		}

		/**
		 * Set whether or not to display only the top modulator(s) in the
		 * targets table.
		 *
		 * @param modulatorsLimited -
		 *            true if to limit display to the specified number of top
		 *            modulator(s), and false otherwise.
		 */
		public void setModulatorsLimited(boolean modulatorsLimited) {
			this.modulatorsLimited = modulatorsLimited;
		}

		/**
		 * Get the number of top modulator(s) to display in the targets table.
		 *
		 * @return number of top modulator(s) to display
		 */
		public int getModLimit() {
			return modLimit;
		}

		/**
		 * Set the number of top modulator(s) to display in the targets table.
		 *
		 * @param modLimit -
		 *            number of top modulator(s) to display
		 */
		public void setModLimit(int modLimit) {
			this.modLimit = modLimit;
		}

		/**
		 * Get the list of enabled modulators.
		 *
		 * @return list of enabled modulators
		 */
		public List<DSGeneMarker> getEnabledModulators() {
			return enabledModulators;
		}

		/**
		 * Set the list of enabled modulators.
		 * called by selectAllModulators
		 *
		 * @param enabledModulators -
		 *            list of enabled modulators
		 */
		public void setEnabledModulators(List<DSGeneMarker> enabledModulators) {
			this.enabledModulators = enabledModulators;
			this.checkedModulators = new boolean[this.enabledModulators.size()
					+ AggregateTableModel.EXTRA_COLS];
			this.ascendSortStates = new boolean[this.enabledModulators.size()
					+ AggregateTableModel.EXTRA_COLS];
			recalcActiveTargets();
			resortModulators(); // This also fires structure changed
			MindyPlugin.this
					.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
							.isSelected());

			fireTableStructureChanged();
		}

		/**
		 * Get the list of selected targets.
		 *
		 * @return list of selected targets
		 */
		public List<DSGeneMarker> getCheckedTargets() {
			return this.selectedTargets;
		}

		/**
		 * Get the list of selected modulators.
		 *
		 * @return list of selected modulators
		 */
		public List<DSGeneMarker> getCheckedModulators() {
			return this.selectedModulators;
		}

		/**
		 * Enable the specified modulator.
		 *
		 * @param mod -
		 *            the modulator to enable
		 */
		public void enableModulator(DSGeneMarker mod) {
			if (!enabledModulators.contains(mod)) {
				enabledModulators.add(mod);
				recalcActiveTargets();
				resortModulators(); // This also fires structure changed
				MindyPlugin.this
						.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
								.isSelected());

				fireTableStructureChanged();
			}
		}

		/**
		 * Disable the specified modulator.
		 *
		 * @param mod -
		 *            the modulator to disable
		 */
		public void disableModulator(DSGeneMarker mod) {
			enabledModulators.remove(mod);
			// ch2514 -- re-examine this!
			recalcActiveTargets();
			resortModulators(); // This also fires structure changed
			MindyPlugin.this
					.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
							.isSelected());

			fireTableStructureChanged();
		}

		public void disableAllModulators() {
			enabledModulators.clear();
		}

		private void recalcActiveTargets() {
			// activeTargets.clear();

			if ((this.enabledModulators != null)
					&& (this.enabledModulators.size() > 0)) {
//				DSGeneMarker modMarker = this.enabledModulators.get(0);
				if (targetAllMarkersCheckBox.isSelected()) {
//					this.activeTargets = mindyData.getTargets(modMarker);
					this.activeTargets = mindyData.getTargets(this.enabledModulators);
				} else {
					if ((this.limitedTargets != null)
							&& (this.limitedTargets.size() > 0)) {
						this.activeTargets = (List<DSGeneMarker>) ((ArrayList<DSGeneMarker>) this.limitedTargets)
								.clone();
					} else {
						// this.activeTargets = mindyData.getAllTargets(); //
						// MindyData.getAllTargets() broken??
//						this.activeTargets = mindyData.getTargets(modMarker);
						this.activeTargets = mindyData.getTargets(this.enabledModulators);
					}
				}

				// yank out the rows with all zero scores in all columns
				for (int i = 0; i < activeTargets.size(); i++) {
					float tally = 0;
					for (int j = 0; j < enabledModulators.size(); j++) {
						tally += mindyData.getScore(enabledModulators.get(j),
								activeTargets.get(i));
					}
					if (tally == 0) {
						activeTargets.remove(i);
						i--;
					}
				}

				checkedTargets = new boolean[activeTargets.size()];
				for (int i = 0; i < checkedTargets.length; i++) {
					if (this.selectedTargets
							.contains(this.activeTargets.get(i))) {
						checkedTargets[i] = true;
					} else {
						checkedTargets[i] = false;
					}
				}

//				fireTableDataChanged();

				MindyPlugin.this
						.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
								.isSelected());
			}
		}

		void rememberSelections() {
			checkedModulators = new boolean[this.enabledModulators.size()];
			checkedTargets = new boolean[this.activeTargets.size()];
			for (int i = 0; i < this.enabledModulators.size(); i++) {
				if (this.selectedModulators.contains(enabledModulators.get(i)))
					checkedModulators[i] = true;
				else
					checkedModulators[i] = false;
				if (this.selectedTargets.contains(this.activeTargets.get(i)))
					checkedTargets[i] = true;
				else
					checkedTargets[i] = false;
			}
		}

		/**
		 * Get the number of columns in the targets table.
		 *
		 * @return the number of columns in the targets table
		 */
		public int getColumnCount() {
			// Number of allModulators plus target name and checkbox column
			if (!modulatorsLimited) {
				int r = enabledModulators.size()
						+ AggregateTableModel.EXTRA_COLS;
				return r;
			} else {
				int r = Math.min(modLimit + AggregateTableModel.EXTRA_COLS,
						enabledModulators.size()
								+ AggregateTableModel.EXTRA_COLS);
				return r;
			}
		}

		// called from MindyVisualComponent
		// i.e. when SelectionPanel changes marker set selections via
		// GeneSelectorEvent
		/**
		 * Callback method for the targets table when the user changes marker
		 * set selections in the Selection Panel.
		 *
		 * @param -
		 *            list of selected markers
		 */
		public void limitMarkers(List<DSGeneMarker> limitList) {
			if (limitList == null) {
				limitedTargets = null;
				log.debug("Cleared modulator and target limits.");
			} else {
				limitedTargets = limitList;
				log.debug("Limited list table to " + limitedTargets.size()
						+ " targets.");
			}

/*			if (!targetAllMarkersCheckBox.isSelected()) {
				redrawTable();
			}
*/
			if (limitList == null) {
				this.checkSelectedMarkers(true);
			} else {
				this.checkSelectedMarkers(false);
			}

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
		 * Panel. Applies only to the targets table.
		 */
		public void showLimitedMarkers() {
			redrawTable();
			this.checkSelectedMarkers(false);
		}

		// called from "All Markers" checkbox
		/**
		 * Show all markers. Applies only to the targets table.
		 */
		public void showAllMarkers() {
			redrawTable();
			this.checkSelectedMarkers(true);
		}

		private void checkSelectedMarkers(boolean showAll) {
			if (showAll) {
				if ((this.selectedModulators != null)
						&& (this.selectedModulators.size() > 0)
						&& (this.enabledModulators.size() == this.selectedModulators
								.size())) {
					selectAllModsCheckBoxTarget.setSelected(true);
				} else {
					selectAllModsCheckBoxTarget.setSelected(false);
				}
				if ((this.selectedTargets != null)
						&& (this.selectedTargets.size() > 0)
						&& (this.activeTargets.size() == this.selectedTargets
								.size())) {
					selectAllTargetsCheckBoxTarget.setSelected(true);
				} else {
					selectAllTargetsCheckBoxTarget.setSelected(false);
				}
			} else {
				if ((this.selectedModulators != null)
						&& (this.enabledModulators != null)
						&& (this.selectedModulators.size() > 0)
						&& (this.enabledModulators.size() > 0)
						&& (this.enabledModulators.size() <= this.selectedModulators
								.size())) {
					// need to match items in lim and selected
					boolean allMods = true;
					for (DSGeneMarker m : this.enabledModulators) {
						if (!this.selectedModulators.contains(m)) {
							allMods = false;
							break;
						}
					}
					selectAllModsCheckBoxTarget.setSelected(allMods);
				} else {
					selectAllModsCheckBoxTarget.setSelected(false);
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
					selectAllTargetsCheckBoxTarget.setSelected(allTargets);
				} else {
					selectAllTargetsCheckBoxTarget.setSelected(false);
				}
			}
		}

		public void redrawTable() {
			recalcActiveTargets();
			MindyPlugin.this
					.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
							.isSelected());

			fireTableStructureChanged();
			fireTableDataChanged();
			columnScrolling();
		}

		/**
		 * Get the number of rows on the targets table.
		 *
		 * @return number of rows on the table
		 */
		public int getRowCount() {
			if (activeTargets == null) {
				return 0;
			}
			return activeTargets.size();
		}

		/**
		 * Get the class object representing the specified table column.
		 *
		 * @param columnIndex -
		 *            column index
		 * @return the class object representing the table column
		 */
		public Class<?> getColumnClass(int i) {
			if (i == 0) {
				return Boolean.class;
			} else if (i == 1) {
				return String.class;
			} else {
				return Float.class;
			}
		}

		/**
		 * Get the values of targets table cells.
		 *
		 * @param rowIndex -
		 *            row index of the cell
		 * @param columnIndex -
		 *            column index of the cell
		 * @return the value object of specified table cell
		 */
		public Object getValueAt(int row, int col) {
			if (col == 1) {
				return getMarkerDisplayName(this.isShowProbeName(), (DSGeneMarker) activeTargets
						.get(row));
			} else if (col == 0) {
				return checkedTargets[row];
			} else {
				float score = mindyData.getScore(enabledModulators.get(col
						- AggregateTableModel.EXTRA_COLS), activeTargets.get(row));
				if (score != 0) {
					if (scoreView) {
						return score;
					} else {
						return Math.signum(score) * 1;
					}
				} else {
					return score;
				}
			}
		}

		/**
		 * Get the score of a specified targets table cell.
		 *
		 * @param row -
		 *            row index of the table cell
		 * @param col -
		 *            col index of the table cell
		 * @return the score of the modulator and target
		 */
		public float getScoreAt(int row, int col) {
			float score = mindyData.getScore(enabledModulators.get(col
					- AggregateTableModel.EXTRA_COLS), activeTargets.get(row));
			return score;
		}

		/**
		 * Set values of targets table cells.
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
				boolean select = (Boolean) aValue;
				checkedTargets[rowIndex] = select;
				DSGeneMarker m = this.activeTargets.get(rowIndex);
				if (select) {
					if (!this.selectedTargets.contains(m)) {
						this.selectedTargets.add(m);
					}
				} else {
					this.selectedTargets.remove(m);
				}
				int modColToSelect = this.enabledModulators.indexOf(m);
				if (modColToSelect >= 0)
					this.setModulatorCheckBoxState(modColToSelect
							+ AggregateTableModel.EXTRA_COLS, select);
			}

			selectionEnabledCheckBoxTarget.setText(MindyPlugin.ENABLE_SELECTION
					+ " " + this.getNumberOfMarkersSelected());

			if (this.getCheckedTargets().size() == this.getActiveTargets()
					.size())
				selectAllTargetsCheckBoxTarget.setSelected(true);
			else
				selectAllTargetsCheckBoxTarget.setSelected(false);

			MindyPlugin.this
					.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
							.isSelected());
		}

		/**
		 * Get the specified table column name.
		 *
		 * @param col -
		 *            column index
		 */
		public String getColumnName(int col) {
			if (col == 0) {
				return " ";
			} else if (col == 1) {
				return "Target";
			} else {
				DSGeneMarker mod = enabledModulators.get(col
						- AggregateTableModel.EXTRA_COLS);
				String colName = getMarkerDisplayName(this.isShowProbeName(), mod);
				if (modulatorSortMethod == ModulatorSort.Aggregate) {
					colName += " (M# "
						+ mindyData.getFilteredStatistics(mod).getCount() + ")";
				} else if (modulatorSortMethod == ModulatorSort.Enhancing) {
					colName += " (M+ "
						+ mindyData.getFilteredStatistics(mod).getMover() + ")";
				} else if (modulatorSortMethod == ModulatorSort.Negative) {
					colName += " (M- "
							+ mindyData.getFilteredStatistics(mod).getMunder() + ")";
				}
				return colName;
			}
		}

		/**
		 * Sorts the columns on the targets table based on modulator stat (M#,
		 * M+. M-) selection.
		 */
		public void resortModulators() {
			Collections.sort(enabledModulators, new ModulatorStatComparator(
					mindyData, modulatorSortMethod));
			for (int i = 0; i < this.enabledModulators.size(); i++) {
				if (this.selectedModulators.contains(this.enabledModulators
						.get(i))) {
					this.checkedModulators[i + AggregateTableModel.EXTRA_COLS] = true;
				} else {
					this.checkedModulators[i + AggregateTableModel.EXTRA_COLS] = false;
				}
			}


			//			fireTableStructureChanged();

			MindyPlugin.this
					.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
							.isSelected());
			if ((this.selectedModulators != null)
					&& (this.selectedModulators.size() > 0)
					&& (this.enabledModulators.size() == this.selectedModulators
							.size())) {
				selectAllModsCheckBoxTarget.setSelected(true);
			} else {
				selectAllModsCheckBoxTarget.setSelected(false);
			}
		}

		/**
		 * Clear all modulator selection from the targets table.
		 */
/*		public void clearModulatorSelections() {
			int length = this.checkedModulators.length;
			this.checkedModulators = new boolean[length];
			// for (int i = 0; i < this.checkedModulators.length; i++)
			// this.checkedModulators[i] = false;
			this.selectedModulators.clear();

			fireTableStructureChanged();

			MindyPlugin.this
					.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
							.isSelected());
		}
*/
		/**
		 * Handles table column sorting for the targets table.
		 *
		 * @param col -
		 *            the column index of the column to sort
		 * @param ascending -
		 *            if true, sort the column in ascending order. Otherwise,
		 *            sort in descending order.
		 */
		public void sort(int col, boolean ascending) {
			log.debug("\t\ttable model::sort::start::"
					+ System.currentTimeMillis());
			if (col == 0)
				return;
			if (col == 1) {
				setCursor(hourglassCursor);
				ArrayList<MindyGeneMarker> mindyTargets = mindyData
						.convertToMindyGeneMarker(this.activeTargets);
				Collections.sort(mindyTargets, new MindyMarkerListComparator(
						MindyMarkerListComparator.SHORT_NAME, ascending, showProbeName));
				this.activeTargets = mindyData
						.convertToDSGeneMarker(mindyTargets);
				setCursor(normalCursor);
			} else {
				setCursor(hourglassCursor);
				Collections.sort(this.activeTargets,
						new GeneMarkerListComparator(mindyData,
								enabledModulators.get(col
										- AggregateTableModel.EXTRA_COLS),
								GeneMarkerListComparator.SCORE, ascending));
				setCursor(normalCursor);
			}
			for (int i = 0; i < this.checkedTargets.length; i++) {
				if (this.selectedTargets.contains(this.activeTargets.get(i))) {
					this.checkedTargets[i] = true;
				} else {
					this.checkedTargets[i] = false;
				}
			}
			fireTableStructureChanged();

			selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " "
					+ aggregateModel.getNumberOfMarkersSelected());
			selectAllTargetsCheckBoxTarget.setSelected(false);
			MindyPlugin.this
					.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
							.isSelected());
			log.debug("\t\ttable model::sort::end::"
					+ System.currentTimeMillis());
		}

		/**
		 * The union of selected modulators and targets from the targets table.
		 *
		 * @return the list of selected markers
		 */
		public List<DSGeneMarker> getUniqueCheckedTargetsAndModulators() {
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

		/**
		 * Get the total number of markers (modulators and targets) selected in
		 * the targets table.
		 *
		 * @return the total number of markers selected
		 */
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
		 * Get the list of active targets.
		 *
		 * @return list of active targets
		 */
		public List<DSGeneMarker> getActiveTargets() {
			return activeTargets;
		}

		/**
		 * Get the sorting states (ascending or descending) of each column in
		 * the targets table.
		 *
		 * @return a list of sorting states (ascending = true, descending =
		 *         false)
		 */
		public boolean[] getAscendSortStates() {
			return this.ascendSortStates;
		}

		/**
		 * Set the sorting states (ascending or descending) of each column in
		 * the targets table.
		 *
		 * @param b -
		 *            a list of sorting states (ascending = true, descending =
		 *            false)
		 */
		public void setAscendSortStates(boolean[] b) {
			this.ascendSortStates = b;
		}

		/**
		 * Check to see if the targets table should display probe names or gene
		 * names.
		 *
		 * @return If true, the targets table displays probe names. If not, the
		 *         targets table displays gene names.
		 */
		public boolean isShowProbeName() {
			return this.showProbeName;
		}

		/**
		 * Specify whether or not the targets table should display probe names
		 * or gene names.
		 *
		 * @param showProbeName -
		 *            if true, the targets table displays probe names. If not,
		 *            the targets table displays gene names.
		 */
		public void setShowProbeName(boolean showProbeName) {
			this.showProbeName = showProbeName;
		}

		/**
		 * Whether or not the modulator from the specified colum index is
		 * selected.
		 *
		 * @param index -
		 *            table column index
		 * @return true if the modulator represented by the column is selected,
		 *         and false otherwise
		 */
		public boolean getModulatorCheckBoxState(int index) {
			return this.checkedModulators[index];
		}

		/**
		 * Set the modulator checkbox for the specified targets table column
		 * header.
		 *
		 * @param index -
		 *            column index of the interested header
		 * @param b -
		 *            true if the modulator at the specified index is selected,
		 *            and false otherwise
		 */
		public void setModulatorCheckBoxState(int index, boolean b) {
			this.checkedModulators[index] = b;
			DSGeneMarker m = enabledModulators.get(index
					- AggregateTableModel.EXTRA_COLS);
			if (b) {
				if (!this.selectedModulators.contains(m)) {
					this.selectedModulators.add(m);
				}
			} else {
				this.selectedModulators.remove(m);
			}

		}

		/**
		 * Get the number of modulator checkboxes from the table column headers.
		 *
		 * @return the number of modulator checkboxes from the table column
		 *         headers
		 */
		public int getNumberOfModulatorCheckBoxes() {
			return this.checkedModulators.length;
		}

		private void selectAllTargets(boolean select) {
			for (int i = 0; i < checkedTargets.length; i++) {
				checkedTargets[i] = select;
			}
			this.selectedTargets.clear();
			if (select) {
				this.selectedTargets.addAll(this.activeTargets);
			}
			this.fireTableDataChanged();

			MindyPlugin.this
					.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
							.isSelected());
		}

		private void selectAllModulators(boolean select) {
			int top = this.getColumnCount();
			if ((this.modulatorsLimited)
					&& ((this.modLimit + AggregateTableModel.EXTRA_COLS) < top))
				top = this.modLimit + AggregateTableModel.EXTRA_COLS;
			for (int i = AggregateTableModel.EXTRA_COLS; i < top; i++)
				checkedModulators[i] = select;

			this.selectedModulators.clear();
			if (select) {
				this.selectedModulators.addAll(this.enabledModulators);
			}
			this.fireTableStructureChanged();

			MindyPlugin.this
					.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
							.isSelected());
		}
	}

	/**
	 * Compare M#, M+, or M- of two gene markers (for sorting).
	 *
	 * @author mhall
	 * @version $Id: MindyPlugin.java,v 1.85 2009-05-14 17:52:17 oshteynb Exp $
	 */
	private class ModulatorStatComparator implements Comparator<DSGeneMarker> {

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
	 * @version $Id: MindyPlugin.java,v 1.85 2009-05-14 17:52:17 oshteynb Exp $
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
			this.modChecks = new boolean[mindyData.getData().size()];
			this.targetChecks = new boolean[mindyData.getData().size()];
			enabledModulators = new ArrayList<DSGeneMarker>(mindyData.getData()
					.size());
			enabledTargets = new ArrayList<DSGeneMarker>(mindyData.getData()
					.size());
			limitedModulators = new ArrayList<DSGeneMarker>();
			limitedTargets = new ArrayList<DSGeneMarker>();
			selectedModulators = new ArrayList<DSGeneMarker>();
			selectedTargets = new ArrayList<DSGeneMarker>();

			for (int i = 0; i < mindyData.getData().size(); i++) {
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

	/**
	 * Handles column sorting in MINDY tables. Also handles modulator selection
	 * for the targets table.
	 *
	 * @author ch2514
	 * @version $Id: MindyPlugin.java,v 1.85 2009-05-14 17:52:17 oshteynb Exp $
	 */
	private class ColumnHeaderListener extends MouseAdapter {
		/**
		 * Handles mouse clicks on table column headers.
		 *
		 * @param evt -
		 *            MouseEvent
		 */
		public void mouseClicked(MouseEvent evt) {
			JTable table = ((JTableHeader) evt.getSource()).getTable();
			TableColumnModel colModel = table.getColumnModel();
			TableModel model = table.getModel();

			// The index of the column whose header was clicked
			int vColIndex = colModel.getColumnIndexAtX(evt.getX());
			int mColIndex = table.convertColumnIndexToModel(vColIndex);

			// Return if not clicked on any column header
			if (vColIndex == -1) {
				return;
			}

			// Determine if mouse was clicked between column heads
			Rectangle headerRect = table.getTableHeader().getHeaderRect(
					vColIndex);
			if (vColIndex == 0) {
				headerRect.width -= 3; // Hard-coded constant
			} else {
				headerRect.grow(-3, 0); // Hard-coded constant
			}
			if (!headerRect.contains(evt.getX(), evt.getY())) {
				// Mouse was clicked between column heads
				// vColIndex is the column head closest to the click

				// vLeftColIndex is the column head to the left of the click
				int vLeftColIndex = vColIndex;
				if (evt.getX() < headerRect.x) {
					vLeftColIndex--;
				}
			}

			if (model instanceof ModulatorModel) {
				// sort
				ModulatorModel mm = (ModulatorModel) model;
				boolean[] states = mm.getAscendSortStates();
				if (mColIndex < states.length) {
					boolean tmp = states[mColIndex];
					states[mColIndex] = !tmp;
					mm.sort(mColIndex, states[mColIndex]);
				}

			}
			if (model instanceof AggregateTableModel) {
				AggregateTableModel atm = (AggregateTableModel) model;
				boolean clickedCheckbox = false;
				// checkbox
				if ((selectionEnabledCheckBoxTarget.isSelected())
						&& (mColIndex >= 2)
						&& (evt.getX() >= headerRect.getX())
						&& (evt.getX() <= (headerRect.getX() + 15))) {
					clickedCheckbox = true;
					JCheckBox cb = MindyPlugin.this.getHeaderCheckBoxes()[mColIndex];
					if ((cb != null)
							&& (mColIndex < atm
									.getNumberOfModulatorCheckBoxes())) {
						boolean tmp = atm.getModulatorCheckBoxState(mColIndex);
						atm.setModulatorCheckBoxState(mColIndex, !tmp);
						cb
								.setSelected(atm
										.getModulatorCheckBoxState(mColIndex));
						atm.fireTableStructureChanged();
						DSGeneMarker m = atm.enabledModulators.get(mColIndex
								- AggregateTableModel.EXTRA_COLS);
						int tRowIndex = atm.activeTargets.indexOf(m);
						if (tRowIndex >= 0) {
							atm.setValueAt(!tmp, tRowIndex, 0);
							atm.fireTableDataChanged();
						}

						selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION
								+ " " + atm.getNumberOfMarkersSelected());

						if (atm.getCheckedModulators().size() == atm
								.getEnabledModulators().size())
							selectAllModsCheckBoxTarget.setSelected(true);
						else
							selectAllModsCheckBoxTarget.setSelected(false);
						MindyPlugin.this
								.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
										.isSelected());
					}
					if (mColIndex >= atm.getNumberOfModulatorCheckBoxes())
						log.error("check box index [" + mColIndex
								+ "] not in check box state");
				}

				// sort
				if ((mColIndex == 1)
						|| ((mColIndex >= 2) && (!clickedCheckbox))) {
					boolean[] states = atm.getAscendSortStates();
					if (mColIndex < states.length) {
						boolean tmp = states[mColIndex];
						states[mColIndex] = !tmp;
						atm.sort(mColIndex, states[mColIndex]);
					}
				}
			}
			if (model instanceof ModulatorTargetModel) {
				// sort
				ModulatorTargetModel mtm = (ModulatorTargetModel) model;
				boolean[] states = mtm.getAscendSortStates();
				if (mColIndex < states.length) {
					boolean tmp = states[mColIndex];
					states[mColIndex] = !tmp;
					mtm.sort(mColIndex, states[mColIndex]);
				}
			}
		}
	}
}
