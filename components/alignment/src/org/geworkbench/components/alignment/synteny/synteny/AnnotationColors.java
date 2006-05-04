package org.geworkbench.components.alignment.synteny;

import java.awt.*;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

public class AnnotationColors {

    public static int numColors = 256;
    public static Color[] SmoothColdMap;
    public static Color[] SmoothWarmMap;
    public static Color[] SmoothColdToWarmMap;

    public static Color[] AnnoColorMap = { /* AKA DistinctiveMap*/
        Color.black, Color.red, Color.blue, Color.green,
        Color.magenta, Color.orange, Color.cyan,
        Color.pink, Color.yellow, Color.gray,
        Color.darkGray,
        Color.black, Color.red, Color.blue, Color.green,
        Color.magenta, Color.orange, Color.cyan,
        Color.pink, Color.yellow, Color.gray,
        Color.darkGray,
        Color.black, Color.red, Color.blue, Color.green,
        Color.magenta, Color.orange, Color.cyan,
        Color.pink, Color.yellow, Color.gray,
        Color.darkGray,
        Color.black, Color.red, Color.blue, Color.green,
        Color.magenta, Color.orange, Color.cyan,
        Color.pink, Color.yellow, Color.gray,
        Color.darkGray
    };

    /*****************************************/
    public static void initSmoothColdMap() {
        SmoothColdMap = new Color[numColors];
        for (int i = 0; i < numColors; i++) {
            SmoothColdMap[i] = new Color(Color.HSBtoRGB( 0.66f, (float) (i / 255.),1.f
                 ));
        }
    }

    /*****************************************/
    public static void initSmoothWarmMap() {
        SmoothWarmMap = new Color[numColors];
        for (int i = 0; i < 256; i++) {
            SmoothWarmMap[i] = new Color(Color.HSBtoRGB(1.f, (float) (i / 255.),
                1.f));
        }
    }

//    public static Color[] AnnoColorMap = new Color[126];

    /*****************************************/
    public static void AnnotationColors() {
        /*        int i, j, k, n=0;
                for(i=0;i<3;i++)
                    for(j=0;j<3;j++)
                        for(k=3;k>0;k--)
                            AnnoColorMap[n++]=new Color(i*75,k*75,j*75);
         */
    }

    /*****************************************/
    public static Color getAnnoColor(int n) {
        return AnnoColorMap[n];
    }
}
