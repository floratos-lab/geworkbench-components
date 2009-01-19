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

import jalview.datamodel.*;
import jalview.schemes.*;

public class UserDefinedColours
    extends Panel implements ActionListener, AdjustmentListener
{

  AlignmentPanel ap;
  SequenceGroup seqGroup;
  Button selectedButton;
  Vector oldColours = new Vector();
  ColourSchemeI oldColourScheme;
  Frame frame;
  MCview.AppletPDBCanvas pdbcanvas;
  AppletJmol jmol;

  Dialog dialog;
  Object caller;
  String originalLabel;
  Color originalColour;

  int R = 0, G = 0, B = 0;

  public ColourSchemeI loadDefaultColours()
  {
    // NOT IMPLEMENTED YET IN APPLET VERSION
    return null;
  }

  public UserDefinedColours(AlignmentPanel ap, SequenceGroup sg)
  {
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

    init();
  }

  public UserDefinedColours(MCview.AppletPDBCanvas pdb)
  {
    this.pdbcanvas = pdb;
    init();
  }

  public UserDefinedColours(AppletJmol jmol)
  {
    this.jmol = jmol;
    init();
  }

  public UserDefinedColours(FeatureRenderer fr, Frame alignframe)
  {
    caller = fr;
    originalColour = fr.colourPanel.getBackground();
    originalLabel = "Feature Colour";
    setForDialog("Select Feature Colour", alignframe);
    setTargetColour(fr.colourPanel.getBackground());
    dialog.setVisible(true);
  }

  public UserDefinedColours(Component caller,
                            Color col1,
                            Frame alignframe)
  {
    this.caller = caller;
    originalColour = col1;
    originalLabel = "Select Colour";
    setForDialog("Select Colour", alignframe);
    setTargetColour(col1);
    dialog.setVisible(true);
  }


  public UserDefinedColours(Object caller,
                            String label,
                            Color colour)
  {
    this.caller = caller;
    originalColour = colour;
    originalLabel = label;
    init();
    remove(buttonPanel);

    setTargetColour(colour);

    okcancelPanel.setBounds(new Rectangle(0, 113, 400, 35));
    frame.setTitle("User Defined Colours - " + label);
    frame.setSize(420, 200);
  }

  void setForDialog(String title, Frame alignframe)
  {
    init();
    frame.setVisible(false);
    remove(buttonPanel);
    dialog = new Dialog(alignframe, title, true);

    dialog.add(this);
    this.setSize(400,123);
    okcancelPanel.setBounds(new Rectangle(0, 123, 400, 35));
    int height = 160 + alignframe.getInsets().top + getInsets().bottom;
    int width = 400;

    dialog.setBounds(alignframe.getBounds().x
              + (alignframe.getSize().width - width) / 2,
              alignframe.getBounds().y
              + (alignframe.getSize().height - height) / 2,
              width, height);

  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == okButton)
    {
      okButton_actionPerformed();
    }
    else if (evt.getSource() == applyButton)
    {
      applyButton_actionPerformed();
    }
    else if (evt.getSource() == cancelButton)
    {
      cancelButton_actionPerformed();
    }
    else if (evt.getSource() == rText)
    {
      rText_actionPerformed();
    }
    else if (evt.getSource() == gText)
    {
      gText_actionPerformed();
    }
    else if (evt.getSource() == bText)
    {
      bText_actionPerformed();
    }
  }

  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    if (evt.getSource() == rScroller)
    {
      rScroller_adjustmentValueChanged();
    }
    else if (evt.getSource() == gScroller)
    {
      gScroller_adjustmentValueChanged();
    }
    else if (evt.getSource() == bScroller)
    {
      bScroller_adjustmentValueChanged();
    }
  }

  void init()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    frame = new Frame();
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame, "User defined colours", 420, 345);

    if (seqGroup != null)
    {
      frame.setTitle(frame.getTitle() + " (" + seqGroup.getName() + ")");
    }

    for (int i = 0; i < 20; i++)
    {
      makeButton(ResidueProperties.aa2Triplet.get(ResidueProperties.aa[i]) +
                 "", ResidueProperties.aa[i]);
    }

    makeButton("B", "B");
    makeButton("Z", "Z");
    makeButton("X", "X");
    makeButton("Gap", "'.','-',' '");

    validate();
  }

  protected void rText_actionPerformed()
  {
    try
    {
      int i = Integer.parseInt(rText.getText());
      rScroller.setValue(i);
      rScroller_adjustmentValueChanged();
    }
    catch (NumberFormatException ex)
    {}
  }

  protected void gText_actionPerformed()
  {
    try
    {
      int i = Integer.parseInt(gText.getText());
      gScroller.setValue(i);
      gScroller_adjustmentValueChanged();
    }
    catch (NumberFormatException ex)
    {}

  }

  protected void bText_actionPerformed()
  {
    try
    {
      int i = Integer.parseInt(bText.getText());
      bScroller.setValue(i);
      bScroller_adjustmentValueChanged();
    }
    catch (NumberFormatException ex)
    {}

  }

  protected void rScroller_adjustmentValueChanged()
  {
    R = rScroller.getValue();
    rText.setText(R + "");
    colourChanged();
  }

  protected void gScroller_adjustmentValueChanged()
  {
    G = gScroller.getValue();
    gText.setText(G + "");
    colourChanged();
  }

  protected void bScroller_adjustmentValueChanged()
  {
    B = bScroller.getValue();
    bText.setText(B + "");
    colourChanged();
  }

  public void colourChanged()
  {
    Color col = new Color(R, G, B);
    target.setBackground(col);
    target.repaint();

    if (selectedButton != null)
    {
      selectedButton.setBackground(col);
      selectedButton.repaint();
    }
  }

  void setTargetColour(Color col)
  {
    R = col.getRed();
    G = col.getGreen();
    B = col.getBlue();

    rScroller.setValue(R);
    gScroller.setValue(G);
    bScroller.setValue(B);
    rText.setText(R + "");
    gText.setText(G + "");
    bText.setText(B + "");
    colourChanged();
  }

  public void colourButtonPressed(MouseEvent e)
  {
    selectedButton = (Button) e.getSource();
    setTargetColour(selectedButton.getBackground());
  }

  void makeButton(String label, String aa)
  {
    final Button button = new Button();
    Color col = Color.white;

    try
    {
      col = oldColourScheme.findColour(aa.charAt(0), -1);
    }
    catch (Exception ex)
    {}

    button.setBackground(col);
    oldColours.addElement(col);
    button.setLabel(label);
    button.setForeground(col.darker().darker().darker());
    button.setFont(new java.awt.Font("Verdana", 1, 10));
    button.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        colourButtonPressed(e);
      }
    });

    buttonPanel.add(button, null);
  }

  protected void okButton_actionPerformed()
  {
    applyButton_actionPerformed();
    if (dialog != null)
      dialog.setVisible(false);

    frame.setVisible(false);
  }

  public Color getColor()
  {
    return new Color(R, G, B);
  }

  protected void applyButton_actionPerformed()
  {
    if (caller != null)
    {
      if (caller instanceof FeatureSettings)
      {
        ( (FeatureSettings) caller).setUserColour
            (originalLabel, getColor());
      }
      else if (caller instanceof AnnotationColourChooser)
      {
        if (originalLabel.equals("Min Colour"))
        {
          ( (AnnotationColourChooser) caller).minColour_actionPerformed
              (getColor());
        }
        else
        {
          ( (AnnotationColourChooser) caller).maxColour_actionPerformed
              (getColor());
        }
      }
      else if(caller instanceof FeatureRenderer)
      {
        ((FeatureRenderer)caller).colourPanel.setBackground(getColor());
      }

      return;
    }

    Color[] newColours = new Color[24];
    for (int i = 0; i < 24; i++)
    {
      Button button = (Button) buttonPanel.getComponent(i);
      newColours[i] = button.getBackground();
    }

    UserColourScheme ucs = new UserColourScheme(newColours);
    if (ap != null)
    {
      ucs.setThreshold(0, ap.av.getIgnoreGapsConsensus());
    }

    if (ap != null)
    {
      if (seqGroup != null)
      {
        seqGroup.cs = ucs;
      }
      else
      {
        ap.av.setGlobalColourScheme(ucs);
      }
      ap.seqPanel.seqCanvas.img = null;
      ap.paintAlignment(true);
    }
    else if(jmol!=null)
    {
      jmol.setJalviewColourScheme(ucs);
    }
    else if (pdbcanvas != null)
    {
      pdbcanvas.setColours(ucs);
    }
  }

  protected void cancelButton_actionPerformed()
  {
    if (caller != null)
    {
      if (caller instanceof FeatureSettings)
      {
        ( (FeatureSettings) caller).setUserColour
            (originalLabel, originalColour);
      }
      else if (caller instanceof AnnotationColourChooser)
      {
        if (originalLabel.equals("Min Colour"))
        {
          ( (AnnotationColourChooser) caller).minColour_actionPerformed
              (originalColour);
        }
        else
        {
          ( (AnnotationColourChooser) caller).maxColour_actionPerformed
              (originalColour);
        }
      }
      else if (caller instanceof FeatureRenderer)
      {
        ( (FeatureRenderer) caller).colourPanel.setBackground(originalColour);

      }

      if(dialog!=null)
        dialog.setVisible(false);

      frame.setVisible(false);
      return;
    }

    Color[] newColours = new Color[24];
    for (int i = 0; i < 24; i++)
    {
      newColours[i] = (Color) oldColours.elementAt(i);
      buttonPanel.getComponent(i).setBackground(newColours[i]);
    }

    UserColourScheme ucs = new UserColourScheme(newColours);

    if (ap != null)
    {
      if (seqGroup != null)
      {
        seqGroup.cs = ucs;
      }
      else
      {
        ap.av.setGlobalColourScheme(ucs);
      }
      ap.paintAlignment(true);
    }
    else if(jmol !=null)
    {
      jmol.setJalviewColourScheme(ucs);
    }
    else if (pdbcanvas != null)
    {
      pdbcanvas.pdb.setColours(ucs);
    }

    frame.setVisible(false);
  }

  protected Panel buttonPanel = new Panel();
  protected GridLayout gridLayout = new GridLayout();
  Panel okcancelPanel = new Panel();
  protected Button okButton = new Button();
  protected Button applyButton = new Button();
  protected Button cancelButton = new Button();
  protected Scrollbar rScroller = new Scrollbar();
  Label label1 = new Label();
  protected TextField rText = new TextField();
  Label label4 = new Label();
  protected Scrollbar gScroller = new Scrollbar();
  protected TextField gText = new TextField();
  Label label5 = new Label();
  protected Scrollbar bScroller = new Scrollbar();
  protected TextField bText = new TextField();
  protected Panel target = new Panel();

  private void jbInit()
      throws Exception
  {
    this.setLayout(null);
    buttonPanel.setLayout(gridLayout);
    gridLayout.setColumns(6);
    gridLayout.setRows(4);
    okButton.setFont(new java.awt.Font("Verdana", 0, 11));
    okButton.setLabel("OK");
    okButton.addActionListener(this);
    applyButton.setFont(new java.awt.Font("Verdana", 0, 11));
    applyButton.setLabel("Apply");
    applyButton.addActionListener(this);
    cancelButton.setFont(new java.awt.Font("Verdana", 0, 11));
    cancelButton.setLabel("Cancel");
    cancelButton.addActionListener(this);
    this.setBackground(new Color(212, 208, 223));
    okcancelPanel.setBounds(new Rectangle(0, 265, 400, 35));
    buttonPanel.setBounds(new Rectangle(0, 123, 400, 142));
    rScroller.setMaximum(256);
    rScroller.setMinimum(0);
    rScroller.setOrientation(0);
    rScroller.setUnitIncrement(1);
    rScroller.setVisibleAmount(1);
    rScroller.setBounds(new Rectangle(36, 27, 119, 19));
    rScroller.addAdjustmentListener(this);
    label1.setAlignment(Label.RIGHT);
    label1.setText("R");
    label1.setBounds(new Rectangle(19, 30, 16, 15));
    rText.setFont(new java.awt.Font("Dialog", Font.PLAIN, 10));
    rText.setText("0        ");
    rText.setBounds(new Rectangle(156, 27, 53, 19));
    rText.addActionListener(this);
    label4.setAlignment(Label.RIGHT);
    label4.setText("G");
    label4.setBounds(new Rectangle(15, 56, 20, 15));
    gScroller.setMaximum(256);
    gScroller.setMinimum(0);
    gScroller.setOrientation(0);
    gScroller.setUnitIncrement(1);
    gScroller.setVisibleAmount(1);
    gScroller.setBounds(new Rectangle(35, 52, 120, 20));
    gScroller.addAdjustmentListener(this);
    gText.setFont(new java.awt.Font("Dialog", Font.PLAIN, 10));
    gText.setText("0        ");
    gText.setBounds(new Rectangle(156, 52, 53, 20));
    gText.addActionListener(this);
    label5.setAlignment(Label.RIGHT);
    label5.setText("B");
    label5.setBounds(new Rectangle(14, 82, 20, 15));
    bScroller.setMaximum(256);
    bScroller.setMinimum(0);
    bScroller.setOrientation(0);
    bScroller.setUnitIncrement(1);
    bScroller.setVisibleAmount(1);
    bScroller.setBounds(new Rectangle(35, 78, 120, 20));
    bScroller.addAdjustmentListener(this);
    bText.setFont(new java.awt.Font("Dialog", Font.PLAIN, 10));
    bText.setText("0        ");
    bText.setBounds(new Rectangle(157, 78, 52, 20));
    bText.addActionListener(this);
    target.setBackground(Color.black);
    target.setBounds(new Rectangle(229, 26, 134, 79));
    this.add(okcancelPanel, null);
    okcancelPanel.add(okButton, null);
    okcancelPanel.add(applyButton, null);
    okcancelPanel.add(cancelButton, null);
    this.add(buttonPanel, null);
    this.add(target, null);
    this.add(gScroller);
    this.add(rScroller);
    this.add(bScroller);
    this.add(label5);
    this.add(label4);
    this.add(label1);
    this.add(gText);
    this.add(rText);
    this.add(bText);
  }

}
