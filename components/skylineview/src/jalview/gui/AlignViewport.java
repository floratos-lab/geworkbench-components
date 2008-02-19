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

import jalview.analysis.*;

import jalview.bin.*;

import jalview.datamodel.*;

import jalview.schemes.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class AlignViewport
{
    int startRes;
    int endRes;
    int startSeq;
    int endSeq;
    boolean showJVSuffix = true;
    boolean showText = true;
    boolean showColourText = false;
    boolean showBoxes = true;
    boolean wrapAlignment = false;
    boolean renderGaps = true;
    boolean showSequenceFeatures = false;
    boolean showAnnotation = true;
    boolean colourAppliesToAllGroups = true;
    ColourSchemeI globalColourScheme = null;
    boolean conservationColourSelected = false;
    boolean abovePIDThreshold = false;
    SequenceGroup selectionGroup;
    int charHeight;
    int charWidth;
    boolean validCharWidth;
    int wrappedWidth;
    Font font;
    boolean seqNameItalics;
    AlignmentI alignment;
    ColumnSelection colSel = new ColumnSelection();
    int threshold;
    int increment;
    NJTree currentTree = null;
    boolean scaleAboveWrapped = false;
    boolean scaleLeftWrapped = true;
    boolean scaleRightWrapped = true;
    boolean hasHiddenColumns = false;
    boolean hasHiddenRows = false;
    boolean showHiddenMarkers = true;

    boolean cursorMode = false;

    // The following vector holds the features which are
    // currently visible, in the correct order or rendering
    Hashtable featuresDisplayed = null;


    /** DOCUMENT ME!! */
    public Hashtable [] hconsensus;
    AlignmentAnnotation consensus;
    AlignmentAnnotation conservation;
    AlignmentAnnotation quality;
    boolean autoCalculateConsensus = true;

    /** DOCUMENT ME!! */
    public int ConsPercGaps = 25; // JBPNote : This should be a scalable property!

    // JBPNote Prolly only need this in the applet version.
  private java.beans.PropertyChangeSupport changeSupport = new java.beans.
      PropertyChangeSupport(this);

    boolean ignoreGapsInConsensusCalculation = false;

    boolean isDataset = false;

    boolean antiAlias = false;

    boolean padGaps = false;

    Rectangle explodedPosition;

    String viewName;

    String sequenceSetID;

    boolean gatherViewsHere = false;

    Stack historyList = new Stack();
    Stack redoList = new Stack();

    Hashtable sequenceColours;

    int thresholdTextColour = 0;
    Color textColour = Color.black;
    Color textColour2 = Color.white;

    boolean rightAlignIds = false;

    Hashtable hiddenRepSequences;


    /**
     * Creates a new AlignViewport object.
     *
     * @param al DOCUMENT ME!
     */
    public AlignViewport(AlignmentI al)
    {
        setAlignment(al);
        init();
    }
    /**
     * Create a new AlignViewport with hidden regions
     * @param al AlignmentI
     * @param hiddenColumns ColumnSelection
     */
  public AlignViewport(AlignmentI al, ColumnSelection hiddenColumns)
  {
      setAlignment(al);
    if (hiddenColumns != null)
    {
        this.colSel = hiddenColumns;
        if (hiddenColumns.getHiddenColumns() != null)
      {
          hasHiddenColumns = true;
      }
    }
      init();
    }

    void init()
    {
        this.startRes = 0;
        this.endRes = alignment.getWidth() - 1;
        this.startSeq = 0;
        this.endSeq = alignment.getHeight() - 1;

      antiAlias = Cache.getDefault("ANTI_ALIAS", false);

      showJVSuffix = Cache.getDefault("SHOW_JVSUFFIX", true);
      showAnnotation = Cache.getDefault("SHOW_ANNOTATIONS", true);

      rightAlignIds = Cache.getDefault("RIGHT_ALIGN_IDS", false);

      autoCalculateConsensus = Cache.getDefault("AUTO_CALC_CONSENSUS", true);

      padGaps = Cache.getDefault("PAD_GAPS", true);

       String fontName = Cache.getDefault("FONT_NAME", "SansSerif");
       String fontStyle = Cache.getDefault("FONT_STYLE", Font.PLAIN + "") ;
       String fontSize = Cache.getDefault("FONT_SIZE", "10");

       seqNameItalics = Cache.getDefault("ID_ITALICS", true);

       int style = 0;

       if (fontStyle.equals("bold"))
       {
         style = 1;
       }
       else if (fontStyle.equals("italic"))
       {
         style = 2;
       }

       setFont(new Font(fontName, style, Integer.parseInt(fontSize)));

       alignment.setGapCharacter( Cache.getDefault("GAP_SYMBOL", "-").charAt(0) );


        // We must set conservation and consensus before setting colour,
        // as Blosum and Clustal require this to be done
        if(hconsensus==null && !isDataset)
        {
          if(!alignment.isNucleotide())
          {
            conservation = new AlignmentAnnotation("Conservation",
                "Conservation of total alignment less than " +
                ConsPercGaps + "% gaps",
                new Annotation[1], 0f,
                11f,
                AlignmentAnnotation.BAR_GRAPH);
            conservation.hasText = true;
            conservation.autoCalculated=true;


            if (Cache.getDefault("SHOW_CONSERVATION", true))
            {
              alignment.addAnnotation(conservation);
            }

            if (Cache.getDefault("SHOW_QUALITY", true))
            {
              quality = new AlignmentAnnotation("Quality",
                                                "Alignment Quality based on Blosum62 scores",
                                                new Annotation[1],
                                                0f,
                                                11f,
                                                AlignmentAnnotation.BAR_GRAPH);
              quality.hasText = true;
              quality.autoCalculated=true;

              alignment.addAnnotation(quality);
            }
          }

          consensus = new AlignmentAnnotation("Consensus", "PID",
                                               new Annotation[1], 0f, 100f,
                                               AlignmentAnnotation.BAR_GRAPH);
          consensus.hasText = true;
          consensus.autoCalculated=true;

           if (Cache.getDefault("SHOW_IDENTITY", true))
           {
             alignment.addAnnotation(consensus);
           }
        }

        if (jalview.bin.Cache.getProperty("DEFAULT_COLOUR") != null)
        {
          globalColourScheme = ColourSchemeProperty.getColour(alignment,
              jalview.bin.Cache.getProperty("DEFAULT_COLOUR"));

            if (globalColourScheme instanceof UserColourScheme)
            {
                globalColourScheme = UserDefinedColours.loadDefaultColours();
        ( (UserColourScheme) globalColourScheme).setThreshold(0,
            getIgnoreGapsConsensus());
            }

            if (globalColourScheme != null)
            {
                globalColourScheme.setConsensus(hconsensus);
            }
        }

        wrapAlignment = jalview.bin.Cache.getDefault("WRAP_ALIGNMENT", false);
    }



    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setShowSequenceFeatures(boolean b)
    {
        showSequenceFeatures = b;
    }

    public boolean getShowSequenceFeatures()
    {
      return showSequenceFeatures;
    }

  class ConservationThread
      extends Thread
    {
      AlignmentPanel ap;
      public ConservationThread(AlignmentPanel ap)
      {
        this.ap = ap;
      }

      public void run()
      {
        try
        {
          updatingConservation = true;

          while (UPDATING_CONSERVATION)
          {
            try
            {
              if (ap != null)
              {
                ap.paintAlignment(false);
              }
              Thread.sleep(200);
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
          }

          UPDATING_CONSERVATION = true;


          int alWidth = alignment.getWidth();
          if(alWidth<0)
        {
            return;
        }

          Conservation cons = new jalview.analysis.Conservation("All",
              jalview.schemes.ResidueProperties.propHash, 3,
              alignment.getSequences(), 0, alWidth -1);

          cons.calculate();
          cons.verdict(false, ConsPercGaps);

          if (quality!=null)
          {
            cons.findQuality();
          }

          char [] sequence = cons.getConsSequence().getSequence();
          float minR;
          float minG;
          float minB;
          float maxR;
          float maxG;
          float maxB;
          minR = 0.3f;
          minG = 0.0f;
          minB = 0f;
          maxR = 1.0f - minR;
          maxG = 0.9f - minG;
          maxB = 0f - minB; // scalable range for colouring both Conservation and Quality

          float min = 0f;
          float max = 11f;
          float qmin = 0f;
          float qmax = 0f;

          char c;

          conservation.annotations = new Annotation[alWidth];

          if (quality!=null)
          {
            quality.graphMax = cons.qualityRange[1].floatValue();
            quality.annotations = new Annotation[alWidth];
            qmin = cons.qualityRange[0].floatValue();
            qmax = cons.qualityRange[1].floatValue();
          }

          for (int i = 0; i < alWidth; i++)
          {
            float value = 0;

            c = sequence[i];

            if (Character.isDigit(c))
          {
              value = (int) (c - '0');
          }
            else if (c == '*')
          {
              value = 11;
          }
            else if (c == '+')
          {
              value = 10;
          }

            float vprop = value - min;
            vprop /= max;
            conservation.annotations[i] =
                new Annotation(String.valueOf(c),
                               String.valueOf(value), ' ', value,
                               new Color(minR + (maxR * vprop),
                                         minG + (maxG * vprop),
                                         minB + (maxB * vprop)));

            // Quality calc
            if (quality!=null)
            {
              value = ( (Double) cons.quality.get(i)).floatValue();
              vprop = value - qmin;
              vprop /= qmax;
            quality.annotations[i] = new Annotation(" ", String.valueOf(value),
                ' ',
                                               value,
                                               new Color(minR + (maxR * vprop),
                  minG + (maxG * vprop),
                  minB + (maxB * vprop)));
            }
          }
        }
        catch (OutOfMemoryError error)
        {
          javax.swing.SwingUtilities.invokeLater(new Runnable()
          {


            public void run()
            {
              javax.swing.JOptionPane.showInternalMessageDialog(Desktop.desktop,
                  "Out of memory calculating conservation!!"
                  +
                  "\nSee help files for increasing Java Virtual Machine memory."
                  , "Out of memory",
                  javax.swing.JOptionPane.WARNING_MESSAGE);
            }
          });

          conservation = null;
          quality = null;

          System.out.println("Conservation calculation: " + error);
          System.gc();

        }

        UPDATING_CONSERVATION = false;
        updatingConservation = false;

        if(ap!=null)
        {
        ap.paintAlignment(true);
        }

      }
    }


    ConservationThread conservationThread;

    ConsensusThread consensusThread;

    boolean consUpdateNeeded = false;

    static boolean UPDATING_CONSENSUS = false;

    static boolean UPDATING_CONSERVATION = false;

    boolean updatingConsensus = false;

    boolean updatingConservation = false;

    /**
     * DOCUMENT ME!
     */
    public void updateConservation(final AlignmentPanel ap)
    {
      if (alignment.isNucleotide() || conservation==null)
    {
        return;
    }

      conservationThread = new ConservationThread(ap);
      conservationThread.start();
    }

    /**
     * DOCUMENT ME!
     */
    public void updateConsensus(final AlignmentPanel ap)
    {
      consensusThread = new ConsensusThread(ap);
      consensusThread.start();
    }

  class ConsensusThread
      extends Thread
    {
      AlignmentPanel ap;
      public ConsensusThread(AlignmentPanel ap)
      {
        this.ap = ap;
      }
      public void run()
      {
        updatingConsensus = true;
        while (UPDATING_CONSENSUS)
        {
          try
          {
            if (ap != null)
            {
            ap.paintAlignment(false);
            }

            Thread.sleep(200);
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }


        UPDATING_CONSENSUS = true;

        try
        {
          int aWidth = alignment.getWidth();
          if(aWidth<0)
        {
            return;
        }

          consensus.annotations = null;
          consensus.annotations = new Annotation[aWidth];


          hconsensus = new Hashtable[aWidth];
          AAFrequency.calculate(alignment.getSequencesArray(),
                                0,
                                alignment.getWidth(),
                                hconsensus);

          for (int i = 0; i < aWidth; i++)
          {
            float value = 0;
            if (ignoreGapsInConsensusCalculation)
          {
              value = ( (Float) hconsensus[i].get(AAFrequency.PID_NOGAPS)).
                  floatValue();
          }
            else
          {
              value = ( (Float) hconsensus[i].get(AAFrequency.PID_GAPS)).
                  floatValue();
          }

            String maxRes = hconsensus[i].get(AAFrequency.MAXRESIDUE).toString();
            String mouseOver = hconsensus[i].get(AAFrequency.MAXRESIDUE) + " ";

            if (maxRes.length() > 1)
            {
              mouseOver = "[" + maxRes + "] ";
              maxRes = "+";
            }

            mouseOver += ( (int) value + "%");
          consensus.annotations[i] = new Annotation(maxRes, mouseOver, ' ',
              value);
          }


          if (globalColourScheme != null)
        {
            globalColourScheme.setConsensus(hconsensus);
        }

        }
        catch (OutOfMemoryError error)
        {
          alignment.deleteAnnotation(consensus);

          consensus = null;
          hconsensus = null;
          javax.swing.SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              javax.swing.JOptionPane.showInternalMessageDialog(Desktop.desktop,
                  "Out of memory calculating consensus!!"
                  +
                  "\nSee help files for increasing Java Virtual Machine memory."
                  , "Out of memory",
                  javax.swing.JOptionPane.WARNING_MESSAGE);
            }
          });

          System.out.println("Consensus calculation: " + error);
          System.gc();
        }
        UPDATING_CONSENSUS = false;
        updatingConsensus = false;

        if (ap != null)
        {
        ap.paintAlignment(true);
        }
      }
    }
    /**
     * get the consensus sequence as displayed under the PID consensus annotation row.
     * @return consensus sequence as a new sequence object
     */
  public SequenceI getConsensusSeq()
  {
      if (consensus==null)
    {
        updateConsensus(null);
    }
      if (consensus==null)
    {
        return null;
    }
      StringBuffer seqs=new StringBuffer();
    for (int i = 0; i < consensus.annotations.length; i++)
    {
      if (consensus.annotations[i] != null)
      {
          if (consensus.annotations[i].description.charAt(0) == '[')
        {
            seqs.append(consensus.annotations[i].description.charAt(1));
        }
          else
        {
            seqs.append(consensus.annotations[i].displayCharacter);
        }
      }
    }

      SequenceI sq = new Sequence("Consensus", seqs.toString());
    sq.setDescription("Percentage Identity Consensus " +
                      ( (ignoreGapsInConsensusCalculation) ? " without gaps" :
                       ""));
      return sq;
    }
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SequenceGroup getSelectionGroup()
    {
        return selectionGroup;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sg DOCUMENT ME!
     */
    public void setSelectionGroup(SequenceGroup sg)
    {
        selectionGroup = sg;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getConservationSelected()
    {
        return conservationColourSelected;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setConservationSelected(boolean b)
    {
        conservationColourSelected = b;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getAbovePIDThreshold()
    {
        return abovePIDThreshold;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setAbovePIDThreshold(boolean b)
    {
        abovePIDThreshold = b;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getStartRes()
    {
        return startRes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getEndRes()
    {
        return endRes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getStartSeq()
    {
        return startSeq;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cs DOCUMENT ME!
     */
    public void setGlobalColourScheme(ColourSchemeI cs)
    {
        globalColourScheme = cs;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ColourSchemeI getGlobalColourScheme()
    {
        return globalColourScheme;
    }

    /**
     * DOCUMENT ME!
     *
     * @param res DOCUMENT ME!
     */
    public void setStartRes(int res)
    {
        this.startRes = res;
    }

    /**
     * DOCUMENT ME!
     *
     * @param seq DOCUMENT ME!
     */
    public void setStartSeq(int seq)
    {
        this.startSeq = seq;
    }

    /**
     * DOCUMENT ME!
     *
     * @param res DOCUMENT ME!
     */
    public void setEndRes(int res)
    {
        if (res > (alignment.getWidth() - 1))
        {
            // log.System.out.println(" Corrected res from " + res + " to maximum " + (alignment.getWidth()-1));
            res = alignment.getWidth() - 1;
        }

        if (res < 0)
        {
            res = 0;
        }

        this.endRes = res;
    }

    /**
     * DOCUMENT ME!
     *
     * @param seq DOCUMENT ME!
     */
    public void setEndSeq(int seq)
    {
        if (seq > alignment.getHeight())
        {
            seq = alignment.getHeight();
        }

        if (seq < 0)
        {
            seq = 0;
        }

        this.endSeq = seq;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getEndSeq()
    {
        return endSeq;
    }

    /**
     * DOCUMENT ME!
     *
     * @param f DOCUMENT ME!
     */
    public void setFont(Font f)
    {
        font = f;

        Container c = new Container();

        java.awt.FontMetrics fm = c.getFontMetrics(font);
        setCharHeight(fm.getHeight());
        setCharWidth(fm.charWidth('M'));
        validCharWidth = true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Font getFont()
    {
        return font;
    }

    /**
     * DOCUMENT ME!
     *
     * @param w DOCUMENT ME!
     */
    public void setCharWidth(int w)
    {
        this.charWidth = w;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getCharWidth()
    {
        return charWidth;
    }

    /**
     * DOCUMENT ME!
     *
     * @param h DOCUMENT ME!
     */
    public void setCharHeight(int h)
    {
        this.charHeight = h;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getCharHeight()
    {
        return charHeight;
    }

    /**
     * DOCUMENT ME!
     *
     * @param w DOCUMENT ME!
     */
    public void setWrappedWidth(int w)
    {
        this.wrappedWidth = w;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getWrappedWidth()
    {
        return wrappedWidth;
    }


    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public AlignmentI getAlignment()
    {
        return alignment;
    }

    /**
     * DOCUMENT ME!
     *
     * @param align DOCUMENT ME!
     */
    public void setAlignment(AlignmentI align)
    {
        this.alignment = align;
    }

    /**
     * DOCUMENT ME!
     *
     * @param state DOCUMENT ME!
     */
    public void setWrapAlignment(boolean state)
    {
        wrapAlignment = state;
    }

    /**
     * DOCUMENT ME!
     *
     * @param state DOCUMENT ME!
     */
    public void setShowText(boolean state)
    {
        showText = state;
    }

    /**
     * DOCUMENT ME!
     *
     * @param state DOCUMENT ME!
     */
    public void setRenderGaps(boolean state)
    {
        renderGaps = state;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getColourText()
    {
        return showColourText;
    }

    /**
     * DOCUMENT ME!
     *
     * @param state DOCUMENT ME!
     */
    public void setColourText(boolean state)
    {
        showColourText = state;
    }

    /**
     * DOCUMENT ME!
     *
     * @param state DOCUMENT ME!
     */
    public void setShowBoxes(boolean state)
    {
        showBoxes = state;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getWrapAlignment()
    {
        return wrapAlignment;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getShowText()
    {
        return showText;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getShowBoxes()
    {
        return showBoxes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public char getGapCharacter()
    {
        return getAlignment().getGapCharacter();
    }

    /**
     * DOCUMENT ME!
     *
     * @param gap DOCUMENT ME!
     */
    public void setGapCharacter(char gap)
    {
        if (getAlignment() != null)
        {
            getAlignment().setGapCharacter(gap);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param thresh DOCUMENT ME!
     */
    public void setThreshold(int thresh)
    {
        threshold = thresh;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getThreshold()
    {
        return threshold;
    }

    /**
     * DOCUMENT ME!
     *
     * @param inc DOCUMENT ME!
     */
    public void setIncrement(int inc)
    {
        increment = inc;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getIncrement()
    {
        return increment;
    }


    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ColumnSelection getColumnSelection()
    {
        return colSel;
    }


    /**
     * DOCUMENT ME!
     *
     * @param tree DOCUMENT ME!
     */
    public void setCurrentTree(NJTree tree)
    {
        currentTree = tree;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public NJTree getCurrentTree()
    {
        return currentTree;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setColourAppliesToAllGroups(boolean b)
    {
        colourAppliesToAllGroups = b;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getColourAppliesToAllGroups()
    {
        return colourAppliesToAllGroups;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getShowJVSuffix()
    {
        return showJVSuffix;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setShowJVSuffix(boolean b)
    {
        showJVSuffix = b;
    }


    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getShowAnnotation()
    {
        return showAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setShowAnnotation(boolean b)
    {
        showAnnotation = b;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getScaleAboveWrapped()
    {
        return scaleAboveWrapped;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getScaleLeftWrapped()
    {
        return scaleLeftWrapped;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getScaleRightWrapped()
    {
        return scaleRightWrapped;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setScaleAboveWrapped(boolean b)
    {
        scaleAboveWrapped = b;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setScaleLeftWrapped(boolean b)
    {
        scaleLeftWrapped = b;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setScaleRightWrapped(boolean b)
    {
        scaleRightWrapped = b;
    }

    /**
     * Property change listener for changes in alignment
     *
     * @param listener DOCUMENT ME!
     */
    public void addPropertyChangeListener(
        java.beans.PropertyChangeListener listener)
    {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param listener DOCUMENT ME!
     */
    public void removePropertyChangeListener(
        java.beans.PropertyChangeListener listener)
    {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Property change listener for changes in alignment
     *
     * @param prop DOCUMENT ME!
     * @param oldvalue DOCUMENT ME!
     * @param newvalue DOCUMENT ME!
     */
    public void firePropertyChange(String prop, Object oldvalue, Object newvalue)
    {
        changeSupport.firePropertyChange(prop, oldvalue, newvalue);
    }

    public void setIgnoreGapsConsensus(boolean b, AlignmentPanel ap)
    {
      ignoreGapsInConsensusCalculation = b;
      updateConsensus(ap);
      if(globalColourScheme!=null)
      {
      globalColourScheme.setThreshold(globalColourScheme.getThreshold(),
                                      ignoreGapsInConsensusCalculation);
      }
    }

    public boolean getIgnoreGapsConsensus()
    {
     return ignoreGapsInConsensusCalculation;
    }

    public void setDataset(boolean b)
    {
      isDataset = b;
    }

    public boolean isDataset()
    {
      return isDataset;
    }


    public void hideSelectedColumns()
    {
      if (colSel.size() < 1)
    {
        return;
    }

      colSel.hideSelectedColumns();
      setSelectionGroup(null);

      hasHiddenColumns = true;
    }


    public void hideColumns(int start, int end)
    {
      if(start==end)
    {
        colSel.hideColumns(start);
    }
      else
    {
        colSel.hideColumns(start, end);
    }

      hasHiddenColumns = true;
    }

    public void hideRepSequences(SequenceI repSequence, SequenceGroup sg)
    {
      int sSize = sg.getSize();
      if(sSize < 2)
    {
        return;
    }

      if(hiddenRepSequences==null)
    {
        hiddenRepSequences = new Hashtable();
    }

       hiddenRepSequences.put(repSequence, sg);

      //Hide all sequences except the repSequence
      SequenceI [] seqs = new SequenceI[sSize-1];
      int index = 0;
      for(int i=0; i<sSize; i++)
    {
        if(sg.getSequenceAt(i)!=repSequence)
        {
          if(index==sSize-1)
        {
            return;
        }

          seqs[index++] = sg.getSequenceAt(i);
        }
    }

      hideSequence(seqs);

    }

    public void hideAllSelectedSeqs()
    {
    if (selectionGroup == null || selectionGroup.getSize()<1)
    {
        return;
    }

      SequenceI[] seqs = selectionGroup.getSequencesInOrder(alignment);

      hideSequence(seqs);

      setSelectionGroup(null);
    }

    public void hideSequence(SequenceI [] seq)
    {
      if(seq!=null)
      {
        for (int i = 0; i < seq.length; i++)
        {
          alignment.getHiddenSequences().hideSequence(seq[i]);
        }
        hasHiddenRows = true;
        firePropertyChange("alignment", null, alignment.getSequences());
      }
    }

    public void showSequence(int index)
    {
      Vector tmp = alignment.getHiddenSequences().showSequence(index
          , hiddenRepSequences);
      if(tmp.size()>0)
      {
        if(selectionGroup==null)
        {
          selectionGroup = new SequenceGroup();
          selectionGroup.setEndRes(alignment.getWidth()-1);
        }

        for (int t = 0; t < tmp.size(); t++)
        {
          selectionGroup.addSequence(
              (SequenceI) tmp.elementAt(t), false
              );
        }
        firePropertyChange("alignment", null, alignment.getSequences());
      }

      if(alignment.getHiddenSequences().getSize()<1)
    {
        hasHiddenRows = false;
    }
  }

    public void showColumn(int col)
    {
      colSel.revealHiddenColumns(col);
      if(colSel.getHiddenColumns()==null)
    {
        hasHiddenColumns = false;
    }
  }

    public void showAllHiddenColumns()
    {
      colSel.revealAllHiddenColumns();
      hasHiddenColumns = false;
    }

    public void showAllHiddenSeqs()
    {
      if(alignment.getHiddenSequences().getSize()>0)
      {
        if(selectionGroup==null)
        {
          selectionGroup = new SequenceGroup();
          selectionGroup.setEndRes(alignment.getWidth()-1);
        }
        Vector tmp = alignment.getHiddenSequences().showAll(hiddenRepSequences);
        for(int t=0; t<tmp.size(); t++)
        {
          selectionGroup.addSequence(
              (SequenceI)tmp.elementAt(t), false
              );
        }
        firePropertyChange("alignment", null, alignment.getSequences());
        hasHiddenRows = false;
        hiddenRepSequences = null;
      }
    }



    public void invertColumnSelection()
    {
      for(int i=0; i<alignment.getWidth(); i++)
      {
        if(colSel.contains(i))
      {
          colSel.removeElement(i);
      }
        else
        {
          if (!hasHiddenColumns || colSel.isVisible(i))
          {
            colSel.addElement(i);
          }
        }
      }
    }

    public int adjustForHiddenSeqs(int alignmentIndex)
    {
      return alignment.getHiddenSequences().adjustForHiddenSeqs(alignmentIndex);
    }

    /**
     * This method returns an array of new SequenceI objects
     * derived from the whole alignment or just the current
     * selection with start and end points adjusted
     * @note if you need references to the actual SequenceI objects in the alignment or currently selected then use getSequenceSelection()
     * @return String[]
     */
    public SequenceI[] getSelectionAsNewSequence()
    {
      SequenceI[] sequences;

      if (selectionGroup == null)
    {
        sequences = alignment.getSequencesArray();
        AlignmentAnnotation[] annots = alignment.getAlignmentAnnotation();
        for (int i=0; i<sequences.length; i++)
        {
          sequences[i] = new Sequence(sequences[i], annots); // construct new sequence with subset of visible annotation
        }
    }
      else
    {
        sequences = selectionGroup.getSelectionAsNewSequences(alignment);
    }

      return sequences;
    }
    /**
     * get the currently selected sequence objects or all the sequences in the alignment.
     * @return array of references to sequence objects
     */
    public SequenceI[] getSequenceSelection()
    {
      SequenceI[] sequences;
      if (selectionGroup==null)
      {
        sequences = alignment.getSequencesArray();
      }
      else
      {
        sequences = selectionGroup.getSequencesInOrder(alignment);
      }
      return sequences;
    }
    /**
     * This method returns the visible alignment as text, as
     * seen on the GUI, ie if columns are hidden they will not
     * be returned in the result.
     * Use this for calculating trees, PCA, redundancy etc on views
     * which contain hidden columns.
     * @return String[]
     */
  public jalview.datamodel.CigarArray getViewAsCigars(boolean
      selectedRegionOnly)
    {
      CigarArray selection=null;
      SequenceI [] seqs= null;
      int i, iSize;
      int start = 0, end = 0;
      if(selectedRegionOnly && selectionGroup!=null)
      {
        iSize = selectionGroup.getSize();
        seqs = selectionGroup.getSequencesInOrder(alignment);
        start = selectionGroup.getStartRes();
        end = selectionGroup.getEndRes(); // inclusive for start and end in SeqCigar constructor
      }
      else
      {
        iSize = alignment.getHeight();
        seqs = alignment.getSequencesArray();
        end = alignment.getWidth()-1;
      }
      SeqCigar[] selseqs = new SeqCigar[iSize];
      for(i=0; i<iSize; i++)
      {
        selseqs[i] = new SeqCigar(seqs[i], start, end);
      }
      selection=new CigarArray(selseqs);
      // now construct the CigarArray operations
    if (hasHiddenColumns)
    {
        Vector regions = colSel.getHiddenColumns();
        int [] region;
        int hideStart, hideEnd;
        int last=start;
        for (int j = 0; last<end & j < regions.size(); j++)
        {
          region = (int[]) regions.elementAt(j);
          hideStart = region[0];
          hideEnd = region[1];
          // edit hidden regions to selection range
        if (hideStart < last)
        {
            if (hideEnd > last)
            {
              hideStart = last;
          }
          else
          {
              continue;
          }
        }

          if (hideStart>end)
        {
            break;
        }

          if (hideEnd>end)
        {
            hideEnd=end;
        }

          if (hideStart>hideEnd)
        {
            break;
        }
          /**
           * form operations...
           */
          if (last<hideStart)
        {
            selection.addOperation(CigarArray.M, hideStart-last);
        }
          selection.addOperation(CigarArray.D, 1+hideEnd-hideStart);
          last = hideEnd+1;
        }
        // Final match if necessary.
        if (last<end)
      {
          selection.addOperation(CigarArray.M, end-last+1);
      }
    }
    else
    {
        selection.addOperation(CigarArray.M, end-start+1);
      }
      return selection;
    }
    /**
     * return a compact representation of the current alignment selection to
     * pass to an analysis function
     * @param selectedOnly boolean true to just return the selected view
     * @return AlignmentView
     */
  jalview.datamodel.AlignmentView getAlignmentView(boolean selectedOnly)
  {
      // JBPNote:
      // this is here because the AlignmentView constructor modifies the CigarArray
      // object. Refactoring of Cigar and alignment view representation should
      // be done to remove redundancy.
      CigarArray aligview = getViewAsCigars(selectedOnly);
    if (aligview != null)
    {
        return new AlignmentView(aligview,
                               (selectedOnly && selectionGroup != null) ?
                               selectionGroup.getStartRes() : 0);
      }
      return null;
    }
    /**
     * This method returns the visible alignment as text, as
     * seen on the GUI, ie if columns are hidden they will not
     * be returned in the result.
     * Use this for calculating trees, PCA, redundancy etc on views
     * which contain hidden columns.
     * @return String[]
     */
    public String [] getViewAsString(boolean selectedRegionOnly)
    {
      String [] selection = null;
      SequenceI [] seqs= null;
      int i, iSize;
      int start = 0, end = 0;
      if(selectedRegionOnly && selectionGroup!=null)
      {
        iSize = selectionGroup.getSize();
        seqs = selectionGroup.getSequencesInOrder(alignment);
        start = selectionGroup.getStartRes();
        end = selectionGroup.getEndRes()+1;
      }
      else
      {
        iSize = alignment.getHeight();
        seqs = alignment.getSequencesArray();
        end = alignment.getWidth();
      }

      selection = new String[iSize];
    if (hasHiddenColumns)
    {
        selection = colSel.getVisibleSequenceStrings(start, end, seqs);
    }
    else
    {
        for(i=0; i<iSize; i++)
        {
          selection[i] = seqs[i].getSequenceAsString(start, end);
        }

      }
      return selection;
    }

  public int [][] getVisibleRegionBoundaries(int min, int max)
  {
    Vector regions = new Vector();
    int start = min;
    int end = max;

    do
    {
      if (hasHiddenColumns)
      {
        if (start == 0)
        {
          start = colSel.adjustForHiddenColumns(start);
        }

        end = colSel.getHiddenBoundaryRight(start);
        if (start == end)
        {
          end = max;
        }
        if (end > max)
        {
          end = max;
        }
      }

      regions.addElement(new int[]
                         {start, end});

      if (hasHiddenColumns)
      {
        start = colSel.adjustForHiddenColumns(end);
        start = colSel.getHiddenBoundaryLeft(start) + 1;
      }
    }
    while (end < max);

    int[][] startEnd = new int[regions.size()][2];

    regions.copyInto(startEnd);

    return startEnd;

  }

    public boolean getShowHiddenMarkers()
    {
      return showHiddenMarkers;
    }

    public void setShowHiddenMarkers(boolean show)
    {
      showHiddenMarkers = show;
    }

    public String getSequenceSetId()
    {
      if(sequenceSetID==null)
    {
        sequenceSetID =  alignment.hashCode()+"";
    }

      return sequenceSetID;
    }

    public void alignmentChanged(AlignmentPanel ap)
    {
        if (padGaps)
    {
          alignment.padGaps();
    }

        if (hconsensus != null && autoCalculateConsensus)
        {
          updateConsensus(ap);
          updateConservation(ap);
        }

        //Reset endRes of groups if beyond alignment width
        int alWidth = alignment.getWidth();
        Vector groups = alignment.getGroups();
        if(groups!=null)
        {
          for(int i=0; i<groups.size(); i++)
          {
            SequenceGroup sg = (SequenceGroup)groups.elementAt(i);
            if(sg.getEndRes()>alWidth)
        {
              sg.setEndRes(alWidth-1);
          }
        }
    }

        if(selectionGroup!=null && selectionGroup.getEndRes()>alWidth)
    {
          selectionGroup.setEndRes(alWidth-1);
    }

        resetAllColourSchemes();

       // alignment.adjustSequenceAnnotations();
    }


    void resetAllColourSchemes()
    {
      ColourSchemeI cs = globalColourScheme;
      if(cs!=null)
      {
        if (cs instanceof ClustalxColourScheme)
        {
          ( (ClustalxColourScheme) cs).
              resetClustalX(alignment.getSequences(),
                            alignment.getWidth());
        }

        cs.setConsensus(hconsensus);
        if (cs.conservationApplied())
        {
          Alignment al = (Alignment) alignment;
          Conservation c = new Conservation("All",
                                            ResidueProperties.propHash, 3,
                                            al.getSequences(), 0,
                                            al.getWidth() - 1);
          c.calculate();
          c.verdict(false, ConsPercGaps);

          cs.setConservation(c);
        }
      }

      int s, sSize = alignment.getGroups().size();
      for(s=0; s<sSize; s++)
      {
        SequenceGroup sg = (SequenceGroup)alignment.getGroups().elementAt(s);
        if(sg.cs!=null && sg.cs instanceof ClustalxColourScheme)
        {
          ((ClustalxColourScheme)sg.cs).resetClustalX(
              sg.getSequences(hiddenRepSequences), sg.getWidth());
        }
        sg.recalcConservation();
      }
    }


    public Color getSequenceColour(SequenceI seq)
    {
      if(sequenceColours==null || !sequenceColours.containsKey(seq))
    {
        return Color.white;
    }
      else
    {
        return (Color)sequenceColours.get(seq);
    }
  }

    public void setSequenceColour(SequenceI seq, Color col)
    {
      if(sequenceColours==null)
    {
        sequenceColours = new Hashtable();
    }

      if(col == null)
    {
        sequenceColours.remove(seq);
    }
      else
    {
        sequenceColours.put(seq, col);
    }
    }
    /**
     * returns the visible column regions of the alignment
     * @param selectedRegionOnly true to just return the contigs intersecting with the selected area
     * @return
     */
    public int[] getViewAsVisibleContigs(boolean selectedRegionOnly) {
      int[] viscontigs=null;
      int start = 0, end = 0;
      if(selectedRegionOnly && selectionGroup!=null)
      {
        start = selectionGroup.getStartRes();
        end = selectionGroup.getEndRes()+1;
      }
      else
      {
        end = alignment.getWidth();
      }
      viscontigs = colSel.getVisibleContigs(start, end);
      return viscontigs;
    }


}
