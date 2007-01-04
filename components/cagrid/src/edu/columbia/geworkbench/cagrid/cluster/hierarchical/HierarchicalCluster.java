/**
 * HierarchicalClustering.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.hierarchical;

public class HierarchicalCluster implements java.io.Serializable {
    private java.lang.String name;
    private edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode microarrayCluster;
    private edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode markerCluster;

    public HierarchicalCluster() {
    }

    public HierarchicalCluster(
            edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode markerCluster,
            edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode microarrayCluster,
            java.lang.String name ) {
        this.name = name;
        this.microarrayCluster = microarrayCluster;
        this.markerCluster = markerCluster;
    }

    /**
     * Gets the name value for this HierarchicalCluster.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this HierarchicalCluster.
     * 
     * @param name
     */
    public void setName( java.lang.String name ) {
        this.name = name;
    }

    /**
     * Gets the microarrayCluster value for this HierarchicalCluster.
     * 
     * @return microarrayCluster
     */
    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode getMicroarrayCluster() {
        return microarrayCluster;
    }

    /**
     * Sets the microarrayCluster value for this HierarchicalCluster.
     * 
     * @param microarrayCluster
     */
    public void setMicroarrayCluster(
            edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode microarrayCluster ) {
        this.microarrayCluster = microarrayCluster;
    }

    /**
     * Gets the markerCluster value for this HierarchicalCluster.
     * 
     * @return markerCluster
     */
    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode getMarkerCluster() {
        return markerCluster;
    }

    /**
     * Sets the markerCluster value for this HierarchicalCluster.
     * 
     * @param markerCluster
     */
    public void setMarkerCluster(
            edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode markerCluster ) {
        this.markerCluster = markerCluster;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals( java.lang.Object obj ) {
        if ( !( obj instanceof HierarchicalCluster ) ) return false;
        HierarchicalCluster other = ( HierarchicalCluster ) obj;
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
                && ( ( this.microarrayCluster == null && other.getMicroarrayCluster() == null ) || ( this.microarrayCluster != null && this.microarrayCluster
                        .equals( other.getMicroarrayCluster() ) ) )
                && ( ( this.markerCluster == null && other.getMarkerCluster() == null ) || ( this.markerCluster != null && this.markerCluster
                        .equals( other.getMarkerCluster() ) ) );
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
        if ( getMicroarrayCluster() != null ) {
            _hashCode += getMicroarrayCluster().hashCode();
        }
        if ( getMarkerCluster() != null ) {
            _hashCode += getMarkerCluster().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
            HierarchicalCluster.class, true );

    static {
        typeDesc.setXmlType( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster",
                "HierarchicalClustering" ) );
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "name" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster", "name" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "string" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "microarrayCluster" );
        elemField
                .setXmlName( new javax.xml.namespace.QName(
                        "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster",
                        "microarrayCluster" ) );
        elemField.setXmlType( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster",
                "HierarchicalClusterNode" ) );
        elemField.setMinOccurs( 0 );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "markerCluster" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster", "markerCluster" ) );
        elemField.setXmlType( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster",
                "HierarchicalClusterNode" ) );
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
