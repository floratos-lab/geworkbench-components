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
package org.geworkbench.components.gpmodule_v3_0.classification;

import org.genepattern.io.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;


/**
 * @author Marc-Danie Nazaire
 */
public class PredictionModel
{
    OdfObject model;
    private File predFile;

    public PredictionModel(File fileName)
    {
        try
        {
            model = new OdfObject(fileName.getAbsolutePath());

            fileName.delete();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public File getPredModelFile()
    {
        try
        {
            if(predFile!= null && predFile.exists())
                return predFile;

            predFile = File.createTempFile("predModel", ".odf", new File(System.getProperty("temporary.files.directory")));
            predFile.deleteOnExit();

            OdfWriter odfWriter = new OdfWriter(predFile.getAbsolutePath(), null,
                                    model.getHeader("Model"), model.getIntHeader("DataLines"), false);

            String[] columnTypes = new String[model.getColumnCount()];
            for(int i=0; i < model.getColumnCount(); i++)
                columnTypes[i]= model.getColumnType(i);

            odfWriter.setColumnTypes(columnTypes);

            String[] assignments = model.getArrayHeader("ASSIGNMENTS");
            odfWriter.addHeader("ASSIGNMENTS", assignments);

            odfWriter.printHeader();
            for(int r = 0; r < model.getRowCount(); r++)
            {
                for(int c = 0; c < model.getColumnCount(); c++ )
                {
                     odfWriter.write(model.getValueAt(r, c));
                     odfWriter.write("\t");
                }
                odfWriter.write(System.getProperty("line.separator"));
            }

            odfWriter.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return predFile;
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

        public String[] getArrayHeader(String key)
        {
            return (String[])handler.keyValuePair.get(key);
        }

        public int getColumnCount()
        {
            return handler.numColumns;
        }

        public int getRowCount()
        {
            return handler.numRows;
        }

        public String getHeader(String key)
        {
             return (String)handler.keyValuePair.get(key);
        }

        public int getIntHeader(String key)
        {
             return ((Integer)handler.keyValuePair.get(key)).intValue();
        }

        public String getColumnType(int col)
        {
            String[] columnTypes = (String[])handler.keyValuePair.get("COLUMN_TYPES");

            return columnTypes[col];
        }

        public String getValueAt(int row, int col)
        {
            return handler.data[row][col];
        }

    }

    private class OdfHandler implements IOdfHandler
    {
        HashMap keyValuePair;
        int numRows;
        int numColumns;
        String[][] data = null;

        public OdfHandler()
        {
            keyValuePair = new HashMap();
        }

        public void endHeader(){}

        public void header(String key, String[] values)throws ParseException
        {
            if(key.equalsIgnoreCase("COLUMN_TYPES"))
                numColumns = values.length;
            
            keyValuePair.put(key,  values);
        }

        public void header(String key, String value) throws ParseException
        {
            if (key.equalsIgnoreCase("DataLines"))
            {
                numRows = Integer.parseInt(value);
                keyValuePair.put(key, new Integer(value));
            }
            else
                keyValuePair.put(key, value);
        }

        public void data(int row, int column, String d)
        {
            if(data == null)
            {
                data = new String[numRows][numColumns];
            }

            data[row][column] = d;
        }
    }
}
