package org.geworkbench.components.genspace.rating;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.geworkbench.components.genspace.ServerConfig;
import org.geworkbench.components.genspace.ServerRequest;
import org.geworkbench.components.genspace.bean.RatingBean;


public class Rater {
	
	private ServerConfig server;
	private String writeCommand;
	private String getCommand;
	
	public Rater(ServerConfig server, String writeCommand, String getCommand){
		this.server = server;
		this.writeCommand = writeCommand;
		this.getCommand = getCommand;
	}

	public RatingBean writeRating(int id, String user, int rating){
		
		ArrayList args = new ArrayList();
		args.add(new Integer(id));
		args.add(user);
		args.add(new Integer(rating));
		RatingBean newRating = (RatingBean)ServerRequest.get(server, writeCommand, args);
		

		//System.out.println(user + " submitted rating to " + server.getHost() + ":" + id + " as a " + rating);
		
		return newRating;
	}
	
	public RatingBean getRating(int id, String user){
		ArrayList args = new ArrayList();
		args.add(new Integer(id));
		args.add(user);
		RatingBean currentRating = (RatingBean)ServerRequest.get(server, getCommand, args);
		//System.out.println(user + " requested rating for " + server.getHost() + ":" + id + ". Response: " + currentRating);
		return currentRating;
	}
}
