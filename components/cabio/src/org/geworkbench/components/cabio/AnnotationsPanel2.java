package org.geworkbench.components.cabio;

import gov.nih.nci.cabio.domain.Agent;
import gov.nih.nci.cabio.domain.DiseaseOntology;
import gov.nih.nci.cabio.domain.Evidence;
import gov.nih.nci.cabio.domain.Gene;
import gov.nih.nci.cabio.domain.GeneAgentAssociation;
import gov.nih.nci.cabio.domain.GeneDiseaseAssociation;
import gov.nih.nci.cabio.domain.GeneFunctionAssociation;
import gov.nih.nci.common.domain.DatabaseCrossReference;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.TableCellRenderer;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Title: caBio component
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003 -2004
 * </p>
 * <p>
 * Company: Columbia University
 * </p>
 * 
 * Component responsible for displaying Gene Annotation obtained from caBIO
 * Displays data in a Tabular format with 3 columns. First column contains
 * marker information. The second column contains The Gene Description and the
 * third column contains a list of known Pathways that this gene's product
 * participates in.
 * 
 * It also displaying Disease and Agent information obtained from Cancer Gene
 * Index database through caBio. Displays data in two table with 6 columns each. 
 * 
 * @author yc2480
 * @version $Id: AnnotationsPanel2.java,v 1.2 2009-05-05 21:02:26 chiangy Exp $
 * 
 */
@AcceptTypes({DSMicroarraySet.class})
@SuppressWarnings("unchecked")
public class AnnotationsPanel2 implements VisualPlugin, Observer{
    private static final String GENE_FINDER_PREFIX = "http://cgap.nci.nih.gov/Genes/GeneInfo?";
    private static final String RETRIEVE_INFORMATION = "Retrieve Annotations";
	private static final String RETRIEVE_PATHWAY_DATA = "Get gene annotations";
	private static final String RETRIEVE_CGI_DATA = "Get Disease/Agent associations (from CGI)";
    /**
     * Web URL prefix for obtaining Gene annotation
     */        
    private static final String GeneCards_PREFIX = "http://www.genecards.org/cgi-bin/carddisp.pl?gene=";
    /**
     * Web URL prefix for obtaining Pubmed article
     */        
    private static final String PUBMED_PREFIX = "http://www.ncbi.nlm.nih.gov/pubmed/";
    
    /**
     * Web URL prefix for obtaining Agent annotation
     */        
    private static final String EVS_PREFIX = "http://nciterms.nci.nih.gov/NCIBrowser/ConceptReport.jsp?dictionary=NCI_Thesaurus&code=";
	static Log log = LogFactory.getLog(AnnotationsPanel2.class);
    public static final int COL_MARKER = 0;
    public static final int COL_GENE = 1;
    public static final int COL_AGENT = 2;
    public static final int COL_DISEASE = 2;
    public static final int COL_ROLE = 3;
    public static final int COL_SENTENCE = 4;
    public static final int COL_PUBMED = 5;
    public static final int COL_EVSID = 6;
    JComboBox[] dropDownLists = new JComboBox[8];	//drop down lists for filters
    JPanel leftHeadPanel = new JPanel();	//I put it as global variable so we can change their width at run time.
    JPanel rightHeadPanel = new JPanel();	//I put it as global variable so we can change their width at run time.
	JPopupMenu expandCollapseRetrieveAllMenu = new JPopupMenu();
	DSGeneMarker selectedMarker = null;
	int selectedRow = -1;
	String selectedGene = "";
	String selectedDisease = "";
	SortableTable selectedTable = null;
    //Aris want the table to sort by number of records when the records just been retrieved.
    boolean sortByNumberOfRecords=true;

    private boolean stopAlgorithm = false;

	private String wrapInHTML(String s) {
		return "<html><a href=\"__noop\">" + s + "</a></html>";
	}

	private String unwrapFromHTML(String s) {
		return s.substring("<html><a href=\"__noop\">".length(), s.length()
				- "</a></html>".length());
	}

	/*
	 * 
	 */
    private class AnnotationTableModel extends SortableTableModel {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public static final int COL_MARKER = 0;
        public static final int COL_GENE = 1;
        public static final int COL_PATHWAY = 2;

        private MarkerData[] markerData;
        private GeneData[] geneData;
        private PathwayData[] pathwayData;

        private Integer[] indices;
        private int size;

        public AnnotationTableModel(MarkerData[] markerData, GeneData[] geneData, PathwayData[] pathwayData) {
            this.markerData = markerData;
            this.geneData = geneData;
            this.pathwayData = pathwayData;
            size = pathwayData.length;
            indices = new Integer[size];
            resetIndices();
        }

        public AnnotationTableModel() {
            this.markerData = new MarkerData[0];
            this.geneData = new GeneData[0];
            this.pathwayData = new PathwayData[0];
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
                    return markerData[indices[rowIndex]].name;
                case COL_GENE:
                    return wrapInHTML(geneData[indices[rowIndex]].name);
                case COL_PATHWAY:
                    return wrapInHTML(pathwayData[indices[rowIndex]].name);
            }
            return null;
        }

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
                    MarkerData marker = markerData[indices[rowIndex]];
                    activateMarker(marker);
                    break;
                case COL_GENE:
                    GeneData gene = geneData[indices[rowIndex]];
                    activateGene(gene);
                    break;
                case COL_PATHWAY:
                    PathwayData pathway = pathwayData[indices[rowIndex]];
                    // Could be the blank "(none)" pathway.
                    if (pathway.pathway != null) {
                        activatePathway(pathway);
                    }
                    break;
            }
        }
    }

	/*
	 * 
	 */
	private class CGITableModel extends SortableTableModel {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
        final int numOfColumns = 6;		
		private MarkerData[] markerData;
        private GeneData[] geneData;
        private DiseaseData[] diseaseData;
        private RoleData[] roleData;
        private SentenceData[] sentenceData;
        private PubmedData[] pubmedData;
        
        private Integer[] indices;			//for sorting feature
        private Integer[] filterIndices;	//for filtering feature
        private int size;
        private int filteredSize;
        //for expand/collapse feature
        private Set<String> expandedByKeys;	//store unique gene,disease pairs of expanded nodes.
        private Boolean[] repExpandedDiseaseList;	//TRUE if that record is the representative node for it's collapsed group.	
        private Boolean[] expandedDiseaseList;	//TRUE if that record is expanded.	
        //for filter feature work with expand/collapse
        private String[] filterByStrings;	//store filterBy string for each column, empty if not filtered.  
        private Boolean[] filteredDiseaseList;	//TRUE if that record is filtered.
        int[] numOfDuplicatesCache;
        
        public CGITableModel(MarkerData[] markerData, GeneData[] geneData, DiseaseData[] diseaseData, RoleData[] roleData, SentenceData[] sentenceData, PubmedData[] pubmedData) {
            this.markerData = markerData;
            this.geneData = geneData;
            this.diseaseData = diseaseData;
            this.roleData = roleData;
            this.sentenceData = sentenceData;
            this.pubmedData = pubmedData;
            size = diseaseData.length;
            filteredSize = size;
            indices = new Integer[size];
            filterIndices = new Integer[size];
            expandedByKeys = new HashSet<String>();
            repExpandedDiseaseList = new Boolean[size];
            expandedDiseaseList = new Boolean[size];
            filterByStrings = new String[numOfColumns];
            filteredDiseaseList = new Boolean[size];
            numOfDuplicatesCache = new int[size];
            resetIndices();
            for (int i = 0; i < size; i++) {
                indices[i] = i;
                filterIndices[i] = i;
                expandedDiseaseList[i]=true;
                filteredDiseaseList[i]=false;
                repExpandedDiseaseList[i]=true;
            }
        }

        /**
		 * This is the method contains the algorithm to determine if a record is
		 * already exist in the model or not.
		 * 
		 * @param markerData
		 * @param geneData
		 * @param diseaseData
		 * @param roleData
		 * @param sentenceData
		 * @param pubmedData
		 * @return
		 */
        public boolean containsRecord(MarkerData markerData, GeneData geneData,
				DiseaseData diseaseData, RoleData roleData,
				SentenceData sentenceData, PubmedData pubmedData) {
			for (int i = 0; i < this.markerData.length; i++) {
				MarkerData markerData2 = this.markerData[i];
				GeneData geneData2 = this.geneData[i];
				DiseaseData diseaseData2 = this.diseaseData[i];
				RoleData roleData2 = this.roleData[i];
				SentenceData sentenceData2 = this.sentenceData[i];
				PubmedData pubmedData2 = this.pubmedData[i];

				if (markerData2.name.equals(markerData.name)
						&& geneData2.name.equals(geneData.name)
						&& diseaseData2.name.equals(diseaseData.name)
						&& roleData2.role.equals(roleData.role)
						&& sentenceData2.sentence.equals(sentenceData.sentence)
						&& pubmedData2.id.equals(pubmedData.id)) {
					return true;
				}
			}
			return false;
		}
        
        public CGITableModel() {
            this.markerData = new MarkerData[0];
            this.geneData = new GeneData[0];
            this.diseaseData = new DiseaseData[0];
            this.roleData = new RoleData[0];
            this.sentenceData = new SentenceData[0];
            this.pubmedData = new PubmedData[0];
            size = 0;
            filteredSize = size;
            indices = new Integer[0];
            filterIndices = new Integer[0];
            expandedByKeys = new HashSet<String>();
            repExpandedDiseaseList = new Boolean[size];
            expandedDiseaseList = new Boolean[0];
            filterByStrings = new String[numOfColumns];            
            filteredDiseaseList = new Boolean[0];
        }


        private void resetIndices() {
            for (int i = 0; i < size; i++) {
                indices[i] = i;
            }
        }

        public void filterBy(int columnIndex, String value) {
			filterByStrings[columnIndex]=value;
			if ((value == null)||(value.equals(""))) { // show all
				filteredSize = 0;
				for (int i = 0; i < markerData.length; i++) {
					if (expandedDiseaseList[i]){
						filterIndices[filteredSize]=indices[i];
						filteredSize++;
					}
					filteredDiseaseList[i]=false;
				}
			} else {
				// filter rows
				filteredSize = 0;
				switch (columnIndex) {
				case COL_MARKER:
					for (int i = 0; i < markerData.length; i++) {
						if (markerData[indices[i]].name.equals(value)){
							if (expandedDiseaseList[i]){
								filterIndices[filteredSize]=indices[i];
								filteredSize++;
							}else{
								filterIndices[i]=-1;								
							}
							filteredDiseaseList[i]=false;
						}else{
							filterIndices[i]=-1;
							filteredDiseaseList[i]=true;
						}
					}
					break;
				case COL_GENE:
					for (int i = 0; i < markerData.length; i++) {
						if (geneData[indices[i]].name.equals(value)){
							if (expandedDiseaseList[i]){
								filterIndices[filteredSize]=indices[i];
								filteredSize++;
							}else{
								filterIndices[i]=-1;								
							}
							filteredDiseaseList[i]=false;
						}else{
							filterIndices[i]=-1;
							filteredDiseaseList[i]=true;
						}
					}
					break;
				case COL_DISEASE:
					for (int i = 0; i < markerData.length; i++) {
						if (diseaseData[i].name.equals(value)){
							if (expandedDiseaseList[i]){
								filterIndices[filteredSize]=i;
								filteredSize++;
							}else{
								filterIndices[i]=-1;								
							}
							filteredDiseaseList[i]=false;
						}else{
							filterIndices[i]=-1;
							filteredDiseaseList[i]=true;
						}
					}
					break;
				case COL_ROLE:
					for (int i = 0; i < markerData.length; i++) {
						if (roleData[indices[i]].role.equals(value)){
							if (expandedDiseaseList[i]){
								filterIndices[filteredSize]=indices[i];
								filteredSize++;
							}else{
								filterIndices[i]=-1;								
							}
							filteredDiseaseList[i]=false;
						}else{
							filterIndices[i]=-1;
							filteredDiseaseList[i]=true;
						}
					}
					break;
				}
			}
		}
        Map collapsedDisease = new HashMap(); //key index
        
        public void collapseBy(SortableTable aTable, String gene, String disease) {
        	log.debug("collapseBy "+gene+","+disease);
			filteredSize = 0;
			String collapsedKey = gene+disease;
			expandedByKeys.remove(collapsedKey);
			Map uniqList = new HashMap();
			for (int i = 0; i < markerData.length; i++) {
				filterIndices[i]=-1;
			}
			for (int i = 0; i < markerData.length; i++) {
				String key = geneData[indices[i]].name+diseaseData[indices[i]].name;
				if (!filteredDiseaseList[i]){
					if (geneData[indices[i]].name.equals(gene) && diseaseData[indices[i]].name.equals(disease))
						if (!uniqList.containsKey(key)){
							uniqList.put(key, indices[i]);
							repExpandedDiseaseList[i]=true;
							if (collapsedDisease.containsValue(key)){
								//skip this one
							}else
								collapsedDisease.put(new Integer(i), key);
						}else{
							repExpandedDiseaseList[i]=false;
							expandedDiseaseList[i] = false;
						}
					if (expandedDiseaseList[i]){
						filterIndices[filteredSize]=indices[i];
						filteredSize++;
					}
					numOfDuplicatesCache[i]=getNumOfDuplicates(geneData[indices[i]].name, diseaseData[indices[i]].name);
				}
				log.debug("filterIndices[i] (for "+key+")=="+filterIndices[i]);
			}
			log.debug("filteredSize becomes to "+filteredSize);
			aTable.revalidate();
			aTable.repaint();
		}

        /*
		 * This is a wrapper for expandBy, to be called from inside of this
		 * file. So program will know it's called by the user, not by the code.
		 * When called, it will set sortByNumberOfRecords to false, so the
		 * program will exit it's first-time status. And the sorting will back
		 * to normal status (sort by String instead of sort by number of
		 * records).
		 */
        private void _expandBy(SortableTable aTable, String gene, String disease) {
        	sortByNumberOfRecords=false;
        	expandBy(aTable, gene, disease);
        	//sort by disease
        	((SortableTableModel)aTable.getModel()).sortByColumn(2,true);
        	//sort by marker
        	((SortableTableModel)aTable.getModel()).sortByColumn(0,true);
        }
        
        public void expandBy(SortableTable aTable, String gene, String disease) {
			String expandKey = gene+disease;
			expandedByKeys.add(expandKey);
			filteredSize = 0;
			for (int i = 0; i < markerData.length; i++) {
					//log.error(i+": "+geneData[indices[i]].name+", "+ diseaseData[indices[i]].name);
					if (geneData[i].name.equals(gene) && diseaseData[i].name.equals(disease)){
						expandedDiseaseList[i] = true;
						//log.error("expand ^^^");
					}
					if (expandedDiseaseList[i] && (!filteredDiseaseList[i])){
						if ((repExpandedDiseaseList[i])&&(!(geneData[i].name.equals(gene) && diseaseData[i].name.equals(disease)))){
							//it's expanded because it's a representatives, not because it's expanded in this run. 
						}else{
							//it's expanded, remove this one from collapsedDisease list
							collapsedDisease.remove(new Integer(i));
						}
						filterIndices[filteredSize]=i;
						filteredSize++;
					}else
						filterIndices[i]=-1;
			}
			aTable.revalidate();
			aTable.repaint();
		}

        public void toggleExpandCollapse(SortableTable aTable, String gene, String disease){
        	boolean collapsed = false;
        	String key = gene+disease;
        	if (collapsedDisease.containsValue(key)){
        		collapsed = true;
        	}
			if (collapsed)
				_expandBy(aTable, gene, disease);
			else
				collapseBy(aTable, gene, disease);
        }

        /**
		 * Internally used for retrieve all feature. Can by used externally.
		 * 
		 * @param index
		 *            Where to insert to. 0 will insert from the beginning. The
		 *            records which has index large or equals to this number
		 *            will be moved toward the end of array to have space to
		 *            insert these records.
		 * @param markerData
		 * @param geneData
		 * @param diseaseData
		 * @param roleData
		 * @param sentenceData
		 * @param pubmedData
		 */
        public void insertData(int index, MarkerData[] markerData,
				GeneData[] geneData, DiseaseData[] diseaseData,
				RoleData[] roleData, SentenceData[] sentenceData,
				PubmedData[] pubmedData) {
			int sizeOfRecords = markerData.length;
			if (sizeOfRecords==0) return;
			if ((geneData.length != sizeOfRecords)
					|| (diseaseData.length != sizeOfRecords)
					|| (roleData.length != sizeOfRecords)
					|| (sentenceData.length != sizeOfRecords)
					|| (pubmedData.length != sizeOfRecords)) {
				log.error("Insert data should have equal numbers of rows for all arrays.");
				return;
			}
			
            //calculate new size
            int newSize = size+sizeOfRecords;

			//TODO: append arrays (eg: markerData, geneData) to old arrays.
            //this.markerData = markerData;
            MarkerData[] newMarkerData = new MarkerData[newSize];
            for (int i = 0; i < newMarkerData.length; i++) {
				if (i<index)
					newMarkerData[i]=this.markerData[i];
				else
					newMarkerData[i]=markerData[i-index];
			}
            //this.geneData = geneData;
            GeneData[] newGeneData = new GeneData[newSize];
            for (int i = 0; i < newGeneData.length; i++) {
				if (i<index)
					newGeneData[i]=this.geneData[i];
				else
					newGeneData[i]=geneData[i-index];
			}
            //this.diseaseData = diseaseData;
            DiseaseData[] newDiseaseData = new DiseaseData[newSize];
            for (int i = 0; i < newDiseaseData.length; i++) {
				if (i<index)
					newDiseaseData[i]=this.diseaseData[i];
				else
					newDiseaseData[i]=diseaseData[i-index];
			}
            //this.roleData = roleData;
            RoleData[] newRoleData = new RoleData[newSize];
            for (int i = 0; i < newRoleData.length; i++) {
				if (i<index)
					newRoleData[i]=this.roleData[i];
				else
					newRoleData[i]=roleData[i-index];
			}
            //this.sentenceData = sentenceData;
            SentenceData[] newSentenceData = new SentenceData[newSize];
            for (int i = 0; i < newSentenceData.length; i++) {
				if (i<index)
					newSentenceData[i]=this.sentenceData[i];
				else
					newSentenceData[i]=sentenceData[i-index];
			}
            //this.pubmedData = pubmedData;
            PubmedData[] newPubmedData = new PubmedData[newSize];
            for (int i = 0; i < newPubmedData.length; i++) {
				if (i<index)
					newPubmedData[i]=this.pubmedData[i];
				else
					newPubmedData[i]=pubmedData[i-index];
			}

            
            //update indices
            //indices = new Integer[size];
    		//for this implementation, I put it at the end of the list.
            Integer[] newIndices = new Integer[newSize];	//with new size
            for (int i = 0; i < newIndices.length; i++) {
            	if (i<size)
            		newIndices[i]=indices[i];
            	else
            		newIndices[i]=i;
			} 
                        
            //filterIndices = new Integer[size];
            Integer[] newFilterIndices = new Integer[newSize];
            for (int i = 0; i < newFilterIndices.length; i++) {
            	if (i<size)
            		newFilterIndices[i]=filterIndices[i];
            	else
            		newFilterIndices[i]=i;
			} 
            
            //expandedByKeys = new HashSet<String>();
            HashSet<String> newExpandedByKeys = new HashSet<String>();
            newExpandedByKeys.addAll(expandedByKeys);
            for (int i = 0; i < geneData.length; i++) {
            	String key = geneData[i].name+diseaseData[i].name;
                newExpandedByKeys.add(key);				
			}
            
            //repExpandedDiseaseList = new Boolean[size];
            //expandedDiseaseList = new Boolean[size];
			Map uniqList = new HashMap();
            Boolean[] newRepExpandedDiseaseList = new Boolean[newSize];
            Boolean[] newExpandedDiseaseList = new Boolean[newSize];
            for (int i = 0; i < newRepExpandedDiseaseList.length; i++) {
            	if (i<index){
            		newRepExpandedDiseaseList[i]=repExpandedDiseaseList[i];
            		newExpandedDiseaseList[i]=expandedDiseaseList[i];
            	}else{
                	String key = geneData[i-index].name+diseaseData[i-index].name;
					if (!uniqList.containsKey(key)){
						uniqList.put(key, i);
						newRepExpandedDiseaseList[i]=true;
					}else
						newRepExpandedDiseaseList[i]=false;
					newExpandedDiseaseList[i]=true;
				}
			}
            
            //filterByStrings = new String[numOfColumns];
            //this will not be changed during insertion, we use the old one.
            String[] newFilterByStrings = filterByStrings;
            
            //filteredDiseaseList = new Boolean[size];
            Boolean[] newFilteredDiseaseList = new Boolean[newSize];
            for (int i = 0; i < newFilteredDiseaseList.length; i++) {
            	if (i<index){
            		newFilteredDiseaseList[i]=filteredDiseaseList[i];
            	}else{
            		//since there's no way user can retrieve a filtered-out record.
            		newFilteredDiseaseList[i]=false;
            	}
			}

            //numOfDuplicatesCache = new int[size];
            int[] newNumOfDuplicatesCache = new int[newSize];
            for (int i = 0; i < newNumOfDuplicatesCache.length; i++) {
            	if (i<index){
            		newNumOfDuplicatesCache[i]=numOfDuplicatesCache[i];
            	}else{
            		//since at the time it collapse, this number will be calculate again, I put 1 here.
            		newNumOfDuplicatesCache[i]=1;
            	}
			}

            this.markerData = newMarkerData;
            this.geneData = newGeneData;
            this.diseaseData = newDiseaseData;
            this.roleData = newRoleData;
            this.sentenceData = newSentenceData;
            this.pubmedData = newPubmedData;
            size = newSize;
            //filteredSize = newFilteredSize;
            indices = newIndices;
            filterIndices = newFilterIndices;
            expandedByKeys = newExpandedByKeys;
            repExpandedDiseaseList = newRepExpandedDiseaseList;
            expandedDiseaseList = newExpandedDiseaseList;
            filterByStrings = newFilterByStrings;
            filteredDiseaseList = newFilteredDiseaseList;
            numOfDuplicatesCache = newNumOfDuplicatesCache;
            //now, everything is in the model, we'll need to refresh the table
            filterBy(0, "");	//first, un-hide the hided records.
            //expand the collapsed
            selectedTable = diseaseTable;
            for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
				((CGITableModel) selectedTable.getModel())._expandBy(selectedTable,
						((GeneData) (((CGITableModel) selectedTable
								.getModel()).getObject(j,
								COL_GENE))).name,
						((DiseaseData) (((CGITableModel) selectedTable
								.getModel()).getObject(j,
								COL_DISEASE))).name);                            	
            }
            //recollapse them
            for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
				((CGITableModel) selectedTable.getModel()).collapseBy(selectedTable,
						((GeneData) (((CGITableModel) selectedTable
								.getModel()).getObject(j,
								COL_GENE))).name,
						((DiseaseData) (((CGITableModel) selectedTable
								.getModel()).getObject(j,
								COL_DISEASE))).name);                            	
            }
            selectedTable = agentTable;
            for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
				((CGITableModel) selectedTable.getModel()).expandBy(selectedTable,
						((GeneData) (((CGITableModel) selectedTable
								.getModel()).getObject(j,
								COL_GENE))).name,
						((DiseaseData) (((CGITableModel) selectedTable
								.getModel()).getObject(j,
								COL_DISEASE))).name);                            	
            }
            //recollapse them
            for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
				((CGITableModel) selectedTable.getModel()).collapseBy(selectedTable,
						((GeneData) (((CGITableModel) selectedTable
								.getModel()).getObject(j,
								COL_GENE))).name,
						((DiseaseData) (((CGITableModel) selectedTable
								.getModel()).getObject(j,
								COL_DISEASE))).name);                            	
            }
		}
        
        public int getRowCount() {
            return filteredSize;
        }

        public int getColumnCount() {
			return numOfColumns;
        }

        public Object getObjectAt(int rowIndex, int columnIndex) {
        	log.debug(rowIndex+","+columnIndex);
        	Integer origIndex = null;
        	try{
            switch (columnIndex) {
                case COL_MARKER:
                    return markerData[indices[filterIndices[rowIndex]]];
                case COL_GENE:
                    return geneData[indices[filterIndices[rowIndex]]];
                case COL_DISEASE:
                	return diseaseData[filterIndices[rowIndex]];
                case COL_ROLE:
                	//FIXME: why following two lines are different? Would we have a bug?
                	origIndex = new Integer(indices[filterIndices[rowIndex]]);
               		return roleData[filterIndices[rowIndex]];
                case COL_SENTENCE:
                	origIndex = new Integer(indices[filterIndices[rowIndex]]);
               		return sentenceData[filterIndices[rowIndex]];
                case COL_PUBMED:
                	origIndex = new Integer(indices[filterIndices[rowIndex]]);
               		return pubmedData[filterIndices[rowIndex]];
                case COL_EVSID:
                    return diseaseData[filterIndices[rowIndex]];
            }
        	}catch (Exception e){
            	log.error(e,e);
            }
            return null;
        }
        public Object getObject(int rowIndex, int columnIndex) {
        	log.debug(rowIndex+","+columnIndex);
        	try{
            switch (columnIndex) {
                case COL_MARKER:
                    return markerData[rowIndex];
                case COL_GENE:
                    return geneData[rowIndex];
                case COL_DISEASE:
                	return diseaseData[rowIndex];
                case COL_ROLE:
               		return roleData[rowIndex];
                case COL_SENTENCE:
               		return sentenceData[rowIndex];
                case COL_PUBMED:
               		return pubmedData[rowIndex];
                case COL_EVSID:
                    return diseaseData[rowIndex];
            }
        	}catch (Exception e){
            	log.error(e,e);
            }
            return null;
        }
        public int getNumOfDuplicates(String geneName, String diseaseName){
    		int numOfDuplicates=0;
			for (int i = 0; i < markerData.length; i++) {
				if (geneData[i].name.equals(geneName) && diseaseData[i].name.equals(diseaseName)){
					numOfDuplicates++;
				}
			}
			return numOfDuplicates;
        }
        public Object getValueAt(int rowIndex, int columnIndex) {
        	log.debug(rowIndex+","+columnIndex);
        	Integer origIndex = null;
        	try{
            switch (columnIndex) {
                case COL_MARKER:
                	if ((filterIndices[rowIndex]<0)||(indices[filterIndices[rowIndex]]<0)) return null;
                    return markerData[indices[filterIndices[rowIndex]]].name;
                case COL_GENE:
                    return wrapInHTML(geneData[indices[filterIndices[rowIndex]]].name);
                case COL_DISEASE:
                	if (collapsedDisease.containsKey(indices[filterIndices[rowIndex]])){
                		int numOfDuplicates = numOfDuplicatesCache[indices[filterIndices[rowIndex]]];//getNumOfDuplicates(geneData[indices[filterIndices[rowIndex]]].name,diseaseData[filterIndices[rowIndex]].name);
                		return diseaseData[indices[filterIndices[rowIndex]]].name+"("+numOfDuplicates+")";
                	}else
                		return diseaseData[indices[filterIndices[rowIndex]]].name;
                case COL_ROLE:
                	origIndex = new Integer(indices[filterIndices[rowIndex]]);
                	if (collapsedDisease.containsKey(origIndex))
                		return "...";
                	else
                		return roleData[filterIndices[rowIndex]].role;
                case COL_SENTENCE:
                	origIndex = new Integer(indices[filterIndices[rowIndex]]);
                	if (collapsedDisease.containsKey(origIndex))
                		return "...";
                	else
                		return sentenceData[filterIndices[rowIndex]].sentence;
                case COL_PUBMED:
                	origIndex = new Integer(indices[filterIndices[rowIndex]]);
                	if (collapsedDisease.containsKey(origIndex))
                		return "...";
                	else
                		return wrapInHTML(pubmedData[filterIndices[rowIndex]].id);
                case COL_EVSID:
                    return diseaseData[filterIndices[rowIndex]].evsId;
            }
        	}catch (Exception e){
            	log.error(e,e);
            }
            return null;
        }
        
        public MarkerData getMarkerAt(int rowIndex, int columnIndex) {
        	log.debug("getMarkerAt "+rowIndex+","+columnIndex);
        	if ((rowIndex==-1) || (filterIndices[rowIndex]==-1)){
        		log.error("getMarkerAt "+rowIndex+","+columnIndex);
        		log.error("filterIndices[rowIndex] "+filterIndices[rowIndex]);
        		return null;
        	}
        	return markerData[filterIndices[rowIndex]];
        }
        
        public void sortByColumn(final int column, final boolean ascending) {
            resetIndices();
            final Comparable[][] columns = {markerData, geneData, diseaseData, roleData, sentenceData, pubmedData};
            Comparator<Integer> comparator = new Comparator<Integer>() {
                public int compare(Integer i, Integer j) {
                    if (ascending) {
                    	if ((i==-1)||(j==-1)) return -1;
                    	if (!expandedDiseaseList[i]) return -1;
                    	if (!expandedDiseaseList[j]) return -1;
                    	if (column>COL_DISEASE){
	                    	if (collapsedDisease.containsKey(i)&&collapsedDisease.containsKey(j))
	                    		return 0;
	                    	if (collapsedDisease.containsKey(i))
	                    		return 1;
	                    	if (collapsedDisease.containsKey(j))
	                    		return -1;
                    	}
                    	if (sortByNumberOfRecords && (column==COL_DISEASE)) {
                    		if (numOfDuplicatesCache[i]<numOfDuplicatesCache[j])
                    			return -1;
                    		else if (numOfDuplicatesCache[i]>numOfDuplicatesCache[j])
                    			return 1;
                    		else return 0;
                    	}
                        return columns[column][i].compareTo(columns[column][j]);
                    } else {
                    	if ((i==-1)||(j==-1)) return -1;
                    	if (!expandedDiseaseList[i]) return -1;
                    	if (!expandedDiseaseList[j]) return -1;
                    	if (column>COL_DISEASE){
	                    	if (collapsedDisease.containsKey(i)&&collapsedDisease.containsKey(j))
	                    		return 0;
                    		if (collapsedDisease.containsKey(i))
	                    		return -1;
                    		if (collapsedDisease.containsKey(j))
	                    		return 1;
                    	}
                    	if (sortByNumberOfRecords && (column==COL_DISEASE)) {
                    		if (numOfDuplicatesCache[i]<numOfDuplicatesCache[j])
                    			return 1;
                    		else if (numOfDuplicatesCache[i]>numOfDuplicatesCache[j])
                    			return -1;
                    		else return 0;
                    	}
                        return columns[column][j].compareTo(columns[column][i]);
                    }
                }
            };
            Arrays.sort(filterIndices, comparator);
            super.sortByColumn(column, ascending);
        }

        public boolean isSortable(int i) {
            return true;
        }

        /**
         * Handles activated cell in CGI table
         * @param rowIndex
         * @param columnIndex
         */
        public void activateCell(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_MARKER:
                    MarkerData marker = markerData[filterIndices[indices[rowIndex]]];
                    if (selectedTable.equals(diseaseTable))
                    	dropDownLists[4].setSelectedItem(marker.name);
                    else if(selectedTable.equals(agentTable))
                    	dropDownLists[0].setSelectedItem(marker.name);
                    agentTable.repaint();
                    activateMarker(marker);
                    diseaseTable.repaint();
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

		public MarkerData[] getMarkerData() {
			return markerData;
		}
    }

    private static class MarkerData implements Comparable {

        public String name;
        public DSGeneMarker marker;
        public String numOutOfNum;

        public MarkerData(DSGeneMarker marker, String numOutOfNum) {
            this.name = marker.getLabel();
            this.marker = marker;
            this.numOutOfNum = numOutOfNum;
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
         * Web URL prefix for obtaining CGAP annotation
         */
        private static final String GENE_FINDER_PREFIX = "http://cgap.nci.nih.gov/Genes/GeneInfo?";
        /**
         * Web URL prefix currently being used
         */
        public static final String PREFIX_USED = GENE_FINDER_PREFIX;

        public String name;
        public Gene gene;
        public String organism;
        
        public GeneData(String name, Gene gene) {
            this.name = name;
            this.gene = gene;
            this.organism = "";
        }

        public void setOrganism(String organism){
        	this.organism = organism;
        }

        public String getOrganism(){
        	return this.organism;
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
        public String evsId;
        public DiseaseData(String name, DiseaseOntology diseaseOntology, String evsId) {
            this.name = name;
            this.diseaseOntology = diseaseOntology;
            this.evsId = evsId;
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
        public String evsId;
        public AgentData(String name, Agent agent, String evsId) {
            this.name = name;
            this.agent = agent;
            this.evsId = evsId;
        }

        public int compareTo(Object o) {
            if (o instanceof AgentData) {
                return name.compareTo(((AgentData) o).name);
            }
            return -1;
        }
    }
    private static class EvsIdData implements Comparable {

        public String evsId;

        public EvsIdData(String evsId) {
            this.evsId = evsId;
        }

        public int compareTo(Object o) {
            if (o instanceof AgentData) {
                return evsId.compareTo(((EvsIdData) o).evsId);
            }
            return -1;
        }
    }

    /**
     * Default Constructor
     */
    public AnnotationsPanel2() {
        try {
            jbInit();
        }

        catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    /**
     * 
     * @return
     */
    public AnnotationsPanel2 getAnnotationsPanel()
    {
    	return this;
    }

    private boolean userAlsoWantCaBioData = false;
    private boolean userAlsoWantPathwayData = false;
    
    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {    	
    	 
    	mainPanel.setLayout( new GridLayout());
        mainPanel.add(jTabbedPane1);
        //Init button panel in annotation panel
        annoRetrieveButton.setHorizontalAlignment(SwingConstants.CENTER);
        annoRetrieveButton.setText(RETRIEVE_INFORMATION);
        annoRetrieveButton.setToolTipText("Retrieve gene and disease information for markers in activated panels");
        annoRetrieveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanels_actionPerformed(e);
            }

        });
        annoClearButton.setForeground(Color.black);
        annoClearButton.setToolTipText("");
        annoClearButton.setFocusPainted(true);
        annoClearButton.setText("Clear");
        annoClearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                annoClearButton_actionPerformed(e);
            }
        });
        
        annoRetrieveCaBioCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				userAlsoWantCaBioData = (e.getStateChange() == ItemEvent.SELECTED);
				//synchronize with other panel 
				cgiRetrieveCaBioCheckBox.setSelected(userAlsoWantCaBioData);
			}
		});
        annoRetrievePathwayCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				userAlsoWantPathwayData = (e.getStateChange() == ItemEvent.SELECTED);
				cgiRetrievePathwayCheckBox.setSelected(userAlsoWantPathwayData);
			}
		});
        //Init button panel in cgi panel
        cgiRetrieveButton.setHorizontalAlignment(SwingConstants.CENTER);
        cgiRetrieveButton.setText(RETRIEVE_INFORMATION);
        cgiRetrieveButton.setToolTipText("Retrieve gene and disease information for markers in activated panels");
        cgiRetrieveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanels_actionPerformed(e);
            }

        });
        cgiClearButton.setForeground(Color.black);
        cgiClearButton.setToolTipText("");
        cgiClearButton.setFocusPainted(true);
        cgiClearButton.setText("Clear");
        cgiClearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cgiClearButton_actionPerformed(e);
            }
        });
        
        cgiRetrieveCaBioCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				userAlsoWantCaBioData = (e.getStateChange() == ItemEvent.SELECTED);
				annoRetrieveCaBioCheckBox.setSelected(userAlsoWantCaBioData);
			}
		});
        cgiRetrievePathwayCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				userAlsoWantPathwayData = (e.getStateChange() == ItemEvent.SELECTED);
				annoRetrievePathwayCheckBox.setSelected(userAlsoWantPathwayData);
			}
		});
        // JPanel middleSectionPanel = new JPanel();
        //middleSectionPanel.setLayout(new BorderLayout());
        JPanel diseaseMenuAndTablePanel = new JPanel();
        diseaseMenuAndTablePanel.setLayout(new BorderLayout());
        diseaseMenuAndTablePanel.add(leftHeadPanel, BorderLayout.NORTH);
        diseaseMenuAndTablePanel.add(jScrollPane1, BorderLayout.CENTER);
        diseaseMenuAndTablePanel.setMinimumSize(new Dimension (100,100));
        //middleSectionPanel.add(diseaseMenuAndTablePanel,BorderLayout.WEST);
        JPanel agentMenuAndTablePanel = new JPanel();
        agentMenuAndTablePanel.setLayout(new BorderLayout());
        agentMenuAndTablePanel.add(rightHeadPanel, BorderLayout.NORTH);
        agentMenuAndTablePanel.add(jScrollPane2, BorderLayout.CENTER);        
        agentMenuAndTablePanel.setMinimumSize(new Dimension (100,100));
        //middleSectionPanel.add(agentMenuAndTablePanel,BorderLayout.EAST);
        JSplitPane middleSectionPanel = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, diseaseMenuAndTablePanel,
				agentMenuAndTablePanel);
        middleSectionPanel.setResizeWeight(0.5);
        middleSectionPanel.setOneTouchExpandable(true);
        //annotationsPanel.add(jScrollPane1, BorderLayout.WEST);
        //annotationsPanel.add(jScrollPane2, BorderLayout.EAST);
        //This panel using BorderLayout to give text field the ability to fill the window.
        JPanel textFieldPanel = new JPanel();
        textFieldPanel.setLayout(new BorderLayout());
    	textArea.setLineWrap(true);
    	//This panel using JScrollPane to give text field a scroll bar when needed.
        JScrollPane textScrollPanel = new JScrollPane(textArea);
        textFieldPanel.setPreferredSize(new Dimension(100,60));
        textFieldPanel.add(textScrollPanel,BorderLayout.CENTER);
        JPanel cgiBottomPanel = new JPanel();
        cgiBottomPanel.setPreferredSize(new Dimension(100,60));
        cgiBottomPanel.setLayout(new BorderLayout());
        cgiBottomPanel.add(textFieldPanel, BorderLayout.CENTER);
        annoButtonPanel.add(annoRetrieveCaBioCheckBox);
        annoButtonPanel.add(annoRetrievePathwayCheckBox);
        annoButtonPanel.add(annoRetrieveButton);
        annoButtonPanel.add(annoClearButton);
        cgiButtonPanel.add(cgiRetrieveCaBioCheckBox);
        cgiButtonPanel.add(cgiRetrievePathwayCheckBox);
        cgiButtonPanel.add(cgiRetrieveButton);
        cgiButtonPanel.add(cgiClearButton);
        annotationModel = new AnnotationTableModel();
        annotationTable = new SortableTable(annotationModel);
        annotationPanel.setLayout(new BorderLayout());
        annotationPanel.add(new JScrollPane(annotationTable),BorderLayout.CENTER);
        cgiPanel = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT, middleSectionPanel,
				cgiBottomPanel);
        cgiPanel.setResizeWeight(1.0);
        annotationPanel.add(annoButtonPanel, BorderLayout.SOUTH);
        cgiBottomPanel.add(cgiButtonPanel, BorderLayout.SOUTH);
//        annotationsPanel.setLayout(borderLayout1);
//        annotationsPanel.add(middleSectionPanel,BorderLayout.CENTER);
//        annotationsPanel.add(buttomPanel, BorderLayout.SOUTH);
        jTabbedPane1.add("Annotations", annotationPanel);
        jTabbedPane1.add("Pathway", pathwayPanel);
        jTabbedPane1.add("CancerGeneIndex", cgiPanel);
        jbInitPathways();       
        
        diseaseModel = new CGITableModel();
        diseaseTable = new SortableTable(diseaseModel){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			// FIXME: we have bugs here, and have potential to generate error.
			// When we load new data into the table model, there's a period of
			// time that we'll have multithread problem.
			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex,
						vColIndex);
				if (c instanceof JComponent) {
					JComponent jc = (JComponent) c;
					if (vColIndex == 4){
						jc.setToolTipText("<html><table width='300'><tr><td>"+(String) getValueAt(rowIndex, vColIndex)+"</td></tr></table></html>");
					}else if (vColIndex == 0){
						if ((diseaseModel.getMarkerAt(rowIndex,vColIndex)!=null)&&(!diseaseModel.getMarkerAt(rowIndex,vColIndex).numOutOfNum.equals(""))){
							jc.setFont(new Font(jc.getFont().getFontName(), Font.BOLD, jc.getFont().getSize()));
							if (getValueAt(rowIndex, vColIndex) == null)
								log.error("null 1");
							if (diseaseModel.getMarkerAt(rowIndex,vColIndex)==null)
								log.error("null 2");
							try{
								jc.setToolTipText((String) getValueAt(rowIndex, vColIndex) + "("+diseaseModel.getMarkerAt(rowIndex,vColIndex).numOutOfNum+")");
							}catch (Exception e){
								log.error("Multithread?",e);
							}
						}else{
							jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
						}
					}else{
						jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
						//jc.setToolTipText(null);
					}
				}
				return c;
			}
        };
        
        TableColumnModelListener tableColumnModelListener = new TableColumnModelListener() {
			public void columnAdded(TableColumnModelEvent e) {
				log.debug("Added");
			}

			public void columnMarginChanged(ChangeEvent e) {
				log.debug("Margin");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                    	syncHeaderWidthWithTableWidth();
                    }
                });
			}

			public void columnMoved(TableColumnModelEvent e) {
				log.debug("Moved");
			}

			public void columnRemoved(TableColumnModelEvent e) {
				log.debug("Removed");
			}

			public void columnSelectionChanged(ListSelectionEvent e) {
				log.debug("Selection Changed");
			}
		};
        diseaseTable.getColumnModel().addColumnModelListener(tableColumnModelListener);
        diseaseTable.getColumnModel().getColumn(0).setHeaderValue("Marker");
        diseaseTable.getColumnModel().getColumn(1).setHeaderValue("Gene");
        diseaseTable.getColumnModel().getColumn(2).setHeaderValue("Disease");
        diseaseTable.getColumnModel().getColumn(3).setHeaderValue("Role");
        diseaseTable.getColumnModel().getColumn(4).setHeaderValue("Sentence");
        diseaseTable.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
        diseaseTable.setCellSelectionEnabled(false);
        diseaseTable.setRowSelectionAllowed(false);
        diseaseTable.setColumnSelectionAllowed(false);
        diseaseTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	selectedTable = diseaseTable;
                int column = diseaseTable.columnAtPoint(e.getPoint());
                int row = diseaseTable.rowAtPoint(e.getPoint());
                if (e.getButton() == MouseEvent.BUTTON1){
	                if ((column >= 0) && (row >= 0)) {
	                    //model.activateCell(row, column);
	                }
	                if (column == COL_MARKER){
	                    diseaseModel.activateCell(row, column);
	                }
	                if ((column == COL_GENE)) {
	                    String value = (String) diseaseTable.getValueAt(row, column);
	                    value=unwrapFromHTML(value);
	                    String address = GeneCards_PREFIX + value;
	                    try {
							BrowserLauncher.openURL(address);
						} catch (IOException e1) {
							log.error(e1,e1);
						}
	                }
//	                if ((column == COL_DISEASE)) {
//	                    String address = EVS_PREFIX + table.getModel().getValueAt(row, COL_EVSID);
//	                    try {
//							BrowserLauncher.openURL(address);
//						} catch (IOException e1) {
//	                		log.error(e1,e1);
//						}
//	                }
	                if ((column == COL_PUBMED)) {
	                    String value = (String) diseaseTable.getValueAt(row, column);
	                    value=unwrapFromHTML(value);
	                    String address = PUBMED_PREFIX + value;
	                    try {
							BrowserLauncher.openURL(address);
						} catch (IOException e1) {
							log.error(e1,e1);
						}
	                }
	                if ((column == COL_SENTENCE)) {
	                	String value = (String) diseaseTable.getValueAt(row, column);
	                	textArea.setText(value);
	                }
                }else{
                	selectedTable = diseaseTable;
                	selectedMarker = ((CGITableModel)diseaseTable.getModel()).getMarkerAt(row,0).marker;
                	selectedRow = row;
                	selectedGene = ((GeneData)(((CGITableModel)diseaseTable.getModel()).getObjectAt(row, COL_GENE))).name;
                	selectedDisease = ((DiseaseData)(((CGITableModel)diseaseTable.getModel()).getObjectAt(row, COL_DISEASE))).name;
                	expandCollapseRetrieveAllMenu.show(diseaseTable, e.getX(), e.getY());
                }
            }
        });
        diseaseTable.addMouseMotionListener(new MouseMotionAdapter() {
            private boolean isHand = false;

            public void mouseMoved(MouseEvent e) {
                int column = diseaseTable.columnAtPoint(e.getPoint());
                int row = diseaseTable.rowAtPoint(e.getPoint());
                if ((column >= 0) && (row >= 0)) {
                    if ((column == COL_GENE) 
//                    		|| (column == COL_DISEASE)
							|| (column == COL_PUBMED)) {
                        if (!isHand) {
                            isHand = true;
                            diseaseTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }
                    } else {
                        if (isHand) {
                            isHand = false;
                            diseaseTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                }
            }
        });

        annotationTableList = new HashMap<Integer, SortableTableModel>();

        agentModel = new CGITableModel();
        agentTable = new SortableTable(agentModel) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex,
						vColIndex);
				if (c instanceof JComponent) {
					JComponent jc = (JComponent) c;
					jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
					if (vColIndex == 4){
						jc.setToolTipText("<html><table width='300'><tr><td>"+(String) getValueAt(rowIndex, vColIndex)+"</td></tr></table></html>");
					}else if (vColIndex == 0){
						if (agentModel.getMarkerAt(rowIndex,vColIndex)==null){
							//filtered row
						}
						if (!agentModel.getMarkerAt(rowIndex,vColIndex).numOutOfNum.equals("")){
							jc.setFont(new Font(jc.getFont().getFontName(), Font.BOLD, jc.getFont().getSize()));
							jc.setToolTipText((String) getValueAt(rowIndex, vColIndex) + "("+agentModel.getMarkerAt(rowIndex,vColIndex).numOutOfNum+")");
						}else{
							jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
						}
					}else{
						jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
						//jc.setToolTipText(null);
					}
				}
				return c;
			}
        };
        agentTable.getColumnModel().addColumnModelListener(tableColumnModelListener);
        agentTable.getColumnModel().getColumn(0).setHeaderValue("Marker");
        agentTable.getColumnModel().getColumn(1).setHeaderValue("Gene");
        agentTable.getColumnModel().getColumn(2).setHeaderValue("Agent");
        agentTable.getColumnModel().getColumn(3).setHeaderValue("Role");
        agentTable.getColumnModel().getColumn(4).setHeaderValue("Sentence");
        agentTable.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
        agentTable.setCellSelectionEnabled(false);
        agentTable.setRowSelectionAllowed(false);
        agentTable.setColumnSelectionAllowed(false);
        agentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	selectedTable = agentTable;
                int column = agentTable.columnAtPoint(e.getPoint());
                int row = agentTable.rowAtPoint(e.getPoint());
                if (e.getButton() == MouseEvent.BUTTON1){
	                if ((column >= 0) && (row >= 0)) {
	                    //model2.activateCell(row, column);
	                }
	                if (column == COL_MARKER){
	                    agentModel.activateCell(row, column);
	                }                
	                if ((column == COL_GENE)) {
	                    String value = (String) agentTable.getValueAt(row, column);
	                    value=unwrapFromHTML(value);
	                    String address = GeneCards_PREFIX + value;
	                    try {
							BrowserLauncher.openURL(address);
						} catch (IOException e1) {
							log.error(e1,e1);
						}
	                }
//	                if ((column == COL_AGENT)) {
//	                    String address = EVS_PREFIX + table2.getModel().getValueAt(row, COL_EVSID);
//	                    try {
//							BrowserLauncher.openURL(address);
//						} catch (IOException e1) {
//							log.error(e1,e1);
//						}
//	                }
	                if ((column == COL_PUBMED)) {
	                    String value = (String) agentTable.getValueAt(row, column);
	                    value=unwrapFromHTML(value);
	                    String address = "http://www.ncbi.nlm.nih.gov/pubmed/"+value;
	                    try {
							BrowserLauncher.openURL(address);
						} catch (IOException e1) {
							log.error(e1,e1);
						}
	                }
	                if ((column == COL_SENTENCE)) {
	                	String value = (String) agentTable.getValueAt(row, column);
	                	textArea.setText(value);
	                }
                }else{
                	selectedTable = agentTable;
                	selectedMarker = ((CGITableModel)agentTable.getModel()).getMarkerAt(row,0).marker;
                	selectedRow = row;
                	selectedGene = ((GeneData)(((CGITableModel)agentTable.getModel()).getObjectAt(row, COL_GENE))).name;
                	selectedDisease = ((DiseaseData)(((CGITableModel)agentTable.getModel()).getObjectAt(row, COL_DISEASE))).name;
                	expandCollapseRetrieveAllMenu.show(agentTable, e.getX(), e.getY());
                }
            }
        });
        agentTable.addMouseMotionListener(new MouseMotionAdapter() {
            private boolean isHand = false;

            public void mouseMoved(MouseEvent e) {
                int column = agentTable.columnAtPoint(e.getPoint());
                int row = agentTable.rowAtPoint(e.getPoint());
                if ((column >= 0) && (row >= 0)) {
                    if ((column == COL_GENE) 
//                    		|| (column == COL_AGENT)
							|| (column == COL_PUBMED)) {
                        if (!isHand) {
                            isHand = true;
                            agentTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }
                    } else {
                        if (isHand) {
                            isHand = false;
                            agentTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                }
            }
        });
        annotationTable.getColumnModel().getColumn(0).setHeaderValue("Marker");
        annotationTable.getColumnModel().getColumn(1).setHeaderValue("Gene");
        annotationTable.getColumnModel().getColumn(2).setHeaderValue("Pathway");
        annotationTable.setCellSelectionEnabled(false);
        annotationTable.setRowSelectionAllowed(false);
        annotationTable.setColumnSelectionAllowed(false);
        annotationTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int column = annotationTable.columnAtPoint(e.getPoint());
                int row = annotationTable.rowAtPoint(e.getPoint());
                if ((column >= 0) && (row >= 0)) {
                	annotationModel.activateCell(row, column);
                }
            }
        });
        annotationTable.addMouseMotionListener(new MouseMotionAdapter() {
            private boolean isHand = false;

            public void mouseMoved(MouseEvent e) {
                int column = annotationTable.columnAtPoint(e.getPoint());
                int row = annotationTable.rowAtPoint(e.getPoint());
                if ((column >= 0) && (row >= 0)) {
                    if ((column == AnnotationTableModel.COL_GENE) || (column == AnnotationTableModel.COL_PATHWAY)) {
                        if (!isHand) {
                            isHand = true;
                            annotationTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }
                    } else {
                        if (isHand) {
                            isHand = false;
                            annotationTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                }
            }
        });

		class MyComboBoxRenderer extends BasicComboBoxRenderer {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (isSelected) {
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
					if (value!=null)
						list.setToolTipText(value.toString());
				} else {
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}
				setFont(list.getFont());
				setText((value == null) ? "" : value.toString());
				return this;
			}
		}
        String[] tooltips = { "Filter by Marker", "Filter by Gene", "Filter by Disease", "Filter by Role",
        		"Filter by Marker", "Filter by Gene", "Filter by Agent", "Filter by Role"};
        for (int i = 0; i < dropDownLists.length; i++) {
            // TODO: modify JComboBox to use data model (now we directly access items in it).
        	dropDownLists[i]=new JComboBox();
        	dropDownLists[i].setRenderer(new MyComboBoxRenderer());
        	dropDownLists[i].setToolTipText(tooltips[i]);
		}
        class ItemFilterListener implements ItemListener{
			int filterIndex = 0;
			public ItemFilterListener(int i){
				super();
				filterIndex = i;
			}
			public void itemStateChanged(ItemEvent ie) {
				String itemStr = (String) ie.getItem();
				//remove number after it
				String[] strs = itemStr.split(" \\(\\d+\\)");
				String str = strs[0];
				log.debug("itemStateChanged, column:"+filterIndex+"str:"+str);
				if (filterIndex<4){
					((CGITableModel) diseaseTable.getModel()).filterBy(filterIndex, str);
					diseaseTable.revalidate();
					diseaseTable.repaint();
				}else{
					((CGITableModel) agentTable.getModel()).filterBy(filterIndex-4, str);
					agentTable.revalidate();
					diseaseTable.repaint();
				}
			}
        }
        
        for (int i = 0; i < dropDownLists.length; i++) {
			dropDownLists[i].addItem("");
			dropDownLists[i].addItemListener(new ItemFilterListener(i));
		}
        
        leftHeadPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        leftHeadPanel.add(dropDownLists[0]);
        leftHeadPanel.add(dropDownLists[1]);
        leftHeadPanel.add(dropDownLists[2]);
        leftHeadPanel.add(dropDownLists[3]);
        rightHeadPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        rightHeadPanel.add(dropDownLists[4]);
        rightHeadPanel.add(dropDownLists[5]);
        rightHeadPanel.add(dropDownLists[6]);
        rightHeadPanel.add(dropDownLists[7]);
        jScrollPane1.getViewport().add(diseaseTable, null);
        jScrollPane2.getViewport().add(agentTable, null);
        leftHeadPanel.setVisible(false);
        rightHeadPanel.setVisible(false);
        
        class ExpandActionListener implements ActionListener {
			public void actionPerformed(ActionEvent actionEvent) {
				if (selectedTable==diseaseTable)
					((CGITableModel)diseaseTable.getModel())._expandBy(selectedTable, selectedGene, selectedDisease);
				else
					((CGITableModel)agentTable.getModel())._expandBy(selectedTable, selectedGene, selectedDisease);
			}
		}
        ActionListener actionListener = new ExpandActionListener();
        JMenuItem expandItem = new JMenuItem("expand");
        expandItem.addActionListener(actionListener);
        expandCollapseRetrieveAllMenu.add(expandItem);

        class CollapseActionListener implements ActionListener {
			public void actionPerformed(ActionEvent actionEvent) {
				((CGITableModel)selectedTable.getModel()).collapseBy(selectedTable, selectedGene, selectedDisease);
				((SortableTableModel)selectedTable.getModel()).sortByColumn(2,true);
				((SortableTableModel)selectedTable.getModel()).sortByColumn(0,true);
			}
		}
        ActionListener collapseActionListener = new CollapseActionListener();
        JMenuItem collapseItem = new JMenuItem("collapse");
        collapseItem.addActionListener(collapseActionListener);
        expandCollapseRetrieveAllMenu.add(collapseItem);
        
        class RetrieveActionListener implements ActionListener {
			public void actionPerformed(ActionEvent actionEvent) {
				log.debug("Selected: "
						+ actionEvent.getActionCommand());
				retrieveAll(selectedMarker);
			}
		}
        ActionListener retrieveActionListener = new RetrieveActionListener();
        JMenuItem retrieveItem = new JMenuItem("retrieve all");
        retrieveItem.addActionListener(retrieveActionListener);
        expandCollapseRetrieveAllMenu.add(retrieveItem);

        class NCIActionListener implements ActionListener {
			public void actionPerformed(ActionEvent actionEvent) {
				String address = "";
				if (selectedTable == diseaseTable) {
					address = EVS_PREFIX
							+ diseaseTable.getModel().getValueAt(selectedRow,
									COL_EVSID);
				} else {
					address = EVS_PREFIX
							+ agentTable.getModel().getValueAt(selectedRow,
									COL_EVSID);
				}
				try {
					BrowserLauncher.openURL(address);
				} catch (Exception e) {
					log.error(e, e);
				}
			}
		}
		NCIActionListener nciActionListener = new NCIActionListener();
		JMenuItem evsItem = new JMenuItem("Link to NCI_Thesaurus");
		evsItem.addActionListener(nciActionListener);
		expandCollapseRetrieveAllMenu.add(evsItem);

        class CollapseAllActionListener implements ActionListener {
			public void actionPerformed(ActionEvent actionEvent) {
				for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
					((CGITableModel) selectedTable.getModel()).collapseBy(selectedTable,
							((GeneData) (((CGITableModel) selectedTable
									.getModel()).getObject(j,
									COL_GENE))).name,
							((DiseaseData) (((CGITableModel) selectedTable
									.getModel()).getObject(j,
									COL_DISEASE))).name);                            	
	            }
//				sortByNumberOfRecords = true;				
				((SortableTableModel)selectedTable.getModel()).sortByColumn(0,true);				
			}
		}
        ActionListener collapseAllActionListener = new CollapseAllActionListener();
        JMenuItem collapseAllItem = new JMenuItem("collapse all");
        collapseAllItem.addActionListener(collapseAllActionListener);
        expandCollapseRetrieveAllMenu.add(collapseAllItem);

        class ExpandAllActionListener implements ActionListener {
			public void actionPerformed(ActionEvent actionEvent) {
	            for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
					((CGITableModel) selectedTable.getModel())._expandBy(selectedTable,
							((GeneData) (((CGITableModel) selectedTable
									.getModel()).getObject(j,
									COL_GENE))).name,
							((DiseaseData) (((CGITableModel) selectedTable
									.getModel()).getObject(j,
									COL_DISEASE))).name);                            	
	            }
			}
		}
        ActionListener expandAllActionListener = new ExpandAllActionListener();
        JMenuItem expandAllItem = new JMenuItem("expand all");
        expandAllItem.addActionListener(expandAllActionListener);
        expandCollapseRetrieveAllMenu.add(expandAllItem);

    }
    boolean entered = false;
    private void syncHeaderWidthWithTableWidth(){
    	if (entered) return;
    	else{
    		entered = true;
    		try{
//				leftHeadPanel.setPreferredSize(new Dimension(jScrollPane1.getSize().width,dropDownLists[0].getHeight()));
//				rightHeadPanel.setPreferredSize(new Dimension(jScrollPane2.getSize().width,dropDownLists[4].getHeight()));
				dropDownLists[0].setPreferredSize(new Dimension(diseaseTable.getColumnModel().getColumn(0).getWidth(),dropDownLists[0].getHeight()));
				dropDownLists[0].revalidate();
				dropDownLists[1].setPreferredSize(new Dimension(diseaseTable.getColumnModel().getColumn(1).getWidth(),dropDownLists[1].getHeight()));
				dropDownLists[1].revalidate();
				dropDownLists[2].setPreferredSize(new Dimension(diseaseTable.getColumnModel().getColumn(2).getWidth(),dropDownLists[2].getHeight()));
				dropDownLists[2].revalidate();
				dropDownLists[3].setPreferredSize(new Dimension(diseaseTable.getColumnModel().getColumn(3).getWidth(),dropDownLists[3].getHeight()));
				dropDownLists[3].revalidate();
				dropDownLists[4].setPreferredSize(new Dimension(agentTable.getColumnModel().getColumn(0).getWidth(),dropDownLists[4].getHeight()));
				dropDownLists[4].revalidate();
				dropDownLists[5].setPreferredSize(new Dimension(agentTable.getColumnModel().getColumn(1).getWidth(),dropDownLists[5].getHeight()));
				dropDownLists[5].revalidate();
				dropDownLists[6].setPreferredSize(new Dimension(agentTable.getColumnModel().getColumn(2).getWidth(),dropDownLists[6].getHeight()));
				dropDownLists[6].revalidate();
				dropDownLists[7].setPreferredSize(new Dimension(agentTable.getColumnModel().getColumn(3).getWidth(),dropDownLists[7].getHeight()));
				dropDownLists[7].revalidate();
				leftHeadPanel.revalidate();
				rightHeadPanel.revalidate();
    		}finally{
    			entered = false;
    		}
    	}
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

    /*
     * 
     */
    private void retrieveAll(DSGeneMarker marker){
    	retrieveMarkerInfo.clear();
        DSGeneMarker retrieveMarker = marker;
        retrieveMarkerInfo.add(retrieveMarker);
        
        try {
            Runnable query = new Runnable() {
            	
            	public void run() {
//            		dropDownLists[2].removeAllItems();
//            		dropDownLists[2].addItem("");
            		ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                    pb.addObserver(getAnnotationsPanel());
                    pb.setMessage("Connecting to server...");
                    ArrayList<MarkerData> markerData = new ArrayList<MarkerData>();
                    ArrayList<GeneData> geneData = new ArrayList<GeneData>();
                    ArrayList<DiseaseData> diseaseData = new ArrayList<DiseaseData>();
                    ArrayList<RoleData> roleData = new ArrayList<RoleData>();
                    ArrayList<SentenceData> sentenceData = new ArrayList<SentenceData>();
                    ArrayList<PubmedData> pubmedData = new ArrayList<PubmedData>();
                    ArrayList<MarkerData> markerData2 = new ArrayList<MarkerData>();
                    ArrayList<GeneData> geneData2 = new ArrayList<GeneData>();
                    ArrayList<EvsIdData> evsIdData = new ArrayList<EvsIdData>();
                    ArrayList<DiseaseData> agentData = new ArrayList<DiseaseData>();
                    ArrayList<RoleData> agentRoleData = new ArrayList<RoleData>();
                    ArrayList<SentenceData> agentSentenceData = new ArrayList<SentenceData>();
                    ArrayList<PubmedData> agentPubmedData = new ArrayList<PubmedData>();
                    if (retrieveMarkerInfo != null) {
                        pb.setTitle("Querying caBIO..");
                        pb.start();           

                		ApplicationService appService = null;
                		try {
                			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                			appService = ApplicationServiceProvider.getApplicationService();
                		} catch (Exception e) {
                			log.error(e,e);
            				JOptionPane
    						.showConfirmDialog(
    								null,
    								"Please try again later",
    								"Server side error", JOptionPane.OK_OPTION);
                			pb.stop();
                			return;
                		}

                        int index = 0;
                        //TODO: to save network communication time, we should query only once by submitting the list.                        
                        for (int i = 0; i < retrieveMarkerInfo.size(); i++) {
                            index++;
                            String progressMessage = "";
                            progressMessage += "Marker "+index+"/"+retrieveMarkerInfo.size()+"   ";
							if (stopAlgorithm == true) {
								stopAlgorithm(pb);
								break;
							}
                            
                            String geneName = retrieveMarkerInfo.get(i).getGeneName();
                            
                    		String geneSymbol = geneName;
                    		//int uniGeneId = marker.getUnigene().getUnigeneId();
                    		Gene gene = new Gene();
                    		gene.setSymbol(geneSymbol);

                    		List<Object> results2 = null;
                    		try {
                    			results2 = appService.search(GeneFunctionAssociation.class, gene);
                    		} catch (Exception e) {
                    			log.error(e,e);
                    			JOptionPane.showMessageDialog(null, "Please try again later",
                    					"Server side error", JOptionPane.ERROR_MESSAGE);
                    			pb.stop();
                    			return;
                    		}

                    		log.debug("\nDisease associated with Gene: " + geneSymbol);
                    		int diseaseIndex = 0;
                    		int agentIndex = 0;
                    		int diseaseRecords=0;
                    		int agentRecords=0;
                    		if (results2!=null)
                    		for (Object gfa : results2) {
                    			if (gfa instanceof GeneDiseaseAssociation) {
                    				diseaseRecords++;
                    			}                    		
                    			else if (gfa instanceof GeneAgentAssociation) {
                    				agentRecords++;
                    			}
                    		}
                    		String numOutOfNumDisease = "";
                    		String numOutOfNumAgent = "";
//                    		if (diseaseRecords>diseaseLimitIndex){
//                    			numOutOfNumDisease = diseaseLimitIndex+" out of "+diseaseRecords;
//                    		}
//                    		if (agentRecords>agentLimitIndex){
//                    			numOutOfNumAgent = agentLimitIndex+" out of "+agentRecords;
//                    		}
                    		if (results2!=null)
                    		for (Object gfa : results2) {
                                pb.setMessage(progressMessage+"Records "+(diseaseIndex+agentIndex)+"/"+(diseaseRecords+agentRecords)+"\n");
								if (stopAlgorithm == true) {
									stopAlgorithm(pb);
									break;
								}
                    			if (gfa instanceof GeneDiseaseAssociation) {
                    				diseaseIndex++;                    				
                    				GeneDiseaseAssociation gda = (GeneDiseaseAssociation) gfa;
                    				Evidence e = gda.getEvidence();                    				
                    				MarkerData markerDataNew = new MarkerData(retrieveMarkerInfo.get(i),numOutOfNumDisease);
                    				GeneData geneDataNew = new GeneData(gda.getGene().getSymbol(),gda.getGene());
                    				DiseaseData diseaseDataNew = new DiseaseData(gda.getDiseaseOntology().getName(),gda.getDiseaseOntology(), gda.getDiseaseOntology().getEVSId());
                    				RoleData roleDataNew = new RoleData(gda.getRole());
                    				SentenceData sentenceDataNew = new SentenceData(e.getSentence());
                    				PubmedData pubmedDataNew = new PubmedData(Integer.toString(e.getPubmedId()));
                    				if (!diseaseModel.containsRecord(markerDataNew, geneDataNew, diseaseDataNew, roleDataNew, sentenceDataNew, pubmedDataNew)){
	                    				markerData.add(new MarkerData(retrieveMarkerInfo.get(i),numOutOfNumDisease));
	                    				geneData.add(new GeneData(gda.getGene().getSymbol(),gda.getGene()));
	                    				log.debug("  Disease: " + gda.getDiseaseOntology().getName());
	                    				diseaseData.add(new DiseaseData(gda.getDiseaseOntology().getName(),gda.getDiseaseOntology(), gda.getDiseaseOntology().getEVSId()));
	                    				log.debug("    Role: " + gda.getRole());
	                    				log.debug("    Sentence: "+e.getSentence());
	                    				log.debug("    PubmedId:"+e.getPubmedId());
	                    				roleData.add(new RoleData(gda.getRole()));
	                    				sentenceData.add(new SentenceData(e.getSentence()));
	                    				pubmedData.add(new PubmedData(Integer.toString(e.getPubmedId())));
                    				}
                    			}
                    			else if (gfa instanceof GeneAgentAssociation) {
                    				diseaseIndex++;                    				
                    				GeneAgentAssociation gaa = (GeneAgentAssociation) gfa;
                    				Evidence e = gaa.getEvidence();
                    				MarkerData markerDataNew = new MarkerData(retrieveMarkerInfo.get(i),numOutOfNumAgent);
                    				GeneData geneDataNew = new GeneData(gaa.getGene().getSymbol(),gaa.getGene());
                    				DiseaseData diseaseDataNew = new DiseaseData(gaa.getAgent().getName(),null, gaa.getAgent().getEVSId());
                    				RoleData roleDataNew = new RoleData(gaa.getRole());
                    				SentenceData sentenceDataNew = new SentenceData(e.getSentence());
                    				PubmedData pubmedDataNew = new PubmedData(Integer.toString(e.getPubmedId()));
                    				if (!agentModel.containsRecord(markerDataNew, geneDataNew, diseaseDataNew, roleDataNew, sentenceDataNew, pubmedDataNew)){
	                    				markerData2.add(new MarkerData(retrieveMarkerInfo.get(i),numOutOfNumAgent));
	                    				geneData2.add(new GeneData(gaa.getGene().getSymbol(),gaa.getGene()));
	                    				evsIdData.add(new EvsIdData(gaa.getAgent().getEVSId()));
	                    				agentData.add(new DiseaseData(gaa.getAgent().getName(),null, gaa.getAgent().getEVSId()));
	                    				log.debug("  Id: " + gaa.getId());
	                    				log.debug("  Role: " + gaa.getRole());
	                    				log.debug("  EvsId: " + gaa.getAgent().getEVSId());
	                    				log.debug("  Name: " + gaa.getAgent().getName());
	                    				log.debug("    Sentence: "+e.getSentence());
	                    				log.debug("    PubmedId:"+e.getPubmedId());
	                    				agentRoleData.add(new RoleData(gaa.getRole()));
	                    				agentSentenceData.add(new SentenceData(e.getSentence()));
	                    				agentPubmedData.add(new PubmedData(Integer.toString(e.getPubmedId())));
                    				}
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
                    MarkerData[] markers2 = markerData2.toArray(new MarkerData[0]);
                    GeneData[] genes2 = geneData2.toArray(new GeneData[0]);
                    //TODO: clean variables for this evsIds, since we handle evsIds in agentData now. 
                    EvsIdData[] evsIds = evsIdData.toArray(new EvsIdData[0]);
                    DiseaseData[] agents = agentData.toArray(new DiseaseData[0]);
                    RoleData[] agentRoles = agentRoleData.toArray(new RoleData[0]);
                    SentenceData[] agentSentences = agentSentenceData.toArray(new SentenceData[0]);
                    PubmedData[] agentPubmeds = agentPubmedData.toArray(new PubmedData[0]);
					// FIXME: remove number appended on the list, then do these,
					// follow by recalculate the number and append to the list.
                    for (int j = 0; j < diseases.length; j++) {
                    	//removeItem() followed by addItem() will produce a non-duplicate list
        				dropDownLists[0].removeItem(markers[j].name);
        				dropDownLists[0].addItem(markers[j].name);						
        				dropDownLists[1].removeItem(genes[j].name);
        				dropDownLists[1].addItem(genes[j].name);						
        				dropDownLists[2].removeItem(diseases[j].name);
        				dropDownLists[2].addItem(diseases[j].name);
        				dropDownLists[3].removeItem(roles[j].role);
        				dropDownLists[3].addItem(roles[j].role);
					}
                    for (int j = 0; j < agents.length; j++) {
        				dropDownLists[4].removeItem(markers2[j].name);
        				dropDownLists[4].addItem(markers2[j].name);						
        				dropDownLists[5].removeItem(genes2[j].name);
        				dropDownLists[5].addItem(genes2[j].name);						
        				dropDownLists[6].removeItem(agents[j].name);
        				dropDownLists[6].addItem(agents[j].name);
        				dropDownLists[7].removeItem(agentRoles[j].role);
        				dropDownLists[7].addItem(agentRoles[j].role);
					}
                    //since we'll use updateDiseaseNumber(), no need to do it now.
//                    appendDiseaseNumber(markers,diseases,dropDownLists[2]);
//                    appendDiseaseNumber(markers2,agents,dropDownLists[6]);

                    diseaseTable.setVisible(false);
                    agentTable.setVisible(false);
                    diseaseModel.insertData(diseaseModel.size, markers, genes, diseases, roles, sentences, pubmeds);
                    diseaseModel.filterBy(0, "");
                    diseaseModel.sortByColumn(0,true);
                    diseaseTable.revalidate();
                    agentModel.insertData(agentModel.size, markers2, genes2, agents, agentRoles, agentSentences, agentPubmeds);
                    agentModel.filterBy(0, "");
                    agentModel.sortByColumn(0,true);
                    agentTable.revalidate();
                    diseaseTable.setVisible(true);
                    agentTable.setVisible(true);
                    sortByNumberOfRecords = true;
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            diseaseTable.getTableHeader().repaint();
                            agentTable.getTableHeader().repaint();
                            leftHeadPanel.setVisible(true);
                            leftHeadPanel.revalidate();
                            rightHeadPanel.setVisible(true);
                            rightHeadPanel.revalidate();                            
                            syncHeaderWidthWithTableWidth();
                            updateDiseaseNumber();
                            orderDropDownLists(dropDownLists[2]);
                            orderDropDownLists(dropDownLists[6]);
                        }
                    });
                }
            	private void orderDropDownLists(JComboBox dropDownLists){
                	int itemCount = dropDownLists.getItemCount();
                	String[] array = new String[itemCount];
                	for (int i = 0; i < itemCount; i++) {
                		array[i]=(String)dropDownLists.getItemAt(i);
            		}
                	Arrays.sort(array);
                	dropDownLists.removeAllItems();
                	for (int i = 0; i < itemCount; i++) {
                		dropDownLists.addItem(array[i]);
            		}    	
                }
            };
            Thread t = new Thread(query);
            t.setPriority(Thread.MIN_PRIORITY);
           	t.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        for (int i = 0; i < diseaseModel.markerData.length; i++) {
			MarkerData markerData = diseaseModel.markerData[i];
			if (retrieveMarker.getLabel().equals(markerData.name))
				markerData.numOutOfNum="";
		}
        for (int i = 0; i < agentModel.markerData.length; i++) {
			MarkerData markerData = agentModel.markerData[i];
			if (retrieveMarker.getLabel().equals(markerData.name))
				markerData.numOutOfNum="";
		}
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
            	
            	public void run() {
            		dropDownLists[2].removeAllItems();
            		dropDownLists[2].addItem("");
            		ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                    pb.addObserver(getAnnotationsPanel());
                    pb.setMessage("Connecting to server...");
                    ArrayList<MarkerData> markerData = new ArrayList<MarkerData>();
                    ArrayList<GeneData> geneData = new ArrayList<GeneData>();
                    ArrayList<DiseaseData> diseaseData = new ArrayList<DiseaseData>();
                    ArrayList<RoleData> roleData = new ArrayList<RoleData>();
                    ArrayList<SentenceData> sentenceData = new ArrayList<SentenceData>();
                    ArrayList<PubmedData> pubmedData = new ArrayList<PubmedData>();
                    ArrayList<MarkerData> markerData2 = new ArrayList<MarkerData>();
                    ArrayList<GeneData> geneData2 = new ArrayList<GeneData>();
                    ArrayList<EvsIdData> evsIdData = new ArrayList<EvsIdData>();
                    ArrayList<DiseaseData> agentData = new ArrayList<DiseaseData>();
                    ArrayList<RoleData> agentRoleData = new ArrayList<RoleData>();
                    ArrayList<SentenceData> agentSentenceData = new ArrayList<SentenceData>();
                    ArrayList<PubmedData> agentPubmedData = new ArrayList<PubmedData>();
                    if (selectedMarkerInfo != null) {
                        pb.setTitle("Querying caBIO..");
                        pb.start();           

                		ApplicationService appService = null;
                		try {
                			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                			appService = ApplicationServiceProvider.getApplicationService();
                		} catch (Exception e) {
                			log.error(e,e);
            				JOptionPane
    						.showConfirmDialog(
    								null,
    								"Please try again later",
    								"Server side error", JOptionPane.OK_OPTION);
                			pb.stop();
                			return;
                		}

                        int index = 0;
                        //TODO: to save network communication time, we should query only once by submitting the list.                        
                        for (int i = 0; i < selectedMarkerInfo.size(); i++) {
                            index++;
                            String progressMessage = "";
                            progressMessage += "Marker "+index+"/"+selectedMarkerInfo.size()+"   ";
							if (stopAlgorithm == true) {
								stopAlgorithm(pb);
								break;
							}
                            
                            String geneName = selectedMarkerInfo.get(i).getGeneName();
                            
                    		String geneSymbol = geneName;
                    		//int uniGeneId = marker.getUnigene().getUnigeneId();
                    		Gene gene = new Gene();
                    		gene.setSymbol(geneSymbol);

                    		List<Object> results2 = null;
                    		try {
                    			results2 = appService.search(GeneFunctionAssociation.class, gene);
                    		} catch (Exception e) {
                    			log.error(e,e);
                    			JOptionPane.showMessageDialog(null, "Please try again later",
                    					"Server side error", JOptionPane.ERROR_MESSAGE);
                    			pb.stop();
                    			return;
                    		}

                    		log.debug("\nDisease associated with Gene: " + geneSymbol);
                    		final int diseaseLimit = 10;
                    		final int agentLimit = 10;
                    		int diseaseLimitIndex = diseaseLimit;
                    		int agentLimitIndex = agentLimit;
                    		int diseaseRecords=0;
                    		int agentRecords=0;
                    		if (results2!=null)
                    		for (Object gfa : results2) {
                    			if (gfa instanceof GeneDiseaseAssociation) {
                    				diseaseRecords++;
                    			}                    		
                    			else if (gfa instanceof GeneAgentAssociation) {
                    				agentRecords++;
                    			}
                    		}
                    		String numOutOfNumDisease = "";
                    		String numOutOfNumAgent = "";
                    		if (diseaseRecords>diseaseLimitIndex){
                    			numOutOfNumDisease = diseaseLimitIndex+" out of "+diseaseRecords;
                    		}
                    		if (agentRecords>agentLimitIndex){
                    			numOutOfNumAgent = agentLimitIndex+" out of "+agentRecords;
                    		}
                    		if (results2!=null)
                    		for (Object gfa : results2) {
                                pb.setMessage(progressMessage+"Records "+(diseaseLimit+agentLimit-diseaseLimitIndex-agentLimitIndex)+"/"+(diseaseRecords+agentRecords)+"\n");
								if (stopAlgorithm == true) {
									stopAlgorithm(pb);
									break;
								}
                    			if (gfa instanceof GeneDiseaseAssociation) {
                        			if (diseaseLimitIndex>0)
                        				diseaseLimitIndex--;
                        			else
                        				continue;
                    				GeneDiseaseAssociation gda = (GeneDiseaseAssociation) gfa;
                    				markerData.add(new MarkerData(selectedMarkerInfo.get(i),numOutOfNumDisease));
                    				geneData.add(new GeneData(gda.getGene().getSymbol(),gda.getGene()));
                    				log.debug("  Disease: " + gda.getDiseaseOntology().getName());
                    				diseaseData.add(new DiseaseData(gda.getDiseaseOntology().getName(),gda.getDiseaseOntology(), gda.getDiseaseOntology().getEVSId()));
                    				log.debug("    Role: " + gda.getRole());
                    				Evidence e = gda.getEvidence();
                    				log.debug("    Sentence: "+e.getSentence());
                    				log.debug("    PubmedId:"+e.getPubmedId());
                    				roleData.add(new RoleData(gda.getRole()));
                    				sentenceData.add(new SentenceData(e.getSentence()));
                    				pubmedData.add(new PubmedData(Integer.toString(e.getPubmedId())));
                    			}
                    			else if (gfa instanceof GeneAgentAssociation) {
                        			if (agentLimitIndex>0)
                        				agentLimitIndex--;
                        			else
                        				continue;
                    				GeneAgentAssociation gaa = (GeneAgentAssociation) gfa;
                    				markerData2.add(new MarkerData(selectedMarkerInfo.get(i),numOutOfNumAgent));
                    				geneData2.add(new GeneData(gaa.getGene().getSymbol(),gaa.getGene()));
                    				evsIdData.add(new EvsIdData(gaa.getAgent().getEVSId()));
                    				agentData.add(new DiseaseData(gaa.getAgent().getName(),null, gaa.getAgent().getEVSId()));
                    				log.debug("  Id: " + gaa.getId());
                    				log.debug("  Role: " + gaa.getRole());
                    				log.debug("  EvsId: " + gaa.getAgent().getEVSId());
                    				log.debug("  Name: " + gaa.getAgent().getName());
                    				Evidence e = gaa.getEvidence();
                    				log.debug("    Sentence: "+e.getSentence());
                    				log.debug("    PubmedId:"+e.getPubmedId());
                    				agentRoleData.add(new RoleData(gaa.getRole()));
                    				agentSentenceData.add(new SentenceData(e.getSentence()));
                    				agentPubmedData.add(new PubmedData(Integer.toString(e.getPubmedId())));
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
                    MarkerData[] markers2 = markerData2.toArray(new MarkerData[0]);
                    GeneData[] genes2 = geneData2.toArray(new GeneData[0]);
                    //TODO: clean this variable
                    EvsIdData[] evsIds = evsIdData.toArray(new EvsIdData[0]);
                    DiseaseData[] agents = agentData.toArray(new DiseaseData[0]);
                    RoleData[] agentRoles = agentRoleData.toArray(new RoleData[0]);
                    SentenceData[] agentSentences = agentSentenceData.toArray(new SentenceData[0]);
                    PubmedData[] agentPubmeds = agentPubmedData.toArray(new PubmedData[0]);
                    if (diseases.length+agents.length==0){
        				JOptionPane
						.showMessageDialog(
								null,
								"Server does not have records about these markers, please try other markers.",
								"Server returns no records", JOptionPane.OK_OPTION);
                    }
                    for (int i = 0; i < dropDownLists.length; i++) {
                    	dropDownLists[i].removeAllItems();
                    	dropDownLists[i].addItem("");
					}
                    for (int j = 0; j < diseases.length; j++) {
                    	//removeItem() followed by addItem() will produce a non-duplicate list
        				dropDownLists[0].removeItem(markers[j].name);
        				dropDownLists[0].addItem(markers[j].name);						
        				dropDownLists[1].removeItem(genes[j].name);
        				dropDownLists[1].addItem(genes[j].name);						
        				dropDownLists[2].removeItem(diseases[j].name);
        				dropDownLists[2].addItem(diseases[j].name);
        				dropDownLists[3].removeItem(roles[j].role);
        				dropDownLists[3].addItem(roles[j].role);
					}
                    for (int j = 0; j < agents.length; j++) {
        				dropDownLists[4].removeItem(markers2[j].name);
        				dropDownLists[4].addItem(markers2[j].name);						
        				dropDownLists[5].removeItem(genes2[j].name);
        				dropDownLists[5].addItem(genes2[j].name);						
        				dropDownLists[6].removeItem(agents[j].name);
        				dropDownLists[6].addItem(agents[j].name);
        				dropDownLists[7].removeItem(agentRoles[j].role);
        				dropDownLists[7].addItem(agentRoles[j].role);
					}
                    for (int i = 0; i < dropDownLists.length; i++) {
                    	orderDropDownLists(dropDownLists[i]);
					}
                    appendDiseaseNumber(markers,diseases,dropDownLists[2]);
                    appendDiseaseNumber(markers2,agents,dropDownLists[6]);
                    
                    diseaseModel = new CGITableModel(markers, genes, diseases, roles, sentences, pubmeds);
                    //annotationTableList.put(new Integer(maSet.hashCode()),  new TableModel(markers, genes, diseases, pubmeds));
                    diseaseTable.setSortableModel(diseaseModel);
                    agentModel = new CGITableModel(markers2, genes2, agents, agentRoles, agentSentences, agentPubmeds);
                    agentTable.setSortableModel(agentModel);

                    diseaseTable.getColumnModel().getColumn(0).setHeaderValue("Marker");
                    diseaseTable.getColumnModel().getColumn(1).setHeaderValue("Gene");
                    diseaseTable.getColumnModel().getColumn(2).setHeaderValue("Disease");
                    diseaseTable.getColumnModel().getColumn(3).setHeaderValue("Role");
                    diseaseTable.getColumnModel().getColumn(4).setHeaderValue("Sentence");
                    diseaseTable.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
                    agentTable.getColumnModel().getColumn(0).setHeaderValue("Marker");
                    agentTable.getColumnModel().getColumn(1).setHeaderValue("Gene");
                    agentTable.getColumnModel().getColumn(2).setHeaderValue("Agent");
                    agentTable.getColumnModel().getColumn(3).setHeaderValue("Role");
                    agentTable.getColumnModel().getColumn(4).setHeaderValue("Sentence");
                    agentTable.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
                    sortByNumberOfRecords = true;

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            agentTable.getTableHeader().repaint();
                            leftHeadPanel.setVisible(true);
                            rightHeadPanel.setVisible(true);
                            selectedTable = diseaseTable;
                            for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
                				((CGITableModel) selectedTable.getModel()).collapseBy(selectedTable,
										((GeneData) (((CGITableModel) selectedTable
												.getModel()).getObject(j,
												COL_GENE))).name,
										((DiseaseData) (((CGITableModel) selectedTable
												.getModel()).getObject(j,
												COL_DISEASE))).name);                            	
                            }
                        	((SortableTableModel)selectedTable.getModel()).sortByColumn(2,false);
                        	((SortableTableModel)selectedTable.getModel()).sortByColumn(0,true);
                            selectedTable = agentTable;
                            for (int j = 0; j < ((CGITableModel)selectedTable.getModel()).size; j++) {
                				((CGITableModel) selectedTable.getModel()).collapseBy(selectedTable,
										((GeneData) (((CGITableModel) selectedTable
												.getModel()).getObject(j,
												COL_GENE))).name,
										((DiseaseData) (((CGITableModel) selectedTable
												.getModel()).getObject(j,
												COL_DISEASE))).name);                            	
                            }
                        	((SortableTableModel)selectedTable.getModel()).sortByColumn(2,false);
                        	((SortableTableModel)selectedTable.getModel()).sortByColumn(0,true);
                            syncHeaderWidthWithTableWidth();
                        }
                    });
                }

            	private void orderDropDownLists(JComboBox dropDownLists){
                	int itemCount = dropDownLists.getItemCount();
                	String[] array = new String[itemCount];
                	for (int i = 0; i < itemCount; i++) {
                		array[i]=(String)dropDownLists.getItemAt(i);
            		}
                	Arrays.sort(array);
                	dropDownLists.removeAllItems();
                	for (int i = 0; i < itemCount; i++) {
                		dropDownLists.addItem(array[i]);
            		}    	
                }
                
            };
            Thread t = new Thread(query);
            t.setPriority(Thread.MIN_PRIORITY);
            if (userAlsoWantCaBioData)
            	t.start();
            if (userAlsoWantPathwayData &&(!userAlsoWantCaBioData))
                jTabbedPane1.setSelectedComponent(annotationPanel);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //block for retrieving CGI data ends
        //block for retrieving annotation data starts
        try {
            Runnable query = new Runnable() {         	 
            	 
            	public void run() {
            		ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                    pb.addObserver(getAnnotationsPanel());
                    pb.setMessage("Connecting to server...");
                    ArrayList<MarkerData> markerData = new ArrayList<MarkerData>();
                    ArrayList<GeneData> geneData = new ArrayList<GeneData>();
                    ArrayList<PathwayData> pathwayData = new ArrayList<PathwayData>();
                    if (selectedMarkerInfo != null) {
                        pb.setTitle("Querying caBIO..");
                        pb.start();           

                        if (criteria == null) {
                            try {
                    			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                                criteria = new GeneSearchCriteriaImpl();
                            } catch (Exception e) {
                                log.error("Exception: could not create caBIO search criteria in Annotation Panel. Exception is: ");
                                e.printStackTrace();
                                return;
                            }
                        }
                                              
                        for (int i = 0; i < selectedMarkerInfo.size(); i++) {
                             
                           if (stopAlgorithm == true)
                           {
                               stopAlgorithm(pb);
                                  return;
                            }
                            
                            String geneName = selectedMarkerInfo.get(i).getGeneName();
                            GeneAnnotation[] annotations;
                            annotations = criteria.searchByName(geneName);

                            if (annotations == null )
                            	return;
                            
                            pb.setMessage("Getting Marker Annotation and Pathways: " + selectedMarkerInfo.get(i).getLabel());
                            MarkerData marker = new MarkerData(selectedMarkerInfo.get(i),"");
                            if ( annotations.length > 0) {
                                for (int j = 0; j < annotations.length; j++) {
                                    if (stopAlgorithm == true)                       
                                    {
                                       stopAlgorithm(pb);
                                        return;
                                    }
                                    
                                    Pathway[] pways = annotations[j].getPathways();
                                    Pathway[] temp = new Pathway[pathways.length + pways.length];
                                    System.arraycopy(pathways, 0, temp, 0, pathways.length);
                                    System.arraycopy(pways, 0, temp, pathways.length, pways.length);
                                    pathways = temp;
                                    GeneData gene = new GeneData(annotations[j].getGeneSymbol(),annotations[j].getGene());
                                    if (pways.length > 0) {
                                        for (int k = 0; k < pways.length; k++) {
                                            pathwayData.add(new PathwayData(pways[k].getPathwayName(), pways[k]));
                                            gene.setOrganism(annotations[j].getOrganismAbbreviation());
                                            geneData.add(gene);
                                            markerData.add(marker);
                                        }
                                    } else {
                                        pathwayData.add(new PathwayData("", null));
                                        geneData.add(gene);
                                        markerData.add(marker);
                                    }
                                }
                            } else {
                                pathwayData.add(new PathwayData("", null));
                                geneData.add(new GeneData("", null));
                                markerData.add(marker);
                            }
                        } 
                        
                        pb.stop();
                        pb.dispose();                       
                        
                    }
                    MarkerData[] markers = markerData.toArray(new MarkerData[0]);
                    GeneData[] genes = geneData.toArray(new GeneData[0]);
                    PathwayData[] pathways = pathwayData.toArray(new PathwayData[0]);
                    annotationModel = new AnnotationTableModel(markers, genes, pathways);
                    annotationTableList.put(new Integer(maSet.hashCode()),  annotationModel);
                    annotationTable.setSortableModel(annotationModel);
                    annotationTable.getColumnModel().getColumn(0).setHeaderValue("Marker");
                    annotationTable.getColumnModel().getColumn(1).setHeaderValue("Gene");
                    annotationTable.getColumnModel().getColumn(2).setHeaderValue("Pathway");
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                        	annotationTable.getTableHeader().repaint();
                        }
                    });
                }
            };
            Thread t = new Thread(query);
            t.setPriority(Thread.MIN_PRIORITY);
            if (userAlsoWantPathwayData)
            	t.start();
            if ((!userAlsoWantPathwayData) && userAlsoWantCaBioData)
                jTabbedPane1.setSelectedComponent(cgiPanel);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
     * 
     */
    private void updateDiseaseNumber(){
    	JComboBox comboBox = dropDownLists[2];
    	MarkerData[] mData = ((CGITableModel)diseaseTable.getModel()).markerData;
    	DiseaseData[] dData = ((CGITableModel)diseaseTable.getModel()).diseaseData;
    	HashSet<String> diseaseNames = new HashSet<String>();
    	for (int i = 0; i < dData.length; i++) {
			String diseaseName = dData[i].name;
			diseaseNames.add(diseaseName);
		}
    	HashSet<String> markerNames = new HashSet<String>();
    	for (int i = 0; i < mData.length; i++) {
			String markerName = mData[i].name;
			markerNames.add(markerName);
		}
    	HashMap<String, Integer> countMap = new HashMap<String, Integer>(); 
    	for (Iterator<String> iterator = diseaseNames.iterator(); iterator.hasNext();) {
    		String diseaseName = iterator.next();
    		HashSet hitMarkers = new HashSet<String>();
	    	for (Iterator<String> iterator2 = markerNames.iterator(); iterator2.hasNext();) {
	    		String markerName = iterator2.next();
	        	for (int i = 0; i < dData.length; i++) {
	    			String aDiseaseName = dData[i].name;
	    			if(mData[i].name.equals(markerName)){
	        			if (aDiseaseName.equals(diseaseName)){
	        				hitMarkers.add(markerName);
	        			}
	    			}
    			}
    		}
	    	countMap.put(diseaseName, hitMarkers.size());
		}
		comboBox.removeAllItems();
		comboBox.addItem("");
		for (Iterator<String> iterator = diseaseNames.iterator(); iterator.hasNext();) {
			String diseaseName = iterator.next();
			comboBox.addItem(diseaseName+" ("+countMap.get(diseaseName)+")");
		}
		// do the same thing for table2
    	comboBox = dropDownLists[6];
    	mData = ((CGITableModel)agentTable.getModel()).markerData;
    	dData = ((CGITableModel)agentTable.getModel()).diseaseData;
    	diseaseNames = new HashSet<String>();
    	for (int i = 0; i < dData.length; i++) {
			String diseaseName = dData[i].name;
			diseaseNames.add(diseaseName);
		}
    	markerNames = new HashSet<String>();
    	for (int i = 0; i < mData.length; i++) {
			String markerName = mData[i].name;
			markerNames.add(markerName);
		}
    	countMap = new HashMap<String, Integer>(); 
    	for (Iterator<String> iterator = diseaseNames.iterator(); iterator.hasNext();) {
    		String diseaseName = iterator.next();
    		HashSet hitMarkers = new HashSet<String>();
	    	for (Iterator<String> iterator2 = markerNames.iterator(); iterator2.hasNext();) {
	    		String markerName = iterator2.next();
	        	for (int i = 0; i < dData.length; i++) {
	    			String aDiseaseName = dData[i].name;
	    			if(mData[i].name.equals(markerName)){
	        			if (aDiseaseName.equals(diseaseName)){
	        				hitMarkers.add(markerName);
	        			}
	    			}
    			}
    		}
	    	countMap.put(diseaseName, hitMarkers.size());
		}
		comboBox.removeAllItems();
		comboBox.addItem("");
		for (Iterator<String> iterator = diseaseNames.iterator(); iterator.hasNext();) {
			String diseaseName = iterator.next();
			comboBox.addItem(diseaseName+" ("+countMap.get(diseaseName)+")");
		}		
    }
    
    /*
     * 
     */
    private void appendDiseaseNumber(MarkerData[] markers2,
			DiseaseData[] disease, JComboBox comboBox) {
		int itemCount = comboBox.getItemCount();
    	String[] itemName = new String[itemCount];
		//for each item in comboBox,
		for (int i = 0; i < itemCount; i++) {
			String diseaseName = (String)comboBox.getItemAt(i);
			Set<String> markers = new HashSet(); 
			//check how many markers associate with it,
			if (diseaseName.equals("")){
				//it's the first one, we don't need to count.
				itemName[i]=diseaseName;
			}else{
				int count = 0;
				for (int j = 0; j < disease.length; j++) {
					if (disease[j].name.equals(diseaseName)){
						// we need to consider markers, if we already
						// counted for this marker, we skip it.
						String marker = markers2[j].name;
						if (!markers.contains(marker)){
							markers.add(marker);
							count++;
						}
					}
				}
				//append the number
				itemName[i]=diseaseName.concat(" ("+count+")");;
			}
		}
		comboBox.removeAllItems();
    	for (int i = 0; i < itemCount; i++) {
    		comboBox.addItem(itemName[i]);
		}    	
	}
    
    /*
     * 
     */
    private void cgiClearButton_actionPerformed(ActionEvent e) {
        diseaseTable.setSortableModel(new CGITableModel());
        diseaseTable.getColumnModel().getColumn(0).setHeaderValue("Marker");
        diseaseTable.getColumnModel().getColumn(1).setHeaderValue("Gene");
        diseaseTable.getColumnModel().getColumn(2).setHeaderValue("Disease");
        diseaseTable.getColumnModel().getColumn(3).setHeaderValue("Role");
        diseaseTable.getColumnModel().getColumn(4).setHeaderValue("Sentence");
        diseaseTable.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
        diseaseTable.getTableHeader().revalidate();
        agentTable.setSortableModel(new CGITableModel());
        agentTable.getColumnModel().getColumn(0).setHeaderValue("Marker");
        agentTable.getColumnModel().getColumn(1).setHeaderValue("Gene");
        agentTable.getColumnModel().getColumn(2).setHeaderValue("Agent");
        agentTable.getColumnModel().getColumn(3).setHeaderValue("Role");
        agentTable.getColumnModel().getColumn(4).setHeaderValue("Sentence");
        agentTable.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
        agentTable.getTableHeader().revalidate();
        leftHeadPanel.setVisible(false);
        rightHeadPanel.setVisible(false);
        annotationTableList.put(new Integer(maSet.hashCode()),  new CGITableModel());
    }

    /*
     * 
     */
    private void annoClearButton_actionPerformed(ActionEvent e) {
        annotationTable.setSortableModel(new AnnotationTableModel());
        annotationTable.getColumnModel().getColumn(0).setHeaderValue("Marker");
        annotationTable.getColumnModel().getColumn(1).setHeaderValue("Gene");
        annotationTable.getColumnModel().getColumn(2).setHeaderValue("Pathway");
        annotationTable.getTableHeader().revalidate();
        annotationTableList.put(new Integer(maSet.hashCode()),  new AnnotationTableModel());
    }

    /*
     * 
     */
    private void showPanels_actionPerformed(ActionEvent e) {
        if (selectedMarkerInfo == null || selectedMarkerInfo.size() == 0) {
            JOptionPane.showMessageDialog(cgiPanel.getParent(), "Please activate marker panels to retrieve annotations.");
        }else if((!userAlsoWantCaBioData)&&(!userAlsoWantPathwayData)){
        	JOptionPane.showMessageDialog(cgiPanel.getParent(), "Please select a data type to retrieve. (Annotation data and/or Cancer Gene Index data)");
        }else
        	showAnnotation();
    }

    /*
     * 
     */
    private void activateMarker(MarkerData markerData) {
        publishMarkerSelectedEvent(new MarkerSelectedEvent(markerData.marker));
    }

    /*
     * 
     */
    private void activateGene(final GeneData gene) {
        JPopupMenu popup = new JPopupMenu();
        String value = (String) gene.name;
        //Get Entrez id
        String entrezId = "";
        Collection<DatabaseCrossReference> crossReferences = gene.gene.getDatabaseCrossReferenceCollection();
        for (Iterator iterator = crossReferences.iterator(); iterator.hasNext();) {
			DatabaseCrossReference crossReference = (DatabaseCrossReference) iterator.next();
			if (crossReference.getDataSourceName().equals("LOCUS_LINK_ID")){
				entrezId = crossReference.getCrossReferenceId();
				break;
			}
		}
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
        if (!gene.getOrganism().equals("")){
	        String cgapUrl = GENE_FINDER_PREFIX + "ORG=" + gene.getOrganism() + "&CID=" + gene.gene.getClusterId();
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
                    String address = GeneCards_PREFIX + value;
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
        
        popup.show(annotationTable, (int) (MouseInfo.getPointerInfo().getLocation().getX() - annotationTable.getLocationOnScreen().getX()),
                (int) (MouseInfo.getPointerInfo().getLocation().getY() - annotationTable.getLocationOnScreen().getY()));
    }

    /*
     * 
     */
    private void activatePathway(final PathwayData pathwayData) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem viewDiagram = new JMenuItem("View Diagram");
        viewDiagram.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                publishAnnotationsEvent(new AnnotationsEvent("Pathway Selected", pathwayData.pathway));
                receive(new AnnotationsEvent("Pathway Selected", pathwayData.pathway));
            
            }
        });
        if (pathwayData.pathway.getPathwayDiagram()!=null)
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

        popup.show(annotationTable, (int) (MouseInfo.getPointerInfo().getLocation().getX() - annotationTable.getLocationOnScreen().getX()),
                (int) (MouseInfo.getPointerInfo().getLocation().getY() - annotationTable.getLocationOnScreen().getY()));
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
    private JSplitPane cgiPanel = null;
    /**
     * Visual Widget
     */
    private JPanel annotationPanel = new JPanel();	//for annotation
    private JScrollPane jScrollPane1 = new JScrollPane(); //for disease
    private JScrollPane jScrollPane2 = new JScrollPane(); //for agent

    /**
     * Visual Widget
     */
    private SortableTable diseaseTable;
    private CGITableModel diseaseModel;
    private SortableTable agentTable;
    private CGITableModel agentModel;
    private SortableTable annotationTable;
    private AnnotationTableModel annotationModel;
    
    private HashMap<Integer, SortableTableModel> annotationTableList; 

    /**
     * Visual Widget
     */
    private JPanel annoButtonPanel = new JPanel();
    private JPanel cgiButtonPanel = new JPanel();
    private JTextArea textArea = new JTextArea();
    /**
     * Visual Widget
     */
    //GUIs used by Annotation panel
    private JButton annoRetrieveButton = new JButton();
    JButton annoClearButton = new JButton();
    private JCheckBox annoRetrieveCaBioCheckBox = new JCheckBox(RETRIEVE_CGI_DATA);
    private JCheckBox annoRetrievePathwayCheckBox = new JCheckBox(RETRIEVE_PATHWAY_DATA);
    //GUIs used by CGI panel
    private JButton cgiRetrieveButton = new JButton();
    JButton cgiClearButton = new JButton();
    private JCheckBox cgiRetrieveCaBioCheckBox = new JCheckBox(RETRIEVE_CGI_DATA);
    private JCheckBox cgiRetrievePathwayCheckBox = new JCheckBox(RETRIEVE_PATHWAY_DATA);
    
    
    private DSItemList<DSGeneMarker> selectedMarkerInfo = null;
    private DSItemList<DSGeneMarker> retrieveMarkerInfo = new CSItemList<DSGeneMarker>();
    private GeneSearchCriteria criteria = null;
    private Pathway[] pathways = new Pathway[0];

    private DSMicroarraySet maSet = null;

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    @Subscribe
    public void receive(GeneSelectorEvent e, Object source) {
        if (maSet != null && e.getPanel() != null) {
			DSPanel markerPanel = e.getPanel().activeSubset();
            DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(maSet);
            maView.setMarkerPanel(markerPanel);
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
        
//        if (annotationTableList.containsKey(new Integer(hashcode)))
//        {
//        	diseaseModel = annotationTableList.get(new Integer(hashcode));
//        	diseaseTable.setSortableModel(diseaseModel);
//        	diseaseTable.getColumnModel().getColumn(0).setHeaderValue("Marker");
//            diseaseTable.getColumnModel().getColumn(1).setHeaderValue("Gene");
//            diseaseTable.getColumnModel().getColumn(2).setHeaderValue("Disease");
//            diseaseTable.getColumnModel().getColumn(3).setHeaderValue("Role");
//            diseaseTable.getColumnModel().getColumn(4).setHeaderValue("Sentence");
//            diseaseTable.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
//            diseaseTable.getTableHeader().revalidate();
//             
//        }           
//        else
//        {
//        	diseaseTable.setSortableModel(new CGITableModel());
//        	diseaseTable.getColumnModel().getColumn(0).setHeaderValue("Marker");
//            diseaseTable.getColumnModel().getColumn(1).setHeaderValue("Gene");
//            diseaseTable.getColumnModel().getColumn(2).setHeaderValue("Disease");
//            diseaseTable.getColumnModel().getColumn(3).setHeaderValue("Role");
//            diseaseTable.getColumnModel().getColumn(4).setHeaderValue("Sentence");
//            diseaseTable.getColumnModel().getColumn(5).setHeaderValue("Pubmed");
//            diseaseTable.getTableHeader().revalidate();
//            //jTabbedPane1.setSelectedIndex(0);
//             
//        }
         
         
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
    
    
    
    /*
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
    
    /*
     * 
     */
    private void pathwayComboBox_actionPerformed(ActionEvent e) {
       
    	 
    	Object pathwayName = pathwayComboBox.getSelectedItem();
    	if (pathwayName != null && !pathwayName.toString().trim().equals(""))
    	{    setSvg(svgStringList.get(pathwayName));
    	    jTabbedPane1.setTitleAt(jTabbedPane1.indexOfComponent(pathwayPanel), pathwayName.toString());
    	}
    	else
    	{
    	    try{
    		svgCanvas.setDocument(null);
    	    }catch(IllegalStateException ex)
    	    {
    	    	log.error(ex,ex);
    	    }
    		svgCanvas.revalidate();
            pathwayPanel.revalidate();
            jTabbedPane1.setTitleAt(jTabbedPane1.indexOfComponent(pathwayPanel),"Pathway");
    	}     
    	 
    }
    
    /*
     * 
     */
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
            jTabbedPane1.setTitleAt(jTabbedPane1.indexOfComponent(pathwayPanel),"Pathway");
              
    	}  	
    	 
    	
    }
    
    /*
     * 
     */
    private void clearHistButton_actionPerformed(ActionEvent e) {
        
    	pathwayComboBox.removeAllItems();
    	pathwayComboBox.insertItemAt(" ", 0);
    	svgStringList.clear();    	 
		pathwayList.clear();
		try{
    	svgCanvas.setDocument(null); 
		}catch(IllegalStateException ex)
	    {
	    	log.error(ex,ex);
	    }
		svgCanvas.revalidate();
        pathwayPanel.revalidate();
        jTabbedPane1.setTitleAt(jTabbedPane1.indexOfComponent(pathwayPanel),"Pathway");    
    }

    /**
     * 
     * @return
     */
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
          
        	private String pathwayName = pathway.getPathwayName();
        	private String pathwayDiagram = pathway.getPathwayDiagram();
        	public void run() {
            org.geworkbench.util.ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);             
            pb.setTitle("Constructing SVG Pathway");
            pb.setMessage("Creating Image..");
            pb.start();           
            
            addPathwayName(pathwayName, pathwayDiagram);
           
            pb.stop(); 
            pb.dispose();            
          
            Container parent = pathwayPanel.getParent();                
            if (parent instanceof JTabbedPane)
            {    ((JTabbedPane) parent).setSelectedComponent(pathwayPanel);
               JTabbedPane p =  (JTabbedPane) parent;
               p.setTitleAt(jTabbedPane1.indexOfComponent(pathwayPanel), pathway.getPathwayName());
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
    
    /**
     * <code>Canvas</code> on which the Pathway SVG image is drawn
     */
    private JSVGCanvas svgCanvas = new JSVGCanvas(new UserAgent(), true, true);
    private static final String SVG_BASE_URL = "http://cgap.nci.nih.gov/";

       
    
   
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
            	log.error(ex);
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
      	    	log.error(ex);
      	      }
     	    }
            svgCanvas.revalidate();
            pathwayPanel.revalidate(); 
        } else {
            JOptionPane.showMessageDialog(pathwayPanel, "No Pathway diagram obtained from caBIO", "Diagram missing", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    private static final String URI_PREFIX = "/CMAP/";
    private void svgCanvas_linkActivated(LinkActivationEvent lae) {
    	String referenceUri = lae.getReferencedURI();
    	if(referenceUri.startsWith(URI_PREFIX)) {
    		referenceUri = referenceUri.substring(URI_PREFIX.length());
    	} else {
    		log.warn("reference URI does not start with /CAMP/ as expected: "+referenceUri);
    	}
        String uri = SVG_BASE_URL + referenceUri;

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

    /*
     * 
     */
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

    
    /**
     * 
     * @param pb
     */
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
