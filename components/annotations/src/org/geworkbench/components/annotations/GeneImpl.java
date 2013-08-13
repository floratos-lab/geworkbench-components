package org.geworkbench.components.annotations;

/**
 * @version $Id$
 */
public class GeneImpl implements GeneBase {

	final private String symbol;
	final private String fullName;
	
	public GeneImpl(String symbol, String fullName) {
		this.symbol = symbol;
		this.fullName = fullName;
	}
	@Override
	public String getGeneName() {
		return fullName;
	}

	@Override
	public String getGeneSymbol() {
		return symbol;
	}

}
