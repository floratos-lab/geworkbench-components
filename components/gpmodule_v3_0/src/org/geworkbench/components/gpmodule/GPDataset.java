/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2008) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule;

import org.genepattern.matrix.AbstractDataset;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nazaire
 * Date: Apr 30, 2010
 */
public class GPDataset extends AbstractDataset
{
    private List<float[]> data;
    private String[] rowNames;
    private String[] columnNames;

    public GPDataset(List<float[]> data, String[] rowNames, String[] columnNames)
    {
        this.data = data;
        this.rowNames = rowNames;
        this.columnNames = columnNames;
    }

    public double getValue(int row, int column)
    {
        return data.get(column)[row];
    }

    public String getRowName(int row)
    {
        return rowNames[row];
    }

    public int getRowCount()
    {
        return rowNames.length;
    }

    public String getRowDescription(int row)
    {
        return "";
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public String getColumnName(int column)
    {
        if(columnNames != null && columnNames[column] != null)
        {
            return columnNames[column];
        }

        return "Column " + column;
    }

    public String getColumnDescription(int column)
    {
        return "";
    }

    public String[] getRowNames()
    {
        return rowNames;
    }

    public String[] getColumnNames()
    {
        return columnNames;
    }
    public List<float[]> getData(){
    	return data;
    }
}
