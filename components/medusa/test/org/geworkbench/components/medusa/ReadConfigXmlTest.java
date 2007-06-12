package org.geworkbench.components.medusa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author keshav
 * @version $Id: ReadConfigXmlTest.java,v 1.1 2007-06-12 19:02:29 keshav Exp $
 */
public class ReadConfigXmlTest extends TestCase {
	private Log log = LogFactory.getLog(this.getClass());

	InputStream is = null;

	Document doc = null;

	/**
	 * 
	 * 
	 */
	public void testReadConfigXml() {

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			is = new FileInputStream("data/test/dataset/config.xml");
			doc = docBuilder.parse(is);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		NodeList paramNodes = doc.getElementsByTagName("parameters");
		Node paramNode = paramNodes.item(0);
		NamedNodeMap paramNodeMap = paramNode.getAttributes();
		Node iterationsNode = paramNodeMap.getNamedItem("iterations");
		String iterationsVal = iterationsNode.getNodeValue();

		/* change value */
		String newIterationsVal = "4";
		iterationsNode.setNodeValue(newIterationsVal);

		Document hackedDoc = doc;

		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(hackedDoc);
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();
			log.info(xmlString);
			File outFile = new File("data/test/dataset/hacked_config.xml");
			FileWriter writer = new FileWriter(outFile);
			writer.write(xmlString);
			writer.close();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
