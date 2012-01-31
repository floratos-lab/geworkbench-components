package edu.columbia.geworkbench.cagrid.discovery.client;

import gov.nih.nci.cagrid.discovery.client.DiscoveryClient;
import gov.nih.nci.cagrid.metadata.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;
import gov.nih.nci.cagrid.metadata.exceptions.QueryInvalidException;
import gov.nih.nci.cagrid.metadata.exceptions.RemoteResourcePropertyRetrievalException;
import gov.nih.nci.cagrid.metadata.exceptions.ResourcePropertyRetrievalException;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author keshav
 * @version $Id: AnalyticalServiceDiscoveryClient.java,v 1.1.4.1 2007/10/29
 *          19:33:17 keshav Exp $
 */
public class AnalyticalServiceDiscoveryClient extends DiscoveryClient {
	private static Log log = LogFactory
			.getLog(AnalyticalServiceDiscoveryClient.class);
	protected static final String LOCALHOST_INDEX_SERVICE = "http://localhost:8080/wsrf/services/DefaultIndexService";

	private String url;

	/**
	 * @throws MalformedURIException
	 */
	public AnalyticalServiceDiscoveryClient() throws MalformedURIException {
		super(LOCALHOST_INDEX_SERVICE);
		this.url = LOCALHOST_INDEX_SERVICE;
	}

	/**
	 * @param indexURL
	 * @throws MalformedURIException
	 */
	public AnalyticalServiceDiscoveryClient(String indexURL)
			throws MalformedURIException {
		super(indexURL);
		this.url = indexURL;
	}

	/**
	 * Get all the services from the Index service running on the localhost.
	 * 
	 * @return EndpointReferenceType[]
	 * @throws ResourcePropertyRetrievalException 
	 * @throws QueryInvalidException 
	 * @throws RemoteResourcePropertyRetrievalException 
	 */
	public EndpointReferenceType[] getAllServices()
			throws MalformedURIException,
			RemoteResourcePropertyRetrievalException, QueryInvalidException,
			ResourcePropertyRetrievalException {
		AnalyticalServiceDiscoveryClient client = null;

		if (url != null) {
			client = new AnalyticalServiceDiscoveryClient(url);
		} else {
			client = new AnalyticalServiceDiscoveryClient();
		}

		/* If true, returns only services with standard metadata. */
		// allServices = client.getAllServices( true );
		 EndpointReferenceType[] allServices = client.getAllServices(false);

		if (allServices != null) {
			for (int i = 0; i < allServices.length; i++) {
				EndpointReferenceType service = allServices[i];
				log.info("\n\n" + service.getAddress());
				try {
					ServiceMetadata commonMetadata = MetadataUtils
							.getServiceMetadata(service);
					if (commonMetadata != null
							&& commonMetadata.getHostingResearchCenter() != null
							&& commonMetadata.getHostingResearchCenter()
									.getResearchCenter() != null) {
						log.info("Service is from:"
								+ commonMetadata.getHostingResearchCenter()
										.getResearchCenter().getDisplayName());
					}
				} catch (Exception e) {
					// e.printStackTrace();
					log
							.error("Unable to access service's standard resource properties: "
									+ e.getMessage());
				}
			}
		} else {
			log.warn("No services found.");

		}
		return allServices;
	}

}
