package org.geworkbench.components.sequenceretriever;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.components.parsers.RMAExpressFileFormat;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.sequences.GeneChromosomeMatcher;

/**
 * <p>Title: </p>
 * <p/>
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author XZ
 * @version 1.0
 */
public class SequenceFetcher {
	static Log log = LogFactory.getLog(RMAExpressFileFormat.class);
    public static final String UCSC = "UCSC";
    private static SequenceFetcher theSequenceFetcher = new SequenceFetcher();
    private final static String chiptyemapfilename = "chiptypeDatabaseMap.txt";
    private static HashMap<String, String> chiptypeMap = new HashMap<String, String>();
    private static ArrayList<String> chipTypes = new ArrayList<String>();
    public static final String newline = System.getProperty("line.separator");
    public static String UCSCDATABASEURL = "jdbc:mysql://genome-mysql.cse.ucsc.edu:3306/";
    public static final String EBIURL = "http://www.ebi.ac.uk/ws/services/Dbfetch";
    public static int UPSTREAM = 2000;
    public static int DOWNSTREAM = 2000;

    private static String genomeAssembly = "";

    private static CSSequenceSet cachedSequences = null;
	private static ArrayList<String> allDBs = new ArrayList<String>();

    public static void populateSequenceCache() {
        File file = new File( FilePathnameUtils.getTemporaryFilesDirectoryPath() +
                "sequences" + File.separator +
                "cachedSequences" );
        if (cachedSequences == null) {
            if (file.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    cachedSequences = (CSSequenceSet) ois.readObject();
                    ois.close();
                    fis.close();
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                }
            } else {
                URL url =  SequenceFetcher.class.getResource(
                        "All.NC.-2k+2k.txt");
                File downloadedFile =
                        new File(FilePathnameUtils.getTemporaryFilesDirectoryPath() +
                                "sequences" + File.separator +
                                "downloadedSequences");
                try {
                    if (!downloadedFile.exists()) {
                        downloadedFile.getParentFile().mkdirs();
                        downloadedFile.createNewFile();
                        url = new URL(System.getProperty("data.download.site") +
                                "All.NC.-2k+2k.txt");
                        BufferedReader br = new BufferedReader(new
                                InputStreamReader(url.openStream()));
                        BufferedWriter bw = new BufferedWriter(new FileWriter(
                                downloadedFile));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            bw.write(line);
                            bw.write("\n");
                        }
                        bw.flush();
                        br.close();
                        bw.close();
                    }
                } catch (MalformedURLException mfe) {
                    mfe.printStackTrace();
                } catch (FileNotFoundException fnfe) {

                } catch (IOException ioe) {

                }
                try {
                    cachedSequences = CSSequenceSet.getSequenceDB(
                            downloadedFile);
                    cachedSequences.parseMarkers();
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file.
                            getAbsolutePath());
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(cachedSequences);
                    oos.flush();
                    oos.close();
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    public static CSSequence getCachedPromoterSequence(DSGeneMarker marker,
                                                       int upstream, int fromStart) {
        if (cachedSequences == null) {
            populateSequenceCache();
        }
        if (cachedSequences != null) {
            CSSequence sequence = (CSSequence) cachedSequences.get(marker.
                    getLabel());
            if (sequence != null) {
                return sequence.getSubSequence(UPSTREAM - upstream - 1,
                        sequence.length() - DOWNSTREAM +
                                fromStart - 1);
            }
        }
        return null;
    }

	/*
	 * Get databases from the USCS MySQL server 
	 * which have the highest version number.
	 * 
	 * Look up the database name (without a version number) 
	 * from the chiptypeDatabaseMap.txt file
	 * based on the name of annotation file associated with  
	 * the data set in the Project panel.
	 */
    static {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                SequenceFetcher.class.getResourceAsStream(chiptyemapfilename)));
        try {
            String str = br.readLine();
            while (str != null && str.contains(",")) {
                String[] data = str.split(",");
                chiptypeMap.put(data[0].trim(), data[1].trim());
                chipTypes.add(data[1].trim());
                str = br.readLine();
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    	Statement statement ;
		try {
		Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException cnfe) {
			JOptionPane.showMessageDialog(null, ClassNotFoundException.class
					.getSimpleName()
					+ " for jdbc driver.", "Have you installed jdbc driver?",
					JOptionPane.ERROR_MESSAGE);
			log.error(cnfe, cnfe);
		}
			
		String dburl = UCSCDATABASEURL;
		Connection connection;
		try {
			connection = DriverManager.getConnection(dburl, "genome", "");
			statement = connection.createStatement();
			statement.execute("show databases");
			ResultSet resultSet = statement .getResultSet();
			while ( resultSet.next() ){
				allDBs.add( resultSet.getString(1));
			}
		    Collections.sort(allDBs, new VersionComparator());
		} catch (Exception e) {
			log.error(e);
		}
    }


    public SequenceFetcher() {
    }

    public static SequenceFetcher getSequenceFetcher() {
        return theSequenceFetcher;
    }

    public String translateFromAffIDtoRefSeqID(String affID) {
        return null;
    }


    public static CSSequenceSet getAffyProteinSequences(String affyid) {
        CSSequenceSet sequenceSet = new CSSequenceSet();
        try {

            Call call = (Call) new Service().createCall();
            call.setTargetEndpointAddress(new java.net.URL(
                    EBIURL));
            call.setOperationName(new QName("urn:Dbfetch", "fetchData"));
            call.addParameter("query", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("format", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("style", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.SOAP_ARRAY);
            String[] uniprotids = AnnotationParser.getInfo(affyid,
                    AnnotationParser.SWISSPROT);
            if (uniprotids != null) {
                for (int i = 0; i < uniprotids.length; i++) {
                    if (uniprotids[i] != null &&
                            !uniprotids[i].trim().equals("")) {
                        String[] result = (String[]) call.invoke(new Object[]{
                                "uniprot:" + uniprotids[i].trim(), "fasta",
                                "raw"});

                        if (result.length == 0) {
                            System.out.println("hmm...something wrong :-(\n");
                        } else {
                            if (result[0].trim().startsWith("<html>")) {
                                //retrieved some error message. skip this sequence.
                                continue;
                            }
                            CSSequence sequence = new CSSequence();
                            String label = affyid + "_" + uniprotids[i];
                            String seqStr = "";
                            for (int count = 1; count < result.length; count++) {
                               // seqStr += result[count] + newline;
                                      seqStr += result[count];//new line causes troubles when blastp directly. xz
                            }
                            sequence = new CSSequence(label, seqStr);
                            sequenceSet.addASequence(sequence);
                        }
                    }
                }
            }
        } catch (Exception e) {
        	if ( e.getMessage().contains("DbfNoEntryFoundException"))
        	 	 log.info("No result found for affyid " + affyid );
        	else
                 e.printStackTrace();
        }
        return sequenceSet;
    }

    public static CSSequence[] getSequences(String geneName, String source) {
        if (source.equals(UCSC)) {
            return getSequences(geneName);
        }
        return null;
    }

    protected static String matchChipType(String chipId) {

		String defaultChipChoice = "";
		TreeSet<String> differentValues = new TreeSet<String>();
		Iterator<String> fileIterator = chiptypeMap.keySet().iterator();
		while (fileIterator.hasNext()) {
			String annotationFileSegment = (String) fileIterator.next();
			String dbWithoutVersion = (String) chiptypeMap
					.get(annotationFileSegment);

			String db = "";
			Iterator<String> allDBsIterator = allDBs.iterator();
			while (allDBsIterator.hasNext()) {
				db = (String) allDBsIterator.next();
				if (db.startsWith(dbWithoutVersion)) {
					differentValues.add(db);

					if (chipId.contains(annotationFileSegment)) {
						defaultChipChoice = db;
					}
					break;
				}
			}
		}

		Object selectedValue = JOptionPane.showInputDialog(null,
				"Please confirm your genome assembly", "Select Assembly",
				JOptionPane.QUESTION_MESSAGE, null, differentValues.toArray(),
				defaultChipChoice);
		if (selectedValue != null) {
			genomeAssembly = (String) selectedValue;
		}
		String database = (String) selectedValue;
		return database;
	}

    /**
     * getSequences
     *
     * @param geneName String
     * @return CSSequence[]
     */
    private static CSSequence[] getSequences(String geneName) {
        return null;
    }

    /**
	 * getGeneChromosomeMatchers
	 *
	 * @param geneName
	 *            String
	 * @param database
	 *            String
	 * @return any[]
     * @throws SQLException
	 */
	public static Vector getGeneChromosomeMatchers(String geneName,
			String database) throws SQLException {
		if (database == null) {
			return null;
		}
		String[] columnName = { "chrom", "strand", "txStart", "txEnd" };
		Vector<GeneChromosomeMatcher> vector = new Vector<GeneChromosomeMatcher>();
		try {
			Statement stmt;
			Class.forName("com.mysql.jdbc.Driver");

			String url = UCSCDATABASEURL + database.trim()
					+ "?autoReconnect=true";
			Connection con = DriverManager.getConnection(url, "genome", "");
			stmt = con.createStatement();
			boolean success = stmt
					.execute("select known.chrom, known.strand, known.txStart, known.txEnd, kg.refseq from knownGene as known, kgXref as kg  where kg.refseq = '"
							+ geneName + "' and kg.kgID = known.name ");
			if (success) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					String chrom = rs.getString(columnName[0]);
					boolean positiveStrand = true;
					if (rs.getString(columnName[1]).equalsIgnoreCase("-")) {
						positiveStrand = false;
					}
					int txStart = new Integer(rs.getString(columnName[2]))
							.intValue();
					int txEnd = new Integer(rs.getString(columnName[3]))
							.intValue();
					GeneChromosomeMatcher geneMatcher = new GeneChromosomeMatcher(
							positiveStrand, chrom, txStart, txEnd, database);
					geneMatcher.setName(geneName);
					vector.add(geneMatcher);
				}
			}
		} catch (SQLException sqle) {
			throw sqle;
		} catch (ClassNotFoundException cnfe) {
			JOptionPane.showMessageDialog(null, ClassNotFoundException.class
					.getSimpleName()
					+ " for jdbc driver.", "Have you installed jdbc driver?",
					JOptionPane.ERROR_MESSAGE);
			log.error(cnfe, cnfe);
		} catch (Exception e) {
			log.error(e);
		}
		return vector;
	}

    /**
	 * Retrieve sequences based on the chromosome position of the gene.
	 *
	 * @param geneChromosomeMatcher
	 * @param upstreamRegion
	 * @param downstreamRegion
	 * @return
	 */
    public CSSequence getSequences(GeneChromosomeMatcher
            geneChromosomeMatcher,
                                   int upstreamRegion,
                                   int downstreamRegion) {
        if (geneChromosomeMatcher == null) {
            return null;
        }
        CSSequence sequence = null;
        if (geneChromosomeMatcher != null) {
            int upStartPoint = geneChromosomeMatcher.getStartPoint();
            int downStartPoint = geneChromosomeMatcher.getEndPoint();
            int startPoint = upStartPoint - upstreamRegion;
            int endPoint = upStartPoint + downstreamRegion - 1;
            if (!geneChromosomeMatcher.isPositiveStrandDirection()) {
                startPoint = downStartPoint - downstreamRegion + 1;
                endPoint = downStartPoint + upstreamRegion;
            }
            sequence = getSequence(geneChromosomeMatcher.
                    getGenomeBuildNumber(),
                    geneChromosomeMatcher.getChr(),
                    startPoint, endPoint,
                    geneChromosomeMatcher.
                            isPositiveStrandDirection());
        }
        return sequence;
    }

    /**
     * Contact UCSC to get the sequence back. Real workhorse.
     *
     * @param genomeBuilderName
     * @param chromosomeName
     * @param startPoint
     * @param endPoint
     * @param isPositiveStrand
     * @return
     */
    private CSSequence getSequence(String genomeBuilderName,
                                   String chromosomeName,
                                   int startPoint, int endPoint,
                                   boolean isPositiveStrand) {

        int maxSize = 1000000;
        String request = "http://genome.cse.ucsc.edu/cgi-bin/das/" +
                genomeBuilderName + "/dna?segment=" +
                chromosomeName +
                ":" + startPoint + ":" + endPoint;
        ;

        try {
            InputStream uin = new URL(request).openStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    uin));
            String line;
            StringBuffer sequenceContent = new StringBuffer();
            boolean reachStartPoint = false;
            boolean reachEndPoint = false;
            while ((line = in.readLine()) != null) {
                int size = 0;
                if (line.trim().startsWith("<DNA")) {
                    reachStartPoint = true;
                    String[] str = line.split(">");
                    if (str.length > 1) {
                        sequenceContent.append(str[1]);
                        size = str[1].length();
                    }

                    String label = "request";

                    while ((line = in.readLine()) != null &&
                            !line.trim().endsWith("DNA>")) {
                        size += line.length();
                        if (size >= maxSize) {

                        }
                        sequenceContent.append(line);
                    }
                    String content = sequenceContent.toString();
                    if (!isPositiveStrand) {
                        content = CSSequence.reverseString(content);
                    }
                    CSSequence seq = new CSSequence(label,
                            content);
                    return seq;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static String getGenomeAssembly()
    {
    	return genomeAssembly;
    }

}

/**
 * 
 * @author tg2321
 *
 * compares Strings of the format hg18 to hg19
 * and sorts on the integer part of the String
 * moving the later versions to the beginning.
 * 
 */
class VersionComparator implements Comparator<Object> {
	public int compare(Object o1, Object o2) {
		if (o1 instanceof String && o2 instanceof String) {
			String string1 = (String) o1;
			String string2 = (String) o2;
			Pattern integersOnly = Pattern.compile("\\d+");
			Matcher matcher1 = integersOnly.matcher(string1);
			boolean foundInteger1 = matcher1.find();
			Matcher matcher2 = integersOnly.matcher(string2);
			boolean foundInteger2 = matcher2.find();
			if (!foundInteger1 && !foundInteger2) {
				return 0;
			}
			if (!foundInteger1) {
				return 1;
			}
			if (!foundInteger2) {
				return -1;
			}

			String versionNumber1 = matcher1.group();
			String versionNumber2 = matcher2.group();
			Integer versionInt1 = new Integer(versionNumber1);
			Integer versionInt2 = new Integer(versionNumber2);

			int compareOut = versionInt1.compareTo(versionInt2); 
			
			return -1 * compareOut ;
		}

		return 0;
	}
}
