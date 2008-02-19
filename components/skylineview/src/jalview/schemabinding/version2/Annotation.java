/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Annotation.java,v 1.1 2008-02-19 16:22:47 wangm Exp $
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
 * Class Annotation.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:47 $
 */
public class Annotation implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _graph
     */
    private boolean _graph;

    /**
     * keeps track of state for field: _graph
     */
    private boolean _has_graph;

    /**
     * Field _graphType
     */
    private int _graphType;

    /**
     * keeps track of state for field: _graphType
     */
    private boolean _has_graphType;

    /**
     * Field _sequenceRef
     */
    private java.lang.String _sequenceRef;

    /**
     * Field _graphColour
     */
    private int _graphColour;

    /**
     * keeps track of state for field: _graphColour
     */
    private boolean _has_graphColour;

    /**
     * Field _graphGroup
     */
    private int _graphGroup;

    /**
     * keeps track of state for field: _graphGroup
     */
    private boolean _has_graphGroup;

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _scoreOnly
     */
    private boolean _scoreOnly = false;

    /**
     * keeps track of state for field: _scoreOnly
     */
    private boolean _has_scoreOnly;

    /**
     * Field _score
     */
    private double _score;

    /**
     * keeps track of state for field: _score
     */
    private boolean _has_score;

    /**
     * Field _visible
     */
    private boolean _visible;

    /**
     * keeps track of state for field: _visible
     */
    private boolean _has_visible;

    /**
     * Field _annotationElementList
     */
    private java.util.Vector _annotationElementList;

    /**
     * Field _label
     */
    private java.lang.String _label;

    /**
     * Field _description
     */
    private java.lang.String _description;

    /**
     * Field _thresholdLine
     */
    private jalview.schemabinding.version2.ThresholdLine _thresholdLine;


      //----------------/
     //- Constructors -/
    //----------------/

    public Annotation() {
        super();
        _annotationElementList = new Vector();
    } //-- jalview.schemabinding.version2.Annotation()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addAnnotationElement
     * 
     * 
     * 
     * @param vAnnotationElement
     */
    public void addAnnotationElement(jalview.schemabinding.version2.AnnotationElement vAnnotationElement)
        throws java.lang.IndexOutOfBoundsException
    {
        _annotationElementList.addElement(vAnnotationElement);
    } //-- void addAnnotationElement(jalview.schemabinding.version2.AnnotationElement) 

    /**
     * Method addAnnotationElement
     * 
     * 
     * 
     * @param index
     * @param vAnnotationElement
     */
    public void addAnnotationElement(int index, jalview.schemabinding.version2.AnnotationElement vAnnotationElement)
        throws java.lang.IndexOutOfBoundsException
    {
        _annotationElementList.insertElementAt(vAnnotationElement, index);
    } //-- void addAnnotationElement(int, jalview.schemabinding.version2.AnnotationElement) 

    /**
     * Method deleteGraph
     * 
     */
    public void deleteGraph()
    {
        this._has_graph= false;
    } //-- void deleteGraph() 

    /**
     * Method deleteGraphColour
     * 
     */
    public void deleteGraphColour()
    {
        this._has_graphColour= false;
    } //-- void deleteGraphColour() 

    /**
     * Method deleteGraphGroup
     * 
     */
    public void deleteGraphGroup()
    {
        this._has_graphGroup= false;
    } //-- void deleteGraphGroup() 

    /**
     * Method deleteGraphType
     * 
     */
    public void deleteGraphType()
    {
        this._has_graphType= false;
    } //-- void deleteGraphType() 

    /**
     * Method deleteScore
     * 
     */
    public void deleteScore()
    {
        this._has_score= false;
    } //-- void deleteScore() 

    /**
     * Method deleteScoreOnly
     * 
     */
    public void deleteScoreOnly()
    {
        this._has_scoreOnly= false;
    } //-- void deleteScoreOnly() 

    /**
     * Method deleteVisible
     * 
     */
    public void deleteVisible()
    {
        this._has_visible= false;
    } //-- void deleteVisible() 

    /**
     * Method enumerateAnnotationElement
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateAnnotationElement()
    {
        return _annotationElementList.elements();
    } //-- java.util.Enumeration enumerateAnnotationElement() 

    /**
     * Method getAnnotationElement
     * 
     * 
     * 
     * @param index
     * @return AnnotationElement
     */
    public jalview.schemabinding.version2.AnnotationElement getAnnotationElement(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _annotationElementList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.schemabinding.version2.AnnotationElement) _annotationElementList.elementAt(index);
    } //-- jalview.schemabinding.version2.AnnotationElement getAnnotationElement(int) 

    /**
     * Method getAnnotationElement
     * 
     * 
     * 
     * @return AnnotationElement
     */
    public jalview.schemabinding.version2.AnnotationElement[] getAnnotationElement()
    {
        int size = _annotationElementList.size();
        jalview.schemabinding.version2.AnnotationElement[] mArray = new jalview.schemabinding.version2.AnnotationElement[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.schemabinding.version2.AnnotationElement) _annotationElementList.elementAt(index);
        }
        return mArray;
    } //-- jalview.schemabinding.version2.AnnotationElement[] getAnnotationElement() 

    /**
     * Method getAnnotationElementCount
     * 
     * 
     * 
     * @return int
     */
    public int getAnnotationElementCount()
    {
        return _annotationElementList.size();
    } //-- int getAnnotationElementCount() 

    /**
     * Returns the value of field 'description'.
     * 
     * @return String
     * @return the value of field 'description'.
     */
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * Returns the value of field 'graph'.
     * 
     * @return boolean
     * @return the value of field 'graph'.
     */
    public boolean getGraph()
    {
        return this._graph;
    } //-- boolean getGraph() 

    /**
     * Returns the value of field 'graphColour'.
     * 
     * @return int
     * @return the value of field 'graphColour'.
     */
    public int getGraphColour()
    {
        return this._graphColour;
    } //-- int getGraphColour() 

    /**
     * Returns the value of field 'graphGroup'.
     * 
     * @return int
     * @return the value of field 'graphGroup'.
     */
    public int getGraphGroup()
    {
        return this._graphGroup;
    } //-- int getGraphGroup() 

    /**
     * Returns the value of field 'graphType'.
     * 
     * @return int
     * @return the value of field 'graphType'.
     */
    public int getGraphType()
    {
        return this._graphType;
    } //-- int getGraphType() 

    /**
     * Returns the value of field 'id'.
     * 
     * @return String
     * @return the value of field 'id'.
     */
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Returns the value of field 'label'.
     * 
     * @return String
     * @return the value of field 'label'.
     */
    public java.lang.String getLabel()
    {
        return this._label;
    } //-- java.lang.String getLabel() 

    /**
     * Returns the value of field 'score'.
     * 
     * @return double
     * @return the value of field 'score'.
     */
    public double getScore()
    {
        return this._score;
    } //-- double getScore() 

    /**
     * Returns the value of field 'scoreOnly'.
     * 
     * @return boolean
     * @return the value of field 'scoreOnly'.
     */
    public boolean getScoreOnly()
    {
        return this._scoreOnly;
    } //-- boolean getScoreOnly() 

    /**
     * Returns the value of field 'sequenceRef'.
     * 
     * @return String
     * @return the value of field 'sequenceRef'.
     */
    public java.lang.String getSequenceRef()
    {
        return this._sequenceRef;
    } //-- java.lang.String getSequenceRef() 

    /**
     * Returns the value of field 'thresholdLine'.
     * 
     * @return ThresholdLine
     * @return the value of field 'thresholdLine'.
     */
    public jalview.schemabinding.version2.ThresholdLine getThresholdLine()
    {
        return this._thresholdLine;
    } //-- jalview.schemabinding.version2.ThresholdLine getThresholdLine() 

    /**
     * Returns the value of field 'visible'.
     * 
     * @return boolean
     * @return the value of field 'visible'.
     */
    public boolean getVisible()
    {
        return this._visible;
    } //-- boolean getVisible() 

    /**
     * Method hasGraph
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasGraph()
    {
        return this._has_graph;
    } //-- boolean hasGraph() 

    /**
     * Method hasGraphColour
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasGraphColour()
    {
        return this._has_graphColour;
    } //-- boolean hasGraphColour() 

    /**
     * Method hasGraphGroup
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasGraphGroup()
    {
        return this._has_graphGroup;
    } //-- boolean hasGraphGroup() 

    /**
     * Method hasGraphType
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasGraphType()
    {
        return this._has_graphType;
    } //-- boolean hasGraphType() 

    /**
     * Method hasScore
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasScore()
    {
        return this._has_score;
    } //-- boolean hasScore() 

    /**
     * Method hasScoreOnly
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasScoreOnly()
    {
        return this._has_scoreOnly;
    } //-- boolean hasScoreOnly() 

    /**
     * Method hasVisible
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasVisible()
    {
        return this._has_visible;
    } //-- boolean hasVisible() 

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
     * Method removeAllAnnotationElement
     * 
     */
    public void removeAllAnnotationElement()
    {
        _annotationElementList.removeAllElements();
    } //-- void removeAllAnnotationElement() 

    /**
     * Method removeAnnotationElement
     * 
     * 
     * 
     * @param index
     * @return AnnotationElement
     */
    public jalview.schemabinding.version2.AnnotationElement removeAnnotationElement(int index)
    {
        java.lang.Object obj = _annotationElementList.elementAt(index);
        _annotationElementList.removeElementAt(index);
        return (jalview.schemabinding.version2.AnnotationElement) obj;
    } //-- jalview.schemabinding.version2.AnnotationElement removeAnnotationElement(int) 

    /**
     * Method setAnnotationElement
     * 
     * 
     * 
     * @param index
     * @param vAnnotationElement
     */
    public void setAnnotationElement(int index, jalview.schemabinding.version2.AnnotationElement vAnnotationElement)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _annotationElementList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _annotationElementList.setElementAt(vAnnotationElement, index);
    } //-- void setAnnotationElement(int, jalview.schemabinding.version2.AnnotationElement) 

    /**
     * Method setAnnotationElement
     * 
     * 
     * 
     * @param annotationElementArray
     */
    public void setAnnotationElement(jalview.schemabinding.version2.AnnotationElement[] annotationElementArray)
    {
        //-- copy array
        _annotationElementList.removeAllElements();
        for (int i = 0; i < annotationElementArray.length; i++) {
            _annotationElementList.addElement(annotationElementArray[i]);
        }
    } //-- void setAnnotationElement(jalview.schemabinding.version2.AnnotationElement) 

    /**
     * Sets the value of field 'description'.
     * 
     * @param description the value of field 'description'.
     */
    public void setDescription(java.lang.String description)
    {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * Sets the value of field 'graph'.
     * 
     * @param graph the value of field 'graph'.
     */
    public void setGraph(boolean graph)
    {
        this._graph = graph;
        this._has_graph = true;
    } //-- void setGraph(boolean) 

    /**
     * Sets the value of field 'graphColour'.
     * 
     * @param graphColour the value of field 'graphColour'.
     */
    public void setGraphColour(int graphColour)
    {
        this._graphColour = graphColour;
        this._has_graphColour = true;
    } //-- void setGraphColour(int) 

    /**
     * Sets the value of field 'graphGroup'.
     * 
     * @param graphGroup the value of field 'graphGroup'.
     */
    public void setGraphGroup(int graphGroup)
    {
        this._graphGroup = graphGroup;
        this._has_graphGroup = true;
    } //-- void setGraphGroup(int) 

    /**
     * Sets the value of field 'graphType'.
     * 
     * @param graphType the value of field 'graphType'.
     */
    public void setGraphType(int graphType)
    {
        this._graphType = graphType;
        this._has_graphType = true;
    } //-- void setGraphType(int) 

    /**
     * Sets the value of field 'id'.
     * 
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id)
    {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Sets the value of field 'label'.
     * 
     * @param label the value of field 'label'.
     */
    public void setLabel(java.lang.String label)
    {
        this._label = label;
    } //-- void setLabel(java.lang.String) 

    /**
     * Sets the value of field 'score'.
     * 
     * @param score the value of field 'score'.
     */
    public void setScore(double score)
    {
        this._score = score;
        this._has_score = true;
    } //-- void setScore(double) 

    /**
     * Sets the value of field 'scoreOnly'.
     * 
     * @param scoreOnly the value of field 'scoreOnly'.
     */
    public void setScoreOnly(boolean scoreOnly)
    {
        this._scoreOnly = scoreOnly;
        this._has_scoreOnly = true;
    } //-- void setScoreOnly(boolean) 

    /**
     * Sets the value of field 'sequenceRef'.
     * 
     * @param sequenceRef the value of field 'sequenceRef'.
     */
    public void setSequenceRef(java.lang.String sequenceRef)
    {
        this._sequenceRef = sequenceRef;
    } //-- void setSequenceRef(java.lang.String) 

    /**
     * Sets the value of field 'thresholdLine'.
     * 
     * @param thresholdLine the value of field 'thresholdLine'.
     */
    public void setThresholdLine(jalview.schemabinding.version2.ThresholdLine thresholdLine)
    {
        this._thresholdLine = thresholdLine;
    } //-- void setThresholdLine(jalview.schemabinding.version2.ThresholdLine) 

    /**
     * Sets the value of field 'visible'.
     * 
     * @param visible the value of field 'visible'.
     */
    public void setVisible(boolean visible)
    {
        this._visible = visible;
        this._has_visible = true;
    } //-- void setVisible(boolean) 

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
        return (jalview.schemabinding.version2.Annotation) Unmarshaller.unmarshal(jalview.schemabinding.version2.Annotation.class, reader);
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
