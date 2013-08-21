package org.geworkbench.components.gpmodule.pca;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.media.j3d.RenderingError;
import javax.media.j3d.RenderingErrorListener;
import javax.media.j3d.VirtualUniverse;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.CSMarkerVector;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.pca.CSPCADataSet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.util.colorcontext.ColorContext;
import org.geworkbench.components.gpmodule.pca.viewer.PCAContent3D;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.visualproperties.PanelVisualProperties;
import org.geworkbench.util.visualproperties.PanelVisualPropertiesManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.tigr.util.FloatMatrix;

/**
 * @author: Marc-Danie Nazaire
 * @version $Id$
 */
@AcceptTypes({CSPCADataSet.class})
@SuppressWarnings("rawtypes")
public class PCA implements VisualPlugin
{
	// these fields used to be in the base class MicroarrayViewEventBase
	private DSMicroarraySet refMASet = null;
	private DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView = null;
	private JButton plotButton = new JButton("Plot");
	private final String markerLabelPrefix = "  Markers: ";
	private JLabel numMarkersSelectedLabel = new JLabel(markerLabelPrefix);
	private JPanel mainPanel;
	private JToolBar jToolBar3;
	private DSPanel<DSGeneMarker> markers = null;
	private DSPanel<DSGeneMarker> activatedMarkers = null;
	private DSItemList<? extends DSGeneMarker> uniqueMarkers = null;
	private DSPanel<DSMicroarray> activatedArrays = null;

    private static Log log = LogFactory.getLog(PCA.class);
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

    private CSPCADataSet pcaDataSet;
	private CSMicroarraySet dataSet;

    private JScrollPane mainScrollPane;
    private Map<String, Set<String>> dataLabelGroups;
    private PCAContent3D pcaContent3D;
    private RenderingErrorListener errorListener = null;

    // principal components currently plotted
    private int[] plottedComps;

    public PCA()
    {
    	// initialization that used to be in the base class MicroarrayViewEventBase
		mainPanel = new JPanel();
		jToolBar3 = new JToolBar();
		BorderLayout borderLayout2 = new BorderLayout();
		mainPanel.setLayout(borderLayout2);
		mainPanel.add(jToolBar3, java.awt.BorderLayout.SOUTH);

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
        projPanel.setIgnoreRepaint(true);
        tabbedPane.addTab("Projection", projPanel);
        tabbedPane.addChangeListener( new PCAChangeListener(tabbedPane.getSelectedIndex()));

        projGraphPanel = new Container();
        projPanel.setRightComponent(projGraphPanel);

        perVar = new JTextField();
        perVar.setEditable(false);
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
                    JOptionPane.showMessageDialog(null, "Cannot create microarray set: No principal components selected");
                    return;
                }

                CSMicroarraySet pcDataSet = new CSMicroarraySet();
                pcDataSet.setLabel("PCA_" + dataSet.getLabel());
                pcDataSet.addObject(ColorContext.class, dataSet.getObject(ColorContext.class));

                FloatMatrix uMatrix = new FloatMatrix(pcaDataSet.getUMatrix());
                int numRows = uMatrix.getRowDimension();

                for(int pc = 0; pc < pcs.length; pc++)
                {
                    DSMicroarray array =  new CSMicroarray(pc, numRows, "PC " + (pcs[pc]+1), DSMicroarraySet.DO_NOT_CREATE_VALUE_OBJECT);
                    CSMarkerVector markerVector = pcDataSet.getMarkers();
                    for(int r = 0; r < numRows; r++)
                    {
                        CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue();
                        markerValue.setMissing(false);
                        markerValue.setValue(uMatrix.get(r, pcs[pc]));

                        if(pcaDataSet.getVariables().equals("experiments"))
                        {
                            DSGeneMarker marker = ((CSMicroarraySet)dataSet).getMarkers().get(r);
                            markerVector.add(marker);
                        }
                        else
                        {
                            CSExpressionMarker marker = new CSExpressionMarker(r);
                            marker.setLabel(((DSMicroarray)dataSet.get(r)).getLabel());
                            marker.setDescription(((DSMicroarray)dataSet.get(r)).getLabel());
                            markerVector.add(marker);
                        }

                        array.setMarkerValue(r, markerValue);
                    }

                    pcDataSet.add(array);
                }

                publishProjectNodeAddedEvent(new ProjectNodeAddedEvent("PCA_" + dataSet.getDataSetName(), pcDataSet, null));

            }
        });

        compResultsTable = new JTable()
        {
			private static final long serialVersionUID = 1924953061319615092L;

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

                    DecimalFormat myFormat = new DecimalFormat("0.000");
                    perVar.setText(String.valueOf(myFormat.format(sum)));
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
                plottedComps = projResultsTable.getSelectedRows();                
                pcaContent3D = null;

                buildProjectionPlot(plottedComps);
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
                plottedComps = null;

                pcaContent3D = null;
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
                org.geworkbench.events.ImageSnapshotEvent imageEvent = new org.geworkbench.events.ImageSnapshotEvent("PCA Image View", newIcon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
                publishImageSnapshotEvent(imageEvent);
            }
        });

        projResultsTable = new JTable()
        {
			private static final long serialVersionUID = -9127250606276011834L;

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

        clearPlotButton.doClick();

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
                @SuppressWarnings("unchecked")
				Map<String, String> vuMap = javax.media.j3d.VirtualUniverse.getProperties();
                System.out.println("Vendor: " + vuMap.get("j3d.vendor"));
                System.out.println("Vendor version: " + vuMap.get("j3d.version"));
                System.out.println("Renderer: " + vuMap.get("j3d.renderer"));
                return true;
			}
        }
        catch(Exception e)
        {
            log.error(e);
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
        String[] columnNames = {"ID", "Eigenvalue", "Variance"};
        TableModel tableModel = new DefaultTableModel(columnNames, pcaDataSet.getNumPCs()){

        	private static final long serialVersionUID = 4021265202483880605L;

			public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return false;
            }
        };

        for(int i=1; i <= pcaDataSet.getNumPCs(); i++)
        {
            tableModel.setValueAt(i, i-1, 0);
            Map<Integer, Double> eigenValues = pcaDataSet.getEigenValues();
            tableModel.setValueAt(eigenValues.get(Integer.valueOf(i)), i-1, 1);

            Map<Integer, String> percentVars = pcaDataSet.getPercentVars();
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

    @SuppressWarnings("unchecked")
	private void buildEigenVectorsTable(int[] pComp)
    {
        if(pComp == null || pComp.length == 0)
        {
            log.error("No principal components found");
            return;
        }

        JTable eigenVectorsTable = new JTable()
        {
			private static final long serialVersionUID = 3692664148295500817L;

			public boolean getScrollableTracksViewportHeight()
            {
                Component parent = getParent();
                if(parent instanceof JViewport)
                   return parent.getHeight() > getPreferredSize().height;

                return false;
            }

            public boolean getScrollableTracksViewportWidth()
            {
                Component parent = getParent();
                if(parent instanceof JViewport)
                return parent.getWidth() > getPreferredSize().width;

                 return false;
           }
        };

        DefaultTableModel tableModel = new DefaultTableModel()
        {
			private static final long serialVersionUID = -6053029709630367294L;

			public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return false;
            }
        };
        
        Map<Integer, List<String>> map = pcaDataSet.getEigenVectors();
        tableModel.setColumnCount((map.values().iterator().next()).size()+1);
        for(int i = 0; i < pComp.length; i++)
        {
            int pc = pComp[i]+1;
            List<String> eigenVector = new ArrayList<String>();
            eigenVector.add(0, "Prin. Comp. " + pc);
            eigenVector.addAll(map.get(new Integer(pc)));

            tableModel.addRow(new Vector<String>(eigenVector));
        }

        Vector<String> columnNames = new Vector<String>();
        columnNames.addAll((Vector<String>)tableModel.getDataVector().get(0));
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

    private HashMap<String, Object> createClusterColorMap(boolean useMarkers, boolean includeShapes)
    {
    	// map value can be PanelVisualProperties or Color
        HashMap<String, Object> colorMap = new HashMap<String, Object>();

        DSItemList<?> panels;
        if(!useMarkers && activatedArrays != null)
        {
            panels = activatedArrays.panels();
        }
        else
        {
            panels = markers.panels();
        }

        PanelVisualPropertiesManager manager = PanelVisualPropertiesManager.getInstance();
        colorMap.put("group 1", includeShapes ? manager.getDefaultVisualProperties(0): manager.getDefaultVisualProperties(0).getColor());

        if(panels == null || panels.size() == 0)
            return null;

        for(int i = 0; i < panels.size(); i++)
        {
            DSPanel<?> dp = (DSPanel<?>)panels.get(i);
            if(dp.size() == 0 || !dp.isActive())
            {
                continue;
            }

            PanelVisualProperties visualProperties = manager.getVisualProperties(dp);
             if (visualProperties == null)
             {
                visualProperties = manager.getDefaultVisualProperties(i);
             }

            if(includeShapes)
            {
                colorMap.put(dp.getLabel(), visualProperties);
            }
            else
                colorMap.put(dp.getLabel(), visualProperties.getColor());
        }

        return colorMap;
    }


    public void buildEigenvectorGraph(int[] pComp)
    {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

        Map<Integer, List<String>> map = pcaDataSet.getEigenVectors();
        for(int i = 0; i < pComp.length; i++)
        {
            int pc = pComp[i]+1;
            List<String> eigenVector = new ArrayList<String>(map.get(new Integer(pc)));

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

    private void buildProjectionPlot(int[] pComp)
    {
    	CSMicroarraySet maSet = (CSMicroarraySet)dataSet;
        List<String> dataLabelList = new ArrayList<String>();
        FloatMatrix u_Matrix = null;
        HashMap<String, Set<String>> dataLabelGps = new HashMap<String, Set<String>>();

        if(pcaDataSet.getVariables().equals("genes"))
        {
            for(int i = 0; i < maSet.size(); i++)
            {
                dataLabelList.add(((DSMicroarray) maSet.get(i)).getLabel());
            }
        }
        else
        {
            for(int i = 0; i < maSet.getMarkers().size(); i++)
            {
                dataLabelList.add(maSet.getMarkers().get(i).getLabel());
            }
        }

        ArrayList<String> group1List = new ArrayList<String>(dataLabelList);
        if(pcaDataSet.getVariables().equals("experiments")
                && activatedMarkers != null && activatedMarkers.size() > 0 )
        {
            DSAnnotationContext<DSGeneMarker> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet.getMarkers());

            for(int i =0; i < activatedMarkers.size(); i++)
            {
                DSGeneMarker marker = activatedMarkers.get(i);
                String[] label = context.getLabelsForItem(marker);

                if(label != null && label.length > 0)
                {
                    for(int k = 0; k < label.length; k++)
                    {
                        if(!context.isLabelActive(label[k]))
                            continue;

                        Set<String> set = dataLabelGps.get(label[k]);
                        if(set == null)
                        set = new LinkedHashSet<String>();

                        set.add(marker.getLabel());
                        group1List.remove(marker.getLabel());

                        dataLabelGps.put(label[k], set);
                    }
                }
            }
        }
        else if(pcaDataSet.getVariables().equals("genes")
                 && activatedArrays != null && activatedArrays.size() > 0)
        {
            DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);

            for(int i =0; i < activatedArrays.size(); i++)
            {
                DSMicroarray array = (DSMicroarray)activatedArrays.get(i);
                String[] label = context.getLabelsForItem(array);

                if(label != null && label.length > 0)
                {
                    Set<String> set = dataLabelGps.get(label[0]);
                    if(set == null)
                        set = new LinkedHashSet<String>();
                    set.add(array.getLabel());
                    group1List.remove(array.getLabel());

                    dataLabelGps.put(label[0], set);
                }
            }
        }

        if(group1List.size() != 0)
            dataLabelGps.put("group 1", new LinkedHashSet<String>(group1List));


        u_Matrix = new FloatMatrix(pcaDataSet.getUMatrix());
        dataLabelGroups = new HashMap<String, Set<String>>(dataLabelGps);

        // build 3D projection plot
        if(pComp.length == 3)
        {
            if(pcaContent3D == null && !hasJava3D())
            {
                JOptionPane.showMessageDialog(null, "Java3D is not installed. " +
                        "\nPlease install Java3D and restart geWorkbench. " +
                        "\nFor details about downloading and installing Java3D go to https://java3d.dev.java.net");
                return;
            }

    		if (errorListener == null){
    		    errorListener = new RenderingErrorListener(){
        			public void errorOccurred(RenderingError e){
				    	JOptionPane.showMessageDialog(null, e.getErrorMessage()+"\nOpenGL 1.2 or better is required");
        			}
    		    };

    		    VirtualUniverse.addRenderingErrorListener(errorListener);
    		}

            int pc1 = pComp[0]+1;
            int pc2 = pComp[1]+1;
            int pc3 = pComp[2]+1;
            List<PCAContent3D.XYZData> data = new ArrayList<PCAContent3D.XYZData>();

            Set<String> dataGroups = dataLabelGroups.keySet();
            Iterator<String> it = dataGroups.iterator();
            while(it.hasNext())
            {
                String group = (String)it.next();

                Set<String> labels = dataLabelGroups.get(group);
                Iterator<String> labelsIt = labels.iterator();

                while(labelsIt.hasNext())
                {
                   String label = (String)labelsIt.next();
                    int row = dataLabelList.indexOf(label);

                    PCAContent3D.XYZData xyzData = new PCAContent3D.XYZData(u_Matrix.get(row, pc1-1),
                            u_Matrix.get(row, pc2-1), u_Matrix.get(row, pc3-1), label);
                    if((!group.equals("group 1") && dataGroups.size() != 1) || (group.equals("group 1") && dataGroups.size() == 1))
                    {
                         xyzData.setCluster(group);
                    }

                    data.add(xyzData);
                }
            }

            boolean update = true;
            if(pcaContent3D == null)
            {
                update = false;
                pcaContent3D = new PCAContent3D();
            }

            HashMap<String, Object> clusterColors = createClusterColorMap(pcaDataSet.getVariables().equals("experiments"), false);
            pcaContent3D.setData(data);
            pcaContent3D.setClusterColors(clusterColors);
            pcaContent3D.setPointSize((float)1.4);
            pcaContent3D.setXAxisLabel("Prin. Comp. " + pc1);
            pcaContent3D.setYAxisLabel("Prin. Comp. " + pc2);
            pcaContent3D.setZAxisLabel("Prin. Comp. " + pc3);

            if(update)
            {
                pcaContent3D.updatePoints();
            }
            else
            {
                pcaContent3D.updateScene();
                pcaContent3D.getComponent(0).addMouseListener(new PCA3DMouseListener());
                projGraphPanel = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
                ((ScrollPane)projGraphPanel).add(pcaContent3D);
                projPanel.setRightComponent(projGraphPanel);
            }
        }
        else  //build 2D Projection plot
        {
            int pc1 = pComp[0]+1;
            int pc2 = pComp[1]+1;
                    
            JFreeChart graph = ChartFactory.createScatterPlot
                                  ("2D Projection", "Prin. Comp. " + pc1, "Prin. Comp. " + pc2, null, PlotOrientation.VERTICAL, true, false, false);

            XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
            graph.getXYPlot().setDataset(xySeriesCollection);

            Set<String> dataGroups = dataLabelGroups.keySet();
            Iterator<String> it = dataGroups.iterator();
            int series = 0;
            while(it.hasNext())
            {
                String group = (String)it.next();
                XYSeries xySeries = new XYSeries("", false, true);
                if(!group.equals("group 1"))
                {
                    xySeries.setKey(group);
                }

                if(group.equals("group 1") && dataGroups.size() != 1)
                {
                    continue;   
                }

                HashMap<String, Object> clusterColors = createClusterColorMap(pcaDataSet.getVariables().equals("experiments"), true);
                PanelVisualProperties visualProperties = (PanelVisualProperties)clusterColors.get(group);
                graph.getXYPlot().getRenderer().setSeriesPaint(series, visualProperties.getColor());
                graph.getXYPlot().getRenderer().setSeriesShape(series,  visualProperties.getShape());
                series++;

                Set<String> labels = dataLabelGroups.get(group);
                Iterator<String> labelsIt = labels.iterator();

                while(labelsIt.hasNext())
                {
                    String label = (String)labelsIt.next();
                    int row = dataLabelList.indexOf(label);

                    XYDataItem item = new XYDataItem(u_Matrix.get(row, pc1-1), u_Matrix.get(row, pc2-1));
                    xySeries.add(item);
                }

                xySeriesCollection.addSeries(xySeries);
            }           

            graph.getXYPlot().getRangeAxis().setTickMarksVisible(true);
            graph.getXYPlot().getRangeAxis().setTickMarkPaint(Color.BLACK);

            graph.getXYPlot().getDomainAxis().setTickMarksVisible(true);
            graph.getXYPlot().getDomainAxis().setTickMarkPaint(Color.BLACK);

            set2DPlotBounds(graph, true, true);

            graph.getXYPlot().addRangeMarker(new ValueMarker(0.0, Color.BLACK, new BasicStroke((float)1.4)));
            graph.getXYPlot().addDomainMarker(new ValueMarker(0.0, Color.BLACK, new BasicStroke((float)1.4)));

            graph.getXYPlot().setDomainGridlinesVisible(false);
            graph.getXYPlot().getRenderer().setToolTipGenerator( new StandardXYToolTipGenerator()
            {
				private static final long serialVersionUID = -542247088075401407L;

				public String generateToolTip(XYDataset data, int series, int item)
                {
                    XYSeries xySeries = ((XYSeriesCollection)data).getSeries(series);
                    String key = (String)xySeries.getKey();

                    if(key.equals(""))
                        key = "group 1";
                    Set<String> labels = dataLabelGroups.get(key);

                    if(labels == null)
                    {
                        return "";    
                    }

                    Iterator<String> it = labels.iterator();
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
				private static final long serialVersionUID = 6678141586529831486L;

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

             if(chart.getXYPlot().getDataset().getItemCount(0) == 1)
                maxDomainRange = chart.getXYPlot().getDomainAxis().getStandardTickUnits().getCeilingTickUnit(maxDomainRange).getSize();
             chart.getXYPlot().getDomainAxis().setLowerBound(-maxDomainRange);
             chart.getXYPlot().getDomainAxis().setUpperBound(maxDomainRange);
        }

        if(setRange)
        {
            Range range =  chart.getXYPlot().getRangeAxis().getRange();
            double maxRange = Math.max(Math.abs(range.getLowerBound()), range.getUpperBound());

             if(chart.getXYPlot().getDataset().getItemCount(0) == 1)
                maxRange = chart.getXYPlot().getRangeAxis().getStandardTickUnits().getCeilingTickUnit(maxRange).getSize();
            chart.getXYPlot().getRangeAxis().setLowerBound(-maxRange);
            chart.getXYPlot().getRangeAxis().setUpperBound(maxRange);
        }
    }

    private void buildComponentsPanel(int[] pComp)
    {
        buildEigenVectorsTable(pComp);
        buildEigenvectorGraph(pComp);

        compPanel.setDividerLocation(0.25);
        compPanel.setRightComponent(compGraphPanel);
    }

    @Subscribe
    public void receive(ProjectEvent e, Object source)
    {

        if(e.getDataSet() != null && e.getDataSet() instanceof CSPCADataSet)
        {
            pcaDataSet = ((CSPCADataSet)e.getDataSet());
            DSDataSet<?> parentDataSet = pcaDataSet.getParentDataSet();
            if(parentDataSet instanceof CSMicroarraySet)
            	dataSet = (CSMicroarraySet)parentDataSet;

            reset();
            buildResultsTable();
        }
        else
            reset();
    }

    /**
    * geneSelectorAction
   *
   * @param e
     *            GeneSelectorEvent
    */
    @Subscribe
     public void receive(GeneSelectorEvent e, Object source)
    {
        log.debug("Source object " + source);

        markers = e.getPanel();
        activatedMarkers = new CSPanel<DSGeneMarker>();
        if (markers != null && markers.size() > 0)
        {
            for (int j = 0; j < markers.panels().size(); j++)
            {
                DSPanel<DSGeneMarker> mrk = markers.panels().get(j);
                if (mrk.isActive())
                {
                    for (int i = 0; i < mrk.size(); i++)
                        activatedMarkers.add(mrk.get(i));
                }
            }
        }

        refreshMaSetView();

        if(plottedComps != null)
            buildProjectionPlot(plottedComps);
    }

     /**
    * phenotypeSelectorAction
   *
   * @param e
     *            PhenotypeSelectorEvent
    */
    @Subscribe
    @SuppressWarnings({ "unchecked" })
    public void receive(PhenotypeSelectorEvent e, Object source)
    {
         log.debug("Source object " + source);
       
        DSPanel arrays  = e.getTaggedItemSetTree();
        if(arrays != null)
        {
            activatedArrays = arrays;

            refreshMaSetView();

            if(plottedComps != null)
                buildProjectionPlot(plottedComps);
        }
    }    

    private volatile boolean beingRefreshed = false;
	/**
	 * Refreshes the chart view.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	final private void refreshMaSetView() {
		if(beingRefreshed) {
			return;
		}
		
		beingRefreshed = true;
		maSetView = new CSMicroarraySetView(this.refMASet);
		if (activatedMarkers != null && activatedMarkers.panels().size() > 0)
			maSetView.setMarkerPanel(activatedMarkers);
		if (activatedArrays != null && activatedArrays.panels().size() > 0 && activatedArrays.size() > 0)
			maSetView.setItemPanel(activatedArrays);

		uniqueMarkers = maSetView.getUniqueMarkers();

		fireModelChangedEvent();
		beingRefreshed = false;
	}
	
	/**
	 * @param event
	 */
	protected synchronized void fireModelChangedEvent() {
		// no-op
	}
	
    private void buildJToolBar3()
    {
        jToolBar3.removeAll();

        jToolBar3.add(new JLabel("Variance(%)"));
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
        }
        else {
            jToolBar3.add(createButton);
        }

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
            if(pcaDataSet.getVariables().equals("genes"))
            {
                DSMicroarray microarray = ((CSMicroarraySet)dataSet).getMicroarrayWithId(label);
                if (microarray != null)
                {
                    PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(microarray);
                    publishPhenotypeSelectedEvent(pse);
                }
            }
            else
            {
                DSGeneMarker marker = ((CSMicroarraySet)dataSet).getMarkers().get(label);
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
                Set<String> labels = dataLabelGroups.get(key);

                Iterator<String> it = labels.iterator();
                int i = 0;
                while(i < item)
                {
                    it.next();
                    i++;
                }

                String label = (String)it.next();

                if(pcaDataSet.getVariables().equals("experiments"))
                {
                    DSMicroarray microarray = ((CSMicroarraySet)dataSet).getMicroarrayWithId(label);
                    if (microarray != null)
                    {
                        PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(microarray);
                        publishPhenotypeSelectedEvent(pse);
                    }
                }
                else
                {
                    DSGeneMarker marker = ((CSMicroarraySet)dataSet).getMarkers().get(label);
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
