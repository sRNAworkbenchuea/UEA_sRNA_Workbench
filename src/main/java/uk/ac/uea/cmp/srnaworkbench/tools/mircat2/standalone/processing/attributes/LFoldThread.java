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
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model.LFoldInfo;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;


/**
 *
 * @author keu13sgu
 */
public class LFoldThread implements Callable<ArrayList<LFoldInfo>> {
    
    private InputStream in;
    private OutputStream out;
    private String toFold;
    private int len;
    private boolean isNegative;
    private int beg;

    public LFoldThread(Process p, String toFold, int length, boolean isNegative, int beg) {
        this.in = p.getInputStream();
        this.out = p.getOutputStream();
        this.toFold = toFold;
        len = length;
        this.isNegative = isNegative;
        this.beg = beg;
    }

    @Override
    public ArrayList<LFoldInfo> call() throws Exception {
        try {
            writeToProcess();

            ArrayList<LFoldInfo> folds = readFromProcess();
            return folds;

        } catch (IOException ex) {
            Logger.getLogger(AttributesExtracter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void writeToProcess() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.out));
        out.write(toFold);
        out.newLine();

        out.write("@");
        out.newLine();

        out.flush();
        out.close();

    }

    private ArrayList<LFoldInfo> readFromProcess() throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(this.in));
        ArrayList<LFoldInfo> folds = new ArrayList<>();

        String line = null;
        while ((line = in.readLine()) != null) {

            String parts[] = line.split(" ");
            
            if(parts.length <= 2)
                continue;
            
            String fold = parts[0];
            double mfe = 0;
            int pos = 0;
            
            int count = 0;
            
            for(int i = 1; i < parts.length; i++){
                if(!parts[i].isEmpty() && !parts[i].equals("(")){
                    if(count == 0){
                        int b = 0, e = parts[i].length() - 1;
                        if(parts[i].charAt(0) == '('){
                            b = 1;
                        }
                        mfe = Double.parseDouble(parts[i].substring(b, e));
                        count = 1;
                    }
                    else{
                        pos = Integer.parseInt(parts[i]) - 1;
                    }   
                }
            }   
            if(!this.isNegative){
                folds.add(new LFoldInfo(pos, fold, mfe));
            }else{
                int flipedBeg = this.len - (pos + fold.length());
                folds.add(new LFoldInfo(flipedBeg, reverse(fold), mfe));
            }
        }

        in.close();
        return folds;
    }
    
     private String reverse( String sequence )
  {
        String s =  new StringBuilder( sequence ).reverse().toString();
        String rez = "";
        for(char c: s.toCharArray()){

            switch(c){
               case '(': rez += ')'; break;
               case ')': rez += '('; break;
               default : rez += '.';
        }
        }
        return rez;
        
  }

}

