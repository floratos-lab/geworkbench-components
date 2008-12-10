package org.geworkbench.components.skybaseview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.apache.commons.lang.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSPrtDBResultSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSimpleViewer;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.openscience.jmol.ui.JmolPopup;
import org.openscience.jmol.ui.JmolPopupSwing;

/**
 * Display SkyBase blast results in table, bar chart and jmol
 * 
 * @author mw2518
 * @version $Id: SkyBaseViewer.java,v 1.4 2008-12-10 20:34:15 wangm Exp $
 * 
 */

@AcceptTypes( { DSPrtDBResultSet.class })
public class SkyBaseViewer implements VisualPlugin {
	private Log log = LogFactory.getLog(this.getClass());
	private JPanel mainPanel = new JPanel();
	private JPanel jp = new JPanel(new BorderLayout());
	private Border border = jp.getBorder();
	private JPanel jp2 = new JPanel(new BorderLayout());
	private String result = null, qname = null;
	private static String strScript = "wireframe off; spacefill off; cartoons; color structure;";
	private JmolPanel jmolPanel = new JmolPanel();
	private JmolSimpleViewer viewer = jmolPanel.getViewer();
	private Dimension prefsize1 = new Dimension(800, 235);
	private Dimension prefsize = new Dimension(770, 210);

	JTable table;
	ChartPanel cp;
	String pdburl;
	String pdbroot = "http://156.145.102.40/";
	String seqid = null;
	String blastroot = "http://156.145.238.15:8070/SkyBaseData/tmpblast/";
	String[] columnNames = { "Model Rank", "Id% Query-Model Sequence",
			"Model Start-End", "Query Start-End", "Model SeqID",
			"Model Sequence", "Query Sequence", "pG", "Coverage Template",
			"Id% Template-Model Sequences", "Template", "Template Length",
			"eValue", "Model Length", "Model Coverage", "Model Species",
			"Model Description", "Model File" };
	int colcnt = columnNames.length;
	MyBarRenderer renderer = new MyBarRenderer();
	int besti = -1;
	String colKey = null;
	String lastseqid = null;

	private String trimdot(String result) {
		int i = result.indexOf(".");
		return i > 0 ? result.substring(0, i) : result;
	}

	/*
	 * start viewer only when receiving skybase results
	 */
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet dataset = event.getDataSet();
		if (dataset instanceof DSPrtDBResultSet) {
			result = ((DSPrtDBResultSet) dataset).getDataSetName();
			qname = trimdot(result);
			try {
				jbInit(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * initialize skybase result visualization panel
	 */
	private void jbInit(String result) {
		// pdburl = "http://156.145.102.40/pdb/08-13-2008AAK22092.1.pdb";

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.removeAll();
		mainPanel.revalidate();
		mainPanel.repaint();

		jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
		jp.removeAll();
		jp.revalidate();
		jp.repaint();

		if (result == null || !result.endsWith(".blout.hits")) {
			mainPanel.add(new JTextArea("No Blast Results Found in SkyBase!"));
			mainPanel.add(new JTextArea(""));
		} else {
			try {
				URL url = new URL(blastroot + result);
				URLConnection uc = url.openConnection();

				BufferedReader br = new BufferedReader(new InputStreamReader(uc
						.getInputStream()));
				String line = null;
				int i = 0, linecnt = 0;
				while ((line = br.readLine()) != null) {
					linecnt++;
				}
				br.close();

				String[][] first = new String[linecnt][colcnt];
				String[][] data = new String[linecnt][colcnt];
				br = new BufferedReader(new InputStreamReader(url
						.openConnection().getInputStream()));
				while ((line = br.readLine()) != null) {
					first[i++] = line.split("\t");
				}
				br.close();

				String res = result.substring(0, result
						.lastIndexOf("blout.hits"))
						+ "res";
				br = new BufferedReader(new InputStreamReader(new URL(blastroot
						+ res).openConnection().getInputStream()));
				i = 0;
				String[] mod = new String[linecnt];
				Double[] cov = new Double[linecnt];
				Double[] pg = new Double[linecnt];
				Double[] pctid = new Double[linecnt];
				Hashtable<Integer, Hashtable<Double, Hashtable<Double, Hashtable<Integer, Character>>>> ht = new Hashtable<Integer, Hashtable<Double, Hashtable<Double, Hashtable<Integer, Character>>>>();
				String[][] second = new String[linecnt][];
				while ((line = br.readLine()) != null) {
					String[] toks = line.split("\t");
					ArrayList<String> al = new ArrayList<String>(Arrays
							.asList(first[i]));
					second[i] = new String[] { toks[20], toks[10], toks[5],
							toks[1], toks[13], toks[4], toks[9], toks[6],
							toks[14], toks[17], toks[32] };

					toks[3] = toks[3].substring(0, toks[3].lastIndexOf("."));
					mod[i] = toks[3];
					pctid[i] = string2double(toks[5], false);
					cov[i] = string2double(toks[10], false);
					pg[i] = string2double(toks[20], false);

					log.info(mod[i] + ": " + pctid[i] + ": " + cov[i] + ": "
							+ pg[i]);

					// rank model quality by sorting models in groups with .1
					// increment of pG:
					// quality of models with pg in [0.9, 1] > [0.8, 0.9) >
					// [0.7, 0.8) > [0, 0.7)
					// for each group, rank models by sorting template coverage,
					// then sequence identity%
					int pggrp = 0;
					if (pg[i] >= 0.9)
						pggrp = 3;
					else if (pg[i] >= 0.8)
						pggrp = 2;
					else if (pg[i] >= 0.7)
						pggrp = 1;

					if (!ht.containsKey(pggrp)) {
						ht
								.put(
										pggrp,
										new Hashtable<Double, Hashtable<Double, Hashtable<Integer, Character>>>());
					}
					if (!ht.get(pggrp).containsKey(cov[i])) {
						ht
								.get(pggrp)
								.put(
										cov[i],
										new Hashtable<Double, Hashtable<Integer, Character>>());
					}
					if (!ht.get(pggrp).get(cov[i]).containsKey(pctid[i])) {
						ht.get(pggrp).get(cov[i]).put(pctid[i],
								new Hashtable<Integer, Character>());
					}
					if (!ht.get(pggrp).get(cov[i]).get(pctid[i]).containsKey(i)) {
						ht.get(pggrp).get(cov[i]).get(pctid[i]).put(i, 'T');
					}

					al.addAll(Arrays.asList(second[i]));
					data[i++] = al.toArray(new String[colcnt]);
				}
				br.close();

				Integer[] rank = new Integer[linecnt];
				int r = linecnt;
				int b = 0;
				Vector<Integer> v1 = new Vector<Integer>(ht.keySet());
				Collections.sort(v1);
				for (Enumeration<Integer> e1 = v1.elements(); e1
						.hasMoreElements();) {
					Integer key1 = (Integer) e1.nextElement();
					if (key1 != null) {
						Vector<Double> v2 = new Vector<Double>(ht.get(key1)
								.keySet());
						Collections.sort(v2);
						for (Enumeration<Double> e2 = v2.elements(); e2
								.hasMoreElements();) {
							Double key2 = (Double) e2.nextElement();
							if (key2 != null) {
								Vector<Double> v3 = new Vector<Double>(ht.get(
										key1).get(key2).keySet());
								Collections.sort(v3);
								for (Enumeration<Double> e3 = v3.elements(); e3
										.hasMoreElements();) {
									Double key3 = (Double) e3.nextElement();
									if (key3 != null) {
										Vector<Integer> v4 = new Vector<Integer>(
												ht.get(key1).get(key2)
														.get(key3).keySet());
										Collections.sort(v4);
										for (Enumeration<Integer> e4 = v4
												.elements(); e4
												.hasMoreElements();) {
											b = (int) e4.nextElement();
											rank[b] = r--;
										}
									}
								}
							}
						}
					}
				}
				besti = b;

				String[][] data2 = new String[linecnt][colcnt + 1];
				for (int k = 0; k < first.length; k++) {
					ArrayList<String> al2 = new ArrayList<String>(Arrays
							.asList(rank[k].toString()));
					al2.addAll(Arrays.asList(first[k]));
					al2.addAll(Arrays.asList(second[k]));
					data2[k] = al2.toArray(new String[colcnt + 1]);
				}

				cp = plot_model_quality(mod, cov, pg, pctid, rank);

				HitsTableModel tableModel = new HitsTableModel(data2,
						columnNames) {
					private static final long serialVersionUID = 1L;
				};

				table = new JTable(tableModel);

				JTableHeader header = table.getTableHeader();
				TableSorter ts = new TableSorter(tableModel, header);
				table.setModel(ts);

				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				table.setPreferredScrollableViewportSize(prefsize1);
				table.getSelectionModel().addListSelectionListener(
						new MyListSelectionListener());
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				table.changeSelection(besti, 0, false, false);

			} catch (Exception e) {
				e.printStackTrace();
			}

			JScrollPane sp = new JScrollPane(table);
			sp
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			sp
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp.setPreferredSize(prefsize1);
			sp.setBorder(BorderFactory.createCompoundBorder(BorderFactory
					.createCompoundBorder(BorderFactory
							.createTitledBorder("SkyBase Blast Results for "
									+ qname), BorderFactory.createEmptyBorder(
							5, 5, 5, 5)), sp.getBorder()));

			mainPanel.add(sp);

			JScrollPane sp2 = new JScrollPane(cp);
			jp.add(sp2);

			jp2.add(jmolPanel, BorderLayout.CENTER);
			jp.add(jp2);

			mainPanel.revalidate();
			mainPanel.repaint();
			mainPanel.add(jp);
		}
	}

	public Component getComponent() {
		return mainPanel;
	}

	/*
	 * table model for skybase results
	 */
	public class HitsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private String[] columnNames;
		private Object[][] data;

		public HitsTableModel(Object[][] d, String[] cn) {
			columnNames = cn;
			data = d;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			if (col == 0)
				return Integer.valueOf((String) data[row][col]);
			return data[row][col];
		}

		public Class getColumnClass(int c) {
			if (c == 0)
				return Integer.class;
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

	/*
	 * jmol panel for selected model
	 */
	static class JmolPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		JmolViewer viewer;
		JmolAdapter adapter;
		JmolPopup popup;
		MyStatusListener listener;

		JmolPanel() {
			adapter = new SmarterJmolAdapter(null);
			viewer = JmolViewer.allocateViewer(this, adapter);
			popup = new JmolPopupSwing(viewer);
			listener = new MyStatusListener(popup);
			viewer.setJmolStatusListener(listener);
		}

		public JmolSimpleViewer getViewer() {
			return viewer;
		}

		final Dimension currentSize = new Dimension();
		final Rectangle rectClip = new Rectangle();

		public void paint(Graphics g) {
			getSize(currentSize);
			g.getClipBounds(rectClip);
			viewer.renderScreenImage(g, currentSize, rectClip);
		}
	}

	static class MyStatusListener implements JmolStatusListener {

		JmolPopup jmolpopup;

		public MyStatusListener(JmolPopup jmolpopup) {
			this.jmolpopup = jmolpopup;
		}

		public void notifyFileLoaded(String fullPathName, String fileName,
				String modelName, Object clientFile, String errorMsg) {
			jmolpopup.updateComputedMenus();
		}

		public void setStatusMessage(String statusMessage) {
			if (statusMessage == null)
				return;
		}

		public void scriptEcho(String strEcho) {
			scriptStatus(strEcho);
		}

		public void scriptStatus(String strStatus) {
		}

		public void notifyScriptTermination(String errorMessage, int msWalltime) {
		}

		public void handlePopupMenu(int x, int y) {
			if (jmolpopup != null)
				jmolpopup.show(x, y);
		}

		public void measureSelection(int atomIndex) {
		}

		public void notifyMeasurementsChanged() {
		}

		public void notifyFrameChanged(int frameNo) {
		}

		public void notifyAtomPicked(int atomIndex, String strInfo) {
		}

		public void showUrl(String urlString) {
		}

		public void showConsole(boolean showConsole) {
		}

	}

	/*
	 * download web content from fname
	 */
	private StringBuffer getContent(String fname) {
		StringBuffer contents = null;
		try {
			URL url = new URL(fname);
			URLConnection uc = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(uc
					.getInputStream()));

			contents = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				contents.append(line);
				contents.append("\n");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.info("getContent notconnected error: " + fname);
		}
		return contents;
	}

	/*
	 * when table selection is changed, bar chart and jmol will change too
	 */
	public class MyListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;
			ListSelectionModel l = (ListSelectionModel) e.getSource();
			if (l.isSelectionEmpty()) {
				int rowcnt = table.getModel().getRowCount();
				for (int j = 0; j < rowcnt; j++) {
					if (table.getModel().getValueAt(j, 4).equals(colKey)) {
						table.changeSelection(j, 0, false, false);
						break;
					}
				}
			} else {
				int selectedRow = l.getMinSelectionIndex();
				TableModel tm = table.getModel();
				seqid = (String) tm.getValueAt(selectedRow, 4);

				if (!seqid.equals(lastseqid)) {
					String pdblink = (String) tm.getValueAt(selectedRow,
							colcnt - 1);
					pdburl = pdbroot + pdblink;

					StringBuffer contents = getContent(pdburl);
					viewer.openStringInline(contents.toString());
					viewer.evalString(strScript);

					jp2.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createCompoundBorder(BorderFactory
									.createTitledBorder(seqid), BorderFactory
									.createEmptyBorder(5, 5, 5, 5)), border));

					renderer.setcol(seqid);
					lastseqid = seqid;
				}
				colKey = seqid;
			}
		}
	}

	/*
	 * model quality chart based on pG, template coverage, template-model
	 * sequence identity% and model rank
	 */
	private ChartPanel plot_model_quality(String[] mod, Double[] cov,
			Double[] pg, Double[] pctid, Integer[] rank) {
		DefaultCategoryDataset[] sets = create_datasets(mod, cov, pg, pctid,
				rank);

		JFreeChart ch = ChartFactory.createBarChart(null, null,
				"pG / Coverage / Identity", sets[0], PlotOrientation.VERTICAL,
				false, true, false);
		ch.setTitle(new TextTitle("Models for SkyBase Blast Results", new Font(
				"Arial", Font.BOLD, 12)));
		ch.setBackgroundPaint(new Color(225, 225, 225));

		CategoryPlot plot = ch.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
		plot.setRenderer(0, renderer);
		renderer.setDrawBarOutline(false);
		renderer
				.setGradientPaintTransformer(new StandardGradientPaintTransformer(
						GradientPaintTransformType.HORIZONTAL));

		Color gp0 = new Color(100, 205, 100, 255);
		Color gp1 = new Color(132, 112, 255, 255);
		Color gp2 = new Color(255, 255, 0, 255);

		renderer.setSeriesPaint(0, gp0);
		renderer.setSeriesPaint(1, gp1);
		renderer.setSeriesPaint(2, gp2);

		renderer
				.setLegendItemToolTipGenerator(new StandardCategorySeriesLabelGenerator(
						"ToolTip: {0}"));
		renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());

		plot.setDataset(1, sets[1]);
		plot.mapDatasetToRangeAxis(1, 1);
		NumberAxis axis2 = new NumberAxis("Model Rank");
		axis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		plot.setRangeAxis(1, axis2);
		axis2.setRange(0, sets[1].getColumnCount());
		LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
		renderer2.setToolTipGenerator(new StandardCategoryToolTipGenerator());
		plot.setRenderer(1, renderer2);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		LegendTitle legend = new LegendTitle(renderer);
		legend.setPosition(RectangleEdge.TOP);
		ch.addLegend(legend);

		ChartPanel cp = new ChartPanel(ch);
		cp.setPreferredSize(prefsize);
		cp.addChartMouseListener(new ChartMouseListener() {
			public void chartMouseClicked(ChartMouseEvent ev) {
				try {
					if (ev.getChart() != null & ev.getEntity() != null) {
						CategoryItemEntity cie = (CategoryItemEntity) ev
								.getEntity();
						if (cie.getCategory() == null)
							return;
						if (cie.getCategoryIndex() >= 0) {
							cie = (CategoryItemEntity) ev.getEntity();
							colKey = (String) cie.getCategory();
							int rowcnt = table.getModel().getRowCount();
							for (int j = 0; j < rowcnt; j++) {
								if (table.getModel().getValueAt(j, 4).equals(
										colKey)) {
									table.changeSelection(j, 0, false, false);
									break;
								}
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			public void chartMouseMoved(ChartMouseEvent ev) {
			}
		});
		return cp;
	}

	/*
	 * create category datasets for bar chart
	 */
	private DefaultCategoryDataset[] create_datasets(String[] mod,
			Double[] cov, Double[] pg, Double[] pctid, Integer[] rank) {
		DefaultCategoryDataset set1 = new DefaultCategoryDataset();
		DefaultCategoryDataset set2 = new DefaultCategoryDataset();
		String ser1 = "Model Quality pG";
		String ser2 = "Template Coverage";
		String ser3 = "Model-Template Sequence Identity";
		String ser4 = "Model Rank";
		int i = 0;
		for (i = 0; i < mod.length; i++) {
			set1.addValue(pg[i], ser1, mod[i]);
			set1.addValue(cov[i], ser2, mod[i]);
			set1.addValue(pctid[i], ser3, mod[i]);

			set2.addValue(rank[i], ser4, mod[i]);
			if (i == besti) {
				colKey = mod[i];
			}
		}
		DefaultCategoryDataset[] sets = { set1, set2 };
		return sets;
	}

	private Double string2double(String s, boolean pct) {
		Double r = 0.0;
		int k = 0;
		if (NumberUtils.isNumber(s)) {
			if ((k = s.indexOf("%")) > 0)
				r = Double.valueOf(s.substring(0, k)).doubleValue() * 0.01;
			else
				r = Double.valueOf(s).doubleValue();
		} else
			log.info("Warning: " + s + " is not a number!");

		if (pct == true)
			r = r * 100;
		return r;
	}

}
