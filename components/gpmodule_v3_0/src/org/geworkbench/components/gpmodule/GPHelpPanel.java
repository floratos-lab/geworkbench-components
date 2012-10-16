package org.geworkbench.components.gpmodule;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.text.StyledEditorKit;

import org.genepattern.util.BrowserLauncher;

/**
 * @author: Marc-Danie Nazaire
 * @version $Id$
 */
public class GPHelpPanel extends JSplitPane
{
	private static final long serialVersionUID = 3781167718944638823L;
	
	private JScrollPane paramDescPanel;
    private JScrollPane classDescPanel;
    private URL paramDescFile;
    private URL classDescFile;

    public GPHelpPanel(String label, URL paramDescFile, URL classDescFile)
    {
        if(paramDescFile != null)
            this.paramDescFile = paramDescFile;
        if(classDescFile != null)
            this.classDescFile = classDescFile;

        init(label);
    }

    private void init(String label)
    {
        JPanel leftComponent = new JPanel();
        BoxLayout bLayout = new BoxLayout(leftComponent, BoxLayout.PAGE_AXIS);
        leftComponent.setLayout(bLayout);

        initClassDescPanel();

        if(this.paramDescFile != null)
        {
            JButton classifierButton = new JButton(label);
            classifierButton.setText("<html><center>" + label + "</center></html>");
            classifierButton.setMinimumSize(new Dimension(145, 27));
            classifierButton.setPreferredSize(new Dimension(145, 27));
            classifierButton.setMaximumSize(new Dimension(145, 27));
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
            parameterButton.setMinimumSize(new Dimension(145, 26));
            parameterButton.setPreferredSize(new Dimension(145, 26));
            parameterButton.setMaximumSize(new Dimension(145, 26));
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
        }

        JButton genePatternButton = new JButton();
        genePatternButton.setText("<html><center>GenePattern Website</center></html>");
        genePatternButton.setMinimumSize(new Dimension(145, 49));
        genePatternButton.setPreferredSize(new Dimension(145, 49));
        genePatternButton.setMaximumSize(new Dimension(145, 49));
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

        JButton gpDownloadButton = new JButton();
        gpDownloadButton.setText("<html><center>Download GenePattern</center></html>");
        gpDownloadButton.setMinimumSize(new Dimension(145, 49));
        gpDownloadButton.setPreferredSize(new Dimension(145, 49));
        gpDownloadButton.setMaximumSize(new Dimension(145, 49));

        leftComponent.add(Box.createRigidArea(new Dimension(1, 10)));
        leftComponent.add(gpDownloadButton);
        gpDownloadButton.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                try
                {
                    BrowserLauncher.openURL("http://www.broadinstitute.org/cancer/software/genepattern/download/");
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

        this.setBorder(BorderFactory.createEmptyBorder());
    }

    private void initParamDescPanel()
    {
        paramDescPanel = new JScrollPane();

        if(paramDescFile == null)
            return;

        JTextPane paramDescTextPane = new JTextPane();
        paramDescTextPane.setEditorKit(new StyledEditorKit());
        try
        {
            paramDescTextPane.setPage(paramDescFile);
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
            paramDescTextPane.setPage(classDescFile);
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