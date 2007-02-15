/**
 * BioAssayData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package gov.nih.nci.mageom.domain.bioassay;


/**
 * Represents the dataset created when the BioAssays are created.
 * BioAssayData is the entry point to the values.  Because the actual
 * values are represented by a different object, BioDataValues, which
 * can be memory intensive, the annotation of the transformation can
 * be gotten separate from the data.
 */
public abstract class BioAssayData  extends gov.nih.nci.mageom.domain.bioassay.Identifiable  implements java.io.Serializable {
    private gov.nih.nci.mageom.domain.bioassay.DesignElementDimension designElementDimension;
    private gov.nih.nci.mageom.domain.bioassay.BioAssayDimension bioAssayDimension;
    private gov.nih.nci.mageom.domain.bioassay.BioDataValues bioDataValues;
    private gov.nih.nci.mageom.domain.bioassay.QuantitationTypeDimension quantitationTypeDimension;

    public BioAssayData() {
    }

    public BioAssayData(
           gov.nih.nci.mageom.domain.bioassay.BioAssayDimension bioAssayDimension,
           gov.nih.nci.mageom.domain.bioassay.BioDataValues bioDataValues,
           gov.nih.nci.mageom.domain.bioassay.DesignElementDimension designElementDimension,
           gov.nih.nci.mageom.domain.bioassay.QuantitationTypeDimension quantitationTypeDimension) {
           this.designElementDimension = designElementDimension;
           this.bioAssayDimension = bioAssayDimension;
           this.bioDataValues = bioDataValues;
           this.quantitationTypeDimension = quantitationTypeDimension;
    }


    /**
     * Gets the designElementDimension value for this BioAssayData.
     * 
     * @return designElementDimension
     */
    public gov.nih.nci.mageom.domain.bioassay.DesignElementDimension getDesignElementDimension() {
        return designElementDimension;
    }


    /**
     * Sets the designElementDimension value for this BioAssayData.
     * 
     * @param designElementDimension
     */
    public void setDesignElementDimension(gov.nih.nci.mageom.domain.bioassay.DesignElementDimension designElementDimension) {
        this.designElementDimension = designElementDimension;
    }


    /**
     * Gets the bioAssayDimension value for this BioAssayData.
     * 
     * @return bioAssayDimension
     */
    public gov.nih.nci.mageom.domain.bioassay.BioAssayDimension getBioAssayDimension() {
        return bioAssayDimension;
    }


    /**
     * Sets the bioAssayDimension value for this BioAssayData.
     * 
     * @param bioAssayDimension
     */
    public void setBioAssayDimension(gov.nih.nci.mageom.domain.bioassay.BioAssayDimension bioAssayDimension) {
        this.bioAssayDimension = bioAssayDimension;
    }


    /**
     * Gets the bioDataValues value for this BioAssayData.
     * 
     * @return bioDataValues
     */
    public gov.nih.nci.mageom.domain.bioassay.BioDataValues getBioDataValues() {
        return bioDataValues;
    }


    /**
     * Sets the bioDataValues value for this BioAssayData.
     * 
     * @param bioDataValues
     */
    public void setBioDataValues(gov.nih.nci.mageom.domain.bioassay.BioDataValues bioDataValues) {
        this.bioDataValues = bioDataValues;
    }


    /**
     * Gets the quantitationTypeDimension value for this BioAssayData.
     * 
     * @return quantitationTypeDimension
     */
    public gov.nih.nci.mageom.domain.bioassay.QuantitationTypeDimension getQuantitationTypeDimension() {
        return quantitationTypeDimension;
    }


    /**
     * Sets the quantitationTypeDimension value for this BioAssayData.
     * 
     * @param quantitationTypeDimension
     */
    public void setQuantitationTypeDimension(gov.nih.nci.mageom.domain.bioassay.QuantitationTypeDimension quantitationTypeDimension) {
        this.quantitationTypeDimension = quantitationTypeDimension;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BioAssayData)) return false;
        BioAssayData other = (BioAssayData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.designElementDimension==null && other.getDesignElementDimension()==null) || 
             (this.designElementDimension!=null &&
              this.designElementDimension.equals(other.getDesignElementDimension()))) &&
            ((this.bioAssayDimension==null && other.getBioAssayDimension()==null) || 
             (this.bioAssayDimension!=null &&
              this.bioAssayDimension.equals(other.getBioAssayDimension()))) &&
            ((this.bioDataValues==null && other.getBioDataValues()==null) || 
             (this.bioDataValues!=null &&
              this.bioDataValues.equals(other.getBioDataValues()))) &&
            ((this.quantitationTypeDimension==null && other.getQuantitationTypeDimension()==null) || 
             (this.quantitationTypeDimension!=null &&
              this.quantitationTypeDimension.equals(other.getQuantitationTypeDimension())));
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
        if (getDesignElementDimension() != null) {
            _hashCode += getDesignElementDimension().hashCode();
        }
        if (getBioAssayDimension() != null) {
            _hashCode += getBioAssayDimension().hashCode();
        }
        if (getBioDataValues() != null) {
            _hashCode += getBioDataValues().hashCode();
        }
        if (getQuantitationTypeDimension() != null) {
            _hashCode += getQuantitationTypeDimension().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(BioAssayData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "BioAssayData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("designElementDimension");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "designElementDimension"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "DesignElementDimension"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bioAssayDimension");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "bioAssayDimension"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "BioAssayDimension"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bioDataValues");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "bioDataValues"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "BioDataValues"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("quantitationTypeDimension");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "quantitationTypeDimension"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "QuantitationTypeDimension"));
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
