/**
 * FeatureExtraction.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package gov.nih.nci.mageom.domain.bioassay;


/**
 * The process by which data is extracted from an image producing
 * a measuredBioAssayData and a measuredBioAssay.
 */
public class FeatureExtraction  implements java.io.Serializable {
    private gov.nih.nci.mageom.domain.bioassay.PhysicalBioAssay physicalBioAssaySource;

    public FeatureExtraction() {
    }

    public FeatureExtraction(
           gov.nih.nci.mageom.domain.bioassay.PhysicalBioAssay physicalBioAssaySource) {
           this.physicalBioAssaySource = physicalBioAssaySource;
    }


    /**
     * Gets the physicalBioAssaySource value for this FeatureExtraction.
     * 
     * @return physicalBioAssaySource
     */
    public gov.nih.nci.mageom.domain.bioassay.PhysicalBioAssay getPhysicalBioAssaySource() {
        return physicalBioAssaySource;
    }


    /**
     * Sets the physicalBioAssaySource value for this FeatureExtraction.
     * 
     * @param physicalBioAssaySource
     */
    public void setPhysicalBioAssaySource(gov.nih.nci.mageom.domain.bioassay.PhysicalBioAssay physicalBioAssaySource) {
        this.physicalBioAssaySource = physicalBioAssaySource;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FeatureExtraction)) return false;
        FeatureExtraction other = (FeatureExtraction) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.physicalBioAssaySource==null && other.getPhysicalBioAssaySource()==null) || 
             (this.physicalBioAssaySource!=null &&
              this.physicalBioAssaySource.equals(other.getPhysicalBioAssaySource())));
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
        if (getPhysicalBioAssaySource() != null) {
            _hashCode += getPhysicalBioAssaySource().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(FeatureExtraction.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "FeatureExtraction"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("physicalBioAssaySource");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "physicalBioAssaySource"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "PhysicalBioAssay"));
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
