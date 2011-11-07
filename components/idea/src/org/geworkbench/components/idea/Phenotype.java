package org.geworkbench.components.idea;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Data structure to hold the phenotype information from parameter panel. 
 * 
 * @author zji
 * @version $Id$
 *
 */
public class Phenotype implements Serializable {
	private static final long serialVersionUID = 1929271049658752446L;

	private static final int INCLUDE_LINE_NUMBER=0;
	private static final int EXCLUDE_LINE_NUMBER=1;
	private Set<Integer> newColumnIncluded;	
	private Set<Integer> columnExcluded;
	
	private String[] phenotypeAsString=new String[2];
	
	public Phenotype(){
		
	}

	/**
	 * Constructor from given column indices included. User for null distribution only.
	 * @param nullPhenoCols 
	 */
	public Phenotype(Set<Integer> nullPhenoCols)  {
		columnExcluded = new HashSet<Integer>();
		newColumnIncluded = nullPhenoCols;
	}

	/**
	 * Check if this column number belongs to the 'phenotype' set.
	 * The index is based on the set that excludes the 'excluded' columns.
	 * 
	 */
	boolean isIncluded(int columnIndexAfterExclusion) {
		if(newColumnIncluded.contains(columnIndexAfterExclusion))return true;
		else return false;
	}

	/**
	 * Check if this column number belongs to the excluded columne set.
	 * The index is based on the original complete dataset.
	 * 
	 */
	boolean isExcluded(int columnIndexInOriginalDataset) {
		if(columnExcluded.contains(columnIndexInOriginalDataset))return true;
		else return false;
	}

	public int getIncludedCount() {
		return newColumnIncluded.size();
	}

	public int getExcludedCount() {
		return columnExcluded.size();
	}
	public String[] getPhenotypeAsString(){
		phenotypeAsString[INCLUDE_LINE_NUMBER]="Include"+"\t";
		phenotypeAsString[EXCLUDE_LINE_NUMBER]="Exclude"+"\t";
		for(int i:newColumnIncluded){
			phenotypeAsString[INCLUDE_LINE_NUMBER]+=Integer.toString(i)+"\t";
		}
		String s=phenotypeAsString[INCLUDE_LINE_NUMBER].substring(0,phenotypeAsString[INCLUDE_LINE_NUMBER].length()-1);
		phenotypeAsString[INCLUDE_LINE_NUMBER]=s;	//remove the last one which is tab
		
		for(int i:columnExcluded){
			phenotypeAsString[EXCLUDE_LINE_NUMBER]+=Integer.toString(i)+"\t";
		}
		s=phenotypeAsString[EXCLUDE_LINE_NUMBER].substring(0,phenotypeAsString[EXCLUDE_LINE_NUMBER].length()-1);
		phenotypeAsString[EXCLUDE_LINE_NUMBER]=s;	//remove the last one
		
		return phenotypeAsString;
	}
	public void setIncludeList(Set<Integer> includeSet){
		this.newColumnIncluded=includeSet;		
	}
	
	public Set<Integer> getIncludeList(){
		return newColumnIncluded;
	}
	
	public void setExcludeList(Set<Integer> excludeSet){
		this.columnExcluded=excludeSet;		
	}
	public Set<Integer> getExcludeList(){
		return columnExcluded;
	}
	
}
