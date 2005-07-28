package org.geworkbench.components.alignment.synteny;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */


import org.geworkbench.util.sequences.SequenceAnnotation;
import org.geworkbench.components.alignment.synteny.*;
import org.geworkbench.components.alignment.synteny.AnnotationColors;

import javax.swing.*;
import java.awt.*;

public class AnnotationViewWidget extends JPanel {

    int active_tracks;
    int total_tracks;
    int max_track_lines = 30;
    int element_height, i, j;
    int[] track_lines; /* hight in elements of particular track */
    int[][] feature_lines; /* y position of every feature in particular track */
    int[][][] feature_x;
    int[][][] feature_y;
    int[] itemp = new int[4];
    double scale;
    double angle;
    int panel_width;
    int panel_height;


    public AnnotationViewWidget(Graphics2D g, SequenceAnnotation sa, org.geworkbench.components.alignment.synteny.AnnotationGraphicalObjects ago, int left, int top, int width, int height, int direction, int seqStart, int seqEnd) {
        int k, l, num_in_track, ms, me, js, je;

        active_tracks = sa.getActiveAnnoTrackNum();
        total_tracks = sa.getAnnotationTrackNum();

        track_lines = new int[total_tracks];
        feature_lines = new int[total_tracks][];
        feature_x = new int[total_tracks][][];
        feature_y = new int[total_tracks][][];

        if (direction == 0 || direction == 2) {
            panel_width = width;
            panel_height = height;
            angle = 0;
        } else {
            panel_width = height;
            panel_height = width;
            angle = Math.PI + Math.PI / 2;
        }

        /* Determine parameters of drawing */
        for (i = 0; i < total_tracks; i++) {
            if (sa.getAnnoTrackActive(i) == false) continue;

            num_in_track = sa.getAnnotationTrack(i).getFeatureNum();

            /* Now we attempt to put all elements in this group as low as possible */
            feature_lines[i] = new int[num_in_track];
            for (j = 0; j < num_in_track; j++) feature_lines[i][j] = 1;

            for (int m = 0; m < num_in_track; m++) { /* go by the features in this track */

                ms = sa.getAnnotationTrack(i).getSequenceHitStart(m);
                me = sa.getAnnotationTrack(i).getSequenceHitEnd(m);

                /* Go through previous record in this track */
                for (j = 0; j < m; j++) {

                    js = sa.getAnnotationTrack(i).getSequenceHitStart(j);
                    je = sa.getAnnotationTrack(i).getSequenceHitStart(j);

                    if (sa.getAnnotationTrack(i).getFeatureActive(j)) {
                        if (feature_lines[i][m] == feature_lines[i][j]) {
                            if ((js >= ms && js <= me) || (je >= ms && je <= me) || (ms >= js && ms <= je) || (me >= js && me <= je))
                                feature_lines[i][m] = feature_lines[i][j] + 1;
                        }
                    }
                }
            }

            for (k = 0, l = 0; l < num_in_track; l++)
                if (feature_lines[i][l] > k) k = feature_lines[i][l];
            track_lines[i] = k;
        }

        /* computing the absolute position of every feature in every track */
        for (k = 0, i = 0; i < total_tracks; i++) {
            if (sa.getAnnoTrackActive(i) == false) continue;
            for (l = 0; l < sa.getAnnotationTrack(i).getFeatureNum(); l++) {
                feature_lines[i][l] += k;
            }
            k += track_lines[i];
        }

        if (k != 0) {
            element_height = ((panel_height - 4) / k);
        } else {
            return;
        }
        if (element_height > 10) element_height = 10;
        scale = ((double) panel_width) / (seqEnd - seqStart + 1);


        /* Now calculating the coordinates in current view */
        for (i = 0; i < total_tracks; i++) {
            if (sa.getAnnoTrackActive(i) == false) continue;

            num_in_track = sa.getAnnotationTrack(i).getFeatureNum();

            feature_x[i] = new int[num_in_track][4];
            feature_y[i] = new int[num_in_track][4];

            for (j = 0; j < num_in_track; j++) {

                feature_x[i][j][0] = (int) (scale * (sa.getAnnotationTrack(i).getSequenceHitStart(j) - seqStart));
                feature_x[i][j][1] = (int) (scale * (sa.getAnnotationTrack(i).getSequenceHitEnd(j) - seqStart));

                if (sa.getAnnotationTrack(i).getFeatureDirection(j)) {
                    feature_x[i][j][2] = feature_x[i][j][0] + (feature_x[i][j][1] - feature_x[i][j][0]) / 2;
                    feature_x[i][j][3] = feature_x[i][j][0];
                } else {
                    feature_x[i][j][2] = feature_x[i][j][1];
                    feature_x[i][j][3] = feature_x[i][j][0] + (feature_x[i][j][1] - feature_x[i][j][0]) / 2;
                }

                feature_y[i][j][1] = feature_y[i][j][0] = feature_lines[i][j] * element_height;
                feature_y[i][j][2] = feature_y[i][j][3] = (feature_lines[i][j] + 1) * element_height - 1;

                for (l = 0; l < 4; l++) {
                    if (feature_x[i][j][l] > 450) feature_x[i][j][l] = 455;
                    if (feature_x[i][j][l] < 10) feature_x[i][j][l] = 8;
                }
            }
        }

        /* Now transforming and drawing */
        l = 0;
        for (i = 0; i < total_tracks; i++) {
            if (sa.getAnnoTrackActive(i) == false) continue;

            g.setColor(AnnotationColors.AnnoColorMap[sa.getAnnotationTrack(i).getColorNum()]);

            num_in_track = sa.getAnnotationTrack(i).getFeatureNum();
            for (j = 0; j < num_in_track; j++) {

                if (direction == 0 || direction == 2) {
                    for (k = 0; k < 4; k++) {
                        feature_x[i][j][k] += left;
                        feature_y[i][j][k] += top;
                    }
                } else {
                    for (k = 0; k < 4; k++) {
                        itemp[k] = feature_x[i][j][k];
                        feature_x[i][j][k] = feature_y[i][j][k] + left;
                        feature_y[i][j][k] = itemp[k] + top;
                    }
                }
                if (feature_x[i][j][0] > 0 && feature_x[i][j][1] > 0) {

                    Polygon poly = new Polygon(feature_x[i][j], feature_y[i][j], 4);

                    g.setColor(AnnotationColors.AnnoColorMap[sa.getAnnotationTrack(i).getColorNum()]);
                    g.fill(poly);

                    ago.setPolygon(l, poly);
                    ago.setTrackNum(l, i);
                    ago.setNumInTrack(l++, j);
                    ago.newTotal(l);
                }
            }
        }
    }
}

