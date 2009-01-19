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

/** a Bean to be returned via SOAP. It takes care of the DAS -  coordinate Systems
 * @author Andreas Prlic
 */
public class DasCoordinateSystem
{

  String name;
  String category;
  String organism_name;
  int ncbi_tax_id;
  String uniqueId;
  String version;
  String testCode;

  public DasCoordinateSystem()
  {
    uniqueId = "";
    name = "";
    category = "";
    organism_name = "";
    ncbi_tax_id = 0;
    version = "";
    testCode = "";
  }

  public boolean equals(DasCoordinateSystem other)
  {
    boolean match = true;
    System.out.println("comparing " + this.toString() + " to " + other.toString());
    // URI has piority
    if ( (!uniqueId.equals("")) && (uniqueId.equals(other.getUniqueId())))
    {
      return true;
    }

    if (ncbi_tax_id != other.getNCBITaxId())
    {
      System.out.println("mismatch in ncbi tax id " + ncbi_tax_id + " != " +
                         other.getNCBITaxId());
      match = false;
    }
    if (!version.equals(other.getVersion()))
    {
      System.out.println("mismatch in version");
      match = false;
    }
    if (!category.equals(other.getCategory()))
    {
      System.out.println("mismatch in category");
      match = false;
    }
    if (!name.equals(other.getName()))
    {
      System.out.println("mismatch in name");
      match = false;
    }
    System.out.println(" match: " + match);

    return match;
  }

  public Object clone()
  {
    DasCoordinateSystem d = new DasCoordinateSystem();
    d.setTestCode(testCode);
    d.setCategory(category);
    d.setName(name);
    d.setNCBITaxId(ncbi_tax_id);
    d.setUniqueId(getUniqueId());
    d.setOrganismName(getOrganismName());
    d.setVersion(getVersion());
    return d;
  }

  public String getTestCode()
  {
    return testCode;
  }

  public void setTestCode(String testCode)
  {
    if (testCode == null)
    {
      testCode = "";
    }
    this.testCode = testCode;
  }

  public void setUniqueId(String id)
  {
    uniqueId = id;
  }

  public String getUniqueId()
  {
    return uniqueId;
  }

  public void setName(String n)
  {
    name = n;
  }

  public String getName()
  {
    return name;
  }

  public void setCategory(String c)
  {
    category = c;
  }

  public String getCategory()
  {
    return category;
  }

  public void setOrganismName(String t)
  {
    organism_name = t;
  }

  public String getOrganismName()
  {
    return organism_name;
  }

  public void setNCBITaxId(int id)
  {
    ncbi_tax_id = id;
  }

  public int getNCBITaxId()
  {
    return ncbi_tax_id;
  }

  public String getVersion()
  {
    return version;
  }

  public void setVersion(String version)
  {
    if (version == null)
    {
      version = "";
    }
    this.version = version;
  }

  public String toString()
  {
    String nam = name;
    if (!version.equals(""))
    {
      nam += "_" + version;
    }

    if (organism_name.equals(""))
    {
      return nam + "," + category;
    }
    else
    {
      return nam + "," + category + "," + organism_name;
    }
  }

  public static DasCoordinateSystem fromString(String rawString)
  {
    String[] spl = rawString.split(",");
    DasCoordinateSystem dcs = new DasCoordinateSystem();
    if (spl.length == 2)
    {
      dcs.setName(spl[0]);
      dcs.setCategory(spl[1]);
    }
    if (spl.length == 3)
    {
      dcs.setName(spl[0]);
      dcs.setCategory(spl[1]);
      dcs.setOrganismName(spl[2]);
    }
    return dcs;
  }

}
