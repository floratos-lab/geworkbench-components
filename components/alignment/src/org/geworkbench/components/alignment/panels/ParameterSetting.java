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

    public ParameterSetting() {
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
}
