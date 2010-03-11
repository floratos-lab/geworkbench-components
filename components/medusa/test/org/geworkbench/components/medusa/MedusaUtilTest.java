package org.geworkbench.components.medusa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.medusa.MedusaCommand;
import org.w3c.dom.Document;

import edu.columbia.ccls.medusa.io.RuleParser;
import edu.columbia.ccls.medusa.io.SerializedRule;

/**
 * Tests the medusa utility such as reading (previously generated) MEDUSA rule
 * files, generating consensue sequences, etc.
 * 
 * @author keshav
 * @version $Id: MedusaUtilTest.java,v 1.9 2007-07-10 17:25:22 keshav Exp $
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
	 * Tests making changes to the config file.
	 * 
	 */
	public void testChangeConfigXml() {
		MedusaCommand medusaCommand = new MedusaCommand();
		MedusaUtil.updateConfigXml("data/test/dataset/config.xml",
				"data/test/dataset/config_hacked.xml", medusaCommand);
	}

	/**
	 * Writes the PSSM to a file.
	 * 
	 */
	public void testWritePssmToFile() {
		List<String> rulesFilesList = new ArrayList<String>();
		rulesFilesList.add(rulesFile);

		ArrayList<SerializedRule> srules = MedusaUtil.getSerializedRules(
				rulesFilesList, rulesPath);

		Random r = new Random();
		String filename = "data/test/dataset/pssm_" + Math.abs(r.nextLong());
		MedusaUtil.writePssmToFile(filename, srules);

	}
}
