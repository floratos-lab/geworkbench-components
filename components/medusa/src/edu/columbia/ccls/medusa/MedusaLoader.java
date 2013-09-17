package edu.columbia.ccls.medusa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cern.colt.bitvector.QuickBitMatrix;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenDoubleIntHashMap;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.Matrix2;
import cern.jet.math.Functions;
import edu.columbia.ccls.medusa.agglomeration.Agglomerator;
import edu.columbia.ccls.medusa.boosting.Rule;
import edu.columbia.ccls.medusa.io.MedusaReader;
import edu.columbia.ccls.medusa.io.MedusaWriter;
import edu.columbia.ccls.medusa.io.RuleParser;
import edu.columbia.ccls.medusa.sequence.FeatureReader;
import edu.columbia.ccls.medusa.sequence.FileFeatureReader;
import edu.columbia.ccls.medusa.sequence.IterativeMotifBuilder;
import edu.columbia.ccls.medusa.sequence.PositionalMotifBuilder;
import edu.columbia.ccls.medusa.sequence.WindowMotifBuilder;
import edu.columbia.ccls.medusa.sequence.pssm.PssmFunctions;
import edu.columbia.ccls.medusa.sequence.pssm.ScorePssm;
import edu.columbia.ccls.utilities.ArrayUtils;

/* this class is decompiled from Perseus.jar
	to intentionally shadow the original one that has a bug. see line 54-55.
 */
public class MedusaLoader
{
  protected static final Log log;

  protected static HashMap<String, String> hshArgs;
  protected static final String slash;
  protected static DoubleFactory2D matrixFactory;

  public static void main(String[] paramArrayOfString)
    throws Exception
  {
    long l1 = System.currentTimeMillis();
    // not System.getProperty("java.vm.version"));
    int i = Integer.parseInt(System.getProperty("java.version").substring(2, 3));
    if (i < 5)
    {
      System.err.println("ERROR: MEDUSA requires Java Virtual Machine version 1.5.0 or greater.");
      System.exit(-1);
    }
    if (paramArrayOfString.length == 0)
    {
      System.err.println(getHelpMessage());
    }
    else
    {
      hshArgs = new HashMap<String, String>();
      for (int j = 0; j < paramArrayOfString.length; ++j)
      {
        String[] localObject = paramArrayOfString[j].split("=");
        if (localObject.length != 2)
          throw new IllegalArgumentException("ERROR: Invalid parameter: " + paramArrayOfString[j]);
        hshArgs.put(localObject[0].trim(), localObject[1].trim());
      }
      MedusaConfiguration localMedusaConfiguration = new MedusaConfiguration(hshArgs);
      initializeOutputDirs(localMedusaConfiguration);
      Object localObject = new Medusa();
      initialize((Medusa)localObject, localMedusaConfiguration);
      System.gc();
      if (localMedusaConfiguration.resumeRun())
        throw new RuntimeException("Resumed runs don't work just yet anymore");
      if (localMedusaConfiguration.saveFeatures())
      {
        MedusaWriter.saveFeatures((Medusa)localObject, localMedusaConfiguration.getFeaturesPath());
        moveData((Medusa)localObject, localMedusaConfiguration);
      }
      initHoldout((Medusa)localObject);
      ((Medusa)localObject).initRootScore();
      log.info("Root score is: " + ((Medusa)localObject).getRootScore());
      log.info("Beginning MEDUSA run of " + localMedusaConfiguration.getMaxIter() + " iterations.");
      ArrayList<Rule> localArrayList = ((Medusa)localObject).findRules();
      long l2 = System.currentTimeMillis() - l1;
      if (localMedusaConfiguration.isVerbose())
        System.out.println("Total execution time " + l2 + " ms, " + (l2 / 1000L) + " seconds.");
      MedusaWriter localMedusaWriter = new MedusaWriter(((Medusa)localObject).getBgroundProbability());
      localMedusaWriter.writeRules(localArrayList, ((Medusa)localObject).getRootScore(), localMedusaConfiguration.getDirExperiment() + slash + localMedusaConfiguration.getRunName() + slash + localMedusaConfiguration.getFileRules());
      localMedusaWriter.saveState((Medusa)localObject, localMedusaConfiguration.getOutputPath() + slash + "serial");
      if (localMedusaConfiguration.getMatlabDir() != null)
      {
        if (localMedusaConfiguration.isVerbose())
          System.out.println("Writing Matlab version of files.  This may take a long time if the files are large...");
        localMedusaWriter.writeRulesForMatlab(localArrayList, localMedusaConfiguration.getTargets_names(), ((Medusa)localObject).getRootScore(), new Matrix2(((Medusa)localObject).getFeatureList()), ((Medusa)localObject).getMotifs(), localMedusaConfiguration.getCexp(), localMedusaConfiguration.getPexp(), localMedusaConfiguration.getMatlabDir());
        if (localMedusaConfiguration.isVerbose())
          System.out.println("Done writing files to Matlab file directory.");
      }
      serializeRunInfo((Medusa)localObject, localMedusaConfiguration);
    }
  }

  public static void moveData(Medusa paramMedusa, MedusaConfiguration paramMedusaConfiguration)
    throws IOException
  {
    MedusaReader localMedusaReader = new MedusaReader();
    int i = 0;
    for (String str1 : paramMedusaConfiguration.getFileFasta())
    {
      String str2 = paramMedusaConfiguration.getDataPath() + File.separator + "fasta." + i + ".clean.txt";
      String str3 = paramMedusaConfiguration.getDataPath() + File.separator + "genes." + i + ".txt";
      List<String> localList = localMedusaReader.createCleanFile(str1, str2, 0, null);
      FileWriter localFileWriter = new FileWriter(new File(str3));
      Iterator<String> localIterator = localList.iterator();
      while (localIterator.hasNext())
      {
        String str4 = (String)localIterator.next();
        localFileWriter.write(str4 + "\n");
      }
      localFileWriter.close();
      ++i;
    }
  }

  public static void serializeRunInfo(Medusa paramMedusa, MedusaConfiguration paramMedusaConfiguration)
    throws IOException
  {
    String str = paramMedusaConfiguration.getOutputPath() + File.separator + "info.txt";
    BufferedWriter localBufferedWriter = new BufferedWriter(new FileWriter(new File(str)));
    localBufferedWriter.write("root_score=" + paramMedusa.getRootScore() + "\n");
    Rule localRule = (Rule)paramMedusa.getRules().get(paramMedusa.getRules().size() - 1);
    localBufferedWriter.write("test_accuracy=" + (1.0D - localRule.getTestLossForIter()) + "\n");
    localBufferedWriter.write("train_accuracy=" + (1.0D - localRule.getTrainingLoss()) + "\n");
    localBufferedWriter.close();
  }

  public static void initialize(Medusa paramMedusa, MedusaConfiguration paramMedusaConfiguration)
  {
    paramMedusa.setConfiguration(paramMedusaConfiguration);
    initializeFeatures(paramMedusa);
    paramMedusa.setRegulatorsNames(paramMedusaConfiguration.getRegulatorNames());
    String str = null;
    for (int i = 0; i < paramMedusaConfiguration.getMotifBuilderTypes().length; ++i)
    {
      if (paramMedusaConfiguration.getMotifBuilderTypes()[i] == 3)
        continue;
      str = paramMedusaConfiguration.getFileFasta()[i];
      break;
    }
    if (str != null)
      paramMedusa.setPssmScorer(new ScorePssm(str + ".clean.N", paramMedusaConfiguration.isRCSameAsOriginal()));
    paramMedusa.getPssmScorer().setPbg(paramMedusa.getBgroundProbability());
    paramMedusa.setRegulatorsUp(paramMedusaConfiguration.getRUp());
    paramMedusa.setRegulatorsDown(paramMedusaConfiguration.getRDown());
    paramMedusa.setCexp(paramMedusaConfiguration.getCexp());
    paramMedusa.setMaxPssmLength(paramMedusaConfiguration.getMaxPssmLength());
    PssmFunctions localPssmFunctions = new PssmFunctions(paramMedusa.getBgroundProbability(), paramMedusaConfiguration.getMaxPssmLength(), paramMedusa.getPssmScorer());
    paramMedusa.setPssmFunctions(localPssmFunctions);
    paramMedusa.setAgglomerator(new Agglomerator(localPssmFunctions, paramMedusa.getFeatureList()));
    paramMedusa.setRuleParser(new RuleParser(paramMedusaConfiguration.getRulesPath()));
  }

  public static void initializeOutputDirs(MedusaConfiguration paramMedusaConfiguration)
    throws IOException
  {
    File localFile1 = new File(paramMedusaConfiguration.getOutputPath());
    Object localObject1;
    Object localObject2;

    File localFile2;
    if (!(paramMedusaConfiguration.resumeRun()))
    {
      if (localFile1.isDirectory())
      {
        log.warn("The output directory already exists. Making a new one!");
        localObject1 = paramMedusaConfiguration.getRunName();
        localObject2 = new SimpleDateFormat("yyyy.MM.dd'@'hh.mm.ss");
        while (localFile1.canRead() == true)
        {
          Date localDate = new Date();
          String str1 = ((SimpleDateFormat)localObject2).format(localDate);
          paramMedusaConfiguration.setRunName(((String)localObject1) + "__" + str1);
          localFile1 = new File(paramMedusaConfiguration.getOutputPath());
        }
        log.warn("New output directory is: " + paramMedusaConfiguration.getOutputPath());
        log.warn("**** Please update your drive file to reflect the new path if you plan on running Gorgon!! ****");
      }
      String[] x = paramMedusaConfiguration.getAllOutputPaths();
      for (String s : x)
      {
        localFile2 = new File(s);
        boolean bool = localFile2.mkdir();
        if (bool)
          continue;
        log.error("There was a problem creating directory:" + localFile2.getAbsolutePath());
      }
    }
    else
    {
      String[] x = paramMedusaConfiguration.getAllOutputPaths();
      for (String s : x)
      {
        localFile2 = new File(s);
        if (localFile2.canWrite())
          continue;
        throw new RuntimeException("Can't read/write to the directory " + localFile2.getAbsolutePath() + " " + "check it out and try again.");
      }
    }
  }

  public static void initializeFeatures(Medusa paramMedusa)
  {
    MedusaConfiguration localMedusaConfiguration = paramMedusa.getConfiguration();
    log.info("Calculating feature matrix ...");
    Matrix2 localMatrix21 = null;
    QuickBitMatrix localQuickBitMatrix = null;
    Matrix2 localMatrix22 = new Matrix2(1, 4);
    Object localObject1;
    if (localMedusaConfiguration.getLoadFeatureFile() != null)
    {
      log.info("Loading stored features from" + localMedusaConfiguration.getLoadFeatureFile());
      localObject1 = new FileFeatureReader();
      ((FeatureReader)localObject1).loadFeatures(localMedusaConfiguration.getLoadFeatureFile());
      localMatrix21 = ((FeatureReader)localObject1).getFeatures();
      localQuickBitMatrix = ((FeatureReader)localObject1).getFeatureMatrix();
      localMatrix22.viewRow(0).assign(((FeatureReader)localObject1).getPbg());
    }
    else
    {
      String[] x = localMedusaConfiguration.getFileFasta();
      int[] arrayOfInt = localMedusaConfiguration.getMotifBuilderTypes();
      OpenDoubleIntHashMap localOpenDoubleIntHashMap = new OpenDoubleIntHashMap();
      for (int i = 0; i < x.length; ++i)
      {
    	  FeatureReader localObject2 = null;
        if (arrayOfInt[i] == 0)
          localObject2 = new IterativeMotifBuilder(localMedusaConfiguration, i, localOpenDoubleIntHashMap);
        else if (arrayOfInt[i] == 2)
          localObject2 = new WindowMotifBuilder(localMedusaConfiguration, i, localOpenDoubleIntHashMap, localMedusaConfiguration.getMinCount()[i], localMedusaConfiguration.getMinWindowSize()[i]);
        else if (arrayOfInt[i] == 1)
          localObject2 = new PositionalMotifBuilder(localMedusaConfiguration, i, localOpenDoubleIntHashMap);
        else if (arrayOfInt[i] == 3)
          localObject2 = new FileFeatureReader(localMedusaConfiguration.getFileFasta()[i]);
        else
          throw new IllegalArgumentException("Unknown type of motif builder:" + arrayOfInt[i]);
        ((FeatureReader)localObject2).scanFeatures();
        if (localMatrix21 == null)
        {
          localMatrix21 = ((FeatureReader)localObject2).getFeatures();
          localQuickBitMatrix = ((FeatureReader)localObject2).getFeatureMatrix();
        }
        else
        {
          localMatrix21 = localMatrix21.append(((FeatureReader)localObject2).getFeatures(), 1);
          localQuickBitMatrix = localQuickBitMatrix.appendBelow(((FeatureReader)localObject2).getFeatureMatrix());
        }
        if (localMedusaConfiguration.getStoreFeature() != null)
        {
          if (localMedusaConfiguration.isVerbose())
            System.out.println("Saving stored features to " + localMedusaConfiguration.getStoreFeature());
          ((FeatureReader)localObject2).saveFeatures(localMedusaConfiguration.getStoreFeature());
        }
        if (arrayOfInt[i] == 3)
        {
          if (((FeatureReader)localObject2).getFeatureMatrix().columns() != localMedusaConfiguration.getTargets_names().length)
            throw new IllegalArgumentException("Feature matrix has incorrect number of genes; saw " + ((FeatureReader)localObject2).getFeatureMatrix().columns() + " expecting " + localMedusaConfiguration.getTargets_names().length);
        }
        else
        {
          localMatrix22.setQuick(0, 0, localMatrix22.getQuick(0, 0) + localObject2.getPbg()[0]);
          localMatrix22.setQuick(0, 1, localMatrix22.getQuick(0, 1) + localObject2.getPbg()[1]);
          localMatrix22.setQuick(0, 2, localMatrix22.getQuick(0, 2) + localObject2.getPbg()[2]);
          localMatrix22.setQuick(0, 3, localMatrix22.getQuick(0, 3) + localObject2.getPbg()[3]);
        }
        localObject2 = null;
        System.gc();
      }
      if (localMedusaConfiguration.isVerbose())
        log.info("Finished building feature matrix: " + localQuickBitMatrix.rows() + " motifs, " + "number of nonzero elements: " + localQuickBitMatrix.cardinality());
    }
    paramMedusa.setMotifs(localQuickBitMatrix);
    paramMedusa.setFeatureList(localMatrix21.toArray());
    double d = localMatrix22.zSum();
    if (d != 0.0D)
      localMatrix22.assign(Functions.div(d));
    paramMedusa.setBgroundProbability(localMatrix22);
  }

  public static void trimFeatures(Medusa paramMedusa)
  {
    Object localObject1 = paramMedusa.getMotifs();
    MedusaConfiguration localMedusaConfiguration = paramMedusa.getConfiguration();
    Object localObject2 = matrixFactory.make(paramMedusa.getFeatureList());
    int i = 0;
    if ((localMedusaConfiguration.getFewestTrim() <= 0.0D) && (localMedusaConfiguration.getMostTrim() <= 0.0D))
      return;
    int j = localMedusaConfiguration.getCexp().rows();
    Matrix2 localMatrix2 = ((QuickBitMatrix)localObject1).sumRows();
    IntArrayList localIntArrayList1 = new IntArrayList();
    IntArrayList localIntArrayList2 = new IntArrayList();
    DoubleArrayList localDoubleArrayList = new DoubleArrayList();

    HashMap<Double, Integer> localHashMap1 = new HashMap<Double, Integer>();
    HashMap<Double, Integer> localHashMap2 = new HashMap<Double, Integer>();
    if (localMedusaConfiguration.getFewestTrim() != 0.0D)
    {
      double d1 = Math.ceil(localMedusaConfiguration.getFewestTrim() / 100.0D * j);
      localMatrix2.find(localIntArrayList1, localIntArrayList2, localDoubleArrayList, Functions.less(d1));
    }
    if (localMedusaConfiguration.getMostTrim() != 0.0D)
    {
    	IntArrayList localIntArrayList3 = new IntArrayList();
      double d2 = Math.ceil(localMedusaConfiguration.getMostTrim() / 100.0D * j);
      localMatrix2.find(localIntArrayList3, localIntArrayList2, localDoubleArrayList, Functions.greater(d2));
      localIntArrayList1.addAllOf(localIntArrayList3);
    }
    localIntArrayList1.trimToSize();
    IntArrayList localIntArrayList3 = new IntArrayList();
    for (int k = 0; k < ((QuickBitMatrix)localObject1).rows(); ++k)
    {
      Double localDouble = Double.valueOf(((DoubleMatrix2D)localObject2).getQuick(k, 0));
      if (localIntArrayList1.indexOf(k) == -1)
      {
        localIntArrayList3.add(k);
        localHashMap2.put(localDouble, Integer.valueOf((int)localMatrix2.getQuick(k, 0)));
      }
      else
      {
        ++i;
        localHashMap1.put(localDouble, Integer.valueOf((int)localMatrix2.getQuick(k, 0)));
      }
    }
    localIntArrayList3.trimToSize();
    if (localIntArrayList3.size() == 0)
    {
      System.out.println("All features have been filtered out, reduce the fewest_trim and most_trim contsraints and run again");
      System.exit(1);
    }
    int[] arrayOfInt = new int[((QuickBitMatrix)localObject1).columns()];
    for (int l = 0; l < arrayOfInt.length; ++l)
      arrayOfInt[l] = l;
    QuickBitMatrix localQuickBitMatrix = ((QuickBitMatrix)localObject1).getRowsCols(localIntArrayList3.elements(), arrayOfInt);
    DoubleMatrix2D localDoubleMatrix2D = ((DoubleMatrix2D)localObject2).viewSelection(localIntArrayList3, null);
    localObject1 = localQuickBitMatrix;
    localObject2 = localDoubleMatrix2D;
    String str = localMedusaConfiguration.getDirExperiment() + System.getProperty("file.separator") + localMedusaConfiguration.getRunName();
    MedusaWriter.writeMotifHistogram(str, "motifs_filtered.txt", localHashMap1);
    MedusaWriter.writeMotifHistogram(str, "motifs_kept.txt", localHashMap2);
    if (i > 0)
      log.info("Trimmed " + i + " motifs from original feature matrix");
    paramMedusa.setFeatureList(((DoubleMatrix2D)localObject2).toArray());
    paramMedusa.setMotifs((QuickBitMatrix)localObject1);
  }

  public static void initHoldout(Medusa paramMedusa)
  {
    MedusaConfiguration localMedusaConfiguration = paramMedusa.getConfiguration();
    if (localMedusaConfiguration.getHeldoutType().equals("random"))
    {
      initRandomHoldout(paramMedusa, localMedusaConfiguration.getHeldoutPercent());
    }
    else if (localMedusaConfiguration.getHeldoutType().equals("genes"))
    {
      initGeneHoldout(paramMedusa, localMedusaConfiguration.getHeldoutGenes());
    }
    else if (localMedusaConfiguration.getHeldoutType().equals("experiments"))
    {
      initExperimentHoldout(paramMedusa, localMedusaConfiguration.getHeldoutExperiments());
    }
    else if (localMedusaConfiguration.getHeldoutType().equals("list"))
    {
      initExampleHoldout(paramMedusa, localMedusaConfiguration.getHeldoutGenes(), localMedusaConfiguration.getHeldoutExperiments());
    }
    else
    {
      assert (localMedusaConfiguration.getHeldoutType().equals("homatrix"));
      initMatrixHoldout(paramMedusa, localMedusaConfiguration.getHoldoutMatrix());
    }
  }

  public static void initRandomHoldout(Medusa paramMedusa, double paramDouble)
  {
    IntArrayList localIntArrayList1 = new IntArrayList();
    IntArrayList localIntArrayList2 = new IntArrayList();
    DoubleArrayList localDoubleArrayList = new DoubleArrayList();
    DoubleMatrix2D localDoubleMatrix2D1 = paramMedusa.getCexp();
    localDoubleMatrix2D1.getNonZeros(localIntArrayList1, localIntArrayList2, localDoubleArrayList);
    int i = (int)Math.ceil(paramDouble / 100.0D * localIntArrayList1.size());
    int[] arrayOfInt = ArrayUtils.shuffle(ArrayUtils.ripIntArray(localIntArrayList1.size()));
    DoubleMatrix2D localDoubleMatrix2D2 = localDoubleMatrix2D1.copy();
    for (int j = 0; j < i; ++j)
    {
      int k = localIntArrayList1.get(arrayOfInt[j]);
      int l = localIntArrayList2.get(arrayOfInt[j]);
      localDoubleMatrix2D2.setQuick(k, l, 0.0D);
    }
    paramMedusa.setTrainMatrix(localDoubleMatrix2D2);
  }

  public static void initGeneHoldout(Medusa paramMedusa, int[] paramArrayOfInt)
  {
    DoubleMatrix2D localDoubleMatrix2D1 = paramMedusa.getCexp();
    for (int i = 0; i < paramArrayOfInt.length; ++i)
    {
      if (paramArrayOfInt[i] < localDoubleMatrix2D1.rows())
        continue;
      throw new IndexOutOfBoundsException("You are specifying an out-of-range gene index");
    }
    DoubleMatrix2D localDoubleMatrix2D2 = localDoubleMatrix2D1.copy();
    localDoubleMatrix2D2.viewSelection(paramArrayOfInt, null).assign(0.0D);
    paramMedusa.setTrainMatrix(localDoubleMatrix2D2);
  }

  public static void initExperimentHoldout(Medusa paramMedusa, int[] paramArrayOfInt)
  {
    DoubleMatrix2D localDoubleMatrix2D1 = paramMedusa.getCexp();
    for (int i = 0; i < paramArrayOfInt.length; ++i)
    {
      if (paramArrayOfInt[i] < localDoubleMatrix2D1.columns())
        continue;
      throw new IndexOutOfBoundsException("Out of bounds experiment index specified for holdout");
    }
    DoubleMatrix2D localDoubleMatrix2D2 = localDoubleMatrix2D1.copy();
    localDoubleMatrix2D2.viewSelection(null, paramArrayOfInt).assign(0.0D);
    paramMedusa.setTrainMatrix(localDoubleMatrix2D2);
  }

  public static void initExampleHoldout(Medusa paramMedusa, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
  {
    if (paramArrayOfInt1.length != paramArrayOfInt2.length)
      throw new IllegalArgumentException("Length of holdout index arrays must be of equal length!");
    DoubleMatrix2D localDoubleMatrix2D1 = paramMedusa.getCexp();
    DoubleMatrix2D localDoubleMatrix2D2 = localDoubleMatrix2D1.copy();
    localDoubleMatrix2D2.assign(paramArrayOfInt1, paramArrayOfInt2, 0.0D);
    paramMedusa.setTrainMatrix(localDoubleMatrix2D2);
  }

  public static void initMatrixHoldout(Medusa paramMedusa, QuickBitMatrix paramQuickBitMatrix)
  {
    IntArrayList localIntArrayList1 = new IntArrayList();
    IntArrayList localIntArrayList2 = new IntArrayList();
    paramQuickBitMatrix.getNonZeros(localIntArrayList1, localIntArrayList2);
    initExampleHoldout(paramMedusa, localIntArrayList1.elements(), localIntArrayList2.elements());
  }

  public static String getHelpMessage()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("medusa - predictive modeling v. 1.0\n");
    localStringBuilder.append("Columbia Center for Computational Learning Systems 2006\n\n");
    localStringBuilder.append("The preferred way to use this software is to create a configuration file and \n");
    localStringBuilder.append("call this code with the -i flag to indiate the location of that file.  Values \n");
    localStringBuilder.append("in that file can be overridden by passing in a command-line flag for that value.\n\n");
    localStringBuilder.append("usage:\n");
    localStringBuilder.append("   java -server -jar Perseus.jar [options]\n\n");
    localStringBuilder.append("options:\n\n");
    localStringBuilder.append("  N means this value is an integer.\n");
    localStringBuilder.append("  F means this value is True/False, 'T' for true, 'F' for false\n");
    localStringBuilder.append("  L means this value is the name of a file\n");
    localStringBuilder.append("  D means this value is the name of a directory/folder\n");
    localStringBuilder.append("  C means a comma-delimited list of items, one for each sequence file\n");
    localStringBuilder.append("  S means this value is a word\n\n");
    localStringBuilder.append("  -i=L            Fully Qualified path to an XML file with MEDUSA parameters.\n");
    localStringBuilder.append("                  Commands in commandfile are overwritten by any passed parameters.\n");
    localStringBuilder.append("  -load=L         State file to load, for continuing a MEDUSA run.\n");
    localStringBuilder.append("  -iter=N         Number of boosting iterations. Default is 10.\n");
    localStringBuilder.append("  -maxkmer=C      List of integers, maximum size of simple motifs.\n");
    localStringBuilder.append("  -minkmer=C      List of integers, minimum size of simple motifs.\n");
    localStringBuilder.append("  -maxdimer=C     List of integers, maximum size of dimer.\n");
    localStringBuilder.append("  -mindimer=C     List of integers, minimum size of dimer.\n");
    localStringBuilder.append("  -maxpssm=N      Maximum size of agglomerated PSSMs.\n");
    localStringBuilder.append("  -pssms=F        Include pssm agglomeration.\n");
    localStringBuilder.append("  -dimers=F       Include dimers as motifs.\n");
    localStringBuilder.append("  -maxgap=N       Maximum gap between dimers.\n");
    localStringBuilder.append("  -clustersize=N  Number of motifs to cluster together in each boosting round.\n");
    localStringBuilder.append("  -windowsize=N   Minimum size of window for window builder.  See docs.\n");
    localStringBuilder.append("  -mincount=N     Minimum number of times we must see motif for window builder.  See docs.\n");
    localStringBuilder.append("  -alwayswrite=F  Continuously write out rule file.\n");
    localStringBuilder.append("  -corrected=T    Use corrected math in loss calculation.\n");
    localStringBuilder.append("  -direxpt=D      Root directory for output writing.\n");
    localStringBuilder.append("  -runname=D      Subdirectory under dirExpt where this run will be written.\n");
    localStringBuilder.append("                  All output files are written to the directory 'runname'\n");
    localStringBuilder.append("  -filerules=L    Fully qualified path to file for learned rules.\n");
    localStringBuilder.append("  -filelabels=L   Fully qualified path to label file\n");
    localStringBuilder.append("  -fasta=C        Fully qualified path to comma-delimited set of fasta files\n");
    localStringBuilder.append("  -mbtype=C       List of strings, indicate which Feature Builders to use for each fasta file\n");
    localStringBuilder.append("                  iterative=Iterative Motifs\n");
    localStringBuilder.append("                  positional=Positional Motifs\n");
    localStringBuilder.append("                  window=Window-limited Motifs\n");
    localStringBuilder.append("  -hotype=S       Whether to hold out 'experiments' or 'random'.\n");
    localStringBuilder.append("  -hoexpt=S       comma-delimited list of experiments to hold out.\n");
    localStringBuilder.append("                  Only relevant if hoType=experiments.\n");
    localStringBuilder.append("  -hogene=S       comma-delimited list of genes to hold out.\n");
    localStringBuilder.append("                  Only relevant if hoType=genes.\n");
    localStringBuilder.append("  -hopercent=N    0 and 100, percent of targets to hold out\n");
    localStringBuilder.append("                  Only relevant if hoType=random.\n");
    localStringBuilder.append("  -revcompsame=T  Treat reverse complement motifs as separate motifs in motif generation.\n");
    localStringBuilder.append("                  Default is 'T', so normally reverse complements are considered identical.\n");
    localStringBuilder.append("  -verbose=F      'F' to run silently, writing nothing to stdout.\n");
    localStringBuilder.append("                  Default is 'T', writing status information to stdout.\n");
    localStringBuilder.append("  -stumpsonly=F   Generate stumps-only tree vs. full alternating decision tree\n");
    localStringBuilder.append("  -matlab=D       [directory for matlab files] Directory in which to write files readable\n");
    localStringBuilder.append("                  by the Matlab version of Medusa.\n");
    localStringBuilder.append("  -fewesttrim=F   Ignore features that occur in F% or less in feature matrix\n");
    localStringBuilder.append("  -mosttrim=M     Ignore feature athat occur in M% or more in the feature matrix\n");
    localStringBuilder.append("  -reportagglom=T Write out file to keep track of kmers that were agglomerated after\n");
    localStringBuilder.append("                  each boosting iteration, if there were any. Defaults to false\n");
    return localStringBuilder.toString();
  }

  static
  {
    log = LogFactory.getLog(MedusaLoader.class);

    slash = File.separator;
    matrixFactory = DoubleFactory2D.dense;
  }
}

/* Location:           C:\eclipse_workspaces\geworkbench_full\medusa\lib\Perseus.jar
 * Qualified Name:     edu.columbia.ccls.medusa.MedusaLoader
 * Java Class Version: 5 (49.0)
 * JD-Core Version:    0.5.3
 */