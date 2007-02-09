/**
 * ListType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.duke.cabig.rproteomics.model.statml;


/**
 * A list contains any number of lists, arrays, and values
 */
public class ListType  implements java.io.Serializable {
    /** Any number of values that contain language-specific information
 * about a list. */
    private edu.duke.cabig.rproteomics.model.statml.ListType context;
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
    private java.lang.String name;  // attribute
    private org.apache.axis.types.NonNegativeInteger length;  // attribute
    private edu.duke.cabig.rproteomics.model.statml.ScalarTypeType type;  // attribute

    public ListType() {
    }

    public ListType(
           java.lang.String _null,
           edu.duke.cabig.rproteomics.model.statml.ArrayType array,
           edu.duke.cabig.rproteomics.model.statml.ListType context,
           org.apache.axis.types.NonNegativeInteger length,
           edu.duke.cabig.rproteomics.model.statml.ListType list,
           java.lang.String name,
           edu.duke.cabig.rproteomics.model.statml.ObjectType object,
           edu.duke.cabig.rproteomics.model.statml.ScalarType scalar,
           edu.duke.cabig.rproteomics.model.statml.ScalarTypeType type) {
           this.context = context;
           this.list = list;
           this.array = array;
           this.scalar = scalar;
           this._null = _null;
           this.object = object;
           this.name = name;
           this.length = length;
           this.type = type;
    }


    /**
     * Gets the context value for this ListType.
     * 
     * @return context Any number of values that contain language-specific information
 * about a list.
     */
    public edu.duke.cabig.rproteomics.model.statml.ListType getContext() {
        return context;
    }


    /**
     * Sets the context value for this ListType.
     * 
     * @param context Any number of values that contain language-specific information
 * about a list.
     */
    public void setContext(edu.duke.cabig.rproteomics.model.statml.ListType context) {
        this.context = context;
    }


    /**
     * Gets the list value for this ListType.
     * 
     * @return list A list contains any number of lists, arrays, scalars, or nulls.
     */
    public edu.duke.cabig.rproteomics.model.statml.ListType getList() {
        return list;
    }


    /**
     * Sets the list value for this ListType.
     * 
     * @param list A list contains any number of lists, arrays, scalars, or nulls.
     */
    public void setList(edu.duke.cabig.rproteomics.model.statml.ListType list) {
        this.list = list;
    }


    /**
     * Gets the array value for this ListType.
     * 
     * @return array A non-ragged array of scalar values.  The array must be homogenous
 * in type.  The values must be encoded in base64.
     */
    public edu.duke.cabig.rproteomics.model.statml.ArrayType getArray() {
        return array;
    }


    /**
     * Sets the array value for this ListType.
     * 
     * @param array A non-ragged array of scalar values.  The array must be homogenous
 * in type.  The values must be encoded in base64.
     */
    public void setArray(edu.duke.cabig.rproteomics.model.statml.ArrayType array) {
        this.array = array;
    }


    /**
     * Gets the scalar value for this ListType.
     * 
     * @return scalar A single value
     */
    public edu.duke.cabig.rproteomics.model.statml.ScalarType getScalar() {
        return scalar;
    }


    /**
     * Sets the scalar value for this ListType.
     * 
     * @param scalar A single value
     */
    public void setScalar(edu.duke.cabig.rproteomics.model.statml.ScalarType scalar) {
        this.scalar = scalar;
    }


    /**
     * Gets the _null value for this ListType.
     * 
     * @return _null The lack of a value
     */
    public java.lang.String get_null() {
        return _null;
    }


    /**
     * Sets the _null value for this ListType.
     * 
     * @param _null The lack of a value
     */
    public void set_null(java.lang.String _null) {
        this._null = _null;
    }


    /**
     * Gets the object value for this ListType.
     * 
     * @return object
     */
    public edu.duke.cabig.rproteomics.model.statml.ObjectType getObject() {
        return object;
    }


    /**
     * Sets the object value for this ListType.
     * 
     * @param object
     */
    public void setObject(edu.duke.cabig.rproteomics.model.statml.ObjectType object) {
        this.object = object;
    }


    /**
     * Gets the name value for this ListType.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this ListType.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the length value for this ListType.
     * 
     * @return length
     */
    public org.apache.axis.types.NonNegativeInteger getLength() {
        return length;
    }


    /**
     * Sets the length value for this ListType.
     * 
     * @param length
     */
    public void setLength(org.apache.axis.types.NonNegativeInteger length) {
        this.length = length;
    }


    /**
     * Gets the type value for this ListType.
     * 
     * @return type
     */
    public edu.duke.cabig.rproteomics.model.statml.ScalarTypeType getType() {
        return type;
    }


    /**
     * Sets the type value for this ListType.
     * 
     * @param type
     */
    public void setType(edu.duke.cabig.rproteomics.model.statml.ScalarTypeType type) {
        this.type = type;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ListType)) return false;
        ListType other = (ListType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.context==null && other.getContext()==null) || 
             (this.context!=null &&
              this.context.equals(other.getContext()))) &&
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
              this.object.equals(other.getObject()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.length==null && other.getLength()==null) || 
             (this.length!=null &&
              this.length.equals(other.getLength()))) &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType())));
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
        if (getContext() != null) {
            _hashCode += getContext().hashCode();
        }
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getLength() != null) {
            _hashCode += getLength().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ListType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "listType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("name");
        attrField.setXmlName(new javax.xml.namespace.QName("", "name"));
        attrField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "nameType"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("length");
        attrField.setXmlName(new javax.xml.namespace.QName("", "length"));
        attrField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "lengthType"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("type");
        attrField.setXmlName(new javax.xml.namespace.QName("", "type"));
        attrField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "scalarTypeType"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("context");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "context"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "listType"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
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
