package org.geworkbench.components.cagrid.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;

import com.jgoodies.forms.builder.DefaultFormBuilder;

import edu.columbia.geworkbench.cagrid.discovery.client.DiscoveryServiceUtil;
import gov.nih.nci.cagrid.discovery.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;

/**
 * An action listener for the grid services button.
 * 
 * @author keshav
 * @version $Id: GridServicesButtonListener.java,v 1.1 2007/04/03 02:39:14
 *          keshav Exp $
 */
public class GridServicesButtonListener implements ActionListener {

	IndexServiceSelectionButtonListener indexServiceSelectionButtonListener = null;

	IndexServiceLabelListener indexServiceLabelListener = null;

	DefaultFormBuilder urlServiceBuilder = null;

	String selectedAnalysisType = null;

	ButtonGroup servicesButtonGroup = null;

	public GridServicesButtonListener(
			IndexServiceSelectionButtonListener indexServiceSelectionButtonListener,
			IndexServiceLabelListener indexServiceLabelListener,
			DefaultFormBuilder urlServiceBuilder) {
		super();
		this.indexServiceSelectionButtonListener = indexServiceSelectionButtonListener;
		this.indexServiceLabelListener = indexServiceLabelListener;
		this.urlServiceBuilder = urlServiceBuilder;
	}

	public void actionPerformed(ActionEvent e) {

		Thread t = new Thread(new Runnable() {
			public void run() {
				ProgressBar pBar = Util.createProgressBar("Grid Services",
						"Retrieving Services");

				pBar.start();
				pBar.reset();
				EndpointReferenceType[] services = DiscoveryServiceUtil
						.getServices(indexServiceLabelListener.getHost(),
								indexServiceLabelListener.getPort(),
								selectedAnalysisType);

				if (services == null) {
					// TODO clear panel if populated
				}

				else {
					servicesButtonGroup = new ButtonGroup();
					for (EndpointReferenceType service : services) {

						ServiceMetadata commonMetadata;
						try {
							commonMetadata = MetadataUtils
									.getServiceMetadata(service);

							String url = DiscoveryServiceUtil.getUrl(service);
							String researchCenter = DiscoveryServiceUtil
									.getResearchCenterName(commonMetadata);
							String description = DiscoveryServiceUtil
									.getDescription(commonMetadata);

							JRadioButton button = new JRadioButton();
							button
									.addActionListener(indexServiceSelectionButtonListener);
							button.setActionCommand(url);
							servicesButtonGroup.add(button);

							/* check if we've already seen this service */
							if (!indexServiceSelectionButtonListener
									.getSeenServices().containsKey(url)) {
								indexServiceSelectionButtonListener
										.getSeenServices().put(url, service);

								urlServiceBuilder.append(button);
								urlServiceBuilder.append(new JLabel(url));
								urlServiceBuilder.append(new JLabel(
										researchCenter));
								urlServiceBuilder
										.append(new JLabel(description));
								urlServiceBuilder.nextLine();
							}

						} catch (Exception e1) {
							throw new RuntimeException(e1);
						}
					}
				}

				pBar.stop();

				urlServiceBuilder.getPanel().revalidate();
				indexServiceSelectionButtonListener.getServiceDetailsBuilder()
						.getPanel().revalidate();

			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();

	}

	public ButtonGroup getServicesButtonGroup() {
		return this.servicesButtonGroup;
	}

	public void setSelectedAnalysisType(String selectedAnalysisType) {
		this.selectedAnalysisType = selectedAnalysisType;
	}
}
