package edu.columbia.geworkbench.cagrid.discovery.client;

import org.apache.axis.message.addressing.AttributedURI;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.nih.nci.cagrid.metadata.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;
import gov.nih.nci.cagrid.metadata.ServiceMetadataHostingResearchCenter;
import gov.nih.nci.cagrid.metadata.ServiceMetadataServiceDescription;
import gov.nih.nci.cagrid.metadata.common.Address;
import gov.nih.nci.cagrid.metadata.common.PointOfContact;
import gov.nih.nci.cagrid.metadata.common.ResearchCenter;
import gov.nih.nci.cagrid.metadata.common.ResearchCenterDescription;
import gov.nih.nci.cagrid.metadata.common.ResearchCenterPointOfContactCollection;
import gov.nih.nci.cagrid.metadata.service.InputParameter;
import gov.nih.nci.cagrid.metadata.service.Operation;
import gov.nih.nci.cagrid.metadata.service.OperationInputParameterCollection;
import gov.nih.nci.cagrid.metadata.service.Service;
import gov.nih.nci.cagrid.metadata.service.ServiceContext;
import gov.nih.nci.cagrid.metadata.service.ServiceContextOperationCollection;
import gov.nih.nci.cagrid.metadata.service.ServiceServiceContextCollection;

/**
 * 
 * @author keshav
 * @version $Id: DiscoveryServiceUtil.java,v 1.6 2008-11-18 21:03:08 keshav Exp $
 */
public class DiscoveryServiceUtil {
	private static final String NOT_AVAILABLE = "unavailable";

	private static Log log = LogFactory.getLog(DiscoveryServiceUtil.class);

	/**
	 * Retrieves all services at indexServiceUrl
	 * 
	 * @param indexServiceHost
	 * @param indexServicePort
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public static EndpointReferenceType[] getServices(String indexServiceUrl,
			String search) throws Exception {

		EndpointReferenceType[] allServices = null;

		AnalyticalServiceDiscoveryClient client = new AnalyticalServiceDiscoveryClient(
				indexServiceUrl);

		if (StringUtils.isEmpty(search)) {
			allServices = client.getAllServices();
		} else {
			allServices = client.discoverServicesBySearchString(search);
		}

		if (log.isInfoEnabled()){
			try{
				displayMetadata(allServices);
			}catch(Exception e){
				log.info("Unable to get metadata for services");
			}
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

				getUrl(service);

				getDescription(commonMetadata);

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
	public static String getUrl(EndpointReferenceType service) {

		AttributedURI uri = service.getAddress();

		String url = uri.toString();

		return url;
	}

	/**
	 * 
	 * @param service
	 * @return
	 */
	public static String getDescription(ServiceMetadata commonMetadata) {

		String description = NOT_AVAILABLE;

		ServiceMetadataServiceDescription serviceMetadataDescription = commonMetadata
				.getServiceDescription();
		if (serviceMetadataDescription == null)
			return description;

		Service service = serviceMetadataDescription.getService();
		if (service == null)
			return description;

		description = service.getDescription();
		if (StringUtils.isEmpty(description))
			description = NOT_AVAILABLE;

		return description;
	}

	/**
	 * 
	 * @param commonMetadata
	 * @return
	 */
	public static String getResearchCenterName(ServiceMetadata commonMetadata) {

		String researchCenterName = NOT_AVAILABLE;

		ServiceMetadataHostingResearchCenter serviceMetadataHostingResearchCenter = commonMetadata
				.getHostingResearchCenter();

		if (serviceMetadataHostingResearchCenter == null)
			return researchCenterName;

		ResearchCenter researchCenter = serviceMetadataHostingResearchCenter
				.getResearchCenter();
		if (researchCenter == null)
			return researchCenterName;

		researchCenterName = researchCenter.getDisplayName();
		if (StringUtils.isEmpty(researchCenterName))
			researchCenterName = NOT_AVAILABLE;

		return researchCenterName;

	}

	/**
	 * 
	 * @param commonMetadata
	 * @return
	 */
	public static String getContactName(ServiceMetadata commonMetadata) {

		String contactNames = NOT_AVAILABLE;

		ServiceMetadataHostingResearchCenter serviceMetadataHostingResearchCenter = commonMetadata
				.getHostingResearchCenter();
		if (serviceMetadataHostingResearchCenter == null)
			return contactNames;

		ResearchCenter researchCenter = serviceMetadataHostingResearchCenter
				.getResearchCenter();
		if (researchCenter == null)
			return contactNames;

		ResearchCenterPointOfContactCollection pointOfContactCol = researchCenter
				.getPointOfContactCollection();
		if (pointOfContactCol == null)
			return contactNames;

		PointOfContact contacts[] = pointOfContactCol.getPointOfContact();
		if (contacts == null)
			return contactNames;

		int i = 0;
		for (PointOfContact contact : contacts) {
			contactNames = contact.getFirstName() + " " + contact.getLastName();
			if (i < contacts.length - 1)
				contactNames = contactNames + ", ";
		}

		if (StringUtils.isEmpty(contactNames))
			contactNames = NOT_AVAILABLE;

		return contactNames;
	}

	/**
	 * 
	 * @param commonMetadata
	 * @return
	 */
	public static String getContactNumber(ServiceMetadata commonMetadata) {
		String contactNum = NOT_AVAILABLE;

		ServiceMetadataHostingResearchCenter serviceMetadataHostingResearchCenter = commonMetadata
				.getHostingResearchCenter();
		if (serviceMetadataHostingResearchCenter == null)
			return contactNum;

		ResearchCenter researchCenter = serviceMetadataHostingResearchCenter
				.getResearchCenter();
		if (researchCenter == null)
			return contactNum;

		ResearchCenterPointOfContactCollection pointOfContactCol = researchCenter
				.getPointOfContactCollection();
		if (pointOfContactCol == null)
			return contactNum;

		PointOfContact contacts[] = pointOfContactCol.getPointOfContact();
		if (contacts == null)
			return contactNum;

		int i = 0;
		for (PointOfContact contact : contacts) {
			contactNum = contact.getPhoneNumber();
			if (i < contacts.length - 1)
				contactNum = contactNum + ", ";
		}

		if (StringUtils.isEmpty(contactNum))
			contactNum = NOT_AVAILABLE;

		return contactNum;
	}

	/**
	 * 
	 * @param commonMetadata
	 * @return
	 */
	public static String getAddress(ServiceMetadata commonMetadata) {
		String addressName = NOT_AVAILABLE;

		ServiceMetadataHostingResearchCenter serviceMetadataHostingResearchCenter = commonMetadata
				.getHostingResearchCenter();
		if (serviceMetadataHostingResearchCenter == null)
			return addressName;

		ResearchCenter researchCenter = serviceMetadataHostingResearchCenter
				.getResearchCenter();
		if (researchCenter == null)
			return addressName;

		Address address = researchCenter.getAddress();
		if (address == null)
			return addressName;

		addressName = address.toString();
		if (StringUtils.isEmpty(addressName))
			addressName = NOT_AVAILABLE;

		return addressName;
	}

	/**
	 * 
	 * @param commonMetadata
	 * @return
	 */
	public static String getType(ServiceMetadata commonMetadata) {
		String typeDescription = NOT_AVAILABLE;

		ServiceMetadataHostingResearchCenter serviceMetadataHostingResearchCenter = commonMetadata
				.getHostingResearchCenter();
		if (serviceMetadataHostingResearchCenter == null)
			return typeDescription;

		ResearchCenter researchCenter = serviceMetadataHostingResearchCenter
				.getResearchCenter();
		if (researchCenter == null)
			return typeDescription;

		ResearchCenterDescription type = researchCenter
				.getResearchCenterDescription();
		if (type == null)
			return typeDescription;

		typeDescription = type.getDescription();
		if (StringUtils.isEmpty(typeDescription))
			typeDescription = NOT_AVAILABLE;

		return typeDescription;
	}

}
