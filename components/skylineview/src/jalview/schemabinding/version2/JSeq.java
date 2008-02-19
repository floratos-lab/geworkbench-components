/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: JSeq.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
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
 * Class JSeq.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
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
     * Field _hidden
     */
    private boolean _hidden;

    /**
     * keeps track of state for field: _hidden
     */
    private boolean _has_hidden;

    /**
     * Field _featuresList
     */
    private java.util.Vector _featuresList;

    /**
     * Field _pdbidsList
     */
    private java.util.Vector _pdbidsList;

    /**
     * Field _hiddenSequencesList
     */
    private java.util.Vector _hiddenSequencesList;


      //----------------/
     //- Constructors -/
    //----------------/

    public JSeq() {
        super();
        _featuresList = new Vector();
        _pdbidsList = new Vector();
        _hiddenSequencesList = new Vector();
    } //-- jalview.schemabinding.version2.JSeq()


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
    public void addFeatures(jalview.schemabinding.version2.Features vFeatures)
        throws java.lang.IndexOutOfBoundsException
    {
        _featuresList.addElement(vFeatures);
    } //-- void addFeatures(jalview.schemabinding.version2.Features) 

    /**
     * Method addFeatures
     * 
     * 
     * 
     * @param index
     * @param vFeatures
     */
    public void addFeatures(int index, jalview.schemabinding.version2.Features vFeatures)
        throws java.lang.IndexOutOfBoundsException
    {
        _featuresList.insertElementAt(vFeatures, index);
    } //-- void addFeatures(int, jalview.schemabinding.version2.Features) 

    /**
     * Method addHiddenSequences
     * 
     * 
     * 
     * @param vHiddenSequences
     */
    public void addHiddenSequences(int vHiddenSequences)
        throws java.lang.IndexOutOfBoundsException
    {
        _hiddenSequencesList.addElement(new java.lang.Integer(vHiddenSequences));
    } //-- void addHiddenSequences(int) 

    /**
     * Method addHiddenSequences
     * 
     * 
     * 
     * @param index
     * @param vHiddenSequences
     */
    public void addHiddenSequences(int index, int vHiddenSequences)
        throws java.lang.IndexOutOfBoundsException
    {
        _hiddenSequencesList.insertElementAt(new java.lang.Integer(vHiddenSequences), index);
    } //-- void addHiddenSequences(int, int) 

    /**
     * Method addPdbids
     * 
     * 
     * 
     * @param vPdbids
     */
    public void addPdbids(jalview.schemabinding.version2.Pdbids vPdbids)
        throws java.lang.IndexOutOfBoundsException
    {
        _pdbidsList.addElement(vPdbids);
    } //-- void addPdbids(jalview.schemabinding.version2.Pdbids) 

    /**
     * Method addPdbids
     * 
     * 
     * 
     * @param index
     * @param vPdbids
     */
    public void addPdbids(int index, jalview.schemabinding.version2.Pdbids vPdbids)
        throws java.lang.IndexOutOfBoundsException
    {
        _pdbidsList.insertElementAt(vPdbids, index);
    } //-- void addPdbids(int, jalview.schemabinding.version2.Pdbids) 

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
     * Method deleteHidden
     * 
     */
    public void deleteHidden()
    {
        this._has_hidden= false;
    } //-- void deleteHidden() 

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
     * Method enumerateHiddenSequences
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateHiddenSequences()
    {
        return _hiddenSequencesList.elements();
    } //-- java.util.Enumeration enumerateHiddenSequences() 

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
    public jalview.schemabinding.version2.Features getFeatures(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _featuresList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.Features) _featuresList.elementAt(index);
    } //-- jalview.schemabinding.version2.Features getFeatures(int) 

    /**
     * Method getFeatures
     * 
     * 
     * 
     * @return Features
     */
    public jalview.schemabinding.version2.Features[] getFeatures()
    {
        int size = _featuresList.size();
        jalview.schemabinding.version2.Features[] mArray = new jalview.schemabinding.version2.Features[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.Features) _featuresList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.Features[] getFeatures() 

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
     * Returns the value of field 'hidden'.
     * 
     * @return boolean
     * @return the value of field 'hidden'.
     */
    public boolean getHidden()
    {
        return this._hidden;
    } //-- boolean getHidden() 

    /**
     * Method getHiddenSequences
     * 
     * 
     * 
     * @param index
     * @return int
     */
    public int getHiddenSequences(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _hiddenSequencesList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return ((java.lang.Integer)_hiddenSequencesList.elementAt(index)).intValue();
    } //-- int getHiddenSequences(int) 

    /**
     * Method getHiddenSequences
     * 
     * 
     * 
     * @return int
     */
    public int[] getHiddenSequences()
    {
        int size = _hiddenSequencesList.size();
        int[] mArray = new int[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = ((java.lang.Integer)_hiddenSequencesList.elementAt(index)).intValue();
        }
        return mArray;
    } //-- int[] getHiddenSequences() 

    /**
     * Method getHiddenSequencesCount
     * 
     * 
     * 
     * @return int
     */
    public int getHiddenSequencesCount()
    {
        return _hiddenSequencesList.size();
    } //-- int getHiddenSequencesCount() 

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
    public jalview.schemabinding.version2.Pdbids getPdbids(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _pdbidsList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.Pdbids) _pdbidsList.elementAt(index);
    } //-- jalview.schemabinding.version2.Pdbids getPdbids(int) 

    /**
     * Method getPdbids
     * 
     * 
     * 
     * @return Pdbids
     */
    public jalview.schemabinding.version2.Pdbids[] getPdbids()
    {
        int size = _pdbidsList.size();
        jalview.schemabinding.version2.Pdbids[] mArray = new jalview.schemabinding.version2.Pdbids[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.Pdbids) _pdbidsList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.Pdbids[] getPdbids() 

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
     * Method hasHidden
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasHidden()
    {
        return this._has_hidden;
    } //-- boolean hasHidden() 

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
     * Method removeAllHiddenSequences
     * 
     */
    public void removeAllHiddenSequences()
    {
        _hiddenSequencesList.removeAllElements();
    } //-- void removeAllHiddenSequences() 

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
    public jalview.schemabinding.version2.Features removeFeatures(int index)
    {
        java.lang.Object obj = _featuresList.elementAt(index);
        _featuresList.removeElementAt(index);
        return (jalview.schemabinding.version2.Features) obj;
    } //-- jalview.schemabinding.version2.Features removeFeatures(int) 

    /**
     * Method removeHiddenSequences
     * 
     * 
     * 
     * @param index
     * @return int
     */
    public int removeHiddenSequences(int index)
    {
        java.lang.Object obj = _hiddenSequencesList.elementAt(index);
        _hiddenSequencesList.removeElementAt(index);
        return ((java.lang.Integer)obj).intValue();
    } //-- int removeHiddenSequences(int) 

    /**
     * Method removePdbids
     * 
     * 
     * 
     * @param index
     * @return Pdbids
     */
    public jalview.schemabinding.version2.Pdbids removePdbids(int index)
    {
        java.lang.Object obj = _pdbidsList.elementAt(index);
        _pdbidsList.removeElementAt(index);
        return (jalview.schemabinding.version2.Pdbids) obj;
    } //-- jalview.schemabinding.version2.Pdbids removePdbids(int) 

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
    public void setFeatures(int index, jalview.schemabinding.version2.Features vFeatures)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _featuresList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _featuresList.setElementAt(vFeatures, index);
    } //-- void setFeatures(int, jalview.schemabinding.version2.Features) 

    /**
     * Method setFeatures
     * 
     * 
     * 
     * @param featuresArray
     */
    public void setFeatures(jalview.schemabinding.version2.Features[] featuresArray)
    {
        //-- copy array
        _featuresList.removeAllElements();
        for (int i = 0; i < featuresArray.length; i++) {
            _featuresList.addElement(featuresArray[i]);
        }
    } //-- void setFeatures(jalview.schemabinding.version2.Features) 

    /**
     * Sets the value of field 'hidden'.
     * 
     * @param hidden the value of field 'hidden'.
     */
    public void setHidden(boolean hidden)
    {
        this._hidden = hidden;
        this._has_hidden = true;
    } //-- void setHidden(boolean) 

    /**
     * Method setHiddenSequences
     * 
     * 
     * 
     * @param index
     * @param vHiddenSequences
     */
    public void setHiddenSequences(int index, int vHiddenSequences)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _hiddenSequencesList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _hiddenSequencesList.setElementAt(new java.lang.Integer(vHiddenSequences), index);
    } //-- void setHiddenSequences(int, int) 

    /**
     * Method setHiddenSequences
     * 
     * 
     * 
     * @param hiddenSequencesArray
     */
    public void setHiddenSequences(int[] hiddenSequencesArray)
    {
        //-- copy array
        _hiddenSequencesList.removeAllElements();
        for (int i = 0; i < hiddenSequencesArray.length; i++) {
            _hiddenSequencesList.addElement(new java.lang.Integer(hiddenSequencesArray[i]));
        }
    } //-- void setHiddenSequences(int) 

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
    public void setPdbids(int index, jalview.schemabinding.version2.Pdbids vPdbids)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _pdbidsList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _pdbidsList.setElementAt(vPdbids, index);
    } //-- void setPdbids(int, jalview.schemabinding.version2.Pdbids) 

    /**
     * Method setPdbids
     * 
     * 
     * 
     * @param pdbidsArray
     */
    public void setPdbids(jalview.schemabinding.version2.Pdbids[] pdbidsArray)
    {
        //-- copy array
        _pdbidsList.removeAllElements();
        for (int i = 0; i < pdbidsArray.length; i++) {
            _pdbidsList.addElement(pdbidsArray[i]);
        }
    } //-- void setPdbids(jalview.schemabinding.version2.Pdbids) 

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
        return (jalview.schemabinding.version2.JSeq) Unmarshaller.unmarshal(jalview.schemabinding.version2.JSeq.class, reader);
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
