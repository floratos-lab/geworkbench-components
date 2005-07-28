package org.geworkbench.components.colormosaic;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.util.DSAnnotLabel;

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

public class MicroarrayManager {
    boolean isActive = true;
    DSAnnotLabel phenoProperty = null;
    ArrayList phenoValues = null;
    String label = null;
    DSMicroarraySet<DSMicroarray> chips = null;
    public ArrayList chipProxyId = new ArrayList();
    ArrayList groups = new ArrayList();
    public ArrayList boundaries = new ArrayList();

    public MicroarrayManager(DSMicroarraySet chips, String label, boolean all) {
        this.chips = chips;
        this.label = label;
        if (all) {
            addAllChips();
        }
    }

    private void addAllChips() {
        if (chips != null) {
            for (int i = 0; i < chips.size(); i++) {
                DSMicroarray chip = chips.get(i);
                chipProxyId.add(chip.getSerial(), chip);
            }
        }
    }

    public int getChipNo() {
        return chipProxyId.size();
    }

    public MicroarrayManager getCMForProperty(String property) {
        for (int i = 0; i < groups.size(); i++) {
            MicroarrayManager maMgr = (MicroarrayManager) groups.get(i);
            if (maMgr.label.equalsIgnoreCase(property)) {
                return maMgr;
            }
        }
        return null;
    }

    public void cacheChips() {
        boundaries.clear();
        chipProxyId.clear();
        if (groups.size() == 0) {
            addAllChips();
        } else {
            for (int i = 0; i < groups.size(); i++) {
                MicroarrayManager chipMgr = (MicroarrayManager) groups.get(i);
                if (chipMgr.isActive) {
                    if (i > 0) {
                        boundaries.add(new Integer(chipProxyId.size()));
                    }
                    for (int j = 0; j < chipMgr.chipProxyId.size(); j++) {
                        chipProxyId.add(chipMgr.chipProxyId.get(j));
                    }
                }
            }
        }
    }

    public void removeChip(int chipId) {
        for (int i = 0; i < chipProxyId.size(); i++) {
            DSMicroarray chip = (DSMicroarray) chipProxyId.get(i);
            if (chip.getSerial() == chipId) {
                chipProxyId.remove(i);
                //System.out.println("Removed: " + String.valueOf(chipId));
            }
        }
    }

    public void addChip(int chipId) {
        for (int i = 0; i < chipProxyId.size(); i++) {
            DSMicroarray chip = (DSMicroarray) chipProxyId.get(i);
            if (chip.getSerial() == chipId) {
                break;
            }
            if (chip.getSerial() > chipId) {
                chipProxyId.add(i, chips.get(chipId));
                //System.out.println("Added: " + String.valueOf(chipId));
                break;
            }
        }
    }
}
