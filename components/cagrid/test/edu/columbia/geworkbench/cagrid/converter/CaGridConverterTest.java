package edu.columbia.geworkbench.cagrid.converter;

import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.ginkgo.labs.reader.TabFileReader;

import edu.duke.cabig.rproteomics.model.statml.Data;

/**
 * 
 * @author keshav
 * @version $Id: CagridMicroarrayTypeConverterTest.java,v 1.2 2007/02/09
 *          21:56:10 keshav Exp $
 */
public class CaGridConverterTest extends TestCase {

	String filename = "test/edu/columbia/geworkbench/cagrid/converter/aTestDataSet_without_headers_30x45.txt";
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
	public void testConvertToCagridDataType() {

		float[][] fdata = TabFileReader.readTabFile(is);
		Data microaraySet = CaGridConverter.float2DToDataType(fdata);
		// CagridMicroarrayTypeConverter.convertToCagridData(microarraySetView);

	}

}
