package org.geworkbench.components.genspace.chat;

import org.jivesoftware.smack.XMPPConnection;

public class ChatTest {

	public static void main(String[] args) {
		// XMPPConnection.DEBUG_ENABLED = true;
		XMPPConnection.DEBUG_ENABLED = true;
		ChatReceiver t = new ChatReceiver();
		t.login("jon2", "test");
		while (true) {

		}
	}

}
