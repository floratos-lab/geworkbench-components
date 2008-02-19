/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: SequenceSet.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
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
 * Class SequenceSet.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class SequenceSet implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _gapChar
     */
    private java.lang.String _gapChar;

    /**
     * Field _aligned
     */
    private boolean _aligned;

    /**
     * keeps track of state for field: _aligned
     */
    private boolean _has_aligned;

    /**
     * Field _sequenceList
     */
    private java.util.Vector _sequenceList;

    /**
     * Field _annotationList
     */
    private java.util.Vector _annotationList;


      //----------------/
     //- Constructors -/
    //----------------/

    public SequenceSet() {
        super();
        _sequenceList = new Vector();
        _annotationList = new Vector();
    } //-- jalview.binding.SequenceSet()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addAnnotation
     * 
     * 
     * 
     * @param vAnnotation
     */
    public void addAnnotation(jalview.binding.Annotation vAnnotation)
        throws java.lang.IndexOutOfBoundsException
    {
        _annotationList.addElement(vAnnotation);
    } //-- void addAnnotation(jalview.binding.Annotation) 

    /**
     * Method addAnnotation
     * 
     * 
     * 
     * @param index
     * @param vAnnotation
     */
    public void addAnnotation(int index, jalview.binding.Annotation vAnnotation)
        throws java.lang.IndexOutOfBoundsException
    {
        _annotationList.insertElementAt(vAnnotation, index);
    } //-- void addAnnotation(int, jalview.binding.Annotation) 

    /**
     * Method addSequence
     * 
     * 
     * 
     * @param vSequence
     */
    public void addSequence(jalview.binding.Sequence vSequence)
        throws java.lang.IndexOutOfBoundsException
    {
        _sequenceList.addElement(vSequence);
    } //-- void addSequence(jalview.binding.Sequence) 

    /**
     * Method addSequence
     * 
     * 
     * 
     * @param index
     * @param vSequence
     */
    public void addSequence(int index, jalview.binding.Sequence vSequence)
        throws java.lang.IndexOutOfBoundsException
    {
        _sequenceList.insertElementAt(vSequence, index);
    } //-- void addSequence(int, jalview.binding.Sequence) 

    /**
     * Method deleteAligned
     * 
     */
    public void deleteAligned()
    {
        this._has_aligned= false;
    } //-- void deleteAligned() 

    /**
     * Method enumerateAnnotation
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateAnnotation()
    {
        return _annotationList.elements();
    } //-- java.util.Enumeration enumerateAnnotation() 

    /**
     * Method enumerateSequence
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateSequence()
    {
        return _sequenceList.elements();
    } //-- java.util.Enumeration enumerateSequence() 

    /**
     * Returns the value of field 'aligned'.
     * 
     * @return boolean
     * @return the value of field 'aligned'.
     */
    public boolean getAligned()
    {
        return this._aligned;
    } //-- boolean getAligned() 

    /**
     * Method getAnnotation
     * 
     * 
     * 
     * @param index
     * @return Annotation
     */
    public jalview.binding.Annotation getAnnotation(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _annotationList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.Annotation) _annotationList.elementAt(index);
    } //-- jalview.binding.Annotation getAnnotation(int) 

    /**
     * Method getAnnotation
     * 
     * 
     * 
     * @return Annotation
     */
    public jalview.binding.Annotation[] getAnnotation()
    {
        int size = _annotationList.size();
        jalview.binding.Annotation[] mArray = new jalview.binding.Annotation[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.Annotation) _annotationList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.Annotation[] getAnnotation() 

    /**
     * Method getAnnotationCount
     * 
     * 
     * 
     * @return int
     */
    public int getAnnotationCount()
    {
        return _annotationList.size();
    } //-- int getAnnotationCount() 

    /**
     * Returns the value of field 'gapChar'.
     * 
     * @return String
     * @return the value of field 'gapChar'.
     */
    public java.lang.String getGapChar()
    {
        return this._gapChar;
    } //-- java.lang.String getGapChar() 

    /**
     * Method getSequence
     * 
     * 
     * 
     * @param index
     * @return Sequence
     */
    public jalview.binding.Sequence getSequence(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _sequenceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.Sequence) _sequenceList.elementAt(index);
    } //-- jalview.binding.Sequence getSequence(int) 

    /**
     * Method getSequence
     * 
     * 
     * 
     * @return Sequence
     */
    public jalview.binding.Sequence[] getSequence()
    {
        int size = _sequenceList.size();
        jalview.binding.Sequence[] mArray = new jalview.binding.Sequence[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.Sequence) _sequenceList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.Sequence[] getSequence() 

    /**
     * Method getSequenceCount
     * 
     * 
     * 
     * @return int
     */
    public int getSequenceCount()
    {
        return _sequenceList.size();
    } //-- int getSequenceCount() 

    /**
     * Method hasAligned
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasAligned()
    {
        return this._has_aligned;
    } //-- boolean hasAligned() 

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
     * Method removeAllAnnotation
     * 
     */
    public void removeAllAnnotation()
    {
        _annotationList.removeAllElements();
    } //-- void removeAllAnnotation() 

    /**
     * Method removeAllSequence
     * 
     */
    public void removeAllSequence()
    {
        _sequenceList.removeAllElements();
    } //-- void removeAllSequence() 

    /**
     * Method removeAnnotation
     * 
     * 
     * 
     * @param index
     * @return Annotation
     */
    public jalview.binding.Annotation removeAnnotation(int index)
    {
        java.lang.Object obj = _annotationList.elementAt(index);
        _annotationList.removeElementAt(index);
        return (jalview.binding.Annotation) obj;
    } //-- jalview.binding.Annotation removeAnnotation(int) 

    /**
     * Method removeSequence
     * 
     * 
     * 
     * @param index
     * @return Sequence
     */
    public jalview.binding.Sequence removeSequence(int index)
    {
        java.lang.Object obj = _sequenceList.elementAt(index);
        _sequenceList.removeElementAt(index);
        return (jalview.binding.Sequence) obj;
    } //-- jalview.binding.Sequence removeSequence(int) 

    /**
     * Sets the value of field 'aligned'.
     * 
     * @param aligned the value of field 'aligned'.
     */
    public void setAligned(boolean aligned)
    {
        this._aligned = aligned;
        this._has_aligned = true;
    } //-- void setAligned(boolean) 

    /**
     * Method setAnnotation
     * 
     * 
     * 
     * @param index
     * @param vAnnotation
     */
    public void setAnnotation(int index, jalview.binding.Annotation vAnnotation)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _annotationList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _annotationList.setElementAt(vAnnotation, index);
    } //-- void setAnnotation(int, jalview.binding.Annotation) 

    /**
     * Method setAnnotation
     * 
     * 
     * 
     * @param annotationArray
     */
    public void setAnnotation(jalview.binding.Annotation[] annotationArray)
    {
        //-- copy array
        _annotationList.removeAllElements();
        for (int i = 0; i < annotationArray.length; i++) {
            _annotationList.addElement(annotationArray[i]);
        }
    } //-- void setAnnotation(jalview.binding.Annotation) 

    /**
     * Sets the value of field 'gapChar'.
     * 
     * @param gapChar the value of field 'gapChar'.
     */
    public void setGapChar(java.lang.String gapChar)
    {
        this._gapChar = gapChar;
    } //-- void setGapChar(java.lang.String) 

    /**
     * Method setSequence
     * 
     * 
     * 
     * @param index
     * @param vSequence
     */
    public void setSequence(int index, jalview.binding.Sequence vSequence)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _sequenceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _sequenceList.setElementAt(vSequence, index);
    } //-- void setSequence(int, jalview.binding.Sequence) 

    /**
     * Method setSequence
     * 
     * 
     * 
     * @param sequenceArray
     */
    public void setSequence(jalview.binding.Sequence[] sequenceArray)
    {
        //-- copy array
        _sequenceList.removeAllElements();
        for (int i = 0; i < sequenceArray.length; i++) {
            _sequenceList.addElement(sequenceArray[i]);
        }
    } //-- void setSequence(jalview.binding.Sequence) 

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
        return (jalview.binding.SequenceSet) Unmarshaller.unmarshal(jalview.binding.SequenceSet.class, reader);
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
