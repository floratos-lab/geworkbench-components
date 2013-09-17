package org.geworkbench.components.skylineview;

import jalview.bin.Cache;
import jalview.gui.AlignFrame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
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

/**
 * SkyLine result viewer for all homology models
 * 
 * @author mw2518
 * @version $Id$
 */
@AcceptTypes( { SkyLineResultDataSet.class })
public class SkyLineViewAllPanel implements VisualPlugin, ActionListener {
	private Log log = LogFactory.getLog(this.getClass());
	private DSProteinStructure proteinData;
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JScrollPane jScrollPane = new JScrollPane();
	private Border border = jScrollPane.getBorder();
	private String title = "";
	//private String rootdir = "http://luna.bioc.columbia.edu:8081/SkyLineData/output";
	private String rootdir = "http://cagridnode.c2b2.columbia.edu:8080/luna/SkyLineData/output";
	private String resultdir = "";
	private String pname = "";
	private JComboBox allmodels;// = new JComboBox();
	private boolean finish = false;
	private JPanel choose = new JPanel();
	private JLabel choosefile = new JLabel();
	private int maxhitcols = 16;

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
			log.info("checkJobFinish error: " + logfile);
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
		// display leverage file
		title = pname + ".leverage";

		// check if results are available
		String logfile = resultdir + "ANALYSIS/" + pname + ".log";
		finish = checkJobFinish(logfile);
		if (!finish) {
			mainPanel.removeAll();
			mainPanel.revalidate();
			mainPanel.repaint();
			return;
		}
		displayLeverage("ANALYSIS");

		URL url = new URL(resultdir + "ANALYSIS/");
		URLConnection uc = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(uc
				.getInputStream()));
		String line;
		boolean start = false;
		int offset = 0, i = 0, j = 0;
		Vector<String> amfiles = new Vector<String>();
		while ((line = in.readLine()) != null) {
			if (start) {
				if ((offset = line.indexOf("a href=\"")) > -1) {
					String subline = line.substring(offset + 8, line
							.lastIndexOf("\""));
					amfiles.addElement(subline.substring(subline
							.lastIndexOf("/") + 1));
					if (subline.indexOf(".leverage") > -1)
						j = i;
					i++;
				}
			} else if (line.indexOf("Filename") > -1) {
				start = true;
			}
		}
		in.close();

		// for (Enumeration e = amfiles.elements(); e.hasMoreElements();) {
		// log.info(e.nextElement()); }

		allmodels = new JComboBox(amfiles);
		allmodels.setSelectedIndex(j);
		allmodels.addActionListener(this);

		choose.removeAll();
		choosefile.setFont(choosefile.getFont().deriveFont(Font.BOLD));
		choosefile.setText("Choose Skyline output for all models: ");

		choose.add(choosefile);
		choose.add(allmodels);

		mainPanel.remove(choose);
		mainPanel.revalidate();
		mainPanel.repaint();
		mainPanel.add(choose, BorderLayout.PAGE_START);
	}

	public Component getComponent() {
		return mainPanel;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(allmodels) && allmodels.getItemCount() > 0) {
			title = (String) allmodels.getSelectedItem();
			try {
				displayLeverage("ANALYSIS");
			} catch (Exception ae) {
				ae.printStackTrace();
				JTextArea textArea = new JTextArea("Cannot Read Results");
				textArea.setFont(new Font("Courier", Font.PLAIN, 16));
				jScrollPane.getViewport().add(textArea, null);
				jScrollPane.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(BorderFactory
								.createTitledBorder(title), BorderFactory
								.createEmptyBorder(5, 5, 5, 5)), border));
				log.info("displayLeverage not connected error");
				JOptionPane.showMessageDialog(null,
						"Cannot connect to SkyLine webserver",
						"Show Results Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void displayLeverage(String subdir) throws Exception {
		// display leverage file
		String leverage = resultdir + subdir + "/" + title;
		URL url = new URL(leverage);

		if (title.endsWith(".profile")) {
			try {
				Runtime.getRuntime().exec("gs -sDEVICE=x11 " + leverage);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else {
			URLConnection uc = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(uc
					.getInputStream()));
			StringBuffer contents = new StringBuffer();
			String line = null;
			boolean hits = false, models = false;
			int linecnt = 0;
			if (title.endsWith(".hits") || title.endsWith(".models")) {
				if (title.endsWith(".hits")) {
					hits = true;
					maxhitcols = 16;
				} else if (title.endsWith(".models")) {
					models = true;
					maxhitcols = 8;
				}

				while ((line = br.readLine()) != null) {
					if (!line.startsWith("#") && !line.startsWith("\t"))
						linecnt++;
				}
				br.close();
				br = new BufferedReader(new InputStreamReader(url
						.openConnection().getInputStream()));
				if (hits)
					linecnt--;
			}

			String[] columnNames = new String[maxhitcols];
			Object[][] data = new Object[linecnt][maxhitcols];
			int i = 0, j = 0;
			String colnamesplitter = " ";
			String coldatasplitter = "\t";
			while ((line = br.readLine()) != null) {
				if (hits == true) {
					if (line.startsWith("#") && i < maxhitcols) {
						String[] tmp = line.split(colnamesplitter, 2);
						columnNames[i] = tmp[1];
					} else if (i > maxhitcols + 1) {
						if (line.startsWith(coldatasplitter)) {
							line += "; ";
							int colid = maxhitcols - 1;
							data[j - 1][colid] = ((String) data[j - 1][colid])
									.concat(line);
						} else {
							line += "\t\t";
							data[j++] = line.split(coldatasplitter, maxhitcols);
						}
					}
					i++;
				} else if (models == true) {
					if (i++ == 0) {
						columnNames = line.split(coldatasplitter, maxhitcols);
					} else {
						data[j++] = line.split(coldatasplitter, maxhitcols);
					}
				} else {
					contents.append(line);
					contents.append("\n");
				}
			}
			br.close();

			if (hits == true) {
				JTable table = new JTable(
						new HitsTableModel(data, columnNames) {
							private static final long serialVersionUID = 1L;
						});
				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);// ALL_COLUMNS);
				// table sorter available on jdk1.6 or higher
				// table.setAutoCreateRowSorter(true);
				jScrollPane.getViewport().add(table);
			} else if (models == true) {
				JTable table = new JTable(new ModelsTableModel(data,
						columnNames) {
					private static final long serialVersionUID = 1L;
				});
				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);// ALL_COLUMNS);
				// table sorter available on jdk1.6 or higher
				// table.setAutoCreateRowSorter(true);
				jScrollPane.getViewport().add(table);
			} else if (title.indexOf(".clust") > -1) {
				String color = "CLUSTAL";
				try {
					Cache.initLogger();
				} catch (Exception e) {
					e.printStackTrace();
				}

				String localfile = leverage;
				String protocol = "URL";
				/*
				 * String localfile = localroot+title; String protocol = "File";
				 * 
				 * br = new BufferedReader(new InputStreamReader(new
				 * URL(leverage).openConnection().getInputStream()));
				 * BufferedWriter out=new BufferedWriter(new
				 * FileWriter(localfile)); while((line = br.readLine()) != null) {
				 * out.write(line+"\n"); } out.close(); br.close();
				 */

				String format = new jalview.io.IdentifyFile().Identify(
						localfile, protocol);
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
			} else {
				JTextArea textArea = new JTextArea(contents.toString());
				textArea.setFont(new Font("Courier", Font.PLAIN, 16));
				// textArea.setLineWrap(true);
				// textArea.setWrapStyleWord(true);
				jScrollPane.getViewport().add(textArea, null);
			}

			jScrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			jScrollPane
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			jScrollPane.setPreferredSize(new Dimension(800, 400));
			jScrollPane.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createCompoundBorder(BorderFactory
							.createTitledBorder(title), BorderFactory
							.createEmptyBorder(5, 5, 5, 5)), border));
			mainPanel.add(jScrollPane, BorderLayout.CENTER);
		}
	}

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
			switch (col) {
			case 0:
			case 5:
			case 6:
			case 7:
			case 9:
			case 10:
			case 11:
				return Integer.parseInt((String) data[row][col]);
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

	public class ModelsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private String[] columnNames;
		private Object[][] data;

		public ModelsTableModel(Object[][] d, String[] cn) {
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
			/*
			 * String n = "NA"; if (((String)data[row][col]).equals(n)) return
			 * "NA"; switch(col) { case 2: case 3: return
			 * Double.parseDouble((String)data[row][col]); default:
			 */
			return data[row][col];
		}

		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}
}
