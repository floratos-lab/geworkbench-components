package org.geworkbench.components.anova.service;

import org.geworkbench.components.anova.AnovaException;
import org.geworkbench.components.anova.data.AnovaInput;
import org.geworkbench.components.anova.data.AnovaOutput;
import org.geworkbench.components.anova.Anova;

public class AnovaService {

	public AnovaOutput execute(AnovaInput input) {
		 
		try {
			AnovaOutput output = null;
			try {
			output = new Anova(
					input).execute();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			return output;
		} catch (AnovaException e) {
			e.printStackTrace();
			return null;
		}
	}
}
