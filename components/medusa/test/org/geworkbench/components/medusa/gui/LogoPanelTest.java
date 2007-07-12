package org.geworkbench.components.medusa.gui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumn;

import junit.framework.TestCase;

import org.ginkgo.labs.gui.SwingUtil;
import org.ginkgo.labs.psam.PsamUtil;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.larvalabs.chart.PSAMPlot;

import edu.columbia.ccls.medusa.io.RuleParser;
import edu.columbia.ccls.medusa.io.SerializedRule;

/**
 * This tests the logo panel gui. All additions to the logo gui are first tested
 * here.
 * 
 * @author keshav
 * @version $Id: LogoPanelTest.java,v 1.2 2007-07-12 21:12:56 keshav Exp $
 */
public class LogoPanelTest extends TestCase {

	private static final int COLUMN_WIDTH = 80;

	SerializedRule srule = null;

	String rulesPath = "data/test/dataset/pssm/rules/";

	String rulesFile = "rule_0.xml";

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() {
		try {
			srule = RuleParser.read(rulesPath + rulesFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() {

	}

	/**
	 * 
	 * 
	 */
	public void testCreateLogoPanel() {

		JDialog pssmPanel = new JDialog();

		// the main logic
		JScrollPane mainScrollPane = new JScrollPane();

		mainScrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		mainScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
				"left:default", // columns
				""));// rows added dynamically

		PSAMPlot psamPlot = new PSAMPlot(PsamUtil.convertScoresToWeights(srule
				.getPssm(), true));
		psamPlot.setMaintainProportions(false);
		psamPlot.setAxisDensityScale(4);
		psamPlot.setAxisLabelScale(3);
		BufferedImage image = new BufferedImage(
				MedusaVisualComponent.IMAGE_WIDTH,
				MedusaVisualComponent.IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		psamPlot.layoutChart(MedusaVisualComponent.IMAGE_WIDTH,
				MedusaVisualComponent.IMAGE_HEIGHT, graphics
						.getFontRenderContext());
		psamPlot.paint(graphics);
		ImageIcon psamImage = new ImageIcon(image);

		/* add the image as a label */
		builder.append(new JLabel(psamImage));

		/* add the table */
		JTable pssmTable = PsamUtil.createPssmTable(srule.getPssm(),
				"Nucleotides");

		TableColumn column = null;
		for (int k = 0; k < 5; k++) {
			column = pssmTable.getColumnModel().getColumn(k);
			if (k > 0) {
				column.setPreferredWidth(COLUMN_WIDTH);
			}
		}
		pssmTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(pssmTable);
		scrollPane.setVerticalScrollBar(new JScrollBar());

		builder.append(scrollPane);

		JPanel transFacButtonPanel = new JPanel();
		List<JRadioButton> buttons = SwingUtil.createRadioButtonGroup("JASPAR",
				"Custom");
		for (JRadioButton b : buttons) {
			transFacButtonPanel.add(b);
		}

		JButton loadTransFacButton = SwingUtil
				.createButton("Load TF",
						"Load file containing new transcription factors to add to the TF listing.");
		transFacButtonPanel.add(loadTransFacButton);
		builder.append(transFacButtonPanel);

		// add search results table

		JPanel pssmButtonPanel = new JPanel();
		JButton exportButton = SwingUtil.createButton("Export",
				"Export search results to file in PSSM file format.");
		pssmButtonPanel.add(exportButton);

		JButton searchButton = SwingUtil.createButton("Search",
				"Executes a database search.");
		pssmButtonPanel.add(searchButton);

		builder.append(pssmButtonPanel);

		mainScrollPane.setViewportView(builder.getPanel());

		pssmPanel.add(mainScrollPane);

		// end the main logic

		pssmPanel.pack();
		pssmPanel.setModal(true);
		pssmPanel.setVisible(true);
	}
}
