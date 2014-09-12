package org.geworkbench.components.alignment.blast;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
	private static final String TOOL = "geWorkbench";
	private static final String CLIENT_EMAIL = "geworkbench.c2b2.columbia.edu";
	
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	List<DatabaseInfo> proteinList = new ArrayList<DatabaseInfo>();  
    	List<DatabaseInfo> nucleotideList = new ArrayList<DatabaseInfo>();
		if (databaseList != null) {
			for (DatabaseInfo d : databaseList) {
				if (d.getType().equals("protein"))
					proteinList.add(d);
				else if (d.getType().equals("nucleotide"))
					nucleotideList.add(d);
				else {
					log.error("wrong type " + d.getType() + " "
							+ d.abbreviation);
				}
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
	
	static private Map<String, String> template2number= new HashMap<String, String>();
	static{
		template2number.put("None", "0");
		template2number.put("16", "16");
		template2number.put("18", "18");
		template2number.put("21", "21");
		template2number.put("Coding", "0");
		template2number.put("Maximal", "1");
		template2number.put("Two template", "2");
	}	
	 
	static private Map<String, String> compositional2number= new HashMap<String, String>();
	static{
		compositional2number.put("No adjustment", "0");
		compositional2number.put("Composition-based statistics", "1");
		compositional2number.put("Conditional compositional score matrix adjustment", "2");
		compositional2number.put("Universal compositional score matrix adjustment", "3");		
	}
	
	static private Map<String, String> geneticCode2number=new HashMap<String, String>();
	static{
		geneticCode2number.put("Standard (1)","1");
		geneticCode2number.put("Vertebrate Mitochondrial (2)","2");
		geneticCode2number.put("Yeast Mitochondrial (3)","3");
		geneticCode2number.put("Mold Mitochondrial (4)","4");
		geneticCode2number.put("Invertebrate Mitochondrial (5)","5");
		geneticCode2number.put("Ciliate Nuclear (6)","6");
		geneticCode2number.put("Echinoderm Mitochondrial (9)","9");
		geneticCode2number.put("Euplotid Nuclear (10)","10");
		geneticCode2number.put("Bacteria and Archaea (11)","11");
		geneticCode2number.put("Alternative Yeast Nuclear (12)","12");
		geneticCode2number.put("Ascidian Mitochondrial (13)","13");
		geneticCode2number.put("Flatworm Mitochondrial (14)","14");
		geneticCode2number.put("Blepharisma Macronuclear (15)","15");
	}

	private static final String GAPB0_1 = "Linear";
    private static final String GAPB0_2 = "Existence: 5 Extension: 2";
    private static final String GAPB0_3 = "Existence: 2 Extension: 2";
    private static final String GAPB0_4 = "Existence: 1 Extension: 2";
    private static final String GAPB0_5 = "Existence: 0 Extension: 2";
    private static final String GAPB0_6 = "Existence: 3 Extension: 1";
    private static final String GAPB0_7 = "Existence: 2 Extension: 1";
    private static final String GAPB0_8 = "Existence: 1 Extension: 1";
    
    private static final String GAPB1_1 = "Existence: 4 Extension: 4";
    private static final String GAPB1_2 = "Existence: 2 Extension: 4";
    private static final String GAPB1_3 = "Existence: 0 Extension: 4";
    private static final String GAPB1_4 = "Existence: 3 Extension: 3";
    private static final String GAPB1_5 = "Existence: 6 Extension: 2";
    private static final String GAPB1_6 = "Existence: 5 Extension: 2";
    private static final String GAPB1_7 = "Existence: 4 Extension: 2";
    private static final String GAPB1_8 = "Existence: 2 Extension: 2";	
    
	private static final String GAP0 = "Existence: 11 Extension: 1";
    private static final String GAP1 = "Existence: 9 Extension: 2";
    private static final String GAP2 = "Existence: 8 Extension: 2";
    private static final String GAP3 = "Existence: 7 Extension: 2";
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
    private static final String GAPB80_3 = "Existence: 7 Extension: 2";
    private static final String GAPB80_4 = "Existence: 6 Extension: 2";
    private static final String GAPB80_5 = "Existence: 11 Extension: 1";
    private static final String GAPB80_6 = "Existence: 9 Extension: 1";
    private static final String GAPP30_1 = "Existence: 9 Extension: 1";
    private static final String GAPP30_2 = "Existence: 7 Extension: 2";
    private static final String GAPP30_3 = "Existence: 6 Extension: 2";
    private static final String GAPP30_4 = "Existence: 5 Extension: 2";
    private static final String GAPP30_5 = "Existence: 8 Extension: 1";
    private static final String GAPP30_6 = "Existence: 10 Extension: 1";
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
            return new String[] {MATRIX4,MATRIX5, MATRIX3, MATRIX1, MATRIX2 
            };
        }
    }

    /**
     * Static utility used in BlastAppComponent: Match matrix name with gap costs.
     * @param programName String
     * @return String[]
     */
    static String[] translateToGapcosts(String programName) {
    	if (programName.equalsIgnoreCase(MATRIX0)) {
            return new String[] {GAPB0_1, GAPB0_2, GAPB0_3, GAPB0_4, GAPB0_5, GAPB0_6,GAPB0_7,GAPB0_8};

        } else if (programName.equalsIgnoreCase(MATRIX1)) {
            return new String[] {GAP1, GAP2, GAP3, GAP4, GAP0, GAP5};

        } else if (programName.equalsIgnoreCase(MATRIX2)) {
            return new String[] { GAPB45_2, GAPB45_3, GAPB45_4,
                    GAPB45_5, GAPB45_1, GAPB45_6, GAPB45_7, GAPB45_8, GAPB45_9, GAPB45_10,
                    GAPB45_11, GAPB45_12
            };
        } else if (programName.equalsIgnoreCase(MATRIX3)) {
            return new String[] {GAPB80_2, GAPB80_3, GAPB80_4,
                    GAPB80_5, GAPB80_1, GAPB80_6};
        } else if (programName.equalsIgnoreCase(MATRIX4)) {
            return new String[] {GAPP30_2, GAPP30_3, GAPP30_4,
            		GAPP30_6,GAPP30_1, GAPP30_5};
        } else if (programName.equalsIgnoreCase(MATRIX5)) {
            return new String[] {GAPP70_4, GAPP70_2, GAPP70_3,  
            		GAPP70_6, GAPP70_1, GAPP70_5, };
        }

        String[] defaultGAPCOSTS = new String[] {GAP1, GAP2, GAP3, GAP4, GAP0, GAP5};
        return defaultGAPCOSTS;

    }
    
    static String[] translateToGapcosts(String selectedProgramName, String optimizeFor) {
    	if (selectedProgramName.equalsIgnoreCase("blastn")) {
    		if (optimizeFor.equalsIgnoreCase("megablast")){
    			return new String[] {GAPB0_1, GAPB0_2, GAPB0_3, GAPB0_4, GAPB0_5, GAPB0_6,GAPB0_7,GAPB0_8};
    		}
    		else {
    			return new String[] {GAPB1_1, GAPB1_2, GAPB1_3, GAPB1_4, GAPB1_5, GAPB1_6,GAPB1_7,GAPB1_8};
    		}
    		
    	}    	
    	String[] defaultGAPCOSTS = new String[] {GAP1, GAP2, GAP3, GAP4, GAP0, GAP5};
        return defaultGAPCOSTS;
    }
    
    
    static Map<String, Integer> defaultGapcostIndex = new HashMap<String, Integer>();
    static {    	
    	defaultGapcostIndex.put(MATRIX1, 4);
    	defaultGapcostIndex.put(MATRIX2, 4);
    	defaultGapcostIndex.put(MATRIX3, 4);
    	defaultGapcostIndex.put(MATRIX4, 4);
    	defaultGapcostIndex.put(MATRIX5, 4);
    	defaultGapcostIndex.put("megablast", 0);
    	defaultGapcostIndex.put("discontiguous", 5);
    }    
   
    /**
     * Static utility ussed in BlastAppComponent: translate program name to word size.
     *
     * @param selectedProgramName String
     * @return String[]
     */
   
    static String[] translateToWordSize(String selectedProgramName, String optimizeFor) {
        if (selectedProgramName.trim().equalsIgnoreCase("blastn")) {
            if(optimizeFor.trim().equalsIgnoreCase("megablast")){
            	return new String[]{"16","20","24","28","32","48","64","128","256"};
            }
            else         	
            	return new String[] {"11", "12"};
        } else {
            return new String[] {"2", "3"};
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
                    }  else if (gapCost.equalsIgnoreCase("Linear")) {
                        cmd += " -G 0 -E 0 ";
                    } else if (gapCost.equals(GAPB0_2)) {
                        cmd += " -G 5 -E 2 ";
                    } else if (gapCost.equals(GAPB0_3)) {
                        cmd += " -G 2 -E 2 ";
                    } else if (gapCost.equals(GAPB0_4)) {
                        cmd += " -G 1 -E 2 ";
                    } else if (gapCost.equals(GAPB0_5)) {
                        cmd += " -G 0 -E 2 ";
                    } else if (gapCost.equals(GAPB0_6)) {
                        cmd += " -G 3 -E 1 ";
                    } else if (gapCost.equals(GAPB0_7)) {
                        cmd += " -G 2 -E 1 ";
                    } else if (gapCost.equals(GAPB0_8)) {
                        cmd += " -G 1 -E 1 ";
                    } else if (gapCost.equals(GAPB1_1)) {
                        cmd += " -G 4 -E 4 ";
                    } else if (gapCost.equals(GAPB1_2)) {
                        cmd += " -G 2 -E 4 ";
                    } else if (gapCost.equals(GAPB1_3)) {
                        cmd += " -G 0 -E 4 ";
                    } else if (gapCost.equals(GAPB1_4)) {
                        cmd += " -G 3 -E 3 ";
                    } else if (gapCost.equals(GAPB1_5)) {
                        cmd += " -G 6 -E 2 ";
                    } else if (gapCost.equals(GAPB1_6)) {
                        cmd += " -G 5 -E 2 ";
                    } else if (gapCost.equals(GAPB1_7)) {
                        cmd += " -G 4 -E 2 ";
                    } else if (gapCost.equals(GAPB1_8)) {
                        cmd += " -G 2 -E 2 ";
                    }
                }

            } else {
                String dbName = ps.getDbName();
                cmd="";
               
                cmd += "&DATABASE=" + dbName + "&PROGRAM=" +ps.getProgramName();
                if (ps.isLowComplexityFilterOn()) {
                    cmd += "&FILTER=L";
                }
                if (ps.isHumanRepeatFilterOn()) {
                    cmd += "&FILTER=R";
                }               	
                
                if (ps.isMaskLookupTable()) {
                    cmd += "&FILTER=m";
                }
                
                cmd +="&FILTER=F";
                
                if (ps.isExcludeModelsOn()){
                	cmd += "&EXCLUDE_MODELS=yes";
                }
                if (ps.isExcludeUncultureOn()){
                	cmd += "&EXCLUDE_SEQ_UNCULT=yes";
                }
                if (!(ps.getEntrezQuery()==null)||(ps.getEntrezQuery().trim().length()!=0)){
                	String entrezQuery = ps.getEntrezQuery();
                	try {
                		entrezQuery = URLEncoder.encode(entrezQuery, "UTF-8");
        		    } catch (UnsupportedEncodingException ex) {
        			    throw new RuntimeException("UTF-8 not supported", ex);
        		    }                 	
                	cmd += "&EQ_TEXT="+entrezQuery;
                }
                if (!(ps.getFromQuery()==null)||(ps.getFromQuery().trim().length()!=0)){
                	cmd += "&QUERY_FROM="+ps.getFromQuery();
                }
                if (!(ps.getToQuery()==null)||(ps.getToQuery().trim().length()!=0)){
                	cmd += "&QUERY_TO="+ps.getToQuery();
                }
                
                if(ps.getProgramName().equalsIgnoreCase("blastn")){
                	cmd +="&PAGE=MegaBlast";
	                if (ps.isMegaBlastOn()){
	                	cmd += "&BLAST_PROGRAMS=megaBlast"; 
	                	cmd+="&MEGABLAST=on";
	            		cmd+="&SELECTED_PROG_TYPE=megaBlast";
	                }
	                else if (ps.isDiscontiguousOn()){
	                	cmd += "&BLAST_PROGRAMS=discoMegablast";
	                	cmd+="&SELECTED_PROG_TYPE=discoMegablast";
	                	cmd+="&MEGABLAST=on";
	                	
	                    if (ps.getTemplateLength() != null) {                        
	                       cmd += "&TEMPLATE_LENGTH=" + template2number.get(ps.getTemplateLength());
	                    }
	                    if (ps.getTemplateType() != null) {
	                        cmd += "&TEMPLATE_TYPE=" + template2number.get(ps.getTemplateType());
	                    }
	 
	                }
	                else if (ps.isBlastnBtnOn()){
	                	cmd += "&BLAST_PROGRAMS=blastn";
	                }
                }                
                else cmd += "&BLAST_PROGRAMS="+ps.getProgramName();
                if (ps.getProgramName().equalsIgnoreCase("blastp"))
                	cmd +="&PAGE=Proteins";
                if(ps.getProgramName().equalsIgnoreCase("blastx")||ps.getProgramName().equalsIgnoreCase("tblastn")||ps.getProgramName().equalsIgnoreCase("tblastx")){
                	cmd +="&PAGE=Translations";
                	cmd +="&PAGE_TYPE=BlastSearch";
                }
                if (ps.getProgramName().equalsIgnoreCase("blastx")||ps.getProgramName().equalsIgnoreCase("tblastx")){
                	cmd +="&GENETIC_CODE="+ geneticCode2number.get(ps.getGeneticCode());
                }
                if (ps.getProgramName().equalsIgnoreCase("blastp")||ps.getProgramName().equalsIgnoreCase("tblastn")){
                	cmd +="&COMPOSITION_BASED_STATISTICS="+ compositional2number.get(ps.getCompositionalAdjustment());                	
                }
                  
                if(ps.getMaxTargetNumber()!=null){
                	cmd+="&MAX_NUM_SEQ=" + ps.getMaxTargetNumber();
                	//cmd+="&NUM_DIFFS=3&NUM_OPTS_DIFFS=2&NUM_ORG=1&NUM_OVERVIEW=100&OLD_BLAST=false";
                }
              
                if (ps.getProgramName().equalsIgnoreCase("blastp")||ps.getProgramName().equalsIgnoreCase("blastn")){
	                if (ps.isShortQueriesOn()){
	                	cmd += "&SHORT_QUERY_ADJUST=yes";
	                }
	            	else cmd += "&SHORT_QUERY_ADJUST=";
                }
                
                if (ps.isMaskLowCase()) {
                    cmd += "&LCASE_MASK=yes";
                }
                
                
                if (!ps.getMatrix().startsWith("dna")) {
                    cmd += "&MATRIX_NAME=" + ps.getMatrix().trim();
                }
                if (ps.getWordsize() != null) {
                    cmd += "&WORD_SIZE=" + ps.getWordsize().trim();
                }
                cmd+="&HSP_RANGE_MAX=" + ps.getHspRange().trim();	
                String gapCost = ps.getGapCost();
                if ((gapCost != null)&&!(ps.getProgramName().equalsIgnoreCase("tblastx"))) {
                	if(gapCost.equalsIgnoreCase("Linear")) gapCost="Existence: 0 Extension: 0";
                    String[] s = gapCost.split(" ");
                    if (s.length > 3) {
                        cmd += "&GAPCOSTS=" + s[1].trim() + "%20" +
                                s[3].trim();
                    }
                }
                
                if(ps.getProgramName().equalsIgnoreCase("blastn")){    
	                String matchScores=ps.getMatchScores();
	                if(matchScores!=null){
	                	String[] s=matchScores.split(",");
	                	if(s.length>1){
	                		cmd+="&MATCH_SCORES=" + s[0].trim() + "%2C" + s[1].trim();                		
	                	}
	                }
                }
        		cmd+="&PAGE_TYPE=BlastSearch";
                
                Map<String, String> specie_repeat=new HashMap<String,String>();
                specie_repeat.put("Human","repeat_9606");
                specie_repeat.put("Rodents","repeat_9989");
                specie_repeat.put("Arabidopsis","repeat_3702");
                specie_repeat.put("Rice","repeat_4530");
                specie_repeat.put("Mammals","repeat_40674");
                specie_repeat.put("Fungi","repeat_4751");
                specie_repeat.put("C.elegans","repeat_6239");
                specie_repeat.put("A.gambiae","repeat_7165");
                specie_repeat.put("Zebrafish","repeat_7955");
                specie_repeat.put("Fruit fly","repeat_7227");
                if((ps.getSpeciesRepeat()!=null)&&(ps.isHumanRepeatFilterOn())){                	
                		cmd+="&REPEATS=" + specie_repeat.get(ps.getSpeciesRepeat());                
                }
             
                if (ps.getProgramName().equals("blastp")||ps.getProgramName().equals("tblastn"))	//COMPOSITION only applies to blastp and tblastn
                	cmd += "&EXPECT=" + ps.getExpect() + "&AUTO_FORMAT=Semiauto&CDD_SEARCH=on&SHOW_OVERVIEW=on&SERVICE=plain";
                else
                	cmd += "&EXPECT=" + ps.getExpect() + "&AUTO_FORMAT=Semiauto&SHOW_OVERVIEW=on&SERVICE=plain";
					
				cmd +=  "&tool=" + TOOL + "&mail=" + CLIENT_EMAIL;
				cmd +=  "\r\n\r\n";

            }

        }
        
        return cmd;
    }
}
