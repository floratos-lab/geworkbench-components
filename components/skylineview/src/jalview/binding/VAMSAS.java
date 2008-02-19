/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: VAMSAS.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
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
 * Class VAMSAS.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class VAMSAS implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _alignmentList
     */
    private java.util.Vector _alignmentList;

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
        _alignmentList = new Vector();
        _treeList = new Vector();
        _sequenceSetList = new Vector();
    } //-- jalview.binding.VAMSAS()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addAlignment
     * 
     * 
     * 
     * @param vAlignment
     */
    public void addAlignment(jalview.binding.Alignment vAlignment)
        throws java.lang.IndexOutOfBoundsException
    {
        _alignmentList.addElement(vAlignment);
    } //-- void addAlignment(jalview.binding.Alignment) 

    /**
     * Method addAlignment
     * 
     * 
     * 
     * @param index
     * @param vAlignment
     */
    public void addAlignment(int index, jalview.binding.Alignment vAlignment)
        throws java.lang.IndexOutOfBoundsException
    {
        _alignmentList.insertElementAt(vAlignment, index);
    } //-- void addAlignment(int, jalview.binding.Alignment) 

    /**
     * Method addSequenceSet
     * 
     * 
     * 
     * @param vSequenceSet
     */
    public void addSequenceSet(jalview.binding.SequenceSet vSequenceSet)
        throws java.lang.IndexOutOfBoundsException
    {
        _sequenceSetList.addElement(vSequenceSet);
    } //-- void addSequenceSet(jalview.binding.SequenceSet) 

    /**
     * Method addSequenceSet
     * 
     * 
     * 
     * @param index
     * @param vSequenceSet
     */
    public void addSequenceSet(int index, jalview.binding.SequenceSet vSequenceSet)
        throws java.lang.IndexOutOfBoundsException
    {
        _sequenceSetList.insertElementAt(vSequenceSet, index);
    } //-- void addSequenceSet(int, jalview.binding.SequenceSet) 

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
     * Method enumerateAlignment
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateAlignment()
    {
        return _alignmentList.elements();
    } //-- java.util.Enumeration enumerateAlignment() 

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
     * Method getAlignment
     * 
     * 
     * 
     * @param index
     * @return Alignment
     */
    public jalview.binding.Alignment getAlignment(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _alignmentList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.Alignment) _alignmentList.elementAt(index);
    } //-- jalview.binding.Alignment getAlignment(int) 

    /**
     * Method getAlignment
     * 
     * 
     * 
     * @return Alignment
     */
    public jalview.binding.Alignment[] getAlignment()
    {
        int size = _alignmentList.size();
        jalview.binding.Alignment[] mArray = new jalview.binding.Alignment[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.Alignment) _alignmentList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.Alignment[] getAlignment() 

    /**
     * Method getAlignmentCount
     * 
     * 
     * 
     * @return int
     */
    public int getAlignmentCount()
    {
        return _alignmentList.size();
    } //-- int getAlignmentCount() 

    /**
     * Method getSequenceSet
     * 
     * 
     * 
     * @param index
     * @return SequenceSet
     */
    public jalview.binding.SequenceSet getSequenceSet(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _sequenceSetList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.SequenceSet) _sequenceSetList.elementAt(index);
    } //-- jalview.binding.SequenceSet getSequenceSet(int) 

    /**
     * Method getSequenceSet
     * 
     * 
     * 
     * @return SequenceSet
     */
    public jalview.binding.SequenceSet[] getSequenceSet()
    {
        int size = _sequenceSetList.size();
        jalview.binding.SequenceSet[] mArray = new jalview.binding.SequenceSet[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.SequenceSet) _sequenceSetList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.SequenceSet[] getSequenceSet() 

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
     * Method removeAlignment
     * 
     * 
     * 
     * @param index
     * @return Alignment
     */
    public jalview.binding.Alignment removeAlignment(int index)
    {
        java.lang.Object obj = _alignmentList.elementAt(index);
        _alignmentList.removeElementAt(index);
        return (jalview.binding.Alignment) obj;
    } //-- jalview.binding.Alignment removeAlignment(int) 

    /**
     * Method removeAllAlignment
     * 
     */
    public void removeAllAlignment()
    {
        _alignmentList.removeAllElements();
    } //-- void removeAllAlignment() 

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
    public jalview.binding.SequenceSet removeSequenceSet(int index)
    {
        java.lang.Object obj = _sequenceSetList.elementAt(index);
        _sequenceSetList.removeElementAt(index);
        return (jalview.binding.SequenceSet) obj;
    } //-- jalview.binding.SequenceSet removeSequenceSet(int) 

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
     * Method setAlignment
     * 
     * 
     * 
     * @param index
     * @param vAlignment
     */
    public void setAlignment(int index, jalview.binding.Alignment vAlignment)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _alignmentList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _alignmentList.setElementAt(vAlignment, index);
    } //-- void setAlignment(int, jalview.binding.Alignment) 

    /**
     * Method setAlignment
     * 
     * 
     * 
     * @param alignmentArray
     */
    public void setAlignment(jalview.binding.Alignment[] alignmentArray)
    {
        //-- copy array
        _alignmentList.removeAllElements();
        for (int i = 0; i < alignmentArray.length; i++) {
            _alignmentList.addElement(alignmentArray[i]);
        }
    } //-- void setAlignment(jalview.binding.Alignment) 

    /**
     * Method setSequenceSet
     * 
     * 
     * 
     * @param index
     * @param vSequenceSet
     */
    public void setSequenceSet(int index, jalview.binding.SequenceSet vSequenceSet)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _sequenceSetList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _sequenceSetList.setElementAt(vSequenceSet, index);
    } //-- void setSequenceSet(int, jalview.binding.SequenceSet) 

    /**
     * Method setSequenceSet
     * 
     * 
     * 
     * @param sequenceSetArray
     */
    public void setSequenceSet(jalview.binding.SequenceSet[] sequenceSetArray)
    {
        //-- copy array
        _sequenceSetList.removeAllElements();
        for (int i = 0; i < sequenceSetArray.length; i++) {
            _sequenceSetList.addElement(sequenceSetArray[i]);
        }
    } //-- void setSequenceSet(jalview.binding.SequenceSet) 

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
        return (jalview.binding.VAMSAS) Unmarshaller.unmarshal(jalview.binding.VAMSAS.class, reader);
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
