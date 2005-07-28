package org.geworkbench.components.versioninfo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;


/**
 * <p>Title: caWorkbench</p>
 * <p/>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p/>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 3.0
 */
public class VersionInfoComponent implements org.geworkbench.engine.config.MenuListener {
    //Holds refrences to listeners of menu items for this component.
    private HashMap listeners = new HashMap();

    public VersionInfoComponent() {
        //Register menu items listener - sessions dialog
        ActionListener allSession = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                helpAbout();
            }
        };
        listeners.put("Help.About", allSession);
    }

    /**
     * Display the About box.
     */

    void helpAbout() {
        VersionInfoDialog dlg = new VersionInfoDialog();
        dlg.setLocationRelativeTo(null);
        dlg.setModal(true);
        dlg.show();

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

}
