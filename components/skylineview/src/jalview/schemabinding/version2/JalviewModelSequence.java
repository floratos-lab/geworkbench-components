/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: JalviewModelSequence.java,v 1.1 2008-02-19 16:22:47 wangm Exp $
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
 * Class JalviewModelSequence.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:47 $
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
    private jalview.schemabinding.version2.FeatureSettings _featureSettings;


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
    } //-- jalview.schemabinding.version2.JalviewModelSequence()


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
    public void addJGroup(jalview.schemabinding.version2.JGroup vJGroup)
        throws java.lang.IndexOutOfBoundsException
    {
        _JGroupList.addElement(vJGroup);
    } //-- void addJGroup(jalview.schemabinding.version2.JGroup) 

    /**
     * Method addJGroup
     * 
     * 
     * 
     * @param index
     * @param vJGroup
     */
    public void addJGroup(int index, jalview.schemabinding.version2.JGroup vJGroup)
        throws java.lang.IndexOutOfBoundsException
    {
        _JGroupList.insertElementAt(vJGroup, index);
    } //-- void addJGroup(int, jalview.schemabinding.version2.JGroup) 

    /**
     * Method addJSeq
     * 
     * 
     * 
     * @param vJSeq
     */
    public void addJSeq(jalview.schemabinding.version2.JSeq vJSeq)
        throws java.lang.IndexOutOfBoundsException
    {
        _JSeqList.addElement(vJSeq);
    } //-- void addJSeq(jalview.schemabinding.version2.JSeq) 

    /**
     * Method addJSeq
     * 
     * 
     * 
     * @param index
     * @param vJSeq
     */
    public void addJSeq(int index, jalview.schemabinding.version2.JSeq vJSeq)
        throws java.lang.IndexOutOfBoundsException
    {
        _JSeqList.insertElementAt(vJSeq, index);
    } //-- void addJSeq(int, jalview.schemabinding.version2.JSeq) 

    /**
     * Method addTree
     * 
     * 
     * 
     * @param vTree
     */
    public void addTree(jalview.schemabinding.version2.Tree vTree)
        throws java.lang.IndexOutOfBoundsException
    {
        _treeList.addElement(vTree);
    } //-- void addTree(jalview.schemabinding.version2.Tree) 

    /**
     * Method addTree
     * 
     * 
     * 
     * @param index
     * @param vTree
     */
    public void addTree(int index, jalview.schemabinding.version2.Tree vTree)
        throws java.lang.IndexOutOfBoundsException
    {
        _treeList.insertElementAt(vTree, index);
    } //-- void addTree(int, jalview.schemabinding.version2.Tree) 

    /**
     * Method addUserColours
     * 
     * 
     * 
     * @param vUserColours
     */
    public void addUserColours(jalview.schemabinding.version2.UserColours vUserColours)
        throws java.lang.IndexOutOfBoundsException
    {
        _userColoursList.addElement(vUserColours);
    } //-- void addUserColours(jalview.schemabinding.version2.UserColours) 

    /**
     * Method addUserColours
     * 
     * 
     * 
     * @param index
     * @param vUserColours
     */
    public void addUserColours(int index, jalview.schemabinding.version2.UserColours vUserColours)
        throws java.lang.IndexOutOfBoundsException
    {
        _userColoursList.insertElementAt(vUserColours, index);
    } //-- void addUserColours(int, jalview.schemabinding.version2.UserColours) 

    /**
     * Method addViewport
     * 
     * 
     * 
     * @param vViewport
     */
    public void addViewport(jalview.schemabinding.version2.Viewport vViewport)
        throws java.lang.IndexOutOfBoundsException
    {
        _viewportList.addElement(vViewport);
    } //-- void addViewport(jalview.schemabinding.version2.Viewport) 

    /**
     * Method addViewport
     * 
     * 
     * 
     * @param index
     * @param vViewport
     */
    public void addViewport(int index, jalview.schemabinding.version2.Viewport vViewport)
        throws java.lang.IndexOutOfBoundsException
    {
        _viewportList.insertElementAt(vViewport, index);
    } //-- void addViewport(int, jalview.schemabinding.version2.Viewport) 

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
    public jalview.schemabinding.version2.FeatureSettings getFeatureSettings()
    {
        return this._featureSettings;
    } //-- jalview.schemabinding.version2.FeatureSettings getFeatureSettings() 

    /**
     * Method getJGroup
     * 
     * 
     * 
     * @param index
     * @return JGroup
     */
    public jalview.schemabinding.version2.JGroup getJGroup(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _JGroupList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.JGroup) _JGroupList.elementAt(index);
    } //-- jalview.schemabinding.version2.JGroup getJGroup(int) 

    /**
     * Method getJGroup
     * 
     * 
     * 
     * @return JGroup
     */
    public jalview.schemabinding.version2.JGroup[] getJGroup()
    {
        int size = _JGroupList.size();
        jalview.schemabinding.version2.JGroup[] mArray = new jalview.schemabinding.version2.JGroup[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.JGroup) _JGroupList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.JGroup[] getJGroup() 

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
    public jalview.schemabinding.version2.JSeq getJSeq(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _JSeqList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.JSeq) _JSeqList.elementAt(index);
    } //-- jalview.schemabinding.version2.JSeq getJSeq(int) 

    /**
     * Method getJSeq
     * 
     * 
     * 
     * @return JSeq
     */
    public jalview.schemabinding.version2.JSeq[] getJSeq()
    {
        int size = _JSeqList.size();
        jalview.schemabinding.version2.JSeq[] mArray = new jalview.schemabinding.version2.JSeq[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.JSeq) _JSeqList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.JSeq[] getJSeq() 

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
    public jalview.schemabinding.version2.Tree getTree(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _treeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.Tree) _treeList.elementAt(index);
    } //-- jalview.schemabinding.version2.Tree getTree(int) 

    /**
     * Method getTree
     * 
     * 
     * 
     * @return Tree
     */
    public jalview.schemabinding.version2.Tree[] getTree()
    {
        int size = _treeList.size();
        jalview.schemabinding.version2.Tree[] mArray = new jalview.schemabinding.version2.Tree[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.Tree) _treeList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.Tree[] getTree() 

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
    public jalview.schemabinding.version2.UserColours getUserColours(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _userColoursList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.UserColours) _userColoursList.elementAt(index);
    } //-- jalview.schemabinding.version2.UserColours getUserColours(int) 

    /**
     * Method getUserColours
     * 
     * 
     * 
     * @return UserColours
     */
    public jalview.schemabinding.version2.UserColours[] getUserColours()
    {
        int size = _userColoursList.size();
        jalview.schemabinding.version2.UserColours[] mArray = new jalview.schemabinding.version2.UserColours[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.UserColours) _userColoursList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.UserColours[] getUserColours() 

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
    public jalview.schemabinding.version2.Viewport getViewport(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _viewportList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.Viewport) _viewportList.elementAt(index);
    } //-- jalview.schemabinding.version2.Viewport getViewport(int) 

    /**
     * Method getViewport
     * 
     * 
     * 
     * @return Viewport
     */
    public jalview.schemabinding.version2.Viewport[] getViewport()
    {
        int size = _viewportList.size();
        jalview.schemabinding.version2.Viewport[] mArray = new jalview.schemabinding.version2.Viewport[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.Viewport) _viewportList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.Viewport[] getViewport() 

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
    public jalview.schemabinding.version2.JGroup removeJGroup(int index)
    {
        java.lang.Object obj = _JGroupList.elementAt(index);
        _JGroupList.removeElementAt(index);
        return (jalview.schemabinding.version2.JGroup) obj;
    } //-- jalview.schemabinding.version2.JGroup removeJGroup(int) 

    /**
     * Method removeJSeq
     * 
     * 
     * 
     * @param index
     * @return JSeq
     */
    public jalview.schemabinding.version2.JSeq removeJSeq(int index)
    {
        java.lang.Object obj = _JSeqList.elementAt(index);
        _JSeqList.removeElementAt(index);
        return (jalview.schemabinding.version2.JSeq) obj;
    } //-- jalview.schemabinding.version2.JSeq removeJSeq(int) 

    /**
     * Method removeTree
     * 
     * 
     * 
     * @param index
     * @return Tree
     */
    public jalview.schemabinding.version2.Tree removeTree(int index)
    {
        java.lang.Object obj = _treeList.elementAt(index);
        _treeList.removeElementAt(index);
        return (jalview.schemabinding.version2.Tree) obj;
    } //-- jalview.schemabinding.version2.Tree removeTree(int) 

    /**
     * Method removeUserColours
     * 
     * 
     * 
     * @param index
     * @return UserColours
     */
    public jalview.schemabinding.version2.UserColours removeUserColours(int index)
    {
        java.lang.Object obj = _userColoursList.elementAt(index);
        _userColoursList.removeElementAt(index);
        return (jalview.schemabinding.version2.UserColours) obj;
    } //-- jalview.schemabinding.version2.UserColours removeUserColours(int) 

    /**
     * Method removeViewport
     * 
     * 
     * 
     * @param index
     * @return Viewport
     */
    public jalview.schemabinding.version2.Viewport removeViewport(int index)
    {
        java.lang.Object obj = _viewportList.elementAt(index);
        _viewportList.removeElementAt(index);
        return (jalview.schemabinding.version2.Viewport) obj;
    } //-- jalview.schemabinding.version2.Viewport removeViewport(int) 

    /**
     * Sets the value of field 'featureSettings'.
     * 
     * @param featureSettings the value of field 'featureSettings'.
     */
    public void setFeatureSettings(jalview.schemabinding.version2.FeatureSettings featureSettings)
    {
        this._featureSettings = featureSettings;
    } //-- void setFeatureSettings(jalview.schemabinding.version2.FeatureSettings) 

    /**
     * Method setJGroup
     * 
     * 
     * 
     * @param index
     * @param vJGroup
     */
    public void setJGroup(int index, jalview.schemabinding.version2.JGroup vJGroup)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _JGroupList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _JGroupList.setElementAt(vJGroup, index);
    } //-- void setJGroup(int, jalview.schemabinding.version2.JGroup) 

    /**
     * Method setJGroup
     * 
     * 
     * 
     * @param JGroupArray
     */
    public void setJGroup(jalview.schemabinding.version2.JGroup[] JGroupArray)
    {
        //-- copy array
        _JGroupList.removeAllElements();
        for (int i = 0; i < JGroupArray.length; i++) {
            _JGroupList.addElement(JGroupArray[i]);
        }
    } //-- void setJGroup(jalview.schemabinding.version2.JGroup) 

    /**
     * Method setJSeq
     * 
     * 
     * 
     * @param index
     * @param vJSeq
     */
    public void setJSeq(int index, jalview.schemabinding.version2.JSeq vJSeq)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _JSeqList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _JSeqList.setElementAt(vJSeq, index);
    } //-- void setJSeq(int, jalview.schemabinding.version2.JSeq) 

    /**
     * Method setJSeq
     * 
     * 
     * 
     * @param JSeqArray
     */
    public void setJSeq(jalview.schemabinding.version2.JSeq[] JSeqArray)
    {
        //-- copy array
        _JSeqList.removeAllElements();
        for (int i = 0; i < JSeqArray.length; i++) {
            _JSeqList.addElement(JSeqArray[i]);
        }
    } //-- void setJSeq(jalview.schemabinding.version2.JSeq) 

    /**
     * Method setTree
     * 
     * 
     * 
     * @param index
     * @param vTree
     */
    public void setTree(int index, jalview.schemabinding.version2.Tree vTree)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _treeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _treeList.setElementAt(vTree, index);
    } //-- void setTree(int, jalview.schemabinding.version2.Tree) 

    /**
     * Method setTree
     * 
     * 
     * 
     * @param treeArray
     */
    public void setTree(jalview.schemabinding.version2.Tree[] treeArray)
    {
        //-- copy array
        _treeList.removeAllElements();
        for (int i = 0; i < treeArray.length; i++) {
            _treeList.addElement(treeArray[i]);
        }
    } //-- void setTree(jalview.schemabinding.version2.Tree) 

    /**
     * Method setUserColours
     * 
     * 
     * 
     * @param index
     * @param vUserColours
     */
    public void setUserColours(int index, jalview.schemabinding.version2.UserColours vUserColours)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _userColoursList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _userColoursList.setElementAt(vUserColours, index);
    } //-- void setUserColours(int, jalview.schemabinding.version2.UserColours) 

    /**
     * Method setUserColours
     * 
     * 
     * 
     * @param userColoursArray
     */
    public void setUserColours(jalview.schemabinding.version2.UserColours[] userColoursArray)
    {
        //-- copy array
        _userColoursList.removeAllElements();
        for (int i = 0; i < userColoursArray.length; i++) {
            _userColoursList.addElement(userColoursArray[i]);
        }
    } //-- void setUserColours(jalview.schemabinding.version2.UserColours) 

    /**
     * Method setViewport
     * 
     * 
     * 
     * @param index
     * @param vViewport
     */
    public void setViewport(int index, jalview.schemabinding.version2.Viewport vViewport)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _viewportList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _viewportList.setElementAt(vViewport, index);
    } //-- void setViewport(int, jalview.schemabinding.version2.Viewport) 

    /**
     * Method setViewport
     * 
     * 
     * 
     * @param viewportArray
     */
    public void setViewport(jalview.schemabinding.version2.Viewport[] viewportArray)
    {
        //-- copy array
        _viewportList.removeAllElements();
        for (int i = 0; i < viewportArray.length; i++) {
            _viewportList.addElement(viewportArray[i]);
        }
    } //-- void setViewport(jalview.schemabinding.version2.Viewport) 

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
        return (jalview.schemabinding.version2.JalviewModelSequence) Unmarshaller.unmarshal(jalview.schemabinding.version2.JalviewModelSequence.class, reader);
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
