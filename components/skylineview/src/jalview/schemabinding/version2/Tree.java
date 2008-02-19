/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Tree.java,v 1.1 2008-02-19 16:22:46 wangm Exp $
 */

package jalview.schemabinding.version2;

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
 * Class Tree.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:46 $
 */
public class Tree implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

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

    /**
     * Field _threshold
     */
    private float _threshold;

    /**
     * keeps track of state for field: _threshold
     */
    private boolean _has_threshold;

    /**
     * Field _showBootstrap
     */
    private boolean _showBootstrap;

    /**
     * keeps track of state for field: _showBootstrap
     */
    private boolean _has_showBootstrap;

    /**
     * Field _showDistances
     */
    private boolean _showDistances;

    /**
     * keeps track of state for field: _showDistances
     */
    private boolean _has_showDistances;

    /**
     * Field _markUnlinked
     */
    private boolean _markUnlinked;

    /**
     * keeps track of state for field: _markUnlinked
     */
    private boolean _has_markUnlinked;

    /**
     * Field _fitToWindow
     */
    private boolean _fitToWindow;

    /**
     * keeps track of state for field: _fitToWindow
     */
    private boolean _has_fitToWindow;

    /**
     * Field _currentTree
     */
    private boolean _currentTree;

    /**
     * keeps track of state for field: _currentTree
     */
    private boolean _has_currentTree;

    /**
     * Field _title
     */
    private java.lang.String _title;

    /**
     * Field _newick
     */
    private java.lang.String _newick;


      //----------------/
     //- Constructors -/
    //----------------/

    public Tree() {
        super();
    } //-- jalview.schemabinding.version2.Tree()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteCurrentTree
     * 
     */
    public void deleteCurrentTree()
    {
        this._has_currentTree= false;
    } //-- void deleteCurrentTree() 

    /**
     * Method deleteFitToWindow
     * 
     */
    public void deleteFitToWindow()
    {
        this._has_fitToWindow= false;
    } //-- void deleteFitToWindow() 

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
     * Method deleteMarkUnlinked
     * 
     */
    public void deleteMarkUnlinked()
    {
        this._has_markUnlinked= false;
    } //-- void deleteMarkUnlinked() 

    /**
     * Method deleteShowBootstrap
     * 
     */
    public void deleteShowBootstrap()
    {
        this._has_showBootstrap= false;
    } //-- void deleteShowBootstrap() 

    /**
     * Method deleteShowDistances
     * 
     */
    public void deleteShowDistances()
    {
        this._has_showDistances= false;
    } //-- void deleteShowDistances() 

    /**
     * Method deleteThreshold
     * 
     */
    public void deleteThreshold()
    {
        this._has_threshold= false;
    } //-- void deleteThreshold() 

    /**
     * Method deleteWidth
     * 
     */
    public void deleteWidth()
    {
        this._has_width= false;
    } //-- void deleteWidth() 

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
     * Returns the value of field 'currentTree'.
     * 
     * @return boolean
     * @return the value of field 'currentTree'.
     */
    public boolean getCurrentTree()
    {
        return this._currentTree;
    } //-- boolean getCurrentTree() 

    /**
     * Returns the value of field 'fitToWindow'.
     * 
     * @return boolean
     * @return the value of field 'fitToWindow'.
     */
    public boolean getFitToWindow()
    {
        return this._fitToWindow;
    } //-- boolean getFitToWindow() 

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
     * Returns the value of field 'markUnlinked'.
     * 
     * @return boolean
     * @return the value of field 'markUnlinked'.
     */
    public boolean getMarkUnlinked()
    {
        return this._markUnlinked;
    } //-- boolean getMarkUnlinked() 

    /**
     * Returns the value of field 'newick'.
     * 
     * @return String
     * @return the value of field 'newick'.
     */
    public java.lang.String getNewick()
    {
        return this._newick;
    } //-- java.lang.String getNewick() 

    /**
     * Returns the value of field 'showBootstrap'.
     * 
     * @return boolean
     * @return the value of field 'showBootstrap'.
     */
    public boolean getShowBootstrap()
    {
        return this._showBootstrap;
    } //-- boolean getShowBootstrap() 

    /**
     * Returns the value of field 'showDistances'.
     * 
     * @return boolean
     * @return the value of field 'showDistances'.
     */
    public boolean getShowDistances()
    {
        return this._showDistances;
    } //-- boolean getShowDistances() 

    /**
     * Returns the value of field 'threshold'.
     * 
     * @return float
     * @return the value of field 'threshold'.
     */
    public float getThreshold()
    {
        return this._threshold;
    } //-- float getThreshold() 

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
     * Method hasCurrentTree
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasCurrentTree()
    {
        return this._has_currentTree;
    } //-- boolean hasCurrentTree() 

    /**
     * Method hasFitToWindow
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasFitToWindow()
    {
        return this._has_fitToWindow;
    } //-- boolean hasFitToWindow() 

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
     * Method hasMarkUnlinked
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasMarkUnlinked()
    {
        return this._has_markUnlinked;
    } //-- boolean hasMarkUnlinked() 

    /**
     * Method hasShowBootstrap
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShowBootstrap()
    {
        return this._has_showBootstrap;
    } //-- boolean hasShowBootstrap() 

    /**
     * Method hasShowDistances
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasShowDistances()
    {
        return this._has_showDistances;
    } //-- boolean hasShowDistances() 

    /**
     * Method hasThreshold
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasThreshold()
    {
        return this._has_threshold;
    } //-- boolean hasThreshold() 

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
     * Sets the value of field 'currentTree'.
     * 
     * @param currentTree the value of field 'currentTree'.
     */
    public void setCurrentTree(boolean currentTree)
    {
        this._currentTree = currentTree;
        this._has_currentTree = true;
    } //-- void setCurrentTree(boolean) 

    /**
     * Sets the value of field 'fitToWindow'.
     * 
     * @param fitToWindow the value of field 'fitToWindow'.
     */
    public void setFitToWindow(boolean fitToWindow)
    {
        this._fitToWindow = fitToWindow;
        this._has_fitToWindow = true;
    } //-- void setFitToWindow(boolean) 

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
     * Sets the value of field 'markUnlinked'.
     * 
     * @param markUnlinked the value of field 'markUnlinked'.
     */
    public void setMarkUnlinked(boolean markUnlinked)
    {
        this._markUnlinked = markUnlinked;
        this._has_markUnlinked = true;
    } //-- void setMarkUnlinked(boolean) 

    /**
     * Sets the value of field 'newick'.
     * 
     * @param newick the value of field 'newick'.
     */
    public void setNewick(java.lang.String newick)
    {
        this._newick = newick;
    } //-- void setNewick(java.lang.String) 

    /**
     * Sets the value of field 'showBootstrap'.
     * 
     * @param showBootstrap the value of field 'showBootstrap'.
     */
    public void setShowBootstrap(boolean showBootstrap)
    {
        this._showBootstrap = showBootstrap;
        this._has_showBootstrap = true;
    } //-- void setShowBootstrap(boolean) 

    /**
     * Sets the value of field 'showDistances'.
     * 
     * @param showDistances the value of field 'showDistances'.
     */
    public void setShowDistances(boolean showDistances)
    {
        this._showDistances = showDistances;
        this._has_showDistances = true;
    } //-- void setShowDistances(boolean) 

    /**
     * Sets the value of field 'threshold'.
     * 
     * @param threshold the value of field 'threshold'.
     */
    public void setThreshold(float threshold)
    {
        this._threshold = threshold;
        this._has_threshold = true;
    } //-- void setThreshold(float) 

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
        return (jalview.schemabinding.version2.Tree) Unmarshaller.unmarshal(jalview.schemabinding.version2.Tree.class, reader);
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
