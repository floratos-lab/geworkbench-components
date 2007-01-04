package edu.columbia.geworkbench.cagrid.cluster.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * @author keshav
 * @version $Id: Parser.java,v 1.2 2007-01-04 22:03:15 watkinson Exp $
 */
public interface Parser {

    public static final int PARSE_ALERT_FREQUENCY = 10000;

    /**
     * Parse a {@link InputStream}.
     * 
     * @throws IOException
     * @param stream
     */
    public void parse( InputStream is ) throws IOException;

    /**
     * Parse a file identified by its path.
     * 
     * @param filename Absolute path to the file
     * @throws IOException
     */
    public void parse( String filename ) throws IOException;

    /**
     * @return the results of the parse.
     */
    // public Collection<Object> getResults();
    public Collection getResults();

    /**
     * Parse one line.
     * 
     * @param line
     * @return
     */
    public abstract Object parseOneLine( String line );

}