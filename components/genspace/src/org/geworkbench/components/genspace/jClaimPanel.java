package org.geworkbench.components.genspace;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
/**
 * This is an example geWorkbench component.
 *
 * 
 */
// This annotation lists the data set types that this component accepts.
// The component will only appear when a data set of the appropriate type is selected.
//@AcceptTypes({DSMicroarraySet.class})
public class jClaimPanel extends JPanel implements VisualPlugin, ActionListener {

    private JButton b;
    String jarFile = "components/genspace/lib/genspace-communicator.jar";
    //String jarFile = "components/genspaceLogin/lib/jclaim-genspace.jar";
    //String jarFile = "genspace-communicator.jar";
    
    /**
     * Constructor
     */
    public jClaimPanel() {
        b = new JButton("Start jClaimPanel");
        add(b);
        b.addActionListener(this);
        
        
    }
    
    /**
     * Action Listener - either called when the button is clicked or
     * when this has registered with a genspaceLogin component and 
     * the user logs in or registers.
     * 
     * TODO: should we use a different method or ActionEvent instead
     *       of reusing the one for button clicks?
     */
    public void actionPerformed(ActionEvent e) {
    	// before we start JClaim, make sure they're logged into genSpace
    	if (!genspaceLogin.isLoggedIn)
    	{
    		// if they're not logged in, then pop up the login window
    	    genspaceLogin loginHandler = new genspaceLogin();
            // register with the loginHandler
            loginHandler.addActionListener(this);
    		loginHandler.showFrame(); 

    		// note that we don't have to start jClaim here; the genspaceLogin
    		// object will call this method again, at which time isLoggedIn will be true
    	}
    	else
    	{
    		System.out.println("Starting jClaim as: " + genspaceLogin.genspaceLogin);
    		// if they are logged in, then start jClaim
    		ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarFile, genspaceLogin.genspaceLogin);  
    		try {
    			Process p = pb.start();
    		} catch (Exception ex) {
    			// TODO Auto-generated catch block
    			ex.printStackTrace();
    		}
    	}
    }
    
    /**
     * This method fulfills the contract of the {@link VisualPlugin} interface.
     * It returns the GUI component for this visual plugin.
     */
    public Component getComponent() {
        // In this case, this object is also the GUI component.
        return this ;
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

    /*
     * Just a little test method.
     */
    public static void main(String[] args)
    {
    	JFrame frame = new JFrame();
    	frame.add(new jClaim());
    	frame.setSize(400,200);
    	frame.setLocation(0,0);
    	frame.setResizable(false);
    	frame.setVisible(true);
    }
}
