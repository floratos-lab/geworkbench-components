/**
 * ExecuteRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs;

public class ExecuteRequest  implements java.io.Serializable {
    private edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.ExecuteRequestBioAssay bioAssay;
    private edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.ExecuteRequestHierarchicalClusteringParameter hierarchicalClusteringParameter;

    public ExecuteRequest() {
    }

    public ExecuteRequest(
           edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.ExecuteRequestBioAssay bioAssay,
           edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.ExecuteRequestHierarchicalClusteringParameter hierarchicalClusteringParameter) {
           this.bioAssay = bioAssay;
           this.hierarchicalClusteringParameter = hierarchicalClusteringParameter;
    }


    /**
     * Gets the bioAssay value for this ExecuteRequest.
     * 
     * @return bioAssay
     */
    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.ExecuteRequestBioAssay getBioAssay() {
        return bioAssay;
    }


    /**
     * Sets the bioAssay value for this ExecuteRequest.
     * 
     * @param bioAssay
     */
    public void setBioAssay(edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.ExecuteRequestBioAssay bioAssay) {
        this.bioAssay = bioAssay;
    }


    /**
     * Gets the hierarchicalClusteringParameter value for this ExecuteRequest.
     * 
     * @return hierarchicalClusteringParameter
     */
    public edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.ExecuteRequestHierarchicalClusteringParameter getHierarchicalClusteringParameter() {
        return hierarchicalClusteringParameter;
    }


    /**
     * Sets the hierarchicalClusteringParameter value for this ExecuteRequest.
     * 
     * @param hierarchicalClusteringParameter
     */
    public void setHierarchicalClusteringParameter(edu.columbia.geworkbench.cagrid.cluster.hierarchical.mage.stubs.ExecuteRequestHierarchicalClusteringParameter hierarchicalClusteringParameter) {
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
            ((this.bioAssay==null && other.getBioAssay()==null) || 
             (this.bioAssay!=null &&
              this.bioAssay.equals(other.getBioAssay()))) &&
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
        if (getBioAssay() != null) {
            _hashCode += getBioAssay().hashCode();
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
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClusteringMage", ">ExecuteRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bioAssay");
        elemField.setXmlName(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClusteringMage", "bioAssay"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClusteringMage", ">>ExecuteRequest>bioAssay"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hierarchicalClusteringParameter");
        elemField.setXmlName(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClusteringMage", "hierarchicalClusteringParameter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClusteringMage", ">>ExecuteRequest>hierarchicalClusteringParameter"));
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
