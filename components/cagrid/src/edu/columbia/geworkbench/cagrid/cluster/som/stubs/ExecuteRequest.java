/**
 * ExecuteRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.som.stubs;

public class ExecuteRequest  implements java.io.Serializable {
    private edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestMicroarraySet microarraySet;
    private edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestSomClusteringParameter somClusteringParameter;

    public ExecuteRequest() {
    }

    public ExecuteRequest(
           edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestMicroarraySet microarraySet,
           edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestSomClusteringParameter somClusteringParameter) {
           this.microarraySet = microarraySet;
           this.somClusteringParameter = somClusteringParameter;
    }


    /**
     * Gets the microarraySet value for this ExecuteRequest.
     * 
     * @return microarraySet
     */
    public edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestMicroarraySet getMicroarraySet() {
        return microarraySet;
    }


    /**
     * Sets the microarraySet value for this ExecuteRequest.
     * 
     * @param microarraySet
     */
    public void setMicroarraySet(edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestMicroarraySet microarraySet) {
        this.microarraySet = microarraySet;
    }


    /**
     * Gets the somClusteringParameter value for this ExecuteRequest.
     * 
     * @return somClusteringParameter
     */
    public edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestSomClusteringParameter getSomClusteringParameter() {
        return somClusteringParameter;
    }


    /**
     * Sets the somClusteringParameter value for this ExecuteRequest.
     * 
     * @param somClusteringParameter
     */
    public void setSomClusteringParameter(edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestSomClusteringParameter somClusteringParameter) {
        this.somClusteringParameter = somClusteringParameter;
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
            ((this.somClusteringParameter==null && other.getSomClusteringParameter()==null) || 
             (this.somClusteringParameter!=null &&
              this.somClusteringParameter.equals(other.getSomClusteringParameter())));
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
        if (getSomClusteringParameter() != null) {
            _hashCode += getSomClusteringParameter().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ExecuteRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/SomClustering", ">ExecuteRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("microarraySet");
        elemField.setXmlName(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/SomClustering", "microarraySet"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/SomClustering", ">>ExecuteRequest>microarraySet"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("somClusteringParameter");
        elemField.setXmlName(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/SomClustering", "somClusteringParameter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/SomClustering", ">>ExecuteRequest>somClusteringParameter"));
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
