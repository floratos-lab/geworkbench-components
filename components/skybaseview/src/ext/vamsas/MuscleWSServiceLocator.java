/**
 * MuscleWSServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

/*
 * Jalview - A Sequence Alignment Editor and Viewer
 * Copyright (C) 2007 AM Waterhouse, J Procter, G Barton, M Clamp, S Searle
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package ext.vamsas;

public class MuscleWSServiceLocator
    extends org.apache.axis.client.Service implements ext.vamsas.
    MuscleWSService
{
  // Use to get a proxy class for MuscleWS
  private java.lang.String MuscleWS_address =
      "http://anaplog.compbio.dundee.ac.uk:8080/axis/services/MuscleWS";

  // The WSDD service name defaults to the port name.
  private java.lang.String MuscleWSWSDDServiceName = "MuscleWS";
  private java.util.HashSet ports = null;

  public MuscleWSServiceLocator()
  {
  }

  public MuscleWSServiceLocator(org.apache.axis.EngineConfiguration config)
  {
    super(config);
  }

  public java.lang.String getMuscleWSAddress()
  {
    return MuscleWS_address;
  }

  public java.lang.String getMuscleWSWSDDServiceName()
  {
    return MuscleWSWSDDServiceName;
  }

  public void setMuscleWSWSDDServiceName(java.lang.String name)
  {
    MuscleWSWSDDServiceName = name;
  }

  public ext.vamsas.MuscleWS getMuscleWS()
      throws javax.xml.rpc.ServiceException
  {
    java.net.URL endpoint;

    try
    {
      endpoint = new java.net.URL(MuscleWS_address);
    }
    catch (java.net.MalformedURLException e)
    {
      throw new javax.xml.rpc.ServiceException(e);
    }

    return getMuscleWS(endpoint);
  }

  public ext.vamsas.MuscleWS getMuscleWS(java.net.URL portAddress)
      throws javax.xml.rpc.ServiceException
  {
    try
    {
      ext.vamsas.MuscleWSSoapBindingStub _stub = new ext.vamsas.
          MuscleWSSoapBindingStub(portAddress,
                                  this);
      _stub.setPortName(getMuscleWSWSDDServiceName());

      return _stub;
    }
    catch (org.apache.axis.AxisFault e)
    {
      return null;
    }
  }

  public void setMuscleWSEndpointAddress(java.lang.String address)
  {
    MuscleWS_address = address;
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
      if (ext.vamsas.MuscleWS.class.isAssignableFrom(
          serviceEndpointInterface))
      {
        ext.vamsas.MuscleWSSoapBindingStub _stub = new ext.vamsas.
            MuscleWSSoapBindingStub(new java.net.URL(
                MuscleWS_address), this);
        _stub.setPortName(getMuscleWSWSDDServiceName());

        return _stub;
      }
    }
    catch (java.lang.Throwable t)
    {
      throw new javax.xml.rpc.ServiceException(t);
    }

    throw new javax.xml.rpc.ServiceException(
        "There is no stub implementation for the interface:  " +
        ( (serviceEndpointInterface == null) ? "null"
         : serviceEndpointInterface.getName()));
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

    if ("MuscleWS".equals(inputPortName))
    {
      return getMuscleWS();
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
    return new javax.xml.namespace.QName("vamsas", "MuscleWSService");
  }

  public java.util.Iterator getPorts()
  {
    if (ports == null)
    {
      ports = new java.util.HashSet();
      ports.add(new javax.xml.namespace.QName("vamsas", "MuscleWS"));
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
    if ("MuscleWS".equals(portName))
    {
      setMuscleWSEndpointAddress(address);
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
