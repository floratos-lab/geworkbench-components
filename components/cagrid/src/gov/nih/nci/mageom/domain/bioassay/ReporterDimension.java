/**
 * ReporterDimension.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package gov.nih.nci.mageom.domain.bioassay;


/**
 * Specialized DesignElementDimension to hold Reporters.
 */
public class ReporterDimension  extends gov.nih.nci.mageom.domain.bioassay.DesignElementDimension  implements java.io.Serializable {
    private gov.nih.nci.mageom.domain.bioassay.Reporter[] reporters;

    public ReporterDimension() {
    }

    public ReporterDimension(
           gov.nih.nci.mageom.domain.bioassay.Reporter[] reporters) {
           this.reporters = reporters;
    }


    /**
     * Gets the reporters value for this ReporterDimension.
     * 
     * @return reporters
     */
    public gov.nih.nci.mageom.domain.bioassay.Reporter[] getReporters() {
        return reporters;
    }


    /**
     * Sets the reporters value for this ReporterDimension.
     * 
     * @param reporters
     */
    public void setReporters(gov.nih.nci.mageom.domain.bioassay.Reporter[] reporters) {
        this.reporters = reporters;
    }

    public gov.nih.nci.mageom.domain.bioassay.Reporter getReporters(int i) {
        return this.reporters[i];
    }

    public void setReporters(int i, gov.nih.nci.mageom.domain.bioassay.Reporter _value) {
        this.reporters[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReporterDimension)) return false;
        ReporterDimension other = (ReporterDimension) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.reporters==null && other.getReporters()==null) || 
             (this.reporters!=null &&
              java.util.Arrays.equals(this.reporters, other.getReporters())));
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
        if (getReporters() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getReporters());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getReporters(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ReporterDimension.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "ReporterDimension"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reporters");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "reporters"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "Reporter"));
        elemField.setMinOccurs(0);
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
