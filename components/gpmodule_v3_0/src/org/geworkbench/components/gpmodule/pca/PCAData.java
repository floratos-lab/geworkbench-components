/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2007) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.pca;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.gui.impl.pca.ValuesViewer;
import org.genepattern.io.OdfParser;
import org.genepattern.io.IOdfHandler;

import javax.swing.*;
import java.io.FileInputStream;
import java.util.List;

/**
 * @author: Marc-Danie Nazaire
 */
public class PCAData
{
    private FloatMatrix S_matrix;
    private FloatMatrix T_matrix;
    private FloatMatrix U_matrix;

    public PCAData(List files)
    {
        try
        {
            initMatrices(files);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initMatrices(List<String> files) throws Exception
    {
        for(String file : files)
        {
            OdfObject odfObject = new OdfObject(file);
            float[][] data = odfObject.getData();

            if(file.contains("s.odf"))
            {
                S_matrix = new FloatMatrix(data);
                System.out.println("S_matrix: " + data.length);
            }
            if(file.contains("t.odf"))
                T_matrix = new FloatMatrix(data);
            if(file.contains("u.odf"))
                U_matrix = new FloatMatrix(data);
        }

        ValuesViewer v = new ValuesViewer(S_matrix);
        v.getContentComponent().setVisible(true);

        JPanel panel = new JPanel();
        panel.add(v.getContentComponent());
        panel.setVisible(true);

        System.out.println("v content1: " +  ((JTextArea)v.getContentComponent()).getColumns());
        System.out.println("/n v content2: " +  ((JTextArea)v.getContentComponent()).getText());
    }

    private class OdfObject
    {
        OdfHandler handler;
        public OdfObject(String file) throws Exception
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(file);
                OdfParser parser = new OdfParser();
                handler = new OdfHandler();
                parser.setHandler(handler);
                parser.parse(fis);
            }
            catch(Exception e)
            {
                throw e;
            }
        }

        public int getColumnCount()
        {
            return handler.numColumns;
        }

        public int getRowCount()
        {
            return handler.numRows;
        }

        public float getValueAt(int row, int col)
        {
            return Float.valueOf(handler.data[row][col]).floatValue();
        }

        public float[][] getData()
        {
            return handler.data;
        }

    }

    private class OdfHandler implements IOdfHandler
    {
        int numRows;
        int numColumns;
        float[][] data = null;

        public OdfHandler(){}

        public void endHeader(){}

        public void header(String key, String[] values){}
        
        public void header(String key, String value)
        {
            if (key.equalsIgnoreCase("DataLines"))
                numRows = Integer.parseInt(value);

             if(key.equalsIgnoreCase("Columns"))
                numColumns = Integer.parseInt(value);
        }

        public void data(int row, int column, String d)
        {
            if(data == null)
            {
                data = new float[numRows][numColumns];
            }
                        
            data[row][column] = Float.valueOf(d).floatValue();
        }
    }
}
