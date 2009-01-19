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

import jalview.bin.*;
import jalview.io.*;
import jalview.jbgui.*;
import jalview.schemes.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class Preferences
    extends GPreferences
{
  /** Holds name and link separated with | character. Sequence ID must be $SEQUENCE_ID$ */
  public static Vector sequenceURLLinks;
  static
  {
    String string = Cache.getDefault("SEQUENCE_LINKS",
                                     "SRS|http://srs.ebi.ac.uk/srsbin/cgi-bin/wgetz?-newId+(([uniprot-all:$SEQUENCE_ID$]))+-view+SwissEntry");
    sequenceURLLinks = new Vector();

    try
    {
      StringTokenizer st = new StringTokenizer(string, "|");
      while (st.hasMoreElements())
      {
        sequenceURLLinks.addElement(st.nextToken() + "|" + st.nextToken());
      }
    }
    catch (Exception ex)
    {
      System.out.println(ex + "\nError parsing sequence links");
    }
  }

  Vector nameLinks, urlLinks;

  JInternalFrame frame;

  DasSourceBrowser dasSource;

  /**
   * Creates a new Preferences object.
   */
  public Preferences()
  {

    frame = new JInternalFrame();
    frame.setContentPane(this);
    dasSource = new DasSourceBrowser();
    dasPanel.add(dasSource, BorderLayout.CENTER);

    int width = 500, height = 420;
    if (System.getProperty("os.name").startsWith("Mac"))
    {
      width = 570;
      height = 460;
    }

    Desktop.addInternalFrame(frame, "Preferences", width, height);
    frame.setMinimumSize(new Dimension(width, height));

    seqLimit.setSelected(Cache.getDefault("SHOW_JVSUFFIX", true));
    rightAlign.setSelected(Cache.getDefault("RIGHT_ALIGN_IDS", false));
    fullScreen.setSelected(Cache.getDefault("SHOW_FULLSCREEN", false));
    annotations.setSelected(Cache.getDefault("SHOW_ANNOTATIONS", true));

    conservation.setEnabled(Cache.getDefault("SHOW_ANNOTATIONS", true));
    quality.setEnabled(Cache.getDefault("SHOW_ANNOTATIONS", true));
    identity.setEnabled(Cache.getDefault("SHOW_ANNOTATIONS", true));

    conservation.setSelected(Cache.getDefault("SHOW_CONSERVATION", true));
    quality.setSelected(Cache.getDefault("SHOW_QUALITY", true));
    identity.setSelected(Cache.getDefault("SHOW_IDENTITY", true));
    openoverv.setSelected(Cache.getDefault("SHOW_OVERVIEW", false));

    for (int i = 0; i < 13; i++)
    {
      colour.addItem(ColourSchemeProperty.getColourName(i));
    }

    String string = Cache.getDefault("DEFAULT_COLOUR", "None");

    colour.setSelectedItem(string);

    String[] fonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getAvailableFontFamilyNames();

    for (int i = 0; i < fonts.length; i++)
    {
      fontNameCB.addItem(fonts[i]);
    }

    for (int i = 1; i < 31; i++)
    {
      fontSizeCB.addItem(i + "");
    }

    fontStyleCB.addItem("plain");
    fontStyleCB.addItem("bold");
    fontStyleCB.addItem("italic");

    fontNameCB.setSelectedItem(Cache.getDefault("FONT_NAME", "SansSerif"));
    fontSizeCB.setSelectedItem(Cache.getDefault("FONT_SIZE", "10"));
    fontStyleCB.setSelectedItem(Cache.getDefault("FONT_STYLE", Font.PLAIN + ""));

    smoothFont.setSelected(Cache.getDefault("ANTI_ALIAS", false));

    idItalics.setSelected(Cache.getDefault("ID_ITALICS", true));

    wrap.setSelected(Cache.getDefault("WRAP_ALIGNMENT", false));

    gapSymbolCB.addItem("-");
    gapSymbolCB.addItem(".");

    gapSymbolCB.setSelectedItem(Cache.getDefault("GAP_SYMBOL", "-"));

    startupCheckbox.setSelected(Cache.getDefault("SHOW_STARTUP_FILE", true));
    startupFileTextfield.setText(Cache.getDefault("STARTUP_FILE",
                                                  "http://www.jalview.org/examples/exampleFile2_3.jar"));

    sortby.addItem("No sort");
    sortby.addItem("Id");
    sortby.addItem("Pairwise Identity");
    sortby.setSelectedItem(Cache.getDefault("SORT_ALIGNMENT", "No sort"));

    epsRendering.addItem("Prompt each time");
    epsRendering.addItem("Lineart");
    epsRendering.addItem("Text");
    epsRendering.setSelectedItem(Cache.getDefault("EPS_RENDERING",
                                                  "Prompt each time"));

    blcjv.setSelected(Cache.getDefault("BLC_JVSUFFIX", true));
    clustaljv.setSelected(Cache.getDefault("CLUSTAL_JVSUFFIX", true));
    fastajv.setSelected(Cache.getDefault("FASTA_JVSUFFIX", true));
    msfjv.setSelected(Cache.getDefault("MSF_JVSUFFIX", true));
    pfamjv.setSelected(Cache.getDefault("PFAM_JVSUFFIX", true));
    pileupjv.setSelected(Cache.getDefault("PILEUP_JVSUFFIX", true));
    pirjv.setSelected(Cache.getDefault("PIR_JVSUFFIX", true));

    modellerOutput.setSelected(Cache.getDefault("PIR_MODELLER", false));

    autoCalculateConsCheck.setSelected(Cache.getDefault("AUTO_CALC_CONSENSUS", true));
    padGaps.setSelected(Cache.getDefault("PAD_GAPS", false));

    /****************************************************
     * Set up Connections
     */
    nameLinks = new Vector();
    urlLinks = new Vector();
    for (int i = 0; i < sequenceURLLinks.size(); i++)
    {
      String link = sequenceURLLinks.elementAt(i).toString();
      nameLinks.addElement(link.substring(0, link.indexOf("|")));
      urlLinks.addElement(link.substring(link.indexOf("|") + 1));
    }

    updateLinkData();

    useProxy.setSelected(Cache.getDefault("USE_PROXY", false));
    proxyServerTB.setEnabled(useProxy.isSelected());
    proxyPortTB.setEnabled(useProxy.isSelected());
    proxyServerTB.setText(Cache.getDefault("PROXY_SERVER", ""));
    proxyPortTB.setText(Cache.getDefault("PROXY_PORT", ""));

    defaultBrowser.setText(Cache.getDefault("DEFAULT_BROWSER", ""));
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
   * @param e DOCUMENT ME!
   */
  public void ok_actionPerformed(ActionEvent e)
  {

    Cache.applicationProperties.setProperty("SHOW_JVSUFFIX",
                                            Boolean.toString(seqLimit.
        isSelected()));
    Cache.applicationProperties.setProperty("RIGHT_ALIGN_IDS",
                                            Boolean.toString(rightAlign.
        isSelected()));
    Cache.applicationProperties.setProperty("SHOW_FULLSCREEN",
                                            Boolean.toString(fullScreen.
        isSelected()));
    Cache.applicationProperties.setProperty("SHOW_OVERVIEW",
                                            Boolean.toString(openoverv.
        isSelected()));
    Cache.applicationProperties.setProperty("SHOW_ANNOTATIONS",
                                            Boolean.
                                            toString(annotations.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_CONSERVATION",
                                            Boolean.
                                            toString(conservation.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_QUALITY",
                                            Boolean.toString(quality.isSelected()));
    Cache.applicationProperties.setProperty("SHOW_IDENTITY",
                                            Boolean.toString(identity.
        isSelected()));

    Cache.applicationProperties.setProperty("DEFAULT_COLOUR",
                                            colour.getSelectedItem().toString());
    Cache.applicationProperties.setProperty("GAP_SYMBOL",
                                            gapSymbolCB.getSelectedItem().
                                            toString());

    Cache.applicationProperties.setProperty("FONT_NAME",
                                            fontNameCB.getSelectedItem().
                                            toString());
    Cache.applicationProperties.setProperty("FONT_STYLE",
                                            fontStyleCB.getSelectedItem().
                                            toString());
    Cache.applicationProperties.setProperty("FONT_SIZE",
                                            fontSizeCB.getSelectedItem().
                                            toString());

    Cache.applicationProperties.setProperty("ID_ITALICS",
                                            Boolean.toString(idItalics.
        isSelected()));

    Cache.applicationProperties.setProperty("ANTI_ALIAS",
                                            Boolean.toString(smoothFont.
        isSelected()));

    Cache.applicationProperties.setProperty("WRAP_ALIGNMENT",
                                            Boolean.toString(wrap.isSelected()));

    Cache.applicationProperties.setProperty("STARTUP_FILE",
                                            startupFileTextfield.getText());
    Cache.applicationProperties.setProperty("SHOW_STARTUP_FILE",
                                            Boolean.
                                            toString(startupCheckbox.isSelected()));

    Cache.applicationProperties.setProperty("SORT_ALIGNMENT",
                                            sortby.getSelectedItem().toString());

    if (epsRendering.getSelectedItem().equals("Prompt each time"))
    {
      Cache.applicationProperties.remove("EPS_RENDERING");
    }
    else
    {
      Cache.applicationProperties.setProperty("EPS_RENDERING",
                                              epsRendering.getSelectedItem().
                                              toString());
    }

    if (defaultBrowser.getText().trim().length() < 1)
    {
      Cache.applicationProperties.remove("DEFAULT_BROWSER");
    }
    else
    {
      Cache.applicationProperties.setProperty("DEFAULT_BROWSER",
                                              defaultBrowser.getText());
    }

    jalview.util.BrowserLauncher.resetBrowser();

    if (nameLinks.size() > 0)
    {
      StringBuffer links = new StringBuffer();
      sequenceURLLinks = new Vector();
      for (int i = 0; i < nameLinks.size(); i++)
      {
        sequenceURLLinks.addElement(nameLinks.elementAt(i) + "|" +
                                    urlLinks.elementAt(i));
        links.append(sequenceURLLinks.elementAt(i).toString());
        links.append("|");
      }
      // remove last "|"
      links.setLength(links.length() - 1);
      Cache.applicationProperties.setProperty("SEQUENCE_LINKS", links.toString());
    }
    else
    {
      Cache.applicationProperties.remove("SEQUENCE_LINKS");
    }

    Cache.applicationProperties.setProperty("USE_PROXY",
                                            Boolean.toString(useProxy.
        isSelected()));

    if (proxyServerTB.getText().trim().length() < 1)
    {
      Cache.applicationProperties.remove("PROXY_SERVER");
    }
    else
    {
      Cache.applicationProperties.setProperty("PROXY_SERVER",
                                              proxyServerTB.getText());
    }

    if (proxyPortTB.getText().trim().length() < 1)
    {
      Cache.applicationProperties.remove("PROXY_PORT");
    }
    else
    {
      Cache.applicationProperties.setProperty("PROXY_PORT", proxyPortTB.getText());
    }

    if (useProxy.isSelected())
    {
      System.setProperty("http.proxyHost", proxyServerTB.getText());
      System.setProperty("http.proxyPort", proxyPortTB.getText());
    }
    else
    {
      System.setProperty("http.proxyHost", "");
      System.setProperty("http.proxyPort", "");
    }

    Cache.applicationProperties.setProperty("BLC_JVSUFFIX",
                                            Boolean.toString(blcjv.isSelected()));
    Cache.applicationProperties.setProperty("CLUSTAL_JVSUFFIX",
                                            Boolean.
                                            toString(clustaljv.isSelected()));
    Cache.applicationProperties.setProperty("FASTA_JVSUFFIX",
                                            Boolean.toString(fastajv.isSelected()));
    Cache.applicationProperties.setProperty("MSF_JVSUFFIX",
                                            Boolean.toString(msfjv.isSelected()));
    Cache.applicationProperties.setProperty("PFAM_JVSUFFIX",
                                            Boolean.toString(pfamjv.isSelected()));
    Cache.applicationProperties.setProperty("PILEUP_JVSUFFIX",
                                            Boolean.toString(pileupjv.
        isSelected()));
    Cache.applicationProperties.setProperty("PIR_JVSUFFIX",
                                            Boolean.toString(pirjv.isSelected()));
    Cache.applicationProperties.setProperty("PIR_MODELLER",
                                            Boolean.toString(modellerOutput.
        isSelected()));
    jalview.io.PIRFile.useModellerOutput = modellerOutput.isSelected();

    Cache.applicationProperties.setProperty("AUTO_CALC_CONSENSUS",
                                            Boolean.toString(
        autoCalculateConsCheck.isSelected()));
    Cache.applicationProperties.setProperty("PAD_GAPS",
                                            Boolean.toString(padGaps.isSelected()));

    dasSource.saveProperties(Cache.applicationProperties);

    Cache.saveProperties();
    try
    {
      frame.setClosed(true);
    }
    catch (Exception ex)
    {
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void startupFileTextfield_mouseClicked()
  {
    JalviewFileChooser chooser = new JalviewFileChooser(jalview.bin.Cache.
        getProperty(
            "LAST_DIRECTORY"),
        new String[]
        {
        "fa, fasta, fastq", "aln", "pfam", "msf", "pir", "blc",
        "jar"
    },
        new String[]
        {
        "Fasta", "Clustal", "PFAM", "MSF", "PIR", "BLC", "Jalview"
    }, jalview.bin.Cache.getProperty("DEFAULT_FILE_FORMAT"));
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle("Select startup file");

    int value = chooser.showOpenDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      jalview.bin.Cache.applicationProperties.setProperty("DEFAULT_FILE_FORMAT",
          chooser.getSelectedFormat());
      startupFileTextfield.setText(chooser.getSelectedFile()
                                   .getAbsolutePath());
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void cancel_actionPerformed(ActionEvent e)
  {
    try
    {
      frame.setClosed(true);
    }
    catch (Exception ex)
    {
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void annotations_actionPerformed(ActionEvent e)
  {
    conservation.setEnabled(annotations.isSelected());
    quality.setEnabled(annotations.isSelected());
    identity.setEnabled(annotations.isSelected());
  }

  public void newLink_actionPerformed(ActionEvent e)
  {

    GSequenceLink link = new GSequenceLink();
    boolean valid = false;
    while (!valid)
    {
      if (JOptionPane.showInternalConfirmDialog(Desktop.desktop, link,
                                                "New sequence URL link",
                                                JOptionPane.OK_CANCEL_OPTION
                                                , -1, null)
          == JOptionPane.OK_OPTION)
      {
        if (link.checkValid())
        {
          nameLinks.addElement(link.getName());
          urlLinks.addElement(link.getURL());
          updateLinkData();
          valid = true;
        }
      }
      else
      {
        break;
      }
    }
  }

  public void editLink_actionPerformed(ActionEvent e)
  {
    GSequenceLink link = new GSequenceLink();

    int index = linkNameList.getSelectedIndex();
    if (index == -1)
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                            "No link selected!"
                                            , "No link selected",
                                            JOptionPane.WARNING_MESSAGE);
      return;
    }

    link.setName(nameLinks.elementAt(index).toString());
    link.setURL(urlLinks.elementAt(index).toString());

    boolean valid = false;
    while (!valid)
    {

      if (JOptionPane.showInternalConfirmDialog(Desktop.desktop, link,
                                                "New sequence URL link",
                                                JOptionPane.OK_CANCEL_OPTION
                                                , -1, null)
          == JOptionPane.OK_OPTION)
      {
        if (link.checkValid())
        {
          nameLinks.setElementAt(link.getName(), index);
          urlLinks.setElementAt(link.getURL(), index);
          updateLinkData();
          valid = true;
        }
      }

      else
      {
        break;
      }
    }
  }

  public void deleteLink_actionPerformed(ActionEvent e)
  {
    int index = linkNameList.getSelectedIndex();
    if (index == -1)
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                            "No link selected!"
                                            , "No link selected",
                                            JOptionPane.WARNING_MESSAGE);
      return;
    }
    nameLinks.removeElementAt(index);
    urlLinks.removeElementAt(index);
    updateLinkData();
  }

  void updateLinkData()
  {
    linkNameList.setListData(nameLinks);
    linkURLList.setListData(urlLinks);
  }

  public void defaultBrowser_mouseClicked(MouseEvent e)
  {
    JFileChooser chooser = new JFileChooser(".");
    chooser.setDialogTitle("Select default web browser");

    int value = chooser.showOpenDialog(this);

    if (value == JFileChooser.APPROVE_OPTION)
    {
      defaultBrowser.setText(chooser.getSelectedFile().getAbsolutePath());
    }

  }

  private void jbInit()
      throws Exception
  {
  }
}
