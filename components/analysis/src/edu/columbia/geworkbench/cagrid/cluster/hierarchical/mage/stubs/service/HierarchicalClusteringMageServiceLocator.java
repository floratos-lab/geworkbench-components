/**
 * HierarchicalClusteringMageServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.service;

public class HierarchicalClusteringMageServiceLocator extends org.apache.axis.client.Service implements edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.service.HierarchicalClusteringMageService {

    public HierarchicalClusteringMageServiceLocator() {
    }


    public HierarchicalClusteringMageServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public HierarchicalClusteringMageServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for HierarchicalClusteringMagePortTypePort
    private java.lang.String HierarchicalClusteringMagePortTypePort_address = "http://localhost:8080/wsrf/services/";

    public java.lang.String getHierarchicalClusteringMagePortTypePortAddress() {
        return HierarchicalClusteringMagePortTypePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String HierarchicalClusteringMagePortTypePortWSDDServiceName = "HierarchicalClusteringMagePortTypePort";

    public java.lang.String getHierarchicalClusteringMagePortTypePortWSDDServiceName() {
        return HierarchicalClusteringMagePortTypePortWSDDServiceName;
    }

    public void setHierarchicalClusteringMagePortTypePortWSDDServiceName(java.lang.String name) {
        HierarchicalClusteringMagePortTypePortWSDDServiceName = name;
    }

    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.HierarchicalClusteringMagePortType getHierarchicalClusteringMagePortTypePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(HierarchicalClusteringMagePortTypePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getHierarchicalClusteringMagePortTypePort(endpoint);
    }

    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.HierarchicalClusteringMagePortType getHierarchicalClusteringMagePortTypePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.bindings.HierarchicalClusteringMagePortTypeSOAPBindingStub _stub = new edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.bindings.HierarchicalClusteringMagePortTypeSOAPBindingStub(portAddress, this);
            _stub.setPortName(getHierarchicalClusteringMagePortTypePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setHierarchicalClusteringMagePortTypePortEndpointAddress(java.lang.String address) {
        HierarchicalClusteringMagePortTypePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.HierarchicalClusteringMagePortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.bindings.HierarchicalClusteringMagePortTypeSOAPBindingStub _stub = new edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.bindings.HierarchicalClusteringMagePortTypeSOAPBindingStub(new java.net.URL(HierarchicalClusteringMagePortTypePort_address), this);
                _stub.setPortName(getHierarchicalClusteringMagePortTypePortWSDDServiceName());
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
        if ("HierarchicalClusteringMagePortTypePort".equals(inputPortName)) {
            return getHierarchicalClusteringMagePortTypePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClusteringMage/service", "HierarchicalClusteringMageService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClusteringMage/service", "HierarchicalClusteringMagePortTypePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        if ("HierarchicalClusteringMagePortTypePort".equals(portName)) {
            setHierarchicalClusteringMagePortTypePortEndpointAddress(address);
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
