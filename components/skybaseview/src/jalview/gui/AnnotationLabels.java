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

import java.util.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import jalview.datamodel.*;
import jalview.io.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class AnnotationLabels
    extends JPanel implements MouseListener,
    MouseMotionListener, ActionListener
{
  static String ADDNEW = "Add New Row";
  static String EDITNAME = "Edit Label/Description";
  static String HIDE = "Hide This Row";
  static String DELETE = "Delete This Row";
  static String SHOWALL = "Show All Hidden Rows";
  static String OUTPUT_TEXT = "Export Annotation";
  static String COPYCONS_SEQ = "Copy Consensus Sequence";
  boolean resizePanel = false;
  Image image;
  AlignmentPanel ap;
  AlignViewport av;
  boolean resizing = false;
  MouseEvent dragEvent;
  int oldY;
  int selectedRow;
  int scrollOffset = 0;
  Font font = new Font("Arial", Font.PLAIN, 11);

  /**
   * Creates a new AnnotationLabels object.
   *
   * @param ap DOCUMENT ME!
   */
  public AnnotationLabels(AlignmentPanel ap)
  {
    this.ap = ap;
    av = ap.av;
    ToolTipManager.sharedInstance().registerComponent(this);

    java.net.URL url = getClass().getResource("/jalview/gui/images/idwidth.gif");
    Image temp = null;

    if (url != null)
    {
      temp = java.awt.Toolkit.getDefaultToolkit().createImage(url);
    }

    try
    {
      MediaTracker mt = new MediaTracker(this);
      mt.addImage(temp, 0);
      mt.waitForID(0);
    }
    catch (Exception ex)
    {
    }

    BufferedImage bi = new BufferedImage(temp.getHeight(this),
                                         temp.getWidth(this),
                                         BufferedImage.TYPE_INT_RGB);
    Graphics2D g = (Graphics2D) bi.getGraphics();
    g.rotate(Math.toRadians(90));
    g.drawImage(temp, 0, -bi.getWidth(this), this);
    image = (Image) bi;

    addMouseListener(this);
    addMouseMotionListener(this);
  }

  public AnnotationLabels(AlignViewport av)
  {
    this.av = av;
  }

  /**
   * DOCUMENT ME!
   *
   * @param y DOCUMENT ME!
   */
  public void setScrollOffset(int y)
  {
    scrollOffset = y;
    repaint();
  }

  void getSelectedRow(int y)
  {
    int height = 0;
    AlignmentAnnotation[] aa = ap.av.alignment.getAlignmentAnnotation();

    if (aa != null)
    {
      for (int i = 0; i < aa.length; i++)
      {
        if (!aa[i].visible)
        {
          continue;
        }

        height += aa[i].height;

        if (y < height)
        {
          selectedRow = i;

          break;
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void actionPerformed(ActionEvent evt)
  {
    AlignmentAnnotation[] aa = ap.av.alignment.getAlignmentAnnotation();

    if (evt.getActionCommand().equals(ADDNEW))
    {
      AlignmentAnnotation newAnnotation = new AlignmentAnnotation(null,
          null,
          new Annotation[ap.av.alignment.getWidth()]);

      if (!editLabelDescription(newAnnotation))
      {
        return;
      }

      ap.av.alignment.addAnnotation(newAnnotation);
      ap.av.alignment.setAnnotationIndex(newAnnotation, 0);
    }
    else if (evt.getActionCommand().equals(EDITNAME))
    {
      editLabelDescription(aa[selectedRow]);
      repaint();
    }
    else if (evt.getActionCommand().equals(HIDE))
    {
      aa[selectedRow].visible = false;

      if (aa[selectedRow].label.equals("Quality"))
      {
        ap.av.quality = null;
      }
    }
    else if (evt.getActionCommand().equals(DELETE))
    {
      ap.av.alignment.deleteAnnotation(aa[selectedRow]);
    }
    else if (evt.getActionCommand().equals(SHOWALL))
    {
      for (int i = 0; i < aa.length; i++)
      {
        if (!aa[i].visible && aa[i].annotations!=null)
        {
          aa[i].visible = true;
        }
      }
    }
    else if (evt.getActionCommand().equals(OUTPUT_TEXT))
    {
      new AnnotationExporter().exportAnnotations(
          ap,
          new AlignmentAnnotation[]
          {aa[selectedRow]},
          null, null
          );
    }
    else if (evt.getActionCommand().equals(COPYCONS_SEQ))
    {
      SequenceI cons = av.getConsensusSeq();
      if (cons != null)
      {
        copy_annotseqtoclipboard(cons);
      }

    }

    ap.annotationPanel.adjustPanelHeight();
    ap.annotationScroller.validate();
    ap.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  boolean editLabelDescription(AlignmentAnnotation annotation)
  {
    EditNameDialog dialog = new EditNameDialog(annotation.label,
                                               annotation.description,
                                               "       Annotation Name ",
                                               "Annotation Description ",
                                               "Edit Annotation Name/Description");

    if (!dialog.accept)
    {
      return false;
    }

    annotation.label = dialog.getName();

    String text = dialog.getDescription();
    if (text!=null && text.length() == 0)
    {
      text = null;
    }
    annotation.description = text;

    return true;
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mousePressed(MouseEvent evt)
  {
    getSelectedRow(evt.getY() - scrollOffset);
    oldY = evt.getY();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseReleased(MouseEvent evt)
  {
    int start = selectedRow;
    getSelectedRow(evt.getY() - scrollOffset);
    int end = selectedRow;

    if (start != end)
    {
      //Swap these annotations
      AlignmentAnnotation startAA = ap.av.alignment.getAlignmentAnnotation()[
          start];
      AlignmentAnnotation endAA = ap.av.alignment.getAlignmentAnnotation()[end];

      ap.av.alignment.getAlignmentAnnotation()[end] = startAA;
      ap.av.alignment.getAlignmentAnnotation()[start] = endAA;
    }

    resizePanel = false;
    dragEvent = null;
    repaint();
    ap.annotationPanel.repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseEntered(MouseEvent evt)
  {
    if (evt.getY() < 10)
    {
      resizePanel = true;
      repaint();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseExited(MouseEvent evt)
  {
    if (dragEvent == null)
    {
      resizePanel = false;
      repaint();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseDragged(MouseEvent evt)
  {
    dragEvent = evt;

    if (resizePanel)
    {
      Dimension d = ap.annotationScroller.getPreferredSize();
      int dif = evt.getY() - oldY;

      dif /= ap.av.charHeight;
      dif *= ap.av.charHeight;

      if ( (d.height - dif) > 20)
      {
        ap.annotationScroller.setPreferredSize(new Dimension(d.width,
            d.height - dif));
        d = ap.annotationSpaceFillerHolder.getPreferredSize();
        ap.annotationSpaceFillerHolder.setPreferredSize(new Dimension(
            d.width, d.height - dif));
        ap.paintAlignment(true);
      }

      ap.addNotify();
    }
    else
    {
      repaint();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseMoved(MouseEvent evt)
  {
    resizePanel = evt.getY() < 10;

    getSelectedRow(evt.getY() - scrollOffset);


    if (selectedRow > -1
        && ap.av.alignment.getAlignmentAnnotation().length > selectedRow)
    {
      AlignmentAnnotation aa = ap.av.alignment.
          getAlignmentAnnotation()[selectedRow];

      StringBuffer desc = new StringBuffer("<html>");

      if (aa.description != null && !aa.description.equals("New description"))
      {
        desc.append(aa.description);
        if(aa.hasScore)
          desc.append("<br>");
      }
      if(aa.hasScore())
      {
        desc.append("Score: "+aa.score);
      }

      if(desc.length()!=6)
      {
        desc.append("</html>");
        this.setToolTipText(desc.toString());
      }
      else
        this.setToolTipText(null);
    }

  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseClicked(MouseEvent evt)
  {
    if (!SwingUtilities.isRightMouseButton(evt))
    {
      return;
    }

    AlignmentAnnotation[] aa = ap.av.alignment.getAlignmentAnnotation();

    JPopupMenu pop = new JPopupMenu("Annotations");
    JMenuItem item = new JMenuItem(ADDNEW);
    item.addActionListener(this);

    if ( (aa == null) || (aa.length == 0))
    {
      item = new JMenuItem(SHOWALL);
      item.addActionListener(this);
      pop.add(item);
      pop.show(this, evt.getX(), evt.getY());
      return;
    }

    pop.add(item);
    item = new JMenuItem(EDITNAME);
    item.addActionListener(this);
    pop.add(item);
    item = new JMenuItem(HIDE);
    item.addActionListener(this);
    pop.add(item);
    item = new JMenuItem(DELETE);
    item.addActionListener(this);
    pop.add(item);
    item = new JMenuItem(SHOWALL);
    item.addActionListener(this);
    pop.add(item);
    item = new JMenuItem(OUTPUT_TEXT);
    item.addActionListener(this);
    pop.add(item);
    // annotation object should be typed
    if (selectedRow<aa.length && aa[selectedRow] == ap.av.consensus)
    {
      pop.addSeparator();
      final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(
          "Ignore Gaps In Consensus",
          ap.av.getIgnoreGapsConsensus());
      cbmi.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          ap.av.setIgnoreGapsConsensus(cbmi.getState(), ap);
        }
      });
      pop.add(cbmi);
      final JMenuItem consclipbrd = new JMenuItem(COPYCONS_SEQ);
      consclipbrd.addActionListener(this);
      pop.add(consclipbrd);
    }

    pop.show(this, evt.getX(), evt.getY());
  }

  /**
   * do a single sequence copy to jalview and the system clipboard
   *
   * @param sq sequence to be copied to clipboard
   */
  protected void copy_annotseqtoclipboard(SequenceI sq)
  {
    SequenceI[] seqs = new SequenceI[]
        {
        sq};
    String[] omitHidden = null;
    SequenceI[] dseqs = new SequenceI[]
        {
        sq.getDatasetSequence()};
    if (dseqs[0] == null)
    {
      dseqs[0] = new Sequence(sq);
      dseqs[0].setSequence(
          jalview.analysis.AlignSeq.extractGaps(
              jalview.util.Comparison.GapChars,
              sq.getSequenceAsString()));

      sq.setDatasetSequence(dseqs[0]);
    }
    Alignment ds = new Alignment(dseqs);
    if (av.hasHiddenColumns)
    {
      omitHidden = av.getColumnSelection().getVisibleSequenceStrings(0,
          sq.getLength(), seqs);
    }

    String output = new FormatAdapter().formatSequences(
        "Fasta",
        seqs,
        omitHidden);

    Toolkit.getDefaultToolkit().getSystemClipboard()
        .setContents(new StringSelection(output), Desktop.instance);

    Vector hiddenColumns = null;
    if (av.hasHiddenColumns)
    {
      hiddenColumns = new Vector();
      for (int i = 0; i < av.getColumnSelection().getHiddenColumns().size(); i++)
      {
        int[] region = (int[])
            av.getColumnSelection().getHiddenColumns().elementAt(i);

        hiddenColumns.addElement(new int[]
                                 {region[0],
                                 region[1]});
      }
    }

    Desktop.jalviewClipboard = new Object[]
        {
        seqs,
        ds, // what is the dataset of a consensus sequence ? need to flag sequence as special.
        hiddenColumns};
  }

  /**
   * DOCUMENT ME!
   *
   * @param g1 DOCUMENT ME!
   */
  public void paintComponent(Graphics g)
  {

    int width = getWidth();
    if (width == 0)
    {
      width = ap.calculateIdWidth().width + 4;
    }

    Graphics2D g2 = (Graphics2D) g;
    if (av.antiAlias)
    {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);
    }

    drawComponent(g2, width);

  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   */
  public void drawComponent(Graphics g, int width)
  {
    if (av.getFont().getSize() < 10)
    {
      g.setFont(font);
    }
    else
    {
      g.setFont(av.getFont());
    }

    FontMetrics fm = g.getFontMetrics(g.getFont());
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());

    g.translate(0, scrollOffset);
    g.setColor(Color.black);

    AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();
    int fontHeight = g.getFont().getSize();
    int y = 0;
    int x = 0;
    int graphExtras = 0;
    int offset =0;

    if (aa != null)
    {
      for (int i = 0; i < aa.length; i++)
      {
        g.setColor(Color.black);

        if (!aa[i].visible)
        {
          continue;
        }


        y += aa[i].height;

        offset = -aa[i].height/2;

        if(aa[i].hasText)
        {
          offset += fm.getHeight()/2;
          offset -= fm.getDescent();
        }
        else
          offset += fm.getDescent();

        x = width - fm.stringWidth(aa[i].label) - 3;

        if (aa[i].graphGroup > -1)
        {
          int groupSize = 0;
          for (int gg = 0; gg < aa.length; gg++)
          {
            if (aa[gg].graphGroup == aa[i].graphGroup)
            {
              groupSize++;
            }
          }

          if (groupSize * (fontHeight + 8) < aa[i].height)
          {
            graphExtras = (aa[i].height - (groupSize * (fontHeight + 8))) / 2;
          }

          for (int gg = 0; gg < aa.length; gg++)
          {
            if (aa[gg].graphGroup == aa[i].graphGroup)
            {
              x = width - fm.stringWidth(aa[gg].label) - 3;
              g.drawString(aa[gg].label, x,y - graphExtras);
              if (aa[gg].annotations[0] != null)
              {
                g.setColor(aa[gg].annotations[0].colour);
              }

              g.drawLine(x, y - graphExtras - 3,
                         x + fm.stringWidth(aa[gg].label),
                         y - graphExtras - 3);

              g.setColor(Color.black);
              graphExtras += fontHeight + 8;
            }
          }
        }
        else
        {
          g.drawString(aa[i].label, x, y +offset);
        }
      }
    }

    if (resizePanel)
    {
      g.drawImage(image, 2, 0 - scrollOffset, this);
    }
    else if (dragEvent != null && aa != null)
    {
      g.setColor(Color.lightGray);
      g.drawString(aa[selectedRow].label, dragEvent.getX(),
                   dragEvent.getY() - scrollOffset);
    }

    if ( (aa == null) || (aa.length < 1))
    {
      g.drawString("Right click", 2, 8);
      g.drawString("to add annotation", 2, 18);
    }
  }
}
