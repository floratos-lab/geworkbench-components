/**
 * 
 */
package org.geworkbench.components.geneontology2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geworkbench.bison.datastructure.biocollections.GoAnalysisResult;


/**
 * @author zji
 *
 */
// this class is intended to be immutable and created by factory. 
public class GoTermMatcher {
	private boolean match;
	
	private GoTermMatcher(int goTermId, int searchId) {
		if(goTermId==searchId) match = true;
		else match = false;
	}

	private GoTermMatcher(int goTermId, String searchName) {
		String name = GoAnalysisResult.getGoTermName(goTermId);
		if(name!=null && name.contains(searchName)) match = true;
		else match = false;
	}
	
	static GoTermMatcher createMatcher(GoTreeNode node, String searchText) {
		Pattern p = Pattern.compile( "\\d+" );
		Matcher m = p.matcher(searchText);
		if(m.matches()) {
			return new GoTermMatcher(node.goId, Integer.parseInt(searchText) );
		} else {
			return new GoTermMatcher(node.goId, searchText);
		}

	}
	
	public boolean match(){ return match; }
}
