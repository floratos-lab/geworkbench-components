package org.geworkbench.components.microarrays;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.visualproperties.PanelVisualProperties;
import org.geworkbench.util.visualproperties.PanelVisualPropertiesManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * EVD Panel to visualize microarray dataset.
 *
 * @author Xiaoqing Zhang
 */
@AcceptTypes({DSMicroarraySet.class})
public class EVDPanel implements VisualPlugin {
	private static Log log = LogFactory.getLog(EVDPanel.class);

	// constants
    private final static int DEFAULTBASKETNUM = 99;
    private final static DecimalFormat myFormatter = new DecimalFormat("0.000");
    private final static int MAXBINNUM = 200;
    
    // jfree.chart
    private final ChartPanel graph;
    
    // Swing
    private final JToggleButton jTTestBttn;
    private final JToggleButton jEnabledBox = new JToggleButton();

    private final JSlider jLeftBoundarySlider;
    private final JSlider jRightBoundarySlider;
    private final JSlider jBinSizeSlider;
    
    private final JLabel jBinSizeLabel = new JLabel("Total bins: 100");
    private final JCheckBox colorCheck = new JCheckBox("One color per array");

    private final JLabel leftBoundary;
    private final JLabel rightBoundary;
    private final JLabel selectedGeneNum;
    private final JLabel binNumLabel;
    
    private JSlider jMASlider;

    // properties
    private double lowValue = 0.0d;
    private double highValue = 0.0d;
    private double maxValue = 0.0d;
    private double minValue = 0.0d;
    
    private double minExpressionValue = 0;
    private double maxExpressionValue = 0;
    private int maxOccurrence = 0;

    private int basketNum = DEFAULTBASKETNUM;
    
    private double binSize = 0.1d;
    
    // others
    private ArrayList<PanelVisualProperties> propertiesList = new ArrayList<
            PanelVisualProperties>();
    private Histogram hs;
   
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView = null;

	private final JPanel mainPanel = new JPanel();

	@Override
	public Component getComponent() {
		return mainPanel;
	}

	/**
	 * Receive ProjectEvent.
	 *
	 */
	@Subscribe(Asynchronous.class)
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {

		DSDataSet<?> dataSet = e.getDataSet();
		if (dataSet instanceof DSMicroarraySet
				&& (maSetView == null || dataSet != maSetView
						.getMicroarraySet())) {
			maSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>((DSMicroarraySet) dataSet);
			refreshMaSetView(null, null);
		}
	}

	/**
	 * Receive GeneSelectorEvent.
	 *
	 */
	@Subscribe(Asynchronous.class)
	public void receive(GeneSelectorEvent e, Object source) {

		log.debug("Source object " + source);

		DSPanel<DSGeneMarker> markers = e.getPanel();
		DSPanel<DSGeneMarker> activatedMarkers = new CSPanel<DSGeneMarker>();
		if (markers != null && markers.size() > 0) {            
			for (int j = 0; j < markers.panels().size(); j++) {
				DSPanel<DSGeneMarker> mrk = markers.panels().get(j);
				if (mrk.isActive()) {
					for (int i = 0; i < mrk.size(); i++) {						
						activatedMarkers.add(mrk.get(i));

					}
				}
			}
		}
		DSPanel<DSMicroarray> p = null;
		if(maSetView!=null) p = maSetView.getItemPanel();
		refreshMaSetView(activatedMarkers,p);
	}

	/**
	 * Receive PhenotypeSelectorEvent.
	 *
	 */
	@Subscribe(Asynchronous.class)
	public void receive(org.geworkbench.events.PhenotypeSelectorEvent<DSMicroarray> e,
			Object source) {

		if (e.getTaggedItemSetTree() != null) {
			refreshMaSetView(null, e.getTaggedItemSetTree().activeSubset());
		} else {
			refreshMaSetView(null, null);
		}
	}

	private volatile boolean beingRefreshed = false;
	/**
	 * Refreshes the chart view when receiving a project event or a selector event.
	 */
	private void refreshMaSetView(final DSPanel<DSGeneMarker> activatedMarkers, final DSPanel<DSMicroarray> activatedArrays) {
		if(beingRefreshed || maSetView == null) {
			return;
		}
		
		beingRefreshed = true;

		if (activatedMarkers != null && activatedMarkers.panels().size() > 0)
			maSetView.setMarkerPanel(activatedMarkers);
		if (activatedArrays != null && activatedArrays.panels().size() > 0 && activatedArrays.size() > 0)
			maSetView.setItemPanel(activatedArrays);
		else
			maSetView.setItemPanel(new CSPanel<DSMicroarray>());

		if (maSetView.markers() == null) {
			return;
		}

		// using 0 instead of the actual minimum value is what the system test deems to be 'correct' behavior
		minExpressionValue = 0; //Double.MAX_VALUE;
		maxExpressionValue = Double.MIN_VALUE;
		maxOccurrence = 0;

		// FIXME there may be some more efficient way
		DSItemList<DSMicroarray> microarrays = maSetView.items();
		int numArraySets = microarrays.size();
		for (int maCtr = 0; maCtr < numArraySets; maCtr++) {
			DSMicroarray ma = microarrays.get(maCtr);
			for (DSMarkerValue v : ma.getMarkerValues()) {
				double value = v.getValue();
				if (Double.isNaN(value)) {
					value = 0;
				} else if (value > maxExpressionValue) {
					maxExpressionValue = value;
				} else if (value < minExpressionValue) {
					minExpressionValue = value;
				}
			}
		}
		/* Note: Because CSItemList does not follow Java collections' normal behavior,
		 * the regular for-each loop or loop via iterator does not work here. */
		for(int index=0; index<microarrays.size(); index++) {
			DSMicroarray currentMicroarray = microarrays.get(index);
            hs = new Histogram(basketNum, currentMicroarray, minExpressionValue, maxExpressionValue, maSetView.markers());
            int[] basketValues = hs.getBasketvalues();
            for (int i = 0; i <= basketNum; i++) {
                if (basketValues[i] > 0) {
                    if (basketValues[i] > maxOccurrence)
                    	maxOccurrence = basketValues[i];
                }
            }
		}

		if(jMASlider!=null)
			jMASlider.setMaximum(numArraySets-1);
		
		refresh();
		
		beingRefreshed = false;
	}

	/** Constructor. */
	public EVDPanel() {

		JToolBar jToolBar3 = new JToolBar();

		BorderLayout borderLayout2 = new BorderLayout();
		mainPanel.setLayout(borderLayout2);

		mainPanel.add(jToolBar3, java.awt.BorderLayout.SOUTH);

        JButton jPrintBttn = new JButton();
        jPrintBttn.setText("Print");
        jPrintBttn.setToolTipText("Print The Chart.");
        jPrintBttn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                org.geworkbench.util.PrintUtils.printComponent(graph);
            }
        });

        jTTestBttn = new JToggleButton("T-Test");
        jTTestBttn.setToolTipText("Two-sample TTest Only.");
        jTTestBttn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	refresh();
            }
        });
        
        JButton jAddBttn = new JButton();
        jAddBttn.setText("Add to Set ");
        jAddBttn.setToolTipText("Add to Set.");
        jAddBttn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jAddBttn_actionPerformed(e);
            }
        });

        jEnabledBox.setIcon(new ImageIcon(this.getClass().getResource(
                "bulb_icon_grey.gif")));
        jEnabledBox.setSelectedIcon(new ImageIcon(this.getClass().getResource(
                "bulb_icon_gold.gif")));
        jEnabledBox.setSelected(true);
        jEnabledBox.setToolTipText(
                "Push down to view above graph details with mouse moveover");
        jEnabledBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });

        jMASlider = new JSlider();
        jMASlider.setValue(0);
        jMASlider.setMaximum(1);
        jMASlider.setMinimum(0);
        jMASlider.setSnapToTicks(true);
        jMASlider.setPaintTicks(true);
        jMASlider.setMinorTickSpacing(1);
        jMASlider.setMajorTickSpacing(5);
        jMASlider.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.
                HAND_CURSOR));
        jMASlider.setToolTipText("Move the slider to change base microarray");
        jMASlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            	refresh();
            }
        });
        
        jLeftBoundarySlider = new JSlider();
        jLeftBoundarySlider.setValue(0);
        jLeftBoundarySlider.setSnapToTicks(true);
        jLeftBoundarySlider.setPaintTicks(true);
        jLeftBoundarySlider.setMinorTickSpacing(1);
        jLeftBoundarySlider.setMajorTickSpacing(5);
        jLeftBoundarySlider.setCursor(java.awt.Cursor.getPredefinedCursor(java.
                awt.Cursor.HAND_CURSOR));
        jLeftBoundarySlider.addChangeListener(new javax.swing.event.
                ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jLeftBoundarySlider_stateChanged(e);
            }
        });
        jLeftBoundarySlider.setToolTipText(
                "Move the slider to change the lower boundary of selected genes expression.");

        jRightBoundarySlider = new JSlider();
        jRightBoundarySlider.setValue(0);
        jRightBoundarySlider.setSnapToTicks(true);
        jRightBoundarySlider.setPaintTicks(true);
        jRightBoundarySlider.setMinorTickSpacing(1);
        jRightBoundarySlider.setMajorTickSpacing(5);
        jRightBoundarySlider.setCursor(java.awt.Cursor.getPredefinedCursor(java.
                awt.Cursor.HAND_CURSOR));
        jRightBoundarySlider.setToolTipText(
                "Move the slider to change the upper boundary of selected genes");
        jRightBoundarySlider.addChangeListener(new javax.swing.event.
                ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jRightBoundarySlider_stateChanged(e);
            }
        });

        JPanel controlPanel = new JPanel(new GridLayout(1, 2));
        jBinSizeSlider = new JSlider(SwingConstants.VERTICAL, 0, MAXBINNUM, 100);
        jBinSizeSlider.setSnapToTicks(true);
        jBinSizeSlider.setPaintTicks(true);
        jBinSizeSlider.setMinorTickSpacing(1);
        jBinSizeSlider.setMajorTickSpacing(5);
        jBinSizeSlider.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.
                Cursor.HAND_CURSOR));
        jBinSizeSlider.setToolTipText("Move the slider to change the bin size");
        jBinSizeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jBinSlider_stateChanged(e);
            }
        });
        controlPanel.add(jBinSizeSlider);
        
        leftBoundary = new JLabel("0.00");
        rightBoundary = new JLabel("0.00");
        selectedGeneNum = new JLabel("Selected genes: 0");
        binNumLabel = new JLabel("Bin size:   0.1");
        
        JPanel markerSelectionPanel = new JPanel();
        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setLayout(new GridLayout(2, 1));
        JPanel middleTopPanel = new JPanel(new GridLayout(2, 3));
        JPanel rightTopPanel = new JPanel();
        rightTopPanel.setLayout(new GridLayout(2, 1));
        JPanel buttonPanel = new JPanel();

        leftTopPanel.add(binNumLabel);
        leftTopPanel.add(jBinSizeLabel);
        buttonPanel.add(jAddBttn);
        middleTopPanel.add(buttonPanel);
        middleTopPanel.add(new JLabel("Select values from: "));
        middleTopPanel.add(leftBoundary);
        // middleTopPanel.add(colorCheck);
        middleTopPanel.add(selectedGeneNum);
        middleTopPanel.add(new JLabel("Select values to: "));
        middleTopPanel.add(rightBoundary);
        rightTopPanel.add(jLeftBoundarySlider);
        rightTopPanel.add(jRightBoundarySlider);
        //        leftTopPanel.setBorder(BorderFactory.createLineBorder(Color.
        //                black));
        markerSelectionPanel.setLayout(new BoxLayout(markerSelectionPanel,
                BoxLayout.X_AXIS));
        markerSelectionPanel.add(leftTopPanel);
        markerSelectionPanel.add(middleTopPanel);
        markerSelectionPanel.add(rightTopPanel);
        JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainPane.setOneTouchExpandable(true);
        mainPanel.add(controlPanel, BorderLayout.WEST);
        mainPanel.add(markerSelectionPanel, BorderLayout.NORTH);
        JButton imageSnapshotButton = new JButton("Image Snapshot");
        imageSnapshotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createImageSnapshot();
            }
        });
        
        // the bottom toolbar
        jToolBar3.add(colorCheck);
        jToolBar3.add(jMASlider);
        jToolBar3.add(jEnabledBox);
        jToolBar3.add(jTTestBttn);
        jToolBar3.add(jPrintBttn);
        jToolBar3.add(imageSnapshotButton);

        JFreeChart chart = ChartFactory.createXYLineChart("EVD", "Value", "Occurrences", null,
                PlotOrientation.VERTICAL, true,
                true, true); // Title,  X-Axis label,  Y-Axis label,  Dataset,  orientation, Show legend, show ToolTips, urls

        graph = new ChartPanel(chart, true);
        XYPlot newPlot = (XYPlot) chart.getPlot();
        // change the auto tick unit selection to integer units only...
        NumberAxis rangeAxis = (NumberAxis) newPlot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        mainPanel.add(graph, BorderLayout.CENTER);

        markerSelectionPanel.setBorder(BorderFactory.createLineBorder(Color.
                black));
        colorCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
		        refresh();
			}
        	
        });

    }

    private void jLeftBoundarySlider_stateChanged(ChangeEvent e) {
        int value = jLeftBoundarySlider.getValue();
        XYPlot plot = graph.getChart().getXYPlot();

        lowValue = minValue + value * binSize;
        plot.setDomainCrosshairValue(lowValue - 0.5 * binSize);
        String s = myFormatter.format(lowValue);
        leftBoundary.setText(s);
        /* TODO hs may be re-used multiple times and the last one is retained (see
         * createCollection(double min, double max, int selectedId, boolean active),
		 * so using it this way is not a good idea.
         */
        if (hs != null) {
            selectedGeneNum.setText("Selected genes: " +
                    hs.getGeneNumbers(lowValue, highValue));
        }

    }

    private void jRightBoundarySlider_stateChanged(ChangeEvent e) {
        int value = jRightBoundarySlider.getValue();
        XYPlot plot = graph.getChart().getXYPlot();

        highValue = minValue + value * binSize;
        plot.setDomainCrosshairValue(highValue - 0.5 * binSize);
        String highStr = myFormatter.format(highValue); //String.format("Bin size: %1$.2d", new Double(binSize));
        rightBoundary.setText(highStr);
        /* TODO hs may be re-used multiple times and the last one is retained (see
         * createCollection(double min, double max, int selectedId, boolean active),
		 * so using it this way is not a good idea.
         */
        if (hs != null) {
            selectedGeneNum.setText("Selected genes: " +
                    hs.getGeneNumbers(lowValue, highValue));
        }

    }

    // this is the only place where basketNum could be changed. It is called "Bin size" on GUI though.
    private void jBinSlider_stateChanged(ChangeEvent e) {
        basketNum = MAXBINNUM - jBinSizeSlider.getValue();
        jBinSizeLabel.setText("Total bins: " + (basketNum + 1));
        if (basketNum == 0) {
            binNumLabel.setText("Bin size: unlimited");
        } else {
            binSize = (maxValue - minValue) / basketNum;
            String bin = myFormatter.format(binSize);
            binNumLabel.setText("Bin size:  " + bin);
        }

        jLeftBoundarySlider.setMaximum(basketNum + 1);
        jLeftBoundarySlider.setValue(0);
        jRightBoundarySlider.setMaximum(basketNum + 1);
        jRightBoundarySlider.setValue(0);
//        maxOccurrenceNum = maxOccurrence();
        refresh();
    }

    @Publish
    public org.geworkbench.events.ImageSnapshotEvent
            createImageSnapshot() {
        Dimension panelSize = graph.getSize();
        BufferedImage image = new BufferedImage(panelSize.width,
                panelSize.height,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        graph.paint(g);
        ImageIcon icon = new ImageIcon(image, "EVD Plot");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.
                events.ImageSnapshotEvent("EVD Snapshot", icon,
                org.geworkbench.events.
                        ImageSnapshotEvent.Action.SAVE);
        return event;
    }

    // this is for t-test case. maybe we should separate the shared variable to avoid confusion
    private XYSeriesCollection createCollection(double[] tValues,
                                               DSItemList<DSGeneMarker> item) {
        int[] basketValues = new int[basketNum + 1];
        binSize = (maxValue - minValue) / basketNum;
        binNumLabel.setText("Bin size:  " + myFormatter.format(binSize));
        XYSeriesCollection plots = new XYSeriesCollection();
        if (tValues == null) {
            return null;
        }
        hs = new Histogram(basketNum, minValue, maxValue, tValues, item);
        basketValues = hs.getBasketvalues();
        XYSeries dataSeries = new XYSeries("Two-Sample TTest");
        if (basketNum == 0) {
            dataSeries.add(minValue, basketValues[0]);
        } else {
            for (int i = 0; i <= basketNum; i++) {

                //only add value > 0. is it right?
                if (basketValues[i] > 0) {
                    dataSeries.add(i * ((maxValue - minValue) / basketNum) +
                            minValue, basketValues[i]);

                }

            }

        }

        plots.addSeries(dataSeries);
        return plots;

    }

    private XYSeriesCollection createCollection(int selectedId) {
        PanelVisualPropertiesManager propertiesManager =
                PanelVisualPropertiesManager.getInstance();
        binSize = (maxValue - minValue) / basketNum;
        String bin = myFormatter.format(binSize);
        binNumLabel.setText("Bin size:  " + bin);
        XYSeriesCollection plots = new XYSeriesCollection();
        if (maSetView == null) {
            return null;
        }
        try {

            //draw base array.
            DSMicroarray ma = maSetView.get(selectedId);
            XYSeries dataSeries = new XYSeries(ma.getLabel() + "(base)");
            int[] basketValues = new int[basketNum + 1];
            for (int i = 0; i < basketNum; i++) {
                basketValues[i] = 0;
            }
            if (maSetView.markers() == null) {
                return null;
            }
            hs = new Histogram(basketNum, ma, minValue, maxValue, maSetView.markers());

            basketValues = hs.getBasketvalues();

            //get rid of 0s. Not inside but on boundary
            //Should not remove 0s. need more care review.
            int maxIndex = basketNum;
            int minIndex = 0;
            if (basketValues == null) {
                return null;
            }
            if (basketNum == 0) {
                dataSeries.add(minValue, basketValues[0]);
            } else {

                for (int i = minIndex; i <= maxIndex; i++) {

                    if (basketValues[i] > 0) {
                        dataSeries.add(i * ((maxValue - minValue) / basketNum) +
                                minValue, basketValues[i]);

                    }

                }
            }
            plots.addSeries(dataSeries);
            //clear the old propertiesList.
            propertiesList.clear();

            //drew active phenotpye data.

            int panelIndex = 0;
            if (maSetView.size() > 0) {
                for (int pId = 0; pId < maSetView.getItemPanel().panels().size();
                     pId++) {
                    DSPanel<DSMicroarray>
                            panel = maSetView.getItemPanel().panels().get(pId);
                    PanelVisualProperties properties = propertiesManager.
                            getVisualProperties(panel);
                    panelIndex++;
                    if (properties == null) {
                        properties = propertiesManager.
                                getDefaultVisualProperties(panelIndex);
                    }

                    /* after the loop, hs will retain the last instance. not likely to be intended */ 
					for (int k = 0; k < panel.size(); k++) {

						DSMicroarray currentMicroarray = panel.get(k);
						propertiesList.add(properties);

						hs = new Histogram(basketNum, currentMicroarray,
								minValue, maxValue, maSetView.markers());

						basketValues = hs.getBasketvalues();

						dataSeries = new XYSeries(currentMicroarray.getLabel());

						maxIndex = basketNum;
						minIndex = 0;
						if (basketNum == 0) {
							dataSeries.add(minValue, basketValues[0]);
						} else {

							for (int i = minIndex; i <= maxIndex; i++) {
								if (basketValues[i] > 0) {
									dataSeries
											.add(i
													* ((maxValue - minValue) / basketNum)
													+ minValue, basketValues[i]);

								}

							}

							plots.addSeries(dataSeries);

						}
					}
                }

            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return plots;
    }

    private void drawPlot(final XYSeriesCollection plots, String title) {
        if (plots == null) {
            return;
        }
        boolean tooltipEnabled = jEnabledBox.isSelected();
        JFreeChart chart = ChartFactory.createXYLineChart(title, "Value", "Occurrences",
                plots, PlotOrientation.VERTICAL, 
                true, // show legend
                tooltipEnabled, 
                true  // urls
                );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        XYPlot newPlot = (XYPlot) chart.getPlot();
        Color c = UIManager.getColor("Panel.background");
        if (c == null) {
            c = Color.white;
        }
        newPlot.setBackgroundPaint(c);
        newPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        newPlot.setDomainGridlinePaint(Color.white);
        newPlot.setRangeGridlinePaint(Color.white);
        newPlot.setDomainCrosshairVisible(true);
        newPlot.setDomainCrosshairLockedOnData(true);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) newPlot.
                getRenderer();
		renderer.setSeriesShapesVisible(0, true);
        if (tooltipEnabled) {

			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {

				public String generateToolTip(XYDataset dataset, int series,
						int item) {
					double x = dataset.getXValue(series, item);
					if (Double.isNaN(x) && dataset.getX(series, item) == null) {
						return "";
					}

					double y = dataset.getYValue(series, item);
					if (Double.isNaN(y) && dataset.getX(series, item) == null) {
						return "";
					}

					String label = (String) (plots.getSeries(series).getKey());
					return label + ": ([" + myFormatter.format(x) + ", "
							+ myFormatter.format(x + binSize) + "), " + (int) y
							+ ")";
				}
			});
        }
        renderer.setSeriesLinesVisible(0, false);
        for (int i = 1; i < newPlot.getDatasetCount(); i++) {
            renderer.setSeriesLinesVisible(i, true);
        }

        //base color & shape
        if (!colorCheck.isSelected()) {

            for (int i = 0; i < propertiesList.size(); i++) {
                PanelVisualProperties panelVisualProperties = propertiesList.
                        get(i);
                // Note: "i+1" because we did not define base.
                int index = i + 1;
                renderer.setSeriesPaint(index, panelVisualProperties.getColor());
                renderer.setSeriesShape(index, panelVisualProperties.getShape());

                renderer.setSeriesShapesVisible(index, true);
            }

        } else {
            for (int i = 0; i < propertiesList.size(); i++) {
                // Note: "i+1" because we did not define base.
                renderer.setSeriesShapesVisible(i+1, true);
            }
        }

        newPlot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        NumberAxis rangeAxis = (NumberAxis) newPlot.getRangeAxis();
        rangeAxis.setUpperBound(maxOccurrence);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        ValueAxis xAxis = newPlot.getDomainAxis();
        final int MARGIN = 2; //the left and right edge size beyond the min/max values.
        if (minValue < maxValue) {
            xAxis.setRange(minValue - MARGIN * binSize,
                    maxValue + MARGIN * binSize);
        }

        // OPTIONAL CUSTOMISATION COMPLETED.

        graph.setChart(chart);
        graph.addChartMouseListener(new ChartMouseListener() {
        	public void chartMouseMoved(ChartMouseEvent e){
        	}
        	public void chartMouseClicked(ChartMouseEvent e){
        		if (e.getEntity() instanceof XYItemEntity) {
        			XYItemEntity i = (XYItemEntity)e.getEntity();
        			int selectedId = i.getSeriesIndex();
        			if (selectedId > 0)
        				setSlider(i.getDataset().getSeriesKey(selectedId).toString());
        		}
        	}
        });
    }

    private void jAddBttn_actionPerformed(ActionEvent e) {
        try {
            lowValue = new Double(leftBoundary.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please set up correct lower threshold.",
                    "Please check",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            highValue = new Double(rightBoundary.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please set up correct upper threshold.",
                    "Please check",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (lowValue > highValue) {
            double temp = highValue;
            highValue = lowValue;
            lowValue = temp;
        }
        /* TODO hs may be re-used multiple times and the last one is retained (see
         * createCollection(double min, double max, int selectedId, boolean active),
		 * so using it this way is not a good idea.
         */
        DSPanel<DSGeneMarker> panel = hs.getPanel(lowValue, highValue);
        selectedGeneNum.setText("Selected genes: " + panel.size());

        if (panel != null && panel.size()>0) {
            publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(DSGeneMarker.class, panel, org.geworkbench.events.SubpanelChangedEvent.NEW));
        } else {
            JOptionPane.showMessageDialog(null, "No gene is selected",
                    "Please check",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

	@Publish
	public SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}

    private void setSlider(String selected) {
    	String currentBase = maSetView.get(jMASlider.getValue()).getLabel();
    	if (!currentBase.equals(selected)){
    		int newBaseId = 0;
    		for (int i = 0; i < maSetView.size(); i++) {
    			if (maSetView.get(i).getLabel().equals(selected)) {
    				newBaseId = i;
    				break;
    			}
    		}
    		jMASlider.setValue(newBaseId);
    		refresh();
    	}
    }
    
    /* refresh the plot. */
    private void refresh() {

        if (!jTTestBttn.isSelected() ) { // mode == EVDMODE) {

            if (maSetView == null) {
            	JFreeChart chart = ChartFactory.createXYLineChart("EVD", "Value",
                        "Occurrences", null, PlotOrientation.VERTICAL, 
                        true, // legend
                        jEnabledBox.isSelected(), 
                        true // urls
                        );

                graph.setChart(chart);
                mainPanel.repaint();

                return;
            }

            jMASlider.setEnabled(true);

            int mArrayId = jMASlider.getValue();
            if (mArrayId >= 0) {
            	double max = 0;
            	double min = 0;
                // create New Chart
                lowValue = highValue = minExpressionValue;
                
                // the rationale behind the following logic is not obvious 
                // but let's keep it because it is what current system test describes as 'correct'
                if(minExpressionValue<min) min = minExpressionValue;
                if(maxExpressionValue>max) max = maxExpressionValue;
                
                leftBoundary.setText(myFormatter.format(min));
                rightBoundary.setText(myFormatter.format(min));
                jLeftBoundarySlider.setValue(0);
                jRightBoundarySlider.setValue(0);
                
                maxValue = maxExpressionValue;
                minValue = minExpressionValue;
                drawPlot(createCollection(mArrayId), "EVD");
                mainPanel.repaint();
            }

        } else { // if (mode == TTESTMODE) { // jTTestBttn.isSelected()
            jMASlider.setEnabled(false);
            SimpleTTest simpleTTest = new SimpleTTest();

            double values[] = simpleTTest.execute(maSetView);
            minValue = simpleTTest.getMinT();
            maxValue = simpleTTest.getMaxT();
            drawPlot(createCollection(values, maSetView.markers()), "T-Test");
        }

    }

}
