/**
 * 
 */
package org.geworkbench.components.lincs;

import java.util.List;

import org.geworkbench.service.lincs.LincsService;
import org.geworkbench.service.lincs.data.xsd.ComputationalData;
import org.geworkbench.service.lincs.data.xsd.ExperimentalData;;

/**
 * Java client for LINCS web services.
 * 
 * @author zji
 * 
 */
// TODO for now, all the methods are basically place holders. They are supposed
// to be replaced by actual calling to web services.
public class Lincs {

	LincsService lincsService = new LincsService();

	public Lincs(String url, String username, String passwprd) {

	}

	List<String> getAllTissueNames() throws Exception {
		List<String> names = null;
	 
		names = lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllTissueNames();
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		 
		return names;

	}

	List<String> getAllCellLineNamesForTissueTypes(List<String> tissueTypes) throws Exception {
		List<String> names = null;
	 
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCellLineNamesForTissueType(tissueTypes);
		if (names != null && names.size() > 0)
			   names.add(0, "All");
	 
		return names;
	}

	List<String> getAllAssayTypeNames()  throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllAssayTypeNames();
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;

	}

	List<String> getAllMeasurementTypeNames()  throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllMeasurementTypeNames();
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;
	}

	List<String> getALLSimilarAlgorithmNames()  throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getALLSimilarAlgorithmNames();
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;

	}
	
	List<String> GetDrug1NamesFromExperimental(List<String> tyssueTypes, List<String>cellLines) throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCompound1NamesFromExperimental(tyssueTypes, cellLines);

		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;
	}
	
	List<String> GetDrug2NamesFromExperimental(List<String> tissueTypes, List<String>cellLines, List<String> drug1Names) throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCompound2NamesFromExperimental(tissueTypes, cellLines, drug1Names) ;
		if (names != null && names.size() > 0)
		   names.add(0, "All");
		return names;
	}
	
	List<String> getDrug1NamesFromComputational(List<String> tissueTypes, List<String>cellLines) throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint().getCompound1NamesFromComputational(tissueTypes, cellLines);
			 
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;
	}
	
	List<String> getDrug2NamesFromComputational(List<String> tyssueTypes, List<String>cellLines, List<String> drug1Names) throws Exception {

		List<String> names = null;
		names = lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCompound2NamesFromComputational(tyssueTypes, cellLines, drug1Names) ;
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;
	}
	
	
	
	
	List<ExperimentalData> getExperimentalData(List<String>tissueTypes, List<String>cellLineNames, List<String>drug1Names, List<String>drug2Names, List<String>measurementTypes,  List<String>assayTypes) throws Exception {

		 
		return  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getExperimentalData(tissueTypes, cellLineNames, drug1Names, drug2Names, measurementTypes, assayTypes);
         
	}
	
	List<ExperimentalData> getExperimentalData(List<String>tissueTypes, List<String>cellLineNames, List<String>drug1Names, List<String>drug2Names, List<String>measurmentTypes,  List<String>assayTypes, boolean onlyTitration, int rowLimit) throws Exception {

		 
		return  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getExperimentalDataWithLimit(tissueTypes, cellLineNames, drug1Names, drug2Names, measurmentTypes, assayTypes, onlyTitration, rowLimit);
         
	}
	
	
	List<ComputationalData> getComputationalData(List<String>tissueTypes, List<String>cellLineNames, List<String>drug1Names, List<String>drug2Names, List<String>similarityAlgorithms) throws Exception {

		 
		return  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getComputationalData(tissueTypes, cellLineNames, drug1Names, drug2Names, similarityAlgorithms);
         
	}
	
	List<ComputationalData> getComputationalData(List<String>tissueTypes, List<String>cellLineNames, List<String>drug1Names, List<String>drug2Names, List<String>similarityAlgorithms, int rowLimit) throws Exception {

		return lincsService.getLincsServiceHttpSoap11Endpoint()
				.getComputationalDataWithLimit(tissueTypes, cellLineNames, drug1Names, drug2Names, similarityAlgorithms, rowLimit);

	}

}
