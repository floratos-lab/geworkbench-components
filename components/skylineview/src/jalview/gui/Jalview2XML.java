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
import jalview.schemabinding.version2.*;
import jalview.schemes.*;
import jalview.structure.StructureSelectionManager;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class Jalview2XML
{

  Hashtable seqRefIds;

  /**
   * This maintains a list of viewports, the key being the
   * seqSetId. Important to set historyItem and redoList
   * for multiple views
   */
  Hashtable viewportsAdded;

  Hashtable annotationIds = new Hashtable();

  String uniqueSetSuffix = "";
  /**
   * List of pdbfiles added to Jar
   */
  Vector pdbfiles = null;

  // SAVES SEVERAL ALIGNMENT WINDOWS TO SAME JARFILE
  public void SaveState(File statefile)
  {
    JInternalFrame[] frames = Desktop.desktop.getAllFrames();

    if (frames == null)
    {
      return;
    }

    try
    {
      FileOutputStream fos = new FileOutputStream(statefile);
      JarOutputStream jout = new JarOutputStream(fos);

      //NOTE UTF-8 MUST BE USED FOR WRITING UNICODE CHARS
      ////////////////////////////////////////////////////
      PrintWriter out = new PrintWriter(new OutputStreamWriter(jout,
          "UTF-8"));

      Vector shortNames = new Vector();

      //REVERSE ORDER
      for (int i = frames.length - 1; i > -1; i--)
      {
        if (frames[i] instanceof AlignFrame)
        {
          AlignFrame af = (AlignFrame) frames[i];

          String shortName = af.getTitle();

          if (shortName.indexOf(File.separatorChar) > -1)
          {
            shortName = shortName.substring(shortName.lastIndexOf(
                File.separatorChar) + 1);
          }

          int count = 1;

          while (shortNames.contains(shortName))
          {
            if (shortName.endsWith("_" + (count - 1)))
            {
              shortName = shortName.substring(0,
                                              shortName.lastIndexOf("_"));
            }

            shortName = shortName.concat("_" + count);
            count++;
          }

          shortNames.addElement(shortName);

          if (!shortName.endsWith(".xml"))
          {
            shortName = shortName + ".xml";
          }

          int ap, apSize = af.alignPanels.size();
          for (ap = 0; ap < apSize; ap++)
          {
            AlignmentPanel apanel = (AlignmentPanel) af.alignPanels.
                elementAt(ap);

            SaveState(apanel,
                      apSize == 1 ? shortName : ap + shortName,
                      jout, out);
          }
        }
      }

      out.close();
      jout.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  // USE THIS METHOD TO SAVE A SINGLE ALIGNMENT WINDOW
  public boolean SaveAlignment(AlignFrame af, String jarFile,
                               String fileName)
  {
    try
    {
      int ap, apSize = af.alignPanels.size();
      FileOutputStream fos = new FileOutputStream(jarFile);
      JarOutputStream jout = new JarOutputStream(fos);
      PrintWriter out = new PrintWriter(new OutputStreamWriter(jout,
          "UTF-8"));
      for (ap = 0; ap < apSize; ap++)
      {
        AlignmentPanel apanel = (AlignmentPanel) af.alignPanels.elementAt(ap);

        SaveState(apanel,
                  apSize == 1 ? fileName : fileName + ap,
                  jout, out);
      }

      out.close();
      jout.close();
      return true;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return false;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param af DOCUMENT ME!
   * @param timeStamp DOCUMENT ME!
   * @param fileName DOCUMENT ME!
   * @param jout DOCUMENT ME!
   * @param out DOCUMENT ME!
   */
  public JalviewModel SaveState(AlignmentPanel ap,
                                String fileName,
                                JarOutputStream jout,
                                PrintWriter out)
  {
    if (seqRefIds == null)
    {
      seqRefIds = new Hashtable();
    }

    Vector userColours = new Vector();

    AlignViewport av = ap.av;

    JalviewModel object = new JalviewModel();
    object.setVamsasModel(new jalview.schemabinding.version2.VamsasModel());

    object.setCreationDate(new java.util.Date(System.currentTimeMillis()));
    object.setVersion(jalview.bin.Cache.getProperty("VERSION"));

    jalview.datamodel.AlignmentI jal = av.alignment;

    if (av.hasHiddenRows)
    {
      jal = jal.getHiddenSequences().getFullAlignment();
    }

    SequenceSet vamsasSet = new SequenceSet();
    Sequence vamsasSeq;
    JalviewModelSequence jms = new JalviewModelSequence();

    vamsasSet.setGapChar(jal.getGapCharacter() + "");

    if(jal.getProperties()!=null)
    {
      Enumeration en = jal.getProperties().keys();
      while(en.hasMoreElements())
      {
        String key = en.nextElement().toString();
        SequenceSetProperties ssp = new SequenceSetProperties();
        ssp.setKey(key);
        ssp.setValue(jal.getProperties().get(key).toString());
        vamsasSet.addSequenceSetProperties(ssp);
      }
    }

    JSeq jseq;

    //SAVE SEQUENCES
    int id = 0;
    jalview.datamodel.SequenceI jds;
    for (int i = 0; i < jal.getHeight(); i++)
    {
      jds = jal.getSequenceAt(i);
      id = jds.hashCode();

      if (seqRefIds.get(id + "") != null)
      {

      }
      else
      {
        vamsasSeq = new Sequence();
        vamsasSeq.setId(id + "");
        vamsasSeq.setName(jds.getName());
        vamsasSeq.setSequence(jds.getSequenceAsString());
        vamsasSeq.setDescription(jds.getDescription());

        if (jds.getDatasetSequence().getDBRef() != null)
        {
          jalview.datamodel.DBRefEntry[] dbrefs =
              jds.getDatasetSequence().getDBRef();

          for (int d = 0; d < dbrefs.length; d++)
          {
            DBRef dbref = new DBRef();
            dbref.setSource(dbrefs[d].getSource());
            dbref.setVersion(dbrefs[d].getVersion());
            dbref.setAccessionId(dbrefs[d].getAccessionId());
            vamsasSeq.addDBRef(dbref);
          }
        }

        vamsasSet.addSequence(vamsasSeq);
        seqRefIds.put(id + "", jal.getSequenceAt(i));
      }

      jseq = new JSeq();
      jseq.setStart(jds.getStart());
      jseq.setEnd(jds.getEnd());
      jseq.setColour(av.getSequenceColour(jds).getRGB());

      jseq.setId(id);

      if (av.hasHiddenRows)
      {
        jseq.setHidden(av.alignment.getHiddenSequences().isHidden(jds));

        if (av.hiddenRepSequences != null
            && av.hiddenRepSequences.containsKey(jal.getSequenceAt(i)))
        {
          jalview.datamodel.SequenceI[] reps =
              ( (jalview.datamodel.SequenceGroup)
               av.hiddenRepSequences.get(
                   jal.getSequenceAt(i))).getSequencesInOrder(jal);

          for (int h = 0; h < reps.length; h++)
          {
            if (reps[h] != jal.getSequenceAt(i))
            {
              jseq.addHiddenSequences(
                  jal.findIndex(reps[h])
                  );
            }
          }
        }
      }

      if (jds.getDatasetSequence().getSequenceFeatures() != null)
      {
        jalview.datamodel.SequenceFeature[] sf
            = jds.getDatasetSequence().getSequenceFeatures();
        int index = 0;
        while (index < sf.length)
        {
          Features features = new Features();

          features.setBegin(sf[index].getBegin());
          features.setEnd(sf[index].getEnd());
          features.setDescription(sf[index].getDescription());
          features.setType(sf[index].getType());
          features.setFeatureGroup(sf[index].getFeatureGroup());
          features.setScore(sf[index].getScore());
          if (sf[index].links != null)
          {
            for (int l = 0; l < sf[index].links.size(); l++)
            {
              OtherData keyValue = new OtherData();
              keyValue.setKey("LINK_" + l);
              keyValue.setValue(sf[index].links.elementAt(l).toString());
              features.addOtherData(keyValue);
            }
          }
          if (sf[index].otherDetails != null)
          {
            String key;
            Enumeration keys = sf[index].otherDetails.keys();
            while (keys.hasMoreElements())
            {
              key = keys.nextElement().toString();
              OtherData keyValue = new OtherData();
              keyValue.setKey(key);
              keyValue.setValue(
                  sf[index].otherDetails.get(key).toString());
              features.addOtherData(keyValue);
            }
          }

          jseq.addFeatures(features);
          index++;
        }
      }

      if (jds.getDatasetSequence().getPDBId() != null)
      {
        Enumeration en = jds.getDatasetSequence().getPDBId().elements();
        while (en.hasMoreElements())
        {
          Pdbids pdb = new Pdbids();
          jalview.datamodel.PDBEntry entry
              = (jalview.datamodel.PDBEntry) en.nextElement();

          pdb.setId(entry.getId());
          pdb.setType(entry.getType());

          AppJmol jmol;
          //This must have been loaded, is it still visible?
          JInternalFrame[] frames = Desktop.desktop.getAllFrames();
          for (int f = frames.length - 1; f > -1; f--)
          {
            if (frames[f] instanceof AppJmol)
            {
              jmol = (AppJmol) frames[f];
              if (!jmol.pdbentry.getId().equals(entry.getId()))
                continue;


              StructureState state = new StructureState();
              state.setVisible(true);
              state.setXpos(jmol.getX());
              state.setYpos(jmol.getY());
              state.setWidth(jmol.getWidth());
              state.setHeight(jmol.getHeight());

              String statestring = null;//jmol.viewer.getStateInfo();
              if(state!=null)
              {
                state.setContent(statestring.replaceAll("\n", ""));
              }
              for (int s = 0; s < jmol.sequence.length; s++)
              {
                if (jal.findIndex(jmol.sequence[s]) > -1)
                {
                  pdb.addStructureState(state);
                }
              }
            }
          }


          if (entry.getFile() != null)
          {
            pdb.setFile(entry.getFile());
            if (pdbfiles == null)
            {
              pdbfiles = new Vector();
            }

            if (!pdbfiles.contains(entry.getId()))
            {
              pdbfiles.addElement(entry.getId());
              try
              {
                File file = new File(entry.getFile());
                if (file.exists() && jout != null)
                {
                  byte[] data = new byte[ (int) file.length()];
                  jout.putNextEntry(new JarEntry(entry.getId()));
                  DataInputStream dis = new DataInputStream(new
                      FileInputStream(file));
                  dis.readFully(data);

                  DataOutputStream dout = new DataOutputStream(jout);
                  dout.write(data, 0, data.length);
                  jout.closeEntry();
                }
              }
              catch (Exception ex)
              {
                  ex.printStackTrace();
              }

            }
          }

          if (entry.getProperty() != null)
          {
            PdbentryItem item = new PdbentryItem();
            Hashtable properties = entry.getProperty();
            Enumeration en2 = properties.keys();
            while (en2.hasMoreElements())
            {
              Property prop = new Property();
              String key = en2.nextElement().toString();
              prop.setName(key);
              prop.setValue(properties.get(key).toString());
              item.addProperty(prop);
            }
            pdb.addPdbentryItem(item);
          }

          jseq.addPdbids(pdb);
        }
      }

      jms.addJSeq(jseq);
    }

    if (av.hasHiddenRows)
    {
      jal = av.alignment;
    }

    //SAVE TREES
    ///////////////////////////////////
    if (av.currentTree != null)
    {
      // FIND ANY ASSOCIATED TREES
      // NOT IMPLEMENTED FOR HEADLESS STATE AT PRESENT
      if (Desktop.desktop != null)
      {
        JInternalFrame[] frames = Desktop.desktop.getAllFrames();

        for (int t = 0; t < frames.length; t++)
        {
          if (frames[t] instanceof TreePanel)
          {
            TreePanel tp = (TreePanel) frames[t];

            if (tp.treeCanvas.av.alignment == jal)
            {
              Tree tree = new Tree();
              tree.setTitle(tp.getTitle());
              tree.setCurrentTree( (av.currentTree == tp.getTree()));
              tree.setNewick(tp.getTree().toString());
              tree.setThreshold(tp.treeCanvas.threshold);

              tree.setFitToWindow(tp.fitToWindow.getState());
              tree.setFontName(tp.getTreeFont().getName());
              tree.setFontSize(tp.getTreeFont().getSize());
              tree.setFontStyle(tp.getTreeFont().getStyle());
              tree.setMarkUnlinked(tp.placeholdersMenu.getState());

              tree.setShowBootstrap(tp.bootstrapMenu.getState());
              tree.setShowDistances(tp.distanceMenu.getState());

              tree.setHeight(tp.getHeight());
              tree.setWidth(tp.getWidth());
              tree.setXpos(tp.getX());
              tree.setYpos(tp.getY());

              jms.addTree(tree);
            }
          }
        }
      }
    }

    //SAVE ANNOTATIONS
    if (jal.getAlignmentAnnotation() != null)
    {
      jalview.datamodel.AlignmentAnnotation[] aa = jal.getAlignmentAnnotation();

      for (int i = 0; i < aa.length; i++)
      {
        Annotation an = new Annotation();

        if (aa[i].annotationId != null)
        {
          annotationIds.put(aa[i].annotationId, aa[i]);
        }

        an.setId(aa[i].annotationId);

        if (aa[i] == av.quality ||
            aa[i] == av.conservation ||
            aa[i] == av.consensus)
        {
          an.setLabel(aa[i].label);
          an.setGraph(true);
          vamsasSet.addAnnotation(an);
          continue;
        }

        an.setVisible(aa[i].visible);

        an.setDescription(aa[i].description);

        if (aa[i].sequenceRef != null)
        {
          an.setSequenceRef(aa[i].sequenceRef.getName());
        }

        if (aa[i].graph > 0)
        {
          an.setGraph(true);
          an.setGraphType(aa[i].graph);
          an.setGraphGroup(aa[i].graphGroup);
          if (aa[i].getThreshold() != null)
          {
            ThresholdLine line = new ThresholdLine();
            line.setLabel(aa[i].getThreshold().label);
            line.setValue(aa[i].getThreshold().value);
            line.setColour(aa[i].getThreshold().colour.getRGB());
            an.setThresholdLine(line);
          }
        }
        else
        {
          an.setGraph(false);
        }

        an.setLabel(aa[i].label);
        if (aa[i].hasScore())
        {
          an.setScore(aa[i].getScore());
        }
        AnnotationElement ae;
        if (aa[i].annotations!=null)
        {
          an.setScoreOnly(false);
          for (int a = 0; a < aa[i].annotations.length; a++)
          {
            if ((aa[i] == null) || (aa[i].annotations[a] == null))
            {
              continue;
            }

            ae = new AnnotationElement();
            if (aa[i].annotations[a].description != null)
              ae.setDescription(aa[i].annotations[a].description);
            if(aa[i].annotations[a].displayCharacter!=null)
              ae.setDisplayCharacter(aa[i].annotations[a].displayCharacter);

            if (!Float.isNaN(aa[i].annotations[a].value))
              ae.setValue(aa[i].annotations[a].value);

            ae.setPosition(a);
            if (aa[i].annotations[a].secondaryStructure != ' '
                && aa[i].annotations[a].secondaryStructure != '\0')
              ae.setSecondaryStructure(aa[i].annotations[a].secondaryStructure
                                       + "");

            if (aa[i].annotations[a].colour!=null
                && aa[i].annotations[a].colour != java.awt.Color.black)
            {
              ae.setColour(aa[i].annotations[a].colour.getRGB());
            }

            an.addAnnotationElement(ae);
          }
        } else {
          an.setScoreOnly(true);
        }
        vamsasSet.addAnnotation(an);
      }
    }

    //SAVE GROUPS
    if (jal.getGroups() != null)
    {
      JGroup[] groups = new JGroup[jal.getGroups().size()];

      for (int i = 0; i < groups.length; i++)
      {
        groups[i] = new JGroup();

        jalview.datamodel.SequenceGroup sg = (jalview.datamodel.SequenceGroup)
            jal.getGroups()
            .elementAt(i);
        groups[i].setStart(sg.getStartRes());
        groups[i].setEnd(sg.getEndRes());
        groups[i].setName(sg.getName());
        if (sg.cs != null)
        {
          if (sg.cs.conservationApplied())
          {
            groups[i].setConsThreshold(sg.cs.getConservationInc());

            if (sg.cs instanceof jalview.schemes.UserColourScheme)
            {
              groups[i].setColour(SetUserColourScheme(sg.cs,
                  userColours,
                  jms));
            }
            else
            {
              groups[i].setColour(ColourSchemeProperty.getColourName(sg.
                  cs));
            }
          }
          else if (sg.cs instanceof jalview.schemes.AnnotationColourGradient)
          {
            groups[i].setColour(
                ColourSchemeProperty.getColourName(
                    ( (jalview.schemes.AnnotationColourGradient) sg.cs).
                    getBaseColour()));
          }
          else if (sg.cs instanceof jalview.schemes.UserColourScheme)
          {
            groups[i].setColour(SetUserColourScheme(sg.cs, userColours,
                jms));
          }
          else
          {
            groups[i].setColour(ColourSchemeProperty.getColourName(
                sg.cs));
          }

          groups[i].setPidThreshold(sg.cs.getThreshold());
        }

        groups[i].setOutlineColour(sg.getOutlineColour().getRGB());
        groups[i].setDisplayBoxes(sg.getDisplayBoxes());
        groups[i].setDisplayText(sg.getDisplayText());
        groups[i].setColourText(sg.getColourText());
        groups[i].setTextCol1(sg.textColour.getRGB());
        groups[i].setTextCol2(sg.textColour2.getRGB());
        groups[i].setTextColThreshold(sg.thresholdTextColour);

        for (int s = 0; s < sg.getSize(); s++)
        {
          jalview.datamodel.Sequence seq =
              (jalview.datamodel.Sequence) sg.getSequenceAt(s);
          groups[i].addSeq(seq.hashCode());
        }
      }

      jms.setJGroup(groups);
    }

    ///////////SAVE VIEWPORT
    Viewport view = new Viewport();
    view.setTitle(ap.alignFrame.getTitle());
    view.setSequenceSetId(av.getSequenceSetId());
    view.setViewName(av.viewName);
    view.setGatheredViews(av.gatherViewsHere);



    if (ap.av.explodedPosition != null)
    {
      view.setXpos(av.explodedPosition.x);
      view.setYpos(av.explodedPosition.y);
      view.setWidth(av.explodedPosition.width);
      view.setHeight(av.explodedPosition.height);
    }
    else
    {
      view.setXpos(ap.alignFrame.getBounds().x);
      view.setYpos(ap.alignFrame.getBounds().y);
      view.setWidth(ap.alignFrame.getBounds().width);
      view.setHeight(ap.alignFrame.getBounds().height);
    }

    view.setStartRes(av.startRes);
    view.setStartSeq(av.startSeq);

    if (av.getGlobalColourScheme() instanceof jalview.schemes.UserColourScheme)
    {
      view.setBgColour(SetUserColourScheme(av.getGlobalColourScheme(),
                                           userColours, jms));
    }
    else if (av.getGlobalColourScheme() instanceof jalview.schemes.
             AnnotationColourGradient)
    {
      jalview.schemes.AnnotationColourGradient acg
          = (jalview.schemes.AnnotationColourGradient) av.getGlobalColourScheme();

      AnnotationColours ac = new AnnotationColours();
      ac.setAboveThreshold(acg.getAboveThreshold());
      ac.setThreshold(acg.getAnnotationThreshold());
      ac.setAnnotation(acg.getAnnotation());
      if (acg.getBaseColour() instanceof jalview.schemes.UserColourScheme)
      {
        ac.setColourScheme(SetUserColourScheme(acg.getBaseColour(),
                                               userColours, jms));
      }
      else
      {
        ac.setColourScheme(ColourSchemeProperty.getColourName(acg.getBaseColour()));
      }

      ac.setMaxColour(acg.getMaxColour().getRGB());
      ac.setMinColour(acg.getMinColour().getRGB());
      view.setAnnotationColours(ac);
      view.setBgColour("AnnotationColourGradient");
    }
    else
    {
      view.setBgColour(ColourSchemeProperty.getColourName(
          av.getGlobalColourScheme()));
    }

    ColourSchemeI cs = av.getGlobalColourScheme();

    if (cs != null)
    {
      if (cs.conservationApplied())
      {
        view.setConsThreshold(cs.getConservationInc());
        if (cs instanceof jalview.schemes.UserColourScheme)
        {
          view.setBgColour(SetUserColourScheme(cs, userColours, jms));
        }
      }

      if (cs instanceof ResidueColourScheme)
      {
        view.setPidThreshold(cs.getThreshold());
      }
    }

    view.setConservationSelected(av.getConservationSelected());
    view.setPidSelected(av.getAbovePIDThreshold());
    view.setFontName(av.font.getName());
    view.setFontSize(av.font.getSize());
    view.setFontStyle(av.font.getStyle());
    view.setRenderGaps(av.renderGaps);
    view.setShowAnnotation(av.getShowAnnotation());
    view.setShowBoxes(av.getShowBoxes());
    view.setShowColourText(av.getColourText());
    view.setShowFullId(av.getShowJVSuffix());
    view.setRightAlignIds(av.rightAlignIds);
    view.setShowSequenceFeatures(av.showSequenceFeatures);
    view.setShowText(av.getShowText());
    view.setWrapAlignment(av.getWrapAlignment());
    view.setTextCol1(av.textColour.getRGB());
    view.setTextCol2(av.textColour2.getRGB());
    view.setTextColThreshold(av.thresholdTextColour);

    if (av.featuresDisplayed != null)
    {
      jalview.schemabinding.version2.FeatureSettings fs
          = new jalview.schemabinding.version2.FeatureSettings();

      String[] renderOrder =
          ap.seqPanel.seqCanvas.getFeatureRenderer().renderOrder;

      Vector settingsAdded = new Vector();
      for (int ro = 0; ro < renderOrder.length; ro++)
      {
        Setting setting = new Setting();
        setting.setType(renderOrder[ro]);
        setting.setColour(
            ap.seqPanel.seqCanvas.getFeatureRenderer().getColour(renderOrder[ro]).
            getRGB()
            );

        setting.setDisplay(
            av.featuresDisplayed.containsKey(renderOrder[ro])
            );
        float rorder=ap.seqPanel.seqCanvas.getFeatureRenderer().getOrder(renderOrder[ro]);
        if (rorder>-1) {
          setting.setOrder(rorder);
        }
        fs.addSetting(setting);
        settingsAdded.addElement(renderOrder[ro]);
      }

      //Make sure we save none displayed feature settings
      Enumeration en =
          ap.seqPanel.seqCanvas.getFeatureRenderer().featureColours.keys();
      while (en.hasMoreElements())
      {
        String key = en.nextElement().toString();
        if (settingsAdded.contains(key))
        {
          continue;
        }

        Setting setting = new Setting();
        setting.setType(key);
        setting.setColour(
            ap.seqPanel.seqCanvas.getFeatureRenderer().getColour(key).getRGB()
            );

        setting.setDisplay(false);
        float rorder = ap.seqPanel.seqCanvas.getFeatureRenderer().getOrder(key);
        if (rorder>-1)
        {
          setting.setOrder(rorder);
        }
        fs.addSetting(setting);
        settingsAdded.addElement(key);
      }
      en = ap.seqPanel.seqCanvas.getFeatureRenderer().featureGroups.keys();
      Vector groupsAdded=new Vector();
      while (en.hasMoreElements())
      {
        String grp = en.nextElement().toString();
        if (groupsAdded.contains(grp))
        {
          continue;
        }
        Group g = new Group();
        g.setName(grp);
        g.setDisplay(((Boolean)ap.seqPanel.seqCanvas.getFeatureRenderer().featureGroups.get(grp)).booleanValue());
        fs.addGroup(g);
        groupsAdded.addElement(grp);
      }
      jms.setFeatureSettings(fs);

    }

    if (av.hasHiddenColumns)
    {
      for (int c = 0; c < av.getColumnSelection().getHiddenColumns().size(); c++)
      {
        int[] region = (int[]) av.getColumnSelection().getHiddenColumns().
            elementAt(c);
        HiddenColumns hc = new HiddenColumns();
        hc.setStart(region[0]);
        hc.setEnd(region[1]);
        view.addHiddenColumns(hc);
      }
    }

    jms.addViewport(view);

    object.setJalviewModelSequence(jms);
    object.getVamsasModel().addSequenceSet(vamsasSet);

    if (out != null)
    {
      //We may not want to right the object to disk,
      //eg we can copy the alignViewport to a new view object
      //using save and then load
      try
      {
        if (!fileName.endsWith(".xml"))
        {
          fileName = fileName + ".xml";
        }

        JarEntry entry = new JarEntry(fileName);
        jout.putNextEntry(entry);

        object.marshal(out);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return object;
  }

  String SetUserColourScheme(jalview.schemes.ColourSchemeI cs,
                             Vector userColours, JalviewModelSequence jms)
  {
    String id = null;
    jalview.schemes.UserColourScheme ucs = (jalview.schemes.UserColourScheme)
        cs;

    if (!userColours.contains(ucs))
    {
      userColours.add(ucs);

      java.awt.Color[] colours = ucs.getColours();
      jalview.schemabinding.version2.UserColours uc = new jalview.schemabinding.
          version2.UserColours();
      jalview.schemabinding.version2.UserColourScheme jbucs = new jalview.
          schemabinding.version2.UserColourScheme();

      for (int i = 0; i < colours.length; i++)
      {
        jalview.schemabinding.version2.Colour col = new jalview.schemabinding.
            version2.Colour();
        col.setName(ResidueProperties.aa[i]);
        col.setRGB(jalview.util.Format.getHexString(colours[i]));
        jbucs.addColour(col);
      }
      if (ucs.getLowerCaseColours() != null)
      {
        colours = ucs.getLowerCaseColours();
        for (int i = 0; i < colours.length; i++)
        {
          jalview.schemabinding.version2.Colour col = new jalview.schemabinding.
              version2.Colour();
          col.setName(ResidueProperties.aa[i].toLowerCase());
          col.setRGB(jalview.util.Format.getHexString(colours[i]));
          jbucs.addColour(col);
        }
      }

      id = "ucs" + userColours.indexOf(ucs);
      uc.setId(id);
      uc.setUserColourScheme(jbucs);
      jms.addUserColours(uc);
    }

    return id;
  }

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

    java.awt.Color[] newColours = new java.awt.Color[24];

    for (int i = 0; i < 24; i++)
    {
      newColours[i] = new java.awt.Color(Integer.parseInt(
          colours.getUserColourScheme().getColour(i).getRGB(), 16));
    }

    jalview.schemes.UserColourScheme ucs =
        new jalview.schemes.UserColourScheme(newColours);

    if (colours.getUserColourScheme().getColourCount() > 24)
    {
      newColours = new java.awt.Color[23];
      for (int i = 0; i < 23; i++)
      {
        newColours[i] = new java.awt.Color(Integer.parseInt(
            colours.getUserColourScheme().getColour(i + 24).getRGB(), 16));
      }
      ucs.setLowerCaseColours(newColours);
    }

    return ucs;
  }

  /**
   * DOCUMENT ME!
   *
   * @param file DOCUMENT ME!
   */
  public AlignFrame LoadJalviewAlign(final String file)
  {
    uniqueSetSuffix = System.currentTimeMillis() % 100000 + "";

    jalview.gui.AlignFrame af = null;

    seqRefIds = new Hashtable();
    viewportsAdded = new Hashtable();

    Hashtable gatherToThisFrame = new Hashtable();

    String errorMessage = null;

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

        if (jarentry != null && jarentry.getName().endsWith(".xml"))
        {
          InputStreamReader in = new InputStreamReader(jin, "UTF-8");
          JalviewModel object = new JalviewModel();

          Unmarshaller unmar = new Unmarshaller(object.getClass());
          unmar.setValidation(false);
          object = (JalviewModel) unmar.unmarshal(in);

          af = LoadFromObject(object, file, true);
          if (af.viewport.gatherViewsHere)
          {
            gatherToThisFrame.put(af.viewport.getSequenceSetId(), af);
          }
          entryCount++;
        }
        else if (jarentry != null)
        {
          //Some other file here.
          entryCount++;
        }
      }
      while (jarentry != null);
    }
    catch(java.io.FileNotFoundException ex)
    {
      ex.printStackTrace();
      errorMessage = "Couldn't locate Jalview XML file : "+file;
      System.err.println("Exception whilst loading jalview XML file : " +
                         ex + "\n");
    }
    catch (java.net.UnknownHostException ex)
    {
      ex.printStackTrace();
      errorMessage = "Couldn't locate Jalview XML file : " +file;
      System.err.println("Exception whilst loading jalview XML file : " +
                         ex + "\n");
    }
    catch (Exception ex)
    {
      //Is Version 1 Jar file?
      af = new Jalview2XML_V1().LoadJalviewAlign(file);

      if (af != null)
      {
        System.out.println("Successfully loaded archive file");
        return af;
      }
      ex.printStackTrace();

      System.err.println("Exception whilst loading jalview XML file : " +
                         ex + "\n");
    }

    if (Desktop.instance != null)
    {
      Desktop.instance.stopLoading();
    }

    Enumeration en = gatherToThisFrame.elements();
    while(en.hasMoreElements())
    {
      Desktop.instance.gatherViews(
          (AlignFrame) en.nextElement());
    }

    if(errorMessage!=null)
    {
      final String finalErrorMessage = errorMessage;
      javax.swing.SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                                finalErrorMessage,
                                                "Error loading Jalview file",
                                                JOptionPane.WARNING_MESSAGE);
        }
      });
    }

    return af;
  }

  Hashtable alreadyLoadedPDB;
  String loadPDBFile(String file, String pdbId)
  {
    if (alreadyLoadedPDB == null)
      alreadyLoadedPDB = new Hashtable();

    if (alreadyLoadedPDB.containsKey(pdbId))
      return alreadyLoadedPDB.get(pdbId).toString();

    try
    {
      JarInputStream jin = null;

      if (file.startsWith("http://"))
      {
        jin = new JarInputStream(new URL(file).openStream());
      }
      else
      {
        jin = new JarInputStream(new FileInputStream(file));
      }

      JarEntry entry = null;
      do
      {
        entry = jin.getNextJarEntry();
      }
      while (!entry.getName().equals(pdbId));

      BufferedReader in = new BufferedReader(new InputStreamReader(jin));
      File outFile = File.createTempFile("jalview_pdb", ".txt");
      outFile.deleteOnExit();
      PrintWriter out = new PrintWriter(new FileOutputStream(outFile));
      String data;

      while ( (data = in.readLine()) != null)
      {
        out.println(data);
      }
      out.close();

      alreadyLoadedPDB.put(pdbId, outFile.getAbsolutePath());
      return outFile.getAbsolutePath();

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    return null;
  }

  AlignFrame LoadFromObject(JalviewModel object,
                            String file,
                            boolean loadTreesAndStructures)
  {
    SequenceSet vamsasSet = object.getVamsasModel().getSequenceSet(0);
    Sequence[] vamsasSeq = vamsasSet.getSequence();

    JalviewModelSequence jms = object.getJalviewModelSequence();

    Viewport view = jms.getViewport(0);

    //////////////////////////////////
    //LOAD SEQUENCES

    Vector hiddenSeqs = null;
    jalview.datamodel.Sequence jseq;

    ArrayList tmpseqs = new ArrayList();

    boolean multipleView = false;

    JSeq[] JSEQ = object.getJalviewModelSequence().getJSeq();
    for (int i = 0; i < JSEQ.length; i++)
    {
      String seqId = JSEQ[i].getId() + "";

      if (seqRefIds.get(seqId) != null)
      {
        tmpseqs.add( (jalview.datamodel.Sequence) seqRefIds.get(seqId));
        multipleView = true;
      }
      else
      {
        jseq = new jalview.datamodel.Sequence(vamsasSeq[i].getName(),
                                              vamsasSeq[i].getSequence());
        jseq.setDescription(vamsasSeq[i].getDescription());
        jseq.setStart(JSEQ[i].getStart());
        jseq.setEnd(JSEQ[i].getEnd());
        seqRefIds.put(vamsasSeq[i].getId(), jseq);
        tmpseqs.add(jseq);
      }

      if (JSEQ[i].getHidden())
      {
        if (hiddenSeqs == null)
        {
          hiddenSeqs = new Vector();
        }

        hiddenSeqs.addElement(
            (jalview.datamodel.Sequence) seqRefIds.get(seqId));
      }

    }

    ///SequenceFeatures are added to the DatasetSequence,
    // so we must create the dataset before loading features
    /////////////////////////////////


    jalview.datamodel.Sequence[] orderedSeqs = new jalview.datamodel.Sequence[
        tmpseqs.size()];

    tmpseqs.toArray(orderedSeqs);

    jalview.datamodel.Alignment al =
        new jalview.datamodel.Alignment(orderedSeqs);

    for(int i=0; i<vamsasSet.getSequenceSetPropertiesCount(); i++ )
    {
      SequenceSetProperties ssp = vamsasSet.getSequenceSetProperties(i);
      al.setProperty(ssp.getKey(), ssp.getValue());
    }


    al.setDataset(null);
    /////////////////////////////////


    Hashtable pdbloaded = new Hashtable();

    if (!multipleView)
    {
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
                features[f].getFeatureGroup());

            sf.setScore(features[f].getScore());
            for (int od = 0; od < features[f].getOtherDataCount(); od++)
            {
              OtherData keyValue = features[f].getOtherData(od);
              if (keyValue.getKey().startsWith("LINK"))
              {
                sf.addLink(keyValue.getValue());
              }
              else
              {
                sf.setValue(keyValue.getKey(), keyValue.getValue());
              }

            }

            al.getSequenceAt(i).getDatasetSequence().addSequenceFeature(sf);
          }
        }

        if (JSEQ[i].getPdbidsCount() > 0)
        {
          Pdbids[] ids = JSEQ[i].getPdbids();
          for (int p = 0; p < ids.length; p++)
          {
            jalview.datamodel.PDBEntry entry = new jalview.datamodel.
                PDBEntry();
            entry.setId(ids[p].getId());
            entry.setType(ids[p].getType());
            if (ids[p].getFile() != null)
            {
              if (!pdbloaded.containsKey(ids[p].getFile()))
              {
                entry.setFile(loadPDBFile(file, ids[p].getId()));
              }
              else
              {
                entry.setFile(pdbloaded.get(ids[p].getId()).toString());
              }
            }

            al.getSequenceAt(i).getDatasetSequence().addPDBId(entry);
          }
        }
        if (vamsasSeq[i].getDBRefCount() > 0)
        {
          for (int d = 0; d < vamsasSeq[i].getDBRefCount(); d++)
          {
            jalview.datamodel.DBRefEntry entry =
                new jalview.datamodel.DBRefEntry(
                    vamsasSeq[i].getDBRef(d).getSource(),
                    vamsasSeq[i].getDBRef(d).getVersion(),
                    vamsasSeq[i].getDBRef(d).getAccessionId()
                );
            al.getSequenceAt(i).getDatasetSequence().addDBRef(entry);
          }

        }
      }
    }

    /////////////////////////////////
    //////////////////////////////////
    //LOAD ANNOTATIONS
    boolean hideQuality = true,
        hideConservation = true,
        hideConsensus = true;

    if (vamsasSet.getAnnotationCount() > 0)
    {
      Annotation[] an = vamsasSet.getAnnotation();

      for (int i = 0; i < an.length; i++)
      {
        if (an[i].getLabel().equals("Quality"))
        {
          hideQuality = false;
          continue;
        }
        else if (an[i].getLabel().equals("Conservation"))
        {
          hideConservation = false;
          continue;
        }
        else if (an[i].getLabel().equals("Consensus"))
        {
          hideConsensus = false;
          continue;
        }

        if (an[i].getId() != null
            && annotationIds.containsKey(an[i].getId()))
        {
          jalview.datamodel.AlignmentAnnotation jda =
              (jalview.datamodel.AlignmentAnnotation) annotationIds.get(an[i].
              getId());
          if (an[i].hasVisible())
            jda.visible = an[i].getVisible();

          al.addAnnotation(jda);

          continue;
        }

        AnnotationElement[] ae = an[i].getAnnotationElement();
        jalview.datamodel.Annotation[] anot = null;

        if (!an[i].getScoreOnly())
        {
          anot = new jalview.datamodel.Annotation[
                    al.getWidth()];

          for (int aa = 0; aa < ae.length && aa < anot.length; aa++)
          {
            if(ae[aa].getPosition()>=anot.length)
              continue;

            anot[ae[aa].getPosition()] = new jalview.datamodel.Annotation(

                    ae[aa].getDisplayCharacter(),
                    ae[aa].getDescription(),
                    (ae[aa].getSecondaryStructure()==null || ae[aa].getSecondaryStructure().length() == 0) ? ' ' :
                      ae[aa].getSecondaryStructure().charAt(0),
                      ae[aa].getValue()

                            );


              anot[ae[aa].getPosition()].colour = new java.awt.Color(ae[aa].
                  getColour());
          }
        }
        jalview.datamodel.AlignmentAnnotation jaa = null;

        if (an[i].getGraph())
        {
          jaa = new jalview.datamodel.AlignmentAnnotation(an[i].getLabel(),
              an[i].getDescription(), anot, 0, 0,
              an[i].getGraphType());

          jaa.graphGroup = an[i].getGraphGroup();

          if (an[i].getThresholdLine() != null)
          {
            jaa.setThreshold(new jalview.datamodel.GraphLine(
                an[i].getThresholdLine().getValue(),
                an[i].getThresholdLine().getLabel(),
                new java.awt.Color(an[i].getThresholdLine().getColour()))
                );

          }

        }
        else
        {
          jaa = new jalview.datamodel.AlignmentAnnotation(an[i].getLabel(),
              an[i].getDescription(), anot);
        }

        if (an[i].getId() != null)
        {
          annotationIds.put(an[i].getId(), jaa);
          jaa.annotationId = an[i].getId();
        }

        if (an[i].getSequenceRef() != null)
        {
          if (al.findName(an[i].getSequenceRef()) != null)
          {
            jaa.createSequenceMapping(
                al.findName(an[i].getSequenceRef()), 1, true
                );
            al.findName(an[i].getSequenceRef()).addAlignmentAnnotation(jaa);
          }
        }
        if (an[i].hasScore())
        {
          jaa.setScore(an[i].getScore());
        }

        if(an[i].hasVisible())
          jaa.visible =  an[i].getVisible();

        al.addAnnotation(jaa);
      }
    }

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

        for (int s = 0; s < groups[i].getSeqCount(); s++)
        {
          String seqId = groups[i].getSeq(s) + "";
          jalview.datamodel.SequenceI ts = (jalview.datamodel.SequenceI)
              seqRefIds.get(seqId);

          if (ts != null)
          {
            seqs.addElement(ts);
          }
        }

        if (seqs.size() < 1)
        {
          continue;
        }

        jalview.datamodel.SequenceGroup sg = new jalview.datamodel.
            SequenceGroup(seqs,
                          groups[i].getName(), cs, groups[i].getDisplayBoxes(),
                          groups[i].getDisplayText(), groups[i].getColourText(),
                          groups[i].getStart(), groups[i].getEnd());

        sg.setOutlineColour(new java.awt.Color(
            groups[i].getOutlineColour()));

        sg.textColour = new java.awt.Color(groups[i].getTextCol1());
        sg.textColour2 = new java.awt.Color(groups[i].getTextCol2());
        sg.thresholdTextColour = groups[i].getTextColThreshold();

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

    /////////////////////////////////
    // LOAD VIEWPORT

    AlignFrame af = new AlignFrame(al,
                                   view.getWidth(),
                                   view.getHeight());

    af.setFileName(file, "Jalview");

    for (int i = 0; i < JSEQ.length; i++)
    {
      af.viewport.setSequenceColour(
          af.viewport.alignment.getSequenceAt(i),
          new java.awt.Color(
              JSEQ[i].getColour()));
    }

    //If we just load in the same jar file again, the sequenceSetId
    //will be the same, and we end up with multiple references
    //to the same sequenceSet. We must modify this id on load
    //so that each load of the file gives a unique id
    String uniqueSeqSetId = view.getSequenceSetId() + uniqueSetSuffix;

    af.viewport.gatherViewsHere = view.getGatheredViews();

    if (view.getSequenceSetId() != null)
    {
      jalview.gui.AlignViewport av =
          (jalview.gui.AlignViewport)
          viewportsAdded.get(uniqueSeqSetId);

      af.viewport.sequenceSetID = uniqueSeqSetId;
      if (av != null)
      {

        af.viewport.historyList = av.historyList;
        af.viewport.redoList = av.redoList;
      }
      else
      {
        viewportsAdded.put(uniqueSeqSetId, af.viewport);
      }

      PaintRefresher.Register(af.alignPanel, uniqueSeqSetId);
    }
    if (hiddenSeqs != null)
    {
      for (int s = 0; s < JSEQ.length; s++)
      {
        jalview.datamodel.SequenceGroup hidden =
            new jalview.datamodel.SequenceGroup();

        for (int r = 0; r < JSEQ[s].getHiddenSequencesCount(); r++)
        {
          hidden.addSequence(
              al.getSequenceAt(JSEQ[s].getHiddenSequences(r))
              , false
              );
        }
        af.viewport.hideRepSequences(al.getSequenceAt(s), hidden);
      }

      jalview.datamodel.SequenceI[] hseqs = new
          jalview.datamodel.SequenceI[hiddenSeqs.size()];

      for (int s = 0; s < hiddenSeqs.size(); s++)
      {
        hseqs[s] = (jalview.datamodel.SequenceI) hiddenSeqs.elementAt(s);
      }

      af.viewport.hideSequence(hseqs);

    }

    if ( (hideConsensus || hideQuality || hideConservation)
        && al.getAlignmentAnnotation() != null)
    {
      int hSize = al.getAlignmentAnnotation().length;
      for (int h = 0; h < hSize; h++)
      {
        if (
            (hideConsensus &&
             al.getAlignmentAnnotation()[h].label.equals("Consensus"))
            ||
            (hideQuality &&
             al.getAlignmentAnnotation()[h].label.equals("Quality"))
            ||
            (hideConservation &&
             al.getAlignmentAnnotation()[h].label.equals("Conservation")))
        {
          al.deleteAnnotation(al.getAlignmentAnnotation()[h]);
          hSize--;
          h--;
        }
      }
      af.alignPanel.adjustAnnotationHeight();
    }

    if (view.getViewName() != null)
    {
      af.viewport.viewName = view.getViewName();
      af.setInitialTabVisible();
    }
    af.setBounds(view.getXpos(), view.getYpos(), view.getWidth(),
                 view.getHeight());

    af.viewport.setShowAnnotation(view.getShowAnnotation());
    af.viewport.setAbovePIDThreshold(view.getPidSelected());

    af.viewport.setColourText(view.getShowColourText());

    af.viewport.setConservationSelected(view.getConservationSelected());
    af.viewport.setShowJVSuffix(view.getShowFullId());
    af.viewport.rightAlignIds = view.getRightAlignIds();
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

    af.viewport.textColour = new java.awt.Color(view.getTextCol1());
    af.viewport.textColour2 = new java.awt.Color(view.getTextCol2());
    af.viewport.thresholdTextColour = view.getTextColThreshold();

    af.viewport.setStartRes(view.getStartRes());
    af.viewport.setStartSeq(view.getStartSeq());

    ColourSchemeI cs = null;

    if (view.getBgColour() != null)
    {
      if (view.getBgColour().startsWith("ucs"))
      {
        cs = GetUserColourScheme(jms, view.getBgColour());
      }
      else if (view.getBgColour().startsWith("Annotation"))
      {
        //int find annotation
        for (int i = 0;
             i < af.viewport.alignment.getAlignmentAnnotation().length; i++)
        {
          if (af.viewport.alignment.getAlignmentAnnotation()[i].label.
              equals(view.getAnnotationColours().getAnnotation()))
          {
            if (af.viewport.alignment.getAlignmentAnnotation()[i].
                getThreshold() == null)
            {
              af.viewport.alignment.getAlignmentAnnotation()[i].
                  setThreshold(
                      new jalview.datamodel.GraphLine(
                          view.getAnnotationColours().getThreshold(),
                          "Threshold", java.awt.Color.black)

                  );
            }

            if (view.getAnnotationColours().getColourScheme().equals(
                "None"))
            {
              cs = new AnnotationColourGradient(
                  af.viewport.alignment.getAlignmentAnnotation()[i],
                  new java.awt.Color(view.getAnnotationColours().
                                     getMinColour()),
                  new java.awt.Color(view.getAnnotationColours().
                                     getMaxColour()),
                  view.getAnnotationColours().getAboveThreshold());
            }
            else if (view.getAnnotationColours().getColourScheme().
                     startsWith("ucs"))
            {
              cs = new AnnotationColourGradient(
                  af.viewport.alignment.getAlignmentAnnotation()[i],
                  GetUserColourScheme(jms, view.getAnnotationColours().
                                      getColourScheme()),
                  view.getAnnotationColours().getAboveThreshold()
                  );
            }
            else
            {
              cs = new AnnotationColourGradient(
                  af.viewport.alignment.getAlignmentAnnotation()[i],
                  ColourSchemeProperty.getColour(al,
                                                 view.getAnnotationColours().
                                                 getColourScheme()),
                  view.getAnnotationColours().getAboveThreshold()
                  );
            }

            // Also use these settings for all the groups
            if (al.getGroups() != null)
            {
              for (int g = 0; g < al.getGroups().size(); g++)
              {
                jalview.datamodel.SequenceGroup sg
                    = (jalview.datamodel.SequenceGroup) al.getGroups().
                    elementAt(g);

                if (sg.cs == null)
                {
                  continue;
                }

                /*    if (view.getAnnotationColours().getColourScheme().equals("None"))
                    {
                      sg.cs = new AnnotationColourGradient(
                          af.viewport.alignment.getAlignmentAnnotation()[i],
                          new java.awt.Color(view.getAnnotationColours().
                                             getMinColour()),
                          new java.awt.Color(view.getAnnotationColours().
                                             getMaxColour()),
                          view.getAnnotationColours().getAboveThreshold());
                    }
                    else*/
                {
                  sg.cs = new AnnotationColourGradient(
                      af.viewport.alignment.getAlignmentAnnotation()[i],
                      sg.cs,
                      view.getAnnotationColours().getAboveThreshold()
                      );
                }

              }
            }

            break;
          }

        }
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

    if (view.getConservationSelected() && cs != null)
    {
      cs.setConservationInc(view.getConsThreshold());
    }

    af.changeColour(cs);

    af.viewport.setColourAppliesToAllGroups(true);

    if (view.getShowSequenceFeatures())
    {
      af.viewport.showSequenceFeatures = true;
    }

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
        if (setting.hasOrder())
          af.alignPanel.seqPanel.seqCanvas.getFeatureRenderer().setOrder(setting.getType(), setting.getOrder());
        else
          af.alignPanel.seqPanel.seqCanvas.getFeatureRenderer().setOrder(setting.getType(), fs/jms.getFeatureSettings().getSettingCount());
        if (setting.getDisplay())
        {
          af.viewport.featuresDisplayed.put(
              setting.getType(), new Integer(setting.getColour()));
        }
      }
      af.alignPanel.seqPanel.seqCanvas.getFeatureRenderer().renderOrder =
          renderOrder;
      Hashtable fgtable;
      af.alignPanel.seqPanel.seqCanvas.getFeatureRenderer().featureGroups = fgtable=new Hashtable();
      for (int gs=0;gs<jms.getFeatureSettings().getGroupCount(); gs++)
      {
        Group grp = jms.getFeatureSettings().getGroup(gs);
        fgtable.put(grp.getName(), new Boolean(grp.getDisplay()));
      }
    }

    if (view.getHiddenColumnsCount() > 0)
    {
      for (int c = 0; c < view.getHiddenColumnsCount(); c++)
      {
        af.viewport.hideColumns(
            view.getHiddenColumns(c).getStart(),
            view.getHiddenColumns(c).getEnd() //+1
            );
      }
    }

    af.setMenusFromViewport(af.viewport);

    Desktop.addInternalFrame(af, view.getTitle(),
                             view.getWidth(), view.getHeight());

    //LOAD TREES
    ///////////////////////////////////////
    if (loadTreesAndStructures && jms.getTreeCount() > 0)
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

    ////LOAD STRUCTURES
    if(loadTreesAndStructures)
    {
      for (int i = 0; i < JSEQ.length; i++)
      {
        if (JSEQ[i].getPdbidsCount() > 0)
        {
          Pdbids[] ids = JSEQ[i].getPdbids();
          for (int p = 0; p < ids.length; p++)
          {
            for (int s = 0; s < ids[p].getStructureStateCount(); s++)
            {
              jalview.datamodel.PDBEntry jpdb = new jalview.datamodel.PDBEntry();

              jpdb.setFile(loadPDBFile(ids[p].getFile(), ids[p].getId()));
              jpdb.setId(ids[p].getId());

              int x = ids[p].getStructureState(s).getXpos();
              int y = ids[p].getStructureState(s).getYpos();
              int width = ids[p].getStructureState(s).getWidth();
              int height = ids[p].getStructureState(s).getHeight();

              java.awt.Component comp = null;

              JInternalFrame [] frames = Desktop.desktop.getAllFrames();
              for(int f=0; f<frames.length; f++)
              {
                if(frames[f] instanceof AppJmol)
                {
                  if (frames[f].getX() == x && frames[f].getY() == y
                      && frames[f].getHeight() == height
                      && frames[f].getWidth() == width)
                  {
                    comp = frames[f];
                    break;
                  }
                }
              }


              Desktop.desktop.getComponentAt(x, y);

              String pdbFile = loadPDBFile(file, ids[p].getId());

              jalview.datamodel.SequenceI[] seq = new jalview.datamodel.
                  SequenceI[]
                  {
                   (jalview.datamodel.SequenceI)
                  seqRefIds.get(JSEQ[i].getId()+"")};


              if (comp == null)
              {
                String state = ids[p].getStructureState(s).getContent();

                StringBuffer newFileLoc = new StringBuffer(state.substring(0,
                    state.indexOf("\"", state.indexOf("load")) + 1));

                newFileLoc.append(jpdb.getFile());
                newFileLoc.append(state.substring(
                    state.indexOf("\"", state.indexOf("load \"") + 6)));

                new AppJmol(pdbFile,
                            ids[p].getId(),
                            seq,
                            af.alignPanel,
                            newFileLoc.toString(),
                            new java.awt.Rectangle(x, y, width, height));

              }
              else if(comp!=null)
              {
                StructureSelectionManager.getStructureSelectionManager()
                    .setMapping(seq, null, pdbFile,
                                jalview.io.AppletFormatAdapter.FILE);

                ( (AppJmol) comp).addSequence(seq);
              }
            }
          }
        }
      }
    }

    return af;
  }

  public jalview.gui.AlignmentPanel copyAlignPanel(AlignmentPanel ap,
      boolean keepSeqRefs)
  {
    jalview.schemabinding.version2.JalviewModel jm
        = SaveState(ap, null, null, null);

    if (!keepSeqRefs)
    {
      seqRefIds.clear();
      jm.getJalviewModelSequence().getViewport(0).setSequenceSetId(null);
    }
    else
    {
      uniqueSetSuffix = "";
    }

    viewportsAdded = new Hashtable();

    AlignFrame af = LoadFromObject(jm, null, false);
    af.alignPanels.clear();
    af.closeMenuItem_actionPerformed(true);

    /*  if(ap.av.alignment.getAlignmentAnnotation()!=null)
      {
        for(int i=0; i<ap.av.alignment.getAlignmentAnnotation().length; i++)
        {
          if(!ap.av.alignment.getAlignmentAnnotation()[i].autoCalculated)
          {
            af.alignPanel.av.alignment.getAlignmentAnnotation()[i] =
                ap.av.alignment.getAlignmentAnnotation()[i];
          }
        }
      }   */

    return af.alignPanel;
  }
}
