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
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.tigr.microarray.mev.cluster.gui.impl.pca.Content3D;
import org.tigr.microarray.mev.cluster.gui.impl.pca.PCA2DViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.util.FloatMatrix;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
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
    private JTextField perVar;
    private JButton createButton;
    private JButton clearPlotButton;
    private JButton imageSnapshotButton;

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
        compPanel.setRightComponent(compGraphPanel);

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
        projPanel.setRightComponent(projGraphPanel);

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
                projGraphPanel.getViewport().removeAll();
            }
        });

        imageSnapshotButton = new JButton("Image Snapshot");
        imageSnapshotButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                Component component = projGraphPanel.getViewport().getComponent(0);

                BufferedImage graphImage = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
                if(component instanceof PCA2DViewer)
                {
                    component.paint(graphImage.createGraphics());
                }
                else
                {
                    graphImage = ((PCAContent3D)component).createImage();  
                }

                ImageIcon newIcon = new ImageIcon(graphImage, "PCA View");
                org.geworkbench.events.ImageSnapshotEvent imageEvent = new org.geworkbench.events.ImageSnapshotEvent("Color Mosaic View", newIcon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
                publishImageSnapshotEvent(imageEvent);
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

        chkAllMarkers.setSelected(true);
        chkAllMarkers.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                onlyActivatedMarkers = !chkAllMarkers.isSelected();
            }
        });

        onlyActivatedMarkers = false;
        
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
        compPanel.setLeftComponent(new JScrollPane(compResultsTable));
        compPanel.setDividerLocation(200);

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

        FloatMatrix u_Matrix = null;
        List markerList = null;

        if(onlyActivatedMarkers)
        {
            if(activatedMarkers == null || activatedMarkers.isEmpty())
            {
                JOptionPane.showMessageDialog(mainPanel, "No markers selected");
                return;
            }

            markerList = activatedMarkers;
            u_Matrix = new FloatMatrix(activatedMarkers.size(), pcaData.getNumPCs());
        }
        else
        {
            markerList = maSet.getMarkers();
            u_Matrix = pcaData.getUMatrix();
        }

        FloatMatrix expFM  = new FloatMatrix(markerList.size(), pcaData.getNumPCs());

        ArrayList featuresList = new ArrayList();
        for(int i=0; i < expFM.getColumnDimension(); i++)
        {
            DSMicroarray array = maSet.get(i);

            for(int j = 0; j < markerList.size(); j++)
            {
                DSGeneMarker marker = (DSGeneMarker)markerList.get(j);
                CSMarkerValue markerValue = (CSMarkerValue)array.getMarkerValue(marker);
                expFM.set(j, i, new Float(markerValue.getValue()).floatValue());

                if(onlyActivatedMarkers)
                {
                    u_Matrix.set(j, i, pcaData.getUMatrix().get(maSet.getMarkers().indexOf(marker), i));
                }

                if(i == 0)
                {
                    SlideData slideData = new SlideData();
                    slideData.setSlideDataName(marker.getLabel());
                    featuresList.add(slideData);
                }
            }
        }
    

        int[] column = new int[expFM.getColumnDimension()];
        //Color[] colors = new Color[column.length];
        ArrayList colors = new ArrayList();
        for(int i=0; i < column.length; i ++ )
        {
            //colors[i] = Color.RED;
            colors.add(Color.RED);
            column[i] = i;
        }

        org.tigr.microarray.mev.cluster.gui.Experiment experiment =
                new org.tigr.microarray.mev.cluster.gui.Experiment(expFM, column);

        PCIData multipleArrayData = new PCIData(experiment);
        multipleArrayData.setFeaturesList(featuresList);
        multipleArrayData.setExperimentColors(colors);
        multipleArrayData.setExperimentColorIndices(column);
        
        int xAxis, yAxis, zAxis;
        xAxis = pComp[0];
        yAxis = pComp[1];
        if(pComp.length == 3)
        {
            zAxis = pComp[2];

            PCAContent3D content = new PCAContent3D(1, u_Matrix, experiment, false, xAxis, yAxis, zAxis);
            content.setData(multipleArrayData);
            content.setPointSize((float)0.95);
            content.setShowSpheres(true);
            content.draw();

            content.setMaximumSize(new Dimension(400, 280));
            projGraphPanel.setViewportView(content);
        }
        else
        {
            PCA2DViewer  pca2DViewer = new PCA2DViewer(experiment, u_Matrix, false, xAxis, yAxis);
            pca2DViewer.setData(multipleArrayData);
            pca2DViewer.getContentComponent().repaint();
            projGraphPanel.setViewportView(pca2DViewer.getContentComponent());
        }
        
        projPanel.setRightComponent(projGraphPanel);
        projPanel.setDividerLocation(200);
        projPanel.repaint();

        System.out.println("Activated Markers: " + super.activatedMarkers);
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
            jToolBar3.addSeparator(new Dimension(80, 10));
            jToolBar3.add(imageSnapshotButton);
            jToolBar3.addSeparator(new Dimension(270, 10));
            jToolBar3.add(chkAllMarkers);
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

    @Publish
    public org.geworkbench.events.ImageSnapshotEvent publishImageSnapshotEvent
            (org.geworkbench.events.ImageSnapshotEvent
                    event) {
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

        public void
        draw()
        {
            updateScene();
        }
    }

    private class PCIData implements IData {

            private ArrayList featuresList = new ArrayList();
            private ArrayList indicesList  = new ArrayList(); // array of int[]'s

            private int[] colorIndices;
            private ArrayList experimentColors = new ArrayList(); //array of experiment colors
            private int [] experimentColorIndices;

            private Experiment experiment = null;
            private Map markerClusters;

            public PCIData(Experiment experiment)
            {
                this.experiment = experiment;
            }

            /**
            * Returns the experiment data (ratio values).
            * @see Experiment
            */
           public Experiment getExperiment()
           {
                return experiment;
           }

           /**
            * Returns the experiment data (ratio values) without application of cutoffs.
            * @see Experiment
            */
           public Experiment getFullExperiment()
           {
               return null;
           }

           /**
            * Returns count of features.
            */
           public int getFeaturesCount()
           {
               return featuresList.size();
           }


            /**
            * Sets the features.
            */
           public void setFeaturesList(ArrayList featuresList)
           {
               this.featuresList = featuresList;
           }

           /**
            * Returns size of features.
            */
           public int getFeaturesSize()
           {
               return featuresList.size();
           }

           /**
            * Retruns the indicated feature
            */
           public ISlideData getFeature(int index)
           {
               return null;
           }

           /**
            * Returns the indicated ISlideDataElement
            */
           public ISlideDataElement getSlideDataElement(int row, int col)
           {
                return null;
           }

           /**
            * Returns the integer identifying the type of input data
            */
           public int getDataType()
           {
               return -1;
           }

           /**
            * Returns CY3 value.
            */
           public float getCY3(int column, int row)
           {
               return -1;
           }

           /**
            * Returns CY5 value.
            */
           public float getCY5(int column, int row)
           {
               return -1;
           }

           /**
            * Returns max CY3 value.
            */
           public float getMaxCY3()
           {
               return -1;
           }

           /**
            * Returns max CY5 value.
            */
           public float getMaxCY5()
           {
               return -1;
           }

           /**
            * Returns ratio value.
            */
           public float getRatio(int column, int row, int logState)
           {
               return -1;
           }

           /**
            * Returns min ratio value.
            */
           public float getMinRatio()
           {
               return -1;
           }

           /**
            * Returns max ratio value
            */
           public float getMaxRatio()
           {
               return -1;
           }

           /**
            * Returns feature name.
            */
           public String getSampleName(int column)
           {
               return null;
           }

           /**
            * Returns the slected sample annotation
            */
           public String getSampleAnnotation(int column, String key)
           {
               return null;
           }

           /**
            * Returns full feature name.
            */
           public String getFullSampleName(int column)
           {
               return null;
           }

           /**
            * Sets the experiment label index for the collection of features
            */
           public void setSampleLabelKey(String key)
           {

           }

           /**
            * Returns an element attribute.
            */
           public String getElementAttribute(int row, int attr)
           {
               return null;
           }

           /**
            * Returns a probe column in micro array.
            */
           public int getProbeColumn(int column, int row)
           {
               return -1;
           }

           /**
            * Returns a probe row in micro array.
            */
           public int getProbeRow(int column, int row)
           {
               return -1;
           }

           /**
            * Returns a gene unique id.
            */
           public String getUniqueId(int row)
           {
               return null;
           }

           /**
            * Returns a gene name.
            */
           public String getGeneName(int row)
           {
               return null;
           }

           /**
            *Returns all the annotation fields
            */

           public String[] getFieldNames()
           {
               return null;
           }

           /**
            *Returns all annotation field names associated with the loaded samples
            */
           public Vector getSampleAnnotationFieldNames()
           {
               return null;
           }

           /**
            * Returns sorted indices for specified column.
            */
           public int[] getSortedIndices(int column)
           {
               return null;
           }

           /**
            * Returns array of published colors.
            */
           public Color[] getColors()
           {
               return null;
           }

           /**
            * Delete all the published colors.
            */
           public void deleteColors()
           {

           }

           /**
            * Returns public color by specified row.
            */
           public Color getProbeColor(int row)
           {
               return null;
           }

           /**
            * Sets public color for specified rows.
            */
           public void setProbesColor(int[] rows, Color color)
           {

           }

           /**
            * Returns index of the public color for specified row.
            */
           public int getProbeColorIndex(int row)
           {
               return -1;
           }

           /**
            * Returns probe color indices
            */
           public int[] getColorIndices()
           {
               return null;
           }

           /**
            * Returns count of rows which have public color index equals to colorIndex.
            */
           public int getColoredProbesCount(int colorIndex)
           {
               return -1;
           }

           /**
            * Delete all the published experiment colors.
            */
           public void deleteExperimentColors()
           {

           }

           public void setExperimentColors(ArrayList colors)
           {
                this.experimentColors = colors;   
           }
           /**
            * Returns color for specified column data
            */
           public Color getExperimentColor(int col)
           {

                if(experimentColors == null)
                    return null;

                // return (Color)experimentColors.get(col);

               Random rand = new Random(12345);
                Color c = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));

               return c;
           }

           /**
            * Sets color for specified experiment indices
            */
           public void setExperimentColor(int [] indices, Color color)
           {

           }

           /**
            * Returns index of the public experiment color for specified row.
            */
           public int getExperimentColorIndex(int row)
           {
               return -1;
           }

            public void setExperimentColorIndices(int[] eci)
            {
                experimentColorIndices = eci;
            }

           /**
            * Returns experiment color indices
            */
           public int[] getExperimentColorIndices()
           {
               return null;
           }

           /**
            * Returns count of rows which have public color index equals to colorIndex.
            */
           public int getColoredExperimentsCount(int colorIndex)
           {
               return -1;
           }

           /**
            * Returns array of published colors.
            */
           public Color[] getExperimentColors()
           {
               return null;
           }

           /**
            * Returns an annotation array for the provided indices based on annotation key
            */
           public String [] getAnnotationList(String fieldName, int [] indices)
           {
               return null;
           }

           /**
            * Returns true if loaded intensities are known to be median
            */
           public boolean areMedianIntensities()
           {
               return false;
           }

           /**
            * Sets median intensity flag
            */
           public void setMedianIntensities(boolean areMedians)
           {

           }

           /**
            * Returns size of features.
            */
           public int getFeaturesSize(int chromosome)
           {
               return -1;
           }

           /**
            * Returns CY3 value.
            */
           public float getCY3(int column, int row, int chromosome)
           {
               return -1;
           }

           /**
            * Returns CY5 value.
            */
           public float getCY5(int column, int row, int chromosome)
           {
               return -1;
           }

           /**
            * Returns an element attribute.
            */
           public String getElementAttribute(int row, int attr, int chromosome)
           {
               return null;
           }

           public float getValue(int experiment, int clone, int chromosome)
           {
               return -1;
           }

           public ArrayList getFeaturesList()
           {
               return null;
           }

        public void setMarkerClusters(Map map)
        {
            this.markerClusters = map;
        }
    }
}
