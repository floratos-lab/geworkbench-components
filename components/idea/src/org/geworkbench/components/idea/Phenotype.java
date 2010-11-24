package org.geworkbench.components.idea;

/**
 * Phenotype data structure.
 * 
 * @author zm2165
 * @version $Id$
 */
public class Phenotype {	
	private String phenoName;
	private int[] expCols;
	private int[] excludeCols;
	private int[] allExpCols;
	
	public Phenotype(){
		
	}
	
	public Phenotype(String phenoName, int [] expCols, int[] excludeCols){
		this.phenoName=phenoName;
		this.expCols=expCols;
		this.excludeCols=excludeCols;
	}
	
	public Phenotype(int [] expCols){
		this.expCols=expCols;
	}
	
	public void setExpCols(int[] expCols) {
		this.expCols = expCols;
	}
	public int[] getExpCols() {
		return expCols;
	}
	
	public String getPhenoName() {
		return phenoName;
	}

	public void setExcludeCols(int[] excludeCols) {
		this.excludeCols = excludeCols;
	}

	public int[] getExcludeCols() {
		return excludeCols;
	}

	public void setAllExpCols(int[] allExpCols) {
		this.allExpCols = allExpCols;
	}

	public int[] getAllExpCols() {
		return allExpCols;
	}

}

