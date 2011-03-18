package org.geworkbench.components.discovery;

import polgara.soapPD_wsdl.Parameters;

import java.util.HashMap;

import org.geworkbench.bison.datastructure.complex.pattern.PatternDiscoveryParameters;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Jan 24, 2007
 * Time: 6:28:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParameterTranslation {
    static HashMap<PatternDiscoveryParameters, Parameters> hashMap = new HashMap<PatternDiscoveryParameters, Parameters>();
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
    
    public static PatternDiscoveryParameters translate(Parameters parameters){
		PatternDiscoveryParameters newParameters = new PatternDiscoveryParameters(
				parameters.getMinSupport(),
				parameters.getMinTokens(),
				parameters.getWindow(),
				parameters.getMinWTokens(),
				parameters.getExactTokens(),
				parameters.getCountSeq(),
				parameters.getExact(),
				parameters.getPrintDetails(),
				parameters.getGroupingType(),
				parameters.getGroupingN(),
				parameters.getSortMode(),
				parameters.getOutputMode(),
				parameters.getMinPer100Support(),
				parameters.getComputePValue(),
				parameters.getMinPValue(),
				parameters.getThreadNo(),
				parameters.getThreadId(),
				parameters.getMinPatternNo(),
				parameters.getMaxPatternNo(),
				parameters.getMaxRunTime(),
				parameters.getSimilarityMatrix(),
				parameters.getSimilarityThreshold(),
				parameters.getInputName(),
				parameters.getOutputName());
               
        hashMap.put(newParameters, parameters);
        return newParameters;
    }
/**
 * Get the corresponding previous native version of Parameters based on the new created BISON parameters.
 * @param parameters
 * @return
 */
    public static Parameters getParameters(PatternDiscoveryParameters parameters){
        return hashMap.get(parameters);
    }
}
