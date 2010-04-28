package org.geworkbench.util.remote;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import polgara.soapPD_wsdl.SoapPDLocator;
import polgara.soapPD_wsdl.SoapPDPortType;

/**
 * <p>Title: Connection</p>
 * <p>Description: A class to abstract a link, i.e. a connection,
 * to a Server. Note: currently we are  supporting only one connection
 * type - through the SoapPDPortType. Nevertheless, the class can easily made to
 * implement a "connection" interface if we ever want to support more connection
 * types. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author Aner
 * @version $Id$
 */
public class Connection {
    private SoapPDPortType port;

    public Connection(URL serverURL) throws ConnectionCreationException {
        try {
                port = connect(serverURL);
        } catch (ServiceException ex) {
            throw new ConnectionCreationException("Could not establish connection.");
        }
    }

    /**
     * This method tries to connect to a soap server.
     *
     * @param url a URL to the server.
     * @return a soap port
     * @throws ServiceException if cannot connect to the server.
     */
    private SoapPDPortType connect(URL url) throws ServiceException {
        return new SoapPDLocator().getsoapPD(url);
    }

    /**
     * This method returns the port of which this connection is binded to.
     *
     * @return port
     */
    public SoapPDPortType getPort() {
        return port;
    }

    /**
     * This is a helper method to build a URL.
     *
     * @param host name of a host
     * @param port the port on the host
     * @return URL object
     * @throws MalformedURLException
     */
    static public URL getURL(String host, int port) throws MalformedURLException {
        String urlString = new String("http://" + host + ":" + port + "/" + "PDServ.exe");

        return new URL(urlString);
    }
}
