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
package jalview.io;

import java.net.*;
import java.util.*;

import javax.swing.*;

import org.biojava.dasobert.das.*;
import org.biojava.dasobert.das2.*;
import org.biojava.dasobert.das2.io.*;
import org.biojava.dasobert.dasregistry.*;
import org.biojava.dasobert.eventmodel.*;
import jalview.bin.Cache;
import jalview.datamodel.*;
import jalview.gui.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class DasSequenceFeatureFetcher
{
  SequenceI[] sequences;
  AlignFrame af;
  FeatureSettings fsettings;
  StringBuffer sbuffer = new StringBuffer();
  Vector selectedSources;
  boolean cancelled = false;

  long startTime;

  /**
   * Creates a new SequenceFeatureFetcher object.
   * Uses default
   *
   * @param align DOCUMENT ME!
   * @param ap DOCUMENT ME!
   */
  public DasSequenceFeatureFetcher(SequenceI[] sequences,
                                   FeatureSettings fsettings,
                                   Vector selectedSources)
  {
    this.selectedSources = selectedSources;
    this.sequences = sequences;
    this.af = fsettings.af;
    this.fsettings = fsettings;

    int uniprotCount = 0;
    for (int i = 0; i < selectedSources.size(); i++)
    {
      DasSource source = (DasSource) selectedSources.elementAt(i);
      DasCoordinateSystem[] coords = source.getCoordinateSystem();
      for (int c = 0; c < coords.length; c++)
      {
        if (coords[c].getName().indexOf("UniProt") > -1)
        {
          uniprotCount++;
          break;
        }
      }
    }

    int refCount = 0;
    for (int i = 0; i < sequences.length; i++)
    {
      DBRefEntry[] dbref = sequences[i].getDBRef();
      if (dbref != null)
      {
        for (int j = 0; j < dbref.length; j++)
        {
          if (dbref[j].getSource()
              .equals(jalview.datamodel.DBRefSource.UNIPROT))
          {
            refCount++;
            break;
          }
        }
      }
    }

    if (refCount < sequences.length && uniprotCount > 0)
    {

      int reply = JOptionPane.showInternalConfirmDialog(Desktop.desktop,
          "Do you want Jalview to find\n"
          + "Uniprot Accession ids for given sequence names?",
          "Find Uniprot Accession Ids",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE);

      if (reply == JOptionPane.YES_OPTION)
      {
        Thread thread = new Thread(new FetchDBRefs());
        thread.start();
      }
      else
      {
        startFetching();
    }
    }
    else
    {
      startFetching();
    }

    }

  class FetchDBRefs
      implements Runnable
  {
    public void run()
    {
      new DBRefFetcher(sequences, af).fetchDBRefs(true);
      startFetching();
    }
  }


   /**
    * Spawns a number of dasobert Fetcher threads to add features to sequences in the dataset
    */
   void startFetching()
   {
     cancelled = false;
     startTime = System.currentTimeMillis();
     af.setProgressBar("Fetching DAS Sequence Features", startTime);

     DasSource[] sources = new jalview.gui.DasSourceBrowser().getDASSource();

     if (selectedSources == null || selectedSources.size() == 0)
     {
       String active = jalview.bin.Cache.getDefault("DAS_ACTIVE_SOURCE",
           "uniprot");
       StringTokenizer st = new StringTokenizer(active, "\t");
       Vector selectedSources = new Vector();
       String token;
       while (st.hasMoreTokens())
       {
         token = st.nextToken();
         for (int i = 0; i < sources.length; i++)
         {
           if (sources[i].getNickname().equals(token))
           {
             selectedSources.addElement(sources[i]);
             break;
           }
         }
       }
     }

     if (selectedSources == null || selectedSources.size() == 0)
     {
       System.out.println("No DAS Sources active");
       af.setProgressBar("No DAS Sources Active", startTime);
       cancelled = true;
       fsettings.noDasSourceActive();
       return;
     }

       sourcesRemaining = selectedSources.size();
       //Now sending requests one at a time to each server
       for (int sourceIndex = 0;
            sourceIndex < selectedSources.size()
            && !cancelled;
            sourceIndex++)
       {
         DasSource dasSource = (DasSource) selectedSources.elementAt(
             sourceIndex);

         nextSequence(dasSource, sequences[0]);
       }
   }

   public void cancel()
   {
     af.setProgressBar("DAS Feature Fetching Cancelled", startTime);
     cancelled = true;
   }

   int sourcesRemaining=0;
   void responseComplete(DasSource dasSource, SequenceI seq)
   {
     if (seq != null)
     {
       for (int seqIndex = 0;
            seqIndex < sequences.length-1
            && !cancelled; seqIndex++)
       {
         if (sequences[seqIndex] == seq)
         {
           nextSequence(dasSource, sequences[++seqIndex]);
           return;
         }
       }
     }

     sourcesRemaining --;

     if(sourcesRemaining==0)
     {
       af.setProgressBar("DAS Feature Fetching Complete", startTime);

       if(af.featureSettings!=null)
      {
         af.featureSettings.setTableData();
      }

       fsettings.complete();
     }

   }

   void featuresAdded(SequenceI seq)
   {
     af.getFeatureRenderer().featuresAdded();

     int start = af.getViewport().getStartSeq();
     int end = af.getViewport().getEndSeq();
     int index;
     for(index=start; index<end; index++)
       {
      if (seq ==
          af.getViewport().getAlignment().getSequenceAt(index).getDatasetSequence())
      {
        af.alignPanel.paintAlignment(true);
         break;
       }
   }
  }


  void nextSequence(DasSource dasSource, SequenceI seq)
  {
    if (cancelled)
      return;
    DBRefEntry[] uprefs = jalview.util.DBRefUtils.selectRefs(seq.getDBRef(),
          new String[]
          {
        //  jalview.datamodel.DBRefSource.PDB,
          jalview.datamodel.DBRefSource.UNIPROT,
        //  jalview.datamodel.DBRefSource.EMBL - not tested on any EMBL coord sys sources
      });
// TODO: minimal list of DAS queries to make by querying with untyped ID if distinct from any typed IDs

      boolean dasCoordSysFound = false;

      if (uprefs != null)
        {
          // do any of these ids match the source's coordinate system ?
          for (int j = 0; !dasCoordSysFound && j < uprefs.length; j++)
          {
            DasCoordinateSystem cs[] = dasSource.getCoordinateSystem();

            for(int csIndex=0; csIndex<cs.length && !dasCoordSysFound; csIndex++)
            {
              if (cs.length > 0 && jalview.util.DBRefUtils
                  .isDasCoordinateSystem(cs[csIndex].getName(), uprefs[j]))
              {
                Cache.log.debug("Launched fetcher for coordinate system " +
                                cs[0].getName());
                //  Will have to pass any mapping information to the fetcher
                //- the start/end for the DBRefEntry may not be the same as the sequence's start/end

                System.out.println(seq.getName() + " " + (seq.getDatasetSequence() == null)
                                   + " " + dasSource.getUrl());

                dasCoordSysFound = true; // break's out of the loop
                createFeatureFetcher(seq,
                                     dasSource,
                                     uprefs[j]);
              }
              else
                System.out.println("IGNORE " + cs[csIndex].getName());
            }
          }
        }

        if(!dasCoordSysFound)
        {
          String id = null;
          // try and use the name as the sequence id
          if (seq.getName().indexOf("|") > -1)
          {
            id = seq.getName().substring(
                seq.getName().lastIndexOf("|") + 1);
          }
          else
          {
            id = seq.getName();
          }
          if (id != null)
          {
            // Should try to call a general feature fetcher that
            // queries many sources with name to discover applicable ID references
            createFeatureFetcher(seq,
                                 dasSource,
                                 id);
          }
        }

   }


/**
 * fetch and add das features to a sequence using the given source URL and compatible DbRef id.
 * new features are mapped using the DbRef mapping to the local coordinate system.
 * @param seq
 * @param SourceUrl
 * @param dbref
 */
  protected void createFeatureFetcher(final SequenceI seq, final DasSource dasSource,
        final DBRefEntry dbref) {

    //////////////
    /// fetch DAS features
    final Das1Source source = new Das1Source();
    source.setUrl(dasSource.getUrl());
    source.setNickname(dasSource.getNickname());
    if (dbref==null || dbref.getAccessionId()==null || dbref.getAccessionId().length()<1)
    {
      return;
    }
    Cache.log.debug("new Das Feature Fetcher for " + dbref.getSource()+":"+dbref.getAccessionId() + " querying " +
                    dasSource.getUrl());
    FeatureThread fetcher = new FeatureThread(dbref.getAccessionId()
                                                //  +  ":" + start + "," + end,
                                                , source);

      fetcher.addFeatureListener(new FeatureListener()
      {
        public void comeBackLater(FeatureEvent e)
        {
          responseComplete(dasSource, seq);
          Cache.log.debug("das source " + e.getDasSource().getNickname() +
                          " asked us to come back in " + e.getComeBackLater() +
                          " secs.");
        }

        public void newFeatures(FeatureEvent e)
        {

          Das1Source ds = e.getDasSource();

          Map[] features = e.getFeatures();
          // add features to sequence
          Cache.log.debug("das source " + ds.getUrl() + " returned " +
                          features.length + " features");

          if (features.length > 0)
          {
            for (int i = 0; i < features.length; i++)
            {
              SequenceFeature f = newSequenceFeature(features[i],
                  source.getNickname());
              if (dbref.getMap()!=null && f.getBegin()>0 && f.getEnd()>0) {
                Cache.log.debug("mapping from "+f.getBegin()+" - "+f.getEnd());
                SequenceFeature vf[]=null;
                
                try {
                  vf = dbref.getMap().locateFeature(f);
                }
                catch (Exception ex)
                {
                  Cache.log.info("Error in 'experimental' mapping of features. Please try to reproduce and then report info to help@jalview.org.");
                  Cache.log.info("Mapping feature from "+f.getBegin()+" to "+f.getEnd()+" in dbref "+dbref.getAccessionId()+" in "+dbref.getSource());
                  Cache.log.info("using das Source "+ds.getUrl());
                  Cache.log.info(ex);
                }
                
                if (vf!=null) {
                  for (int v=0;v<vf.length;v++)
                  {
                    Cache.log.debug("mapping to "+v+": "+vf[v].getBegin()+" - "+vf[v].getEnd());
                    seq.addSequenceFeature(vf[v]);
                  }
                }
              } else {
                seq.addSequenceFeature(f);
              }
            }

            featuresAdded(seq);
          }
          else
          {
          //  System.out.println("No features found for " + seq.getName()
          //                     + " from: " + e.getDasSource().getNickname());
          }
          responseComplete(dasSource, seq);

        }
      }

      );

      fetcher.start();
    }
  protected void createFeatureFetcher(final SequenceI seq,
                                      final DasSource dasSource,
                                      String id)
  {
    //////////////
    /// fetch DAS features
    final Das1Source source = new Das1Source();
    source.setUrl(dasSource.getUrl());
    source.setNickname(dasSource.getNickname());

    Cache.log.debug("new Das Feature Fetcher for " + id + " querying " +
                    dasSource.getUrl());

    if (id != null && id.length() > 0)
    {
      FeatureThread fetcher = new FeatureThread(id
                                                //  +  ":" + start + "," + end,
                                                , source);

      fetcher.addFeatureListener(new FeatureListener()
      {
        public void comeBackLater(FeatureEvent e)
        {
          responseComplete(dasSource, seq);
          Cache.log.debug("das source " + e.getDasSource().getNickname() +
                          " asked us to come back in " + e.getComeBackLater() +
                          " secs.");
        }

        public void newFeatures(FeatureEvent e)
        {

          Das1Source ds = e.getDasSource();

          Map[] features = e.getFeatures();
          // add features to sequence
          Cache.log.debug("das source " + ds.getUrl() + " returned " +
                          features.length + " features");

          if (features.length > 0)
          {
            for (int i = 0; i < features.length; i++)
            {
              SequenceFeature f = newSequenceFeature(features[i],
                  source.getNickname());

              seq.addSequenceFeature(f);
            }

            featuresAdded(seq);
          }
          else
          {
          //  System.out.println("No features found for " + seq.getName()
          //                     + " from: " + e.getDasSource().getNickname());
          }
          responseComplete(dasSource, seq);

        }
      }

      );

      fetcher.start();
    }
  }

  /**
   * creates a jalview sequence feature from a das feature document
   * @param dasfeature
   * @return sequence feature object created using dasfeature information
   */
  SequenceFeature newSequenceFeature(Map dasfeature, String nickname)
  {
    if (dasfeature==null)
    {
      return null;
    }
    try
    {
      /**
       * Different qNames for a DAS Feature - are string keys to the HashMaps in features
       * "METHOD") ||
                  qName.equals("TYPE") ||
                  qName.equals("START") ||
                  qName.equals("END") ||
                  qName.equals("NOTE") ||
                  qName.equals("LINK") ||
                  qName.equals("SCORE")
       */
      String desc = new String();
      if (dasfeature.containsKey("NOTE"))
      {
        desc += (String) dasfeature.get("NOTE");
      }

      int start = 0, end = 0;
      float score = 0f;

      try
      {
        start = Integer.parseInt(dasfeature.get("START").toString());
      }
      catch (Exception ex)
      {}
      try
      {
        end = Integer.parseInt(dasfeature.get("END").toString());
      }
      catch (Exception ex)
      {}
      try
      {
        score = Integer.parseInt(dasfeature.get("SCORE").toString());
      }
      catch (Exception ex)
      {}

      SequenceFeature f = new SequenceFeature(
          (String) dasfeature.get("TYPE"),
          desc,
          start,
          end,
          score,
          nickname);

      if (dasfeature.containsKey("LINK"))
      {
        f.addLink(f.getType() + " " + f.begin + "_" + f.end
                  + "|" + dasfeature.get("LINK"));
      }

      return f;
    }
    catch (Exception e)
    {
      System.out.println("ERRR " + e);
      e.printStackTrace();
      System.out.println("############");
      Cache.log.debug("Failed to parse " + dasfeature.toString(), e);
      return null;
    }
  }

  public static DasSource[] getDASSources()
  {
    DasSourceReaderImpl reader = new DasSourceReaderImpl();

    String registryURL = jalview.bin.Cache.getDefault("DAS_REGISTRY_URL",
        DasSourceBrowser.DEFAULT_REGISTRY
        );

    try
    {
      URL url = new URL(registryURL);

      DasSource[] sources = reader.readDasSource(url);

      List das1sources = new ArrayList();
      for (int i = 0; i < sources.length; i++)
      {
        DasSource ds = sources[i];
        if (ds instanceof Das2Source)
        {
          Das2Source d2s = (Das2Source) ds;
          if (d2s.hasDas1Capabilities())
          {
            Das1Source d1s = DasSourceConverter.toDas1Source(d2s);
            das1sources.add(d1s);
          }

        }
        else if (ds instanceof Das1Source)
        {
          das1sources.add( (Das1Source) ds);
        }
      }

      return (Das1Source[]) das1sources.toArray(new Das1Source[das1sources.size()]);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }
  }

}

