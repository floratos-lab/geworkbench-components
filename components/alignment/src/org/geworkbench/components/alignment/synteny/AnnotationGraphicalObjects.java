package org.geworkbench.components.alignment.synteny;


import org.geworkbench.util.sequences.SequenceAnnotation;

import java.awt.*;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

public class AnnotationGraphicalObjects {

    private Polygon[] polygons;
    private int[] track_num;    // index of track to wich belongs each feature
    private int[] num_in_track; // index of particular feature in it's own track
    private int fnum;

    public AnnotationGraphicalObjects(SequenceAnnotation sa) {
        int i;

        for (fnum = 0, i = 0; i < sa.getAnnotationTrackNum(); i++) {
            fnum += ( (sa.getAnnotationTrack(i)).getFeatureNum());
        }

        polygons = new Polygon[fnum];
        track_num = new int[fnum];
        num_in_track = new int[fnum];
    }

    /**
     * accessors
     */
    public Polygon getPolygon(int n) {
        return polygons[n];
    }

    public int getTrackNum(int n) {
        return track_num[n];
    }

    public int getNumInTrack(int n) {
        return num_in_track[n];
    }

    /**
     * modifyers
     */
    public void setPolygon(int n, Polygon pol) {
        polygons[n] = pol;
    }

    public void setTrackNum(int n, int tn) {
        track_num[n] = tn;
    }

    public void setNumInTrack(int n, int nit) {
        num_in_track[n] = nit;
    }

    public void newTotal(int n) {
        fnum = n;
    }

    public int getHit(int x, int y) {
        int i;
        for (i = 0; i < fnum; i++) {
            if (polygons[i] != null) {
                if (polygons[i].contains(x, y)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
