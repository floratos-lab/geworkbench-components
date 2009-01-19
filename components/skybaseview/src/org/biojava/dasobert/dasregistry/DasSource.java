/*
 *                  BioJava development code
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
 * Created on Feb 8, 2006
 *
 */
package org.biojava.dasobert.dasregistry;

import java.util.*;

public interface DasSource
{

  public void setLocal(boolean flag);

  public boolean isLocal();

  /** compare if two das sources are equal
   *
   * @param ds
   * @return returns true if two DAS sources are equivalent
   */
  public boolean equals(DasSource ds);

  /** classes that implement equals, should also implement hashKey
   *
   * @return the hash code of a das source
   */
  public int hashCode();

  public void setId(String i);

  /** get a the Id of the DasSource. The Id is a unique db
   * identifier. The public DAS-Registry has Auto_Ids that look like
   * DASSOURCE:12345; public look like XYZ:12345, where the XYZ
   * prefix can be configured in the config file.
   * @return String the ID of a Das Source
   */
  public String getId();

  public void setNickname(String name);

  public String getNickname();

  public void setUrl(String u);

  public void setAdminemail(String u);

  public void setDescription(String u);

  public void setCoordinateSystem(DasCoordinateSystem[] u);

  public void setCapabilities(String[] u);

  public String getUrl();

  public String getAdminemail();

  public String getDescription();

  public String[] getCapabilities();

  public DasCoordinateSystem[] getCoordinateSystem();

  public void setRegisterDate(Date d);

  public Date getRegisterDate();

  public void setLeaseDate(Date d);

  public Date getLeaseDate();

  public void setLabels(String[] ls);

  public String[] getLabels();

  public void setHelperurl(String url);

  public String getHelperurl();

  // TestCode is now part of the coordinate system!
  //public  void setTestCode(String code);
  //public  String getTestCode();

  public void setAlertAdmin(boolean flag);

  public boolean getAlertAdmin();

}