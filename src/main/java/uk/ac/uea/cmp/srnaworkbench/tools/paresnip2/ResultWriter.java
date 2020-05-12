/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rew13hpu
 */
public class ResultWriter implements Runnable {

    private static BufferedWriter writer;
    private static boolean isSetUp;
    private List<AlignmentPath> list;

    public static void init(String file) {
        if (!isSetUp) {
            try {
                writer = new BufferedWriter(new FileWriter(file));
            } catch (IOException ex) {
                Logger.getLogger(ResultWriter.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                isSetUp = true;
            }

        }
    }

    public ResultWriter(List<AlignmentPath> l) {
        list = l;
    }

    private static synchronized void writeMethod(List<AlignmentPath> l) {
        try {
            for (AlignmentPath ap : l) {
            writer.write(ap.toString());
            }
        } catch (IOException ex) {
            Logger.getLogger(ResultWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {

        writeMethod(list);

    }

}
