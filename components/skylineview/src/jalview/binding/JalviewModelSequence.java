/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: JalviewModelSequence.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
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
 * Class JalviewModelSequence.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class JalviewModelSequence implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _JSeqList
     */
    private java.util.Vector _JSeqList;

    /**
     * Field _JGroupList
     */
    private java.util.Vector _JGroupList;

    /**
     * Field _viewportList
     */
    private java.util.Vector _viewportList;

    /**
     * Field _userColoursList
     */
    private java.util.Vector _userColoursList;

    /**
     * Field _treeList
     */
    private java.util.Vector _treeList;

    /**
     * Field _featureSettings
     */
    private jalview.binding.FeatureSettings _featureSettings;


      //----------------/
     //- Constructors -/
    //----------------/

    public JalviewModelSequence() {
        super();
        _JSeqList = new Vector();
        _JGroupList = new Vector();
        _viewportList = new Vector();
        _userColoursList = new Vector();
        _treeList = new Vector();
    } //-- jalview.binding.JalviewModelSequence()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addJGroup
     * 
     * 
     * 
     * @param vJGroup
     */
    public void addJGroup(jalview.binding.JGroup vJGroup)
        throws java.lang.IndexOutOfBoundsException
    {
        _JGroupList.addElement(vJGroup);
    } //-- void addJGroup(jalview.binding.JGroup) 

    /**
     * Method addJGroup
     * 
     * 
     * 
     * @param index
     * @param vJGroup
     */
    public void addJGroup(int index, jalview.binding.JGroup vJGroup)
        throws java.lang.IndexOutOfBoundsException
    {
        _JGroupList.insertElementAt(vJGroup, index);
    } //-- void addJGroup(int, jalview.binding.JGroup) 

    /**
     * Method addJSeq
     * 
     * 
     * 
     * @param vJSeq
     */
    public void addJSeq(jalview.binding.JSeq vJSeq)
        throws java.lang.IndexOutOfBoundsException
    {
        _JSeqList.addElement(vJSeq);
    } //-- void addJSeq(jalview.binding.JSeq) 

    /**
     * Method addJSeq
     * 
     * 
     * 
     * @param index
     * @param vJSeq
     */
    public void addJSeq(int index, jalview.binding.JSeq vJSeq)
        throws java.lang.IndexOutOfBoundsException
    {
        _JSeqList.insertElementAt(vJSeq, index);
    } //-- void addJSeq(int, jalview.binding.JSeq) 

    /**
     * Method addTree
     * 
     * 
     * 
     * @param vTree
     */
    public void addTree(jalview.binding.Tree vTree)
        throws java.lang.IndexOutOfBoundsException
    {
        _treeList.addElement(vTree);
    } //-- void addTree(jalview.binding.Tree) 

    /**
     * Method addTree
     * 
     * 
     * 
     * @param index
     * @param vTree
     */
    public void addTree(int index, jalview.binding.Tree vTree)
        throws java.lang.IndexOutOfBoundsException
    {
        _treeList.insertElementAt(vTree, index);
    } //-- void addTree(int, jalview.binding.Tree) 

    /**
     * Method addUserColours
     * 
     * 
     * 
     * @param vUserColours
     */
    public void addUserColours(jalview.binding.UserColours vUserColours)
        throws java.lang.IndexOutOfBoundsException
    {
        _userColoursList.addElement(vUserColours);
    } //-- void addUserColours(jalview.binding.UserColours) 

    /**
     * Method addUserColours
     * 
     * 
     * 
     * @param index
     * @param vUserColours
     */
    public void addUserColours(int index, jalview.binding.UserColours vUserColours)
        throws java.lang.IndexOutOfBoundsException
    {
        _userColoursList.insertElementAt(vUserColours, index);
    } //-- void addUserColours(int, jalview.binding.UserColours) 

    /**
     * Method addViewport
     * 
     * 
     * 
     * @param vViewport
     */
    public void addViewport(jalview.binding.Viewport vViewport)
        throws java.lang.IndexOutOfBoundsException
    {
        _viewportList.addElement(vViewport);
    } //-- void addViewport(jalview.binding.Viewport) 

    /**
     * Method addViewport
     * 
     * 
     * 
     * @param index
     * @param vViewport
     */
    public void addViewport(int index, jalview.binding.Viewport vViewport)
        throws java.lang.IndexOutOfBoundsException
    {
        _viewportList.insertElementAt(vViewport, index);
    } //-- void addViewport(int, jalview.binding.Viewport) 

    /**
     * Method enumerateJGroup
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateJGroup()
    {
        return _JGroupList.elements();
    } //-- java.util.Enumeration enumerateJGroup() 

    /**
     * Method enumerateJSeq
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateJSeq()
    {
        return _JSeqList.elements();
    } //-- java.util.Enumeration enumerateJSeq() 

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
     * Method enumerateUserColours
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateUserColours()
    {
        return _userColoursList.elements();
    } //-- java.util.Enumeration enumerateUserColours() 

    /**
     * Method enumerateViewport
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateViewport()
    {
        return _viewportList.elements();
    } //-- java.util.Enumeration enumerateViewport() 

    /**
     * Returns the value of field 'featureSettings'.
     * 
     * @return FeatureSettings
     * @return the value of field 'featureSettings'.
     */
    public jalview.binding.FeatureSettings getFeatureSettings()
    {
        return this._featureSettings;
    } //-- jalview.binding.FeatureSettings getFeatureSettings() 

    /**
     * Method getJGroup
     * 
     * 
     * 
     * @param index
     * @return JGroup
     */
    public jalview.binding.JGroup getJGroup(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _JGroupList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.JGroup) _JGroupList.elementAt(index);
    } //-- jalview.binding.JGroup getJGroup(int) 

    /**
     * Method getJGroup
     * 
     * 
     * 
     * @return JGroup
     */
    public jalview.binding.JGroup[] getJGroup()
    {
        int size = _JGroupList.size();
        jalview.binding.JGroup[] mArray = new jalview.binding.JGroup[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.JGroup) _JGroupList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.JGroup[] getJGroup() 

    /**
     * Method getJGroupCount
     * 
     * 
     * 
     * @return int
     */
    public int getJGroupCount()
    {
        return _JGroupList.size();
    } //-- int getJGroupCount() 

    /**
     * Method getJSeq
     * 
     * 
     * 
     * @param index
     * @return JSeq
     */
    public jalview.binding.JSeq getJSeq(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _JSeqList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.JSeq) _JSeqList.elementAt(index);
    } //-- jalview.binding.JSeq getJSeq(int) 

    /**
     * Method getJSeq
     * 
     * 
     * 
     * @return JSeq
     */
    public jalview.binding.JSeq[] getJSeq()
    {
        int size = _JSeqList.size();
        jalview.binding.JSeq[] mArray = new jalview.binding.JSeq[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.JSeq) _JSeqList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.JSeq[] getJSeq() 

    /**
     * Method getJSeqCount
     * 
     * 
     * 
     * @return int
     */
    public int getJSeqCount()
    {
        return _JSeqList.size();
    } //-- int getJSeqCount() 

    /**
     * Method getTree
     * 
     * 
     * 
     * @param index
     * @return Tree
     */
    public jalview.binding.Tree getTree(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _treeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.Tree) _treeList.elementAt(index);
    } //-- jalview.binding.Tree getTree(int) 

    /**
     * Method getTree
     * 
     * 
     * 
     * @return Tree
     */
    public jalview.binding.Tree[] getTree()
    {
        int size = _treeList.size();
        jalview.binding.Tree[] mArray = new jalview.binding.Tree[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.Tree) _treeList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.Tree[] getTree() 

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
     * Method getUserColours
     * 
     * 
     * 
     * @param index
     * @return UserColours
     */
    public jalview.binding.UserColours getUserColours(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _userColoursList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.UserColours) _userColoursList.elementAt(index);
    } //-- jalview.binding.UserColours getUserColours(int) 

    /**
     * Method getUserColours
     * 
     * 
     * 
     * @return UserColours
     */
    public jalview.binding.UserColours[] getUserColours()
    {
        int size = _userColoursList.size();
        jalview.binding.UserColours[] mArray = new jalview.binding.UserColours[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.UserColours) _userColoursList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.UserColours[] getUserColours() 

    /**
     * Method getUserColoursCount
     * 
     * 
     * 
     * @return int
     */
    public int getUserColoursCount()
    {
        return _userColoursList.size();
    } //-- int getUserColoursCount() 

    /**
     * Method getViewport
     * 
     * 
     * 
     * @param index
     * @return Viewport
     */
    public jalview.binding.Viewport getViewport(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _viewportList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.Viewport) _viewportList.elementAt(index);
    } //-- jalview.binding.Viewport getViewport(int) 

    /**
     * Method getViewport
     * 
     * 
     * 
     * @return Viewport
     */
    public jalview.binding.Viewport[] getViewport()
    {
        int size = _viewportList.size();
        jalview.binding.Viewport[] mArray = new jalview.binding.Viewport[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.Viewport) _viewportList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.Viewport[] getViewport() 

    /**
     * Method getViewportCount
     * 
     * 
     * 
     * @return int
     */
    public int getViewportCount()
    {
        return _viewportList.size();
    } //-- int getViewportCount() 

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
     * Method removeAllJGroup
     * 
     */
    public void removeAllJGroup()
    {
        _JGroupList.removeAllElements();
    } //-- void removeAllJGroup() 

    /**
     * Method removeAllJSeq
     * 
     */
    public void removeAllJSeq()
    {
        _JSeqList.removeAllElements();
    } //-- void removeAllJSeq() 

    /**
     * Method removeAllTree
     * 
     */
    public void removeAllTree()
    {
        _treeList.removeAllElements();
    } //-- void removeAllTree() 

    /**
     * Method removeAllUserColours
     * 
     */
    public void removeAllUserColours()
    {
        _userColoursList.removeAllElements();
    } //-- void removeAllUserColours() 

    /**
     * Method removeAllViewport
     * 
     */
    public void removeAllViewport()
    {
        _viewportList.removeAllElements();
    } //-- void removeAllViewport() 

    /**
     * Method removeJGroup
     * 
     * 
     * 
     * @param index
     * @return JGroup
     */
    public jalview.binding.JGroup removeJGroup(int index)
    {
        java.lang.Object obj = _JGroupList.elementAt(index);
        _JGroupList.removeElementAt(index);
        return (jalview.binding.JGroup) obj;
    } //-- jalview.binding.JGroup removeJGroup(int) 

    /**
     * Method removeJSeq
     * 
     * 
     * 
     * @param index
     * @return JSeq
     */
    public jalview.binding.JSeq removeJSeq(int index)
    {
        java.lang.Object obj = _JSeqList.elementAt(index);
        _JSeqList.removeElementAt(index);
        return (jalview.binding.JSeq) obj;
    } //-- jalview.binding.JSeq removeJSeq(int) 

    /**
     * Method removeTree
     * 
     * 
     * 
     * @param index
     * @return Tree
     */
    public jalview.binding.Tree removeTree(int index)
    {
        java.lang.Object obj = _treeList.elementAt(index);
        _treeList.removeElementAt(index);
        return (jalview.binding.Tree) obj;
    } //-- jalview.binding.Tree removeTree(int) 

    /**
     * Method removeUserColours
     * 
     * 
     * 
     * @param index
     * @return UserColours
     */
    public jalview.binding.UserColours removeUserColours(int index)
    {
        java.lang.Object obj = _userColoursList.elementAt(index);
        _userColoursList.removeElementAt(index);
        return (jalview.binding.UserColours) obj;
    } //-- jalview.binding.UserColours removeUserColours(int) 

    /**
     * Method removeViewport
     * 
     * 
     * 
     * @param index
     * @return Viewport
     */
    public jalview.binding.Viewport removeViewport(int index)
    {
        java.lang.Object obj = _viewportList.elementAt(index);
        _viewportList.removeElementAt(index);
        return (jalview.binding.Viewport) obj;
    } //-- jalview.binding.Viewport removeViewport(int) 

    /**
     * Sets the value of field 'featureSettings'.
     * 
     * @param featureSettings the value of field 'featureSettings'.
     */
    public void setFeatureSettings(jalview.binding.FeatureSettings featureSettings)
    {
        this._featureSettings = featureSettings;
    } //-- void setFeatureSettings(jalview.binding.FeatureSettings) 

    /**
     * Method setJGroup
     * 
     * 
     * 
     * @param index
     * @param vJGroup
     */
    public void setJGroup(int index, jalview.binding.JGroup vJGroup)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _JGroupList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _JGroupList.setElementAt(vJGroup, index);
    } //-- void setJGroup(int, jalview.binding.JGroup) 

    /**
     * Method setJGroup
     * 
     * 
     * 
     * @param JGroupArray
     */
    public void setJGroup(jalview.binding.JGroup[] JGroupArray)
    {
        //-- copy array
        _JGroupList.removeAllElements();
        for (int i = 0; i < JGroupArray.length; i++) {
            _JGroupList.addElement(JGroupArray[i]);
        }
    } //-- void setJGroup(jalview.binding.JGroup) 

    /**
     * Method setJSeq
     * 
     * 
     * 
     * @param index
     * @param vJSeq
     */
    public void setJSeq(int index, jalview.binding.JSeq vJSeq)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _JSeqList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _JSeqList.setElementAt(vJSeq, index);
    } //-- void setJSeq(int, jalview.binding.JSeq) 

    /**
     * Method setJSeq
     * 
     * 
     * 
     * @param JSeqArray
     */
    public void setJSeq(jalview.binding.JSeq[] JSeqArray)
    {
        //-- copy array
        _JSeqList.removeAllElements();
        for (int i = 0; i < JSeqArray.length; i++) {
            _JSeqList.addElement(JSeqArray[i]);
        }
    } //-- void setJSeq(jalview.binding.JSeq) 

    /**
     * Method setTree
     * 
     * 
     * 
     * @param index
     * @param vTree
     */
    public void setTree(int index, jalview.binding.Tree vTree)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _treeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _treeList.setElementAt(vTree, index);
    } //-- void setTree(int, jalview.binding.Tree) 

    /**
     * Method setTree
     * 
     * 
     * 
     * @param treeArray
     */
    public void setTree(jalview.binding.Tree[] treeArray)
    {
        //-- copy array
        _treeList.removeAllElements();
        for (int i = 0; i < treeArray.length; i++) {
            _treeList.addElement(treeArray[i]);
        }
    } //-- void setTree(jalview.binding.Tree) 

    /**
     * Method setUserColours
     * 
     * 
     * 
     * @param index
     * @param vUserColours
     */
    public void setUserColours(int index, jalview.binding.UserColours vUserColours)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _userColoursList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _userColoursList.setElementAt(vUserColours, index);
    } //-- void setUserColours(int, jalview.binding.UserColours) 

    /**
     * Method setUserColours
     * 
     * 
     * 
     * @param userColoursArray
     */
    public void setUserColours(jalview.binding.UserColours[] userColoursArray)
    {
        //-- copy array
        _userColoursList.removeAllElements();
        for (int i = 0; i < userColoursArray.length; i++) {
            _userColoursList.addElement(userColoursArray[i]);
        }
    } //-- void setUserColours(jalview.binding.UserColours) 

    /**
     * Method setViewport
     * 
     * 
     * 
     * @param index
     * @param vViewport
     */
    public void setViewport(int index, jalview.binding.Viewport vViewport)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _viewportList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _viewportList.setElementAt(vViewport, index);
    } //-- void setViewport(int, jalview.binding.Viewport) 

    /**
     * Method setViewport
     * 
     * 
     * 
     * @param viewportArray
     */
    public void setViewport(jalview.binding.Viewport[] viewportArray)
    {
        //-- copy array
        _viewportList.removeAllElements();
        for (int i = 0; i < viewportArray.length; i++) {
            _viewportList.addElement(viewportArray[i]);
        }
    } //-- void setViewport(jalview.binding.Viewport) 

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
        return (jalview.binding.JalviewModelSequence) Unmarshaller.unmarshal(jalview.binding.JalviewModelSequence.class, reader);
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
