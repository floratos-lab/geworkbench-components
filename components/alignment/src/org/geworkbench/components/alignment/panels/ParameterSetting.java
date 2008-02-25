package org.geworkbench.components.alignment.panels;

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
        private int startPoint = -1;
        private int endPoint = -1;


    public ParameterSetting() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public void setViewInBrowser(boolean viewInBrowser) {
        this.viewInBrowser = viewInBrowser;
    }

    public void setMatrix(String matrix) {
        this.matrix = matrix;
    }

    public void setPenalty(String penalty) {
        this.penalty = penalty;
    }

    public void setExpect(double expect) {
        this.expect = expect;
    }

    public void setHumanRepeatFilterOn(boolean humanRepeatFilterOn) {
        this.humanRepeatFilterOn = humanRepeatFilterOn;
    }

    public void setLowComplexityFilterOn(boolean lowComplexityFilterOn) {
        this.lowComplexityFilterOn = lowComplexityFilterOn;
    }

    public void setUseNCBI(boolean useNCBI) {
        this.useNCBI = useNCBI;
    }

    public void setEndPoint(int endPoint) {
        this.endPoint = endPoint;
    }

    public void setStartPoint(int startPoint) {
        this.startPoint = startPoint;
    }

    public void setMaskLowCase(boolean maskLowCase) {
        this.maskLowCase = maskLowCase;
    }

    public void setWordsize(String wordsize) {
        this.wordsize = wordsize;
    }

    public void setGapCost(String gapCost) {
        this.gapCost = gapCost;
    }

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

    public int getEndPoint() {
        return endPoint;
    }

    public int getStartPoint() {
        return startPoint;
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

    private void jbInit() throws Exception {
    }

	/**
	 * @return the maskLookupTable
	 */
	public boolean isMaskLookupTable() {
		return maskLookupTable;
	}

	/**
	 * @param maskLookupTable the maskLookupTable to set
	 */
	public void setMaskLookupTable(boolean maskLookupTable) {
		this.maskLookupTable = maskLookupTable;
	}
}
