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

import jalview.analysis.*;
import jalview.bin.*;
import jalview.datamodel.*;
import jalview.datamodel.Alignment;
import jalview.gui.*;
import vamsas.objects.simple.MsaResult;

/**
 * <p>
 * Title:
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 *
 * <p>
 * Company: Dundee University
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */
class MsaWSThread
    extends WSThread implements WSClientI
{
  boolean submitGaps = false; // pass sequences including gaps to alignment

  // service

  boolean preserveOrder = true; // and always store and recover sequence

  // order

  class MsaWSJob
      extends WSThread.WSJob
  {
    // hold special input for this
    vamsas.objects.simple.SequenceSet seqs = new vamsas.objects.simple.
        SequenceSet();

    /**
     * MsaWSJob
     *
     * @param jobNum
     *            int
     * @param jobId
     *            String
     */
    public MsaWSJob(int jobNum, SequenceI[] inSeqs)
    {
      this.jobnum = jobNum;
      if (!prepareInput(inSeqs, 2))
      {
        submitted = true;
        subjobComplete = true;
        result = new MsaResult();
        result.setFinished(true);
        result.setStatus("Job never ran - input returned to user.");
      }

    }

    Hashtable SeqNames = new Hashtable();
    Vector emptySeqs = new Vector();
    /**
     * prepare input sequences for MsaWS service
     * @param seqs jalview sequences to be prepared
     * @param minlen minimum number of residues required for this MsaWS service
     * @return true if seqs contains sequences to be submitted to service.
     */
    private boolean prepareInput(SequenceI[] seqs, int minlen)
    {
      int nseqs = 0;
      if (minlen < 0)
      {
        throw new Error("Implementation error: minlen must be zero or more.");
      }
      for (int i = 0; i < seqs.length; i++)
      {
        if (seqs[i].getEnd() - seqs[i].getStart() > minlen - 1)
        {
          nseqs++;
        }
      }
      boolean valid = nseqs > 1; // need at least two seqs
      vamsas.objects.simple.Sequence[] seqarray =
          (valid)
          ? new vamsas.objects.simple.Sequence[nseqs]
          : null;
      for (int i = 0, n = 0; i < seqs.length; i++)
      {

        String newname = jalview.analysis.SeqsetUtils.unique_name(i); // same
        // for
        // any
        // subjob
        SeqNames.put(newname, jalview.analysis.SeqsetUtils
                     .SeqCharacterHash(seqs[i]));
        if (valid && seqs[i].getEnd() - seqs[i].getStart() > minlen - 1)
        {
          seqarray[n] = new vamsas.objects.simple.Sequence();
          seqarray[n].setId(newname);
          seqarray[n++].setSeq( (submitGaps) ? seqs[i].getSequenceAsString()
                               : AlignSeq.extractGaps(
                                   jalview.util.Comparison.GapChars, seqs[i]
                                   .getSequenceAsString()));
        }
        else
        {
          String empty = null;
          if (seqs[i].getEnd() >= seqs[i].getStart())
          {
            empty = (submitGaps) ? seqs[i].getSequenceAsString()
                : AlignSeq.extractGaps(
                    jalview.util.Comparison.GapChars, seqs[i]
                    .getSequenceAsString());
          }
          emptySeqs.add(new String[]
                        {newname, empty});
        }
      }
      this.seqs = new vamsas.objects.simple.SequenceSet();
      this.seqs.setSeqs(seqarray);
      return valid;
    }

    /**
     *
     * @return true if getAlignment will return a valid alignment result.
     */
    public boolean hasResults()
    {
      if (subjobComplete && result != null && result.isFinished()
          && ( (MsaResult) result).getMsa() != null &&
          ( (MsaResult) result).getMsa().getSeqs() != null)
      {
        return true;
      }
      return false;
    }

    public Object[] getAlignment()
    {

      if (result != null && result.isFinished())
      {
        SequenceI[] alseqs = null;
        char alseq_gapchar = '-';
        int alseq_l = 0;
        if ( ( (MsaResult) result).getMsa() != null)
        {
          alseqs = getVamsasAlignment( ( (MsaResult) result).getMsa());
          alseq_gapchar = ( (MsaResult) result).getMsa().getGapchar().charAt(0);
          alseq_l = alseqs.length;
        }
        if (emptySeqs.size() > 0)
        {
          SequenceI[] t_alseqs = new SequenceI[alseq_l + emptySeqs.size()];
          // get width
          int i, w = 0;
          if (alseq_l > 0)
          {
            for (i = 0, w = alseqs[0].getLength(); i < alseq_l; i++)
            {
              if (w < alseqs[i].getLength())
              {
                w = alseqs[i].getLength();
              }
              t_alseqs[i] = alseqs[i];
              alseqs[i] = null;
            }
          }
          // check that aligned width is at least as wide as emptySeqs width.
          int ow = w, nw = w;
          for (i = 0, w = emptySeqs.size(); i < w; i++)
          {
            String[] es = (String[]) emptySeqs.get(i);
            if (es != null && es[1] != null)
            {
              int sw = es[1].length();
              if (nw < sw)
              {
                nw = sw;
              }
            }
          }
          // make a gapped string.
          StringBuffer insbuff = new StringBuffer(w);
          for (i = 0; i < nw; i++)
          {
            insbuff.append(alseq_gapchar);
          }
          if (ow < nw)
          {
            for (i = 0; i < alseq_l; i++)
            {
              int sw = t_alseqs[i].getLength();
              if (nw > sw)
              {
                // pad at end
                alseqs[i].setSequence(t_alseqs[i].getSequenceAsString() +
                                      insbuff.substring(0, sw - nw));
              }
            }
          }
          for (i = 0, w = emptySeqs.size(); i < w; i++)
          {
            String[] es = (String[]) emptySeqs.get(i);
            if (es[1] == null)
            {
              t_alseqs[i +
                  alseq_l] = new jalview.datamodel.Sequence(es[0],
                  insbuff.toString(), 1, 0);
            }
            else
            {
              if (es[1].length() < nw)
              {
                t_alseqs[i +
                    alseq_l] = new jalview.datamodel.Sequence(es[0],
                    es[1] + insbuff.substring(0, nw - es[1].length()), 1,
                    1 + es[1].length());
              }
              else
              {
                t_alseqs[i +
                    alseq_l] = new jalview.datamodel.Sequence(es[0], es[1]);
              }
            }
          }
          alseqs = t_alseqs;
        }
        AlignmentOrder msaorder = new AlignmentOrder(alseqs);
        // always recover the order - makes parseResult()'s life easier.
        jalview.analysis.AlignmentSorter.recoverOrder(alseqs);
        // account for any missing sequences
        jalview.analysis.SeqsetUtils.deuniquify(SeqNames, alseqs);
        return new Object[]
            {
            alseqs, msaorder};
      }
      return null;
    }

    /**
     * mark subjob as cancelled and set result object appropriatly
     */
    void cancel()
    {
      cancelled = true;
      subjobComplete = true;
      result = null;
    }

    /**
     *
     * @return boolean true if job can be submitted.
     */
    boolean hasValidInput()
    {
      if (seqs.getSeqs() != null)
      {
        return true;
      }
      return false;
    }
  }

  String alTitle; // name which will be used to form new alignment window.
  Alignment dataset; // dataset to which the new alignment will be

  // associated.

  ext.vamsas.MuscleWS server = null;
  /**
   * set basic options for this (group) of Msa jobs
   *
   * @param subgaps
   *            boolean
   * @param presorder
   *            boolean
   */
  MsaWSThread(ext.vamsas.MuscleWS server, String wsUrl,
              WebserviceInfo wsinfo, jalview.gui.AlignFrame alFrame,
              AlignmentView alview,
              String wsname, boolean subgaps, boolean presorder)
  {
    this.server = server;
    this.WsUrl = wsUrl;
    this.wsInfo = wsinfo;
    this.WebServiceName = wsname;
    this.input = alview;
    this.submitGaps = subgaps;
    this.preserveOrder = presorder;
    this.alignFrame = alFrame;
  }

  /**
   * create one or more Msa jobs to align visible seuqences in _msa
   *
   * @param title
   *            String
   * @param _msa
   *            AlignmentView
   * @param subgaps
   *            boolean
   * @param presorder
   *            boolean
   * @param seqset
   *            Alignment
   */
  MsaWSThread(ext.vamsas.MuscleWS server, String wsUrl,
              WebserviceInfo wsinfo, jalview.gui.AlignFrame alFrame,
              String wsname, String title, AlignmentView _msa, boolean subgaps,
              boolean presorder, Alignment seqset)
  {
    this(server, wsUrl, wsinfo, alFrame, _msa, wsname, subgaps, presorder);
    OutputHeader = wsInfo.getProgressText();
    alTitle = title;
    dataset = seqset;

    SequenceI[][] conmsa = _msa.getVisibleContigs('-');
    if (conmsa != null)
    {
      int njobs = conmsa.length;
      jobs = new MsaWSJob[njobs];
      for (int j = 0; j < njobs; j++)
      {
        if (j != 0)
        {
          jobs[j] = new MsaWSJob(wsinfo.addJobPane(), conmsa[j]);
        }
        else
        {
          jobs[j] = new MsaWSJob(0, conmsa[j]);
        }
        if (njobs > 0)
        {
          wsinfo.setProgressName("region " + jobs[j].jobnum, jobs[j].jobnum);
        }
        wsinfo.setProgressText(jobs[j].jobnum, OutputHeader);
      }
    }
  }

  public boolean isCancellable()
  {
    return true;
  }

  public void cancelJob()
  {
    if (!jobComplete && jobs != null)
    {
      boolean cancelled = true;
      for (int job = 0; job < jobs.length; job++)
      {
        if (jobs[job].submitted && !jobs[job].subjobComplete)
        {
          String cancelledMessage = "";
          try
          {
            vamsas.objects.simple.WsJobId cancelledJob = server
                .cancel(jobs[job].jobId);
            if (cancelledJob.getStatus() == 2)
            {
              // CANCELLED_JOB
              cancelledMessage = "Job cancelled.";
              ( (MsaWSJob) jobs[job]).cancel();
              wsInfo.setStatus(jobs[job].jobnum,
                               WebserviceInfo.STATE_CANCELLED_OK);
            }
            else if (cancelledJob.getStatus() == 3)
            {
              // VALID UNSTOPPABLE JOB
              cancelledMessage +=
                  "Server cannot cancel this job. just close the window.\n";
              cancelled = false;
              // wsInfo.setStatus(jobs[job].jobnum,
              //                 WebserviceInfo.STATE_RUNNING);
            }

            if (cancelledJob.getJobId() != null)
            {
              cancelledMessage += ("[" + cancelledJob.getJobId() + "]");
            }

            cancelledMessage += "\n";
          }
          catch (Exception exc)
          {
            cancelledMessage +=
                ("\nProblems cancelling the job : Exception received...\n"
                 + exc + "\n");
            Cache.log.warn("Exception whilst cancelling " + jobs[job].jobId,
                           exc);
          }
          wsInfo.setProgressText(jobs[job].jobnum, OutputHeader
                                 + cancelledMessage + "\n");
        }
      }
      if (cancelled)
      {
        wsInfo.setStatus(WebserviceInfo.STATE_CANCELLED_OK);
        jobComplete = true;
      }
      this.interrupt(); // kick thread to update job states.
    }
    else
    {
      if (!jobComplete)
      {
        wsInfo
            .setProgressText(OutputHeader
                             + "Server cannot cancel this job because it has not been submitted properly. just close the window.\n");
      }
    }
  }

  void pollJob(WSJob job)
      throws Exception
  {
    ( (MsaWSJob) job).result = server.getResult( ( (MsaWSJob) job).jobId);
  }

  void StartJob(WSJob job)
  {
    if (! (job instanceof MsaWSJob))
    {
      throw new Error("StartJob(MsaWSJob) called on a WSJobInstance " +
                      job.getClass());
    }
    MsaWSJob j = (MsaWSJob) job;
    if (j.submitted)
    {
      if (Cache.log.isDebugEnabled())
      {
        Cache.log.debug("Tried to submit an already submitted job " + j.jobId);
      }
      return;
    }
    if (j.seqs.getSeqs() == null)
    {
      // special case - selection consisted entirely of empty sequences...
      j.submitted = true;
      j.result = new MsaResult();
      j.result.setFinished(true);
      j.result.setStatus("Empty Alignment Job");
      ( (MsaResult) j.result).setMsa(null);
    }
    try
    {
      vamsas.objects.simple.WsJobId jobsubmit = server.align(j.seqs);

      if ( (jobsubmit != null) && (jobsubmit.getStatus() == 1))
      {
        j.jobId = jobsubmit.getJobId();
        j.submitted = true;
        j.subjobComplete = false;
        // System.out.println(WsURL + " Job Id '" + jobId + "'");
      }
      else
      {
        if (jobsubmit == null)
        {
          throw new Exception(
              "Server at "
              + WsUrl
              +
              " returned null object, it probably cannot be contacted. Try again later ?");
        }

        throw new Exception(jobsubmit.getJobId());
      }
    }
    catch (Exception e)
    {
      // TODO: JBPNote catch timeout or other fault types explicitly
      // For unexpected errors
      System.err
          .println(WebServiceName
                   + "Client: Failed to submit the sequences for alignment (probably a server side problem)\n"
                   + "When contacting Server:" + WsUrl + "\n"
                   + e.toString() + "\n");
      j.allowedServerExceptions = 0;
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_SERVERERROR);
      wsInfo.setStatus(j.jobnum, WebserviceInfo.STATE_STOPPED_SERVERERROR);
      wsInfo
          .appendProgressText(
              j.jobnum,
              "Failed to submit sequences for alignment.\n"
              + "It is most likely that there is a problem with the server.\n"
              + "Just close the window\n");

      // e.printStackTrace(); // TODO: JBPNote DEBUG
    }
  }

  private jalview.datamodel.Sequence[] getVamsasAlignment(
      vamsas.objects.simple.Alignment valign)
  {
    vamsas.objects.simple.Sequence[] seqs = valign.getSeqs().getSeqs();
    jalview.datamodel.Sequence[] msa = new jalview.datamodel.Sequence[seqs.
        length];

    for (int i = 0, j = seqs.length; i < j; i++)
    {
      msa[i] = new jalview.datamodel.Sequence(seqs[i].getId(), seqs[i]
                                              .getSeq());
    }

    return msa;
  }

  void parseResult()
  {
    int results = 0; // number of result sets received
    JobStateSummary finalState = new JobStateSummary();
    try
    {
      for (int j = 0; j < jobs.length; j++)
      {
        finalState.updateJobPanelState(wsInfo, OutputHeader, jobs[j]);
        if (jobs[j].submitted && jobs[j].subjobComplete && jobs[j].hasResults())
        {
          results++;
          vamsas.objects.simple.Alignment valign = ( (MsaResult) jobs[j].result).
              getMsa();
          if (valign != null)
          {
            wsInfo.appendProgressText(jobs[j].jobnum,
                                      "\nAlignment Object Method Notes\n");
            String[] lines = valign.getMethod();
            for (int line = 0; line < lines.length; line++)
            {
              wsInfo.appendProgressText(jobs[j].jobnum, lines[line] + "\n");
            }
            // JBPNote The returned files from a webservice could be
            //  hidden behind icons in the monitor window that,
            // when clicked, pop up their corresponding data
          }
        }
      }
    }
    catch (Exception ex)
    {

      Cache.log.error("Unexpected exception when processing results for " +
                      alTitle, ex);
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_ERROR);
    }
    if (results > 0)
    {
      wsInfo.showResultsNewFrame
          .addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(
            java.awt.event.ActionEvent evt)
        {
          displayResults(true);
        }
      });
      wsInfo.mergeResults
          .addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(
            java.awt.event.ActionEvent evt)
        {
          displayResults(false);
        }
      });
      wsInfo.setResultsReady();
    }
    else
    {
      wsInfo.setFinishedNoResults();
    }
  }

  void displayResults(boolean newFrame)
  {
    // view input or result data for each block
    Vector alorders = new Vector();
    SequenceI[][] results = new SequenceI[jobs.length][];
    AlignmentOrder[] orders = new AlignmentOrder[jobs.length];

    for (int j = 0; j < jobs.length; j++)
    {
      if (jobs[j].hasResults())
      {
        Object[] res = ( (MsaWSJob) jobs[j]).getAlignment();
        alorders.add(res[1]);
        results[j] = (SequenceI[]) res[0];
        orders[j] = (AlignmentOrder) res[1];
//    SequenceI[] alignment = input.getUpdated
      }
      else
      {
        results[j] = null;
      }
    }
    Object[] newview = input.getUpdatedView(results, orders, '-');
    // trash references to original result data
    for (int j = 0; j < jobs.length; j++)
    {
      results[j] = null;
      orders[j] = null;
    }
    SequenceI[] alignment = (SequenceI[]) newview[0];
    ColumnSelection columnselection = (ColumnSelection) newview[1];
    Alignment al = new Alignment(alignment);
    if (dataset != null)
    {
      al.setDataset(dataset);
    }

    // JBNote- TODO: warn user if a block is input rather than aligned data ?

    if (newFrame)
    {
      AlignFrame af = new AlignFrame(al, columnselection,
                                     AlignFrame.DEFAULT_WIDTH,
                                     AlignFrame.DEFAULT_HEIGHT);

      // >>>This is a fix for the moment, until a better solution is
      // found!!<<<
      af.getFeatureRenderer().transferSettings(
          alignFrame.getFeatureRenderer());
      // update orders
      if (alorders.size() > 0)
      {
        if (alorders.size() == 1)
        {
          af.addSortByOrderMenuItem(WebServiceName + " Ordering",
                                    (AlignmentOrder) alorders.get(0));
        }
        else
        {
          // construct a non-redundant ordering set
          Vector names = new Vector();
          for (int i = 0, l = alorders.size(); i < l; i++)
          {
            String orderName = new String(" Region " + i);
            int j = i + 1;

            while (j < l)
            {
              if ( ( (AlignmentOrder) alorders.get(i)).equals( ( (
                  AlignmentOrder) alorders.get(j))))
              {
                alorders.remove(j);
                l--;
                orderName += "," + j;
              }
              else
              {
                j++;
              }
            }

            if (i == 0 && j == 1)
            {
              names.add(new String(""));
            }
            else
            {
              names.add(orderName);
            }
          }
          for (int i = 0, l = alorders.size(); i < l; i++)
          {
            af.addSortByOrderMenuItem(WebServiceName
                                      + ( (String) names.get(i)) +
                                      " Ordering",
                                      (AlignmentOrder) alorders.get(i));
          }
        }
      }

      Desktop.addInternalFrame(af, alTitle,
                               AlignFrame.DEFAULT_WIDTH,
                               AlignFrame.DEFAULT_HEIGHT);

    }
    else
    {
      System.out.println("MERGE WITH OLD FRAME");

    }
  }

  public boolean canMergeResults()
  {
    return false;
  }
}
