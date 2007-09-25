package org.geworkbench.components.ei;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Sep 19, 2007
 * Time: 4:01:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoldStandardSummary {
    String name;
    int totalPP;//total positive links
    int totalFF;//total negative links

    public GoldStandardSummary(String _name, int pp, int ff) {
        name = _name;
        totalFF = ff;
        totalPP = pp;
    }

    public String toString ()

    {
        return name;
    }
}
