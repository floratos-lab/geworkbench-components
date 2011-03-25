package org.geworkbench.components.gpmodule.kmeans;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.KMeansResult;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.gpmodule.GPAnalysis;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.ProgressBar;
import org.genepattern.client.GPClient;
import org.genepattern.util.GPpropertiesManager;
import org.genepattern.webservice.AnalysisWebServiceProxy;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.Parameter;
import org.genepattern.webservice.WebServiceException;

/**
 *
 * @author zm2165
 * @version $Id$
 */

public class KMAnalysis extends GPAnalysis{	
	
	private static final long serialVersionUID = -8885144423769088617L;
	private int localAnalysisType;
	private static Log log = LogFactory.getLog(KMAnalysis.class);	

	private static final String RESULT_TEXT_FILE="stdout.txt";
	private static final int MAXIUM_MARKERS_ONELINE=14;
	private static final int INDEX_OF_GENE=0;
	private static final int INDEX_OF_ARRAY=1;
	
	private int numGenes;
	private String clusterNum;
	private int clusterBy;
	
	private Parameter[] parameters = new Parameter[6];
	
	
	public KMAnalysis() {
		//localAnalysisType = AbstractAnalysis.KMEANS_ANALYSIS_TYPE;
		setDefaultPanel(new KMeansPanel());        
	}
	
	

	public int getAnalysisType() {
		return localAnalysisType;
	}

	@Override
	public AlgorithmExecutionResults execute(Object input) {
		return calculate(input, false);
	}

	@SuppressWarnings("unchecked")
	AlgorithmExecutionResults calculate(Object input,
			boolean calledFromOtherComponent) {
		
		DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> data = (DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray>) input;
		boolean allArrays = !data.useItemPanel();
		log.info("All arrays: " + allArrays);

		if (input == null || !(input instanceof DSMicroarraySetView)) {
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		}
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		
		try{
			clusterNum=((KMeansPanel) aspp).getNumClusters();
			clusterBy=((KMeansPanel) aspp).getClusterBy();
			if(Integer.parseInt(clusterNum)<2)
				return new AlgorithmExecutionResults(false, "Number of clusters should not be less than 2.", null);
			if((view.items().size()<Integer.parseInt(clusterNum))&&clusterBy==INDEX_OF_ARRAY)
				return new AlgorithmExecutionResults(false, "Number of clusters should be greater than the size of arrays", null);
			if((view.markers().size()<Integer.parseInt(clusterNum))&&clusterBy==INDEX_OF_GENE)
				return new AlgorithmExecutionResults(false, "Number of clusters should be greater than the size of markers", null);
		}
		catch(NumberFormatException e){			
			return new AlgorithmExecutionResults(false,
					"Parameters are not valid.", null);
		}	
		
		ProgressBar pbFCtest = ProgressBar
			.create(ProgressBar.INDETERMINATE_TYPE);
		pbFCtest.addObserver(this);
		pbFCtest.setTitle("K-Means Clustering Analysis");
		pbFCtest.setBounds(new ProgressBar.IncrementModel(0, numGenes, 0,
				numGenes, 1));
		
		pbFCtest.setMessage("getting results from server, please wait...");
		pbFCtest.start();
		this.stopAlgorithm = false;	
		
		String gctFileName = createGCTFile("KMDataset", view.markers(),
				view.items()).getAbsolutePath();
		
		
		numGenes = data.markers().size();
		
		DSDataSet<? extends DSBioObject> set = data.getDataSet();

		if (!(set instanceof DSMicroarraySet)) {
			pbFCtest.dispose();
			return null;
		}

		DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) set;			
		
		DSAnnotationContextManager manager = CSAnnotationContextManager
			.getInstance();
		DSAnnotationContext<DSMicroarray> arraySet = manager
			.getCurrentContext(maSet);

		parameters[0] = new Parameter("input.filename", gctFileName);
        parameters[1] = new Parameter("output.base.name", "<input.filename_basename>_KMcluster_output");
        parameters[2] = new Parameter("number.of.clusters", clusterNum); 
        parameters[3] = new Parameter("seed.value", "12345");
        parameters[4] = new Parameter("cluster.by", Integer.toString(clusterBy));
        parameters[5] = new Parameter("distance.metric", "0");		
        File resultFile=null; 
		try{
			resultFile = trainData("urn:lsid:broad.mit.edu:cancer.software.genepattern.module.analysis:00081:1", parameters);
		}
		catch (ClassifierException e){
			return new AlgorithmExecutionResults(false, "trainData process aborted.", null);
		}
		
		ArrayList<List<String[]>> resultList=new ArrayList<List<String[]>>();
		
		String resultText="";
		
		DSItemList<DSGeneMarker> dsList=maSet.getMarkers();
		
		try{
			FileReader reader=new FileReader(resultFile);
			Scanner in = new Scanner(reader);
			boolean clusterFollow=false;
			boolean clusterContinue=false;
			List<String[]> aClusterItems=null;
			while (in.hasNextLine()){
				String line = in.nextLine();
				resultText+=line+"\n";
				
				int numOfItemsInLine=0;
				
				if(clusterFollow){
					if(!clusterContinue)
						aClusterItems=new ArrayList<String[]>();
					String[] itemsStr = line.split ("[\\t\\n]");
					
						for(String s:itemsStr){
							String[] anItemRow=new String[3];
							anItemRow[0]=s;
							anItemRow[1]="n/a";
							if(clusterBy==INDEX_OF_GENE){//markers
								for(DSGeneMarker d:dsList){							
									if(d.getLabel().equalsIgnoreCase(s)){
										anItemRow[1]=d.getGeneName();
										anItemRow[2]=d.getDescription();
									}								
								}						
							}
							else{	//arrays
								String arrayMemberShip=null;
								for (int cx = 0; cx < arraySet.getNumberOfLabels(); cx++) {								
									DSPanel<DSMicroarray> selectorArrayPanel=
										(DSPanel<DSMicroarray>) arraySet.getItemsWithLabel(arraySet.getLabel(cx));								
									for(int i=0;i<selectorArrayPanel.getNumberOfProperItems();i++){									
										if(s.equalsIgnoreCase(selectorArrayPanel.get(i).toString())){
											if(arrayMemberShip==null)
												arrayMemberShip=arraySet.getLabel(cx);
											else
												arrayMemberShip+=", "+arraySet.getLabel(cx);	
										}
									}
										
										
								}
								anItemRow[1]=arrayMemberShip;
							
							}
							if (!(anItemRow[0].equals("")||anItemRow[0].equals(" "))){//sometimes there are extra line feed in the results
								aClusterItems.add(anItemRow);
								numOfItemsInLine++;
							}
						}
						
					if(numOfItemsInLine<MAXIUM_MARKERS_ONELINE)
						resultList.add(aClusterItems);
				}
				int markersAhead1=line.indexOf("Cluster:");
				int markersAhead2=line.indexOf("Size :");
				if(!((clusterFollow)&&(numOfItemsInLine>=MAXIUM_MARKERS_ONELINE))){
					clusterContinue=false;
					if((markersAhead1!=-1)&&(markersAhead2!=-1))
						clusterFollow=true;									
					else
						clusterFollow=false;				
				}
				else
					clusterContinue=true;
			}//end of line process
			if(clusterContinue){
				resultList.add(aClusterItems);
			}
			in.close();
			reader.close();
			
		}
		catch(Exception e){
			return new AlgorithmExecutionResults(false, "No invalid output.", null);
		}
		
		String histHeader = null;
		String histMarkerString = GenerateMarkerString(data);		
		
		KMeansResult analysisResult = new KMeansResult(maSet,"K-Means Clustering",
				clusterBy,resultText,resultList);
		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"K-Means Analysis", analysisResult);
		
		// add data set history.
		histHeader = GenerateHistoryHeader();
		String stemp=histHeader + histMarkerString;
		ProjectPanel.addToHistory(analysisResult, stemp );
		
		pbFCtest.dispose();
		
		return results;
		
	} // end of method calculate

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}

	protected String GenerateHistoryHeader() {

		String histStr = "";
		// Header
		histStr += "K-Means Clustering Analysis with the following parameters:\n";
		histStr += "----------------------------------------------------------\n";

		histStr += "Input Parameters:" + "\n";			
		histStr += "\t" + "Number of cluster: " + clusterNum + "\n";
		String st= (clusterBy==INDEX_OF_GENE)? "Gene" :"Array";
		histStr += "\t" + "Cluster by: "+ st + "\n";
		histStr += "\t" + "Distance metric: "+ "Euclidean" + "\n";
		
		return histStr;
	}

	String GenerateMarkerString(
			DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> view) {
		String histStr = null;

		histStr = view.markers().size() + " markers analyzed:\n";
		for (DSGeneMarker marker : view.markers()) {
			histStr += "\t" + marker.getLabel() + "\n";
		}
		return histStr;
	}	
	
    public File trainData(String classifierName, Parameter[] parameters) throws ClassifierException
    {
        File modelFile = null;
        try
        {
            String serverName = GPpropertiesManager.getProperty("gp.server");
            String userName = GPpropertiesManager.getProperty("gp.user.name");
/*            
            String password = ((GPTrainingPanel)this.panel).getPassword();

            String passwordRequired = ((GPTrainingPanel)this.panel).
                    getConfigPanel().passwordRequired(serverName, userName);

            //Check if password needs to be entered
            if((password == null || password.equals("")) && (passwordRequired != null &&  passwordRequired.equals("true")))
            {
                ((GPTrainingPanel)this.panel).
                    getConfigPanel().highlightPassword(true);
                ((GPTrainingPanel)this.panel).
                    getConfigPanel().showEditServerSettingsFrame("Please enter your password");
                password = ((GPTrainingPanel)this.panel).getPassword();
                ((GPTrainingPanel)this.panel).
                    getConfigPanel().highlightPassword(false);
            }

            if(password == null)
            */
             String   password="";
            GPClient server = new GPClient(serverName, userName, password);
            
            JobResult analysisResult = server.runAnalysis(classifierName, parameters);
           
            String modelFileName = null;
            modelFileName =RESULT_TEXT_FILE;
         
           
            if(modelFileName == null)
                throw new ClassifierException("Error: model could not be generated from server.");

            //download model result file from server
            AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
            String[] resultFiles = new String[1] ;
            resultFiles[0] = modelFileName;

            File[] result = analysisProxy.getResultFiles(analysisResult.getJobNumber(), resultFiles, new File(FilePathnameUtils.getTemporaryFilesDirectoryPath()), true);
            if(result == null || result.length == 0)
                throw new ClassifierException("Error: Could not retrieve training model from GenePattern");

            modelFile =  result[0];

            // remove job from GenePattern server
            analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
            analysisProxy.purgeJob(analysisResult.getJobNumber());
        }
        catch(WebServiceException we)
        {
             we.printStackTrace();

             if(we.getMessage().indexOf(classifierName + " not found on server") != -1)
             {
                throw new ClassifierException(classifierName + " module not found on GenePattern server");
             }
             else
                throw new ClassifierException("Could not connect to GenePattern server");
        }
        catch(ClassifierException ce)
        {
            throw ce;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new ClassifierException("Error creating " + classifierName + " model");
        }

        return modelFile;
    }
	
    

}
