package org.geworkbench.components.alignment.grid.session;

/**
 * <p>Title: Bioworks</p>
 *
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 *
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 *
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */


import org.geworkbench.components.alignment.grid.CreateGridServiceDialog;
import org.geworkbench.util.session.LoginPanelModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class GridSessionAppComponent implements org.geworkbench.engine.config.MenuListener {
    //Holds refrences to listeners of menu items for this component.
    private HashMap listeners = new HashMap();
    LoginPanelModel loginPanelModel = new LoginPanelModel();

    public GridSessionAppComponent() {
        //Register menu items listener - sessions dialog
        ActionListener allSession = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAllSessionDialog(e);
            }
        };
        listeners.put("Commands.GridSessions.ViewAll", allSession);
    }

    /**
     * Return a listener which registered with the var string.
     *
     * @param var - the name of the listener
     * @return - the listener
     */
    public ActionListener getActionListener(String var) {
        return (ActionListener) listeners.get(var);
    }

    /**
     * Display the sessions dialog.
     *
     * @param e
     */
    private void showAllSessionDialog(ActionEvent e) {
        CreateGridServiceDialog csd = new CreateGridServiceDialog(null, "grid service");
        /* SessionsViewDialog viewer =
             new SessionsViewDialog(new SessionsViewController(loginPanelModel));
         viewer.setVisible(true); */
    }

}

