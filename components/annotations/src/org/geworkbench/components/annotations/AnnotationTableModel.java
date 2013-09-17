package org.geworkbench.components.annotations;

import java.awt.Component;
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
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.CsvFileFilter;
import org.geworkbench.util.OWFileChooser;

import com.Ostermiller.util.CSVPrinter;

/**
 * Used by Annotation sub-component for Pathway annotations.
 * $Id$
 */

public class AnnotationTableModel extends AbstractTableModel {
        private static Log log = LogFactory.getLog(AnnotationTableModel.class);
        
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
            log.debug("empty instance constructed");
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
					GeneAnnotation gene = geneData[cx];

					csvout.print(markerData[cx].getLabel());
					csvout.print(gene.getGeneSymbol());
					csvout.print(gene.getEntrezId());
					csvout.print(pathwayData[cx]);
					csvout.print(getEntrezUrl(gene));
					csvout.print(getCGAPUrl(gene));
					csvout.print(getGeneCardsUrl(gene));
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
						annotationsPanel.showPathwayDiagram(pathwayData);
					}
				});
				JMenuItem viewDiagramExternal = new JMenuItem(
						"View Diagram on BioCarta site");
				viewDiagramExternal.addActionListener(new BrowserLaunchingListener(getPathwayUrl(pathwayData)));
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
					PathwayGeneTask addTask = new PathwayGeneTask(
							"Retrieving and Adding " + tmpSetLabel
									+ " genes to set", annotationsPanel,
							PathwayGeneTask.TaskType.ADD_TO_PROJECT, tmpSetLabel,
							pathwayData);
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
						PathwayGeneTask exportTask = new PathwayGeneTask(
								"Retrieving and Exporting " + pathwayData
										+ " to CSV", annotationsPanel,
								PathwayGeneTask.TaskType.EXPORT, chooser
										.getSelectedFile().getAbsolutePath(),
								pathwayData);
						annotationsPanel.pd.executeTask(exportTask);
	                }
	            }
	        });
	        popup.add(export);

	        Component component = annotationsPanel.getComponent();
	        popup.show(component, (int) (MouseInfo.getPointerInfo().getLocation().getX() - component.getLocationOnScreen().getX()),
	                (int) (MouseInfo.getPointerInfo().getLocation().getY() - component.getLocationOnScreen().getY()));
	    }
	    
	    private void activateGene(final GeneAnnotation gene) {
	        JPopupMenu popup = new JPopupMenu();
	        String value = gene.getGeneSymbol();
	        //Get Entrez id
	        if (!gene.getEntrezId().equals("")){	//if we got an ID
	            JMenuItem entrezJMenuItem = new JMenuItem("Go to Entrez for " + value);
	            entrezJMenuItem.addActionListener( new BrowserLaunchingListener(getEntrezUrl(gene)) );
	            popup.add(entrezJMenuItem);
	        }
	        //CGAP section
	        if (!gene.getOrganismAbbreviation().equals("")){
		        JMenuItem cgapJMenuItem = new JMenuItem("Go to CGAP for " + value);
		        cgapJMenuItem.addActionListener( new BrowserLaunchingListener(getCGAPUrl(gene)) );
		        popup.add(cgapJMenuItem);
	        }
	        //GeneCard section
	        JMenuItem jMenuItem = new JMenuItem("Go to GeneCards for " + value);
	        jMenuItem.addActionListener( new BrowserLaunchingListener(getGeneCardsUrl(gene)) );
	        popup.add(jMenuItem);

	        Component component = annotationsPanel.getComponent();
	        popup.show(component, (int) (MouseInfo.getPointerInfo().getLocation().getX() - component.getLocationOnScreen().getX()),
	                (int) (MouseInfo.getPointerInfo().getLocation().getY() - component.getLocationOnScreen().getY()));
	    }
	    
	    private static String getPathwayUrl(final String pathwayName) {
	    	return "http://www.biocarta.com/pathfiles/"+pathwayName+".asp";
	    }
	    
	    private static String getEntrezUrl(final GeneAnnotation g) {
	    	return "http://www.ncbi.nlm.nih.gov/sites/entrez?Db=gene&Cmd=ShowDetailView&TermToSearch="+g.getEntrezId();
	    }
	    
	    private static String getCGAPUrl(final GeneAnnotation g) {
	    	return "http://cgap.nci.nih.gov/Genes/GeneInfo?"+"ORG=" + g.getOrganismAbbreviation() + "&CID=" + g.getClusterId();
	    }
	    
	    private static String getGeneCardsUrl(final GeneAnnotation g) {
	    	return "http://www.genecards.org/cgi-bin/carddisp.pl?gene="+g.getGeneSymbol();
	    }
	    
	    private static class BrowserLaunchingListener implements ActionListener {

	    	final private String url;
	    	
	    	BrowserLaunchingListener(String url) {
	    		this.url = url;
	    	}
	    	
			@Override
			public void actionPerformed(ActionEvent e) {
                try {
                    BrowserLauncher.openURL(url);
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
			}
	    }
}