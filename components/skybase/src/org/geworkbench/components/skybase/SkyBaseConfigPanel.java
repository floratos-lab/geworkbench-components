package org.geworkbench.components.skybase;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * parameter panel for blast skybase
 * 
 * @author mw2518
 * @version $Id: SkyBaseConfigPanel.java,v 1.7 2009-06-19 19:23:50 jiz Exp $
 * 
 */
public class SkyBaseConfigPanel extends AbstractSaveableParameterPanel {
	private JTextField mincovEdit = new JTextField();
	private JTextField minsidEdit = new JTextField();
	private JTextField rphitsEdit = new JTextField();
	private JComboBox dbBox = new JComboBox(new String[]{"PDB60", "NESG"});

	private double mincov = 75, minsid = 30; 
	private int rphits = 10;

	private static class SerializedInstance implements Serializable {
		private static final long serialVersionUID = 1L;
		private double mincov = 75, minsid = 30; 
		private int rphits = 10;

		public SerializedInstance(double mincov, double minsid, int rphits) {
			this.mincov = mincov;
			this.minsid = minsid;
			this.rphits = rphits;
		}

		Object readResolve() throws ObjectStreamException {

			SkyBaseConfigPanel panel = new SkyBaseConfigPanel();
			panel.mincovEdit.setText(new Double(mincov).toString());
			panel.minsidEdit.setText(new Double(minsid).toString()); 
			panel.rphitsEdit.setText(new Integer(rphits).toString());
			return panel;
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new SerializedInstance((new Double(mincovEdit.getText())),
				(new Double(minsidEdit.getText())),new Integer(rphitsEdit.getText()));
	}

	public SkyBaseConfigPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		mincovEdit.addActionListener(parameterActionListener);
		minsidEdit.addActionListener(parameterActionListener);
		rphitsEdit.addActionListener(parameterActionListener);
		mincovEdit.addFocusListener(parameterActionListener);
		minsidEdit.addFocusListener(parameterActionListener);
		rphitsEdit.addFocusListener(parameterActionListener);
		dbBox.addActionListener(parameterActionListener);
	}

	/*
	 * add blast parameter list
	 */
	private void jbInit() throws Exception {
		FormLayout layout = new FormLayout(
				"right:max(40dlu;pref), 15dlu, max(70dlu;pref)", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Default SkyBase parameters");
		builder.append("The percentage of minimum sequence alignment coverage", mincovEdit);
		builder.append("The percentage of minimum sequence identity", minsidEdit);
		builder.append("The number of most similar hits to report", rphitsEdit);
		builder.append("Homology Models SkyBase", dbBox);

		this.add(builder.getPanel(), BorderLayout.CENTER);
		setDefaultParameters();
	}

	/*
	 * set default blast parameters
	 */
	public void setDefaultParameters() {
		mincovEdit.setFont(new Font("Sans Serif", Font.BOLD, 14));
		mincovEdit.setText(new Double(mincov).toString());
		 
		minsidEdit.setFont(new Font("Sans Serif", Font.BOLD, 14));
		minsidEdit.setText(new Double(minsid).toString());
		 
		rphitsEdit.setFont(new Font("Sans Serif", Font.BOLD, 14));
		rphitsEdit.setText(new Integer(rphits).toString());
		 
		dbBox.setSelectedIndex(0);
	}

	public Double getmincovValue() {
		try
		{
		    return new Double(mincovEdit.getText());
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public Double getminsidValue() {
		try
		{
		    return new Double(minsidEdit.getText());
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	 
	public Integer getrphitsValue() {
		try
		{
		    return new Integer(rphitsEdit.getText());
		}
		catch(Exception e)
		{
			return null;
		}		
		 
	}

	public String getdatabase() {
		return dbBox.getSelectedItem().toString();
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		revalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put("mincov", getmincovValue()); // int
		parameters.put("minsid", getminsidValue()); // int
		parameters.put("trphits", getrphitsValue()); // int
		parameters.put("database", getdatabase());

		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 */
	public void setParameters(Map<Serializable, Serializable> parameters) {
		if (getStopNotifyAnalysisPanelTemporaryFlag() == true)
			return;
		stopNotifyAnalysisPanelTemporary(true);
		Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if(key.equals("mincov"))mincovEdit.setText(value.toString()); // int
			else if(key.equals("minsid"))minsidEdit.setText(value.toString()); // int
			else if(key.equals("trphits"))rphitsEdit.setText(value.toString()); // int 
			else if(key.equals("database")) dbBox.setSelectedItem(value.toString());
		}
		stopNotifyAnalysisPanelTemporary(false);
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}	 

	@Override
    public ParamValidationResults validateParameters() {
		boolean status = true;
		String msg = "good";
		
		if (getmincovValue() == null || getmincovValue() < 0 || getmincovValue() > 100)
		{
			status  = false;
			msg = "Please enter 0 to 100 for \"minimum alignment coverage\".";
		}
		else if (getminsidValue() == null || getminsidValue()<0 || getminsidValue()>100)
		{
			status  = false;
			msg = "Please enter 0 to 100 for \"minimun sequence identity\".";
		}
		else if (getrphitsValue() == null || getrphitsValue() <= 0)
		{
			status  = false;
			msg = "Please enter a positive interger for \"most similar hits to report\".";
		}
			
		
		return new ParamValidationResults(status, msg);
    }
}
