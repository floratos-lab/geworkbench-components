package org.geworkbench.components.genspace;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.geworkbench.engine.config.VisualPlugin;


public class Message extends JPanel implements VisualPlugin
{
        //Instantiation of the Message Tabbed Pane
        private JTabbedPane tabbedPanel = new JTabbedPane();
        ComposeMessage compose  = null;
          JTabbedPane jtp =  tabbedPanel;
          JScrollPane jsp = null;
         Inbox in = new Inbox();
         Outbox out = new Outbox();;
        
       
       public Message()
        { 
    	   
            initComponents();  
        }
       
        //Called from the LoginManager.java when a new user logs into the System
        public void startMessage(){
            compose.userIsLogged();
        }
       

      
        //Instantiates the Tabbed Panes with the concerned ComposeMessage,Inbox and Outbox classes
        public void initComponents()
        {       
                setLayout(new BorderLayout());
                compose = new ComposeMessage();
               
                jtp.addTab("Compose", compose);
                jtp.addTab("Inbox",new Inbox());
                jtp.addTab("Outbox",new Outbox());
                jsp = new JScrollPane(jtp);
                jsp.setBounds(1,1,1000,1000);
                add(jsp);
//                in.addActionListener();
        }
       
    
       
        //Required for VisualPlugin
        public Component getComponent()
        {
            return this;
        }

}
