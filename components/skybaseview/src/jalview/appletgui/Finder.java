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

public class Finder
    extends Panel implements ActionListener
{
  AlignViewport av;
  AlignmentPanel ap;
  Frame frame;

  SearchResults searchResults;

  int seqIndex = 0;
  int resIndex = 0;
  public Finder(final AlignmentPanel ap)
  {
    try
    {
      jbInit();

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    this.av = ap.av;
    this.ap = ap;
    frame = new Frame();
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame, "Find", 340, 120);
    frame.repaint();
    frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent evt)
      {
        ap.highlightSearchResults(null);
      }
    });
    textfield.requestFocus();
  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == textfield)
    {
      doSearch(false);
    }

    else if (evt.getSource() == findNext)
    {
      doSearch(false);
    }

    else if (evt.getSource() == findAll)
    {
      resIndex = 0;
      seqIndex = 0;
      doSearch(true);
    }
    else if (evt.getSource() == createNewGroup)
    {
      createNewGroup_actionPerformed();
    }
  }

  public void createNewGroup_actionPerformed()
  {
    SequenceI[] seqs = new SequenceI[searchResults.getSize()];
    SequenceFeature[] features = new SequenceFeature[searchResults.getSize()];

    for (int i = 0; i < searchResults.getSize(); i++)
    {
      seqs[i] = searchResults.getResultSequence(i);

      features[i] = new SequenceFeature(textfield.getText().trim(),
                                        "Search Results", null,
                                        searchResults.getResultStart(i),
                                        searchResults.getResultEnd(i),
                                        "Search Results");
    }

    if (ap.seqPanel.seqCanvas.getFeatureRenderer()
        .amendFeatures(seqs, features, true, ap))
    {
      ap.alignFrame.sequenceFeatures.setState(true);
      av.showSequenceFeatures(true);
      ap.highlightSearchResults(null);
    }
  }

  void doSearch(boolean findAll)
  {
    if (jalview.bin.JalviewLite.currentAlignFrame != null)
    {
      ap = jalview.bin.JalviewLite.currentAlignFrame.alignPanel;
      av = ap.av;
    }
    createNewGroup.setEnabled(false);
    jalview.analysis.Finder finder = new jalview.analysis.Finder(av.
        getAlignment(), av.getSelectionGroup(), seqIndex, resIndex);
    finder.setCaseSensitive(caseSensitive.getState());
    finder.setFindAll(findAll);

    String searchString = textfield.getText();

    finder.find(searchString);
    seqIndex = finder.getSeqIndex();
    resIndex = finder.getResIndex();
    searchResults = finder.getSearchResults();
    Vector idMatch = finder.getIdMatch();

    if (searchResults.getSize() == 0 && idMatch.size() > 0)
    {
      ap.idPanel.highlightSearchResults(idMatch);
    }

    if (searchResults.getSize() > 0)
    {
      createNewGroup.setEnabled(true);

    }
    else
    {
      searchResults = null;
      resIndex = 0;
      seqIndex = 0;
    }

    // if allResults is null, this effectively switches displaySearch flag in seqCanvas
    ap.highlightSearchResults(searchResults);

    if (findAll)
    {
      String message = (searchResults == null ? 0 : searchResults.getSize()) +
          " matches found.";
      ap.alignFrame.statusBar.setText("Search results: " + searchString + " : " +
                                      message);
    }

  }

  Label jLabel1 = new Label();
  protected TextField textfield = new TextField();
  protected Button findAll = new Button();
  protected Button findNext = new Button();
  Panel jPanel1 = new Panel();
  GridLayout gridLayout1 = new GridLayout();
  protected Button createNewGroup = new Button();
  Checkbox caseSensitive = new Checkbox();

  private void jbInit()
      throws Exception
  {
    jLabel1.setFont(new java.awt.Font("Verdana", 0, 12));
    jLabel1.setText("Find");
    jLabel1.setBounds(new Rectangle(3, 30, 34, 15));
    this.setLayout(null);
    textfield.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    textfield.setText("");
    textfield.setBounds(new Rectangle(40, 27, 133, 21));
    textfield.addKeyListener(new java.awt.event.KeyAdapter()
    {
      public void keyTyped(KeyEvent e)
      {
        textfield_keyTyped(e);
      }
    });
    textfield.addActionListener(this);
    findAll.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    findAll.setLabel("Find all");
    findAll.addActionListener(this);
    findNext.setEnabled(false);
    findNext.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    findNext.setLabel("Find Next");
    findNext.addActionListener(this);
    jPanel1.setBounds(new Rectangle(180, 5, 141, 64));
    jPanel1.setLayout(gridLayout1);
    gridLayout1.setHgap(0);
    gridLayout1.setRows(3);
    gridLayout1.setVgap(2);
    createNewGroup.setEnabled(false);
    createNewGroup.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    createNewGroup.setLabel("New Feature");
    createNewGroup.addActionListener(this);
    caseSensitive.setLabel("Match Case");
    caseSensitive.setBounds(new Rectangle(40, 49, 126, 23));
    jPanel1.add(findNext, null);
    jPanel1.add(findAll, null);
    jPanel1.add(createNewGroup, null);
    this.add(caseSensitive);
    this.add(textfield, null);
    this.add(jLabel1, null);
    this.add(jPanel1, null);
  }

  void textfield_keyTyped(KeyEvent e)
  {
    findNext.setEnabled(true);
  }

}
