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

import java.awt.*;
import java.awt.event.*;

public class JVDialog extends Dialog implements ActionListener
{
  AlignmentPanel ap;
  Panel buttonPanel;
  Button ok = new Button("Accept");
  Button cancel = new Button("Cancel");
  boolean accept = false;
  Frame owner;

  public JVDialog(Frame owner,
                  String title,
                  boolean modal,
                  int width, int height)
  {
      super(owner, title, modal);
      this.owner = owner;

      height += owner.getInsets().top + getInsets().bottom;

      setBounds(owner.getBounds().x
                + (owner.getSize().width - width) / 2,
                owner.getBounds().y
                + (owner.getSize().height - height) / 2,
                width, height);
  }

  void setMainPanel(Panel panel)
  {
    add(panel, BorderLayout.NORTH);

    buttonPanel = new Panel(new FlowLayout());

    buttonPanel.add(ok);
    buttonPanel.add(cancel);
    ok.addActionListener(this);
    cancel.addActionListener(this);

    add(buttonPanel, BorderLayout.SOUTH);

    pack();

  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == ok)
    {
      accept = true;
    }

    setVisible(false);
  }


}
