package org.geworkbench.components.caarray.arraydata;

import edu.georgetown.pir.Organism;
import gov.nih.nci.caarray.domain.array.AbstractDesignElement;
import gov.nih.nci.caarray.domain.array.AbstractProbe;
import gov.nih.nci.caarray.domain.contact.Organization;
import gov.nih.nci.caarray.domain.contact.Person;
import gov.nih.nci.caarray.domain.data.AbstractDataColumn;
import gov.nih.nci.caarray.domain.data.DataRetrievalRequest;
import gov.nih.nci.caarray.domain.data.DataSet;
import gov.nih.nci.caarray.domain.data.DerivedArrayData;
import gov.nih.nci.caarray.domain.data.DesignElementList;
import gov.nih.nci.caarray.domain.data.DoubleColumn;
import gov.nih.nci.caarray.domain.data.FloatColumn;
import gov.nih.nci.caarray.domain.data.HybridizationData;
import gov.nih.nci.caarray.domain.data.IntegerColumn;
import gov.nih.nci.caarray.domain.data.LongColumn;
import gov.nih.nci.caarray.domain.data.QuantitationType;
import gov.nih.nci.caarray.domain.hybridization.Hybridization;
import gov.nih.nci.caarray.domain.project.Experiment;
import gov.nih.nci.caarray.domain.project.ExperimentContact;
import gov.nih.nci.caarray.domain.sample.AbstractBioMaterial;
import gov.nih.nci.caarray.services.CaArrayServer;
import gov.nih.nci.caarray.services.ServerConnectionException;
import gov.nih.nci.caarray.services.search.CaArraySearchService;
import gov.nih.nci.cagrid.cqlquery.CQLQuery;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.remoteresources.carraydata.CaArray2Experiment;

import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.CaArrayEvent;
import org.geworkbench.events.CaArrayQueryResultEvent;
import org.geworkbench.events.CaArrayRequestEvent;

public class StandAloneCaArrayClientWrapper {

	private CaArrayQueryClient cmdDataSetDownloadClient = new CaArrayQueryClient();

	private Log log = LogFactory.getLog(CaArray2Component.class);

	private static TreeMap<String, String> experimentDesciptions = new TreeMap<String, String>(); // For

	public final static String EXPERIMENTINFO = "experimentinfo";

	public final static String FILTERINFO = "filterinfo";

	public final static String HYB = "HYB";

	public final static String TYPEVALUE = "TYPEVALUE";

	public static String prefixCMD = null;

	public static String PATHSEP;

	public static String FILESEP;

	public static String CLASSPATH;

	private static String systempDir = System
			.getProperty("temporary.files.directory");

	public final static String tmpDir;

	/**
	 * Get the valid experiment names and their associated properites.
	 * 
	 * @param request
	 * @param url
	 * @param port
	 * @param usesname
	 * @param password
	 * @return
	 * @throws Exception
	 */

	static {
		// Set up the env.

		if (systempDir == null) {
			systempDir = "temp" + File.separator + "GEAW" + File.separator;
		}
		tmpDir = systempDir;
		File tempdir = new File(tmpDir);
		if (!tempdir.exists()) {
			tempdir.mkdir();
		}
		try {
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
			String jkdLocation = System.getProperty("java.home");
			PATHSEP = System.getProperty("path.separator");
			FILESEP = System.getProperty("file.separator");
			if (jkdLocation != null) {
				CLASSPATH = "C:\\java\\apps\\eclipse_workspace\\caarray\\classes;";
				String currentdir = System.getProperty("user.dir");
				CLASSPATH = CLASSPATH + currentdir + FILESEP + "classes"
						+ PATHSEP;
				// Do libs
				String dir = currentdir + FILESEP + "components" + FILESEP
						+ "caarray" + FILESEP;
				CLASSPATH = CLASSPATH + dir + "classes" + PATHSEP;
				String shortPath = "components" + FILESEP + "caarray" + FILESEP
						+ "lib" + FILESEP;
				File libdir = new File(dir + "lib");
				if (libdir.exists()) {
					File[] libFiles = libdir.listFiles();
					for (int i = 0; i < libFiles.length; i++) {
						File file = libFiles[i];
						if (!file.isDirectory()) {
							String name = file.getName().toLowerCase();
							if (name.endsWith(".jar") || name.endsWith(".zip")
									|| name.endsWith(".xsd")
									|| name.endsWith(".xml")
									|| name.endsWith(".dtd")
									|| name.endsWith(".properties")
									|| name.endsWith(".dll")) {

								// CLASSPATH = CLASSPATH +
								// file.getAbsolutePath()
								// + PATHSEP;
								CLASSPATH = CLASSPATH + shortPath
										+ file.getName() + PATHSEP;

							}
						}
					}

				}
				CLASSPATH += ".";
				prefixCMD = jkdLocation
						+ '/'
						+ "bin"
						+ '/'
						+ "java -Xmx400M -classpath "
						+ CLASSPATH
						+ " org.geworkbench.components.caarray.arraydata.StandAloneCaArrayClientExec ";

			}
		} catch (Exception e) {

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
		startJobThread(cmdline, savedFilename);
		TreeMap<String, Set<String>> tree = new TreeMap<String, Set<String>>();
		if (isFailed(savedFilename)) {
			return null;
		}
		try {
			// use buffering

			InputStream file = new FileInputStream(savedFilename);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			try {
				// deserialize the List
				tree = (TreeMap<String, Set<String>>) input.readObject();
				// display its data

			} finally {
				input.close();
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
	public boolean isFailed(String resultFilename) throws Exception {
		 
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
	public void cleanup(String resultFilename) {
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
		startJobThread(cmdline, savedFilename);
		if (isFailed(savedFilename)) {
			return null;
		}
		CaArray2Experiment[] caArrayExperiments = null;
		try {
			// use buffering

			InputStream file = new FileInputStream(savedFilename);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			try {
				// deserialize the List
				caArrayExperiments = (CaArray2Experiment[]) input.readObject();

			} finally {
				input.close();
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return caArrayExperiments;
	}

	public boolean isJobFinished(String filename) {
		try {
			if (new File(filename).exists()) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public CaArray2Experiment[] lookupExperiments(String url, int port,
			String username, String password, HashMap<String, String[]> filters)
			throws Exception {

		String[] arrays = new String[filters.size()];
		arrays = filters.keySet().toArray(arrays);
		for (String key : arrays) {
			String[] values = filters.get(key);
			if (values != null && values.length > 0) {
				return lookupExperimentsWithFilter(url, port, username,
						password, key, values[0]);
			}
		}
		return null;

	}

	public CaArray2Experiment[] lookupExperimentsWithFilter(String url,
			int port, String username, String password, String type,
			String value) throws Exception {

		String savedFilename = tmpDir + url + "_" + port + "_" + username + "_"
				+ type + "_" + value + ".txt";

		String cmdline = prefixCMD + FILTERINFO + " " + savedFilename + " "
				+ url + " " + port + " " + type + " " + value;
		if (username != null)
			cmdline = cmdline + " " + username + " " + password;
		startJobThread(cmdline, savedFilename);
		CaArray2Experiment[] tree = null;
		if (isFailed(savedFilename)) {
			return null;
		}
		try {
			InputStream file = new FileInputStream(savedFilename);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			try {
				// deserialize the List
				tree = (CaArray2Experiment[]) input.readObject();
				// display its data

			} finally {
				input.close();
			}
			return tree;
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	// static DataSet getDataSet(CaArraySearchService service,
	// Hybridization hybridization) {
	// DataSet dataSet = null;
	//
	// // If raw data doesn't exist, try to find derived data
	// Set<DerivedArrayData> derivedArrayDataSet = hybridization
	// .getDerivedDataCollection();
	// for (DerivedArrayData derivedArrayData : derivedArrayDataSet) {
	// // Return the data set associated with the first derived data.
	// DerivedArrayData populatedArrayData = service.search(
	// derivedArrayData).get(0);
	// dataSet = populatedArrayData.getDataSet();
	// }
	//
	// if (dataSet == null) {
	// return null;
	// } else {
	// return service.search(dataSet).get(0);
	// }
	// }

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

	private void startJobThread(String cmdline, String savedFilename) {
		if (!isJobFinished(savedFilename + ".over")) {
			JobThread jobThread = new JobThread(cmdline, savedFilename);
			jobThread.start();
		}
		while (!isJobFinished(savedFilename + ".over")) {
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * THe method to grab the data from caArray server with defined
	 * Hybridization and QuantitationType. A BISON DataType will be returned.
	 * 
	 * @param service
	 * @param hybridizationStr
	 * @param quantitationType
	 * @return
	 */
	public CSExprMicroarraySet getDataSet(String url, int port,
			String username, String password, String hybridizationStr,
			String quantitationType) throws Exception {
		String savedFilename = tmpDir + url + "_" + port + "_" + HYB + "_"
				+ quantitationType + ".txt";
		if (username != null) {
			savedFilename = tmpDir + url + "_" + port + "_" + username + "_"
					+ HYB + "_" + quantitationType + ".txt";
		}
		String cmdline = prefixCMD + HYB + " " + savedFilename + " " + url
				+ " " + port + " " + hybridizationStr + " " + quantitationType;
		if (username != null)
			cmdline = cmdline + " " + username + " " + password;
		startJobThread(cmdline, savedFilename);
		if (isFailed(savedFilename)) {
			return null;
		}
		return processDataToBISON(savedFilename, hybridizationStr);
	}

	/**
	 * Translate the data file into BISON type.
	 * 
	 * @param markersArray
	 * @param values
	 * @param name
	 * @return
	 */

	public CSExprMicroarraySet processDataToBISON(String filename, String name) {

		BufferedReader inputStream = null;
		PrintWriter outputStream = null;
		List<String> markerNames = new ArrayList<String>();
		List<Double> valuesList = new ArrayList<Double>();

		try {
			inputStream = new BufferedReader(new FileReader(filename));

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
			// maSet.setCompatibilityLabel(bioAssayImpl.getIdentifier());
			for (int z = 0; z < markerNo; z++) {

				String markerName = markerNames.get(z);
				if (markerName != null) {
					maSet.getMarkers().get(z).setGeneName(markerName);
					maSet.getMarkers().get(z).setDisPlayType(
							DSGeneMarker.AFFY_TYPE);
					maSet.getMarkers().get(z).setLabel(markerName);
					maSet.getMarkers().get(z).setDescription(markerName);
					// Why annonation information are always null? xz.
					// maSet.getMarkers().get(z).setDescription(
					// markersArray[z].getAnnotation().getLsid());
				} else {
					log
							.error("LogicalProbes have some null values. The location is "
									+ z);
				}
			}
		}
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
		System.out.println("For " + name
				+ ", the total second to convert it to BISON Data is "
				+ ((endTime - startTime) / 1000) + ".");
		maSet.setLabel("CaArray Data");
		return maSet;
	}

	private class JobThread extends Thread {
		private String cmdline;
		private String inputFilename;

		public JobThread() {
		}

		public JobThread(String cmd, String type) {

			cmdline = cmd;
			inputFilename = type;

		}

		public void run() {
			try {
//				System.out.println(new Date()
//						+ " at the thread. start the job: " + cmdline);

				// cmdline = "perl /razor/0/common/pudge/scr/psub.pl
				// 2resub.cfg";
				Process p = Runtime.getRuntime().exec(cmdline);

				FileOutputStream fos = new FileOutputStream(
						"caArrayExecLog.txt");
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(cmdline);

				// any error message?
				StreamGobbler errorGobbler = new StreamGobbler(proc
						.getErrorStream(), "ERROR", fos, inputFilename);

				// any output?
				StreamGobbler outputGobbler = new StreamGobbler(proc
						.getInputStream(), "OUTPUT", fos, inputFilename);

				// kick them off
				errorGobbler.start();
				outputGobbler.start();

				// any error???
				int exitVal = proc.waitFor();
				//System.out.println("ExitValue: " + exitVal);
				fos.flush();
				fos.close();
				if (proc.waitFor() != 0) {
					System.err.println("exit value = " + proc.exitValue());
				}
				File f = new File(inputFilename + ".over");
				f.createNewFile();
				
			}

			catch (Exception e) {
				System.err.println(e);
			}
		}
	}

	class StreamGobbler extends Thread {
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
					//System.out.println(type + ">" + line);
					//
					// if (line.trim().indexOf("P") > -1) {
					// currentJobFolderName = line.substring(
					// line.indexOf("P"), line.indexOf("P") + 6);
					// File fr = new File(inputname + "."
					// + currentJobFolderName);
					// fr.createNewFile();
					// System.out.println("create filename>" + fr.getName());
					// }
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
