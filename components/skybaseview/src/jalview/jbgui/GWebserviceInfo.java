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
public class GWebserviceInfo
    extends JPanel
{
  protected JTextArea infoText = new JTextArea();
  JScrollPane jScrollPane1 = new JScrollPane();
  JPanel jPanel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  protected JPanel titlePanel = new JPanel();
  BorderLayout borderLayout3 = new BorderLayout();
  protected JPanel buttonPanel = new JPanel();
  public JButton cancel = new JButton();
  public JButton showResultsNewFrame = new JButton();
  public JButton mergeResults = new JButton();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  /**
   * Creates a new GWebserviceInfo object.
   */
  public GWebserviceInfo()
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
    infoText.setFont(new java.awt.Font("Verdana", 0, 10));
    infoText.setBorder(null);
    infoText.setEditable(false);
    infoText.setText("");
    infoText.setLineWrap(true);
    infoText.setWrapStyleWord(true);
    this.setLayout(borderLayout1);
    jPanel1.setLayout(borderLayout2);
    titlePanel.setBackground(Color.white);
    titlePanel.setPreferredSize(new Dimension(0, 60));
    titlePanel.setLayout(borderLayout3);
    jScrollPane1.setBorder(null);
    jScrollPane1.setPreferredSize(new Dimension(400, 70));
    cancel.setFont(new java.awt.Font("Verdana", 0, 11));
    cancel.setText("Cancel");
    cancel.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });
    buttonPanel.setLayout(gridBagLayout1);
    buttonPanel.setOpaque(false);
    showResultsNewFrame.setText("New Window");
    mergeResults.setText("Merge Results");
    this.setBackground(Color.white);
    this.add(jPanel1, BorderLayout.NORTH);
    jPanel1.add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(infoText, null);
    jPanel1.add(titlePanel, BorderLayout.NORTH);
    titlePanel.add(buttonPanel, BorderLayout.EAST);
    buttonPanel.add(cancel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER,
        GridBagConstraints.NONE,
        new Insets(19, 6, 16, 4), 0, 0));
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void cancel_actionPerformed(ActionEvent e)
  {
  }
}
