package org.geworkbench.components.caarray.arraydata;

import gov.nih.nci.caarray.services.ServerConnectionException;
import gov.nih.nci.caarray.services.external.v1_0.InvalidInputException;

import java.awt.Component;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.security.auth.login.FailedLoginException;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.remoteresources.carraydata.CaArray2Experiment;
import org.geworkbench.builtin.projects.util.CaARRAYPanel;
import org.geworkbench.components.caarray.arraydata.CaArrayClient.MarkerValuePair;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.CaArrayEvent;
import org.geworkbench.events.CaArrayQueryEvent;
import org.geworkbench.events.CaArrayRequestEvent;
import org.geworkbench.events.CaArrayRequestHybridizationListEvent;
import org.geworkbench.events.CaArrayReturnHybridizationListEvent;
import org.geworkbench.events.CaArraySuccessEvent;

/**
 * The wrapper class for CaArray Component.
 *
 * @author xiaoqing
 * @version $Id$
 *
 */
@AcceptTypes( { DSMicroarraySet.class, CSSequenceSet.class })
public class CaArray2Component implements VisualPlugin {

	private static Log log = LogFactory.getLog(CaArray2Component.class);

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

				String qType = ce.getQType();
				if (qType == null) {
					qType = "CHPSignal";
				}

				String chipType = AnnotationParser.matchChipType(null, "",
						false);

				doMerge(client, hybridzations, qType, experimentName,
							currentConnectionInfo, chipType);

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

	/**
	 * Translate the data file into BISON type.
	 *
	 */
	private static DSMicroarray processDataToBISON(
			MarkerValuePair[] pairs, String name, final DSMicroarraySet<DSMicroarray> microarraySet) {

		int markerNo = pairs.length;
		List<Double> valuesList = new ArrayList<Double>();
		for (int i = 0; i < pairs.length; i++) {
			MarkerValuePair p = pairs[i];
			valuesList.add(new Double(p.value));
		}

		DSMicroarray microarray = new CSMicroarray(0, markerNo, name, null, null, false,
				DSMicroarraySet.geneExpType);
		microarray.setLabel(name);
		int[] markerOrder = microarraySet.getNewMarkerOrder();
		for (int i = 0; i < markerNo; i++) {
			DSMutableMarkerValue m = (DSMutableMarkerValue) microarray.getMarkerValue(markerOrder[i]);
			m.setValue(valuesList.get(i));
			m.setMissing(false);
		}

		return microarray;
	}

	private static CSMicroarraySet createInitialMicroarraySet(final MarkerValuePair[] markerValuePairs, String chipType) {
		CSMicroarraySet microarraySet = new CSMicroarraySet();
		List<String> markerNames = new ArrayList<String>();

		for (int i = 0; i < markerValuePairs.length; i++) {
			MarkerValuePair p = markerValuePairs[i];
			markerNames.add(p.marker);
		}

		int markerNo = markerNames.size();
		microarraySet.initialize(0, markerNo);
		microarraySet.getMarkerVector().clear();
		// maSet.setCompatibilityLabel(bioAssayImpl.getIdentifier());
		for (int z = 0; z < markerNo; z++) {

			String markerName = markerNames.get(z);
			if (markerName != null) {
				CSExpressionMarker marker = new CSExpressionMarker(z);
				// bug 1956 geneName will be correctly initialized before usage,
				// lazy initialization
				marker.setGeneName(null);
				marker.setLabel(markerName);
				marker.setDescription(markerName);
				microarraySet.getMarkerVector().add(z, marker);
			} else {
				log.error("LogicalProbes have some null values. The location is "
						+ z);
			}
		}
		microarraySet.setCompatibilityLabel(chipType);
		microarraySet.setAnnotationFileName(AnnotationParser.getLastAnnotationFileName());
		AnnotationParser.setChipType(microarraySet, chipType);
		microarraySet.sortMarkers(markerNo);
		
		return microarraySet;
	}
	
	private void doMerge(CaArrayClient client,
			SortedMap<String, String> hybridzations, String qType,
			String experimentName, String currentConnectionInfo, String chipType)
			throws Exception {
		CSMicroarraySet microarraySet = null;

		String desc = "";
		if(hybridzations.size()>1)desc = "Merged DataSet: ";

		int number = 0;
		CaArraySuccessEvent caArraySuccessEvent = new CaArraySuccessEvent(hybridzations.size());
		for (String hybridizationName : hybridzations.keySet()) {
			String hybridizationId = hybridzations.get(hybridizationName);
			MarkerValuePair[] markerValuePairs = client.getDataSet(hybridizationName,
					hybridizationId, qType);
			
			if(number==0) { // create the dataset when processing the first microarray
				microarraySet = createInitialMicroarraySet(markerValuePairs, chipType);
			}

			DSMicroarray microarray = processDataToBISON(markerValuePairs, hybridizationName, microarraySet);

			if (CaARRAYPanel.isCancelled
					&& CaARRAYPanel.cancelledConnectionInfo != null
					&& CaARRAYPanel.cancelledConnectionInfo
							.equalsIgnoreCase(currentConnectionInfo)) {
				return;
			}

			microarraySet.add(microarray);
			desc += experimentName + "_" + hybridizationName + " ";
			
			publishCaArraySuccessEvent(caArraySuccessEvent);
			number++;
		} // loop of all hybridizations

		microarraySet.setLabel(desc);
		ProjectPanel.getInstance().addProjectNode(microarraySet, null);
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

		boolean succeeded;
		String message = null;
		TreeMap<String, Set<String>> treeMap = null;
		try {
			CaArrayClient client = new CaArrayClient(url, port, username,
					password);
			treeMap = client.lookupTypeValues();
			succeeded = true;
		} catch (ServerConnectionException se) {
			succeeded = false;
			message = "ServerConnectionException: host " + url
					+ "; port " + port + "; " + se.getMessage();
		} catch (FailedLoginException fe) {
			succeeded = false;
			message = "FailedLoginException: username " + username
					+ "; " + fe.getMessage();
		} catch (RemoteException e) {
			succeeded = false;
			message = "RemoteException: " + e.getMessage();
		} catch (InvalidInputException e) {
			succeeded = false;
			message = "InvalidInputException: "
					+ e.getMessage();
		}
		ProjectPanel.getInstance().processCaArrayResult(succeeded, message, treeMap);
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
