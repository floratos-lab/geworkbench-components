package org.geworkbench.components.annotations;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.svg.LinkActivationEvent;
import org.apache.batik.swing.svg.LinkActivationListener;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.util.XMLResourceDescriptor;
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
import org.geworkbench.util.CsvFileFilter;
import org.geworkbench.util.ProgressDialog;
import org.geworkbench.util.ProgressItem;
import org.jfree.ui.SortableTable;
import org.w3c.dom.Document;

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
    private static final String RETRIEVE_INFORMATION = "Retrieve Annotations";

	private static final String[] Human_Mouse= {"Human","Mouse"};
    private static final String[] Human_Mouse_Code= {"Hs","Mm"};
    /**
     * Web URL prefix for obtaining Gene annotation
     */
    static final String GeneCards_PREFIX = "http://www.genecards.org/cgi-bin/carddisp.pl?gene=";

	private static Log log = LogFactory.getLog(AnnotationsPanel2.class);
    
    String humanOrMouse = Human_Mouse_Code[0];	//default to Human

    /**
     * Default Constructor
     */
    public AnnotationsPanel2() {
        annotationModel = new AnnotationTableModel();
        annotationTable = new SortableTable(annotationModel);
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        annotationTableList = new HashMap<Integer, AnnotationTableModel>();

        svgStringListMap = new HashMap<Integer, HashMap<String, String>>();
        pathwayListMap = new HashMap<Integer, ArrayList<String>>();
        tabPanelSelectedMap = new HashMap<Integer, Integer>();
        pathwayComboItemSelectedMap = new HashMap<Integer, Integer>();
    }

    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {

    	mainPanel.setLayout( new GridLayout());
        mainPanel.add(jTabbedPane1);
        
        //Init button panel in annotation panel
        //GUIs used by Annotation panel
        JButton annoRetrieveButton = new JButton();
        JButton annoClearButton = new JButton();
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

        JButton annotationExportButton = new JButton();
        annotationExportButton.setForeground(Color.black);
        annotationExportButton.setToolTipText("Export to CSV files");
        annotationExportButton.setFocusPainted(true);
        annotationExportButton.setText("Export");
        annotationExportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	annotationExportButton_actionPerformed(e);
            }
        });
        
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
            }
        };
        
        JComboBox annoHumanOrMouseComboBox = new JComboBox(Human_Mouse);
        JPanel annoButtonPanel = new JPanel();
        annoButtonPanel.add(annoHumanOrMouseComboBox);
        annoHumanOrMouseComboBox.addActionListener(HumanMouseListener);
        annoButtonPanel.add(annoRetrieveButton);
        annoButtonPanel.add(annoClearButton);
        annoButtonPanel.add(annotationExportButton);

        annotationTable.getTableHeader().setPreferredSize(new Dimension(0, 25));

        JPanel annotationPanel = new JPanel();	//for annotation
        annotationPanel.setLayout(new BorderLayout());
        annotationPanel.add(new JScrollPane(annotationTable),BorderLayout.CENTER);
        annotationPanel.add(annoButtonPanel, BorderLayout.SOUTH);

        jTabbedPane1.add("Annotations", annotationPanel);
        jTabbedPane1.add("Pathway", pathwayPanel);

        jbInitPathways();

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

	final ProgressDialog pd = ProgressDialog.create(ProgressDialog.NONMODAL_TYPE);

    private AnnotTask annotTask = null;

    private void annotationExportButton_actionPerformed(ActionEvent e) {
		JFileChooser jFC=new JFileChooser();

		//We remove "all files" from filter, since we only allow CSV format
		FileFilter ft = jFC.getAcceptAllFileFilter();
		jFC.removeChoosableFileFilter(ft);

		CsvFileFilter filter = new CsvFileFilter();
        jFC.setFileFilter(filter);

	    //Save model to CSV file
        jFC.setDialogTitle("Save annotations table");
		int returnVal = jFC.showSaveDialog(this.getComponent());
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
			String tabFilename;
			tabFilename = jFC.getSelectedFile().getAbsolutePath();
			if (!tabFilename.toLowerCase().endsWith(".csv")) {
				tabFilename += ".csv";
			}
			annotationModel.toCSV(tabFilename);
		}
    }

    private void annoClearButton_actionPerformed(ActionEvent e) {
        annotationTable.setSortableModel(new AnnotationTableModel());
        annotationTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
        annotationTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
        annotationTable.getColumnModel().getColumn(2).setHeaderValue("     Pathway");
        annotationTable.getTableHeader().revalidate();
        annotationTableList.put(new Integer(maSet.hashCode()),  new AnnotationTableModel());
    }

    private void showPanels_actionPerformed(ActionEvent e) {
        if (selectedMarkerInfo == null || selectedMarkerInfo.size() == 0) {
            JOptionPane.showMessageDialog(jTabbedPane1, "Please activate a marker set to retrieve annotations.");
        } else {
        	pathways = new Pathway[0];

    		if (annotTask != null && !annotTask.isDone()) {
    			annotTask.cancel(true);
    			annotTask = null;
    		}
    		annotTask = new AnnotTask(ProgressItem.BOUNDED_TYPE,
    				"Connecting to server...", this);
    		pd.executeTask(annotTask);
        }
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

    final private JPanel mainPanel = new JPanel();
    final JTabbedPane jTabbedPane1 = new JTabbedPane();

    final SortableTable annotationTable;
    AnnotationTableModel annotationModel;

    final HashMap<Integer, AnnotationTableModel> annotationTableList;

    DSItemList<DSGeneMarker> selectedMarkerInfo = null;

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

        if ( maSet != null ) {
        	hashcode = maSet.hashCode();
        }

        if (oldHashCode == 0)
        {
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
        } else {
        	annotationTable.setSortableModel(new AnnotationTableModel());
            annotationTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
            annotationTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
            annotationTable.getColumnModel().getColumn(2).setHeaderValue("     Pathway");
            annotationTable.getTableHeader().revalidate();
        }

    	if (svgStringListMap.containsKey(new Integer(hashcode))) {
            svgStringList = svgStringListMap.get(new Integer(hashcode));
    	} else {
    		  pathwayComboBox.removeAllItems();
    		  pathwayList.clear();
    		  svgStringList.clear();
    	}

    	if (pathwayListMap.containsKey(new Integer(hashcode))) {
    		  int selectIndex = pathwayComboItemSelectedMap.get(new Integer(hashcode));

              pathwayList = pathwayListMap.get(new Integer(hashcode));
              pathwayComboBox.setModel(new DefaultComboBoxModel(pathwayList.toArray()));
    		  pathwayComboBox.setSelectedIndex(selectIndex);
    		  pathwayComboBox.revalidate();
    	}

    	if (tabPanelSelectedMap.containsKey(new Integer(hashcode))) {
    		  jTabbedPane1.setSelectedIndex(tabPanelSelectedMap.get(new Integer(hashcode)));
    	} else {
    		  jTabbedPane1.setSelectedIndex(0);
    	}

    	if ( maSet != null) {
    	    oldHashCode = maSet.hashCode();
    	}
    }

    /* initialize pathwayPanel */
    private void jbInitPathways() throws Exception {

        pathwayPanel.setLayout(new BorderLayout());
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

        JButton clearDiagramButton = new JButton();
        clearDiagramButton.setForeground(Color.black);
        clearDiagramButton.setFocusPainted(true);
        clearDiagramButton.setText("Clear Diagram");
        clearDiagramButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearDiagramButton_actionPerformed(e);
            }
        });

        JButton clearHistButton = new JButton();
        clearHistButton.setForeground(Color.black);
        clearHistButton.setFocusPainted(true);
        clearHistButton.setText("Clear History");
        clearHistButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	clearHistButton_actionPerformed(e);
            }
        });

        JButton imagePathwayButton = new JButton();
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
        pathwayTool.add(Box.createVerticalStrut(8));
        pathwayTool.add(imagePathwayButton);

        createSvgCanvas();

        svgStringList = new HashMap<String, String>();
        pathwayList = new ArrayList<String>();
    }

    final private LinkActivationListener linkListener = new LinkActivationListener() {
    	@Override
        public void linkActivated(LinkActivationEvent lae) {
            svgCanvas_linkActivated(lae);
        }
    };

    final private GVTTreeRendererListener renderListener = new GVTTreeRendererAdapter() {
    	@Override
    	public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
    		//set canvas viewbox size to document size
    		svgCanvas.revalidate();
        }
    };

    private void createSvgCanvas()
    {
    	svgCanvas = new JSVGCanvas(svgUserAgent, true, true);
        svgCanvas.addLinkActivationListener(linkListener);
        svgCanvas.addGVTTreeRendererListener(renderListener);
        jscrollPanePathway.getViewport().add(svgCanvas, null);
    }

    private void pathwayComboBox_actionPerformed(ActionEvent e) {

    	Object pathwayName = pathwayComboBox.getSelectedItem();
    	if (pathwayName != null && !pathwayName.toString().trim().equals("")) {
    		setSvg(svgStringList.get(pathwayName));
    	    jTabbedPane1.setTitleAt(jTabbedPane1.indexOfComponent(pathwayPanel), pathwayName.toString());
    	} else {
    	    try{
    	    	svgCanvas.setDocument(null);
    	    } catch(IllegalStateException ex) {
    	    	log.error(ex,ex);
    	    }
    		svgCanvas.revalidate();
            pathwayPanel.revalidate();
            jTabbedPane1.setTitleAt(jTabbedPane1.indexOfComponent(pathwayPanel),"Pathway");
    	}
    }

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

    @Publish 
    public ImageSnapshotEvent createImageSnapshot() {
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
    ArrayList<String> pathwayList = null;
    final private HashMap<Integer, HashMap<String, String>> svgStringListMap;
    final private HashMap<Integer, ArrayList<String>> pathwayListMap;
    final private HashMap<Integer,Integer> tabPanelSelectedMap;
    final private HashMap<Integer,Integer> pathwayComboItemSelectedMap;

    private int oldHashCode = 0;

    final JPanel pathwayPanel = new JPanel();
    final private SVGUserAgent svgUserAgent = new SVGUserAgentImpl(pathwayPanel);
    private JToolBar pathwayTool = new JToolBar();
    final JComboBox pathwayComboBox = new JComboBox();

    /**
     * Visual Widget
     */
    private final JScrollPane jscrollPanePathway = new JScrollPane();

    /**
     * <code>Canvas</code> on which the Pathway SVG image is drawn
     */
    private JSVGCanvas svgCanvas = null;
    private static final String SVG_BASE_URL = "http://cgap.nci.nih.gov/";

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

        try {
            log.debug("Opening " + uri);
            org.geworkbench.util.BrowserLauncher.openURL(uri);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

	void clearTable()
	{
	    annotationModel = new AnnotationTableModel();
	    annotationTableList.put(new Integer(maSet.hashCode()), annotationModel);
	    annotationTable.setSortableModel(annotationModel);
	    annotationTable.getColumnModel().getColumn(0).setHeaderValue("     Marker");
	    annotationTable.getColumnModel().getColumn(1).setHeaderValue("     Gene");
	    annotationTable.getColumnModel().getColumn(2).setHeaderValue("     Pathway");
	    annotationTable.getTableHeader().revalidate();
	}

}
