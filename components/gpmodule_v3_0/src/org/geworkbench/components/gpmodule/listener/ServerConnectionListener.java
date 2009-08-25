package org.geworkbench.components.gpmodule.listener;

import org.geworkbench.components.gpmodule.event.ServerConnectionEvent;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: nazaire
 * Date: Aug 23, 2009
 */
public abstract class ServerConnectionListener implements EventListener
{
    public void serverConnected(ServerConnectionEvent event){}
}

