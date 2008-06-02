package org.geworkbench.components.caarray.arraydata;

import java.io.File;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
		java.io.File testfile = new File("test.txt");
		testfile.createNewFile();
		System.out.println("TESTED");
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
