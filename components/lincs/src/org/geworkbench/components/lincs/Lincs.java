/**
 * 
 */
package org.geworkbench.components.lincs;

import java.util.List;

import org.geworkbench.service.lincs.*;
import org.geworkbench.service.lincs.data.xsd.ComputationalData;

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

	List<String> getAllTissueNames() {
		List<String> names = null;
		names = lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllTissueNames();
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;

	}

	List<String> getAllCellLineNamesForTissueTypes(List<String> tissueTypes) {
		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCellLineNamesForTissueType(tissueTypes);
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;
	}

	List<String> getAllAssayTypeNames() {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllAssayTypeNames();
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;

	}

	List<String> getAllMeasurementTypeNames() {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllMeasurementTypeNames();
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;
	}

	List<String> getALLSimilarAlgorithmNames() {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getALLSimilarAlgorithmNames();
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;

	}
	
	List<String> GetDrug1NamesFromExperimental(List<String> tyssueTypes, List<String>cellLines) {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getDrug1NamesFromExperimental(tyssueTypes, cellLines);

		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;
	}
	
	List<String> GetDrug2NamesFromExperimental(List<String> tyssueTypes, List<String>cellLines, List<String> drug1Names) {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getDrug2NamesFromExperimental(tyssueTypes, cellLines, drug1Names) ;
		if (names != null && names.size() > 0)
		   names.add(0, "All");
		return names;
	}
	
	List<String> getDrug1NamesFromComputational(List<String> tyssueTypes, List<String>cellLines) {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getDrug1NamesFromComputational(tyssueTypes, cellLines);
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;
	}
	
	List<String> getDrug2NamesFromComputational(List<String> tyssueTypes, List<String>cellLines, List<String> drug1Names) {

		List<String> names = null;
		names = lincsService.getLincsServiceHttpSoap11Endpoint()
				.getDrug2NamesFromComputational(tyssueTypes, cellLines, drug1Names) ;
		if (names != null && names.size() > 0)
			   names.add(0, "All");
		return names;
	}
	
	List<ComputationalData> getComputationalData(List<String>tissueTypes, List<String>cellLineNames, List<String>drug1Names, List<String>drug2Names, List<String>similarityAlgorithms) {

		 
		return  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getComputationalData(tissueTypes, cellLineNames, drug1Names, drug2Names, similarityAlgorithms);
         
	}
	
	List<ComputationalData> getComputationalDataWithLimit(List<String>tissueTypes, List<String>cellLineNames, List<String>drug1Names, List<String>drug2Names, List<String>similarityAlgorithms, int rowLimit) {

		return lincsService.getLincsServiceHttpSoap11Endpoint()
				.getComputationalDataWithLimit(tissueTypes, cellLineNames, drug1Names, drug2Names, similarityAlgorithms, rowLimit);

	}

}
