/**
 * ServiceHandles.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package ext.vamsas;

public class ServiceHandles
    implements java.io.Serializable
{
  private ext.vamsas.ServiceHandle[] services;

  public ServiceHandles()
  {
  }

  public ServiceHandles(
      ext.vamsas.ServiceHandle[] services)
  {
    this.services = services;
  }

  /**
   * Gets the services value for this ServiceHandles.
   *
   * @return services
   */
  public ext.vamsas.ServiceHandle[] getServices()
  {
    return services;
  }

  /**
   * Sets the services value for this ServiceHandles.
   *
   * @param services
   */
  public void setServices(ext.vamsas.ServiceHandle[] services)
  {
    this.services = services;
  }

  private java.lang.Object __equalsCalc = null;
  public synchronized boolean equals(java.lang.Object obj)
  {
    if (! (obj instanceof ServiceHandles))
    {
      return false;
    }
    ServiceHandles other = (ServiceHandles) obj;
    if (obj == null)
    {
      return false;
    }
    if (this == obj)
    {
      return true;
    }
    if (__equalsCalc != null)
    {
      return (__equalsCalc == obj);
    }
    __equalsCalc = obj;
    boolean _equals;
    _equals = true &&
        ( (this.services == null && other.getServices() == null) ||
         (this.services != null &&
          java.util.Arrays.equals(this.services, other.getServices())));
    __equalsCalc = null;
    return _equals;
  }

  private boolean __hashCodeCalc = false;
  public synchronized int hashCode()
  {
    if (__hashCodeCalc)
    {
      return 0;
    }
    __hashCodeCalc = true;
    int _hashCode = 1;
    if (getServices() != null)
    {
      for (int i = 0;
           i < java.lang.reflect.Array.getLength(getServices());
           i++)
      {
        java.lang.Object obj = java.lang.reflect.Array.get(getServices(), i);
        if (obj != null &&
            !obj.getClass().isArray())
        {
          _hashCode += obj.hashCode();
        }
      }
    }
    __hashCodeCalc = false;
    return _hashCode;
  }

  // Type metadata
  private static org.apache.axis.description.TypeDesc typeDesc =
      new org.apache.axis.description.TypeDesc(ServiceHandles.class, true);

  static
  {
    typeDesc.setXmlType(new javax.xml.namespace.QName("registry.objects.vamsas",
        "ServiceHandles"));
    org.apache.axis.description.ElementDesc elemField = new org.apache.axis.
        description.ElementDesc();
    elemField.setFieldName("services");
    elemField.setXmlName(new javax.xml.namespace.QName("", "services"));
    elemField.setXmlType(new javax.xml.namespace.QName(
        "registry.objects.vamsas", "ServiceHandle"));
    typeDesc.addFieldDesc(elemField);
  }

  /**
   * Return type metadata object
   */
  public static org.apache.axis.description.TypeDesc getTypeDesc()
  {
    return typeDesc;
  }

  /**
   * Get Custom Serializer
   */
  public static org.apache.axis.encoding.Serializer getSerializer(
      java.lang.String mechType,
      java.lang.Class _javaType,
      javax.xml.namespace.QName _xmlType)
  {
    return
        new org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
  }

  /**
   * Get Custom Deserializer
   */
  public static org.apache.axis.encoding.Deserializer getDeserializer(
      java.lang.String mechType,
      java.lang.Class _javaType,
      javax.xml.namespace.QName _xmlType)
  {
    return
        new org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
  }

}
