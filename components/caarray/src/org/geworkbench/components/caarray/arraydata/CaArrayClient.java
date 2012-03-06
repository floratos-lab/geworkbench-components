package org.geworkbench.components.caarray.arraydata;

import gov.nih.nci.caarray.external.v1_0.CaArrayEntityReference;
import gov.nih.nci.caarray.external.v1_0.array.ArrayProvider;
import gov.nih.nci.caarray.external.v1_0.data.DataSet;
import gov.nih.nci.caarray.external.v1_0.data.FileCategory;
import gov.nih.nci.caarray.external.v1_0.data.QuantitationType;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.builtin.projects.remoteresources.CaArrayFilteringDialog;
import org.geworkbench.builtin.projects.remoteresources.carraydata.CaArray2Experiment;

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
		String[] types = CaArrayFilteringDialog.listContent;

		TreeMap<String, Set<String>> tree = new TreeMap<String, Set<String>>();

		for (String type : types) {
			TreeSet<String> values = new TreeSet<String>();
			if (type.equalsIgnoreCase(CaArrayFilteringDialog.ORGANISM)) {
				List<Organism> organisms = lookupOrganisms();
				for (Organism o : organisms) {
					String commonName = o.getCommonName();
					if(commonName!=null) {
						values.add( commonName );
					}
				}
			} else if (type.equalsIgnoreCase(CaArrayFilteringDialog.PINAME)) {
				List<Person> pis = searchService.getAllPrincipalInvestigators();
				for (Person p : pis) {
					values.add( p.getLastName() + NAME_SEPARATOR
							+ p.getFirstName() );
				}
			} else if (type.equalsIgnoreCase(CaArrayFilteringDialog.CHIPPROVIDER)) {
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
		if (filterkey.equalsIgnoreCase(CaArrayFilteringDialog.ORGANISM)) {
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
		} else if (filterkey.equalsIgnoreCase(CaArrayFilteringDialog.PINAME)) {
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
		} else if (filterkey.equalsIgnoreCase(CaArrayFilteringDialog.CHIPPROVIDER)) {
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
						.getDescription(), e.getPublicIdentifier()));
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
	 * Get caArray dataset from caArray server with defined
	 * Hybridization and QuantitationType.
	 *
	 */
	DataSet getCaArrayDataSet(String hybridizationName,
			String hybridizationId, String quantitationType)
			throws Exception {

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

        return dataService.getDataSet(dataSetRequest);
	}

}
