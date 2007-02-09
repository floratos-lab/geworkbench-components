/**
 * ArrayType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.duke.cabig.rproteomics.model.statml;


/**
 * A non-ragged array of scalar values.  The array must be homogenous
 * in type.  The values must be encoded in base64.
 */
public class ArrayType  implements java.io.Serializable, org.apache.axis.encoding.SimpleType {
    private java.lang.String _value;
    private java.lang.String name;  // attribute
    private edu.duke.cabig.rproteomics.model.statml.ArrayTypeType type;  // attribute
    private java.lang.String dimensions;  // attribute

    public ArrayType() {
    }

    // Simple Types must have a String constructor
    public ArrayType(java.lang.String _value) {
        this._value = _value;
    }
    // Simple Types must have a toString for serializing the value
    public java.lang.String toString() {
        return _value;
    }


    /**
     * Gets the _value value for this ArrayType.
     * 
     * @return _value
     */
    public java.lang.String get_value() {
        return _value;
    }


    /**
     * Sets the _value value for this ArrayType.
     * 
     * @param _value
     */
    public void set_value(java.lang.String _value) {
        this._value = _value;
    }


    /**
     * Gets the name value for this ArrayType.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this ArrayType.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the type value for this ArrayType.
     * 
     * @return type
     */
    public edu.duke.cabig.rproteomics.model.statml.ArrayTypeType getType() {
        return type;
    }


    /**
     * Sets the type value for this ArrayType.
     * 
     * @param type
     */
    public void setType(edu.duke.cabig.rproteomics.model.statml.ArrayTypeType type) {
        this.type = type;
    }


    /**
     * Gets the dimensions value for this ArrayType.
     * 
     * @return dimensions
     */
    public java.lang.String getDimensions() {
        return dimensions;
    }


    /**
     * Sets the dimensions value for this ArrayType.
     * 
     * @param dimensions
     */
    public void setDimensions(java.lang.String dimensions) {
        this.dimensions = dimensions;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ArrayType)) return false;
        ArrayType other = (ArrayType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this._value==null && other.get_value()==null) || 
             (this._value!=null &&
              this._value.equals(other.get_value()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.dimensions==null && other.getDimensions()==null) || 
             (this.dimensions!=null &&
              this.dimensions.equals(other.getDimensions())));
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
        if (get_value() != null) {
            _hashCode += get_value().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getDimensions() != null) {
            _hashCode += getDimensions().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ArrayType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "arrayType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("name");
        attrField.setXmlName(new javax.xml.namespace.QName("", "name"));
        attrField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "nameType"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("type");
        attrField.setXmlName(new javax.xml.namespace.QName("", "type"));
        attrField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "arrayTypeType"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("dimensions");
        attrField.setXmlName(new javax.xml.namespace.QName("", "dimensions"));
        attrField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "dimensionsType"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_value");
        elemField.setXmlName(new javax.xml.namespace.QName("", "_value"));
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
          new  org.apache.axis.encoding.ser.SimpleSerializer(
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
          new  org.apache.axis.encoding.ser.SimpleDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
