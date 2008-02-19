/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: VAMSAS.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
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
 * Class VAMSAS.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
 */
public class VAMSAS implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _treeList
     */
    private java.util.Vector _treeList;

    /**
     * Field _sequenceSetList
     */
    private java.util.Vector _sequenceSetList;


      //----------------/
     //- Constructors -/
    //----------------/

    public VAMSAS() {
        super();
        _treeList = new Vector();
        _sequenceSetList = new Vector();
    } //-- jalview.schemabinding.version2.VAMSAS()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addSequenceSet
     * 
     * 
     * 
     * @param vSequenceSet
     */
    public void addSequenceSet(jalview.schemabinding.version2.SequenceSet vSequenceSet)
        throws java.lang.IndexOutOfBoundsException
    {
        _sequenceSetList.addElement(vSequenceSet);
    } //-- void addSequenceSet(jalview.schemabinding.version2.SequenceSet) 

    /**
     * Method addSequenceSet
     * 
     * 
     * 
     * @param index
     * @param vSequenceSet
     */
    public void addSequenceSet(int index, jalview.schemabinding.version2.SequenceSet vSequenceSet)
        throws java.lang.IndexOutOfBoundsException
    {
        _sequenceSetList.insertElementAt(vSequenceSet, index);
    } //-- void addSequenceSet(int, jalview.schemabinding.version2.SequenceSet) 

    /**
     * Method addTree
     * 
     * 
     * 
     * @param vTree
     */
    public void addTree(java.lang.String vTree)
        throws java.lang.IndexOutOfBoundsException
    {
        _treeList.addElement(vTree);
    } //-- void addTree(java.lang.String) 

    /**
     * Method addTree
     * 
     * 
     * 
     * @param index
     * @param vTree
     */
    public void addTree(int index, java.lang.String vTree)
        throws java.lang.IndexOutOfBoundsException
    {
        _treeList.insertElementAt(vTree, index);
    } //-- void addTree(int, java.lang.String) 

    /**
     * Method enumerateSequenceSet
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateSequenceSet()
    {
        return _sequenceSetList.elements();
    } //-- java.util.Enumeration enumerateSequenceSet() 

    /**
     * Method enumerateTree
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateTree()
    {
        return _treeList.elements();
    } //-- java.util.Enumeration enumerateTree() 

    /**
     * Method getSequenceSet
     * 
     * 
     * 
     * @param index
     * @return SequenceSet
     */
    public jalview.schemabinding.version2.SequenceSet getSequenceSet(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _sequenceSetList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.SequenceSet) _sequenceSetList.elementAt(index);
    } //-- jalview.schemabinding.version2.SequenceSet getSequenceSet(int) 

    /**
     * Method getSequenceSet
     * 
     * 
     * 
     * @return SequenceSet
     */
    public jalview.schemabinding.version2.SequenceSet[] getSequenceSet()
    {
        int size = _sequenceSetList.size();
        jalview.schemabinding.version2.SequenceSet[] mArray = new jalview.schemabinding.version2.SequenceSet[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.SequenceSet) _sequenceSetList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.SequenceSet[] getSequenceSet() 

    /**
     * Method getSequenceSetCount
     * 
     * 
     * 
     * @return int
     */
    public int getSequenceSetCount()
    {
        return _sequenceSetList.size();
    } //-- int getSequenceSetCount() 

    /**
     * Method getTree
     * 
     * 
     * 
     * @param index
     * @return String
     */
    public java.lang.String getTree(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _treeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (String)_treeList.elementAt(index);
    } //-- java.lang.String getTree(int) 

    /**
     * Method getTree
     * 
     * 
     * 
     * @return String
     */
    public java.lang.String[] getTree()
    {
        int size = _treeList.size();
        java.lang.String[] mArray = new java.lang.String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String)_treeList.elementAt(index);
        }
        return mArray;
    } //-- java.lang.String[] getTree() 

    /**
     * Method getTreeCount
     * 
     * 
     * 
     * @return int
     */
    public int getTreeCount()
    {
        return _treeList.size();
    } //-- int getTreeCount() 

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
     * Method removeAllSequenceSet
     * 
     */
    public void removeAllSequenceSet()
    {
        _sequenceSetList.removeAllElements();
    } //-- void removeAllSequenceSet() 

    /**
     * Method removeAllTree
     * 
     */
    public void removeAllTree()
    {
        _treeList.removeAllElements();
    } //-- void removeAllTree() 

    /**
     * Method removeSequenceSet
     * 
     * 
     * 
     * @param index
     * @return SequenceSet
     */
    public jalview.schemabinding.version2.SequenceSet removeSequenceSet(int index)
    {
        java.lang.Object obj = _sequenceSetList.elementAt(index);
        _sequenceSetList.removeElementAt(index);
        return (jalview.schemabinding.version2.SequenceSet) obj;
    } //-- jalview.schemabinding.version2.SequenceSet removeSequenceSet(int) 

    /**
     * Method removeTree
     * 
     * 
     * 
     * @param index
     * @return String
     */
    public java.lang.String removeTree(int index)
    {
        java.lang.Object obj = _treeList.elementAt(index);
        _treeList.removeElementAt(index);
        return (String)obj;
    } //-- java.lang.String removeTree(int) 

    /**
     * Method setSequenceSet
     * 
     * 
     * 
     * @param index
     * @param vSequenceSet
     */
    public void setSequenceSet(int index, jalview.schemabinding.version2.SequenceSet vSequenceSet)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _sequenceSetList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _sequenceSetList.setElementAt(vSequenceSet, index);
    } //-- void setSequenceSet(int, jalview.schemabinding.version2.SequenceSet) 

    /**
     * Method setSequenceSet
     * 
     * 
     * 
     * @param sequenceSetArray
     */
    public void setSequenceSet(jalview.schemabinding.version2.SequenceSet[] sequenceSetArray)
    {
        //-- copy array
        _sequenceSetList.removeAllElements();
        for (int i = 0; i < sequenceSetArray.length; i++) {
            _sequenceSetList.addElement(sequenceSetArray[i]);
        }
    } //-- void setSequenceSet(jalview.schemabinding.version2.SequenceSet) 

    /**
     * Method setTree
     * 
     * 
     * 
     * @param index
     * @param vTree
     */
    public void setTree(int index, java.lang.String vTree)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _treeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _treeList.setElementAt(vTree, index);
    } //-- void setTree(int, java.lang.String) 

    /**
     * Method setTree
     * 
     * 
     * 
     * @param treeArray
     */
    public void setTree(java.lang.String[] treeArray)
    {
        //-- copy array
        _treeList.removeAllElements();
        for (int i = 0; i < treeArray.length; i++) {
            _treeList.addElement(treeArray[i]);
        }
    } //-- void setTree(java.lang.String) 

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
        return (jalview.schemabinding.version2.VAMSAS) Unmarshaller.unmarshal(jalview.schemabinding.version2.VAMSAS.class, reader);
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
