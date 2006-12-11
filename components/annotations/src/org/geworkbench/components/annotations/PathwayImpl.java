package org.geworkbench.components.annotations;

import gov.nih.nci.caBIO.util.PathwayDiagram;
import gov.nih.nci.common.util.SVGManipulator;
import org.geworkbench.util.annotation.Pathway;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Implementation of the <code>Pathway</code> contract
 *
 * @author First Genetic Trust
 * @version 1.0
 */
public class PathwayImpl implements org.geworkbench.util.annotation.Pathway {
    private gov.nih.nci.cabio.domain.Pathway pathway = null;

    /**
     * Default Constructor
     */
    public PathwayImpl() {
    }

    public PathwayImpl(gov.nih.nci.cabio.domain.Pathway pathway) {
        this.pathway = pathway;
    }

    /**
     * Sets the conatined <code>gov.nih.nci.caBIO.bean.Pathway</code>
     *
     * @param pathway Pathway to be contained in this instance
     */
    public void setPathway(gov.nih.nci.cabio.domain.Pathway pathway) {
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
    public static Pathway[] toArray(gov.nih.nci.cabio.domain.Pathway[] array) {
        Pathway[] toBeReturned = new PathwayImpl[array.length];
        for (int i = 0; i < array.length; i++) {
            toBeReturned[i] = new PathwayImpl(array[i]);
        }

        return toBeReturned;
    }

}

