/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: JGroup.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
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
 * Class JGroup.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class JGroup implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

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
     * Field _name
     */
    private java.lang.String _name;

    /**
     * Field _colour
     */
    private java.lang.String _colour;

    /**
     * Field _consThreshold
     */
    private int _consThreshold;

    /**
     * keeps track of state for field: _consThreshold
     */
    private boolean _has_consThreshold;

    /**
     * Field _pidThreshold
     */
    private int _pidThreshold;

    /**
     * keeps track of state for field: _pidThreshold
     */
    private boolean _has_pidThreshold;

    /**
     * Field _outlineColour
     */
    private int _outlineColour;

    /**
     * keeps track of state for field: _outlineColour
     */
    private boolean _has_outlineColour;

    /**
     * Field _displayBoxes
     */
    private boolean _displayBoxes;

    /**
     * keeps track of state for field: _displayBoxes
     */
    private boolean _has_displayBoxes;

    /**
     * Field _displayText
     */
    private boolean _displayText;

    /**
     * keeps track of state for field: _displayText
     */
    private boolean _has_displayText;

    /**
     * Field _colourText
     */
    private boolean _colourText;

    /**
     * keeps track of state for field: _colourText
     */
    private boolean _has_colourText;

    /**
     * Field _seqList
     */
    private java.util.Vector _seqList;


      //----------------/
     //- Constructors -/
    //----------------/

    public JGroup() {
        super();
        _seqList = new Vector();
    } //-- jalview.binding.JGroup()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addSeq
     * 
     * 
     * 
     * @param vSeq
     */
    public void addSeq(int vSeq)
        throws java.lang.IndexOutOfBoundsException
    {
        _seqList.addElement(new java.lang.Integer(vSeq));
    } //-- void addSeq(int) 

    /**
     * Method addSeq
     * 
     * 
     * 
     * @param index
     * @param vSeq
     */
    public void addSeq(int index, int vSeq)
        throws java.lang.IndexOutOfBoundsException
    {
        _seqList.insertElementAt(new java.lang.Integer(vSeq), index);
    } //-- void addSeq(int, int) 

    /**
     * Method deleteColourText
     * 
     */
    public void deleteColourText()
    {
        this._has_colourText= false;
    } //-- void deleteColourText() 

    /**
     * Method deleteConsThreshold
     * 
     */
    public void deleteConsThreshold()
    {
        this._has_consThreshold= false;
    } //-- void deleteConsThreshold() 

    /**
     * Method deleteDisplayBoxes
     * 
     */
    public void deleteDisplayBoxes()
    {
        this._has_displayBoxes= false;
    } //-- void deleteDisplayBoxes() 

    /**
     * Method deleteDisplayText
     * 
     */
    public void deleteDisplayText()
    {
        this._has_displayText= false;
    } //-- void deleteDisplayText() 

    /**
     * Method deleteEnd
     * 
     */
    public void deleteEnd()
    {
        this._has_end= false;
    } //-- void deleteEnd() 

    /**
     * Method deleteOutlineColour
     * 
     */
    public void deleteOutlineColour()
    {
        this._has_outlineColour= false;
    } //-- void deleteOutlineColour() 

    /**
     * Method deletePidThreshold
     * 
     */
    public void deletePidThreshold()
    {
        this._has_pidThreshold= false;
    } //-- void deletePidThreshold() 

    /**
     * Method deleteStart
     * 
     */
    public void deleteStart()
    {
        this._has_start= false;
    } //-- void deleteStart() 

    /**
     * Method enumerateSeq
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateSeq()
    {
        return _seqList.elements();
    } //-- java.util.Enumeration enumerateSeq() 

    /**
     * Returns the value of field 'colour'.
     * 
     * @return String
     * @return the value of field 'colour'.
     */
    public java.lang.String getColour()
    {
        return this._colour;
    } //-- java.lang.String getColour() 

    /**
     * Returns the value of field 'colourText'.
     * 
     * @return boolean
     * @return the value of field 'colourText'.
     */
    public boolean getColourText()
    {
        return this._colourText;
    } //-- boolean getColourText() 

    /**
     * Returns the value of field 'consThreshold'.
     * 
     * @return int
     * @return the value of field 'consThreshold'.
     */
    public int getConsThreshold()
    {
        return this._consThreshold;
    } //-- int getConsThreshold() 

    /**
     * Returns the value of field 'displayBoxes'.
     * 
     * @return boolean
     * @return the value of field 'displayBoxes'.
     */
    public boolean getDisplayBoxes()
    {
        return this._displayBoxes;
    } //-- boolean getDisplayBoxes() 

    /**
     * Returns the value of field 'displayText'.
     * 
     * @return boolean
     * @return the value of field 'displayText'.
     */
    public boolean getDisplayText()
    {
        return this._displayText;
    } //-- boolean getDisplayText() 

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
     * Returns the value of field 'name'.
     * 
     * @return String
     * @return the value of field 'name'.
     */
    public java.lang.String getName()
    {
        return this._name;
    } //-- java.lang.String getName() 

    /**
     * Returns the value of field 'outlineColour'.
     * 
     * @return int
     * @return the value of field 'outlineColour'.
     */
    public int getOutlineColour()
    {
        return this._outlineColour;
    } //-- int getOutlineColour() 

    /**
     * Returns the value of field 'pidThreshold'.
     * 
     * @return int
     * @return the value of field 'pidThreshold'.
     */
    public int getPidThreshold()
    {
        return this._pidThreshold;
    } //-- int getPidThreshold() 

    /**
     * Method getSeq
     * 
     * 
     * 
     * @param index
     * @return int
     */
    public int getSeq(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _seqList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return ((java.lang.Integer)_seqList.elementAt(index)).intValue();
    } //-- int getSeq(int) 

    /**
     * Method getSeq
     * 
     * 
     * 
     * @return int
     */
    public int[] getSeq()
    {
        int size = _seqList.size();
        int[] mArray = new int[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = ((java.lang.Integer)_seqList.elementAt(index)).intValue();
        }
        return mArray;
    } //-- int[] getSeq() 

    /**
     * Method getSeqCount
     * 
     * 
     * 
     * @return int
     */
    public int getSeqCount()
    {
        return _seqList.size();
    } //-- int getSeqCount() 

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
     * Method hasColourText
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasColourText()
    {
        return this._has_colourText;
    } //-- boolean hasColourText() 

    /**
     * Method hasConsThreshold
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasConsThreshold()
    {
        return this._has_consThreshold;
    } //-- boolean hasConsThreshold() 

    /**
     * Method hasDisplayBoxes
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasDisplayBoxes()
    {
        return this._has_displayBoxes;
    } //-- boolean hasDisplayBoxes() 

    /**
     * Method hasDisplayText
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasDisplayText()
    {
        return this._has_displayText;
    } //-- boolean hasDisplayText() 

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
     * Method hasOutlineColour
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasOutlineColour()
    {
        return this._has_outlineColour;
    } //-- boolean hasOutlineColour() 

    /**
     * Method hasPidThreshold
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasPidThreshold()
    {
        return this._has_pidThreshold;
    } //-- boolean hasPidThreshold() 

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
     * Method removeAllSeq
     * 
     */
    public void removeAllSeq()
    {
        _seqList.removeAllElements();
    } //-- void removeAllSeq() 

    /**
     * Method removeSeq
     * 
     * 
     * 
     * @param index
     * @return int
     */
    public int removeSeq(int index)
    {
        java.lang.Object obj = _seqList.elementAt(index);
        _seqList.removeElementAt(index);
        return ((java.lang.Integer)obj).intValue();
    } //-- int removeSeq(int) 

    /**
     * Sets the value of field 'colour'.
     * 
     * @param colour the value of field 'colour'.
     */
    public void setColour(java.lang.String colour)
    {
        this._colour = colour;
    } //-- void setColour(java.lang.String) 

    /**
     * Sets the value of field 'colourText'.
     * 
     * @param colourText the value of field 'colourText'.
     */
    public void setColourText(boolean colourText)
    {
        this._colourText = colourText;
        this._has_colourText = true;
    } //-- void setColourText(boolean) 

    /**
     * Sets the value of field 'consThreshold'.
     * 
     * @param consThreshold the value of field 'consThreshold'.
     */
    public void setConsThreshold(int consThreshold)
    {
        this._consThreshold = consThreshold;
        this._has_consThreshold = true;
    } //-- void setConsThreshold(int) 

    /**
     * Sets the value of field 'displayBoxes'.
     * 
     * @param displayBoxes the value of field 'displayBoxes'.
     */
    public void setDisplayBoxes(boolean displayBoxes)
    {
        this._displayBoxes = displayBoxes;
        this._has_displayBoxes = true;
    } //-- void setDisplayBoxes(boolean) 

    /**
     * Sets the value of field 'displayText'.
     * 
     * @param displayText the value of field 'displayText'.
     */
    public void setDisplayText(boolean displayText)
    {
        this._displayText = displayText;
        this._has_displayText = true;
    } //-- void setDisplayText(boolean) 

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
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(java.lang.String name)
    {
        this._name = name;
    } //-- void setName(java.lang.String) 

    /**
     * Sets the value of field 'outlineColour'.
     * 
     * @param outlineColour the value of field 'outlineColour'.
     */
    public void setOutlineColour(int outlineColour)
    {
        this._outlineColour = outlineColour;
        this._has_outlineColour = true;
    } //-- void setOutlineColour(int) 

    /**
     * Sets the value of field 'pidThreshold'.
     * 
     * @param pidThreshold the value of field 'pidThreshold'.
     */
    public void setPidThreshold(int pidThreshold)
    {
        this._pidThreshold = pidThreshold;
        this._has_pidThreshold = true;
    } //-- void setPidThreshold(int) 

    /**
     * Method setSeq
     * 
     * 
     * 
     * @param index
     * @param vSeq
     */
    public void setSeq(int index, int vSeq)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _seqList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _seqList.setElementAt(new java.lang.Integer(vSeq), index);
    } //-- void setSeq(int, int) 

    /**
     * Method setSeq
     * 
     * 
     * 
     * @param seqArray
     */
    public void setSeq(int[] seqArray)
    {
        //-- copy array
        _seqList.removeAllElements();
        for (int i = 0; i < seqArray.length; i++) {
            _seqList.addElement(new java.lang.Integer(seqArray[i]));
        }
    } //-- void setSeq(int) 

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
        return (jalview.binding.JGroup) Unmarshaller.unmarshal(jalview.binding.JGroup.class, reader);
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
