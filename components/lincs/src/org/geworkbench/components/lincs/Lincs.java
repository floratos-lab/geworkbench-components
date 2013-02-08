/**
 * 
 */
package org.geworkbench.components.lincs;
import java.util.List;  
import javax.xml.ws.Response;

import org.geworkbench.service.lincs.*; 


/**
 * Java client for LINCS web services.
 * 
 * @author zji
 *
 */
// TODO for now, all the methods are basically place holders. They are supposed to be replaced by actual calling to web services.
public class Lincs {
	
	LincsService lincsService = new LincsService();
	 
	public Lincs(String url, String username, String passwprd) {
		
	}
	
	List<String> getAllTissueNames() {
		
	   return lincsService.getLincsServiceHttpSoap11Endpoint().getAllTissueNames();
				 
	}

	List<String> getAllCellLineNamesForTissueTypes(List<String> tissueTypes) {
		 return lincsService.getLincsServiceHttpSoap11Endpoint().getCellLineNamesForTissueType(tissueTypes);
	}
	
	
	List<String> getAllAssayTypeNames() {
		
		   return lincsService.getLincsServiceHttpSoap11Endpoint().getAllAssayTypeNames();
					 
    }
	
	List<String> getAllMeasurementTypeNames() {
		
		   return lincsService.getLincsServiceHttpSoap11Endpoint().getAllMeasurementTypeNames();
					 
 }
	
	
}
