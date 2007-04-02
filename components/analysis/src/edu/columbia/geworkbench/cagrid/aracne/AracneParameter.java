/**
 * AracneParameter.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.aracne;

/**
 * Tuning parameters for the aracne algorithm.
 * <p>
 * accurracy - The algorithm can be set to either focus on speed or accuracy. Permissible values are either 'fast' or
 * 'accurate'.
 * </p>
 * <p>
 * kernelWidth - This determines how broad/narrow the distribution is of two genes on the same array. Perimissible
 * values are between [0,1].
 * </p>
 * <p>
 * mutualInformationThreshold - default: 0. The mutual information between genes will only be reported if the mutual
 * information is above this threshold.
 * </p>
 * <p>
 * pvalue - P-value for mutual information reconstruction.
 * </p>
 * <p>
 * tolerence - DPI tolerance between [0,1] .
 * </p>
 * <p>
 * hub - he probe id of the hub gene.
 * </p>
 * <p>
 * mutualInformationSteps - Number of bins to use when specifying the fast method.
 * </p>
 * <p>
 * condition - Conditional network reconstructs the network given a specified probe being most expressed or least
 * expressed. In the format that follows -c, probeId indicate the probe to be conditioned on; + or - specify whether the
 * upper or lower tail of the probe's expression should be used as the condition, and % is a percentage between (0, 1)
 * specifying the proportion of samples used as the conditioning subset. Example useage:-c +24 0.35, -c -1973_s_at 0.4
 * </p>
 * <p>
 * mean - Filter genes by mean. Specifically, only gene expression values above this mean will be considered.
 * </p>
 * <p>
 * coefficientOfVariance - Filters genes by the coefficient of variance (standard deviation/mean). Specifically, only
 * expression values above the coefficent of varience will be considered.
 * </p>
 * More information regarding ARACNE can be found at {@link http://wiki.c2b2.columbia.edu/workbench}.
 * 
 * @author keshav
 * @version $Id: AracneParameter.java,v 1.1 2007-04-02 14:57:04 keshav Exp $
 */
public class AracneParameter implements java.io.Serializable {
    /**
     * The algorithm can be set to either focus on speed or accuracy. Permissible values are either 'fast' or
     * 'accurate'.
     */
    private java.lang.String algorithm;
    /**
     * The kernel width of the algorithm. This determines how broad/narrow the distribution is of two genes on the same
     * array. Perimissible values are between [0,1].
     */
    private double kernelWidth;
    /**
     * MI threshold, default: 0. The mutual information between genes will only be reported if the mutual information is
     * above this threshold.
     */
    private double mutualInformationThreshold;
    /** P-value for mutual information reconstruction. */
    private double pvalue;
    /** DPI tolerance between [0,1] */
    private double tolerance;
    /** The probe id of the hub gene. */
    private java.lang.String hub;
    /** Number of bins to use when specifying the fast method. */
    private int mutualInformationSteps;
    /**
     * Conditional network reconstructs the network given a specified probe being most expressed or least expressed. In
     * the format that follows -c, probeId indicate the probe to be conditioned on; + or - specify whether the upper or
     * lower tail of the probe's expression should be used as the condition, and % is a percentage between (0, 1)
     * specifying the proportion of samples used as the conditioning subset. Example useage:-c +24 0.35, -c -1973_s_at
     * 0.4
     */
    private java.lang.String condition;
    /**
     * Filter genes by mean. Specifically, only gene expression values above this mean will be considered.
     */
    private double mean;
    /**
     * Filters genes by the coefficient of variance (standard deviation/mean). Specifically, only expression values
     * above the coefficent of varience will be considered.
     */
    private double coefficientOfVariance;

    public AracneParameter() {
    }

    public AracneParameter( java.lang.String algorithm, double coefficientOfVariance, java.lang.String condition,
            java.lang.String hub, double kernelWidth, double mean, int mutualInformationSteps,
            double mutualInformationThreshold, double pvalue, double tolerance ) {
        this.algorithm = algorithm;
        this.kernelWidth = kernelWidth;
        this.mutualInformationThreshold = mutualInformationThreshold;
        this.pvalue = pvalue;
        this.tolerance = tolerance;
        this.hub = hub;
        this.mutualInformationSteps = mutualInformationSteps;
        this.condition = condition;
        this.mean = mean;
        this.coefficientOfVariance = coefficientOfVariance;
    }

    /**
     * Gets the algorithm value for this AracneParameter.
     * 
     * @return algorithm The algorithm can be set to either focus on speed or accuracy. Permissible values are either
     *         'fast' or 'accurate'.
     */
    public java.lang.String getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the algorithm value for this AracneParameter.
     * 
     * @param algorithm The algorithm can be set to either focus on speed or accuracy. Permissible values are either
     *        'fast' or 'accurate'.
     */
    public void setAlgorithm( java.lang.String algorithm ) {
        this.algorithm = algorithm;
    }

    /**
     * Gets the kernelWidth value for this AracneParameter.
     * 
     * @return kernelWidth The kernel width of the algorithm. This determines how broad/narrow the distribution is of
     *         two genes on the same array. Perimissible values are between [0,1].
     */
    public double getKernelWidth() {
        return kernelWidth;
    }

    /**
     * Sets the kernelWidth value for this AracneParameter.
     * 
     * @param kernelWidth The kernel width of the algorithm. This determines how broad/narrow the distribution is of two
     *        genes on the same array. Perimissible values are between [0,1].
     */
    public void setKernelWidth( double kernelWidth ) {
        this.kernelWidth = kernelWidth;
    }

    /**
     * Gets the mutualInformationThreshold value for this AracneParameter.
     * 
     * @return mutualInformationThreshold MI threshold, default: 0. The mutual information between genes will only be
     *         reported if the mutual information is above this threshold.
     */
    public double getMutualInformationThreshold() {
        return mutualInformationThreshold;
    }

    /**
     * Sets the mutualInformationThreshold value for this AracneParameter.
     * 
     * @param mutualInformationThreshold MI threshold, default: 0. The mutual information between genes will only be
     *        reported if the mutual information is above this threshold.
     */
    public void setMutualInformationThreshold( double mutualInformationThreshold ) {
        this.mutualInformationThreshold = mutualInformationThreshold;
    }

    /**
     * Gets the pvalue value for this AracneParameter.
     * 
     * @return pvalue P-value for mutual information reconstruction.
     */
    public double getPvalue() {
        return pvalue;
    }

    /**
     * Sets the pvalue value for this AracneParameter.
     * 
     * @param pvalue P-value for mutual information reconstruction.
     */
    public void setPvalue( double pvalue ) {
        this.pvalue = pvalue;
    }

    /**
     * Gets the tolerance value for this AracneParameter.
     * 
     * @return tolerance DPI tolerance between [0,1]
     */
    public double getTolerance() {
        return tolerance;
    }

    /**
     * Sets the tolerance value for this AracneParameter.
     * 
     * @param tolerance DPI tolerance between [0,1]
     */
    public void setTolerance( double tolerance ) {
        this.tolerance = tolerance;
    }

    /**
     * Gets the hub value for this AracneParameter.
     * 
     * @return hub The probe id of the hub gene.
     */
    public java.lang.String getHub() {
        return hub;
    }

    /**
     * Sets the hub value for this AracneParameter.
     * 
     * @param hub The probe id of the hub gene.
     */
    public void setHub( java.lang.String hub ) {
        this.hub = hub;
    }

    /**
     * Gets the mutualInformationSteps value for this AracneParameter.
     * 
     * @return mutualInformationSteps Number of bins to use when specifying the fast method.
     */
    public int getMutualInformationSteps() {
        return mutualInformationSteps;
    }

    /**
     * Sets the mutualInformationSteps value for this AracneParameter.
     * 
     * @param mutualInformationSteps Number of bins to use when specifying the fast method.
     */
    public void setMutualInformationSteps( int mutualInformationSteps ) {
        this.mutualInformationSteps = mutualInformationSteps;
    }

    /**
     * Gets the condition value for this AracneParameter.
     * 
     * @return condition Conditional network reconstructs the network given a specified probe being most expressed or
     *         least expressed. In the format that follows -c, probeId indicate the probe to be conditioned on; + or -
     *         specify whether the upper or lower tail of the probe's expression should be used as the condition, and %
     *         is a percentage between (0, 1) specifying the proportion of samples used as the conditioning subset.
     *         Example useage:-c +24 0.35, -c -1973_s_at 0.4
     */
    public java.lang.String getCondition() {
        return condition;
    }

    /**
     * Sets the condition value for this AracneParameter.
     * 
     * @param condition Conditional network reconstructs the network given a specified probe being most expressed or
     *        least expressed. In the format that follows -c, probeId indicate the probe to be conditioned on; + or -
     *        specify whether the upper or lower tail of the probe's expression should be used as the condition, and %
     *        is a percentage between (0, 1) specifying the proportion of samples used as the conditioning subset.
     *        Example useage:-c +24 0.35, -c -1973_s_at 0.4
     */
    public void setCondition( java.lang.String condition ) {
        this.condition = condition;
    }

    /**
     * Gets the mean value for this AracneParameter.
     * 
     * @return mean Filter genes by mean. Specifically, only gene expression values above this mean will be considered.
     */
    public double getMean() {
        return mean;
    }

    /**
     * Sets the mean value for this AracneParameter.
     * 
     * @param mean Filter genes by mean. Specifically, only gene expression values above this mean will be considered.
     */
    public void setMean( double mean ) {
        this.mean = mean;
    }

    /**
     * Gets the coefficientOfVariance value for this AracneParameter.
     * 
     * @return coefficientOfVariance Filters genes by the coefficient of variance (standard deviation/mean).
     *         Specifically, only expression values above the coefficent of varience will be considered.
     */
    public double getCoefficientOfVariance() {
        return coefficientOfVariance;
    }

    /**
     * Sets the coefficientOfVariance value for this AracneParameter.
     * 
     * @param coefficientOfVariance Filters genes by the coefficient of variance (standard deviation/mean).
     *        Specifically, only expression values above the coefficent of varience will be considered.
     */
    public void setCoefficientOfVariance( double coefficientOfVariance ) {
        this.coefficientOfVariance = coefficientOfVariance;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals( java.lang.Object obj ) {
        if ( !( obj instanceof AracneParameter ) ) return false;
        AracneParameter other = ( AracneParameter ) obj;
        if ( obj == null ) return false;
        if ( this == obj ) return true;
        if ( __equalsCalc != null ) {
            return ( __equalsCalc == obj );
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                && ( ( this.algorithm == null && other.getAlgorithm() == null ) || ( this.algorithm != null && this.algorithm
                        .equals( other.getAlgorithm() ) ) )
                && this.kernelWidth == other.getKernelWidth()
                && this.mutualInformationThreshold == other.getMutualInformationThreshold()
                && this.pvalue == other.getPvalue()
                && this.tolerance == other.getTolerance()
                && ( ( this.hub == null && other.getHub() == null ) || ( this.hub != null && this.hub.equals( other
                        .getHub() ) ) )
                && this.mutualInformationSteps == other.getMutualInformationSteps()
                && ( ( this.condition == null && other.getCondition() == null ) || ( this.condition != null && this.condition
                        .equals( other.getCondition() ) ) ) && this.mean == other.getMean()
                && this.coefficientOfVariance == other.getCoefficientOfVariance();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;

    public synchronized int hashCode() {
        if ( __hashCodeCalc ) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if ( getAlgorithm() != null ) {
            _hashCode += getAlgorithm().hashCode();
        }
        _hashCode += new Double( getKernelWidth() ).hashCode();
        _hashCode += new Double( getMutualInformationThreshold() ).hashCode();
        _hashCode += new Double( getPvalue() ).hashCode();
        _hashCode += new Double( getTolerance() ).hashCode();
        if ( getHub() != null ) {
            _hashCode += getHub().hashCode();
        }
        _hashCode += getMutualInformationSteps();
        if ( getCondition() != null ) {
            _hashCode += getCondition().hashCode();
        }
        _hashCode += new Double( getMean() ).hashCode();
        _hashCode += new Double( getCoefficientOfVariance() ).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
            AracneParameter.class, true );

    static {
        typeDesc.setXmlType( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "AracneParameter" ) );
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "algorithm" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "algorithm" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "string" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "kernelWidth" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "kernelWidth" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "double" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "mutualInformationThreshold" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne",
                "mutualInformationThreshold" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "double" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "pvalue" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "pvalue" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "double" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "tolerance" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "tolerance" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "double" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "hub" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "hub" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "string" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "mutualInformationSteps" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne",
                "mutualInformationSteps" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "int" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "condition" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "condition" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "string" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "mean" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne", "mean" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "double" ) );
        typeDesc.addFieldDesc( elemField );
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName( "coefficientOfVariance" );
        elemField.setXmlName( new javax.xml.namespace.QName(
                "gme://cagrid.geworkbench.columbia.edu/1/edu.columbia.geworkbench.cagrid.aracne",
                "coefficientOfVariance" ) );
        elemField.setXmlType( new javax.xml.namespace.QName( "http://www.w3.org/2001/XMLSchema", "double" ) );
        typeDesc.addFieldDesc( elemField );
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
    public static org.apache.axis.encoding.Serializer getSerializer( java.lang.String mechType,
            java.lang.Class _javaType, javax.xml.namespace.QName _xmlType ) {
        return new org.apache.axis.encoding.ser.BeanSerializer( _javaType, _xmlType, typeDesc );
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer( java.lang.String mechType,
            java.lang.Class _javaType, javax.xml.namespace.QName _xmlType ) {
        return new org.apache.axis.encoding.ser.BeanDeserializer( _javaType, _xmlType, typeDesc );
    }

}
