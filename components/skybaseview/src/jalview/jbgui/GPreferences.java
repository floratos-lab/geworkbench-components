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
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.Rectangle;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class GPreferences
    extends JPanel
{
  JTabbedPane tabbedPane = new JTabbedPane();

  JButton ok = new JButton();
  JButton cancel = new JButton();
  JPanel okCancelPanel = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  protected JCheckBox quality = new JCheckBox();
  JPanel visualTab = new JPanel();
  protected JCheckBox fullScreen = new JCheckBox();
  protected JCheckBox conservation = new JCheckBox();
  protected JCheckBox identity = new JCheckBox();
  protected JCheckBox annotations = new JCheckBox();
  JLabel gapLabel = new JLabel();
  protected JComboBox colour = new JComboBox();
  JLabel colourLabel = new JLabel();
  JLabel fontLabel = new JLabel();
  protected JComboBox fontSizeCB = new JComboBox();
  protected JComboBox fontStyleCB = new JComboBox();
  protected JComboBox fontNameCB = new JComboBox();
  protected JComboBox gapSymbolCB = new JComboBox();
  protected JCheckBox startupCheckbox = new JCheckBox();
  protected JTextField startupFileTextfield = new JTextField();
  JPanel connectTab = new JPanel();
  JLabel serverLabel = new JLabel();
  protected JList linkURLList = new JList();
  protected JTextField proxyServerTB = new JTextField();
  protected JTextField proxyPortTB = new JTextField();
  JLabel portLabel = new JLabel();
  JLabel browserLabel = new JLabel();
  protected JTextField defaultBrowser = new JTextField();
  JButton newLink = new JButton();
  JButton editLink = new JButton();
  JButton deleteLink = new JButton();
  JScrollPane linkScrollPane = new JScrollPane();
  JPanel linkPanel = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  JPanel editLinkButtons = new JPanel();
  GridLayout gridLayout1 = new GridLayout();
  protected JList linkNameList = new JList();
  JPanel linkPanel2 = new JPanel();
  BorderLayout borderLayout3 = new BorderLayout();
  protected JCheckBox useProxy = new JCheckBox();
  JPanel jPanel1 = new JPanel();
  TitledBorder titledBorder1 = new TitledBorder("Proxy Server");
  TitledBorder titledBorder2 = new TitledBorder("File Output");
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout3 = new GridBagLayout();
  protected JComboBox sortby = new JComboBox();
  JLabel sortLabel = new JLabel();
  JPanel jPanel2 = new JPanel();
  GridLayout gridLayout2 = new GridLayout();
  JPanel jPanel3 = new JPanel();
  JPanel exportTab = new JPanel();
  JLabel epsLabel = new JLabel();
  protected JComboBox epsRendering = new JComboBox();
  JLabel jLabel1 = new JLabel();
  protected JCheckBox blcjv = new JCheckBox();
  protected JCheckBox pileupjv = new JCheckBox();
  protected JCheckBox clustaljv = new JCheckBox();
  protected JCheckBox msfjv = new JCheckBox();
  protected JCheckBox fastajv = new JCheckBox();
  protected JCheckBox pfamjv = new JCheckBox();
  FlowLayout flowLayout1 = new FlowLayout();
  protected JCheckBox pirjv = new JCheckBox();
  JPanel jPanel11 = new JPanel();
  Font verdana11 = new java.awt.Font("Verdana", Font.PLAIN, 11);
  protected JCheckBox seqLimit = new JCheckBox();
  GridLayout gridLayout3 = new GridLayout();
  protected JCheckBox smoothFont = new JCheckBox();
  JPanel calcTab = new JPanel();
  protected JCheckBox autoCalculateConsCheck = new JCheckBox();
  protected JCheckBox padGaps = new JCheckBox();
  protected JCheckBox modellerOutput = new JCheckBox();
  protected JPanel dasPanel = new JPanel();
  BorderLayout borderLayout4 = new BorderLayout();
  protected JCheckBox wrap = new JCheckBox();
  protected JCheckBox rightAlign = new JCheckBox();
  protected JCheckBox idItalics = new JCheckBox();
  protected JCheckBox openoverv = new JCheckBox();
  /**
   * Creates a new GPreferences object.
   */
  public GPreferences()
  {
    try
    {
      jbInit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
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
    this.setLayout(borderLayout1);
    ok.setText("OK");
    ok.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed(e);
      }
    });
    cancel.setText("Cancel");
    cancel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });
    quality.setEnabled(false);
    quality.setFont(verdana11);
    quality.setHorizontalAlignment(SwingConstants.RIGHT);
    quality.setHorizontalTextPosition(SwingConstants.LEFT);
    quality.setSelected(true);
    quality.setText("Quality");
    visualTab.setBorder(new TitledBorder("Open new alignment"));
    visualTab.setLayout(null);
    fullScreen.setFont(verdana11);
    fullScreen.setHorizontalAlignment(SwingConstants.RIGHT);
    fullScreen.setHorizontalTextPosition(SwingConstants.LEFT);
    fullScreen.setText("Maximise Window");
    conservation.setEnabled(false);
    conservation.setFont(verdana11);
    conservation.setHorizontalAlignment(SwingConstants.RIGHT);
    conservation.setHorizontalTextPosition(SwingConstants.LEFT);
    conservation.setSelected(true);
    conservation.setText("Conservation");
    identity.setEnabled(false);
    identity.setFont(verdana11);
    identity.setHorizontalAlignment(SwingConstants.RIGHT);
    identity.setHorizontalTextPosition(SwingConstants.LEFT);
    identity.setSelected(true);
    identity.setText("Consensus");
    annotations.setFont(verdana11);
    annotations.setHorizontalAlignment(SwingConstants.RIGHT);
    annotations.setHorizontalTextPosition(SwingConstants.LEFT);
    annotations.setSelected(true);
    annotations.setText("Show Annotations");
    annotations.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        annotations_actionPerformed(e);
      }
    });
    gapLabel.setFont(verdana11);
    gapLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    gapLabel.setText("Gap Symbol ");
    colour.setFont(verdana11);
    colour.setBounds(new Rectangle(172, 225, 155, 21));
    colourLabel.setFont(verdana11);
    colourLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    colourLabel.setText("Colour ");
    fontLabel.setFont(verdana11);
    fontLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    fontLabel.setText("Font ");
    fontSizeCB.setFont(verdana11);
    fontSizeCB.setBounds(new Rectangle(319, 111, 49, 21));
    fontStyleCB.setFont(verdana11);
    fontStyleCB.setBounds(new Rectangle(367, 111, 70, 21));
    fontNameCB.setFont(verdana11);
    fontNameCB.setBounds(new Rectangle(172, 111, 147, 21));
    gapSymbolCB.setFont(verdana11);
    gapSymbolCB.setBounds(new Rectangle(172, 204, 69, 21));
    startupCheckbox.setText("Open file");
    startupCheckbox.setFont(verdana11);
    startupCheckbox.setHorizontalAlignment(SwingConstants.RIGHT);
    startupCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
    startupCheckbox.setSelected(true);
    startupFileTextfield.setFont(verdana11);
    startupFileTextfield.setBounds(new Rectangle(172, 273, 270, 20));
    startupFileTextfield.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() > 1)
        {
          startupFileTextfield_mouseClicked();
        }
      }
    });

    connectTab.setLayout(gridBagLayout3);
    serverLabel.setText("Address");
    serverLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    serverLabel.setFont(verdana11);
    proxyServerTB.setFont(verdana11);
    proxyPortTB.setFont(verdana11);
    portLabel.setFont(verdana11);
    portLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    portLabel.setText("Port");
    browserLabel.setFont(new java.awt.Font("SansSerif", 0, 11));
    browserLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    browserLabel.setText("Default Browser (Unix)");
    defaultBrowser.setFont(verdana11);
    defaultBrowser.setText("");
    newLink.setText("New");
    newLink.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        newLink_actionPerformed(e);
      }
    });
    editLink.setText("Edit");
    editLink.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        editLink_actionPerformed(e);
      }
    });
    deleteLink.setText("Delete");
    deleteLink.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        deleteLink_actionPerformed(e);
      }
    });

    linkURLList.addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        int index = linkURLList.getSelectedIndex();
        linkNameList.setSelectedIndex(index);
      }
    });

    linkNameList.addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        int index = linkNameList.getSelectedIndex();
        linkURLList.setSelectedIndex(index);
      }
    });

    linkScrollPane.setBorder(null);
    linkPanel.setBorder(new TitledBorder("URL link from Sequence ID"));
    linkPanel.setLayout(borderLayout2);
    editLinkButtons.setLayout(gridLayout1);
    gridLayout1.setRows(3);
    linkNameList.setFont(verdana11);
    linkNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    linkPanel2.setLayout(borderLayout3);
    linkURLList.setFont(verdana11);
    linkURLList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    defaultBrowser.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() > 1)
        {
          defaultBrowser_mouseClicked(e);
        }
      }
    });
    useProxy.setFont(verdana11);
    useProxy.setHorizontalAlignment(SwingConstants.RIGHT);
    useProxy.setHorizontalTextPosition(SwingConstants.LEADING);
    useProxy.setText("Use a proxy server");
    useProxy.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        useProxy_actionPerformed();
      }
    });
    jPanel1.setBorder(titledBorder1);
    jPanel1.setLayout(gridBagLayout1);
    sortby.setFont(verdana11);
    sortby.setBounds(new Rectangle(172, 249, 155, 21));
    sortLabel.setFont(verdana11);
    sortLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    sortLabel.setText("Sort by ");
    jPanel2.setBounds(new Rectangle(7, 17, 158, 278));
    jPanel2.setLayout(gridLayout2);
    gridLayout2.setRows(12);
    jPanel3.setBounds(new Rectangle(173, 35, 274, 26));
    exportTab.setLayout(null);
    epsLabel.setFont(verdana11);
    epsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    epsLabel.setText("EPS Rendering Style");
    epsLabel.setBounds(new Rectangle(9, 31, 140, 24));
    epsRendering.setFont(verdana11);
    epsRendering.setBounds(new Rectangle(154, 34, 187, 21));
    jLabel1.setFont(verdana11);
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setText("Append /start-end (/15-380)");
    jLabel1.setFont(verdana11);
    fastajv.setFont(verdana11);
    fastajv.setHorizontalAlignment(SwingConstants.LEFT);
    clustaljv.setText("Clustal     ");
    blcjv.setText("BLC     ");
    fastajv.setText("Fasta     ");
    msfjv.setText("MSF     ");
    pfamjv.setText("PFAM     ");
    pileupjv.setText("Pileup     ");
    msfjv.setFont(verdana11);
    msfjv.setHorizontalAlignment(SwingConstants.LEFT);
    pirjv.setText("PIR     ");
    jPanel11.setFont(verdana11);
    jPanel11.setBorder(titledBorder2);
    jPanel11.setBounds(new Rectangle(30, 72, 196, 182));
    jPanel11.setLayout(gridLayout3);
    blcjv.setFont(verdana11);
    blcjv.setHorizontalAlignment(SwingConstants.LEFT);
    clustaljv.setFont(verdana11);
    clustaljv.setHorizontalAlignment(SwingConstants.LEFT);
    pfamjv.setFont(verdana11);
    pfamjv.setHorizontalAlignment(SwingConstants.LEFT);
    pileupjv.setFont(verdana11);
    pileupjv.setHorizontalAlignment(SwingConstants.LEFT);
    pirjv.setFont(verdana11);
    pirjv.setHorizontalAlignment(SwingConstants.LEFT);
    seqLimit.setFont(verdana11);
    seqLimit.setHorizontalAlignment(SwingConstants.RIGHT);
    seqLimit.setHorizontalTextPosition(SwingConstants.LEFT);
    seqLimit.setText("Full Sequence Id");
    gridLayout3.setRows(8);
    smoothFont.setFont(verdana11);
    smoothFont.setHorizontalAlignment(SwingConstants.RIGHT);
    smoothFont.setHorizontalTextPosition(SwingConstants.LEADING);
    smoothFont.setText("Smooth Font");
    calcTab.setLayout(null);
    autoCalculateConsCheck.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    autoCalculateConsCheck.setText("AutoCalculate Consensus");
    autoCalculateConsCheck.setBounds(new Rectangle(21, 52, 209, 23));
    padGaps.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    padGaps.setText("Pad gaps when editing");
    padGaps.setBounds(new Rectangle(22, 94, 168, 23));
    modellerOutput.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    modellerOutput.setText("Use Modeller Output");
    modellerOutput.setBounds(new Rectangle(228, 226, 168, 23));
    dasPanel.setLayout(borderLayout4);
    wrap.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    wrap.setHorizontalAlignment(SwingConstants.TRAILING);
    wrap.setHorizontalTextPosition(SwingConstants.LEADING);
    wrap.setText("Wrap Alignment");
    rightAlign.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    rightAlign.setForeground(Color.black);
    rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
    rightAlign.setHorizontalTextPosition(SwingConstants.LEFT);
    rightAlign.setText("Right Align Ids");
    idItalics.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    idItalics.setHorizontalAlignment(SwingConstants.RIGHT);
    idItalics.setHorizontalTextPosition(SwingConstants.LEADING);
    idItalics.setText("Sequence Name Italics");
    openoverv.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    openoverv.setActionCommand("Open Overview");
    openoverv.setHorizontalAlignment(SwingConstants.RIGHT);
    openoverv.setHorizontalTextPosition(SwingConstants.LEADING);
    openoverv.setText("Open Overview Window");
    openoverv.setBounds(new Rectangle(169, 17, 200, 23));
    jPanel2.add(fullScreen);
    jPanel2.add(annotations);
    jPanel2.add(seqLimit);
    jPanel2.add(rightAlign);
    jPanel2.add(fontLabel);
    jPanel2.add(idItalics);
    jPanel2.add(smoothFont);
    jPanel2.add(wrap);
    jPanel2.add(gapLabel);
    jPanel2.add(colourLabel);
    jPanel2.add(sortLabel);
    jPanel2.add(startupCheckbox);
    visualTab.add(openoverv);
    visualTab.add(startupFileTextfield);
    visualTab.add(sortby);
    visualTab.add(colour);
    visualTab.add(gapSymbolCB);
    visualTab.add(jPanel3);
    visualTab.add(fontNameCB);
    visualTab.add(fontSizeCB);
    visualTab.add(fontStyleCB);
    jPanel3.add(conservation);
    jPanel3.add(identity);
    jPanel3.add(quality);
    visualTab.add(jPanel2);
    linkPanel.add(editLinkButtons, BorderLayout.EAST);
    editLinkButtons.add(newLink, null);
    editLinkButtons.add(editLink, null);
    editLinkButtons.add(deleteLink, null);
    linkPanel.add(linkScrollPane, BorderLayout.CENTER);
    linkScrollPane.getViewport().add(linkPanel2, null);
    linkPanel2.add(linkURLList, BorderLayout.CENTER);
    linkPanel2.add(linkNameList, BorderLayout.WEST);
    okCancelPanel.add(ok);
    okCancelPanel.add(cancel);
    this.add(tabbedPane, java.awt.BorderLayout.CENTER);

    this.add(okCancelPanel, java.awt.BorderLayout.SOUTH);
    jPanel1.add(serverLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(0, 2, 4, 0), 5, 0));
    jPanel1.add(portLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE,
                                                  new Insets(0, 0, 4, 0), 11, 6));
    connectTab.add(linkPanel, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(16, 0, 0, 12), 359, -17));
    connectTab.add(jPanel1, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(21, 0, 35, 12), 4, 6));
    connectTab.add(browserLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(16, 0, 0, 0), 5, 1));
    jPanel1.add(proxyPortTB, new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(0, 2, 4, 2), 54, 1));
    jPanel1.add(proxyServerTB, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(0, 2, 4, 0), 263, 1));
    connectTab.add(defaultBrowser, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(15, 0, 0, 15), 307, 1));

    jPanel1.add(useProxy, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(0, 2, 5, 185), 2,
                                                 -4));
    DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
    dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
    gapSymbolCB.setRenderer(dlcr);

    tabbedPane.add(visualTab, "Visual");
    tabbedPane.add(connectTab, "Connections");
    tabbedPane.add(exportTab, "Output");
    jPanel11.add(jLabel1);
    jPanel11.add(blcjv);
    jPanel11.add(clustaljv);
    jPanel11.add(fastajv);
    jPanel11.add(msfjv);
    jPanel11.add(pfamjv);
    jPanel11.add(pileupjv);
    jPanel11.add(pirjv);
    exportTab.add(modellerOutput);
    tabbedPane.add(calcTab, "Editing");
    calcTab.add(autoCalculateConsCheck);
    calcTab.add(padGaps);
    tabbedPane.add(dasPanel, "DAS Settings");

    exportTab.add(epsLabel);
    exportTab.add(epsRendering);
    exportTab.add(jPanel11);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void ok_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void cancel_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void annotations_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   */
  public void startupFileTextfield_mouseClicked()
  {
  }

  public void newLink_actionPerformed(ActionEvent e)
  {

  }

  public void editLink_actionPerformed(ActionEvent e)
  {

  }

  public void deleteLink_actionPerformed(ActionEvent e)
  {

  }

  public void defaultBrowser_mouseClicked(MouseEvent e)
  {

  }

  public void linkURLList_keyTyped(KeyEvent e)
  {

  }

  public void useProxy_actionPerformed()
  {
    proxyServerTB.setEnabled(useProxy.isSelected());
    proxyPortTB.setEnabled(useProxy.isSelected());
  }

}
