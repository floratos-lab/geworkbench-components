package org.geworkbench.components.genspace;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GoMapping;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.events.ProjectEvent;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatisticsImpl;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.security.MessageDigest;
import java.util.ArrayList;

/**
 * This is an example geWorkbench component.
 *
 * 
 */
// This annotation lists the data set types that this component accepts.
// The component will only appear when a data set of the appropriate type is selected.
//@AcceptTypes({DSMicroarraySet.class})
public class genspaceLogin extends JPanel implements VisualPlugin, ActionListener {

    private JLabel l1, l2, l3;
    private JTextField tf;
    private JPasswordField pf;
    private JButton b1, b2, b3;
    private String filename = "genspace.txt";
    private String hash = "MD5";

    // to indicate whether the user is logged in or not
    public static boolean isLoggedIn = false;
    
    // the user's login ID
    public static String genspaceLogin = null;
     
    // a list of all ActionListeners waiting for login events
    private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
    
    // the surrounding JFrame when this is used as a standalone window
    private JFrame frame;
    
    /**
     * Constructor
     */
    public genspaceLogin() {
        l1 = new JLabel("Login");
        l2 = new JLabel("Password");
        l3 = new JLabel("");
        tf = new JTextField(10);
        pf = new JPasswordField(10);
        b1 = new JButton("Login");
        b2 = new JButton("Clear");
        b3 = new JButton("Register");
        l3.setVisible(false);
        add(l1);
        add(tf);
        add(l2);
        add(pf);
        add(b1);
        add(b2);
        add(b3);
        add(l3);
        b1.addActionListener(this);
        b2.addActionListener(this);
        b3.addActionListener(this);
    }
    
    /**
     * Action Listener
     */
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == b1) {
    		if (check() == 0) {
    			l3.setText("Login Successful");
    			isLoggedIn = true;
    		}
    		else {
    			l3.setText("Login Failed");
    			isLoggedIn = false;
    		}
    		l3.setVisible(true);
    	}
    	else if (e.getSource() == b2) {
    		tf.setText("");
    		pf.setText("");
    		l3.setVisible(false);
    	}
    	else if (e.getSource() == b3) {
    		if (create() == 0) {
    			l3.setText("Login Created");
    			isLoggedIn = true;
    		}
    		else {
    			l3.setText("Login Creation Failed");
    			isLoggedIn = false;
    		}
    		l3.setVisible(true);
    	}	

    	// if they're logged in, do some bookkeeping and cleanup
    	if (isLoggedIn)
    	{
    		// may want a different event but it doesn't matter for now
    		notifyActionListeners(e);
    		// TODO: make sure this doesn't break everything when it's a genspace plugin
    		//if (frame != null) frame.setVisible(false);
    		/*String jarFile = "components/genspace/lib/genspace-communicator.jar";
    		ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarFile, genspaceLogin);  
    		try {
    			Process p = pb.start();
    		} catch (Exception ex) {
    			// TODO Auto-generated catch block
    			ex.printStackTrace();
    		}*/
    	}
    }
    
     
    /**
     * Checks if a login is valid
     * @return 0 is valid, -1 otherwise
     */
    private int check()
    {
    	return check(tf.getText(), new String(pf.getPassword()));
    }
    
    /**
     * Checks if a login is valid
     * @return 0 is valid, -1 otherwise
     */
    public int check(String login, String pass) {
    	try{
    		String encrypted_pass = getEncodedString(pass);
    		BufferedReader br = new BufferedReader(new FileReader(filename));
    		String file_login = br.readLine();
    		String file_encrypted_pass = br.readLine();
    		br.close();
    		if (login.equals(file_login) && encrypted_pass.equals(file_encrypted_pass)) {
    			genspaceLogin = login;
    			return 0;
    		}
    		else {
    			return -1;
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		return -1;
    	}
    }

    /**
     * Converts a byte stream to a String
     * @param b The byte stream
     * @return The converted String
     */
    public String bytesToHex(byte[] b) {
		char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		StringBuffer buf = new StringBuffer();
		for (int j=0; j<b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}
		return buf.toString();
	}
    
    /**
     * Creates a Hash
     * @param clearText The Clear-Text String
     * @return The Hashed String
     */
    private String getEncodedString(String clearText) {
    	try {
	    	MessageDigest md = MessageDigest.getInstance(hash);
	    	md.update(clearText.getBytes());
	    	byte[] output = md.digest();
	    	return bytesToHex(output);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return "";
    }
    
    /**
     * Creates a Login
     * @return 0 If Successful, -1 otherwise
     */
    private int create() 
    {
    	return create(tf.getText(), new String(pf.getPassword()));
    }

    /**
     * Creates a Login
     * @return 0 If Successful, -1 otherwise
     */
    public int create(String login, String pass) {
    	try{
    		String encrypted_pass = getEncodedString(pass);
    		FileWriter fw = new FileWriter(filename);
    		fw.write(login);
    		fw.write("\n");
    		fw.write(encrypted_pass);
    		fw.close();
    		genspaceLogin = login;
    		return 0;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		return -1;
    	}
    }

    /**
     * This method fulfills the contract of the {@link VisualPlugin} interface.
     * It returns the GUI component for this visual plugin.
     */
    public Component getComponent() {
        // In this case, this object is also the GUI component.
        return this;
    }

    /**
     * This is a <b>Subscribe</b> method. The annotation before the method alerts
     * the engine that it should route published objects to this method.
     * The type of objects that are routed to this method are indicated by the first parameter of the method.
     * In this case, it is {@link ProjectEvent}.
     *
     * @param event  the received object.
     * @param source the entity that published the object.
     */
    @Subscribe
    public void receive(ProjectEvent event, Object source) {
            
        }
    
    /**
     * For notifying other components of a login event.
     */
    public void addActionListener(ActionListener al)
    {
    	listeners.add(al);
    }
    
    public void notifyActionListeners(ActionEvent e)
    {
    	for (ActionListener al : listeners)
    	{
    		al.actionPerformed(e);
    	}
    }
    
    
    /**
     * For when we want to show this panel in its own frame.
     */
    public void showFrame()
    {
    	frame = new JFrame();
    	frame.add(this);
    	frame.setSize(400,200);
    	frame.setLocation(0,0);
    	frame.setResizable(false);
    	frame.setTitle("Please login or register before starting jClaim");
    	frame.setVisible(true);    	
    }

    public static void main(String[] args)
    {
    	genspaceLogin login = new genspaceLogin();
    	login.showFrame();
    }
}
