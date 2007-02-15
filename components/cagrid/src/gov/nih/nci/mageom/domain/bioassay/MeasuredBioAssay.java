/**
 * MeasuredBioAssay.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package gov.nih.nci.mageom.domain.bioassay;


/**
 * A measured bioAssay is the direct processing of information in
 * a physical bioAssay by the featureExtraction event.  Often uses images
 * which are referenced through the physical bioAssay.
 */
public class MeasuredBioAssay  extends gov.nih.nci.mageom.domain.bioassay.BioAssay  implements java.io.Serializable {
    private gov.nih.nci.mageom.domain.bioassay.FeatureExtraction featureExtraction;

    public MeasuredBioAssay() {
    }

    public MeasuredBioAssay(
           gov.nih.nci.mageom.domain.bioassay.FeatureExtraction featureExtraction) {
           this.featureExtraction = featureExtraction;
    }


    /**
     * Gets the featureExtraction value for this MeasuredBioAssay.
     * 
     * @return featureExtraction
     */
    public gov.nih.nci.mageom.domain.bioassay.FeatureExtraction getFeatureExtraction() {
        return featureExtraction;
    }


    /**
     * Sets the featureExtraction value for this MeasuredBioAssay.
     * 
     * @param featureExtraction
     */
    public void setFeatureExtraction(gov.nih.nci.mageom.domain.bioassay.FeatureExtraction featureExtraction) {
        this.featureExtraction = featureExtraction;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof MeasuredBioAssay)) return false;
        MeasuredBioAssay other = (MeasuredBioAssay) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.featureExtraction==null && other.getFeatureExtraction()==null) || 
             (this.featureExtraction!=null &&
              this.featureExtraction.equals(other.getFeatureExtraction())));
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
        if (getFeatureExtraction() != null) {
            _hashCode += getFeatureExtraction().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(MeasuredBioAssay.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "MeasuredBioAssay"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("featureExtraction");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "featureExtraction"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "FeatureExtraction"));
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
