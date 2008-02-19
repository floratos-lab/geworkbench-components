/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: JalviewUserColours.java,v 1.1 2008-02-19 16:22:47 wangm Exp $
 */

package jalview.schemabinding.version2;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * Class JalviewUserColours.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:47 $
 */
public class JalviewUserColours implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _schemeName
     */
    private java.lang.String _schemeName;

    /**
     * Field _colourList
     */
    private java.util.Vector _colourList;


      //----------------/
     //- Constructors -/
    //----------------/

    public JalviewUserColours() {
        super();
        _colourList = new Vector();
    } //-- jalview.schemabinding.version2.JalviewUserColours()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addColour
     * 
     * 
     * 
     * @param vColour
     */
    public void addColour(jalview.schemabinding.version2.Colour vColour)
        throws java.lang.IndexOutOfBoundsException
    {
        _colourList.addElement(vColour);
    } //-- void addColour(jalview.schemabinding.version2.Colour) 

    /**
     * Method addColour
     * 
     * 
     * 
     * @param index
     * @param vColour
     */
    public void addColour(int index, jalview.schemabinding.version2.Colour vColour)
        throws java.lang.IndexOutOfBoundsException
    {
        _colourList.insertElementAt(vColour, index);
    } //-- void addColour(int, jalview.schemabinding.version2.Colour) 

    /**
     * Method enumerateColour
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateColour()
    {
        return _colourList.elements();
    } //-- java.util.Enumeration enumerateColour() 

    /**
     * Method getColour
     * 
     * 
     * 
     * @param index
     * @return Colour
     */
    public jalview.schemabinding.version2.Colour getColour(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _colourList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.Colour) _colourList.elementAt(index);
    } //-- jalview.schemabinding.version2.Colour getColour(int) 

    /**
     * Method getColour
     * 
     * 
     * 
     * @return Colour
     */
    public jalview.schemabinding.version2.Colour[] getColour()
    {
        int size = _colourList.size();
        jalview.schemabinding.version2.Colour[] mArray = new jalview.schemabinding.version2.Colour[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.Colour) _colourList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.Colour[] getColour() 

    /**
     * Method getColourCount
     * 
     * 
     * 
     * @return int
     */
    public int getColourCount()
    {
        return _colourList.size();
    } //-- int getColourCount() 

    /**
     * Returns the value of field 'schemeName'.
     * 
     * @return String
     * @return the value of field 'schemeName'.
     */
    public java.lang.String getSchemeName()
    {
        return this._schemeName;
    } //-- java.lang.String getSchemeName() 

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
     * Method removeAllColour
     * 
     */
    public void removeAllColour()
    {
        _colourList.removeAllElements();
    } //-- void removeAllColour() 

    /**
     * Method removeColour
     * 
     * 
     * 
     * @param index
     * @return Colour
     */
    public jalview.schemabinding.version2.Colour removeColour(int index)
    {
        java.lang.Object obj = _colourList.elementAt(index);
        _colourList.removeElementAt(index);
        return (jalview.schemabinding.version2.Colour) obj;
    } //-- jalview.schemabinding.version2.Colour removeColour(int) 

    /**
     * Method setColour
     * 
     * 
     * 
     * @param index
     * @param vColour
     */
    public void setColour(int index, jalview.schemabinding.version2.Colour vColour)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _colourList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _colourList.setElementAt(vColour, index);
    } //-- void setColour(int, jalview.schemabinding.version2.Colour) 

    /**
     * Method setColour
     * 
     * 
     * 
     * @param colourArray
     */
    public void setColour(jalview.schemabinding.version2.Colour[] colourArray)
    {
        //-- copy array
        _colourList.removeAllElements();
        for (int i = 0; i < colourArray.length; i++) {
            _colourList.addElement(colourArray[i]);
        }
    } //-- void setColour(jalview.schemabinding.version2.Colour) 

    /**
     * Sets the value of field 'schemeName'.
     * 
     * @param schemeName the value of field 'schemeName'.
     */
    public void setSchemeName(java.lang.String schemeName)
    {
        this._schemeName = schemeName;
    } //-- void setSchemeName(java.lang.String) 

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
        return (jalview.schemabinding.version2.JalviewUserColours) Unmarshaller.unmarshal(jalview.schemabinding.version2.JalviewUserColours.class, reader);
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
