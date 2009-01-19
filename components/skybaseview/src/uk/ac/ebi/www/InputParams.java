/**
 * InputParams.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.www;

public class InputParams
    implements java.io.Serializable
{
  private java.lang.String program;
  private java.lang.String database;
  private java.lang.String matrix;
  private float exp;
  private boolean echofilter;
  private java.lang.String filter;
  private int numal;
  private int scores;
  private java.lang.String sensitivity;
  private java.lang.String sort;
  private java.lang.String stats;
  private java.lang.String strand;
  private java.lang.String outformat;
  private int topcombon;
  private boolean async;
  private java.lang.String email;

  public InputParams()
  {
  }

  public java.lang.String getProgram()
  {
    return program;
  }

  public void setProgram(java.lang.String program)
  {
    this.program = program;
  }

  public java.lang.String getDatabase()
  {
    return database;
  }

  public void setDatabase(java.lang.String database)
  {
    this.database = database;
  }

  public java.lang.String getMatrix()
  {
    return matrix;
  }

  public void setMatrix(java.lang.String matrix)
  {
    this.matrix = matrix;
  }

  public float getExp()
  {
    return exp;
  }

  public void setExp(float exp)
  {
    this.exp = exp;
  }

  public boolean isEchofilter()
  {
    return echofilter;
  }

  public void setEchofilter(boolean echofilter)
  {
    this.echofilter = echofilter;
  }

  public java.lang.String getFilter()
  {
    return filter;
  }

  public void setFilter(java.lang.String filter)
  {
    this.filter = filter;
  }

  public int getNumal()
  {
    return numal;
  }

  public void setNumal(int numal)
  {
    this.numal = numal;
  }

  public int getScores()
  {
    return scores;
  }

  public void setScores(int scores)
  {
    this.scores = scores;
  }

  public java.lang.String getSensitivity()
  {
    return sensitivity;
  }

  public void setSensitivity(java.lang.String sensitivity)
  {
    this.sensitivity = sensitivity;
  }

  public java.lang.String getSort()
  {
    return sort;
  }

  public void setSort(java.lang.String sort)
  {
    this.sort = sort;
  }

  public java.lang.String getStats()
  {
    return stats;
  }

  public void setStats(java.lang.String stats)
  {
    this.stats = stats;
  }

  public java.lang.String getStrand()
  {
    return strand;
  }

  public void setStrand(java.lang.String strand)
  {
    this.strand = strand;
  }

  public java.lang.String getOutformat()
  {
    return outformat;
  }

  public void setOutformat(java.lang.String outformat)
  {
    this.outformat = outformat;
  }

  public int getTopcombon()
  {
    return topcombon;
  }

  public void setTopcombon(int topcombon)
  {
    this.topcombon = topcombon;
  }

  public boolean isAsync()
  {
    return async;
  }

  public void setAsync(boolean async)
  {
    this.async = async;
  }

  public java.lang.String getEmail()
  {
    return email;
  }

  public void setEmail(java.lang.String email)
  {
    this.email = email;
  }

  private java.lang.Object __equalsCalc = null;
  public synchronized boolean equals(java.lang.Object obj)
  {
    if (! (obj instanceof InputParams))
    {
      return false;
    }
    InputParams other = (InputParams) obj;
    if (obj == null)
    {
      return false;
    }
    if (this == obj)
    {
      return true;
    }
    if (__equalsCalc != null)
    {
      return (__equalsCalc == obj);
    }
    __equalsCalc = obj;
    boolean _equals;
    _equals = true &&
        ( (program == null && other.getProgram() == null) ||
         (program != null &&
          program.equals(other.getProgram()))) &&
        ( (database == null && other.getDatabase() == null) ||
         (database != null &&
          database.equals(other.getDatabase()))) &&
        ( (matrix == null && other.getMatrix() == null) ||
         (matrix != null &&
          matrix.equals(other.getMatrix()))) &&
        exp == other.getExp() &&
        echofilter == other.isEchofilter() &&
        ( (filter == null && other.getFilter() == null) ||
         (filter != null &&
          filter.equals(other.getFilter()))) &&
        numal == other.getNumal() &&
        scores == other.getScores() &&
        ( (sensitivity == null && other.getSensitivity() == null) ||
         (sensitivity != null &&
          sensitivity.equals(other.getSensitivity()))) &&
        ( (sort == null && other.getSort() == null) ||
         (sort != null &&
          sort.equals(other.getSort()))) &&
        ( (stats == null && other.getStats() == null) ||
         (stats != null &&
          stats.equals(other.getStats()))) &&
        ( (strand == null && other.getStrand() == null) ||
         (strand != null &&
          strand.equals(other.getStrand()))) &&
        ( (outformat == null && other.getOutformat() == null) ||
         (outformat != null &&
          outformat.equals(other.getOutformat()))) &&
        topcombon == other.getTopcombon() &&
        async == other.isAsync() &&
        ( (email == null && other.getEmail() == null) ||
         (email != null &&
          email.equals(other.getEmail())));
    __equalsCalc = null;
    return _equals;
  }

  private boolean __hashCodeCalc = false;
  public synchronized int hashCode()
  {
    if (__hashCodeCalc)
    {
      return 0;
    }
    __hashCodeCalc = true;
    int _hashCode = 1;
    if (getProgram() != null)
    {
      _hashCode += getProgram().hashCode();
    }
    if (getDatabase() != null)
    {
      _hashCode += getDatabase().hashCode();
    }
    if (getMatrix() != null)
    {
      _hashCode += getMatrix().hashCode();
    }
    _hashCode += new Float(getExp()).hashCode();
    _hashCode += new Boolean(isEchofilter()).hashCode();
    if (getFilter() != null)
    {
      _hashCode += getFilter().hashCode();
    }
    _hashCode += getNumal();
    _hashCode += getScores();
    if (getSensitivity() != null)
    {
      _hashCode += getSensitivity().hashCode();
    }
    if (getSort() != null)
    {
      _hashCode += getSort().hashCode();
    }
    if (getStats() != null)
    {
      _hashCode += getStats().hashCode();
    }
    if (getStrand() != null)
    {
      _hashCode += getStrand().hashCode();
    }
    if (getOutformat() != null)
    {
      _hashCode += getOutformat().hashCode();
    }
    _hashCode += getTopcombon();
    _hashCode += new Boolean(isAsync()).hashCode();
    if (getEmail() != null)
    {
      _hashCode += getEmail().hashCode();
    }
    __hashCodeCalc = false;
    return _hashCode;
  }

  // Type metadata
  private static org.apache.axis.description.TypeDesc typeDesc =
      new org.apache.axis.description.TypeDesc(InputParams.class);

  static
  {
    org.apache.axis.description.FieldDesc field = new org.apache.axis.
        description.ElementDesc();
    field.setFieldName("program");
    field.setXmlName(new javax.xml.namespace.QName("", "program"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("database");
    field.setXmlName(new javax.xml.namespace.QName("", "database"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("matrix");
    field.setXmlName(new javax.xml.namespace.QName("", "matrix"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("exp");
    field.setXmlName(new javax.xml.namespace.QName("", "exp"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "float"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("echofilter");
    field.setXmlName(new javax.xml.namespace.QName("", "echofilter"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "boolean"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("filter");
    field.setXmlName(new javax.xml.namespace.QName("", "filter"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("numal");
    field.setXmlName(new javax.xml.namespace.QName("", "numal"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "int"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("scores");
    field.setXmlName(new javax.xml.namespace.QName("", "scores"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "int"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("sensitivity");
    field.setXmlName(new javax.xml.namespace.QName("", "sensitivity"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("sort");
    field.setXmlName(new javax.xml.namespace.QName("", "sort"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("stats");
    field.setXmlName(new javax.xml.namespace.QName("", "stats"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("strand");
    field.setXmlName(new javax.xml.namespace.QName("", "strand"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("outformat");
    field.setXmlName(new javax.xml.namespace.QName("", "outformat"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("topcombon");
    field.setXmlName(new javax.xml.namespace.QName("", "topcombon"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "int"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("async");
    field.setXmlName(new javax.xml.namespace.QName("", "async"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "boolean"));
    typeDesc.addFieldDesc(field);
    field = new org.apache.axis.description.ElementDesc();
    field.setFieldName("email");
    field.setXmlName(new javax.xml.namespace.QName("", "email"));
    field.setXmlType(new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema", "string"));
    typeDesc.addFieldDesc(field);
  };

  /**
   * Return type metadata object
   */
  public static org.apache.axis.description.TypeDesc getTypeDesc()
  {
    return typeDesc;
  }

  /**
   * Get Custom Serializer
   */
  public static org.apache.axis.encoding.Serializer getSerializer(
      java.lang.String mechType,
      java.lang.Class _javaType,
      javax.xml.namespace.QName _xmlType)
  {
    return
        new org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
  }

  /**
   * Get Custom Deserializer
   */
  public static org.apache.axis.encoding.Deserializer getDeserializer(
      java.lang.String mechType,
      java.lang.Class _javaType,
      javax.xml.namespace.QName _xmlType)
  {
    return
        new org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
  }

}
