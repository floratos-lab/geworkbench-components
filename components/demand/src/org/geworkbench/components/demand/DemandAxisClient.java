package org.geworkbench.components.demand;

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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class DemandAxisClient {
	private static final String demandNamespace = "http://www.geworkbench.org/service/demand";
	private static final String indexNamespace  = "http://www.geworkbench.org/service/index";
	private static final String indexAddress    = System.getProperty("internal.indexServer.url");
	
	private static final String resFile	=	"DMAND_result.txt";	//result file
 	private static final String resEdge	=	"KL_edge.txt";		//result edge file
 	private static final String resMod	=	"Module.txt";		//result module file

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
 	
	public String executeDemand(String serviceAddress, String setName, String expFname, String nwFname, String annoFname, String spFname, String resultDir) throws AxisFault {
		OMElement demandRequest = createAxiomRequestElement(setName, expFname, nwFname, annoFname, spFname);
		return doWebServiceCallWithAxis(serviceAddress, demandRequest, resultDir);
	}
	
	private OMElement createAxiomRequestElement(String setName, String expFname, String nwFname, String annoFname, String spFname) {

		OMFactory omFactory = OMAbstractFactory.getSOAP11Factory();
		OMNamespace namespace = omFactory.createOMNamespace(demandNamespace, null);
		OMElement executeDemandRequest = omFactory.createOMElement("ExecuteDemandRequest", namespace);
		
		OMElement nameElement = omFactory.createOMElement("name", namespace);
		nameElement.setText(setName);
		executeDemandRequest.addChild(nameElement);

		addFileElement("expfile",  expFname,  omFactory, namespace, executeDemandRequest);
		addFileElement("nwfile",    nwFname,  omFactory, namespace, executeDemandRequest);
		addFileElement("annofile",annoFname,  omFactory, namespace, executeDemandRequest);
		addFileElement("spfile",    spFname,  omFactory, namespace, executeDemandRequest);

		return executeDemandRequest;
	}
	
	private void addFileElement(String title, String filepath, 
			OMFactory omFactory, OMNamespace namespace, OMElement executeDemandRequest){
		DataSource messageDataSource = null;
		messageDataSource = new FileDataSource(filepath);
		DataHandler dataHandler = new DataHandler(messageDataSource);

		OMElement expfileElement = omFactory.createOMElement(title, namespace);
		OMText textData = omFactory.createOMText(dataHandler, true);
		expfileElement.addChild(textData);
		executeDemandRequest.addChild(expfileElement);
	}

	private String doWebServiceCallWithAxis(String serviceAddress, OMElement demandRequest, String resultDir) throws AxisFault {
		org.apache.axis2.client.Options serviceOptions = new org.apache.axis2.client.Options();
    	serviceOptions.setProperty( Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE );
    	serviceOptions.setProperty( Constants.Configuration.ATTACHMENT_TEMP_DIR, System.getProperty("java.io.tmpdir") );
    	serviceOptions.setProperty( Constants.Configuration.CACHE_ATTACHMENTS, Constants.VALUE_TRUE );
    	serviceOptions.setProperty( Constants.Configuration.FILE_SIZE_THRESHOLD, "1024" );
		// 50-hour timeout
		serviceOptions.setTimeOutInMilliSeconds(180000000);

		ServiceClient serviceClient = new ServiceClient();
		serviceClient.setOptions(serviceOptions);
		setContextProperties(serviceClient);
		EndpointReference ref = new EndpointReference();
		ref.setAddress(serviceAddress);
		serviceClient.setTargetEPR(ref);

		OMElement response = serviceClient.sendReceive(demandRequest);

		OMElement logElement = (OMElement)response.getFirstChildWithName(new QName(demandNamespace, "log"));
		String errlog = logElement.getText();
		if (errlog!=null && errlog.length()>0) return errlog;
		
		String resFname 	=	resultDir + resFile;
		String resEdgeFname	=	resultDir + resEdge;
		String resModFname	=	resultDir + resMod;
 	
    	writeElementToFile(response, "resfile", resFname);
    	writeElementToFile(response, "edgefile", resEdgeFname);
    	writeElementToFile(response, "modfile", resModFname);

		return null;
	}
	
	private void writeElementToFile(OMElement response, String title, String filepath){
		OMElement fileElement = (OMElement)response.getFirstChildWithName(new QName(demandNamespace, title));
		DataHandler handler = (DataHandler)((OMText)fileElement.getFirstOMChild()).getDataHandler();
		
		File file = new File(filepath);
		OutputStream os = null;
		try{
		   	os = new FileOutputStream(file);
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
	}
	
	private void setContextProperties(ServiceClient serviceClient) {
		ConfigurationContext context = serviceClient.getServiceContext().getConfigurationContext();
		context.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);
		context.setProperty(HTTPConstants.AUTO_RELEASE_CONNECTION, true);
		MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setDefaultMaxConnectionsPerHost(20); 
		multiThreadedHttpConnectionManager.setParams(params);
		HttpClient httpClient = new HttpClient(multiThreadedHttpConnectionManager);
		context.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
	}
}
