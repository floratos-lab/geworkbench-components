package org.geworkbench.components.mindy;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.events.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.solarmetric.ide.ui.CheckboxCellRenderer;

/**
 * MINDY result view main GUI class
 * 
 * @author mhall
 * @ch2514
 * 
 * @version $Id: MindyPlugin.java,v 1.47 2007-07-30 17:05:09 hungc Exp $
 */
@SuppressWarnings("serial")
public class MindyPlugin extends JPanel {

    static Log log = LogFactory.getLog(MindyPlugin.class);

    private static final int DEFAULT_MODULATOR_LIMIT = 10;
	private static final String NUM_MOD_SELECTED_LABEL = "Modulators Selected: ";
	private static final String ENABLE_SELECTION = "Enable Selection";
	private static final String WARNING = "Warning";
    private static enum ModulatorSort {Aggregate, Enhancing, Negative;}

    private AggregateTableModel aggregateModel;
    private ModulatorTargetModel modTargetModel;
    private ModulatorModel modulatorModel;
    private ModulatorHeatMap heatmap;
    
    private JTabbedPane tabs;

    private JCheckBox selectionEnabledCheckBox, selectionEnabledCheckBoxTarget;
    private JList heatMapModNameList;

    private JScrollPane heatMapScrollPane;
    private List<DSGeneMarker> modulators;
    private List<DSGeneMarker> transFactors;
    private MindyData mindyData;
    private JTable modTable, listTable, targetTable;
    private JCheckBox selectAll /*list tab*/, selectAllModsCheckBox, selectAllModsCheckBoxTarget;
    private JCheckBox selectAllTargetsCheckBox, selectAllTargetsCheckBoxTarget;
    private JCheckBox heatmapAllMarkersCheckBox, targetAllMarkersCheckBox, listAllMarkersCheckBox;
    private JButton addToSetButton /*list tab*/, addToSetButtonTarget, addToSetButtonMod;
    private JLabel numModSelectedInModTab;
    
	private JCheckBox[] boxes;

    // Contains the state of selections passed in from the marker panel and overrides via All Markers checkboxes
    private MarkerLimitState globalSelectionState = new MarkerLimitState();

    /**
     * Constructor.
     * 
     * @param data - MINDY data
     * @param visualPlugin - MINDY component (the class the implements the VisualPlugin interface)
     */
    @SuppressWarnings("serial")
    public MindyPlugin(MindyData data, final MindyVisualComponent visualPlugin) {
        this.mindyData = data;
        modulators = mindyData.getModulators();


        tabs = new JTabbedPane();
        {
            // Modulator Table
            modulatorModel = new ModulatorModel(mindyData);
            modTable = new JTable(modulatorModel){
                public Component prepareRenderer(TableCellRenderer renderer,
                        int rowIndex, int vColIndex) {
                		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                		if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
                			c.setBackground(new Color(237, 237, 237));
                		} else {
                			// If not shaded, match the table's background
                			c.setBackground(getBackground());
                		}
                		return c;
                }
            }; 

            JScrollPane scrollPane = new JScrollPane(modTable);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            restoreBooleanRenderers(modTable);
            modTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            renderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            modTable.getColumnModel().getColumn(5).setCellRenderer(renderer);
            modTable.getColumnModel().getColumn(0).setMaxWidth(15);
            modTable.setAutoCreateColumnsFromModel(false);
            modTable.getTableHeader().addMouseListener(new ColumnHeaderListener());
                        
            numModSelectedInModTab = new JLabel(NUM_MOD_SELECTED_LABEL + 0);
            addToSetButtonMod = new JButton("Add To Set");
            addToSetButtonMod.addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent actionEvent){
            		ModulatorModel model = (ModulatorModel) modTable.getModel();
            		List<DSGeneMarker> selections = model.getSelectedModulators();
            		if(selections.size() > 0) addToSet(selections, visualPlugin);
            	}
            });

            JLabel ll = new JLabel("List Selections  ", SwingConstants.LEFT);
            ll.setFont(new Font(ll.getFont().getName(), Font.BOLD, 12));
            ll.setForeground(Color.BLUE);
            selectAll = new JCheckBox("Select All");
            selectAll.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    ModulatorModel model = (ModulatorModel) modTable.getModel();
                    model.selectAllModulators(selectAll.isSelected());
                    modTable.repaint();
                    numModSelectedInModTab.setText(NUM_MOD_SELECTED_LABEL + model.getNumberOfModulatorsSelected());
                    numModSelectedInModTab.repaint();
                }
            });
            
            JLabel dl = new JLabel("Marker Display  ", SwingConstants.LEFT);
            dl.setFont(new Font(dl.getFont().getName(), Font.BOLD, 12));
            dl.setForeground(Color.BLUE);
            JRadioButton showSymbol = new JRadioButton("Symbol");
            showSymbol.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		modulatorModel.setShowProbeName(false);
            		modulatorModel.fireTableDataChanged();
            	}
            });
            JRadioButton showProbeName = new JRadioButton("Probe Name");
            showProbeName.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		modulatorModel.setShowProbeName(true);
            		modulatorModel.fireTableDataChanged();
            	}
            });
            ButtonGroup displayGroup = new ButtonGroup();
            displayGroup.add(showSymbol);
            displayGroup.add(showProbeName);
            showProbeName.setSelected(true);
            
            JPanel taskContainer = new JPanel(new GridLayout(7, 1, 10, 10));
            taskContainer.add(dl);
            taskContainer.add(showProbeName);
            taskContainer.add(showSymbol);
            taskContainer.add(ll);
            taskContainer.add(selectAll);
            taskContainer.add(numModSelectedInModTab);
            taskContainer.add(addToSetButtonMod);
            JPanel p = new JPanel(new BorderLayout());
            p.add(taskContainer, BorderLayout.NORTH);
            JScrollPane sp = new JScrollPane(p);
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, scrollPane);
            panel.setResizeWeight(0.055);
            panel.setOneTouchExpandable(false);
            panel.setContinuousLayout(true);

            tabs.add("Modulator", panel);
        }

        {
            // Modulator / Target Table
            
            JLabel dl = new JLabel("Marker Display  ", SwingConstants.LEFT);
            dl.setFont(new Font(dl.getFont().getName(), Font.BOLD, 12));
            dl.setForeground(Color.BLUE);
            JRadioButton showSymbol = new JRadioButton("Symbol");
            showSymbol.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		aggregateModel.setShowProbeName(false);
            		aggregateModel.fireTableStructureChanged();
            		MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
            	}
            });
            JRadioButton showProbeName = new JRadioButton("Probe Name");
            showProbeName.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		aggregateModel.setShowProbeName(true);
            		aggregateModel.fireTableStructureChanged();
            		MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
            	}
            });
            ButtonGroup displayGroup = new ButtonGroup();
            displayGroup.add(showSymbol);
            displayGroup.add(showProbeName);
            showProbeName.setSelected(true);

            JLabel ls = new JLabel("Sorting", SwingConstants.LEFT);
            ls.setFont(new Font(ls.getFont().getName(), Font.BOLD, 12));
            ls.setForeground(Color.BLUE);
            

            JRadioButton sortOptionsAggregate = new JRadioButton("Aggregate");
            JRadioButton sortOptionsEnhancing = new JRadioButton("Enhancing");
            JRadioButton sortOptionsNegative = new JRadioButton("Negative");            
            ButtonGroup sortGroup = new ButtonGroup();
            sortGroup.add(sortOptionsAggregate);
            sortGroup.add(sortOptionsEnhancing);
            sortGroup.add(sortOptionsNegative);
            ActionListener sortAction = new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		String selected = actionEvent.getActionCommand().toString();
            		if (selected.equals("Aggregate")) {
                        log.debug("Setting sort to Aggregate");
                        aggregateModel.setModulatorSortMethod(ModulatorSort.Aggregate);
                    } else if (selected.equals("Enhancing")) {
                        log.debug("Setting sort to Enhancing");
                        aggregateModel.setModulatorSortMethod(ModulatorSort.Enhancing);
                    } else {
                        log.debug("Setting sort to Negative");
                        aggregateModel.setModulatorSortMethod(ModulatorSort.Negative);
                    }
            		MindyPlugin.this.clearAllTargetTableModulatorSelections();
            	}
            };
            sortOptionsAggregate.addActionListener(sortAction);
            sortOptionsEnhancing.addActionListener(sortAction);
            sortOptionsNegative.addActionListener(sortAction);   
            sortOptionsAggregate.setSelected(true);
            

            Panel limitControls = new Panel(new BorderLayout());
            JLabel lm = new JLabel("Modulator Limits", SwingConstants.LEFT);
            lm.setFont(new Font(lm.getFont().getName(), Font.BOLD, 12));
            lm.setForeground(Color.BLUE);
            final JCheckBox modulatorLimits = new JCheckBox("Limit To Top");
            modulatorLimits.setSelected(true);
            limitControls.add(modulatorLimits, BorderLayout.WEST);
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(DEFAULT_MODULATOR_LIMIT, 1, 1000, 1);
            final JSpinner modLimitValue = new JSpinner(spinnerModel);
            limitControls.add(modLimitValue, BorderLayout.CENTER);

            JLabel ld = new JLabel("Display Options", SwingConstants.LEFT);
            ld.setFont(new Font(ld.getFont().getName(), Font.BOLD, 12));
            ld.setForeground(Color.BLUE);
            final JCheckBox colorCheck = new JCheckBox("Color View");
            final JCheckBox scoreCheck = new JCheckBox("Score View");
            
            JLabel lmp = new JLabel("Marker Override  ", SwingConstants.LEFT);
            lmp.setFont(new Font(lmp.getFont().getName(), Font.BOLD, 12));
            lmp.setForeground(Color.BLUE);
            targetAllMarkersCheckBox = new JCheckBox("All Markers");
            targetAllMarkersCheckBox.setSelected(false);

            final ColorGradient gradient = new ColorGradient(Color.blue, Color.red);
            gradient.addColorPoint(Color.white, 0f);

            aggregateModel = new AggregateTableModel(mindyData);
            aggregateModel.setModLimit(DEFAULT_MODULATOR_LIMIT);
            aggregateModel.setModulatorsLimited(modulatorLimits.isSelected());
            targetTable = new JTable(aggregateModel){
                public Component prepareRenderer(TableCellRenderer tableCellRenderer, int row, int col) {
                    Component component = super.prepareRenderer(tableCellRenderer, row, col);
               		if (row % 2 == 0 && !isCellSelected(row, col)) {
            			component.setBackground(new Color(237, 237, 237));
            		} else {
            			// If not shaded, match the table's background
            			component.setBackground(getBackground());
            		}
               		if (colorCheck.isSelected() && col > 1) {  
                        float score = aggregateModel.getScoreAt(row, col);
                        if(score != 0){
                        	//ch2514 -- change to use ColorContext??
                        	//Color cellColor = colorContext.getMarkerValueColor(((DSMicroarray) mindyData.getArraySet().get(i)).getMarkerValue(modulator), modulator, 1.0f);
                            component.setBackground(gradient.getColor(score));
                        	//component.setBackground(cellColor);
                            //display red/blue only
                            //component.setBackground(gradient.getColor(Math.signum(score) * 1));
                        }
                    }
                    return component;
                }
            };
            targetTable.getTableHeader().setDefaultRenderer(new CheckBoxRenderer());
            targetTable.getTableHeader().addMouseListener(new ColumnHeaderListener());
            targetTable.setAutoCreateColumnsFromModel(true);
            JScrollPane scrollPane = new JScrollPane(targetTable);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            restoreBooleanRenderers(targetTable);

            colorCheck.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                	targetTable.invalidate();
                	targetTable.repaint();
                }
            });

            scoreCheck.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    aggregateModel.setScoreView(scoreCheck.isSelected());
                    targetTable.invalidate();
                    targetTable.repaint();
                }
            });

            modulatorLimits.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    Integer modLimit = (Integer) modLimitValue.getValue();
                    log.debug("Limiting modulators displayed to top " + modLimit);
                    boolean selected = modulatorLimits.isSelected();
                    limitModulators(modLimit, selected, targetTable);
                }
            });

            modLimitValue.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    if (modulatorLimits.isSelected()) {
                        limitModulators((Integer) modLimitValue.getValue(), true, targetTable);
                    }
                }
            });
            
            
            selectAllModsCheckBoxTarget = new JCheckBox("Select All Modulators");
            selectAllModsCheckBoxTarget.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                	AggregateTableModel model = (AggregateTableModel) targetTable.getModel();
                    model.selectAllModulators(selectAllModsCheckBoxTarget.isSelected());
                    selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " [" + model.getUniqueCheckedTargetsAndModulators().size() + "]");
                }
            });
            
            
            selectAllTargetsCheckBoxTarget = new JCheckBox("Select All Targets");
            selectAllTargetsCheckBoxTarget.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                	AggregateTableModel model = (AggregateTableModel) targetTable.getModel();
                	model.selectAllTargets(selectAllTargetsCheckBoxTarget.isSelected());
                		selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " [" + model.getUniqueCheckedTargetsAndModulators().size() + "]");
                	}
            });
            

            addToSetButtonTarget = new JButton("Add To Set");
            addToSetButtonTarget.addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent actionEvent){
            		AggregateTableModel atm = (AggregateTableModel) targetTable.getModel();
                	addToSet(atm.getUniqueCheckedTargetsAndModulators(), visualPlugin);                	
            	}
            });
            
            selectionEnabledCheckBoxTarget = new JCheckBox(ENABLE_SELECTION + " [0]");
            selectionEnabledCheckBoxTarget.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    boolean selected = selectionEnabledCheckBoxTarget.isSelected();
                    log.debug("Setting test box visibility to " + selected);
                    aggregateModel.fireTableStructureChanged();
                    setTargetControlVisibility(selected);
                    setTargetTableViewOptions();
                    setTargetCheckboxesVisibility(selected);
                    if(!selected){
                    	selectAllModsCheckBoxTarget.setSelected(false);
                    	selectAllTargetsCheckBoxTarget.setSelected(false);
                    	aggregateModel.fireTableStructureChanged();
                    	aggregateModel.selectAllModulators(selected);
                    	aggregateModel.selectAllTargets(selected);
                    	setTargetCheckboxesVisibility(selected);
                    	selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " [" + aggregateModel.getUniqueCheckedTargetsAndModulators().size() + "]");
                    }
                }
            });
            
            
            targetAllMarkersCheckBox.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		selectAllTargetsCheckBoxTarget.setSelected(false);
    	        	selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " [0]");
            		if(targetAllMarkersCheckBox.isSelected()
            				|| (aggregateModel.limitedTargets == null)
            				|| (aggregateModel.limitedTargets.size() <= 0)
            				){
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
            taskContainer.add(showProbeName);
            taskContainer.add(showSymbol);
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
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, scrollPane);
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
            showSymbol.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		modTargetModel.setShowProbeName(false);
            		modTargetModel.fireTableDataChanged();
            	}
            });
            JRadioButton showProbeName = new JRadioButton("Probe Name");
            showProbeName.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		modTargetModel.setShowProbeName(true);
            		modTargetModel.fireTableDataChanged();
            	}
            });
            ButtonGroup displayGroup = new ButtonGroup();
            displayGroup.add(showSymbol);
            displayGroup.add(showProbeName);
            showProbeName.setSelected(true);

            JLabel l = new JLabel("Marker Set", SwingConstants.LEFT);
            l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
            l.setForeground(Color.BLUE);

            modTargetModel = new ModulatorTargetModel(mindyData);
            listTable = new JTable(modTargetModel){
                public Component prepareRenderer(TableCellRenderer renderer,
                        int rowIndex, int vColIndex) {
                		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                		if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
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
            listTable.getTableHeader().addMouseListener(new ColumnHeaderListener());

            restoreBooleanRenderers(listTable);

            selectionEnabledCheckBox = new JCheckBox(ENABLE_SELECTION);
            selectionEnabledCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    boolean selected = selectionEnabledCheckBox.isSelected();
                    log.debug("Setting list box visibility to " + selected);
                    setListCheckboxesVisibility(selected);
                    setListControlVisibility(selected);
                    setListTableViewOptions();
                    if(!selected){
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
                    ModulatorTargetModel model = (ModulatorTargetModel) listTable.getModel();
                    model.selectAllModulators(selectAllModsCheckBox.isSelected());
                    listTable.repaint();
                    selectionEnabledCheckBox.setText(MindyPlugin.ENABLE_SELECTION + " [" + model.getUniqueSelectedMarkers().size() + "]");
                    selectionEnabledCheckBox.repaint();
                }
            });
            selectAllTargetsCheckBox = new JCheckBox("Select All Targets");
            selectAllTargetsCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    ModulatorTargetModel model = (ModulatorTargetModel) listTable.getModel();
                    model.selectAllTargets(selectAllTargetsCheckBox.isSelected());
                    listTable.repaint();
                    selectionEnabledCheckBox.setText(MindyPlugin.ENABLE_SELECTION + " [" + model.getUniqueSelectedMarkers().size() + "]");
                    selectionEnabledCheckBox.repaint();
                }
            });

            addToSetButton = new JButton("Add To Set");
            addToSetButton.addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent actionEvent){
            		ModulatorTargetModel model = (ModulatorTargetModel) listTable.getModel();
            		addToSet(model.getUniqueSelectedMarkers(), visualPlugin);
            	}
            });
            setListControlVisibility(true);

            l = new JLabel("Marker Override", SwingConstants.LEFT);
            l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
            l.setForeground(Color.BLUE);
            listAllMarkersCheckBox = new JCheckBox("All Markers"); 
            listAllMarkersCheckBox.setSelected(false);
            listAllMarkersCheckBox.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		if(listAllMarkersCheckBox.isSelected()
            				|| (modTargetModel.limitedModulators == null)
            				|| (modTargetModel.limitedModulators.size() <= 0)
            				|| (modTargetModel.limitedTargets == null)
            				|| (modTargetModel.limitedTargets.size() <= 0)
            			){
            			modTargetModel.showAllMarkers();
            		} else {
            	        modTargetModel.showLimitedMarkers();
            		}
            	}
            });
            
            selectionEnabledCheckBox.setSelected(true);
            selectAllModsCheckBox.setEnabled(true);
            selectAllTargetsCheckBox.setEnabled(true);
            addToSetButton.setEnabled(true);

            JPanel p = new JPanel(new GridLayout(9, 1, 10, 10));
            p.add(dl);
            p.add(showProbeName);
            p.add(showSymbol);
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
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, scrollPane);
            panel.setResizeWeight(0.055);
            panel.setOneTouchExpandable(false);
            panel.setContinuousLayout(true);

            tabs.add("List", panel);
        }

        {
            // Heat Map

            // This is modulator just to give us something to generate the heat map with upon first running
            DSGeneMarker modulator = modulators.iterator().next();
            transFactors = mindyData.getTranscriptionFactors(modulator);
            heatmap = new ModulatorHeatMap(modulator, transFactors.iterator().next(), mindyData, null);
            heatMapScrollPane = new JScrollPane(heatmap);

            JPanel transFacPane = new JPanel(new BorderLayout());
            JLabel l = new JLabel("Transcription Factor", SwingConstants.LEFT);
            l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
            l.setForeground(Color.BLUE);
            transFacPane.add(l, BorderLayout.NORTH);
            JLabel transFactorName = new JLabel(heatmap.getMarkerDisplayName(mindyData.getTranscriptionFactor()));
            transFacPane.add(transFactorName);

            JPanel modulatorPane = new JPanel(new BorderLayout());
            JTextField modFilterField = new JTextField(15);
            l = new JLabel("Modulators", SwingConstants.LEFT);
            l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
            l.setForeground(Color.BLUE);
            modulatorPane.add(l, BorderLayout.NORTH);
                        
            heatMapModNameList = new JList();
            heatMapModNameList.setFixedCellWidth(12);
            JScrollPane modListScrollPane = new JScrollPane(heatMapModNameList);
            heatMapModNameList.setModel(new ModulatorListModel());
            heatMapModNameList.setSelectedIndex(0);
            
            JButton screenshotButton = new JButton("  Take Screenshot  ");
            screenshotButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    visualPlugin.createImageSnapshot(heatMapScrollPane.getViewport().getComponent(0));
                }
            });
            
            heatmapAllMarkersCheckBox = new JCheckBox("All Markers");
            heatmapAllMarkersCheckBox.setSelected(false);
            heatmapAllMarkersCheckBox.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		if(heatmapAllMarkersCheckBox.isSelected()){
            			rebuildHeatMap(null);
            		} else {
            			rebuildHeatMap(visualPlugin.getSelectedMarkers());
            		}
            	}
            });
            
            heatMapModNameList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent listSelectionEvent) {
                	if(heatmapAllMarkersCheckBox.isSelected()){
            			rebuildHeatMap(null);
            		} else {
            			rebuildHeatMap(visualPlugin.getSelectedMarkers());
            		}
                }
            });
            
            AutoCompleteDecorator.decorate(heatMapModNameList, modFilterField);
            JButton refreshButton = new JButton("  Refresh HeatMap  ");
            refreshButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                	heatMapModNameList.setSelectedIndex(0);
                	if(heatmapAllMarkersCheckBox.isSelected()){
            			rebuildHeatMap(null);
            		} else {
            			rebuildHeatMap(visualPlugin.getSelectedMarkers());
            		}
                }
            });
            
            JLabel dl = new JLabel("Marker Display  ", SwingConstants.LEFT);
            dl.setFont(new Font(dl.getFont().getName(), Font.BOLD, 12));
            dl.setForeground(Color.BLUE);
            JRadioButton showSymbol = new JRadioButton("Symbol");
            showSymbol.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		heatmap.setShowProbeName(false);
            		if(heatmapAllMarkersCheckBox.isSelected()){
            			rebuildHeatMap(null);
            		} else {
            			rebuildHeatMap(visualPlugin.getSelectedMarkers());
            		}
            	}
            });
            JRadioButton showProbeName = new JRadioButton("Probe Name");
            showProbeName.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		heatmap.setShowProbeName(true);
            		if(heatmapAllMarkersCheckBox.isSelected()){
            			rebuildHeatMap(null);
            		} else {
            			rebuildHeatMap(visualPlugin.getSelectedMarkers());
            		}
            	}
            });
            ButtonGroup displayGroup = new ButtonGroup();
            displayGroup.add(showSymbol);
            displayGroup.add(showProbeName);
            showProbeName.setSelected(true);
            
            
            JPanel displayPane = new JPanel(new GridLayout(4, 1));
            displayPane.add(dl);
            displayPane.add(showProbeName);
            displayPane.add(showSymbol);

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
            modTransScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, modTransScroll, heatMapScrollPane);
            panel.setResizeWeight(0.055);
            panel.setOneTouchExpandable(false);
            panel.setContinuousLayout(true);
            
            tabs.add("Heat Map", panel);
        }

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        tabs.setEnabledAt(1, false);
        tabs.setEnabledAt(2, false);
        tabs.setEnabledAt(3, false);
    }

    private void limitModulators(Integer modLimit, boolean selected, JTable table){
        aggregateModel.setModLimit(modLimit);
        aggregateModel.setModulatorsLimited(selected);
        aggregateModel.clearModulatorSelections();
        aggregateModel.fireTableStructureChanged();
        this.clearAllTargetTableModulatorSelections();
        MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
    }
    
    private void clearAllTargetTableModulatorSelections(){
        selectAllModsCheckBoxTarget.setSelected(false);
        selectAllModsCheckBoxTarget.repaint();
        selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " [" + aggregateModel.getUniqueCheckedTargetsAndModulators().size() + "]");
        selectionEnabledCheckBoxTarget.repaint();
    }

    private void rebuildHeatMap(List<DSGeneMarker> targetLimits) {
        int selectedIndex = heatMapModNameList.getSelectedIndex();
        DSGeneMarker modMarker;
        if((modTargetModel.getEnabledModulators() == null) 
        		|| (modTargetModel.getEnabledModulators().size() <= 0)
        		){
        	log.warn("No modulators selected.");
        	return;
        }
        if (selectedIndex > 0) {
            modMarker = modTargetModel.getEnabledModulators().get(selectedIndex);
        } else {
            modMarker = modTargetModel.getEnabledModulators().get(0);
            heatMapModNameList.setSelectedIndex(0);
        }

        if (modMarker != null) {
        	if(targetLimits != null){
        		log.debug("Rebuilding heat map with limited markers");
        		boolean b = heatmap.isShowProbeName();
        		heatmap = new ModulatorHeatMap(modMarker, mindyData.getTranscriptionFactor(), mindyData, targetLimits);
        		heatmap.setShowProbeName(b);
        		setHeatMap(heatmap);
        	} else {
	            log.debug("Rebuilding heat map.");
	            boolean b = heatmap.isShowProbeName();
	            heatmap = new ModulatorHeatMap(modMarker, mindyData.getTranscriptionFactor(), mindyData, null);
	            heatmap.setShowProbeName(b);
	            setHeatMap(heatmap);
        	}
        }
    }

    private void restoreBooleanRenderers(JTable table){
        table.setDefaultEditor(Boolean.class, new DefaultCellEditor(new JCheckBox()));
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
    	if(show){
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
        listTable.repaint();
    }
    
    private void setTargetCheckboxesVisibility(boolean show){
    	if(show){
    		targetTable.getColumn(" ").setMaxWidth(15);
    		targetTable.getColumn(" ").setMinWidth(15);
    		targetTable.getColumn(" ").setWidth(15);
    	} else {
    		targetTable.getColumn(" ").setMaxWidth(0);
    		targetTable.getColumn(" ").setMinWidth(0);
    		targetTable.getColumn(" ").setWidth(0);
    	}
    	targetTable.repaint();
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
        heatMapScrollPane.repaint();
    }

    /**
     * Callback method for the MINDY result view GUI when the user changes marker set selections in the Selection Panel.
     * 
     * @param - list of selected markers
     */
    public void limitMarkers(List<DSGeneMarker> markers) {        
        // modulator table tab    
    	modulatorModel.limitMarkers(markers);
        
        // target table tab
        aggregateModel.limitMarkers(markers);
                
        // list table tab
        modTargetModel.limitMarkers(markers);       
                
        // heat map
        if((!heatmapAllMarkersCheckBox.isSelected())
        		&& (markers != null)
        		&& (markers.size() > 0)
        		){
        	heatmap.setTargetLimits(markers);
        	rebuildHeatMap(markers);        	
        } else {
        	rebuildHeatMap(null);
        }
    }
    
    /**
     * Specifies the marker name (probe name vs. gene name) 
     * to display on the table (modulator, targets, or list).
     * 
     * @param model - table data model of the table displaying marker names
     * @param marker - gene marker
     * @return The marker name (probe vs. gene) to display on the heat map.
     */
    public String getMarkerDisplayName(TableModel model, DSGeneMarker marker){
    	String result = marker.getGeneName();
    	boolean showProbeName = false;
    	if(model instanceof ModulatorModel){
    		showProbeName = ((ModulatorModel) model).isShowProbeName();
    	}
    	if(model instanceof AggregateTableModel){
    		showProbeName = ((AggregateTableModel) model).isShowProbeName();
    	}
    	if(model instanceof ModulatorTargetModel){
    		showProbeName = ((ModulatorTargetModel) model).isShowProbeName();
    	}
    	if(showProbeName){
    		result = marker.getLabel();
    	}
    	return result;
    }
    
    private JCheckBox[] getHeaderCheckBoxes(){
    	return this.boxes;
    }
    
    @SuppressWarnings("unchecked")
    private void addToSet(List<DSGeneMarker> selections, final MindyVisualComponent visualPlugin){
    	if(visualPlugin == null){
    		log.error("No plugin from which to add to set.");
    		return;
    	}
    	
    	if((selections == null) || (selections.size() <= 0)){
    		JOptionPane.showMessageDialog(this, "No markers selected.", WARNING, JOptionPane.WARNING_MESSAGE);
    		return;
    	}    		
    	
    	// Ask user for marker set label
    	String tmpLabel = JOptionPane.showInputDialog("Set Label:", "");
    	if (tmpLabel == null || tmpLabel.equals("")) 
          return;
    	else {
    		DSPanel<DSGeneMarker> subpanel = null; 
    		// If Mindy plugin doesn't have access to selector panel and/or marker sets
    		if(((visualPlugin.getSelectorPanel() == null) || (visualPlugin.getSelectorPanel().panels() == null))
    				&& ((visualPlugin.getSelectedMarkers() == null) || (visualPlugin.getSelectedMarkers().size() <= 0))
    				){
        		log.error("Plugin does not have a viable selector panel/marker set.");
        		subpanel = new CSPanel<DSGeneMarker>();
    			subpanel.setLabel(tmpLabel);
    			for(int i = 0; i < selections.size(); i++){
    				Object o = selections.get(i);
    				if(o instanceof DSGeneMarker){
    					subpanel.add((DSGeneMarker) o);
    				}
    			}
                visualPlugin.publishSubpanelChangedEvent(new SubpanelChangedEvent(DSGeneMarker.class, subpanel, SubpanelChangedEvent.NEW));
        		return;
        	} 
    		
    		// Checking to see if a subpanel already has the same label
    		if((visualPlugin.getSelectorPanel() != null) && (visualPlugin.getSelectorPanel().panels() != null)){
    			DSPanel selectorPanel = visualPlugin.getSelectorPanel();
	    		for(int i = 0; i < selectorPanel.panels().size(); i++){
	    			Object o = selectorPanel.panels().get(i);
	    			if((o instanceof DSPanel) && ((DSPanel) o).getLabel().equals(tmpLabel)){
	    				subpanel = ((DSPanel) o);
	    				for(int j = 0; j < selections.size(); j++){
	    					Object oo = selections.get(j);
	    					if(oo instanceof DSGeneMarker){
	    						// Checking to see if marker is already part of the subpanel
	    						if(subpanel.contains((DSGeneMarker) oo)){
	    							continue;
	    						} else {    					
	    						// If not, add the marker to the subpanel
	    							subpanel.add((DSGeneMarker) oo);
	    						}
	    					}
	    				}
	    	            visualPlugin.publishSubpanelChangedEvent(new SubpanelChangedEvent(DSGeneMarker.class, subpanel, SubpanelChangedEvent.SET_CONTENTS));
	    				return;
	    			}
	    		}
    		} else {
    			// If not, check if Mindy plugin has a selected marker set
    			if((visualPlugin.getSelectedMarkers() != null) && (visualPlugin.getSelectedMarkers().size() > 0)){
    				boolean needToAdd = false;
    				subpanel = new CSPanel<DSGeneMarker>();
        			subpanel.setLabel(tmpLabel);
    				for(int i = 0; i < selections.size(); i++){
    					Object o = selections.get(i);
    					if((o instanceof DSGeneMarker) 
    							&& (!visualPlugin.getSelectedMarkers().contains((DSGeneMarker) o))
    							){
    						subpanel.add((DSGeneMarker) o);
    						needToAdd = true;
    					}
    				}
    				if(needToAdd){
    					for(int i = 0; i < visualPlugin.getSelectedMarkers().size(); i++){
    						Object o = visualPlugin.getSelectedMarkers().get(i);
    						if(o instanceof DSGeneMarker){
    							subpanel.add((DSGeneMarker) o);
    						}
    					}
    					visualPlugin.publishSubpanelChangedEvent(new SubpanelChangedEvent(DSGeneMarker.class, subpanel, SubpanelChangedEvent.SET_CONTENTS));
	    				return;
    				}
    			}
    		}
    		
    		// If not, create a subpanel and add selected targets/modulators to the subpanel and publish event
    		if(subpanel == null){
    			subpanel = new CSPanel<DSGeneMarker>();
    			subpanel.setLabel(tmpLabel);
    			for(int i = 0; i < selections.size(); i++){
    				Object o = selections.get(i);
    				if(o instanceof DSGeneMarker){
    					subpanel.add((DSGeneMarker) o);
    				}
    			}
                visualPlugin.publishSubpanelChangedEvent(new SubpanelChangedEvent(DSGeneMarker.class, subpanel, SubpanelChangedEvent.NEW));
    		} 
    		
    	}
    }
    
     //Models and support classes follow

    /**
     * Modulator table data model.
     * 
     * @author mhall
     * @author ch2514
     * @version $Id: MindyPlugin.java,v 1.47 2007-07-30 17:05:09 hungc Exp $
     */
    private class ModulatorModel extends DefaultTableModel {

        private boolean[] enabled;
        private List<DSGeneMarker> modulators;
        private List<DSGeneMarker> limitedModulators = new ArrayList<DSGeneMarker>();
        private MindyData mindyData; 
        private String[] columnNames = new String[]{" ", "Modulator", " M# ", " M+ ", " M- ", " Mode ", "Modulator Description"};
        private boolean[] ascendSortStates;
        private boolean showProbeName = true;

        /**
         * Constructor.
         * 
         * @param mindyData - MINDY data
         */
        public ModulatorModel(MindyData mindyData) {
            modulators = new ArrayList<DSGeneMarker>();
            for (Map.Entry<DSGeneMarker, MindyData.ModulatorStatistics> entry : mindyData.getAllModulatorStatistics().entrySet())
            {
                modulators.add(entry.getKey());
            }
            this.enabled = new boolean[modulators.size()];
            this.mindyData = mindyData;
            this.ascendSortStates = new boolean[columnNames.length];
            for(int i = 0; i < this.ascendSortStates.length; i++)
            	this.ascendSortStates[i] = true;
        }

        /**
         * Callback method for the modulator table when the user changes marker set selections in the Selection Panel.
         * 
         * @param - list of selected markers
         */
        public void limitMarkers(List<DSGeneMarker> limitList) {
            if (limitList == null) {
                limitedModulators = null;
                log.debug("Cleared modulator limits.");
            } else {
                limitedModulators = new ArrayList<DSGeneMarker>();
                for (DSGeneMarker marker : limitList) {
                    if (modulators.contains(marker)) {
                        limitedModulators.add(marker);
                    }
                }
                log.debug("Limited modulators table to " + limitedModulators.size() + " mods.");
                this.enabled = new boolean[modulators.size()];
            }
            if (!globalSelectionState.allMarkerOverride) {
                fireTableDataChanged();
            }
        }

        /**
         * Get the number of columns in the modulator table.
         * @return the number of columns in the modulator table
         */
        public int getColumnCount() {
            return columnNames.length;
        }

        /**
         * Get the number of rows on the modulator table.
         * 
         * @return number of rows on the table
         */
        public int getRowCount() {
            if (globalSelectionState.allMarkerOverride) {
                if (enabled == null) {
                    return 0;
                } else {
                    return modulators.size();
                }
            } else {
                if (limitedModulators == null) {
                    return 0;
                }
                return limitedModulators.size();
            }
        }

        /**
         * Whether or not the specified modulator table cell is editable.
         * 
         * @param rowIndex - row index of the table cell
         * @prarm columnIndex - column index of the table cell
         * @return true if the table cell is editable, and false otherwise
         */
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Get the class object representing the specified table column.
         * 
         * @param columnIndex - column index
         * @return the class object representing the table column
         */
        @SuppressWarnings("unchecked")
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else if (columnIndex == 1) {
                return String.class;
            } else if (columnIndex == getColumnCount() - 1 || columnIndex == getColumnCount() - 2) {
                return String.class;
            } else {
                return Integer.class;
            }
        }

        /**
         * Get the values of modulator table cells.
         * 
         * @param rowIndex - row index of the cell
         * @param columnIndex - column index of the cell
         * @return the value object of specified table cell
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            DSGeneMarker mod;
            mod = getModulatorForIndex(rowIndex);
            if (columnIndex == 0) {
                return enabled[rowIndex];
            } else if (columnIndex == 1) {
                //return mod.getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            	return getMarkerDisplayName(this, mod);
            } else if (columnIndex == 2) {
                return mindyData.getStatistics(mod).getCount();
            } else if (columnIndex == 3) {
                return mindyData.getStatistics(mod).getMover();
            } else if (columnIndex == 4) {
                return mindyData.getStatistics(mod).getMunder();
            } else if (columnIndex == 5) {
                int mover = mindyData.getStatistics(mod).getMover();
                int munder = mindyData.getStatistics(mod).getMunder();
                if (mover > munder) {
                    return "+";
                } else if (mover < munder) {
                    return "-";
                } else {
                    return "=";
                }
            } else {
                return mod.getDescription();
            }
        }

        /**
         * Set values of modulator table cells.
         * 
         * @param aValue - value of the cell
         * @param rowIndex - row index of the cell
         * @param columnIndex - column index of the cell
         */
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                enableModulator(rowIndex, (Boolean) aValue);
                if (this.getNumberOfModulatorsSelected() == enabled.length)
                	selectAll.setSelected(true);
                else 
                	selectAll.setSelected(false);
                numModSelectedInModTab.setText(MindyPlugin.NUM_MOD_SELECTED_LABEL + this.getNumberOfModulatorsSelected());
                numModSelectedInModTab.repaint();
            }
        }

        private DSGeneMarker getModulatorForIndex(int rowIndex) {
            DSGeneMarker mod;
            if (globalSelectionState.allMarkerOverride) {
                mod = modulators.get(rowIndex);
            } else {
                mod = limitedModulators.get(rowIndex);
            }
            return mod;
        }

        /**
         * Get the column name of the specified column index.
         * 
         * @param columnIndex - index of the column
         * @return name of the column
         */
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }
        
        /**
         * Get the sorting states (ascending or descending) of each column in the modulator table.
         * 
         * @return a list of sorting states (ascending = true, descending = false)
         */
        public boolean[] getAscendSortStates(){
        	return this.ascendSortStates;
        }
        
        /**
         * Set the sorting states (ascending or descending) of each column in the modulator table.
         * 
         * @param states - a list of sorting states (ascending = true, descending = false)
         */
        public void setAscendSortStates(boolean[] states){
        	this.ascendSortStates = states;
        }

        /**
         * Select all modulators on the modulator table.
         * 
         * @param selected - true to select all modulators on the table, and false otherwise
         */
        public void selectAllModulators(boolean selected) {
            for (int i = 0; i < enabled.length; i++) {
                enableModulator(i, selected);
            }
        }

        private void enableModulator(int rowIndex, boolean enable) {
            enabled[rowIndex] = enable;
            DSGeneMarker mod = getModulatorForIndex(rowIndex);
            if (enabled[rowIndex]) {
                aggregateModel.enableModulator(mod);
                modTargetModel.enableModulator(mod);
                List<DSGeneMarker> t = aggregateModel.getActiveTargets();
                for(int i = 0; i < t.size(); i++){
                	modTargetModel.enableTarget(t.get(i));
                }
                ModulatorListModel model = (ModulatorListModel) heatMapModNameList.getModel();
                model.refresh();
            } else {
                aggregateModel.disableModulator(mod);
                modTargetModel.disableModulator(mod);
                ModulatorListModel model = (ModulatorListModel) heatMapModNameList.getModel();
                model.refresh();
            }
            if(this.getNumberOfModulatorsSelected() > 0){
            	tabs.setEnabledAt(1, true);
            	tabs.setEnabledAt(2, true);
            	tabs.setEnabledAt(3, true);
            	
            	rebuildHeatMap(heatmap.getTargetLimits());
            	
            } else {
            	tabs.setEnabledAt(1, false);
            	tabs.setEnabledAt(2, false);
            	tabs.setEnabledAt(3, false);
            }
        }
        
        /**
         * Get the number of modulator that has been selected.
         * 
         * @return number of modulator selected
         */
        public int getNumberOfModulatorsSelected(){
        	int result = 0;
        	for(int i = 0; i < enabled.length; i++){
        		if(enabled[i] == true) result++;
        	}
        	return result;
        }
        
        /**
         * Get the list of user selected modulators.
         * 
         * @return the list of selected modulators
         */
        public List<DSGeneMarker> getSelectedModulators(){
        	List<DSGeneMarker> result = new ArrayList<DSGeneMarker>();
        	for(int i = 0; i < enabled.length; i++){
        		if(enabled[i] == true){
        			result.add(this.getModulatorForIndex(i));
        		}
        	}
        	return result;
        }
        
        /**
         * Handles table column sorting for the modulator table.
         * @param col - the column index of the column to sort
         * @param ascending - if true, sort the column in ascending order.  
         * Otherwise, sort in descending order.
         */
        public void sort(int col, boolean ascending){
        	if(col == 0) return;
        	List<DSGeneMarker> mods = this.modulators;
        	if (!globalSelectionState.allMarkerOverride) 
        		mods = this.limitedModulators;
        	if(col == 1){   	
        		Collections.sort(mods, new GeneMarkerListComparator(mindyData, GeneMarkerListComparator.SHORT_NAME, ascending));
        	}
        	if(col == 2){  
        		Collections.sort(mods, new GeneMarkerListComparator(mindyData, GeneMarkerListComparator.M_POUND, ascending));
        	}
        	if(col == 3){    
        		Collections.sort(mods, new GeneMarkerListComparator(mindyData, GeneMarkerListComparator.M_PLUS, ascending));
        	}
        	if(col == 4){     
        		Collections.sort(mods, new GeneMarkerListComparator(mindyData, GeneMarkerListComparator.M_MINUS, ascending));
        	}
        	if(col == 5){
        		Collections.sort(mods, new GeneMarkerListComparator(mindyData, GeneMarkerListComparator.MODE, ascending));
        	}
        	if(col == 6){       
        		Collections.sort(mods, new GeneMarkerListComparator(mindyData, GeneMarkerListComparator.DESCRIPTION, ascending));
        	}
        	if (!globalSelectionState.allMarkerOverride)
        		limitedModulators = mods;
        	else
        		modulators = mods;
        	enabled = new boolean[modulators.size()];
        	for(int i = 0; i < enabled.length; i++){
        		enabled[i] = false;
        		this.enableModulator(i, false);
        	}
        	numModSelectedInModTab.setText(NUM_MOD_SELECTED_LABEL + "0");
        	numModSelectedInModTab.repaint();
        	selectAll.setSelected(false);
    		fireTableStructureChanged();
    		
        }
        
        /**
         * Check to see if the modulator table should display probe names or gene names.
         * @return If true, the modulator table displays probe names.  
         * If not, the modulator table displays gene names.
         */
        public boolean isShowProbeName(){
        	return this.showProbeName;
        }
        
        /**
         * Specify whether or not the modulator table should display probe names or gene names.
         * @param showProbeName - if true, the modulator table displays probe names.  
         * If not, the modulator table displays gene names.
         */
        public void setShowProbeName(boolean showProbeName){
        	this.showProbeName = showProbeName;
        }
    }
    
    /**
     * For rendering modulator checkboxes on the targets table column headers.
     * 
     * @author ch2514
     * @version $Id: MindyPlugin.java,v 1.47 2007-07-30 17:05:09 hungc Exp $
     */
    private class CheckBoxRenderer extends DefaultTableCellRenderer {
    	/**
    	 * Specifies how to render targets table column headers.
    	 * 
    	 * @param table - targets table
    	 * @param value - the value of the cell to be rendered
    	 * @param isSelected - true if the cell is to be rendered with the selection highlighted; otherwise false
    	 * @param hasFocus - true if the header cell has focus, and false otherwise
    	 * @param row - the row index of the cell being drawn. When drawing the header, the value of row is -1
    	 * @param column - the column index of the cell being drawn
    	 * @return 
    	 */
        public Component getTableCellRendererComponent(JTable table, 
                                                       Object value,
                                                       boolean isSelected, 
                                                       boolean hasFocus,
                                                       int row, 
                                                       int column) {
        	Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        	Object o = table.getModel();
        	if(o instanceof AggregateTableModel){
        		AggregateTableModel atm = (AggregateTableModel) o;
        		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        		boxes = new JCheckBox[table.getColumnCount()];
	        	if(column == 0){ 
	        		JPanel blank = new JPanel();
	        		JLabel blankLabel = new JLabel(" ");
	        		blankLabel.setBackground(c.getBackground());
	        		blank.setBorder(loweredetched);
	        		blank.add(blankLabel);
	        		blank.setMaximumSize(new Dimension((int) blank.getSize().getWidth(), 10));
	        		return blank;
	        	} else if (column == 1) {
	        		JLabel jl = new JLabel(atm.getColumnName(column), SwingConstants.LEFT);
	        		jl.setBackground(c.getBackground());
	        		JPanel blank = new JPanel();
	        		blank.setBorder(loweredetched);
	        		blank.add(jl);
	        		blank.setMaximumSize(new Dimension((int) blank.getSize().getWidth(), 10));
	        		return blank;
	        	} else if(column < boxes.length){
		            boxes[column] = new JCheckBox();
		            boxes[column].setEnabled(true);
		            if(column < atm.getNumberOfModulatorCheckBoxes())
		            	boxes[column].setSelected(atm.getModulatorCheckBoxState(column));
		            else 
		            	log.error("column [" + column + "] does not have a corresponding checkbox state.");
		            JLabel jl = new JLabel("  " + atm.getColumnName(column));
		            jl.setBackground(c.getBackground());
		            JPanel p = new JPanel(new BorderLayout());
		            p.setBorder(loweredetched);
		            if(selectionEnabledCheckBoxTarget.isSelected())
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
     * @version $Id: MindyPlugin.java,v 1.47 2007-07-30 17:05:09 hungc Exp $
     */
    private class AggregateTableModel extends DefaultTableModel {

        private static final int EXTRA_COLS = 2;

        private boolean[] checkedTargets;
        private boolean[] checkedModulators;
        private List<DSGeneMarker> allModulators;
        private List<DSGeneMarker> enabledModulators;
        private List<DSGeneMarker> activeTargets;
        private List<DSGeneMarker> limitedTargets;
        private MindyData mindyData;
        private boolean scoreView = false;
        private boolean modulatorsLimited = false;
        private int modLimit = DEFAULT_MODULATOR_LIMIT;
        private ModulatorSort modulatorSortMethod = ModulatorSort.Aggregate;
        private boolean[] ascendSortStates;
        private boolean showProbeName = true;

        /**
         * Constructor.
         * @param mindyData
         */
        public AggregateTableModel(MindyData mindyData) {
            this.checkedTargets = new boolean[mindyData.getData().size()];
            this.mindyData = mindyData;
            allModulators = mindyData.getModulators();
            enabledModulators = new ArrayList<DSGeneMarker>();
            activeTargets = new ArrayList<DSGeneMarker>();
            ascendSortStates =  new boolean[allModulators.size() + AggregateTableModel.EXTRA_COLS];
            this.checkedModulators = new boolean[this.allModulators.size() + AggregateTableModel.EXTRA_COLS];
        }

        /**
         * Whether the targets table shows the actual scores or just -1, 0, and 1.
         * @return true if the table is to show the actual scores, and false otherwise
         */
        public boolean isScoreView() {
            return scoreView;
        }

        /**
         * Set shether the targets table shows the actual scores or just -1, 0, and 1.
         * @param scoreView - true if the table is to show the actual scores, and false otherwise
         */
        public void setScoreView(boolean scoreView) {
            this.scoreView = scoreView;
        }

        /**
         * Get the modulator sort methods for targets table.
         * 
         * @return the ModulatorSort object representing the sorting scheme for the table columns
         */
        public ModulatorSort getModulatorSortMethod() {
            return modulatorSortMethod;
        }

        /**
         * Set the sort scheme for modulators in the targets table.
         * 
         * @param modulatorSortMethod - ModulatorSort object that specifies how to sort table columns
         */
        public void setModulatorSortMethod(ModulatorSort modulatorSortMethod) {
            this.modulatorSortMethod = modulatorSortMethod;
            resortModulators();
        }

        /**
         * Whether or not to display only the top modulator(s) in the targets table.
         * 
         * @return true if to limit display to the specified number of 
         * top modulator(s), and false otherwise.
         */
        public boolean isModulatorsLimited() {
            return modulatorsLimited;
        }

        /**
         * Set whether or not to display only the top modulator(s) in the targets table.
         * 
         * @param modulatorsLimited - true if to limit display to the specified number of 
         * top modulator(s), and false otherwise.
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
         * @param modLimit - number of top modulator(s) to display
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
         * 
         * @param enabledModulators - list of enabled modulators
         */
        public void setEnabledModulators(List<DSGeneMarker> enabledModulators) {
            this.enabledModulators = enabledModulators;
            this.checkedModulators = new boolean[this.enabledModulators.size() + AggregateTableModel.EXTRA_COLS];
            this.ascendSortStates = new boolean[this.enabledModulators.size() + AggregateTableModel.EXTRA_COLS];
            MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
            repaint();
        }

        /**
         * Get the list of selected targets.
         * 
         * @return list of selected targets
         */
        public List<DSGeneMarker> getCheckedTargets() {
            ArrayList<DSGeneMarker> result = new ArrayList<DSGeneMarker>(checkedTargets.length);
            for (int i = 0; i < checkedTargets.length; i++) {
                if (checkedTargets[i]) {
                    result.add(activeTargets.get(i));
                }
            }
            result.trimToSize();
            return result;
        }
        
        /**
         * Get the list of selected modulators.
         * 
         * @return list of selected modulators
         */
        public List<DSGeneMarker> getCheckedModulators(){
        	ArrayList<DSGeneMarker> result = new ArrayList<DSGeneMarker>(checkedModulators.length);
        	for(int i = AggregateTableModel.EXTRA_COLS; i < checkedModulators.length; i++){
        		if(checkedModulators[i]){
        			result.add(enabledModulators.get(i - AggregateTableModel.EXTRA_COLS));
        		}
        	}
        	result.trimToSize();
        	return result;
        }

        /**
         * Enable the specified modulator.
         * 
         * @param mod - the modulator to enable
         */
        public void enableModulator(DSGeneMarker mod) {
            if (!enabledModulators.contains(mod)) {
                enabledModulators.add(mod);
                recalcActiveTargets();
                resortModulators();     // This also fires structure changed
                MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
                repaint();
            }
        }

        /**
         * Disable the specified modulator.
         * 
         * @param mod - the modulator to disable
         */
        public void disableModulator(DSGeneMarker mod) {
            enabledModulators.remove(mod);
            recalcActiveTargets();
            resortModulators();     // This also fires structure changed
            MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
            repaint();
        }

        private void recalcActiveTargets() {
            activeTargets.clear();
            if(targetAllMarkersCheckBox.isSelected()){
	            for (DSGeneMarker modMarker : enabledModulators) {
	                List<MindyData.MindyResultRow> rows = mindyData.getRows(modMarker, mindyData.getTranscriptionFactor());
	                for (MindyData.MindyResultRow row : rows) {
	                    DSGeneMarker target = row.getTarget();
	                    if (!activeTargets.contains(target)) {
	                        activeTargets.add(target);
	                    }
	                }
	            }
	            checkedTargets = new boolean[activeTargets.size()];
	            this.selectAllTargets(false);
            } else {
	            for(DSGeneMarker modMarker: enabledModulators){
	            	List<DSGeneMarker> targets = mindyData.getTargets(modMarker, mindyData.getTranscriptionFactor());
	            	List<DSGeneMarker> ts = targets;
	            	if((this.limitedTargets != null) && (this.limitedTargets.size() > 0))
	            		ts = this.limitedTargets;
	            	for(DSGeneMarker target: targets){
	            		if(ts.contains(target) && (!activeTargets.contains(target))){
	            			activeTargets.add(target);
	            		}
	            	}
	            }
	            checkedTargets = new boolean[activeTargets.size()];
	            this.selectAllTargets(false);
	            
            }
            fireTableDataChanged();
            MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
        }

        /**
         * Get the number of columns in the targets table.
         * @return the number of columns in the targets table
         */
        public int getColumnCount() {
            // Number of allModulators plus target name and checkbox column
            if (!modulatorsLimited) {
                return enabledModulators.size() + AggregateTableModel.EXTRA_COLS;
            } else {
                return Math.min(modLimit + AggregateTableModel.EXTRA_COLS, enabledModulators.size() + AggregateTableModel.EXTRA_COLS);
            }
        }
        
        // called from MindyVisualComponent
        // i.e. when SelectionPanel changes marker set selections via GeneSelectorEvent
        /**
         * Callback method for the targets table when the user changes marker set selections in the Selection Panel.
         * 
         * @param - list of selected markers
         */
        public void limitMarkers(List<DSGeneMarker> limitList) {
            if (limitList == null) {
                limitedTargets = null;
                log.debug("Cleared modulator and target limits.");
            } else {
            	limitedTargets = new ArrayList<DSGeneMarker>();
                for (DSGeneMarker marker : limitList) {
                	limitedTargets.add(marker);
                }
                log.debug("Limited list table to " + limitedTargets.size() + " targets.");
            }
            if (!targetAllMarkersCheckBox.isSelected()) {
            	recalcActiveTargets();
                fireTableDataChanged();
                MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
                repaint();
            }
        }
        
        // called from "All Markers" checkbox
        /**
         * Show only the markers selected in the marker sets on the Selector Panel.
         * Applies only to the targets table.
         */
        public void showLimitedMarkers(){
    		recalcActiveTargets();
        	fireTableDataChanged();
        	MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
            repaint();
        }
        
        // called from "All Markers" checkbox
        /**
         * Show all markers.
         * Applies only to the targets table.
         */
        public void showAllMarkers(){
        	recalcActiveTargets();
        	fireTableDataChanged();
        	MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
            repaint();
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
         * @param columnIndex - column index
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
         * @param rowIndex - row index of the cell
         * @param columnIndex - column index of the cell
         * @return the value object of specified table cell
         */
        public Object getValueAt(int row, int col) {
            if (col == 1) {
            	return getMarkerDisplayName(this, (DSGeneMarker) activeTargets.get(row));
            } else if (col == 0) {
                return checkedTargets[row];
            } else {
                float score = mindyData.getScore(enabledModulators.get(col - AggregateTableModel.EXTRA_COLS), mindyData.getTranscriptionFactor(), activeTargets.get(row));
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
         * @param row - row index of the table cell
         * @param col - col index of the table cell
         * @return the score of the modulator and target
         */
        public float getScoreAt(int row, int col) {
        	float score = mindyData.getScore(enabledModulators.get(col - AggregateTableModel.EXTRA_COLS), mindyData.getTranscriptionFactor(), activeTargets.get(row));
            return score;
        }

        /**
         * Set values of targets table cells.
         * 
         * @param aValue - value of the cell
         * @param rowIndex - row index of the cell
         * @param columnIndex - column index of the cell
         */
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        	if (columnIndex == 0) 
                checkedTargets[rowIndex] = (Boolean) aValue;
            
            selectionEnabledCheckBoxTarget.setText(MindyPlugin.ENABLE_SELECTION + " [" +  this.getUniqueCheckedTargetsAndModulators().size() + "]");
            selectionEnabledCheckBoxTarget.repaint();
            
        	if(this.getCheckedTargets().size() == this.getActiveTargets().size())
        		selectAllTargetsCheckBoxTarget.setSelected(true);
        	else
        		selectAllTargetsCheckBoxTarget.setSelected(false);
        	
        	MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
            targetTable.repaint();
        }

        /**
         * Get the specified table column name.
         * 
         * @param col - column index
         */
        public String getColumnName(int col) {
            if (col == 0) {
            	return " ";
            } else if (col == 1) {
                return "Target";
            } else {
                DSGeneMarker mod = enabledModulators.get(col - AggregateTableModel.EXTRA_COLS);
                String colName = getMarkerDisplayName(this, mod);
                if (modulatorSortMethod == ModulatorSort.Aggregate) {
                    colName += " (M# " + mindyData.getStatistics(mod).getCount() + ")";
                } else if (modulatorSortMethod == ModulatorSort.Enhancing) {
                    colName += " (M+ " + mindyData.getStatistics(mod).getMover() + ")";
                } else if (modulatorSortMethod == ModulatorSort.Negative) {
                    colName += " (M- " + mindyData.getStatistics(mod).getMunder() + ")";
                }
                return colName;
            }
        }

        /**
         * Sorts the columns on the targets table based on modulator stat (M#, M+. M-) selection.
         */
        public void resortModulators() {
            Collections.sort(enabledModulators, new ModulatorStatComparator(mindyData, modulatorSortMethod));
            this.clearModulatorSelections();
            fireTableStructureChanged();
            MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
        }
        
        /**
         * Clear all modulator selection from the targets table.
         */
        public void clearModulatorSelections(){
        	for(int i = 0; i < this.checkedModulators.length; i++)
            	this.checkedModulators[i] = false;
            fireTableStructureChanged();
        	MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
        }
        
        /**
         * Handles table column sorting for the targets table.
         * @param col - the column index of the column to sort
         * @param ascending - if true, sort the column in ascending order.  
         * Otherwise, sort in descending order.
         */
        public void sort(int col, boolean ascending){
        	if(col == 0)
        		return;
        	if(col == 1){
        		Collections.sort(this.activeTargets, new GeneMarkerListComparator(mindyData, GeneMarkerListComparator.SHORT_NAME, ascending));
        	} else {   
        		Collections.sort(this.activeTargets, new GeneMarkerListComparator(mindyData, enabledModulators.get(col - AggregateTableModel.EXTRA_COLS), GeneMarkerListComparator.SCORE, ascending));
        	}  
        	for(int i = 0; i < this.checkedTargets.length; i++){
        		this.checkedTargets[i] = false;
        	}
        	fireTableStructureChanged();
        	selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " [" + aggregateModel.getUniqueCheckedTargetsAndModulators().size() + "]");
        	selectionEnabledCheckBoxTarget.repaint();
        	selectAllTargetsCheckBoxTarget.setSelected(false);
        	MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
        }
        
        /**
         * The union of selected modulators and targets from the targets table.
         * 
         * @return the list of selected markers
         */
        public List<DSGeneMarker> getUniqueCheckedTargetsAndModulators(){
        	ArrayList<DSGeneMarker> result = new ArrayList<DSGeneMarker>(this.getCheckedTargets().size()+ this.getEnabledModulators().size());
        	for(int i = 0; i < this.getCheckedTargets().size(); i++){
        		DSGeneMarker m = (DSGeneMarker) this.getCheckedTargets().get(i);
        		if(!result.contains(m))
        			result.add(m);
        	}
        	for(int i = 0; i < this.getCheckedModulators().size(); i++){
        		DSGeneMarker m = (DSGeneMarker) this.getCheckedModulators().get(i);
        		if(!result.contains(m))
        			result.add(m);
        	}
        	result.trimToSize();
        	return result;
        }
        
        /**
         * Get the total number of markers (modulators and targets) selected in the targets table.
         * 
         * @return the total number of markers selected
         */
        public int getNumberOfMarkersSelected(){
        	return this.getUniqueCheckedTargetsAndModulators().size();
        }
        
        /**
         * Get the list of active targets.
         * 
         * @return list of active targets
         */
        public List<DSGeneMarker> getActiveTargets(){
        	return activeTargets;
        }
        
        /**
         * Get the sorting states (ascending or descending) of each column in the targets table.
         * 
         * @return a list of sorting states (ascending = true, descending = false)
         */
        public boolean[] getAscendSortStates(){
        	return this.ascendSortStates;
        }
        
        /**
         * Set the sorting states (ascending or descending) of each column in the targets table.
         * 
         * @param b - a list of sorting states (ascending = true, descending = false)
         */
        public void setAscendSortStates(boolean[] b){
        	this.ascendSortStates = b;
        }
        
        /**
         * Check to see if the targets table should display probe names or gene names.
         * 
         * @return If true, the targets table displays probe names.  
         * If not, the targets table displays gene names.
         */
        public boolean isShowProbeName(){
        	return this.showProbeName;
        }
        
        /**
         * Specify whether or not the targets table should display probe names or gene names.
         * 
         * @param showProbeName - if true, the targets table displays probe names.  
         * If not, the targets table displays gene names.
         */
        public void setShowProbeName(boolean showProbeName){
        	this.showProbeName = showProbeName;
        }
        
        /**
         * Whether or not the modulator from the specified colum index is selected.
         * 
         * @param index - table column index
         * @return true if the modulator represented by the column is selected, 
         * and false otherwise
         */
        public boolean getModulatorCheckBoxState(int index){
        	return this.checkedModulators[index];
        }
        
        /**
         * Set the modulator checkbox for the specified targets table column header.
         * 
         * @param index - column index of the interested header
         * @param b - true if the modulator at the specified index is selected,
         * and false otherwise
         */
        public void setModulatorCheckBoxState(int index, boolean b){
        	this.checkedModulators[index] = b;
        }
        
        /**
         * Get the number of modulator checkboxes from the table column headers.
         * 
         * @return the number of modulator checkboxes from the table column headers
         */
        public int getNumberOfModulatorCheckBoxes(){
        	return this.checkedModulators.length;
        }
        
        private void selectAllTargets(boolean select){
        	for (int i = 0; i < checkedTargets.length; i++) {
        		checkedTargets[i] = select;
            }
            this.fireTableDataChanged();
        	MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
        }
        
        private void selectAllModulators(boolean select){
        	int top = checkedModulators.length;
        	if((this.modulatorsLimited) && ((this.modLimit + AggregateTableModel.EXTRA_COLS) < top))
        		top = this.modLimit + AggregateTableModel.EXTRA_COLS;
        	for(int i = 0; i < top; i++){
        		checkedModulators[i] = select;
        	}
        	this.fireTableStructureChanged();
        	MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
        }
    }

    /**
     * Compare M#, M+, or M- of two gene markers (for sorting).
     * 
     * @author mhall
     * @version $Id: MindyPlugin.java,v 1.47 2007-07-30 17:05:09 hungc Exp $
     */
    private class ModulatorStatComparator implements Comparator<DSGeneMarker> {

        private MindyData data;
        private ModulatorSort sortType;

        /**
         * Constructor.
         * 
         * @param data - MINDY data
         * @param sortType - specifies whether to sort by M#, M+, or M-
         */
        public ModulatorStatComparator(MindyData data, ModulatorSort sortType) {
            this.data = data;
            this.sortType = sortType;
        }

        /**
         * Compare two gene markers based on M#, M+, or M-.
         * The choice is determined by sort type specified in the constructor.
         * 
         * @param dsGeneMarker - the first gene marker to be compared
	     * @param dsGeneMarker1 - the second gene marker to be compared
	     * @return A negative integer if the first gene marker precedes the second.
	     * Zero if the two markers are the same.  
	     * A positive integer if the second marker precedes the first.
         */
        public int compare(DSGeneMarker dsGeneMarker, DSGeneMarker dsGeneMarker1) {
            if (sortType == ModulatorSort.Aggregate) {
                return data.getStatistics(dsGeneMarker1).getCount() - data.getStatistics(dsGeneMarker).getCount();
            } else if (sortType == ModulatorSort.Enhancing) {
                return data.getStatistics(dsGeneMarker1).getMover() - data.getStatistics(dsGeneMarker).getMover();
            } else {
                return data.getStatistics(dsGeneMarker1).getMunder() - data.getStatistics(dsGeneMarker).getMunder();
            }
        }

    }

    /**
     * Table data model for the list table.
     * 
     * @author mhall
     * @author ch2514
     * @version $Id: MindyPlugin.java,v 1.47 2007-07-30 17:05:09 hungc Exp $
     */
    private class ModulatorTargetModel extends DefaultTableModel {

        private boolean[] modChecks;
        private boolean[] targetChecks;
        private ArrayList<DSGeneMarker> enabledModulators = new ArrayList<DSGeneMarker>();
        private ArrayList<DSGeneMarker> enabledTargets = new ArrayList<DSGeneMarker>();
        private List<DSGeneMarker> limitedModulators = new ArrayList<DSGeneMarker>();
        private ArrayList<DSGeneMarker> limitedTargets = new ArrayList<DSGeneMarker>();
        private MindyData mindyData;
        private String[] columnNames = new String[]{" ", "Modulator", "  ", "Target", "Score"};
        private ArrayList<MindyData.MindyResultRow> rows = new ArrayList<MindyData.MindyResultRow>();
        private boolean[] ascendSortStates;
        private boolean showProbeName = true;
        private boolean limMarkers = false;

        /**
         * Constructor.
         * 
         * @param mindyData - data for the MINDY component
         */
        public ModulatorTargetModel(MindyData mindyData) {
            this.modChecks = new boolean[mindyData.getData().size()];
            this.targetChecks = new boolean[mindyData.getData().size()];
            this.mindyData = mindyData;
            this.ascendSortStates = new boolean[columnNames.length];
            for(int i = 0; i < this.ascendSortStates.length; i++)
            	this.ascendSortStates[i] = true;
        }

        /**
         * Get the enabled modulators.
         * @return a list of enabled modulators
         */
        public ArrayList<DSGeneMarker> getEnabledModulators() {
            return enabledModulators;
        }

        /**
         * Set the list of enabled modulators.
         * @param enabledModulators - list of enabled modulators
         */
        public void setEnabledModulators(ArrayList<DSGeneMarker> enabledModulators) {
            this.enabledModulators = enabledModulators;
        }

        /**
         * Get a list of enabled targets.
         * @return a list of enabled targets.
         */
        public ArrayList<DSGeneMarker> getEnabledTargets() {
            return enabledTargets;
        }

        /**
         * Set the list of enabled targets.
         * @param enabledTargets - list of enabled targets
         */
        public void setEnabledTargets(ArrayList<DSGeneMarker> enabledTargets) {
            this.enabledTargets = enabledTargets;
        }

        /**
         * Enable a specified modulator
         * @param mod - the modulator to enable
         */
        public void enableModulator(DSGeneMarker mod) {
            if (!enabledModulators.contains(mod)) {
                enabledModulators.add(mod);
                recalculateRows();
                fireTableDataChanged();
                setListTableViewOptions();
                repaint();
            }
        }

        /**
         * Disable a specified modulator
         * @param mod - the modulator to disable
         */
        public void disableModulator(DSGeneMarker mod) {
            enabledModulators.remove(mod);
            recalculateRows();
            fireTableStructureChanged();
            setListTableViewOptions();
            repaint();
        }

        /**
         * Enable a specified target
         * @param target - the target to enable
         */
        public void enableTarget(DSGeneMarker target) {
            if (!enabledTargets.contains(target)) {
                enabledTargets.add(target);
                recalculateRows();
                fireTableDataChanged();
                setListTableViewOptions();
                repaint();
            }
        }

        /**
         * Disable a specified target
         * @param target - the target to disable
         */
        public void disableTarget(DSGeneMarker target) {
            enabledTargets.remove(target);
            recalculateRows();
            fireTableDataChanged();
            setListTableViewOptions();
            repaint();
        }

        private void recalculateRows() {
            rows.clear();
            if(listAllMarkersCheckBox.isSelected()){
	            for (DSGeneMarker modMarker : enabledModulators) {
	                List<DSGeneMarker> targets = mindyData.getTargets(modMarker, mindyData.getTranscriptionFactor());
	                for (DSGeneMarker target : targets) {
	                    if (enabledTargets.contains(target)) {
	                        rows.add(mindyData.getRow(modMarker, mindyData.getTranscriptionFactor(), target));
	                    }
	                }
	            }
	            modChecks = new boolean[rows.size()];
	            targetChecks = new boolean[rows.size()];
	            this.selectAllModulators(false);
	            this.selectAllTargets(false);
	            
            } else {
	            List<DSGeneMarker> mods = this.enabledModulators;
	            if((this.limitedModulators != null) && (this.limitedModulators.size() > 0))
	            	mods = this.limitedModulators;
	            for(DSGeneMarker modMarker: mods){
	            	List<DSGeneMarker> targets = mindyData.getTargets(modMarker, mindyData.getTranscriptionFactor());
	            	List<DSGeneMarker> ts = this.enabledTargets;
	            	if((this.limitedTargets != null) && (this.limitedTargets.size() > 0))
	            		ts = this.limitedTargets;
	            	for(DSGeneMarker target: targets){
	            		if(ts.contains(target)){
	            			rows.add(mindyData.getRow(modMarker, mindyData.getTranscriptionFactor(), target));
	            		}
	            	}
	            }
	            modChecks = new boolean[rows.size()];
	            targetChecks = new boolean[rows.size()];
	            this.selectAllModulators(false);
	            this.selectAllTargets(false);
            }
        }
        
        private void redistributeRows(List<DSGeneMarker> targets){
        	rows.clear();  
        	List<DSGeneMarker> mods = this.limitedModulators;
            if(!this.limMarkers)
            	mods = this.enabledModulators;    
            for(DSGeneMarker t: targets){
        		for(DSGeneMarker m: mods){
        			rows.add(mindyData.getRow(m, mindyData.getTranscriptionFactor(), (DSGeneMarker) t));
        		}
        	}
            modChecks = new boolean[rows.size()];
            targetChecks = new boolean[rows.size()];
            this.selectAllModulators(false);
            this.selectAllTargets(false);
        }
        
        /**
         * Get the number of columns in the list table.
         * @return the number of columns in the list table
         */
        public int getColumnCount() {
            return columnNames.length;
        }
        
        // called from MindyVisualComponent
        // i.e. when SelectionPanel changes marker set selections via GeneSelectorEvent
        /**
         * Callback method for the list table when the user changes marker set selections in the Selection Panel.
         * 
         * @param - list of selected markers
         */
        public void limitMarkers(List<DSGeneMarker> limitList) {
            if (limitList == null) {
                limitedModulators = null;
                limitedTargets = null;
                limMarkers = false;
                log.debug("Cleared modulator and target limits.");
            } else {
                limitedModulators = new ArrayList<DSGeneMarker>();
                limitedTargets = new ArrayList<DSGeneMarker>();
                limMarkers = true;
                for (DSGeneMarker marker : limitList) {
                    if (enabledModulators.contains(marker)) {
                        limitedModulators.add(marker);
                    }
                    if (enabledTargets.contains(marker)) {
                        limitedTargets.add(marker);
                    }
                }
                log.debug("Limited list table to " + limitedModulators.size() + " mods. and " + limitedTargets.size() + " targets.");
            }
            if (!listAllMarkersCheckBox.isSelected()) {
            	recalculateRows();
                fireTableDataChanged();
                setListTableViewOptions();
                repaint();
            }
        }
        
        // called from "All Markers" checkbox
        /**
         * Show only the markers selected in the marker sets on the Selector Panel.
         * Applies only to the list table.
         */
        public void showLimitedMarkers(){
        	this.limMarkers = true;
        	recalculateRows();
        	fireTableDataChanged();
            setListTableViewOptions();
            repaint();
        }
        
        // called from "All Markers" checkbox
        /**
         * Show all markers.
         * Applies only to the list table.
         */
        public void showAllMarkers(){
        	this.limMarkers = false;
        	recalculateRows();
        	fireTableDataChanged();
            setListTableViewOptions();
            repaint();
        }

        /**
         * Get the number of rows on the list table.
         * 
         * @return number of rows on the table
         */
        public int getRowCount() {
        	if ((listAllMarkersCheckBox != null) && listAllMarkersCheckBox.isSelected()){
	            if (modChecks == null) {
	                return 0;
	            } else {
	                return rows.size();
	            }
        	} else {
        		if(rows != null)
        			return rows.size();
        		else 
        			return 0;
            }
        }

        /**
         * Whether or not the specified list table cell is editable.
         * 
         * @param rowIndex - row index of the table cell
         * @prarm columnIndex - column index of the table cell
         * @return true if the table cell is editable, and false otherwise
         */
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if ((columnIndex == 0) ||(columnIndex == 2)) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Get the class object representing the specified table column.
         * 
         * @param columnIndex - column index
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
         * @param rowIndex - row index of the cell
         * @param columnIndex - column index of the cell
         * @return the value object of specified table cell
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
            	return modChecks[rowIndex];
            } else if (columnIndex == 1) {
            	return getMarkerDisplayName(this, rows.get(rowIndex).getModulator());
            } else if (columnIndex == 2) {
                return targetChecks[rowIndex];
            } else if (columnIndex == 3) {
                return getMarkerDisplayName(this, rows.get(rowIndex).getTarget());
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
         * @param aValue - value of the cell
         * @param rowIndex - row index of the cell
         * @param columnIndex - column index of the cell
         */
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
            	String marker = getMarkerDisplayName(this, rows.get(rowIndex).getModulator());
            	for(int i = 0; i < rows.size(); i++){
            		if(marker.equals(getMarkerDisplayName(this, rows.get(i).getModulator()).trim())){
            			modChecks[i] = (Boolean) aValue;
            		}
            	}
            	listTable.repaint();
            } else if (columnIndex == 2) {
            	String marker = getMarkerDisplayName(this, rows.get(rowIndex).getTarget()).trim();
            	for(int i = 0; i < rows.size(); i++){
            		if(marker.equals(getMarkerDisplayName(this, rows.get(i).getTarget()).trim())){
            			targetChecks[i] = (Boolean) aValue;
            		}
            	}
            	listTable.repaint();
            }
            
        	selectionEnabledCheckBox.setText(MindyPlugin.ENABLE_SELECTION + " [" + this.getUniqueSelectedMarkers().size() + "]");
        	selectionEnabledCheckBox.repaint();
        	
        	if(this.getSelectedModulators().size() == this.getEnabledModulators().size())
        		selectAllModsCheckBox.setSelected(true);
        	else 
        		selectAllModsCheckBox.setSelected(false);
        	if(this.getSelectedTargets().size() == this.getEnabledTargets().size())
        		selectAllTargetsCheckBox.setSelected(true);
        	else
        		selectAllTargetsCheckBox.setSelected(false);        	
        }

        /**
         * Get the column name of the specified column index.
         * 
         * @param columnIndex - index of the column
         * @return name of the column
         */
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        private void selectAllModulators(boolean select) {
            for (int i = 0; i < modChecks.length; i++) {
                modChecks[i] = select;
            }
            if(!select){
            	selectionEnabledCheckBox.setText(MindyPlugin.ENABLE_SELECTION + " [" + this.getUniqueSelectedMarkers().size() + "]");
            	selectionEnabledCheckBox.repaint();
            	selectAllModsCheckBox.setSelected(false);
            }
            listTable.repaint();
        }
        
        /**
         * Get the sorting states (ascending or descending) of each column in the list table.
         * 
         * @return a list of sorting states (ascending = true, descending = false)
         */
        public boolean[] getAscendSortStates(){
        	return this.ascendSortStates;
        }
        /**
         * Set the sorting states (ascending or descending) of each column in the list table.
         * 
         * @param states - a list of sorting states (ascending = true, descending = false)
         */
        public void setAscendSortStates(boolean[] states){
        	this.ascendSortStates = states;
        }

        private void selectAllTargets(boolean select) {
            for (int i = 0; i < targetChecks.length; i++) {
                targetChecks[i] = select;
            }
            if(!select){
            	selectionEnabledCheckBox.setText(MindyPlugin.ENABLE_SELECTION + " [" + this.getUniqueSelectedMarkers().size() + "]");
            	selectionEnabledCheckBox.repaint();
            	selectAllTargetsCheckBox.setSelected(false);
            }
            listTable.repaint();
        }
        
        /**
         * Get the list of user selected modulators.
         * @return the list of selected modulators
         */
        public List<DSGeneMarker> getSelectedModulators(){
        	ArrayList<DSGeneMarker> result = new ArrayList<DSGeneMarker>();
        	for(int i = 0; i < modChecks.length; i++){
        		if(modChecks[i] == true){
        			DSGeneMarker m = rows.get(i).getModulator();
        			if(!result.contains(m)) result.add(m);
        		}
        	}
        	result.trimToSize();
        	return result;
        }
        
        /**
         * Get the list of user selected targets.
         * @return the list of selected targets
         */
        public List<DSGeneMarker> getSelectedTargets(){
        	ArrayList<DSGeneMarker> result = new ArrayList<DSGeneMarker>();
        	for(int i = 0; i < targetChecks.length; i++){
        		if(targetChecks[i] == true){
        			DSGeneMarker m = rows.get(i).getTarget();
        			if(!result.contains(m)) result.add(m);
        		}
        	}
        	result.trimToSize();
        	return result;
        }
        
        /**
         * Get the union of selected modulators and targets for the list table.
         * @return the union of selected modulators and targets
         */
        public List<DSGeneMarker> getUniqueSelectedMarkers(){
        	ArrayList<DSGeneMarker> result = new ArrayList<DSGeneMarker>();
        	List<DSGeneMarker> mods = this.getSelectedModulators();
        	List<DSGeneMarker> targets = this.getSelectedTargets();
        	for(int i = 0; i < mods.size(); i++){
        		DSGeneMarker m = (DSGeneMarker) mods.get(i);
        		if(!result.contains(m)) result.add(m);
        	}
        	for(int i = 0; i < targets.size(); i++){
        		DSGeneMarker m = (DSGeneMarker) targets.get(i);
        		if(!result.contains(m)) result.add(m);
        	}
        	result.trimToSize();
        	return result;
        }
        
        /**
         * Handles table column sorting for the list table.
         * @param col - the column index of the column to sort
         * @param ascending - if true, sort the column in ascending order.  
         * Otherwise, sort in descending order.
         */
        public void sort(int col, boolean ascending){
        	if((col == 0) || (col == 2)) return;
        	if(col == 1){
        		ArrayList<DSGeneMarker> mods = this.enabledModulators;
        		if(this.limMarkers) mods = (ArrayList<DSGeneMarker>) this.limitedModulators;
        		Collections.sort(mods, new GeneMarkerListComparator(mindyData, GeneMarkerListComparator.SHORT_NAME, ascending));
        		if(!this.limMarkers){
        			this.enabledModulators = mods;
        		} else {
        			this.limitedModulators = mods;
        		}
        		this.recalculateRows();
        	}
        	if(col == 3){
        		ArrayList<DSGeneMarker> mods = this.enabledTargets;
        		// ch2514
        		if(this.limMarkers) mods = this.limitedTargets;
        		Collections.sort(mods, new GeneMarkerListComparator(mindyData, GeneMarkerListComparator.SHORT_NAME, ascending));
        		// ch2514
        		if(!this.limMarkers){
        			this.enabledTargets = mods;
        			this.redistributeRows(this.enabledTargets);
        		} else {
        			this.limitedTargets = mods;
        			this.redistributeRows(this.limitedTargets);
        		}
           	}
        	if(col == 4){
        		Collections.sort(rows, new MindyRowComparator(MindyRowComparator.SCORE, ascending));
        		modChecks = new boolean[rows.size()];
	            targetChecks = new boolean[rows.size()];
	            this.selectAllModulators(false);
	            this.selectAllTargets(false);
        	}        	
        	fireTableStructureChanged();
        }
        
        /**
         * Check to see if the list table should display probe names or gene names.
         * @return If true, the list table displays probe names.  
         * If not, the list table displays gene names.
         */
        public boolean isShowProbeName(){
        	return this.showProbeName;
        }
        
        /**
         * Specify whether or not the list table should display probe names or gene names.
         * @param showProbeName - if true, the list table displays probe names.  
         * If not, the list table displays gene names.
         */
        public void setShowProbeName(boolean showProbeName){
        	this.showProbeName = showProbeName;
        }

    }
    
    /**
     * Heat map data model.
     * 
     * @author mhall
     * @version $Id: MindyPlugin.java,v 1.47 2007-07-30 17:05:09 hungc Exp $
     */
    private class ModulatorListModel extends AbstractListModel {
    	/**
    	 * Get the number of enabled modulators.
    	 * @return number of enabled modulators
    	 */
        public int getSize() {
            return modTargetModel.getEnabledModulators().size();
        }

        /**
         * Get the modulator specified by the index.
         * 
         * @param i - index
         * @return Modulator marker name of the enabled modulators in the heat map as specified by index i.
         */
        public Object getElementAt(int i) {
        	return getMarkerDisplayName(modTargetModel, modTargetModel.getEnabledModulators().get(i));
        }

        /**
         * Refreshes the data model.
         */
        public void refresh() {
            fireContentsChanged(this, 0, getSize());
        }
    }

    /**
     * State of selections and overrides for the entire component
     * 
     * @author mhall
     * @version $ID$
     */
    private class MarkerLimitState {
    	/**
    	 * Represents the markers in the marker sets selected by the user.
    	 */
        public List<DSGeneMarker> globalUserSelection = new ArrayList<DSGeneMarker>();
        /**
         * Whether the user has made a global "All Markers On" selection
         */
        public boolean allMarkerOverride = true;
    }
 
    /**
     * Handles column sorting in MINDY tables.
     * Also handles modulator selection for the targets table.
     * 
     * @author ch2514
     * @version $Id: MindyPlugin.java,v 1.47 2007-07-30 17:05:09 hungc Exp $
     */
    private class ColumnHeaderListener extends MouseAdapter {
    	/**
    	 * Handles mouse clicks on table column headers.
    	 * 
    	 * @param evt - MouseEvent
    	 */
        public void mouseClicked(MouseEvent evt) {
            JTable table = ((JTableHeader)evt.getSource()).getTable();
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
            Rectangle headerRect = table.getTableHeader().getHeaderRect(vColIndex);
            if (vColIndex == 0) {
                headerRect.width -= 3;    // Hard-coded constant
            } else {
                headerRect.grow(-3, 0);   // Hard-coded constant
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
            
            if(model instanceof ModulatorModel){
            	// sort
            	ModulatorModel mm = (ModulatorModel) model;
            	boolean[] states = mm.getAscendSortStates();
            	if(mColIndex < states.length){
            		boolean tmp = states[mColIndex];
            		states[mColIndex] = !tmp;
            		mm.sort(mColIndex, states[mColIndex]);
            	}
            	
            }
            if(model instanceof AggregateTableModel){
            	AggregateTableModel atm = (AggregateTableModel) model;
            	boolean clickedCheckbox = false;
            	// checkbox
            	if((selectionEnabledCheckBoxTarget.isSelected()) 
            			&& (mColIndex >= 2) 
            			&& (evt.getX() >= headerRect.getX()) 
            			&& (evt.getX() <= (headerRect.getX() + 15))
            			){
            		clickedCheckbox = true;
                	JCheckBox cb = MindyPlugin.this.getHeaderCheckBoxes()[mColIndex];
                	if((cb != null) && (mColIndex < atm.getNumberOfModulatorCheckBoxes())){
                		boolean tmp = atm.getModulatorCheckBoxState(mColIndex);
                		atm.setModulatorCheckBoxState(mColIndex, !tmp);
                		cb.setSelected(atm.getModulatorCheckBoxState(mColIndex));
                		atm.fireTableStructureChanged();
                		selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " [" + atm.getUniqueCheckedTargetsAndModulators().size() + "]");
                		selectionEnabledCheckBoxTarget.repaint();
                        
                    	if(atm.getCheckedModulators().size() == atm.getEnabledModulators().size())
                    		selectAllModsCheckBoxTarget.setSelected(true);
                    	else
                    		selectAllModsCheckBoxTarget.setSelected(false);
                		MindyPlugin.this.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget.isSelected());
                	}         
                	if(mColIndex >= atm.getNumberOfModulatorCheckBoxes())
                		log.error("check box index [" + mColIndex + "] not in check box state");
                }
            	
            	// sort
            	if((mColIndex == 1) || ((mColIndex >= 2) && (!clickedCheckbox))){
	            	boolean[] states = atm.getAscendSortStates();
	            	if(mColIndex < states.length){
	            		boolean tmp = states[mColIndex];
	            		states[mColIndex] = !tmp;
	            		atm.sort(mColIndex, states[mColIndex]);
	    			}
            	}
            }
            if(model instanceof ModulatorTargetModel){   
            	// sort
            	ModulatorTargetModel mtm = (ModulatorTargetModel) model;
            	boolean[] states = mtm.getAscendSortStates();
            	if(mColIndex < states.length){
            		boolean tmp = states[mColIndex];
            		states[mColIndex] = !tmp;
            		mtm.sort(mColIndex, states[mColIndex]);
            	}
            }
        }
    }
}
