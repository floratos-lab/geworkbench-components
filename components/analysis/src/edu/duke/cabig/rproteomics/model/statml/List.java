/**
 * List.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.duke.cabig.rproteomics.model.statml;


/**
 * A list contains any number of lists, arrays, and values
 */
public class List  implements java.io.Serializable {
    /** The name of the list. */
    private java.lang.String name;
    /** If this list is contains all scalars of the same type, that type
 * is repeated here. */
    private java.lang.String type;
    /** The number of items (elements) in the list. */
    private java.math.BigInteger length;
    private edu.duke.cabig.rproteomics.model.statml.Scalar[] scalar;
    private edu.duke.cabig.rproteomics.model.statml.List list;
    private edu.duke.cabig.rproteomics.model.statml._null[] aNull;
    private edu.duke.cabig.rproteomics.model.statml.Array[] array;

    public List() {
    }

    public List(
           edu.duke.cabig.rproteomics.model.statml._null[] aNull,
           edu.duke.cabig.rproteomics.model.statml.Array[] array,
           java.math.BigInteger length,
           edu.duke.cabig.rproteomics.model.statml.List list,
           java.lang.String name,
           edu.duke.cabig.rproteomics.model.statml.Scalar[] scalar,
           java.lang.String type) {
           this.name = name;
           this.type = type;
           this.length = length;
           this.scalar = scalar;
           this.list = list;
           this.aNull = aNull;
           this.array = array;
    }


    /**
     * Gets the name value for this List.
     * 
     * @return name The name of the list.
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this List.
     * 
     * @param name The name of the list.
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the type value for this List.
     * 
     * @return type If this list is contains all scalars of the same type, that type
 * is repeated here.
     */
    public java.lang.String getType() {
        return type;
    }


    /**
     * Sets the type value for this List.
     * 
     * @param type If this list is contains all scalars of the same type, that type
 * is repeated here.
     */
    public void setType(java.lang.String type) {
        this.type = type;
    }


    /**
     * Gets the length value for this List.
     * 
     * @return length The number of items (elements) in the list.
     */
    public java.math.BigInteger getLength() {
        return length;
    }


    /**
     * Sets the length value for this List.
     * 
     * @param length The number of items (elements) in the list.
     */
    public void setLength(java.math.BigInteger length) {
        this.length = length;
    }


    /**
     * Gets the scalar value for this List.
     * 
     * @return scalar
     */
    public edu.duke.cabig.rproteomics.model.statml.Scalar[] getScalar() {
        return scalar;
    }


    /**
     * Sets the scalar value for this List.
     * 
     * @param scalar
     */
    public void setScalar(edu.duke.cabig.rproteomics.model.statml.Scalar[] scalar) {
        this.scalar = scalar;
    }

    public edu.duke.cabig.rproteomics.model.statml.Scalar getScalar(int i) {
        return this.scalar[i];
    }

    public void setScalar(int i, edu.duke.cabig.rproteomics.model.statml.Scalar _value) {
        this.scalar[i] = _value;
    }


    /**
     * Gets the list value for this List.
     * 
     * @return list
     */
    public edu.duke.cabig.rproteomics.model.statml.List getList() {
        return list;
    }


    /**
     * Sets the list value for this List.
     * 
     * @param list
     */
    public void setList(edu.duke.cabig.rproteomics.model.statml.List list) {
        this.list = list;
    }


    /**
     * Gets the aNull value for this List.
     * 
     * @return aNull
     */
    public edu.duke.cabig.rproteomics.model.statml._null[] getANull() {
        return aNull;
    }


    /**
     * Sets the aNull value for this List.
     * 
     * @param aNull
     */
    public void setANull(edu.duke.cabig.rproteomics.model.statml._null[] aNull) {
        this.aNull = aNull;
    }

    public edu.duke.cabig.rproteomics.model.statml._null getANull(int i) {
        return this.aNull[i];
    }

    public void setANull(int i, edu.duke.cabig.rproteomics.model.statml._null _value) {
        this.aNull[i] = _value;
    }


    /**
     * Gets the array value for this List.
     * 
     * @return array
     */
    public edu.duke.cabig.rproteomics.model.statml.Array[] getArray() {
        return array;
    }


    /**
     * Sets the array value for this List.
     * 
     * @param array
     */
    public void setArray(edu.duke.cabig.rproteomics.model.statml.Array[] array) {
        this.array = array;
    }

    public edu.duke.cabig.rproteomics.model.statml.Array getArray(int i) {
        return this.array[i];
    }

    public void setArray(int i, edu.duke.cabig.rproteomics.model.statml.Array _value) {
        this.array[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof List)) return false;
        List other = (List) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.length==null && other.getLength()==null) || 
             (this.length!=null &&
              this.length.equals(other.getLength()))) &&
            ((this.scalar==null && other.getScalar()==null) || 
             (this.scalar!=null &&
              java.util.Arrays.equals(this.scalar, other.getScalar()))) &&
            ((this.list==null && other.getList()==null) || 
             (this.list!=null &&
              this.list.equals(other.getList()))) &&
            ((this.aNull==null && other.getANull()==null) || 
             (this.aNull!=null &&
              java.util.Arrays.equals(this.aNull, other.getANull()))) &&
            ((this.array==null && other.getArray()==null) || 
             (this.array!=null &&
              java.util.Arrays.equals(this.array, other.getArray())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getLength() != null) {
            _hashCode += getLength().hashCode();
        }
        if (getScalar() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getScalar());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getScalar(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getList() != null) {
            _hashCode += getList().hashCode();
        }
        if (getANull() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getANull());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getANull(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getArray() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getArray());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getArray(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(List.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "List"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
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
        elemField.setFieldName("length");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "length"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("scalar");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "scalar"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "Scalar"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("list");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "List"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "List"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ANull");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "aNull"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "Null"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("array");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "array"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "Array"));
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
