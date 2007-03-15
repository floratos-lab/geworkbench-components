package org.geworkbench.components.cagrid;

import com.ibm.wsdl.BindingOperationImpl;
import com.ibm.wsdl.PortImpl;
import com.ibm.wsdl.ServiceImpl;
import com.ibm.wsdl.xml.WSDLReaderImpl;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Dim;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Distance;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Method;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteResponse;
import edu.columbia.geworkbench.cagrid.cluster.client.HierarchicalClusteringClient;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.utils.ClassUtils;
import org.ginkgo.labs.reader.TabFileReader;

import javax.wsdl.*;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author mhall
 */
public class DynamicTest {
    public static void main(String [] args) {
        try {

            MicroarraySet arraySet = HierarchicalClusteringClient.float2DToMicroarraySet(TabFileReader.readTabFile("/Users/mhall/code/geworkbench/components/cagrid/src/edu/columbia/geworkbench/cagrid/cluster/client/aTestDataSet_without_headers_30.txt"));

            HierarchicalClusteringParameter parameters = new HierarchicalClusteringParameter();
            parameters.setDim(Dim.both);
            parameters.setDistance(Distance.euclidean);
            parameters.setMethod(Method.complete);

            String endpoint = "http://cagridnode.c2b2.columbia.edu:8080/wsrf/services/cagrid/HierarchicalClustering";
            WSDLReader wsdlReader = new WSDLReaderImpl();
            Definition def = wsdlReader.readWSDL(null, endpoint + "?wsdl");
//                    "http://nagoya.apache.org:5049/axis/services/echo";

            Set<Map.Entry> set = def.getServices().entrySet();
            for (Map.Entry entry : set) {
                ServiceImpl service = (ServiceImpl) entry.getValue();
                System.out.println("Found service: " + service.getQName());
                Set<Map.Entry> portEntries = service.getPorts().entrySet();
                for (Map.Entry portEntry : portEntries) {
                    PortImpl port = (PortImpl) portEntry.getValue();
                    System.out.println("--Found port: " + port.getName());
                    Binding binding = port.getBinding();
                    List<BindingOperationImpl> bindingOperations = binding.getBindingOperations();
                    for (BindingOperationImpl bindingOperation : bindingOperations) {
                        Operation operation = bindingOperation.getOperation();
                        System.out.println("----Found operation: " + operation.getName());
                        Message inMessage = operation.getInput().getMessage();
                        Message outMessage = operation.getOutput().getMessage();
                        Part inPart = inMessage.getPart("parameters");
                        Part outPart = outMessage.getPart("parameters");
                        if (inPart != null && outPart != null) {
                            System.out.println("------Input: " + inPart.getElementName());
                            System.out.println("------Output: " + outPart.getElementName());
                        }
                    }
                }
            }
            Service service = new Service();

            InputStream resourceAsStream = ClassUtils.getResourceAsStream(DynamicTest.class, "conf/client-config.wsdd");
            if (resourceAsStream != null) {
                // we found it, so tell axis to configure an engine to use it
                EngineConfiguration engineConfig = new FileProvider(resourceAsStream);
                // set the engine of the locator
//                locator.setEngine(new AxisClient(engineConfig));
                service.setEngine(new AxisClient(engineConfig));
            }

            org.apache.axis.description.OperationDesc oper;
            oper = new org.apache.axis.description.OperationDesc();
            oper.setName("execute");
            oper.addParameter(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClustering", "ExecuteRequest"),
                    new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClustering", ">ExecuteRequest"),
                    edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequest.class, org.apache.axis.description.ParameterDesc.IN, false, false);
            QName returnQName = new QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClustering", ">ExecuteResponse");
            oper.setReturnType(returnQName);
            oper.setReturnClass(edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteResponse.class);
//            oper.setReturnClass(JROMComplexValue.class);
            oper.setReturnQName(new javax.xml.namespace.QName("http://cagrid.geworkbench.columbia.edu/HierarchicalClustering", "ExecuteResponse"));
            oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
            oper.setUse(org.apache.axis.constants.Use.LITERAL);

            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

//            org.apache.axis.client.Call _call = createCall();
            call.setOperation(oper);
            call.setUseSOAPAction(true);
            call.setSOAPActionURI("http://cagrid.geworkbench.columbia.edu/HierarchicalClustering/ExecuteRequest");
            call.setEncodingStyle(null);
            call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
            call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
            call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
            call.setOperationName(new javax.xml.namespace.QName("", "execute"));

/*
            call.registerTypeMapping(JROMStringValue.class, Constants.XSD_STRING,
                    new JROMSerializerFactory(),
                    new JROMDeserializerFactory());
            call.registerTypeMapping(JROMIntegerValue.class, Constants.XSD_INT,
                    new JROMSerializerFactory(),
                    new JROMDeserializerFactory());
            call.registerTypeMapping(JROMComplexValue.class, returnQName,
                    new JROMSerializerFactory(),
                    new JROMDeserializerFactory());
*/

            edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequest params = new edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequest();
            edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestMicroarraySet microarraySetContainer = new edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestMicroarraySet();
            microarraySetContainer.setMicroarraySet(arraySet);
            params.setMicroarraySet(microarraySetContainer);
            edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestHierarchicalClusteringParameter hierarchicalClusteringParameterContainer = new edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestHierarchicalClusteringParameter();
            hierarchicalClusteringParameterContainer.setHierarchicalClusteringParameter(parameters);
            params.setHierarchicalClusteringParameter(hierarchicalClusteringParameterContainer);

//            setRequestHeaders(_call);
//            setAttachments(_call);
            java.lang.Object _resp = call.invoke(new java.lang.Object[]{params});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
//                extractAttachments(_call);
                ExecuteResponse resp;
                try {
                    resp = (ExecuteResponse) _resp;
                } catch (java.lang.Exception _exception) {
                    System.err.println("Problem casting results, trying to convert.");
                    resp = (edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteResponse) org.apache.axis.utils.JavaUtils.convert(_resp, edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteResponse.class);
                }
                System.out.println("Received hier cluster: " + resp.getHierarchicalCluster().getName());
            }

//            call.setOperationName(new QName("http://soapinterop.org/", "echoString"));

            // Call to addParameter/setReturnType as described in user-guide.html
//            call.addParameter("testParam",
//                              org.apache.axis.Constants.XSD_STRING,
//                              javax.xml.rpc.ParameterMode.IN);
//            call.setReturnType(org.apache.axis.Constants.XSD_STRING);

//            String ret = (String) call.invoke(new Object[]{"Hello!"});

//            System.out.println("Sent 'Hello!', got '" + ret + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
