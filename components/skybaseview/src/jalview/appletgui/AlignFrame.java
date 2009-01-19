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

import java.io.*;
import java.net.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import jalview.analysis.*;
import jalview.commands.*;
import jalview.datamodel.*;
import jalview.io.*;
import jalview.schemes.*;

public class AlignFrame
    extends Frame implements ActionListener,
    ItemListener, KeyListener, MouseListener
{
  public AlignmentPanel alignPanel;
  public AlignViewport viewport;
  int DEFAULT_WIDTH = 700;
  int DEFAULT_HEIGHT = 500;

  String jalviewServletURL;

  public AlignFrame(AlignmentI al,
                    jalview.bin.JalviewLite applet,
                    String title,
                    boolean embedded)
  {

    if (applet != null)
    {
      jalviewServletURL = applet.getParameter("APPLICATION_URL");
    }

    try
    {
      jbInit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    viewport = new AlignViewport(al, applet);
    alignPanel = new AlignmentPanel(this, viewport);

    viewport.updateConservation(alignPanel);
    viewport.updateConsensus(alignPanel);

    annotationPanelMenuItem.setState(viewport.showAnnotation);

    seqLimits.setState(viewport.showJVSuffix);

    if (applet != null)
    {
      String param = applet.getParameter("sortBy");
      if (param != null)
      {
        if (param.equalsIgnoreCase("Id"))
        {
          sortIDMenuItem_actionPerformed();
        }
        else if (param.equalsIgnoreCase("Pairwise Identity"))
        {
          sortPairwiseMenuItem_actionPerformed();
        }
      }

      param = applet.getParameter("wrap");
      if (param != null)
      {
        if (param.equalsIgnoreCase("true"))
        {
          wrapMenuItem.setState(true);
          wrapMenuItem_actionPerformed();
        }
      }

      try
      {
        param = applet.getParameter("windowWidth");
        if (param != null)
        {
          int width = Integer.parseInt(param);
          DEFAULT_WIDTH = width;
        }
        param = applet.getParameter("windowHeight");
        if (param != null)
        {
          int height = Integer.parseInt(param);
          DEFAULT_HEIGHT = height;
        }
      }
      catch (Exception ex)
      {}

    }

    //Some JVMS send keyevents to Top frame or lowest panel,
    //Havent worked out why yet. So add to both this frame and seqCanvas for now
    this.addKeyListener(this);
    alignPanel.seqPanel.seqCanvas.addKeyListener(this);
    alignPanel.idPanel.idCanvas.addKeyListener(this);
    alignPanel.scalePanel.addKeyListener(this);
    alignPanel.annotationPanel.addKeyListener(this);

    if (embedded)
    {
      setEmbedded();
    }
    else
    {
      add(alignPanel, BorderLayout.CENTER);
      jalview.bin.JalviewLite.addFrame(this, title, DEFAULT_WIDTH,
                                       DEFAULT_HEIGHT);
    }
    alignPanel.validate();
    alignPanel.paintAlignment(true);
  }

  public AlignViewport getAlignViewport()
  {
    return viewport;
  }

  public SeqCanvas getSeqcanvas()
  {
    return alignPanel.seqPanel.seqCanvas;
  }

  /**
   * DOCUMENT ME!
   *
   * @param String DOCUMENT ME!
   */

  public void parseFeaturesFile(String file, String type)
  {
    Hashtable featureLinks = new Hashtable();
    boolean featuresFile = false;
    try
    {
      featuresFile = new jalview.io.FeaturesFile(file,
          type).parse(viewport.alignment,
                      alignPanel.seqPanel.seqCanvas.
                      getFeatureRenderer().featureColours,
                      featureLinks,
                      true);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    if (featuresFile)
    {
      if (featureLinks.size() > 0)
      {
        alignPanel.seqPanel.seqCanvas
            .getFeatureRenderer().featureLinks = featureLinks;
      }
      viewport.showSequenceFeatures = true;
      sequenceFeatures.setState(true);
      alignPanel.paintAlignment(true);
    }

  }

  public void keyPressed(KeyEvent evt)
  {
    if (viewport.cursorMode &&
        ( (evt.getKeyCode() >= KeyEvent.VK_0 &&
           evt.getKeyCode() <= KeyEvent.VK_9)
         ||
         (evt.getKeyCode() >= KeyEvent.VK_NUMPAD0 &&
          evt.getKeyCode() <= KeyEvent.VK_NUMPAD9)
        )
        && Character.isDigit(evt.getKeyChar()))
            alignPanel.seqPanel.numberPressed(evt.getKeyChar());


    switch (evt.getKeyCode())
    {
      case 27: // escape key
        deselectAllSequenceMenuItem_actionPerformed();
        break;
      case KeyEvent.VK_X:
        if (evt.isControlDown() || evt.isMetaDown())
        {
          cut_actionPerformed();
        }
        break;
      case KeyEvent.VK_C:
        if (viewport.cursorMode && !evt.isControlDown())
        {
          alignPanel.seqPanel.setCursorColumn();
        }
        if (evt.isControlDown() || evt.isMetaDown())
        {
          copy_actionPerformed();
        }
        break;
      case KeyEvent.VK_V:
        if (evt.isControlDown())
        {
          paste(evt.isShiftDown());
        }
        break;
      case KeyEvent.VK_A:
        if (evt.isControlDown() || evt.isMetaDown())
        {
          selectAllSequenceMenuItem_actionPerformed();
        }
        break;
      case KeyEvent.VK_DOWN:
        if (viewport.cursorMode)
        {
          alignPanel.seqPanel.moveCursor(0, 1);
        }
        else
        {
          moveSelectedSequences(false);
        }
        break;

      case KeyEvent.VK_UP:
        if (viewport.cursorMode)
        {
          alignPanel.seqPanel.moveCursor(0, -1);
        }
        else
        {
          moveSelectedSequences(true);
        }
        break;

      case KeyEvent.VK_LEFT:
        if (evt.isAltDown() || !viewport.cursorMode)
          slideSequences(false,
                         alignPanel.seqPanel.getKeyboardNo1());
        else
          alignPanel.seqPanel.moveCursor( -1, 0);
        break;

      case KeyEvent.VK_RIGHT:
        if (evt.isAltDown() || !viewport.cursorMode)
          slideSequences(true,
                         alignPanel.seqPanel.getKeyboardNo1());
        else
          alignPanel.seqPanel.moveCursor( 1, 0);
            break;

      case KeyEvent.VK_SPACE:
        if (viewport.cursorMode)
        {
          alignPanel.seqPanel.insertGapAtCursor(evt.isControlDown()
                                                || evt.isShiftDown()
                                                || evt.isAltDown()
              );
        }
        break;

      case KeyEvent.VK_DELETE:
      case KeyEvent.VK_BACK_SPACE:
        if (viewport.cursorMode)
        {
          alignPanel.seqPanel.deleteGapAtCursor(evt.isControlDown()
                                                || evt.isShiftDown()
                                                || evt.isAltDown());
        }
        else
        {
          cut_actionPerformed();
          alignPanel.seqPanel.seqCanvas.repaint();
        }
        break;

      case KeyEvent.VK_S:
        if (viewport.cursorMode)
        {
          alignPanel.seqPanel.setCursorRow();
        }
        break;
      case KeyEvent.VK_P:
        if (viewport.cursorMode)
        {
          alignPanel.seqPanel.setCursorPosition();
        }
        break;

      case KeyEvent.VK_ENTER:
      case KeyEvent.VK_COMMA:
        if (viewport.cursorMode)
        {
          alignPanel.seqPanel.setCursorRowAndColumn();
        }
        break;

      case KeyEvent.VK_Q:
        if (viewport.cursorMode)
        {
          alignPanel.seqPanel.setSelectionAreaAtCursor(true);
        }
        break;
      case KeyEvent.VK_M:
        if (viewport.cursorMode)
        {
          alignPanel.seqPanel.setSelectionAreaAtCursor(false);
        }
        break;

      case KeyEvent.VK_F2:
        viewport.cursorMode = !viewport.cursorMode;
        statusBar.setText("Keyboard editing mode is " +
                          (viewport.cursorMode ? "on" : "off"));
        if (viewport.cursorMode)
        {
          alignPanel.seqPanel.seqCanvas.cursorX = viewport.startRes;
          alignPanel.seqPanel.seqCanvas.cursorY = viewport.startSeq;
        }
        break;

      case KeyEvent.VK_F:
        if (evt.isControlDown())
        {
          findMenuItem_actionPerformed();
        }
        break;

      case KeyEvent.VK_H:
      {
        boolean toggleSeqs = !evt.isControlDown();
        boolean toggleCols = !evt.isShiftDown();
        boolean hide = false;
        SequenceGroup sg = viewport.getSelectionGroup();

        if (toggleSeqs)
        {
          if (sg != null && sg.getSize() != viewport.alignment.getHeight())
          {
            hide = true;
            viewport.hideAllSelectedSeqs();
          }
          else if (! (toggleCols && viewport.colSel.getSelected().size() > 0))
          {
            viewport.showAllHiddenSeqs();
          }
        }

        if (toggleCols)
        {
          if (viewport.colSel.getSelected().size() > 0)
          {
            viewport.hideSelectedColumns();
            if (!toggleSeqs)
            {
              viewport.selectionGroup = sg;
            }
          }
          else if (!hide)
          {
            viewport.showAllHiddenColumns();
          }
        }
        break;
      }

      case KeyEvent.VK_PAGE_UP:
        if (viewport.wrapAlignment)
        {
          alignPanel.scrollUp(true);
        }
        else
        {
          alignPanel.setScrollValues(viewport.startRes,
                                     viewport.startSeq
                                     - viewport.endSeq + viewport.startSeq);
        }
        break;

      case KeyEvent.VK_PAGE_DOWN:
        if (viewport.wrapAlignment)
        {
          alignPanel.scrollUp(false);
        }
        else
        {
          alignPanel.setScrollValues(viewport.startRes,
                                     viewport.startSeq
                                     + viewport.endSeq - viewport.startSeq);
        }
        break;

      case KeyEvent.VK_Z:
        if (evt.isControlDown())
        {
          undoMenuItem_actionPerformed();
        }
        break;

      case KeyEvent.VK_Y:
        if (evt.isControlDown())
        {
          redoMenuItem_actionPerformed();
        }
        break;

      case KeyEvent.VK_L:
        if (evt.isControlDown())
        {
          trimAlignment(true);
        }
        break;

      case KeyEvent.VK_R:
        if (evt.isControlDown())
        {
          trimAlignment(false);
        }
        break;

      case KeyEvent.VK_E:
        if (evt.isControlDown())
        {
          if (evt.isShiftDown())
          {
            this.removeAllGapsMenuItem_actionPerformed();
          }
          else
          {
            removeGappedColumnMenuItem_actionPerformed();
          }
        }
        break;
      case KeyEvent.VK_I:
        if (evt.isControlDown())
        {
          if (evt.isAltDown())
          {
            viewport.invertColumnSelection();
          }
          else
          {
            this.invertSequenceMenuItem_actionPerformed();
          }
        }
        break;

      case KeyEvent.VK_U:
        if (evt.isControlDown())
        {
          this.deleteGroups_actionPerformed();
        }
        break;

      case KeyEvent.VK_T:
        if (evt.isControlDown())
        {
          newView();
        }
        break;

    }
    alignPanel.paintAlignment(true);
  }

  public void keyReleased(KeyEvent evt)
  {}

  public void keyTyped(KeyEvent evt)
  {}

  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == colourTextMenuItem)
    {
      colourTextMenuItem_actionPerformed();
    }
    else if (evt.getSource() == wrapMenuItem)
    {
      wrapMenuItem_actionPerformed();
    }
    else if (evt.getSource() == scaleAbove)
    {
      viewport.setScaleAboveWrapped(scaleAbove.getState());
    }
    else if (evt.getSource() == scaleLeft)
    {
      viewport.setScaleLeftWrapped(scaleLeft.getState());
    }
    else if (evt.getSource() == scaleRight)
    {
      viewport.setScaleRightWrapped(scaleRight.getState());
    }
    else if (evt.getSource() == seqLimits)
    {
      seqLimits_itemStateChanged();
    }
    else if (evt.getSource() == viewBoxesMenuItem)
    {
      viewport.setShowBoxes(viewBoxesMenuItem.getState());
    }
    else if (evt.getSource() == viewTextMenuItem)
    {
      viewport.setShowText(viewTextMenuItem.getState());
    }
    else if (evt.getSource() == renderGapsMenuItem)
    {
      viewport.setRenderGaps(renderGapsMenuItem.getState());
    }
    else if (evt.getSource() == annotationPanelMenuItem)
    {
      viewport.setShowAnnotation(annotationPanelMenuItem.getState());
      alignPanel.setAnnotationVisible(annotationPanelMenuItem.getState());
    }
    else if (evt.getSource() == sequenceFeatures)
    {
      viewport.showSequenceFeatures(sequenceFeatures.getState());
      alignPanel.seqPanel.seqCanvas.repaint();
    }
    else if (evt.getSource() == conservationMenuItem)
    {
      conservationMenuItem_actionPerformed();
    }
    else if (evt.getSource() == abovePIDThreshold)
    {
      abovePIDThreshold_actionPerformed();
    }
    else if (evt.getSource() == applyToAllGroups)
    {
      viewport.setColourAppliesToAllGroups(applyToAllGroups.getState());
    }
    else if (evt.getSource() == autoCalculate)
    {
      viewport.autocalculateConsensus = autoCalculate.getState();
    }

    alignPanel.paintAlignment(true);
  }

  public void actionPerformed(ActionEvent evt)
  {
    Object source = evt.getSource();

    if (source == inputText)
    {
      inputText_actionPerformed();
    }
    else if (source == loadTree)
    {
      loadTree_actionPerformed();
    }
    else if (source == loadApplication)
    {
      launchFullApplication();
    }
    else if (source == loadAnnotations)
    {
      loadAnnotations();
    }
    else if (source == outputAnnotations)
    {
      outputAnnotations(true);
    }
    else if (source == outputFeatures)
    {
      outputFeatures(true, "Jalview");
    }
    else if (source == closeMenuItem)
    {
      closeMenuItem_actionPerformed();
    }
    else if (source == copy)
    {
      copy_actionPerformed();
    }
    else if (source == undoMenuItem)
    {
      undoMenuItem_actionPerformed();
    }
    else if (source == redoMenuItem)
    {
      redoMenuItem_actionPerformed();
    }
    else if (source == inputText)
    {
      inputText_actionPerformed();
    }
    else if (source == closeMenuItem)
    {
      closeMenuItem_actionPerformed();
    }
    else if (source == undoMenuItem)
    {
      undoMenuItem_actionPerformed();
    }
    else if (source == redoMenuItem)
    {
      redoMenuItem_actionPerformed();
    }
    else if (source == copy)
    {
      copy_actionPerformed();
    }
    else if (source == pasteNew)
    {
      pasteNew_actionPerformed();
    }
    else if (source == pasteThis)
    {
      pasteThis_actionPerformed();
    }
    else if (source == cut)
    {
      cut_actionPerformed();
    }
    else if (source == delete)
    {
      delete_actionPerformed();
    }
    else if (source == deleteGroups)
    {
      deleteGroups_actionPerformed();
    }
    else if (source == selectAllSequenceMenuItem)
    {
      selectAllSequenceMenuItem_actionPerformed();
    }
    else if (source == deselectAllSequenceMenuItem)
    {
      deselectAllSequenceMenuItem_actionPerformed();
    }
    else if (source == invertSequenceMenuItem)
    {
      invertSequenceMenuItem_actionPerformed();
    }
    else if (source == invertColSel)
    {
      viewport.invertColumnSelection();
      alignPanel.paintAlignment(true);
    }
    else if (source == remove2LeftMenuItem)
    {
      trimAlignment(true);
    }
    else if (source == remove2RightMenuItem)
    {
      trimAlignment(false);
    }
    else if (source == removeGappedColumnMenuItem)
    {
      removeGappedColumnMenuItem_actionPerformed();
    }
    else if (source == removeAllGapsMenuItem)
    {
      removeAllGapsMenuItem_actionPerformed();
    }
    else if (source == findMenuItem)
    {
      findMenuItem_actionPerformed();
    }
    else if (source == font)
    {
      new FontChooser(alignPanel);
    }
    else if (source == newView)
    {
      newView();
    }
    else if (source == showColumns)
    {
      viewport.showAllHiddenColumns();
      alignPanel.paintAlignment(true);
    }
    else if (source == showSeqs)
    {
      viewport.showAllHiddenSeqs();
    }
    else if (source == hideColumns)
    {
      viewport.hideSelectedColumns();
      alignPanel.paintAlignment(true);
    }
    else if (source == hideSequences && viewport.getSelectionGroup() != null)
    {
      viewport.hideAllSelectedSeqs();
    }
    else if (source == featureSettings)
    {
      new FeatureSettings(alignPanel);
    }
    else if(source== alProperties)
    {
        StringBuffer contents = new StringBuffer();

        float avg  = 0;
        int min=Integer.MAX_VALUE, max=0;
        for(int i=0; i<viewport.alignment.getHeight(); i++)
        {
          int size = viewport.alignment.getSequenceAt(i).getEnd()
              -viewport.alignment.getSequenceAt(i).getStart();
          avg += size;
          if(size>max)
            max = size;
          if(size<min)
            min = size;
        }
        avg = avg/(float)viewport.alignment.getHeight();

        contents.append("\nSequences: "+ viewport.alignment.getHeight());
        contents.append("\nMinimum Sequence Length: "+min);
        contents.append("\nMaximum Sequence Length: "+max);
        contents.append("\nAverage Length: "+(int)avg);

        if (((Alignment)viewport.alignment).alignmentProperties != null)
        {
          Hashtable props = ((Alignment)viewport.alignment).alignmentProperties;
          Enumeration en = props.keys();
          while(en.hasMoreElements())
          {
            String key = en.nextElement().toString();
            contents.append("\n"+key+"\t"+props.get(key));
          }
        }

        CutAndPasteTransfer cap = new CutAndPasteTransfer(false, this);
        cap.setText(contents.toString());
        Frame frame = new Frame();
        frame.add(cap);
        jalview.bin.JalviewLite.addFrame(frame,
                                         "Alignment Properties: "+getTitle()
                                         , 400, 250);
    }
    else if (source == overviewMenuItem)
    {
      overviewMenuItem_actionPerformed();
    }
    else if (source == noColourmenuItem)
    {
      changeColour(null);
    }
    else if (source == clustalColour)
    {
      abovePIDThreshold.setState(false);
      changeColour(new ClustalxColourScheme(viewport.alignment.getSequences(),
                                            viewport.alignment.getWidth()));
    }
    else if (source == zappoColour)
    {
      changeColour(new ZappoColourScheme());
    }
    else if (source == taylorColour)
    {
      changeColour(new TaylorColourScheme());
    }
    else if (source == hydrophobicityColour)
    {
      changeColour(new HydrophobicColourScheme());
    }
    else if (source == helixColour)
    {
      changeColour(new HelixColourScheme());
    }
    else if (source == strandColour)
    {
      changeColour(new StrandColourScheme());
    }
    else if (source == turnColour)
    {
      changeColour(new TurnColourScheme());
    }
    else if (source == buriedColour)
    {
      changeColour(new BuriedColourScheme());
    }
    else if (source == nucleotideColour)
    {
      changeColour(new NucleotideColourScheme());
    }
    else if (source == modifyPID)
    {
      modifyPID_actionPerformed();
    }
    else if (source == modifyConservation)
    {
      modifyConservation_actionPerformed();
    }
    else if (source == userDefinedColour)
    {
      new UserDefinedColours(alignPanel, null);
    }
    else if (source == PIDColour)
    {
      changeColour(new PIDColourScheme());
    }
    else if (source == BLOSUM62Colour)
    {
      changeColour(new Blosum62ColourScheme());
    }
    else if (source == annotationColour)
    {
      new AnnotationColourChooser(viewport, alignPanel);
    }
    else if (source == sortPairwiseMenuItem)
    {
      sortPairwiseMenuItem_actionPerformed();
    }
    else if (source == sortIDMenuItem)
    {
      sortIDMenuItem_actionPerformed();
    }
    else if (source == sortGroupMenuItem)
    {
      sortGroupMenuItem_actionPerformed();
    }
    else if (source == removeRedundancyMenuItem)
    {
      removeRedundancyMenuItem_actionPerformed();
    }
    else if (source == pairwiseAlignmentMenuItem)
    {
      pairwiseAlignmentMenuItem_actionPerformed();
    }
    else if (source == PCAMenuItem)
    {
      PCAMenuItem_actionPerformed();
    }
    else if (source == averageDistanceTreeMenuItem)
    {
      averageDistanceTreeMenuItem_actionPerformed();
    }
    else if (source == neighbourTreeMenuItem)
    {
      neighbourTreeMenuItem_actionPerformed();
    }
    else if (source == njTreeBlosumMenuItem)
    {
      njTreeBlosumMenuItem_actionPerformed();
    }
    else if (source == avDistanceTreeBlosumMenuItem)
    {
      avTreeBlosumMenuItem_actionPerformed();
    }
    else if (source == documentation)
    {
      documentation_actionPerformed();
    }
    else if (source == about)
    {
      about_actionPerformed();
    }

  }

  public void inputText_actionPerformed()
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer(true, this);
    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame, "Cut & Paste Input", 500, 500);
  }

  protected void outputText_actionPerformed(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer(true, this);
    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame,
                                     "Alignment output - " + e.getActionCommand(),
                                     600, 500);
    cap.setText(new AppletFormatAdapter().formatSequences(e.getActionCommand(),
        viewport.getAlignment(),
        viewport.showJVSuffix));
  }

  public void loadAnnotations()
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer(true, this);
    cap.setText("Paste your features / annotations file here.");
    cap.setAnnotationImport();
    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame, "Paste Annotations ", 400, 300);

  }

  public String outputAnnotations(boolean displayTextbox)
  {
    String annotation = new AnnotationFile().printAnnotations(
        viewport.showAnnotation ? viewport.alignment.getAlignmentAnnotation() : null,
        viewport.alignment.getGroups(),
         ((Alignment) viewport.alignment).alignmentProperties);

    if (displayTextbox)
    {
      CutAndPasteTransfer cap = new CutAndPasteTransfer(false, this);
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame,
                                       "Annotations",
                                       600, 500);
      cap.setText(annotation);
    }

    return annotation;
  }

  public String outputFeatures(boolean displayTextbox, String format)
  {
    String features;
    if (format.equalsIgnoreCase("Jalview"))
    {
      features = new FeaturesFile().printJalviewFormat(
          viewport.alignment.getSequencesArray(),
          viewport.featuresDisplayed);
    }
    else
    {
      features = new FeaturesFile().printGFFFormat(
          viewport.alignment.getSequencesArray(),
          viewport.featuresDisplayed);
    }

    if (displayTextbox)
    {
      CutAndPasteTransfer cap = new CutAndPasteTransfer(false, this);
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame,
                                       "Features",
                                       600, 500);

      cap.setText(features);
    }

    return features;
  }

  void launchFullApplication()
  {
    StringBuffer url = new StringBuffer(jalviewServletURL);

    url.append("?open=" +
               appendProtocol(viewport.applet.getParameter("file")));

    if (viewport.applet.getParameter("features") != null)
    {
      url.append("&features=");
      url.append(appendProtocol(viewport.applet.getParameter("features")));
    }

    if (viewport.applet.getParameter("annotations") != null)
    {
      url.append("&annotations=");
      url.append(appendProtocol(viewport.applet.getParameter("annotations")));
    }

    if (viewport.applet.getParameter("jnetfile") != null)
    {
      url.append("&annotations=");
      url.append(appendProtocol(viewport.applet.getParameter("jnetfile")));
    }

    if (viewport.applet.getParameter("defaultColour") != null)
    {
      url.append("&colour=" +
                 removeWhiteSpace(viewport.applet.getParameter("defaultColour"))
          );
    }

    if (viewport.applet.getParameter("userDefinedColour") != null)
    {
      url.append("&colour=" +
                 removeWhiteSpace(viewport.applet.getParameter(
          "userDefinedColour"))
          );
    }

    showURL(url.toString(), "FULL_APP");
  }

  String removeWhiteSpace(String colour)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < colour.length(); i++)
    {
      if (Character.isWhitespace(colour.charAt(i)))
      {
        sb.append("%20");
      }
      else
      {
        sb.append(colour.charAt(i));
      }
    }

    return sb.toString();
  }

  String appendProtocol(String url)
  {
    try
    {
      new URL(url);
    }
    catch (java.net.MalformedURLException ex)
    {
      url = viewport.applet.getCodeBase() + url;
    }
    return url;
  }

  public void closeMenuItem_actionPerformed()
  {
    PaintRefresher.RemoveComponent(alignPanel);
    PaintRefresher.RemoveComponent(alignPanel.seqPanel.seqCanvas);
    PaintRefresher.RemoveComponent(alignPanel.idPanel.idCanvas);

    if (PaintRefresher.components.size() == 0 && viewport.applet == null)
    {
      System.exit(0);
    }

    this.dispose();
  }

  /**
   * DOCUMENT ME!
   */
  void updateEditMenuBar()
  {

    if (viewport.historyList.size() > 0)
    {
      undoMenuItem.setEnabled(true);
      CommandI command = (CommandI) viewport.historyList.peek();
      undoMenuItem.setLabel("Undo " + command.getDescription());
    }
    else
    {
      undoMenuItem.setEnabled(false);
      undoMenuItem.setLabel("Undo");
    }

    if (viewport.redoList.size() > 0)
    {
      redoMenuItem.setEnabled(true);

      CommandI command = (CommandI) viewport.redoList.peek();
      redoMenuItem.setLabel("Redo " + command.getDescription());
    }
    else
    {
      redoMenuItem.setEnabled(false);
      redoMenuItem.setLabel("Redo");
    }
  }

  public void addHistoryItem(CommandI command)
  {
    if (command.getSize() > 0)
    {
      viewport.historyList.push(command);
      viewport.redoList.removeAllElements();
      updateEditMenuBar();
      viewport.hasHiddenColumns = viewport.colSel.getHiddenColumns() != null;
    }
  }
  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void undoMenuItem_actionPerformed()
  {
    if (viewport.historyList.size() < 1)
    {
      return;
    }

    CommandI command = (CommandI) viewport.historyList.pop();
    viewport.redoList.push(command);
    command.undoCommand(null);

    AlignViewport originalSource = getOriginatingSource(command);

    originalSource.hasHiddenColumns = viewport.colSel.getHiddenColumns() != null;
    updateEditMenuBar();
    originalSource.firePropertyChange("alignment", null,
                                      originalSource.alignment.getSequences());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void redoMenuItem_actionPerformed()
  {
    if (viewport.redoList.size() < 1)
    {
      return;
    }

    CommandI command = (CommandI) viewport.redoList.pop();
    viewport.historyList.push(command);
    command.doCommand(null);

    AlignViewport originalSource = getOriginatingSource(command);
    originalSource.hasHiddenColumns = viewport.colSel.getHiddenColumns() != null;

    updateEditMenuBar();
    originalSource.firePropertyChange("alignment", null,
                                      originalSource.alignment.getSequences());
  }

  AlignViewport getOriginatingSource(CommandI command)
  {
    AlignViewport originalSource = null;
    //For sequence removal and addition, we need to fire
    //the property change event FROM the viewport where the
    //original alignment was altered
    AlignmentI al = null;
    if (command instanceof EditCommand)
    {
      EditCommand editCommand = (EditCommand) command;
      al = editCommand.getAlignment();
      Vector comps = (Vector) PaintRefresher.components
          .get(viewport.getSequenceSetId());
      for (int i = 0; i < comps.size(); i++)
      {
        if (comps.elementAt(i) instanceof AlignmentPanel)
        {
          if (al == ( (AlignmentPanel) comps.elementAt(i)).av.alignment)
          {
            originalSource = ( (AlignmentPanel) comps.elementAt(i)).av;
            break;
          }
        }
      }
    }

    if (originalSource == null)
    {
      //The original view is closed, we must validate
      //the current view against the closed view first
      if (al != null)
      {
        PaintRefresher.validateSequences(al, viewport.alignment);
      }

      originalSource = viewport;
    }

    return originalSource;
  }

  public void moveSelectedSequences(boolean up)
  {
    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg == null)
    {
      return;
    }

    if (up)
    {
      for (int i = 1; i < viewport.alignment.getHeight(); i++)
      {
        SequenceI seq = viewport.alignment.getSequenceAt(i);
        if (!sg.getSequences(null).contains(seq))
        {
          continue;
        }

        SequenceI temp = viewport.alignment.getSequenceAt(i - 1);
        if (sg.getSequences(null).contains(temp))
        {
          continue;
        }

        viewport.alignment.getSequences().setElementAt(temp, i);
        viewport.alignment.getSequences().setElementAt(seq, i - 1);
      }
    }
    else
    {
      for (int i = viewport.alignment.getHeight() - 2; i > -1; i--)
      {
        SequenceI seq = viewport.alignment.getSequenceAt(i);
        if (!sg.getSequences(viewport.hiddenRepSequences).contains(seq))
        {
          continue;
        }

        SequenceI temp = viewport.alignment.getSequenceAt(i + 1);
        if (sg.getSequences(viewport.hiddenRepSequences).contains(temp))
        {
          continue;
        }

        viewport.alignment.getSequences().setElementAt(temp, i);
        viewport.alignment.getSequences().setElementAt(seq, i + 1);
      }
    }

    alignPanel.paintAlignment(true);
  }

  synchronized void slideSequences(boolean right, int size)
  {
    Vector sg = new Vector();
    if(viewport.cursorMode)
    {
      sg.addElement(viewport.alignment.getSequenceAt(
          alignPanel.seqPanel.seqCanvas.cursorY));
    }
    else if(viewport.getSelectionGroup()!=null
        && viewport.getSelectionGroup().getSize()!=viewport.alignment.getHeight())
   {
     sg = viewport.getSelectionGroup().getSequences(
         viewport.hiddenRepSequences);
   }

    if(sg.size()<1)
    {
      return;
    }

    Vector invertGroup = new Vector();

    for (int i = 0; i < viewport.alignment.getHeight(); i++)
    {
      if(!sg.contains(viewport.alignment.getSequenceAt(i)))
         invertGroup.addElement(viewport.alignment.getSequenceAt(i));
    }

    SequenceI[] seqs1 = new SequenceI[sg.size()];
    for (int i = 0; i < sg.size(); i++)
      seqs1[i] = (SequenceI) sg.elementAt(i);

    SequenceI[] seqs2 = new SequenceI[invertGroup.size()];
    for (int i = 0; i < invertGroup.size(); i++)
      seqs2[i] = (SequenceI) invertGroup.elementAt(i);

    SlideSequencesCommand ssc;
    if (right)
      ssc = new SlideSequencesCommand("Slide Sequences",
                                      seqs2, seqs1, size,
                                      viewport.getGapCharacter()
          );
    else
      ssc = new SlideSequencesCommand("Slide Sequences",
                                      seqs1, seqs2, size,
                                      viewport.getGapCharacter()
          );

    int groupAdjustment = 0;
    if (ssc.getGapsInsertedBegin() && right)
    {
      if (viewport.cursorMode)
        alignPanel.seqPanel.moveCursor(size, 0);
      else
        groupAdjustment = size;
    }
    else if (!ssc.getGapsInsertedBegin() && !right)
    {
      if (viewport.cursorMode)
        alignPanel.seqPanel.moveCursor( -size, 0);
      else
        groupAdjustment = -size;
    }

    if (groupAdjustment != 0)
    {
      viewport.getSelectionGroup().setStartRes(
          viewport.getSelectionGroup().getStartRes() + groupAdjustment);
      viewport.getSelectionGroup().setEndRes(
          viewport.getSelectionGroup().getEndRes() + groupAdjustment);
    }


    boolean appendHistoryItem = false;
    if(viewport.historyList!=null
       && viewport.historyList.size()>0
      && viewport.historyList.peek() instanceof SlideSequencesCommand)
    {
      appendHistoryItem = ssc.appendSlideCommand(
          (SlideSequencesCommand)viewport.historyList.peek())
          ;
    }

    if(!appendHistoryItem)
      addHistoryItem(ssc);

    repaint();
  }

  static StringBuffer copiedSequences;
  static Vector copiedHiddenColumns;
  protected void copy_actionPerformed()
  {
    if (viewport.getSelectionGroup() == null)
    {
      return;
    }

    SequenceGroup sg = viewport.getSelectionGroup();
    copiedSequences = new StringBuffer();
    Hashtable orderedSeqs = new Hashtable();
    for (int i = 0; i < sg.getSize(); i++)
    {
      SequenceI seq = sg.getSequenceAt(i);
      int index = viewport.alignment.findIndex(seq);
      orderedSeqs.put(index + "", seq);
    }

    int index = 0, startRes, endRes;
    char ch;

    if (viewport.hasHiddenColumns && viewport.getSelectionGroup() != null)
    {
      copiedHiddenColumns = new Vector();
      int hiddenOffset = viewport.getSelectionGroup().getStartRes();
      for (int i = 0; i < viewport.getColumnSelection().getHiddenColumns().size();
           i++)
      {
        int[] region = (int[])
            viewport.getColumnSelection().getHiddenColumns().elementAt(i);

        copiedHiddenColumns.addElement(new int[]
                                       {region[0] - hiddenOffset,
                                       region[1] - hiddenOffset});
      }
    }
    else
    {
      copiedHiddenColumns = null;
    }

    for (int i = 0; i < sg.getSize(); i++)
    {
      SequenceI seq = null;

      while (seq == null)
      {
        if (orderedSeqs.containsKey(index + ""))
        {
          seq = (SequenceI) orderedSeqs.get(index + "");
          index++;

          break;
        }
        else
        {
          index++;
        }
      }

      //FIND START RES
      //Returns residue following index if gap
      startRes = seq.findPosition(sg.getStartRes());

      //FIND END RES
      //Need to find the residue preceeding index if gap
      endRes = 0;

      for (int j = 0; j < sg.getEndRes() + 1 && j < seq.getLength(); j++)
      {
        ch = seq.getCharAt(j);
        if (!jalview.util.Comparison.isGap( (ch)))
        {
          endRes++;
        }
      }

      if (endRes > 0)
      {
        endRes += seq.getStart() - 1;
      }

      copiedSequences.append(seq.getName() + "\t" +
                             startRes + "\t" +
                             endRes + "\t" +
                             seq.getSequenceAsString(sg.getStartRes(),
          sg.getEndRes() + 1) + "\n");
    }

  }

  protected void pasteNew_actionPerformed()
  {
    paste(true);
  }

  protected void pasteThis_actionPerformed()
  {
    paste(false);
  }

  void paste(boolean newAlignment)
  {
    try
    {

      if (copiedSequences == null)
      {
        return;
      }

      StringTokenizer st = new StringTokenizer(copiedSequences.toString());
      Vector seqs = new Vector();
      while (st.hasMoreElements())
      {
        String name = st.nextToken();
        int start = Integer.parseInt(st.nextToken());
        int end = Integer.parseInt(st.nextToken());
        seqs.addElement(new Sequence(name, st.nextToken(), start, end));
      }
      SequenceI[] newSeqs = new SequenceI[seqs.size()];
      for (int i = 0; i < seqs.size(); i++)
      {
        newSeqs[i] = (SequenceI) seqs.elementAt(i);
      }

      if (newAlignment)
      {
        String newtitle = new String("Copied sequences");
        if (getTitle().startsWith("Copied sequences"))
        {
          newtitle = getTitle();
        }
        else
        {
          newtitle = newtitle.concat("- from " + getTitle());
        }
        AlignFrame af = new AlignFrame(new Alignment(newSeqs),
                                       viewport.applet,
                                       newtitle,
                                       false);
        if (copiedHiddenColumns != null)
        {
          for (int i = 0; i < copiedHiddenColumns.size(); i++)
          {
            int[] region = (int[]) copiedHiddenColumns.elementAt(i);
            af.viewport.hideColumns(region[0], region[1]);
          }
        }

        jalview.bin.JalviewLite.addFrame(af, newtitle, DEFAULT_WIDTH,
                                         DEFAULT_HEIGHT);
      }
      else
      {
        addSequences(newSeqs);
      }

    }
    catch (Exception ex)
    {} // could be anything being pasted in here

  }

  void addSequences(SequenceI[] seqs)
  {
    for (int i = 0; i < seqs.length; i++)
    {
      viewport.alignment.addSequence(seqs[i]);
    }

    //!newAlignment
    addHistoryItem(new EditCommand(
        "Add sequences",
        EditCommand.PASTE,
        seqs,
        0,
        viewport.alignment.getWidth(),
        viewport.alignment)
        );

    viewport.setEndSeq(viewport.alignment.getHeight());
    viewport.alignment.getWidth();
    viewport.firePropertyChange("alignment", null,
                                viewport.alignment.getSequences());

  }

  protected void cut_actionPerformed()
  {
    copy_actionPerformed();
    delete_actionPerformed();
  }

  protected void delete_actionPerformed()
  {

    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg == null)
    {
      return;
    }

    Vector seqs = new Vector();
    SequenceI seq;
    for (int i = 0; i < sg.getSize(); i++)
    {
      seq = sg.getSequenceAt(i);
      seqs.addElement(seq);
    }

    // If the cut affects all sequences, remove highlighted columns
    if (sg.getSize() == viewport.alignment.getHeight())
    {
      viewport.getColumnSelection().removeElements(sg.getStartRes(),
          sg.getEndRes() + 1);
    }

    SequenceI[] cut = new SequenceI[seqs.size()];
    for (int i = 0; i < seqs.size(); i++)
    {
      cut[i] = (SequenceI) seqs.elementAt(i);
    }

    /*
         //ADD HISTORY ITEM
     */
    addHistoryItem(new EditCommand("Cut Sequences",
                                   EditCommand.CUT,
                                   cut,
                                   sg.getStartRes(),
                                   sg.getEndRes() - sg.getStartRes() + 1,
                                   viewport.alignment));

    viewport.setSelectionGroup(null);
    viewport.alignment.deleteGroup(sg);

    viewport.firePropertyChange("alignment", null,
                                viewport.getAlignment().getSequences());

    if (viewport.getAlignment().getHeight() < 1)
    {
      this.setVisible(false);
    }
  }

  protected void deleteGroups_actionPerformed()
  {
    viewport.alignment.deleteAllGroups();
    viewport.sequenceColours = null;
    viewport.setSelectionGroup(null);

    alignPanel.paintAlignment(true);
  }

  public void selectAllSequenceMenuItem_actionPerformed()
  {
    SequenceGroup sg = new SequenceGroup();
    for (int i = 0; i < viewport.getAlignment().getSequences().size(); i++)
    {
      sg.addSequence(viewport.getAlignment().getSequenceAt(i), false);
    }
    sg.setEndRes(viewport.alignment.getWidth() - 1);
    viewport.setSelectionGroup(sg);
    alignPanel.paintAlignment(true);
    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
  }

  public void deselectAllSequenceMenuItem_actionPerformed()
  {
    if (viewport.cursorMode)
    {
      alignPanel.seqPanel.keyboardNo1 = null;
      alignPanel.seqPanel.keyboardNo2 = null;
    }
    viewport.setSelectionGroup(null);
    viewport.getColumnSelection().clear();
    viewport.setSelectionGroup(null);
    alignPanel.idPanel.idCanvas.searchResults = null;
    alignPanel.seqPanel.seqCanvas.highlightSearchResults(null);
    alignPanel.paintAlignment(true);
    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
  }

  public void invertSequenceMenuItem_actionPerformed()
  {
    SequenceGroup sg = viewport.getSelectionGroup();
    for (int i = 0; i < viewport.getAlignment().getSequences().size(); i++)
    {
      sg.addOrRemove(viewport.getAlignment().getSequenceAt(i), false);
    }

    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
  }

  void trimAlignment(boolean trimLeft)
  {
    ColumnSelection colSel = viewport.getColumnSelection();
    int column;

    if (colSel.size() > 0)
    {
      if (trimLeft)
      {
        column = colSel.getMin();
      }
      else
      {
        column = colSel.getMax();
      }

      SequenceI[] seqs;
      if (viewport.getSelectionGroup() != null)
      {
        seqs = viewport.getSelectionGroup().getSequencesAsArray(viewport.
            hiddenRepSequences);
      }
      else
      {
        seqs = viewport.alignment.getSequencesArray();
      }

      TrimRegionCommand trimRegion;
      if (trimLeft)
      {
        trimRegion = new TrimRegionCommand("Remove Left",
                                           TrimRegionCommand.TRIM_LEFT,
                                           seqs,
                                           column,
                                           viewport.alignment,
                                           viewport.colSel,
                                           viewport.selectionGroup);
        viewport.setStartRes(0);
      }
      else
      {
        trimRegion = new TrimRegionCommand("Remove Right",
                                           TrimRegionCommand.TRIM_RIGHT,
                                           seqs,
                                           column,
                                           viewport.alignment,
                                           viewport.colSel,
                                           viewport.selectionGroup);
      }

      statusBar.setText("Removed " + trimRegion.getSize() + " columns.");

      addHistoryItem(trimRegion);

      Vector groups = viewport.alignment.getGroups();

      for (int i = 0; i < groups.size(); i++)
      {
        SequenceGroup sg = (SequenceGroup) groups.elementAt(i);

        if ( (trimLeft && !sg.adjustForRemoveLeft(column))
            || (!trimLeft && !sg.adjustForRemoveRight(column)))
        {
          viewport.alignment.deleteGroup(sg);
        }
      }

      viewport.firePropertyChange("alignment", null,
                                  viewport.getAlignment().getSequences());
    }
  }

  public void removeGappedColumnMenuItem_actionPerformed()
  {
    int start = 0, end = viewport.alignment.getWidth() - 1;

    SequenceI[] seqs;
    if (viewport.getSelectionGroup() != null)
    {
      seqs = viewport.getSelectionGroup().getSequencesAsArray(viewport.
          hiddenRepSequences);
      start = viewport.getSelectionGroup().getStartRes();
      end = viewport.getSelectionGroup().getEndRes();
    }
    else
    {
      seqs = viewport.alignment.getSequencesArray();
    }

    RemoveGapColCommand removeGapCols =
        new RemoveGapColCommand("Remove Gapped Columns",
                                seqs,
                                start, end,
                                viewport.alignment);

    addHistoryItem(removeGapCols);

    statusBar.setText("Removed " + removeGapCols.getSize() + " empty columns.");

    //This is to maintain viewport position on first residue
    //of first sequence
    SequenceI seq = viewport.alignment.getSequenceAt(0);
    int startRes = seq.findPosition(viewport.startRes);
    // ShiftList shifts;
    // viewport.getAlignment().removeGaps(shifts=new ShiftList());
    // edit.alColumnChanges=shifts.getInverse();
    // if (viewport.hasHiddenColumns)
    //   viewport.getColumnSelection().compensateForEdits(shifts);
    viewport.setStartRes(seq.findIndex(startRes) - 1);
    viewport.firePropertyChange("alignment", null,
                                viewport.getAlignment().getSequences());

  }

  public void removeAllGapsMenuItem_actionPerformed()
  {
    int start = 0, end = viewport.alignment.getWidth() - 1;

    SequenceI[] seqs;
    if (viewport.getSelectionGroup() != null)
    {
      seqs = viewport.getSelectionGroup().getSequencesAsArray(viewport.
          hiddenRepSequences);
      start = viewport.getSelectionGroup().getStartRes();
      end = viewport.getSelectionGroup().getEndRes();
    }
    else
    {
      seqs = viewport.alignment.getSequencesArray();
    }

    //This is to maintain viewport position on first residue
    //of first sequence
    SequenceI seq = viewport.alignment.getSequenceAt(0);
    int startRes = seq.findPosition(viewport.startRes);

    addHistoryItem(new RemoveGapsCommand("Remove Gaps",
                                         seqs,
                                         start, end,
                                         viewport.alignment));

    viewport.setStartRes(seq.findIndex(startRes) - 1);

    viewport.firePropertyChange("alignment", null,
                                viewport.getAlignment().getSequences());

  }

  public void findMenuItem_actionPerformed()
  {
    new Finder(alignPanel);
  }

  public void newView()
  {
    AlignmentI newal;
    if (viewport.hasHiddenRows)
    {
      newal = new Alignment(viewport.getAlignment().
                            getHiddenSequences()
                            .getFullAlignment().
                            getSequencesArray());
    }
    else
    {
      newal = new Alignment(viewport.alignment.getSequencesArray());
    }

    if (viewport.alignment.getAlignmentAnnotation() != null)
    {
      for (int i = 0; i < viewport.alignment.getAlignmentAnnotation().length; i++)
      {
        if (!viewport.alignment.getAlignmentAnnotation()[i].autoCalculated)
        {
          newal.addAnnotation(viewport.alignment.getAlignmentAnnotation()[i]);
        }
      }
    }

    AlignFrame newaf = new AlignFrame(newal,
                                      viewport.applet,
                                      "",
                                      false);

    newaf.viewport.sequenceSetID = alignPanel.av.getSequenceSetId();
    PaintRefresher.Register(alignPanel, alignPanel.av.getSequenceSetId());
    PaintRefresher.Register(newaf.alignPanel,
                            newaf.alignPanel.av.getSequenceSetId());

    PaintRefresher.Register(newaf.alignPanel.idPanel.idCanvas,
                            newaf.alignPanel.av.getSequenceSetId());
    PaintRefresher.Register(newaf.alignPanel.seqPanel.seqCanvas,
                            newaf.alignPanel.av.getSequenceSetId());

    Vector comps = (Vector) PaintRefresher.components.get(viewport.
        getSequenceSetId());
    int viewSize = -1;
    for (int i = 0; i < comps.size(); i++)
    {
      if (comps.elementAt(i) instanceof AlignmentPanel)
      {
        viewSize++;
      }
    }

    String title = new String(this.getTitle());
    if (title.indexOf("(View") > -1)
    {
      title = title.substring(0, title.indexOf("(View"));
    }

    title += "(View " + viewSize + ")";

    newaf.setTitle(title.toString());

    newaf.viewport.historyList = viewport.historyList;
    newaf.viewport.redoList = viewport.redoList;

  }

  public void seqLimits_itemStateChanged()
  {
    viewport.setShowJVSuffix(seqLimits.getState());
    alignPanel.fontChanged();
    alignPanel.paintAlignment(true);
  }

  protected void colourTextMenuItem_actionPerformed()
  {
    viewport.setColourText(colourTextMenuItem.getState());
    alignPanel.paintAlignment(true);
  }

  protected void wrapMenuItem_actionPerformed()
  {
    viewport.setWrapAlignment(wrapMenuItem.getState());
    alignPanel.setWrapAlignment(wrapMenuItem.getState());
    scaleAbove.setEnabled(wrapMenuItem.getState());
    scaleLeft.setEnabled(wrapMenuItem.getState());
    scaleRight.setEnabled(wrapMenuItem.getState());
    alignPanel.paintAlignment(true);
  }

  public void overviewMenuItem_actionPerformed()
  {
    if (alignPanel.overviewPanel != null)
    {
      return;
    }

    Frame frame = new Frame();
    OverviewPanel overview = new OverviewPanel(alignPanel);
    frame.add(overview);
    // +50 must allow for applet frame window
    jalview.bin.JalviewLite.addFrame(frame, "Overview " + this.getTitle(),
                                     overview.getPreferredSize().width,
                                     overview.getPreferredSize().height + 50);

    frame.pack();
    frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        alignPanel.setOverviewPanel(null);
      };
    });

    alignPanel.setOverviewPanel(overview);

  }

  void changeColour(ColourSchemeI cs)
  {
    int threshold = 0;

    if (cs != null)
    {
      if (viewport.getAbovePIDThreshold())
      {
        threshold = SliderPanel.setPIDSliderSource(alignPanel, cs, "Background");

        cs.setThreshold(threshold, viewport.getIgnoreGapsConsensus());

        viewport.setGlobalColourScheme(cs);
      }
      else
      {
        cs.setThreshold(0, viewport.getIgnoreGapsConsensus());
      }

      if (viewport.getConservationSelected())
      {

        Alignment al = (Alignment) viewport.alignment;
        Conservation c = new Conservation("All",
                                          ResidueProperties.propHash, 3,
                                          al.getSequences(), 0,
                                          al.getWidth() - 1);

        c.calculate();
        c.verdict(false, viewport.ConsPercGaps);

        cs.setConservation(c);

        cs.setConservationInc(SliderPanel.setConservationSlider(alignPanel, cs,
            "Background"));

      }
      else
      {
        cs.setConservation(null);
      }

      cs.setConsensus(viewport.hconsensus);

    }
    viewport.setGlobalColourScheme(cs);

    if (viewport.getColourAppliesToAllGroups())
    {
      Vector groups = viewport.alignment.getGroups();
      for (int i = 0; i < groups.size(); i++)
      {
        SequenceGroup sg = (SequenceGroup) groups.elementAt(i);

        if (cs == null)
        {
          sg.cs = null;
          continue;
        }
        if (cs instanceof ClustalxColourScheme)
        {
          sg.cs = new ClustalxColourScheme(
              sg.getSequences(viewport.hiddenRepSequences),
              sg.getWidth());
        }
        else
        {
          try
          {
            sg.cs = (ColourSchemeI) cs.getClass().newInstance();
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
            sg.cs = cs;
          }
        }

        if (viewport.getAbovePIDThreshold()
            || cs instanceof PIDColourScheme
            || cs instanceof Blosum62ColourScheme)
        {
          sg.cs.setThreshold(threshold, viewport.getIgnoreGapsConsensus());
          sg.cs.setConsensus(AAFrequency.calculate(
              sg.getSequences(viewport.hiddenRepSequences), 0, sg.getWidth()));
        }
        else
        {
          sg.cs.setThreshold(0, viewport.getIgnoreGapsConsensus());
        }

        if (viewport.getConservationSelected())
        {
          Conservation c = new Conservation("Group",
                                            ResidueProperties.propHash, 3,
                                            sg.getSequences(viewport.
              hiddenRepSequences), 0,
                                            viewport.alignment.getWidth() - 1);
          c.calculate();
          c.verdict(false, viewport.ConsPercGaps);
          sg.cs.setConservation(c);
        }
        else
        {
          sg.cs.setConservation(null);
          sg.cs.setThreshold(0, viewport.getIgnoreGapsConsensus());
        }

      }
    }

    if (alignPanel.getOverviewPanel() != null)
    {
      alignPanel.getOverviewPanel().updateOverviewImage();
    }

    jalview.structure.StructureSelectionManager.getStructureSelectionManager()
        .sequenceColoursChanged(alignPanel);

    alignPanel.paintAlignment(true);
  }

  protected void modifyPID_actionPerformed()
  {
    if (viewport.getAbovePIDThreshold() && viewport.globalColourScheme != null)
    {
      SliderPanel.setPIDSliderSource(alignPanel, viewport.getGlobalColourScheme(),
                                     "Background");
      SliderPanel.showPIDSlider();
    }
  }

  protected void modifyConservation_actionPerformed()
  {
    if (viewport.getConservationSelected() && viewport.globalColourScheme != null)
    {
      SliderPanel.setConservationSlider(alignPanel, viewport.globalColourScheme,
                                        "Background");
      SliderPanel.showConservationSlider();
    }
  }

  protected void conservationMenuItem_actionPerformed()
  {
    viewport.setConservationSelected(conservationMenuItem.getState());

    viewport.setAbovePIDThreshold(false);
    abovePIDThreshold.setState(false);

    changeColour(viewport.getGlobalColourScheme());

    modifyConservation_actionPerformed();
  }

  public void abovePIDThreshold_actionPerformed()
  {
    viewport.setAbovePIDThreshold(abovePIDThreshold.getState());

    conservationMenuItem.setState(false);
    viewport.setConservationSelected(false);

    changeColour(viewport.getGlobalColourScheme());

    modifyPID_actionPerformed();
  }

  public void sortPairwiseMenuItem_actionPerformed()
  {
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByPID(viewport.getAlignment(),
                              viewport.getAlignment().getSequenceAt(0));
    addHistoryItem(new OrderCommand("Pairwise Sort", oldOrder,
                                    viewport.alignment));
    alignPanel.paintAlignment(true);
  }

  public void sortIDMenuItem_actionPerformed()
  {
    //  addHistoryItem(new HistoryItem("ID Sort", viewport.alignment,
    //                                HistoryItem.SORT));
    AlignmentSorter.sortByID(viewport.getAlignment());
    alignPanel.paintAlignment(true);
  }

  public void sortGroupMenuItem_actionPerformed()
  {
    //  addHistoryItem(new HistoryItem("Group Sort", viewport.alignment,
    //                                 HistoryItem.SORT));
    AlignmentSorter.sortByGroup(viewport.getAlignment());
    alignPanel.paintAlignment(true);

  }

  public void removeRedundancyMenuItem_actionPerformed()
  {
    new RedundancyPanel(alignPanel);
  }

  public void pairwiseAlignmentMenuItem_actionPerformed()
  {
    if (viewport.getSelectionGroup() != null
        && viewport.getSelectionGroup().getSize() > 1)
    {
      Frame frame = new Frame();
      frame.add(new PairwiseAlignPanel(alignPanel));
      jalview.bin.JalviewLite.addFrame(frame, "Pairwise Alignment", 600, 500);
    }
  }

  public void PCAMenuItem_actionPerformed()
  {
    //are the sequences aligned?
    if (!viewport.alignment.isAligned())
    {
      SequenceI current;
      int Width = viewport.getAlignment().getWidth();

      for (int i = 0; i < viewport.getAlignment().getSequences().size();
           i++)
      {
        current = viewport.getAlignment().getSequenceAt(i);

        if (current.getLength() < Width)
        {
          current.insertCharAt(Width - 1, viewport.getGapCharacter());
        }
      }
      alignPanel.paintAlignment(true);
    }

    if ( (viewport.getSelectionGroup() != null &&
          viewport.getSelectionGroup().getSize() < 4 &&
          viewport.getSelectionGroup().getSize() > 0)
        || viewport.getAlignment().getHeight() < 4)
    {
      return;
    }

    try
    {
      new PCAPanel(viewport);
    }
    catch (java.lang.OutOfMemoryError ex)
    {
    }

  }

  public void averageDistanceTreeMenuItem_actionPerformed()
  {
    NewTreePanel("AV", "PID", "Average distance tree using PID");
  }

  public void neighbourTreeMenuItem_actionPerformed()
  {
    NewTreePanel("NJ", "PID", "Neighbour joining tree using PID");
  }

  protected void njTreeBlosumMenuItem_actionPerformed()
  {
    NewTreePanel("NJ", "BL", "Neighbour joining tree using BLOSUM62");
  }

  protected void avTreeBlosumMenuItem_actionPerformed()
  {
    NewTreePanel("AV", "BL", "Average distance tree using BLOSUM62");
  }

  void NewTreePanel(String type, String pwType, String title)
  {
    //are the sequences aligned?
    if (!viewport.alignment.isAligned())
    {
      SequenceI current;
      int Width = viewport.getAlignment().getWidth();

      for (int i = 0; i < viewport.getAlignment().getSequences().size();
           i++)
      {
        current = viewport.getAlignment().getSequenceAt(i);

        if (current.getLength() < Width)
        {
          current.insertCharAt(Width - 1, viewport.getGapCharacter());
        }
      }
      alignPanel.paintAlignment(true);

    }

    if ( (viewport.getSelectionGroup() != null &&
          viewport.getSelectionGroup().getSize() > 1)
        || (viewport.getSelectionGroup() == null
            && viewport.alignment.getHeight() > 1))
    {
      final TreePanel tp = new TreePanel(viewport,
                                         type,
                                         pwType);

      addTreeMenuItem(tp, title);

      jalview.bin.JalviewLite.addFrame(tp, title, 600, 500);
    }
  }

  void loadTree_actionPerformed()
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer(true, this);
    cap.setText("Paste your Newick tree file here.");
    cap.setTreeImport();
    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame, "Paste Newick file ", 400, 300);
  }

  public void loadTree(jalview.io.NewickFile tree, String treeFile)
  {
    TreePanel tp = new TreePanel(viewport,
                                 treeFile,
                                 "From File - ",
                                 tree);
    jalview.bin.JalviewLite.addFrame(tp, treeFile, 600, 500);
    addTreeMenuItem(tp, treeFile);
  }

  void addTreeMenuItem(final TreePanel treePanel, String title)
  {
    final MenuItem item = new MenuItem(title);
    sortByTreeMenu.add(item);
    item.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        //  addHistoryItem(new HistoryItem("Sort", viewport.alignment,
        //                                 HistoryItem.SORT));
        AlignmentSorter.sortByTree(viewport.getAlignment(), treePanel.getTree());
        alignPanel.paintAlignment(true);
      }
    });

    treePanel.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        sortByTreeMenu.remove(item);
      };
    });
  }

  protected void documentation_actionPerformed()
  {
    showURL("http://www.jalview.org/help.html", "HELP");
  }

  protected void about_actionPerformed()
  {

    class AboutPanel
        extends Canvas
    {
      String version;
      public AboutPanel(String version)
      {
        this.version = version;
      }

      public void paint(Graphics g)
      {
        g.setColor(Color.white);
        g.fillRect(0, 0, getSize().width, getSize().height);
        g.setFont(new Font("Helvetica", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        int fh = fm.getHeight();
        int y = 5, x = 7;
        g.setColor(Color.black);
        g.setFont(new Font("Helvetica", Font.BOLD, 14));
        g.drawString("Jalview - Release " + version, 200, y += fh);
        g.setFont(new Font("Helvetica", Font.PLAIN, 12));
        g.drawString("Authors:  Michele Clamp, James Cuff, Steve Searle, Andrew Waterhouse, Jim Procter & Geoff Barton.",
                     x, y += fh * 2);
        g.drawString("Current development managed by Andrew Waterhouse; Barton Group, University of Dundee.",
                     x, y += fh);
        g.drawString(
            "For any issues relating to Jalview, email help@jalview.org", x,
            y += fh);
        g.drawString("If  you use JalView, please cite:", x, y += fh + 8);
        g.drawString("\"Clamp, M., Cuff, J., Searle, S. M. and Barton, G. J. (2004), The Jalview Java Alignment Editor\"",
                     x, y += fh);
        g.drawString("Bioinformatics,  2004 20;426-7.", x, y += fh);
      }
    }

    String version = "test";
    java.net.URL url = getClass().getResource("/.build_properties");
    if (url != null)
    {
      try
      {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            url.openStream()));
        String line;
        while ( (line = reader.readLine()) != null)
        {
          if (line.indexOf("VERSION") > -1)
          {
            version = line.substring(line.indexOf("=") + 1);
          }
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }

    Frame frame = new Frame();
    frame.add(new AboutPanel(version));
    jalview.bin.JalviewLite.addFrame(frame, "Jalview", 580, 200);

  }

  public void showURL(String url, String target)
  {
    if (viewport.applet == null)
    {
      System.out.println("Not running as applet - no browser available.");
    }
    else
    {
      try
      {
        System.out.println("Show url: " + url);
        viewport.applet.getAppletContext().showDocument(new java.net.URL(url),
            target);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////////
  //JBuilder Graphics here

   MenuBar alignFrameMenuBar = new MenuBar();
   Menu fileMenu = new Menu("File");
   MenuItem loadApplication = new MenuItem("View in Full Application");
   MenuItem loadTree = new MenuItem("Load Associated Tree ...");
   MenuItem loadAnnotations = new MenuItem(
      "Load Features/Annotations ...");
   MenuItem outputFeatures = new MenuItem("Export Features ...");
   MenuItem outputAnnotations = new MenuItem("Export Annotations ...");

   MenuItem closeMenuItem = new MenuItem("Close");
   Menu editMenu = new Menu("Edit");
   Menu viewMenu = new Menu("View");
   Menu colourMenu = new Menu("Colour");
   Menu calculateMenu = new Menu("Calculate");
   MenuItem selectAllSequenceMenuItem = new MenuItem("Select all");
   MenuItem deselectAllSequenceMenuItem = new MenuItem("Deselect All");
   MenuItem invertSequenceMenuItem = new MenuItem("Invert Selection");
   MenuItem remove2LeftMenuItem = new MenuItem();
   MenuItem remove2RightMenuItem = new MenuItem();
   MenuItem removeGappedColumnMenuItem = new MenuItem();
   MenuItem removeAllGapsMenuItem = new MenuItem();
   CheckboxMenuItem viewBoxesMenuItem = new CheckboxMenuItem();
   CheckboxMenuItem viewTextMenuItem = new CheckboxMenuItem();
   MenuItem sortPairwiseMenuItem = new MenuItem();
   MenuItem sortIDMenuItem = new MenuItem();
   MenuItem sortGroupMenuItem = new MenuItem();
   MenuItem removeRedundancyMenuItem = new MenuItem();
   MenuItem pairwiseAlignmentMenuItem = new MenuItem();
   MenuItem PCAMenuItem = new MenuItem();
   MenuItem averageDistanceTreeMenuItem = new MenuItem();
   MenuItem neighbourTreeMenuItem = new MenuItem();
  BorderLayout borderLayout1 = new BorderLayout();
  public Label statusBar = new Label();
   Menu outputTextboxMenu = new Menu();
   MenuItem clustalColour = new MenuItem();
   MenuItem zappoColour = new MenuItem();
   MenuItem taylorColour = new MenuItem();
   MenuItem hydrophobicityColour = new MenuItem();
   MenuItem helixColour = new MenuItem();
   MenuItem strandColour = new MenuItem();
   MenuItem turnColour = new MenuItem();
   MenuItem buriedColour = new MenuItem();
   MenuItem userDefinedColour = new MenuItem();
   MenuItem PIDColour = new MenuItem();
   MenuItem BLOSUM62Colour = new MenuItem();
  MenuItem njTreeBlosumMenuItem = new MenuItem();
  MenuItem avDistanceTreeBlosumMenuItem = new MenuItem();
   CheckboxMenuItem annotationPanelMenuItem = new CheckboxMenuItem();
   CheckboxMenuItem colourTextMenuItem = new CheckboxMenuItem();
   MenuItem alProperties = new MenuItem("Alignment Properties...");
  MenuItem overviewMenuItem = new MenuItem();
   MenuItem undoMenuItem = new MenuItem();
   MenuItem redoMenuItem = new MenuItem();
   CheckboxMenuItem conservationMenuItem = new CheckboxMenuItem();
  MenuItem noColourmenuItem = new MenuItem();
   CheckboxMenuItem wrapMenuItem = new CheckboxMenuItem();
   CheckboxMenuItem renderGapsMenuItem = new CheckboxMenuItem();
  MenuItem findMenuItem = new MenuItem();
   CheckboxMenuItem abovePIDThreshold = new CheckboxMenuItem();
   MenuItem nucleotideColour = new MenuItem();
  MenuItem deleteGroups = new MenuItem();
  MenuItem delete = new MenuItem();
  MenuItem copy = new MenuItem();
  MenuItem cut = new MenuItem();
  Menu pasteMenu = new Menu();
  MenuItem pasteNew = new MenuItem();
  MenuItem pasteThis = new MenuItem();
   CheckboxMenuItem applyToAllGroups = new CheckboxMenuItem();
   MenuItem font = new MenuItem();
   CheckboxMenuItem scaleAbove = new CheckboxMenuItem();
   CheckboxMenuItem scaleLeft = new CheckboxMenuItem();
   CheckboxMenuItem scaleRight = new CheckboxMenuItem();
  MenuItem modifyPID = new MenuItem();
  MenuItem modifyConservation = new MenuItem();
   CheckboxMenuItem autoCalculate
      = new CheckboxMenuItem("Autocalculate Consensus", true);
   Menu sortByTreeMenu = new Menu();
  Menu sort = new Menu();
  Menu calculate = new Menu();
  MenuItem inputText = new MenuItem();
  Menu helpMenu = new Menu();
  MenuItem documentation = new MenuItem();
  MenuItem about = new MenuItem();
   CheckboxMenuItem seqLimits = new CheckboxMenuItem();
  Panel embeddedMenu;
  Label embeddedEdit;
  Label embeddedSelect;
  Label embeddedView;
  Label embeddedFormat;
  Label embeddedColour;
  Label embeddedFile;
  Label embeddedHelp;
  Label embeddedCalculate;
  FlowLayout flowLayout1;

  private void jbInit()
      throws Exception
  {

    setMenuBar(alignFrameMenuBar);

    MenuItem item;

    // dynamically fill save as menu with available formats
    for (int i = 0; i < jalview.io.AppletFormatAdapter.WRITEABLE_FORMATS.length;
         i++)
    {

      item = new MenuItem(jalview.io.AppletFormatAdapter.WRITEABLE_FORMATS[i]);

      item.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          outputText_actionPerformed(e);
        }
      });

      outputTextboxMenu.add(item);
    }
    closeMenuItem.addActionListener(this);
    loadApplication.addActionListener(this);

    loadTree.addActionListener(this);
    loadAnnotations.addActionListener(this);
    outputFeatures.addActionListener(this);
    outputAnnotations.addActionListener(this);
    selectAllSequenceMenuItem.addActionListener(this);
    deselectAllSequenceMenuItem.addActionListener(this);
    invertSequenceMenuItem.addActionListener(this);
    remove2LeftMenuItem.setLabel("Remove Left");
    remove2LeftMenuItem.addActionListener(this);
    remove2RightMenuItem.setLabel("Remove Right");
    remove2RightMenuItem.addActionListener(this);
    removeGappedColumnMenuItem.setLabel("Remove Empty Columns");
    removeGappedColumnMenuItem.addActionListener(this);
    removeAllGapsMenuItem.setLabel("Remove All Gaps");
    removeAllGapsMenuItem.addActionListener(this);
    viewBoxesMenuItem.setLabel("Boxes");
    viewBoxesMenuItem.setState(true);
    viewBoxesMenuItem.addItemListener(this);
    viewTextMenuItem.setLabel("Text");
    viewTextMenuItem.setState(true);
    viewTextMenuItem.addItemListener(this);
    sortPairwiseMenuItem.setLabel("by Pairwise Identity");
    sortPairwiseMenuItem.addActionListener(this);
    sortIDMenuItem.setLabel("by ID");
    sortIDMenuItem.addActionListener(this);
    sortGroupMenuItem.setLabel("by Group");
    sortGroupMenuItem.addActionListener(this);
    removeRedundancyMenuItem.setLabel("Remove Redundancy...");
    removeRedundancyMenuItem.addActionListener(this);
    pairwiseAlignmentMenuItem.setLabel("Pairwise Alignments...");
    pairwiseAlignmentMenuItem.addActionListener(this);
    PCAMenuItem.setLabel("Principal Component Analysis");
    PCAMenuItem.addActionListener(this);
    averageDistanceTreeMenuItem.setLabel(
        "Average Distance Using % Identity");
    averageDistanceTreeMenuItem.addActionListener(this);
    neighbourTreeMenuItem.setLabel("Neighbour Joining Using % Identity");
    neighbourTreeMenuItem.addActionListener(this);
    statusBar.setBackground(Color.white);
    statusBar.setFont(new java.awt.Font("Verdana", 0, 11));
    statusBar.setText("Status bar");
    outputTextboxMenu.setLabel("Output to Textbox");
    clustalColour.setLabel("Clustalx");

    clustalColour.addActionListener(this);
    zappoColour.setLabel("Zappo");
    zappoColour.addActionListener(this);
    taylorColour.setLabel("Taylor");
    taylorColour.addActionListener(this);
    hydrophobicityColour.setLabel("Hydrophobicity");
    hydrophobicityColour.addActionListener(this);
    helixColour.setLabel("Helix Propensity");
    helixColour.addActionListener(this);
    strandColour.setLabel("Strand Propensity");
    strandColour.addActionListener(this);
    turnColour.setLabel("Turn Propensity");
    turnColour.addActionListener(this);
    buriedColour.setLabel("Buried Index");
    buriedColour.addActionListener(this);
    userDefinedColour.setLabel("User Defined...");
    userDefinedColour.addActionListener(this);
    PIDColour.setLabel("Percentage Identity");
    PIDColour.addActionListener(this);
    BLOSUM62Colour.setLabel("BLOSUM62 Score");
    BLOSUM62Colour.addActionListener(this);
    avDistanceTreeBlosumMenuItem.setLabel(
        "Average Distance Using BLOSUM62");
    avDistanceTreeBlosumMenuItem.addActionListener(this);
    njTreeBlosumMenuItem.setLabel("Neighbour Joining Using BLOSUM62");
    njTreeBlosumMenuItem.addActionListener(this);
    annotationPanelMenuItem.setLabel("Show Annotations");
    annotationPanelMenuItem.addItemListener(this);
    colourTextMenuItem.setLabel("Colour Text");
    colourTextMenuItem.addItemListener(this);
    alProperties.addActionListener(this);
    overviewMenuItem.setLabel("Overview Window");
    overviewMenuItem.addActionListener(this);
    undoMenuItem.setEnabled(false);
    undoMenuItem.setLabel("Undo");
    undoMenuItem.addActionListener(this);
    redoMenuItem.setEnabled(false);
    redoMenuItem.setLabel("Redo");
    redoMenuItem.addActionListener(this);
    conservationMenuItem.setLabel("by Conservation");
    conservationMenuItem.addItemListener(this);
    noColourmenuItem.setLabel("None");
    noColourmenuItem.addActionListener(this);
    wrapMenuItem.setLabel("Wrap");
    wrapMenuItem.addItemListener(this);
    renderGapsMenuItem.setLabel("Show Gaps");
    renderGapsMenuItem.setState(true);
    renderGapsMenuItem.addItemListener(this);
    findMenuItem.setLabel("Find...");
    findMenuItem.addActionListener(this);
    abovePIDThreshold.setLabel("Above Identity Threshold");
    abovePIDThreshold.addItemListener(this);
    nucleotideColour.setLabel("Nucleotide");
    nucleotideColour.addActionListener(this);
    deleteGroups.setLabel("Undefine Groups");
    deleteGroups.addActionListener(this);
    copy.setLabel("Copy");
    copy.addActionListener(this);
    cut.setLabel("Cut");
    cut.addActionListener(this);
    delete.setLabel("Delete");
    delete.addActionListener(this);
    pasteMenu.setLabel("Paste");
    pasteNew.setLabel("To New Alignment");
    pasteNew.addActionListener(this);
    pasteThis.setLabel("Add To This Alignment");
    pasteThis.addActionListener(this);
    applyToAllGroups.setLabel("Apply Colour To All Groups");
    applyToAllGroups.setState(true);
    applyToAllGroups.addItemListener(this);
    font.setLabel("Font...");
    font.addActionListener(this);
    scaleAbove.setLabel("Scale Above");
    scaleAbove.setState(true);
    scaleAbove.setEnabled(false);
    scaleAbove.addItemListener(this);
    scaleLeft.setEnabled(false);
    scaleLeft.setState(true);
    scaleLeft.setLabel("Scale Left");
    scaleLeft.addItemListener(this);
    scaleRight.setEnabled(false);
    scaleRight.setState(true);
    scaleRight.setLabel("Scale Right");
    scaleRight.addItemListener(this);
    modifyPID.setLabel("Modify Identity Threshold...");
    modifyPID.addActionListener(this);
    modifyConservation.setLabel("Modify Conservation Threshold...");
    modifyConservation.addActionListener(this);
    sortByTreeMenu.setLabel("By Tree Order");
    sort.setLabel("Sort");
    calculate.setLabel("Calculate Tree");
    autoCalculate.addItemListener(this);
    inputText.setLabel("Input from textbox");
    inputText.addActionListener(this);

    helpMenu.setLabel("Help");
    documentation.setLabel("Documentation");
    documentation.addActionListener(this);

    about.setLabel("About...");
    about.addActionListener(this);
    seqLimits.setState(true);
    seqLimits.setLabel("Show Sequence Limits");
    seqLimits.addItemListener(this);
    featureSettings.setLabel("Feature Settings...");
    featureSettings.addActionListener(this);
    sequenceFeatures.setLabel("Sequence Features");
    sequenceFeatures.addItemListener(this);
    sequenceFeatures.setState(false);
    annotationColour.setLabel("by Annotation...");
    annotationColour.addActionListener(this);
    invertSequenceMenuItem.setLabel("Invert Sequence Selection");
    invertColSel.setLabel("Invert Column Selection");
    menu1.setLabel("Show");
    showColumns.setLabel("All Columns ");
    showSeqs.setLabel("All Sequences");
    menu2.setLabel("Hide");
    hideColumns.setLabel("Selected Columns");
    hideSequences.setLabel("Selected Sequences");
    invertColSel.addActionListener(this);
    showColumns.addActionListener(this);
    showSeqs.addActionListener(this);
    hideColumns.addActionListener(this);
    hideSequences.addActionListener(this);
    formatMenu.setLabel("Format");
    selectMenu.setLabel("Select");
    newView.setLabel("New View");
    newView.addActionListener(this);
    alignFrameMenuBar.add(fileMenu);
    alignFrameMenuBar.add(editMenu);
    alignFrameMenuBar.add(selectMenu);
    alignFrameMenuBar.add(viewMenu);
    alignFrameMenuBar.add(formatMenu);
    alignFrameMenuBar.add(colourMenu);
    alignFrameMenuBar.add(calculateMenu);
    alignFrameMenuBar.add(helpMenu);

    fileMenu.add(inputText);
    fileMenu.add(loadTree);
    fileMenu.add(loadAnnotations);

    fileMenu.addSeparator();
    fileMenu.add(outputTextboxMenu);
    fileMenu.add(outputFeatures);
    fileMenu.add(outputAnnotations);

    if (jalviewServletURL != null)
    {
      fileMenu.add(loadApplication);
    }

    fileMenu.addSeparator();
    fileMenu.add(closeMenuItem);

    editMenu.add(undoMenuItem);
    editMenu.add(redoMenuItem);
    editMenu.add(cut);
    editMenu.add(copy);
    editMenu.add(pasteMenu);
    editMenu.add(delete);
    editMenu.addSeparator();
    editMenu.add(remove2LeftMenuItem);
    editMenu.add(remove2RightMenuItem);
    editMenu.add(removeGappedColumnMenuItem);
    editMenu.add(removeAllGapsMenuItem);
    editMenu.add(removeRedundancyMenuItem);
    viewMenu.add(newView);
    viewMenu.addSeparator();
    viewMenu.add(menu1);
    viewMenu.add(menu2);
    viewMenu.addSeparator();
    viewMenu.add(annotationPanelMenuItem);
    viewMenu.addSeparator();
    viewMenu.add(sequenceFeatures);
    viewMenu.add(featureSettings);
    viewMenu.addSeparator();
    viewMenu.add(alProperties);
    viewMenu.addSeparator();
    viewMenu.add(overviewMenuItem);
    colourMenu.add(applyToAllGroups);
    colourMenu.addSeparator();
    colourMenu.add(noColourmenuItem);
    colourMenu.add(clustalColour);
    colourMenu.add(BLOSUM62Colour);
    colourMenu.add(PIDColour);
    colourMenu.add(zappoColour);
    colourMenu.add(taylorColour);
    colourMenu.add(hydrophobicityColour);
    colourMenu.add(helixColour);
    colourMenu.add(strandColour);
    colourMenu.add(turnColour);
    colourMenu.add(buriedColour);
    colourMenu.add(nucleotideColour);
    colourMenu.add(userDefinedColour);
    colourMenu.addSeparator();
    colourMenu.add(conservationMenuItem);
    colourMenu.add(modifyConservation);
    colourMenu.add(abovePIDThreshold);
    colourMenu.add(modifyPID);
    colourMenu.add(annotationColour);
    calculateMenu.add(sort);
    calculateMenu.add(calculate);
    calculateMenu.addSeparator();
    calculateMenu.add(pairwiseAlignmentMenuItem);
    calculateMenu.add(PCAMenuItem);
    calculateMenu.add(autoCalculate);
    this.add(statusBar, BorderLayout.SOUTH);
    pasteMenu.add(pasteNew);
    pasteMenu.add(pasteThis);
    sort.add(sortIDMenuItem);
    sort.add(sortByTreeMenu);
    sort.add(sortGroupMenuItem);
    sort.add(sortPairwiseMenuItem);
    calculate.add(averageDistanceTreeMenuItem);
    calculate.add(neighbourTreeMenuItem);
    calculate.add(avDistanceTreeBlosumMenuItem);
    calculate.add(njTreeBlosumMenuItem);
    helpMenu.add(documentation);
    helpMenu.add(about);
    menu1.add(showColumns);
    menu1.add(showSeqs);
    menu2.add(hideColumns);
    menu2.add(hideSequences);
    formatMenu.add(font);
    formatMenu.add(seqLimits);
    formatMenu.add(wrapMenuItem);
    formatMenu.add(scaleAbove);
    formatMenu.add(scaleLeft);
    formatMenu.add(scaleRight);
    formatMenu.add(viewBoxesMenuItem);
    formatMenu.add(viewTextMenuItem);
    formatMenu.add(colourTextMenuItem);
    formatMenu.add(renderGapsMenuItem);
    selectMenu.add(findMenuItem);
    selectMenu.addSeparator();
    selectMenu.add(selectAllSequenceMenuItem);
    selectMenu.add(deselectAllSequenceMenuItem);
    selectMenu.add(invertSequenceMenuItem);
    selectMenu.add(invertColSel);
    selectMenu.add(deleteGroups);
  }

  public void setEmbedded()
  {

    embeddedMenu = new Panel();
    embeddedEdit = new Label("Edit");
    embeddedSelect = new Label("Select");
    embeddedView = new Label("View");
    embeddedFormat = new Label("Format");
    embeddedColour = new Label("Colour");
    embeddedFile = new Label("File");
    embeddedHelp = new Label("Help");
    embeddedCalculate = new Label("Calculate");
    flowLayout1 = new FlowLayout();
    embeddedMenu.setBackground(Color.lightGray);
    embeddedMenu.setLayout(flowLayout1);
    embeddedEdit.setFont(new java.awt.Font("Arial", Font.PLAIN, 10));
    embeddedSelect.setFont(new java.awt.Font("Arial", Font.PLAIN, 10));
    embeddedView.setFont(new java.awt.Font("Arial", Font.PLAIN, 10));
    embeddedFormat.setFont(new java.awt.Font("Arial", Font.PLAIN, 10));
    embeddedColour.setFont(new java.awt.Font("Arial", Font.PLAIN, 10));
    embeddedFile.setFont(new java.awt.Font("Arial", Font.PLAIN, 10));
    embeddedHelp.setFont(new java.awt.Font("Arial", Font.PLAIN, 10));
    embeddedCalculate.setFont(new java.awt.Font("Arial", Font.PLAIN, 10));
    embeddedMenu.add(embeddedFile);
    embeddedMenu.add(embeddedEdit);
    embeddedMenu.add(embeddedSelect);
    embeddedMenu.add(embeddedView);
    embeddedMenu.add(embeddedFormat);
    embeddedMenu.add(embeddedColour);
    embeddedMenu.add(embeddedCalculate);
    embeddedMenu.add(embeddedHelp);
    flowLayout1.setAlignment(FlowLayout.LEFT);
    flowLayout1.setHgap(2);
    flowLayout1.setVgap(0);
    embeddedFile.addMouseListener(this);
    embeddedEdit.addMouseListener(this);
    embeddedSelect.addMouseListener(this);
    embeddedView.addMouseListener(this);
    embeddedFormat.addMouseListener(this);
    embeddedColour.addMouseListener(this);
    embeddedCalculate.addMouseListener(this);
    embeddedHelp.addMouseListener(this);

    fileMenu.remove(closeMenuItem);
    fileMenu.remove(3); // Seperator

    viewport.applet.setLayout(new BorderLayout());
    viewport.applet.add(embeddedMenu, BorderLayout.NORTH);
    viewport.applet.add(statusBar, BorderLayout.SOUTH);

    alignPanel.setSize(viewport.applet.getSize().width,
                       viewport.applet.getSize().height
                       - embeddedMenu.HEIGHT - statusBar.HEIGHT);

    viewport.applet.add(alignPanel, BorderLayout.CENTER);
    viewport.applet.validate();

  }

  PopupMenu filePopup, editPopup, searchPopup,
  viewPopup, formatPopup, colourPopup, calculatePopup, helpPopup;
  MenuItem featureSettings = new MenuItem();
  CheckboxMenuItem sequenceFeatures = new CheckboxMenuItem();
  MenuItem annotationColour = new MenuItem();
  MenuItem invertColSel = new MenuItem();
  Menu menu1 = new Menu();
  MenuItem showColumns = new MenuItem();
  MenuItem showSeqs = new MenuItem();
  Menu menu2 = new Menu();
  MenuItem hideColumns = new MenuItem();
  MenuItem hideSequences = new MenuItem();
  Menu formatMenu = new Menu();
  Menu selectMenu = new Menu();
  MenuItem newView = new MenuItem();

  public void mousePressed(MouseEvent evt)
  {
    PopupMenu popup = null;
    Label source = (Label) evt.getSource();
    if (source == embeddedFile)
    {
      popup = filePopup = genPopupMenu(filePopup, fileMenu);
    }
    else if (source == embeddedEdit)
    {
      popup = editPopup = genPopupMenu(editPopup, editMenu);
    }
    else if (source == embeddedSelect)
    {
      popup = searchPopup = genPopupMenu(searchPopup, selectMenu);
    }
    else if (source == embeddedView)
    {
      popup = viewPopup = genPopupMenu(viewPopup, viewMenu);
    }
    else if (source == embeddedFormat)
    {
      popup = formatPopup = genPopupMenu(formatPopup, formatMenu);
    }
    else if (source == embeddedColour)
    {
      popup = colourPopup = genPopupMenu(colourPopup, colourMenu);
    }
    else if (source == embeddedCalculate)
    {
      popup = calculatePopup = genPopupMenu(calculatePopup, calculateMenu);
    }
    else if (source == embeddedHelp)
    {
      popup = helpPopup = genPopupMenu(helpPopup, helpMenu);
    }

    embeddedMenu.add(popup);
    popup.show(embeddedMenu,
               source.getBounds().x,
               source.getBounds().y + source.getBounds().getSize().height);
  }

  PopupMenu genPopupMenu(PopupMenu popup, Menu original)
  {
    if (popup != null)
    {
      return popup;
    }
    popup = new PopupMenu();
    int m, mSize = original.getItemCount();
    for (m = 0; m < mSize; m++)
    {
      popup.add(original.getItem(m));
      mSize--;
      m--;
    }

    return popup;
  }

  public void mouseClicked(MouseEvent evt)
  {}

  public void mouseReleased(MouseEvent evt)
  {}

  public void mouseEntered(MouseEvent evt)
  {}

  public void mouseExited(MouseEvent evt)
  {}

}
