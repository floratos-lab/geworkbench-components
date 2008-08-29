package org.geworkbench.components.netboost;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.pathwaydecoder.mutualinformation.NetBoostData;
import org.geworkbench.util.pathwaydecoder.mutualinformation.NetBoostDataSet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * NetBoost Plugin
 * 
 * @author ch2514
 * @version $Id: NetBoostPlugin.java,v 1.6 2008-08-29 21:54:23 hungc Exp $
 */

public class NetBoostPlugin extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// variable
	private static Log log = LogFactory.getLog(NetBoostPlugin.class);

	private static final String BOOST_ITERATION_TITLE = "Net Boost";

	private static final String BOOST_ITERATION_X_LABEL = "Boosting Iterations";

	private static final String BOOST_ITERATION_Y_LABEL = "Losses";

	private static final String SCORES_TABLE_TITLE = "Scores";

	private static final String SCORES_TABLE_MODEL = "Model";

	private static final String SCORES_TABLE_DATA = "Score +/- Variance";

	private static final String CONFUSION_MATRIX_TITLE = "Confusion Matrix";

	private static final String CONFUSION_MATRIX_X_LABEL = "PREDICTIONS";

	private static final String CONFUSION_MATRIX_Y_LABEL = "TRUTH";

	JFreeChart iterChart;

	ChartPanel iterChartPanel;

	JButton imageSnapshotButton, csvExportButton;

	JTable scoresTable, confusionMatrix;

	NetBoostData nbdata;

	DefaultTableXYDataset iterDataSet;

	ScoresModel scoresModel;

	ConfusionModel confusedModel;

	/**
	 * 
	 * @param data
	 * @param visualPlugin
	 */
	public NetBoostPlugin(NetBoostDataSet data,
			NetBoostVisualComponent visualPlugin) {
		final NetBoostVisualComponent vp = visualPlugin;
		this.nbdata = data.getData();

		// init internal panels
		JPanel leftPanel = new JPanel(new BorderLayout());
		JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel rightPanel = new JPanel(new GridLayout(2, 1, 20, 20));
		JPanel rightTopPanel = new JPanel(new BorderLayout());
		JScrollPane rightTopTablePanel = new JScrollPane(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel rightBottomPanel = new JPanel(new BorderLayout());
		JScrollPane rightBottomTablePanel = new JScrollPane(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel rightBottomTitlePanel = new JPanel(new GridLayout(2, 1));

		// boost iteration chart
		iterDataSet = new DefaultTableXYDataset();
		iterChart = ChartFactory.createXYLineChart(
				BOOST_ITERATION_TITLE // + "(" + data.getFilename() + ")"
				, BOOST_ITERATION_Y_LABEL, BOOST_ITERATION_X_LABEL,
				new IterDataSet().getDataSet(), PlotOrientation.VERTICAL, true,
				true, false);
		XYPlot plot = iterChart.getXYPlot();
		NumberAxis domainAxis = new NumberAxis(BOOST_ITERATION_X_LABEL);
		NumberAxis rangeAxis = new NumberAxis(BOOST_ITERATION_Y_LABEL);
		plot.setDomainAxis(domainAxis);
		plot.setRangeAxis(rangeAxis);
		iterChart.setBackgroundPaint(Color.white);
		plot.setOutlinePaint(Color.black);
		iterChartPanel = new ChartPanel(iterChart);
		iterChartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		imageSnapshotButton = new JButton("   Image Snapshot   ");
		imageSnapshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				vp.createImageSnapshot(iterChartPanel);
			}
		});
		csvExportButton = new JButton("  Export to CSV  ");
		csvExportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				exportToCSV();
			}
		});

		leftBottomPanel.add(imageSnapshotButton);
		leftBottomPanel.add(csvExportButton);
		leftPanel.add(iterChartPanel, BorderLayout.CENTER);
		leftPanel.add(leftBottomPanel, BorderLayout.SOUTH);

		// scores table
		JLabel scoresTableTitle = new JLabel(SCORES_TABLE_TITLE,
				SwingConstants.LEADING);
		scoresTableTitle.setFont(new Font("Arial", Font.BOLD, 12));
		scoresModel = new ScoresModel();
		scoresTable = new JTable(scoresModel) {
			public Component prepareRenderer(
					TableCellRenderer tableCellRenderer, int row, int col) {
				Component component = super.prepareRenderer(
						tableCellRenderer, row, col);
				if(row == 0){					
					component.setFont(new Font(component.getFont().getFontName(), Font.BOLD, component.getFont().getSize()));
				}				
				return component;
			}
		};
		scoresTable.setAutoCreateColumnsFromModel(false);

		rightTopTablePanel.getViewport().add(scoresTable);
		rightTopPanel.add(scoresTableTitle, BorderLayout.NORTH);
		rightTopPanel.add(rightTopTablePanel, BorderLayout.CENTER);

		// confusion matrix
		JLabel confusedTitle = new JLabel(CONFUSION_MATRIX_TITLE,
				SwingConstants.LEADING);
		JLabel confusedX = new JLabel(CONFUSION_MATRIX_X_LABEL,
				SwingConstants.CENTER);
		confusedTitle.setFont(new Font("Arial", Font.BOLD, 12));
		confusedModel = new ConfusionModel();
		confusionMatrix = new JTable(confusedModel);
		confusionMatrix.setAutoCreateColumnsFromModel(false);

		rightBottomTitlePanel
				.add(new JPanel(new FlowLayout(FlowLayout.LEADING))
						.add(confusedTitle));
		rightBottomTitlePanel.add(new JPanel(new FlowLayout(FlowLayout.CENTER))
				.add(confusedX));
		rightBottomTablePanel.getViewport().add(confusionMatrix);
		rightBottomPanel.add(rightBottomTitlePanel, BorderLayout.NORTH);
		rightBottomPanel.add(rightBottomTablePanel, BorderLayout.CENTER);

		// arrange internal panels
		rightPanel.add(rightTopPanel);
		rightPanel.add(rightBottomPanel);

		this.setLayout(new GridLayout(1, 2));
		this.add(new JScrollPane(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
				.getViewport().add(leftPanel));
		this.add(rightPanel);
	}

	/**
	 * 
	 * @return
	 */
	private String convertToCSV() {
		StringBuilder scores = new StringBuilder(
				"Scores\nModel,Score +/- Variance\n");
		StringBuilder confused = new StringBuilder();

		// scores
		for (int i = 0; i < scoresModel.models.length; i++) {
			scores.append(scoresModel.models[i]);
			scores.append(",");
			scores.append(scoresModel.scores[i]);
			scores.append(" +/- ");
			scores.append(scoresModel.variances[i]);
			scores.append("\n");
		}

		// confusion matrix
		StringBuilder headings = new StringBuilder();
		for (int i = 0; i < confusedModel.rowNames.length; i++) {
			headings.append(",");
			headings.append(confusedModel.rowNames[i]);
			confused.append(confusedModel.rowNames[i]);
			for (int j = 0; j < confusedModel.data[i].length; j++) {
				confused.append(",");
				confused.append(confusedModel.data[i][j]);
				confused.append("%");
			}
			confused.append("\n");
		}
		headings.append("\n");
		confused.insert(0, headings.toString());
		confused.insert(0, "Confusion Matrix\n");

		return scores.toString() + "\n\n" + confused.toString();
	}

	/**
	 * 
	 * 
	 */
	private void exportToCSV() {
		File currentDir = new File(".");
		JFileChooser fc = new JFileChooser(currentDir);
		NetBoostFileFilter nbFilter = new NetBoostFileFilter();
		fc.setFileFilter(nbFilter);
		int choice = fc.showOpenDialog(this);
		String nbFilename = null;
		if (choice == JFileChooser.APPROVE_OPTION) {
			nbFilename = fc.getSelectedFile().getAbsolutePath();
			if (!nbFilename.endsWith("." + nbFilter.getExtension())) {
				nbFilename += "." + nbFilter.getExtension();
			}
			PrintWriter pw = null;
			String s = convertToCSV();
			log.debug("Writing to file [" + nbFilename + "]\n" + s);
			try {
				pw = new PrintWriter(new FileOutputStream(nbFilename, true));
				pw.println(s);
				log.info("Wrote NetBoost results to file: " + nbFilename);
			} catch (Exception e) {
				log.error("Cannot export NetBoost charts to csv: "
						+ e.getMessage());
			} finally {
				if (pw != null)
					pw.close();
			}
		}
	}

	/**
	 * 
	 * @author ch2514
	 * 
	 */
	private class NetBoostFileFilter extends FileFilter {
		public String getDescription() {
			return "NetBoost Files";
		}

		public boolean accept(File f) {
			String name = f.getName();
			boolean nbFile = name.endsWith("csv") || name.endsWith("CSV");
			if (f.isDirectory() || nbFile) {
				return true;
			}

			return false;
		}

		public String getExtension() {
			return "csv";
		}

	}

	/**
	 * 
	 * @author ch2514
	 * 
	 */
	private class IterDataSet {
		// TODO: change test/train loss variable names...
		// Beware that the train loss and test loss variable names are reversed
		// in this class and NetBoostData class.
		private static final String TEST_LOSS = "Train Loss";

		private static final String TRAIN_LOSS = "Test Loss";

		private static final String BOUND = "Bound";

		private static final String BIAS = "Bias";

		private XYSeriesCollection dataset;

		private XYSeries testLossSeries, trainLossSeries, boundSeries, biasSeries; 

		/**
		 * 
		 */
		public IterDataSet() {
			testLossSeries = new XYSeries(TEST_LOSS);
			trainLossSeries = new XYSeries(TRAIN_LOSS);
			boundSeries = new XYSeries(BOUND);
			biasSeries = new XYSeries(BIAS);

			double[][] tl = nbdata.getTestLoss();
			for (int i = 0; i < tl.length; i++) {
				testLossSeries.add(tl[i][0], tl[i][1]);
			}
			tl = nbdata.getTrainLoss();
			for (int i = 0; i < tl.length; i++) {
				trainLossSeries.add(tl[i][0], tl[i][1]);
			}

			dataset = new XYSeriesCollection();
			dataset.addSeries(trainLossSeries);
			dataset.addSeries(boundSeries);
			dataset.addSeries(testLossSeries);
			dataset.addSeries(biasSeries);
		}

		/**
		 * 
		 * @return
		 */
		public XYSeriesCollection getDataSet() {
			return this.dataset;
		}
	}

	/**
	 * 
	 * @author ch2514
	 * 
	 */
	private class ScoresModel extends AbstractTableModel {
		private String[] columnNames = { SCORES_TABLE_MODEL, SCORES_TABLE_DATA };

		private String[] models = nbdata.getModels();

		private double[] scores = nbdata.getScores();

		private double[] variances = nbdata.getVariances();

		/**
		 * 
		 * 
		 */
		public ScoresModel() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return 2;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			return models.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		public String getColumnName(int col) {
			return columnNames[col];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int col) {
			if (col == 0)
				return models[row];
			if (col == 1)
				return new String(scores[row] + " +/- " + variances[row]);

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
		 *      int, int)
		 */
		public void setValueAt(Object value, int row, int col) {
			// do nothing for now
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			String result = "";

			return result;
		}

	}

	/**
	 * 
	 * @author ch2514
	 * 
	 */
	private class ConfusionModel extends AbstractTableModel {
		private String[] columnNames = new String[nbdata.getModels().length + 1];

		private String[] rowNames = nbdata.getModels();

		private double[][] data;

		/**
		 * 
		 * 
		 */
		public ConfusionModel() {
			columnNames[0] = CONFUSION_MATRIX_Y_LABEL;
			for (int i = 1; i < columnNames.length; i++) {
				columnNames[i] = nbdata.getModels()[i - 1];
			}
			data = nbdata.getConfusedData();
			String[] s = nbdata.getModels();
			if ((s != null) && (s.length > 0)) {
				rowNames = s;
				columnNames = new String[rowNames.length + 1];
				columnNames[0] = CONFUSION_MATRIX_Y_LABEL;
				for (int i = 0; i < rowNames.length; i++) {
					columnNames[i + 1] = rowNames[i];
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return columnNames.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			return rowNames.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		public String getColumnName(int col) {
			return columnNames[col];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int col) {
			if (col == 0)
				return rowNames[row];
			else
				return new String(data[col - 1][row] + "%");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
		 *      int, int)
		 */
		public void setValueAt(Object value, int row, int col) {
			// do nothing for now
		}
	}
}
