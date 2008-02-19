/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Alignment.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
 */

package jalview.binding;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * Class Alignment.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class Alignment implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _annotation
     */
    private jalview.binding.Annotation _annotation;

    /**
     * Field _sequenceSet
     */
    private jalview.binding.SequenceSet _sequenceSet;


      //----------------/
     //- Constructors -/
    //----------------/

    public Alignment() {
        super();
    } //-- jalview.binding.Alignment()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'annotation'.
     * 
     * @return Annotation
     * @return the value of field 'annotation'.
     */
    public jalview.binding.Annotation getAnnotation()
    {
        return this._annotation;
    } //-- jalview.binding.Annotation getAnnotation() 

    /**
     * Returns the value of field 'sequenceSet'.
     * 
     * @return SequenceSet
     * @return the value of field 'sequenceSet'.
     */
    public jalview.binding.SequenceSet getSequenceSet()
    {
        return this._sequenceSet;
    } //-- jalview.binding.SequenceSet getSequenceSet() 

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
     * Sets the value of field 'annotation'.
     * 
     * @param annotation the value of field 'annotation'.
     */
    public void setAnnotation(jalview.binding.Annotation annotation)
    {
        this._annotation = annotation;
    } //-- void setAnnotation(jalview.binding.Annotation) 

    /**
     * Sets the value of field 'sequenceSet'.
     * 
     * @param sequenceSet the value of field 'sequenceSet'.
     */
    public void setSequenceSet(jalview.binding.SequenceSet sequenceSet)
    {
        this._sequenceSet = sequenceSet;
    } //-- void setSequenceSet(jalview.binding.SequenceSet) 

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
        return (jalview.binding.Alignment) Unmarshaller.unmarshal(jalview.binding.Alignment.class, reader);
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
