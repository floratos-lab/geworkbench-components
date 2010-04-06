package org.geworkbench.components.genspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.JTableHeader;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.geworkbench.components.genspace.ui.LoginManager;



public class Outbox extends JPanel implements ActionListener
{       
    private String clientSideID = null;

    private static String url = RuntimeEnvironmentSettings.ISBU_SERVER.getHost();
    private static int urlport = RuntimeEnvironmentSettings.ISBU_SERVER.getPort();
     LoginManager logg = new LoginManager();
     String logged_user = "defaultuser";
     JButton button = null;
     JButton buttonShow = null;
     JButton refresh = null;
    private Hashtable msgID = new Hashtable(16);
       table tableInst1 = null;
       JTable tab1 = null;
     
        public Outbox()
        {
        	 if(logg != null){
        	    	logged_user = logg.getLoggedInUser();
         }
        	 System.out.println("inside constructor of outbox ****");
         button = new JButton ("Delete");
        button.addActionListener(this);
         buttonShow = new JButton ("Show");
        buttonShow.addActionListener(this);
        buttonShow.setVisible(false);
        button.setVisible(false);
       
         refresh = new JButton ("Refresh");
        refresh.addActionListener(this);
         tableInst1 = new table("To");
         tab1 = tableInst1.table;
        if(null != tab1){
            tab1.setRowSelectionAllowed(true);
            JTableHeader header = tab1.getTableHeader();
            if(null != header)
                header.setBackground(Color.BLUE);
            JScrollPane pane = new JScrollPane(tab1);
            add(pane, BorderLayout.WEST);
        }
        if (null != tableInst1){
            
        }
        add(button,BorderLayout.BEFORE_FIRST_LINE);
        add(buttonShow,BorderLayout.EAST);
        add(refresh,BorderLayout.WEST);
        try{
            //getAllOutBox();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
    }
        
              
        
        public void actionPerformed(ActionEvent ae){
            String msg = ae.getActionCommand();
            if(msg.equals("Delete")){
                if(null != tab1 && tab1.getSelectedRow() != -1){
                    if(JOptionPane.showConfirmDialog(null, "Are you sure that you want to delete the message?","GenSpace",JOptionPane.OK_CANCEL_OPTION)==0){
                    try{
                                
                                System.out.println(tab1.getSelectedRow()+"rectify row deleted");
                    			deleteFromOutbox(tab1.getSelectedRow());
                                tableInst1.removeRow(tab1.getSelectedRow());
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                        JOptionPane.showMessageDialog(null, "Message Deleted!","GenSpace",1);
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null, "No row selected. ","GenSpace",1);
                }
               
               
            }
            if(msg.equals("Show")){
                String showMessage = "No row selected.";
                if(tab1.getSelectedRow() != -1){
                	 showMessage ="From: "+ tab1.getValueAt(tab1.getSelectedRow(), 0)+ "        "+"\nDate: "+tab1.getValueAt(tab1.getSelectedRow(), 1)+"\nMessage: "+tab1.getValueAt(tab1.getSelectedRow(), 2);
                  	JTextArea text = new JTextArea(showMessage);
                  	text.setEditable(false); 
                  	text.setLineWrap(true);
                  	text.setWrapStyleWord(true);
                  	JScrollPane scrollit = new JScrollPane(text);
                     scrollit.setPreferredSize(new Dimension(300, 100));
                     JOptionPane.showMessageDialog(null, scrollit,"GenSpace",1);
                }
                else{
                	JOptionPane.showMessageDialog(null, showMessage,"GenSpace",1);
                }
            }
            if(msg.equals("Refresh")){
            	try{button.setVisible(true);
          	    buttonShow.setVisible(true);
                    getAllOutBox();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
       
        public ArrayList getAllOutBox() throws Exception{
        	String loggedin = null;
        	logged_user = logg.getLoggedInUser();	
        	ArrayList listBack = new ArrayList(); 
        	button.setVisible(true);
      	    buttonShow.setVisible(true);
        	/*if(cont.equals("logout")){
        		System.out.println("logout string passed here");
        		this.tab1.setVisible(false)	;
        	}*/
      	    //System.out.println("Hanged");
        	if(null == logged_user)
        	{	button.setVisible(false);
          	    buttonShow.setVisible(false);  
        	}
        	else{
        		tab1.setVisible(true);
        	  System.out.println("user inside outbox"+logged_user);
        	  button.setVisible(true);
        	  buttonShow.setVisible(true);
        	removeAllOutboxRows();
            int rowCount = -1;
            
            int lastPos = 0;
            String[] elements = new String[100];
            String serverString = null;
            int count = -1;
            if(null != logged_user){
            	loggedin = logged_user;
            }
            clientSideID = "select from outbox#"+loggedin;
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
            Iterator iter = listBack.iterator();
            while(iter.hasNext()){
                rowCount ++;
                count = 0;
                ;
                serverString = (String) iter.next();
             
                for(int i = 0 ; i< serverString.length(); i++){
                   
                    if(serverString.charAt(i) == '#'){
                        count ++;
                     
                        elements[count] = serverString.substring(lastPos,i );
                        lastPos = i+1;
                     
                    }tab1.removeAll();
                    if(count ==4 ){
                     if (null != tableInst1){
                             msgID.put(new Integer(rowCount),elements[1]);
                         
                            try{
                            tableInst1.insertRows(elements[2],elements[3],elements[4]);
                            }
                            catch(Exception e){
                            	e.printStackTrace();
                          
                            }
                            count = 0;
                            lastPos = 0;
                            break;
                        } 
                    }
                }
              
            }
        	 }
             return listBack;
        }
        private void deleteFromOutbox(int rownum) throws Exception{
               ArrayList listBack = new ArrayList();
           Hashtable msgID1 = new Hashtable(16);
            String deleteID = "delete from outbox";
            deleteID = deleteID.concat("#" + msgID.get(new Integer(rownum)));
            if(rownum != msgID.size()-1){
            	System.out.println(rownum+" "+msgID.size());
	            for(int i = rownum; i<msgID.size()-1; i++){
	            	msgID.put(new Integer(i), msgID.get(new Integer(i+1))) ;
	            }
            }
            //msgID = msgID1;
            System.out.println("Message id for outbox deletion sent to bambi = "+ msgID.get(new Integer(rownum)));
            try{
            	Socket s = new Socket(url,urlport);
                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                oos.writeObject(deleteID);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                s.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        public void removeAllOutboxRows(){
        	if(tab1.getRowCount()>-1){
        		System.out.println("Inside removeAlloutboxRows"+tab1.getRowCount() );
          		int x = tab1.getRowCount();
        		for(int i = x-1; i >=0;i--){
           		   this.tableInst1.removeRow(i);
            	}
        	}
        	else{
        		System.out.println("inside removealloutboxrows countt is  less than -1");
        	}
        }
        
}

class JTextFieldLimit extends PlainDocument
{
        private int limit;
        private boolean toUppercase = false;

        JTextFieldLimit(int limit)
        {
                super();
                this.limit = limit;
        }

        JTextFieldLimit(int limit, boolean upper)
        {
                super();
                this.limit = limit;
                toUppercase = upper;
        }

        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
        {
                if(str == null) return;

                if((getLength() + str.length()) <= limit)
                {
                        if(toUppercase)
                                str = str.toUpperCase();
                        super.insertString(offset, str, attr);
                }
        }
        
}

