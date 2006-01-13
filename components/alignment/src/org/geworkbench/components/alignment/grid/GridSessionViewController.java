package org.geworkbench.components.alignment.grid;

import org.geworkbench.engine.config.events.Event;
import org.geworkbench.util.session.LoginPanelModel;
import org.geworkbench.util.session.SessionsViewController;





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
public class GridSessionViewController
    extends SessionsViewController {


    public GridSessionViewController(LoginPanelModel lModel) {
        super(lModel, null);
    }


    public void listAllSessions(){
        return;
    }


    /**
  * throws an application event
  * @param c
  * @param method
  * @param evt
  */








 public void generateEvent(Class c, String method,
                             Event evt) {
//   try {
//     throwEvent(c, method, evt);
//   }
//   catch (Exception ex) {
//     System.out.println("generateEvent failed: " +
//                        "[class=" + c + " method=" + method + "evt=" + evt +
//                        "]");
//     ex.printStackTrace();
//   }
}

}
