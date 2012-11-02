package org.geworkbench.components.aracne.service;

import org.geworkbench.components.aracne.AracneException;
import org.geworkbench.components.aracne.data.AracneInput;
import org.geworkbench.components.aracne.data.AracneOutput;
import org.geworkbench.components.aracne.AracneComputation;

public class AracneService {

	public AracneOutput execute(AracneInput input) {
		 
		try {
			AracneOutput output = null;
			try {
			  output = new AracneComputation(
					input).execute();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			return output;
		} catch (AracneException e) {
			e.printStackTrace();
			return null;
		}
	}
}
