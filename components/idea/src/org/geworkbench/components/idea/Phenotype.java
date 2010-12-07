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
	
	private final Set<Integer> columnIncluded;
	private final Set<Integer> columnExcluded;

	final static private String PHENO_INCLUDE = "Include";
	final static private String PHENO_EXCLUDE = "Exclude";

	/**
	 * Constructor from given column indices included. User for null distribution only.
	 * @param nullPhenoCols 
	 */
	Phenotype(Set<Integer> nullPhenoCols)  {
		columnIncluded = nullPhenoCols;
		columnExcluded = new HashSet<Integer>();
	}
	
	Phenotype(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		String[] tokens = line.split("\\s");
		if(tokens[0].equals(PHENO_INCLUDE)) {
			columnIncluded = new HashSet<Integer>();
			for(int i=0; i<tokens.length-1; i++) {
				columnIncluded.add( Integer.parseInt(tokens[i + 1]) - 1 );
			}
		} else {
			throw new IOException("Format Error: phetype file does not have 'Included' line.");
		}

		line = br.readLine();
		tokens = line.split("\\s");
		if(tokens[0].equals(PHENO_EXCLUDE)) {
			columnExcluded = new HashSet<Integer>();
			for(int i=0; i<tokens.length-1; i++) {
				columnExcluded.add( Integer.parseInt(tokens[i + 1]) - 1 );
			}
		} else {
			throw new IOException("Format Error: phetype file does not have 'Excluded' line.");
		}
	}
	
	boolean isIncluded(int col) {
		if(columnIncluded.contains(col))return true;
		else return false;
	}

	boolean isExcluded(int col) {
		if(columnExcluded.contains(col))return true;
		else return false;
	}

	public int getIncludedCount() {
		return columnIncluded.size();
	}

	public int getExcludedCount() {
		return columnExcluded.size();
	}
}
