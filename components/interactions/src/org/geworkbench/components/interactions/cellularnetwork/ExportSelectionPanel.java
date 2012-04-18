package org.geworkbench.components.interactions.cellularnetwork;

/**
 * @author my2248
 * @version $Id$ 
 */

//import java.io.BufferedReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.ProgressBar;

public class ExportSelectionPanel extends JPanel {

	private static final long serialVersionUID = 8300065011440745717L;

	private Log log = LogFactory.getLog(ExportSelectionPanel.class);
	
	private static final String lastDirConf = FilePathnameUtils.getUserSettingDirectoryPath()
	+ "interactions" + FilePathnameUtils.FILE_SEPARATOR + "lastDir.conf";

	private final JDialog parent;

	private JComboBox formatJcb = new JComboBox();
	private JComboBox presentJcb = new JComboBox();
	private JComboBox exportToJcb = new JComboBox();

	private String fileName = null;
	private final String context;
	private final String version;
	private final List<String> interactionTypes;
	private boolean isRestricted;

	private CellularNetworkKnowledgeWidget c;

	public ExportSelectionPanel(CellularNetworkKnowledgeWidget c,
			JDialog parent, String context, String version,
			List<String> interactionTypes, boolean isRestricted) {

		this.context = context;
		this.version = version;
		this.interactionTypes = interactionTypes;
		this.isRestricted = isRestricted;
		setLayout(new GridLayout(6, 3));
		this.c = c;
		this.parent = parent;

		init();

	}

	private void init() {

		JLabel label1 = new JLabel("Export To:   ");

		exportToJcb.addItem(Constants.PROJECT);
		exportToJcb.addItem(Constants.FILE);

		

		JLabel label2 = new JLabel("Search Based On:   ");

		presentJcb.addItem(Constants.GENE_SYMBOL_ONLY);
		presentJcb.addItem(Constants.ENTREZ_ID_ONLY);
		presentJcb.addItem(Constants.GENE_SYMBOL_PREFERRED);
		presentJcb.addItem(Constants.ENTREZ_ID_PREFERRED);
		presentJcb.setRenderer(new ComboboxToolTipRenderer());

		JLabel label3 = new JLabel("File Format:    ");

		formatJcb.addItem(Constants.ADJ_FORMAT);
		formatJcb.addItem(Constants.SIF_FORMAT);	 
		formatJcb.setEnabled(false);
		
		JButton continueButton = new JButton("Continue");
		JButton cancelButton = new JButton("Cancel");

		continueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				continueButtonActionPerformed();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.dispose();
			}
		});
		add(new JLabel("                "));
		add(new JLabel("                "));
		add(new JLabel("                "));
		add(label1);
		add(exportToJcb);
		add(new JLabel("                "));
		add(label2);
		add(presentJcb);
		add(new JLabel("                "));
		add(label3);
		add(formatJcb);
		add(new JLabel("                "));
		add(new JLabel("                "));
		add(new JLabel("                "));
		add(new JLabel("                "));
		add(continueButton);
		add(new JLabel("                "));
		add(cancelButton);
		
	 
		
		exportToJcb.addItemListener(new ItemListener()
		{
			 public void itemStateChanged(ItemEvent itemEvent) {
		     
				 if (exportToJcb.getSelectedItem().toString().equals(Constants.PROJECT))
				 {
					 formatJcb.setSelectedIndex(0);
					 formatJcb.setEnabled(false);
				 }
				 else
				 {
					 formatJcb.setEnabled(true);
				 }
				 
		      
			 }
			
		});
		 
	}

	private void continueButtonActionPerformed() {

		String selectedPresent = presentJcb.getSelectedItem().toString();
		if ((selectedPresent.equals(Constants.GENE_SYMBOL_PREFERRED) || selectedPresent
				.equals(Constants.ENTREZ_ID_PREFERRED)) && isRestricted) {
			int o = JOptionPane
					.showConfirmDialog(
							null,
							"\"Restrict to genes present in microarray set\" would only work for nodes which is represented by\n"
									+ " \"Gene Symbol Only \" or \"Entrez ID Only \", would you like to continue?",
							"Export File", JOptionPane.YES_NO_OPTION);
			if (o != JOptionPane.YES_OPTION) {
				return;
			}

		}

		String selectedExportTo = exportToJcb.getSelectedItem().toString();

		ProgressBar computePb = ProgressBar
				.create(ProgressBar.INDETERMINATE_TYPE);

		if (selectedExportTo.equals(Constants.FILE)) {

			File f = new File("export_" + context + "_" + version + ".sif");
			String selectedFormart = formatJcb.getSelectedItem().toString();
			if (selectedFormart.equals(Constants.ADJ_FORMAT))
				f = new File("export_" + context + "_" + version + ".adj");
			
			JFileChooser jFileChooser1 = new JFileChooser(f);
			String lastDir = null;
			if ((lastDir = getLastDir()) != null) {
				jFileChooser1.setCurrentDirectory(new File(lastDir));
			}			
		 
			jFileChooser1.setSelectedFile(f);			
			String newFileName = null;			
			if (JFileChooser.APPROVE_OPTION == jFileChooser1
					.showSaveDialog(null)) {
				newFileName = jFileChooser1.getSelectedFile().getPath();				
			} else {
				return;
			}


			saveLastDir(jFileChooser1.getSelectedFile().getParent());
			
			if (new File(newFileName).exists()) {
				int o = JOptionPane.showConfirmDialog(null,

				"Replace the file", "Replace the existing file?",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (o != JOptionPane.YES_OPTION) {
					return;
				}

			}

			parent.dispose();

			new ExportWorker(computePb, newFileName).execute();
		} else {

			parent.dispose();
			new ExportWorker(computePb, null).execute();

		}

	}

	private String getToolTipString(String value) {
		if (value.equals(Constants.GENE_SYMBOL_ONLY))
			return "omit nodes without gene symbol";
		else if (value.equals(Constants.ENTREZ_ID_ONLY))
			return "omit nodes without Entrez ID";
		else if (value.equals(Constants.GENE_SYMBOL_PREFERRED))
			return "if gene symbol not present, use other primary id";
		else if (value.equals(Constants.ENTREZ_ID_PREFERRED))
			return "if Entrez ID not present, use other primary id";
		else
			return "unknown";
	}
	
	private String getLastDir(){
		String dir = null;
		try {
			File file = new File(lastDirConf);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				dir = br.readLine();
				br.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return dir;
	}
	private void saveLastDir(String dir){
		//save as last used dir
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(lastDirConf));
			br.write(dir);
			br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	

	private class ComboboxToolTipRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -1299748207172613887L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			Component comp = (Component) super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);

			if (isSelected && value != null) {
				if (-1 < index) {
					list.setToolTipText((value == null) ? ""
							: getToolTipString(value.toString()));
				}

			}

			return comp;
		}
	}

	private class ExportWorker extends SwingWorker<Void, Void> implements
			Observer {

		ProgressBar pb = null;

		ExportWorker(ProgressBar pb, String newFileName) {
			super();
			this.pb = pb;
			fileName = newFileName;
		}

		@Override
		protected void done() {
			if (this.isCancelled()) {
				log.info("Exporting task is cancel.");

			} else {
				log.info("Exporting task is done.");
				pb.dispose();

			}
		}

		@Override
		protected Void doInBackground() {

			pb.addObserver(this);
			pb.setTitle("Export selected interactome");
			pb.setMessage("Process exporting ...");
			pb.start();
			pb.toFront();

			String selectedFormat = formatJcb.getSelectedItem().toString();
			String selectedPresent = presentJcb.getSelectedItem().toString();
			String selectedExportTo = exportToJcb.getSelectedItem().toString();
			boolean needRestrict = false;

			if (isRestricted
					&& !selectedPresent.equals(Constants.GENE_SYMBOL_PREFERRED)
					&& !selectedPresent.equals(Constants.ENTREZ_ID_PREFERRED))
				needRestrict = true;

			BufferedWriter writer = null;
			File file = null;

			try {

				if (selectedExportTo.equals(Constants.FILE)) {
					file = new File(fileName);
					file.createNewFile();
					if (!file.canWrite()) {
						JOptionPane.showMessageDialog(null,
								"Cannot write to specified file.");
						return null;
					}
					writer = new BufferedWriter(new FileWriter(file));
				}

				try {

					InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
					AdjacencyMatrix matrix = null;

					DSDataSet<?> ds = ProjectPanel.getInstance().getDataSet();
					DSMicroarraySet dataset = (DSMicroarraySet) ds;

					for (String interactionType : interactionTypes) {
						if (isCancelled())
							break;
						List<String> lines = new ArrayList<String>();

						if (selectedFormat
								.equalsIgnoreCase(Constants.SIF_FORMAT))
							lines = interactionsConnection
									.getInteractionsSifFormat(context, version,
											interactionType, selectedPresent);
						else if ((selectedFormat
								.equalsIgnoreCase(Constants.ADJ_FORMAT)))
							lines = interactionsConnection
									.getInteractionsAdjFormat(context, version,
											interactionType, selectedPresent);

						if (selectedExportTo.equals(Constants.FILE)) {
							if (needRestrict) {

								for (String line : lines) {
									if (isCancelled())
										break;
									line = getRestrictedNodes(dataset, line,
											selectedFormat);
									if (line != null) {
										writer.write(line);
										writer.write("\n");
									}

								}

							} else {
								for (String line : lines) {
									if (isCancelled())
										break;
									writer.write(line);
									writer.write("\n");

								}
							}

							writer.flush();
							if (isCancelled())
								file.delete();

						} else {

							matrix = exportToProject(matrix, dataset, lines,
									selectedFormat, selectedPresent,
									needRestrict);

						}

					}

					if (selectedExportTo.equals(Constants.PROJECT)) {
						
						if (matrix == null)
							if (matrix == null)
							      matrix = new AdjacencyMatrix(null, dataset,
							    		  CellularNetworkPreferencePanel.interactionTypeSifMap);
						AdjacencyMatrixDataSet adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(
								matrix, 0.0f, "export_" + context + "_" + version,
								dataset.getLabel(), dataset);

						c.publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
								"Adjacency Matrix Added", null,
								adjacencyMatrixdataSet));
					}

				} catch (Exception ex) {
					throw ex;
				} finally {
					if (writer != null)
						writer.close();
				}

			
			} 
			catch (InputFileFormatException ife) {
				JOptionPane
						.showMessageDialog(
								null,
								"The input lines from CNKB servlet server may be uncorrect.",

								"Alert", JOptionPane.ERROR_MESSAGE);
			 
			 
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.getMessage());
			 
		}
			return null;
		}

		public void update(Observable o, Object arg) {
			cancel(true);
		}

	}

	private String getRestrictedNodes(DSMicroarraySet dataset, String line,
			String format) {
		if (line == null || line.trim().equals(""))
			return null;
		StringBuilder restrictedLine = null;

		DSItemList<DSGeneMarker> markers = dataset.getMarkers();
		StringTokenizer tr = new StringTokenizer(line, "\t");
		String node1 = tr.nextToken();
		if (markers.get(node1) == null)
			return null;
		else
			restrictedLine = new StringBuilder(node1);

		if (format.equals(Constants.SIF_FORMAT) && tr.hasMoreTokens())
			restrictedLine.append("\t" + tr.nextToken());
        boolean hasRestrictedNode = false;
		while (tr.hasMoreTokens()) {
			String node2 = tr.nextToken();
			if (markers.get(node2) == null) {
				if (format.equals(Constants.ADJ_FORMAT))
					tr.nextToken();
				continue;
			}
			restrictedLine.append("\t" + node2);
			if (format.equals(Constants.ADJ_FORMAT))
				restrictedLine.append("\t" + tr.nextToken());
			hasRestrictedNode = true;
		}
         if (!hasRestrictedNode)
        	 return null;
		return restrictedLine.toString();
	}

	private AdjacencyMatrix exportToProject(AdjacencyMatrix matrix,
			DSMicroarraySet dataset, List<String> lines, String format,
			String selectedRepresentedBy, boolean isRestrict) throws InputFileFormatException  {

		String nodePresentBy = null;

		if (selectedRepresentedBy.equals(Constants.GENE_SYMBOL_ONLY))
			nodePresentBy = AdjacencyMatrixDataSet.GENE_NAME;
		else if (selectedRepresentedBy.equals(Constants.ENTREZ_ID_ONLY))
			nodePresentBy = AdjacencyMatrixDataSet.ENTREZ_ID;
		else
			nodePresentBy = AdjacencyMatrixDataSet.OTHER;

		AdjacencyMatrix theMatrix = null;

		try {

			theMatrix = AdjacencyMatrixDataSet.parseAdjacencyMatrix(matrix,
					lines, dataset,
					CellularNetworkPreferencePanel.interactionTypeSifMap,
					formatJcb.getSelectedItem().toString(), nodePresentBy,
					isRestrict);

		} catch (InputFileFormatException ife) {
			 throw new InputFileFormatException(ife.getMessage());
		}

		return theMatrix;
	}

	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent pe) {
		return pe;
	}

}