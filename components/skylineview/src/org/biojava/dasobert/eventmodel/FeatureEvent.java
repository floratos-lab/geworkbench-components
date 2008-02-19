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
 * Created on Oct 28, 2005
 *
 */
package org.biojava.dasobert.eventmodel;

import java.util.*;

import org.biojava.dasobert.dasregistry.*;

public class FeatureEvent
{

  Map[] features;
  Das1Source dasSource;
  int comeBackLater;

  public FeatureEvent(Map[] features, Das1Source dasSource)
  {
    super();
    this.features = features;
    this.dasSource = dasSource;
    comeBackLater = -1;
  }

  public int getComeBackLater()
  {
    return comeBackLater;
  }

  public void setComeBackLater(int comeBackLater)
  {
    this.comeBackLater = comeBackLater;
  }

  /** get the features that have been found.
   *
   * do something like
   * Map[] features = event.getFeatures();
   * <pre>
   * for (int i = 0 ; i< features;i++) {
   *      Map f = features[i];
   *      String type = (String) f.get("TYPE") ;
   *      System.out.println(type);
   * }
   * </pre>
   * @return a Map containng the features
   */
  public Map[] getFeatures()
  {
    return features;
  }

  public Das1Source getDasSource()
  {
    return dasSource;
  }

}
