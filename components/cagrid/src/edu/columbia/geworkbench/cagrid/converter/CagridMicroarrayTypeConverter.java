package edu.columbia.geworkbench.cagrid.converter;

import org.apache.commons.lang.StringUtils;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;

import edu.columbia.geworkbench.cagrid.microarray.Marker;
import edu.columbia.geworkbench.cagrid.microarray.Microarray;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;

/**
 * Converts to/from cagrid microarray set types from/to geworkbench microarray set types.
 * 
 * @author keshav
 * @version $Id: CagridMicroarrayTypeConverter.java,v 1.2 2007-01-04 22:03:15 watkinson Exp $
 */
public class CagridMicroarrayTypeConverter {

    /**
     * Convert to edu.columbia.geworkbench.cagrid.microarray.MicroarraySet from
     * org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView
     * 
     * @param microarraySetView
     * @return MicroarraySet
     */
    public static MicroarraySet convertToCagridMicroarrayType( DSMicroarraySetView microarraySetView ) {

        DSMicroarraySet microarraySet = microarraySetView.getMicroarraySet();

        /* extract microarray info from DSMicroarraySet */
        int numArrays = microarraySetView.size();
        String microarraySetName = microarraySet.getDataSetName();

        Microarray[] gridMicroarrays = new Microarray[numArrays];
        for ( int i = 0; i < numArrays; i++ ) {
            /* geworkbench array */
            DSMicroarray microarray = ( DSMicroarray ) microarraySetView.get( i );
            float data[] = microarray.getRawMarkerData();
            String name = microarray.getLabel();
            if ( name == null || StringUtils.isEmpty( name ) ) name = "i";// give array a name

            /* cagrid array */
            Microarray gridMicroarray = new Microarray();
            gridMicroarray.setArrayData( data );
            gridMicroarray.setArrayName( name );
            gridMicroarrays[i] = gridMicroarray;
        }

        /* extract marker info from DSMicroarraySet */
        int numMarkers = ( ( DSMicroarray ) microarraySet.get( 0 ) ).getMarkerNo();

        Marker[] gridMarkers = new Marker[numMarkers];
        int i = 0;
        for ( DSGeneMarker marker : ( DSItemList<DSGeneMarker> ) microarraySetView.markers() ) {
            Marker gridMarker = new Marker();
            gridMarker.setMarkerName( marker.getLabel() );
            gridMarkers[i] = gridMarker;
            i++;
        }

        /* cagrid array set */
        MicroarraySet gridMicroarraySet = new MicroarraySet();
        gridMicroarraySet.setName( microarraySetName );
        gridMicroarraySet.setMicroarray( gridMicroarrays );
        gridMicroarraySet.setMarker( gridMarkers );
        // TODO set to get(set)Microarrays and get(set)Markers

        return gridMicroarraySet;
    }

    /**
     * Convert from edu.columbia.geworkbench.cagrid.microarray.MicroarraySet to
     * org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView.
     * 
     * @param gridMicroarraySet
     * @return DSMicroarraySet
     */
    public static DSMicroarraySetView convertFromCagridMicroarrayType( MicroarraySet gridMicroarraySet ) {

        /* microarray info */
        int numMarkers = gridMicroarraySet.getMicroarray().length;
        String microarraySetName = gridMicroarraySet.getName();
        Microarray[] gridMicroarrays = gridMicroarraySet.getMicroarray();

        DSMicroarraySetView microarraySetView = new CSMicroarraySetView();
        DSMicroarraySet microarraySet = new CSMicroarraySet();
        microarraySet.setLabel( microarraySetName );

        for ( int i = 0; i < numMarkers; i++ ) {
            /* cagrid array */
            float[] arrayData = gridMicroarrays[i].getArrayData();
            String arrayName = gridMicroarrays[i].getArrayName();

            /* bison array */
            DSMicroarray microarray = new CSMicroarray( arrayData.length );
            microarray.setLabel( arrayName );
            for ( int j = 0; j < arrayData.length; j++ ) {
                DSMarkerValue markerValue = new CSExpressionMarkerValue( arrayData[j] );
                microarray.setMarkerValue( j, markerValue );
            }
            microarraySet.add( i, microarray );
        }

        // I need to add the marker names
        microarraySetView.setMicroarraySet( microarraySet );

        return microarraySetView;
    }

}
