/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: FeatureSettings.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
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
 * Class FeatureSettings.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
 */
public class FeatureSettings implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _settingList
     */
    private java.util.Vector _settingList;

    /**
     * Field _groupList
     */
    private java.util.Vector _groupList;


      //----------------/
     //- Constructors -/
    //----------------/

    public FeatureSettings() {
        super();
        _settingList = new Vector();
        _groupList = new Vector();
    } //-- jalview.schemabinding.version2.FeatureSettings()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addGroup
     * 
     * 
     * 
     * @param vGroup
     */
    public void addGroup(jalview.schemabinding.version2.Group vGroup)
        throws java.lang.IndexOutOfBoundsException
    {
        _groupList.addElement(vGroup);
    } //-- void addGroup(jalview.schemabinding.version2.Group) 

    /**
     * Method addGroup
     * 
     * 
     * 
     * @param index
     * @param vGroup
     */
    public void addGroup(int index, jalview.schemabinding.version2.Group vGroup)
        throws java.lang.IndexOutOfBoundsException
    {
        _groupList.insertElementAt(vGroup, index);
    } //-- void addGroup(int, jalview.schemabinding.version2.Group) 

    /**
     * Method addSetting
     * 
     * 
     * 
     * @param vSetting
     */
    public void addSetting(jalview.schemabinding.version2.Setting vSetting)
        throws java.lang.IndexOutOfBoundsException
    {
        _settingList.addElement(vSetting);
    } //-- void addSetting(jalview.schemabinding.version2.Setting) 

    /**
     * Method addSetting
     * 
     * 
     * 
     * @param index
     * @param vSetting
     */
    public void addSetting(int index, jalview.schemabinding.version2.Setting vSetting)
        throws java.lang.IndexOutOfBoundsException
    {
        _settingList.insertElementAt(vSetting, index);
    } //-- void addSetting(int, jalview.schemabinding.version2.Setting) 

    /**
     * Method enumerateGroup
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateGroup()
    {
        return _groupList.elements();
    } //-- java.util.Enumeration enumerateGroup() 

    /**
     * Method enumerateSetting
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateSetting()
    {
        return _settingList.elements();
    } //-- java.util.Enumeration enumerateSetting() 

    /**
     * Method getGroup
     * 
     * 
     * 
     * @param index
     * @return Group
     */
    public jalview.schemabinding.version2.Group getGroup(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _groupList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.Group) _groupList.elementAt(index);
    } //-- jalview.schemabinding.version2.Group getGroup(int) 

    /**
     * Method getGroup
     * 
     * 
     * 
     * @return Group
     */
    public jalview.schemabinding.version2.Group[] getGroup()
    {
        int size = _groupList.size();
        jalview.schemabinding.version2.Group[] mArray = new jalview.schemabinding.version2.Group[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.Group) _groupList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.Group[] getGroup() 

    /**
     * Method getGroupCount
     * 
     * 
     * 
     * @return int
     */
    public int getGroupCount()
    {
        return _groupList.size();
    } //-- int getGroupCount() 

    /**
     * Method getSetting
     * 
     * 
     * 
     * @param index
     * @return Setting
     */
    public jalview.schemabinding.version2.Setting getSetting(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _settingList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.Setting) _settingList.elementAt(index);
    } //-- jalview.schemabinding.version2.Setting getSetting(int) 

    /**
     * Method getSetting
     * 
     * 
     * 
     * @return Setting
     */
    public jalview.schemabinding.version2.Setting[] getSetting()
    {
        int size = _settingList.size();
        jalview.schemabinding.version2.Setting[] mArray = new jalview.schemabinding.version2.Setting[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.Setting) _settingList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.Setting[] getSetting() 

    /**
     * Method getSettingCount
     * 
     * 
     * 
     * @return int
     */
    public int getSettingCount()
    {
        return _settingList.size();
    } //-- int getSettingCount() 

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
     * Method removeAllGroup
     * 
     */
    public void removeAllGroup()
    {
        _groupList.removeAllElements();
    } //-- void removeAllGroup() 

    /**
     * Method removeAllSetting
     * 
     */
    public void removeAllSetting()
    {
        _settingList.removeAllElements();
    } //-- void removeAllSetting() 

    /**
     * Method removeGroup
     * 
     * 
     * 
     * @param index
     * @return Group
     */
    public jalview.schemabinding.version2.Group removeGroup(int index)
    {
        java.lang.Object obj = _groupList.elementAt(index);
        _groupList.removeElementAt(index);
        return (jalview.schemabinding.version2.Group) obj;
    } //-- jalview.schemabinding.version2.Group removeGroup(int) 

    /**
     * Method removeSetting
     * 
     * 
     * 
     * @param index
     * @return Setting
     */
    public jalview.schemabinding.version2.Setting removeSetting(int index)
    {
        java.lang.Object obj = _settingList.elementAt(index);
        _settingList.removeElementAt(index);
        return (jalview.schemabinding.version2.Setting) obj;
    } //-- jalview.schemabinding.version2.Setting removeSetting(int) 

    /**
     * Method setGroup
     * 
     * 
     * 
     * @param index
     * @param vGroup
     */
    public void setGroup(int index, jalview.schemabinding.version2.Group vGroup)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _groupList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _groupList.setElementAt(vGroup, index);
    } //-- void setGroup(int, jalview.schemabinding.version2.Group) 

    /**
     * Method setGroup
     * 
     * 
     * 
     * @param groupArray
     */
    public void setGroup(jalview.schemabinding.version2.Group[] groupArray)
    {
        //-- copy array
        _groupList.removeAllElements();
        for (int i = 0; i < groupArray.length; i++) {
            _groupList.addElement(groupArray[i]);
        }
    } //-- void setGroup(jalview.schemabinding.version2.Group) 

    /**
     * Method setSetting
     * 
     * 
     * 
     * @param index
     * @param vSetting
     */
    public void setSetting(int index, jalview.schemabinding.version2.Setting vSetting)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _settingList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _settingList.setElementAt(vSetting, index);
    } //-- void setSetting(int, jalview.schemabinding.version2.Setting) 

    /**
     * Method setSetting
     * 
     * 
     * 
     * @param settingArray
     */
    public void setSetting(jalview.schemabinding.version2.Setting[] settingArray)
    {
        //-- copy array
        _settingList.removeAllElements();
        for (int i = 0; i < settingArray.length; i++) {
            _settingList.addElement(settingArray[i]);
        }
    } //-- void setSetting(jalview.schemabinding.version2.Setting) 

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
        return (jalview.schemabinding.version2.FeatureSettings) Unmarshaller.unmarshal(jalview.schemabinding.version2.FeatureSettings.class, reader);
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
