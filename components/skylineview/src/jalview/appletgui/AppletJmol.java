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

package jalview.appletgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import jalview.datamodel.*;
import jalview.structure.*;
import jalview.io.*;

import org.jmol.api.*;
import org.jmol.adapter.smarter.SmarterJmolAdapter;

import org.jmol.popup.*;
import jalview.schemes.*;


public class AppletJmol extends Frame
    implements  StructureListener, JmolStatusListener,
    KeyListener, ActionListener, ItemListener

{
  Menu fileMenu = new Menu("File");
  Menu viewMenu = new Menu("View");
  Menu coloursMenu = new Menu("Colours");
  Menu chainMenu = new Menu("Show Chain");
  Menu helpMenu = new Menu("Help");
  MenuItem mappingMenuItem = new MenuItem("View Mapping");

  CheckboxMenuItem seqColour = new CheckboxMenuItem("By Sequence", true);
  MenuItem chain = new MenuItem("By Chain");
  MenuItem charge = new MenuItem("Charge & Cysteine");
  MenuItem zappo = new MenuItem("Zappo");
  MenuItem taylor = new MenuItem("Taylor");
  MenuItem hydro = new MenuItem("Hydrophobicity");
  MenuItem helix = new MenuItem("Helix Propensity");
  MenuItem strand = new MenuItem("Strand Propensity");
  MenuItem turn = new MenuItem("Turn Propensity");
  MenuItem buried = new MenuItem("Buried Index");
  MenuItem user = new MenuItem("User Defined Colours");

  MenuItem jmolHelp = new MenuItem("Jmol Help");

  JmolViewer viewer;
  JmolPopup jmolpopup;

  Panel scriptWindow;
  TextField inputLine;
  TextArea history;
  SequenceI[] sequence;
  String [] chains;
  StructureSelectionManager ssm;
  RenderPanel renderPanel;
  AlignmentPanel ap;
  String fileLoadingError;
  boolean loadedInline;
  PDBEntry pdbentry;
  boolean colourBySequence = true;
  Vector atomsPicked = new Vector();

  public AppletJmol(PDBEntry pdbentry,
                    SequenceI[] seq,
                    String[] chains,
                    AlignmentPanel ap,
                    String protocol)
  {
    this.ap = ap;
    this.sequence = seq;
    this.chains = chains;
    this.pdbentry = pdbentry;

   String alreadyMapped = StructureSelectionManager
        .getStructureSelectionManager()
        .alreadyMappedToFile(pdbentry.getId());

    if (alreadyMapped != null)
    {
       StructureSelectionManager.getStructureSelectionManager()
            .setMapping(seq, chains, pdbentry.getFile(), protocol);
       //PROMPT USER HERE TO ADD TO NEW OR EXISTING VIEW?
       //FOR NOW, LETS JUST OPEN A NEW WINDOW
    }

    renderPanel = new RenderPanel();

    this.add(renderPanel, BorderLayout.CENTER);
    viewer = JmolViewer.allocateViewer(renderPanel, null);//new SmarterJmolAdapter());

    /*    viewer.setAppletContext("jalview",
                       ap.av.applet.getDocumentBase(),
                            ap.av.applet.getCodeBase(),
                            null);
    */
    viewer.setJmolStatusListener(this);

    jmolpopup = JmolPopup.newJmolPopup(viewer);

    this.addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent evt)
          {
            closeViewer();
          }
        });

    MenuBar menuBar = new MenuBar();
    menuBar.add(fileMenu);
    fileMenu.add(mappingMenuItem);
    menuBar.add(viewMenu);
    mappingMenuItem.addActionListener(this);
    viewMenu.add(chainMenu);
    menuBar.add(coloursMenu);
    menuBar.add(helpMenu);

    charge.addActionListener(this);
    hydro.addActionListener(this);
    chain.addActionListener(this);
    seqColour.addItemListener(this);
    zappo.addActionListener(this);
    taylor.addActionListener(this);
    helix.addActionListener(this);
    strand.addActionListener(this);
    turn.addActionListener(this);
    buried.addActionListener(this);
    user.addActionListener(this);

    jmolHelp.addActionListener(this);

    coloursMenu.add(seqColour);
    coloursMenu.add(chain);
    coloursMenu.add(charge);
    coloursMenu.add(zappo);
    coloursMenu.add(taylor);
    coloursMenu.add(hydro);
    coloursMenu.add(helix);
    coloursMenu.add(strand);
    coloursMenu.add(turn);
    coloursMenu.add(buried);
    coloursMenu.add(user);

    helpMenu.add(jmolHelp);

    this.setMenuBar(menuBar);

    if(pdbentry.getFile()!=null)
    {
      if (protocol.equals(AppletFormatAdapter.PASTE))
        loadInline(pdbentry.getFile());
      else
          viewer.openFile(pdbentry.getFile());
    }

    jalview.bin.JalviewLite.addFrame(this, "Jmol", 400,400);
  }

    public void notifyMeasurementsChanged(){}

    public void scriptStatus(String s){}

    public void scriptEcho(String s){}

    public void setStatusMessage(String s){}

  public void loadInline(String string)
  {
    loadedInline = true;
    viewer.openStringInline(string);
  }


  void setChainMenuItems(Vector chains)
  {
    chainMenu.removeAll();

    MenuItem menuItem = new MenuItem("All");
    menuItem.addActionListener(this);

    chainMenu.add(menuItem);

    CheckboxMenuItem menuItemCB;
    for (int c = 0; c < chains.size(); c++)
    {
      menuItemCB = new CheckboxMenuItem(chains.elementAt(c).toString(), true);
      menuItemCB.addItemListener(this);
      chainMenu.add(menuItemCB);
    }
  }

  boolean allChainsSelected = false;
  void centerViewer()
  {
    StringBuffer cmd = new StringBuffer();
    for (int i = 0; i < chainMenu.getItemCount(); i++)
    {
      if (chainMenu.getItem(i) instanceof CheckboxMenuItem)
      {
        CheckboxMenuItem item = (CheckboxMenuItem) chainMenu.getItem(i);
        if (item.getState())
          cmd.append(":" + item.getLabel() + " or ");
      }
    }

    if (cmd.length() > 0)
      cmd.setLength(cmd.length() - 4);

    viewer.evalString("select *;restrict "
                      + cmd + ";cartoon;center " + cmd);
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
        .removeStructureViewerListener(this, pdbentry.getId());

    this.setVisible(false);
  }

  public void actionPerformed(ActionEvent evt)
  {
    if(evt.getSource()==mappingMenuItem)
    {
      jalview.appletgui.CutAndPasteTransfer cap
          = new jalview.appletgui.CutAndPasteTransfer(false, null);
      Frame frame = new Frame();
      frame.add(cap);

      jalview.bin.JalviewLite.addFrame(frame, "PDB - Sequence Mapping", 550,
                                       600);
      cap.setText(
          StructureSelectionManager.getStructureSelectionManager().printMapping(
              pdbentry.getFile())
          );
    }
    else if (evt.getSource() == charge)
    {
      colourBySequence = false;
      seqColour.setState(false);
      viewer.evalStringQuiet("select *;color white;select ASP,GLU;color red;"
                      +"select LYS,ARG;color blue;select CYS;color yellow");
    }

    else if (evt.getSource() == chain)
    {
      colourBySequence = false;
      seqColour.setState(false);
      viewer.evalStringQuiet("select *;color chain");
    }
    else if (evt.getSource() == zappo)
    {
      setJalviewColourScheme(new ZappoColourScheme());
    }
    else if (evt.getSource() == taylor)
    {
     setJalviewColourScheme(new TaylorColourScheme());
    }
    else if (evt.getSource() == hydro)
    {
      setJalviewColourScheme(new HydrophobicColourScheme());
    }
    else if (evt.getSource() == helix)
    {
      setJalviewColourScheme(new HelixColourScheme());
    }
    else if (evt.getSource() == strand)
    {
      setJalviewColourScheme(new StrandColourScheme());
    }
    else if (evt.getSource() == turn)
    {
      setJalviewColourScheme(new TurnColourScheme());
    }
    else if (evt.getSource() == buried)
    {
      setJalviewColourScheme(new BuriedColourScheme());
    }
    else if (evt.getSource() == user)
    {
      new UserDefinedColours(this);
    }
    else if(evt.getSource() == jmolHelp)
    {
      try{
        ap.av.applet.getAppletContext().showDocument(
            new java.net.URL("http://jmol.sourceforge.net/docs/JmolUserGuide/"),
            "jmolHelp");
      }catch(java.net.MalformedURLException ex){}
    }
    else
    {
      allChainsSelected = true;
      for (int i = 0; i < chainMenu.getItemCount(); i++)
      {
        if (chainMenu.getItem(i) instanceof CheckboxMenuItem)
          ( (CheckboxMenuItem) chainMenu.getItem(i)).setState(true);
      }
      centerViewer();
      allChainsSelected = false;
    }
  }

  public void setJalviewColourScheme(ColourSchemeI cs)
  {
    colourBySequence = false;
    seqColour.setState(false);

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

  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == seqColour)
    {
      lastCommand = null;
      colourBySequence = seqColour.getState();
      colourBySequence(ap);
    }
    else if (!allChainsSelected)
      centerViewer();
  }

  public void keyPressed(KeyEvent evt)
  {
    if (evt.getKeyCode() == KeyEvent.VK_ENTER
        && scriptWindow.isVisible())
    {
      viewer.evalString(inputLine.getText());
      history.append("\n$ "+inputLine.getText());
      inputLine.setText("");
    }

  }

  public void keyTyped(KeyEvent evt)
  {  }

  public void keyReleased(KeyEvent evt){}

  //////////////////////////////////
  ///StructureListener
  public String getPdbFile()
  {
    return "???";
  }



  String lastMessage;
  public void mouseOverStructure(int atomIndex, String strInfo)
  {
      int pdbResNum;

      int chainSeparator = strInfo.indexOf(":");

      if(chainSeparator==-1)
        chainSeparator = strInfo.indexOf(".");

      pdbResNum = Integer.parseInt(
          strInfo.substring(strInfo.indexOf("]")+ 1, chainSeparator));

      String chainId;

      if (strInfo.indexOf(":") > -1)
        chainId = strInfo.substring
            (strInfo.indexOf(":")+1, strInfo.indexOf("."));
      else
      {
        chainId = " ";
      }

      if (lastMessage == null || !lastMessage.equals(strInfo))
        ssm.mouseOverStructure(pdbResNum, chainId, pdbentry.getFile());

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

  public void updateColours(Object source)
  {
    colourBySequence( (AlignmentPanel) source);
  }

//End StructureListener
////////////////////////////

  public Color getColour(int atomIndex, int pdbResNum, String chain, String pdbfile)
  {
    if (!pdbfile.equals(pdbentry.getFile()))
      return null;

    return new Color(0,0,0);//viewer.getAtomArgb(atomIndex));
  }

  String lastCommand;
  FeatureRenderer fr=null;
  public void colourBySequence(AlignmentPanel sourceap)
  {
    this.ap = sourceap;

    if (!colourBySequence)
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
        fr = new jalview.appletgui.FeatureRenderer(ap.av);
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
            && ap.av.alignment.findIndex(sequence[s]) > -1)
        {
          for (int r = 0; r < sequence[s].getLength(); r++)
          {
            int pos = mapping[m].getPDBResNum(
                sequence[s].findPosition(r));

            if (pos < 1 || pos == lastPos)
              continue;

            lastPos = pos;

            Color col = sr.getResidueBoxColour(sequence[s], r);

            if (showFeatures)
              col = fr.findFeatureColour(col, sequence[s], r);

            if (command.toString().endsWith(":" + mapping[m].getChain() +
                                            ";color["
                                            + col.getRed() + ","
                                            + col.getGreen() + ","
                                            + col.getBlue() + "]"))
            {
              command = condenseCommand(command.toString(), pos);
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


  StringBuffer condenseCommand(String command, int pos)
  {

    StringBuffer sb = new StringBuffer(command.substring(0, command.lastIndexOf("select")+7));

    command = command.substring(sb.length());

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
  {}

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
      jmolpopup.updateComputedMenus();
            viewer.evalStringQuiet(
          "select backbone;restrict;cartoon;wireframe off;spacefill off");

      ssm = StructureSelectionManager.getStructureSelectionManager();

      MCview.PDBfile pdb;
      if (loadedInline)
      {
        pdb = ssm.setMapping(sequence,chains,
                                pdbentry.getFile(),
                                AppletFormatAdapter.PASTE);
        pdbentry.setFile("INLINE"+pdb.id);
      }
      else
      {
         pdb = ssm.setMapping(sequence,chains,
                              pdbentry.getFile(),
                              AppletFormatAdapter.URL);
      }

      pdbentry.setId(pdb.id);

      ssm.addStructureViewerListener(this);

      Vector chains = new Vector();
      for (int i = 0; i < pdb.chains.size(); i++)
      {
        chains.addElement( ( (MCview.PDBChain) pdb.chains.elementAt(i)).id);
      }
      setChainMenuItems(chains);

      colourBySequence(ap);

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


      viewer.evalStringQuiet("set picking label");

      this.setTitle(title.toString());

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
    if (scriptWindow == null)
      showConsole(true);

    history.append("\n"+strEcho);
  }

  public void sendConsoleMessage(String strStatus)
  {
    if(history!=null && strStatus!=null
       && !strStatus.equals("Script completed"))
    {
      history.append("\n"+strStatus);
    }
  }

  public void notifyScriptTermination(String strStatus, int msWalltime)
  {  }

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

    int chainSeparator = strInfo.indexOf(":");

    if(chainSeparator==-1)
      chainSeparator = strInfo.indexOf(".");

    String picked =
        strInfo.substring(strInfo.indexOf("]")+ 1, chainSeparator);


    if (strInfo.indexOf(":") > -1)
      picked+=strInfo.substring(strInfo.indexOf(":")+1,
                               strInfo.indexOf("."));

    picked+=".CA";

    if (!atomsPicked.contains(picked))
    {
      viewer.evalString("select "+picked+";label %n %r:%c");
      atomsPicked.addElement(picked);
    }
    else
    {
      viewer.evalString("select "+picked+";label off");
      atomsPicked.removeElement(picked);
    }
  }

  public void notifyAtomHovered(int atomIndex, String strInfo)
  {
    mouseOverStructure(atomIndex, strInfo);
  }

  public void sendSyncScript(String script, String appletName)
  {}

  public void showUrl(String url)
  {
    try{
      ap.av.applet.getAppletContext().showDocument(new java.net.URL(url),
          "jmolOutput");
    }catch(java.net.MalformedURLException ex)
    {}
  }

  public void showConsole(boolean showConsole)
  {
    if (scriptWindow == null)
    {
      scriptWindow = new Panel(new BorderLayout());
      inputLine = new TextField();
      history = new TextArea(5, 40);
      scriptWindow.add(history, BorderLayout.CENTER);
      scriptWindow.add(inputLine, BorderLayout.SOUTH);
      add(scriptWindow, BorderLayout.SOUTH);
      scriptWindow.setVisible(false);
      history.setEditable(false);
      inputLine.addKeyListener(this);
    }

    scriptWindow.setVisible(!scriptWindow.isVisible());
    validate();
  }

  public float functionXY(String functionName, int x, int y)
  {
    return 0;
  }

  ///End JmolStatusListener
  ///////////////////////////////


  class RenderPanel
      extends Panel
  {
    Dimension currentSize = new Dimension();
    Rectangle rectClip = new Rectangle();

    public void update(Graphics g) {
      paint(g);
    }
    public void paint(Graphics g)
    {
      currentSize = this.getSize();
      rectClip = g.getClipBounds();

      if (viewer == null)
      {
        g.setColor(Color.black);
        g.fillRect(0, 0, currentSize.width, currentSize.height);
        g.setColor(Color.white);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString("Retrieving PDB data....", 20, currentSize.height / 2);
      }
      else
      {
        viewer.renderScreenImage(g, currentSize, rectClip);
      }
    }
  }

}
