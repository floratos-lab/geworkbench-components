package org.geworkbench.components.cagrid.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;

import junit.framework.TestCase;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.Util;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.columbia.geworkbench.cagrid.discovery.client.DiscoveryServiceUtil;
import gov.nih.nci.cagrid.discovery.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;

/**
 * 
 * @author keshav
 * @version $Id: CaGridGuiTest.java,v 1.4 2007-03-15 16:21:39 keshav Exp $
 */
public class CaGridGuiTest extends TestCase {

	private Log log = LogFactory.getLog(CaGridGuiTest.class);

	/* for testDefaultFormBuilder */
	private int DEFAULT_PORT = 8080;

	private String DEFAULT_FILTER = "hierarchical";

	private String DEFAULT_HOST = "localhost";

	private String host = DEFAULT_HOST;

	private int port = DEFAULT_PORT;

	private String filter = DEFAULT_FILTER;

	JLabel indexServiceLabel = new JLabel("test text");

	private static JDialog jIndexServiceDialog = null;

	/* for testCreateScrollPane */
	private JPanel mainPanel;

	private JPanel innerPanel; // Panel w/ JScrollPane

	private JScrollPane scroll;

	private JButton addItem;

	public static final int WINDOW_WIDTH = 200;

	public static final int WINDOW_HEIGHT = 200;

	// /**
	// * Creates a form builder.
	// *
	// */
	// public void testDefaultFormBuilder() {
	//
	// jIndexServiceDialog = new JDialog();
	// // JPanel mainPanel = new JPanel(new BorderLayout());
	// DefaultFormBuilder mainPanelBuilder = new DefaultFormBuilder(
	// new FormLayout("right:20dlu"));
	// final JTextField hostField = new JTextField(host);
	// final JTextField portField = new JTextField("" + port);
	// final JTextField filterField = new JTextField("" + filter);
	//
	// JPanel buttonPanel = new JPanel(new FlowLayout());
	// JButton okButton = new JButton("Ok");
	// okButton.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// port = Integer.parseInt(portField.getText());
	// host = hostField.getText();
	// filter = filterField.getText();
	//
	// log.info("host: " + host);
	// log.info("port: " + port);
	// log.info("filter: " + filter);
	//
	// jIndexServiceDialog.dispose();
	// }
	// });
	//
	// JButton cancelButton = new JButton("Cancel");
	// cancelButton.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// jIndexServiceDialog.dispose();
	// }
	// });
	//
	// /* add to button panel */
	// buttonPanel.add(okButton);
	// buttonPanel.add(cancelButton);
	//
	// mainPanelBuilder.appendColumn("5dlu");
	// mainPanelBuilder.appendColumn("45dlu");
	//
	// /* add main panel */
	// mainPanelBuilder.append("host", hostField);
	// mainPanelBuilder.append("port", portField);
	// mainPanelBuilder.append("filter", filterField);
	//
	// if (!StringUtils.isEmpty(host))
	// hostField.setText(host);
	//
	// /* change color for clickable text */
	// indexServiceLabel.setForeground(Color.magenta);
	//
	// JPanel mainPanel = new JPanel(new BorderLayout());
	// mainPanel.add(mainPanelBuilder.getPanel());
	// mainPanel.add(buttonPanel, BorderLayout.SOUTH);
	// jIndexServiceDialog.add(mainPanel);
	// jIndexServiceDialog.setModal(true);
	// jIndexServiceDialog.pack();
	// Util.centerWindow(jIndexServiceDialog);
	// jIndexServiceDialog.setVisible(true);
	//
	// }
	//
	// /**
	// * Creates a scroll pane.
	// *
	// */
	// public void testCreateScrollPane() {
	//
	// mainPanel = new JPanel();
	// innerPanel = new JPanel();
	//
	// // added code:
	// innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
	// // end added code.
	//
	// // removed code:
	// // innerPanel.setPreferredSize(new Dimension(100, 100));
	// // end removed code.
	//
	// scroll = new JScrollPane(innerPanel);
	// scroll.setPreferredSize(new Dimension(100, 100));
	// addItem = new JButton("Add an item to JPanel");
	// addItem.addActionListener(new ButtonListener());
	// mainPanel.add(scroll);
	// mainPanel.add(addItem);
	//
	// JDialog frame = new JDialog();
	// frame.add(mainPanel);
	// frame.setModal(true);
	// frame.pack();
	// Util.centerWindow(frame);
	// frame.setVisible(true);
	// }
	//
	// /**
	// * Action listener for the button used by the scroll pane example.
	// *
	// * @author keshav
	// *
	// */
	// private class ButtonListener implements ActionListener {
	//
	// public void actionPerformed(ActionEvent e) {
	//
	// innerPanel.add(new JLabel("Testing......"));
	// innerPanel.validate();
	//
	// // added code:
	// scroll.validate();
	// scroll.getVerticalScrollBar().setValue(
	// (scroll.getVerticalScrollBar()).getMaximum());
	// // end added code.
	//
	// }
	// }

	/**
	 * 
	 */
	public void testComplexDefaultFormBuilder() {
		JDialog testDialog = new JDialog();

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
		GridSelectionButtonListener gridSelectionButtonListener = new GridSelectionButtonListener();
		String localButtonString = "Local";
		JRadioButton localButton = new JRadioButton(localButtonString);
		localButton.setSelected(true);
		localButton.setActionCommand(localButtonString);
		localButton.addActionListener(gridSelectionButtonListener);
		// TODO move this
		String gridButtonString = "Grid";
		JRadioButton gridButton = new JRadioButton(gridButtonString);
		gridButton.setSelected(false);
		gridButton.setActionCommand(gridButtonString);
		gridButton.addActionListener(gridSelectionButtonListener);

		ButtonGroup buttonGroup = new ButtonGroup();
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
		final Map<String, EndpointReferenceType> seenServices = new HashMap<String, EndpointReferenceType>();

		final DefaultFormBuilder serviceDetailsBuilder = new DefaultFormBuilder(
				new FormLayout(
						"right:max(60dlu;pref), 3dlu, max(150dlu;pref), 7dlu",
						""));
		serviceDetailsBuilder.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		serviceDetailsBuilder.appendSeparator("Service Details");
		serviceDetailsBuilder.nextLine();

		JScrollPane serviceDetailsBuilderScrollPane = new JScrollPane(
				serviceDetailsBuilder.getPanel(),
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// end C

		// TODO refactor me into a separate listener
		final ButtonGroup servicesButtonGroup = new ButtonGroup();
		getServicesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EndpointReferenceType[] services = DiscoveryServiceUtil
						.getServices(indexServiceLabelListener.getHost(),
								indexServiceLabelListener.getPort(),
								indexServiceLabelListener.getFilter());

				for (EndpointReferenceType service : services) {

					ServiceMetadata commonMetadata;
					try {
						commonMetadata = MetadataUtils
								.getServiceMetadata(service);
					} catch (Exception e1) {
						throw new RuntimeException(e1);
					}

					String url = DiscoveryServiceUtil
							.getUrlFromMetadata(service);
					String description = DiscoveryServiceUtil
							.getDescriptionFromMetadata(commonMetadata);

					JRadioButton button = new JRadioButton();
					IndexServiceSelectionButtonListener indexSelectionButtonListener = new IndexServiceSelectionButtonListener();
					button.setActionCommand(url);
					button.addActionListener(indexSelectionButtonListener);

					servicesButtonGroup.add(button);

					/* check if we've already seen this service */
					if (!seenServices.containsKey(url)) {
						seenServices.put(url, service);
						urlServiceBuilder.append(button);
						urlServiceBuilder.append(new JLabel(url));
						urlServiceBuilder.append(new JLabel("put center here"));
						urlServiceBuilder.append(new JLabel(description));
						urlServiceBuilder.nextLine();

						serviceDetailsBuilder.append("Research Center Name: ",
								new JLabel("put center name"));
						serviceDetailsBuilder.append("Type: ", new JLabel(
								"put type here"));
						serviceDetailsBuilder.append("Description: ",
								new JLabel(description));
						serviceDetailsBuilder.append("Contact: ", new JLabel(
								"put contact here"));
						serviceDetailsBuilder.append("Contact Number: ",
								new JLabel("put number here"));
						serviceDetailsBuilder.append("Address: ", new JLabel(
								"put address here"));

					}
				}
				urlServiceBuilder.getPanel().revalidate();
				serviceDetailsBuilder.getPanel().revalidate();
			}
		});

		/* add A, B, and C to the main */
		testDialog.add(indexServiceBuilder.getPanel(), BorderLayout.NORTH);
		testDialog.add(urlServiceBuilderScrollPane);
		testDialog.add(serviceDetailsBuilderScrollPane, BorderLayout.SOUTH);
		
		testDialog.pack();
		Util.centerWindow(testDialog);
		testDialog.setModal(true);
		testDialog.setVisible(true);
	}

}
