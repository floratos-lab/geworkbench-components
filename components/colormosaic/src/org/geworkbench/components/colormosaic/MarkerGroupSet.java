package org.geworkbench.components.colormosaic;

import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version 1.0
 */

public class MarkerGroupSet {
    public static MarkerGroupSet gtPanels = new MarkerGroupSet("panels.txt");
    ArrayList<DSPanel<EisenMarkerGroup>> panels = new ArrayList<DSPanel<EisenMarkerGroup>>();

    public MarkerGroupSet(String fileName) {
        File geneGroupFile = new File(fileName);
        BufferedReader reader;
        panels.add(new CSPanel<EisenMarkerGroup>("All"));
        try {
            reader = new BufferedReader(new FileReader(geneGroupFile));
        } catch (FileNotFoundException fnf) {
            return;
        }
        String line;
        DSPanel<EisenMarkerGroup> panel = null;
        EisenMarkerGroup group = null;
        try {
            while ((line = reader.readLine()) != null) {
                String label;
                if (line.trim().length() > 0) {
                    StringTokenizer st = new StringTokenizer(line, "\t\n\r");
                    if (st.hasMoreTokens()) {
                        label = st.nextToken();
                        if (label.equalsIgnoreCase("Panel:")) {
                            label = st.nextToken();
                            panel = new CSPanel<EisenMarkerGroup>(label);
                            panels.add(panel);
                        } else if (label.equalsIgnoreCase("Label:")) {
                            if (panel != null) {
                                label = st.nextToken();
                                group = new EisenMarkerGroup(label);
                                panel.add(group);
                            }
                        } else {
                            if (group != null) {
                                group.add(label);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public DSPanel<EisenMarkerGroup> getPanel(int i) {
        return panels.get(i);
    }

    public int size() {
        return panels.size();
    }
}
