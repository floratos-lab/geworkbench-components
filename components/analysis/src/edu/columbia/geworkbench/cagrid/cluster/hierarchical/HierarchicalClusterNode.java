/**
 * HierarchicalClusterNode.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.hierarchical;

/**
 * @author keshav
 * @version $Id: HierarchicalClusterNode.java,v 1.1 2007-03-14 20:30:06 keshav Exp $
 */
public class HierarchicalClusterNode implements java.io.Serializable {
    private double height;
    private int depth;
    private java.lang.String leafLabel;
    private edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode[] hierarchicalClusterNode;

    public HierarchicalClusterNode() {
    }

    public HierarchicalClusterNode( String leafLabel ) {
        this.leafLabel = leafLabel;
    }

    public HierarchicalClusterNode( HierarchicalClusterNode left, HierarchicalClusterNode right, float height ) {
        if ( left != null && right != null ) {
            depth = Math.max( left.getDepth(), right.getDepth() ) + 1;
        }
        this.height = height;
        hierarchicalClusterNode = new HierarchicalClusterNode[] { left, right };
    }

    public HierarchicalClusterNode( int depth, double height,
            edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode[] hierarchicalClusterNode,
            java.lang.String leafLabel ) {
        this.height = height;
        this.depth = depth;
        this.leafLabel = leafLabel;
        this.hierarchicalClusterNode = hierarchicalClusterNode;
    }

    /**
     * Gets the height value for this HierarchicalClusterNode.
     * 
     * @return height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the height value for this HierarchicalClusterNode.
     * 
     * @param height
     */
    public void setHeight( double height ) {
        this.height = height;
    }

    /**
     * Gets the depth value for this HierarchicalClusterNode.
     * 
     * @return depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Sets the depth value for this HierarchicalClusterNode.
     * 
     * @param depth
     */
    public void setDepth( int depth ) {
        this.depth = depth;
    }

    /**
     * Gets the leafLabel value for this HierarchicalClusterNode.
     * 
     * @return leafLabel
     */
    public java.lang.String getLeafLabel() {
        return leafLabel;
    }

    /**
     * Sets the leafLabel value for this HierarchicalClusterNode.
     * 
     * @param leafLabel
     */
    public void setLeafLabel( java.lang.String leafLabel ) {
        this.leafLabel = leafLabel;
    }

    public boolean isLeaf() {
        return leafLabel != null;
    }

    /**
     * Gets the hierarchicalClusterNode value for this HierarchicalClusterNode.
     * 
     * @return hierarchicalClusterNode
     */
    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode[] getHierarchicalClusterNode() {
        return hierarchicalClusterNode;
    }

    /**
     * Sets the hierarchicalClusterNode value for this HierarchicalClusterNode.
     * 
     * @param hierarchicalClusterNode
     */
    public void setHierarchicalClusterNode(
            edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode[] hierarchicalClusterNode ) {
        this.hierarchicalClusterNode = hierarchicalClusterNode;
    }

    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode getHierarchicalClusterNode(
            int i ) {
        return this.hierarchicalClusterNode[i];
    }

    public void setHierarchicalClusterNode( int i,
            edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode _value ) {
        this.hierarchicalClusterNode[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals( java.lang.Object obj ) {
        if ( !( obj instanceof HierarchicalClusterNode ) ) return false;
        HierarchicalClusterNode other = ( HierarchicalClusterNode ) obj;
        if ( obj == null ) return false;
        if ( this == obj ) return true;
        if ( __equalsCalc != null ) {
            return ( __equalsCalc == obj );
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                && this.height == other.getHeight()
                && this.depth == other.getDepth()
                && ( ( this.leafLabel == null && other.getLeafLabel() == null ) || ( this.leafLabel != null && this.leafLabel
                        .equals( other.getLeafLabel() ) ) )
                && ( ( this.hierarchicalClusterNode == null && other.getHierarchicalClusterNode() == null ) || ( this.hierarchicalClusterNode != null && java.util.Arrays
                        .equals( this.hierarchicalClusterNode, other.getHierarchicalClusterNode() ) ) );
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
        _hashCode += new Double( getHeight() ).hashCode();
        _hashCode += getDepth();
        if ( getLeafLabel() != null ) {
            _hashCode += getLeafLabel().hashCode();
        }
        if ( getHierarchicalClusterNode() != null ) {
            for ( int i = 0; i < java.lang.reflect.Array.getLength( getHierarchicalClusterNode() ); i++ ) {
                java.lang.Object obj = java.lang.reflect.Array.get( getHierarchicalClusterNode(), i );
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
            HierarchicalClusterNode.class, true );

    static {
        typeDesc.setXmlType( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster",
                "HierarchicalClusterNode" ) );
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "height" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster", "height" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "double" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "depth" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster", "depth" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "int" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "leafLabel" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster", "leafLabel" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "string" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "hierarchicalClusterNode" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster",
                "HierarchicalClusterNode" ) );
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
