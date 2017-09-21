package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Josh
 */
public class ResultWriter implements Runnable {

    private static File resultsFile = null;
    private List<Alignment> list;
    private static boolean setUp = false;
    private static BufferedWriter writer;

    public ResultWriter(List<Alignment> l) {
        if (!setUp) {
            throw new Error("Writer not set up");
        }
        this.list = l;
    }

    @Override
    public void run() {
        for (Alignment alignment : list) {
            write(alignment);
        }
    }

    private void write(Alignment alignment) {
   
        
        
    }
    
    public static void initialise(String fName) {
        try {
            resultsFile = new File(fName);
            writer = new BufferedWriter(new FileWriter(resultsFile));
            setUp = true;
        } catch (IOException ex) {
            Logger.getLogger(ResultWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void reset() {
        close();
    }

    public static void close() {
        try {
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(ResultWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
