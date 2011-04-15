package org.geworkbench.components.genspace.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataVisibilityBean implements SecurityMessageBean, Serializable{
	private String username;
	private int  logData;
	private int  dataVisibility;
	private List selectedNetworks;
	private String message;

	public String getUsername(){
		return username;
	}
	
	public int getLogData() {
		return logData;
	}

	public void setLogData(int logData) {
		this.logData = logData;
	}

	public int getDataVisibility() {
		return dataVisibility;
	}

	public void setDataVisibility(int dataVisibility) {
		this.dataVisibility = dataVisibility;
	}

	public List getSelectedNetworks() {
		return selectedNetworks;
	}

	public void setSelectedNetworks(List selectedNetworks) {
		this.selectedNetworks = selectedNetworks;
	}
	
	public String getMessage(){
		return message;
	}	
	
	public void setUName(String uname){
		username = uname;
	}
	
	public void setMessage(String msg){
		message = msg;
	}
	
	/* Method used to serialize the object
	 * Returns a byte array after serialization
	 * */
	public byte[] write() throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(bos);
	
		os.writeObject(this);
		os.close();
		
		return bos.toByteArray();
	}
	
	/* Method used to read back the object details from the byte array
	 * Byte array passed across the socket can be de-serialized
	 * to get the object details*/
	public static Object read(byte[] buf) throws IOException, ClassNotFoundException{
		ByteArrayInputStream bis = new ByteArrayInputStream(buf);
		ObjectInputStream is = new ObjectInputStream(bis);

		if(buf != null){
			Object obj = is.readObject();
			return obj;
		}
		return null;
	}
}
