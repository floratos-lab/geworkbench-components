package org.geworkbench.components.promoter.modulediscovery;

import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;

import java.util.Comparator;

/**
 * Note: this comparator imposes orderings that are inconsistent with equals."
 * <p>
 * Title: Bioworks
 * </p>
 * <p>
 * Description: Modular Application Framework for Gene Expession, Sequence and
 * Genotype Analysis
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003 -2004
 * </p>
 * <p>
 * Company: Columbia University
 * </p>
 * 
 * @author not attributable
 * @version $Id$
 */
public class PatternMatchComparator implements
		Comparator<DSPatternMatch<DSSequence, CSSeqRegistration>> {

	/**
	 * equals
	 * 
	 * @param obj
	 *            Object
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		return false;
	}

	/**
	 * compare
	 * 
	 * @param o1
	 *            Object
	 * @param o2
	 *            Object
	 * @return int
	 */
	public int compare(DSPatternMatch<DSSequence, CSSeqRegistration> m1,
			DSPatternMatch<DSSequence, CSSeqRegistration> m2) {
		CSSeqRegistration reg1 = (CSSeqRegistration) m1.getRegistration();
		CSSeqRegistration reg2 = (CSSeqRegistration) m2.getRegistration();

		if (reg1.x1 > reg2.x1)
			return 1;
		else if (reg1.x1 < reg2.x1)
			return -1;
		return 0;
	}
}
