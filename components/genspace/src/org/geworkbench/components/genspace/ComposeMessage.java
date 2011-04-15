package org.geworkbench.components.genspace;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.geworkbench.components.genspace.ui.LoginManager;



public class  ComposeMessage extends JPanel implements ActionListener
{
    private static String clientSideID = null;
    private static String url = RuntimeEnvironmentSettings.ISBU_SERVER.getHost();
    private static int urlport = RuntimeEnvironmentSettings.ISBU_SERVER.getPort();
    LoginManager logg = new LoginManager();
    String logged_user = "default";

    //Declaration for JComboBox of Compose Message along with the JButtons 
    JComboBox users;
    JComboBox networks;
    JComboBox decision;
    JTextArea text1;
    JButton j1;
    JButton j2;
    JButton jd;
    JButton jr;
    
        public ComposeMessage()
        {
        //Fetch Logged in User's Username
        logged_user = logg.getLoggedInUser();	
              
        setSize(100, 200);
        
        //Inititalizing Comboboxes
        users = new JComboBox();
        networks = new JComboBox();
        decision = new JComboBox();
       
        //Initializing TextArea component
        text1 = new JTextArea(6,30);
        text1.setLineWrap(true);
        text1.setWrapStyleWord(true);
        text1.setEditable(true);          
        text1.setBackground(Color.white);
        text1.setDocument(new JTextFieldLimit(200));
        add(text1);
        
        //Initializing Button components
        j1 = new JButton("Send to User!");
        add(j1);
        j1.setBounds(50, 40, 200, 210);
        j1.addActionListener(this);
       
        j2 = new JButton("Send to Group!");
        add(j2);
        j2.addActionListener(this);    
       
        jd = new JButton("Decision!");
        jd.addActionListener(this);
        add(jd);
        jd.setVisible(false);
        
        jr = new JButton("Main Menu!");
        jr.addActionListener(this);
        add(jr);
        
        //Initializing Combobox Component
        decision = new JComboBox();
        decision.addItem("User");
        decision.addItem("Group");
        decision.setVisible(false);
        add(decision);
        
        
              
        try{
        	//Instantiates the visibility property of the GUI components
            loadingDecision();
            

        }
        catch(Exception e){
            e.printStackTrace();
        }
        }
        
        //Fucntion which reinstantiates the Compose Message component when a new user logs in to the system and also fetches his username
       public void userIsLogged(){
    	   jd.setVisible(true);
    	   decision.setVisible(true);
    	   logged_user = logg.getLoggedInUser();
    	   System.out.println(logged_user+"thhhe userr");
       }    
       
        public void actionPerformed(ActionEvent ae)
        {
            String msg = ae.getActionCommand();
            logged_user = logg.getLoggedInUser();
            boolean flag = true;

            //Action Performed based tag checks
             if(msg.equals("Send to User!"))
             {
                 try{
                	 //JTextArea Validation for possible SQL Injection Attacks
                	 String message = text1.getText().toString();
                	 String pattern = "[a-zA-Z0-9]{1}[a-zA-Z0-9#$@!%&*,.;'? ]*";
                	 Pattern p = Pattern.compile(pattern);
                	 
                	 Matcher m = p.matcher(message);
                	
                	 if(m.matches()==true)
                	 {
                     //Insertion into Inbox and Outbox tables for respective Users while sending message to a particular User
                     insertIntoMessageBoxU();
                     JOptionPane.showMessageDialog(null, "Message Sent!","GenSpace",1);
                	 }
                	 else
                	 {
                		 JOptionPane.showMessageDialog(null, "Invalid Characters used as Message","GenSpace",1);
                	 }
                 }
                 catch(Exception e){
                     e.printStackTrace();
                 }
             }
             else if(msg.equals("Send to Group!"))
             {
            	 try{
//            		JTextArea Validation for possible SQL Injection Attacks
            		 String message = text1.getText().toString();
                	 String pattern = "[a-zA-Z0-9]{1}[a-zA-Z0-9#$@!%&*,.;'? ]*";
                	 Pattern p = Pattern.compile(pattern);
                	 
                	 Matcher m = p.matcher(message);
                	
                	 if(m.matches())
                	 {
               		//Insertion into Inbox and Outbox tables for respective Users while sending message to a particular Group
                     insertIntoMessageBoxG();
                     JOptionPane.showMessageDialog(null, "Message Sent!","GenSpace",1);
                	 }
                	 else
                	 {
                		 JOptionPane.showMessageDialog(null, "Invalid Characters used as Message","GenSpace",1);
                	 }
                 }
                 catch(Exception e){
                     e.printStackTrace();
                 }
           	 }
             
             else if(msg.equals("Decision!"))
             {
             	
                 try{
                	 //Directs to Compose Message Section for User or Group depending upon the Combobox item selected
                	 String temp = decision.getSelectedItem().toString();
                	 if(logged_user.length() == 0)
                     {
                		 System.out.println("nulllllllllllllllllllll"+logged_user);
                    	 JOptionPane.showMessageDialog(null, "You need to login to view your messages","GenSpace",1);
                     }
                	 else if(logged_user.length() != 0)
                	 {
                		 
                	 if(temp.equals("User"))
                     {System.out.println("user nulllllllllllllllllllll "+logged_user);
                    	 users.removeAllItems();
                         text1.setVisible(true);
                         users.setVisible(true);
                         j1.setVisible(true);
                         jd.setVisible(false);
                         jr.setVisible(true);
                         decision.setVisible(false);
//                       Declaration of temporary Arraylists
                         ArrayList al = null;
                         //Calling the functions to populate Username and Networkname in Comboboxes
                         al = getAllUsers();
                     }
                     if(temp.equals("Group"))
                     {
                    	 ArrayList al2 = null;
                    	 try
                    	 {
                         al2 = getAllNetworks();
                    	 }
                    	 catch(Exception e)
                    	 {
                    		 e.printStackTrace();
                    	 }
                    	if(null != al2 && !al2.isEmpty())
                    	{
                    	 System.out.println("group nulllllllllllllllllllll"+logged_user);
                    	 networks.removeAllItems();
                         text1.setVisible(true);
                         networks.setVisible(true);
                         j2.setVisible(true);
                         jd.setVisible(false);
                         jr.setVisible(true);
                         decision.setVisible(false);
//                       Declaration of temporary Arraylists
                         ArrayList al1 = null;
                         al1 = getAllNetworks();
                    	}
                    	else
                    	{
                    		jd.setVisible(true);
                    		decision.setVisible(true);
                    		JOptionPane.showMessageDialog(null, "You are not affiliated with any Group!","GenSpace",1);
                    	}
                     }
                     }
                 }
                 catch(Exception e){
                     e.printStackTrace();
                 }
            	 }
             else if(msg.equals("Main Menu!"))
             {
            	 try{
            		 //Reinstantiates the Compose Message Object and it's Jbuttons, Jtextarea
                     loadingDecision();
                 }
                 catch(Exception e){
                     e.printStackTrace();    
                 }
            }             
        }
        
        public void loadingDecision() throws Exception{
            
        	System.out.println("Inside compose");
        	
         try{
        	 text1.setText("");
        	 jd.setVisible(true);
             decision.setVisible(true);       
             text1.setVisible(false);
             j1.setVisible(false);
             j2.setVisible(false);
             jr.setVisible(false);
             users.setVisible(false);
             networks.setVisible(false);
         }
         catch(Exception e){
             e.printStackTrace();
         }
     }
        //Function to handle message box insertions for a User
        private void insertIntoMessageBoxU() throws Exception{
            ArrayList listBack = new ArrayList();
            clientSideID = "insert into Messagebox";
            logged_user = logg.getLoggedInUser();	
            String text = text1.getText().toString();
            //Handling Empty Message
            if(text.length() == 0)
            {
            	clientSideID = clientSideID.concat("#"+ " " + "#" + logged_user + "#" + users.getSelectedItem());
            }
            else
            {
         clientSideID = clientSideID.concat("#"+ text + "#" + logged_user + "#" + users.getSelectedItem());
            }
         try{
        	 //Socket Connection Implementation to ISBU Server
             Socket s = new Socket(url,urlport);
             ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
             oos.writeObject(clientSideID);
             oos.flush();
             ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
             text1.setText("");
             loadingDecision();
         }
         catch(Exception e)
         {
         e.printStackTrace();
         }
     }
        
        //Function to handle message box insertions for a Group
        private void insertIntoMessageBoxG() throws Exception{
            ArrayList listBack = new ArrayList();
            clientSideID = "insert into Group Messagebox";
            logged_user = logg.getLoggedInUser();	
         String text = text1.getText().toString();
         //Handling Empty Message
         if(text.length() == 0)
         {
         	clientSideID = clientSideID.concat("#"+ " " + "#" + logged_user + "#" + networks.getSelectedItem());
         }
         else
         {
      clientSideID = clientSideID.concat("#"+ text + "#" + logged_user + "#" + networks.getSelectedItem());
         }
         try{
        	 //Socket Connection Implementation to ISBU Server
             Socket s = new Socket(url,urlport);
             ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
             oos.writeObject(clientSideID);
             oos.flush();
             ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
             text1.setText("");
             loadingDecision();
         }
         catch(Exception e){
             e.printStackTrace();
         }
     }
        
        ArrayList al = null;
       
        //Populates the User JComboBox with Usernames of all the Users in the System
        private ArrayList getAllUsers() throws Exception{
           
            ArrayList listBack = new ArrayList();
            ArrayList listBack1 = new ArrayList();
            logged_user = logg.getLoggedInUser();	
            int lastPos = 0;
            String[] elements = new String[100];
            String serverString = null;
            String size = null;
            int count = -1;
           
            clientSideID = "select users from registration#"+logged_user;
            try{
                Socket s = new Socket(url,urlport);
                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                oos.writeObject(clientSideID);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                listBack = (ArrayList) ois.readObject();
                s.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            count = 0;
            Iterator iter = listBack.iterator();
            do{
                //count=0;
                serverString = (String) iter.next();

                if(serverString.length() != 0 && null != serverString && serverString.length()>0)
                {
                   users.addItem(serverString);
                }
                count++;
            }while(iter.hasNext());

            add(users);
            return listBack;
        }
        
        //Populates the Networks JComboBox with Networks affiliated with the current logged in User
        private ArrayList getAllNetworks() throws Exception{
            
            ArrayList listBack = new ArrayList();
            ArrayList listBack1 = new ArrayList();

            int lastPos = 0;
            String[] elements = new String[100];
            logged_user = logg.getLoggedInUser();
            String serverString = null;
            String size = null;
            int count = -1;

           
            clientSideID = "select network#"+logged_user;
            try{
 
                Socket s = new Socket(url,urlport);
                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                oos.writeObject(clientSideID);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                listBack = (ArrayList) ois.readObject();
                s.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            count = 0;
            Iterator iter = listBack.iterator();
            do{
                serverString = (String) iter.next();

                if(serverString.length() != 0 && null != serverString && serverString.length()>0)
                {
                   networks.addItem(serverString);
                }
                count++;
            }while(iter.hasNext());

            add(networks);
            return listBack;
        }
}
