/**
 * ScalarTypeType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.duke.cabig.rproteomics.model.statml;

public class ScalarTypeType implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected ScalarTypeType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _value1 = "double";
    public static final java.lang.String _value2 = "float";
    public static final java.lang.String _value3 = "long";
    public static final java.lang.String _value4 = "integer";
    public static final java.lang.String _value5 = "short";
    public static final java.lang.String _value6 = "boolean";
    public static final java.lang.String _value7 = "character";
    public static final java.lang.String _value8 = "string";
    public static final ScalarTypeType value1 = new ScalarTypeType(_value1);
    public static final ScalarTypeType value2 = new ScalarTypeType(_value2);
    public static final ScalarTypeType value3 = new ScalarTypeType(_value3);
    public static final ScalarTypeType value4 = new ScalarTypeType(_value4);
    public static final ScalarTypeType value5 = new ScalarTypeType(_value5);
    public static final ScalarTypeType value6 = new ScalarTypeType(_value6);
    public static final ScalarTypeType value7 = new ScalarTypeType(_value7);
    public static final ScalarTypeType value8 = new ScalarTypeType(_value8);
    public java.lang.String getValue() { return _value_;}
    public static ScalarTypeType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        ScalarTypeType enumeration = (ScalarTypeType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static ScalarTypeType fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ScalarTypeType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://RProteomics.caBIG/2.0/edu.duke.cabig.rproteomics.model.statml", "scalarTypeType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
