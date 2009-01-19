/*
 * Jalview - A Sequence Alignment Editor and Viewer
 * Copyright (C) 2007 AM Waterhouse, J Procter, G Barton, M Clamp, S Searle
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package jalview.appletgui;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import jalview.datamodel.*;

public class FeatureSettings
    extends Panel implements ItemListener,
    MouseListener, MouseMotionListener, ActionListener, AdjustmentListener
{
  FeatureRenderer fr;
  AlignmentPanel ap;
  AlignViewport av;
  Frame frame;
  Panel groupPanel;
  Panel featurePanel = new Panel();
  ScrollPane scrollPane;
  boolean alignmentHasFeatures = false;
  Image linkImage;
  Scrollbar transparency;

  public FeatureSettings(final AlignmentPanel ap)
  {
    this.ap = ap;
    this.av = ap.av;
    fr = ap.seqPanel.seqCanvas.getFeatureRenderer();

    transparency = new Scrollbar(Scrollbar.HORIZONTAL,
                                 100 - (int) (fr.transparency * 100), 1, 1, 100);

    if (fr.transparencySetter != null)
    {
      transparency.addAdjustmentListener(this);
    }
    else
    {
      transparency.setEnabled(false);
    }

    java.net.URL url = getClass().getResource("/images/link.gif");
    if (url != null)
    {
      linkImage = java.awt.Toolkit.getDefaultToolkit().getImage(url);
    }

    if (av.featuresDisplayed == null)
    {
      fr.findAllFeatures();
    }

    setTableData();

    this.setLayout(new BorderLayout());
    scrollPane = new ScrollPane();
    scrollPane.add(featurePanel);
    if (alignmentHasFeatures)
    {
      add(scrollPane, BorderLayout.CENTER);
    }

    Button invert = new Button("Invert Selection");
    invert.addActionListener(this);

    Panel lowerPanel = new Panel(new GridLayout(2, 1, 5, 10));
    lowerPanel.add(invert);

    Panel tPanel = new Panel(new BorderLayout());

    if (fr.transparencySetter != null)
    {
      tPanel.add(transparency, BorderLayout.CENTER);
      tPanel.add(new Label("Transparency"), BorderLayout.EAST);
    }
    else
    {
      tPanel.add(new Label("Transparency not available in this web browser"),
                 BorderLayout.CENTER);
    }

    lowerPanel.add(tPanel, BorderLayout.SOUTH);

    add(lowerPanel, BorderLayout.SOUTH);

    if (groupPanel != null)
    {
      groupPanel.setLayout(
          new GridLayout(fr.featureGroups.size() / 4 + 1, 4));
      groupPanel.validate();

      add(groupPanel, BorderLayout.NORTH);
    }
    frame = new Frame();
    frame.add(this);
    int height = featurePanel.getComponentCount() * 50 + 60;

    height = Math.max(200, height);
    height = Math.min(400, height);

    jalview.bin.JalviewLite.addFrame(frame, "Feature Settings", 280,
                                     height);
  }

  public void paint(Graphics g)
  {
    g.setColor(Color.black);
    g.drawString("No Features added to this alignment!!", 10, 20);
    g.drawString("(Features can be added from searches or", 10, 40);
    g.drawString("from Jalview / GFF features files)", 10, 60);
  }

  void setTableData()
  {
    alignmentHasFeatures = false;

    if (fr.featureGroups == null)
    {
      fr.featureGroups = new Hashtable();
    }

    Vector allFeatures = new Vector();
    Vector allGroups = new Vector();
    SequenceFeature[] tmpfeatures;
    String group;

    for (int i = 0; i < av.alignment.getHeight(); i++)
    {
      if (av.alignment.getSequenceAt(i).getSequenceFeatures() == null)
      {
        continue;
      }

      alignmentHasFeatures = true;

      tmpfeatures = av.alignment.getSequenceAt(i).getSequenceFeatures();
      int index = 0;
      while (index < tmpfeatures.length)
      {
        if (tmpfeatures[index].getFeatureGroup() != null)
        {
          group = tmpfeatures[index].featureGroup;
          if (!allGroups.contains(group))
          {
            allGroups.addElement(group);

            boolean visible = true;
            if (fr.featureGroups.containsKey(group))
            {
              visible = ( (Boolean) fr.featureGroups.get(group)).booleanValue();
            }

            fr.featureGroups.put(group, new Boolean(visible));

            if (groupPanel == null)
            {
              groupPanel = new Panel();
            }

            Checkbox check = new MyCheckbox(
                group,
                visible,
                (fr.featureLinks != null && fr.featureLinks.containsKey(group))
                );

            check.addMouseListener(this);
            check.setFont(new Font("Serif", Font.BOLD, 12));
            check.addItemListener(this);
            groupPanel.add(check);
          }
        }

        if (!allFeatures.contains(tmpfeatures[index].getType()))
        {
          allFeatures.addElement(tmpfeatures[index].getType());
        }
        index++;
      }
    }

    resetTable(false);
  }

  //This routine adds and removes checkboxes depending on
  //Group selection states
  void resetTable(boolean groupsChanged)
  {
    SequenceFeature[] tmpfeatures;
    String group = null, type;
    Vector visibleChecks = new Vector();

    for (int i = 0; i < av.alignment.getHeight(); i++)
    {
      if (av.alignment.getSequenceAt(i).getSequenceFeatures() == null)
      {
        continue;
      }

      tmpfeatures = av.alignment.getSequenceAt(i).getSequenceFeatures();
      int index = 0;
      while (index < tmpfeatures.length)
      {
        group = tmpfeatures[index].featureGroup;

        if (group == null || fr.featureGroups.get(group) == null ||
            ( (Boolean) fr.featureGroups.get(group)).booleanValue())
        {
          type = tmpfeatures[index].getType();
          if (!visibleChecks.contains(type))
          {
            visibleChecks.addElement(type);
          }
        }
        index++;
      }
    }

    Component[] comps;
    int cSize = featurePanel.getComponentCount();
    Checkbox check;
    //This will remove any checkboxes which shouldn't be
    //visible
    for (int i = 0; i < cSize; i++)
    {
      comps = featurePanel.getComponents();
      check = (Checkbox) comps[i];
      if (!visibleChecks.contains(check.getLabel()))
      {
        featurePanel.remove(i);
        cSize--;
        i--;
      }
    }

    if (fr.renderOrder != null)
    {
      //First add the checks in the previous render order,
      //in case the window has been closed and reopened
      for (int ro = fr.renderOrder.length - 1; ro > -1; ro--)
      {
        String item = fr.renderOrder[ro];

        if (!visibleChecks.contains(item))
        {
          continue;
        }

        visibleChecks.removeElement(item);

        addCheck(false, item);
      }
    }

    // now add checkboxes which should be visible,
    // if they have not already been added
    Enumeration en = visibleChecks.elements();

    while (en.hasMoreElements())
    {
      addCheck(groupsChanged, en.nextElement().toString());
    }

    featurePanel.setLayout(new GridLayout(featurePanel.getComponentCount(), 1,
                                          10, 5));
    featurePanel.validate();

    if (scrollPane != null)
    {
      scrollPane.validate();
    }

    itemStateChanged(null);
  }

  void addCheck(boolean groupsChanged, String type)
  {
    boolean addCheck;
    Component[] comps = featurePanel.getComponents();
    Checkbox check;
    addCheck = true;
    for (int i = 0; i < featurePanel.getComponentCount(); i++)
    {
      check = (Checkbox) comps[i];
      if (check.getLabel().equals(type))
      {
        addCheck = false;
        break;
      }
    }

    if (addCheck)
    {
      boolean selected = false;
      if (groupsChanged || av.featuresDisplayed.containsKey(type))
      {
        selected = true;
      }

      check = new MyCheckbox(type,
                             selected,
                             (fr.featureLinks != null &&
                              fr.featureLinks.containsKey(type))
          );

      check.addMouseListener(this);
      check.addMouseMotionListener(this);
      check.setBackground(fr.getColour(type));
      check.addItemListener(this);
      featurePanel.add(check);
    }
  }

  public void actionPerformed(ActionEvent evt)
  {
    for (int i = 0; i < featurePanel.getComponentCount(); i++)
    {
      Checkbox check = (Checkbox) featurePanel.getComponent(i);
      check.setState(!check.getState());
    }
    selectionChanged();
  }

  public void itemStateChanged(ItemEvent evt)
  {
    if (evt != null)
    {
      //Is the source a top level featureGroup?
      Checkbox source = (Checkbox) evt.getSource();
      if (fr.featureGroups.containsKey(source.getLabel()))
      {
        fr.featureGroups.put(source.getLabel(), new Boolean(source.getState()));
        ap.seqPanel.seqCanvas.repaint();
        if (ap.overviewPanel != null)
        {
          ap.overviewPanel.updateOverviewImage();
        }

        resetTable(true);
        return;
      }
    }
    selectionChanged();
  }

  void selectionChanged()
  {
    Component[] comps = featurePanel.getComponents();
    int cSize = comps.length;

    Object[][] tmp = new Object[cSize][3];
    int tmpSize = 0;
    for (int i = 0; i < cSize; i++)
    {
      Checkbox check = (Checkbox) comps[i];
      tmp[tmpSize][0] = check.getLabel();
      tmp[tmpSize][1] = fr.getColour(check.getLabel());
      tmp[tmpSize][2] = new Boolean(check.getState());
      tmpSize++;
    }

    Object[][] data = new Object[tmpSize][3];
    System.arraycopy(tmp, 0, data, 0, tmpSize);

    fr.setFeaturePriority(data);

    ap.paintAlignment(true);
  }

  MyCheckbox selectedCheck;
  boolean dragging = false;

  public void mousePressed(MouseEvent evt)
  {

    selectedCheck = (MyCheckbox) evt.getSource();

    if (fr.featureLinks != null
        && fr.featureLinks.containsKey(selectedCheck.getLabel())
        )
    {
      if (evt.getX() > selectedCheck.stringWidth + 20)
      {
        evt.consume();
      }
    }

  }

  public void mouseDragged(MouseEvent evt)
  {
    if ( ( (Component) evt.getSource()).getParent() != featurePanel)
    {
      return;
    }
    dragging = true;
  }

  public void mouseReleased(MouseEvent evt)
  {
    if ( ( (Component) evt.getSource()).getParent() != featurePanel)
    {
      return;
    }

    Component comp = null;
    Checkbox target = null;

    int height = evt.getY() + evt.getComponent().getLocation().y;

    if (height > featurePanel.getSize().height)
    {

      comp = featurePanel.getComponent(featurePanel.getComponentCount() - 1);
    }
    else if (height < 0)
    {
      comp = featurePanel.getComponent(0);
    }
    else
    {
      comp = featurePanel.getComponentAt(evt.getX(),
                                         evt.getY() +
                                         evt.getComponent().getLocation().y);
    }

    if (comp != null && comp instanceof Checkbox)
    {
      target = (Checkbox) comp;
    }

    if (selectedCheck != null
        && target != null
        && selectedCheck != target)
    {
      int targetIndex = -1;
      for (int i = 0; i < featurePanel.getComponentCount(); i++)
      {
        if (target == featurePanel.getComponent(i))
        {
          targetIndex = i;
          break;
        }
      }

      featurePanel.remove(selectedCheck);
      featurePanel.add(selectedCheck, targetIndex);
      featurePanel.validate();
      itemStateChanged(null);
    }
  }

  public void setUserColour(String feature, Color col)
  {
    fr.setColour(feature, col);
    featurePanel.removeAll();
    resetTable(false);
    ap.paintAlignment(true);
  }

  public void mouseEntered(MouseEvent evt)
  {}

  public void mouseExited(MouseEvent evt)
  {}

  public void mouseClicked(MouseEvent evt)
  {
    MyCheckbox check = (MyCheckbox) evt.getSource();

    if (fr.featureLinks != null
        && fr.featureLinks.containsKey(check.getLabel()))
    {
      if (evt.getX() > check.stringWidth + 20)
      {
        evt.consume();
        String link = fr.featureLinks.get(check.getLabel()).toString();
        ap.alignFrame.showURL(link.substring(link.indexOf("|") + 1),
                              link.substring(0, link.indexOf("|")));
      }
    }

    if (check.getParent() != featurePanel)
    {
      return;
    }

    if (evt.getClickCount() > 1)
    {
      new UserDefinedColours(this, check.getLabel(),
                             fr.getColour(check.getLabel()));
    }
  }

  public void mouseMoved(MouseEvent evt)
  {}

  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    fr.transparency = ( (float) (100 - transparency.getValue()) / 100f);
    ap.seqPanel.seqCanvas.repaint();

  }

  class MyCheckbox
      extends Checkbox
  {
    public int stringWidth;
    boolean hasLink;
    public MyCheckbox(String label, boolean checked, boolean haslink)
    {
      super(label, checked);

      FontMetrics fm = av.nullFrame.getFontMetrics(av.nullFrame.getFont());
      stringWidth = fm.stringWidth(label);
      this.hasLink = haslink;
    }

    public void paint(Graphics g)
    {
      if (hasLink)
      {
        g.drawImage(linkImage, stringWidth + 25, (
            getSize().height - linkImage.getHeight(this)) / 2,
                    this);
      }
    }
  }
}
