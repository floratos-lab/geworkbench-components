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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import jalview.datamodel.*;
import jalview.io.*;

public class FeatureSettings
    extends JPanel
{
  DasSourceBrowser dassourceBrowser;
  jalview.io.DasSequenceFeatureFetcher dasFeatureFetcher;
  JPanel settingsPane = new JPanel();
  JPanel dasSettingsPane = new JPanel();

  final FeatureRenderer fr;
  public final AlignFrame af;
  Object[][] originalData;
  final JInternalFrame frame;
  JScrollPane scrollPane = new JScrollPane();
  JTable table;
  JPanel groupPanel;
  JSlider transparency = new JSlider();

  JPanel transPanel = new JPanel(new FlowLayout());

  public FeatureSettings(AlignFrame af)
  {
    this.af = af;
    fr = af.getFeatureRenderer();

    transparency.setMaximum(100 - (int) (fr.transparency * 100));

    try
    {
      jbInit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    table = new JTable();
    table.getTableHeader().setFont(new Font("Verdana", Font.PLAIN, 12));
    table.setFont(new Font("Verdana", Font.PLAIN, 12));
    table.setDefaultRenderer(Color.class,
                             new ColorRenderer());

    table.setDefaultEditor(Color.class,
                           new ColorEditor());

    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    table.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent evt)
      {
        selectedRow = table.rowAtPoint(evt.getPoint());
      }
    });

    table.addMouseMotionListener(new MouseMotionAdapter()
    {
      public void mouseDragged(MouseEvent evt)
      {
        int newRow = table.rowAtPoint(evt.getPoint());
        if (newRow != selectedRow
            && selectedRow != -1
            && newRow != -1)
        {
          Object[] temp = new Object[3];
          temp[0] = table.getValueAt(selectedRow, 0);
          temp[1] = table.getValueAt(selectedRow, 1);
          temp[2] = table.getValueAt(selectedRow, 2);

          table.setValueAt(table.getValueAt(newRow, 0), selectedRow, 0);
          table.setValueAt(table.getValueAt(newRow, 1), selectedRow, 1);
          table.setValueAt(table.getValueAt(newRow, 2), selectedRow, 2);

          table.setValueAt(temp[0], newRow, 0);
          table.setValueAt(temp[1], newRow, 1);
          table.setValueAt(temp[2], newRow, 2);

          selectedRow = newRow;
        }
      }
    });

    scrollPane.setViewportView(table);

    dassourceBrowser = new DasSourceBrowser();
    dasSettingsPane.add(dassourceBrowser, BorderLayout.CENTER);

    if (af.getViewport().featuresDisplayed == null || fr.renderOrder == null)
    {
      fr.findAllFeatures(true); // display everything!
    }

    setTableData();
    final PropertyChangeListener change;
    final FeatureSettings fs=this;
    fr.addPropertyChangeListener(change=new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt)
      {
        if (!fs.resettingTable && !fs.handlingUpdate) {
          fs.handlingUpdate=true;
          fs.resetTable(null); // new groups may be added with new seuqence feature types only
          fs.handlingUpdate=false;
        }
      }

    });

    frame = new JInternalFrame();
    frame.setContentPane(this);
    Desktop.addInternalFrame(frame, "Sequence Feature Settings", 400, 450);
    frame.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter()
    {
      public void internalFrameClosed(
          javax.swing.event.InternalFrameEvent evt)
      {
        fr.removePropertyChangeListener(change);
      }
      ;
    });
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
  }
  /**
   * true when Feature Settings are updating from feature renderer
   */
  private boolean handlingUpdate=false;

  /**
   * contains a float[3] for each feature type string. created by setTableData
   */
  Hashtable typeWidth=null;
  synchronized public void setTableData()
  {
    if (fr.featureGroups == null)
    {
      fr.featureGroups = new Hashtable();
    }
    Vector allFeatures = new Vector();
    Vector allGroups = new Vector();
    SequenceFeature[] tmpfeatures;
    String group;
    for (int i = 0; i < af.getViewport().alignment.getHeight(); i++)
    {
      if (af.getViewport().alignment.getSequenceAt(i).getDatasetSequence().
          getSequenceFeatures() == null)
      {
        continue;
      }

      tmpfeatures = af.getViewport().alignment.getSequenceAt(i).
          getDatasetSequence().getSequenceFeatures();

      int index = 0;
      while (index < tmpfeatures.length)
      {
        if (tmpfeatures[index].begin == 0 && tmpfeatures[index].end == 0)
        {
          index++;
          continue;
        }

        if (tmpfeatures[index].getFeatureGroup() != null)
        {
          group = tmpfeatures[index].featureGroup;
          if (!allGroups.contains(group))
          {
            allGroups.addElement(group);
            if (group!=null)
            {
              checkGroupState(group);
            }
          }
        }

        if (!allFeatures.contains(tmpfeatures[index].getType()))
        {
          allFeatures.addElement(tmpfeatures[index].getType());
        }
        index++;
      }
    }

    resetTable(null);

    validate();
  }
  /**
   *
   * @param group
   * @return true if group has been seen before and is already added to set.
   */
  private boolean checkGroupState(String group) {
    boolean visible;
    if (fr.featureGroups.containsKey(group))
    {
      visible = ( (Boolean) fr.featureGroups.get(group)).booleanValue();
        } else {
        visible=true; // new group is always made visible
      }

      if (groupPanel == null)
      {
        groupPanel = new JPanel();
      }

      boolean alreadyAdded = false;
      for (int g = 0; g < groupPanel.getComponentCount(); g++)
      {
        if ( ( (JCheckBox) groupPanel.getComponent(g))
            .getText().equals(group))
        {
          alreadyAdded = true;
          ((JCheckBox)groupPanel.getComponent(g)).setSelected(visible);
          break;
        }
      }

      if (alreadyAdded)
      {

        return true;
      }

      fr.featureGroups.put(group, new Boolean(visible));
      final String grp = group;
      final JCheckBox check = new JCheckBox(group, visible);
      check.setFont(new Font("Serif", Font.BOLD, 12));
      check.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent evt)
        {
          fr.featureGroups.put(check.getText(),
                               new Boolean(check.isSelected()));
          af.alignPanel.seqPanel.seqCanvas.repaint();
          if (af.alignPanel.overviewPanel != null)
          {
            af.alignPanel.overviewPanel.updateOverviewImage();
          }

          resetTable(new String[] { grp } );
        }
      });
      groupPanel.add(check);
      return false;
  }
  boolean resettingTable=false;
  synchronized void resetTable(String[] groupChanged)
  {
    if (resettingTable==true)
    {
      return;
    }
    resettingTable=true;
    typeWidth=new Hashtable();
    // TODO: change avWidth calculation to 'per-sequence' average and use long rather than float
    float[] avWidth=null;
    SequenceFeature[] tmpfeatures;
    String group = null, type;
    Vector visibleChecks = new Vector();

    //Find out which features should be visible depending on which groups
    //are selected / deselected
    //and recompute average width ordering
    for (int i = 0; i < af.getViewport().alignment.getHeight(); i++)
    {

      tmpfeatures = af.getViewport().alignment.getSequenceAt(i).
          getDatasetSequence().getSequenceFeatures();
      if (tmpfeatures == null)
      {
        continue;
      }

      int index = 0;
      while (index < tmpfeatures.length)
      {
        group = tmpfeatures[index].featureGroup;

        if (tmpfeatures[index].begin == 0 && tmpfeatures[index].end == 0)
        {
          index++;
          continue;
        }

        if (group == null || fr.featureGroups.get(group) == null ||
            ( (Boolean) fr.featureGroups.get(group)).booleanValue())
        {
          if (group!=null)
            checkGroupState(group);
          type = tmpfeatures[index].getType();
          if (!visibleChecks.contains(type))
          {
            visibleChecks.addElement(type);
          }
        }
        if (!typeWidth.containsKey(tmpfeatures[index].getType())) {
          typeWidth.put(tmpfeatures[index].getType(), avWidth=new float[3]);
        } else {
          avWidth = (float[]) typeWidth.get(tmpfeatures[index].getType());
        }
        avWidth[0]++;
        if (tmpfeatures[index].getBegin()>tmpfeatures[index].getEnd())
        {
          avWidth[1]+=1+tmpfeatures[index].getBegin()-tmpfeatures[index].getEnd();
        } else {
          avWidth[1]+=1+tmpfeatures[index].getEnd()-tmpfeatures[index].getBegin();
        }
        index++;
      }
    }

    int fSize = visibleChecks.size();
    Object[][] data = new Object[fSize][3];
    int dataIndex = 0;

    if (fr.renderOrder != null)
    {
      if (!handlingUpdate)
        fr.findAllFeatures(groupChanged!=null); // prod to update colourschemes. but don't affect display
      //First add the checks in the previous render order,
      //in case the window has been closed and reopened
      for (int ro = fr.renderOrder.length - 1; ro > -1; ro--)
      {
        type = fr.renderOrder[ro];

        if (!visibleChecks.contains(type))
        {
          continue;
        }

        data[dataIndex][0] = type;
        data[dataIndex][1] = fr.getColour(type);
        data[dataIndex][2] = new Boolean(af.getViewport().featuresDisplayed.containsKey(type));
        dataIndex++;
        visibleChecks.removeElement(type);
      }
    }

    fSize = visibleChecks.size();
    for (int i = 0; i < fSize; i++)
    {
      //These must be extra features belonging to the group
      //which was just selected
      type = visibleChecks.elementAt(i).toString();
      data[dataIndex][0] = type;

      data[dataIndex][1] = fr.getColour(type);
      if (data[dataIndex][1] == null)
      {
        //"Colour has been updated in another view!!"
        fr.renderOrder = null;
        return;
      }

      data[dataIndex][2] = new Boolean(true);
      dataIndex++;
    }

    if (originalData == null)
    {
      originalData = new Object[data.length][3];
      System.arraycopy(data, 0, originalData, 0, data.length);
    }

    table.setModel(new FeatureTableModel(data));
    table.getColumnModel().getColumn(0).setPreferredWidth(200);

    if (groupPanel != null)
    {
      groupPanel.setLayout(
          new GridLayout(fr.featureGroups.size() / 4 + 1, 4));

      groupPanel.validate();
      bigPanel.add(groupPanel, BorderLayout.NORTH);
    }

    updateFeatureRenderer(data, groupChanged!=null);
    resettingTable=false;
  }
  /**
   * reorder data based on the featureRenderers global priority list.
   * @param data
   */
  private void ensureOrder(Object[][] data)
  {
    boolean sort=false;
    float[] order = new float[data.length];
    for (int i=0;i<order.length; i++)
    {
      order[i] = fr.getOrder(data[i][0].toString());
      if (order[i]<0)
        order[i] = fr.setOrder(data[i][0].toString(), i/order.length);
      if (i>1)
        sort = sort || order[i-1]>order[i];
    }
    if (sort)
      jalview.util.QuickSort.sort(order, data);
  }

  void load()
  {
    JalviewFileChooser chooser = new JalviewFileChooser(jalview.bin.Cache.
        getProperty(
            "LAST_DIRECTORY"), new String[]
        {"fc"},
        new String[]
        {"Sequence Feature Colours"}, "Sequence Feature Colours");
    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle("Load Feature Colours");
    chooser.setToolTipText("Load");

    int value = chooser.showOpenDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      File file = chooser.getSelectedFile();

      try
      {
        InputStreamReader in = new InputStreamReader(new FileInputStream(
            file), "UTF-8");

        jalview.binding.JalviewUserColours jucs = new jalview.binding.
            JalviewUserColours();
        jucs = (jalview.binding.JalviewUserColours) jucs.unmarshal(in);

        for (int i = jucs.getColourCount()-1; i >=0; i--)
        {
          String name;
          fr.setColour(name=jucs.getColour(i).getName(),
                       new Color(Integer.parseInt(jucs.getColour(i).getRGB(),
                                                  16)));
          fr.setOrder(name,(i==0) ? 0 : i/jucs.getColourCount());
        }
        if (table!=null) {
          resetTable(null);
          Object[][] data=((FeatureTableModel) table.getModel()).getData();
          ensureOrder(data);
          updateFeatureRenderer(data,false);
          table.repaint();
        }
      }
      catch (Exception ex)
      {
        System.out.println("Error loading User Colour File\n" + ex);
      }
    }
  }

  void save()
  {
    JalviewFileChooser chooser = new JalviewFileChooser(jalview.bin.Cache.
        getProperty(
            "LAST_DIRECTORY"), new String[]
        {"fc"},
        new String[]
        {"Sequence Feature Colours"}, "Sequence Feature Colours");
    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle("Save Feature Colour Scheme");
    chooser.setToolTipText("Save");

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String choice = chooser.getSelectedFile().getPath();
      jalview.binding.JalviewUserColours ucs = new jalview.binding.
          JalviewUserColours();
      ucs.setSchemeName("Sequence Features");
      try
      {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(choice), "UTF-8"));

        Enumeration e = fr.featureColours.keys();
        float[] sortOrder = new float[fr.featureColours.size()];
        String[] sortTypes = new String[fr.featureColours.size()];
        int i=0;
        while (e.hasMoreElements())
        {
          sortTypes[i] = e.nextElement().toString();
          sortOrder[i]  = fr.getOrder(sortTypes[i]);
          i++;
        }
        jalview.util.QuickSort.sort(sortOrder, sortTypes);
        sortOrder=null;
        for (i=0; i<sortTypes.length; i++) {
          jalview.binding.Colour col = new jalview.binding.Colour();
          col.setName(sortTypes[i]);
          col.setRGB(jalview.util.Format.getHexString(
              fr.getColour(col.getName())));
          ucs.addColour(col);
        }
        ucs.marshal(out);
        out.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  public void invertSelection()
  {
    for (int i = 0; i < table.getRowCount(); i++)
    {
      Boolean value = (Boolean) table.getValueAt(i, 2);

      table.setValueAt(
          new Boolean(!value.booleanValue()),
          i, 2);
    }
  }
  public void orderByAvWidth() {
    if (table==null || table.getModel()==null)
      return;
    Object[][] data = ((FeatureTableModel) table.getModel()).getData();
    float[] width = new float[data.length];
    float[] awidth;
    float max=0;
    int num=0;
    for (int i=0;i<data.length;i++) {
       awidth = (float[]) typeWidth.get(data[i][0]);
       if (awidth[0]>0) {
         width[i] = awidth[1]/awidth[0];// *awidth[0]*awidth[2]; - better weight - but have to make per sequence, too (awidth[2])
         //if (width[i]==1) // hack to distinguish single width sequences.
         num++;
       } else {
         width[i]=0;
       }
       if (max<width[i])
         max=width[i];
    }
    boolean sort=false;
    for (int i=0;i<width.length; i++) {
      //awidth = (float[]) typeWidth.get(data[i][0]);
      if (width[i]==0)
      {
        width[i] = fr.getOrder(data[i][0].toString());
        if (width[i]<0)
        {
          width[i] = fr.setOrder(data[i][0].toString(), i/data.length);
        }
      } else {
        width[i] /=max; // normalize
        fr.setOrder(data[i][0].toString(), width[i]); // store for later
      }
      if (i>0)
        sort = sort || width[i-1]>width[i];
    }
    if (sort)
      jalview.util.QuickSort.sort(width, data);
    // update global priority order

    updateFeatureRenderer(data,false);
    table.repaint();
  }
  public void close()
  {
    try
    {
      frame.setClosed(true);
    }
    catch (Exception exe)
    {}

  }

  public void updateFeatureRenderer(Object[][] data)
  {
    updateFeatureRenderer(data, true);
  }
  private void updateFeatureRenderer(Object[][] data, boolean visibleNew)
  {
    fr.setFeaturePriority(data, visibleNew);
    af.alignPanel.paintAlignment(true);
  }

  int selectedRow = -1;
  JTabbedPane tabbedPane = new JTabbedPane();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  BorderLayout borderLayout3 = new BorderLayout();
  JPanel bigPanel = new JPanel();
  BorderLayout borderLayout4 = new BorderLayout();
  JButton invert = new JButton();
  JPanel buttonPanel = new JPanel();
  JButton cancel = new JButton();
  JButton ok = new JButton();
  JButton loadColours = new JButton();
  JButton saveColours = new JButton();
  JPanel dasButtonPanel = new JPanel();
  JButton fetchDAS = new JButton();
  JButton saveDAS = new JButton();
  JButton cancelDAS = new JButton();
  JButton optimizeOrder = new JButton();
  JPanel transbuttons = new JPanel(new BorderLayout());
  private void jbInit()
      throws Exception
  {
    this.setLayout(borderLayout1);
    settingsPane.setLayout(borderLayout2);
    dasSettingsPane.setLayout(borderLayout3);
    bigPanel.setLayout(borderLayout4);
    invert.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    invert.setText("Invert Selection");
    invert.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        invertSelection();
      }
    });
    optimizeOrder.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    optimizeOrder.setText("Optimise Order");
    optimizeOrder.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        orderByAvWidth();
      }
    });
    cancel.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    cancel.setText("Cancel");
    cancel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        updateFeatureRenderer(originalData);
        close();
      }
    });
    ok.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    ok.setText("OK");
    ok.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        close();
      }
    });
    loadColours.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    loadColours.setText("Load Colours");
    loadColours.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        load();
      }
    });
    saveColours.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    saveColours.setText("Save Colours");
    saveColours.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        save();
      }
    });
    transparency.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent evt)
      {
        fr.setTransparency( (float) (100 - transparency.getValue()) / 100f);
        af.alignPanel.paintAlignment(true);
      }
    });

    transparency.setMaximum(70);
    fetchDAS.setText("Fetch DAS Features");
    fetchDAS.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        fetchDAS_actionPerformed(e);
      }
    });
    saveDAS.setText("Save as default");
    saveDAS.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        saveDAS_actionPerformed(e);
      }
    });
    dasButtonPanel.setBorder(BorderFactory.createEtchedBorder());
    dasSettingsPane.setBorder(null);
    cancelDAS.setEnabled(false);
    cancelDAS.setText("Cancel Fetch");
    cancelDAS.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancelDAS_actionPerformed(e);
      }
    });
    this.add(tabbedPane, java.awt.BorderLayout.CENTER);
    tabbedPane.addTab("Feature Settings", settingsPane);
    tabbedPane.addTab("DAS Settings", dasSettingsPane);
    bigPanel.add(transPanel, java.awt.BorderLayout.SOUTH);
    transPanel.add(transparency);
    transbuttons.add(invert, java.awt.BorderLayout.NORTH);
    transbuttons.add(optimizeOrder,java.awt.BorderLayout.SOUTH);
    transPanel.add(transbuttons);
    buttonPanel.add(ok);
    buttonPanel.add(cancel);
    buttonPanel.add(loadColours);
    buttonPanel.add(saveColours);
    bigPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
    dasSettingsPane.add(dasButtonPanel, java.awt.BorderLayout.SOUTH);
    dasButtonPanel.add(fetchDAS);
    dasButtonPanel.add(cancelDAS);
    dasButtonPanel.add(saveDAS);
    settingsPane.add(bigPanel, java.awt.BorderLayout.CENTER);
    settingsPane.add(buttonPanel, java.awt.BorderLayout.SOUTH);
  }

  public void fetchDAS_actionPerformed(ActionEvent e)
  {
    fetchDAS.setEnabled(false);
    cancelDAS.setEnabled(true);
    Vector selectedSources = dassourceBrowser.getSelectedSources();

    SequenceI[] dataset, seqs;
    int iSize;

    if (af.getViewport().getSelectionGroup() != null
        && af.getViewport().getSelectionGroup().getSize() > 0)
    {
      iSize = af.getViewport().getSelectionGroup().getSize();
      dataset = new SequenceI[iSize];
      seqs = af.getViewport().getSelectionGroup().
          getSequencesInOrder(
              af.getViewport().getAlignment());
    }
    else
    {
      iSize = af.getViewport().getAlignment().getHeight();
      seqs = af.getViewport().getAlignment().getSequencesArray();
    }

    dataset = new SequenceI[iSize];
    for (int i = 0; i < iSize; i++)
    {
      dataset[i] = seqs[i].getDatasetSequence();
    }

    dasFeatureFetcher =
        new jalview.io.DasSequenceFeatureFetcher(
            dataset,
            this,
            selectedSources);
    cancelDAS.setEnabled(true);
    af.getViewport().setShowSequenceFeatures(true);
    af.showSeqFeatures.setSelected(true);
  }

  public void saveDAS_actionPerformed(ActionEvent e)
  {
    dassourceBrowser.saveProperties(jalview.bin.Cache.applicationProperties);
  }

  public void complete()
  {
    fetchDAS.setEnabled(true);
    cancelDAS.setEnabled(false);
  }

  public void cancelDAS_actionPerformed(ActionEvent e)
  {
    dasFeatureFetcher.cancel();
    fetchDAS.setEnabled(true);
    cancelDAS.setEnabled(false);
  }
  public void noDasSourceActive()
  {
    JOptionPane.showInternalConfirmDialog(Desktop.desktop,
            "No das sources were selected.\n"
            + "Please select some sources and\n"
            +" try again.",
            "No Sources Selected",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE);
    complete();
  }

  /////////////////////////////////////////////////////////////////////////
  // http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
  /////////////////////////////////////////////////////////////////////////
  class FeatureTableModel
      extends AbstractTableModel
  {
    FeatureTableModel(Object[][] data)
    {
      this.data = data;
    }

    private String[] columnNames =
        {
        "Feature Type", "Colour", "Display"};
    private Object[][] data;

    public Object[][] getData()
    {
      return data;
    }

    public void setData(Object[][] data)
    {
      this.data = data;
    }

    public int getColumnCount()
    {
      return columnNames.length;
    }

    public Object[] getRow(int row)
    {
      return data[row];
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

    public Class getColumnClass(int c)
    {
      return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col)
    {
      return col == 0 ? false : true;
    }

    public void setValueAt(Object value, int row, int col)
    {
      data[row][col] = value;
      fireTableCellUpdated(row, col);
      updateFeatureRenderer(data);
    }

  }

  class ColorRenderer
      extends JLabel implements TableCellRenderer
  {
    javax.swing.border.Border unselectedBorder = null;
    javax.swing.border.Border selectedBorder = null;

    public ColorRenderer()
    {
      setOpaque(true); //MUST do this for background to show up.
    }

    public Component getTableCellRendererComponent(
        JTable table, Object color,
        boolean isSelected, boolean hasFocus,
        int row, int column)
    {
      Color newColor = (Color) color;
      setBackground(newColor);
      if (isSelected)
      {
        if (selectedBorder == null)
        {
          selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
              table.getSelectionBackground());
        }
        setBorder(selectedBorder);
      }
      else
      {
        if (unselectedBorder == null)
        {
          unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
              table.getBackground());
        }
        setBorder(unselectedBorder);
      }

      setToolTipText("RGB value: " + newColor.getRed() + ", "
                     + newColor.getGreen() + ", "
                     + newColor.getBlue());
      return this;
    }
  }

}

class ColorEditor
    extends AbstractCellEditor implements TableCellEditor,
    ActionListener
{
  Color currentColor;
  JButton button;
  JColorChooser colorChooser;
  JDialog dialog;
  protected static final String EDIT = "edit";

  public ColorEditor()
  {
    //Set up the editor (from the table's point of view),
    //which is a button.
    //This button brings up the color chooser dialog,
    //which is the editor from the user's point of view.
    button = new JButton();
    button.setActionCommand(EDIT);
    button.addActionListener(this);
    button.setBorderPainted(false);
    //Set up the dialog that the button brings up.
    colorChooser = new JColorChooser();
    dialog = JColorChooser.createDialog(button,
                                        "Select new Colour",
                                        true, //modal
                                        colorChooser,
                                        this, //OK button handler
                                        null); //no CANCEL button handler
  }

  /**
   * Handles events from the editor button and from
   * the dialog's OK button.
   */
  public void actionPerformed(ActionEvent e)
  {

    if (EDIT.equals(e.getActionCommand()))
    {
      //The user has clicked the cell, so
      //bring up the dialog.
      button.setBackground(currentColor);
      colorChooser.setColor(currentColor);
      dialog.setVisible(true);

      //Make the renderer reappear.
      fireEditingStopped();

    }
    else
    { //User pressed dialog's "OK" button.
      currentColor = colorChooser.getColor();
    }
  }

  //Implement the one CellEditor method that AbstractCellEditor doesn't.
  public Object getCellEditorValue()
  {
    return currentColor;
  }

  //Implement the one method defined by TableCellEditor.
  public Component getTableCellEditorComponent(JTable table,
                                               Object value,
                                               boolean isSelected,
                                               int row,
                                               int column)
  {
    currentColor = (Color) value;
    return button;
  }
}
