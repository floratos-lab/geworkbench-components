package org.geworkbench.components.alignment.synteny;

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
public class SyntenyMapObject {
    int FragmentsNum = 0;
    SyntenyMapFragment[] fragments = new SyntenyMapFragment[50];

    public SyntenyMapObject() {
    }

    public void addSyntenyFragment(SyntenyMapFragment smf){
        fragments[FragmentsNum++]=smf;
    }

    public int getFragmentsNum(){
        return FragmentsNum;
    }

    public SyntenyMapFragment getFragment(int n){
        return fragments[n];
    }
}

