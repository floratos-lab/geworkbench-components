package org.geworkbench.components.colormosaic;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.ColorScale;

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
public class ColorMosaicPanel implements Printable, VisualPlugin, MenuListener {
	private static Log log = LogFactory.getLog(ColorMosaicPanel.class);
	
    private JPanel mainPanel = new JPanel();
    private JToolBar jToolBar1 = new JToolBar();
    private JButton printBtn = new JButton();
    private JButton copyBtn = new JButton();

    private JScrollPane jScrollPane = new JScrollPane();
    private ColorMosaicImage colorMosaicImage = new ColorMosaicImage();
    private JPanel jPanel1 = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JSpinner jGeneWidthSlider = new JSpinner(new SpinnerNumberModel(20,1,100,1));
    private JSpinner jGeneHeightSlider = new JSpinner(new SpinnerNumberModel(10,1,100,1));
    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private JSlider jIntensitySlider = new JSlider();
    private JLabel jLabel5 = new JLabel();
    private JButton exportButton = new JButton("Export Data");
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
    private BorderLayout borderLayout2 = new BorderLayout();
    private CMHRuler colRuler = new CMHRuler(colorMosaicImage);
    private CMVRuler rowRuler = new CMVRuler(colorMosaicImage);
    private JCheckBox jAllMArrays = new JCheckBox();
    private JCheckBox jAllMarkers = new JCheckBox();
    private HashMap<String, ActionListener> listeners = new HashMap<String, ActionListener>();
    private JPopupMenu jCMMenu = new JPopupMenu();
	private JMenuItem jZoomInItem = new JMenuItem();
	private JMenuItem jZoomOutItem = new JMenuItem();
	private JMenuItem jPrintItem = new JMenuItem();
	private JMenuItem jSnapshotItem = new JMenuItem();
	private JMenuItem jExportItem = new JMenuItem();
	
    private boolean significanceMode = false;
    private DSSignificanceResultSet<DSGeneMarker> significance = null;
    private ArrayList<DSGeneMarker> sortedMarkers = new ArrayList<DSGeneMarker>();
    private ArrayList<DSGeneMarker> unsortedMarkers = new ArrayList<DSGeneMarker>();
    private DSSignificanceResultSet<DSGeneMarker> sigSet = null;
    private List<DSGeneMarker> markerSet = null;
    private boolean showSignal = false;
    
    private static final int GENE_HEIGHT = 10;
    private static final int GENE_WIDTH = 20;
	private static final double displayXFactor = 2/3.0;
    
    private enum searchBy {ARRAYNAME, ACCESSION, LABEL};

    public ColorMosaicPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setChips(DSMicroarraySet chips) {
        if (chips != null) {
            colorMosaicImage.setChips(chips);
            mainPanel.repaint();
        }
    }

    private void setSignificance(DSSignificanceResultSet<DSGeneMarker> sigSet) {
        colorMosaicImage.setSignificanceResultSet(sigSet);
    }

    private ColorScale colorScale = new ColorScale(Color.gray, Color.gray, Color.gray);
    
    private void jbInit() throws Exception {
        mainPanel.setBackground(Color.WHITE);
        colRuler.setPreferredWidth(1000);
        rowRuler.setPreferredSize(new Dimension(20, 1000));
        printBtn.setMaximumSize(new Dimension(26, 26));
        printBtn.setMinimumSize(new Dimension(26, 26));
        printBtn.setPreferredSize(new Dimension(26, 26));
        printBtn.setIcon(new ImageIcon(ColorMosaicPanel.class.getResource("print.gif")));
        printBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printBtn_actionPerformed(e);
            }
        });
        mainPanel.setMinimumSize(new Dimension(200, 300));
        mainPanel.setPreferredSize(new Dimension(438, 300));
        mainPanel.setLayout(borderLayout2);
        copyBtn.setMaximumSize(new Dimension(26, 26));
        copyBtn.setMinimumSize(new Dimension(26, 26));
        copyBtn.setPreferredSize(new Dimension(26, 26));
        copyBtn.setIcon(new ImageIcon(ColorMosaicPanel.class.getResource("copy.gif")));

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

        jPanel1.setLayout(gridBagLayout1);
        jLabel1.setText("Gene Height");
        jLabel2.setText("Gene Width");

        colorMosaicImage.setGeneHeight(10);
        colorMosaicImage.setParent(this);
        jGeneHeightSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jGeneHeightSlider_stateChanged(e);
            }
        });

        colorMosaicImage.setGeneWidth(20);
        jGeneWidthSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jGeneWidthSlider_stateChanged(e);
            }
        });
        jIntensitySlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jIntensitySlider_stateChanged(e);
            }
        });
        jIntensitySlider.setPaintTicks(true);
        jIntensitySlider.setValue(100);
        jIntensitySlider.setMinorTickSpacing(2);
        jIntensitySlider.setMinimum(1);
        jIntensitySlider.setMaximum(200);
        jIntensitySlider.setMajorTickSpacing(50);
        jLabel5.setText("Intensity");

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
                jHideMaskedBtn_actionPerformed();
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
		searchArray.getDocument().addDocumentListener(new DocListener(searchBy.ARRAYNAME));
		searchArray.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				searchText(e, searchBy.ARRAYNAME);
			}
		});

		searchAccessionLbl.setForeground(Color.gray);
		searchAccession.setEnabled(false);
		searchAccession.getDocument().addDocumentListener(new DocListener(searchBy.ACCESSION));
		searchAccession.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				searchText(e, searchBy.ACCESSION);
			}
		});

		searchLabel.getDocument().addDocumentListener(new DocListener(searchBy.LABEL));
		searchLabel.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				searchText(e, searchBy.LABEL);
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
		jToggleSortButton.setToolTipText("Sort by fold changes and t-values");
		jToggleSortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jToggleSortButton_actionPerformed(e);
            }
        });
        if(!significanceMode) jToggleSortButton.setEnabled(false);
        
        jScrollPane.getViewport().setBackground(Color.white);
        jAllMArrays.setSelected(false);
        jAllMArrays.setText("All arrays");
        jAllMArrays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jAllMArrays_actionPerformed(e);
            }
        });
        
		exportButton.setMargin(new Insets(2, 3, 2, 3));
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (significance != null) {
                	significance.saveDataToCSVFile();
				}else
        			JOptionPane.showMessageDialog(null, "No significance data to export. Please do this after you got significance data.",
        					"Operation failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        exportButton.setEnabled(false);
        jExportItem.setEnabled(false);
        
        jAllMarkers.setSelected(false);
        jAllMarkers.setText("All Markers");
        jAllMarkers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jAllMarkers_actionPerformed(e);
            }
        });
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
        mainPanel.add(jScrollPane, BorderLayout.CENTER);
        JToolBar jToolBar2 = new JToolBar();
        jToolBar2.add(exportButton, null);
        jToolBar2.addSeparator();
        jToolBar2.add(jAllMArrays, null);
        jToolBar2.add(jAllMarkers, null);
        jToolBar2.addSeparator();
        jToolBar2.add(jLabel5, null);
        jToolBar2.add(jIntensitySlider);
        jToolBar2.add(colorScale, null);
        jToolBar2.add(jLabel1, null);
        jToolBar2.add(jGeneHeightSlider, null);
        jToolBar2.addSeparator();
        jToolBar2.add(jLabel2, null);
        jToolBar2.add(jGeneWidthSlider, null);
        mainPanel.add(jToolBar2, BorderLayout.SOUTH);
        jScrollPane.getViewport().add(colorMosaicImage, null);
        jScrollPane.setColumnHeaderView(colRuler);
        jScrollPane.setRowHeaderView(rowRuler);
        jScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JComponent() {
			private static final long serialVersionUID = 9052547911998188349L;
			public void paint(Graphics g) {
                setBackground(Color.WHITE);
                Rectangle clip = g.getClipBounds();
                g.clearRect(clip.x, clip.y, clip.width, clip.height);
            }
        });
        colorMosaicImage.setPrintRatio(jTogglePrintRatio.isSelected());
        colorMosaicImage.setPrintAccession(jTogglePrintAccession.isSelected());
        colorMosaicImage.setPrintDescription(jTogglePrintDescription.isSelected());
    	
    	colorMosaicImage.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON3)
					jCMMenu.show(colorMosaicImage, e.getX(), e.getY());
			}
		});
    	jPrintItem.setText("Print...");
    	ActionListener listenerPrint = new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printBtn_actionPerformed(e);
			}
		};
		jPrintItem.addActionListener(listenerPrint);
    	jCMMenu.add(jPrintItem);

    	jCMMenu.addSeparator();
    	
    	jZoomInItem.setText("Zoom In");
    	ActionListener listenerZoomIn = new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int h = ((Integer) jGeneHeightSlider.getValue()).intValue();
				int w = ((Integer) jGeneWidthSlider.getValue()).intValue();
				jGeneHeightSlider.setValue(h+1);
				jGeneWidthSlider.setValue(w+1);
			}
		};
		jZoomInItem.addActionListener(listenerZoomIn);
    	jCMMenu.add(jZoomInItem);

    	jZoomOutItem.setText("Zoom Out");
    	ActionListener listenerZoomOut = new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int h = ((Integer) jGeneHeightSlider.getValue()).intValue();
				int w = ((Integer) jGeneWidthSlider.getValue()).intValue();
				if (h>1)
					jGeneHeightSlider.setValue(h-1);
				if (w>1)
					jGeneWidthSlider.setValue(w-1);
			}
		};
		jZoomOutItem.addActionListener(listenerZoomOut);
    	jCMMenu.add(jZoomOutItem);

    	jCMMenu.addSeparator();
    	
    	jSnapshotItem.setText("Take Snapshot");
    	ActionListener listenerjSnapshotItem = new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createImageSnapshot();
			}
		};
		jSnapshotItem.addActionListener(listenerjSnapshotItem);
    	jCMMenu.add(jSnapshotItem);

    	jExportItem.setText("Export significance value");
    	ActionListener listenerExportItem = new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
                if (significance != null) {
                	significance.saveDataToCSVFile();
				}else
        			JOptionPane.showMessageDialog(null, "No significance data to export. Please do this after you got significance data.",
        					"Operation failed", JOptionPane.ERROR_MESSAGE);
			}
		};
		jExportItem.addActionListener(listenerExportItem);
    	jCMMenu.add(jExportItem);
    }

    private void revalidate() {
        if (colorMosaicImage.isDisplayable()) {
            mainPanel.revalidate();
            if (rowRuler != null) {
                rowRuler.revalidate();
                rowRuler.repaint();
            }
            if (colRuler != null) {
                colRuler.revalidate();
                colRuler.repaint();
            }
        }
    }

    private void resetColorMosaicImage(int geneHeight, int geneWidth,
                                       boolean printRatio, boolean printAccession, boolean printDesc) {
        colorMosaicImage.setGeneHeight(geneHeight);
        colorMosaicImage.setGeneWidth(geneWidth);
        colorMosaicImage.setPrintRatio(printRatio);
        colorMosaicImage.setPrintAccession(printAccession);
        colorMosaicImage.setPrintRatio(printRatio);
        colorMosaicImage.setMarkerPanel(null);
        colorMosaicImage.setPanel(null);
        colorMosaicImage.setChips(null);
    }

    private void jGeneHeightSlider_stateChanged(ChangeEvent e) {
        int h = ((Integer) jGeneHeightSlider.getValue()).intValue();
        colorMosaicImage.setGeneHeight(h);
        revalidate();
    }

    private void jGeneWidthSlider_stateChanged(ChangeEvent e) {
        int w = ((Integer) jGeneWidthSlider.getValue()).intValue();
        colorMosaicImage.setGeneWidth(w);
        revalidate();
    }

    private void jIntensitySlider_stateChanged(ChangeEvent e) {
        double v = (double) jIntensitySlider.getValue() / 100.0;
        if (v > 1) {
            colorMosaicImage.setIntensity(1 + Math.exp(v) - Math.exp(1.0));
        } else {
            colorMosaicImage.setIntensity(v);
        }
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

    /* invoke from EDT only */
    private void jHideMaskedBtn_actionPerformed() {
        if (colorMosaicImage.isDisplayable()) {
            if (jHideMaskedBtn.isSelected()) {
            	if(significanceMode) {
            		refreshSignificanceResultView();
            		return;
            	} else {
            		displayMosaic();
            	}
            } else {
                clearMosaic();
            } 
            if(jAllMarkers.isSelected() || ((colorMosaicImage.getPanel() != null) && (colorMosaicImage.getPanel().size() > 0))){        
            	colorMosaicImage.showAllMarkers(jAllMarkers.isSelected());
            }
            if(jAllMArrays.isSelected() || ((colorMosaicImage.getMArrayPanel() != null) && (colorMosaicImage.getMArrayPanel().size() > 0))){
            	colorMosaicImage.showAllMArrays(jAllMArrays.isSelected());
            	
            }
            if(!jAllMarkers.isSelected() && ((colorMosaicImage.getPanel() == null) || (colorMosaicImage.getPanel().size() <= 0))){
            	colorMosaicImage.showAllMarkers(true);
            }            
            if(!jAllMArrays.isSelected() && ((colorMosaicImage.getMArrayPanel() == null) || (colorMosaicImage.getMArrayPanel().size() <= 0))){
            	colorMosaicImage.showAllMArrays(true);
            }
            revalidate();
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

    /* Enter: search forward starting from next item
     * Ctl-B: search backwards from next item
     */
    private void searchText(KeyEvent e, searchBy type) {
        char c = e.getKeyChar();
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            findNext(1, c, type);
        } else if (e.isControlDown() && (c == '\u0002')) {
            findNext(-1, c, type);
        }
    }
    
    private void findNext(int offset, char c, searchBy type) {
        int index = 0;
        String markerString = "";
        if (type == searchBy.ACCESSION) {
        	index = colorMosaicImage.getSelectedAccession();
        	markerString = searchAccession.getText().toLowerCase();
        } else if (type == searchBy.LABEL) {
        	index = colorMosaicImage.getSelectedLabel();
        	markerString = searchLabel.getText().toLowerCase();
        } else if (type == searchBy.ARRAYNAME) {
        	index = colorMosaicImage.getSelectedArray();
        	markerString = searchArray.getText().toLowerCase();
        }
        if (index < 0)  index = 0;
        if (Character.isLetterOrDigit(c))  markerString += Character.toLowerCase(c);
    	int chipNo = colorMosaicImage.getChipNo();
        boolean found = false;
        if(type == searchBy.ARRAYNAME) {
        	for (int idx = Math.abs(offset); idx <= chipNo; idx++) {
				int j = 0;
				if (offset < 0)  j = (index + chipNo - idx) % chipNo;
				else  j = (index + idx) % chipNo;
				DSMicroarray pl = colorMosaicImage.getPhenoLabel(j);
				if (pl instanceof DSMicroarray) {
					DSMicroarray mArray = (DSMicroarray) pl;
					String name = mArray.toString().toLowerCase();
					if (name.indexOf(markerString) >= 0) {
						colorMosaicImage.setSelectedArray(j, markerString);
						found = true;//searchArray.setForeground(Color.black);
						int xstart = (j - colorMosaicImage.maxDisplayArray / 2) * colorMosaicImage.geneWidth;
						if (xstart < 0)  xstart = 0;
						else if (xstart > colorMosaicImage.getVWidth())  xstart = colorMosaicImage.getVWidth();
						jScrollPane.getViewport().setViewPosition(new Point(xstart, 0));
						break;
					}
				}
			}
			if (!found || markerString.length()==0) {
		        if (!found)
		        	//searchArray.setForeground(Color.red);
					JOptionPane.showMessageDialog(null, "No match found for search term: "+markerString, "Warning", JOptionPane.WARNING_MESSAGE);   
				colorMosaicImage.setSelectedArray(-1, null);
				jScrollPane.getViewport().setViewPosition(new Point(0, 0));
			}
			colRuler.repaint();
		} else {
			if(markerSet==null)return;
			
			List<DSGeneMarker> markers = markerSet;
			DSPanel<DSGeneMarker> mp = colorMosaicImage.getPanel();
			if (mp!=null && mp.size()>0 && !jAllMarkers.isSelected()) markers = mp;

			int markerNo = markers.size();
			if (colorMosaicImage.significanceResultSet != null)
				markerNo = colorMosaicImage.markerList.size();
			found = false;
			int xstart = colorMosaicImage.getVWidth();
			if (xstart < 0) xstart = 0;
			if (xstart > 0){
				int cstartmax = chipNo - (int)(colorMosaicImage.maxDisplayArray * displayXFactor);
				if (cstartmax < 0) cstartmax = 0;
				int xstartmax = cstartmax * colorMosaicImage.geneWidth;
				if (xstart > xstartmax) xstart = xstartmax;	
			}
			for (int idx = Math.abs(offset); idx <= markerNo; idx++) {
				int i = 0;
				if (offset < 0)  i = (index + markerNo - idx) % markerNo;
				else  i = (index + idx) % markerNo;
				DSGeneMarker marker = markers.get(i);
				if (colorMosaicImage.significanceResultSet != null)
					marker = colorMosaicImage.markerList.get(i);
				String name = "";
				if (type == searchBy.ACCESSION)   name = marker.getLabel().toLowerCase();
				else if (type == searchBy.LABEL)  name = marker.getShortName().toLowerCase();
				if (name.indexOf(markerString) >= 0) {
					if (type == searchBy.ACCESSION)   colorMosaicImage.setSelectedAccession(i, markerString);
					else if (type == searchBy.LABEL)  colorMosaicImage.setSelectedLabel(i, markerString);
					found = true;
					int ystart = (i-colorMosaicImage.maxDisplayMarker/2)*colorMosaicImage.geneHeight;
					if (ystart < 0)  ystart = 0;
					else if (ystart > colorMosaicImage.getVHeight())  ystart = colorMosaicImage.getVHeight();
					jScrollPane.getViewport().setViewPosition(new Point(xstart, ystart));
					break;
				}
			}
			if (!found || markerString.length()==0) {
		        if (!found)
					JOptionPane.showMessageDialog(null, "No match found for search term: "+markerString, "Warning", JOptionPane.WARNING_MESSAGE);   
				if (type == searchBy.ACCESSION)   colorMosaicImage.setSelectedAccession(-1, null);
				else if (type == searchBy.LABEL)  colorMosaicImage.setSelectedLabel(-1, null);	
				jScrollPane.getViewport().setViewPosition(new Point(xstart, 0));
			}
			colorMosaicImage.repaint();
		}
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
	
    private void clearMosaic() {
        colorMosaicImage.clearPatterns();
        colRuler.setClearDisplay(true);
    }

    private void displayMosaic() {
        colorMosaicImage.clearPatterns();
        DSMicroarraySet mArraySet = colorMosaicImage.getGeneChips();
        if (mArraySet != null) {
            markerSet = mArraySet.getMarkers();
            int markerNo = markerSet.size();
            CSMatrixPattern thePattern = new CSMatrixPattern();
            thePattern.init(markerNo);
            for (int i = 0; i < markerNo; i++) {
            	thePattern.markers()[i] = markerSet.get(i);
            }
            colorMosaicImage.addPattern(thePattern);
            
            org.geworkbench.bison.util.colorcontext.ColorContext colorContext = (org.geworkbench.bison.util.colorcontext.ColorContext) mArraySet
			.getObject(org.geworkbench.bison.util.colorcontext.ColorContext.class);
            colorScale.setMinColor(colorContext
					.getMinColorValue(jIntensitySlider.getValue()));
            colorScale.setCenterColor(colorContext
					.getMiddleColorValue(jIntensitySlider.getValue()));
            colorScale.setMaxColor(colorContext
					.getMaxColorValue(jIntensitySlider.getValue()));
            colorScale.repaint();
            colorMosaicImage.revalidate();
            colRuler.setClearDisplay(false);
        }
    }

    private void printBtn_actionPerformed(ActionEvent e) {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);
        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (Exception PrinterExeption) {
                System.out.println("Exception: " + PrinterExeption);
            }
        }

    }
    
    @Override
    public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
        int[] res = {600, 600, 3};
        Graphics2D g2 = (Graphics2D) g;
        double scaleX = (double) res[0] / 72.0;
        double scaleY = (double) res[0] / 72.0;
        g2.scale(1.0 / scaleX, 1.0 / scaleY);
        g2.setStroke(new BasicStroke(1.0F));
        if (pi >= 1) {
            return Printable.NO_SUCH_PAGE;
        }
        double inches = 0.0;
        rowRuler.computeWidth(scaleY);

        int rowSpace = (int) rowRuler.getScaledWidth();
        int colSpace = (int) ((double) colRuler.getHeight() * scaleX);
        int r = 72;
        double d = pf.getImageableWidth();
        inches = d / (double) r - (double) rowSpace / (double) res[0];

        g2.translate(pf.getImageableX() * scaleX, pf.getImageableY() * scaleY);
        colorMosaicImage.setAutoWidth(inches, res[0]);
        System.out.print("Painting ... ");
        g2.translate(rowSpace, 0);
        colRuler.paint(g2, res[0]);
        g2.translate(-rowSpace, colSpace);
        rowRuler.paint(g2, res[0]);
        g2.translate(rowSpace, 0);
        colorMosaicImage.paint(g2, res[0], false);
        System.out.println("Done");
        return Printable.PAGE_EXISTS;
    }

    public String getComponentName() {
        return "Color Mosaic";
    }

    private void setMarkerPanel(DSPanel<DSGeneMarker> panel) {
        if (panel != null) {
            colorMosaicImage.setMarkerPanel(panel);
            if (colorMosaicImage.isDisplayable()) {
                revalidate();
                colorMosaicImage.repaint();
            }
        }
    }

    private void setMicroarrayPanel(DSPanel<DSMicroarray> panel) {
        if (panel != null) {
            colorMosaicImage.setPanel(panel);
            if (colorMosaicImage.isDisplayable()) {
                revalidate();
                colorMosaicImage.repaint();
            }
        }
    }

    private void jAllMArrays_actionPerformed(ActionEvent e) {        
        if(!jAllMArrays.isSelected() && ((colorMosaicImage.getMArrayPanel() == null) || (colorMosaicImage.getMArrayPanel().size() <= 0))){
        	colorMosaicImage.showAllMArrays(true);
        } else {
        	colorMosaicImage.showAllMArrays(jAllMArrays.isSelected());
        }
        if(!jAllMarkers.isSelected() && ((colorMosaicImage.getPanel() == null) || (colorMosaicImage.getPanel().size() <= 0))){
        	colorMosaicImage.showAllMarkers(true);
        } else {
        	colorMosaicImage.showAllMarkers(jAllMarkers.isSelected());
        }
        colRuler.revalidate();
        mainPanel.repaint();
    }

    private void jAllMarkers_actionPerformed(ActionEvent e) {        
        if(!jAllMarkers.isSelected() && ((colorMosaicImage.getPanel() == null) || (colorMosaicImage.getPanel().size() <= 0))){
        	colorMosaicImage.showAllMarkers(true);
        } else {
        	colorMosaicImage.showAllMarkers(jAllMarkers.isSelected());
        }
        if(!jAllMArrays.isSelected() && ((colorMosaicImage.getMArrayPanel() == null) || (colorMosaicImage.getMArrayPanel().size() <= 0))){
        	colorMosaicImage.showAllMArrays(true);
        } else {
        	colorMosaicImage.showAllMArrays(jAllMArrays.isSelected());
        }
        rowRuler.revalidate();
        mainPanel.repaint();
    }

    public Component getComponent() {
        return mainPanel;
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

	// this should be called only from EDT
	private void refreshSignificanceResultView() {
		significance = sigSet;
		DSMicroarraySet set = sigSet.getParentDataSet();
		jToggleSortButton.setEnabled(true);
		exportButton.setEnabled(true);
		jExportItem.setEnabled(true);

		// by default color mosaic displays unsorted markers
		jToggleSortButton.setSelected(false);
		if (colorMosaicImage.getChips() != set) {
			colorMosaicImage.setChips(set);
		}
		// // Make panels from the significance data and display only that
		// Pheno
		CSPanel<DSMicroarray> phenoPanel = new CSPanel<DSMicroarray>(
				"Phenotypes");
		DSItemList<DSPanel<DSMicroarray>> list = phenoPanel.panels();
		list.clear();
		DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager
				.getInstance().getCurrentContext(set);
		for (int i = 0; i < 2; i++) {
			String[] labels = sigSet.getLabels(i);
			for (int j = 0; j < labels.length; j++) {
				DSPanel<DSMicroarray> p = new CSPanel<DSMicroarray>(
						context.getItemsWithLabel(labels[j]));
				if (p != null) {
					p.setActive(true);
					phenoPanel.panels().add(p);
				}
			}
		}

		setMicroarrayPanel(phenoPanel);
		// Markers
		CSPanel<DSGeneMarker> genePanel = new CSPanel<DSGeneMarker>("Markers");
		sigSet.getSignificantMarkers().setActive(true);
		genePanel.panels().add(sigSet.getSignificantMarkers());
		setMarkerPanel(genePanel);
		sortByTValue(genePanel);
		// Force all arrays and all markers off
		jAllMArrays.setSelected(false);
		colorMosaicImage.showAllMArrays(jAllMArrays.isSelected());
		jAllMarkers.setSelected(false);
		colorMosaicImage.showAllMarkers(jAllMarkers.isSelected());
		jHideMaskedBtn.setSelected(true);
		jAllMArrays.setEnabled(false);
		jAllMarkers.setEnabled(false);

		setSignificance(sigSet);
		displayMosaic();
		revalidate();
		mainPanel.repaint();
	}
    
    @SuppressWarnings("unchecked")
    @Subscribe public void receive(ProjectEvent projectEvent, Object source) {
        final DSDataSet<?> dataFile = projectEvent.getDataSet();
        significanceMode = false;
        jAllMArrays.setEnabled(true);
        jAllMarkers.setEnabled(true);
        if (dataFile != null) {
            if (dataFile instanceof DSMicroarraySet) {
                jToggleSortButton.setEnabled(false);
            	exportButton.setEnabled(false);
            	jExportItem.setEnabled(false);
                DSMicroarraySet set = (DSMicroarraySet) dataFile;
                if (colorMosaicImage.getChips() != set) {
                    colorMosaicImage.microarrayPanel = null;
                    colorMosaicImage.setMarkerPanel(null);
                    setChips(set);
                    colorMosaicImage.clearSignificanceResultSet();
                    colorMosaicImage.showAllMArrays(true); 
                    colorMosaicImage.showAllMarkers(true);
                    if (jHideMaskedBtn.isSelected())  jHideMaskedBtn_actionPerformed();
                } else{
                	colorMosaicImage.clearSignificanceResultSet();
                    colorMosaicImage.showAllMArrays(true); 
                    colorMosaicImage.showAllMarkers(true);
                    jAllMArrays.setEnabled(true);
                   jAllMarkers.setEnabled(true);
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
        		sigSet = (DSSignificanceResultSet<DSGeneMarker>) dataFile;
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

    @Override
	public ActionListener getActionListener(String key) {
		return (ActionListener) listeners.get(key);
	}

    private PhenotypeSelectorEvent<DSMicroarray> pse = null;
    @Subscribe public void receive(PhenotypeSelectorEvent<DSMicroarray> e, Object source) {
    	pse = e;
        if (significanceMode || e == null) {
            return;
        }
        if (jAllMArrays.isEnabled()==false)
        	colorMosaicImage.microarrayPanel = null;
        if (jAllMarkers.isEnabled()==false)
        	setMarkerPanel(null);
        DSPanel<DSMicroarray> pl = e.getTaggedItemSetTree();
        setMicroarrayPanel(pl);    
        jHideMaskedBtn_actionPerformed(); 
    }

    private GeneSelectorEvent gse = null;
    @Subscribe public void receive(GeneSelectorEvent e, Object source) {
    	gse = e;
        if (significanceMode || e == null) {
            return;
        }
        DSPanel<DSGeneMarker> panel = e.getPanel();
        if (panel != null) {
			colorMosaicImage.pValueWidth = 18;
			colorMosaicImage.isPrintPValue = true;
			setMarkerPanel(panel);
			if(SwingUtilities.isEventDispatchThread()) {
				jHideMaskedBtn_actionPerformed();
			} else {
            	log.debug("non-EDT");
				SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							jHideMaskedBtn_actionPerformed();
						}
						
					});
			}
        } 
    }   
    
    @Publish public MarkerSelectedEvent publishMarkerSelectedEvent
            (MarkerSelectedEvent
                    event) {
        return event;
    }
    
    @Publish public PhenotypeSelectedEvent publishPhenotypeSelectedEvent
    		(PhenotypeSelectedEvent
    				event) {
    	return event;
    }

    private Image getColorMosaicAsImage
            () {
        Dimension mainDim = colorMosaicImage.getPreferredSize();
        Dimension topDim = colRuler.getPreferredSize();
        Dimension leftDim = rowRuler.getPreferredSize();
        Dimension dim = new Dimension(mainDim.width + leftDim.width, mainDim.height + topDim.height);
        int w = dim.width;
        int h = dim.height;
		long size = w*h;
		final long MAX_SIZE = 100*1024*1024;
		if(size > MAX_SIZE) {
			JOptionPane.showMessageDialog(this.getComponent(),
			"Cannot create snapshot.\n"+ 
			"The requested snapshot is "+w+"X"+h+" pixels, or about "+size/1000000+" megapixels.\n"+ 
			"The upper limit is 100 megapixels.");
			return null;
		}
        BufferedImage image;
		try {
	        image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
		} catch (OutOfMemoryError e) {
			JOptionPane.showMessageDialog(this.getComponent(),
					"OutOfMemoryError when the image's size is "+w+"X"+h);
			return null;
		}

        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, dim.width, dim.height);
        AffineTransform start = g.getTransform();
        // Draw the main image
        g.translate(leftDim.width, topDim.height);
        colorMosaicImage.paint(g, ColorMosaicImage.DEFAULTRES, false);
        // Draw the column ruler
        g.setTransform(start);
        g.translate(leftDim.width, 0);
        // g.setClip(leftDim.width, 0, mainDim.width, topDim.height);
        colRuler.paint(g);
        // Draw the row ruler
        g.setTransform(start);
        g.translate(0, topDim.height);

        rowRuler.paint(g);
        return image;
    }

    public void createImageSnapshot() {
        Image currentImage = getColorMosaicAsImage();
        if(currentImage==null)return;
        
        ImageIcon newIcon = new ImageIcon(currentImage, "Color Mosaic View");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("Color Mosaic View", newIcon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
        publishImageSnapshotEvent(event);

    }

    @Publish
    public org.geworkbench.events.ImageSnapshotEvent publishImageSnapshotEvent
            (org.geworkbench.events.ImageSnapshotEvent
                    event) {
        return event;
    }
    
    private static class TValueComparator implements Comparator<DSGeneMarker> {
    	DSSignificanceResultSet<DSGeneMarker> significantResultSet;
    	
    	public TValueComparator(DSSignificanceResultSet<DSGeneMarker> significantResultSet){
    		this.significantResultSet = significantResultSet;
    	}

        public int compare(DSGeneMarker x, DSGeneMarker y) {        	
        	double tX = significantResultSet.getTValue(x);
            double tY = significantResultSet.getTValue(y);
            if (tX > tY) {
                return 1;
            } else if (tX < tY) {
                return -1;
            } else {
                return 0;
            }
        }
  
    }
    
    private void sortByTValue(DSItemList<DSGeneMarker> origMarkers){
    	if(significanceMode){
    		// create two marker lists: unsorted vs. sorted
    		unsortedMarkers.clear();
    		sortedMarkers.clear();
            for(DSGeneMarker m: origMarkers){
            	unsortedMarkers.add(m);
            	sortedMarkers.add(m);
            }
            
            // sort first by fold changes
            Collections.sort(sortedMarkers, new FoldChangesComparator(sigSet));        
            
            // break down the list of markers into positive and negative fold changes
            ArrayList<DSGeneMarker> positiveFolds = new ArrayList<DSGeneMarker>();   
            ArrayList<DSGeneMarker> negativeFolds = new ArrayList<DSGeneMarker>();
            for(DSGeneMarker m: sortedMarkers){
            	if(sigSet.getTValue(m) >= 0) positiveFolds.add(m);
				if(sigSet.getTValue(m) < 0) negativeFolds.add(m);
            }
            
            // sort each list by t-value
            Collections.sort(positiveFolds, new TValueComparator(sigSet));
            Collections.sort(negativeFolds, new TValueComparator(sigSet));
            
            // recombine lists
            sortedMarkers = positiveFolds;
            for(DSGeneMarker m: negativeFolds) positiveFolds.add(m);   
    	}
    }
    
    private void printTValueAndPValue(DSPanel<DSGeneMarker> markersInUse, boolean sorted){
    	StringBuilder sb = new StringBuilder();  	
    	
    	for(DSGeneMarker m: markersInUse){
    		sb.append(m.getShortName());
    		sb.append("\t");
    		sb.append(sigSet.getTValue(m));
    		sb.append("\t\t");
    		sb.append(sigSet.getSignificance(m));		
    		sb.append("\n");
    	}    	
    	sb.trimToSize();
    	
    	StringBuilder headerSB = new StringBuilder();    	
    	
    	if(sorted)
    		headerSB.append("Sorted markers:\n");
    	else
    		headerSB.append("Unsorted markers:\n");    	
    	
    	headerSB.append("marker\t\tt-value\t\tp-value\n");
    	headerSB.trimToSize();
    	
    	log.debug(headerSB.toString() + sb.toString());
    }
    
    private static class FoldChangesComparator implements Comparator<DSGeneMarker> {
    	DSSignificanceResultSet<DSGeneMarker> significantResultSet;
    	
    	public FoldChangesComparator(DSSignificanceResultSet<DSGeneMarker> significantResultSet){
    		this.significantResultSet = significantResultSet;
    	}

    	public int compare(DSGeneMarker x, DSGeneMarker y) { 
            double foldX = significantResultSet.getFoldChange(x);
            double foldY = significantResultSet.getFoldChange(y);
            if (foldX > foldY) {
                return 1;
            } else if (foldX < foldY) {
                return -1;
            } else {
                return 0;
            }
        }
  
    }
    
    private class DocListener implements DocumentListener {
    	private searchBy searchType;
    	DocListener(searchBy type){
    		searchType = type;
    	}
        public void insertUpdate(DocumentEvent e) {
            findNext(0, '\u000E', searchType);
        }
        public void removeUpdate(DocumentEvent e) {
            findNext(0, '\u000E', searchType);
        }
        public void changedUpdate(DocumentEvent e) {
        }
    }
}
