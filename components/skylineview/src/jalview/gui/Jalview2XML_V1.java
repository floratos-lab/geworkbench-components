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
import java.net.*;
import java.util.*;
import java.util.jar.*;

import javax.swing.*;

import org.exolab.castor.xml.*;
import jalview.binding.*;
import jalview.schemes.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class Jalview2XML_V1
{
  jalview.schemes.UserColourScheme GetUserColourScheme(
      JalviewModelSequence jms, String id)
  {
    UserColours[] uc = jms.getUserColours();
    UserColours colours = null;

    for (int i = 0; i < uc.length; i++)
    {
      if (uc[i].getId().equals(id))
      {
        colours = uc[i];

        break;
      }
    }

    int csize = colours.getUserColourScheme().getColourCount();
    java.awt.Color[] newColours = new java.awt.Color[csize];

    for (int i = 0; i < csize; i++)
    {
      newColours[i] = new java.awt.Color(Integer.parseInt(
          colours.getUserColourScheme().getColour(i).getRGB(), 16));
    }

    return new jalview.schemes.UserColourScheme(newColours);
  }

  /**
   * DOCUMENT ME!
   *
   * @param file DOCUMENT ME!
   */
  public AlignFrame LoadJalviewAlign(final String file)
  {

    jalview.gui.AlignFrame af = null;

    try
    {
      //UNMARSHALLER SEEMS TO CLOSE JARINPUTSTREAM, MOST ANNOYING
      URL url = null;

      if (file.startsWith("http://"))
      {
        url = new URL(file);
      }

      JarInputStream jin = null;
      JarEntry jarentry = null;
      int entryCount = 1;

      do
      {
        if (url != null)
        {
          jin = new JarInputStream(url.openStream());
        }
        else
        {
          jin = new JarInputStream(new FileInputStream(file));
        }

        for (int i = 0; i < entryCount; i++)
        {
          jarentry = jin.getNextJarEntry();
        }

        class NoDescIDResolver
            implements IDResolver
        {
          public Object resolve(String idref)
          {
            System.out.println(idref + " used");
            return null;
          }
        }

        if (jarentry != null)
        {
          InputStreamReader in = new InputStreamReader(jin, "UTF-8");
          JalviewModel object = new JalviewModel();

          object = (JalviewModel) object.unmarshal(in);

          af = LoadFromObject(object, file);
          entryCount++;
        }
      }
      while (jarentry != null);
    }
    catch (final java.net.UnknownHostException ex)
    {
      ex.printStackTrace();
      javax.swing.SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {

          System.err.println("Couldn't locate Jalview XML file : " +
                             ex + "\n");
          JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                                "Couldn't locate " + file,
                                                "URL not found",
                                                JOptionPane.WARNING_MESSAGE);
        }
      });
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.err.println("Exception whilst loading jalview XML file : " +
                         ex + "\n");
      javax.swing.SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {

          JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                                "Error loading  " + file,
                                                "Error loading Jalview file",
                                                JOptionPane.WARNING_MESSAGE);
        }
      });
    }

    return af;
  }

  AlignFrame LoadFromObject(JalviewModel object, String file)
  {
    Vector seqids = new Vector();
    SequenceSet vamsasSet = object.getVamsasModel().getSequenceSet(0);
    Sequence[] vamsasSeq = vamsasSet.getSequence();

    JalviewModelSequence jms = object.getJalviewModelSequence();

    //////////////////////////////////
    //LOAD SEQUENCES
    jalview.datamodel.Sequence[] jseqs = new jalview.datamodel.Sequence[
        vamsasSeq.length];
    JSeq[] JSEQ = object.getJalviewModelSequence().getJSeq();
    for (int i = 0; i < vamsasSeq.length; i++)
    {
      jseqs[i] = new jalview.datamodel.Sequence(vamsasSeq[i].getName(),
                                                vamsasSeq[i].getSequence());
      jseqs[i].setStart(JSEQ[i].getStart());
      jseqs[i].setEnd(JSEQ[i].getEnd());
      seqids.add(jseqs[i]);
    }

    ///SequenceFeatures are added to the DatasetSequence,
    // so we must create the dataset before loading features
    /////////////////////////////////
    jalview.datamodel.Alignment al = new jalview.datamodel.Alignment(jseqs);
    al.setDataset(null);
    /////////////////////////////////

    for (int i = 0; i < vamsasSeq.length; i++)
    {
      if (JSEQ[i].getFeaturesCount() > 0)
      {
        Features[] features = JSEQ[i].getFeatures();
        for (int f = 0; f < features.length; f++)
        {
          jalview.datamodel.SequenceFeature sf
              = new jalview.datamodel.SequenceFeature(features[f].getType(),
              features[f].getDescription(), features[f].getStatus(),
              features[f].getBegin(), features[f].getEnd(),
              null);

          al.getSequenceAt(i).getDatasetSequence().addSequenceFeature(sf);
        }
      }
      if (JSEQ[i].getPdbidsCount() > 0)
      {
        Pdbids[] ids = JSEQ[i].getPdbids();
        for (int p = 0; p < ids.length; p++)
        {
          jalview.datamodel.PDBEntry entry = new jalview.datamodel.PDBEntry();
          entry.setId(ids[p].getId());
          entry.setType(ids[p].getType());
          al.getSequenceAt(i).getDatasetSequence().addPDBId(entry);
        }

      }
    }

    /////////////////////////////////
    //////////////////////////////////
    //LOAD ANNOTATIONS
    if (vamsasSet.getAnnotation() != null)
    {
      Annotation[] an = vamsasSet.getAnnotation();

      for (int i = 0; i < an.length; i++)
      {
        AnnotationElement[] ae = an[i].getAnnotationElement();
        jalview.datamodel.Annotation[] anot = new jalview.datamodel.Annotation[
            al.getWidth()];

        for (int aa = 0; aa < ae.length; aa++)
        {
          anot[ae[aa].getPosition()] = new jalview.datamodel.Annotation(ae[aa].
              getDisplayCharacter(),
              ae[aa].getDescription(),
              ae[aa].getSecondaryStructure().charAt(0),
              ae[aa].getValue());
        }

        jalview.datamodel.AlignmentAnnotation jaa = null;

        if (an[i].getGraph())
        {
          jaa = new jalview.datamodel.AlignmentAnnotation(an[i].getLabel(),
              an[i].getDescription(), anot, 0, 0,
              jalview.datamodel.AlignmentAnnotation.BAR_GRAPH);
        }
        else
        {
          jaa = new jalview.datamodel.AlignmentAnnotation(an[i].getLabel(),
              an[i].getDescription(), anot);
        }

        al.addAnnotation(jaa);
      }
    }

    /////////////////////////////////
    // LOAD VIEWPORT
    Viewport[] views = jms.getViewport();
    Viewport view = views[0]; // DEAL WITH MULTIPLE VIEWPORTS LATER

    AlignFrame af = new AlignFrame(al, view.getWidth(), view.getHeight());

    af.setFileName(file, "Jalview");

    for (int i = 0; i < JSEQ.length; i++)
    {
      af.viewport.setSequenceColour(
          af.viewport.alignment.getSequenceAt(i),
          new java.awt.Color(
              JSEQ[i].getColour()));
    }

    //  af.changeColour() );
    /////////////////////////
    //LOAD GROUPS
    if (jms.getJGroupCount() > 0)
    {
      JGroup[] groups = jms.getJGroup();

      for (int i = 0; i < groups.length; i++)
      {
        ColourSchemeI cs = null;

        if (groups[i].getColour() != null)
        {
          if (groups[i].getColour().startsWith("ucs"))
          {
            cs = GetUserColourScheme(jms, groups[i].getColour());
          }
          else
          {
            cs = ColourSchemeProperty.getColour(al,
                                                groups[i].getColour());
          }

          if (cs != null)
          {
            cs.setThreshold(groups[i].getPidThreshold(), true);
          }

        }

        Vector seqs = new Vector();
        int[] ids = groups[i].getSeq();

        for (int s = 0; s < ids.length; s++)
        {
          seqs.addElement( (jalview.datamodel.SequenceI) seqids.elementAt(
              ids[s]));
        }

        jalview.datamodel.SequenceGroup sg = new jalview.datamodel.
            SequenceGroup(seqs,
                          groups[i].getName(), cs, groups[i].getDisplayBoxes(),
                          groups[i].getDisplayText(), groups[i].getColourText(),
                          groups[i].getStart(), groups[i].getEnd());

        sg.setOutlineColour(new java.awt.Color(
            groups[i].getOutlineColour()));

        if (groups[i].getConsThreshold() != 0)
        {
          jalview.analysis.Conservation c = new jalview.analysis.Conservation(
              "All",
              ResidueProperties.propHash, 3, sg.getSequences(null), 0,
              sg.getWidth() - 1);
          c.calculate();
          c.verdict(false, 25);
          sg.cs.setConservation(c);
        }

        al.addGroup(sg);
      }
    }

    af.setBounds(view.getXpos(), view.getYpos(), view.getWidth(),
                 view.getHeight());
    af.viewport.setStartRes(view.getStartRes());
    af.viewport.setStartSeq(view.getStartSeq());
    af.viewport.setShowAnnotation(view.getShowAnnotation());
    af.viewport.setAbovePIDThreshold(view.getPidSelected());
    af.viewport.setColourText(view.getShowColourText());
    af.viewport.setConservationSelected(view.getConservationSelected());
    af.viewport.setShowJVSuffix(view.getShowFullId());
    af.viewport.setFont(new java.awt.Font(view.getFontName(),
                                          view.getFontStyle(), view.getFontSize()));
    af.alignPanel.fontChanged();

    af.viewport.setRenderGaps(view.getRenderGaps());
    af.viewport.setWrapAlignment(view.getWrapAlignment());
    af.alignPanel.setWrapAlignment(view.getWrapAlignment());
    af.viewport.setShowAnnotation(view.getShowAnnotation());
    af.alignPanel.setAnnotationVisible(view.getShowAnnotation());
    af.viewport.setShowBoxes(view.getShowBoxes());
    af.viewport.setShowText(view.getShowText());

    ColourSchemeI cs = null;

    if (view.getBgColour() != null)
    {
      if (view.getBgColour().startsWith("ucs"))
      {
        cs = GetUserColourScheme(jms, view.getBgColour());
      }
      else
      {
        cs = ColourSchemeProperty.getColour(al, view.getBgColour());
      }

      if (cs != null)
      {
        cs.setThreshold(view.getPidThreshold(), true);
        cs.setConsensus(af.viewport.hconsensus);
      }
    }

    af.viewport.setGlobalColourScheme(cs);
    af.viewport.setColourAppliesToAllGroups(false);
    af.changeColour(cs);
    if (view.getConservationSelected() && cs != null)
    {
      cs.setConservationInc(view.getConsThreshold());
    }

    af.viewport.setColourAppliesToAllGroups(true);
    af.viewport.showSequenceFeatures = view.getShowSequenceFeatures();

    if (jms.getFeatureSettings() != null)
    {
      af.viewport.featuresDisplayed = new Hashtable();
      String[] renderOrder = new String[jms.getFeatureSettings().
          getSettingCount()];
      for (int fs = 0; fs < jms.getFeatureSettings().getSettingCount(); fs++)
      {
        Setting setting = jms.getFeatureSettings().getSetting(fs);

        af.alignPanel.seqPanel.seqCanvas.getFeatureRenderer().setColour(setting.
            getType(),
            new java.awt.Color(setting.getColour()));

        renderOrder[fs] = setting.getType();

        if (setting.getDisplay())
        {
          af.viewport.featuresDisplayed.put(
              setting.getType(), new Integer(setting.getColour()));
        }
      }
      af.alignPanel.seqPanel.seqCanvas.getFeatureRenderer().renderOrder =
          renderOrder;
    }

    af.setMenusFromViewport(af.viewport);

    Desktop.addInternalFrame(af, view.getTitle(),
                             view.getWidth(), view.getHeight());

    //LOAD TREES
    ///////////////////////////////////////
    if (jms.getTreeCount() > 0)
    {
      try
      {
        for (int t = 0; t < jms.getTreeCount(); t++)
        {

          Tree tree = jms.getTree(t);

          TreePanel tp = af.ShowNewickTree(new jalview.io.NewickFile(
              tree.getNewick()), tree.getTitle(),
                                           tree.getWidth(), tree.getHeight(),
                                           tree.getXpos(), tree.getYpos());

          tp.fitToWindow.setState(tree.getFitToWindow());
          tp.fitToWindow_actionPerformed(null);

          if (tree.getFontName() != null)
          {
            tp.setTreeFont(new java.awt.Font(tree.getFontName(),
                                             tree.getFontStyle(),
                                             tree.getFontSize()));
          }
          else
          {
            tp.setTreeFont(new java.awt.Font(view.getFontName(),
                                             view.getFontStyle(),
                                             tree.getFontSize()));
          }

          tp.showPlaceholders(tree.getMarkUnlinked());
          tp.showBootstrap(tree.getShowBootstrap());
          tp.showDistances(tree.getShowDistances());

          tp.treeCanvas.threshold = tree.getThreshold();

          if (tree.getCurrentTree())
          {
            af.viewport.setCurrentTree(tp.getTree());
          }
        }

      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }

    }

    return af;
  }
}
