package org.geworkbench.components.alignment.client;

import java.util.Date;

public class JobStatus {
    private String jobDescription;
    private double finished_Pecentage;
    private int occupiedProcessorNumber;
    private Date startTime;
    private double totalProcessors;
    private int sessionID;

    /**
     * setOccupiedProcessorNumber
     *
     * @param _occupiedProcessorNumber int
     */
    public void setOccupiedProcessorNumber(int _occupiedProcessorNumber) {
        occupiedProcessorNumber = _occupiedProcessorNumber;
    }

    /**
     * setFinished_Pecentage
     *
     * @param _finsihed_Pecentage double
     */
    public void setFinished_Pecentage(double _finsihed_Pecentage) {
        finished_Pecentage = _finsihed_Pecentage;
    }

    /**
     * setStartTime
     *
     * @param _startTime Date
     */
    public void setStartTime(Date _startTime) {
        startTime = _startTime;

    }

    /**
     * setTotalProcesses
     *
     * @param _total double
     */
    public void setTotalProcesses(double _total) {
        totalProcessors = _total;
    }

    /**
     * getstartTime
     *
     * @return Date
     */
    public Date getstartTime() {
        return startTime;
    }

    /**
     * getSessionID
     *
     * @return Object
     */
    public int getSessionID() {
        return sessionID;
    }

    public JobStatus() {
    }

    /**
     * JobStatus
     *
     * @param sessionID Object
     */
    public JobStatus(int _sessionID) {
        sessionID = _sessionID;
    }

}
