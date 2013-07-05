package org.geworkbench.components.annotations;

import java.util.List;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Implementation of the <code>Pathway</code> contract
 *
 * @author First Genetic Trust
 * @version $Id$
 */
public class PathwayImpl implements Pathway {
	
    private final gov.nih.nci.cabio.domain.Pathway pathway;

    public PathwayImpl(gov.nih.nci.cabio.domain.Pathway pathway) {
        this.pathway = pathway;
    }

    /**
     * Gets the name of the Pathway contained in this instance
     *
     * @return Pathway name
     */
    public String getPathwayName() {
        return pathway.getName();
    }

    /**
     * Gets the <code>PathwayDiagram</code> contained in the <code>Pathway</code>
     * instance
     *
     * @return Pathway diagram
     */
    public String getPathwayDiagram() {
        return pathway.getDiagram();
    }

    /**
     * Gets the Pathway Identifier of the <code>Pathway</code> instance
     *
     * @return Pathway ID
     */
    public String getPathwayId() {
        return pathway.getId().toString();
    }

    /**
     * Creates a <code>Pathway[]</code> instance from a
     * <code>gov.nih.nci.caBIO.bean.Pathway[]</code> instance
     *
     * @param array List of Pathways obtained from caBIO
     * @return list of <code>Pathway</code> intances corresponding to the caBIO
     *         results
     */
    public static Pathway[] toArray(List<gov.nih.nci.cabio.domain.Pathway> array) {
        Pathway[] toBeReturned = new PathwayImpl[array.size()];
        for (int i = 0; i < array.size(); i++) {
            toBeReturned[i] = new PathwayImpl(array.get(i));
        }

        return toBeReturned;
    }

    @Override
    public int compareTo(Pathway other) {
    	String name = pathway.getName();
    	String otherName = other.getPathwayName();
    	if(name!=null) {
    		return name.compareTo(otherName);
    	} else {
    		return -1;
    	}
    }

}

