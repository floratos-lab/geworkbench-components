package org.geworkbench.components.alignment.synteny;

import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.config.events.EventSource;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;


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
