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

import java.applet.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import jalview.appletgui.*;
import jalview.datamodel.*;
import jalview.io.*;

/**
 * Jalview Applet. Runs in Java 1.18 runtime
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class JalviewLite
    extends Applet
{



  ///////////////////////////////////////////
  //The following public methods maybe called
  //externally, eg via javascript in HTML page

  public String getSelectedSequences()
  {
    StringBuffer result = new StringBuffer("");

    if (initialAlignFrame.viewport.getSelectionGroup() != null)
    {
      SequenceI[] seqs = initialAlignFrame.viewport.getSelectionGroup().
          getSequencesInOrder(
              initialAlignFrame.viewport.getAlignment());

      for (int i = 0; i < seqs.length; i++)
      {
        result.append(seqs[i].getName() + "¬");
      }
    }

    return result.toString();
  }

  public String getAlignment(String format)
  {
    return getAlignment(format, "true");
  }

  public String getAlignment(String format, String suffix)
  {
    try
    {
      boolean seqlimits = suffix.equalsIgnoreCase("true");

      String reply = new AppletFormatAdapter().formatSequences(format,
          currentAlignFrame.viewport.getAlignment(), seqlimits);
      return reply;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return "Error retrieving alignment in " + format + " format. ";
    }
  }

  public void loadAnnotation(String annotation)
  {
    if (new AnnotationFile().readAnnotationFile(
        currentAlignFrame.getAlignViewport().getAlignment(), annotation,
        AppletFormatAdapter.PASTE))
    {
      currentAlignFrame.alignPanel.fontChanged();
      currentAlignFrame.alignPanel.setScrollValues(0, 0);
    }
    else
    {
      currentAlignFrame.parseFeaturesFile(annotation, AppletFormatAdapter.PASTE);
    }
  }

  public String getFeatures(String format)
  {
    return currentAlignFrame.outputFeatures(false, format);
  }

  public String getAnnotation()
  {
    return currentAlignFrame.outputAnnotations(false);
  }

  public void loadAlignment(String text, String title)
  {
    Alignment al = null;
    String format = new IdentifyFile().Identify(text, AppletFormatAdapter.PASTE);
    try
    {
      al = new AppletFormatAdapter().readFile(text,
                                              AppletFormatAdapter.PASTE,
                                              format);
      if (al.getHeight() > 0)
      {
        new AlignFrame(al, this, title, false);
      }
    }
    catch (java.io.IOException ex)
    {
      ex.printStackTrace();
    }
  }

  ////////////////////////////////////////////////
  ////////////////////////////////////////////////



  static int lastFrameX = 200;
  static int lastFrameY = 200;
  boolean fileFound = true;
  String file = "No file";
  Button launcher = new Button("Start Jalview");

  //The currentAlignFrame is static, it will change
  //if and when the user selects a new window
  public static AlignFrame currentAlignFrame;

  //This is the first frame to be displayed, and does not change
  AlignFrame initialAlignFrame;

  boolean embedded = false;

  public boolean jmolAvailable = false;

  /**
   * init method for Jalview Applet
   */
  public void init()
  {
    int r = 255;
    int g = 255;
    int b = 255;
    String param = getParameter("RGB");

    if (param != null)
    {
      try
      {
        r = Integer.parseInt(param.substring(0, 2), 16);
        g = Integer.parseInt(param.substring(2, 4), 16);
        b = Integer.parseInt(param.substring(4, 6), 16);
      }
      catch (Exception ex)
      {
        r = 255;
        g = 255;
        b = 255;
      }
    }

    param = getParameter("label");
    if (param != null)
    {
      launcher.setLabel(param);
    }

    this.setBackground(new Color(r, g, b));

    file = getParameter("file");

    if (file == null)
    {
      //Maybe the sequences are added as parameters
      StringBuffer data = new StringBuffer("PASTE");
      int i = 1;
      while ( (file = getParameter("sequence" + i)) != null)
      {
        data.append(file.toString() + "\n");
        i++;
      }
      if (data.length() > 5)
      {
        file = data.toString();
      }
    }

    LoadJmolThread jmolAvailable = new LoadJmolThread();
    jmolAvailable.start();

    final JalviewLite applet = this;
    if (getParameter("embedded") != null
        && getParameter("embedded").equalsIgnoreCase("true"))
    {
      embedded = true;
      LoadingThread loader = new LoadingThread(file, applet);
      loader.start();
    }
    else if (file != null)
    {
      add(launcher);

      launcher.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          LoadingThread loader = new LoadingThread(file,
              applet);
          loader.start();
        }
      });
    }
    else
    {
      file = "NO FILE";
      fileFound = false;
    }
  }


  /**
   * Initialises and displays a new java.awt.Frame
   *
   * @param frame java.awt.Frame to be displayed
   * @param title title of new frame
   * @param width width if new frame
   * @param height height of new frame
   */
  public static void addFrame(final Frame frame, String title, int width,
                              int height)
  {
    frame.setLocation(lastFrameX, lastFrameY);
    lastFrameX += 40;
    lastFrameY += 40;
    frame.setSize(width, height);
    frame.setTitle(title);
    frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        if (frame instanceof AlignFrame)
        {
          ( (AlignFrame) frame).closeMenuItem_actionPerformed();
        }
        if (currentAlignFrame == frame)
        {
          currentAlignFrame = null;
        }
        lastFrameX -= 40;
        lastFrameY -= 40;
        frame.setMenuBar(null);
        frame.dispose();
      }

      public void windowActivated(WindowEvent e)
      {
        if (frame instanceof AlignFrame)
        {
          currentAlignFrame = (AlignFrame) frame;
        }
      }

    });
    frame.setVisible(true);
  }

  /**
   * This paints the background surrounding the "Launch Jalview button"
   * <br>
   * <br>If file given in parameter not found, displays error message
   *
   * @param g graphics context
   */
  public void paint(Graphics g)
  {
    if (!fileFound)
    {
      g.setColor(new Color(200, 200, 200));
      g.setColor(Color.cyan);
      g.fillRect(0, 0, getSize().width, getSize().height);
      g.setColor(Color.red);
      g.drawString("Jalview can't open file", 5, 15);
      g.drawString("\"" + file + "\"", 5, 30);
    }
    else if (embedded)
    {
      g.setColor(Color.black);
      g.setFont(new Font("Arial", Font.BOLD, 24));
      g.drawString("Jalview Applet", 50, this.getSize().height / 2 - 30);
      g.drawString("Loading Data...", 50, this.getSize().height / 2);
    }
  }


  class LoadJmolThread extends Thread
  {
    public void run()
    {
      try
      {
        if (!System.getProperty("java.version").startsWith("1.1"))
        {
          Class.forName("org.jmol.adapter.smarter.SmarterJmolAdapter");
          jmolAvailable = true;
        }
      }
      catch (java.lang.ClassNotFoundException ex)
      {
        System.out.println("Jmol not available - Using MCview for structures");
      }
    }
  }


  class LoadingThread
      extends Thread
  {
    String file;
    String protocol;
    String format;
    JalviewLite applet;

    public LoadingThread(String _file,
                         JalviewLite _applet)
    {
      file = _file;
      if (file.startsWith("PASTE"))
      {
        file = file.substring(5);
        protocol = AppletFormatAdapter.PASTE;
      }
      else if (inArchive(file))
      {
        protocol = AppletFormatAdapter.CLASSLOADER;
      }
      else
      {
        file = addProtocol(file);
        protocol = AppletFormatAdapter.URL;
      }
      format = new jalview.io.IdentifyFile().Identify(file, protocol);
      applet = _applet;
    }

    public void run()
    {
      startLoading();
    }

    private void startLoading()
    {
      Alignment al = null;
      try
      {
        al = new AppletFormatAdapter().readFile(file, protocol,
                                                format);
      }
      catch (java.io.IOException ex)
      {
        ex.printStackTrace();
      }
      if ( (al != null) && (al.getHeight() > 0))
      {
        currentAlignFrame = new AlignFrame(al,
                                           applet,
                                           file,
                                           embedded);

        if (protocol == jalview.io.AppletFormatAdapter.PASTE)
        {
          currentAlignFrame.setTitle("Sequences from " + getDocumentBase());
        }

        initialAlignFrame = currentAlignFrame;

        currentAlignFrame.statusBar.setText("Successfully loaded file " + file);

        String treeFile = applet.getParameter("tree");
        if (treeFile == null)
        {
          treeFile = applet.getParameter("treeFile");
        }

        if (treeFile != null)
        {
          try
          {
            if (inArchive(treeFile))
            {
              protocol = AppletFormatAdapter.CLASSLOADER;
            }
            else
            {
              protocol = AppletFormatAdapter.URL;
              treeFile = addProtocol(treeFile);
            }

            jalview.io.NewickFile fin = new jalview.io.NewickFile(treeFile,
                protocol);

            fin.parse();

            if (fin.getTree() != null)
            {
              currentAlignFrame.loadTree(fin, treeFile);
            }
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }

        String param = getParameter("features");
        if (param != null)
        {
          if (!inArchive(param))
          {
            param = addProtocol(param);
          }

          currentAlignFrame.parseFeaturesFile(param, protocol);
        }

        param = getParameter("showFeatureSettings");
        if (param != null && param.equalsIgnoreCase("true"))
        {
          currentAlignFrame.viewport.showSequenceFeatures(true);
          new FeatureSettings(currentAlignFrame.alignPanel);
        }

        param = getParameter("annotations");
        if (param != null)
        {
          if (!inArchive(param))
          {
            param = addProtocol(param);
          }

          new AnnotationFile().readAnnotationFile(
              currentAlignFrame.viewport.getAlignment(),
              param,
              protocol);

          currentAlignFrame.alignPanel.fontChanged();
          currentAlignFrame.alignPanel.setScrollValues(0, 0);

        }

        param = getParameter("jnetfile");
        if (param != null)
        {
          try
          {
            if (inArchive(param))
            {
              protocol = AppletFormatAdapter.CLASSLOADER;
            }
            else
            {
              protocol = AppletFormatAdapter.URL;
              param = addProtocol(param);
            }

            jalview.io.JPredFile predictions = new jalview.io.JPredFile(
                param, protocol);
            new JnetAnnotationMaker().add_annotation(predictions,
                currentAlignFrame.viewport.getAlignment(),
                0, false); // do not add sequence profile from concise output
            currentAlignFrame.alignPanel.fontChanged();
            currentAlignFrame.alignPanel.setScrollValues(0, 0);
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }

        /*
         <param name="PDBfile" value="1gaq.txt PDB|1GAQ|1GAQ|A PDB|1GAQ|1GAQ|B PDB|1GAQ|1GAQ|C">

         <param name="PDBfile2" value="1gaq.txt A=SEQA B=SEQB C=SEQB">

         <param name="PDBfile3" value="1q0o Q45135_9MICO">
        */


        int pdbFileCount = 0;
        do{
          if (pdbFileCount > 0)
            param = getParameter("PDBFILE" + pdbFileCount);
          else
            param = getParameter("PDBFILE");

          if (param != null)
          {
            PDBEntry pdb = new PDBEntry();

            String seqstring;
            SequenceI[] seqs = null;
            String [] chains = null;

            StringTokenizer st = new StringTokenizer(param, " ");

            if (st.countTokens() < 2)
            {
              String sequence = applet.getParameter("PDBSEQ");
              if (sequence != null)
                seqs = new SequenceI[]
                    {
                    (Sequence) currentAlignFrame.
                    getAlignViewport().getAlignment().
                    findName(sequence)};

            }
            else
            {
              param = st.nextToken();
              Vector tmp = new Vector();
              Vector tmp2 = new Vector();

              while (st.hasMoreTokens())
              {
                seqstring = st.nextToken();
                StringTokenizer st2 = new StringTokenizer(seqstring,"=");
                if(st2.countTokens()>1)
                {
                  //This is the chain
                  tmp2.addElement(st2.nextToken());
                  seqstring = st2.nextToken();
                }
                tmp.addElement( (Sequence) currentAlignFrame.
                                 getAlignViewport().getAlignment().
                                 findName(seqstring));
              }

              seqs = new SequenceI[tmp.size()];
              tmp.copyInto(seqs);
              if(tmp2.size()==tmp.size())
              {
                chains = new String[tmp2.size()];
                tmp2.copyInto(chains);
              }
            }

            if (inArchive(param) && !jmolAvailable)
            {
              protocol = AppletFormatAdapter.CLASSLOADER;
            }
            else
            {
              protocol = AppletFormatAdapter.URL;
              param = addProtocol(param);
            }

            pdb.setFile(param);

            if(seqs!=null)
            {
              for (int i = 0; i < seqs.length; i++)
              {
                ( (Sequence) seqs[i]).addPDBId(pdb);
              }

              if (jmolAvailable)
              {
                new jalview.appletgui.AppletJmol(pdb,
                                                 seqs,
                                                 chains,
                                                 currentAlignFrame.alignPanel,
                                                 protocol);
                lastFrameX += 40;
                lastFrameY+=40;
              }
              else
                    new MCview.AppletPDBViewer(pdb,
                                           seqs,
                                           chains,
                                           currentAlignFrame.alignPanel,
                                           protocol);
            }
          }

          pdbFileCount++;
        }
        while(pdbFileCount < 10);

      }
      else
      {
        fileFound = false;
        remove(launcher);
        repaint();
      }
    }

    /**
     * Discovers whether the given file is in the Applet Archive
     * @param file String
     * @return boolean
     */
    boolean inArchive(String file)
    {
      //This might throw a security exception in certain browsers
      //Netscape Communicator for instance.
      try
      {
        return (getClass().getResourceAsStream("/" + file) != null);
      }
      catch (Exception ex)
      {
        System.out.println("Exception checking resources: " + file + " " + ex);
        return false;
      }
    }

    String addProtocol(String file)
    {
      if (file.indexOf("://") == -1)
      {
        file = getCodeBase() + file;
      }

      return file;
    }
  }
}
