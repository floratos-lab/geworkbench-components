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
import org.geworkbench.builtin.projects.remoteresources.query.CaARRAYQueryPanel;
import org.geworkbench.builtin.projects.util.CaARRAYPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.CaArrayEvent;
import org.geworkbench.events.CaArrayQueryResultEvent;
import org.geworkbench.events.CaArrayRequestEvent;

public class StandAloneCaArrayClientExec {
	// private Log log = LogFactory.getLog(StandAloneCaArrayClientExec.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StandAloneCaArrayClientExec exec = new StandAloneCaArrayClientExec();
		try {
			 
			if (args == null || args.length <= 1) {
				args = new String[] { StandAloneCaArrayClientWrapper.TYPEVALUE,
						"oddtype.txt" };
			}
			// create an expriment name with unzero hybridations.
			System.out.println("In Exec class, Excuting..." + args);
			if (args != null && args.length >= 4) {
				String url = args[2];
				int port = new Integer(args[3].trim());
				String username = null;
				String password = null;
				String resultFilename = args[1];
				
				if (args[0]
						.equalsIgnoreCase(StandAloneCaArrayClientWrapper.EXPERIMENTINFO)) {
					if(args.length>=6){
						username = args[4];
						password = args[5];
					}
					exec.lookupExperiments(url, port,
							username, password, resultFilename);
					System.out.println("Ending lookup experiment. "
							+ new Date());
				} else if (args[0]
						.equalsIgnoreCase(StandAloneCaArrayClientWrapper.HYB)) {
					String hybridname = args[4];
					String qType = args[5];
					if(args.length>=8){
					 	username = args[6];
						password = args[7];
					}
					exec.getDataSet(url, port, username,
							password, hybridname, qType, resultFilename);
					System.out.println("Ending loading dataset" + new Date());
				} else if (args[0]
						.equalsIgnoreCase(StandAloneCaArrayClientWrapper.TYPEVALUE)) {
					if(args.length>=6){
						username = args[4];
						password = args[5];
					}
					exec.lookupTypeValues(url, port, username,
							password, CaARRAYQueryPanel.listContent, resultFilename);
					System.out.println("Ending loading type values"
							+ new Date());
				} else if (args[0]
						.equalsIgnoreCase(StandAloneCaArrayClientWrapper.FILTERINFO)) {
					String filtername = args[4];
					String filterkey = args[5];
					if(args.length>=8){
					 	username = args[6];
						password = args[7];
					}
					exec.lookupExperimentsWithFilter(url, port, username,
							password, filtername, filterkey, args[1]);
					System.out.println("Ending loading tyype values"
							+ new Date());
				}
			}
		} catch (Exception e) {
			exec.handleException(e, args);
		}
	}

	private CaArrayQueryClient cmdDataSetDownloadClient = new CaArrayQueryClient();
	private Log log = LogFactory.getLog(CaArray2Component.class);
	private static TreeMap<String, String> experimentDesciptions = new TreeMap<String, String>(); // For
	public static final String ServerConnectionException = "ServerConnectionException";
	public static final String FailedLoginException = "FailedLoginException";
	public static final String  Exception = "Exception";
	private void handleException(Exception e, String[] args) {

	}

	/**
	 * Get the valid type values. Current implementation will skip the tissue
	 * type.
	 * 
	 * @param url
	 * @param port
	 * @param username
	 * @param password
	 * @param types
	 * @param typeValueFileName
	 * @return
	 */

	public TreeMap<String, Set<String>> lookupTypeValues(String url, int port,
			String username, String password, String[] types,
			String typeValueFileName) {
		try {
			 
			CaArrayServer server = new CaArrayServer(url, port);
			if (username == null || username.trim().length() == 0) {
				server.connect();// enable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService searchService = server.getSearchService();
			DataRetrievalRequest request = new DataRetrievalRequest();

			TreeMap<String, Set<String>> tree = new TreeMap<String, Set<String>>();
			CQLQuery query = new CQLQuery();
			query.setTarget(new gov.nih.nci.cagrid.cqlquery.Object());
			String target = "edu.georgetown.pir.Organism";
			StopWatch stopwatch = new StopWatch();
			for (String type : types) {
				query = CQLQueryGenerator.generateQuery(type, type);
				List list = searchService.search(query);
				TreeSet<String> values = new TreeSet<String>();
				for (Object o : list) {
					stopwatch.reset();
					stopwatch.start();
					if (o instanceof Organism) {
						Organism e = ((Organism) o);
						if (e.getCommonName() != null) {
							values.add(e.getCommonName());
						}
					}
					if (o instanceof Person) {
						Person p = (Person) o;
						values.add(p.getFirstName() + " " + p.getLastName());
					}
					if (o instanceof AbstractBioMaterial) {
						AbstractBioMaterial p = (AbstractBioMaterial) o;
						if (p != null && p.getTissueSite() != null
								&& p.getTissueSite().getValue() != null) {
							String tissueSite = p.getTissueSite().getValue();

							values.add(tissueSite);
						} else {
							// log.debug("NULL TissueType");
						}

					}
					if (o instanceof Organization) {
						Organization p = (Organization) o;
						if (p.isProvider()) {

							values.add(p.getName());
						}
					}
					stopwatch.stop();
					// System.out.println("Need: " + stopwatch.getTime() + " for
					// " +
					// o.getClass() );
				}
				tree.put(type, values);
			}
			if (tree == null || tree.size() == 0) {
				System.out.println("No tree is created."
						+ new File(typeValueFileName).getAbsolutePath());
				return null;
			}
			ObjectOutputStream outputStream = null;

			try {
				 
				outputStream = new ObjectOutputStream(new FileOutputStream(
						typeValueFileName));
				outputStream.writeObject(tree);

			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				// Close the ObjectOutputStream
				try {
					if (outputStream != null) {
						outputStream.flush();
						outputStream.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			return tree;
		} catch (Exception e) {
			e.printStackTrace();
			processException(e, typeValueFileName);
		}

		return null;
	}


	public void processException(Exception e, String filename){
		if (e instanceof gov.nih.nci.caarray.services.ServerConnectionException){
			createFile(filename, ServerConnectionException);
		}else if (e instanceof FailedLoginException){
			createFile(filename, FailedLoginException);
		}else{
			createFile(filename, Exception);
		}
	}
	
	public void createFile(String filename, String extension){
		try{
			File file = new File(filename + "." + extension);
			file.createNewFile();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
//	public CaArray2Experiment[] lookupExperiments(String url, int port,
//			String username, String password, HashMap<String, String[]> filters)
//			throws Exception {
//		CaArrayServer server = new CaArrayServer(url, port);
//		server.connect();
//		String[] arrays = new String[filters.size()];
//		arrays = filters.keySet().toArray(arrays);
//		for (String key : arrays) {
//			String[] values = filters.get(key);
//			if (values != null && values.length > 0) {
//				return lookupExperimentsWithFilter(url, port, key, values[0]);
//			}
//		}
//		return null;
//
//	}

	public CaArray2Experiment[] lookupExperimentsWithFilter(String url,
			int port, String username, String password, String type,
			String value, String savedFilename) {

		log.debug("Get  into lookupExpermentWithFilter");

		try {

			System.out.println("Beginning to load ");
			CaArrayServer server = new CaArrayServer(url, port);
			server.connect();
			if (username == null || username.trim().length() == 0) {
				server.connect();// enable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService service = server.getSearchService();
			// CaArraySearchService service = server.getSearchService();
			TreeMap<String, String[]> tree = new TreeMap<String, String[]>();
			gov.nih.nci.cagrid.cqlquery.Object object = new gov.nih.nci.cagrid.cqlquery.Object();

			CQLQuery query = CQLQueryGenerator.generateQuery(
					CQLQueryGenerator.EXPERIMENT, type, value);
			List list = service.search(query);

			Vector<CaArray2Experiment> exps = new Vector<CaArray2Experiment>();
			for (Object o : list) {
				Experiment e = ((Experiment) o);
				log.debug("Experiment : " + e.getTitle() + "| "
						+ e.getManufacturer().getName());
				Set<Hybridization> h1 = (((Experiment) o).getHybridizations());

				if (h1.size() > 0) {
					String[] hybridizationValues = new String[h1.size()];
					int i = 0;
					Hybridization oneHybridization = null;
					TreeSet<String> set = new TreeSet<String>();
					for (Hybridization h : h1) {
						oneHybridization = h;
						set.add(h.getName());
					}
					hybridizationValues = set.toArray(hybridizationValues);

					Hybridization hybridization = (Hybridization) (service
							.search(oneHybridization).get(0));
					DataSet dataSet = getDataSet(service, hybridization);
					String[] qTypes = null;
					if (dataSet != null) {
						List<QuantitationType> qList = dataSet
								.getQuantitationTypes();
						qTypes = new String[qList.size()];
						int j = 0;
						for (QuantitationType qu : qList) {
							qTypes[j] = qu.getName();
							j++;
						}
					}
					CaArray2Experiment exp = new CaArray2Experiment(url, port);

					exp.setName(e.getTitle());
					exp.setDescription(e.getDescription());
					exp.setHybridizations(hybridizationValues);
					exp.setQuantitationTypes(qTypes);
					exps.add(exp);

				}
			}
			CaArray2Experiment[] experimentsArray = new CaArray2Experiment[exps
					.size()];
			ObjectOutputStream outputStream = null;

			try {
				System.out.println("File saved at"
						+ new File(savedFilename).getAbsolutePath());
				// Construct the LineNumberReader object
				outputStream = new ObjectOutputStream(new FileOutputStream(
						savedFilename));
				outputStream.writeObject(exps.toArray(experimentsArray));

			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				// Close the ObjectOutputStream
				try {
					if (outputStream != null) {
						outputStream.flush();
						outputStream.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			processException(e, savedFilename);
		}
		return null;
	}

	public void lookupExperiments(String url, int port, String username,
			String password, String experimentFileName) {
		 
		CaArrayServer server = new CaArrayServer(url, port);
		try{
		if (username == null || username.trim().length() == 0) {
			server.connect();// disable a user login.
		} else {
			server.connect(username, password);
		}
		CaArraySearchService service = server.getSearchService();
		experimentDesciptions = new TreeMap<String, String>();
		Vector<CaArray2Experiment> exps = new Vector<CaArray2Experiment>();

		CQLQuery query = CQLQueryGenerator
				.generateQuery(CQLQueryGenerator.EXPERIMENT);
		List list = service.search(query);
		for (Object o : list) {
			Experiment e = ((Experiment) o);
			System.out.println("get Exp: " + new Date());
			Set<Hybridization> h1 = (((Experiment) o).getHybridizations());
			if (h1.size() > 0) {
				String[] hybridizationValues = new String[h1.size()];
				TreeSet<String> set = new TreeSet<String>();
				Hybridization oneHybridization = null;
				for (Hybridization h : h1) {
					oneHybridization = h;
					set.add(h.getName());
				}
				hybridizationValues = set.toArray(hybridizationValues);
				// below is to get the QuantitationType for each experiment.
				Hybridization hybridization = (Hybridization) (service
						.search(oneHybridization).get(0));
				DataSet dataSet = getDataSet(service, hybridization);
				String[] qTypes = null;
				Set<String> sets = new TreeSet<String>();
				if (dataSet != null) {
					List<QuantitationType> qList = dataSet
							.getQuantitationTypes();
					for (QuantitationType qType : qList) {

						Class typeClass = qType.getTypeClass();
						// Retrieve the appropriate data depending on the type
						// of the column.
						if (typeClass != String.class
								&& typeClass != Boolean.class) {
							sets.add(qType.getName());
						}
					}
					qTypes = new String[sets.size()];
					qTypes = sets.toArray(qTypes);
				}
				CaArray2Experiment exp = new CaArray2Experiment(url, port);

				exp.setName(e.getTitle());
				exp.setDescription(e.getDescription());
				exp.setHybridizations(hybridizationValues);
				exp.setQuantitationTypes(qTypes);
				exps.add(exp);
				 
			}
		}

		CaArray2Experiment[] experimentsArray = new CaArray2Experiment[exps
				.size()];
		// experimentsArray = exps.toArray(experimentsArray);
		exps.toArray(experimentsArray);

		ObjectOutputStream outputStream = null;

		try {
			System.out.println("File saved at"
					+ new File(experimentFileName).getAbsolutePath());
			// Construct the LineNumberReader object
			outputStream = new ObjectOutputStream(new FileOutputStream(
					experimentFileName));
			outputStream.writeObject(exps.toArray(experimentsArray));

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			// Close the ObjectOutputStream
			try {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		}catch (Exception e){
			e.printStackTrace();
			processException(e, experimentFileName);
		}
		System.out.println("Ending the lookupExperiments " + new Date());

	}

//	public static CaArray2Experiment[] lookupExperimentsWithFilter(String url,
//			int port, String type, String value) {
//
//		try {
//			CaArrayServer server = new CaArrayServer(url, port);
//			CaArraySearchService service = server.getSearchService();
//			TreeMap<String, String[]> tree = new TreeMap<String, String[]>();
//			gov.nih.nci.cagrid.cqlquery.Object object = new gov.nih.nci.cagrid.cqlquery.Object();
//
//			CQLQuery query = CQLQueryGenerator.generateQuery(
//					CQLQueryGenerator.EXPERIMENT, type, value);
//			List list = service.search(query);
//
//			Vector<CaArray2Experiment> exps = new Vector<CaArray2Experiment>();
//			for (Object o : list) {
//				Experiment e = ((Experiment) o);
//
//				Set<Hybridization> h1 = (((Experiment) o).getHybridizations());
//
//				if (h1.size() > 0) {
//					String[] hybridizationValues = new String[h1.size()];
//					int i = 0;
//					Hybridization oneHybridization = null;
//					TreeSet<String> set = new TreeSet<String>();
//					for (Hybridization h : h1) {
//						oneHybridization = h;
//						set.add(h.getName());
//					}
//					hybridizationValues = set.toArray(hybridizationValues);
//
//					Hybridization hybridization = (Hybridization) (service
//							.search(oneHybridization).get(0));
//					DataSet dataSet = getDataSet(service, hybridization);
//					String[] qTypes = null;
//					if (dataSet != null) {
//						List<QuantitationType> qList = dataSet
//								.getQuantitationTypes();
//						qTypes = new String[qList.size()];
//						int j = 0;
//						for (QuantitationType qu : qList) {
//							qTypes[j] = qu.getName();
//							j++;
//						}
//					}
//					CaArray2Experiment exp = new CaArray2Experiment(url, port);
//
//					exp.setName(e.getTitle());
//					exp.setDescription(e.getDescription());
//					exp.setHybridizations(hybridizationValues);
//					exp.setQuantitationTypes(qTypes);
//					exps.add(exp);
//
//				}
//			}
//			CaArray2Experiment[] experimentsArray = new CaArray2Experiment[exps
//					.size()];
//			return exps.toArray(experimentsArray);
//		} catch (Exception e) {
//			e.printStackTrace();
//			 
//		}
//		return null;
//	}

	static DataSet getDataSet(CaArraySearchService service,
			Hybridization hybridization) {
		DataSet dataSet = null;

		// If raw data doesn't exist, try to find derived data
		Set<DerivedArrayData> derivedArrayDataSet = hybridization
				.getDerivedDataCollection();
		for (DerivedArrayData derivedArrayData : derivedArrayDataSet) {
			// Return the data set associated with the first derived data.
			DerivedArrayData populatedArrayData = service.search(
					derivedArrayData).get(0);
			dataSet = populatedArrayData.getDataSet();
		}

		if (dataSet == null) {
			return null;
		} else {
			return service.search(dataSet).get(0);
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
			String quantitationType, String outputFilename) {
		System.out.println("Running the getDataSet" + new Date());
		CaArrayServer server = new CaArrayServer(url, port);
		try {
			if (username == null || username.trim().length() == 0) {
				server.connect();// disable a user login.
			} else {
				server.connect(username, password);
			}
		} catch (Exception e) {
			processException(e, outputFilename);
		}
		CaArraySearchService service = server.getSearchService();
		Date date = new Date();
		long startTime = date.getTime();
		AbstractProbe[] markersArray;
		Hybridization hybridization = new Hybridization();
		hybridization.setName(hybridizationStr);
		List<Hybridization> set = service.search(hybridization);
		if (set == null || set.size() == 0) {
			return null;
		}

		hybridization = service.search(hybridization).get(0);
		DataSet dataSet = null;

		// If raw data doesn't exist, try to find derived data
		Set<DerivedArrayData> derivedArrayDataSet = hybridization
				.getDerivedDataCollection();
		for (DerivedArrayData derivedArrayData : derivedArrayDataSet) {
			// Return the data set associated with the first derived data.
			DerivedArrayData populatedArrayData = service.search(
					derivedArrayData).get(0);
			dataSet = populatedArrayData.getDataSet();
			List<DataSet> dataSetList = service.search(dataSet);
			DataSet data = dataSetList.get(0);
			// Below is the code to get the names of each marker.

			DesignElementList designElementList = data.getDesignElementList();
			List<DesignElementList> designElementLists = service
					.search(designElementList);
			DesignElementList designElements = designElementLists.get(0);
			List<AbstractDesignElement> list = designElements
					.getDesignElements();
			markersArray = new AbstractProbe[list.size()];
			markersArray = list.toArray(markersArray);
			// Add populate probe and get annotation later.

			// Below is the code to get the values for the quantitationType.
			for (HybridizationData oneHybData : data.getHybridizationDataList()) {
				HybridizationData populatedHybData = service.search(oneHybData)
						.get(0);
				double[] doubleValues = new double[markersArray.length];
				// Get each column in the HybridizationData.
				for (AbstractDataColumn column : populatedHybData.getColumns()) {
					AbstractDataColumn populatedColumn = service.search(column)
							.get(0);
					// Find the type of the column.
					QuantitationType qType = populatedColumn
							.getQuantitationType();
					if (qType.getName().equalsIgnoreCase(quantitationType)) {
						Class typeClass = qType.getTypeClass();
						// Retrieve the appropriate data depending
						// on the type of the column.
						if (typeClass == Float.class) {
							float[] values = ((FloatColumn) populatedColumn)
									.getValues();
							long endTime = new Date().getTime();
							System.out.println("For " + hybridizationStr
									+ ", the total second to load is "
									+ ((endTime - startTime) / 1000) + ".");

							for (int i = 0; i < values.length; i++) {
								doubleValues[i] = values[i];
							}

						} else if (typeClass == Integer.class) {
							int[] values = ((IntegerColumn) populatedColumn)
									.getValues();
							long endTime = new Date().getTime();
							System.out.println("For " + hybridizationStr
									+ ", the total second to load is "
									+ ((endTime - startTime) / 1000) + ".");

							for (int i = 0; i < values.length; i++) {
								doubleValues[i] = values[i];
							}

						} else if (typeClass == Long.class) {
							long[] values = ((LongColumn) populatedColumn)
									.getValues();
							long endTime = new Date().getTime();
							System.out.println("For " + hybridizationStr
									+ ", the total second to load is "
									+ ((endTime - startTime) / 1000) + ".");

							for (int i = 0; i < values.length; i++) {
								doubleValues[i] = values[i];
							}

						} else if (typeClass == Double.class) {
							doubleValues = ((DoubleColumn) populatedColumn)
									.getValues();
							long endTime = new Date().getTime();
							System.out.println("For " + hybridizationStr
									+ ", the total second to load is "
									+ ((endTime - startTime) / 1000) + ".");

						}

						// write the marker names and values to a file.
						PrintWriter outputStream = null;

						try {

							outputStream = new PrintWriter(new FileWriter(
									outputFilename));

							for (int i = 0; i < doubleValues.length; i++) {
								outputStream.println(markersArray[i].getName()
										+ "\t" + doubleValues[i]);
							}

						} catch (IOException io) {
							processException(io, outputFilename);
						} finally {

							if (outputStream != null) {
								outputStream.close();
							}
						}
					}
				}
			}

		}

		return null;
	}

	public void receive(CaArrayRequestEvent ce, Object source) {
		if (ce == null) {
			return;
		}
		String url = ce.getUrl();
		int port = ce.getPort();
		String username = ce.getUsername();
		String password = ce.getPassword();

		// below is to invoke external Java process to call caArray server.

		String jreLocation = System.getProperty("java.home");

		try {

			CaArrayServer server = new CaArrayServer(url, port);
			if (username == null || username.trim().length() == 0) {
				server.connect();// disable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService searchService = server.getSearchService();

			if (ce.getRequestItem().equalsIgnoreCase(
					CaArrayRequestEvent.EXPERIMENT)) {

				// TreeMap<String, String[]> treeMap = null;
				CaArrayEvent event = new CaArrayEvent(url, port);
				// TreeMap<String, String> desTreeMap = null;
				CaArray2Experiment[] exps = null;
				DataRetrievalRequest request = new DataRetrievalRequest();
				if (ce.isUseFilterCrit()) {
					HashMap<String, String[]> filters = ce.getFilterCrit();
					if (filters != null) {
						exps = cmdDataSetDownloadClient.lookupExperiments(
								searchService, url, port, username, password,
								filters);
					}
				} else {
					exps = cmdDataSetDownloadClient.lookupExperiments(
							searchService, request, url, port, username,
							password);
				}
				if (exps != null && exps.length > 0) {
					event.setExperiments(exps);
					event.setPopulated(true);
				} else {
					event.setPopulated(false);
					event
							.setErrorMessage("No experiment can be retrieved from the server: "
									+ url + ":" + port);
				}

				publishCaArrayEvent(event);
			} else {
				// For BioAssay detail, another kind of request.
				if (ce.getRequestItem().equalsIgnoreCase(
						CaArrayRequestEvent.BIOASSAY)) {
					HashMap<String, String[]> filterCrit = ce.getFilterCrit();
					String experimentName = filterCrit
							.get(CaArrayRequestEvent.EXPERIMENT)[0];
					String[] hybridzations = filterCrit
							.get(CaArrayRequestEvent.BIOASSAY);
					boolean merge = ce.isMerge();
					String qType = ce.getQType();
					if (qType == null) {
						qType = "CHPSignal";
					}
					CSExprMicroarraySet maSet = getDataSet(searchService,
							hybridzations[0], qType);
					CSExprMicroarraySet totalSet = maSet;
					if (!merge) {
						if (maSet != null) {
							maSet.setLabel(experimentName + "_"
									+ hybridzations[0]);
							org.geworkbench.events.ProjectNodeAddedEvent pevent = new org.geworkbench.events.ProjectNodeAddedEvent(
									"message", maSet, null);
							ProjectPanel.addToHistory(maSet,
									"Get from CaArray Server " + url + ":"
											+ port + ".");
							// xz publishProjectNodeAddedEvent(pevent);
						}
					}

					CaArrayEvent event = new CaArrayEvent(url, port);
					if (totalSet != null) {
						event.setPopulated(true);
					} else {
						event.setPopulated(false);
						event
								.setErrorMessage("No data associated with the quantitation type\n \""
										+ qType
										+ "\"\ncan be retrieved from the server: \n"
										+ url + ":" + port + ".");
					}
					if (hybridzations.length > 1) {
						for (int i = 1; i < hybridzations.length; i++) {
							CSExprMicroarraySet maSet2 = getDataSet(
									searchService, hybridzations[i], qType);
							if (maSet2 == null) {
								event.setPopulated(false);
							} else {
								maSet2.setLabel(experimentName);
								event.setPopulated(true);
								if (!merge) {
									maSet.setLabel(experimentName + "_"
											+ hybridzations[i]);
									org.geworkbench.events.ProjectNodeAddedEvent pevent = new org.geworkbench.events.ProjectNodeAddedEvent(
											"message", maSet2, null);

									// Need change it. xz
									// publishProjectNodeAddedEvent(pevent);
								} else {
									if (maSet2 != null && maSet2.size() > 0
											&& totalSet != null)
										totalSet.add(maSet2.get(0));
								}
							}
						}

					}
					if (merge) {

						org.geworkbench.events.ProjectNodeAddedEvent pevent = new org.geworkbench.events.ProjectNodeAddedEvent(
								"message", totalSet, null);
						totalSet.setLabel(experimentName + "_"
								+ hybridzations.length + "_merged");

						// xz
						// publishProjectNodeAddedEvent(pevent);
					}
					event.setDataSet(totalSet);
					event.setInfoType(CaArrayEvent.BIOASSAY);
					publishCaArrayEvent(event);

				}
			}

		} catch (ServerConnectionException se) {
			CaArrayEvent event = new CaArrayEvent(url, port);
			event.setPopulated(false);
			event.setSucceed(false);
			se.printStackTrace();
			publishCaArrayEvent(event);
			event.setErrorMessage("Cannot connect to the server at " + url
					+ ":" + port);
		} catch (FailedLoginException fe) {
			CaArrayEvent event = new CaArrayEvent(url, port);
			event.setPopulated(false);
			event.setSucceed(false);

			event
					.setErrorMessage("Either username or password is incorrect. Please check your login credentials. ");
			publishCaArrayEvent(event);

		}

		catch (Exception e) {
			CaArrayEvent event = new CaArrayEvent(url, port);
			event.setPopulated(false);
			event.setSucceed(false);
			event.setErrorMessage(e.getMessage());
			publishCaArrayEvent(event);
			e.printStackTrace();

		}

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
	 * THe method to grab the data from caArray server with defined
	 * Hybridization and QuantitationType. A BISON DataType will be returned.
	 * 
	 * @param service
	 * @param hybridizationStr
	 * @param quantitationType
	 * @return
	 */
	public CSExprMicroarraySet getDataSet(CaArraySearchService service,
			String hybridizationStr, String quantitationType) {
		Date date = new Date();
		long startTime = date.getTime();
		AbstractProbe[] markersArray;
		Hybridization hybridization = new Hybridization();
		hybridization.setName(hybridizationStr);
		List<Hybridization> set = service.search(hybridization);
		if (set == null || set.size() == 0) {
			return null;
		}

		hybridization = service.search(hybridization).get(0);
		DataSet dataSet = null;

		// If raw data doesn't exist, try to find derived data
		Set<DerivedArrayData> derivedArrayDataSet = hybridization
				.getDerivedDataCollection();
		for (DerivedArrayData derivedArrayData : derivedArrayDataSet) {
			// Return the data set associated with the first derived data.
			DerivedArrayData populatedArrayData = service.search(
					derivedArrayData).get(0);
			dataSet = populatedArrayData.getDataSet();
			List<DataSet> dataSetList = service.search(dataSet);
			DataSet data = dataSetList.get(0);
			// Below is the code to get the names of each marker.

			DesignElementList designElementList = data.getDesignElementList();
			List<DesignElementList> designElementLists = service
					.search(designElementList);
			DesignElementList designElements = designElementLists.get(0);
			List<AbstractDesignElement> list = designElements
					.getDesignElements();
			markersArray = new AbstractProbe[list.size()];
			markersArray = list.toArray(markersArray);
			// Add populate probe and get annotation later.

			// Below is the code to get the values for the quantitationType.
			for (HybridizationData oneHybData : data.getHybridizationDataList()) {
				HybridizationData populatedHybData = service.search(oneHybData)
						.get(0);
				double[] doubleValues = new double[markersArray.length];
				// Get each column in the HybridizationData.
				for (AbstractDataColumn column : populatedHybData.getColumns()) {
					AbstractDataColumn populatedColumn = service.search(column)
							.get(0);
					// Find the type of the column.
					QuantitationType qType = populatedColumn
							.getQuantitationType();
					if (qType.getName().equalsIgnoreCase(quantitationType)) {
						Class typeClass = qType.getTypeClass();
						// Retrieve the appropriate data depending
						// on the type of the column.
						if (typeClass == Float.class) {
							float[] values = ((FloatColumn) populatedColumn)
									.getValues();
							long endTime = new Date().getTime();
							System.out.println("For " + hybridizationStr
									+ ", the total second to load is "
									+ ((endTime - startTime) / 1000) + ".");

							for (int i = 0; i < values.length; i++) {
								doubleValues[i] = values[i];
							}
							return processDataToBISON(markersArray,
									doubleValues, hybridizationStr);
						} else if (typeClass == Integer.class) {
							int[] values = ((IntegerColumn) populatedColumn)
									.getValues();
							long endTime = new Date().getTime();
							System.out.println("For " + hybridizationStr
									+ ", the total second to load is "
									+ ((endTime - startTime) / 1000) + ".");

							for (int i = 0; i < values.length; i++) {
								doubleValues[i] = values[i];
							}
							return processDataToBISON(markersArray,
									doubleValues, hybridizationStr);
						} else if (typeClass == Long.class) {
							long[] values = ((LongColumn) populatedColumn)
									.getValues();
							long endTime = new Date().getTime();
							System.out.println("For " + hybridizationStr
									+ ", the total second to load is "
									+ ((endTime - startTime) / 1000) + ".");

							for (int i = 0; i < values.length; i++) {
								doubleValues[i] = values[i];
							}
							return processDataToBISON(markersArray,
									doubleValues, hybridizationStr);
						} else if (typeClass == Double.class) {
							doubleValues = ((DoubleColumn) populatedColumn)
									.getValues();
							long endTime = new Date().getTime();
							System.out.println("For " + hybridizationStr
									+ ", the total second to load is "
									+ ((endTime - startTime) / 1000) + ".");

							return processDataToBISON(markersArray,
									doubleValues, hybridizationStr);
						}
					}
				}
			}

		}

		return null;
	}

	/**
	 * Translate the CaArray DataSet into BISON type.
	 * 
	 * @param markersArray
	 * @param values
	 * @param name
	 * @return
	 */

	public CSExprMicroarraySet processDataToBISON(AbstractProbe[] markersArray,
			double[] values, String name) {
		Date date = new Date();
		long startTime = date.getTime();

		int markerNo = markersArray.length;
		DSMicroarray microarray = null;
		CSExprMicroarraySet maSet = new CSExprMicroarraySet();
		if (!maSet.initialized) {
			maSet.initialize(0, markerNo);
			// maSet.setCompatibilityLabel(bioAssayImpl.getIdentifier());
			for (int z = 0; z < markerNo; z++) {

				if (markersArray[z] != null) {
					maSet.getMarkers().get(z).setGeneName(
							markersArray[z].getName());
					maSet.getMarkers().get(z).setDisPlayType(
							DSGeneMarker.AFFY_TYPE);
					maSet.getMarkers().get(z).setLabel(
							markersArray[z].getName());
					maSet.getMarkers().get(z).setDescription(
							markersArray[z].getName());
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
					.setValue(((Double) values[i]).doubleValue());
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

}
