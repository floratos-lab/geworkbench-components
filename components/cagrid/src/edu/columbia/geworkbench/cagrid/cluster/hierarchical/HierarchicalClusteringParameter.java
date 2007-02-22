/**
 * HierarchicalClusteringParameter.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.hierarchical;


/**
 * Tuning Parameters for the Hierarchical Clustering algorithm.
 */
public class HierarchicalClusteringParameter  implements java.io.Serializable {
    private edu.columbia.geworkbench.cagrid.cluster.hierarchical.Dim dim;
    private edu.columbia.geworkbench.cagrid.cluster.hierarchical.Distance distance;
    private edu.columbia.geworkbench.cagrid.cluster.hierarchical.Method method;

    public HierarchicalClusteringParameter() {
    }

    public HierarchicalClusteringParameter(
           edu.columbia.geworkbench.cagrid.cluster.hierarchical.Dim dim,
           edu.columbia.geworkbench.cagrid.cluster.hierarchical.Distance distance,
           edu.columbia.geworkbench.cagrid.cluster.hierarchical.Method method) {
           this.dim = dim;
           this.distance = distance;
           this.method = method;
    }


    /**
     * Gets the dim value for this HierarchicalClusteringParameter.
     * 
     * @return dim
     */
    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.Dim getDim() {
        return dim;
    }


    /**
     * Sets the dim value for this HierarchicalClusteringParameter.
     * 
     * @param dim
     */
    public void setDim(edu.columbia.geworkbench.cagrid.cluster.hierarchical.Dim dim) {
        this.dim = dim;
    }


    /**
     * Gets the distance value for this HierarchicalClusteringParameter.
     * 
     * @return distance
     */
    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.Distance getDistance() {
        return distance;
    }


    /**
     * Sets the distance value for this HierarchicalClusteringParameter.
     * 
     * @param distance
     */
    public void setDistance(edu.columbia.geworkbench.cagrid.cluster.hierarchical.Distance distance) {
        this.distance = distance;
    }


    /**
     * Gets the method value for this HierarchicalClusteringParameter.
     * 
     * @return method
     */
    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.Method getMethod() {
        return method;
    }


    /**
     * Sets the method value for this HierarchicalClusteringParameter.
     * 
     * @param method
     */
    public void setMethod(edu.columbia.geworkbench.cagrid.cluster.hierarchical.Method method) {
        this.method = method;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof HierarchicalClusteringParameter)) return false;
        HierarchicalClusteringParameter other = (HierarchicalClusteringParameter) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.dim==null && other.getDim()==null) || 
             (this.dim!=null &&
              this.dim.equals(other.getDim()))) &&
            ((this.distance==null && other.getDistance()==null) || 
             (this.distance!=null &&
              this.distance.equals(other.getDistance()))) &&
            ((this.method==null && other.getMethod()==null) || 
             (this.method!=null &&
              this.method.equals(other.getMethod())));
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
        if (getDim() != null) {
            _hashCode += getDim().hashCode();
        }
        if (getDistance() != null) {
            _hashCode += getDistance().hashCode();
        }
        if (getMethod() != null) {
            _hashCode += getMethod().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(HierarchicalClusteringParameter.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.hierarchical", "HierarchicalClusteringParameter"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dim");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.hierarchical", "dim"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.hierarchical", "dim"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("distance");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.hierarchical", "distance"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.hierarchical", "distance"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("method");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.hierarchical", "method"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.hierarchical", "method"));
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
