package org.geworkbench.components.annotations;

/**
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: Columbia University</p>
 * <p/>
 * Defines a contract for a <code>SearchCriteria</code> that can be used
 * to make caBIO queries
 *
 * @author First Genetic Trust
 * @version $Id$
 */
public interface GeneSearchCriteria {

    GeneAnnotation[] searchByName(String name, String organism);

    GeneAnnotation[] getGenesInPathway(org.geworkbench.util.annotation.Pathway pathway);
}