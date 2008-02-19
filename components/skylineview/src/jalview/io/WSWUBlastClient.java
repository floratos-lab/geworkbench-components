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

import java.util.*;

import javax.swing.*;

import jalview.analysis.*;
import jalview.datamodel.*;
import jalview.gui.*;
import uk.ac.ebi.www.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class WSWUBlastClient
{
  AlignmentPanel ap;
  AlignmentI al;
  CutAndPasteTransfer output = new CutAndPasteTransfer();
  int jobsRunning = 0;

  Vector suggestedIds = new Vector();
  /**
   * Creates a new WSWUBlastClient object.
   *
   * @param al DOCUMENT ME!
   * @param ids DOCUMENT ME!
   */
  public WSWUBlastClient(AlignmentPanel ap, AlignmentI al, ArrayList ids)
  {
    this.ap = ap;
    this.al = al;
    output.setText(
        "To display sequence features an exact Uniprot id with 100% sequence identity match must be entered."
        + "\nIn order to display these features, try changing the names of your sequences to the ids suggested below."
        + "\n\nRunning WSWUBlast at EBI."
        + "\nPlease quote Pillai S., Silventoinen V., Kallio K., Senger M., Sobhany S., Tate J., Velankar S., Golovin A., Henrick K., Rice P., Stoehr P., Lopez R."
        +
        "\nSOAP-based services provided by the European Bioinformatics Institute."
        + "\nNucleic Acids Res. 33(1):W25-W28 (2005));");

    Desktop.addInternalFrame(output,
                             "BLASTing for unidentified sequences ", 800, 300);

    for (int i = 0; i < ids.size(); i++)
    {
      Sequence sequence = (Sequence) ids.get(i);
      System.out.println(sequence.getName());

      BlastThread thread = new BlastThread(sequence);
      thread.start();
      jobsRunning++;
    }

    ImageTwirler thread = new ImageTwirler();
    thread.start();
  }

  /**
   * DOCUMENT ME!
   *
   * @param id1 DOCUMENT ME!
   * @param res DOCUMENT ME!
   */
  void parseResult(Sequence seq, String res)
  {
    StringTokenizer st = new StringTokenizer(res, "\n");
    String data;
    String id2;
    int maxFound = 90;
    StringBuffer buffer = new StringBuffer("\n\n" + seq.getName() + " :");

    while (st.hasMoreTokens())
    {
      data = st.nextToken();

      if (data.indexOf(">UNIPROT") > -1)
      {
        int index = data.indexOf(">UNIPROT") + 9;
        id2 = data.substring(index, data.indexOf(" ", index));

        boolean identitiesFound = false;
        while (!identitiesFound)
        {
          data = st.nextToken();

          if (data.indexOf("Identities") > -1)
          {
            identitiesFound = true;

            int value = Integer.parseInt(data.substring(data.indexOf(
                "(") + 1,
                data.indexOf("%")));

            if (value >= maxFound)
            {
              maxFound = value;
              buffer.append(" " + id2 + " " + value + "%; ");
              suggestedIds.addElement(new Object[]
                                      {seq, id2});
            }
          }
        }
      }
    }

    output.appendText(buffer.toString());
  }

  void updateIds()
  {
    // This must be outside the run() body as java 1.5
    // will not return any value from the OptionPane to the expired thread.
    int reply = JOptionPane.showConfirmDialog(
        Desktop.desktop, "Automatically update suggested ids?",
        "Auto replace sequence ids", JOptionPane.YES_NO_OPTION);

    if (reply == JOptionPane.YES_OPTION)
    {
      Enumeration keys = suggestedIds.elements();
      while (keys.hasMoreElements())
      {
        Object[] object = (Object[]) keys.nextElement();

        Sequence oldseq = (Sequence) object[0];

        oldseq.setName(object[1].toString());

        // Oldseq is actually in the dataset, we must find the
        // Visible seq and change its name also.
        for (int i = 0; i < al.getHeight(); i++)
        {
          if (al.getSequenceAt(i).getDatasetSequence() == oldseq)
          {
            al.getSequenceAt(i).setName(oldseq.getName());
            break;
          }
        }

        DBRefEntry[] entries = oldseq.getDBRef();
        if (entries != null)
        {
          oldseq.addDBRef(new jalview.datamodel.
                          DBRefEntry(jalview.datamodel.DBRefSource.UNIPROT,
                                     "0",
                                     entries[0].getAccessionId()));
        }
      }
    }
    ap.paintAlignment(true);

  }

  class ImageTwirler
      extends Thread
  {
    ImageIcon[] imageIcon;
    int imageIndex = 0;

    public ImageTwirler()
    {
      imageIcon = new ImageIcon[9];

      for (int i = 0; i < 9; i++)
      {
        java.net.URL url = getClass().getResource("/images/dna" +
                                                  (i + 1) + ".gif");

        if (url != null)
        {
          imageIcon[i] = new ImageIcon(url);
        }
      }
    }

    public void run()
    {
      while (jobsRunning > 0)
      {
        try
        {
          Thread.sleep(100);
          imageIndex++;
          imageIndex %= 9;
          output.setFrameIcon(imageIcon[imageIndex]);
          output.setTitle("BLASTing for unidentified sequences - " +
                          jobsRunning + " jobs running.");
        }
        catch (Exception ex)
        {
        }
      }

      if (jobsRunning == 0)
      {
        updateIds();
      }
    }
  }

  class BlastThread
      extends Thread
  {
    Sequence sequence;
    String jobid;
    boolean jobComplete = false;

    BlastThread(Sequence sequence)
    {
      System.out.println("blasting for: " + sequence.getName());
      this.sequence = sequence;
    }

    public void run()
    {
      StartJob();

      while (!jobComplete)
      {
        try
        {
          WSWUBlastService service = new WSWUBlastServiceLocator();
          WSWUBlast wublast = service.getWSWUBlast();
          WSFile[] results = wublast.getResults(jobid);

          if (results != null)
          {
            String result = new String(wublast.poll(jobid, "tooloutput"));
            parseResult(sequence, result);
            jobComplete = true;
            jobsRunning--;
          }
          else
          {
            Thread.sleep(10000);
            System.out.println("WSWuBlastClient: I'm alive " +
                               sequence.getName() + " " + jobid); // log.debug
          }
        }
        catch (Exception ex)
        {
        }
      }
    }

    void StartJob()
    {
      InputParams params = new InputParams();

      params.setProgram("blastp");
      params.setDatabase("uniprot");
      params.setMatrix("pam10");

      params.setNumal(5);
      params.setSensitivity("low");
      params.setSort("totalscore");
      params.setOutformat("txt");
      params.setAsync(true);

      try
      {
        Data inputs[] = new Data[1];
        Data input = new Data();
        input.setType("sequence");
        input.setContent(AlignSeq.extractGaps("-. ",
                                              sequence.getSequenceAsString()));
        inputs[0] = input;

        WSWUBlastService service = new WSWUBlastServiceLocator();
        WSWUBlast wublast = service.getWSWUBlast();
        jobid = wublast.runWUBlast(params, inputs);
      }
      catch (Exception exp)
      {
        jobComplete = true;
        jobsRunning--;
        System.err.println("WSWUBlastClient error:\n" + exp.toString());
        exp.printStackTrace();
      }
    }
  }
}
