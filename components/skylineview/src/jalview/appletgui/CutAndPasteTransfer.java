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

import java.awt.*;
import java.awt.event.*;

import jalview.datamodel.*;
import jalview.io.*;

public class CutAndPasteTransfer
    extends Panel implements ActionListener, MouseListener
{
  boolean pdbImport = false;
  boolean treeImport = false;
  boolean annotationImport = false;
  Sequence  seq;
  AlignFrame alignFrame;

  public CutAndPasteTransfer(boolean forImport, AlignFrame alignFrame)
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    this.alignFrame = alignFrame;

    if (!forImport)
    {
      buttonPanel.setVisible(false);
    }
  }

  public String getText()
  {
    return textarea.getText();
  }

  public void setText(String text)
  {
    textarea.setText(text);
  }

  public void setPDBImport(Sequence seq)
  {
    this.seq = seq;
    accept.setLabel("Accept");
    addSequences.setVisible(false);
    pdbImport = true;
  }

  public void setTreeImport()
  {
    treeImport = true;
    accept.setLabel("Accept");
    addSequences.setVisible(false);
  }

  public void setAnnotationImport()
  {
    annotationImport = true;
    accept.setLabel("Accept");
    addSequences.setVisible(false);
  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == accept)
    {
      ok(true);
    }
    else if (evt.getSource() == addSequences)
    {
      ok(false);
    }
    else if (evt.getSource() == cancel)
    {
      cancel();
    }
  }

  protected void ok(boolean newWindow)
  {
    String text = getText();
    int length = text.length();
    textarea.append("\n");
    if (textarea.getText().length() == length)
    {
      String warning =
          "\n\n#################################################\n"
          + "WARNING!! THIS IS THE MAXIMUM SIZE OF TEXTAREA!!\n"
          + "\nCAN'T INPUT FULL ALIGNMENT"
          + "\n\nYOU MUST DELETE THIS WARNING TO CONTINUE"
          + "\n\nMAKE SURE LAST SEQUENCE PASTED IS COMPLETE"
          + "\n#################################################\n";
      textarea.setText(text.substring(0, text.length() - warning.length())
                       + warning);

      textarea.setCaretPosition(text.length());
    }

    if (pdbImport)
    {
      PDBEntry pdb = new PDBEntry();
      pdb.setFile(text);

      if ( alignFrame.alignPanel.av.applet.jmolAvailable )
        new jalview.appletgui.AppletJmol(pdb,
                                         new Sequence[]{seq},
                                         null,
                                         alignFrame.alignPanel,
                                         AppletFormatAdapter.PASTE);
      else

        new MCview.AppletPDBViewer(pdb,
                                   new Sequence[]{seq},
                                   null,
                                   alignFrame.alignPanel,
                                   AppletFormatAdapter.PASTE);

    }
    else if (treeImport)
    {
      try
      {
        jalview.io.NewickFile fin = new jalview.io.NewickFile(textarea.getText(),
            "Paste");

        fin.parse();
        if (fin.getTree() != null)
        {
          alignFrame.loadTree(fin, "Pasted tree file");
        }

      }
      catch (Exception ex)
      {
        textarea.setText("Could not parse Newick file!\n" + ex);
        return;
      }
    }
    else if (annotationImport)
    {
      if (new AnnotationFile().readAnnotationFile(
          alignFrame.viewport.alignment, textarea.getText(),
          jalview.io.AppletFormatAdapter.PASTE))
      {
        alignFrame.alignPanel.fontChanged();
        alignFrame.alignPanel.setScrollValues(0, 0);

      }
      else
      {
        alignFrame.parseFeaturesFile(textarea.getText(),
                                     jalview.io.AppletFormatAdapter.PASTE);
      }
    }
    else if (alignFrame != null)
    {
      Alignment al = null;

      String format = new IdentifyFile().Identify(text,
                                                  AppletFormatAdapter.PASTE);
      try
      {
        al = new AppletFormatAdapter().readFile(text, AppletFormatAdapter.PASTE,
                                                format);
      }
      catch (java.io.IOException ex)
      {
        ex.printStackTrace();
      }

      if (al != null)
      {
        if (newWindow)
        {
          AlignFrame af = new AlignFrame(al, alignFrame.viewport.applet,
                                         "Cut & Paste input - " + format,
                                         false);
          af.statusBar.setText("Successfully pasted alignment file");
        }
        else
        {
          alignFrame.addSequences(al.getSequencesArray());
        }
      }
    }

    if (this.getParent() instanceof Frame)
    {
      ( (Frame)this.getParent()).setVisible(false);
    }
    else
    {
      ( (Dialog)this.getParent()).setVisible(false);
    }
  }

  protected void cancel()
  {
    textarea.setText("");
    if (this.getParent() instanceof Frame)
    {
      ( (Frame)this.getParent()).setVisible(false);
    }
    else
    {
      ( (Dialog)this.getParent()).setVisible(false);
    }
  }

  protected TextArea textarea = new TextArea();
  Button accept = new Button("New Window");
  Button addSequences = new Button("Add to Current Alignment");
  Button cancel = new Button("Close");

  protected Panel buttonPanel = new Panel();
  BorderLayout borderLayout1 = new BorderLayout();

  private void jbInit()
      throws Exception
  {
    textarea.setFont(new java.awt.Font("Monospaced", Font.PLAIN, 10));
    textarea.setText("Paste your alignment file here");
    textarea.addMouseListener(this);
    this.setLayout(borderLayout1);
    accept.addActionListener(this);
    addSequences.addActionListener(this);
    cancel.addActionListener(this);
    this.add(buttonPanel, BorderLayout.SOUTH);
    buttonPanel.add(accept, null);
    buttonPanel.add(addSequences);
    buttonPanel.add(cancel, null);
    this.add(textarea, java.awt.BorderLayout.CENTER);
  }

  public void mousePressed(MouseEvent evt)
  {
    if (textarea.getText().startsWith("Paste your"))
    {
      textarea.setText("");
    }
  }

  public void mouseReleased(MouseEvent evt)
  {}

  public void mouseClicked(MouseEvent evt)
  {}

  public void mouseEntered(MouseEvent evt)
  {}

  public void mouseExited(MouseEvent evt)
  {}
}
