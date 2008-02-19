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
public class GUserDefinedColours
    extends JPanel
{
  protected JColorChooser colorChooser = new JColorChooser();
  protected JPanel buttonPanel = new JPanel();
  protected GridLayout gridLayout = new GridLayout();
  JPanel lowerPanel = new JPanel();
  protected JButton okButton = new JButton();
  protected JButton applyButton = new JButton();
  protected JButton loadbutton = new JButton();
  protected JButton savebutton = new JButton();
  protected JButton cancelButton = new JButton();
  JPanel namePanel = new JPanel();
  JLabel jLabel1 = new JLabel();
  protected JTextField schemeName = new JTextField();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel panel1 = new JPanel();
  JPanel okCancelPanel = new JPanel();
  JPanel saveLoadPanel = new JPanel();
  BorderLayout borderLayout3 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  FlowLayout flowLayout1 = new FlowLayout();
  BorderLayout borderLayout4 = new BorderLayout();
  JPanel jPanel4 = new JPanel();
  BorderLayout borderLayout5 = new BorderLayout();
  JLabel label = new JLabel();
  protected JPanel casePanel = new JPanel();
  protected JCheckBox caseSensitive = new JCheckBox();
  protected JButton lcaseColour = new JButton();
  /**
   * Creates a new GUserDefinedColours object.
   */
  public GUserDefinedColours()
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
    this.setLayout(borderLayout4);
    buttonPanel.setLayout(gridLayout);
    gridLayout.setColumns(4);
    gridLayout.setRows(5);
    okButton.setFont(new java.awt.Font("Verdana", 0, 11));
    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        okButton_actionPerformed(e);
      }
    });
    applyButton.setFont(new java.awt.Font("Verdana", 0, 11));
    applyButton.setText("Apply");
    applyButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        applyButton_actionPerformed(e);
      }
    });
    loadbutton.setFont(new java.awt.Font("Verdana", 0, 11));
    loadbutton.setText("Load scheme");
    loadbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        loadbutton_actionPerformed(e);
      }
    });
    savebutton.setFont(new java.awt.Font("Verdana", 0, 11));
    savebutton.setText("Save scheme");
    savebutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        savebutton_actionPerformed(e);
      }
    });
    cancelButton.setFont(new java.awt.Font("Verdana", 0, 11));
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancelButton_actionPerformed(e);
      }
    });
    this.setBackground(new Color(212, 208, 223));
    lowerPanel.setOpaque(false);
    lowerPanel.setLayout(borderLayout3);
    colorChooser.setOpaque(false);
    jLabel1.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    jLabel1.setText("Name");
    namePanel.setMinimumSize(new Dimension(300, 31));
    namePanel.setOpaque(false);
    namePanel.setPreferredSize(new Dimension(240, 25));
    namePanel.setLayout(borderLayout1);
    schemeName.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    schemeName.setPreferredSize(new Dimension(105, 21));
    schemeName.setText("");
    schemeName.setHorizontalAlignment(SwingConstants.CENTER);
    panel1.setLayout(flowLayout1);
    panel1.setOpaque(false);
    okCancelPanel.setOpaque(false);
    saveLoadPanel.setOpaque(false);
    jPanel4.setLayout(borderLayout5);
    label.setFont(new java.awt.Font("Verdana", Font.ITALIC, 10));
    label.setOpaque(false);
    label.setPreferredSize(new Dimension(260, 34));
    label.setText(
        "<html>Save your colour scheme with a unique name and it will be added " +
        "to the Colour menu.</html>");
    caseSensitive.setText("Case Sensitive");
    caseSensitive.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        caseSensitive_actionPerformed(e);
      }
    });
    lcaseColour.setText("Lower Case Colour");
    lcaseColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        lcaseColour_actionPerformed(e);
      }
    });

    saveLoadPanel.add(savebutton);
    saveLoadPanel.add(loadbutton);
    okCancelPanel.add(applyButton);
    okCancelPanel.add(okButton);
    okCancelPanel.add(cancelButton);
    lowerPanel.add(saveLoadPanel, java.awt.BorderLayout.NORTH);
    lowerPanel.add(okCancelPanel, java.awt.BorderLayout.SOUTH);

    namePanel.add(schemeName, java.awt.BorderLayout.CENTER);
    namePanel.add(jLabel1, java.awt.BorderLayout.WEST);
    panel1.add(namePanel, null);
    panel1.add(buttonPanel, null);
    panel1.add(casePanel);
    casePanel.add(caseSensitive);
    casePanel.add(lcaseColour);
    panel1.add(lowerPanel, null);
    panel1.add(label);

    jPanel4.add(panel1, java.awt.BorderLayout.CENTER);
    this.add(jPanel4, java.awt.BorderLayout.CENTER);
    this.add(colorChooser, java.awt.BorderLayout.EAST);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void okButton_actionPerformed(ActionEvent e)
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
  protected void loadbutton_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void savebutton_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void cancelButton_actionPerformed(ActionEvent e)
  {
  }

  public void caseSensitive_actionPerformed(ActionEvent e)
  {

  }

  public void lcaseColour_actionPerformed(ActionEvent e)
  {

  }
}
