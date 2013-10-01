package org.geworkbench.components.viper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;

public class ViperAxisClient {
	private static final String viperNamespace = "http://www.geworkbench.org/service/viper";
	private static final String indexNamespace = "http://www.geworkbench.org/service/index";
	private static final String indexAddress   = System.getProperty("internal.indexServer.url");
	
	/**
	 * Returns service url string associated with the query
	 * Returns null if no service found
	 * @param query
	 * @return service url
	 */
	public String findService(String query) throws AxisFault{
		OMFactory omFactory = OMAbstractFactory.getSOAP11Factory();
		OMNamespace namespace = omFactory.createOMNamespace(indexNamespace, "");
		OMElement indexRequest = omFactory.createOMElement("indexRequest", namespace);
		indexRequest.setText(query);
		
		ServiceClient serviceClient = new ServiceClient();
		EndpointReference ref = new EndpointReference(indexAddress);
		serviceClient.setTargetEPR(ref);

		OMElement response = serviceClient.sendReceive(indexRequest);
		String serviceAddress = response.getText();
		if (serviceAddress.length()==0) return null;
		return serviceAddress;
	}

	public String executeViper(String serviceAddress, String filepath, String outfname, String regulon, String regtype, String method, String rlibpath) throws AxisFault {
		OMElement viperRequest = createAxiomRequestElement(filepath, regulon, regtype, method, rlibpath);
		return doWebServiceCallWithAxis(serviceAddress, viperRequest, outfname);
	}

	private OMElement createAxiomRequestElement(String filepath, String regulon, String regtype, String method, String rlibpath) {

		OMFactory omFactory = OMAbstractFactory.getSOAP11Factory();
		OMNamespace namespace = omFactory.createOMNamespace(viperNamespace, null);
		OMElement executeViperRequest = omFactory.createOMElement("ExecuteViperRequest", namespace);
		
		DataSource messageDataSource = null;
		messageDataSource = new FileDataSource(filepath);
		DataHandler dataHandler = new DataHandler(messageDataSource);

		OMElement expfileElement = omFactory.createOMElement("expfile", namespace);
		OMText textData = omFactory.createOMText(dataHandler, true);
		expfileElement.addChild(textData);
		executeViperRequest.addChild(expfileElement);
		
		OMElement nameElement = omFactory.createOMElement("name", namespace);
		nameElement.setText(messageDataSource.getName());
		executeViperRequest.addChild(nameElement);
		
		OMElement regulonElement = omFactory.createOMElement("regulon", namespace);
		regulonElement.setText(regulon);
		executeViperRequest.addChild(regulonElement);
		
		OMElement regtypeElement = omFactory.createOMElement("regtype", namespace);
		regtypeElement.setText(regtype);
		executeViperRequest.addChild(regtypeElement);

		OMElement methodElement = omFactory.createOMElement("method", namespace);
		methodElement.setText(method);
		executeViperRequest.addChild(methodElement);

		OMElement rlibpathElement = omFactory.createOMElement("rlibpath", namespace);
		rlibpathElement.setText(rlibpath);
		executeViperRequest.addChild(rlibpathElement);
		
		return executeViperRequest;
	}

	private String doWebServiceCallWithAxis(String serviceAddress, OMElement viperRequest, String outfname) throws AxisFault {
		org.apache.axis2.client.Options serviceOptions = new org.apache.axis2.client.Options();
    	serviceOptions.setProperty( Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE );
    	serviceOptions.setProperty( Constants.Configuration.ATTACHMENT_TEMP_DIR, System.getProperty("java.io.tmpdir") );
    	serviceOptions.setProperty( Constants.Configuration.CACHE_ATTACHMENTS, Constants.VALUE_TRUE );
    	serviceOptions.setProperty( Constants.Configuration.FILE_SIZE_THRESHOLD, "1024" );
		// 50-hour timeout
		serviceOptions.setTimeOutInMilliSeconds(180000000);

		ServiceClient serviceClient = new ServiceClient();
		serviceClient.setOptions(serviceOptions);
		EndpointReference ref = new EndpointReference();
		ref.setAddress(serviceAddress);
		serviceClient.setTargetEPR(ref);

		OMElement response = serviceClient.sendReceive(viperRequest);

		OMElement logElement = (OMElement)response.getFirstChildWithName(new QName(viperNamespace, "log"));
		String errlog = logElement.getText();
		if (errlog!=null && errlog.length()>0) return errlog;
		
		OMElement fileElement = (OMElement)response.getFirstChildWithName(new QName(viperNamespace, "outfile"));
		DataHandler handler = (DataHandler)((OMText)fileElement.getFirstOMChild()).getDataHandler();

		File outfile = new File(outfname);
		OutputStream os = null;
		try{
		   	os = new FileOutputStream(outfile);
		  	handler.writeTo(os);
		}catch(IOException e){
		   	e.printStackTrace();
		}finally{
		   	try{
		   		if (os != null) os.close();
		   	}catch(IOException e){
		  		e.printStackTrace();
		  	}
		}

		return null;
	}

}
