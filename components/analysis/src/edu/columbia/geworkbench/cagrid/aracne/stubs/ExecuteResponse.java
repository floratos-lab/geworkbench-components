/**
 * ExecuteResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.aracne.stubs;

public class ExecuteResponse  implements java.io.Serializable {
    private edu.columbia.geworkbench.cagrid.aracne.AdjacencyMatrix adjacencyMatrix;

    public ExecuteResponse() {
    }

    public ExecuteResponse(
           edu.columbia.geworkbench.cagrid.aracne.AdjacencyMatrix adjacencyMatrix) {
           this.adjacencyMatrix = adjacencyMatrix;
    }


    /**
     * Gets the adjacencyMatrix value for this ExecuteResponse.
     * 
     * @return adjacencyMatrix
     */
    public edu.columbia.geworkbench.cagrid.aracne.AdjacencyMatrix getAdjacencyMatrix() {
        return adjacencyMatrix;
    }


    /**
     * Sets the adjacencyMatrix value for this ExecuteResponse.
     * 
     * @param adjacencyMatrix
     */
    public void setAdjacencyMatrix(edu.columbia.geworkbench.cagrid.aracne.AdjacencyMatrix adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ExecuteResponse)) return false;
        ExecuteResponse other = (ExecuteResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.adjacencyMatrix==null && other.getAdjacencyMatrix()==null) || 
             (this.adjacencyMatrix!=null &&
              this.adjacencyMatrix.equals(other.getAdjacencyMatrix())));
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
        if (getAdjacencyMatrix() != null) {
            _hashCode += getAdjacencyMatrix().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ExecuteResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/Aracne", ">ExecuteResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("adjacencyMatrix");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "AdjacencyMatrix"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "AdjacencyMatrix"));
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
