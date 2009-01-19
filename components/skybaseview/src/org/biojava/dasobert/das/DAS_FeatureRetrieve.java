/**
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 19.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.dasobert.das;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.xml.parsers.*;

import org.xml.sax.*;

/**
 * A class to perform a DAS features request
 *
 * @author Andreas Prlic
 *
 */
public class DAS_FeatureRetrieve
{

  List features;
  Logger logger;
  int comeBackLater;
  URL url;
  /**
   * @param url the URL the features should be downloaded from
   *
   */
  public DAS_FeatureRetrieve(URL url)
  {
    super();

    logger = Logger.getLogger("org.biojava.spice");
    features = new ArrayList();
    comeBackLater = -1;
    this.url = url;
    reload();
  }

  /** contact the DAS-feature server again. Usually
   * it is not necessary to call this again, because the constructor already does, but
   * if comeBackLater > -1 this should be called again.
   *
   */
  public void reload()
  {

    try
    {

      InputStream dasInStream = null;
      try
      {
        dasInStream = open(url);
      }
      catch (Exception e)
      {
        comeBackLater = -1;
        System.out.println("NO RESPONSE FROM " + url);
        logger.log(Level.FINE, "could not open connection to " + url, e);
        return;
      }

      SAXParserFactory spfactory =
          SAXParserFactory.newInstance();

      spfactory.setValidating(false);

      SAXParser saxParser = null;

      try
      {
        saxParser =
            spfactory.newSAXParser();
      }
      catch (ParserConfigurationException e)
      {
        e.printStackTrace();
      }

      String vali = System.getProperty("XMLVALIDATION");

      boolean validation = false;
      if (vali != null)
      {
        if (vali.equals("true"))
        {
          validation = true;
        }
      }

      XMLReader xmlreader = saxParser.getXMLReader();

      //XMLReader xmlreader = XMLReaderFactory.createXMLReader();
      try
      {
        xmlreader.setFeature("http://xml.org/sax/features/validation",
                             validation);
      }
      catch (SAXException e)
      {
        logger.log(Level.FINE, "Cannot set validation " + validation);
      }

      try
      {
        xmlreader.setFeature(
            "http://apache.org/xml/features/nonvalidating/load-external-dtd",
            validation);
      }
      catch (SAXNotRecognizedException e)
      {
        e.printStackTrace();
        logger.log(Level.FINE, "Cannot set load-external-dtd " + validation);

      }

      DAS_Feature_Handler cont_handle = new DAS_Feature_Handler();
      cont_handle.setDASCommand(url.toString());
      xmlreader.setContentHandler(cont_handle);
      xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
      InputSource insource = new InputSource();
      insource.setByteStream(dasInStream);

      try
      {
        xmlreader.parse(insource);
        features = cont_handle.get_features();
        comeBackLater = cont_handle.getComBackLater();
      }
      catch (Exception e)
      {
        System.out.println("Error parsing response from: " + url + "\n" + e);
        logger.log(Level.FINE, "error while parsing response from " + url);
        comeBackLater = -1;
        features = new ArrayList();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      comeBackLater = -1;
    }
  }

  /** open HttpURLConnection. Recommended way to open
   * HttpURLConnections, since this take care of setting timeouts
   * properly for java 1.4 and 1.5*/
  public static HttpURLConnection openHttpURLConnection(URL url)
      throws IOException, ConnectException
  {
    HttpURLConnection huc = null;
    huc = (HttpURLConnection) url.openConnection();

    String os_name = java.lang.System.getProperty("os.name");
    String os_version = java.lang.System.getProperty("os.version");
    String os_arch = java.lang.System.getProperty("os.arch");
    String VERSION = "1.0";

    String userAgent = "Jalview " + VERSION + "(" + os_name + "; " + os_arch +
        " ; " + os_version + ")";
    //e.g. "Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.7.2) Gecko/20040803"
    huc.addRequestProperty("User-Agent", userAgent);
    //logger.finest("opening "+url);


    int timeout = 10000;
    System.setProperty("sun.net.client.defaultConnectTimeout", timeout + "");
    System.setProperty("sun.net.client.defaultReadTimeout", timeout + "");

    // use reflection to determine if get and set timeout methods for urlconnection are available
    // seems java 1.5 does not watch the System properties any longer...
    // and java 1.4 did not provide these...
    // for 1.4 see setSystemProperties

    try
    {
      // try to use reflection to set timeout property
      Class urlconnectionClass = Class.forName("java.net.HttpURLConnection");

      Method setconnecttimeout = urlconnectionClass.getMethod(
          "setConnectTimeout", new Class[]
          {int.class}
          );
      setconnecttimeout.invoke(huc, new Object[]
                               {new Integer(timeout)});

      Method setreadtimeout = urlconnectionClass.getMethod(
          "setReadTimeout", new Class[]
          {int.class}
          );
      setreadtimeout.invoke(huc, new Object[]
                            {new Integer(timeout)});
      //System.out.println("successfully set java 1.5 timeout");
    }
    catch (Exception e)
    {
      //e.printStackTrace();
      // most likely it was a NoSuchMEthodException and we are running java 1.4.
    }
    return huc;
  }

  private InputStream open(URL url)
      throws java.io.IOException, java.net.ConnectException
  {
    InputStream inStream = null;

    HttpURLConnection huc = openHttpURLConnection(url);

    inStream = huc.getInputStream();

    return inStream;

  }

  /** returns a List of Features
   * @return a List of Maps containing the features*/
  public List get_features()
  {

    return features;
  }

  /** returns the comeBackLater value - if a server returned suchh -
   *
   * @return comeBackLater in seconds, or -1 if not provided by server
   */
  public int getComeBackLater()
  {

    return comeBackLater;

  }

}
