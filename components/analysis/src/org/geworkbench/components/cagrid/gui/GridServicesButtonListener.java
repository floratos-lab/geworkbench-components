package org.geworkbench.components.cagrid.gui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;

import com.jgoodies.forms.builder.DefaultFormBuilder;

import edu.columbia.geworkbench.cagrid.discovery.client.DiscoveryServiceUtil;
import gov.nih.nci.cagrid.metadata.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;

/**
 * An action listener for the grid services button.
 * 
 * @author keshav
 * @version $Id$
 */
public class GridServicesButtonListener implements ActionListener {
	private static Log log = LogFactory.getLog(GridServicesButtonListener.class);

	private IndexServiceSelectionButtonListener indexServiceSelectionButtonListener = null;

	private String selectedAnalysisType = null;

	private ButtonGroup servicesButtonGroup = null;

	private GridServicePanel gridServicePanel = null;

	/**
	 * 
	 * @param indexServiceSelectionButtonListener
	 * @param indexServiceLabelListener
	 * @param urlServiceBuilder
	 */
	public GridServicesButtonListener(
			final GridServicePanel gridServicePanel,
			final IndexServiceSelectionButtonListener indexServiceSelectionButtonListener,
			final ButtonGroup servicesButtonGroup
			) {
		super();
		this.gridServicePanel = gridServicePanel;
		this.indexServiceSelectionButtonListener = indexServiceSelectionButtonListener;
		this.servicesButtonGroup = servicesButtonGroup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		Thread t = new Thread(new Runnable() {
			public void run() {
				ProgressBar pBar = Util.createProgressBar("Grid Services",
						"Retrieving Services");

				pBar.start();
				pBar.reset();

				String indexServerUrl = gridServicePanel.getIndexServerUrl();
				EndpointReferenceType[] services = null;
				try {
					services = DiscoveryServiceUtil.getServices(indexServerUrl,gridServicePanel.getDispatcherUrl(),
							selectedAnalysisType);
				} catch (Exception e) {
					final JLabel linkedLabel = new JLabel("<html>No service running. Please check with the administrator of your grid infrastructure. For services hosted by the geWorkbench team you can inquire at the geWorkbench <A href=https://cabig-kc.nci.nih.gov/Molecular/forums/viewforum.php?f=10&sid=237690e6926a15d8062491481120867a>KC Forum</a></html>");
					linkedLabel.addMouseListener(new MouseListener() {
						public void mouseClicked(MouseEvent e) {
							try {
								BrowserLauncher.openURL("https://cabig-kc.nci.nih.gov/Molecular/forums/viewforum.php?f=10&sid=237690e6926a15d8062491481120867a");
								} catch (IOException e1) {
								e1.printStackTrace();
								}
							
						}
						public void mouseEntered(MouseEvent e) {
							// TODO Auto-generated method stub
						}
						public void mouseExited(MouseEvent e) {
							// TODO Auto-generated method stub	
						}
						public void mousePressed(MouseEvent e) {
							// TODO Auto-generated method stub	
						}
						public void mouseReleased(MouseEvent e) {
							// TODO Auto-generated method stub	
						}
					});
					
					linkedLabel
							.addMouseMotionListener(new MouseMotionAdapter() {
								public void mouseMoved(MouseEvent e) {
									linkedLabel.setCursor(new Cursor(
											Cursor.HAND_CURSOR));
								}

							});
					
					JOptionPane.showMessageDialog(null, linkedLabel, "Error",
							JOptionPane.ERROR_MESSAGE);
				}

				DefaultFormBuilder urlServiceBuilder = gridServicePanel.urlServiceBuilder;

				if (services == null) {
					// TODO clear panel if populated
				} else {
					for (EndpointReferenceType service : services) {

						ServiceMetadata commonMetadata;
						String researchCenter = "C2B2, Columbia University", description = null;
						try {
							commonMetadata = MetadataUtils
									.getServiceMetadata(service);
							researchCenter = DiscoveryServiceUtil
									.getResearchCenterName(commonMetadata);
							description = DiscoveryServiceUtil
									.getDescription(commonMetadata);
						} catch (Exception ec) {
							log.info("Unable to get metadata for "+service.toString());
						}
						try {
							String url = DiscoveryServiceUtil.getUrl(service);

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
				gridServicePanel.getRootPane().revalidate();
				gridServicePanel.serviceDetailsBuilder
						.getPanel().revalidate();

			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();

	}

	/**
	 * 
	 * @param selectedAnalysisType
	 */
	public void setSelectedAnalysisType(String selectedAnalysisType) {
		this.selectedAnalysisType = selectedAnalysisType;
	}

	public String getServiceUrl() {
		ButtonModel bm = servicesButtonGroup.getSelection();

		if (bm == null) {
			return null;
		}

		return bm.getActionCommand();
	}
	
}
