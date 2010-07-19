package org.geworkbench.components.ei;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

/**
 * @author mhall
 */
public class EIProgress extends JDialog {

    private Thread aracneThread;
    private JButton cancelButton = new JButton("Cancel");

    public EIProgress(Thread thread) throws HeadlessException {
        aracneThread = thread;
        setLayout(new BorderLayout());
        setModal(true);
        setTitle("Evidence Integration Running");
        setSize(300, 50);
        setLocation((int) (getToolkit().getScreenSize().getWidth() - getWidth()) / 2, (int) (getToolkit().getScreenSize().getHeight() - getHeight()) / 2);
        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        add(progress, BorderLayout.CENTER);
        add(cancelButton, BorderLayout.EAST);

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                aracneThread.stop();
                setVisible(false);
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                // Abort execution if progres window closed
                aracneThread.stop();
            }
        });

        aracneThread.start();
    }

}
