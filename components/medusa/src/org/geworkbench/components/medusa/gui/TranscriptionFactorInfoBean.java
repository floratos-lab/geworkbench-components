package org.geworkbench.components.medusa.gui;

/**
 * A bean used to hold transcription factor related information.
 * 
 * @author kk2457
 * @version $Id: TranscriptionFactorInfoBean.java,v 1.1 2007-08-20 15:35:47 keshav Exp $
 */
public class TranscriptionFactorInfoBean {
	private String name;
	private String description;
	private String source;
	private double pssm[][];
	double distance = -1;

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 * @return
	 */
	public String getSource() {
		return source;
	}

	/**
	 * 
	 * @param source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * 
	 * @return
	 */
	public double[][] getPssm() {
		return pssm;
	}

	/**
	 * 
	 * @param pssm
	 */
	public void setPssm(double[][] pssm) {
		this.pssm = pssm;
	}

	/**
	 * 
	 * @return
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * 
	 * @param distance
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}

}
