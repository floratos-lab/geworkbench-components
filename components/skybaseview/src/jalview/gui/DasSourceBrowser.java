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

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.biojava.dasobert.dasregistry.*;
import jalview.jbgui.*;
import jalview.util.*;

public class DasSourceBrowser
    extends GDasSourceBrowser implements Runnable, ListSelectionListener
{
  static DasSource[] dasSources = null;

  Hashtable localSources = null;

  Vector selectedSources;

  public static String DEFAULT_REGISTRY =
      "http://www.dasregistry.org/das1/sources/";

  boolean loadingDasSources = false;

  public DasSourceBrowser()
  {
    String registry = jalview.bin.Cache.getDefault("DAS_REGISTRY_URL",
        DEFAULT_REGISTRY);

    if (registry.indexOf("/registry/das1/sources/") > -1)
    {
      jalview.bin.Cache.setProperty("DAS_REGISTRY_URL", DEFAULT_REGISTRY);
      registry = DEFAULT_REGISTRY;
    }

    registryURL.setText(registry);

    setSelectedFromProperties();

    displayFullDetails(null);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    filter1.addListSelectionListener(this);
    filter2.addListSelectionListener(this);
    filter3.addListSelectionListener(this);

    //Ask to be notified of selection changes.
    ListSelectionModel rowSM = table.getSelectionModel();
    rowSM.addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (!lsm.isSelectionEmpty())
        {
          int selectedRow = lsm.getMinSelectionIndex();
          displayFullDetails(table.getValueAt(selectedRow, 0).toString());
        }
      }
    });

    table.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent evt)
      {
        if (evt.getClickCount() == 2
            || SwingUtilities.isRightMouseButton(evt))
        {
          editRemoveLocalSource(evt);
        }
      }
    });

    if (dasSources != null)
    {
      init();
    }
  }

  public void paintComponent(java.awt.Graphics g)
  {
    if (dasSources == null && !loadingDasSources)
    {
      Thread worker = new Thread(this);
      worker.start();
    }
  }

  void init()
  {
    int dSize = dasSources.length;
    Object[][] data = new Object[dSize][2];
    for (int i = 0; i < dSize; i++)
    {
      data[i][0] = dasSources[i].getNickname();
      data[i][1] = new Boolean(selectedSources.contains(dasSources[i].
          getNickname()));
    }

    refreshTableData(data);
    setCapabilities(dasSources);

    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        TableSorter sorter = (TableSorter) table.getModel();
        sorter.setSortingStatus(1, TableSorter.DESCENDING);
        sorter.setSortingStatus(1, TableSorter.NOT_SORTED);
      }
    });

    progressBar.setIndeterminate(false);
    progressBar.setVisible(false);
    addLocal.setVisible(true);
    refresh.setVisible(true);
  }

  public void refreshTableData(Object[][] data)
  {
    TableSorter sorter = new TableSorter(new DASTableModel(data));
    sorter.setTableHeader(table.getTableHeader());
    table.setModel(sorter);
  }

  void displayFullDetails(String nickName)
  {

    StringBuffer text = new StringBuffer(
        "<HTML><font size=\"2\" face=\"Verdana, Arial, Helvetica, sans-serif\">");

    if (nickName == null)
    {
      fullDetails.setText(text +
                          "Select a DAS service from the table"
                          + " to read a full description here.</font></html>");
      return;
    }

    int dSize = dasSources.length;
    for (int i = 0; i < dSize; i++)
    {
      if (!dasSources[i].getNickname().equals(nickName))
      {
        continue;
      }

      DasSource ds = dasSources[i];

      text.append("<font color=\"#0000FF\">Id:</font> " + dasSources[i].getId() +
                  "<br>");
      text.append("<font color=\"#0000FF\">Nickname:</font> " +
                  dasSources[i].getNickname() + "<br>");
      text.append("<font color=\"#0000FF\">URL:</font> " + dasSources[i].getUrl() +
                  "<br>");

      text.append(
          "<font color=\"#0000FF\">Admin Email:</font> <a href=\"mailto:"
          + dasSources[i].getAdminemail()
          + "\">" + dasSources[i].getAdminemail() + "</a>" +
          "<br>");

      text.append("<font color=\"#0000FF\">Registered at:</font> " +
                  dasSources[i].getRegisterDate() +
                  "<br>");

      text.append("<font color=\"#0000FF\">Last successful test:</font> " +
                  dasSources[i].getLeaseDate() +
                  "<br>");

      text.append("<font color=\"#0000FF\">Labels:</font> ");
      for (int s = 0; s < dasSources[i].getLabels().length; s++)
      {
        text.append(dasSources[i].getLabels()[s]);
        if (s < dasSources[i].getLabels().length - 1)
        {
          text.append(",");
        }
        text.append(" ");
      }
      text.append("<br>");

      text.append("<font color=\"#0000FF\">Capabilities:</font> ");
      String[] scap = dasSources[i].getCapabilities();
      for (int j = 0; j < scap.length; j++)
      {
        text.append(scap[j]);
        if (j < scap.length - 1)
        {
          text.append(", ");
        }
      }
      text.append("<br>");

      text.append("<font color=\"#0000FF\">Coordinates:</font> ");
      DasCoordinateSystem[] dcs = ds.getCoordinateSystem();
      for (int j = 0; j < dcs.length; j++)
      {
        text.append("(" + dcs[j].getUniqueId() + ") "
                    + dcs[j].getCategory() + ", " + dcs[j].getName());
        if (dcs[j].getNCBITaxId() != 0)
        {
          text.append(", " + dcs[j].getNCBITaxId());
        }
        if (dcs[j].getOrganismName().length() > 0)
        {
          text.append(", " + dcs[j].getOrganismName());
        }

        text.append("<br>");
      }

      text.append("<font color=\"#0000FF\">Description:</font> " +
                  dasSources[i].getDescription() + "<br>");

      if (dasSources[i].getHelperurl() != null
          && dasSources[i].getHelperurl().length() > 0)
      {
        text.append("<font color=\"#0000FF\"><a href=\"" +
                    dasSources[i].getHelperurl()
                    + "\">Go to site</a></font<br>");
      }

      text.append("</font></html>");

      break;
    }

    fullDetails.setText(text.toString());
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        fullDetailsScrollpane.getVerticalScrollBar().setValue(0);
      }
    });
  }

  public void run()
  {
    loadingDasSources = true;

    addLocal.setVisible(false);
    refresh.setVisible(false);
    progressBar.setVisible(true);
    progressBar.setIndeterminate(true);

    dasSources = jalview.io.DasSequenceFeatureFetcher.getDASSources();

    appendLocalSources();

    init();

    loadingDasSources = false;

  }

  public Vector getSelectedSources()
  {
    Vector selected = new Vector();
    for (int r = 0; r < selectedSources.size(); r++)
    {
      for (int i = 0; i < dasSources.length; i++)
      {
        if (dasSources[i].getNickname().equals(
            selectedSources.elementAt(r)))
        {
          selected.addElement(dasSources[i]);
          break;
        }
      }
    }

    return selected;
  }

  public DasSource[] getDASSource()
  {
    if (dasSources == null)
    {
      dasSources = jalview.io.DasSequenceFeatureFetcher.getDASSources();
      appendLocalSources();
    }

    return dasSources;
  }

  public void refresh_actionPerformed(ActionEvent e)
  {
    saveProperties(jalview.bin.Cache.applicationProperties);

    Thread worker = new Thread(this);
    worker.start();
  }

  private void setCapabilities(DasSource[] sources)
  {
    Vector authority = new Vector();
    Vector type = new Vector();
    Vector label = new Vector();

    authority.addElement("Any");
    type.addElement("Any");
    label.addElement("Any");

    for (int i = 0; i < sources.length; i++)
    {
      DasSource ds = sources[i];

      DasCoordinateSystem[] dcs = ds.getCoordinateSystem();

      for (int j = 0; j < dcs.length; j++)
      {
        if (!type.contains(dcs[j].getCategory()))
        {
          type.addElement(dcs[j].getCategory());
        }

        if (!authority.contains(dcs[j].getName()))
        {
          authority.addElement(dcs[j].getName());
        }
      }

      String[] slabels = ds.getLabels();
      for (int s = 0; s < slabels.length; s++)
      {
        if (!label.contains(slabels[s]))
        {
          label.addElement(slabels[s]);
        }
      }

    }

    filter1.setListData(authority);
    filter2.setListData(type);
    filter3.setListData(label);

    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        filter1.setSelectedIndex(0);
        filter2.setSelectedIndex(0);
        filter3.setSelectedIndex(0);
      }
    });
  }

  public void amendLocal(boolean newSource)
  {
    String url = "http://localhost:8080/", nickname = "";

    if (!newSource)
    {
      int selectedRow = table.getSelectionModel().getMinSelectionIndex();
      nickname = table.getValueAt(selectedRow, 0).toString();
      url = ( (DasSource) localSources.get(nickname)).getUrl();
    }

    JTextField nametf = new JTextField(nickname, 40);
    JTextField urltf = new JTextField(url, 40);

    JPanel panel = new JPanel(new BorderLayout());
    JPanel pane12 = new JPanel(new BorderLayout());
    pane12.add(new JLabel("Nickname: "), BorderLayout.CENTER);
    pane12.add(nametf, BorderLayout.EAST);
    panel.add(pane12, BorderLayout.NORTH);
    pane12 = new JPanel(new BorderLayout());
    pane12.add(new JLabel("URL: "), BorderLayout.CENTER);
    pane12.add(urltf, BorderLayout.EAST);
    panel.add(pane12, BorderLayout.SOUTH);

    int reply = JOptionPane.showInternalConfirmDialog(Desktop.desktop,
        panel, "Enter Nickname & URL of Local DAS Source",
        JOptionPane.OK_CANCEL_OPTION);

    if (reply != JOptionPane.OK_OPTION)
    {
      return;
    }

    if (!urltf.getText().endsWith("/"))
    {
      urltf.setText(urltf.getText() + "/");
    }

    Das1Source local = new Das1Source();

    local.setUrl(urltf.getText());
    local.setNickname(nametf.getText());

    if (localSources == null)
    {
      localSources = new Hashtable();
    }

    localSources.put(local.getNickname(), local);

    if (!newSource && !nickname.equals(nametf.getText()))
    {
      localSources.remove(nickname);
    }

    int size = dasSources.length;
    int adjust = newSource ? 1 : 0;

    Object[][] data = new Object[size + adjust][2];
    for (int i = 0; i < size; i++)
    {
      if (!newSource && dasSources[i].getNickname().equals(nickname))
      {
        ( (DasSource) dasSources[i]).setNickname(local.getNickname());
        ( (DasSource) dasSources[i]).setUrl(local.getUrl());
        data[i][0] = local.getNickname();
        data[i][1] = new Boolean(true);
      }
      else
      {
        data[i][0] = dasSources[i].getNickname();
        data[i][1] = new Boolean(selectedSources.contains(dasSources[i].
            getNickname()));
      }
    }

    if (newSource)
    {
      data[size][0] = local.getNickname();
      data[size][1] = new Boolean(true);
      selectedSources.add(local.getNickname());
    }

    DasSource[] tmp = new DasSource[size + adjust];

    System.arraycopy(dasSources, 0, tmp, 0, size);

    if (newSource)
    {
      tmp[size] = local;
    }

    dasSources = tmp;

    refreshTableData(data);

    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        scrollPane.getVerticalScrollBar().setValue(
            scrollPane.getVerticalScrollBar().getMaximum()
            );
      }
    });

    displayFullDetails(local.getNickname());
  }

  public void editRemoveLocalSource(MouseEvent evt)
  {
    int selectedRow = table.getSelectionModel().getMinSelectionIndex();
    if (selectedRow == -1)
    {
      return;
    }

    String nickname = table.getValueAt(selectedRow, 0).toString();

    if (!localSources.containsKey(nickname))
    {
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                            "You can only edit or remove local DAS Sources!",
                                            "Public DAS source - not editable",
                                            JOptionPane.WARNING_MESSAGE);
      return;
    }

    Object[] options =
        {
        "Edit", "Remove", "Cancel"};
    int choice = JOptionPane.showInternalOptionDialog(Desktop.desktop,
        "Do you want to edit or remove " + nickname + "?",
        "Edit / Remove Local DAS Source",
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[2]);

    switch (choice)
    {
      case 0:
        amendLocal(false);
        break;
      case 1:
        localSources.remove(nickname);
        selectedSources.remove(nickname);
        Object[][] data = new Object[dasSources.length - 1][2];
        DasSource[] tmp = new DasSource[dasSources.length - 1];
        int index = 0;
        for (int i = 0; i < dasSources.length; i++)
        {
          if (dasSources[i].getNickname().equals(nickname))
          {
            continue;
          }
          else
          {
            tmp[index] = dasSources[i];
            data[index][0] = dasSources[i].getNickname();
            data[index][1] = new Boolean(selectedSources.contains(dasSources[i].
                getNickname()));
            index++;
          }
        }
        dasSources = tmp;
        refreshTableData(data);
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            scrollPane.getVerticalScrollBar().setValue(
                scrollPane.getVerticalScrollBar().getMaximum()
                );
          }
        });

        break;
    }
  }

  void appendLocalSources()
  {
    if (localSources == null)
    {
      return;
    }

    int size = dasSources != null ? dasSources.length : 0;
    int lsize = localSources.size();

    Object[][] data = new Object[size + lsize][2];
    for (int i = 0; i < size; i++)
    {
      data[i][0] = dasSources[i].getNickname();
      data[i][1] = new Boolean(selectedSources.contains(dasSources[i].
          getNickname()));
    }

    DasSource[] tmp = new DasSource[size + lsize];
    if (dasSources != null)
    {
      System.arraycopy(dasSources, 0, tmp, 0, size);
    }

    Enumeration en = localSources.keys();
    int index = size;
    while (en.hasMoreElements())
    {
      String key = en.nextElement().toString();
      data[index][0] = key;
      data[index][1] = new Boolean(false);
      tmp[index] = new Das1Source();
      tmp[index].setNickname(key);
      tmp[index].setUrl( ( (DasSource) localSources.get(key)).getUrl());

      index++;
    }

    dasSources = tmp;

    refreshTableData(data);
  }

  public void valueChanged(ListSelectionEvent evt)
  {
    //Called when the MainTable selection changes
    if (evt.getValueIsAdjusting())
    {
      return;
    }

    displayFullDetails(null);

    // Filter the displayed data sources
    int dSize = dasSources.length;

    ArrayList names = new ArrayList();
    ArrayList selected = new ArrayList();
    DasSource ds;

    //The features filter is not visible, but we must still
    //filter the das source list here.
    //July 2006 - only 6 sources fo not serve features
    Object[] dummyFeatureList = new Object[]
        {
        "features"};

    for (int i = 0; i < dSize; i++)
    {
      ds = dasSources[i];
      DasCoordinateSystem[] dcs = ds.getCoordinateSystem();

      if (dcs.length == 0 && ds.getCapabilities().length == 0
          && filter1.getSelectedIndex() == 0
          && filter2.getSelectedIndex() == 0
          && filter3.getSelectedIndex() == 0)
      {
        //THIS IS A FIX FOR LOCAL SOURCES WHICH DO NOT
        //HAVE COORDINATE SYSTEMS, INFO WHICH AT PRESENT
        //IS ADDED FROM THE REGISTRY
        names.add(ds.getNickname());
        selected.add(new Boolean(
            selectedSources.contains(ds.getNickname())));
        continue;
      }

      if (!selectedInList(dummyFeatureList, ds.getCapabilities())
          || !selectedInList(filter3.getSelectedValues(),
                             ds.getLabels()))
      {
        continue;
      }

      for (int j = 0; j < dcs.length; j++)
      {
        if (selectedInList(filter1.getSelectedValues(),
                           new String[]
                           {dcs[j].getName()})
            && selectedInList(filter2.getSelectedValues(),
                              new String[]
                              {dcs[j].getCategory()}))
        {
          names.add(ds.getNickname());
          selected.add(new Boolean(
              selectedSources.contains(ds.getNickname())));
          break;
        }
      }
    }

    dSize = names.size();
    Object[][] data = new Object[dSize][2];
    for (int d = 0; d < dSize; d++)
    {
      data[d][0] = names.get(d);
      data[d][1] = selected.get(d);
    }

    refreshTableData(data);
  }

  boolean selectedInList(Object[] selection, String[] items)
  {
    for (int i = 0; i < selection.length; i++)
    {
      if (selection[i].equals("Any"))
      {
        return true;
      }

      for (int j = 0; j < items.length; j++)
      {
        if (selection[i].equals(items[j]))
        {
          return true;
        }
      }
    }

    return false;
  }

  void setSelectedFromProperties()
  {
    String active = jalview.bin.Cache.getDefault("DAS_ACTIVE_SOURCE", "uniprot");
    StringTokenizer st = new StringTokenizer(active, "\t");
    selectedSources = new Vector();
    while (st.hasMoreTokens())
    {
      selectedSources.addElement(st.nextToken());
    }

    String local = jalview.bin.Cache.getProperty("DAS_LOCAL_SOURCE");
    if (local != null)
    {
      if (localSources == null)
      {
        localSources = new Hashtable();
      }

      st = new StringTokenizer(local, "\t");
      while (st.hasMoreTokens())
      {
        String token = st.nextToken();
        int bar = token.indexOf("|");
        Das1Source source = new Das1Source();

        source.setUrl(token.substring(bar + 1));
        source.setNickname(token.substring(0, bar));

        localSources.put(source.getNickname(), source);
      }
    }
  }

  public void reset_actionPerformed(ActionEvent e)
  {
    registryURL.setText(DEFAULT_REGISTRY);
  }

  public void saveProperties(Properties properties)
  {
    if (registryURL.getText() == null || registryURL.getText().length() < 1)
    {
      properties.remove("DAS_REGISTRY_URL");
    }
    else
    {
      properties.setProperty("DAS_REGISTRY_URL", registryURL.getText());
    }

    StringBuffer sb = new StringBuffer();
    for (int r = 0; r < table.getModel().getRowCount(); r++)
    {
      if ( ( (Boolean) table.getValueAt(r, 1)).booleanValue())
      {
        sb.append(table.getValueAt(r, 0) + "\t");
      }
    }

    properties.setProperty("DAS_ACTIVE_SOURCE", sb.toString());

    if (localSources != null)
    {
      sb = new StringBuffer();
      Enumeration en = localSources.keys();
      while (en.hasMoreElements())
      {
        String token = en.nextElement().toString();
        sb.append(token + "|"
                  + ( (DasSource) localSources.get(token)).getUrl()
                  + "\t");
      }

      properties.setProperty("DAS_LOCAL_SOURCE", sb.toString());
    }

  }

  class DASTableModel
      extends AbstractTableModel
  {

    public DASTableModel(Object[][] data)
    {
      this.data = data;
    }

    private String[] columnNames = new String[]
        {
        "Nickname", "Use Source"};

    private Object[][] data;

    public int getColumnCount()
    {
      return columnNames.length;
    }

    public int getRowCount()
    {
      return data.length;
    }

    public String getColumnName(int col)
    {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col)
    {
      return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c)
    {
      return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col)
    {
      //Note that the data/cell address is constant,
      //no matter where the cell appears onscreen.
      return col == 1;

    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col)
    {
      data[row][col] = value;
      fireTableCellUpdated(row, col);

      String name = getValueAt(row, 0).toString();
      boolean selected = ( (Boolean) value).booleanValue();

      if (selectedSources.contains(name) && !selected)
      {
        selectedSources.remove(name);
      }

      if (!selectedSources.contains(name) && selected)
      {
        selectedSources.add(name);
      }
    }
  }
}
