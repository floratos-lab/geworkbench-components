package edu.columbia.geworkbench.cagrid.cluster.parser;

/**
 * @author keshav
 * @version $Id: LineParser.java,v 1.2 2007-01-04 22:03:15 watkinson Exp $
 */
public interface LineParser extends Parser {

    /**
     * Handle the parsing of a single line from the input.
     * 
     * @param line
     */
    abstract Object parseOneLine( String line );

}