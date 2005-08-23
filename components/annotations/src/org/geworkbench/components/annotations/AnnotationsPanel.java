package org.geworkbench.components.annotations;

import org.geworkbench.events.*;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.util.annotation.*;
import org.geworkbench.util.annotation.Pathway;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.BrowserLauncher;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author manjunath at genomecenter dot columbia dot edu
 * @version 1.0
 *
 * Component responsible for displaying Gene Annotation obtained from caBIO
 * Displays data in a Tabular format with 2 columns. The first column contains
 * The Gene Discription and the second column contains a list of known Pathways
 * that this gene's product participates in.
 *
 */

public class AnnotationsPanel implements VisualPlugin {

 /**
  * Default Constructor
  */
  public AnnotationsPanel() {
    try {
      jbInit();
    }

    catch(Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Configures the Graphical User Interface and Listeners
   * @throws Exception
   */
  private void jbInit() throws Exception {
    annotationsPanel.setLayout(borderLayout1);
    showPanels.setHorizontalAlignment(SwingConstants.CENTER);
    showPanels.setText("Retrieve annotations");
    showPanels.setToolTipText("Retrieve gene and pathway information for markers in activated panels");
    showPanels.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        showPanels_actionPerformed(e);
      }

    });
    checkBoxPanel.setLayout(borderLayout2);
    clearButton.setForeground(Color.black);
    clearButton.setToolTipText("");
    clearButton.setFocusPainted(true);
    clearButton.setText("Clear");
    clearButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            clearButton_actionPerformed(e);
        }
    });
    annotationsPanel.add(jScrollPane1, BorderLayout.CENTER);
    annotationsPanel.add(checkBoxPanel,  BorderLayout.SOUTH);
    checkBoxPanel.add(showPanels,  BorderLayout.WEST);
    checkBoxPanel.add(clearButton,  BorderLayout.EAST);
    jEditorPane1.setEditable(false);
    jEditorPane1.addHyperlinkListener(new HyperlinkListener(){
      public void hyperlinkUpdate(HyperlinkEvent e){
        editorPaneClicked(e);
      }

    });
    jScrollPane1.getViewport().add(jEditorPane1, null);
  }

  /**
   * Interface <code>VisualPlugin</code> method that returns a
   * <code>Component</code> which is the visual representation of
   * the this plugin.
   * @return <code>Component</code> visual representation of
   * <code>AnnotationsPanel</code>
   */
  public Component getComponent(){
    return annotationsPanel;
  }

  /**
   * Performs caBIO queries and constructs HTML display of the results
   */
  private void showAnnotation(){
      if(criteria == null) {
          try {
              criteria = new GeneSearchCriteriaImpl();
          } catch (Exception e) {
              System.out.println("Exception: could not create caBIO search criteria in Annotation Panel");
              return;
          }
      }
    pathways = new Pathway[0];
    try {
        Runnable query = new Runnable(){
            public void run(){
                ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                if (!showMarkers) {
                  if (singleMarker != null) {
                    pb.setTitle("Querying caBIO..");
                    pb.start();
                    pb.setMessage("Getting Marker Annotation..");
                    criteria.setSearchName(singleMarker.getLabel());
                    criteria.search();
                    geneAnnotation = "<html><body>";
                    GeneAnnotation[] annotations = criteria.getGeneAnnotations();
                    geneAnnotation +=
                        "<table border=\"1\" cellspacing=\"0\" "
                        + "cellpadding=\"2\">";
                    for (int j = 0; j < annotations.length; j++) {
                      pb.setMessage("Getting Pathways..");
                      Pathway[] pways = annotations[j].getPathways();
                      Pathway[] temp = new Pathway[pathways.length + pways.length];
                      System.arraycopy(pathways, 0, temp, 0, pathways.length);
                      System.arraycopy(pways, 0, temp, pathways.length, pways.length);
                      pathways = temp;
                      //System.arraycopy(pathways, 0, pways, 0, pways.length);
                      //geneAnnotation +=
                      //  "<table width=\"90%\" border=\"1\" cellspacing=\"0\" "
                      //+ "cellpadding=\"2\"><tr valign=\"top\">";
                      geneAnnotation += "<tr valign=\"top\"><td rowspan=\""
                          + pways.length
                          + "\"><a href="
                          + annotations[j].getGeneURL()
                          + "\">" + annotations[j].getGeneName() + "</a></td>";
                      for (int k = 0; k < pways.length; k++) {
                        if (k > 0) {
                          geneAnnotation += "<tr>";
                          geneAnnotation +=
                              "<td><a href=\"http://caBIOPathways/"
                              + pways[k].getPathwayName()
                              + "\">" + pways[k].getPathwayName() + "</a></td></tr>";
                        }
                        else {
                          if (pways.length > 0) {
                            geneAnnotation +=
                                "<td><a href=\"http://caBIOPathways/"
                                + pways[k].getPathwayName()
                                + "\">" + pways[k].getPathwayName() + "</a></td></tr>";
                          }
                        }
                      }
                      if (pways.length == 0)
                        geneAnnotation += "<td>&nbsp;</td>";
                      //geneAnnotation += "</tr>";
                    }
                    geneAnnotation += "</table>";
                    geneAnnotation += "</body></html>";
                    pb.stop();
                    pb.dispose();
                  }
                }
                else {
                  if (selectedMarkerInfo != null) {
                    pb.setTitle("Querying caBIO..");
                    pb.start();
                    geneAnnotation = "<html><body>";
                    geneAnnotation +=
                        "<table border=\"1\" cellspacing=\"0\" "
                        + "cellpadding=\"2\">";
                    for (int i = 0; i < selectedMarkerInfo.size(); i++) {
                      criteria.setSearchName(selectedMarkerInfo.get(i).getLabel());
                      pb.setMessage("Getting Marker Annotation and Pathways: " + selectedMarkerInfo.get(i).getLabel());
                      criteria.search();
                      GeneAnnotation[] annotations = criteria.getGeneAnnotations();
                      for (int j = 0; j < annotations.length; j++) {
                        Pathway[] pways = annotations[j].getPathways();
                        Pathway[] temp = new Pathway[pathways.length + pways.length];
                        System.arraycopy(pathways, 0, temp, 0, pathways.length);
                        System.arraycopy(pways, 0, temp, pathways.length, pways.length);
                        pathways = temp;
                        //geneAnnotation +=
                        //  "<table width=\"90%\" border=\"1\" cellspacing=\"0\" "
                        //+ "cellpadding=\"2\"><tr valign=\"top\">";
                        geneAnnotation += "<tr valign=\"top\"><td rowspan=\""
                            + pways.length
                            + "\"><a href="
                            + annotations[j].getGeneURL()
                            + "\">" + annotations[j].getGeneName() + "</a></td>";
                        for (int k = 0; k < pways.length; k++) {
                          if (k > 0) {
                            //geneAnnotation += "<tr>";
                            geneAnnotation +=
                                "<td><a href=\"http://caBIOPathways/"
                                + pways[k].getPathwayName()
                                + "\">" + pways[k].getPathwayName() + "</a></td></tr>";
                          }
                          else {
                            if (pways.length > 0) {
                              geneAnnotation +=
                                  "<td><a href=\"http://caBIOPathways/"
                                  + pways[k].getPathwayName()
                                  + "\">" + pways[k].getPathwayName() + "</a></td></tr>";
                            }
                          }
                        }
                        if (pways.length == 0)
                          geneAnnotation += "<td>&nbsp;</td></tr>";
                          //geneAnnotation += "</table>";
                      }
                    }
                    pb.stop();
                    pb.dispose();
                    geneAnnotation += "</table>";
                    geneAnnotation += "</body></html>";
                  }
                }
                if (geneAnnotation != null) {
                  pb.setMessage("Creating HTML table..");
                  if (geneAnnotation.equalsIgnoreCase("<html><body><table border=\"1\" " +
                                                      "cellspacing=\"0\" cellpadding=\"2\">" +
                                                      "<tr valign=\"top\"></table></body></html>")) {
                    JOptionPane.showMessageDialog(annotationsPanel,
                        "No annotation information obtained from caBIO for selected genes",
                        "Empty search",
                        JOptionPane.INFORMATION_MESSAGE);
                  }
                  jEditorPane1.setContentType("text/html");
                  jEditorPane1.setText(geneAnnotation);
                }
                annotationsPanel.revalidate();
            }
        };
        Thread t = new Thread(query);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }
    catch (Exception e){e.printStackTrace();}
  }

  private void clearButton_actionPerformed(ActionEvent e){
      jEditorPane1.setContentType("text/plain");
      jEditorPane1.setText("");
      annotationsPanel.revalidate();
  }

  private void showPanels_actionPerformed(ActionEvent e){
      showAnnotation();
  }

  private void editorPaneClicked(HyperlinkEvent e){
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      URL url = e.getURL();
      String host = url.getHost();
      if (GeneAnnotationImpl.PREFIX_USED.indexOf(host) != -1){
        try {
            BrowserLauncher.openURL(url.toString());
        }
        catch (IOException ioe) { ioe.printStackTrace(); }
      }
      else {
        String target = url.getPath();
        for (int i = 0; i < pathways.length; i++) {
          if (target.equals("/" + pathways[i].getPathwayName())) {
              publishAnnotationsEvent(new AnnotationsEvent("Pathway Selected",
                                              pathways[i]));
          }
        }
      }
    }
  }

  @Publish public AnnotationsEvent publishAnnotationsEvent(AnnotationsEvent ae){
      return ae;
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
  private JButton showPanels = new JButton();
  private DSItemList<DSGeneMarker> selectedMarkerInfo = null;
  private DSGeneMarker singleMarker = null;
  private GeneSearchCriteria criteria = null;
  private boolean showMarkers = true;
  private Pathway[] pathways = new Pathway[0];

  private DSMicroarraySet maSet = null;
  BorderLayout borderLayout2 = new BorderLayout();
  JButton clearButton = new JButton();

  /**
   * geneSelectorAction
   *
   * @param e GeneSelectorEvent
   */
  @Subscribe public void receive(GeneSelectorEvent e, Object source) {
      if (maSet != null && e.getPanel() != null){
          DSMicroarraySetView<DSGeneMarker,DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(maSet);
          maView.setMarkerPanel(e.getPanel());
          maView.useMarkerPanel(true);
          selectedMarkerInfo = maView.markers();
      }
  }

  /**
   * receiveProjectSelection
   *
   * @param e ProjectEvent
   */
  @Subscribe public void receive(ProjectEvent e, Object source) {
      DSDataSet data = e.getDataSet();
      if (data != null && data instanceof DSMicroarraySet)
          maSet = (DSMicroarraySet)data;
  }
}
