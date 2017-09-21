/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.binaryexecutor;

/**
 *
 * @author w0445959
 */
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 * This class handles execution of External Binary programs without the need for
 * sockets
 *
 *
 * It provides an API for calling the platform dependent binaries.
 *
 * @author M.B Stocks
 */
public final class BinaryExecutor
{
    /* tab character */

    private static final String TAB = "\t";

    /* Default constructor */
    public BinaryExecutor()
    {


    }
//
//    /**
//     * Call RNAFold
//     *
//     * @param input
//     * @return
//     */
    public String execRNAFold(String input, String options)
    {
    //String message = BinaryManager.RNAFOLD.name() + TAB + input + ( options.isEmpty() ? "" : " " + options ) + LINE_SEPARATOR;

    //String result = sendMessage( message );
        String result = "";
        try
        {
            String[] data = new String[2];
            data[0] = input;
            data[1] = options;
    
            result = BinaryManager.RNAFOLD.run(data);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    //
//    /**
//     * Call RNALFold
//     *
//     * @param input
//     * @return
//     */
    public String execRNALFold(String input, String options)
    {

        String result = "";
        try
        {
            String[] data = new String[2];
            data[0] = options;
            data[1] = input;
    
            result = BinaryManager.RNALFOLD.run(data);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    /**
     * Call RNAeval
     *
     * @param input
     * @param options
     * @return
     */
    public String execRNAEval(String input, String options)
    {
        String result = "";
        try
        {
            String[] data = new String[2];
            data[0] = input;
            data[1] = options;
            result = BinaryManager.RNAEVAL.run(data);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Call patman
     *
     * @param input
     */
    public void execPatman(String input)
    {
        try
        {
            String[] data = new String[1];
            data[0] = input;
            BinaryManager.PATMAN.run(data);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Call BowTIE
     *
     * @param input
     */
    public void execBowtie(String input)
    {
        try
        {
            
            String[] data = input.split("\t");
            BinaryManager.BOWTIE.run(data);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public void execFastqDump(String input)
    {
        try
        {
            String[] data = new String[1];
            data[0] = input;
            BinaryManager.FASTQ_DUMP.run(data);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Call randfold
     *
     * @param input
     * @return
     */
    public String execRandFold(String input)
    {
        String result = "";

        try
        {
            String[] data = new String[1];
            data[0] = input;
            result = BinaryManager.RANDFOLD.run(data);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Call RNAPlot
     *
     * @param input
     * @return 
     */
    public String execRNAPlot(String input)
    {
        String result = "";

        try
        {
            result = BinaryManager.RNAPLOT.run(input.split("\t"));
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Call RNAPlot2
     *
     * @param input
     * @return 
     */
    public String execRNAPlot2(String id, String sequence, String structure, String format)
    {
       String result = "";
        try
        {
            String[] data = new String[4];
            data[0] = format;
            data[1] = ">" + id;
            data[2] = sequence;
            data[3] = structure;
            result = BinaryManager.RNAPLOT2.run(data);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    
    /**
     * Call GhostScript
     *
     * @param input
     */
    public String execGhostScript(String input)
    {
        String result = "";

        try
        {

            result = BinaryManager.GHOSTSCRIPT.run(input.split("\t"));
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Call RNACoFold
     *
     * @param data
     * @return
     */
    public String execRNACoFold(List<String> data)
    {
        String result = "";

        try
        {
            String[] dataArray = new String[data.size()];
            result = BinaryManager.GHOSTSCRIPT.run(data.toArray(dataArray));
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Call muscle
     *
     * @param args
     */
    public void execMuscle(String args)
    {
        try
        {
            String[] data = new String[1];
            data[0] = args;
            BinaryManager.MUSCLE.run(data);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }


}
