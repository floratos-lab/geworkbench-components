package org.geworkbench.components.annotations;

import org.geworkbench.events.AnnotationsEvent;
import org.geworkbench.events.MarkerPanelEvent;
import org.geworkbench.events.SingleMarkerEvent;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.util.annotation.*;
import org.geworkbench.util.annotation.Pathway;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Component responsible for displaying Gene Annotation obtained from caBIO
 * Displays data in a Tabular format with 2 columns. The first column contains
 * The Gene Discription and the second column contains a list of known Pathways
 * that this gene's product participates in.
 *
 * @author First Genetic Trust
 * @version 1.0
 * @(#)AnnotationsPanel.java	1.0 06/02/03
 */
public class AnnotationsPanel implements VisualPlugin {

    /**
     * Default Constructor
     */
    public AnnotationsPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        annotationsPanel.setLayout(borderLayout1);
        showPanels.setText("Use Panels");
        showPanels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanels_actionPerformed(e);
            }

        });
        checkBoxPanel.setLayout(gridLayout1);
        showPathways.setText("Show Pathways");
        showPathways.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPathways_actionPerformed(e);
            }

        });
        sortByPathway.setText("Sort By Pathway");
        sortByPathway.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sortByPathway_actionPerformed(e);
            }

        });
        sortByPathway.setEnabled(sPathways);
        annotationsPanel.add(jScrollPane1, BorderLayout.CENTER);
        annotationsPanel.add(checkBoxPanel, BorderLayout.SOUTH);
        checkBoxPanel.add(showPanels, null);
        checkBoxPanel.add(showPathways, null);
        jEditorPane1.setEditable(false);
        jEditorPane1.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                editorPaneClicked(e);
            }

        });
        jScrollPane1.getViewport().add(jEditorPane1, null);
        checkBoxPanel.add(sortByPathway, null);
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
        return annotationsPanel;
    }

    /**
     * Interface <code>MarkerPanelListener</code> method that uses Panel
     * selections in the <code>MarkerPanel</code> plugin to set selected
     * <code>MarkerInfo</code> for annotation display.
     *
     * @param mpe <code>MarkerPanelEvent</code> received from
     *            <code>MarkerPanel</code> plugin
     */
    @Subscribe public void receive(org.geworkbench.events.MarkerPanelEvent mpe, Object source) {
        /** @todo Will miss the microarray set. Must implement the project selection panel */
        //DSMicroarraySet maSet = ((GenericMarkerSelectorPanel)mpe.getSource()).getMicroarrays();
        CSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>();
        //maView.setMA(maSet);
        maView.useMarkerPanel(true);
        maView.setMarkerPanel(mpe.getPanels());
        selectedMarkerInfo = maView.markers();
    }

    /**
     * Performs caBIO queries and constructs HTML display of the results
     */
    private void showAnnotation() {
        if (criteria == null) {
            try {
                criteria = new GeneSearchCriteriaImpl();
            } catch (Exception e) {
                System.out.println("Exception: could not create caBIO search criteria in Annotation Panel");
                return;
            }
        }
        pathways = new Pathway[0];
        if (!showMarkers) {
            if (singleMarker != null) {
                criteria.setSearchName(singleMarker.getLabel());
                criteria.search();
                geneAnnotation = "<html><body>";
                GeneAnnotation[] annotations = criteria.getGeneAnnotations();
                geneAnnotation += "<table border=\"1\" cellspacing=\"0\" " + "cellpadding=\"2\">";
                for (int j = 0; j < annotations.length; j++) {
                    Pathway[] pways = annotations[j].getPathways();
                    Pathway[] temp = new Pathway[pathways.length + pways.length];
                    System.arraycopy(pathways, 0, temp, 0, pathways.length);
                    System.arraycopy(pways, 0, temp, pathways.length, pways.length);
                    pathways = temp;
                    //System.arraycopy(pathways, 0, pways, 0, pways.length);
                    //geneAnnotation +=
                    //  "<table width=\"90%\" border=\"1\" cellspacing=\"0\" "
                    //+ "cellpadding=\"2\"><tr valign=\"top\">";
                    geneAnnotation += "<tr valign=\"top\"><td rowspan=\"" + pways.length + "\"><a href=" + annotations[j].getGeneURL() + "\">" + annotations[j].getGeneName() + "</a></td>";
                    for (int k = 0; k < pways.length; k++) {
                        if (k > 0) {
                            geneAnnotation += "<tr>";
                            geneAnnotation += "<td><a href=\"http://caBIOPathways/" + pways[k].getPathwayName() + "\">" + pways[k].getPathwayName() + "</a></td></tr>";
                        } else {
                            if (pways.length > 0) {
                                geneAnnotation += "<td><a href=\"http://caBIOPathways/" + pways[k].getPathwayName() + "\">" + pways[k].getPathwayName() + "</a></td></tr>";
                            }

                        }

                    }

                    if (pways.length == 0) {
                        geneAnnotation += "<td>&nbsp;</td>";
                    }
                    //geneAnnotation += "</tr>";
                }

                geneAnnotation += "</table>";
                geneAnnotation += "</body></html>";
            }

        } else {
            if (selectedMarkerInfo != null) {
                geneAnnotation = "<html><body>";
                geneAnnotation += "<table border=\"1\" cellspacing=\"0\" " + "cellpadding=\"2\">";
                for (int i = 0; i < selectedMarkerInfo.size(); i++) {
                    criteria.setSearchName(selectedMarkerInfo.get(i).getLabel());
                    criteria.search();
                    GeneAnnotation[] annotations = criteria.getGeneAnnotations();
                    for (int j = 0; j < annotations.length; j++) {
                        Pathway[] pways = annotations[j].getPathways();
                        Pathway[] temp = new org.geworkbench.util.annotation.Pathway[pathways.length + pways.length];
                        System.arraycopy(pathways, 0, temp, 0, pathways.length);
                        System.arraycopy(pways, 0, temp, pathways.length, pways.length);
                        pathways = temp;
                        //geneAnnotation +=
                        //  "<table width=\"90%\" border=\"1\" cellspacing=\"0\" "
                        //+ "cellpadding=\"2\"><tr valign=\"top\">";
                        geneAnnotation += "<tr valign=\"top\"><td rowspan=\"" + pways.length + "\"><a href=" + annotations[j].getGeneURL() + "\">" + annotations[j].getGeneName() + "</a></td>";
                        for (int k = 0; k < pways.length; k++) {
                            if (k > 0) {
                                //geneAnnotation += "<tr>";
                                geneAnnotation += "<td><a href=\"http://caBIOPathways/" + pways[k].getPathwayName() + "\">" + pways[k].getPathwayName() + "</a></td></tr>";
                            } else {
                                if (pways.length > 0) {
                                    geneAnnotation += "<td><a href=\"http://caBIOPathways/" + pways[k].getPathwayName() + "\">" + pways[k].getPathwayName() + "</a></td></tr>";
                                }

                            }

                        }

                        if (pways.length == 0) {
                            geneAnnotation += "<td>&nbsp;</td></tr>";
                        }
                        //geneAnnotation += "</table>";
                    }

                }

                geneAnnotation += "</table>";
                geneAnnotation += "</body></html>";
            }

        }

        if (geneAnnotation != null) {
            if (geneAnnotation.equalsIgnoreCase("<html><body><table border=\"1\" " + "cellspacing=\"0\" cellpadding=\"2\">" + "<tr valign=\"top\"></table></body></html>")) {
                JOptionPane.showMessageDialog(annotationsPanel, "No annotation information obtained from caBIO for selected genes", "Empty search", JOptionPane.INFORMATION_MESSAGE);
            }

            jEditorPane1.setContentType("text/html");
            jEditorPane1.setText(geneAnnotation);
        }

        annotationsPanel.revalidate();
    }

    private void showPathways() {
    }

    /**
     * Interface <code>MarkerPanelListener</code> method
     *
     * @param event <code>SingleMarkerEvent</code> containing a marker selection
     */
    @Subscribe public void receive(SingleMarkerEvent event, Object source) {
        if (showMarkers) {
            showPanels.setSelected(false);
            showMarkers = false;
            jEditorPane1.setContentType("text/plain");
            jEditorPane1.setText("");
            annotationsPanel.revalidate();
        }

        singleMarker = event.getMarker();
        showAnnotation();
    }

    private void showPanels_actionPerformed(ActionEvent e) {
        showMarkers = ((JCheckBox) showPanels).isSelected();
        if (showMarkers) {
            showAnnotation();
        } else {
            jEditorPane1.setContentType("text/plain");
            jEditorPane1.setText("");
            annotationsPanel.revalidate();
        }

    }

    private void showPathways_actionPerformed(ActionEvent e) {
        sPathways = !sPathways;
        showPathways();
        sortByPathway.setEnabled(sPathways);
    }

    private void sortByPathway_actionPerformed(ActionEvent e) {
        sortPathways = !sortPathways;
        showPathways();
    }

    @Publish public AnnotationsEvent publishAnnotationsEvent(AnnotationsEvent event) {
        return event;
    }

    private void editorPaneClicked(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            URL url = e.getURL();
            String host = url.getHost();
            if (GeneAnnotationImpl.PREFIX_USED.indexOf(host) != -1) {
                try {
                    Runtime.getRuntime().exec(System.getProperty("browser.path") + " " + url.toString());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

            } else {
                String target = url.getPath();
                for (int i = 0; i < pathways.length; i++) {
                    if (target.equals("/" + pathways[i].getPathwayName())) {
                        publishAnnotationsEvent(new AnnotationsEvent("Pathway Selected", pathways[i]));
                    }

                }

            }

        }

    }

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
    private JEditorPane jEditorPane1 = new JEditorPane("text/html", "");
    private String geneAnnotation = null;
    /**
     * Visual Widget
     */
    private JPanel checkBoxPanel = new JPanel();
    /**
     * Visual Widget
     */
    private JCheckBox showPanels = new JCheckBox();
    private DSItemList<DSGeneMarker> selectedMarkerInfo = null;
    private DSGeneMarker singleMarker = null;
    private GeneSearchCriteria criteria = null;
    private boolean showMarkers = false;
    private boolean sPathways = false;
    private boolean sortPathways = false;
    private Pathway[] pathways = new Pathway[0];
    /**
     * Visual Widget
     */
    private GridLayout gridLayout1 = new GridLayout();
    /**
     * Visual Widget
     */
    private JCheckBox showPathways = new JCheckBox();
    /**
     * Visual Widget
     */
    private JCheckBox sortByPathway = new JCheckBox();
}
