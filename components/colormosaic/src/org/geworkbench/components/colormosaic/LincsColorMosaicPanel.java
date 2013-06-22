package org.geworkbench.components.colormosaic;

import java.awt.BorderLayout;
import java.awt.Color;  
import java.awt.Font; 
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints; 
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox; 
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
 

import javax.swing.JLabel;

import javax.swing.JPanel;

import javax.swing.JTextField;
import javax.swing.JToggleButton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.lincs.LincsDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

/**
 * <p>
 * Title: Plug And Play
 * </p>
 * <p>
 * Description: Dynamic Proxy Implementation of enGenious
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: First Genetic Trust Inc.
 * </p>
 * 
 * @author Manjunath Kustagi
 * @version $Id: ColorMosaicPanel.java 10033 2012-10-12 19:14:13Z wangmen $
 */

@AcceptTypes({ LincsDataSet.class })
public class LincsColorMosaicPanel extends BasicColorMosaicPanel {

	private static final long serialVersionUID = -2442357892019684993L;
	private static Log log = LogFactory.getLog(LincsColorMosaicPanel.class);

	private JPanel conditionPanel1;
	private JPanel conditionPanel2;
	private JPanel upperPanel;
	private JLabel showLabel = new JLabel("Show");
	private JLabel searchLable = new JLabel("Search");
    private JLabel assayTypeLabel = new JLabel("                    Assay Type");
    private JLabel measurementLabel = new JLabel("Synergy Measurement");
	private JLabel hAxisNamesLbl = new JLabel("   Horizontal Axis Names");
	private JLabel vAxisNamesLbl = new JLabel("       Vertical Axis Names");
	private JCheckBox hAxisNames;
	private JCheckBox vAxisNames;

	private JTextField searchHoriAxisNames = new JTextField(10);
	private JTextField searchVertAxisNames = new JTextField(10);
	private JButton clearButton = new JButton("Clear Search");
	private JToggleButton jToolTipToggleButton = new JToggleButton();
	
	private JTextField tissueTF = new JTextField();
	private JTextField celllineTF = new JTextField();
	private JTextField drug1TF = new JTextField();
	private JTextField drug2TF = new JTextField();
	private JTextField assayTypeTF = new JTextField();
	private JTextField measurementTF = new JTextField();
	
	
	
	private boolean showSignal = false;

	public LincsColorMosaicPanel() {
		super();
		isDisplay = true;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		
		conditionPanel1 = new JPanel();
		conditionPanel1.setBackground(Color.white);
		conditionPanel2 = new JPanel();
		conditionPanel2.setBackground(Color.white);
		 
		conditionPanel1.setLayout(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	   
		hAxisNames = new JCheckBox();			
		hAxisNames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hAxisNames_actionPerformed();
			}
		});
		 
		vAxisNames = new JCheckBox();	 
		vAxisNames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				vAxisNames_actionPerformed(e);
			}
		});
		 
		//jToolTipToggleButton.setMargin(new Insets(2, 3, 2, 3));
		jToolTipToggleButton.setBorderPainted(false);
		jToolTipToggleButton.setToolTipText("Toggle signal");
		jToolTipToggleButton.setActionCommand("TOOL_TIP_TOGGLE");
		jToolTipToggleButton.setSelected(false);
		jToolTipToggleButton.setIcon(new ImageIcon(this.getClass().getResource(
				"bulb_icon_grey.gif")));
		jToolTipToggleButton.setSelectedIcon(new ImageIcon(this.getClass()
				.getResource("bulb_icon_gold.gif")));
		jToolTipToggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jToolTipToggleButton_actionPerformed(e);
			}
		});
		jToolTipToggleButton.setBackground(Color.WHITE);
	    searchLable.setEnabled(false);
		searchHoriAxisNames.setText("search horizontal");
		final Font f = searchHoriAxisNames.getFont();
		searchHoriAxisNames.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
            	if (searchHoriAxisNames.getText().equals("search horizontal"))
            		searchHoriAxisNames.setText("");
            	searchHoriAxisNames.setFont(f);
            }
        });		 
		searchHoriAxisNames.setFont(new Font("Courier", Font.ITALIC, 12));
		
		searchVertAxisNames.setText("search vertical");		 
		searchVertAxisNames.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
            	if (searchVertAxisNames.getText().equals("search vertical"))
            		searchVertAxisNames.setText("");
            	searchVertAxisNames.setFont(f);
            }
        });	
	 
		searchVertAxisNames.setFont(new Font("Courier", Font.ITALIC, 12));


		searchHoriAxisNames.setEnabled(false);
		searchVertAxisNames.setEnabled(false);
		
		searchHoriAxisNames.getDocument().addDocumentListener(
				new DocListener(searchBy.ARRAYNAME));
		searchHoriAxisNames.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				searchText(e, searchBy.ARRAYNAME, searchHoriAxisNames.getText()
						.toLowerCase());
			}
		});

		searchVertAxisNames.getDocument().addDocumentListener(
				new DocListener(searchBy.LABEL));
		searchVertAxisNames.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				searchText(e, searchBy.LABEL, searchVertAxisNames.getText()
						.toLowerCase());
			}
		});
		 
	 
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearButton_actionPerformed(e);
			}
		});
		
		tissueTF.setEditable(false);
		celllineTF.setEditable(false);
		drug1TF.setEditable(false);
		drug2TF.setEditable(false);
		assayTypeTF.setEditable(false);
		measurementTF.setEditable(false);	 
		tissueTF.setFocusable(false);
		celllineTF.setFocusable(false);
		drug1TF.setFocusable(false);
		drug2TF.setFocusable(false);
		assayTypeTF.setFocusable(false);
		measurementTF.setFocusable(false);	 

		
		upperPanel = new JPanel();
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
		upperPanel.add(conditionPanel1);
		JSeparator Separator = new JSeparator(SwingConstants.HORIZONTAL);
		upperPanel.add(Separator);		 
		upperPanel.add(conditionPanel2);
		mainPanel.add(upperPanel, BorderLayout.NORTH);		
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;    
        c.gridx = 1;
        c.gridy = 0;		 
		conditionPanel1.add(new JLabel(""), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;    
        c.gridx = 3;
        c.gridy = 0;		 
		conditionPanel1.add(showLabel, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.3;
	    
        c.gridx = 4;
        c.gridy = 0;
		conditionPanel1.add(searchLable, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;	      
        c.gridx = 2;
        c.gridy = 1;		
		conditionPanel1.add(hAxisNamesLbl, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.2;    
        c.gridx = 3;
        c.gridy = 1;
		conditionPanel1.add(hAxisNames, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.3;	   
        c.gridx = 4;
        c.gridy = 1;
		conditionPanel1.add(searchHoriAxisNames, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.2;
	 
        c.gridx = 5;
        c.gridy = 1;
		conditionPanel1.add(clearButton, c);
		c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.8;			
	        c.gridx = 6;
	        c.gridy = 1;
			conditionPanel1.add(new JLabel("                              "), c);
		 
			c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.01;    
        c.gridx = 0;
        c.gridy = 2;
       
		conditionPanel1.add(jToolTipToggleButton, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.2;
	    
        c.gridx = 2;
        c.gridy = 2;	
		conditionPanel1.add(vAxisNamesLbl,c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;    
        c.gridx = 3;
        c.gridy = 2;
		conditionPanel1.add(vAxisNames, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.3;	     
        c.gridx = 4;
        c.gridy = 2;
		conditionPanel1.add(searchVertAxisNames, c);
		 
		
		conditionPanel2.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;    
        c.gridx = 0;
        c.gridy = 0;        
		conditionPanel2.add(new JLabel("   Tissue"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.8;    
        c.gridx = 1;
        c.gridy = 0; 
      
		conditionPanel2.add(tissueTF, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;    
        c.gridx = 2;
        c.gridy = 0;        
		conditionPanel2.add(new JLabel(""), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;    
        c.gridx = 3;
        c.gridy = 0;        
		conditionPanel2.add(new JLabel("Drug1"), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.8;    
        c.gridx = 4;
        c.gridy = 0;        
		conditionPanel2.add(drug1TF, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;    
        c.gridx = 5;
        c.gridy = 0;  
    	conditionPanel2.add(new JLabel("  "), c);
        c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;    
        c.gridx = 6;
        c.gridy = 0; 
		conditionPanel2.add(assayTypeLabel, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.8;    
        c.gridx = 7;
        c.gridy = 0;        
		conditionPanel2.add(assayTypeTF, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.8;    
        c.gridx = 8;
        c.gridy = 0;        
		conditionPanel2.add(new JLabel("                        "), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;    
        c.gridx = 0;
        c.gridy = 1;        
		conditionPanel2.add(new JLabel("Cell Line"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.8;    
        c.gridx = 1;
        c.gridy = 1;        
		conditionPanel2.add(celllineTF, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;    
        c.gridx = 3;
        c.gridy = 1;        
		conditionPanel2.add(new JLabel("Drug2"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.8;    
        c.gridx = 4;
        c.gridy = 1;        
		conditionPanel2.add(drug2TF, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;    
        c.gridx = 6;
        c.gridy = 1;        
		conditionPanel2.add(measurementLabel, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.8;    
        c.gridx = 7;
        c.gridy = 1;        
		conditionPanel2.add(measurementTF, c);

		
		colorMosaicImage.setPrintRatio(false);
	    colorMosaicImage.setPrintAccession(false);
	    colorMosaicImage.setPrintDescription(false);
	    	
	 
	}

	private void vAxisNames_actionPerformed(ActionEvent e) {
		colorMosaicImage.setPrintDescription(vAxisNames.isSelected());
		if (vAxisNames.isSelected()) {
			searchLable.setEnabled(true);
			searchVertAxisNames.setEnabled(true);
			colRuler.revalidate();
		} else {
			if (!hAxisNames.isSelected())
				searchLable.setEnabled(false);
			searchVertAxisNames.setEnabled(false);
		} 
		
	}

	private void hAxisNames_actionPerformed() {
		if (hAxisNames.isSelected()) {
			searchLable.setEnabled(true);
			searchHoriAxisNames.setEnabled(true);
			colRuler.setClearArraynames(false);
			
		} else {
			searchHoriAxisNames.setEnabled(false);
			if (!vAxisNames.isSelected())
				searchLable.setEnabled(false);
			colRuler.setClearArraynames(true);
		}
		colRuler.revalidate();
		colRuler.repaint();
	}

	private void clearButton_actionPerformed(ActionEvent e) {
		searchHoriAxisNames.setText("");	 
		searchVertAxisNames.setText("");
		colorMosaicImage.setSelectedAccession(-1, null);
		colorMosaicImage.setSelectedLabel(-1, null);
		colorMosaicImage.setSelectedArray(-1, null);
		colorMosaicImage.repaint();
		jScrollPane.getViewport().setViewPosition(new Point(0, 0));
		colRuler.repaint();
	}

	public String getComponentName() {
		return "Lincs Color Mosaic";
	}

	/**
	 * Handles selection/deselections of the ToolTip toggle button
	 * 
	 * @param e
	 *            <code>ActionEvent</code> forwarded by the listener
	 */
	private void jToolTipToggleButton_actionPerformed(ActionEvent e) {
		showSignal = jToolTipToggleButton.isSelected();
		if (showSignal)
			jToolTipToggleButton.setBackground(Color.gray);
		else
			jToolTipToggleButton.setBackground(Color.WHITE);
		colorMosaicImage.setSignal(showSignal);
	}

	@Subscribe
	public void receive(ProjectEvent projectEvent, Object source) {
		final DSDataSet<?> dataFile = projectEvent.getDataSet();	 
		if (dataFile != null) {
			if (dataFile instanceof LincsDataSet) {
				resetconditionPanel2((LincsDataSet)dataFile);
				levelTwoIds = ((LincsDataSet)dataFile).getLevelTwoIds();
				pValues = ((LincsDataSet) dataFile).getPValues();
				variableNames = ((LincsDataSet)dataFile).getfreeVariableNames();
				disableExportItem();
				DSMicroarraySet set = null;

				set = (DSMicroarraySet) ((LincsDataSet) dataFile)
						.getParentDataSet();
                
				if (colorMosaicImage.getChips() != set) {
					colorMosaicImage.microarrayPanel = null;
					colorMosaicImage.setMarkerPanel(null);
					setChips(set);
					colorMosaicImage.clearSignificanceResultSet();
					colorMosaicImage.showAllMArrays(true);
					colorMosaicImage.showAllMarkers(true);
					display_actionPerformed();
				} else {
					colorMosaicImage.clearSignificanceResultSet();
					colorMosaicImage.showAllMArrays(true);
					colorMosaicImage.showAllMarkers(true);
				}
				org.geworkbench.bison.util.colorcontext.ColorContext colorContext = (org.geworkbench.bison.util.colorcontext.ColorContext) set
						.getObject(org.geworkbench.bison.util.colorcontext.ColorContext.class);
				colorScale.setMinColor(colorContext
						.getMinColorValue(jIntensitySlider.getValue()));
				colorScale.setCenterColor(colorContext
						.getMiddleColorValue(jIntensitySlider.getValue()));
				colorScale.setMaxColor(colorContext
						.getMaxColorValue(jIntensitySlider.getValue()));
				 
			}
		} else {

			resetColorMosaicImage(GENE_HEIGHT, GENE_WIDTH, false, false,
					vAxisNames.isSelected());
			colorMosaicImage.repaint();
			mainPanel.repaint();
		}
	}
	
	private void resetconditionPanel2(LincsDataSet dataFile)
	{
		if (dataFile.isExperimental())
		{		
			assayTypeTF.setVisible(true);
			assayTypeLabel.setVisible(true);
			//assayTypeLabel.setText("                    Assay Type");
			measurementLabel.setText("Synergy Measurement");
		}
		else
		{
			assayTypeTF.setVisible(false);
			assayTypeLabel.setVisible(false);		 
			measurementLabel.setText("Similarity Algorithm");
		}
		this.celllineTF.setText(dataFile.getCellLine());
		this.tissueTF.setText(dataFile.getTissue());
		this.drug1TF.setText(dataFile.getDrug1());
		this.drug2TF.setText(dataFile.getDrug2());
		this.assayTypeTF.setText(dataFile.getAssayType());
		this.measurementTF.setText(dataFile.getMeasurement());
	}

}
