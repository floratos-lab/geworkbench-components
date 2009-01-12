package org.geworkbench.components.caarray.arraydata;

import edu.georgetown.pir.Organism;
import gov.nih.nci.caarray.domain.array.AbstractDesignElement;
import gov.nih.nci.caarray.domain.array.AbstractProbe;
import gov.nih.nci.caarray.domain.contact.Organization;
import gov.nih.nci.caarray.domain.contact.Person;
import gov.nih.nci.caarray.domain.data.AbstractDataColumn;
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
import gov.nih.nci.caarray.domain.sample.AbstractBioMaterial;
import gov.nih.nci.caarray.services.CaArrayServer;
import gov.nih.nci.caarray.services.search.CaArraySearchService;
import gov.nih.nci.cagrid.cqlquery.CQLQuery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.builtin.projects.remoteresources.carraydata.CaArray2Experiment;
import org.geworkbench.builtin.projects.remoteresources.query.CaARRAYQueryPanel;

/**
 * This is invoked as a stand-alone application by StandAloneCaArrayClientWrapper.
 * 
 * @author xiaoqing
 * @version $Id: StandAloneCaArrayClientExec.java,v 1.6 2009-01-12 17:44:52 jiz Exp $
 *
 */
public class StandAloneCaArrayClientExec {
	// log is intentionally set to another class's log
	static private Log log = LogFactory.getLog(CaArray2Component.class);
	// private Log log = LogFactory.getLog(StandAloneCaArrayClientExec.class);

	// only used in this package, change to default
	static final String ServerConnectionException = "ServerConnectionException";
	static final String FailedLoginException = "FailedLoginException";
	static final String Exception = "Exception";

	// there are not member variables in this class
	// all the method invoked from main are thus all changed to static to avoid
	// misleading

	// exception that is swallowed and never handled is changed to be thrown
	// from main

	// this class is used as stand-alone now so all other methods except
	// main are private
	// when (if) this is integrated into geWorkbench, all those four called in
	// main probably need to be changed to be more accessible
	/**
	 * Currently main is the only invocation point of this class from
	 * geWorkbench.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args == null || args.length <= 1) {
			args = new String[] { StandAloneCaArrayClientWrapper.TYPEVALUE,
					"oddtype.txt" };
		}
		log.debug("In StandAloneCaArrayClientExec main. args="
				+ java.util.Arrays.toString(args));
		if (args != null && args.length >= 4) {
			String url = args[2];
			int port = new Integer(args[3].trim());
			String username = null;
			String password = null;
			String resultFilename = args[1];

			if (args[0]
					.equalsIgnoreCase(StandAloneCaArrayClientWrapper.EXPERIMENTINFO)) {
				if (args.length >= 6) {
					username = args[4];
					password = args[5];
				}
				lookupExperiments(url, port, username, password, resultFilename);
				log.debug("Ending lookup experiment. " + new Date());
			} else if (args[0]
					.equalsIgnoreCase(StandAloneCaArrayClientWrapper.HYB)) {
				String hybridname = args[4];
				String qType = args[5];
				if (args.length >= 8) {
					username = args[6];
					password = args[7];
				}
				getDataSet(url, port, username, password, hybridname, qType,
						resultFilename);
				log.debug("Ending loading dataset" + new Date());
			} else if (args[0]
					.equalsIgnoreCase(StandAloneCaArrayClientWrapper.TYPEVALUE)) {
				if (args.length >= 6) {
					username = args[4];
					password = args[5];
				}
				lookupTypeValues(url, port, username, password,
						CaARRAYQueryPanel.listContent, resultFilename);
				log.debug("Ending loading type values" + new Date());
			} else if (args[0]
					.equalsIgnoreCase(StandAloneCaArrayClientWrapper.FILTERINFO)) {
				String filtername = args[4];
				String filterkey = args[5];
				if (args.length >= 8) {
					username = args[6];
					password = args[7];
				}
				lookupExperimentsWithFilter(url, port, username, password,
						filtername, filterkey, args[1]);
				log.debug("Ending loading with filter" + new Date());
			}
		}
	}

	// used in main, changed to private
	// return value is removed because it is never caught and used by anything
	// what this class does is to create a disk file
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
	 */
	@SuppressWarnings("unchecked")
	private static void lookupTypeValues(String url, int port, String username,
			String password, String[] types, String typeValueFileName) {
		try {

			CaArrayServer server = new CaArrayServer(url, port);
			if (username == null || username.trim().length() == 0) {
				server.connect();// enable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService searchService = server.getSearchService();

			TreeMap<String, Set<String>> tree = new TreeMap<String, Set<String>>();
			CQLQuery query = null;

			for (String type : types) {
				query = CQLQueryGenerator.generateQuery(type, null); // TODO
																		// the
																		// second
																		// parameter
																		// of
																		// generateQuery
																		// is
																		// never
																		// used
				List list = searchService.search(query);
				TreeSet<String> values = new TreeSet<String>();
				for (Object o : list) {
					if (o instanceof Organism) {
						Organism e = (Organism) o;
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
				}
				tree.put(type, values);
			}
			if (tree == null || tree.size() == 0) {
				log.debug("No tree is created."
						+ new File(typeValueFileName).getAbsolutePath());
				return;
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
			return;
		} catch (Exception e) {
			e.printStackTrace();
			processException(e, typeValueFileName);
		}
	}

	// used in this class, changed to private
	private static void processException(Exception e, String filename) {
		if (e instanceof gov.nih.nci.caarray.services.ServerConnectionException) {
			createFile(filename, ServerConnectionException);
		} else if (e instanceof FailedLoginException) {
			createFile(filename, FailedLoginException);
		} else {
			createFile(filename, Exception);
		}
	}

	// used only in this class, changed to private
	private static void createFile(String filename, String extension) {
		try {
			File file = new File(filename + "." + extension);
			file.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// used in main, change to private for now
	// return value is removed because it is not used
	@SuppressWarnings("unchecked")
	private static void lookupExperimentsWithFilter(String url, int port,
			String username, String password, String type, String value,
			String savedFilename) {
		log.debug("Get  into lookupExpermentWithFilter");

		try {

			log.debug("Beginning lookupExperimentsWithFilter");
			CaArrayServer server = new CaArrayServer(url, port);
			server.connect();
			if (username == null || username.trim().length() == 0) {
				server.connect();// enable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService service = server.getSearchService();

			CQLQuery query = CQLQueryGenerator.generateQuery(
					CQLQueryGenerator.EXPERIMENT, type, value);
			List list = service.search(query);
			Vector<CaArray2Experiment> exps = new Vector<CaArray2Experiment>();

			for (Object o : list) {
				Experiment e = (Experiment) o;
				log.debug("Experiment : " + e.getTitle() + "| "
						+ e.getManufacturer().getName());
				Set<Hybridization> h1 = (((Experiment) o).getHybridizations());

				if (h1.size() > 0) {
					String[] hybridizationValues = new String[h1.size()];
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
				} // end of checking h1.size() > 0
			}
			
			CaArray2Experiment[] experimentsArray = new CaArray2Experiment[exps
					.size()];
			ObjectOutputStream outputStream = null;

			try {
				log.debug("File saved at"
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
			return;
		} catch (Exception e) {
			e.printStackTrace();
			processException(e, savedFilename);
		}
	}

	// used in main, changed to private for now
	@SuppressWarnings("unchecked")
	private static void lookupExperiments(String url, int port,
			String username, String password, String experimentFileName) {

		CaArrayServer server = new CaArrayServer(url, port);
		try {
			if (username == null || username.trim().length() == 0) {
				server.connect();// disable a user login.
			} else {
				server.connect(username, password);
			}
			CaArraySearchService service = server.getSearchService();
			Vector<CaArray2Experiment> exps = new Vector<CaArray2Experiment>();

			CQLQuery query = CQLQueryGenerator
					.generateQuery(CQLQueryGenerator.EXPERIMENT);
			long time0 = System.currentTimeMillis();
			List list = service.search(query);
			long time1 = System.currentTimeMillis();
			log.debug("list size "+list.size()+" returned in "+(time1-time0)+" milliseconds");
			for (Object o : list) {
				Experiment e = ((Experiment) o);
				log.debug("get Exp: " + new Date());
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
							// Retrieve the appropriate data depending on the
							// type
							// of the column.
							if (typeClass != String.class
									&& typeClass != Boolean.class) {
								sets.add(qType.getName());
							}
						}
						qTypes = new String[sets.size()];
						qTypes = sets.toArray(qTypes);
					}

					long time2 = System.currentTimeMillis();
					log.debug("deeper data query takes "+(time2-time1)+ " milliseconds");
	
					CaArray2Experiment exp = new CaArray2Experiment(url, port);
					exp.setName(e.getTitle());
					exp.setDescription(e.getDescription());
					exp.setHybridizations(hybridizationValues);
					exp.setQuantitationTypes(qTypes);
					
					exps.add(exp);
				} // end of checking h1.size() > 0
			}

			CaArray2Experiment[] experimentsArray = new CaArray2Experiment[exps
					.size()];
			exps.toArray(experimentsArray);

			ObjectOutputStream outputStream = null;

			try {
				log.debug("File saved at"
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
		} catch (Exception e) {
			e.printStackTrace();
			processException(e, experimentFileName);
		}
		log.debug("Ending the lookupExperiments " + new Date());

	}

	// used only in this class, so changed to private
	/**
	 * Called by lookupExepriemtns and lookupExperimentsWithFiler.
	 * 
	 * @param service
	 * @param hybridization
	 * @return gov.nih.nci.caarray.domain.data.DataSet
	 */
	private static DataSet getDataSet(CaArraySearchService service,
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

	// used in main, changed to private for now
	// return type is removed because it is not used
	/**
	 * THe method to grab the data from caArray server with defined
	 * Hybridization and QuantitationType. A BISON DataType will be returned.
	 * 
	 * @param service
	 * @param hybridizationStr
	 * @param quantitationType
	 * @return
	 */
	private static void getDataSet(String url, int port,
			String username, String password, String hybridizationStr,
			String quantitationType, String outputFilename) {
		log.debug("Running the getDataSet" + new Date());
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
		AbstractProbe[] markersArray;
		Hybridization hybridization = new Hybridization();
		hybridization.setName(hybridizationStr);
		List<Hybridization> set = service.search(hybridization);
		if (set == null || set.size() == 0) {
			return;
		}

		hybridization = service.search(hybridization).get(0);
		DataSet dataSet = null;

		// If raw data doesn't exist, try to find derived data
		Set<DerivedArrayData> derivedArrayDataSet = hybridization
				.getDerivedDataCollection();
		log.debug("Size of the derivedArrayDataSet is "
				+ derivedArrayDataSet.size());
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
						Class<?> typeClass = qType.getTypeClass();
						// Retrieve the appropriate data depending
						// on the type of the column.
						if (typeClass == Float.class) {
							float[] values = ((FloatColumn) populatedColumn)
									.getValues();
							for (int i = 0; i < values.length; i++) {
								doubleValues[i] = values[i];
							}

						} else if (typeClass == Integer.class) {
							int[] values = ((IntegerColumn) populatedColumn)
									.getValues();
							for (int i = 0; i < values.length; i++) {
								doubleValues[i] = values[i];
							}

						} else if (typeClass == Long.class) {
							long[] values = ((LongColumn) populatedColumn)
									.getValues();
							for (int i = 0; i < values.length; i++) {
								doubleValues[i] = values[i];
							}

						} else if (typeClass == Double.class) {
							doubleValues = ((DoubleColumn) populatedColumn)
									.getValues();
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
	}
}
