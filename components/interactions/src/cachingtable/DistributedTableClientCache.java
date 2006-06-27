
package cachingtable;

import interactions.SwingWorker;

/**
 * Class that is responsible for retrieving the data for the table from
 * the server and storing it locally.
 * @author Jeremy Dickson, 2003.
 */

public class DistributedTableClientCache {
    
    //THE MAXIMUM SIZE OF THE CACHE
    private int maximumCacheSize = -1;
    
    //THE NUMBER OF ROWS THAT ARE RETRIEVED AT A TIME
    private int chunkSize = -1;
    
    //THE CACHE OF ROWS
    private Object[] data = null;
    
    //AN INDEX- AN INTS ARE STORED CORREPONDING TO A ROWS REAL INDEX IN THE TABLE. THE LOCATION OF THE INDEX IN THIS
    //ARRAY SHOWS WHICH LOCATION TO ACCESS IN THE data ARRAY
    private int[] rowIndexLookup = null;
    
    //STORES THE INDEX THAT THE NEXT WRITES TO THE TWO ARRAYS SHOULD TAKE PLACE IN. WHEN IT REACHES
    //THE MAX CACHE SIZE IT GOES BACK TO ZERO
    private int writePositionIndex = 0;
    
    //THE SOURCE OF DATA
    private DistributedTableDataSource tableDataSource = null;
    
    //THE INDEX IN THE TABLE TO FETCH DATA FROM, (STORED AS A CLASS VARIABLE FOR EFFICIENCY)
    private int toIndex = -1;
    
    //THE INDEX IN THE TABLE TO FETCH DATA TO, (STORED AS A CLASS VARIABLE FOR EFFICIENCY)
    private int fromIndex = -1;
    
    //THE LAST INDEX THAT WAS REQUIRED WHEN A FETCH OCCURRED. DETERMINES WHETHER THE USER IS ASCENDING
    //OR DESCENDING THE TABLE
    private int lastRequiredFetchRowIndex = 0;
    
    //CONVENIENCE VARIABLE, (STORED AS A CLASS VARIABLE FOR EFFICIENCY)
    private int tableIndex = -1;
    
    //THE LAST ARRAY INDEX OF THE CACHE TO BE INDEXED
    private int lastRowAccess = 0;
    
    //CONVENIENCE
    private int i = 0;
    
    private DistributedTableDescription tableDescription;
    
    DistributedTableModel model = null;
    
    TableWorker worker = null;
    
    /** Creates new DistributedTableClientCache
     *@param chunkSize The number of rows of data that are to be
     * retrieved from the remote store at a time.
     *@param maximumCacheSize The maximum number of rows that will be cached. When this number is exceeded
     *by new data that has been fetched, the oldest data is overwritten.
     *@tableDataSource A source of table data, (via the method <code>retrieveRows</code>).
     */
    public DistributedTableClientCache(int chunkSize, int maximumCacheSize, DistributedTableDataSource tableDataSource, DistributedTableModel m) throws Exception {
        this.tableDataSource = tableDataSource;
        this.tableDescription = tableDataSource.getTableDescription();
        this.model = m;
        //ENSURE CHUNK SIZE NOT TOO SMALL
        if(chunkSize < 1) {
            chunkSize = 1;
        }
        this.chunkSize = chunkSize;
        
        //ENSURE MAX CACHE SIZE NOT TOO SMALL
        if(maximumCacheSize < 300) {
            maximumCacheSize = 300;
        }
        this.maximumCacheSize = maximumCacheSize;
        
        //MAKE SURE THE CHUNK SIZE NOT BIGGER THAN THE MAX CACHE SIZE
        if(chunkSize > maximumCacheSize) {
            chunkSize = maximumCacheSize ;
        }
        
        //INITIALIZE THE ARRAYS
        data = new Object[maximumCacheSize];
        rowIndexLookup = new int[maximumCacheSize];
        
        //SET ALL THE ROWS TO -1, (THEY INITIALIZE TO 0).
        for(int i = 0; i < rowIndexLookup.length; i++) {
            rowIndexLookup[i] = -1;
        }
    }
    
    /**
     *Retrieves a row from the data cache. If the row is not currently in
     * the cache it will be retrieved from the DistributedTableDataSource
     * object.
     *@param rowIndex The row index in the table that is to be retrieved.
     */
    public synchronized Object[] retrieveRowFromCache(int rowIndex) {
        worker = new TableWorker(rowIndex);
//        ensureRowCached(rowIndex);
        worker.start();
        if (getIndexOfRowInCache(rowIndex) == -1)
            return new Object[]{"loading...", "loading...", "loading...", "loading...", "loading...", "loading...", "loading..."};
        return (Object[])data[getIndexOfRowInCache(rowIndex)];
    }
    
    class TableWorker extends SwingWorker{
        int row = 0;
        
        public TableWorker(int r){
            row = r;
        }
        
        public synchronized Object construct(){
            if (!isRowCached(row))
                ensureRowCached(row);
            return "done";
        }
        
        public void finished(){
            model.fireTableDataChanged();
        }
    }
    
    /**
     *Ensures that a row index in the table is cached and if not a chunk of data is retrieved.
     */
    private synchronized void ensureRowCached(int rowIndex) {
        if(!isRowCached(rowIndex)) {
            //HAVE TO FETCH DATA FROM THE REMOTE STORE
            
            //SET THE toIndex AND fromIndex VARIABLES
            
            //TEST IF THE USER IS DESCENDING THE TABLE
            if (rowIndex >= lastRequiredFetchRowIndex) {
                fromIndex = rowIndex;
                toIndex = rowIndex+chunkSize;           
                try {
                    if(toIndex > tableDescription.getRowCount()) {
                        toIndex = tableDescription.getRowCount();
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            //USER IS ASCENDING THE TABLE
            else {
                fromIndex = rowIndex-chunkSize;
                if(fromIndex < 0) {
                    fromIndex = 0;
                }
                toIndex = rowIndex+1;
            }
            
            Object[][] rows = null;
            //RETRIEVE THE DATA
            try {
                rows = tableDataSource.retrieveRows(fromIndex, toIndex);
            } catch(Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException("Problem occurred retrieving table data \n");
            }
            
            //ADD THE DATA TO THE CACHE
            for(int i = 0; i < rows.length; i++) {
                //SET THE VALUE IN THE DATA ARRAY
                data[writePositionIndex] = rows[i];
                
                //CREATE AN INDEX TO THE NEW CACHED DATA
                tableIndex = fromIndex+i;
                rowIndexLookup[writePositionIndex] = tableIndex;
                
                //CLOCK UP writePositionIndex AND REZERO IF NECESSARY
                if(writePositionIndex == (maximumCacheSize-1)) {
                    writePositionIndex = 0;
                } else {
                    writePositionIndex++;
                }
                lastRequiredFetchRowIndex = rowIndex;
            }
        }
    }
    
    /**
     *Returns whether a particular row index in the table is cached.
     */
    private boolean isRowCached(int rowIndexInTable) {
        return getIndexOfRowInCache(rowIndexInTable) >= 0;
    }
    
    /**
     *Returns the array index of a particular row index in the table
     */
    private int getIndexOfRowInCache(int rowIndex) {
        for(i = lastRowAccess; i < rowIndexLookup.length; i++) {
            if(rowIndexLookup[i] == rowIndex) {
                lastRowAccess = i;
                return i;
            }
        }
        for(i = 0; i < lastRowAccess; i++) {
            if(rowIndexLookup[i] == rowIndex) {
                lastRowAccess = i;
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Called after a sort has been carried out to nullify the data
     * in the cache so that the newly sorted data must be fetched from
     * the server.
     */
    public void sortOccurred() {
        //SET ALL THE ROWS TO -1, (THEY INITIALIZE TO 0).
        for(int i = 0; i < data.length; i++) {
            data[i] = null;
            rowIndexLookup[i] = -1;
        }
    }   
}