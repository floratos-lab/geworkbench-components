package org.geworkbench.components.alignment.synteny;

import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.config.events.EventSource;
import org.geworkbench.events.ProjectEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;



/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author Califano Lab
 * @version 1.0
 */

public class SyntenyMapViewAppComponent extends EventSource
    implements VisualPlugin, MenuListener {

  JPanel jPanel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  HashMap listeners = new HashMap();
  SyntenyMapViewWidget smViewWidget = new SyntenyMapViewWidget();

  /**
   * Default Constructor. Creates a component at application at startup time on
   * encountering it's reference in a configuration file as specified by the
   * <code>component.configuration.file</code> system property.
   */
  public SyntenyMapViewAppComponent(){
    try {
         jbInit();
     } catch (Exception ex) {
         ex.printStackTrace();
     }
  }

  /**
   * Utility method that composes the visual aspects of this component
   * @throws Exception exception thrown during visual composition
   */
  private void jbInit() throws Exception{
    jPanel1.setLayout(borderLayout1);
    jPanel1.add(smViewWidget);
  }

  /**
   * <code>MenuListener</code> interface method that returns the appropriate
   * <code>ActionListener</code> instance for use with application wide
   * <code>MenuItem</code>s. Actions on a <code>MenuItem</code> are forwarded to
   * all registered components and every component handles them in their own
   * context
   * @param key String
   * @return ActionListener
   */
  public ActionListener getActionListener(String key){
    return (ActionListener)listeners.get(key);
  }

  /**
   * <code>VisulPlugin</code> interface method that returns the visual
   * representation of this component as required by the application framework
   * @return Component visual representation of this component
   */
  public Component getComponent(){
    return jPanel1;
  }

    /**
     * <code>ProjectListener</code> interface method. This is an application
     * specific <code>Listener</code> that is used for communicating between
     * components. Other predefined listeners are available in the
     * <code>geaw.components.events</code> package or in the
     * <code>org.bioworks.components.listeners</code> package. A creator of a
     * component can readily create new listeners by extending the
     * <code>core.config.events.AppEventListener</code> interface.
     * @param e ProjectEvent The <code>Event</code> instance that is propogated
     * to this component using the application messaging framework
     */
    public void receiveProjectSelection(ProjectEvent e) {
        // Handle the selection or loading of a new dataset in the application
        // in the context of this component
    }
}
