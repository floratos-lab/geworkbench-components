package org.geworkbench.components.annotations;

import java.awt.Container;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.CsvFileFilter;
import org.geworkbench.util.OWFileChooser;
import org.geworkbench.util.ProgressItem;
import org.jfree.ui.SortableTableModel;

import com.Ostermiller.util.CSVPrinter;

/**
 * Used by Annotation sub-component for Pathway annotations.
 * $Id$
 */

public class AnnotationTableModel extends SortableTableModel {
        private static Log log = LogFactory.getLog(AnnotationTableModel.class);
        
        private static final String GENE_FINDER_PREFIX = "http://cgap.nci.nih.gov/Genes/GeneInfo?";

		private static final long serialVersionUID = 1L;
		public static final int COL_MARKER = 0;
        public static final int COL_GENE = 1;
        public static final int COL_PATHWAY = 2;

        private DSGeneMarker[] markerData;
        private GeneAnnotation[] geneData;
        private Pathway[] pathwayData;

        private Integer[] indices;
        private int size;
        private AnnotationsPanel2 annotationsPanel = null;

        public AnnotationTableModel(AnnotationsPanel2 annotationsPanel, DSGeneMarker[] markerData, GeneAnnotation[] geneData, Pathway[] pathwayData) {
            this.annotationsPanel = annotationsPanel;
        	this.markerData = markerData;
            this.geneData = geneData;
            this.pathwayData = pathwayData;
            size = pathwayData.length;
            indices = new Integer[size];
            resetIndices();
        }

        public AnnotationTableModel() {
            this.markerData = new DSGeneMarker[0];
            this.geneData = new GeneAnnotation[0];
            this.pathwayData = new PathwayImpl[0];
            size = 0;
            indices = new Integer[0];
        }

        private void resetIndices() {
            for (int i = 0; i < size; i++) {
                indices[i] = i;
            }
        }

        public int getRowCount() {
            return size;
        }

        public int getColumnCount() {
            return 3;
        }

        private String wrapInHTML(String s) {
//            return "<html><u><font color=\"#0000FF\">" + s + "</font></u></html>";
            return "<html><a href=\"__noop\">" + s + "</a></html>";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_MARKER:
                    return markerData[indices[rowIndex]].getLabel();
                case COL_GENE:
                    return wrapInHTML(geneData[indices[rowIndex]].getGeneSymbol());
                case COL_PATHWAY:
                    return wrapInHTML(pathwayData[indices[rowIndex]].getPathwayName());
            }
            return null;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
		public void sortByColumn(final int column, final boolean ascending) {
            resetIndices();
            final Comparable[][] columns = {markerData, geneData, pathwayData};
            Comparator<Integer> comparator = new Comparator<Integer>() {
                public int compare(Integer i, Integer j) {
                    if (ascending) {
                        return columns[column][i].compareTo(columns[column][j]);
                    } else {
                        return columns[column][j].compareTo(columns[column][i]);
                    }
                }
            };
            Arrays.sort(indices, comparator);
            super.sortByColumn(column, ascending);
        }

        public boolean isSortable(int i) {
            return true;
        }

        public void activateCell(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_MARKER:
                	DSGeneMarker marker = markerData[indices[rowIndex]];
                    annotationsPanel.publishMarkerSelectedEvent(new MarkerSelectedEvent(marker));
                    break;
                case COL_GENE:
                    GeneAnnotation gene = geneData[indices[rowIndex]];
                    activateGene(gene);
                    break;
                case COL_PATHWAY:
                	Pathway pathway = pathwayData[indices[rowIndex]];
                    // Could be the blank "(none)" pathway.
                    if (pathway != null) {
                    	activatePathway(pathway);
                    }
                    break;
            }
        }

		public boolean toCSV(String filename) {
			boolean ret = true;

			String[] annotationsHeader = { "Marker", "Gene", "Entrez GeneId", "Pathway", "Entrez URL", "CGAP URL", "GeneCards URL" };
			File tempAnnot = new File(filename);
			try {
				CSVPrinter csvout = new CSVPrinter(new BufferedOutputStream(
						new FileOutputStream(tempAnnot)));

				for (int i = 0; i < annotationsHeader.length; i++) {
					csvout.print(annotationsHeader[i]);
				}
				csvout.println();

				for (int cx = 0; cx < this.size; cx++) {
					String markerName = markerData[cx].getLabel();
					String geneName = geneData[cx].getGeneSymbol();
			        String entrezId = GeneAnnotationImpl.getEntrezId(geneData[cx].getGene());
		            String entrezUrl = "http://www.ncbi.nlm.nih.gov/sites/entrez?Db=gene&Cmd=ShowDetailView&TermToSearch="+entrezId;
		            String cgapUrl = GENE_FINDER_PREFIX + "ORG=" + geneData[cx].getOrganismAbbreviation() + "&CID=" + geneData[cx].getGene().getClusterId();
                    String GeneCardsUrl = AnnotationsPanel2.GeneCards_PREFIX + geneName;
					String pathwayName = pathwayData[cx].getPathwayName();

					csvout.print(markerName);
					csvout.print(geneName);
					csvout.print(entrezId);
					csvout.print(pathwayName);
					csvout.print(entrezUrl);
					csvout.print(cgapUrl);
					csvout.print(GeneCardsUrl);
					csvout.println();
				}

				csvout.flush();
				csvout.close();
			} catch (Exception e) {
				e.printStackTrace();
				ret = false;
			}

			return ret;
		}
		
	    private void activatePathway(final Pathway pathwayData) {
	    	JPopupMenu popup = new JPopupMenu();

	        JMenuItem viewDiagram = new JMenuItem("View Diagram");
	        viewDiagram.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent actionEvent) {
	            	processPathway(pathwayData);
	            }
	        });
	        if (pathwayData.getPathwayDiagram()!=null)
	        	popup.add(viewDiagram);

	        JMenuItem makeSet = new JMenuItem("Add pathway genes to set");
	        makeSet.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent actionEvent) {
	            	String tmpSetLabel = JOptionPane.showInputDialog("Panel Set Label:", pathwayData.getPathwayName());
	                // String tmpLabel = JOptionPane.showInputDialog("Set Label:", "");
	                if (tmpSetLabel == null) {
	                    // User hit cancel
	                    return;
	                }
	                if (tmpSetLabel.equals("") || tmpSetLabel == null) {
	                    tmpSetLabel = pathwayData.getPathwayName();
	                }
	                AddTask addTask = new AddTask(ProgressItem.INDETERMINATE_TYPE,
	                		"Retrieving and Adding "+tmpSetLabel+" genes to set",
	                		annotationsPanel, tmpSetLabel, pathwayData);
	                annotationsPanel.pd.executeTask(addTask);
	            }
	        });
	        popup.add(makeSet);

	        JMenuItem export = new JMenuItem("Export genes to CSV");
	        export.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent actionEvent) {
	                OWFileChooser chooser = new OWFileChooser(pathwayData.getPathwayName() + ".csv");
	                chooser.setFileFilter(new CsvFileFilter());
	                int returnVal = chooser.showSaveDialog(null);
	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                    ExportTask exportTask = new ExportTask(ProgressItem.INDETERMINATE_TYPE,
	                    		"Retrieving and Exporting "+pathwayData.getPathwayName()+" to CSV",
	                    		annotationsPanel, chooser.getSelectedFile(), pathwayData);
	                    annotationsPanel.pd.executeTask(exportTask);
	                }
	            }
	        });
	        popup.add(export);

	        popup.show(annotationsPanel.annotationTable, (int) (MouseInfo.getPointerInfo().getLocation().getX() - annotationsPanel.annotationTable.getLocationOnScreen().getX()),
	                (int) (MouseInfo.getPointerInfo().getLocation().getY() - annotationsPanel.annotationTable.getLocationOnScreen().getY()));
	    }
	    
	     private void processPathway(final org.geworkbench.components.annotations.Pathway pathway){
	    	 //FIXME to do this in background is very problematic
	    	 SwingUtilities.invokeLater(new Runnable(){
	         		public void run(){
	                 	String pathwayName = pathway.getPathwayName();
	                 	String pathwayDiagram = pathway.getPathwayDiagram();
	                     addPathwayName(pathwayName, pathwayDiagram);

	                     Container parent = annotationsPanel.pathwayPanel.getParent();
	                     if (parent instanceof JTabbedPane)
	                     {    ((JTabbedPane) parent).setSelectedComponent(annotationsPanel.pathwayPanel);
	                        JTabbedPane p =  (JTabbedPane) parent;
	                        p.setTitleAt(annotationsPanel.jTabbedPane1.indexOfComponent(annotationsPanel.pathwayPanel), pathway.getPathwayName());
	                     }
	         		}
	         	});
	    }
	     
	     private void addPathwayName(String pathwayName, String pathwayDiagram){

	     	if (annotationsPanel.svgStringList.containsKey(pathwayName))
	     	{
	     		annotationsPanel.pathwayComboBox.removeItem(pathwayName);
	     		annotationsPanel.pathwayList.remove(pathwayName);
	     	}
	     	annotationsPanel.svgStringList.put(pathwayName, pathwayDiagram);
	     	annotationsPanel.pathwayList.add(pathwayName);
	     	annotationsPanel.pathwayComboBox.addItem(pathwayName);
	     	if (annotationsPanel.pathwayComboBox.getSelectedIndex() != annotationsPanel.pathwayList.size()-1)
	     		annotationsPanel.pathwayComboBox.setSelectedIndex(annotationsPanel.pathwayList.size()-1);
	     	annotationsPanel.pathwayComboBox.revalidate();
	     }
	     
	     /*
	     *
	     */
	    private void activateGene(final GeneAnnotation gene) {
	        JPopupMenu popup = new JPopupMenu();
	        String value = gene.getGeneSymbol();
	        //Get Entrez id
	        String entrezId = GeneAnnotationImpl.getEntrezId(gene.getGene());
	        if (!entrezId.equals("")){	//if we got an ID
	            String entrezUrl = "http://www.ncbi.nlm.nih.gov/sites/entrez?Db=gene&Cmd=ShowDetailView&TermToSearch="+entrezId;
	            JMenuItem entrezJMenuItem = new JMenuItem("Go to Entrez for " + value);
	            class MyEntrezActionListener implements ActionListener{
	            	String value="";
	            	public MyEntrezActionListener(String value){
	            		super();
	            		this.value = value;
	            	}
	                public void actionPerformed(ActionEvent actionEvent) {
	                    try {
	                        String address = value;
	                        log.debug("Opening " + address);
	                        BrowserLauncher.openURL(address);
	                    }
	                    catch (IOException ioe) {
	                        ioe.printStackTrace();
	                    }
	                }
	            }
	            entrezJMenuItem.addActionListener( new MyEntrezActionListener(entrezUrl) );
	            popup.add(entrezJMenuItem);
	        }
	        //CGAP section
	        if (!gene.getOrganismAbbreviation().equals("")){
		        String cgapUrl = GENE_FINDER_PREFIX + "ORG=" + gene.getOrganismAbbreviation() + "&CID=" + gene.getGene().getClusterId();
		        JMenuItem cgapJMenuItem = new JMenuItem("Go to CGAP for " + value);
		        class MyCGAPActionListener implements ActionListener{
		        	String value="";
		        	public MyCGAPActionListener(String value){
		        		super();
		        		this.value = value;
		        	}
		            public void actionPerformed(ActionEvent actionEvent) {
		                try {
		                    String address = value;
		                    log.debug("Opening " + address);
		                    BrowserLauncher.openURL(address);
		                }
		                catch (IOException ioe) {
		                    ioe.printStackTrace();
		                }
		            }
		        }
		        cgapJMenuItem.addActionListener( new MyCGAPActionListener(cgapUrl) );
		        popup.add(cgapJMenuItem);
	        }
	        //GeneCard section
	        JMenuItem jMenuItem = new JMenuItem("Go to GeneCards for " + value);
	        class MyActionListener implements ActionListener{
	        	String value="";
	        	public MyActionListener(String value){
	        		super();
	        		this.value = value;
	        	}
	            public void actionPerformed(ActionEvent actionEvent) {
	                try {
	                    String address = AnnotationsPanel2.GeneCards_PREFIX + value;
	                    log.debug("Opening " + address);
	                    BrowserLauncher.openURL(address);
	                }
	                catch (IOException ioe) {
	                    ioe.printStackTrace();
	                }
	            }
	        }
	        jMenuItem.addActionListener( new MyActionListener(value) );
	        popup.add(jMenuItem);

	        popup.show(annotationsPanel.annotationTable, (int) (MouseInfo.getPointerInfo().getLocation().getX() - annotationsPanel.annotationTable.getLocationOnScreen().getX()),
	                (int) (MouseInfo.getPointerInfo().getLocation().getY() - annotationsPanel.annotationTable.getLocationOnScreen().getY()));
	    }
    }