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

import jalview.analysis.*;
import jalview.datamodel.*;
import jalview.schemes.*;
import jalview.util.*;

public class TreeCanvas
    extends Panel implements MouseListener, MouseMotionListener
{
  NJTree tree;
  ScrollPane scrollPane;
  AlignViewport av;
  public static final String PLACEHOLDER = " * ";
  Font font;
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

  public TreeCanvas(AlignViewport av, ScrollPane scroller)
  {
    this.av = av;
    font = av.getFont();
    scrollPane = scroller;
    addMouseListener(this);
    addMouseMotionListener(this);
    setLayout(null);

    PaintRefresher.Register(this, av.getSequenceSetId());
  }

  public void treeSelectionChanged(SequenceI sequence)
  {
    SequenceGroup selected = av.getSelectionGroup();
    if (selected == null)
    {
      selected = new SequenceGroup();
      av.setSelectionGroup(selected);
    }

    selected.setEndRes(av.alignment.getWidth() - 1);
    selected.addOrRemove(sequence, true);
  }

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

  public void drawNode(Graphics g, SequenceNode node, float chunk, float scale,
                       int width, int offx, int offy)
  {
    if (node == null)
    {
      return;
    }

    if (node.left() == null && node.right() == null)
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
      if (showDistances && node.dist > 0)
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

      String name = (markPlaceholders && node.isPlaceholder()) ?
          (PLACEHOLDER + node.getName()) : node.getName();
      FontMetrics fm = g.getFontMetrics(font);
      int charWidth = fm.stringWidth(name) + 3;
      int charHeight = fm.getHeight();

      Rectangle rect = new Rectangle(xend + 10, ypos - charHeight,
                                     charWidth, charHeight);

      nameHash.put( (SequenceI) node.element(), rect);

      // Colour selected leaves differently
      SequenceGroup selected = av.getSelectionGroup();
      if (selected != null &&
          selected.getSequences(null).contains( (SequenceI) node.element()))
      {
        g.setColor(Color.gray);

        g.fillRect(xend + 10, ypos - charHeight + 3, charWidth, charHeight);
        g.setColor(Color.white);
      }
      g.drawString(name, xend + 10, ypos);
      g.setColor(Color.black);
    }
    else
    {
      drawNode(g, (SequenceNode) node.left(), chunk, scale, width, offx, offy);
      drawNode(g, (SequenceNode) node.right(), chunk, scale, width, offx, offy);

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

      int ystart = (int) ( ( (SequenceNode) node.left()).ycount * chunk) + offy;
      int yend = (int) ( ( (SequenceNode) node.right()).ycount * chunk) + offy;

      Rectangle pos = new Rectangle(xend - 2, ypos - 2, 5, 5);
      nodeHash.put(node, pos);

      g.drawLine( (int) (height * scale) + offx, ystart,
                 (int) (height * scale) + offx, yend);

      if (showDistances && node.dist > 0)
      {
        g.drawString(new Format("%-.2f").form(node.dist), xstart + 2, ypos - 2);
      }

    }
  }

  public Object findElement(int x, int y)
  {
    Enumeration keys = nameHash.keys();

    while (keys.hasMoreElements())
    {
      Object ob = keys.nextElement();
      Rectangle rect = (Rectangle) nameHash.get(ob);

      if (x >= rect.x && x <= (rect.x + rect.width) &&
          y >= rect.y && y <= (rect.y + rect.height))
      {
        return ob;
      }
    }
    keys = nodeHash.keys();

    while (keys.hasMoreElements())
    {
      Object ob = keys.nextElement();
      Rectangle rect = (Rectangle) nodeHash.get(ob);

      if (x >= rect.x && x <= (rect.x + rect.width) &&
          y >= rect.y && y <= (rect.y + rect.height))
      {
        return ob;
      }
    }
    return null;

  }

  public void pickNodes(Rectangle pickBox)
  {
    int width = getSize().width;
    int height = getSize().height;

    SequenceNode top = tree.getTopNode();

    float wscale = (float) (width * .8 - offx * 2) / tree.getMaxHeight()
        ;
    if (top.count == 0)
    {
      top.count = ( (SequenceNode) top.left()).count +
          ( (SequenceNode) top.right()).count;
    }
    float chunk = (float) (height - offy) / top.count;

    pickNode(pickBox, top, chunk, wscale, width, offx, offy);
  }

  public void pickNode(Rectangle pickBox, SequenceNode node, float chunk,
                       float scale, int width, int offx, int offy)
  {
    if (node == null)
    {
      return;
    }

    if (node.left() == null && node.right() == null)
    {
      float height = node.height;
      //float dist = node.dist;

      //int xstart = (int) ( (height - dist) * scale) + offx;
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
      pickNode(pickBox, (SequenceNode) node.left(), chunk, scale, width, offx,
               offy);
      pickNode(pickBox, (SequenceNode) node.right(), chunk, scale, width, offx,
               offy);
    }
  }

  public void setColor(SequenceNode node, Color c)
  {
    if (node == null)
    {
      return;
    }

    if (node.left() == null && node.right() == null)
    {
      node.color = c;

      if (node.element() instanceof SequenceI)
      {
        av.setSequenceColour( (SequenceI) node.element(), c);
      }
    }
    else
    {
      node.color = c;
      setColor( (SequenceNode) node.left(), c);
      setColor( (SequenceNode) node.right(), c);
    }
  }

  public void update(Graphics g)
  {
    paint(g);
  }

  public void paint(Graphics g)
  {
    if (tree == null)
    {
      return;
    }

    if (nameHash.size() == 0)
    {
      repaint();
    }

    int width = scrollPane.getSize().width;
    int height = scrollPane.getSize().height;
    if (!fitToWindow)
    {
      height = g.getFontMetrics(font).getHeight() * nameHash.size();
    }

    if (getSize().width > width)
    {
      setSize(new Dimension(width, height));
      scrollPane.validate();
      return;
    }

    setSize(new Dimension(width, height));

    g.setFont(font);

    draw(g, width, height);

  }

  public void draw(Graphics g, int width, int height)
  {
    offy = font.getSize() + 10;

    g.setColor(Color.white);
    g.fillRect(0, 0, width, height);

    labelLength = g.getFontMetrics(font).stringWidth(longestName) + 20; //20 allows for scrollbar

    float wscale = (float) (width - labelLength - offx * 2) / tree.getMaxHeight();

    SequenceNode top = tree.getTopNode();

    if (top.count == 0)
    {
      top.count = ( (SequenceNode) top.left()).count +
          ( (SequenceNode) top.right()).count;
    }
    float chunk = (float) (height - offy) / top.count;

    drawNode(g, tree.getTopNode(), chunk, wscale, width, offx, offy);

    if (threshold != 0)
    {
      if (av.getCurrentTree() == tree)
      {
        g.setColor(Color.red);
      }
      else
      {
        g.setColor(Color.gray);
      }

      int x = (int) (threshold *
                     (float) (getSize().width - labelLength - 2 * offx) + offx);

      g.drawLine(x, 0, x, getSize().height);
    }

  }

  public void mouseReleased(MouseEvent e)
  {}

  public void mouseEntered(MouseEvent e)
  {}

  public void mouseExited(MouseEvent e)
  {}

  public void mouseClicked(MouseEvent evt)
  {
    if (highlightNode != null)
    {
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

      PaintRefresher.Refresh(this, av.getSequenceSetId());
      repaint();
    }
  }

  public void mouseDragged(MouseEvent ect)
  {}

  public void mouseMoved(MouseEvent evt)
  {
    av.setCurrentTree(tree);

    Object ob = findElement(evt.getX(), evt.getY());

    if (ob instanceof SequenceNode)
    {
      highlightNode = (SequenceNode) ob;
      repaint();
    }
    else
    {
      if (highlightNode != null)
      {
        highlightNode = null;
        repaint();
      }
    }
  }

  public void mousePressed(MouseEvent e)
  {
    av.setCurrentTree(tree);

    int x = e.getX();
    int y = e.getY();

    Object ob = findElement(x, y);

    if (ob instanceof SequenceI)
    {
      treeSelectionChanged( (Sequence) ob);
      PaintRefresher.Refresh(this, av.getSequenceSetId());
      repaint();
      return;
    }
    else if (! (ob instanceof SequenceNode))
    {
      // Find threshold

      if (tree.getMaxHeight() != 0)
      {
        threshold = (float) (x - offx) /
            (float) (getSize().width - labelLength - 2 * offx);

        tree.getGroups().removeAllElements();
        tree.groupNodes(tree.getTopNode(), threshold);
        setColor(tree.getTopNode(), Color.black);

        av.setSelectionGroup(null);
        av.alignment.deleteAllGroups();
        av.sequenceColours = null;

        colourGroups();

      }
    }

    PaintRefresher.Refresh(this, av.getSequenceSetId());
    repaint();

  }

  void colourGroups()
  {
    for (int i = 0; i < tree.getGroups().size(); i++)
    {

      Color col = new Color( (int) (Math.random() * 255),
                            (int) (Math.random() * 255),
                            (int) (Math.random() * 255));
      setColor( (SequenceNode) tree.getGroups().elementAt(i), col.brighter());

      Vector l = tree.findLeaves( (SequenceNode) tree.getGroups().elementAt(
          i), new Vector());

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

      SequenceGroup sg = new SequenceGroup(sequences, "",
                                           cs, true, true,
                                           false, 0,
                                           av.alignment.getWidth() - 1);

      sg.setName("JTreeGroup:" + sg.hashCode());

      if (av.getGlobalColourScheme() != null
          && av.getGlobalColourScheme().conservationApplied())
      {
        Conservation c = new Conservation("Group",
                                          ResidueProperties.propHash, 3,
                                          sg.getSequences(null),
                                          sg.getStartRes(),
                                          sg.getEndRes());

        c.calculate();
        c.verdict(false, av.ConsPercGaps);
        cs.setConservation(c);

        sg.cs = cs;

      }

      av.alignment.addGroup(sg);

    }

  }

  public void setShowDistances(boolean state)
  {
    this.showDistances = state;
    repaint();
  }

  public void setShowBootstrap(boolean state)
  {
    this.showBootstrap = state;
    repaint();
  }

  public void setMarkPlaceholders(boolean state)
  {
    this.markPlaceholders = state;
    repaint();
  }

}
