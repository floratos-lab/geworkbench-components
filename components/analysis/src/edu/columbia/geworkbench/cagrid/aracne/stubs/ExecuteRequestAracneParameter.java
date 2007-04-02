/**
 * ExecuteRequestAracneParameter.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.aracne.stubs;

public class ExecuteRequestAracneParameter  implements java.io.Serializable {
    private edu.columbia.geworkbench.cagrid.aracne.AracneParameter aracneParameter;

    public ExecuteRequestAracneParameter() {
    }

    public ExecuteRequestAracneParameter(
           edu.columbia.geworkbench.cagrid.aracne.AracneParameter aracneParameter) {
           this.aracneParameter = aracneParameter;
    }


    /**
     * Gets the aracneParameter value for this ExecuteRequestAracneParameter.
     * 
     * @return aracneParameter
     */
    public edu.columbia.geworkbench.cagrid.aracne.AracneParameter getAracneParameter() {
        return aracneParameter;
    }


    /**
     * Sets the aracneParameter value for this ExecuteRequestAracneParameter.
     * 
     * @param aracneParameter
     */
    public void setAracneParameter(edu.columbia.geworkbench.cagrid.aracne.AracneParameter aracneParameter) {
        this.aracneParameter = aracneParameter;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ExecuteRequestAracneParameter)) return false;
        ExecuteRequestAracneParameter other = (ExecuteRequestAracneParameter) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.aracneParameter==null && other.getAracneParameter()==null) || 
             (this.aracneParameter!=null &&
              this.aracneParameter.equals(other.getAracneParameter())));
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
        if (getAracneParameter() != null) {
            _hashCode += getAracneParameter().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ExecuteRequestAracneParameter.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/Aracne", ">>ExecuteRequest>aracneParameter"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aracneParameter");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "AracneParameter"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "AracneParameter"));
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
