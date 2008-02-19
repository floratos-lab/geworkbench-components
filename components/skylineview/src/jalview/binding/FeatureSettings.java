/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: FeatureSettings.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
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
 * Class FeatureSettings.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class FeatureSettings implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _settingList
     */
    private java.util.Vector _settingList;


      //----------------/
     //- Constructors -/
    //----------------/

    public FeatureSettings() {
        super();
        _settingList = new Vector();
    } //-- jalview.binding.FeatureSettings()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addSetting
     * 
     * 
     * 
     * @param vSetting
     */
    public void addSetting(jalview.binding.Setting vSetting)
        throws java.lang.IndexOutOfBoundsException
    {
        _settingList.addElement(vSetting);
    } //-- void addSetting(jalview.binding.Setting) 

    /**
     * Method addSetting
     * 
     * 
     * 
     * @param index
     * @param vSetting
     */
    public void addSetting(int index, jalview.binding.Setting vSetting)
        throws java.lang.IndexOutOfBoundsException
    {
        _settingList.insertElementAt(vSetting, index);
    } //-- void addSetting(int, jalview.binding.Setting) 

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
     * Method getSetting
     * 
     * 
     * 
     * @param index
     * @return Setting
     */
    public jalview.binding.Setting getSetting(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _settingList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.Setting) _settingList.elementAt(index);
    } //-- jalview.binding.Setting getSetting(int) 

    /**
     * Method getSetting
     * 
     * 
     * 
     * @return Setting
     */
    public jalview.binding.Setting[] getSetting()
    {
        int size = _settingList.size();
        jalview.binding.Setting[] mArray = new jalview.binding.Setting[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.Setting) _settingList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.Setting[] getSetting() 

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
     * Method removeAllSetting
     * 
     */
    public void removeAllSetting()
    {
        _settingList.removeAllElements();
    } //-- void removeAllSetting() 

    /**
     * Method removeSetting
     * 
     * 
     * 
     * @param index
     * @return Setting
     */
    public jalview.binding.Setting removeSetting(int index)
    {
        java.lang.Object obj = _settingList.elementAt(index);
        _settingList.removeElementAt(index);
        return (jalview.binding.Setting) obj;
    } //-- jalview.binding.Setting removeSetting(int) 

    /**
     * Method setSetting
     * 
     * 
     * 
     * @param index
     * @param vSetting
     */
    public void setSetting(int index, jalview.binding.Setting vSetting)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _settingList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _settingList.setElementAt(vSetting, index);
    } //-- void setSetting(int, jalview.binding.Setting) 

    /**
     * Method setSetting
     * 
     * 
     * 
     * @param settingArray
     */
    public void setSetting(jalview.binding.Setting[] settingArray)
    {
        //-- copy array
        _settingList.removeAllElements();
        for (int i = 0; i < settingArray.length; i++) {
            _settingList.addElement(settingArray[i]);
        }
    } //-- void setSetting(jalview.binding.Setting) 

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
        return (jalview.binding.FeatureSettings) Unmarshaller.unmarshal(jalview.binding.FeatureSettings.class, reader);
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
