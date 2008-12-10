package org.geworkbench.components.skybase;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.swing.JFormattedTextField;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * parameter panel for blast skybase
 * 
 * @author mw2518
 * @version $Id: SkyBaseConfigPanel.java,v 1.3 2008-12-10 20:33:43 wangm Exp $
 * 
 */
public class SkyBaseConfigPanel extends AbstractSaveableParameterPanel
		implements Serializable {
	private JFormattedTextField mincovEdit = new JFormattedTextField();
	private JFormattedTextField minsidEdit = new JFormattedTextField();
	private JFormattedTextField rphitsEdit = new JFormattedTextField();

	private int mincov = 75, minsid = 30, rphits = 10;

	private static class SerializedInstance implements Serializable {
		private static final long serialVersionUID = 1L;
		private int mincov = 75, minsid = 30, rphits = 10;

		public SerializedInstance(int mincov, int minsid, int rphits) {
			this.mincov = mincov;
			this.minsid = minsid;
			this.rphits = rphits;
		}

		Object readResolve() throws ObjectStreamException {

			SkyBaseConfigPanel panel = new SkyBaseConfigPanel();
			panel.mincovEdit.setValue(mincov);
			panel.minsidEdit.setValue(minsid);
			panel.rphitsEdit.setValue(rphits);
			return panel;
		}
	}

	Object writeReplace() throws ObjectStreamException {
		return new SerializedInstance((Integer) mincovEdit.getValue(),
				(Integer) minsidEdit.getValue(), (Integer) rphitsEdit
						.getValue());
	}

	public SkyBaseConfigPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * add blast parameter list
	 */
	private void jbInit() throws Exception {
		FormLayout layout = new FormLayout(
				"right:max(40dlu;pref), 8dlu, max(60dlu;pref)", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Default SkyBase parameters");
		builder.append("% minimum alignment coverage", mincovEdit);
		builder.append("% minimum sequence identity", minsidEdit);
		builder.append("most similar hits to report", rphitsEdit);

		this.add(builder.getPanel(), BorderLayout.CENTER);
		setDefaultParameters();
	}

	/*
	 * set default blast parameters
	 */
	public void setDefaultParameters() {
		mincovEdit.setFont(new Font("Sans Serif", Font.BOLD, 14));
		mincovEdit.setValue(mincov);
		// mincovEdit.setEditable(false);
		mincovEdit.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		minsidEdit.setFont(new Font("Sans Serif", Font.BOLD, 14));
		minsidEdit.setValue(minsid);
		// minsidEdit.setEditable(false);
		minsidEdit.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		rphitsEdit.setFont(new Font("Sans Serif", Font.BOLD, 14));
		rphitsEdit.setValue(rphits);
		// rphitsEdit.setEditable(false);
		rphitsEdit.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
	}

	public int getmincovValue() {
		return ((Integer) mincovEdit.getValue()).intValue();
	}

	public int getminsidValue() {
		return ((Integer) minsidEdit.getValue()).intValue();
	}

	public int getrphitsValue() {
		return ((Integer) rphitsEdit.getValue()).intValue();
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		revalidate();
	}
}
