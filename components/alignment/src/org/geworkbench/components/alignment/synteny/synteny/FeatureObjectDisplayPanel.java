package org.geworkbench.components.alignment.synteny;

import java.awt.*;


/**
 * <p>Title: Bioworks</p>
 *
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 *
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 *
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FeatureObjectDisplayPanel {

    public FeatureObjectDisplayPanel(Graphics g, FeatureMatrixObj FetM, int x,
                                     int y, int width, int height) {
        DisplayObject(g, FetM, x, y, width, height);
    }

    public void DisplayObject(Graphics g, FeatureMatrixObj FetM, int x, int y,
                              int width, int height) {
        int i, j, k;
        int ftsize; // size of single feature box
        double scale;

        if (FetM == null) {return;}
        if (FetM.getXnum() > FetM.getYnum()) {
            ftsize = (int) (width/ FetM.getXnum());
        }
        else {
            ftsize = (int) (height/ FetM.getYnum());
        }

        if (ftsize < 2) {
            ftsize = 2;
        }

        // draw the feature matrix
        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.white);
        g.fillRect(x, y, width, height);

        // The title
        AnnotationColors.initSmoothColdMap();

        scale = (FetM.getMax() - FetM.getMin());
        scale = scale / 255.;

        for (i = 0; i < FetM.getXnum(); i++) {
            for (j = 0; j < FetM.getYnum(); j++) {
                k = (int) ( (FetM.getMax() - FetM.getArrayCell(i, j)) / scale);
                g.setColor(AnnotationColors.SmoothColdMap[k]);
                g.fillRect(x + ftsize * i + 1,
                           y + ftsize * j + 1, ftsize - 2,
                           ftsize - 2);
            }
        }
    }
}
