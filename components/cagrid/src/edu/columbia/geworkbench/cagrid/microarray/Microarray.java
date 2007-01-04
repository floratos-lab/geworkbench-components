/**
 * Microarray.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.microarray;

/**
 * A microarray object.
 * 
 * @author keshav
 * @version $Id: Microarray.java,v 1.2 2007-01-04 22:03:15 watkinson Exp $
 */
public class Microarray implements java.io.Serializable {
    /**
     * The array data, typically thought of as a column in a two dimensional matrix.
     */
    private float[] arrayData;
    /** The microarray name */
    private java.lang.String arrayName;

    public Microarray() {
    }

    public Microarray( float[] arrayData, java.lang.String arrayName ) {
        this.arrayData = arrayData;
        this.arrayName = arrayName;
    }

    /**
     * Gets the arrayData value for this Microarray.
     * 
     * @return arrayData The array data, typically thought of as a column in a two dimensional matrix.
     */
    public float[] getArrayData() {
        return arrayData;
    }

    /**
     * Sets the arrayData value for this Microarray.
     * 
     * @param arrayData The array data, typically thought of as a column in a two dimensional matrix.
     */
    public void setArrayData( float[] arrayData ) {
        this.arrayData = arrayData;
    }

    public float getArrayData( int i ) {
        return this.arrayData[i];
    }

    public void setArrayData( int i, float _value ) {
        this.arrayData[i] = _value;
    }

    /**
     * Gets the arrayName value for this Microarray.
     * 
     * @return arrayName The microarray name
     */
    public java.lang.String getArrayName() {
        return arrayName;
    }

    /**
     * Sets the arrayName value for this Microarray.
     * 
     * @param arrayName The microarray name
     */
    public void setArrayName( java.lang.String arrayName ) {
        this.arrayName = arrayName;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals( java.lang.Object obj ) {
        if ( !( obj instanceof Microarray ) ) return false;
        Microarray other = ( Microarray ) obj;
        if ( obj == null ) return false;
        if ( this == obj ) return true;
        if ( __equalsCalc != null ) {
            return ( __equalsCalc == obj );
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                && ( ( this.arrayData == null && other.getArrayData() == null ) || ( this.arrayData != null && java.util.Arrays
                        .equals( this.arrayData, other.getArrayData() ) ) )
                && ( ( this.arrayName == null && other.getArrayName() == null ) || ( this.arrayName != null && this.arrayName
                        .equals( other.getArrayName() ) ) );
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;

    public synchronized int hashCode() {
        if ( __hashCodeCalc ) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if ( getArrayData() != null ) {
            for ( int i = 0; i < java.lang.reflect.Array.getLength( getArrayData() ); i++ ) {
                java.lang.Object obj = java.lang.reflect.Array.get( getArrayData(), i );
                if ( obj != null && !obj.getClass().isArray() ) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if ( getArrayName() != null ) {
            _hashCode += getArrayName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
            Microarray.class, true );

    static {
        typeDesc.setXmlType( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray", "Microarray" ) );
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "arrayData" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray", "arrayData" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "float" ) );
        elemField.setMinOccurs( 0 );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "arrayName" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray", "arrayName" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "string" ) );
        typeDesc.addFieldDesc( elemField );
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
    public static org.apache.axis.encoding.Serializer getSerializer( java.lang.String mechType,
            java.lang.Class _javaType, javax.xml.namespace.QName _xmlType ) {
        return new org.apache.axis.encoding.ser.BeanSerializer( _javaType, _xmlType, typeDesc );
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer( java.lang.String mechType,
            java.lang.Class _javaType, javax.xml.namespace.QName _xmlType ) {
        return new org.apache.axis.encoding.ser.BeanDeserializer( _javaType, _xmlType, typeDesc );
    }

}
