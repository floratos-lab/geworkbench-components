/**
 * WSWUBlastSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.www;

public class WSWUBlastSoapBindingStub
    extends org.apache.axis.client.Stub implements uk.ac.ebi.www.WSWUBlast
{
  private java.util.Vector cachedSerClasses = new java.util.Vector();
  private java.util.Vector cachedSerQNames = new java.util.Vector();
  private java.util.Vector cachedSerFactories = new java.util.Vector();
  private java.util.Vector cachedDeserFactories = new java.util.Vector();

  public WSWUBlastSoapBindingStub()
      throws org.apache.axis.AxisFault
  {
    this(null);
  }

  public WSWUBlastSoapBindingStub(java.net.URL endpointURL,
                                  javax.xml.rpc.Service service)
      throws org.apache.axis.AxisFault
  {
    this(service);
    super.cachedEndpoint = endpointURL;
  }

  public WSWUBlastSoapBindingStub(javax.xml.rpc.Service service)
      throws org.apache.axis.AxisFault
  {
    if (service == null)
    {
      super.service = new org.apache.axis.client.Service();
    }
    else
    {
      super.service = service;
    }
    java.lang.Class cls;
    javax.xml.namespace.QName qName;
    java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
    java.lang.Class beandf = org.apache.axis.encoding.ser.
        BeanDeserializerFactory.class;
    java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
    java.lang.Class enumdf = org.apache.axis.encoding.ser.
        EnumDeserializerFactory.class;
    java.lang.Class arraysf = org.apache.axis.encoding.ser.
        ArraySerializerFactory.class;
    java.lang.Class arraydf = org.apache.axis.encoding.ser.
        ArrayDeserializerFactory.class;
    java.lang.Class simplesf = org.apache.axis.encoding.ser.
        SimpleSerializerFactory.class;
    java.lang.Class simpledf = org.apache.axis.encoding.ser.
        SimpleDeserializerFactory.class;
    qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSWUBlast",
                                          "inputParams");
    cachedSerQNames.add(qName);
    cls = uk.ac.ebi.www.InputParams.class;
    cachedSerClasses.add(cls);
    cachedSerFactories.add(beansf);
    cachedDeserFactories.add(beandf);

    qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSWUBlast",
                                          "WSArrayofFile");
    cachedSerQNames.add(qName);
    cls = uk.ac.ebi.www.WSFile[].class;
    cachedSerClasses.add(cls);
    cachedSerFactories.add(arraysf);
    cachedDeserFactories.add(arraydf);

    qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSWUBlast",
                                          "WSArrayofData");
    cachedSerQNames.add(qName);
    cls = uk.ac.ebi.www.Data[].class;
    cachedSerClasses.add(cls);
    cachedSerFactories.add(arraysf);
    cachedDeserFactories.add(arraydf);

    qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSWUBlast",
                                          "data");
    cachedSerQNames.add(qName);
    cls = uk.ac.ebi.www.Data.class;
    cachedSerClasses.add(cls);
    cachedSerFactories.add(beansf);
    cachedDeserFactories.add(beandf);

    qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSWUBlast",
                                          "WSFile");
    cachedSerQNames.add(qName);
    cls = uk.ac.ebi.www.WSFile.class;
    cachedSerClasses.add(cls);
    cachedSerFactories.add(beansf);
    cachedDeserFactories.add(beandf);

  }

  private org.apache.axis.client.Call createCall()
      throws java.rmi.RemoteException
  {
    try
    {
      org.apache.axis.client.Call _call =
          (org.apache.axis.client.Call)super.service.createCall();
      if (super.maintainSessionSet)
      {
        _call.setMaintainSession(super.maintainSession);
      }
      if (super.cachedUsername != null)
      {
        _call.setUsername(super.cachedUsername);
      }
      if (super.cachedPassword != null)
      {
        _call.setPassword(super.cachedPassword);
      }
      if (super.cachedEndpoint != null)
      {
        _call.setTargetEndpointAddress(super.cachedEndpoint);
      }
      if (super.cachedTimeout != null)
      {
        _call.setTimeout(super.cachedTimeout);
      }
      if (super.cachedPortName != null)
      {
        _call.setPortName(super.cachedPortName);
      }
      java.util.Enumeration keys = super.cachedProperties.keys();
      while (keys.hasMoreElements())
      {
        java.lang.String key = (java.lang.String) keys.nextElement();
        if (_call.isPropertySupported(key))
        {
          _call.setProperty(key, super.cachedProperties.get(key));
        }
        //  else
        //   _call.setScopedProperty(key, super.cachedProperties.get(key));
      }
      // All the type mapping information is registered
      // when the first call is made.
      // The type mapping information is actually registered in
      // the TypeMappingRegistry of the service, which
      // is the reason why registration is only needed for the first call.
      synchronized (this)
      {
        if (firstCall())
        {
          // must set encoding style before registering serializers
          _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
          for (int i = 0; i < cachedSerFactories.size(); ++i)
          {
            java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
            javax.xml.namespace.QName qName =
                (javax.xml.namespace.QName) cachedSerQNames.get(i);
            java.lang.Class sf = (java.lang.Class)
                cachedSerFactories.get(i);
            java.lang.Class df = (java.lang.Class)
                cachedDeserFactories.get(i);
            _call.registerTypeMapping(cls, qName, sf, df, false);
          }
        }
      }
      return _call;
    }
    catch (java.lang.Throwable t)
    {
      throw new org.apache.axis.AxisFault(
          "Failure trying to get the Call object", t);
    }
  }

  public byte[] poll(java.lang.String jobid, java.lang.String type)
      throws java.rmi.RemoteException
  {
    if (super.cachedEndpoint == null)
    {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.addParameter(new javax.xml.namespace.QName("", "jobid"),
                       new javax.
                       xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                           "string"), java.lang.String.class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.addParameter(new javax.xml.namespace.QName("", "type"),
                       new javax.xml.
                       namespace.QName("http://www.w3.org/2001/XMLSchema",
                                       "string"), java.lang.String.class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.setReturnType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("http://www.ebi.ac.uk/WSWUBlast#poll");
    _call.setOperationStyle("rpc");
    _call.setOperationName(new javax.xml.namespace.QName(
        "http://www.ebi.ac.uk/WSWUBlast", "poll"));

    java.lang.Object _resp = _call.invoke(new java.lang.Object[]
                                          {jobid, type});

    if (_resp instanceof java.rmi.RemoteException)
    {
      throw (java.rmi.RemoteException) _resp;
    }
    else
    {
      try
      {
        return (byte[]) _resp;
      }
      catch (java.lang.Exception _exception)
      {
        return (byte[]) org.apache.axis.utils.JavaUtils.convert(_resp, byte[].class);
      }
    }
  }

  public java.lang.String runWUBlast(uk.ac.ebi.www.InputParams params,
                                     uk.ac.ebi.www.Data[] content)
      throws java.rmi.RemoteException
  {
    if (super.cachedEndpoint == null)
    {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.addParameter(new javax.xml.namespace.QName("", "params"),
                       new javax.
                       xml.namespace.QName("http://www.ebi.ac.uk/WSWUBlast",
                                           "inputParams"),
                       uk.ac.ebi.www.InputParams.class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.addParameter(new javax.xml.namespace.QName("", "content"),
                       new javax.
                       xml.namespace.QName("http://www.ebi.ac.uk/WSWUBlast",
                                           "WSArrayofData"), uk.ac.ebi.www.Data[].class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.setReturnType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("http://www.ebi.ac.uk/WSWUBlast#runWUBlast");
    _call.setOperationStyle("rpc");
    _call.setOperationName(new javax.xml.namespace.QName(
        "http://www.ebi.ac.uk/WSWUBlast", "runWUBlast"));

    java.lang.Object _resp = _call.invoke(new java.lang.Object[]
                                          {params, content});

    if (_resp instanceof java.rmi.RemoteException)
    {
      throw (java.rmi.RemoteException) _resp;
    }
    else
    {
      try
      {
        return (java.lang.String) _resp;
      }
      catch (java.lang.Exception _exception)
      {
        return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp,
            java.lang.String.class);
      }
    }
  }

  public byte[] test(java.lang.String jobid, java.lang.String type)
      throws java.rmi.RemoteException
  {
    if (super.cachedEndpoint == null)
    {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.addParameter(new javax.xml.namespace.QName("", "jobid"),
                       new javax.
                       xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                           "string"), java.lang.String.class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.addParameter(new javax.xml.namespace.QName("", "type"),
                       new javax.xml.
                       namespace.QName("http://www.w3.org/2001/XMLSchema",
                                       "string"), java.lang.String.class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.setReturnType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("http://www.ebi.ac.uk/WSWUBlast#test");
    _call.setOperationStyle("rpc");
    _call.setOperationName(new javax.xml.namespace.QName(
        "http://www.ebi.ac.uk/WSWUBlast", "test"));

    java.lang.Object _resp = _call.invoke(new java.lang.Object[]
                                          {jobid, type});

    if (_resp instanceof java.rmi.RemoteException)
    {
      throw (java.rmi.RemoteException) _resp;
    }
    else
    {
      try
      {
        return (byte[]) _resp;
      }
      catch (java.lang.Exception _exception)
      {
        return (byte[]) org.apache.axis.utils.JavaUtils.convert(_resp, byte[].class);
      }
    }
  }

  public java.lang.String checkStatus(java.lang.String jobid)
      throws java.rmi.RemoteException
  {
    if (super.cachedEndpoint == null)
    {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.addParameter(new javax.xml.namespace.QName("", "jobid"),
                       new javax.
                       xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                           "string"), java.lang.String.class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.setReturnType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("http://www.ebi.ac.uk/WSWUBlast#checkStatus");
    _call.setOperationStyle("rpc");
    _call.setOperationName(new javax.xml.namespace.QName(
        "http://www.ebi.ac.uk/WSWUBlast", "checkStatus"));

    java.lang.Object _resp = _call.invoke(new java.lang.Object[]
                                          {jobid});

    if (_resp instanceof java.rmi.RemoteException)
    {
      throw (java.rmi.RemoteException) _resp;
    }
    else
    {
      try
      {
        return (java.lang.String) _resp;
      }
      catch (java.lang.Exception _exception)
      {
        return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp,
            java.lang.String.class);
      }
    }
  }

  public uk.ac.ebi.www.WSFile[] getResults(java.lang.String jobid)
      throws java.rmi.RemoteException
  {
    if (super.cachedEndpoint == null)
    {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.addParameter(new javax.xml.namespace.QName("", "jobid"),
                       new javax.
                       xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                           "string"), java.lang.String.class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.setReturnType(new javax.xml.namespace.QName(
        "http://www.ebi.ac.uk/WSWUBlast", "WSArrayofFile"),
                        uk.ac.ebi.www.WSFile[].class);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("http://www.ebi.ac.uk/WSWUBlast#getResults");
    _call.setOperationStyle("rpc");
    _call.setOperationName(new javax.xml.namespace.QName(
        "http://www.ebi.ac.uk/WSWUBlast", "getResults"));

    java.lang.Object _resp = _call.invoke(new java.lang.Object[]
                                          {jobid});

    if (_resp instanceof java.rmi.RemoteException)
    {
      throw (java.rmi.RemoteException) _resp;
    }
    else
    {
      try
      {
        return (uk.ac.ebi.www.WSFile[]) _resp;
      }
      catch (java.lang.Exception _exception)
      {
        return (uk.ac.ebi.www.WSFile[]) org.apache.axis.utils.JavaUtils.convert(
            _resp, uk.ac.ebi.www.WSFile[].class);
      }
    }
  }

  public byte[] polljob(java.lang.String jobid, java.lang.String outformat)
      throws java.rmi.RemoteException
  {
    if (super.cachedEndpoint == null)
    {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.addParameter(new javax.xml.namespace.QName("", "jobid"),
                       new javax.
                       xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                           "string"), java.lang.String.class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.addParameter(new javax.xml.namespace.QName("", "outformat"),
                       new
                       javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema",
        "string"), java.lang.String.class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.setReturnType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("http://www.ebi.ac.uk/WSWUBlast#polljob");
    _call.setOperationStyle("rpc");
    _call.setOperationName(new javax.xml.namespace.QName(
        "http://www.ebi.ac.uk/WSWUBlast", "polljob"));

    java.lang.Object _resp = _call.invoke(new java.lang.Object[]
                                          {jobid, outformat});

    if (_resp instanceof java.rmi.RemoteException)
    {
      throw (java.rmi.RemoteException) _resp;
    }
    else
    {
      try
      {
        return (byte[]) _resp;
      }
      catch (java.lang.Exception _exception)
      {
        return (byte[]) org.apache.axis.utils.JavaUtils.convert(_resp, byte[].class);
      }
    }
  }

  public byte[] doWUBlast(uk.ac.ebi.www.InputParams params, byte[] content)
      throws java.rmi.RemoteException
  {
    if (super.cachedEndpoint == null)
    {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.addParameter(new javax.xml.namespace.QName("", "params"),
                       new javax.
                       xml.namespace.QName("http://www.ebi.ac.uk/WSWUBlast",
                                           "inputParams"),
                       uk.ac.ebi.www.InputParams.class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.addParameter(new javax.xml.namespace.QName("", "content"),
                       new javax.
                       xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                           "base64Binary"), byte[].class,
                       javax.xml.rpc.ParameterMode.IN);
    _call.setReturnType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("http://www.ebi.ac.uk/WSWUBlast#doWUBlast");
    _call.setOperationStyle("rpc");
    _call.setOperationName(new javax.xml.namespace.QName(
        "http://www.ebi.ac.uk/WSWUBlast", "doWUBlast"));

    java.lang.Object _resp = _call.invoke(new java.lang.Object[]
                                          {params, content});

    if (_resp instanceof java.rmi.RemoteException)
    {
      throw (java.rmi.RemoteException) _resp;
    }
    else
    {
      try
      {
        return (byte[]) _resp;
      }
      catch (java.lang.Exception _exception)
      {
        return (byte[]) org.apache.axis.utils.JavaUtils.convert(_resp, byte[].class);
      }
    }
  }

}
