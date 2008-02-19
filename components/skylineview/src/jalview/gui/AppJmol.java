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
package jalview.gui;

import java.util.regex.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;

import jalview.jbgui.GStructureViewer;
import jalview.datamodel.*;
import jalview.gui.*;
import jalview.structure.*;
import jalview.datamodel.PDBEntry;
import jalview.io.*;
import jalview.schemes.*;

import org.jmol.api.*;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.popup.*;


public class AppJmol
    extends GStructureViewer
    implements StructureListener, JmolStatusListener, Runnable

{
  JmolViewer viewer;
  JmolPopup jmolpopup;
  ScriptWindow scriptWindow;
  PDBEntry pdbentry;
  SequenceI[] sequence;
  String [] chains;
  StructureSelectionManager ssm;
  JSplitPane splitPane;
  RenderPanel renderPanel;
  AlignmentPanel ap;
  String fileLoadingError;
  boolean colourBySequence = true;
  boolean loadingFromArchive = false;
  Vector atomsPicked = new Vector();

  public AppJmol(String file, String id,
                 SequenceI[] seq,
                 AlignmentPanel ap,
                 String loadStatus,
                 Rectangle bounds)
  {
    loadingFromArchive = true;
    pdbentry = new PDBEntry();
    pdbentry.setFile(file);
    pdbentry.setId(id);
    this.chains = chains;
    this.sequence = seq;
    this.ap = ap;
    this.setBounds(bounds);

    colourBySequence = false;
    seqColour.setSelected(false);

    jalview.gui.Desktop.addInternalFrame(this, "Loading File",
                                         bounds.width,bounds.height);

    initJmol(loadStatus);

    this.addInternalFrameListener(new InternalFrameAdapter()
    {
      public void internalFrameClosing(InternalFrameEvent internalFrameEvent)
      {
        closeViewer();
      }
    });
  }

public synchronized void addSequence(SequenceI [] seq)
   {
    Vector v = new Vector();
     for(int i=0; i<sequence.length; i++)
       v.addElement(sequence[i]);

     for(int i=0; i<seq.length; i++)
      if(!v.contains(seq[i]))
          v.addElement(seq[i]);

     SequenceI [] tmp = new SequenceI[v.size()];
     v.copyInto(tmp);
     sequence = tmp;
   }

  public AppJmol(PDBEntry pdbentry, SequenceI[] seq, String [] chains, AlignmentPanel ap)
  {
    //////////////////////////////////
    //Is the pdb file already loaded?
    String alreadyMapped = StructureSelectionManager
        .getStructureSelectionManager()
        .alreadyMappedToFile(pdbentry.getId());

    if (alreadyMapped != null)
    {
      int option = JOptionPane.showInternalConfirmDialog(Desktop.desktop,
          pdbentry.getId() + " is already displayed."
          + "\nDo you want to map sequences to the visible structure?",
          "Map Sequences to Visible Window: " + pdbentry.getId(),
          JOptionPane.YES_NO_OPTION);

      if (option == JOptionPane.YES_OPTION)
      {
        StructureSelectionManager.getStructureSelectionManager()
            .setMapping(seq, chains, alreadyMapped, AppletFormatAdapter.FILE);
        if (ap.seqPanel.seqCanvas.fr!=null) {
          ap.seqPanel.seqCanvas.fr.featuresAdded();
          ap.paintAlignment(true);
        }

        //Now this AppJmol is mapped to new sequences. We must add them to
        // the exisiting array
        JInternalFrame [] frames = Desktop.instance.getAllFrames();

        for(int i=0; i<frames.length; i++)
        {
          if(frames[i] instanceof AppJmol)
          {
           AppJmol topJmol = ((AppJmol)frames[i]);
           if(topJmol.pdbentry.getFile().equals(alreadyMapped))
           {
             topJmol.addSequence(seq);
             break;
           }
          }
        }

        return;
      }
    }
    ///////////////////////////////////

    this.ap = ap;
    this.pdbentry = pdbentry;
    this.sequence = seq;

    jalview.gui.Desktop.addInternalFrame(this, "Loading File", 400, 400);

    if (pdbentry.getFile() != null)
    {
      initJmol("load \""+pdbentry.getFile()+"\"");
    }
    else
    {
      Thread worker = new Thread(this);
      worker.start();
    }

    this.addInternalFrameListener(new InternalFrameAdapter()
    {
      public void internalFrameClosing(InternalFrameEvent internalFrameEvent)
      {
        closeViewer();
      }
    });
  }


  void initJmol(String command)
  {
    renderPanel = new RenderPanel();

    this.getContentPane().add(renderPanel, java.awt.BorderLayout.CENTER);

    StringBuffer title = new StringBuffer(sequence[0].getName() + ":" +
                                          pdbentry.getId());

    if (pdbentry.getProperty() != null)
    {
      if (pdbentry.getProperty().get("method") != null)
      {
        title.append(" Method: ");
        title.append(pdbentry.getProperty().get("method"));
      }
      if (pdbentry.getProperty().get("chains") != null)
      {
        title.append(" Chain:");
        title.append(pdbentry.getProperty().get("chains"));
      }
    }

    this.setTitle(title.toString());

    viewer = org.jmol.api.JmolViewer.allocateViewer(renderPanel,
						    null);
						    //        new SmarterJmolAdapter());


    //    viewer.setAppletContext("", null, null, "");

    viewer.setJmolStatusListener(this);

    jmolpopup = JmolPopup.newJmolPopup(viewer);

    viewer.evalStringQuiet(command);
  }


  void setChainMenuItems(Vector chains)
  {
    chainMenu.removeAll();

    JMenuItem menuItem = new JMenuItem("All");
    menuItem.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent evt)
          {
            allChainsSelected = true;
            for(int i=0; i<chainMenu.getItemCount(); i++)
            {
              if (chainMenu.getItem(i) instanceof JCheckBoxMenuItem)
                ( (JCheckBoxMenuItem) chainMenu.getItem(i)).setSelected(true);
            }
            centerViewer();
            allChainsSelected = false;
          }
        });

    chainMenu.add(menuItem);

    for (int c = 0; c < chains.size(); c++)
    {
      menuItem = new JCheckBoxMenuItem(chains.elementAt(c).toString(), true);
      menuItem.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent evt)
        {
          if (!allChainsSelected)
            centerViewer();
        }
      });

      chainMenu.add(menuItem);
    }
  }

  boolean allChainsSelected = false;
  void centerViewer()
  {
    StringBuffer cmd = new StringBuffer();
    for(int i=0; i<chainMenu.getItemCount(); i++)
    {
      if (chainMenu.getItem(i) instanceof JCheckBoxMenuItem)
      {
       JCheckBoxMenuItem item = (JCheckBoxMenuItem) chainMenu.getItem(i);
       if(item.isSelected())
         cmd.append(":"+item.getText()+" or ");
      }
    }

    if (cmd.length() > 0)
      cmd.setLength(cmd.length() - 4);

    viewer.evalStringQuiet("select *;restrict "
                      +cmd+";cartoon;center "+cmd);
  }

  void closeViewer()
  {
      //    viewer.setModeMouse(org.jmol.viewer.JmolConstants.MOUSE_NONE);
    viewer.evalStringQuiet("zap");
    viewer.setJmolStatusListener(null);
    viewer = null;

    //We'll need to find out what other
    // listeners need to be shut down in Jmol
    StructureSelectionManager
        .getStructureSelectionManager()
        .removeStructureViewerListener(this, pdbentry.getFile());
  }

  public void run()
  {
    try
    {
      EBIFetchClient ebi = new EBIFetchClient();
      String query = "pdb:" + pdbentry.getId();
      pdbentry.setFile(ebi.fetchDataAsFile(query, "default", "raw")
                       .getAbsolutePath());
      initJmol("load "+pdbentry.getFile());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void pdbFile_actionPerformed(ActionEvent actionEvent)
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
        jalview.bin.Cache.getProperty(
            "LAST_DIRECTORY"));

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle("Save PDB File");
    chooser.setToolTipText("Save");

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      try
      {
        BufferedReader in = new BufferedReader(new FileReader(pdbentry.getFile()));
        File outFile = chooser.getSelectedFile();

        PrintWriter out = new PrintWriter(new FileOutputStream(outFile));
        String data;
        while ( (data = in.readLine()) != null)
        {
          if (
              ! (data.indexOf("<PRE>") > -1 || data.indexOf("</PRE>") > -1)
              )
          {
            out.println(data);
          }
        }
        out.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  public void viewMapping_actionPerformed(ActionEvent actionEvent)
  {
    jalview.gui.CutAndPasteTransfer cap = new jalview.gui.CutAndPasteTransfer();
    jalview.gui.Desktop.addInternalFrame(cap, "PDB - Sequence Mapping", 550,
                                         600);
    cap.setText(
        StructureSelectionManager.getStructureSelectionManager().printMapping(
            pdbentry.getFile())
        );
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void eps_actionPerformed(ActionEvent e)
  {
    makePDBImage(jalview.util.ImageMaker.EPS);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void png_actionPerformed(ActionEvent e)
  {
    makePDBImage(jalview.util.ImageMaker.PNG);
  }

  void makePDBImage(int type)
  {
    int width = getWidth();
    int height = getHeight();

    jalview.util.ImageMaker im;

    if (type == jalview.util.ImageMaker.PNG)
    {
      im = new jalview.util.ImageMaker(this,
                                       jalview.util.ImageMaker.PNG,
                                       "Make PNG image from view",
                                       width, height,
                                       null, null);
    }
    else
    {
      im = new jalview.util.ImageMaker(this,
                                       jalview.util.ImageMaker.EPS,
                                       "Make EPS file from view",
                                       width, height,
                                       null, this.getTitle());
    }

    if (im.getGraphics() != null)
    {
      Rectangle rect = new Rectangle(width, height);
      viewer.renderScreenImage(im.getGraphics(),
                               rect.getSize(), rect);
      im.writeImage();
    }
  }


  public void seqColour_actionPerformed(ActionEvent actionEvent)
  {
    lastCommand = null;
    colourBySequence = seqColour.isSelected();
    colourBySequence(ap.alignFrame.alignPanel);
  }

  public void chainColour_actionPerformed(ActionEvent actionEvent)
  {
    colourBySequence = false;
    seqColour.setSelected(false);
    viewer.evalStringQuiet("select *;color chain");
  }

  public void chargeColour_actionPerformed(ActionEvent actionEvent)
  {
    colourBySequence = false;
    seqColour.setSelected(false);
    viewer.evalStringQuiet("select *;color white;select ASP,GLU;color red;"
                      +"select LYS,ARG;color blue;select CYS;color yellow");
  }

  public void zappoColour_actionPerformed(ActionEvent actionEvent)
  {
    setJalviewColourScheme(new ZappoColourScheme());
  }

  public void taylorColour_actionPerformed(ActionEvent actionEvent)
  {
    setJalviewColourScheme(new TaylorColourScheme());
  }

  public void hydroColour_actionPerformed(ActionEvent actionEvent)
  {
    setJalviewColourScheme(new HydrophobicColourScheme());
  }

  public void helixColour_actionPerformed(ActionEvent actionEvent)
  {
    setJalviewColourScheme(new HelixColourScheme());
  }

  public void strandColour_actionPerformed(ActionEvent actionEvent)
  {
    setJalviewColourScheme(new StrandColourScheme());
  }

  public void turnColour_actionPerformed(ActionEvent actionEvent)
  {
    setJalviewColourScheme(new TurnColourScheme());
  }

  public void buriedColour_actionPerformed(ActionEvent actionEvent)
  {
    setJalviewColourScheme(new BuriedColourScheme());
  }

  public void setJalviewColourScheme(ColourSchemeI cs)
  {
    colourBySequence = false;
    seqColour.setSelected(false);

    if(cs==null)
      return;

    String res;
    int index;
    Color col;

    Enumeration en = ResidueProperties.aa3Hash.keys();
    StringBuffer command = new StringBuffer("select *;color white;");
    while(en.hasMoreElements())
    {
      res = en.nextElement().toString();
      index = ((Integer) ResidueProperties.aa3Hash.get(res)).intValue();
      if(index>20)
        continue;

      col = cs.findColour(ResidueProperties.aa[index].charAt(0));

      command.append("select "+res+";color["
                        + col.getRed() + ","
                        + col.getGreen() + ","
                        + col.getBlue() + "];");
    }

    viewer.evalStringQuiet(command.toString());
  }

  public void userColour_actionPerformed(ActionEvent actionEvent)
  {
    new UserDefinedColours(this, null);
  }

  public void backGround_actionPerformed(ActionEvent actionEvent)
  {
    java.awt.Color col = JColorChooser.showDialog(this,
                                                  "Select Background Colour",
                                                  null);

    if (col != null)
    {
      viewer.evalStringQuiet("background ["
                        + col.getRed() + ","
                        + col.getGreen() + ","
                        + col.getBlue() + "];");
    }
  }


  public void jmolHelp_actionPerformed(ActionEvent actionEvent)
  {
       try{
         jalview.util.BrowserLauncher.openURL(
             "http://jmol.sourceforge.net/docs/JmolUserGuide/");
       }catch(Exception ex){}
   }


  //////////////////////////////////
  ///StructureListener
  public String getPdbFile()
  {
    return pdbentry.getFile();
  }

  Pattern pattern = Pattern.compile(
      "\\[(.*)\\]([0-9]+)(:[a-zA-Z]*)?\\.([a-zA-Z]+)(/[0-9]*)?"
      );

  String lastMessage;
  public void mouseOverStructure(int atomIndex, String strInfo)
  {
    Matcher matcher = pattern.matcher(strInfo);
    matcher.find();
    matcher.group(1);
    int pdbResNum = Integer.parseInt(matcher.group(2));
    String chainId = matcher.group(3);

    if (chainId != null)
      chainId = chainId.substring(1, chainId.length());
    else
    {
      chainId = " ";
    }

    if (lastMessage == null || !lastMessage.equals(strInfo))
    {
      ssm.mouseOverStructure(pdbResNum, chainId, pdbentry.getFile());
    }
    lastMessage = strInfo;
  }

  StringBuffer resetLastRes = new StringBuffer();
  StringBuffer eval = new StringBuffer();

  public void highlightAtom(int atomIndex, int pdbResNum, String chain, String pdbfile)
  {
    if (!pdbfile.equals(pdbentry.getFile()))
      return;

    if (resetLastRes.length() > 0)
    {
      viewer.evalStringQuiet(resetLastRes.toString());
    }

    eval.setLength(0);
    eval.append("select " + pdbResNum);

    resetLastRes.setLength(0);
    resetLastRes.append("select " + pdbResNum);

    if (!chain.equals(" "))
    {
      eval.append(":" + chain);
      resetLastRes.append(":" + chain);
    }

    eval.append(";wireframe 100;"+eval.toString()+".CA;");

    resetLastRes.append(";wireframe 0;"+resetLastRes.toString()+".CA;spacefill 0;");

    eval.append("spacefill 200;select none");

    viewer.evalStringQuiet(eval.toString());
  }

  public Color getColour(int atomIndex, int pdbResNum, String chain, String pdbfile)
  {
    if (!pdbfile.equals(pdbentry.getFile()))
      return null;

    return new Color(0,0,0);//viewer.getAtomArgb(atomIndex));
  }

    public void notifyMeasurementsChanged(){}

    public void scriptStatus(String s){}

    public void scriptEcho(String s){}

    public void setStatusMessage(String s){}

  public void updateColours(Object source)
  {
    colourBySequence( (AlignmentPanel) source);
  }


//End StructureListener
////////////////////////////

  String lastCommand;
  FeatureRenderer fr=null;
  public void colourBySequence(AlignmentPanel sourceap)
  {
    this.ap = sourceap;

    if(!colourBySequence || ap.alignFrame.getCurrentView()!=ap.av)
      return;

    StructureMapping[] mapping = ssm.getMapping(pdbentry.getFile());

    if (mapping.length < 1)
     return;


    SequenceRenderer sr = new SequenceRenderer(ap.av);

    boolean showFeatures = false;

    if (ap.av.showSequenceFeatures)
    {
      showFeatures = true;
      if (fr == null)
      {
        fr = new jalview.gui.FeatureRenderer(ap);
      }

      fr.transferSettings(ap.seqPanel.seqCanvas.getFeatureRenderer());
    }

    StringBuffer command = new StringBuffer();

    int lastPos = -1;
    for (int s = 0; s < sequence.length; s++)
    {
      for (int m = 0; m < mapping.length; m++)
      {
        if (mapping[m].getSequence() == sequence[s]
            && ap.av.alignment.findIndex(sequence[s])>-1)
        {
          for (int r = 0; r < sequence[s].getLength(); r++)
          {
            int pos = mapping[m].getPDBResNum(
                sequence[s].findPosition(r));

            if (pos < 1 || pos==lastPos)
              continue;

            lastPos = pos;

            Color col = sr.getResidueBoxColour(sequence[s], r);

            if (showFeatures)
              col = fr.findFeatureColour(col, sequence[s], r);

            if (command.toString().endsWith(":" + mapping[m].getChain()+
                                            ";color["
                                            + col.getRed() + ","
                                            + col.getGreen() + ","
                                            + col.getBlue() + "]"))
            {
              command = condenseCommand(command, pos);
              continue;
            }

            command.append(";select " + pos);

            if (!mapping[m].getChain().equals(" "))
            {
              command.append(":" + mapping[m].getChain());
            }

            command.append(";color["
                             + col.getRed() + ","
                             + col.getGreen() + ","
                             + col.getBlue() + "]");

          }
          break;
        }
      }
    }

    if (lastCommand == null || !lastCommand.equals(command.toString()))
    {
      viewer.evalStringQuiet(command.toString());
    }
    lastCommand = command.toString();
  }

  StringBuffer condenseCommand(StringBuffer command, int pos)
  {
    StringBuffer sb = new StringBuffer(command.substring(0, command.lastIndexOf("select")+7));

    command.delete(0, sb.length());

    String start;

    if (command.indexOf("-") > -1)
    {
      start = command.substring(0,command.indexOf("-"));
    }
    else
    {
      start = command.substring(0, command.indexOf(":"));
    }

    sb.append(start+"-"+pos+command.substring(command.indexOf(":")));

    return sb;
  }

  /////////////////////////////////
  //JmolStatusListener

  public String eval(String strEval)
  {
   // System.out.println(strEval);
   //"# 'eval' is implemented only for the applet.";
    return null;
  }

  public void createImage(String file, String type, int quality)
  {
    System.out.println("JMOL CREATE IMAGE");
  }

  public void setCallbackFunction(String callbackType,
                                  String callbackFunction)
  {}

  public void notifyFileLoaded(String fullPathName, String fileName,
                               String modelName, Object clientFile,
                               String errorMsg)
  {
    if(errorMsg!=null)
    {
      fileLoadingError = errorMsg;
      repaint();
      return;
    }

    fileLoadingError = null;

    if (fileName != null)
    {

      //FILE LOADED OK
      ssm = StructureSelectionManager.getStructureSelectionManager();
      MCview.PDBfile pdbFile = ssm.setMapping(sequence,chains,pdbentry.getFile(), AppletFormatAdapter.FILE);
      ssm.addStructureViewerListener(this);
      Vector chains = new Vector();
      for(int i=0; i<pdbFile.chains.size(); i++)
      {
        chains.addElement(((MCview.PDBChain)pdbFile.chains.elementAt(i)).id);
      }
      setChainMenuItems(chains);

      jmolpopup.updateComputedMenus();

      if(!loadingFromArchive)
      {
        viewer.evalStringQuiet(
             "select backbone;restrict;cartoon;wireframe off;spacefill off");

        colourBySequence(ap);
      }
      if (fr!=null)
        fr.featuresAdded();

      viewer.evalStringQuiet("set picking label");

      loadingFromArchive = false;
    }
    else
      return;
  }

  public void notifyFrameChanged(int frameNo)
  {
    boolean isAnimationRunning = (frameNo <= -2);
  }

  public void notifyScriptStart(String statusMessage, String additionalInfo)
  {}

  public void sendConsoleEcho(String strEcho)
  {
    if (scriptWindow != null)
      scriptWindow.sendConsoleEcho(strEcho);
  }

  public void sendConsoleMessage(String strStatus)
  {
    if (scriptWindow != null)
      scriptWindow.sendConsoleMessage(strStatus);
  }

  public void notifyScriptTermination(String strStatus, int msWalltime)
  {
    if (scriptWindow != null)
      scriptWindow.notifyScriptTermination(strStatus, msWalltime);
  }

  public void handlePopupMenu(int x, int y)
  {
    jmolpopup.show(x, y);
  }

  public void notifyNewPickingModeMeasurement(int iatom, String strMeasure)
  {
    notifyAtomPicked(iatom, strMeasure);
  }

  public void notifyNewDefaultModeMeasurement(int count, String strInfo)
  {}

  public void notifyAtomPicked(int atomIndex, String strInfo)
  {
    Matcher matcher = pattern.matcher(strInfo);
    matcher.find();

    matcher.group(1);
    String resnum = new String(matcher.group(2));
    String chainId = matcher.group(3);

    String picked = resnum;

    if (chainId != null)
      picked+=(":"+chainId.substring(1, chainId.length()));

    picked+=".CA";


    if (!atomsPicked.contains(picked))
    {
      if(chainId!=null)
      viewer.evalString("select "+picked+";label %n %r:%c");
    else
      viewer.evalString("select "+picked+";label %n %r");
      atomsPicked.addElement(picked);
    }
    else
    {
      viewer.evalString("select "+picked+";label off");
      atomsPicked.removeElement(picked);
    }

    if (scriptWindow != null)
    {
      scriptWindow.sendConsoleMessage(strInfo);
      scriptWindow.sendConsoleMessage("\n");
    }
  }

  public void notifyAtomHovered(int atomIndex, String strInfo)
  {
    mouseOverStructure(atomIndex, strInfo);
  }

  public void sendSyncScript(String script, String appletName)
  {}

  public void showUrl(String url)
  {}

  public void showConsole(boolean showConsole)
  {
    if (scriptWindow == null)
      scriptWindow = new ScriptWindow(this);

    if(showConsole)
    {
      if(splitPane==null)
      {
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(renderPanel);
        splitPane.setBottomComponent(scriptWindow);
        this.getContentPane().add(splitPane, BorderLayout.CENTER);
      }

      splitPane.setDividerLocation(getHeight()-200);
      splitPane.validate();
    }
    else
    {
      if (splitPane != null)
        splitPane.setVisible(false);

      splitPane = null;

      this.getContentPane().add(renderPanel, BorderLayout.CENTER);
    }

    validate();
  }

  public float functionXY(String functionName, int x, int y)
  {
    return 0;
  }

  ///End JmolStatusListener
  ///////////////////////////////


  class RenderPanel
      extends JPanel
  {
    final Dimension currentSize = new Dimension();
    final Rectangle rectClip = new Rectangle();

    public void paintComponent(Graphics g)
    {
      getSize(currentSize);
      g.getClipBounds(rectClip);

      if (viewer == null)
      {
        g.setColor(Color.black);
        g.fillRect(0, 0, currentSize.width, currentSize.height);
        g.setColor(Color.white);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString("Retrieving PDB data....", 20, currentSize.height / 2);
      }
      else if(fileLoadingError!=null)
      {
        g.setColor(Color.black);
        g.fillRect(0, 0, currentSize.width, currentSize.height);
        g.setColor(Color.white);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString("Error loading file..." + pdbentry.getId(), 20,
                     currentSize.height / 2);
      }
      else
      {
        viewer.renderScreenImage(g, currentSize, rectClip);
      }
    }
  }

}
