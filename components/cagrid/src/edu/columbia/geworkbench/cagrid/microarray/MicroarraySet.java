/**
 * MicroarraySet.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.microarray;

/**
 * A group of microarrays. This gives you the two dimensional data set typically encountered in microarray analyses.
 * 
 * @author keshav
 * @version $Id: MicroarraySet.java,v 1.2 2007-01-04 22:03:15 watkinson Exp $
 */
public class MicroarraySet implements java.io.Serializable {
    private java.lang.String name;
    private edu.columbia.geworkbench.cagrid.microarray.Microarray[] microarray;
    private edu.columbia.geworkbench.cagrid.microarray.Marker[] marker;

    public MicroarraySet() {
    }

    public MicroarraySet( edu.columbia.geworkbench.cagrid.microarray.Marker[] marker,
            edu.columbia.geworkbench.cagrid.microarray.Microarray[] microarray, java.lang.String name ) {
        this.name = name;
        this.microarray = microarray;
        this.marker = marker;
    }

    /**
     * Gets the name value for this MicroarraySet.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this MicroarraySet.
     * 
     * @param name
     */
    public void setName( java.lang.String name ) {
        this.name = name;
    }

    /**
     * Gets the microarray value for this MicroarraySet.
     * 
     * @return microarray
     */
    public edu.columbia.geworkbench.cagrid.microarray.Microarray[] getMicroarray() {
        return microarray;
    }

    /**
     * Sets the microarray value for this MicroarraySet.
     * 
     * @param microarray
     */
    public void setMicroarray( edu.columbia.geworkbench.cagrid.microarray.Microarray[] microarray ) {
        this.microarray = microarray;
    }

    public edu.columbia.geworkbench.cagrid.microarray.Microarray getMicroarray( int i ) {
        return this.microarray[i];
    }

    public void setMicroarray( int i, edu.columbia.geworkbench.cagrid.microarray.Microarray _value ) {
        this.microarray[i] = _value;
    }

    /**
     * Gets the marker value for this MicroarraySet.
     * 
     * @return marker
     */
    public edu.columbia.geworkbench.cagrid.microarray.Marker[] getMarker() {
        return marker;
    }

    /**
     * Sets the marker value for this MicroarraySet.
     * 
     * @param marker
     */
    public void setMarker( edu.columbia.geworkbench.cagrid.microarray.Marker[] marker ) {
        this.marker = marker;
    }

    public edu.columbia.geworkbench.cagrid.microarray.Marker getMarker( int i ) {
        return this.marker[i];
    }

    public void setMarker( int i, edu.columbia.geworkbench.cagrid.microarray.Marker _value ) {
        this.marker[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals( java.lang.Object obj ) {
        if ( !( obj instanceof MicroarraySet ) ) return false;
        MicroarraySet other = ( MicroarraySet ) obj;
        if ( obj == null ) return false;
        if ( this == obj ) return true;
        if ( __equalsCalc != null ) {
            return ( __equalsCalc == obj );
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                && ( ( this.name == null && other.getName() == null ) || ( this.name != null && this.name.equals( other
                        .getName() ) ) )
                && ( ( this.microarray == null && other.getMicroarray() == null ) || ( this.microarray != null && java.util.Arrays
                        .equals( this.microarray, other.getMicroarray() ) ) )
                && ( ( this.marker == null && other.getMarker() == null ) || ( this.marker != null && java.util.Arrays
                        .equals( this.marker, other.getMarker() ) ) );
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
        if ( getName() != null ) {
            _hashCode += getName().hashCode();
        }
        if ( getMicroarray() != null ) {
            for ( int i = 0; i < java.lang.reflect.Array.getLength( getMicroarray() ); i++ ) {
                java.lang.Object obj = java.lang.reflect.Array.get( getMicroarray(), i );
                if ( obj != null && !obj.getClass().isArray() ) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if ( getMarker() != null ) {
            for ( int i = 0; i < java.lang.reflect.Array.getLength( getMarker() ); i++ ) {
                java.lang.Object obj = java.lang.reflect.Array.get( getMarker(), i );
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
            MicroarraySet.class, true );

    static {
        typeDesc
                .setXmlType( new javax.xml.namespace.QName(
                        "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray",
                        "MicroarraySet" ) );
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "name" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray", "name" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "string" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "microarray" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray", "Microarray" ) );
        elemField.setXmlType( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray", "Microarray" ) );
        elemField.setMinOccurs( 0 );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "marker" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray", "Marker" ) );
        elemField.setXmlType( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.microarray", "Marker" ) );
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
