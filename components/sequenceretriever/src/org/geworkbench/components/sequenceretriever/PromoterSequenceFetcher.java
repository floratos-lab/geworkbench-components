package org.geworkbench.components.sequenceretriever;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.util.sequences.SequenceDB;

/**
 * <p>Title: geworkbench</p>
 * <p/>
 * <p> Script to retrieve Promoter sequence from UCSC's DAS sequence server</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2005</p>
 * <p>Company: Columbia University</p>
 *
 * @author Adam Margolin
 * @author Nilanjana Banerjee, manjunath at genomecenter dot columbia dot edu
 */

public class PromoterSequenceFetcher {
    
    public PromoterSequenceFetcher() {
    }
    
    private static SequenceDB cachedSequences = null;
    
    public static void populateSequenceCache(){
        if (cachedSequences == null){
            try {
                URL url = PromoterSequenceFetcher.class.getResource("All.NC.-2k+2k.txt");
                if (url == null){
                    try {
                        url = new URL(System.getProperty("data.download.site") + "All.NC.-2k+2k.txt");
                    } catch (MalformedURLException mfe){}
                }
                File sequences = new File(url.toURI());
                cachedSequences = SequenceDB.getSequenceDB(sequences);
                cachedSequences.parseMarkers();
            } catch (URISyntaxException use){}
        }
    }
    
    public static CSSequence getPromoterSequence(DSGeneMarker marker, int upstream, int fromStart) {
        if (cachedSequences == null)
            populateSequenceCache();
        
        return (CSSequence)cachedSequences.get(marker.getLabel());
    }
}