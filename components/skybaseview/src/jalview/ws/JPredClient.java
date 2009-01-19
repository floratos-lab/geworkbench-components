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
package jalview.ws;

import java.util.*;

import javax.swing.*;

import ext.vamsas.*;
import jalview.analysis.*;
import jalview.bin.*;
import jalview.datamodel.*;
import jalview.gui.*;

public class JPredClient
    extends WSClient
{
  /**
   * crate a new GUI JPred Job
   * @param sh ServiceHandle
   * @param title String
   * @param msa boolean - true - submit alignment as a sequence profile
   * @param alview AlignmentView
   * @param viewonly TODO
   */
  public JPredClient(ext.vamsas.ServiceHandle sh, String title, boolean msa,
                     AlignmentView alview, AlignFrame parentFrame,
                     boolean viewonly)
  {
    super();
    wsInfo = setWebService(sh);
    startJPredClient(title, msa, alview, parentFrame, viewonly);

  }

  /**
   * startJPredClient
   * TODO: refine submission to cope with local prediction of visible regions or multiple single sequence jobs
   * TODO: sequence representative support - could submit alignment of representatives as msa.
   * TODO:  msa hidden region prediction - submit each chunk for prediction. concatenate results of each.
   * TODO:  single seq prediction - submit each contig of each sequence for prediction (but must cope with flanking regions and short seqs)
   * @param title String
   * @param msa boolean
   * @param alview AlignmentView
   * @param viewonly if true then the prediction will be made just on the concatenated visible regions
   */
  private void startJPredClient(String title, boolean msa,
                                jalview.datamodel.AlignmentView alview,
                                AlignFrame parentFrame, boolean viewonly)
  {
    AlignmentView input = alview;
    if (wsInfo == null)
    {
      wsInfo = setWebService();
    }
    Jpred server = locateWebService();
    if (server == null)
    {
      Cache.log.warn("Couldn't find a Jpred webservice to invoke!");
      return;
    }
    SeqCigar[] msf = null;
    SequenceI seq = null;
    int[] delMap = null;
    // original JNetClient behaviour - submit full length of sequence or profile
    // and mask result.
    msf = input.getSequences();
    seq = msf[0].getSeq('-');

    if (viewonly)
    {
      int[] viscontigs = alview.getVisibleContigs();
      int spos = 0;
      int i = 0;
      if (viscontigs != null)
      {
        // Construct the delMap - mapping from the positions within the input to Jnet to the contigs in the original sequence

        delMap = new int[seq.getEnd() - seq.getStart() + 1];
        int gapMap[] = seq.gapMap();
        for (int contig = 0; contig < viscontigs.length; contig += 2)
        {

          while (spos < gapMap.length && gapMap[spos] < viscontigs[contig])
          {
            spos++;
          }
          while (spos < gapMap.length && gapMap[spos] <= viscontigs[contig + 1])
          {
            delMap[i++] = spos++;
          }
        }
        int tmap[] = new int[i];
        System.arraycopy(delMap, 0, tmap, 0, i);
        delMap = tmap;
      }
    }
    if (msa && msf.length > 1)
    {

      String altitle = "JNet prediction on " + (viewonly ? "visible " : "") +
          seq.getName() +
          " using alignment from " + title;

      SequenceI aln[] = new SequenceI[msf.length];
      for (int i = 0, j = msf.length; i < j; i++)
      {
        aln[i] = msf[i].getSeq('-');
      }

      Hashtable SequenceInfo = jalview.analysis.SeqsetUtils.uniquify(aln, true);
      if (viewonly)
      {
        // Remove hidden regions from sequence objects.
        String seqs[] = alview.getSequenceStrings('-');
        for (int i = 0, j = msf.length; i < j; i++)
        {
          aln[i].setSequence(seqs[i]);
        }
        seq.setSequence(seqs[0]);
      }
      wsInfo.setProgressText("Job details for " + (viewonly ? "visible " : "") +
                             "MSA based prediction (" +
                             title + ") on sequence :\n>" + seq.getName() +
                             "\n" +
                             AlignSeq.extractGaps("-. ",
                                                  seq.getSequenceAsString()) +
                             "\n");
      JPredThread jthread = new JPredThread(wsInfo, altitle, server,
                                            SequenceInfo, aln, delMap, alview,
                                            parentFrame, WsURL);
      wsInfo.setthisService(jthread);
      jthread.start();
    }
    else
    {
      if (!msa && msf.length > 1)
      {
        throw new Error("Implementation Error! Multiple single sequence prediction jobs are not yet supported.");
      }
      String altitle = "JNet prediction for " + (viewonly ? "visible " : "") +
          "sequence " + seq.getName() +
          " from " +
          title;
      String seqname = seq.getName();
      Hashtable SequenceInfo = jalview.analysis.SeqsetUtils.SeqCharacterHash(
          seq);
      if (viewonly)
      {
        // Remove hidden regions from input sequence
        String seqs[] = alview.getSequenceStrings('-');
        seq.setSequence(seqs[0]);
      }
      wsInfo.setProgressText("Job details for prediction on " +
                             (viewonly ? "visible " : "") + "sequence :\n>" +
                             seqname + "\n" +
                             AlignSeq.extractGaps("-. ",
                                                  seq.getSequenceAsString()) +
                             "\n");
      JPredThread jthread = new JPredThread(wsInfo, altitle, server, WsURL,
                                            SequenceInfo, seq, delMap, alview,
                                            parentFrame);
      wsInfo.setthisService(jthread);
      jthread.start();
    }
  }

  public JPredClient(ext.vamsas.ServiceHandle sh, String title, SequenceI seq,
                     AlignFrame parentFrame)
  {
    super();
    wsInfo = setWebService(sh);
    startJPredClient(title, seq, parentFrame);
  }

  public JPredClient(ext.vamsas.ServiceHandle sh, String title, SequenceI[] msa,
                     AlignFrame parentFrame)
  {
    wsInfo = setWebService(sh);
    startJPredClient(title, msa, parentFrame);
  }

  public JPredClient(String title, SequenceI[] msf)
  {
    startJPredClient(title, msf, null);
  }

  public JPredClient(String title, SequenceI seq)
  {
    startJPredClient(title, seq, null);
  }

  private void startJPredClient(String title, SequenceI[] msf,
                                AlignFrame parentFrame)
  {
    if (wsInfo == null)
    {
      wsInfo = setWebService();
    }

    SequenceI seq = msf[0];

    String altitle = "JNet prediction on " + seq.getName() +
        " using alignment from " + title;

    wsInfo.setProgressText("Job details for MSA based prediction (" +
                           title + ") on sequence :\n>" + seq.getName() + "\n" +
                           AlignSeq.extractGaps("-. ", seq.getSequenceAsString()) +
                           "\n");
    SequenceI aln[] = new SequenceI[msf.length];
    for (int i = 0, j = msf.length; i < j; i++)
    {
      aln[i] = new jalview.datamodel.Sequence(msf[i]);
    }

    Hashtable SequenceInfo = jalview.analysis.SeqsetUtils.uniquify(aln, true);

    Jpred server = locateWebService();
    if (server == null)
    {
      return;
    }

    JPredThread jthread = new JPredThread(wsInfo, altitle, server, SequenceInfo,
                                          aln, null, null, parentFrame, WsURL);
    wsInfo.setthisService(jthread);
    jthread.start();
  }

  public void startJPredClient(String title, SequenceI seq,
                               AlignFrame parentFrame)
  {
    if (wsInfo == null)
    {
      wsInfo = setWebService();
    }
    wsInfo.setProgressText("Job details for prediction on sequence :\n>" +
                           seq.getName() + "\n" +
                           AlignSeq.extractGaps("-. ", seq.getSequenceAsString()) +
                           "\n");
    String altitle = "JNet prediction for sequence " + seq.getName() + " from " +
        title;

    Hashtable SequenceInfo = jalview.analysis.SeqsetUtils.SeqCharacterHash(seq);

    Jpred server = locateWebService();
    if (server == null)
    {
      return;
    }

    JPredThread jthread = new JPredThread(wsInfo, altitle, server, WsURL,
                                          SequenceInfo, seq, null, null,
                                          parentFrame);
    wsInfo.setthisService(jthread);
    jthread.start();
  }

  private WebserviceInfo setWebService()
  {
    WebServiceName = "JNetWS";
    WebServiceJobTitle = "JNet secondary structure prediction";
    WebServiceReference =
        "\"Cuff J. A and Barton G.J (2000) Application of " +
        "multiple sequence alignment profiles to improve protein secondary structure prediction, " +
        "Proteins 40:502-511\".";
    WsURL = "http://www.compbio.dundee.ac.uk/JalviewWS/services/jpred";

    WebserviceInfo wsInfo = new WebserviceInfo(WebServiceJobTitle,
                                               WebServiceReference);

    return wsInfo;
  }

  private ext.vamsas.Jpred locateWebService()
  {
    ext.vamsas.JpredServiceLocator loc = new JpredServiceLocator(); // Default
    ext.vamsas.Jpred server = null;
    try
    {
      server = loc.getjpred(new java.net.URL(WsURL)); // JBPNote will be set from properties
      ( (JpredSoapBindingStub) server).setTimeout(60000); // one minute stub
      //((JpredSoapBindingStub)this.server)._setProperty(org.apache.axis.encoding.C, Boolean.TRUE);

    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(Desktop.desktop,
                                    "The Secondary Structure Prediction Service named " +
                                    WebServiceName + " at " + WsURL +
                                    " couldn't be located.",
                                    "Internal Jalview Error",
                                    JOptionPane.WARNING_MESSAGE);
      wsInfo.setProgressText("Serious! " + WebServiceName +
                             " Service location failed\nfor URL :" + WsURL +
                             "\n" +
                             ex.getMessage());
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_SERVERERROR);

    }

    return server;
  }
}
