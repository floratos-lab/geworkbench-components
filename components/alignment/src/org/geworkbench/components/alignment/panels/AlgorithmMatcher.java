package org.geworkbench.components.alignment.panels;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class AlgorithmMatcher {


    public static final String GAP0 = "Existence: 11 Extension: 1";
    public static final String GAP1 = "Existence:  9 Extension: 2";
    public static final String GAP2 = "Existence:  8 Extension: 2";
    public static final String GAP3 = "Existence:  7 Extension: 2";
    public static final String GAP4 = "Existence: 12 Extension: 1";
    public static final String GAP5 = "Existence: 10 Extension: 1";
    public static final String GAPB45_1 = "Existence: 15 Extension: 2";
    public static final String GAPB45_2 = "Existence: 13 Extension: 3";
    public static final String GAPB45_3 = "Existence: 12 Extension: 3";
    public static final String GAPB45_4 = "Existence: 11 Extension: 3";
    public static final String GAPB45_5 = "Existence: 10 Extension: 3";
    public static final String GAPB45_6 = "Existence: 14 Extension: 2";
    public static final String GAPB45_7 = "Existence: 13 Extension: 2";
    public static final String GAPB45_8 = "Existence: 12 Extension: 2";
    public static final String GAPB45_9 = "Existence: 19 Extension: 1";
    public static final String GAPB45_10 = "Existence: 18 Extension: 1";
    public static final String GAPB45_11 = "Existence: 17 Extension: 1";
    public static final String GAPB45_12 = "Existence: 16 Extension: 1";
    public static final String GAPB80_1 = "Existence: 10 Extension: 1";
    public static final String GAPB80_2 = "Existence: 8 Extension: 2";
    public static final String GAPB80_3 = "Existence: 7 Extension:2";
    public static final String GAPB80_4 = "Existence: 6 Extension: 2";
    public static final String GAPB80_5 = "Existence: 11 Extension: 1";
    public static final String GAPB80_6 = "Existence: 9 Extension: 1";
    public static final String GAPP30_1 = "Existence: 9 Extension: 1";
    public static final String GAPP30_2 = "Existence: 7 Extension: 2";
    public static final String GAPP30_3 = "Existence: 6 Extension: 2";
    public static final String GAPP30_4 = "Existence: 5 Extension: 2";
    public static final String GAPP30_5 = "Existence: 8 Extension: 1";
    public static final String GAPP70_1 = "Existence: 10 Extension: 1";
    public static final String GAPP70_2 = "Existence: 7 Extension: 2";
    public static final String GAPP70_3 = "Existence: 6 Extension: 2";
    public static final String GAPP70_4 = "Existence: 8 Extension: 2";
    public static final String GAPP70_5 = "Existence: 9 Extension: 1";
    public static final String GAPP70_6 = "Existence: 11 Extension: 1";


    public static final String MATRIX1 = "BLOSUM62";
    public static final String MATRIX2 = "BLOSUM45";
    public static final String MATRIX3 = "BLOSUM80";
    public static final String MATRIX4 = "PAM30";
    public static final String MATRIX5 = "PAM70";
    public static final String MATRIX0 = "dna.mat";

    public static final String BLASTPROGRAM1 = "blastn";
    public static final String BLASTPROGRAM2 = "blastx";
    public static final String BLASTPROGRAM3 = "tblastx";
    public static final String BLASTPROGRAM4 = "blastp";
    public static final String BLASTPROGRAM5 = "tblastn";
    public static final String BLASTPROGRAM0 = "Please select a program first.";


    public AlgorithmMatcher() {
    }

    /**
     * translate to real paramters used by GeneMatcher2.
     *
     * @param  algoTitle
     * @return String
     */
    public static String translate(String algoTitle) {
        //HashTable ht = new HashTable();
        /*    "Smith-Waterman DNA",
           "Smith-Waterman Protein",
         "Frame (for DNA sequece to protein DB)",
         "Frame (for protein sequecne to protein DB)",
         */
        if (algoTitle.equalsIgnoreCase("Smith-Waterman DNA")) {
            return "swn";

        } else if (algoTitle.equalsIgnoreCase("Smith-Waterman Protein")) {
            return "swp";
        } else if (algoTitle.equalsIgnoreCase("Pfam local alignment only")) {
            return "hmm queryset=Pfam_latest_fs_local";

        } else if (algoTitle.equalsIgnoreCase("Pfam global alignment only")) {
            return "hmm queryset=Pfam_latest_ls_global";
        } else if (algoTitle.equalsIgnoreCase(
                "Pfam global and local alignments")) {
            return "hmm queryset=Pfam_latest*";

        }

        return null;
    }

    /**
     * Match to correct database.
     * @param programName String
     * @return String[]
     */
    public static String[] translateToArray(String programName) {
        if (programName.equalsIgnoreCase("blastp")) {
            return new String[] {
                    "ncbi/nr                      Peptides of all non-redundant sequences.",
                    "ncbi/pdbaa               Peptide sequences  derived from the PDB.",
                    "ncbi/swissprot       SWISS-PROT protein sequence database.",
                    "ncbi/yeast.aa            Yeast  genomic CDS translations."};

        } else if (programName.equalsIgnoreCase("blastn")) {
            return new String[] {
                    "ncbi/nt                    All non-redundant  DNA sequences.",
                    "ncbi/pdbnt               Nucleotide sequences  derived from the PDB.",
                    "ncbi/yeast.nt           Yeast genomic nucleotide sequences."
// ,
//                    "/genomes/mouse/goldenPath_Aug2005/100/*",
//                    "/genomes/rat/goldenPath_June2003/100/*",
//                    "/genomes/chimpanzee/goldenPath_Feb2004/100/*",
//                    "/genomes/dog/goldenPath/2005_May/100/*"
            };

        } else if (programName.startsWith("tblast")) {
            return new String[] {
                    "ncbi/nt                   All non-redundant  DNA sequences.",
                    "ncbi/pdbnt               Nucleotide sequences  derived from the PDB.",
                    "ncbi/yeast.nt           Yeast genomic nucleotide sequences."};

        } else if (programName.equalsIgnoreCase("blastx")) {
            return new String[] {
                    "ncbi/nr                      Peptides of all non-redundant sequences.",
                    "ncbi/pdbaa               Peptide sequences  derived from the PDB.",
                    "ncbi/swissprot      SWISS-PROT protein sequence database.",
                    "ncbi/yeast.aa            Yeast  genomic CDS translations."};

        } else if (programName.equalsIgnoreCase("gridblast.cu-genome.org")) {
            return new String[] {"http://adgate.cu-genome.org:8080/ogsa/services/core/registry/ContainerRegistryService"};
        } else if (programName.equalsIgnoreCase("informatics40")) {
            return new String[] {"http://156.145.235.50:8081/ogsa/services/core/registry/ContainerRegistryService"};

        } else {
            return new String[] {};
        }

    }

    /**
     * Match to correct matrix.
     * @param programName String
     * @return String[]
     */
    public static String[] translateToMatrices(String programName) {
        if (programName.equalsIgnoreCase("blastn")) {
            return new String[] {MATRIX0};

        } else {
            return new String[] {MATRIX1, MATRIX2, MATRIX3, MATRIX4, MATRIX5
            };
        }

    }

    /**
     * Match matrix name with gap costs.
     * @param programName String
     * @return String[]
     */
    public static String[] translateToGapcosts(String programName) {
        if (programName.equalsIgnoreCase(MATRIX1)) {
            return new String[] {GAP0, GAP1, GAP2, GAP3, GAP3, GAP4, GAP5};

        } else if (programName.equalsIgnoreCase(MATRIX2)) {
            return new String[] {GAPB45_1, GAPB45_2, GAPB45_3, GAPB45_4,
                    GAPB45_5, GAPB45_6, GAPB45_7, GAPB45_8, GAPB45_9, GAPB45_10,
                    GAPB45_11, GAPB45_12
            };
        } else if (programName.equalsIgnoreCase(MATRIX3)) {
            return new String[] {GAPB80_1, GAPB80_2, GAPB80_3, GAPB80_4,
                    GAPB80_5, GAPB80_6};
        } else if (programName.equalsIgnoreCase(MATRIX4)) {
            return new String[] {GAPP30_1, GAPP30_2, GAPP30_3, GAPP30_4,
                    GAPP30_5};
        } else if (programName.equalsIgnoreCase(MATRIX5)) {
            return new String[] {GAPP70_1, GAPP70_2, GAPP70_3, GAPP70_4,
                    GAPP70_5, GAPP70_6};
        }

        String[] defaultGAPCOSTS = new String[] {GAP0, GAP1, GAP2, GAP3, GAP3,
                                   GAP4, GAP5};
        return defaultGAPCOSTS;

    }


    public static String translateToCommandline(ParameterSetting ps) {
        String cmd = null;
        if (ps != null) {
            if (!ps.isUseNCBI()) {
                cmd = "pb blastall -p " + ps.getProgramName() + "   -d   " +
                      ps.getDbName() + " -e " + ps.getExpect() + " -M " +
                      ps.getMatrix();
                if (ps.isLowComplexityFilterOn()) {
                    cmd += " -F T ";
                } else {
                    cmd += " -F F ";
                }
                if (ps.isMaskLowCase()) {
                    cmd += " -U T ";
                } else {
                    cmd += " -U F ";
                }
                if (ps.getWordsize() != null) {
                    cmd += " -W " + ps.getWordsize() + " ";
                }
                if (!ps.getProgramName().equals("blastn")) {

                    String gapCost = ps.getGapCost();
                    if (gapCost.equals(GAP1)) {
                        cmd += " -G 9 -E 2 ";
                    } else if (gapCost.equals(GAP2)) {
                        cmd += " -G 8 -E 2 ";
                    } else if (gapCost.equals(GAP3)) {
                        cmd += " -G 7 -E 2 ";
                    } else if (gapCost.equals(GAP4)) {
                        cmd += " -G 12 -E 1 ";
                    } else if (gapCost.equals(GAP5)) {
                        cmd += " -G 10 -E 1 ";
                    } else if (gapCost.equals(GAPB45_1)) {
                        cmd += " -G 15 -E 2 ";
                    } else if (gapCost.equals(GAPB45_2)) {
                        cmd += " -G 13 -E 3 ";
                    } else if (gapCost.equals(GAPB45_3)) {
                        cmd += " -G 12 -E 3 ";
                    } else if (gapCost.equals(GAPB45_4)) {
                        cmd += " -G 11 -E 3 ";
                    } else if (gapCost.equals(GAPB45_5)) {
                        cmd += " -G 10 -E 3 ";
                    } else if (gapCost.equals(GAPB45_6)) {
                        cmd += " -G 14 -E 2 ";
                    } else if (gapCost.equals(GAPB45_7)) {
                        cmd += " -G 13 -E 2 ";
                    } else if (gapCost.equals(GAPB45_8)) {
                        cmd += " -G 12 -E 2 ";
                    } else if (gapCost.equals(GAPB45_9)) {
                        cmd += " -G 19 -E 1 ";
                    } else if (gapCost.equals(GAPB45_10)) {
                        cmd += " -G 18 -E 1 ";
                    } else if (gapCost.equals(GAPB45_11)) {
                        cmd += " -G 17 -E 1 ";
                    } else if (gapCost.equals(GAPB45_12)) {
                        cmd += " -G 16 -E 1 ";
                    } else if (gapCost.equals(GAPB45_6)) {
                        cmd += " -G 11 -E 1 ";
                    } else if (gapCost.equals(GAPP30_1)) {
                        cmd += " -G 9 -E 1 ";
                    } else if (gapCost.equals(GAPP30_4)) {
                        cmd += " -G 5 -E 2 ";
                    } else if (gapCost.equals(GAPP30_2)) {
                        cmd += " -G 7 -E 2 ";
                    } else if (gapCost.equals(GAPP30_3)) {
                        cmd += " -G 6 -E 2 ";
                    } else if (gapCost.equals(GAPP30_5)) {
                        cmd += " -G 8 -E 1 ";
                    } else if (gapCost.equals(GAPB80_5)) {
                        cmd += " -G 11 -E 1 ";
                    } else if (gapCost.equals(GAPB80_1)) {
                        cmd += " -G 10 -E 1 ";
                    } else if (gapCost.equals(GAPB80_2)) {
                        cmd += " -G 8 -E 2 ";
                    } else if (gapCost.equals(GAPB80_3)) {
                        cmd += " -G 7 -E 2 ";
                    } else if (gapCost.equals(GAPB80_4)) {
                        cmd += " -G 6 -E 2 ";
                    } else if (gapCost.equals(GAPB80_5)) {
                        cmd += " -G 11 -E 1 ";
                    } else if (gapCost.equals(GAPB80_6)) {
                        cmd += " -G 9 -E 1 ";
                    }

                }

            } else {
                String dbName = ps.getDbName();
                String[] list = dbName.split("/");
                if (list.length > 1) {
                    String[] dbNameWithSuffix = list[list.length - 1].split(" ");
                    dbName = dbNameWithSuffix[0];
                }
                cmd = "&DATABASE=" + dbName + "&PROGRAM=" +
                      ps.getProgramName();
                if (ps.isLowComplexityFilterOn()) {
                    cmd += "&FILTER=L";
                }
                if (ps.isHumanRepeatFilterOn()) {
                    cmd += "&FILTER=R";
                }
                
                if (ps.isMaskLowCase()) {
                    cmd += "&LCASE_MASK=yes";
                }
                
                if (ps.isMaskLookupTable()) {
                    cmd += "&FILTER=m";
                }
                if (!ps.getMatrix().startsWith("dna")) {
                    cmd += "&MATRIX_NAME=" + ps.getMatrix().trim();
                }
                if (ps.getWordsize() != null) {
                    cmd += "&WORD_SIZE=" + ps.getWordsize().trim();
                }

                if (!ps.getProgramName().equals("blastn")) {

                    String gapCost = ps.getGapCost();
                    if (gapCost != null) {

                        String[] s = gapCost.split(" ");
                        if (s.length > 3) {
                            cmd += "&GAPCOSTS=" + s[1].trim() + "%20" +
                                    s[3].trim();
                        }

                    }
                }
                cmd += "&EXPECT=" + ps.getExpect() + "&MAX_NUM_SEQ=50&&COMPOSITION_BASED_STATISTICS=2&AUTO_FORMAT=Semiauto&CDD_SEARCH=on&SHOW_OVERVIEW=on&SERVICE=plain\r\n\r\n";

            }

        }
        //   System.out.println("(" + cmd + ")");
        return cmd;

    }

    /**
     * translateToWordSize
     *
     * @param selectedProgramName String
     * @return String[]
     */
    public static String[] translateToWordSize(String selectedProgramName) {
        if (selectedProgramName.trim().equalsIgnoreCase("blastn")) {
            return new String[] {"11", "7", "15"};

        } else {
            return new String[] {"3", "2"};
        }

    }

    /**
     * translateToPrograms
     *
     * @param isDNA boolean
     * @return String[]
     */
    public static String[] translateToPrograms(boolean isDNA) {
        if (isDNA) {
            return new String[] {BLASTPROGRAM0, BLASTPROGRAM1, BLASTPROGRAM2,
                    BLASTPROGRAM3};

        } else {
            return new String[] {BLASTPROGRAM0, BLASTPROGRAM4, BLASTPROGRAM5};
        }

    }
}
