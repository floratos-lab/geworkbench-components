/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: Annotation.java,v 1.1 2008-02-19 16:22:48 wangm Exp $
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
 * Class Annotation.
 * 
 * @version $Revision: 1.1 $ $Date: 2008-02-19 16:22:48 $
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


      //----------------/
     //- Constructors -/
    //----------------/

    public Annotation() {
        super();
        _annotationElementList = new Vector();
    } //-- jalview.binding.Annotation()


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
    public void addAnnotationElement(jalview.binding.AnnotationElement vAnnotationElement)
        throws java.lang.IndexOutOfBoundsException
    {
        _annotationElementList.addElement(vAnnotationElement);
    } //-- void addAnnotationElement(jalview.binding.AnnotationElement) 

    /**
     * Method addAnnotationElement
     * 
     * 
     * 
     * @param index
     * @param vAnnotationElement
     */
    public void addAnnotationElement(int index, jalview.binding.AnnotationElement vAnnotationElement)
        throws java.lang.IndexOutOfBoundsException
    {
        _annotationElementList.insertElementAt(vAnnotationElement, index);
    } //-- void addAnnotationElement(int, jalview.binding.AnnotationElement) 

    /**
     * Method deleteGraph
     * 
     */
    public void deleteGraph()
    {
        this._has_graph= false;
    } //-- void deleteGraph() 

    /**
     * Method deleteGraphType
     * 
     */
    public void deleteGraphType()
    {
        this._has_graphType= false;
    } //-- void deleteGraphType() 

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
    public jalview.binding.AnnotationElement getAnnotationElement(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _annotationElementList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (jalview.binding.AnnotationElement) _annotationElementList.elementAt(index);
    } //-- jalview.binding.AnnotationElement getAnnotationElement(int) 

    /**
     * Method getAnnotationElement
     * 
     * 
     * 
     * @return AnnotationElement
     */
    public jalview.binding.AnnotationElement[] getAnnotationElement()
    {
        int size = _annotationElementList.size();
        jalview.binding.AnnotationElement[] mArray = new jalview.binding.AnnotationElement[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (jalview.binding.AnnotationElement) _annotationElementList.elementAt(index);
        }
        return mArray;
    } //-- jalview.binding.AnnotationElement[] getAnnotationElement() 

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
    public jalview.binding.AnnotationElement removeAnnotationElement(int index)
    {
        java.lang.Object obj = _annotationElementList.elementAt(index);
        _annotationElementList.removeElementAt(index);
        return (jalview.binding.AnnotationElement) obj;
    } //-- jalview.binding.AnnotationElement removeAnnotationElement(int) 

    /**
     * Method setAnnotationElement
     * 
     * 
     * 
     * @param index
     * @param vAnnotationElement
     */
    public void setAnnotationElement(int index, jalview.binding.AnnotationElement vAnnotationElement)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _annotationElementList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _annotationElementList.setElementAt(vAnnotationElement, index);
    } //-- void setAnnotationElement(int, jalview.binding.AnnotationElement) 

    /**
     * Method setAnnotationElement
     * 
     * 
     * 
     * @param annotationElementArray
     */
    public void setAnnotationElement(jalview.binding.AnnotationElement[] annotationElementArray)
    {
        //-- copy array
        _annotationElementList.removeAllElements();
        for (int i = 0; i < annotationElementArray.length; i++) {
            _annotationElementList.addElement(annotationElementArray[i]);
        }
    } //-- void setAnnotationElement(jalview.binding.AnnotationElement) 

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
     * Sets the value of field 'label'.
     * 
     * @param label the value of field 'label'.
     */
    public void setLabel(java.lang.String label)
    {
        this._label = label;
    } //-- void setLabel(java.lang.String) 

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
        return (jalview.binding.Annotation) Unmarshaller.unmarshal(jalview.binding.Annotation.class, reader);
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
