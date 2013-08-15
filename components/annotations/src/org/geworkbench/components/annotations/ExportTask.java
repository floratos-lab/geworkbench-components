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

public class ExportTask extends ProgressTask<GeneBase[], Void>{
    	private File savefile = null;
    	private String pathway = null;
    	private AnnotationsPanel2 ap = null;

    	public ExportTask(int pbtype, String message, AnnotationsPanel2 ap2, File sf, String pw){
    		super(pbtype, message);
    		ap = ap2;
    		savefile = sf;
    		pathway = pw;
    	}

    	@Override
    	protected GeneBase[] doInBackground(){
    		if (isCancelled()) return null;
    		BioDBnetClient client = new BioDBnetClient();
    		return client.queryGenesForPathway(pathway);
    	}

    	@Override
    	protected void done(){
    		ap.pd.removeTask(this);
    		if (isCancelled()) return;
    		GeneBase[] genesInPathway = null;
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
    	
        private void saveGenesInPathway(File selectedFile, GeneBase[] genesInPathway) throws IOException {
        	String filename = selectedFile.getAbsolutePath();
            if(!filename.endsWith("csv")){
           		filename += ".csv";
           	}
            FileWriter writer = new FileWriter(filename);
            for (int i = 0; i < genesInPathway.length; i++) {
            	GeneBase gene = genesInPathway[i];
                writer.write(gene.getGeneSymbol() + ", " + gene.getGeneName() + "\n");
            }
            writer.close();
        }
}