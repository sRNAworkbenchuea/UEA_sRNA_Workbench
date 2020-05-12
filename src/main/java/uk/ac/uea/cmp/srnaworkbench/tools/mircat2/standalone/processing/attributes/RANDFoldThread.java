/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.processing.attributes;

/**
 *
 * @author keu13sgu
 */
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.AttributesExtracter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author keu13sgu
 */
public class RANDFoldThread implements Callable<String>{
   
    private InputStream in;
    private OutputStream out;
    private final String x = "";

    public RANDFoldThread(Process p) {
        this.in = p.getInputStream();
        this.out = p.getOutputStream();
    }

    @Override
    public String call() throws Exception {
        try {
           // writeToProcess();
//            synchronized(x){
//                x.wait(1000000);
//            }
           // wait(10000);
            String folds = readFromProcess();
            return folds;

        } catch (IOException ex) {
            Logger.getLogger(AttributesExtracter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
     private void writeToProcess() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.out));

        out.newLine();
        out.write("@");
        out.newLine();
        out.flush();
        out.close();

    }

    private String readFromProcess() throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(this.in));
       String result = "";

        String line = null;
        while ((line = in.readLine()) != null) {

           // System.out.println(line);
            result += line;
        }

        in.close();
        return result;
    }



}

