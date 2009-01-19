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

public class AnnotationLabels
    extends Panel implements ActionListener, MouseListener, MouseMotionListener
{
  Image image;
  boolean active = false;
  AlignmentPanel ap;
  AlignViewport av;
  boolean resizing = false;
  int oldY, mouseX;

  static String ADDNEW = "Add New Row";
  static String EDITNAME = "Edit Label/Description";
  static String HIDE = "Hide This Row";
  static String SHOWALL = "Show All Hidden Rows";
  static String OUTPUT_TEXT = "Show Values In Textbox";
  static String COPYCONS_SEQ = "Copy Consensus Sequence";

  int scrollOffset = 0;
  int selectedRow = -1;

  Tooltip tooltip;

  public AnnotationLabels(AlignmentPanel ap)
  {
    this.ap = ap;
    this.av = ap.av;
    setLayout(null);
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  public AnnotationLabels(AlignViewport av)
  {
    this.av = av;
  }

  public void setScrollOffset(int y)
  {
    scrollOffset = y;
    repaint();
  }

  int getSelectedRow(int y)
  {
    int row = -1;
    AlignmentAnnotation[] aa = ap.av.alignment.getAlignmentAnnotation();

    if (aa == null)
    {
      return row;
    }

    int height = 0;
    for (int i = 0; i < aa.length; i++)
    {
      if (!aa[i].visible)
      {
        continue;
      }

      height += aa[i].height;
      if (y < height)
      {
        row = i;
        break;
      }
    }

    return row;
  }

  public void actionPerformed(ActionEvent evt)
  {
    AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();

    if (evt.getActionCommand().equals(ADDNEW))
    {
      AlignmentAnnotation newAnnotation = new AlignmentAnnotation("",
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
    }
    else if (evt.getActionCommand().equals(HIDE))
    {
      aa[selectedRow].visible = false;
    }
    else if (evt.getActionCommand().equals(SHOWALL))
    {
      for (int i = 0; i < aa.length; i++)
      {
        aa[i].visible = (aa[i].annotations==null) ? false : true;
      }
    }
    else if (evt.getActionCommand().equals(OUTPUT_TEXT))
    {
      CutAndPasteTransfer cap = new CutAndPasteTransfer(false, ap.alignFrame);
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame,
                                       ap.alignFrame.getTitle() + " - " +
                                       aa[selectedRow].label, 500, 100);
      cap.setText(aa[selectedRow].toString());
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
    setSize(getSize().width, ap.annotationPanel.getSize().height);
    ap.validate();
    ap.paintAlignment(true);
  }

  boolean editLabelDescription(AlignmentAnnotation annotation)
  {
    Checkbox padGaps = new Checkbox("Fill Empty Gaps With \""
                                    +ap.av.getGapCharacter()+"\"",
                                    annotation.padGaps);

    EditNameDialog dialog = new EditNameDialog(
        annotation.label,
        annotation.description,
        "      Annotation Label",
        "Annotation Description",
        ap.alignFrame,
        "Edit Annotation Name / Description",
        500, 180, false);

    Panel empty = new Panel(new FlowLayout());
    empty.add(padGaps);
    dialog.add(empty);
    dialog.pack();

    dialog.setVisible(true);

    if (dialog.accept)
    {
      annotation.label = dialog.getName();
      annotation.description = dialog.getDescription();
      annotation.setPadGaps(padGaps.getState(), av.getGapCharacter());
      repaint();
      return true;
    }
    else
      return false;

  }

  public void mouseMoved(MouseEvent evt)
  {
    int row = getSelectedRow(evt.getY() - scrollOffset);

    if (row > -1)
    {
      if (tooltip == null)
      {
        tooltip = new Tooltip(ap.av.alignment.
                              getAlignmentAnnotation()[row].
                              description,
                              this);
      }
      else
      {
        tooltip.setTip(ap.av.alignment.
                       getAlignmentAnnotation()[row].description);
      }
    }
    else if (tooltip != null)
    {
      tooltip.setTip("");
    }

  }

  public void mouseDragged(MouseEvent evt)
  {}

  public void mouseClicked(MouseEvent evt)
  {}

  public void mouseReleased(MouseEvent evt)
  {}

  public void mouseEntered(MouseEvent evt)
  {}

  public void mouseExited(MouseEvent evt)
  {}

  public void mousePressed(MouseEvent evt)
  {
    selectedRow = getSelectedRow(evt.getY() - scrollOffset);

    AlignmentAnnotation[] aa = ap.av.alignment.getAlignmentAnnotation();

    PopupMenu popup = new PopupMenu("Annotations");

    MenuItem item = new MenuItem(ADDNEW);
    item.addActionListener(this);
    popup.add(item);
    item = new MenuItem(EDITNAME);
    item.addActionListener(this);
    popup.add(item);
    item = new MenuItem(HIDE);
    item.addActionListener(this);
    popup.add(item);
    item = new MenuItem(SHOWALL);
    item.addActionListener(this);
    popup.add(item);
    this.add(popup);
    item = new MenuItem(OUTPUT_TEXT);
    item.addActionListener(this);
    popup.add(item);

    if (aa[selectedRow] == ap.av.consensus)
    {
      popup.addSeparator();
      final CheckboxMenuItem cbmi = new CheckboxMenuItem(
          "Ignore Gaps In Consensus",
          ap.av.getIgnoreGapsConsensus());

      cbmi.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          ap.av.setIgnoreGapsConsensus(cbmi.getState());
          ap.paintAlignment(true);
        }
      });
      popup.add(cbmi);
      item = new MenuItem(COPYCONS_SEQ);
      item.addActionListener(this);
      popup.add(item);
    }

    popup.show(this, evt.getX(), evt.getY());

  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void copy_annotseqtoclipboard(SequenceI sq)
  {
    if (sq == null || sq.getLength() < 1)
    {
      return;
    }
    jalview.appletgui.AlignFrame.copiedSequences = new StringBuffer();
    jalview.appletgui.AlignFrame.copiedSequences.append(sq.getName() + "\t" +
        sq.getStart() + "\t" +
        sq.getEnd() + "\t" +
        sq.getSequenceAsString() + "\n");
    if (av.hasHiddenColumns)
    {
      jalview.appletgui.AlignFrame.copiedHiddenColumns = new Vector();
      for (int i = 0; i < av.getColumnSelection().getHiddenColumns().size(); i++)
      {
        int[] region = (int[])
            av.getColumnSelection().getHiddenColumns().elementAt(i);

        jalview.appletgui.AlignFrame.copiedHiddenColumns.addElement(new int[]
            {region[0],
            region[1]});
      }
    }
  }

  public void update(Graphics g)
  {
    paint(g);
  }

  public void paint(Graphics g)
  {
    int w = getSize().width;
    if (image == null || w != image.getWidth(this))
    {
      image = createImage(w, ap.annotationPanel.getSize().height);
    }

    drawComponent(image.getGraphics(), w);
    g.drawImage(image, 0, 0, this);
  }

  public void drawComponent(Graphics g, int width)
  {
    g.setFont(av.getFont());
    FontMetrics fm = g.getFontMetrics(av.getFont());
    g.setColor(Color.white);
    g.fillRect(0, 0, getSize().width, getSize().height);

    g.translate(0, scrollOffset);
    g.setColor(Color.black);

    AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();
    int y = g.getFont().getSize();
    int x = 0;

    if (aa != null)
    {
      for (int i = 0; i < aa.length; i++)
      {
        if (!aa[i].visible)
        {
          continue;
        }

        x = width - fm.stringWidth(aa[i].label) - 3;

        if (aa[i].graph > 0)
        {
          y += (aa[i].height / 3);
        }

        g.drawString(aa[i].label, x, y);

        if (aa[i].graph > 0)
        {
          y += (2 * aa[i].height / 3);
        }
        else
        {
          y += aa[i].height;
        }
      }
    }
  }

}
