package edu.columbia.geworkbench.cagrid.cluster.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Forces the user implement how a single line will be parsed.
 * 
 * @author keshav
 * @version $Id: BasicLineParser.java,v 1.2 2007-01-04 22:03:15 watkinson Exp $
 */
public abstract class BasicLineParser implements Parser {

    protected static final Log log = LogFactory.getLog( BasicLineParser.class );

    // private Collection<Object> results;
    private Collection results;

    protected int linesParsed = 0;

    /**
     * 
     *
     */
    public BasicLineParser() {
        // results = new HashSet<Object>();
        results = new HashSet();
    }

    /**
     * @param is
     * @throws IOException
     */
    public void parse( InputStream is ) throws IOException {
        linesParsed = 0;
        BufferedReader br = new BufferedReader( new InputStreamReader( is ) );

        String line = null;

        while ( ( line = br.readLine() ) != null ) {
            Object newItem = parseOneLine( line );

            if ( newItem != null ) {
                results.add( newItem );
                linesParsed++;
            }
            if ( linesParsed % PARSE_ALERT_FREQUENCY == 0 ) log.debug( "Parsed " + linesParsed + " lines..." );

        }
        log.info( "Parsed " + linesParsed + " lines." );
    }

    /**
     * @param file
     * @throws IOException
     */
    public void parse( File file ) throws IOException {
        if ( !file.exists() || !file.canRead() ) {
            throw new IOException( "Could not read from file " + file.getPath() );
        }
        FileInputStream stream = new FileInputStream( file );
        parse( stream );
        stream.close();
    }

    /**
     * @param filename
     */
    public void parse( String filename ) throws IOException {
        File infile = new File( filename );
        parse( infile );
    }

    /**
     * Add an object to the results collection.
     * 
     * @param obj
     */
    protected void addResult( Object obj ) {
        this.results.add( obj );
    }

    /**
     * 
     */
    // public Collection<Object> getResults() {
    public Collection getResults() {
        return results;
    }

}
