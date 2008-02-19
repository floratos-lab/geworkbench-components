/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Feature.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
 */

package jalview.binding;

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
 * Class Feature.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class Feature implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _begin
     */
    private int _begin;

    /**
     * keeps track of state for field: _begin
     */
    private boolean _has_begin;

    /**
     * Field _end
     */
    private int _end;

    /**
     * keeps track of state for field: _end
     */
    private boolean _has_end;

    /**
     * Field _type
     */
    private java.lang.String _type;

    /**
     * Field _description
     */
    private java.lang.String _description;

    /**
     * Field _status
     */
    private java.lang.String _status;

    /**
     * Field _featureGroup
     */
    private java.lang.String _featureGroup;


      //----------------/
     //- Constructors -/
    //----------------/

    public Feature() {
        super();
    } //-- jalview.binding.Feature()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteBegin
     * 
     */
    public void deleteBegin()
    {
        this._has_begin= false;
    } //-- void deleteBegin() 

    /**
     * Method deleteEnd
     * 
     */
    public void deleteEnd()
    {
        this._has_end= false;
    } //-- void deleteEnd() 

    /**
     * Returns the value of field 'begin'.
     * 
     * @return int
     * @return the value of field 'begin'.
     */
    public int getBegin()
    {
        return this._begin;
    } //-- int getBegin() 

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
     * Returns the value of field 'end'.
     * 
     * @return int
     * @return the value of field 'end'.
     */
    public int getEnd()
    {
        return this._end;
    } //-- int getEnd() 

    /**
     * Returns the value of field 'featureGroup'.
     * 
     * @return String
     * @return the value of field 'featureGroup'.
     */
    public java.lang.String getFeatureGroup()
    {
        return this._featureGroup;
    } //-- java.lang.String getFeatureGroup() 

    /**
     * Returns the value of field 'status'.
     * 
     * @return String
     * @return the value of field 'status'.
     */
    public java.lang.String getStatus()
    {
        return this._status;
    } //-- java.lang.String getStatus() 

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
     * Method hasBegin
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasBegin()
    {
        return this._has_begin;
    } //-- boolean hasBegin() 

    /**
     * Method hasEnd
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasEnd()
    {
        return this._has_end;
    } //-- boolean hasEnd() 

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
     * Sets the value of field 'begin'.
     * 
     * @param begin the value of field 'begin'.
     */
    public void setBegin(int begin)
    {
        this._begin = begin;
        this._has_begin = true;
    } //-- void setBegin(int) 

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
     * Sets the value of field 'end'.
     * 
     * @param end the value of field 'end'.
     */
    public void setEnd(int end)
    {
        this._end = end;
        this._has_end = true;
    } //-- void setEnd(int) 

    /**
     * Sets the value of field 'featureGroup'.
     * 
     * @param featureGroup the value of field 'featureGroup'.
     */
    public void setFeatureGroup(java.lang.String featureGroup)
    {
        this._featureGroup = featureGroup;
    } //-- void setFeatureGroup(java.lang.String) 

    /**
     * Sets the value of field 'status'.
     * 
     * @param status the value of field 'status'.
     */
    public void setStatus(java.lang.String status)
    {
        this._status = status;
    } //-- void setStatus(java.lang.String) 

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
        return (jalview.binding.Feature) Unmarshaller.unmarshal(jalview.binding.Feature.class, reader);
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
