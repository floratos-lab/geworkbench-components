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

public class SyntenyMapFragment {

    String UpperName = null;
    String LowerName = null;
    String UpperChromosome = null;
    String LowerChromosome = null;
    String UpperGenome = null;
    String LowerGenome = null;
    String[] WeightNames = null;
    String[] UpperObjectsNames = null;
    String[] LowerObjectsNames = null;

    int UpperFrom = 0;
    int UpperTo = 0;
    int LowerFrom = 0;
    int LowerTo = 0;

    int[] UpperObjectsStarts = null;
    int[] LowerObjectsStarts = null;
    int[] UpperObjectsEnds = null;
    int[] LowerObjectsEnds = null;

    int[][] SyntenyPairs = null;
    double[][] SyntenyPairsWeight = null;

    int SyntenyPairsNum = 0;
    int UpperObjectsNum = 0;
    int LowerObjectsNum = 0;
    int WeightTypesNum = 1;
    int ActiveWeightType = 0;

    public SyntenyMapFragment(int un, int ln){

        int np = un * ln;

        // upper number, lower number, number of pairs
        SyntenyPairs = new int[np][2];
        SyntenyPairsWeight = new double[np][];

        UpperObjectsNames = new String[un];
        LowerObjectsNames = new String[ln];

        UpperObjectsStarts = new int[un];
        LowerObjectsStarts = new int[ln];
        UpperObjectsEnds = new int[un];
        LowerObjectsEnds = new int[ln];

        SyntenyPairsNum = np;
        UpperObjectsNum = un;
        LowerObjectsNum = ln;
    }

    /**
     * Setting names
     * @param nms String[]
     */
    public void setUpperNames(String[] nms){
        for(int i=0; i<UpperObjectsNum;i++){
            UpperObjectsNames[i]=new String(nms[i]);
        }
    }

    public void setLowerNames(String[] nms){
        for(int i=0; i<LowerObjectsNum;i++){
            LowerObjectsNames[i]=new String(nms[i]);
        }
    }

    public void setUpperName(int n, String nm){
            UpperObjectsNames[n]=new String(nm);
    }

    public void setLowerName(int n, String nm){
            LowerObjectsNames[n]=new String(nm);
    }

    public void setWeightName(int n, String nm){
            WeightNames[n]=new String(nm);
    }

//    public void setWeightTypesNum(int n){
//        WeightNames = new String[n];
//        for(int i=0;i<SyntenyPairsNum;i++){
//            SyntenyPairsWeight[i]=new double[n];
//        }
//    }

    public void setWeightTypeNum(int n){

        WeightTypesNum = n;
        WeightNames = new String[WeightTypesNum];
        for(int j=0;j<SyntenyPairsNum;j++){
            SyntenyPairsWeight[j]=new double[WeightTypesNum];
        }
    }

    /**
     * Setting starts
     * // @param nms String[]
     */
    public void setUpperStarts(int[] sta){
        for(int i=0; i<UpperObjectsNum;i++){
            UpperObjectsStarts[i]=sta[i];
        }
    }

    public void setLowerStarts(int[] sta){
        for(int i=0; i<LowerObjectsNum;i++){
            LowerObjectsStarts[i]=sta[i];
        }
    }

    public void setUpperStart(int n, int s){
            UpperObjectsStarts[n]=s;
    }

    public void setLowerStart(int n, int s){
            LowerObjectsStarts[n]=s;
    }

    /**
     * Setting starts
     * //@param nms String[]
     */
    public void setUpperEnds(int[] sta){
        for(int i=0; i<UpperObjectsNum;i++){
            UpperObjectsEnds[i]=sta[i];
        }
    }

    public void setLowerEnds(int[] sta){
        for(int i=0; i<LowerObjectsNum;i++){
            LowerObjectsEnds[i]=sta[i];
        }
    }

    public void setUpperEnd(int n, int s){
            UpperObjectsEnds[n]=s;
    }

    public void setLowerEnd(int n, int s){
            LowerObjectsEnds[n]=s;
    }

    public void setActiveWeightType(int n){
            ActiveWeightType=n;
    }

    /**
     * Setting pairs
     * //@param nms String[]
     */
    public void setPairs(int[] first, int[] second, int[] weight){
        for(int i=0;i<SyntenyPairsNum;i++){
            SyntenyPairs[i][0]=first[i];
            SyntenyPairs[i][1]=second[i];
            SyntenyPairsWeight[i][ActiveWeightType]=weight[i];
        }
    }

    public void addPair(int n, int first, int second, double weight){
        SyntenyPairs[n][0]=first;
        SyntenyPairs[n][1]=second;
        SyntenyPairsWeight[n][ActiveWeightType]=weight;
    }

    public void setUpperName(String nm){
        if(nm != null){
            UpperName = new String(nm);
        }
    }

    public void setLowerName(String nm){
        if(nm != null){
            LowerName = new String(nm);
        }
    }

    public void setLowerChromosome(String chr){
        if(chr != null){
            LowerChromosome = new String(chr);
        }
    }

    public void setUpperChromosome(String chr){
        if(chr != null){
            UpperChromosome = new String(chr);
        }
    }

    public void setLowerGenome(String gnm){
        if(gnm != null){
            LowerGenome = new String(gnm);
        }
    }

    public void setUpperGenome(String gnm){
        if(gnm != null){
            UpperGenome = new String(gnm);
        }
    }

    public void setUpperCoordinates(int from, int to){
        UpperFrom = from;
        UpperTo = to;
    }

    public void setLowerCoordinates(int from, int to){
        LowerFrom = from;
        LowerTo = to;
    }

    /**
     * Accessors
     */

    public String getWeightName(int n){
        if(WeightNames[n]!=null)
            return WeightNames[n];
        else
            return "Unknown";
    }

    public int getWeightTypesNum(){
            return WeightTypesNum;
    }

    public String getSingleUpperName(int n){
            return UpperObjectsNames[n];
    }
    public String getSingleLowerName(int n){
            return LowerObjectsNames[n];
    }

    public int getSingleUpperStart(int n){
            return UpperObjectsStarts[n];
    }

    public int getSingleLowerStart(int n){
            return LowerObjectsStarts[n];
    }

    public int getSingleUpperEnd(int n){
            return UpperObjectsEnds[n];
    }

    public int getSingleLowerEnd(int n){
            return LowerObjectsEnds[n];
    }

    public int getPairNum(){
            return SyntenyPairsNum;
    }

    public int[] getPair(int n){
        return SyntenyPairs[n];
    }

    public double getPairWeight(int n){
        return SyntenyPairsWeight[n][ActiveWeightType];
    }

    public String getUpperName(){
        return UpperName;
    }

    public String getLowerName(){
        return LowerName;
    }

    public String getLowerChromosome(){
        return LowerChromosome;
    }

    public String getUpperChromosome(){
        return UpperChromosome;
    }

    public String getLowerGenome(){
        return LowerGenome;
    }

    public String getUpperGenome(){
        return UpperGenome;
    }

    public int getUpperCromosomeStart(){
        return UpperFrom;
    }
    public int getUpperCromosomeEnd(){
        return UpperTo;
    }

    public int getLowerCromosomeStart(){
        return LowerFrom;
    }
    public int getLowerCromosomeEnd(){
        return LowerTo;
    }
    public int getLowerSpan(){
        return LowerTo-LowerFrom+1;
    }
    public int getUpperSpan(){
        return UpperTo-UpperFrom+1;
    }
    public int getSpan(){
        int ls=getLowerSpan();
        int us=getUpperSpan();

        if(ls > us)
            return ls;
        else
            return us;
    }
    public int getLowerObjectsNum(){
        return LowerObjectsNum;
    }
    public int getUpperObjectsNum(){
        return UpperObjectsNum;
    }
    public int getUpperObjectStart(int n){
        return UpperObjectsStarts[n];
    }
    public int getUpperObjectEnd(int n){
        return UpperObjectsEnds[n];
    }
    public int getLowerObjectStart(int n){
        return LowerObjectsStarts[n];
    }
    public int getLowerObjectEnd(int n){
        return LowerObjectsEnds[n];
    }
    public int getLowerPair(int n){
        return SyntenyPairs[n][1];
    }
    public int getUpperPair(int n){
        return SyntenyPairs[n][0];
    }
    public void adjustCoordinates(){
        int i;

        for (i = 0; i < UpperObjectsNum; i++) {
            if (UpperFrom > UpperObjectsStarts[i]) UpperFrom =
                    UpperObjectsStarts[i] - 20;
            if (UpperTo < UpperObjectsEnds[i]) UpperTo = UpperObjectsEnds[i] +
                    20;
        }
        for (i = 0; i < LowerObjectsNum; i++) {
            if (LowerFrom > LowerObjectsStarts[i]) LowerFrom =
                    LowerObjectsStarts[i] - 20;
            if (LowerTo < LowerObjectsEnds[i]) LowerTo = LowerObjectsEnds[i] +
                    20;
        }
    }
}
