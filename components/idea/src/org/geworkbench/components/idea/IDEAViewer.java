package org.geworkbench.components.idea;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge;
import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge.InteractionType;
import org.geworkbench.bison.datastructure.bioobjects.IdeaProbeGene;
import org.geworkbench.bison.datastructure.bioobjects.IdeaResult;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

/**
 * IDEAViewer of IDEA analysis component
 * 
 * @author zm2165
 * @version $Id$
 * 
 */
@AcceptTypes({ IdeaResult.class })
public class IDEAViewer extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = -4415752683103679560L;

	private static class IdeaEdgeTableModel extends AbstractTableModel {
		private static final int COLUMN_COUNT = 8;

		private static final long serialVersionUID = -6551819301207179797L;

		private static final String[] columnNames = new String[] { "Probe1",
				"Gene1", "Probe2", "Gene2", "MI", "DeltaMI", "NormDelta",
				"Z-score" };

		List<IdeaEdge> list = null;

		public IdeaEdgeTableModel() {
			list = new ArrayList<IdeaEdge>();
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			IdeaEdge edge = list.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return edge.getProbeId1();
			case 1:
				return edge.getMarker1().getGeneName();
			case 2:
				return edge.getProbeId2();
			case 3:
				return edge.getMarker2().getGeneName();
			case 4:
				return edge.getMI();
			case 5:
				return edge.getDeltaCorr();
			case 6:
				return edge.getNormCorr();
			case 7:
				return edge.getzDeltaCorr();
			}
			return 0;
		}

		void setValues(List<IdeaEdge> list) {
			this.list = list;
		}
	}

	private class IdeaGeneTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 4140458497876037744L;

		private static final int COLUMN_COUNT = 13;

		List<IdeaProbeGene> list = null;
		private final String[] columnNames = new String[] { "Probe", "Gene",
				"ChrBand", "Conn", "Nes", "Loc", "LoCHits", "LoCEs", "LoCNes",
				"Goc", "GoCHits", "GoCEs", "GoCNes" };

		public IdeaGeneTableModel() {
			list = new ArrayList<IdeaProbeGene>();
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (ideaResult == null)
				return null;

			DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) ideaResult
					.getParentDataSet();
			if (maSet == null)
				return null;

			IdeaProbeGene ideaProbeGene = list.get(rowIndex);
			DSGeneMarker m = maSet.getMarkers().get(ideaProbeGene.getProbeId());

			int locHits = 0;
			int gocHits = 0;
			for (IdeaEdge e : ideaProbeGene.getEdges()) {
				if (e.getDeltaCorr() < 0)
					locHits++;
				else if (e.getDeltaCorr() > 0)
					gocHits++;
			}
			double locnes = -Math.log(ideaProbeGene.getCumLoc());
			double gocnes = -Math.log(ideaProbeGene.getCumGoc());

			switch (columnIndex) {
			case 0:
				return ideaProbeGene.getProbeId();
			case 1:
				return m.getGeneName();
			case 2:
				return "chromosomal"; // FIXME placeholder
			case 3:
				return ideaProbeGene.getEdges().size();
			case 4:
				return ideaProbeGene.getNes();
			case 5:
				return ideaProbeGene.getLocs();
			case 6:
				return locHits;
			case 7:
				return ideaProbeGene.getCumLoc();
			case 8:
				return locnes;
			case 9:
				return ideaProbeGene.getGocs();
			case 10:
				return gocHits;
			case 11:
				return ideaProbeGene.getCumGoc();
			case 12:
				return gocnes;
			}
			return 0;
		}

		void setValues(List<IdeaProbeGene> list) {
			this.list = list;
		}
	}

	private class IdeaNodeTableModel extends AbstractTableModel {
	
		private static final long serialVersionUID = 9153220106537748604L;

		private static final int COLUMN_COUNT = 5;

		List<String[]> list = null;
		private final String[] columnNames = new String[] { "Gene1", "Gene2",
				"Conn-type", "Loc", "Goc"};

		public IdeaNodeTableModel() {
			list = new ArrayList<String[]>();
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (ideaResult == null)
				return null;

			DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) ideaResult
					.getParentDataSet();
			if (maSet == null)
				return null;
			
			

			switch (columnIndex) {
			case 0:
				return list.get(rowIndex)[0];
			case 1:
				return list.get(rowIndex)[1];
			case 2:
				return list.get(rowIndex)[2];
			case 3:
				return list.get(rowIndex)[3];
			case 4:
				return list.get(rowIndex)[4];
			}
			
			
			return 0;
		}

		void setValues(List<String[]> list) {
			this.list = list;
		}
	}

	
	
	private IdeaEdgeTableModel locTableModel = new IdeaEdgeTableModel();
	private IdeaEdgeTableModel gocTableModel = new IdeaEdgeTableModel();
	private IdeaNodeTableModel nodeTableModel= new IdeaNodeTableModel();
	private IdeaGeneTableModel significantGeneTableModel = new IdeaGeneTableModel();

	public IDEAViewer() {
		// super(new GridLayout(1, 1));
		JTabbedPane tabbedPane = new JTabbedPane();

		JTable gocTable = new JTable(gocTableModel);
		JTable locTable = new JTable(locTableModel);
		JTable nodeTable = new JTable(nodeTableModel);
		JTable significantGeneTable = new JTable(significantGeneTableModel);

		// significantGeneTable.setPreferredSize(new Dimension(700, 50));
		tabbedPane.addTab("Genes of Significance", new JScrollPane(
				significantGeneTable));
		tabbedPane.addTab("Edges of LOC", new JScrollPane(locTable));
		tabbedPane.addTab("Edges of GOC", new JScrollPane(gocTable));
		tabbedPane.addTab("Module in Nodes", new JScrollPane(nodeTable));

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		JButton saveSigGeneButton = new JButton();
		saveSigGeneButton.setText("save significant genes");
		JButton saveLocButton = new JButton();
		saveLocButton.setText("save Loc egdes");
		JButton saveGocButton = new JButton();
		saveGocButton.setText("save Goc edges");
		JButton saveNodesButton = new JButton("save significant nodes");
		JButton saveNullButton = new JButton();
		saveNullButton.setText("save null distribution data");
		

		bottomPanel.add(saveSigGeneButton);
		bottomPanel.add(saveLocButton);
		bottomPanel.add(saveGocButton);
		bottomPanel.add(saveNodesButton);
		bottomPanel.add(saveNullButton);		
		add(bottomPanel, BorderLayout.SOUTH);

		saveNodesButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showSaveDialog(IDEAViewer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					saveNodeInformationFile(fc.getSelectedFile(),
							ideaResult.getSignificantGeneList());
				}
			}

		});

		saveSigGeneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showSaveDialog(IDEAViewer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) ideaResult
							.getParentDataSet();
					saveSignificantGenesAsFile(fc.getSelectedFile(),
							ideaResult.getSignificantGeneList(),
							maSet.getMarkers());
				}
			}
		});

		saveLocButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showSaveDialog(IDEAViewer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					saveAsFile(fc.getSelectedFile(), ideaResult.getLocList());
				}
			}
		});

		saveGocButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showSaveDialog(IDEAViewer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					saveAsFile(fc.getSelectedFile(), ideaResult.getGocList());
				}
			}
		});

		saveNullButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showSaveDialog(IDEAViewer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File outputFile = fc.getSelectedFile();
					// This is where to save the file.
					File inputFile = new File(System.getProperty("user.dir")
							+ "\\data\\null.dat");
					try {
						InputStream in = new FileInputStream(inputFile);
						OutputStream out = new FileOutputStream(outputFile);
						int c;
						while ((c = in.read()) != -1)
							out.write((byte) c);

						in.close();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void saveNodeInformationFile(File file,
			List<IdeaProbeGene> probes) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(file);

			out.print( "Gene1\tGene2\tconn_type\tLoc\tGoc" );
			for (IdeaProbeGene p : probes) {// present significant node with its
				// edges
				if ((p.getCumLoc() < ideaResult.getPvalue()) || (p.getCumGoc() < ideaResult.getPvalue())) {
					// nodeStr+=p.getProbeId()+"\n";
					for (IdeaEdge e : p.getEdges()) {
						String isLoc = "";
						String isGoc = "";
						String ppi = "";
						if (e.isLoc())
							isLoc = "X";
						if (e.isGoc())
							isGoc = "X";
						if (e.getPpi() == InteractionType.PROTEIN_PROTEIN)
							ppi = "ppi";
						else if (e.getPpi() == InteractionType.PROTEIN_DNA)
							ppi = "pdi";

						out.print( "\n" + e.getProbeId1() + "\t"
								+ e.getProbeId2() + "\t" + ppi + "\t" + isLoc
								+ "\t" + isGoc );
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}

	}

	private void saveAsFile(File file, List<IdeaEdge> list) {
		String edgeStr = "";

		edgeStr += "Probe1\tGene1\tProbe2\tGene2\tMI\tDeltaMI\tNormDelta\tZ-score";
		int output1Row = 0;
		for (IdeaEdge e : list) {
			edgeStr += "\n" + e.getProbeId1() + "\t"
					+ e.getMarker1().getGeneName() + "\t" + e.getProbeId2()
					+ "\t" + e.getMarker2().getGeneName() + "\t" + e.getMI()
					+ "\t" + e.getDeltaCorr() + "\t" + e.getNormCorr() + "\t"
					+ e.getzDeltaCorr();

			output1Row++;
		}

		PrintWriter out = null;
		try {
			out = new PrintWriter(file);
			out.println(edgeStr);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			out.close();
		}
	}

	private void saveSignificantGenesAsFile(File file,
			List<IdeaProbeGene> significantGeneList,
			DSItemList<DSGeneMarker> markers) {
		String nodeStr = "";
		nodeStr += "Probe\tGene\tChrBand\tConn\tNes\tLoc\tLoCHits\tLoCEs\tLoCNes\tGoc\tGoCHits\tGoCEs\tGoCNes";
		int row = 0;
		for (IdeaProbeGene p : significantGeneList) {// present significant
														// nodes
			int locHits = 0;
			int gocHits = 0;
			for (IdeaEdge e : p.getEdges()) {
				if (e.getDeltaCorr() < 0)
					locHits++;
				else if (e.getDeltaCorr() > 0)
					gocHits++;
			}
			double locnes = -Math.log(p.getCumLoc());
			double gocnes = -Math.log(p.getCumGoc());

			DSGeneMarker m = markers.get(p.getProbeId());

			nodeStr += "\n" + p.getProbeId() + "\t" + m.getGeneName() + "\t";
			nodeStr += "chromosomal" + "\t" + p.getEdges().size() + "\t"
					+ p.getNes() + "\t" + p.getLocs() + "\t" + locHits + "\t"
					+ p.getCumLoc() + "\t" + locnes + "\t" + p.getGocs() + "\t"
					+ gocHits + "\t" + p.getCumGoc() + "\t" + gocnes;

			row++;
		}

		PrintWriter out = null;
		try {
			out = new PrintWriter(file);
			out.println(nodeStr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
	}

	private IdeaResult ideaResult = null;

	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataSet = event.getDataSet();
		if (dataSet instanceof IdeaResult) {

			ideaResult = (IdeaResult) dataSet;
			gocTableModel.setValues(ideaResult.getGocList());
			gocTableModel.fireTableDataChanged();
			locTableModel.setValues(ideaResult.getLocList());
			locTableModel.fireTableDataChanged();
			nodeTableModel.setValues(ideaResult.getNodeList());
			nodeTableModel.fireTableDataChanged();
			significantGeneTableModel.setValues(ideaResult
					.getSignificantGeneList());
			significantGeneTableModel.fireTableDataChanged();
		}
	}

	public Component getComponent() {
		return this;
	}

}
