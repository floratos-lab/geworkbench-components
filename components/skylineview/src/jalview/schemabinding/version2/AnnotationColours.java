/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: AnnotationColours.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
 */

package jalview.schemabinding.version2;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * Class AnnotationColours.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
 */
public class AnnotationColours implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _aboveThreshold
     */
    private int _aboveThreshold;

    /**
     * keeps track of state for field: _aboveThreshold
     */
    private boolean _has_aboveThreshold;

    /**
     * Field _annotation
     */
    private java.lang.String _annotation;

    /**
     * Field _minColour
     */
    private int _minColour;

    /**
     * keeps track of state for field: _minColour
     */
    private boolean _has_minColour;

    /**
     * Field _maxColour
     */
    private int _maxColour;

    /**
     * keeps track of state for field: _maxColour
     */
    private boolean _has_maxColour;

    /**
     * Field _colourScheme
     */
    private java.lang.String _colourScheme;

    /**
     * Field _threshold
     */
    private float _threshold;

    /**
     * keeps track of state for field: _threshold
     */
    private boolean _has_threshold;


      //----------------/
     //- Constructors -/
    //----------------/

    public AnnotationColours() {
        super();
    } //-- jalview.schemabinding.version2.AnnotationColours()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteAboveThreshold
     * 
     */
    public void deleteAboveThreshold()
    {
        this._has_aboveThreshold= false;
    } //-- void deleteAboveThreshold() 

    /**
     * Method deleteMaxColour
     * 
     */
    public void deleteMaxColour()
    {
        this._has_maxColour= false;
    } //-- void deleteMaxColour() 

    /**
     * Method deleteMinColour
     * 
     */
    public void deleteMinColour()
    {
        this._has_minColour= false;
    } //-- void deleteMinColour() 

    /**
     * Method deleteThreshold
     * 
     */
    public void deleteThreshold()
    {
        this._has_threshold= false;
    } //-- void deleteThreshold() 

    /**
     * Returns the value of field 'aboveThreshold'.
     * 
     * @return int
     * @return the value of field 'aboveThreshold'.
     */
    public int getAboveThreshold()
    {
        return this._aboveThreshold;
    } //-- int getAboveThreshold() 

    /**
     * Returns the value of field 'annotation'.
     * 
     * @return String
     * @return the value of field 'annotation'.
     */
    public java.lang.String getAnnotation()
    {
        return this._annotation;
    } //-- java.lang.String getAnnotation() 

    /**
     * Returns the value of field 'colourScheme'.
     * 
     * @return String
     * @return the value of field 'colourScheme'.
     */
    public java.lang.String getColourScheme()
    {
        return this._colourScheme;
    } //-- java.lang.String getColourScheme() 

    /**
     * Returns the value of field 'maxColour'.
     * 
     * @return int
     * @return the value of field 'maxColour'.
     */
    public int getMaxColour()
    {
        return this._maxColour;
    } //-- int getMaxColour() 

    /**
     * Returns the value of field 'minColour'.
     * 
     * @return int
     * @return the value of field 'minColour'.
     */
    public int getMinColour()
    {
        return this._minColour;
    } //-- int getMinColour() 

    /**
     * Returns the value of field 'threshold'.
     * 
     * @return float
     * @return the value of field 'threshold'.
     */
    public float getThreshold()
    {
        return this._threshold;
    } //-- float getThreshold() 

    /**
     * Method hasAboveThreshold
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasAboveThreshold()
    {
        return this._has_aboveThreshold;
    } //-- boolean hasAboveThreshold() 

    /**
     * Method hasMaxColour
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasMaxColour()
    {
        return this._has_maxColour;
    } //-- boolean hasMaxColour() 

    /**
     * Method hasMinColour
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasMinColour()
    {
        return this._has_minColour;
    } //-- boolean hasMinColour() 

    /**
     * Method hasThreshold
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasThreshold()
    {
        return this._has_threshold;
    } //-- boolean hasThreshold() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param out
     */
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, (java.io.Writer)handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'aboveThreshold'.
     * 
     * @param aboveThreshold the value of field 'aboveThreshold'.
     */
    public void setAboveThreshold(int aboveThreshold)
    {
        this._aboveThreshold = aboveThreshold;
        this._has_aboveThreshold = true;
    } //-- void setAboveThreshold(int) 

    /**
     * Sets the value of field 'annotation'.
     * 
     * @param annotation the value of field 'annotation'.
     */
    public void setAnnotation(java.lang.String annotation)
    {
        this._annotation = annotation;
    } //-- void setAnnotation(java.lang.String) 

    /**
     * Sets the value of field 'colourScheme'.
     * 
     * @param colourScheme the value of field 'colourScheme'.
     */
    public void setColourScheme(java.lang.String colourScheme)
    {
        this._colourScheme = colourScheme;
    } //-- void setColourScheme(java.lang.String) 

    /**
     * Sets the value of field 'maxColour'.
     * 
     * @param maxColour the value of field 'maxColour'.
     */
    public void setMaxColour(int maxColour)
    {
        this._maxColour = maxColour;
        this._has_maxColour = true;
    } //-- void setMaxColour(int) 

    /**
     * Sets the value of field 'minColour'.
     * 
     * @param minColour the value of field 'minColour'.
     */
    public void setMinColour(int minColour)
    {
        this._minColour = minColour;
        this._has_minColour = true;
    } //-- void setMinColour(int) 

    /**
     * Sets the value of field 'threshold'.
     * 
     * @param threshold the value of field 'threshold'.
     */
    public void setThreshold(float threshold)
    {
        this._threshold = threshold;
        this._has_threshold = true;
    } //-- void setThreshold(float) 

    /**
     * Method unmarshal
     * 
     * 
     * 
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (jalview.schemabinding.version2.AnnotationColours) Unmarshaller.unmarshal(jalview.schemabinding.version2.AnnotationColours.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * 
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
