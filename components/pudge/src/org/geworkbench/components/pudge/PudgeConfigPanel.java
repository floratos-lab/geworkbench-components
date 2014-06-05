package org.geworkbench.components.pudge;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.io.Serializable;
import java.io.File;
import java.util.Map;

import javax.naming.OperationNotSupportedException;
import javax.swing.JFormattedTextField;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.util.BrowserLauncher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Configuration panel for Pudge analysis
 * 
 * @author mw2518
 * @version $Id$
 */
public class PudgeConfigPanel extends AbstractSaveableParameterPanel implements Serializable
{
    private Log log = LogFactory.getLog(this.getClass());
    private static final long serialVersionUID = 1L;
    private JFormattedTextField jobname = new JFormattedTextField();
    private JFormattedTextField natives = new JFormattedTextField();
	private JCheckBox academic = new JCheckBox("");
    private JFileChooser jfc = new JFileChooser();
	private String mainMessage = "<html>I am a non-profit/academic user and this server will<br>"
			+ "be used solely for educational purposes or for basic<br>"
			+ "research intended to advance scientific knowledge.</html>";
	private String popMessage = "<html>Some of the external applications are restricted by their authors<br>"
			+ "to academic users. Most of the applications are open to all users.</html>";
	private String linkMessage = "<html><u>Click to see the list of restricted software</u></html>";
	private String popURL = "http://wiki.c2b2.columbia.edu/honiglab_public/index.php?"
			+ "title=Software:Show_Results#Applications_Restricted_to_Academic_Users";

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
	jobname.setToolTipText("<html>Please use word characters(letters, numbers, underscores)." +
			"<br>Non-word characters will be replaced by underscores.</html>");
	builder.append("Job Name", jobname);
	JButton ob = new JButton("Browse for Native Structure");
	ob.addActionListener(new OpenAction());
	builder.append(ob, natives);
	JLabel lb = new JLabel(mainMessage);
	lb.setToolTipText(popMessage);

	JLabel url = new JLabel(linkMessage);
	url.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	url.setToolTipText(popMessage);
	url.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				try {
					BrowserLauncher.openURL(popURL);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public void mouseEntered(MouseEvent evt) {
				evt.getComponent().setForeground(new Color(0xC0, 0xC0, 0xF0));

			}

			public void mouseExited(MouseEvent evt) {
				evt.getComponent().setForeground(Color.BLACK);
			}
		});

	BorderLayout bl = new BorderLayout();
	bl.setVgap(5);
	JPanel lbPane = new JPanel(bl);
	lbPane.add(lb, BorderLayout.CENTER);
	lbPane.add(url, BorderLayout.SOUTH);
	builder.append(academic, lbPane);
	
	this.add(builder.getPanel(), BorderLayout.CENTER);
	setDefaultParameters();
    }

    class OpenAction implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    int ret = jfc.showOpenDialog(PudgeConfigPanel.this);
	    if (ret == JFileChooser.APPROVE_OPTION)
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
	academic.setSelected(false);
    }

    public String getjobnameValue()
    {
	return (String)jobname.getValue();
    }
    public String getnativesValue()
    {
	return (String)natives.getValue();
    }
    public boolean isAcademic()
    {
    	return academic.isSelected();
    }

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	public Map<Serializable, Serializable> getParameters() {
		log.warn(new OperationNotSupportedException("getParameters() not implemented"));
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 */
	public void setParameters(Map<Serializable, Serializable> parameters) {
		log.warn(new OperationNotSupportedException("setParameters() not implemented"));
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

}
