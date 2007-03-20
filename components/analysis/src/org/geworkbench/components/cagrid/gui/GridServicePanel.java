package org.geworkbench.components.cagrid.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.columbia.geworkbench.cagrid.discovery.client.DiscoveryServiceUtil;
import gov.nih.nci.cagrid.discovery.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;

/**
 * 
 * @author keshav
 * @version $Id: GridServicePanel.java,v 1.15 2007-03-20 19:32:22 keshav Exp $
 */
public class GridServicePanel extends JPanel {
	private Log log = LogFactory.getLog(this.getClass());

	JPanel innerPanel = null;

	JPanel outerPanel = null;

	JScrollPane serviceDetailsScrollPane = null;

	ButtonGroup buttonGroup = null;

	ButtonGroup servicesButtonGroup = null;

	Collection<String> analysisSet = new HashSet<String>();

	String selectedAnalysisType = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GridServicePanel(String name) {
		super();
		super.setName(name);
		super.setLayout(new BorderLayout());

		analysisSet.add("Hierarchical");
		analysisSet.add("Som");
		analysisSet.add("Aracne");

		/* part A */
		DefaultFormBuilder indexServiceBuilder = new DefaultFormBuilder(
				new FormLayout(""));
		indexServiceBuilder.appendColumn("right:pref");
		indexServiceBuilder.appendColumn("10dlu");
		indexServiceBuilder.appendColumn("right:pref");
		indexServiceBuilder.appendColumn("10dlu");
		indexServiceBuilder.appendColumn("right:pref");
		indexServiceBuilder.appendColumn("10dlu");
		indexServiceBuilder.appendColumn("right:pref");
		indexServiceBuilder.appendColumn("10dlu");

		// TODO move this
		String localButtonString = "Local";
		JRadioButton localButton = new JRadioButton(localButtonString);
		localButton.setSelected(true);
		localButton.setActionCommand(localButtonString);
		// TODO move this
		String gridButtonString = "Grid";
		JRadioButton gridButton = new JRadioButton(gridButtonString);
		gridButton.setSelected(false);
		gridButton.setActionCommand(gridButtonString);
		/* add to the button group */
		buttonGroup = new ButtonGroup();
		buttonGroup.add(localButton);
		buttonGroup.add(gridButton);

		indexServiceBuilder.append(localButton);
		indexServiceBuilder.append(gridButton);

		JLabel indexServiceLabel = new JLabel("Change Index Service");

		indexServiceLabel.setForeground(Color.BLUE);

		final IndexServiceLabelListener indexServiceLabelListener = new IndexServiceLabelListener(
				indexServiceLabel);
		indexServiceLabel.addMouseListener(indexServiceLabelListener);
		indexServiceBuilder.append(indexServiceLabel);

		JButton getServicesButton = indexServiceLabelListener
				.getIndexServiceButton();
		indexServiceBuilder.append(getServicesButton);

		/* part B */
		final DefaultFormBuilder urlServiceBuilder = new DefaultFormBuilder(
				new FormLayout(""));
		urlServiceBuilder.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		urlServiceBuilder.appendColumn("center:pref");
		urlServiceBuilder.appendColumn("10dlu");
		urlServiceBuilder.appendColumn("center:pref");
		urlServiceBuilder.appendColumn("10dlu");
		urlServiceBuilder.appendColumn("center:pref");
		urlServiceBuilder.appendColumn("10dlu");
		urlServiceBuilder.appendColumn("center:pref");
		urlServiceBuilder.appendColumn("10dlu");

		urlServiceBuilder.append("");
		urlServiceBuilder.append("Grid Service URL");
		urlServiceBuilder.append("Research Center Name");
		urlServiceBuilder.append("Description");

		JScrollPane urlServiceBuilderScrollPane = new JScrollPane(
				urlServiceBuilder.getPanel(),
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		/* part C */
		// TODO refactor me into a separate listener
		final IndexServiceSelectionButtonListener indexServiceSelectionButtonListener = new IndexServiceSelectionButtonListener();
		servicesButtonGroup = new ButtonGroup();
		getServicesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EndpointReferenceType[] services = DiscoveryServiceUtil
						.getServices(indexServiceLabelListener.getHost(),
								indexServiceLabelListener.getPort(),
								selectedAnalysisType);

				if (services == null) {
					// TODO clear panel if populated
				}

				else {
					for (EndpointReferenceType service : services) {

						ServiceMetadata commonMetadata;
						try {
							commonMetadata = MetadataUtils
									.getServiceMetadata(service);
						} catch (Exception e1) {
							throw new RuntimeException(e1);
						}

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
						indexServiceSelectionButtonListener
								.setServicesButtonGroup(servicesButtonGroup);

						/* check if we've already seen this service */
						if (!indexServiceSelectionButtonListener
								.getSeenServices().containsKey(url)) {
							indexServiceSelectionButtonListener
									.getSeenServices().put(url, service);

							urlServiceBuilder.append(button);
							urlServiceBuilder.append(new JLabel(url));
							urlServiceBuilder
									.append(new JLabel(researchCenter));
							urlServiceBuilder.append(new JLabel(description));
							urlServiceBuilder.nextLine();
						}
					}
				}

				urlServiceBuilder.getPanel().revalidate();
				indexServiceSelectionButtonListener.getServiceDetailsBuilder()
						.getPanel().revalidate();
			}
		});

		/* add A, B, and C to the main (this) */
		this.add(indexServiceBuilder.getPanel(), BorderLayout.NORTH);
		this.add(urlServiceBuilderScrollPane);
		this.add(indexServiceSelectionButtonListener
				.getServiceDetailsBuilderScrollPane(), BorderLayout.SOUTH);
	}

	/**
	 * 
	 * @param analysisType
	 */
	public void setAnalysisType(AbstractAnalysis analysisType) {

		for (String type : analysisSet) {
			if (StringUtils.lowerCase(analysisType.getLabel()).contains(
					StringUtils.lowerCase(type))) {
				log.info("Analysis is " + type);
				selectedAnalysisType = type;
				break;
			}
		}

	}

	/**
	 * 
	 * @return
	 */
	public ButtonGroup getButtonGroup() {
		return buttonGroup;
	}

	/**
	 * 
	 * @return
	 */
	public ButtonGroup getServicesButtonGroup() {
		return servicesButtonGroup;
	}
}
