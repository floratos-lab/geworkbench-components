package org.geworkbench.components.ei;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.util.JAutoList;
import org.geworkbench.util.PrintUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import edu.columbia.c2b2.evidenceinegration.Evidence;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Sep 11, 2007
 * Time: 12:24:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenericDisplayPanel extends JPanel {

	private static final long serialVersionUID = -1061818066699969605L;

	static Log log = LogFactory.getLog(GenericDisplayPanel.class);

    private EvidenceIntegrationVisualization evidenceIntegrationVisualization;

    private List<Evidence> evidencesStringList;
    private List<String> goldStrArrayList;
    private List<Evidence> currentSelectedEvidences;
    private List<Evidence> currentSelectedGoldStrs;
    private HashMap<Integer, XYSeriesCollection> gsPlotData;
    private HashMap<Integer, XYSeriesCollection> gsPlotDataFilteredWithThreshold;
    public static final int DEFAULT_MAXIMUM_CHARTS = 4;
    public static final double MINIUMGAP = 0.05;
    private int currentMaximumCharts = DEFAULT_MAXIMUM_CHARTS;


    /**
     * Maximum number of charts that can be viewed at once.
     */
    public int getCurrentMaximumCharts() {
        return currentMaximumCharts;
    }

    public void setCurrentMaximumCharts(int currentMaximumCharts) {
        this.currentMaximumCharts = currentMaximumCharts;
    }


    /**
     * The two types of plots.
     * <ul>
     * <li>PERF - Plots the values of performance for every selected Gold standard and multi-Evidence.
     * <li>ROC - Plots the values of Roc curce for one gold standard
     * </ul>
     */
    public enum PlotType {
        PERF, ROC
    }

    /**
     * The two type input
     */
    public enum ListType {
        GOLD, EVIDENCE
    }

    ;
    Map<Integer, String> currentGoldSMap;
    private JSplitPane performancePanel;

    private JPanel evidenceListPanel;
    private JLabel evdienceLabel;
    private JPanel goldListPanel;
    private JLabel goldLabel;
    private JPanel evidenceListPanel1;

    private JSlider thresholdSlider = new JSlider();
    private JTextField thresholdTextField = new JTextField();

    private JAutoList evidenceList;
    private JAutoList goldStrList;
    private JPanel chartPanel, topChartPanel, bottomChartPanel;

    private JButton clearButton;
    private JButton printButton;
    private JButton createMatrixtButton;
    private JButton imageSnapshotButton;

    private JPopupMenu popupMenu;
    private PlotType popupType;
    private PlotType currentType;
    private int popupIndex;
    private JLabel thresholdLabel;
    private DecimalFormat myFormatter = new DecimalFormat("0.00");
    private double currentThresholdValue = 0;
    private Integer currentGSNumber;
    private String currentTargetGSName;
    JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    /**
     * The two chart groups (one for microarrays, one for markers).
     */
    private EnumMap<PlotType, ChartGroup> chartGroups;

    private GoldStrListModel goldStrModel = new GoldStrListModel();
    private EvidenceListModel evidenceModel = new EvidenceListModel();


    public GenericDisplayPanel(AbstractListModel firstListModel, AbstractListModel secondListModel) {
        super();
        goldStrModel = (GoldStrListModel) firstListModel;

    }

    /**
     * Construct a specific plot type display panel.
     * @param type
     * @param newEvidence
     * @param newGoldSMap
     */
    public GenericDisplayPanel(EvidenceIntegrationVisualization evidenceIntegrationVisualization, PlotType type, List<Evidence> newEvidence, Map<Integer, String> newGoldSMap) {
        this.evidenceIntegrationVisualization = evidenceIntegrationVisualization;
        currentType = type;
        goldStrArrayList = new ArrayList<String>();
        currentGoldSMap = newGoldSMap;
        Set<Integer> set = newGoldSMap.keySet();
        for (Integer in : set) {
            goldStrArrayList.add(newGoldSMap.get(in));
        }
        evidencesStringList = newEvidence;
        currentSelectedEvidences = new ArrayList<Evidence>();
        currentSelectedGoldStrs = new ArrayList<Evidence>();
        init();
    }

    /**
     * Constructor lays out the component and adds behaviors.
     */
    public GenericDisplayPanel() {
        init();
    }

    /**
     * Main GUI building block.
     */
    public void init() {
        //// Create empty chart groups
        chartGroups = new EnumMap<PlotType, ChartGroup>(PlotType.class);
        chartGroups.put(PlotType.PERF, new ChartGroup());
        chartGroups.put(PlotType.ROC, new ChartGroup());
        mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        performancePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        mainPanel.setOneTouchExpandable(true);
        JPanel westPanel = new JPanel(new BorderLayout());
        mainPanel.add(westPanel);

        westPanel.add(performancePanel, BorderLayout.CENTER);
        makeDefaultMicroArrayLists();
        makeDefaultMarkerLists();
        evidenceListPanel = new JPanel();
        evidenceListPanel1 = new JPanel();
        evdienceLabel = new JLabel("<html><font color=blue><B>Evidence</b></font></html>");

        evidenceListPanel.setLayout(new BorderLayout());
        evidenceListPanel.add(evdienceLabel, BorderLayout.NORTH);
        evidenceListPanel.add(evidenceList, BorderLayout.CENTER);

        evidenceListPanel1.setLayout(new BorderLayout());

        goldListPanel = new JPanel();

        goldLabel = new JLabel("<html><font color=blue><B>Gold Standards </b></font></html>");

        goldListPanel.setLayout(new BorderLayout());
        goldListPanel.add(goldLabel, BorderLayout.NORTH);
        goldListPanel.add(goldStrList, BorderLayout.CENTER);
        performancePanel.add(evidenceListPanel);
        performancePanel.add(goldListPanel);

        // Right side:
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(Box.createGlue());
        rightPanel.add(topPanel, BorderLayout.NORTH);
        chartPanel = new JPanel();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
        topChartPanel = new JPanel();
        topChartPanel.setLayout(new BoxLayout(topChartPanel, BoxLayout.X_AXIS));
        bottomChartPanel = new JPanel();
        bottomChartPanel.setLayout(new BoxLayout(bottomChartPanel, BoxLayout.X_AXIS));
        chartPanel.add(topChartPanel);
        chartPanel.add(bottomChartPanel);
        rightPanel.add(chartPanel, BorderLayout.CENTER);
        JPanel bottomSpacingPanel = new JPanel();
        bottomSpacingPanel.setLayout(new BoxLayout(bottomSpacingPanel, BoxLayout.Y_AXIS));
        bottomSpacingPanel.add(Box.createVerticalStrut(6));
        JPanel bottomPanel = new JPanel();
        JToolBar thresholdPanel = new JToolBar();
          bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        createMatrixtButton = new JButton("Create Matrix");
        createMatrixtButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createMatrix();
            }
        });
           if (currentType != null && currentType.equals(PlotType.PERF)) {
            bottomPanel.add(Box.createHorizontalStrut(60));
            // bottomPanel.add(slopeLabel);
            bottomPanel.add(Box.createHorizontalStrut(5));
            // bottomPanel.add(slopeField);
            bottomPanel.add(Box.createGlue());
        } else if (currentType != null && currentType.equals(PlotType.ROC)) {
            thresholdLabel = new JLabel("Threshold");
            thresholdSlider = new JSlider(0, 100);

            thresholdTextField = new JTextField(".00", 4);
            thresholdTextField.setMaximumSize(new Dimension(20, 20));
            thresholdTextField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jThresholdTextField_actionPerformed();
                }
            });
            thresholdTextField.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e) {
                    jThresholdTextField_actionPerformed();

                }
            });

            thresholdSlider = new JSlider();
            thresholdSlider.setValue(0);
            thresholdSlider.setMinimum(0);
            thresholdSlider.setMaximum(100);
            thresholdSlider.setSnapToTicks(true);
            thresholdSlider.setPaintTicks(true);
            thresholdSlider.setMinorTickSpacing(1);
            thresholdSlider.setMajorTickSpacing(5);
            thresholdSlider.setCursor(java.awt.Cursor.getPredefinedCursor(java.
                    awt.Cursor.HAND_CURSOR));
            thresholdSlider.addChangeListener(new javax.swing.event.
                    ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    thresholdSlider_stateChanged(e);
                }
            });
            thresholdSlider.setToolTipText(
                    "Move the slider to change the threshold.");

            thresholdPanel.add(thresholdLabel);
            thresholdPanel.add(thresholdTextField);
            thresholdPanel.add(thresholdSlider);
            bottomPanel.add(createMatrixtButton);
            bottomSpacingPanel.add(thresholdPanel);


        }
        bottomSpacingPanel.add(bottomPanel);
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

        // Use these listeners to effectively prevent selection (we handle that specially ourselves)
        evidenceList.getList().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // evidenceList.getList().clearSelection();
            }
        });
        goldStrList.getList().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // goldStrList.getList().clearSelection();
            }
        });

        xAxisItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setNewXAxis(popupType, popupIndex);
            }
        });
        // Popup action for clearing all charts.
        clearAllItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAllCharts(popupType);
                if (popupType == PlotType.PERF) {
                    evidenceModel.refresh();
                } else {
                    goldStrModel.refresh();
                }
                packChartPanel(popupType);
            }
        });
        // Clear button action
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAllCharts(getActivePlotType());
                packChartPanel(getActivePlotType());
                if (getActivePlotType() == PlotType.ROC) {
                    goldStrModel.refresh();
                } else {
                    evidenceModel.refresh();
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
        createMatrixtButton.setEnabled(false);
        // Initialize chart panel
        packChartPanel(PlotType.PERF);
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
    }

    private void makeDefaultMarkerLists() {
        goldStrList = new GoldAutoList();
        goldStrList.getList().setCellRenderer(new CellRenderer());
        goldStrList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        goldStrList.getList().setFixedCellWidth(250);
    }

    private void makeDefaultMicroArrayLists() {
        evidenceList = new EvidenceAutoList();
        evidenceList.getList().setCellRenderer(new CellRenderer());
        evidenceList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        evidenceList.getList().setFixedCellWidth(250);

    }

    private void thresholdSlider_stateChanged(ChangeEvent e) {
        int value = thresholdSlider.getValue();

        double lowValue = (double) value / 100;

        String s = myFormatter.format(lowValue);
        thresholdTextField.setText(s);
        if (applyThresholdFilter()) {
            updateROCCharts();
        }
        ;

    }

    private double getCurrentThreshold() {
        int value = thresholdSlider.getValue();

        currentThresholdValue = (double) value / 100;
        return currentThresholdValue;
    }

    /**
     * Respond to the change of the Threshold Text field.
     *
     * @param
     */
    public void jThresholdTextField_actionPerformed() {
        double newvalue = 0;
        try {
            newvalue = new Double(thresholdTextField.getText().trim());
        } catch (NumberFormatException e1) {
            JOptionPane.showMessageDialog(null, "The input is not a number.", "Please check your input.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double newSliderValue = newvalue * 100;
        thresholdSlider.setValue((int) newSliderValue);
        thresholdSlider_stateChanged(null);

    }

    /**
     * Adds the appropriate chart panels to the component.
     * Call this whenever charts are added or removed.
     * Charts are layed out in a single row (1 or 2 charts) or two rows (3 to 6 charts) depends on currentMAXIMUM varible.
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
            createMatrixtButton.setEnabled(false);
            thresholdSlider.setEnabled(false);
            thresholdTextField.setEnabled(false);
        } else {
            // Enable buttons
            clearButton.setEnabled(true);
            printButton.setEnabled(true);
            imageSnapshotButton.setEnabled(true);
            createMatrixtButton.setEnabled(true);
            thresholdSlider.setEnabled(true);
            thresholdTextField.setEnabled(true);
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

					private static final long serialVersionUID = 7775097314187933184L;

					@Override
                    public Dimension getPreferredSize() {
                        return template.getPreferredSize();
                    }

                    @Override
                    public Dimension getMaximumSize() {
                        return template.getMaximumSize();
                    }

                    @Override
                    public Dimension getMinimumSize() {
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
        //updateCharts(type);//removed by xz.
        if (chartRemoved) {
            packChartPanel(type);
        }
        if (type == PlotType.PERF) {
            evidenceModel.refresh();
        } else {
            goldStrModel.refresh();
        }
    }

    /**
     * Called when an item is clicked in either the Evidence or Gold Standard list.
     *
     * @param type  the type of item clicked.
     * @param index the index of the item.
     */
    private void itemClicked(ListType listType, PlotType type, int index) {

        ChartGroup group = chartGroups.get(type);
        if (group.charts != null && group.charts.size() > 0) {
            int num = group.charts.size() - 1;
            for (int i = num; i >= 0; i--) {
                group.charts.remove(i);
            }
        }

        /**
         * reset the charts.
         */

        if (listType.equals(ListType.GOLD)) {

            if (currentSelectedGoldStrs.size() >= currentMaximumCharts) {
                currentSelectedGoldStrs.remove(0);
            }
            String newGolS = goldStrArrayList.get(index);
            if (currentSelectedGoldStrs.contains(newGolS)) {
                currentSelectedGoldStrs.remove(newGolS);
            } else {
            	// FIXME this would not work
                //currentSelectedGoldStrs.add(newGolS);
            	log.error("incompatible types for evidence list");
            }

        } else {
            Evidence newGolS = evidencesStringList.get(index);
            if (currentSelectedEvidences.contains(newGolS)) {
                currentSelectedEvidences.remove(newGolS);
            } else {
                currentSelectedEvidences.add(newGolS);
            }
        }
        Object[] selectedIndices = currentSelectedGoldStrs.toArray();
        for (Object selectedGoldStr : selectedIndices) {
                  Chart attributes;
            if (group.charts.size() == currentMaximumCharts) {
                attributes = group.charts.remove(0);
            } else {
                // Otherwise, create a new one.
                attributes = new Chart();
            }
            if (listType.equals(ListType.GOLD)) {
                attributes.index = index;
            }

            group.charts.add(attributes);
            JFreeChart chart;
            chart = createChart(type, currentSelectedEvidences, selectedGoldStr, attributes.chartData);
            if (attributes.panel == null) {
                attributes.panel = new ChartPanel(chart);
                if (type == PlotType.PERF) {
                    attributes.panel.addChartMouseListener(new MicroarrayChartMouseListener(attributes.chartData));
                } else {
                    attributes.panel.addChartMouseListener(new GeneChartMouseListener(attributes.chartData));
                }
            } else {
                attributes.panel.setChart(chart);
            }
        }
        packChartPanel(type);
        this.revalidate();
        this.repaint();

    }

    private void clearAllCharts(PlotType type) {
        ChartGroup chartGroup = chartGroups.get(type);

        chartGroup.charts.clear();
    }

    private PlotType getActivePlotType() {
        return currentType;
    }

    @Publish
    public void createImageSnapshot() {
        Dimension panelSize = chartPanel.getSize();
        BufferedImage image = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        chartPanel.paint(g);
        ImageIcon icon = new ImageIcon(image, "Evidence Integration");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("Scatter Plot Snapshot", icon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
        evidenceIntegrationVisualization.createImageSnapshot(event);
    }

    @Publish
    public ImageSnapshotEvent createMatrix() {
        Dimension panelSize = chartPanel.getSize();
        BufferedImage image = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        chartPanel.paint(g);
        ImageIcon icon = new ImageIcon(image, "Evidence Integration");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("Scatter Plot Snapshot", icon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
        evidenceIntegrationVisualization.createImageSnapshot(event);
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
    @Subscribe
    public void receive(org.geworkbench.events.GeneSelectorEvent e, Object source) {
        if (e.getPanel() != null) {
            goldStrModel.refresh();
        }
        //updateCharts(PlotType.PERF); removed by xz
    }

    /**
     * Receives a phenotype selection event.
     * The list of microarrays is updated and marker plots are updated to reflect the selection.
     *
     * @param e      the event.
     * @param source the source of the event (unused).
     */
    @Subscribe
    public void receive(org.geworkbench.events.PhenotypeSelectorEvent<DSMicroarray> e, Object source) {
        if (e.getTaggedItemSetTree() != null) {
            evidenceModel.refresh();
            // All marker charts must be updated
        }
        //  updateCharts(PlotType.ROC);  removed by xz.
    }

    private JFreeChart createChart(PlotType plotType, List<Evidence> list, Object targetGSName, ChartData chartData) throws SeriesException {

        gsPlotData = new HashMap<Integer, XYSeriesCollection>();
        gsPlotDataFilteredWithThreshold = new HashMap<Integer, XYSeriesCollection>();
        JFreeChart ch = null;
        List<Evidence> listEvidences = list;

        if (plotType.equals(PlotType.PERF)) {
            for (Evidence evidence : listEvidences) {
                Map<Integer, Map<Integer, Float>> binPerformance = evidence.getBinPerformance();
                for (Map.Entry<Integer, Map<Integer, Float>> gsValues : binPerformance.entrySet()) {
                    Integer goldStandardID = gsValues.getKey();
                    String gsName = currentGoldSMap.get(goldStandardID);

                    XYSeriesCollection gsSeries = gsPlotData.get(goldStandardID);
                    if (gsSeries == null) {
                        gsSeries = new XYSeriesCollection();
                    }
                    if (targetGSName.equals(gsName)) {
                        XYSeries series = new XYSeries("" + evidence.getName());
                        for (Map.Entry<Integer, Float> binValue : gsValues.getValue().entrySet()) {
                            series.add(binValue.getKey(), binValue.getValue());
                        }
                        gsSeries.addSeries(series);
                        gsPlotData.put(goldStandardID, gsSeries);
                    }
                }

            }


            for (Map.Entry<Integer, XYSeriesCollection> gsEntry : gsPlotData.entrySet()) {
                // Draw graphs for each Gold Standard set
                // if (targetGSName.toString().equals(gsEntry.getKey())) {
                ch = ChartFactory.createXYLineChart(targetGSName.toString(),
                        "Bin",
                        "Value", // Y-Axis label
                        gsEntry.getValue(), // Dataset
                        PlotOrientation.VERTICAL, true, // Show legend
                        true, true);

// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
                ch.setBackgroundPaint(Color.white);

                // get a reference to the plot for further customisation...
                XYPlot newPlot = (XYPlot) ch.getPlot();
                Color c = UIManager.getColor("Panel.background");
                if (c != null) {
                    newPlot.setBackgroundPaint(c);
                } else {
                    c = Color.white;
                }
                newPlot.setBackgroundPaint(c);
 //               newPlot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
//                newPlot.setDomainGridlinePaint(Color.white);
//                newPlot.setRangeGridlinePaint(Color.white);
                newPlot.setDomainCrosshairVisible(true);
                newPlot.setDomainCrosshairLockedOnData(true);
                //Set up fixed ranges.
                //        ValueAxis xaxis = new NumberAxis();
                //        xaxis.setRange(minValue, maxValue);
                //        newPlot.setRangeAxis(xaxis);
                XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) newPlot.
                        getRenderer();
             //  renderer.setShapesVisible(true);
                //renderer.setShapesFilled(true);
                boolean isToolTipEnabled = true;
                if (isToolTipEnabled) {

                    renderer.setToolTipGenerator(new XYToolTipGenerator() {


                        public String generateToolTip(XYDataset dataset, int series,
                                                      int item) {
                            String resultStr = "";

                            String label = "";
                            double x = dataset.getXValue(series, item);
                            if (Double.isNaN(x) && dataset.getX(series, item) == null) {
                                return resultStr;
                            }

                            double y = dataset.getYValue(series, item);
                            if (Double.isNaN(y) && dataset.getX(series, item) == null) {
                                return resultStr;
                            }
                            String xStr = myFormatter.format(x);
                            String yStr = myFormatter.format(y);
                            return resultStr = label + "(" + xStr + ", " +
                                    yStr + ")";
                        }
                    });
                }
                renderer.setSeriesLinesVisible(0, true);
                for (int i = 1; i < newPlot.getDatasetCount(); i++) {
                    renderer.setSeriesLinesVisible(i, true);
                }

                //base color & shape
                // renderer.setSeriesPaint(0, baseColor);
                //        renderer.setSeriesShape(0, baseShape);


                newPlot.setRenderer(renderer);


                return ch;
            }

            //End of PER curve.
        } else {
            //ROC curve

            XYSeriesCollection gsSeries = new XYSeriesCollection();
            for (Map.Entry<Integer, String> gsEntry : currentGoldSMap.entrySet()) {
                Integer currentGSIndex = gsEntry.getKey();
                String gsName = currentGoldSMap.get(currentGSIndex);
                if (gsName.equalsIgnoreCase(targetGSName.toString())) {
                    currentGSNumber = currentGSIndex;
                    currentTargetGSName = gsName;
                    for (Evidence evidence : listEvidences) {
                        XYSeries series = new ROCComputation().getXYSeries(MINIUMGAP, evidence, currentGSIndex);
                        gsSeries.addSeries(series);
                    }
                    gsPlotData.put(currentGSIndex, gsSeries);
                }

            }
            //  applyThresholdFilter(listEvidences, targetGSName.toString())

            if (applyThresholdFilter()) {
                return createROCChart();
            }
        }
        return ch;

    }

    private boolean applyThresholdFilter() {
        getCurrentThreshold();
        gsPlotDataFilteredWithThreshold = new HashMap<Integer, XYSeriesCollection>();
        int startPoint = (int) (currentThresholdValue / MINIUMGAP);
        int endPoint = (int) (1 / MINIUMGAP) -1;
        Integer goldStandardID = 0;
        XYSeriesCollection gsSeries = gsPlotData.get(currentGSNumber);
        if (gsSeries == null) {
            return false;

        }
        XYSeriesCollection currrentGsSeries = new XYSeriesCollection();
        for (Object series : gsSeries.getSeries()) {
            if (series instanceof XYSeries) {
                try {
                    XYSeries xySeries = ((XYSeries) series).createCopy(startPoint, endPoint);
                    currrentGsSeries.addSeries(xySeries);
                } catch (CloneNotSupportedException e) {
                    return false;
                }
            }
        }
        gsPlotDataFilteredWithThreshold.put(goldStandardID, currrentGsSeries);
        return true;
    }

    private boolean updateROCCharts() {
        PlotType type = PlotType.ROC;
        JFreeChart chart = createROCChart();
        if (chart != null) {
            ChartGroup group = chartGroups.get(type);
            Chart attributes;
            if (group.charts.size() == currentMaximumCharts) {
                attributes = group.charts.remove(0);
            } else {
                // Otherwise, create a new one.
                attributes = new Chart();

            }
            if (attributes.panel == null) {
                attributes.panel = new ChartPanel(chart);
                //attributes.panel.addChartMouseListener(new GeneChartMouseListener(attributes.chartData));
            } else {
                attributes.panel.setChart(chart);
            }

            group.charts.add(attributes);
            packChartPanel(type);
            this.revalidate();
            this.repaint();
            return true;
        }
        return false;
    }

    private JFreeChart createROCChart() {
        if (gsPlotDataFilteredWithThreshold == null) {
            return null;
        }
        for (Map.Entry<Integer, XYSeriesCollection> gsEntry : gsPlotDataFilteredWithThreshold.entrySet()) {
            JFreeChart chart = ChartFactory.createXYLineChart(currentTargetGSName, // Title

                    "pp", // X-Axis label
                    "np", // Y-Axis label
                    gsEntry.getValue(), // Dataset
                    PlotOrientation.VERTICAL, true, // Show legend
                    true, true);
// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
            chart.setBackgroundPaint(Color.white);

            // get a reference to the plot for further customisation...
            XYPlot newPlot = (XYPlot) chart.getPlot();
            Color c = UIManager.getColor("Panel.background");
            if (c != null) {
                newPlot.setBackgroundPaint(c);
            } else {
                c = Color.white;
            }
            newPlot.setBackgroundPaint(c);
            newPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
            newPlot.setDomainGridlinePaint(Color.white);
            newPlot.setRangeGridlinePaint(Color.white);
            newPlot.setDomainCrosshairVisible(true);
            newPlot.setDomainCrosshairLockedOnData(true);
            //Set up fixed ranges.
            //        ValueAxis xaxis = new NumberAxis();
            //        xaxis.setRange(minValue, maxValue);
            //        newPlot.setRangeAxis(xaxis);
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) newPlot.
                    getRenderer();
            renderer.setShapesVisible(true);
            renderer.setShapesFilled(true);
            boolean isToolTipEnabled = true;
            if (isToolTipEnabled) {

                renderer.setToolTipGenerator(new XYToolTipGenerator() {


                    public String generateToolTip(XYDataset dataset, int series,
                                                  int item) {
                        String resultStr = "";

                        double currentValue = MINIUMGAP * item + currentThresholdValue;
                        String label = "Threshold: " + myFormatter.format(currentValue);
                        double x = dataset.getXValue(series, item);
                        if (Double.isNaN(x) && dataset.getX(series, item) == null) {
                            return resultStr;
                        }

                        double y = dataset.getYValue(series, item);
                        if (Double.isNaN(y) && dataset.getX(series, item) == null) {
                            return resultStr;
                        }
                        String xStr = myFormatter.format(x);
                        String yStr = myFormatter.format(y);
                        return resultStr = label + ": (" + xStr + ", " +
                                yStr + ")";
                    }
                });
            }
            renderer.setSeriesLinesVisible(0, true);
            for (int i = 1; i < newPlot.getDatasetCount(); i++) {
                renderer.setSeriesLinesVisible(i, true);
            }

            //base color & shape
            // renderer.setSeriesPaint(0, baseColor);
            //        renderer.setSeriesShape(0, baseShape);


            newPlot.setRenderer(renderer);


            return chart;
        }
        return null;
    }


    @Publish
    public org.geworkbench.events.MarkerSelectedEvent publishMarkerSelectedEvent
            (MarkerSelectedEvent
                    event) {
        return event;
    }

    @Publish
    public PhenotypeSelectedEvent publishPhenotypeSelectedEvent
            (PhenotypeSelectedEvent
                    event) {
        return event;
    }

    /**
     * ListModel for the marker list.
     */
    private class GoldStrListModel extends AbstractListModel {

		private static final long serialVersionUID = 6661612028150564738L;

		public int getSize() {
            if (goldStrArrayList == null) {
                return 0;
            }
            return goldStrArrayList.size();
        }

        public Object getElementAt(int index) {
            if (goldStrArrayList == null) {
                return null;
            }
            //@ temp solution xz.
            return goldStrArrayList.get(index);
        }

        /**
         * Indicates to the associated JList that the contents need to be redrawn.
         */
        public void refresh() {
            if (goldStrArrayList == null) {
                fireContentsChanged(this, 0, 0);
            } else {
                fireContentsChanged(this, 0, goldStrArrayList.size());
            }
        }

    }

    /**
     * ListModel for the microarray list.
     */
    private class EvidenceListModel extends AbstractListModel {

		private static final long serialVersionUID = 4982520718928284460L;

		public int getSize() {
            if (evidencesStringList == null) {
                return 0;
            }
            return evidencesStringList.size();
        }

        public Object getElementAt(int index) {
            if (evidencesStringList == null) {
                return null;
            }
            // return evidencesStringList.get(index).getName();
            if (evidencesStringList.get(index) instanceof Evidence) {
                return ((Evidence) (evidencesStringList.get(index))).getName();
            }
            return evidencesStringList.get(index);
        }

        /**
         * Indicates to the associated JList that the contents need to be redrawn.
         */
        public void refresh() {
            if (evidencesStringList == null) {
                fireContentsChanged(this, 0, 0);
            } else {
                fireContentsChanged(this, 0, evidencesStringList.size());
            }
        }

    }

    /**
     * Cell rendered for the lists that handles the special selection conditions.
     */
    private class CellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -1878430498341935169L;

		@Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);    //To change body of overridden methods use File | Settings | File Templates.

            boolean highlighted = false;
            // See if there is chart for this item
            if (list == evidenceList.getList()) {

                Object o = evidencesStringList.get(index);
                if (currentSelectedEvidences.contains(o)) {
                    highlighted = true;
                }
            } else {

                Object o = goldStrArrayList.get(index);
                if (currentSelectedGoldStrs.contains(o)) {
                    highlighted = true;
                }
            }
            if (highlighted) {
                label.setBackground(Color.LIGHT_GRAY);
            } else {
                label.setBackground(Color.WHITE);
            }

            return label;
        }
    }

    /**
     * The marker JAutoList type.
     */
    private class GoldAutoList extends JAutoList {

		private static final long serialVersionUID = 8259768674877458248L;

		public GoldAutoList() {
            super(goldStrModel);
            removeSearchBar();
        }

        public GoldAutoList(GoldStrListModel model) {
            super(model);
            removeSearchBar();
        }

        @Override
        protected void elementClicked(int index, MouseEvent e) {

            itemClicked(ListType.GOLD, currentType, index);
            goldStrModel.refresh();
        }

        @Override
        protected void elementRightClicked(int index, MouseEvent e) {
            itemRightClicked(PlotType.ROC, e, index);
        }
    }

    /**
     * The microarray JAutoList type.
     */
    private class EvidenceAutoList extends JAutoList {

		private static final long serialVersionUID = -1361509350865875531L;

		public EvidenceAutoList() {
            super(evidenceModel);
            removeSearchBar();
        }

        @Override
        protected void elementClicked(int index, MouseEvent e) {
            itemClicked(ListType.EVIDENCE, currentType, index);
            evidenceModel.refresh();
        }

        @Override
        protected void elementRightClicked(int index, MouseEvent e) {
            itemRightClicked(PlotType.PERF, e, index);
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

        public ArrayList<Chart> charts;

    }
//removed by xz.
/**
 * Cell rendered for the lists that handles the special selection conditions.
 */

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
//        private ArrayList<ArrayList<org.geworkbench.util.pathwaydecoder.RankSorter>> xyPoints;

        public ChartData() {
        }

        public DSGeneMarker getMarker(int series, int item) {
//            ArrayList<RankSorter> list = xyPoints.get(series);
//            org.geworkbench.util.pathwaydecoder.RankSorter rs = (RankSorter) list.get(item);
//            DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
//            if (maSet != null) {
//                if (rs.id < maSet.getMarkers().size()) {
//                    DSGeneMarker marker = maSet.getMarkers().get(rs.id);
//                    return marker;
//                }
//            }
            return null;
        }

        public DSMicroarray getMicroarray(int series, int item) {
//            ArrayList<RankSorter> list = xyPoints.get(series);
//            RankSorter rs = (org.geworkbench.util.pathwaydecoder.RankSorter) list.get(item);
//            DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
//            if (maSet != null) {
//                if (rs.id < maSet.size()) {
//                    DSMicroarray ma = maSet.get(rs.id);
//                    return ma;
//                }
//            }
            return null;
        }

    }

}
