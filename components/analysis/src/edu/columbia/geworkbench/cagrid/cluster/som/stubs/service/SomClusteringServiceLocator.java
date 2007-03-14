/**
 * SomClusteringServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.som.stubs.service;

public class SomClusteringServiceLocator extends org.apache.axis.client.Service implements edu.columbia.geworkbench.cagrid.cluster.som.stubs.service.SomClusteringService {

    public SomClusteringServiceLocator() {
    }


    public SomClusteringServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SomClusteringServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SomClusteringPortTypePort
    private java.lang.String SomClusteringPortTypePort_address = "http://localhost:8080/wsrf/services/";

    public java.lang.String getSomClusteringPortTypePortAddress() {
        return SomClusteringPortTypePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SomClusteringPortTypePortWSDDServiceName = "SomClusteringPortTypePort";

    public java.lang.String getSomClusteringPortTypePortWSDDServiceName() {
        return SomClusteringPortTypePortWSDDServiceName;
    }

    public void setSomClusteringPortTypePortWSDDServiceName(java.lang.String name) {
        SomClusteringPortTypePortWSDDServiceName = name;
    }

    public edu.columbia.geworkbench.cagrid.cluster.som.stubs.SomClusteringPortType getSomClusteringPortTypePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SomClusteringPortTypePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSomClusteringPortTypePort(endpoint);
    }

    public edu.columbia.geworkbench.cagrid.cluster.som.stubs.SomClusteringPortType getSomClusteringPortTypePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.columbia.geworkbench.cagrid.cluster.som.stubs.bindings.SomClusteringPortTypeSOAPBindingStub _stub = new edu.columbia.geworkbench.cagrid.cluster.som.stubs.bindings.SomClusteringPortTypeSOAPBindingStub(portAddress, this);
            _stub.setPortName(getSomClusteringPortTypePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSomClusteringPortTypePortEndpointAddress(java.lang.String address) {
        SomClusteringPortTypePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.columbia.geworkbench.cagrid.cluster.som.stubs.SomClusteringPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.columbia.geworkbench.cagrid.cluster.som.stubs.bindings.SomClusteringPortTypeSOAPBindingStub _stub = new edu.columbia.geworkbench.cagrid.cluster.som.stubs.bindings.SomClusteringPortTypeSOAPBindingStub(new java.net.URL(SomClusteringPortTypePort_address), this);
                _stub.setPortName(getSomClusteringPortTypePortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("SomClusteringPortTypePort".equals(inputPortName)) {
            return getSomClusteringPortTypePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/SomClustering/service", "SomClusteringService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/SomClustering/service", "SomClusteringPortTypePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        if ("SomClusteringPortTypePort".equals(portName)) {
            setSomClusteringPortTypePortEndpointAddress(address);
        }
        else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
