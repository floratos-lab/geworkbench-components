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

	private final Set<Integer> newColumnIncluded;	//zheng
	private final Set<Integer> columnExcluded;

	final static private String PHENO_INCLUDE = "Include";
	final static private String PHENO_EXCLUDE = "Exclude";

	/**
	 * Constructor from given column indices included. User for null distribution only.
	 * @param nullPhenoCols 
	 */
	Phenotype(Set<Integer> nullPhenoCols)  {
		columnExcluded = new HashSet<Integer>();
		newColumnIncluded = nullPhenoCols;
	}
	
	Phenotype(File file) throws IOException {
		int maxIndex = 0;
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		String[] tokens = line.split("\\s");
		Set<Integer> columnIncluded = new HashSet<Integer>();
		if(tokens[0].equals(PHENO_INCLUDE)) {
			for(int i=0; i<tokens.length-1; i++) {
				int index = Integer.parseInt(tokens[i + 1]) - 1;
				if(index>maxIndex) maxIndex = index;
				columnIncluded.add( index );
			}
		} else {
			throw new IOException("Format Error: phetype file does not have 'Included' line.");
		}

		line = br.readLine();
		tokens = line.split("\\s");
		if(tokens[0].equals(PHENO_EXCLUDE)) {
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
}
