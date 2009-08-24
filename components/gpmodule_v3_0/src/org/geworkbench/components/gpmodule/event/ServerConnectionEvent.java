package org.geworkbench.components.gpmodule.event;

import org.genepattern.webservice.TaskInfo;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: nazaire
 * Date: Aug 21, 2009
 */
public class ServerConnectionEvent extends AWTEvent
{
    public static final int
            SERVER_CONNECTION_EVENT = AWTEvent.RESERVED_ID_MAX + 5555;
    private TaskInfo taskInfo = null;

    public ServerConnectionEvent(TaskInfo taskInfo)
    {
        super(taskInfo, SERVER_CONNECTION_EVENT);
        this.taskInfo = taskInfo;
    }

    public TaskInfo getModuleInfo()
    {
        return taskInfo;
    }
}

