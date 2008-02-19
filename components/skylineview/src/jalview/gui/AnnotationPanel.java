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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import jalview.datamodel.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class AnnotationPanel
    extends JPanel implements MouseListener,
    MouseMotionListener, ActionListener, AdjustmentListener
{
  final String HELIX = "Helix";
  final String SHEET = "Sheet";
  final String LABEL = "Label";
  final String REMOVE = "Remove Annotation";
  final String COLOUR = "Colour";
  final Color HELIX_COLOUR = Color.red.darker();
  final Color SHEET_COLOUR = Color.green.darker().darker();

  /** DOCUMENT ME!! */
  AlignViewport av;
  AlignmentPanel ap;
  int activeRow = -1;
  BufferedImage image;
  BufferedImage fadedImage;
  Graphics2D gg;
  FontMetrics fm;
  int imgWidth = 0;
  boolean fastPaint = false;

  //Used For mouse Dragging and resizing graphs
  int graphStretch = -1;
  int graphStretchY = -1;
  int min; //used by mouseDragged to see if user
  int max; //used by mouseDragged to see if user
  boolean mouseDragging = false;

  boolean MAC = false;

  /**
   * Creates a new AnnotationPanel object.
   *
   * @param ap DOCUMENT ME!
   */
  public AnnotationPanel(AlignmentPanel ap)
  {

    if (System.getProperty("os.name").startsWith("Mac"))
    {
      MAC = true;
    }

    ToolTipManager.sharedInstance().registerComponent(this);
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(10000);
    this.ap = ap;
    av = ap.av;
    this.setLayout(null);
    addMouseListener(this);
    addMouseMotionListener(this);
    ap.annotationScroller.getVerticalScrollBar().addAdjustmentListener(this);
  }

  public AnnotationPanel(AlignViewport av)
  {
    this.av = av;
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    ap.alabels.setScrollOffset( -evt.getValue());
  }

  /**
   * DOCUMENT ME!
   */
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

    this.setPreferredSize(new Dimension(1, height));

    return height;
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

    if (evt.getActionCommand().equals(REMOVE))
    {
      for (int i = 0; i < av.getColumnSelection().size(); i++)
      {
        anot[av.getColumnSelection().columnAt(i)] = null;
      }
    }
    else if (evt.getActionCommand().equals(LABEL))
    {
      String label = JOptionPane.showInputDialog(this, "Enter Label ",
                                                 "Enter label",
                                                 JOptionPane.QUESTION_MESSAGE);

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
      Color col = JColorChooser.showDialog(this,
                                           "Choose foreground colour",
                                           Color.black);

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

      String label = JOptionPane.showInputDialog(
          "Enter a label for the structure?",
          symbol);

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

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
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
        else if (aa[i].graph > 0)
        {
          //Stretch Graph
          graphStretch = i;
          graphStretchY = evt.getY();
        }

        break;
      }
    }

    if (SwingUtilities.isRightMouseButton(evt) && activeRow != -1)
    {
      if (av.getColumnSelection() == null)
      {
        return;
      }

      JPopupMenu pop = new JPopupMenu("Structure type");
      JMenuItem item = new JMenuItem(HELIX);
      item.addActionListener(this);
      pop.add(item);
      item = new JMenuItem(SHEET);
      item.addActionListener(this);
      pop.add(item);
      item = new JMenuItem(LABEL);
      item.addActionListener(this);
      pop.add(item);
      item = new JMenuItem(COLOUR);
      item.addActionListener(this);
      pop.add(item);
      item = new JMenuItem(REMOVE);
      item.addActionListener(this);
      pop.add(item);
      pop.show(this, evt.getX(), evt.getY());

      return;
    }

    if (aa == null)
    {
      return;
    }

    ap.scalePanel.mousePressed(evt);

  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseReleased(MouseEvent evt)
  {
    graphStretch = -1;
    graphStretchY = -1;
    mouseDragging = false;
    ap.scalePanel.mouseReleased(evt);
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseEntered(MouseEvent evt)
  {
     ap.scalePanel.mouseEntered(evt);
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseExited(MouseEvent evt)
  {
    ap.scalePanel.mouseExited(evt);
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseDragged(MouseEvent evt)
  {
    if (graphStretch > -1)
    {
      av.alignment.getAlignmentAnnotation()[graphStretch].graphHeight +=
          graphStretchY - evt.getY();
      if (av.alignment.getAlignmentAnnotation()[graphStretch].graphHeight < 0)
      {
        av.alignment.getAlignmentAnnotation()[graphStretch].graphHeight = 0;
      }
      graphStretchY = evt.getY();
      adjustPanelHeight();
      ap.paintAlignment(true);
    }
    else
    {
      ap.scalePanel.mouseDragged(evt);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseMoved(MouseEvent evt)
  {
    AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();

    if (aa == null)
    {
      this.setToolTipText(null);
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

    if (row == -1)
    {
      this.setToolTipText(null);
      return;
    }

    int res = (evt.getX() / av.getCharWidth()) + av.getStartRes();

    if (av.hasHiddenColumns)
    {
      res = av.getColumnSelection().adjustForHiddenColumns(res);
    }

    if (aa[row].annotations != null
        && row > -1
        && res < (int) aa[row].annotations.length)
    {
      if (aa[row].graphGroup > -1)
      {
        StringBuffer tip = new StringBuffer("<html>");
        for (int gg = 0; gg < aa.length; gg++)
        {
          if (aa[gg].graphGroup == aa[row].graphGroup && aa[gg].annotations[res] != null)
          {
            tip.append(aa[gg].label + " " + aa[gg].annotations[res].description +
                       "<br>");
          }
        }
        if (tip.length() != 6)
        {
          tip.setLength(tip.length() - 4);
          this.setToolTipText(tip.toString() + "</html>");
        }
      }
      else if (aa[row].annotations[res] != null
               && aa[row].annotations[res].description != null)
      {
        this.setToolTipText(aa[row].annotations[res].description);
      }

      if (aa[row].annotations[res] != null)
      {
        StringBuffer text = new StringBuffer("Sequence position " +
                                             (res + 1));

        if (aa[row].annotations[res].description != null)
        {
          text.append("  " + aa[row].annotations[res].description);
        }

        ap.alignFrame.statusBar.setText(text.toString());
      }
    }
    else
    {
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
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   */
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());

    if (image != null)
    {
      if (fastPaint
          || (getVisibleRect().width != g.getClipBounds().width)
          || (getVisibleRect().height != g.getClipBounds().height))
      {
        g.drawImage(image, 0, 0, this);
        fastPaint = false;
        return;
      }
    }
    imgWidth = (av.endRes - av.startRes + 1) * av.charWidth;

    if (image == null || imgWidth != image.getWidth()
        || image.getHeight(this) != getHeight())
    {
      image = new BufferedImage(imgWidth, ap.annotationPanel.getHeight(),
                                BufferedImage.TYPE_INT_RGB);
      gg = (Graphics2D) image.getGraphics();

      if (av.antiAlias)
      {
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
      }

      gg.setFont(av.getFont());
      fm = gg.getFontMetrics();
      gg.setColor(Color.white);
      gg.fillRect(0, 0, imgWidth, image.getHeight());
    }

    drawComponent(gg, av.startRes, av.endRes + 1);
    g.drawImage(image, 0, 0, this);
  }

  /**
   * DOCUMENT ME!
   *
   * @param horizontal DOCUMENT ME!
   */
  public void fastPaint(int horizontal)
  {
    if ( (horizontal == 0)
        || gg == null
        || av.alignment.getAlignmentAnnotation() == null
        || av.alignment.getAlignmentAnnotation().length < 1
        || av.updatingConsensus
        || av.updatingConservation)
    {
      repaint();
      return;
    }

    gg.copyArea(0, 0, imgWidth, getHeight(), -horizontal * av.charWidth, 0);

    int sr = av.startRes;
    int er = av.endRes + 1;
    int transX = 0;

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
    if (av.updatingConsensus || av.updatingConservation)
    {
      if (image == null)
      {
        return;
      }
      //We'll keep a record of the old image,
      //and draw a faded image until the calculation
      //has completed
      if (fadedImage == null
          || fadedImage.getWidth() != imgWidth
          || fadedImage.getHeight() != image.getHeight())
      {
        fadedImage = new BufferedImage(
            imgWidth, image.getHeight(),
            BufferedImage.TYPE_INT_RGB);

        Graphics2D fadedG = (Graphics2D) fadedImage.getGraphics();

        fadedG.setColor(Color.white);
        fadedG.fillRect(0, 0, imgWidth, image.getHeight());

        fadedG.setComposite(
            AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, .3f));
        fadedG.drawImage(image, 0, 0, this);

      }

    }
    else
    {
      fadedImage = null;
    }

    g.setColor(Color.white);
    g.fillRect(0, 0, (endRes - startRes) * av.charWidth, getHeight());

    g.setFont(av.getFont());
    if (fm == null)
    {
      fm = g.getFontMetrics();
    }

    if ( (av.alignment.getAlignmentAnnotation() == null) ||
        (av.alignment.getAlignmentAnnotation().length < 1))
    {
      g.setColor(Color.white);
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(Color.black);
      if (av.validCharWidth)
      {
        g.drawString("Alignment has no annotations", 20, 15);
      }

      return;
    }

    AlignmentAnnotation[] aa = av.alignment.getAlignmentAnnotation();

    int x = 0, y=0;
    int column = 0;
    char lastSS;
    int lastSSX;
    int iconOffset = 0;
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
          iconOffset =av.charHeight -fm.getDescent();
          y -= av.charHeight;
        }
      }
      else if (row.hasText)
      {
        iconOffset = av.charHeight -fm.getDescent();

      }
      else
      {
        iconOffset = 0;
      }


      if (av.updatingConsensus && aa[i] == av.consensus)
      {
        y += av.charHeight;

        g.drawImage(fadedImage,
                    0, y - row.height, imgWidth, y,
                    0, y - row.height, imgWidth, y, this);
        g.setColor(Color.black);
        // g.drawString("Calculating Consensus....",20, y-row.height/2);

        continue;
      }
      else if (av.updatingConservation && aa[i].label.equals("Conservation"))
      {

        y += av.charHeight;
        g.drawImage(fadedImage,
                    0, y - row.height, imgWidth, y,
                    0, y - row.height, imgWidth, y, this);

        g.setColor(Color.black);
        //  g.drawString("Calculating Conservation.....",20, y-row.height/2);

        continue;
      }
      else if (av.updatingConservation && aa[i].label.equals("Quality"))
      {

        y += av.charHeight;
        g.drawImage(fadedImage,
                    0, y - row.height, imgWidth, y,
                    0, y - row.height, imgWidth, y, this);
        g.setColor(Color.black);
        /// g.drawString("Calculating Quality....",20, y-row.height/2);

        continue;
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

        if (av.validCharWidth && validRes
            && row.annotations[column].displayCharacter != null
            && (row.annotations[column].displayCharacter.length() > 0))
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
                         y + iconOffset);
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
                         x * av.charWidth + charOffset,
                         y + iconOffset);
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
                         (x + 1) * av.charWidth - lastSSX, 7);
            }
            break;

          default:
            g.setColor(Color.gray);
            g.fillRect(lastSSX, y + 6 + iconOffset,
                       (x * av.charWidth) - lastSSX, 2);

            break;
        }
      }

      if (row.graph > 0 && row.graphHeight > 0)
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
      x++;
    }

    int y1 = y, y2 = y;
    float range = max - min;

    ////Draw origin
    if (min < 0)
    {
      y2 = y - (int) ( (0 - min / range) * graphHeight);
    }

    g.setColor(Color.gray);
    g.drawLine(x - av.charWidth, y2, (eRes - sRes + 1) * av.charWidth, y2);

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
      Graphics2D g2 = (Graphics2D) g;
      g2.setStroke(new BasicStroke(1,
                                   BasicStroke.CAP_SQUARE,
                                   BasicStroke.JOIN_ROUND, 3f,
                                   new float[]
                                   {5f, 3f}, 0f));

      y2 = (int) (y - ( (aa.threshold.value - min) / range) * graphHeight);
      g.drawLine(0, y2, (eRes - sRes) * av.charWidth, y2);
      g2.setStroke(new BasicStroke());
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
      Graphics2D g2 = (Graphics2D) g;
      g2.setStroke(new BasicStroke(1,
                                   BasicStroke.CAP_SQUARE,
                                   BasicStroke.JOIN_ROUND, 3f,
                                   new float[]
                                   {5f, 3f}, 0f));

      y2 = (int) (y - ( (aa.threshold.value - min) / range) * aa.graphHeight);
      g.drawLine(0, y2, (eRes - sRes) * av.charWidth, y2);
      g2.setStroke(new BasicStroke());
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
      if (aa.annotations[j] != null)
      {
        if (aa.annotations[j].colour == null)
          g.setColor(Color.black);
        else
          g.setColor(aa.annotations[j].colour);

        height = (int) ( (aa.annotations[j].value / aa.graphMax) * y);
        if (height > y)
        {
          height = y;
        }

        g.fillRect(x, y - height, av.charWidth, height);
      }
      x += av.charWidth;
    }
  }

}
