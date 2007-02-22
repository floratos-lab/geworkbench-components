package edu.columbia.geworkbench.cagrid.converter;

import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ginkgo.labs.reader.TabFileReader;

import edu.columbia.geworkbench.cagrid.MageBioAssayGenerator;
import edu.columbia.geworkbench.cagrid.MageBioAssayGeneratorImpl;
import edu.duke.cabig.rproteomics.model.statml.Data;
import gov.nih.nci.cagrid.annualdemo.util.MageParser;
import gov.nih.nci.cagrid.annualdemo.util.MageParser.MGEDCubeHandler;
import gov.nih.nci.mageom.domain.bioassay.BioAssay;
import gov.nih.nci.mageom.domain.bioassay.BioDataCube;
import gov.nih.nci.mageom.domain.bioassay.BioDataValues;
import gov.nih.nci.mageom.domain.bioassay.DerivedBioAssay;
import gov.nih.nci.mageom.domain.bioassay.DerivedBioAssayData;

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

	/**
	 * 
	 * 
	 */
	public void testConvertToCagridBioAssayArray() {
		float[][] fdata = TabFileReader.readTabFile(is);

		MageBioAssayGenerator mageBioAssayGenerator = new MageBioAssayGeneratorImpl();

		String[] rowNames = new String[fdata.length];
		for (int i = 0; i < rowNames.length; i++) {
			rowNames[i] = String.valueOf(i);
		}

		String[] colNames = new String[fdata[0].length];
		for (int i = 0; i < colNames.length; i++) {
			rowNames[i] = String.valueOf(i);
		}
		BioAssay[] bioAssays = mageBioAssayGenerator.float2DToBioAssayArray(
				fdata, rowNames, colNames);

		String cube = null;
		for (BioAssay bioAssay : bioAssays) {
			DerivedBioAssay derivedBioAssay = (DerivedBioAssay) bioAssay;
			DerivedBioAssayData[] derivedBioAssayDatas = derivedBioAssay
					.getDerivedBioAssayData();
			for (DerivedBioAssayData derivedBioAssayData : derivedBioAssayDatas) {
				BioDataValues bioDataValues = derivedBioAssayData
						.getBioDataValues();
				cube = ((BioDataCube) bioDataValues).getCube();
				// byte[] bdata = Base64.decode(cube);
				// float[] returnedFdata =
				// BasicConverter.byteArrayToFloats(bdata);
				// log.debug("float data: " + returnedFdata);
			}

		}

		// parseBase64Encoding(cube);

	}

	/**
	 * 
	 * @param cube
	 */
	private void parseBase64Encoding(String cube) {
		MageParser mageParser = new MageParser();
		MGEDCubeHandler cubeHandler = mageParser.new MGEDCubeHandler();
		double[][][] dcube = cubeHandler.getCubeFromString(cube);
		log.debug(dcube);
	}
}
