/*
 * Jalview - A Sequence Alignment Editor and Viewer
 * Copyright (C) 2007 AM Waterhouse, J Procter, G Barton, M Clamp, S Searle
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package jalview.io;

import java.io.*;
import java.util.*;
import javax.xml.namespace.*;
import javax.xml.rpc.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class EBIFetchClient
{
  Call call;
  String format = "default";
  String style = "raw";

  /**
   * Creates a new EBIFetchClient object.
   */
  public EBIFetchClient()
  {
    try
    {
      call = (Call)new Service().createCall();
      call.setTargetEndpointAddress(new java.net.URL(
          "http://www.ebi.ac.uk/ws/services/Dbfetch"));
    }
    catch (Exception ex)
    {
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String[] getSupportedDBs()
  {
    try
    {
      call.setOperationName(new QName("urn:Dbfetch", "getSupportedDBs"));
      call.setReturnType(XMLType.SOAP_ARRAY);

      return (String[]) call.invoke(new Object[]
                                    {});
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String[] getSupportedFormats()
  {
    try
    {
      call.setOperationName(new QName("urn:Dbfetch", "getSupportedFormats"));
      call.setReturnType(XMLType.SOAP_ARRAY);

      return (String[]) call.invoke(new Object[]
                                    {});
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String[] getSupportedStyles()
  {
    try
    {
      call.setOperationName(new QName("urn:Dbfetch", "getSupportedStyles"));
      call.setReturnType(XMLType.SOAP_ARRAY);

      return (String[]) call.invoke(new Object[]
                                    {});
    }
    catch (Exception ex)
    {
      return null;
    }
  }


  public File fetchDataAsFile(String ids, String f, String s)
  {
    String[] data = fetchData(ids, f, s);
    File outFile = null;
    try
    {
      outFile = File.createTempFile("jalview", ".xml");
      outFile.deleteOnExit();
      PrintWriter out = new PrintWriter(new FileOutputStream(outFile));
      int index = 0;
      while (index < data.length)
      {
        out.println(data[index]);
        index++;
      }
      out.close();
    }
    catch (Exception ex)
    {}
    return outFile;
  }

  /**
   * Single DB multiple record retrieval
   *
   * @param ids db:query1;query2;query3
   * @param f raw/xml
   * @param s ?
   *
   * @return Raw string array result of query set
   */
  public String[] fetchData(String ids, String f, String s)
  {
    // Need to split
    // ids of the form uniprot:25KD_SARPE;ADHR_DROPS;
    StringTokenizer queries = new StringTokenizer(ids, ";");
    String db = null;
    StringBuffer querystring = null;
    while (queries.hasMoreTokens())
    {
      String query = queries.nextToken();
      int p;
      if ( (p = query.indexOf(':')) > -1)
      {
        db = query.substring(0, p);
        query = query.substring(p + 1);
      }
      if (querystring == null)
      {
        querystring = new StringBuffer(query);
      }
      else
      {
        querystring.append("," + query);
      }
    }
    if (db == null)
    {
      System.err.println("Invalid Query string : '" + ids +
                         "'\nShould be of form 'dbname:q1;q2;q3;q4'");
    }
    return fetchBatch(querystring.toString(), db, f, s);
  }

  public String[] fetchBatch(String ids, String db, String f, String s)
  {
    // max 50 ids can be added at one time
    try
    {
      //call.setOperationName(new QName("urn:Dbfetch", "fetchData"));
      call.setOperationName(new QName("urn:Dbfetch", "fetchBatch"));
      call.addParameter("ids", XMLType.XSD_STRING, ParameterMode.IN);
      call.addParameter("db", XMLType.XSD_STRING, ParameterMode.IN);
      call.addParameter("format", XMLType.XSD_STRING, ParameterMode.IN);
      call.addParameter("style", XMLType.XSD_STRING, ParameterMode.IN);
      call.setReturnType(XMLType.SOAP_ARRAY);

      if (f != null)
      {
        format = f;
      }

      if (s != null)
      {
        style = s;
      }

      try
      {
        return (String[]) call.invoke(new Object[]
                                      {ids.toLowerCase(), db.toLowerCase(),
                                      format, style});
      }
      catch (OutOfMemoryError er)
      {
        System.out.println("OUT OF MEMORY DOWNLOADING QUERY FROM " + db + ":\n" +
                           ids);
      }
      return null;
    }
    catch (Exception ex)
    {
      if (ex.getMessage().startsWith(
          "uk.ac.ebi.jdbfetch.exceptions.DbfNoEntryFoundException"))
      {
        return null;
      }
      System.err.println("Unexpected exception when retrieving from " + db +
                         "\nQuery was : '" + ids + "'");
      ex.printStackTrace(System.err);
      return null;
    }
  }
}
