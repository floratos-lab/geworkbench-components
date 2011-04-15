package org.geworkbench.components.genspace;


import javax.swing.*;
import org.geworkbench.engine.config.*;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

import hypergraph.applications.hexplorer.*;

public class NetworkVisualization extends JPanel implements VisualPlugin, ActionListener {

	private static final String HOST = RuntimeEnvironmentSettings.NETWORK_VIS_SERVER.getHost();
	private static final int PORT = RuntimeEnvironmentSettings.NETWORK_VIS_SERVER.getPort();

	private String login = "swap"; // TODO: WHERE DOES THIS COME FROM?
	private String dir = "components/genspace/lib/"; // TODO: where to write the XML files??
	
	//TODO: Fix the NullPointerExceptions

	public NetworkVisualization()
	{
		System.out.println("Network Visualization started");

		initComponents();
	}


	private void initComponents()
	{
		String file = dir + "graph-" + login + ".xml";
		getNetworkData(file);
		String prop = dir + "sample.properties"; // TODO: need to make sure this file exists!

		setLayout(new BorderLayout());

		HExplorerApplet a = new HExplorerApplet(dir, file, prop);
		a.init();
		add(a.getContentPane(), BorderLayout.CENTER);
		//System.out.println("Showing network");

		// TODO: how do we get the social network to refresh?
	}


	/**
	 * This actually does the work of connecting to the server and getting the network data.
	 * It writes the data to a file, and then returns the name of the file.
	 */
	private void getNetworkData(String file)
	{
    	PrintWriter out = null;
    	PrintWriter outFile = null;
    	Socket s = null;
    	// connect to the server and get the info we need
    	try
    	{
    		s = new Socket(HOST, PORT);
    		out = new PrintWriter(s.getOutputStream());

    		// send the name of the user
    		out.println(login);
    		out.flush();

    		outFile = new PrintWriter(new File(file));

			// write the header and such
			outFile.println("<?xml version=\"1.0\"?>");
			outFile.println("<!DOCTYPE GraphXML SYSTEM \"GraphXML.dtd\">");
			outFile.println("<graph id=\"My First Graph\">");

			// keep track of the user nodes we've created so far
			ArrayList<String> users = new ArrayList<String>();

			// this is the network that we're currently dealing with
			String currentNetwork = null;

			// now start reading
			Scanner in = new Scanner(s.getInputStream());
			while(in.hasNext())
			{
				String line = in.nextLine();
				//System.out.println(line);

				// if it's a network, update the currentNetwork
				if (line.startsWith("NETWORK"))
				{
					currentNetwork = line.split(" ")[1].trim();

					// now create the node for this network
					outFile.println("<node name=\"" + currentNetwork + "\"><label>" + currentNetwork + "</label></node>");
				}
				// if it's a user, create a node (if it doesn't already exist) and add an edge
				else if (line.startsWith("USER"))
				{
					String user = line.split(" ")[1].trim();

					// add a node if the user doesn't already exist
					if (users.contains(user) == false)
					{
						String name = user;
						if (name.equals(login)) name = "&lt;&lt;ME&gt;&gt;";
						outFile.println("<node name=\"" + user + "\"><label>" + name + "</label></node>");
						users.add(user);
					}

					// now connect the user to the network
					outFile.println("<edge source=\"" + user + "\" target=\"" + currentNetwork + "\" />");

				}

			}
			outFile.println("</graph>");

			outFile.flush();
    	}
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
    		// TODO: handle the error more gracefully
    	}
    	finally
    	{
    		try { out.close(); } catch (Exception ex) { }
    		try { outFile.close(); } catch (Exception ex) { }
    		try { s.close(); } catch (Exception ex) { }
    	}

	}

    public void actionPerformed(ActionEvent e)
    {



    }


    public Component getComponent() {
        // In this case, this object is also the GUI component.
        return this;
    }


}
