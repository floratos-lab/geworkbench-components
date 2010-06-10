package org.geworkbench.components.mindy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Refactored from MindyPlugin.java
 *
 * @author os2201
 * @version $Id$
 */
public class MindyTableTab extends JSplitPane {
	private static Log log = LogFactory.getLog(MindyTableTab.class);

	private static final long serialVersionUID = -3908347039215503124L;

	private AggregateTableModel aggregateModel;

	private JCheckBox[] headerCheckBoxes;

	private JScrollPane scrollPane;

	private JCheckBox selectAllModsCheckBoxTarget;

	private JCheckBox selectAllTargetsCheckBoxTarget;

	private JCheckBox selectionEnabledCheckBoxTarget;

	private JCheckBox targetAllMarkersCheckBox;

	private JTable targetTable;

	/**
	 * this part of action is taken out of constructor so they are caried out after mindyPlugin is proper constructed.
	 */
	void setMindyPlugin(final MindyPlugin mindyPlugin) {
		if (mindyPlugin.getMindyData().isAnnotated())
			showSymbol.setSelected(true);
		else
			showProbeName.setSelected(true);

		aggregateModel = new AggregateTableModel(mindyPlugin);
		aggregateModel.setModLimit(
				MindyPlugin.DEFAULT_MODULATOR_LIMIT);
		aggregateModel.setModulatorsLimited(
				modulatorLimits.isSelected());
		targetTable = new JTable(aggregateModel);
		targetTable.setDefaultRenderer(Object.class, new ColoredCellRenderer());
		/* do not allow the user to reorder table columns */
		targetTable.getTableHeader().setReorderingAllowed(
				false);
		// 0 hardcoded for consistency, refactor later
		targetTable.getColumnModel().getColumn(0).setMinWidth(
				MindyPlugin.MIN_CHECKBOX_WIDTH);
		targetTable.getColumnModel().getColumn(0).setMaxWidth(
				MindyPlugin.MAX_CHECKBOX_WIDTH);
		targetTable.getTableHeader().setDefaultRenderer(
				new CheckBoxRenderer(mindyPlugin));
		targetTable.getTableHeader().addMouseListener(
				new ColumnHeaderListener(mindyPlugin));
		targetTable.setAutoCreateColumnsFromModel(true);
		scrollPane = new JScrollPane(targetTable);
		scrollPane.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		MindyPlugin.restoreBooleanRenderers(targetTable);		
		
		targetTable.setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		
		addToSetButtonTarget.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						AggregateTableModel atm = (AggregateTableModel) targetTable
								.getModel();
						mindyPlugin.addToSet(atm
								.getUniqueCheckedTargetsAndModulators());
					}
				});

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
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		setLeftComponent(sp);
		setRightComponent(scrollPane);
		setResizeWeight(0.055);
		setOneTouchExpandable(false);
		setContinuousLayout(true);
	}
	
    private static DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
    
    private class ColoredCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -3923528011064944031L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component defaultComponent = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			c.setBackground(defaultComponent.getBackground());
			// note: because c is in fact reused, we need to start with a new background color (the only property we may change)
			if (colorCheck.isSelected() && col > 1) {
				float score = aggregateModel.getScoreAt(
						row, col);
				if (score != 0) {
					c.setBackground(gradient.getColor(score));
				}
			}
			
            return c;
        }
    };
    
	private JLabel dl = new JLabel("Marker Display  ", SwingConstants.LEFT);
	private JLabel ls = new JLabel("Modulator Sorting", SwingConstants.LEFT);
	private JRadioButton showSymbol = new JRadioButton("Symbol");
	private JRadioButton showProbeName = new JRadioButton("Probe Name");
	private final JCheckBox modulatorLimits = new JCheckBox("Limit To Top");
	private final JCheckBox colorCheck = new JCheckBox("Color View");
	private final JCheckBox scoreCheck = new JCheckBox("Score View");
	private final ColorGradient gradient = new ColorGradient(Color.blue, Color.red);
	private JButton addToSetButtonTarget = new JButton("Add To Set");
	private JLabel ld = new JLabel("Display Options", SwingConstants.LEFT);
	private JLabel lm = new JLabel("Modulator Limits", SwingConstants.LEFT);
	private JLabel lmp = new JLabel("Marker Selection  ", SwingConstants.LEFT);
	private JPanel limitControls = new JPanel(new BorderLayout());
	private JRadioButton sortOptionsAggregate = new JRadioButton("Aggregate");
	private JRadioButton sortOptionsEnhancing = new JRadioButton("Enhancing");
	private JRadioButton sortOptionsNegative = new JRadioButton("Negative");

	/**
	 * Constructor.
	 * 
	 * @param mindyPlugin
	 */
	public MindyTableTab() {
		super(JSplitPane.HORIZONTAL_SPLIT);
		
		dl.setFont(new Font(dl.getFont().getName(), Font.BOLD, 12));
		dl.setForeground(Color.BLUE);
		showSymbol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				aggregateModel.setShowProbeName(false);
				aggregateModel.fireTableStructureChanged();
				setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
			}
		});
		showProbeName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				aggregateModel.setShowProbeName(true);
				aggregateModel.fireTableStructureChanged();
				setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
			}
		});
		ButtonGroup displayGroup = new ButtonGroup();
		displayGroup.add(showSymbol);
		displayGroup.add(showProbeName);

		ls.setFont(new Font(ls.getFont().getName(), Font.BOLD, 12));
		ls.setForeground(Color.BLUE);

		ButtonGroup sortGroup = new ButtonGroup();
		sortGroup.add(sortOptionsAggregate);
		sortGroup.add(sortOptionsEnhancing);
		sortGroup.add(sortOptionsNegative);
		ActionListener sortAction = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selected = actionEvent.getActionCommand().toString();
				if (selected.equals("Aggregate")) {
					log.debug("Setting sort to Aggregate");
					aggregateModel.setModulatorSortMethod(
							MindyPlugin.ModulatorSort.Aggregate);
					setFirstColumnWidth(30);
				} else if (selected.equals("Enhancing")) {
					log.debug("Setting sort to Enhancing");
					aggregateModel.setModulatorSortMethod(
							MindyPlugin.ModulatorSort.Enhancing);
					setFirstColumnWidth(30);
				} else {
					log.debug("Setting sort to Negative");
					aggregateModel.setModulatorSortMethod(
							MindyPlugin.ModulatorSort.Negative);
					setFirstColumnWidth(30);
				}
				clearAllTargetTableModulatorSelections();
			}
		};
		sortOptionsAggregate.addActionListener(sortAction);
		sortOptionsEnhancing.addActionListener(sortAction);
		sortOptionsNegative.addActionListener(sortAction);
		sortOptionsAggregate.setSelected(true);

		lm.setFont(new Font(lm.getFont().getName(), Font.BOLD, 12));
		lm.setForeground(Color.BLUE);

		modulatorLimits.setSelected(true);
		limitControls.add(modulatorLimits, BorderLayout.WEST);
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
				MindyPlugin.DEFAULT_MODULATOR_LIMIT, 1, 1000, 1);
		final JSpinner modLimitValue = new JSpinner(spinnerModel);
		limitControls.add(modLimitValue, BorderLayout.CENTER);

		ld.setFont(new Font(ld.getFont().getName(), Font.BOLD, 12));
		ld.setForeground(Color.BLUE);

		lmp.setFont(new Font(lmp.getFont().getName(), Font.BOLD, 12));
		lmp.setForeground(Color.BLUE);
		targetAllMarkersCheckBox = new JCheckBox("All Markers");
		targetAllMarkersCheckBox.setSelected(false);

		gradient.addColorPoint(Color.white, 0f);

		colorCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				targetTable.invalidate();
				aggregateModel.fireTableDataChanged();
			}
		});

		scoreCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				aggregateModel.setScoreView(
						scoreCheck.isSelected());
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
					limitModulators((Integer) modLimitValue
							.getValue(), true, targetTable);
				}
			}
		});

		selectAllModsCheckBoxTarget = new JCheckBox("Select All Modulators");
		selectAllModsCheckBoxTarget.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						AggregateTableModel model = (AggregateTableModel) targetTable
								.getModel();
						model
								.selectAllModulators(selectAllModsCheckBoxTarget
										.isSelected());
						selectionEnabledCheckBoxTarget.setText(
								MindyPlugin.ENABLE_SELECTION + " "
										+ model.getNumberOfMarkersSelected());
					}
				});

		selectAllTargetsCheckBoxTarget = new JCheckBox("Select All Targets");
		selectAllTargetsCheckBoxTarget.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						AggregateTableModel model = (AggregateTableModel) targetTable
								.getModel();
						model.selectAllTargets(selectAllTargetsCheckBoxTarget
								.isSelected());
						selectionEnabledCheckBoxTarget.setText(
								MindyPlugin.ENABLE_SELECTION + " "
										+ model.getNumberOfMarkersSelected());
					}
				});

		selectionEnabledCheckBoxTarget = new JCheckBox(
				MindyPlugin.ENABLE_SELECTION + " 0");
		selectionEnabledCheckBoxTarget.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						boolean selected = selectionEnabledCheckBoxTarget
								.isSelected();
						log.debug("Setting test box visibility to "
								+ selected);
						aggregateModel
								.fireTableStructureChanged();
						setTargetControlVisibility(selected);
						setTargetTableViewOptions();
						setTargetCheckboxesVisibility(selected);
						if (!selected) {
							selectAllModsCheckBoxTarget
									.setSelected(false);
							selectAllTargetsCheckBoxTarget
									.setSelected(false);
							aggregateModel
									.fireTableStructureChanged();
							aggregateModel
									.selectAllModulators(selected);
							aggregateModel.selectAllTargets(
									selected);
							setTargetCheckboxesVisibility(selected);
							selectionEnabledCheckBoxTarget
									.setText(
											MindyPlugin.ENABLE_SELECTION
													+ " "
													+ aggregateModel
															.getNumberOfMarkersSelected());
						}
					}
				});

		targetAllMarkersCheckBox.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						selectAllTargetsCheckBoxTarget
								.setSelected(false);
						selectionEnabledCheckBoxTarget
								.setText(
										MindyPlugin.ENABLE_SELECTION
												+ " "
												+ aggregateModel
														.getNumberOfMarkersSelected());
						if (targetAllMarkersCheckBox
								.isSelected()
								|| (aggregateModel.getLimitedTargets() == null)
								|| (aggregateModel.getLimitedTargets()
										.size() <= 0)) {
							aggregateModel.showAllMarkers();
						} else {
							aggregateModel
									.showLimitedMarkers();
						}
						setFirstColumnWidth(30);
					}
				});

		selectionEnabledCheckBoxTarget.setSelected(true);
		selectAllModsCheckBoxTarget.setEnabled(true);
		selectAllTargetsCheckBoxTarget.setEnabled(true);
		addToSetButtonTarget.setEnabled(true);
	}

	public AggregateTableModel getAggregateModel() {
		return aggregateModel;
	}

	private void clearAllTargetTableModulatorSelections() {
		selectAllModsCheckBoxTarget.setSelected(false);
		selectionEnabledCheckBoxTarget.setText(MindyPlugin.ENABLE_SELECTION + " "
				+ aggregateModel.getNumberOfMarkersSelected());
	}

	private void limitModulators(Integer modLimit, boolean selected, JTable table) {
		aggregateModel.setModLimit(modLimit);
		aggregateModel.setModulatorsLimited(selected);
		aggregateModel.fireTableStructureChanged();

		setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
				.isSelected());

		columnScrolling();
	}

	private void setTargetControlVisibility(boolean show) {
		selectAllModsCheckBoxTarget.setEnabled(show);
		selectAllTargetsCheckBoxTarget.setEnabled(show);
		addToSetButtonTarget.setEnabled(show);
	}

	private void setTargetTableViewOptions() {
		boolean selected = selectionEnabledCheckBoxTarget.isSelected();
		if (selected) {
			targetTable.getColumnModel().getColumn(0).setMaxWidth(30);
		} else {
			targetTable.getColumnModel().getColumn(0).setMaxWidth(0);
		}
		setTargetCheckboxesVisibility(selected);
	}

	void columnScrolling() {
		int colCount = targetTable.getColumnCount();
		int w = MindyPlugin.MIN_CHECKBOX_WIDTH + MindyPlugin.MIN_MARKER_NAME_WIDTH + colCount
				* MindyPlugin.MIN_SCORE_WIDTH;
		if (w > scrollPane.getWidth()) {
			targetTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		} else {
			targetTable
					.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		}
	}

	JCheckBox[] getHeaderCheckBoxes() {
		return headerCheckBoxes;
	}

	JCheckBox getSelectAllModsCheckBoxTarget() {
		return selectAllModsCheckBoxTarget;
	}

	// TODO to be refactored: only used by AggregateTableModel
	JCheckBox getSelectAllTargetsCheckBoxTarget() {
		return selectAllTargetsCheckBoxTarget;
	}

	JCheckBox getSelectionEnabledCheckBoxTarget() {
		return selectionEnabledCheckBoxTarget;
	}

	// only used by AggregateTableModel
	JCheckBox getTargetAllMarkersCheckBox() {
		return targetAllMarkersCheckBox;
	}

	// only used by CheckBoxRenderer 
	void setHeaderCheckBoxes(JCheckBox[] headerCheckBoxes) {
		this.headerCheckBoxes = headerCheckBoxes;
	}

	void setFirstColumnWidth(int w) {
		targetTable.getColumn(" ").setMaxWidth(w);
		targetTable.getColumn(" ").setMinWidth(w);	 
	}
	
	void setTargetCheckboxesVisibility(boolean show) {
		if (show) {
			setFirstColumnWidth(30);
		} else {
			setFirstColumnWidth(0);
		}
	}

}
