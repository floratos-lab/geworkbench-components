/*
 * Jalview - A Sequence Alignment Editor and Viewer
 * Copyright (C) 2007 AM Waterhouse, J Procter, G Barton, M Clamp, S Searle
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package jalview.ws;

import javax.swing.*;

import ext.vamsas.*;
import jalview.datamodel.*;
import jalview.gui.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class MsaWSClient
    extends WSClient
{
  /**
   * server is a WSDL2Java generated stub for an archetypal MsaWSI service.
   */
  ext.vamsas.MuscleWS server;
  AlignFrame alignFrame;

  /**
   * Creates a new MsaWSClient object that uses a service
   * given by an externally retrieved ServiceHandle
   *
   * @param sh service handle of type AbstractName(MsaWS)
   * @param altitle DOCUMENT ME!
   * @param msa DOCUMENT ME!
   * @param submitGaps DOCUMENT ME!
   * @param preserveOrder DOCUMENT ME!
   */

  public MsaWSClient(ext.vamsas.ServiceHandle sh, String altitle,
                     jalview.datamodel.AlignmentView msa,
                     boolean submitGaps, boolean preserveOrder,
                     Alignment seqdataset,
                     AlignFrame _alignFrame)
  {
    super();
    alignFrame = _alignFrame;
    if (!sh.getAbstractName().equals("MsaWS"))
    {
      JOptionPane.showMessageDialog(Desktop.desktop,
                                    "The Service called \n" + sh.getName() +
                                    "\nis not a \nMultiple Sequence Alignment Service !",
                                    "Internal Jalview Error",
                                    JOptionPane.WARNING_MESSAGE);

      return;
    }

    if ( (wsInfo = setWebService(sh)) == null)
    {
      JOptionPane.showMessageDialog(Desktop.desktop,
                                    "The Multiple Sequence Alignment Service named " +
                                    sh.getName() +
                                    " is unknown", "Internal Jalview Error",
                                    JOptionPane.WARNING_MESSAGE);

      return;
    }
    startMsaWSClient(altitle, msa, submitGaps, preserveOrder, seqdataset);

  }

  private void startMsaWSClient(String altitle, AlignmentView msa,
                                boolean submitGaps, boolean preserveOrder,
                                Alignment seqdataset)
  {
    if (!locateWebService())
    {
      return;
    }

    wsInfo.setProgressText( ( (submitGaps) ? "Re-alignment" : "Alignment") +
                           " of " + altitle + "\nJob details\n");
    String jobtitle = WebServiceName.toLowerCase();
    if (jobtitle.endsWith("alignment"))
    {
      if (submitGaps
          && (!jobtitle.endsWith("realignment")
              || jobtitle.indexOf("profile") == -1))
      {
        int pos = jobtitle.indexOf("alignment");
        jobtitle = WebServiceName.substring(0, pos) + "re-alignment of " +
            altitle;
      }
      else
      {
        jobtitle = WebServiceName + " of " + altitle;
      }
    }
    else
    {
      jobtitle = WebServiceName + (submitGaps ? " re" : " ") + "alignment of " +
          altitle;
    }

    MsaWSThread msathread = new MsaWSThread(server, WsURL, wsInfo, alignFrame,
                                            WebServiceName,
                                            jobtitle,
                                            msa,
                                            submitGaps, preserveOrder,
                                            seqdataset);
    wsInfo.setthisService(msathread);
    msathread.start();
  }

  /**
   * Initializes the server field with a valid service implementation.
   *
   * @return true if service was located.
   */
  private boolean locateWebService()
  {
    // TODO: MuscleWS transmuted to generic MsaWS client
    MuscleWSServiceLocator loc = new MuscleWSServiceLocator(); // Default

    try
    {
      this.server = (MuscleWS) loc.getMuscleWS(new java.net.URL(WsURL));
      ( (MuscleWSSoapBindingStub)this.server).setTimeout(60000); // One minute timeout
    }
    catch (Exception ex)
    {
      wsInfo.setProgressText("Serious! " + WebServiceName +
                             " Service location failed\nfor URL :" + WsURL +
                             "\n" +
                             ex.getMessage());
      wsInfo.setStatus(WebserviceInfo.ERROR);
      ex.printStackTrace();

      return false;
    }

    loc.getEngine().setOption("axis", "1");

    return true;
  }

  protected String getServiceActionKey()
  {
    return "MsaWS";
  }

  protected String getServiceActionDescription()
  {
    return "Multiple Sequence Alignment";
  }
}
