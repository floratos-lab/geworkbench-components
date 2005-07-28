package org.geworkbench.components.alignment.grid;

import org.geworkbench.util.session.dialog.SessionChooser;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class CreateGridServiceDialog extends SessionChooser {
    public CreateGridServiceDialog() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int returnValue = CANCEL_OPTION;
    private ServiceDialog dialog = null;
    public static int CONNECT_OPTION = 1;


    public CreateGridServiceDialog(Frame frame, String title, ServiceDataModel sd) {
        //System.out.println("Create GridService");
        dialog = new ServiceDialog(frame, title, sd, true);
        show();
        //System.out.println("Create GridService");
    }

    public CreateGridServiceDialog(Frame frame, String title) {
        ServiceDataModel sdm = new ServiceDataModel();
        dialog = new ServiceDialog(frame, title, sdm, true);
        show();
        //System.out.println("Create GridService");
    }


    public int show() {
        returnValue = CANCEL_OPTION;

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                returnValue = CANCEL_OPTION;
            }
        });

        dialog.show();
        int ret = dialog.getReturnValue();

        if (ret == CONNECT_OPTION) {
            returnValue = APPROVE_OPTION;
            /*   host = dialog.getHostName();
               this.port = dialog.getPortNum();
               this.userName = dialog.getUserName();
               char[] pWord = dialog.getPassWord();
               if(pWord != null){
                 this.passWord = new char[pWord.length];
                  System.arraycopy(pWord, 0, this.passWord, 0, pWord.length);
               }
               else{
                 this.passWord = new char[0];
               }
               this.sessionName = dialog.getSessionName();
                  }
              */
        }

        dialog.setVisible(true);
        // dialog.dispose();
        //dialog = null;
        return returnValue;
    }

    private void jbInit() throws Exception {
    }

}
