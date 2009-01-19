/**
 * ServiceHandle.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package ext.vamsas;

public class ServiceHandle
    implements java.io.Serializable
{
  private java.lang.String abstractName;
  private java.lang.String description;
  private java.lang.String endpointURL;
  private java.lang.String name;

  public ServiceHandle()
  {
  }

  public ServiceHandle(
      java.lang.String abstractName,
      java.lang.String description,
      java.lang.String endpointURL,
      java.lang.String name)
  {
    this.abstractName = abstractName;
    this.description = description;
    this.endpointURL = endpointURL;
    this.name = name;
  }

  /**
   * Gets the abstractName value for this ServiceHandle.
   *
   * @return abstractName
   */
  public java.lang.String getAbstractName()
  {
    return abstractName;
  }

  /**
   * Sets the abstractName value for this ServiceHandle.
   *
   * @param abstractName
   */
  public void setAbstractName(java.lang.String abstractName)
  {
    this.abstractName = abstractName;
  }

  /**
   * Gets the description value for this ServiceHandle.
   *
   * @return description
   */
  public java.lang.String getDescription()
  {
    return description;
  }

  /**
   * Sets the description value for this ServiceHandle.
   *
   * @param description
   */
  public void setDescription(java.lang.String description)
  {
    this.description = description;
  }

  /**
   * Gets the endpointURL value for this ServiceHandle.
   *
   * @return endpointURL
   */
  public java.lang.String getEndpointURL()
  {
    return endpointURL;
  }

  /**
   * Sets the endpointURL value for this ServiceHandle.
   *
   * @param endpointURL
   */
  public void setEndpointURL(java.lang.String endpointURL)
  {
    this.endpointURL = endpointURL;
  }

  /**
   * Gets the name value for this ServiceHandle.
   *
   * @return name
   */
  public java.lang.String getName()
  {
    return name;
  }

  /**
   * Sets the name value for this ServiceHandle.
   *
   * @param name
   */
  public void setName(java.lang.String name)
  {
    this.name = name;
  }

  private java.lang.Object __equalsCalc = null;
  public synchronized boolean equals(java.lang.Object obj)
  {
    if (! (obj instanceof ServiceHandle))
    {
      return false;
    }
    ServiceHandle other = (ServiceHandle) obj;
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
        ( (this.abstractName == null && other.getAbstractName() == null) ||
         (this.abstractName != null &&
          this.abstractName.equals(other.getAbstractName()))) &&
        ( (this.description == null && other.getDescription() == null) ||
         (this.description != null &&
          this.description.equals(other.getDescription()))) &&
        ( (this.endpointURL == null && other.getEndpointURL() == null) ||
         (this.endpointURL != null &&
          this.endpointURL.equals(other.getEndpointURL()))) &&
        ( (this.name == null && other.getName() == null) ||
         (this.name != null &&
          this.name.equals(other.getName())));
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
    if (getAbstractName() != null)
    {
      _hashCode += getAbstractName().hashCode();
    }
    if (getDescription() != null)
    {
      _hashCode += getDescription().hashCode();
    }
    if (getEndpointURL() != null)
    {
      _hashCode += getEndpointURL().hashCode();
    }
    if (getName() != null)
    {
      _hashCode += getName().hashCode();
    }
    __hashCodeCalc = false;
    return _hashCode;
  }

  // Type metadata
  private static org.apache.axis.description.TypeDesc typeDesc =
      new org.apache.axis.description.TypeDesc(ServiceHandle.class, true);

  static
  {
    typeDesc.setXmlType(new javax.xml.namespace.QName("registry.objects.vamsas",
        "ServiceHandle"));
    org.apache.axis.description.ElementDesc elemField = new org.apache.axis.
        description.ElementDesc();
    elemField.setFieldName("abstractName");
    elemField.setXmlName(new javax.xml.namespace.QName("", "abstractName"));
    elemField.setXmlType(new javax.xml.namespace.QName(
        "http://schemas.xmlsoap.org/soap/encoding/", "string"));
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("description");
    elemField.setXmlName(new javax.xml.namespace.QName("", "description"));
    elemField.setXmlType(new javax.xml.namespace.QName(
        "http://schemas.xmlsoap.org/soap/encoding/", "string"));
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("endpointURL");
    elemField.setXmlName(new javax.xml.namespace.QName("", "endpointURL"));
    elemField.setXmlType(new javax.xml.namespace.QName(
        "http://schemas.xmlsoap.org/soap/encoding/", "string"));
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("name");
    elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
    elemField.setXmlType(new javax.xml.namespace.QName(
        "http://schemas.xmlsoap.org/soap/encoding/", "string"));
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
