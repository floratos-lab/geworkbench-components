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
    public AlgorithmMatcher() {
    }

    /**
     * translate to real paramters used by GeneMatcher2.
     *
     * @param aString String
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
                    "ncbi/yeast.nt           Yeast genomic nucleotide sequences."};

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
            return new String[] {"dna.mat"};

        } else {
            return new String[] {"BLOSUM62", "BLOSUM50", "BLOSUM100",
                    "BLOSUM50"};
        }

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
                }else{
                    cmd += " -F F ";
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
                    cmd += "&FILTER=m";
                }
                if (!ps.getMatrix().startsWith("dna")) {
                    cmd += "&MATRIX_NAME=" + ps.getMatrix().trim();
                }
                cmd += "&EXPECT=" + ps.getExpect() + "&HITLIST_SZE=500&AUTO_FORMAT=Semiauto&CDD_SEARCH=on&SHOW_OVERVIEW=on&SERVICE=plain\r\n\r\n";

            }
            //+ "&FILTER=L&HITLIST_SZE=500&AUTO_FORMAT=Semiauto&CDD_SEARCH=on&SHOW_OVERVIEW=on&SERVICE=plain\r\n\r\n";


        }
        System.out.println("(" + cmd + ")");
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
}
