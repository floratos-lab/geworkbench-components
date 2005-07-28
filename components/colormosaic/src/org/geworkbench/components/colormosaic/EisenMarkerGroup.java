package org.geworkbench.components.colormosaic;

import org.geworkbench.bison.datastructure.properties.DSSequential;

import java.util.HashSet;

/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version 1.0
 */

public class EisenMarkerGroup extends HashSet implements DSSequential {
    public boolean isActive = true;
    public String label;
    public int serial;

    public EisenMarkerGroup(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }

    public boolean add(Object obj) {
        if (!this.contains(obj)) {
            return super.add(obj);
        }
        return false;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String _label) {
        label = _label;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int i) {
        serial = i;
    }
}
