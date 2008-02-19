/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: AnnotationElement.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
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
 * Class AnnotationElement.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
 */
public class AnnotationElement implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _position
     */
    private int _position;

    /**
     * keeps track of state for field: _position
     */
    private boolean _has_position;

    /**
     * Field _colour
     */
    private int _colour;

    /**
     * keeps track of state for field: _colour
     */
    private boolean _has_colour;

    /**
     * Field _displayCharacter
     */
    private java.lang.String _displayCharacter;

    /**
     * Field _description
     */
    private java.lang.String _description;

    /**
     * Field _secondaryStructure
     */
    private java.lang.String _secondaryStructure;

    /**
     * Field _value
     */
    private float _value;

    /**
     * keeps track of state for field: _value
     */
    private boolean _has_value;


      //----------------/
     //- Constructors -/
    //----------------/

    public AnnotationElement() {
        super();
    } //-- jalview.schemabinding.version2.AnnotationElement()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteColour
     * 
     */
    public void deleteColour()
    {
        this._has_colour= false;
    } //-- void deleteColour() 

    /**
     * Method deletePosition
     * 
     */
    public void deletePosition()
    {
        this._has_position= false;
    } //-- void deletePosition() 

    /**
     * Method deleteValue
     * 
     */
    public void deleteValue()
    {
        this._has_value= false;
    } //-- void deleteValue() 

    /**
     * Returns the value of field 'colour'.
     * 
     * @return int
     * @return the value of field 'colour'.
     */
    public int getColour()
    {
        return this._colour;
    } //-- int getColour() 

    /**
     * Returns the value of field 'description'.
     * 
     * @return String
     * @return the value of field 'description'.
     */
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * Returns the value of field 'displayCharacter'.
     * 
     * @return String
     * @return the value of field 'displayCharacter'.
     */
    public java.lang.String getDisplayCharacter()
    {
        return this._displayCharacter;
    } //-- java.lang.String getDisplayCharacter() 

    /**
     * Returns the value of field 'position'.
     * 
     * @return int
     * @return the value of field 'position'.
     */
    public int getPosition()
    {
        return this._position;
    } //-- int getPosition() 

    /**
     * Returns the value of field 'secondaryStructure'.
     * 
     * @return String
     * @return the value of field 'secondaryStructure'.
     */
    public java.lang.String getSecondaryStructure()
    {
        return this._secondaryStructure;
    } //-- java.lang.String getSecondaryStructure() 

    /**
     * Returns the value of field 'value'.
     * 
     * @return float
     * @return the value of field 'value'.
     */
    public float getValue()
    {
        return this._value;
    } //-- float getValue() 

    /**
     * Method hasColour
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasColour()
    {
        return this._has_colour;
    } //-- boolean hasColour() 

    /**
     * Method hasPosition
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasPosition()
    {
        return this._has_position;
    } //-- boolean hasPosition() 

    /**
     * Method hasValue
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasValue()
    {
        return this._has_value;
    } //-- boolean hasValue() 

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
     * Sets the value of field 'colour'.
     * 
     * @param colour the value of field 'colour'.
     */
    public void setColour(int colour)
    {
        this._colour = colour;
        this._has_colour = true;
    } //-- void setColour(int) 

    /**
     * Sets the value of field 'description'.
     * 
     * @param description the value of field 'description'.
     */
    public void setDescription(java.lang.String description)
    {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * Sets the value of field 'displayCharacter'.
     * 
     * @param displayCharacter the value of field 'displayCharacter'
     */
    public void setDisplayCharacter(java.lang.String displayCharacter)
    {
        this._displayCharacter = displayCharacter;
    } //-- void setDisplayCharacter(java.lang.String) 

    /**
     * Sets the value of field 'position'.
     * 
     * @param position the value of field 'position'.
     */
    public void setPosition(int position)
    {
        this._position = position;
        this._has_position = true;
    } //-- void setPosition(int) 

    /**
     * Sets the value of field 'secondaryStructure'.
     * 
     * @param secondaryStructure the value of field
     * 'secondaryStructure'.
     */
    public void setSecondaryStructure(java.lang.String secondaryStructure)
    {
        this._secondaryStructure = secondaryStructure;
    } //-- void setSecondaryStructure(java.lang.String) 

    /**
     * Sets the value of field 'value'.
     * 
     * @param value the value of field 'value'.
     */
    public void setValue(float value)
    {
        this._value = value;
        this._has_value = true;
    } //-- void setValue(float) 

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
        return (jalview.schemabinding.version2.AnnotationElement) Unmarshaller.unmarshal(jalview.schemabinding.version2.AnnotationElement.class, reader);
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
