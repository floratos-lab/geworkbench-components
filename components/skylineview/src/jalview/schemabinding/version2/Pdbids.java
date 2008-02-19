/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Pdbids.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
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
 * Class Pdbids.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
 */
public class Pdbids extends jalview.schemabinding.version2.Pdbentry 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _structureStateList
     */
    private java.util.Vector _structureStateList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Pdbids() {
        super();
        _structureStateList = new Vector();
    } //-- jalview.schemabinding.version2.Pdbids()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addStructureState
     * 
     * 
     * 
     * @param vStructureState
     */
    public void addStructureState(jalview.schemabinding.version2.StructureState vStructureState)
        throws java.lang.IndexOutOfBoundsException
    {
        _structureStateList.addElement(vStructureState);
    } //-- void addStructureState(jalview.schemabinding.version2.StructureState) 

    /**
     * Method addStructureState
     * 
     * 
     * 
     * @param index
     * @param vStructureState
     */
    public void addStructureState(int index, jalview.schemabinding.version2.StructureState vStructureState)
        throws java.lang.IndexOutOfBoundsException
    {
        _structureStateList.insertElementAt(vStructureState, index);
    } //-- void addStructureState(int, jalview.schemabinding.version2.StructureState) 

    /**
     * Method enumerateStructureState
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateStructureState()
    {
        return _structureStateList.elements();
    } //-- java.util.Enumeration enumerateStructureState() 

    /**
     * Method getStructureState
     * 
     * 
     * 
     * @param index
     * @return StructureState
     */
    public jalview.schemabinding.version2.StructureState getStructureState(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _structureStateList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.StructureState) _structureStateList.elementAt(index);
    } //-- jalview.schemabinding.version2.StructureState getStructureState(int) 

    /**
     * Method getStructureState
     * 
     * 
     * 
     * @return StructureState
     */
    public jalview.schemabinding.version2.StructureState[] getStructureState()
    {
        int size = _structureStateList.size();
        jalview.schemabinding.version2.StructureState[] mArray = new jalview.schemabinding.version2.StructureState[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.StructureState) _structureStateList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.StructureState[] getStructureState() 

    /**
     * Method getStructureStateCount
     * 
     * 
     * 
     * @return int
     */
    public int getStructureStateCount()
    {
        return _structureStateList.size();
    } //-- int getStructureStateCount() 

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
     * Method removeAllStructureState
     * 
     */
    public void removeAllStructureState()
    {
        _structureStateList.removeAllElements();
    } //-- void removeAllStructureState() 

    /**
     * Method removeStructureState
     * 
     * 
     * 
     * @param index
     * @return StructureState
     */
    public jalview.schemabinding.version2.StructureState removeStructureState(int index)
    {
        java.lang.Object obj = _structureStateList.elementAt(index);
        _structureStateList.removeElementAt(index);
        return (jalview.schemabinding.version2.StructureState) obj;
    } //-- jalview.schemabinding.version2.StructureState removeStructureState(int) 

    /**
     * Method setStructureState
     * 
     * 
     * 
     * @param index
     * @param vStructureState
     */
    public void setStructureState(int index, jalview.schemabinding.version2.StructureState vStructureState)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _structureStateList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _structureStateList.setElementAt(vStructureState, index);
    } //-- void setStructureState(int, jalview.schemabinding.version2.StructureState) 

    /**
     * Method setStructureState
     * 
     * 
     * 
     * @param structureStateArray
     */
    public void setStructureState(jalview.schemabinding.version2.StructureState[] structureStateArray)
    {
        //-- copy array
        _structureStateList.removeAllElements();
        for (int i = 0; i < structureStateArray.length; i++) {
            _structureStateList.addElement(structureStateArray[i]);
        }
    } //-- void setStructureState(jalview.schemabinding.version2.StructureState) 

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
        return (jalview.schemabinding.version2.Pdbids) Unmarshaller.unmarshal(jalview.schemabinding.version2.Pdbids.class, reader);
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
