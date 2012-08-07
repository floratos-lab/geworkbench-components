/**
 * 
 */
package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.CellularNetworkPreference;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * @author zji
 * @version $Id$
 * 
 */
public class ThrottleGraph extends JPanel {

	private static final long serialVersionUID = -8455634459226637288L;

	static private final DecimalFormat myFormatter = new DecimalFormat("0.00");

	private JFreeChart chart;
	private ChartPanel graph;
	private JPanel chartPanel;
	private JPanel legendPanel;

	private JComboBox thresholdTypes = new JComboBox();
	private JTextField thresholdTextField;
	private JSlider thresholdSlider;

	private LengendCheckBox[] lengendCheckBoxList;
	private JButton[] jButtonList;

	private LegendObjectCollection legendList = new LegendObjectCollection();

	private long maxX = 1;

	final private CellularNetworkKnowledgeWidget widget;
	private CellularNetworkPreference tgPreference = null;

	ThrottleGraph(CellularNetworkKnowledgeWidget widget) {
		setBorder(javax.swing.BorderFactory.createLineBorder(new Color(204,
				204, 255)));
		setMinimumSize(new Dimension(230, 100));
		setPreferredSize(new Dimension(230, 300));

		setLayout(new BorderLayout());

		// add GUI components
		chart = ChartFactory.createXYLineChart("Throttle Graph", "likelihood",
				"# interactions", null, PlotOrientation.VERTICAL, true, true,
				true); // Title, X-Axis label, Y-Axis label, Dataset, Show
		// legend, show ToolTips
		graph = new ChartPanel(chart, true);

		XYPlot newPlot = (XYPlot) chart.getPlot();

		// change the auto tick unit selection to integer units only...
		NumberAxis rangeAxis = (NumberAxis) newPlot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		chartPanel = new JPanel();
		chartPanel.setLayout(new BorderLayout());

		chartPanel.add(graph);

		legendPanel = new JPanel();
		legendPanel.setLayout(new java.awt.FlowLayout());
		legendPanel.setBackground(Color.WHITE);

		chartPanel.add(legendPanel, BorderLayout.SOUTH);

		JToolBar graphToolBar = new JToolBar();
		graphToolBar
				.setLayout(new BoxLayout(graphToolBar, BoxLayout.LINE_AXIS));
		graphToolBar.add(Box.createRigidArea(new Dimension(10, 0)));
		graphToolBar.add(new JLabel("Threshold: "));

		thresholdTypes.setPreferredSize(new Dimension(140, 25));
		thresholdTypes.setMaximumSize(new Dimension(140, 25));
		graphToolBar.add(thresholdTypes);

		thresholdTypes.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {

				if (thresholdTypes.getSelectedItem() != null) {
					String type = CellularNetworkPreferencePanel.interactionConfidenceTypeMap
							.get(thresholdTypes.getSelectedItem().toString());
					CellularNetWorkElementInformation
							.setUsedConfidenceType(new Short(type));

					// drawPlot(createCollection(0, 1, 1, true), false, true);
					// throttlePanel.repaint();

				}

			}

		});

		thresholdTextField = new JTextField("", 7);
		thresholdTextField.setMaximumSize(new Dimension(100, 50));
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
		thresholdSlider.setCursor(java.awt.Cursor
				.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		thresholdSlider
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						thresholdSlider_stateChanged();
					}
				});
		thresholdSlider
				.setToolTipText("Move the slider to change the threshold for the throttle graph");
		graphToolBar.add(thresholdTextField);
		graphToolBar.add(thresholdSlider);

		add(chartPanel, BorderLayout.CENTER);
		add(graphToolBar, BorderLayout.SOUTH);

		lengendCheckBoxList = new LengendCheckBox[8];
		jButtonList = new JButton[8];

		for (int i = 0; i < 8; i++) {
			lengendCheckBoxList[i] = new LengendCheckBox("", true);

			jButtonList[i] = new JButton();
			jButtonList[i].setPreferredSize(new java.awt.Dimension(10, 10));
		}

		this.widget = widget;
	}

	// this is invoked from CellularNetworkWidget only
	void repaint(boolean needRedraw, boolean needCreateLegendItems) {
		drawPlot(createCollection(0, 1, 1, true), needRedraw,
				needCreateLegendItems);
		super.repaint();
	}

	/**
	 * Generate the data to draw the curve.
	 * 
	 * @param min
	 * @param max
	 * @param selectedId
	 * @param active
	 * @return
	 */
	private XYSeriesCollection createCollection(double min, double max,
			int selectedId, boolean active) {

		boolean needDraw = false;
		Vector<CellularNetWorkElementInformation> hits = widget.getHits();
		if (hits != null && hits.size() > 0) {
			updateLegendList();
		} else {
			this.legendList.clear();
			thresholdTypes.removeAllItems();
			CellularNetWorkElementInformation.clearConfidenceTypes();
		}

		double maxConfidenceValue = CellularNetWorkElementInformation
				.getMaxConfidenceValue();
		if (maxConfidenceValue > 1) {
			int a = (int) Math.log10(maxConfidenceValue);
			double b = maxConfidenceValue / (Math.pow(10, a));
			double maxX = Math.round(b);
			maxX = maxX * (Math.pow(10, a));
			long smallestIncrement = (long) maxX / 100;

			CellularNetWorkElementInformation
					.setSmallestIncrement(smallestIncrement);
			this.maxX = (int) maxX;

		} else {
			CellularNetWorkElementInformation.setSmallestIncrement(0.01);
			maxX = 1;
		}

		XYSeries dataSeries = new XYSeries("Total Distribution");
		int binSize = CellularNetWorkElementInformation.getBinNumber();
		XYSeriesCollection plots = new XYSeriesCollection();
		try {

			Map<String, XYSeries> interactionDataSeriesMap = new HashMap<String, XYSeries>();
			List<String> displaySelectedInteractionTypes = widget
					.getDisplaySelectedInteractionTypes();

			for (String interactionType : displaySelectedInteractionTypes)
				interactionDataSeriesMap.put(interactionType, new XYSeries(
						interactionType));

			int[] basketValues = new int[binSize];

			Map<String, int[]> interactionBasketValuesMap = new HashMap<String, int[]>();
			for (String interactionType : displaySelectedInteractionTypes)
				interactionBasketValuesMap.put(interactionType,
						new int[binSize]);

			for (int i = 0; i < binSize; i++) {
				basketValues[i] = 0;
			}
			if (hits != null && hits.size() > 0) {
				for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
					DSGeneMarker marker = cellularNetWorkElementInformation
							.getdSGeneMarker();

					if (marker == null || marker.getGeneId() == -1
							|| cellularNetWorkElementInformation.isDirty())
						continue;
					needDraw = true;
					int[] distributionArray = cellularNetWorkElementInformation
							.getDistribution(displaySelectedInteractionTypes);

					for (int i = 0; i < binSize; i++)
						basketValues[i] += distributionArray[i];

					for (String interactionType : displaySelectedInteractionTypes) {
						int[] interactionDistribution = cellularNetWorkElementInformation
								.getInteractionDistribution(interactionType);
						int[] interactionBasketValues = interactionBasketValuesMap
								.get(interactionType);
						for (int i = 0; i < binSize; i++)
							interactionBasketValues[i] += interactionDistribution[i];

					}

				}
			}

			for (int i = 0; i < binSize; i++) {
				dataSeries.add(
						i
								* CellularNetWorkElementInformation
										.getSmallestIncrement(),
						basketValues[i]);

				for (String interactionType : displaySelectedInteractionTypes) {
					(interactionDataSeriesMap.get(interactionType)).add(
							i
									* CellularNetWorkElementInformation
											.getSmallestIncrement(),
							interactionBasketValuesMap.get(interactionType)[i]);
				}
			}

			if (hits != null && hits.size() > 0 && needDraw == true) {

				plots.addSeries(dataSeries);
				for (String interactionType : displaySelectedInteractionTypes) {
					plots.addSeries(interactionDataSeriesMap
							.get(interactionType));
				}

			} else {
				this.legendList.clear();
				thresholdTypes.removeAllItems();
				CellularNetWorkElementInformation.clearConfidenceTypes();
			}

		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return plots;
	}

	/**
	 * Respond to the change of the Threshold Text field.
	 * 
	 * @param
	 */
	private void jThresholdTextField_actionPerformed() {
		double newvalue = 0;
		try {
			newvalue = new Double(thresholdTextField.getText().trim());
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(null, "The input is not a number.",
					"Please check your input.", JOptionPane.ERROR_MESSAGE);
			return;
		}
		XYPlot plot = this.chart.getXYPlot();
		double newSliderValue = newvalue * 100 / maxX;
		thresholdSlider.setValue((int) newSliderValue);
		plot.setDomainCrosshairValue(newvalue);
		Vector<CellularNetWorkElementInformation> hits = widget.getHits();
		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
			cellularNetWorkElementInformation.setThreshold(newvalue);
		}

		widget.detailTableDataChanged();
	}

	/**
	 * Create the plot for the throttle graph.
	 * 
	 * @param plots
	 * @param title
	 */
	public void drawPlot(final XYSeriesCollection plots, boolean needRedraw,
			boolean needCreateLegendItems) {

		if (plots == null) {
			return;
		}

		boolean isToolTipEnabled = true;
		XYPlot xyPlot = null;
		XYLineAndShapeRenderer renderer = null;

		if (needRedraw || plots.getSeriesCount() == 0) {
			String context = widget.getSelectedContext();
			String version = widget.getSelectedVersion();

			if (plots.getSeriesCount() > 0) {
				tgPreference.setTitle("Throttle Graph(" + context + " v"
						+ version + ")");
			} else
				tgPreference.setTitle("Throttle Graph");

			Object selectedType = thresholdTypes.getSelectedItem();
			String xAxisLabel = "likelihood";
			if (selectedType != null && !selectedType.toString().equals(""))
				xAxisLabel = selectedType.toString();
			chart = ChartFactory.createXYLineChart(tgPreference.getTitle(),
					xAxisLabel, "#interactions", plots,
					PlotOrientation.VERTICAL, true, true, true);
			xyPlot = (XYPlot) chart.getPlot();
			chart.setBackgroundPaint(Color.white);
			Color c = UIManager.getColor("Panel.background");
			if (c != null) {
				xyPlot.setBackgroundPaint(c);
			} else {
				c = Color.white;
			}
			xyPlot.setBackgroundPaint(c);

			renderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setShapesFilled(true);
			if (isToolTipEnabled) {

				renderer.setToolTipGenerator(new XYToolTipGenerator() {

					public String generateToolTip(XYDataset dataset,
							int series, int item) {
						String resultStr = "";

						String label = (String) (dataset.getSeriesKey(series));

						double x = dataset.getXValue(series, item);
						if (Double.isNaN(x)
								&& dataset.getX(series, item) == null) {
							return resultStr;
						}

						double y = dataset.getYValue(series, item);
						if (Double.isNaN(y)
								&& dataset.getX(series, item) == null) {
							return resultStr;
						}
						String xStr = myFormatter.format(x);

						return resultStr = label
								+ ": (["
								+ xStr
								+ ", "
								+ myFormatter.format(x
										+ CellularNetWorkElementInformation
												.getSmallestIncrement())
								+ "], " + (int) y + ")";
					}
				});
			}

			xyPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
			xyPlot.setDomainGridlinePaint(Color.white);
			xyPlot.setRangeGridlinePaint(Color.white);
			xyPlot.setDomainCrosshairVisible(true);
			xyPlot.setDomainCrosshairLockedOnData(true);

			// change the auto tick unit selection to integer units only...
			NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

			ValueAxis xAxis = xyPlot.getDomainAxis();
			xAxis.setAutoRange(true);

			if (maxX <= 1)
				xAxis.setRange(0, 1);

			// OPTIONAL CUSTOMISATION COMPLETED.

			chart.addProgressListener(new ChartProgressListener() {

				public void chartProgress(ChartProgressEvent event) {
					if (event.getType() == ChartProgressEvent.DRAWING_FINISHED) {
						// set text field and slider
						JFreeChart chart = event.getChart();
						XYPlot plot = (XYPlot) chart.getPlot();
						double aCrosshair = plot.getDomainCrosshairValue();

						String s = myFormatter.format(aCrosshair);
						thresholdTextField.setText(s);

						double newSliderValue = aCrosshair / maxX * 100;

						thresholdSlider.setValue((int) newSliderValue);
					}
				}

			});

			chart.addChangeListener(new ChartChangeListener() {

				public void chartChanged(ChartChangeEvent event) {
					if (event instanceof TitleChangeEvent)
						tgPreference.setTitle(chart.getTitle().getText());
				}

			});

		} else {
			xyPlot = (XYPlot) chart.getPlot();
			xyPlot.setDataset(plots);
			renderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
			chart.setTitle(tgPreference.getTitle());
		}

		for (int i = 0; i < xyPlot.getDatasetCount(); i++) {
			renderer.setSeriesLinesVisible(i, true);
		}

		for (int i = 0; i < plots.getSeriesCount(); i++) {
			renderer.setSeriesVisible(i, true);
		}
		renderer.setSeriesVisibleInLegend(true);

		LegendItemCollection legendItems = renderer.getLegendItems();
		for (int i = 0; i < legendItems.getItemCount(); i++) {
			LegendItem lg = legendItems.get(i);
			LegendObject lo = legendList.get(lg.getLabel());
			lo.setColor((Color) lg.getFillPaint());

		}

		renderer.setSeriesVisibleInLegend(false);
		graph.setChart(chart);

		for (int i = 0; i < legendItems.getItemCount(); i++) {
			LegendItem lg = legendItems.get(i);
			LegendObject lo = legendList.get(lg.getLabel());
			if (!lo.isChecked())
				renderer.setSeriesVisible(lg.getSeriesIndex(), false);

		}

		if (needCreateLegendItems)
			createLegendPanel();

		setThresholdSliderValue();
		thresholdSlider_stateChanged();

	} // end of drawPlot

	private void thresholdSlider_stateChanged() {
		int value = thresholdSlider.getValue();
		XYPlot plot = chart.getXYPlot();

		double lowValue = (double) value * maxX / 100;

		plot.setDomainCrosshairValue(lowValue);
		String s = myFormatter.format(lowValue);
		thresholdTextField.setText(s);

		Vector<CellularNetWorkElementInformation> hits = widget.getHits();
		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
			cellularNetWorkElementInformation.setThreshold(lowValue);

		}

		widget.detailTableDataChanged();
	}

	private void setThresholdSliderValue() {
		double threshhold = 0;
		Vector<CellularNetWorkElementInformation> hits = widget.getHits();
		if (hits != null && hits.size() > 0)
			threshhold = hits.get(0).getThreshold();
		double newSliderValue = threshhold * 100 / maxX;

		thresholdSlider.setValue((int) newSliderValue);
	}

	private void createLegendPanel() {

		if (legendPanel != null) {
			legendPanel.removeAll();

		}

		for (int i = 0; i < legendList.getItemCount(); i++) {

			Color color = (Color) legendList.get(i).getColor();

			jButtonList[i].setBackground(color);

			legendPanel.add(jButtonList[i]);

			lengendCheckBoxList[i].setText(legendList.get(i).getLabel());
			lengendCheckBoxList[i].setSelected(legendList.get(i).isChecked());

			legendPanel.add(lengendCheckBoxList[i]);
		}

	}

	private class LengendCheckBox extends JCheckBox {

		private static final long serialVersionUID = -8657497943937239528L;

		public LengendCheckBox(String label, boolean isSelected) {

			super(label, isSelected);

			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {

					if (me.getSource() instanceof LengendCheckBox) {
						LengendCheckBox cb = (LengendCheckBox) me.getSource();
						String label = ((LengendCheckBox) me.getSource())
								.getText();
						LegendObject item = legendList.get(label);
						if (cb.isSelected())
							item.setSelected(true);
						else {
							item.setSelected(false);
							item.setColor(Color.GRAY);
						}

						ThrottleGraph.this.repaint(false, false);

					}
				}
			});

		}
	}

	private void updateLegendList() {
		List<String> displaySelectedInteractionTypes = widget
				.getDisplaySelectedInteractionTypes();

		if (legendList.getItemCount() == 0)
			legendList.add(new LegendObject("Total Distribution"));

		int c = legendList.getItemCount();
		for (int i = c - 1; i > 0; i--) {
			if (!displaySelectedInteractionTypes.contains(legendList.get(i)
					.getLabel()))
				legendList.remove(i);

		}
		for (String interactionType : displaySelectedInteractionTypes) {
			LegendObject item = new LegendObject(interactionType);
			if (!legendList.contains(item))
				legendList.add(item);
		}
	}

	public LegendObjectCollection getLegendItems() {
		return legendList;
	}

	public ImageSnapshotEvent createImageSnapshot() {
		Dimension panelSize = chartPanel.getSize();
		BufferedImage image = new BufferedImage(panelSize.width,
				panelSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		chartPanel.paint(g);
		ImageIcon icon = new ImageIcon(image, "CNKB Throttle Graph");
		org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
				"CNKB Throttle Graph Snapshot", icon,
				org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
		return event;
	}

	// this is invoked when the CNKB query is done
	public void refreshThresholdTypes(List<Short> confidenceTypeList,
			boolean hasRereivedRecord) {
		Object selectItem = thresholdTypes.getSelectedItem();
		thresholdTypes.removeAllItems();
		for (int i = 0; i < confidenceTypeList.size(); i++) {
			Short typeId = confidenceTypeList.get(i);
			String typeName = CellularNetworkPreferencePanel.interactionConfidenceTypeMap
					.get(typeId.toString());
			thresholdTypes.addItem(typeName);
		}
		if (hasRereivedRecord && thresholdTypes.getItemCount() > 0
				&& selectItem != null)
			thresholdTypes.setSelectedItem(selectItem.toString());
		else {
			Vector<CellularNetWorkElementInformation> hits = widget.getHits();
			if (hits.size() > 0 && thresholdTypes.getItemCount() > 0
					&& selectItem != null)
				thresholdTypes.setSelectedIndex(0);
		}

	}

	public String getThresholdText() {
		return thresholdTextField.getText().trim();
	}

	public boolean updatePreference(String context, String version) {
		if (tgPreference.getContext() == null
				|| !tgPreference.getContext().equals(context)
				|| !tgPreference.getVersion().equals(version)
				|| tgPreference.getTitle().equals("Throttle Graph")) {
			tgPreference.setContext(context);
			tgPreference.setVersion(version);
			return true;
		} else {
			return false;
		}
	}

	// invoked from CellularNetworkWidget in one place only
	public void setPreference(
			CellularNetworkPreference cellularNetworkPreference) {
		tgPreference = cellularNetworkPreference;
	}
}
