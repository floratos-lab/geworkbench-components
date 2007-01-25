package edu.columbia.geworkbench.cagrid.utils;

import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.columbia.geworkbench.cagrid.cluster.parser.TabDelimParser;

/**
 * @author keshav
 * @version $Id: GridUtils.java,v 1.3 2007-01-25 22:28:59 keshav Exp $
 */
public class GridUtils {
	private static Log log = LogFactory.getLog(GridUtils.class);

	private static TabDelimParser parser = null;

	/**
	 * @param is
	 * @return
	 */
	public static float[][] readTabFile(InputStream is) {

		parser = new TabDelimParser();
		Collection results = new LinkedHashSet();
		try {
			parser.parse(is);
			results = parser.getResults();
		} catch (Exception e) {
			e.printStackTrace();
		}

		int i = 0;
		float[][] values = new float[results.size()][];

		Iterator iter = results.iterator();
		while (iter.hasNext()) {
			String[] array = (String[]) iter.next();
			values[i] = new float[array.length];
			for (int j = 0; j < values[i].length; j++) {
				/*
				 * FIXME exp files acually contain val | pval, but this is a
				 * performance test, so I don't care about the actual values.
				 * Inspects the data, row by row. If it starts with a character,
				 * then this is the header, if it ends with _at, then this is a
				 * probe name. FIXME I also don't like the _at check here as not
				 * all probes will end with this. You need something more
				 * generic.
				 */
				if (array[j].equalsIgnoreCase("probe"))
					break;
				if (Character.isLetter(array[j].charAt(0))
						|| array[j].endsWith("_at"))
					continue;
				values[i][j] = Float.parseFloat(array[j]);
			}
			i++;
		}

		return values;
	}

	/**
	 * @param filename
	 * @return float[][]
	 */
	public static float[][] readTabFile(String filename) {

		InputStream is;

		float[][] values = null;

		try {
			is = new FileInputStream(new File(filename));
			values = readTabFile(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return values;
	}

	/**
	 * @return Object
	 */
	public static Object getHeader() {

		if (parser == null)
			throw new RuntimeException(
					"Null header.  Try calling readTabFile first.");

		return parser.getHeader();

	}

	/**
	 * 
	 * @param obj
	 */
	public static void serializeToXml(Object obj) {
		log.info("Writing object " + obj + " to xml");

		FileOutputStream fos = null;
		Random r = new Random();
		try {
			fos = new FileOutputStream(obj.getClass().getSimpleName() + "-"
					+ r.nextInt() + ".xml");
			// Create XML encoder.
			XMLEncoder xenc = new XMLEncoder(fos);
			// Write object.
			xenc.writeObject(obj);
			xenc.close();
		} catch (FileNotFoundException e) {
			log.error("Could not create the FileOutputStream.  Exception is: ");
			throw new RuntimeException(e);
		}
	}
}
