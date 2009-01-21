package org.geworkbench.components.genspace;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Date;

public class XmlClient
{
	// the output stream for writing to the server
	private PrintWriter out;

	/**
	 * Creates a new object, which connects to the server
	 */
	public XmlClient(String host, int port)
	{
		try
		{
			Socket connect = new Socket(host, port);

			// get the output stream
			out = new PrintWriter(connect.getOutputStream());
		}
		catch (Exception e)
		{
			// if we can't connect, that's fine
			//System.out.println("Cannot create XmlClient!");
			//e.printStackTrace();
		}
	}

	/**
	 * A very simple method that just sends the data as an entire line to the server.
	 */
	public void sendData(String data)
	{
		try
		{
			out.write(data + "\n");
			out.flush();
		}
		catch (Exception e)
		{
		}
		finally
		{
			try { out.close(); }
			catch (Exception e) { }
		}

	}

	/**
	 * Reads the file from the disk and then sends it to the server
	 * @param filename the full path to the file to send to the server
	 */
	public boolean readAndSendFile(String filename)
	{
		boolean success = true;
		
		try
		{
			// create a File object
			File file = new File(filename);

			// create a Scanner
			Scanner scan = new Scanner(file);

			// this is the string we'll send to the server
			// it starts with the filename in the first line
			// before we send it, though, we need to strip out the path stuff
			String[] split = filename.split("/");
			String fileContent = split[split.length - 1];
			
			Date d = new Date();
			fileContent += "_" + d.toString();
			
			fileContent = fileContent + "\n" + "<measurement>";

			// keep looping as long as there is something to read
			while (scan.hasNext())
			{
				// read one line of the file
				String line = scan.nextLine();

				// append the lines read from the file together
				//System.out.println(line);

				fileContent = fileContent + "\n" + line;
			}
			
			fileContent = fileContent + "\n" + "</measurement>";

			//signifies we're done reading the file
			String line = "END";
			fileContent = fileContent + "\n" + line;

			// now write the contents of the file to the network connection
			//System.out.println(fileContent);
			out.write(fileContent + "\n");
			out.flush();
		}
		catch (FileNotFoundException e)
		{
			//System.out.println("That file doesn't exist");
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			success = false;
		}
		finally
		{
			try { 
				out.close();
			} catch (Exception e) { }
		}
		return success;

	}

	/* 
	 * The main method of the client - for running it as a standalone app only 
	 * 
	 */
	public static void main(String[] args)
	{
		if (args.length < 3)
		{
			System.out.println("Please specify the host name, port number and file name!");
			System.exit(0);
		}

		String host = args[0];
		int port = Integer.parseInt(args[1]);
		XmlClient xc = new XmlClient(host, port);
		String filename = args[2];
		xc.readAndSendFile(filename);
	}


}
