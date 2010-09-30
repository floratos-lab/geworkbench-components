package org.geworkbench.components.discovery.algorithm;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.SoapParmsDataSet;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.components.discovery.SequenceDiscoveryViewAppComponent;
import org.geworkbench.events.ProgressChangeEvent;
import org.geworkbench.events.StatusBarEvent;
import org.geworkbench.util.patterns.PatternSorter;
import org.geworkbench.util.patterns.SequentialPatternSource;

import javax.swing.*;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;

/**
 * This class loads saved patterns from a file.
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version $Id$
 */
public class RegularDiscoveryFileLoader extends AbstractSequenceDiscoveryAlgorithm implements org.geworkbench.util.patterns.SequentialPatternSource {
    FileDataSource PatternSource = null;
    org.geworkbench.util.patterns.PatternDB patternDB = null;

    File patternFile = null;
    File sequenceFile = null;
    private String statusBarMessage = "";
    int patternNumber = 0;
    DSDataSet<DSSequence> parent;

    private SequenceDiscoveryViewAppComponent appComponent = null;
	private boolean newNode = false;
    
    public RegularDiscoveryFileLoader(File sequenceFile, File patternFile, final SequenceDiscoveryViewAppComponent appComponent,
    		boolean newNode, DSDataSet<DSSequence> parent) {

        this.sequenceFile = sequenceFile;
        this.patternFile = patternFile;
        
        this.appComponent = appComponent;
        this.newNode = newNode;
        this.parent = parent;
    }

    /**
     * start
     */
    public void start() {
        if (sequenceFile == null) {
            JOptionPane.showMessageDialog(null, "Please first select the sequence file that is\n" + "associated with this pattern file. ");

            return;
        }

        patternDB = new org.geworkbench.util.patterns.PatternDB(sequenceFile, parent);
        String idString =  RandomNumberGenerator.getID();
        patternDB.setID(idString);

        //loading stuff
        if (patternDB.read(patternFile)) {
            PatternSource = new FileDataSource(patternDB);
            statusBarMessage = "Patterns were loaded from: " + patternFile.getAbsoluteFile();
            fireStatusBarChanged(new StatusBarEvent(statusBarMessage));
            patternNumber = patternDB.getPatternNo();
            progressChange();

            if(newNode)
				appComponent.createNewNode(patternDB);

        } else {
            JOptionPane.showMessageDialog(null, "The file " + patternDB.getDataSetName() + " could not be loaded.\n " + "Please make sure that the sequence file" + " is loaded and selected in the project.");
        }

    }

    /* This class is not for loading file instead of getting results, so ProgressChangeEvent is always set to be initial.  */
    private void progressChange() {
        fireProgressChanged(new ProgressChangeEvent(true, patternNumber));
    }

    protected void statusChangedListenerAdded() {
        progressChange();
        fireStatusBarChanged(new StatusBarEvent(statusBarMessage));
    }

    /**
     * stop
     */
    public void stop() {
    }

    /**
     * getPattern
     *
     * @param index int
     * @return Pattern
     */
    public DSMatchedSeqPattern getPattern(int index) {
        return PatternSource.getPattern(index);
    }

    /**
     * getPatternSourceSize
     *
     * @return int
     */
    public int getPatternSourceSize() {
        if (PatternSource == null) {
            return 0;
        }
        return PatternSource.getPatternSourceSize();
    }

    /**
     * mask
     *
     * @param index int[]
     * @param mask  boolean
     */
    public void mask(int[] index, boolean mask) {
        PatternSource.mask(index, mask);
    }

    /**
     * sort
     *
     * @param field int
     */
    public void sort(int field) {
        PatternSource.sort(field);
    }
}

/**
 * An adapter class for showing Patterns from a file.
 */
class FileDataSource implements SequentialPatternSource {
    //out pattern source
    private DSMatchedSeqPattern[] pattern = null;

    //Used to sort patterns
    static private PatternSorter sorter = new PatternSorter();

    public FileDataSource(org.geworkbench.util.patterns.PatternDB db) {
        pattern = new DSMatchedSeqPattern[db.getPatternNo()];
        for (int i = 0; i < pattern.length; ++i) {
            pattern[i] = db.getPattern(i);
        }
    }

    public int getPatternSourceSize() {
        return pattern.length;
    }

    public DSMatchedSeqPattern getPattern(int i) {
        return pattern[i];
    }

    public void sort(int field) {
        sorter.setMode(field);
        Arrays.sort(pattern, sorter);
    }

    public void mask(int[] index, boolean mask) {
        //does nothing for local patterns
    }
}
