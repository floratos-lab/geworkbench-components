package org.geworkbench.components.colormosaic;

 
import java.awt.BorderLayout;
import java.awt.Color; 
import java.awt.Dimension;
 
import java.awt.Insets;
import java.awt.Point; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;  
import javax.swing.ImageIcon;
import javax.swing.JButton; 
import javax.swing.JLabel; 
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
 
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.lincs.LincsDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet; 
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker; 
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet; 
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;  
import org.geworkbench.engine.management.AcceptTypes; 
import org.geworkbench.engine.management.Subscribe; 
import org.geworkbench.events.ProjectEvent;
 

/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version $Id$
 */

@AcceptTypes({DSMicroarraySet.class, DSSignificanceResultSet.class})
public class ColorMosaicPanel extends BasicColorMosaicPanel {
	private static Log log = LogFactory.getLog(ColorMosaicPanel.class);
   
    private JToolBar jToolBar1 = new JToolBar();
    private JButton printBtn = new JButton();
    
    private JToggleButton jToolTipToggleButton = new JToggleButton();
    private JTextField searchArray = new JTextField(10);
    private JTextField searchAccession = new JTextField(10);
    private JTextField searchLabel = new JTextField(10);
    private JLabel searchArrayLbl = new JLabel("Search Array");
    private JLabel searchAccessionLbl = new JLabel("Search Accession");
    private JLabel searchLabelLbl = new JLabel("Search Label");
    private JButton clearButton = new JButton("Clear Search");
    private JToggleButton jTogglePrintDescription = new JToggleButton("Label", true);
    private JToggleButton jTogglePrintRatio = new JToggleButton("Ratio", true);
    private JToggleButton jTogglePrintAccession = new JToggleButton("Accession", false);

    private JToggleButton jHideMaskedBtn = new JToggleButton("Display");
    private JToggleButton jToggleArraynames = new JToggleButton("Array Names", false);
    private JToggleButton jToggleSortButton = new JToggleButton("Sort");     
    
    private boolean showSignal = false;   
    
    public ColorMosaicPanel() {
    	super();
    	try {
    		jbInit() ;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
 
 
    
    private void jbInit() throws Exception {
        
        printBtn.setMaximumSize(new Dimension(26, 26));
        printBtn.setMinimumSize(new Dimension(26, 26));
        printBtn.setPreferredSize(new Dimension(26, 26));
        printBtn.setIcon(new ImageIcon(ColorMosaicPanel.class.getResource("print.gif")));
        printBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printBtn_actionPerformed(e);
            }
        });
       
		jToolTipToggleButton.setMargin(new Insets(2, 3, 2, 3));
        jToolTipToggleButton.setToolTipText("Toggle signal");
        jToolTipToggleButton.setActionCommand("TOOL_TIP_TOGGLE");
        jToolTipToggleButton.setSelected(false);
        jToolTipToggleButton.setIcon(new ImageIcon(this.getClass().getResource("bulb_icon_grey.gif")));
        jToolTipToggleButton.setSelectedIcon(new ImageIcon(this.getClass().getResource("bulb_icon_gold.gif")));
        jToolTipToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToolTipToggleButton_actionPerformed(e);
            }
        });

       

        jTogglePrintDescription.setMargin(new Insets(2, 3, 2, 3));

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jTogglePrintDescription_actionPerformed(e);
            }
        };
        listeners.put("File.Print", listener);
        jTogglePrintDescription.addActionListener(listener);

        
        
        jTogglePrintRatio.setMaximumSize(new Dimension(50, 25));
        jTogglePrintRatio.setMinimumSize(new Dimension(50, 25));
        jTogglePrintRatio.setPreferredSize(new Dimension(50, 25));
        jTogglePrintRatio.setMargin(new Insets(2, 3, 2, 3));
        jTogglePrintRatio.setSelected(false);

        jTogglePrintRatio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                colorMosaicImage.setPrintRatio(jTogglePrintRatio.isSelected());
            }
        });

        jTogglePrintAccession.setMargin(new Insets(2, 3, 2, 3));

        jTogglePrintAccession.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jTogglePrintAccession_actionPerformed(e);
            }
        });

        jHideMaskedBtn.setMargin(new Insets(2, 3, 2, 3));
        jHideMaskedBtn.setHorizontalTextPosition(SwingConstants.CENTER);

        jHideMaskedBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                          
            	isDisplay = jHideMaskedBtn.isSelected();
            	if (colorMosaicImage.isDisplayable()) {
        			if (isDisplay) {
        				if (significanceMode) {
        					refreshSignificanceResultView();
        					return;
        				}
        		}	}
                
            	display_actionPerformed();                
            }
        });

		jToggleArraynames.setMargin(new Insets(2, 3, 2, 3));
        jToggleArraynames.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleArraynames_actionPerformed(e);
            }
        });

		searchArrayLbl.setForeground(Color.gray);
		searchArray.setEnabled(false);
		searchArray.getDocument().putProperty("owner", searchArray);
		searchArray.getDocument().addDocumentListener(new DocListener(searchBy.ARRAYNAME));
		searchArray.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				searchText(e, searchBy.ARRAYNAME, searchArray.getText().toLowerCase());
			}
		});
		searchAccession.getDocument().putProperty("owner", searchAccession);
		searchAccessionLbl.setForeground(Color.gray);
		searchAccession.setEnabled(false);
		searchAccession.getDocument().addDocumentListener(new DocListener(searchBy.ACCESSION));
		searchAccession.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				searchText(e, searchBy.ACCESSION, searchAccession.getText().toLowerCase());
			}
		});
		
		searchLabel.getDocument().putProperty("owner", searchLabel);
		searchLabel.getDocument().addDocumentListener(new DocListener(searchBy.LABEL));
		searchLabel.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				searchText(e, searchBy.LABEL, searchLabel.getText().toLowerCase());
			}
		});

		clearButton.setMargin(new Insets(2, 3, 2, 3));
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearButton_actionPerformed(e);
			}
		});

		jToggleSortButton.setMargin(new Insets(2, 3, 2, 3));
		jToggleSortButton.setHorizontalTextPosition(SwingConstants.CENTER);
		jToggleSortButton.setToolTipText("Group markers by sign of fold change, then sort the two groups by ascending t-value, positive group first. This leaves the most significant positive and and negative values adjacent in the center of the sorted list");
		jToggleSortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jToggleSortButton_actionPerformed(e);
            }
        });
        if(!significanceMode) jToggleSortButton.setEnabled(false);
        
     
        mainPanel.add(jToolBar1, BorderLayout.NORTH);
        jToolBar1.add(printBtn, null);
        jToolBar1.add(jHideMaskedBtn, null);
        jToolBar1.add(jToggleArraynames, null);
        jToolBar1.add(jTogglePrintAccession, null);        
        jToolBar1.add(jTogglePrintDescription, null);
        jToolBar1.add(jToggleSortButton, null);
        jToolBar1.add(jToolTipToggleButton, null);
        jToolBar1.addSeparator();
		jToolBar1.add(searchArrayLbl, null);
		jToolBar1.add(searchArray, null);
		jToolBar1.addSeparator();
		jToolBar1.add(searchAccessionLbl, null);
		jToolBar1.add(searchAccession, null);
		jToolBar1.addSeparator();
		jToolBar1.add(searchLabelLbl, null);
		jToolBar1.add(searchLabel, null);
		jToolBar1.addSeparator();
		jToolBar1.add(clearButton, null);        
     
        colorMosaicImage.setPrintRatio(jTogglePrintRatio.isSelected());
        colorMosaicImage.setPrintAccession(jTogglePrintAccession.isSelected());
        colorMosaicImage.setPrintDescription(jTogglePrintDescription.isSelected());
    	
      
    }
    
			
    private void jTogglePrintAccession_actionPerformed(ActionEvent e) {
		colorMosaicImage.setPrintAccession(jTogglePrintAccession.isSelected());
		if (jTogglePrintAccession.isSelected()) {
			searchAccessionLbl.setForeground(Color.black);
			searchAccession.setEnabled(true);
			colRuler.revalidate();
		} else {
			searchAccessionLbl.setForeground(Color.gray);
			searchAccession.setEnabled(false);
		}
	}

    private void jTogglePrintDescription_actionPerformed(ActionEvent e) {
        colorMosaicImage.setPrintDescription(jTogglePrintDescription.isSelected());
    	if (jTogglePrintDescription.isSelected()) {
			searchLabelLbl.setForeground(Color.black);
			searchLabel.setEnabled(true);
			colRuler.revalidate();
		} else {
			searchLabelLbl.setForeground(Color.gray);
			searchLabel.setEnabled(false);
		}
    }
  
    private void jToggleArraynames_actionPerformed(ActionEvent e) {
		if (jToggleArraynames.isSelected() &&  jHideMaskedBtn.isSelected()) {
			searchArrayLbl.setForeground(Color.black);
			searchArray.setEnabled(true);
			colRuler.setClearArraynames(false);
		} else {
			searchArrayLbl.setForeground(Color.gray);
			searchArray.setEnabled(false);
			colRuler.setClearArraynames(true);
		}
		colRuler.revalidate();
    	colRuler.repaint();
    }    
    
    private void jToggleSortButton_actionPerformed(ActionEvent e) {
    	if (colorMosaicImage.isDisplayable() && significanceMode) {
    		DSPanel<DSGeneMarker> mp = colorMosaicImage.getPanel();
    		int markerNo = 0;
    		mp.clear();
    		if (jToggleSortButton.isSelected()){      	
    			markerNo = sortedMarkers.size();
    			for (int i = 0; i < markerNo; i++) {          				
    				mp.add(i, sortedMarkers.get(i));    				
                }
    			printTValueAndPValue(mp, true);
    		} else {
    			markerNo = unsortedMarkers.size();
    			for (int i = 0; i < markerNo; i++) {   
    				mp.add(i, unsortedMarkers.get(i));  
                }    			
    			printTValueAndPValue(mp, false);
    		}    		
    		colorMosaicImage.setMarkerPanel(mp);
    		if(jHideMaskedBtn.isSelected()){
        		displayMosaic();
        		revalidate();        	        		
        	}
    	}     	
    }

	private void clearButton_actionPerformed(ActionEvent e) {
		searchArray.setText("");
		searchAccession.setText("");
		searchLabel.setText("");
		colorMosaicImage.setSelectedAccession(-1, null);
		colorMosaicImage.setSelectedLabel(-1, null);
		colorMosaicImage.setSelectedArray(-1, null);
		colorMosaicImage.repaint();
		jScrollPane.getViewport().setViewPosition(new Point(0, 0));
		colRuler.repaint();
	} 
	
    public String getComponentName() {
        return "Color Mosaic";
    }
 
    
    /**
     * Handles selection/deselections of the ToolTip toggle button
     *
     * @param e <code>ActionEvent</code> forwarded by the listener
     */
    private void jToolTipToggleButton_actionPerformed(ActionEvent e) {
        showSignal = jToolTipToggleButton.isSelected();
        colorMosaicImage.setSignal(showSignal);
    }	 
    
    @SuppressWarnings("unchecked")
    @Subscribe public void receive(ProjectEvent projectEvent, Object source) {
        final DSDataSet<?> dataFile = projectEvent.getDataSet();
        significanceMode = false;

       
        if (dataFile != null) {         	 
            if (dataFile instanceof DSMicroarraySet || dataFile instanceof LincsDataSet) {
                jToggleSortButton.setEnabled(false);
                disableExportItem();
                DSMicroarraySet set = null;
                if (dataFile instanceof LincsDataSet)
                	set = (DSMicroarraySet)((LincsDataSet)dataFile).getParentDataSet();
                else
                	set = (DSMicroarraySet) dataFile;
                if (colorMosaicImage.getChips() != set) {
                    colorMosaicImage.microarrayPanel = null;
                    colorMosaicImage.setMarkerPanel(null);
                    setChips(set);
                    colorMosaicImage.clearSignificanceResultSet();
                    colorMosaicImage.showAllMArrays(true); 
                    colorMosaicImage.showAllMarkers(true);
                    if (jHideMaskedBtn.isSelected())  display_actionPerformed();
                } else{
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
				//update marker and array selections after microarray set is handled
                receive(pse, null);
                receive(gse, null);             

            } else if (dataFile instanceof DSSignificanceResultSet) {          
                significanceMode = true;
                jHideMaskedBtn.setSelected(true);
                jToggleSortButton.setEnabled(true);
        		sigSet = (DSSignificanceResultSet<DSGeneMarker>) dataFile;
        		jToggleSortButton.setSelected(false);        	 
        		if(SwingUtilities.isEventDispatchThread()) {
                	refreshSignificanceResultView();
                } else {
                	log.debug("non-EDT");

                	SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								refreshSignificanceResultView();
							}
							
						});
                }
            }
        } else {
            jHideMaskedBtn.setSelected(false);
            resetColorMosaicImage(GENE_HEIGHT, GENE_WIDTH, jTogglePrintRatio.isSelected(),
                    jTogglePrintAccession.isSelected(), jTogglePrintDescription.isSelected());
            colorMosaicImage.repaint();
            mainPanel.repaint();
        }
    }
 
   
     
}
