/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: StructureState.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
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
 * Class StructureState.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
 */
public class StructureState implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
    private java.lang.String _content = "";

    /**
     * Field _visible
     */
    private boolean _visible;

    /**
     * keeps track of state for field: _visible
     */
    private boolean _has_visible;

    /**
     * Field _xpos
     */
    private int _xpos;

    /**
     * keeps track of state for field: _xpos
     */
    private boolean _has_xpos;

    /**
     * Field _ypos
     */
    private int _ypos;

    /**
     * keeps track of state for field: _ypos
     */
    private boolean _has_ypos;

    /**
     * Field _width
     */
    private int _width;

    /**
     * keeps track of state for field: _width
     */
    private boolean _has_width;

    /**
     * Field _height
     */
    private int _height;

    /**
     * keeps track of state for field: _height
     */
    private boolean _has_height;


      //----------------/
     //- Constructors -/
    //----------------/

    public StructureState() {
        super();
        setContent("");
    } //-- jalview.schemabinding.version2.StructureState()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteHeight
     * 
     */
    public void deleteHeight()
    {
        this._has_height= false;
    } //-- void deleteHeight() 

    /**
     * Method deleteVisible
     * 
     */
    public void deleteVisible()
    {
        this._has_visible= false;
    } //-- void deleteVisible() 

    /**
     * Method deleteWidth
     * 
     */
    public void deleteWidth()
    {
        this._has_width= false;
    } //-- void deleteWidth() 

    /**
     * Method deleteXpos
     * 
     */
    public void deleteXpos()
    {
        this._has_xpos= false;
    } //-- void deleteXpos() 

    /**
     * Method deleteYpos
     * 
     */
    public void deleteYpos()
    {
        this._has_ypos= false;
    } //-- void deleteYpos() 

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     * 
     * @return String
     * @return the value of field 'content'.
     */
    public java.lang.String getContent()
    {
        return this._content;
    } //-- java.lang.String getContent() 

    /**
     * Returns the value of field 'height'.
     * 
     * @return int
     * @return the value of field 'height'.
     */
    public int getHeight()
    {
        return this._height;
    } //-- int getHeight() 

    /**
     * Returns the value of field 'visible'.
     * 
     * @return boolean
     * @return the value of field 'visible'.
     */
    public boolean getVisible()
    {
        return this._visible;
    } //-- boolean getVisible() 

    /**
     * Returns the value of field 'width'.
     * 
     * @return int
     * @return the value of field 'width'.
     */
    public int getWidth()
    {
        return this._width;
    } //-- int getWidth() 

    /**
     * Returns the value of field 'xpos'.
     * 
     * @return int
     * @return the value of field 'xpos'.
     */
    public int getXpos()
    {
        return this._xpos;
    } //-- int getXpos() 

    /**
     * Returns the value of field 'ypos'.
     * 
     * @return int
     * @return the value of field 'ypos'.
     */
    public int getYpos()
    {
        return this._ypos;
    } //-- int getYpos() 

    /**
     * Method hasHeight
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasHeight()
    {
        return this._has_height;
    } //-- boolean hasHeight() 

    /**
     * Method hasVisible
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasVisible()
    {
        return this._has_visible;
    } //-- boolean hasVisible() 

    /**
     * Method hasWidth
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasWidth()
    {
        return this._has_width;
    } //-- boolean hasWidth() 

    /**
     * Method hasXpos
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasXpos()
    {
        return this._has_xpos;
    } //-- boolean hasXpos() 

    /**
     * Method hasYpos
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasYpos()
    {
        return this._has_ypos;
    } //-- boolean hasYpos() 

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
     * Sets the value of field 'content'. The field 'content' has
     * the following description: internal content storage
     * 
     * @param content the value of field 'content'.
     */
    public void setContent(java.lang.String content)
    {
        this._content = content;
    } //-- void setContent(java.lang.String) 

    /**
     * Sets the value of field 'height'.
     * 
     * @param height the value of field 'height'.
     */
    public void setHeight(int height)
    {
        this._height = height;
        this._has_height = true;
    } //-- void setHeight(int) 

    /**
     * Sets the value of field 'visible'.
     * 
     * @param visible the value of field 'visible'.
     */
    public void setVisible(boolean visible)
    {
        this._visible = visible;
        this._has_visible = true;
    } //-- void setVisible(boolean) 

    /**
     * Sets the value of field 'width'.
     * 
     * @param width the value of field 'width'.
     */
    public void setWidth(int width)
    {
        this._width = width;
        this._has_width = true;
    } //-- void setWidth(int) 

    /**
     * Sets the value of field 'xpos'.
     * 
     * @param xpos the value of field 'xpos'.
     */
    public void setXpos(int xpos)
    {
        this._xpos = xpos;
        this._has_xpos = true;
    } //-- void setXpos(int) 

    /**
     * Sets the value of field 'ypos'.
     * 
     * @param ypos the value of field 'ypos'.
     */
    public void setYpos(int ypos)
    {
        this._ypos = ypos;
        this._has_ypos = true;
    } //-- void setYpos(int) 

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
        return (jalview.schemabinding.version2.StructureState) Unmarshaller.unmarshal(jalview.schemabinding.version2.StructureState.class, reader);
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
