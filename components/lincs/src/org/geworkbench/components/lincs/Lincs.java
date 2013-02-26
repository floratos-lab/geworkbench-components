/**
 * 
 */
package org.geworkbench.components.lincs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.service.lincs.LincsService;
import org.geworkbench.service.lincs.data.xsd.ComputationalData;
import org.geworkbench.service.lincs.data.xsd.ExperimentalData;;

/**
 * Java client for LINCS web services.
 * 
 * @author zji
 * 
 */
 
public class Lincs {

	private static final Log log = LogFactory.getLog(LincsInterface.class);
	private static final String DEL = "|";
	
	LincsService lincsService = null;;
	 
	
	public Lincs(String url, String username, String passwprd) {
		URL baseUrl;
		baseUrl = org.geworkbench.service.lincs.LincsService.class
				.getResource(".");
		try {
			baseUrl = new URL(baseUrl, url);
		} catch (MalformedURLException e) {			 
			log.warn("Failed to create URL for the wsdl Location: " + url +", retrying as a local file");
			log.warn(e.getMessage());
		}
		lincsService = new LincsService(baseUrl, new QName(
				"http://lincs.service.geworkbench.org", "LincsService"));
	}
	
     

	public List<String> getAllTissueNames() throws Exception {
		List<String> names = null;
	 
		names = lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllTissueNames();
	
		return names;

	}

	public List<String> getAllCellLineNamesForTissueTypes(List<String> tissueTypes) throws Exception {
		List<String> names = null;
	 
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCellLineNamesForTissueType(convertToString(tissueTypes));
	 
		return names;
	}

	public List<String> getAllAssayTypeNames()  throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllAssayTypeNames();
	 
		return names;

	}

	public List<String> getAllMeasurementTypeNames()  throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllMeasurementTypeNames();
		 
		return names;
	}

	public List<String> getALLSimilarAlgorithmNames()  throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getALLSimilarAlgorithmNames();
	 
		return names;

	}
	
	public List<String> GetCompound1NamesFromExperimental(List<String> tyssueTypes, List<String>cellLines) throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCompound1NamesFromExperimental(convertToString(tyssueTypes), convertToString(cellLines));

	 
		return names;
	}
	
	public List<String> GetCompound2NamesFromExperimental(List<String> tissueTypes, List<String>cellLines, List<String> drug1Names) throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCompound2NamesFromExperimental(convertToString(tissueTypes), convertToString(cellLines), convertToString(drug1Names)) ;
	 
		return names;
	}
	
	public List<String> getCompound1NamesFromComputational(List<String> tissueTypes, List<String>cellLines) throws Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint().getCompound1NamesFromComputational(convertToString(tissueTypes),convertToString(cellLines));
	 
		return names;
	}
	
	public List<String> getCompound2NamesFromComputational(List<String> tyssueTypes, List<String>cellLines, List<String> drug1Names) throws Exception {

		List<String> names = null;
		names = lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCompound2NamesFromComputational(convertToString(tyssueTypes), convertToString(cellLines), convertToString(drug1Names)) ;
	 
		return names;
	}
	
	
	
	
	public List<ExperimentalData> getExperimentalData(List<String>tissueTypes, List<String>cellLineNames, List<String>drug1Names, List<String>drug2Names, List<String>measurementTypes,  List<String>assayTypes) throws Exception {

		 
		return  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getExperimentalData(convertToString(tissueTypes), convertToString(cellLineNames), convertToString(drug1Names), convertToString(drug2Names), convertToString(measurementTypes), convertToString(assayTypes));
         
	}
	
	public List<ExperimentalData> getExperimentalData(List<String>tissueTypes, List<String>cellLineNames, List<String>drug1Names, List<String>drug2Names, List<String>measurmentTypes,  List<String>assayTypes, boolean onlyTitration, int rowLimit) throws Exception {

		 
		return  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getExperimentalDataWithLimit(convertToString(tissueTypes), convertToString(cellLineNames), convertToString(drug1Names), convertToString(drug2Names), convertToString(measurmentTypes), convertToString(assayTypes), onlyTitration, rowLimit);
         
	}
	
	
	public List<ComputationalData> getComputationalData(List<String>tissueTypes, List<String>cellLineNames, List<String>drug1Names, List<String>drug2Names, List<String>similarityAlgorithms) throws Exception {

		 
		return  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getComputationalData(convertToString(tissueTypes), convertToString(cellLineNames), convertToString(drug1Names), convertToString(drug2Names), convertToString(similarityAlgorithms));
         
	}
	
	public List<ComputationalData> getComputationalData(List<String>tissueTypes, List<String>cellLineNames, List<String>drug1Names, List<String>drug2Names, List<String>similarityAlgorithms, int rowLimit) throws Exception {

		return lincsService.getLincsServiceHttpSoap11Endpoint()
				.getComputationalDataWithLimit(convertToString(tissueTypes), convertToString(cellLineNames), convertToString(drug1Names), convertToString(drug2Names), convertToString(similarityAlgorithms), rowLimit);

	}
	
	private String convertToString(List<String> list)
	{
		String s = "";
	    if (list != null && list.size() > 0)
	    {
	    	for(int i=0; i<list.size()-1; i++)
	    	{
	    		if (list.get(i) != null && !list.get(i).trim().equals(""))
	    		{
	    			s = s + list.get(i).trim() + DEL;
	    		}
	    	}
	    	if (list.get(list.size()-1) != null && !list.get(list.size()-1).trim().equals(""));
	           s = s + list.get(list.size()-1);
	    }
	    
	    return s;
	}

}
