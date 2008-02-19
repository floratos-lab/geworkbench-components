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
 * Created on Mar 15, 2006
 *
 */
package org.biojava.dasobert.das2.io;

import java.util.*;

import org.biojava.dasobert.das2.*;
import org.biojava.dasobert.dasregistry.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/** a parser for the DAS2 sources response
 *
 * @author Andreas Prlic
 * @since 6:53:45 PM
 * @version %I% %G%
 */
public class DAS2SourceHandler
    extends DefaultHandler
{

  List sources;
  Das2Source currentSource;
  List coordinates;
  List capabilities;
  List labels;

  public static final String LABELPROPERTY = "label";

  public DAS2SourceHandler()
  {
    super();

    sources = new ArrayList();
    currentSource = new Das2SourceImpl();
    coordinates = new ArrayList();
    capabilities = new ArrayList();
    labels = new ArrayList();
  }

  private void startSource(String uri, String name, String qName,
                           Attributes atts)
  {

    String id = atts.getValue("uri");
    String title = atts.getValue("title");
    String doc_ref = atts.getValue("doc_href");
    String description = atts.getValue("description");

    currentSource.setId(id);
    currentSource.setNickname(title);
    currentSource.setHelperurl(doc_ref);
    currentSource.setDescription(description);

  }

  private DasCoordinateSystem getCoordinateSystem(String uri, String name,
                                                  String qname, Attributes atts)
  {
    // e.g. uri="http://das.sanger.ac.uk/dasregistry/coordsys/CS_LOCAL6"
    // source="Protein Sequence" authority="UniProt" test_range="P06213" />
    DasCoordinateSystem dcs = new DasCoordinateSystem();
    String id = atts.getValue("uri");
    dcs.setUniqueId(id);

    String source = atts.getValue("source");
    dcs.setCategory(source);

    String authority = atts.getValue("authority");
    dcs.setName(authority);

    String test_range = atts.getValue("test_range");
    dcs.setTestCode(test_range);

    try
    {
      String taxidstr = atts.getValue("taxid");
      int taxid = Integer.parseInt(taxidstr);
      dcs.setNCBITaxId(taxid);
    }
    catch (Exception e)
    {}

    String version = atts.getValue("version");
    if (version != null)
    {
      dcs.setVersion(version);
    }

    return dcs;
  }

  public void startElement(String uri, String name, String qName,
                           Attributes atts)
  {
    //System.out.println("new element "+qName);

    if (qName.equals("SOURCE"))
    {
      //System.out.println("new Source " + atts.getValue(uri));
      currentSource = new Das2SourceImpl();
      coordinates = new ArrayList();
      capabilities = new ArrayList();

      startSource(uri, name, qName, atts);

    }
    else if (qName.equals("MAINTAINER"))
    {
      String email = atts.getValue("email");
      currentSource.setAdminemail(email);
    }
    else if (qName.equals("COORDINATES"))
    {
      DasCoordinateSystem dcs = getCoordinateSystem(uri, name, qName, atts);
      coordinates.add(dcs);

    }
    else if (qName.equals("CAPABILITY"))
    {
      Das2Capability cap = getCapability(uri, name, qName, atts);
      capabilities.add(cap);
    }
    else if (qName.equals("PROPERTY"))
    {
      addProperty(uri, name, qName, atts);
    }
  }

  private Das2Capability getCapability(String uri, String name, String qName,
                                       Attributes atts)
  {
    // e.g <CAPABILITY type="features" query_id="http://das.biopackages.net/das/genome/yeast/S228C/feature" />
    Das2Capability cap = new Das2CapabilityImpl();

    String type = atts.getValue("type");
    cap.setCapability(type);
    String query_uri = atts.getValue("query_uri");
    cap.setQueryUri(query_uri);
    return cap;

  }

  private void addProperty(String uri, String name, String qName,
                           Attributes atts)
  {
    String pname = atts.getValue("name");
    String label = atts.getValue("value");
    if (pname.equals(LABELPROPERTY))
    {
      labels.add(label);
    }
  }

  public void startDocument()
  {
    sources = new ArrayList();
    coordinates = new ArrayList();
    capabilities = new ArrayList();
  }

  public void endElement(String uri, String name, String qName)
  {
    if (qName.equals("SOURCE"))
    {
      currentSource.setDas2Capabilities( (Das2Capability[]) capabilities.
                                        toArray(new Das2Capability[capabilities.
                                                size()]));
      //System.out.println("got coordinates " + coordinates.size());
      currentSource.setCoordinateSystem( (DasCoordinateSystem[]) coordinates.
                                        toArray(new DasCoordinateSystem[
                                                coordinates.size()]));

      currentSource.setLabels( (String[]) labels.toArray(new String[labels.size()]));
      labels.clear();

      //System.out.println("Das2SourceHandler endElement name " + name + " uri " + uri + " qName " + qName);
      //System.out.println("Das2SourceHandler adding to source: " + currentSource.getId());
      sources.add(currentSource);
      currentSource = new Das2SourceImpl();
    }
  }

  public DasSource[] getSources()
  {
    //System.out.println("Das2SourceHandler: source size: " + sources.size());
    return (DasSource[]) sources.toArray(new DasSource[sources.size()]);
  }

}
