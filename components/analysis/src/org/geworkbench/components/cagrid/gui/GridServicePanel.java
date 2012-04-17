package org.geworkbench.components.cagrid.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
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
 * @version $Id$
 */
public class GridServicePanel extends JPanel {
	private static final long serialVersionUID = -5691010510843212800L;

	private static Log log = LogFactory.getLog(GridServicePanel.class);

	private static Collection<String> analysisSet = new HashSet<String>();

	private GridServicesButtonListener gridServicesButtonListener;

	DefaultFormBuilder serviceDetailsBuilder = null;
	JScrollPane serviceDetailsBuilderScrollPane = null;
	DefaultFormBuilder urlServiceBuilder = createUrlServiceBuilder();

	private JRadioButton localButton = new JRadioButton("Local");

	public GridServicePanel(String name) {
		super();
		setName(name);
		setLayout(new BorderLayout());

		analysisSet.add("Hierarchical");
		analysisSet.add("Som");
		analysisSet.add("Aracne");
		analysisSet.add("EIGrid");
		analysisSet.add("NetBoost");
		analysisSet.add("Anova");
		analysisSet.add("MatrixREDUCE");
		analysisSet.add("Mindy");
		analysisSet.add("Evidence");
		analysisSet.add("Medusa");
		analysisSet.add("MarkUs");
		analysisSet.add("SkyLine");
		analysisSet.add("SkyBase");
		analysisSet.add("MRA");
		analysisSet.add("Idea");
		analysisSet.add("Sam");

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

		localButton.setSelected(true);

		/* create and add to the button group */
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(localButton);

		indexServiceBuilder.append(localButton);

		// index service label
		final JLabel indexServiceLabel = new JLabel(SwingUtil
				.convertTextToHtml("Change Index Service"));
		indexServiceLabel.setForeground(Color.BLUE);

		indexServiceLabel.addMouseListener(new UrlLabelListener(this,
				"indexServerURL", "indexServer.url", INDEX_INDEX));
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
		dispatcherLabel.addMouseListener(new UrlLabelListener(this,
				"dispatcherURL", "dispatcher.url",
				DISPATCHER_INDEX));
		dispatcherLabel.addMouseMotionListener(new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				dispatcherLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

		});
		indexServiceBuilder.append(dispatcherLabel);

		// grid services button
		JButton getServicesButton = new JButton("Search Grid Services");
		indexServiceBuilder.append(getServicesButton);

		/* part B */
		JScrollPane urlServiceBuilderScrollPane = new JScrollPane(
				urlServiceBuilder.getPanel(),
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		urlServiceBuilderScrollPane.setMinimumSize(new Dimension(300, 50));

		/* part C */
		serviceDetailsBuilder = new DefaultFormBuilder(new FormLayout(
				"right:max(60dlu;pref), 3dlu, max(150dlu;pref), 7dlu", ""));
		serviceDetailsBuilder.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		serviceDetailsBuilder.appendSeparator("Service Details");
		serviceDetailsBuilder.nextLine();

		serviceDetailsBuilderScrollPane = new JScrollPane(serviceDetailsBuilder
				.getPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		final IndexServiceSelectionButtonListener indexServiceSelectionButtonListener 
			= new IndexServiceSelectionButtonListener(this);

		gridServicesButtonListener = new GridServicesButtonListener(this,
				indexServiceSelectionButtonListener, buttonGroup);
		getServicesButton.addActionListener(gridServicesButtonListener);

		/* add A, B, and C to the main (this) */
		this.add(indexServiceBuilder.getPanel(), BorderLayout.NORTH);

		/* add a split between B and C */
		JSplitPane jSplitPane1 = new JSplitPane();
		jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.add(jSplitPane1, BorderLayout.CENTER);

		jSplitPane1.add(serviceDetailsBuilderScrollPane, JSplitPane.BOTTOM);
		jSplitPane1.add(urlServiceBuilderScrollPane, JSplitPane.TOP);

		this.revalidate();
	}

	/**
	 * 
	 * @return
	 */
	private static DefaultFormBuilder createUrlServiceBuilder() {
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

	public boolean isCaGridVersion() {
		return !localButton.isSelected();
	}


	String[] url = new String[2];
	private static int INDEX_INDEX = 0;
	private static int DISPATCHER_INDEX = 1;
	public String getIndexServerUrl(){
		return url[INDEX_INDEX];
	}

	public String getDispatcherUrl() {
		return url[DISPATCHER_INDEX];
	}

	public String getServiceUrl() {
		return gridServicesButtonListener.getServiceUrl();
	}
	
}
