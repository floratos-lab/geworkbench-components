package org.geworkbench.components.discovery;

import polgara.soapPD_wsdl.Parameters;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Jan 24, 2007
 * Time: 6:28:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParameterTranslation {
    static HashMap<org.geworkbench.bison.datastructure.complex.pattern.Parameters, Parameters> hashMap = new HashMap<org.geworkbench.bison.datastructure.complex.pattern.Parameters, Parameters>();
    static private ParameterTranslation parameterTranslation;
    public ParameterTranslation(){

    }
    public static synchronized
	 ParameterTranslation getParameterTranslation(){
        if(parameterTranslation==null){
            parameterTranslation = new ParameterTranslation();
        }
        return parameterTranslation;
    }
    public static org.geworkbench.bison.datastructure.complex.pattern.Parameters translate(Parameters parameters){
      org.geworkbench.bison.datastructure.complex.pattern.Parameters newParameters   = new org.geworkbench.bison.datastructure.complex.pattern.Parameters();
        //@todo  why only few values are translated? xz/
        
        newParameters.setMinSupport(parameters.getMinSupport());
       newParameters.setMinTokens(parameters.getMinTokens());
       newParameters.setWindow(parameters.getWindow());
         newParameters.setExactTokens(parameters.getExactTokens());
        newParameters.setCountSeq(parameters.getCountSeq() );
        newParameters.setExact(parameters.getExact());
       // printDetails == parameters.getPrintDetails() && groupingType == parameters.getGroupingType() && groupingN == parameters.getGroupingN() && sortMode == parameters.getSortMode() && outputMode == parameters.getOutputMode() && minPer100Support == parameters.getMinPer100Support() && computePValue == parameters.getComputePValue() && minPValue == parameters.getMinPValue() && threadNo == parameters.getThreadNo() && threadId == parameters.getThreadId() && minPatternNo == parameters.getMinPatternNo() && maxPatternNo == parameters.getMaxPatternNo() && maxRunTime == parameters.getMaxRunTime() && (similarityMatrix == null && parameters.getSimilarityMatrix() == null || similarityMatrix != null && similarityMatrix.equals(parameters.getSimilarityMatrix())) && similarityThreshold == parameters.getSimilarityThreshold() && (inputName == null && parameters.getInputName() == null || inputName != null && inputName.equals(parameters.getInputName())) && (outputName == null && parameters.getOutputName() == null || outputName != null && outputName.equals(parameters.getOutputName()))
        hashMap.put(newParameters, parameters);
        return newParameters;
    }

    public static Parameters getParameters(org.geworkbench.bison.datastructure.complex.pattern.Parameters parameters){
        return hashMap.get(parameters);
    }
}
