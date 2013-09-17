package org.geworkbench.components.ttest.service;

import org.geworkbench.components.ttest.TTestException;
import org.geworkbench.components.ttest.TTest;
import org.geworkbench.components.ttest.data.TTestInput;
import org.geworkbench.components.ttest.data.TTestOutput;

public class TTestService {

	public TTestOutput execute(TTestInput input) {
		 
		try {
			TTestOutput output = null;
			try {
			output = new TTest(
					input).execute();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			return output;
		} catch (TTestException e) {
			e.printStackTrace();
			return null;
		}
	}
}
