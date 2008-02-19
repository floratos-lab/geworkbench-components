/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Setting.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
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
 * Class Setting.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
 */
public class Setting implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _type
     */
    private java.lang.String _type;

    /**
     * Field _colour
     */
    private int _colour;

    /**
     * keeps track of state for field: _colour
     */
    private boolean _has_colour;

    /**
     * Field _display
     */
    private boolean _display;

    /**
     * keeps track of state for field: _display
     */
    private boolean _has_display;

    /**
     * Field _order
     */
    private float _order;

    /**
     * keeps track of state for field: _order
     */
    private boolean _has_order;


      //----------------/
     //- Constructors -/
    //----------------/

    public Setting() {
        super();
    } //-- jalview.schemabinding.version2.Setting()


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
     * Method deleteDisplay
     * 
     */
    public void deleteDisplay()
    {
        this._has_display= false;
    } //-- void deleteDisplay() 

    /**
     * Method deleteOrder
     * 
     */
    public void deleteOrder()
    {
        this._has_order= false;
    } //-- void deleteOrder() 

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
     * Returns the value of field 'display'.
     * 
     * @return boolean
     * @return the value of field 'display'.
     */
    public boolean getDisplay()
    {
        return this._display;
    } //-- boolean getDisplay() 

    /**
     * Returns the value of field 'order'.
     * 
     * @return float
     * @return the value of field 'order'.
     */
    public float getOrder()
    {
        return this._order;
    } //-- float getOrder() 

    /**
     * Returns the value of field 'type'.
     * 
     * @return String
     * @return the value of field 'type'.
     */
    public java.lang.String getType()
    {
        return this._type;
    } //-- java.lang.String getType() 

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
     * Method hasDisplay
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasDisplay()
    {
        return this._has_display;
    } //-- boolean hasDisplay() 

    /**
     * Method hasOrder
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasOrder()
    {
        return this._has_order;
    } //-- boolean hasOrder() 

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
     * Sets the value of field 'display'.
     * 
     * @param display the value of field 'display'.
     */
    public void setDisplay(boolean display)
    {
        this._display = display;
        this._has_display = true;
    } //-- void setDisplay(boolean) 

    /**
     * Sets the value of field 'order'.
     * 
     * @param order the value of field 'order'.
     */
    public void setOrder(float order)
    {
        this._order = order;
        this._has_order = true;
    } //-- void setOrder(float) 

    /**
     * Sets the value of field 'type'.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(java.lang.String type)
    {
        this._type = type;
    } //-- void setType(java.lang.String) 

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
        return (jalview.schemabinding.version2.Setting) Unmarshaller.unmarshal(jalview.schemabinding.version2.Setting.class, reader);
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
