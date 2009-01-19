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

import java.beans.*;
import java.io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;

import jalview.datamodel.*;
import jalview.jbgui.*;
import jalview.schemes.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class AlignmentPanel
    extends GAlignmentPanel implements AdjustmentListener, Printable
{
  public AlignViewport av;
  OverviewPanel overviewPanel;
  SeqPanel seqPanel;
  IdPanel idPanel;
  IdwidthAdjuster idwidthAdjuster;

  /** DOCUMENT ME!! */
  public AlignFrame alignFrame;
  ScalePanel scalePanel;
  AnnotationPanel annotationPanel;
  AnnotationLabels alabels;

  // this value is set false when selection area being dragged
  boolean fastPaint = true;
  int hextent = 0;
  int vextent = 0;

  /**
   * Creates a new AlignmentPanel object.
   *
   * @param af DOCUMENT ME!
   * @param av DOCUMENT ME!
   */
  public AlignmentPanel(AlignFrame af, final AlignViewport av)
  {
    alignFrame = af;
    this.av = av;
    seqPanel = new SeqPanel(av, this);
    idPanel = new IdPanel(av, this);

    scalePanel = new ScalePanel(av, this);

    idPanelHolder.add(idPanel, BorderLayout.CENTER);
    idwidthAdjuster = new IdwidthAdjuster(this);
    idSpaceFillerPanel1.add(idwidthAdjuster, BorderLayout.CENTER);

    annotationPanel = new AnnotationPanel(this);
    alabels = new AnnotationLabels(this);

    annotationScroller.setViewportView(annotationPanel);
    annotationSpaceFillerHolder.add(alabels, BorderLayout.CENTER);

    scalePanelHolder.add(scalePanel, BorderLayout.CENTER);
    seqPanelHolder.add(seqPanel, BorderLayout.CENTER);

    setScrollValues(0, 0);

    setAnnotationVisible(av.getShowAnnotation());

    hscroll.addAdjustmentListener(this);
    vscroll.addAdjustmentListener(this);

    final AlignmentPanel ap = this;
    av.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent evt)
      {
        if (evt.getPropertyName().equals("alignment"))
        {
          PaintRefresher.Refresh(ap,
                                 av.getSequenceSetId(),
                                 true,
                                 true);
          alignmentChanged();
        }
      }
    });

    fontChanged();
    adjustAnnotationHeight();

  }

  public void alignmentChanged()
  {
    av.alignmentChanged(this);

    alignFrame.updateEditMenuBar();

    paintAlignment(true);

  }

  /**
   * DOCUMENT ME!
   */
  public void fontChanged()
  {
    // set idCanvas bufferedImage to null
    // to prevent drawing old image
    FontMetrics fm = getFontMetrics(av.getFont());

    scalePanelHolder.setPreferredSize(new Dimension(10,
        av.charHeight + fm.getDescent()));
    idSpaceFillerPanel1.setPreferredSize(new Dimension(10,
        av.charHeight + fm.getDescent()));

    idPanel.idCanvas.gg = null;
    seqPanel.seqCanvas.img = null;
    annotationPanel.adjustPanelHeight();

    Dimension d = calculateIdWidth();
    d.setSize(d.width + 4, d.height);
    idPanel.idCanvas.setPreferredSize(d);
    hscrollFillerPanel.setPreferredSize(d);

    if (overviewPanel != null)
    {
      overviewPanel.setBoxPosition();
    }

    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Dimension calculateIdWidth()
  {
    Container c = new Container();

    FontMetrics fm = c.getFontMetrics(
        new Font(av.font.getName(), Font.ITALIC, av.font.getSize()));

    AlignmentI al = av.getAlignment();

    int i = 0;
    int idWidth = 0;
    String id;

    while ( (i < al.getHeight()) && (al.getSequenceAt(i) != null))
    {
      SequenceI s = al.getSequenceAt(i);

      id = s.getDisplayId(av.getShowJVSuffix());

      if (fm.stringWidth(id) > idWidth)
      {
        idWidth = fm.stringWidth(id);
      }

      i++;
    }

    // Also check annotation label widths
    i = 0;

    if (al.getAlignmentAnnotation() != null)
    {
      fm = c.getFontMetrics(alabels.getFont());

      while (i < al.getAlignmentAnnotation().length)
      {
        String label = al.getAlignmentAnnotation()[i].label;

        if (fm.stringWidth(label) > idWidth)
        {
          idWidth = fm.stringWidth(label);
        }

        i++;
      }
    }

    return new Dimension(idWidth, 12);
  }

  /**
   * DOCUMENT ME!
   *
   * @param results DOCUMENT ME!
   */
  public void highlightSearchResults(SearchResults results)
  {
    seqPanel.seqCanvas.highlightSearchResults(results);

    // do we need to scroll the panel?
    if (results != null)
    {
      SequenceI seq = results.getResultSequence(0);
      int seqIndex = av.alignment.findIndex(seq);
      int start = seq.findIndex(results.getResultStart(0)) - 1;
      int end = seq.findIndex(results.getResultEnd(0)) - 1;

      if (!av.wrapAlignment)
      {
        if ( (av.getStartRes() > end) || (av.getEndRes() < start) ||
            ( (av.getStartSeq() > seqIndex) || (av.getEndSeq() < seqIndex)))
        {
          setScrollValues(start, seqIndex);
        }
      }
      else
      {
        scrollToWrappedVisible(start);
      }
    }

    paintAlignment(true);
  }

  void scrollToWrappedVisible(int res)
  {
    int cwidth = seqPanel.seqCanvas.getWrappedCanvasWidth(seqPanel.seqCanvas.
        getWidth());
    if (res <= av.getStartRes() || res >= (av.getStartRes() + cwidth))
    {
      vscroll.setValue(res / cwidth);
      av.startRes = vscroll.getValue() * cwidth;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public OverviewPanel getOverviewPanel()
  {
    return overviewPanel;
  }

  /**
   * DOCUMENT ME!
   *
   * @param op DOCUMENT ME!
   */
  public void setOverviewPanel(OverviewPanel op)
  {
    overviewPanel = op;
  }

  /**
   * DOCUMENT ME!
   *
   * @param b DOCUMENT ME!
   */
  public void setAnnotationVisible(boolean b)
  {
    if (!av.wrapAlignment)
    {
      annotationSpaceFillerHolder.setVisible(b);
      annotationScroller.setVisible(b);
    }
    repaint();
  }

  public void adjustAnnotationHeight()
  {
    if (alignFrame.getHeight() == 0)
    {
      System.out.println("NEEDS FIXING");
    }

    int height = annotationPanel.adjustPanelHeight();

    if (hscroll.isVisible())
    {
      height += hscroll.getPreferredSize().height;
    }
    if (height > alignFrame.getHeight() / 2)
    {
      height = alignFrame.getHeight() / 2;
    }

    hscroll.addNotify();

    annotationScroller.setPreferredSize(
        new Dimension(annotationScroller.getWidth(), height));


    annotationSpaceFillerHolder.setPreferredSize(new Dimension(
        annotationSpaceFillerHolder.getWidth(),
        height));

    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param wrap DOCUMENT ME!
   */
  public void setWrapAlignment(boolean wrap)
  {
    av.startSeq = 0;
    scalePanelHolder.setVisible(!wrap);
    hscroll.setVisible(!wrap);
    idwidthAdjuster.setVisible(!wrap);

    if (wrap)
    {
      annotationScroller.setVisible(false);
      annotationSpaceFillerHolder.setVisible(false);
    }
    else if (av.showAnnotation)
    {
      annotationScroller.setVisible(true);
      annotationSpaceFillerHolder.setVisible(true);
    }

    idSpaceFillerPanel1.setVisible(!wrap);

    repaint();
  }

  // return value is true if the scroll is valid
  public boolean scrollUp(boolean up)
  {
    if (up)
    {
      if (vscroll.getValue() < 1)
      {
        return false;
      }

      fastPaint = false;
      vscroll.setValue(vscroll.getValue() - 1);
    }
    else
    {
      if ( (vextent + vscroll.getValue()) >= av.getAlignment().getHeight())
      {
        return false;
      }

      fastPaint = false;
      vscroll.setValue(vscroll.getValue() + 1);
    }

    fastPaint = true;

    return true;
  }

  /**
   * DOCUMENT ME!
   *
   * @param right DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean scrollRight(boolean right)
  {
    if (!right)
    {
      if (hscroll.getValue() < 1)
      {
        return false;
      }

      fastPaint = false;
      hscroll.setValue(hscroll.getValue() - 1);
    }
    else
    {
      if ( (hextent + hscroll.getValue()) >= av.getAlignment().getWidth())
      {
        return false;
      }

      fastPaint = false;
      hscroll.setValue(hscroll.getValue() + 1);
    }

    fastPaint = true;

    return true;
  }

  /**
   * DOCUMENT ME!
   *
   * @param x DOCUMENT ME!
   * @param y DOCUMENT ME!
   */
  public void setScrollValues(int x, int y)
  {

    int width = av.alignment.getWidth();
    int height = av.alignment.getHeight();

    if (av.hasHiddenColumns)
    {
      width = av.getColumnSelection().findColumnPosition(width);
    }

    av.setEndRes( (x + (seqPanel.seqCanvas.getWidth() / av.charWidth)) - 1);

    hextent = seqPanel.seqCanvas.getWidth() / av.charWidth;
    vextent = seqPanel.seqCanvas.getHeight() / av.charHeight;

    if (hextent > width)
    {
      hextent = width;
    }

    if (vextent > height)
    {
      vextent = height;
    }

    if ( (hextent + x) > width)
    {
      x = width - hextent;
    }

    if ( (vextent + y) > height)
    {
      y = height - vextent;
    }

    if (y < 0)
    {
      y = 0;
    }

    if (x < 0)
    {
      x = 0;
    }

    hscroll.setValues(x, hextent, 0, width);
    vscroll.setValues(y, vextent, 0, height);
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {

    int oldX = av.getStartRes();
    int oldY = av.getStartSeq();

    if (evt.getSource() == hscroll)
    {
      int x = hscroll.getValue();
      av.setStartRes(x);
      av.setEndRes( (x +
                     (seqPanel.seqCanvas.getWidth() / av.getCharWidth())) - 1);
    }

    if (evt.getSource() == vscroll)
    {
      int offy = vscroll.getValue();

      if (av.getWrapAlignment())
      {
        if (offy > -1)
        {
          int rowSize = seqPanel.seqCanvas.getWrappedCanvasWidth(seqPanel.
              seqCanvas.getWidth());
          av.setStartRes(offy * rowSize);
          av.setEndRes( (offy + 1) * rowSize);
        }
        else
        {
          //This is only called if file loaded is a jar file that
          //was wrapped when saved and user has wrap alignment true
          //as preference setting
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              setScrollValues(av.getStartRes(), av.getStartSeq());
            }
          });
        }
      }
      else
      {
        av.setStartSeq(offy);
        av.setEndSeq(offy +
                     (seqPanel.seqCanvas.getHeight() / av.getCharHeight()));
      }
    }

    if (overviewPanel != null)
    {
      overviewPanel.setBoxPosition();
    }

    int scrollX = av.startRes - oldX;
    int scrollY = av.startSeq - oldY;

    if (av.getWrapAlignment() || !fastPaint)
    {
      repaint();
    }
    else
    {
      // Make sure we're not trying to draw a panel
      // larger than the visible window
      if (scrollX > av.endRes - av.startRes)
      {
        scrollX = av.endRes - av.startRes;
      }
      else if (scrollX < av.startRes - av.endRes)
      {
        scrollX = av.startRes - av.endRes;
      }

      if (scrollX != 0 || scrollY != 0)
      {
        idPanel.idCanvas.fastPaint(scrollY);
        seqPanel.seqCanvas.fastPaint(scrollX,
                                     scrollY);
        scalePanel.repaint();

        if (av.getShowAnnotation())
        {
          annotationPanel.fastPaint(scrollX);
        }
      }
    }
  }

  public void paintAlignment(boolean updateOverview)
  {
    repaint();

    if(updateOverview)
    {
      jalview.structure.StructureSelectionManager.getStructureSelectionManager()
          .sequenceColoursChanged(this);

      if (overviewPanel != null)
      {
        overviewPanel.updateOverviewImage();
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   */
  public void paintComponent(Graphics g)
  {
    invalidate();

    Dimension d = idPanel.idCanvas.getPreferredSize();
    idPanelHolder.setPreferredSize(d);
    hscrollFillerPanel.setPreferredSize(new Dimension(d.width, 12));
    validate();

    if (av.getWrapAlignment())
    {
      int maxwidth = av.alignment.getWidth();

      if (av.hasHiddenColumns)
      {
        maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
      }

      int canvasWidth = seqPanel.seqCanvas.getWrappedCanvasWidth(seqPanel.
          seqCanvas.getWidth());
      if (canvasWidth > 0)
      {
        int max = maxwidth /
            seqPanel.seqCanvas.getWrappedCanvasWidth(seqPanel.seqCanvas.
            getWidth()) +
            1;
        vscroll.setMaximum(max);
        vscroll.setUnitIncrement(1);
        vscroll.setVisibleAmount(1);
      }
    }
    else
    {
      setScrollValues(av.getStartRes(), av.getStartSeq());
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param pg DOCUMENT ME!
   * @param pf DOCUMENT ME!
   * @param pi DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws PrinterException DOCUMENT ME!
   */
  public int print(Graphics pg, PageFormat pf, int pi)
      throws PrinterException
  {
    pg.translate( (int) pf.getImageableX(), (int) pf.getImageableY());

    int pwidth = (int) pf.getImageableWidth();
    int pheight = (int) pf.getImageableHeight();

    if (av.getWrapAlignment())
    {
      return printWrappedAlignment(pg, pwidth, pheight, pi);
    }
    else
    {
      return printUnwrapped(pg, pwidth, pheight, pi);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param pg DOCUMENT ME!
   * @param pwidth DOCUMENT ME!
   * @param pheight DOCUMENT ME!
   * @param pi DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws PrinterException DOCUMENT ME!
   */
  public int printUnwrapped(Graphics pg, int pwidth, int pheight, int pi)
      throws PrinterException
  {
    int idWidth = getVisibleIdWidth();
    FontMetrics fm = getFontMetrics(av.getFont());
    int scaleHeight = av.charHeight + fm.getDescent();

    pg.setColor(Color.white);
    pg.fillRect(0, 0, pwidth, pheight);
    pg.setFont(av.getFont());

    ////////////////////////////////////
    /// How many sequences and residues can we fit on a printable page?
    int totalRes = (pwidth - idWidth) / av.getCharWidth();

    int totalSeq = (int) ( (pheight - scaleHeight) / av.getCharHeight()) -
        1;

    int pagesWide = (av.getAlignment().getWidth() / totalRes) + 1;

    /////////////////////////////
    /// Only print these sequences and residues on this page
    int startRes;

    /////////////////////////////
    /// Only print these sequences and residues on this page
    int endRes;

    /////////////////////////////
    /// Only print these sequences and residues on this page
    int startSeq;

    /////////////////////////////
    /// Only print these sequences and residues on this page
    int endSeq;
    startRes = (pi % pagesWide) * totalRes;
    endRes = (startRes + totalRes) - 1;

    if (endRes > (av.getAlignment().getWidth() - 1))
    {
      endRes = av.getAlignment().getWidth() - 1;
    }

    startSeq = (pi / pagesWide) * totalSeq;
    endSeq = startSeq + totalSeq;

    if (endSeq > av.getAlignment().getHeight())
    {
      endSeq = av.getAlignment().getHeight();
    }

    int pagesHigh = ( (av.alignment.getHeight() / totalSeq) + 1) * pheight;

    if (av.showAnnotation)
    {
      pagesHigh += annotationPanel.adjustPanelHeight() + 3;
    }

    pagesHigh /= pheight;

    if (pi >= (pagesWide * pagesHigh))
    {
      return Printable.NO_SUCH_PAGE;
    }

    //draw Scale
    pg.translate(idWidth, 0);
    scalePanel.drawScale(pg, startRes, endRes, pwidth - idWidth, scaleHeight);
    pg.translate( -idWidth, scaleHeight);

    ////////////////
    // Draw the ids
    Color currentColor = null;
    Color currentTextColor = null;

    pg.setFont(idPanel.idCanvas.idfont);

    SequenceI seq;
    for (int i = startSeq; i < endSeq; i++)
    {
      seq = av.getAlignment().getSequenceAt(i);
      if ( (av.getSelectionGroup() != null) &&
          av.getSelectionGroup().getSequences(null).contains(seq))
      {
        currentColor = Color.gray;
        currentTextColor = Color.black;
      }
      else
      {
        currentColor = av.getSequenceColour(seq);
        currentTextColor = Color.black;
      }

      pg.setColor(currentColor);
      pg.fillRect(0, (i - startSeq) * av.charHeight, idWidth,
                  av.getCharHeight());

      pg.setColor(currentTextColor);

      int xPos = 0;
      if (av.rightAlignIds)
      {
        fm = pg.getFontMetrics();
        xPos = idWidth - fm.stringWidth(
            seq.getDisplayId(av.getShowJVSuffix())
            ) - 4;
      }

      pg.drawString(seq.getDisplayId(av.getShowJVSuffix()),
                    xPos,
                    ( ( (i - startSeq) * av.charHeight) + av.getCharHeight()) -
                    (av.getCharHeight() / 5));
    }

    pg.setFont(av.getFont());

    // draw main sequence panel
    pg.translate(idWidth, 0);
    seqPanel.seqCanvas.drawPanel(pg, startRes, endRes, startSeq, endSeq, 0);

    if (av.showAnnotation && (endSeq == av.alignment.getHeight()))
    {
      pg.translate( -idWidth - 3, (endSeq - startSeq) * av.charHeight + 3);
      alabels.drawComponent( (Graphics2D) pg, idWidth);
      pg.translate(idWidth + 3, 0);
      annotationPanel.drawComponent( (Graphics2D) pg, startRes, endRes +
                                    1);
    }

    return Printable.PAGE_EXISTS;
  }

  /**
   * DOCUMENT ME!
   *
   * @param pg DOCUMENT ME!
   * @param pwidth DOCUMENT ME!
   * @param pheight DOCUMENT ME!
   * @param pi DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   *
   * @throws PrinterException DOCUMENT ME!
   */
  public int printWrappedAlignment(Graphics pg, int pwidth, int pheight,
                                   int pi)
      throws PrinterException
  {

    int annotationHeight = 0;
    AnnotationLabels labels = null;
    if (av.showAnnotation)
    {
      annotationHeight = annotationPanel.adjustPanelHeight();
      labels = new AnnotationLabels(av);
    }

    int hgap = av.charHeight;
    if (av.scaleAboveWrapped)
    {
      hgap += av.charHeight;
    }

    int cHeight = av.getAlignment().getHeight() * av.charHeight
        + hgap
        + annotationHeight;

    int idWidth = getVisibleIdWidth();

    int maxwidth = av.alignment.getWidth();
    if (av.hasHiddenColumns)
    {
      maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
    }

    int resWidth = seqPanel.seqCanvas.getWrappedCanvasWidth(pwidth -
        idWidth);

    int totalHeight = cHeight * (maxwidth / resWidth + 1);

    pg.setColor(Color.white);
    pg.fillRect(0, 0, pwidth, pheight);
    pg.setFont(av.getFont());

    ////////////////
    // Draw the ids
    pg.setColor(Color.black);

    pg.translate(0, -pi * pheight);

    pg.setClip(0, pi * pheight, pwidth, pheight);

    int ypos = hgap;

    do
    {
      for (int i = 0; i < av.alignment.getHeight(); i++)
      {
        pg.setFont(idPanel.idCanvas.idfont);
        SequenceI s = av.alignment.getSequenceAt(i);
        String string = s.getDisplayId(av.getShowJVSuffix());
        int xPos = 0;
        if (av.rightAlignIds)
        {
          FontMetrics fm = pg.getFontMetrics();
          xPos = idWidth - fm.stringWidth(string) - 4;
        }
        pg.drawString(string, xPos,
                      ( (i * av.charHeight) + ypos + av.charHeight) -
                      (av.charHeight / 5));
      }
      if (labels != null)
      {
        pg.translate( -3,
                     ypos +
                     (av.getAlignment().getHeight() * av.charHeight));

        pg.setFont(av.getFont());
        labels.drawComponent(pg, idWidth);
        pg.translate( +3,
                     -ypos -
                     (av.getAlignment().getHeight() * av.charHeight));
      }

      ypos += cHeight;
    }
    while (ypos < totalHeight);

    pg.translate(idWidth, 0);

    seqPanel.seqCanvas.drawWrappedPanel(pg, pwidth - idWidth, totalHeight, 0);

    if ( (pi * pheight) < totalHeight)
    {
      return Printable.PAGE_EXISTS;

    }
    else
    {
      return Printable.NO_SUCH_PAGE;
    }
  }

  int getVisibleIdWidth()
  {
    return
        idPanel.getWidth() > 0 ? idPanel.getWidth() :
        calculateIdWidth().width + 4;
  }

  void makeAlignmentImage(int type, File file)
  {
    int maxwidth = av.alignment.getWidth();
    if (av.hasHiddenColumns)
    {
      maxwidth = av.getColumnSelection().findColumnPosition(maxwidth);
    }

    int height = ( (av.alignment.getHeight() + 1) * av.charHeight)
        + scalePanel.getHeight();
    int width = getVisibleIdWidth() + (maxwidth * av.charWidth);

    if (av.getWrapAlignment())
    {
      height = getWrappedHeight();
      if (System.getProperty("java.awt.headless") != null
          && System.getProperty("java.awt.headless").equals("true"))
      {
        width = alignFrame.getWidth()
            - vscroll.getPreferredSize().width
            - alignFrame.getInsets().left
            - alignFrame.getInsets().right;
      }
      else
      {
        width = seqPanel.getWidth() + getVisibleIdWidth();
      }

    }
    else if (av.getShowAnnotation())
    {
      height += annotationPanel.adjustPanelHeight() + 3;
    }

    try
    {

      jalview.util.ImageMaker im;
      if (type == jalview.util.ImageMaker.PNG)
      {
        im = new jalview.util.ImageMaker(this,
                                         jalview.util.ImageMaker.PNG,
                                         "Create PNG image from alignment",
                                         width, height, file, null);
      }
      else
      {
        im = new jalview.util.ImageMaker(this,
                                         jalview.util.ImageMaker.EPS,
                                         "Create EPS file from alignment",
                                         width, height, file,
                                         alignFrame.getTitle());
      }

      if (av.getWrapAlignment())
      {
        if (im.getGraphics() != null)
        {
          printWrappedAlignment(im.getGraphics(), width, height, 0);
          im.writeImage();
        }
      }
      else
      {
        if (im.getGraphics() != null)
        {
          printUnwrapped(im.getGraphics(), width, height, 0);
          im.writeImage();
        }
      }
    }
    catch (OutOfMemoryError err)
    {
      System.out.println("########################\n"
                         + "OUT OF MEMORY " + file + "\n"
                         + "########################");

      JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                            "Out of Memory Creating Image!!"
                                            +
                                            "\nSee help files for increasing Java Virtual Machine memory."
                                            , "Out of memory",
                                            JOptionPane.WARNING_MESSAGE);
      System.out.println("Create IMAGE: " + err);
      System.gc();

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void makeEPS(File epsFile)
  {
    makeAlignmentImage(jalview.util.ImageMaker.EPS, epsFile);
  }

  /**
   * DOCUMENT ME!
   */
  public void makePNG(File pngFile)
  {
    makeAlignmentImage(jalview.util.ImageMaker.PNG, pngFile);
  }

  public void makePNGImageMap(File imgMapFile, String imageName)
  {
    ///////ONLY WORKS WITH NONE WRAPPED ALIGNMENTS
    //////////////////////////////////////////////
    int idWidth = getVisibleIdWidth();
    FontMetrics fm = getFontMetrics(av.getFont());
    int scaleHeight = av.charHeight + fm.getDescent();

    // Gen image map
    //////////////////////////////////
    if (imgMapFile != null)
    {
      try
      {
        int s, sSize = av.alignment.getHeight(),
            res, alwidth = av.alignment.getWidth(), g, gSize, f, fSize, sy;
        StringBuffer text = new StringBuffer();
        PrintWriter out = new PrintWriter(new FileWriter(imgMapFile));
        out.println(jalview.io.HTMLOutput.getImageMapHTML());
        out.println("<img src=\"" + imageName +
                    "\" border=\"0\" usemap=\"#Map\" >"
                    + "<map name=\"Map\">");

        for (s = 0; s < sSize; s++)
        {
          sy = s * av.charHeight + scaleHeight;

          SequenceI seq = av.alignment.getSequenceAt(s);
          SequenceFeature[] features = seq.getDatasetSequence().
              getSequenceFeatures();
          SequenceGroup[] groups = av.alignment.findAllGroups(seq);
          for (res = 0; res < alwidth; res++)
          {
            text = new StringBuffer();
            Object obj = null;
            if (av.alignment.isNucleotide())
            {
              obj = ResidueProperties.nucleotideName.get(seq.getCharAt(res) +
                  "");
            }
            else
            {
              obj = ResidueProperties.aa2Triplet.get(
                  seq.getCharAt(res) + "");
            }

            if (obj == null)
            {
              continue;
            }

            String triplet = obj.toString();
            int alIndex = seq.findPosition(res);
            gSize = groups.length;
            for (g = 0; g < gSize; g++)
            {
              if (text.length() < 1)
              {
                text.append("<area shape=\"rect\" coords=\""
                            + (idWidth + res * av.charWidth) + ","
                            + sy + ","
                            + (idWidth + (res + 1) * av.charWidth) + ","
                            + (av.charHeight + sy) + "\""
                            + " onMouseOver=\"toolTip('"
                            + alIndex + " " + triplet);
              }

              if (groups[g].getStartRes() < res && groups[g].getEndRes() > res)
              {
                text.append("<br><em>" + groups[g].getName() + "</em>");
              }
            }

            if (features != null)
            {
              if (text.length() < 1)
              {
                text.append("<area shape=\"rect\" coords=\""
                            + (idWidth + res * av.charWidth) + ","
                            + sy + ","
                            + (idWidth + (res + 1) * av.charWidth) + ","
                            + (av.charHeight + sy) + "\""
                            + " onMouseOver=\"toolTip('"
                            + alIndex + " " + triplet);
              }
              fSize = features.length;
              for (f = 0; f < fSize; f++)
              {

                if ( (features[f].getBegin() <= seq.findPosition(res)) &&
                    (features[f].getEnd() >= seq.findPosition(res)))
                {
                  if (features[f].getType().equals("disulfide bond"))
                  {
                    if (features[f].getBegin() == seq.findPosition(res)
                        || features[f].getEnd() == seq.findPosition(res))
                    {
                      text.append("<br>disulfide bond " + features[f].getBegin() +
                                  ":" +
                                  features[f].getEnd());
                    }
                  }
                  else
                  {
                    text.append("<br>");
                    text.append(features[f].getType());
                    if (features[f].getDescription() != null &&
                        !features[f].
                        getType().equals(features[f].getDescription()))
                    {
                      text.append(" " + features[f].getDescription());
                    }

                    if (features[f].getValue("status") != null)
                    {
                      text.append(" (" + features[f].getValue("status") + ")");
                    }
                  }
                }

              }
            }
            if (text.length() > 1)
            {
              text.append("')\"; onMouseOut=\"toolTip()\";  href=\"#\">");
              out.println(text.toString());
            }
          }
        }
        out.println("</map></body></html>");
        out.close();

      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    } ///////////END OF IMAGE MAP

  }

  int getWrappedHeight()
  {
    int seqPanelWidth = seqPanel.seqCanvas.getWidth();

    if (System.getProperty("java.awt.headless") != null
        && System.getProperty("java.awt.headless").equals("true"))
    {
      seqPanelWidth = alignFrame.getWidth()
          - getVisibleIdWidth()
          - vscroll.getPreferredSize().width
          - alignFrame.getInsets().left
          - alignFrame.getInsets().right;
    }

    int chunkWidth = seqPanel.seqCanvas.getWrappedCanvasWidth(
        seqPanelWidth
        );

    int hgap = av.charHeight;
    if (av.scaleAboveWrapped)
    {
      hgap += av.charHeight;
    }

    int annotationHeight = 0;
    if (av.showAnnotation)
    {
      annotationHeight = annotationPanel.adjustPanelHeight();
    }

    int cHeight = av.getAlignment().getHeight() * av.charHeight
        + hgap
        + annotationHeight;

    int maxwidth = av.alignment.getWidth();
    if (av.hasHiddenColumns)
    {
      maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
    }

    int height = ( (maxwidth / chunkWidth) + 1) * cHeight;

    return height;
  }
}
