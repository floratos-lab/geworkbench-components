package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Agent;
import gov.nih.nci.cabio.domain.DiseaseOntology;
import gov.nih.nci.cabio.domain.Evidence;
import gov.nih.nci.cabio.domain.Gene;
import gov.nih.nci.cabio.domain.GeneDiseaseAssociation;
import gov.nih.nci.cabio.domain.GeneFunctionAssociation;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.batik.bridge.DefaultExternalResourceSecurity;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.LinkActivationEvent;
import org.apache.batik.swing.svg.LinkActivationListener;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.util.gui.JErrorPane;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.ExampleFilter;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AnnotationsEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.annotation.Pathway;
import org.jfree.ui.SortableTable;
import org.jfree.ui.SortableTableModel;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
 
 
/**
 * <p>
 * Title: Bioworks
 * </p>
 * <p>
 * Description: Modular Application Framework for Gene Expession, Sequence and
 * Genotype Analysis
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003 -2004
 * </p>
 * <p>
 * Company: Columbia University
 * </p>
 * 
 * Component responsible for displaying Gene Annotation obtained from caBIO
 * Displays data in a Tabular format with 2 columns. The first column contains
 * The Gene Discription and the second column contains a list of known Pathways
 * that this gene's product participates in.
 * 
 * @author manjunath at genomecenter dot columbia dot edu
 * @version $Id: AnnotationsPanel.java,v 1.34 2009-06-11 21:07:21 chiangy Exp $
 * 
 * 
 */
@AcceptTypes({DSMicroarraySet.class})
public class AnnotationsPanel implements VisualPlugin, Observer{
    static Log log = LogFactory.getLog(AnnotationsPanel.class);
    public static final int COL_MARKER = 0;
    public static final int COL_GENE = 1;
    public static final int COL_DISEASE = 2;
    public static final int COL_ROLE = 3;
    public static final int COL_SENTENCE = 4;
    public static final int COL_PUBMED = 5;

    private boolean stopAlgorithm = false;

	private String wrapInHTML(String s) {
		return "<html><a href=\"__noop\">" + s + "</a></html>";
	}

	private String unwrapFromHTML(String s) {
		return s.substring("<html><a href=\"__noop\">".length(), s.length()
				- "</a></html>".length());
	}

	private class TableModel extends SortableTableModel {

        private MarkerData[] markerData;
        private GeneData[] geneData;
        private PathwayData[] pathwayData;
        private DiseaseData[] diseaseData;
        private RoleData[] roleData;
        private SentenceData[] sentenceData;
        private PubmedData[] pubmedData;
        
        private Integer[] indices;
        private int size;

        public TableModel(MarkerData[] markerData, GeneData[] geneData, DiseaseData[] diseaseData, RoleData[] roleData, SentenceData[] sentenceData, PubmedData[] pubmedData) {
            this.markerData = markerData;
            this.geneData = geneData;
            this.diseaseData = diseaseData;
            this.roleData = roleData;
            this.sentenceData = sentenceData;
            this.pubmedData = pubmedData;
            size = diseaseData.length;
            indices = new Integer[size];
            resetIndices();
        }

        public TableModel() {
            this.markerData = new MarkerData[0];
            this.geneData = new GeneData[0];
            this.diseaseData = new DiseaseData[0];
            this.roleData = new RoleData[0];
            this.sentenceData = new SentenceData[0];
            this.pubmedData = new PubmedData[0];
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
            return 6;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_MARKER:
                    return markerData[indices[rowIndex]].name;
                case COL_GENE:
                    return wrapInHTML(geneData[indices[rowIndex]].name);
                case COL_DISEASE:
                    return diseaseData[indices[rowIndex]].name;
                case COL_ROLE:
                    return roleData[indices[rowIndex]].role;
                case COL_SENTENCE:
                    return sentenceData[indices[rowIndex]].sentence;
                case COL_PUBMED:
                    return wrapInHTML(pubmedData[indices[rowIndex]].id);
            }
            return null;
        }

        public void sortByColumn(final int column, final boolean ascending) {
            resetIndices();
            final Comparable[][] columns = {markerData, geneData, diseaseData, roleData, sentenceData, pubmedData};
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
                    MarkerData marker = markerData[indices[rowIndex]];
                    activateMarker(marker);
                    break;
                case COL_GENE:
                    GeneData gene = geneData[indices[rowIndex]];
//                    if (gene.getCGAPGeneURLs().size() > 0) {
//                        activateGene(gene);
//                    }
                    break;
                case COL_DISEASE:
                	DiseaseData disease = diseaseData[indices[rowIndex]];
                    // Could be the blank "(none)" pathway.
                    if (disease.diseaseOntology != null) {
                        //activatePathway(pathway);
                    }
                    break;
            }
        }
    }

    private static class MarkerData implements Comparable {

        public String name;
        public DSGeneMarker marker;

        public MarkerData(DSGeneMarker marker) {
            this.name = marker.getLabel();
            this.marker = marker;
        }

        public int compareTo(Object o) {
            if (o instanceof MarkerData) {
                return name.compareTo(((MarkerData) o).name);
            }
            return -1;
        }
    }

    private static class GeneData implements Comparable {
        /**
         * Web URL prefix for obtaining Locus Link annotation
         */
        private static final String LOCUS_LINK_PREFIX = "http://www.ncbi.nlm.nih.gov/LocusLink/LocRpt.cgi?l=";
        /**
         * Web URL prefix for obtaining CGAP annotation
         */
        private static final String GENE_FINDER_PREFIX = "http://cgap.nci.nih.gov/Genes/GeneInfo?";
        /**
         * Web URL prefix currently being used
         */
        public static final String PREFIX_USED = GENE_FINDER_PREFIX;
        private static final String HUMAN_ABBREV = "Hs";
        private static final String MOUSE_ABBREV = "Mm";

        public String name;
        public Gene gene;
        
        public GeneData(String name, Gene gene) {
            this.name = name;
            this.gene = gene;
        }

        public int compareTo(Object o) {
            if (o instanceof GeneData) {
                return name.compareTo(((GeneData) o).name);
            }
            return -1;
        }

    }

    private static class PathwayData implements Comparable {

        public String name;
        public Pathway pathway;

        public PathwayData(String name, Pathway pathway) {
            this.name = name;
            this.pathway = pathway;
        }

        public int compareTo(Object o) {
            if (o instanceof PathwayData) {
                return name.compareTo(((PathwayData) o).name);
            }
            return -1;
        }
    }

    private static class DiseaseData implements Comparable {

        public String name;
        public DiseaseOntology diseaseOntology;

        public DiseaseData(String name, DiseaseOntology diseaseOntology) {
            this.name = name;
            this.diseaseOntology = diseaseOntology;
        }

        public int compareTo(Object o) {
            if (o instanceof DiseaseData) {
                return name.compareTo(((DiseaseData) o).name);
            }
            return -1;
        }
    }
    private static class RoleData implements Comparable {

        public String role;

        public RoleData(String role) {
            this.role = role;
        }

        public int compareTo(Object o) {
            if (o instanceof RoleData) {
                return role.compareTo(((RoleData) o).role);
            }
            return -1;
        }
    }
    private static class SentenceData implements Comparable {

        public String sentence;

        public SentenceData(String sentence) {
            this.sentence = sentence;
        }

        public int compareTo(Object o) {
            if (o instanceof SentenceData) {
                return sentence.compareTo(((SentenceData) o).sentence);
            }
            return -1;
        }
    }

    private static class PubmedData implements Comparable {

        public String id;

        public PubmedData(String id) {
            this.id = id;
        }

        public int compareTo(Object o) {
            if (o instanceof PubmedData) {
                return id.compareTo(((PubmedData) o).id);
            }
            return -1;
        }
    }

    private static class AgentData implements Comparable {

        public String name;
        public Agent agent;

        public AgentData(String name, Agent agent) {
            this.name = name;
            this.agent = agent;
        }

        public int compareTo(Object o) {
            if (o instanceof AgentData) {
                return name.compareTo(((AgentData) o).name);
            }
            return -1;
        }
    }

    /**
     * Default Constructor
     */
    public AnnotationsPanel() {
        try {
            jbInit();
        }

        catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    public AnnotationsPanel getAnnotationsPanel()
    {
    	return this;
    }

    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {    	
    	 
    	mainPanel.setLayout( new GridLayout());
        mainPanel.add(jTabbedPane1);
        jTabbedPane1.add("Annotations", annotationsPanel);
        jTabbedPane1.add("Pathway", pathwayPanel);        
        
        jbInitPathways();       
        
        annotationsPanel.setLayout(borderLayout1);
        showPanels.setHorizontalAlignment(SwingConstants.CENTER);
        showPanels.setText("Retrieve Disease Information");
        showPanels.setToolTipText("Retrieve gene and disease information for markers in activated panels");
        showPanels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanels_actionPerformed(e);
            }

        });
        // buttonPanel.setLayout(borderLayout2);
        clearButton.setForeground(Color.black);
        clearButton.setToolTipText("");
        clearButton.setFocusPainted(true);
        clearButton.setText("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearButton_actionPerformed(e);
            }
        });
        annotationsPanel.add(jScrollPane1, BorderLayout.CENTER);
        annotationsPanel.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(showPanels);
        buttonPanel.add(clearButton);       
        model = new TableModel();
        table = new SortableTable(model);
        table.getColumnModel().getColumn(0).setHeaderValue("Marker");
        table.getColumnModel().getColumn(1).setHeaderValue("Gene");
        table.getColumnModel().getColumn(2).setHeaderValue("Disease");
        table.getColumnModel().getColumn(3).setHeaderValue("Role");
        table.getColumnModel().getColumn(4).setHeaderValue("Sentence");
        table.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if ((column >= 0) && (row >= 0)) {
                    model.activateCell(row, column);
                }
                if ((column == COL_GENE)) {
                    String value = (String) table.getValueAt(row, column);
                    value=unwrapFromHTML(value);
                    String address = "http://www.genecards.org/cgi-bin/carddisp.pl?gene="+value;
                    try {
						BrowserLauncher.openURL(address);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }
                if ((column == COL_PUBMED)) {
                    String value = (String) table.getValueAt(row, column);
                    value=unwrapFromHTML(value);
                    String address = "http://www.ncbi.nlm.nih.gov/pubmed/"+value;
                    try {
						BrowserLauncher.openURL(address);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }
            }
        });
        table.addMouseMotionListener(new MouseMotionAdapter() {
            private boolean isHand = false;

            public void mouseMoved(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if ((column >= 0) && (row >= 0)) {
                    if ((column == COL_GENE) || (column == COL_PUBMED)) {
                        if (!isHand) {
                            isHand = true;
                            table.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }
                    } else {
                        if (isHand) {
                            isHand = false;
                            table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                }
            }
        });
        jScrollPane1.getViewport().add(table, null);
        
        annotationTableList = new HashMap<Integer, TableModel>();
    }

    /**
     * Interface <code>VisualPlugin</code> method that returns a
     * <code>Component</code> which is the visual representation of
     * the this plugin.
     *
     * @return <code>Component</code> visual representation of
     *         <code>AnnotationsPanel</code>
     */
    public Component getComponent() {
        return mainPanel;
    }

    /**
     * Performs caBIO queries and constructs HTML display of the results
     */
    private void showAnnotation() {
        if (criteria == null) {
            try {
//                criteria = new GeneSearchCriteriaImpl();
            } catch (Exception e) {
                log.error("Exception: could not create caBIO search criteria in Annotation Panel. Exception is: ");
                e.printStackTrace();
                return;
            }
        }

        pathways = new Pathway[0];
        try {
            Runnable query = new Runnable() {
            	
            	//FIXME: error thrown from caBio should be handled by giving user a popup dialog. and disable the progress bar.
            	public void run() {
            		ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                    pb.addObserver(getAnnotationsPanel());
                    pb.setMessage("Connecting to server...");
                    ArrayList<MarkerData> markerData = new ArrayList<MarkerData>();
                    ArrayList<GeneData> geneData = new ArrayList<GeneData>();
                    ArrayList<DiseaseData> diseaseData = new ArrayList<DiseaseData>();
                    ArrayList<RoleData> roleData = new ArrayList<RoleData>();
                    ArrayList<SentenceData> sentenceData = new ArrayList<SentenceData>();
                    ArrayList<PubmedData> pubmedData = new ArrayList<PubmedData>();
                    if (selectedMarkerInfo != null) {
                        pb.setTitle("Querying caBIO..");
                        pb.start();           

                		ApplicationService appService = null;
                		try {
                			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                			appService = ApplicationServiceProvider.getApplicationService();
                		} catch (Exception e) {
                			// TODO Auto-generated catch block
                			e.printStackTrace();
                		}

                        int index = 0;
                        //TODO: to save network communication time, we should query only once by submitting the list.
                        for (int i = 0; i < selectedMarkerInfo.size(); i++) {
                             
                           if (stopAlgorithm == true)
                           {
                               stopAlgorithm(pb);
                                  return;
                            }
                            
                            String geneName = selectedMarkerInfo.get(i).getGeneName();
                            String probeLabel = selectedMarkerInfo.get(i).getLabel();
                            
                    		String geneSymbol = geneName;
                    		//int uniGeneId = marker.getUnigene().getUnigeneId();
                    		Gene gene = new Gene();
                    		gene.setSymbol(geneSymbol);

                    		List<Object> results2 = null;
                    		try {
                    			results2 = appService.search(GeneFunctionAssociation.class, gene);
                    		} catch (ApplicationException e) {
                    			// TODO Auto-generated catch block
                    			e.printStackTrace();
                    		}

                    		System.out.println("\nDisease associated with Gene: " + geneSymbol);
                    		for (Object gfa : results2) {
                    			if (gfa instanceof GeneDiseaseAssociation) {
                    				GeneDiseaseAssociation gda = (GeneDiseaseAssociation) gfa;
                    				markerData.add(new MarkerData(selectedMarkerInfo.get(i)));
                    				geneData.add(new GeneData(gda.getGene().getSymbol(),gda.getGene()));
                    				System.out.println("  Disease: " + gda.getDiseaseOntology().getName());
                    				diseaseData.add(new DiseaseData(gda.getDiseaseOntology().getName(),gda.getDiseaseOntology()));
                    				System.out.println("    Role: " + gda.getRole());
                    				Collection<Evidence> ce = gda.getEvidenceCollection();
                    				GeneAnnotationImpl.EvidenceStruct e = GeneAnnotationImpl.getSentencePubmedid(ce);
                    				System.out.println("    Sentence: "+e.getSentence());
                    				System.out.println("    PubmedId:"+e.getPubmedId());
                    				roleData.add(new RoleData(gda.getRole()));
                    				sentenceData.add(new SentenceData(e.getSentence()));
                    				pubmedData.add(new PubmedData(e.getPubmedId()));
                    			}
                    		}
                        } 
                        
                        pb.stop();
                        pb.dispose();                       
                        
                    }
                    MarkerData[] markers = markerData.toArray(new MarkerData[0]);
                    GeneData[] genes = geneData.toArray(new GeneData[0]);
                    DiseaseData[] diseases = diseaseData.toArray(new DiseaseData[0]);
                    RoleData[] roles = roleData.toArray(new RoleData[0]);
                    SentenceData[] sentences = sentenceData.toArray(new SentenceData[0]);
                    PubmedData[] pubmeds = pubmedData.toArray(new PubmedData[0]);
                    model = new TableModel(markers, genes, diseases, roles, sentences, pubmeds);
                    //annotationTableList.put(new Integer(maSet.hashCode()),  new TableModel(markers, genes, diseases, pubmeds));
                    table.setSortableModel(model);
                    table.getColumnModel().getColumn(0).setHeaderValue("Marker");
                    table.getColumnModel().getColumn(1).setHeaderValue("Gene");
                    table.getColumnModel().getColumn(2).setHeaderValue("Disease");
                    table.getColumnModel().getColumn(3).setHeaderValue("Role");
                    table.getColumnModel().getColumn(4).setHeaderValue("Sentence");
                    table.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            table.getTableHeader().repaint();
                        }
                    });
                }
            };
            Thread t = new Thread(query);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearButton_actionPerformed(ActionEvent e) {
        table.setSortableModel(new TableModel());
        table.getColumnModel().getColumn(0).setHeaderValue("Marker");
        table.getColumnModel().getColumn(1).setHeaderValue("Gene");
        table.getColumnModel().getColumn(2).setHeaderValue("Disease");
        table.getColumnModel().getColumn(3).setHeaderValue("Role");
        table.getColumnModel().getColumn(4).setHeaderValue("Sentence");
        table.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
        table.getTableHeader().revalidate();
        annotationTableList.put(new Integer(maSet.hashCode()),  new TableModel());
    }

    private void showPanels_actionPerformed(ActionEvent e) {
        if (selectedMarkerInfo == null || selectedMarkerInfo.size() == 0) {
            JOptionPane.showMessageDialog(annotationsPanel, "Please activate marker panels to retrieve annotations.");
        }
        showAnnotation();
        
    }

    private void activateMarker(MarkerData markerData) {
        publishMarkerSelectedEvent(new MarkerSelectedEvent(markerData.marker));
    }

    private void activateGene(final GeneData gene) {
        JPopupMenu popup = new JPopupMenu();
//        JPopupMenu CGAPPopup = new JPopupMenu("CGAP");
        java.util.List<GeneAnnotationImpl.CGAPUrl> cgapGeneURLs = null;//gene.annotation.getCGAPGeneURLs();
        for (final GeneAnnotationImpl.CGAPUrl cgapUrl : cgapGeneURLs) {
            JMenuItem jMenuItem = new JMenuItem("CGAP > " + cgapUrl.getOrganismName());
            jMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    try {
                        log.debug("Opening " + cgapUrl.getUrl().toString());
                        BrowserLauncher.openURL(cgapUrl.getUrl().toString());
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            });

            popup.add(jMenuItem);
        }

//        popup.add(CGAPPopup);

        popup.show(table, (int) (MouseInfo.getPointerInfo().getLocation().getX() - table.getLocationOnScreen().getX()),
                (int) (MouseInfo.getPointerInfo().getLocation().getY() - table.getLocationOnScreen().getY()));
    }

    private void activatePathway(final PathwayData pathwayData) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem viewDiagram = new JMenuItem("View Diagram");
        viewDiagram.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                publishAnnotationsEvent(new AnnotationsEvent("Pathway Selected", pathwayData.pathway));
                receive(new AnnotationsEvent("Pathway Selected", pathwayData.pathway));
            
            }
        });
        popup.add(viewDiagram);

        JMenuItem makeSet = new JMenuItem("Add pathway genes to set");
        makeSet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {

                Thread thread = new Thread(new Runnable() {
                	
                	//private GeneAnnotation[] genesInPathway = null;
                	public void run() {

                        String tmpSetLabel = JOptionPane.showInputDialog("Panel Set Label:", pathwayData.pathway.getPathwayName());
                        // String tmpLabel = JOptionPane.showInputDialog("Set Label:", "");
                        if (tmpSetLabel == null) {
                            // User hit cancel
                            return;
                        }
                        if (tmpSetLabel.equals("") || tmpSetLabel == null) {
                            tmpSetLabel = pathwayData.pathway.getPathwayName();
                        }

                        ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                        pb.addObserver(getAnnotationsPanel());
                        pb.setTitle("Genes For Pathway");
                        pb.setMessage("Retrieving from server...");
                        pb.start();

                        GeneAnnotation[] genesInPathway = criteria.getGenesInPathway(pathwayData.pathway);
                         
                        pb.stop();
                        pb.dispose();

                        if ( genesInPathway == null )
                        	return;
                        
                        DSPanel<DSGeneMarker> selectedMarkers = new CSPanel<DSGeneMarker>(tmpSetLabel, tmpSetLabel);
                        for (int i = 0; i < genesInPathway.length; i++) {
                            GeneAnnotation geneAnnotation = genesInPathway[i];
                            log.info(geneAnnotation.getGeneSymbol() + " : " + geneAnnotation.getGeneName());
                            DSItemList markers = maSet.getMarkers();
                            for (Iterator iterator = markers.iterator(); iterator.hasNext();) {
                                DSGeneMarker marker = (DSGeneMarker) iterator.next();
                                if (marker.getShortName().equalsIgnoreCase(geneAnnotation.getGeneSymbol())) {
                                    log.debug("Found " + geneAnnotation.getGeneSymbol() + " in set.");
                                    selectedMarkers.add(marker);
                                   // break; Disabled it because there may be mutiple markers for the same gene.
                                }
                            }
                        }

                        selectedMarkers.setActive(true);
                        publishSubpanelChangedEvent(new SubpanelChangedEvent(DSGeneMarker.class, selectedMarkers, SubpanelChangedEvent.SET_CONTENTS));

                    }
                });
                thread.start();

            }
        });
        popup.add(makeSet);

        JMenuItem export = new JMenuItem("Export genes to CSV");
        export.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {

                Thread thread = new Thread(new Runnable() {                	
                	 
                    public void run() {
                        JFileChooser chooser = new JFileChooser(pathwayData.pathway.getPathwayName() + ".csv");
                        ExampleFilter filter = new ExampleFilter();
                        filter.addExtension("csv");
                        filter.setDescription("CSV Files");
                        chooser.setFileFilter(filter);
                        int returnVal = chooser.showSaveDialog(null);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            try {
                                ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                                pb.addObserver(getAnnotationsPanel());
                                pb.setTitle("Genes For Pathway");
                                pb.setMessage("Retrieving from server...");
                                pb.start();
                                
                                GeneAnnotation[] genesInPathway = criteria.getGenesInPathway(pathwayData.pathway);
                           
                                pb.stop();
                                pb.dispose();
                                
                                if ( genesInPathway == null )
                                	return;
                                
                                saveGenesInPathway(chooser.getSelectedFile(), genesInPathway);
                            } catch (IOException ex) {
                                log.error(ex);
                            }
                        }
                    }
                });
                thread.start();
            }

        });
        popup.add(export);

        popup.show(table, (int) (MouseInfo.getPointerInfo().getLocation().getX() - table.getLocationOnScreen().getX()),
                (int) (MouseInfo.getPointerInfo().getLocation().getY() - table.getLocationOnScreen().getY()));
    }

    private void saveGenesInPathway(File selectedFile, GeneAnnotation[] genesInPathway) throws IOException {

	String filename = selectedFile.getAbsolutePath();
    	if(!filename.endsWith("csv")){
    		filename += ".csv";
    	}
    	FileWriter writer = new FileWriter(filename); 
        for (int i = 0; i < genesInPathway.length; i++) {
            GeneAnnotation geneAnnotation = genesInPathway[i];
            writer.write(geneAnnotation.getGeneSymbol() + ", " + geneAnnotation.getGeneName() + "\n");
        }
        writer.close();
    }

    @Publish
    public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(org.geworkbench.events.SubpanelChangedEvent event) {
        return event;
    }

    @Publish
    public AnnotationsEvent publishAnnotationsEvent(AnnotationsEvent ae) {
        return ae;
    }

    @Publish
    public MarkerSelectedEvent publishMarkerSelectedEvent(MarkerSelectedEvent event) {
        return event;
    }
    
    
    
    private JPanel mainPanel = new JPanel();
    private JTabbedPane jTabbedPane1 = new JTabbedPane();  
    
    

    /**
     * The Visual Component on which the annotation results are shown
     */
    private JPanel annotationsPanel = new JPanel();

    /**
     * Visual Widget
     */
    private JScrollPane jScrollPane1 = new JScrollPane();

    /**
     * Visual Widget
     */
    private BorderLayout borderLayout1 = new BorderLayout();

    /**
     * Visual Widget
     */
    private SortableTable table;
    private TableModel model;
    
    private HashMap<Integer, TableModel> annotationTableList; 

    /**
     * Visual Widget
     */
    private JPanel buttonPanel = new JPanel();

    /**
     * Visual Widget
     */
    private JButton showPanels = new JButton();
    private DSItemList<DSGeneMarker> selectedMarkerInfo = null;
    private DSGeneMarker singleMarker = null;
    private GeneSearchCriteria criteria = null;
    private boolean showMarkers = true;
    private Pathway[] pathways = new Pathway[0];

    private DSMicroarraySet maSet = null;
    JButton clearButton = new JButton();

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    @Subscribe
    public void receive(GeneSelectorEvent e, Object source) {
        if (maSet != null && e.getPanel() != null) {
            DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(maSet);
            maView.setMarkerPanel(e.getPanel());
            maView.useMarkerPanel(true);
            if (maView.getMarkerPanel().activeSubset().size() == 0) {
                selectedMarkerInfo = new CSItemList<DSGeneMarker>();
            } else {
                selectedMarkerInfo = maView.getUniqueMarkers();
            }
        }
    }

    /**
     * receiveProjectSelection
     *
     * @param e ProjectEvent
     */
    @Subscribe
    public void receive(ProjectEvent e, Object source) {
        DSDataSet data = e.getDataSet();
        int hashcode = 0;
        if (data != null && data instanceof DSMicroarraySet) {
            maSet = (DSMicroarraySet) data;            
            
        }
        
        if ( maSet != null )
        	hashcode = maSet.hashCode();
        	
       
        
        if (oldHashCode == 0)  
        {
        	//pathwayComboBox.setSelectedIndex(0);     		 
           // jTabbedPane1.setTitleAt(1,"Pathway");
        	oldHashCode = hashcode;
        	return;
        }
        
        pathwayComboItemSelectedMap.put(new Integer(oldHashCode), new Integer(pathwayComboBox.getSelectedIndex()));
        svgStringListMap.put(new Integer(oldHashCode), (HashMap<String, String>)svgStringList.clone());
        pathwayListMap.put(new Integer(oldHashCode), (ArrayList<String>)pathwayList.clone());
        tabPanelSelectedMap.put(new Integer(oldHashCode), jTabbedPane1.getSelectedIndex());
        
        if (annotationTableList.containsKey(new Integer(hashcode)))
        {
        	model = annotationTableList.get(new Integer(hashcode));
        	table.setSortableModel(model);
        	table.getColumnModel().getColumn(0).setHeaderValue("Marker");
            table.getColumnModel().getColumn(1).setHeaderValue("Gene");
            table.getColumnModel().getColumn(2).setHeaderValue("Disease");
            table.getColumnModel().getColumn(3).setHeaderValue("Role");
            table.getColumnModel().getColumn(4).setHeaderValue("Sentence");
            table.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
            table.getTableHeader().revalidate();
             
        }           
        else
        {
        	table.setSortableModel(new TableModel());
        	table.getColumnModel().getColumn(0).setHeaderValue("Marker");
            table.getColumnModel().getColumn(1).setHeaderValue("Gene");
            table.getColumnModel().getColumn(2).setHeaderValue("Disease");
            table.getColumnModel().getColumn(3).setHeaderValue("Role");
            table.getColumnModel().getColumn(4).setHeaderValue("Sentence");
            table.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
            table.getTableHeader().revalidate();
            jTabbedPane1.setSelectedIndex(0);
             
        }
         
         
    	  if (svgStringListMap.containsKey(new Integer(hashcode)))
            svgStringList = svgStringListMap.get(new Integer(hashcode));
    	  else
    	  {
    		  pathwayComboBox.removeAllItems();
    		  pathwayComboBox.addItem(" ");
    		  pathwayList.clear();
    		  svgStringList.clear();
    	  }
    		  
    	  if (pathwayListMap.containsKey(new Integer(hashcode)))
    	  {
    		  int selectIndex = pathwayComboItemSelectedMap.get(new Integer(hashcode));    		   
    		  
              pathwayList = pathwayListMap.get(new Integer(hashcode));
    		  pathwayComboBox.removeAllItems();
    		  pathwayComboBox.addItem(" ");
    		  for (int i=pathwayList.size()-1; i>=0; i--)
    			  pathwayComboBox.addItem(pathwayList.get(i));    		  
    		  pathwayComboBox.setSelectedIndex(selectIndex);    		 
    		  pathwayComboBox.revalidate();    
    	  
    	  
    	  } 	  
    		
    	  if (tabPanelSelectedMap.containsKey(new Integer(hashcode)))
    	  {
    		  jTabbedPane1.setSelectedIndex(tabPanelSelectedMap.get(new Integer(hashcode)));
    	  }
    	  else
    		  jTabbedPane1.setSelectedIndex(0);
    	    //pathwayComboBox.setSelectedIndex(0);     		 
            //jTabbedPane1.setTitleAt(1,"Pathway");   		 
      
    	    if ( maSet != null)
    	    oldHashCode = maSet.hashCode();
          
    }
    
   
    
    
    //****************************************************************
    //The following code integrate Pathway component with Annotations
    
    
    
    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInitPathways() throws Exception {
    	 
        pathwayPanel.setLayout(borderLayout2);         
        pathwayPanel.add(jscrollPanePathway, BorderLayout.CENTER);
        pathwayPanel.add(pathwayTool, BorderLayout.NORTH);
         
        pathwayComboBox.setMaximumSize(new Dimension(130, 25));
        pathwayComboBox.setMinimumSize(new Dimension(130, 25));
        pathwayComboBox.setPreferredSize(new Dimension(130, 25));
        pathwayComboBox.insertItemAt(" ", 0);
        pathwayComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	pathwayComboBox_actionPerformed(e);
            }
        });
        
        
        clearDiagramButton.setForeground(Color.black);
        clearDiagramButton.setToolTipText("");
        clearDiagramButton.setFocusPainted(true);
        clearDiagramButton.setText("Clear Diagram");
        clearDiagramButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearDiagramButton_actionPerformed(e);
            }
        });
        
        clearHistButton.setForeground(Color.black);
        clearHistButton.setToolTipText("");
        clearHistButton.setFocusPainted(true);
        clearHistButton.setText("Clear History");
        clearHistButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	clearHistButton_actionPerformed(e);
            }
        });
        
        imagePathwayButton.setForeground(Color.black);
        imagePathwayButton.setToolTipText("");
        imagePathwayButton.setFocusPainted(true);
        imagePathwayButton.setText("Image Snapshot");
        imagePathwayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	createImageSnapshot();
            }
        });
                
        pathwayTool.add(pathwayComboBox, null);
        pathwayTool.add(clearDiagramButton);
        pathwayTool.add(clearHistButton);
        pathwayTool.add(component1);
        pathwayTool.add(imagePathwayButton);
        
        svgCanvas.addLinkActivationListener(new LinkActivationListener() {
            public void linkActivated(LinkActivationEvent lae) {
                svgCanvas_linkActivated(lae);
            }

        });
        jscrollPanePathway.getViewport().add(svgCanvas, null);
        
        svgStringList = new HashMap<String, String>();
        pathwayList = new ArrayList<String>();
        svgStringListMap = new HashMap<Integer, HashMap<String, String>>();
        pathwayListMap = new HashMap<Integer, ArrayList<String>>();
        pathwayComboItemSelectedMap = new HashMap<Integer, Integer>();
        tabPanelSelectedMap = new HashMap<Integer, Integer>();
    }
    
    private void pathwayComboBox_actionPerformed(ActionEvent e) {
       
    	 
    	Object pathwayName = pathwayComboBox.getSelectedItem();
    	if (pathwayName != null && !pathwayName.toString().trim().equals(""))
    	{    setSvg(svgStringList.get(pathwayName));
    	    jTabbedPane1.setTitleAt(1, pathwayName.toString());
    	}
    	else
    	{
    	    try{
    		svgCanvas.setDocument(null);
    	    }catch(IllegalStateException ex)
    	    {
    	    	//do nothing 
    	    }
    		svgCanvas.revalidate();
            pathwayPanel.revalidate();
            jTabbedPane1.setTitleAt(1,"Pathway");
    	}     
    	 
    }
    
    private void clearDiagramButton_actionPerformed(ActionEvent e) {
    	Object pathwayName = pathwayComboBox.getSelectedItem();
    	if (pathwayName != null && !pathwayName.toString().trim().equals(""))
    	{
    		svgStringList.remove(pathwayName);    	 
    		pathwayList.remove(pathwayName);    		 
    		//svgCanvas.setDocument(null);   	 
    		pathwayComboBox.setSelectedIndex(0);  	
    		pathwayComboBox.removeItem(pathwayName);    		 
    		svgCanvas.revalidate();
            pathwayPanel.revalidate();
            jTabbedPane1.setTitleAt(1,"Pathway");
              
    	}  	
    	 
    	
    }
    
    private void clearHistButton_actionPerformed(ActionEvent e) {
        
    	pathwayComboBox.removeAllItems();
    	pathwayComboBox.insertItemAt(" ", 0);
    	svgStringList.clear();    	 
		pathwayList.clear();
		try{
    	svgCanvas.setDocument(null); 
		}catch(IllegalStateException ex)
	    {
	    	//do nothing 
	    }
		svgCanvas.revalidate();
        pathwayPanel.revalidate();
        jTabbedPane1.setTitleAt(1,"Pathway");    
    }

    
    @Publish public ImageSnapshotEvent createImageSnapshot() {
    	Object pathwayName = pathwayComboBox.getSelectedItem();
    	if (pathwayName != null && !pathwayName.toString().trim().equals(""))
    	{
    	   Dimension panelSize = svgCanvas.getSize();
           BufferedImage image = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_RGB);
           Graphics g = image.getGraphics();
           svgCanvas.paint(g);
           ImageIcon icon = new ImageIcon(image, pathwayName.toString());
           org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(pathwayName.toString(), icon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
           return event;
        }
    	else
    		return null;
    }

    
    
    
  

    void addPathwayName(String pathwayName, String pathwayDiagram){
    	 
    	if (svgStringList.containsKey(pathwayName))
    	{	
    	    pathwayComboBox.removeItem(pathwayName); 
    	    pathwayList.remove(pathwayName);
    	}    	 
    	svgStringList.put(pathwayName, pathwayDiagram);      	 
		pathwayList.add(pathwayName);	    	
    	pathwayComboBox.insertItemAt(pathwayName, 1);      
    	pathwayComboBox.setSelectedIndex(1);
    	pathwayComboBox.revalidate();
    	
        
    }
    

    /**
    * Interface <code>AnnotationsListener</code> method that received a
    * selected <code>Pathway</code> to be shown in the <code>PathwayPanel</code>
    * plugin.
    *
    * @param ae <code>AnnotationsEvent</code> that contains the
    *           <code>Pathway</code> to be shown
    */
 
    public void receive(org.geworkbench.events.AnnotationsEvent ae){
        
        pathway = ae.getPathway();
        
        Runnable pway = new Runnable() {
          
        	private String pathwayName = null;
        	private String pathwayDiagram = null;
        	public void run() {
            org.geworkbench.util.ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);             
            pb.setTitle("Constructing SVG Pathway");
            pb.setMessage("Creating Image..");
            pb.start();           
            
            addPathwayName(pathway.getPathwayName(), pathway.getPathwayDiagram());
           
            pb.stop(); 
            pb.dispose();            
          
            Container parent = pathwayPanel.getParent();                
            if (parent instanceof JTabbedPane)
            {    ((JTabbedPane) parent).setSelectedComponent(pathwayPanel);
               JTabbedPane p =  (JTabbedPane) parent;
               p.setTitleAt(1, pathway.getPathwayName());
            }
         }
        };
    Thread t = new Thread(pway);
    t.setPriority(Thread.MIN_PRIORITY);
    t.start();
   } 



    private HashMap<String, String> svgStringList = null;   
    private HashMap<Integer, HashMap<String, String>> svgStringListMap = null;
    private ArrayList<String> pathwayList = null;
    private HashMap<Integer, ArrayList<String>> pathwayListMap = null;   
    private HashMap<Integer,Integer> tabPanelSelectedMap = null;
    private HashMap<Integer,Integer> pathwayComboItemSelectedMap = null;
    
    
    
    private int oldHashCode = 0;
    
    
    private JPanel pathwayPanel = new JPanel();   
    private BorderLayout borderLayout2 = new BorderLayout();
    JToolBar pathwayTool = new JToolBar();
    JComboBox pathwayComboBox = new JComboBox();
    JButton clearDiagramButton = new JButton();
    JButton clearHistButton = new JButton();
    JButton imagePathwayButton = new JButton();
    
    Component component1 = Box.createVerticalStrut(8);
    
    /**
     * Visual Widget
     */
    private JScrollPane jscrollPanePathway = new JScrollPane();
    private JTextField pathwayName = new JTextField();
    
    /**
     * <code>Canvas</code> on which the Pathway SVG image is drawn
     */
    private JSVGCanvas svgCanvas = new JSVGCanvas(new UserAgent(), true, true);
    private static final String CABIO_BASE_URL = "http://cmap.nci.nih.gov/";

       
    
   
    org.geworkbench.util.annotation.Pathway pathway = null;

     
    /**
     * Wrapper method for setting the <code>SVGDocument</code> received
     * from the <code>Pathway</code> objects obtained from a caBIO search
     *
     * @param svgString SVG document returned from a caBIO search as a String
     */
    private void setSvg(String svgString) {
        if (svgString != null) {
            StringReader reader = new StringReader(svgString);
            Document document = null;
            try {
                String parser = XMLResourceDescriptor.getXMLParserClassName();
                SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
                document = f.createDocument(null, reader);
            } catch (IOException ex) {
            }
           
            svgCanvas.setDocument(document); 
            if (svgCanvas.getGraphics() != null)
     	    {
              try
              {
    	      svgCanvas.resetRenderingTransform();   	    
              }
              catch(IllegalStateException ex)
      	      {
      	    	//do nothing 
      	      }
     	    }
            svgCanvas.revalidate();
            pathwayPanel.revalidate(); 
        } else {
            JOptionPane.showMessageDialog(pathwayPanel, "No Pathway diagram obtained from caBIO", "Diagram missing", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    private void walk(Node node) {
        int type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE: {
                System.out.println("<?xml version=\"1.0\" encoding=\"" + "UTF-8" + "\"?>");
                break;
            }

            case Node.ELEMENT_NODE: {
                System.out.print('<' + node.getNodeName());
                NamedNodeMap nnm = node.getAttributes();
                if (nnm != null) {
                    int len = nnm.getLength();
                    Attr attr;
                    for (int i = 0; i < len; i++) {
                        attr = (Attr) nnm.item(i);
                        System.out.print(" " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
                    }

                }

                System.out.print('>');
                break;
            }

            case Node.ENTITY_REFERENCE_NODE: {
                System.out.print('&' + node.getNodeName() + ';');
                break;
            }

            case Node.CDATA_SECTION_NODE: {
                System.out.print("<![CDATA[" + node.getNodeValue() + "]]>");
                break;
            }

            case Node.TEXT_NODE: {
                System.out.print(node.getNodeValue());
                break;
            }

            case Node.PROCESSING_INSTRUCTION_NODE: {
                System.out.print("<?" + node.getNodeName());
                String data = node.getNodeValue();
                if (data != null && data.length() > 0) {
                    System.out.print(' ');
                    System.out.print(data);
                }

                System.out.println("?>");
                break;
            }

        }

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            walk(child);
        }

        if (type == Node.ELEMENT_NODE)
            System.out.print("</" + node.getNodeName() + ">");
    }

    private static final String URI_PREFIX = "/CMAP/";
    private void svgCanvas_linkActivated(LinkActivationEvent lae) {
    	String referenceUri = lae.getReferencedURI();
    	if(referenceUri.startsWith(URI_PREFIX)) {
    		referenceUri = referenceUri.substring(URI_PREFIX.length());
    	} else {
    		log.warn("reference URI does not start with /CAMP/ as expected: "+referenceUri);
    	}
        String uri = CABIO_BASE_URL + referenceUri;

/*
        int index = uri.indexOf("BCID");
        String bcid = uri.substring(index + 5, uri.length());
        GeneSearchCriteria criteria = new GeneSearchCriteriaImpl();
        GeneAnnotation[] matchingGenes = criteria.searchByBCID(bcid);
//        criteria.search();
//        GeneAnnotation[] matchingGenes = criteria.getGeneAnnotations();
        assert matchingGenes.length == 1 : "Search on BCID should return just 1 Gene";
*/
        try {
            log.debug("Opening " + uri);
            org.geworkbench.util.BrowserLauncher.openURL(uri);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    protected class UserAgent implements SVGUserAgent {
        protected UserAgent() {
        }

        public void displayError(String message) {
            JOptionPane pane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
            JDialog dialog = pane.createDialog(pathwayPanel, "ERROR");
            dialog.setModal(false);
            dialog.setVisible(true);
        }

        public void checkLoadExternalResource(ParsedURL resourceURL, ParsedURL docURL) {
        }

        public void checkLoadScript(String scriptType, ParsedURL scriptURL, ParsedURL docURL) {
        }

        public void displayError(Exception ex) {
            JErrorPane pane = new JErrorPane(ex, JOptionPane.ERROR_MESSAGE);
            JDialog dialog = pane.createDialog(pathwayPanel, "ERROR");
            dialog.setModal(false);
            dialog.setVisible(true);
        }

        public void displayMessage(String message) {
        }

        public String getAlternateStyleSheet() {
            return "alternate";
        }

        public float getBolderFontWeight(float f) {
            return 10f;
        }

        public String getDefaultFontFamily() {
            return "Arial";
        }

        public ExternalResourceSecurity getExternalResourceSecurity(ParsedURL resourceURL, ParsedURL docURL) {
            return new DefaultExternalResourceSecurity(resourceURL, docURL);
        }

        public float getLighterFontWeight(float f) {
            return 8f;
        }

        public float getMediumFontSize() {
            return 9f;
        }

        public float getPixelToMM() {
            return 0.264583333333333333333f; // 96 dpi
        }

        public float getPixelUnitToMillimeter() {
            return 0.264583333333333333333f; // 96 dpi
        }

        public ScriptSecurity getScriptSecurity(String scriptType, ParsedURL scriptURL, ParsedURL docURL) {
            return new DefaultScriptSecurity(scriptType, scriptURL, docURL);
        }

        public String getLanguages() {
            return Locale.getDefault().getLanguage();
        }

        public String getUserStyleSheetURI() {
            return null;
        }

        public String getXMLParserClassName() {
            return XMLResourceDescriptor.getXMLParserClassName();
        }

        public boolean isXMLParserValidating() {
            return true;
        }

        public String getMedia() {
            return "screen";
        }

        public void openLink(String uri, boolean newc) {
        }

        public void showAlert(String message) {
        }

        public boolean showConfirm(String message) {
            return true;
        }

        public boolean supportExtension(String s) {
            return false;
        }

        public String showPrompt(java.lang.String message) {
            return "";
        }

        public String showPrompt(String message, String defaultValue) {
            return "";
        }

        public void handleElement(Element elt, Object data) {
        }
        
       

    }

    
    
    public void stopAlgorithm(ProgressBar pb) {
		
    	stopAlgorithm = false;    	 
		pb.stop();
        pb.dispose(); 
	 
	}
    
    /**
	 * @param o
	 * @param arg
	 */
	public void update(Observable o, Object arg) {
		stopAlgorithm = true;
		GeneAnnotationImpl.stopAlgorithm = true;
    }
    
    
    
    
    
     
    
}
