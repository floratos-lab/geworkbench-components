/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Residue.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
 */

package jalview.binding;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.*;

/**
 * Class Residue.
 *
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class Residue
    implements java.io.Serializable
{

  //--------------------------/
  //- Class/Member Variables -/
  //--------------------------/

  /**
   * Field _name
   */
  private java.lang.String _name;

  /**
   * Field _RGB
   */
  private int _RGB;

  /**
   * keeps track of state for field: _RGB
   */
  private boolean _has_RGB;

  //----------------/
  //- Constructors -/
  //----------------/

  public Residue()
  {
    super();
  } //-- jalview.binding.Residue()

  //-----------/
  //- Methods -/
  //-----------/

  /**
   * Method deleteRGB
   *
   */
  public void deleteRGB()
  {
    this._has_RGB = false;
  } //-- void deleteRGB()

  /**
   * Returns the value of field 'name'.
   *
   * @return String
   * @return the value of field 'name'.
   */
  public java.lang.String getName()
  {
    return this._name;
  } //-- java.lang.String getName()

  /**
   * Returns the value of field 'RGB'.
   *
   * @return int
   * @return the value of field 'RGB'.
   */
  public int getRGB()
  {
    return this._RGB;
  } //-- int getRGB()

  /**
   * Method hasRGB
   *
   *
   *
   * @return boolean
   */
  public boolean hasRGB()
  {
    return this._has_RGB;
  } //-- boolean hasRGB()

  /**
   * Method isValid
   *
   *
   *
   * @return boolean
   */
  public boolean isValid()
  {
    try
    {
      validate();
    }
    catch (org.exolab.castor.xml.ValidationException vex)
    {
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
      throws org.exolab.castor.xml.MarshalException,
      org.exolab.castor.xml.ValidationException
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
      throws java.io.IOException, org.exolab.castor.xml.MarshalException,
      org.exolab.castor.xml.ValidationException
  {

    Marshaller.marshal(this, (java.io.Writer)handler);
  } //-- void marshal(org.xml.sax.ContentHandler)

  /**
   * Sets the value of field 'name'.
   *
   * @param name the value of field 'name'.
   */
  public void setName(java.lang.String name)
  {
    this._name = name;
  } //-- void setName(java.lang.String)

  /**
   * Sets the value of field 'RGB'.
   *
   * @param RGB the value of field 'RGB'.
   */
  public void setRGB(int RGB)
  {
    this._RGB = RGB;
    this._has_RGB = true;
  } //-- void setRGB(int)

  /**
   * Method unmarshal
   *
   *
   *
   * @param reader
   * @return Object
   */
  public static java.lang.Object unmarshal(java.io.Reader reader)
      throws org.exolab.castor.xml.MarshalException,
      org.exolab.castor.xml.ValidationException
  {
    return (jalview.binding.Residue) Unmarshaller.unmarshal(jalview.binding.
        Residue.class, reader);
  } //-- java.lang.Object unmarshal(java.io.Reader)

  /**
   * Method validate
   *
   */
  public void validate()
      throws org.exolab.castor.xml.ValidationException
  {
    org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.
        Validator();
    validator.validate(this);
  } //-- void validate()

}
