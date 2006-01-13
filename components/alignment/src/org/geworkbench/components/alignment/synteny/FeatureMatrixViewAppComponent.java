package org.geworkbench.components.alignment.synteny;

import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.config.events.EventSource;
import org.geworkbench.events.SequenceDiscoveryTableEvent;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

public class FeatureMatrixViewAppComponent
    extends EventSource
    implements VisualPlugin,
    MenuListener, PropertyChangeListener {

  EventListenerList listenerList = new EventListenerList();
  FeatureMatrixViewWidget fmViewWidget;
  HashMap listeners = new HashMap();
  ActionListener listener = null;

    public FeatureMatrixViewAppComponent() {

    fmViewWidget=new FeatureMatrixViewWidget();
    fmViewWidget.addPropertyChangeListener(this);

  //  New dotmatrix arrives;
    listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jNewFeatureMatrix_actionPerformed(e);
      }
    };

    listeners.put("Processing new feature matrix", listener);
    listeners.put("File.Open.File", listener);
  }

  public void sequenceDiscoveryTableRowSelected(SequenceDiscoveryTableEvent e) {
  }

  public Component getComponent() {
    return fmViewWidget;
  }

  public ActionListener getActionListener(String var) {

    return (ActionListener) getListeners().get(var);

  } //implementation of core.config.MenuListener interface

/*  public void receiveProjectSelection(ProjectEvent e) {
    EventSource source = e.getSource();
    if (source instanceof ProjectSelection) {
      ProjectSelection selection = (ProjectSelection) source;
      DataSet dataFile = selection.getDataSet();
      if (dataFile instanceof CSSequenceSet) {
//        sViewWidget.setSequenceDB( (CSSequenceSet) dataFile);
      }
    }
  }
  */

  public void propertyChange(PropertyChangeEvent e) {
    String propertyName = e.getPropertyName();
  }

  void jNewFeatureMatrix_actionPerformed(ActionEvent e) {
    fmViewWidget = new FeatureMatrixViewWidget();

    }

 //   public Object getComponent(){ return this; }

  public HashMap getListeners() {
    return listeners;
  }
}
