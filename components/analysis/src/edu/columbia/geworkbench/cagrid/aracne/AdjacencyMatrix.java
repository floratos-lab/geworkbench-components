/**
 * AdjacencyMatrix.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.aracne;


/**
 * The return type of the aracne algorithm.  The following example
 * illustrates how to interpret the results in this return type.  geneListA={"foo",
 * "foo","bar"} geneListB={"a", "b", "c"} mutualInformationValues{0.197,
 * 0.275, 0.331}  says, "the mutual information between 'foo' and 'a'
 * is 0.197, the mutual information between 'foo' and 'b' is 0.275, etc."
 * 
 * @author keshav
 * @version $Id: AdjacencyMatrix.java,v 1.1 2007-04-02 14:57:04 keshav Exp $
 */
public class AdjacencyMatrix  implements java.io.Serializable {
    /** Stores the mutual information values between the genes with the
 * equivalent indicies in geneListA and geneListB. */
    private double[] mutualInformationValues;
    /** A list of genes for which the mutual information is calculated
 * for with genes in geneListB with matching indicies. */
    private java.lang.String[] geneListA;
    /** A list of genes for which the mutual information is calculated
 * for with genes in geneListA with matching indicies. */
    private java.lang.String[] geneListB;

    public AdjacencyMatrix() {
    }

    public AdjacencyMatrix(
           java.lang.String[] geneListA,
           java.lang.String[] geneListB,
           double[] mutualInformationValues) {
           this.mutualInformationValues = mutualInformationValues;
           this.geneListA = geneListA;
           this.geneListB = geneListB;
    }


    /**
     * Gets the mutualInformationValues value for this AdjacencyMatrix.
     * 
     * @return mutualInformationValues Stores the mutual information values between the genes with the
 * equivalent indicies in geneListA and geneListB.
     */
    public double[] getMutualInformationValues() {
        return mutualInformationValues;
    }


    /**
     * Sets the mutualInformationValues value for this AdjacencyMatrix.
     * 
     * @param mutualInformationValues Stores the mutual information values between the genes with the
 * equivalent indicies in geneListA and geneListB.
     */
    public void setMutualInformationValues(double[] mutualInformationValues) {
        this.mutualInformationValues = mutualInformationValues;
    }

    public double getMutualInformationValues(int i) {
        return this.mutualInformationValues[i];
    }

    public void setMutualInformationValues(int i, double _value) {
        this.mutualInformationValues[i] = _value;
    }


    /**
     * Gets the geneListA value for this AdjacencyMatrix.
     * 
     * @return geneListA A list of genes for which the mutual information is calculated
 * for with genes in geneListB with matching indicies.
     */
    public java.lang.String[] getGeneListA() {
        return geneListA;
    }


    /**
     * Sets the geneListA value for this AdjacencyMatrix.
     * 
     * @param geneListA A list of genes for which the mutual information is calculated
 * for with genes in geneListB with matching indicies.
     */
    public void setGeneListA(java.lang.String[] geneListA) {
        this.geneListA = geneListA;
    }

    public java.lang.String getGeneListA(int i) {
        return this.geneListA[i];
    }

    public void setGeneListA(int i, java.lang.String _value) {
        this.geneListA[i] = _value;
    }


    /**
     * Gets the geneListB value for this AdjacencyMatrix.
     * 
     * @return geneListB A list of genes for which the mutual information is calculated
 * for with genes in geneListA with matching indicies.
     */
    public java.lang.String[] getGeneListB() {
        return geneListB;
    }


    /**
     * Sets the geneListB value for this AdjacencyMatrix.
     * 
     * @param geneListB A list of genes for which the mutual information is calculated
 * for with genes in geneListA with matching indicies.
     */
    public void setGeneListB(java.lang.String[] geneListB) {
        this.geneListB = geneListB;
    }

    public java.lang.String getGeneListB(int i) {
        return this.geneListB[i];
    }

    public void setGeneListB(int i, java.lang.String _value) {
        this.geneListB[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AdjacencyMatrix)) return false;
        AdjacencyMatrix other = (AdjacencyMatrix) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.mutualInformationValues==null && other.getMutualInformationValues()==null) || 
             (this.mutualInformationValues!=null &&
              java.util.Arrays.equals(this.mutualInformationValues, other.getMutualInformationValues()))) &&
            ((this.geneListA==null && other.getGeneListA()==null) || 
             (this.geneListA!=null &&
              java.util.Arrays.equals(this.geneListA, other.getGeneListA()))) &&
            ((this.geneListB==null && other.getGeneListB()==null) || 
             (this.geneListB!=null &&
              java.util.Arrays.equals(this.geneListB, other.getGeneListB())));
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
        if (getMutualInformationValues() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMutualInformationValues());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMutualInformationValues(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getGeneListA() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGeneListA());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGeneListA(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getGeneListB() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGeneListB());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGeneListB(), i);
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
        new org.apache.axis.description.TypeDesc(AdjacencyMatrix.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "AdjacencyMatrix"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mutualInformationValues");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "mutualInformationValues"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("geneListA");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "geneListA"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("geneListB");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "geneListB"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
