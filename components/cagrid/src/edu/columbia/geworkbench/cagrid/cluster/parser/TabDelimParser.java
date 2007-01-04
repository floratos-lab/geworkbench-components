package edu.columbia.geworkbench.cagrid.cluster.parser;

/**
 * @author keshav
 * @version $Id: TabDelimParser.java,v 1.2 2007-01-04 22:03:15 watkinson Exp $
 */
public class TabDelimParser extends BasicLineParser {
    
    /*
     * (non-Javadoc)
     * @see edu.columbia.geworkbench.cagrid.cluster.parser.Parser#parseOneLine(java.lang.String)
     */
    public Object parseOneLine( String line ) {
        String[] fields = line.split( "\t" );
        return fields;
    }

}
