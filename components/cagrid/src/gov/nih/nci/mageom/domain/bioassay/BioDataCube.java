/**
 * BioDataCube.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package gov.nih.nci.mageom.domain.bioassay;


/**
 * A three-dimensional cube representation of the data.
 */
public class BioDataCube  extends gov.nih.nci.mageom.domain.bioassay.BioDataValues  implements java.io.Serializable {
    /** Three dimension array, indexed by the three dimensions to provide
 * the data for the BioAssayData. */
    private java.lang.String cube;
    /** The order to expect the dimension. The enumeration uses the first
 * letter of the three dimensions to represent the six possible orderings. */
    private java.lang.String order;

    public BioDataCube() {
    }

    public BioDataCube(
           java.lang.String cube,
           java.lang.String order) {
           this.cube = cube;
           this.order = order;
    }


    /**
     * Gets the cube value for this BioDataCube.
     * 
     * @return cube Three dimension array, indexed by the three dimensions to provide
 * the data for the BioAssayData.
     */
    public java.lang.String getCube() {
        return cube;
    }


    /**
     * Sets the cube value for this BioDataCube.
     * 
     * @param cube Three dimension array, indexed by the three dimensions to provide
 * the data for the BioAssayData.
     */
    public void setCube(java.lang.String cube) {
        this.cube = cube;
    }


    /**
     * Gets the order value for this BioDataCube.
     * 
     * @return order The order to expect the dimension. The enumeration uses the first
 * letter of the three dimensions to represent the six possible orderings.
     */
    public java.lang.String getOrder() {
        return order;
    }


    /**
     * Sets the order value for this BioDataCube.
     * 
     * @param order The order to expect the dimension. The enumeration uses the first
 * letter of the three dimensions to represent the six possible orderings.
     */
    public void setOrder(java.lang.String order) {
        this.order = order;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BioDataCube)) return false;
        BioDataCube other = (BioDataCube) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.cube==null && other.getCube()==null) || 
             (this.cube!=null &&
              this.cube.equals(other.getCube()))) &&
            ((this.order==null && other.getOrder()==null) || 
             (this.order!=null &&
              this.order.equals(other.getOrder())));
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
        if (getCube() != null) {
            _hashCode += getCube().hashCode();
        }
        if (getOrder() != null) {
            _hashCode += getOrder().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(BioDataCube.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "BioDataCube"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("cube");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "cube"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("order");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "order"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
