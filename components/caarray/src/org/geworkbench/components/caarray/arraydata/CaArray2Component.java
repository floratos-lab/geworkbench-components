package org.geworkbench.components.caarray.arraydata;

import gov.nih.nci.caarray.services.ServerConnectionException;
import gov.nih.nci.caarray.services.external.v1_0.InvalidInputException;

import java.awt.Component;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.SortedMap;

import javax.security.auth.login.FailedLoginException;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
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
import org.geworkbench.events.CaArrayRequestHybridizationListEvent;
import org.geworkbench.events.CaArrayReturnHybridizationListEvent;
import org.geworkbench.events.CaArraySuccessEvent;

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

	private String cancelledConnectionInfo = null;
	private boolean isCancelled = false;

	// process two types of queries: (1) the list of experiments; and (2) the
	// actual data
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

		String currentConnectionInfo = url + port;
		if (username != null && username.length() > 0) {
			currentConnectionInfo = currentConnectionInfo + username + password;
		}
		try {
			CaArrayClient client = new CaArrayClient(url, port, username,
					password);

			if (ce.getRequestItem().equalsIgnoreCase(
					CaArrayRequestEvent.EXPERIMENT)) {

				// TreeMap<String, String[]> treeMap = null;
				CaArrayEvent event = new CaArrayEvent(url, port);
				// TreeMap<String, String> desTreeMap = null;
				CaArray2Experiment[] exps = null;
				if (ce.isUseFilterCrit()) {
					Map<String, String> filters = ce.getFilterCrit();
					if (filters != null) {
						for (String key : filters.keySet()) {
							String value = filters.get(key);
							if (value != null) {
								exps = client
										.getExperimentListWithFilter(
												key, value);
								continue;
							}
						}
					}
				} else {
					// case of no filtering
					exps = client.getExperimentListWithFilter(
							null, null);
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
					Map<String, String> filterCrit = ce.getFilterCrit();
					String experimentName = filterCrit
							.get(CaArrayRequestEvent.EXPERIMENT);
					SortedMap<String, String> hybridzations = ce
							.getAssayNameFilter();
					boolean merge = ce.isMerge();
					String qType = ce.getQType();
					if (qType == null) {
						qType = "CHPSignal";
					}
					String hybName = hybridzations.firstKey();
					String hybId = hybridzations.get(hybName);
					String chipType = AnnotationParser.matchChipType(null, "",
							false);
					CSExprMicroarraySet maSet = client.getDataSet(hybName, hybId, qType,
							chipType);
					publishCaArraySuccessEvent(new CaArraySuccessEvent(1, 1));
					CSExprMicroarraySet totalSet = maSet;
					if (!merge) {
						if (maSet != null) {
							maSet.setLabel(experimentName + "_" + hybName);
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
					if (hybridzations.size() > 1) {
						String firstName = hybridzations.firstKey();
						int number = 0; // index number out of total arrays
						for (String hybridizationName : hybridzations.keySet()) {
							if (hybridizationName.equals(firstName))
								continue;

							if (isCancelled
									&& cancelledConnectionInfo != null
									&& cancelledConnectionInfo
											.equalsIgnoreCase(currentConnectionInfo)) {
								return;
							}
							String hybridizationId = hybridzations
									.get(hybridizationName);
							CSExprMicroarraySet maSet2 = client
									.getDataSet(
											hybridizationName, hybridizationId,
											qType, chipType);
							;
							publishCaArraySuccessEvent(new CaArraySuccessEvent(
									number++, hybridzations.size()));
							if (maSet2 == null) {
								event.setPopulated(false);
							} else {
								maSet2.setLabel(experimentName);
								event.setPopulated(true);
								if (!merge) {

									maSet2.setLabel(experimentName + "_"
											+ hybridizationName);
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
								+ hybridzations.size() + "_merged");
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
			event.setErrorMessage("Cannot connect to the server at " + url
					+ ":" + port);
			publishCaArrayEvent(event);
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

	// the event that data has been retrieved
	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public CaArraySuccessEvent publishCaArraySuccessEvent(
			CaArraySuccessEvent event) {
		return event;
	}

	/**
	 * 
	 * process the query for filtering, publish the event with either the result
	 * of all filter type-values or failure info
	 * 
	 * @param ce
	 * @param source
	 */
	@Subscribe
	public void receive(CaArrayQueryEvent ce, Object source) {
		log.debug("CaArrayQueryEvent is received.");

		String url = ce.getUrl();
		int port = ce.getPort();
		String username = ce.getUsername();
		String password = ce.getPassword();
		CaArrayQueryResultEvent event = new CaArrayQueryResultEvent(null, url,
				port, username, password);
		try {
			CaArrayClient client = new CaArrayClient(url, port, username,
					password);
			event.setQueryPairs(client.lookupTypeValues());
			event.setSucceed(true);
		} catch (ServerConnectionException se) {
			event.setSucceed(false);
			event.setErrorMessage("ServerConnectionException: host " + url
					+ "; port " + port + "; " + se.getMessage());
		} catch (FailedLoginException fe) {
			event.setSucceed(false);
			event.setErrorMessage("FailedLoginException: username " + username
					+ "; " + fe.getMessage());
		} catch (RemoteException e) {
			event.setSucceed(false);
			event.setErrorMessage("RemoteException: " + e.getMessage());
		} catch (InvalidInputException e) {
			event.setSucceed(false);
			event.setErrorMessage("InvalidInputException: "
					+ e.getMessage());
		}
		publishCaArrayQueryResultEvent(event);
	}

	// the event of the new data node to be added
	@Publish
	public org.geworkbench.events.ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			org.geworkbench.events.ProjectNodeAddedEvent event) {
		return event;
	}

	/**
	 * 
	 * @param event:
	 *            the list of experiment retrieved
	 * @return
	 */
	@Publish
	public CaArrayEvent publishCaArrayEvent(CaArrayEvent event) {
		return event;
	}

	// the even that the filter has been processed
	@Publish
	public CaArrayQueryResultEvent publishCaArrayQueryResultEvent(
			CaArrayQueryResultEvent event) {
		return event;
	}

	/**
	 * The constructor does not do much, but it has to be here to make Digester
	 * work.
	 */
	public CaArray2Component() {
		mainPanel = new JPanel();
	}

	protected JPanel mainPanel;

	public Component getComponent() {
		return mainPanel;
	}
	
	@Publish
	public CaArrayReturnHybridizationListEvent publishCaArrayReturnHybridizationListEvent(CaArrayReturnHybridizationListEvent event) {
		return event;
	}
	
	@Subscribe
	public void receive(CaArrayRequestHybridizationListEvent event, Object source) {
		CaArray2Experiment caArray2Experiment = event.getExperiment();
		
		String url = event.getUrl();
		int port = event.getPort();
		String username = event.getUsername();
		String password = event.getPassword();

		try {
			CaArrayClient client = new CaArrayClient(url, port, username,
					password);
			client.getHybridizations(caArray2Experiment);
			publishCaArrayReturnHybridizationListEvent(new CaArrayReturnHybridizationListEvent(caArray2Experiment));
		} catch (FailedLoginException e) {
			CaArrayEvent errorEvent = new CaArrayEvent(url, port);
			errorEvent.setPopulated(false);
			errorEvent.setSucceed(false);

			errorEvent
					.setErrorMessage("Either username or password is incorrect. Please check your login credentials. ");
			publishCaArrayEvent(errorEvent);
		} catch (ServerConnectionException se) {
			CaArrayEvent errorEvent = new CaArrayEvent(url, port);
			errorEvent.setPopulated(false);
			errorEvent.setSucceed(false);
			se.printStackTrace();
			errorEvent.setErrorMessage("Cannot connect to the server at " + url
					+ ":" + port);
			publishCaArrayEvent(errorEvent);
		} catch (InvalidInputException e) {
			CaArrayEvent errorEvent = new CaArrayEvent(url, port);
			errorEvent.setPopulated(false);
			errorEvent.setSucceed(false);
			e.printStackTrace();
			errorEvent.setErrorMessage("InvalidInputException: " + e.getMessage());
			publishCaArrayEvent(errorEvent);
		}
	}
	
}
