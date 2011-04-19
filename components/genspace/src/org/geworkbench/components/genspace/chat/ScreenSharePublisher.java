package org.geworkbench.components.genspace.chat;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.SocketConnector;

/**
 * Publish screen share information to a listener.
 * 
 * @author jon
 * 
 */
public class ScreenSharePublisher extends IoHandlerAdapter {
	private IoConnector connector;
	private ConnectFuture future;
	private IoSession session;

	public ScreenSharePublisher(InetAddress remote, int port) {
		connector = new SocketConnector();
		future = connector.connect(new InetSocketAddress(remote, port), this);
		future.join();
		session = future.getSession();
	}

	public void stop() {
		session.close();
	}

	public void sendMessage(HashMap<String, Object> m) {
		session.write(m);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {

		super.sessionClosed(session);
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		IoFilter CODEC_FILTER = new ProtocolCodecFilter(
				new ObjectSerializationCodecFactory());
		session.getFilterChain().addLast("codec", CODEC_FILTER);

	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		cause.printStackTrace();
		session.close();
	}
}
