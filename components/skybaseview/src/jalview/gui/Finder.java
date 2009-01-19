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

import jalview.datamodel.*;
import jalview.jbgui.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class Finder
    extends GFinder
{
  AlignViewport av;
  AlignmentPanel ap;
  JInternalFrame frame;
  int seqIndex = 0;
  int resIndex = 0;

  SearchResults searchResults;

  /**
   * Creates a new Finder object.
   *
   * @param av DOCUMENT ME!
   * @param ap DOCUMENT ME!
   * @param f DOCUMENT ME!
   */
  public Finder()
  {
    frame = new JInternalFrame();
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    Desktop.addInternalFrame(frame, "Find", 340, 110);

    textfield.requestFocus();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void findNext_actionPerformed(ActionEvent e)
  {
    if (getFocusedViewport())
    {
      doSearch(false);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void findAll_actionPerformed(ActionEvent e)
  {
    if (getFocusedViewport())
    {
      resIndex = 0;
      seqIndex = 0;
      doSearch(true);
    }
  }

  boolean getFocusedViewport()
  {
    JInternalFrame frame = Desktop.desktop.getAllFrames()[1];

    if (frame != null && frame instanceof AlignFrame)
    {
      av = ( (AlignFrame) frame).viewport;
      ap = ( (AlignFrame) frame).alignPanel;
      return true;
    }
    return false;
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void createNewGroup_actionPerformed(ActionEvent e)
  {
    SequenceI[] seqs = new SequenceI[searchResults.getSize()];
    SequenceFeature[] features = new SequenceFeature[searchResults.getSize()];

    for (int i = 0; i < searchResults.getSize(); i++)
    {
      seqs[i] = searchResults.getResultSequence(i).getDatasetSequence();

      features[i] = new SequenceFeature(textfield.getText().trim(),
                                        "Search Results", null,
                                        searchResults.getResultStart(i),
                                        searchResults.getResultEnd(i),
                                        "Search Results");
    }

    if (ap.seqPanel.seqCanvas.getFeatureRenderer()
        .amendFeatures(seqs, features, true, ap))
    {
      ap.alignFrame.showSeqFeatures.setSelected(true);
      av.setShowSequenceFeatures(true);
      ap.highlightSearchResults(null);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param findAll DOCUMENT ME!
   */
  void doSearch(boolean findAll)
  {
    createNewGroup.setEnabled(false);

    String searchString = textfield.getText().trim();

    if (searchString.length() < 1)
    {
      return;
    }
    // TODO: extend finder to match descriptions, features and annotation, and other stuff
    // TODO: add switches to control what is searched - sequences, IDS, descriptions, features
    jalview.analysis.Finder finder = new jalview.analysis.Finder(av.alignment,
        av.getSelectionGroup(), seqIndex, resIndex);
    finder.setCaseSensitive(caseSensitive.isSelected());
    finder.setFindAll(findAll);

    finder.find(searchString); // returns true if anything was actually found

    seqIndex = finder.getSeqIndex();
    resIndex = finder.getResIndex();

    searchResults = finder.getSearchResults(); // find(regex, caseSensitive.isSelected(), )
    Vector idMatch = finder.getIdMatch();
    // set or reset the GUI
    if ( (searchResults.getSize() == 0) && (idMatch.size() > 0))
    {
      ap.idPanel.highlightSearchResults(idMatch);
    }

    int resultSize = searchResults.getSize();

    if (searchResults.getSize() > 0)
    {
      createNewGroup.setEnabled(true);
    }
    else
    {
      searchResults = null;
    }

    // if allResults is null, this effectively switches displaySearch flag in seqCanvas
    ap.highlightSearchResults(searchResults);
    // TODO: add enablers for 'SelectSequences' or 'SelectColumns' or 'SelectRegion' selection
    if (!findAll && resultSize == 0)
    {
      JOptionPane.showInternalMessageDialog(this, "Finished searching",
                                            null,
                                            JOptionPane.INFORMATION_MESSAGE);
      resIndex = 0;
      seqIndex = 0;
    }

    if (findAll)
    {
      String message = resultSize + " matches found.";
      JOptionPane.showInternalMessageDialog(this, message, null,
                                            JOptionPane.INFORMATION_MESSAGE);
    }

  }
}
