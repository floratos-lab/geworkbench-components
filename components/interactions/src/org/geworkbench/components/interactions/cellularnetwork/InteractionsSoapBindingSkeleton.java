/**
 * InteractionsSoapBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.geworkbench.components.interactions.cellularnetwork;

public class InteractionsSoapBindingSkeleton implements org.geworkbench.components.interactions.cellularnetwork.INTERACTIONS, org.apache.axis.wsdl.Skeleton {
    private org.geworkbench.components.interactions.cellularnetwork.INTERACTIONS impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
     * Returns List of OperationDesc objects with this name
     */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List) _myOperations.get(methodName);
    }

    /**
     * Returns Collection of OperationDescs
     */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getINTERACTIONTYPE", _params, new javax.xml.namespace.QName("", "getINTERACTIONTYPEReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getINTERACTIONTYPE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getINTERACTIONTYPE") == null) {
            _myOperations.put("getINTERACTIONTYPE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getINTERACTIONTYPE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setINTERACTIONTYPE", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setINTERACTIONTYPE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setINTERACTIONTYPE") == null) {
            _myOperations.put("setINTERACTIONTYPE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setINTERACTIONTYPE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getMSID2", _params, new javax.xml.namespace.QName("", "getMSID2Return"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getMSID2"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getMSID2") == null) {
            _myOperations.put("getMSID2", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getMSID2")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"), java.math.BigDecimal.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setMSID2", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setMSID2"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setMSID2") == null) {
            _myOperations.put("setMSID2", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setMSID2")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getISREVERSIBLE", _params, new javax.xml.namespace.QName("", "getISREVERSIBLEReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getISREVERSIBLE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getISREVERSIBLE") == null) {
            _myOperations.put("getISREVERSIBLE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getISREVERSIBLE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setISREVERSIBLE", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setISREVERSIBLE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setISREVERSIBLE") == null) {
            _myOperations.put("setISREVERSIBLE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setISREVERSIBLE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getMSID1", _params, new javax.xml.namespace.QName("", "getMSID1Return"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getMSID1"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getMSID1") == null) {
            _myOperations.put("getMSID1", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getMSID1")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"), java.math.BigDecimal.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setMSID1", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setMSID1"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setMSID1") == null) {
            _myOperations.put("setMSID1", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setMSID1")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getSOURCE", _params, new javax.xml.namespace.QName("", "getSOURCEReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getSOURCE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getSOURCE") == null) {
            _myOperations.put("getSOURCE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getSOURCE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setSOURCE", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setSOURCE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setSOURCE") == null) {
            _myOperations.put("setSOURCE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setSOURCE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getCONTROLTYPE", _params, new javax.xml.namespace.QName("", "getCONTROLTYPEReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getCONTROLTYPE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getCONTROLTYPE") == null) {
            _myOperations.put("getCONTROLTYPE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getCONTROLTYPE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setCONTROLTYPE", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setCONTROLTYPE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setCONTROLTYPE") == null) {
            _myOperations.put("setCONTROLTYPE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setCONTROLTYPE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getDIRECTION", _params, new javax.xml.namespace.QName("", "getDIRECTIONReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getDIRECTION"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDIRECTION") == null) {
            _myOperations.put("getDIRECTION", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getDIRECTION")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setDIRECTION", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setDIRECTION"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setDIRECTION") == null) {
            _myOperations.put("setDIRECTION", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setDIRECTION")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getCONFIDENCEVALUE", _params, new javax.xml.namespace.QName("", "getCONFIDENCEVALUEReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getCONFIDENCEVALUE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getCONFIDENCEVALUE") == null) {
            _myOperations.put("getCONFIDENCEVALUE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getCONFIDENCEVALUE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"), double.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setCONFIDENCEVALUE", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setCONFIDENCEVALUE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setCONFIDENCEVALUE") == null) {
            _myOperations.put("setCONFIDENCEVALUE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setCONFIDENCEVALUE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getISMODULATED", _params, new javax.xml.namespace.QName("", "getISMODULATEDReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getISMODULATED"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getISMODULATED") == null) {
            _myOperations.put("getISMODULATED", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getISMODULATED")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setISMODULATED", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setISMODULATED"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setISMODULATED") == null) {
            _myOperations.put("setISMODULATED", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setISMODULATED")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getID", _params, new javax.xml.namespace.QName("", "getIDReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getID"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getID") == null) {
            _myOperations.put("getID", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getID")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("insert", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "insert"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("insert") == null) {
            _myOperations.put("insert", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("insert")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("retrieve", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "retrieve"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("retrieve") == null) {
            _myOperations.put("retrieve", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("retrieve")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"), java.math.BigDecimal.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setID", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setID"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setID") == null) {
            _myOperations.put("setID", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setID")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getCHROMOSOME", _params, new javax.xml.namespace.QName("", "getCHROMOSOMEReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getCHROMOSOME"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getCHROMOSOME") == null) {
            _myOperations.put("getCHROMOSOME", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getCHROMOSOME")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setCHROMOSOME", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setCHROMOSOME"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setCHROMOSOME") == null) {
            _myOperations.put("setCHROMOSOME", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setCHROMOSOME")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getGENECOUNT", _params, new javax.xml.namespace.QName("", "getGENECOUNTReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getGENECOUNT"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getGENECOUNT") == null) {
            _myOperations.put("getGENECOUNT", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getGENECOUNT")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"), java.math.BigDecimal.class, false, false),
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("getFIRSTNEIGHBORS", _params, new javax.xml.namespace.QName("", "getFIRSTNEIGHBORSReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "ArrayOf_xsd_anyType"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getFIRSTNEIGHBORS"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getFIRSTNEIGHBORS") == null) {
            _myOperations.put("getFIRSTNEIGHBORS", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getFIRSTNEIGHBORS")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"), java.math.BigDecimal.class, false, false),
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("getINTERACTIONCOUNT", _params, new javax.xml.namespace.QName("", "getINTERACTIONCOUNTReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getINTERACTIONCOUNT"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getINTERACTIONCOUNT") == null) {
            _myOperations.put("getINTERACTIONCOUNT", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getINTERACTIONCOUNT")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getENTREZID", _params, new javax.xml.namespace.QName("", "getENTREZIDReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getENTREZID"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getENTREZID") == null) {
            _myOperations.put("getENTREZID", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getENTREZID")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"), java.math.BigDecimal.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setENTREZID", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setENTREZID"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setENTREZID") == null) {
            _myOperations.put("setENTREZID", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setENTREZID")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getTAXONID", _params, new javax.xml.namespace.QName("", "getTAXONIDReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getTAXONID"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getTAXONID") == null) {
            _myOperations.put("getTAXONID", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getTAXONID")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"), java.math.BigDecimal.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setTAXONID", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setTAXONID"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setTAXONID") == null) {
            _myOperations.put("setTAXONID", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setTAXONID")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getGENETYPE", _params, new javax.xml.namespace.QName("", "getGENETYPEReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getGENETYPE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getGENETYPE") == null) {
            _myOperations.put("getGENETYPE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getGENETYPE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setGENETYPE", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setGENETYPE"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setGENETYPE") == null) {
            _myOperations.put("setGENETYPE", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setGENETYPE")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getGENESYMBOL", _params, new javax.xml.namespace.QName("", "getGENESYMBOLReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getGENESYMBOL"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getGENESYMBOL") == null) {
            _myOperations.put("getGENESYMBOL", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getGENESYMBOL")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setGENESYMBOL", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setGENESYMBOL"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setGENESYMBOL") == null) {
            _myOperations.put("setGENESYMBOL", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setGENESYMBOL")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"), java.math.BigDecimal.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("getGENEROW", _params, new javax.xml.namespace.QName("", "getGENEROWReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "ArrayOf_xsd_anyType"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getGENEROW"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getGENEROW") == null) {
            _myOperations.put("getGENEROW", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getGENEROW")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getENTREZTOGO", _params, new javax.xml.namespace.QName("", "getENTREZTOGOReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "ArrayOf_xsd_anyType"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getENTREZTOGO"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getENTREZTOGO") == null) {
            _myOperations.put("getENTREZTOGO", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getENTREZTOGO")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getLOCUSTAG", _params, new javax.xml.namespace.QName("", "getLOCUSTAGReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getLOCUSTAG"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getLOCUSTAG") == null) {
            _myOperations.put("getLOCUSTAG", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getLOCUSTAG")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setLOCUSTAG", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setLOCUSTAG"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setLOCUSTAG") == null) {
            _myOperations.put("setLOCUSTAG", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setLOCUSTAG")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
        };
        _oper = new org.apache.axis.description.OperationDesc("getDESCRIPTION", _params, new javax.xml.namespace.QName("", "getDESCRIPTIONReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "getDESCRIPTION"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getDESCRIPTION") == null) {
            _myOperations.put("getDESCRIPTION", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("getDESCRIPTION")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc []{
                new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false),
        };
        _oper = new org.apache.axis.description.OperationDesc("setDESCRIPTION", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("urn:org.geworkbench.components.interactions.cellularnetwork", "setDESCRIPTION"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("setDESCRIPTION") == null) {
            _myOperations.put("setDESCRIPTION", new java.util.ArrayList());
        }
        ((java.util.List) _myOperations.get("setDESCRIPTION")).add(_oper);
    }

    public InteractionsSoapBindingSkeleton() {
        this.impl = new org.geworkbench.components.interactions.cellularnetwork.InteractionsSoapBindingImpl();
    }

    public InteractionsSoapBindingSkeleton(org.geworkbench.components.interactions.cellularnetwork.INTERACTIONS impl) {
        this.impl = impl;
    }

    public java.lang.String getINTERACTIONTYPE() throws java.rmi.RemoteException {
        java.lang.String ret = impl.getINTERACTIONTYPE();
        return ret;
    }

    public void setINTERACTIONTYPE(java.lang.String in0) throws java.rmi.RemoteException {
        impl.setINTERACTIONTYPE(in0);
    }

    public java.math.BigDecimal getMSID2() throws java.rmi.RemoteException {
        java.math.BigDecimal ret = impl.getMSID2();
        return ret;
    }

    public void setMSID2(java.math.BigDecimal in0) throws java.rmi.RemoteException {
        impl.setMSID2(in0);
    }

    public java.lang.String getISREVERSIBLE() throws java.rmi.RemoteException {
        java.lang.String ret = impl.getISREVERSIBLE();
        return ret;
    }

    public void setISREVERSIBLE(java.lang.String in0) throws java.rmi.RemoteException {
        impl.setISREVERSIBLE(in0);
    }

    public java.math.BigDecimal getMSID1() throws java.rmi.RemoteException {
        java.math.BigDecimal ret = impl.getMSID1();
        return ret;
    }

    public void setMSID1(java.math.BigDecimal in0) throws java.rmi.RemoteException {
        impl.setMSID1(in0);
    }

    public java.lang.String getSOURCE() throws java.rmi.RemoteException {
        java.lang.String ret = impl.getSOURCE();
        return ret;
    }

    public void setSOURCE(java.lang.String in0) throws java.rmi.RemoteException {
        impl.setSOURCE(in0);
    }

    public java.lang.String getCONTROLTYPE() throws java.rmi.RemoteException {
        java.lang.String ret = impl.getCONTROLTYPE();
        return ret;
    }

    public void setCONTROLTYPE(java.lang.String in0) throws java.rmi.RemoteException {
        impl.setCONTROLTYPE(in0);
    }

    public java.lang.String getDIRECTION() throws java.rmi.RemoteException {
        java.lang.String ret = impl.getDIRECTION();
        return ret;
    }

    public void setDIRECTION(java.lang.String in0) throws java.rmi.RemoteException {
        impl.setDIRECTION(in0);
    }

    public double getCONFIDENCEVALUE() throws java.rmi.RemoteException {
        double ret = impl.getCONFIDENCEVALUE();
        return ret;
    }

    public void setCONFIDENCEVALUE(double in0) throws java.rmi.RemoteException {
        impl.setCONFIDENCEVALUE(in0);
    }

    public java.lang.String getISMODULATED() throws java.rmi.RemoteException {
        java.lang.String ret = impl.getISMODULATED();
        return ret;
    }

    public void setISMODULATED(java.lang.String in0) throws java.rmi.RemoteException {
        impl.setISMODULATED(in0);
    }

    public java.math.BigDecimal getID() throws java.rmi.RemoteException {
        java.math.BigDecimal ret = impl.getID();
        return ret;
    }

    public void insert() throws java.rmi.RemoteException {
        impl.insert();
    }

    public void retrieve() throws java.rmi.RemoteException {
        impl.retrieve();
    }

    public void setID(java.math.BigDecimal in0) throws java.rmi.RemoteException {
        impl.setID(in0);
    }

    public java.lang.String getCHROMOSOME() throws java.rmi.RemoteException {
        java.lang.String ret = impl.getCHROMOSOME();
        return ret;
    }

    public void setCHROMOSOME(java.lang.String in0) throws java.rmi.RemoteException {
        impl.setCHROMOSOME(in0);
    }

    public java.math.BigDecimal getGENECOUNT() throws java.rmi.RemoteException {
        java.math.BigDecimal ret = impl.getGENECOUNT();
        return ret;
    }

    public java.lang.Object[] getFIRSTNEIGHBORS(java.math.BigDecimal in0, java.lang.String in1) throws java.rmi.RemoteException {
        java.lang.Object[] ret = impl.getFIRSTNEIGHBORS(in0, in1);
        return ret;
    }

    public java.math.BigDecimal getINTERACTIONCOUNT(java.math.BigDecimal in0, java.lang.String in1) throws java.rmi.RemoteException {
        java.math.BigDecimal ret = impl.getINTERACTIONCOUNT(in0, in1);
        return ret;
    }

    public java.math.BigDecimal getENTREZID() throws java.rmi.RemoteException {
        java.math.BigDecimal ret = impl.getENTREZID();
        return ret;
    }

    public void setENTREZID(java.math.BigDecimal in0) throws java.rmi.RemoteException {
        impl.setENTREZID(in0);
    }

    public java.math.BigDecimal getTAXONID() throws java.rmi.RemoteException {
        java.math.BigDecimal ret = impl.getTAXONID();
        return ret;
    }

    public void setTAXONID(java.math.BigDecimal in0) throws java.rmi.RemoteException {
        impl.setTAXONID(in0);
    }

    public java.lang.String getGENETYPE() throws java.rmi.RemoteException {
        java.lang.String ret = impl.getGENETYPE();
        return ret;
    }

    public void setGENETYPE(java.lang.String in0) throws java.rmi.RemoteException {
        impl.setGENETYPE(in0);
    }

    public java.lang.String getGENESYMBOL() throws java.rmi.RemoteException {
        java.lang.String ret = impl.getGENESYMBOL();
        return ret;
    }

    public void setGENESYMBOL(java.lang.String in0) throws java.rmi.RemoteException {
        impl.setGENESYMBOL(in0);
    }

    public java.lang.Object[] getGENEROW(java.math.BigDecimal in0) throws java.rmi.RemoteException {
        java.lang.Object[] ret = impl.getGENEROW(in0);
        return ret;
    }

    public java.lang.Object[] getENTREZTOGO() throws java.rmi.RemoteException {
        java.lang.Object[] ret = impl.getENTREZTOGO();
        return ret;
    }

    public java.lang.String getLOCUSTAG() throws java.rmi.RemoteException {
        java.lang.String ret = impl.getLOCUSTAG();
        return ret;
    }

    public void setLOCUSTAG(java.lang.String in0) throws java.rmi.RemoteException {
        impl.setLOCUSTAG(in0);
    }

    public java.lang.String getDESCRIPTION() throws java.rmi.RemoteException {
        java.lang.String ret = impl.getDESCRIPTION();
        return ret;
    }

    public void setDESCRIPTION(java.lang.String in0) throws java.rmi.RemoteException {
        impl.setDESCRIPTION(in0);
    }

}
