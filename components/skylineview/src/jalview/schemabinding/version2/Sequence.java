/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Sequence.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
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
 * Class Sequence.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
 */
public class Sequence extends jalview.schemabinding.version2.SequenceType 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _DBRefList
     */
    private java.util.Vector _DBRefList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Sequence() {
        super();
        _DBRefList = new Vector();
    } //-- jalview.schemabinding.version2.Sequence()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addDBRef
     * 
     * 
     * 
     * @param vDBRef
     */
    public void addDBRef(jalview.schemabinding.version2.DBRef vDBRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _DBRefList.addElement(vDBRef);
    } //-- void addDBRef(jalview.schemabinding.version2.DBRef) 

    /**
     * Method addDBRef
     * 
     * 
     * 
     * @param index
     * @param vDBRef
     */
    public void addDBRef(int index, jalview.schemabinding.version2.DBRef vDBRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _DBRefList.insertElementAt(vDBRef, index);
    } //-- void addDBRef(int, jalview.schemabinding.version2.DBRef) 

    /**
     * Method enumerateDBRef
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateDBRef()
    {
        return _DBRefList.elements();
    } //-- java.util.Enumeration enumerateDBRef() 

    /**
     * Method getDBRef
     * 
     * 
     * 
     * @param index
     * @return DBRef
     */
    public jalview.schemabinding.version2.DBRef getDBRef(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _DBRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.DBRef) _DBRefList.elementAt(index);
    } //-- jalview.schemabinding.version2.DBRef getDBRef(int) 

    /**
     * Method getDBRef
     * 
     * 
     * 
     * @return DBRef
     */
    public jalview.schemabinding.version2.DBRef[] getDBRef()
    {
        int size = _DBRefList.size();
        jalview.schemabinding.version2.DBRef[] mArray = new jalview.schemabinding.version2.DBRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.DBRef) _DBRefList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.DBRef[] getDBRef() 

    /**
     * Method getDBRefCount
     * 
     * 
     * 
     * @return int
     */
    public int getDBRefCount()
    {
        return _DBRefList.size();
    } //-- int getDBRefCount() 

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
     * Method removeAllDBRef
     * 
     */
    public void removeAllDBRef()
    {
        _DBRefList.removeAllElements();
    } //-- void removeAllDBRef() 

    /**
     * Method removeDBRef
     * 
     * 
     * 
     * @param index
     * @return DBRef
     */
    public jalview.schemabinding.version2.DBRef removeDBRef(int index)
    {
        java.lang.Object obj = _DBRefList.elementAt(index);
        _DBRefList.removeElementAt(index);
        return (jalview.schemabinding.version2.DBRef) obj;
    } //-- jalview.schemabinding.version2.DBRef removeDBRef(int) 

    /**
     * Method setDBRef
     * 
     * 
     * 
     * @param index
     * @param vDBRef
     */
    public void setDBRef(int index, jalview.schemabinding.version2.DBRef vDBRef)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _DBRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _DBRefList.setElementAt(vDBRef, index);
    } //-- void setDBRef(int, jalview.schemabinding.version2.DBRef) 

    /**
     * Method setDBRef
     * 
     * 
     * 
     * @param DBRefArray
     */
    public void setDBRef(jalview.schemabinding.version2.DBRef[] DBRefArray)
    {
        //-- copy array
        _DBRefList.removeAllElements();
        for (int i = 0; i < DBRefArray.length; i++) {
            _DBRefList.addElement(DBRefArray[i]);
        }
    } //-- void setDBRef(jalview.schemabinding.version2.DBRef) 

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
        return (jalview.schemabinding.version2.Sequence) Unmarshaller.unmarshal(jalview.schemabinding.version2.Sequence.class, reader);
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
