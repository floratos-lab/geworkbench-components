package org.geworkbench.components.genspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.geworkbench.components.genspace.ui.LoginManager;



class table extends JTable{
    DefaultTableModel model = null;
    JTable table;
    public table(){
        table = initComponents("Name");
    }
    public table(String msg){
        table = initComponents(msg);
    }
    public JTable initComponents(String msg){
        String[] columnNames = {msg,"Date","Message"};
        Object[][] data = new String[0][0];
        model = new DefaultTableModel(data, columnNames);
        table = new JTable(model){
                                    public Component prepareRenderer (TableCellRenderer renderer, int Index_Row, int Index_Col){
                                        Component comp = super.prepareRenderer(renderer, Index_Row, Index_Col);
                                        JComponent jcomp = (JComponent) comp;

                                        if(Index_Row %2 == 0 && !isCellSelected(Index_Row,Index_Col)){
                                            comp.setBackground(Color.lightGray);  
                                            }
                                        else{                                   	                                      
                                        	comp.setBackground(Color.white);
                                        }
                                        if(comp == jcomp){
                                            jcomp.setToolTipText((String) getValueAt(Index_Row, Index_Col));
                                        }
                                        return comp;
                                    }
        };
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        TableColumn col1 = table.getColumnModel().getColumn(0);
        col1.setPreferredWidth(10);
       
        return table;
    }
    public void removeRow(int rowNum){
        model.removeRow(rowNum);
    }
    public void insertRows(String first, String second, String third){
        if(null!=table){
            model.insertRow(table.getRowCount(),new Object[] {first,second, third});
        }
    }
  
}

public class Inbox extends JPanel implements ActionListener
{       
    private String clientSideID = null;
   
    private static String url = RuntimeEnvironmentSettings.ISBU_SERVER.getHost();
    private static int urlport = RuntimeEnvironmentSettings.ISBU_SERVER.getPort();
    LoginManager logg = new LoginManager();
    String logged_user = "default";
    private Hashtable msgID = new Hashtable(16);
    table tableInst = null;
    public JTable tab = null;
    JButton button = null;
    JButton inboxmsg = null;
    JButton showMessage = null;
   
        public Inbox()
        {
        	//setLayout(new BorderLayout());
        	if(logg != null){
    	    	logged_user = logg.getLoggedInUser();
    	    }
        	 	    
        button = new JButton ("Delete");
        
        button.addActionListener(this);
         showMessage = new JButton ("Show");
         showMessage.addActionListener(this);
         inboxmsg = new JButton ("Refresh");
        inboxmsg.addActionListener(this);
        tableInst = new table("From");
        //if(null == tab){
        	tab = tableInst.table;
        //}
        button.setVisible(false);
	    
	    showMessage.setVisible(false);
        if(null != tab){
            tab.setRowSelectionAllowed(true);
            JTableHeader header = tab.getTableHeader();
            if(null != header)
                header.setBackground(Color.BLUE);
            JScrollPane pane = new JScrollPane(tab);
            add(pane, BorderLayout.WEST);
        }
        if (null != tableInst){

        }
        add(button,new GridLayout(2,2));
        add(showMessage,BorderLayout.EAST);
        add(inboxmsg,BorderLayout.EAST);

        try{
           // getAllInbox();
        }
        catch(Exception e){
            e.printStackTrace();
        }
   }
        public void actionPerformed(ActionEvent ae){
            String msg = ae.getActionCommand();
            if(msg.equals("Delete")){
                if(null != tab && tab.getSelectedRow() != -1){
                    if(JOptionPane.showConfirmDialog(null, "Are you sure that you want to delete the message?","GenSpace",JOptionPane.OK_CANCEL_OPTION)==0){
                    try{
                                
                                deleteFromInbox(tab.getSelectedRow());
                                tableInst.removeRow(tab.getSelectedRow());
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
               //JScrollPane scrollit = null
                if(tab.getSelectedRow() != -1){
                	//String str = wrapMessage(tab.getValueAt(tab.getSelectedRow(), 2).toString());
                    showMessage ="From: "+ tab.getValueAt(tab.getSelectedRow(), 0)+ "        "+"\nDate: "+tab.getValueAt(tab.getSelectedRow(), 1)+"\nMessage: "+tab.getValueAt(tab.getSelectedRow(), 2);
                 	JTextArea text = new JTextArea(showMessage);
                 	text.setEditable(false); 
                 	text.setLineWrap(true);
                 	text.setWrapStyleWord(true);
                 	JScrollPane scrollit = new JScrollPane(text);
                    scrollit.setPreferredSize(new Dimension(300, 100));
                    JOptionPane.showMessageDialog(null, scrollit,"GenSpace",1);
                    //JOptionPane.showMessageDialog(null, showMessage,"GenSpace",1);
                 }
                else{
                //JOptionPane p = new 
                JOptionPane.showMessageDialog(null, showMessage,"GenSpace",1);
                }
            }
            if(msg.equals("Refresh")){
            	try{
                    getAllInbox();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        
        
       
        public ArrayList getAllInbox() throws Exception{
           
        	String loggedin = null;
        	ArrayList listBack = new ArrayList(); 
        	logged_user = logg.getLoggedInUser();	
        	if(null == logged_user){
        		button.setVisible(false);
        		showMessage.setVisible(false);
             	loggedin = logged_user;
             	JOptionPane.showMessageDialog(null, "You need to login to view your messages","GenSpace",1);
        	}
        	else{
        		System.out.println("USER LOGGED IS "+ logged_user);
        	/*if(logged_user.isEmpty() || logged_user.equals(" ")){
        		System.out.println("SPACE USER");
        		this.tab.setVisible(false);
        		this.removeAllInboxRows();
        		ta.revalidate();
        		tableInst.revalidate();
        	}
        	else{*/
        	//	tab.setVisible(true);
        	/*if((tab.getRowCount() == 0)){
            		System.out.println("inside tab visible");
            		tab.setVisible(false);
            }*/
        	  
        	  button.setVisible(true);
        	  showMessage.setVisible(true);
        	removeAllInboxRows();
            int rowCount = -1;
            int lastPos = 0;
            String[] elements = new String[100];
            String serverString = null;
            int count = -1;
            if(null != logged_user){
            	loggedin = logged_user;
            }
            clientSideID = "select from inbox#"+loggedin;
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
                serverString = (String) iter.next();     
                for(int i = 0 ; i< serverString.length(); i++){
                    if(serverString.charAt(i) == '#'){
                        count ++;
                        elements[count] = serverString.substring(lastPos,i );
                        lastPos = i+1;
                    }
                    if(count ==4 ){
                     if (null != tableInst){
                             msgID.put(new Integer(rowCount),elements[1]);
                            tableInst.insertRows(elements[2],elements[3],elements[4]);
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
        private void deleteFromInbox(int rownum) throws Exception{
            String deleteID = "delete from inbox";
            deleteID = deleteID.concat("#" + msgID.get(new Integer(rownum)));
            if(rownum != msgID.size()-1){
            	System.out.println(rownum+" "+msgID.size());
	            for(int i = rownum; i<msgID.size()-1; i++){
	            	msgID.put(new Integer(i), msgID.get(new Integer(i+1))) ;
	            }
            }
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
        
        public void removeAllInboxRows(){
        	
        	if(tab.getRowCount()>-1){
        		System.out.println("Inside removeAllInboxRows"+tab.getRowCount() );
        		int x = tab.getRowCount();
        		for(int i = x-1; i >=0;i--){
           		   tableInst.removeRow(i);
            	}
        	}
        	
        	else{
        		System.out.println("inside removealloutboxrows countt is  less than -1");
        	}
          	
        	
        }
       
}
