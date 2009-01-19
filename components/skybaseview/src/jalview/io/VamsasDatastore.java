/*
 * Jalview - A Sequence Alignment Editor and Viewer
 * Copyright (C) 2005 AM Waterhouse, J Procter, G Barton, M Clamp, S Searle
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

package jalview.io;

import jalview.bin.Cache;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.gui.Desktop;
import jalview.gui.TreePanel;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Vector;

import org.vamsas.client.Vobject;
import org.vamsas.client.VorbaId;
import org.vamsas.objects.core.Alignment;
import org.vamsas.objects.core.AlignmentSequence;
import org.vamsas.objects.core.AlignmentSequenceAnnotation;
import org.vamsas.objects.core.AnnotationElement;
import org.vamsas.objects.core.DataSet;
import org.vamsas.objects.core.DataSetAnnotations;
import org.vamsas.objects.core.DbRef;
import org.vamsas.objects.core.Entry;
import org.vamsas.objects.core.Glyph;
import org.vamsas.objects.core.Input;
import org.vamsas.objects.core.Link;
import org.vamsas.objects.core.Newick;
import org.vamsas.objects.core.Param;
import org.vamsas.objects.core.Property;
import org.vamsas.objects.core.Provenance;
import org.vamsas.objects.core.RangeAnnotation;
import org.vamsas.objects.core.RangeType;
import org.vamsas.objects.core.Seg;
import org.vamsas.objects.core.Sequence;
import org.vamsas.objects.core.Tree;
import org.vamsas.objects.core.VAMSAS;
import org.vamsas.test.simpleclient.ClientDoc;

/*
 *
 * static {
 * org.exolab.castor.util.LocalConfiguration.getInstance().getProperties().setProperty(
 * "org.exolab.castor.serializer", "org.apache.xml.serialize.XMLSerilazizer"); }
 *
 */

public class VamsasDatastore
{
  Entry provEntry = null;

  // AlignViewport av;

  org.exolab.castor.types.Date date = new org.exolab.castor.types.Date(
      new java.util.Date());

  ClientDoc cdoc;

  Hashtable vobj2jv;

  IdentityHashMap jv2vobj;

  public VamsasDatastore(ClientDoc cdoc, Hashtable vobj2jv,
                         IdentityHashMap jv2vobj, Entry provEntry)
  {
    this.cdoc = cdoc;
    this.vobj2jv = vobj2jv;
    this.jv2vobj = jv2vobj;
    this.provEntry = provEntry;
  }

  /*
   * public void storeJalview(String file, AlignFrame af) { try { // 1. Load the
   * mapping information from the file Mapping map = new
   * Mapping(getClass().getClassLoader()); java.net.URL url =
   * getClass().getResource("/jalview_mapping.xml"); map.loadMapping(url); // 2.
   * Unmarshal the data // Unmarshaller unmar = new Unmarshaller();
   * //unmar.setIgnoreExtraElements(true); //unmar.setMapping(map); // uni =
   * (UniprotFile) unmar.unmarshal(new FileReader(file)); // 3. marshal the data
   * with the total price back and print the XML in the console Marshaller
   * marshaller = new Marshaller( new FileWriter(file) );
   *
   * marshaller.setMapping(map); marshaller.marshal(af); } catch (Exception e) {
   * e.printStackTrace(); } }
   *
   *
   */
  /**
   * @return the Vobject bound to Jalview datamodel object
   */
  protected Vobject getjv2vObj(Object jvobj)
  {
    if (jv2vobj.containsKey(jvobj))
    {
      return cdoc.getObject( (VorbaId) jv2vobj.get(jvobj));
    }
    return null;
  }

  /**
   *
   * @param vobj
   * @return Jalview datamodel object bound to the vamsas document object
   */
  protected Object getvObj2jv(org.vamsas.client.Vobject vobj)
  {
    VorbaId id = vobj.getVorbaId();
    if (id == null)
    {
      id = cdoc.registerObject(vobj);
      Cache.log
          .debug("Registering new object and returning null for getvObj2jv");
      return null;
    }
    if (vobj2jv.containsKey(vobj.getVorbaId()))
    {
      return vobj2jv.get(vobj.getVorbaId());
    }
    return null;
  }

  protected void bindjvvobj(Object jvobj, org.vamsas.client.Vobject vobj)
  {
    VorbaId id = vobj.getVorbaId();
    if (id == null)
    {
      id = cdoc.registerObject(vobj);
      if (id == null || vobj.getVorbaId() == null)
      {
        Cache.log.error("Failed to get id for " +
                        (vobj.isRegisterable() ? "registerable" :
                         "unregisterable") + " object " + vobj);
      }
    }

    if (vobj2jv.containsKey(vobj.getVorbaId()) &&
        ! ( (VorbaId) vobj2jv.get(vobj.getVorbaId())).equals(jvobj))
    {
      Cache.log.debug("Warning? Overwriting existing vamsas id binding for " +
                      vobj.getVorbaId(),
                      new Exception("Overwriting vamsas id binding."));
    }
    else if (jv2vobj.containsKey(jvobj) &&
             ! ( (VorbaId) jv2vobj.get(jvobj)).equals(vobj.getVorbaId()))
    {
      Cache.log.debug(
          "Warning? Overwriting existing jalview object binding for " + jvobj,
          new Exception("Overwriting jalview object binding."));
    }
    /* Cache.log.error("Attempt to make conflicting object binding! "+vobj+" id " +vobj.getVorbaId()+" already bound to "+getvObj2jv(vobj)+" and "+jvobj+" already bound to "+getjv2vObj(jvobj),new Exception("Excessive call to bindjvvobj"));
         }*/
    // we just update the hash's regardless!
    vobj2jv.put(vobj.getVorbaId(), jvobj);
    // JBPNote - better implementing a hybrid invertible hash.
    jv2vobj.put(jvobj, vobj.getVorbaId());
  }

  /**
   * put the alignment viewed by AlignViewport into cdoc.
   *
   * @param av alignViewport to be stored
   * @param aFtitle title for alignment
   */
  public void storeVAMSAS(AlignViewport av, String aFtitle)
  {
    try
    {
      jalview.datamodel.AlignmentI jal = av.getAlignment();
      boolean nw = false;
      VAMSAS root = null; // will be resolved based on Dataset Parent.
      // /////////////////////////////////////////
      // SAVE THE DATASET
      if (jal.getDataset() == null)
      {
        Cache.log.warn("Creating new dataset for an alignment.");
        jal.setDataset(null);
      }
      DataSet dataset = (DataSet) getjv2vObj(jal.getDataset());
      if (dataset == null)
      {
        root = cdoc.getVamsasRoots()[0]; // default vamsas root for modifying.
        dataset = new DataSet();
        root.addDataSet(dataset);
        bindjvvobj(jal.getDataset(), dataset);
        dataset.setProvenance(dummyProvenance());
        dataset.getProvenance().addEntry(provEntry);
        nw = true;
      }
      else
      {
        root = (VAMSAS) dataset.getV_parent();
      }
      // update dataset
      Sequence sequence;
      DbRef dbref;
      // set new dataset and alignment sequences based on alignment Nucleotide
      // flag.
      // this *will* break when alignment contains both nucleotide and amino
      // acid sequences.
      String dict = jal.isNucleotide() ?
          org.vamsas.objects.utils.SymbolDictionary.STANDARD_NA
          : org.vamsas.objects.utils.SymbolDictionary.STANDARD_AA;
      for (int i = 0; i < jal.getHeight(); i++)
      {
        SequenceI sq = jal.getSequenceAt(i).getDatasetSequence(); // only insert
        // referenced
        // sequences
        // to dataset.
        sequence = (Sequence) getjv2vObj(sq);
        if (sequence == null)
        {
          sequence = new Sequence();
          bindjvvobj(sq, sequence);
          sq.setVamsasId(sequence.getVorbaId().getId());
          sequence.setSequence(sq.getSequenceAsString());
          sequence.setDictionary(dict);
          sequence.setName(jal.getDataset().getSequenceAt(i).getName());
          sequence.setStart(jal.getDataset().getSequenceAt(i).getStart());
          sequence.setEnd(jal.getDataset().getSequenceAt(i).getEnd());
          dataset.addSequence(sequence);
        }
        else
        {
          // verify principal attributes. and update any new
          // features/references.
          System.out.println("update dataset sequence object.");
        }
        if (sq.getSequenceFeatures() != null)
        {
          int sfSize = sq.getSequenceFeatures().length;

          for (int sf = 0; sf < sfSize; sf++)
          {
            jalview.datamodel.SequenceFeature feature = (jalview.datamodel.
                SequenceFeature) sq
                .getSequenceFeatures()[sf];

            DataSetAnnotations dsa = (DataSetAnnotations) getjv2vObj(feature);
            if (dsa == null)
            {
              dsa = (DataSetAnnotations) getDSAnnotationFromJalview(
                  new DataSetAnnotations(), feature);
              if (dsa.getProvenance() == null)
              {
                dsa.setProvenance(new Provenance());
              }
              addProvenance(dsa.getProvenance(), "created"); // JBPNote - need
              // to update
              dsa.setSeqRef(sequence);
              bindjvvobj(feature, dsa);
              dataset.addDataSetAnnotations(dsa);
            }
            else
            {
              // todo: verify and update dataset annotations for sequence
              System.out.println("update dataset sequence annotations.");
            }
          }
        }

        if (sq.getDBRef() != null)
        {
          DBRefEntry[] entries = sq.getDBRef();
          jalview.datamodel.DBRefEntry dbentry;
          for (int db = 0; db < entries.length; db++)
          {
            dbentry = entries[db];
            dbref = (DbRef) getjv2vObj(dbentry);
            if (dbref == null)
            {
              dbref = new DbRef();
              bindjvvobj(dbentry, dbref);
              dbref.setAccessionId(dbentry.getAccessionId());
              dbref.setSource(dbentry.getSource());
              dbref.setVersion(dbentry.getVersion());
              /*
               * TODO: Maps are not yet supported by Jalview. Map vMap = new
               * Map(); vMap.set dbref.addMap(vMap);
               */
              sequence.addDbRef(dbref);
            }
            else
            {
              // TODO: verify and update dbrefs in vamsas document
              // there will be trouble when a dataset sequence is modified to
              // contain more residues than were originally referenced - we must
              // then make a number of dataset sequence entries
              System.out
                  .println("update dataset sequence database references.");
            }
          }

        }
      }
      // dataset.setProvenance(getVamsasProvenance(jal.getDataset().getProvenance()));
      // ////////////////////////////////////////////

      // ////////////////////////////////////////////
      // Save the Alignments

      Alignment alignment = (Alignment) getjv2vObj(av); // this is so we can get the alignviewport back
      if (alignment == null)
      {
        alignment = new Alignment();
        bindjvvobj(av, alignment);
        if (alignment.getProvenance() == null)
        {
          alignment.setProvenance(new Provenance());
        }
        addProvenance(alignment.getProvenance(), "added"); // TODO: insert some
        // sensible source
        // here
        dataset.addAlignment(alignment);
        {
          Property title = new Property();
          title.setName("jalview:AlTitle");
          title.setType("string");
          title.setContent(aFtitle);
          alignment.addProperty(title);
        }
        alignment.setGapChar(String.valueOf(av.getGapCharacter()));
        AlignmentSequence alseq = null;
        for (int i = 0; i < jal.getHeight(); i++)
        {
          alseq = new AlignmentSequence();
          // TODO: VAMSAS: translate lowercase symbols to annotation ?
          alseq.setSequence(jal.getSequenceAt(i).getSequenceAsString());
          alseq.setName(jal.getSequenceAt(i).getName());
          alseq.setStart(jal.getSequenceAt(i).getStart());
          alseq.setEnd(jal.getSequenceAt(i).getEnd());
          alseq.setRefid(getjv2vObj(jal.getSequenceAt(i).getDatasetSequence()));
          alignment.addAlignmentSequence(alseq);
          bindjvvobj(jal.getSequenceAt(i), alseq);
        }
      }
      else
      {
        // todo: verify and update mutable alignment props.
        if (alignment.getModifiable())
        {
          System.out.println("update alignment in document.");
        }
        else
        {
          System.out
              .println("update edited alignment to new alignment in document.");
        }
      }
      // ////////////////////////////////////////////
      // SAVE Alignment Sequence Features
      for (int i = 0, iSize = alignment.getAlignmentSequenceCount(); i < iSize;
           i++)
      {
        AlignmentSequence valseq;
        SequenceI alseq = (SequenceI) getvObj2jv(valseq = alignment
                                                 .getAlignmentSequence(i));
        if (alseq != null && alseq.getSequenceFeatures() != null)
        {
          jalview.datamodel.SequenceFeature[] features = alseq
              .getSequenceFeatures();
          for (int f = 0; f < features.length; f++)
          {
            if (features[f] != null)
            {
              AlignmentSequenceAnnotation valseqf = (
                  AlignmentSequenceAnnotation) getjv2vObj(features[i]);
              if (valseqf == null)
              {

                valseqf = (AlignmentSequenceAnnotation)
                    getDSAnnotationFromJalview(
                        new AlignmentSequenceAnnotation(), features[i]);
                if (valseqf.getProvenance() == null)
                {
                  valseqf.setProvenance(new Provenance());
                }
                addProvenance(valseqf.getProvenance(), "created"); // JBPNote -
                // need to
                // update
                bindjvvobj(features[i], valseqf);
                valseq.addAlignmentSequenceAnnotation(valseqf);
              }
            }

          }
        }
      }

      // ////////////////////////////////////////////
      // SAVE ANNOTATIONS
      if (jal.getAlignmentAnnotation() != null)
      {
        jalview.datamodel.AlignmentAnnotation[] aa = jal
            .getAlignmentAnnotation();
        java.util.HashMap AlSeqMaps = new HashMap(); // stores int maps from
        // alignment columns to
        // sequence positions.
        for (int i = 0; i < aa.length; i++)
        {
          if (aa[i] == null || isJalviewOnly(aa[i]))
          {
            continue;
          }
          if (aa[i].sequenceRef != null)
          {
            org.vamsas.objects.core.AlignmentSequence alsref = (org.vamsas.
                objects.core.AlignmentSequence) getjv2vObj(aa[i].sequenceRef);
            org.vamsas.objects.core.AlignmentSequenceAnnotation an = (org.
                vamsas.objects.core.AlignmentSequenceAnnotation) getjv2vObj(aa[
                i]);
            int[] gapMap = null;
            if (AlSeqMaps.containsKey(aa[i].sequenceRef))
            {
              gapMap = (int[]) AlSeqMaps.get(aa[i].sequenceRef);
            }
            else
            {
              gapMap = new int[aa[i].sequenceRef.getLength()];
              // map from alignment position to sequence position.
              int[] sgapMap = aa[i].sequenceRef.gapMap();
              for (int a = 0; a < sgapMap.length; a++)
              {
                gapMap[sgapMap[a]] = a;
              }
            }
            if (an == null)
            {
              an = new org.vamsas.objects.core.AlignmentSequenceAnnotation();
              Seg vSeg = new Seg();
              vSeg.setStart(1);
              vSeg.setInclusive(true);
              vSeg.setEnd(gapMap.length);
              an.addSeg(vSeg);
              an.setType("jalview:SecondaryStructurePrediction"); // TODO: better fix this rough guess ;)
              alsref.addAlignmentSequenceAnnotation(an);
              bindjvvobj(aa[i], an);
              // LATER: much of this is verbatim from the alignmentAnnotation
              // method below. suggests refactoring to make rangeAnnotation the
              // base class
              an.setDescription(aa[i].description);
              if (aa[i].graph > 0)
              {
                an.setGraph(true); // aa[i].graph);
              }
              else
              {
                an.setGraph(false);
              }
              an.setLabel(aa[i].label);
              an.setProvenance(dummyProvenance()); // get provenance as user
              // created, or jnet, or
              // something else.
              an.setGroup(Integer.toString(aa[i].graphGroup)); // // JBPNote -
              // originally we
              // were going to
              // store
              // graphGroup in
              // the Jalview
              // specific
              // bits.
              AnnotationElement ae;
              for (int a = 0; a < aa[i].annotations.length; a++)
              {
                if (aa[i].annotations[a] == null)
                {
                  continue;
                }

                ae = new AnnotationElement();
                ae.setDescription(aa[i].annotations[a].description);
                ae.addGlyph(new Glyph());
                ae.getGlyph(0)
                    .setContent(aa[i].annotations[a].displayCharacter); // assume
                // jax-b
                // takes
                // care
                // of
                // utf8
                // translation
                if (aa[i].graph !=
                    jalview.datamodel.AlignmentAnnotation.NO_GRAPH)
                {
                  ae.addValue(aa[i].annotations[a].value);
                }
                ae.setPosition(gapMap[a] + 1); // position w.r.t. AlignmentSequence
                // symbols
                if (aa[i].annotations[a].secondaryStructure != ' ')
                {
                  // we only write an annotation where it really exists.
                  Glyph ss = new Glyph();
                  ss
                      .setDict(org.vamsas.objects.utils.GlyphDictionary.
                               PROTEIN_SS_3STATE);
                  ss.setContent(String
                                .valueOf(aa[i].annotations[a].
                                         secondaryStructure));
                  ae.addGlyph(ss);
                }
                an.addAnnotationElement(ae);
              }
            }
            else
            {
              // update reference sequence Annotation
              if (an.getModifiable())
              {
                // verify existing alignment sequence annotation is up to date
                System.out.println("update alignment sequence annotation.");
              }
              else
              {
                // verify existing alignment sequence annotation is up to date
                System.out
                    .println(
                    "make new alignment sequence annotation if modification has happened.");
              }
            }
          }
          else
          {
            // add Alignment Annotation
            org.vamsas.objects.core.AlignmentAnnotation an = (org.vamsas.
                objects.core.AlignmentAnnotation) getjv2vObj(aa[i]);
            if (an == null)
            {
              an = new org.vamsas.objects.core.AlignmentAnnotation();
              an.setType("jalview:AnnotationRow");
              an.setDescription(aa[i].description);
              alignment.addAlignmentAnnotation(an);
              Seg vSeg = new Seg();
              vSeg.setStart(1);
              vSeg.setInclusive(true);
              vSeg.setEnd(jal.getWidth());
              an.addSeg(vSeg);
              if (aa[i].graph > 0)
              {
                an.setGraph(true); // aa[i].graph);
              }
              an.setLabel(aa[i].label);
              an.setProvenance(dummyProvenance());
              if (aa[i].graph != aa[i].NO_GRAPH)
              {
                an.setGroup(Integer.toString(aa[i].graphGroup)); // // JBPNote -
                // originally we
                // were going to
                // store
                // graphGroup in
                // the Jalview
                // specific
                // bits.
                an.setGraph(true);
              }
              else
              {
                an.setGraph(false);
              }
              AnnotationElement ae;

              for (int a = 0; a < aa[i].annotations.length; a++)
              {
                if ( (aa[i] == null) || (aa[i].annotations[a] == null))
                {
                  continue;
                }

                ae = new AnnotationElement();
                ae.setDescription(aa[i].annotations[a].description);
                ae.addGlyph(new Glyph());
                ae.getGlyph(0)
                    .setContent(aa[i].annotations[a].displayCharacter); // assume
                // jax-b
                // takes
                // care
                // of
                // utf8
                // translation
                ae.addValue(aa[i].annotations[a].value);
                ae.setPosition(a + 1);
                if (aa[i].annotations[a].secondaryStructure != ' ')
                {
                  Glyph ss = new Glyph();
                  ss
                      .setDict(org.vamsas.objects.utils.GlyphDictionary.
                               PROTEIN_SS_3STATE);
                  ss.setContent(String
                                .valueOf(aa[i].annotations[a].
                                         secondaryStructure));
                  ae.addGlyph(ss);
                }
                an.addAnnotationElement(ae);
              }
              if (aa[i].editable)
              {
                //an.addProperty(newProperty("jalview:editable", null, "true"));
                an.setModifiable(true);
              }
              if (aa[i].graph != jalview.datamodel.AlignmentAnnotation.NO_GRAPH)
              {
                an.setGraph(true);
                an.setGroup(Integer.toString(aa[i].graphGroup));
                an.addProperty(newProperty("jalview:graphType", null,
                                           ( (aa[i].graph ==
                                              jalview.datamodel.AlignmentAnnotation.
                                              BAR_GRAPH) ? "BAR_GRAPH" :
                                            "LINE_GRAPH")));

                /** and on and on..
                 vProperty=new Property();
                  vProperty.setName("jalview:graphThreshhold");
                  vProperty.setContent(aa[i].threshold);
                 */

              }
            }
            else
            {
              if (an.getModifiable())
              {
                // verify annotation - update (perhaps)
                Cache.log.info(
                    "update alignment sequence annotation. not yet implemented.");
              }
              else
              {
                // verify annotation - update (perhaps)
                Cache.log.info("updated alignment sequence annotation added.");
              }
            }
          }
        }
      }
      // /////////////////////////////////////////////////////

      // //////////////////////////////////////////////
      // /SAVE THE TREES
      // /////////////////////////////////
      // FIND ANY ASSOCIATED TREES
      if (Desktop.desktop != null)
      {
        javax.swing.JInternalFrame[] frames = Desktop.instance.getAllFrames();

        for (int t = 0; t < frames.length; t++)
        {
          if (frames[t] instanceof TreePanel)
          {
            TreePanel tp = (TreePanel) frames[t];

            if (tp.getAlignment() == jal)
            {
              Tree tree = (Tree) getjv2vObj(tp);
              if (tree == null)
              {
                tree = new Tree();
                bindjvvobj(tp, tree);
                tree.setTitle(tp.getTitle());
                Newick newick = new Newick();
                // TODO: translate sequenceI to leaf mappings to vamsas
                // references - see tree specification in schema.
                newick.setContent(tp.getTree().toString());
                newick.setTitle(tp.getTitle());
                tree.addNewick(newick);
                tree.setProvenance(makeTreeProvenance(jal, tp));
                alignment.addTree(tree);
              }
              else
              {
                if (tree.getModifiable())
                {
                  // verify any changes.
                  System.out.println("Update tree in document.");
                }
                else
                {
                  System.out
                      .println("Add modified tree as new tree in document.");
                }
              }
            }
          }
        }
      }
      // Store Jalview specific stuff in the Jalview appData
      // not implemented in the SimpleDoc interface.
    }

    catch (Exception ex)
    {
      ex.printStackTrace();
    }

  }

  private Property newProperty(String name, String type, String content)
  {
    Property vProperty = new Property();
    vProperty.setName(name);
    if (type != null)
    {
      vProperty.setType(type);
    }
    else
    {
      vProperty.setType("String");
    }
    vProperty.setContent(content);
    return vProperty;
  }

  /**
   * correctly create a RangeAnnotation from a jalview sequence feature
   *
   * @param dsa
   *          (typically DataSetAnnotations or AlignmentSequenceAnnotation)
   * @param feature
   *          (the feature to be mapped from)
   * @return
   */
  private RangeAnnotation getDSAnnotationFromJalview(RangeAnnotation dsa,
      SequenceFeature feature)
  {
    dsa.setType(feature.getType());
    Seg vSeg = new Seg();
    vSeg.setStart(feature.getBegin());
    vSeg.setEnd(feature.getEnd());
    vSeg.setInclusive(true);
    dsa.addSeg(vSeg);
    dsa.setDescription(feature.getDescription());
    dsa.setStatus(feature.getStatus());
    if (feature.links != null && feature.links.size() > 0)
    {
      for (int i = 0, iSize = feature.links.size(); i < iSize; i++)
      {
        String link = (String) feature.links.elementAt(i);
        int sep = link.indexOf('|');
        if (sep > -1)
        {
          Link vLink = new Link();
          if (sep > 0)
          {
            vLink.setContent(link.substring(0, sep - 1));
          }
          else
          {
            vLink.setContent("");
          }
          vLink.setHref(link.substring(sep + 1)); // TODO: validate href.
          dsa.addLink(vLink);
        }
      }
    }
    dsa.setGroup(feature.getFeatureGroup());
    return dsa;
  }

  /**
   * correctly creates provenance for trees calculated on an alignment by
   * jalview.
   *
   * @param jal
   * @param tp
   * @return
   */
  private Provenance makeTreeProvenance(AlignmentI jal, TreePanel tp)
  {
    Provenance prov = new Provenance();
    prov.addEntry(new Entry());
    prov.getEntry(0).setAction("imported " + tp.getTitle());
    prov.getEntry(0).setUser(provEntry.getUser());
    prov.getEntry(0).setApp(provEntry.getApp());
    prov.getEntry(0).setDate(provEntry.getDate());
    if (tp.getTree().hasOriginalSequenceData())
    {
      Input vInput = new Input();
      // LATER: check to see if tree input data is contained in this alignment -
      // or just correctly resolve the tree's seqData to the correct alignment in
      // the document.
      // vInput.setObjRef(getjv2vObj(jal));
      vInput.setObjRef(getjv2vObj(tp.getViewPort()));
      prov.getEntry(0).setAction("created " + tp.getTitle());
      prov.getEntry(0).addInput(vInput);
      vInput.setName("jalview:seqdist");
      prov.getEntry(0).addParam(new Param());
      prov.getEntry(0).getParam(0).setName("treeType");
      prov.getEntry(0).getParam(0).setType("utf8");
      prov.getEntry(0).getParam(0).setContent("NJ");

      int ranges[] = tp.getTree().seqData.getVisibleContigs();
      // VisibleContigs are with respect to alignment coordinates. Still need offsets
      int start = tp.getTree().seqData.getAlignmentOrigin();
      for (int r = 0; r < ranges.length; r += 2)
      {
        Seg visSeg = new Seg();
        visSeg.setStart(1 + start + ranges[r]);
        visSeg.setEnd(start + ranges[r + 1]);
        visSeg.setInclusive(true);
        vInput.addSeg(visSeg);
      }
    }
    return prov;
  }

  /**
   *
   * @param tp
   * @return Object[] { AlignmentView, AlignmentI - reference alignment for
   *         input }
   */
  private Object[] recoverInputData(Provenance tp)
  {
    for (int pe = 0; pe < tp.getEntryCount(); pe++)
    {
      if (tp.getEntry(pe).getInputCount() > 0)
      {
        if (tp.getEntry(pe).getInputCount() > 1)
        {
          Cache.log.warn("Ignoring additional input spec in provenance entry "
                         + tp.getEntry(pe).toString());
        }
        // LATER: deal sensibly with multiple inputs.
        Input vInput = tp.getEntry(pe).getInput(0);
        if (vInput.getObjRef() instanceof org.vamsas.objects.core.Alignment)
        {
          // recover an AlignmentView for the input data
          AlignViewport javport = (AlignViewport) getvObj2jv( (org.vamsas.
              client.Vobject) vInput
              .getObjRef());
          jalview.datamodel.AlignmentI jal = javport.getAlignment();
          jalview.datamodel.CigarArray view = javport.getAlignment().
              getCompactAlignment();
          int from = 1, to = jal.getWidth();
          int offset = 0; // deleteRange modifies its frame of reference
          for (int r = 0, s = vInput.getSegCount(); r < s; r++)
          {
            Seg visSeg = vInput.getSeg(r);
            int se[] = getSegRange(visSeg, true); // jalview doesn't do bidirection alignments yet.
            if (to < se[1])
            {
              Cache.log.warn("Ignoring invalid segment in InputData spec.");
            }
            else
            {
              if (se[0] > from)
              {
                view.deleteRange(offset + from - 1, offset + se[0] - 2);
                offset -= se[0] - from;
              }
              from = se[1] + 1;
            }
          }
          if (from < to)
          {
            view.deleteRange(offset + from - 1, offset + to - 1); // final deletion - TODO: check off by
            // one for to
          }
          return new Object[]
              {
              new AlignmentView(view), jal};
        }
      }
    }
    Cache.log.debug("Returning null for input data recovery from provenance.");
    return null;
  }

  /**
   * get start<end range of segment, adjusting for inclusivity flag and
   * polarity.
   *
   * @param visSeg
   * @param ensureDirection when true - always ensure start is less than end.
   * @return int[] { start, end, direction} where direction==1 for range running from end to start.
   */
  private int[] getSegRange(Seg visSeg, boolean ensureDirection)
  {
    boolean incl = visSeg.getInclusive();
    // adjust for inclusive flag.
    int pol = (visSeg.getStart() <= visSeg.getEnd()) ? 1 : -1; // polarity of
    // region.
    int start = visSeg.getStart() + (incl ? 0 : pol);
    int end = visSeg.getEnd() + (incl ? 0 : -pol);
    if (ensureDirection && pol == -1)
    {
      // jalview doesn't deal with inverted ranges, yet.
      int t = end;
      end = start;
      start = t;
    }
    return new int[]
        {
        start, end, pol < 0 ? 1 : 0};
  }

  /**
   *
   * @param annotation
   * @return true if annotation is not to be stored in document
   */
  private boolean isJalviewOnly(AlignmentAnnotation annotation)
  {
    return annotation.label.equals("Quality")
        || annotation.label.equals("Conservation")
        || annotation.label.equals("Consensus");
  }

  /**
   * This will return the first AlignFrame viewing AlignViewport av.
   * It will break if there are more than one AlignFrames viewing a particular av.
   * This also shouldn't be in the io package.
   * @param av
   * @return alignFrame for av
   */
  public AlignFrame getAlignFrameFor(AlignViewport av)
  {
    if (Desktop.desktop != null)
    {
      javax.swing.JInternalFrame[] frames = Desktop.instance.getAllFrames();

      for (int t = 0; t < frames.length; t++)
      {
        if (frames[t] instanceof AlignFrame)
        {
          if ( ( (AlignFrame) frames[t]).getViewport() == av)
          {
            return (AlignFrame) frames[t];
          }
        }
      }
    }
    return null;
  }

  public void updateToJalview()
  {
    VAMSAS _roots[] = cdoc.getVamsasRoots();

    for (int _root = 0; _root < _roots.length; _root++)
    {
      VAMSAS root = _roots[_root];
      boolean newds = false;
      for (int _ds = 0, _nds = root.getDataSetCount(); _ds < _nds; _ds++)
      {
        // ///////////////////////////////////
        // ///LOAD DATASET
        DataSet dataset = root.getDataSet(_ds);
        int i, iSize = dataset.getSequenceCount();
        Vector dsseqs;
        jalview.datamodel.Alignment jdataset = (jalview.datamodel.Alignment)
            getvObj2jv(dataset);
        int jremain = 0;
        if (jdataset == null)
        {
          Cache.log.debug("Initialising new jalview dataset fields");
          newds = true;
          dsseqs = new Vector();
        }
        else
        {
          Cache.log.debug("Update jalview dataset from vamsas.");
          jremain = jdataset.getHeight();
          dsseqs = jdataset.getSequences();
        }

        // TODO: test sequence merging - we preserve existing non vamsas
        // sequences but add in any new vamsas ones, and don't yet update any
        // sequence attributes
        for (i = 0; i < iSize; i++)
        {
          Sequence vdseq = dataset.getSequence(i);
          jalview.datamodel.SequenceI dsseq = (SequenceI) getvObj2jv(vdseq);
          if (dsseq != null)
          {
            if (!dsseq.getSequence().equals(vdseq.getSequence()))
            {
              throw new Error(
                  "Broken! - mismatch of dataset sequence and jalview internal dataset sequence.");
            }
            jremain--;
          }
          else
          {
            dsseq = new jalview.datamodel.Sequence(
                dataset.getSequence(i).getName(),
                dataset.getSequence(i).getSequence(),
                dataset.getSequence(i).getStart(),
                dataset.getSequence(i).getEnd());
            dsseq.setDescription(dataset.getSequence(i).getDescription());
            bindjvvobj(dsseq, dataset.getSequence(i));
            dsseq.setVamsasId(dataset.getSequence(i).getVorbaId().getId());
            dsseqs.add(dsseq);
          }
          if (vdseq.getDbRefCount() > 0)
          {
            DbRef[] dbref = vdseq.getDbRef();
            for (int db = 0; db < dbref.length; db++)
            {
              jalview.datamodel.DBRefEntry dbr = (jalview.datamodel.DBRefEntry)
                  getvObj2jv(dbref[db]);
              if (dbr == null)
              {
                // add new dbref
                dsseq.addDBRef(dbr = new jalview.datamodel.DBRefEntry
                               (
                                   dbref[db].getSource().toString(),
                                   dbref[db].getVersion().toString(),
                                   dbref[db].getAccessionId().toString()));
                bindjvvobj(dbr, dbref[db]);
              }
            }
          }
        }

        if (newds)
        {
          SequenceI[] seqs = new SequenceI[dsseqs.size()];
          for (i = 0, iSize = dsseqs.size(); i < iSize; i++)
          {
            seqs[i] = (SequenceI) dsseqs.elementAt(i);
            dsseqs.setElementAt(null, i);
          }
          jdataset = new jalview.datamodel.Alignment(seqs);
          Cache.log.debug("New vamsas dataset imported into jalview.");
          bindjvvobj(jdataset, dataset);
        }
        // ////////
        // add any new dataset sequence feature annotations
        if (dataset.getDataSetAnnotations() != null)
        {
          for (int dsa = 0; dsa < dataset.getDataSetAnnotationsCount(); dsa++)
          {
            DataSetAnnotations dseta = dataset.getDataSetAnnotations(dsa);
            SequenceI dsSeq = (SequenceI) getvObj2jv( (Vobject) dseta.getSeqRef());
            if (dsSeq == null)
            {
              jalview.bin.Cache.log.warn(
                  "Couldn't resolve jalview sequenceI for dataset object reference " +
                  ( (Vobject) dataset.getDataSetAnnotations(dsa).getSeqRef()).
                  getVorbaId().getId());
            }
            else
            {
              if (dseta.getAnnotationElementCount() == 0)
              {
                jalview.datamodel.SequenceFeature sf = (jalview.datamodel.
                    SequenceFeature) getvObj2jv(dseta);
                if (sf == null)
                {
                  dsSeq.addSequenceFeature(sf = getJalviewSeqFeature(dseta));
                  bindjvvobj(sf, dseta);
                }
              }
              else
              {
                // TODO: deal with alignmentAnnotation style annotation
                // appearing on dataset sequences.
                // JBPNote: we could just add them to all alignments but
                // that may complicate cross references in the jalview
                // datamodel
                Cache.log.warn("Ignoring dataset annotation with annotationElements. Not yet supported in jalview.");
              }
            }
          }
        }

        if (dataset.getAlignmentCount() > 0)
        {
          // LOAD ALIGNMENTS from DATASET

          for (int al = 0, nal = dataset.getAlignmentCount(); al < nal; al++)
          {
            org.vamsas.objects.core.Alignment alignment = dataset.getAlignment(
                al);
            AlignViewport av = (AlignViewport) getvObj2jv(alignment);
            jalview.datamodel.AlignmentI jal = null;
            if (av != null)
            {
              jal = av.getAlignment();
            }
            iSize = alignment.getAlignmentSequenceCount();
            boolean newal = (jal == null) ? true : false;
            Vector newasAnnots = new Vector();
            char gapChar = ' '; // default for new alignments read in from the document
            if (jal != null)
            {
              dsseqs = jal.getSequences(); // for merge/update
              gapChar = jal.getGapCharacter();
            }
            else
            {
              dsseqs = new Vector();
            }
            char valGapchar = alignment.getGapChar().charAt(0);
            for (i = 0; i < iSize; i++)
            {
              AlignmentSequence valseq = alignment.getAlignmentSequence(i);
              jalview.datamodel.SequenceI alseq = (SequenceI) getvObj2jv(valseq);
              if (alseq != null)
              {
                //TODO: upperCase/LowerCase situation here ? do we allow it ?
                //if (!alseq.getSequence().equals(valseq.getSequence())) {
                // throw new Error("Broken! - mismatch of dataset sequence and jalview internal dataset sequence.");
                if (Cache.log.isDebugEnabled())
                {
                  Cache.log.debug("Updating apparently edited sequence " +
                                  alseq.getName());
                }
                // this might go *horribly* wrong
                alseq.setSequence(new String(valseq.getSequence()).replace(
                    valGapchar, gapChar));
                jremain--;
              }
              else
              {
                alseq = new jalview.datamodel.Sequence(
                    valseq.getName(),
                    valseq.getSequence().replace(valGapchar, gapChar),
                    valseq.getStart(),
                    valseq.getEnd());

                Vobject datsetseq = (Vobject) valseq.getRefid();
                if (datsetseq != null)
                {
                  alseq.setDatasetSequence( (SequenceI) getvObj2jv(datsetseq)); // exceptions if AlignemntSequence reference isn't a simple SequenceI
                }
                else
                {
                  Cache.log.error(
                      "Invalid dataset sequence id (null) for alignment sequence " +
                      valseq.getVorbaId());
                }
                bindjvvobj(alseq, valseq);
                alseq.setVamsasId(valseq.getVorbaId().getId());
                dsseqs.add(alseq);
              }
              if (valseq.getAlignmentSequenceAnnotationCount() > 0)
              {
                AlignmentSequenceAnnotation[] vasannot = valseq.
                    getAlignmentSequenceAnnotation();
                for (int a = 0; a < vasannot.length; a++)
                {
                  jalview.datamodel.AlignmentAnnotation asa = (jalview.
                      datamodel.AlignmentAnnotation) getvObj2jv(vasannot[a]); // TODO: 1:many jalview alignment sequence annotations
                  if (asa == null)
                  {
                    int se[] = getBounds(vasannot[a]);
                    asa = getjAlignmentAnnotation(jal, vasannot[a]);
                    asa.sequenceRef = alseq;
                    asa.createSequenceMapping(alseq, alseq.getStart() + se[0], false); // TODO: verify that positions in alseqAnnotation correspond to ungapped residue positions.
                    bindjvvobj(asa, vasannot[a]);
                    newasAnnots.add(asa);
                  }
                  else
                  {
                    // update existing annotation - can do this in place
                    if (vasannot[a].getModifiable())
                    {
                      Cache.log.info(
                          "UNIMPLEMENTED: not recovering user modifiable sequence alignment annotation");
                      // TODO: should at least replace with new one - otherwise things will break
                      // basically do this:
                      // int se[] = getBounds(vasannot[a]);
                      // asa.update(getjAlignmentAnnotation(jal, vasannot[a])); //  update from another annotation object in place.
                      // asa.createSequenceMapping(alseq, se[0], false);

                    }
                  }
                }
              }
            }
            if (jal == null)
            {
              SequenceI[] seqs = new SequenceI[dsseqs.size()];
              for (i = 0, iSize = dsseqs.size(); i < iSize; i++)
              {
                seqs[i] = (SequenceI) dsseqs.elementAt(i);
                dsseqs.setElementAt(null, i);
              }
              jal = new jalview.datamodel.Alignment(seqs);
              Cache.log.debug("New vamsas alignment imported into jalview " +
                              alignment.getVorbaId().getId());
              jal.setDataset(jdataset);
            }
            if (newasAnnots != null && newasAnnots.size() > 0)
            {
              // Add the new sequence annotations in to the alignment.
              for (int an = 0, anSize = newasAnnots.size(); an < anSize; an++)
              {
                jal.addAnnotation( (AlignmentAnnotation) newasAnnots.elementAt(
                    an));
                // TODO: check if anything has to be done - like calling adjustForAlignment or something.
                newasAnnots.setElementAt(null, an);
              }
              newasAnnots = null;
            }
            // //////////////////////////////////////////
            // //LOAD ANNOTATIONS FOR THE ALIGNMENT
            // ////////////////////////////////////
            if (alignment.getAlignmentAnnotationCount() > 0)
            {
              org.vamsas.objects.core.AlignmentAnnotation[] an = alignment.
                  getAlignmentAnnotation();

              for (int j = 0; j < an.length; j++)
              {
                jalview.datamodel.AlignmentAnnotation jan = (jalview.datamodel.
                    AlignmentAnnotation) getvObj2jv(an[j]);
                if (jan != null)
                {
                  // update or stay the same.
                  // TODO: should at least replace with a new one - otherwise things will break
                  // basically do this:
                  // jan.update(getjAlignmentAnnotation(jal, an[a])); //  update from another annotation object in place.

                  Cache.log.debug("update from vamsas alignment annotation to existing jalview alignment annotation.");
                  if (an[j].getModifiable())
                  {
                    // TODO: user defined annotation is totally mutable... - so load it up or throw away if locally edited.
                    Cache.log.info(
                        "NOT IMPLEMENTED - Recovering user-modifiable annotation - yet...");
                  }
                  // TODO: compare annotation element rows
                  // TODO: compare props.
                }
                else
                {
                  jan = getjAlignmentAnnotation(jal, an[j]);
                  jal.addAnnotation(jan);
                  bindjvvobj(jan, an[j]);
                }
              }
            }
            AlignFrame alignFrame;
            if (av == null)
            {
              Cache.log.debug("New alignframe for alignment " +
                              alignment.getVorbaId());
              // ///////////////////////////////
              // construct alignment view
              alignFrame = new AlignFrame(jal, AlignFrame.DEFAULT_WIDTH,
                                          AlignFrame.DEFAULT_HEIGHT);
              av = alignFrame.getViewport();
              String title = alignment.getProvenance().getEntry(alignment.
                  getProvenance().getEntryCount() - 1).getAction();
              if (alignment.getPropertyCount() > 0)
              {
                for (int p = 0, pe = alignment.getPropertyCount(); p < pe; p++)
                {
                  if (alignment.getProperty(p).getName().equals(
                      "jalview:AlTitle"))
                  {
                    title = alignment.getProperty(p).getContent();
                  }
                }
              }
              // TODO: automatically create meaningful title for a vamsas alignment using its provenance.
              jalview.gui.Desktop.addInternalFrame(alignFrame,
                  title + "(" + alignment.getVorbaId() + ")",
                  AlignFrame.DEFAULT_WIDTH,
                  AlignFrame.DEFAULT_HEIGHT);
              bindjvvobj(av, alignment);
            }
            else
            {
              // find the alignFrame for jal.
              // TODO: fix this so we retrieve the alignFrame handing av *directly*
              alignFrame = getAlignFrameFor(av);
            }
            // LOAD TREES
            // /////////////////////////////////////
            if (alignment.getTreeCount() > 0)
            {

              for (int t = 0; t < alignment.getTreeCount(); t++)
              {
                Tree tree = alignment.getTree(t);
                TreePanel tp = (TreePanel) getvObj2jv(tree);
                if (tp != null)
                {
                  Cache.log.info(
                      "Update from vamsas document to alignment associated tree not implemented yet.");
                }
                else
                {
                  // make a new tree
                  Object[] idata = this.recoverInputData(tree.getProvenance());
                  try
                  {
                    AlignmentView inputData = null;
                    if (idata != null && idata[0] != null)
                    {
                      inputData = (AlignmentView) idata[0];
                    }
                    tp = alignFrame.ShowNewickTree(
                        new jalview.io.NewickFile(tree.getNewick(0).getContent()),
                        tree.getNewick(0).getTitle() + " (" + tree.getVorbaId() +
                        ")", inputData,
                        600, 500,
                        t * 20 + 50, t * 20 + 50);
                    bindjvvobj(tp, tree);
                  }
                  catch (Exception e)
                  {
                    Cache.log.warn("Problems parsing treefile '" +
                                   tree.getNewick(0).getContent() + "'", e);
                  }
                }
              }
            }

          }
        }
      }
    }
  }

  // bitfields - should be a template in j1.5
  private static int HASSECSTR = 0;
  private static int HASVALS = 1;
  private static int HASHPHOB = 2;
  private static int HASDC = 3;
  private static int HASDESCSTR = 4;
  private static int HASTWOSTATE = 5; // not used yet.
  /**
   * parses the AnnotationElements - if they exist - into jalview.datamodel.Annotation[] rows
   * Two annotation rows are made if there are distinct annotation for both at 'pos' and 'after pos' at any particular site.
   * @param annotation
   * @return { boolean[static int constants ], int[ae.length] - map to annotated object frame, jalview.datamodel.Annotation[], jalview.datamodel.Annotation[] (after)}
   */
  private Object[] parseRangeAnnotation(org.vamsas.objects.core.RangeAnnotation
                                        annotation)
  {
    // set these attributes by looking in the annotation to decide what kind of alignment annotation rows will be made
    // TODO: potentially we might make several annotation rows from one vamsas alignment annotation. the jv2Vobj binding mechanism
    // may not quite cope with this (without binding an array of annotations to a vamsas alignment annotation)
    // summary flags saying what we found over the set of annotation rows.
    boolean[] AeContent = new boolean[]
        {
        false, false, false, false, false};
    int[] rangeMap = getMapping(annotation);
    jalview.datamodel.Annotation[][] anot = new jalview.datamodel.Annotation[][]
        {
        new jalview.datamodel.Annotation[rangeMap.length],
        new jalview.datamodel.Annotation[rangeMap.length]
    };
    boolean mergeable = true; //false  if 'after positions cant be placed on same annotation row as positions.

    if (annotation.getAnnotationElementCount() > 0)
    {
      AnnotationElement ae[] = annotation.getAnnotationElement();
      for (int aa = 0; aa < ae.length; aa++)
      {
        int pos = ae[aa].getPosition() - 1; // pos counts from 1 to (|seg.start-seg.end|+1)
        if (pos >= 0 && pos < rangeMap.length)
        {
          int row = ae[aa].getAfter() ? 1 : 0;
          if (anot[row][pos] != null)
          {
            // only time this should happen is if the After flag is set.
            Cache.log.debug("Ignoring duplicate annotation site at " + pos);
            continue;
          }
          if (anot[1 - row][pos] != null)
          {
            mergeable = false;
          }
          String desc = "";
          if (ae[aa].getDescription() != null)
          {
            desc = ae[aa].getDescription();
            if (desc.length() > 0)
            {
              // have imported valid description string
              AeContent[HASDESCSTR] = true;
            }
          }
          String dc = null; //ae[aa].getDisplayCharacter()==null ? "dc" : ae[aa].getDisplayCharacter();
          String ss = null; //ae[aa].getSecondaryStructure()==null ? "ss" : ae[aa].getSecondaryStructure();
          java.awt.Color colour = null;
          if (ae[aa].getGlyphCount() > 0)
          {
            Glyph[] glyphs = ae[aa].getGlyph();
            for (int g = 0; g < glyphs.length; g++)
            {
              if (glyphs[g].getDict().equals(org.vamsas.objects.utils.
                                             GlyphDictionary.PROTEIN_SS_3STATE))
              {
                ss = glyphs[g].getContent();
                AeContent[HASSECSTR] = true;
              }
              else if (glyphs[g].getDict().equals(org.vamsas.objects.utils.
                                                  GlyphDictionary.
                                                  PROTEIN_HD_HYDRO))
              {
                Cache.log.debug("ignoring hydrophobicity glyph marker.");
                AeContent[HASHPHOB] = true;
                char c = (dc = glyphs[g].getContent()).charAt(0);
                // dc may get overwritten - but we still set the colour.
                colour = new java.awt.Color(c == '+' ? 255 : 0,
                                            c == '.' ? 255 : 0,
                                            c == '-' ? 255 : 0);

              }
              else if (glyphs[g].getDict().equals(org.vamsas.objects.utils.
                                                  GlyphDictionary.DEFAULT))
              {
                dc = glyphs[g].getContent();
                AeContent[HASDC] = true;
              }
              else
              {
                Cache.log.debug("Ignoring unknown glyph type " +
                                glyphs[g].getDict());
              }
            }
          }
          float val = 0;
          if (ae[aa].getValueCount() > 0)
          {
            AeContent[HASVALS] = true;
            if (ae[aa].getValueCount() > 1)
            {
              Cache.log.warn("ignoring additional " +
                             (ae[aa].getValueCount() - 1) +
                             "values in annotation element.");
            }
            val = ae[aa].getValue(0);
          }
          if (colour == null)
          {
            anot[row][pos] = new jalview.datamodel.Annotation( (dc != null) ?
                dc : "", desc, (ss != null) ? ss.charAt(0) : ' ', val);
          }
          else
          {
            anot[row][pos] = new jalview.datamodel.Annotation( (dc != null) ?
                dc : "", desc, (ss != null) ? ss.charAt(0) : ' ', val, colour);
          }
        }
        else
        {
          Cache.log.warn("Ignoring out of bound annotation element " + aa +
                         " in " + annotation.getVorbaId().getId());
        }
      }
      // decide on how many annotation rows are needed.
      if (mergeable)
      {
        for (int i = 0; i < anot[0].length; i++)
        {
          if (anot[1][i] != null)
          {
            anot[0][i] = anot[1][i];
            anot[0][i].description = anot[0][i].description + " (after)";
            AeContent[HASDESCSTR] = true; // we have valid description string data
            anot[1][i] = null;
          }
        }
        anot[1] = null;
      }
      else
      {
        for (int i = 0; i < anot[0].length; i++)
        {
          anot[1][i].description = anot[1][i].description + " (after)";
        }
      }
      return new Object[]
          {
          AeContent, rangeMap, anot[0], anot[1]};
    }
    else
    {
      // no annotations to parse. Just return an empty annotationElement[] array.
      return new Object[]
          {
          AeContent, rangeMap, anot[0], anot[1]};
    }
    // return null;
  }

  /**
   * @param jal the jalview alignment to which the annotation will be attached (ideally - freshly updated from corresponding vamsas alignment)
   * @param annotation
   * @return unbound jalview alignment annotation object.
   */
  private jalview.datamodel.AlignmentAnnotation getjAlignmentAnnotation(jalview.
      datamodel.AlignmentI jal,
      org.vamsas.objects.core.RangeAnnotation annotation)
  {
    jalview.datamodel.AlignmentAnnotation jan = null;
    if (annotation == null)
    {
      return null;
    }
    // boolean hasSequenceRef=annotation.getClass().equals(org.vamsas.objects.core.AlignmentSequenceAnnotation.class);
    //boolean hasProvenance=hasSequenceRef || (annotation.getClass().equals(org.vamsas.objects.core.AlignmentAnnotation.class));
    /*int se[] = getBounds(annotation);
         if (se==null)
      se=new int[] {0,jal.getWidth()-1};
     */
    Object[] parsedRangeAnnotation = parseRangeAnnotation(annotation);
    String a_label = annotation.getLabel();
    String a_descr = annotation.getDescription();
    if (a_label == null || a_label.length() == 0)
    {
      a_label = annotation.getType();
      if (a_label.length() == 0)
      {
        a_label = "Unamed annotation";
      }
    }
    if (a_descr == null || a_descr.length() == 0)
    {
      a_descr = "Annotation of type '" + annotation.getType() + "'";
    }
    if (parsedRangeAnnotation == null)
    {
      Cache.log.debug(
          "Inserting empty annotation row elements for a whole-alignment annotation.");

    }
    else
    {
      if (parsedRangeAnnotation[3] != null)
      {
        Cache.log.warn("Ignoring 'After' annotation row in " +
                       annotation.getVorbaId());
      }
      jalview.datamodel.Annotation[] arow = (jalview.datamodel.Annotation[])
          parsedRangeAnnotation[2];
      boolean[] has = (boolean[]) parsedRangeAnnotation[0];
      // VAMSAS: getGraph is only on derived annotation for alignments - in this way its 'odd' - there is already an existing TODO about removing this flag as being redundant
      /*if ((annotation.getClass().equals(org.vamsas.objects.core.AlignmentAnnotation.class) && ((org.vamsas.objects.core.AlignmentAnnotation)annotation).getGraph())
          || (hasSequenceRef=true && ((org.vamsas.objects.core.AlignmentSequenceAnnotation)annotation).getGraph())) {
       */
      if (has[HASVALS])
      {
        // make bounds and automatic description strings for jalview user's benefit (these shouldn't be written back to vamsas document)
        boolean first = true;
        float min = 0, max = 1;
        int lastval = 0;
        for (int i = 0; i < arow.length; i++)
        {
          if (arow[i] != null)
          {
            if (i - lastval > 1)
            {
              // do some interpolation *between* points
              if (arow[lastval] != null)
              {
                float interval = arow[i].value - arow[lastval].value;
                interval /= i - lastval;
                float base = arow[lastval].value;
                for (int ip = lastval + 1, np = 0; ip < i; np++, ip++)
                {
                  arow[ip] = new jalview.datamodel.Annotation("", "", ' ',
                      interval * np + base);
                  // NB - Interpolated points don't get a tooltip and description.
                }
              }
            }
            lastval = i;
            // check range - shouldn't we have a min and max property in the annotation object ?
            if (first)
            {
              min = max = arow[i].value;
              first = false;
            }
            else
            {
              if (arow[i].value < min)
              {
                min = arow[i].value;
              }
              else if (arow[i].value > max)
              {
                max = arow[i].value;
              }
            }
            // make tooltip and display char value
            if (!has[HASDESCSTR])
            {
              arow[i].description = arow[i].value + "";
            }
            if (!has[HASDC])
            {
              arow[i].displayCharacter = arow[i].value + "";
            }
          }
        }
        int type = jalview.datamodel.AlignmentAnnotation.LINE_GRAPH;
        if (has[HASHPHOB])
        {
          type = jalview.datamodel.AlignmentAnnotation.BAR_GRAPH;
        }
        jan = new jalview.datamodel.AlignmentAnnotation(a_label, a_descr, arow,
            min, max, type);
      }
      else
      {
        jan = new jalview.datamodel.AlignmentAnnotation(a_label, a_descr, arow);
        jan.setThreshold(null);
      }
      if (annotation.getLinkCount() > 0)
      {
        Cache.log.warn("Ignoring " + annotation.getLinkCount() +
                       "links added to AlignmentAnnotation.");
      }
      if (annotation.getModifiable())
      {
        jan.editable = true;
      }

      if (annotation.getPropertyCount() > 0)
      {
        // look for special jalview properties
        org.vamsas.objects.core.Property[] props = annotation.getProperty();
        for (int p = 0; p < props.length; p++)
        {
          if (props[p].getName().equalsIgnoreCase("jalview:graphType"))
          {
            try
            {
              // probably a jalview annotation graph so recover the visualization hints.
              jan.graph = jalview.datamodel.AlignmentAnnotation.
                  getGraphValueFromString(props[p].getContent());
            }
            catch (Exception e)
            {
              Cache.log.debug(
                  "Invalid graph type value in jalview:graphType property.");
            }
            try
            {
              if (annotation.getGroup() != null &&
                  annotation.getGroup().length() > 0)
              {
                jan.graphGroup = Integer.parseInt(annotation.getGroup());
              }
            }
            catch (Exception e)
            {
              Cache.log.info("UNIMPLEMENTED : Couldn't parse non-integer group value for setting graphGroup correctly.");
            }
          }
        }
      }

      return jan;

    }

    return null;
  }

  private SequenceFeature getJalviewSeqFeature(RangeAnnotation dseta)
  {
    int[] se = getBounds(dseta);
    SequenceFeature sf = new jalview.datamodel.SequenceFeature(dseta.getType(),
        dseta.getDescription(), dseta.getStatus(), se[0], se[1], dseta
        .getGroup());
    if (dseta.getLinkCount() > 0)
    {
      Link[] links = dseta.getLink();
      for (int i = 0; i < links.length; i++)
      {
        sf.addLink(links[i].getContent() + "|" + links[i].getHref());
      }
    }
    return sf;
  }

  /**
   * get real bounds of a RangeType's specification. start and end are an
   * inclusive range within which all segments and positions lie.
   * TODO: refactor to vamsas utils
   * @param dseta
   * @return int[] { start, end}
   */
  private int[] getBounds(RangeType dseta)
  {
    if (dseta != null)
    {
      int[] se = null;
      if (dseta.getSegCount() > 0 && dseta.getPosCount() > 0)
      {
        throw new Error("Invalid vamsas RangeType - cannot resolve both lists of Pos and Seg from choice!");
      }
      if (dseta.getSegCount() > 0)
      {
        se = getSegRange(dseta.getSeg(0), true);
        for (int s = 1, sSize = dseta.getSegCount(); s < sSize; s++)
        {
          int nse[] = getSegRange(dseta.getSeg(s), true);
          if (se[0] > nse[0])
          {
            se[0] = nse[0];
          }
          if (se[1] < nse[1])
          {
            se[1] = nse[1];
          }
        }
      }
      if (dseta.getPosCount() > 0)
      {
        // could do a polarity for pos range too. and pass back indication of discontinuities.
        int pos = dseta.getPos(0).getI();
        se = new int[]
            {
            pos, pos};
        for (int p = 0, pSize = dseta.getPosCount(); p < pSize; p++)
        {
          pos = dseta.getPos(p).getI();
          if (se[0] > pos)
          {
            se[0] = pos;
          }
          if (se[1] < pos)
          {
            se[1] = pos;
          }
        }
      }
      return se;
    }
    return null;
  }

  /**
   * map from a rangeType's internal frame to the referenced object's coordinate frame.
   * @param dseta
   * @return int [] { ref(pos)...} for all pos in rangeType's frame.
   */
  private int[] getMapping(RangeType dseta)
  {
    Vector posList = new Vector();
    if (dseta != null)
    {
      int[] se = null;
      if (dseta.getSegCount() > 0 && dseta.getPosCount() > 0)
      {
        throw new Error("Invalid vamsas RangeType - cannot resolve both lists of Pos and Seg from choice!");
      }
      if (dseta.getSegCount() > 0)
      {
        for (int s = 0, sSize = dseta.getSegCount(); s < sSize; s++)
        {
          se = getSegRange(dseta.getSeg(s), false);
          int se_end = se[1 - se[2]] + (se[2] == 0 ? 1 : -1);
          for (int p = se[se[2]]; p != se_end; p += se[2] == 0 ? 1 : -1)
          {
            posList.add(new Integer(p));
          }
        }
      }
      else if (dseta.getPosCount() > 0)
      {
        int pos = dseta.getPos(0).getI();

        for (int p = 0, pSize = dseta.getPosCount(); p < pSize; p++)
        {
          pos = dseta.getPos(p).getI();
          posList.add(new Integer(pos));
        }
      }
    }
    if (posList != null && posList.size() > 0)
    {
      int[] range = new int[posList.size()];
      for (int i = 0; i < range.length; i++)
      {
        range[i] = ( (Integer) posList.elementAt(i)).intValue();
      }
      posList.clear();
      return range;
    }
    return null;
  }

  /* not needed now.
   * Provenance getVamsasProvenance(jalview.datamodel.Provenance jprov) {
    jalview.datamodel.ProvenanceEntry[] entries = null;
    // TODO: fix App and Action here.
    Provenance prov = new Provenance();
    org.exolab.castor.types.Date date = new org.exolab.castor.types.Date(
        new java.util.Date());
    Entry provEntry;

    if (jprov != null)
    {
      entries = jprov.getEntries();
      for (int i = 0; i < entries.length; i++)
      {
        provEntry = new Entry();
        try
        {
          date = new org.exolab.castor.types.Date(entries[i].getDate());
        } catch (Exception ex)
        {
          ex.printStackTrace();

          date = new org.exolab.castor.types.Date(entries[i].getDate());
        }
        provEntry.setDate(date);
        provEntry.setUser(entries[i].getUser());
        provEntry.setAction(entries[i].getAction());
        prov.addEntry(provEntry);
      }
    }
    else
    {
      provEntry = new Entry();
      provEntry.setDate(date);
      provEntry.setUser(System.getProperty("user.name")); // TODO: ext string
      provEntry.setApp("JVAPP"); // TODO: ext string
      provEntry.setAction(action);
      prov.addEntry(provEntry);
    }

    return prov;
     }
   */
  jalview.datamodel.Provenance getJalviewProvenance(Provenance prov)
  {
    // TODO: fix App and Action entries and check use of provenance in jalview.
    jalview.datamodel.Provenance jprov = new jalview.datamodel.Provenance();
    for (int i = 0; i < prov.getEntryCount(); i++)
    {
      jprov.addEntry(prov.getEntry(i).getUser(), prov.getEntry(i).getAction(),
                     prov.getEntry(i).getDate().toDate(),
                     prov.getEntry(i).getId());
    }

    return jprov;
  }

  /**
   *
   * @return default initial provenance list for a Jalview created vamsas
   *         object.
   */
  Provenance dummyProvenance()
  {
    return dummyProvenance(null);
  }

  Entry dummyPEntry(String action)
  {
    Entry entry = new Entry();
    entry.setApp(this.provEntry.getApp());
    if (action != null)
    {
      entry.setAction(action);
    }
    else
    {
      entry.setAction("created.");
    }
    entry.setDate(new org.exolab.castor.types.Date(new java.util.Date()));
    entry.setUser(this.provEntry.getUser());
    return entry;
  }

  Provenance dummyProvenance(String action)
  {
    Provenance prov = new Provenance();
    prov.addEntry(dummyPEntry(action));
    return prov;
  }

  void addProvenance(Provenance p, String action)
  {
    p.addEntry(dummyPEntry(action));
  }

}
