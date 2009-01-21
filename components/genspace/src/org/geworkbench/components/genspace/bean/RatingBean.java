package org.geworkbench.components.genspace.bean;

import java.io.Serializable;

public class RatingBean implements Serializable, Comparable {
	
	//the primary key of the object being rated 
	private int identifier;
	
	//an average of all ratings for the given object (0 if none exist)
	private double overallRating;
	
	//the user's rating of the object (0 if currently not rated by user, -1 if rating is not allowed) 
	private double userRating = 0;
	
	//total number of ratings for the object
	private long totalRatings;
	
	public RatingBean(int i, double o, double u, long t){
		identifier = i;
		overallRating = o;
		userRating = u;
		totalRatings = t;
	}

	public double getOverallRating() {
		return overallRating;
	}

	public double getUserRating() {
		return userRating;
	}
	
	public int getIdentifier() {
		return identifier;
	}

	public long getTotalRatings() {
		return totalRatings;
	}

	public String toString(){
		return "(Avg:" + overallRating + " User:" + userRating + " Total Times Rated:" + totalRatings + ")";
				
	}
	
	public int compareTo(Object arg0) {
		RatingBean other = (RatingBean) arg0;
		if (this.overallRating > other.overallRating) return 1;
		else if (this.overallRating < other.overallRating) return -1;
		else return 0;
	}
}