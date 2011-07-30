/*
 * ChatWindow.java
 *
 * Created on Jul 11, 2009, 2:27:28 PM
 */

package org.geworkbench.components.genspace.ui.chat;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.RealTimeWorkFlowSuggestion;
import org.geworkbench.components.genspace.chat.ScreenShareListener;
import org.geworkbench.components.genspace.chat.ScreenSharePublisher;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.wrapper.WorkflowWrapper;
import org.geworkbench.components.genspace.ui.WorkflowVisualizationPanel;
import org.geworkbench.engine.config.GUIFramework;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;

/**
 * 
 * @author jsb2125
 */
public class ChatWindow extends javax.swing.JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6960828216997367807L;
	private Chat chat;
	private String chatText = "";

	private enum lastChatter {
		YOU, ME, NONE
	};

	private lastChatter last = lastChatter.NONE;
	private ScreenSharingReceiver screenReceiver;
	private HashMap<Integer, Integer> tileHashes = new HashMap<Integer, Integer>();
	private final static int TILE_SIZE = 32;

	public enum messageTypes {
		WORKFLOW, SCREEN_REQUEST, SCREEN_HANDSHAKE, CHAT, SCREEN_TX_END, SCREEN_RX_END
	};

	public static boolean sharingScreen = false;

	/** Creates new form ChatWindow */
	public ChatWindow() {
		initComponents();
		ogm.requestFocusInWindow();
		this.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				ogm.requestFocusInWindow();
			}

			@Override
			public void windowLostFocus(WindowEvent e) {

			};
		});
	}

	/**
	 * Set the chat that this window is in reference to
	 * 
	 * @param c
	 */
	public void setChat(Chat c) {
		this.chat = c;
		this.setTitle("Chat with " + c.getParticipant().replace("@genspace", "").replace("/Smack",""));
	}

	private ScreenShareListener screenListener;

	/**
	 * Handle a standard "text message"
	 * 
	 * @param m
	 */
	private void processTextMessage(Message m) {
		if (!last.equals(lastChatter.YOU)) {
			chatText += "<br><font color=\"green\">"
					+ chat.getParticipant().replace("@genspace", "").replace("/Smack", "")
					+ "      "
					+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
					+ ":"
					+ Calendar.getInstance().get(Calendar.MINUTE)
					+ "</font>";
		}
		last = lastChatter.YOU;
		chatText += "<br>" + m.getBody();
		txtMsging.setText("<html><body>" + chatText + "</body></html>");
		txtMsging.setCaretPosition(txtMsging.getDocument().getLength());
	}

	/**
	 * Triage incoming messages
	 * 
	 * @param m
	 *            Message
	 */
	public void processMessage(Message m) {
		if (m.getBody() != null) {
			// If this window has been hidden, unhide it
			if (!this.isVisible())
				this.setVisible(true);
			
			if(m.getProperty("specialType") == null)
				processTextMessage(m);
			else if (m.getProperty("specialType").equals(messageTypes.CHAT)) {
				processTextMessage(m);
			} else if (m.getProperty("specialType").equals(
					messageTypes.WORKFLOW)) {
				processWorkflowVisualizationMessage(m);
			} else if (m.getProperty("specialType").equals(
					messageTypes.SCREEN_REQUEST)) {
				processScreenRequestMessage(m);
			} else if (m.getProperty("specialType").equals(
					messageTypes.SCREEN_HANDSHAKE)) {
				processScreenHandshakeMessage(m);
			} else if (m.getProperty("specialType").equals(
					messageTypes.SCREEN_TX_END)) {
				// Stop sending screens
				screenPublisher.stop();
				sharingScreen = false;
				mnuShareScreen.setText("Share Screen");
				screenSendTimer.stop();
			} else if (m.getProperty("specialType").equals(
					messageTypes.SCREEN_RX_END)) {
				// Stop showing
				sharingScreen = false;
				screenListener.stop();
				screenShareFrame.setVisible(false);
			} else {
				// This is an unknown screen type.
			}
		}
	}

	/**
	 * Handle a screen handshake message
	 * 
	 * @param m
	 */
	private void processScreenHandshakeMessage(Message m) {
		InetAddress ip = (InetAddress) m.getProperty("IP");
		Integer port = (Integer) m.getProperty("port");
		startSendingScreen(ip, port);
	}

	static JFrame screenShareFrame;

	/**
	 * Start receiving screens based upon this message
	 * 
	 * @param m
	 */
	private void processScreenRequestMessage(Message m) {
		if (screenReceiver == null) {
			screenShareFrame = new JFrame();
			screenShareFrame.setSize(600, 600);
			screenReceiver = new ScreenSharingReceiver();
			screenShareFrame.add(screenReceiver);
			screenShareFrame.setVisible(true);
			screenShareFrame
					.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			screenListener = new ScreenShareListener(screenReceiver);

			screenShareFrame.addWindowListener(new WindowListener() {

				@Override
				public void windowActivated(WindowEvent e) {
					
				}

				@Override
				public void windowClosed(WindowEvent e) {
					screenShareFrame.setVisible(false);
					screenShareFrame.dispose();
					screenListener.stop();
					Message m = new Message();
					m.setProperty("specialType", messageTypes.SCREEN_TX_END);
					try {
						chat.sendMessage(m);
					} catch (XMPPException e1) {
						GenSpace.logger.warn("Error",e1);
					}


				}

				@Override
				public void windowClosing(WindowEvent e) {

				}

				@Override
				public void windowDeactivated(WindowEvent e) {

				}

				@Override
				public void windowDeiconified(WindowEvent e) {

				}

				@Override
				public void windowIconified(WindowEvent e) {

				}

				@Override
				public void windowOpened(WindowEvent e) {
				}
			});
			Message ret = new Message(m.getFrom());
			ret.setBody("IM LISTENING");
			ret.setProperty("specialType", messageTypes.SCREEN_HANDSHAKE);
			ret.setProperty("IP", screenListener.getLocalAddress());
			ret.setProperty("port", screenListener.getLocalPort());
			try {
				chat.sendMessage(ret);
			} catch (XMPPException e) {
				GenSpace.logger.warn("Error",e);
			}
		}
	}

	/**
	 * Process an incoming workflow visualization
	 * 
	 * @param m
	 */
	private void processWorkflowVisualizationMessage(Message m) {
		JFrame fr = new JFrame();
		WorkflowVisualizationPanel p = new WorkflowVisualizationPanel();
		fr.setSize(600, 500);
		fr.add(p);
		p.setSize(600, 500);
		fr.setVisible(true);

		p.render(new WorkflowWrapper((Workflow) m.getProperty("workflow")));
		fr.setTitle("Workflow from " + m.getFrom());
//		p.render(m.getBody(), "Workflow from " + m.getFrom());
	}

	/**
	 * Send a text message in this chat
	 * 
	 * @param evt
	 */
	private void ogmKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			// Send the message
			try {
				Message m = new Message();
				m.setBody(ogm.getText());
				m.setProperty("specialType", messageTypes.CHAT);
				chat.sendMessage(m);
			} catch (XMPPException e) {
				GenSpace.logger.warn("Error",e);
			}
			if (!last.equals(lastChatter.ME)) {
				chatText += "<br><font color=\"green\">You      "
						+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
						+ ":"
						+ Calendar.getInstance().get(Calendar.MINUTE)
						+ "</font>";
			}
			last = lastChatter.ME;
			chatText += "<br>" + ogm.getText();
			txtMsging.setText("<html><body>" + chatText + "</body></html>");
			txtMsging.setCaretPosition(txtMsging.getDocument().getLength());
			ogm.setText("");

		}
	}

	/**
	 * End the chat, but never fully dispose of it
	 * 
	 * @param evt
	 */
	private void mnuEndChatActionPerformed(java.awt.event.ActionEvent evt) {
		this.setVisible(false);
	}

	private ScreenSharePublisher screenPublisher;
	Timer screenSendTimer;
	private final int SCREEN_FPS = 300;

	/**
	 * Create a timer to automatically send the screen
	 * 
	 * @param ip
	 *            Destination address
	 * @param port
	 *            Destination port
	 */
	private void startSendingScreen(InetAddress ip, Integer port) {
		screenPublisher = new ScreenSharePublisher(ip, port);

		screenSendTimer = new Timer(SCREEN_FPS, new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				sendScreenUpdate();
			};
		});
		screenSendTimer.start();
	}

	private boolean amSharing = false;

	/**
	 * Either start sending or stop sending the screen
	 * 
	 * @param evt
	 */
	private void mnuShareScreenActionPerformed(java.awt.event.ActionEvent evt) {
		if (amSharing) {
			// Tell the other guy to stop

			screenPublisher.stop();
			Message m = new Message();
			m.setBody("hidden");
			m.setProperty("specialType", messageTypes.SCREEN_RX_END);
			try {
				chat.sendMessage(m);
			} catch (XMPPException e) {
				GenSpace.logger.warn("Error",e);
			}
			screenSendTimer.stop();
			amSharing = false;
		} else if (sharingScreen) {
			JDialog warning = new JDialog(this, "Can't start screen share",
					true);
			warning.setVisible(true);
		} else {
			amSharing = true;
			sharingScreen = true;
			mnuShareScreen.setText("Stop Sharing Screen");
			try {
				Message m = new Message();
				m.setBody("SEND TO ME");
				m.setProperty("specialType", messageTypes.SCREEN_REQUEST);
				chat.sendMessage(m);
			} catch (XMPPException e) {
				GenSpace.logger.warn("Error",e);
			}
		}

	}

	/**
	 * Send a single screen update message
	 */
	private void sendScreenUpdate() {
		Robot r;
		try {
			r = new Robot();
			BufferedImage n = r.createScreenCapture(GUIFramework.getFrame()
					.getBounds());
			// BufferedImage n = r.createScreenCapture(new
			// Rectangle(0,0,10,10));
			int imgNum;
			HashMap<Integer, byte[]> output = new HashMap<Integer, byte[]>();

			// Find updated tiles
			int numTiles = 0;
			for (int j = 0; j < n.getWidth(null) && numTiles < 100; j += TILE_SIZE) {
				for (int k = 0; k < n.getHeight(null) && numTiles < 100; k += TILE_SIZE) {
					imgNum = n.getWidth(null) * k + j;

					Rectangle this_tile = new Rectangle(j, k,
							(j + TILE_SIZE > n.getWidth(null) ? n
									.getWidth(null) - j : TILE_SIZE),
							(k + TILE_SIZE > n.getHeight(null) ? n
									.getHeight(null) - k : TILE_SIZE));
					WritableRaster tile = (WritableRaster) n.getData(this_tile);

					Raster tile_for_code = n.getData(this_tile);
					int[] ints = null;
					ints = tile_for_code.getPixels(tile_for_code.getMinX(),
							tile_for_code.getMinY(), tile_for_code.getWidth(),
							tile_for_code.getHeight(), ints);
					int code = 0;
					for (int l = 0; l < ints.length; l++)
						code += ints[l] % ints.length;
					if (!tileHashes.containsKey(imgNum)
							|| !tileHashes.get(imgNum).equals(code)) {
						tileHashes.put(imgNum, code);
						WritableRaster child = tile.createWritableChild(
								tile.getMinX(), tile.getMinY(),
								tile.getWidth(), tile.getHeight(), 0, 0, null);

						BufferedImage child_img = new BufferedImage(
								n.getColorModel(), child, false, null);
						ByteArrayOutputStream bs = new ByteArrayOutputStream();
						Jimi.putImage("image/png", child_img, bs);

						numTiles++;
						output.put(imgNum, bs.toByteArray());
					}

				}
			}

			if (!output.isEmpty()) {
				// Assemble the message
				HashMap<String, Object> m = new HashMap<String, Object>();
				m.put("width", n.getWidth());
				m.put("height", n.getHeight());
				m.put("indices", output.keySet().toArray());
				for (Integer tile : output.keySet())
					m.put("img" + tile, output.get(tile));
				screenPublisher.sendMessage(m);
			}

		} catch (AWTException e) {
			GenSpace.logger.warn("Error",e);
		} catch (JimiException e) {
			GenSpace.logger.warn("Error",e);
		}
	}

	/**
	 * Send the current workflow to this chat partner
	 * 
	 * @param evt
	 */
	private void mnuSendWorkflowActionPerformed(java.awt.event.ActionEvent evt) {
		if(RealTimeWorkFlowSuggestion.cwf == null)
		{
			return;
		}
		Message m = new Message(chat.getParticipant());
		m.setProperty("specialType", messageTypes.WORKFLOW);

		m.setBody("Workflow attached");
		m.setProperty("workflow", RealTimeWorkFlowSuggestion.cwf);
		try {
			chat.sendMessage(m);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

		ogm = new javax.swing.JTextField();
		jScrollPane1 = new javax.swing.JScrollPane();
		txtMsging = new javax.swing.JTextPane();
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		mnuSendWorkflow = new javax.swing.JMenuItem();
		mnuShareScreen = new javax.swing.JMenuItem();
		mnuEndChat = new javax.swing.JMenuItem();

		setTitle("Chat with XXX");

		ogm.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
				ogmKeyPressed(evt);
			}
		});

		txtMsging.setContentType("text/html");
		txtMsging.setEditable(false);
		jScrollPane1.setViewportView(txtMsging);

		jMenu1.setText("File");

		mnuSendWorkflow.setText("Send Current Workflow");
		mnuSendWorkflow.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				mnuSendWorkflowActionPerformed(evt);
			}
		});
		jMenu1.add(mnuSendWorkflow);

		mnuShareScreen.setText("Share Screen");
		mnuShareScreen.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				mnuShareScreenActionPerformed(evt);
			}
		});
//		jMenu1.add(mnuShareScreen);

		mnuEndChat.setText("End Chat");
		mnuEndChat.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				mnuEndChatActionPerformed(evt);
			}
		});
		mnuEndChat.setMnemonic('W');
		jMenu1.add(mnuEndChat);

		jMenuBar1.add(jMenu1);

		setJMenuBar(jMenuBar1);

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(ogm, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271,
						Short.MAX_VALUE)
				.add(jScrollPane1,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271,
						Short.MAX_VALUE));
		layout.setVerticalGroup(layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(org.jdesktop.layout.GroupLayout.TRAILING,
						layout.createSequentialGroup()
								.add(jScrollPane1,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										150, Short.MAX_VALUE)
								.addPreferredGap(
										org.jdesktop.layout.LayoutStyle.RELATED)
								.add(ogm,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

		pack();
	}// </editor-fold>

	// Variables declaration - do not modify
	private javax.swing.JMenu jMenu1;
	private javax.swing.JMenuBar jMenuBar1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JMenuItem mnuEndChat;
	private javax.swing.JMenuItem mnuSendWorkflow;
	private javax.swing.JMenuItem mnuShareScreen;
	private javax.swing.JTextField ogm;
	private javax.swing.JTextPane txtMsging;
	// End of variables declaration
}
