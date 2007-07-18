package org.geworkbench.components.gpmodule;

import org.genepattern.util.BrowserLauncher;

import javax.swing.*;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author: Marc-Danie Nazaire
 */
public class GPHelpPanel extends JSplitPane
{
    private JScrollPane paramDescPanel;
    private JScrollPane classDescPanel;
    private File paramDescFile;
    private File classDescFile;

    public GPHelpPanel(String label, String paramDescFile, String classDescFile)
    {
        if(paramDescFile != null)
            this.paramDescFile = new File(paramDescFile);
        if(classDescFile != null)
            this.classDescFile = new File(classDescFile);

        init(label);
    }

    private void init(String label)
    {
        JPanel leftComponent = new JPanel();
        BoxLayout bLayout = new BoxLayout(leftComponent, BoxLayout.PAGE_AXIS);
        leftComponent.setLayout(bLayout);

        initClassDescPanel();
        JButton classifierButton = new JButton(label);
        classifierButton.setMinimumSize(new Dimension(145, 24));
        classifierButton.setPreferredSize(new Dimension(145, 24));
        classifierButton.setMaximumSize(new Dimension(145, 24));
        classifierButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                remove(getRightComponent());
                setRightComponent(classDescPanel);
            }
        });

        leftComponent.add(Box.createRigidArea(new Dimension(1, 8)));
        leftComponent.add(classifierButton);

        initParamDescPanel();
        JButton parameterButton = new JButton("Parameters");
        parameterButton.setMinimumSize(new Dimension(145, 24));
        parameterButton.setPreferredSize(new Dimension(145, 24));
        parameterButton.setMaximumSize(new Dimension(145, 24));
        parameterButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                remove(getRightComponent());
                setRightComponent(paramDescPanel);
            }
        });

        leftComponent.add(Box.createRigidArea(new Dimension(1,10)));
        leftComponent.add(parameterButton);

        JButton genePatternButton = new JButton("GenePattern Website");
        genePatternButton.setMinimumSize(new Dimension(145, 24));
        genePatternButton.setPreferredSize(new Dimension(145, 24));
        genePatternButton.setMaximumSize(new Dimension(145, 24));
        genePatternButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                try
                {
                    BrowserLauncher.openURL("http://www.genepattern.org");
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        JButton gpDownloadButton = new JButton("Download GenePattern");
        gpDownloadButton.setMinimumSize(new Dimension(145, 24));
        gpDownloadButton.setPreferredSize(new Dimension(145, 24));
        gpDownloadButton.setMaximumSize(new Dimension(145, 24));
        leftComponent.add(Box.createRigidArea(new Dimension(1, 10)));
        leftComponent.add(gpDownloadButton);
        gpDownloadButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                try
                {
                    BrowserLauncher.openURL("http://www.broad.mit.edu/cancer/software/genepattern/download/");
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        leftComponent.add(Box.createRigidArea(new Dimension(1, 10)));
        leftComponent.add(genePatternButton, BorderLayout.CENTER);
        leftComponent.setVisible(true);

        this.setDividerSize(10);
        this.setLeftComponent(leftComponent);
        this.setRightComponent(classDescPanel);
    }

    protected void initParamDescPanel()
    {
        paramDescPanel = new JScrollPane();

        if(paramDescFile == null)
            return;

        JTextPane paramDescTextPane = new JTextPane();
        paramDescTextPane.setEditorKit(new StyledEditorKit());
        try
        {
            paramDescTextPane.setPage(paramDescFile.toURL());
            paramDescTextPane.setEditable(false);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        JViewport viewPort = new JViewport();
        viewPort.setMinimumSize(new Dimension(220, 150));
        viewPort.setPreferredSize(new Dimension(220, 150));
        viewPort.setMaximumSize(new Dimension(220, 150));
        viewPort.setView(paramDescTextPane);
        paramDescPanel.setViewport(viewPort);
    }

    protected void initClassDescPanel()
    {
        classDescPanel = new JScrollPane();

        if(classDescFile == null)
            return;

        JTextPane paramDescTextPane = new JTextPane();
        paramDescTextPane.setEditorKit(new StyledEditorKit());
        try
        {
            paramDescTextPane.setPage(classDescFile.toURL());
            paramDescTextPane.setEditable(false);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        JViewport viewPort = new JViewport();
        viewPort.setMinimumSize(new Dimension(220, 150));
        viewPort.setPreferredSize(new Dimension(220, 150));
        viewPort.setMaximumSize(new Dimension(220, 150));
        viewPort.setView(paramDescTextPane);
        classDescPanel.setViewport(viewPort);
    }
}