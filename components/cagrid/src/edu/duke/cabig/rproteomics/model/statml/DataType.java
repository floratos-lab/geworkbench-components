/**
 * DataType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.duke.cabig.rproteomics.model.statml;


/**
 * The root element contains any number of lists, arrays, and values
 */
public class DataType  implements java.io.Serializable {
    /** A list contains any number of lists, arrays, scalars, or nulls. */
    private edu.duke.cabig.rproteomics.model.statml.ListType list;
    /** A non-ragged array of scalar values.  The array must be homogenous
 * in type.  The values must be encoded in base64. */
    private edu.duke.cabig.rproteomics.model.statml.ArrayType array;
    /** A single value */
    private edu.duke.cabig.rproteomics.model.statml.ScalarType scalar;
    /** The lack of a value */
    private java.lang.String _null;
    private edu.duke.cabig.rproteomics.model.statml.ObjectType object;

    public DataType() {
    }

    public DataType(
           java.lang.String _null,
           edu.duke.cabig.rproteomics.model.statml.ArrayType array,
           edu.duke.cabig.rproteomics.model.statml.ListType list,
           edu.duke.cabig.rproteomics.model.statml.ObjectType object,
           edu.duke.cabig.rproteomics.model.statml.ScalarType scalar) {
           this.list = list;
           this.array = array;
           this.scalar = scalar;
           this._null = _null;
           this.object = object;
    }


    /**
     * Gets the list value for this DataType.
     * 
     * @return list A list contains any number of lists, arrays, scalars, or nulls.
     */
    public edu.duke.cabig.rproteomics.model.statml.ListType getList() {
        return list;
    }


    /**
     * Sets the list value for this DataType.
     * 
     * @param list A list contains any number of lists, arrays, scalars, or nulls.
     */
    public void setList(edu.duke.cabig.rproteomics.model.statml.ListType list) {
        this.list = list;
    }


    /**
     * Gets the array value for this DataType.
     * 
     * @return array A non-ragged array of scalar values.  The array must be homogenous
 * in type.  The values must be encoded in base64.
     */
    public edu.duke.cabig.rproteomics.model.statml.ArrayType getArray() {
        return array;
    }


    /**
     * Sets the array value for this DataType.
     * 
     * @param array A non-ragged array of scalar values.  The array must be homogenous
 * in type.  The values must be encoded in base64.
     */
    public void setArray(edu.duke.cabig.rproteomics.model.statml.ArrayType array) {
        this.array = array;
    }


    /**
     * Gets the scalar value for this DataType.
     * 
     * @return scalar A single value
     */
    public edu.duke.cabig.rproteomics.model.statml.ScalarType getScalar() {
        return scalar;
    }


    /**
     * Sets the scalar value for this DataType.
     * 
     * @param scalar A single value
     */
    public void setScalar(edu.duke.cabig.rproteomics.model.statml.ScalarType scalar) {
        this.scalar = scalar;
    }


    /**
     * Gets the _null value for this DataType.
     * 
     * @return _null The lack of a value
     */
    public java.lang.String get_null() {
        return _null;
    }


    /**
     * Sets the _null value for this DataType.
     * 
     * @param _null The lack of a value
     */
    public void set_null(java.lang.String _null) {
        this._null = _null;
    }


    /**
     * Gets the object value for this DataType.
     * 
     * @return object
     */
    public edu.duke.cabig.rproteomics.model.statml.ObjectType getObject() {
        return object;
    }


    /**
     * Sets the object value for this DataType.
     * 
     * @param object
     */
    public void setObject(edu.duke.cabig.rproteomics.model.statml.ObjectType object) {
        this.object = object;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DataType)) return false;
        DataType other = (DataType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.list==null && other.getList()==null) || 
             (this.list!=null &&
              this.list.equals(other.getList()))) &&
            ((this.array==null && other.getArray()==null) || 
             (this.array!=null &&
              this.array.equals(other.getArray()))) &&
            ((this.scalar==null && other.getScalar()==null) || 
             (this.scalar!=null &&
              this.scalar.equals(other.getScalar()))) &&
            ((this._null==null && other.get_null()==null) || 
             (this._null!=null &&
              this._null.equals(other.get_null()))) &&
            ((this.object==null && other.getObject()==null) || 
             (this.object!=null &&
              this.object.equals(other.getObject())));
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
        if (getList() != null) {
            _hashCode += getList().hashCode();
        }
        if (getArray() != null) {
            _hashCode += getArray().hashCode();
        }
        if (getScalar() != null) {
            _hashCode += getScalar().hashCode();
        }
        if (get_null() != null) {
            _hashCode += get_null().hashCode();
        }
        if (getObject() != null) {
            _hashCode += getObject().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DataType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "dataType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("list");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "list"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "listType"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("array");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "array"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "arrayType"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("scalar");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "scalar"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "scalarType"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_null");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "null"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("object");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "object"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "objectType"));
        elemField.setMinOccurs(0);
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
