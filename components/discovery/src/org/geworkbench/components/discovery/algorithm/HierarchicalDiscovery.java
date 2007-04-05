package org.geworkbench.components.discovery.algorithm;

import org.geworkbench.components.discovery.view.PatternNode;
import org.geworkbench.util.associationdiscovery.cluster.hierarchical.PatternDiscoveryHierachicalNode;
import org.geworkbench.util.patterns.PatternOperations;
import org.geworkbench.util.patterns.TreePatternSource;
import org.geworkbench.util.patterns.PatternDB;
import org.geworkbench.util.remote.SPLASHDefinition;
import org.geworkbench.util.session.DiscoverySession;
import org.geworkbench.util.session.SessionOperationException;
import polgara.soapPD_wsdl.Parameters;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: The Hierarchical SPLASH discovery.
 * Note:
 * 1) We also make this "algorithm" a data source through the
 * TreePatternSource interface. I.e the class saves the result
 * from the server here!
 * 2) The algorithm is designed to be invoked once only. i.e. Calling start
 * more than once is a mistake. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class HierarchicalDiscovery extends ServerBaseDiscovery implements TreePatternSource {
    //The locally cached tree of patterns
    private DefaultTreeModel tree = null;
    //number of times the tree split
    private int split = 0;

    //root of our tree
    private DefaultMutableTreeNode root = null;

    //the path to the node tobe retrived next.
    private StringBuffer path = new StringBuffer("0");

    //the number of patterns cached locally.
    private int sizeLocal = 0;

    public HierarchicalDiscovery(DiscoverySession s, Parameters parameter) {
        super(s, parameter);
        init();
    }

    public HierarchicalDiscovery(DiscoverySession s) {
        super(s);
        init();
    }

    /**
     * Initialize tree/root
     */
    private void init() {
        int seqNo = getSession().getSequenceDB().getSequenceNo();
        root = new DefaultMutableTreeNode("Sequences: " + seqNo);
        tree = new DefaultTreeModel(root);
    }

    protected void runAlgorithm() {
        DiscoverySession discoverySession = getSession();
        try {
            //start discovery
            discoverySession.discover(SPLASHDefinition.Algorithm.HIERARCHICAL);
            pollAndUpdate();
        } catch (SessionOperationException ex) { //end try
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
    }

    protected void reconnectAlgorithm() {
        pollAndUpdate();
    }

    /**
     * The method polls the server for the status of the discovery.
     */
    private void pollAndUpdate() {
        //the number of patterns on the server as per the last call
        int sizeRemote = 0;
        DefaultMutableTreeNode latestNode = root;
        DiscoverySession discoverySession = getSession();

        try {
            while (!done && !isStop()) {
                Thread.sleep(100);
                tryWait();
                done = discoverySession.isDone();

                //check for new patterns
                sizeRemote = discoverySession.getPatternNo();
                if (sizeRemote > sizeLocal) {
                    latestNode = buildTree(sizeRemote, latestNode);
                    sizeLocal = sizeRemote;
                    ++split;
                    fireStatusBarEvent(getSplitCount());
                }

                //visual updates
                fireProgressBarEvent();
            }
            writeToResultfile();
            if (isStop()) {
                stopMessage();
            } else {
                doneMessage();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SessionOperationException e) {
            e.getCause().printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMessage() {
        fireStatusBarEvent(getSplitCount() + " (Algorithm was stopped).");
    }

    private void doneMessage() {
        fireStatusBarEvent("Done " + "(" + getSplitCount() + ")");
        fireProgressBarEvent();
    }

    /**
     * Get a String message of number of splits.
     *
     * @return String
     */
    private String getSplitCount() {
        return "Splits: " + split;
    }

    /**
     * The method builds a binary tree of found patterns.
     * For a node: the left child ("0") is the node with the found pattern, while
     * the right childe ("1") has no pattern. The right child will NEVER come from
     * the server - it is deduced from the left node.
     *
     * @param remote      int the number of node on the server
     * @param currentNode DefaultMutableTreeNode the local node that was created locally
     * @throws SessionOperationException
     */
    private DefaultMutableTreeNode buildTree(int remote, DefaultMutableTreeNode currentNode) throws SessionOperationException {

        while (sizeLocal < remote) {
            PatternDiscoveryHierachicalNode node = getRemoteNode(path.toString());
            if (node != null) {

                //check the path and, if needed, make the right child the new currentNode
                if ((currentNode.getChildCount() == 2) && (path.length() > 1) && (path.charAt(path.length() - 2) == '1')) {
                    //switch to the node with no pattern
                    currentNode = (DefaultMutableTreeNode) currentNode.getChildAt(0);
                }

                //add the node with NO pattern
                addRemoteNode(currentNode, node, false);
                //add the node with the pattern
                currentNode = addRemoteNode(currentNode, node, true);
                ++sizeLocal;
                path.append('0');
            } else {
                //Backtrack
                //the last path did not yield a node. removed the last 0
                path.setLength(path.length() - 1);
                //if leaf
                if (path.charAt(path.length() - 1) == '0') {
                    if (currentNode != root) {
                        currentNode = (DefaultMutableTreeNode) currentNode.getParent();
                    }
                }
                //backtrack
                while ((path.length() > 1) && (path.charAt(path.length() - 1) == '1')) {
                    path.setLength(path.length() - 1);

                    if (currentNode != root) {
                        currentNode = (DefaultMutableTreeNode) currentNode.getParent();
                    }
                }

                path.setCharAt(path.length() - 1, '1');
                path.append('0');
            }
        }

        return currentNode;
    }

    /**
     * Add the remote node to the local tree.
     *

     */
    /**
     *
     * @param parent
     * @param node
     * @param pNode
     * @return
     */
    private DefaultMutableTreeNode addRemoteNode(DefaultMutableTreeNode parent, PatternDiscoveryHierachicalNode node, boolean pNode) {

        //initializing our splash data nodes and the default tree nodes
        DefaultMutableTreeNode child = new DefaultMutableTreeNode();
        PatternNode nodePattern = new PatternNode();

        //setting the nodes with HiearcDisc results
        if (pNode) {
            nodePattern.setPattern(node.pattern);
        } else {
            nodePattern.setPattern(null);
            nodePattern.setSequenceExcluded(node.patExcluded);
        }

        child.setUserObject(nodePattern);

        if (pNode) {
            DefaultMutableTreeNode hmmChild = new DefaultMutableTreeNode();
            PatternNode hmmNodePattern = new PatternNode();
            hmmNodePattern.setPattern(node.hmmPattern);
            hmmNodePattern.setSequenceExcluded(node.hPatExcluded);
            hmmChild.setUserObject(hmmNodePattern);
            child.add(hmmChild);
        }
        addNode(parent, child);
        fireHiearcProgressChange(0, parent, child);
        return child;
    }

    private synchronized void fireHiearcProgressChange(int pattern, DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
        fireProgressChanged(new org.geworkbench.events.HierarchicalProgressEvent(pattern, parent, child));
    }

    private synchronized void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
        parent.add(child);

    }

    public int getNumberOfNodes() {
        return tree.getChildCount(root);
    }

    /**
     * Attach the root of this source to the main model
      */
    /**
     *
     * @param visitorModel
     */
    public synchronized void getRoot(DefaultTreeModel visitorModel) {
        visitorModel.setRoot(root);
    }

    /**
     * Fetched a node from the server.
     *
     * @param path String path to the node
     * @return Node
     * @throws SessionOperationException
     */
    private PatternDiscoveryHierachicalNode getRemoteNode(String path) throws SessionOperationException {
        DiscoverySession discoverySession = getSession();
        PatternDiscoveryHierachicalNode node = null;

        node = discoverySession.getPatternNode(path.toString());

        if (node != null) {
            PatternOperations.fill(node.pattern, discoverySession.getSequenceDB());
        }
        return (node);
    }

    protected void statusChangedListenerAdded() {
        super.statusChangedListenerAdded();
        if (done) {
            doneMessage();
        } else if (isStop()) {
            stopMessage();
        } else {
            fireStatusBarEvent(getSplitCount());
        }
    }

    protected void progressChangeListenerAdded() {
        super.progressChangeListenerAdded();
        fireProgressBarEvent();
    }
    public boolean writeToResultfile(){
         try{
        DiscoverySession session = getSession();
        org.geworkbench.util.patterns.PatternDB patternDB = new PatternDB(sequenceInputData.getFile(), null);
        int totalPatternNum = session.getPatternNo();
        for (int i = 0; i <totalPatternNum; i++) {
            //todo why?
//            DSMatchedSeqPattern pattern = getPattern(i);
//            PatternOperations.fill(pattern, sequenceInputData);
//            patternDB.add(pattern);
        }
        //patternDB.setParameters(widget.getParameters());
//        patternDB.write(resultFile);

         }catch (Exception e){
             e.printStackTrace();
         }
        return true;
    }

}
