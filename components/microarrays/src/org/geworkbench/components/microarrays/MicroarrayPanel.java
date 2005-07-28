package org.geworkbench.components.microarrays;

import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.SingleMicroarrayEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.util.microarrayutils.*;
import org.geworkbench.util.microarrayutils.MatrixCreater;
import org.geworkbench.util.microarrayutils.MicroarrayVisualizer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

/**
 * <p>Title: Plug And Play Framework</p>
 * <p>Description: Architecture for enGenious Plug&Play</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust</p>
 *
 * @author Andrea Califano
 * @version 1.0
 */

public class MicroarrayPanel extends MicroarrayVisualizer implements VisualPlugin, MenuListener {
    //IMarkerGraphSubscriber markerPlot = null;
    //JMicroarrayVisualzer visualizer           = new JMicroarrayVisualzer(this);
    MicroarrayDisplay microarrayImageArea = new MicroarrayDisplay(this);
    int x = 0;
    int y = 0;
    JButton jSaveBttn = new JButton();
    JToolBar jToolBar = new JToolBar();
    JSlider jMASlider = new JSlider();
    Component jSpacer;
    JCheckBox jEnabledBox = new JCheckBox();
    JCheckBox jShowAllMArrays = new JCheckBox();
    JTextField jMALabel = new JTextField(10) {
        @Override public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    };
    JPopupMenu jDisplayPanelPopup = new JPopupMenu();
    JMenuItem jShowMarkerMenu = new JMenuItem();
    JMenuItem jRemoveMarkerMenu = new JMenuItem();
    JMenuItem jSaveImageMenu = new JMenuItem();
    JCheckBox jShowAllMarkers = new JCheckBox();
    BorderLayout jLayout = new BorderLayout();
    HashMap listeners = new HashMap();
    DSMicroarraySet<DSMicroarray> mArraySet = null;
    private boolean forcedSliderChange = false;
    public static final int IMAGE_SNAPSHOT_WIDTH = 800;

    public MicroarrayPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ActionListener getActionListener(String key) {
        return (ActionListener) listeners.get(key);
    }

    public void setMicroarraySet(DSMicroarraySet maSet) {
        // Note that the check to guarantee that this is in fact a valid MA Set and that it is
        // different from the previous one is already performed at the superclass level.
        // This method should never be called other than by the superclass JNotifyMAChange method.
        mArraySet = maSet;
        microarrayImageArea.setMicroarrays(mArraySet);
        reset();
        selectMicroarray(0);
    }

    protected void reset() {
        if (mArraySet != null) {
            jMASlider.setMaximum(dataSetView.items().size() - 1);
            jMASlider.setMinimum(0);
            jMASlider.setValue(0);
        } else {
            jMASlider.setMaximum(0);
            jMASlider.setMinimum(0);
            jMASlider.setValue(0);
        }
    }

    protected void addPattern(Object pattern) {
        microarrayImageArea.patterns.add(pattern);
    }

    protected void clearPatterns() {
        microarrayImageArea.patterns.clear();
    }

    void pushMask(Object mask) {
        microarrayImageArea.masks.push(mask);
    }

    void popMask() {
        microarrayImageArea.masks.pop();
    }

    void clearMasks() {
        microarrayImageArea.masks.clear();
    }

    private void jbInit() throws Exception {
        jSpacer = Box.createHorizontalStrut(8);
        jSaveBttn.setText("File Operation");
        jSaveBttn.setToolTipText("Save or make new matrix file");
        jSaveBttn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jSaveBttn_actionPerformed(e);
            }
        });

        microarrayImageArea.setLayout(jLayout);
        jEnabledBox.setText("Enabled");
        jEnabledBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enabledBox_actionPerformed(e);
            }
        });
        microarrayImageArea.setBorder(BorderFactory.createEtchedBorder());
        microarrayImageArea.setOpaque(false);

        microarrayImageArea.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                microarrayImageArea_mouseMoved(e);
            }
        });
        microarrayImageArea.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                microarrayImageArea_mouseClicked(e);
            }

            public void mouseExited(MouseEvent e) {
                microarrayImageArea_mouseExited(e);
            }

            public void mouseReleased(MouseEvent e) {
                microarrayImageArea_mouseReleased(e);
            }
        });
        jMASlider.setValue(0);
        jMASlider.setMaximum(0);
        jMASlider.setMinimum(0);
        jMASlider.setSnapToTicks(true);
        jMASlider.setPaintTicks(true);
        jMASlider.setMinorTickSpacing(1);
        jMASlider.setMajorTickSpacing(5);
        jMASlider.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        jMASlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                chipSlider_stateChanged(e);
            }
        });

        jShowMarkerMenu.setText("Show Marker");
        jShowMarkerMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jShowMarkerMenu_actionPerformed(e);
            }
        });

        jRemoveMarkerMenu.setText("Remove Marker");
        jRemoveMarkerMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jRemoveMarkerMenu_actionPerformed(e);
            }
        });

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jSaveImageMenu_actionPerformed(e);
            }
        };

        listeners.put("File.Image Snapshot", listener);
        jSaveImageMenu.setText("Image Snapshot");
        jSaveImageMenu.addActionListener(listener);

        jShowAllMArrays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jShowAllMArrays_actionPerformed(e);
            }
        });
        jShowAllMArrays.setSelected(true);
        jShowAllMArrays.setText("All Arrays");
        jShowAllMarkers.setText("All Markers");
        jShowAllMarkers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jShowAllMarkers_actionPerformed(e);
            }
        });
        jShowAllMarkers.setSelected(true);
        jShowAllMarkers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jShowAllMarkers_actionPerformed(e);
            }
        });
        jMALabel.setEditable(false);
        mainPanel.add(jToolBar, BorderLayout.SOUTH);
        jToolBar.add(jShowAllMArrays, null);
        jToolBar.add(jShowAllMarkers, null);
        jToolBar.add(jMALabel, null);
        jToolBar.add(jMASlider, null);
        jToolBar.add(jSpacer, null);
        jToolBar.add(jEnabledBox, null);
        jToolBar.add(jSaveBttn, null);
        mainPanel.add(microarrayImageArea, BorderLayout.CENTER);
        jDisplayPanelPopup.add(jShowMarkerMenu);
        jDisplayPanelPopup.add(jRemoveMarkerMenu); // Popup Menu Added to the MAImageArea
        jDisplayPanelPopup.add(jSaveImageMenu);
    }

    void chipSlider_stateChanged(ChangeEvent e) {
        int mArrayId = jMASlider.getValue();
        if ((mArrayId >= 0) && !forcedSliderChange) {
            selectMicroarray(mArrayId);
            DSMicroarray array = dataSetView.items().get(mArrayId);
            publishPhenotypeSelectedEvent(new org.geworkbench.events.PhenotypeSelectedEvent(array));
            updateLabel(array);
        }
    }

    private void updateLabel(DSMicroarray array) {
        jMALabel.setText(array.getLabel());
    }

    @Publish public org.geworkbench.events.PhenotypeSelectedEvent publishPhenotypeSelectedEvent(org.geworkbench.events.PhenotypeSelectedEvent event) {
        return event;
    }

    private void selectMicroarray(int mArrayId) {
        if (mArraySet != null && dataSetView.items().size() > 0) {
            DSMicroarray mArray = dataSetView.items().get(mArrayId);
            if (mArray != null) {
                microarrayId = mArray.getSerial();
                microarrayImageArea.setMicroarray(mArray);
                //subscriber.notifyChange(this, IMicroarrayIdChangeSubscriber.class);
                //subscriber.notifyChange(this, IMarkerIdClickSubscriber.class);
                try {
                    microarrayImageArea.repaint();
                    jEnabledBox.setSelected(mArray.enabled());
                } catch (java.lang.Exception exception) {
                    exception.printStackTrace();
                }
            }
        } else {
            microarrayImageArea.setMicroarray(null);
        }
    }

    void microarrayImageArea_mouseMoved(MouseEvent event) {
        if (mArraySet != null) {
            x = event.getX();
            y = event.getY();
            markerId = microarrayImageArea.getGeneIdAndRubberBand(x, y);
            if ((markerId >= 0) && (markerId < mArraySet.size()) && (markerId != microarrayImageArea.selectedGeneId)) {
                microarrayImageArea.selectedGeneId = markerId;
                //selectedGeneId is not used anywhere
                //this is not right, markerID is just the relative position of the marker in current image
                //it can't be used to find the marker in microarryset++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++!!!!!
                //subscriber.notifyChange(this, IMarkerIdChangeSubscriber.class);
            }
        }
    }

    void microarrayImageArea_mouseExited(MouseEvent e) {
        microarrayImageArea.rubberBandBox(-1, -1);
    }

    public void showValidOnly(boolean show) {
        microarrayImageArea.showValidOnly = show;
        mainPanel.repaint();
    }

    void enabledBox_actionPerformed(ActionEvent e) {
        DSMicroarray im = microarrayImageArea.getMicroarray();
        if (im != null) {

            microarrayImageArea.getMicroarray().enable(jEnabledBox.isSelected());
        }
    }

    void jShowMarkerMenu_actionPerformed(ActionEvent e) {

        //markerPlot.notifyMarkerGraph(microarraySet, microarrayId, markerId, true);
        microarrayImageArea.graphGene(markerId);
        microarrayImageArea.repaint();
    }

    public String getComponentName() {
        return "Microarray Display Panel";
    }

    void jRemoveMarkerMenu_actionPerformed(ActionEvent e) {
        //markerPlot.notifyMarkerGraph(microarraySet, visualizer.microarrayId, visualizer.markerId, false);
        microarrayImageArea.ungraphGene(markerId);
        microarrayImageArea.repaint();
    }

    void jSaveImageMenu_actionPerformed(ActionEvent e) {
        Image currentImage = microarrayImageArea.getCurrentImage();
        if (currentImage != null && microarrayImageArea.microarray != null) {
            int width = currentImage.getWidth(null);
            int height = currentImage.getHeight(null);
            double factor = IMAGE_SNAPSHOT_WIDTH / (double) width;
            width = (int) (width * factor);
            height = (int) (height * factor);
            Image scaled = currentImage.getScaledInstance(width, height, Image.SCALE_FAST);
            ImageIcon newIcon = new ImageIcon(scaled, "Microarray Image: " + microarrayImageArea.microarray.getLabel());
            org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("MicroarrayPanel ImageSnapshot", newIcon, ImageSnapshotEvent.Action.SAVE);
            publishImageSnapshotEvent(event);
        }
    }

    void jShowAllMArrays_actionPerformed(ActionEvent e) {
        showAllMArrays(jShowAllMArrays.isSelected());
    }

    void microarrayImageArea_mouseClicked(MouseEvent e) {

        if (mArraySet != null) {
            //int clickNo = e.getClickCount(); //? not used anywhere

            String uid = Integer.toString(markerId);
            if (markerId != -1) {
                MarkerSelectedEvent mse = new MarkerSelectedEvent(mArraySet.getMarkers().get(markerId));
                publishMarkerSelectedEvent(mse);
            }
        }
    }

    void microarrayImageArea_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            jDisplayPanelPopup.show(mainPanel, e.getX(), e.getY());
        }
    }

    void jShowAllMarkers_actionPerformed(ActionEvent e) {
        showAllMarkers(jShowAllMarkers.isSelected());
    }

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    @Subscribe public void receive(GeneSelectorEvent e, Object source) {
        if (e.getPanel() != null && e.getPanel().size() > 0) {
            markerPanel = e.getPanel().activeSubset();
            dataSetView.setMarkerPanel(markerPanel);
            reset();
            repaint();
        }
    }

    @Subscribe public void receive(SingleMicroarrayEvent event, Object source) {
        DSMicroarray array = event.getMicroarray();
        int index = dataSetView.items().indexOf(array);
        if (index != -1) {
            selectMicroarray(index);
            forcedSliderChange = true;
            jMASlider.setValue(index);
            forcedSliderChange = false;
            updateLabel(array);
        }
    }

    /**
     * phenotypeSelectorAction
     *
     * @param e PhenotypeSelectorEvent
     */
    @Subscribe public void receive(org.geworkbench.events.PhenotypeSelectorEvent e, Object source) {
        if (e.getTaggedItemSetTree() != null && e.getTaggedItemSetTree().size() > 0) {
            mArrayPanel = e.getTaggedItemSetTree();
            dataSetView.setItemPanel((DSPanel) mArrayPanel);
            reset();
            repaint();
        }
    }

    void jSaveBttn_actionPerformed(ActionEvent e) {
        Object[] options = {"Create new matrix file from data", "Add Phenotype"};
        int n = JOptionPane.showOptionDialog(null, "What do you want to do?", "File Operation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        switch (n) {

            case 0:
                org.geworkbench.util.microarrayutils.MatrixCreater.loadData();
                ;
                break;

            case 1:
                MatrixCreater.addPhenotype();
                break;
            default:
                ;

        }

    }

}
