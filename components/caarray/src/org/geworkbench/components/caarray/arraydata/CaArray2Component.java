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
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.remoteresources.carraydata.CaArray2Experiment;
import org.geworkbench.builtin.projects.util.CaARRAYPanel;
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

		CaARRAYPanel.isCancelled = false;

		String currentConnectionInfo = CaARRAYPanel.createConnectonInfo(url, port, username,
				password);
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

				if (CaARRAYPanel.isCancelled
						&& CaARRAYPanel.cancelledConnectionInfo != null
						&& CaARRAYPanel.cancelledConnectionInfo
								.equalsIgnoreCase(currentConnectionInfo)) {
					return;
				}
				publishCaArrayEvent(event);
			} else if (ce.getRequestItem().equalsIgnoreCase(
					CaArrayRequestEvent.BIOASSAY)) { // For BioAssay detail,
														// another kind of
														// request.
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

				String chipType = AnnotationParser.matchChipType(null, "",
						false);

				if (merge) {
					doMerge(client, hybridzations, qType, experimentName,
							currentConnectionInfo, chipType);
				} else {

					int number = 0; // index number out of total arrays
					for (String hybridizationName : hybridzations.keySet()) {

						if (CaARRAYPanel.isCancelled
								&& CaARRAYPanel.cancelledConnectionInfo != null
								&& CaARRAYPanel.cancelledConnectionInfo
										.equalsIgnoreCase(currentConnectionInfo)) {
							return;
						}
						String hybridizationId = hybridzations
								.get(hybridizationName);
						CSExprMicroarraySet maSet2 = client.getDataSet(
								hybridizationName, hybridizationId, qType,
								chipType);
						;

						if (CaARRAYPanel.isCancelled
								&& CaARRAYPanel.cancelledConnectionInfo != null
								&& CaARRAYPanel.cancelledConnectionInfo
										.equalsIgnoreCase(currentConnectionInfo)) {
							return;
						}

						publishCaArraySuccessEvent(new CaArraySuccessEvent(
								number++, hybridzations.size()));

						if (maSet2 != null) {
							maSet2.setLabel(experimentName + "_"
									+ hybridizationName);

							publishProjectNodeAddedEvent(new org.geworkbench.events.ProjectNodeAddedEvent(
									"message", maSet2, null));
						}
					} // loop of all hybridizations
				}

				CaArrayEvent event = new CaArrayEvent(url, port);
				event.setPopulated(true);
				event.setInfoType(CaArrayEvent.BIOASSAY); // this only disposes
															// caARRAYPanel
				if (CaARRAYPanel.isCancelled
						&& CaARRAYPanel.cancelledConnectionInfo != null
						&& CaARRAYPanel.cancelledConnectionInfo
								.equalsIgnoreCase(currentConnectionInfo)) {
					return;
				}
				publishCaArrayEvent(event);

			} // end of the last 'else if' for CaArrayRequestEvent.BIOASSAY)

		} catch (ServerConnectionException se) {
			CaArrayEvent event = new CaArrayEvent(url, port);
			event.setPopulated(false);
			event.setSucceed(false);
			if (CaARRAYPanel.isCancelled
					&& CaARRAYPanel.cancelledConnectionInfo != null
					&& CaARRAYPanel.cancelledConnectionInfo
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
			if (CaARRAYPanel.isCancelled
					&& CaARRAYPanel.cancelledConnectionInfo != null
					&& CaARRAYPanel.cancelledConnectionInfo
							.equalsIgnoreCase(currentConnectionInfo)) {
				return;
			}
			publishCaArrayEvent(event);

		} catch (Exception e) {
			CaArrayEvent event = new CaArrayEvent(url, port);
			event.setPopulated(false);
			event.setSucceed(false);
			event.setErrorMessage(e.getMessage());
			publishCaArrayEvent(event);
		}

	}

	@SuppressWarnings("unchecked")
	private void doMerge(CaArrayClient client,
			SortedMap<String, String> hybridzations, String qType,
			String experimentName, String currentConnectionInfo, String chipType)
			throws Exception {
		DSMicroarraySet<? extends DSMicroarray>[] sets = new DSMicroarraySet<?>[hybridzations
				.size()];

		int number = 0;
		for (String hybridizationName : hybridzations.keySet()) {
			String hybridizationId = hybridzations.get(hybridizationName);
			sets[number] = client.getDataSet(hybridizationName,
					hybridizationId, qType, chipType);

			if (sets[number] == null)
				continue;

			sets[number].setLabel(experimentName + "_" + hybridizationName);

			if (CaARRAYPanel.isCancelled
					&& CaARRAYPanel.cancelledConnectionInfo != null
					&& CaARRAYPanel.cancelledConnectionInfo
							.equalsIgnoreCase(currentConnectionInfo)) {
				return;
			}

			publishCaArraySuccessEvent(new CaArraySuccessEvent(number++,
					hybridzations.size()));

		} // loop of all hybridizations
		ProjectPanel.getInstance().doMergeSets(sets);
		for(DSMicroarraySet<? extends DSMicroarray>set: sets) {
			Object obj = set;
			AnnotationParser.cleanUpAnnotatioAfterUnload((DSDataSet<DSBioObject>) obj);
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
		CaArrayQueryResultEvent event = new CaArrayQueryResultEvent();
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
