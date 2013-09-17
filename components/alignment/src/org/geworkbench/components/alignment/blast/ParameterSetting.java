package org.geworkbench.components.alignment.blast;

/**
 * 
 * @author zji
 * @version $Id$
 */
public class ParameterSetting {
        /**
         * Database name, a required parameter.
         */
        private String dbName;
        /**
         * Blast program name, a required parameter.
         */
        private String programName;
        /**
         * Whether launch Web browser to view the  result. Default is yes.
         */
        private boolean viewInBrowser;
        /**
         * Matrix name, a optional parameter. For BLASTN, dna.mat, for others default is blossum62
         */
        private String matrix;
        /**
         * Frame shift penalty, optional parameter. Default is no OOP.
         */
        private String penalty;

        private final double DEFAULTEXPECT = 10;
        /**
         * Expect value.
         */
        private double expect = DEFAULTEXPECT;

        /**
         * Low complexity filter. Default is on.
         */
        private boolean lowComplexityFilterOn = true;
        /**
         * Human Repeat Filter, only for blastn program
         */
        private boolean humanRepeatFilterOn = false;

        /**
         * Filter the low case.
         */

        private boolean maskLowCase = false;

        /**
         * Whether use NCBI Blast Server
         */
        private boolean useNCBI = false;
        private boolean maskLookupTable = false;
        private String wordsize = "11";
        private String gapCost;
        /**
         * For subsequence information.
         */
        
        private boolean excludeModelsOn;
        private boolean excludeUncultureOn;
        private boolean megaBlastOn;
        private boolean discontiguousOn;
        private boolean blastnBtnOn;
        private boolean shortQueriesOn;
        private String matchScores;
        private String compositionalAdjustment;
        private String speciesRepeat;
        private String templateLength;
        private String templateType;
        private String geneticCode;
        private String maxTargetNumber;
        private String entrezQuery;
        private String fromQuery;
        private String toQuery;
        private String hspRange;
        
    /**
     * No public constructor because this is only used in this package.
     * 
     * @param dbName
     * @param programName
     * @param viewInBrowser
     * @param expect
     * @param lowComplexityFilterOn
     * @param humanRepeatFilterOn
     * @param maskLowCase
     * @param matrix
     * @param maskLookupTable
     * 
     * @param excludeModelsOn
     * @param excludeUncultureOn
     * @param megaBlastOn
     * @param discontiguousOn
     * @param blastnBtnOn
     * @param shortQueriesOn
     * @param matchScores
     * @param compositionalAdjustment
     * @param speciesRepeat
     * @param geneticCode
     * @param maxTargetNumber
     * @param entrezQuery
     * @param fromQuery
     * @param toQuery
     * @param hspRange
     * 
     */
    ParameterSetting(String dbName, String programName, boolean viewInBrowser,
			double expect, boolean lowComplexityFilterOn,
			boolean humanRepeatFilterOn, boolean maskLowCase, String matrix,
			boolean maskLookupTable, boolean excludeModelsOn, boolean excludeUncultureOn, String entrezQuery,
			String fromQuery, String toQuery, boolean megaBlastOn, boolean discontiguousOn, boolean blastnBtnOn, boolean shortQueriesOn, 
			String matchScores, String compositionalAdjustment, String speciesRepeat, String templateLength, 
			String templateType, String geneticCode, String maxTargetNumber, String hspRange) {
        this.dbName = dbName;
        this.programName = programName;
        this.viewInBrowser = viewInBrowser;
        this.expect = expect;
        this.lowComplexityFilterOn = lowComplexityFilterOn;
        this.humanRepeatFilterOn = humanRepeatFilterOn;
        this.maskLowCase=maskLowCase;
        this.matrix = matrix;
		this.maskLookupTable = maskLookupTable;
		
		this.excludeModelsOn=excludeModelsOn;
		this.excludeUncultureOn=excludeUncultureOn;
		this.megaBlastOn=megaBlastOn;
		this.discontiguousOn=discontiguousOn;
		this.blastnBtnOn=blastnBtnOn;
		this.shortQueriesOn=shortQueriesOn;
		this.matchScores=matchScores;
		this.compositionalAdjustment=compositionalAdjustment;
		this.speciesRepeat=speciesRepeat;
		this.templateLength=templateLength;
		this.templateType=templateType;
		this.geneticCode=geneticCode;
		this.maxTargetNumber=maxTargetNumber;
		this.entrezQuery=entrezQuery;
		this.fromQuery=fromQuery;
		this.toQuery=toQuery;
		this.hspRange=hspRange;
    }

    public void setPenalty(String penalty) {
        this.penalty = penalty;
    }

    public void setUseNCBI(boolean useNCBI) {
        this.useNCBI = useNCBI;
    }     

    public void setWordsize(String wordsize) {
        this.wordsize = wordsize;
    }

    public void setGapCost(String gapCost) {
        this.gapCost = gapCost;
    }

    // following are the getters
    public String getDbName() {
        return dbName;
    }

    public String getProgramName() {
        return programName;
    }

    public boolean isViewInBrowser() {
        return viewInBrowser;
    }

    public String getMatrix() {
        return matrix;
    }

    public String getPenalty() {
        return penalty;
    }

    public double getExpect() {
        return expect;
    }

    public boolean isHumanRepeatFilterOn() {
        return humanRepeatFilterOn;
    }

    public boolean isLowComplexityFilterOn() {
        return lowComplexityFilterOn;
    }

    public boolean isUseNCBI() {
        return useNCBI;
    } 

    public boolean isMaskLowCase() {
        return maskLowCase;
    }

    public String getWordsize() {
        return wordsize;
    }

    public String getGapCost() {
        return gapCost;
    }

	/**
	 * @return the maskLookupTable
	 */
	public boolean isMaskLookupTable() {
		return maskLookupTable;
	}

	public boolean isExcludeModelsOn() {
		return excludeModelsOn;
	}

	public boolean isExcludeUncultureOn() {
		return excludeUncultureOn;
	}

	public boolean isMegaBlastOn() {
		return megaBlastOn;
	}


	public boolean isDiscontiguousOn() {
		return discontiguousOn;
	}


	public boolean isShortQueriesOn() {
		return shortQueriesOn;
	}

	public String getMatchScores() {
		return matchScores;
	}

	public String getSpeciesRepeat() {
		return speciesRepeat;
	}
	
	public boolean isBlastnBtnOn() {
		return blastnBtnOn;
	}

	public String getTemplateLength() {
		return templateLength;
	}
	
	public String getTemplateType() {
		return templateType;
	}
	
	public String getGeneticCode() {
		return geneticCode;
	}
	
	public String getMaxTargetNumber() {
		return maxTargetNumber;
	}
	
	public String getCompositionalAdjustment() {
		return compositionalAdjustment;
	}
	
	public String getEntrezQuery() {
		return entrezQuery;
	}
	public String getFromQuery() {
		return fromQuery;
	}
	public String getToQuery() {
		return toQuery;
	}
	public String getHspRange() {
		return hspRange;
	}
}
