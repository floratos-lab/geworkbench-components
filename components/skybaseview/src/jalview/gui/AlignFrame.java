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
 * along with this program; if not, write to the Free Softwarechang
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package jalview.gui;

import java.beans.*;
import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.MenuEvent;

import jalview.analysis.*;
import jalview.commands.*;
import jalview.datamodel.*;
import jalview.io.*;
import jalview.jbgui.*;
import jalview.schemes.*;
import jalview.ws.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class AlignFrame
    extends GAlignFrame implements DropTargetListener
{
  /** DOCUMENT ME!! */
  public static final int DEFAULT_WIDTH = 700;

  /** DOCUMENT ME!! */
  public static final int DEFAULT_HEIGHT = 500;
  public AlignmentPanel alignPanel;

  AlignViewport viewport;

  Vector alignPanels = new Vector();


  /** DOCUMENT ME!! */
  String currentFileFormat = null;

  String fileName = null;


  /**
   * Creates a new AlignFrame object.
   *
   * @param al DOCUMENT ME!
   */
  public AlignFrame(AlignmentI al, int width, int height)
  {
    this(al, null, width, height);
  }


  /**
   * new alignment window with hidden columns
   * @param al AlignmentI
   * @param hiddenColumns ColumnSelection or null
   */
  public AlignFrame(AlignmentI al, ColumnSelection hiddenColumns,
                    int width, int height)
  {
    this.setSize(width, height);
    viewport = new AlignViewport(al, hiddenColumns);

    alignPanel = new AlignmentPanel(this, viewport);

    if(al.getDataset()==null)
    {
      al.setDataset(null);
    }

    addAlignmentPanel(alignPanel, true);
    init();
  }

  /**
   * Make a new AlignFrame from exisiting alignmentPanels
   * @param ap AlignmentPanel
   * @param av AlignViewport
   */
  public AlignFrame(AlignmentPanel ap)
  {
    viewport = ap.av;
    alignPanel = ap;
    addAlignmentPanel(ap, false);
    init();
  }

  void init()
  {
    if (viewport.conservation == null)
    {
      BLOSUM62Colour.setEnabled(false);
      conservationMenuItem.setEnabled(false);
      modifyConservation.setEnabled(false);
    //  PIDColour.setEnabled(false);
    //  abovePIDThreshold.setEnabled(false);
    //  modifyPID.setEnabled(false);
    }

    String sortby = jalview.bin.Cache.getDefault("SORT_ALIGNMENT", "No sort");

    if (sortby.equals("Id"))
    {
      sortIDMenuItem_actionPerformed(null);
    }
    else if (sortby.equals("Pairwise Identity"))
    {
      sortPairwiseMenuItem_actionPerformed(null);
    }

    if (Desktop.desktop != null)
   {
     this.setDropTarget(new java.awt.dnd.DropTarget(this, this));
     addServiceListeners();
     setGUINucleotide(viewport.alignment.isNucleotide());
   }

   setMenusFromViewport(viewport);
   buildSortByAnnotationScoresMenu();
   if (viewport.wrapAlignment)
   {
     wrapMenuItem_actionPerformed(null);
   }

    if (jalview.bin.Cache.getDefault("SHOW_OVERVIEW",false))
    {
      this.overviewMenuItem_actionPerformed(null);
    }

   addKeyListener();

  }

  public void setFileName(String file, String format)
  {
     fileName = file;
     currentFileFormat = format;
     reload.setEnabled(true);
  }

  void addKeyListener()
  {
      addKeyListener(new KeyAdapter()
      {
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
              deselectAllSequenceMenuItem_actionPerformed(null);

              break;

            case KeyEvent.VK_DOWN:
             if (evt.isAltDown() || !viewport.cursorMode)
              moveSelectedSequences(false);
              if (viewport.cursorMode)
                alignPanel.seqPanel.moveCursor(0, 1);
              break;

            case KeyEvent.VK_UP:
            if (evt.isAltDown() || !viewport.cursorMode)
              moveSelectedSequences(true);
              if (viewport.cursorMode)
                alignPanel.seqPanel.moveCursor(0, -1);

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
                alignPanel.seqPanel.moveCursor(1, 0);
              break;

            case KeyEvent.VK_SPACE:
              if (viewport.cursorMode)
              {
                alignPanel.seqPanel.insertGapAtCursor(evt.isControlDown()
                                           || evt.isShiftDown()
                                           || evt.isAltDown());
              }
              break;

            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
              if (!viewport.cursorMode)
              {
                cut_actionPerformed(null);
              }
              else
            {
                alignPanel.seqPanel.deleteGapAtCursor(evt.isControlDown()
                                           || evt.isShiftDown()
                                           || evt.isAltDown());
            }

              break;

            case KeyEvent.VK_S:
              if (viewport.cursorMode)
              {
                alignPanel.seqPanel.setCursorRow();
              }
              break;
            case KeyEvent.VK_C:
              if (viewport.cursorMode && !evt.isControlDown())
              {
                alignPanel.seqPanel.setCursorColumn();
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
              alignPanel.seqPanel.seqCanvas.repaint();
              break;

            case KeyEvent.VK_F1:
              try
              {
                ClassLoader cl = jalview.gui.Desktop.class.getClassLoader();
                java.net.URL url = javax.help.HelpSet.findHelpSet(cl, "help/help");
                javax.help.HelpSet hs = new javax.help.HelpSet(cl, url);

                javax.help.HelpBroker hb = hs.createHelpBroker();
                hb.setCurrentID("home");
                hb.setDisplayed(true);
              }
              catch (Exception ex)
              {
                ex.printStackTrace();
              }
              break
                  ;
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
                  hideSelSequences_actionPerformed(null);
                  hide = true;
                }
              else if (! (toggleCols &&
                          viewport.colSel.getSelected().size() > 0))
              {
                  showAllSeqs_actionPerformed(null);
              }
            }

              if (toggleCols)
              {
                if (viewport.colSel.getSelected().size() > 0)
                {
                  hideSelColumns_actionPerformed(null);
                  if (!toggleSeqs)
                {
                    viewport.selectionGroup = sg;
                }
              }
                else if (!hide)
              {
                  showAllColumns_actionPerformed(null);
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
          }
        }

      public void keyReleased(KeyEvent evt)
      {
        switch(evt.getKeyCode())
        {
          case KeyEvent.VK_LEFT:
            if (evt.isAltDown() || !viewport.cursorMode)
              viewport.firePropertyChange("alignment", null,
                                          viewport.getAlignment().getSequences());
            break;

          case KeyEvent.VK_RIGHT:
            if (evt.isAltDown() || !viewport.cursorMode)
              viewport.firePropertyChange("alignment", null,
                                          viewport.getAlignment().getSequences());
            break;
        }
      }
      });
  }


  public void addAlignmentPanel(final AlignmentPanel ap,
                                boolean newPanel)
  {
    ap.alignFrame = this;

    alignPanels.addElement(ap);

    PaintRefresher.Register(ap, ap.av.getSequenceSetId());

    int aSize = alignPanels.size();

    tabbedPane.setVisible(aSize>1 || ap.av.viewName!=null);

    if (aSize == 1 && ap.av.viewName==null)
    {
      this.getContentPane().add(ap, BorderLayout.CENTER);
    }
    else
    {
      if (aSize == 2)
      {
        setInitialTabVisible();
      }

      expandViews.setEnabled(true);
      gatherViews.setEnabled(true);
      tabbedPane.addTab(ap.av.viewName, ap);

      ap.setVisible(false);
    }

    if(newPanel)
    {
      if (ap.av.padGaps)
      {
        ap.av.alignment.padGaps();
      }
      ap.av.updateConservation(ap);
      ap.av.updateConsensus(ap);
    }
  }

  public void setInitialTabVisible()
  {
    expandViews.setEnabled(true);
    gatherViews.setEnabled(true);
    tabbedPane.setVisible(true);
    AlignmentPanel first = (AlignmentPanel) alignPanels.firstElement();
    tabbedPane.addTab(first.av.viewName,first);
    this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
  }


  public AlignViewport getViewport()
  {
    return viewport;
  }

  /* Set up intrinsic listeners for dynamically generated GUI bits. */
  private void addServiceListeners()
  {
    final java.beans.PropertyChangeListener thisListener;
    // Do this once to get current state
    BuildWebServiceMenu();
    Desktop.discoverer.addPropertyChangeListener(
        thisListener = new java.beans.PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent evt)
      {
        // System.out.println("Discoverer property change.");
        if (evt.getPropertyName().equals("services"))
        {
          // System.out.println("Rebuilding web service menu");
          BuildWebServiceMenu();
        }
      }
    });

    addInternalFrameListener(new javax.swing.event.
                             InternalFrameAdapter()
    {
      public void internalFrameClosed(
          javax.swing.event.InternalFrameEvent evt)
      {
        // System.out.println("deregistering discoverer listener");
        Desktop.discoverer.removePropertyChangeListener(thisListener);
        closeMenuItem_actionPerformed(true);
      }
      ;
    });
  }

  public void setGUINucleotide(boolean nucleotide)
  {
    showTranslation.setVisible( nucleotide );
    conservationMenuItem.setEnabled( !nucleotide );
    modifyConservation.setEnabled(   !nucleotide );

    //Remember AlignFrame always starts as protein
    if(!nucleotide)
    {
      calculateMenu.remove(calculateMenu.getItemCount()-2);
    }
  }

  /**
   * Need to call this method when tabs are selected for multiple views,
   * or when loading from Jalview2XML.java
   * @param av AlignViewport
   */
  void setMenusFromViewport(AlignViewport av)
  {
    padGapsMenuitem.setSelected(av.padGaps);
    colourTextMenuItem.setSelected(av.showColourText);
    abovePIDThreshold.setSelected(av.getAbovePIDThreshold());
    conservationMenuItem.setSelected(av.getConservationSelected());
    seqLimits.setSelected(av.getShowJVSuffix());
    idRightAlign.setSelected(av.rightAlignIds);
    renderGapsMenuItem.setSelected(av.renderGaps);
    wrapMenuItem.setSelected(av.wrapAlignment);
    scaleAbove.setVisible(av.wrapAlignment);
    scaleLeft.setVisible(av.wrapAlignment);
    scaleRight.setVisible(av.wrapAlignment);
    annotationPanelMenuItem.setState(av.showAnnotation);
    viewBoxesMenuItem.setSelected(av.showBoxes);
    viewTextMenuItem.setSelected(av.showText);

    setColourSelected(ColourSchemeProperty.
                      getColourName(av.getGlobalColourScheme()));

    showSeqFeatures.setSelected(av.showSequenceFeatures);
    hiddenMarkers.setState(av.showHiddenMarkers);
    applyToAllGroups.setState(av.colourAppliesToAllGroups);

    updateEditMenuBar();
  }


  Hashtable progressBars;
  public void setProgressBar(String message, long id)
  {
    if(progressBars == null)
    {
      progressBars = new Hashtable();
    }

    JPanel progressPanel;
    GridLayout layout = (GridLayout) statusPanel.getLayout();
    if(progressBars.get( new Long(id) )!=null)
     {
       progressPanel = (JPanel)progressBars.get( new Long(id) );
       statusPanel.remove(progressPanel);
       progressBars.remove( progressPanel );
       progressPanel = null;
       if(message!=null)
      {
         statusBar.setText(message);
      }

       layout.setRows(layout.getRows() - 1);
     }
    else
    {
      progressPanel = new JPanel(new BorderLayout(10, 5));

      JProgressBar progressBar = new JProgressBar();
      progressBar.setIndeterminate(true);

      progressPanel.add(new JLabel(message), BorderLayout.WEST);
      progressPanel.add(progressBar, BorderLayout.CENTER);

      layout.setRows(layout.getRows() + 1);
      statusPanel.add(progressPanel);

      progressBars.put(new Long(id), progressPanel);
    }

    validate();
  }




  /*
   Added so Castor Mapping file can obtain Jalview Version
  */
  public String getVersion()
  {
    return  jalview.bin.Cache.getProperty("VERSION");
  }

  public FeatureRenderer getFeatureRenderer()
  {
    return alignPanel.seqPanel.seqCanvas.getFeatureRenderer();
  }


  public void fetchSequence_actionPerformed(ActionEvent e)
  {
    new SequenceFetcher(this);
  }

  public void addFromFile_actionPerformed(ActionEvent e)
  {
    Desktop.instance.inputLocalFileMenuItem_actionPerformed(viewport);
  }

  public void reload_actionPerformed(ActionEvent e)
  {
    if(fileName!=null)
    {
      if(currentFileFormat.equals("Jalview"))
      {
        JInternalFrame [] frames = Desktop.desktop.getAllFrames();
        for(int i=0; i<frames.length; i++)
        {
          if (frames[i] instanceof AlignFrame
              && frames[i] != this
              && ( (AlignFrame) frames[i]).fileName.equals(fileName))
          {
            try
            {
              frames[i].setSelected(true);
              Desktop.instance.closeAssociatedWindows();
          }
            catch (java.beans.PropertyVetoException ex)
            {}
          }

        }
        Desktop.instance.closeAssociatedWindows();

        FileLoader loader = new FileLoader();
        String protocol = fileName.startsWith("http:")? "URL":"File";
        loader.LoadFile(viewport, fileName, protocol, currentFileFormat);
      }
      else
      {
        Rectangle bounds = this.getBounds();

        FileLoader loader = new FileLoader();
        String protocol = fileName.startsWith("http:") ? "URL" : "File";
        AlignFrame newframe =
            loader.LoadFileWaitTillLoaded(fileName, protocol, currentFileFormat);

        newframe.setBounds(bounds);

        this.closeMenuItem_actionPerformed(true);
      }
    }
  }


  public void addFromText_actionPerformed(ActionEvent e)
  {
    Desktop.instance.inputTextboxMenuItem_actionPerformed(viewport);
  }

  public void addFromURL_actionPerformed(ActionEvent e)
  {
    Desktop.instance.inputURLMenuItem_actionPerformed(viewport);
  }


  public void save_actionPerformed(ActionEvent e)
  {
    if(fileName==null
       || currentFileFormat==null
       || fileName.startsWith("http")
        )
    {
      saveAs_actionPerformed(null);
    }
    else
    {
      saveAlignment(fileName, currentFileFormat);
  }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void saveAs_actionPerformed(ActionEvent e)
  {
    JalviewFileChooser chooser = new JalviewFileChooser(jalview.bin.Cache.
        getProperty( "LAST_DIRECTORY"),
        new String[]
        { "fa, fasta, fastq", "aln", "pfam", "msf", "pir", "blc","jar" },
        new String[]
        { "Fasta", "Clustal", "PFAM", "MSF", "PIR", "BLC", "Jalview" },
        currentFileFormat,
        false);


    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle("Save Alignment to file");
    chooser.setToolTipText("Save");

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
        currentFileFormat = chooser.getSelectedFormat();
        if (currentFileFormat == null)
        {
          JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                                "You must select a file format before saving!",
                                                "File format not specified",
                                                JOptionPane.WARNING_MESSAGE);
          value = chooser.showSaveDialog(this);
          return;
        }

        fileName = chooser.getSelectedFile().getPath();

      jalview.bin.Cache.setProperty("DEFAULT_FILE_FORMAT",
                                    currentFileFormat);

      jalview.bin.Cache.setProperty("LAST_DIRECTORY", fileName);

      saveAlignment(fileName, currentFileFormat);
    }
  }

  public boolean saveAlignment(String file, String format)
  {
    boolean success = true;

    if (format.equalsIgnoreCase("Jalview"))
    {
      String shortName = title;

      if (shortName.indexOf(java.io.File.separatorChar) > -1)
      {
        shortName = shortName.substring(shortName.lastIndexOf(
            java.io.File.separatorChar) + 1);
      }

      success = new Jalview2XML().SaveAlignment(this, file, shortName);

      statusBar.setText("Successfully saved to file: "
                          +fileName+" in "
                          +format +" format.");

    }
    else
    {

      String[] omitHidden = null;

      if (viewport.hasHiddenColumns)
      {
        int reply = JOptionPane.showInternalConfirmDialog(Desktop.desktop,
            "The Alignment contains hidden columns."
            + "\nDo you want to save only the visible alignment?",
            "Save / Omit Hidden Columns",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (reply == JOptionPane.YES_OPTION)
        {
          omitHidden = viewport.getViewAsString(false);
      }
      }

      String output = new FormatAdapter().formatSequences(
          format,
          viewport.alignment.getSequencesArray(),
          omitHidden);

      if (output == null)
      {
        success = false;
      }
      else
      {
        try
        {
          java.io.PrintWriter out = new java.io.PrintWriter(
              new java.io.FileWriter(file));

          out.print(output);
          out.close();
          this.setTitle(file);
          statusBar.setText("Successfully saved to file: "
                            + fileName + " in "
                            + format + " format.");
        }
        catch (Exception ex)
        {
          success = false;
          ex.printStackTrace();
        }
      }
    }

    if (!success)
    {
      JOptionPane.showInternalMessageDialog(
          this, "Couldn't save file: " + fileName,
          "Error Saving File",
          JOptionPane.WARNING_MESSAGE);
    }

    return success;
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void outputText_actionPerformed(ActionEvent e)
  {
    String [] omitHidden = null;

    if(viewport.hasHiddenColumns)
    {
      int reply = JOptionPane.showInternalConfirmDialog(Desktop.desktop,
          "The Alignment contains hidden columns."
      +"\nDo you want to output only the visible alignment?",
      "Save / Omit Hidden Columns",
      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

      if(reply==JOptionPane.YES_OPTION)
      {
        omitHidden = viewport.getViewAsString(false);
      }
    }

    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    cap.setForInput(null);
    Desktop.addInternalFrame(cap,
                             "Alignment output - " + e.getActionCommand(), 600,
                             500);


    cap.setText(new FormatAdapter().formatSequences(
        e.getActionCommand(),
        viewport.alignment.getSequencesArray(),
        omitHidden));
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void htmlMenuItem_actionPerformed(ActionEvent e)
  {
    new HTMLOutput(alignPanel,
                   alignPanel.seqPanel.seqCanvas.getSequenceRenderer(),
        alignPanel.seqPanel.seqCanvas.getFeatureRenderer());
  }

  public void createImageMap(File file, String image)
  {
    alignPanel.makePNGImageMap(file, image);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void createPNG(File f)
  {
    alignPanel.makePNG(f);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void createEPS(File f)
  {
    alignPanel.makeEPS(f);
  }


  public void pageSetup_actionPerformed(ActionEvent e)
  {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    PrintThread.pf = printJob.pageDialog(printJob.defaultPage());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void printMenuItem_actionPerformed(ActionEvent e)
  {
    //Putting in a thread avoids Swing painting problems
    PrintThread thread = new PrintThread(alignPanel);
    thread.start();
  }

  public void exportFeatures_actionPerformed(ActionEvent e)
  {
    new AnnotationExporter().exportFeatures(alignPanel);
  }


  public void exportAnnotations_actionPerformed(ActionEvent e)
  {
    new AnnotationExporter().exportAnnotations(
      alignPanel,
        viewport.showAnnotation ? viewport.alignment.getAlignmentAnnotation() : null,
      viewport.alignment.getGroups(),
      ((Alignment)viewport.alignment).alignmentProperties
        );
  }


  public void associatedData_actionPerformed(ActionEvent e)
  {
    // Pick the tree file
    JalviewFileChooser chooser = new JalviewFileChooser(jalview.bin.Cache.
        getProperty(
            "LAST_DIRECTORY"));
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle("Load Jalview Annotations or Features File");
    chooser.setToolTipText("Load Jalview Annotations / Features file");

    int value = chooser.showOpenDialog(null);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String choice = chooser.getSelectedFile().getPath();
      jalview.bin.Cache.setProperty("LAST_DIRECTORY", choice);
      loadJalviewDataFile(choice);
    }

  }


  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void closeMenuItem_actionPerformed(boolean closeAllTabs)
  {
    if(alignPanels!=null && alignPanels.size()<2)
    {
      closeAllTabs = true;
    }

    try
    {
      if(alignPanels!=null)
      {
        if (closeAllTabs)
        {
          for (int i = 0; i < alignPanels.size(); i++)
          {
            AlignmentPanel ap = (AlignmentPanel) alignPanels.elementAt(i);
            jalview.structure.StructureSelectionManager.getStructureSelectionManager()
                .removeStructureViewerListener(ap.seqPanel, null);
            PaintRefresher.RemoveComponent(ap.seqPanel.seqCanvas);
            PaintRefresher.RemoveComponent(ap.idPanel.idCanvas);
            PaintRefresher.RemoveComponent(ap);
            ap.av.alignment = null;
          }
        }
        else
        {
          int index = tabbedPane.getSelectedIndex();

          alignPanels.removeElement(alignPanel);
          PaintRefresher.RemoveComponent(alignPanel.seqPanel.seqCanvas);
          PaintRefresher.RemoveComponent(alignPanel.idPanel.idCanvas);
          PaintRefresher.RemoveComponent(alignPanel);
          viewport.alignment = null;
          alignPanel = null;
          viewport = null;

          tabbedPane.removeTabAt(index);
          tabbedPane.validate();

          if(index==tabbedPane.getTabCount())
          {
            index --;
          }

          this.tabSelectionChanged(index);
        }
      }

      if (closeAllTabs)
      {
        this.setClosed(true);
    }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
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
      undoMenuItem.setText("Undo " + command.getDescription());
    }
    else
    {
      undoMenuItem.setEnabled(false);
      undoMenuItem.setText("Undo");
    }

    if (viewport.redoList.size() > 0)
    {
      redoMenuItem.setEnabled(true);

      CommandI command = (CommandI) viewport.redoList.peek();
      redoMenuItem.setText("Redo " + command.getDescription());
    }
    else
    {
      redoMenuItem.setEnabled(false);
      redoMenuItem.setText("Redo");
    }
  }


  public void addHistoryItem(CommandI command)
  {
    if(command.getSize()>0)
    {
      viewport.historyList.push(command);
      viewport.redoList.clear();
      updateEditMenuBar();
      viewport.hasHiddenColumns = viewport.colSel.getHiddenColumns() != null;
    }
  }

  /**
   *
   * @return alignment objects for all views
   */
  AlignmentI[] getViewAlignments()
  {
    if (alignPanels!=null)
    {
      Enumeration e = alignPanels.elements();
      AlignmentI[] als = new AlignmentI[alignPanels.size()];
      for (int i=0; e.hasMoreElements(); i++)
      {
        als[i] = ((AlignmentPanel) e.nextElement()).av.getAlignment();
      }
      return als;
    }
    if (viewport!=null)
    {
      return new AlignmentI[] { viewport.alignment };
    }
    return null;
  }
  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void undoMenuItem_actionPerformed(ActionEvent e)
  {
    if (viewport.historyList.empty())
      return;
    CommandI command = (CommandI)viewport.historyList.pop();
    viewport.redoList.push(command);
    command.undoCommand(getViewAlignments());

    AlignViewport originalSource = getOriginatingSource(command);
    updateEditMenuBar();

    if(originalSource!=null)
    {
      originalSource.hasHiddenColumns = viewport.colSel.getHiddenColumns() != null;
      originalSource.firePropertyChange("alignment",
                                        null,
                                        originalSource.alignment.getSequences());
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void redoMenuItem_actionPerformed(ActionEvent e)
  {
    if(viewport.redoList.size()<1)
    {
      return;
    }

    CommandI command = (CommandI) viewport.redoList.pop();
    viewport.historyList.push(command);
    command.doCommand(getViewAlignments());

    AlignViewport originalSource = getOriginatingSource(command);
    updateEditMenuBar();

    if(originalSource!=null)
    {
      originalSource.hasHiddenColumns = viewport.colSel.getHiddenColumns() != null;
      originalSource.firePropertyChange("alignment",
                                        null,
                                        originalSource.alignment.getSequences());
    }
  }

  AlignViewport getOriginatingSource(CommandI command)
  {
    AlignViewport originalSource = null;
    //For sequence removal and addition, we need to fire
   //the property change event FROM the viewport where the
   //original alignment was altered
    AlignmentI al=null;
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

  /**
   * DOCUMENT ME!
   *
   * @param up DOCUMENT ME!
   */
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

        if (!sg.getSequences(null).contains(seq))
        {
          continue;
        }

        SequenceI temp = viewport.alignment.getSequenceAt(i + 1);

        if (sg.getSequences(null).contains(temp))
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
         invertGroup.add(viewport.alignment.getSequenceAt(i));
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


  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void copy_actionPerformed(ActionEvent e)
  {
    System.gc();
    if (viewport.getSelectionGroup() == null)
    {
      return;
    }
    //  TODO: preserve the ordering of displayed alignment annotation in any internal paste (particularly sequence associated annotation)
    SequenceI [] seqs = viewport.getSelectionAsNewSequence();
    String[] omitHidden = null;

    if (viewport.hasHiddenColumns)
    {
      omitHidden = viewport.getViewAsString(true);
    }

    String output = new FormatAdapter().formatSequences(
        "Fasta",
        seqs,
        omitHidden);

    StringSelection ss = new StringSelection(output);

    try
    {
      jalview.gui.Desktop.internalCopy = true;
      //Its really worth setting the clipboard contents
      //to empty before setting the large StringSelection!!
      Toolkit.getDefaultToolkit().getSystemClipboard()
          .setContents(new StringSelection(""), null);

      Toolkit.getDefaultToolkit().getSystemClipboard()
          .setContents(ss, Desktop.instance);
    }
    catch (OutOfMemoryError er)
    {
      er.printStackTrace();
      javax.swing.SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              javax.swing.JOptionPane.showInternalMessageDialog(Desktop.desktop,
                  "Out of memory copying region!!"
                  +
                  "\nSee help files for increasing Java Virtual Machine memory."
                  , "Out of memory",
                  javax.swing.JOptionPane.WARNING_MESSAGE);
            }
          });

      return;
    }

    Vector hiddenColumns = null;
    if(viewport.hasHiddenColumns)
    {
      hiddenColumns =new Vector();
      int hiddenOffset = viewport.getSelectionGroup().getStartRes();
      for (int i = 0; i < viewport.getColumnSelection().getHiddenColumns().size();
           i++)
      {
        int[] region = (int[])
            viewport.getColumnSelection().getHiddenColumns().elementAt(i);

        hiddenColumns.addElement(new int[]
                                 {region[0] - hiddenOffset,
                          region[1]-hiddenOffset});
      }
    }

    Desktop.jalviewClipboard = new Object[]
        {
        seqs,
        viewport.alignment.getDataset(),
        hiddenColumns};
    statusBar.setText("Copied "+seqs.length+" sequences to clipboard.");
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void pasteNew_actionPerformed(ActionEvent e)
  {
    paste(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void pasteThis_actionPerformed(ActionEvent e)
  {
    paste(false);
  }

  /**
   * DOCUMENT ME!
   *
   * @param newAlignment DOCUMENT ME!
   */
  void paste(boolean newAlignment)
  {
    boolean externalPaste=true;
    try
    {
      Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable contents = c.getContents(this);

      if (contents == null)
      {
        return;
      }

      String str, format;
      try
      {
        str = (String) contents.getTransferData(DataFlavor.stringFlavor);
        if (str.length() < 1)
        {
          return;
        }

        format = new IdentifyFile().Identify(str, "Paste");

      }
      catch (OutOfMemoryError er)
      {
        er.printStackTrace();
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            javax.swing.JOptionPane.showInternalMessageDialog(Desktop.desktop,
                "Out of memory pasting sequences!!"
                +
                "\nSee help files for increasing Java Virtual Machine memory."
                , "Out of memory",
                javax.swing.JOptionPane.WARNING_MESSAGE);
          }
        });

        return;
      }

      SequenceI[] sequences;
      boolean annotationAdded = false;
      AlignmentI alignment = null;

     if(Desktop.jalviewClipboard!=null)
     {
       // The clipboard was filled from within Jalview, we must use the sequences
       // And dataset from the copied alignment
       SequenceI[] newseq = (SequenceI[])Desktop.jalviewClipboard[0];
       // be doubly sure that we create *new* sequence objects.
       sequences = new SequenceI[newseq.length];
       for (int i=0;i<newseq.length;i++) {
         sequences[i] = new Sequence(newseq[i]);
       }
       alignment = new Alignment(sequences);
       externalPaste = false;
     }
     else
     {
       // parse the clipboard as an alignment.
       alignment = new FormatAdapter().readFile(str, "Paste", format);
       sequences = alignment.getSequencesArray();
     }

     int alwidth=0;

     if (newAlignment)
     {

       if (Desktop.jalviewClipboard != null)
       {
         // dataset is inherited
         alignment.setDataset( (Alignment) Desktop.jalviewClipboard[1]);
       }
       else
       {
         // new dataset is constructed
         alignment.setDataset(null);
       }
       alwidth = alignment.getWidth()+1;
     }
     else
     {
       AlignmentI pastedal = alignment; // preserve pasted alignment object
       // Add pasted sequences and dataset into existing alignment.
       alignment = viewport.getAlignment();
       alwidth = alignment.getWidth()+1;
        // decide if we need to import sequences from an existing dataset
        boolean importDs = Desktop.jalviewClipboard != null
                && Desktop.jalviewClipboard[1] != alignment.getDataset();
        // importDs==true instructs us to copy over new dataset sequences from
        // an existing alignment
        Vector newDs = (importDs) ? new Vector() : null; // used to create
                                                          // minimum dataset set

        for (int i = 0; i < sequences.length; i++)
        {
          if (importDs)
          {
            newDs.addElement(null);
          }
          SequenceI ds = sequences[i].getDatasetSequence(); // null for a simple
                                                            // paste
          if (importDs && ds != null)
          {
            if (!newDs.contains(ds))
            {
              newDs.setElementAt(ds, i);
              ds = new Sequence(ds);
              // update with new dataset sequence
              sequences[i].setDatasetSequence(ds);
            }
            else
            {
              ds = sequences[newDs.indexOf(ds)].getDatasetSequence();
            }
          }
          else
          {
            // copy and derive new dataset sequence
            sequences[i] = sequences[i].deriveSequence();
            alignment.getDataset().addSequence(sequences[i].getDatasetSequence());
            // TODO: avoid creation of duplicate dataset sequences with a
            // 'contains' method using SequenceI.equals()/SequenceI.contains()
          }
          alignment.addSequence(sequences[i]); // merges dataset
        }
        if (newDs != null)
        {
          newDs.clear(); // tidy up
        }
        if (pastedal.getAlignmentAnnotation()!=null) {
          // Add any annotation attached to alignment.
          AlignmentAnnotation[] alann = pastedal.getAlignmentAnnotation();
          for (int i=0; i<alann.length; i++)
          {
            annotationAdded=true;
            if (alann[i].sequenceRef==null && !alann[i].autoCalculated) {
              AlignmentAnnotation newann = new AlignmentAnnotation(alann[i]);
              newann.padAnnotation(alwidth);
              alignment.addAnnotation(newann);
            }
          }
        }
     }
     if (!newAlignment) {
       ///////
       // ADD HISTORY ITEM
       //
       addHistoryItem(new EditCommand(
               "Add sequences",
               EditCommand.PASTE,
               sequences,
               0,
               alignment.getWidth(),
               alignment)
              );
     }
     // Add any annotations attached to sequences
     for (int i = 0; i < sequences.length; i++)
     {
       if (sequences[i].getAnnotation() != null)
       {
         for (int a = 0; a < sequences[i].getAnnotation().length; a++)
         {
           annotationAdded=true;
           sequences[i].getAnnotation()[a].adjustForAlignment();
           sequences[i].getAnnotation()[a].padAnnotation(alwidth);
           alignment.addAnnotation(sequences[i].getAnnotation()[a]); // annotation was duplicated earlier
           alignment.setAnnotationIndex(sequences[i].getAnnotation()[a], a);
         }
       }
     }
     if (!newAlignment) {

       // propagate alignment changed.
       viewport.setEndSeq(alignment.getHeight());
       if (annotationAdded)
       {
         // Duplicate sequence annotation in all views.
         AlignmentI[] alview = this.getViewAlignments();
         for (int i = 0; i < sequences.length; i++)
         {
           AlignmentAnnotation sann[] = sequences[i].getAnnotation();
           if (sann == null)
             continue;
           for (int avnum=0;avnum<alview.length; avnum++)
           {
             if (alview[avnum]!=alignment)
             {
               // duplicate in a view other than the one with input focus
               int avwidth = alview[avnum].getWidth()+1;
               // this relies on sann being preserved after we 
               // modify the sequence's annotation array for each duplication
               for (int a=0; a<sann.length; a++)
               {
                 AlignmentAnnotation newann = new AlignmentAnnotation(sann[a]);
                 sequences[i].addAlignmentAnnotation(newann);
                 newann.padAnnotation(avwidth);
                 alview[avnum].addAnnotation(newann); // annotation was duplicated earlier
                 alview[avnum].setAnnotationIndex(newann, a);
               }
             }
           }
         }
         buildSortByAnnotationScoresMenu();
       }
       viewport.firePropertyChange("alignment", null, alignment.getSequences());

     } else {
       AlignFrame af = new AlignFrame(alignment, DEFAULT_WIDTH, DEFAULT_HEIGHT);
       String newtitle = new String("Copied sequences");

       if(Desktop.jalviewClipboard!=null && Desktop.jalviewClipboard[2]!=null)
         {
           Vector hc = (Vector)Desktop.jalviewClipboard[2];
           for(int i=0; i<hc.size(); i++)
           {
             int [] region = (int[]) hc.elementAt(i);
             af.viewport.hideColumns(region[0], region[1]);
           }
         }


       //>>>This is a fix for the moment, until a better solution is found!!<<<
       af.alignPanel.seqPanel.seqCanvas.getFeatureRenderer().transferSettings(
           alignPanel.seqPanel.seqCanvas.getFeatureRenderer());

       // TODO: maintain provenance of an alignment, rather than just make the title a concatenation of operations.
       if (!externalPaste) {
         if (title.startsWith("Copied sequences"))
         {
           newtitle = title;
         }
         else
         {
           newtitle = newtitle.concat("- from " + title);
         }
       } else {
         newtitle = new String("Pasted sequences");
       }

       Desktop.addInternalFrame(af, newtitle, DEFAULT_WIDTH,
                                DEFAULT_HEIGHT);

     }


    }
    catch (Exception ex)
    {
      ex.printStackTrace();
        System.out.println("Exception whilst pasting: "+ex);
        // could be anything being pasted in here
    }


  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void cut_actionPerformed(ActionEvent e)
  {
    copy_actionPerformed(null);
    delete_actionPerformed(null);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void delete_actionPerformed(ActionEvent evt)
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


    SequenceI [] cut = new SequenceI[seqs.size()];
    for(int i=0; i<seqs.size(); i++)
    {
      cut[i] = (SequenceI)seqs.elementAt(i);
    }


    /*
    //ADD HISTORY ITEM
    */
    addHistoryItem(new EditCommand("Cut Sequences",
                                      EditCommand.CUT,
                                      cut,
                                      sg.getStartRes(),
                                      sg.getEndRes()-sg.getStartRes()+1,
                                      viewport.alignment));


    viewport.setSelectionGroup(null);
    viewport.alignment.deleteGroup(sg);

    viewport.firePropertyChange("alignment", null,
                                  viewport.getAlignment().getSequences());

    if (viewport.getAlignment().getHeight() < 1)
    {
      try
      {
        this.setClosed(true);
      }
      catch (Exception ex)
      {
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void deleteGroups_actionPerformed(ActionEvent e)
  {
    viewport.alignment.deleteAllGroups();
    viewport.sequenceColours = null;
    viewport.setSelectionGroup(null);
    PaintRefresher.Refresh(this, viewport.getSequenceSetId());
    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void selectAllSequenceMenuItem_actionPerformed(ActionEvent e)
  {
    SequenceGroup sg = new SequenceGroup();

    for (int i = 0; i < viewport.getAlignment().getSequences().size();
         i++)
    {
      sg.addSequence(viewport.getAlignment().getSequenceAt(i), false);
    }

    sg.setEndRes(viewport.alignment.getWidth() - 1);
    viewport.setSelectionGroup(sg);
    alignPanel.paintAlignment(true);
    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void deselectAllSequenceMenuItem_actionPerformed(ActionEvent e)
  {
    if(viewport.cursorMode)
    {
      alignPanel.seqPanel.keyboardNo1 = null;
      alignPanel.seqPanel.keyboardNo2 = null;
    }
    viewport.setSelectionGroup(null);
    viewport.getColumnSelection().clear();
    viewport.setSelectionGroup(null);
    alignPanel.seqPanel.seqCanvas.highlightSearchResults(null);
    alignPanel.idPanel.idCanvas.searchResults = null;
    alignPanel.paintAlignment(true);
    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void invertSequenceMenuItem_actionPerformed(ActionEvent e)
  {
    SequenceGroup sg = viewport.getSelectionGroup();

    if (sg == null)
    {
      selectAllSequenceMenuItem_actionPerformed(null);

      return;
    }

    for (int i = 0; i < viewport.getAlignment().getSequences().size();
         i++)
    {
      sg.addOrRemove(viewport.getAlignment().getSequenceAt(i), false);
    }

    alignPanel.paintAlignment(true);

    PaintRefresher.Refresh(alignPanel, viewport.getSequenceSetId());
  }

  public void invertColSel_actionPerformed(ActionEvent e)
  {
    viewport.invertColumnSelection();
    alignPanel.paintAlignment(true);
  }


  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void remove2LeftMenuItem_actionPerformed(ActionEvent e)
  {
    trimAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void remove2RightMenuItem_actionPerformed(ActionEvent e)
  {
      trimAlignment(false);
  }

  void trimAlignment(boolean trimLeft)
  {
    ColumnSelection colSel = viewport.getColumnSelection();
    int column;

    if (colSel.size() > 0)
    {
      if(trimLeft)
      {
        column = colSel.getMin();
      }
      else
      {
        column = colSel.getMax();
      }

      SequenceI [] seqs;
      if(viewport.getSelectionGroup()!=null)
      {
        seqs = viewport.getSelectionGroup().getSequencesAsArray(viewport.
            hiddenRepSequences);
      }
      else
      {
        seqs = viewport.alignment.getSequencesArray();
      }


      TrimRegionCommand trimRegion;
      if(trimLeft)
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

     statusBar.setText("Removed "+trimRegion.getSize()+" columns.");


      addHistoryItem(trimRegion);

      Vector groups = viewport.alignment.getGroups();

      for (int i = 0; i < groups.size(); i++)
      {
        SequenceGroup sg = (SequenceGroup) groups.get(i);

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

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void removeGappedColumnMenuItem_actionPerformed(ActionEvent e)
  {
    int start = 0, end = viewport.alignment.getWidth()-1;

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

    statusBar.setText("Removed "+removeGapCols.getSize()+" empty columns.");

    //This is to maintain viewport position on first residue
    //of first sequence
    SequenceI seq = viewport.alignment.getSequenceAt(0);
    int startRes = seq.findPosition(viewport.startRes);
   // ShiftList shifts;
   // viewport.getAlignment().removeGaps(shifts=new ShiftList());
   // edit.alColumnChanges=shifts.getInverse();
   // if (viewport.hasHiddenColumns)
   //   viewport.getColumnSelection().compensateForEdits(shifts);
   viewport.setStartRes(seq.findIndex(startRes)-1);
    viewport.firePropertyChange("alignment", null,
                                viewport.getAlignment().getSequences());

  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void removeAllGapsMenuItem_actionPerformed(ActionEvent e)
  {
    int start = 0, end = viewport.alignment.getWidth()-1;

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

    viewport.setStartRes(seq.findIndex(startRes)-1);

    viewport.firePropertyChange("alignment", null,
                                viewport.getAlignment().getSequences());

  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void padGapsMenuitem_actionPerformed(ActionEvent e)
  {
    viewport.padGaps = padGapsMenuitem.isSelected();

    viewport.firePropertyChange("alignment",
                                null,
                                viewport.getAlignment().getSequences());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void findMenuItem_actionPerformed(ActionEvent e)
  {
    new Finder();
  }

  public void newView_actionPerformed(ActionEvent e)
  {
    AlignmentPanel newap =
        new Jalview2XML().copyAlignPanel(alignPanel, true);

    newap.av.gatherViewsHere = false;

    if (viewport.viewName == null)
    {
      viewport.viewName = "Original";
    }

    newap.av.historyList = viewport.historyList;
    newap.av.redoList = viewport.redoList;

    int index = Desktop.getViewCount(viewport.getSequenceSetId());
    String newViewName = "View " +index;

    Vector comps = (Vector) PaintRefresher.components.get(viewport.
        getSequenceSetId());
    Vector existingNames = new Vector();
    for(int i=0; i<comps.size(); i++)
    {
      if(comps.elementAt(i) instanceof AlignmentPanel)
      {
        AlignmentPanel ap = (AlignmentPanel)comps.elementAt(i);
        if(!existingNames.contains(ap.av.viewName))
        {
          existingNames.addElement(ap.av.viewName);
      }
    }
    }

    while(existingNames.contains(newViewName))
    {
      newViewName = "View "+ (++index);
    }

    newap.av.viewName = newViewName;

    addAlignmentPanel(newap, false);

    if(alignPanels.size()==2)
    {
      viewport.gatherViewsHere = true;
    }
    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
  }

  public void expandViews_actionPerformed(ActionEvent e)
  {
        Desktop.instance.explodeViews(this);
  }

  public void gatherViews_actionPerformed(ActionEvent e)
  {
    Desktop.instance.gatherViews(this);
  }



  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void font_actionPerformed(ActionEvent e)
  {
    new FontChooser(alignPanel);
  }


  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void seqLimit_actionPerformed(ActionEvent e)
  {
    viewport.setShowJVSuffix(seqLimits.isSelected());

    alignPanel.idPanel.idCanvas.setPreferredSize(alignPanel.calculateIdWidth());
    alignPanel.paintAlignment(true);
  }

  public void idRightAlign_actionPerformed(ActionEvent e)
  {
    viewport.rightAlignIds = idRightAlign.isSelected();
    alignPanel.paintAlignment(true);
  }



  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void colourTextMenuItem_actionPerformed(ActionEvent e)
  {
    viewport.setColourText(colourTextMenuItem.isSelected());
    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void wrapMenuItem_actionPerformed(ActionEvent e)
  {
    scaleAbove.setVisible(wrapMenuItem.isSelected());
    scaleLeft.setVisible(wrapMenuItem.isSelected());
    scaleRight.setVisible(wrapMenuItem.isSelected());
    viewport.setWrapAlignment(wrapMenuItem.isSelected());
    alignPanel.setWrapAlignment(wrapMenuItem.isSelected());
  }

  public void showAllSeqs_actionPerformed(ActionEvent e)
  {
    viewport.showAllHiddenSeqs();
  }

  public void showAllColumns_actionPerformed(ActionEvent e)
  {
    viewport.showAllHiddenColumns();
    repaint();
  }

  public void hideSelSequences_actionPerformed(ActionEvent e)
  {
    viewport.hideAllSelectedSeqs();
    alignPanel.paintAlignment(true);
  }

  public void hideSelColumns_actionPerformed(ActionEvent e)
  {
    viewport.hideSelectedColumns();
    alignPanel.paintAlignment(true);
  }

  public void hiddenMarkers_actionPerformed(ActionEvent e)
  {
    viewport.setShowHiddenMarkers(hiddenMarkers.isSelected());
    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void scaleAbove_actionPerformed(ActionEvent e)
  {
    viewport.setScaleAboveWrapped(scaleAbove.isSelected());
    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void scaleLeft_actionPerformed(ActionEvent e)
  {
    viewport.setScaleLeftWrapped(scaleLeft.isSelected());
    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void scaleRight_actionPerformed(ActionEvent e)
  {
    viewport.setScaleRightWrapped(scaleRight.isSelected());
    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void viewBoxesMenuItem_actionPerformed(ActionEvent e)
  {
    viewport.setShowBoxes(viewBoxesMenuItem.isSelected());
    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void viewTextMenuItem_actionPerformed(ActionEvent e)
  {
    viewport.setShowText(viewTextMenuItem.isSelected());
    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void renderGapsMenuItem_actionPerformed(ActionEvent e)
  {
    viewport.setRenderGaps(renderGapsMenuItem.isSelected());
    alignPanel.paintAlignment(true);
  }


  public FeatureSettings featureSettings;
  public void featureSettings_actionPerformed(ActionEvent e)
  {
    if(featureSettings !=null )
    {
      featureSettings.close();
      featureSettings = null;
    }
    featureSettings = new FeatureSettings(this);
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void showSeqFeatures_actionPerformed(ActionEvent evt)
  {
    viewport.setShowSequenceFeatures(showSeqFeatures.isSelected());
    alignPanel.paintAlignment(true);
    if (alignPanel.getOverviewPanel() != null)
    {
      alignPanel.getOverviewPanel().updateOverviewImage();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void annotationPanelMenuItem_actionPerformed(ActionEvent e)
  {
    viewport.setShowAnnotation(annotationPanelMenuItem.isSelected());
    alignPanel.setAnnotationVisible(annotationPanelMenuItem.isSelected());
  }

  public void alignmentProperties()
  {
    JEditorPane editPane = new JEditorPane("text/html","");
    editPane.setEditable(false);
    StringBuffer contents = new StringBuffer("<html>");

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

    contents.append("<br>Sequences: "+ viewport.alignment.getHeight());
    contents.append("<br>Minimum Sequence Length: "+min);
    contents.append("<br>Maximum Sequence Length: "+max);
    contents.append("<br>Average Length: "+(int)avg);

    if (((Alignment)viewport.alignment).getProperties() != null)
    {
      Hashtable props = ((Alignment)viewport.alignment).getProperties();
      Enumeration en = props.keys();
      contents.append("<br><br><table border=\"1\">");
      while(en.hasMoreElements())
      {
        String key = en.nextElement().toString();
        StringBuffer val = new StringBuffer();
        String vals = props.get(key).toString();
        int pos=0, npos;
        do {
          npos = vals.indexOf("\n",pos);
          if (npos==-1)
          {
            val.append(vals.substring(pos));
          } else {
            val.append(vals.substring(pos, npos));
            val.append("<br>");
          }
          pos = npos+1;
        } while (npos!=-1);
        contents.append("<tr><td>"+key+"</td><td>"+val+"</td></tr>");
      }
      contents.append("</table>");
    }
    editPane.setText(contents.toString()+"</html>");
    JInternalFrame frame = new JInternalFrame();
    frame.getContentPane().add(new JScrollPane(editPane));

    Desktop.instance.addInternalFrame(frame,"Alignment Properties: "+getTitle(),500,400);
  }


  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void overviewMenuItem_actionPerformed(ActionEvent e)
  {
    if (alignPanel.overviewPanel != null)
    {
      return;
    }

    JInternalFrame frame = new JInternalFrame();
    OverviewPanel overview = new OverviewPanel(alignPanel);
    frame.setContentPane(overview);
    Desktop.addInternalFrame(frame, "Overview " + this.getTitle(),
                             frame.getWidth(), frame.getHeight());
    frame.pack();
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    frame.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter()
    {
      public void internalFrameClosed(
          javax.swing.event.InternalFrameEvent evt)
      {
        alignPanel.setOverviewPanel(null);
      }
      ;
    });

    alignPanel.setOverviewPanel(overview);
  }

  public void textColour_actionPerformed(ActionEvent e)
  {
    new TextColourChooser().chooseColour(alignPanel, null);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void noColourmenuItem_actionPerformed(ActionEvent e)
  {
    changeColour(null);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void clustalColour_actionPerformed(ActionEvent e)
  {
    changeColour(new ClustalxColourScheme(
        viewport.alignment.getSequences(), viewport.alignment.getWidth()));
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void zappoColour_actionPerformed(ActionEvent e)
  {
    changeColour(new ZappoColourScheme());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void taylorColour_actionPerformed(ActionEvent e)
  {
    changeColour(new TaylorColourScheme());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void hydrophobicityColour_actionPerformed(ActionEvent e)
  {
    changeColour(new HydrophobicColourScheme());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void helixColour_actionPerformed(ActionEvent e)
  {
    changeColour(new HelixColourScheme());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void strandColour_actionPerformed(ActionEvent e)
  {
    changeColour(new StrandColourScheme());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void turnColour_actionPerformed(ActionEvent e)
  {
    changeColour(new TurnColourScheme());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void buriedColour_actionPerformed(ActionEvent e)
  {
    changeColour(new BuriedColourScheme());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void nucleotideColour_actionPerformed(ActionEvent e)
  {
    changeColour(new NucleotideColourScheme());
  }

  public void annotationColour_actionPerformed(ActionEvent e)
  {
    new AnnotationColourChooser(viewport, alignPanel);
  }


  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void applyToAllGroups_actionPerformed(ActionEvent e)
  {
    viewport.setColourAppliesToAllGroups(applyToAllGroups.isSelected());
  }

  /**
   * DOCUMENT ME!
   *
   * @param cs DOCUMENT ME!
   */
  public void changeColour(ColourSchemeI cs)
  {
    int threshold = 0;

    if(cs!=null)
    {
      if (viewport.getAbovePIDThreshold())
      {
        threshold = SliderPanel.setPIDSliderSource(alignPanel, cs,
                                                   "Background");

        cs.setThreshold(threshold,
                        viewport.getIgnoreGapsConsensus());

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
              sg.getSequences(viewport.hiddenRepSequences), sg.getWidth());
        }
        else if (cs instanceof UserColourScheme)
        {
          sg.cs = new UserColourScheme( ( (UserColourScheme) cs).getColours());
        }
        else
        {
          try
          {
            sg.cs = (ColourSchemeI) cs.getClass().newInstance();
          }
          catch (Exception ex)
          {
          }
        }

        if (viewport.getAbovePIDThreshold()
            || cs instanceof PIDColourScheme
            || cs instanceof Blosum62ColourScheme)
        {
         sg.cs.setThreshold(threshold,
                viewport.getIgnoreGapsConsensus());

         sg.cs.setConsensus(AAFrequency.calculate(
             sg.getSequences(viewport.hiddenRepSequences), sg.getStartRes(),
             sg.getEndRes()+1));
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
              hiddenRepSequences),
                                            sg.getStartRes(),
                                            sg.getEndRes()+1);
          c.calculate();
          c.verdict(false, viewport.ConsPercGaps);
          sg.cs.setConservation(c);
        }
        else
        {
          sg.cs.setConservation(null);
      }
    }
    }

    if (alignPanel.getOverviewPanel() != null)
    {
      alignPanel.getOverviewPanel().updateOverviewImage();
    }




    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void modifyPID_actionPerformed(ActionEvent e)
  {
    if (viewport.getAbovePIDThreshold() && viewport.globalColourScheme!=null)
    {
      SliderPanel.setPIDSliderSource(alignPanel,
                                     viewport.getGlobalColourScheme(),
                                     "Background");
      SliderPanel.showPIDSlider();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void modifyConservation_actionPerformed(ActionEvent e)
  {
    if (viewport.getConservationSelected() && viewport.globalColourScheme!=null)
    {
      SliderPanel.setConservationSlider(alignPanel,
                                        viewport.globalColourScheme,
                                        "Background");
      SliderPanel.showConservationSlider();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void conservationMenuItem_actionPerformed(ActionEvent e)
  {
    viewport.setConservationSelected(conservationMenuItem.isSelected());

    viewport.setAbovePIDThreshold(false);
    abovePIDThreshold.setSelected(false);

    changeColour(viewport.getGlobalColourScheme());

    modifyConservation_actionPerformed(null);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void abovePIDThreshold_actionPerformed(ActionEvent e)
  {
    viewport.setAbovePIDThreshold(abovePIDThreshold.isSelected());

    conservationMenuItem.setSelected(false);
    viewport.setConservationSelected(false);

    changeColour(viewport.getGlobalColourScheme());

    modifyPID_actionPerformed(null);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void userDefinedColour_actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals("User Defined..."))
    {
      new UserDefinedColours(alignPanel, null);
    }
    else
    {
      UserColourScheme udc = (UserColourScheme) UserDefinedColours.
          getUserColourSchemes().get(e.getActionCommand());

      changeColour(udc);
    }
  }

  public void updateUserColourMenu()
  {

    Component[] menuItems = colourMenu.getMenuComponents();
    int i, iSize = menuItems.length;
    for (i = 0; i < iSize; i++)
    {
      if (menuItems[i].getName() != null &&
          menuItems[i].getName().equals("USER_DEFINED"))
      {
        colourMenu.remove(menuItems[i]);
        iSize--;
      }
    }
    if (jalview.gui.UserDefinedColours.getUserColourSchemes() != null)
    {
      java.util.Enumeration userColours = jalview.gui.UserDefinedColours.
          getUserColourSchemes().keys();

      while (userColours.hasMoreElements())
      {
        final JRadioButtonMenuItem radioItem = new JRadioButtonMenuItem(
            userColours.
            nextElement().toString());
        radioItem.setName("USER_DEFINED");
        radioItem.addMouseListener(new MouseAdapter()
            {
              public void mousePressed(MouseEvent evt)
              {
                if(evt.isControlDown() || SwingUtilities.isRightMouseButton(evt))
                {
                  radioItem.removeActionListener(radioItem.getActionListeners()[0]);

              int option = JOptionPane.showInternalConfirmDialog(jalview.gui.
                  Desktop.desktop,
                      "Remove from default list?",
                      "Remove user defined colour",
                      JOptionPane.YES_NO_OPTION);
                  if(option == JOptionPane.YES_OPTION)
                  {
                jalview.gui.UserDefinedColours.removeColourFromDefaults(
                    radioItem.getText());
                    colourMenu.remove(radioItem);
                  }
                  else
              {
                    radioItem.addActionListener(new ActionListener()
                    {
                      public void actionPerformed(ActionEvent evt)
                      {
                        userDefinedColour_actionPerformed(evt);
                      }
                    });
                }
              }
          }
            });
        radioItem.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent evt)
          {
            userDefinedColour_actionPerformed(evt);
          }
        });

        colourMenu.insert(radioItem, 15);
        colours.add(radioItem);
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void PIDColour_actionPerformed(ActionEvent e)
  {
    changeColour(new PIDColourScheme());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void BLOSUM62Colour_actionPerformed(ActionEvent e)
  {
    changeColour(new Blosum62ColourScheme());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void sortPairwiseMenuItem_actionPerformed(ActionEvent e)
  {
    SequenceI [] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByPID(viewport.getAlignment(),
                              viewport.getAlignment().getSequenceAt(0));
    addHistoryItem(new OrderCommand("Pairwise Sort", oldOrder,
                                    viewport.alignment));
    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void sortIDMenuItem_actionPerformed(ActionEvent e)
  {
    SequenceI [] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByID(viewport.getAlignment());
    addHistoryItem(new OrderCommand("ID Sort", oldOrder, viewport.alignment));
    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void sortGroupMenuItem_actionPerformed(ActionEvent e)
  {
    SequenceI [] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByGroup(viewport.getAlignment());
    addHistoryItem(new OrderCommand("Group Sort", oldOrder, viewport.alignment));

    alignPanel.paintAlignment(true);
  }
  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void removeRedundancyMenuItem_actionPerformed(ActionEvent e)
  {
    new RedundancyPanel(alignPanel, this);
  }


  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void pairwiseAlignmentMenuItem_actionPerformed(ActionEvent e)
  {
    if ( (viewport.getSelectionGroup() == null) ||
        (viewport.getSelectionGroup().getSize() < 2))
    {
      JOptionPane.showInternalMessageDialog(this,
                                            "You must select at least 2 sequences.",
                                            "Invalid Selection",
                                            JOptionPane.WARNING_MESSAGE);
    }
    else
    {
      JInternalFrame frame = new JInternalFrame();
      frame.setContentPane(new PairwiseAlignPanel(viewport));
      Desktop.addInternalFrame(frame, "Pairwise Alignment", 600, 500);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void PCAMenuItem_actionPerformed(ActionEvent e)
  {
    if ( ( (viewport.getSelectionGroup() != null) &&
          (viewport.getSelectionGroup().getSize() < 4) &&
          (viewport.getSelectionGroup().getSize() > 0)) ||
        (viewport.getAlignment().getHeight() < 4))
    {
      JOptionPane.showInternalMessageDialog(this,
                                            "Principal component analysis must take\n" +
                                            "at least 4 input sequences.",
                                            "Sequence selection insufficient",
                                            JOptionPane.WARNING_MESSAGE);

      return;
    }

     new PCAPanel(alignPanel);
  }


  public void autoCalculate_actionPerformed(ActionEvent e)
  {
    viewport.autoCalculateConsensus = autoCalculate.isSelected();
    if(viewport.autoCalculateConsensus)
    {
      viewport.firePropertyChange("alignment",
                                  null,
                                  viewport.getAlignment().getSequences());
    }
  }


  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void averageDistanceTreeMenuItem_actionPerformed(ActionEvent e)
  {
    NewTreePanel("AV", "PID", "Average distance tree using PID");
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void neighbourTreeMenuItem_actionPerformed(ActionEvent e)
  {
    NewTreePanel("NJ", "PID", "Neighbour joining tree using PID");
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void njTreeBlosumMenuItem_actionPerformed(ActionEvent e)
  {
    NewTreePanel("NJ", "BL", "Neighbour joining tree using BLOSUM62");
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void avTreeBlosumMenuItem_actionPerformed(ActionEvent e)
  {
    NewTreePanel("AV", "BL", "Average distance tree using BLOSUM62");
  }

  /**
   * DOCUMENT ME!
   *
   * @param type DOCUMENT ME!
   * @param pwType DOCUMENT ME!
   * @param title DOCUMENT ME!
   */
  void NewTreePanel(String type, String pwType, String title)
  {
    TreePanel tp;

    if (viewport.getSelectionGroup() != null)
    {
      if (viewport.getSelectionGroup().getSize() < 3)
      {
        JOptionPane.showMessageDialog(Desktop.desktop,
                                      "You need to have more than two sequences selected to build a tree!",
                                      "Not enough sequences",
                                      JOptionPane.WARNING_MESSAGE);
        return;
      }

      int s = 0;
      SequenceGroup sg = viewport.getSelectionGroup();

      /* Decide if the selection is a column region */
      while (s < sg.getSize())
      {
        if ( ( (SequenceI) sg.getSequences(null).elementAt(s++)).getLength() <
            sg.getEndRes())
        {
          JOptionPane.showMessageDialog(Desktop.desktop,
                                        "The selected region to create a tree may\nonly contain residues or gaps.\n" +
                                        "Try using the Pad function in the edit menu,\n" +
                                        "or one of the multiple sequence alignment web services.",
                                        "Sequences in selection are not aligned",
                                        JOptionPane.WARNING_MESSAGE);

          return;
        }
      }

      title = title + " on region";
      tp = new TreePanel(alignPanel, type, pwType);
    }
    else
    {
      //are the sequences aligned?
      if (!viewport.alignment.isAligned())
      {
        JOptionPane.showMessageDialog(Desktop.desktop,
                                      "The sequences must be aligned before creating a tree.\n" +
                                      "Try using the Pad function in the edit menu,\n" +
                                      "or one of the multiple sequence alignment web services.",
                                      "Sequences not aligned",
                                      JOptionPane.WARNING_MESSAGE);

        return;
      }

      if(viewport.alignment.getHeight()<2)
      {
        return;
      }

      tp = new TreePanel(alignPanel, type, pwType);
    }

    title += " from ";

    if(viewport.viewName!=null)
    {
      title+= viewport.viewName+" of ";
    }

    title += this.title;

    Desktop.addInternalFrame(tp, title, 600, 500);
  }

  /**
   * DOCUMENT ME!
   *
   * @param title DOCUMENT ME!
   * @param order DOCUMENT ME!
   */
  public void addSortByOrderMenuItem(String title, final AlignmentOrder order)
  {
    final JMenuItem item = new JMenuItem("by " + title);
    sort.add(item);
    item.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        SequenceI [] oldOrder = viewport.getAlignment().getSequencesArray();

        // TODO: JBPNote - have to map order entries to curent SequenceI pointers
        AlignmentSorter.sortBy(viewport.getAlignment(), order);

        addHistoryItem(new OrderCommand(order.getName(), oldOrder,
                                        viewport.alignment));

        alignPanel.paintAlignment(true);
      }
    });
  }
  /**
   * Add a new sort by annotation score menu item
   * @param sort the menu to add the option to
   * @param scoreLabel the label used to retrieve scores for each sequence on the alignment
   */
  public void addSortByAnnotScoreMenuItem(JMenu sort, final String scoreLabel)
  {
    final JMenuItem item = new JMenuItem(scoreLabel);
    sort.add(item);
    item.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        SequenceI [] oldOrder = viewport.getAlignment().getSequencesArray();
        AlignmentSorter.sortByAnnotationScore(scoreLabel, viewport.getAlignment());
        addHistoryItem(new OrderCommand("Sort by "+scoreLabel, oldOrder, viewport.alignment));
        alignPanel.paintAlignment(true);
      }
    });
  }
  /**
   * last hash for alignment's annotation array - used to minimise cost of rebuild.
   */
  protected int _annotationScoreVectorHash;
  /**
   * search the alignment and rebuild the sort by annotation score submenu
   * the last alignment annotation vector hash is stored to minimize
   * cost of rebuilding in subsequence calls.
   *
   */
  public void buildSortByAnnotationScoresMenu()
  {
    if(viewport.alignment.getAlignmentAnnotation()==null)
    {
      return;
    }

    if (viewport.alignment.getAlignmentAnnotation().hashCode()!=_annotationScoreVectorHash)
    {
      sortByAnnotScore.removeAll();
      // almost certainly a quicker way to do this - but we keep it simple
      Hashtable scoreSorts=new Hashtable();
      AlignmentAnnotation aann[];
      Enumeration sq = viewport.alignment.getSequences().elements();
      while (sq.hasMoreElements())
      {
        aann = ((SequenceI) sq.nextElement()).getAnnotation();
        for (int i=0;aann!=null && i<aann.length; i++)
        {
          if (aann[i].hasScore() && aann[i].sequenceRef!=null)
          {
            scoreSorts.put(aann[i].label, aann[i].label);
          }
        }
      }
      Enumeration labels = scoreSorts.keys();
      while (labels.hasMoreElements())
      {
        addSortByAnnotScoreMenuItem(sortByAnnotScore, (String) labels.nextElement());
      }
      sortByAnnotScore.setVisible(scoreSorts.size()>0);
      scoreSorts.clear();

      _annotationScoreVectorHash =
          viewport.alignment.getAlignmentAnnotation().hashCode();
    }
  }

  /**
   * Maintain the Order by->Displayed Tree menu.
   * Creates a new menu item for a TreePanel with an appropriate
   * <code>jalview.analysis.AlignmentSorter</code> call. Listeners are added
   * to remove the menu item when the treePanel is closed, and adjust
   * the tree leaf to sequence mapping when the alignment is modified.
   * @param treePanel Displayed tree window.
   * @param title SortBy menu item title.
   */
  public void buildTreeMenu()
  {
    sortByTreeMenu.removeAll();

    Vector comps = (Vector) PaintRefresher.components.get(viewport.
        getSequenceSetId());
    Vector treePanels = new Vector();
    int i, iSize = comps.size();
    for(i=0; i<iSize; i++)
    {
      if(comps.elementAt(i) instanceof TreePanel)
      {
        treePanels.add(comps.elementAt(i));
      }
    }

    iSize = treePanels.size();

    if(iSize<1)
    {
      sortByTreeMenu.setVisible(false);
      return;
    }

    sortByTreeMenu.setVisible(true);

    for(i=0; i<treePanels.size(); i++)
    {
      TreePanel tp = (TreePanel)treePanels.elementAt(i);
      final JMenuItem item = new JMenuItem(tp.getTitle());
      final NJTree tree = ((TreePanel)treePanels.elementAt(i)).getTree();
      item.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
          AlignmentSorter.sortByTree(viewport.getAlignment(), tree);

          addHistoryItem(new OrderCommand("Tree Sort",
                                          oldOrder,
                                          viewport.alignment));

          alignPanel.paintAlignment(true);
        }
      });

      sortByTreeMenu.add(item);
    }
  }

  /**
   * Work out whether the whole set of sequences
   * or just the selected set will be submitted for multiple alignment.
   *
   */
  private jalview.datamodel.AlignmentView gatherSequencesForAlignment()
  {
    // Now, check we have enough sequences
    AlignmentView msa = null;

    if ( (viewport.getSelectionGroup() != null) &&
        (viewport.getSelectionGroup().getSize() > 1))
    {
      // JBPNote UGLY! To prettify, make SequenceGroup and Alignment conform to some common interface!
      /*SequenceGroup seqs = viewport.getSelectionGroup();
      int sz;
      msa = new SequenceI[sz = seqs.getSize(false)];

      for (int i = 0; i < sz; i++)
      {
        msa[i] = (SequenceI) seqs.getSequenceAt(i);
      } */
      msa = viewport.getAlignmentView(true);
    }
    else
    {
      /*Vector seqs = viewport.getAlignment().getSequences();

      if (seqs.size() > 1)
      {
        msa = new SequenceI[seqs.size()];

        for (int i = 0; i < seqs.size(); i++)
        {
          msa[i] = (SequenceI) seqs.elementAt(i);
        }
      }*/
      msa = viewport.getAlignmentView(false);
    }
    return msa;
  }

  /**
   * Decides what is submitted to a secondary structure prediction service,
   * the currently selected sequence, or the currently selected alignment
   * (where the first sequence in the set is the one that the prediction
   * will be for).
   */
  AlignmentView gatherSeqOrMsaForSecStrPrediction()
  {
   AlignmentView seqs = null;

    if ( (viewport.getSelectionGroup() != null) &&
        (viewport.getSelectionGroup().getSize() > 0))
    {
      seqs = viewport.getAlignmentView(true);
    }
    else
    {
      seqs = viewport.getAlignmentView(false);
    }
    // limit sequences - JBPNote in future - could spawn multiple prediction jobs
    // TODO: viewport.alignment.isAligned is a global state - the local selection may well be aligned - we preserve 2.0.8 behaviour for moment.
    if (!viewport.alignment.isAligned())
    {
      seqs.setSequences(new SeqCigar[]
                        {seqs.getSequences()[0]});
    }
    return seqs;
  }
  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void LoadtreeMenuItem_actionPerformed(ActionEvent e)
  {
    // Pick the tree file
    JalviewFileChooser chooser = new JalviewFileChooser(jalview.bin.Cache.
        getProperty(
            "LAST_DIRECTORY"));
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle("Select a newick-like tree file");
    chooser.setToolTipText("Load a tree file");

    int value = chooser.showOpenDialog(null);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String choice = chooser.getSelectedFile().getPath();
      jalview.bin.Cache.setProperty("LAST_DIRECTORY", choice);

      try
      {
        jalview.io.NewickFile fin = new jalview.io.NewickFile(choice,
            "File");
        viewport.setCurrentTree(ShowNewickTree(fin, choice).getTree());
      }
      catch (Exception ex)
      {
        JOptionPane.showMessageDialog(Desktop.desktop,
                                      "Problem reading tree file",
                                      ex.getMessage(),
                                      JOptionPane.WARNING_MESSAGE);
        ex.printStackTrace();
      }
    }
  }


  public TreePanel ShowNewickTree(NewickFile nf, String title)
  {
    return ShowNewickTree(nf,title,600,500,4,5);
  }

  public TreePanel ShowNewickTree(NewickFile nf, String title,
                                  AlignmentView input)
  {
    return ShowNewickTree(nf,title, input, 600,500,4,5);
  }

  public TreePanel ShowNewickTree(NewickFile nf, String title, int w, int h,
                                  int x, int y)
  {
    return ShowNewickTree(nf, title, null, w, h, x, y);
  }
  /**
   * Add a treeviewer for the tree extracted from a newick file object to the current alignment view
   *
   * @param nf the tree
   * @param title tree viewer title
   * @param input Associated alignment input data (or null)
   * @param w width
   * @param h height
   * @param x position
   * @param y position
   * @return TreePanel handle
   */
  public TreePanel ShowNewickTree(NewickFile nf, String title,
                                  AlignmentView input, int w, int h, int x,
                                  int y)
  {
    TreePanel tp = null;

    try
    {
      nf.parse();

      if (nf.getTree() != null)
      {
        tp = new TreePanel(alignPanel,
                           "FromFile",
                           title,
                           nf, input);

        tp.setSize(w,h);

        if(x>0 && y>0)
        {
          tp.setLocation(x,y);
        }


        Desktop.addInternalFrame(tp, title, w, h);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    return tp;
  }


  /**
   * Generates menu items and listener event actions for web service clients
   *
   */
  public void BuildWebServiceMenu()
  {
    if ( (Discoverer.services != null)
        && (Discoverer.services.size() > 0))
    {
      Vector msaws = (Vector) Discoverer.services.get("MsaWS");
      Vector secstrpr = (Vector) Discoverer.services.get("SecStrPred");
      Vector wsmenu = new Vector();
      final AlignFrame af = this;
      if (msaws != null)
      {
        // Add any Multiple Sequence Alignment Services
        final JMenu msawsmenu = new JMenu("Alignment");
        for (int i = 0, j = msaws.size(); i < j; i++)
        {
          final ext.vamsas.ServiceHandle sh = (ext.vamsas.ServiceHandle) msaws.
              get(i);
          final JMenuItem method = new JMenuItem(sh.getName());
          method.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              AlignmentView msa = gatherSequencesForAlignment();
              new jalview.ws.MsaWSClient(sh, title, msa,
                                         false, true,
                                         viewport.getAlignment().getDataset(),
                                         af);

            }

          });
          msawsmenu.add(method);
          // Deal with services that we know accept partial alignments.
          if (sh.getName().indexOf("lustal") > -1)
          {
            // We know that ClustalWS can accept partial alignments for refinement.
            final JMenuItem methodR = new JMenuItem(sh.getName()+" Realign");
            methodR.addActionListener(new ActionListener()
            {
              public void actionPerformed(ActionEvent e)
              {
                AlignmentView msa = gatherSequencesForAlignment();
                new jalview.ws.MsaWSClient(sh, title, msa,
                                           true, true,
                                           viewport.getAlignment().getDataset(),
                                           af);

              }

            });
            msawsmenu.add(methodR);

          }
        }
        wsmenu.add(msawsmenu);
      }
      if (secstrpr != null)
      {
        // Add any secondary structure prediction services
        final JMenu secstrmenu = new JMenu("Secondary Structure Prediction");
        for (int i = 0, j = secstrpr.size(); i < j; i++)
        {
          final ext.vamsas.ServiceHandle sh = (ext.vamsas.ServiceHandle)
              secstrpr.get(i);
          final JMenuItem method = new JMenuItem(sh.getName());
          method.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              AlignmentView msa = gatherSeqOrMsaForSecStrPrediction();
              if (msa.getSequences().length == 1)
              {
                // Single Sequence prediction
                new jalview.ws.JPredClient(sh, title, false, msa, af, true);
              }
              else
              {
                if (msa.getSequences().length > 1)
                {
                  // Sequence profile based prediction
                  new jalview.ws.JPredClient(sh,
                      title, true, msa, af, true);
                }
              }
            }
          });
          secstrmenu.add(method);
        }
        wsmenu.add(secstrmenu);
      }
      resetWebServiceMenu();
      for (int i = 0, j = wsmenu.size(); i < j; i++)
      {
        webService.add( (JMenu) wsmenu.get(i));
      }
    }
    else
    {
      resetWebServiceMenu();
      this.webService.add(this.webServiceNoServices);
    }
    // TODO: add in rediscovery function
    // TODO: reduce code redundancy.
    // TODO: group services by location as well as function.
  }


  /**
   * empty the web service menu and add any ad-hoc functions
   * not dynamically discovered.
   *
   */
  private void resetWebServiceMenu()
  {
    webService.removeAll();
    // Temporary hack - DBRef Fetcher always top level ws entry.
    JMenuItem rfetch = new JMenuItem("Fetch DB References");
    rfetch.setToolTipText("Retrieve and parse uniprot records for the alignment or the currently selected sequences");
    webService.add(rfetch);
    rfetch.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e)
      {
        new jalview.io.DBRefFetcher(
                alignPanel.av.getSequenceSelection(),
                alignPanel.alignFrame).fetchDBRefs(false);
      }

    });
  }

 /* public void vamsasStore_actionPerformed(ActionEvent e)
  {
    JalviewFileChooser chooser = new JalviewFileChooser(jalview.bin.Cache.
        getProperty("LAST_DIRECTORY"));

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle("Export to Vamsas file");
    chooser.setToolTipText("Export");

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      jalview.io.VamsasDatastore vs = new jalview.io.VamsasDatastore(viewport);
      //vs.store(chooser.getSelectedFile().getAbsolutePath()   );
      vs.storeJalview( chooser.getSelectedFile().getAbsolutePath(), this);
    }
  }*/



public void showTranslation_actionPerformed(ActionEvent e)
{
  ///////////////////////////////
  // Collect Data to be translated/transferred

  SequenceI [] selection = viewport.getSequenceSelection();
  String [] seqstring = viewport.getViewAsString(true);
  AlignmentI al  = null;
  try {
    al = jalview.analysis.Dna.CdnaTranslate(selection, seqstring, viewport.getViewAsVisibleContigs(true),
        viewport.getGapCharacter(), viewport.alignment.getAlignmentAnnotation(),
        viewport.alignment.getWidth());
  } catch (Exception ex) {
    al = null;
    jalview.bin.Cache.log.debug("Exception during translation.",ex);
  }
  if (al==null)
  {
    JOptionPane.showMessageDialog(Desktop.desktop,
        "Please select at least three bases in at least one sequence in order to perform a cDNA translation.",
        "Translation Failed",
        JOptionPane.WARNING_MESSAGE);
  } else {
    AlignFrame af = new AlignFrame(al, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    Desktop.addInternalFrame(af, "Translation of "+this.getTitle(),
                             DEFAULT_WIDTH,
                             DEFAULT_HEIGHT);
  }
}

/**
 * DOCUMENT ME!
 *
 * @param String DOCUMENT ME!
 */
public boolean parseFeaturesFile(String file, String type)
{
    boolean featuresFile = false;
    try
    {
      featuresFile = new FeaturesFile(file,
          type).parse(viewport.alignment.getDataset(),
                                         alignPanel.seqPanel.seqCanvas.
                                         getFeatureRenderer().featureColours,
                                         false);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }

    if(featuresFile)
    {
      viewport.showSequenceFeatures = true;
      showSeqFeatures.setSelected(true);
      alignPanel.paintAlignment(true);
    }

    return featuresFile;
}

public void dragEnter(DropTargetDragEvent evt)
{}

public void dragExit(DropTargetEvent evt)
{}

public void dragOver(DropTargetDragEvent evt)
{}

public void dropActionChanged(DropTargetDragEvent evt)
{}

public void drop(DropTargetDropEvent evt)
{
    Transferable t = evt.getTransferable();
    java.util.List files = null;

    try
    {
      DataFlavor uriListFlavor = new DataFlavor(
          "text/uri-list;class=java.lang.String");
      if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
      {
        //Works on Windows and MacOSX
        evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        files = (java.util.List) t.getTransferData(DataFlavor.
            javaFileListFlavor);
      }
      else if (t.isDataFlavorSupported(uriListFlavor))
      {
        // This is used by Unix drag system
        evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        String data = (String) t.getTransferData(uriListFlavor);
        files = new java.util.ArrayList(1);
        for (java.util.StringTokenizer st = new java.util.StringTokenizer(
            data,
            "\r\n");
             st.hasMoreTokens(); )
        {
          String s = st.nextToken();
          if (s.startsWith("#"))
          {
            // the line is a comment (as per the RFC 2483)
            continue;
          }

          java.net.URI uri = new java.net.URI(s);
          java.io.File file = new java.io.File(uri);
          files.add(file);
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    if (files != null)
    {
      try
      {

        for (int i = 0; i < files.size(); i++)
        {
          loadJalviewDataFile(files.get(i).toString());
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
}

  // This method will attempt to load a "dropped" file first by testing
  // whether its and Annotation file, then features file. If both are
  // false then the user may have dropped an alignment file onto this
  // AlignFrame
   public void loadJalviewDataFile(String file)
  {
    try
    {
      String protocol = "File";

      if (file.indexOf("http:") > -1 || file.indexOf("file:") > -1)
      {
        protocol = "URL";
      }

      boolean isAnnotation = new AnnotationFile().readAnnotationFile(viewport.
          alignment, file, protocol);

      if (!isAnnotation)
      {
        boolean isGroupsFile = parseFeaturesFile(file,protocol);
        if (!isGroupsFile)
        {
          String format = new IdentifyFile().Identify(file, protocol);

          if(format.equalsIgnoreCase("JnetFile"))
          {
            jalview.io.JPredFile predictions = new jalview.io.JPredFile(
                file, protocol);
            new JnetAnnotationMaker().add_annotation(predictions,
                viewport.getAlignment(),
                0, false);
            alignPanel.adjustAnnotationHeight();
            alignPanel.paintAlignment(true);
          }
          else
          {
            new FileLoader().LoadFile(viewport, file, protocol, format);
        }
      }
      }
      else
      {
        // (isAnnotation)
        alignPanel.adjustAnnotationHeight();
        buildSortByAnnotationScoresMenu();
      }

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void tabSelectionChanged(int index)
  {
    if (index > -1)
    {
      alignPanel = (AlignmentPanel) alignPanels.elementAt(index);
      viewport = alignPanel.av;
      setMenusFromViewport(viewport);
    }
  }

  public void tabbedPane_mousePressed(MouseEvent e)
  {
    if(SwingUtilities.isRightMouseButton(e))
    {
      String reply = JOptionPane.showInternalInputDialog(this,
          "Enter View Name",
          "Edit View Name",
          JOptionPane.QUESTION_MESSAGE);

      if (reply != null)
      {
        viewport.viewName = reply;
        tabbedPane.setTitleAt( tabbedPane.getSelectedIndex() ,reply);
      }
    }
  }


  public AlignViewport getCurrentView()
  {
    return viewport;
  }


  /**
   * Open the dialog for regex description parsing.
   */
  protected void extractScores_actionPerformed(ActionEvent e)
  {
    ParseProperties pp = new jalview.analysis.ParseProperties(viewport.alignment);
    if (pp.getScoresFromDescription("col", "score column ", "\\W+([-+]?\\d*\\.?\\d*e?-?\\d*)\\W+([-+]?\\d*\\.?\\d*e?-?\\d*)")>0)
    {
      buildSortByAnnotationScoresMenu();
    }
  }
}

class PrintThread
    extends Thread
{
  AlignmentPanel ap;
  public PrintThread(AlignmentPanel ap)
  {
   this.ap = ap;
  }
  static PageFormat pf;
  public void run()
  {
    PrinterJob printJob = PrinterJob.getPrinterJob();

    if (pf != null)
    {
      printJob.setPrintable(ap, pf);
    }
    else
    {
      printJob.setPrintable(ap);
    }

    if (printJob.printDialog())
    {
      try
      {
        printJob.print();
      }
      catch (Exception PrintException)
      {
        PrintException.printStackTrace();
      }
    }
  }
}
