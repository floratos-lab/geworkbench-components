package org.geworkbench.components.discovery.algorithm;

/**
 * This class loads saved patterns from a file.
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

import org.geworkbench.components.discovery.view.PatternNode;
import org.geworkbench.events.HierarchicalProgressEvent;
import org.geworkbench.util.patterns.TreePatternSource;
import org.geworkbench.util.patterns.CSMatchedSeqPattern;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

public class HierarchicalDiscoveryFileLoader extends AbstractSequenceDiscoveryAlgorithm implements TreePatternSource {

    //root of our tree
    private DefaultMutableTreeNode root = null;

    File dbFile = null;
    private File patternFile = null;

    public HierarchicalDiscoveryFileLoader(File db, File file) {
        dbFile = db;
        this.patternFile = file;
        root = new DefaultMutableTreeNode("loading");
    }

    public int getNumberOfNodes() {
        return 0;
    }

    /**
     * Attach the root of this source to the main model
     *
     * @param visitormodel
     */
    public synchronized void getRoot(DefaultTreeModel visitorModel) {
        visitorModel.setRoot(root);
    }

    /**
     * start
     */
    public void start() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.patternFile));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        String filepath = null;
        try {
            br.readLine();
            filepath = br.readLine();

            HashMap hash = new HashMap();
            if ((dbFile != null) && (filepath.equalsIgnoreCase(dbFile.getName()))) {

                br.readLine();
                String line = br.readLine();

                root.setUserObject(line);
                hash.put("", root);
                br.readLine();
                line = br.readLine();
                while (line != null) {
                    String data[] = line.split("\t");
                    PatternNode pn = new PatternNode();
                    line = br.readLine();
                    if ((line != null) && (!line.equalsIgnoreCase(""))) {
                        CSMatchedSeqPattern p = new org.geworkbench.util.patterns.CSMatchedSeqPattern(line);
                        pn.setPattern(p);
                        pn.getPattern().seqNo.value = Integer.parseInt(data[2]);
                    } else {
                        pn.setSequenceExcluded(Integer.parseInt(data[2]));
                    }
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                    node.setUserObject(pn);
                    line = br.readLine();
                    hash.put(data[0], node);
                }
                for (Iterator it = hash.keySet().iterator(); it.hasNext();) {
                    String path = (String) it.next();
                    DefaultMutableTreeNode nod = (DefaultMutableTreeNode) hash.get(path);

                    if (path.length() > 1) {
                        String parentPath = path.substring(0, (path.length() - 1));
                        DefaultMutableTreeNode parentnod = (DefaultMutableTreeNode) hash.get(parentPath);
                        parentnod.insert(nod, parentnod.getChildCount());

                    } else {
                        if (!root.equals(nod)) {
                            root.insert(nod, root.getChildCount());
                        }

                    }
                }

            } else {
                JOptionPane.showMessageDialog(null, "The file " + dbFile + " could not be loaded.\n " + "Please make sure that the sequence file" + " is loaded and selected in the project.");
            }

        } catch (IOException ex1) {
            ex1.printStackTrace();

        }
        fireProgressChanged(new HierarchicalProgressEvent(0, root, null));
    }

    /**
     * stop
     */
    public void stop() {
    }

}
