/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: SequenceSet.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
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
 * Class SequenceSet.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
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
     * Field _sequenceList
     */
    private java.util.Vector _sequenceList;

    /**
     * Field _annotationList
     */
    private java.util.Vector _annotationList;

    /**
     * Field _sequenceSetPropertiesList
     */
    private java.util.Vector _sequenceSetPropertiesList;


      //----------------/
     //- Constructors -/
    //----------------/

    public SequenceSet() {
        super();
        _sequenceList = new Vector();
        _annotationList = new Vector();
        _sequenceSetPropertiesList = new Vector();
    } //-- jalview.schemabinding.version2.SequenceSet()


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
    public void addAnnotation(jalview.schemabinding.version2.Annotation vAnnotation)
        throws java.lang.IndexOutOfBoundsException
    {
        _annotationList.addElement(vAnnotation);
    } //-- void addAnnotation(jalview.schemabinding.version2.Annotation) 

    /**
     * Method addAnnotation
     * 
     * 
     * 
     * @param index
     * @param vAnnotation
     */
    public void addAnnotation(int index, jalview.schemabinding.version2.Annotation vAnnotation)
        throws java.lang.IndexOutOfBoundsException
    {
        _annotationList.insertElementAt(vAnnotation, index);
    } //-- void addAnnotation(int, jalview.schemabinding.version2.Annotation) 

    /**
     * Method addSequence
     * 
     * 
     * 
     * @param vSequence
     */
    public void addSequence(jalview.schemabinding.version2.Sequence vSequence)
        throws java.lang.IndexOutOfBoundsException
    {
        _sequenceList.addElement(vSequence);
    } //-- void addSequence(jalview.schemabinding.version2.Sequence) 

    /**
     * Method addSequence
     * 
     * 
     * 
     * @param index
     * @param vSequence
     */
    public void addSequence(int index, jalview.schemabinding.version2.Sequence vSequence)
        throws java.lang.IndexOutOfBoundsException
    {
        _sequenceList.insertElementAt(vSequence, index);
    } //-- void addSequence(int, jalview.schemabinding.version2.Sequence) 

    /**
     * Method addSequenceSetProperties
     * 
     * 
     * 
     * @param vSequenceSetProperties
     */
    public void addSequenceSetProperties(jalview.schemabinding.version2.SequenceSetProperties vSequenceSetProperties)
        throws java.lang.IndexOutOfBoundsException
    {
        _sequenceSetPropertiesList.addElement(vSequenceSetProperties);
    } //-- void addSequenceSetProperties(jalview.schemabinding.version2.SequenceSetProperties) 

    /**
     * Method addSequenceSetProperties
     * 
     * 
     * 
     * @param index
     * @param vSequenceSetProperties
     */
    public void addSequenceSetProperties(int index, jalview.schemabinding.version2.SequenceSetProperties vSequenceSetProperties)
        throws java.lang.IndexOutOfBoundsException
    {
        _sequenceSetPropertiesList.insertElementAt(vSequenceSetProperties, index);
    } //-- void addSequenceSetProperties(int, jalview.schemabinding.version2.SequenceSetProperties) 

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
     * Method enumerateSequenceSetProperties
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateSequenceSetProperties()
    {
        return _sequenceSetPropertiesList.elements();
    } //-- java.util.Enumeration enumerateSequenceSetProperties() 

    /**
     * Method getAnnotation
     * 
     * 
     * 
     * @param index
     * @return Annotation
     */
    public jalview.schemabinding.version2.Annotation getAnnotation(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _annotationList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.Annotation) _annotationList.elementAt(index);
    } //-- jalview.schemabinding.version2.Annotation getAnnotation(int) 

    /**
     * Method getAnnotation
     * 
     * 
     * 
     * @return Annotation
     */
    public jalview.schemabinding.version2.Annotation[] getAnnotation()
    {
        int size = _annotationList.size();
        jalview.schemabinding.version2.Annotation[] mArray = new jalview.schemabinding.version2.Annotation[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.Annotation) _annotationList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.Annotation[] getAnnotation() 

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
    public jalview.schemabinding.version2.Sequence getSequence(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _sequenceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.Sequence) _sequenceList.elementAt(index);
    } //-- jalview.schemabinding.version2.Sequence getSequence(int) 

    /**
     * Method getSequence
     * 
     * 
     * 
     * @return Sequence
     */
    public jalview.schemabinding.version2.Sequence[] getSequence()
    {
        int size = _sequenceList.size();
        jalview.schemabinding.version2.Sequence[] mArray = new jalview.schemabinding.version2.Sequence[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.Sequence) _sequenceList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.Sequence[] getSequence() 

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
     * Method getSequenceSetProperties
     * 
     * 
     * 
     * @param index
     * @return SequenceSetProperties
     */
    public jalview.schemabinding.version2.SequenceSetProperties getSequenceSetProperties(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _sequenceSetPropertiesList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.SequenceSetProperties) _sequenceSetPropertiesList.elementAt(index);
    } //-- jalview.schemabinding.version2.SequenceSetProperties getSequenceSetProperties(int) 

    /**
     * Method getSequenceSetProperties
     * 
     * 
     * 
     * @return SequenceSetProperties
     */
    public jalview.schemabinding.version2.SequenceSetProperties[] getSequenceSetProperties()
    {
        int size = _sequenceSetPropertiesList.size();
        jalview.schemabinding.version2.SequenceSetProperties[] mArray = new jalview.schemabinding.version2.SequenceSetProperties[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.SequenceSetProperties) _sequenceSetPropertiesList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.SequenceSetProperties[] getSequenceSetProperties() 

    /**
     * Method getSequenceSetPropertiesCount
     * 
     * 
     * 
     * @return int
     */
    public int getSequenceSetPropertiesCount()
    {
        return _sequenceSetPropertiesList.size();
    } //-- int getSequenceSetPropertiesCount() 

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
     * Method removeAllSequenceSetProperties
     * 
     */
    public void removeAllSequenceSetProperties()
    {
        _sequenceSetPropertiesList.removeAllElements();
    } //-- void removeAllSequenceSetProperties() 

    /**
     * Method removeAnnotation
     * 
     * 
     * 
     * @param index
     * @return Annotation
     */
    public jalview.schemabinding.version2.Annotation removeAnnotation(int index)
    {
        java.lang.Object obj = _annotationList.elementAt(index);
        _annotationList.removeElementAt(index);
        return (jalview.schemabinding.version2.Annotation) obj;
    } //-- jalview.schemabinding.version2.Annotation removeAnnotation(int) 

    /**
     * Method removeSequence
     * 
     * 
     * 
     * @param index
     * @return Sequence
     */
    public jalview.schemabinding.version2.Sequence removeSequence(int index)
    {
        java.lang.Object obj = _sequenceList.elementAt(index);
        _sequenceList.removeElementAt(index);
        return (jalview.schemabinding.version2.Sequence) obj;
    } //-- jalview.schemabinding.version2.Sequence removeSequence(int) 

    /**
     * Method removeSequenceSetProperties
     * 
     * 
     * 
     * @param index
     * @return SequenceSetProperties
     */
    public jalview.schemabinding.version2.SequenceSetProperties removeSequenceSetProperties(int index)
    {
        java.lang.Object obj = _sequenceSetPropertiesList.elementAt(index);
        _sequenceSetPropertiesList.removeElementAt(index);
        return (jalview.schemabinding.version2.SequenceSetProperties) obj;
    } //-- jalview.schemabinding.version2.SequenceSetProperties removeSequenceSetProperties(int) 

    /**
     * Method setAnnotation
     * 
     * 
     * 
     * @param index
     * @param vAnnotation
     */
    public void setAnnotation(int index, jalview.schemabinding.version2.Annotation vAnnotation)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _annotationList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _annotationList.setElementAt(vAnnotation, index);
    } //-- void setAnnotation(int, jalview.schemabinding.version2.Annotation) 

    /**
     * Method setAnnotation
     * 
     * 
     * 
     * @param annotationArray
     */
    public void setAnnotation(jalview.schemabinding.version2.Annotation[] annotationArray)
    {
        //-- copy array
        _annotationList.removeAllElements();
        for (int i = 0; i < annotationArray.length; i++) {
            _annotationList.addElement(annotationArray[i]);
        }
    } //-- void setAnnotation(jalview.schemabinding.version2.Annotation) 

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
    public void setSequence(int index, jalview.schemabinding.version2.Sequence vSequence)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _sequenceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _sequenceList.setElementAt(vSequence, index);
    } //-- void setSequence(int, jalview.schemabinding.version2.Sequence) 

    /**
     * Method setSequence
     * 
     * 
     * 
     * @param sequenceArray
     */
    public void setSequence(jalview.schemabinding.version2.Sequence[] sequenceArray)
    {
        //-- copy array
        _sequenceList.removeAllElements();
        for (int i = 0; i < sequenceArray.length; i++) {
            _sequenceList.addElement(sequenceArray[i]);
        }
    } //-- void setSequence(jalview.schemabinding.version2.Sequence) 

    /**
     * Method setSequenceSetProperties
     * 
     * 
     * 
     * @param index
     * @param vSequenceSetProperties
     */
    public void setSequenceSetProperties(int index, jalview.schemabinding.version2.SequenceSetProperties vSequenceSetProperties)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _sequenceSetPropertiesList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _sequenceSetPropertiesList.setElementAt(vSequenceSetProperties, index);
    } //-- void setSequenceSetProperties(int, jalview.schemabinding.version2.SequenceSetProperties) 

    /**
     * Method setSequenceSetProperties
     * 
     * 
     * 
     * @param sequenceSetPropertiesArray
     */
    public void setSequenceSetProperties(jalview.schemabinding.version2.SequenceSetProperties[] sequenceSetPropertiesArray)
    {
        //-- copy array
        _sequenceSetPropertiesList.removeAllElements();
        for (int i = 0; i < sequenceSetPropertiesArray.length; i++) {
            _sequenceSetPropertiesList.addElement(sequenceSetPropertiesArray[i]);
        }
    } //-- void setSequenceSetProperties(jalview.schemabinding.version2.SequenceSetProperties) 

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
        return (jalview.schemabinding.version2.SequenceSet) Unmarshaller.unmarshal(jalview.schemabinding.version2.SequenceSet.class, reader);
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
