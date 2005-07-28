package org.geworkbench.components.alignment.panels;

import org.geworkbench.algorithms.BWAbstractAlgorithm;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: AMDeC_Califano lab</p>
 *
 * @author XZ
 * @version 1.0
 */

public class ParameterSetter {
    public ParameterSetter() {
    }


    private org.geworkbench.algorithms.BWAbstractAlgorithm algo;

    public BWAbstractAlgorithm getAlgo() {
        return algo;
    }

    public void setAlgo(BWAbstractAlgorithm algo) {
        this.algo = algo;
    }

}
