/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2010) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.genepattern.client.GPClient;
import org.genepattern.io.cls.ClsWriter;
import org.genepattern.io.gct.GctWriter;
import org.genepattern.matrix.AbstractDataset;
import org.genepattern.matrix.ClassVector;
import org.genepattern.matrix.Dataset;
import org.genepattern.matrix.DefaultClassVector;
import org.genepattern.util.GPpropertiesManager;
import org.genepattern.webservice.AnalysisWebServiceProxy;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.Parameter;
import org.geworkbench.bison.algorithm.classification.CSVisualClassifier;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.PredictionModel;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.gpmodule.GPDataset;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.FilePathnameUtils;

/**
 * @author Marc-Danie Nazaire
 * @version $Id$
 */
public abstract class GPClassifier extends CSVisualClassifier
{
	private static final long serialVersionUID = -7154110880713522340L;
	
	protected PredictionModel predModel;
    protected GPDataset dataset;
    private String password;
    protected String moduleName;

    protected GPClassifier(String moduleName, DSDataSet<?> parent, String label, String[] classifications, PredictionModel model, GPDataset dataset,
                           DSPanel<DSMicroarray> casePanel, DSPanel<DSMicroarray> controlPanel)
    {
        super(parent, label, classifications, model.getModelFileContent(), Arrays.asList(dataset.getRowNames()), casePanel, controlPanel);

        this.predModel = model;
        this.dataset = dataset;
        this.moduleName = moduleName;
        password = null;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPassword(){
    	return password;
    }

    public PredictionModel getPredictionModel(){
    	return predModel;
    }

    protected File createTestGCTFile(String fileName, final List<float[]> trainingSet)
    {
        return createTestGCTFile(fileName, trainingSet, null);
    }

    protected File createTestGCTFile(String fileName, final List<float[]> trainingSet, final List<String> arrayNames)
    {
        File gctTestFile = null;

        Dataset testData = new AbstractDataset()
        {
            public double getValue(int row, int column)
            {
                 return ((float[])trainingSet.get(column))[row];
            }

            public String getRowName(int row)
            {
                return dataset.getRowNames()[row];
            }

            public int getRowCount()
            {
                return dataset.getRowCount();
            }

            public int getColumnCount()
            {
                return trainingSet.size();
            }

            public String getColumnName(int column)
            {
                if(arrayNames != null && arrayNames.get(column) != null)
                {
                    return (String) arrayNames.get(column);
                }

                return ("Column " + column);
            }

        };

        try
        {
            gctTestFile = new File(fileName + ".gct") ;
            gctTestFile.deleteOnExit();
            GctWriter writer = new GctWriter();
            OutputStream os = new BufferedOutputStream(new FileOutputStream(gctTestFile));
            writer.write(testData, os);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return gctTestFile;
    }

    protected File createTestCLSFile(String fileName, int numArrays)
    {
        return createTestCLSFile(fileName, numArrays, null);
    }

    protected File createTestCLSFile(String fileName, int numArrays, String[] classLabels)
    {
        File testClsData = null;
        BufferedOutputStream clsOutputStream = null;

        try
        {
            testClsData = new File(fileName + ".cls");
            testClsData.deleteOnExit();
            clsOutputStream = new BufferedOutputStream(new FileOutputStream(testClsData));

            if(classLabels == null || classLabels.length < numArrays)
            {
                classLabels = new String[numArrays];
                Arrays.fill(classLabels, 0, numArrays/2, "Control");
                Arrays.fill(classLabels, numArrays/2, numArrays, "Case");
            }

            ClassVector classVector = new DefaultClassVector(classLabels);

            ClsWriter writer = new ClsWriter();
            writer.write(classVector, clsOutputStream);
        }
        catch(Exception e)
        {   e.printStackTrace(); }
        finally
        {
            try
            {
                if (clsOutputStream != null)
                {   clsOutputStream.close(); }
            }
            catch (IOException e)
            {   e.printStackTrace();    }
        }

        return testClsData;
    }

    public PredictionResult runPredictor(String classifierName, Parameter[] parameters)
    {
        PredictionResult predResult = null;
        try
        {
            String serverName = GPpropertiesManager.getProperty("gp.server");
            String userName = GPpropertiesManager.getProperty("gp.user.name");
            GPClient server = new GPClient(serverName, userName, password);

            JobResult analysisResult = server.runAnalysis(classifierName, parameters);

            System.out.println("Error occurred: " + analysisResult.hasStandardError());

            String[] outputFiles = analysisResult.getOutputFileNames();

            String predFileName = null;
            for(int i = 0; i < outputFiles.length; i++)
            {
                if(outputFiles[i].indexOf(".odf") != -1)
                    predFileName = outputFiles[i];
            }

            if(predFileName == null)
                throw new ClassifierException("Error: Classifier prediction model could not be generated");

            AnalysisWebServiceProxy analysisProxy = new AnalysisWebServiceProxy(serverName, userName, password);
            String[] resultFiles = new String[1] ;
            resultFiles[0] = predFileName;

            File[] result = analysisProxy.getResultFiles(analysisResult.getJobNumber(), resultFiles, new File( FilePathnameUtils.getTemporaryFilesDirectoryPath() ), true);
            if(result == null || result.length == 0)
                throw new ClassifierException("Error: Could not retrieve classifier model from GenePattern");

            File predFile = result[0];
            predFile.deleteOnExit();

            predResult = new PredictionResult(predFile);
            // remove job from GenePattern server
            analysisProxy = new AnalysisWebServiceProxy(server.getServer(), server.getUsername(), password);
            analysisProxy.purgeJob(analysisResult.getJobNumber());
        }
        catch(ClassifierException ce)
        {
            JOptionPane.showMessageDialog(null, ce.getMessage());
        }
        catch(Exception e)
        {
            if(e.getMessage().indexOf("Unknown user or invalid password") != -1)
                JOptionPane.showMessageDialog(null, "Could not connect to GenePattern: " + e.getMessage());

            e.printStackTrace();
        }

        return predResult;
    }

    protected String getPredictedClass(PredictionResult result)
    {
        int predClassIndx = result.getColumn("Predicted Class");

        return result.getValueAt(0, predClassIndx);
    }

    public GPDataset getGPDataset(){
    	return dataset;
    }
}
