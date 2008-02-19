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
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class IdentifyFile
{
  /**
   * Identify a datasource's file content. 
   * @note Do not use this method
   * for stream sources - create a FileParse object instead. 
   *
   * @param file DOCUMENT ME!
   * @param protocol DOCUMENT ME! 
   * @return ID String
   */
  public String Identify(String file, String protocol)
  {
    FileParse parser = null;
    try {
      parser = new FileParse(file, protocol);
      if (parser.isValid()) {
        return Identify(parser);
      }
    } catch (Exception e) {
      System.err.println("Error whilst identifying");
      e.printStackTrace(System.err);
    }
    if (parser!=null)
      return parser.errormessage;
    return "UNIDENTIFIED FILE PARSING ERROR";
  }
  public String Identify(FileParse source) {
    return Identify(source, true); // preserves original behaviour prior to version 2.3
  }
  /**
   * Identify contents of source, closing it or resetting source to start afterwards.
   * @param source
   * @param closeSource
   * @return filetype string
   */
  public String Identify(FileParse source, boolean closeSource) {
    String reply = "PFAM";
    String data;
    int length=0;
    boolean lineswereskipped=false;
    boolean isBinary = false; // true if length is non-zero and non-printable characters are encountered
    try {
      while ( (data = source.nextLine()) != null)
      {
        length+=data.length();
        if (!lineswereskipped)
        {
          for (int i=0;!isBinary && i<data.length(); i++)
          {
            char c = data.charAt(i);
            isBinary = (c<32 && c!='\t' && c!='\n' && c!='\r' && c!=5 && c!=27); // nominal binary character filter excluding CR, LF, tab,DEL and ^E for certain blast ids 
          }
        }
        if (isBinary)
        {
          // jar files are special - since they contain all sorts of random characters.
          if (source.inFile!=null) 
          {
              String fileStr=source.inFile.getName();
              // possibly a Jalview archive. 
              if (fileStr.lastIndexOf(".jar")>-1 || fileStr.lastIndexOf(".zip")>-1) 
              {
                reply = "Jalview";
              }
          } 
          if (!lineswereskipped && data.startsWith("PK")) {
            reply="Jalview"; // archive.
            break;
          }
        }
        data = data.toUpperCase();

        if ( (data.indexOf("# STOCKHOLM") > -1))
        {
          reply = "STH";

          break;
        }

        if ((data.length() < 1) || (data.indexOf("#") == 0))
        {
          lineswereskipped=true;
          continue;
        }

        if (data.indexOf("PILEUP") > -1)
        {
          reply = "PileUp";

          break;
        }

        if ( (data.indexOf("//") == 0) ||
            ( (data.indexOf("!!") > -1) &&
             (data.indexOf("!!") < data.indexOf(
                 "_MULTIPLE_ALIGNMENT "))))
        {
          reply = "MSF";

          break;
        }
        else if (data.indexOf("CLUSTAL") > -1)
        {
          reply = "CLUSTAL";

          break;
        }
        else if ( (data.indexOf(">P1;") > -1) ||
                 (data.indexOf(">DL;") > -1))
        {
          reply = "PIR";

          break;
        }
        else if (data.indexOf(">") > -1)
        {
          // could be BLC file, read next line to confirm
          data = source.nextLine();

          if (data.indexOf(">") > -1)
          {
            reply = "BLC";
          }
          else
          {
            //Is this a single line BLC file?
            source.nextLine();
            String data2 = source.nextLine();
            if (data2 != null
                && data.indexOf("*") > -1
                && data.indexOf("*") == data2.indexOf("*"))
            {
              reply = "BLC";
            }
            else
            {
                reply = "FASTA";
            }
          }
          break;
        }
        else if (data.indexOf("HEADER") == 0 ||
                 data.indexOf("ATOM") == 0)
        {
          reply = "PDB";
          break;
        }
        else if (!lineswereskipped 
                && data.charAt(0)!='*' 
                  && data.charAt(0)!=' ' 
                    && data.indexOf(":") < data.indexOf(",")) //  && data.indexOf(",")<data.indexOf(",", data.indexOf(",")))
        {
          // file looks like a concise JNet file
          reply = "JnetFile";
          break;
        }
        
        lineswereskipped=true; // this means there was some junk before any key file signature  
      }
      if (closeSource) {
        source.close();
      } else {
        source.reset(); // so the file can be parsed from the beginning again.
      }
    }
    catch (Exception ex)
    {
      System.err.println("File Identification failed!\n" + ex);
      return source.errormessage;
    }
    if (length==0)
    {
      System.err.println("File Identification failed! - Empty file was read.");
      return "EMPTY DATA FILE";
    }
    return reply;
  }
  public static void main(String[] args) {
    for (int i=0; args!=null && i<args.length; i++)
    {
      IdentifyFile ider = new IdentifyFile();
      String type = ider.Identify(args[i], AppletFormatAdapter.FILE);
      System.out.println("Type of "+args[i]+" is "+type);
    }
    if (args==null || args.length==0)
    {
      System.err.println("Usage: <Filename> [<Filename> ...]");
    }
  }
}
