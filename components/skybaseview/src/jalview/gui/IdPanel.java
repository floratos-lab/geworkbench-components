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
import javax.swing.*;

import jalview.datamodel.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class IdPanel
    extends JPanel implements MouseListener,
    MouseMotionListener, MouseWheelListener
{
  protected IdCanvas idCanvas;
  protected AlignViewport av;
  protected AlignmentPanel alignPanel;
  ScrollThread scrollThread = null;
  int offy;
  int width;
  int lastid = -1;
  boolean mouseDragging = false;

  /**
   * Creates a new IdPanel object.
   *
   * @param av DOCUMENT ME!
   * @param parent DOCUMENT ME!
   */
  public IdPanel(AlignViewport av, AlignmentPanel parent)
  {
    this.av = av;
    alignPanel = parent;
    idCanvas = new IdCanvas(av);
    setLayout(new BorderLayout());
    add(idCanvas, BorderLayout.CENTER);
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mouseMoved(MouseEvent e)
  {
    int seq = Math.max(0, alignPanel.seqPanel.findSeq(e));
    String tmp;
    if (seq > -1 && seq < av.alignment.getHeight())
    {
      SequenceI sequence = av.alignment.getSequenceAt(seq);
      StringBuffer tip = new StringBuffer();
      tip.append("<i>");

      int maxWidth = 0;
      if (sequence.getDescription() != null)
      {
        tmp = sequence.getDescription();
        tip.append("<br>"+tmp);
        maxWidth = Math.max(maxWidth, tmp.length());
      }

      DBRefEntry[] dbrefs = sequence.getDatasetSequence().getDBRef();
      if (dbrefs != null)
      {
        for (int i = 0; i < dbrefs.length; i++)
        {
          tip.append("<br>");
          tmp = dbrefs[i].getSource() + " " + dbrefs[i].getAccessionId();
          tip.append(tmp);
          maxWidth = Math.max(maxWidth, tmp.length());
        }
      }


      //ADD NON POSITIONAL SEQUENCE INFO
      SequenceFeature[] features = sequence.getDatasetSequence().
          getSequenceFeatures();
      if (features != null)
      {
        for (int i = 0; i < features.length; i++)
        {
          if (features[i].begin == 0 && features[i].end == 0)
          {
            tmp = features[i].featureGroup
                       + " " + features[i].getType() + " " +
                       features[i].description;
            tip.append("<br>" + tmp);
            maxWidth = Math.max(maxWidth, tmp.length());
          }
        }
      }

      if(maxWidth > 60)
      {
         tip.insert(0, "<table width=350 border=0><tr><td><i>");
         tip.append("</i></td></tr></table>");
      }

      tip.append("</html>");

      setToolTipText("<html>"+sequence.getDisplayId(true)+tip.toString());
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mouseDragged(MouseEvent e)
  {
    mouseDragging = true;

    int seq = Math.max(0, alignPanel.seqPanel.findSeq(e));

    if (seq < lastid)
    {
      selectSeqs(lastid - 1, seq);
    }
    else if (seq > lastid)
    {
      selectSeqs(lastid + 1, seq);
    }

    lastid = seq;
    alignPanel.paintAlignment(true);
  }

  public void mouseWheelMoved(MouseWheelEvent e)
  {
    e.consume();
      if (e.getWheelRotation() > 0)
      {
        alignPanel.scrollUp(false);
      }
      else
      {
        alignPanel.scrollUp(true);
      }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mouseClicked(MouseEvent e)
  {
    if (e.getClickCount() < 2)
    {
      return;
    }

    java.util.Vector links = Preferences.sequenceURLLinks;
    if (links == null || links.size() < 1)
    {
      return;
    }

    int seq = alignPanel.seqPanel.findSeq(e);

    //DEFAULT LINK IS FIRST IN THE LINK LIST

    String id = av.getAlignment().getSequenceAt(seq).getName();
    if (id.indexOf("|") > -1)
    {
      id = id.substring(id.lastIndexOf("|") + 1);
    }

    String url = links.elementAt(0).toString();
    url = url.substring(url.indexOf("|") + 1);

    int index = url.indexOf("$SEQUENCE_ID$");
    url = url.substring(0, index) + id + url.substring(index + 13);

    try
    {
      jalview.util.BrowserLauncher.openURL(url);
    }
    catch (Exception ex)
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                            "Unixers: Couldn't find default web browser."
                                            +
          "\nAdd the full path to your browser in Preferences.",
                                            "Web browser not found",
                                            JOptionPane.WARNING_MESSAGE);
      ex.printStackTrace();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mouseEntered(MouseEvent e)
  {
    if (scrollThread != null)
    {
      scrollThread.running = false;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mouseExited(MouseEvent e)
  {
    if (av.getWrapAlignment())
    {
      return;
    }

    if (mouseDragging && (e.getY() < 0) && (av.getStartSeq() > 0))
    {
      scrollThread = new ScrollThread(true);
    }

    if (mouseDragging && (e.getY() >= getHeight()) &&
        (av.alignment.getHeight() > av.getEndSeq()))
    {
      scrollThread = new ScrollThread(false);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mousePressed(MouseEvent e)
  {
    if (e.getClickCount() == 2)
    {
      return;
    }

    int seq = alignPanel.seqPanel.findSeq(e);

    if (javax.swing.SwingUtilities.isRightMouseButton(e))
    {
      jalview.gui.PopupMenu pop = new jalview.gui.PopupMenu(alignPanel,
          (Sequence) av.getAlignment().getSequenceAt(seq),
          Preferences.sequenceURLLinks);
      pop.show(this, e.getX(), e.getY());

      return;
    }

    if ( (av.getSelectionGroup() == null) ||
        ( (!e.isControlDown() && !e.isShiftDown()) && av.getSelectionGroup() != null))
    {
      av.setSelectionGroup(new SequenceGroup());
      av.getSelectionGroup().setStartRes(0);
      av.getSelectionGroup().setEndRes(av.alignment.getWidth() - 1);
    }

    if (e.isShiftDown() && (lastid != -1))
    {
      selectSeqs(lastid, seq);
    }
    else
    {
      selectSeq(seq);
    }

    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param seq DOCUMENT ME!
   */
  void selectSeq(int seq)
  {
    lastid = seq;

    SequenceI pickedSeq = av.getAlignment().getSequenceAt(seq);
    av.getSelectionGroup().addOrRemove(pickedSeq, true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param start DOCUMENT ME!
   * @param end DOCUMENT ME!
   */
  void selectSeqs(int start, int end)
  {
    if (av.getSelectionGroup() == null)
    {
      return;
    }

    if (end >= av.getAlignment().getHeight())
    {
      end = av.getAlignment().getHeight() - 1;
    }

    lastid = start;

    if (end < start)
    {
      int tmp = start;
      start = end;
      end = tmp;
      lastid = end;
    }

    for (int i = start; i <= end; i++)
    {
      av.getSelectionGroup().addSequence(av.getAlignment().getSequenceAt(i),
                                         true);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mouseReleased(MouseEvent e)
  {
    if (scrollThread != null)
    {
      scrollThread.running = false;
    }

    mouseDragging = false;
    PaintRefresher.Refresh(this, av.getSequenceSetId());
  }

  /**
   * DOCUMENT ME!
   *
   * @param found DOCUMENT ME!
   */
  public void highlightSearchResults(java.util.Vector found)
  {
    idCanvas.setHighlighted(found);

    if (found == null)
    {
      return;
    }

    int index = av.alignment.findIndex( (SequenceI) found.get(0));

    // do we need to scroll the panel?
    if ( (av.getStartSeq() > index) || (av.getEndSeq() < index))
    {
      alignPanel.setScrollValues(av.getStartRes(), index);
    }
  }

  // this class allows scrolling off the bottom of the visible alignment
  class ScrollThread
      extends Thread
  {
    boolean running = false;
    boolean up = true;

    public ScrollThread(boolean up)
    {
      this.up = up;
      start();
    }

    public void stopScrolling()
    {
      running = false;
    }

    public void run()
    {
      running = true;

      while (running)
      {
        if (alignPanel.scrollUp(up))
        {
          // scroll was ok, so add new sequence to selection
          int seq = av.getStartSeq();

          if (!up)
          {
            seq = av.getEndSeq();
          }

          if (seq < lastid)
          {
            selectSeqs(lastid - 1, seq);
          }
          else if (seq > lastid)
          {
            selectSeqs(lastid + 1, seq);
          }

          lastid = seq;
        }
        else
        {
          running = false;
        }

        alignPanel.paintAlignment(false);

        try
        {
          Thread.sleep(100);
        }
        catch (Exception ex)
        {
        }
      }
    }
  }
}
