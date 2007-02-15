/**
 * Identifiable.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package gov.nih.nci.mageom.domain.bioassay;


/**
 * An Identifiable class is one that has an unambiguous reference
 * within the scope.  It also has a potentially ambiguous name.
 */
public abstract class Identifiable  extends gov.nih.nci.mageom.domain.bioassay.Describable  implements java.io.Serializable {
    /** The potentially ambiguous common identifier. */
    private java.lang.String name;
    /** An identifier is an unambiguous string that is unique within the
 * scope (i.e. a document, a set of related documents, or a repository)
 * of its use. */
    private java.lang.String identifier;

    public Identifiable() {
    }

    public Identifiable(
           java.lang.String identifier,
           java.lang.String name) {
           this.name = name;
           this.identifier = identifier;
    }


    /**
     * Gets the name value for this Identifiable.
     * 
     * @return name The potentially ambiguous common identifier.
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this Identifiable.
     * 
     * @param name The potentially ambiguous common identifier.
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the identifier value for this Identifiable.
     * 
     * @return identifier An identifier is an unambiguous string that is unique within the
 * scope (i.e. a document, a set of related documents, or a repository)
 * of its use.
     */
    public java.lang.String getIdentifier() {
        return identifier;
    }


    /**
     * Sets the identifier value for this Identifiable.
     * 
     * @param identifier An identifier is an unambiguous string that is unique within the
 * scope (i.e. a document, a set of related documents, or a repository)
 * of its use.
     */
    public void setIdentifier(java.lang.String identifier) {
        this.identifier = identifier;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Identifiable)) return false;
        Identifiable other = (Identifiable) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.identifier==null && other.getIdentifier()==null) || 
             (this.identifier!=null &&
              this.identifier.equals(other.getIdentifier())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getIdentifier() != null) {
            _hashCode += getIdentifier().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Identifiable.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://caArray.caBIG/1.1/gov.nih.nci.mageom.domain.BioAssay", "Identifiable"));
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
