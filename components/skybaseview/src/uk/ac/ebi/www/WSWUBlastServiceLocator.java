/**
 * WSWUBlastServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.www;

public class WSWUBlastServiceLocator
    extends org.apache.axis.client.Service implements uk.ac.ebi.www.
    WSWUBlastService
{

  // Use to get a proxy class for WSWUBlast
  private final java.lang.String WSWUBlast_address =
      "http://www.ebi.ac.uk/~alabarga/cgi-bin/webservices/WSWUBlast";

  public java.lang.String getWSWUBlastAddress()
  {
    return WSWUBlast_address;
  }

  // The WSDD service name defaults to the port name.
  private java.lang.String WSWUBlastWSDDServiceName = "WSWUBlast";

  public java.lang.String getWSWUBlastWSDDServiceName()
  {
    return WSWUBlastWSDDServiceName;
  }

  public void setWSWUBlastWSDDServiceName(java.lang.String name)
  {
    WSWUBlastWSDDServiceName = name;
  }

  public uk.ac.ebi.www.WSWUBlast getWSWUBlast()
      throws javax.xml.rpc.ServiceException
  {
    java.net.URL endpoint;
    try
    {
      endpoint = new java.net.URL(WSWUBlast_address);
    }
    catch (java.net.MalformedURLException e)
    {
      return null; // unlikely as URL was validated in WSDL2Java
    }
    return getWSWUBlast(endpoint);
  }

  public uk.ac.ebi.www.WSWUBlast getWSWUBlast(java.net.URL portAddress)
      throws javax.xml.rpc.ServiceException
  {
    try
    {
      uk.ac.ebi.www.WSWUBlastSoapBindingStub _stub = new uk.ac.ebi.www.
          WSWUBlastSoapBindingStub(portAddress, this);
      _stub.setPortName(getWSWUBlastWSDDServiceName());
      return _stub;
    }
    catch (org.apache.axis.AxisFault e)
    {
      return null;
    }
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
      if (uk.ac.ebi.www.WSWUBlast.class.isAssignableFrom(
          serviceEndpointInterface))
      {
        uk.ac.ebi.www.WSWUBlastSoapBindingStub _stub = new uk.ac.ebi.www.
            WSWUBlastSoapBindingStub(new java.net.URL(WSWUBlast_address), this);
        _stub.setPortName(getWSWUBlastWSDDServiceName());
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
    java.rmi.Remote _stub = getPort(serviceEndpointInterface);
    ( (org.apache.axis.client.Stub) _stub).setPortName(portName);
    return _stub;
  }

  public javax.xml.namespace.QName getServiceName()
  {
    return new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSWUBlast",
                                         "WSWUBlastService");
  }

  private java.util.HashSet ports = null;

  public java.util.Iterator getPorts()
  {
    if (ports == null)
    {
      ports = new java.util.HashSet();
      ports.add(new javax.xml.namespace.QName("WSWUBlast"));
    }
    return ports.iterator();
  }

}
