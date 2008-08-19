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
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
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
import org.geworkbench.util.pathwaydecoder.RankSorter;
import org.geworkbench.util.visualproperties.PanelVisualProperties;
import org.geworkbench.util.visualproperties.PanelVisualPropertiesManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Scatter Plot component.
 *
 * @author John Watkinson
 */
@AcceptTypes({DSMicroarraySet.class})
public class ScatterPlot implements VisualPlugin {

    /**
     * Maximum number of charts that can be viewed at once.
     */
    public static final int MAXIMUM_CHARTS = 6;

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

    ;

    private static final int TAB_ARRAY = 0;
    private static final int TAB_MARKER = 1;

    /**
     * ListModel for the marker list.
     */
    private class MarkerListModel extends AbstractListModel {

        public int getSize() {
            if (dataSetView.getMicroarraySet() == null) {
                return 0;
            }
            if(dataSetView.useMarkerPanel()){
            	DSPanel<DSGeneMarker> mp = dataSetView.getMarkerPanel();
            	if((mp != null) && (mp.size() > 0)) {      
                	return mp.size();
            	} else {
            		return 0;
            	}
            } else {
            	return dataSetView.getMicroarraySet().getMarkers().size();
            }
            
        }

        public Object getElementAt(int index) {
            if (dataSetView.getMicroarraySet() == null) {
                return null;
            }
            if(dataSetView.useMarkerPanel()){
            	DSPanel<DSGeneMarker> mp = dataSetView.getMarkerPanel();
            	if((mp != null) && (mp.size() > 0) && (index < mp.size())) {      
                	return mp.get(index);
            	} else {
            		return null;
            	}
            } else {
            	return dataSetView.getMicroarraySet().getMarkers().get(index);
            }
        }

        public DSGeneMarker getMarker(int index) {
            return dataSetView.getMicroarraySet().getMarkers().get(index);
        }

        /**
         * Indicates to the associated JList that the contents need to be redrawn.
         */
        public void refresh() {
            if (dataSetView.getMicroarraySet() == null) {
                fireContentsChanged(this, 0, 0);
            } else {
            	if(dataSetView.useMarkerPanel()) {
            		fireContentsChanged(this, 0, dataSetView.getMarkerPanel().size() - 1);
            	} else {
            		fireContentsChanged(this, 0, dataSetView.getMicroarraySet().getMarkers().size() - 1);
            	}
            }
        }

    }

    /**
     * ListModel for the microarray list.
     */
    private class MicroarrayListModel extends AbstractListModel {

        public int getSize() {
            if (dataSetView.getMicroarraySet() == null) {
                return 0;
            }
            if(dataSetView.useItemPanel()){
            	DSPanel<DSMicroarray> ap = dataSetView.getItemPanel();
            	if((ap != null) && (ap.size() > 0)){
            		return ap.size();
            	} else {
            		return 0;
            	}
            } else {
            	return dataSetView.getMicroarraySet().size();
            }            
        }

        public Object getElementAt(int index) {
            if (dataSetView.getMicroarraySet() == null) {
                return null;
            }
            if(dataSetView.useItemPanel()){
            	DSPanel<DSMicroarray> ap = dataSetView.getItemPanel();
            	if((ap != null) && (ap.size() > 0) && (index < ap.size())){
            		return ap.get(index);
            	} else {
            		return null;
            	}
            } else {
            	return dataSetView.getMicroarraySet().get(index).getLabel();
            }
        }

        public DSBioObject getMicroarray(int index) {
            return dataSetView.getMicroarraySet().get(index);
        }

        /**
         * Indicates to the associated JList that the contents need to be redrawn.
         */
        public void refresh() {        	
            if (dataSetView.getMicroarraySet() == null) {
                fireContentsChanged(this, 0, 0);
            } else {
            	if(dataSetView.useItemPanel()){
            		fireContentsChanged(this, 0, dataSetView.getItemPanel().size() - 1);
            	} else {
            		fireContentsChanged(this, 0, dataSetView.getMicroarraySet().size() - 1);
            	}
            }
        }

    }

    /**
     * The marker JAutoList type.
     */
    private class MarkerAutoList extends JAutoList {

        public MarkerAutoList() {
            super(markerModel);
        }

        @Override protected void elementClicked(int index, MouseEvent e) {
            itemClicked(PlotType.MARKER, index);
            markerModel.refresh();
        }

        @Override protected void elementRightClicked(int index, MouseEvent e) {
            itemRightClicked(PlotType.MARKER, e, index);
        }
    }

    /**
     * The microarray JAutoList type.
     */
    private class MicroarrayAutoList extends JAutoList {

        public MicroarrayAutoList() {
            super(microarrayModel);
        }

        @Override protected void elementClicked(int index, MouseEvent e) {
            itemClicked(PlotType.ARRAY, index);
            microarrayModel.refresh();
        }

        @Override protected void elementRightClicked(int index, MouseEvent e) {
            itemRightClicked(PlotType.ARRAY, e, index);
        }

    }

    /**
     * Struct for the objects associated with a chart.
     */
    private class Chart {

        public Chart() {
            chartData = new ChartData();
        }

        public int index;
        public ChartPanel panel;
        public ChartData chartData;
    }

    /**
     * Struct for a group of charts.
     */
    private static class ChartGroup {
        public ChartGroup() {
            charts = new ArrayList<Chart>();
        }

        public int xIndex = -1;
        public ArrayList<Chart> charts;
        public boolean referenceLineEnabled = true;
        public double slope = 1.0;
        public XYLineAnnotation lineAnnotation = getXYLineAnnotation(slope);
    }

    /**
     * Cell rendered for the lists that handles the special selection conditions.
     */
    private class CellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);    //To change body of overridden methods use File | Settings | File Templates.
            Chart chart;
            ChartGroup group;
            // See if there is chart for this item
            if (list == microarrayList.getList()) {
                group = chartGroups.get(PlotType.ARRAY);
                chart = getChartForTypeAndIndex(PlotType.ARRAY, index);
            } else {
                group = chartGroups.get(PlotType.MARKER);
                chart = getChartForTypeAndIndex(PlotType.MARKER, index);
            }
            if (chart != null) {
                // Color the label to indicate that there is a chart.
                label.setBackground(Color.LIGHT_GRAY);
            } else {
                // Check for the cell being on the x-axis
                if (group.xIndex == index) {
                    // Color the label to indicate that it is on the x-axis of the chart.
                    label.setBackground(Color.BLACK);
                    label.setForeground(Color.WHITE);
                }
            }
            return label;
        }
    }

    /**
     * Responsible for handling microarray selection in a gene scatter plot.
     */
    private class GeneChartMouseListener implements ChartMouseListener {

        private ChartData chartData;

        public GeneChartMouseListener(ChartData data) {
            this.chartData = data;
        }

        public void chartMouseClicked(ChartMouseEvent event) {
            // Currently a no-op, but could select the microarray
            ChartEntity entity = event.getEntity();
            if ((entity != null) && (entity instanceof XYItemEntity)) {
                XYItemEntity xyEntity = (XYItemEntity) entity;
                int series = xyEntity.getSeriesIndex();
                int item = xyEntity.getItem();
                DSMicroarray microarray = chartData.getMicroarray(series, item);
                PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(microarray);
                publishPhenotypeSelectedEvent(pse);
            }
        }

        public void chartMouseMoved(ChartMouseEvent event) {
            // No-op
        }
    }

    /**
     * Responsible for handling marker selection in a microarray scatter plot.
     */
    private class MicroarrayChartMouseListener implements ChartMouseListener {

        private ChartData chartData;

        public MicroarrayChartMouseListener(ChartData data) {
            this.chartData = data;
        }

        public void chartMouseClicked(ChartMouseEvent event) {
            ChartEntity entity = event.getEntity();
            if ((entity != null) && (entity instanceof XYItemEntity)) {
                XYItemEntity xyEntity = (XYItemEntity) entity;
                int series = xyEntity.getSeriesIndex();
                int item = xyEntity.getItem();
                DSGeneMarker marker = chartData.getMarker(series, item);
                if (marker != null) {
                    MarkerSelectedEvent mse = new org.geworkbench.events.MarkerSelectedEvent(marker);
                    publishMarkerSelectedEvent(mse);
                }
            }
        }

        public void chartMouseMoved(ChartMouseEvent event) {
            // No-op
        }
    }

    private class ChartData {
        private ArrayList<ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter>> xyPoints;
        private String xLabel = "";
        private String yLabel = "";

        public ChartData() {
        }

        public void setXyPoints(ArrayList<ArrayList<RankSorter>> xyPoints) {
            this.xyPoints = xyPoints;
        }

        public DSGeneMarker getMarker(int series, int item) {
            ArrayList<RankSorter> list = xyPoints.get(series);
            org.geworkbench.util.pathwaydecoder.RankSorter rs = (RankSorter) list.get(item);
            DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) dataSetView.getDataSet();
            if (maSet != null) {
                if (rs.id < maSet.getMarkers().size()) {
                    DSGeneMarker marker = maSet.getMarkers().get(rs.id);
                    return marker;
                }
            }
            return null;
        }

        public DSMicroarray getMicroarray(int series, int item) {
            ArrayList<RankSorter> list = xyPoints.get(series);
            RankSorter rs = (org.geworkbench.util.pathwaydecoder.RankSorter) list.get(item);
            DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) dataSetView.getDataSet();
            if (maSet != null) {
                if (rs.id < maSet.size()) {
                    DSMicroarray ma = maSet.get(rs.id);
                    return ma;
                }
            }
            return null;
        }

        public org.geworkbench.util.pathwaydecoder.RankSorter getRankSorter(int series, int item) {
            ArrayList<RankSorter> list = xyPoints.get(series);
            RankSorter rs = (RankSorter) list.get(item);
            return rs;
        }
        
        public String getXLabel(){
        	return this.xLabel;
        }
        
        public void setXLabel(String x){
        	this.xLabel = x;
        }
        
        public String getYLabel(){
        	return this.yLabel;
        }
        
        public void setYLabel(String y){
        	this.yLabel = y;
        }
    }

    /**
     * Tool-tip renderer for gene charts.
     */
    private class GeneXYToolTip extends StandardXYToolTipGenerator {

        private ChartData chartData;

        public GeneXYToolTip(ChartData data) {
            this.chartData = data;
        }

        public String generateToolTip(XYDataset data, int series, int item) {
            String result = "Unknown: ";
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);

            DSMicroarray ma = chartData.getMicroarray(series, item);
            if (ma != null) {
                org.geworkbench.util.pathwaydecoder.RankSorter rs = chartData.getRankSorter(series, item);
                DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) dataSetView.getDataSet();
                DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);
                String xLabel = nf.format(rs.x);
                String yLabel = nf.format(rs.y);
                String[] labels = context.getLabelsForItem(ma);

                if (labels.length > 0) {
                    result = ma.getLabel() + ": " + labels[0] + " [" + xLabel + "," + yLabel + "]";
                } else {
                    result = ma.getLabel() + ": " + "No Panel, " + " [" + xLabel + "," + yLabel + "]";
                }
                return result;
            } else {
                return "";
            }
        }
    }

    /**
     * Tool-tip renderer for microarray charts.
     */
    private class MicroarrayXYToolTip extends StandardXYToolTipGenerator {

        private ChartData chartData;

        public MicroarrayXYToolTip(ChartData data) {
            this.chartData = data;
        }

        public String generateToolTip(XYDataset data, int series, int item) {
            String result = "Unknown: ";
            DSGeneMarker marker = chartData.getMarker(series, item);
            if (marker != null) {
                org.geworkbench.util.pathwaydecoder.RankSorter rs = chartData.getRankSorter(series, item);
                DSPanel<DSGeneMarker> panel = dataSetView.getMarkerPanel();
                DSPanel value = panel.getPanel(marker);
                if (value != null) {
                    result = marker.getLabel() + ": " + value.getLabel() + " [" + rs.x + "," + rs.y + "]";
                } else {
                    result = marker.getLabel() + ": " + "No Panel, " + " [" + rs.x + "," + rs.y + "]";
                }
                return result;
            } else {
                return "";
            }
        }
    }

    private JSplitPane mainPanel;
    private JTabbedPane tabbedPane;
    private org.geworkbench.util.JAutoList microarrayList;
    private JAutoList markerList;
    private JPanel chartPanel, topChartPanel, bottomChartPanel;
    private JCheckBox rankStatisticsCheckbox;
    private JCheckBox allMarkersCheckBox;
    private JCheckBox allArraysCheckBox;
    private JCheckBox referenceLineCheckBox;
    private JButton clearButton;
    private JButton printButton;
    private JButton imageSnapshotButton;
    private JTextField slopeField;
    private JPopupMenu popupMenu;
    private PlotType popupType;
    private int popupIndex;
    private int limitMarkers = 0;
    private int limitArrays = 0;

    /**
     * The dataset that holds the microarrayset and panels.
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>();

    /**
     * The two chart groups (one for microarrays, one for markers).
     */
    private EnumMap<PlotType, ChartGroup> chartGroups;

    private MarkerListModel markerModel = new MarkerListModel();
    private MicroarrayListModel microarrayModel = new MicroarrayListModel();

    /**
     * Constructor lays out the component and adds behaviors.
     */
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
        makeDefaultMicroArrayList();
        makeDefaultMarkerList();
        tabbedPane.add("Array", microarrayList);
        tabbedPane.add("Marker", markerList);
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.X_AXIS));
        westPanel.add(checkboxPanel, BorderLayout.SOUTH);
        allArraysCheckBox = new JCheckBox("All Arrays", false);
        checkboxPanel.add(allArraysCheckBox);
        checkboxPanel.add(Box.createHorizontalStrut(10));
        allMarkersCheckBox = new JCheckBox("All Markers", false);
        checkboxPanel.add(allMarkersCheckBox);
        // Right side:
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(Box.createGlue());
        rankStatisticsCheckbox = new JCheckBox("Rank Statistics Plot");
        topPanel.add(rankStatisticsCheckbox);
        rightPanel.add(topPanel, BorderLayout.NORTH);
        //        XYLineAnnotation identityAnnotation = getXYLineAnnotation(1.0);
        //        JFreeChart chart = ChartFactory.createScatterPlot("", "", "", new XYSeriesCollection(), PlotOrientation.VERTICAL, false, true, true);
        //        chart.getXYPlot().addAnnotation(identityAnnotation);
        //        blankChartPanel = new ChartPanel(chart);
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
        referenceLineCheckBox = new JCheckBox("Reference Line", true);
        JLabel slopeLabel = new JLabel("Slope:");
        slopeField = new JTextField("1.0", 10) {
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
        //// Add behavior
        allMarkersCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                	if((limitMarkers == 0) && (dataSetView.getMarkerPanel().size() > 0)){
                		dataSetView.useMarkerPanel(true);
                		limitMarkers = dataSetView.getMarkerPanel().size();
                		markerModel.refresh();
                	}
                } else {
                	if(limitMarkers > 0){
                		dataSetView.useMarkerPanel(false);
                		limitMarkers = 0;
                		markerModel.refresh();
                	}
                }   
                updateBothTabs(PlotType.ARRAY);                
            }
        });
        allArraysCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                	if((limitArrays == 0) && (dataSetView.getItemPanel().size() > 0)){
                		dataSetView.useItemPanel(true);
                		limitArrays = dataSetView.getItemPanel().size();
                		microarrayModel.refresh();
                	}
                } else {
                	if(limitArrays > 0){
                		dataSetView.useItemPanel(false);
                		limitArrays = 0;
                		microarrayModel.refresh();
                	}
                }
                updateBothTabs(PlotType.MARKER); 
            }
        });
        // Initially inactive
        allArraysCheckBox.setEnabled(false);
        rankStatisticsCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                // Update all charts
                updateCharts(PlotType.MARKER);
                updateCharts(PlotType.ARRAY);
            }
        });
        // Use these listeners to effectively prevent selection (we handle that specially ourselves)
        microarrayList.getList().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // microarrayList.getList().clearSelection();
            }
        });
        markerList.getList().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // markerList.getList().clearSelection();
            }
        });
        // Change over the charts when the user switches between microarray and marker.
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedIndex() == TAB_ARRAY) {
                    packChartPanel(PlotType.ARRAY);
                    allArraysCheckBox.setEnabled(false);
                    allMarkersCheckBox.setEnabled(true);
                } else {
                    packChartPanel(PlotType.MARKER);
                    allArraysCheckBox.setEnabled(true);
                    allMarkersCheckBox.setEnabled(false);
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
                    slopeChanged(group);
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

    private void makeDefaultMarkerList() {
        markerList = new MarkerAutoList();
        markerList.getList().setCellRenderer(new CellRenderer());
        markerList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        markerList.getList().setFixedCellWidth(250);
    }

    private void makeDefaultMicroArrayList() {
        microarrayList = new MicroarrayAutoList();
        microarrayList.getList().setCellRenderer(new CellRenderer());
        microarrayList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        microarrayList.getList().setFixedCellWidth(250);
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
            slopeChanged(group);
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
    private void packChartPanel(PlotType type) {
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
                bottomChartPanel.add(padding);
            }
        }
        // Ensure that the panel repaints.
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private static XYLineAnnotation getXYLineAnnotation(double slope) {
        // Lines must unfortunately be limited in length due to bug in JFreeChart.
        double low = -100.0;
        double high = 1000.0;
        if (slope > 1) {
            double highX = high / slope;
            double lowX = low / slope;
            return new XYLineAnnotation(lowX, low, highX, high);
        } else {
            double highY = high * slope;
            double lowY = low * slope;
            return new XYLineAnnotation(low, lowY, high, highY);
        }
    }

    private void slopeChanged(ChartGroup group) {
        // Change annotations on all charts in this chart group
        XYLineAnnotation newAnnotation;
        if (group.referenceLineEnabled) {
            newAnnotation = getXYLineAnnotation(group.slope);
        } else {
            newAnnotation = null;
        }
        for (int i = 0; i < group.charts.size(); i++) {
            Chart chart = group.charts.get(i);
            XYPlot plot = chart.panel.getChart().getXYPlot();
            if (group.lineAnnotation != null) {
                plot.removeAnnotation(group.lineAnnotation);
            }
            if (newAnnotation != null) {
                plot.addAnnotation(newAnnotation);
            }
        }
        group.lineAnnotation = newAnnotation;
    }

    /**
     * Called when an item is clicked in either the microarray or marker list.
     *
     * @param type the type of item clicked.
     * @param e    the mouse event for the action.
     */
    private void itemRightClicked(PlotType type, MouseEvent e, int index) {
        popupType = type;
        popupIndex = index;
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void setNewXAxis(PlotType type, int xIndex) {
        ChartGroup group = chartGroups.get(type);
        group.xIndex = xIndex;
        Iterator<Chart> chartIterator = group.charts.iterator();
        boolean chartRemoved = false;
        while (chartIterator.hasNext()) {
            Chart chart = chartIterator.next();
            if (chart.index == xIndex) {
                chartIterator.remove();
                chartRemoved = true;
                break;
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

    /**
     * Called when an item is clicked in either the microarray or marker list.
     *
     * @param type  the type of item clicked.
     * @param index the index of the item.
     */
    private void itemClicked(PlotType type, int index) {
        ChartGroup group = chartGroups.get(type);
        if (group.xIndex == -1) {
            // This item goes on the x-axis.
            group.xIndex = index;
        } else {
            if (index == group.xIndex) {
                // Clicked on the x-axis item-- ignore.
            } else {
                // Is it already plotted on the y-axis?
                Chart existing = getChartForTypeAndIndex(type, index);
                if (existing == null) {
                    // Put item on the y-axis.
                    // Already reach maximum plots? Replace the oldest one.
                    Chart attributes;
                    if (group.charts.size() == MAXIMUM_CHARTS) {
                        attributes = group.charts.remove(0);
                    } else {
                        // Otherwise, create a new one.
                        attributes = new Chart();
                    }
                    attributes.index = index;
                    group.charts.add(attributes);
                    JFreeChart chart;
                    if (type == PlotType.ARRAY) {
                        chart = createMicroarrayChart(group.xIndex, index, attributes.chartData);
                    } else {
                        chart = createGeneChart(group.xIndex, index, attributes.chartData);
                    }
                    if (attributes.panel == null) {
                        attributes.panel = new ChartPanel(chart);
                        if (type == PlotType.ARRAY) {
                            attributes.panel.addChartMouseListener(new MicroarrayChartMouseListener(attributes.chartData));
                        } else {
                            attributes.panel.addChartMouseListener(new GeneChartMouseListener(attributes.chartData));
                        }
                    } else {
                        attributes.panel.setChart(chart);
                    }
                } else {
                    // Otherwise, remove the chart
                    group.charts.remove(existing);
                }
                // Lay out the chart panel again (as the number of charts has changed)
                packChartPanel(type);
            }
        }
    }
    
    private void updateBothTabs(PlotType show){
    	if(PlotType.ARRAY == show){
    		updateCharts(PlotType.MARKER);
            packChartPanel(PlotType.MARKER);
            updateCharts(PlotType.ARRAY);
            packChartPanel(PlotType.ARRAY);
            //tabbedPane.setSelectedIndex(TAB_ARRAY);
    	} else {
    		updateCharts(PlotType.ARRAY);
            packChartPanel(PlotType.ARRAY);
            updateCharts(PlotType.MARKER);
            packChartPanel(PlotType.MARKER);
            //tabbedPane.setSelectedIndex(TAB_MARKER);
    	}    	
    	microarrayList.clearSelections();
        markerList.clearSelections();
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
            if (type == PlotType.ARRAY) {
                chart.panel.setChart(createMicroarrayChart(group.xIndex, chart.index, chart.chartData));
            } else {
                chart.panel.setChart(createGeneChart(group.xIndex, chart.index, chart.chartData));
            }
        }
    }

    /**
     * Makes the chart fit in with the background color of the application.
     */
    private void setChartBackgroundColor(JFreeChart chart) {
        Color c = UIManager.getColor("Panel.background");
        if (c != null) {
            chart.setBackgroundPaint(c);
        }
    }

    /**
     * Gets the chart for the given attributes.
     *
     * @param type  the chart type (microarray or marker).
     * @param index the index of the microarray or marker.
     * @return the chart or <code>null</code> if there is no chart at this index.
     */
    private Chart getChartForTypeAndIndex(PlotType type, int index) {
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
    		if ((e.getPanel().size() > 0) && !allMarkersCheckBox.isSelected() && (limitMarkers != e.getPanel().size())){ 
    			ChartGroup group = chartGroups.get(PlotType.MARKER);
    			if(group.charts.size() > 0){
    				ChartData cd = group.charts.get(0).chartData;
    				String xlabel = cd.getXLabel();
    				if(!isInMarkerPanel(e.getPanel(), xlabel)){
            			clearAllCharts(PlotType.MARKER);
            			group.xIndex = -1;
            		} else {
		            	for(int i = 0; i < group.charts.size(); i++){
		            		cd = group.charts.get(i).chartData;
		            		String ylabel = cd.getYLabel();
		            		if(!isInMarkerPanel(e.getPanel(), ylabel)){
		            			group.charts.remove(i);
		            			i--;
		            		}
		            	}
            		}
    			}
            	dataSetView.useMarkerPanel(true);
                dataSetView.setMarkerPanel(e.getPanel());
                limitMarkers = dataSetView.getMarkerPanel().size();
                markerModel.refresh();
                updateBothTabs(PlotType.MARKER);
            } 
    		if((e.getPanel().size() > 0) && allMarkersCheckBox.isSelected() && (limitMarkers != e.getPanel().size())){
                dataSetView.setMarkerPanel(e.getPanel());
                updateCharts(PlotType.ARRAY);
    		}
    		if(((e.getPanel().size() <= 0) || allMarkersCheckBox.isSelected()) && (limitMarkers > 0)) {
            	dataSetView.useMarkerPanel(false);
            	dataSetView.setMarkerPanel(null);
            	limitMarkers = 0;
            	markerModel.refresh();
            	updateBothTabs(PlotType.MARKER);
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
    @Subscribe public void receive(org.geworkbench.events.PhenotypeSelectorEvent e, Object source) {    
        if (e.getTaggedItemSetTree() != null) {
            DSPanel<DSMicroarray> activatedArrays = e.getTaggedItemSetTree().activeSubset();
            if((activatedArrays != null) && (activatedArrays.size() > 0) && !allArraysCheckBox.isSelected() && (limitArrays != activatedArrays.size())){
            	ChartGroup group = chartGroups.get(PlotType.ARRAY);
    			if(group.charts.size() > 0){
    				ChartData cd = group.charts.get(0).chartData;
    				String xlabel = cd.getXLabel();
    				if(!isInMicroarrayPanel(activatedArrays, xlabel)){
            			clearAllCharts(PlotType.ARRAY);
            			group.xIndex = -1;
            		} else {
		            	for(int i = 0; i < group.charts.size(); i++){
		            		cd = group.charts.get(i).chartData;
		            		String ylabel = cd.getYLabel();
		            		if(!isInMicroarrayPanel(activatedArrays, ylabel)){
		            			group.charts.remove(i);
		            			i--;
		            		}
		            	}
            		}
    			}
            	dataSetView.useItemPanel(true);
	            dataSetView.setItemPanel(activatedArrays);
	            limitArrays = dataSetView.getItemPanel().size();
	            microarrayModel.refresh();
	            updateBothTabs(PlotType.ARRAY);
            } 
            if((activatedArrays != null) && (activatedArrays.size() > 0) && allArraysCheckBox.isSelected() && (limitArrays != activatedArrays.size())){
            	dataSetView.setItemPanel(activatedArrays);
            	updateCharts(PlotType.MARKER);
            }
            if((((activatedArrays != null) && (activatedArrays.size() <= 0)) || allArraysCheckBox.isSelected()) && (limitArrays > 0)){
            	dataSetView.useItemPanel(false);
            	dataSetView.setItemPanel(null);
            	limitArrays = 0;
            	microarrayModel.refresh();
            	updateBothTabs(PlotType.ARRAY);
            }       
            
        }        
    }

    /**
     * Receives a project event.
     *
     * @param e      the event.
     * @param source the source of the event (unused).
     */
    @Subscribe public void receive(ProjectEvent e, Object source) {
        boolean doClear = true;
        if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
            dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>();
            markerModel.refresh();
            microarrayModel.refresh();
//            makeDefaultMarkerList();
//            makeDefaultMicroArrayList();
//            tabbedPane.invalidate();
        } else {
            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSDataSet dataFile = selection.getDataSet();
            if (dataFile != null && dataFile instanceof DSMicroarraySet) {
                DSMicroarraySet set = (DSMicroarraySet) dataFile;
                // If it is the same dataset as before, then don't reset everything
                if (dataSetView.getDataSet() != set) {
                    dataSetView.setMicroarraySet(set);
                } else {
                    doClear = false;
                }
            } else {
                dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>();
                markerModel.refresh();
                microarrayModel.refresh();
            }
        }
        if (doClear) {
            clearAllCharts(PlotType.ARRAY);
            clearAllCharts(PlotType.MARKER);
            markerModel.refresh();
            microarrayModel.refresh();
            packChartPanel(getActivePlotType());
        }
    }

    /**
     * @todo Merge this method with {@link #createMicroarrayChart(int, int, org.geworkbench.components.plots.ScatterPlot.ChartData)} .
     */
    private JFreeChart createGeneChart(int marker1, int marker2, ChartData chartData) throws SeriesException {
        XYSeriesCollection plots = new XYSeriesCollection();
        ArrayList seriesList = new ArrayList();
        ArrayList<PanelVisualProperties> propertiesList = new ArrayList<PanelVisualProperties>();
        PanelVisualPropertiesManager propertiesManager = PanelVisualPropertiesManager.getInstance();
        boolean showAll = allArraysCheckBox.isSelected();

        //dataSetView.useItemPanel(!showAll);
        //dataSetView.useMarkerPanel(!allMarkersCheckBox.isSelected());

        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) dataSetView.getDataSet();
        boolean rankPlot = rankStatisticsCheckbox.isSelected();
        HashMap map = new HashMap();
        //        int microarrayNo = maSet.size();
        int microarrayNo = dataSetView.getMicroarraySet().size();

        // First put all the gene pairs in the xyValues array
        org.geworkbench.util.pathwaydecoder.RankSorter[] xyValues = new RankSorter[microarrayNo];
        ArrayList<ArrayList<RankSorter>> xyPoints = new ArrayList<ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter>>();
        for (int i = 0; i < microarrayNo; i++) {
            //            DSMicroarray ma = maSet.get(i);
            xyValues[i] = new org.geworkbench.util.pathwaydecoder.RankSorter();
            xyValues[i].x = dataSetView.getMicroarraySet().getValue(marker1, i);
            xyValues[i].y = dataSetView.getMicroarraySet().getValue(marker2, i);

            xyValues[i].id = i;
            //            map.put(new Integer(i), xyValues[i]);
            map.put(new Integer(dataSetView.getMicroarraySet().get(i).getSerial()), xyValues[i]);
        }
        boolean panelsSelected = (dataSetView.getItemPanel() != null) && (dataSetView.getItemPanel().size() > 0) && (dataSetView.getItemPanel().getLabel().compareToIgnoreCase("Unsupervised") != 0) && (dataSetView.getItemPanel().panels().size() > 0);
        if ((dataSetView.getItemPanel() != null) && (dataSetView.getItemPanel().activeSubset().size() == 0)) {
            panelsSelected = false;
            showAll = true;
        }
        if (rankPlot && !showAll) {
            // Must first activate all valid points
            if (panelsSelected) {
                for (int pId = 0; pId < dataSetView.getItemPanel().panels().size(); pId++) {
                    DSPanel<DSMicroarray> panel = dataSetView.getItemPanel().panels().get(pId);
                    int itemNo = panel.size();
                    if (itemNo > 0) {
                        for (int i = 0; i < itemNo; i++) {
                            int serial = panel.get(i).getSerial();
                            xyValues[serial].setActive(true);
                        }
                    }
                }
            }
        }

        // Perform rank sorting if required
        int rank = 0;
        /*
        Arrays.sort(xyValues, RankSorter.SORT_Y);
        for (int j = 1; j < xyValues.length; j++) {
            if (rankPlot) {
                if (xyValues[j].y == xyValues[j - 1].y) {
                    xyValues[j - 1].y += Math.random() * 0.1 - 0.05;
                }
            }
        }
        */
        Arrays.sort(xyValues, RankSorter.SORT_Y);
        for (int j = 0; j < xyValues.length; j++) {
            if (showAll || xyValues[j].isActive() || xyValues[j].isFiltered()) {
                xyValues[j].iy = rank++;
            }
        }
        double maxY = xyValues[xyValues.length - 1].y;

        rank = 0;
        /*
        Arrays.sort(xyValues, RankSorter.SORT_X);
        for (int j = 1; j < xyValues.length; j++) {
            if (rankPlot) {
                if (xyValues[j].x == xyValues[j - 1].x) {
                    xyValues[j - 1].x += Math.random() * 0.1 - 0.05;
                }
            }
        }
        */
        
        Arrays.sort(xyValues, org.geworkbench.util.pathwaydecoder.RankSorter.SORT_X);
        for (int j = 0; j < xyValues.length; j++) {
            if (showAll || xyValues[j].isActive() || xyValues[j].isFiltered()) {
                xyValues[j].ix = rank++;
            }
        }
        double maxX = xyValues[xyValues.length - 1].x;

        boolean panelsUsed = false;
        int panelIndex = 0;
        // If phenotypic panels have been selected
        if (panelsSelected) {
            for (int pId = 0; pId < dataSetView.getItemPanel().panels().size(); pId++) {
                ArrayList<RankSorter> list = new ArrayList<RankSorter>();
                DSPanel<DSMicroarray> panel = dataSetView.getItemPanel().panels().get(pId);
                int itemNo = panel.size();
                if (panel.isActive() && itemNo > 0) {
                    panelIndex++;
                    XYSeries series = new XYSeries(panel.getLabel());
                    for (int i = 0; i < itemNo; i++) {
                        int serial = panel.get(i).getSerial();
                        RankSorter xy = (RankSorter) map.get(new Integer(serial));
                        xy.setPlotted();
                        list.add(xy);
                        double x = 0;
                        double y = 0;
                        if (rankPlot) {
                            x = xy.ix;
                            y = xy.iy;
                            series.add(x, y);
                        } else {
                            //if ( (x < 4000) && (y < 4000)) {
                            x = xy.x;
                            y = xy.y;
                            series.add(x, y);
                            //}
                        }
                    }
                    //plots.addSeries(series);
                    seriesList.add(series);
                    PanelVisualProperties properties = propertiesManager.getVisualProperties(panel);
                    if (properties == null) {
                        properties = propertiesManager.getDefaultVisualProperties(panelIndex);
                    }
                    propertiesList.add(properties);
                    Collections.sort(list, org.geworkbench.util.pathwaydecoder.RankSorter.SORT_X);
                    xyPoints.add(list);
                }
            }
        }
        // finally if all the others must be shown as well
        if (showAll) {
            ArrayList<RankSorter> list = new ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter>();
            XYSeries series = new XYSeries(panelsUsed ? "Other" : "All");
            for (int serial = 0; serial < xyValues.length; serial++) {
                if (!xyValues[serial].isPlotted()) {
                    list.add(xyValues[serial]);
                    double x = 0;
                    double y = 0;
                    if (rankPlot) {
                        x = xyValues[serial].ix;
                        y = xyValues[serial].iy;
                        series.add(x, y);
                    } else {
                        //if ( (x < 4000) && (y < 4000)) {
                        x = xyValues[serial].x;
                        y = xyValues[serial].y;
                        series.add(x, y);
                        //}
                    }
                }
            }
            xyPoints.add(0, list);
            plots.addSeries(series);
        }
        for (int i = 0; i < seriesList.size(); i++) {
            XYSeries series = (XYSeries) seriesList.get(i);
            plots.addSeries(series);
        }
        String label1 = "";
        String label2 = "";
        label1 = maSet.getMarkers().get(marker1).getLabel();
        label2 = maSet.getMarkers().get(marker2).getLabel();
        chartData.setXLabel(label1);
        chartData.setYLabel(label2);
        JFreeChart mainChart = ChartFactory.createScatterPlot("", label1, label2, plots, PlotOrientation.VERTICAL, true, true, false); // Title, (, // X-Axis label,  Y-Axis label,  Dataset,  Show legend
        XYLineAnnotation annotation = chartGroups.get(PlotType.MARKER).lineAnnotation;
        if (annotation != null) {
            mainChart.getXYPlot().addAnnotation(annotation);
        }
        chartData.setXyPoints(xyPoints);
        StandardXYToolTipGenerator tooltips = new GeneXYToolTip(chartData);
        StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES, tooltips);
        for (int i = 0; i < propertiesList.size(); i++) {
            PanelVisualProperties panelVisualProperties = propertiesList.get(i);
            // Note: "i+1" because we skip the default 'other' series.
            int index = showAll ? i + 1 : i;
            renderer.setSeriesPaint(index, panelVisualProperties.getColor());
            renderer.setSeriesShape(index, panelVisualProperties.getShape());

        }
        mainChart.getXYPlot().setRenderer(renderer);
        //BufferedImage image = mainChart.createBufferedImage(width, height);
        //return image;
        return mainChart;
    }

    /**
     * @todo Merge this method with {@link #createGeneChart(int, int, org.geworkbench.components.plots.ScatterPlot.ChartData)} .
     */
    private JFreeChart createMicroarrayChart(int exp1, int exp2, ChartData chartData) throws SeriesException {
        XYSeriesCollection plots = new XYSeriesCollection();
        ArrayList seriesList = new ArrayList();
        ArrayList<PanelVisualProperties> propertiesList = new ArrayList<PanelVisualProperties>();
        PanelVisualPropertiesManager propertiesManager = PanelVisualPropertiesManager.getInstance();
        boolean showAll = allMarkersCheckBox.isSelected();
        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) dataSetView.getDataSet();
        boolean rankPlot = rankStatisticsCheckbox.isSelected();
        HashMap map = new HashMap();
        int numMarkers = maSet.getMarkers().size();
        // First put all the gene pairs in the xyValues array
        RankSorter[] xyValues = new RankSorter[numMarkers];
        ArrayList<ArrayList<RankSorter>> xyPoints = new ArrayList<ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter>>();
        DSMicroarray ma1 = maSet.get(exp1);
        DSMicroarray ma2 = maSet.get(exp2);
        for (int i = 0; i < numMarkers; i++) {
            xyValues[i] = new RankSorter();
            xyValues[i].x = ma1.getMarkerValue(i).getValue();
            xyValues[i].y = ma2.getMarkerValue(i).getValue();
            xyValues[i].id = i;
            map.put(new Integer(i), xyValues[i]);
        }
        boolean panelsSelected = (dataSetView.getMarkerPanel() != null) && (dataSetView.getMarkerPanel().size() > 0) && (dataSetView.getMarkerPanel().getLabel().compareToIgnoreCase("Unsupervised") != 0) && (dataSetView.getMarkerPanel().panels().size() > 0);
        if ((dataSetView.getMarkerPanel() != null) && (dataSetView.getMarkerPanel().activeSubset().size() == 0)) {
            panelsSelected = false;
            showAll = true;
        }
        if (rankPlot && !showAll) {
            // Must first activate all valid points
            if ((dataSetView.getMarkerPanel().size() > 0) && (dataSetView.getMarkerPanel().panels().size() > 0)) {
                for (int pId = 0; pId < dataSetView.getMarkerPanel().panels().size(); pId++) {
                    DSPanel<DSGeneMarker> panel = dataSetView.getMarkerPanel().panels().get(pId);
                    int itemNo = panel.size();
                    if (itemNo > 0) {
                        for (int i = 0; i < itemNo; i++) {
                            int serial = panel.get(i).getSerial();
                            xyValues[serial].setActive(true);
                        }
                    }
                }
            }
        }

        // Perform rank sorting if required
        int rank = 0;
        /*
        Arrays.sort(xyValues, RankSorter.SORT_Y);
        for (int j = 1; j < xyValues.length; j++) {
            if (rankPlot) {
                if (xyValues[j].y == xyValues[j - 1].y) {
                    xyValues[j - 1].y += Math.random() * 0.1 - 0.05;
                }
            }
        }
        */
        Arrays.sort(xyValues, RankSorter.SORT_Y);
        for (int j = 0; j < xyValues.length; j++) {
            if (showAll || xyValues[j].isActive() || xyValues[j].isFiltered()) {
                xyValues[j].iy = rank++;
            }
        }
        double maxY = xyValues[xyValues.length - 1].y;

        rank = 0;
        /*
        Arrays.sort(xyValues, RankSorter.SORT_X);
        for (int j = 1; j < xyValues.length; j++) {
            if (rankPlot) {
                if (xyValues[j].x == xyValues[j - 1].x) {
                    xyValues[j - 1].x += Math.random() * 0.1 - 0.05;
                }
            }
        }
        */
        Arrays.sort(xyValues, RankSorter.SORT_X);
        for (int j = 0; j < xyValues.length; j++) {
            if (showAll || xyValues[j].isActive() || xyValues[j].isFiltered()) {
                xyValues[j].ix = rank++;
            }
        }
        double maxX = xyValues[xyValues.length - 1].x;

        int panelIndex = 0;
        boolean panelsUsed = false;
        // If gene panels have been selected
        if (panelsSelected) {
            panelsUsed = true;
            for (int pId = 0; pId < dataSetView.getMarkerPanel().panels().size(); pId++) {
                ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter> list = new ArrayList<RankSorter>();
                DSPanel<DSGeneMarker> panel = dataSetView.getMarkerPanel().panels().get(pId);
                int itemNo = panel.size();
                if (panel.isActive() && itemNo > 0) {
                    panelIndex++;
                    XYSeries series = new XYSeries(panel.getLabel());
                    for (int i = 0; i < itemNo; i++) {
                        int serial = panel.get(i).getSerial();
                        RankSorter xy = (RankSorter) map.get(new Integer(serial));
                        xy.setPlotted();
                        list.add(xy);
                        double x = 0;
                        double y = 0;
                        if (rankPlot) {
                            x = xy.ix;
                            y = xy.iy;
                            series.add(x, y);
                        } else {
                            //if ( (x < 4000) && (y < 4000)) {
                            x = xy.x;
                            y = xy.y;
                            series.add(x, y);
                            //}
                        }
                    }
                    //plots.addSeries(series);
                    seriesList.add(series);
                    PanelVisualProperties properties = propertiesManager.getVisualProperties(panel);
                    if (properties == null) {
                        properties = propertiesManager.getDefaultVisualProperties(panelIndex);
                    }
                    propertiesList.add(properties);
                    Collections.sort(list, RankSorter.SORT_X);
                    xyPoints.add(list);
                }
            }
        }
        // finally if all the others must be shown as well
        if (showAll) {
            ArrayList<RankSorter> list = new ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter>();
            XYSeries series = new XYSeries(panelsUsed ? "Other" : "All");
            for (int serial = 0; serial < xyValues.length; serial++) {
                if (!xyValues[serial].isPlotted()) {
                    list.add(xyValues[serial]);
                    double x = 0;
                    double y = 0;
                    if (rankPlot) {
                        x = xyValues[serial].ix;
                        y = xyValues[serial].iy;
                        series.add(x, y);
                    } else {
                        //if ( (x < 4000) && (y < 4000)) {
                        x = xyValues[serial].x;
                        y = xyValues[serial].y;
                        series.add(x, y);
                        //}
                    }
                }
            }
            xyPoints.add(0, list);
            plots.addSeries(series);
        }
        for (int i = 0; i < seriesList.size(); i++) {
            XYSeries series = (XYSeries) seriesList.get(i);
            plots.addSeries(series);
        }
        String label1 = "";
        String label2 = "";
        label1 = ma1.getLabel();
        label2 = ma2.getLabel();
        chartData.setXLabel(label1);
        chartData.setYLabel(label2);
        JFreeChart mainChart = ChartFactory.createScatterPlot("", label1, label2, plots, PlotOrientation.VERTICAL, true, true, false); // Title, (, // X-Axis label,  Y-Axis label,  Dataset,  Show legend
        XYLineAnnotation annotation = chartGroups.get(PlotType.ARRAY).lineAnnotation;
        if (annotation != null) {
            mainChart.getXYPlot().addAnnotation(annotation);
        }
        chartData.setXyPoints(xyPoints);
        StandardXYToolTipGenerator tooltips = new MicroarrayXYToolTip(chartData);
        StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES, tooltips);
        for (int i = 0; i < propertiesList.size(); i++) {
            PanelVisualProperties panelVisualProperties = propertiesList.get(i);
            // Note: "i+1" because we skip the default 'other' series.
            int index = showAll ? i + 1 : i;
            renderer.setSeriesPaint(index, panelVisualProperties.getColor());
            renderer.setSeriesShape(index, panelVisualProperties.getShape());
        }
        mainChart.getXYPlot().setRenderer(renderer);
        //BufferedImage image = mainChart.createBufferedImage(width, height);
        //return image;
        return mainChart;
    }

    @Publish public org.geworkbench.events.MarkerSelectedEvent publishMarkerSelectedEvent(MarkerSelectedEvent event) {
        return event;
    }

    @Publish public PhenotypeSelectedEvent publishPhenotypeSelectedEvent(PhenotypeSelectedEvent event) {
        return event;
    }
    
    private boolean isInMicroarrayPanel(DSPanel<DSMicroarray> mas, String label){
    	for(DSMicroarray ma: mas){
    		if(StringUtils.equals(ma.getLabel().trim(), label.trim())) 
    			return true;
    	}
    	return false;
    }
    
    private boolean isInMarkerPanel(DSPanel<DSGeneMarker> markers, String label){
    	for(DSGeneMarker m: markers){
    		if(StringUtils.equals(m.getLabel().trim(), label.trim())) 
    			return true;
    	}
    	return false;
    }

}
