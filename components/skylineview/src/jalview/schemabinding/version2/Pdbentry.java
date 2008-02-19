/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Pdbentry.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
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
 * Class Pdbentry.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
 */
public class Pdbentry implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _type
     */
    private java.lang.String _type;

    /**
     * Field _file
     */
    private java.lang.String _file;

    /**
     * Field _items
     */
    private java.util.Vector _items;


      //----------------/
     //- Constructors -/
    //----------------/

    public Pdbentry() {
        super();
        _items = new Vector();
    } //-- jalview.schemabinding.version2.Pdbentry()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addPdbentryItem
     * 
     * 
     * 
     * @param vPdbentryItem
     */
    public void addPdbentryItem(jalview.schemabinding.version2.PdbentryItem vPdbentryItem)
        throws java.lang.IndexOutOfBoundsException
    {
        _items.addElement(vPdbentryItem);
    } //-- void addPdbentryItem(jalview.schemabinding.version2.PdbentryItem) 

    /**
     * Method addPdbentryItem
     * 
     * 
     * 
     * @param index
     * @param vPdbentryItem
     */
    public void addPdbentryItem(int index, jalview.schemabinding.version2.PdbentryItem vPdbentryItem)
        throws java.lang.IndexOutOfBoundsException
    {
        _items.insertElementAt(vPdbentryItem, index);
    } //-- void addPdbentryItem(int, jalview.schemabinding.version2.PdbentryItem) 

    /**
     * Method enumeratePdbentryItem
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumeratePdbentryItem()
    {
        return _items.elements();
    } //-- java.util.Enumeration enumeratePdbentryItem() 

    /**
     * Returns the value of field 'file'.
     * 
     * @return String
     * @return the value of field 'file'.
     */
    public java.lang.String getFile()
    {
        return this._file;
    } //-- java.lang.String getFile() 

    /**
     * Returns the value of field 'id'.
     * 
     * @return String
     * @return the value of field 'id'.
     */
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Method getPdbentryItem
     * 
     * 
     * 
     * @param index
     * @return PdbentryItem
     */
    public jalview.schemabinding.version2.PdbentryItem getPdbentryItem(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.PdbentryItem) _items.elementAt(index);
    } //-- jalview.schemabinding.version2.PdbentryItem getPdbentryItem(int) 

    /**
     * Method getPdbentryItem
     * 
     * 
     * 
     * @return PdbentryItem
     */
    public jalview.schemabinding.version2.PdbentryItem[] getPdbentryItem()
    {
        int size = _items.size();
        jalview.schemabinding.version2.PdbentryItem[] mArray = new jalview.schemabinding.version2.PdbentryItem[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.PdbentryItem) _items.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.PdbentryItem[] getPdbentryItem() 

    /**
     * Method getPdbentryItemCount
     * 
     * 
     * 
     * @return int
     */
    public int getPdbentryItemCount()
    {
        return _items.size();
    } //-- int getPdbentryItemCount() 

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
     * Method removeAllPdbentryItem
     * 
     */
    public void removeAllPdbentryItem()
    {
        _items.removeAllElements();
    } //-- void removeAllPdbentryItem() 

    /**
     * Method removePdbentryItem
     * 
     * 
     * 
     * @param index
     * @return PdbentryItem
     */
    public jalview.schemabinding.version2.PdbentryItem removePdbentryItem(int index)
    {
        java.lang.Object obj = _items.elementAt(index);
        _items.removeElementAt(index);
        return (jalview.schemabinding.version2.PdbentryItem) obj;
    } //-- jalview.schemabinding.version2.PdbentryItem removePdbentryItem(int) 

    /**
     * Sets the value of field 'file'.
     * 
     * @param file the value of field 'file'.
     */
    public void setFile(java.lang.String file)
    {
        this._file = file;
    } //-- void setFile(java.lang.String) 

    /**
     * Sets the value of field 'id'.
     * 
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id)
    {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Method setPdbentryItem
     * 
     * 
     * 
     * @param index
     * @param vPdbentryItem
     */
    public void setPdbentryItem(int index, jalview.schemabinding.version2.PdbentryItem vPdbentryItem)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        _items.setElementAt(vPdbentryItem, index);
    } //-- void setPdbentryItem(int, jalview.schemabinding.version2.PdbentryItem) 

    /**
     * Method setPdbentryItem
     * 
     * 
     * 
     * @param pdbentryItemArray
     */
    public void setPdbentryItem(jalview.schemabinding.version2.PdbentryItem[] pdbentryItemArray)
    {
        //-- copy array
        _items.removeAllElements();
        for (int i = 0; i < pdbentryItemArray.length; i++) {
            _items.addElement(pdbentryItemArray[i]);
        }
    } //-- void setPdbentryItem(jalview.schemabinding.version2.PdbentryItem) 

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
        return (jalview.schemabinding.version2.Pdbentry) Unmarshaller.unmarshal(jalview.schemabinding.version2.Pdbentry.class, reader);
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
