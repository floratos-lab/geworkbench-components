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

//////////////////////////////////////////////////////////////////
package jalview.io;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class JalviewFileChooser
    extends JFileChooser
{

  public JalviewFileChooser(String dir)
  {
    super(dir);
    setAccessory(new RecentlyOpened());
  }

  public JalviewFileChooser(String dir,
                            String[] suffix,
                            String[] desc,
                            String selected,
                            boolean selectAll)
  {
    super(dir);
    init(suffix, desc, selected, selectAll);
  }

  public JalviewFileChooser(String dir,
                            String[] suffix,
                            String[] desc,
                            String selected)
  {
    super(dir);
    init(suffix, desc, selected, true);
  }

  void init(String[] suffix,
            String[] desc,
            String selected,
            boolean selectAll)
  {

    JalviewFileFilter chosen = null;

    //SelectAllFilter needs to be set first before adding further
    //file filters to fix bug on Mac OSX
    setAcceptAllFileFilterUsed(selectAll);

    for (int i = 0; i < suffix.length; i++)
    {
      JalviewFileFilter jvf = new JalviewFileFilter(suffix[i], desc[i]);
      addChoosableFileFilter(jvf);

      if ( (selected != null) && selected.equalsIgnoreCase(desc[i]))
      {
        chosen = jvf;
      }
    }

    if (chosen != null)
    {
      setFileFilter(chosen);
    }

    setAccessory(new RecentlyOpened());
  }

  public void setFileFilter(javax.swing.filechooser.FileFilter filter)
  {
    super.setFileFilter(filter);

    try
    {
      if (getUI() instanceof javax.swing.plaf.basic.BasicFileChooserUI)
      {
        final javax.swing.plaf.basic.BasicFileChooserUI ui = (javax.swing.plaf.
            basic.BasicFileChooserUI) getUI();
        final String name = ui.getFileName().trim();

        if ( (name == null) || (name.length() == 0))
        {
          return;
        }

        EventQueue.invokeLater(new Thread()
        {
          public void run()
          {
            String currentName = ui.getFileName();
            if ( (currentName == null) || (currentName.length() == 0))
            {
              ui.setFileName(name);
            }
          }
        });
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      // Some platforms do not have BasicFileChooserUI
    }
  }

  public String getSelectedFormat()
  {
    if (getFileFilter() == null)
    {
      return null;
    }

    String format = getFileFilter().getDescription();

    if (format.toUpperCase().startsWith("JALVIEW"))
    {
      format = "Jalview";
    }
    else if (format.toUpperCase().startsWith("FASTA"))
    {
      format = "FASTA";
    }
    else if (format.toUpperCase().startsWith("MSF"))
    {
      format = "MSF";
    }
    else if (format.toUpperCase().startsWith("CLUSTAL"))
    {
      format = "CLUSTAL";
    }
    else if (format.toUpperCase().startsWith("BLC"))
    {
      format = "BLC";
    }
    else if (format.toUpperCase().startsWith("PIR"))
    {
      format = "PIR";
    }
    else if (format.toUpperCase().startsWith("PFAM"))
    {
      format = "PFAM";
    }

    return format;
  }

  public int showSaveDialog(Component parent)
      throws HeadlessException
  {
    this.setAccessory(null);

    setDialogType(SAVE_DIALOG);

    int ret = showDialog(parent, "Save");

    if (getFileFilter() instanceof JalviewFileFilter)
    {
      JalviewFileFilter jvf = (JalviewFileFilter) getFileFilter();

      if (!jvf.accept(getSelectedFile()))
      {
        String withExtension = getSelectedFile() + "." +
            jvf.getAcceptableExtension();
        setSelectedFile(new File(withExtension));
      }
    }

    if ( (ret == JalviewFileChooser.APPROVE_OPTION) &&
        getSelectedFile().exists())
    {
      int confirm = JOptionPane.showConfirmDialog(parent,
                                                  "Overwrite existing file?",
                                                  "File exists",
                                                  JOptionPane.YES_NO_OPTION);

      if (confirm != JOptionPane.YES_OPTION)
      {
        ret = JalviewFileChooser.CANCEL_OPTION;
      }
    }

    return ret;
  }

  void recentListSelectionChanged(String selection)
  {
    setSelectedFile(null);

    File file = new File(selection);
    if (getFileFilter() instanceof JalviewFileFilter)
    {
      JalviewFileFilter jvf = (JalviewFileFilter)this.getFileFilter();

      if (!jvf.accept(file))
      {
        setFileFilter(getChoosableFileFilters()[0]);
      }
    }

    setSelectedFile(file);
  }

  class RecentlyOpened
      extends JPanel
  {
    JList list;
    public RecentlyOpened()
    {
      String historyItems = jalview.bin.Cache.getProperty("RECENT_FILE");
      StringTokenizer st;
      Vector recent = new Vector();

      if (historyItems != null)
      {
        st = new StringTokenizer(historyItems, "\t");

        while (st.hasMoreTokens())
        {
          recent.addElement(st.nextElement());
        }
      }

      list = new JList(recent);

      DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
      dlcr.setHorizontalAlignment(DefaultListCellRenderer.RIGHT);
      list.setCellRenderer(dlcr);

      list.addMouseListener(new MouseAdapter()
      {
        public void mousePressed(MouseEvent evt)
        {
          recentListSelectionChanged(list.getSelectedValue().toString());
        }
      });

      this.setBorder(new javax.swing.border.TitledBorder("Recently Opened"));

      final JScrollPane scroller = new JScrollPane(list);
      scroller.setPreferredSize(new Dimension(130, 200));
      this.add(scroller);

      javax.swing.SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          scroller.getHorizontalScrollBar().setValue(
              scroller.getHorizontalScrollBar().getMaximum());
        }
      });

    }

  }
}
