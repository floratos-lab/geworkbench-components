package org.geworkbench.components.colormosaic;

import java.util.ArrayList;

/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version 1.0
 */

public class MarkerPanel extends ArrayList {
    public String label = null;

    public MarkerPanel(String label) {
        this.label = label;
    }

    public EisenMarkerGroup getGroup(int i) {
        return (EisenMarkerGroup) this.get(i);
    }

    public int getMarkerGroupNo() {
        return size();
    }

    public String toString() {
        if (label != null) {
            return label;
        }
        return new String("What?!!");
    }

    public boolean add(Object obj) {
        if (!this.contains(obj)) {
            return super.add(obj);
        }
        return false;
    }
}
