package org.geworkbench.components.plots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSTTestResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.BusySwingWorker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Volcano plot.
 *
 * @author Matt Hall, John Watkinson
 * @version $Id$
 */
@AcceptTypes({DSTTestResultSet.class})
public class VolcanoPlot implements VisualPlugin {

    static private Log log = LogFactory.getLog(VolcanoPlot.class);

	private static class MarkerAndStats implements Comparable<MarkerAndStats> {

        DSGeneMarker marker;
        double fold;
        double pValue;

        public MarkerAndStats(DSGeneMarker marker, double fold, double pValue) {
            this.marker = marker;
            this.fold = fold;
            this.pValue = pValue;
        }

        public int compareTo(MarkerAndStats o) {
            if (fold > o.fold) {
                return 1;
            } else if (fold < o.fold) {
                return -1;
            } else {
                return 0;
            }
        }
    }
	
    private class MarkerXYToolTipGenerator extends StandardXYToolTipGenerator implements ChartMouseListener {
		private static final long serialVersionUID = -6345314319585564074L;

        private List<MarkerAndStats> markers;         
        private MarkerAndStats[] markerList;
               
        public MarkerXYToolTipGenerator() {
            markers = new ArrayList<MarkerAndStats>();            
        }

        @Override
        public void chartMouseClicked(ChartMouseEvent event) {
            ChartEntity entity = event.getEntity();
            if (entity instanceof XYItemEntity) {
                XYItemEntity xyEntity = (XYItemEntity) entity;
                int item = xyEntity.getItem();
                MarkerAndStats markerStats = markerList[item];
                if (markerStats != null) {
                    publishMarkerSelectedEvent(new MarkerSelectedEvent(markerStats.marker));
                }
            }
        }

        @Override
        public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
            // No-op
        }

        public void addMarkerAndStats(DSGeneMarker marker, double fold, double pValue) {
            markers.add(new MarkerAndStats(marker, fold, pValue));
        }

        public void processTooltips() {
            markerList = markers.toArray(new MarkerAndStats[0]);
        }

        @Override
        public String generateToolTip(XYDataset data, int series, int item) {
            String unknown = "Unknown: ";
            DecimalFormat df = new DecimalFormat("0.###E0");

            if ( item > markerList.length-1 )
            	return unknown;
            MarkerAndStats markerStats = markerList[item];
            if (markerStats != null) {
                return markerStats.marker.getLabel() + " (" + markerStats.marker.getGeneName() + "): " + df.format(markerStats.fold) + ", " + df.format(markerStats.pValue);
            } else {
            	return unknown;
            }
        }

		public void sort() {
			Collections.sort(markers);
		}
    }

    private JPanel mainPanel;
    private JPanel parentPanel;
    private JButton exportButton;  

    /**
     * The significance results we're plotting
     */
    private DSTTestResultSet<DSGeneMarker> significance = null;

    /**
     * Constructor lays out the component and adds behaviors.
     */
    public VolcanoPlot() {
        parentPanel = new JPanel(new BorderLayout());
        mainPanel = new JPanel(new BorderLayout());
        parentPanel.add(mainPanel, BorderLayout.CENTER);
        
        JPanel lowerPanel = new JPanel(new FlowLayout());
        exportButton = new JButton("Export Data");
        lowerPanel.add(exportButton);
        parentPanel.add(lowerPanel, BorderLayout.SOUTH);
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (significance instanceof CSTTestResultSet) {
                	((CSTTestResultSet<DSGeneMarker>)significance).saveDataToCSVFile();                     
                }
            }
        });
       
    }

    /**
     * The component for the GUI engine.
     */
    public Component getComponent() {
        return parentPanel;
    }

    /**
     * Receives a project event.
     *
     * @param e      the event.
     * @param source the source of the event (unused).
     */
	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(ProjectEvent e, Object source) {
		DSDataSet<? extends DSBioObject> dataFile = e.getDataSet();

		if (dataFile instanceof CSTTestResultSet) {
			significance = (CSTTestResultSet<DSGeneMarker>) dataFile;
			generateChart();
		}
	}

	private void generateChart() {
        DSMicroarraySet set = significance.getParentDataSet();
        String[] caseLabels = significance.getLabels(DSTTestResultSet.CASE);
        String[] controlLabels = significance.getLabels(DSTTestResultSet.CONTROL);
        DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(set);
        DSPanel<DSMicroarray> casePanel = new CSPanel<DSMicroarray>("Case");
        for (String label : caseLabels) {
            casePanel.addAll(context.getItemsWithLabel(label));
        }
        casePanel.setActive(true);
        DSPanel<DSMicroarray> controlPanel = new CSPanel<DSMicroarray>("Control");
        for (String label : controlLabels) {
            controlPanel.addAll(context.getItemsWithLabel(label));
        }
        casePanel.setActive(true);
        DSPanel<DSGeneMarker> significantGenes = significance.getSignificantMarkers();
        DSPanel<DSMicroarray> itemPanel = new CSPanel<DSMicroarray>();
        itemPanel.panels().add(casePanel);
        itemPanel.panels().add(controlPanel);
        significantGenes.setActive(true);
        DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(set);
        dataSetView.getMarkerPanel().panels().add(significantGenes);
        dataSetView.setItemPanel(itemPanel);
        log.debug("Generating graph.");
        generateChartAndDisplay(dataSetView);
    }

    private void generateChartAndDisplay(final DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView) {
        mainPanel.removeAll();
        final BusySwingWorker<ChartPanel, Void> worker = new BusySwingWorker<ChartPanel, Void>() {

            @Override
            protected ChartPanel doInBackground() {
                setShowProgress(true);
                setBusy(mainPanel);
                MarkerXYToolTipGenerator toolTipGenerator = new MarkerXYToolTipGenerator();
                ChartPanel cpanel = new ChartPanel(createVolcanoChart(dataSetView, significance, false, false, this, toolTipGenerator));
                cpanel.addChartMouseListener(toolTipGenerator);
                return cpanel;
            }

            @Override
            public void done() {
                try {
                    mainPanel.removeAll();
					mainPanel.add(get());
	                mainPanel.revalidate();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
            }
        };
        worker.execute();
    }

    // this updates the selection in the gene selector panel
    @Publish public MarkerSelectedEvent publishMarkerSelectedEvent(MarkerSelectedEvent event) {
        return event;
    }

    private JFreeChart createVolcanoChart(
            DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView,
            DSTTestResultSet<DSGeneMarker> significance,
            boolean showAllArrays,
            boolean showAllMarkers,
            BusySwingWorker<ChartPanel, Void> worker,
            MarkerXYToolTipGenerator toolTipGenerator
    ) throws SeriesException {
    	
    	 
        XYSeriesCollection plots = new XYSeriesCollection();

        // First put all the gene pairs in the xyValues array
        int numMarkers = dataSetView.getMarkerPanel().size();

        if (worker != null) {
            worker.setProgressMax(numMarkers * 2);
        }

        // First pass to determine negative value correction amount
        
        XYSeries series = new XYSeries("All");
        List<Integer> underflowLocations = new ArrayList<Integer>();
        double validMinSigValue = Double.MAX_VALUE;
        double minPlotValue = Double.MAX_VALUE;
        double maxPlotValue = Double.MIN_VALUE;
        for (int i = 0; i < numMarkers; i++) {
            DSGeneMarker marker = dataSetView.getMarkerPanel().get(i);
            

            double sigValue = significance.getSignificance(marker);
            
            if (sigValue >= 0.0 && sigValue < 4.9E-45  ) {
	  	        sigValue = 4.9E-45;
            } 
	        else if (sigValue < 0) {
		        log.debug("Significance less than 0, (" + sigValue + ") setting to 1 for the moment.");
		        sigValue = 1;
		    }

	    	if (sigValue < validMinSigValue) {
	    		validMinSigValue = sigValue;
	    	}
            
             
            double xVal = significance.getFoldChange(marker);
                       
            if (!Double.isNaN(xVal) && !Double.isInfinite(xVal)) {
                double yVal = -Math.log10(sigValue);
                double plotVal = Math.abs(xVal) * Math.abs(yVal);
                if (plotVal < minPlotValue) {
                    minPlotValue = plotVal;
                }
                if (plotVal > maxPlotValue) {
                    maxPlotValue = plotVal;
                }

                series.add(xVal, yVal);
                toolTipGenerator.addMarkerAndStats(marker, xVal, yVal);
            } else {
                log.debug("Marker " + i + " was infinite or NaN.");
            }

            if (worker != null) {
                worker.setCurrentProgress(numMarkers + i);
            }
        }
        toolTipGenerator.sort();

        // Fix underflow values
        for (Integer fixIndex : underflowLocations) {
            series.getDataItem(fixIndex).setY(validMinSigValue);
        }

        toolTipGenerator.processTooltips();

        plots.addSeries(series);

        boolean isLogNormalized=significance.getIsLogNormalized();
        String s=isLogNormalized?"(Difference of avg. Log2 values)":"Log2(ratio)";
        JFreeChart mainChart = ChartFactory.createScatterPlot(significance.getLabel(), "Fold Change "+s, "Significance (-Log10) ", plots, PlotOrientation.VERTICAL, false, true, false); // Title, (, // X-Axis label,  Y-Axis label,  Dataset,  Show legend
        mainChart.getXYPlot().setRenderer(new VolcanoRenderer(plots, minPlotValue, maxPlotValue, toolTipGenerator));
        
        return mainChart;
    }

    private static class VolcanoRenderer extends StandardXYItemRenderer {
		private static final long serialVersionUID = -8526944841096475280L;
		
		private XYDataset dataset;
		private GMTColorPalette colormap;

        public VolcanoRenderer(XYDataset dataset, double min, double max, MarkerXYToolTipGenerator toolTipGenerator) {
            super(StandardXYItemRenderer.SHAPES, toolTipGenerator);
            this.dataset = dataset;
            GMTColorPalette.ColorRange[] range = {new GMTColorPalette.ColorRange(min, Color.BLUE.brighter(), max - (max / 3), Color.BLUE),
                    new GMTColorPalette.ColorRange(max - (max / 3), Color.BLUE, max, Color.RED)};
            this.colormap = new GMTColorPalette(range);
            this.setSeriesShape(0, new Rectangle(6, 6));
        }

        public Paint getItemPaint(int series, int item) {
            double x = dataset.getXValue(series, item);
            double y = dataset.getYValue(series, item);
            return colormap.getColor(Math.abs(x) * Math.abs(y));
        }

    }

}
