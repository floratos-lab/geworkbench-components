/**
 * 
 */
package org.geworkbench.components.cagrid.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.util.Util;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * URL label listener for caGrid service panel (replacing the old
 * DispatcherLabelListener and IndexServiceListener).
 * 
 * @author zji
 * @version $Id$
 * 
 */
public class UrlLabelListener implements MouseListener {
	private GridServicePanel gridServicePanel = null;
	private String GRID_HOST_KEY = null;
	private int urlId = 0;

	UrlLabelListener(final GridServicePanel gridServicePanel,
			final String GRID_HOST_KEY, final String DEFAULT_URL_KEY,
			int urlId) {
		this.gridServicePanel = gridServicePanel;
		this.GRID_HOST_KEY = GRID_HOST_KEY;
		this.urlId = urlId;
		gridServicePanel.url[urlId] = System.getProperty(DEFAULT_URL_KEY);
		readProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		final JDialog urlDialog = new JDialog();
		DefaultFormBuilder indexServerPanelBuilder = new DefaultFormBuilder(
				new FormLayout("right:20dlu"));

		readProperties();

		final JTextField hostField = new JTextField(gridServicePanel.url[urlId]);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gridServicePanel.url[urlId] = hostField.getText();

				saveProperties();

				urlDialog.dispose();

			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				urlDialog.dispose();
			}
		});

		/* add to button panel */
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		/* the builder */
		indexServerPanelBuilder.appendColumn("5dlu");
		indexServerPanelBuilder.appendColumn("250dlu");

		indexServerPanelBuilder.append("URL", hostField);

		JPanel indexServerPanel = new JPanel(new BorderLayout());
		indexServerPanel.add(indexServerPanelBuilder.getPanel());
		indexServerPanel.add(buttonPanel, BorderLayout.SOUTH);
		urlDialog.add(indexServerPanel);
		urlDialog.setModal(true);
		urlDialog.pack();
		Util.centerWindow(urlDialog);
		urlDialog.setVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	private void readProperties() {
		PropertiesManager pm = PropertiesManager.getInstance();
		String savedHost = null;
		try {
			savedHost = pm.getProperty(this.getClass(), GRID_HOST_KEY,
					gridServicePanel.url[urlId]);
			if (!StringUtils.isEmpty(savedHost)) {
				gridServicePanel.url[urlId] = savedHost;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveProperties() {

		PropertiesManager properties = PropertiesManager.getInstance();
		try {
			properties.setProperty(this.getClass(), GRID_HOST_KEY,
					String.valueOf(gridServicePanel.url[urlId]));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
