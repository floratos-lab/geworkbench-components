package org.geworkbench.components.microarrays;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.SingleMicroarrayEvent;
import org.geworkbench.util.ColorScale;
import org.geworkbench.util.microarrayutils.MicroarrayVisualizer;

/**
 * <p>
 * Title: Plug And Play Framework
 * </p>
 * <p>
 * Description: Architecture for enGenious Plug&Play
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: First Genetic Trust
 * </p>
 * 
 * @author Andrea Califano
 * @version $Id$
 */

@AcceptTypes( { DSMicroarraySet.class })
public class MicroarrayPanel extends MicroarrayVisualizer implements
		VisualPlugin, MenuListener {
	private MicroarrayDisplay microarrayImageArea = new MicroarrayDisplay(this);
	private int x = 0;
	private int y = 0;
	private JToolBar jToolBar = new JToolBar();
	private JSlider jMASlider = new JSlider();
	private JSlider intensitySlider = new JSlider();
	private JCheckBox jShowAllMArrays = new JCheckBox();
	private JTextField jMALabel = new JTextField(20);
	private JLabel intensityLabel = new JLabel("Intensity");
	private JLabel arrayLabel = new JLabel("Array");
	private JPopupMenu jDisplayPanelPopup = new JPopupMenu();
	private JMenuItem jShowMarkerMenu = new JMenuItem();
	private JMenuItem jRemoveMarkerMenu = new JMenuItem();
	private JMenuItem jSaveImageMenu = new JMenuItem();
	private JCheckBox jShowAllMarkers = new JCheckBox();
	private ColorScale valueGradient = new ColorScale(
			Color.gray, Color.gray, Color.gray);
	private BorderLayout jLayout = new BorderLayout();
	private HashMap<String, ActionListener> listeners = new HashMap<String, ActionListener>();
	private DSMicroarraySet<DSMicroarray> mArraySet = null;
	private boolean forcedSliderChange = false;

	public MicroarrayPanel() {
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void setMicroarraySet(DSMicroarraySet maSet) {
		// Note that the check to guarantee that this is in fact a valid MA Set
		// and that it is
		// different from the previous one is already performed at the
		// superclass level.
		// This method should never be called other than by the superclass
		// changeMicroArraySet method.
		mArraySet = maSet;
		microarrayImageArea.setMicroarrays(mArraySet);
		if (maSet != null) {
			org.geworkbench.bison.util.colorcontext.ColorContext colorContext = (org.geworkbench.bison.util.colorcontext.ColorContext) maSet
					.getObject(org.geworkbench.bison.util.colorcontext.ColorContext.class);
			if (colorContext != null) {
				valueGradient.setMinColor(colorContext
						.getMinColorValue(intensitySlider.getValue()));
				valueGradient.setCenterColor(colorContext
						.getMiddleColorValue(intensitySlider.getValue()));
				valueGradient.setMaxColor(colorContext
						.getMaxColorValue(intensitySlider.getValue()));
				valueGradient.repaint();
			}
			reset();
			selectMicroarray(0);
		}
	}

	@Override
	protected void reset() {
		super.reset();
		if (mArraySet != null) {
			jMASlider.setMaximum(dataSetView.items().size() - 1);
		} else {
			jMASlider.setMaximum(0);
			jMASlider.setMinimum(0);
			jMASlider.setValue(0);
		}
	}

	private void jbInit() throws Exception {
		microarrayImageArea.setLayout(jLayout);
		microarrayImageArea.setBorder(BorderFactory.createEtchedBorder());
		microarrayImageArea.setOpaque(false);

		microarrayImageArea.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				if (mArraySet != null) {
					x = e.getX();
					y = e.getY();
					markerId = microarrayImageArea.getGeneIdAndRubberBand(x, y);
					if ((markerId >= 0) && (markerId < mArraySet.size())
							&& (markerId != microarrayImageArea.selectedGeneId)) {
						microarrayImageArea.selectedGeneId = markerId;
					}
				}
			}
		});
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
		jMASlider.setValue(0);
		jMASlider.setMaximum(0);
		jMASlider.setMinimum(0);
		jMASlider.setSnapToTicks(true);
		jMASlider.setPaintTicks(true);
		jMASlider.setMinorTickSpacing(1);
		jMASlider.setMajorTickSpacing(5);
		jMASlider.setCursor(java.awt.Cursor
				.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		jMASlider.addChangeListener(new javax.swing.event.ChangeListener() {
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

		jShowMarkerMenu.setText("Show Marker");
		jShowMarkerMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				microarrayImageArea.graphGene(markerId);
				microarrayImageArea.repaint();
			}
		});

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
		jSaveImageMenu.setText("Image Snapshot");
		jSaveImageMenu.addActionListener(imageSnapshotListener);

		jShowAllMArrays.setText("All Arrays");
		jShowAllMArrays.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAllMArrays(jShowAllMArrays.isSelected());
			}
		});
		jShowAllMArrays.setSelected(false);
		
		jShowAllMarkers.setText("All Markers");
		jShowAllMarkers.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAllMarkers(jShowAllMarkers.isSelected());
			}
		});
		jShowAllMarkers.setSelected(false);
		
		jMALabel.setEditable(false);		
		jMALabel.setMinimumSize(new Dimension(40, 20));
		
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
		jToolBar.add(intensityLabel, null);
		jToolBar.add(Box.createHorizontalStrut(5), null);
		jToolBar.add(intensitySlider, null);
		jToolBar.add(Box.createGlue(), null);
		jToolBar.add(arrayLabel, null);
		jToolBar.add(Box.createHorizontalStrut(5), null);
		jToolBar.add(jMASlider, null);
		mainPanel.add(microarrayImageArea, BorderLayout.CENTER);
		jDisplayPanelPopup.add(jShowMarkerMenu);
		jDisplayPanelPopup.add(jRemoveMarkerMenu); // Popup Menu Added to the
													// MAImageArea
		jDisplayPanelPopup.add(jSaveImageMenu);
	}

	private void chipSlider_stateChanged(ChangeEvent e) {
		int mArrayId = jMASlider.getValue();
		if ((mArrayId >= 0) && !forcedSliderChange) {
			if (selectMicroarray(mArrayId)) {
				DSMicroarray array = dataSetView.items().get(mArrayId);
				publishPhenotypeSelectedEvent(new org.geworkbench.events.PhenotypeSelectedEvent(
						array));
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

	private boolean selectMicroarray(int mArrayId) {
		if (mArraySet != null && dataSetView.items().size() > 0) {
			DSMicroarray mArray = dataSetView.items().get(mArrayId);
			if (mArray != null) {
				microarrayId = mArray.getSerial();
				microarrayImageArea.setMicroarray(mArray);
				try {
					microarrayImageArea.repaint();
				} catch (java.lang.Exception exception) {
					exception.printStackTrace();
				}
			}
			return true;
		} else {
			microarrayImageArea.setMicroarray(null);
			return false;
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
			markerPanel = e.getPanel().activeSubset();
			dataSetView.setMarkerPanel(markerPanel);
			reset();
			repaint();
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
			jMASlider.setValue(index);
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
			mArrayPanel = e.getTaggedItemSetTree();
			dataSetView.setItemPanel((DSPanel) mArrayPanel);
			reset();
			// Keep old microarray selection, if possible
			if (oldArray != null) {
				displayMicroarray(oldArray);
			}
			repaint();
		}
	}

}
