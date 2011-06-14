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
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.sequences.GeneChromosomeMatcher;

/**
 * 
 * The class containing the static methods that handle the remote retrieving.
 * 
 * All the fields are static.
 * 
 * @author XZ
 * @version $Id$
 */
public class SequenceFetcher {
	private static Log log = LogFactory.getLog(SequenceFetcher.class);

	private static final String UCSCDATABASEURL = "jdbc:mysql://genome-mysql.cse.ucsc.edu:3306/";
	private static final String EBIURL = "http://www.ebi.ac.uk/ws/services/Dbfetch";
	private static final int UPSTREAM = 2000;
	private static final int DOWNSTREAM = 2000;

	private static String genomeAssembly = "";

	private static CSSequenceSet<CSSequence> cachedSequences = null;
	private static SortedMap<String, String> recentDBs = new TreeMap<String, String>();

	private static ArrayList<String> displayList = new ArrayList<String>();
	private static String defaultChipChoice = "Select a genome";
	private static Object selectedValue = null;

	@SuppressWarnings("unchecked")
	private static void populateSequenceCache() {
		File file = new File(FilePathnameUtils.getTemporaryFilesDirectoryPath()
				+ "sequences" + File.separator + "cachedSequences");

		if (file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				ObjectInputStream ois = new ObjectInputStream(fis);
				cachedSequences = (CSSequenceSet<CSSequence>) ois.readObject();
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
			File downloadedFile = new File(
					FilePathnameUtils.getTemporaryFilesDirectoryPath()
							+ "sequences" + File.separator
							+ "downloadedSequences");
			try {
				if (!downloadedFile.exists()) {
					downloadedFile.getParentFile().mkdirs();
					downloadedFile.createNewFile();
					URL url = new URL(System.getProperty("data.download.site")
							+ "All.NC.-2k+2k.txt");
					BufferedReader br = new BufferedReader(
							new InputStreamReader(url.openStream()));
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
				fnfe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			try {
				cachedSequences = CSSequenceSet.getSequenceDB(downloadedFile);
				cachedSequences.parseMarkers();
				if (!file.exists()) {
					file.getParentFile().mkdirs();
					file.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(
						file.getAbsolutePath());
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

	static CSSequence getCachedPromoterSequence(DSGeneMarker marker,
			int upstream, int fromStart) {
		if (cachedSequences == null) {
			populateSequenceCache();
		}
		if (cachedSequences != null) {
			CSSequence sequence = (CSSequence) cachedSequences.get(marker
					.getLabel());
			if (sequence != null) {
				return sequence.getSubSequence(UPSTREAM - upstream - 1,
						sequence.length() - DOWNSTREAM + fromStart - 1);
			}
		}
		return null;
	}

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException cnfe) {
			log.error(cnfe, cnfe);
		}

		Connection connection;
		try {
			connection = DriverManager.getConnection(UCSCDATABASEURL
					+ "hgcentral", "genome", "");
			Statement statement = connection.createStatement();
			statement.execute("SELECT * FROM defaultDb");
			ResultSet resultSet = statement.getResultSet();
			List<String> defaultDb = new ArrayList<String>();
			while (resultSet.next()) {
				defaultDb.add(resultSet.getString("name"));
			}
			statement.execute("SELECT * FROM dbDb");
			resultSet = statement.getResultSet();
			while (resultSet.next()) {
				String name = resultSet.getString("name");
				boolean included = false;
				for(String 	dbName: defaultDb) {
					if(name.equals(dbName)) {
						included = true;
						break; // found match
					}
				}
				if(included) {
					String s = resultSet.getString("description");
					int index1 = s.indexOf("(")+1;
					int index2 = s.indexOf(")");
					String organism = resultSet.getString("organism");
					recentDBs.put(name, organism+" - "+s.substring(index1, index2));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static CSSequenceSet<CSSequence> getAffyProteinSequences(String affyid)
			throws RemoteException {
		CSSequenceSet<CSSequence> sequenceSet = new CSSequenceSet<CSSequence>();
		String[] uniprotids = AnnotationParser.getInfo(affyid,
				AnnotationParser.SWISSPROT);
		if (uniprotids == null)
			return sequenceSet;

		try {
			Call call = (Call) new Service().createCall();
			call.setTargetEndpointAddress(new java.net.URL(EBIURL));
			call.setOperationName(new QName("urn:Dbfetch", "fetchData"));
			call.addParameter("query", XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter("format", XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter("style", XMLType.XSD_STRING, ParameterMode.IN);
			call.setReturnType(XMLType.SOAP_ARRAY);

			for (String swissprot : uniprotids) {
				if (swissprot == null || swissprot.trim().equals(""))
					continue;

				String[] result = (String[]) call.invoke(new Object[] {
						"uniprot:" + swissprot.trim(), "fasta", "raw" });

				if (result.length == 0) {
					log.warn("empty result from "+EBIURL);
					continue;
				}

				if (result[0].trim().startsWith("<html>")) {
					log.warn("error message returned: " + result[0]);
					// retrieved some error message. skip this
					// sequence.
					continue;
				}
				StringBuffer seqStr = new StringBuffer();
				for (int count = 1; count < result.length; count++) {
					seqStr.append( result[count] );
				}
				CSSequence sequence = new CSSequence(affyid + "_" + swissprot.trim(), seqStr.toString());
				sequenceSet.addASequence(sequence);
			}
		} catch (ServiceException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (MalformedURLException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (RemoteException e) {
			// DbfNoEntryFoundException & DbfConnException will be caught here
			log.warn(e.getMessage());
			if (!e.toString().contains("DbfNoEntryFoundException")) // do not
																	// re-throw
																	// if it is
																	// DbfNoEntryFoundException
				throw e;
		} catch (NoClassDefFoundError e) { // runtime
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		return sequenceSet;
	}

	static String matchChipType(String chipId, String annotationFileName) {

		displayList.clear();
		displayList.add("Select a genome");

		for (String dbName : recentDBs.keySet()) {

			String displayString = recentDBs.get(dbName);

			displayList.add(displayString);

			if (annotationFileName.contains("HG_") && dbName.startsWith("hg")) {
				defaultChipChoice = displayString;
			} else if (annotationFileName.contains("Mouse")
					&& dbName.startsWith("mm")) {
				defaultChipChoice = displayString;
			} else if (annotationFileName.contains("Rat")
					&& dbName.startsWith("rn")) {
				defaultChipChoice = displayString;
			}

		}
		showGenomeDialog();

		String database = null;
		if (selectedValue != null && !selectedValue.equals("Select a genome")) {
			genomeAssembly = (String) selectedValue;
			String choice = (String) selectedValue;
			database = choice.substring(choice.indexOf("/") + 1);
		}

		return database;
	}

	private static void showGenomeDialog() {
		Runnable showInputDialog = new Runnable() {
			public void run() {
				selectedValue = JOptionPane
						.showInputDialog(
								null,
								"Please select a species.\nIts latest genome version\nfrom UCSC will be used.",
								"Confirm Genome Version",
								JOptionPane.QUESTION_MESSAGE, null,
								displayList.toArray(), defaultChipChoice);
			}
		};
		try {
			SwingUtilities.invokeAndWait(showInputDialog);
		} catch (Exception e) {
			log.error(e);
		}
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
	static Vector<GeneChromosomeMatcher> getGeneChromosomeMatchers(
			String geneName, String database) throws SQLException {
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
			JOptionPane.showMessageDialog(null,
					ClassNotFoundException.class.getSimpleName()
							+ " for jdbc driver.",
					"Have you installed jdbc driver?",
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
	static CSSequence getSequences(GeneChromosomeMatcher geneChromosomeMatcher,
			int upstreamRegion, int downstreamRegion) {
		if (geneChromosomeMatcher == null) {
			return null;
		}

		int upStartPoint = geneChromosomeMatcher.getStartPoint();
		int downStartPoint = geneChromosomeMatcher.getEndPoint();
		int startPoint = upStartPoint - upstreamRegion;
		int endPoint = upStartPoint + downstreamRegion - 1;
		if (!geneChromosomeMatcher.isPositiveStrandDirection()) {
			startPoint = downStartPoint - downstreamRegion + 1;
			endPoint = downStartPoint + upstreamRegion;
		}

		int maxSize = 1000000;
		String request = "http://genome.cse.ucsc.edu/cgi-bin/das/"
				+ geneChromosomeMatcher.getGenomeBuildNumber()
				+ "/dna?segment=" + geneChromosomeMatcher.getChr() + ":"
				+ startPoint + ":" + endPoint;
		;

		try {
			InputStream uin = new URL(request).openStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(uin));
			String line;
			StringBuffer sequenceContent = new StringBuffer();
			while ((line = in.readLine()) != null) {
				int size = 0;
				if (line.trim().startsWith("<DNA")) {
					String[] str = line.split(">");
					if (str.length > 1) {
						sequenceContent.append(str[1]);
						size = str[1].length();
					}

					String label = "request";

					while ((line = in.readLine()) != null
							&& !line.trim().endsWith("DNA>")) {
						size += line.length();
						if (size >= maxSize) {

						}
						sequenceContent.append(line);
					}
					String content = sequenceContent.toString();
					if (!geneChromosomeMatcher.isPositiveStrandDirection()) {
						content = CSSequence.reverseString(content);
					}
					CSSequence seq = new CSSequence(label, content);
					return seq;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	static String getGenomeAssembly() {
		return genomeAssembly;
	}

}