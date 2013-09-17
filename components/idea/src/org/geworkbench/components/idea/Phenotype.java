package org.geworkbench.components.idea;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;

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
	private Set<Integer> columnIncluded;	
	private Set<Integer> columnExcluded;
	
	private String[] phenotypeAsString=new String[2];
	private DSMicroarraySet maSet=null;
	
	public Phenotype(){
		
	}

	/**
	 * Constructor from given column indices included. User for null distribution only.
	 * @param nullPhenoCols 
	 */
	public Phenotype(Set<Integer> nullPhenoCols)  {
		columnExcluded = new HashSet<Integer>();
		columnIncluded = nullPhenoCols;
	}

	/**
	 * Check if this column number belongs to the 'phenotype' set.
	 * The index is based on the set that excludes the 'excluded' columns.
	 * 
	 */
	boolean isIncluded(int columnIndexAfterExclusion) {
		if(columnIncluded.contains(columnIndexAfterExclusion))return true;
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
		return columnIncluded.size();
	}

	public int getExcludedCount() {
		return columnExcluded.size();
	}
	public String[] getPhenotypeAsString(){
		phenotypeAsString[INCLUDE_LINE_NUMBER]="Include"+"\t";
		phenotypeAsString[EXCLUDE_LINE_NUMBER]="Exclude"+"\t";
		for(int i:columnIncluded){
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
	
	public void setMicroarraySet(DSMicroarraySet maSet){
		this.maSet = maSet;
	}
	
	public String getPhenotypeInArrayNames(){
		String s="Phenotype:"+"\n";
		ArrayList<Integer> list=new ArrayList<Integer>(columnIncluded);
		Collections.sort(list);
		for(int i=0;i<list.size();i++){
			s+=maSet.get((Integer)list.get(i)-1)+"\t";
		}		
		s=s.substring(0,s.length()-1);
		return s;
	}
	
	public String getExcludeInArrayNames(){
		String s="Exclude:"+"\n";
		ArrayList<Integer> list=new ArrayList<Integer>(columnExcluded);
		Collections.sort(list);
		for(int i=0;i<list.size();i++){
			s+=maSet.get((Integer)list.get(i)-1)+"\t";
		}		
		s=s.substring(0,s.length()-1);
		return s;
	}
	
	
	public void setIncludeList(Set<Integer> includeSet){
		this.columnIncluded=includeSet;		
	}
	
	public Set<Integer> getIncludeList(){
		return columnIncluded;
	}
	
	public void setExcludeList(Set<Integer> excludeSet){
		this.columnExcluded=excludeSet;		
	}
	public Set<Integer> getExcludeList(){
		return columnExcluded;
	}
	
}
