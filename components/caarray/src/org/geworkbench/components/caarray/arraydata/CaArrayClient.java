package org.geworkbench.components.caarray.arraydata;

import gov.nih.nci.caarray.external.v1_0.CaArrayEntityReference;
import gov.nih.nci.caarray.external.v1_0.array.ArrayProvider;
import gov.nih.nci.caarray.external.v1_0.data.AbstractDataColumn;
import gov.nih.nci.caarray.external.v1_0.data.DataSet;
import gov.nih.nci.caarray.external.v1_0.data.DataType;
import gov.nih.nci.caarray.external.v1_0.data.DesignElement;
import gov.nih.nci.caarray.external.v1_0.data.DoubleColumn;
import gov.nih.nci.caarray.external.v1_0.data.FileCategory;
import gov.nih.nci.caarray.external.v1_0.data.FloatColumn;
import gov.nih.nci.caarray.external.v1_0.data.HybridizationData;
import gov.nih.nci.caarray.external.v1_0.data.IntegerColumn;
import gov.nih.nci.caarray.external.v1_0.data.LongColumn;
import gov.nih.nci.caarray.external.v1_0.data.QuantitationType;
import gov.nih.nci.caarray.external.v1_0.data.ShortColumn;
import gov.nih.nci.caarray.external.v1_0.experiment.Experiment;
import gov.nih.nci.caarray.external.v1_0.experiment.Organism;
import gov.nih.nci.caarray.external.v1_0.experiment.Person;
import gov.nih.nci.caarray.external.v1_0.query.DataSetRequest;
import gov.nih.nci.caarray.external.v1_0.query.ExampleSearchCriteria;
import gov.nih.nci.caarray.external.v1_0.query.ExperimentSearchCriteria;
import gov.nih.nci.caarray.external.v1_0.query.HybridizationSearchCriteria;
import gov.nih.nci.caarray.external.v1_0.query.QuantitationTypeSearchCriteria;
import gov.nih.nci.caarray.external.v1_0.sample.Hybridization;
import gov.nih.nci.caarray.services.ServerConnectionException;
import gov.nih.nci.caarray.services.external.v1_0.CaArrayServer;
import gov.nih.nci.caarray.services.external.v1_0.InvalidInputException;
import gov.nih.nci.caarray.services.external.v1_0.data.DataService;
import gov.nih.nci.caarray.services.external.v1_0.search.JavaSearchApiUtils;
import gov.nih.nci.caarray.services.external.v1_0.search.SearchApiUtils;
import gov.nih.nci.caarray.services.external.v1_0.search.SearchService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

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
import org.geworkbench.builtin.projects.remoteresources.query.CaARRAYQueryPanel;

/**
 * The class to invoke StandAloneCaArrayWrapper
 *
 * @author xiaoqing
 * @author zji
 * @version $Id: StandAloneCaArrayClientWrapper.java,v 1.12 2009/08/05 21:08:49
 *          jiz Exp $
 *
 */
public class CaArrayClient {
	private Log log = null;

	private static final String NAME_SEPARATOR = ", ";

	private CaArrayServer server = null;
    private SearchService searchService = null;
    private SearchApiUtils searchServiceHelper = null;
	private DataService dataService = null;

	CaArrayClient(String url,
			int port, String username,
			String password) throws ServerConnectionException, FailedLoginException {
		server = new CaArrayServer(url, port);

		ClassLoader originalContextClassLoader = Thread.currentThread()
				.getContextClassLoader();
		Thread.currentThread().setContextClassLoader(
				server.getClass().getClassLoader());
		log = LogFactory.getLog(CaArrayClient.class);

		if (username == null || username.trim().length() == 0) {
			server.connect();// enable a user login.
		} else {
			server.connect(username, password);
		}
		searchService = server.getSearchService();
		searchServiceHelper = new JavaSearchApiUtils(searchService);
		dataService = server.getDataService();
		Thread.currentThread()
				.setContextClassLoader(originalContextClassLoader);
	}

    private List<Organism> lookupOrganisms() throws InvalidInputException {
        ExampleSearchCriteria<Organism> criteria = new ExampleSearchCriteria<Organism>();
        Organism exampleOrganism = new Organism();
        criteria.setExample(exampleOrganism);
        List<Organism> organisms = searchServiceHelper.byExample(criteria).list();
        return organisms;
    }

    private List<ArrayProvider> lookupArrayProviders() throws InvalidInputException {
        ExampleSearchCriteria<ArrayProvider> criteria = new ExampleSearchCriteria<ArrayProvider>();
        ArrayProvider exampleProvider = new ArrayProvider();
        criteria.setExample(exampleProvider);
        List<ArrayProvider> arrayProviders = searchServiceHelper.byExample(criteria).list();
        return arrayProviders;
    }


	/**
	 * The method to query caArray server to return valid values for all filter types. For
	 * example, return all valid Organisms in caArray.
	 *
	 * @param service
	 * @param request
	 * @param type
	 * @return
	 * @throws RemoteException
	 * @throws InvalidInputException
	 */
	TreeMap<String, Set<String>> lookupTypeValues() throws RemoteException, InvalidInputException
			 {
		String[] types = CaARRAYQueryPanel.listContent;

		TreeMap<String, Set<String>> tree = new TreeMap<String, Set<String>>();

		for (String type : types) {
			TreeSet<String> values = new TreeSet<String>();
			if (type.equalsIgnoreCase(CaARRAYQueryPanel.ORGANISM)) {
				List<Organism> organisms = lookupOrganisms();
				for (Organism o : organisms) {
					String commonName = o.getCommonName();
					if(commonName!=null) {
						values.add( commonName );
					}
				}
			} else if (type.equalsIgnoreCase(CaARRAYQueryPanel.PINAME)) {
				List<Person> pis = searchService.getAllPrincipalInvestigators();
				for (Person p : pis) {
					values.add( p.getLastName() + NAME_SEPARATOR
							+ p.getFirstName() );
				}
			} else if (type.equalsIgnoreCase(CaARRAYQueryPanel.CHIPPROVIDER)) {
				List<ArrayProvider> providers = lookupArrayProviders();
				for (ArrayProvider p : providers) {
					values.add( p.getName() );
				}
			}

			if(values.size()>0)
				tree.put(type, values);
		}

		return tree;
	}

	CaArray2Experiment[] getExperimentListWithFilter(String filterkey, String filtervalue) throws Exception {

		Vector<CaArray2Experiment> exps = new Vector<CaArray2Experiment>();

		ExperimentSearchCriteria experimentSearchCriteria = new ExperimentSearchCriteria();

		if(filterkey!=null) {
		if (filterkey.equalsIgnoreCase(CaARRAYQueryPanel.ORGANISM)) {
	        ExampleSearchCriteria<Organism> organismCriteria = new ExampleSearchCriteria<Organism>();
	        Organism exampleOrganism = new Organism();
	        exampleOrganism.setCommonName(filtervalue);
	        organismCriteria.setExample(exampleOrganism);
	        List<Organism> organisms = searchService.searchByExample(organismCriteria, null).getResults();
	        if (organisms == null || organisms.size() <= 0) {
	            System.err.println("Could not find organism with common name = " + filtervalue);
	        } else {
		        CaArrayEntityReference organismRef = organisms.get(0).getReference();
		        experimentSearchCriteria.setOrganism(organismRef);
	        }
		} else if (filterkey.equalsIgnoreCase(CaARRAYQueryPanel.PINAME)) {
			String[] name = filtervalue.split(NAME_SEPARATOR);
	        // Select principal investigator by last name
	        List<Person> investigators = searchService.getAllPrincipalInvestigators();
	        for (Person investigator : investigators) {
	            if (name[0].equalsIgnoreCase(investigator.getLastName()) && name[1].equalsIgnoreCase(investigator.getFirstName())) {
	                Set<CaArrayEntityReference> principalInvestigators = new TreeSet<CaArrayEntityReference>();
	                principalInvestigators.add(investigator.getReference());
					experimentSearchCriteria.setPrincipalInvestigators(principalInvestigators);
	                // RC3 version is as following
	                //experimentSearchCriteria.setPrincipalInvestigators(.setPrincipalInvestigator(investigator.getReference());
	                break;
	            }
	        }
		} else if (filterkey.equalsIgnoreCase(CaARRAYQueryPanel.CHIPPROVIDER)) {
	        // Select array provider. (See LookUpEntities example client to see how to get list of all array providers.)
	        ExampleSearchCriteria<ArrayProvider> providerCriteria = new ExampleSearchCriteria<ArrayProvider>();
	        ArrayProvider exampleProvider = new ArrayProvider();
	        exampleProvider.setName(filtervalue);
	        providerCriteria.setExample(exampleProvider);
	        List<ArrayProvider> arrayProviders = searchService.searchByExample(providerCriteria, null).getResults();
	        if (arrayProviders == null || arrayProviders.size() <= 0) {
	            System.err.println("Could not find array provider called " + filtervalue);
	        } else {
		        CaArrayEntityReference providerRef = arrayProviders.get(0).getReference();
		        experimentSearchCriteria.setArrayProvider(providerRef);
	        }
		} else { // other filtering never implemented
			return null;
		}
		}


        List<Experiment> experiments = (searchServiceHelper.experimentsByCriteria(experimentSearchCriteria)).list();

		for (Experiment e : experiments) {
			CaArrayEntityReference experimentRef = e.getReference();
			exps.add(new CaArray2Experiment(experimentRef.getId(), e.getTitle(), e
						.getDescription()));
		}

		CaArray2Experiment[] experimentsArray = new CaArray2Experiment[exps.size()];
		exps.toArray(experimentsArray);

		log.debug("Ending the lookupExperiments " + new Date());
		return experimentsArray;
	}

	/**
	 * This method fills hybridization info for the given CaArray2Experiment.
	 * Even in the case of zero hybridization, hybridization list should be set as zero size, not null.
	 *
	 * @param caArray2Experiment
	 * @throws InvalidInputException
	 */
	// RC3 version is as following
	//void getHybridizations(CaArray2Experiment caArray2Experiment)
	void getHybridizations(CaArray2Experiment caArray2Experiment)
			throws InvalidInputException {
		String experimentReferenceId = caArray2Experiment.getExperimentReferenceId();
		CaArrayEntityReference experimentRef = new CaArrayEntityReference(experimentReferenceId);

		HybridizationSearchCriteria searchCriteria = new HybridizationSearchCriteria();
		searchCriteria.setExperiment(experimentRef);
		List<Hybridization> hybridizations = (searchServiceHelper
				.hybridizationsByCriteria(searchCriteria)).list();

		Map<String, String> hybridizationIds = new HashMap<String, String>();
		caArray2Experiment.setHybridizations(hybridizationIds);

		if (hybridizations != null && hybridizations.size() > 0) {

			for (Hybridization h : hybridizations) {
				hybridizationIds.put(h.getName(), h.getId());
			}

			List<QuantitationType> quantitationTypes = getQuantitationTypes(hybridizations
					.get(0));
			String[] qTypes = new String[quantitationTypes.size()];
			int i = 0;
			for (QuantitationType q : quantitationTypes) {
				qTypes[i++] = q.getName();
			}
			caArray2Experiment.setQuantitationTypes(qTypes);
		}
	}

	/**
	 *
	 * get all quantitations type for a hybridization
	 * @throws InvalidInputException
	 */
	// RC3 version is as following
	//private List<QuantitationType> getQuantitationTypes(Hybridization hybridization) throws InvalidReferenceException{
	private List<QuantitationType> getQuantitationTypes(Hybridization hybridization) throws InvalidInputException{
        QuantitationTypeSearchCriteria qtCrit = new QuantitationTypeSearchCriteria();
        qtCrit.setHybridization(hybridization.getReference());
        return searchService.searchForQuantitationTypes(qtCrit);
	}

	/**
	 * The method to grab the data from caArray server with defined
	 * Hybridization and QuantitationType. A BISON DataType will be returned.
	 *
	 */
	CSExprMicroarraySet getDataSet(String hybridizationName,
			String hybridizationId, String quantitationType, String chipType)
			throws Exception {

		MarkerValuePair[] markerValuePairs = null;

		DataSetRequest dataSetRequest = new DataSetRequest();
		Set<CaArrayEntityReference> hybridizationRefs = new HashSet<CaArrayEntityReference>();
		Hybridization hybridization = new Hybridization();
		hybridization.setId(hybridizationId);
		hybridizationRefs.add(hybridization.getReference());
        dataSetRequest.setHybridizations(hybridizationRefs);

        // Select the quantitation types (columns) of interest.
        QuantitationTypeSearchCriteria qtCrit = new QuantitationTypeSearchCriteria();
        qtCrit.setHybridization(hybridization.getReference());
        qtCrit.getFileCategories().add(FileCategory.DERIVED_DATA);
        List<QuantitationType> qtypes = searchService.searchForQuantitationTypes(qtCrit);
        Set<CaArrayEntityReference> quantitationTypeRefs = new HashSet<CaArrayEntityReference>();
        for (QuantitationType qt : qtypes) {
            quantitationTypeRefs.add(qt.getReference());
        }

        if (quantitationTypeRefs.isEmpty()) {
            System.err.println("Could not find one or more of the requested quantitation types... ");
            return null;
        }
        dataSetRequest.setQuantitationTypes(quantitationTypeRefs);

        DataSet dataSet = dataService.getDataSet(dataSetRequest);

        // Ordered list of row headers (probe sets)
        List<DesignElement> probeSets = dataSet.getDesignElements();
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

        markerValuePairs = new MarkerValuePair[probeSets.size()];
		double[] doubleValues = new double[probeSets.size()];

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

        switch (columnDataType) {
            case INTEGER:
                int[] intValues = ((IntegerColumn) dataColumn).getValues();
        		for (int i = 0; i < doubleValues.length; i++) doubleValues[i] = intValues[i];
                break;
            case DOUBLE:
                doubleValues = ((DoubleColumn) dataColumn).getValues();
                break;
            case FLOAT:
                float[] floatValues = ((FloatColumn) dataColumn).getValues();
        		for (int i = 0; i < doubleValues.length; i++) doubleValues[i] = floatValues[i];
                break;
            case SHORT:
                short[] shortValues = ((ShortColumn) dataColumn).getValues();
        		for (int i = 0; i < doubleValues.length; i++) doubleValues[i] = shortValues[i];
                break;
            case LONG:
                long[] longValues = ((LongColumn) dataColumn).getValues();
        		for (int i = 0; i < doubleValues.length; i++) doubleValues[i] = longValues[i];
                break;
            case BOOLEAN:
            case STRING:
            default:
                // Should never get here.
            	log.error("Type "+columnDataType + " not expected.");
        }

		for (int i = 0; i < doubleValues.length; i++) {
				markerValuePairs[i] = new MarkerValuePair(
						probeSets.get(i).getName(), doubleValues[i]);
		}

		return processDataToBISON(markerValuePairs, hybridizationName, chipType);
	}

	/**
	 * Translate the data file into BISON type.
	 *
	 */
	private CSExprMicroarraySet processDataToBISON(
			MarkerValuePair[] pairs, String name, String chipType) {

		List<String> markerNames = new ArrayList<String>();
		List<Double> valuesList = new ArrayList<Double>();

		for (int i = 0; i < pairs.length; i++) {
			MarkerValuePair p = pairs[i];
			markerNames.add(p.marker);
			valuesList.add(new Double(p.value));
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
//					bug 1956 geneName will be correctly initialized before usage, lazy initialization
					marker.setGeneName(null);
					marker.setDisPlayType(DSGeneMarker.AFFY_TYPE);
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
		maSet.setAnnotationFileName(AnnotationParser.getLastAnnotationFileName());
		AnnotationParser.setChipType(maSet, chipType);
		maSet.sortMarkers(markerNo);

		microarray = new CSMicroarray(0, markerNo, name, null, null, false,
				DSMicroarraySet.geneExpType);
		microarray.setLabel(name);
		for (int i = 0; i < markerNo; i++) {
			DSMutableMarkerValue m = (DSMutableMarkerValue) microarray.getMarkerValue(maSet.newid[i]);
			m.setValue(valuesList.get(i));
			m.setMissing(false);
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

	private static class MarkerValuePair {
		public MarkerValuePair(String marker, double value) {
			this.marker = marker;
			this.value = value;
		}

		String marker;
		double value;
	}
}
