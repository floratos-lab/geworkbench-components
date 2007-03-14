/**
 * SomCluster.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.som;


/**
 * Self Organized Maps type to be returned by the SomClustering service.
 */
public class SomCluster  implements java.io.Serializable {
    /** The x coordinates of each item in the som rectangular grid. */
    private int[] xCoordinate;
    /** The y coordinates of each item in the som rectangular grid. */
    private int[] yCoordinate;
    /** The names of each item in the som rectangular grid. */
    private java.lang.String[] name;
    /** The width of the grid. */
    private int width;
    /** The height of the grid. */
    private int height;

    public SomCluster() {
    }

    public SomCluster(
           int height,
           java.lang.String[] name,
           int width,
           int[] xCoordinate,
           int[] yCoordinate) {
           this.xCoordinate = xCoordinate;
           this.yCoordinate = yCoordinate;
           this.name = name;
           this.width = width;
           this.height = height;
    }


    /**
     * Gets the xCoordinate value for this SomCluster.
     * 
     * @return xCoordinate The x coordinates of each item in the som rectangular grid.
     */
    public int[] getXCoordinate() {
        return xCoordinate;
    }


    /**
     * Sets the xCoordinate value for this SomCluster.
     * 
     * @param xCoordinate The x coordinates of each item in the som rectangular grid.
     */
    public void setXCoordinate(int[] xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public int getXCoordinate(int i) {
        return this.xCoordinate[i];
    }

    public void setXCoordinate(int i, int _value) {
        this.xCoordinate[i] = _value;
    }


    /**
     * Gets the yCoordinate value for this SomCluster.
     * 
     * @return yCoordinate The y coordinates of each item in the som rectangular grid.
     */
    public int[] getYCoordinate() {
        return yCoordinate;
    }


    /**
     * Sets the yCoordinate value for this SomCluster.
     * 
     * @param yCoordinate The y coordinates of each item in the som rectangular grid.
     */
    public void setYCoordinate(int[] yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public int getYCoordinate(int i) {
        return this.yCoordinate[i];
    }

    public void setYCoordinate(int i, int _value) {
        this.yCoordinate[i] = _value;
    }


    /**
     * Gets the name value for this SomCluster.
     * 
     * @return name The names of each item in the som rectangular grid.
     */
    public java.lang.String[] getName() {
        return name;
    }


    /**
     * Sets the name value for this SomCluster.
     * 
     * @param name The names of each item in the som rectangular grid.
     */
    public void setName(java.lang.String[] name) {
        this.name = name;
    }

    public java.lang.String getName(int i) {
        return this.name[i];
    }

    public void setName(int i, java.lang.String _value) {
        this.name[i] = _value;
    }


    /**
     * Gets the width value for this SomCluster.
     * 
     * @return width The width of the grid.
     */
    public int getWidth() {
        return width;
    }


    /**
     * Sets the width value for this SomCluster.
     * 
     * @param width The width of the grid.
     */
    public void setWidth(int width) {
        this.width = width;
    }


    /**
     * Gets the height value for this SomCluster.
     * 
     * @return height The height of the grid.
     */
    public int getHeight() {
        return height;
    }


    /**
     * Sets the height value for this SomCluster.
     * 
     * @param height The height of the grid.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SomCluster)) return false;
        SomCluster other = (SomCluster) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.xCoordinate==null && other.getXCoordinate()==null) || 
             (this.xCoordinate!=null &&
              java.util.Arrays.equals(this.xCoordinate, other.getXCoordinate()))) &&
            ((this.yCoordinate==null && other.getYCoordinate()==null) || 
             (this.yCoordinate!=null &&
              java.util.Arrays.equals(this.yCoordinate, other.getYCoordinate()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              java.util.Arrays.equals(this.name, other.getName()))) &&
            this.width == other.getWidth() &&
            this.height == other.getHeight();
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
        if (getXCoordinate() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getXCoordinate());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getXCoordinate(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getYCoordinate() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getYCoordinate());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getYCoordinate(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getName() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getName());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getName(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += getWidth();
        _hashCode += getHeight();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SomCluster.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "SomCluster"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("XCoordinate");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "xCoordinate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("YCoordinate");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "yCoordinate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("width");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "width"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("height");
        elemField.setXmlName(new javax.xml.namespace.QName("gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.cluster.som", "height"));
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
