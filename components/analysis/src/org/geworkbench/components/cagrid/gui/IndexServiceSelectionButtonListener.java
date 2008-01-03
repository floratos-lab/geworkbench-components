package org.geworkbench.components.cagrid.gui;

import edu.columbia.geworkbench.cagrid.discovery.client.DiscoveryServiceUtil;
import gov.nih.nci.cagrid.metadata.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;

import org.apache.axis.message.addressing.EndpointReferenceType;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author keshav
 * @version $Id: IndexServiceSelectionButtonListener.java,v 1.1 2007/03/15
 *          15:41:35 keshav Exp $
 */
public class IndexServiceSelectionButtonListener implements ActionListener {

	private DefaultFormBuilder serviceDetailsBuilder = null;

	private JScrollPane serviceDetailsBuilderScrollPane = null;

	private String url = null;

	private Map<String, EndpointReferenceType> seenServices = null;

	private Map<String, EndpointReferenceType> seenServicesTwice = null;

	/**
	 * 
	 * 
	 */
	public IndexServiceSelectionButtonListener() {

		seenServices = new HashMap<String, EndpointReferenceType>();

		seenServicesTwice = new HashMap<String, EndpointReferenceType>();

		serviceDetailsBuilder = new DefaultFormBuilder(new FormLayout(
				"right:max(60dlu;pref), 3dlu, max(150dlu;pref), 7dlu", ""));
		serviceDetailsBuilder.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		serviceDetailsBuilder.appendSeparator("Service Details");
		serviceDetailsBuilder.nextLine();

		serviceDetailsBuilderScrollPane = new JScrollPane(serviceDetailsBuilder
				.getPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		url = e.getActionCommand();

		if (seenServices.containsKey(url)) {

			/* check if we've already seen this service elsewhere */
			if (!seenServicesTwice.containsKey(url)) {

				EndpointReferenceType service = seenServices.get(url);

				seenServicesTwice.put(url, service);

				ServiceMetadata commonMetadata;
				try {
					commonMetadata = MetadataUtils.getServiceMetadata(service);
				} catch (Exception e1) {
					throw new RuntimeException(e1);
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

	}

	/**
	 * 
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	public Map<String, EndpointReferenceType> getSeenServices() {
		return seenServices;
	}

	/**
	 * 
	 * @return
	 */
	public DefaultFormBuilder getServiceDetailsBuilder() {
		return serviceDetailsBuilder;
	}

	/**
	 * 
	 * @return
	 */
	public JScrollPane getServiceDetailsBuilderScrollPane() {
		return serviceDetailsBuilderScrollPane;
	}

}
