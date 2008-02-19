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

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import jalview.datamodel.*;
import jalview.io.*;
import jalview.jbgui.*;
import jalview.schemes.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class UserDefinedColours
    extends GUserDefinedColours implements ChangeListener
{
  AlignmentPanel ap;
  SequenceGroup seqGroup;
  Vector selectedButtons;
  ColourSchemeI oldColourScheme;
  JInternalFrame frame;
  AppJmol jmol;
  Vector upperCaseButtons;
  Vector lowerCaseButtons;

  /**
   * Creates a new UserDefinedColours object.
   *
   * @param ap DOCUMENT ME!
   * @param sg DOCUMENT ME!
   */
  public UserDefinedColours(AlignmentPanel ap, SequenceGroup sg)
  {
    super();

    lcaseColour.setEnabled(false);

    this.ap = ap;
    seqGroup = sg;

    if (seqGroup != null)
    {
      oldColourScheme = seqGroup.cs;
    }
    else
    {
      oldColourScheme = ap.av.getGlobalColourScheme();
    }

    if (oldColourScheme instanceof UserColourScheme)
    {
      schemeName.setText( ( (UserColourScheme) oldColourScheme).getName());
      if ( ( (UserColourScheme) oldColourScheme).getLowerCaseColours() != null)
      {
        caseSensitive.setSelected(true);
        lcaseColour.setEnabled(true);
        resetButtonPanel(true);
      }
      else
      {
        resetButtonPanel(false);
      }
    }
    else
    {
      resetButtonPanel(false);
    }

    showFrame();
  }

  public UserDefinedColours(AppJmol jmol, ColourSchemeI oldcs)
  {
    super();
    this.jmol = jmol;

    colorChooser.getSelectionModel().addChangeListener(this);

    oldColourScheme = oldcs;

    if (oldColourScheme instanceof UserColourScheme)
    {
      schemeName.setText( ( (UserColourScheme) oldColourScheme).getName());
    }

    resetButtonPanel(false);

    showFrame();

  }

  void showFrame()
  {
    colorChooser.getSelectionModel().addChangeListener(this);
    frame = new JInternalFrame();
    frame.setContentPane(this);
    Desktop.addInternalFrame(frame, "User Defined Colours", 720, 370, true);

    if (seqGroup != null)
    {
      frame.setTitle(frame.getTitle() + " (" + seqGroup.getName() + ")");
    }

    if (System.getProperty("os.name").startsWith("Mac"))
    {
      frame.setSize(760, 370);
    }
  }

  void resetButtonPanel(boolean caseSensitive)
  {
    buttonPanel.removeAll();

    if (upperCaseButtons == null)
    {
      upperCaseButtons = new Vector();
    }

    JButton button;
    String label;
    for (int i = 0; i < 20; i++)
    {
      if (caseSensitive)
      {
        label = ResidueProperties.aa[i];
      }
      else
      {
        label = ResidueProperties.aa2Triplet.get
            (ResidueProperties.aa[i]).toString();
      }

      button = makeButton(label,
                          ResidueProperties.aa[i],
                          upperCaseButtons, i);

      buttonPanel.add(button);
    }

    buttonPanel.add(makeButton("B", "B", upperCaseButtons, 20));
    buttonPanel.add(makeButton("Z", "Z", upperCaseButtons, 21));
    buttonPanel.add(makeButton("X", "X", upperCaseButtons, 22));
    buttonPanel.add(makeButton("Gap", "-", upperCaseButtons, 23));

    if (!caseSensitive)
    {
      gridLayout.setRows(6);
      gridLayout.setColumns(4);
    }
    else
    {
      gridLayout.setRows(7);
      int cols = 7;
      gridLayout.setColumns(cols + 1);

      if (lowerCaseButtons == null)
      {
        lowerCaseButtons = new Vector();
      }

      for (int i = 0; i < 20; i++)
      {
        int row = i / cols + 1;
        int index = (row * cols) + i;
        button = makeButton(
            ResidueProperties.aa[i].toLowerCase(),
            ResidueProperties.aa[i].toLowerCase(),
            lowerCaseButtons,
            i);

        buttonPanel.add(button, index);
      }
    }

    if (caseSensitive)
    {
      buttonPanel.add(makeButton("b", "b", lowerCaseButtons, 20));
      buttonPanel.add(makeButton("z", "z", lowerCaseButtons, 21));
      buttonPanel.add(makeButton("x", "x", lowerCaseButtons, 22));
    }

    buttonPanel.validate();
    validate();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void stateChanged(ChangeEvent evt)
  {
    if (selectedButtons != null)
    {
      JButton button = null;
      for (int i = 0; i < selectedButtons.size(); i++)
      {
        button = (JButton) selectedButtons.elementAt(i);
        button.setBackground(colorChooser.getColor());
        button.setForeground(button.getBackground().brighter().brighter().
                             brighter());
      }
      if (button == lcaseColour)
      {
        for (int i = 0; i < lowerCaseButtons.size(); i++)
        {
          button = (JButton) lowerCaseButtons.elementAt(i);
          button.setBackground(colorChooser.getColor());
          button.setForeground(button.getBackground().brighter().brighter().
                               brighter());
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void colourButtonPressed(MouseEvent e)
  {
    if (selectedButtons == null)
    {
      selectedButtons = new Vector();
    }

    JButton pressed = (JButton) e.getSource();

    if (e.isShiftDown())
    {
      JButton start, end = (JButton) e.getSource();
      if (selectedButtons.size() > 0)
      {
        start = (JButton) selectedButtons.elementAt(selectedButtons.size() - 1);
      }
      else
      {
        start = (JButton) e.getSource();
      }

      int startIndex = 0, endIndex = 0;
      for (int b = 0; b < buttonPanel.getComponentCount(); b++)
      {
        if (buttonPanel.getComponent(b) == start)
        {
          startIndex = b;
        }
        if (buttonPanel.getComponent(b) == end)
        {
          endIndex = b;
        }
      }

      if (startIndex > endIndex)
      {
        int temp = startIndex;
        startIndex = endIndex;
        endIndex = temp;
      }

      for (int b = startIndex; b <= endIndex; b++)
      {
        JButton button = (JButton) buttonPanel.getComponent(b);
        if (!selectedButtons.contains(button))
        {
          button.setForeground(button.getBackground().brighter().brighter());
          selectedButtons.add(button);
        }
      }
    }
    else if (!e.isControlDown())
    {
      for (int b = 0; b < selectedButtons.size(); b++)
      {
        JButton button = (JButton) selectedButtons.elementAt(b);
        button.setForeground(button.getBackground().darker().darker());
      }
      selectedButtons.clear();
      pressed.setForeground(pressed.getBackground().brighter().brighter());
      selectedButtons.addElement(pressed);

    }
    else if (e.isControlDown())
    {
      if (selectedButtons.contains(pressed))
      {
        pressed.setForeground(pressed.getBackground().darker().darker());
        selectedButtons.remove(pressed);
      }
      else
      {
        pressed.setForeground(pressed.getBackground().brighter().brighter());
        selectedButtons.addElement(pressed);
      }
    }

    if (selectedButtons.size() > 0)
    {
      colorChooser.setColor( ( (JButton) selectedButtons.elementAt(0)).
                            getBackground());
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param label DOCUMENT ME!
   * @param aa DOCUMENT ME!
   */
  JButton makeButton(String label,
                     String aa,
                     Vector caseSensitiveButtons,
                     int buttonIndex)
  {
    final JButton button;
    Color col;

    if (buttonIndex < caseSensitiveButtons.size())
    {
      button = (JButton) caseSensitiveButtons.elementAt(buttonIndex);
      col = button.getBackground();
    }
    else
    {
      button = new JButton();
      button.addMouseListener(new java.awt.event.MouseAdapter()
      {
        public void mouseClicked(MouseEvent e)
        {
          colourButtonPressed(e);
        }
      });

      caseSensitiveButtons.addElement(button);

      col = Color.white;

      try
      {
        col = oldColourScheme.findColour(aa.charAt(0), -1);
      }
      catch (Exception ex)
      {}
    }

    if (caseSensitive.isSelected())
    {
      button.setMargin(new java.awt.Insets(2, 2, 2, 2));
    }
    else
    {
      button.setMargin(new java.awt.Insets(2, 14, 2, 14));
    }

    button.setBackground(col);
    button.setText(label);
    button.setForeground(col.darker().darker().darker());
    button.setFont(new java.awt.Font("Verdana", Font.BOLD, 10));

    return button;
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void okButton_actionPerformed(ActionEvent e)
  {
    applyButton_actionPerformed(null);

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
  protected void applyButton_actionPerformed(ActionEvent e)
  {
    UserColourScheme ucs = getSchemeFromButtons();
    ucs.setName(schemeName.getText());

    if (seqGroup != null)
    {
      seqGroup.cs = ucs;
      ap.paintAlignment(true);
    }
    else if (ap != null)
    {
      ap.alignFrame.changeColour(ucs);
    }
    else if (jmol != null)
    {
      jmol.setJalviewColourScheme(ucs);
    }
  }

  UserColourScheme getSchemeFromButtons()
  {

    Color[] newColours = new Color[24];

    for (int i = 0; i < 24; i++)
    {
      JButton button = (JButton) upperCaseButtons.elementAt(i);
      newColours[i] = button.getBackground();
    }

    UserColourScheme ucs = new UserColourScheme(newColours);

    if (caseSensitive.isSelected())
    {
      newColours = new Color[23];
      for (int i = 0; i < 23; i++)
      {
        JButton button = (JButton) lowerCaseButtons.elementAt(i);
        newColours[i] = button.getBackground();
      }
      ucs.setLowerCaseColours(newColours);
    }

    if (ap != null)
    {
      ucs.setThreshold(0, ap.av.getIgnoreGapsConsensus());
    }

    return ucs;
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void loadbutton_actionPerformed(ActionEvent e)
  {
    upperCaseButtons = new Vector();
    lowerCaseButtons = new Vector();

    JalviewFileChooser chooser = new JalviewFileChooser(jalview.bin.Cache.
        getProperty(
            "LAST_DIRECTORY"), new String[]
        {"jc"},
        new String[]
        {"Jalview User Colours"}, "Jalview User Colours");
    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle("Load colour scheme");
    chooser.setToolTipText("Load");

    int value = chooser.showOpenDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      File choice = chooser.getSelectedFile();
      jalview.bin.Cache.setProperty("LAST_DIRECTORY", choice.getParent());
      String defaultColours = jalview.bin.Cache.getDefault(
          "USER_DEFINED_COLOURS",
          choice.getPath());
      if (defaultColours.indexOf(choice.getPath()) == -1)
      {
        defaultColours = defaultColours.concat("|").concat(choice.getPath());
      }

      jalview.bin.Cache.setProperty("USER_DEFINED_COLOURS", defaultColours);

      UserColourScheme ucs = loadColours(choice.getAbsolutePath());
      Color[] colors = ucs.getColours();
      schemeName.setText(ucs.getName());

      if (ucs.getLowerCaseColours() != null)
      {
        caseSensitive.setSelected(true);
        lcaseColour.setEnabled(true);
        resetButtonPanel(true);
        for (int i = 0; i < lowerCaseButtons.size(); i++)
        {
          JButton button = (JButton) lowerCaseButtons.elementAt(i);
          button.setBackground(ucs.getLowerCaseColours()[i]);
        }

      }
      else
      {
        caseSensitive.setSelected(false);
        lcaseColour.setEnabled(false);
        resetButtonPanel(false);
      }

      for (int i = 0; i < upperCaseButtons.size(); i++)
      {
        JButton button = (JButton) upperCaseButtons.elementAt(i);
        button.setBackground(colors[i]);
      }

    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public static UserColourScheme loadDefaultColours()
  {
    UserColourScheme ret = null;

    String colours = jalview.bin.Cache.getProperty("USER_DEFINED_COLOURS");
    if (colours != null)
    {
      if (colours.indexOf("|") > -1)
      {
        colours = colours.substring(0, colours.indexOf("|"));
      }

      ret = loadColours(colours);
    }

    if (ret == null)
    {
      Color[] newColours = new Color[24];
      for (int i = 0; i < 24; i++)
      {
        newColours[i] = Color.white;
      }
      ret = new UserColourScheme(newColours);
    }

    return ret;
  }

  /**
   * DOCUMENT ME!
   *
   * @param file DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  static UserColourScheme loadColours(String file)
  {
    UserColourScheme ucs = null;
    Color[] newColours = null;
    try
    {
      InputStreamReader in = new InputStreamReader(new FileInputStream(
          file), "UTF-8");

      jalview.schemabinding.version2.JalviewUserColours jucs
          = new jalview.schemabinding.version2.JalviewUserColours();

      org.exolab.castor.xml.Unmarshaller unmar
          = new org.exolab.castor.xml.Unmarshaller(jucs.getClass());
      jucs = (jalview.schemabinding.version2.JalviewUserColours) unmar.
          unmarshal(in);

      newColours = new Color[24];

      Color[] lowerCase = null;
      boolean caseSensitive = false;

      String name;
      int index;
      for (int i = 0; i < jucs.getColourCount(); i++)
      {
        name = jucs.getColour(i).getName();
        if (ResidueProperties.aa3Hash.containsKey(name))
        {
          index = ( (Integer) ResidueProperties.aa3Hash.get(name)).intValue();
        }
        else
        {
          index = ResidueProperties.aaIndex[name.charAt(0)];
        }
        if (index == -1)
        {
          continue;
        }

        if (name.toLowerCase().equals(name))
        {
          if (lowerCase == null)
          {
            lowerCase = new Color[23];
          }
          caseSensitive = true;
          lowerCase[index] = new Color(Integer.parseInt(
              jucs.getColour(i).getRGB(), 16));
        }
        else
        {
          newColours[index] = new Color(Integer.parseInt(
              jucs.getColour(i).getRGB(), 16));
        }
      }

      if (newColours != null)
      {
        ucs = new UserColourScheme(newColours);
        ucs.setName(jucs.getSchemeName());
        if (caseSensitive)
        {
          ucs.setLowerCaseColours(lowerCase);
        }
      }

    }
    catch (Exception ex)
    {
      //Could be Archive Jalview format
      try
      {
        InputStreamReader in = new InputStreamReader(new FileInputStream(
            file), "UTF-8");

        jalview.binding.JalviewUserColours jucs
            = new jalview.binding.JalviewUserColours();

        jucs = (jalview.binding.JalviewUserColours) jucs.unmarshal(in);

        newColours = new Color[jucs.getColourCount()];

        for (int i = 0; i < 24; i++)
        {
          newColours[i] = new Color(Integer.parseInt(
              jucs.getColour(i).getRGB(), 16));
        }
        if (newColours != null)
        {
          ucs = new UserColourScheme(newColours);
          ucs.setName(jucs.getSchemeName());
        }
      }
      catch (Exception ex2)
      {
        ex2.printStackTrace();
      }

      if (newColours == null)
      {
        System.out.println("Error loading User ColourFile\n" + ex);
      }
    }

    return ucs;
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void savebutton_actionPerformed(ActionEvent e)
  {
    if (schemeName.getText().trim().length() < 1)
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                            "User colour scheme must have a name!",
                                            "No name for colour scheme",
                                            JOptionPane.WARNING_MESSAGE);
      return;
    }

    if (userColourSchemes != null &&
        userColourSchemes.containsKey(schemeName.getText()))
    {
      int reply = JOptionPane.showInternalConfirmDialog(Desktop.desktop,
          "Colour scheme " + schemeName.getText() + " exists."
          + "\nContinue saving colour scheme as " + schemeName.getText() + "?",
          "Duplicate scheme name", JOptionPane.YES_NO_OPTION);
      if (reply != JOptionPane.YES_OPTION)
      {
        return;
      }

      userColourSchemes.remove(schemeName.getText());
    }
    JalviewFileChooser chooser = new JalviewFileChooser(jalview.bin.Cache.
        getProperty(
            "LAST_DIRECTORY"), new String[]
        {"jc"},
        new String[]
        {"Jalview User Colours"}, "Jalview User Colours");

    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle("Save colour scheme");
    chooser.setToolTipText("Save");

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String choice = chooser.getSelectedFile().getPath();
      String defaultColours = jalview.bin.Cache.getDefault(
          "USER_DEFINED_COLOURS", choice);
      if (defaultColours.indexOf(choice) == -1)
      {
        if (defaultColours.length() > 0)
        {
          defaultColours = defaultColours.concat("|");
        }
        defaultColours = defaultColours.concat(choice);
      }

      userColourSchemes.put(schemeName.getText(), getSchemeFromButtons());

      ap.alignFrame.updateUserColourMenu();

      jalview.bin.Cache.setProperty("USER_DEFINED_COLOURS", defaultColours);

      jalview.schemabinding.version2.JalviewUserColours ucs
          = new jalview.schemabinding.version2.JalviewUserColours();

      ucs.setSchemeName(schemeName.getText());
      try
      {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(choice), "UTF-8"));

        for (int i = 0; i < buttonPanel.getComponentCount(); i++)
        {
          JButton button = (JButton) buttonPanel.getComponent(i);
          jalview.schemabinding.version2.Colour col
              = new jalview.schemabinding.version2.Colour();
          col.setName(button.getText());
          col.setRGB(jalview.util.Format.getHexString(
              button.getBackground()));
          ucs.addColour(col);
        }

        ucs.marshal(out);
        out.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void cancelButton_actionPerformed(ActionEvent e)
  {
    if (ap != null)
    {
      if (seqGroup != null)
      {
        seqGroup.cs = oldColourScheme;
      }
      else if (ap != null)
      {
        ap.av.setGlobalColourScheme(oldColourScheme);
      }
      ap.paintAlignment(true);
    }

    if (jmol != null)
    {
      jmol.setJalviewColourScheme(oldColourScheme);
    }

    try
    {
      frame.setClosed(true);
    }
    catch (Exception ex)
    {
    }
  }

  static Hashtable userColourSchemes;

  public static Hashtable getUserColourSchemes()
  {
    return userColourSchemes;
  }

  public static void initUserColourSchemes(String files)
  {
    userColourSchemes = new Hashtable();

    if (files == null || files.length() == 0)
    {
      return;
    }

    // In case colours can't be loaded, we'll remove them
    // from the default list here.
    StringBuffer coloursFound = new StringBuffer();
    StringTokenizer st = new StringTokenizer(files, "|");
    while (st.hasMoreElements())
    {
      String file = st.nextToken();
      try
      {
        UserColourScheme ucs = loadColours(file);
        if (ucs != null)
        {
          if (coloursFound.length() > 0)
          {
            coloursFound.append("|");
          }
          coloursFound.append(file);
          userColourSchemes.put(ucs.getName(), ucs);
        }
      }
      catch (Exception ex)
      {
        System.out.println("Error loading User ColourFile\n" + ex);
      }
    }
    if (!files.equals(coloursFound.toString()))
    {
      if (coloursFound.toString().length() > 1)
      {
        jalview.bin.Cache.setProperty("USER_DEFINED_COLOURS",
                                      coloursFound.toString());
      }
      else
      {
        jalview.bin.Cache.applicationProperties.remove("USER_DEFINED_COLOURS");
      }
    }
  }

  public static void removeColourFromDefaults(String target)
  {
    // The only way to find colours by name is to load them in
    // In case colours can't be loaded, we'll remove them
    // from the default list here.

    userColourSchemes = new Hashtable();

    StringBuffer coloursFound = new StringBuffer();
    StringTokenizer st = new StringTokenizer(
        jalview.bin.Cache.getProperty("USER_DEFINED_COLOURS"), "|");

    while (st.hasMoreElements())
    {
      String file = st.nextToken();
      try
      {
        UserColourScheme ucs = loadColours(file);
        if (ucs != null && !ucs.getName().equals(target))
        {
          if (coloursFound.length() > 0)
          {
            coloursFound.append("|");
          }
          coloursFound.append(file);
          userColourSchemes.put(ucs.getName(), ucs);
        }
      }
      catch (Exception ex)
      {
        System.out.println("Error loading User ColourFile\n" + ex);
      }
    }

    if (coloursFound.toString().length() > 1)
    {
      jalview.bin.Cache.setProperty("USER_DEFINED_COLOURS",
                                    coloursFound.toString());
    }
    else
    {
      jalview.bin.Cache.applicationProperties.remove("USER_DEFINED_COLOURS");
    }

  }

  public void caseSensitive_actionPerformed(ActionEvent e)
  {
    resetButtonPanel(caseSensitive.isSelected());
    lcaseColour.setEnabled(caseSensitive.isSelected());
  }

  public void lcaseColour_actionPerformed(ActionEvent e)
  {
    if (selectedButtons == null)
    {
      selectedButtons = new Vector();
    }
    else
    {
      selectedButtons.clear();
    }
    selectedButtons.add(lcaseColour);
  }

}
