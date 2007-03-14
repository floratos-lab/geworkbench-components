package edu.columbia.geworkbench.cagrid.discovery.client;

import org.apache.axis.message.addressing.AttributedURI;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.nih.nci.cagrid.discovery.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;
import gov.nih.nci.cagrid.metadata.service.InputParameter;
import gov.nih.nci.cagrid.metadata.service.Operation;
import gov.nih.nci.cagrid.metadata.service.OperationInputParameterCollection;
import gov.nih.nci.cagrid.metadata.service.ServiceContext;
import gov.nih.nci.cagrid.metadata.service.ServiceContextOperationCollection;
import gov.nih.nci.cagrid.metadata.service.ServiceServiceContextCollection;

/**
 * 
 * @author keshav
 * @version $Id: DiscoveryServiceUtil.java,v 1.1 2007-03-14 20:29:02 keshav Exp $
 */
public class DiscoveryServiceUtil {
	private static Log log = LogFactory.getLog(DiscoveryServiceUtil.class);

	/**
	 * Retrieves all services at indexServiceUrl
	 * 
	 * @param indexServiceUrl
	 */
	public static EndpointReferenceType[] getServices(String indexServiceHost,
			int indexServicePort, String search) {

		EndpointReferenceType[] allServices = null;
		try {
			AnalyticalServiceDiscoveryClient client = new AnalyticalServiceDiscoveryClient(
					indexServiceHost, indexServicePort);

			if (StringUtils.isEmpty(search)) {
				allServices = client.getAllServices();
			} else {
				allServices = client.discoverServicesBySearchString(search);
			}
			if (log.isInfoEnabled())
				displayMetadata(allServices);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return allServices;
	}

	/**
	 * 
	 * @param allServices
	 * @throws Exception
	 */
	public static void displayMetadata(EndpointReferenceType[] allServices)
			throws Exception {
		if (allServices != null) {
			for (EndpointReferenceType service : allServices) {

				ServiceMetadata commonMetadata = MetadataUtils
						.getServiceMetadata(service);

				getUrlFromMetadata(service);

				getDescriptionFromMetadata(commonMetadata);

				ServiceServiceContextCollection serContextCol = commonMetadata
						.getServiceDescription().getService()
						.getServiceContextCollection();

				ServiceContext[] contexts = serContextCol.getServiceContext();
				if (contexts == null)
					continue;

				for (ServiceContext context : contexts) {
					log.info("context: " + context.getName());
					ServiceContextOperationCollection serOperatonCol = context
							.getOperationCollection();

					Operation[] operations = serOperatonCol.getOperation();
					if (operations == null)
						continue;

					for (Operation operation : operations) {
						log.info("operation: " + operation.getName());
						OperationInputParameterCollection inputParamCol = operation
								.getInputParameterCollection();

						InputParameter[] inputParams = inputParamCol
								.getInputParameter();
						if (inputParams == null)
							continue;

						for (InputParameter param : inputParams) {
							log.info("input param: " + param.getName());
							log.info("qname: " + param.getQName());
						}

					}

				}
			}
		}
	}

	/**
	 * 
	 * @param service
	 * @return
	 */
	public static String getUrlFromMetadata(EndpointReferenceType service) {

		AttributedURI uri = service.getAddress();

		String url = uri.toString();
		log.info("Service: " + url);

		return url;
	}

	/**
	 * 
	 * @param service
	 * @return
	 */
	public static String getDescriptionFromMetadata(
			ServiceMetadata commonMetadata) {

		String description = commonMetadata.getServiceDescription()
				.getService().getDescription();
		log.info("  Description: " + description);

		return description;
	}
}
