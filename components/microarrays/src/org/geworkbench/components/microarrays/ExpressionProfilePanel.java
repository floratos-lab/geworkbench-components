package org.geworkbench.components.microarrays;

/*
 * The geworkbench project
 *
 * Copyright (c) 2006 Columbia University
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.MicroarraySetViewEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.util.BusySwingWorker;
import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.SymbolicXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Modular Application Framework for Gene Expession, Sequence and Genotype
 * Analysis
 * 
 * @author Adam Margolin
 * @version $Id: ExpressionProfilePanel.java,v 1.12 2008-03-19 20:08:21 my2248 Exp $
 * @see MenuListener, VisualPlugin
 * 
 */
@AcceptTypes( { DSMicroarraySet.class })
public class ExpressionProfilePanel extends MicroarrayViewEventBase implements
		MenuListener, VisualPlugin {

	Log log = LogFactory.getLog(this.getClass());

	private JPanel graphPanel;
	private ChartPanel graph;
	private ChartPanel chartPanel;
	private JFreeChart chart;
	private JButton imageSnapshotButton;
	JToggleButton jEnabledBox;

	private boolean isToolTipEnabled = true;

	public ExpressionProfilePanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @throws Exception
	 */
	protected void jbInit() throws Exception {

		super.jbInit();
		graphPanel = new JPanel(new BorderLayout());
		chart = ChartFactory.createXYLineChart(null, // Title
				"Experiment", // X-Axis label
				"Value", // Y-Axis label
				new XYSeriesCollection(), // Dataset
				PlotOrientation.VERTICAL, false, // Show legend
				true, true);
		graph = new ChartPanel(chart, true);
		graph.setAutoscrolls(true);
		graphPanel.add(graph, BorderLayout.CENTER);
		mainPanel.add(graphPanel, BorderLayout.CENTER);

		plotButton
				.addActionListener(new ExpressionProfilePanel_plotButton_actionAdapter(
						this));
		jToolBar3.add(plotButton);
		jToolBar3.add(numMarkersSelectedLabel);

		imageSnapshotButton = new JButton("Image Snapshot");
		imageSnapshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createImageSnapshot();
			}
		});
		jToolBar3.add(imageSnapshotButton);
		imageSnapshotButton.setEnabled(false);

		jEnabledBox = new JToggleButton();
		jEnabledBox.setIcon(new ImageIcon(this.getClass().getResource(
				"bulb_icon_grey.gif")));
		jEnabledBox.setSelectedIcon(new ImageIcon(this.getClass().getResource(
				"bulb_icon_gold.gif")));
		jEnabledBox.setSelected(true);
		jEnabledBox
				.setToolTipText("Push down to view above graph details with mouse moveover");
		jEnabledBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isToolTipEnabled = jEnabledBox.isSelected();
				setPlotRefresh(true);
				fireModelChangedEvent(null);
			}
		});

		jToolBar3.add(jEnabledBox);

		chkAllMarkers.setSelected(false);
		this.onlyActivatedMarkers = true;

		chkAllArrays.setSelected(false);
		this.onlyActivatedArrays = true;
	}

	/**
	 * @param event
	 */
	protected void fireModelChangedEvent(MicroarraySetViewEvent event) {

		log.debug("Event is " + event);

		if (maSetView == null)
			return;

		if (isPlotRefresh()) {

			graphPanel.removeAll();

			setPlotRefresh(false);

			BusySwingWorker worker = new BusySwingWorker() {
				public Object construct() {
					setBusy(graphPanel);
					DSPanel<DSGeneMarker> genes = new CSPanel<DSGeneMarker>("");
					genes.addAll(maSetView.markers());
					DSPanel<DSMicroarray> arrays = new CSPanel<DSMicroarray>("");
					arrays.addAll(maSetView.items());
					XYSeriesCollection plots = new XYSeriesCollection();
					int numGenes = (genes.size() > 500) ? 500 : genes.size();

					for (int geneCtr = 0; geneCtr < numGenes; geneCtr++) {
						XYSeries dataSeries = new XYSeries(genes.get(geneCtr)
								.getLabel());
						for (int maCtr = 0; maCtr < arrays.size(); maCtr++) {
							double value = arrays.get(maCtr).getMarkerValue(
									genes.get(geneCtr)).getValue();
							if (Double.isNaN(value)) {
								dataSeries.add(maCtr, 0);
							} else {
								dataSeries.add(maCtr, value);
							}
						}
						plots.addSeries(dataSeries);
					}

					ExpressionXYToolTip tooltipGenerator = null;

					if (isToolTipEnabled) {
						tooltipGenerator = new ExpressionXYToolTip();
						tooltipGenerator.setCurrentGenes(genes, arrays);
					}

					StandardXYItemRenderer renderer = new StandardXYItemRenderer(
							StandardXYItemRenderer.LINES, tooltipGenerator);

					JFreeChart ch = ChartFactory.createXYLineChart(null, // Title
							"Experiment", // X-Axis label
							"Value", // Y-Axis label
							plots, // Dataset
							PlotOrientation.VERTICAL, false, // Show legend
							isToolTipEnabled, true);

					ch.getXYPlot().setRenderer(renderer);

					if (arrays != null) {
						String[] alist;
						alist = new String[arrays.size()];
						for (int maCtr = 0; maCtr < arrays.size(); maCtr++)
							alist[maCtr] = arrays.get(maCtr).getLabel();
						ch.getXYPlot().setDomainAxis(
								new SymbolAxis("Experiment", alist));
						ch.getXYPlot().getDomainAxis().setVerticalTickLabels(
								true);

					}

					chartPanel = new ChartPanel(ch);
					return null;

				}

				public void finished() {
					graphPanel.removeAll();
					chartPanel
							.addChartMouseListener(new MicroarrayChartMouseListener());
					graphPanel.add(chartPanel, BorderLayout.CENTER);

					graphPanel.revalidate();
					graphPanel.repaint();

				}

			};

			worker.start();

		}
	}

	/**
	 * @return
	 */
	public JPanel getGraphPanel() {
		assert graphPanel != null : "Null widget a " + graphPanel;

		return graphPanel;
	}

	/**
	 * @return
	 */
	public ChartPanel getGraph() {
		assert graphPanel != null : "Null widget a " + graph;

		return graph;
	}

	/**
	 * @return
	 */
	public JFreeChart getChart() {
		assert graphPanel != null : "Null widget a " + chart;

		return chart;
	}

	/**
	 * @return
	 */
	public JFreeChart getChartPanel() {
		assert graphPanel != null : "Null widget a " + chart;

		return chart;
	}

	/**
	 * Responsible for handling marker selection in a microarray scatter plot.
	 * 
	 * @author unattributable
	 */
	private class MicroarrayChartMouseListener implements ChartMouseListener {

		public void chartMouseClicked(ChartMouseEvent event) {
			ChartEntity entity = event.getEntity();
			if ((entity != null) && (entity instanceof XYItemEntity)) {
				XYItemEntity xyEntity = (XYItemEntity) entity;
				int series = xyEntity.getSeriesIndex();
				int item = xyEntity.getItem();

				DSMicroarray array = maSetView.items().get(item);
				if (array != null) {
					PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(
							array);
					publishPhenotypeSelectedEvent(pse);
				}

				DSGeneMarker marker = maSetView.markers().get(series);
				if (marker != null) {
					MarkerSelectedEvent mse = new org.geworkbench.events.MarkerSelectedEvent(
							marker);
					publishMarkerSelectedEvent(mse);
				}

			}
		}

		public void chartMouseMoved(ChartMouseEvent event) {
			// No-op
		}
	}

	@Publish
	public ImageSnapshotEvent createImageSnapshot() {
		Dimension panelSize = chartPanel.getSize();
		BufferedImage image = new BufferedImage(panelSize.width,
				panelSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		chartPanel.paint(g);
		ImageIcon icon = new ImageIcon(image, "Expression Profile");
		org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
				"Expression Profile Snapshot", icon,
				org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
		return event;
	}

	/**
	 * Tool-tip renderer for gene charts.
	 * 
	 * @author unattributable
	 */
	private class ExpressionXYToolTip extends SymbolicXYItemLabelGenerator {

		/**
		 * @param data
		 * @param series
		 * @param item
		 * @return String
		 */

		DSPanel<DSGeneMarker> markers = new CSPanel<DSGeneMarker>("");
		DSPanel<DSMicroarray> arrays = new CSPanel<DSMicroarray>("");

		public void setCurrentGenes(DSPanel<DSGeneMarker> markers,
				DSPanel<DSMicroarray> arrays) {
			this.markers = markers;
			this.arrays = arrays;
		}

		public String generateToolTip(XYDataset data, int series, int item) {

			String tooltip = "";

			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);

			DSGeneMarker marker = markers.get(series);
			DSMicroarray array = arrays.get(item);

			if (marker != null) {
				tooltip += "Marker:" + marker.getLabel();
			}
			if (array != null) {
				tooltip += "  Array:" + array.getLabel();
			}

			tooltip += "  Value:" + nf.format(data.getYValue(series, item));

			return tooltip;

		}
	}

	/**
	 * @param var
	 * @return ActionListener
	 */
	public ActionListener getActionListener(String var) {
		return null;
	}

	/**
	 * @param e
	 */
	void plotButton_actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals(plotButton.getText())) {
			setPlotRefresh(true);
			imageSnapshotButton.setEnabled(true);
			refreshMaSetView();
		}

	}

	@Publish
	public org.geworkbench.events.MarkerSelectedEvent publishMarkerSelectedEvent(
			MarkerSelectedEvent event) {
		return event;
	}

	@Publish
	public PhenotypeSelectedEvent publishPhenotypeSelectedEvent(
			PhenotypeSelectedEvent event) {
		return event;
	}

}

/**
 * @author keshav
 */
class ExpressionProfilePanel_plotButton_actionAdapter implements
		java.awt.event.ActionListener {

	private Log log = LogFactory.getLog(this.getClass());

	ExpressionProfilePanel adaptee;

	ExpressionProfilePanel_plotButton_actionAdapter(
			ExpressionProfilePanel adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		log.debug("actionPerformed " + e);

		adaptee.plotButton_actionPerformed(e);
		adaptee.getComponent().repaint();
	}
}