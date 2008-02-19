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

import javax.swing.*;

import jalview.bin.*;
import jalview.datamodel.*;
import jalview.gui.*;

public abstract class WSThread
    extends Thread
{
  /**
   * Generic properties for Web Service Client threads.
   */
  AlignFrame alignFrame = null;
  WebserviceInfo wsInfo = null;
  AlignmentView input = null;
  boolean jobComplete = false;
  abstract class WSJob
  {
    /**
     * Generic properties for an individual job within a Web Service Client thread
     */
    int jobnum = 0; // WebServiceInfo pane for this job
    String jobId; // ws job ticket
    boolean cancelled = false;
    int allowedServerExceptions = 3; // job dies if too many exceptions.
    boolean submitted = false;
    boolean subjobComplete = false;
    /**
     *
     * @return true if job has completed and valid results are available
     */
    abstract boolean hasResults();

    /**
     *
     * @return boolean true if job can be submitted.
     */
    abstract boolean hasValidInput();

    vamsas.objects.simple.Result result;
  }

  class JobStateSummary
  {
    int running = 0;
    int queuing = 0;
    int finished = 0;
    int error = 0;
    int serror = 0;
    int cancelled = 0;
    int results = 0;
    void updateJobPanelState(WebserviceInfo wsInfo, String OutputHeader,
                             WSJob j)
    {
      if (j.result != null)
      {
        String progheader = "";
        // Parse state of job[j]
        if (j.result.isRunning())
        {
          running++;
          wsInfo.setStatus(j.jobnum, WebserviceInfo.STATE_RUNNING);
        }
        else if (j.result.isQueued())
        {
          queuing++;
          wsInfo.setStatus(j.jobnum, WebserviceInfo.STATE_QUEUING);
        }
        else if (j.result.isFinished())
        {
          finished++;
          j.subjobComplete = true;
          if (j.hasResults())
          {
            results++;
          }
          wsInfo.setStatus(j.jobnum, WebserviceInfo.STATE_STOPPED_OK);
        }
        else if (j.result.isFailed())
        {
          progheader += "Job failed.\n";
          j.subjobComplete = true;
          wsInfo.setStatus(j.jobnum, WebserviceInfo.STATE_STOPPED_ERROR);
          error++;
        }
        else if (j.result.isServerError())
        {
          serror++;
          j.subjobComplete = true;
          wsInfo.setStatus(j.jobnum,
                           WebserviceInfo.STATE_STOPPED_SERVERERROR);
        }
        else if (j.result.isBroken() || j.result.isFailed())
        {
          error++;
          j.subjobComplete = true;
          wsInfo.setStatus(j.jobnum, WebserviceInfo.STATE_STOPPED_ERROR);
        }
        // and pass on any sub-job messages to the user
        wsInfo.setProgressText(j.jobnum, OutputHeader);
        wsInfo.appendProgressText(j.jobnum, progheader);
        if (j.result.getStatus() != null)
        {
          wsInfo.appendProgressText(j.jobnum, j.result.getStatus());
        }
      }
      else
      {
        if (j.submitted && j.subjobComplete)
        {
          if (j.allowedServerExceptions == 0)
          {
            serror++;
          }
          else if (j.result == null)
          {
            error++;
          }
        }
      }
    }
  }

  WSJob jobs[] = null;
  String WebServiceName = null;
  String OutputHeader;
  String WsUrl = null;
  abstract void pollJob(WSJob job)
      throws Exception;

  public void run()
  {
    JobStateSummary jstate = null;
    if (jobs == null)
    {
      jobComplete = true;
    }
    while (!jobComplete)
    {
      jstate = new JobStateSummary();
      for (int j = 0; j < jobs.length; j++)
      {

        if (!jobs[j].submitted && jobs[j].hasValidInput())
        {
          StartJob(jobs[j]);
        }

        if (jobs[j].submitted && !jobs[j].subjobComplete)
        {
          try
          {
            pollJob(jobs[j]);
            if (jobs[j].result == null)
            {
              throw (new Exception(
                  "Timed out when communicating with server\nTry again later.\n"));
            }
            jalview.bin.Cache.log.debug("Job " + j + " Result state " +
                                        jobs[j].result.getState()
                                        + "(ServerError=" +
                                        jobs[j].result.isServerError() + ")");
          }
          catch (Exception ex)
          {
            // Deal with Transaction exceptions
            wsInfo.appendProgressText(jobs[j].jobnum, "\n" + WebServiceName
                                      + " Server exception!\n" + ex.getMessage());
            Cache.log.warn(WebServiceName + " job(" + jobs[j].jobnum
                           + ") Server exception: " + ex.getMessage());

            if (jobs[j].allowedServerExceptions > 0)
            {
              jobs[j].allowedServerExceptions--;
              Cache.log.debug("Sleeping after a server exception.");
              try
              {
                Thread.sleep(5000);
              }
              catch (InterruptedException ex1)
              {
              }
            }
            else
            {
              Cache.log.warn("Dropping job " + j + " " + jobs[j].jobId);
              jobs[j].subjobComplete = true;
              wsInfo.setStatus(jobs[j].jobnum,
                               WebserviceInfo.STATE_STOPPED_SERVERERROR);
            }
          }
          catch (OutOfMemoryError er)
          {
            jobComplete = true;
            jobs[j].subjobComplete = true;
            jobs[j].result = null; // may contain out of date result object
            wsInfo.setStatus(jobs[j].jobnum,
                             WebserviceInfo.STATE_STOPPED_ERROR);
            JOptionPane
                .showInternalMessageDialog(
                    Desktop.desktop,
                    "Out of memory handling result for job !!"
                    +
                    "\nSee help files for increasing Java Virtual Machine memory.",
                    "Out of memory", JOptionPane.WARNING_MESSAGE);
            Cache.log.error("Out of memory when retrieving Job " + j + " id:" +
                            WsUrl + "/" + jobs[j].jobId, er);
            System.gc();
          }
        }
        jstate.updateJobPanelState(wsInfo, OutputHeader, jobs[j]);
      }
      // Decide on overall state based on collected jobs[] states
      if (jstate.running > 0)
      {
        wsInfo.setStatus(WebserviceInfo.STATE_RUNNING);
      }
      else if (jstate.queuing > 0)
      {
        wsInfo.setStatus(WebserviceInfo.STATE_QUEUING);
      }
      else
      {
        jobComplete = true;
        if (jstate.finished > 0)
        {
          wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_OK);
        }
        else if (jstate.error > 0)
        {
          wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_ERROR);
        }
        else if (jstate.serror > 0)
        {
          wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_SERVERERROR);
        }
      }
      if (!jobComplete)
      {
        try
        {
          Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
          Cache.log.debug("Interrupted sleep waiting for next job poll.", e);
        }
        // System.out.println("I'm alive "+alTitle);
      }
    }
    if (jobComplete && jobs != null)
    {
      parseResult(); // tidy up and make results available to user
    }
    else
    {
      Cache.log.debug("WebServiceJob poll loop finished with no jobs created.");
    }
  }

  abstract void StartJob(WSJob job);

  abstract void parseResult();
}
