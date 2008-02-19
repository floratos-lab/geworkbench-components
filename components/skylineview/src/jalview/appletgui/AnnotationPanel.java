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

public class AnnotationPanel
    extends Panel
    implements AdjustmentListener, ActionListener, MouseListener, MouseMotionListener
{
  AlignViewport av;
  AlignmentPanel ap;
  int activeRow = -1;

  Vector activeRes;
  static String HELIX = "Helix";
  static String SHEET = "Sheet";
  static String LABEL = "Label";
  static String REMOVE = "Remove Annotation";
  static String COLOUR = "Colour";
  static Color HELIX_COLOUR = Color.red.darker();
  static Color SHEET_COLOUR = Color.green.darker().darker();

  Image image;
  Graphics gg;
  FontMetrics fm;
  int imgWidth = 0;

  boolean fastPaint = false;

  public static int GRAPH_HEIGHT = 40;

  boolean MAC = false;

  public AnnotationPanel(AlignmentPanel ap)
  {
    if (System.getProperty("os.name").startsWith("Mac"))
    {
      MAC = true;
    }

    this.ap = ap;
    av = ap.av;
    setLayout(null);
    adjustPanelHeight();

    addMouseMotionListener(this);

    addMouseListener(this);


    // ap.annotationScroller.getVAdjustable().addAdjustmentListener( this );
  }

  public AnnotationPanel(AlignViewport av)
  {
    this.av = av;
  }

  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    ap.alabels.setScrollOffset( -evt.getValue());
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void actionPerformed(ActionEvent evt)
  {
    AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();
    Annotation[] anot = aa[activeRow].annotations;

    if (anot.length < av.getColumnSelection().getMax())
    {
      Annotation[] temp = new Annotation[av.getColumnSelection().getMax() + 2];
      System.arraycopy(anot, 0, temp, 0, anot.length);
      anot = temp;
      aa[activeRow].annotations = anot;
    }

    String label = "";
    if (av.colSel != null && av.colSel.size() > 0
        && anot[av.colSel.getMin()] != null)
      label = anot[av.getColumnSelection().getMin()].displayCharacter;


    if (evt.getActionCommand().equals(REMOVE))
    {
      for (int i = 0; i < av.getColumnSelection().size(); i++)
      {
        anot[av.getColumnSelection().columnAt(i)] = null;
      }
    }
    else if (evt.getActionCommand().equals(LABEL))
    {
      label = enterLabel(label, "Enter Label");

      if (label == null)
      {
        return;
      }

      if ( (label.length() > 0) && !aa[activeRow].hasText)
      {
        aa[activeRow].hasText = true;
      }

      for (int i = 0; i < av.getColumnSelection().size(); i++)
      {
        int index = av.getColumnSelection().columnAt(i);

        if(!av.colSel.isVisible(index))
          continue;

        if (anot[index] == null)
        {
          anot[index] = new Annotation(label, "", ' ', 0);
        }

        anot[index].displayCharacter = label;
      }
    }
    else if (evt.getActionCommand().equals(COLOUR))
    {
      UserDefinedColours udc = new UserDefinedColours(
          this,
         Color.black, ap.alignFrame);

      Color col = udc.getColor();

      for (int i = 0; i < av.getColumnSelection().size(); i++)
      {
        int index = av.getColumnSelection().columnAt(i);

        if(!av.colSel.isVisible(index))
          continue;

        if (anot[index] == null)
        {
          anot[index] = new Annotation("", "", ' ', 0);
        }

        anot[index].colour = col;
      }
    }
    else // HELIX OR SHEET
    {
      char type = 0;
      String symbol = "\u03B1";

      if (evt.getActionCommand().equals(HELIX))
      {
        type = 'H';
      }
      else if (evt.getActionCommand().equals(SHEET))
      {
        type = 'E';
        symbol = "\u03B2";
      }

      if (!aa[activeRow].hasIcons)
      {
        aa[activeRow].hasIcons = true;
      }

      label = enterLabel(symbol, "Enter Label");

      if (label == null)
      {
        return;
      }

      if ( (label.length() > 0) && !aa[activeRow].hasText)
      {
        aa[activeRow].hasText = true;
      }

      for (int i = 0; i < av.getColumnSelection().size(); i++)
      {
        int index = av.getColumnSelection().columnAt(i);

        if(!av.colSel.isVisible(index))
          continue;

        if (anot[index] == null)
        {
          anot[index] = new Annotation(label, "", type, 0);
        }

        anot[index].secondaryStructure = type;
        anot[index].displayCharacter = label;
      }
    }

    adjustPanelHeight();
    repaint();

    return;
  }

  String enterLabel(String text, String label)
  {
    EditNameDialog dialog = new EditNameDialog(text,null,label,null,
        ap.alignFrame,"Enter Label", 400,200, true);

    if(dialog.accept)
      return dialog.getName();
    else
      return null;
  }

  public void mousePressed(MouseEvent evt)
  {
      AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();
      if (aa == null)
      {
        return;
      }

      int height = 0;
      activeRow = -1;


      for (int i = 0; i < aa.length; i++)
      {
        if (aa[i].visible)
        {
          height += aa[i].height;
        }

        if (evt.getY() < height)
        {
          if (aa[i].editable)
          {
            activeRow = i;
          }

          break;
        }
      }

      if ( (evt.getModifiers() & InputEvent.BUTTON3_MASK) ==
        InputEvent.BUTTON3_MASK  && activeRow != -1)
      {
        if (av.getColumnSelection() == null)
        {
          return;
        }

        PopupMenu pop = new PopupMenu("Structure type");
        MenuItem item = new MenuItem(HELIX);
        item.addActionListener(this);
        pop.add(item);
        item = new MenuItem(SHEET);
        item.addActionListener(this);
        pop.add(item);
        item = new MenuItem(LABEL);
        item.addActionListener(this);
        pop.add(item);
        item = new MenuItem(COLOUR);
        item.addActionListener(this);
        pop.add(item);
        item = new MenuItem(REMOVE);
        item.addActionListener(this);
        pop.add(item);
        ap.alignFrame.add(pop);
        pop.show(this, evt.getX(), evt.getY());

        return;
      }

      if (aa == null)
      {
        return;
      }

      ap.scalePanel.mousePressed(evt);
  }

  public void mouseReleased(MouseEvent evt)
  {
    ap.scalePanel.mouseReleased(evt);
  }

  public void mouseClicked(MouseEvent evt)
  {}

  public void mouseDragged(MouseEvent evt)
  {
    ap.scalePanel.mouseDragged(evt);
  }

  public void mouseMoved(MouseEvent evt)
  {
    AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();
    if (aa == null)
    {
      return;
    }

    int row = -1;
    int height = 0;
    for (int i = 0; i < aa.length; i++)
    {

      if (aa[i].visible)
      {
        height += aa[i].height;
      }

      if (evt.getY() < height)
      {
        row = i;
        break;
      }
    }

    int res = evt.getX() / av.getCharWidth() + av.getStartRes();

    if (av.hasHiddenColumns)
    {
      res = av.getColumnSelection().adjustForHiddenColumns(res);
    }

    if (row > -1 && res < aa[row].annotations.length && aa[row].annotations[res] != null)
    {
      StringBuffer text = new StringBuffer("Sequence position " + (res + 1));
      if (aa[row].annotations[res].description != null)
      {
        text.append("  " + aa[row].annotations[res].description);
      }
      ap.alignFrame.statusBar.setText(text.toString());
    }
  }
  public void mouseEntered(MouseEvent evt)
  {
    ap.scalePanel.mouseEntered(evt);
  }
  public void mouseExited(MouseEvent evt)
  {
    ap.scalePanel.mouseExited(evt);
  }


  public int adjustPanelHeight()
  {
    // setHeight of panels
    AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();
    int height = 0;

    if (aa != null)
    {
      for (int i = 0; i < aa.length; i++)
      {
        if (!aa[i].visible)
        {
          continue;
        }

        aa[i].height = 0;

        if (aa[i].hasText)
        {
          aa[i].height += av.charHeight;
        }

        if (aa[i].hasIcons)
        {
          aa[i].height += 16;
        }

        if (aa[i].graph > 0)
        {
          aa[i].height += aa[i].graphHeight;
        }

        if (aa[i].height == 0)
        {
          aa[i].height = 20;
        }

        height += aa[i].height;
      }
    }
    else
    {
      height = 20;
    }

    this.setSize(getSize().width, height);

    repaint();

    return height;

  }

  public void addEditableColumn(int i)
  {
    if (activeRow == -1)
    {
      AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();
      if (aa == null)
      {
        return;
      }

      for (int j = 0; j < aa.length; j++)
      {
        if (aa[j].editable)
        {
          activeRow = j;
          break;
        }
      }
    }

    if (activeRes == null)
    {
      activeRes = new Vector();
      activeRes.addElement(String.valueOf(i));
      return;
    }

    activeRes.addElement(String.valueOf(i));
  }


  public void update(Graphics g)
  {
    paint(g);
  }

  public void paint(Graphics g)
  {

    imgWidth = getSize().width;
    //(av.endRes - av.startRes + 1) * av.charWidth;

    if (image == null || imgWidth != image.getWidth(this))
    {
      image = createImage(imgWidth, ap.annotationPanel.getSize().height);
      gg = image.getGraphics();
      gg.setFont(av.getFont());
      fm = gg.getFontMetrics();
      fastPaint = false;
    }

    if (fastPaint)
    {
      g.drawImage(image, 0, 0, this);
      fastPaint = false;
      return;
    }

    gg.setColor(Color.white);
    gg.fillRect(0, 0, getSize().width, getSize().height);
    drawComponent(gg, av.startRes, av.endRes + 1);

    g.drawImage(image, 0, 0, this);
  }

  public void fastPaint(int horizontal)
  {
    if (horizontal == 0
        || av.alignment.getAlignmentAnnotation() == null
        || av.alignment.getAlignmentAnnotation().length < 1
        )
    {
      repaint();
      return;
    }

    gg.copyArea(0, 0, imgWidth, getSize().height, -horizontal * av.charWidth, 0);
    int sr = av.startRes, er = av.endRes + 1, transX = 0;

    if (horizontal > 0) // scrollbar pulled right, image to the left
    {
      transX = (er - sr - horizontal) * av.charWidth;
      sr = er - horizontal;
    }
    else if (horizontal < 0)
    {
      er = sr - horizontal;
    }

    gg.translate(transX, 0);

    drawComponent(gg, sr, er);

    gg.translate( -transX, 0);

    fastPaint = true;
    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   * @param startRes DOCUMENT ME!
   * @param endRes DOCUMENT ME!
   */
  public void drawComponent(Graphics g, int startRes, int endRes)
  {
    g.setFont(av.getFont());

    g.setColor(Color.white);
    g.fillRect(0, 0, (endRes - startRes) * av.charWidth, getSize().height);

    if (fm == null)
    {
      fm = g.getFontMetrics();
    }

    if ( (av.alignment.getAlignmentAnnotation() == null) ||
        (av.alignment.getAlignmentAnnotation().length < 1))
    {
      g.setColor(Color.white);
      g.fillRect(0, 0, getSize().width, getSize().height);
      g.setColor(Color.black);
      if (av.validCharWidth)
      {
        g.drawString("Alignment has no annotations", 20, 15);
      }

      return;
    }

    AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();

    int x = 0;
    int y = 0;
    int column = 0;
    char lastSS;
    int lastSSX;
    int iconOffset = av.charHeight / 2;
    boolean validRes = false;

    boolean[] graphGroupDrawn = new boolean[aa.length];

    //\u03B2 \u03B1
    for (int i = 0; i < aa.length; i++)
    {
      AlignmentAnnotation row = aa[i];

      if (!row.visible)
      {
        continue;
      }

      lastSS = ' ';
      lastSSX = 0;

      if (row.graph > 0)
      {
        if (row.graphGroup > -1 && graphGroupDrawn[row.graphGroup])
        {
          continue;
        }

        // this is so that we draw the characters below the graph
        y += row.height;

        if (row.hasText)
        {
          y -= av.charHeight;
        }
      }

      if (row.hasText)
      {
        iconOffset = av.charHeight / 2;
      }
      else
      {
        iconOffset = 0;
      }

      x = 0;
      while (x < endRes - startRes)
      {
        if (av.hasHiddenColumns)
        {
          column = av.getColumnSelection().adjustForHiddenColumns(startRes + x);
          if (column > row.annotations.length - 1)
          {
            break;
          }
        }
        else
        {
          column = startRes + x;
        }

        if ( (row.annotations.length <= column) ||
            (row.annotations[column] == null))
        {
          validRes = false;
        }
        else
        {
          validRes = true;
        }

        if (activeRow == i)
        {
          g.setColor(Color.red);

          if (av.getColumnSelection() != null)
          {
            for (int n = 0; n < av.getColumnSelection().size(); n++)
            {
              int v = av.getColumnSelection().columnAt(n);

              if (v == column)
              {
                g.fillRect(x * av.charWidth, y,
                           av.charWidth, av.charHeight);
              }
            }
          }
        }



        if (av.validCharWidth && validRes &&
            (row.annotations[column].displayCharacter.length() > 0))
        {
          int charOffset = (av.charWidth -
                            fm.charWidth(row.annotations[column].
                                         displayCharacter.charAt(
                                             0))) / 2;

          if (row.annotations[column].colour == null)
            g.setColor(Color.black);
          else
            g.setColor(row.annotations[column].colour);

          if (column == 0 || row.graph > 0)
          {
            g.drawString(row.annotations[column].displayCharacter,
                         (x * av.charWidth) + charOffset,
                         y + iconOffset + 3);
          }
          else if (
              row.annotations[column - 1] == null
              || (!row.annotations[column].displayCharacter.equals(
                  row.annotations[column - 1].displayCharacter)
                  ||
                  (row.annotations[column].displayCharacter.length() < 2 &&
                   row.annotations[column].secondaryStructure == ' ')))
          {
            g.drawString(row.annotations[column].displayCharacter,
                         (x * av.charWidth) + charOffset,
                         y + iconOffset + 3);
          }
        }

        if (row.hasIcons)
        {
          if (!validRes ||
              (row.annotations[column].secondaryStructure != lastSS))
          {
            switch (lastSS)
            {
              case 'H':
                g.setColor(HELIX_COLOUR);
                if (MAC)
                {
                  //Off by 1 offset when drawing rects and ovals
                  //to offscreen image on the MAC
                  g.fillRoundRect(lastSSX, y + 4 + iconOffset,
                                  (x * av.charWidth) - lastSSX, 7, 8, 8);
                  break;
                }

                int sCol = (lastSSX / av.charWidth) + startRes;
                int x1 = lastSSX;
                int x2 = (x * av.charWidth);

                if (sCol == 0 ||
                    row.annotations[sCol - 1] == null ||
                    row.annotations[sCol - 1].secondaryStructure != 'H')
                {
                  g.fillArc(lastSSX, y + 4 + iconOffset, av.charWidth, 8, 90,
                            180);
                  x1 += av.charWidth / 2;
                }

                if (row.annotations[column] == null ||
                    row.annotations[column].secondaryStructure != 'H')
                {
                  g.fillArc( (x * av.charWidth) - av.charWidth,
                            y + 4 + iconOffset, av.charWidth, 8, 270, 180);
                  x2 -= av.charWidth / 2;
                }

                g.fillRect(x1, y + 4 + iconOffset, x2 - x1, 8);
                break;

              case 'E':
                g.setColor(SHEET_COLOUR);
                g.fillRect(lastSSX, y + 4 + iconOffset,
                           (x * av.charWidth) - lastSSX - 4, 7);
                g.fillPolygon(new int[]
                              { (x * av.charWidth) - 4,
                              (x * av.charWidth) - 4,
                              (x * av.charWidth)},
                              new int[]
                              {
                              y + iconOffset, y + 14 + iconOffset,
                              y + 8 + iconOffset
                }, 3);

                break;

              default:
                g.setColor(Color.gray);
                g.fillRect(lastSSX, y + 6 + iconOffset,
                           (x * av.charWidth) - lastSSX, 2);

                break;
            }

            if (validRes)
            {
              lastSS = row.annotations[column].secondaryStructure;
            }
            else
            {
              lastSS = ' ';
            }

            lastSSX = (x * av.charWidth);
          }
        }

        column++;
        x++;
      }

      if (column >= row.annotations.length)
      {
        column = row.annotations.length - 1;
      }

      //  x ++;

      if (row.hasIcons)
      {
        switch (lastSS)
        {
          case 'H':
            g.setColor(HELIX_COLOUR);
            if (MAC)
            {
              //Off by 1 offset when drawing rects and ovals
              //to offscreen image on the MAC
              g.fillRoundRect(lastSSX, y + 4 + iconOffset,
                              (x * av.charWidth) - lastSSX, 7, 8, 8);
              break;
            }

            int sCol = (lastSSX / av.charWidth) + startRes;
            int x1 = lastSSX;
            int x2 = (x * av.charWidth);

            if (sCol == 0 ||
                row.annotations[sCol - 1] == null ||
                row.annotations[sCol - 1].secondaryStructure != 'H')
            {
              g.fillArc(lastSSX, y + 4 + iconOffset, av.charWidth, 8, 90, 180);
              x1 += av.charWidth / 2;
            }

            if (row.annotations[column] == null ||
                row.annotations[column].secondaryStructure != 'H')
            {
              g.fillArc( (x * av.charWidth) - av.charWidth,
                        y + 4 + iconOffset, av.charWidth, 8, 270,
                        180);
              x2 -= av.charWidth / 2;
            }

            g.fillRect(x1, y + 4 + iconOffset, x2 - x1, 8);

            break;

          case 'E':
            g.setColor(SHEET_COLOUR);

            if (row.annotations[endRes] == null
                || row.annotations[endRes].secondaryStructure != 'E')
            {
              g.fillRect(lastSSX, y + 4 + iconOffset,
                         (x * av.charWidth) - lastSSX - 4, 7);
              g.fillPolygon(new int[]
                            { (x * av.charWidth) - 4,
                            (x * av.charWidth) - 4,
                            (x * av.charWidth)},
                            new int[]
                            {
                            y + iconOffset, y + 14 + iconOffset,
                            y + 7 + iconOffset
              }, 3);
            }
            else
            {
              g.fillRect(lastSSX, y + 4 + iconOffset,
                         x * av.charWidth - lastSSX, 7);
            }
            break;

          default:
            g.setColor(Color.gray);
            if (!av.wrapAlignment || endRes == av.endRes)
            {
              g.fillRect(lastSSX, y + 6 + iconOffset,
                         (x * av.charWidth) - lastSSX, 2);
            }

            break;
        }
      }

      if (row.graph > 0)
      {
        if (row.graph == AlignmentAnnotation.LINE_GRAPH)
        {
          if (row.graphGroup > -1 && !graphGroupDrawn[row.graphGroup])
          {
            float groupmax = -999999, groupmin = 9999999;
            for (int gg = 0; gg < aa.length; gg++)
            {
              if (aa[gg].graphGroup != row.graphGroup)
              {
                continue;
              }

              if (aa[gg] != row)
              {
                aa[gg].visible = false;
              }

              if (aa[gg].graphMax > groupmax)
              {
                groupmax = aa[gg].graphMax;
              }
              if (aa[gg].graphMin < groupmin)
              {
                groupmin = aa[gg].graphMin;
              }
            }

            for (int gg = 0; gg < aa.length; gg++)
            {
              if (aa[gg].graphGroup == row.graphGroup)
              {
                drawLineGraph(g, aa[gg], startRes, endRes, y,
                              groupmin, groupmax,
                              row.graphHeight);
              }
            }

            graphGroupDrawn[row.graphGroup] = true;
          }
          else
          {
            drawLineGraph(g, row, startRes, endRes,
                          y, row.graphMin, row.graphMax, row.graphHeight);
          }
        }
        else if (row.graph == AlignmentAnnotation.BAR_GRAPH)
        {
          drawBarGraph(g, row, startRes, endRes,
                       row.graphMin, row.graphMax, y);
        }
      }

      if (row.graph > 0 && row.hasText)
      {
        y += av.charHeight;
      }

      if (row.graph == 0)
      {
        y += aa[i].height;
      }
    }
  }

  public void drawLineGraph(Graphics g, AlignmentAnnotation aa,
                            int sRes, int eRes,
                            int y,
                            float min, float max,
                            int graphHeight)
  {
    if (sRes > aa.annotations.length)
    {
      return;
    }

    int x = 0;

    //Adjustment for fastpaint to left
    if (eRes < av.endRes)
    {
      eRes++;
    }

    eRes = Math.min(eRes, aa.annotations.length);

    if (sRes == 0)
    {
      sRes++;
      x += av.charWidth;
    }

    int y1 = y, y2 = y;
    float range = max - min;

    ////Draw origin
    if (min < 0)
    {
      y2 = y - (int) ( (0 - min / range) * graphHeight);
    }

    g.setColor(Color.gray);
    g.drawLine(x - av.charWidth, y2, (eRes - sRes) * av.charWidth, y2);

    eRes = Math.min(eRes, aa.annotations.length);

    int column;
    int aaMax = aa.annotations.length - 1;

    while (x < eRes - sRes)
    {
      column = sRes + x;
      if (av.hasHiddenColumns)
      {
        column = av.getColumnSelection().adjustForHiddenColumns(column);
      }

      if (column > aaMax)
      {
        break;
      }

      if (aa.annotations[column] == null || aa.annotations[column - 1] == null)
      {
        x++;
        continue;
      }

      if (aa.annotations[column].colour == null)
        g.setColor(Color.black);
      else
        g.setColor(aa.annotations[column].colour);

      y1 = y -
          (int) ( ( (aa.annotations[column - 1].value - min) / range) * graphHeight);
      y2 = y -
          (int) ( ( (aa.annotations[column].value - min) / range) * graphHeight);

      g.drawLine(x * av.charWidth - av.charWidth / 2, y1,
                 x * av.charWidth + av.charWidth / 2, y2);
      x++;
    }

    if (aa.threshold != null)
    {
      g.setColor(aa.threshold.colour);

      y2 = (int) (y - ( (aa.threshold.value - min) / range) * graphHeight);
      g.drawLine(0, y2, (eRes - sRes) * av.charWidth, y2);
    }
  }

  public void drawBarGraph(Graphics g, AlignmentAnnotation aa,
                           int sRes, int eRes,
                           float min, float max,
                           int y)
  {
    if (sRes > aa.annotations.length)
    {
      return;
    }

    eRes = Math.min(eRes, aa.annotations.length);

    int x = 0, y1 = y, y2 = y;

    float range = max - min;

    if (min < 0)
    {
      y2 = y - (int) ( (0 - min / (range)) * aa.graphHeight);
    }

    g.setColor(Color.gray);

    g.drawLine(x, y2, (eRes - sRes) * av.charWidth, y2);

    int column;
    int aaMax = aa.annotations.length - 1;

    while (x < eRes - sRes)
    {
      column = sRes + x;
      if (av.hasHiddenColumns)
      {
        column = av.getColumnSelection().adjustForHiddenColumns(column);
      }

      if (column > aaMax)
      {
        break;
      }

      if (aa.annotations[column] == null)
      {
        x++;
        continue;
      }

      if (aa.annotations[column].colour == null)
        g.setColor(Color.black);
      else
        g.setColor(aa.annotations[column].colour);

      y1 = y -
          (int) ( ( (aa.annotations[column].value - min) / (range)) * aa.graphHeight);

      if (y1 - y2 > 0)
      {
        g.fillRect(x * av.charWidth, y2, av.charWidth, y1 - y2);
      }
      else
      {
        g.fillRect(x * av.charWidth, y1, av.charWidth, y2 - y1);
      }

      x++;

    }
    if (aa.threshold != null)
    {
      g.setColor(aa.threshold.colour);
      y2 = (int) (y - ( (aa.threshold.value - min) / range) * aa.graphHeight);
      g.drawLine(0, y2, (eRes - sRes) * av.charWidth, y2);
    }
  }

  // used by overview window
  public void drawGraph(Graphics g, AlignmentAnnotation aa, int width, int y,
                        int sRes, int eRes)
  {
    eRes = Math.min(eRes, aa.annotations.length);
    g.setColor(Color.white);
    g.fillRect(0, 0, width, y);
    g.setColor(new Color(0, 0, 180));

    int x = 0, height;

    for (int j = sRes; j < eRes; j++)
    {
      if (aa.annotations[j].colour == null)
        g.setColor(Color.black);
      else
        g.setColor(aa.annotations[j].colour);

      height = (int) ( (aa.annotations[j].value / aa.graphMax) * GRAPH_HEIGHT);
      if (height > y)
      {
        height = y;
      }
      g.fillRect(x, y - height, av.charWidth, height);
      x += av.charWidth;
    }
  }
}
