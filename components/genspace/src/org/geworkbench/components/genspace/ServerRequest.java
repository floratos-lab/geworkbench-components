package org.geworkbench.components.genspace;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

public class ServerRequest {

	public static Serializable get(ServerConfig config, String command,
			ArrayList arg) {
		// send request to server
		Socket s = null;
		Serializable result = null;

		try {
			s = new Socket(config.getHost(), config.getPort());
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

			// send the action keyword and the name of the tool
			oos.writeObject(command);
			oos.writeObject(arg);
			oos.flush();
			
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			result = (Serializable)ois.readObject();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			// TODO: handle the error more gracefully
		} finally {
			try {
				s.close();
			} catch (Exception ex) {
			}
		}
		return result;
	}
	
	
	public static void main(String s[]){
		
		ArrayList args = new ArrayList();
		args.add("ARACNE");
		
		System.out.println(
				ServerRequest.get(
						RuntimeEnvironmentSettings.TOOL_SERVER, 
						"getToolId", args ));
		
	}

}
