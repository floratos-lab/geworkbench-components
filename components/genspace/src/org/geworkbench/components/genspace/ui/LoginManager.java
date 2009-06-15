//package genspace.ui;
package org.geworkbench.components.genspace.ui;

import org.geworkbench.components.genspace.RuntimeEnvironmentSettings;
import org.geworkbench.components.genspace.bean.*;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.components.genspace.Message;
import org.geworkbench.components.genspace.Inbox;
import org.geworkbench.components.genspace.Outbox;
import org.geworkbench.components.genspace.ComposeMessage;

import java.net.Socket;
import java.io.*;
import java.util.ArrayList;

public class LoginManager {
	
	private static final String PROPERTY_GENSPACE_LOGIN_USER="LoginUserId";
	RegisterBean bean = null;
	DataVisibilityBean dvb = null;
	NetworkVisibilityBean nvb = null;
	
	public static String loggedUser = null;
	
	public LoginManager() {
		super();
	}				
	
	public LoginManager(String userId, char[] password) {
		bean.setUName(userId);
		bean.setPassword(password);
	}	
	
	public LoginManager(RegisterBean regBean) {
		bean = regBean;
	}		
	
	public LoginManager(DataVisibilityBean dvbBean) {
		dvb = dvbBean;
	}	
	
	public LoginManager(NetworkVisibilityBean nvbBean) {
		nvb = nvbBean;
	}

	public LoginManager(String message, String userId, char[] password, char[] confirmPassword,
			String firstName, String lastName, String labAffiliation,
			String eMail, String phoneNumber, String address1, String address2,
			String city, String state, String zipCode) {
		
		bean.setMessage(message); 

		bean.setUName(userId);

		bean.setPassword(password);
		bean.setConfirmPasswd(confirmPassword);
		
		bean.setFName(firstName);
		bean.setLName(lastName);
		
		bean.setLabAffiliation(labAffiliation);
		
		bean.setEmail(eMail);
		
		bean.setPhoneNumber(phoneNumber);
		
		bean.setAddr1(address1);
		bean.setAddr2(address2);
		bean.setCity(city);
		bean.setState(state);
		bean.setZipcode(zipCode);
	}
	
	private Socket getSocket() {
		try {
			Socket s = new Socket(RuntimeEnvironmentSettings.SECURITY_SERVER.getHost(),
					RuntimeEnvironmentSettings.SECURITY_SERVER.getPort());
			OutputStream pw = s.getOutputStream();
		
			return s;
		} catch (Exception ex) { 
			ex.printStackTrace();
			
			return null;
		}		
	}
	
	public boolean userDupCheck() {
		bean.setMessage("Duplication");
		
		try {
			byte[] buf ; 
			buf = bean.write();
				
			Socket s = getSocket();
			
			OutputStream pw = s.getOutputStream();
			pw.write (buf);
			pw.flush();
				
            boolean ret = false;
			BufferedReader socketBr = new BufferedReader(new InputStreamReader(s.getInputStream()));            
            if (socketBr.readLine().equals("true")) {
            	ret = true;
            } else {
            	ret = false;
            }
            
            pw.close();
            socketBr.close();
            s.close();

            return ret;
		} catch (Exception ex) { 
			ex.printStackTrace();
			
			//in case there's an exception such as losing network connections, this used to give a false error message that the user id is duplicated.
			//assume that the user is not a duplicate for now, and try registering
			//changed the return value to true
			return true;
		}		
		
	}
	
	public boolean userRegister() {
		bean.setMessage("Register");

		try {
			byte[] buf ; 
			buf = bean.write();
				
			Socket s = getSocket();
			
			OutputStream pw = s.getOutputStream();
			pw.write (buf);
			pw.flush();
				
            boolean ret = false;			
			BufferedReader socketBr = new BufferedReader(new InputStreamReader(s.getInputStream()));
            if (socketBr.readLine().equals("true")) {
            	ret = true;
            } else {
            	ret = false;
            } 			
            
            pw.close();
            socketBr.close();
            s.close();
            
            return ret;
		} catch (Exception ex) { 
			ex.printStackTrace();
			
			return false;			
		}
	}		
	
	public boolean userLogin() {
		bean.setMessage("Login");	//This is set to Login for logging in. Appropriately set in Jpanel for registration/login
		
		try {		
			byte[] buf ; 
			buf = bean.write();
				
			Socket s = getSocket();
			
			OutputStream pw = s.getOutputStream();
			pw.write (buf);
			pw.flush();
				
			boolean ret = false;
			BufferedReader socketBr = new BufferedReader(new InputStreamReader(s.getInputStream()));			
	        if (socketBr.readLine().equals("true")) {
	        	loggedUser = bean.getUsername();
	        	try
	    		{
	    			PropertiesManager properties = PropertiesManager.getInstance();
	    			properties.setProperty(GenSpaceLogin.class, PROPERTY_GENSPACE_LOGIN_USER, bean.getUsername());
	    			//Message msg = new Message();
	    			//msg.startMessage();
	    		}
	    		catch (Exception ex) { }
	        	ret= true;
	        } else {
	        	ret = false;
	        } 	
	        
	        pw.close();
	        socketBr.close();
	        s.close();
	        
	        return ret;
		} catch (Exception ex) { 
			ex.printStackTrace();
			
			return false;			
		}	        
	}
	
	public RegisterBean getUserInfo(){
		bean.setMessage("GetUserInfo");
		try{
			byte[] buf;
			buf = bean.write();
			
			Socket s = getSocket();
			OutputStream pw = s.getOutputStream();
			pw.write (buf);
			pw.flush();
				
			//System.out.println("Sent socket!");
			byte[] buffer = new byte[1024];
			InputStream in =  s.getInputStream();
			in.read(buffer);
			
			RegisterBean bean = (RegisterBean) RegisterBean.read(buffer);
			return bean;
		}
		catch(Exception ex){
			return null;
		}
	}
	
	public boolean userUpdate(){
		
		try {
			byte[] buf ;
			
			bean.setMessage("Update");
			buf = bean.write();
				
			Socket s = getSocket();
			OutputStream pw = s.getOutputStream();
			BufferedReader socketBr = new BufferedReader(new InputStreamReader(s.getInputStream()));			
			pw.write (buf);
			pw.flush();
				
			//System.out.println("Sent socket!");
			

            if (socketBr.readLine().equalsIgnoreCase("true")) {
            	//System.out.println("Sent true!");
            	return true;
            } else {
            	return false;
            } 			
		} catch (Exception ex) { 
			return false;			
		}
	}
	
	public ArrayList<String> getAllNetworks() {
		nvb = new NetworkVisibilityBean();
		nvb.setMessage("getAllNetworks");	
		ArrayList<String> allNetworks = new ArrayList<String>();
		
		try {		
			byte[] buf ; 
			buf = nvb.write();
				
			Socket s = getSocket();
			
			OutputStream os = s.getOutputStream();
			os.write (buf);
			os.flush();
				
			BufferedReader socketBr = new BufferedReader(new InputStreamReader(s.getInputStream()));			
			int allNetworksLength = Integer.parseInt(socketBr.readLine());
			for (int i = 0; i < allNetworksLength; i++) {
				allNetworks.add(socketBr.readLine());
			}
		    
			os.close();
			socketBr.close();
			s.close();
		
			return allNetworks;
		} catch (Exception ex) { 
			ex.printStackTrace();
			
			return null;			
		}	        
	}
	
	public boolean saveDataVisibility(  ) {
		
		dvb.setMessage("datavisibilityedit");		
		
		try {		
			byte[] buf ; 
			buf = dvb.write();
				
			Socket s = getSocket();
			
			OutputStream pw = s.getOutputStream();
			pw.write (buf);
			pw.flush();
					
			boolean ret = false;
			BufferedReader socketBr = new BufferedReader(new InputStreamReader(s.getInputStream()));			
	        if (socketBr.readLine().equals("true")) {
	        	ret = true;
	        } else {
	        	ret = false;
	        } 	
	        
			pw.close();
			socketBr.close();
			s.close();
			
			return ret;
		} catch (Exception ex) { 
			ex.printStackTrace();
			
			return false;			
		}	        
	}
	
	public boolean saveNetworkVisibility(NetworkVisibilityBean nvb) {
		
		nvb.setMessage("networkvisibilityedit");		
		
		try {		
			byte[] buf ; 
			buf = nvb.write();
				
			Socket s = getSocket();
			
			OutputStream pw = s.getOutputStream();
			pw.write (buf);
			pw.flush();    	
			
			boolean ret = false;
			BufferedReader socketBr = new BufferedReader(new InputStreamReader(s.getInputStream()));
	        if (socketBr.readLine().equals("true")) {
	        	ret = true;
	        } else {
	        	ret = false;
	        } 	
	        
			pw.close();
			socketBr.close();
			s.close();
			
			return ret;	        
		} catch (Exception ex) { 
			ex.printStackTrace();
			
			return false;			
		}	        
	}
	
	public DataVisibilityBean getDataVisibilityBean(String userId)
	{
		dvb = new DataVisibilityBean();
		dvb.setMessage("DataVisibilityLoad");		
		dvb.setUName(userId);
		
		try {
			int len = 0, off = 0;			
			byte[] buf = dvb.write();
			byte[] buf1 = new byte[1024];
				
			Socket s = getSocket();
			
			OutputStream pw = s.getOutputStream();
			pw.write (buf);
			pw.flush();			
			
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			InputStream is = s.getInputStream();			
			while(true)
			{
				if(is.available() != 0)
					break;
			}
			
			while ((len=is.available())!=0) {
				is.read(buf1);
				b.write(buf1, off, len);
				off += len;
			}

			Object obj = DataVisibilityBean.read(b.toByteArray());
			String msg = ((SecurityMessageBean)obj).getMessage();
			
			pw.close();
			b.close();
			is.close();
			s.close();
			
			if (msg.equalsIgnoreCase("success")) {
	        	return (DataVisibilityBean)obj;
	        } else {
	        	return null;
	        } 	
		} catch (Exception ex) {
			ex.printStackTrace();
			
			return null;			
		}	        

	}
	
	public NetworkVisibilityBean  getNWVisibilityBean(String userId)
	{
		nvb = new NetworkVisibilityBean();
		nvb.setMessage("networkvisibilityload");		
		nvb.setUName(userId);

		try {		
			int len = 0, off = 0;
			byte[] buf = nvb.write();
			byte[] buf1 = new byte[1024];
				
			Socket s = getSocket();
			
			OutputStream pw = s.getOutputStream();
			pw.write (buf);
			pw.flush();
				
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			
			InputStream is = s.getInputStream();			
			while(true)
			{
				if(is.available() != 0)
					break;
			}
			
			while ((len=is.available())!=0) {
				is.read(buf1);
				b.write(buf1, off, len);
				off += len;
			}

			Object obj = DataVisibilityBean.read(b.toByteArray());
			String msg = ((SecurityMessageBean)obj).getMessage();
			
			pw.close();
			b.close();
			is.close();
			s.close();
			
	        if (msg.equalsIgnoreCase("Success")) {
	        	return (NetworkVisibilityBean)obj;
	        } else {
	        	return null;
	        } 	
		} catch (Exception ex) { 
			ex.printStackTrace();
			
			return null;			
		}	        
	}

	public static String getLoggedInUser()
	{
		String userId ="";
		try
		{
		PropertiesManager properties = PropertiesManager.getInstance();
		userId = properties.getProperty(GenSpaceLogin.class, PROPERTY_GENSPACE_LOGIN_USER, "");
		
		}
		catch(Exception e) {}
		return userId;
	}
	public boolean logout()
	{
	 	try
		{
			PropertiesManager properties = PropertiesManager.getInstance();
			properties.setProperty(GenSpaceLogin.class, PROPERTY_GENSPACE_LOGIN_USER, "");
			return true;
		}
		catch (Exception ex) { return false;}

	}
}


