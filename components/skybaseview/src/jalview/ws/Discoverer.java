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

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: Dundee University</p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.util.*;

import javax.swing.*;

import ext.vamsas.*;

public class Discoverer
    extends Thread implements Runnable
{
  ext.vamsas.IRegistry registry; // the root registry service.
  private java.beans.PropertyChangeSupport changeSupport = new java.beans.
      PropertyChangeSupport(this);

  /**
   * change listeners are notified of "services" property changes
   *
   * @param listener to be added that consumes new services Hashtable object.
   */
  public void addPropertyChangeListener(
      java.beans.PropertyChangeListener listener)
  {
    changeSupport.addPropertyChangeListener(listener);
  }

  /**
   *
   *
   * @param listener to be removed
   */
  public void removePropertyChangeListener(
      java.beans.PropertyChangeListener listener)
  {
    changeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Property change listener firing routine
   *
   * @param prop services
   * @param oldvalue old services hash
   * @param newvalue new services hash
   */
  public void firePropertyChange(String prop, Object oldvalue, Object newvalue)
  {
    changeSupport.firePropertyChange(prop, oldvalue, newvalue);
  }

  /**
   * Initializes the server field with a valid service implementation.
   *
   * @return true if service was located.
   */
  private IRegistry locateWebService(java.net.URL WsURL)
  {
    IRegistryServiceLocator loc = new IRegistryServiceLocator(); // Default
    IRegistry server = null;
    try
    {
      server = loc.getRegistryService(WsURL);
      ( (RegistryServiceSoapBindingStub) server).setTimeout(60000); // One minute timeout
    }
    catch (Exception ex)
    {
      jalview.bin.Cache.log.error(
          "Serious!  Service location failed\nfor URL :" + WsURL +
          "\n", ex);

      return null;
    }

    loc.getEngine().setOption("axis", "1");

    return server;
  }

  static private java.net.URL RootServiceURL = null;
  static public Vector ServiceURLList = null;
  static private boolean reallyDiscoverServices = true;

  public static java.util.Hashtable services = null; // vectors of services stored by abstractServiceType string
  public static java.util.Vector serviceList = null; // flat list of services
  static private Vector getDiscoveryURLS()
  {
    Vector urls = new Vector();
    String RootServiceURLs = jalview.bin.Cache.getDefault("DISCOVERY_URLS",
        "http://www.compbio.dundee.ac.uk/JalviewWS/services/ServiceRegistry");

    try
    {
      StringTokenizer st = new StringTokenizer(RootServiceURLs, ",");
      while (st.hasMoreElements())
      {
        String url = null;
        try
        {
          java.net.URL u = new java.net.URL(url = st.nextToken());
          if (!urls.contains(u))
          {
            urls.add(u);
          }
          else
          {
            jalview.bin.Cache.log.info(
                "Ignoring duplicate url in DISCOVERY_URLS list");
          }
        }
        catch (Exception ex)
        {
          jalview.bin.Cache.log.warn(
              "Problem whilst trying to make a URL from '" +
              ( (url != null) ? url : "<null>") + "'");
          jalview.bin.Cache.log.warn(
              "This was probably due to a malformed comma separated list"
              + " in the DISCOVERY_URLS entry of $(HOME)/.jalview_properties)");
          jalview.bin.Cache.log.debug("Exception was ", ex);
        }
      }
    }
    catch (Exception ex)
    {
      jalview.bin.Cache.log.warn(
          "Error parsing comma separated list of urls in DISCOVERY_URLS.", ex);
    }
    if (urls.size() > 0)
    {
      return urls;
    }
    return null;
  }

  /**
   * fetch new services or reset to hardwired defaults depending on preferences.
   */
  static public void doDiscovery()
  {
    jalview.bin.Cache.log.debug("(Re)-Initialising the discovery URL list.");
    try
    {
      reallyDiscoverServices = jalview.bin.Cache.getDefault("DISCOVERY_START", false);
      if (reallyDiscoverServices)
      {
        ServiceURLList = getDiscoveryURLS();
      }
      else
      {
        jalview.bin.Cache.log.debug("Setting default services");
        services = new Hashtable();
        // Muscle, Clustal and JPred.
        ServiceHandle[] defServices =
            {
            new ServiceHandle(
                "MsaWS",
                "Edgar, Robert C. (2004), MUSCLE: multiple sequence alignment " +
                "with high accuracy and high throughput, Nucleic Acids Research 32(5), 1792-97.",
                "http://www.compbio.dundee.ac.uk/JalviewWS/services/MuscleWS",
                "Muscle Multiple Protein Sequence Alignment"
            ),
            new ServiceHandle(
                "MsaWS",
                "Katoh, K., K. Kuma, K., Toh, H.,  and Miyata, T. (2005) " +
                "\"MAFFT version 5: improvement in accuracy of multiple sequence alignment.\"" +
                " Nucleic Acids Research, 33 511-518",
                "http://www.compbio.dundee.ac.uk/JalviewWS/services/MafftWS",
                "MAFFT Multiple Sequence Alignment"),
            new ServiceHandle(
                "MsaWS",
                "Thompson, J.D., Higgins, D.G. and Gibson, T.J. (1994) CLUSTAL W: improving the sensitivity of progressive multiple" +
                " sequence alignment through sequence weighting, position specific gap penalties and weight matrix choice." +
                " Nucleic Acids Research, 22 4673-4680",
                "http://www.compbio.dundee.ac.uk/JalviewWS/services/ClustalWS",
                "ClustalW Multiple Sequence Alignment"),
            new ServiceHandle(
                "SecStrPred",
                "Cuff J. A and Barton G.J (2000) Application of " +
                "multiple sequence alignment profiles to improve protein secondary structure prediction, " +
                "Proteins 40:502-511",
                "http://www.compbio.dundee.ac.uk/JalviewWS/services/jpred",
                "JNet Secondary Structure Prediction"
            )};
        services = new Hashtable();
        serviceList = new Vector();
        buildServiceLists(defServices, serviceList, services);
      }

    }
    catch (Exception e)
    {
      System.err.println(
          "jalview.rootRegistry is not a proper url!\nWas set to " +
          RootServiceURL + "\n" + e);
    }

  }

  // TODO: JBPNote : make this discover more services based on list of
  // discovery service urls, break cyclic references to the same url and
  // duplicate service entries (same endpoint *and* same interface)
  private ServiceHandle[] getServices(java.net.URL location)
  {
    ServiceHandles shs = null;
    try
    {
      jalview.bin.Cache.log.debug("Discovering services using " + location);
      shs = locateWebService(location).getServices();
    }
    catch (org.apache.axis.AxisFault f)
    {
      // JBPNote - should do this a better way!
      if (f.getFaultReason().indexOf("(407)") > -1)
      {
        if (jalview.gui.Desktop.desktop != null)
        {
          JOptionPane.showMessageDialog(jalview.gui.Desktop.desktop, "Please set up your proxy settings in the 'Connections' tab of the Preferences window",
                                        "Proxy Authorization Failed",
                                        JOptionPane.WARNING_MESSAGE);
        }
      }
      else
      {
        jalview.bin.Cache.log.warn("No Discovery service at " +
                                   location);
        jalview.bin.Cache.log.debug("Axis Fault", f);
      }
    }
    catch (Exception e)
    {
      jalview.bin.Cache.log.warn("No Discovery service at " +
                                 location);
      jalview.bin.Cache.log.debug("Discovery Service General Exception", e);
    }
    if ( (shs != null) && shs.getServices().length > 0)
    {
      return shs.getServices();
    }
    return null;
  }

  /**
   * Adds a list of services to the service catalog and categorised catalog
   * returns true if ServiceURLList was modified with a new DiscoveryService URL
   * @param sh ServiceHandle[]
   * @param cat Vector
   * @param sscat Hashtable
   * @return boolean
   */
  static private boolean buildServiceLists(ServiceHandle[] sh, Vector cat,
                                           Hashtable sscat)
  {
    boolean seenNewDiscovery = false;
    for (int i = 0, j = sh.length; i < j; i++)
    {
      if (!cat.contains(sh[i]))
      {
        jalview.bin.Cache.log.debug("A " + sh[i].getAbstractName() +
                                    " service called " +
                                    sh[i].getName() + " exists at " +
                                    sh[i].getEndpointURL() + "\n");
        if (!sscat.containsKey(sh[i].getAbstractName()))
        {
          sscat.put(sh[i].getAbstractName(), cat = new Vector());
        }
        else
        {
          cat = (Vector) sscat.get(sh[i].getAbstractName());
        }
        cat.add(sh[i]);
        if (sh[i].getAbstractName().equals("Registry"))
        {
          for (int s = 0, sUrls = ServiceURLList.size(); s < sUrls; s++)
          {
            java.net.URL disc_serv = null;
            try
            {
              disc_serv = new java.net.URL(sh[i].getEndpointURL());
              if (!ServiceURLList.contains(disc_serv))
              {
                jalview.bin.Cache.log.debug(
                    "Adding new discovery service at " + disc_serv);
                ServiceURLList.add(disc_serv);
                seenNewDiscovery = true;
              }
            }
            catch (Exception e)
            {
              jalview.bin.Cache.log.debug(
                  "Ignoring bad discovery service URL " + sh[i].getEndpointURL(),
                  e);
            }
          }
        }
      }
    }
    return seenNewDiscovery;
  }

  public void discoverServices()
  {
    Hashtable sscat = new Hashtable();
    Vector cat = new Vector();
    ServiceHandle sh[] = null;
    int s_url = 0;
    if (ServiceURLList == null)
    {
      jalview.bin.Cache.log.debug(
          "No service endpoints to use for service discovery.");
      return;
    }
    while (s_url < ServiceURLList.size())
    {
      if ( (sh = getServices( (java.net.URL) ServiceURLList.get(s_url))) != null)
      {

        buildServiceLists(sh, cat, sscat);
      }
      else
      {
        jalview.bin.Cache.log.warn(
            "No services at "
            + ( (java.net.URL) ServiceURLList.get(s_url))
            + " - check DISCOVERY_URLS property in .jalview_properties");
      }
      s_url++;
    }
    // TODO: decide on correct semantics for services list - PropertyChange
    // provides a way of passing the new object around
    // so no need to access original discovery thread.
    // Curent decision is to change properties then notify listeners with old and new values.
    Hashtable oldServices = services;
    //Vector oldServicelist = serviceList;
    services = sscat;
    serviceList = cat;
    firePropertyChange("services", oldServices, services);
  }

  /**
   * creates a new thread to call discoverServices()
   */
  public void run()
  {
    final Discoverer discoverer = this;
    Thread discoverThread = new Thread()
    {
      public void run()
      {
        discoverer.doDiscovery();
        discoverer.discoverServices();
      }
    };
    discoverThread.start();
  }
}
