package org.geworkbench.components.alignment.client;

import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: AMDeC_Califano lab</p>
 *
 * @author XZ
 * @version 1.0
 */

public class AlignmentResultEvent extends ProjectNodeAddedEvent {

    private String path;

    public AlignmentResultEvent(String path, String message, DSDataSet dataSet, DSAncillaryDataSet ancDataSet) {

        super(message, dataSet, ancDataSet);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
