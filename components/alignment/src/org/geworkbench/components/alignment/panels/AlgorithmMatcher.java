package org.geworkbench.components.alignment.panels;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version $Id$
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
    
    private static class DatabaseInfo {
    	private String abbreviation;
		private String description;
    	
    	DatabaseInfo(String abbreviation, String description) {
    		this.abbreviation = abbreviation;
    		this.description = description;
    	}

    	String getAbbreviation() {
			return abbreviation;
		}

		String getDescription() {
			return description;
		}
    }

    private static final DatabaseInfo[] nucleotideDBdescription = {
			new DatabaseInfo("nr", "All GenBank+EMBL+DDBJ+PDB sequences"),
			new DatabaseInfo("refseq_mrna", "mRNA from NCBI"),
			new DatabaseInfo("refseq_genomic", "Genomic from NCBI"),
			new DatabaseInfo("est", "GenBank+EMBL+DDBJ from EST"),
			new DatabaseInfo("est_human", "Human subset of est"),
			new DatabaseInfo("est_mouse", "Mouse subset of est"),
			new DatabaseInfo("est_others", "Other than human or mouse"),
			new DatabaseInfo("gss", "Genome Survey Sequence"),
			new DatabaseInfo("htgs",
					"Unfinished High Throughput Genomic Sequences"),
			new DatabaseInfo("pat", "GenBank's Patent division nucleotides"),
			new DatabaseInfo("pdb",
					"3D structure seqeuences from Protein Data Bank"),
			new DatabaseInfo("month", "Recent GenBank+EMBL+DDBJ+PDB sequences"),
			new DatabaseInfo("alu_repeats", "Select Alu repeats from REPBASE"),
			new DatabaseInfo("dbsts", "GenBank + EMBL + DDBJ"),
			new DatabaseInfo("chromosome", "Complete chromosomes from NCBI RSP"),
			new DatabaseInfo("wgs",
					"Assemblies of Whole Genome Shotgun sequences"),
			new DatabaseInfo("env_nt", "Sequences from environmental samples") };

	private static final DatabaseInfo[] proteinDBdescription = {
			new DatabaseInfo("nr", "All GenBank+EMBL+DDBJ+PDB sequences"),
			new DatabaseInfo("refseq", "NCBI Protein sequences"),
			new DatabaseInfo("swissprot", "SWISS-PROT protein sequences"),
			new DatabaseInfo("pat", "GenBank's Patent division"),
			new DatabaseInfo("month", "Recent GenBank+EMBL+DDBJ+PDB"),
			new DatabaseInfo("pdb",
					"3D structure seqeuences from Protein Data Bank"),
			new DatabaseInfo("env_nr", "CDS translations"),
			new DatabaseInfo("Smart v4.0", "663 PSSMs from Smart"),
			new DatabaseInfo("Pfam v11.0", "7255 PSSMs from Pfam"),
			new DatabaseInfo("COG v1.00", "4873 PSSMs from NCBI COG"),
			new DatabaseInfo("KOG v1.00", "4825 PSSMs from NCBI KOG"),
			new DatabaseInfo("CDD v2.05", "11399 PSSMs from NCBI curated cd set") };
	
	private static final String[][] nucleotideDBdescriptionArray = new String[nucleotideDBdescription.length][2];
	private static final String[][] proteinDBdescriptionArray  = new String[proteinDBdescription.length][2];
	static {
		for(int i=0; i<nucleotideDBdescription.length; i++) {
			nucleotideDBdescriptionArray[i][0] = nucleotideDBdescription[i].getAbbreviation();
			nucleotideDBdescriptionArray[i][1] = nucleotideDBdescription[i].getDescription();
		}
		for(int i=0; i<proteinDBdescription.length; i++) {
			proteinDBdescriptionArray[i][0] = proteinDBdescription[i].getAbbreviation();
			proteinDBdescriptionArray[i][1] = proteinDBdescription[i].getDescription();
		}
	}
    
    private static final String[] nucleotideDBdetails = {
        "All GenBank+EMBL+DDBJ+PDB sequences\n" +
        "(but no EST, STS, GSS, or phase 0,\n" +
        "1 or 2 HTGS sequences).\n" +
        "No longer 'non-redundant'\n" +
        "due to computational cost.", // nr
        
        "mRNA sequences from NCBI\n" +
        "Reference Sequence Project.", // refseq_mrna
        
        "Genomic sequences from\n" +
        "NCBI Reference Sequence Project.", // refseq_genomic
        
        "Database of GenBank + EMBL + DDBJ\n" +
        "sequences from EST division.", // est
        
        "Human subset of est.", // est_human
        "Mouse subset of est.", // est_mouse
        "Subset of est other than human or mouse.", // est_others
        
        "Genome Survey Sequence, includes\n" + 
        "single-pass genomic data,\n" +
        "nexon-trapped sequences,\n" +
        "and Alu PCR sequences.", // gss
        
        "Unfinished High Throughput\n" +
        "Genomic Sequences:\n" +
        "phases 0, 1 and 2. Finished,\n" +
        "phase 3 HTG sequences are in nr.", // htgs
        
        "Nucleotides from the\n" +
        "Patent division of GenBank.", // pat
                
        "Sequences derived from the 3-dimensional\n" +
        "structure records from Protein Data Bank.\n" +
        "They are NOT the coding sequences for the\n" +
        "coresponding proteins found in the same\n" +
        "PDB record.", // pdb
        
        "All new or revised GenBank+EMBL+DDBJ+PDB\n" +
        "sequences released in the last 30 days.", // month
        
        "Select Alu repeats from REPBASE,\n" +
        "suitable for masking Alu repeats\n" +
        "from query sequences.\n" +
        "See 'Alu alert' by Claverie and Makalowski,\n" +
        "Nature 371: 752 (1994).", // alu_repeats
        
        "Database of Sequence Tag Site entries\n" +
        "from the STS division of\n" +
        "GenBank + EMBL + DDBJ.", // dbsts
        
        "Complete genomes and complete chromosomes\n" +
        "from the NCBI Reference Sequence project.\n" +
        "It overlaps with refseq_genomic.", // chromosome
        
        "Assemblies of Whole Genome Shotgun sequences.", // wgs
        
        "Sequences from environmental samples,\n" +
        "such as uncultured bacterial samples\n" +
        "isolated from soil or marine samples.\n" +
        "The largest single source is\n" +
        "Sagarsso Sea project. \n" + 
        "This does NOT overlap with nucleotide nr." // env_nt

    
    };

    

    private static final String[] proteinDBdetails = {
        "Non-redundant GenBank CDS translations\n" +
        "+ PDB + SwissProt + PIR + PRF, \n" +
        "excluding those in env_nr.", // nr
        
        "Protein sequences from NCBI Reference\n" +
        "Sequence project.", // refseq
        
        "Last major release of the SWISS-PROT protein\n" +
        "sequence database (no incremental updates).", // swissprot
        
        "Proteins from the Patent division of GenBank.", // pat
        "All new or revised GenBank CDS translations\n" +
        "+ PDB + SwissProt + PIR + PRF released in the\n" +
        "last 30 days.", // month
        
        "Sequences derived from the 3-dimensional\n" +
        "structure records from the Protein Data Bank.", // pdb

        "Non-redundant CDS translations\n" +
        "from env_nt entries.", // env_nr
        
        "663 PSSMs from Smart,\n" +
        "no longer actively maintained.", // Smart v4.0 ²
        
        "7255 PSSMs from Pfam, not the latest.", // Pfam v11.0 ²
        "4873 PSSMs from NCBI COG set.", // COG v1.00 ²
        
        "4825 PSSMs from NCBI KOG set\n" +
        "(eukaryotic COG equivalent).", // KOG v1.00 ²
        
        "11399 PSSMs from NCBI curated cd set." // CDD V2.05 ²
    };

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
     * 
     */
    static String[][] translateToArray(String programName) {
		if (programName.equalsIgnoreCase("blastp")) {
			return proteinDBdescriptionArray;
		} else if (programName.equalsIgnoreCase("blastn")) {
			return nucleotideDBdescriptionArray;
		} else if (programName.startsWith("tblast")) {
			return nucleotideDBdescriptionArray;
		} else if (programName.equalsIgnoreCase("blastx")) {
			return proteinDBdescriptionArray;
		} else {
			return null;
		}
	}

    public static String translateToDBdetails(String programName, int selection) {
    	if (selection < 0 ){
    		return "";
    	}
    	
		if (programName.equalsIgnoreCase("blastp")) {
			return proteinDBdetails[selection];
		} else if (programName.equalsIgnoreCase("blastn")) {
			return nucleotideDBdetails[selection];
		} else if (programName.startsWith("tblast")) {
			return nucleotideDBdetails[selection];
		} else if (programName.equalsIgnoreCase("blastx")) {
			return proteinDBdetails[selection];
		} else if (programName.equalsIgnoreCase("gridblast.cu-genome.org")) {
			return "http://adgate.cu-genome.org:8080/ogsa/services/core/registry/ContainerRegistryService";
		} else if (programName.equalsIgnoreCase("informatics40")) {
			return "http://156.145.235.50:8081/ogsa/services/core/registry/ContainerRegistryService";
		} else {
			return "";
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
                if (ps.getProgramName().equals("blastp")||ps.getProgramName().equals("tblastn"))	//COMPOSITION only applies to blastp and tblastn
                	cmd += "&EXPECT=" + ps.getExpect() + "&MAX_NUM_SEQ=50&&COMPOSITION_BASED_STATISTICS=2&AUTO_FORMAT=Semiauto&CDD_SEARCH=on&SHOW_OVERVIEW=on&SERVICE=plain\r\n\r\n";
                else
                	cmd += "&EXPECT=" + ps.getExpect() + "&MAX_NUM_SEQ=50&AUTO_FORMAT=Semiauto&CDD_SEARCH=on&SHOW_OVERVIEW=on&SERVICE=plain\r\n\r\n";
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
