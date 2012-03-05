package org.geworkbench.components.colormosaic;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.util.DSPValue;

/**
 * Interface that used to be in geWorkbench core.
 * 
 * @author zji
 * @version $Id$
 *
 */
public interface DSMatrixPattern {
    /**
     * returns the registration of the best match to the object
     *
     */
	DSPValue match(DSMicroarray object);
    
    public void add(DSMicroarray array);

    public boolean containsMarker(DSGeneMarker testMarker);

    public boolean containsMarkers(CSMatrixPattern pat);

    public DSGeneMarker[] markers();

    public void init(int size);
}
