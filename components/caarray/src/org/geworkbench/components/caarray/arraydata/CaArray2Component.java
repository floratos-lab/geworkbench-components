package org.geworkbench.components.caarray.arraydata;

import gov.nih.nci.caarray.services.ServerConnectionException;

import java.awt.Component;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.security.auth.login.FailedLoginException;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
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

	private StandAloneCaArrayClientWrapper externalDataSetDownloadClient = new StandAloneCaArrayClientWrapper();

	protected static final String SERVER_NAME = "array-stage.nci.nih.gov ";
	protected static final int JNDI_PORT = 8080;
	protected static final int GRID_SERVICE_PORT = 8080;

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

		// below is to invoke external Java process to call caArray server.
		String currentConnectionInfo = url + port;
		if (username != null && username.length() > 0) {
			currentConnectionInfo = currentConnectionInfo + username + password;
		}
		try {

			if (ce.getRequestItem().equalsIgnoreCase(
					CaArrayRequestEvent.EXPERIMENT)) {

				// TreeMap<String, String[]> treeMap = null;
				CaArrayEvent event = new CaArrayEvent(url, port);
				// TreeMap<String, String> desTreeMap = null;
				CaArray2Experiment[] exps = null;
				if (ce.isUseFilterCrit()) {
					Map<String, String> filters = ce.getFilterCrit();
					if (filters != null) {
						exps = externalDataSetDownloadClient.lookupExperiments(
								url, port, username, password, filters);
					}
				} else {
					exps = externalDataSetDownloadClient.lookupExperiments(url,
							port, username, password);
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
					SortedMap<String, Long> hybridzations = ce.getAssayNameFilter();
					boolean merge = ce.isMerge();
					String qType = ce.getQType();
					if (qType == null) {
						qType = "CHPSignal";
					}
					String hybName = hybridzations.firstKey();
					Long hybId = hybridzations.get(hybName);
					CSExprMicroarraySet maSet = externalDataSetDownloadClient
							.getDataSet(url, port, username, password,
									hybName, hybId, qType);
					CSExprMicroarraySet totalSet = maSet;
					if (!merge) {
						if (maSet != null) {
							maSet.setLabel(experimentName + "_"
									+ hybName);
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
						for (String hybridizationName: hybridzations.keySet()) {
							if(hybridizationName.equals(firstName))
								continue;
							
							if (isCancelled
									&& cancelledConnectionInfo != null
									&& cancelledConnectionInfo
											.equalsIgnoreCase(currentConnectionInfo)) {
								return;
							}
							Long hybridizationId = hybridzations.get(hybridizationName);
							CSExprMicroarraySet maSet2 = externalDataSetDownloadClient
									.getDataSet(url, port, username, password,
											hybridizationName, hybridizationId, qType);
							;
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

	/**
	 * 
	 * @param ce
	 * @param source
	 */
	@Subscribe
	public void receive(CaArrayQueryEvent ce, Object source) {
		log.debug("CaArrayQueryEvent is received.");

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
				treeMap = externalDataSetDownloadClient.lookupTypeValues(
							url, port, username, password, listCritiria);
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

	protected JPanel mainPanel;

	public Component getComponent() {
		return mainPanel;
	}
}
