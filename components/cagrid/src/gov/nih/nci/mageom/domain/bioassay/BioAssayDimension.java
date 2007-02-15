/**
 * BioAssayDimension.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package gov.nih.nci.mageom.domain.bioassay;


/**
 * An ordered list of bioAssays.
 */
public class BioAssayDimension  extends gov.nih.nci.mageom.domain.bioassay.Identifiable  implements java.io.Serializable {
    private gov.nih.nci.mageom.domain.bioassay.BioAssay[] bioAssays;

    public BioAssayDimension() {
    }

    public BioAssayDimension(
           gov.nih.nci.mageom.domain.bioassay.BioAssay[] bioAssays) {
           this.bioAssays = bioAssays;
    }


    /**
     * Gets the bioAssays value for this BioAssayDimension.
     * 
     * @return bioAssays
     */
    public gov.nih.nci.mageom.domain.bioassay.BioAssay[] getBioAssays() {
        return bioAssays;
    }


    /**
     * Sets the bioAssays value for this BioAssayDimension.
     * 
     * @param bioAssays
     */
    public void setBioAssays(gov.nih.nci.mageom.domain.bioassay.BioAssay[] bioAssays) {
        this.bioAssays = bioAssays;
    }

    public gov.nih.nci.mageom.domain.bioassay.BioAssay getBioAssays(int i) {
        return this.bioAssays[i];
    }

    public void setBioAssays(int i, gov.nih.nci.mageom.domain.bioassay.BioAssay _value) {
        this.bioAssays[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BioAssayDimension)) return false;
        BioAssayDimension other = (BioAssayDimension) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.bioAssays==null && other.getBioAssays()==null) || 
             (this.bioAssays!=null &&
              java.util.Arrays.equals(this.bioAssays, other.getBioAssays())));
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
        if (getBioAssays() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getBioAssays());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getBioAssays(), i);
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
        new org.apache.axis.description.TypeDesc(BioAssayDimension.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "BioAssayDimension"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bioAssays");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "bioAssays"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "BioAssay"));
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
