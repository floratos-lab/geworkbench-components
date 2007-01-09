/**
 * SomClusteringParameter.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.som;


/**
 * Tuning parameters for a SOM clustering analysis.
 */
public class SomClusteringParameter  implements java.io.Serializable {
    /** The horizontal number of grid cells in the rectangular topology
 * of clusters */
    private int dim_x;
    /** The vertical number of grid cells in the rectangular topology of
 * clusters */
    private int dim_y;
    /** The function to be used represented by an integer.  Bubble=0, Gaussian=1. */
    private int function;
    /** The radius. */
    private float radius;
    /** Initial learning rate. */
    private float alpha;
    /** The number of iterations. */
    private int iteration;

    public SomClusteringParameter() {
    }

    public SomClusteringParameter(
           float alpha,
           int dim_x,
           int dim_y,
           int function,
           int iteration,
           float radius) {
           this.dim_x = dim_x;
           this.dim_y = dim_y;
           this.function = function;
           this.radius = radius;
           this.alpha = alpha;
           this.iteration = iteration;
    }


    /**
     * Gets the dim_x value for this SomClusteringParameter.
     * 
     * @return dim_x The horizontal number of grid cells in the rectangular topology
 * of clusters
     */
    public int getDim_x() {
        return dim_x;
    }


    /**
     * Sets the dim_x value for this SomClusteringParameter.
     * 
     * @param dim_x The horizontal number of grid cells in the rectangular topology
 * of clusters
     */
    public void setDim_x(int dim_x) {
        this.dim_x = dim_x;
    }


    /**
     * Gets the dim_y value for this SomClusteringParameter.
     * 
     * @return dim_y The vertical number of grid cells in the rectangular topology of
 * clusters
     */
    public int getDim_y() {
        return dim_y;
    }


    /**
     * Sets the dim_y value for this SomClusteringParameter.
     * 
     * @param dim_y The vertical number of grid cells in the rectangular topology of
 * clusters
     */
    public void setDim_y(int dim_y) {
        this.dim_y = dim_y;
    }


    /**
     * Gets the function value for this SomClusteringParameter.
     * 
     * @return function The function to be used represented by an integer.  Bubble=0, Gaussian=1.
     */
    public int getFunction() {
        return function;
    }


    /**
     * Sets the function value for this SomClusteringParameter.
     * 
     * @param function The function to be used represented by an integer.  Bubble=0, Gaussian=1.
     */
    public void setFunction(int function) {
        this.function = function;
    }


    /**
     * Gets the radius value for this SomClusteringParameter.
     * 
     * @return radius The radius.
     */
    public float getRadius() {
        return radius;
    }


    /**
     * Sets the radius value for this SomClusteringParameter.
     * 
     * @param radius The radius.
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }


    /**
     * Gets the alpha value for this SomClusteringParameter.
     * 
     * @return alpha Initial learning rate.
     */
    public float getAlpha() {
        return alpha;
    }


    /**
     * Sets the alpha value for this SomClusteringParameter.
     * 
     * @param alpha Initial learning rate.
     */
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }


    /**
     * Gets the iteration value for this SomClusteringParameter.
     * 
     * @return iteration The number of iterations.
     */
    public int getIteration() {
        return iteration;
    }


    /**
     * Sets the iteration value for this SomClusteringParameter.
     * 
     * @param iteration The number of iterations.
     */
    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SomClusteringParameter)) return false;
        SomClusteringParameter other = (SomClusteringParameter) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.dim_x == other.getDim_x() &&
            this.dim_y == other.getDim_y() &&
            this.function == other.getFunction() &&
            this.radius == other.getRadius() &&
            this.alpha == other.getAlpha() &&
            this.iteration == other.getIteration();
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
        _hashCode += getDim_x();
        _hashCode += getDim_y();
        _hashCode += getFunction();
        _hashCode += new Float(getRadius()).hashCode();
        _hashCode += new Float(getAlpha()).hashCode();
        _hashCode += getIteration();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SomClusteringParameter.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "SomClusteringParameter"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dim_x");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "dim_x"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dim_y");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "dim_y"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("function");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "function"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("radius");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "radius"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alpha");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "alpha"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("iteration");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "iteration"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
