/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils.patman;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;

import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.Filter;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.FileUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;

/**
 * A helpful wrapper class that manages calls to the patman tool (Prufer et al.
 * 2008. Patman: rapid alignment of short sequences to large databases.
 * Bioinformatics, 24(13), 1530-1531).
 *
 * @author Dan Mapleson
 */
public class PatmanRunner extends RunnableTool {

    private final static String TOOL_NAME = "SEQ ALIGN";
    private File input_file;
    private File database_file;
    private File output_file;
    private File temp_dir;
    private PatmanParams params;
    private BinaryExecutor binaryExecutor;
    private boolean completed = false;
    private boolean usingDatabase = true;

    /**
     * Creates a default PatmanRunner (probably shouldn't be used).
     */
    private PatmanRunner() {
        this(null, null, null, null, new PatmanParams());
    }

    /**
     * Creates a new PatmanRunner with all the details necessary to start the
     * binary. This constructor variant is intended for use by tools running at
     * the command line or when GUI progress statistics are not required.
     *
     * @param input The short read file
     * @param database The long read file
     * @param output The file to which matches will be written
     * @param temp_dir The directly to which any temporary files will be
     * written.
     * @param params The parameters describing how patman should execute
     */
    public PatmanRunner(File input, File database, File output, File temp_dir, PatmanParams params) {
        this(input, database, output, temp_dir, params, null);
    }

    /**
     * Creates a new PatmanRunner with all the details necessary to start the
     * binary. This constructor variant is intended for use by tools running on
     * the GUI where progress statistics can be tracked using a
     * {@link StatusTracker} object containing progress bar and a status label.
     *
     * @param input The short read file
     * @param database The long read file
     * @param output The file to which matches will be written
     * @param temp_dir The directly to which any temporary files will be
     * written.
     * @param params The parameters describing how patman should execute
     */
    public PatmanRunner(File input, File database, File output, File temp_dir, PatmanParams params, StatusTracker tracker) {
        super(TOOL_NAME, tracker);

        this.input_file = input;
        this.database_file = database;
        this.output_file = output;
        this.temp_dir = temp_dir;
        this.params = params;

        binaryExecutor = AppUtils.INSTANCE.getBinaryExecutor();

    }

    public void setUsingDatabase(boolean b) {
        usingDatabase = b;
    }

    /**
     * Initiates patman.
     */
    @Override
    protected void process() throws Exception {
        Tools.trackEvent("Internal Procedure Run", "Patman Sequence Alignment");
        completed = false;
        File short_read_file = this.input_file;
        File database = this.database_file;
        File patman_output_file = this.output_file;

        if (params.getPreProcess()) {
            short_read_file = preProcess();
        } else if (params.getMakeNR()) {
            short_read_file = makeNonRedundant();
        }
        if (usingDatabase) {
            DatabaseWorkflowModule.getInstance().printLap("Patman: preprocessing file");
        }

        if (params.getPostProcess()) {
            patman_output_file = new File(temp_dir.getPath() + DIR_SEPARATOR + this.output_file.getName() + "_temp.patman");
        }

        // Patman requires unix style line endings in it's FASTA files.
        // This adds extra work but
        boolean isUnixFile = FileUtils.hasUnixLineEndings(short_read_file);

        File unixStyle = isUnixFile
                ? short_read_file
                : new File(temp_dir.getPath() + DIR_SEPARATOR + short_read_file.getName() + "_unix.fa");

        if (!isUnixFile) {
            FileUtils.replaceLineEndings(short_read_file, unixStyle, "\n");
            LOGGER.log(Level.WARNING, "Had to convert {0} to unix style for PatMan.", short_read_file.getName());
        }

        // Patman requires unix style line endings in it's FASTA files.
        // This is for the genome
        boolean isUnixLongFile = FileUtils.hasUnixLineEndings(database);

        File unixLongStyle = isUnixLongFile
                ? database
                : new File(temp_dir.getPath() + DIR_SEPARATOR + database.getName() + "_unix.fa");

        if (!isUnixLongFile) {
            FileUtils.replaceLineEndings(database, unixLongStyle, "\n");
            LOGGER.log(Level.WARNING, "Had to convert {0} to unix style for PatMan.", database.getName());
        }
        if (usingDatabase) {
            DatabaseWorkflowModule.getInstance().printLap("Patman: Optional unix conversions");
        }
        if (fileExceedMax(unixStyle, params.getChunkSize())) {
            //System.out.println("file exceeds maximum chunk size of " + params.getChunkSize() + ". It will be mapped in chunks");
            List<File> chunks = FileUtils.splitupFile(unixStyle, temp_dir, params.getChunkSize());
            ArrayList<File> pat_chunks = new ArrayList<File>();

            trackerInitKnownRuntime("Patman: Processing chunks", chunks.size());

            // Call patman for each chunk
            for (File f : chunks) {
                // create the output chunk name by adding .patout to the input chunk name
                File pat_chunk = new File(f.getPath() + ".patman");
                binaryExecutor.execPatman(buildArguments(f, unixLongStyle, pat_chunk));
//                    int patr = run_patman(buildArguments(f, pat_chunk));
//                    if (patr != 0)
//                        throw new IOException("PATMAN ERROR: Code returned: " + patr);
                pat_chunks.add(pat_chunk);
                trackerIncrement();
            }

            // Combine files
            FileUtils.concatFiles(pat_chunks, patman_output_file);

            // Delete the temporary files
            for (int i = 0; i < chunks.size(); i++) {
                chunks.get(i).delete();
                pat_chunks.get(i).delete();
            }

            trackerReset();
        } else {
            trackerInitUnknownRuntime("Executing patman.");

            binaryExecutor.execPatman(buildArguments(unixStyle, unixLongStyle, patman_output_file));
//                int patr = run_patman(buildArguments(unixStyle, output_file));
//                if (patr != 0)
//                    throw new IOException("PATMAN ERROR: Code returned: " + patr);
            int countLines = FileUtils.countLines(patman_output_file);
            this.trackerInitKnownRuntime("Filtering out positive strand matches", countLines);

            if (params.getNegativeStrandOnly()) {
                removeLineFromFile(patman_output_file, "\t+\t");
            }
            trackerReset();
        }
        if (usingDatabase) {
            DatabaseWorkflowModule.getInstance().printLap("Patman: mapping fasta files");
        }

        // TODO might want to check if the output file contains some content here too.
        if (!patman_output_file.exists()) {
            throw new IOException("PATMAN ERROR: Patman result file was not created: " + output_file);
        }

        if (params.getPostProcess()) {
            trackerInitUnknownRuntime("Post-processing patman file.");

            postProcess(patman_output_file);

            trackerReset();
        }
        if (usingDatabase) {
            DatabaseWorkflowModule.getInstance().printLap("Patman: Postprocessing");
        }

        completed = true;
    }

    public void removeLineFromFile(File patman_file, String lineToRemove) {

        File tempFile = new File(patman_file.getAbsolutePath() + ".tmp");
        BufferedReader br = null;
        PrintWriter pw = null;
        try {

            br = new BufferedReader(new FileReader(patman_file));
            pw = new PrintWriter(new FileWriter(tempFile));

            if (!patman_file.isFile()) {
                throw new IOException("PATMAN ERROR: Patman file is not a file: " + patman_file);
            }

            //Construct the new file that will later be renamed to the original filename.
            String line = null;

            //Read from the original file and write to the new
            //unless content matches data to be removed.
            while ((line = br.readLine()) != null) {

                if (!line.trim().contains(lineToRemove)) {

                    pw.println(line);
                    pw.flush();
                }
            }

            //Delete the original file
            if (!patman_file.delete()) {
                throw new IOException("PATMAN ERROR: Could not delete original File: " + patman_file);

            }

            //Rename the new file to the filename the original file had.
            if (!tempFile.renameTo(patman_file)) {
                throw new IOException("PATMAN ERROR: Could not rename new File: " + patman_file);
            }

        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                pw.close();
                br.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

//    // Only for use to get around exe_manager issues
//    private int run_patman(String args) throws Exception
//    {
//        // Do matching using patman (assumes patman is on the path)
//        Process p = Runtime.getRuntime().exec("patman " + args);
//
//        ProcessStreamManager pm = new ProcessStreamManager(p, "PATMAN");
//        return pm.runInForeground(true, false);
//    }
    private boolean fileExceedMax(File file, int lines) throws IOException {
        return FileUtils.countLines(file) > lines;
    }

    private String buildArguments(File in, File database, File out) {
        String quote = Tools.isWindows() ? "\"" : "";
        String ss_str = params.getPositiveStrandOnly() ? "--singlestrand " : "";
        String args = " --gaps=" + params.getMaxGaps() + " " + ss_str + " --edits=" + params.getMaxMismatches()
                + " --databases " + quote + database.getPath() + quote
                + " --patterns " + quote + in.getPath() + quote
                + " --output=" + quote + out.getPath() + quote;

        return args;
    }

    private File preProcess() throws Exception {
        FilterParams filt_params = new FilterParams.Builder().setMinLength(params.getMinSRNALength()).setMaxLength(params.getMaxSRNALength()).setMinAbundance(params.getMinSRNAAbundance()).setFilterLowComplexitySeq(true).setFilterInvalidSeq(true).setOutputNonRedundant(true).build();

        File pre_process_file = new File(this.temp_dir.getPath() + DIR_SEPARATOR + this.input_file.getName() + "_filtered.fa");

        Filter filter = new Filter(this.input_file, pre_process_file, temp_dir, filt_params, this.getTracker());
        filter.run();

        if (filter.failed()) {
            throw new Exception(filter.getErrorMessage());
        }

        return pre_process_file;
    }

    private void postProcess(File patman_file) throws Exception {
        // Load patman file, filter it and write out again
        Patman p = new PatmanReader(patman_file).process();

        p.stripWeighted((float) params.getMinWeightedAbundance());

        p.save(this.output_file);
    }

    private File makeNonRedundant() throws Exception {
        FilterParams filt_params = new FilterParams.Builder().setMinLength(FilterParams.Definition.MINIMUM_LENGTH.getLowerLimit(Integer.class)).setMaxLength(FilterParams.Definition.MAXIMUM_LENGTH.getUpperLimit(Integer.class)).setFilterLowComplexitySeq(false).setFilterInvalidSeq(true).setOutputNonRedundant(true).build();

        File pre_process_file = new File(this.temp_dir.getPath() + DIR_SEPARATOR + this.input_file.getName() + "_NR.fa");

        Filter filter = new Filter(this.input_file, pre_process_file, temp_dir, filt_params, this.getTracker());
        filter.run();

        if (filter.failed()) {
            throw new Exception(filter.getErrorMessage());
        }

        return pre_process_file;
    }

    public boolean getCompleted() {
        return completed;
    }
}
