package org.geworkbench.components.annotations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.geworkbench.util.ProgressTask;

/**
 * ExportTask: export pathway genes to csv
 * $Id$
 */

public class ExportTask extends ProgressTask<GeneAnnotation[], Void>{
    	private File savefile = null;
    	private Pathway pathway = null;
    	private AnnotationsPanel2 ap = null;

    	public ExportTask(int pbtype, String message, AnnotationsPanel2 ap2, File sf, Pathway pw){
    		super(pbtype, message);
    		ap = ap2;
    		savefile = sf;
    		pathway = pw;
    	}

    	@Override
    	protected GeneAnnotation[] doInBackground(){
    		if (isCancelled()) return null;
    		GeneAnnotation[] genesInPathway = ap.criteria.getGenesInPathway(pathway);
    		return genesInPathway;
    	}

    	@Override
    	protected void done(){
    		ap.pd.removeTask(this);
    		if (isCancelled()) return;
    		GeneAnnotation[] genesInPathway = null;
    		try{
    			genesInPathway = get();
    		}catch(ExecutionException e){
    			e.printStackTrace();
    		}catch(InterruptedException e){
    			e.printStackTrace();
    		}
    		if ( genesInPathway == null )
            	return;
    		try{
    			saveGenesInPathway(savefile, genesInPathway);
    		}catch(IOException e){
    			e.printStackTrace();
    		}
    	}
    	
        private void saveGenesInPathway(File selectedFile, GeneAnnotation[] genesInPathway) throws IOException {
        	String filename = selectedFile.getAbsolutePath();
            if(!filename.endsWith("csv")){
           		filename += ".csv";
           	}
            FileWriter writer = new FileWriter(filename);
            for (int i = 0; i < genesInPathway.length; i++) {
                GeneAnnotation geneAnnotation = genesInPathway[i];
                writer.write(geneAnnotation.getGeneSymbol() + ", " + geneAnnotation.getGeneName() + "\n");
            }
            writer.close();
        }
}