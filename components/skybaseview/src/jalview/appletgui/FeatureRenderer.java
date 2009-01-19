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

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class FeatureRenderer
{
  AlignViewport av;

  Hashtable featureColours = new Hashtable();

  // A higher level for grouping features of a
  // particular type
  Hashtable featureGroups = null;

  // Holds web links for feature groups and feature types
  // in the form label|link
  Hashtable featureLinks = null;

  // This is actually an Integer held in the hashtable,
  // Retrieved using the key feature type
  Object currentColour;

  String[] renderOrder;

  FontMetrics fm;
  int charOffset;

  float transparency = 1f;

  TransparencySetter transparencySetter = null;

  /**
   * Creates a new FeatureRenderer object.
   *
   * @param av DOCUMENT ME!
   */
  public FeatureRenderer(AlignViewport av)
  {
    this.av = av;

    if (!System.getProperty("java.version").startsWith("1.1"))
    {
      transparencySetter = new TransparencySetter();
    }
  }

  public void transferSettings(FeatureRenderer fr)
  {
    renderOrder = fr.renderOrder;
    featureGroups = fr.featureGroups;
    featureColours = fr.featureColours;
    transparency = fr.transparency;
  }


  static String lastFeatureAdded;
  static String lastFeatureGroupAdded;
  static String lastDescriptionAdded;

  int featureIndex = 0;
  boolean deleteFeature = false;
  Panel colourPanel;
  boolean amendFeatures(final SequenceI[] sequences,
                        final SequenceFeature[] features,
                        boolean newFeatures,
                        final AlignmentPanel ap)
  {
    Panel bigPanel = new Panel(new BorderLayout());
    final TextField name = new TextField(16);
    final TextField source = new TextField(16);
    final TextArea description = new TextArea(3, 35);
    final TextField start = new TextField(8);
    final TextField end = new TextField(8);
    final Choice overlaps;
    Button deleteButton = new Button("Delete");
    deleteFeature = false;

    colourPanel = new Panel(null);
    colourPanel.setSize(110,15);
    final FeatureRenderer fr = this;

    Panel panel = new Panel(new GridLayout(3, 1));

    Panel tmp;

    ///////////////////////////////////////
    ///MULTIPLE FEATURES AT SELECTED RESIDUE
    if(!newFeatures && features.length>1)
    {
     panel = new Panel(new GridLayout(4, 1));
     tmp = new Panel();
     tmp.add(new Label("Select Feature: "));
     overlaps = new Choice();
     for(int i=0; i<features.length; i++)
     {
       String item = features[i].getType()
        +"/"+features[i].getBegin()+"-"+features[i].getEnd();

       if(features[i].getFeatureGroup()!=null)
         item += " ("+features[i].getFeatureGroup()+")";

       overlaps.addItem(item);
     }

     tmp.add(overlaps);

     overlaps.addItemListener(new java.awt.event.ItemListener()
     {
       public void itemStateChanged(java.awt.event.ItemEvent e)
       {
         int index = overlaps.getSelectedIndex();
         if (index != -1)
         {
           featureIndex = index;
           name.setText(features[index].getType());
           description.setText(features[index].getDescription());
           source.setText(features[index].getFeatureGroup());
           start.setText(features[index].getBegin()+"");
           end.setText(features[index].getEnd()+"");

           SearchResults highlight = new SearchResults();
           highlight.addResult(sequences[0],
                               features[index].getBegin(),
                               features[index].getEnd());

           ap.seqPanel.seqCanvas.highlightSearchResults(highlight);

         }
         Color col = getColour(name.getText());
         if (col == null)
         {
           col = new
               jalview.schemes.UserColourScheme()
               .createColourFromName(name.getText());
         }

         colourPanel.setBackground(col);
       }
     });


     panel.add(tmp);
    }
    //////////
    //////////////////////////////////////


    tmp = new Panel();
    panel.add(tmp);
    tmp.add(new Label("Name: ", Label.RIGHT));
    tmp.add(name);

    tmp = new Panel();
    panel.add(tmp);
    tmp.add(new Label("Group: ",Label.RIGHT));
    tmp.add(source);

    tmp = new Panel();
    panel.add(tmp);
    tmp.add(new Label("Colour: ", Label.RIGHT));
    tmp.add(colourPanel);

    bigPanel.add(panel, BorderLayout.NORTH);

    panel = new Panel();
    panel.add(new Label("Description: ", Label.RIGHT));
    panel.add(new ScrollPane().add(description));

    if (!newFeatures)
    {
      bigPanel.add(panel, BorderLayout.SOUTH);

      panel = new Panel();
      panel.add(new Label(" Start:", Label.RIGHT));
      panel.add(start);
      panel.add(new Label("  End:", Label.RIGHT));
      panel.add(end);
      bigPanel.add(panel, BorderLayout.CENTER);
    }
    else
    {
      bigPanel.add(panel, BorderLayout.CENTER);
    }

    if (lastFeatureAdded == null)
    {
      if (features[0].type != null)
      {
        lastFeatureAdded = features[0].type;
      }
      else
      {
        lastFeatureAdded = "feature_1";
      }
    }

    if (lastFeatureGroupAdded == null)
    {
      if (features[0].featureGroup != null)
      {
        lastFeatureGroupAdded = features[0].featureGroup;
      }
      else
      {
        lastFeatureAdded = "Jalview";
      }
    }


    String title = newFeatures ? "Create New Sequence Feature(s)" :
        "Amend/Delete Features for "
        + sequences[0].getName();

    final JVDialog dialog = new JVDialog(ap.alignFrame,
                               title,
                               true,
                               385,240);

    dialog.setMainPanel(bigPanel);

    if(newFeatures)
    {
      name.setText(lastFeatureAdded);
      source.setText(lastFeatureGroupAdded);
    }
    else
    {
      dialog.ok.setLabel("Amend");
      dialog.buttonPanel.add(deleteButton, 1);
      deleteButton.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent evt)
            {
              deleteFeature = true;
              dialog.setVisible(false);
            }
          });
      name.setText(features[0].getType());
      source.setText(features[0].getFeatureGroup());
    }

    start.setText(features[0].getBegin()+"");
    end.setText(features[0].getEnd()+"");
    description.setText(features[0].getDescription());

    Color col = getColour(name.getText());
    if (col == null)
    {
      col = new
          jalview.schemes.UserColourScheme()
          .createColourFromName(name.getText());
    }

    colourPanel.setBackground(col);

    dialog.setResizable(true);


    colourPanel.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mousePressed(java.awt.event.MouseEvent evt)
      {
        new UserDefinedColours(fr, ap.alignFrame);
      }
    });

    dialog.setVisible(true);

    jalview.io.FeaturesFile ffile = new jalview.io.FeaturesFile();

    if (dialog.accept)
    {
      //This ensures that the last sequence
      //is refreshed and new features are rendered
      lastSeq = null;
      lastFeatureAdded = name.getText().trim();
      lastFeatureGroupAdded = source.getText().trim();
      lastDescriptionAdded = description.getText().replace('\n', ' ');
    }

    if(lastFeatureGroupAdded !=null && lastFeatureGroupAdded.length()<1)
      lastFeatureGroupAdded = null;


    if (!newFeatures)
    {
      SequenceFeature sf = features[featureIndex];

      if (dialog.accept)
      {
        sf.type = lastFeatureAdded;
        sf.featureGroup = lastFeatureGroupAdded;
        sf.description = lastDescriptionAdded;
        setColour(sf.type, colourPanel.getBackground());
        try
        {
          sf.begin = Integer.parseInt(start.getText());
          sf.end =  Integer.parseInt(end.getText());
        }
        catch (NumberFormatException ex)
        {}

        ffile.parseDescriptionHTML(sf, false);
      }
      if (deleteFeature)
      {
        sequences[0].deleteFeature(sf);
      }

    }
    else
    {
      if (dialog.accept && name.getText().length()>0)
      {
        for (int i = 0; i < sequences.length; i++)
        {
          features[i].type = lastFeatureAdded;
          features[i].featureGroup = lastFeatureGroupAdded;
          features[i].description = lastDescriptionAdded;
          sequences[i].addSequenceFeature(features[i]);
          ffile.parseDescriptionHTML(features[i], false);
        }

        if (av.featuresDisplayed == null)
        {
          av.featuresDisplayed = new Hashtable();
        }

        if (featureGroups == null)
        {
          featureGroups = new Hashtable();
        }

        col = colourPanel.getBackground();
        setColour(lastFeatureAdded, col);

        if(lastFeatureGroupAdded!=null)
        {
          featureGroups.put(lastFeatureGroupAdded, new Boolean(true));
          av.featuresDisplayed.put(lastFeatureGroupAdded,
                                   new Integer(col.getRGB()));
        }
        findAllFeatures();

        String [] tro = new String[renderOrder.length];
        tro[0] = renderOrder[renderOrder.length-1];
        System.arraycopy(renderOrder,0,tro,1,renderOrder.length-1);
        renderOrder = tro;

        ap.paintAlignment(true);

        return true;
      }
      else
      {
        return false;
      }
    }

    findAllFeatures();

    ap.paintAlignment(true);

    return true;
  }


  public Color findFeatureColour(Color initialCol, SequenceI seq, int i)
  {
    overview = true;
    if (!av.showSequenceFeatures)
    {
      return initialCol;
    }

    lastSeq = seq;
    sequenceFeatures = lastSeq.getSequenceFeatures();
    if (sequenceFeatures == null)
    {
      return initialCol;
    }

    sfSize = sequenceFeatures.length;

    if (jalview.util.Comparison.isGap(lastSeq.getCharAt(i)))
    {
      return Color.white;
    }

    currentColour = null;

    drawSequence(null, lastSeq, lastSeq.findPosition(i), -1, -1);

    if (currentColour == null)
    {
      return initialCol;
    }

    return new Color( ( (Integer) currentColour).intValue());
  }

  /**
   * This is used by the Molecule Viewer to get the accurate colour
   * of the rendered sequence
   */
  boolean overview = false;


  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   * @param seq DOCUMENT ME!
   * @param sg DOCUMENT ME!
   * @param start DOCUMENT ME!
   * @param end DOCUMENT ME!
   * @param x1 DOCUMENT ME!
   * @param y1 DOCUMENT ME!
   * @param width DOCUMENT ME!
   * @param height DOCUMENT ME!
   */
  // String type;
  // SequenceFeature sf;

  SequenceI lastSeq;
  SequenceFeature[] sequenceFeatures;
  int sfSize, sfindex, spos, epos;

  synchronized public void drawSequence(Graphics g, SequenceI seq,
                           int start, int end, int y1)
  {
    if (seq.getSequenceFeatures() == null
        || seq.getSequenceFeatures().length == 0)
    {
      return;
    }

    if (transparencySetter != null && g != null)
    {
      transparencySetter.setTransparency(g, transparency);
    }

    if (lastSeq == null || seq != lastSeq || sequenceFeatures!=seq.getSequenceFeatures())
    {
      lastSeq = seq;
      sequenceFeatures = seq.getSequenceFeatures();
      sfSize = sequenceFeatures.length;
    }

    if (av.featuresDisplayed == null || renderOrder == null)
    {
      findAllFeatures();
      if (av.featuresDisplayed.size() < 1)
      {
        return;
      }

      sequenceFeatures = seq.getSequenceFeatures();
      sfSize = sequenceFeatures.length;
    }
    if (!overview)
    {
      spos = lastSeq.findPosition(start);
      epos = lastSeq.findPosition(end);
      if (g != null)
      {
        fm = g.getFontMetrics();
      }
    }
    String type;
    for (int renderIndex = 0; renderIndex < renderOrder.length; renderIndex++)
    {
      type = renderOrder[renderIndex];
      if (!av.featuresDisplayed.containsKey(type))
      {
        continue;
      }

      // loop through all features in sequence to find
      // current feature to render
      for (sfindex = 0; sfindex < sfSize; sfindex++)
      {
        if (!sequenceFeatures[sfindex].type.equals(type))
        {
          continue;
        }

        if (featureGroups != null
            && sequenceFeatures[sfindex].featureGroup != null
            &&
            featureGroups.containsKey(sequenceFeatures[sfindex].featureGroup)
            &&
            ! ( (Boolean) featureGroups.get(sequenceFeatures[sfindex].
                                            featureGroup)).
            booleanValue())
        {
          continue;
        }

        if (!overview && (sequenceFeatures[sfindex].getBegin() > epos
                          || sequenceFeatures[sfindex].getEnd() < spos))
        {
          continue;
        }

        if (overview)
        {
          if (sequenceFeatures[sfindex].begin <= start &&
              sequenceFeatures[sfindex].end >= start)
          {
            currentColour = av.featuresDisplayed.get(sequenceFeatures[sfindex].
                type);
          }

        }
        else if (sequenceFeatures[sfindex].type.equals("disulfide bond"))
        {

          renderFeature(g, seq,
                        seq.findIndex(sequenceFeatures[sfindex].begin) - 1,
                        seq.findIndex(sequenceFeatures[sfindex].begin) - 1,
                        new Color( ( (Integer) av.featuresDisplayed.get(
                            sequenceFeatures[sfindex].type)).intValue()),
                        start, end, y1);
          renderFeature(g, seq,
                        seq.findIndex(sequenceFeatures[sfindex].end) - 1,
                        seq.findIndex(sequenceFeatures[sfindex].end) - 1,
                        new Color( ( (Integer) av.featuresDisplayed.get(
                            sequenceFeatures[sfindex].type)).intValue()),
                        start, end, y1);

        }
        else
        {
          renderFeature(g, seq,
                        seq.findIndex(sequenceFeatures[sfindex].begin) - 1,
                        seq.findIndex(sequenceFeatures[sfindex].end) - 1,
                        getColour(sequenceFeatures[sfindex].type),
                        start, end, y1);
        }

      }
    }

    if (transparencySetter != null && g != null)
    {
      transparencySetter.setTransparency(g, 1.0f);
    }
  }

  char s;
  int i;
  void renderFeature(Graphics g, SequenceI seq,
                     int fstart, int fend, Color featureColour, int start,
                     int end, int y1)
  {

    if ( ( (fstart <= end) && (fend >= start)))
    {
      if (fstart < start)
      { // fix for if the feature we have starts before the sequence start,
        fstart = start; // but the feature end is still valid!!
      }

      if (fend >= end)
      {
        fend = end;
      }

      for (i = fstart; i <= fend; i++)
      {
        s = seq.getCharAt(i);

        if (jalview.util.Comparison.isGap(s))
        {
          continue;
        }

        g.setColor(featureColour);

        g.fillRect( (i - start) * av.charWidth, y1, av.charWidth, av.charHeight);

        if (!av.validCharWidth)
        {
          continue;
        }

        g.setColor(Color.white);
        charOffset = (av.charWidth - fm.charWidth(s)) / 2;
        g.drawString(String.valueOf(s),
                     charOffset + (av.charWidth * (i - start)),
                     (y1 + av.charHeight) - av.charHeight / 5); //pady = height / 5;

      }
    }
  }

  void findAllFeatures()
  {
    jalview.schemes.UserColourScheme ucs = new
        jalview.schemes.UserColourScheme();

    av.featuresDisplayed = new Hashtable();
    Vector allfeatures = new Vector();
    for (int i = 0; i < av.alignment.getHeight(); i++)
    {
      SequenceFeature[] features = av.alignment.getSequenceAt(i).
          getSequenceFeatures();

      if (features == null)
      {
        continue;
      }

      int index = 0;
      while (index < features.length)
      {
        if (!av.featuresDisplayed.containsKey(features[index].getType()))
        {
          if (getColour(features[index].getType()) == null)
          {
            featureColours.put(features[index].getType(),
                               ucs.createColourFromName(features[index].
                getType()));
          }

          av.featuresDisplayed.put(features[index].getType(),
                                   new Integer(getColour(features[index].
              getType()).getRGB()));
          allfeatures.addElement(features[index].getType());
        }
        index++;
      }
    }

    renderOrder = new String[allfeatures.size()];
    Enumeration en = allfeatures.elements();
    int i = allfeatures.size() - 1;
    while (en.hasMoreElements())
    {
      renderOrder[i] = en.nextElement().toString();
      i--;
    }
  }

  public Color getColour(String featureType)
  {
    if (!featureColours.containsKey(featureType))
    {
      jalview.schemes.UserColourScheme ucs = new
          jalview.schemes.UserColourScheme();
      Color col = ucs.createColourFromName(featureType);
      featureColours.put(featureType, col);
      return col;
    }
    else
      return (Color) featureColours.get(featureType);
  }


  public void setColour(String featureType, Color col)
  {
    featureColours.put(featureType, col);
  }

  public void setFeaturePriority(Object[][] data)
  {
    // The feature table will display high priority
    // features at the top, but theses are the ones
    // we need to render last, so invert the data
    if (av.featuresDisplayed != null)
    {
      av.featuresDisplayed.clear();
    }

   /* if (visibleNew)
    {
      if (av.featuresDisplayed != null)
      {
        av.featuresDisplayed.clear();
      }
      else
      {
        av.featuresDisplayed = new Hashtable();
      }
    }
    if (data == null)
    {
      return;
    }*/


    renderOrder = new String[data.length];

    if (data.length > 0)
    {
      for (int i = 0; i < data.length; i++)
      {
        String type = data[i][0].toString();
        setColour(type, (Color) data[i][1]);
        if ( ( (Boolean) data[i][2]).booleanValue())
        {
          av.featuresDisplayed.put(type, new Integer(getColour(type).getRGB()));
        }

        renderOrder[data.length - i - 1] = type;
      }
    }
  }
}

class TransparencySetter
{
  void setTransparency(Graphics g, float value)
  {
     Graphics2D g2 = (Graphics2D) g;
     g2.setComposite(
        AlphaComposite.getInstance(
             AlphaComposite.SRC_OVER, value));
  }
}
