/**
 * QuantitationTypeDimension.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package gov.nih.nci.mageom.domain.bioassay;


/**
 * An ordered list of quantitationTypes.
 */
public class QuantitationTypeDimension  implements java.io.Serializable {
    /** The potentially ambiguous common identifier. */
    private java.lang.String name;
    /** An identifier is an unambiguous string that is unique within the
 * scope (i.e. a document, a set of related documents, or a repository)
 * of its use. */
    private java.lang.String identifier;
    /** The potentially ambiguous common identifier. */
    private java.lang.String name2;
    /** An identifier is an unambiguous string that is unique within the
 * scope (i.e. a document, a set of related documents, or a repository)
 * of its use. */
    private java.lang.String identifier2;
    private gov.nih.nci.mageom.domain.bioassay.QuantitationType[] quantitationTypes;

    public QuantitationTypeDimension() {
    }

    public QuantitationTypeDimension(
           java.lang.String identifier,
           java.lang.String identifier2,
           java.lang.String name,
           java.lang.String name2,
           gov.nih.nci.mageom.domain.bioassay.QuantitationType[] quantitationTypes) {
           this.name = name;
           this.identifier = identifier;
           this.name2 = name2;
           this.identifier2 = identifier2;
           this.quantitationTypes = quantitationTypes;
    }


    /**
     * Gets the name value for this QuantitationTypeDimension.
     * 
     * @return name The potentially ambiguous common identifier.
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this QuantitationTypeDimension.
     * 
     * @param name The potentially ambiguous common identifier.
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the identifier value for this QuantitationTypeDimension.
     * 
     * @return identifier An identifier is an unambiguous string that is unique within the
 * scope (i.e. a document, a set of related documents, or a repository)
 * of its use.
     */
    public java.lang.String getIdentifier() {
        return identifier;
    }


    /**
     * Sets the identifier value for this QuantitationTypeDimension.
     * 
     * @param identifier An identifier is an unambiguous string that is unique within the
 * scope (i.e. a document, a set of related documents, or a repository)
 * of its use.
     */
    public void setIdentifier(java.lang.String identifier) {
        this.identifier = identifier;
    }


    /**
     * Gets the name2 value for this QuantitationTypeDimension.
     * 
     * @return name2 The potentially ambiguous common identifier.
     */
    public java.lang.String getName2() {
        return name2;
    }


    /**
     * Sets the name2 value for this QuantitationTypeDimension.
     * 
     * @param name2 The potentially ambiguous common identifier.
     */
    public void setName2(java.lang.String name2) {
        this.name2 = name2;
    }


    /**
     * Gets the identifier2 value for this QuantitationTypeDimension.
     * 
     * @return identifier2 An identifier is an unambiguous string that is unique within the
 * scope (i.e. a document, a set of related documents, or a repository)
 * of its use.
     */
    public java.lang.String getIdentifier2() {
        return identifier2;
    }


    /**
     * Sets the identifier2 value for this QuantitationTypeDimension.
     * 
     * @param identifier2 An identifier is an unambiguous string that is unique within the
 * scope (i.e. a document, a set of related documents, or a repository)
 * of its use.
     */
    public void setIdentifier2(java.lang.String identifier2) {
        this.identifier2 = identifier2;
    }


    /**
     * Gets the quantitationTypes value for this QuantitationTypeDimension.
     * 
     * @return quantitationTypes
     */
    public gov.nih.nci.mageom.domain.bioassay.QuantitationType[] getQuantitationTypes() {
        return quantitationTypes;
    }


    /**
     * Sets the quantitationTypes value for this QuantitationTypeDimension.
     * 
     * @param quantitationTypes
     */
    public void setQuantitationTypes(gov.nih.nci.mageom.domain.bioassay.QuantitationType[] quantitationTypes) {
        this.quantitationTypes = quantitationTypes;
    }

    public gov.nih.nci.mageom.domain.bioassay.QuantitationType getQuantitationTypes(int i) {
        return this.quantitationTypes[i];
    }

    public void setQuantitationTypes(int i, gov.nih.nci.mageom.domain.bioassay.QuantitationType _value) {
        this.quantitationTypes[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof QuantitationTypeDimension)) return false;
        QuantitationTypeDimension other = (QuantitationTypeDimension) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.identifier==null && other.getIdentifier()==null) || 
             (this.identifier!=null &&
              this.identifier.equals(other.getIdentifier()))) &&
            ((this.name2==null && other.getName2()==null) || 
             (this.name2!=null &&
              this.name2.equals(other.getName2()))) &&
            ((this.identifier2==null && other.getIdentifier2()==null) || 
             (this.identifier2!=null &&
              this.identifier2.equals(other.getIdentifier2()))) &&
            ((this.quantitationTypes==null && other.getQuantitationTypes()==null) || 
             (this.quantitationTypes!=null &&
              java.util.Arrays.equals(this.quantitationTypes, other.getQuantitationTypes())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getIdentifier() != null) {
            _hashCode += getIdentifier().hashCode();
        }
        if (getName2() != null) {
            _hashCode += getName2().hashCode();
        }
        if (getIdentifier2() != null) {
            _hashCode += getIdentifier2().hashCode();
        }
        if (getQuantitationTypes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getQuantitationTypes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getQuantitationTypes(), i);
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
        new org.apache.axis.description.TypeDesc(QuantitationTypeDimension.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "QuantitationTypeDimension"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("identifier");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "identifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("identifier");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "identifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("quantitationTypes");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "quantitationTypes"));
        elemField.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "QuantitationType"));
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
