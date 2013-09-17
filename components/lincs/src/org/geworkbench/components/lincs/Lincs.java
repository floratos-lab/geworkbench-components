/**
 * 
 */
package org.geworkbench.components.lincs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 
import org.geworkbench.service.lincs.LincsService;
import org.geworkbench.service.lincs.data.xsd.ComputationalData;
import org.geworkbench.service.lincs.data.xsd.ExperimentalData;
import org.geworkbench.service.lincs.data.xsd.FmoaData;
import org.geworkbench.service.lincs.data.xsd.CnkbInteractionData;
import org.geworkbench.service.lincs.data.xsd.GeneRank;
import org.geworkbench.service.lincs.data.xsd.InteractionType;
import org.geworkbench.service.lincs.data.xsd.TitrationCurveData;

import org.geworkbench.service.lincs.LincsServiceException_Exception;;

/**
 * Java client for LINCS web services.
 * 
 * @author zji
 * 
 */
 
public class Lincs {

	private static final Log log = LogFactory.getLog(Lincs.class);
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
	
     

	public List<String> getAllTissueNames() throws LincsServiceException_Exception {
		List<String> names = null;
	 
		names = lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllTissueNames();
	
		return names;

	}
	
	
	public List<String> getComputationaTissueNames() throws LincsServiceException_Exception {
		List<String> names = null;
	 
		names = lincsService.getLincsServiceHttpSoap11Endpoint()
				.getComputationaTissueNames();
	
		return names;

	}

	public List<String> getAllCellLineNamesForTissueTypes(List<String> tissueTypes) throws LincsServiceException_Exception {
		List<String> names = null;
	 
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCellLineNamesForTissueType(convertToString(tissueTypes));
	 
		return names;
	}

	public List<String> getAllAssayTypeNames()  throws LincsServiceException_Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllAssayTypeNames();
	 
		return names;

	}

	public List<String> getAllMeasurementTypeNames()  throws LincsServiceException_Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getAllMeasurementTypeNames();
		 
		return names;
	}

	public List<String> getALLSimilarAlgorithmNames()  throws LincsServiceException_Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getALLSimilarAlgorithmNames();
	 
		return names;

	}
	
	public List<String> getCompound1NamesFromExperimental(List<String> tyssueTypes, List<String>cellLines) throws LincsServiceException_Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCompound1NamesFromExperimental(convertToString(tyssueTypes), convertToString(cellLines));

	 
		return names;
	}
	
	public List<String> getCompound2NamesFromExperimental(List<String> tissueTypes, List<String>cellLines, List<String> compound1Names) throws LincsServiceException_Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCompound2NamesFromExperimental(convertToString(tissueTypes), convertToString(cellLines), convertToString(compound1Names)) ;
	 
		return names;
	}
	
	public List<String> getCompound1NamesFromComputational(List<String> tissueTypes, List<String>cellLines) throws LincsServiceException_Exception {

		List<String> names = null;
		names =  lincsService.getLincsServiceHttpSoap11Endpoint().getCompound1NamesFromComputational(convertToString(tissueTypes),convertToString(cellLines));
	 
		return names;
	}
	
	public List<String> getCompound2NamesFromComputational(List<String> tyssueTypes, List<String>cellLines, List<String> compound1Names) throws LincsServiceException_Exception {

		List<String> names = null;
		names = lincsService.getLincsServiceHttpSoap11Endpoint()
				.getCompound2NamesFromComputational(convertToString(tyssueTypes), convertToString(cellLines), convertToString(compound1Names)) ;
	 
		return names;
	}
	
	
	
	
	public List<ExperimentalData> getExperimentalData(List<String>tissueTypes, List<String>cellLineNames, List<String>compound1Names, List<String>compound2Names, List<String>measurementTypes,  List<String>assayTypes) throws LincsServiceException_Exception {

		 
		return  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getExperimentalData(convertToString(tissueTypes), convertToString(cellLineNames), convertToString(compound1Names), convertToString(compound2Names), convertToString(measurementTypes), convertToString(assayTypes));
         
	}
	
	public List<ExperimentalData> getExperimentalData(List<String>tissueTypes, List<String>cellLineNames, List<String>compound1Names, List<String>compound2Names, List<String>measurmentTypes,  List<String>assayTypes, boolean onlyTitration, int rowLimit) throws LincsServiceException_Exception {

		 
		return  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getExperimentalDataWithLimit(convertToString(tissueTypes), convertToString(cellLineNames), convertToString(compound1Names), convertToString(compound2Names), convertToString(measurmentTypes), convertToString(assayTypes), onlyTitration, rowLimit);
         
	}
	
	
	public List<ComputationalData> getComputationalData(List<String>tissueTypes, List<String>cellLineNames, List<String>compound1Names, List<String>compound2Names, List<String>similarityAlgorithms) throws LincsServiceException_Exception {

		 
		return  lincsService.getLincsServiceHttpSoap11Endpoint()
				.getComputationalData(convertToString(tissueTypes), convertToString(cellLineNames), convertToString(compound1Names), convertToString(compound2Names), convertToString(similarityAlgorithms));
         
	}
	
	public List<ComputationalData> getComputationalData(List<String>tissueTypes, List<String>cellLineNames, List<String>compound1Names, List<String>compound2Names, List<String>similarityAlgorithms, int rowLimit) throws LincsServiceException_Exception {

		return lincsService.getLincsServiceHttpSoap11Endpoint()
				.getComputationalDataWithLimit(convertToString(tissueTypes), convertToString(cellLineNames), convertToString(compound1Names), convertToString(compound2Names), convertToString(similarityAlgorithms), rowLimit);

	}
	
	public CnkbInteractionData getInteractionData(long geneId, String geneSymbol, long interactomeVersionId) throws LincsServiceException_Exception {

		return lincsService.getLincsServiceHttpSoap11Endpoint().
		     getInteractionData(geneId, geneSymbol, interactomeVersionId);				 
	}
	
	public TitrationCurveData getTitrationCurveData(long titrationId) throws LincsServiceException_Exception {

		return lincsService.getLincsServiceHttpSoap11Endpoint().
		     getTitrationCurveData(titrationId);				 
	}
	
	public List<GeneRank> getGeneRankData(String geneIds, long compoundId, long differentialExpressionRunId) throws LincsServiceException_Exception {

		return lincsService.getLincsServiceHttpSoap11Endpoint()
		   .getGeneRankData(geneIds, compoundId, differentialExpressionRunId);
		     			 
	}
	
	public HashMap<String, String> getInteractionTypeMap() throws LincsServiceException_Exception {
		HashMap<String, String> map = new HashMap<String, String>();
		List<InteractionType> dataList = lincsService.getLincsServiceHttpSoap11Endpoint().getInteractionTypes();
		for(InteractionType data : dataList)
		{
			map.put(data.getInteractionType(), data.getShortName());
			map.put(data.getShortName(), data.getInteractionType());
		}
		return map;
	}
	
	public FmoaData getFmoaData(String compoundName, long fmoaId) throws LincsServiceException_Exception {

		return lincsService.getLincsServiceHttpSoap11Endpoint()
				.getFmoaData(compoundName, fmoaId);

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
