package org.geworkbench.components.aracne;

import org.geworkbench.util.threading.SwingWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import wb.plugins.aracne.WeightedGraph;

/**
 * @author mhall
 */
public class AracneProgress extends JDialog {

    SwingWorker<WeightedGraph, Object> worker;
    Thread aracneThread;
    private JButton cancelButton = new JButton("Cancel");

    public AracneProgress(Thread thread) throws HeadlessException {
        aracneThread = thread;
        setLayout(new BorderLayout());
        setTitle("ARACNE Process Running");
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

        aracneThread.start();
    }
}
