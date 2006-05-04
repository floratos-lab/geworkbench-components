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
public class FeatureMatrixObjList {
    int alloc_num;
    int real_num;
    int min=0;
    int max=0;
    String featureName=new String("Unnamed feature");
    String[] x_names;
    String[] y_names;
    FeatureMatrixObj[] fmObj;

    public FeatureMatrixObjList() {
        alloc_num=50;
        real_num=0;
        x_names=new String[50];
        y_names=new String[50];
        fmObj=new FeatureMatrixObj[50];
    }

    public FeatureMatrixObjList(int n) {
        alloc_num=n;
        real_num=0;
        x_names=new String[n];
        y_names=new String[n];
        fmObj=new FeatureMatrixObj[n];
    }

    public void addFeatureMatrixObj(FeatureMatrixObj fmo,String xn, String yn){
        if(real_num<alloc_num){
            fmObj[real_num]=fmo;
            x_names[real_num]=xn;
            y_names[real_num]=xn;
            if(min>fmo.getMin()){
                min=fmo.getMin();
            }
            if(max<fmo.getMax()){
                max=fmo.getMax();
            }
            real_num++;
        } else {
            System.out.println("Warning: Maximum number of FeatureMatrixObjects in list reached!");
            // Here should be reallocation of objects
            return;
        }
    }

    public void setFeatureName(String fn){
        featureName=fn;
    }

    public String getFeatureName(){
        return featureName;
    }

    public int getNum(){
        return real_num;
    }

    public int getMin(){
        return min;
    }

    public int getMax(){
        return max;
    }

    public String getXName(int n){
        return x_names[n];
    }

    public String getYName(int n){
        return y_names[n];
    }

    public FeatureMatrixObj getFMObj(int n){
        return fmObj[n];
    }
}
