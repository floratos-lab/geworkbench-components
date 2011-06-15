package org.geworkbench.components.gpmodule.event;

import java.awt.AWTEvent;

import org.genepattern.webservice.TaskInfo;

/**
 * Created by IntelliJ IDEA.
 * @author nazaire
 * @version $Id$
 */
public class ServerConnectionEvent extends AWTEvent
{
	private static final long serialVersionUID = 5215196201843148735L;
	
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

