/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Viewport.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
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
 * Class Viewport.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
 */
public class Viewport implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _conservationSelected
     */
    private boolean _conservationSelected;

    /**
     * keeps track of state for field: _conservationSelected
     */
    private boolean _has_conservationSelected;

    /**
     * Field _pidSelected
     */
    private boolean _pidSelected;

    /**
     * keeps track of state for field: _pidSelected
     */
    private boolean _has_pidSelected;

    /**
     * Field _bgColour
     */
    private java.lang.String _bgColour;

    /**
     * Field _consThreshold
     */
    private int _consThreshold;

    /**
     * keeps track of state for field: _consThreshold
     */
    private boolean _has_consThreshold;

    /**
     * Field _pidThreshold
     */
    private int _pidThreshold;

    /**
     * keeps track of state for field: _pidThreshold
     */
    private boolean _has_pidThreshold;

    /**
     * Field _title
     */
    private java.lang.String _title;

    /**
     * Field _showFullId
     */
    private boolean _showFullId;

    /**
     * keeps track of state for field: _showFullId
     */
    private boolean _has_showFullId;

    /**
     * Field _showText
     */
    private boolean _showText;

    /**
     * keeps track of state for field: _showText
     */
    private boolean _has_showText;

    /**
     * Field _showColourText
     */
    private boolean _showColourText;

    /**
     * keeps track of state for field: _showColourText
     */
    private boolean _has_showColourText;

    /**
     * Field _showBoxes
     */
    private boolean _showBoxes;

    /**
     * keeps track of state for field: _showBoxes
     */
    private boolean _has_showBoxes;

    /**
     * Field _wrapAlignment
     */
    private boolean _wrapAlignment;

    /**
     * keeps track of state for field: _wrapAlignment
     */
    private boolean _has_wrapAlignment;

    /**
     * Field _renderGaps
     */
    private boolean _renderGaps;

    /**
     * keeps track of state for field: _renderGaps
     */
    private boolean _has_renderGaps;

    /**
     * Field _showSequenceFeatures
     */
    private boolean _showSequenceFeatures;

    /**
     * keeps track of state for field: _showSequenceFeatures
     */
    private boolean _has_showSequenceFeatures;

    /**
     * Field _showAnnotation
     */
    private boolean _showAnnotation;

    /**
     * keeps track of state for field: _showAnnotation
     */
    private boolean _has_showAnnotation;

    /**
     * Field _showConservation
     */
    private boolean _showConservation;

    /**
     * keeps track of state for field: _showConservation
     */
    private boolean _has_showConservation;

    /**
     * Field _showQuality
     */
    private boolean _showQuality;

    /**
     * keeps track of state for field: _showQuality
     */
    private boolean _has_showQuality;

    /**
     * Field _showIdentity
     */
    private boolean _showIdentity;

    /**
     * keeps track of state for field: _showIdentity
     */
    private boolean _has_showIdentity;

    /**
     * Field _xpos
     */
    private int _xpos;

    /**
     * keeps track of state for field: _xpos
     */
    private boolean _has_xpos;

    /**
     * Field _ypos
     */
    private int _ypos;

    /**
     * keeps track of state for field: _ypos
     */
    private boolean _has_ypos;

    /**
     * Field _width
     */
    private int _width;

    /**
     * keeps track of state for field: _width
     */
    private boolean _has_width;

    /**
     * Field _height
     */
    private int _height;

    /**
     * keeps track of state for field: _height
     */
    private boolean _has_height;

    /**
     * Field _startRes
     */
    private int _startRes;

    /**
     * keeps track of state for field: _startRes
     */
    private boolean _has_startRes;

    /**
     * Field _startSeq
     */
    private int _startSeq;

    /**
     * keeps track of state for field: _startSeq
     */
    private boolean _has_startSeq;

    /**
     * Field _fontName
     */
    private java.lang.String _fontName;

    /**
     * Field _fontSize
     */
    private int _fontSize;

    /**
     * keeps track of state for field: _fontSize
     */
    private boolean _has_fontSize;

    /**
     * Field _fontStyle
     */
    private int _fontStyle;

    /**
     * keeps track of state for field: _fontStyle
     */
    private boolean _has_fontStyle;


      //----------------/
     //- Constructors -/
    //----------------/

    public Viewport() {
        super();
    } //-- jalview.binding.Viewport()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteConsThreshold
     * 
     */
    public void deleteConsThreshold()
    {
        this._has_consThreshold= false;
    } //-- void deleteConsThreshold() 

    /**
     * Method deleteConservationSelected
     * 
     */
    public void deleteConservationSelected()
    {
        this._has_conservationSelected= false;
    } //-- void deleteConservationSelected() 

    /**
     * Method deleteFontSize
     * 
     */
    public void deleteFontSize()
    {
        this._has_fontSize= false;
    } //-- void deleteFontSize() 

    /**
     * Method deleteFontStyle
     * 
     */
    public void deleteFontStyle()
    {
        this._has_fontStyle= false;
    } //-- void deleteFontStyle() 

    /**
     * Method deleteHeight
     * 
     */
    public void deleteHeight()
    {
        this._has_height= false;
    } //-- void deleteHeight() 

    /**
     * Method deletePidSelected
     * 
     */
    public void deletePidSelected()
    {
        this._has_pidSelected= false;
    } //-- void deletePidSelected() 

    /**
     * Method deletePidThreshold
     * 
     */
    public void deletePidThreshold()
    {
        this._has_pidThreshold= false;
    } //-- void deletePidThreshold() 

    /**
     * Method deleteRenderGaps
     * 
     */
    public void deleteRenderGaps()
    {
        this._has_renderGaps= false;
    } //-- void deleteRenderGaps() 

    /**
     * Method deleteShowAnnotation
     * 
     */
    public void deleteShowAnnotation()
    {
        this._has_showAnnotation= false;
    } //-- void deleteShowAnnotation() 

    /**
     * Method deleteShowBoxes
     * 
     */
    public void deleteShowBoxes()
    {
        this._has_showBoxes= false;
    } //-- void deleteShowBoxes() 

    /**
     * Method deleteShowColourText
     * 
     */
    public void deleteShowColourText()
    {
        this._has_showColourText= false;
    } //-- void deleteShowColourText() 

    /**
     * Method deleteShowConservation
     * 
     */
    public void deleteShowConservation()
    {
        this._has_showConservation= false;
    } //-- void deleteShowConservation() 

    /**
     * Method deleteShowFullId
     * 
     */
    public void deleteShowFullId()
    {
        this._has_showFullId= false;
    } //-- void deleteShowFullId() 

    /**
     * Method deleteShowIdentity
     * 
     */
    public void deleteShowIdentity()
    {
        this._has_showIdentity= false;
    } //-- void deleteShowIdentity() 

    /**
     * Method deleteShowQuality
     * 
     */
    public void deleteShowQuality()
    {
        this._has_showQuality= false;
    } //-- void deleteShowQuality() 

    /**
     * Method deleteShowSequenceFeatures
     * 
     */
    public void deleteShowSequenceFeatures()
    {
        this._has_showSequenceFeatures= false;
    } //-- void deleteShowSequenceFeatures() 

    /**
     * Method deleteShowText
     * 
     */
    public void deleteShowText()
    {
        this._has_showText= false;
    } //-- void deleteShowText() 

    /**
     * Method deleteStartRes
     * 
     */
    public void deleteStartRes()
    {
        this._has_startRes= false;
    } //-- void deleteStartRes() 

    /**
     * Method deleteStartSeq
     * 
     */
    public void deleteStartSeq()
    {
        this._has_startSeq= false;
    } //-- void deleteStartSeq() 

    /**
     * Method deleteWidth
     * 
     */
    public void deleteWidth()
    {
        this._has_width= false;
    } //-- void deleteWidth() 

    /**
     * Method deleteWrapAlignment
     * 
     */
    public void deleteWrapAlignment()
    {
        this._has_wrapAlignment= false;
    } //-- void deleteWrapAlignment() 

    /**
     * Method deleteXpos
     * 
     */
    public void deleteXpos()
    {
        this._has_xpos= false;
    } //-- void deleteXpos() 

    /**
     * Method deleteYpos
     * 
     */
    public void deleteYpos()
    {
        this._has_ypos= false;
    } //-- void deleteYpos() 

    /**
     * Returns the value of field 'bgColour'.
     * 
     * @return String
     * @return the value of field 'bgColour'.
     */
    public java.lang.String getBgColour()
    {
        return this._bgColour;
    } //-- java.lang.String getBgColour() 

    /**
     * Returns the value of field 'consThreshold'.
     * 
     * @return int
     * @return the value of field 'consThreshold'.
     */
    public int getConsThreshold()
    {
        return this._consThreshold;
    } //-- int getConsThreshold() 

    /**
     * Returns the value of field 'conservationSelected'.
     * 
     * @return boolean
     * @return the value of field 'conservationSelected'.
     */
    public boolean getConservationSelected()
    {
        return this._conservationSelected;
    } //-- boolean getConservationSelected() 

    /**
     * Returns the value of field 'fontName'.
     * 
     * @return String
     * @return the value of field 'fontName'.
     */
    public java.lang.String getFontName()
    {
        return this._fontName;
    } //-- java.lang.String getFontName() 

    /**
     * Returns the value of field 'fontSize'.
     * 
     * @return int
     * @return the value of field 'fontSize'.
     */
    public int getFontSize()
    {
        return this._fontSize;
    } //-- int getFontSize() 

    /**
     * Returns the value of field 'fontStyle'.
     * 
     * @return int
     * @return the value of field 'fontStyle'.
     */
    public int getFontStyle()
    {
        return this._fontStyle;
    } //-- int getFontStyle() 

    /**
     * Returns the value of field 'height'.
     * 
     * @return int
     * @return the value of field 'height'.
     */
    public int getHeight()
    {
        return this._height;
    } //-- int getHeight() 

    /**
     * Returns the value of field 'pidSelected'.
     * 
     * @return boolean
     * @return the value of field 'pidSelected'.
     */
    public boolean getPidSelected()
    {
        return this._pidSelected;
    } //-- boolean getPidSelected() 

    /**
     * Returns the value of field 'pidThreshold'.
     * 
     * @return int
     * @return the value of field 'pidThreshold'.
     */
    public int getPidThreshold()
    {
        return this._pidThreshold;
    } //-- int getPidThreshold() 

    /**
     * Returns the value of field 'renderGaps'.
     * 
     * @return boolean
     * @return the value of field 'renderGaps'.
     */
    public boolean getRenderGaps()
    {
        return this._renderGaps;
    } //-- boolean getRenderGaps() 

    /**
     * Returns the value of field 'showAnnotation'.
     * 
     * @return boolean
     * @return the value of field 'showAnnotation'.
     */
    public boolean getShowAnnotation()
    {
        return this._showAnnotation;
    } //-- boolean getShowAnnotation() 

    /**
     * Returns the value of field 'showBoxes'.
     * 
     * @return boolean
     * @return the value of field 'showBoxes'.
     */
    public boolean getShowBoxes()
    {
        return this._showBoxes;
    } //-- boolean getShowBoxes() 

    /**
     * Returns the value of field 'showColourText'.
     * 
     * @return boolean
     * @return the value of field 'showColourText'.
     */
    public boolean getShowColourText()
    {
        return this._showColourText;
    } //-- boolean getShowColourText() 

    /**
     * Returns the value of field 'showConservation'.
     * 
     * @return boolean
     * @return the value of field 'showConservation'.
     */
    public boolean getShowConservation()
    {
        return this._showConservation;
    } //-- boolean getShowConservation() 

    /**
     * Returns the value of field 'showFullId'.
     * 
     * @return boolean
     * @return the value of field 'showFullId'.
     */
    public boolean getShowFullId()
    {
        return this._showFullId;
    } //-- boolean getShowFullId() 

    /**
     * Returns the value of field 'showIdentity'.
     * 
     * @return boolean
     * @return the value of field 'showIdentity'.
     */
    public boolean getShowIdentity()
    {
        return this._showIdentity;
    } //-- boolean getShowIdentity() 

    /**
     * Returns the value of field 'showQuality'.
     * 
     * @return boolean
     * @return the value of field 'showQuality'.
     */
    public boolean getShowQuality()
    {
        return this._showQuality;
    } //-- boolean getShowQuality() 

    /**
     * Returns the value of field 'showSequenceFeatures'.
     * 
     * @return boolean
     * @return the value of field 'showSequenceFeatures'.
     */
    public boolean getShowSequenceFeatures()
    {
        return this._showSequenceFeatures;
    } //-- boolean getShowSequenceFeatures() 

    /**
     * Returns the value of field 'showText'.
     * 
     * @return boolean
     * @return the value of field 'showText'.
     */
    public boolean getShowText()
    {
        return this._showText;
    } //-- boolean getShowText() 

    /**
     * Returns the value of field 'startRes'.
     * 
     * @return int
     * @return the value of field 'startRes'.
     */
    public int getStartRes()
    {
        return this._startRes;
    } //-- int getStartRes() 

    /**
     * Returns the value of field 'startSeq'.
     * 
     * @return int
     * @return the value of field 'startSeq'.
     */
    public int getStartSeq()
    {
        return this._startSeq;
    } //-- int getStartSeq() 

    /**
     * Returns the value of field 'title'.
     * 
     * @return String
     * @return the value of field 'title'.
     */
    public java.lang.String getTitle()
    {
        return this._title;
    } //-- java.lang.String getTitle() 

    /**
     * Returns the value of field 'width'.
     * 
     * @return int
     * @return the value of field 'width'.
     */
    public int getWidth()
    {
        return this._width;
    } //-- int getWidth() 

    /**
     * Returns the value of field 'wrapAlignment'.
     * 
     * @return boolean
     * @return the value of field 'wrapAlignment'.
     */
    public boolean getWrapAlignment()
    {
        return this._wrapAlignment;
    } //-- boolean getWrapAlignment() 

    /**
     * Returns the value of field 'xpos'.
     * 
     * @return int
     * @return the value of field 'xpos'.
     */
    public int getXpos()
    {
        return this._xpos;
    } //-- int getXpos() 

    /**
     * Returns the value of field 'ypos'.
     * 
     * @return int
     * @return the value of field 'ypos'.
     */
    public int getYpos()
    {
        return this._ypos;
    } //-- int getYpos() 

    /**
     * Method hasConsThreshold
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasConsThreshold()
    {
        return this._has_consThreshold;
    } //-- boolean hasConsThreshold() 

    /**
     * Method hasConservationSelected
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasConservationSelected()
    {
        return this._has_conservationSelected;
    } //-- boolean hasConservationSelected() 

    /**
     * Method hasFontSize
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasFontSize()
    {
        return this._has_fontSize;
    } //-- boolean hasFontSize() 

    /**
     * Method hasFontStyle
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasFontStyle()
    {
        return this._has_fontStyle;
    } //-- boolean hasFontStyle() 

    /**
     * Method hasHeight
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasHeight()
    {
        return this._has_height;
    } //-- boolean hasHeight() 

    /**
     * Method hasPidSelected
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasPidSelected()
    {
        return this._has_pidSelected;
    } //-- boolean hasPidSelected() 

    /**
     * Method hasPidThreshold
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasPidThreshold()
    {
        return this._has_pidThreshold;
    } //-- boolean hasPidThreshold() 

    /**
     * Method hasRenderGaps
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasRenderGaps()
    {
        return this._has_renderGaps;
    } //-- boolean hasRenderGaps() 

    /**
     * Method hasShowAnnotation
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShowAnnotation()
    {
        return this._has_showAnnotation;
    } //-- boolean hasShowAnnotation() 

    /**
     * Method hasShowBoxes
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShowBoxes()
    {
        return this._has_showBoxes;
    } //-- boolean hasShowBoxes() 

    /**
     * Method hasShowColourText
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShowColourText()
    {
        return this._has_showColourText;
    } //-- boolean hasShowColourText() 

    /**
     * Method hasShowConservation
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShowConservation()
    {
        return this._has_showConservation;
    } //-- boolean hasShowConservation() 

    /**
     * Method hasShowFullId
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShowFullId()
    {
        return this._has_showFullId;
    } //-- boolean hasShowFullId() 

    /**
     * Method hasShowIdentity
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShowIdentity()
    {
        return this._has_showIdentity;
    } //-- boolean hasShowIdentity() 

    /**
     * Method hasShowQuality
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShowQuality()
    {
        return this._has_showQuality;
    } //-- boolean hasShowQuality() 

    /**
     * Method hasShowSequenceFeatures
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShowSequenceFeatures()
    {
        return this._has_showSequenceFeatures;
    } //-- boolean hasShowSequenceFeatures() 

    /**
     * Method hasShowText
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShowText()
    {
        return this._has_showText;
    } //-- boolean hasShowText() 

    /**
     * Method hasStartRes
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasStartRes()
    {
        return this._has_startRes;
    } //-- boolean hasStartRes() 

    /**
     * Method hasStartSeq
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasStartSeq()
    {
        return this._has_startSeq;
    } //-- boolean hasStartSeq() 

    /**
     * Method hasWidth
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasWidth()
    {
        return this._has_width;
    } //-- boolean hasWidth() 

    /**
     * Method hasWrapAlignment
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasWrapAlignment()
    {
        return this._has_wrapAlignment;
    } //-- boolean hasWrapAlignment() 

    /**
     * Method hasXpos
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasXpos()
    {
        return this._has_xpos;
    } //-- boolean hasXpos() 

    /**
     * Method hasYpos
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasYpos()
    {
        return this._has_ypos;
    } //-- boolean hasYpos() 

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
     * Sets the value of field 'bgColour'.
     * 
     * @param bgColour the value of field 'bgColour'.
     */
    public void setBgColour(java.lang.String bgColour)
    {
        this._bgColour = bgColour;
    } //-- void setBgColour(java.lang.String) 

    /**
     * Sets the value of field 'consThreshold'.
     * 
     * @param consThreshold the value of field 'consThreshold'.
     */
    public void setConsThreshold(int consThreshold)
    {
        this._consThreshold = consThreshold;
        this._has_consThreshold = true;
    } //-- void setConsThreshold(int) 

    /**
     * Sets the value of field 'conservationSelected'.
     * 
     * @param conservationSelected the value of field
     * 'conservationSelected'.
     */
    public void setConservationSelected(boolean conservationSelected)
    {
        this._conservationSelected = conservationSelected;
        this._has_conservationSelected = true;
    } //-- void setConservationSelected(boolean) 

    /**
     * Sets the value of field 'fontName'.
     * 
     * @param fontName the value of field 'fontName'.
     */
    public void setFontName(java.lang.String fontName)
    {
        this._fontName = fontName;
    } //-- void setFontName(java.lang.String) 

    /**
     * Sets the value of field 'fontSize'.
     * 
     * @param fontSize the value of field 'fontSize'.
     */
    public void setFontSize(int fontSize)
    {
        this._fontSize = fontSize;
        this._has_fontSize = true;
    } //-- void setFontSize(int) 

    /**
     * Sets the value of field 'fontStyle'.
     * 
     * @param fontStyle the value of field 'fontStyle'.
     */
    public void setFontStyle(int fontStyle)
    {
        this._fontStyle = fontStyle;
        this._has_fontStyle = true;
    } //-- void setFontStyle(int) 

    /**
     * Sets the value of field 'height'.
     * 
     * @param height the value of field 'height'.
     */
    public void setHeight(int height)
    {
        this._height = height;
        this._has_height = true;
    } //-- void setHeight(int) 

    /**
     * Sets the value of field 'pidSelected'.
     * 
     * @param pidSelected the value of field 'pidSelected'.
     */
    public void setPidSelected(boolean pidSelected)
    {
        this._pidSelected = pidSelected;
        this._has_pidSelected = true;
    } //-- void setPidSelected(boolean) 

    /**
     * Sets the value of field 'pidThreshold'.
     * 
     * @param pidThreshold the value of field 'pidThreshold'.
     */
    public void setPidThreshold(int pidThreshold)
    {
        this._pidThreshold = pidThreshold;
        this._has_pidThreshold = true;
    } //-- void setPidThreshold(int) 

    /**
     * Sets the value of field 'renderGaps'.
     * 
     * @param renderGaps the value of field 'renderGaps'.
     */
    public void setRenderGaps(boolean renderGaps)
    {
        this._renderGaps = renderGaps;
        this._has_renderGaps = true;
    } //-- void setRenderGaps(boolean) 

    /**
     * Sets the value of field 'showAnnotation'.
     * 
     * @param showAnnotation the value of field 'showAnnotation'.
     */
    public void setShowAnnotation(boolean showAnnotation)
    {
        this._showAnnotation = showAnnotation;
        this._has_showAnnotation = true;
    } //-- void setShowAnnotation(boolean) 

    /**
     * Sets the value of field 'showBoxes'.
     * 
     * @param showBoxes the value of field 'showBoxes'.
     */
    public void setShowBoxes(boolean showBoxes)
    {
        this._showBoxes = showBoxes;
        this._has_showBoxes = true;
    } //-- void setShowBoxes(boolean) 

    /**
     * Sets the value of field 'showColourText'.
     * 
     * @param showColourText the value of field 'showColourText'.
     */
    public void setShowColourText(boolean showColourText)
    {
        this._showColourText = showColourText;
        this._has_showColourText = true;
    } //-- void setShowColourText(boolean) 

    /**
     * Sets the value of field 'showConservation'.
     * 
     * @param showConservation the value of field 'showConservation'
     */
    public void setShowConservation(boolean showConservation)
    {
        this._showConservation = showConservation;
        this._has_showConservation = true;
    } //-- void setShowConservation(boolean) 

    /**
     * Sets the value of field 'showFullId'.
     * 
     * @param showFullId the value of field 'showFullId'.
     */
    public void setShowFullId(boolean showFullId)
    {
        this._showFullId = showFullId;
        this._has_showFullId = true;
    } //-- void setShowFullId(boolean) 

    /**
     * Sets the value of field 'showIdentity'.
     * 
     * @param showIdentity the value of field 'showIdentity'.
     */
    public void setShowIdentity(boolean showIdentity)
    {
        this._showIdentity = showIdentity;
        this._has_showIdentity = true;
    } //-- void setShowIdentity(boolean) 

    /**
     * Sets the value of field 'showQuality'.
     * 
     * @param showQuality the value of field 'showQuality'.
     */
    public void setShowQuality(boolean showQuality)
    {
        this._showQuality = showQuality;
        this._has_showQuality = true;
    } //-- void setShowQuality(boolean) 

    /**
     * Sets the value of field 'showSequenceFeatures'.
     * 
     * @param showSequenceFeatures the value of field
     * 'showSequenceFeatures'.
     */
    public void setShowSequenceFeatures(boolean showSequenceFeatures)
    {
        this._showSequenceFeatures = showSequenceFeatures;
        this._has_showSequenceFeatures = true;
    } //-- void setShowSequenceFeatures(boolean) 

    /**
     * Sets the value of field 'showText'.
     * 
     * @param showText the value of field 'showText'.
     */
    public void setShowText(boolean showText)
    {
        this._showText = showText;
        this._has_showText = true;
    } //-- void setShowText(boolean) 

    /**
     * Sets the value of field 'startRes'.
     * 
     * @param startRes the value of field 'startRes'.
     */
    public void setStartRes(int startRes)
    {
        this._startRes = startRes;
        this._has_startRes = true;
    } //-- void setStartRes(int) 

    /**
     * Sets the value of field 'startSeq'.
     * 
     * @param startSeq the value of field 'startSeq'.
     */
    public void setStartSeq(int startSeq)
    {
        this._startSeq = startSeq;
        this._has_startSeq = true;
    } //-- void setStartSeq(int) 

    /**
     * Sets the value of field 'title'.
     * 
     * @param title the value of field 'title'.
     */
    public void setTitle(java.lang.String title)
    {
        this._title = title;
    } //-- void setTitle(java.lang.String) 

    /**
     * Sets the value of field 'width'.
     * 
     * @param width the value of field 'width'.
     */
    public void setWidth(int width)
    {
        this._width = width;
        this._has_width = true;
    } //-- void setWidth(int) 

    /**
     * Sets the value of field 'wrapAlignment'.
     * 
     * @param wrapAlignment the value of field 'wrapAlignment'.
     */
    public void setWrapAlignment(boolean wrapAlignment)
    {
        this._wrapAlignment = wrapAlignment;
        this._has_wrapAlignment = true;
    } //-- void setWrapAlignment(boolean) 

    /**
     * Sets the value of field 'xpos'.
     * 
     * @param xpos the value of field 'xpos'.
     */
    public void setXpos(int xpos)
    {
        this._xpos = xpos;
        this._has_xpos = true;
    } //-- void setXpos(int) 

    /**
     * Sets the value of field 'ypos'.
     * 
     * @param ypos the value of field 'ypos'.
     */
    public void setYpos(int ypos)
    {
        this._ypos = ypos;
        this._has_ypos = true;
    } //-- void setYpos(int) 

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
        return (jalview.binding.Viewport) Unmarshaller.unmarshal(jalview.binding.Viewport.class, reader);
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
