/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: JalviewUserColoursItem.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
 */
package jalview.binding;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/
import java.io.Serializable;


/**
 * Class JalviewUserColoursItem.
 *
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class JalviewUserColoursItem implements java.io.Serializable
{
    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _colour
     */
    private jalview.binding.Colour _colour;

    //----------------/
    //- Constructors -/
    //----------------/
    public JalviewUserColoursItem()
    {
        super();
    }

    //-- jalview.binding.JalviewUserColoursItem()
    //-----------/

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public jalview.binding.Colour getColour()
    {
        return this._colour;
    }

    //-- jalview.binding.Colour getColour() 

    /**
     * Sets the value of field 'colour'.
     *
     * @param colour the value of field 'colour'.
     */
    public void setColour(jalview.binding.Colour colour)
    {
        this._colour = colour;
    }

    //-- void setColour(jalview.binding.Colour) 
}
