package org.geworkbench.components.mindy;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.events.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatisticsImpl;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import com.solarmetric.ide.ui.CheckboxCellRenderer;

/**
 * @author mhall
 */
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

    private JCheckBox selectionEnabledCheckBox, selectionEnabledCheckBoxTarget;
    private JList heatMapModNameList;

    private JScrollPane heatMapScrollPane;
    private List<DSGeneMarker> modulators;
    private List<DSGeneMarker> transFactors;
    private MindyData mindyData;
    private JTable modTable, listTable, targetTable;
    private JCheckBox selectAll, selectAllModsCheckBox, selectAllModsCheckBoxTarget;
    private JCheckBox selectAllTargetsCheckBox, selectAllTargetsCheckBoxTarget;
    private JButton addToSetButton /*list tab*/, addToSetButtonTarget, addToSetButtonMod;
    private JLabel numModSelectedInModTab, numModSelectedInTableTab, numModSelectedInListTab;

    // Contains the state of selections passed in from the marker panel and overrides via All Markers checkboxes
    private MarkerLimitState globalSelectionState = new MarkerLimitState();

    public MindyPlugin(MindyData data, final MindyVisualComponent visualPlugin) {
        this.mindyData = data;
//        log.debug("Doing mean variance...");
//        logNormalize(data.getArraySet());
//        markerMeanVariance(data.getArraySet());
//        log.debug("Done.");
        modulators = mindyData.getModulators();


        JTabbedPane tabs = new JTabbedPane();
        {
            // Modulator Table
            JPanel panel = new JPanel(new BorderLayout(10, 10));

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
            

            panel.add(scrollPane, BorderLayout.CENTER);
            
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
            
            JPanel taskContainer = new JPanel(new GridLayout(4, 1, 10, 10));
            taskContainer.add(ll);
            taskContainer.add(selectAll);
            taskContainer.add(numModSelectedInModTab);
            taskContainer.add(addToSetButtonMod);
            JPanel p = new JPanel(new BorderLayout());
            p.add(taskContainer, BorderLayout.NORTH);
            panel.add(p, BorderLayout.WEST);

            tabs.add("Modulator", panel);
        }

        {
            // Modulator / Target Table
            Panel panel = new Panel(new BorderLayout(10, 10));

            JLabel ls = new JLabel("Sorting", SwingConstants.LEFT);
            ls.setFont(new Font(ls.getFont().getName(), Font.BOLD, 12));
            ls.setForeground(Color.BLUE);
            
            JComboBox sortOptions = new JComboBox();
            sortOptions.setEnabled(true);
            
            sortOptions.addItem(ModulatorSort.Aggregate);
            sortOptions.addItem(ModulatorSort.Enhancing);
            sortOptions.addItem(ModulatorSort.Negative);
           
            sortOptions.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                	System.out.println("combo box action!!");
                	
                    JComboBox cb = (JComboBox) actionEvent.getSource();
                    ModulatorSort selected = (ModulatorSort) cb.getSelectedItem();
                    if (selected == ModulatorSort.Aggregate) {
                        log.debug("Setting sort to Aggregate");
                        aggregateModel.setModulatorSortMethod(ModulatorSort.Aggregate);
                    } else if (selected == ModulatorSort.Enhancing) {
                        log.debug("Setting sort to Enhacing");
                        aggregateModel.setModulatorSortMethod(ModulatorSort.Enhancing);
                    } else {
                        log.debug("Setting sort to Negative");
                        aggregateModel.setModulatorSortMethod(ModulatorSort.Negative);
                    }
                }
            });

            Panel limitControls = new Panel(new BorderLayout());
            JLabel lm = new JLabel("Modulator Limits", SwingConstants.LEFT);
            lm.setFont(new Font(lm.getFont().getName(), Font.BOLD, 12));
            lm.setForeground(Color.BLUE);
            final JCheckBox modulatorLimits = new JCheckBox("Limit To Top");
            modulatorLimits.setSelected(true);
            limitControls.add(modulatorLimits, BorderLayout.WEST);
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(DEFAULT_MODULATOR_LIMIT, 1, 1000, 1);
            final JSpinner modLimitValue = new JSpinner(spinnerModel);
            limitControls.add(modLimitValue, BorderLayout.EAST);

            JLabel ld = new JLabel("Display Options", SwingConstants.LEFT);
            ld.setFont(new Font(ld.getFont().getName(), Font.BOLD, 12));
            ld.setForeground(Color.BLUE);
            final JCheckBox colorCheck = new JCheckBox("Color View");
            final JCheckBox scoreCheck = new JCheckBox("Score View");
            
            JLabel lmp = new JLabel("Marker Panel Override  ", SwingConstants.LEFT);
            lmp.setFont(new Font(lmp.getFont().getName(), Font.BOLD, 12));
            lmp.setForeground(Color.BLUE);
            final JCheckBox allMarkersCheckBox = new JCheckBox("All Markers");
            allMarkersCheckBox.setSelected(true);

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
                            component.setBackground(gradient.getColor(score));
                            //display red/blue only
                            //component.setBackground(gradient.getColor(Math.signum(score) * 1));
                        }
                    }
                    return component;
                }
            };
            //targetTable.setAutoCreateColumnsFromModel(true);
            JScrollPane scrollPane = new JScrollPane(targetTable);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            restoreBooleanRenderers(targetTable);
            targetTable.getColumnModel().getColumn(0).setMaxWidth(15);
            targetTable.getColumnModel().getColumn(0).setWidth(15);
            targetTable.getColumnModel().getColumn(0).setPreferredWidth(15);

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
            
            selectionEnabledCheckBoxTarget = new JCheckBox(ENABLE_SELECTION);
            selectionEnabledCheckBoxTarget.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    boolean selected = selectionEnabledCheckBoxTarget.isSelected();
                    log.debug("Setting test box visibility to " + selected);
                    setTargetCheckboxesVisibility(selected);
                    setTargetControlVisibility(selected);
                    setTargetTableViewOptions();
                }
            });
            
            selectAllModsCheckBoxTarget = new JCheckBox("Select All Modulators");
            selectAllModsCheckBoxTarget.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    //ModulatorTargetModel model = (ModulatorTargetModel) listTable.getModel();
                    //model.selectAllModulators(selectAllModsCheckBox.isSelected());
                }
            });
            selectAllTargetsCheckBoxTarget = new JCheckBox("Select All Targets");
            selectAllTargetsCheckBoxTarget.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                	AggregateTableModel model = (AggregateTableModel) targetTable.getModel();
                	model.selectAllTargets(selectAllTargetsCheckBoxTarget.isSelected());
                	if(selectAllTargetsCheckBoxTarget.isSelected()){
                		selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " [" + model.getActiveTargets().size() + "]");
                	} else {
                		selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " [0]");
                	}
                }
            });

            addToSetButtonTarget = new JButton("Add To Set");
            addToSetButtonTarget.addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent actionEvent){
                	MindyPlugin.this.addToSet(((AggregateTableModel) targetTable.getModel()).getUniqueCheckedTargetsAndModulators(), visualPlugin);                	
            	}
            });
            
            allMarkersCheckBox.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		if(allMarkersCheckBox.isSelected()){
            			selectAllTargetsCheckBoxTarget.setSelected(false);
            			selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " [0]");
            			aggregateModel.showAllMarkers();
            		} else {
            			if((aggregateModel.limitedTargets == null) || (aggregateModel.limitedTargets.size() <= 0)){
            	        		JOptionPane.showMessageDialog(null, "No marker set has been selected.", WARNING, JOptionPane.WARNING_MESSAGE);
            	        		allMarkersCheckBox.setSelected(true);
            	        } else {
            	        	selectAllTargetsCheckBoxTarget.setSelected(false);
            	        	selectionEnabledCheckBoxTarget.setText(ENABLE_SELECTION + " [0]");
            	        	aggregateModel.showLimitedMarkers();
            	        }
            		}
            	}
            });
            
            setTargetControlVisibility(false);

            targetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            panel.add(scrollPane, BorderLayout.CENTER);

            JPanel taskContainer = new JPanel();
            taskContainer.setLayout(new GridLayout(13, 1, 10, 10));
            taskContainer.add(ls);
            taskContainer.add(sortOptions);
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
            taskContainer.add(allMarkersCheckBox);
            JPanel p = new JPanel(new BorderLayout());
            p.add(taskContainer, BorderLayout.NORTH);
            panel.add(p, BorderLayout.WEST);

            tabs.add("Table", panel);
        }

        {
            // Modulator / Target list

            JPanel panel = new JPanel(new BorderLayout(10, 10));

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
            listTable.setBackground(new Color(245, 245, 245));
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
            setListControlVisibility(false);

            l = new JLabel("Marker Panel Override", SwingConstants.LEFT);
            l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
            l.setForeground(Color.BLUE);
            final JCheckBox allMarkersCheckBox = new JCheckBox("All Markers"); 
            allMarkersCheckBox.setSelected(true);
            allMarkersCheckBox.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		if(allMarkersCheckBox.isSelected()){
            			modTargetModel.showAllMarkers();
            		} else {
            			if(((modTargetModel.limitedModulators == null) || (modTargetModel.limitedModulators.size() <= 0))
            	        		&& ((modTargetModel.limitedTargets == null) || (modTargetModel.limitedTargets.size() <= 0))	
            	        		){
            	        		JOptionPane.showMessageDialog(null, "No marker set has been selected.", WARNING, JOptionPane.WARNING_MESSAGE);
            	        		allMarkersCheckBox.setSelected(true);
            	        } else {
            	        	modTargetModel.showLimitedMarkers();
            	        }
            		}
            	}
            });

            JPanel p = new JPanel(new GridLayout(6, 1, 10, 10));
            p.add(l);
            p.add(selectionEnabledCheckBox);
            p.add(selectAllModsCheckBox);
            p.add(selectAllTargetsCheckBox);
            p.add(addToSetButton);
            p.add(allMarkersCheckBox);
            JPanel taskContainer = new JPanel();
            taskContainer.setLayout(new BorderLayout(10, 10));
            taskContainer.add(p, BorderLayout.NORTH);
            panel.add(taskContainer, BorderLayout.WEST);

            panel.add(scrollPane, BorderLayout.CENTER);

            tabs.add("List", panel);
        }

        {
            // Heat Map

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            // This is modulator just to give us something to generate the heat map with upon first running
            DSGeneMarker modulator = modulators.iterator().next();
            transFactors = mindyData.getTranscriptionFactors(modulator);
            heatmap = new ModulatorHeatMap(modulator, transFactors.iterator().next(), mindyData, null);
            heatMapScrollPane = new JScrollPane(heatmap);
            panel.add(heatMapScrollPane, BorderLayout.CENTER);

            JPanel transFacPane = new JPanel(new BorderLayout());
            JLabel l = new JLabel("Transcription Factor", SwingConstants.LEFT);
            l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
            l.setForeground(Color.BLUE);
            transFacPane.add(l, BorderLayout.NORTH);
            JLabel transFactorName = new JLabel(mindyData.getTranscriptionFactor().getShortName());
            transFacPane.add(transFactorName);

            JPanel modulatorPane = new JPanel(new BorderLayout());
            JTextField modFilterField = new JTextField(15);
            l = new JLabel("Modulators", SwingConstants.LEFT);
            l.setFont(new Font(l.getFont().getName(), Font.BOLD, 12));
            l.setForeground(Color.BLUE);
            modulatorPane.add(l, BorderLayout.NORTH);
            JLabel lmp = new JLabel("Marker Panel Override  ", SwingConstants.LEFT);
            lmp.setFont(new Font(lmp.getFont().getName(), Font.BOLD, 12));
            lmp.setForeground(Color.BLUE);
                        
            heatMapModNameList = new JList();
            heatMapModNameList.setFixedCellWidth(12);
            JScrollPane modListScrollPane = new JScrollPane(heatMapModNameList);
            heatMapModNameList.setModel(new ModulatorListModel());
            heatMapModNameList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent listSelectionEvent) {
                    rebuildHeatMap(null);
                }
            });
            AutoCompleteDecorator.decorate(heatMapModNameList, modFilterField);
            JButton refreshButton = new JButton("  Refresh HeatMap  ");
            refreshButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    rebuildHeatMap(null);
                }
            });
            JButton screenshotButton = new JButton("  Take Screenshot  ");
            screenshotButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    visualPlugin.createImageSnapshot(heatMapScrollPane.getViewport().getComponent(0));
                }
            });
            
            final JCheckBox allMarkersCheckBox = new JCheckBox("All Markers");
            allMarkersCheckBox.setSelected(true);
            allMarkersCheckBox.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent actionEvent){
            		heatmap.setAllMarkersOn(allMarkersCheckBox.isSelected());
            		if(allMarkersCheckBox.isSelected()){
            			rebuildHeatMap(null);
            		} else {
            			if((visualPlugin.getSelectedMarkers() == null) || (visualPlugin.getSelectedMarkers().size() <= 0)){
            				JOptionPane.showMessageDialog(null, "No marker set has been selected.", WARNING, JOptionPane.WARNING_MESSAGE);
        	        		allMarkersCheckBox.setSelected(true);
        	        		heatmap.setAllMarkersOn(true);
            			} else {        
            				if((modTargetModel.getEnabledModulators() == null) || (modTargetModel.getEnabledModulators().size() <= 0)){
            					JOptionPane.showMessageDialog(null, "No modulator(s) enabled!", WARNING, JOptionPane.WARNING_MESSAGE);
            					allMarkersCheckBox.setSelected(true);
            					heatmap.setAllMarkersOn(true);
            				} else {
            					rebuildHeatMap(visualPlugin.getSelectedMarkers());
            				}
            			}
            		}
            	}
            });
            
            JPanel p = new JPanel(new BorderLayout(10, 10));
            p.add(modFilterField, BorderLayout.NORTH);
            p.add(modListScrollPane, BorderLayout.CENTER);
            JPanel ps = new JPanel(new GridLayout(4, 1));
            ps.add(refreshButton);
            ps.add(screenshotButton);
            ps.add(lmp);
            ps.add(allMarkersCheckBox);
            p.add(ps, BorderLayout.SOUTH);
            modulatorPane.add(p, BorderLayout.CENTER);

            JPanel taskContainer = new JPanel();
            taskContainer.setLayout(new BorderLayout(10, 10));
            taskContainer.add(transFacPane, BorderLayout.NORTH);
            taskContainer.add(modulatorPane, BorderLayout.CENTER);
            JScrollPane modTransScroll = new JScrollPane(taskContainer);
            panel.add(modTransScroll, BorderLayout.WEST);
            tabs.add("Heat Map", panel);
        }

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    private void limitModulators(Integer modLimit, boolean selected, JTable table){
        aggregateModel.setModLimit(modLimit);
        aggregateModel.setModulatorsLimited(selected);
        aggregateModel.fireTableStructureChanged();
        table.getColumnModel().getColumn(0).setMaxWidth(15);
        table.getColumnModel().getColumn(0).setWidth(15);
        table.repaint();
    }

    private void rebuildHeatMap(List<DSGeneMarker> targetLimits) {
        int selectedIndex = heatMapModNameList.getSelectedIndex();
        DSGeneMarker modMarker;
        if(modTargetModel.getEnabledModulators() == null){
        	JOptionPane.showMessageDialog(this, "No modulators selected.", WARNING, JOptionPane.WARNING_MESSAGE);
        	return;
        }
        if (selectedIndex > 0) {
            modMarker = modTargetModel.getEnabledModulators().get(selectedIndex);
        } else {
            modMarker = modTargetModel.getEnabledModulators().get(0);
        }

        if (modMarker != null) {
        	if(targetLimits != null){
        		log.debug("Rebuilding heat map with limited markers");
        		setHeatMap(new ModulatorHeatMap(modMarker, mindyData.getTranscriptionFactor(), mindyData, targetLimits));
        	} else {
	            log.debug("Rebuilding heat map.");
	            //setHeatMap(new ModulatorHeatMap(modMarker, mindyData.getTranscriptionFactor(), mindyData, aggregateModel.getCheckedTargets()));
	            setHeatMap(new ModulatorHeatMap(modMarker, mindyData.getTranscriptionFactor(), mindyData, null));
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
        boolean selected = selectionEnabledCheckBox.isSelected();
        if (selected) {
        	targetTable.getColumnModel().getColumn(0).setMaxWidth(15);
        }
        setTargetCheckboxesVisibility(selected);
    }

    private void markerMeanVariance(DSMicroarraySet microarraySet) {
        int n = microarraySet.size();
        int m = microarraySet.getMarkers().size();
        for (int i = 0; i < m; i++) {
            SummaryStatistics stats = new SummaryStatisticsImpl();
            for (int j = 0; j < n; j++) {
                stats.addValue(microarraySet.getValue(i, j));
            }
            StatisticalSummary summary = stats.getSummary();
            double mean = summary.getMean();
            double sd = summary.getStandardDeviation();
            if (sd > 0) {
                for (int j = 0; j < n; j++) {
                    DSMicroarray array = (DSMicroarray) microarraySet.get(j);
                    DSMutableMarkerValue markerValue = array.getMarkerValue(i);
                    markerValue.setValue((markerValue.getValue() - mean) / sd);
//                    microarraySet.getMicroarray(j).getValues()[i] = (float) ((microarraySet.getMicroarray(j).getValues()[i] - mean) / sd);
                }
            } else {
                for (int j = 0; j < n; j++) {
                    DSMicroarray array = (DSMicroarray) microarraySet.get(j);
                    DSMutableMarkerValue markerValue = array.getMarkerValue(i);
                    markerValue.setValue(0);

//                    microarraySet.getMicroarray(j).getValues()[i] = 0f;
                }
            }
        }
    }

    private void logNormalize(DSMicroarraySet microarraySet) {
        double log2 = Math.log10(2);
        int n = microarraySet.size();
        int m = microarraySet.getMarkers().size();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                DSMicroarray array = (DSMicroarray) microarraySet.get(j);
                DSMutableMarkerValue markerValue = array.getMarkerValue(i);
                markerValue.setValue(Math.log10(markerValue.getValue()) / log2);
            }
        }

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

    public void limitMarkers(List<DSGeneMarker> markers) {
        globalSelectionState.globalUserSelection = markers;
        // modulators tab
        modulatorModel.limitMarkers(markers);
        
        // target table tab
        aggregateModel.limitMarkers(markers);
        
        // list table tab
        modTargetModel.limitMarkers(markers);
        
        // heat map
        if((!heatmap.getAllMarkersOn())
        		&& (markers != null)
        		&& (markers.size() > 0)
        		){
        	rebuildHeatMap(markers);
        }
    }

    private void setAllMarkersOverride(boolean allMarkers) {
        globalSelectionState.allMarkerOverride = allMarkers;
        aggregateModel.fireTableStructureChanged();
        modTargetModel.fireTableDataChanged();
        modulatorModel.fireTableDataChanged();
    }
    
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
        		// Create a subpanel anyway
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
    
    //  Regardless of sort order (ascending or descending), null values always appear last.
    // colIndex specifies a column in model.
    private void sortAllRowsBy(JTable table, int colIndex, boolean ascending) {
        Vector data = ((DefaultTableModel) table.getModel()).getDataVector();
        // sorting
        Collections.sort(data, new ColumnSorter(colIndex, ascending));
        
        // informing table of the change
        ((DefaultTableModel) table.getModel()).fireTableStructureChanged();
        //table.repaint();
    }
    
    
    /**
     * Models and support classes follow
     */

    private class ModulatorModel extends DefaultTableModel {

        private boolean[] enabled;
        private List<DSGeneMarker> modulators;
        private List<DSGeneMarker> limitedModulators = new ArrayList<DSGeneMarker>();
        private MindyData mindyData; 
        private String[] columnNames = new String[]{" ", "Modulator", " M# ", " M+ ", " M- ", " Mode ", "Modulator Description"};
        private boolean[] ascendSortStates;

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

        public int getColumnCount() {
            return columnNames.length;
        }

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

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return true;
            } else {
                return false;
            }
        }

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

        public Object getValueAt(int rowIndex, int columnIndex) {
            DSGeneMarker mod;
            mod = getModulatorForIndex(rowIndex);
            if (columnIndex == 0) {
                return enabled[rowIndex];
            } else if (columnIndex == 1) {
                return mod.getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
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

        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }
        
        public boolean[] getAscendSortStates(){
        	return this.ascendSortStates;
        }
        
        public void setAscendSortStates(boolean[] states){
        	this.ascendSortStates = states;
        }

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
                List<DSGeneMarker> t = aggregateModel.getActiveTargets();
                for(int i = 0; i < t.size(); i++){
                	modTargetModel.disableTarget(t.get(i));
                }
                ModulatorListModel model = (ModulatorListModel) heatMapModNameList.getModel();
                model.refresh();
            }
        }
        
        public int getNumberOfModulatorsSelected(){
        	int result = 0;
        	for(int i = 0; i < enabled.length; i++){
        		if(enabled[i] == true) result++;
        	}
        	return result;
        }
        
        public List<DSGeneMarker> getSelectedModulators(){
        	List<DSGeneMarker> result = new ArrayList<DSGeneMarker>();
        	for(int i = 0; i < enabled.length; i++){
        		if(enabled[i] == true){
        			result.add(this.getModulatorForIndex(i));
        		}
        	}
        	return result;
        }

    }
    
    

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
        private boolean allMarkersOn = true;

        public AggregateTableModel(MindyData mindyData) {
            this.checkedTargets = new boolean[mindyData.getData().size()];
            this.mindyData = mindyData;
            allModulators = mindyData.getModulators();
            enabledModulators = new ArrayList<DSGeneMarker>();
            activeTargets = new ArrayList<DSGeneMarker>();
        }

        public boolean isScoreView() {
            return scoreView;
        }

        public void setScoreView(boolean scoreView) {
            this.scoreView = scoreView;
        }

        public ModulatorSort getModulatorSortMethod() {
            return modulatorSortMethod;
        }

        public void setModulatorSortMethod(ModulatorSort modulatorSortMethod) {
            this.modulatorSortMethod = modulatorSortMethod;
            resortModulators();
        }

        public boolean isModulatorsLimited() {
            return modulatorsLimited;
        }

        public void setModulatorsLimited(boolean modulatorsLimited) {
            this.modulatorsLimited = modulatorsLimited;
        }

        public int getModLimit() {
            return modLimit;
        }

        public void setModLimit(int modLimit) {
            this.modLimit = modLimit;
        }

        public List<DSGeneMarker> getEnabledModulators() {
            return enabledModulators;
        }

        public void setEnabledModulators(List<DSGeneMarker> enabledModulators) {
            this.enabledModulators = enabledModulators;
            this.checkedModulators = new boolean[this.enabledModulators.size()];
            repaint();
        }

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
        
        public List<DSGeneMarker> getCheckedModulators(){
        	ArrayList<DSGeneMarker> result = new ArrayList<DSGeneMarker>(checkedModulators.length);
        	for(int i = 0; i < checkedModulators.length; i++){
        		if(checkedModulators[i]){
        			result.add(allModulators.get(i));
        		}
        	}
        	result.trimToSize();
        	return result;
        }

        public void enableModulator(DSGeneMarker mod) {
            if (!enabledModulators.contains(mod)) {
                enabledModulators.add(mod);
                recalcActiveTargets();
                resortModulators();     // This also fires structure changed
                repaint();
            }
        }

        public void disableModulator(DSGeneMarker mod) {
            enabledModulators.remove(mod);
            recalcActiveTargets();
            resortModulators();     // This also fires structure changed
            repaint();
        }

        private void recalcActiveTargets() {
            activeTargets.clear();
            if(this.allMarkersOn){
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
        }

        public int getColumnCount() {
            // Number of allModulators plus target name and checkbox column
            if (!modulatorsLimited) {
                return enabledModulators.size() + EXTRA_COLS;
            } else {
                return Math.min(modLimit + EXTRA_COLS, enabledModulators.size() + EXTRA_COLS);
            }
        }
        
        // called from MindyVisualComponent
        // i.e. when SelectionPanel changes marker set selections via GeneSelectorEvent
        public void limitMarkers(List<DSGeneMarker> limitList) {
            if (limitList == null) {
                limitedTargets = null;
                log.debug("Cleared modulator and target limits.");
            } else {
            	limitedTargets = new ArrayList<DSGeneMarker>();
                for (DSGeneMarker marker : limitList) {
                    //if (activeTargets.contains(marker)) {
                        limitedTargets.add(marker);
                    //}
                }
                log.debug("Limited list table to " + limitedTargets.size() + " targets.");
            }
            if (!this.allMarkersOn) {
            	recalcActiveTargets();
                fireTableDataChanged();
                //setTargetTableViewOptions();
                repaint();
            }
        }
        
        // called from "All Markers" checkbox
        public void showLimitedMarkers(){
    		//globalSelectionState.allMarkerOverride = false;
        	allMarkersOn = false;
    		recalcActiveTargets();
        	fireTableDataChanged();
        	//setTargetTableViewOptions();
            repaint();
        }
        
        // called from "All Markers" checkbox
        public void showAllMarkers(){
        	//globalSelectionState.allMarkerOverride = true;
        	allMarkersOn = true;
        	recalcActiveTargets();
        	fireTableDataChanged();
        	//setTargetTableViewOptions();
            repaint();
        }

        public int getRowCount() {
            if (activeTargets == null) {
                return 0;
            }
            return activeTargets.size();
        }
        
        public Class<?> getColumnClass(int i) {
            if (i == 0) {
            	return Boolean.class;
            } else if (i == 1) {                
                return String.class;
            } else {
                return Float.class;
            }
        }

  /*      public Object getValueAt(int row, int col) {
        	if ((col == 0) && (row == 0))
        		return null;
        	else if ((col == 0) && (row > 0)) {
            	return checkedTargets[row - 1];
            } else if((col > 1) && (row == 0)) {
            	return null;
            } else if ((col == 1) && (row > 0)) {                
                return activeTargets.get(row).getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            } else if(row > 0){
                float score = mindyData.getScore(enabledModulators.get(col - EXTRA_COLS), mindyData.getTranscriptionFactor(), activeTargets.get(row));
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
        	return null;
        }*/
        
        public Object getValueAt(int row, int col) {
            if (col == 1) {
                return activeTargets.get(row).getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            } else if (col == 0) {
                return checkedTargets[row];
            } else {
                float score = mindyData.getScore(enabledModulators.get(col - EXTRA_COLS), mindyData.getTranscriptionFactor(), activeTargets.get(row));
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

        public float getScoreAt(int row, int col) {
            float score = mindyData.getScore(allModulators.get(col - EXTRA_COLS), mindyData.getTranscriptionFactor(), activeTargets.get(row));
            return score;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        	/*
            if ((columnIndex == 0) && (rowIndex > 0)) {
                checkedTargets[rowIndex] = (Boolean) aValue;
            }
            if ((rowIndex == 0) && (columnIndex > 0)){
            	checkedModulators[rowIndex] = (Boolean) aValue;
            }*/
        	if (columnIndex == 0) 
                checkedTargets[rowIndex] = (Boolean) aValue;
            targetTable.repaint();
            
            selectionEnabledCheckBoxTarget.setText(MindyPlugin.ENABLE_SELECTION + " [" +  this.getUniqueCheckedTargetsAndModulators().size() + "]");
            selectionEnabledCheckBoxTarget.repaint();
        }

        public String getColumnName(int col) {
            if (col == 0) {
            	return " ";
            } else if (col == 1) {
                return "Target";
            } else {
                DSGeneMarker mod = enabledModulators.get(col - EXTRA_COLS);
                String colName =  mod.getShortName();
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

        public void resortModulators() {
            Collections.sort(enabledModulators, new ModulatorStatComparator(mindyData, modulatorSortMethod));
            fireTableStructureChanged();
        }
        
        public List<DSGeneMarker> getUniqueCheckedTargetsAndModulators(){
        	//ArrayList<DSGeneMarker> result = new ArrayList(this.getCheckedTargets().size() + this.getEnabledModulators().size());
        	ArrayList<DSGeneMarker> result = new ArrayList(this.getCheckedTargets().size());
        	for(int i = 0; i < this.getCheckedTargets().size(); i++){
        		result.add(this.getCheckedTargets().get(i));
        	}
        	/*
        	for(int i = 0; i < this.getCheckedModulators().size(); i++){
        		result.add(this.getCheckedModulators().get(i));
        	}*/
        	result.trimToSize();
        	return result;
        }
        
        public int getNumberOfMarkersSelected(){
        	return this.getUniqueCheckedTargetsAndModulators().size();
        }
        
        public List<DSGeneMarker> getActiveTargets(){
        	return activeTargets;
        }
        
        private void selectAllTargets(boolean select){
        	for (int i = 0; i < checkedTargets.length; i++) {
        		checkedTargets[i] = select;
            }
            targetTable.repaint();
        }
        
        private void selectAllModulators(boolean select){
        	for(int i = 0; i < checkedModulators.length; i++){
        		checkedModulators[i] = select;
        	}
        	targetTable.repaint();
        }
    }

    private class ModulatorStatComparator implements Comparator<DSGeneMarker> {

        private MindyData data;
        private ModulatorSort sortType;

        public ModulatorStatComparator(MindyData data, ModulatorSort sortType) {
            this.data = data;
            this.sortType = sortType;
        }

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
        private boolean allMarkersOn = true;

        public ModulatorTargetModel(MindyData mindyData) {
            this.modChecks = new boolean[mindyData.getData().size()];
            this.targetChecks = new boolean[mindyData.getData().size()];
            this.mindyData = mindyData;
            this.ascendSortStates = new boolean[columnNames.length];
            for(int i = 0; i < this.ascendSortStates.length; i++)
            	this.ascendSortStates[i] = true;
        }

        public ArrayList<DSGeneMarker> getEnabledModulators() {
            return enabledModulators;
        }

        public void setEnabledModulators(ArrayList<DSGeneMarker> enabledModulators) {
            this.enabledModulators = enabledModulators;
        }

        public ArrayList<DSGeneMarker> getEnabledTargets() {
            return enabledTargets;
        }

        public void setEnabledTargets(ArrayList<DSGeneMarker> enabledTargets) {
            this.enabledTargets = enabledTargets;
        }

        public void enableModulator(DSGeneMarker mod) {
            if (!enabledModulators.contains(mod)) {
                enabledModulators.add(mod);
                recalculateRows();
                fireTableDataChanged();
                setListTableViewOptions();
                repaint();
            }
        }

        public void disableModulator(DSGeneMarker mod) {
            enabledModulators.remove(mod);
            recalculateRows();
            fireTableStructureChanged();
            setListTableViewOptions();
            repaint();
        }

        public void enableTarget(DSGeneMarker target) {
            if (!enabledTargets.contains(target)) {
                enabledTargets.add(target);
                recalculateRows();
                fireTableDataChanged();
                setListTableViewOptions();
                repaint();
            }
        }

        public void disableTarget(DSGeneMarker target) {
            enabledTargets.remove(target);
            recalculateRows();
            fireTableDataChanged();
            setListTableViewOptions();
            repaint();
        }

        private void recalculateRows() {
            rows.clear();            
            if(this.allMarkersOn){
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

        public int getColumnCount() {
            return columnNames.length;
        }
        
        // called from MindyVisualComponent
        // i.e. when SelectionPanel changes marker set selections via GeneSelectorEvent
        public void limitMarkers(List<DSGeneMarker> limitList) {
            if (limitList == null) {
                limitedModulators = null;
                limitedTargets = null;
                log.debug("Cleared modulator and target limits.");
            } else {
                limitedModulators = new ArrayList<DSGeneMarker>();
                limitedTargets = new ArrayList<DSGeneMarker>();
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
            if (!this.allMarkersOn) {
            	recalculateRows();
                fireTableDataChanged();
                setListTableViewOptions();
                repaint();
            }
        }
        
        // called from "All Markers" checkbox
        public void showLimitedMarkers(){
    		//globalSelectionState.allMarkerOverride = false;
        	allMarkersOn = false;
        	recalculateRows();
        	fireTableDataChanged();
            setListTableViewOptions();
            repaint();
        }
        
        // called from "All Markers" checkbox
        public void showAllMarkers(){
        	//globalSelectionState.allMarkerOverride = true;
        	allMarkersOn = true;
        	recalculateRows();
        	fireTableDataChanged();
            setListTableViewOptions();
            repaint();
        }

        public int getRowCount() {
        	if ((this.allMarkersOn) ||(globalSelectionState.allMarkerOverride)){
	            if (modChecks == null) {
	                return 0;
	            } else {
	                return rows.size();
	            }
        	} else {
                return rows.size();
            }
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if ((columnIndex == 0) ||(columnIndex == 2)) {
                return true;
            } else {
                return false;
            }
        }

        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0 || columnIndex == 2) {
                return Boolean.class;
            } else if (columnIndex == columnNames.length - 1) {
                return Float.class;
            } else {
                return String.class;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
            	return modChecks[rowIndex];
            } else if (columnIndex == 1) {
                return rows.get(rowIndex).getModulator().getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            } else if (columnIndex == 2) {
                return targetChecks[rowIndex];
            } else if (columnIndex == 3) {
                return rows.get(rowIndex).getTarget().getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            } else if (columnIndex == 4) {
                return rows.get(rowIndex).getScore();
            } else {
                log.error("Requested unknown column");
                return null;
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
            	String marker = rows.get(rowIndex).getModulator().getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS).trim();
            	for(int i = 0; i < rows.size(); i++){
            		if(marker.equals(rows.get(i).getModulator().getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS).trim())){
            			modChecks[i] = (Boolean) aValue;
            		}
            	}
            	listTable.repaint();
            } else if (columnIndex == 2) {
            	String marker = rows.get(rowIndex).getTarget().getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS).trim();
            	for(int i = 0; i < rows.size(); i++){
            		if(marker.equals(rows.get(i).getTarget().getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS).trim())){
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
        
        public boolean[] getAscendSortStates(){
        	return this.ascendSortStates;
        }
        
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

    }

    private class ScoreColorRenderer extends DefaultTableCellRenderer {
        MindyData data;

        public ScoreColorRenderer(MindyData data) {
            this.data = data;
        }

        public Component getTableCellRendererComponent(JTable jTable, Object object, boolean b, boolean b1, int i, int i1) {
            setValue(object);
            this.setBackground(Color.red);
            return this;
        }
    }

    private class ModulatorListModel extends AbstractListModel {
        public int getSize() {
            return modTargetModel.getEnabledModulators().size();
        }

        public Object getElementAt(int i) {
            return modTargetModel.getEnabledModulators().get(i).getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
        }

        public void refresh() {
            fireContentsChanged(this, 0, getSize());
        }
    }

    /**
     * State of selections and overrides for the entire component
     */
    private class MarkerLimitState {
        public List<DSGeneMarker> globalUserSelection = new ArrayList<DSGeneMarker>();
        public boolean allMarkerOverride = true;
    }
 
    /**
     * For table sorting purposes
     * @author ch2514
     * @version $Id: MindyPlugin.java,v 1.19 2007-06-28 23:25:46 hungc Exp $
     */
    private class ColumnHeaderListener extends MouseAdapter {
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
            
            // sorting
            boolean ascending = true;
            if(model instanceof ModulatorModel){
            	boolean[] states = ((ModulatorModel) model).getAscendSortStates();
            	if(mColIndex < states.length){
            		states[mColIndex] = !states[mColIndex];
            		ascending = states[mColIndex];
            	}
            	
            }
            //if(model instanceof AggregateTableModel){
            	//boolean[] states = ((AggregateTableModel) model).getDescendSortStates();
            	//if(mColIndex < states.length){
            		//states[mColIndex] = !states[mColIndex];
            		//ascending = states[mColIndex];
    			//}
            //}
            if(model instanceof ModulatorTargetModel){            	
            	boolean[] states = ((ModulatorTargetModel) model).getAscendSortStates();
            	if(mColIndex < states.length){
            		states[mColIndex] = !states[mColIndex];
            		ascending = states[mColIndex];
            	}
            }
            sortAllRowsBy(table, mColIndex, ascending);
        }
    }
    
    /**
     * This comparator is used to sort vectors of data
     * @author ch2514
     * @version $Id: MindyPlugin.java,v 1.19 2007-06-28 23:25:46 hungc Exp $
     */ 
    public class ColumnSorter implements Comparator {
        int colIndex;
        boolean ascending;
        ColumnSorter(int colIndex, boolean ascending) {
            this.colIndex = colIndex;
            this.ascending = ascending;
        }
        public int compare(Object a, Object b) {
            Vector v1 = (Vector)a;
            Vector v2 = (Vector)b;
            Object o1 = v1.get(colIndex);
            Object o2 = v2.get(colIndex);
    
            // Treat empty strains like nulls
            if (o1 instanceof String && ((String)o1).length() == 0) {
                o1 = null;
            }
            if (o2 instanceof String && ((String)o2).length() == 0) {
                o2 = null;
            }
    
            // Sort nulls so they appear last, regardless
            // of sort order
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else if (o1 instanceof Comparable) {
                if (ascending) {
                    return ((Comparable)o1).compareTo(o2);
                } else {
                    return ((Comparable)o2).compareTo(o1);
                }
            } else {
                if (ascending) {
                    return o1.toString().compareTo(o2.toString());
                } else {
                    return o2.toString().compareTo(o1.toString());
                }
            }
        }
    }


    /**
     * This doesn't quite work, but it's close.
     */
    /*
    private class ModulatorHighlighter extends ConditionalHighlighter {

        private boolean useBackground = false;
        ArrayList<Boolean> rowBackgrounds = new ArrayList<Boolean>();
        private int lastRowChecked = -1;
        private int maxRowChecked = -1;

        protected boolean test(ComponentAdapter adapter) {
//            if (!adapter.isTestable(0)) {
//                return false;
//            }

            if (adapter.row < rowBackgrounds.size()) {
                return rowBackgrounds.get(adapter.row);
            }

            if (adapter.row == lastRowChecked) {
                return useBackground;
            }

            if (adapter.row == 0) {
                lastRowChecked = 0;
                maxRowChecked = 0;
                rowBackgrounds.add(useBackground);
                return useBackground;
            }

            Object value = adapter.getFilteredValueAt(adapter.row, 0);
            Object compareValue = adapter.getFilteredValueAt(adapter.row - 1, 0);
            if (!(value instanceof String)) {
                value = adapter.getFilteredValueAt(adapter.row, 1);
                compareValue = adapter.getFilteredValueAt(adapter.row - 1, 1);
            }

            lastRowChecked = adapter.row;

            if (value == null || compareValue == null) {
                rowBackgrounds.add(useBackground);
                return useBackground;
            } else {
                boolean matches = value.toString().trim().equals(compareValue.toString().trim());
                if (!matches) {
                    useBackground = !useBackground;
                }
            }
            rowBackgrounds.add(useBackground);
            return useBackground;
        }
    }
    */
}
