package org.geworkbench.components.sequenceretriever;

import java.io.*;
import java.net.URL;
import java.sql.*;

import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.util.sequences.GeneChromosomeMatcher;

import javax.swing.JOptionPane;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

import java.util.*;

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
    public static final String UCSC = "UCSC";
    private static SequenceFetcher theSequenceFetcher = new SequenceFetcher();
    private final static String chiptyemapfilename = "chiptypeDatabaseMap.txt";
    private static HashMap chiptypeMap = new HashMap();
    private static ArrayList<String> chipTypes = new ArrayList<String>();
    public static String DEFAULT_CHIPTYPE = "hg18";
    public static String newline = System.getProperty("line.separator");


    static {

        BufferedReader br = new BufferedReader(new InputStreamReader(
                SequenceFetcher.class.getResourceAsStream(chiptyemapfilename)));
        try {
            String str = br.readLine();
            while (str != null) {
                String[] data = str.split(",");
                chiptypeMap.put(data[0].trim(), data[1].trim());
                chiptypeMap.put(data[1].trim(), data[0].trim());
                chipTypes.add(data[1].trim());
                str = br.readLine();
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
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
                    "http://www.ebi.ac.uk/ws/services/Dbfetch"));
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
                            CSSequence sequence = new CSSequence();
                            String label = affyid + "_" + uniprotids[i];
                            String seqStr = "";
                            for (int count = 1; count < result.length; count++) {
                                seqStr += result[count] + newline;

                            }
                            sequence = new CSSequence(label, seqStr);
                            sequenceSet.addASequence(sequence);
                            // return ;
                        }
                    }
                }
            }
        } catch (Exception e) {
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


    public static String matchChipType(String chipId) {
        if (chiptypeMap.size() == 0) {
            //no property file loaded.
            return matchChipTypeToDatabase(chipId);
        }
        String database = (String) chiptypeMap.get(chipId);
        String defaultChipChoice = database;

        if (defaultChipChoice == null) {
            defaultChipChoice = DEFAULT_CHIPTYPE;
            Object[] possibleValues = chipTypes.toArray();
            TreeSet differentValues = new TreeSet();
            for (Object o : possibleValues) {
                differentValues.add(o);
            }

            Object selectedValue = JOptionPane.showInputDialog(null,
                    "Please confirm your genome assembly",
                    "Select Assembly", JOptionPane.QUESTION_MESSAGE, null,
                    differentValues.toArray(), defaultChipChoice);
            if (selectedValue != null) {
                database = (String) selectedValue;
                if (database.equals("Other")) {
                    return null;
                }
            }
            return database;
        }
        return database;
    }

    /**
     * Only used when the property file cannot be found.
     */
    public static String matchChipTypeToDatabase(String chipType) {
        if (chipType.startsWith("HG")) {
            return "hg18";
        } else if (chipType.startsWith("M")) {
            return "mm8";
        }
        return "hg18";
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
     * @param geneName String
     * @param chipType String
     * @return any[]
     */
    public static Vector getGeneChromosomeMatchers(String
            geneName,
                                                   String database) {
        if (database == null) {
            return null;
        }
        String[] columnName = {"chrom", "strand", "txStart", "txEnd"};
        Vector<GeneChromosomeMatcher>
                vector = new Vector<GeneChromosomeMatcher>();
        try {
            Statement stmt;
            Class.forName("com.mysql.jdbc.Driver");
            String url =
                    "jdbc:mysql://genome-mysql.cse.ucsc.edu:3306/" +
                            database.trim() + "?autoReconnect=true";
            Connection con =
                    DriverManager.getConnection(
                            url, "genome", "");
            stmt = con.createStatement();
            boolean success = stmt.execute(
                    "select known.chrom, known.strand, known.txStart, known.txEnd, kg.refseq from knownGene as known, kgXref as kg  where kg.refseq = '" +
                            geneName + "' and kg.kgID = known.name ");
            if (success) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {
                    String chrom = rs.getString(columnName[0]);
                    boolean positiveStrand = true;
                    if (rs.getString(columnName[1]).equalsIgnoreCase("-")) {
                        positiveStrand = false;
                    }
                    int txStart = new Integer(rs.getString(columnName[2])).
                            intValue();
                    int txEnd = new Integer(rs.getString(columnName[3])).
                            intValue();
                    GeneChromosomeMatcher geneMatcher = new
                            GeneChromosomeMatcher(positiveStrand, chrom,
                            txStart, txEnd, database);
                    geneMatcher.setName(geneName);
                    vector.add(geneMatcher);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vector;
    }

    /**
     * getSequences
     *
     * @param geneChromosomeMatcher GeneChromosomeMatcher
     * @param upsteamRegion         int
     * @param downstreamRegion      int
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
     * getSequence
     *
     * @param genomeBuilderName String
     * @param chromosomeName    String
     * @param startPoint        int
     * @param length            int
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

}
