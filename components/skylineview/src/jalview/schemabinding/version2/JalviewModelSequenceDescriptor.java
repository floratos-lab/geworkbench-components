/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: JalviewModelSequenceDescriptor.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
 */

package jalview.schemabinding.version2;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.xml.TypeValidator;
import org.exolab.castor.xml.XMLFieldDescriptor;
import org.exolab.castor.xml.validators.*;

/**
 * Class JalviewModelSequenceDescriptor.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
 */
public class JalviewModelSequenceDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field nsPrefix
     */
    private java.lang.String nsPrefix;

    /**
     * Field nsURI
     */
    private java.lang.String nsURI;

    /**
     * Field xmlName
     */
    private java.lang.String xmlName;

    /**
     * Field identity
     */
    private org.exolab.castor.xml.XMLFieldDescriptor identity;


      //----------------/
     //- Constructors -/
    //----------------/

    public JalviewModelSequenceDescriptor() {
        super();
        nsURI = "www.jalview.org";
        
        //-- set grouping compositor
        setCompositorAsSequence();
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl  desc           = null;
        org.exolab.castor.xml.XMLFieldHandler              handler        = null;
        org.exolab.castor.xml.FieldValidator               fieldValidator = null;
        //-- initialize attribute descriptors
        
        //-- initialize element descriptors
        
        //-- _JSeqList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(jalview.schemabinding.version2.JSeq.class, "_JSeqList", "JSeq", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                JalviewModelSequence target = (JalviewModelSequence) object;
                return target.getJSeq();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    JalviewModelSequence target = (JalviewModelSequence) object;
                    target.addJSeq( (jalview.schemabinding.version2.JSeq) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new jalview.schemabinding.version2.JSeq();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("www.jalview.org");
        desc.setRequired(true);
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _JSeqList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _JGroupList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(jalview.schemabinding.version2.JGroup.class, "_JGroupList", "JGroup", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                JalviewModelSequence target = (JalviewModelSequence) object;
                return target.getJGroup();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    JalviewModelSequence target = (JalviewModelSequence) object;
                    target.addJGroup( (jalview.schemabinding.version2.JGroup) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new jalview.schemabinding.version2.JGroup();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("www.jalview.org");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _JGroupList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _viewportList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(jalview.schemabinding.version2.Viewport.class, "_viewportList", "Viewport", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                JalviewModelSequence target = (JalviewModelSequence) object;
                return target.getViewport();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    JalviewModelSequence target = (JalviewModelSequence) object;
                    target.addViewport( (jalview.schemabinding.version2.Viewport) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new jalview.schemabinding.version2.Viewport();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("www.jalview.org");
        desc.setRequired(true);
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _viewportList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _userColoursList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(jalview.schemabinding.version2.UserColours.class, "_userColoursList", "UserColours", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                JalviewModelSequence target = (JalviewModelSequence) object;
                return target.getUserColours();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    JalviewModelSequence target = (JalviewModelSequence) object;
                    target.addUserColours( (jalview.schemabinding.version2.UserColours) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new jalview.schemabinding.version2.UserColours();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("www.jalview.org");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _userColoursList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _treeList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(jalview.schemabinding.version2.Tree.class, "_treeList", "tree", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                JalviewModelSequence target = (JalviewModelSequence) object;
                return target.getTree();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    JalviewModelSequence target = (JalviewModelSequence) object;
                    target.addTree( (jalview.schemabinding.version2.Tree) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new jalview.schemabinding.version2.Tree();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("www.jalview.org");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _treeList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _featureSettings
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(jalview.schemabinding.version2.FeatureSettings.class, "_featureSettings", "FeatureSettings", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                JalviewModelSequence target = (JalviewModelSequence) object;
                return target.getFeatureSettings();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    JalviewModelSequence target = (JalviewModelSequence) object;
                    target.setFeatureSettings( (jalview.schemabinding.version2.FeatureSettings) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new jalview.schemabinding.version2.FeatureSettings();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("www.jalview.org");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        
        //-- validation code for: _featureSettings
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
    } //-- jalview.schemabinding.version2.JalviewModelSequenceDescriptor()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method getAccessMode
     * 
     * 
     * 
     * @return AccessMode
     */
    public org.exolab.castor.mapping.AccessMode getAccessMode()
    {
        return null;
    } //-- org.exolab.castor.mapping.AccessMode getAccessMode() 

    /**
     * Method getExtends
     * 
     * 
     * 
     * @return ClassDescriptor
     */
    public org.exolab.castor.mapping.ClassDescriptor getExtends()
    {
        return null;
    } //-- org.exolab.castor.mapping.ClassDescriptor getExtends() 

    /**
     * Method getIdentity
     * 
     * 
     * 
     * @return FieldDescriptor
     */
    public org.exolab.castor.mapping.FieldDescriptor getIdentity()
    {
        return identity;
    } //-- org.exolab.castor.mapping.FieldDescriptor getIdentity() 

    /**
     * Method getJavaClass
     * 
     * 
     * 
     * @return Class
     */
    public java.lang.Class getJavaClass()
    {
        return jalview.schemabinding.version2.JalviewModelSequence.class;
    } //-- java.lang.Class getJavaClass() 

    /**
     * Method getNameSpacePrefix
     * 
     * 
     * 
     * @return String
     */
    public java.lang.String getNameSpacePrefix()
    {
        return nsPrefix;
    } //-- java.lang.String getNameSpacePrefix() 

    /**
     * Method getNameSpaceURI
     * 
     * 
     * 
     * @return String
     */
    public java.lang.String getNameSpaceURI()
    {
        return nsURI;
    } //-- java.lang.String getNameSpaceURI() 

    /**
     * Method getValidator
     * 
     * 
     * 
     * @return TypeValidator
     */
    public org.exolab.castor.xml.TypeValidator getValidator()
    {
        return this;
    } //-- org.exolab.castor.xml.TypeValidator getValidator() 

    /**
     * Method getXMLName
     * 
     * 
     * 
     * @return String
     */
    public java.lang.String getXMLName()
    {
        return xmlName;
    } //-- java.lang.String getXMLName() 

}
