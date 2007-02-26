package edu.columbia.geworkbench.cagrid.converter;

import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ginkgo.labs.reader.TabFileReader;

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
		Data microaraySet = CaGridConverter.float2DToData(fdata);
		// CagridMicroarrayTypeConverter.convertToCagridData(microarraySetView);

	}
}
