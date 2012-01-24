package org.geworkbench.components.colormosaic;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.StringCharacterIterator;


/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version $Id$
 */
public final class CMVRuler extends JComponent {
	private static final long serialVersionUID = -8325233520281492489L;
	
	static private final int SIZE = 22;
    static private final int DEFAULTRES = 72;
    private Font labelFont = null;
    private int fontSize = 9;
    private int selGroupId = -1;

    private double width = SIZE;
    private ColorMosaicImage colorMosaicImage = null;
    private boolean verticalText = false;
    
    private JMenu jMenu2 = new JMenu();
    private JPopupMenu jGeneRuleMenu = new JPopupMenu();
    private JMenuItem jMenuItem1 = new JMenuItem();
    private JMenuItem jMenuItem2 = new JMenuItem();
    private JMenuItem jMenuItem3 = new JMenuItem();
    private JMenuItem jMenuItem4 = new JMenuItem();
    
    public CMVRuler(ColorMosaicImage area) {
        colorMosaicImage = area;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setFont(double scale) {
        int fontSize = Math.min(colorMosaicImage.getFontSize(), 10);

        if (scale != 1.0) {
            fontSize = 15;
        }

        if (labelFont == null) {
            this.fontSize = fontSize;
            labelFont = new Font("Times New Roman", Font.PLAIN, this.fontSize);
        } else {
            if (this.fontSize != fontSize) {
                this.fontSize = fontSize;
                labelFont = new Font("SansSerif", Font.PLAIN, this.fontSize);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics gGen) {
        paint(gGen, DEFAULTRES);
    }

    public void paint(Graphics gGen, int res) {
        Graphics2D g = (Graphics2D) gGen;
        double scale = (double) res / (double) DEFAULTRES;
        Rectangle drawHere = g.getClipBounds();
        g.setColor(Color.WHITE);
        g.fill(drawHere);
        AffineTransform saveAt = null;

        if (verticalText) {
            AffineTransform at = new AffineTransform();
            saveAt = g.getTransform();
            at.rotate(-Math.PI / 2);
            g.transform(at);
        }

        int clusterNo = colorMosaicImage.getClusterNo();

        if (clusterNo > 0) {
            // Do the ruler labels in a small font that's black.
            computeWidth(scale);

            int tickSize = (int) (5.0 * scale);
            int minSize = (int) scale;
            g.setFont(labelFont);
            g.setColor(Color.black);

            int geneId = 0;
            EisenBlock[] cluster = colorMosaicImage.getClusters();
            
            // TODO we need to review whether cluster and eisenBlock are intended to be possibly null
            if(cluster==null) return;

            for (EisenBlock eisenBlock : cluster) {
            	if(eisenBlock==null) return;

                // Use clipping bounds to calculate first tick and last tick location.
                //Vector boundaries = Area.GetChipManager().Boundaries;
                DSPanel<DSGeneMarker> gm = eisenBlock.getPanel();

                if (!eisenBlock.showAllMarkers && (gm != null)) {
                    for (int j = 0; j < gm.panels().size(); j++) {

                        //IMarkerSet gg = gm.getGroup(j);
                        DSPanel<DSGeneMarker> gg = gm.panels().get(j);

                        if (gg.isActive()) {
                            int firstGene = geneId;
                            int lastGene = geneId + gg.size();

                            if (lastGene > firstGene) {
                                int y0 = (firstGene * colorMosaicImage.geneHeight) + (colorMosaicImage.geneHeight / 2);
                                int y1 = (lastGene * colorMosaicImage.geneHeight) - (colorMosaicImage.geneHeight / 2);
                                Rectangle2D rect = labelFont.getStringBounds(new StringCharacterIterator(gg.getLabel()), 0, gg.getLabel().length(), g.getFontRenderContext());
                                int yy = (y0 + y1) / 2;

                                if (verticalText) {
                                    int halfWidth = (int) (rect.getWidth() / 2);
                                    g.drawLine(-y0, (int) (width - minSize), -y0, (int) (width - tickSize));
                                    g.drawLine(-y0, (int) (width - tickSize), -(yy - halfWidth - 2), (int) (width - tickSize));
                                    g.drawLine(-y1, (int) width - tickSize, -(yy + halfWidth + 2), (int) width - tickSize);
                                    g.drawLine(-y1, (int) width - minSize, -y1, (int) width - tickSize);
                                    g.drawString(gg.getLabel(), -(int) (yy + halfWidth), (int) width - tickSize);
                                } else {
                                    g.drawLine((int) (width - minSize), y0, (int) (width - tickSize), y0);
                                    g.drawLine((int) (width - tickSize), y0, (int) (width - tickSize), y1);
                                    g.drawLine((int) width - minSize, y1, (int) width - tickSize, y1);
                                    g.drawString(gg.getLabel(), tickSize, (int) (yy + (colorMosaicImage.getFontSize() / 2)));
                                }

                                geneId = lastGene;
                            }
                        }
                    }
                }
            }
        }

        if (verticalText) {
            g.transform(saveAt);
        }
    }

    private void jbInit() throws Exception {
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                this_mouseClicked(e);
            }
        });
        jMenuItem1.setText("Hide");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuItem1_actionPerformed(e);
            }
        });
        jMenu2.setText("Show");
        jMenuItem2.setText("Show All");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuItem2_actionPerformed(e);
            }
        });
        jMenuItem3.setText("<< Sel");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuItem3_actionPerformed(e);
            }
        });
        jMenuItem4.setText(">> Sel");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuItem4_actionPerformed(e);
            }
        });
        this.setBackground(Color.white);
        jGeneRuleMenu.add(jMenuItem1);
        jGeneRuleMenu.add(jMenuItem3);
        jGeneRuleMenu.add(jMenuItem4);
        jGeneRuleMenu.add(jMenuItem2);
        jGeneRuleMenu.add(jMenu2);
    }

    private void this_mouseClicked(MouseEvent e) {
        if (e.getModifiers() == Event.META_MASK) {

            // Make the jPopupMenu visible relative to the current mouse position in the container.
            DSPanel<DSGeneMarker> markerGroup = colorMosaicImage.getPanel();

            if (markerGroup != null) {
                jMenu2.removeAll();

                for (int i = 0; i < markerGroup.panels().size(); i++) {
                    DSPanel<DSGeneMarker> markerVector = markerGroup.panels().get(i);

                    JCheckBoxMenuItem jMenuItem = new JCheckBoxMenuItem();
                    jMenuItem.setText(markerVector.getLabel());
                    jMenuItem.setState(markerVector.isActive());
                    jMenu2.add(jMenuItem);
                }

                jGeneRuleMenu.show(this, e.getX(), e.getY());

                int x = e.getX();
                selGroupId = getGroupId(x);
            }
        }
    }

    private void jMenuItem2_actionPerformed(ActionEvent e) {
        DSPanel<DSGeneMarker> panel = colorMosaicImage.getPanel();

        for (int i = 0; i < panel.panels().size(); i++) {
            DSPanel<DSGeneMarker> markerVector = panel.panels().get(i);
            markerVector.setActive(true);
        }

        colorMosaicImage.repaint();
        repaint();
    }

    private int getGroupId(int x) {
        int markerId = x / colorMosaicImage.geneWidth;
        DSPanel<DSGeneMarker> panel = colorMosaicImage.getPanel();

        int lastId = 0;

        for (int i = 0; i < panel.panels().size(); i++) {
            DSPanel<DSGeneMarker> markerVector = panel.panels().get(i);

            if (markerId < (lastId + markerVector.size())) {
                return i;
            }

            lastId += markerVector.size();
        }

        return -1;
    }

    private void jMenuItem1_actionPerformed(ActionEvent e) {
        if (selGroupId >= 0) {
            DSPanel<DSGeneMarker> panel = colorMosaicImage.getPanel();
            panel.panels().get(selGroupId).setActive(false);
            colorMosaicImage.repaint();
            repaint();
        }
    }

    private void jMenuItem3_actionPerformed(ActionEvent e) {
        if (selGroupId > 0) {
            DSPanel<DSGeneMarker> panel = colorMosaicImage.getPanel();

            DSPanel<DSGeneMarker> subPanel = panel.panels().get(selGroupId);
            panel.panels().set(selGroupId, panel.panels().get(selGroupId - 1));
            panel.panels().set(selGroupId - 1, subPanel);
            colorMosaicImage.repaint();
            repaint();
        }
    }

    private void jMenuItem4_actionPerformed(ActionEvent e) {
        DSPanel<DSGeneMarker> panel = colorMosaicImage.getPanel();

        if ((selGroupId >= 0) && (selGroupId < (panel.panels().size() - 1))) {
            DSPanel<DSGeneMarker> subPanel = panel.panels().get(selGroupId);
            panel.panels().set(selGroupId, panel.panels().get(selGroupId + 1));
            panel.panels().set(selGroupId + 1, subPanel);
            colorMosaicImage.repaint();
            repaint();
        }
    }

    public void computeWidth(double scale) {
        if (verticalText) {
            width = SIZE * scale;
        } else {
            int clusterNo = colorMosaicImage.getClusterNo();
			boolean isGrouped = false;

			width = 0;

			if (clusterNo > 0) {
				int geneId = 0;
				setFont(scale);

				EisenBlock[] cluster = colorMosaicImage.getClusters();
				if (null != cluster) {
					for (int i = 0; i < clusterNo; i++) {
						if (null != cluster[i]) {
							DSPanel<DSGeneMarker> gm = cluster[i].getPanel();

							if (!cluster[i].showAllMarkers && (gm != null)) {
								for (int j = 0; j < gm.panels().size(); j++) {
									DSPanel<DSGeneMarker> gg = gm.panels().get(j);

									if (gg.isActive()) {
										int firstGene = geneId;
										int lastGene = geneId + gg.size();

										if (lastGene > firstGene) {
											isGrouped = true;
											if (getGraphics()!=null){
											Rectangle2D rect = labelFont
													.getStringBounds(
															new StringCharacterIterator(
																	gg
																			.getLabel()),
															0,
															gg.getLabel()
																	.length(),
															((Graphics2D) getGraphics())
																	.getFontRenderContext());
											width = Math.max(width, rect
													.getWidth());
											}else{
												width = 0;
											}
											geneId = lastGene;
										}
									}
								}
							}
						}
					}
				}
				if (isGrouped) {
					width += (5.0 * 2.5 * scale);
				}
			}
		}

        setPreferredSize(new Dimension((int) width, colorMosaicImage.getRequiredHeight()));
    }

    @Override
    public void revalidate() {
        computeWidth(1.0);
        super.revalidate();
    }

    public double getScaledWidth() {
        return width;
    }
}
