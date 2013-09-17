package org.geworkbench.components.lincs;

import java.awt.Color; 

public class ScoreObject implements Comparable<ScoreObject>{

	private Float value;		 
	private Color c = null;
	
	public ScoreObject(float value, Color c) {
		this.value = value;	 
		this.c = c;
	}
	
	public float getValue()
	{
		return value;
	}
	
	 
	public void setColor(Color c)
	{
		this.c = c;
	}
	
	public Color getColor()
	{
		return this.c;
	}
	
	
	@Override
	 public String toString() {
	     return  value.toString();
	 }

	@Override
	public int compareTo(ScoreObject o) {
		 
		return this.value.compareTo(o.getValue());
	}
	
	
}
