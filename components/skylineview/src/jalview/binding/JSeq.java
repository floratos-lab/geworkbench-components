/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: JSeq.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
 */

package jalview.binding;

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
 * Class JSeq.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class JSeq implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _colour
     */
    private int _colour;

    /**
     * keeps track of state for field: _colour
     */
    private boolean _has_colour;

    /**
     * Field _start
     */
    private int _start;

    /**
     * keeps track of state for field: _start
     */
    private boolean _has_start;

    /**
     * Field _end
     */
    private int _end;

    /**
     * keeps track of state for field: _end
     */
    private boolean _has_end;

    /**
     * Field _id
     */
    private int _id;

    /**
     * keeps track of state for field: _id
     */
    private boolean _has_id;

    /**
     * Field _featuresList
     */
    private java.util.Vector _featuresList;

    /**
     * Field _pdbidsList
     */
    private java.util.Vector _pdbidsList;


      //----------------/
     //- Constructors -/
    //----------------/

    public JSeq() {
        super();
        _featuresList = new Vector();
        _pdbidsList = new Vector();
    } //-- jalview.binding.JSeq()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addFeatures
     * 
     * 
     * 
     * @param vFeatures
     */
    public void addFeatures(jalview.binding.Features vFeatures)
        throws java.lang.IndexOutOfBoundsException
    {
        _featuresList.addElement(vFeatures);
    } //-- void addFeatures(jalview.binding.Features) 

    /**
     * Method addFeatures
     * 
     * 
     * 
     * @param index
     * @param vFeatures
     */
    public void addFeatures(int index, jalview.binding.Features vFeatures)
        throws java.lang.IndexOutOfBoundsException
    {
        _featuresList.insertElementAt(vFeatures, index);
    } //-- void addFeatures(int, jalview.binding.Features) 

    /**
     * Method addPdbids
     * 
     * 
     * 
     * @param vPdbids
     */
    public void addPdbids(jalview.binding.Pdbids vPdbids)
        throws java.lang.IndexOutOfBoundsException
    {
        _pdbidsList.addElement(vPdbids);
    } //-- void addPdbids(jalview.binding.Pdbids) 

    /**
     * Method addPdbids
     * 
     * 
     * 
     * @param index
     * @param vPdbids
     */
    public void addPdbids(int index, jalview.binding.Pdbids vPdbids)
        throws java.lang.IndexOutOfBoundsException
    {
        _pdbidsList.insertElementAt(vPdbids, index);
    } //-- void addPdbids(int, jalview.binding.Pdbids) 

    /**
     * Method deleteColour
     * 
     */
    public void deleteColour()
    {
        this._has_colour= false;
    } //-- void deleteColour() 

    /**
     * Method deleteEnd
     * 
     */
    public void deleteEnd()
    {
        this._has_end= false;
    } //-- void deleteEnd() 

    /**
     * Method deleteId
     * 
     */
    public void deleteId()
    {
        this._has_id= false;
    } //-- void deleteId() 

    /**
     * Method deleteStart
     * 
     */
    public void deleteStart()
    {
        this._has_start= false;
    } //-- void deleteStart() 

    /**
     * Method enumerateFeatures
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateFeatures()
    {
        return _featuresList.elements();
    } //-- java.util.Enumeration enumerateFeatures() 

    /**
     * Method enumeratePdbids
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumeratePdbids()
    {
        return _pdbidsList.elements();
    } //-- java.util.Enumeration enumeratePdbids() 

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
     * Method getFeatures
     * 
     * 
     * 
     * @param index
     * @return Features
     */
    public jalview.binding.Features getFeatures(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _featuresList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.Features) _featuresList.elementAt(index);
    } //-- jalview.binding.Features getFeatures(int) 

    /**
     * Method getFeatures
     * 
     * 
     * 
     * @return Features
     */
    public jalview.binding.Features[] getFeatures()
    {
        int size = _featuresList.size();
        jalview.binding.Features[] mArray = new jalview.binding.Features[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.Features) _featuresList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.Features[] getFeatures() 

    /**
     * Method getFeaturesCount
     * 
     * 
     * 
     * @return int
     */
    public int getFeaturesCount()
    {
        return _featuresList.size();
    } //-- int getFeaturesCount() 

    /**
     * Returns the value of field 'id'.
     * 
     * @return int
     * @return the value of field 'id'.
     */
    public int getId()
    {
        return this._id;
    } //-- int getId() 

    /**
     * Method getPdbids
     * 
     * 
     * 
     * @param index
     * @return Pdbids
     */
    public jalview.binding.Pdbids getPdbids(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _pdbidsList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.Pdbids) _pdbidsList.elementAt(index);
    } //-- jalview.binding.Pdbids getPdbids(int) 

    /**
     * Method getPdbids
     * 
     * 
     * 
     * @return Pdbids
     */
    public jalview.binding.Pdbids[] getPdbids()
    {
        int size = _pdbidsList.size();
        jalview.binding.Pdbids[] mArray = new jalview.binding.Pdbids[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.Pdbids) _pdbidsList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.Pdbids[] getPdbids() 

    /**
     * Method getPdbidsCount
     * 
     * 
     * 
     * @return int
     */
    public int getPdbidsCount()
    {
        return _pdbidsList.size();
    } //-- int getPdbidsCount() 

    /**
     * Returns the value of field 'start'.
     * 
     * @return int
     * @return the value of field 'start'.
     */
    public int getStart()
    {
        return this._start;
    } //-- int getStart() 

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
     * Method hasId
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasId()
    {
        return this._has_id;
    } //-- boolean hasId() 

    /**
     * Method hasStart
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasStart()
    {
        return this._has_start;
    } //-- boolean hasStart() 

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
     * Method removeAllFeatures
     * 
     */
    public void removeAllFeatures()
    {
        _featuresList.removeAllElements();
    } //-- void removeAllFeatures() 

    /**
     * Method removeAllPdbids
     * 
     */
    public void removeAllPdbids()
    {
        _pdbidsList.removeAllElements();
    } //-- void removeAllPdbids() 

    /**
     * Method removeFeatures
     * 
     * 
     * 
     * @param index
     * @return Features
     */
    public jalview.binding.Features removeFeatures(int index)
    {
        java.lang.Object obj = _featuresList.elementAt(index);
        _featuresList.removeElementAt(index);
        return (jalview.binding.Features) obj;
    } //-- jalview.binding.Features removeFeatures(int) 

    /**
     * Method removePdbids
     * 
     * 
     * 
     * @param index
     * @return Pdbids
     */
    public jalview.binding.Pdbids removePdbids(int index)
    {
        java.lang.Object obj = _pdbidsList.elementAt(index);
        _pdbidsList.removeElementAt(index);
        return (jalview.binding.Pdbids) obj;
    } //-- jalview.binding.Pdbids removePdbids(int) 

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
     * Method setFeatures
     * 
     * 
     * 
     * @param index
     * @param vFeatures
     */
    public void setFeatures(int index, jalview.binding.Features vFeatures)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _featuresList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _featuresList.setElementAt(vFeatures, index);
    } //-- void setFeatures(int, jalview.binding.Features) 

    /**
     * Method setFeatures
     * 
     * 
     * 
     * @param featuresArray
     */
    public void setFeatures(jalview.binding.Features[] featuresArray)
    {
        //-- copy array
        _featuresList.removeAllElements();
        for (int i = 0; i < featuresArray.length; i++) {
            _featuresList.addElement(featuresArray[i]);
        }
    } //-- void setFeatures(jalview.binding.Features) 

    /**
     * Sets the value of field 'id'.
     * 
     * @param id the value of field 'id'.
     */
    public void setId(int id)
    {
        this._id = id;
        this._has_id = true;
    } //-- void setId(int) 

    /**
     * Method setPdbids
     * 
     * 
     * 
     * @param index
     * @param vPdbids
     */
    public void setPdbids(int index, jalview.binding.Pdbids vPdbids)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _pdbidsList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _pdbidsList.setElementAt(vPdbids, index);
    } //-- void setPdbids(int, jalview.binding.Pdbids) 

    /**
     * Method setPdbids
     * 
     * 
     * 
     * @param pdbidsArray
     */
    public void setPdbids(jalview.binding.Pdbids[] pdbidsArray)
    {
        //-- copy array
        _pdbidsList.removeAllElements();
        for (int i = 0; i < pdbidsArray.length; i++) {
            _pdbidsList.addElement(pdbidsArray[i]);
        }
    } //-- void setPdbids(jalview.binding.Pdbids) 

    /**
     * Sets the value of field 'start'.
     * 
     * @param start the value of field 'start'.
     */
    public void setStart(int start)
    {
        this._start = start;
        this._has_start = true;
    } //-- void setStart(int) 

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
        return (jalview.binding.JSeq) Unmarshaller.unmarshal(jalview.binding.JSeq.class, reader);
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
