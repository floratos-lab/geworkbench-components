/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 15.04.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.dasobert.dasregistry;

import java.util.*;

//import org.biojava.dasobert.das2.io.DasSourceWriter;
//import org.biojava.dasobert.das2.io.DasSourceWriterImpl;
//import org.biojava.utils.xml.PrettyXMLWriter;


/** a simple Bean class to be returned via SOAP
 * @author Andreas Prlic
 */

public class Das1Source
    implements DasSource
{
  String url;
  protected String nickname;
  String adminemail;
  String description;
  DasCoordinateSystem[] coordinateSystem;
  String[] capabilities;
  String[] labels;
  String helperurl;
  Date registerDate;
  Date leaseDate;
  String id;
  boolean local;

  boolean alertAdmin;

  public static String EMPTY_ID = "UNK:-1";

  public Das1Source()
  {
    id = EMPTY_ID;
    url = "";
    adminemail = "";
    description = "";
    //String empty     = "" ;
    nickname = "";
    coordinateSystem = new DasCoordinateSystem[0];
    //coordinateSystem[0] = new DasCoordinateSystem();
    capabilities = new String[0];
    labels = new String[0];
    //capabilities[0]  = empty ;
    registerDate = new Date();
    leaseDate = new Date();
    helperurl = "";
    local = true;
  }

  public boolean equals(DasSource other)
  {
    System.out.println("Das1Source equals, comparing with other DasSource");
    if (! (other instanceof Das1Source))
    {
      return false;
    }

    Das1Source ods = (Das1Source) other;

    if (ods.getUrl().equals(url))
    {
      return true;
    }
    if (ods.getNickname().equals(nickname))
    {
      return true;
    }
    return false;
  }

  public int hashCode()
  {
    int h = 7;

    h = 31 * h + (null == nickname ? 0 : nickname.hashCode());
    h = 31 * h + (null == url ? 0 : url.hashCode());

    return h;
  }

  /** the DAS2 string representation of this DAS source
   *
       public String toString() {

      StringWriter writer = new StringWriter();

      PrintWriter pw = new PrintWriter(writer);
      PrettyXMLWriter xw = new PrettyXMLWriter(pw);

      DasSourceWriter dswriter = new DasSourceWriterImpl();
      try {
          dswriter.writeDasSource(xw,this);
      } catch (IOException e){
          e.printStackTrace();
      }

      return writer.toString();

       }
   */
  public void setLocal(boolean flag)
  {
    local = flag;
  }

  public boolean isLocal()
  {
    return local;
  }

  public void setId(String i)
  {
    id = i;
  }

  /** get a the Id of the DasSource. The Id is a unique db
   * identifier. The public DAS-Registry has Auto_Ids that look like
   * DASSOURCE:12345; public look like XYZ:12345, where the XYZ
   * prefix can be configured in the config file.
   */
  public String getId()
  {
    return id;
  }

  public void setNickname(String name)
  {
    nickname = name;
  }

  public String getNickname()
  {
    return nickname;
  }

  public void setUrl(String u)
  {
    char lastChar = u.charAt(u.length() - 1);
    if (lastChar != '/')
    {
      u += "/";
    }

    url = u;
  }

  public void setAdminemail(String u)
  {
    adminemail = u;
  }

  public void setDescription(String u)
  {
    description = u;
  }

  public void setCoordinateSystem(DasCoordinateSystem[] u)
  {
    coordinateSystem = u;
  }

  public void setCapabilities(String[] u)
  {
    capabilities = u;
  }

  public String getUrl()
  {
    return url;
  }

  public String getAdminemail()
  {
    return adminemail;
  }

  public String getDescription()
  {
    return description;
  }

  public String[] getCapabilities()
  {
    return capabilities;
  }

  public DasCoordinateSystem[] getCoordinateSystem()
  {
    return coordinateSystem;
  }

  public void setRegisterDate(Date d)
  {
    registerDate = d;
  }

  public Date getRegisterDate()
  {
    return registerDate;
  }

  public void setLeaseDate(Date d)
  {
    leaseDate = d;
  }

  public Date getLeaseDate()
  {
    return leaseDate;
  }

  public void setLabels(String[] ls)
  {
    labels = ls;
  }

  public String[] getLabels()
  {
    return labels;
  }

  public void setHelperurl(String url)
  {
    helperurl = url;
  }

  public String getHelperurl()
  {
    return helperurl;
  }

  public void setAlertAdmin(boolean flag)
  {
    alertAdmin = flag;
  }

  public boolean getAlertAdmin()
  {
    return alertAdmin;
  }

}
