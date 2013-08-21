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

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.util.BusySwingWorker;
import org.geworkbench.util.ProgressBar;
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
 * @version $Id$
 * 
 */
@AcceptTypes( { DSMicroarraySet.class })
public class ExpressionProfilePanel extends MicroarrayViewEventBase implements
		VisualPlugin {

	private static final String Y_AXIS_LABEL = "Expression Level";

	private static final String X_AXIS_LABEL = "Experiment";

	private JPanel graphPanel;
	private ChartPanel graph;
	private ChartPanel chartPanel;
	private JFreeChart chart;
	private JButton imageSnapshotButton;
	private JToggleButton jEnabledBox;

	private boolean isPlotRefresh = false;

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
	private void jbInit() throws Exception {

		graphPanel = new JPanel(new BorderLayout());
		chart = ChartFactory.createXYLineChart(null, // Title
				X_AXIS_LABEL, // X-Axis label
				Y_AXIS_LABEL, // Y-Axis label
				new XYSeriesCollection(), // Dataset
				PlotOrientation.VERTICAL, false, // Show legend
				true, true);
		graph = new ChartPanel(chart, true);
		graph.setAutoscrolls(true);
		graphPanel.add(graph, BorderLayout.CENTER);
		mainPanel.add(graphPanel, BorderLayout.CENTER);

		plotButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				isPlotRefresh = true;
				imageSnapshotButton.setEnabled(true);
				refreshMaSetView();
				mainPanel.repaint();
			}
			
		});

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
				isPlotRefresh = true;
				fireModelChangedEvent();
			}
		});

		jToolBar3.add(jEnabledBox);

	}

	/**
	 * @param event
	 */
	@Override
	protected void fireModelChangedEvent() {

		if (maSetView == null)
			return;

		if (isPlotRefresh) {

			graphPanel.removeAll();

			isPlotRefresh = false;

			BusySwingWorker<Void, Void> worker = new BusySwingWorker<Void, Void>() {
				ProgressBar pb = null;

				@Override
				protected Void doInBackground() throws Exception {
					setBusy(graphPanel);
					DSPanel<DSGeneMarker> genes = new CSPanel<DSGeneMarker>("");
					genes.addAll(maSetView.markers());

					XYSeriesCollection plots = new XYSeriesCollection();
                    int numGenes = genes.size();
                	
            		pb = org.geworkbench.util.ProgressBar
    					.create(org.geworkbench.util.ProgressBar.INDETERMINATE_TYPE);
            		pb.setTitle("Expression Profile is creating chart");
            		pb.setMessage("Drawing...");
            		pb.setBounds(new org.geworkbench.util.ProgressBar.IncrementModel(0,
            				numGenes, 0, numGenes, 1));
            		pb.start();

            		DSItemList<DSMicroarray> arrays = maSetView.items();
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
						pb.update();
					}

					ExpressionXYToolTip tooltipGenerator = null;

					boolean isToolTipEnabled = jEnabledBox.isSelected();
					if (isToolTipEnabled) {
						tooltipGenerator = new ExpressionXYToolTip(genes, arrays);
					}

					StandardXYItemRenderer renderer = new StandardXYItemRenderer(
							StandardXYItemRenderer.LINES, tooltipGenerator);

					JFreeChart ch = ChartFactory.createXYLineChart(null, // Title
							X_AXIS_LABEL, // X-Axis label
							Y_AXIS_LABEL, // Y-Axis label
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
								new SymbolAxis(X_AXIS_LABEL, alist));
						ch.getXYPlot().getDomainAxis().setVerticalTickLabels(
								true);

					}

					chartPanel = new ChartPanel(ch);
					return null;

				}

				@Override
				public void done() {
					graphPanel.removeAll();
					chartPanel
							.addChartMouseListener(new MicroarrayChartMouseListener());
					graphPanel.add(chartPanel, BorderLayout.CENTER);

					graphPanel.revalidate();
					graphPanel.repaint();
                    pb.stop();

				}

			};

			worker.execute();

		}
	}

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
		private static final long serialVersionUID = -3031603961146669009L;

		final DSPanel<DSGeneMarker> markers;
		final DSItemList<DSMicroarray> arrays;

		public ExpressionXYToolTip(final DSPanel<DSGeneMarker> markers,
				final DSItemList<DSMicroarray> arrays) {
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
