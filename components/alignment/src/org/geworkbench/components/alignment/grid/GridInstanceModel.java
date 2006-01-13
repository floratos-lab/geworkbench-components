package org.geworkbench.components.alignment.grid;

import java.util.Observable;

/**
 * <p>Title: Bioworks</p>
 *
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 *
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 *
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GridInstanceModel
    extends Observable {
    public GridInstanceModel() {
        super();
        init();
    }

    private Object[] instanceArray;
    private int currentInstance;
    public void init(){
        instanceArray = new GridUtil().getInstances();
    }

    public Object[] getInstanceArray() {
        return instanceArray;
    }

    public void setInstanceArray(Object[] instanceArray) {
        this.instanceArray = instanceArray;
    }
}
