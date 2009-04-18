package org.geworkbench.components.caarray.arraydata;

import gov.nih.nci.caarray.services.ServerConnectionException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.builtin.projects.remoteresources.carraydata.CaArray2Experiment;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.CaArrayEvent;
import org.geworkbench.events.CaArrayQueryResultEvent;

/**
 * The class to invoke StandAloneCaArrayWrapper
 * 
 * @author xiaoqing
 * @version $Id: StandAloneCaArrayClientWrapper.java,v 1.10 2009-04-18 12:20:28 jiz Exp $
 * 
 */
public class StandAloneCaArrayClientWrapper {
	private Log log = LogFactory.getLog(StandAloneCaArrayClientWrapper.class);

	// four types of queries
	public final static String EXPERIMENTINFO = "experimentinfo";
	public final static String FILTERINFO = "filterinfo";
	public final static String HYB = "HYB";
	public final static String TYPEVALUE = "TYPEVALUE";

	private static String prefixCMD = null;
	private final static String tmpDir;

	/**
	 * 
	 */
	static {
		String systempDir = System.getProperty("temporary.files.directory");

		if (systempDir == null) {
			systempDir = "temp" + File.separator + "GEAW" + File.separator;
		}
		tmpDir = systempDir;
		String jkdLocation = System.getProperty("java.home");
		File tempdir = new File(tmpDir);
		if (!tempdir.exists()) {
			tempdir.mkdir();
		}

		// remove previous session information.
		File tmpDirF = new File(tmpDir);
		String[] list = tmpDirF.list();
		File tempfile;
		for (int i = 0; i < list.length; i++) {
			if (list[i].endsWith(".over") || list[i].endsWith("Exception")) {
				tempfile = new File(tmpDirF, list[i]);
				tempfile.delete();
			}
		}
		// Set up classpath.

		String PATHSEP = System.getProperty("path.separator");
		String FILESEP = System.getProperty("file.separator");
		String CLASSPATH = "";
		if (jkdLocation != null) {
			String currentdir = System.getProperty("user.dir");
			CLASSPATH = currentdir + PATHSEP + currentdir + FILESEP + "classes" + PATHSEP;
			// Do libs
			String dir = currentdir + FILESEP + "components" + FILESEP
					+ "caarray" + FILESEP;
			CLASSPATH = CLASSPATH + dir + "classes" + PATHSEP;
			String shortPath = "components" + FILESEP + "caarray" + FILESEP
					+ "lib" + FILESEP;
			File libdir = new File(dir + "lib");
			if (libdir.exists()) {
				for (File file: libdir.listFiles()) {
					if (!file.isDirectory()) {
						String name = file.getName().toLowerCase();
						if (name.endsWith(".jar") || name.endsWith(".zip")
								|| name.endsWith(".xsd")
								|| name.endsWith(".xml")
								|| name.endsWith(".dtd")
								|| name.endsWith(".properties")
								|| name.endsWith(".dll")) {

							CLASSPATH = CLASSPATH + shortPath + file.getName()
									+ PATHSEP;
						}
					}
				}

			}
			CLASSPATH += ".";
			prefixCMD = jkdLocation + "/bin/java -Xmx400M -classpath \""
					+ CLASSPATH
					+ "\" org.geworkbench.components.caarray.arraydata.StandAloneCaArrayClientExec ";
		}
	}

	/**
	 * The method to query caArray server to return valid values for a type. For
	 * example, return all valid Organisms in caArray.
	 * 
	 * @param service
	 * @param request
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TreeMap<String, Set<String>> lookupTypeValues(String url, int port,
			String username, String password, String[] types) throws Exception {

		String savedFilename = tmpDir + url + "_" + port + "_" + TYPEVALUE
				+ ".txt";
		if (username != null) {
			savedFilename = tmpDir + url + "_" + port + "_" + username + "_"
					+ TYPEVALUE + ".txt";
		}
		String cmdline = prefixCMD + TYPEVALUE + " " + savedFilename + " "
				+ url + " " + port;
		if (username != null)
			cmdline = cmdline + " " + username + " " + password;
		invokeStandAloneApp(cmdline, savedFilename);
		TreeMap<String, Set<String>> tree = new TreeMap<String, Set<String>>();
		if (isFailed(savedFilename)) {
			return null;
		}
		try {
			// use buffering

			File file = new File(savedFilename);
			InputStream buffer = new BufferedInputStream(new FileInputStream(
					file));
			ObjectInput input = new ObjectInputStream(buffer);
			try {
				// deserialize the List
				tree = (TreeMap<String, Set<String>>) input.readObject();
				// display its data

			} finally {
				input.close();
				file.delete();
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return tree;
	}

	/**
	 * Check whether there is any exception file existing.
	 * 
	 * @param resultFilename
	 * @return
	 * @throws Exception
	 */
	private boolean isFailed(String resultFilename) throws Exception {

		File exceptionFile = new File(resultFilename + "."
				+ StandAloneCaArrayClientExec.ServerConnectionException);
		if (exceptionFile.exists()) {
			cleanup(resultFilename);
			throw new ServerConnectionException(
					"Cannot connect with the server.", new Exception());
		}
		exceptionFile = new File(resultFilename + "."
				+ StandAloneCaArrayClientExec.FailedLoginException);
		if (exceptionFile.exists()) {
			cleanup(resultFilename);
			throw new FailedLoginException("Cannot connect with the server.");
		}
		exceptionFile = new File(resultFilename + "."
				+ StandAloneCaArrayClientExec.Exception);
		if (exceptionFile.exists()) {
			cleanup(resultFilename);
			throw new Exception("Cannot connect with the server.");
		}
		return false;
	}

	/**
	 * remove related files if there is an exception.
	 * 
	 * @param resultFilename
	 */
	private void cleanup(String resultFilename) {
		File tmpDirF = new File(tmpDir);
		String[] list = tmpDirF.list();
		File tempfile;
		for (int i = 0; i < list.length; i++) {
			if (list[i].startsWith(resultFilename)) {
				tempfile = new File(tmpDirF, list[i]);
				tempfile.delete();
			}
		}
	}

	public CaArray2Experiment[] lookupExperiments(String url, int port,
			String username, String password) throws Exception {
		String savedFilename = tmpDir + url + "_" + port + "_" + EXPERIMENTINFO
				+ ".txt";
		if (username != null) {
			savedFilename = tmpDir + url + "_" + port + "_" + username + "_"
					+ EXPERIMENTINFO + ".txt";
		}
		String cmdline = prefixCMD + EXPERIMENTINFO + " " + savedFilename + " "
				+ url + " " + port;
		if (username != null)
			cmdline = cmdline + " " + username + " " + password;
		invokeStandAloneApp(cmdline, savedFilename);
		if (isFailed(savedFilename)) {
			return null;
		}
		CaArray2Experiment[] caArrayExperiments = null;
		try {
			// use buffering

			File file = new File(savedFilename);
			InputStream buffer = new BufferedInputStream(new FileInputStream(
					file));
			ObjectInput input = new ObjectInputStream(buffer);
			try {
				// deserialize the List
				caArrayExperiments = (CaArray2Experiment[]) input.readObject();

			} finally {
				input.close();
				file.delete();
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return caArrayExperiments;
	}

	// TODO only one filter is used, maybe we should change the map to two strings
	public CaArray2Experiment[] lookupExperiments(String url, int port,
			String username, String password, Map<String, String> filters)
			throws Exception {

		for (String key : filters.keySet()) {
			String value = filters.get(key);
			if (value != null) {
				return lookupExperimentsWithFilter(url, port, username,
						password, key, value);
			}
		}
		return null;

	}

	public CaArray2Experiment[] lookupExperimentsWithFilter(String url,
			int port, String username, String password, String type,
			String value) throws Exception {

		String savedFilename = tmpDir + url + "_" + port + "_" + type + "_"
				+ value + ".txt";

		if (username != null) {
			savedFilename = tmpDir + url + "_" + port + "_" + username + "_"
					+ type + "_" + value + ".txt";
		}
		String cmdline = prefixCMD + FILTERINFO + " " + savedFilename + " "
				+ url + " " + port + " " + type + " " + value;
		if (username != null)
			cmdline = cmdline + " " + username + " " + password;
		invokeStandAloneApp(cmdline, savedFilename);
		CaArray2Experiment[] tree = null;
		if (isFailed(savedFilename)) {
			return null;
		}
		try {
			File file = new File(savedFilename);
			InputStream buffer = new BufferedInputStream(new FileInputStream(
					file));
			ObjectInput input = new ObjectInputStream(buffer);
			try {
				// deserialize the List
				tree = (CaArray2Experiment[]) input.readObject();
				// display its data

			} finally {
				input.close();
				file.delete();
			}
			return tree;
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public CaArrayEvent publishCaArrayEvent(CaArrayEvent event) {
		return event;
	}

	@Publish
	public CaArrayQueryResultEvent publishCaArrayQueryResultEvent(
			CaArrayQueryResultEvent event) {
		return event;
	}

	/**
	 * The method to grab the data from caArray server with defined
	 * Hybridization and QuantitationType. A BISON DataType will be returned.
	 * 
	 * @param service
	 * @param hybridizationStr
	 * @param quantitationType
	 * @return
	 */
	public CSExprMicroarraySet getDataSet(String url, int port,
			String username, String password, String hybridizationName, Long hybridizationId,
			String quantitationType, String chipType) throws Exception {
		String savedFilename = tmpDir + url + "_" + port + "_" + HYB + "_"
				+ quantitationType + ".txt";
		if (username != null) {
			savedFilename = tmpDir + url + "_" + port + "_" + username + "_"
					+ HYB + "_" + quantitationType + ".txt";
		}
		String cmdline = prefixCMD + HYB + " " + savedFilename + " " + url
				+ " " + port + " " + hybridizationId + " " + quantitationType;
		if (username != null)
			cmdline = cmdline + " " + username + " " + password;
		invokeStandAloneApp(cmdline, savedFilename);
		if (isFailed(savedFilename)) {
			return null;
		}
		return processDataToBISON(savedFilename, hybridizationName, chipType);
	}
	
	/**
	 * Translate the data file into BISON type.
	 * 
	 * @param markersArray
	 * @param values
	 * @param name
	 * @return
	 */

	private CSExprMicroarraySet processDataToBISON(String filename, String name, String chipType) {

		BufferedReader inputStream = null;

		List<String> markerNames = new ArrayList<String>();
		List<Double> valuesList = new ArrayList<Double>();

		File file = null;
		try {
			file = new File(filename);
			inputStream = new BufferedReader(new FileReader(file));

			String l;
			while ((l = inputStream.readLine()) != null) {
				String[] items = l.split("\t");

				if (items != null && items.length > 1) {
					markerNames.add(items[0]);
					valuesList.add(new Double(items[1].trim()));

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				file.delete();

			} catch (Exception e) {

			}
		}

		Date date = new Date();
		long startTime = date.getTime();

		int markerNo = markerNames.size();
		DSMicroarray microarray = null;
		CSExprMicroarraySet maSet = new CSExprMicroarraySet();
		if (!maSet.initialized) {
			maSet.initialize(0, markerNo);
			maSet.getMarkerVector().clear();
			// maSet.setCompatibilityLabel(bioAssayImpl.getIdentifier());
			for (int z = 0; z < markerNo; z++) {

				String markerName = markerNames.get(z);
				if (markerName != null) {
					CSExpressionMarker marker = new CSExpressionMarker(z);
					marker.setGeneName(markerName);
					marker.setDisPlayType(
							DSGeneMarker.AFFY_TYPE);
					marker.setLabel(markerName);
					marker.setDescription(markerName);
					maSet.getMarkerVector().add(z, marker);
					// Why annotation information are always null? xz.
					// maSet.getMarkers().get(z).setDescription(
					// markersArray[z].getAnnotation().getLsid());
				} else {
					log
							.error("LogicalProbes have some null values. The location is "
									+ z);
				}
			}
		}

		maSet.setCompatibilityLabel(chipType);
		AnnotationParser.setChipType(maSet, chipType);
		
		microarray = new CSMicroarray(0, markerNo, name, null, null, true,
				DSMicroarraySet.geneExpType);
		microarray.setLabel(name);
		for (int i = 0; i < markerNo; i++) {
			((DSMutableMarkerValue) microarray.getMarkerValue(i))
					.setValue(valuesList.get(i));
		}
		if (maSet != null && microarray != null) {
			maSet.add(microarray);
		}
		long endTime = new Date().getTime();
		log.debug("For " + name
				+ ", the total second to convert it to BISON Data is "
				+ ((endTime - startTime) / 1000) + ".");
		maSet.setLabel("CaArray Data");
		return maSet;
	}

	private void invokeStandAloneApp(String cmdline, String inputFilename)
			throws IOException, InterruptedException {
		log.debug(new Date() + " at the thread. The job: " + cmdline);

		FileOutputStream fos = new FileOutputStream("caArrayExecLog.txt");
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(cmdline);

		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(),
				"ERROR", fos, inputFilename);

		// any output?
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(),
				"OUTPUT", fos, inputFilename);

		// kick them off
		errorGobbler.start();
		outputGobbler.start();

		// any error???
		int exitVal = proc.waitFor();
		log.debug("For cmdline " + cmdline + "\nExitValue: " + exitVal);
		fos.flush();
		fos.close();
		if (proc.waitFor() != 0) {
			log.warn("exit value = " + proc.exitValue());
		}
	}

	static private class StreamGobbler extends Thread {
		InputStream is;
		String type;
		OutputStream os;
		String inputname;
		String currentJobFolderName;

		public String getCurrentJobFolderName() {
			return currentJobFolderName;
		}

		public void setCurrentJobFolderName(String currentJobFolderName) {
			this.currentJobFolderName = currentJobFolderName;
		}

		StreamGobbler(InputStream is, String type) {
			this(is, type, null);
		}

		StreamGobbler(InputStream is, String type, OutputStream redirect) {
			this.is = is;
			this.type = type;
			this.os = redirect;
			inputname = "";
		}

		StreamGobbler(InputStream is, String type, OutputStream redirect,
				String inputFilename) {
			this.is = is;
			this.type = type;
			this.os = redirect;
			this.inputname = inputFilename;
		}

		public void run() {
			try {
				PrintWriter pw = null;
				if (os != null) {
					pw = new PrintWriter(os);
				}

				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					if (pw != null) {
						pw.println(line);
					}
				}
				if (pw != null) {
					pw.flush();
					pw.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

}
