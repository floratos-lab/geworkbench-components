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
import javax.swing.*;

import jalview.commands.*;
import jalview.datamodel.*;
import jalview.schemes.*;
import jalview.structure.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class SeqPanel
    extends JPanel implements MouseListener,
    MouseMotionListener, MouseWheelListener, SequenceListener

{
  /** DOCUMENT ME!! */
  public SeqCanvas seqCanvas;

  /** DOCUMENT ME!! */
  public AlignmentPanel ap;
  protected int lastres;
  protected int startseq;
  protected AlignViewport av;

  ScrollThread scrollThread = null;
  boolean mouseDragging = false;
  boolean editingSeqs = false;
  boolean groupEditing = false;

  //////////////////////////////////////////
  /////Everything below this is for defining the boundary of the rubberband
  //////////////////////////////////////////
  int oldSeq = -1;
  boolean changeEndSeq = false;
  boolean changeStartSeq = false;
  boolean changeEndRes = false;
  boolean changeStartRes = false;
  SequenceGroup stretchGroup = null;
  boolean remove = false;

  Point lastMousePress;
  boolean mouseWheelPressed = false;
  StringBuffer keyboardNo1;
  StringBuffer keyboardNo2;

  java.net.URL linkImageURL;

  StringBuffer tooltipText = new StringBuffer("<html>");
  String tmpString;

  EditCommand editCommand;

  StructureSelectionManager ssm;


  /**
   * Creates a new SeqPanel object.
   *
   * @param avp DOCUMENT ME!
   * @param p DOCUMENT ME!
   */
  public SeqPanel(AlignViewport av, AlignmentPanel ap)
  {
    linkImageURL = getClass().getResource("/images/link.gif");
    ToolTipManager.sharedInstance().registerComponent(this);
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(10000);
    this.av = av;
    setBackground(Color.white);

    seqCanvas = new SeqCanvas(ap);
    setLayout(new BorderLayout());
    add(seqCanvas, BorderLayout.CENTER);

    this.ap = ap;

    if (!av.isDataset())
    {
      addMouseMotionListener(this);
      addMouseListener(this);
      addMouseWheelListener(this);
      ssm = StructureSelectionManager.getStructureSelectionManager();
      ssm.addStructureViewerListener(this);
    }
  }

  int startWrapBlock = -1;
  int wrappedBlock = -1;
  int findRes(MouseEvent evt)
  {
    int res = 0;
    int x = evt.getX();

    if (av.wrapAlignment)
    {

      int hgap = av.charHeight;
      if (av.scaleAboveWrapped)
      {
        hgap += av.charHeight;
      }

      int cHeight = av.getAlignment().getHeight() * av.charHeight
          + hgap + seqCanvas.getAnnotationHeight();

      int y = evt.getY();
      y -= hgap;
      x -= seqCanvas.LABEL_WEST;

      int cwidth = seqCanvas.getWrappedCanvasWidth(this.getWidth());
      if (cwidth < 1)
      {
        return 0;
      }

      wrappedBlock = y / cHeight;
      wrappedBlock += av.getStartRes() / cwidth;

      res = wrappedBlock * cwidth + x / av.getCharWidth();

    }
    else
    {
      res = (x / av.getCharWidth()) + av.getStartRes();
    }

    if (av.hasHiddenColumns)
    {
      res = av.getColumnSelection().adjustForHiddenColumns(res);
    }

    return res;

  }

  int findSeq(MouseEvent evt)
  {
    int seq = 0;
    int y = evt.getY();

    if (av.wrapAlignment)
    {
      int hgap = av.charHeight;
      if (av.scaleAboveWrapped)
      {
        hgap += av.charHeight;
      }

      int cHeight = av.getAlignment().getHeight() * av.charHeight
          + hgap + seqCanvas.getAnnotationHeight();

      y -= hgap;

      seq = Math.min( (y % cHeight) / av.getCharHeight(),
                     av.alignment.getHeight() - 1);
    }
    else
    {
      seq = Math.min( (y / av.getCharHeight()) + av.getStartSeq(),
                     av.alignment.getHeight() - 1);
    }

    return seq;
  }

  SequenceFeature[] findFeaturesAtRes(SequenceI sequence, int res)
  {
    Vector tmp = new Vector();
    SequenceFeature[] features = sequence.getSequenceFeatures();
    if (features != null)
    {
      for (int i = 0; i < features.length; i++)
      {
        if (av.featuresDisplayed == null
            || !av.featuresDisplayed.containsKey(features[i].getType()))
        {
          continue;
        }

        if (features[i].featureGroup != null
           && seqCanvas.fr.featureGroups!=null
            && seqCanvas.fr.featureGroups.containsKey(features[i].featureGroup)
            && !((Boolean)seqCanvas.fr.featureGroups.get(features[i].featureGroup)).booleanValue())
          continue;


        if ( (features[i].getBegin() <= res) &&
            (features[i].getEnd() >= res))
        {
          tmp.addElement(features[i]);
        }
      }
    }

    features = new SequenceFeature[tmp.size()];
    tmp.copyInto(features);

    return features;
  }


  void endEditing()
  {
    if (editCommand != null && editCommand.getSize() > 0)
    {
      ap.alignFrame.addHistoryItem(editCommand);
      av.firePropertyChange("alignment", null,
                            av.getAlignment().getSequences());
    }

    startseq = -1;
    lastres = -1;
    editingSeqs = false;
    groupEditing = false;
    keyboardNo1 = null;
    keyboardNo2 = null;
    editCommand = null;
  }

  void setCursorRow()
  {
    seqCanvas.cursorY = getKeyboardNo1() - 1;
    scrollToVisible();
  }

  void setCursorColumn()
  {
    seqCanvas.cursorX = getKeyboardNo1() - 1;
    scrollToVisible();
  }

  void setCursorRowAndColumn()
  {
    if (keyboardNo2 == null)
    {
      keyboardNo2 = new StringBuffer();
    }
    else
    {
      seqCanvas.cursorX = getKeyboardNo1() - 1;
      seqCanvas.cursorY = getKeyboardNo2() - 1;
      scrollToVisible();
    }
  }

  void setCursorPosition()
  {
    SequenceI sequence =
        (Sequence) av.getAlignment().getSequenceAt(seqCanvas.cursorY);

    seqCanvas.cursorX = sequence.findIndex(
        getKeyboardNo1() - 1
        );
    scrollToVisible();
  }

  void moveCursor(int dx, int dy)
  {
    seqCanvas.cursorX += dx;
    seqCanvas.cursorY += dy;
    if (av.hasHiddenColumns && !av.colSel.isVisible(seqCanvas.cursorX))
    {
      int original = seqCanvas.cursorX - dx;
      int maxWidth = av.alignment.getWidth();

      while (!av.colSel.isVisible(seqCanvas.cursorX)
             && seqCanvas.cursorX < maxWidth
             && seqCanvas.cursorX > 0)
      {
        seqCanvas.cursorX += dx;
      }

      if (seqCanvas.cursorX >= maxWidth
          || !av.colSel.isVisible(seqCanvas.cursorX))
      {
        seqCanvas.cursorX = original;
      }
    }

    scrollToVisible();
  }

  void scrollToVisible()
  {
    if (seqCanvas.cursorX < 0)
    {
      seqCanvas.cursorX = 0;
    }
    else if (seqCanvas.cursorX > av.alignment.getWidth() - 1)
    {
      seqCanvas.cursorX = av.alignment.getWidth() - 1;
    }

    if (seqCanvas.cursorY < 0)
    {
      seqCanvas.cursorY = 0;
    }
    else if (seqCanvas.cursorY > av.alignment.getHeight() - 1)
    {
      seqCanvas.cursorY = av.alignment.getHeight() - 1;
    }

    endEditing();
    if (av.wrapAlignment)
    {
      ap.scrollToWrappedVisible(seqCanvas.cursorX);
    }
    else
    {
      while (seqCanvas.cursorY < av.startSeq)
      {
        ap.scrollUp(true);
      }
      while (seqCanvas.cursorY + 1 > av.endSeq)
      {
        ap.scrollUp(false);
      }
      if (!av.wrapAlignment)
      {
        while (seqCanvas.cursorX < av.colSel.adjustForHiddenColumns(av.startRes))
        {
          if (!ap.scrollRight(false))
          {
            break;
          }
        }
        while (seqCanvas.cursorX > av.colSel.adjustForHiddenColumns(av.endRes))
        {
          if (!ap.scrollRight(true))
          {
            break;
          }
        }
      }
    }
    setStatusMessage(av.alignment.getSequenceAt(seqCanvas.cursorY),
                     seqCanvas.cursorX, seqCanvas.cursorY);

    seqCanvas.repaint();
  }

  void setSelectionAreaAtCursor(boolean topLeft)
  {
    SequenceI sequence =
        (Sequence) av.getAlignment().getSequenceAt(seqCanvas.cursorY);

    if (av.getSelectionGroup() != null)
    {
      SequenceGroup sg = av.selectionGroup;
      //Find the top and bottom of this group
      int min = av.alignment.getHeight(), max = 0;
      for (int i = 0; i < sg.getSize(); i++)
      {
        int index = av.alignment.findIndex(sg.getSequenceAt(i));
        if (index > max)
        {
          max = index;
        }
        if (index < min)
        {
          min = index;
        }
      }

      max++;

      if (topLeft)
      {
        sg.setStartRes(seqCanvas.cursorX);
        if (sg.getEndRes() < seqCanvas.cursorX)
        {
          sg.setEndRes(seqCanvas.cursorX);
        }

        min = seqCanvas.cursorY;
      }
      else
      {
        sg.setEndRes(seqCanvas.cursorX);
        if (sg.getStartRes() > seqCanvas.cursorX)
        {
          sg.setStartRes(seqCanvas.cursorX);
        }

        max = seqCanvas.cursorY + 1;
      }

      if (min > max)
      {
        // Only the user can do this
        av.setSelectionGroup(null);
      }
      else
      {
        // Now add any sequences between min and max
        sg.getSequences(null).clear();
        for (int i = min; i < max; i++)
        {
          sg.addSequence(av.alignment.getSequenceAt(i), false);
        }
      }
    }

    if (av.getSelectionGroup() == null)
    {
      SequenceGroup sg = new SequenceGroup();
      sg.setStartRes(seqCanvas.cursorX);
      sg.setEndRes(seqCanvas.cursorX);
      sg.addSequence(sequence, false);
      av.setSelectionGroup(sg);
    }

    ap.paintAlignment(false);
  }

  void insertGapAtCursor(boolean group)
  {
    groupEditing = group;
    startseq = seqCanvas.cursorY;
    lastres = seqCanvas.cursorX;
    editSequence(true, seqCanvas.cursorX + getKeyboardNo1());
    endEditing();
  }

  void deleteGapAtCursor(boolean group)
  {
    groupEditing = group;
    startseq = seqCanvas.cursorY;
    lastres = seqCanvas.cursorX + getKeyboardNo1();
    editSequence(false, seqCanvas.cursorX);
    endEditing();
  }

  void numberPressed(char value)
  {
    if (keyboardNo1 == null)
    {
      keyboardNo1 = new StringBuffer();
    }

    if (keyboardNo2 != null)
    {
      keyboardNo2.append(value);
    }
    else
    {
      keyboardNo1.append(value);
    }
  }

  int getKeyboardNo1()
  {
    if (keyboardNo1 == null)
      return 1;
    else
    {
      int value = Integer.parseInt(keyboardNo1.toString());
      keyboardNo1 = null;
      return value;
    }
  }

  int getKeyboardNo2()
  {
    if (keyboardNo2 == null)
      return 1;
    else
    {
      int value = Integer.parseInt(keyboardNo2.toString());
      keyboardNo2 = null;
      return value;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseReleased(MouseEvent evt)
  {
    mouseDragging = false;
    mouseWheelPressed = false;

    if (!editingSeqs)
    {
      doMouseReleasedDefineMode(evt);
      return;
    }

    endEditing();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mousePressed(MouseEvent evt)
  {
    lastMousePress = evt.getPoint();

    if (javax.swing.SwingUtilities.isMiddleMouseButton(evt))
    {
      mouseWheelPressed = true;
      return;
    }

    if (evt.isShiftDown() || evt.isAltDown() ||
        evt.isControlDown())
    {
      if (evt.isAltDown() || evt.isControlDown())
      {
        groupEditing = true;
      }
      editingSeqs = true;
    }
    else
    {
      doMousePressedDefineMode(evt);
      return;
    }

    int seq = findSeq(evt);
    int res = findRes(evt);

    if (seq < 0 || res < 0)
    {
      return;
    }

    if ( (seq < av.getAlignment().getHeight()) &&
        (res < av.getAlignment().getSequenceAt(seq).getLength()))
    {
      startseq = seq;
      lastres = res;
    }
    else
    {
      startseq = -1;
      lastres = -1;
    }

    return;
  }

  String lastMessage;
  public void mouseOverSequence(SequenceI sequence, int index)
  {
    String tmp = sequence.hashCode()+index+"";
    if (lastMessage == null || !lastMessage.equals(tmp))
      ssm.mouseOverSequence(sequence, index);

    lastMessage = tmp;
  }


  public void highlightSequence(jalview.datamodel.SequenceI seq, int index)
  {
    if(av.alignment.findIndex(seq)>-1)
    {
      SearchResults highlight = new SearchResults();
      highlight.addResult(seq,index,index);
      seqCanvas.highlightSearchResults(highlight);
    }
  }

  public void updateColours(SequenceI seq, int index)
  {
    System.out.println("update the seqPanel colours");
    //repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseMoved(MouseEvent evt)
  {
    if (editingSeqs)
    {
      // This is because MacOSX creates a mouseMoved
      // If control is down, other platforms will not.
      mouseDragged(evt);
    }

    int res = findRes(evt);
    int seq = findSeq(evt);

    if (res < 0 || seq < 0 || seq >= av.getAlignment().getHeight())
    {
      return;
    }

    SequenceI sequence = av.getAlignment().getSequenceAt(seq);

    if (res > sequence.getLength())
    {
      return;
    }

    if (ssm != null)
      mouseOverSequence(sequence, sequence.findPosition(res));

    setStatusMessage(sequence, res, seq);

    tooltipText.setLength(6); // Cuts the buffer back to <html>

    SequenceGroup[] groups = av.alignment.findAllGroups(sequence);
    if (groups != null)
    {
      for (int g = 0; g < groups.length; g++)
      {
        if (groups[g].getStartRes() <= res && groups[g].getEndRes() >= res)
        {
          if (tooltipText.length() > 6)
          {
            tooltipText.append("<br>");
          }

          if (!groups[g].getName().startsWith("JTreeGroup") &&
              !groups[g].getName().startsWith("JGroup"))
          {
            tooltipText.append(groups[g].getName());
          }

          if (groups[g].getDescription() != null)
          {
            tooltipText.append(": " + groups[g].getDescription());
          }
        }
      }
    }

    // use aa to see if the mouse pointer is on a
    if (av.showSequenceFeatures)
    {
      SequenceFeature[] features = findFeaturesAtRes(
          sequence.getDatasetSequence(), sequence.findPosition(res));

      if (features != null)
      {
        for (int i = 0; i < features.length; i++)
        {
          if (features[i].getType().equals("disulfide bond"))
          {
            if (features[i].getBegin() == sequence.findPosition(res)
                || features[i].getEnd() == sequence.findPosition(res))
            {
              if (tooltipText.length() > 6)
              {
                tooltipText.append("<br>");
              }
              tooltipText.append("disulfide bond " + features[i].getBegin() +
                                 ":" +
                                 features[i].getEnd());
              if (features[i].links != null)
              {
                tooltipText.append(" <img src=\"" + linkImageURL + "\">");
              }
            }
          }
          else
          {
            if (tooltipText.length() > 6)
            {
              tooltipText.append("<br>");
            }

            tooltipText.append(features[i].getType() + " " +
                               features[i].begin);
            if (features[i].begin != features[i].end)
            {
              tooltipText.append(" " + features[i].end);
            }

            if (features[i].getDescription() != null
                && !features[i].description.equals(features[i].getType()))
            {
              tmpString = features[i].getDescription();
              int startTag = tmpString.toUpperCase().indexOf("<HTML>");
              if (startTag > -1)
              {
                tmpString = tmpString.substring(startTag + 6);
              }
              int endTag = tmpString.toUpperCase().indexOf("</BODY>");
              if (endTag > -1)
              {
                tmpString = tmpString.substring(0, endTag);
              }
              endTag = tmpString.toUpperCase().indexOf("</HTML>");
              if (endTag > -1)
              {
                tmpString = tmpString.substring(0, endTag);
              }

              if (startTag > -1)
              {
                tooltipText.append("; " + tmpString);
              }
              else
              {
                if (tmpString.indexOf("<") > -1 || tmpString.indexOf(">") > -1)
                {
                  //The description does not specify html is to
                  //be used, so we must remove < > symbols
                  tmpString = tmpString.replaceAll("<", "&lt;");
                  tmpString = tmpString.replaceAll(">", "&gt;");

                  tooltipText.append("; ");
                  tooltipText.append(tmpString);

                }
                else
                {
                  tooltipText.append("; " + tmpString);
                }
              }
            }
            if (features[i].getValue("status") != null)
            {
              String status = features[i].getValue("status").toString();
              if (status.length() > 0)
              {
                tooltipText.append("; (" + features[i].getValue("status") + ")");
              }
            }

            if (features[i].links != null)
            {
              tooltipText.append(" <img src=\"" + linkImageURL + "\">");
            }

          }
        }
      }
    }

    if (tooltipText.length() == 6) // <html></html>
    {
      setToolTipText(null);
    }
    else
    {
      tooltipText.append("</html>");
      if (lastTooltip == null || !lastTooltip.equals(tooltipText.toString()))
        setToolTipText(tooltipText.toString());

      lastTooltip = tooltipText.toString();
    }

  }
  String lastTooltip;

  void setStatusMessage(SequenceI sequence, int res, int seq)
  {
    StringBuffer text = new StringBuffer("Sequence " + (seq + 1) + " ID: " +
                                         sequence.getName());

    Object obj = null;
    if (av.alignment.isNucleotide())
    {
      obj = ResidueProperties.nucleotideName.get(sequence.getCharAt(res) +
                                                 "");
      if (obj != null)
      {
        text.append(" Nucleotide: ");
      }
    }
    else
    {
      obj = ResidueProperties.aa2Triplet.get(sequence.getCharAt(res) + "");
      if (obj != null)
      {
        text.append("  Residue: ");
      }
    }

    if (obj != null)
    {

      if (obj != "")
      {
        text.append(obj + " (" + sequence.findPosition(res) +
                    ")");
      }
    }
    ap.alignFrame.statusBar.setText(text.toString());

  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseDragged(MouseEvent evt)
  {
    if (mouseWheelPressed)
    {
      int oldWidth = av.charWidth;

      //Which is bigger, left-right or up-down?
      if (Math.abs(evt.getY() - lastMousePress.getY())
          > Math.abs(evt.getX() - lastMousePress.getX()))
      {
        int fontSize = av.font.getSize();

        if (evt.getY() < lastMousePress.getY())
        {
          fontSize--;
        }
        else if (evt.getY() > lastMousePress.getY())
        {
          fontSize++;
        }

        if (fontSize < 1)
        {
          fontSize = 1;
        }

        av.setFont(new Font(av.font.getName(), av.font.getStyle(), fontSize));
        av.charWidth = oldWidth;
        ap.fontChanged();
      }
      else
      {
        if (evt.getX() < lastMousePress.getX() && av.charWidth > 1)
        {
          av.charWidth--;
        }
        else if (evt.getX() > lastMousePress.getX())
        {
          av.charWidth++;
        }

        ap.paintAlignment(false);
      }

      FontMetrics fm = getFontMetrics(av.getFont());
      av.validCharWidth = fm.charWidth('M') <= av.charWidth;

      lastMousePress = evt.getPoint();

      return;
    }

    if (!editingSeqs)
    {
      doMouseDraggedDefineMode(evt);
      return;
    }

    int res = findRes(evt);

    if (res < 0)
    {
      res = 0;
    }

    if ( (lastres == -1) || (lastres == res))
    {
      return;
    }

    if ( (res < av.getAlignment().getWidth()) && (res < lastres))
    {
      // dragLeft, delete gap
      editSequence(false, res);
    }
    else
    {
      editSequence(true, res);
    }

    mouseDragging = true;
    if (scrollThread != null)
    {
      scrollThread.setEvent(evt);
    }
  }



  synchronized void editSequence(boolean insertGap, int startres)
  {
    int fixedLeft = -1;
    int fixedRight = -1;
    boolean fixedColumns = false;
    SequenceGroup sg = av.getSelectionGroup();

    SequenceI seq = av.alignment.getSequenceAt(startseq);

    //No group, but the sequence may represent a group
    if (!groupEditing && av.hasHiddenRows)
    {
      if (av.hiddenRepSequences != null
          && av.hiddenRepSequences.containsKey(seq))
      {
        sg = (SequenceGroup) av.hiddenRepSequences.get(seq);
        groupEditing = true;
      }
    }

    StringBuffer message = new StringBuffer();
    if (groupEditing)
    {
      message.append("Edit group:");
      if (editCommand == null)
      {
        editCommand = new EditCommand("Edit Group");
      }
    }
    else
    {
      message.append("Edit sequence: " + seq.getName());
      String label = seq.getName();
      if (label.length() > 10)
      {
        label = label.substring(0, 10);
      }
      if (editCommand == null)
      {
        editCommand = new EditCommand("Edit " + label);
      }
    }

    if (insertGap)
    {
      message.append(" insert ");
    }
    else
    {
      message.append(" delete ");
    }

    message.append(Math.abs(startres - lastres) + " gaps.");
    ap.alignFrame.statusBar.setText(message.toString());

    //Are we editing within a selection group?
    if (groupEditing
        || (sg != null && sg.getSequences(av.hiddenRepSequences).contains(seq)))
    {
      fixedColumns = true;

      //sg might be null as the user may only see 1 sequence,
      //but the sequence represents a group
      if (sg == null)
      {
        if (av.hiddenRepSequences == null
            || !av.hiddenRepSequences.containsKey(seq))
        {
          endEditing();
          return;
        }
        sg = (SequenceGroup) av.hiddenRepSequences.get(seq);
      }

      fixedLeft = sg.getStartRes();
      fixedRight = sg.getEndRes();

      if ( (startres < fixedLeft && lastres >= fixedLeft)
          || (startres >= fixedLeft && lastres < fixedLeft)
          || (startres > fixedRight && lastres <= fixedRight)
          || (startres <= fixedRight && lastres > fixedRight))
      {
        endEditing();
        return;
      }

      if (fixedLeft > startres)
      {
        fixedRight = fixedLeft - 1;
        fixedLeft = 0;
      }
      else if (fixedRight < startres)
      {
        fixedLeft = fixedRight;
        fixedRight = -1;
      }
    }

    if (av.hasHiddenColumns)
    {
      fixedColumns = true;
      int y1 = av.getColumnSelection().getHiddenBoundaryLeft(startres);
      int y2 = av.getColumnSelection().getHiddenBoundaryRight(startres);

      if ( (insertGap && startres > y1 && lastres < y1)
          || (!insertGap && startres < y2 && lastres > y2))
      {
        endEditing();
        return;
      }

      //System.out.print(y1+" "+y2+" "+fixedLeft+" "+fixedRight+"~~");
      //Selection spans a hidden region
      if (fixedLeft < y1 && (fixedRight > y2 || fixedRight == -1))
      {
        if (startres >= y2)
        {
          fixedLeft = y2;
        }
        else
        {
          fixedRight = y2 - 1;
        }
      }
    }

    if (groupEditing)
    {
      Vector vseqs = sg.getSequences(av.hiddenRepSequences);
      int g, groupSize = vseqs.size();
      SequenceI[] groupSeqs = new SequenceI[groupSize];
      for (g = 0; g < groupSeqs.length; g++)
      {
        groupSeqs[g] = (SequenceI) vseqs.elementAt(g);
      }

      // drag to right
      if (insertGap)
      {
        //If the user has selected the whole sequence, and is dragging to
        // the right, we can still extend the alignment and selectionGroup
        if (sg.getStartRes() == 0
            && sg.getEndRes() == fixedRight
            && sg.getEndRes() == av.alignment.getWidth() - 1
            )
        {
          sg.setEndRes(av.alignment.getWidth() + startres - lastres);
          fixedRight = sg.getEndRes();
        }

        // Is it valid with fixed columns??
        // Find the next gap before the end
        // of the visible region boundary
        boolean blank = false;
        for (fixedRight = fixedRight;
             fixedRight > lastres;
             fixedRight--)
        {
          blank = true;

          for (g = 0; g < groupSize; g++)
          {
            for (int j = 0; j < startres - lastres; j++)
            {
              if (!jalview.util.Comparison.isGap(
                  groupSeqs[g].getCharAt(fixedRight - j)))
              {
                blank = false;
                break;
              }
            }
          }
          if (blank)
          {
            break;
          }
        }

        if (!blank)
        {
          if (sg.getSize() == av.alignment.getHeight())
          {
            if ( (av.hasHiddenColumns
                  &&
                  startres < av.getColumnSelection().getHiddenBoundaryRight(startres)))
            {
              endEditing();
              return;
            }

            int alWidth = av.alignment.getWidth();
            if (av.hasHiddenRows)
            {
              int hwidth = av.alignment.getHiddenSequences().getWidth();
              if (hwidth > alWidth)
              {
                alWidth = hwidth;
              }
            }
            //We can still insert gaps if the selectionGroup
            //contains all the sequences
            sg.setEndRes(sg.getEndRes() + startres - lastres);
            fixedRight = alWidth + startres - lastres;
          }
          else
          {
            endEditing();
            return;
          }
        }
      }

      // drag to left
      else if (!insertGap)
      {
        /// Are we able to delete?
        // ie are all columns blank?

        for (g = 0; g < groupSize; g++)
        {
          for (int j = startres; j < lastres; j++)
          {
            if (groupSeqs[g].getLength() <= j)
            {
              continue;
            }

            if (!jalview.util.Comparison.isGap(
                groupSeqs[g].getCharAt(j)))
            {
              // Not a gap, block edit not valid
              endEditing();
              return;
            }
          }
        }
      }

      if (insertGap)
      {
        // dragging to the right
        if (fixedColumns && fixedRight != -1)
        {
          for (int j = lastres; j < startres; j++)
          {
            insertChar(j, groupSeqs, fixedRight);
          }
        }
        else
        {
          editCommand.appendEdit(EditCommand.INSERT_GAP,
                                 groupSeqs,
                                 startres, startres - lastres,
                                 av.alignment,
                                 true);
        }
      }
      else
      {
        // dragging to the left
        if (fixedColumns && fixedRight != -1)
        {
          for (int j = lastres; j > startres; j--)
          {
            deleteChar(startres, groupSeqs, fixedRight);
          }
        }
        else
        {
          editCommand.appendEdit(EditCommand.DELETE_GAP,
                                 groupSeqs,
                                 startres, lastres - startres,
                                 av.alignment,
                                 true);
        }

      }
    }
    else /////Editing a single sequence///////////
    {
      if (insertGap)
      {
        // dragging to the right
        if (fixedColumns && fixedRight != -1)
        {
          for (int j = lastres; j < startres; j++)
          {
            insertChar(j, new SequenceI[]
                       {seq}, fixedRight);
          }
        }
        else
        {
          editCommand.appendEdit(EditCommand.INSERT_GAP,
                                 new SequenceI[]
                                 {seq},
                                 lastres, startres - lastres,
                                 av.alignment,
                                 true);
        }
      }
      else
      {
        // dragging to the left
        if (fixedColumns && fixedRight != -1)
        {
          for (int j = lastres; j > startres; j--)
          {
            if (!jalview.util.Comparison.isGap(seq.getCharAt(startres)))
            {
              endEditing();
              break;
            }
            deleteChar(startres, new SequenceI[]
                       {seq}, fixedRight);
          }
        }
        else
        {
          //could be a keyboard edit trying to delete none gaps
          int max = 0;
          for (int m = startres; m < lastres; m++)
          {
            if (!jalview.util.Comparison.isGap(seq.getCharAt(m)))
            {
              break;
            }
            max++;
          }

          if (max > 0)
          {
            editCommand.appendEdit(EditCommand.DELETE_GAP,
                                   new SequenceI[]
                                   {seq},
                                   startres, max,
                                   av.alignment,
                                   true);
          }
        }
      }
    }

    lastres = startres;
    seqCanvas.repaint();
  }

  void insertChar(int j, SequenceI[] seq, int fixedColumn)
  {
    int blankColumn = fixedColumn;
    for (int s = 0; s < seq.length; s++)
    {
      //Find the next gap before the end of the visible region boundary
      //If lastCol > j, theres a boundary after the gap insertion

      for (blankColumn = fixedColumn; blankColumn > j; blankColumn--)
      {
        if (jalview.util.Comparison.isGap(seq[s].getCharAt(blankColumn)))
        {
          //Theres a space, so break and insert the gap
          break;
        }
      }

      if (blankColumn <= j)
      {
        blankColumn = fixedColumn;
        endEditing();
        return;
      }
    }

    editCommand.appendEdit(EditCommand.DELETE_GAP,
                           seq,
                           blankColumn, 1, av.alignment, true);

    editCommand.appendEdit(EditCommand.INSERT_GAP,
                           seq,
                           j, 1, av.alignment,
                           true);

  }

  void deleteChar(int j, SequenceI[] seq, int fixedColumn)
  {

    editCommand.appendEdit(EditCommand.DELETE_GAP,
                           seq,
                           j, 1, av.alignment, true);

    editCommand.appendEdit(EditCommand.INSERT_GAP,
                           seq,
                           fixedColumn, 1, av.alignment, true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void mouseEntered(MouseEvent e)
  {
    if (oldSeq < 0)
    {
      oldSeq = 0;
    }

    if (scrollThread != null)
    {
      scrollThread.running = false;
      scrollThread = null;
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

    if (mouseDragging)
    {
      scrollThread = new ScrollThread();
    }
  }

  public void mouseClicked(MouseEvent evt)
  {
    SequenceI sequence = av.alignment.getSequenceAt(findSeq(evt));
    if (evt.getClickCount() > 1)
    {
      if (av.getSelectionGroup().getSize() == 1
          && av.getSelectionGroup().getEndRes()
          - av.getSelectionGroup().getStartRes() < 2)
      {
        av.setSelectionGroup(null);
      }

      SequenceFeature[] features = findFeaturesAtRes(
          sequence.getDatasetSequence(),
          sequence.findPosition(findRes(evt))
          );

      if (features != null && features.length > 0)
      {
        SearchResults highlight = new SearchResults();
        highlight.addResult(sequence,
                            features[0].getBegin(),
                            features[0].getEnd());
        seqCanvas.highlightSearchResults(highlight);
      }
      if (features != null && features.length > 0)
      {
        seqCanvas.getFeatureRenderer().amendFeatures(
            new SequenceI[]
            {sequence}, features, false, ap);

        seqCanvas.highlightSearchResults(null);
      }
    }
  }

  public void mouseWheelMoved(MouseWheelEvent e)
  {
    e.consume();
      if (e.getWheelRotation() > 0)
      {
        ap.scrollUp(false);
      }
      else
      {
        ap.scrollUp(true);
      }
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void doMousePressedDefineMode(MouseEvent evt)
  {
    int res = findRes(evt);
    int seq = findSeq(evt);
    oldSeq = seq;

    startWrapBlock = wrappedBlock;

    if (av.wrapAlignment && seq > av.alignment.getHeight())
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                            "Cannot edit annotations in wrapped view.",
                                            "Wrapped view - no edit",
                                            JOptionPane.WARNING_MESSAGE);
      return;
    }

    if (seq < 0 || res < 0)
    {
      return;
    }

    SequenceI sequence = (Sequence) av.getAlignment().getSequenceAt(seq);

    if ( (sequence == null) || (res > sequence.getLength()))
    {
      return;
    }

    stretchGroup = av.getSelectionGroup();

    if (stretchGroup == null)
    {
      stretchGroup = av.alignment.findGroup(sequence);

      if ( (stretchGroup != null) && (res > stretchGroup.getStartRes()) &&
          (res < stretchGroup.getEndRes()))
      {
        av.setSelectionGroup(stretchGroup);
      }
      else
      {
        stretchGroup = null;
      }
    }
    else if (!stretchGroup.getSequences(null).contains(sequence) ||
             (stretchGroup.getStartRes() > res) ||
             (stretchGroup.getEndRes() < res))
    {
      stretchGroup = null;

      SequenceGroup[] allGroups = av.alignment.findAllGroups(sequence);

      if (allGroups != null)
      {
        for (int i = 0; i < allGroups.length; i++)
        {
          if ( (allGroups[i].getStartRes() <= res) &&
              (allGroups[i].getEndRes() >= res))
          {
            stretchGroup = allGroups[i];
            break;
          }
        }
      }

      av.setSelectionGroup(stretchGroup);

    }

    if (javax.swing.SwingUtilities.isRightMouseButton(evt))
    {
      SequenceFeature [] allFeatures = findFeaturesAtRes(sequence.getDatasetSequence(),
                                               sequence.findPosition(res));
      Vector links = new Vector();
      for (int i = 0; i < allFeatures.length; i++)
      {
        if (allFeatures[i].links != null)
        {
          for (int j = 0; j < allFeatures[i].links.size(); j++)
          {
            links.addElement(allFeatures[i].links.elementAt(j));
          }
        }
      }

      jalview.gui.PopupMenu pop = new jalview.gui.PopupMenu(ap, null, links);
      pop.show(this, evt.getX(), evt.getY());
      return;
    }

    if (av.cursorMode)
    {
      seqCanvas.cursorX = findRes(evt);
      seqCanvas.cursorY = findSeq(evt);
      seqCanvas.repaint();
      return;
    }

    if (stretchGroup == null)
    {
      //Only if left mouse button do we want to change group sizes

      // define a new group here
      SequenceGroup sg = new SequenceGroup();
      sg.setStartRes(res);
      sg.setEndRes(res);
      sg.addSequence(sequence, false);
      av.setSelectionGroup(sg);
      stretchGroup = sg;

      if (av.getConservationSelected())
      {
        SliderPanel.setConservationSlider(ap,
                                          av.getGlobalColourScheme(),
                                          "Background");
      }

      if (av.getAbovePIDThreshold())
      {
        SliderPanel.setPIDSliderSource(ap, av.getGlobalColourScheme(),
                                       "Background");
      }
      if ( (stretchGroup != null) && (stretchGroup.getEndRes() == res))
      {
        // Edit end res position of selected group
        changeEndRes = true;
      }
      else if ( (stretchGroup != null) &&
               (stretchGroup.getStartRes() == res))
      {
        // Edit end res position of selected group
        changeStartRes = true;
      }
      stretchGroup.getWidth();
    }

    seqCanvas.repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void doMouseReleasedDefineMode(MouseEvent evt)
  {
    if (stretchGroup == null)
    {
      return;
    }

    if (stretchGroup.cs != null)
    {
      if (stretchGroup.cs instanceof ClustalxColourScheme)
      {
        ( (ClustalxColourScheme) stretchGroup.cs).resetClustalX(
            stretchGroup.getSequences(av.hiddenRepSequences),
            stretchGroup.getWidth());
      }

      if (stretchGroup.cs instanceof Blosum62ColourScheme
          || stretchGroup.cs instanceof PIDColourScheme
          || stretchGroup.cs.conservationApplied()
          || stretchGroup.cs.getThreshold() > 0)
      {
        stretchGroup.recalcConservation();
      }

      if (stretchGroup.cs.conservationApplied())
      {
        SliderPanel.setConservationSlider(ap, stretchGroup.cs,
                                          stretchGroup.getName());
      }
      else
      {
        SliderPanel.setPIDSliderSource(ap, stretchGroup.cs,
                                       stretchGroup.getName());
      }
      PaintRefresher.Refresh(this, av.getSequenceSetId());
      ap.paintAlignment(true);
    }

    changeEndRes = false;
    changeStartRes = false;
    stretchGroup = null;

  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void doMouseDraggedDefineMode(MouseEvent evt)
  {
    int res = findRes(evt);
    int y = findSeq(evt);

    if (wrappedBlock != startWrapBlock)
    {
      return;
    }

    if (stretchGroup == null)
    {
      return;
    }

    if (res >= av.alignment.getWidth())
    {
      res = av.alignment.getWidth() - 1;
    }

    if (stretchGroup.getEndRes() == res)
    {
      // Edit end res position of selected group
      changeEndRes = true;
    }
    else if (stretchGroup.getStartRes() == res)
    {
      // Edit start res position of selected group
      changeStartRes = true;
    }

    if (res < av.getStartRes())
    {
      res = av.getStartRes();
    }

    if (changeEndRes)
    {
      if (res > (stretchGroup.getStartRes() - 1))
      {
        stretchGroup.setEndRes(res);
      }
    }
    else if (changeStartRes)
    {
      if (res < (stretchGroup.getEndRes() + 1))
      {
        stretchGroup.setStartRes(res);
      }
    }

    int dragDirection = 0;

    if (y > oldSeq)
    {
      dragDirection = 1;
    }
    else if (y < oldSeq)
    {
      dragDirection = -1;
    }

    while ( (y != oldSeq) && (oldSeq > -1) && (y < av.alignment.getHeight()))
    {
      // This routine ensures we don't skip any sequences, as the
      // selection is quite slow.
      Sequence seq = (Sequence) av.getAlignment().getSequenceAt(oldSeq);

      oldSeq += dragDirection;

      if (oldSeq < 0)
      {
        break;
      }

      Sequence nextSeq = (Sequence) av.getAlignment().getSequenceAt(oldSeq);

      if (stretchGroup.getSequences(null).contains(nextSeq))
      {
        stretchGroup.deleteSequence(seq, false);
      }
      else
      {
        if (seq != null)
        {
          stretchGroup.addSequence(seq, false);
        }

        stretchGroup.addSequence(nextSeq, false);
      }
    }

    if (oldSeq < 0)
    {
      oldSeq = -1;
    }

    mouseDragging = true;

    if (scrollThread != null)
    {
      scrollThread.setEvent(evt);
    }

    seqCanvas.repaint();
  }

  void scrollCanvas(MouseEvent evt)
  {
    if (evt == null)
    {
      if (scrollThread != null)
      {
        scrollThread.running = false;
        scrollThread = null;
      }
      mouseDragging = false;
    }
    else
    {
      if (scrollThread == null)
      {
        scrollThread = new ScrollThread();
      }

      mouseDragging = true;
      scrollThread.setEvent(evt);
    }

  }

  // this class allows scrolling off the bottom of the visible alignment
  class ScrollThread
      extends Thread
  {
    MouseEvent evt;
    boolean running = false;

    public ScrollThread()
    {
      start();
    }

    public void setEvent(MouseEvent e)
    {
      evt = e;
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
        if (evt != null)
        {
          if (mouseDragging && (evt.getY() < 0) &&
              (av.getStartSeq() > 0))
          {
            running = ap.scrollUp(true);
          }

          if (mouseDragging && (evt.getY() >= getHeight()) &&
              (av.alignment.getHeight() > av.getEndSeq()))
          {
            running = ap.scrollUp(false);
          }

          if (mouseDragging && (evt.getX() < 0))
          {
            running = ap.scrollRight(false);
          }
          else if (mouseDragging && (evt.getX() >= getWidth()))
          {
            running = ap.scrollRight(true);
          }
        }

        try
        {
          Thread.sleep(20);
        }
        catch (Exception ex)
        {
        }
      }
    }
  }
}
