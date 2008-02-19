/**
 * Result.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package vamsas.objects.simple;

public class Result
    implements java.io.Serializable
{
  private boolean broken;
  private boolean failed;
  private boolean finished;
  private boolean invalid;
  private boolean jobFailed;
  private boolean queued;
  private boolean running;
  private boolean serverError;
  private int state;
  private java.lang.String status;
  private boolean suspended;

  public Result()
  {
  }

  public Result(
      boolean broken,
      boolean failed,
      boolean finished,
      boolean invalid,
      boolean jobFailed,
      boolean queued,
      boolean running,
      boolean serverError,
      int state,
      java.lang.String status,
      boolean suspended)
  {
    this.broken = broken;
    this.failed = failed;
    this.finished = finished;
    this.invalid = invalid;
    this.jobFailed = jobFailed;
    this.queued = queued;
    this.running = running;
    this.serverError = serverError;
    this.state = state;
    this.status = status;
    this.suspended = suspended;
  }

  /**
   * Gets the broken value for this Result.
   *
   * @return broken
   */
  public boolean isBroken()
  {
    return broken;
  }

  /**
   * Sets the broken value for this Result.
   *
   * @param broken
   */
  public void setBroken(boolean broken)
  {
    this.broken = broken;
  }

  /**
   * Gets the failed value for this Result.
   *
   * @return failed
   */
  public boolean isFailed()
  {
    return failed;
  }

  /**
   * Sets the failed value for this Result.
   *
   * @param failed
   */
  public void setFailed(boolean failed)
  {
    this.failed = failed;
  }

  /**
   * Gets the finished value for this Result.
   *
   * @return finished
   */
  public boolean isFinished()
  {
    return finished;
  }

  /**
   * Sets the finished value for this Result.
   *
   * @param finished
   */
  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }

  /**
   * Gets the invalid value for this Result.
   *
   * @return invalid
   */
  public boolean isInvalid()
  {
    return invalid;
  }

  /**
   * Sets the invalid value for this Result.
   *
   * @param invalid
   */
  public void setInvalid(boolean invalid)
  {
    this.invalid = invalid;
  }

  /**
   * Gets the jobFailed value for this Result.
   *
   * @return jobFailed
   */
  public boolean isJobFailed()
  {
    return jobFailed;
  }

  /**
   * Sets the jobFailed value for this Result.
   *
   * @param jobFailed
   */
  public void setJobFailed(boolean jobFailed)
  {
    this.jobFailed = jobFailed;
  }

  /**
   * Gets the queued value for this Result.
   *
   * @return queued
   */
  public boolean isQueued()
  {
    return queued;
  }

  /**
   * Sets the queued value for this Result.
   *
   * @param queued
   */
  public void setQueued(boolean queued)
  {
    this.queued = queued;
  }

  /**
   * Gets the running value for this Result.
   *
   * @return running
   */
  public boolean isRunning()
  {
    return running;
  }

  /**
   * Sets the running value for this Result.
   *
   * @param running
   */
  public void setRunning(boolean running)
  {
    this.running = running;
  }

  /**
   * Gets the serverError value for this Result.
   *
   * @return serverError
   */
  public boolean isServerError()
  {
    return serverError;
  }

  /**
   * Sets the serverError value for this Result.
   *
   * @param serverError
   */
  public void setServerError(boolean serverError)
  {
    this.serverError = serverError;
  }

  /**
   * Gets the state value for this Result.
   *
   * @return state
   */
  public int getState()
  {
    return state;
  }

  /**
   * Sets the state value for this Result.
   *
   * @param state
   */
  public void setState(int state)
  {
    this.state = state;
  }

  /**
   * Gets the status value for this Result.
   *
   * @return status
   */
  public java.lang.String getStatus()
  {
    return status;
  }

  /**
   * Sets the status value for this Result.
   *
   * @param status
   */
  public void setStatus(java.lang.String status)
  {
    this.status = status;
  }

  /**
   * Gets the suspended value for this Result.
   *
   * @return suspended
   */
  public boolean isSuspended()
  {
    return suspended;
  }

  /**
   * Sets the suspended value for this Result.
   *
   * @param suspended
   */
  public void setSuspended(boolean suspended)
  {
    this.suspended = suspended;
  }

  private java.lang.Object __equalsCalc = null;
  public synchronized boolean equals(java.lang.Object obj)
  {
    if (! (obj instanceof Result))
    {
      return false;
    }
    Result other = (Result) obj;
    if (obj == null)
    {
      return false;
    }
    if (this == obj)
    {
      return true;
    }
    if (__equalsCalc != null)
    {
      return (__equalsCalc == obj);
    }
    __equalsCalc = obj;
    boolean _equals;
    _equals = true &&
        this.broken == other.isBroken() &&
        this.failed == other.isFailed() &&
        this.finished == other.isFinished() &&
        this.invalid == other.isInvalid() &&
        this.jobFailed == other.isJobFailed() &&
        this.queued == other.isQueued() &&
        this.running == other.isRunning() &&
        this.serverError == other.isServerError() &&
        this.state == other.getState() &&
        ( (this.status == null && other.getStatus() == null) ||
         (this.status != null &&
          this.status.equals(other.getStatus()))) &&
        this.suspended == other.isSuspended();
    __equalsCalc = null;
    return _equals;
  }

  private boolean __hashCodeCalc = false;
  public synchronized int hashCode()
  {
    if (__hashCodeCalc)
    {
      return 0;
    }
    __hashCodeCalc = true;
    int _hashCode = 1;
    _hashCode += (isBroken() ? Boolean.TRUE : Boolean.FALSE).hashCode();
    _hashCode += (isFailed() ? Boolean.TRUE : Boolean.FALSE).hashCode();
    _hashCode += (isFinished() ? Boolean.TRUE : Boolean.FALSE).hashCode();
    _hashCode += (isInvalid() ? Boolean.TRUE : Boolean.FALSE).hashCode();
    _hashCode += (isJobFailed() ? Boolean.TRUE : Boolean.FALSE).hashCode();
    _hashCode += (isQueued() ? Boolean.TRUE : Boolean.FALSE).hashCode();
    _hashCode += (isRunning() ? Boolean.TRUE : Boolean.FALSE).hashCode();
    _hashCode += (isServerError() ? Boolean.TRUE : Boolean.FALSE).hashCode();
    _hashCode += getState();
    if (getStatus() != null)
    {
      _hashCode += getStatus().hashCode();
    }
    _hashCode += (isSuspended() ? Boolean.TRUE : Boolean.FALSE).hashCode();
    __hashCodeCalc = false;
    return _hashCode;
  }

}
