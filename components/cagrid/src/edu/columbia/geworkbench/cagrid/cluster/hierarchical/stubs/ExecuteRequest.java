/**
 * ExecuteRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs;

public class ExecuteRequest  implements java.io.Serializable {
    private edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestMicroarraySet microarraySet;
    private edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestHierarchicalClusteringParameter hierarchicalClusteringParameter;

    public ExecuteRequest() {
    }

    public ExecuteRequest(
           edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestHierarchicalClusteringParameter hierarchicalClusteringParameter,
           edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestMicroarraySet microarraySet) {
           this.microarraySet = microarraySet;
           this.hierarchicalClusteringParameter = hierarchicalClusteringParameter;
    }


    /**
     * Gets the microarraySet value for this ExecuteRequest.
     * 
     * @return microarraySet
     */
    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestMicroarraySet getMicroarraySet() {
        return microarraySet;
    }


    /**
     * Sets the microarraySet value for this ExecuteRequest.
     * 
     * @param microarraySet
     */
    public void setMicroarraySet(edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestMicroarraySet microarraySet) {
        this.microarraySet = microarraySet;
    }


    /**
     * Gets the hierarchicalClusteringParameter value for this ExecuteRequest.
     * 
     * @return hierarchicalClusteringParameter
     */
    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestHierarchicalClusteringParameter getHierarchicalClusteringParameter() {
        return hierarchicalClusteringParameter;
    }


    /**
     * Sets the hierarchicalClusteringParameter value for this ExecuteRequest.
     * 
     * @param hierarchicalClusteringParameter
     */
    public void setHierarchicalClusteringParameter(edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestHierarchicalClusteringParameter hierarchicalClusteringParameter) {
        this.hierarchicalClusteringParameter = hierarchicalClusteringParameter;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ExecuteRequest)) return false;
        ExecuteRequest other = (ExecuteRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.microarraySet==null && other.getMicroarraySet()==null) || 
             (this.microarraySet!=null &&
              this.microarraySet.equals(other.getMicroarraySet()))) &&
            ((this.hierarchicalClusteringParameter==null && other.getHierarchicalClusteringParameter()==null) || 
             (this.hierarchicalClusteringParameter!=null &&
              this.hierarchicalClusteringParameter.equals(other.getHierarchicalClusteringParameter())));
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
        if (getMicroarraySet() != null) {
            _hashCode += getMicroarraySet().hashCode();
        }
        if (getHierarchicalClusteringParameter() != null) {
            _hashCode += getHierarchicalClusteringParameter().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ExecuteRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClustering", ">ExecuteRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("microarraySet");
        elemField.setXmlName(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClustering", "microarraySet"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClustering", ">>ExecuteRequest>microarraySet"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hierarchicalClusteringParameter");
        elemField.setXmlName(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClustering", "hierarchicalClusteringParameter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClustering", ">>ExecuteRequest>hierarchicalClusteringParameter"));
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
