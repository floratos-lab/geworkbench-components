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
import java.awt.image.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.*;

import jalview.datamodel.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class FeatureRenderer
{
  AlignmentPanel ap;
  AlignViewport av;
  Color resBoxColour;
  float transparency = 1.0f;
  FontMetrics fm;
  int charOffset;

  Hashtable featureColours = new Hashtable();

  // A higher level for grouping features of a
  // particular type
  Hashtable featureGroups = new Hashtable();

  // This is actually an Integer held in the hashtable,
  // Retrieved using the key feature type
  Object currentColour;

  String[] renderOrder;
  PropertyChangeSupport changeSupport=new PropertyChangeSupport(this);

  Vector allfeatures;

  /**
   * Creates a new FeatureRenderer object.
   *
   * @param av
   *          DOCUMENT ME!
   */
  public FeatureRenderer(AlignmentPanel ap)
  {
    this.ap = ap;
    this.av = ap.av;
  }


  public void transferSettings(FeatureRenderer fr)
  {
    this.renderOrder = fr.renderOrder;
    this.featureGroups = fr.featureGroups;
    this.featureColours = fr.featureColours;
    this.transparency = fr.transparency;
    this.featureOrder = fr.featureOrder;
  }

  BufferedImage offscreenImage;
  boolean offscreenRender = false;
  public Color findFeatureColour(Color initialCol, SequenceI seq, int res)
  {
    return new Color(findFeatureColour(initialCol.getRGB(),
                                       seq, res));
  }

  /**
   * This is used by the Molecule Viewer and Overview to get the accurate
   * colourof the rendered sequence
   */
  public int findFeatureColour(int initialCol, SequenceI seq, int column)
  {
    if (!av.showSequenceFeatures)
    {
      return initialCol;
    }

    if (seq != lastSeq)
    {
      lastSeq = seq;
      sequenceFeatures = lastSeq.getDatasetSequence().getSequenceFeatures();
      if (sequenceFeatures!=null)
      {
        sfSize = sequenceFeatures.length;
      }
    }

    if (sequenceFeatures!=lastSeq.getDatasetSequence().getSequenceFeatures()) {
      sequenceFeatures = lastSeq.getDatasetSequence().getSequenceFeatures();
      if (sequenceFeatures != null)
      {
        sfSize = sequenceFeatures.length;
      }
    }

    if (sequenceFeatures == null || sfSize==0)
    {
      return initialCol;
    }


    if (jalview.util.Comparison.isGap(lastSeq.getCharAt(column)))
    {
      return Color.white.getRGB();
    }

    // Only bother making an offscreen image if transparency is applied
    if (transparency != 1.0f && offscreenImage == null)
    {
      offscreenImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    currentColour = null;

    offscreenRender = true;

    if (offscreenImage != null)
    {
      offscreenImage.setRGB(0, 0, initialCol);
      drawSequence(offscreenImage.getGraphics(),
                   lastSeq,
                   column, column, 0);

      return offscreenImage.getRGB(0, 0);
    }
    else
    {
      drawSequence(null,
                   lastSeq,
                   lastSeq.findPosition(column),
                   -1, -1);

      if (currentColour == null)
      {
        return initialCol;
      }
      else
      {
        return ( (Integer) currentColour).intValue();
      }
    }

  }

  /**
   * DOCUMENT ME!
   *
   * @param g
   *          DOCUMENT ME!
   * @param seq
   *          DOCUMENT ME!
   * @param sg
   *          DOCUMENT ME!
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   * @param x1
   *          DOCUMENT ME!
   * @param y1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  // String type;
  // SequenceFeature sf;
  SequenceI lastSeq;
  SequenceFeature[] sequenceFeatures;
  int sfSize, sfindex, spos, epos;

  synchronized public void drawSequence(Graphics g, SequenceI seq,
                           int start, int end, int y1)
  {

    if (seq.getDatasetSequence().getSequenceFeatures() == null
        || seq.getDatasetSequence().getSequenceFeatures().length == 0)
    {
      return;
    }

    if (g != null)
    {
      fm = g.getFontMetrics();
    }

    if (av.featuresDisplayed == null
        || renderOrder == null
        || newFeatureAdded)
    {
      findAllFeatures();
      if (av.featuresDisplayed.size() < 1)
      {
        return;
      }

      sequenceFeatures = seq.getDatasetSequence().getSequenceFeatures();
    }

    if (lastSeq == null || seq != lastSeq
        || seq.getDatasetSequence().getSequenceFeatures()!=sequenceFeatures)
    {
      lastSeq = seq;
      sequenceFeatures = seq.getDatasetSequence().getSequenceFeatures();
    }

    if (transparency != 1 && g != null)
    {
      Graphics2D g2 = (Graphics2D) g;
      g2.setComposite(
          AlphaComposite.getInstance(
              AlphaComposite.SRC_OVER, transparency));
    }

    if (!offscreenRender)
    {
      spos = lastSeq.findPosition(start);
      epos = lastSeq.findPosition(end);
    }

    sfSize = sequenceFeatures.length;
    String type;
    for (int renderIndex = 0; renderIndex < renderOrder.length; renderIndex++)
    {
      type = renderOrder[renderIndex];

      if (type == null || !av.featuresDisplayed.containsKey(type))
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
            sequenceFeatures[sfindex].featureGroup.length()!=0
            && featureGroups.containsKey(sequenceFeatures[sfindex].featureGroup)
            &&
            ! ( (Boolean) featureGroups.get(sequenceFeatures[sfindex].
                                            featureGroup)).
            booleanValue())
        {
          continue;
        }

        if (!offscreenRender && (sequenceFeatures[sfindex].getBegin() > epos
                                 || sequenceFeatures[sfindex].getEnd() < spos))
        {
          continue;
        }

        if (offscreenRender && offscreenImage == null)
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

    if (transparency != 1.0f && g != null)
    {
      Graphics2D g2 = (Graphics2D) g;
      g2.setComposite(
          AlphaComposite.getInstance(
              AlphaComposite.SRC_OVER, 1.0f));
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
      int pady = (y1 + av.charHeight) - av.charHeight / 5;
      for (i = fstart; i <= fend; i++)
      {
        s = seq.getCharAt(i);

        if (jalview.util.Comparison.isGap(s))
        {
          continue;
        }

        g.setColor(featureColour);

        g.fillRect( (i - start) * av.charWidth, y1, av.charWidth, av.charHeight);

        if (offscreenRender || !av.validCharWidth)
        {
          continue;
        }

        g.setColor(Color.white);
        charOffset = (av.charWidth - fm.charWidth(s)) / 2;
        g.drawString(String.valueOf(s),
                     charOffset + (av.charWidth * (i - start)),
                     pady);

      }
    }
  }

  boolean newFeatureAdded = false;
  /**
   * Called when alignment in associated view has new/modified features
   * to discover and display.
   *
   */
  public void featuresAdded()
  {
    lastSeq=null;
    findAllFeatures();
  }

  boolean findingFeatures = false;
  /**
   * search the alignment for all new features, give them a colour and display
   * them. Then fires a PropertyChangeEvent on the changeSupport object.
   *
   */
  void findAllFeatures()
  {
    synchronized (firing)
    {
        if (firing.equals(Boolean.FALSE)) {
          firing=Boolean.TRUE;
          findAllFeatures(true); // add all new features as visible
          changeSupport.firePropertyChange("changeSupport",null,null);
          firing=Boolean.FALSE;
      }
    }
  }
  /**
   * Searches alignment for all features and updates colours
   *
   * @param newMadeVisible
   *          if true newly added feature types will be rendered immediatly
   */
  synchronized void findAllFeatures(boolean newMadeVisible) {
    newFeatureAdded = false;

    if (findingFeatures)
    {
      newFeatureAdded = true;
      return;
    }

    findingFeatures = true;

    if (av.featuresDisplayed == null)
    {
      av.featuresDisplayed = new Hashtable();
    }

    allfeatures = new Vector();
    Vector oldfeatures = new Vector();
    if (renderOrder!=null)
    {
      for (int i=0; i<renderOrder.length; i++) {
        if (renderOrder[i]!=null)
        {
          oldfeatures.addElement(renderOrder[i]);
        }
      }
    }
    for (int i = 0; i < av.alignment.getHeight(); i++)
    {
      SequenceFeature[] features
          = av.alignment.getSequenceAt(i).getDatasetSequence().
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

          if(featureGroups.containsKey(features[index].getType()))
          {
            boolean visible = ( (Boolean) featureGroups.get(
                features[index].featureGroup)).booleanValue();

            if(!visible)
            {
              index++;
              continue;
            }
          }


          if (! (features[index].begin == 0 && features[index].end == 0))
          {
            // If beginning and end are 0, the feature is for the whole sequence
            // and we don't want to render the feature in the normal way

            if (newMadeVisible && !oldfeatures.contains(features[index].getType())) {
              // this is a new feature type on the alignment. Mark it for
              // display.
              av.featuresDisplayed.put(features[index].getType(),
                                     new Integer(getColour(features[index].
                getType()).getRGB()));
              setOrder(features[index].getType(),0);
            }
           }
        }
        if (!allfeatures.contains(features[index].getType()))
        {
          allfeatures.addElement(features[index].getType());
        }
        index++;
      }
    }
    updateRenderOrder(allfeatures);
    findingFeatures = false;
  }
  protected Boolean firing=Boolean.FALSE;
  /**
   * replaces the current renderOrder with the unordered features in allfeatures.
   * The ordering of any types in both renderOrder and allfeatures is preserved,
   * and all new feature types are rendered on top of the existing types, in
   * the order given by getOrder or the order given in allFeatures.
   * Note. this operates directly on the featureOrder hash for efficiency. TODO:
   * eliminate the float storage for computing/recalling the persistent ordering
   *
   * @param allFeatures
   */
  private void updateRenderOrder(Vector allFeatures) {
    Vector allfeatures = new Vector(allFeatures);
    String[] oldRender = renderOrder;
    renderOrder = new String[allfeatures.size()];
    boolean initOrders=(featureOrder==null);
    int opos=0;
    if (oldRender!=null && oldRender.length>0)
    {
      for (int j=0; j<oldRender.length; j++)
      {
        if (oldRender[j]!=null)
          {
            if (initOrders)
            {
              setOrder(oldRender[j], (1-(1+(float)j)/(float) oldRender.length));
            }
            if (allfeatures.contains(oldRender[j])) {
              renderOrder[opos++]  = oldRender[j]; // existing features always
                                                    // appear below new features
              allfeatures.removeElement(oldRender[j]);
            }
          }
        }
    }
    if (allfeatures.size()==0) {
      // no new features - leave order unchanged.
      return;
    }
    int i=allfeatures.size()-1;
    int iSize=i;
    boolean sort=false;
    String[] newf = new String[allfeatures.size()];
    float[] sortOrder = new float[allfeatures.size()];
    Enumeration en = allfeatures.elements();
    // sort remaining elements
    while (en.hasMoreElements())
    {
      newf[i] = en.nextElement().toString();
      if (initOrders || !featureOrder.containsKey(newf[i]))
      {
        int denom = initOrders ? allfeatures.size() : featureOrder.size();
          // new unordered feature - compute persistent ordering at head of
          // existing features.
        setOrder(newf[i], i/(float) denom);
      }
      // set order from newly found feature from persisted ordering.
      sortOrder[i] = 2-((Float) featureOrder.get(newf[i])).floatValue();
      if (i<iSize)
      {
        // only sort if we need to
        sort = sort || sortOrder[i]>sortOrder[i+1];
      }
      i--;
    }
    if (iSize>1 && sort)
      jalview.util.QuickSort.sort(sortOrder, newf);
    sortOrder=null;
    System.arraycopy(newf, 0, renderOrder, opos, newf.length);
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

  static String lastFeatureAdded;
  static String lastFeatureGroupAdded;
  static String lastDescriptionAdded;

  int featureIndex = 0;
  boolean amendFeatures(final SequenceI[] sequences,
                        final SequenceFeature[] features,
                        boolean newFeatures,
                        final AlignmentPanel ap)
  {

    featureIndex = 0;

    JPanel bigPanel = new JPanel(new BorderLayout());
    final JComboBox overlaps;
    final JTextField name = new JTextField(25);
    final JTextField source = new JTextField(25);
    final JTextArea description = new JTextArea(3, 25);
    final JSpinner start = new JSpinner();
    final JSpinner end = new JSpinner();
    start.setPreferredSize(new Dimension(80, 20));
    end.setPreferredSize(new Dimension(80, 20));

    final JPanel colour = new JPanel();
    colour.setBorder(BorderFactory.createEtchedBorder());
    colour.setMaximumSize(new Dimension(40, 10));
    colour.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent evt)
      {
        Color col = JColorChooser.showDialog(Desktop.desktop,
                                             "Select Feature Colour",
                                             colour.getBackground());
        if (col != null)
          colour.setBackground(col);

      }
    });

    JPanel tmp = new JPanel();
    JPanel panel = new JPanel(new GridLayout(3, 1));

    ///////////////////////////////////////
    ///MULTIPLE FEATURES AT SELECTED RESIDUE
    if(!newFeatures && features.length>1)
    {
     panel = new JPanel(new GridLayout(4, 1));
     tmp = new JPanel();
     tmp.add(new JLabel("Select Feature: "));
     overlaps = new JComboBox();
     for(int i=0; i<features.length; i++)
     {
       overlaps.addItem(features[i].getType()
        +"/"+features[i].getBegin()+"-"+features[i].getEnd()
        +" ("+features[i].getFeatureGroup()+")");
     }

     tmp.add(overlaps);

     overlaps.addItemListener(new ItemListener()
     {
       public void itemStateChanged(ItemEvent e)
       {
         int index = overlaps.getSelectedIndex();
         if (index != -1)
         {
           featureIndex = index;
           name.setText(features[index].getType());
           description.setText(features[index].getDescription());
           source.setText(features[index].getFeatureGroup());
           start.setValue(new Integer(features[index].getBegin()));
           end.setValue(new Integer(features[index].getEnd()));

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

         colour.setBackground(col);
       }
     });


     panel.add(tmp);
    }
    //////////
    //////////////////////////////////////

    tmp = new JPanel();
    panel.add(tmp);
    tmp.add(new JLabel("Name: ", JLabel.RIGHT));
    tmp.add(name);

    tmp = new JPanel();
    panel.add(tmp);
    tmp.add(new JLabel("Group: ", JLabel.RIGHT));
    tmp.add(source);

    tmp = new JPanel();
    panel.add(tmp);
    tmp.add(new JLabel("Colour: ", JLabel.RIGHT));
    tmp.add(colour);
    colour.setPreferredSize(new Dimension(150, 15));

    bigPanel.add(panel, BorderLayout.NORTH);

    panel = new JPanel();
    panel.add(new JLabel("Description: ", JLabel.RIGHT));
    description.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    description.setLineWrap(true);
    panel.add(new JScrollPane(description));

    if (!newFeatures)
    {
      bigPanel.add(panel, BorderLayout.SOUTH);

      panel = new JPanel();
      panel.add(new JLabel(" Start:", JLabel.RIGHT));
      panel.add(start);
      panel.add(new JLabel("  End:", JLabel.RIGHT));
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
        lastFeatureGroupAdded = "Jalview";
      }
    }

    if(newFeatures)
    {
      name.setText(lastFeatureAdded);
      source.setText(lastFeatureGroupAdded);
    }
    else
    {
      name.setText(features[0].getType());
      source.setText(features[0].getFeatureGroup());
    }

    start.setValue(new Integer(features[0].getBegin()));
    end.setValue(new Integer(features[0].getEnd()));
    description.setText(features[0].getDescription());
    colour.setBackground(getColour(name.getText()));


    Object[] options;
    if (!newFeatures)
    {
      options = new Object[]
          {
          "Amend", "Delete", "Cancel"};
    }
    else
    {
      options = new Object[]
          {
          "OK", "Cancel"};
    }

    String title = newFeatures ? "Create New Sequence Feature(s)" :
        "Amend/Delete Features for "
        + sequences[0].getName();

    int reply = JOptionPane.showInternalOptionDialog(Desktop.desktop,
        bigPanel,
        title,
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options, "OK");

    jalview.io.FeaturesFile ffile = new jalview.io.FeaturesFile();

    if (reply == JOptionPane.OK_OPTION && name.getText().length()>0)
    {
      // This ensures that the last sequence
      // is refreshed and new features are rendered
      lastSeq = null;
      lastFeatureAdded = name.getText().trim();
      lastFeatureGroupAdded = source.getText().trim();
      lastDescriptionAdded = description.getText().replaceAll("\n", " ");

      if(lastFeatureGroupAdded.length()<1)
        lastFeatureGroupAdded = null;
    }

    if (!newFeatures)
    {
      SequenceFeature sf = features[featureIndex];

      if (reply == JOptionPane.NO_OPTION)
      {
        sequences[0].getDatasetSequence().deleteFeature(sf);
      }
      else if (reply == JOptionPane.YES_OPTION)
      {
        sf.type = lastFeatureAdded;
        sf.featureGroup = lastFeatureGroupAdded;
        sf.description = lastDescriptionAdded;

        setColour(sf.type, colour.getBackground());
        av.featuresDisplayed.put(sf.type,
                                 new Integer(colour.getBackground().getRGB()));

        try
        {
          sf.begin = ( (Integer) start.getValue()).intValue();
          sf.end = ( (Integer) end.getValue()).intValue();
        }
        catch (NumberFormatException ex)
        {}

        ffile.parseDescriptionHTML(sf, false);
      }
    }
    else //NEW FEATURES ADDED
    {
      if (reply == JOptionPane.OK_OPTION
          && lastFeatureAdded.length()>0)
      {
        for (int i = 0; i < sequences.length; i++)
        {
          features[i].type = lastFeatureAdded;
          if (lastFeatureGroupAdded!=null)
            features[i].featureGroup = lastFeatureGroupAdded;
          features[i].description = lastDescriptionAdded;
          sequences[i].addSequenceFeature(features[i]);
          ffile.parseDescriptionHTML(features[i], false);
        }

        if (av.featuresDisplayed == null)
        {
          av.featuresDisplayed = new Hashtable();
        }

        if (lastFeatureGroupAdded != null)
        {
          if (featureGroups == null)
            featureGroups = new Hashtable();
          featureGroups.put(lastFeatureGroupAdded, new Boolean(true));
        }

        Color col = colour.getBackground();
        setColour(lastFeatureAdded, colour.getBackground());
        av.featuresDisplayed.put(lastFeatureAdded,
                                   new Integer(col.getRGB()));

        findAllFeatures(false);

        ap.paintAlignment(true);


        return true;
      }
      else
      {
        return false;
      }
    }

    ap.paintAlignment(true);

    return true;
  }

  public void setColour(String featureType, Color col)
  {
    featureColours.put(featureType, col);
  }

  public void setTransparency(float value)
  {
    transparency = value;
  }

  public float getTransparency()
  {
    return transparency;
  }
  /**
   * Replace current ordering with new ordering
   * @param data { String(Type), Colour(Type), Boolean(Displayed) }
   */
  public void setFeaturePriority(Object[][] data)
  {
    setFeaturePriority(data, true);
  }
  /**
   *
   * @param data { String(Type), Colour(Type), Boolean(Displayed) }
   * @param visibleNew when true current featureDisplay list will be cleared
   */
  public void setFeaturePriority(Object[][] data, boolean visibleNew)
  {
    if (visibleNew)
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
    if (data==null)
    {
      return;
    }

    // The feature table will display high priority
    // features at the top, but theses are the ones
    // we need to render last, so invert the data
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
  Hashtable featureOrder=null;
  /**
   * analogous to colour - store a normalized ordering for all feature types in
   * this rendering context.
   *
   * @param type
   *          Feature type string
   * @param position
   *          normalized priority - 0 means always appears on top, 1 means
   *          always last.
   */
  public float setOrder(String type, float position)
  {
    if (featureOrder==null)
    {
      featureOrder = new Hashtable();
    }
    featureOrder.put(type, new Float(position));
    return position;
  }
  /**
   * get the global priority (0 (top) to 1 (bottom))
   *
   * @param type
   * @return [0,1] or -1 for a type without a priority
   */
  public float getOrder(String type) {
    if (featureOrder!=null)
    {
      if (featureOrder.containsKey(type))
      {
        return ((Float)featureOrder.get(type)).floatValue();
      }
    }
    return -1;
  }

  /**
   * @param listener
   * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    changeSupport.addPropertyChangeListener(listener);
  }

  /**
   * @param listener
   * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    changeSupport.removePropertyChangeListener(listener);
  }
}
