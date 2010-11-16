package org.geworkbench.components.cagrid.gui;

import edu.columbia.geworkbench.cagrid.discovery.client.DiscoveryServiceUtil;
import gov.nih.nci.cagrid.metadata.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * 
 * @author keshav
 * @version $Id$
 */
public class IndexServiceSelectionButtonListener implements ActionListener {
	private static Log log = LogFactory.getLog(IndexServiceSelectionButtonListener.class);

	private String url = null;

	private Map<String, EndpointReferenceType> seenServices = null;

	private Map<String, EndpointReferenceType> seenServicesTwice = null;

	private GridServicePanel gridServicePanel = null;
	/**
	 * 
	 * 
	 */
	public IndexServiceSelectionButtonListener(final GridServicePanel gridServicePanel) {
		this.gridServicePanel = gridServicePanel;

		seenServices = new HashMap<String, EndpointReferenceType>();

		seenServicesTwice = new HashMap<String, EndpointReferenceType>();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		url = e.getActionCommand();

		/* check if we've already seen this service elsewhere */
		if (seenServices.containsKey(url) && !seenServicesTwice.containsKey(url)) {
				DefaultFormBuilder serviceDetailsBuilder = gridServicePanel.serviceDetailsBuilder;
				JComponent serviceDetailsBuilderScrollPane= gridServicePanel.serviceDetailsBuilderScrollPane;;

				EndpointReferenceType service = seenServices.get(url);

				seenServicesTwice.put(url, service);

				ServiceMetadata commonMetadata;
				try {
					commonMetadata = MetadataUtils.getServiceMetadata(service);
				} catch (Exception e1) {
					log.info("Unable to get metadata for "+url);
					serviceDetailsBuilder.append("URL: ", new JLabel(url));
					serviceDetailsBuilder.append("Research Center Name: ",
						new JLabel("C2B2, Columbia University"));
					serviceDetailsBuilderScrollPane.revalidate();
					return;
				}

				String url = DiscoveryServiceUtil.getUrl(service);
				String researchCenter = DiscoveryServiceUtil
						.getResearchCenterName(commonMetadata);
				String description = DiscoveryServiceUtil
						.getDescription(commonMetadata);
				String type = DiscoveryServiceUtil.getType(commonMetadata);
				String contact = DiscoveryServiceUtil
						.getContactName(commonMetadata);
				String contactNumber = DiscoveryServiceUtil
						.getContactNumber(commonMetadata);
				String address = DiscoveryServiceUtil
						.getAddress(commonMetadata);

				serviceDetailsBuilder.append("URL: ", new JLabel(url));
				serviceDetailsBuilder.append("Research Center Name: ",
						new JLabel(researchCenter));
				serviceDetailsBuilder.append("Type: ", new JLabel(type));
				serviceDetailsBuilder.append("Description: ", new JLabel(
						description));
				serviceDetailsBuilder.append("Contact: ", new JLabel(contact));
				serviceDetailsBuilder.append("Contact Number: ", new JLabel(
						contactNumber));
				serviceDetailsBuilder.append("Address: ", new JLabel(address));

				serviceDetailsBuilderScrollPane.revalidate();
		}

	}

	public Map<String, EndpointReferenceType> getSeenServices() {
		return seenServices;
	}

}
