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
import java.net.*;
/** 
 * implements a random access wrapper around a particular datasource, for passing to
 * identifyFile and AlignFile objects.
 */
public class FileParse
{
  public File inFile=null;
  protected char suffixSeparator = '#';
  /**
   * '#' separated string tagged on to end of filename 
   * or url that was clipped off to resolve to valid filename
   */
  protected String suffix=null; 
  protected String type=null;
  protected BufferedReader dataIn=null;
  protected String errormessage="UNITIALISED SOURCE";
  protected boolean error=true;
  /**
   * size of readahead buffer used for when initial stream position is marked.
   */
  final int READAHEAD_LIMIT=2048;
  public FileParse()
  {
  }
  /**
   * Attempt to open a file as a datasource.
   * Sets error and errormessage if fileStr was invalid.
   * @param fileStr
   * @return this.error (true if the source was invalid)
   */
  private boolean checkFileSource(String fileStr) throws IOException {
    error=false;
    this.inFile = new File(fileStr);
    // check to see if it's a Jar file in disguise.
    if (!inFile.exists()) {
      errormessage = "FILE NOT FOUND";
      error=true;
    }
    if (!inFile.canRead()) {
      errormessage = "FILE CANNOT BE OPENED FOR READING";
      error=true;
    }
    if (inFile.isDirectory()) {
      // this is really a 'complex' filetype - but we don't handle directory reads yet.
      errormessage = "FILE IS A DIRECTORY";
      error=true;
    }
    if (!error) {
      dataIn = new BufferedReader(new FileReader(fileStr));
    }
    return error;
  }
  private boolean checkURLSource(String fileStr) throws IOException, MalformedURLException
  {
    errormessage = "URL NOT FOUND";
    URL url = new URL(fileStr);
    dataIn = new BufferedReader(new InputStreamReader(url.openStream()));
    return false;
  }
  /**
   * sets the suffix string (if any) and returns remainder (if suffix was detected) 
   * @param fileStr
   * @return truncated fileStr or null
   */
  private String extractSuffix(String fileStr) {
    // first check that there wasn't a suffix string tagged on.
    int sfpos = fileStr.lastIndexOf(suffixSeparator);
    if (sfpos>-1 && sfpos<fileStr.length()-1) {
      suffix = fileStr.substring(sfpos+1);
      // System.err.println("DEBUG: Found Suffix:"+suffix);
      return fileStr.substring(0,sfpos);
    }
    return null;
  }
  /**
   * Create a datasource for input to Jalview.
   * See AppletFormatAdapter for the types of sources that are handled.
   * @param fileStr - datasource locator/content
   * @param type - protocol of source 
   * @throws MalformedURLException
   * @throws IOException
   */
  public FileParse(String fileStr, String type)
      throws MalformedURLException, IOException
  {
    this.type = type;
    error=false;

    if (type.equals(AppletFormatAdapter.FILE))
    {
      if (checkFileSource(fileStr)) {  
        String suffixLess = extractSuffix(fileStr);
        if (suffixLess!=null)
        {
          if (checkFileSource(suffixLess))
          {
            throw new IOException("Problem opening "+inFile+" (also tried "+suffixLess+") : "+errormessage);
          }
        } else
        {
          throw new IOException("Problem opening "+inFile+" : "+errormessage);
        }
      }
    }
    else if (type.equals(AppletFormatAdapter.URL))
    {
      try {
        checkURLSource(fileStr);
        if (suffixSeparator=='#')
          extractSuffix(fileStr); // URL lref is stored for later reference.
      } catch (IOException e) {
        String suffixLess = extractSuffix(fileStr);
        if (suffixLess==null)
        {
          throw(e);
        } else {
          try {
            checkURLSource(suffixLess);
          }
          catch (IOException e2) {
            errormessage = "BAD URL WITH OR WITHOUT SUFFIX";
            throw(e); // just pass back original - everything was wrong.
          }
        }
      }
    }
    else if (type.equals(AppletFormatAdapter.PASTE))
    {
      errormessage = "PASTE INACCESSIBLE!";
      dataIn = new BufferedReader(new StringReader(fileStr));
    }
    else if (type.equals(AppletFormatAdapter.CLASSLOADER))
    {
      errormessage = "RESOURCE CANNOT BE LOCATED";
      java.io.InputStream is = getClass().getResourceAsStream("/" + fileStr);
      if (is==null) {
        String suffixLess = extractSuffix(fileStr);
        if (suffixLess!=null)
          is = getClass().getResourceAsStream("/" + suffixLess);
      }
      if (is != null)
      {
        dataIn = new BufferedReader(new java.io.InputStreamReader(is));
      } else {
        error = true;
      }
    }
    error=false;
    dataIn.mark(READAHEAD_LIMIT);
  }
  public String nextLine()
      throws IOException
  {
    if (!error)
      return dataIn.readLine();
    throw new IOException("Invalid Source Stream:"+errormessage);
  }

  public boolean isValid()
  {
    return !error;
  }
  /**
   * closes the datasource and tidies up.
   * source will be left in an error state
   */
  public void close() throws IOException
  {
    errormessage="EXCEPTION ON CLOSE";
    error=true;
    dataIn.close();
    dataIn=null;
    errormessage="SOURCE IS CLOSED";
  }
  /**
   * rewinds the datasource the beginning.
   *
   */
  public void reset() throws IOException
  {
    if (dataIn!=null && !error) {
      dataIn.reset();
    } else {
      throw new IOException("Implementation Error: Reset called for invalid source.");
    }
  }
}
