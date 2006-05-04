package org.geworkbench.components.alignment.synteny;

import org.geworkbench.util.sequences.SequenceAnnotation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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


public class SyntenyAnnotationParameters
    extends JPanel {

    SyntenyPresentationsList SPL=null;
    SequenceAnnotation AnnoX = null;
    SequenceAnnotation AnnoY = null;

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
    int[] iniAnnoSelect = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    JCheckBox[] AnnoBoxes = new JCheckBox[NumAnnoKeys];
    JScrollPane scrollPane = null;
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel AnnotationSelection = new JPanel();

    public SyntenyAnnotationParameters() {
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {

        scrollPane = new JScrollPane(AnnotationSelection);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.
                                                HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.
                                              VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(300, 360));

        /* Setting specific values for demonstration */
        this.setLayout(borderLayout1);
        this.setBackground(SystemColor.desktop);
        this.setDoubleBuffered(true);
        this.setMaximumSize(new Dimension(32767, 32767));
        this.setMinimumSize(new Dimension(325, 300));
        this.setPreferredSize(new Dimension(315, 380));
        this.setToolTipText("");
        AnnotationSelection.setLayout(new GridLayout(0, 1));

        for (int tt = 0; tt < NumAnnoKeys; tt++) {
            AnnoBoxes[tt] = new JCheckBox(AnnoKeys[tt]);
            if (ActiveAnnotaton[tt] == 1) {
                AnnoBoxes[tt].setSelected(true);
            }
            AnnotationSelection.add(AnnoBoxes[tt]);
        }

        this.add(scrollPane, java.awt.BorderLayout.CENTER);
        scrollPane.getViewport().add(AnnotationSelection);
    }

    public void setAnnotations(SequenceAnnotation ax, SequenceAnnotation ay){
        AnnoX = ax;
        AnnoY = ay;
    }

    public String getAnnoString(){
        String anno_status = new String("");

        for (int i = 0; i < NumAnnoKeys; i++) {
            if (AnnoBoxes[i].isSelected()) {
                anno_status=anno_status.concat("1");
            } else {
                anno_status=anno_status.concat("0");
            }
        }
        return anno_status;
    }

    /**********************************************************/
    public void setSyntenyPresentationsList(SyntenyPresentationsList sl){
        SPL=sl;
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