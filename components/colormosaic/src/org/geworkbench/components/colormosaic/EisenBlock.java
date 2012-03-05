package org.geworkbench.components.colormosaic;

import java.util.HashMap;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version $Id$
 */

public final class EisenBlock {
    boolean showAllMarkers = true;
    
    private DSMatrixPattern pattern;
    private DSPanel<DSGeneMarker> panel;
    private DSMicroarraySet microarraySet = null;
    /**
     * If true, a cache() operation is required before the markers are correctly returned
     */
    private boolean dirty = true;

    private HashMap<DSGeneMarker, Object> annotCache = new HashMap<DSGeneMarker, Object>();

    EisenBlock(DSMatrixPattern _pattern, DSPanel<DSGeneMarker> _panel, DSMicroarraySet mArraySet) {
        panel = _panel;
        pattern = _pattern;
        microarraySet = mArraySet;
        dirty = true;
        cache();
    }

    public int getMarkerNo() {
        if (dirty) {
            cache();
        }
        if (pattern != null) {          
            if (showAllMarkers ) {
            	return pattern.markers().length;
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
        if (mInfo != null) {
            Object x = annotCache.get(mInfo);
            if (x instanceof Float) {
                return ((Float) x).floatValue();
            }
        }
        return -1;
    }

    public DSGeneMarker getGeneLabel(int row) {
        if (dirty) {
            cache();
        }
        try {
            if (pattern != null) {
                //return MarkerCache.GetMarkerStat(row - FirstRow).GetMarkerId();
                if (showAllMarkers || (panel == null) || (panel.size() == 0)) {
                    return pattern.markers()[row];
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

    public DSMatrixPattern getPattern() {
        return pattern;
    }

    public void setPanel(DSPanel<DSGeneMarker> _panel) {
        if (this.panel != _panel) {
            this.panel = _panel;
        }
        dirty = true;
    }

    @SuppressWarnings("unchecked")
	private void cache() {
        if ((pattern != null) && (panel != null)) {
            annotCache.clear();
            for (int j = 0; j < panel.panels().size(); j++) { //added so the subpanels will be checked.
                DSPanel<DSGeneMarker> onepanel = panel.panels().get(j);
                if (onepanel.isActive()) {
                    for (int k = 0; k < onepanel.size(); k++) {
                        DSGeneMarker m = onepanel.get(k);
                        if (onepanel instanceof DSAnnotatedPanel) {
                            Float x = ((DSAnnotatedPanel<DSGeneMarker, Float>) onepanel).getObject(m);
                            annotCache.put(m, x);
                        }
                    }
                }
            }
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
