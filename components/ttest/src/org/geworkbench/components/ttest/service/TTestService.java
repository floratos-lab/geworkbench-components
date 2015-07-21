package org.geworkbench.components.ttest.service;

import org.geworkbench.components.ttest.TTestException;
import org.geworkbench.components.ttest.TTest;
import org.geworkbench.components.ttest.data.TTestInput;
import org.geworkbench.components.ttest.data.TTestOutput;

public class TTestService {

	public TTestOutput execute(TTestInput input) throws TTestException {

		TTestOutput output = null;
		try {
			output = new TTest(input).execute();
			double[] tValue = output.tValue;
			for (int i = 0; i < tValue.length; i++) {
				if (tValue[i] == Double.POSITIVE_INFINITY)
					tValue[i] = Double.MAX_VALUE;
				if (tValue[i] == Double.NEGATIVE_INFINITY)
					tValue[i] = -Double.MAX_VALUE;
			}

		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return output;
	}
}
