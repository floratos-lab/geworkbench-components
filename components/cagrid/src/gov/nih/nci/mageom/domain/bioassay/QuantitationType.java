/**
 * QuantitationType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package gov.nih.nci.mageom.domain.bioassay;


/**
 * A method for calculating a single datum of the matrix (e.g. raw
 * intensity, background, error).
 */
public abstract class QuantitationType  extends gov.nih.nci.mageom.domain.bioassay.Identifiable  implements java.io.Serializable {
    /** Indicates whether the quantitation has been measured from the background
 * or from the feature itself. */
    private boolean isBackground;

    public QuantitationType() {
    }

    public QuantitationType(
           boolean isBackground) {
           this.isBackground = isBackground;
    }


    /**
     * Gets the isBackground value for this QuantitationType.
     * 
     * @return isBackground Indicates whether the quantitation has been measured from the background
 * or from the feature itself.
     */
    public boolean isIsBackground() {
        return isBackground;
    }


    /**
     * Sets the isBackground value for this QuantitationType.
     * 
     * @param isBackground Indicates whether the quantitation has been measured from the background
 * or from the feature itself.
     */
    public void setIsBackground(boolean isBackground) {
        this.isBackground = isBackground;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof QuantitationType)) return false;
        QuantitationType other = (QuantitationType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            this.isBackground == other.isIsBackground();
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
        _hashCode += (isIsBackground() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(QuantitationType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "QuantitationType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isBackground");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "isBackground"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
