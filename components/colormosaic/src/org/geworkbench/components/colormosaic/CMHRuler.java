package org.geworkbench.components.colormosaic;

import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.StringCharacterIterator;
import java.text.AttributedString;
import java.awt.font.TextAttribute;


/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version 1.0
 */
public class CMHRuler extends JComponent {
	private static final long serialVersionUID = -2929497868642667486L;
	private static final int SIZE = 15;
	private static final int gutter = 4;
    public final static int DEFAULTRES = 72;
    private int selGroupId = -1;
//    private boolean showPattern = true;
    private int increment;
    private ColorMosaicImage colorMosaicImage;
    private int vSize = 0;
    private int arrayNameLength = 0;
    // Do the ruler labels in a small font that's black.
    private int fontSize = 9;
    private Font font = new Font("Times New Roman", Font.PLAIN, fontSize);
    private JPopupMenu jGeneRuleMenu = new JPopupMenu();
    private JMenu jShowMArrayVector = new JMenu();
    private JMenuItem jHideMArrayVector = new JMenuItem();
    private JMenuItem jShowAllMArrayVectors = new JMenuItem();
    private JMenuItem jLeftMArrayVector = new JMenuItem();
    private JMenuItem jRightMArrayVector = new JMenuItem();

    public CMHRuler(ColorMosaicImage area) {
        colorMosaicImage = area;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CMHRuler() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getIncrement() {
        return increment;
    }

    public void setPreferredHeight(int ph) {
        setPreferredSize(new Dimension(colorMosaicImage.getRequiredWidth(), vSize));
    }

    public void setPreferredWidth(int pw) {
        setPreferredSize(new Dimension(colorMosaicImage.getRequiredWidth(), vSize));
    }

    public void paintComponent(Graphics gGen) {
        paint(gGen, DEFAULTRES);
    }

    private boolean clearArraynames = true;
    public void setClearArraynames(boolean val) {
    	clearArraynames = val;
    }

    private boolean clearDisplay = true;
    public void setClearDisplay(boolean val) {
    	clearDisplay = val;
    }
    
    public void paint(Graphics gGen, int res) {
        double scale = (double) res / (double) DEFAULTRES;
		Graphics2D g = (Graphics2D) gGen;
		Rectangle drawHere = g.getClipBounds();
		g.setColor(Color.WHITE);
		g.fill(drawHere);

		if (res != DEFAULTRES) {
			fontSize = 15;
			font.deriveFont(fontSize);
		}
		g.setFont(font);
		g.setColor(Color.black);

		int tickLength = (int) (5 * scale);
		int realSize = (int) (vSize * scale);
		int minSize = (int) scale;
		
		// Use clipping bounds to calculate first tick and last tick location.
		// Vector boundaries = Area.GetChipManager().Boundaries;
		// ArrayList Groups = ColorMosaicImage.GetChipManager().Groups;
		DSPanel<DSMicroarray> maGroupVector = colorMosaicImage.getMArrayPanel();
		
		//ArrayList Groups = ColorMosaicImage.GetMArrayPanel().GetGroups;
		int chipId = 0;
		int chipNo = colorMosaicImage.getChipNo();

		if (maGroupVector != null) {
			// Show each phenotype in the panel if active
			for (int i = 0; i < maGroupVector.panels().size(); i++) {
                DSPanel<DSMicroarray> mArrayVector = maGroupVector.panels().get(i);

                //JMicroarrayManager chipMgr = (JMicroarrayManager)Groups.get(i);
                if (mArrayVector.isActive() && (mArrayVector.size() > 0)) {

                    int firstChip = chipId;
                    int lastChip = chipId + mArrayVector.size();
                    int x0 = ((firstChip * colorMosaicImage.geneWidth) + (colorMosaicImage.geneWidth / 2)) / 1;
                    int x1 = ((lastChip * colorMosaicImage.geneWidth) - (colorMosaicImage.geneWidth / 2)) / 1;
                    FontRenderContext frc = new FontRenderContext(null, false, false);
                    Rectangle2D rect = font.getStringBounds(new StringCharacterIterator(mArrayVector.getLabel()), 0, mArrayVector.getLabel().length(), frc); //g.getFontRenderContext());
                    int xx = (x0 + x1) / 2;
                    int halfWidth = (int) (rect.getWidth() / 2);
                    int x0_Label = xx - halfWidth - 10;
                    int x1_Label = xx + halfWidth + 10;
                    g.drawLine(x0, realSize - minSize, x0, realSize - tickLength);
                    g.drawLine(x1, realSize - minSize, x1, realSize - tickLength);

                    if (x0_Label > x0) {
                        g.drawLine(x0, realSize - tickLength, x0_Label, realSize - tickLength);
                        g.drawLine(x1, realSize - tickLength, x1_Label, realSize - tickLength);
                    }

                    g.drawString(mArrayVector.getLabel(), (int) (xx - halfWidth), realSize - tickLength);
                    chipId = lastChip;                	
                }
			}	
		}
		if (clearArraynames)  return;
		
		AffineTransform hat = g.getTransform();
		AffineTransform vat = new AffineTransform();
		vat.rotate(-Math.PI / 2);
		g.transform(vat);

		for (int j = 0; j < chipNo; j++) {
			DSMicroarray pl = colorMosaicImage.getPhenoLabel(j);
			if (pl instanceof DSMicroarray) {
				DSMicroarray mArray = (DSMicroarray) pl;
				if (j == colorMosaicImage.getSelectedArray()
						&& mArray.toString().toLowerCase().indexOf(colorMosaicImage.searchArray) >= 0) {
			        ((Graphics2D)g).setComposite(colorMosaicImage.hltcomp);
					g.setColor(Color.blue);
					g.fillRect(-arrayNameLength, j * colorMosaicImage.geneWidth, arrayNameLength, colorMosaicImage.geneWidth);
				}
				((Graphics2D)g).setComposite(colorMosaicImage.comp);
				g.setColor(Color.black);

				AttributedString as = new AttributedString(mArray.toString());
				as.addAttribute(TextAttribute.FONT, font);
				as.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_LTR);
				g.drawString(as.getIterator(), -arrayNameLength, (int) ((j + 0.7) * colorMosaicImage.geneWidth));
			}
		}
		g.setTransform(hat);
    }

    private void jbInit() throws Exception {
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                this_mouseClicked(e);
            }
        });
        jHideMArrayVector.setText("Hide");
        jHideMArrayVector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jHideMArrayVector_actionPerformed(e);
            }
        });
        jShowMArrayVector.setText("Show");
        jShowAllMArrayVectors.setText("Show All");
        jShowAllMArrayVectors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jShowAllMArrayVectors_actionPerformed(e);
            }
        });
        jLeftMArrayVector.setText("<< Sel");
        jLeftMArrayVector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jLefttMArrayVector_actionPerformed(e);
            }
        });
        jRightMArrayVector.setText(">> Sel");
        jRightMArrayVector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jRightMArrayVector_actionPerformed(e);
            }
        });
        this.setBackground(Color.white);
        jGeneRuleMenu.add(jHideMArrayVector);
        jGeneRuleMenu.add(jLeftMArrayVector);
        jGeneRuleMenu.add(jRightMArrayVector);
        jGeneRuleMenu.add(jShowAllMArrayVectors);
        jGeneRuleMenu.add(jShowMArrayVector);
    }

    void this_mouseClicked(MouseEvent e) {
        if (e.getModifiers() == Event.META_MASK) {

            // Make the jPopupMenu visible relative to the current mouse position in the container.
            //JMicroarrayManager chipMgr = ColorMosaicImage.GetChipManager();
            DSPanel<DSMicroarray> maGroup = colorMosaicImage.getMArrayPanel();

            if (maGroup != null) {
                jShowMArrayVector.removeAll();

                for (int i = 0; i < maGroup.panels().size(); i++) {
                    DSPanel<DSMicroarray> cm = maGroup.panels().get(i);

                    JCheckBoxMenuItem jMenuItem = new JCheckBoxMenuItem();
                    jMenuItem.setText(cm.getLabel());
                    jMenuItem.setState(cm.isActive());
                    jShowMArrayVector.add(jMenuItem);

                    /*
                                         jMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            jCheckItem_actionPerformed(e);
                        }
                                         });
                     */
                }

                jGeneRuleMenu.show(this, e.getX(), e.getY());

                int x = e.getX();
                selGroupId = getGroupId(x);
            }
        }
    }

    /*
        void jCheckItem_actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
            JMAGroupVector    cm   = ColorMosaicImage.GetMArrayPanel();                                ().GetCMForProperty(item.getText());
            if (cm != null) {
                cm.IsActive = item.getState();
                ColorMosaicImage.GetChipManager().CacheChips();
                ColorMosaicImage.repaint();
                repaint();
            }
        }
     */
    void jShowAllMArrayVectors_actionPerformed(ActionEvent e) {
        DSPanel<DSMicroarray> maVector = colorMosaicImage.getMArrayPanel();

        //ArrayList Groups = ColorMosaicImage.GetChipManager().Groups;
        //int chipId = 0;

        for (int i = 0; i < maVector.panels().size(); i++) {
            DSPanel<DSMicroarray> taggedItemSet = maVector.panels().get(i);
            taggedItemSet.setActive(true);
        }

        colorMosaicImage.repaint();
        repaint();
    }

    int getGroupId(int x) {
        int chipId = (x * 1) / colorMosaicImage.geneWidth;
        DSPanel<DSMicroarray> taggedItemSetNode = colorMosaicImage.getMArrayPanel();

        int lastId = 0;

        for (int i = 0; i < taggedItemSetNode.panels().size(); i++) {
            DSPanel<DSMicroarray> mArrayVector = taggedItemSetNode.panels().get(i);
            if (mArrayVector.isActive()) {
                if (chipId < (lastId + mArrayVector.size())) {
                    return i;
                }
                lastId += mArrayVector.size();
            }
        }

        return -1;
    }

    void jHideMArrayVector_actionPerformed(ActionEvent e) {
        if (selGroupId >= 0) {
            DSPanel<DSMicroarray> taggedItemSetNode = colorMosaicImage.getMArrayPanel();
            taggedItemSetNode.panels().get(selGroupId).setActive(false);
            colorMosaicImage.repaint();
            repaint();
        }
    }

    void jLefttMArrayVector_actionPerformed(ActionEvent e) {
        if (selGroupId > 0) {
            DSPanel<DSMicroarray> taggedItemSetNode = colorMosaicImage.getMArrayPanel();
            DSPanel<DSMicroarray> taggedItemSet = taggedItemSetNode.panels().get(selGroupId);
            int prevGroupId;
            for (prevGroupId = selGroupId - 1; prevGroupId >= 0; prevGroupId--) {
                DSPanel<DSMicroarray> panel = taggedItemSetNode.panels().get(prevGroupId);
                if (panel.isActive()) {
                    break;
                }
            }
            if (prevGroupId >= 0) {
                taggedItemSetNode.panels().set(selGroupId, taggedItemSetNode.panels().get(prevGroupId));
                taggedItemSetNode.panels().set(prevGroupId, taggedItemSet);
                colorMosaicImage.repaint();
                repaint();
            }
        }
    }

    void jRightMArrayVector_actionPerformed(ActionEvent e) {
        DSPanel<DSMicroarray> taggedItemSetNode = colorMosaicImage.getMArrayPanel();
        if ((selGroupId >= 0) && (selGroupId < (taggedItemSetNode.panels().size() - 1))) {
            DSPanel<DSMicroarray> taggedItemSet = taggedItemSetNode.panels().get(selGroupId);
            int nextGroupId;
            for (nextGroupId = selGroupId + 1; nextGroupId < taggedItemSetNode.panels().size(); nextGroupId++) {
                DSPanel<DSMicroarray> panel = taggedItemSetNode.panels().get(nextGroupId);
                if (panel.isActive()) {
                    break;
                }
            }
            if (nextGroupId < taggedItemSetNode.panels().size()) {
                taggedItemSetNode.panels().set(selGroupId, taggedItemSetNode.panels().get(nextGroupId));
                taggedItemSetNode.panels().set(nextGroupId, taggedItemSet);
                colorMosaicImage.repaint();
                repaint();
            }
        }
    }

    public void revalidate() {
		DSPanel<DSMicroarray> marraypanel = colorMosaicImage.getMArrayPanel();
		arrayNameLength = 0;
		int chipno = colorMosaicImage.getChipNo();
		for (int j = 0; j < chipno; j++) {
			DSMicroarray pl = colorMosaicImage.getPhenoLabel(j);
			if (pl instanceof DSMicroarray) {
				FontRenderContext frc = new FontRenderContext(null, false, false);
				Rectangle2D rect = font.getStringBounds( new StringCharacterIterator(pl.toString()), 0, pl.toString().length(), frc);
				if (rect.getWidth() > arrayNameLength)
					arrayNameLength = (int) rect.getWidth();
			}
		}

		if (clearDisplay)  vSize = 0;
		else {
			if (clearArraynames)  vSize = 0;
			else  vSize = arrayNameLength + gutter;
			if (marraypanel != null && marraypanel.size() > 0)  vSize += SIZE;
		}

		setPreferredSize(new Dimension(colorMosaicImage.getRequiredWidth(), vSize));
		super.revalidate();
		repaint();
	}
}
