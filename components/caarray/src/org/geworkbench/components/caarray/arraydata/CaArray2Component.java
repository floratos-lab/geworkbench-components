package org.geworkbench.components.caarray.arraydata;

import gov.nih.nci.caarray.domain.array.AbstractDesignElement;
import gov.nih.nci.caarray.domain.array.AbstractProbe;
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
import gov.nih.nci.caarray.services.CaArrayServer;
import gov.nih.nci.caarray.services.ServerConnectionException;
import gov.nih.nci.caarray.services.search.CaArraySearchService;

import java.awt.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.security.auth.login.FailedLoginException;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.remoteresources.carraydata.CaArray2Experiment;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.CaArrayEvent;
import org.geworkbench.events.CaArrayQueryEvent;
import org.geworkbench.events.CaArrayQueryResultEvent;
import org.geworkbench.events.CaArrayRequestEvent;

// FIXME cleaning up but no plan to commit yet
/**
 * The wrapper class for CaArray Component.
 * 
 * @author xiaoqing
 * @version $Id: CaArray2Component.java,v 1.15 2008/05/01 14:23:10 xiaoqing Exp $
 * 
 */
@AcceptTypes( { DSMicroarraySet.class, CSSequenceSet.class })
public class CaArray2Component implements VisualPlugin {

	private Log log = LogFactory.getLog(CaArray2Component.class);
	private CaArrayQueryClient dataSetDownloadClient = new CaArrayQueryClient();
	private StandAloneCaArrayClientWrapper externalDataSetDownloadClient = new StandAloneCaArrayClientWrapper();

	protected static final String SERVER_NAME = "array-stage.nci.nih.gov ";
	protected static final int JNDI_PORT = 8080;
	protected static final int GRID_SERVICE_PORT = 8080;
	private boolean useExternalCaArray = true;
	private String cancelledConnectionInfo = null;
	private boolean isCancelled = false;

	/**
	 * 
	 * @param ce
	 * @param source
	 */
	@Subscribe
	public void receive(CaArrayRequestEvent ce, Object source) {
		
		if (ce == null) {
			return;
		}

		String url = ce.getUrl();
		int port = ce.getPort();
		String username = ce.getUsername();
		String password = ce.getPassword();

		if (ce.getRequestItem().equalsIgnoreCase(CaArrayRequestEvent.CANCEL)) {
			cancelledConnectionInfo = url + port;
			if (username != null && username.length() > 0) {
				cancelledConnectionInfo = cancelledConnectionInfo + username
						+ password;
			}
			isCancelled = true;
			return;
		} else {
			isCancelled = false;
		}

		if (!useExternalCaArray) {// below is the default handle the event
			// when integration works.
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
							exps = dataSetDownloadClient.lookupExperiments(
									searchService, url, port, username,
									password, filters);
						}
					} else {
						exps = dataSetDownloadClient.lookupExperiments(
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
						HashMap<String, String[]> filterCrit = ce
								.getFilterCrit();
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
								publishProjectNodeAddedEvent(pevent);
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
										maSet2.setLabel(experimentName + "_"
												+ hybridzations[i]);
										org.geworkbench.events.ProjectNodeAddedEvent pevent = new org.geworkbench.events.ProjectNodeAddedEvent(
												"message", maSet2, null);
										publishProjectNodeAddedEvent(pevent);
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
							publishProjectNodeAddedEvent(pevent);
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
		} else {
			// below is to invoke external Java process to call caArray server.
			String currentConnectionInfo = url + port;
			if (username != null && username.length() > 0) {
				currentConnectionInfo = currentConnectionInfo + username
						+ password;
			}
			try {

				if (ce.getRequestItem().equalsIgnoreCase(
						CaArrayRequestEvent.EXPERIMENT)) {

					// TreeMap<String, String[]> treeMap = null;
					CaArrayEvent event = new CaArrayEvent(url, port);
					// TreeMap<String, String> desTreeMap = null;
					CaArray2Experiment[] exps = null;
					if (ce.isUseFilterCrit()) {
						HashMap<String, String[]> filters = ce.getFilterCrit();
						if (filters != null) {
							exps = externalDataSetDownloadClient
									.lookupExperiments(url, port, username,
											password, filters);
						}
					} else {
						exps = externalDataSetDownloadClient.lookupExperiments(
								url, port, username, password);
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

					if (isCancelled
							&& cancelledConnectionInfo != null
							&& cancelledConnectionInfo
									.equalsIgnoreCase(currentConnectionInfo)) {
						return;
					}
					publishCaArrayEvent(event);
				} else {
					// For BioAssay detail, another kind of request.
					if (ce.getRequestItem().equalsIgnoreCase(
							CaArrayRequestEvent.BIOASSAY)) {
						HashMap<String, String[]> filterCrit = ce
								.getFilterCrit();
						String experimentName = filterCrit
								.get(CaArrayRequestEvent.EXPERIMENT)[0];
						String[] hybridzations = filterCrit
								.get(CaArrayRequestEvent.BIOASSAY);
						boolean merge = ce.isMerge();
						String qType = ce.getQType();
						if (qType == null) {
							qType = "CHPSignal";
						}
						CSExprMicroarraySet maSet = externalDataSetDownloadClient
								.getDataSet(url, port, username, password,
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

								if (isCancelled
										&& cancelledConnectionInfo != null
										&& cancelledConnectionInfo
												.equalsIgnoreCase(currentConnectionInfo)) {
									return;
								}
								publishProjectNodeAddedEvent(pevent);

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
								if (isCancelled
										&& cancelledConnectionInfo != null
										&& cancelledConnectionInfo
												.equalsIgnoreCase(currentConnectionInfo)) {
									return;
								}
								CSExprMicroarraySet maSet2 = externalDataSetDownloadClient
										.getDataSet(url, port, username,
												password, hybridzations[i],
												qType);
								;
								if (maSet2 == null) {
									event.setPopulated(false);
								} else {
									maSet2.setLabel(experimentName);
									event.setPopulated(true);
									if (!merge) {

										maSet2.setLabel(experimentName + "_"
												+ hybridzations[i]);
										org.geworkbench.events.ProjectNodeAddedEvent pevent = new org.geworkbench.events.ProjectNodeAddedEvent(
												"message", maSet2, null);
										publishProjectNodeAddedEvent(pevent);
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
							if (isCancelled
									&& cancelledConnectionInfo != null
									&& cancelledConnectionInfo
											.equalsIgnoreCase(currentConnectionInfo)) {
								return;
							}
							publishProjectNodeAddedEvent(pevent);
						}
						event.setDataSet(totalSet);
						event.setInfoType(CaArrayEvent.BIOASSAY);
						if (isCancelled
								&& cancelledConnectionInfo != null
								&& cancelledConnectionInfo
										.equalsIgnoreCase(currentConnectionInfo)) {
							return;
						}
						publishCaArrayEvent(event);

					}
				}

			} catch (ServerConnectionException se) {
				CaArrayEvent event = new CaArrayEvent(url, port);
				event.setPopulated(false);
				event.setSucceed(false);
				se.printStackTrace();
				if (isCancelled
						&& cancelledConnectionInfo != null
						&& cancelledConnectionInfo
								.equalsIgnoreCase(currentConnectionInfo)) {
					return;
				}
				publishCaArrayEvent(event);
				event.setErrorMessage("Cannot connect to the server at " + url
						+ ":" + port);
			} catch (FailedLoginException fe) {
				CaArrayEvent event = new CaArrayEvent(url, port);
				event.setPopulated(false);
				event.setSucceed(false);

				event
						.setErrorMessage("Either username or password is incorrect. Please check your login credentials. ");
				if (isCancelled
						&& cancelledConnectionInfo != null
						&& cancelledConnectionInfo
								.equalsIgnoreCase(currentConnectionInfo)) {
					return;
				}
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
	}

	/**
	 * 
	 * @param ce
	 * @param source
	 */
	@Subscribe
	public void receive(CaArrayQueryEvent ce, Object source) {
		// FIXME: maybe anything about CaArrayQueryEvent should be removed
		System.out.println("WARNING: CaArrayQueryEvent is never properly published. Then it is received here, something is wrong.");
		System.exit(0);

		try {
			if (ce != null
					&& ce.getInfoType().equalsIgnoreCase(
							CaArrayQueryEvent.GOTVALIDVALUES)) {
				String url = ce.getUrl();
				int port = ce.getPort();
				String[] listCritiria = ce.getQueries();
				String username = ce.getUsername();
				String password = ce.getPassword();

				TreeMap<String, Set<String>> treeMap = null;
				if (useExternalCaArray) {
					treeMap = externalDataSetDownloadClient.lookupTypeValues(
							url, port, username, password, listCritiria);
				} else {
					CaArrayServer server = new CaArrayServer(url, port);
					if (username == null || username.trim().length() == 0) {
						server.connect();// enable a user login.
					} else {
						server.connect(username, password);
					}
					CaArraySearchService searchService = server
							.getSearchService();
					DataRetrievalRequest request = new DataRetrievalRequest();
					treeMap = CaArrayQueryClient.lookupTypeValues(
							searchService, request, listCritiria);
				}
				CaArrayQueryResultEvent event = new CaArrayQueryResultEvent(
						null, url, port, ce.getUsername(), ce.getPassword());

				event.setQueryPairs(treeMap);
				event.setSucceed(true);
				publishCaArrayQueryResultEvent(event);
			} else {// For BioAssay detail, another kind of request.
				if (ce.getInfoType().equalsIgnoreCase(
						CaArrayQueryEvent.GOTEXPERIMENTS)) {

				}
			}

		} catch (gov.nih.nci.caarray.services.ServerConnectionException se) {
			CaArrayQueryResultEvent event = new CaArrayQueryResultEvent(null,
					ce.getUrl(), ce.getPort(), ce.getUsername(), ce
							.getPassword());
			event.setSucceed(false);
			event.setErrorMessage("Cannot connect to the server at "
					+ ce.getUrl() + ":" + ce.getPort());
			publishCaArrayQueryResultEvent(event);
		} catch (FailedLoginException fe) {
			CaArrayQueryResultEvent event = new CaArrayQueryResultEvent(null,
					ce.getUrl(), ce.getPort(), ce.getUsername(), ce
							.getPassword());
			event.setSucceed(false);
			event
					.setErrorMessage("Either username or password is incorrect. Please check your login credentials. ");

			publishCaArrayQueryResultEvent(event);

		}

		catch (Exception e) {

			CaArrayQueryResultEvent event = new CaArrayQueryResultEvent(null,
					ce.getUrl(), ce.getPort(), ce.getUsername(), ce
							.getPassword());
			event.setSucceed(false);
			event.setErrorMessage("Cannot connect to the server.");
			publishCaArrayQueryResultEvent(event);
		}
	}

	@Publish
	public org.geworkbench.events.ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			org.geworkbench.events.ProjectNodeAddedEvent event) {
		return event;
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
	 * The constructor does not do much, but it has to be here to make Digester work.
	 */
	public CaArray2Component() {
		mainPanel = new JPanel();
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
	private CSExprMicroarraySet getDataSet(CaArraySearchService service,
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
						Class<?> typeClass = qType.getTypeClass();
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
	private CSExprMicroarraySet processDataToBISON(AbstractProbe[] markersArray,
			double[] values, String name) {
		Date date = new Date();
		long startTime = date.getTime();

		int markerNo = markersArray.length;
		DSMicroarray microarray = null;
		CSExprMicroarraySet maSet = new CSExprMicroarraySet();
		if (!maSet.initialized) {
			maSet.initialize(0, markerNo);

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

	protected JPanel mainPanel;

	public Component getComponent() {
		return mainPanel;
	}
}
