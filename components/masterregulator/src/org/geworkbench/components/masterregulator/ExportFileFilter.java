package org.geworkbench.components.masterregulator;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExportFileFilter extends FileFilter {
	private final String extension;
	private final String description;
	private final String delimiter;

	ExportFileFilter(String extension, String description,  String delimiter) {
		this.extension = extension;
		this.description = description;
		this.delimiter = delimiter;
	}
	
	public String getDelimiter() {
		return delimiter;
	}
	
	public String getExtension() {
		return extension;
	}

	public String getDescription() {
		return description;
	}

	public boolean accept(File f) {
		boolean returnVal = false;
		if (f.isDirectory() || f.getName().endsWith(extension)) {
			return true;
		}

		return returnVal;
	}

}
