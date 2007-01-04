/**
 * HierarchicalClusteringParameter.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.hierarchical;

/**
 * Tuning parameters for a hierarchical clustering analysis.
 * 
 * @author keshav
 * @version $Id: HierarchicalClusteringParameter.java,v 1.2 2007-01-04 22:03:15 watkinson Exp $
 */
public class HierarchicalClusteringParameter implements java.io.Serializable {
    /** Single, average, or total (complete) linkage. */
    private java.lang.String method;
    /** Cluster by marker, microarray, or both. */
    private java.lang.String dim;
    /** Distance metric. Can be euclidean, pearson, or spearman */
    private java.lang.String distance;

    public HierarchicalClusteringParameter() {
    }

    public HierarchicalClusteringParameter( java.lang.String dim, java.lang.String distance, java.lang.String method ) {
        this.method = method;
        this.dim = dim;
        this.distance = distance;
    }

    /**
     * Gets the method value for this HierarchicalClusteringParameter.
     * 
     * @return method Single, average, or total (complete) linkage.
     */
    public java.lang.String getMethod() {
        return method;
    }

    /**
     * Sets the method value for this HierarchicalClusteringParameter.
     * 
     * @param method Single, average, or total (complete) linkage.
     */
    public void setMethod( java.lang.String method ) {
        this.method = method;
    }

    /**
     * Gets the dim value for this HierarchicalClusteringParameter.
     * 
     * @return dim Cluster by marker, microarray, or both.
     */
    public java.lang.String getDim() {
        return dim;
    }

    /**
     * Sets the dim value for this HierarchicalClusteringParameter.
     * 
     * @param dim Cluster by marker, microarray, or both.
     */
    public void setDim( java.lang.String dim ) {
        this.dim = dim;
    }

    /**
     * Gets the distance value for this HierarchicalClusteringParameter.
     * 
     * @return distance Distance metric. Can be euclidean, pearson, or spearman
     */
    public java.lang.String getDistance() {
        return distance;
    }

    /**
     * Sets the distance value for this HierarchicalClusteringParameter.
     * 
     * @param distance Distance metric. Can be euclidean, pearson, or spearman
     */
    public void setDistance( java.lang.String distance ) {
        this.distance = distance;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals( java.lang.Object obj ) {
        if ( !( obj instanceof HierarchicalClusteringParameter ) ) return false;
        HierarchicalClusteringParameter other = ( HierarchicalClusteringParameter ) obj;
        if ( obj == null ) return false;
        if ( this == obj ) return true;
        if ( __equalsCalc != null ) {
            return ( __equalsCalc == obj );
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                && ( ( this.method == null && other.getMethod() == null ) || ( this.method != null && this.method
                        .equals( other.getMethod() ) ) )
                && ( ( this.dim == null && other.getDim() == null ) || ( this.dim != null && this.dim.equals( other
                        .getDim() ) ) )
                && ( ( this.distance == null && other.getDistance() == null ) || ( this.distance != null && this.distance
                        .equals( other.getDistance() ) ) );
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
        if ( getMethod() != null ) {
            _hashCode += getMethod().hashCode();
        }
        if ( getDim() != null ) {
            _hashCode += getDim().hashCode();
        }
        if ( getDistance() != null ) {
            _hashCode += getDistance().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
            HierarchicalClusteringParameter.class, true );

    static {
        typeDesc.setXmlType( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.hierarchical",
                "HierarchicalClusteringParameter" ) );
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "method" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.hierarchical",
                "method" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "string" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "dim" );
        elemField
                .setXmlName( new javax.xml.namespace.QName(
                        "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.hierarchical",
                        "dim" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "string" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "distance" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.hierarchical",
                "distance" ) );
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
