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

import java.util.*;

import javax.swing.*;

import jalview.gui.*;

/**
 * Main class for Jalview Application
 * <br>
 * <br>start with java -Djava.ext.dirs=$PATH_TO_LIB$ jalview.bin.Jalview
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class Jalview
{

  /**
   * main class for Jalview application
   *
   * @param args open <em>filename</em>
   */
  public static void main(String[] args)
  {
    System.out.println("Java version: " + System.getProperty("java.version"));
    System.out.println(System.getProperty("os.arch") + " "
                       + System.getProperty("os.name") + " "
                       + System.getProperty("os.version"));


    ArgsParser aparser = new ArgsParser(args);
    boolean headless = false;

    if (aparser.contains("help") || aparser.contains("h"))
    {
      System.out.println(
          "Usage: jalview -open [FILE] [OUTPUT_FORMAT] [OUTPUT_FILE]\n\n"
          + "-nodisplay\tRun Jalview without User Interface.\n"
          +
          "-props FILE\tUse the given Jalview properties file instead of users default.\n"
          +
          "-annotations FILE\tAdd precalculated annotations to the alignment.\n"
          +
          "-features FILE\tUse the given file to mark features on the alignment.\n"
          + "-fasta FILE\tCreate alignment file FILE in Fasta format.\n"
          + "-clustal FILE\tCreate alignment file FILE in Clustal format.\n"
          + "-pfam FILE\tCreate alignment file FILE in PFAM format.\n"
          + "-msf FILE\tCreate alignment file FILE in MSF format.\n"
          + "-pileup FILE\tCreate alignment file FILE in Pileup format\n"
          + "-pir FILE\tCreate alignment file FILE in PIR format.\n"
          + "-blc FILE\tCreate alignment file FILE in BLC format.\n"
          + "-jalview FILE\tCreate alignment file FILE in Jalview format.\n"
          + "-png FILE\tCreate PNG image FILE from alignment.\n"
          +
          "-imgMap FILE\tCreate HTML file FILE with image map of PNG image.\n"
          + "-eps FILE\tCreate EPS file FILE from alignment."
          + "-questionnaire URL\tQueries the given URL for information about any Jalview user questionnaires."
          + "\n\n~Read documentation in Application or visit http://www.jalview.org for description of Features and Annotations file~\n\n");
      System.exit(0);
    }

    Cache.loadProperties(aparser.getValue("props")); // must do this before anything else!

    if (aparser.contains("nodisplay"))
    {
      System.setProperty("java.awt.headless", "true");
    }
    if (System.getProperty("java.awt.headless") != null
        && System.getProperty("java.awt.headless").equals("true"))
    {
      headless = true;
    }

    try
    {
      Cache.initLogger();
    }
    catch (java.lang.NoClassDefFoundError error)
    {
      error.printStackTrace();
      System.out.println(
          "\nEssential logging libraries not found."
          + "\nUse: java -Djava.ext.dirs=$PATH_TO_LIB$ jalview.bin.Jalview");
      System.exit(0);
    }

    Desktop desktop = null;

    try
    {
      UIManager.setLookAndFeel(
          UIManager.getSystemLookAndFeelClassName()
          //        UIManager.getCrossPlatformLookAndFeelClassName()
          //"com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
          //"javax.swing.plaf.metal.MetalLookAndFeel"
          //"com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
          //"com.sun.java.swing.plaf.motif.MotifLookAndFeel"

          );
    }
    catch (Exception ex)
    {}

    if (!headless)
    {
      desktop = new Desktop();
      desktop.setVisible(true);
      desktop.discoverer.start();
    /*
      String url = aparser.getValue("questionnaire");
      if (url != null)
      {
        // Start the desktop questionnaire prompter with the specified questionnaire
        Cache.log.debug("Starting questionnaire url at " + url);
        desktop.checkForQuestionnaire(url);
      }
      else
      {
        if (Cache.getProperty("NOQUESTIONNAIRES") == null)
        {
          // Start the desktop questionnaire prompter with the specified questionnaire
          // String defurl = "http://anaplog.compbio.dundee.ac.uk/cgi-bin/questionnaire.pl"; //
          String defurl = "http://www.jalview.org/cgi-bin/questionnaire.pl";
          Cache.log.debug("Starting questionnaire with default url: " + defurl);
          desktop.checkForQuestionnaire(defurl);

        }
      }
    */
    }

    String file = null, protocol = null, format = null, data = null;
    jalview.io.FileLoader fileLoader = new jalview.io.FileLoader();

    file = aparser.getValue("open");

    if (file == null && desktop == null)
    {
      System.out.println("No files to open!");
      System.exit(1);
    }

    if (file != null)
    {
      System.out.println("Opening file: " + file);

      if (!file.startsWith("http://"))
      {
        if (! (new java.io.File(file)).exists())
        {
          System.out.println("Can't find " + file);
          if (headless)
          {
            System.exit(1);
          }
        }
      }

      protocol = "File";

      if (file.indexOf("http:") > -1 || file.indexOf("file:") > -1)
      {
        protocol = "URL";
      }

      if (file.endsWith(".jar"))
      {
        format = "Jalview";
      }
      else
      {
        format = new jalview.io.IdentifyFile().Identify(file, protocol);
      }
      AlignFrame af = fileLoader.LoadFileWaitTillLoaded(file, protocol, format);

      if (af == null)
      {
        System.out.println("error");
        return;
      }

      data = aparser.getValue("colour");
      if (data != null)
      {
        data.replaceAll("%20", " ");

        jalview.schemes.ColourSchemeI cs =
            jalview.schemes.ColourSchemeProperty.getColour(af.getViewport().
            getAlignment(), data);

        if (cs == null)
        {
          jalview.schemes.UserColourScheme ucs
              = new jalview.schemes.UserColourScheme("white");
          ucs.parseAppletParameter(data);
          cs = ucs;
        }

        af.changeColour(cs);
      }

      // Must maintain ability to use the groups flag
      data = aparser.getValue("groups");
      if (data != null)
      {
        af.parseFeaturesFile(data, protocol);
        System.out.println("Added " + data);
      }
      data = aparser.getValue("features");
      if (data != null)
      {
        af.parseFeaturesFile(data, protocol);
        System.out.println("Added " + data);
      }

      data = aparser.getValue("annotations");
      if (data != null)
      {
        af.loadJalviewDataFile(data);
        System.out.println("Added " + data);
      }

      String imageName = "unnamed.png";
      while (aparser.getSize() > 1)
      {
        format = aparser.nextValue();
        file = aparser.nextValue();

        if (format.equalsIgnoreCase("png"))
        {
          af.createPNG(new java.io.File(file));
          imageName = (new java.io.File(file)).getName();
          System.out.println("Creating PNG image: " + file);
          continue;
        }
        else if (format.equalsIgnoreCase("imgMap"))
        {
          af.createImageMap(new java.io.File(file), imageName);
          System.out.println("Creating image map: " + file);
          continue;
        }
        else if (format.equalsIgnoreCase("eps"))
        {
          System.out.println("Creating EPS file: " + file);
          af.createEPS(new java.io.File(file));
          continue;
        }

        if (af.saveAlignment(file, format))
        {
          System.out.println("Written alignment in " + format +
                             " format to " + file);
        }
        else
        {
          System.out.println("Error writing file " + file + " in " + format +
                             " format!!");
        }

      }

      while (aparser.getSize() > 0)
      {
        System.out.println("Unknown arg: " + aparser.nextValue());
      }
    }

    // We'll only open the default file if the desktop is visible.
    // And the user
    //////////////////////
    if (
        !headless
        && file == null
        && jalview.bin.Cache.getDefault("SHOW_STARTUP_FILE", true)
        )
    {

      file = jalview.bin.Cache.getDefault("STARTUP_FILE",
                                          "http://www.jalview.org/examples/exampleFile_2_3.jar");

      protocol = "File";

      if (file.indexOf("http:") > -1)
      {
        protocol = "URL";
      }

      if (file.endsWith(".jar"))
      {
        format = "Jalview";
      }
      else
      {
        format = new jalview.io.IdentifyFile().Identify(file, protocol);
      }

      fileLoader.LoadFile(file, protocol, format);

    }
  }
}

class ArgsParser
{
  Vector vargs = null;
  public ArgsParser(String[] args)
  {
    vargs = new Vector();
    for (int i = 0; i < args.length; i++)
    {
      String arg = args[i].trim();
      if (arg.charAt(0) == '-')
      {
        arg = arg.substring(1);
      }
      vargs.addElement(arg);
    }
  }

  public String getValue(String arg)
  {
    int index = vargs.indexOf(arg);
    String ret = null;
    if (index != -1)
    {
      ret = vargs.elementAt(index + 1).toString();
      vargs.removeElementAt(index);
      vargs.removeElementAt(index);
    }
    return ret;
  }

  public boolean contains(String arg)
  {
    if (vargs.contains(arg))
    {
      vargs.removeElement(arg);
      return true;
    }
    else
    {
      return false;
    }
  }

  public String nextValue()
  {
    return vargs.remove(0).toString();
  }

  public int getSize()
  {
    return vargs.size();
  }

}
