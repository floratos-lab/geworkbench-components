package org.geworkbench.components.genspace.chat;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.ui.chat.ScreenSharingReceiver;

/**
 * Listen to screen share packets, package them together and display them
 * 
 * @author jon
 * 
 */
public class ScreenShareListener extends IoHandlerAdapter {
	DatagramSocket s;
	ScreenSharingReceiver rcv;
	Thread server_thread;
	InetAddress boundAddress;
	int boundPort;

	public void stop() {
		acceptor.unbindAll();
	}

	SocketAcceptor acceptor;

	public ScreenShareListener(ScreenSharingReceiver l) {
		rcv = l;
		try {
			boundAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			GenSpace.logger.warn("Unable to connect to screen share server",e1);
		}

		acceptor = new SocketAcceptor();
		acceptor.getFilterChain().addLast("protocol",
				new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
		try {
			acceptor.bind(new InetSocketAddress(0), this);
		} catch (IOException e) {
			GenSpace.logger.warn("Unable to connect to screen share server",e);
		}
		Set<SocketAddress> addresses = acceptor.getManagedServiceAddresses();
		for (SocketAddress a : addresses) {
			InetSocketAddress b = (InetSocketAddress) a;
			if (!b.getAddress().isLoopbackAddress()) {
				boundPort = b.getPort();
			}
		}
	}

	public InetAddress getLocalAddress() {
		return boundAddress;
	}

	public int getLocalPort() {
		return boundPort;
	}

	@Override
	public void sessionOpened(IoSession session) {
		// set idle time to 60 seconds
		session.setIdleTime(IdleStatus.BOTH_IDLE, 60);
	}

	@Override
	public void messageReceived(IoSession session, Object message) {
		// Check that we can service the request context
		@SuppressWarnings("unchecked")
		java.util.HashMap<String, Object> m = (HashMap<String, Object>) message;
		rcv.receiveImageUpdate(m, (Integer) m.get("width"),
				(Integer) m.get("height"));

	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) {
		session.close();
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		cause.printStackTrace();
		session.close();
	}

}
