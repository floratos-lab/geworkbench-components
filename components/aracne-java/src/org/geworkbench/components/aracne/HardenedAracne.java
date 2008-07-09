/*
 * The aracne-java project
 * 
 * Copyright (c) 2008 Columbia University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.geworkbench.components.aracne;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import edu.columbia.c2b2.aracne.Parameter;
import wb.data.MicroarraySet;
import wb.plugins.aracne.GraphEdge;
import wb.plugins.aracne.WeightedGraph;
import edu.columbia.c2b2.aracne.Aracne;

/**
 * @author zji
 * @version $Id: HardenedAracne.java,v 1.1 2008-07-09 18:33:09 jiz Exp $
 *
 */
public class HardenedAracne {
    static Log log = LogFactory.getLog(HardenedAracne.class);

	private static class AracneTask implements Callable<WeightedGraph> {
		private MicroarraySet microarraySet;
		private Parameter parameter;
		
		AracneTask(MicroarraySet microarraySet, Parameter parameter) {
			this.microarraySet = microarraySet;
			this.parameter = parameter;
		}

		public WeightedGraph call() {
			return Aracne.run(microarraySet, parameter);
		}
		
	}
	
	/*
	 * this method is used in place of Aracne.run(MicroarraySet microarraySet, Parameter parameter)
	 * to make ARACNE 'hardened' - bootstrap and parallel
	 */
	// two parameters (bootstrap & pthreshold) are not part of edu.columbia.c2b2.aracne.Parameter 
	// because they are from the perl script instead of aracne-java
	public static WeightedGraph run(MicroarraySet microarraySet, Parameter parameter, int bootstrapNumber, double pThreshold) {
		if(bootstrapNumber==1)return Aracne.run(microarraySet, parameter);
		
		if(parameter.getSample()!=0) {
			System.out.println("this should never happen. fatal: the initial parameter is not zero");
			System.exit(1);
		}
		/*
		 * to test the improvement of parallel computing over multiple core/processor
		 * compare with ExecutorService executor = Executors.newSingleThreadExecutor();
		 */
		ExecutorService executor = Executors.newCachedThreadPool();
		List<Callable<WeightedGraph>> tasks = new ArrayList<Callable<WeightedGraph>>();
		int bootstrapId = 1;
		/* in the bootstrap process, dataset itself is not duplicated, only multiple lists of ID numbers are produced */
		for(int i=0; i<bootstrapNumber; i++) {
			Parameter param = copyParameter(parameter, bootstrapId);
			tasks.add(new AracneTask(microarraySet, param));
			bootstrapId++;
		}
		
		WeightedGraph[] graph = new WeightedGraph[bootstrapNumber];
		long begin = new Date().getTime(); // in case to log the execution duration
		try {
			// without setting time-out, when invokeAll returns, everything should have finished
			List<Future<WeightedGraph>> futures = executor.invokeAll(tasks);
			int i= 0;
			for(Future<WeightedGraph> f: futures) {
				graph[i] = f.get();
				log.debug("graph #"+i+": number of edges="+graph[i].getEdges().size());
				i++;
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return null; // if something was wrong, don't continue to process incomplete bootstraps
			// Aracne.run(...) chooses to return null at certain exception as well
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null; // if something was wrong, don't continue to process incomplete bootstraps
		}
		log.info("It took ExecutorService executor "+(new Date().getTime()-begin)+" milliseconds to finish.");
		
		return consensusNetwork(graph, parameter, pThreshold);
	}
	
	/**
	 * clone a Parameter object except the 'sample' field
	 * this is necessary because we need to maintain the multiple instances for parallel computing
	 * @param old
	 * @param sample
	 * @return
	 */
	private static Parameter copyParameter(Parameter old, int sample) {
		Parameter copy = new Parameter();
	    
		copy.setThreshold(old.getThreshold());
		copy.setPvalue(old.getPvalue());
		copy.setEps(old.getEps());
		copy.setSigma(old.getSigma());
		copy.setMiSteps(old.getMiSteps());
		copy.setSample(sample); //old.getSample());
		copy.setPercent(old.getPercent());
		copy.setMean(old.getMean());
		copy.setCv(old.getCv());
		copy.setCorrection(old.getCorrection());
		copy.setAlgorithm(old.getAlgorithm());
		copy.setInfile(old.getInfile());
		copy.setOutfile(old.getOutfile());
		copy.setAdjfile(old.getAdjfile());
		copy.setPrecomputedAdjacencies(old.getPrecomputedAdjacencies());
		copy.setHub(old.getHub());
		copy.setSubnetfile(old.getSubnetfile());
		copy.setAnnotfile(old.getAnnotfile());
		copy.setControlId(old.getControlId());
		copy.setCondition(old.getCondition());
		copy.setHome_dir(old.getHome_dir());
		copy.setSubnet(old.getSubnet());
		copy.setTf_list(old.getTf_list());
		copy.setSuppressFileWriting(old.isSuppressFileWriting());

		return copy;
	}
	
	private static NormalDistribution normalDistribution = new NormalDistributionImpl();
	/**
	 * this method constructs consensus network
	 * this emulates the algorithm in the perl script getconsensusnet.pl written by Dr. Califano's group
	 * @param  listOfGraph
	 * @param parameter - this is needed only for the output file name
	 */
	private static WeightedGraph consensusNetwork(WeightedGraph[] listOfGraph, Parameter parameter, double pThreshold) {
		// many variables have a shorten instead of easier-to-read name to map to the original perl script
		// so as to easier to verify the correctness of the algorithm 
		int bootstrapNumber = listOfGraph.length;
		int[] totEdge = new int[bootstrapNumber];
		Map<String, Integer> totSupport = new HashMap<String, Integer>();
		Map<String, Double> totMI = new HashMap<String, Double>();
		
		for(int i=0; i<bootstrapNumber; i++)totEdge[i] = 0;
		
		int bsnum = 0; // this index and some following start with 0, which is different from the perl script
		for(WeightedGraph graph: listOfGraph) {
			/*
			 * In Aracne-java, the duplicated edge (based on the key) exit in the WeightedGraph
			 * only when it is written out on disk file (in Matri.writeGeneLine(PrintStream out, MicroarraySet data, int Id, MarkerStats stats),
			 * they are removed
			 * consequently, the removal needs to be taken care of here to make sure they are counted more than once.
			 * Otherwise, the total edges may be more than the support
			 */
			Set<String> graphKeys = new HashSet<String>();
			for(GraphEdge edge: graph.getEdges()) {
				String hubid = edge.getNode1();
				String node2 = edge.getNode2();
				String key = hubid+"."+node2;
				if(!graphKeys.contains(key)) {
					graphKeys.add(key);
					Integer support = totSupport.get(key);
					if(support==null) {
						totSupport.put(key, 1);
						totMI.put(key, (double)edge.getWeight());
					} else {
						totSupport.put(key, support+1);
						totMI.put(key, totMI.get(key)+edge.getWeight());
					}
					totEdge[bsnum]++;
				}
			}
			bsnum++;
		}
		
		int totedge = totSupport.size();
		System.out.println("total edges tested: "+totedge);
		System.out.println("Bonferroni corrected (0.05) alpha: " + (0.05/totedge) );
		double mu = 0;
		double sigma = 0;
		int keySize = totSupport.keySet().size();
		for(int bs=0; bs<bootstrapNumber; bs++) {
			double prob = (double)totEdge[bs]/keySize;
			mu += prob;
			sigma += prob*(1.-prob);
		}
		sigma = Math.sqrt(sigma);
		System.out.println("mu: "+mu+"\nsigma: "+sigma);
		
		Set<String> keySet = totSupport.keySet();
		SortedSet<String> sortedKeySet = new TreeSet<String>(keySet);
		
		PrintWriter pw = null;
		if(!parameter.isSuppressFileWriting()) {
			try {
				pw = new PrintWriter(new FileWriter(parameter.getOutfile()));
			} catch (IOException e1) {
				System.out.println("fail opening output file "+ parameter.getOutfile());
				e1.printStackTrace();
				// don't do anything in case the output file is not set - very like this is used so intentionally
				// null pw will make the following code not to try to output
			}
		}
		
        WeightedGraph graph = new WeightedGraph("Consensus Network");
        String currentg1 = "-1";
        // only for the purpose of writing a disk file (.adj file) of the network, key set should be sorted first
        for (String key: sortedKeySet) {
        	
        	String[] gene = key.split("\\.");
        	
        	double z = (totSupport.get(key)-mu)/sigma;
        	try {
				double pval = 1. - normalDistribution.cumulativeProbability(z); // this is equivalent to Statistics:Distributions:uprob($z); in perl
				if(pval<pThreshold) {
		        	/* WeightedGraph.addNode is deprecated: it must not be needed any more.
					 Logically the information about the two nodes is duplicated of that of the edge, 
					 so addNode is probably ignored in the actual implementation.
					 The source code at
					 http://gforge.nci.nih.gov/svn/gforge/workbook/trunk/src/wb/plugins/aracne/WeightedGraph.java
					 is incorrect concerning this and may not be the latest version */
		        	//graph.addNode(gene[0]);
		        	//graph.addNode(gene[1]);
		        	float mi =  (float)(totMI.get(key)/totSupport.get(key));
		        	graph.addEdge(gene[0], gene[1], mi);
		        	
		        	// output disk file
		        	if(pw!=null) {
		        		if(!gene[0].equals(currentg1)) {
		        			if(!currentg1.equals("-1")) {
		        				pw.println(); // start a new line
		        			}
		        			pw.print(gene[0]+"\t");
		        			currentg1 = gene[0];
		        		}
		        		pw.format("%s\t%.4f\t", gene[1], mi);
		        	}
		        	// verify the format on console
		        	/*
	        		if(!gene[0].equals(currentg1)) {
	        			if(!currentg1.equals("-1")) {
	        				System.out.println(); // start a new line
	        			}
	        			System.out.print(gene[0]+"\t");
	        			currentg1 = gene[0];
	        		}
	        		System.out.format("%s\t%.4f\t", gene[1], mi);
	        		*/
				}
			} catch (MathException e) {
				e.printStackTrace();
				// in case of unexpected numerical anomaly happens, the edge is not added to the graph 
			} 
			
			if(pw!=null) {
				pw.close();
			}
        }
        return graph;
	}

}
