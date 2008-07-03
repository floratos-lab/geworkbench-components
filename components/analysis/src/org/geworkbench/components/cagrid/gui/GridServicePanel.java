package org.geworkbench.components.cagrid.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.ginkgo.labs.gui.SwingUtil;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author keshav
 * @version $Id: GridServicePanel.java,v 1.31 2008-07-03 17:33:52 jiz Exp $
 */
public class GridServicePanel extends JPanel {
	private Log log = LogFactory.getLog(this.getClass());

	JPanel innerPanel = null;

	JPanel outerPanel = null;

	JScrollPane serviceDetailsScrollPane = null;

	ButtonGroup buttonGroup = null;

	Collection<String> analysisSet = new HashSet<String>();

	GridServicesButtonListener gridServicesButtonListener;

	public DispatcherLabelListener dispatcherLabelListener = null;

	/**
	 * Visual Widget
	 */
	private JSplitPane jSplitPane1 = new JSplitPane();

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
		analysisSet.add("EIGrid");
		analysisSet.add("NetBoost");
		analysisSet.add("Anova");
		analysisSet.add("MatrixREDUCE");
		analysisSet.add("Mindy");

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
		indexServiceBuilder.appendColumn("right:pref");
		indexServiceBuilder.appendColumn("10dlu");

		String localButtonString = "Local";
		JRadioButton localButton = new JRadioButton(localButtonString);
		localButton.setSelected(true);
		localButton.setActionCommand(localButtonString);

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

		// index service label
		final JLabel indexServiceLabel = new JLabel(SwingUtil
				.convertTextToHtml("Change Index Service"));
		indexServiceLabel.setForeground(Color.BLUE);

		// index service label listener
		final IndexServiceLabelListener indexServiceLabelListener = new IndexServiceLabelListener(
				indexServiceLabel);
		indexServiceLabel.addMouseListener(indexServiceLabelListener);
		indexServiceLabel.addMouseMotionListener(new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				indexServiceLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

		});
		indexServiceBuilder.append(indexServiceLabel);

		// dispatcher label
		final JLabel dispatcherLabel = new JLabel(SwingUtil
				.convertTextToHtml("Change Dispatcher"));
		dispatcherLabel.setForeground(Color.BLUE);

		// dispatcher label listener
		dispatcherLabelListener = new DispatcherLabelListener(dispatcherLabel);
		dispatcherLabel.addMouseListener(dispatcherLabelListener);
		dispatcherLabel.addMouseMotionListener(new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				dispatcherLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

		});
		indexServiceBuilder.append(dispatcherLabel);

		// grid services button
		JButton getServicesButton = indexServiceLabelListener
				.getIndexServiceButton();
		indexServiceBuilder.append(getServicesButton);

		/* part B */
		final DefaultFormBuilder urlServiceBuilder = createUrlServiceBuilder();

		JScrollPane urlServiceBuilderScrollPane = new JScrollPane(
				urlServiceBuilder.getPanel(),
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		/* part C */
		final IndexServiceSelectionButtonListener indexServiceSelectionButtonListener = new IndexServiceSelectionButtonListener();

		gridServicesButtonListener = new GridServicesButtonListener(
				indexServiceSelectionButtonListener, indexServiceLabelListener,
				dispatcherLabelListener, urlServiceBuilder);
		getServicesButton.addActionListener(gridServicesButtonListener);

		/* add A, B, and C to the main (this) */
		this.add(indexServiceBuilder.getPanel(), BorderLayout.NORTH);

		/* add a split between B and C */
		jSplitPane1 = new JSplitPane();
		jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.add(jSplitPane1, BorderLayout.CENTER);

		jSplitPane1.add(indexServiceSelectionButtonListener
				.getServiceDetailsBuilderScrollPane(), JSplitPane.BOTTOM);
		jSplitPane1.add(urlServiceBuilderScrollPane, JSplitPane.TOP);

		this.revalidate();
	}

	/**
	 * 
	 * @return
	 */
	public static DefaultFormBuilder createUrlServiceBuilder() {
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
		return urlServiceBuilder;
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
				gridServicesButtonListener.setSelectedAnalysisType(type);
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
		return gridServicesButtonListener.getServicesButtonGroup();
	}
}
