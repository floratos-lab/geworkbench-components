package org.geworkbench.components.idea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
	
	public Phenotype(File file) throws IOException {
		int maxIndex = 0;
		
		BufferedReader br = new BufferedReader(new FileReader(file));		
		String line = br.readLine();	//first line is Include phenotype
		//phenotypeAsString[0]=line;
		String[] tokens = line.split("\\s");
		Set<Integer> columnIncluded = new HashSet<Integer>();
		if(tokens[0].matches("[[A-Za-z]|\\-]*")) {
			for(int i=0; i<tokens.length-1; i++) {
				int index = Integer.parseInt(tokens[i + 1]) - 1;
				if(index>maxIndex) maxIndex = index;
				columnIncluded.add( index );
			}
		} else {
			throw new IOException("Format Error: phetype file does not have 'Included' line.");
		}
		
		line = br.readLine();		//second line is Exclude.
		//phenotypeAsString[1]=line;
		tokens = line.split("\\s");
		if(tokens[0].matches("[[A-Za-z]|\\-]*")) {
			columnExcluded = new HashSet<Integer>();
			for(int i=0; i<tokens.length-1; i++) {
				int index = Integer.parseInt(tokens[i + 1]) - 1;
				if(index>maxIndex) maxIndex = index;
				columnExcluded.add( index );
			}
		} else {
			throw new IOException("Format Error: phetype file does not have 'Excluded' line.");
		}
		
		
		newColumnIncluded= new HashSet<Integer>();
		int newIndex = 0;
		for(int i=0; i<=maxIndex; i++) {
			if(isExcluded(i)) continue;
			
			if(columnIncluded.contains(i)) {
				newColumnIncluded.add(newIndex);
			}
			newIndex++;
		}
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
		for(int i:columnExcluded){
			phenotypeAsString[EXCLUDE_LINE_NUMBER]+=Integer.toString(i)+"\t";
		}
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
