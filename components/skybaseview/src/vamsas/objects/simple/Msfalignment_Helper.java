/**
 * Msfalignment_Helper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package vamsas.objects.simple;

public class Msfalignment_Helper
{
  // Type metadata
  private static org.apache.axis.description.TypeDesc typeDesc =
      new org.apache.axis.description.TypeDesc(Msfalignment.class, true);

  static
  {
    typeDesc.setXmlType(new javax.xml.namespace.QName(
        "http://simple.objects.vamsas", "Msfalignment"));
    org.apache.axis.description.ElementDesc elemField = new org.apache.axis.
        description.ElementDesc();
    elemField.setFieldName("msf");
    elemField.setXmlName(new javax.xml.namespace.QName("", "msf"));
    elemField.setXmlType(new javax.xml.namespace.QName(
        "http://schemas.xmlsoap.org/soap/encoding/", "string"));
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("notes");
    elemField.setXmlName(new javax.xml.namespace.QName("", "notes"));
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
