package edu.columbia.geworkbench.cagrid.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.columbia.geworkbench.cagrid.microarray.Marker;
import edu.columbia.geworkbench.cagrid.microarray.Microarray;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;

/**
 * @author keshav
 * @version $Id: Converter.java,v 1.1 2007-01-10 17:14:28 keshav Exp $
 */
public class Converter {

    private static final Log log = LogFactory.getLog( Converter.class );

    /**
     * @param data
     * @return MicroarraySet
     */
    public static MicroarraySet float2DToMicroarraySet( float[][] data ) {

        int numMarkers = data.length;
        int numMicroarrays = data[0].length;

        log.debug( "data set contains " + numMicroarrays + " microarrays" );
        log.debug( "data set contains " + numMarkers + " markers" );

        MicroarraySet microarraySet = new MicroarraySet();
        Microarray microarrays[] = new Microarray[numMicroarrays];
        Marker markers[] = new Marker[numMarkers];
        // FIXME should have a marker equivalent of constructing this matrix
        // set array data
        for ( int j = 0; j < numMicroarrays; j++ ) {
            float[] col = new float[numMarkers];
            for ( int i = 0; i < data.length; i++ ) {
                col[i] = data[i][j];
            }
            Microarray microarray = new Microarray();
            microarray.setArrayName( "array" + j );
            microarray.setArrayData( col );
            microarrays[j] = microarray;
        }

        // set marker names
        for ( int i = 0; i < numMarkers; i++ ) {
            Marker marker = new Marker();
            marker.setMarkerName( i + "_at" );
            markers[i] = marker;
        }

        microarraySet.setName( "A test microaray set" );
        microarraySet.setMicroarray( microarrays );
        microarraySet.setMarker( markers );
        return microarraySet;
    }
}
