package org.geworkbench.components.skylineview;

import jalview.bin.Cache;
import jalview.gui.AlignFrame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.SkyLineResultDataSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSimpleViewer;

/**
 * SkyLine result viewer for each homology model
 * 
 * @author mw2518
 * @version $Id$
 */
@AcceptTypes( { SkyLineResultDataSet.class })
public class SkyLineViewEachPanel extends JPanel implements VisualPlugin,
		ActionListener, ListSelectionListener {
	private Log log = LogFactory.getLog(this.getClass());
	private static final long serialVersionUID = 1L;
	private DSProteinStructure proteinData;
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JScrollPane jScrollPane = new JScrollPane();
	private Border border = jScrollPane.getBorder();
	private JScrollPane jScrollPane2 = new JScrollPane();
	private Border border2 = jScrollPane2.getBorder();
	private String title = "";
	private String modelname = "";
	//private String rootdir = "http://luna.bioc.columbia.edu:8081/SkyLineData/output";
	private String rootdir = "http://cagridnode.c2b2.columbia.edu:8080/luna/SkyLineData/output";
	private String resultdir = "";
	private String pname = "";
	private JLabel choosefile = new JLabel();
	private JPanel choose = new JPanel();
	private JComboBox allmodels;// = new JComboBox();
	private Boolean finish = false;
	private int maxhitcols = 20;
	private int lasti = 0;
	private int num_model_files = 5;
	private HashMap<String, Integer> forder = new HashMap<String, Integer>();
	private static String strScript = "wireframe off; spacefill off; cartoons; color structure;";
    final static int zoomMin = 5, zoomMax = 200000, zoomNorm = 100;

	private static String energycols[] = { "Residue #", "Pair Energy",
			"Surface Energy", "Combined Energy" };
	private JmolPanel jmolPanel = new JmolPanel();

	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataset = event.getDataSet();
		if (dataset instanceof SkyLineResultDataSet) {
			SkyLineResultDataSet r = (SkyLineResultDataSet) dataset;
			proteinData = (DSProteinStructure) r.getParentDataSet();
			showResults(proteinData);
		}
	}

	private void showResults(DSProteinStructure proteinData) {
		pname = proteinData.getLabel();
        int index = pname.lastIndexOf('.');
        if (index != -1)
            pname = pname.substring(0, index);
		resultdir = rootdir + "/" + pname + "/";

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkJobFinish(String logfile) {
		String line = null;
		String tmp = null;
		String prev = null;
		try {
			URL url = new URL(logfile);
			URLConnection uc = url.openConnection();
			if (uc.getContentLength() <= 0
					|| ((HttpURLConnection) uc).getResponseCode() == 404) {
				return false;
			}
			BufferedReader log = new BufferedReader(new InputStreamReader(uc
					.getInputStream()));
			while ((tmp = log.readLine()) != null) {
				prev = line;
				line = tmp;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("checkjobfinish not connected error: " + logfile);
			JOptionPane.showMessageDialog(null,
					"Cannot connect to SkyLine webserver",
					"Show Results Error", JOptionPane.ERROR_MESSAGE);
		}
		if (prev != null && prev.endsWith("starts at 1.")) {
			return true;
		}
		return false;
	}

	private void jbInit() throws Exception {
		// check if results are available
		String logfile = resultdir + "ANALYSIS/" + pname + ".log";
		finish = checkJobFinish(logfile);
		if (!finish) {
			mainPanel.removeAll();
			mainPanel.revalidate();
			mainPanel.repaint();
			return;
		}
		
		//keep model files in fixed order
		forder.put("pdb", 0);
		forder.put("ali", 1);
		forder.put("energy", 2);
		forder.put("profile", 3);
		forder.put("slp", 4);
		
		// list of models for selection
		displayModels();

		// choose model file output for selected model
		displayModelFiles();

	}

	private void displayModels() throws Exception {
		URL url = new URL(resultdir + "MODELLER/");
		URLConnection uc = url.openConnection();
		if (!uc.getContentType().startsWith("text/html")) {
			return;
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(uc
				.getInputStream()));
		String line;
		boolean start = false;
		int offset = 0;
		Vector<String> modelfiles = new Vector<String>();
		while ((line = in.readLine()) != null) {
			if (start) {
				if ((offset = line.indexOf("a href=\"")) > -1) {
					String subline = line.substring(offset + 8, line
							.lastIndexOf("/\""));
					modelfiles.addElement(subline.substring(subline
							.lastIndexOf("/") + 1));
				}
			} else if (line.indexOf("Filename") > -1) {
				start = true;
			}
		}

		if (modelfiles.size() == 0) {
			System.err.println("no models found in " + resultdir + "MODELLER/");
		}

		JList list = new JList(modelfiles);

		// initialize modelname with the first model
		// if (modelname == "") {
		modelname = (String) modelfiles.elementAt(0);
		list.setSelectedIndex(0);
		// }

		// list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);

		jScrollPane2.getViewport().add(list, null);
		jScrollPane2
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		title = pname + " Models";
		jScrollPane2.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createTitledBorder(title),
						BorderFactory.createEmptyBorder(5, 5, 5, 5)), border2));

		// jScrollPane2.setPreferredSize(new Dimension(200, 400));
		list.addListSelectionListener(this);
		mainPanel.add(jScrollPane2, BorderLayout.LINE_START);
	}

	// list selection
	public void valueChanged(ListSelectionEvent e) {
		String output = "";

		if (e.getValueIsAdjusting() == false) {
			JList lsm = (JList) e.getSource();

			int selections[] = lsm.getSelectedIndices();
			Object selectedValues[] = lsm.getSelectedValues();
			int n = selections.length;
			for (int i = 0; i < n; i++) {
				if (i == 0) {
					output += "Selections: ";
				}
				output += selections[i] + "/" + selectedValues[i] + " ";
			}
			log.info(output);

			modelname = selectedValues[0].toString();

			try {
				displayModelFiles();
			} catch (Exception ae) {
				ae.printStackTrace();
				log.info("displayModelFiles error");
				JOptionPane.showMessageDialog(null,
						"Cannot connect to SkyLine webserver",
						"Show Results Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void displayModelFiles() throws Exception {
		// String[] amfiles = {".energy", ".profile", ".pdb", "zscore.slp",
		// ".ali"};
		String strdir = resultdir + "MODELLER/" + modelname+"/";

		URL url = new URL(strdir);
		URLConnection uc = url.openConnection();
		if (uc.getContentLength() <= 0
				|| ((HttpURLConnection) uc).getResponseCode() == 404) {
			return;
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(uc
				.getInputStream()));
		String line;
		boolean start = false;
		int offset = 0, pdbi = 0;
		Vector<String> amfiles = new Vector<String>();
		amfiles.setSize(num_model_files);
		while ((line = in.readLine()) != null) {
			if (start) {
				if ((offset = line.indexOf("a href=\"")) > -1) {
					String subline = line.substring(offset + 8, line
							.lastIndexOf("\""));
					String elem = subline
							.substring(subline.lastIndexOf("/") + 1);
					String ftype = elem.substring(elem.lastIndexOf(".")+1);

					if (!elem.endsWith("prosa")) {
						amfiles.setElementAt(elem, forder.get(ftype));
					}
				}
			} else if (line.indexOf("Filename") > -1) {
				start = true;
			}
		}

		if (amfiles.size() == 0) {
			System.err.println("no output found in " + strdir);
		}

		allmodels = new JComboBox(amfiles);
		allmodels.addActionListener(this);

		// initialize title with the pdb file of the model
		if (title.endsWith("Models")) {
			lasti = pdbi;
			title = (String) amfiles.elementAt(lasti);
		} else {
			title = (String) allmodels.getSelectedItem();
		}
		allmodels.setSelectedIndex(lasti);

		choose.removeAll();
		choosefile.setFont(choosefile.getFont().deriveFont(Font.BOLD));
		choosefile.setText("Choose Skyline output for model " + modelname
				+ ": ");

		choose.add(choosefile);
		choose.add(allmodels);

		mainPanel.remove(choose);
		mainPanel.revalidate();
		mainPanel.repaint();
		mainPanel.add(choose, BorderLayout.PAGE_START);

	}

	// combobox
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(allmodels) && allmodels.getItemCount() > 0) {
			// JComboBox cb = (JComboBox)e.getSource();
			// title = (String)cb.getSelectedItem();
			title = (String) allmodels.getSelectedItem();
			lasti = allmodels.getSelectedIndex();
			try {
				displayModelFileContent();
			} catch (Exception ae) {
				ae.printStackTrace();
				log.info("displayModelFileContent error");
				JOptionPane.showMessageDialog(null,
						"Cannot connect to SkyLine webserver",
						"Show Results Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public Component getComponent() {
		return mainPanel;
	}

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

	private XYDataset getData(String fname) {
		XYSeries series = new XYSeries("Energy Profile");
		XYSeries control = new XYSeries("Control");
		try {
			URL url = new URL(fname);
			URLConnection uc = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(uc
					.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line);
					int x = 0, i = 0;
					double v = 0;
					while (st.hasMoreTokens()) {
						if (i == 0)
							x = Integer.valueOf(st.nextToken()).intValue();
						else if (i == 3)
							v = Double.valueOf(st.nextToken()).doubleValue();
						else
							st.nextToken();
						i++;
					}
					series.add(x, v);
					control.add(x, 0);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.info("getData not connected error: " + fname);
			JOptionPane.showMessageDialog(null,
					"Cannot connect to SkyLine webserver",
					"Show Results Error", JOptionPane.ERROR_MESSAGE);
		}
		XYSeriesCollection xysc = new XYSeriesCollection(series);
		xysc.addSeries(control);
		XYDataset data = xysc;
		return data;
	}

	private void displayModelFileContent() throws Exception {
		String fname = new String(resultdir + "MODELLER/" + modelname + "/"
				+ title);
		if (title.endsWith(".profile")) {
			int dot_offset = fname.lastIndexOf(".profile");
			fname = fname.substring(0, dot_offset).concat(".energy");
		}
		log.info("display " + fname);

		// redraw .profile plot using jfree library
		if (title.endsWith(".profile")) {
			XYDataset data = getData(fname);
			JFreeChart ch = ChartFactory
					.createXYLineChart(null, "Residue", "Energy", data,
							PlotOrientation.VERTICAL, false, true, true);
			ChartPanel cp = new ChartPanel(ch, 800, 350, 800, 350, 800, 350,
					true, true, true, true, true, true);
			repaint();
			jScrollPane.getViewport().add(cp, null);
		}
		// display .pdb model in jmol
		else if (title.endsWith(".pdb")) {
			JmolSimpleViewer viewer = jmolPanel.getViewer();
			StringBuffer contents = getContent(fname);
			viewer.openStringInline(contents.toString());
			viewer.evalString(strScript);
			repaint();
			jScrollPane.getViewport().add(jmolPanel, null);
		}
		// display .ali in jtextarea
		else if (title.endsWith(".ali")) {
			/*
			 * StringBuffer contents = getContent(fname); JTextArea textArea =
			 * new JTextArea(contents.toString()); textArea.setFont(new
			 * Font("Courier", Font.PLAIN, 16)); repaint();
			 * jScrollPane.getViewport().add(textArea, null);
			 */
			String color = "CLUSTAL";
			try {
				Cache.initLogger();
			} catch (Exception e) {
				e.printStackTrace();
			}

			String localfile = fname;
			String protocol = "URL";
			/*
			 * String localfile = localroot+title; String protocol = "File";
			 * 
			 * BufferedReader br = new BufferedReader(new InputStreamReader(new
			 * URL(fname).openConnection().getInputStream())); String line;
			 * BufferedWriter out=new BufferedWriter(new FileWriter(localfile));
			 * while((line = br.readLine()) != null) { out.write(line+"\n"); }
			 * out.close(); br.close();
			 */

			String format = new jalview.io.IdentifyFile().Identify(localfile,
					protocol);
			jalview.io.FileLoader fileLoader = new jalview.io.FileLoader();
			AlignFrame af = fileLoader.LoadFileWaitTillLoaded(localfile,
					protocol, format);

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
			af.setPreferredSize(new java.awt.Dimension(400, 350));

			jScrollPane.getViewport().add(af);
		}
		// display .energy, .slp in jtable
		else {
			URL url = new URL(fname);
			BufferedReader br = new BufferedReader(new InputStreamReader(url
					.openConnection().getInputStream()));
			String[] columnNames = new String[maxhitcols];
			String line = null;
			int linecnt = 0, maxhitcols = 20;
			if (title.endsWith(".energy")) {
				maxhitcols = 4;
				columnNames = energycols;
			}
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#") && !line.startsWith("Hide")
						&& !line.startsWith("molecule")
						&& !line.startsWith("END")) {
					linecnt++;
				}
			}
			br.close();
			br = new BufferedReader(new InputStreamReader(url.openConnection()
					.getInputStream()));

			Object[][] data = new Object[linecnt][maxhitcols];
			String coldatasplitter = "\\s+";
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("molecule")) {
					columnNames = line.split(coldatasplitter, maxhitcols);
				} else if (!line.startsWith("Hide") && !line.startsWith("#")
						&& !line.startsWith("END")) {
					if (line.startsWith(" "))
						line = line.replaceFirst(" +", "");
					data[i++] = line.split(coldatasplitter, maxhitcols);
				}
			}
			br.close();

			JTable table = new JTable(new NumberTableModel(data, columnNames) {
				private static final long serialVersionUID = 1L;
			});
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			// table sorter available on jdk1.6 or higher
			// table.setAutoCreateRowSorter(true);
			repaint();
			jScrollPane.getViewport().add(table);
		}
		jScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		jScrollPane.setPreferredSize(new Dimension(800, 400));
		jScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createTitledBorder(title),
						BorderFactory.createEmptyBorder(5, 5, 5, 5)), border));
		mainPanel.revalidate();
		mainPanel.repaint();
		mainPanel.add(jScrollPane, BorderLayout.CENTER);
	}

	public class NumberTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private String[] columnNames;
		private Object[][] data;

		public NumberTableModel(Object[][] d, String[] cn) {
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
			return Double.parseDouble((String) data[row][col]);
		}

		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

	public class PDBTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private String[] columnNames;
		private Object[][] data;

		public PDBTableModel(Object[][] d, String[] cn) {
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
			switch (col) {
			case 1:
			case 6:
			case 8:
			case 9:
			case 10:
			case 12:
			case 14:
				// return Integer.parseInt((String)data[row][col]);
				return Double.parseDouble((String) data[row][col]);
			default:
				return data[row][col];
			}
		}

		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

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
			//double zoom = getZoom();
    		//if (zoom != 100) viewer.evalString("zoom "+zoom);

			getSize(currentSize);
			g.getClipBounds(rectClip);
			viewer.renderScreenImage(g, currentSize, rectClip);
		}

/*
		private double getZoom() {
        	double zoom = zoomNorm;
        	int width = getVisibleRect().width;
        	int height = getVisibleRect().height;
        	if (width != height)
        		zoom = height < width ? 100.0*height/width : 100.0*width/height;
        	if (zoom < zoomMin || zoom > zoomMax) zoom = zoomNorm;
        	return zoom;
        }
*/
	}

}
