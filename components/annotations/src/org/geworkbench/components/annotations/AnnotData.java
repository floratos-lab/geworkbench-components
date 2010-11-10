package org.geworkbench.components.annotations;

import java.util.ArrayList;

/**
 * AnnotData: data structure for annotation/pathway results
 * $Id$
 */

public class AnnotData{
            public ArrayList<MarkerData> markerData = new ArrayList<MarkerData>();
            public ArrayList<GeneData> geneData = new ArrayList<GeneData>();
            public ArrayList<PathwayData> pathwayData = new ArrayList<PathwayData>();
            public AnnotData(ArrayList<MarkerData> marker, ArrayList<GeneData> gene, ArrayList<PathwayData> pathway){
            	markerData = marker;
            	geneData = gene;
            	pathwayData = pathway;
            }
}
