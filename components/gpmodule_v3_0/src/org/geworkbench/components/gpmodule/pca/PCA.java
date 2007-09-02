package org.geworkbench.components.gpmodule.pca;

import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.tigr.microarray.mev.cluster.gui.impl.pca.Content3D;
import org.tigr.microarray.mev.cluster.gui.impl.pca.PCA2DViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.util.FloatMatrix;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;


@AcceptTypes({PCADataSet.class})
public class PCA extends MicroarrayViewEventBase
{
    private JTabbedPane tabbedPane;
    private JSplitPane compPanel;
    private JTable compResultsTable;
    private JSplitPane compGraphPanel;
    private JSplitPane projPanel;
    private JTable projResultsTable;
    private JScrollPane projGraphPanel;
   // private JTable projTablePanel;
    //private JTable resultsTable;


    private JTextField perVar;
    //private JSplitPane pcaPanel;
    private JButton createButton;
    private JButton clearPlotButton;

    private PCAData pcaData;
    private DSDataSet dataSet; 

    public PCA()
    {
        tabbedPane = new JTabbedPane();

        compPanel = new JSplitPane();
        compPanel.setOneTouchExpandable(true);
        compPanel.setDividerLocation(200);

        tabbedPane.addTab("Components", compPanel);
        tabbedPane.setSelectedComponent(compPanel);

        projPanel = new JSplitPane();
        projPanel.setOneTouchExpandable(true);
        projPanel.setDividerLocation(200);

        tabbedPane.addTab("Projection", projPanel);
        tabbedPane.addChangeListener( new PCAChangeListener());

        compGraphPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        compGraphPanel.setOneTouchExpandable(true);
        compGraphPanel.setDividerSize(8);
        compGraphPanel.setDividerLocation(0.6);

        compResultsTable = new JTable();
        compResultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        perVar = new JTextField();
        perVar.setMaximumSize(new Dimension(80, 100));

        createButton = new JButton("Create MA Set");
        createButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                int[] pcs = compResultsTable.getSelectedRows();

                CSExprMicroarraySet pcDataSet = new CSExprMicroarraySet();
                pcDataSet.readFromFile(dataSet.getFile());
                pcDataSet.setLabel("PCA_" + dataSet.getFile().getName());
                pcDataSet.clear();
                for(int i = 0; i < pcs.length; i++)
                {
                    pcDataSet.add((DSMicroarray)dataSet.get(i));
                }

                publishProjectNodeAddedEvent(new ProjectNodeAddedEvent("PCA_" + dataSet.getDataSetName(), pcDataSet, null));
                System.out.println("Event action: " + event.getActionCommand());
            }
        });

        compResultsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent event)
            {
                int[] selectedRows = compResultsTable.getSelectedRows();

                double sum = 0;

                for(int i = 0; i < selectedRows.length; i++)
                {
                    String value = ((String)compResultsTable.getValueAt(selectedRows[i], 2)).replace("%", "");
                    sum += Double.parseDouble(value);
                }

                perVar.setText(String.valueOf(sum));
                buildComponentsPanel(selectedRows);
            }
        });
       
        projGraphPanel = new JScrollPane();

        plotButton.setEnabled(false);
        plotButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                buildPlot(projResultsTable.getSelectedRows());
            }
        });

        clearPlotButton = new JButton("Clear Plot");
        clearPlotButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                projResultsTable.clearSelection();
                projGraphPanel.removeAll();
                projGraphPanel.repaint();
            }
        });

        projResultsTable = new JTable();
        projResultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        projResultsTable.setColumnSelectionAllowed(false);

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
                    plotButton.setEnabled(false);

            }
        });

        jToolBar3.remove(chkAllArrays);
        jToolBar3.remove(chkAllMarkers);
        buildJToolBar3();

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }
    /**
     * The component for the GUI engine.
     */
    public Component getComponent()
    {
        return mainPanel;
    }

    private void buildResultsTable()
    {       
        String[] columnNames = {"Id", "Eigen Value", "% Var"};
        TableModel tableModel = new DefaultTableModel(columnNames, pcaData.getNumPCs());

        for(int i=1; i <= pcaData.getNumPCs(); i++)
        {
            tableModel.setValueAt(i, i-1, 0);
            Map eigenValues = pcaData.getEigenValues();
            tableModel.setValueAt(eigenValues.get(Integer.valueOf(i)), i-1, 1);

            Map percentVars = pcaData.getPercentVars();
            tableModel.setValueAt(percentVars.get(Integer.valueOf(i)), i-1, 2);
        }

        compResultsTable.removeAll();
        compResultsTable.setModel(tableModel);
        compPanel.setDividerLocation(200);
        compPanel.setLeftComponent(new JScrollPane(compResultsTable));

        projResultsTable.removeAll();
        projResultsTable.setModel(tableModel);       
        projPanel.setDividerLocation(200);
        projPanel.setLeftComponent(new JScrollPane(projResultsTable));
    }

    private void buildEigenVectorsTable(int[] pComp)
    {
        JTable eigenVectorsTable = new JTable();
        DefaultTableModel tableModel = new DefaultTableModel();

        Map map = pcaData.getEigenVectors();
        tableModel.setColumnCount(((List)map.values().iterator().next()).size());
        for(int i = 0; i < pComp.length; i++)
        {
            int pc = pComp[i]+1;
            List eigenVector = new ArrayList((List)map.get(new Integer(pc)));
            eigenVector.add(0, "Comp " + pc);
            tableModel.addRow(new Vector(eigenVector));
        }

        Vector columnNames = new Vector();
        columnNames.addAll((Vector)tableModel.getDataVector().get(0));
        Collections.fill(columnNames, "");

        tableModel.setColumnIdentifiers(columnNames);
        eigenVectorsTable.setModel(tableModel);
        eigenVectorsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(eigenVectorsTable);
        compGraphPanel.setBottomComponent(scrollPane);
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
                xySeries.add(n+1, Double.parseDouble((String)eigenVector.get(n)));
            }

            xySeriesCollection.addSeries(xySeries);
        }

        JFreeChart lineGraph = ChartFactory.createXYLineChart
                (null, null, null, xySeriesCollection, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel panel = new ChartPanel(lineGraph);
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setMinimumSize(new Dimension(420, 270));

        compGraphPanel.setTopComponent(scrollPane);
    }

    private void buildPlot(int[] pComp)
    {
        CSExprMicroarraySet maSet = (CSExprMicroarraySet)dataSet;
        FloatMatrix fm  = new FloatMatrix(maSet.getMarkers().size(), pComp.length);
        
        ArrayList featuresList = new ArrayList();
        for(int i=0; i < pComp.length; i++)
        {
            DSMicroarray array = maSet.get(pComp[i]);
            float[] markerData = array.getRawMarkerData();


            for(int j = 0; j < markerData.length; j++)
            {  
                fm.set(j, i, markerData[j]);

                if(i == 0)
                {
                    SlideData slideData = new SlideData();
                    featuresList.add(slideData);
                }
            }
        }

		int[] column = new int[fm.getColumnDimension()];
        Color[] colors = new Color[column.length];
        for(int i=0; i < column.length; i ++ )
        {
            colors[i] = Color.RED;
            column[i] = i;
        }

        org.tigr.microarray.mev.cluster.gui.Experiment experiment =
                new org.tigr.microarray.mev.cluster.gui.Experiment(fm, column);

        MultipleArrayData multipleArrayData = new MultipleArrayData();           
        multipleArrayData.setExperimentColors(colors);
        multipleArrayData.setExperimentColorIndices(column);
        multipleArrayData.setFeaturesList(featuresList);

        int xAxis, yAxis, zAxis;
        xAxis = pComp[0];
        yAxis = pComp[1];
        if(pComp.length == 3)
        {

            zAxis = pComp[2];

            PCAContent3D content = new PCAContent3D(3, pcaData.getUMatrix(), experiment, false, xAxis, yAxis, zAxis);
            content.setData(multipleArrayData);
            content.setPointSize((float)0.9);
            content.setShowSpheres(true);
            content.draw();

            content.setMaximumSize(new Dimension(400, 280));
            projGraphPanel.setViewportView(content);
        }
        else
        {
            PCA2DViewer  pca2DViewer = new PCA2DViewer(experiment, pcaData.getUMatrix(), true, xAxis, yAxis);
            pca2DViewer.setData(multipleArrayData);
            projGraphPanel.setViewportView(pca2DViewer.getContentComponent());
        }

        projGraphPanel.repaint();
        projPanel.setRightComponent(projGraphPanel);
        projPanel.setDividerLocation(200);
        projPanel.repaint();
    }

    private void buildComponentsPanel(int[] pComp)
    {
        buildEigenVectorsTable(pComp);
        buildGraph(pComp);

        compPanel.setDividerLocation(200);
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
            buildResultsTable();
        }
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
        }
        else
            jToolBar3.add(createButton);

        jToolBar3.repaint();
    }

    private class PCAChangeListener implements ChangeListener
    {
        public void stateChanged(ChangeEvent event)
        {
            System.out.println("State change: " + event.getSource());

            if(event.getSource() instanceof JTabbedPane)
            {
                JTabbedPane pane = (JTabbedPane)event.getSource();

                int index = pane.getSelectedIndex();

                if(pane.getTitleAt(index).equals("Projection"))
                {
                    buildJToolBar3();
                }
                else
                {
                    buildJToolBar3();
                }
            }
        }
    }

    @Publish
    public ProjectNodeAddedEvent publishProjectNodeAddedEvent(ProjectNodeAddedEvent event)
    {
        return event;
    }

    private class PCAContent3D extends Content3D
    {
        public PCAContent3D(int mode, org.tigr.util.FloatMatrix floatMatrix, Experiment experiment, boolean view)
        {
            super(mode, floatMatrix, experiment, view);
        }

        public PCAContent3D(int mode, org.tigr.util.FloatMatrix floatMatrix, Experiment experiment, boolean view, int xAxis, int yAxis, int zAxis)
        {
            super(mode, floatMatrix, experiment, view, xAxis, yAxis, zAxis);
        }

        public void draw()
        {
            updateScene();
        }
    }
}
