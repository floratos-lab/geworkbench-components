package org.geworkbench.components.alignment.panels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.engine.management.ComponentClassLoader;
import org.geworkbench.engine.management.ComponentResource;
import org.xml.sax.SAXException;

/**
 * Utility used by BlastAppComponent and BlastAlgorithm.
 * 
 * <p>Company: Columbia University</p>
 *
 * @author zji
 * @version $Id$
 */
public class AlgorithmMatcher {
	Log log = LogFactory.getLog(AlgorithmMatcher.class);
	
	private static AlgorithmMatcher instance;
	private List<DatabaseInfo> databaseList = null;
	
	@SuppressWarnings("unchecked")
	private AlgorithmMatcher() {
        String componentDirectory = null;
		ClassLoader classLoader = AlgorithmMatcher.class.getClassLoader();
        if (classLoader instanceof ComponentClassLoader) {
            ComponentClassLoader ccl = (ComponentClassLoader) classLoader;
            ComponentResource componentResource = ccl.getComponentResource();
            componentDirectory = componentResource.getDir();
        } else {
        	log.error("not loaded by ComponentClassLoader");
        }
            
		Digester digester = new Digester();
		digester.setValidating( false );

		digester.setUseContextClassLoader(true);
		digester.addObjectCreate("database_list", ArrayList.class);
		digester.addObjectCreate("database_list/database", DatabaseInfo.class);
		digester.addSetProperties( "database_list/database", "name", "abbreviation" );
		digester.addSetProperties( "database_list/database", "type", "type" );
		digester.addBeanPropertySetter( "database_list/database/description", "description" );
        digester.addBeanPropertySetter( "database_list/database/detail", "detail" );
        digester.addSetNext("database_list/database", "add" );

        File input = new File( componentDirectory+"/classes/databaseInfo.xml" );
        
		try {
			databaseList = (List<DatabaseInfo>)digester.parse( input );
			System.out.println("list size = "+databaseList.size()); // TODO remove
	        for(DatabaseInfo d: databaseList) {
	        	System.out.println(d.getDescription()+":"+d.getDetail()+":"+d.abbreviation+":"+d.getType());
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	List<DatabaseInfo> proteinList = new ArrayList<DatabaseInfo>();  
    	List<DatabaseInfo> nucleotideList = new ArrayList<DatabaseInfo>();  
    	for(DatabaseInfo d: databaseList) {
    		if(d.getType().equals("protein"))proteinList.add(d);
    		else if(d.getType().equals("nucleotide"))nucleotideList.add(d);
    		else {
    			log.error("wrong type "+d.getType()+" "+d.abbreviation);
    		}
    	}
    	proteinDBdescriptionArray  = new String[proteinList.size()][2];
    	int i = 0;
		for(DatabaseInfo d: proteinList) {
			proteinDBdescriptionArray[i][1] = d.getAbbreviation();
			proteinDBdescriptionArray[i][0] = d.getDescription();
			i++;
		}
		nucleotideDBdescriptionArray = new String[nucleotideList.size()][2];
    	i = 0;
		for(DatabaseInfo d: nucleotideList) {
			nucleotideDBdescriptionArray[i][1] = d.getAbbreviation();
			nucleotideDBdescriptionArray[i][0] = d.getDescription();
			i++;
		}
	} // end of constructor
	
	private String[][] nucleotideDBdescriptionArray = null;
	private String[][] proteinDBdescriptionArray = null;
	
	static AlgorithmMatcher getInstance() {
		if(instance==null) {
			instance = new AlgorithmMatcher();
		}
		return instance;
	}
	
	static public class DatabaseInfo {
		private String abbreviation;
		private String description;
		private String detail;
		private String type;
		
		public DatabaseInfo() {
		}

		public void setAbbreviation(String abbreviation) {
			this.abbreviation = abbreviation;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setDetail(String detail) {
			this.detail = detail;
		}

		public void setType(String type) {
			this.type = type;
		}
		
		String getAbbreviation() {
			return abbreviation;
		}

		String getDescription() {
			return description;
		}

		String getDetail() {
			return detail;
		}

		String getType() {
			return type;
		}
	}
	
	static private Map<String, String> program2type = new HashMap<String, String>();
	static {
		program2type.put("blastn", "nucleotide");
		program2type.put("blastp", "protein");
		program2type.put("blastx", "protein");
		program2type.put("tblastn", "nucleotide");
		program2type.put("tblastx", "nucleotide");
	}

	private static final String GAP0 = "Existence: 11 Extension: 1";
    private static final String GAP1 = "Existence:  9 Extension: 2";
    private static final String GAP2 = "Existence:  8 Extension: 2";
    private static final String GAP3 = "Existence:  7 Extension: 2";
    private static final String GAP4 = "Existence: 12 Extension: 1";
    private static final String GAP5 = "Existence: 10 Extension: 1";
    private static final String GAPB45_1 = "Existence: 15 Extension: 2";
    private static final String GAPB45_2 = "Existence: 13 Extension: 3";
    private static final String GAPB45_3 = "Existence: 12 Extension: 3";
    private static final String GAPB45_4 = "Existence: 11 Extension: 3";
    private static final String GAPB45_5 = "Existence: 10 Extension: 3";
    private static final String GAPB45_6 = "Existence: 14 Extension: 2";
    private static final String GAPB45_7 = "Existence: 13 Extension: 2";
    private static final String GAPB45_8 = "Existence: 12 Extension: 2";
    private static final String GAPB45_9 = "Existence: 19 Extension: 1";
    private static final String GAPB45_10 = "Existence: 18 Extension: 1";
    private static final String GAPB45_11 = "Existence: 17 Extension: 1";
    private static final String GAPB45_12 = "Existence: 16 Extension: 1";
    private static final String GAPB80_1 = "Existence: 10 Extension: 1";
    private static final String GAPB80_2 = "Existence: 8 Extension: 2";
    private static final String GAPB80_3 = "Existence: 7 Extension:2";
    private static final String GAPB80_4 = "Existence: 6 Extension: 2";
    private static final String GAPB80_5 = "Existence: 11 Extension: 1";
    private static final String GAPB80_6 = "Existence: 9 Extension: 1";
    private static final String GAPP30_1 = "Existence: 9 Extension: 1";
    private static final String GAPP30_2 = "Existence: 7 Extension: 2";
    private static final String GAPP30_3 = "Existence: 6 Extension: 2";
    private static final String GAPP30_4 = "Existence: 5 Extension: 2";
    private static final String GAPP30_5 = "Existence: 8 Extension: 1";
    private static final String GAPP70_1 = "Existence: 10 Extension: 1";
    private static final String GAPP70_2 = "Existence: 7 Extension: 2";
    private static final String GAPP70_3 = "Existence: 6 Extension: 2";
    private static final String GAPP70_4 = "Existence: 8 Extension: 2";
    private static final String GAPP70_5 = "Existence: 9 Extension: 1";
    private static final String GAPP70_6 = "Existence: 11 Extension: 1";

    private static final String MATRIX1 = "BLOSUM62";
    private static final String MATRIX2 = "BLOSUM45";
    private static final String MATRIX3 = "BLOSUM80";
    private static final String MATRIX4 = "PAM30";
    private static final String MATRIX5 = "PAM70";
    private static final String MATRIX0 = "dna.mat";

    /**
     * Match to correct database.
     * 
     */
    String[][] translateToArray(String programName) {
    	String type = program2type.get(programName);
		if (type==null) {
			log.error("no database type for program "+programName);
			return null;
		} else if (type.equalsIgnoreCase("protein")) {
			return proteinDBdescriptionArray;
		} else if (type.equalsIgnoreCase("nucleotide")) {
			return nucleotideDBdescriptionArray;
		} else {
			log.error("wrong database type "+type+" for program "+programName);
			return null;
		}
	}

    /**
     * Given program + the index of database, return the detail
     * @param programName
     * @param selection
     * @return
     */
    String getDatabaseDetail(String programName, String databaseName) {
    	String type = program2type.get(programName);
    	for(DatabaseInfo d: databaseList) {
    		if (d.getType().equals(type)
					&& d.getAbbreviation().equals(databaseName))
				return d.getDetail();
    	}
    	log.error("no matching database for program "+programName);
    	return null;
	}

    /**
     * Static utility used in BlastAppComponent: Match to correct matrix.
     * @param programName String
     * @return String[]
     */
    static String[] translateToMatrices(String programName) {
        if (programName.equalsIgnoreCase("blastn")) {
            return new String[] {MATRIX0};
        } else {
            return new String[] {MATRIX1, MATRIX2, MATRIX3, MATRIX4, MATRIX5
            };
        }
    }

    /**
     * Static utility used in BlastAppComponent: Match matrix name with gap costs.
     * @param programName String
     * @return String[]
     */
    static String[] translateToGapcosts(String programName) {
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

    /**
     * Static utility ussed in BlastAppComponent: translate program name to word size.
     *
     * @param selectedProgramName String
     * @return String[]
     */
    static String[] translateToWordSize(String selectedProgramName) {
        if (selectedProgramName.trim().equalsIgnoreCase("blastn")) {
            return new String[] {"11", "7", "15"};
        } else {
            return new String[] {"3", "2"};
        }

    }

    /**
     * Utility to create command-line parameter, only used by BlastAlgorithm.execute();
     * 
     * @param ps
     * @return
     */
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
                	cmd += "&EXPECT=" + ps.getExpect() + "&MAX_NUM_SEQ=50&AUTO_FORMAT=Semiauto&SHOW_OVERVIEW=on&SERVICE=plain\r\n\r\n";
            }

        }

        return cmd;
    }
}
