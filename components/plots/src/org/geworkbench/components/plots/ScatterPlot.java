package org.geworkbench.components.plots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.properties.DSNamed;
import org.geworkbench.bison.datastructure.properties.DSSequential;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.JAutoList;
import org.geworkbench.util.PrintUtils;
import org.geworkbench.util.visualproperties.PanelVisualProperties;
import org.geworkbench.util.visualproperties.PanelVisualPropertiesManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Scatter Plot component.
 *
 * @author John Watkinson
 */
@AcceptTypes({DSMicroarraySet.class})
public class ScatterPlot implements VisualPlugin {

	private static Log log = LogFactory.getLog(ScatterPlot.class);
	
    /**
     * Maximum number of charts that can be viewed at once.
     */
    static final int MAXIMUM_CHARTS = 6;

    /**
     * The two types of plots.
     * <ul>
     * <li>ARRAY - Plots the values of each marker for two experiments.
     * <li>MARKER - Plots the values of two markers for each experiment.
     * </ul>
     */
    public enum PlotType {
        ARRAY, MARKER
    }

    private static final int TAB_ARRAY = 0;
    // private static final int TAB_MARKER = 1;

    ChartGroup getGroup(PlotType type) {
    	return chartGroups.get(type);
    }
    
    private final JSplitPane mainPanel;
    private final JTabbedPane tabbedPane;
    private final JAutoList microarrayList;
    private final JAutoList markerList;
    private final JPanel chartPanel, topChartPanel, bottomChartPanel;
    private final JCheckBox rankStatisticsCheckbox;

    private final JButton clearButton;
    private final JButton printButton;
    private final JButton imageSnapshotButton;
    private final JTextField slopeField;
    final JPopupMenu popupMenu;
    PlotType popupType;
    int popupIndex;

    /**
     * The dataset that holds the microarrayset and panels.
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(null);
    final private MarkerListModel markerModel = new MarkerListModel(dataSetView);
    final private MicroarrayListModel microarrayModel = new MicroarrayListModel(dataSetView);

    /**
     * The two chart groups (one for microarrays, one for markers).
     */
    final private EnumMap<PlotType, ChartGroup> chartGroups;

    /**
     * Constructor lays out the component and adds behaviors.
     */
    @SuppressWarnings("unchecked")
	public ScatterPlot() {
        //// Create empty chart groups
        chartGroups = new EnumMap<PlotType, ChartGroup>(PlotType.class);
        chartGroups.put(PlotType.ARRAY, new ChartGroup());
        chartGroups.put(PlotType.MARKER, new ChartGroup());
        //// Create and lay out components
        // Left side:
        mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainPanel.setOneTouchExpandable(true);
        JPanel westPanel = new JPanel(new BorderLayout());
        mainPanel.add(westPanel);
        tabbedPane = new JTabbedPane();
        westPanel.add(tabbedPane, BorderLayout.CENTER);

        microarrayList = new MicroarrayAutoList(microarrayModel, this);
        microarrayList.getList().setCellRenderer( new CellRenderer(chartGroups.get(PlotType.ARRAY)) );
        microarrayList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        microarrayList.getList().setFixedCellWidth(250);
        
        markerList = new MarkerAutoList(markerModel, this);
        markerList.getList().setCellRenderer( new CellRenderer(chartGroups.get(PlotType.MARKER)) );
        markerList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        markerList.getList().setFixedCellWidth(250);
        
        tabbedPane.add("Array", microarrayList);
        tabbedPane.add("Marker", markerList);

        // Right side:
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(Box.createGlue());
        rankStatisticsCheckbox = new JCheckBox("Rank Statistics Plot");
        topPanel.add(rankStatisticsCheckbox);
        rightPanel.add(topPanel, BorderLayout.NORTH);

        chartPanel = new JPanel();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
        topChartPanel = new JPanel();
        topChartPanel.setLayout(new BoxLayout(topChartPanel, BoxLayout.X_AXIS));
        bottomChartPanel = new JPanel();
        bottomChartPanel.setLayout(new BoxLayout(bottomChartPanel, BoxLayout.X_AXIS));
        chartPanel.add(topChartPanel);
        chartPanel.add(bottomChartPanel);
        // setChartBackgroundColor(chart);
        rightPanel.add(chartPanel, BorderLayout.CENTER);
        JPanel bottomSpacingPanel = new JPanel();
        bottomSpacingPanel.setLayout(new BoxLayout(bottomSpacingPanel, BoxLayout.Y_AXIS));
        bottomSpacingPanel.add(Box.createVerticalStrut(6));
        JPanel bottomPanel = new JPanel();
        bottomSpacingPanel.add(bottomPanel);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        final JCheckBox referenceLineCheckBox = new JCheckBox("Reference Line", true);
        JLabel slopeLabel = new JLabel("Slope:");
        slopeField = new JTextField("1.0", 10) {
			private static final long serialVersionUID = 6545882976044437591L;

			@Override public Dimension getMaximumSize() {
                return super.getPreferredSize();
            }
        };
        bottomPanel.add(referenceLineCheckBox);
        bottomPanel.add(Box.createHorizontalStrut(10));
        bottomPanel.add(slopeLabel);
        bottomPanel.add(Box.createHorizontalStrut(5));
        bottomPanel.add(slopeField);
        bottomPanel.add(Box.createGlue());
        clearButton = new JButton("Clear Charts");
        bottomPanel.add(clearButton);
        bottomPanel.add(Box.createHorizontalStrut(5));
        printButton = new JButton("Print...");
        imageSnapshotButton = new JButton("Image Snapshot");
        bottomPanel.add(printButton);
        bottomPanel.add(Box.createHorizontalStrut(5));
        bottomPanel.add(imageSnapshotButton);
        rightPanel.add(bottomSpacingPanel, BorderLayout.SOUTH);
        mainPanel.add(rightPanel);
        popupMenu = new JPopupMenu();
        JMenuItem xAxisItem = new JMenuItem("Put on X-axis", 'X');
        popupMenu.add(xAxisItem);
        JMenuItem clearAllItem = new JMenuItem("Clear All", 'A');
        popupMenu.add(clearAllItem);

        // Initially inactive
        rankStatisticsCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                // Update all charts
                updateCharts(PlotType.MARKER);
                updateCharts(PlotType.ARRAY);
            }
        });

        // Change over the charts when the user switches between microarray and marker.
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedIndex() == TAB_ARRAY) {
                	updateCharts(PlotType.ARRAY);
                    packChartPanel(PlotType.ARRAY);
                } else {
                	updateCharts(PlotType.MARKER);
                    packChartPanel(PlotType.MARKER);
                }
                ChartGroup group = chartGroups.get(getActivePlotType());
                if (group.referenceLineEnabled) {
                    referenceLineCheckBox.setSelected(true);
                    slopeField.setEnabled(true);
                } else {
                    referenceLineCheckBox.setSelected(false);
                    slopeField.setEnabled(false);
                }
                slopeField.setText("" + group.slope);
            }
        });
        // Popup action for putting an item ono the x-axis
        xAxisItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setNewXAxis(popupType, popupIndex);
            }
        });
        // Popup action for clearing all charts.
        clearAllItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAllCharts(popupType);
                if (popupType == PlotType.ARRAY) {
                    microarrayModel.refresh();
                } else {
                    markerModel.refresh();
                }
                packChartPanel(popupType);
            }
        });
        // Clear button action
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAllCharts(getActivePlotType());
                packChartPanel(getActivePlotType());
                if (getActivePlotType() == PlotType.MARKER) {
                    markerModel.refresh();
                } else {
                    microarrayModel.refresh();
                }
            }
        });
        // Initially disabled
        clearButton.setEnabled(false);
        printButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PrintUtils.printComponent(chartPanel);
            }
        });
        // Initially disabled
        printButton.setEnabled(false);
        imageSnapshotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createImageSnapshot();
            }
        });
        // Initially disabled
        imageSnapshotButton.setEnabled(false);
        // Reference line on/off
        referenceLineCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ChartGroup group = chartGroups.get(getActivePlotType());
                if (referenceLineCheckBox.isSelected()) {
                    slopeField.setEnabled(true);
                    group.referenceLineEnabled = true;
                    slopeFieldChanged();
                } else {
                    slopeField.setEnabled(false);
                    group.referenceLineEnabled = false;
                    group.slopeChanged();
                }
            }
        });
        // Reference line slope field
        slopeField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                slopeFieldChanged();
            }
        });
        slopeField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                // Ignore
            }

            public void focusLost(FocusEvent e) {
                slopeFieldChanged();
            }
        });
        // Initialize chart panel
        packChartPanel(PlotType.ARRAY);
    }

    private void slopeFieldChanged() {
        String text = slopeField.getText();
        ChartGroup group = chartGroups.get(getActivePlotType());
        double newSlope;
        try {
            newSlope = Double.parseDouble(text);
            if (newSlope <= 0) {
                throw new NumberFormatException();
            }
            group.slope = newSlope;
            group.slopeChanged();
        } catch (NumberFormatException nfe) {
            // Switch back to old slope
            slopeField.setText("" + group.slope);
        }
    }

    /**
     * Adds the appropriate chart panels to the component.
     * Call this whenever charts are added or removed, or the view is switched between microarray and marker.
     * Charts are layed out in a single row (1 or 2 charts) or two rows (3 to 6 charts).
     *
     * @param type the type of charts to display.
     */
    void packChartPanel(PlotType type) {
        // Empty panel of current contents
        topChartPanel.removeAll();
        bottomChartPanel.removeAll();
        ChartGroup group = chartGroups.get(type);
        int numCharts = group.charts.size();
        if (numCharts == 0) {
            // No charts
            // Disable buttons
            clearButton.setEnabled(false);
            printButton.setEnabled(false);
            imageSnapshotButton.setEnabled(false);
        } else {
            // Enable buttons
            clearButton.setEnabled(true);
            printButton.setEnabled(true);
            imageSnapshotButton.setEnabled(true);
            int topHalf = (numCharts - 1) / 2;
            ChartPanel panel = null;
            for (int i = 0; i < numCharts; i++) {
                panel = group.charts.get(i).panel;
                if ((numCharts <= 2) || (i <= topHalf)) {
                    topChartPanel.add(panel);
                } else {
                    bottomChartPanel.add(panel);
                }
            }
            if ((numCharts > 1) && (numCharts % 2 == 1)) {
                // Add padding component so that charts are not stretched at the bottom
                final JPanel template = panel;
                JComponent padding = new JComponent() {
					private static final long serialVersionUID = -8827231207172510858L;

					@Override public Dimension getPreferredSize() {
                        return template.getPreferredSize();
                    }

                    @Override public Dimension getMaximumSize() {
                        return template.getMaximumSize();
                    }

                    @Override public Dimension getMinimumSize() {
                        return template.getMinimumSize();
                    }
                };
                padding.setForeground(Color.white);
                bottomChartPanel.setBackground(Color.white);
                bottomChartPanel.add(padding);
            }
        }
        // Ensure that the panel repaints.
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void setNewXAxis(PlotType type, int xIndex) {
    	String clickedLabel = "";
    	ChartGroup group = chartGroups.get(type);
    	if(type == PlotType.ARRAY){
    		Object o = this.microarrayModel.getElementAt(xIndex);
    		if(o != null){
    			clickedLabel = (String) o;
    		}
    	} else {
    		Object o = this.markerModel.getElementAt(xIndex);
    		if(o != null){
    			clickedLabel = ((DSGeneMarker) o).getLabel();
    		}
    	}
    	if(StringUtils.isEmpty(clickedLabel)){
    		return;
    	}
    	if(type == PlotType.ARRAY){
    		group.xIndex = this.findMAIndex(clickedLabel);
    	} else {
    		group.xIndex = this.findMarkerIndex(clickedLabel);
    	}
        Iterator<Chart> chartIterator = group.charts.iterator();
        boolean chartRemoved = false;
        while (chartIterator.hasNext()) {
            Chart chart = chartIterator.next();
            chart.setXLabel(null);
            if (!chartRemoved && chart.index == group.xIndex) {
                chartIterator.remove();
                chartRemoved = true;
            }
        }
        updateCharts(type);
        if (chartRemoved) {
            packChartPanel(type);
        }
        if (type == PlotType.ARRAY) {
            microarrayModel.refresh();
        } else {
            markerModel.refresh();
        }
    }

	private void updateBothTabs() {
		microarrayList.clearSelections();
		markerList.clearSelections();

		PlotType t = getActivePlotType();
		updateCharts(t);
		packChartPanel(t);
	}

    private void clearAllCharts(PlotType type) {
        ChartGroup chartGroup = chartGroups.get(type);
        chartGroup.xIndex = -1;
        chartGroup.charts.clear();
    }

    private PlotType getActivePlotType() {
        if (tabbedPane.getSelectedIndex() == TAB_ARRAY) {
            return PlotType.ARRAY;
        } else {
            return PlotType.MARKER;
        }
    }

    /**
     * Call this to update the data of all charts of a specific type.
     *
     * @param type the type of charts to update.
     */
    private void updateCharts(PlotType type) {
        ChartGroup group = chartGroups.get(type);
        for (int i = 0; i < group.charts.size(); i++) {
            Chart chart = group.charts.get(i);
			ChartPanel localChartPanel = chart.panel;
			JFreeChart jFreeChartOriginal = localChartPanel.getChart();
			JFreeChartProperties jFreeChartProperties = new JFreeChartProperties(jFreeChartOriginal);
			JFreeChart jFreeChartFinal = null;
			if (type == PlotType.ARRAY) {
				jFreeChartFinal =  createChart(PlotType.ARRAY, group.xIndex, chart);
			} else {
				jFreeChartFinal =  createChart(PlotType.MARKER, group.xIndex, chart);
			}
			jFreeChartProperties.updateJFreeChartProperties(jFreeChartFinal);
			chart.panel.setChart(jFreeChartFinal);
        }
    }

    /**
     * Gets the chart for the given attributes.
     *
     * @param type  the chart type (microarray or marker).
     * @param index the index of the microarray or marker.
     * @return the chart or <code>null</code> if there is no chart at this index.
     */
    Chart getChartForTypeAndIndex(PlotType type, int index) {
        ChartGroup group = chartGroups.get(type);
        for (int i = 0; i < group.charts.size(); i++) {
            Chart chart = group.charts.get(i);
            if (chart.index == index) {
                return chart;
            }
        }
        return null;
    }

    @Publish public ImageSnapshotEvent createImageSnapshot() {
        Dimension panelSize = chartPanel.getSize();
        BufferedImage image = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        chartPanel.paint(g);
        ImageIcon icon = new ImageIcon(image, "Scatter Plot");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("Scatter Plot Snapshot", icon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
        return event;
    }

    /**
     * The component for the GUI engine.
     */
    public Component getComponent() {
        return mainPanel;
    }

    /**
     * Receives a gene selection event.
     * The list of markers is updated and microarray plots are updated to reflect the selection.
     *
     * @param e      the event
     * @param source the source of the event (unused).
     */
    @Subscribe public void receive(org.geworkbench.events.GeneSelectorEvent e, Object source) {
    	if(e.getPanel() != null){    		
    		if ( e.getPanel().size() > 0 ){
    			ChartGroup group = chartGroups.get(PlotType.MARKER);
    			if(group.charts.size() > 0){
    				Chart cd = group.charts.get(0);
    				String xlabel = cd.getXLabel();
    				if(!matchLabel(e.getPanel(), xlabel)){
            			clearAllCharts(PlotType.MARKER);
            			group.xIndex = -1;
            		} else {
		            	for(int i = 0; i < group.charts.size(); i++){
		            		cd = group.charts.get(i);
		            		String ylabel = cd.getYLabel();
		            		if(!matchLabel(e.getPanel(), ylabel)){
		            			group.charts.remove(i);
		            			i--;
		            		}
		            	}
            		}
    			}
                dataSetView.setMarkerPanel(e.getPanel());
                markerModel.refresh();
                updateBothTabs();
            }

    		if( e.getPanel().size() <= 0 ) {
            	dataSetView.setMarkerPanel(new CSPanel<DSGeneMarker>(""));
            	markerModel.refresh();
            	updateBothTabs();
            }
    	}     	
    }

    /**
     * Receives a phenotype selection event.
     * The list of microarrays is updated and marker plots are updated to reflect the selection.
     *
     * @param e      the event.
     * @param source the source of the event (unused).
     */
    @Subscribe public void receive(org.geworkbench.events.PhenotypeSelectorEvent<DSMicroarray> e, Object source) {   
        if (e.getTaggedItemSetTree() != null) {
    		DSPanel<DSMicroarray> activatedArrays = e.getTaggedItemSetTree().activeSubset();
            if( activatedArrays != null && activatedArrays.size() > 0 ){
            	ChartGroup group = chartGroups.get(PlotType.ARRAY);
    			if(group.charts.size() > 0){
    				Chart cd = group.charts.get(0);
    				String xlabel = cd.getXLabel();
    				if(!matchLabel(activatedArrays, xlabel)){
            			clearAllCharts(PlotType.ARRAY);
            			group.xIndex = -1;
            		} else {
		            	for(int i = 0; i < group.charts.size(); i++){
		            		cd = group.charts.get(i);
		            		String ylabel = cd.getYLabel();
		            		if(!matchLabel(activatedArrays, ylabel)){
		            			group.charts.remove(i);
		            			i--;
		            		}
		            	}
            		}
    			}
	            dataSetView.setItemPanel(activatedArrays);
	            microarrayModel.refresh();
	            updateBothTabs();
            } 
            
            if( activatedArrays != null && activatedArrays.size() <= 0 ) {
            	dataSetView.setItemPanel(new CSPanel<DSMicroarray>(""));
            	microarrayModel.refresh();
            	updateBothTabs();
            }
        }        
    }

    /* This is the only place dataSetView reference can be re-constructed. */
    /**
     * Receives a project event.
     *
     * @param e      the event.
     * @param source the source of the event (unused).
     */
	@SuppressWarnings("unchecked")
	@Subscribe public void receive(ProjectEvent e, Object source) {

		ProjectSelection selection = ((ProjectPanel) source).getSelection();
		DSDataSet<? extends DSBioObject> dataFile = selection.getDataSet();

		if (!(dataFile instanceof DSMicroarraySet)) {
			return; // ignored in this case
		}
		
		DSMicroarraySet set = (DSMicroarraySet) dataFile;
		// If it is the same dataset as before, then don't reset everything
		if (dataSetView.getDataSet() == set) {
			return;
		}

		dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(set);
		markerModel.setDatasetView(dataSetView);
		microarrayModel.setDatasetView(dataSetView);

		clearAllCharts(PlotType.ARRAY);
		clearAllCharts(PlotType.MARKER);
		markerModel.refresh();
		microarrayModel.refresh();
		packChartPanel(getActivePlotType());
	}

    private int findMarkerIndex(String label){
    	DSItemList<DSGeneMarker> markers = dataSetView.getMicroarraySet().getMarkers();
    	for(int i = 0; i < markers.size(); i++){
    		if(StringUtils.equals(markers.get(i).getLabel().trim(), label.trim())){
    			return i;
    		}
    	}
    	return -1;
    }

    /* find the index of matching microarray in the entire dataset, not in the view */
    private int findMAIndex(String label){
    	DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
    	for(int i = 0; i < maSet.size(); i++){
    		if(StringUtils.equals(maSet.get(i).getLabel(), label))
    			return i;
    	}
    	return -1;
    }
    
    /* merged version of the original two version of create...Chart */
	@SuppressWarnings("unchecked")
	JFreeChart createChart(PlotType plotType, int exp1, Chart chart)
			throws SeriesException {
        XYSeriesCollection plots = new XYSeriesCollection();
        ArrayList<PanelVisualProperties> propertiesList = new ArrayList<PanelVisualProperties>();
        PanelVisualPropertiesManager propertiesManager = PanelVisualPropertiesManager.getInstance();

        DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
        boolean rankPlot = rankStatisticsCheckbox.isSelected();
        // First put all the gene/array pairs in the xyValues array
        RankSorter[] xyValues = null;

        int exp2 = chart.index;
        /* This map is necessary only because the rank sorting will change the xvValues array. */
        HashMap<Integer, RankSorter> map = new HashMap<Integer, RankSorter>();
        String label1 = "";
        String label2 = "";
        if(plotType==PlotType.ARRAY) {
	      	DSMicroarray ma1 = maSet.get(exp1);
	      	DSMicroarray ma2 = maSet.get(exp2);
	        label1 = ma1.getLabel();
	        label2 = ma2.getLabel();

	        int numMarkers = maSet.getMarkers().size();
	        xyValues = new RankSorter[numMarkers];
	        for (int i = 0; i < numMarkers; i++) {
	            xyValues[i] = new RankSorter();
	            xyValues[i].x = ma1.getMarkerValue(i).getValue();
	            xyValues[i].y = ma2.getMarkerValue(i).getValue();
	            xyValues[i].id = i;
	            map.put(new Integer(i), xyValues[i]);
	        }
        } else if(plotType==PlotType.MARKER) {
        	int microarrayNo = dataSetView.getMicroarraySet().size();
        	label1 = maSet.getMarkers().get(exp1).getLabel();
        	label2 = maSet.getMarkers().get(exp2).getLabel();

            xyValues = new RankSorter[microarrayNo];
			for (int i = 0; i < microarrayNo; i++) {
				xyValues[i] = new RankSorter();
				xyValues[i].x = dataSetView.getMicroarraySet().getValue(exp1, i);
				xyValues[i].y = dataSetView.getMicroarraySet().getValue(exp2, i);
				xyValues[i].id = i;
	            map.put(new Integer(i), xyValues[i]);
			}
        } else {
        	log.error("wrong plot type");
        	return new JFreeChart(null);
        }

        DSPanel<DSGeneMarker> markerPanel = dataSetView.getMarkerPanel();
        DSPanel<DSMicroarray> miroarrayPanel = dataSetView.getItemPanel();
        boolean panelsSelected = false;
		if (plotType == PlotType.ARRAY) {
			panelsSelected = isSelected(markerPanel);
		} else if (plotType == PlotType.MARKER) {
			panelsSelected = isSelected(miroarrayPanel);
		}

    	DSItemList<?> panels = null;
        if (panelsSelected) {
            if(plotType==PlotType.ARRAY) {
            	panels = markerPanel.panels();
            } else if(plotType==PlotType.MARKER) {
            	panels = miroarrayPanel.panels();
            }
            if (rankPlot) {
				for (int pId = 0; pId < panels.size(); pId++) {
					DSPanel<? extends DSSequential> panel = (DSPanel<? extends DSSequential>) panels
							.get(pId);
					for (int i = 0; i < panel.size(); i++) {
						int serial = panel.get(i).getSerial();
						xyValues[serial].setActive(true);
					}
				}
            }
        }

        // Perform rank sorting if required
        rankSort(xyValues, panelsSelected);

        ArrayList<ArrayList<RankSorter>> xyPoints = new ArrayList<ArrayList<RankSorter>>();
        
        // If some panels have been selected
        if (panelsSelected) {
            int panelIndex = 0;
            for (int pId = 0; pId < panels.size(); pId++) {
                DSPanel<? extends DSSequential> panel = (DSPanel<? extends DSSequential>) panels.get(pId);
                if (!panel.isActive() || panel.size() == 0) {
                	continue;
                }

				panelIndex++;
				XYSeries series = new XYSeries(panel.getLabel());
                ArrayList<RankSorter> list = new ArrayList<RankSorter>();
				for (int i = 0; i < panel.size(); i++) {
					int serial = panel.get(i).getSerial();
					RankSorter xy = map.get(new Integer(serial));
					xy.setPlotted();
					list.add(xy);
					if (rankPlot) {
						series.add(xy.ix, xy.iy);
					} else {
						series.add(xy.x, xy.y);
					}
				}
				plots.addSeries(series);
				PanelVisualProperties properties = propertiesManager
						.getVisualProperties(panel);
				if (properties == null) {
					properties = propertiesManager
							.getDefaultVisualProperties(panelIndex);
				}
				propertiesList.add(properties);
				Collections.sort(list, RankSorter.SORT_X);
				xyPoints.add(list);
            }
        } else { // no panels selected
            ArrayList<RankSorter> list = new ArrayList<RankSorter>();
            XYSeries series = new XYSeries("All");
            for (int serial = 0; serial < xyValues.length; serial++) {
                if (!xyValues[serial].isPlotted()) {
                    list.add(xyValues[serial]);
                    if (rankPlot) {
                        series.add(xyValues[serial].ix, xyValues[serial].iy);
                    } else {
                        series.add(xyValues[serial].x, xyValues[serial].y);
                    }
                }
            }
            xyPoints.add(0, list);
            plots.addSeries(series);
        }

        chart.setXLabel(label1);
        chart.setYLabel(label2);
        JFreeChart mainChart = ChartFactory.createScatterPlot("", label1, label2, plots, PlotOrientation.VERTICAL, true, true, false); // Title, (, // X-Axis label,  Y-Axis label,  Dataset,  Show legend
        XYLineAnnotation annotation = chartGroups.get(plotType).lineAnnotation;
        if (annotation != null) {
            mainChart.getXYPlot().addAnnotation(annotation);
        }
        chart.setXyPoints(xyPoints);

        setTooltips(dataSetView, mainChart, plotType, propertiesList, panelsSelected, chart);

        return mainChart;
    }
	
	private static boolean isSelected(DSPanel<?> panel) {
		boolean selected = (panel != null) && (panel.size() > 0)
				&& (panel.getLabel().compareToIgnoreCase("Unsupervised") != 0)
				&& (panel.panels().size() > 0)
				&& (panel.activeSubset().size() > 0);
		return selected;
	}
	
	private static void rankSort(RankSorter[] xyValues, boolean panelsSelected) {
        int rank = 0;

        Arrays.sort(xyValues, RankSorter.SORT_Y);
        for (int j = 0; j < xyValues.length; j++) {
            if (!panelsSelected || xyValues[j].isActive() || xyValues[j].isFiltered()) {
                xyValues[j].iy = rank++;
            }
        }

        rank = 0;

        Arrays.sort(xyValues, RankSorter.SORT_X);
        for (int j = 0; j < xyValues.length; j++) {
            if (!panelsSelected || xyValues[j].isActive() || xyValues[j].isFiltered()) {
                xyValues[j].ix = rank++;
            }
        }
	}
    
    private static void setTooltips(DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView,
    		JFreeChart mainChart, PlotType plotType, ArrayList<PanelVisualProperties> propertiesList, boolean panelsSelected,
    		Chart chart) // only for microasrray 
    {
    	StandardXYItemRenderer renderer = null;
    	if(plotType==PlotType.ARRAY) {
    	      MicroarrayXYToolTip tooltips = new MicroarrayXYToolTip(dataSetView, chart, mainChart.getXYPlot());
    	      renderer = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES, tooltips);      
    	      Rectangle2D bound = renderer.getBaseShape().getBounds2D();
    	      tooltips.setShapeBound(bound);
    	} else if(plotType==PlotType.MARKER) {
            StandardXYToolTipGenerator tooltips = new GeneXYToolTip(dataSetView, chart);
            renderer = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES, tooltips);
    	} else {
    		log.error("wrong plot type");
    		return;
    	}
    	
        for (int i = 0; i < propertiesList.size(); i++) {
            PanelVisualProperties panelVisualProperties = propertiesList.get(i);
            // Note: "i+1" because we skip the default 'other' series.
            int index = panelsSelected ? i : i + 1;
            renderer.setSeriesPaint(index, panelVisualProperties.getColor());
            renderer.setSeriesShape(index, panelVisualProperties.getShape());
        }
        mainChart.getXYPlot().setRenderer(renderer);
    }
    
    @Publish public org.geworkbench.events.MarkerSelectedEvent publishMarkerSelectedEvent(MarkerSelectedEvent event) {
        return event;
    }

    @Publish public PhenotypeSelectedEvent publishPhenotypeSelectedEvent(PhenotypeSelectedEvent event) {
        return event;
    }
    
	private static boolean matchLabel(DSPanel<? extends DSNamed> panel,
			String label) {
		for (int i=0; i<panel.size(); i++) {
			DSNamed m = panel.get(i);
			if (StringUtils.equals(m.getLabel().trim(), label.trim()))
				return true;
		}
		return false;
	}
}
