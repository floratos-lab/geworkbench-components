/**
 * Array.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.duke.cabig.rproteomics.model.statml;


/**
 * A non-ragged array of scalar values.  The array must be homogenous
 * in type.  The values must be encoded in base64.
 */
public class Array  implements java.io.Serializable {
    /** A comma-delimited integer list of the dimensions of the array. */
    private java.lang.String dimensions;
    /** The name of the array */
    private java.lang.String name;
    /** The type of all the values. */
    private java.lang.String type;
    /** A non-ragged array of scalar values.  The array must be homogenous
 * in type.  The values must be encoded in base64. */
    private java.lang.String base64Value;

    public Array() {
    }

    public Array(
           java.lang.String base64Value,
           java.lang.String dimensions,
           java.lang.String name,
           java.lang.String type) {
           this.dimensions = dimensions;
           this.name = name;
           this.type = type;
           this.base64Value = base64Value;
    }


    /**
     * Gets the dimensions value for this Array.
     * 
     * @return dimensions A comma-delimited integer list of the dimensions of the array.
     */
    public java.lang.String getDimensions() {
        return dimensions;
    }


    /**
     * Sets the dimensions value for this Array.
     * 
     * @param dimensions A comma-delimited integer list of the dimensions of the array.
     */
    public void setDimensions(java.lang.String dimensions) {
        this.dimensions = dimensions;
    }


    /**
     * Gets the name value for this Array.
     * 
     * @return name The name of the array
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this Array.
     * 
     * @param name The name of the array
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the type value for this Array.
     * 
     * @return type The type of all the values.
     */
    public java.lang.String getType() {
        return type;
    }


    /**
     * Sets the type value for this Array.
     * 
     * @param type The type of all the values.
     */
    public void setType(java.lang.String type) {
        this.type = type;
    }


    /**
     * Gets the base64Value value for this Array.
     * 
     * @return base64Value A non-ragged array of scalar values.  The array must be homogenous
 * in type.  The values must be encoded in base64.
     */
    public java.lang.String getBase64Value() {
        return base64Value;
    }


    /**
     * Sets the base64Value value for this Array.
     * 
     * @param base64Value A non-ragged array of scalar values.  The array must be homogenous
 * in type.  The values must be encoded in base64.
     */
    public void setBase64Value(java.lang.String base64Value) {
        this.base64Value = base64Value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Array)) return false;
        Array other = (Array) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.dimensions==null && other.getDimensions()==null) || 
             (this.dimensions!=null &&
              this.dimensions.equals(other.getDimensions()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.base64Value==null && other.getBase64Value()==null) || 
             (this.base64Value!=null &&
              this.base64Value.equals(other.getBase64Value())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getDimensions() != null) {
            _hashCode += getDimensions().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getBase64Value() != null) {
            _hashCode += getBase64Value().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Array.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "Array"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dimensions");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "dimensions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("base64Value");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "base64Value"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
