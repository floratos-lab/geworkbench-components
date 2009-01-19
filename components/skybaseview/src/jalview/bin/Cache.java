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
package jalview.bin;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;

/**
 * Stores and retrieves Jalview Application Properties
 * <br><br>Current properties include:
 * <br>logs.Axis.Level - one of the stringified Levels for log4j controlling the logging level for axis (used for web services)
 * <br>logs.Castor.Level - one of the stringified Levels for log4j controlling the logging level for castor (used for serialization)
 * <br>logs.Jalview.Level - Cache.log stringified level.
 * <br>DISCOVERY_START - Boolean - controls if discovery services are queried on startup
 * <br>DISCOVERY_URLS - comma separated list of Discovery Service endpoints.
 * <br>SCREEN_WIDTH,SCREEN_HEIGHT,SCREEN_Y=285,SCREEN_X=371,SHOW_FULLSCREEN
 * FONT_NAME,FONT_SIZE,FONT_STYLE,GAP_SYMBOL,LAST_DIRECTORY,USER_DEFINED_COLOUR
 * SHOW_FULL_ID,SHOW_IDENTITY,SHOW_QUALITY,SHOW_ANNOTATIONS,SHOW_CONSERVATION,
 * DEFAULT_COLOUR,DEFAULT_FILE_FORMAT,STARTUP_FILE,SHOW_STARTUP_FILE

 * @author $author$
 * @version $Revision: 1.1 $
 */
public class Cache
{
  /**
   * Initialises the Apache Axis logger
   */
  public static Logger log;

  /** Jalview Properties */
  public static Properties applicationProperties = new Properties();

  /** Default file is  ~/.jalview_properties */
  static String propertiesFile;

  public static void initLogger()
  {
    try
    {
      Logger laxis = Logger.getLogger("org.apache.axis");
      Logger lcastor = Logger.getLogger("org.exolab.castor");
      jalview.bin.Cache.log = Logger.getLogger("jalview.bin.Jalview");

      laxis.setLevel(Level.toLevel(Cache.getDefault("logs.Axis.Level",
          Level.INFO.toString())));
      lcastor.setLevel(Level.toLevel(Cache.getDefault("logs.Castor.Level",
          Level.INFO.toString())));
      jalview.bin.Cache.log.setLevel(Level.toLevel(Cache.getDefault(
          "logs.Jalview.level",
          Level.INFO.toString())));
      ConsoleAppender ap = new ConsoleAppender(new SimpleLayout(),
                                               "System.err");
      ap.setName("JalviewLogger");

      laxis.addAppender(ap);
      lcastor.addAppender(ap);
      jalview.bin.Cache.log.addAppender(ap);
      // Tell the user that debug is enabled
      jalview.bin.Cache.log.debug("Jalview Debugging Output Follows.");
    }
    catch (Exception ex)
    {
      System.err.println("Problems initializing the log4j system\n");
    }
  }

  /** Called when Jalview is started */
  public static void loadProperties(String propsFile)
  {
    propertiesFile = propsFile;
    if (propsFile == null)
    {
      propertiesFile = System.getProperty("user.home") + File.separatorChar +
          ".jalview_properties";
    }

    try
    {
      FileInputStream fis = new FileInputStream(propertiesFile);
      applicationProperties.load(fis);
      applicationProperties.remove("LATEST_VERSION");
      applicationProperties.remove("VERSION");
      fis.close();
    }
    catch (Exception ex)
    {
      System.out.println("Error reading properties file: " + ex);
    }

    if (getDefault("USE_PROXY", false))
    {
      System.out.println("Using proxyServer: " + getDefault("PROXY_SERVER", null) +
                         " proxyPort: " + getDefault("PROXY_PORT", null));
      System.setProperty("http.proxyHost", getDefault("PROXY_SERVER", null));
      System.setProperty("http.proxyPort", getDefault("PROXY_PORT", null));
    }

    // FIND THE VERSION NUMBER AND BUILD DATE FROM jalview.jar
    // MUST FOLLOW READING OF LOCAL PROPERTIES FILE AS THE
    // VERSION MAY HAVE CHANGED SINCE LAST USING JALVIEW
    try
    {
      String buildDetails = "jar:"
          .concat(
              Cache.class.getProtectionDomain().getCodeSource().getLocation().
              toString()
              .concat("!/.build_properties")
          );

      java.net.URL localJarFileURL = new java.net.URL(buildDetails);

      InputStream in = localJarFileURL.openStream();
      applicationProperties.load(in);
      in.close();
    }
    catch (Exception ex)
    {
      System.out.println("Error reading build details: " + ex);
      applicationProperties.remove("VERSION");
    }

    String jnlpVersion = System.getProperty("jalview.version");
    String codeVersion = getProperty("VERSION");

    if (codeVersion == null)
    {
      // THIS SHOULD ONLY BE THE CASE WHEN TESTING!!
      codeVersion = "Test";
      jnlpVersion = "Test";
    }

    System.out.println("Jalview Version: " + codeVersion);

    // jnlpVersion will be null if we're using InstallAnywhere
    // Dont do this check if running in headless mode
    if (jnlpVersion == null && (
        System.getProperty("java.awt.headless") == null
        || System.getProperty("java.awt.headless").equals("false")))
    {

      class VersionChecker
          extends Thread
      {
        public void run()
        {
          String jnlpVersion = null;
          try
          {
            java.net.URL url = new java.net.URL(
                "http://www.jalview.org/webstart/jalview.jnlp");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.
                openStream()));
            String line = null;
            while ( (line = in.readLine()) != null)
            {
              if (line.indexOf("jalview.version") == -1)
              {
                continue;
              }

              line = line.substring(line.indexOf("value=") + 7);
              line = line.substring(0, line.lastIndexOf("\""));
              jnlpVersion = line;
              break;
            }
          }
          catch (Exception ex)
          {
            System.out.println(ex);
            jnlpVersion = getProperty("VERSION");
          }

          setProperty("LATEST_VERSION", jnlpVersion);
        }
      }

      VersionChecker vc = new VersionChecker();
      vc.start();
    }
    else
    {
      if (jnlpVersion != null)
      {
        setProperty("LATEST_VERSION", jnlpVersion);
      }
      else
      {
        applicationProperties.remove("LATEST_VERSION");
      }
    }

    setProperty("VERSION", codeVersion);

    //LOAD USERDEFINED COLOURS
    jalview.gui.UserDefinedColours.initUserColourSchemes(getProperty(
        "USER_DEFINED_COLOURS"));
    jalview.io.PIRFile.useModellerOutput = Cache.getDefault("PIR_MODELLER", false);
  }

  /**
   * Gets Jalview application property of given key. Returns null
   * if key not found
   *
   * @param key Name of property
   *
   * @return Property value
   */
  public static String getProperty(String key)
  {
    return applicationProperties.getProperty(key);
  }

  /** These methods are used when checking if the saved preference
   * is different to the default setting
   */

  public static boolean getDefault(String property, boolean def)
  {
    String string = getProperty(property);
    if (string != null)
    {
      def = Boolean.valueOf(string).booleanValue();
    }

    return def;
  }

  /** These methods are used when checking if the saved preference
   * is different to the default setting
   */
  public static String getDefault(String property, String def)
  {
    String string = getProperty(property);
    if (string != null)
    {
      return string;
    }

    return def;
  }

  /**
   * Stores property in the file "HOME_DIR/.jalview_properties"
   *
   * @param key Name of object
   * @param obj String value of property
   *
   * @return String value of property
   */
  public static String setProperty(String key, String obj)
  {
    try
    {
      FileOutputStream out = new FileOutputStream(propertiesFile);
      applicationProperties.setProperty(key, obj);
      applicationProperties.store(out, "---JalviewX Properties File---");
      out.close();
    }
    catch (Exception ex)
    {
      System.out.println("Error setting property: " + key + " " + obj + "\n" +
                         ex);
    }
    return obj;
  }

  public static void saveProperties()
  {
    try
    {
      FileOutputStream out = new FileOutputStream(propertiesFile);
      applicationProperties.store(out, "---JalviewX Properties File---");
      out.close();
    }
    catch (Exception ex)
    {
      System.out.println("Error saving properties: " + ex);
    }
  }

  /**
   * internal vamsas class discovery state
   */
  private static int vamsasJarsArePresent = -1;
  /**
   * Searches for vamsas client classes on class path.
   * @return true if vamsas client is present on classpath
   */
  public static boolean vamsasJarsPresent()
  {
    if (vamsasJarsArePresent == -1)
    {
      try
      {
        if (jalview.jbgui.GDesktop.class.getClassLoader().loadClass(
            "uk.ac.vamsas.client.VorbaId") != null)
        {
          jalview.bin.Cache.log.debug(
              "Found Vamsas Classes (uk.ac..vamsas.client.VorbaId can be loaded)");
          vamsasJarsArePresent = 1;
          Logger lvclient = Logger.getLogger("uk.ac.vamsas");
          lvclient.setLevel(Level.toLevel(Cache.getDefault("logs.Vamsas.Level",
              Level.INFO.toString())));

          lvclient.addAppender(log.getAppender("JalviewLogger"));
          // Tell the user that debug is enabled
          lvclient.debug("Jalview Vamsas Client Debugging Output Follows.");
        }
      }
      catch (Exception e)
      {
        vamsasJarsArePresent = 0;
        jalview.bin.Cache.log.debug("Vamsas Classes are not present");
      }
    }
    return (vamsasJarsArePresent > 0);
  }
  /**
   * internal vamsas class discovery state
   */
  private static int groovyJarsArePresent = -1;
  /**
   * Searches for vamsas client classes on class path.
   * @return true if vamsas client is present on classpath
   */
  public static boolean groovyJarsPresent()
  {
    if (groovyJarsArePresent == -1)
    {
      try
      {
        if (Cache.class.getClassLoader().loadClass(
            "groovy.lang.GroovyObject") != null)
        {
          jalview.bin.Cache.log.debug(
              "Found Groovy (groovy.lang.GroovyObject can be loaded)");
          groovyJarsArePresent = 1;
          Logger lgclient = Logger.getLogger("groovy");
          lgclient.setLevel(Level.toLevel(Cache.getDefault("logs.Groovy.Level",
              Level.INFO.toString())));

          lgclient.addAppender(log.getAppender("JalviewLogger"));
          // Tell the user that debug is enabled
          lgclient.debug("Jalview Groovy Client Debugging Output Follows.");
        }
      }
      catch (Exception e)
      {
        groovyJarsArePresent = 0;
        jalview.bin.Cache.log.debug("Groovy Classes are not present");
      }
    }
    return (groovyJarsArePresent > 0);
  }

}
