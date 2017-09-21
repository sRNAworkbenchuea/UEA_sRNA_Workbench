/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils.aligners;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.FileUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author w0445959
 */
public class BowtieRunner extends RunnableTool
{

    private final static String TOOL_NAME = "BOWTIE_SEQ_ALIGN";
    private final Path input_file;
    private final Path database_file;
    private final Path output_file;
    private boolean completed;
    private final Path temp_dir;
    private int mismatch = 0;

    public BowtieRunner(Path input, Path database, Path output, Path temp_dir, int max_alignment)
    {
        this(input, database, output, temp_dir, max_alignment, null);
    }

    public BowtieRunner(Path input, Path database, Path output, Path temp_dir,int max_alignment, JFXStatusTracker statusTracker)
    {
        super(TOOL_NAME, statusTracker);

        this.input_file = input;
        this.database_file = database;
        this.output_file = output;
        this.temp_dir = temp_dir;
    }

    @Override
    protected void process() throws Exception
    {
        Tools.trackEvent("Internal Procedure Run", "Patman Sequence Alignment");
        completed = false;
        File short_read_file = this.input_file.toFile();
        File database = this.database_file.toFile();
        File bowtie_output_file = this.output_file.toFile();


        // Bowtie may require unix style line endings in it's FASTA files, not sure, might as well convert just in case.
        // This adds extra work but
        boolean isUnixFile = FileUtils.hasUnixLineEndings(short_read_file);

        File unixStyle = isUnixFile
                ? short_read_file
                : new File(temp_dir.toString() + DIR_SEPARATOR + short_read_file.getName() + "_unix.fa");

        if (!isUnixFile)
        {
            FileUtils.replaceLineEndings(short_read_file, unixStyle, "\n");
            LOGGER.log(Level.WARNING, "Had to convert {0} to unix style for BOWTIE.", short_read_file.getName());
        }

        DatabaseWorkflowModule.getInstance().printLap("BOWTIE: Optional unix conversions");
        
        trackerInitUnknownRuntime("Executing BOWTIE.");
        
        //Files.deleteIfExists(bowtie_output_file.toPath());
        
        String args = buildArguments(unixStyle, database, bowtie_output_file);

        AppUtils.INSTANCE.getBinaryExecutor().execBowtie(args);

        /*
        int countLines = FileUtils.countLines(patman_output_file);
        this.trackerInitKnownRuntime("Filtering out positive strand matches", countLines);

        if (params.getNegativeStrandOnly())
        {
            removeLineFromFile(patman_output_file, "\t+\t");
        }
        trackerReset();

        DatabaseWorkflowModule.getInstance().printLap("Patman: mapping fasta files");

        // TODO might want to check if the output file contains some content here too.
        if (!patman_output_file.exists())
        {
            throw new IOException("PATMAN ERROR: Patman result file was not created: " + output_file);
        }

        if (params.getPostProcess())
        {
            trackerInitUnknownRuntime("Post-processing patman file.");

            postProcess(patman_output_file);

            trackerReset();
        }
        */
        DatabaseWorkflowModule.getInstance().printLap("Bowtie: alignment processing");

        completed = true;
    }

    private String buildArguments(File in, File database, File out)
    {
        String quote = Tools.isWindows() ? "\"" : "";
        //String args = quote + "-f -v 0 " + database.getAbsolutePath()+ " " + in.getAbsolutePath() + " > " + out.getAbsolutePath() + quote;
        //String args = quote + "-f -v 0 " + database.getAbsolutePath()+ " " + in.getAbsolutePath() + quote;
        String args =  "-f -v " + mismatch  + " -k " + DatabaseWorkflowModule.getInstance().getAlignmentFilterValue() + " -M " + DatabaseWorkflowModule.getInstance().getAlignmentFilterValue() + " --best " + database.getAbsolutePath()+ " " + in.getAbsolutePath() + "\t" + out.getAbsolutePath();
        
        return args;
    }
    
    public void setGapsMismatch(int mismatch)
    {
        this.mismatch = mismatch;
    }
    public static void main(String[] args)
    {

            Thread myThread = new Thread(new BowtieRunner(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Human/Mesenchymal_tumour_GTTTCG_L002_R1_AR.fa"),
                    Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Human/GRCh38_no_alt/human_set"),
                    Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Human/align_workbench"), Tools.getNextDirectory().toPath(), 100));
            
            myThread.start();
            try
            {
                myThread.join();
            }
            catch (InterruptedException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            



    }

}
