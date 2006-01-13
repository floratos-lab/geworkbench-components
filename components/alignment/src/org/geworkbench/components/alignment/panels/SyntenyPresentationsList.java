package org.geworkbench.components.alignment.panels;

import org.geworkbench.components.alignment.synteny.*;
import org.geworkbench.util.sequences.SequenceAnnotation;

/**
 * <p>Title: Bioworks</p>
 *
 * <p>Description: This structure holds and process structures of DotMatrices and Annotations for Synteny Modules.
 * and Genotype Analysis</p>
 *
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 *
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SyntenyPresentationsList {
    DotMatrixObj[] DotMObj = new DotMatrixObj[50];
    SequenceAnnotation[] AnnotX = new SequenceAnnotation[50];
    SequenceAnnotation[] AnnotY = new SequenceAnnotation[50];

    SyntenyAnnotationParameters SAPar=null;

    String[] AnnoKeys = {
        "PFP", "affyU133", "affyU95", "affyGnf1h", "ECgene",
        "ensGene", "genscan", "softberryGene", "geneid", "cytoBand",
        "cytoBandIdeo", "fosEndPairs", "gc5Base", "vegaGene", "HInvGeneMrna",
        "est", "intronEst", "mrna", "mzPt1Mm3Rn3Gg2_pHMM", "genomicSuperDups",
        "recombRate", "regPotential2X", "regPotential3X", "rnaCluster",
        "sgpGene",
        "snpMap", "tfbsCons", "vegaPseudoGene", "xenoEst", "xenoMrna",
        "celeraCoverage", "celeraDupPositive", "celeraOverlay", "bacEndPairs",
        "acembly"
    };

    int[] ActiveAnnotaton = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    int NumAnnoKeys = 35;
    int cur_num=0;

    public SyntenyPresentationsList() {

    }

    public void addAndDisplay(String fl,int f_x,int t_x,int f_y,int t_y){

        AnnotX[cur_num] = new SequenceAnnotation();
        AnnotY[cur_num] = new SequenceAnnotation();

        AnnotX[cur_num].setSeqSegmentStart(f_x);
        AnnotX[cur_num].setSeqSegmentEnd(t_x);
        AnnotY[cur_num].setSeqSegmentStart(f_y);
        AnnotY[cur_num].setSeqSegmentEnd(t_y);

            GPAnnoParser.runGPAnnoParser(AnnotX[cur_num], fl, 1);
            PFPParser.runPFPParser(AnnotX[cur_num], fl, 1);
//                        AnnoX.SetColors(AnnoKeys, NumAnnoKeys);
            GPAnnoParser.runGPAnnoParser(AnnotY[cur_num], fl, 2);
            PFPParser.runPFPParser(AnnotY[cur_num], fl, 2);
//                        AnnoY.SetColors(AnnoKeys, NumAnnoKeys);

        // Activating the annotations
         AdjustActiveAnnoTracks(AnnotX[cur_num]);
         AdjustActiveAnnoTracks(AnnotY[cur_num]);

        // Read dot matrix
        DotMObj[cur_num]=new DotMatrixObj();
        DotsParser dp = new DotsParser(fl,DotMObj[cur_num]);

        DotMObj[cur_num].setStartX(f_x);
        DotMObj[cur_num].setStartY(f_y);
        DotMObj[cur_num].setEndX(t_x);
        DotMObj[cur_num].setEndY(t_y);

        SAPar.setAnnotations(AnnotX[cur_num],AnnotY[cur_num]);

        DotMatrixViewWidget.drawNewDotMatrix(DotMObj[cur_num], AnnotX[cur_num], AnnotY[cur_num]);
        DotMatrixViewWidget.dmrepaint();
        cur_num++;
    }

    void setSyntenyAnnotationParameters(SyntenyAnnotationParameters sp){
        SAPar = sp;
    }

    void AdjustActiveAnnoTracks(SequenceAnnotation Anno) {
    int i, j;
    int real_an = Anno.getAnnotationTrackNum();

    for (i = 0; i < real_an; i++) {
        for (j = 0; j < NumAnnoKeys; j++) {
            if (AnnoKeys[j].compareTo(Anno.getAnnotationTrack(i).
                                      getAnnotationName()) == 0) {
                if (ActiveAnnotaton[j] == 1) {
                    Anno.setAnnoTrackActive(i, true);
                }
                else {
                    Anno.setAnnoTrackActive(i, false);
                }
            }
        }
    }
}


}
