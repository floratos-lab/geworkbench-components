/**
 * RegistryServiceSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package ext.vamsas;

public class RegistryServiceSoapBindingStub
    extends org.apache.axis.client.Stub implements ext.vamsas.IRegistry
{
  private java.util.Vector cachedSerClasses = new java.util.Vector();
  private java.util.Vector cachedSerQNames = new java.util.Vector();
  private java.util.Vector cachedSerFactories = new java.util.Vector();
  private java.util.Vector cachedDeserFactories = new java.util.Vector();

  static org.apache.axis.description.OperationDesc[] _operations;

  static
  {
    _operations = new org.apache.axis.description.OperationDesc[1];
    _initOperationDesc1();
  }

  private static void _initOperationDesc1()
  {
    org.apache.axis.description.OperationDesc oper;
    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getServices");
    oper.setReturnType(new javax.xml.namespace.QName("registry.objects.vamsas",
        "ServiceHandles"));
    oper.setReturnClass(ext.vamsas.ServiceHandles.class);
    oper.setReturnQName(new javax.xml.namespace.QName("", "getServicesReturn"));
    oper.setStyle(org.apache.axis.constants.Style.RPC);
    oper.setUse(org.apache.axis.constants.Use.ENCODED);
    _operations[0] = oper;

  }

  public RegistryServiceSoapBindingStub()
      throws org.apache.axis.AxisFault
  {
    this(null);
  }

  public RegistryServiceSoapBindingStub(java.net.URL endpointURL,
                                        javax.xml.rpc.Service service)
      throws org.apache.axis.AxisFault
  {
    this(service);
    super.cachedEndpoint = endpointURL;
  }

  public RegistryServiceSoapBindingStub(javax.xml.rpc.Service service)
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
    //java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
    //java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
    java.lang.Class arraysf = org.apache.axis.encoding.ser.
        ArraySerializerFactory.class;
    java.lang.Class arraydf = org.apache.axis.encoding.ser.
        ArrayDeserializerFactory.class;
    //java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
    //java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
    //java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
    //java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
    qName = new javax.xml.namespace.QName("vamsas",
                                          "ArrayOf_tns1_ServiceHandle");
    cachedSerQNames.add(qName);
    cls = ext.vamsas.ServiceHandle[].class;
    cachedSerClasses.add(cls);
    cachedSerFactories.add(arraysf);
    cachedDeserFactories.add(arraydf);

    qName = new javax.xml.namespace.QName("registry.objects.vamsas",
                                          "ServiceHandles");
    cachedSerQNames.add(qName);
    cls = ext.vamsas.ServiceHandles.class;
    cachedSerClasses.add(cls);
    cachedSerFactories.add(beansf);
    cachedDeserFactories.add(beandf);

    qName = new javax.xml.namespace.QName("registry.objects.vamsas",
                                          "ServiceHandle");
    cachedSerQNames.add(qName);
    cls = ext.vamsas.ServiceHandle.class;
    cachedSerClasses.add(cls);
    cachedSerFactories.add(beansf);
    cachedDeserFactories.add(beandf);

  }

  protected org.apache.axis.client.Call createCall()
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
        _call.setProperty(key, super.cachedProperties.get(key));
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
          _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.
                               SOAP11_CONSTANTS);
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
    catch (java.lang.Throwable _t)
    {
      throw new org.apache.axis.AxisFault(
          "Failure trying to get the Call object", _t);
    }
  }

  public ext.vamsas.ServiceHandles getServices()
      throws java.rmi.RemoteException
  {
    if (super.cachedEndpoint == null)
    {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[0]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(new javax.xml.namespace.QName("vamsas",
        "getServices"));

    setRequestHeaders(_call);
    setAttachments(_call);
    java.lang.Object _resp = _call.invoke(new java.lang.Object[]
                                          {});

    if (_resp instanceof java.rmi.RemoteException)
    {
      throw (java.rmi.RemoteException) _resp;
    }
    else
    {
      extractAttachments(_call);
      try
      {
        return (ext.vamsas.ServiceHandles) _resp;
      }
      catch (java.lang.Exception _exception)
      {
        return (ext.vamsas.ServiceHandles) org.apache.axis.utils.JavaUtils.
            convert(_resp, ext.vamsas.ServiceHandles.class);
      }
    }
  }

}
