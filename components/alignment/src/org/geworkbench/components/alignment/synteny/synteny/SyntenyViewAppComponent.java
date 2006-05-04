package org.geworkbench.components.alignment.synteny;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.config.events.EventSource;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.SyntenyEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
@AcceptTypes({DSMicroarraySet.class}) public class SyntenyViewAppComponent extends EventSource
    implements VisualPlugin, MenuListener {
    public SyntenyPresentationsList SPList = null;
    public SynMapPresentationList SMPList = null;
    public SyntenyAnnotationParameters SAP = null;
    public SyntenyAnnotationParameters SMAP = null;

    JPanel jPanel1 = new JPanel();
    HashMap listeners = new HashMap();
    BorderLayout borderLayout1 = new BorderLayout();
    JTabbedPane jTabbedPane1 = new JTabbedPane();

    SyntenyMapViewWidget SMVW = null;
    DotMatrixViewWidget DMVW = null;

    public SyntenyViewAppComponent() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception {

        SyntenyMapViewWidget SMVW = new SyntenyMapViewWidget();
        DotMatrixViewWidget DMVW = new DotMatrixViewWidget();

        SPList = new SyntenyPresentationsList();
        SMPList = new SynMapPresentationList();
        SAP = new SyntenyAnnotationParameters();
        SPList.setSyntenyAnnotationParameters(SAP);
        SMPList.setSyntenyAnnotationParameters(SAP);
        jPanel1.setLayout(borderLayout1);
        jPanel1.add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        jTabbedPane1.add(DMVW,"Dotmatrix");
        jTabbedPane1.add(SMVW,"Synteny Map");
    }

    public Component getComponent(){
        return jPanel1;
}
    public ActionListener getActionListener(String key){
      return (ActionListener)listeners.get(key);
  }

  @Subscribe public void SyntenyEvent(SyntenyEvent e, Object o) {
  String[] splt = e.getText().split("\t");

  if(splt[0].charAt(0) == 'D'){
      System.out.println("\nDoing dotMatrix");
      int f_x = Integer.parseInt(splt[2]);
      int t_x = Integer.parseInt(splt[3]);
      int f_y = Integer.parseInt(splt[4]);
      int t_y = Integer.parseInt(splt[5]);
      SPList.addAndDisplay(splt[1], f_x, t_x, f_y, t_y,DMVW);
  }
  if(splt[0].charAt(0) == 'S'){
      System.out.println("\nDoing synMap");
      int f_x = Integer.parseInt(splt[2]);
      int t_x = Integer.parseInt(splt[3]);
      int f_y = Integer.parseInt(splt[4]);
      int t_y = Integer.parseInt(splt[5]);
      SMPList.addAndDisplay(splt[1], f_x, t_x, f_y, t_y,SMVW);
  }
  if(splt[0].charAt(0) == 'A'){
      SPList.redrawAnnotation(splt[1]);
  }
}

}


