package org.geworkbench.components.colormosaic;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.util.associationdiscovery.cluster.CSMatchedMatrixPattern;

import java.util.HashMap;

/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version 1.0
 */

public class EisenBlock {
    protected int firstRow = 0;
    protected CSMatchedMatrixPattern pattern;
    protected DSPanel<DSGeneMarker> panel;
    protected DSMicroarraySet<DSMicroarray> microarraySet = null;
    protected boolean showAllMarkers = true;
    /**
     * If true, a cache() operation is required before the markers are correctly returned
     */
    private boolean dirty = true;
    /**
     * The cached markers from this and all children
     */
    // private DSPanel<DSGeneMarker> markerCache = new CSPanel<DSGeneMarker>("Cache");
    private HashMap<DSGeneMarker, Object> annotCache = new HashMap<DSGeneMarker, Object>();
    //private DSPanel<IGenericMarker> pValueMarkerCache = new CSPanel<IGenericMarker> ("Markers with associated pValues");

    EisenBlock(CSMatchedMatrixPattern _pattern, DSPanel<DSGeneMarker> _panel, DSMicroarraySet mArraySet) {
        panel = _panel;
        pattern = _pattern;
        microarraySet = mArraySet;
        dirty = true;
        if (_panel != null) {
            //      for (int i = 0; i < _panel.panels().size(); i++) {
            //        if (_panel.panels().get(i) instanceof IPValuePanel) {
            //          pValueMarkerCache.panels().add(_panel.panels().get(i));
            //        }
            //      }
        }
        cache();
    }

    public int getMarkerNo() {
        if (dirty) {
            cache();
        }
        if (pattern != null) {          
            if (showAllMarkers ) {
            	return pattern.getPattern().markers().length;
            } else {
               if (panel != null)
            	 return panel.size();
               else
            	 return 0;
            }
        } else {
            if (showAllMarkers || (panel == null) || (panel.size() == 0)) {
                return 0;
            } else {
                return panel.size();
            }
        }
    }

    public double getGenePValue(DSGeneMarker mInfo) {
        //IGenericMarker gl = mInfo;
        //DSMarker item = markerCache.get(mInfo.getLabel());
        if (mInfo != null) {
            Object x = annotCache.get(mInfo);
            if (x instanceof Float) {
                return ((Float) x).floatValue();
            }
        }
        return -1;
        //    for (int i = 0; i < pValueMarkerCache.panels().size(); i++) {
        //      if (pValueMarkerCache.panels().get(i).contains(gl)) {
        //        return ( (IPValuePanel) pValueMarkerCache.panels().get(i)).getPValue(gl);
        //      }
        //    }
        //    return -1d;
    }

    public int getGeneSerial(int row) {
        DSGeneMarker gl = getGeneLabel(row);
        if (gl != null) {
            return gl.getSerial();
        } else {
            return -1;
        }
    }

    public DSGeneMarker getGeneLabel(int row) {
        if (dirty) {
            cache();
        }
        try {
            if (pattern != null) {
                //return MarkerCache.GetMarkerStat(row - FirstRow).GetMarkerId();
                if (showAllMarkers || (panel == null) || (panel.size() == 0)) {
                    return pattern.getPattern().markers()[row];
                } else {
                    return panel.get(row);
                }
            } else {
                //return Panel.GetMarkerStat(row - FirstRow).GetMarkerId();
                if (showAllMarkers || (panel == null) || (panel.size() == 0)) {
                    return microarraySet.getMarkers().get(row);
                } else {
                    return panel.get(row);

                }
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public CSMatchedMatrixPattern getPattern() {
        return pattern;
    }

    public void setFirstRow(int row) {
        firstRow = row;
    }

    public void setPanel(DSPanel<DSGeneMarker> _panel) {
        if (this.panel != _panel) {
            this.panel = _panel;
        }
        dirty = true;
    }

    private void cache() {
        if ((pattern != null) && (panel != null)) {
            // markerCache.clear();
            annotCache.clear();
            for (int j = 0; j < panel.panels().size(); j++) { //added so the subpanels will be checked.
                DSPanel<DSGeneMarker> onepanel = panel.panels().get(j);
                //        if (onepanel instanceof IPValuePanel && onepanel.isActive()) {
                if (onepanel.isActive()) {
                    for (int k = 0; k < onepanel.size(); k++) {
                        DSGeneMarker m = onepanel.get(k);
                        // markerCache.add(m);
                        if (onepanel instanceof DSAnnotatedPanel) {
                            Object x = ((DSAnnotatedPanel<DSGeneMarker, Float>) onepanel).getObject(m);
                            annotCache.put(m, x);
                        }
                    }
                }
            }
            //        } else {
            //          for (int i = 0; i < pattern.getMarkerIdNo(); i++) {
            //            int markerId = pattern.getMarkerId(i);
            //            IMarkerInfo mi = microarraySet.getIMarkerInfo(markerId);
            //            if (mi != null) {
            //              DSPanelItem<IGenericMarker> item = new CSPanelItem<IGenericMarker>(mi);
            //              markerCache.addItem(item);
            //            }
            //          }
            //        }
        }
        dirty = false;
    }

    public DSPanel<DSGeneMarker> getPanel() {
        return panel;
    }

    public void showAllMarkers(boolean yes_no) {
        showAllMarkers = yes_no;
        dirty = true;
    }
}
