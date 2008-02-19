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

import MCview.*;
import jalview.analysis.*;
import jalview.commands.*;
import jalview.datamodel.*;
import jalview.io.*;
import jalview.schemes.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class PopupMenu
    extends JPopupMenu
{
  JMenu groupMenu = new JMenu();
  JMenuItem groupName = new JMenuItem();
  protected JRadioButtonMenuItem clustalColour = new JRadioButtonMenuItem();
  protected JRadioButtonMenuItem zappoColour = new JRadioButtonMenuItem();
  protected JRadioButtonMenuItem taylorColour = new JRadioButtonMenuItem();
  protected JRadioButtonMenuItem hydrophobicityColour = new
      JRadioButtonMenuItem();
  protected JRadioButtonMenuItem helixColour = new JRadioButtonMenuItem();
  protected JRadioButtonMenuItem strandColour = new JRadioButtonMenuItem();
  protected JRadioButtonMenuItem turnColour = new JRadioButtonMenuItem();
  protected JRadioButtonMenuItem buriedColour = new JRadioButtonMenuItem();
  protected JCheckBoxMenuItem abovePIDColour = new JCheckBoxMenuItem();
  protected JRadioButtonMenuItem userDefinedColour = new JRadioButtonMenuItem();
  protected JRadioButtonMenuItem PIDColour = new JRadioButtonMenuItem();
  protected JRadioButtonMenuItem BLOSUM62Colour = new JRadioButtonMenuItem();
  JRadioButtonMenuItem noColourmenuItem = new JRadioButtonMenuItem();
  protected JCheckBoxMenuItem conservationMenuItem = new JCheckBoxMenuItem();
  AlignmentPanel ap;
  JMenu sequenceMenu = new JMenu();
  JMenuItem sequenceName = new JMenuItem();
  Sequence sequence;
  JMenuItem unGroupMenuItem = new JMenuItem();
  JMenuItem outline = new JMenuItem();
  JRadioButtonMenuItem nucleotideMenuItem = new JRadioButtonMenuItem();
  JMenu colourMenu = new JMenu();
  JCheckBoxMenuItem showBoxes = new JCheckBoxMenuItem();
  JCheckBoxMenuItem showText = new JCheckBoxMenuItem();
  JCheckBoxMenuItem showColourText = new JCheckBoxMenuItem();
  JMenu editMenu = new JMenu();
  JMenuItem cut = new JMenuItem();
  JMenuItem copy = new JMenuItem();
  JMenuItem upperCase = new JMenuItem();
  JMenuItem lowerCase = new JMenuItem();
  JMenuItem toggle = new JMenuItem();
  JMenu pdbMenu = new JMenu();
  JMenuItem pdbFromFile = new JMenuItem();
  JMenuItem enterPDB = new JMenuItem();
  JMenuItem discoverPDB = new JMenuItem();
  JMenu outputMenu = new JMenu();
  JMenuItem sequenceFeature = new JMenuItem();
  JMenuItem textColour = new JMenuItem();
  JMenu jMenu1 = new JMenu();
  JMenu structureMenu = new JMenu();
  JMenu viewStructureMenu = new JMenu();
 // JMenu colStructureMenu = new JMenu();
  JMenuItem editSequence = new JMenuItem();
 // JMenuItem annotationMenuItem = new JMenuItem();

  /**
   * Creates a new PopupMenu object.
   *
   * @param ap DOCUMENT ME!
   * @param seq DOCUMENT ME!
   */
  public PopupMenu(final AlignmentPanel ap, Sequence seq, Vector links)
  {
    ///////////////////////////////////////////////////////////
    // If this is activated from the sequence panel, the user may want to
    // edit or annotate a particular residue. Therefore display the residue menu
    //
    // If from the IDPanel, we must display the sequence menu
    //////////////////////////////////////////////////////////
    this.ap = ap;
    sequence = seq;

    ButtonGroup colours = new ButtonGroup();
    colours.add(noColourmenuItem);
    colours.add(clustalColour);
    colours.add(zappoColour);
    colours.add(taylorColour);
    colours.add(hydrophobicityColour);
    colours.add(helixColour);
    colours.add(strandColour);
    colours.add(turnColour);
    colours.add(buriedColour);
    colours.add(abovePIDColour);
    colours.add(userDefinedColour);
    colours.add(PIDColour);
    colours.add(BLOSUM62Colour);

    for (int i = 0; i < jalview.io.FormatAdapter.WRITEABLE_FORMATS.length; i++)
    {
      JMenuItem item = new JMenuItem(jalview.io.FormatAdapter.WRITEABLE_FORMATS[
                                     i]);

      item.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          outputText_actionPerformed(e);
        }
      });

      outputMenu.add(item);
    }

    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    if (seq != null)
    {
      sequenceMenu.setText(sequence.getName());

      JMenuItem menuItem;
      if (seq.getDatasetSequence().getPDBId() != null
          && seq.getDatasetSequence().getPDBId().size()>0)
      {
        java.util.Enumeration e = seq.getDatasetSequence().getPDBId().
            elements();

        while (e.hasMoreElements())
        {
          final PDBEntry pdb = (PDBEntry) e.nextElement();

          menuItem = new JMenuItem();
          menuItem.setText(pdb.getId());
          menuItem.addActionListener(new java.awt.event.ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              Vector seqs = new Vector();
              for (int i = 0; i < ap.av.alignment.getHeight(); i++)
              {
                Vector pdbs = ap.av.alignment.getSequenceAt(i).getDatasetSequence().getPDBId();
                if(pdbs==null)
                  continue;

                for(int p=0; p<pdbs.size(); p++)
                {
                  PDBEntry p1 = (PDBEntry)pdbs.elementAt(p);
                  if(p1.getId().equals(pdb.getId()))
                  {
                    if (!seqs.contains(ap.av.alignment.getSequenceAt(i)))
                        seqs.addElement(ap.av.alignment.getSequenceAt(i));

                      continue;
                  }
                }
              }

              SequenceI [] seqs2 = new SequenceI[seqs.size()];
              seqs.toArray(seqs2);

              new AppJmol(pdb, seqs2, null, ap);
              //  new PDBViewer(pdb, seqs2, null, ap, AppletFormatAdapter.FILE);
            }
          });
          viewStructureMenu.add(menuItem);

       /*   menuItem = new JMenuItem();
          menuItem.setText(pdb.getId());
          menuItem.addActionListener(new java.awt.event.ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              colourByStructure(pdb.getId());
            }
          });
          colStructureMenu.add(menuItem);*/
        }
      }
      else
      {
        structureMenu.remove(viewStructureMenu);
       // structureMenu.remove(colStructureMenu);
      }

      menuItem = new JMenuItem("Hide Sequences");
      menuItem.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          hideSequences(false);
        }
      });
      add(menuItem);

      if (ap.av.getSelectionGroup() != null
          && ap.av.getSelectionGroup().getSize() > 1)
      {
        menuItem = new JMenuItem("Represent Group with " + seq.getName());
        menuItem.addActionListener(new java.awt.event.ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            hideSequences(true);
          }
        });
        sequenceMenu.add(menuItem);
      }

      if (ap.av.hasHiddenRows)
      {
        final int index = ap.av.alignment.findIndex(seq);

        if (ap.av.adjustForHiddenSeqs(index) -
            ap.av.adjustForHiddenSeqs(index - 1) > 1)
        {
          menuItem = new JMenuItem("Reveal Sequences");
          menuItem.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              ap.av.showSequence(index);
              if (ap.overviewPanel != null)
              {
                ap.overviewPanel.updateOverviewImage();
              }
            }
          });
          add(menuItem);
        }

        menuItem = new JMenuItem("Reveal All");
        menuItem.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            ap.av.showAllHiddenSeqs();
            if (ap.overviewPanel != null)
            {
              ap.overviewPanel.updateOverviewImage();
            }
          }
        });

        add(menuItem);
      }

    }

    SequenceGroup sg = ap.av.getSelectionGroup();

    if (sg != null)
    {
      groupName.setText(sg.getName());

      if (sg.cs instanceof ZappoColourScheme)
      {
        zappoColour.setSelected(true);
      }
      else if (sg.cs instanceof TaylorColourScheme)
      {
        taylorColour.setSelected(true);
      }
      else if (sg.cs instanceof PIDColourScheme)
      {
        PIDColour.setSelected(true);
      }
      else if (sg.cs instanceof Blosum62ColourScheme)
      {
        BLOSUM62Colour.setSelected(true);
      }
      else if (sg.cs instanceof UserColourScheme)
      {
        userDefinedColour.setSelected(true);
      }
      else if (sg.cs instanceof HydrophobicColourScheme)
      {
        hydrophobicityColour.setSelected(true);
      }
      else if (sg.cs instanceof HelixColourScheme)
      {
        helixColour.setSelected(true);
      }
      else if (sg.cs instanceof StrandColourScheme)
      {
        strandColour.setSelected(true);
      }
      else if (sg.cs instanceof TurnColourScheme)
      {
        turnColour.setSelected(true);
      }
      else if (sg.cs instanceof BuriedColourScheme)
      {
        buriedColour.setSelected(true);
      }
      else if (sg.cs instanceof ClustalxColourScheme)
      {
        clustalColour.setSelected(true);
      }
      else
      {
        noColourmenuItem.setSelected(true);
      }

      if (sg.cs != null && sg.cs.conservationApplied())
      {
        conservationMenuItem.setSelected(true);
      }

      showText.setSelected(sg.getDisplayText());
      showColourText.setSelected(sg.getColourText());
      showBoxes.setSelected(sg.getDisplayBoxes());
    }
    else
    {
      groupMenu.setVisible(false);
      editMenu.setVisible(false);
    }

    if (!ap.av.alignment.getGroups().contains(sg))
    {
      unGroupMenuItem.setVisible(false);
    }

    if (seq == null)
    {
      sequenceMenu.setVisible(false);
      structureMenu.setVisible(false);
    }

    if (links != null && links.size() > 0)
    {
      JMenu linkMenu = new JMenu("Link");
      JMenuItem item;
      for (int i = 0; i < links.size(); i++)
      {
        String link = links.elementAt(i).toString();
        final String label = link.substring(0, link.indexOf("|"));
        item = new JMenuItem(label);
        final String url;

        if (link.indexOf("$SEQUENCE_ID$") > -1)
        {
          String id = seq.getName();
          if (id.indexOf("|") > -1)
          {
            id = id.substring(id.lastIndexOf("|") + 1);
          }

          url = link.substring(link.indexOf("|") + 1,
                               link.indexOf("$SEQUENCE_ID$"))
              + id +
              link.substring(link.indexOf("$SEQUENCE_ID$") + 13);
        }
        else
        {
          url = link.substring(link.lastIndexOf("|") + 1);
        }

        item.addActionListener(new java.awt.event.ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            showLink(url);
          }
        });

        linkMenu.add(item);
      }
      if (sequence != null)
      {
        sequenceMenu.add(linkMenu);
      }
      else
      {
        add(linkMenu);
      }
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
    groupMenu.setText("Group");
    groupMenu.setText("Selection");
    groupName.setText("Name");
    groupName.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        groupName_actionPerformed();
      }
    });
    sequenceMenu.setText("Sequence");
    sequenceName.setText("Edit Name/Description");
    sequenceName.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        sequenceName_actionPerformed();
      }
    });
    PIDColour.setFocusPainted(false);
    unGroupMenuItem.setText("Remove Group");
    unGroupMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        unGroupMenuItem_actionPerformed();
      }
    });

    outline.setText("Border colour");
    outline.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        outline_actionPerformed();
      }
    });
    nucleotideMenuItem.setText("Nucleotide");
    nucleotideMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        nucleotideMenuItem_actionPerformed();
      }
    });
    colourMenu.setText("Group Colour");
    showBoxes.setText("Boxes");
    showBoxes.setState(true);
    showBoxes.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        showBoxes_actionPerformed();
      }
    });
    showText.setText("Text");
    showText.setState(true);
    showText.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        showText_actionPerformed();
      }
    });
    showColourText.setText("Colour Text");
    showColourText.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        showColourText_actionPerformed();
      }
    });
    editMenu.setText("Edit");
    cut.setText("Cut");
    cut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cut_actionPerformed();
      }
    });
    upperCase.setText("To Upper Case");
    upperCase.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        changeCase(e);
      }
    });
    copy.setText("Copy");
    copy.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        copy_actionPerformed();
      }
    });
    lowerCase.setText("To Lower Case");
    lowerCase.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        changeCase(e);
      }
    });
    toggle.setText("Toggle Case");
    toggle.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        changeCase(e);
      }
    });
    pdbMenu.setText("Associate Structure with Sequence");
    pdbFromFile.setText("From File");
    pdbFromFile.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        pdbFromFile_actionPerformed();
      }
    });
    enterPDB.setText("Enter PDB Id");
    enterPDB.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        enterPDB_actionPerformed();
      }
    });
    discoverPDB.setText("Discover PDB ids");
    discoverPDB.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        discoverPDB_actionPerformed();
      }
    });
    outputMenu.setText("Output to Textbox...");
    sequenceFeature.setText("Create Sequence Feature");
    sequenceFeature.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        sequenceFeature_actionPerformed();
      }
    });
    textColour.setText("Text Colour");
    textColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        textColour_actionPerformed();
      }
    });
    jMenu1.setText("Group");
    structureMenu.setText("Structure");
    viewStructureMenu.setText("View Structure");
  //  colStructureMenu.setText("Colour By Structure");
    editSequence.setText("Edit Sequence...");
    editSequence.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        editSequence_actionPerformed(actionEvent);
      }
    });
   /* annotationMenuItem.setText("By Annotation");
    annotationMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        annotationMenuItem_actionPerformed(actionEvent);
      }
    });*/

    add(groupMenu);

    add(sequenceMenu);
    this.add(structureMenu);
    groupMenu.add(editMenu);
    groupMenu.add(outputMenu);
    groupMenu.add(sequenceFeature);
    groupMenu.add(jMenu1);
    sequenceMenu.add(sequenceName);
    colourMenu.add(textColour);
    colourMenu.add(noColourmenuItem);
    colourMenu.add(clustalColour);
    colourMenu.add(BLOSUM62Colour);
    colourMenu.add(PIDColour);
    colourMenu.add(zappoColour);
    colourMenu.add(taylorColour);
    colourMenu.add(hydrophobicityColour);
    colourMenu.add(helixColour);
    colourMenu.add(strandColour);
    colourMenu.add(turnColour);
    colourMenu.add(buriedColour);
    colourMenu.add(nucleotideMenuItem);
    colourMenu.add(userDefinedColour);

    if (jalview.gui.UserDefinedColours.getUserColourSchemes() != null)
    {
      java.util.Enumeration userColours = jalview.gui.UserDefinedColours.
          getUserColourSchemes().keys();

      while (userColours.hasMoreElements())
      {
        JMenuItem item = new JMenuItem(userColours.
                                       nextElement().toString());
        item.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent evt)
          {
            userDefinedColour_actionPerformed(evt);
          }
        });
        colourMenu.add(item);
      }
    }

    colourMenu.addSeparator();
    colourMenu.add(abovePIDColour);
    colourMenu.add(conservationMenuItem);
    //colourMenu.add(annotationMenuItem);
    editMenu.add(copy);
    editMenu.add(cut);
    editMenu.add(editSequence);
    editMenu.add(upperCase);
    editMenu.add(lowerCase);
    editMenu.add(toggle);
    pdbMenu.add(pdbFromFile);
    pdbMenu.add(enterPDB);
    pdbMenu.add(discoverPDB);
    jMenu1.add(groupName);
    jMenu1.add(unGroupMenuItem);
    jMenu1.add(colourMenu);
    jMenu1.add(showBoxes);
    jMenu1.add(showText);
    jMenu1.add(showColourText);
    jMenu1.add(outline);
    structureMenu.add(pdbMenu);
    structureMenu.add(viewStructureMenu);
   // structureMenu.add(colStructureMenu);
    noColourmenuItem.setText("None");
    noColourmenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        noColourmenuItem_actionPerformed();
      }
    });

    clustalColour.setText("Clustalx colours");
    clustalColour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        clustalColour_actionPerformed();
      }
    });
    zappoColour.setText("Zappo");
    zappoColour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        zappoColour_actionPerformed();
      }
    });
    taylorColour.setText("Taylor");
    taylorColour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        taylorColour_actionPerformed();
      }
    });
    hydrophobicityColour.setText("Hydrophobicity");
    hydrophobicityColour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        hydrophobicityColour_actionPerformed();
      }
    });
    helixColour.setText("Helix propensity");
    helixColour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        helixColour_actionPerformed();
      }
    });
    strandColour.setText("Strand propensity");
    strandColour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        strandColour_actionPerformed();
      }
    });
    turnColour.setText("Turn propensity");
    turnColour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        turnColour_actionPerformed();
      }
    });
    buriedColour.setText("Buried Index");
    buriedColour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        buriedColour_actionPerformed();
      }
    });
    abovePIDColour.setText("Above % Identity");
    abovePIDColour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        abovePIDColour_actionPerformed();
      }
    });
    userDefinedColour.setText("User Defined...");
    userDefinedColour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        userDefinedColour_actionPerformed(e);
      }
    });
    PIDColour.setText("Percentage Identity");
    PIDColour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        PIDColour_actionPerformed();
      }
    });
    BLOSUM62Colour.setText("BLOSUM62");
    BLOSUM62Colour.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        BLOSUM62Colour_actionPerformed();
      }
    });
    conservationMenuItem.setText("Conservation");
    conservationMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        conservationMenuItem_actionPerformed();
      }
    });
  }

  /**
   * DOCUMENT ME!
   */
  void refresh()
  {
    ap.paintAlignment(true);

    PaintRefresher.Refresh(this, ap.av.getSequenceSetId());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void clustalColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    sg.cs = new ClustalxColourScheme(sg.getSequences(ap.av.hiddenRepSequences),
                                     ap.av.alignment.getWidth());
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void zappoColour_actionPerformed()
  {
    getGroup().cs = new ZappoColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void taylorColour_actionPerformed()
  {
    getGroup().cs = new TaylorColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void hydrophobicityColour_actionPerformed()
  {
    getGroup().cs = new HydrophobicColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void helixColour_actionPerformed()
  {
    getGroup().cs = new HelixColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void strandColour_actionPerformed()
  {
    getGroup().cs = new StrandColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void turnColour_actionPerformed()
  {
    getGroup().cs = new TurnColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void buriedColour_actionPerformed()
  {
    getGroup().cs = new BuriedColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void nucleotideMenuItem_actionPerformed()
  {
    getGroup().cs = new NucleotideColourScheme();
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void abovePIDColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (abovePIDColour.isSelected())
    {
      sg.cs.setConsensus(AAFrequency.calculate(
          sg.getSequences(ap.av.hiddenRepSequences), sg.getStartRes(),
          sg.getEndRes() + 1));

      int threshold = SliderPanel.setPIDSliderSource(ap, sg.cs,
          getGroup().getName());

      sg.cs.setThreshold(threshold, ap.av.getIgnoreGapsConsensus());

      SliderPanel.showPIDSlider();
    }
    else // remove PIDColouring
    {
      sg.cs.setThreshold(0, ap.av.getIgnoreGapsConsensus());
    }

    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void userDefinedColour_actionPerformed(ActionEvent e)
  {
    SequenceGroup sg = getGroup();

    if (e.getActionCommand().equals("User Defined..."))
    {
      new UserDefinedColours(ap, sg);
    }
    else
    {
      UserColourScheme udc = (UserColourScheme) UserDefinedColours.
          getUserColourSchemes().get(e.getActionCommand());

      sg.cs = udc;
    }
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void PIDColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    sg.cs = new PIDColourScheme();
    sg.cs.setConsensus(AAFrequency.calculate(sg.getSequences(ap.av.
        hiddenRepSequences),
                                             sg.getStartRes(),
                                             sg.getEndRes() + 1));
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void BLOSUM62Colour_actionPerformed()
  {
    SequenceGroup sg = getGroup();

    sg.cs = new Blosum62ColourScheme();

    sg.cs.setConsensus(AAFrequency.calculate(sg.getSequences(ap.av.
        hiddenRepSequences),
                                             sg.getStartRes(),
                                             sg.getEndRes() + 1));

    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void noColourmenuItem_actionPerformed()
  {
    getGroup().cs = null;
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void conservationMenuItem_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (conservationMenuItem.isSelected())
    {
      Conservation c = new Conservation("Group",
                                        ResidueProperties.propHash, 3,
                                        sg.getSequences(ap.av.
          hiddenRepSequences),
                                        sg.getStartRes(),
                                        sg.getEndRes() + 1);

      c.calculate();
      c.verdict(false, ap.av.ConsPercGaps);

      sg.cs.setConservation(c);

      SliderPanel.setConservationSlider(ap, sg.cs, sg.getName());
      SliderPanel.showConservationSlider();
    }
    else // remove ConservationColouring
    {
      sg.cs.setConservation(null);
    }

    refresh();
  }

  public void annotationMenuItem_actionPerformed(ActionEvent actionEvent)
  {
    SequenceGroup sg = getGroup();
    if (sg == null)
    {
      return;
    }

    AnnotationColourGradient acg = new AnnotationColourGradient(
        sequence.getAnnotation()[0], null, AnnotationColourGradient.NO_THRESHOLD);

    acg.predefinedColours = true;
    sg.cs = acg;

    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void groupName_actionPerformed()
  {

    SequenceGroup sg = getGroup();
    EditNameDialog dialog = new EditNameDialog(sg.getName(),
                                               sg.getDescription(),
                                               "       Group Name ",
                                               "Group Description ",
                                               "Edit Group Name/Description");

    if (!dialog.accept)
    {
      return;
    }

    sg.setName(dialog.getName());
    sg.setDescription(dialog.getDescription());
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  SequenceGroup getGroup()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    // this method won't add a new group if it already exists
    if (sg != null)
    {
      ap.av.alignment.addGroup(sg);
    }

    return sg;
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  void sequenceName_actionPerformed()
  {
    EditNameDialog dialog = new EditNameDialog(sequence.getName(),
                                               sequence.getDescription(),
                                               "       Sequence Name ",
                                               "Sequence Description ",
                                               "Edit Sequence Name/Description");

    if (!dialog.accept)
    {
      return;
    }

    if (dialog.getName() != null)
    {
      if (dialog.getName().indexOf(" ") > -1)
      {
        JOptionPane.showMessageDialog(ap,
                                      "Spaces have been converted to \"_\"",
                                      "No spaces allowed in Sequence Name",
                                      JOptionPane.WARNING_MESSAGE);
      }

      sequence.setName(dialog.getName().replace(' ', '_'));
      ap.paintAlignment(false);
    }

    sequence.setDescription(dialog.getDescription());

    ap.av.firePropertyChange("alignment", null,
                             ap.av.getAlignment().getSequences());

  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  void unGroupMenuItem_actionPerformed()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    ap.av.alignment.deleteGroup(sg);
    ap.av.setSelectionGroup(null);
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void outline_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    Color col = JColorChooser.showDialog(this, "Select Outline Colour",
                                         Color.BLUE);

    if (col != null)
    {
      sg.setOutlineColour(col);
    }

    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void showBoxes_actionPerformed()
  {
    getGroup().setDisplayBoxes(showBoxes.isSelected());
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void showText_actionPerformed()
  {
    getGroup().setDisplayText(showText.isSelected());
    refresh();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void showColourText_actionPerformed()
  {
    getGroup().setColourText(showColourText.isSelected());
    refresh();
  }

  public void showLink(String url)
  {
    try
    {
      jalview.util.BrowserLauncher.openURL(url);
    }
    catch (Exception ex)
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                            "Unixers: Couldn't find default web browser."
                                            +
          "\nAdd the full path to your browser in Preferences.",
                                            "Web browser not found",
                                            JOptionPane.WARNING_MESSAGE);

      ex.printStackTrace();
    }
  }

  void hideSequences(boolean representGroup)
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    if (sg == null || sg.getSize() < 1)
    {
      ap.av.hideSequence(new SequenceI[]
                         {sequence});
      return;
    }

    ap.av.setSelectionGroup(null);

    if (representGroup)
    {
      ap.av.hideRepSequences(sequence, sg);

      return;
    }

    int gsize = sg.getSize();
    SequenceI[] hseqs;

    hseqs = new SequenceI[gsize];

    int index = 0;
    for (int i = 0; i < gsize; i++)
    {
      hseqs[index++] = sg.getSequenceAt(i);
    }

    ap.av.hideSequence(hseqs);
  }

  public void copy_actionPerformed()
  {
    ap.alignFrame.copy_actionPerformed(null);
  }

  public void cut_actionPerformed()
  {
    ap.alignFrame.cut_actionPerformed(null);
  }

  void changeCase(ActionEvent e)
  {
    Object source = e.getSource();
    SequenceGroup sg = ap.av.getSelectionGroup();

    if (sg != null)
    {
      int[][] startEnd = ap.av.getVisibleRegionBoundaries(
          sg.getStartRes(), sg.getEndRes() + 1);

      String description;
      int caseChange;

      if (source == toggle)
      {
        description = "Toggle Case";
        caseChange = ChangeCaseCommand.TOGGLE_CASE;
      }
      else if (source == upperCase)
      {
        description = "To Upper Case";
        caseChange = ChangeCaseCommand.TO_UPPER;
      }
      else
      {
        description = "To Lower Case";
        caseChange = ChangeCaseCommand.TO_LOWER;
      }

      ChangeCaseCommand caseCommand = new ChangeCaseCommand(
          description, sg.getSequencesAsArray(ap.av.hiddenRepSequences),
          startEnd, caseChange
          );

      ap.alignFrame.addHistoryItem(caseCommand);

      ap.av.firePropertyChange("alignment", null,
                               ap.av.getAlignment().getSequences());

    }
  }

  public void outputText_actionPerformed(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    cap.setForInput(null);
    Desktop.addInternalFrame(cap,
                             "Alignment output - " + e.getActionCommand(), 600,
                             500);

    String[] omitHidden = null;

    if (ap.av.hasHiddenColumns)
    {
      System.out.println("PROMPT USER HERE");
      omitHidden = ap.av.getViewAsString(true);
    }

    cap.setText(new FormatAdapter().formatSequences(
        e.getActionCommand(),
        ap.av.getSelectionAsNewSequence(),
        omitHidden));
  }

  public void pdbFromFile_actionPerformed()
  {
    jalview.io.JalviewFileChooser chooser
        = new jalview.io.JalviewFileChooser(jalview.bin.Cache.
                                            getProperty(
                                                "LAST_DIRECTORY"));
    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle("Select a PDB file");
    chooser.setToolTipText("Load a PDB file");

    int value = chooser.showOpenDialog(null);

    if (value == jalview.io.JalviewFileChooser.APPROVE_OPTION)
    {
      PDBEntry entry = new PDBEntry();
      String choice = chooser.getSelectedFile().getPath();
      jalview.bin.Cache.setProperty("LAST_DIRECTORY", choice);
      try
      {
        MCview.PDBfile pdbfile = new MCview.PDBfile(choice,
            jalview.io.AppletFormatAdapter.FILE);

        if (pdbfile.id == null)
        {
          String reply = JOptionPane.showInternalInputDialog(
              Desktop.desktop,
              "Couldn't find a PDB id in the file supplied."
              + "Please enter an Id to identify this structure.",
              "No PDB Id in File", JOptionPane.QUESTION_MESSAGE);
          if (reply == null)
          {
            return;
          }

          entry.setId(reply);
        }
        else
        {
          entry.setId(pdbfile.id);
        }
      }
      catch (java.io.IOException ex)
      {
        ex.printStackTrace();
      }

      entry.setFile(choice);
      sequence.getDatasetSequence().addPDBId(entry);
    }

  }

  public void enterPDB_actionPerformed()
  {
    String id = JOptionPane.showInternalInputDialog(Desktop.desktop,
        "Enter PDB Id", "Enter PDB Id", JOptionPane.QUESTION_MESSAGE);

    if (id != null && id.length() > 0)
    {
      PDBEntry entry = new PDBEntry();
      entry.setId(id.toUpperCase());
      sequence.getDatasetSequence()
          .addPDBId(entry);
    }
  }

  public void discoverPDB_actionPerformed()
  {
    SequenceI[] sequences =
         ap.av.selectionGroup == null ?
           new Sequence[]{sequence}
         : ap.av.selectionGroup.getSequencesInOrder(ap.av.alignment);

    new jalview.io.DBRefFetcher(sequences,
        ap.alignFrame).fetchDBRefs(false);
  }

  public void sequenceFeature_actionPerformed()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    if (sg == null)
    {
      return;
    }

    int gSize = sg.getSize();
    SequenceI[] seqs = new SequenceI[gSize];
    SequenceFeature[] features = new SequenceFeature[gSize];

    for (int i = 0; i < gSize; i++)
    {
      seqs[i] = sg.getSequenceAt(i).getDatasetSequence();
      int start = sg.getSequenceAt(i).findPosition(sg.getStartRes());
      int end = sg.findEndRes(sg.getSequenceAt(i));
      features[i] = new SequenceFeature(null, null, null, start, end, "Jalview");
    }

    if (ap.seqPanel.seqCanvas.getFeatureRenderer()
        .amendFeatures(seqs, features, true, ap))
    {
      ap.alignFrame.showSeqFeatures.setSelected(true);
      ap.av.setShowSequenceFeatures(true);
      ap.highlightSearchResults(null);
    }
  }

  public void textColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    if (sg != null)
    {
      new TextColourChooser().chooseColour(ap, sg);
    }
  }

  public void colourByStructure(String pdbid)
  {
    Annotation [] anots = jalview.structure.StructureSelectionManager.getStructureSelectionManager()
        .colourSequenceFromStructure(sequence, pdbid);

    AlignmentAnnotation an = new AlignmentAnnotation(
      "Structure", "Coloured by "+pdbid, anots);

    ap.av.alignment.addAnnotation(an);
    an.createSequenceMapping(sequence, 0, true);
    //an.adjustForAlignment();
    ap.av.alignment.setAnnotationIndex(an,0);

    ap.adjustAnnotationHeight();

    sequence.addAlignmentAnnotation(an);

    }

  public void editSequence_actionPerformed(ActionEvent actionEvent)
  {
      SequenceGroup sg = ap.av.getSelectionGroup();

      if(sg!=null)
      {
        if (sequence == null)
          sequence = (Sequence) sg.getSequenceAt(0);

        EditNameDialog dialog = new EditNameDialog(
            sequence.getSequenceAsString(
                sg.getStartRes(),
                sg.getEndRes() + 1),
            null,
            "Edit Sequence ",
            null,
            "Edit Sequence");

        if (dialog.accept)
        {
          EditCommand editCommand = new EditCommand(
              "Edit Sequences", EditCommand.REPLACE,
              dialog.getName().replace(' ', ap.av.getGapCharacter()),
              sg.getSequencesAsArray(ap.av.hiddenRepSequences),
              sg.getStartRes(), sg.getEndRes() + 1, ap.av.alignment
              );

          ap.alignFrame.addHistoryItem(editCommand);

          ap.av.firePropertyChange("alignment", null,
                                   ap.av.getAlignment().getSequences());
        }
      }
  }


}
