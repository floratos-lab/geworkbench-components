package org.geworkbench.components.microarrays;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.util.colorcontext.ColorContext;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SingleMicroarrayEvent;
import org.geworkbench.util.ColorScale;

/**
 * Microarray Viewer.
 * 
 * Copyright: Copyright (c) 2002
 * Company: First Genetic Trust
 * 
 * @author Andrea Califano
 * @version $Id$
 */

@AcceptTypes( { DSMicroarraySet.class })
public class MicroarrayPanel implements
		VisualPlugin, MenuListener {
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView;

    private int microarrayId = 0;
    private int markerId = 0;
    private JPanel mainPanel = new JPanel();

    @Publish public ImageSnapshotEvent publishImageSnapshotEvent(ImageSnapshotEvent event) {
        return event;
    }

    @Publish public MarkerSelectedEvent publishMarkerSelectedEvent(MarkerSelectedEvent event) {
        return event;
    }

    private final void changeMicroArraySet(DSMicroarraySet maSet) {
		mArraySet = maSet;
        dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(maSet);
		dataSetView.useMarkerPanel(true);
        dataSetView.useItemPanel(true);

		microarrayImageArea.setMicroarraySetView(dataSetView);
		
		if (maSet != null) {
			ColorContext colorContext = (ColorContext) maSet
					.getObject(ColorContext.class);
			if (colorContext != null) {
				valueGradient.setMinColor(colorContext
						.getMinColorValue(intensitySlider.getValue()));
				valueGradient.setCenterColor(colorContext
						.getMiddleColorValue(intensitySlider.getValue()));
				valueGradient.setMaxColor(colorContext
						.getMaxColorValue(intensitySlider.getValue()));
				valueGradient.repaint();
			}
			selectMicroarray(0);
		}
		
        resetMicroarraySlider();
    }

    private void showAllMArrays(boolean showAll) {
        dataSetView.useItemPanel(!showAll);
        resetMicroarraySlider();
        mainPanel.repaint();
    }

    private void showAllMarkers(boolean showAll) {
        dataSetView.useMarkerPanel(!showAll);
        mainPanel.repaint();
    }

    @Subscribe public void receive(org.geworkbench.events.ProjectEvent projectEvent, Object source) {

		if (projectEvent.getMessage().equals(ProjectEvent.CLEARED)) {
			changeMicroArraySet(null);
			mainPanel.repaint();
			return;
		}
		
		ProjectSelection selection = ((ProjectPanel) source).getSelection();
		DSDataSet<?> dataSet = selection.getDataSet();
		if (dataSet instanceof DSMicroarraySet) {
			DSMicroarraySet microarraySet = (DSMicroarraySet)dataSet;
			if(microarraySet!=mArraySet) {
				changeMicroArraySet(microarraySet);
			}
		} else {
			changeMicroArraySet(null);
		}
		mainPanel.repaint();
    }

    @Override
    public Component getComponent() {
        return mainPanel;
    }

    // Most of the code up to here used to in the original base MicroarrayVisualizer
    ////////////////////////////////////////////////////////////////////////////////////
    
	final private MicroarrayDisplay microarrayImageArea;

	private JSlider microarraySlider = new JSlider();
	private JSlider intensitySlider = new JSlider();
	private JTextField jMALabel = new JTextField(20);

	private ColorScale valueGradient = new ColorScale(
			Color.gray, Color.gray, Color.gray);

	private HashMap<String, ActionListener> listeners = new HashMap<String, ActionListener>();
	private DSMicroarraySet mArraySet = null;
	private boolean forcedSliderChange = false;

	public MicroarrayPanel() {
		dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(null);
		microarrayImageArea = new MicroarrayDisplay(dataSetView);
		dataSetView.useMarkerPanel(true);
        dataSetView.useItemPanel(true);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ActionListener getActionListener(String key) {
		return (ActionListener) listeners.get(key);
	}

	private void resetMicroarraySlider() {
		if (mArraySet != null) {
			microarraySlider.setMaximum(dataSetView.items().size() - 1);
		} else {
			microarraySlider.setMaximum(0);
			microarraySlider.setMinimum(0);
			microarraySlider.setValue(0);
		}
	}

	private void jbInit() throws Exception {
        mainPanel.setLayout(new BorderLayout());
        
		microarrayImageArea.setLayout(new BorderLayout());
		microarrayImageArea.setBorder(BorderFactory.createEtchedBorder());
		microarrayImageArea.setOpaque(false);

		microarrayImageArea.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				if (mArraySet != null) {
					int x = e.getX();
					int y = e.getY();
					markerId = microarrayImageArea.getGeneIdAndRubberBand(x, y);
				}
			}
		});
		final JPopupMenu jDisplayPanelPopup = new JPopupMenu();
		microarrayImageArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (mArraySet != null) {
					if (markerId != -1) {
						MarkerSelectedEvent mse = new MarkerSelectedEvent(mArraySet
								.getMarkers().get(markerId));
						publishMarkerSelectedEvent(mse);
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				microarrayImageArea.rubberBandBox(-1, -1);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isMetaDown() && markerId != -1) {
					jDisplayPanelPopup.show(mainPanel, e.getX(), e.getY());
				}
			}
		});
		microarraySlider.setValue(0);
		microarraySlider.setMaximum(0);
		microarraySlider.setMinimum(0);
		microarraySlider.setSnapToTicks(true);
		microarraySlider.setPaintTicks(true);
		microarraySlider.setMinorTickSpacing(1);
		microarraySlider.setMajorTickSpacing(5);
		microarraySlider.setCursor(java.awt.Cursor
				.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		microarraySlider.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				chipSlider_stateChanged(e);
			}
		});

		intensitySlider.setPaintTicks(true);
		intensitySlider.setValue(100);
		intensitySlider.setMinorTickSpacing(2);
		intensitySlider.setMinimum(1);
		intensitySlider.setMaximum(200);
		intensitySlider.setMajorTickSpacing(50);
		intensitySlider
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						intensitySlider_stateChanged(e);
					}
				});

		JMenuItem jShowMarkerMenu = new JMenuItem();
		jShowMarkerMenu.setText("Show Marker");
		jShowMarkerMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				microarrayImageArea.graphGene(markerId);
				microarrayImageArea.repaint();
			}
		});

		JMenuItem jRemoveMarkerMenu = new JMenuItem();
		jRemoveMarkerMenu.setText("Remove Marker");
		jRemoveMarkerMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				microarrayImageArea.ungraphGene(markerId);
				microarrayImageArea.repaint();
			}
		});

		ActionListener imageSnapshotListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Image currentImage = microarrayImageArea.getCurrentImage();
				if (currentImage != null && microarrayImageArea.microarray != null) {
					ImageIcon newIcon = new ImageIcon(currentImage,
							"Microarray Image: "
									+ microarrayImageArea.microarray.getLabel());
					org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
							"MicroarrayPanel ImageSnapshot", newIcon,
							ImageSnapshotEvent.Action.SAVE);
					publishImageSnapshotEvent(event);
				}
			}
		};

		listeners.put("File.Image Snapshot", imageSnapshotListener);
		JMenuItem jSaveImageMenu = new JMenuItem();
		jSaveImageMenu.setText("Image Snapshot");
		jSaveImageMenu.addActionListener(imageSnapshotListener);

		final JCheckBox jShowAllMArrays = new JCheckBox();
		jShowAllMArrays.setText("All Arrays");
		jShowAllMArrays.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAllMArrays(jShowAllMArrays.isSelected());
			}
		});
		jShowAllMArrays.setSelected(false);
		
		final JCheckBox jShowAllMarkers = new JCheckBox();
		jShowAllMarkers.setText("All Markers");
		jShowAllMarkers.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAllMarkers(jShowAllMarkers.isSelected());
			}
		});
		jShowAllMarkers.setSelected(false);
		
		jMALabel.setEditable(false);		
		jMALabel.setMinimumSize(new Dimension(40, 20));
		
		JToolBar jToolBar = new JToolBar();
		mainPanel.add(jToolBar, BorderLayout.SOUTH);
		jToolBar.setLayout(new BoxLayout(jToolBar, BoxLayout.X_AXIS));
		jToolBar.add(jShowAllMArrays, null);
		jToolBar.add(Box.createHorizontalStrut(5), null);
		jToolBar.add(jShowAllMarkers, null);
		jToolBar.add(Box.createGlue(), null);
		jToolBar.add(jMALabel, null);
		jToolBar.add(Box.createGlue(), null);
		jToolBar.add(valueGradient);
		jToolBar.add(Box.createGlue(), null);
		jToolBar.add(new JLabel("Intensity"), null);
		jToolBar.add(Box.createHorizontalStrut(5), null);
		jToolBar.add(intensitySlider, null);
		jToolBar.add(Box.createGlue(), null);
		jToolBar.add(new JLabel("Array"), null);
		jToolBar.add(Box.createHorizontalStrut(5), null);
		jToolBar.add(microarraySlider, null);
		mainPanel.add(microarrayImageArea, BorderLayout.CENTER);
		jDisplayPanelPopup.add(jShowMarkerMenu);
		jDisplayPanelPopup.add(jRemoveMarkerMenu); // Popup Menu Added to the
													// MAImageArea
		jDisplayPanelPopup.add(jSaveImageMenu);
	}

	private void chipSlider_stateChanged(ChangeEvent e) {
		int mArrayId = microarraySlider.getValue();
		if ((mArrayId >= 0) && !forcedSliderChange) {
			selectMicroarray(mArrayId);
			if (mArraySet != null && dataSetView.items().size() > 0) {
				DSMicroarray array = dataSetView.items().get(mArrayId);
				jMALabel.setText(array.getLabel());
			}
		}
	}

	private void intensitySlider_stateChanged(ChangeEvent e) {
		float v = intensitySlider.getValue() / 100.0f;
		if (v > 1) {
			microarrayImageArea.setIntensity((float) (1 + Math.exp(v) - Math
					.exp(1.0)));
		} else {
			microarrayImageArea.setIntensity(v);
		}
		microarrayImageArea.repaint();
	}

	@Publish
	public org.geworkbench.events.PhenotypeSelectedEvent publishPhenotypeSelectedEvent(
			org.geworkbench.events.PhenotypeSelectedEvent event) {
		return event;
	}

	private void selectMicroarray(int mArrayId) {
		if (mArraySet != null && dataSetView.items().size() > 0) {
			DSMicroarray mArray = dataSetView.items().get(mArrayId);
			if (mArray != null) {
				microarrayId = mArray.getSerial();
				microarrayImageArea.setMicroarray(mArray);
				microarrayImageArea.repaint();
			}
		} else {
			microarrayImageArea.setMicroarray(null);
		}
	}

	/**
	 * geneSelectorAction
	 * 
	 * @param e
	 *            GeneSelectorEvent
	 */
	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {
		if (e.getPanel() != null) {
			DSPanel<DSGeneMarker> markerPanel = e.getPanel().activeSubset();
			dataSetView.setMarkerPanel(markerPanel);
			resetMicroarraySlider();
			mainPanel.repaint();
		}
	}

	@Subscribe
	public void receive(SingleMicroarrayEvent event, Object source) {
		DSMicroarray array = event.getMicroarray();
		displayMicroarray(array);
	}

	private void displayMicroarray(DSMicroarray array) {
		int index = dataSetView.items().indexOf(array);
		if (index != -1) {
			selectMicroarray(index);
			forcedSliderChange = true;
			microarraySlider.setValue(index);
			forcedSliderChange = false;
			jMALabel.setText(array.getLabel());
		}
	}

	/**
	 * phenotypeSelectorAction
	 * 
	 * @param e
	 *            PhenotypeSelectorEvent
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Subscribe
	public void receive(PhenotypeSelectorEvent e, Object source) {
		if (e.getTaggedItemSetTree() != null) {
			DSMicroarray oldArray = null;
			try {
				oldArray = dataSetView.items().get(microarrayId);
			} catch (IndexOutOfBoundsException ioobe) {
				// Ignore -- no arrays
			}
			DSPanel<DSBioObject> mArrayPanel = e.getTaggedItemSetTree();
			dataSetView.setItemPanel((DSPanel) mArrayPanel);
			resetMicroarraySlider();
			// Keep old microarray selection, if possible
			if (oldArray != null) {
				displayMicroarray(oldArray);
			}
			mainPanel.repaint();
		}
	}

}
