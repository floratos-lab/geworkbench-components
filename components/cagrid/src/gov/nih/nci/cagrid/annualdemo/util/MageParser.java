package gov.nih.nci.cagrid.annualdemo.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.duke.cabig.rproteomics.model.statml.Data;

/**
 * @author mcconnell
 * @author keshav
 * @version $Id: MageParser.java,v 1.1 2007-02-16 21:12:15 keshav Exp $
 */
public class MageParser {
	private MicroarrayData microarrayData = new MicroarrayData();

	public MageParser() {
		super();
	}

	public void parseMicroarray(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		MageHandler handler = new MageHandler();
		parser.parse(new ByteArrayInputStream(xml.getBytes()), handler);
		microarrayData.invert();
	}

	public void parseGeneNames(String xml) throws ParserConfigurationException,
			SAXException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		MageHandler handler = new MageHandler();
		parser.parse(new ByteArrayInputStream(xml.getBytes()), handler);
	}

	private class MageHandler extends DefaultHandler {
		Data data = new Data();
		private StringBuffer chars = new StringBuffer();

		public MageHandler() {
			super();
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			chars.append(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equals("UML:Package")) {

				chars.delete(0, chars.length());
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			chars.delete(0, chars.length());

			String name = qName;
			int index = name.indexOf(":");
			if (index != -1)
				name = name.substring(index + 1);

			if (name.equals("BioDataValues")) {
				double[][] data = new MGEDCubeHandler().getCubeFromString(atts
						.getValue("cube"))[0];
				// for (int i = 0; i < data.length; i++) {
				// System.out.println(data[i][32]);
				// }
				double[] geneData = new double[data.length];
				for (int i = 0; i < data.length; i++) {
					geneData[i] = data[i][32];
				}
				microarrayData.data.add(geneData);
				microarrayData.arrayNames.add("array1");
			} else if (name.equals("Feature")) {
				microarrayData.geneNames.add(atts.getValue("name"));
			}
		}

		public void endDocument() throws SAXException {
		}
	}

	public class MGEDCubeHandler {

		public static final String LINE_DELIMITER = "\\";

		public static final String LINE_DELIMITER_PATTERN = "\\\\";

		public static final String VALUE_DELIMITER = "|";

		public static final String VALUE_DELIMITER_PATTERN = "\\|";

		private static final String LINE_DELIMITER_PROP = "gov.nih.nci.cagrid.caarray.encoding.lineDelimiter";

		private static final String LINE_DELIMITER_PATTERN_PROP = "gov.nih.nci.cagrid.caarray.encoding.lineDelimiterPattern";

		private static final String VALUE_DELIMITER_PROP = "gov.nih.nci.cagrid.caarray.encoding.valueDelimiter";

		private static final String VALUE_DELIMITER_PATTERN_PROP = "gov.nih.nci.cagrid.caarray.encoding.valueDelimiterPattern";

		private static final String NAN = "NaN";

		private String lineDelimiter;

		private String lineDelimiterPattern;

		private String valueDelimiter;

		private String valueDelimiterPattern;

		public String getLineDelimiter() {
			return lineDelimiter;
		}

		public void setLineDelimiter(String lineDelimiter) {
			this.lineDelimiter = lineDelimiter;
		}

		public String getLineDelimiterPattern() {
			return lineDelimiterPattern;
		}

		public void setLineDelimiterPattern(String lineDelimiterPattern) {
			this.lineDelimiterPattern = lineDelimiterPattern;
		}

		public String getValueDelimiter() {
			return valueDelimiter;
		}

		public void setValueDelimiter(String valueDelimiter) {
			this.valueDelimiter = valueDelimiter;
		}

		public String getValueDelimiterPattern() {
			return valueDelimiterPattern;
		}

		public void setValueDelimiterPattern(String valueDelimiterPattern) {
			this.valueDelimiterPattern = valueDelimiterPattern;
		}

		public MGEDCubeHandler() {
			try {
				setLineDelimiter(System.getProperty(LINE_DELIMITER_PROP,
						LINE_DELIMITER));
				setLineDelimiterPattern(System.getProperty(
						LINE_DELIMITER_PATTERN_PROP, LINE_DELIMITER_PATTERN));
				setValueDelimiter(System.getProperty(VALUE_DELIMITER_PROP,
						VALUE_DELIMITER));
				setValueDelimiterPattern(System.getProperty(
						VALUE_DELIMITER_PATTERN_PROP, VALUE_DELIMITER_PATTERN));
			} catch (Exception ex) {
				String msg = "Error getting property: " + ex.getMessage();
				ex.printStackTrace();
			}
		}

		public double[][][] getCubeFromString(String cube) {
			return getCubeFromString(cube, getLineDelimiterPattern(),
					getValueDelimiterPattern());
		}

		/**
		 * Ripped from MAGEstk
		 * 
		 * @param dim1
		 * @param dim2
		 * @param dim3
		 * @param str
		 * @return
		 */
		public double[][][] getCubeFromString(int dim1, int dim2, int dim3,
				String str, String lineDelimPatt, String valueDelimPatt) {

			double[][][] cube = new double[dim1][dim2][dim3];

			List lines = Arrays.asList(str.split(lineDelimPatt));
			Iterator linesIt = lines.iterator();

			for (int i = 0; i < dim1; i++) {
				for (int j = 0; j < dim2; j++) {
					String line = (String) linesIt.next();

					// To make up for the added empty line between the first
					// dimension in the write method.
					if (line.length() < 1) {
						line = (String) linesIt.next();
					}

					// Assume tab is the delimiter.
					String[] tmp = line.split(valueDelimPatt);
					// System.out.println("Got " + tmp.length + " values.");
					double[] dtmp = new double[tmp.length];

					for (int k = 0; k < tmp.length; k++) {
						if (tmp[k].trim().equalsIgnoreCase(NAN)) {
							dtmp[k] = Double.NaN;
						} else {
							dtmp[k] = new Double(tmp[k]);
						}
					}

					// Check loaded dimension and trow exception if not the
					// same as the input parameter.
					if (dtmp.length == dim3) {
						cube[i][j] = dtmp;
					} else if (dtmp.length == dim2 * dim3) {
						for (int k = 0; k < dim2; k++)
							for (int l = 0; l < dim3; l++) {
								cube[i][k][l] = dtmp[k * dim3 + l];
							}

						// force return of j-loop, both dim 2 and 3 was on the
						// same
						// line
						j = dim2;
					} else {
						throw new ArrayIndexOutOfBoundsException(dtmp.length);
					}

				}
			}

			return cube;
		}

		public double[][][] getCubeFromString(String str, String lineDelimPatt,
				String valueDelimPatt) {
			double[][][] cube = null;

			int dim1 = 1;
			int dim2 = 0;
			int dim3 = 0;
			int tempDim2 = 0;
			int tempDim3 = 0;
			// List lines = Arrays.asList(str.split(lineDelimPatt));
			List lines = Arrays.asList(str.split(lineDelimPatt));
			// System.out.println("Got " + lines.size() + " lines.");
			for (Iterator linesIt = lines.iterator(); linesIt.hasNext();) {
				String line = (String) linesIt.next();
				if (line.length() == 0) {
					dim1++;
					tempDim2 = 0;
				} else {
					tempDim2++;
					if (tempDim2 > dim2) {
						dim2 = tempDim2;
					}
					tempDim3 = line.split(valueDelimPatt).length;
					if (tempDim3 > dim3) {
						dim3 = tempDim3;
					}
				}
			}

			cube = getCubeFromString(dim1, dim2, dim3, str, lineDelimPatt,
					valueDelimPatt);

			return cube;
		}

	}

	public MicroarrayData getMicroarrayData() {
		return microarrayData;
	}

	public void setMicroarrayData(MicroarrayData microarrayData) {
		this.microarrayData = microarrayData;
	}
}
