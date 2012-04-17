package org.geworkbench.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import junit.framework.TestCase;

import org.geworkbench.components.ttest.SignificanceMethod;
import org.geworkbench.components.ttest.TTest;
import org.geworkbench.components.ttest.TTestException;
import org.geworkbench.components.ttest.TTestInput;
import org.geworkbench.components.ttest.TTestOutput;

public class TTestTest extends TestCase {

	public void test0() throws IOException {
		System.out.println("=== test0 ===");
		BufferedReader br = new BufferedReader(new FileReader("test0.txt"));
		String line = br.readLine();

		String[] c = splitString(line);
		int rowCount = Integer.parseInt(c[0]);
		int caseCount = Integer.parseInt(c[1]);
		int controlCount = Integer.parseInt(c[2]);

		double[][] caseArray = new double[rowCount][caseCount];
		double[][] controlArray = new double[rowCount][controlCount];
		for (int i = 0; i < rowCount; i++) {
			line = br.readLine();
			String[] f = splitString(line);
			for (int j = 0; j < caseCount; j++) {
				caseArray[i][j] = Double.parseDouble(f[j]);
			}
		}
		for (int i = 0; i < rowCount; i++) {
			line = br.readLine();
			String[] f = splitString(line);
			for (int j = 0; j < controlCount; j++) {
				controlArray[i][j] = Double.parseDouble(f[j]);
			}
		}

		line = br.readLine();
		SignificanceMethod m = SignificanceMethod.JUST_ALPHA;
		if(line==null) throw new IOException("cannot read the choice of the method of calculate significance");
		if(line.equals(SignificanceMethod.STD_BONFERRONI.toString())) {
			m = SignificanceMethod.STD_BONFERRONI;
		} else if(line.equals(SignificanceMethod.ADJ_BONFERRONI.toString())) {
			m = SignificanceMethod.ADJ_BONFERRONI;
		} else if(line.equals(SignificanceMethod.MAX_T.toString())) {
			m = SignificanceMethod.MAX_T;
		} else if(line.equals(SignificanceMethod.MIN_P.toString())) {
			m = SignificanceMethod.MIN_P;
		} 
		line = br.readLine();
		if(line==null) throw new IOException("cannot read alpha");
		double alpha = Double.parseDouble(line);
		line = br.readLine();
		boolean byPermutation = Boolean.parseBoolean(line);
		line = br.readLine();
		boolean useWelch = Boolean.parseBoolean(line);
		line = br.readLine();
		boolean useAllCombinations = Boolean.parseBoolean(line);
		line = br.readLine();
		if(line==null) throw new IOException("cannot read number of combinations");
		int numberCombinations = Integer.parseInt(line);
		line = br.readLine();
		boolean isLogNormalized = Boolean.parseBoolean(line);
		
		TTestInput input = new TTestInput(rowCount, caseCount, controlCount, caseArray, controlArray, m, alpha, byPermutation, useWelch, useAllCombinations, numberCombinations, isLogNormalized);
		try {
			TTestOutput output = new TTest(input).execute();
			System.out.println(output);
		} catch (TTestException e) {
			e.printStackTrace();
		}
		
	}
	
	public void test1() throws IOException {
		System.out.println("=== test1 : use Welch ===");
		BufferedReader br = new BufferedReader(new FileReader("t-test4.exp")); // same file for system test
		String line = br.readLine();

		int rowCount = 110;
		int caseCount = 10;
		int controlCount = 10;

		double[][] caseArray = new double[rowCount][caseCount];
		double[][] controlArray = new double[rowCount][controlCount];
		
		String[] name = new String[rowCount];
		
		for (int i = 0; i < rowCount; i++) {
			line = br.readLine();
			String[] f = splitString(line);
			name[i] = f[0];
			int index = 2;
			for (int j = 0; j < caseCount; j++) {
				caseArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
			for (int j = 0; j < controlCount; j++) {
				controlArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
		}
		br.close();
		
		// parameter file
		Parameter parameter = new Parameter(new File("parameters1.txt"));
		SignificanceMethod m = parameter.m;
		double alpha = parameter.alpha;
		boolean byPermutation = parameter.byPermutation;
		boolean useWelch = parameter.useWelch;
		boolean useAllCombinations = parameter.useAllCombinations;
		int numberCombinations = parameter.numberCombinations;
		boolean isLogNormalized = parameter.isLogNormalized;
		
		TTestInput input = new TTestInput(rowCount, caseCount, controlCount, caseArray, controlArray, m, alpha, byPermutation, useWelch, useAllCombinations, numberCombinations, isLogNormalized);
		try {
			TTestOutput output = new TTest(input).execute();
			System.out.println(output);
			Integer[] index = outputSignificance(output, name);
			try {
				checkResult(name, output, index, "result1.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (TTestException e) {
			e.printStackTrace();
		}
		
	}
	
	public void test2() throws IOException {
		System.out.println("=== test2 : not using Welch (equal variances) ===");
		BufferedReader br = new BufferedReader(new FileReader("t-test4.exp")); // same file for system test
		String line = br.readLine();

		int rowCount = 110;
		int caseCount = 10;
		int controlCount = 10;

		double[][] caseArray = new double[rowCount][caseCount];
		double[][] controlArray = new double[rowCount][controlCount];
		
		String[] name = new String[rowCount];
		
		for (int i = 0; i < rowCount; i++) {
			line = br.readLine();
			String[] f = splitString(line);
			name[i] = f[0];
			int index = 2;
			for (int j = 0; j < caseCount; j++) {
				caseArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
			for (int j = 0; j < controlCount; j++) {
				controlArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
		}
		br.close();
		
		// parameter file
		Parameter parameter = new Parameter(new File("parameters2.txt"));
		SignificanceMethod m = parameter.m;
		double alpha = parameter.alpha;
		boolean byPermutation = parameter.byPermutation;
		boolean useWelch = parameter.useWelch;
		boolean useAllCombinations = parameter.useAllCombinations;
		int numberCombinations = parameter.numberCombinations;
		boolean isLogNormalized = parameter.isLogNormalized;
		
		TTestInput input = new TTestInput(rowCount, caseCount, controlCount, caseArray, controlArray, m, alpha, byPermutation, useWelch, useAllCombinations, numberCombinations, isLogNormalized);
		try {
			TTestOutput output = new TTest(input).execute();
			System.out.println(output);
			Integer[] index = outputSignificance(output, name);
			try {
				checkResult(name, output, index, "result2.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (TTestException e) {
			e.printStackTrace();
		}
		
	}
	
	public void test3() throws IOException {
		System.out.println("=== test3 : random permutations ===");
		BufferedReader br = new BufferedReader(new FileReader("t-test4.exp")); // same file for system test
		String line = br.readLine();

		int rowCount = 110;
		int caseCount = 10;
		int controlCount = 10;

		double[][] caseArray = new double[rowCount][caseCount];
		double[][] controlArray = new double[rowCount][controlCount];
		
		String[] name = new String[rowCount];
		
		for (int i = 0; i < rowCount; i++) {
			line = br.readLine();
			String[] f = splitString(line);
			name[i] = f[0];
			int index = 2;
			for (int j = 0; j < caseCount; j++) {
				caseArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
			for (int j = 0; j < controlCount; j++) {
				controlArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
		}
		br.close();
		
		// parameter file
		Parameter parameter = new Parameter(new File("parameters3.txt"));
		SignificanceMethod m = parameter.m;
		double alpha = parameter.alpha;
		boolean byPermutation = parameter.byPermutation;
		boolean useWelch = parameter.useWelch;
		boolean useAllCombinations = parameter.useAllCombinations;
		int numberCombinations = parameter.numberCombinations;
		boolean isLogNormalized = parameter.isLogNormalized;
		
		TTestInput input = new TTestInput(rowCount, caseCount, controlCount, caseArray, controlArray, m, alpha, byPermutation, useWelch, useAllCombinations, numberCombinations, isLogNormalized);
		try {
			TTestOutput output = new TTest(input).execute();
			System.out.println(output);
			Integer[] index = outputSignificance(output, name);
			
			// this one is randomized result so we cannot confirm the numbers
			double[] p = output.pValue;
			for(int i=0; i<index.length-1; i++) {
				int idx = index[i];
				int idx1 = index[i+1];
				if(p[idx]>p[idx1]) fail ("p-value out of order "+i+" "+p[idx]+" "+p[idx1]);
			}
		} catch (TTestException e) {
			e.printStackTrace();
		}
		
	}
	
	public void test4() throws IOException {
		System.out.println("=== test4 : all permutations ===");
		BufferedReader br = new BufferedReader(new FileReader("t-test4.exp")); // same file for system test
		String line = br.readLine();

		int rowCount = 110;
		int caseCount = 10;
		int controlCount = 10;

		double[][] caseArray = new double[rowCount][caseCount];
		double[][] controlArray = new double[rowCount][controlCount];
		
		String[] name = new String[rowCount];
		
		for (int i = 0; i < rowCount; i++) {
			line = br.readLine();
			String[] f = splitString(line);
			name[i] = f[0];
			int index = 2;
			for (int j = 0; j < caseCount; j++) {
				caseArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
			for (int j = 0; j < controlCount; j++) {
				controlArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
		}
		br.close();
		
		// parameter file
		Parameter parameter = new Parameter(new File("parameters4.txt"));
		SignificanceMethod m = parameter.m;
		double alpha = parameter.alpha;
		boolean byPermutation = parameter.byPermutation;
		boolean useWelch = parameter.useWelch;
		boolean useAllCombinations = parameter.useAllCombinations;
		int numberCombinations = parameter.numberCombinations;
		boolean isLogNormalized = parameter.isLogNormalized;
		
		TTestInput input = new TTestInput(rowCount, caseCount, controlCount, caseArray, controlArray, m, alpha, byPermutation, useWelch, useAllCombinations, numberCombinations, isLogNormalized);
		try {
			TTestOutput output = new TTest(input).execute();
			System.out.println(output);
			Integer[] index = outputSignificance(output, name);
			try {
				checkResult(name, output, index, "result4.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (TTestException e) {
			e.printStackTrace();
		}
		
	}
	
	public void test5() throws IOException {
		System.out.println("=== test5 : standard Bonferroni ===");
		BufferedReader br = new BufferedReader(new FileReader("t-test4.exp"));
		String line = br.readLine();

		int rowCount = 110;
		int caseCount = 10;
		int controlCount = 10;

		double[][] caseArray = new double[rowCount][caseCount];
		double[][] controlArray = new double[rowCount][controlCount];
		
		String[] name = new String[rowCount];
		
		for (int i = 0; i < rowCount; i++) {
			line = br.readLine();
			String[] f = splitString(line);
			name[i] = f[0];
			int index = 2;
			for (int j = 0; j < caseCount; j++) {
				caseArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
			for (int j = 0; j < controlCount; j++) {
				controlArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
		}
		br.close();
		
		// parameter file
		Parameter parameter = new Parameter(new File("parameters5.txt"));
		SignificanceMethod m = parameter.m;
		double alpha = parameter.alpha;
		boolean byPermutation = parameter.byPermutation;
		boolean useWelch = parameter.useWelch;
		boolean useAllCombinations = parameter.useAllCombinations;
		int numberCombinations = parameter.numberCombinations;
		boolean isLogNormalized = parameter.isLogNormalized;
		
		TTestInput input = new TTestInput(rowCount, caseCount, controlCount, caseArray, controlArray, m, alpha, byPermutation, useWelch, useAllCombinations, numberCombinations, isLogNormalized);
		try {
			TTestOutput output = new TTest(input).execute();
			System.out.println(output);
			Integer[] index = outputSignificance(output, name);
			try {
				checkResult(name, output, index, "result5.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (TTestException e) {
			e.printStackTrace();
		}
		
	}

	public void test6() throws IOException {
		System.out.println("=== test6 : step-down Bonferroni ===");
		BufferedReader br = new BufferedReader(new FileReader("t-test4.exp"));
		String line = br.readLine();

		int rowCount = 110;
		int caseCount = 10;
		int controlCount = 10;

		double[][] caseArray = new double[rowCount][caseCount];
		double[][] controlArray = new double[rowCount][controlCount];
		
		String[] name = new String[rowCount];
		
		for (int i = 0; i < rowCount; i++) {
			line = br.readLine();
			String[] f = splitString(line);
			name[i] = f[0];
			int index = 2;
			for (int j = 0; j < caseCount; j++) {
				caseArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
			for (int j = 0; j < controlCount; j++) {
				controlArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
		}
		br.close();
		
		// parameter file
		Parameter parameter = new Parameter(new File("parameters6.txt"));
		SignificanceMethod m = parameter.m;
		double alpha = parameter.alpha;
		boolean byPermutation = parameter.byPermutation;
		boolean useWelch = parameter.useWelch;
		boolean useAllCombinations = parameter.useAllCombinations;
		int numberCombinations = parameter.numberCombinations;
		boolean isLogNormalized = parameter.isLogNormalized;
		
		TTestInput input = new TTestInput(rowCount, caseCount, controlCount, caseArray, controlArray, m, alpha, byPermutation, useWelch, useAllCombinations, numberCombinations, isLogNormalized);
		try {
			TTestOutput output = new TTest(input).execute();
			System.out.println(output);
			Integer[] index = outputSignificance(output, name);
			try {
				checkResult(name, output, index, "result6.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (TTestException e) {
			e.printStackTrace();
		}
		
	}

	public void test7() throws IOException {
		System.out.println("=== test7 : minP ===");
		BufferedReader br = new BufferedReader(new FileReader("t-test4.exp"));
		String line = br.readLine();

		int rowCount = 110;
		int caseCount = 10;
		int controlCount = 10;

		double[][] caseArray = new double[rowCount][caseCount];
		double[][] controlArray = new double[rowCount][controlCount];
		
		String[] name = new String[rowCount];
		
		for (int i = 0; i < rowCount; i++) {
			line = br.readLine();
			String[] f = splitString(line);
			name[i] = f[0];
			int index = 2;
			for (int j = 0; j < caseCount; j++) {
				caseArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
			for (int j = 0; j < controlCount; j++) {
				controlArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
		}
		br.close();
		
		// parameter file
		Parameter parameter = new Parameter(new File("parameters7.txt"));
		SignificanceMethod m = parameter.m;
		double alpha = parameter.alpha;
		boolean byPermutation = parameter.byPermutation;
		boolean useWelch = parameter.useWelch;
		boolean useAllCombinations = parameter.useAllCombinations;
		int numberCombinations = parameter.numberCombinations;
		boolean isLogNormalized = parameter.isLogNormalized;
		
		TTestInput input = new TTestInput(rowCount, caseCount, controlCount, caseArray, controlArray, m, alpha, byPermutation, useWelch, useAllCombinations, numberCombinations, isLogNormalized);
		try {
			TTestOutput output = new TTest(input).execute();
			System.out.println(output);
			Integer[] index = outputSignificance(output, name);
			try {
				checkResult(name, output, index, "result7.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (TTestException e) {
			e.printStackTrace();
		}
		
	}
	
	public void test8() throws IOException {
		System.out.println("=== test8 : maxT ===");
		BufferedReader br = new BufferedReader(new FileReader("t-test4.exp"));
		String line = br.readLine();

		int rowCount = 110;
		int caseCount = 10;
		int controlCount = 10;

		double[][] caseArray = new double[rowCount][caseCount];
		double[][] controlArray = new double[rowCount][controlCount];
		
		String[] name = new String[rowCount];
		
		for (int i = 0; i < rowCount; i++) {
			line = br.readLine();
			String[] f = splitString(line);
			name[i] = f[0];
			int index = 2;
			for (int j = 0; j < caseCount; j++) {
				caseArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
			for (int j = 0; j < controlCount; j++) {
				controlArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
		}
		br.close();
		
		// parameter file
		Parameter parameter = new Parameter(new File("parameters8.txt"));
		SignificanceMethod m = parameter.m;
		double alpha = parameter.alpha;
		boolean byPermutation = parameter.byPermutation;
		boolean useWelch = parameter.useWelch;
		boolean useAllCombinations = parameter.useAllCombinations;
		int numberCombinations = parameter.numberCombinations;
		boolean isLogNormalized = parameter.isLogNormalized;
		
		TTestInput input = new TTestInput(rowCount, caseCount, controlCount, caseArray, controlArray, m, alpha, byPermutation, useWelch, useAllCombinations, numberCombinations, isLogNormalized);
		try {
			TTestOutput output = new TTest(input).execute();
			System.out.println(output);
			Integer[] index = outputSignificance(output, name);
			try {
				checkResult(name, output, index, "result8.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (TTestException e) {
			e.printStackTrace();
		}
		
	}
	
	public void test9() throws IOException {
		System.out.println("=== test9 : is log normalized ===");
		BufferedReader br = new BufferedReader(new FileReader("t-test4.exp"));
		String line = br.readLine();

		int rowCount = 110;
		int caseCount = 10;
		int controlCount = 10;

		double[][] caseArray = new double[rowCount][caseCount];
		double[][] controlArray = new double[rowCount][controlCount];
		
		String[] name = new String[rowCount];
		
		for (int i = 0; i < rowCount; i++) {
			line = br.readLine();
			String[] f = splitString(line);
			name[i] = f[0];
			int index = 2;
			for (int j = 0; j < caseCount; j++) {
				caseArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
			for (int j = 0; j < controlCount; j++) {
				controlArray[i][j] = Double.parseDouble(f[index]);
				index +=2 ; // skip the 0 not used
			}
		}
		br.close();
		
		// parameter file
		Parameter parameter = new Parameter(new File("parameters9.txt"));
		SignificanceMethod m = parameter.m;
		double alpha = parameter.alpha;
		boolean byPermutation = parameter.byPermutation;
		boolean useWelch = parameter.useWelch;
		boolean useAllCombinations = parameter.useAllCombinations;
		int numberCombinations = parameter.numberCombinations;
		boolean isLogNormalized = parameter.isLogNormalized;
		
		TTestInput input = new TTestInput(rowCount, caseCount, controlCount, caseArray, controlArray, m, alpha, byPermutation, useWelch, useAllCombinations, numberCombinations, isLogNormalized);
		try {
			TTestOutput output = new TTest(input).execute();
			System.out.println(output);
			Integer[] index = outputSignificance(output, name);
			try {
				checkResult(name, output, index, "result9.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (TTestException e) {
			e.printStackTrace();
		}
		
	}
	
	private static class Parameter {
		SignificanceMethod m;
		double alpha;
		boolean byPermutation;
		boolean useWelch;
		boolean useAllCombinations;
		int numberCombinations;
		boolean isLogNormalized;

		Parameter(File file) throws IOException {
			// parameter file
			BufferedReader br = new BufferedReader(new FileReader(file));

			String line = br.readLine();
			if(line==null) throw new IOException("cannot read the choice of the method of calculate significance");
			m = SignificanceMethod.JUST_ALPHA;
			if(line.equals(SignificanceMethod.STD_BONFERRONI.toString())) {
				m = SignificanceMethod.STD_BONFERRONI;
			} else if(line.equals(SignificanceMethod.ADJ_BONFERRONI.toString())) {
				m = SignificanceMethod.ADJ_BONFERRONI;
			} else if(line.equals(SignificanceMethod.MAX_T.toString())) {
				m = SignificanceMethod.MAX_T;
			} else if(line.equals(SignificanceMethod.MIN_P.toString())) {
				m = SignificanceMethod.MIN_P;
			} 
			line = br.readLine();
			alpha = Double.parseDouble(stripInlineComment(line));
			line = br.readLine();
			byPermutation = Boolean.parseBoolean(stripInlineComment(line));
			line = br.readLine();
			useWelch = Boolean.parseBoolean(stripInlineComment(line));
			line = br.readLine();
			useAllCombinations = Boolean.parseBoolean(stripInlineComment(line));
			line = br.readLine();
			numberCombinations = Integer.parseInt(stripInlineComment(line));
			line = br.readLine();
			isLogNormalized = Boolean.parseBoolean(stripInlineComment(line));			
		}
		
	}
	
	private static Integer[] outputSignificance(final TTestOutput output, String[] name) {
		System.out.println("significance (p-values)");
		int[] index1 = output.significanceIndex;
		Integer[] index = new Integer[index1.length];
		for(int i=0; i<index1.length; i++) {
			index[i] = index1[i];
		}
		Arrays.sort(index, new Comparator<Integer>(){

			@Override
			public int compare(Integer o1, Integer o2) {
				if( output.pValue[o1]<output.pValue[o2] ) return -1;
				else if( output.pValue[o1]>output.pValue[o2] ) return 1;
				else return 0;
			}
			
		});
		for(int i=0; i<index.length; i++) {
			System.out.println(name[index[i]]+" "+output.pValue[index[i]]+" "+output.foldChange[index[i]] );
		}
		return index;
	}
	
	private static void checkResult(String[] _name, TTestOutput output, Integer[] index, String resultFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(resultFile));
		String line = br.readLine();
		int i = 0;
		while(line!=null) {
			String[] f = line.split("\\s");
			String name = f[0];
			double sig = Double.parseDouble(f[1]);
			double fc = Double.parseDouble(f[2]);
			if( !name.equals(_name[index[i]]) )fail("name not match: "+_name[index[i]]);
			if( sig!=output.pValue[index[i]] )fail("p-value not match: "+output.pValue[index[i]]);
			if( fc!=output.foldChange[index[i]] ) {
				System.out.println("WARN: fold change not match: "+output.foldChange[index[i]]);
			}
			line = br.readLine();
			i++;
		}
	}
	
	private static String[] splitString(String line) {
		if(line==null) return null;
		return line.split("\\s");
	}

	private static String stripInlineComment(String line) {
		if(line==null) return null;
		if(line.indexOf("//")>0) {
			return line.substring(0, line.indexOf("//")).trim();
		} else {
			return line.trim();
		}
	}
}
