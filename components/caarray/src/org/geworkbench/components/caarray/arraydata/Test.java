package org.geworkbench.components.caarray.arraydata;

import java.io.File;

public class Test {
	private static StandAloneCaArrayClientWrapper standAloneCaArrayClientWrapper = new StandAloneCaArrayClientWrapper();
	/**
	 * @param args
	 */
//	public static void main(String[] args) {
//		try{
//		java.io.File testfile = new File("test.txt");
//		testfile.createNewFile();
//		System.out.println("TESTED");
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//	}
	
	public static void main (String [] args){
		//StandAloneCaArrayClientWrapper standAloneCaArrayClientWrapper = new StandAloneCaArrayClientWrapper();
		String arg = "array.nci.nih.gov 8080 u1332plus_ivt_breast_A.mas5 CHPSignal";
		args = arg.split(" ");
		try{
		standAloneCaArrayClientWrapper.getDataSet(args[0], new Integer(args[1]).intValue(), null, null, args[2], args[3]);
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}

}
