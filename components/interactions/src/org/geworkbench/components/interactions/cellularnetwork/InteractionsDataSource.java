/*
 * InteractionsDataSource.java
 *
 * Created on June 2, 2006, 10:45 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.geworkbench.components.interactions.cellularnetwork;

import org.geworkbench.components.interactions.cachingtable.DistributedTableDataSource;
import org.geworkbench.components.interactions.cachingtable.DistributedTableDescription;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

/**
 * @author manjunath at genomecenter dot columbia dot edu
 */
public class InteractionsDataSource implements DistributedTableDataSource {

    private INTERACTIONS interactionsService = null;

    //Cached locally for efficiency
    private DistributedTableDescription tableDescription;

    /**
     * Constructor for InteractionsDataSource.
     */
    public InteractionsDataSource() {
        super();
        try {
            initConnections();
            readTableDescription(); //fetch the TableDescription
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initConnections() {
        org.geworkbench.components.interactions.cellularnetwork.INTERACTIONSServiceLocator service =
                new org.geworkbench.components.interactions.cellularnetwork.INTERACTIONSServiceLocator();
        service.setinteractionsEndpointAddress(System.getProperty("interactions.endpoint"));
        try {
            interactionsService = service.getinteractions();
        } catch (ServiceException se) {
            se.printStackTrace();
        }
    }

    private String[] columnNames = {"Gene Name", "# of Protein-Protein org.geworkbench.components.interactions.cellularnetwork", "# of Protein-DNA org.geworkbench.components.interactions.cellularnetwork"};
    private Class[] columnClass = {String.class, int.class, int.class};
    private int geneCount = 0;

    /**
     * Method from <code>DistributedTableDataSource</code>
     */
    public DistributedTableDescription getTableDescription() throws Exception {
        return tableDescription;
    }

    /**
     * Method from <code>DistributedTableDataSource</code>
     */
    public synchronized Object[][] retrieveRows(int from, int to) throws Exception {
        Object[][] data = new Object[to - from][];
        for (int i = 0; i < (to - from); i++) {
            data[i] = new Object[tableDescription.getColumnCount()];
        }
        try {
            for (int i = 0; i < (to - from); i++) {
                Object[] columns = interactionsService.getGENEROW(new BigDecimal(from + i));
                int j = 0;
                for (Object column : columns) {
                    data[i][j++] = column;
                }
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }
        return data;
    }


    /**
     * Method from <code>DistributedTableDataSource</code>
     */
    public int[] sort(int sortColumn, boolean ascending, int[] selectedRows) throws Exception {
        return null;
    }

    /**
     * Method from <code>DistributedTableDataSource</code>
     */
    public void setSelectedRowsAndColumns(int[] selectedRows, int[] selectedColumns) throws Exception {
    }

    /**
     * Method from <code>DistributedTableDataSource</code>
     */
    public int[] getSelectedRows() throws Exception {
        return null;
    }

    /**
     * Method from <code>DistributedTableDataSource</code>
     */
    public int[] getSelectedColumns() {
        int[] cols = new int[tableDescription.getColumnCount()];
        for (int i = 0; i < cols.length; i++) {
            cols[i] = i;
        }
        return cols;
    }

    /**
     * Reads the table description data from a url and parses
     * them up into a TableDescription object.
     */
    private void readTableDescription() throws Exception {
        tableDescription = new DistributedTableDescription(columnNames, columnClass, geneCount);
    }
}