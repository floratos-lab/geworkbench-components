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
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;

import jalview.analysis.*;
import jalview.datamodel.*;
import jalview.schemes.*;
import jalview.util.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class TreeCanvas
    extends JPanel implements MouseListener, Runnable,
    Printable, MouseMotionListener
{
  /** DOCUMENT ME!! */
  public static final String PLACEHOLDER = " * ";
  NJTree tree;
  JScrollPane scrollPane;
  TreePanel tp;
  AlignViewport av;
  AlignmentPanel ap;
  Font font;
  FontMetrics fm;
  boolean fitToWindow = true;
  boolean showDistances = false;
  boolean showBootstrap = false;
  boolean markPlaceholders = false;
  int offx = 20;
  int offy;
  float threshold;
  String longestName;
  int labelLength = -1;

  Hashtable nameHash = new Hashtable();
  Hashtable nodeHash = new Hashtable();
  SequenceNode highlightNode;

  boolean applyToAllViews = false;

  /**
   * Creates a new TreeCanvas object.
   *
   * @param av DOCUMENT ME!
   * @param tree DOCUMENT ME!
   * @param scroller DOCUMENT ME!
   * @param label DOCUMENT ME!
   */
  public TreeCanvas(TreePanel tp,
                    AlignmentPanel ap,
                    JScrollPane scroller)
  {
    this.tp = tp;
    this.av = ap.av;
    this.ap = ap;
    font = av.getFont();
    scrollPane = scroller;
    addMouseListener(this);
    addMouseMotionListener(this);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /**
   * DOCUMENT ME!
   *
   * @param sequence DOCUMENT ME!
   */
  public void treeSelectionChanged(SequenceI sequence)
  {
    AlignmentPanel[] aps = getAssociatedPanels();

    for (int a = 0; a < aps.length; a++)
    {
      SequenceGroup selected = aps[a].av.getSelectionGroup();

      if (selected == null)
      {
        selected = new SequenceGroup();
        aps[a].av.setSelectionGroup(selected);
      }

      selected.setEndRes(aps[a].av.alignment.getWidth() - 1);
      selected.addOrRemove(sequence, true);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param tree DOCUMENT ME!
   */
  public void setTree(NJTree tree)
  {
    this.tree = tree;
    tree.findHeight(tree.getTopNode());

    // Now have to calculate longest name based on the leaves
    Vector leaves = tree.findLeaves(tree.getTopNode(), new Vector());
    boolean has_placeholders = false;
    longestName = "";

    for (int i = 0; i < leaves.size(); i++)
    {
      SequenceNode lf = (SequenceNode) leaves.elementAt(i);

      if (lf.isPlaceholder())
      {
        has_placeholders = true;
      }

      if (longestName.length() < ( (Sequence) lf.element()).getName()
          .length())
      {
        longestName = TreeCanvas.PLACEHOLDER +
            ( (Sequence) lf.element()).getName();
      }
    }

    setMarkPlaceholders(has_placeholders);
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   * @param node DOCUMENT ME!
   * @param chunk DOCUMENT ME!
   * @param scale DOCUMENT ME!
   * @param width DOCUMENT ME!
   * @param offx DOCUMENT ME!
   * @param offy DOCUMENT ME!
   */
  public void drawNode(Graphics g, SequenceNode node, float chunk,
                       float scale, int width, int offx, int offy)
  {
    if (node == null)
    {
      return;
    }

    if ( (node.left() == null) && (node.right() == null))
    {
      // Drawing leaf node
      float height = node.height;
      float dist = node.dist;

      int xstart = (int) ( (height - dist) * scale) + offx;
      int xend = (int) (height * scale) + offx;

      int ypos = (int) (node.ycount * chunk) + offy;

      if (node.element() instanceof SequenceI)
      {
        SequenceI seq = (SequenceI) ( (SequenceNode) node).element();

        if (av.getSequenceColour(seq) == Color.white)
        {
          g.setColor(Color.black);
        }
        else
        {
          g.setColor(av.getSequenceColour(seq).darker());
        }
      }
      else
      {
        g.setColor(Color.black);
      }

      // Draw horizontal line
      g.drawLine(xstart, ypos, xend, ypos);

      String nodeLabel = "";

      if (showDistances && (node.dist > 0))
      {
        nodeLabel = new Format("%-.2f").form(node.dist);
      }

      if (showBootstrap)
      {
        if (showDistances)
        {
          nodeLabel = nodeLabel + " : ";
        }

        nodeLabel = nodeLabel + String.valueOf(node.getBootstrap());
      }

      if (!nodeLabel.equals(""))
      {
        g.drawString(nodeLabel, xstart + 2, ypos - 2);
      }

      String name = (markPlaceholders && node.isPlaceholder())
          ? (PLACEHOLDER + node.getName()) : node.getName();

      int charWidth = fm.stringWidth(name) + 3;
      int charHeight = font.getSize();

      Rectangle rect = new Rectangle(xend + 10, ypos - charHeight / 2,
                                     charWidth, charHeight);

      nameHash.put( (SequenceI) node.element(), rect);

      // Colour selected leaves differently
      SequenceGroup selected = av.getSelectionGroup();

      if ( (selected != null) &&
          selected.getSequences(null).contains( (SequenceI) node.element()))
      {
        g.setColor(Color.gray);

        g.fillRect(xend + 10, ypos - charHeight / 2, charWidth,
                   charHeight);
        g.setColor(Color.white);
      }

      g.drawString(name, xend + 10, ypos + fm.getDescent());
      g.setColor(Color.black);
    }
    else
    {
      drawNode(g, (SequenceNode) node.left(), chunk, scale, width, offx,
               offy);
      drawNode(g, (SequenceNode) node.right(), chunk, scale, width, offx,
               offy);

      float height = node.height;
      float dist = node.dist;

      int xstart = (int) ( (height - dist) * scale) + offx;
      int xend = (int) (height * scale) + offx;
      int ypos = (int) (node.ycount * chunk) + offy;

      g.setColor( ( (SequenceNode) node).color.darker());

      // Draw horizontal line
      g.drawLine(xstart, ypos, xend, ypos);
      if (node == highlightNode)
      {
        g.fillRect(xend - 3, ypos - 3, 6, 6);
      }
      else
      {
        g.fillRect(xend - 2, ypos - 2, 4, 4);
      }

      int ystart = (int) ( ( (SequenceNode) node.left()).ycount * chunk) +
          offy;
      int yend = (int) ( ( (SequenceNode) node.right()).ycount * chunk) +
          offy;

      Rectangle pos = new Rectangle(xend - 2, ypos - 2, 5, 5);
      nodeHash.put(node, pos);

      g.drawLine( (int) (height * scale) + offx, ystart,
                 (int) (height * scale) + offx, yend);

      if (showDistances && (node.dist > 0))
      {
        g.drawString(new Format("%-.2f").form(node.dist).trim(), xstart + 2,
                     ypos - 2);
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param x DOCUMENT ME!
   * @param y DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Object findElement(int x, int y)
  {
    Enumeration keys = nameHash.keys();

    while (keys.hasMoreElements())
    {
      Object ob = keys.nextElement();
      Rectangle rect = (Rectangle) nameHash.get(ob);

      if ( (x >= rect.x) && (x <= (rect.x + rect.width)) && (y >= rect.y) &&
          (y <= (rect.y + rect.height)))
      {
        return ob;
      }
    }

    keys = nodeHash.keys();

    while (keys.hasMoreElements())
    {
      Object ob = keys.nextElement();
      Rectangle rect = (Rectangle) nodeHash.get(ob);

      if ( (x >= rect.x) && (x <= (rect.x + rect.width)) && (y >= rect.y) &&
          (y <= (rect.y + rect.height)))
      {
        return ob;
      }
    }

    return null;
  }

  /**
   * DOCUMENT ME!
   *
   * @param pickBox DOCUMENT ME!
   */
  public void pickNodes(Rectangle pickBox)
  {
    int width = getWidth();
    int height = getHeight();

    SequenceNode top = tree.getTopNode();

    float wscale = (float) ( (width * .8) - (offx * 2)) / tree.getMaxHeight();

    if (top.count == 0)
    {
      top.count = ( (SequenceNode) top.left()).count +
          ( (SequenceNode) top.right()).count;
    }

    float chunk = (float) (height - (offy)) / top.count;

    pickNode(pickBox, top, chunk, wscale, width, offx, offy);
  }

  /**
   * DOCUMENT ME!
   *
   * @param pickBox DOCUMENT ME!
   * @param node DOCUMENT ME!
   * @param chunk DOCUMENT ME!
   * @param scale DOCUMENT ME!
   * @param width DOCUMENT ME!
   * @param offx DOCUMENT ME!
   * @param offy DOCUMENT ME!
   */
  public void pickNode(Rectangle pickBox, SequenceNode node, float chunk,
                       float scale, int width, int offx, int offy)
  {
    if (node == null)
    {
      return;
    }

    if ( (node.left() == null) && (node.right() == null))
    {
      float height = node.height;
      float dist = node.dist;

      int xstart = (int) ( (height - dist) * scale) + offx;
      int xend = (int) (height * scale) + offx;

      int ypos = (int) (node.ycount * chunk) + offy;

      if (pickBox.contains(new Point(xend, ypos)))
      {
        if (node.element() instanceof SequenceI)
        {
          SequenceI seq = (SequenceI) node.element();
          SequenceGroup sg = av.getSelectionGroup();

          if (sg != null)
          {
            sg.addOrRemove(seq, true);
          }
        }
      }
    }
    else
    {
      pickNode(pickBox, (SequenceNode) node.left(), chunk, scale, width,
               offx, offy);
      pickNode(pickBox, (SequenceNode) node.right(), chunk, scale, width,
               offx, offy);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   * @param c DOCUMENT ME!
   */
  public void setColor(SequenceNode node, Color c)
  {
    if (node == null)
    {
      return;
    }

    if ( (node.left() == null) && (node.right() == null))
    {
      node.color = c;

      if (node.element() instanceof SequenceI)
      {
        AlignmentPanel[] aps = getAssociatedPanels();
        for (int a = 0; a < aps.length; a++)
        {
          aps[a].av.setSequenceColour( (SequenceI) node.element(), c);
        }
      }
    }
    else
    {
      node.color = c;
      setColor( (SequenceNode) node.left(), c);
      setColor( (SequenceNode) node.right(), c);
    }
  }

  /**
   * DOCUMENT ME!
   */
  void startPrinting()
  {
    Thread thread = new Thread(this);
    thread.start();
  }

  // put printing in a thread to avoid painting problems
  public void run()
  {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    PageFormat pf = printJob.pageDialog(printJob.defaultPage());

    printJob.setPrintable(this, pf);

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
    pg.setFont(font);
    pg.translate( (int) pf.getImageableX(), (int) pf.getImageableY());

    int pwidth = (int) pf.getImageableWidth();
    int pheight = (int) pf.getImageableHeight();

    int noPages = getHeight() / pheight;

    if (pi > noPages)
    {
      return Printable.NO_SUCH_PAGE;
    }

    if (pwidth > getWidth())
    {
      pwidth = getWidth();
    }

    if (fitToWindow)
    {
      if (pheight > getHeight())
      {
        pheight = getHeight();
      }

      noPages = 0;
    }
    else
    {
      FontMetrics fm = pg.getFontMetrics(font);
      int height = fm.getHeight() * nameHash.size();
      pg.translate(0, -pi * pheight);
      pg.setClip(0, pi * pheight, pwidth, (pi * pheight) + pheight);

      // translate number of pages,
      // height is screen size as this is the
      // non overlapping text size
      pheight = height;
    }

    draw(pg, pwidth, pheight);

    return Printable.PAGE_EXISTS;
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   */
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    g.setFont(font);

    if (tree == null)
    {
      g.drawString("Calculating tree....", 20, getHeight() / 2);
    }
    else
    {
      fm = g.getFontMetrics(font);

      if (nameHash.size() == 0)
      {
        repaint();
      }

      if (fitToWindow ||
          (!fitToWindow &&
           (scrollPane.getHeight() > ( (fm.getHeight() * nameHash.size()) +
                                      offy))))
      {
        draw(g, scrollPane.getWidth(), scrollPane.getHeight());
        setPreferredSize(null);
      }
      else
      {
        setPreferredSize(new Dimension(scrollPane.getWidth(),
                                       fm.getHeight() * nameHash.size()));
        draw(g, scrollPane.getWidth(), fm.getHeight() * nameHash.size());
      }

      scrollPane.revalidate();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param fontSize DOCUMENT ME!
   */
  public void setFont(Font font)
  {
    this.font = font;
    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param g1 DOCUMENT ME!
   * @param width DOCUMENT ME!
   * @param height DOCUMENT ME!
   */
  public void draw(Graphics g1, int width, int height)
  {
    Graphics2D g2 = (Graphics2D) g1;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(Color.white);
    g2.fillRect(0, 0, width, height);

    g2.setFont(font);

    offy = font.getSize() + 10;

    fm = g2.getFontMetrics(font);

    labelLength = fm.stringWidth(longestName) + 20; //20 allows for scrollbar

    float wscale = (float) (width - labelLength - (offx * 2)) /
        tree.getMaxHeight();

    SequenceNode top = tree.getTopNode();

    if (top.count == 0)
    {
      top.count = ( (SequenceNode) top.left()).count +
          ( (SequenceNode) top.right()).count;
    }

    float chunk = (float) (height - (offy)) / top.count;

    drawNode(g2, tree.getTopNode(), chunk, wscale, width, offx, offy);

    if (threshold != 0)
    {
      if (av.getCurrentTree() == tree)
      {
        g2.setColor(Color.red);
      }
      else
      {
        g2.setColor(Color.gray);
      }

      int x = (int) ( (threshold * (float) (getWidth() - labelLength -
                                            (2 * offx))) + offx);

      g2.drawLine(x, 0, x, getHeight());
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mouseReleased(MouseEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mouseEntered(MouseEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mouseExited(MouseEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mouseClicked(MouseEvent evt)
  {
    if (highlightNode != null)
    {
      if (SwingUtilities.isRightMouseButton(evt))
      {
        Color col = JColorChooser.showDialog(this, "Select Sub-Tree Colour",
                                             highlightNode.color);

        setColor(highlightNode, col);
      }
      else
      if (evt.getClickCount() > 1)
      {
        tree.swapNodes(highlightNode);
        tree.reCount(tree.getTopNode());
        tree.findHeight(tree.getTopNode());
      }
      else
      {
        Vector leaves = new Vector();
        tree.findLeaves(highlightNode, leaves);

        for (int i = 0; i < leaves.size(); i++)
        {
          SequenceI seq =
              (SequenceI) ( (SequenceNode) leaves.elementAt(i)).element();
          treeSelectionChanged(seq);
        }
      }

      PaintRefresher.Refresh(tp, av.getSequenceSetId());
      repaint();
    }
  }

  public void mouseMoved(MouseEvent evt)
  {
    av.setCurrentTree(tree);

    Object ob = findElement(evt.getX(), evt.getY());

    if (ob instanceof SequenceNode)
    {
      highlightNode = (SequenceNode) ob;
      this.setToolTipText(
          "<html>Left click to select leaves"
          + "<br>Double-click to invert leaves"
          + "<br>Right click to change colour");
      repaint();

    }
    else
    {
      if (highlightNode != null)
      {
        highlightNode = null;
        setToolTipText(null);
        repaint();
      }
    }
  }

  public void mouseDragged(MouseEvent ect)
  {}

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mousePressed(MouseEvent e)
  {
    av.setCurrentTree(tree);

    int x = e.getX();
    int y = e.getY();

    Object ob = findElement(x, y);

    if (ob instanceof SequenceI)
    {
      treeSelectionChanged( (Sequence) ob);
      PaintRefresher.Refresh(tp, ap.av.getSequenceSetId());
      repaint();
      return;
    }
    else if (! (ob instanceof SequenceNode))
    {
      // Find threshold
      if (tree.getMaxHeight() != 0)
      {
        threshold = (float) (x - offx) / (float) (getWidth() -
                                                  labelLength - (2 * offx));

        tree.getGroups().removeAllElements();
        tree.groupNodes(tree.getTopNode(), threshold);
        setColor(tree.getTopNode(), Color.black);

        AlignmentPanel[] aps = getAssociatedPanels();

        for (int a = 0; a < aps.length; a++)
        {
          aps[a].av.setSelectionGroup(null);
          aps[a].av.alignment.deleteAllGroups();
          aps[a].av.sequenceColours = null;
        }
        colourGroups();
      }

      PaintRefresher.Refresh(tp, ap.av.getSequenceSetId());
      repaint();
    }

  }

  void colourGroups()
  {
    for (int i = 0; i < tree.getGroups().size(); i++)
    {
      Color col = new Color( (int) (Math.random() * 255),
                            (int) (Math.random() * 255),
                            (int) (Math.random() * 255));
      setColor( (SequenceNode) tree.getGroups().elementAt(i),
               col.brighter());

      Vector l = tree.findLeaves( (SequenceNode) tree.getGroups()
                                 .elementAt(i),
                                 new Vector());

      Vector sequences = new Vector();

      for (int j = 0; j < l.size(); j++)
      {
        SequenceI s1 = (SequenceI) ( (SequenceNode) l.elementAt(j)).element();

        if (!sequences.contains(s1))
        {
          sequences.addElement(s1);
        }
      }

      ColourSchemeI cs = null;

      if (av.getGlobalColourScheme() != null)
      {
        if (av.getGlobalColourScheme() instanceof UserColourScheme)
        {
          cs = new UserColourScheme(
              ( (UserColourScheme) av.getGlobalColourScheme()).getColours());

        }
        else
        {
          cs = ColourSchemeProperty.getColour(sequences,
                                              av.alignment.getWidth(),
                                              ColourSchemeProperty.
                                              getColourName(
                                                  av.getGlobalColourScheme()));
        }

        cs.setThreshold(av.getGlobalColourScheme().getThreshold(),
                        av.getIgnoreGapsConsensus());
      }

      SequenceGroup sg = new SequenceGroup(sequences,
                                           null, cs, true, true, false,
                                           0,
                                           av.alignment.getWidth() - 1);

      sg.setName("JTreeGroup:" + sg.hashCode());

      AlignmentPanel[] aps = getAssociatedPanels();
      for (int a = 0; a < aps.length; a++)
      {
        if (aps[a].av.getGlobalColourScheme() != null
            && aps[a].av.getGlobalColourScheme().conservationApplied())
        {
          Conservation c = new Conservation("Group",
                                            ResidueProperties.propHash, 3,
                                            sg.getSequences(null),
                                            sg.getStartRes(), sg.getEndRes());

          c.calculate();
          c.verdict(false, aps[a].av.ConsPercGaps);
          sg.cs.setConservation(c);
        }

        aps[a].av.alignment.addGroup(sg);
      }
    }

  }

  /**
   * DOCUMENT ME!
   *
   * @param state DOCUMENT ME!
   */
  public void setShowDistances(boolean state)
  {
    this.showDistances = state;
    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param state DOCUMENT ME!
   */
  public void setShowBootstrap(boolean state)
  {
    this.showBootstrap = state;
    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param state DOCUMENT ME!
   */
  public void setMarkPlaceholders(boolean state)
  {
    this.markPlaceholders = state;
    repaint();
  }

  AlignmentPanel[] getAssociatedPanels()
  {
    if (applyToAllViews)
    {
      return PaintRefresher.getAssociatedPanels(av.getSequenceSetId());
    }
    else
    {
      return new AlignmentPanel[]
          {
          ap};
    }
  }
}
