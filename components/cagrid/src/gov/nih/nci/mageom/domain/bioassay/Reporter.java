/**
 * Reporter.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package gov.nih.nci.mageom.domain.bioassay;


/**
 * A Design Element that represents some biological material (clone,
 * oligo, etc.) on an array which will report on some biosequence or
 * biosequences.  The derived data from the measured data of its Features
 * represents the presence or absence of the biosequence or biosequences
 * it is reporting on in the BioAssay.  Reporters are Identifiable and
 * several Features on the same array can be mapped to the same reporter
 * as can Features from a different ArrayDesign.  The granularity of
 * the Reporters independence is dependent on the technology and the
 * intent of the ArrayDesign.  Oligos using mature technologies can in
 * general be assumed to be safely replicated on many features where
 * as with PCR Products there might be the desire for quality assurence
 * to make reporters one to one with features and use the mappings to
 * CompositeSequences for replication purposes.
 */
public class Reporter  extends gov.nih.nci.mageom.domain.bioassay.DesignElement  implements java.io.Serializable {

    public Reporter() {
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Reporter)) return false;
        Reporter other = (Reporter) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj);
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Reporter.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "Reporter"));
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
