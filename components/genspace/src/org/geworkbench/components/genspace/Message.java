package org.geworkbench.components.genspace;

import java.awt.*;

import org.geworkbench.components.genspace.Inbox;
import org.geworkbench.components.genspace.Outbox;
import org.geworkbench.components.genspace.ComposeMessage;

import org.apache.ojb.odmg.NarrowTransaction;
import org.geworkbench.components.genspace.ui.LoginManager;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.*;

import org.geworkbench.builtin.projects.remoteresources.query.GeWorkbenchCaARRAYAdaptor;
import org.geworkbench.engine.config.*;
import java.awt.Component;
import org.jgraph.*;
import org.jgraph.graph.*;

import org.jgraph.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.*;
import java.net.*;


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
