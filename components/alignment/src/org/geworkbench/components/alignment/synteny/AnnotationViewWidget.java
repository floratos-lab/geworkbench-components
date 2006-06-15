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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class AnnotationViewWidget
    extends JPanel {

    int active_tracks;
    int total_tracks;
    int max_track_lines = 50;
    int element_height, i, j;
    int[] track_lines; /* hight in elements of particular track */
    int[][] feature_lines;
        /* y position of every feature in particular track */
    int[][] outOffBoundaries;
    int[][][] feature_x;
    int[][][] feature_y;
    int[] itemp = new int[4];
    int[] anglex = new int[3];
    int[] angley = new int[3];

    double scale;
    double angle;
    int panel_width;
    int panel_height;
    int panel_left;
    int between_tracks = 2;
    Color[] AnnoColor = {
        Color.black, Color.blue, Color.darkGray, Color.red};

    public AnnotationViewWidget(Graphics2D g, SequenceAnnotation sa,
                                AnnotationGraphicalObjects ago, int left,
                                int top, int width, int height, int direction,
                                int seqStart, int seqEnd) {
        int k, l, num_in_track, ms, me, js, je;

        active_tracks = sa.getActiveAnnoTrackNum();
        total_tracks = sa.getAnnotationTrackNum();

        track_lines = new int[total_tracks];
        feature_lines = new int[total_tracks][];
        feature_x = new int[total_tracks][][];
        feature_y = new int[total_tracks][][];
        outOffBoundaries = new int[total_tracks][];

        if (direction == 0 || direction == 2) {
            panel_width = width;
            panel_height = height;
            panel_left = left;
            angle = 0;
        }
        else {
            panel_width = height;
            panel_height = width;
            panel_left = top;
            angle = Math.PI + Math.PI / 2;
        }

        /* Determine parameters of drawing */
        for (i = 0; i < total_tracks; i++) {

            if (!sa.getAnnoTrackActive(i))continue;
            if (sa.getAnnotationTrack(i).getFeatureNum() < 1)continue;

            num_in_track = sa.getAnnotationTrack(i).getFeatureNum();

            /* Now we attempt to put all elements in this group as low as possible */
            feature_lines[i] = new int[num_in_track]; // feature_lines[track_num][feature_num]
            outOffBoundaries[i] = new int[num_in_track];

            for (j = 0; j < num_in_track; j++) {
                feature_lines[i][j] = 1;
                outOffBoundaries[i][j] = 0;
            }

            /* go by the features in this track */
            for (int m = 0; m < num_in_track; m++) {

                ms = sa.getAnnotationTrack(i).getSequenceHitStart(m);
                me = sa.getAnnotationTrack(i).getSequenceHitEnd(m);

                /* Go through previous record in this track and adjust height */
                for (j = 0; j < m; j++) {
                    js = sa.getAnnotationTrack(i).getSequenceHitStart(j);
                    je = sa.getAnnotationTrack(i).getSequenceHitEnd(j);

                    if (sa.getAnnotationTrack(i).getFeatureActive(j)) {
                        if (feature_lines[i][m] == feature_lines[i][j]) {
                            if ( (js >= ms && js <= me) ||
                                (je >= ms && je <= me) ||
                                (ms >= js && ms <= je) || (me >= js && me <= je)) {
                                feature_lines[i][m] = feature_lines[i][j] + 1;
                            }
                        }
                    }
                }
            }

            /* Find the height of this track (track_lines) */
            for (k = 0, l = 0; l < num_in_track; l++) {
                if (feature_lines[i][l] > k) {
                    k = feature_lines[i][l];
                }
            }
            track_lines[i] = k;
        }

        /* computing the absolute vertical position of every feature in
         every track by adding the height of previous tracks */
        for (k = 0, i = 0; i < total_tracks; i++) {
            if (!sa.getAnnoTrackActive(i))continue;
            if (sa.getAnnotationTrack(i).getFeatureNum() < 1)continue;

            for (l = 0; l < sa.getAnnotationTrack(i).getFeatureNum(); l++) {
                feature_lines[i][l] += k;
            }
            k += track_lines[i];
        }

        if (k != 0) {
            element_height = ( (panel_height - 4) / k);
        }
        else {
            return;
        }

        if (element_height > 10) {
            element_height = 10;
        }
        if (element_height < 1) {
            element_height = 1;
        }

        /* Now calculating the coordinates in pixels in current view */
        scale = ( (double) panel_width) / (seqEnd - seqStart + 1);

        for (int s = 0, i = 0; i < total_tracks; i++) {

            if (!sa.getAnnoTrackActive(i))continue;
            if (sa.getAnnotationTrack(i).getFeatureNum() < 1)continue;
            s++;

            num_in_track = sa.getAnnotationTrack(i).getFeatureNum();

            feature_x[i] = new int[num_in_track][4];
            feature_y[i] = new int[num_in_track][4];

            for (j = 0; j < num_in_track; j++) {
                int xs = sa.getAnnotationTrack(i).getSequenceHitStart(j);
                int xe = sa.getAnnotationTrack(i).getSequenceHitEnd(j);

                if (xs < seqStart && xe < seqStart)continue;
                if (xs > seqEnd && xe > seqEnd)continue;

                feature_x[i][j][0] = (int) (scale *
                                            (sa.getAnnotationTrack(i).
                                             getSequenceHitStart(j) - seqStart));
                feature_x[i][j][1] = (int) (scale *
                                            (sa.getAnnotationTrack(i).
                                             getSequenceHitEnd(j) - seqStart));

                int shift =(feature_x[i][j][1] - feature_x[i][j][0]) / 2;
                if (shift > 20) shift = 20;
                if (sa.getAnnotationTrack(i).getFeatureDirection(j)) {
                    feature_x[i][j][2] = feature_x[i][j][1] - shift;
                    feature_x[i][j][3] = feature_x[i][j][0];
                }
                else {
                    feature_x[i][j][2] = feature_x[i][j][1];
                    feature_x[i][j][3] = feature_x[i][j][0] + shift;
                }

                feature_y[i][j][1] = feature_y[i][j][0] = (feature_lines[i][j] -
                    1) *
                    element_height + s * between_tracks;
                feature_y[i][j][2] = feature_y[i][j][3] = feature_lines[i][j] *
                    element_height - 1 + s * between_tracks;

                // Control of left/right boundaries
                for (l = 0; l < 4; l++) {
                    if (feature_x[i][j][l] > panel_width) {
                        feature_x[i][j][l] = panel_width;
                        outOffBoundaries[i][j] = 1;
                    }
                    if (feature_x[i][j][l] < 0) {
                        feature_x[i][j][l] = 0;
                        outOffBoundaries[i][j] = -1;
                    }
                }
            }
        }

        FontMetrics currentMetrics = g.getFontMetrics();

        /* Now transforming and drawing */
        l = 0;
        int cumul = 0;
        Rectangle2D rctngl;
        for (int s = 0, i = 0; i < total_tracks; i++) {
            if (!sa.getAnnoTrackActive(i))continue;
            if (sa.getAnnotationTrack(i).getFeatureNum() < 1)continue;
            s++;
            num_in_track = sa.getAnnotationTrack(i).getFeatureNum();

            g.setColor(new Color(245, 245, 245));
            if (direction == 0 || direction == 2) {
                g.fillRect(left,
                           cumul * element_height + top + s * between_tracks,
                           width, track_lines[i] * element_height);
                g.setColor(Color.black);
                String anm = sa.getAnnotationTrack(i).
                                        getAnnotationName();

                rctngl = currentMetrics.getStringBounds(anm, g);
                int text_width = (int) rctngl.getWidth();

                g.drawString(anm, left - text_width - 7,
                             cumul * element_height + top +
                             (track_lines[i] * element_height) / 2 +
                             s * between_tracks);
            }
            else {
                g.fillRect(left + cumul * element_height + s * between_tracks,
                           top, track_lines[i] * element_height, height);
                g.setColor(Color.black);
                // write vertically
                g.rotate(Math.PI / 2);
                g.drawString(sa.getAnnotationTrack(i).getAnnotationName(),
                             (top + height)+7,
                             (left + cumul * element_height +
                              (track_lines[i] * element_height) / 2 +
                              s * between_tracks) * -1);
                // set back to what it was
                g.rotate( -1 * Math.PI / 2);
            }

            g.setColor(AnnoColor[s % 4]);
            cumul += track_lines[i];
            for (j = 0; j < num_in_track; j++) {
                if (direction == 0 || direction == 2) {
                    for (k = 0; k < 4; k++) {
                        feature_x[i][j][k] += left;
                        feature_y[i][j][k] += top;
                    }
                }
                else {
                    for (k = 0; k < 4; k++) {
                        itemp[k] = feature_x[i][j][k];
                        feature_x[i][j][k] = feature_y[i][j][k] + left;
                        feature_y[i][j][k] = itemp[k] + top;
                    }
                }

                if (feature_x[i][j][0] > 0 && feature_x[i][j][1] > 0) {

                    Polygon poly = new Polygon(feature_x[i][j], feature_y[i][j],
                                               4);
                    g.setColor(AnnoColor[s % 4]);
                    g.fill(poly);

                    // Filing out the ago object
                    ago.setPolygon(l, poly);
                    ago.setTrackNum(l, i);
                    ago.setNumInTrack(l++, j);
                    ago.newTotal(l);

                    if (outOffBoundaries[i][j] == 1) {
                        anglex[2] = anglex[0] = feature_x[i][j][1] + 1;
                        anglex[1] = feature_x[i][j][1] + 4;
                        angley[0] = feature_y[i][j][0];
                        angley[2] = feature_y[i][j][2];
                        angley[1] = (angley[0] + angley[2]) / 2;
                        Polygon apoly = new Polygon(anglex, angley, 3);
                        g.fill(apoly);
                    }
                    if (outOffBoundaries[i][j] == -1) {
                        anglex[2] = anglex[0] = feature_x[i][j][0] - 1;
                        anglex[1] = feature_x[i][j][0] - 4;
                        angley[0] = feature_y[i][j][0];
                        angley[2] = feature_y[i][j][2];
                        angley[1] = (angley[0] + angley[2]) / 2;
                        Polygon apoly = new Polygon(anglex, angley, 3);
                        g.fill(apoly);
                    }
                }
            }
        }
    }
}

