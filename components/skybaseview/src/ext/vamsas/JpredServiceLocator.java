/**
 * JpredServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package ext.vamsas;

public class JpredServiceLocator
    extends org.apache.axis.client.Service implements ext.vamsas.JpredService
{

  public JpredServiceLocator()
  {
  }

  public JpredServiceLocator(org.apache.axis.EngineConfiguration config)
  {
    super(config);
  }

  // Use to get a proxy class for jpred
  private java.lang.String jpred_address =
      "http://www.compbio.dundee.ac.uk/JalviewWS/services/jpred";

  public java.lang.String getjpredAddress()
  {
    return jpred_address;
  }

  // The WSDD service name defaults to the port name.
  private java.lang.String jpredWSDDServiceName = "jpred";

  public java.lang.String getjpredWSDDServiceName()
  {
    return jpredWSDDServiceName;
  }

  public void setjpredWSDDServiceName(java.lang.String name)
  {
    jpredWSDDServiceName = name;
  }

  public ext.vamsas.Jpred getjpred()
      throws javax.xml.rpc.ServiceException
  {
    java.net.URL endpoint;
    try
    {
      endpoint = new java.net.URL(jpred_address);
    }
    catch (java.net.MalformedURLException e)
    {
      throw new javax.xml.rpc.ServiceException(e);
    }
    return getjpred(endpoint);
  }

  public ext.vamsas.Jpred getjpred(java.net.URL portAddress)
      throws javax.xml.rpc.ServiceException
  {
    try
    {
      ext.vamsas.JpredSoapBindingStub _stub = new ext.vamsas.
          JpredSoapBindingStub(portAddress, this);
      _stub.setPortName(getjpredWSDDServiceName());
      return _stub;
    }
    catch (org.apache.axis.AxisFault e)
    {
      return null;
    }
  }

  public void setjpredEndpointAddress(java.lang.String address)
  {
    jpred_address = address;
  }

  /**
   * For the given interface, get the stub implementation.
   * If this service has no port for the given interface,
   * then ServiceException is thrown.
   */
  public java.rmi.Remote getPort(Class serviceEndpointInterface)
      throws javax.xml.rpc.ServiceException
  {
    try
    {
      if (ext.vamsas.Jpred.class.isAssignableFrom(serviceEndpointInterface))
      {
        ext.vamsas.JpredSoapBindingStub _stub = new ext.vamsas.
            JpredSoapBindingStub(new java.net.URL(jpred_address), this);
        _stub.setPortName(getjpredWSDDServiceName());
        return _stub;
      }
    }
    catch (java.lang.Throwable t)
    {
      throw new javax.xml.rpc.ServiceException(t);
    }
    throw new javax.xml.rpc.ServiceException(
        "There is no stub implementation for the interface:  " +
        (serviceEndpointInterface == null ? "null" :
         serviceEndpointInterface.getName()));
  }

  /**
   * For the given interface, get the stub implementation.
   * If this service has no port for the given interface,
   * then ServiceException is thrown.
   */
  public java.rmi.Remote getPort(javax.xml.namespace.QName portName,
                                 Class serviceEndpointInterface)
      throws javax.xml.rpc.ServiceException
  {
    if (portName == null)
    {
      return getPort(serviceEndpointInterface);
    }
    java.lang.String inputPortName = portName.getLocalPart();
    if ("jpred".equals(inputPortName))
    {
      return getjpred();
    }
    else
    {
      java.rmi.Remote _stub = getPort(serviceEndpointInterface);
      ( (org.apache.axis.client.Stub) _stub).setPortName(portName);
      return _stub;
    }
  }

  public javax.xml.namespace.QName getServiceName()
  {
    return new javax.xml.namespace.QName("vamsas", "jpredService");
  }

  private java.util.HashSet ports = null;

  public java.util.Iterator getPorts()
  {
    if (ports == null)
    {
      ports = new java.util.HashSet();
      ports.add(new javax.xml.namespace.QName("vamsas", "jpred"));
    }
    return ports.iterator();
  }

  /**
   * Set the endpoint address for the specified port name.
   */
  public void setEndpointAddress(java.lang.String portName,
                                 java.lang.String address)
      throws javax.xml.rpc.ServiceException
  {
    if ("jpred".equals(portName))
    {
      setjpredEndpointAddress(address);
    }
    else
    { // Unknown Port Name
      throw new javax.xml.rpc.ServiceException(
          " Cannot set Endpoint Address for Unknown Port" + portName);
    }
  }

  /**
   * Set the endpoint address for the specified port name.
   */
  public void setEndpointAddress(javax.xml.namespace.QName portName,
                                 java.lang.String address)
      throws javax.xml.rpc.ServiceException
  {
    setEndpointAddress(portName.getLocalPart(), address);
  }

}
