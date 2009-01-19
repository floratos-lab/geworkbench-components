/**
 * Data.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.www;

public class Data
    implements java.io.Serializable
{
  private java.lang.String type;
  private java.lang.String content;

  public Data()
  {
  }

  public java.lang.String getType()
  {
    return type;
  }

  public void setType(java.lang.String type)
  {
    this.type = type;
  }

  public java.lang.String getContent()
  {
    return content;
  }

  public void setContent(java.lang.String content)
  {
    this.content = content;
  }

  private java.lang.Object __equalsCalc = null;
  public synchronized boolean equals(java.lang.Object obj)
  {
    if (! (obj instanceof Data))
    {
      return false;
    }
    Data other = (Data) obj;
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
        ( (type == null && other.getType() == null) ||
         (type != null &&
          type.equals(other.getType()))) &&
        ( (content == null && other.getContent() == null) ||
         (content != null &&
          content.equals(other.getContent())));
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
    if (getType() != null)
    {
      _hashCode += getType().hashCode();
    }
    if (getContent() != null)
    {
      _hashCode += getContent().hashCode();
    }
    __hashCodeCalc = false;
    return _hashCode;
  }

  // Type metadata
  private static org.apache.axis.description.TypeDesc typeDesc =
      new org.apache.axis.description.TypeDesc(Data.class);

  static
  {
    org.apache.axis.description.FieldDesc field = new org.apache.axis.
        description.ElementDesc();
    field.setFieldName("type");
    field.setXmlName(new javax.xml.namespace.QName("", "type"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("content");
    field.setXmlName(new javax.xml.namespace.QName("", "content"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
  };

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
