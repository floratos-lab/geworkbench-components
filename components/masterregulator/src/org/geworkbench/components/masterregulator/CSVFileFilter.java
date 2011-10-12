package org.geworkbench.components.masterregulator;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class CSVFileFilter extends FileFilter {
	private static final String fileExt = ".csv";

	public String getExtension() {
		return fileExt;
	}

	public String getDescription() {
		return "Comma Separated Value Files";
	}

	public boolean accept(File f) {
		boolean returnVal = false;
		if (f.isDirectory() || f.getName().endsWith(fileExt)) {
			return true;
		}

		return returnVal;
	}

}

 
