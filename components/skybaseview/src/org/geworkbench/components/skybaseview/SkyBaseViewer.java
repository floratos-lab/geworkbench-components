package org.geworkbench.components.skybaseview;

import jalview.bin.Cache;
import jalview.gui.AlignFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.CSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.SkybaseResultSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.FilePathnameUtils;
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

/**
 * Display SkyBase blast results in table, bar chart and jmol
 * 
 * @author mw2518
 * @version $Id$
 * 
 */

@AcceptTypes( { SkybaseResultSet.class })
public class SkyBaseViewer implements VisualPlugin {
	private Log log = LogFactory.getLog(this.getClass());
	private JPanel mainPanel = new JPanel();
	private JPanel mPanel = new JPanel();
	private JTabbedPane jtp = new JTabbedPane();
	private JPanel alnt = new JPanel(new BorderLayout());
	private JPanel alnq = new JPanel(new BorderLayout());
	private String result = null, qname = null, lastqname = null;
	private static String strScript = "wireframe off; spacefill off; cartoons; color structure;";
	private JmolPanel jmolPanel = new JmolPanel();
	private JmolSimpleViewer viewer = jmolPanel.getViewer();
	private Dimension prefsize = new Dimension(770, 210);
	JLabel seqlb;
	int cnt = 0;
	HashMap<String, String> qbesti = new HashMap<String, String>();
	HashMap<String, String> qlasti = new HashMap<String, String>();
	JTable table;
	ChartPanel cp;
	String pdburl;
	String pdbroot = "http://skybase.c2b2.columbia.edu";
	String[] pdbroots = new String[]{pdbroot + "/nesg3/skynesg/", pdbroot + "/pdb60/sky/"};
	String dbtype = "";
	String[] dbtypes = new String[]{"NESG", "PDB60"};
	String seqid = null;
	//String blastroot = "http://skyline.c2b2.columbia.edu:8080/SkyBaseData/tmpblast/";
	String blastroot = "http://cagridnode.c2b2.columbia.edu:8080/v2.0.0/SkyBaseData/tmpblast/";
	String[] columnNames = { "Rank", "Id% Query-Model Sequence",
			"Model Start-End", "Query Start-End", "Model SeqID",
			"Model Sequence", "Query Sequence", "pG", "Coverage Template",
			"Id% Template-Model Sequences", "Template", "Template Length",
			"eValue", "Model Length", "Model Coverage", "Model Species",
			"Model Description", "Model File", "Template-Model Alignment" };
	int colcnt = columnNames.length;
	MyBarRenderer renderer = new MyBarRenderer();
	int besti = -1;
	int lasti = -1;
	String colKey = null;
	String lastseqid = null;
	String ofname = "";
	String tmpfiledir = FilePathnameUtils.getTemporaryFilesDirectoryPath()
			+ "webpdb/";
	File webpdbdir = new File(tmpfiledir);
	JComboBox alncombo[] = new JComboBox[2];
	@SuppressWarnings("unchecked")
	private Vector<String> vec[] = new Vector[2];
	private final NumberFormat dblformatter = new DecimalFormat("0.00");
	private final NumberFormat decformatter = new DecimalFormat("0.0E00");
	private final NumberFormat pctformatter = NumberFormat.getPercentInstance();

	private String trimdot(String result) {
		int i = result.indexOf(".");
		return i > 0 ? result.substring(0, i) : result;
	}

	/*
	 * start viewer only when receiving skybase results
	 */
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataset = event.getDataSet();
		if (dataset instanceof SkybaseResultSet) {
			result = ((SkybaseResultSet) dataset).getDataSetName();
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
		if (!webpdbdir.exists())
			webpdbdir.mkdir();
		seqlb = new JLabel();

		JButton add2prj = new JButton();
		JButton t_alnview = new JButton();
		JButton q_alnview = new JButton();

		mainPanel.setLayout(new GridLayout());
		mainPanel.add(jtp);
		jtp.add("SkyBase Models", mPanel);
		jtp.add("Template Alignment", alnt);
		jtp.add("Query Alignment", alnq);

		mPanel.setLayout(new BoxLayout(mPanel, BoxLayout.PAGE_AXIS));
		mPanel.removeAll();
		mPanel.revalidate();
		mPanel.repaint();
		alnt.removeAll();
		alnq.removeAll();

		JPanel jp = new JPanel();
		jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));

		if (result == null || !result.endsWith(".blout.hits")) {
			mPanel.add(new JTextArea("No Blast Results Found in SkyBase!"));
			mPanel.add(new JTextArea(""));
		} else {
			try {
				String indexurl = System.getProperty("indexServer.url");
				Properties p = getProperties("analysis");
				indexurl = p.getProperty("indexServerURL", indexurl);
				int id = 0;
				if (indexurl!=null && (id = indexurl.indexOf("/wsrf/")) > -1)
					blastroot = indexurl.substring(0, id)+"/SkyBaseData/tmpblast/";
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

				String[][] first = new String[linecnt][];
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
				dbtype = "";
				while ((line = br.readLine()) != null) {
					String[] toks = line.split("\t");
					if (dbtype.equals("") && !toks[32].equals("NA")){
						if (toks[32].startsWith("pipeline_")){
							dbtype  = dbtypes[1];
							pdbroot = pdbroots[1];
						} else {
							dbtype  = dbtypes[0];
							pdbroot = pdbroots[0];
						}
					}

					String aln = new String(toks[26]+" - "+toks[27]+"\n\n"+toks[28]+"\n"+toks[29]+" - "+toks[30]+"\n\n"+toks[31]+"\n");
					second[i] = new String[] { toks[20], toks[10], toks[5],
							toks[1], toks[13], toks[4], toks[9], toks[6],
							toks[14], toks[17], toks[32], aln };

					toks[3] = toks[3].substring(0, toks[3].lastIndexOf("."));
					mod[i] = toks[3];
					pctid[i] = string2double(toks[5], false);
					cov[i] = string2double(toks[10], false);
					pg[i] = string2double(toks[20], false);

					log.debug(mod[i] + ": " + pctid[i] + ": " + cov[i] + ": "
							+ pg[i] + ":" + aln);

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
						ht.put(pggrp, new Hashtable<Double, Hashtable<Double, Hashtable<Integer, Character>>>());
					}
					if (!ht.get(pggrp).containsKey(cov[i])) {
						ht.get(pggrp).put(cov[i], new Hashtable<Double, Hashtable<Integer, Character>>());
					}
					if (!ht.get(pggrp).get(cov[i]).containsKey(pctid[i])) {
						ht.get(pggrp).get(cov[i]).put(pctid[i], new Hashtable<Integer, Character>());
					}
					if (!ht.get(pggrp).get(cov[i]).get(pctid[i]).containsKey(i)) {
						ht.get(pggrp).get(cov[i]).get(pctid[i]).put(i, 'T');
					}
					i++;
				}
				br.close();

				Integer[] rank = new Integer[linecnt];
				int r = linecnt;
				int b = 0;
				Vector<Integer> v1 = new Vector<Integer>(ht.keySet());
				Collections.sort(v1);
				for (Enumeration<Integer> e1 = v1.elements(); e1.hasMoreElements();) {
					Integer key1 = (Integer) e1.nextElement();
					if (key1 != null) {
						Vector<Double> v2 = new Vector<Double>(ht.get(key1).keySet());
						Collections.sort(v2);
						for (Enumeration<Double> e2 = v2.elements(); e2.hasMoreElements();) {
							Double key2 = (Double) e2.nextElement();
							if (key2 != null) {
								Vector<Double> v3 = new Vector<Double>(ht.get(key1).get(key2).keySet());
								Collections.sort(v3);
								for (Enumeration<Double> e3 = v3.elements(); e3.hasMoreElements();) {
									Double key3 = (Double) e3.nextElement();
									if (key3 != null) {
										Vector<Integer> v4 = new Vector<Integer>(ht.get(key1).get(key2).get(key3).keySet());
										Collections.sort(v4);
										for (Enumeration<Integer> e4 = v4.elements(); e4.hasMoreElements();) {
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

				String[][] data2 = new String[linecnt][colcnt];
				for (int k = 0; k < first.length; k++) {
					ArrayList<String> al2 = new ArrayList<String>(Arrays.asList(rank[k].toString()));
					al2.addAll(Arrays.asList(first[k]));
					al2.addAll(Arrays.asList(second[k]));
					data2[k] = al2.toArray(new String[colcnt]);
				}

				cp = plot_model_quality(mod, cov, pg, pctid, rank);

				HitsTableModel tableModel = new HitsTableModel(data2, columnNames) {
					private static final long serialVersionUID = 1L;
				};

				table = new JTable(tableModel);

				JTableHeader header = table.getTableHeader();
				TableSorter ts = new TableSorter(tableModel, header);
				table.setModel(ts);
				CellRenderer renderer = new CellRenderer();
				Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
				while(columns.hasMoreElements()){
					columns.nextElement().setCellRenderer(renderer);
				}
				table.updateUI();

				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				table.getSelectionModel().addListSelectionListener(
						new MyListSelectionListener());
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

				if (qbesti.get(qname) == null) {
					seqid = first[besti][3];
					qbesti.put(qname, seqid);
					table.changeSelection(besti, 0, false, false);
				} else {
					seqid = qbesti.get(qname);
					for (int j = 0; j < first.length; j++) {
						if (table.getModel().getValueAt(j, 4).equals(seqid)) {
							table.changeSelection(j, 0, false, false);
							break;
						}
					}
				}

				Cache.initLogger();
			} catch (Exception e) {
				e.printStackTrace();
			}

			JScrollPane sp = new JScrollPane(table);
			sp
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			sp
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			JScrollPane sp2 = new JScrollPane(cp);
			jp.add(sp2);

			JPanel jp2 = new JPanel(new BorderLayout());
			jp2.add(jmolPanel, BorderLayout.CENTER);

			JPanel buttons = new JPanel();
			seqlb.setText(seqid);
			seqlb.setFont(new Font("Arial", Font.BOLD, 12));
			buttons.add(seqlb);

			add2prj.setText("ATP");
			add2prj.setToolTipText("Add To Project");
			add2prj.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					add2prj_actionPerformed(e);
				}
			});
			buttons.add(add2prj);

			for (int v = 0; v < 2; v++) {
				alncombo[v] = new JComboBox();
				vec[v] = new Vector<String>();
			}

			t_alnview.setText("VAT");
			t_alnview.setToolTipText("View Alignment between Model-Template");
			t_alnview.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					t_alnview_actionPerformed(e);
				}
			});
			buttons.add(t_alnview);

			q_alnview.setText("VAQ");
			q_alnview.setToolTipText("View Alignment between Model-Query");
			q_alnview.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					q_alnview_actionPerformed(e);
				}
			});
			buttons.add(q_alnview);

			jp2.add(buttons, BorderLayout.SOUTH);
			jp.add(jp2);

			mPanel.add(jp);
			mPanel.add(sp);
			mPanel.revalidate();
			mPanel.repaint();
		}
	}

	private Properties getProperties(String name) throws IOException {
        File confFile = new File(FilePathnameUtils.getComponentConfigurationSettingsDir(name) + name + ".xml");
        if (confFile.exists()) {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream(confFile);
            try {
                props.loadFromXML(in);
            } finally {
                in.close();
            }
            return props;
        } else {
            return new Properties();
        }
    }
	
	public void add2prj_actionPerformed(java.awt.event.ActionEvent e) {
		CSProteinStructure dsp = new CSProteinStructure(null, seqid);
		dsp.setFile(new File(ofname));
		ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
				"skybase model", dsp, null);
		publishProjectNodeAddedEvent(event);
	}

	public void t_alnview_actionPerformed(java.awt.event.ActionEvent e) {
		int rowcnt = table.getModel().getRowCount();
		for (int j = 0; j < rowcnt; j++) {
			if (table.getModel().getValueAt(j, 4).equals(colKey)) {
				String aln = (String) table.getModel()
						.getValueAt(j, colcnt - 1);

				String afname = tmpfiledir + seqid + ".t_aln";

				printfile(afname, aln);
				alncombo[0] = getAlnCombo(afname, 0);
				alnt.removeAll();
				alnt.add(getAlnFrame(afname), BorderLayout.CENTER);
				alnt.add(alncombo[0], BorderLayout.PAGE_START);
				jtp.setSelectedIndex(1);
				break;
			}
		}
	}

	public void q_alnview_actionPerformed(java.awt.event.ActionEvent e) {
		int rowcnt = table.getModel().getRowCount();
		for (int j = 0; j < rowcnt; j++) {
			if (table.getModel().getValueAt(j, 4).equals(colKey)) {
				String aln = ">" + seqid + "\n"
						+ (String) table.getModel().getValueAt(j, 5) + "\n"
						+ ">" + qname + "\n"
						+ (String) table.getModel().getValueAt(j, 6) + "\n";

				String afname = tmpfiledir + seqid + ".q_aln";

				printfile(afname, aln);
				alncombo[1] = getAlnCombo(afname, 1);
				alnq.removeAll();
				alnq.add(getAlnFrame(afname), BorderLayout.CENTER);
				alnq.add(alncombo[1], BorderLayout.PAGE_START);
				jtp.setSelectedIndex(2);
				break;
			}
		}
	}

	public void printfile(String afname, String aln) {
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(afname)));
			pw.print(aln);
			pw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public AlignFrame getAlnFrame(String afname) {
		String color = "CLUSTAL";
		String protocol = "File";
		String format = new jalview.io.IdentifyFile()
				.Identify(afname, protocol);
		jalview.io.FileLoader fileLoader = new jalview.io.FileLoader();
		AlignFrame af = fileLoader.LoadFileWaitTillLoaded(afname, protocol,
				format);
		jalview.schemes.ColourSchemeI cs = jalview.schemes.ColourSchemeProperty
				.getColour(af.getViewport().getAlignment(), color);
		af.changeColour(cs);
		af.toFront();
		af.setVisible(true);
		af.setClosable(true);
		af.setResizable(true);
		af.setMaximizable(true);
		af.setIconifiable(true);
		af.setFrameIcon(null);
		af.setPreferredSize(new java.awt.Dimension(1000, 400));
		return af;
	}

	public JComboBox getAlnCombo(String afname, int type) {
		if (!vec[type].contains(afname)) {
			vec[type].add(afname);
			alncombo[type].addItem(afname);
		}
		alncombo[type].setSelectedIndex(vec[type].indexOf(afname));
		if (alncombo[type].getActionListeners().length == 0) {
			if (type == 0)
				alncombo[type]
						.addActionListener(new java.awt.event.ActionListener() {
							public void actionPerformed(
									java.awt.event.ActionEvent e) {
								String selectedfile = ((JComboBox) e
										.getSource()).getSelectedItem()
										.toString();
								alnt.removeAll();
								alnt.add(getAlnFrame(selectedfile),
										BorderLayout.CENTER);
								alnt.add(alncombo[0], BorderLayout.PAGE_START);
							}
						});
			else
				alncombo[type]
						.addActionListener(new java.awt.event.ActionListener() {
							public void actionPerformed(
									java.awt.event.ActionEvent e) {
								String selectedfile = ((JComboBox) e
										.getSource()).getSelectedItem()
										.toString();
								alnq.removeAll();
								alnq.add(getAlnFrame(selectedfile),
										BorderLayout.CENTER);
								alnq.add(alncombo[1], BorderLayout.PAGE_START);
							}
						});
		}
		return alncombo[type];
	}

	public Component getComponent() {
		return mainPanel;
	}

	private class CellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -1427727373354274060L;
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int col) {
			if (col == 0)
				setHorizontalAlignment(JLabel.CENTER);
			try{
				String colname = columnNames[col];
				if (colname.equals("pG"))
					setText((value == null) ? "" : dblformatter.format((Number)value));
				else if (colname.equals("eValue"))
					setText((value == null) ? "" : decformatter.format((Number)value));
				else if (colname.startsWith("Id%") || colname.contains("Coverage"))
					setText((value == null) ? "" : pctformatter.format((Number)value));
				else setText((value == null) ? "" : value.toString());
			}catch(Exception e){
				setText((value == null) ? "" : value.toString());
				e.printStackTrace();
			}
			if (table.isRowSelected(row))
				setBackground(table.getSelectionBackground());
			else setBackground(table.getBackground());
			return this;
	    }
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
			String colname = columnNames[col];
			try{
				if (colname.equals("pG"))
					return Double.valueOf((String)data[row][col]);
				if (colname.equals("eValue"))
					return decformatter.parse(data[row][col].toString().toUpperCase()).doubleValue();
				if (colname.endsWith("Length"))
					return Integer.valueOf((String)data[row][col]);
				if (colname.startsWith("Id%") || colname.contains("Coverage"))
					return pctformatter.parse(data[row][col].toString()).doubleValue();
			}catch(Exception e){
				e.printStackTrace();
			}
			return data[row][col];
		}

		public Class<?> getColumnClass(int c) {
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
		JmolSimpleViewer viewer;
		JmolAdapter adapter;

		JmolPanel() {
			adapter = new SmarterJmolAdapter();
			viewer = JmolSimpleViewer.allocateSimpleViewer(this, adapter);
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


	/*
	 * download web content from fname
	 */
	private String getContent(String fname) {
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
		return contents.toString();
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
				qbesti.put(qname, seqid);
				lastseqid = qlasti.get(qname);

				if (!qname.equals(lastqname) || !seqid.equals(lastseqid)) {
					String pdblink = (String) tm.getValueAt(selectedRow,
							colcnt - 2);
					pdburl = pdbroot + pdblink;

					String contents = getContent(pdburl);
					ofname = tmpfiledir + seqid + ".pdb";
					try {
						PrintWriter pw = new PrintWriter(new BufferedWriter(
								new FileWriter(ofname)));
						pw.print(contents);
						pw.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					viewer.openStringInline(contents);
					viewer.evalString(strScript);
					seqlb.setText(seqid);
					/*
					 * jp2.setBorder(BorderFactory.createCompoundBorder(
					 * BorderFactory.createCompoundBorder(BorderFactory
					 * .createTitledBorder(seqid), BorderFactory
					 * .createEmptyBorder(5, 5, 5, 5)), border));
					 */
					renderer.setcol(seqid);
					lastseqid = seqid;
					qlasti.put(qname, seqid);
					lasti = selectedRow;
				}
				colKey = seqid;
				lastqname = qname;
			}
		}
	}

	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent event) {
		return event;
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
		ch.setTitle(new TextTitle(dbtype + " SkyBase Models for " + qname, new Font(
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
			r = Double.valueOf(s).doubleValue();
		} else if ((k = s.indexOf("%")) > 0) {
			String subs = s.substring(0, k);
			if (NumberUtils.isNumber(subs))
				r = Double.valueOf(subs).doubleValue() * 0.01;
		} else
			log.info("Warning: " + s + " is not a number!");

		if (pct == true)
			r = r * 100;
		return r;
	}
}
