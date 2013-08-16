package org.geworkbench.components.annotations;

import java.awt.Container;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.CsvFileFilter;
import org.geworkbench.util.OWFileChooser;
import org.geworkbench.util.ProgressItem;

import com.Ostermiller.util.CSVPrinter;

/**
 * Used by Annotation sub-component for Pathway annotations.
 * $Id$
 */

public class AnnotationTableModel extends AbstractTableModel {
        private static Log log = LogFactory.getLog(AnnotationTableModel.class);
        
        private static final String GENE_FINDER_PREFIX = "http://cgap.nci.nih.gov/Genes/GeneInfo?";

    	private static final long serialVersionUID = -7866682936244754027L;
		public static final int COL_MARKER = 0;
        public static final int COL_GENE = 1;
        public static final int COL_PATHWAY = 2;

		private static final String[] columnNames = {"Marker", "Gene", "Pathway"};

        final private DSGeneMarker[] markerData;
        final private GeneAnnotation[] geneData;
        final private String[] pathwayData;

        final private int size;
        final private AnnotationsPanel2 annotationsPanel;

        public AnnotationTableModel(AnnotationsPanel2 annotationsPanel, final AnnotData annotData) {
            this.annotationsPanel = annotationsPanel;

            size = annotData.pathwayCount;
            markerData = new DSGeneMarker[size];
        	geneData = new GeneAnnotation[size];
            pathwayData = new String[size];
            int row = 0;
            for(int index = 0; index<annotData.geneData.length; index++) {
        		// assume that the index of markerData and geneData matches
        		DSGeneMarker marker = annotData.markerData[index];
            	GeneAnnotation geneAnnotation = annotData.geneData[index];
            	for(String p : geneAnnotation.getPathways()) {
            		markerData[row] = marker;
            		geneData[row] = geneAnnotation;
            		pathwayData[row] = p;
            		row++;
            	}
            }
        }

        public AnnotationTableModel() {
        	annotationsPanel = null;
        	
            this.markerData = new DSGeneMarker[0];
            this.geneData = new GeneAnnotation[0];
            this.pathwayData = new String[0];
            size = 0;
        }
        
        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
        
        @Override
        public int getRowCount() {
            return size;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        private static String wrapInHTML(String s) {
            return "<html><a href=\"__noop\">" + s + "</a></html>";
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_MARKER:
                    return markerData[rowIndex].getLabel();
                case COL_GENE:
                    return wrapInHTML(geneData[rowIndex].getGeneSymbol());
                case COL_PATHWAY:
                    return wrapInHTML(pathwayData[rowIndex]);
            }
            return null;
        }

        public void activateCell(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_MARKER:
                	DSGeneMarker marker = markerData[rowIndex];
                    annotationsPanel.publishMarkerSelectedEvent(new MarkerSelectedEvent(marker));
                    break;
                case COL_GENE:
                    GeneAnnotation gene = geneData[rowIndex];
                    activateGene(gene);
                    break;
                case COL_PATHWAY:
                	String pathway = pathwayData[rowIndex];
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
			        String entrezId = geneData[cx].getEntrezId();
		            String entrezUrl = "http://www.ncbi.nlm.nih.gov/sites/entrez?Db=gene&Cmd=ShowDetailView&TermToSearch="+entrezId;
		            String cgapUrl = GENE_FINDER_PREFIX + "ORG=" + geneData[cx].getOrganismAbbreviation() + "&CID=" + geneData[cx].getClusterId();
                    String GeneCardsUrl = AnnotationsPanel2.GeneCards_PREFIX + geneName;
					String pathwayName = pathwayData[cx];

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
		
	    private void activatePathway(final String pathwayData) {
	    	JPopupMenu popup = new JPopupMenu();

			if (pathwayData != null) {
				JMenuItem viewDiagram = new JMenuItem("View Diagram");
				viewDiagram.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						processPathway(pathwayData);
					}
				});
				JMenuItem viewDiagramExternal = new JMenuItem(
						"View Diagram on BioCarta site");
				viewDiagramExternal.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						String url = "http://www.biocarta.com/pathfiles/"+pathwayData+".asp";
						try {
							BrowserLauncher.openURL(url);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				popup.add(viewDiagram);
				popup.add(viewDiagramExternal);
			}

	        JMenuItem makeSet = new JMenuItem("Add pathway genes to set");
	        makeSet.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent actionEvent) {
	            	String tmpSetLabel = JOptionPane.showInputDialog("Panel Set Label:", pathwayData);
	                // String tmpLabel = JOptionPane.showInputDialog("Set Label:", "");
	                if (tmpSetLabel == null) {
	                    // User hit cancel
	                    return;
	                }
	                if (tmpSetLabel.equals("") || tmpSetLabel == null) {
	                    tmpSetLabel = pathwayData;
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
	                OWFileChooser chooser = new OWFileChooser(pathwayData + ".csv");
	                chooser.setFileFilter(new CsvFileFilter());
	                int returnVal = chooser.showSaveDialog(null);
	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                    ExportTask exportTask = new ExportTask(ProgressItem.INDETERMINATE_TYPE,
	                    		"Retrieving and Exporting "+pathwayData+" to CSV",
	                    		annotationsPanel, chooser.getSelectedFile(), pathwayData);
	                    annotationsPanel.pd.executeTask(exportTask);
	                }
	            }
	        });
	        popup.add(export);

	        popup.show(annotationsPanel.annotationTable, (int) (MouseInfo.getPointerInfo().getLocation().getX() - annotationsPanel.annotationTable.getLocationOnScreen().getX()),
	                (int) (MouseInfo.getPointerInfo().getLocation().getY() - annotationsPanel.annotationTable.getLocationOnScreen().getY()));
	    }
	    
	    // show pathway diagram
		private void processPathway(final String pathway) {
			if (!annotationsPanel.pathwayList.contains(pathway)) {
				annotationsPanel.pathwayList.add(pathway);
				annotationsPanel.pathwayComboBox.addItem(pathway);
			}
			if (annotationsPanel.pathwayComboBox.getSelectedIndex() != annotationsPanel.pathwayList
					.size() - 1) {
				annotationsPanel.pathwayComboBox
						.setSelectedIndex(annotationsPanel.pathwayList.size() - 1);
			}
			annotationsPanel.pathwayComboBox.revalidate();
	
			Container parent = annotationsPanel.pathwayPanel.getParent();
			if (parent instanceof JTabbedPane) {
				((JTabbedPane) parent)
						.setSelectedComponent(annotationsPanel.pathwayPanel);
				JTabbedPane p = (JTabbedPane) parent;
				p.setTitleAt(annotationsPanel.jTabbedPane1
						.indexOfComponent(annotationsPanel.pathwayPanel), pathway);
			}
		}
	     
	     /*
	     *
	     */
	    private void activateGene(final GeneAnnotation gene) {
	        JPopupMenu popup = new JPopupMenu();
	        String value = gene.getGeneSymbol();
	        //Get Entrez id
	        String entrezId = gene.getEntrezId();
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
		        String cgapUrl = GENE_FINDER_PREFIX + "ORG=" + gene.getOrganismAbbreviation() + "&CID=" + gene.getClusterId();
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