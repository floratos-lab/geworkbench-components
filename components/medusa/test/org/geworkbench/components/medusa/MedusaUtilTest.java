package org.geworkbench.components.medusa;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ginkgo.labs.reader.XmlReader;
import org.ginkgo.labs.reader.XmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.columbia.ccls.medusa.io.RuleParser;
import edu.columbia.ccls.medusa.io.SerializedRule;

/**
 * Tests the medusa utility such as reading (previously generated) MEDUSA rule
 * files, generating consensue sequences, etc.
 * 
 * @author keshav
 * @version $Id: MedusaUtilTest.java,v 1.3 2007-06-12 19:50:10 keshav Exp $
 */
public class MedusaUtilTest extends TestCase {
	private Log log = LogFactory.getLog(this.getClass());

	RuleParser parser = null;

	String rulesPath = "data/test/dataset/pssm/rules/";

	String rulesFile = "rule_0.xml";

	String sequencePath = "data/test/dataset/output/run1";

	private Document doc = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Read 1 rule.
	 * 
	 */
	public void testRead() {
		boolean fail = false;
		SerializedRule srule = null;
		try {
			srule = RuleParser.read(rulesPath + rulesFile);
		} catch (IOException e) {
			fail = true;
			e.printStackTrace();
		} finally {
			assertFalse(fail);
		}

		double data[][] = srule.getPssm();

		// MedusaHelper.printData(data);

		MedusaUtil.generateConsensusSequence(data);
	}

	/**
	 * Read multiple rules files.
	 * 
	 */
	public void testReadAll() {
		boolean fail = false;
		List<SerializedRule> srules = null;
		try {
			srules = RuleParser.readAll(rulesPath);
		} catch (IOException e) {
			fail = true;
			e.printStackTrace();
		} finally {
			assertFalse(fail);
		}

		assertEquals(2, srules.size());
	}

	/**
	 * Tests if the pssm hits the upstream sequence of a target.
	 * 
	 */
	public void testIsHitByPssm() {

		boolean fail = false;
		SerializedRule srule = null;
		try {
			srule = RuleParser.read(rulesPath + rulesFile);
		} catch (IOException e) {
			fail = true;
			e.printStackTrace();
		} finally {
			assertFalse(fail);
		}

		String targetLabel = null;
		boolean isHit = MedusaUtil.isHitByPssm(srule.getPssm(), srule
				.getPssmThreshold(), targetLabel, sequencePath);
		assertFalse(isHit);

	}

	/**
	 * 
	 * 
	 */
	public void testChangeConfigXml() {

		doc = XmlReader.readXmlFile("data/test/dataset/config.xml");
		assertNotNull(doc);

		NodeList paramNodes = doc.getElementsByTagName("parameters");
		Node paramNode = paramNodes.item(0);
		NamedNodeMap paramNodeMap = paramNode.getAttributes();
		Node iterationsNode = paramNodeMap.getNamedItem("iterations");
		String iterationsVal = iterationsNode.getNodeValue();
		log.debug("current iterations val: " + iterationsVal);

		/* change value */
		String newIterationsVal = "10";
		log.debug("new iterations val: " + iterationsVal);
		iterationsNode.setNodeValue(newIterationsVal);

		Document hackedDoc = doc;
		XmlWriter.writeXml(hackedDoc);
	}
}
