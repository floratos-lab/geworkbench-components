/**
 * AracneServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.aracne.stubs.service;

public class AracneServiceLocator extends org.apache.axis.client.Service implements edu.columbia.geworkbench.cagrid.aracne.stubs.service.AracneService {

    public AracneServiceLocator() {
    }


    public AracneServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AracneServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AracnePortTypePort
    private java.lang.String AracnePortTypePort_address = "http://localhost:8080/wsrf/services/";

    public java.lang.String getAracnePortTypePortAddress() {
        return AracnePortTypePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AracnePortTypePortWSDDServiceName = "AracnePortTypePort";

    public java.lang.String getAracnePortTypePortWSDDServiceName() {
        return AracnePortTypePortWSDDServiceName;
    }

    public void setAracnePortTypePortWSDDServiceName(java.lang.String name) {
        AracnePortTypePortWSDDServiceName = name;
    }

    public edu.columbia.geworkbench.cagrid.aracne.stubs.AracnePortType getAracnePortTypePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AracnePortTypePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAracnePortTypePort(endpoint);
    }

    public edu.columbia.geworkbench.cagrid.aracne.stubs.AracnePortType getAracnePortTypePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.columbia.geworkbench.cagrid.aracne.stubs.bindings.AracnePortTypeSOAPBindingStub _stub = new edu.columbia.geworkbench.cagrid.aracne.stubs.bindings.AracnePortTypeSOAPBindingStub(portAddress, this);
            _stub.setPortName(getAracnePortTypePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAracnePortTypePortEndpointAddress(java.lang.String address) {
        AracnePortTypePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.columbia.geworkbench.cagrid.aracne.stubs.AracnePortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.columbia.geworkbench.cagrid.aracne.stubs.bindings.AracnePortTypeSOAPBindingStub _stub = new edu.columbia.geworkbench.cagrid.aracne.stubs.bindings.AracnePortTypeSOAPBindingStub(new java.net.URL(AracnePortTypePort_address), this);
                _stub.setPortName(getAracnePortTypePortWSDDServiceName());
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
        if ("AracnePortTypePort".equals(inputPortName)) {
            return getAracnePortTypePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/Aracne/service", "AracneService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/Aracne/service", "AracnePortTypePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        if ("AracnePortTypePort".equals(portName)) {
            setAracnePortTypePortEndpointAddress(address);
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
