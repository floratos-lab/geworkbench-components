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
 * Created on 19.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.dasobert.das;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * a class to parse the response of a DAS - Feature request
 * @author Andreas Prlic
 *
 */
public class DAS_Feature_Handler
    extends DefaultHandler
{

  /**
   *
   */
  List features;
  boolean first_flag;
  HashMap feature;
  String featurefield;
  String characterdata;
  String dasCommand;

  int comeBackLater;

  int maxFeatures;

  public DAS_Feature_Handler()
  {
    super();

    features = new ArrayList();
    first_flag = true;
    featurefield = "";
    characterdata = "";
    dasCommand = "";
    comeBackLater = -1;
    maxFeatures = -1;
  }

  /** specifies a maximum number of features to be downloaded. if a
    server returns more, they will be ignored.  default is to load
    all features
       @param max the maximium number of features to be downloaded
   */

  public void setMaxFeatures(int max)
  {
    maxFeatures = max;
  }

  public int getMaxFeatures()
  {
    return maxFeatures;
  }

  public void setDASCommand(String cmd)
  {
    dasCommand = cmd;
  }

  public String getDASCommand()
  {
    return dasCommand;
  }

  public List get_features()
  {
    return features;
  }

  public int getComBackLater()
  {
    return comeBackLater;
  }

  void start_feature(String uri, String name, String qName, Attributes atts)
  {

    if ( (maxFeatures > 0) && (features.size() > maxFeatures))
    {
      characterdata = "";
      return;
    }
    feature = new HashMap();
    String id = atts.getValue("id");
    feature.put("id", id);
    feature.put("dassource", dasCommand);
    characterdata = "";
  }

  void add_featuredata(String uri, String name, String qName)
  {
    //System.out.println("featurefield "+featurefield+ " data "+characterdata);
    // NOTE can have multiple lines ..

    if ( (maxFeatures > 0) && (features.size() > maxFeatures))
    {
      return;
    }

    String data = (String) feature.get(featurefield);
    if (data != null)
    {
      characterdata = data + " " + characterdata;
    }

    feature.put(featurefield, characterdata);
    featurefield = "";
    characterdata = "";
  }

  private void addLink(String uri, String name, String qName, Attributes atts)
  {
    String href = atts.getValue("href");
    feature.put("LINK", href);
    characterdata = "";
    featurefield = "LINK-TEXT";

  }

  public void startElement(String uri, String name, String qName,
                           Attributes atts)
  {
    //System.out.println("new element "+qName);

    if (qName.equals("FEATURE"))
    {
      start_feature(uri, name, qName, atts);
    }
    else if (qName.equals("LINK"))
    {
      addLink(uri, name, qName, atts);
    }
    else if (qName.equals("METHOD") ||
             qName.equals("TYPE") ||
             qName.equals("START") ||
             qName.equals("END") ||
             qName.equals("NOTE") ||
             qName.equals("SCORE")
        )
    {
      characterdata = "";
      featurefield = qName;
    }

  }

  public void startDocument()
  {
  }

  public void endDocument()
  {
  }

  public void endElement(String uri, String name, String qName)
  {

    if (qName.equals("METHOD") ||
        qName.equals("TYPE") ||
        qName.equals("START") ||
        qName.equals("END") ||
        qName.equals("NOTE") ||
        qName.equals("LINK") ||
        qName.equals("SCORE")
        )
    {
      add_featuredata(uri, name, qName);
    }
    else if (qName.equals("FEATURE"))
    {

      if (maxFeatures > 0)
      {
        if (features.size() < maxFeatures)
        {
          features.add(feature);
        }
      }
      else
      {
        // no restriction
        features.add(feature);
      }
    }
  }

  public void characters(char ch[], int start, int length)
  {

    for (int i = start; i < start + length; i++)
    {

      characterdata += ch[i];
    }

  }

}
