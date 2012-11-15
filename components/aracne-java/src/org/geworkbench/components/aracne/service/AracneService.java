package org.geworkbench.components.aracne.service;

import org.geworkbench.components.aracne.AracneException;
import org.geworkbench.components.aracne.data.AracneInput;
import org.geworkbench.components.aracne.data.AracneOutput;
import org.geworkbench.components.aracne.AracneComputation;

public class AracneService {

	public AracneOutput execute(AracneInput input) throws AracneException{
		 
		try {
			AracneOutput output = null;			 
			output = new AracneComputation(
					input).execute();			 
			return output;
		} catch (AracneException e) {			 
			throw new  AracneException(e.getMessage());
		}
	}
}
