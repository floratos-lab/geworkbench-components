package org.geworkbench.components.gpmodule.pca;

import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.*;
import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.jfree.chart.*;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.Range;
import org.tigr.util.FloatMatrix;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import org.geworkbench.components.gpmodule.pca.viewer.PCAContent3D;

import java.util.*;
import java.util.List;

/**
 * @author: Marc-Danie Nazaire
 */

@AcceptTypes({PCADataSet.class})
public class PCA extends MicroarrayViewEventBase
{
    private JTabbedPane tabbedPane;
    private JSplitPane compPanel;
    private JTable compResultsTable;
    private JSplitPane compGraphPanel;
    private JSplitPane projPanel;
    private JTable projResultsTable;
    private Component projGraphPanel;
    private JTextField perVar;
    private JButton createButton;
    private JButton clearPlotButton;
    private JButton imageSnapshotButton;

    private PCAData pcaData;
    private DSDataSet dataSet;

    private JScrollPane mainScrollPane;
    private Map dataLabelGroups;
    private PCAContent3D pcaContent3D;

    public PCA()
    {
        tabbedPane = new JTabbedPane();
        
        mainScrollPane = new JScrollPane();
        tabbedPane.setMinimumSize(mainScrollPane.getSize());
        tabbedPane.setMaximumSize(mainScrollPane.getSize());
        tabbedPane.setPreferredSize(mainScrollPane.getSize());

        compPanel = new JSplitPane();
        tabbedPane.addTab("Components", compPanel);
        tabbedPane.setSelectedComponent(compPanel);

        compGraphPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        compPanel.setRightComponent(compGraphPanel);

        projPanel = new JSplitPane();
        tabbedPane.addTab("Projection", projPanel);
        tabbedPane.addChangeListener( new PCAChangeListener(tabbedPane.getSelectedIndex()));

        projGraphPanel = new Container();
        projPanel.setRightComponent(projGraphPanel);

        perVar = new JTextField();
        perVar.setMaximumSize(new Dimension(75, 25));
        perVar.setMinimumSize(new Dimension(75, 25));
        perVar.setPreferredSize(new Dimension(75, 25));

        createButton = new JButton("Create MA Set");
        createButton.setEnabled(false);
        createButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                int[] pcs = compResultsTable.getSelectedRows();

                if(pcs.length == 0)
                {
                    JOptionPane.showMessageDialog(null, "No principal components selected");
                    return;
                }

                CSExprMicroarraySet pcDataSet = new CSExprMicroarraySet();
                pcDataSet.readFromFile(dataSet.getFile());
                pcDataSet.setLabel("PCA_" + dataSet.getFile().getName());
                pcDataSet.clear();
                for(int i = 0; i < pcs.length; i++)
                {
                    pcDataSet.add((DSMicroarray)dataSet.get(i));
                }

                publishProjectNodeAddedEvent(new ProjectNodeAddedEvent("PCA_" + dataSet.getDataSetName(), pcDataSet, null));
            }
        });

        compResultsTable = new JTable()
        {
            public boolean getScrollableTracksViewportHeight()
            {
                Component parent = getParent();
                if(parent instanceof JViewport)
                   return parent.getHeight() > getPreferredSize().height;

                return false;
            }
        };
        compResultsTable.setRowSelectionAllowed(true);
        compResultsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent event)
            {
                if(!event.getValueIsAdjusting())
                {
                    int[] selectedRows = compResultsTable.getSelectedRows();

                    if(selectedRows.length == 0)
                    {
                        reset();

                        perVar.setText("");
                        createButton.setEnabled(false);
                        return;
                    }
                    double sum = 0;

                    for(int i = 0; i < selectedRows.length; i++)
                    {
                        String value = ((String)compResultsTable.getValueAt(selectedRows[i], 2)).replace("%", "");
                        sum += Double.parseDouble(value);
                    }

                    perVar.setText(String.valueOf(sum));
                    createButton.setEnabled(true);
                    buildComponentsPanel(selectedRows);
                }
            }
        });

        plotButton.setEnabled(false);
        plotButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                buildPlot(projResultsTable.getSelectedRows());
                clearPlotButton.setEnabled(true);
                imageSnapshotButton.setEnabled(true);
            }
        });

        clearPlotButton = new JButton("Clear Plot");
        clearPlotButton.setEnabled(false);
        clearPlotButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                projResultsTable.clearSelection();

                if(projGraphPanel instanceof JScrollPane)
                {
                    ((JScrollPane)projGraphPanel).getViewport().removeAll();
                    projGraphPanel.repaint();
                }
                else
                    ((ScrollPane)projGraphPanel).removeAll();

                plotButton.setEnabled(false);
                clearPlotButton.setEnabled(false);
                imageSnapshotButton.setEnabled(false);
            }
        });

        imageSnapshotButton = new JButton("Image Snapshot");
        imageSnapshotButton.setEnabled(false);
        imageSnapshotButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                Component component;
                if(projGraphPanel instanceof JScrollPane)
                {
                    component = ((JScrollPane)projGraphPanel).getViewport().getComponent(0);
                }
                else
                    component = ((ScrollPane)projGraphPanel).getComponent(0);

                BufferedImage graphImage = null;
                if(component instanceof ChartPanel)
                {
                    graphImage = ((ChartPanel)component).getChart().createBufferedImage(component.getWidth(), component.getHeight());
                }
                else
                {
                    graphImage = ((PCAContent3D)component).createImage();
                }

                ImageIcon newIcon = new ImageIcon(graphImage, "PCA Image");
                org.geworkbench.events.ImageSnapshotEvent imageEvent = new org.geworkbench.events.ImageSnapshotEvent("Color Mosaic View", newIcon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
                publishImageSnapshotEvent(imageEvent);
            }
        });

        projResultsTable = new JTable()
        {
            public boolean getScrollableTracksViewportHeight()
            {
                Component parent = getParent();
                if(parent instanceof JViewport)
                   return parent.getHeight() > getPreferredSize().height;

                return false;
            }
        };
        
        projResultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        projResultsTable.setColumnSelectionAllowed(false);
        projResultsTable.setRowSelectionAllowed(true);
        projPanel.setLeftComponent(projResultsTable);

        projResultsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent event)
            {
                int[] selectedRows = projResultsTable.getSelectedRows();

                double sum = 0;

                for(int i = 0; i < selectedRows.length; i++)
                {
                    String value = ((String)projResultsTable.getValueAt(selectedRows[i], 2)).replace("%", "");
                    sum += Double.parseDouble(value);
                }

                perVar.setText(String.valueOf(sum));

                if(selectedRows.length == 4)
                {
                    projResultsTable.removeRowSelectionInterval(selectedRows[0], selectedRows[0]);
                }

                if(selectedRows.length >=2)
                {
                    plotButton.setEnabled(true);
                }
                else
                {
                    plotButton.setEnabled(false);
                }

            }
        });

        onlyActivatedMarkers = false;
        onlyActivatedArrays = false;

        jToolBar3.remove(chkAllArrays);
        jToolBar3.remove(chkAllMarkers);

        chkAllArrays.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                onlyActivatedMarkers = !chkAllArrays.isSelected();
            }
        });

        chkAllMarkers.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                onlyActivatedMarkers = !chkAllMarkers.isSelected();
            }
        });

        onlyActivatedMarkers = false;
        
        buildJToolBar3();
    }

    private void reset()
    {
        compGraphPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        compGraphPanel.setOneTouchExpandable(true);
        compGraphPanel.setDividerLocation(0.5);

        compResultsTable.removeAll();
        compPanel.setLeftComponent(compResultsTable);
        compPanel.setRightComponent(compGraphPanel);

         if(projGraphPanel instanceof JScrollPane)
         {
            ((JScrollPane)projGraphPanel).getViewport().removeAll();
         }

         if(projGraphPanel instanceof ScrollPane)
            ((ScrollPane)projGraphPanel).removeAll();

        projResultsTable.removeAll();
        projPanel.setLeftComponent(projResultsTable);
        projPanel.setRightComponent(projGraphPanel);
        
        compPanel.setOneTouchExpandable(true);
        compPanel.setDividerLocation(0.25);
        tabbedPane.setSelectedComponent(compPanel);

        projPanel.setOneTouchExpandable(true);
        projPanel.setDividerLocation(0.25);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        mainScrollPane.setViewportView(mainPanel);    
    }

    /*
     * check if Java 3D is available
     */
    private boolean hasJava3D()
    {
		try
        {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			classLoader.loadClass("com.sun.j3d.utils.universe.SimpleUniverse");
			Package p = Package.getPackage("javax.media.j3d");

            if(p != null)
            {
                Map vuMap = javax.media.j3d.VirtualUniverse.getProperties();
                System.out.println("Vendor: " + vuMap.get("j3d.vendor"));
                System.out.println("Vendor version: " + vuMap.get("j3d.version"));
                System.out.println("Renderer: " + vuMap.get("j3d.renderer"));
                return true;
			}
        }
        catch(Exception e)
        {
			return false;
		}

        return false;
    }
    /**
     * The component for the GUI engine.
     */
    public Component getComponent()
    {
        return mainScrollPane;
    }

    private void buildResultsTable()
    {
        String[] columnNames = {"Id", "Eigen Value", "% Var"};
        TableModel tableModel = new DefaultTableModel(columnNames, pcaData.getNumPCs()){
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return false;
            }
        };

        for(int i=1; i <= pcaData.getNumPCs(); i++)
        {
            tableModel.setValueAt(i, i-1, 0);
            Map eigenValues = pcaData.getEigenValues();
            tableModel.setValueAt(eigenValues.get(Integer.valueOf(i)), i-1, 1);

            Map percentVars = pcaData.getPercentVars();
            tableModel.setValueAt(percentVars.get(Integer.valueOf(i)), i-1, 2);
        }
       
        compResultsTable.setModel(tableModel);
        JScrollPane compPane = new JScrollPane();
        compPane.setViewportView(compResultsTable);
        compPanel.setLeftComponent(compPane);
        compPanel.setDividerLocation(0.25);
       
        projResultsTable.setModel(tableModel);
        JScrollPane projPane = new JScrollPane();
        projPane.setViewportView(projResultsTable);
        projPanel.setDividerLocation(0.25);
        projPanel.setLeftComponent(projPane);
    }

    private void buildEigenVectorsTable(int[] pComp)
    {
        if(pComp == null || pComp.length == 0)
        {
            System.err.println("No principal components found");
            return;
        }

        JTable eigenVectorsTable = new JTable()
        {
            public boolean getScrollableTracksViewportHeight()
            {
                Component parent = getParent();
                if(parent instanceof JViewport)
                   return parent.getHeight() > getPreferredSize().height;

                return false;
            }
        };

        DefaultTableModel tableModel = new DefaultTableModel()
        {
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return false;
            }
        };
        
        Map map = pcaData.getEigenVectors();
        tableModel.setColumnCount(((List)map.values().iterator().next()).size()+1);
        for(int i = 0; i < pComp.length; i++)
        {
            int pc = pComp[i]+1;
            List eigenVector = new ArrayList();
            eigenVector.add(0, "Prin. Comp. " + pc);
            eigenVector.addAll((List)map.get(new Integer(pc)));

            tableModel.addRow(new Vector(eigenVector));
        }

        Vector columnNames = new Vector();
        columnNames.addAll((Vector)tableModel.getDataVector().get(0));
        Collections.fill(columnNames, " ");

        tableModel.setColumnIdentifiers(columnNames);
        eigenVectorsTable.setModel(tableModel);
        eigenVectorsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        eigenVectorsTable.getColumnModel().getColumn(0).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane();
        compGraphPanel.setBottomComponent(scrollPane);
        compGraphPanel.setDividerLocation(0.5);

        scrollPane.setViewportView(eigenVectorsTable);
    }

    public void buildGraph(int[] pComp)
    {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

        Map map = pcaData.getEigenVectors();
        for(int i = 0; i < pComp.length; i++)
        {
            int pc = pComp[i]+1;
            List eigenVector = new ArrayList((List)map.get(new Integer(pc)));

            XYSeries xySeries = new XYSeries("Prin. Comp. " + pc);
            for(int n = 0; n < eigenVector.size(); n++)
            {
                String ev = (String)eigenVector.get(n);
                ev = ev.replaceAll(",", "");
                xySeries.add(n+1, Double.parseDouble(ev));
            }

            xySeriesCollection.addSeries(xySeries);
        }

        JFreeChart lineGraph = ChartFactory.createXYLineChart
                (null, null, null, xySeriesCollection, PlotOrientation.VERTICAL, true, true, false);
        
        ChartPanel panel = new ChartPanel(lineGraph);
        JScrollPane scrollPane = new JScrollPane(panel);
      
        compGraphPanel.setTopComponent(scrollPane);
        compGraphPanel.setDividerLocation(0.5);
    }

    private void buildPlot(int[] pComp)
    {
        CSExprMicroarraySet maSet = (CSExprMicroarraySet)dataSet;
        List dataLabelList = new ArrayList();
        FloatMatrix u_Matrix = null;
        dataLabelGroups = new HashMap();

        if(pcaData.getVariables().equals("experiments"))
        {
            for(int i = 0; i < maSet.size(); i++)
            {
                dataLabelList.add(maSet.get(i).getLabel());
            }
        }
        else
        {
            for(int i = 0; i < maSet.getMarkers().size(); i++)
            {
                dataLabelList.add(maSet.getMarkers().get(i).getLabel());
            }
        }

        if(pcaData.getVariables().equals("experiments") && onlyActivatedMarkers)
        {
            if(activatedMarkers == null || activatedMarkers.size() == 0)
            {
                JOptionPane.showMessageDialog(mainPanel, "No markers selected");
                return;
            }

            DSAnnotationContext<DSGeneMarker> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet.getMarkerVector());

            for(int i =0; i < activatedMarkers.size(); i++)
            {
                DSGeneMarker marker = activatedMarkers.get(i);
                String[] label = context.getLabelsForItem(marker);

                if(label != null && label.length > 0)
                {
                    Set set = (Set)dataLabelGroups.get(label[0]);
                    if(set == null)
                        set = new LinkedHashSet();

                    set.add(marker.getLabel());

                    dataLabelGroups.put(label[0], set);
                }
            }
        }
        else if(pcaData.getVariables().equals("genes") && onlyActivatedArrays)
        {
            if(activatedArrays == null || activatedArrays.size() == 0)
            {
                JOptionPane.showMessageDialog(mainPanel, "No arrays selected");
                return;
            }

             DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);

            for(int i =0; i < activatedArrays.size(); i++)
            {
                DSMicroarray array = (DSMicroarray)activatedArrays.get(i);
                String[] label = context.getLabelsForItem(array);

                if(label != null && label.length > 0)
                {
                    Set set = (Set)dataLabelGroups.get(label[0]);
                    if(set == null)
                        set = new LinkedHashSet();
                    set.add(array.getLabel());

                    dataLabelGroups.put(label[0], set);
                }
            }
        }
        else
        {
            dataLabelGroups.put("group 1", new LinkedHashSet(dataLabelList));
        }

        u_Matrix = pcaData.getUMatrix();

        // build 3D projection plot
        if(pComp.length == 3)
        {
            if(!hasJava3D())
            {
                JOptionPane.showMessageDialog(null, "Java3D is not installed. " +
                        "\nPlease install Java3D and restart geWorkbench. " +
                        "\nFor details about downloading and installing Java3D go to https://java3d.dev.java.net");
                return;
            }
            
            int pc1 = pComp[0]+1;
            int pc2 = pComp[1]+1;
            int pc3 = pComp[2]+1;
            List data = new ArrayList();

            Set dataGroups = dataLabelGroups.keySet();
            Iterator it = dataGroups.iterator();
            while(it.hasNext())
            {
                String group = (String)it.next();

                Set labels = (Set)dataLabelGroups.get(group);
                Iterator labelsIt = labels.iterator();

                while(labelsIt.hasNext())
                {
                   String label = (String)labelsIt.next();
                    int row = dataLabelList.indexOf(label);

                    PCAContent3D.XYZData xyzData = new PCAContent3D.XYZData(u_Matrix.get(row, pc1-1),
                            u_Matrix.get(row, pc2-1), u_Matrix.get(row, pc3-1), label);
                    xyzData.setCluster(group);
                    data.add(xyzData);
                }
            }

            pcaContent3D = new PCAContent3D(data);
            pcaContent3D.setPointSize((float)1.4);
            pcaContent3D.setXAxisLabel("Prin. Comp. " + pc1);
            pcaContent3D.setYAxisLabel("Prin. Comp. " + pc2);
            pcaContent3D.setZAxisLabel("Prin. Comp. " + pc3);
           
            pcaContent3D.updateScene();
            pcaContent3D.getComponent(0).addMouseListener(new PCA3DMouseListener());

            projGraphPanel = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
            
            ((ScrollPane)projGraphPanel).add(pcaContent3D);
            projPanel.setRightComponent(projGraphPanel);
        }
        else  //build 2D Projection plot
        {
            int pc1 = pComp[0]+1;

            int pc2 = pComp[1]+1;
            JFreeChart graph = ChartFactory.createScatterPlot
                       ("2D Projection", "Prin. Comp. " + pc1, "Prin. Comp. " + pc2, null, PlotOrientation.VERTICAL, true, false, false);

            XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

            Set dataGroups = dataLabelGroups.keySet();
            Iterator it = dataGroups.iterator();
            while(it.hasNext())
            {
                String group = (String)it.next();
                XYSeries xySeries = new XYSeries("", false, true);                

                if(dataGroups.size() > 1)
                    xySeries.setKey(group);

                Set labels = (Set)dataLabelGroups.get(group);
                Iterator labelsIt = labels.iterator();

                while(labelsIt.hasNext())
                {
                    String label = (String)labelsIt.next();
                    int row = dataLabelList.indexOf(label);

                    XYDataItem item = new XYDataItem(u_Matrix.get(row, pc1-1), u_Matrix.get(row, pc2-1));
                    xySeries.add(item);
                }

                xySeriesCollection.addSeries(xySeries);
            }
         
            graph.getXYPlot().setDataset(xySeriesCollection);
            graph.getXYPlot().getRangeAxis().setTickMarksVisible(true);
            graph.getXYPlot().getRangeAxis().setTickMarkPaint(Color.BLACK);          

           set2DPlotBounds(graph, true, true);

            graph.getXYPlot().addRangeMarker(new ValueMarker(0.0, Color.BLACK, new BasicStroke((float)1.4)));
            graph.getXYPlot().addDomainMarker(new ValueMarker(0.0, Color.BLACK, new BasicStroke((float)1.4)));


            graph.getXYPlot().setDomainGridlinesVisible(false);

            graph.getXYPlot().getRenderer().setToolTipGenerator( new StandardXYToolTipGenerator()
            {
                public String generateToolTip(XYDataset data, int series, int item)
                {
                    XYSeries xySeries = ((XYSeriesCollection)data).getSeries(series);
                    String key = (String)xySeries.getKey();

                    if(key.equals(""))
                        key = "group 1";
                    Set labels = (Set)dataLabelGroups.get(key);

                    Iterator it = labels.iterator();
                    int i = 0;
                    while(i < item)
                    {
                        it.next();
                        i++;
                    }

                   String result = "[" + xySeries.getDataItem(item).getX() + ", " + xySeries.getDataItem(item).getY() + "]";
                   if(it != null)
                   {
                       result = it.next() + " : " +  result;
                   }

                    return result;
                }
            });

            ChartPanel panel = new ChartPanel(graph)
            {
                public void restoreAutoBounds()
                {
                    super.restoreAutoBounds();
                    set2DPlotBounds(getChart(), true, true);
                }

                public void restoreAutoDomainBounds()
                {
                    super.restoreAutoDomainBounds();
                    set2DPlotBounds(getChart(), true, false);
                }

                public void restoreAutoRangeBounds()
                {
                    super.restoreAutoRangeBounds();
                    set2DPlotBounds(getChart(), false, true);
                }
            };

            panel.addChartMouseListener(new PCAChartMouseListener());

            projGraphPanel = new JScrollPane();
            ((JScrollPane)projGraphPanel).setViewportView(panel);
            projPanel.setRightComponent(projGraphPanel); 
        }

        projPanel.setDividerLocation(0.25);
    }

    private void set2DPlotBounds(JFreeChart chart, boolean setDomain, boolean setRange)
    {
         if(setDomain)
         {
             Range domainRange = chart.getXYPlot().getDomainAxis().getRange();
            double maxDomainRange = Math.max(Math.abs(domainRange.getLowerBound()), domainRange.getUpperBound());
            chart.getXYPlot().getDomainAxis().setLowerBound(-maxDomainRange);
            chart.getXYPlot().getDomainAxis().setUpperBound(maxDomainRange);
         }

        if(setRange)
        {
            Range range =  chart.getXYPlot().getRangeAxis().getRange();
            double maxRange = Math.max(Math.abs(range.getLowerBound()), range.getUpperBound());
            chart.getXYPlot().getRangeAxis().setLowerBound(-maxRange);
            chart.getXYPlot().getRangeAxis().setUpperBound(maxRange);
        }
    }

    private void buildComponentsPanel(int[] pComp)
    {
        buildEigenVectorsTable(pComp);
        buildGraph(pComp);

        compPanel.setDividerLocation(0.25);
        compPanel.setRightComponent(compGraphPanel);
    }

    @Subscribe
    public void receive(ProjectEvent e, Object source)
    {
        if(e.getDataSet() instanceof PCADataSet)
        {
            PCADataSet pcaDataSet = ((PCADataSet)e.getDataSet());
            pcaData = pcaDataSet.getData();
            dataSet = pcaDataSet.getParentDataSet();

            reset();
            buildResultsTable();
        }
        else
            reset();
    }

    private void buildJToolBar3()
    {
        jToolBar3.removeAll();

        jToolBar3.add(new JLabel("% Var"));
        jToolBar3.add(perVar);
        jToolBar3.addSeparator();

        int viewIndex = tabbedPane.getSelectedIndex();
        if(tabbedPane.getTitleAt(viewIndex).equals("Projection"))
        {
            jToolBar3.add(plotButton);
            jToolBar3.addSeparator();
            jToolBar3.add(clearPlotButton);
            jToolBar3.addSeparator(new Dimension(34, 0));
            jToolBar3.add(imageSnapshotButton);
            jToolBar3.add(Box.createHorizontalGlue());

            if(pcaData.getVariables().equals("genes"))
            {
                chkAllArrays.setSelected(true);
                onlyActivatedMarkers = false;
                jToolBar3.add(chkAllArrays);
            }
            else
            {
                chkAllMarkers.setSelected(true);
                onlyActivatedArrays = false;
                jToolBar3.add(chkAllMarkers);
            }
        }
        else
            jToolBar3.add(createButton);

        jToolBar3.repaint();
    }

    private class PCAChangeListener implements ChangeListener
    {
        int lastSelectedIndex;

        public PCAChangeListener(int lastSelectedIndex)
        {
            this.lastSelectedIndex = lastSelectedIndex;
        }

        public void stateChanged(ChangeEvent event)
        {
            if(event.getSource() instanceof JTabbedPane)
            {
                if(lastSelectedIndex != tabbedPane.getSelectedIndex())
                {
                    buildJToolBar3();
                    lastSelectedIndex = tabbedPane.getSelectedIndex();
                }
            }
        }
    }

    private class PCA3DMouseListener implements MouseListener
    {
        public void mouseClicked(MouseEvent event)
        {
            String label = pcaContent3D.getSelectedPoint();
            if(label == null)
                return;
            if(pcaData.getVariables().equals("genes"))
            {
                DSMicroarray microarray = ((CSExprMicroarraySet)dataSet).getMicroarrayWithId(label);
                if (microarray != null)
                {
                    PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(microarray);
                    publishPhenotypeSelectedEvent(pse);
                }
            }
            else
            {
                DSGeneMarker marker = ((CSExprMicroarraySet)dataSet).getMarkers().get(label);
                if (marker != null)
                {
                    MarkerSelectedEvent mse = new org.geworkbench.events.MarkerSelectedEvent(marker);
                    publishMarkerSelectedEvent(mse);
                }
            }
        }

        public void mousePressed(MouseEvent event)
        {
        }

        public void mouseReleased(MouseEvent event)
        {
        }

        public void mouseEntered(MouseEvent event)
        {
        }

        public void mouseExited(MouseEvent event)
        {
        }
    }

    @Publish
    public ProjectNodeAddedEvent publishProjectNodeAddedEvent(ProjectNodeAddedEvent event)
    {
        return event;
    }

    @Publish
    public org.geworkbench.events.ImageSnapshotEvent publishImageSnapshotEvent
            (org.geworkbench.events.ImageSnapshotEvent
                    event) {
        return event;
    }

    private class PCAChartMouseListener implements ChartMouseListener
    {
        public void chartMouseClicked(ChartMouseEvent event)
        {
            ChartEntity entity = event.getEntity();
            if ((entity != null) && (entity instanceof XYItemEntity))
            {
                XYItemEntity xyEntity = (XYItemEntity) entity;
                int series = xyEntity.getSeriesIndex();
                int item = xyEntity.getItem();

                XYSeries xySeries = ((XYSeriesCollection)xyEntity.getDataset()).getSeries(series);
                String key = (String)xySeries.getKey();

                if(key.equals(""))
                    key = "group 1";
                Set labels = (Set)dataLabelGroups.get(key);

                Iterator it = labels.iterator();
                int i = 0;
                while(i < item)
                {
                    it.next();
                    i++;
                }

                String label = (String)it.next();

                if(pcaData.getVariables().equals("genes"))
                {
                    DSMicroarray microarray = ((CSExprMicroarraySet)dataSet).getMicroarrayWithId(label);
                    if (microarray != null)
                    {
                        PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(microarray);
                        publishPhenotypeSelectedEvent(pse);
                    }
                }
                else
                {
                    DSGeneMarker marker = ((CSExprMicroarraySet)dataSet).getMarkers().get(label);
                    if (marker != null)
                    {
                        MarkerSelectedEvent mse = new org.geworkbench.events.MarkerSelectedEvent(marker);
                        publishMarkerSelectedEvent(mse);
                    }
                }
            }
        }


        public void chartMouseMoved(ChartMouseEvent event)
        {
            // No-op
        }
    }

    @Publish
    public org.geworkbench.events.MarkerSelectedEvent publishMarkerSelectedEvent(
            MarkerSelectedEvent event) {
        return event;
    }

    @Publish
    public PhenotypeSelectedEvent publishPhenotypeSelectedEvent(
            PhenotypeSelectedEvent event) {
        return event;
    }
}
