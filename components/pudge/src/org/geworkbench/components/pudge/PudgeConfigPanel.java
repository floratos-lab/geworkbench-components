package org.geworkbench.components.pudge;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.Serializable;
import java.io.File;
import java.util.Map;

import javax.naming.OperationNotSupportedException;
import javax.swing.JFormattedTextField;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Configuration panel for Pudge analysis
 * 
 * @author mw2518
 * @version $Id: PudgeConfigPanel.java,v 1.3 2009-06-19 19:23:34 jiz Exp $
 */
public class PudgeConfigPanel extends AbstractSaveableParameterPanel implements Serializable
{
    private Log log = LogFactory.getLog(this.getClass());
    private static final long serialVersionUID = 1L;
    private JFormattedTextField jobname = new JFormattedTextField();
    private JFormattedTextField natives = new JFormattedTextField();
    private JFileChooser jfc = new JFileChooser();

    public PudgeConfigPanel()
    {
	try {
	    jbInit();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void jbInit() throws Exception
    {
	FormLayout layout = new FormLayout(
			   "right:max(40dlu;pref), 8dlu, max(60dlu;pref)", "");
	DefaultFormBuilder builder = new DefaultFormBuilder(layout);
	builder.setDefaultDialogBorder();
	builder.appendSeparator("Default Pudge parameters");
	builder.append("Job Name", jobname);
	JButton ob = new JButton("Browse for Native Structure");
	ob.addActionListener(new OpenAction());
	builder.append(ob, natives);
	
	this.add(builder.getPanel(), BorderLayout.CENTER);
	setDefaultParameters();
    }

    class OpenAction implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    int ret = jfc.showOpenDialog(PudgeConfigPanel.this);
	    if (ret == jfc.APPROVE_OPTION)
	    {
		File file = jfc.getSelectedFile();
		natives.setText(file.getAbsolutePath());
		natives.setValue(file.getAbsolutePath());
	    }
	}
    }

    public void setDefaultParameters()
    {
	jobname.setFont(new Font("Sans Serif", Font.BOLD, 14));
	jobname.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
	jobname.setValue("");
	natives.setFont(new Font("Sans Serif", Font.BOLD, 14));
	natives.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
	natives.setValue("");
    }

    public String getjobnameValue()
    {
	return (String)jobname.getValue();
    }
    public String getnativesValue()
    {
	return (String)natives.getValue();
    }

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	public Map<Serializable, Serializable> getParameters() {
		// TODO Auto-generated method stub
		log.error(new OperationNotSupportedException("Please implement getParameters()"));
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 */
	public void setParameters(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		log.error(new OperationNotSupportedException("Please implement setParameters()"));
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

}
