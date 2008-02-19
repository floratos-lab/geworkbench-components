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
package jalview.jbgui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class GAlignmentPanel
    extends JPanel
{
  protected JPanel sequenceHolderPanel = new JPanel();
  protected JScrollBar vscroll = new JScrollBar();
  protected JScrollBar hscroll = new JScrollBar();
  protected JPanel seqPanelHolder = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout3 = new BorderLayout();
  protected JPanel scalePanelHolder = new JPanel();
  protected JPanel idPanelHolder = new JPanel();
  BorderLayout borderLayout5 = new BorderLayout();
  protected JPanel idSpaceFillerPanel1 = new JPanel();
  public JPanel annotationSpaceFillerHolder = new JPanel();
  BorderLayout borderLayout6 = new BorderLayout();
  ButtonGroup buttonGroup1 = new ButtonGroup();
  BorderLayout borderLayout7 = new BorderLayout();
  JPanel hscrollHolder = new JPanel();
  BorderLayout borderLayout10 = new BorderLayout();
  protected JPanel hscrollFillerPanel = new JPanel();
  BorderLayout borderLayout11 = new BorderLayout();
  public JScrollPane annotationScroller = new JScrollPane();
  Border border1;
  BorderLayout borderLayout4 = new BorderLayout();

  public GAlignmentPanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
      throws Exception
  {
    border1 = BorderFactory.createLineBorder(Color.gray, 1);
    idPanelHolder.setBorder(null);
    idPanelHolder.setPreferredSize(new Dimension(70, 10));
    this.setLayout(borderLayout7);
    sequenceHolderPanel.setMaximumSize(new Dimension(2147483647, 2147483647));
    sequenceHolderPanel.setMinimumSize(new Dimension(150, 150));
    sequenceHolderPanel.setPreferredSize(new Dimension(150, 150));
    sequenceHolderPanel.setLayout(borderLayout3);
    seqPanelHolder.setLayout(borderLayout1);
    scalePanelHolder.setBackground(Color.white);
    scalePanelHolder.setMinimumSize(new Dimension(10, 80));
    scalePanelHolder.setPreferredSize(new Dimension(10, 30));
    scalePanelHolder.setLayout(borderLayout6);
    idPanelHolder.setLayout(borderLayout5);
    idSpaceFillerPanel1.setBackground(Color.white);
    idSpaceFillerPanel1.setPreferredSize(new Dimension(10, 30));
    idSpaceFillerPanel1.setLayout(borderLayout11);
    annotationSpaceFillerHolder.setBackground(Color.white);
    annotationSpaceFillerHolder.setPreferredSize(new Dimension(10, 80));
    annotationSpaceFillerHolder.setLayout(borderLayout4);
    hscroll.setOrientation(JScrollBar.HORIZONTAL);
    hscrollHolder.setLayout(borderLayout10);
    hscrollFillerPanel.setBackground(Color.white);
    hscrollFillerPanel.setPreferredSize(new Dimension(70, 10));
    hscrollHolder.setBackground(Color.white);
    annotationScroller.setBorder(null);
    annotationScroller.setPreferredSize(new Dimension(10, 80));
    this.setPreferredSize(new Dimension(220, 166));

    sequenceHolderPanel.add(scalePanelHolder, BorderLayout.NORTH);
    sequenceHolderPanel.add(seqPanelHolder, BorderLayout.CENTER);
    seqPanelHolder.add(vscroll, BorderLayout.EAST);
    sequenceHolderPanel.add(annotationScroller, BorderLayout.SOUTH);

    //  jPanel3.add(secondaryPanelHolder,  BorderLayout.SOUTH);
    this.add(idPanelHolder, BorderLayout.WEST);
    idPanelHolder.add(idSpaceFillerPanel1, BorderLayout.NORTH);
    idPanelHolder.add(annotationSpaceFillerHolder, BorderLayout.SOUTH);
    this.add(hscrollHolder, BorderLayout.SOUTH);
    hscrollHolder.add(hscroll, BorderLayout.CENTER);
    hscrollHolder.add(hscrollFillerPanel, BorderLayout.WEST);
    this.add(sequenceHolderPanel, BorderLayout.CENTER);
  }
}
