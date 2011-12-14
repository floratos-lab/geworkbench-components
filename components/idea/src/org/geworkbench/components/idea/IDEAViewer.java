package org.geworkbench.components.idea;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
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
import org.geworkbench.bison.datastructure.bioobjects.IdeaGLoc;
import org.geworkbench.bison.datastructure.bioobjects.IdeaModule;
import org.geworkbench.bison.datastructure.bioobjects.IdeaNode;
import org.geworkbench.bison.datastructure.bioobjects.IdeaResultDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
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
@AcceptTypes({ IdeaResultDataSet.class })
public class IDEAViewer extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = -4415752683103679560L;

	private static class IdeaEdgeTableModel extends AbstractTableModel {
		private static final int COLUMN_COUNT = 7;

		private static final long serialVersionUID = -6551819301207179797L;

		private static final String[] columnNames = new String[] { "Probe1",
				"Gene1", "Probe2", "Gene2", "MI", "DeltaMI", "Z-score" };

		List<IdeaGLoc> list = null;

		public IdeaEdgeTableModel() {
			list = new ArrayList<IdeaGLoc>();
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
			if(list==null) return 0;
			return list.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			IdeaGLoc e = list.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return e.getProbe1();
			case 1:
				return e.getGene1();
			case 2:
				return e.getProbe2();
			case 3:
				return e.getGene2();
			case 4:
				return Double.toString((((int) (e.getMi()*1000))/1000.0));
			case 5:
				return Double.toString((((int) (e.getDeltaMi()*1000))/1000.0));
				case 6:
				return Double.toString((((int) (e.getzScore()*1000))/1000.0));
			}
			return 0;
		}

		void setValues(List<IdeaGLoc> list) {
			this.list = list;
		}
	}

	private class IdeaGeneTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 4140458497876037744L;

		private static final int COLUMN_COUNT = 13;

		List<IdeaNode> list = null;
		private final String[] columnNames = new String[] { "Probe", "Gene",
				"ChrBand", "Conn", "Nes", "Loc", "LoCHits", "LoCEs", "LoCNes",
				"Goc", "GoCHits", "GoCEs", "GoCNes" };

		public IdeaGeneTableModel() {
			list = new ArrayList<IdeaNode>();
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
			if(list==null) return 0;
			return list.size();
		}
		
		@Override
		public Class<?> getColumnClass(int column) {
			if ((column == 3) || (column == 5) || (column == 6)
					|| (column == 9) || (column == 10)) {
				return Integer.class;
			}
			if ((column == 4) || (column == 7) || (column == 8)
					|| (column == 11) || (column == 12)) {
				return Double.class;
			}
			return String.class;
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (ideaResult == null)
				return null;			

			IdeaNode node = list.get(rowIndex);
			
			switch (columnIndex) {
			case 0:
				return node.getProbe();
			case 1:
				return node.getGene();
			case 2:
				return node.getChrBand(); 
			case 3:
				return node.getConn();
			case 4:
				return Math.abs(node.getNes());
			case 5:
				return node.getLoc();
			case 6:
				return node.getLoCHits();
			case 7:
				return node.getLoCEs();
			case 8:
				return Math.abs(node.getLoCNes());
			case 9:
				return node.getGoc();
			case 10:
				return node.getGoCHits();
			case 11:
				return node.getGoCEs();
			case 12:
				return Math.abs(node.getGoCNes());
			}
			return 0;
		}

		void setValues(List<IdeaNode> list) {
			this.list = list;
		}
	}

	private class IdeaModuleTableModel extends AbstractTableModel {
	
		private static final long serialVersionUID = 9153220106537748604L;

		private static final int COLUMN_COUNT = 4;

		List<IdeaModule> list = null;
		private final String[] columnNames = new String[] { "Gene1", "Gene2",
				"Conn-type", "Loc/Goc"};

		public IdeaModuleTableModel() {
			list = new ArrayList<IdeaModule>();
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
			if(list==null) return 0;
			return list.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (ideaResult == null)
				return null;
			IdeaModule module = list.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return module.getGene1();
			case 1:
				return module.getGene2();
			case 2:
				return module.getConnType();
			case 3:
				return module.getGLoc();			
			}
			
			
			return 0;
		}

		void setValues(List<IdeaModule> list) {
			this.list = list;
		}
	}

	
	
	private IdeaEdgeTableModel locTableModel = new IdeaEdgeTableModel();
	private IdeaEdgeTableModel gocTableModel = new IdeaEdgeTableModel();
	private IdeaModuleTableModel moduleTableModel= new IdeaModuleTableModel();
	private IdeaGeneTableModel significantGeneTableModel = new IdeaGeneTableModel();

	public IDEAViewer() {
		// super(new GridLayout(1, 1));
		JTabbedPane tabbedPane = new JTabbedPane();

		JTable gocTable = new JTable(gocTableModel);
		gocTable.setAutoCreateRowSorter(true);
		JTable locTable = new JTable(locTableModel);
		locTable.setAutoCreateRowSorter(true);
		JTable nodeTable = new JTable(moduleTableModel);
		nodeTable.setAutoCreateRowSorter(true);
		JTable significantGeneTable = new JTable(significantGeneTableModel);
		significantGeneTable.setAutoCreateRowSorter(true);
		
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

		bottomPanel.add(saveSigGeneButton);
		bottomPanel.add(saveLocButton);
		bottomPanel.add(saveGocButton);
		bottomPanel.add(saveNodesButton);		
		add(bottomPanel, BorderLayout.SOUTH);

		saveNodesButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showSaveDialog(IDEAViewer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					saveModuleInformation(fc.getSelectedFile(),
							ideaResult.getModuleList());
				}
			}

		});

		saveSigGeneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showSaveDialog(IDEAViewer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					DSMicroarraySet maSet = (DSMicroarraySet) ideaResult
							.getParentDataSet();
					saveNodeAsFile(fc.getSelectedFile(),
							ideaResult.getNodeList(),
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
	}

	private void saveModuleInformation(File file,
			List<IdeaModule> modules) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(file);

			out.print( "Gene1\tGene2\tconn_type\tLoc/Goc" );
			for (IdeaModule e : modules) {// present significant node with its
				// edges			

				out.print( "\n" + e.getGene1() + "\t"+ e.getGene2() 
						+ "\t" + e.getConnType() + "\t" + e.getGLoc());
					
			}			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}

	}

	private void saveAsFile(File file, List<IdeaGLoc> list) {
		String edgeStr = "";

		edgeStr += "Probe1\tGene1\tProbe2\tGene2\tMI\tDeltaMI\tZ-score";
		for (IdeaGLoc e : list) {
			edgeStr += "\n" + e.getProbe1() + "\t"
					+ e.getGene1() + "\t" + e.getProbe2()
					+ "\t" + e.getGene2() + "\t" + e.getMi()
					+ "\t" + e.getDeltaMi() + "\t" + e.getzScore();
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

	private void saveNodeAsFile(File file,
			List<IdeaNode> nodeList,
			DSItemList<DSGeneMarker> markers) {
		String nodeStr = "";
		nodeStr += "Probe\tGene\tChrBand\tConn\tNes\tLoc\tLoCHits\tLoCEs\tLoCNes\tGoc\tGoCHits\tGoCEs\tGoCNes";
		for (IdeaNode p : nodeList) {// present significant nodes			

			nodeStr += "\n" + p.getProbe() + "\t" + p.getGene() + "\t";
			nodeStr += p.getChrBand() + "\t" + p.getConn() + "\t"
					+ p.getNes() + "\t" + p.getLoc() + "\t" + p.getLoCHits() + "\t"
					+ p.getLoCEs() + "\t" + p.getLoCEs() + "\t" + p.getGoc() + "\t"
					+ p.getGoCHits() + "\t" + p.getGoCEs() + "\t" + p.getGoCNes();
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

	private IdeaResultDataSet ideaResult = null;

	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataSet = event.getDataSet();
		if (dataSet instanceof IdeaResultDataSet) {

			ideaResult = (IdeaResultDataSet) dataSet;
			gocTableModel.setValues(ideaResult.getGocList());
			gocTableModel.fireTableDataChanged();
			locTableModel.setValues(ideaResult.getLocList());
			locTableModel.fireTableDataChanged();
			moduleTableModel.setValues(ideaResult.getModuleList());
			moduleTableModel.fireTableDataChanged();
			significantGeneTableModel.setValues(ideaResult
					.getNodeList());
			significantGeneTableModel.fireTableDataChanged();
		}
	}

	public Component getComponent() {
		return this;
	}

}
