package org.geworkbench.components.genspace.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class RegisterBean implements SecurityMessageBean, Serializable {
	private String fname;
	private String lname;
	private String username;
	private char[] password;
	private char[] confirmPasswd;
	private String email;
	private String imEmail;
	private String imPasswd;
	private String workTitle;
	private String phoneNumber;
	private String labAffiliation;
	private String addr1;
	private String addr2;
	private String city;
	private String state;
	private String zipcode;
	
	private String message;
	
	public String getFName(){
		return fname;
	}
	
	public String getLName(){
		return lname;
	}
	
	public String getUsername(){
		return username;
	}
	
	public char[] getPassword(){
		return password;
	}
	
	public char[] getConfirmPasswd() {
		return confirmPasswd;
	}

	public String getEmail(){
		return email;
	}
	
	public String getIMEmail(){
		return imEmail;
	}
	
	public String getIMPasswd(){
		return imPasswd;
	}
	
	public String getWorkTitle(){
		return workTitle;
	}
	
	public String getPhoneNumber(){
		return phoneNumber;
	}
	
	public String getLabAffliation(){
		return labAffiliation;
	}
	
	public String getAddr1(){
		return addr1;
	}
	
	public String getAddr2(){
		return addr2;
	}
	
	public String getCity(){
		return city;
	}
	
	public String getState(){
		return state;
	}
	
	public String getZipCode(){
		return zipcode;
	}
	
	public String getMessage(){
		return message;
	}
	
	public void setFName(String name){
		fname = name;
	}
	
	public void setLName(String name){
		lname = name;
	}
	
	public void setUName(String uname){
		username = uname;
	}
	
	public void setPassword(char[] passwd){
		password = passwd;
	}
	
	public void setConfirmPasswd(char[] confPasswd){
		confirmPasswd = confPasswd;
	}
	
	public void setEmail(String mail){
		email = mail;
	}
	
	public void setIMEmail(String email){
		imEmail = email;
	}
	
	public void setIMPasswd(String passwd){
		imPasswd = passwd;
	}
	
	public void setWorkTitle(String title){
		workTitle = title;
	}
	
	public void setPhoneNumber(String number){
		phoneNumber = number;
	}
	
	public void setAddr1(String address){
		addr1 = address;
	}
	
	public void setAddr2(String address){
		addr2 = address;
	}
	
	public void setLabAffiliation(String lab){
		labAffiliation = lab;
	}
	
	public void setCity(String cityname){
		city = cityname;
	}
	
	public void setState(String statename){
		state = statename;
	}
	
	public void setZipcode(String zip){
		zipcode = zip;
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
