package edu.columbia.geworkbench.cagrid.converter;

import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import edu.columbia.geworkbench.cagrid.utils.GridUtils;
import edu.duke.cabig.rproteomics.model.statml.DataType;

/**
 * 
 * @author keshav
 * @version $Id: CagridMicroarrayTypeConverterTest.java,v 1.1 2007-02-09 17:18:00 keshav Exp $
 */
public class CagridMicroarrayTypeConverterTest extends TestCase {

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

		float[][] fdata = GridUtils.readTabFile(is);
		DataType microaraySet = Converter.float2DToDataType(fdata);
		// CagridMicroarrayTypeConverter.convertToCagridDataType(microarraySetView);

	}

}
