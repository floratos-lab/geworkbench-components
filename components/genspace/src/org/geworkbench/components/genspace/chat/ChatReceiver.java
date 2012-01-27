package org.geworkbench.components.genspace.chat;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.RuntimeEnvironmentSettings;
import org.geworkbench.components.genspace.ui.chat.ChatWindow;
import org.geworkbench.components.genspace.ui.chat.RosterFrame;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

/**
 * The ChatReceiver is a generic singleton for chat connections, delegating all
 * messages and chats
 * 
 * @author jon
 * 
 */
public class ChatReceiver implements MessageListener, ChatManagerListener {
	public HashMap<String, ChatWindow> chats = new HashMap<String, ChatWindow>();
	public static ChatManager manager;
	public static XMPPConnection connection;

	public ChatReceiver() {
	}

	/**
	 * Login to the chat server
	 * 
	 * @param u
	 *            Username
	 * @param p
	 *            Password
	 */
	public void login(final String u, final String p) {
		final ChatReceiver thisRecvr = this;
		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>()
		{
			@Override
			protected void done() {
				try {
					if(get())
					{
						Presence pr = new Presence(Presence.Type.available);
						pr.setStatus("On genSpace...");
						connection.sendPacket(pr);

						manager = connection.getChatManager();
						Roster r = connection.getRoster();
						r.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
						rf = new RosterFrame();
						rf.setSize(240, 500);
						rf.setRoster(r);
						rf.setVisible(true);

						manager.addChatListener(thisRecvr);
					}
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
				super.done();
			}
			@Override
			protected Boolean doInBackground() throws Exception {
				ConnectionConfiguration config = new ConnectionConfiguration(
						RuntimeEnvironmentSettings.XMPP_HOST, 5222,"genspace");
				// SmackConfiguration.setPacketReplyTimeout(1500000);
//				config.setSASLAuthenticationEnabled(false);

				connection = new XMPPConnection(config);

//				SASLAuthentication.supportSASLMechanism("PLAIN", 0);
				try {
					connection.connect();
//					System.out.println("Connected");
					connection.login(u, p);

				} catch (XMPPException e) {
					GenSpaceServerFactory.handleExecutionException("Unable to connect to chat server. genSpace chat will be unavailable", e);
					return false;
				}
				return true;
			}
			
		};
		worker.execute();
	}

	public RosterFrame rf;

	/**
	 * Callback for chats created
	 */
	@Override
	public void chatCreated(Chat c, boolean createdLocal) {
		if (createdLocal) {
			final ChatWindow nc = new ChatWindow();
			nc.setChat(c);
			nc.setVisible(true);
			chats.put(c.getParticipant(), nc);
		}
		c.addMessageListener(this);
	}

	/**
	 * Delegate messages to the correct chat window
	 */
	@Override
	public void processMessage(Chat c, Message m) {
		if((m.getProperty("specialType") == null || m.getProperty("specialType").equals(ChatWindow.messageTypes.CHAT)) && (m.getBody() == null || m.getBody().equals("")))
			return;
		if (chats.containsKey(c.getParticipant())) {
			chats.get(c.getParticipant()).processMessage(m);
		} else {
			final ChatWindow nc = new ChatWindow();
			nc.setChat(c);
			nc.setVisible(true);
			nc.processMessage(m);
			chats.put(c.getParticipant(), nc);

		}
	}

	public void logout() {
		if(connection != null)
		connection.disconnect();
	}
}
