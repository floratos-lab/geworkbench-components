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

import jalview.analysis.*;
import jalview.commands.*;
import jalview.datamodel.*;
import jalview.schemes.*;
import jalview.io.AppletFormatAdapter;

public class APopupMenu
    extends java.awt.PopupMenu implements ActionListener, ItemListener
{
  Menu groupMenu = new Menu();
  MenuItem editGroupName = new MenuItem();
  protected MenuItem clustalColour = new MenuItem();
  protected MenuItem zappoColour = new MenuItem();
  protected MenuItem taylorColour = new MenuItem();
  protected MenuItem hydrophobicityColour = new MenuItem();
  protected MenuItem helixColour = new MenuItem();
  protected MenuItem strandColour = new MenuItem();
  protected MenuItem turnColour = new MenuItem();
  protected MenuItem buriedColour = new MenuItem();
  protected CheckboxMenuItem abovePIDColour = new CheckboxMenuItem();
  protected MenuItem userDefinedColour = new MenuItem();
  protected MenuItem PIDColour = new MenuItem();
  protected MenuItem BLOSUM62Colour = new MenuItem();
  MenuItem noColourmenuItem = new MenuItem();
  protected CheckboxMenuItem conservationMenuItem = new CheckboxMenuItem();

  final AlignmentPanel ap;
  MenuItem unGroupMenuItem = new MenuItem();
  MenuItem nucleotideMenuItem = new MenuItem();
  Menu colourMenu = new Menu();
  CheckboxMenuItem showBoxes = new CheckboxMenuItem();
  CheckboxMenuItem showText = new CheckboxMenuItem();
  CheckboxMenuItem showColourText = new CheckboxMenuItem();
  Menu editMenu = new Menu("Edit");
  MenuItem copy = new MenuItem("Copy (Jalview Only)");
  MenuItem cut = new MenuItem("Cut (Jalview Only)");
  MenuItem toUpper = new MenuItem("To Upper Case");
  MenuItem toLower = new MenuItem("To Lower Case");
  MenuItem toggleCase = new MenuItem("Toggle Case");
  Menu outputmenu = new Menu();
  Menu seqMenu = new Menu();
  MenuItem pdb = new MenuItem();
  MenuItem hideSeqs = new MenuItem();
  MenuItem repGroup = new MenuItem();
  MenuItem sequenceName = new MenuItem("Edit Name/Description");
  MenuItem sequenceFeature = new MenuItem("Create Sequence Feature");
  MenuItem editSequence = new MenuItem("Edit Sequence");

  Sequence seq;
  MenuItem revealAll = new MenuItem();
  Menu menu1 = new Menu();

  public APopupMenu(AlignmentPanel apanel, final Sequence seq, Vector links)
  {
    ///////////////////////////////////////////////////////////
    // If this is activated from the sequence panel, the user may want to
    // edit or annotate a particular residue. Therefore display the residue menu
    //
    // If from the IDPanel, we must display the sequence menu
    //////////////////////////////////////////////////////////

    this.ap = apanel;
    this.seq = seq;

    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    for (int i = 0; i < jalview.io.AppletFormatAdapter.WRITEABLE_FORMATS.length;
         i++)
    {
      MenuItem item = new MenuItem(jalview.io.AppletFormatAdapter.
                                   WRITEABLE_FORMATS[i]);

      item.addActionListener(this);
      outputmenu.add(item);
    }

    SequenceGroup sg = ap.av.getSelectionGroup();

    if (sg != null && sg.getSize() > 0)
    {
      editGroupName.setLabel(sg.getName());
      showText.setState(sg.getDisplayText());
      showColourText.setState(sg.getColourText());
      showBoxes.setState(sg.getDisplayBoxes());
      if (!ap.av.alignment.getGroups().contains(sg))
      {
        groupMenu.remove(unGroupMenuItem);
      }

    }
    else
    {
      remove(hideSeqs);
      remove(groupMenu);
    }

    if (links != null)
    {
      Menu linkMenu = new Menu("Link");
      MenuItem item;
      String link;
      for (int i = 0; i < links.size(); i++)
      {
        link = links.elementAt(i).toString();
        final String target = link.substring(0, link.indexOf("|"));
        item = new MenuItem(target);

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
            ap.alignFrame.showURL(url, target);
          }
        });
        linkMenu.add(item);
      }
      if (seq != null)
      {
        seqMenu.add(linkMenu);
      }
      else
      {
        add(linkMenu);
      }
    }
    if (seq != null)
    {
      seqMenu.setLabel(seq.getName());
      repGroup.setLabel("Represent Group with " + seq.getName());
    }
    else
    {
      remove(seqMenu);
    }

    if (!ap.av.hasHiddenRows)
    {
      remove(revealAll);
    }
  }

  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == abovePIDColour)
    {
      abovePIDColour_itemStateChanged();
    }
    else if (evt.getSource() == showColourText)
    {
      showColourText_itemStateChanged();
    }
    else if (evt.getSource() == showText)
    {
      showText_itemStateChanged();
    }
    else if (evt.getSource() == showBoxes)
    {
      showBoxes_itemStateChanged();
    }
  }

  public void actionPerformed(ActionEvent evt)
  {
    Object source = evt.getSource();
    if (source == clustalColour)
    {
      clustalColour_actionPerformed();
    }
    else if (source == zappoColour)
    {
      zappoColour_actionPerformed();
    }
    else if (source == taylorColour)
    {
      taylorColour_actionPerformed();
    }
    else if (source == hydrophobicityColour)
    {
      hydrophobicityColour_actionPerformed();
    }
    else if (source == helixColour)
    {
      helixColour_actionPerformed();
    }
    else if (source == strandColour)
    {
      strandColour_actionPerformed();
    }
    else if (source == turnColour)
    {
      turnColour_actionPerformed();
    }
    else if (source == buriedColour)
    {
      buriedColour_actionPerformed();
    }
    else if (source == nucleotideMenuItem)
    {
      nucleotideMenuItem_actionPerformed();
    }

    else if (source == userDefinedColour)
    {
      userDefinedColour_actionPerformed();
    }
    else if (source == PIDColour)
    {
      PIDColour_actionPerformed();
    }
    else if (source == BLOSUM62Colour)
    {
      BLOSUM62Colour_actionPerformed();
    }
    else if (source == noColourmenuItem)
    {
      noColourmenuItem_actionPerformed();
    }
    else if (source == conservationMenuItem)
    {
      conservationMenuItem_itemStateChanged();
    }
    else if (source == unGroupMenuItem)
    {
      unGroupMenuItem_actionPerformed();
    }

    else if (source == sequenceName)
    {
      editName();
    }
    else if (source == pdb)
    {
      addPDB();
    }
    else if (source == hideSeqs)
    {
      hideSequences(false);
    }
    else if (source == repGroup)
    {
      hideSequences(true);
    }
    else if (source == revealAll)
    {
      ap.av.showAllHiddenSeqs();
    }

    else if (source == editGroupName)
    {
      EditNameDialog dialog = new EditNameDialog(
          getGroup().getName(),
          getGroup().getDescription(),
          "       Group Name",
          "Group Description",
          ap.alignFrame,
          "Edit Group Name / Description",
          500,100, true);

      if (dialog.accept)
      {
        getGroup().setName(dialog.getName().replace(' ', '_'));
        getGroup().setDescription(dialog.getDescription());
      }

    }
    else if (source == copy)
    {
      ap.alignFrame.copy_actionPerformed();
    }
    else if (source == cut)
    {
      ap.alignFrame.cut_actionPerformed();
    }
    else if(source == editSequence)
    {
      SequenceGroup sg = ap.av.getSelectionGroup();

      if(sg!=null)
      {
        if (seq == null)
          seq = (Sequence) sg.getSequenceAt(0);

        EditNameDialog dialog = new EditNameDialog(seq.getSequenceAsString(
            sg.getStartRes(),
            sg.getEndRes() + 1),
                                                   null,
                                                   "Edit Sequence ",
                                                   null,

                                                   ap.alignFrame,
                                                   "Edit Sequence",
                                                   500, 100, true);

        if (dialog.accept)
        {
          EditCommand editCommand = new EditCommand(
              "Edit Sequences", EditCommand.REPLACE,
              dialog.getName().replace(' ', ap.av.getGapCharacter()),
              sg.getSequencesAsArray(ap.av.hiddenRepSequences),
              sg.getStartRes(), sg.getEndRes()+1, ap.av.alignment
              );

          ap.alignFrame.addHistoryItem(editCommand);

          ap.av.firePropertyChange("alignment", null,
                                 ap.av.getAlignment().getSequences());
        }
      }
    }
    else if (source == toUpper || source == toLower || source == toggleCase)
    {
      SequenceGroup sg = ap.av.getSelectionGroup();
      Vector regions = new Vector();
      if (sg != null)
      {
        int start = sg.getStartRes();
        int end = sg.getEndRes() + 1;

        do
        {
          if (ap.av.hasHiddenColumns)
          {
            if (start == 0)
            {
              start = ap.av.colSel.adjustForHiddenColumns(start);
            }

            end = ap.av.colSel.getHiddenBoundaryRight(start);
            if (start == end)
            {
              end = sg.getEndRes() + 1;
            }
            if (end > sg.getEndRes())
            {
              end = sg.getEndRes() + 1;
            }
          }

          regions.addElement(new int[]
                             {start, end});

          if (ap.av.hasHiddenColumns)
          {
            start = ap.av.colSel.adjustForHiddenColumns(end);
            start = ap.av.colSel.getHiddenBoundaryLeft(start) + 1;
          }
        }
        while (end < sg.getEndRes());

        int[][] startEnd = new int[regions.size()][2];
        for (int i = 0; i < regions.size(); i++)
        {
          startEnd[i] = (int[]) regions.elementAt(i);
        }

        String description;
        int caseChange;

        if (source == toggleCase)
        {
          description = "Toggle Case";
          caseChange = ChangeCaseCommand.TOGGLE_CASE;
        }
        else if (source == toUpper)
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
    else if(source == sequenceFeature)
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
        seqs[i] = sg.getSequenceAt(i);
        int start = sg.getSequenceAt(i).findPosition(sg.getStartRes());
        int end = sg.findEndRes(sg.getSequenceAt(i));
        features[i] = new SequenceFeature(null, null, null, start, end,
                                          "Jalview");
      }

      if (ap.seqPanel.seqCanvas.getFeatureRenderer()
          .amendFeatures(seqs, features, true, ap))
      {
        ap.alignFrame.sequenceFeatures.setState(true);
        ap.av.showSequenceFeatures(true);
        ap.highlightSearchResults(null);
      }
    }
    else
    {
      outputText(evt);
    }

  }

  void outputText(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer(true, ap.alignFrame);

    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame,
                                     "Selection output - " + e.getActionCommand(),
                                     600, 500);

    cap.setText(new jalview.io.AppletFormatAdapter().formatSequences(
        e.getActionCommand(),
        new Alignment(ap.av.getSelectionAsNewSequence()),
        ap.av.showJVSuffix));

  }

  void editName()
  {
    EditNameDialog dialog = new EditNameDialog(
        seq.getName(),
        seq.getDescription(),
        "       Sequence Name",
        "Sequence Description",
        ap.alignFrame,
        "Edit Sequence Name / Description",
        500,100, true);

    if (dialog.accept)
    {
      seq.setName(dialog.getName());
      seq.setDescription(dialog.getDescription());
      ap.paintAlignment(false);
    }
  }

  void addPDB()
  {
    if(seq.getPDBId()!=null)
    {
      PDBEntry entry = (PDBEntry)seq.getPDBId().firstElement();

      if ( ap.av.applet.jmolAvailable )
       new jalview.appletgui.AppletJmol(entry,
                                        new Sequence[]{seq},
                                        null,
                                        ap,
                                        AppletFormatAdapter.URL);
     else
       new MCview.AppletPDBViewer(entry,
                                  new Sequence[]{seq},
                                  null,
                                  ap,
                                  AppletFormatAdapter.URL);

    }
    else
    {
      CutAndPasteTransfer cap = new CutAndPasteTransfer(true, ap.alignFrame);
      cap.setText("Paste your PDB file here.");
      cap.setPDBImport(seq);
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame, "Paste PDB file ", 400, 300);
    }
  }

  private void jbInit()
      throws Exception
  {
    groupMenu.setLabel("Group");
    groupMenu.setLabel("Selection");
    sequenceFeature.addActionListener(this);

    editGroupName.addActionListener(this);
    unGroupMenuItem.setLabel("Remove Group");
    unGroupMenuItem.addActionListener(this);

    nucleotideMenuItem.setLabel("Nucleotide");
    nucleotideMenuItem.addActionListener(this);
    conservationMenuItem.addItemListener(this);
    abovePIDColour.addItemListener(this);
    colourMenu.setLabel("Group Colour");
    showBoxes.setLabel("Boxes");
    showBoxes.setState(true);
    showBoxes.addItemListener(this);
    sequenceName.addActionListener(this);

    showText.setLabel("Text");
    showText.addItemListener(this);
    showColourText.setLabel("Colour Text");
    showColourText.addItemListener(this);
    outputmenu.setLabel("Output to Textbox...");
    seqMenu.setLabel("Sequence");
    pdb.setLabel("View PDB Structure");
    hideSeqs.setLabel("Hide Sequences");
    repGroup.setLabel("Represent Group with");
    revealAll.setLabel("Reveal All");
    menu1.setLabel("Group");
    add(groupMenu);
    this.add(seqMenu);
    this.add(hideSeqs);
    this.add(revealAll);
    groupMenu.add(editGroupName);
    groupMenu.add(editMenu);
    groupMenu.add(outputmenu);
    groupMenu.add(sequenceFeature);
    groupMenu.add(menu1);

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
    colourMenu.addSeparator();
    colourMenu.add(abovePIDColour);
    colourMenu.add(conservationMenuItem);

    noColourmenuItem.setLabel("None");
    noColourmenuItem.addActionListener(this);

    clustalColour.setLabel("Clustalx colours");
    clustalColour.addActionListener(this);
    zappoColour.setLabel("Zappo");
    zappoColour.addActionListener(this);
    taylorColour.setLabel("Taylor");
    taylorColour.addActionListener(this);
    hydrophobicityColour.setLabel("Hydrophobicity");
    hydrophobicityColour.addActionListener(this);
    helixColour.setLabel("Helix propensity");
    helixColour.addActionListener(this);
    strandColour.setLabel("Strand propensity");
    strandColour.addActionListener(this);
    turnColour.setLabel("Turn propensity");
    turnColour.addActionListener(this);
    buriedColour.setLabel("Buried Index");
    buriedColour.addActionListener(this);
    abovePIDColour.setLabel("Above % Identity");

    userDefinedColour.setLabel("User Defined");
    userDefinedColour.addActionListener(this);
    PIDColour.setLabel("Percentage Identity");
    PIDColour.addActionListener(this);
    BLOSUM62Colour.setLabel("BLOSUM62");
    BLOSUM62Colour.addActionListener(this);
    conservationMenuItem.setLabel("Conservation");

    editMenu.add(copy);
    copy.addActionListener(this);
    editMenu.add(cut);
    cut.addActionListener(this);

    editMenu.add(editSequence);
    editSequence.addActionListener(this);

    editMenu.add(toUpper);
    toUpper.addActionListener(this);
    editMenu.add(toLower);
    toLower.addActionListener(this);
    editMenu.add(toggleCase);
    seqMenu.add(sequenceName);
    seqMenu.add(pdb);
    seqMenu.add(repGroup);
    menu1.add(unGroupMenuItem);
    menu1.add(colourMenu);
    menu1.add(showBoxes);
    menu1.add(showText);
    menu1.add(showColourText);
    toggleCase.addActionListener(this);
    pdb.addActionListener(this);
    hideSeqs.addActionListener(this);
    repGroup.addActionListener(this);
    revealAll.addActionListener(this);
  }

  void refresh()
  {
    ap.paintAlignment(true);
  }

  protected void clustalColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    sg.cs = new ClustalxColourScheme(sg.getSequences(ap.av.hiddenRepSequences),
                                     ap.av.alignment.getWidth());
    refresh();
  }

  protected void zappoColour_actionPerformed()
  {
    getGroup().cs = new ZappoColourScheme();
    refresh();
  }

  protected void taylorColour_actionPerformed()
  {
    getGroup().cs = new TaylorColourScheme();
    refresh();
  }

  protected void hydrophobicityColour_actionPerformed()
  {
    getGroup().cs = new HydrophobicColourScheme();
    refresh();
  }

  protected void helixColour_actionPerformed()
  {
    getGroup().cs = new HelixColourScheme();
    refresh();
  }

  protected void strandColour_actionPerformed()
  {
    getGroup().cs = new StrandColourScheme();
    refresh();
  }

  protected void turnColour_actionPerformed()
  {
    getGroup().cs = new TurnColourScheme();
    refresh();
  }

  protected void buriedColour_actionPerformed()
  {
    getGroup().cs = new BuriedColourScheme();
    refresh();
  }

  public void nucleotideMenuItem_actionPerformed()
  {
    getGroup().cs = new NucleotideColourScheme();
    refresh();
  }

  protected void abovePIDColour_itemStateChanged()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (abovePIDColour.getState())
    {
      sg.cs.setConsensus(AAFrequency.calculate(sg.getSequences(ap.av.
          hiddenRepSequences), 0,
                                               ap.av.alignment.getWidth()));
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

  protected void userDefinedColour_actionPerformed()
  {
    new UserDefinedColours(ap, getGroup());
  }

  protected void PIDColour_actionPerformed()
  {
    SequenceGroup sg = getGroup();
    sg.cs = new PIDColourScheme();
    sg.cs.setConsensus(AAFrequency.calculate(sg.getSequences(ap.av.
        hiddenRepSequences), 0,
                                             ap.av.alignment.getWidth()));
    refresh();
  }

  protected void BLOSUM62Colour_actionPerformed()
  {
    SequenceGroup sg = getGroup();

    sg.cs = new Blosum62ColourScheme();

    sg.cs.setConsensus(AAFrequency.calculate(sg.getSequences(ap.av.
        hiddenRepSequences), 0,
                                             ap.av.alignment.getWidth()));

    refresh();
  }

  protected void noColourmenuItem_actionPerformed()
  {
    getGroup().cs = null;
    refresh();
  }

  protected void conservationMenuItem_itemStateChanged()
  {
    SequenceGroup sg = getGroup();
    if (sg.cs == null)
    {
      return;
    }

    if (conservationMenuItem.getState())
    {

      Conservation c = new Conservation("Group",
                                        ResidueProperties.propHash, 3,
                                        sg.getSequences(ap.av.
          hiddenRepSequences), 0,
                                        ap.av.alignment.getWidth());

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

  void unGroupMenuItem_actionPerformed()
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    ap.av.alignment.deleteGroup(sg);
    ap.av.setSelectionGroup(null);
    ap.paintAlignment(true);
  }

  public void showColourText_itemStateChanged()
  {
    getGroup().setColourText(showColourText.getState());
    refresh();
  }

  public void showText_itemStateChanged()
  {
    getGroup().setDisplayText(showText.getState());
    refresh();
  }

  public void showBoxes_itemStateChanged()
  {
    getGroup().setDisplayBoxes(showBoxes.getState());
    refresh();
  }

  void hideSequences(boolean representGroup)
  {
    SequenceGroup sg = ap.av.getSelectionGroup();
    if (sg == null || sg.getSize() < 1)
    {
      ap.av.hideSequence(new SequenceI[]
                         {seq});
      return;
    }

    ap.av.setSelectionGroup(null);

    if (representGroup)
    {
      ap.av.hideRepSequences(seq, sg);

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

}
