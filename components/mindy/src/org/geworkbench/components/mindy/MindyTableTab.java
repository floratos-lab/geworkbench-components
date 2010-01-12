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
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Refactored from MindyPlugin.java
 *
 * @author os2201
 * @version $Id: $
 */
public class MindyTableTab {

	private static Log log = LogFactory.getLog(MindyTableTab.class);

	private JSplitPane tableTab;

	private JButton addToSetButtonTarget;

	void setAddToSetButtonTarget(JButton addToSetButtonTarget) {
		this.addToSetButtonTarget = addToSetButtonTarget;
	}

	JButton getAddToSetButtonTarget() {
		return addToSetButtonTarget;
	}

	private JCheckBox selectAllTargetsCheckBoxTarget;

	void setSelectAllTargetsCheckBoxTarget(
			JCheckBox selectAllTargetsCheckBoxTarget) {
		this.selectAllTargetsCheckBoxTarget = selectAllTargetsCheckBoxTarget;
	}

	JCheckBox getSelectAllTargetsCheckBoxTarget() {
		return selectAllTargetsCheckBoxTarget;
	}

	private JCheckBox[] headerCheckBoxes;

	public void setHeaderCheckBoxes(JCheckBox[] headerCheckBoxes) {
		this.headerCheckBoxes = headerCheckBoxes;
	}

	public JCheckBox[] getHeaderCheckBoxes() {
		return headerCheckBoxes;
	}

	private JScrollPane scrollPane;

	private JCheckBox targetAllMarkersCheckBox;

	public void setTargetAllMarkersCheckBox(JCheckBox targetAllMarkersCheckBox) {
		this.targetAllMarkersCheckBox = targetAllMarkersCheckBox;
	}

	public JCheckBox getTargetAllMarkersCheckBox() {
		return targetAllMarkersCheckBox;
	}

	private JCheckBox selectAllModsCheckBoxTarget;

	public JCheckBox getSelectAllModsCheckBoxTarget() {
		return selectAllModsCheckBoxTarget;
	}

	public void setSelectAllModsCheckBoxTarget(JCheckBox selectAllModsCheckBoxTarget) {
		this.selectAllModsCheckBoxTarget = selectAllModsCheckBoxTarget;
	}

	private JCheckBox selectionEnabledCheckBoxTarget;

	void setSelectionEnabledCheckBoxTarget(
			JCheckBox selectionEnabledCheckBoxTarget) {
		this.selectionEnabledCheckBoxTarget = selectionEnabledCheckBoxTarget;
	}

	public JCheckBox getSelectionEnabledCheckBoxTarget() {
		return selectionEnabledCheckBoxTarget;
	}

	public void setScrollPane(JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	private JTable targetTable;

	public JTable getTargetTable() {
		return targetTable;
	}

	public void setTargetTable(JTable targetTable) {
		this.targetTable = targetTable;
	}

	private AggregateTableModel aggregateModel;

	public void setAggregateModel(AggregateTableModel aggregateModel) {
		this.aggregateModel = aggregateModel;
	}

	public AggregateTableModel getAggregateModel() {
		return aggregateModel;
	}

	public MindyTableTab(final MindyVisualComponent visualPlugin,
			final MindyPlugin mindyPlugin) {
		JLabel dl = new JLabel("Marker Display  ", SwingConstants.LEFT);
		dl.setFont(new Font(dl.getFont().getName(), Font.BOLD, 12));
		dl.setForeground(Color.BLUE);
		JRadioButton showSymbol = new JRadioButton("Symbol");
		showSymbol.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				getAggregateModel().setShowProbeName(false);
				getAggregateModel().fireTableStructureChanged();
				setTargetCheckboxesVisibility(getSelectionEnabledCheckBoxTarget().isSelected());
			}
		});
		JRadioButton showProbeName = new JRadioButton("Probe Name");
		showProbeName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				getAggregateModel().setShowProbeName(true);
				getAggregateModel().fireTableStructureChanged();
				setTargetCheckboxesVisibility(getSelectionEnabledCheckBoxTarget().isSelected());
			}
		});
		ButtonGroup displayGroup = new ButtonGroup();
		displayGroup.add(showSymbol);
		displayGroup.add(showProbeName);
		if (mindyPlugin.getMindyData().isAnnotated())
			showSymbol.setSelected(true);
		else
			showProbeName.setSelected(true);

		// JLabel ls = new JLabel("Sorting", SwingConstants.LEFT);
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
					getAggregateModel().setModulatorSortMethod(
							MindyPlugin.ModulatorSort.Aggregate);
				} else if (selected.equals("Enhancing")) {
					log.debug("Setting sort to Enhancing");
					getAggregateModel().setModulatorSortMethod(
							MindyPlugin.ModulatorSort.Enhancing);
				} else {
					log.debug("Setting sort to Negative");
					getAggregateModel().setModulatorSortMethod(
							MindyPlugin.ModulatorSort.Negative);
				}
				clearAllTargetTableModulatorSelections();
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
				MindyPlugin.DEFAULT_MODULATOR_LIMIT, 1, 1000, 1);
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
		setTargetAllMarkersCheckBox(new JCheckBox("All Markers"));
		getTargetAllMarkersCheckBox().setSelected(false);

		final ColorGradient gradient = new ColorGradient(Color.blue, Color.red);
		gradient.addColorPoint(Color.white, 0f);

		setAggregateModel(new AggregateTableModel(mindyPlugin,
				mindyPlugin.getMindyData()));
		getAggregateModel().setModLimit(
				MindyPlugin.DEFAULT_MODULATOR_LIMIT);
		getAggregateModel().setModulatorsLimited(
				modulatorLimits.isSelected());
		setTargetTable(new JTable(getAggregateModel()) {
			public Component prepareRenderer(
					TableCellRenderer tableCellRenderer, int row, int col) {
				Component component = super.prepareRenderer(tableCellRenderer,
						row, col);
				if (row % 2 == 0 && !isCellSelected(row, col)) {
					component.setBackground(new Color(237, 237, 237));
				} else {
					// If not shaded, match the table's background
					component.setBackground(getBackground());
				}
				if (colorCheck.isSelected() && col > 1) {
					float score = getAggregateModel().getScoreAt(
							row, col);
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
		});

		/* do not allow the user to reorder table columns */
		getTargetTable().getTableHeader().setReorderingAllowed(
				false);
		// 0 hardcoded for consistency, refactor later
		getTargetTable().getColumnModel().getColumn(0).setMinWidth(
				MindyPlugin.MIN_CHECKBOX_WIDTH);

		getTargetTable().getTableHeader().setDefaultRenderer(
				new CheckBoxRenderer(mindyPlugin));
		getTargetTable().getTableHeader().addMouseListener(
				new ColumnHeaderListener(mindyPlugin));
		getTargetTable().setAutoCreateColumnsFromModel(true);
		setScrollPane(new JScrollPane(getTargetTable()));
		getScrollPane().setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		MindyPlugin.restoreBooleanRenderers(getTargetTable());

		colorCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				getTargetTable().invalidate();
				getAggregateModel().fireTableDataChanged();
			}
		});

		scoreCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				getAggregateModel().setScoreView(
						scoreCheck.isSelected());
				getTargetTable().invalidate();
				getAggregateModel().fireTableDataChanged();
			}
		});

		modulatorLimits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				Integer modLimit = (Integer) modLimitValue.getValue();
				log.debug("Limiting modulators displayed to top "
						+ modLimit);
				boolean selected = modulatorLimits.isSelected();
				limitModulators(modLimit, selected, getTargetTable());
			}
		});

		modLimitValue.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				if (modulatorLimits.isSelected()) {
					limitModulators((Integer) modLimitValue
							.getValue(), true, getTargetTable());
				}
			}
		});

		setSelectAllModsCheckBoxTarget(new JCheckBox(
				"Select All Modulators"));
		getSelectAllModsCheckBoxTarget().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						AggregateTableModel model = (AggregateTableModel) getTargetTable()
								.getModel();
						model
								.selectAllModulators(getSelectAllModsCheckBoxTarget()
										.isSelected());
						getSelectionEnabledCheckBoxTarget().setText(
								MindyPlugin.ENABLE_SELECTION + " "
										+ model.getNumberOfMarkersSelected());
					}
				});

		setSelectAllTargetsCheckBoxTarget(new JCheckBox(
				"Select All Targets"));
		getSelectAllTargetsCheckBoxTarget().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						AggregateTableModel model = (AggregateTableModel) getTargetTable()
								.getModel();
						model.selectAllTargets(getSelectAllTargetsCheckBoxTarget()
								.isSelected());
						getSelectionEnabledCheckBoxTarget().setText(
								MindyPlugin.ENABLE_SELECTION + " "
										+ model.getNumberOfMarkersSelected());
					}
				});

		setAddToSetButtonTarget(new JButton("Add To Set"));
		getAddToSetButtonTarget().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						AggregateTableModel atm = (AggregateTableModel) getTargetTable()
								.getModel();
						mindyPlugin.addToSet(atm
								.getUniqueCheckedTargetsAndModulators(),
								visualPlugin);
					}
				});

		setSelectionEnabledCheckBoxTarget(new JCheckBox(
				MindyPlugin.ENABLE_SELECTION + " 0"));
		getSelectionEnabledCheckBoxTarget().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						boolean selected = getSelectionEnabledCheckBoxTarget()
								.isSelected();
						log.debug("Setting test box visibility to "
								+ selected);
						getAggregateModel()
								.fireTableStructureChanged();
						setTargetControlVisibility(selected);
						setTargetTableViewOptions();
						setTargetCheckboxesVisibility(selected);
						if (!selected) {
							getSelectAllModsCheckBoxTarget()
									.setSelected(false);
							getSelectAllTargetsCheckBoxTarget()
									.setSelected(false);
							getAggregateModel()
									.fireTableStructureChanged();
							getAggregateModel()
									.selectAllModulators(selected);
							getAggregateModel().selectAllTargets(
									selected);
							setTargetCheckboxesVisibility(selected);
							getSelectionEnabledCheckBoxTarget()
									.setText(
											MindyPlugin.ENABLE_SELECTION
													+ " "
													+ getAggregateModel()
															.getNumberOfMarkersSelected());
						}
					}
				});

		getTargetAllMarkersCheckBox().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						getSelectAllTargetsCheckBoxTarget()
								.setSelected(false);
						getSelectionEnabledCheckBoxTarget()
								.setText(
										MindyPlugin.ENABLE_SELECTION
												+ " "
												+ getAggregateModel()
														.getNumberOfMarkersSelected());
						if (getTargetAllMarkersCheckBox()
								.isSelected()
								|| (getAggregateModel().getLimitedTargets() == null)
								|| (getAggregateModel().getLimitedTargets()
										.size() <= 0)) {
							getAggregateModel().showAllMarkers();
						} else {
							getAggregateModel()
									.showLimitedMarkers();
						}
					}
				});

		getTargetTable().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);

		getSelectionEnabledCheckBoxTarget().setSelected(true);
		getSelectAllModsCheckBoxTarget().setEnabled(true);
		getSelectAllTargetsCheckBoxTarget().setEnabled(true);
		getAddToSetButtonTarget().setEnabled(true);

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
		taskContainer.add(getSelectionEnabledCheckBoxTarget());
		taskContainer.add(getSelectAllModsCheckBoxTarget());
		taskContainer.add(getSelectAllTargetsCheckBoxTarget());
		taskContainer.add(getAddToSetButtonTarget());
		taskContainer.add(getTargetAllMarkersCheckBox());
		JPanel p = new JPanel(new BorderLayout());
		p.add(taskContainer, BorderLayout.NORTH);
		JScrollPane sp = new JScrollPane(p);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp,
				getScrollPane());
		panel.setResizeWeight(0.055);
		panel.setOneTouchExpandable(false);
		panel.setContinuousLayout(true);
		tableTab = panel;
	}

	public JSplitPane getTableTab() {
		return tableTab;
	}

	void columnScrolling() {
	//		int w = MIN_CHECKBOX_WIDTH + MIN_MARKER_NAME_WIDTH + boxes.length
		int colCount = getTargetTable().getColumnCount();
		int w = MindyPlugin.MIN_CHECKBOX_WIDTH + MindyPlugin.MIN_MARKER_NAME_WIDTH + colCount
				* MindyPlugin.MIN_SCORE_WIDTH;
		if (w > getScrollPane().getWidth()) {
			getTargetTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		} else {
			getTargetTable()
					.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		}
	}

	void setTargetCheckboxesVisibility(boolean show) {
		if (show) {
			getTargetTable().getColumn(" ").setMaxWidth(30);
			getTargetTable().getColumn(" ").setMinWidth(30);
			getTargetTable().getColumn(" ").setWidth(30);
		} else {
			getTargetTable().getColumn(" ").setMaxWidth(0);
			getTargetTable().getColumn(" ").setMinWidth(0);
			getTargetTable().getColumn(" ").setWidth(0);
		}
	}

	void clearAllTargetTableModulatorSelections() {
		getSelectAllModsCheckBoxTarget().setSelected(false);
		getSelectionEnabledCheckBoxTarget().setText(MindyPlugin.ENABLE_SELECTION + " "
				+ getAggregateModel().getNumberOfMarkersSelected());
	}

	void limitModulators(Integer modLimit, boolean selected, JTable table) {
		getAggregateModel().setModLimit(modLimit);
		getAggregateModel().setModulatorsLimited(selected);
		setTargetCheckboxesVisibility(getSelectionEnabledCheckBoxTarget()
				.isSelected());

		getAggregateModel().fireTableStructureChanged();
		columnScrolling();
	}

	void setTargetControlVisibility(boolean show) {
		getSelectAllModsCheckBoxTarget().setEnabled(show);
		getSelectAllTargetsCheckBoxTarget().setEnabled(show);
		getAddToSetButtonTarget().setEnabled(show);
	}

	void setTargetTableViewOptions() {
		boolean selected = getSelectionEnabledCheckBoxTarget().isSelected();
		if (selected) {
			getTargetTable().getColumnModel().getColumn(0).setMaxWidth(30);
		} else {
			getTargetTable().getColumnModel().getColumn(0).setMaxWidth(0);
		}
		setTargetCheckboxesVisibility(selected);
	}

}
