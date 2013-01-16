/**
 * 
 */
package org.geworkbench.components.lincs;

/**
 * Java client for LINCS web services.
 * 
 * @author zji
 *
 */
// TODO for now, all the methods are basically place holders. They are supposed to be replaced by actual calling to web services.
public class Lincs {
	
	public Lincs(String url, String username, String passwprd) {
		
	}
	
	String[] getAllTissueNames() {
		return new String[]{"tissue 1", "tissue 2"};
	}

	String[] getAllCellLineNamesForTissueType(String tissueType) {
		if(tissueType.equals("tissue 1")) {
			return new String[]{"cell line 1", "cell line 2"};
		} else if(tissueType.equals("tissue 2")) {
			return new String[]{"cell line a", "cell line b"};
		} else {
			return new String[]{"cell line x", "cell line y"};
		}
	}
}
