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

import ext.vamsas.*;
import jalview.gui.*;

public class WSClient
{
  /**
   * MsaWSClient
   *
   * @param msa SequenceI[]
   */
  protected String WebServiceName;
  protected String WebServiceJobTitle;
  protected String WebServiceReference;
  protected String WsURL;
  protected WebserviceInfo wsInfo;
  int jobsRunning = 0;
  /**
   * mappings between abstract interface names and menu entries
   */
  protected java.util.Hashtable ServiceActions;
  {
    ServiceActions = new java.util.Hashtable();
    ServiceActions.put("MsaWS", "Multiple Sequence Alignment");
    ServiceActions.put("SecStrPred", "Secondary Structure Prediction");
  };
  public WSClient()
  {
  }

  protected WebserviceInfo setWebService(ServiceHandle sh)
  {
    WebServiceName = sh.getName();
    if (ServiceActions.containsKey(sh.getAbstractName()))
    {
      WebServiceJobTitle = sh.getName(); // TODO: control sh.Name specification properly
      // add this for short names. +(String) ServiceActions.get(sh.getAbstractName());
    }
    else
    {
      WebServiceJobTitle = sh.getAbstractName() + " using " + sh.getName();

    }
    WebServiceReference = sh.getDescription();
    WsURL = sh.getEndpointURL();
    WebserviceInfo wsInfo = new WebserviceInfo(WebServiceJobTitle,
                                               WebServiceReference);

    return wsInfo;
  }
}
