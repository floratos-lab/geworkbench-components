package org.geworkbench.components.alignment.panels;

public class ParameterSetting {
    private String dbName;
    private String programName;
    private boolean viewInBrowser;
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

    public String getDbName() {
        return dbName;
    }

    public String getProgramName() {
        return programName;
    }

    public boolean isViewInBrowser() {
        return viewInBrowser;
    }
}
