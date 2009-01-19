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
package jalview.jbgui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class GCutAndPasteTransfer
    extends JInternalFrame
{
  protected JTextArea textarea = new JTextArea();
  protected JScrollPane scrollPane = new JScrollPane();
  BorderLayout borderLayout1 = new BorderLayout();
  JMenuBar editMenubar = new JMenuBar();
  JMenu editMenu = new JMenu();
  JMenuItem copyItem = new JMenuItem();
  JMenuItem pasteMenu = new JMenuItem();
  BorderLayout borderLayout2 = new BorderLayout();
  protected JPanel inputButtonPanel = new JPanel();
  protected JButton ok = new JButton();
  JButton cancel = new JButton();
  JMenuItem selectAll = new JMenuItem();
  JMenu jMenu1 = new JMenu();
  JMenuItem save = new JMenuItem();

  /**
   * Creates a new GCutAndPasteTransfer object.
   */
  public GCutAndPasteTransfer()
  {
    try
    {
      setJMenuBar(editMenubar);
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  private void jbInit()
      throws Exception
  {
    scrollPane.setBorder(null);
    ok.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    ok.setText("New Window");
    ok.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed(e);
      }
    });
    cancel.setText("Close");
    cancel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });
    textarea.setBorder(null);

    selectAll.setText("Select All");
    selectAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.
        KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
    selectAll.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        selectAll_actionPerformed(e);
      }
    });
    jMenu1.setText("File");
    save.setText("Save");
    save.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.
        KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
    save.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        save_actionPerformed(e);
      }
    });
    copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.
        KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
    pasteMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.
        KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
    editMenubar.add(jMenu1);
    editMenubar.add(editMenu);
    textarea.setFont(new java.awt.Font("Monospaced", Font.PLAIN, 12));
    textarea.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        textarea_mousePressed(e);
      }
    });
    editMenu.setText("Edit");
    pasteMenu.setText("Paste");
    pasteMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        pasteMenu_actionPerformed(e);
      }
    });
    copyItem.setText("Copy");
    copyItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        copyItem_actionPerformed(e);
      }
    });
    this.getContentPane().setLayout(borderLayout2);
    scrollPane.setBorder(null);
    scrollPane.getViewport().add(textarea, null);
    editMenu.add(selectAll);
    editMenu.add(copyItem);
    editMenu.add(pasteMenu);
    this.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
    inputButtonPanel.add(ok);
    inputButtonPanel.add(cancel);
    jMenu1.add(save);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void textarea_mousePressed(MouseEvent e)
  {

  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void copyItem_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void pasteMenu_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void ok_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void cancel_actionPerformed(ActionEvent e)
  {
  }

  public void selectAll_actionPerformed(ActionEvent e)
  {
    textarea.selectAll();
  }

  public void save_actionPerformed(ActionEvent e)
  {

  }
}
