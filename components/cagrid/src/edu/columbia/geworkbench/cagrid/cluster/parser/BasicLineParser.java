package edu.columbia.geworkbench.cagrid.cluster.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Forces the user implement how a single line will be parsed.
 * 
 * @author keshav
 * @version $Id: BasicLineParser.java,v 1.3 2007-01-10 17:14:28 keshav Exp $
 */
public abstract class BasicLineParser implements Parser {

    protected static final Log log = LogFactory.getLog( BasicLineParser.class );

    // private Collection<Object> results;
    private LinkedHashSet results;

    protected int linesParsed = 0;
    private Object header = null;

    public BasicLineParser() {
        // results = new HashSet<Object>();
        results = new LinkedHashSet();
    }

    public void parse( InputStream is ) throws IOException {
        linesParsed = 0;
        BufferedReader br = new BufferedReader( new InputStreamReader( is ) );

        String line = null;

        while ( ( line = br.readLine() ) != null ) {
            Object newItem = parseOneLine( line );

            if ( newItem != null ) {
                if ( ( ( String[] ) newItem )[0].equalsIgnoreCase( "probe" ) ) {
                    header = newItem;
                }
                results.add( newItem );
                linesParsed++;
            }
            if ( linesParsed % PARSE_ALERT_FREQUENCY == 0 ) log.debug( "Parsed " + linesParsed + " lines..." );

        }
        log.info( "Parsed " + linesParsed + " lines." );
    }

    public void parse( File file ) throws IOException {
        if ( !file.exists() || !file.canRead() ) {
            throw new IOException( "Could not read from file " + file.getPath() );
        }
        FileInputStream stream = new FileInputStream( file );
        parse( stream );
        stream.close();
    }

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

    public Object getHeader() {
        if ( header == null )
            throw new RuntimeException( "Header is null.  Cannot invoke this method if header is null." );

        return header;
    }

}
