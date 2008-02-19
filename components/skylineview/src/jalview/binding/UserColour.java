/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: UserColour.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
 */

package jalview.binding;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.*;

/**
 * Class UserColour.
 *
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class UserColour
    implements java.io.Serializable
{

  //--------------------------/
  //- Class/Member Variables -/
  //--------------------------/

  /**
   * Field _id
   */
  private int _id;

  /**
   * keeps track of state for field: _id
   */
  private boolean _has_id;

  /**
   * Field _userColourScheme
   */
  private jalview.binding.UserColourScheme _userColourScheme;

  //----------------/
  //- Constructors -/
  //----------------/

  public UserColour()
  {
    super();
  } //-- jalview.binding.UserColour()

  //-----------/
  //- Methods -/
  //-----------/

  /**
   * Method deleteId
   *
   */
  public void deleteId()
  {
    this._has_id = false;
  } //-- void deleteId()

  /**
   * Returns the value of field 'id'.
   *
   * @return int
   * @return the value of field 'id'.
   */
  public int getId()
  {
    return this._id;
  } //-- int getId()

  /**
   * Returns the value of field 'userColourScheme'.
   *
   * @return UserColourScheme
   * @return the value of field 'userColourScheme'.
   */
  public jalview.binding.UserColourScheme getUserColourScheme()
  {
    return this._userColourScheme;
  } //-- jalview.binding.UserColourScheme getUserColourScheme()

  /**
   * Method hasId
   *
   *
   *
   * @return boolean
   */
  public boolean hasId()
  {
    return this._has_id;
  } //-- boolean hasId()

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
   * Sets the value of field 'id'.
   *
   * @param id the value of field 'id'.
   */
  public void setId(int id)
  {
    this._id = id;
    this._has_id = true;
  } //-- void setId(int)

  /**
   * Sets the value of field 'userColourScheme'.
   *
   * @param userColourScheme the value of field 'userColourScheme'
   */
  public void setUserColourScheme(jalview.binding.UserColourScheme
                                  userColourScheme)
  {
    this._userColourScheme = userColourScheme;
  } //-- void setUserColourScheme(jalview.binding.UserColourScheme)

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
    return (jalview.binding.UserColour) Unmarshaller.unmarshal(jalview.binding.
        UserColour.class, reader);
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
