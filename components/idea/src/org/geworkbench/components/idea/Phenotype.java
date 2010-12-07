package org.geworkbench.components.idea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

/**
 * Data structure to hold the phenotype information from parameter panel. 
 * 
 * @author zji
 * @version $Id$
 *
 */
public class Phenotype implements Serializable {
	private static final long serialVersionUID = 1929271049658752446L;
	
	final int[] columnIncluded;
	final int[] columnExcluded;

	final static private String PHENO_INCLUDE = "Include";
	final static private String PHENO_EXCLUDE = "Exclude";

	Phenotype(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		String[] tokens = line.split("\\s");
		if(tokens[0].equals(PHENO_INCLUDE)) {
			columnIncluded = new int[tokens.length-1];
			for(int i=0; i<columnIncluded.length; i++) {
				columnIncluded[i] = Integer.parseInt(tokens[i + 1]) - 1;
			}
		} else {
			throw new IOException("Format Error: phetype file does not have 'Included' line.");
		}

		line = br.readLine();
		tokens = line.split("\\s");
		if(tokens[0].equals(PHENO_EXCLUDE)) {
			columnExcluded = new int[tokens.length-1];
			for(int i=0; i<columnExcluded.length; i++) {
				columnExcluded[i] = Integer.parseInt(tokens[i + 1]) - 1;
			}
		} else {
			throw new IOException("Format Error: phetype file does not have 'Excluded' line.");
		}
	}
}
