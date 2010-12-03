package org.geworkbench.components.idea;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge;
import org.geworkbench.bison.datastructure.bioobjects.IdeaProbeGene;
import org.geworkbench.bison.datastructure.bioobjects.IdeaResult;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
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

		private static final String[] columnNames = new String[] {
			"Probe1", "Gene1", "Probe2", "Gene2", "MI", "DeltaMI", "NormDelta", "Z-score"
		};
		
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
			switch(columnIndex) {
			case 0: return edge.getProbeId1();
			case 1: return edge.getMarker1().getGeneName();
			case 2: return edge.getProbeId2();
			case 3: return edge.getMarker2().getGeneName();
			case 4: return edge.getMI();
			case 5: return edge.getDeltaCorr();
			case 6: return edge.getNormCorr();
			case 7: return edge.getzDeltaCorr();
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
		private final String[] columnNames = new String[] {
				"Probe","Gene","ChrBand","Conn","Nes","Loc","LoCHits","LoCEs","LoCNes","Goc","GoCHits","GoCEs","GoCNes"
		};
		
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
			if(ideaResult==null) return null;
			
			DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>)ideaResult.getParentDataSet(); 
			if(maSet==null) return null;
			
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

			switch(columnIndex) {
			case 0: return ideaProbeGene.getProbeId();
			case 1: return m.getGeneName();
			case 2: return "chromosomal"; // FIXME placeholder
			case 3: return ideaProbeGene.getEdges().size();
			case 4: return ideaProbeGene.getNes();
			case 5: return ideaProbeGene.getLocs();
			case 6: return locHits;
			case 7: return ideaProbeGene.getCumLoc();
			case 8: return locnes;
			case 9: return ideaProbeGene.getGocs();
			case 10: return gocHits;
			case 11: return ideaProbeGene.getCumGoc();
			case 12: return gocnes;
			}
			return 0;
		}
	
		void setValues(List<IdeaProbeGene> list) {
			this.list = list;
		}
	}	
	
	private IdeaEdgeTableModel locTableModel = new IdeaEdgeTableModel();
	private IdeaEdgeTableModel gocTableModel = new IdeaEdgeTableModel();
	private IdeaGeneTableModel significantGeneTableModel = new IdeaGeneTableModel();

	public IDEAViewer() {
		//super(new GridLayout(1, 1));
		JTabbedPane tabbedPane = new JTabbedPane();
						
		JTable gocTable = new JTable(gocTableModel);
		JTable locTable = new JTable(locTableModel);
		JTable significantGeneTable = new JTable(significantGeneTableModel);
		
		//significantGeneTable.setPreferredSize(new Dimension(700, 50));		
		tabbedPane.addTab("Genes of Significance", new JScrollPane(significantGeneTable));		
		tabbedPane.addTab("Edges of LOC", new JScrollPane(locTable));
		tabbedPane.addTab("Edges of GOC", new JScrollPane(gocTable));
		
		setLayout(new BorderLayout());		
		add(tabbedPane, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		JButton saveSigGeneButton = new JButton();
		saveSigGeneButton.setText("save significant genes");
		saveSigGeneButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JFileChooser fc = new JFileChooser();
	            int returnVal = fc.showSaveDialog(IDEAViewer.this);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fc.getSelectedFile();
	                //This is where to save the file.
	                String str = "";
	        		str += "Probe\tGene\tChrBand\tConn\tNes\tLoc\tLoCHits\tLoCEs\tLoCNes\tGoc\tGoCHits\tGoCEs\tGoCNes";
	        		int row = 0;
	        		for (IdeaProbeGene p : ideaResult.getSignificantGeneList()) {// present significant nodes
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
	        			DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>)ideaResult.getParentDataSet(); 
	        			
	        			DSGeneMarker m = maSet.getMarkers().get(p.getProbeId());
	        			str += "\n" + p.getProbeId() + "\t" + m.getGeneName()
	        					+ "\t";
	        			str += "chromosomal" + "\t" + p.getEdges().size() + "\t"
	        					+ p.getNes() + "\t" + p.getLocs() + "\t" + locHits
	        					+ "\t" + p.getCumLoc() + "\t" + locnes + "\t"
	        					+ p.getGocs() + "\t" + gocHits + "\t" + p.getCumGoc()
	        					+ "\t" + gocnes;

	        			row++;
	        		}

	        		PrintWriter out = null;
	        		try {
	        			out = new PrintWriter(file);
	        			out.println(str);
	        		} catch (FileNotFoundException e) {
	        			e.printStackTrace();
	        		} finally {
	        			out.close();
	        		}	                
	               
	            } else {
	                //log.append("Save command cancelled by user." + newline);
	            }
	            //log.setCaretPosition(log.getDocument().getLength());
	        
				
			}
			
		});		
		
		JButton saveLocButton = new JButton();
		saveLocButton.setText("save Loc egdes");
		saveLocButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JFileChooser fc = new JFileChooser();
	            int returnVal = fc.showSaveDialog(IDEAViewer.this);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fc.getSelectedFile();
	                //This is where to save the file.
	                String str = "";
	                str += "Probe1\tGene1\tProbe2\tGene2\tMI\tDeltaMI\tNormDelta\tZ-score";
	        		int output1Row = 0;
	        		for (IdeaEdge e : ideaResult.getLocList()) {
	        			str += "\n" + e.getProbeId1() + "\t"
	        					+ e.getMarker1().getGeneName() + "\t" + e.getProbeId2()
	        					+ "\t" + e.getMarker2().getGeneName() + "\t"
	        					+ e.getMI() + "\t" + e.getDeltaCorr() + "\t"
	        					+ e.getNormCorr() + "\t" + e.getzDeltaCorr();

	        			output1Row++;
	        		}
	                PrintWriter out = null;
		        		try {
		        			out = new PrintWriter(file);
		        			out.println(str);
		        		} catch (FileNotFoundException e) {
		        			e.printStackTrace();
		        		} finally {
		        			out.close();
		        		}        			
	        		}			
			}
			
		});
		JButton saveGocButton = new JButton();
		saveGocButton.setText("save Goc edges");
		saveGocButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JFileChooser fc = new JFileChooser();
	            int returnVal = fc.showSaveDialog(IDEAViewer.this);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fc.getSelectedFile();
	                //This is where to save the file.
	                String str = "";
	                str += "Probe1\tGene1\tProbe2\tGene2\tMI\tDeltaMI\tNormDelta\tZ-score";
	        		int output1Row = 0;
	        		for (IdeaEdge e : ideaResult.getGocList()) {
	        			str += "\n" + e.getProbeId1() + "\t"
	        					+ e.getMarker1().getGeneName() + "\t" + e.getProbeId2()
	        					+ "\t" + e.getMarker2().getGeneName() + "\t"
	        					+ e.getMI() + "\t" + e.getDeltaCorr() + "\t"
	        					+ e.getNormCorr() + "\t" + e.getzDeltaCorr();

	        			output1Row++;
	        		}

	                PrintWriter out = null;
		        		try {
		        			out = new PrintWriter(file);
		        			out.println(str);
		        		} catch (FileNotFoundException e) {
		        			e.printStackTrace();
		        		} finally {
		        			out.close();
		        		}        			
	        		}			
			}
			
		});
		bottomPanel.add(saveSigGeneButton);
		bottomPanel.add(saveLocButton);
		bottomPanel.add(saveGocButton);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	private IdeaResult ideaResult = null;
	
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataSet = event.getDataSet();
		if (dataSet instanceof IdeaResult) {
			
			ideaResult = (IdeaResult)dataSet;
			gocTableModel.setValues(ideaResult.getGocList());
			locTableModel.setValues(ideaResult.getLocList());
			significantGeneTableModel.setValues(ideaResult.getSignificantGeneList());
		}
	}

	public Component getComponent() {
		// TODO Auto-generated method stub
		return this;
	}

	/*
	 * Add to Set
	 */
	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}
}
