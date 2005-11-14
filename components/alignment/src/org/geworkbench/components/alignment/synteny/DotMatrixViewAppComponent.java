package org.geworkbench.components.alignment.synteny;

import java.beans.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.HashMap;


import org.geworkbench.engine.config.events.EventSource;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.config.MenuListener;


/**
 * <p>DotMatrixViewAppComponent controls all notification and communication for DotMatrixViewWidget</p>
 * <p>Loads FASTA file </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Califano Lab </p>
 * @author
 * @version 1.0
 */

public class DotMatrixViewAppComponent extends EventSource implements VisualPlugin, MenuListener, PropertyChangeListener {

    EventListenerList listenerList = new EventListenerList();
    DotMatrixViewWidget dmViewWidget;
    HashMap listeners = new HashMap();
    ActionListener listener = null;

    public DotMatrixViewAppComponent() {

        dmViewWidget = new DotMatrixViewWidget();

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getComponentName() {
            //        return dmViewWidget;
            return "Dot Matrix";
    }

    public Component getComponent() {
        return dmViewWidget;
    }

    public ActionListener getActionListener(String var) {
        return (ActionListener) getListeners().get(var);
    } //implementation of core.config.MenuListener interface

/*    public void receiveProjectSelection(ProjectEvent e) {
        EventSource source = e.getSource();
        if (source instanceof ProjectSelection) {
            ProjectSelection selection = (ProjectSelection) source;
            DataSet dataFile = selection.getDataSet();
            if (dataFile instanceof SequenceDB) {
            }
        }
    }
*/
    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
    }

//  void jNewDotMatrix_actionPerformed(ActionEvent e) {
//        dmViewWidget = new DotMatrixViewWidget();
//    }

    public HashMap getListeners() {
        return listeners;
    }

    private void jbInit() throws Exception {

    }
}
