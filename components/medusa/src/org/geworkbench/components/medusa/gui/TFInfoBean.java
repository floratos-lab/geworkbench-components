package org.geworkbench.components.medusa.gui;

/**
 * Created by IntelliJ IDEA.
 * User: kk2457
 * Date: Aug 2, 2007
 * Time: 1:59:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class TFInfoBean {
    private String name;
    private String description;
    private String source;
    private double pssm[][];
    double distance = -1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public double[][] getPssm() {
        return pssm;
    }

    public void setPssm(double[][] pssm) {
        this.pssm = pssm;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }


}
