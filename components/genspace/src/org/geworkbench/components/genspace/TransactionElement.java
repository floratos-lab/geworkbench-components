package org.geworkbench.components.genspace;

/**
 * This class is a bean class used for real time work flow suggestion part
 * 
 * Cheng Niu
 */

public class TransactionElement {

	private int hour;
	private int minute;
	private int second;

	private String toolName;

	public TransactionElement(int hour, int minute, int second, String toolName) {

		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.toolName = toolName;

	}

	public int getHour() {
		return this.hour;
	}

	public int getMinute() {
		return this.minute;
	}

	public int getSecond() {
		return this.second;
	}

	public String getToolName() {
		return this.toolName;
	}

}