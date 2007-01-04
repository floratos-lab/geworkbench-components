/**
 * Marker.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.microarray;

/**
 * The marker, sometimes referred to as gene, or design element (mage) or probe set (affymetrix).
 * 
 * @author keshav
 * @version $Id: Marker.java,v 1.2 2007-01-04 22:03:15 watkinson Exp $
 */
public class Marker implements java.io.Serializable {
    /** The marker name. */
    private java.lang.String markerName;
    private float[] markerData;

    public Marker() {
    }

    public Marker( float[] markerData, java.lang.String markerName ) {
        this.markerName = markerName;
        this.markerData = markerData;
    }

    /**
     * Gets the markerName value for this Marker.
     * 
     * @return markerName The marker name.
     */
    public java.lang.String getMarkerName() {
        return markerName;
    }

    /**
     * Sets the markerName value for this Marker.
     * 
     * @param markerName The marker name.
     */
    public void setMarkerName( java.lang.String markerName ) {
        this.markerName = markerName;
    }

    /**
     * Gets the markerData value for this Marker.
     * 
     * @return markerData
     */
    public float[] getMarkerData() {
        return markerData;
    }

    /**
     * Sets the markerData value for this Marker.
     * 
     * @param markerData
     */
    public void setMarkerData( float[] markerData ) {
        this.markerData = markerData;
    }

    public float getMarkerData( int i ) {
        return this.markerData[i];
    }

    public void setMarkerData( int i, float _value ) {
        this.markerData[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals( java.lang.Object obj ) {
        if ( !( obj instanceof Marker ) ) return false;
        Marker other = ( Marker ) obj;
        if ( obj == null ) return false;
        if ( this == obj ) return true;
        if ( __equalsCalc != null ) {
            return ( __equalsCalc == obj );
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                && ( ( this.markerName == null && other.getMarkerName() == null ) || ( this.markerName != null && this.markerName
                        .equals( other.getMarkerName() ) ) )
                && ( ( this.markerData == null && other.getMarkerData() == null ) || ( this.markerData != null && java.util.Arrays
                        .equals( this.markerData, other.getMarkerData() ) ) );
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
        if ( getMarkerName() != null ) {
            _hashCode += getMarkerName().hashCode();
        }
        if ( getMarkerData() != null ) {
            for ( int i = 0; i < java.lang.reflect.Array.getLength( getMarkerData() ); i++ ) {
                java.lang.Object obj = java.lang.reflect.Array.get( getMarkerData(), i );
                if ( obj != null && !obj.getClass().isArray() ) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
            Marker.class, true );

    static {
        typeDesc.setXmlType( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray", "Marker" ) );
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "markerName" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray", "markerName" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "string" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "markerData" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray", "markerData" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "float" ) );
        elemField.setMinOccurs( 0 );
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
