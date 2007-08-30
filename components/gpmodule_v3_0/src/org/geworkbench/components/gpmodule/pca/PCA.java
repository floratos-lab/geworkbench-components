package org.geworkbench.components.gpmodule.pca;

import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;


@AcceptTypes({PCADataSet.class})
public class PCA extends MicroarrayViewEventBase
{
    private JTabbedPane tabbedPane;
    private JScrollPane resultsPanel;
    private JTable resultsTable;
    private JTextField perVar;
    private JSplitPane rightPane;

    private PCAData pcaData;


    public PCA()
    {
        JSplitPane pcaPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pcaPanel.setOneTouchExpandable(true);

        tabbedPane = new JTabbedPane();

        resultsPanel = new JScrollPane();
        tabbedPane.add("Components", resultsPanel);

        pcaPanel.setLeftComponent(tabbedPane);

        rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pcaPanel.setDividerLocation(270);


        pcaPanel.setRightComponent(rightPane);
             
        perVar = new JTextField();
        perVar.setMaximumSize(new Dimension(80, 100));

        jToolBar3.remove(chkAllArrays);
        jToolBar3.remove(chkAllMarkers);

        jToolBar3.add(new JLabel("% Var"));
        jToolBar3.add(perVar);

        resultsTable = new JTable();
        resultsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent event)
            {
                int[] selectedRows = resultsTable.getSelectedRows();
                double sum = 0;
                for(int i = 0; i < selectedRows.length; i++)
                {
                    String value = ((String)resultsTable.getValueAt(selectedRows[i], 2)).replace("%", "");
                    sum += Double.parseDouble(value);
                }

                perVar.setText(String.valueOf(sum));

                buildEigenVectorsTable(selectedRows);
                buildGraph(selectedRows);
            }
        });

        pcaPanel.setDividerLocation(220);
        mainPanel.add(pcaPanel, BorderLayout.CENTER);
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

        resultsTable.removeAll();
        resultsTable.setModel(tableModel);

        resultsPanel.setViewportView(resultsTable);
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
        rightPane.setBottomComponent(scrollPane);
    }

    public void buildGraph(int[] pComp)
    {
        System.out.println("enter graph ");
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
                (null, null, null, xySeriesCollection, PlotOrientation.VERTICAL, false, true, true);

        ChartPanel panel = new ChartPanel(lineGraph);
        JScrollPane scrollPane = new JScrollPane(panel);

        rightPane.setTopComponent(scrollPane);
    }

    @Subscribe
    public void receive(ProjectEvent e, Object source)
    {        
        if(e.getDataSet() instanceof PCADataSet)
        {
            pcaData = ((PCADataSet)e.getDataSet()).getData();
            buildResultsTable();
        }
    }
}
