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
import java.awt.event.*;
import javax.swing.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class GSliderPanel
    extends JPanel
{
  // this is used for conservation colours, PID colours and redundancy threshold
  protected JSlider slider = new JSlider();
  protected JTextField valueField = new JTextField();
  protected JLabel label = new JLabel();
  protected JPanel southPanel = new JPanel();
  GridLayout gridLayout1 = new GridLayout();
  JPanel jPanel2 = new JPanel();
  protected JButton applyButton = new JButton();
  protected JButton undoButton = new JButton();
  FlowLayout flowLayout1 = new FlowLayout();
  protected JCheckBox allGroupsCheck = new JCheckBox();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel jPanel1 = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();

  /**
   * Creates a new GSliderPanel object.
   */
  public GSliderPanel()
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

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  private void jbInit()
      throws Exception
  {
    this.setLayout(gridLayout1);
    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(1);
    slider.setPaintTicks(true);
    slider.setBackground(Color.white);
    slider.setFont(new java.awt.Font("Verdana", 0, 11));
    slider.setDoubleBuffered(true);
    slider.addMouseListener(new MouseAdapter()
    {
      public void mouseReleased(MouseEvent e)
      {
        slider_mouseReleased(e);
      }
    });
    valueField.setFont(new java.awt.Font("Verdana", 0, 11));
    valueField.setMinimumSize(new Dimension(6, 14));
    valueField.setPreferredSize(new Dimension(50, 12));
    valueField.setText("");
    valueField.setHorizontalAlignment(SwingConstants.CENTER);
    valueField.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        valueField_actionPerformed(e);
      }
    });
    label.setFont(new java.awt.Font("Verdana", 0, 11));
    label.setOpaque(false);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setText("set this label text");
    southPanel.setLayout(borderLayout1);
    gridLayout1.setRows(2);
    jPanel2.setLayout(flowLayout1);
    applyButton.setFont(new java.awt.Font("Verdana", 0, 11));
    applyButton.setOpaque(false);
    applyButton.setText("Apply");
    applyButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        applyButton_actionPerformed(e);
      }
    });
    undoButton.setEnabled(false);
    undoButton.setFont(new java.awt.Font("Verdana", 0, 11));
    undoButton.setOpaque(false);
    undoButton.setText("Undo");
    undoButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        undoButton_actionPerformed(e);
      }
    });
    allGroupsCheck.setEnabled(false);
    allGroupsCheck.setFont(new java.awt.Font("Verdana", 0, 11));
    allGroupsCheck.setOpaque(false);
    allGroupsCheck.setText("Apply to all Groups");
    allGroupsCheck.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        allGroupsCheck_actionPerformed(e);
      }
    });
    this.setBackground(Color.white);
    this.setPreferredSize(new Dimension(415, 84));
    jPanel2.setOpaque(false);
    southPanel.setOpaque(false);
    jPanel1.setLayout(borderLayout2);
    jPanel1.setOpaque(false);
    this.add(jPanel2, null);
    jPanel2.add(label, null);
    jPanel2.add(applyButton, null);
    jPanel2.add(undoButton, null);
    this.add(southPanel, null);
    southPanel.add(jPanel1, java.awt.BorderLayout.EAST);
    southPanel.add(slider, java.awt.BorderLayout.CENTER);
    jPanel1.add(valueField, java.awt.BorderLayout.CENTER);
    jPanel1.add(allGroupsCheck, java.awt.BorderLayout.EAST);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void valueField_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void applyButton_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void undoButton_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void allGroupsCheck_actionPerformed(ActionEvent e)
  {
  }

  public void slider_mouseReleased(MouseEvent e)
  {

  }
}
