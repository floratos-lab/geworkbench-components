package org.geworkbench.components.medusa.pssm;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.medusa.MedusaHelper;

import edu.columbia.ccls.medusa.io.RuleParser;
import edu.columbia.ccls.medusa.io.SerializedRule;

/**
 * Tests for pssm related "stuff", such as reading (previously generated) MEDUSA
 * rule files, generating consensue sequences, etc.
 * 
 * @author keshav
 * @version $Id: PssmTest.java,v 1.2 2007-05-23 16:49:07 keshav Exp $
 */
public class PssmTest extends TestCase {
	private Log log = LogFactory.getLog(this.getClass());

	RuleParser parser = null;

	String rulesPath = "data/test/dataset/pssm/rules/";

	String rulesFile = "rule_0.xml";

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

		MedusaHelper.generateConsensusSequence(data);
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

	public void testIsPssmHit() {

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

		boolean isHit = MedusaHelper.isPssmHit(srule.getPssm(), srule
				.getPssmThreshold());
		assertFalse(isHit);

	}
}
