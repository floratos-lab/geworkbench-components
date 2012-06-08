package org.geworkbench.components.annotations;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.batik.bridge.DefaultExternalResourceSecurity;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
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
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.ProgressDialog;
import org.geworkbench.util.ProgressItem;
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
 * @version $Id$
 *
 */
@AcceptTypes({DSMicroarraySet.class})
@SuppressWarnings("unchecked")
public class AnnotationsPanel2 implements VisualPlugin{
    static final String GENE_FINDER_PREFIX = "http://cgap.nci.nih.gov/Genes/GeneInfo?";
    private static final String RETRIEVE_INFORMATION = "Retrieve Annotations";
	private static final String RETRIEVE_PATHWAY_DATA = "Get gene annotations (from CGAP)";
	private static final String RETRIEVE_CGI_DATA = "Get Disease/Agent associations (from CGI)";
	private static final String[] Human_Mouse= {"Human","Mouse"};
    private static final String[] Human_Mouse_Code= {"Hs","Mm"};
    /**
     * Web URL prefix for obtaining Gene annotation
     */
    static final String GeneCards_PREFIX = "http://www.genecards.org/cgi-bin/carddisp.pl?gene=";
    /**
     * Web URL prefix for obtaining Pubmed article
     */
    private static final String PUBMED_PREFIX = "http://www.ncbi.nlm.nih.gov/pubmed/";

    /**
     * Web URL prefix for obtaining Agent annotation
     */
    private static final String EVS_PREFIX = "http://nciterms.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI%20Thesaurus&code=";
	private static Log log = LogFactory.getLog(AnnotationsPanel2.class);
    
    JComboBox[] dropDownLists = new JComboBox[8];	//drop down lists for filters
    JPanel leftHeadPanel = new JPanel();	//I put it as global variable so we can change their width at run time.
    JPanel rightHeadPanel = new JPanel();	//I put it as global variable so we can change their width at run time.
	JPopupMenu expandCollapseRetrieveAllMenu = new JPopupMenu();
	DSGeneMarker selectedMarker = null;
	int selectedRow = -1;
	String selectedGene = "";
	String selectedDisease = "";
	SortableTable selectedTable = null;
    String humanOrMouse = Human_Mouse_Code[0];	//default to Human
    //Aris want the table to sort by number of records when the records just been retrieved.
    boolean sortByNumberOfRecords=true;

    JMenuItem retrieveItem = new JMenuItem("retrieve all");

	private static String unwrapFromHTML(String s) {
		if(s.startsWith("<html><a href=\"__noop\">"))
			return s.substring("<html><a href=\"__noop\">".length(), s.length()
				- "</a></html>".length());
		else
			return "";
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

    boolean userAlsoWantCaBioData = false;
    boolean userAlsoWantPathwayData = false;

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
				annoHumanOrMouseComboBox.setEnabled(userAlsoWantPathwayData);
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
        cgiClearButton.setToolTipText("Clear both tables.");
        cgiClearButton.setFocusPainted(true);
        cgiClearButton.setText("Clear");
        cgiClearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cgiClearButton_actionPerformed(e);
            }
        });

        exportButton.setForeground(Color.black);
        exportButton.setToolTipText("Export to CSV files");
        exportButton.setFocusPainted(true);
        exportButton.setText("Export");
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	exportButton_actionPerformed(e);
            }
        });

        annotationExportButton.setForeground(Color.black);
        annotationExportButton.setToolTipText("Export to CSV files");
        annotationExportButton.setFocusPainted(true);
        annotationExportButton.setText("Export");
        annotationExportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	annotationExportButton_actionPerformed(e);
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
				cgiHumanOrMouseComboBox.setEnabled(userAlsoWantPathwayData);
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
        ActionListener HumanMouseListener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String selected =(String)((JComboBox)e.getSource()).getSelectedItem();
        		if (selected.equals(Human_Mouse[0]))
        			humanOrMouse = Human_Mouse_Code[0];
        		else if (selected.equals(Human_Mouse[1]))
        			humanOrMouse = Human_Mouse_Code[1];
        		else{
        			log.error("Shouldn't happen");
        		}
        		//sync two dropdown box
        		if (e.getSource() == annoHumanOrMouseComboBox) cgiHumanOrMouseComboBox.setSelectedItem(selected);
        		if (e.getSource() == cgiHumanOrMouseComboBox) annoHumanOrMouseComboBox.setSelectedItem(selected);
            }
        };
        annoButtonPanel.add(annoRetrieveCaBioCheckBox);
        annoButtonPanel.add(annoRetrievePathwayCheckBox);
        annoButtonPanel.add(annoHumanOrMouseComboBox);
        annoHumanOrMouseComboBox.addActionListener(HumanMouseListener);
        annoButtonPanel.add(annoRetrieveButton);
        annoButtonPanel.add(annoClearButton);
        annoButtonPanel.add(annotationExportButton);
        cgiButtonPanel.add(cgiRetrieveCaBioCheckBox);
        cgiButtonPanel.add(cgiRetrievePathwayCheckBox);
        cgiButtonPanel.add(cgiHumanOrMouseComboBox);
        cgiHumanOrMouseComboBox.addActionListener(HumanMouseListener);
        cgiButtonPanel.add(cgiRetrieveButton);
        cgiButtonPanel.add(cgiClearButton);
        cgiButtonPanel.add(exportButton);
        annotationModel = new AnnotationTableModel();
        annotationTable = new SortableTable(annotationModel);
        annotationTable.getTableHeader().setPreferredSize(new Dimension(0, 25));
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

        diseaseModel = new CGITableModel(this);
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
		diseaseTable.getTableHeader().setPreferredSize(new Dimension(0, 25));

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
        diseaseTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
        diseaseTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
        diseaseTable.getColumnModel().getColumn(2).setHeaderValue("     Disease");
        diseaseTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
        diseaseTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
        diseaseTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
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
	                if (column == CGITableModel.COL_MARKER){
	                    diseaseModel.activateCell(row, column);
	                }
	                if ((column == CGITableModel.COL_GENE)) {
	                    String value = (String) diseaseTable.getValueAt(row, column);
	                    value=unwrapFromHTML(value);
	                    if(value.length()>0) {
		                    String address = GeneCards_PREFIX + value;
		                    try {
								BrowserLauncher.openURL(address);
							} catch (IOException e1) {
								log.error(e1,e1);
							}
	                    }
	                }
	                if ((column == CGITableModel.COL_PUBMED)) {
	                    String value = (String) diseaseTable.getValueAt(row, column);
	                    value=unwrapFromHTML(value);
	                    if (value.indexOf(";") > -1)
	                    {
	                    	for (String subvalue : value.split(";"))
	                    		try{
	                    			BrowserLauncher.openURL(PUBMED_PREFIX + subvalue);
	                    		}catch(IOException e1){
	                    			log.error(e1);
	                    		}
	                    } else if(value.length()>0) {
							String address = PUBMED_PREFIX + value;
							try {
								BrowserLauncher.openURL(address);
							} catch (IOException e1) {
								log.error(e1, e1);
							}
						}
	                }
	                if ((column == CGITableModel.COL_SENTENCE)) {
	                	String value = (String) diseaseTable.getValueAt(row, column);
	                	textArea.setText(value);
	                }
                }else{
                	selectedTable = diseaseTable;
                	selectedMarker = ((CGITableModel)diseaseTable.getModel()).getMarkerAt(row,0).marker;
                	selectedRow = row;
                	selectedGene = ((GeneData)(((CGITableModel)diseaseTable.getModel()).getObjectAt(row, CGITableModel.COL_GENE))).name;
                	selectedDisease = ((DiseaseData)(((CGITableModel)diseaseTable.getModel()).getObjectAt(row, CGITableModel.COL_DISEASE))).name;
                	MarkerData md = ((CGITableModel) diseaseTable.getModel())
							.getMarkerAt(row, column);
					if (md.numOutOfNum.equals("")) {
						// If no records left on server, we don't show the
						// Retrieve All button. Mantis #1840
						retrieveItem.setVisible(false);
					} else {
						retrieveItem.setVisible(true);
					}
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
                    if ((column == CGITableModel.COL_GENE)
//                    		|| (column == COL_DISEASE)
							|| (column == CGITableModel.COL_PUBMED)) {
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

        annotationTableList = new HashMap<Integer, AnnotationTableModel>();
        diseaseTableList = new HashMap<Integer, CGITableModel>();
        agentTableList = new HashMap<Integer, CGITableModel>();

        agentModel = new CGITableModel(this);
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
        agentTable.getTableHeader().setPreferredSize(new Dimension(0, 25));
        agentTable.getColumnModel().addColumnModelListener(tableColumnModelListener);
        agentTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
        agentTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
        agentTable.getColumnModel().getColumn(2).setHeaderValue("     Agent");
        agentTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
        agentTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
        agentTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
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
	                if (column == CGITableModel.COL_MARKER){
	                    agentModel.activateCell(row, column);
	                }
	                if ((column == CGITableModel.COL_GENE)) {
	                    String value = (String) agentTable.getValueAt(row, column);
	                    value=unwrapFromHTML(value);
	                    if(value.length()>0) {
		                    String address = GeneCards_PREFIX + value;
		                    try {
								BrowserLauncher.openURL(address);
							} catch (IOException e1) {
								log.error(e1,e1);
							}
	                    }
	                }
	                if ((column == CGITableModel.COL_PUBMED)) {
	                    String value = (String) agentTable.getValueAt(row, column);
	                    value=unwrapFromHTML(value);
	                    if(value.length()>0) {
		                    String address = "http://www.ncbi.nlm.nih.gov/pubmed/"+value;
		                    try {
								BrowserLauncher.openURL(address);
							} catch (IOException e1) {
								log.error(e1,e1);
							}
	                    }
	                }
	                if ((column == CGITableModel.COL_SENTENCE)) {
	                	String value = (String) agentTable.getValueAt(row, column);
	                	textArea.setText(value);
	                }
                }else{
                	selectedTable = agentTable;
                	selectedMarker = ((CGITableModel)agentTable.getModel()).getMarkerAt(row,0).marker;
                	selectedRow = row;
                	selectedGene = ((GeneData)(((CGITableModel)agentTable.getModel()).getObjectAt(row, CGITableModel.COL_GENE))).name;
                	selectedDisease = ((DiseaseData)(((CGITableModel)agentTable.getModel()).getObjectAt(row, CGITableModel.COL_DISEASE))).name;
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
                    if ((column == CGITableModel.COL_GENE)
//                    		|| (column == COL_AGENT)
							|| (column == CGITableModel.COL_PUBMED)) {
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
        annotationTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
        annotationTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
        annotationTable.getColumnModel().getColumn(2).setHeaderValue("     Pathway");
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
        retrieveItem.addActionListener(retrieveActionListener);
        expandCollapseRetrieveAllMenu.add(retrieveItem);

        class NCIActionListener implements ActionListener {
			public void actionPerformed(ActionEvent actionEvent) {
				String address = "";
				if (selectedTable == diseaseTable) {
					address = EVS_PREFIX
							+ diseaseTable.getModel().getValueAt(selectedRow,
									CGITableModel.COL_EVSID);
				} else {
					address = EVS_PREFIX
							+ agentTable.getModel().getValueAt(selectedRow,
									CGITableModel.COL_EVSID);
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
						collapseAll();
			}
		}
        ActionListener collapseAllActionListener = new CollapseAllActionListener();
        JMenuItem collapseAllItem = new JMenuItem("collapse all");
        collapseAllItem.addActionListener(collapseAllActionListener);
        expandCollapseRetrieveAllMenu.add(collapseAllItem);

        class ExpandAllActionListener implements ActionListener {
			public void actionPerformed(ActionEvent actionEvent) {
				int size = ((CGITableModel)selectedTable.getModel()).size;
	            for (int j = 0; j < size; j++) {
					((CGITableModel) selectedTable.getModel()).expandBy(selectedTable,
							((GeneData) (((CGITableModel) selectedTable
									.getModel()).getObject(j,
											CGITableModel.COL_GENE))).name,
							((DiseaseData) (((CGITableModel) selectedTable
									.getModel()).getObject(j,
											CGITableModel.COL_DISEASE))).name);
	            }
	        	sortByNumberOfRecords=false;
	        	//sort by disease
	        	((SortableTableModel)selectedTable.getModel()).sortByColumn(2,true);
	        	//sort by marker
	        	((SortableTableModel)selectedTable.getModel()).sortByColumn(0,true);
			}
		}
        ActionListener expandAllActionListener = new ExpandAllActionListener();
        JMenuItem expandAllItem = new JMenuItem("expand all");
        expandAllItem.addActionListener(expandAllActionListener);
        expandCollapseRetrieveAllMenu.add(expandAllItem);

    }

    private void collapseAll(){

		        int size = ((CGITableModel)selectedTable.getModel()).size;
		        for (int j = 0; j < size; j++) {
					((CGITableModel) selectedTable.getModel())._collapseBy(selectedTable,
							((GeneData) (((CGITableModel) selectedTable
									.getModel()).getObject(j,
											CGITableModel.COL_GENE))).name,
							((DiseaseData) (((CGITableModel) selectedTable
									.getModel()).getObject(j,
											CGITableModel.COL_DISEASE))).name);
		        }
		//		sortByNumberOfRecords = true;
				((SortableTableModel)selectedTable.getModel()).sortByColumn(0,true);
				selectedTable.revalidate();
				selectedTable.repaint();
    }

    boolean entered = false;
    void syncHeaderWidthWithTableWidth(){
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
    	retrieveItem.setEnabled(false);
    	retrieveMarkerInfo.clear();
    	//TODO: If we support retrieve all for multiple markers,
    	//			We should remove retrieveMarker variable.
    	//		If we only allow retrieve all for one marker at a time,
    	//			We should remove retrieveMarkerInfo variable.
        retrieveMarker = marker;
        retrieveMarkerInfo.add(retrieveMarker);

        retrieveAllTask = new RetrieveAllTask(ProgressItem.BOUNDED_TYPE, "Connecting to server...", this);
        pd.executeTask(retrieveAllTask);
    }

	ProgressDialog pd = ProgressDialog.create(ProgressDialog.NONMODAL_TYPE);
    private CGITask cgiTask = null;
    private AnnotTask annotTask = null;
    private RetrieveAllTask retrieveAllTask = null;
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

        if (userAlsoWantCaBioData){
        		dropDownLists[2].removeAllItems();
        		dropDownLists[2].addItem("");
        		if (cgiTask != null && !cgiTask.isDone()){
        			cgiTask.cancel(true);
        			cgiTask = null;
        		}
                if (retrieveAllTask != null && !retrieveAllTask.isDone()){
                	retrieveAllTask.cancel(true);
                	retrieveAllTask = null;
                }
        		cgiTask = new CGITask(ProgressItem.BOUNDED_TYPE, "Connecting to server...", this);
        		pd.executeTask(cgiTask);
        }
        //block for retrieving CGI data ends
        //block for retrieving annotation data starts
        if (userAlsoWantPathwayData){
        		if (annotTask != null && !annotTask.isDone()){
        			annotTask.cancel(true);
        			annotTask = null;
        		}
        		annotTask = new AnnotTask(ProgressItem.BOUNDED_TYPE, "Connecting to server...", this);
        		pd.executeTask(annotTask);
        }

    }


    /*
     *
     */
    private void cgiClearButton_actionPerformed(ActionEvent e) {
        diseaseTable.setSortableModel(new CGITableModel(this));
        diseaseTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
        diseaseTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
        diseaseTable.getColumnModel().getColumn(2).setHeaderValue("     Disease");
        diseaseTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
        diseaseTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
        diseaseTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
        diseaseTable.getTableHeader().revalidate();
        agentTable.setSortableModel(new CGITableModel(this));
        agentTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
        agentTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
        agentTable.getColumnModel().getColumn(2).setHeaderValue("     Agent");
        agentTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
        agentTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
        agentTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
        agentTable.getTableHeader().revalidate();
        leftHeadPanel.setVisible(false);
        rightHeadPanel.setVisible(false);
        diseaseTableList.put(new Integer(maSet.hashCode()),  new CGITableModel(this));
        agentTableList.put(new Integer(maSet.hashCode()),  new CGITableModel(this));
    }

    /*
     *
     */
    private void exportButton_actionPerformed(ActionEvent e) {
		JFileChooser jFC=new JFileChooser();

		//We remove "all files" from filter, since we only allow CSV format
		FileFilter ft = jFC.getAcceptAllFileFilter();
		jFC.removeChoosableFileFilter(ft);

		TabularFileFilter filter = new TabularFileFilter();
        jFC.setFileFilter(filter);

	    //Save disease model to CSV file
        jFC.setDialogTitle("Save disease table");
		int returnVal = jFC.showSaveDialog(this.getComponent());
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
			String tabFilename;
			tabFilename = jFC.getSelectedFile().getAbsolutePath();
			if (!tabFilename.toLowerCase().endsWith(
					"." + filter.getExtension().toLowerCase())) {
				tabFilename += "." + filter.getExtension();
			}
			diseaseModel.toCSV(tabFilename);
		}

	    //Save agent model to CSV file
        jFC.setDialogTitle("Save agent table");
		returnVal = jFC.showSaveDialog(this.getComponent());
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
			String tabFilename;
			tabFilename = jFC.getSelectedFile().getAbsolutePath();
			if (!tabFilename.toLowerCase().endsWith(
					"." + filter.getExtension().toLowerCase())) {
				tabFilename += "." + filter.getExtension();
			}
			agentModel.toCSV(tabFilename);
		}
    }

    private void annotationExportButton_actionPerformed(ActionEvent e) {
		JFileChooser jFC=new JFileChooser();

		//We remove "all files" from filter, since we only allow CSV format
		FileFilter ft = jFC.getAcceptAllFileFilter();
		jFC.removeChoosableFileFilter(ft);

		TabularFileFilter filter = new TabularFileFilter();
        jFC.setFileFilter(filter);

	    //Save model to CSV file
        jFC.setDialogTitle("Save annotations table");
		int returnVal = jFC.showSaveDialog(this.getComponent());
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
			String tabFilename;
			tabFilename = jFC.getSelectedFile().getAbsolutePath();
			if (!tabFilename.toLowerCase().endsWith(
					"." + filter.getExtension().toLowerCase())) {
				tabFilename += "." + filter.getExtension();
			}
			annotationModel.toCSV(tabFilename);
		}
    }

    /*
     *
     */
    private void annoClearButton_actionPerformed(ActionEvent e) {
        annotationTable.setSortableModel(new AnnotationTableModel());
        annotationTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
        annotationTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
        annotationTable.getColumnModel().getColumn(2).setHeaderValue("     Pathway");
        annotationTable.getTableHeader().revalidate();
        annotationTableList.put(new Integer(maSet.hashCode()),  new AnnotationTableModel());
    }

    /*
     *
     */
    private void showPanels_actionPerformed(ActionEvent e) {
        if (selectedMarkerInfo == null || selectedMarkerInfo.size() == 0) {
            JOptionPane.showMessageDialog(cgiPanel.getParent(), "Please activate a marker set to retrieve annotations.");
        }else if((!userAlsoWantCaBioData)&&(!userAlsoWantPathwayData)){
			JOptionPane.showMessageDialog(cgiPanel.getParent(),
					" Please select a data type to retrieve. " + "( "
							+ RETRIEVE_CGI_DATA + " and/or "
							+ RETRIEVE_PATHWAY_DATA + " )");
        }else
        	showAnnotation();
    }

    /*
     *
     */
    void activateMarker(MarkerData markerData) {
        publishMarkerSelectedEvent(new MarkerSelectedEvent(markerData.marker));
    }

	@SuppressWarnings("rawtypes")
	@Publish
    org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(org.geworkbench.events.SubpanelChangedEvent event) {
        return event;
    }

    @Publish
    public MarkerSelectedEvent publishMarkerSelectedEvent(MarkerSelectedEvent event) {
        return event;
    }



    private JPanel mainPanel = new JPanel();
    JTabbedPane jTabbedPane1 = new JTabbedPane();



    /**
     * The Visual Component on which the annotation results are shown
     */
    JSplitPane cgiPanel = null;
    /**
     * Visual Widget
     */
    JPanel annotationPanel = new JPanel();	//for annotation
    private JScrollPane jScrollPane1 = new JScrollPane(); //for disease
    private JScrollPane jScrollPane2 = new JScrollPane(); //for agent

    /**
     * Visual Widget
     */
    SortableTable diseaseTable;
    CGITableModel diseaseModel;
    SortableTable agentTable;
    CGITableModel agentModel;
    SortableTable annotationTable;
    AnnotationTableModel annotationModel;

    HashMap<Integer, AnnotationTableModel> annotationTableList;
    HashMap<Integer, CGITableModel> diseaseTableList;
    HashMap<Integer, CGITableModel> agentTableList;

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
    private JComboBox annoHumanOrMouseComboBox = new JComboBox(Human_Mouse);
    //GUIs used by CGI panel
    private JButton cgiRetrieveButton = new JButton();
    JButton cgiClearButton = new JButton();
    private JCheckBox cgiRetrieveCaBioCheckBox = new JCheckBox(RETRIEVE_CGI_DATA);
    private JCheckBox cgiRetrievePathwayCheckBox = new JCheckBox(RETRIEVE_PATHWAY_DATA);
    private JComboBox cgiHumanOrMouseComboBox = new JComboBox(Human_Mouse);
    private JButton exportButton = new JButton();
    private JButton annotationExportButton = new JButton();


    DSItemList<DSGeneMarker> selectedMarkerInfo = null;
    DSItemList<DSGeneMarker> retrieveMarkerInfo = new CSItemList<DSGeneMarker>();
    DSGeneMarker retrieveMarker = null;
    GeneSearchCriteria criteria = null;
    Pathway[] pathways = new Pathway[0];

    DSMicroarraySet  maSet = null;

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    @Subscribe
    public void receive(GeneSelectorEvent e, Object source) {
        if (maSet != null && e.getPanel() != null) {
			DSPanel<DSGeneMarker> markerPanel = e.getPanel().activeSubset();
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
    @SuppressWarnings("rawtypes")
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
        	annotationModel = annotationTableList.get(new Integer(hashcode));
        	annotationTable.setSortableModel(annotationModel);
            annotationTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            annotationTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            annotationTable.getColumnModel().getColumn(2).setHeaderValue("     Pathway");
            annotationTable.getTableHeader().revalidate();
        }
        else
        {
        	annotationTable.setSortableModel(new AnnotationTableModel());
            annotationTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            annotationTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            annotationTable.getColumnModel().getColumn(2).setHeaderValue("     Pathway");
            annotationTable.getTableHeader().revalidate();
        }

        if (diseaseTableList.containsKey(new Integer(hashcode)))
        {
        	diseaseModel = diseaseTableList.get(new Integer(hashcode));
        	diseaseTable.setSortableModel(diseaseModel);
        	diseaseTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            diseaseTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            diseaseTable.getColumnModel().getColumn(2).setHeaderValue("     Disease");
            diseaseTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
            diseaseTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
            diseaseTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
            diseaseTable.getTableHeader().revalidate();
        }
        else
        {
        	diseaseTable.setSortableModel(new CGITableModel(this));
        	diseaseTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            diseaseTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            diseaseTable.getColumnModel().getColumn(2).setHeaderValue("     Disease");
            diseaseTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
            diseaseTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
            diseaseTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
            diseaseTable.getTableHeader().revalidate();
        }

        if (agentTableList.containsKey(new Integer(hashcode)))
        {
        	agentModel = agentTableList.get(new Integer(hashcode));
        	agentTable.setSortableModel(agentModel);
        	agentTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            agentTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            agentTable.getColumnModel().getColumn(2).setHeaderValue("     Disease");
            agentTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
            agentTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
            agentTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
            agentTable.getTableHeader().revalidate();
        }
        else
        {
        	agentTable.setSortableModel(new CGITableModel(this));
        	agentTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            agentTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            agentTable.getColumnModel().getColumn(2).setHeaderValue("     Disease");
            agentTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
            agentTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
            agentTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
            agentTable.getTableHeader().revalidate();
        }

    	  if (svgStringListMap.containsKey(new Integer(hashcode)))
            svgStringList = svgStringListMap.get(new Integer(hashcode));
    	  else
    	  {
    		  pathwayComboBox.removeAllItems();
    		  pathwayList.clear();
    		  svgStringList.clear();
    	  }

    	  if (pathwayListMap.containsKey(new Integer(hashcode)))
    	  {
    		  int selectIndex = pathwayComboItemSelectedMap.get(new Integer(hashcode));

              pathwayList = pathwayListMap.get(new Integer(hashcode));
              pathwayComboBox.setModel(new DefaultComboBoxModel(pathwayList.toArray()));
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
        pathwayComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	pathwayComboBox_actionPerformed(e);
            }
        });


        clearDiagramButton.setForeground(Color.black);
        clearDiagramButton.setFocusPainted(true);
        clearDiagramButton.setText("Clear Diagram");
        clearDiagramButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearDiagramButton_actionPerformed(e);
            }
        });

        clearHistButton.setForeground(Color.black);
        clearHistButton.setFocusPainted(true);
        clearHistButton.setText("Clear History");
        clearHistButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	clearHistButton_actionPerformed(e);
            }
        });

        imagePathwayButton.setForeground(Color.black);
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

        createSvgCanvas();

        svgStringList = new HashMap<String, String>();
        pathwayList = new ArrayList<String>();
        svgStringListMap = new HashMap<Integer, HashMap<String, String>>();
        pathwayListMap = new HashMap<Integer, ArrayList<String>>();
        pathwayComboItemSelectedMap = new HashMap<Integer, Integer>();
        tabPanelSelectedMap = new HashMap<Integer, Integer>();
    }

    private LinkActivationListener linkListener = new LinkActivationListener() {
        public void linkActivated(LinkActivationEvent lae) {
            svgCanvas_linkActivated(lae);
        }
    };

    private GVTTreeRendererListener renderListener = new GVTTreeRendererAdapter() {
    	public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
    		//set canvas viewbox size to document size
    		svgCanvas.revalidate();
        }
    };

    private void createSvgCanvas()
    {
    	svgCanvas = new JSVGCanvas(new UserAgent(), true, true);
        svgCanvas.addLinkActivationListener(linkListener);
        svgCanvas.addGVTTreeRendererListener(renderListener);
        jscrollPanePathway.getViewport().add(svgCanvas, null);
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
    		pathwayComboBox.removeItem(pathwayName);
    		if (pathwayComboBox.getItemCount()>0) pathwayComboBox.setSelectedIndex(pathwayList.size()-1);
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

    HashMap<String, String> svgStringList = null;
    private HashMap<Integer, HashMap<String, String>> svgStringListMap = null;
    ArrayList<String> pathwayList = null;
    private HashMap<Integer, ArrayList<String>> pathwayListMap = null;
    private HashMap<Integer,Integer> tabPanelSelectedMap = null;
    private HashMap<Integer,Integer> pathwayComboItemSelectedMap = null;



    private int oldHashCode = 0;


    JPanel pathwayPanel = new JPanel();
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
    private JSVGCanvas svgCanvas = null;
    private static final String SVG_BASE_URL = "http://cgap.nci.nih.gov/";




    org.geworkbench.util.annotation.Pathway pathway = null;


    /**
     * Wrapper method for setting the <code>SVGDocument</code> received
     * from the <code>Pathway</code> objects obtained from a caBIO search
     *
     * @param svgString SVG document returned from a caBIO search as a String
     */
    private void setSvg(String svgString) {
    	if (svgCanvas == null || svgCanvas.getGraphics() == null)
    		return;
        if (svgString != null) {
            StringReader reader = new StringReader(svgString);
            Document document = null;
           	ClassLoader currentContextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
            	Thread.currentThread().setContextClassLoader(SAXSVGDocumentFactory.class.getClassLoader());
                String parser = XMLResourceDescriptor.getXMLParserClassName();
                SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
                document = f.createDocument(null, reader);
            } catch (IOException ex) {
            	log.error(ex);
            } finally {
                Thread.currentThread().setContextClassLoader(currentContextClassLoader);
            }

            createSvgCanvas();
            svgCanvas.setDocument(document);
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

	private class TabularFileFilter extends FileFilter {
		public String getDescription() {
			return "CSV Files";
		}

		public boolean accept(File f) {
			String name = f.getName();
			boolean tabFile = name.endsWith("csv") || name.endsWith("CSV");
			if (f.isDirectory() || tabFile) {
				return true;
			}

			return false;
		}

		public String getExtension() {
			return "csv";
		}

	}
	
	void clearTable(String type)
	{
	    if (type.equals("annot")) {
	    	annotationModel = new AnnotationTableModel();
            annotationTableList.put(new Integer(maSet.hashCode()), annotationModel);
        	annotationTable.setSortableModel(annotationModel);
            annotationTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            annotationTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            annotationTable.getColumnModel().getColumn(2).setHeaderValue("     Pathway");
            annotationTable.getTableHeader().revalidate();
	    } 
	    else if (type.equals("cgi")) {
	    	diseaseModel = new CGITableModel(AnnotationsPanel2.this);
            diseaseTableList.put(new Integer(maSet.hashCode()), diseaseModel);
	    	diseaseTable.setSortableModel(diseaseModel);
        	diseaseTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            diseaseTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            diseaseTable.getColumnModel().getColumn(2).setHeaderValue("     Disease");
            diseaseTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
            diseaseTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
            diseaseTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
            diseaseTable.getTableHeader().revalidate();
	    	
	    	agentModel = new CGITableModel(AnnotationsPanel2.this);
            agentTableList.put(new Integer(maSet.hashCode()), agentModel);
        	agentTable.setSortableModel(agentModel);
        	agentTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            agentTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            agentTable.getColumnModel().getColumn(2).setHeaderValue("     Agent");
            agentTable.getColumnModel().getColumn(3).setHeaderValue("     Role");
            agentTable.getColumnModel().getColumn(4).setHeaderValue("     Sentence");
            agentTable.getColumnModel().getColumn(5).setHeaderValue("     Pubmed");
            agentTable.getTableHeader().revalidate();
	    }
	}

	void orderDropDownLists(JComboBox dropDownLists){
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

}
