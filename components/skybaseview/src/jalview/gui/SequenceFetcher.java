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
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import MCview.*;
import jalview.datamodel.*;
import jalview.datamodel.xdb.embl.*;
import java.io.File;
import jalview.io.*;
import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class SequenceFetcher
extends JPanel implements Runnable
{
  JInternalFrame frame;
  AlignFrame alignFrame;
  StringBuffer result;
  final String noDbSelected = "-- Select Database --";
  public SequenceFetcher(AlignFrame af)
  {
    alignFrame = af;
    database.addItem(noDbSelected);
    database.addItem("Uniprot");
    database.addItem("EMBL");
    database.addItem("EMBLCDS");
    database.addItem("PDB");
    database.addItem("PFAM");

    try
    {
      jbInit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    frame = new JInternalFrame();
    frame.setContentPane(this);
    if (System.getProperty("os.name").startsWith("Mac"))
    {
      Desktop.addInternalFrame(frame, getFrameTitle(), 400, 140);
    }
    else
    {
      Desktop.addInternalFrame(frame, getFrameTitle(), 400, 125);
    }
  }

  private String getFrameTitle()
  {
    return ( (alignFrame == null) ? "New " : "Additional ") +
    "Sequence Fetcher";
  }

  private void jbInit()
  throws Exception
  {
    this.setLayout(borderLayout2);

    database.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    jLabel1.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setText(
    "Separate multiple accession ids with semi colon \";\"");
    ok.setText("OK");
    ok.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed();
      }
    });
    close.setText("Close");
    close.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        close_actionPerformed(e);
      }
    });
    textArea.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    textArea.setLineWrap(true);
    textArea.addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        if(e.getKeyCode()==KeyEvent.VK_ENTER)
          ok_actionPerformed();
      }
    });
    jPanel3.setLayout(borderLayout1);
    borderLayout1.setVgap(5);
    jPanel1.add(ok);
    jPanel1.add(close);
    jPanel3.add(jPanel2, java.awt.BorderLayout.WEST);
    jPanel2.add(database);
    jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);
    jPanel3.add(jLabel1, java.awt.BorderLayout.NORTH);
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);
    this.add(jPanel3, java.awt.BorderLayout.CENTER);
    jScrollPane1.getViewport().add(textArea);

  }

  JComboBox database = new JComboBox();
  JLabel jLabel1 = new JLabel();
  JButton ok = new JButton();
  JButton close = new JButton();
  JPanel jPanel1 = new JPanel();
  JTextArea textArea = new JTextArea();
  JScrollPane jScrollPane1 = new JScrollPane();
  JPanel jPanel2 = new JPanel();
  JPanel jPanel3 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  public void close_actionPerformed(ActionEvent e)
  {
    try
    {
      frame.setClosed(true);
    }
    catch (Exception ex)
    {}
  }

  public void ok_actionPerformed()
  {
    database.setEnabled(false);
    textArea.setEnabled(false);
    ok.setEnabled(false);
    close.setEnabled(false);

    Thread worker = new Thread(this);
    worker.start();
  }

  private void resetDialog()
  {
    database.setEnabled(true);
    textArea.setEnabled(true);
    ok.setEnabled(true);
    close.setEnabled(true);
  }

  public void run()
  {
    String error = "";
    if (database.getSelectedItem().equals(noDbSelected))
    {
      error += "Please select the source database\n";
    }
    com.stevesoft.pat.Regex empty = new com.stevesoft.pat.Regex("\\s+", "");
    textArea.setText(empty.replaceAll(textArea.getText()));
    if (textArea.getText().length() == 0)
    {
      error += "Please enter a (semi-colon separated list of) database id(s)";
    }
    if (error.length() > 0)
    {
      showErrorMessage(error);
      resetDialog();
      return;
    }

    result = new StringBuffer();
    if (database.getSelectedItem().equals("Uniprot"))
    {
      getUniprotFile(textArea.getText());
    }
    else if (database.getSelectedItem().equals("EMBL")
        || database.getSelectedItem().equals("EMBLCDS"))
    {
      String DBRefSource = database.getSelectedItem().equals("EMBLCDS")
      ? jalview.datamodel.DBRefSource.EMBLCDS
          : jalview.datamodel.DBRefSource.EMBL;

      StringTokenizer st = new StringTokenizer(textArea.getText(), ";");
      SequenceI[] seqs = null;
      while(st.hasMoreTokens())
      {
        EBIFetchClient dbFetch = new EBIFetchClient();

        File reply = dbFetch.fetchDataAsFile(
            database.getSelectedItem().toString().toLowerCase(
            ) + ":" + st.nextToken(),
            "emblxml",null);

        jalview.datamodel.xdb.embl.EmblFile efile=null;
        if (reply != null && reply.exists())
        {
          efile = jalview.datamodel.xdb.embl.EmblFile.getEmblFile(reply);
        }
        if (efile!=null) {
          for (Iterator i=efile.getEntries().iterator(); i.hasNext(); ) {
            EmblEntry entry = (EmblEntry) i.next();
            SequenceI[] seqparts = entry.getSequences(false,true, DBRefSource);
            if (seqparts!=null) {
              SequenceI[] newseqs = null;
              int si=0;
              if (seqs==null) {
                newseqs = new SequenceI[seqparts.length];
              } else {
                newseqs  = new SequenceI[seqs.length+seqparts.length];

                for (;si<seqs.length; si++) {
                  newseqs[si] = seqs[si];
                  seqs[si] = null;
                }
              }
              for (int j=0;j<seqparts.length; si++, j++) {
                newseqs[si] = seqparts[j].deriveSequence(); // place DBReferences on dataset and refer
              }
              seqs=newseqs;

            }
          }
        } else {
          result=null;
        }
      }
      if (seqs!=null && seqs.length>0) {
        if (parseResult(new Alignment(seqs), null, null)!=null)
          result.append("# Successfully parsed the "+database.getSelectedItem()+" Queries into an Alignment");
      }
    }
    else if (database.getSelectedItem().equals("PDB"))
    {
      StringTokenizer qset = new StringTokenizer(textArea.getText(), ";");
      String query;
      SequenceI[] seqs = null;
      while (qset.hasMoreTokens() && ((query = qset.nextToken())!=null))
      {
        SequenceI[] seqparts = getPDBFile(query.toUpperCase());
        if (seqparts != null)
        {
          if (seqs == null)
          {
            seqs = seqparts;
          }
          else
          {
            SequenceI[] newseqs = new SequenceI[seqs.length+seqparts.length];
            int i=0;
            for (; i < seqs.length; i++)
            {
              newseqs[i] = seqs[i];
              seqs[i] = null;
            }
            for (int j=0;j<seqparts.length; i++, j++)
            {
              newseqs[i] = seqparts[j];
            }
            seqs=newseqs;
          }
          result.append("# Success for "+query.toUpperCase()+"\n");
        }
      }
      if (seqs != null && seqs.length > 0)
      {
        if (parseResult(new Alignment(seqs), null, null)!=null)
        {
          result.append(
          "# Successfully parsed the PDB File Queries into an Alignment");
        }
      }
    }
    else if( database.getSelectedItem().equals("PFAM"))
    {
      try
      {
        result.append(new FastaFile(
            "http://www.sanger.ac.uk/cgi-bin/Pfam/getalignment.pl?format=fal&acc="
            +  textArea.getText().toUpperCase(), "URL").print()
        );

        if(result.length()>0)
        {
          parseResult( result.toString(), textArea.getText().toUpperCase() );
        }

      }
      catch (java.io.IOException ex)
      {
        result = null;
      }
    }

    if (result == null || result.length() == 0)
    {
      showErrorMessage("Error retrieving " + textArea.getText()
          + " from " + database.getSelectedItem());
    }

    resetDialog();
    return;
  }

  void getUniprotFile(String id)
  {
    EBIFetchClient ebi = new EBIFetchClient();
    File file = ebi.fetchDataAsFile("uniprot:" + id, "xml", null);

    DBRefFetcher dbref = new DBRefFetcher();
    Vector entries = dbref.getUniprotEntries(file);

    if (entries != null)
    {
      //First, make the new sequences
      Enumeration en = entries.elements();
      while (en.hasMoreElements())
      {
        UniprotEntry entry = (UniprotEntry) en.nextElement();

        StringBuffer name = new StringBuffer(">UniProt/Swiss-Prot");
        Enumeration en2 = entry.getAccession().elements();
        while (en2.hasMoreElements())
        {
          name.append("|");
          name.append(en2.nextElement());
        }
        en2 = entry.getName().elements();
        while (en2.hasMoreElements())
        {
          name.append("|");
          name.append(en2.nextElement());
        }

        if (entry.getProtein() != null)
        {
          name.append(" " + entry.getProtein().getName().elementAt(0));
        }

        result.append(name + "\n" + entry.getUniprotSequence().getContent() +
        "\n");

      }

      //Then read in the features and apply them to the dataset
      Alignment al = parseResult(result.toString(), null);
      for (int i = 0; i < entries.size(); i++)
      {
        UniprotEntry entry = (UniprotEntry) entries.elementAt(i);
        Enumeration e = entry.getDbReference().elements();
        Vector onlyPdbEntries = new Vector();
        while (e.hasMoreElements())
        {
          PDBEntry pdb = (PDBEntry) e.nextElement();
          if (!pdb.getType().equals("PDB"))
          {
            continue;
          }

          onlyPdbEntries.addElement(pdb);
        }

        Enumeration en2 = entry.getAccession().elements();
        while (en2.hasMoreElements())
        {
          al.getSequenceAt(i).getDatasetSequence().addDBRef(new DBRefEntry(
              DBRefSource.UNIPROT,
              "0",
              en2.nextElement().toString()));
        }




        al.getSequenceAt(i).getDatasetSequence().setPDBId(onlyPdbEntries);
        if (entry.getFeature() != null)
        {
          e = entry.getFeature().elements();
          while (e.hasMoreElements())
          {
            SequenceFeature sf = (SequenceFeature) e.nextElement();
            sf.setFeatureGroup("Uniprot");
            al.getSequenceAt(i).getDatasetSequence().addSequenceFeature( sf );
          }
        }
      }
    }
  }

  SequenceI[] getPDBFile(String id)
  {
    Vector result = new Vector();
    String chain = null;
    if (id.indexOf(":") > -1)
    {
      chain = id.substring(id.indexOf(":") + 1);
      id = id.substring(0, id.indexOf(":"));
    }

    EBIFetchClient ebi = new EBIFetchClient();
    String file = ebi.fetchDataAsFile("pdb:" + id, "pdb", "raw").
    getAbsolutePath();
    if (file == null)
    {
      return null;
    }
    try
    {
      PDBfile pdbfile = new PDBfile(file, jalview.io.AppletFormatAdapter.FILE);
      for (int i = 0; i < pdbfile.chains.size(); i++)
      {
        if (chain == null ||
            ( (PDBChain) pdbfile.chains.elementAt(i)).id.
            toUpperCase().equals(chain))
        {
          PDBChain pdbchain = (PDBChain) pdbfile.chains.elementAt(i);
          // Get the Chain's Sequence - who's dataset includes any special features added from the PDB file
          SequenceI sq = pdbchain.sequence;
          // Specially formatted name for the PDB chain sequences retrieved from the PDB
          sq.setName("PDB|"+id+"|"+sq.getName());
          // Might need to add more metadata to the PDBEntry object
          // like below
          /*
           * PDBEntry entry = new PDBEntry();
            // Construct the PDBEntry
            entry.setId(id);
            if (entry.getProperty() == null)
                entry.setProperty(new Hashtable());
            entry.getProperty().put("chains",
                        pdbchain.id
                        + "=" + sq.getStart()
                        + "-" + sq.getEnd());
            sq.getDatasetSequence().addPDBId(entry);
           */
          // Add PDB DB Refs
          // We make a DBRefEtntry because we have obtained the PDB file from a verifiable source
          // JBPNote - PDB DBRefEntry should also carry the chain and mapping information
          DBRefEntry dbentry = new DBRefEntry(jalview.datamodel.DBRefSource.PDB,
              "0", id + pdbchain.id);
          sq.addDBRef(dbentry);
          // and add seuqence to the retrieved set
          result.addElement(sq.deriveSequence());
        }
      }

      if (result.size() < 1)
      {
        throw new Exception("WsDBFetch for PDB id resulted in zero result size");
      }
    }
    catch (Exception ex) // Problem parsing PDB file
    {
      jalview.bin.Cache.log.warn("Exception when retrieving " +
          textArea.getText() + " from " +
          database.getSelectedItem(), ex);
      return null;
    }


    SequenceI[] results = new SequenceI[result.size()];
    for (int i = 0, j = result.size(); i < j; i++)
    {
      results[i] = (SequenceI) result.elementAt(i);
      result.setElementAt(null,i);
    }
    return results;
  }
  Alignment parseResult(String result, String title)
  {
    String format = new IdentifyFile().Identify(result, "Paste");
    Alignment sequences = null;
    if (FormatAdapter.isValidFormat(format))
    {
      sequences = null;
      try
      {
        sequences = new FormatAdapter().readFile(result.toString(), "Paste",
            format);
      }
      catch (Exception ex)
      {}

      if (sequences!=null)
      {
        return parseResult(sequences, title, format);
      }
    }
    else
    {
      showErrorMessage("Error retrieving " + textArea.getText()
          + " from " + database.getSelectedItem());
    }

    return null;
  }

  Alignment parseResult(Alignment al, String title, String currentFileFormat)
  {

    if (al != null && al.getHeight() > 0)
    {
      if (alignFrame == null)
      {
        AlignFrame af = new AlignFrame(al,
            AlignFrame.DEFAULT_WIDTH,
            AlignFrame.DEFAULT_HEIGHT);
        if (currentFileFormat!=null)
        {
          af.currentFileFormat = currentFileFormat; // WHAT IS THE DEFAULT FORMAT FOR NON-FormatAdapter Sourced Alignments?
        }

        if(title==null)
        {
          title = "Retrieved from " + database.getSelectedItem();
        }

        Desktop.addInternalFrame(af,
            title,
            AlignFrame.DEFAULT_WIDTH,
            AlignFrame.DEFAULT_HEIGHT);

        af.statusBar.setText("Successfully pasted alignment file");

        try
        {
          af.setMaximum(jalview.bin.Cache.getDefault("SHOW_FULLSCREEN", false));
        }
        catch (Exception ex)
        {}
      }
      else
      {
        for (int i = 0; i < al.getHeight(); i++)
        {
          alignFrame.viewport.alignment.addSequence(al.getSequenceAt(i)); // this also creates dataset sequence entries
        }
        alignFrame.viewport.setEndSeq(alignFrame.viewport.alignment.
            getHeight());
        alignFrame.viewport.alignment.getWidth();
        alignFrame.viewport.firePropertyChange("alignment", null,
            alignFrame.viewport.
            getAlignment().getSequences());
      }
    }
    return al;
  }

  void showErrorMessage(final String error)
  {
    resetDialog();
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        JOptionPane.showInternalMessageDialog(Desktop.desktop,
            error, "Error Retrieving Data",
            JOptionPane.WARNING_MESSAGE);
      }
    });
  }
}

