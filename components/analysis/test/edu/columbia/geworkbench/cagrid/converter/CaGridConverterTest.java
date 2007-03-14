package edu.columbia.geworkbench.cagrid.converter;

import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ginkgo.labs.reader.TabFileReader;

import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySetGenerator;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySetGeneratorImpl;
import edu.duke.cabig.rproteomics.model.statml.Data;

/**
 * 
 * @author keshav
 * @version $Id: CagridMicroarrayTypeConverterTest.java,v 1.2 2007/02/09
 *          21:56:10 keshav Exp $
 */
public class CaGridConverterTest extends TestCase {
	private static Log log = LogFactory.getLog(CaGridConverterTest.class);

	String filename = "test/edu/columbia/geworkbench/cagrid/converter/aTestDataSet_without_headers_2x2.txt";
	InputStream is = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		is = new FileInputStream(filename);
	}

	/**
	 * 
	 * 
	 */
	public void testConvertToCagridData() {

		float[][] fdata = TabFileReader.readTabFile(is);

		String[] rowNames = new String[fdata.length];
		for (int i = 0; i < rowNames.length; i++) {
			rowNames[i] = i + "_at";
		}

		String[] colNames = new String[fdata[0].length]; // non-ragged
		for (int j = 0; j < colNames.length; j++) {
			colNames[j] = String.valueOf(j);
		}

		MicroarraySetGenerator microarraySetGenerator = new MicroarraySetGeneratorImpl();
		MicroarraySet arraySet = microarraySetGenerator.float2DToMicroarraySet(
				fdata, rowNames, colNames);
		// CagridMicroarrayTypeConverter.convertToCagridData(microarraySetView);

	}
}
