package org.geworkbench.components.caarray.arraydata;

import gov.nih.nci.caarray.external.v1_0.data.AbstractDataColumn;
import gov.nih.nci.caarray.external.v1_0.data.DataSet;
import gov.nih.nci.caarray.external.v1_0.data.DataType;
import gov.nih.nci.caarray.external.v1_0.data.DesignElement;
import gov.nih.nci.caarray.external.v1_0.data.DoubleColumn;
import gov.nih.nci.caarray.external.v1_0.data.FloatColumn;
import gov.nih.nci.caarray.external.v1_0.data.HybridizationData;
import gov.nih.nci.caarray.external.v1_0.data.IntegerColumn;
import gov.nih.nci.caarray.external.v1_0.data.LongColumn;
import gov.nih.nci.caarray.external.v1_0.data.QuantitationType;
import gov.nih.nci.caarray.external.v1_0.data.ShortColumn;
import gov.nih.nci.caarray.services.ServerConnectionException;
import gov.nih.nci.caarray.services.external.v1_0.InvalidInputException;

import java.awt.Component;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.geworkbench.util.AffyAnnotationUtil;

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
	private volatile boolean isCancelled;

	// process two types of queries: (1) the list of experiments; and (2) the
	// actual data
	/**
	 *
	 * @param ce
	 * @param source
	 */
	@Subscribe
	public void receive(CaArrayRequestEvent ce, Object source) {

		if (ce.getRequestItem().equalsIgnoreCase(
			CaArrayRequestEvent.CANCEL)) {
			isCancelled = true;
			return;
		}

		String url = ce.getUrl();
		int port = ce.getPort();
		String username = ce.getUsername();
		String password = ce.getPassword();

		isCancelled = false;

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

				if (isCancelled
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

				String chipType = AffyAnnotationUtil.matchAffyAnnotationFile(null);

				getData(client, hybridzations, qType, experimentName,
							currentConnectionInfo, chipType);

				CaArrayEvent event = new CaArrayEvent(url, port);
				event.setPopulated(true);
				event.setInfoType(CaArrayEvent.BIOASSAY); // this only disposes
															// caARRAYPanel
				if (isCancelled
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
			if (isCancelled
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
			if (isCancelled
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
	 * Get expression values from caArray DataSet.
	 *
	 */
	private static List<Double> getValues(DataSet dataSet, String quantitationType)
			throws Exception {

        // Ordered list of column headers (quantitation types like Signal, Log Ratio etc.)
        List<QuantitationType> quantitationTypes = dataSet.getQuantitationTypes();
        // Data for the first hybridization (the only hybridization, in our case)
        if(dataSet.getDatas().size()<1) {
        	throw new Exception("Quantitation type: " + quantitationType + " has no data.");
        }
        HybridizationData data = dataSet.getDatas().get(0);
        // Ordered list of columns with values (columns are in the same order as column headers/quantitation types)
        List<AbstractDataColumn> dataColumns = data.getDataColumns();
        Iterator<AbstractDataColumn> columnIterator = dataColumns.iterator();

		AbstractDataColumn dataColumn = null;
        DataType columnDataType = null;
        for (QuantitationType qType : quantitationTypes) {
            dataColumn = (AbstractDataColumn) columnIterator.next();

            if(qType.getName().equalsIgnoreCase(quantitationType)) {
                columnDataType = qType.getDataType();
            	break; // found the right column
            }
        }

        if(columnDataType==null)throw new Exception("No column of type "+quantitationType+" in this dataset.");
        
        List<Double> values = new ArrayList<Double>();

        // handle all numeric types
        switch (columnDataType) {
            case INTEGER:
                int[] intValues = ((IntegerColumn) dataColumn).getValues();
        		for (int i = 0; i < intValues.length; i++) values.add( (double) intValues[i] );
                break;
            case DOUBLE:
                double[] doubleValues = ((DoubleColumn) dataColumn).getValues();
        		for (int i = 0; i < doubleValues.length; i++) values.add( doubleValues[i] );
                break;
            case FLOAT:
                float[] floatValues = ((FloatColumn) dataColumn).getValues();
        		for (int i = 0; i < floatValues.length; i++) values.add( (double) floatValues[i] );
                break;
            case SHORT:
                short[] shortValues = ((ShortColumn) dataColumn).getValues();
        		for (int i = 0; i < shortValues.length; i++) values.add( (double) shortValues[i] );
                break;
            case LONG:
                long[] longValues = ((LongColumn) dataColumn).getValues();
        		for (int i = 0; i < longValues.length; i++) values.add( (double) longValues[i] );
                break;
            case BOOLEAN:
            case STRING:
            default:
                // Should never get here.
            	log.error("Type "+columnDataType + " not expected.");
        }

		return values;
	}
	
	/**
	 * Translate the data file into BISON type.
	 *
	 */
	private static DSMicroarray createMicroarray(
			final List<Double> valuesList, final String name, final DSMicroarraySet microarraySet) {

		int markerNo = valuesList.size();

		DSMicroarray microarray = new CSMicroarray(0, markerNo, name,
				DSMicroarraySet.expPvalueType);
		microarray.setLabel(name);
		int[] markerOrder = microarraySet.getNewMarkerOrder();
		for (int i = 0; i < markerNo; i++) {
			DSMutableMarkerValue m = (DSMutableMarkerValue) microarray.getMarkerValue(markerOrder[i]);
			m.setValue(valuesList.get(i));
			m.setMissing(false);
		}

		return microarray;
	}

	private static CSMicroarraySet createInitialMicroarraySet(final DataSet dataset, String chipType) {
		CSMicroarraySet microarraySet = new CSMicroarraySet();
		List<String> markerNames = new ArrayList<String>();

        // Ordered list of row headers (probe sets)
        List<DesignElement> probeSets = dataset.getDesignElements();
		for (DesignElement element : probeSets) {
			markerNames.add(element.getName());
		}

		int markerNo = markerNames.size();
		microarraySet.initializeMarkerVector(markerNo);
		microarraySet.getMarkers().clear();
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
				microarraySet.getMarkers().add(z, marker);
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
	
	private void getData(CaArrayClient client,
			SortedMap<String, String> hybridzations, String qType,
			final String experimentName, String currentConnectionInfo, String chipType)
			throws Exception {
		CSMicroarraySet microarraySet = null;

		int number = 0;
		CaArraySuccessEvent caArraySuccessEvent = new CaArraySuccessEvent(hybridzations.size());
		for (String hybridizationName : hybridzations.keySet()) {
			String hybridizationId = hybridzations.get(hybridizationName);
			DataSet dataset = client.getCaArrayDataSet(hybridizationName,
					hybridizationId, qType);
			
			if(number==0) { // create the dataset when processing the first microarray
				microarraySet = createInitialMicroarraySet(dataset, chipType);
			}

			List<Double> values = getValues(dataset, qType);
			DSMicroarray microarray = createMicroarray(values, hybridizationName, microarraySet);

			if (isCancelled
					&& CaARRAYPanel.cancelledConnectionInfo != null
					&& CaARRAYPanel.cancelledConnectionInfo
							.equalsIgnoreCase(currentConnectionInfo)) {
				return;
			}

			microarraySet.add(microarray);
			
			publishCaArraySuccessEvent(caArraySuccessEvent);
			number++;
		} // loop of all hybridizations

		microarraySet.setLabel(experimentName);
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
