package org.geworkbench.components.annotations;

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
	
    private final String pathwayName;
    private final String diagram;

    public PathwayImpl(String pathwayName, String diagram) {
        this.pathwayName = pathwayName;
        this.diagram = diagram;
    }

    /**
     * Gets the name of the Pathway contained in this instance
     *
     * @return Pathway name
     */
    @Override
    public String getPathwayName() {
        return pathwayName;
    }

    /**
     * Gets the <code>PathwayDiagram</code> contained in the <code>Pathway</code>
     * instance
     *
     * @return Pathway diagram
     */
    @Override
    public String getPathwayDiagram() {
        return diagram;
    }

    @Override
    public int compareTo(Pathway other) {
    	String otherName = other.getPathwayName();
    	if(pathwayName!=null) {
    		return pathwayName.compareTo(otherName);
    	} else {
    		return -1;
    	}
    }

}

