/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.binaryexecutor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.io.stream.ProcessStreamManager;
import uk.ac.uea.cmp.srnaworkbench.io.stream.Utils;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author M.B.Stocks
 */
public enum BinaryManager
{

    UNKNOWN_BINARY
            {
                @Override
                public String run(String[] args) throws Exception
                {
                    throw new UnsupportedOperationException("Unknown binary.");
                }
            },
    RNACOFOLD
            {
                @Override
                public String run(String[] args) throws Exception
                {
//      initialised();
//      
//      File rnacofold_exe = new File( getToolPath( "RNAcofold" ) );
//      
//      logStart( this, rnacofold_exe.getPath() );
//      
//      RNACoFoldOutput out = new RNACoFoldRunner( rnacofold_exe, args[0], args[1], args[2] ).run();
//      
//      sendMessage( out.toString() + " " + LINE_SEPARATOR );
//      
//      logFinish( this, null );

                    throw new UnsupportedOperationException("RNACofold is not currently required by the workbench.");
                }
            },
    RNAFOLD
            {
                @Override
                public String run(String[] args) throws Exception
                {

                    // launch EXE and grab stdin/stdout and stderr
                    String filePath = getToolPath("RNAfold");

                    logStart(this, filePath);

                    Process process = JAVA_RUNTIME.exec(filePath);
                    ProcessStreamManager psm = new ProcessStreamManager(process, "RNAFOLD", false);

                    List<String> data = Arrays.asList(args);
                    psm.enterData(data, "@");

                    // Wait for binary to complete and get output when done.
                    int code = psm.runInForeground(true);
                    List<String> feedback = psm.getStandardOutput();

                    if (code != 0)
                    {
                        throw new IOException("code " + code + " returned.");
                    }

                    // 2 lines of output are expected:
                    // 1 - the sequence
                    // 2 - the dot bracket notation + MFE
                    if (feedback.size() < 2)
                    {
                        throw new IOException("Didn't get expected results.");
                    }

                    // Collect all the dot bracket notation and MFE lines and separate with "~"
                    List<String> results = new ArrayList<String>();
                    for (int i = 1; i < feedback.size(); i += 2)
                    {
                        results.add(feedback.get(i));
                    }

                    String message = Utils.join(results, "~");

                    logFinish(this, psm);

                    return message;
                }
            },
    RNALFOLD
            {
                @Override
                public String run(String[] args) throws Exception
                {

                    // launch EXE and grab stdin/stdout and stderr
                    String filePath = getToolPath("RNALfold");                  

                    logStart(this, filePath);

                    Process process = JAVA_RUNTIME.exec(filePath + args[0]);
                    ProcessStreamManager psm = new ProcessStreamManager(process, "RNALFOLD", false);

                    List<String> data = Arrays.asList(args[1]);
                    psm.enterData(data, "@");

                    // Wait for binary to complete and get output when done.
                    int code = psm.runInForeground(true);
                    List<String> feedback = psm.getStandardOutput();

                    if (code != 0)
                    {
                        throw new IOException("code " + code + " returned.");
                    }

                    // multiple lines of output are expected:
                    // 1 - the sequence
                    // 2 on - the dot bracket notation + MFE
                    if (feedback.size() < 2)
                    {
                        throw new IOException("Didn't get expected results.");
                    }

                    // Collect all the dot bracket notation and MFE lines and separate with "~"
                    List<String> results = new ArrayList<String>();
                    for (int i = 0; i < feedback.size(); i ++)
                    {
                        results.add(feedback.get(i));
                    }

                    String message = Utils.join(results, "~");

                    logFinish(this, psm);

                    return message;
                }
            },
    
    RNAEVAL
            {
                @Override
                public String run(String[] args) throws Exception
                {

                    // launch EXE and grab stdin/stdout and stderr
                    String filePath = getToolPath("RNAeval(version_issue)");

                    logStart(this, filePath);

                    Process process = JAVA_RUNTIME.exec(filePath);
                    ProcessStreamManager psm = new ProcessStreamManager(process, "RNAEVAL", false);

                    List<String> data = Arrays.asList(args);
                    psm.enterData(data, "@");

                    // Wait for binary to complete and get output when done.
                    int code = psm.runInForeground(true);
                    List<String> feedback = psm.getStandardOutput();

                    if (code != 0)
                    {
                        throw new IOException("code " + code + " returned.");
                    }

                    // 2 lines of output are expected:
                    // 1 - the sequence
                    // 2 - the dot bracket notation + MFE
                    if (feedback.size() < 2)
                    {
                        throw new IOException("Didn't get expected results.");
                    }

                    // Collect all the dot bracket notation and MFE lines and separate with "~"
                    List<String> results = new ArrayList<>();
                    for (int i = 1; i < feedback.size(); i += 2)
                    {
                        results.add(feedback.get(i));
                    }

                    String message = Utils.join(results, "~");

                    logFinish(this, psm);

                    return message;
                }
            },
    RNAPLOT2
            {
                @Override
                public String run(String[] args) throws Exception
                {

                    // launch EXE and grab stdin/stdout and stderr
                    // args[0] = output format (ps, gml, xrna, svg)
                    String filePath = getToolPath("RNAplot") + " -o " + args[0];

                    logStart(this, filePath);

                    Process process = JAVA_RUNTIME.exec(filePath);
                    ProcessStreamManager psm = new ProcessStreamManager(process, "RNAPLOT", false);

                    List<String> data = new ArrayList<>();
                    for(int i=1; i<args.length; i++)
                    {
                        data.add(args[i]);
                    }
                    psm.enterData(data, "@");

                    // Wait for binary to complete and get output when done.
                    int code = psm.runInForeground(true);
                    //List<String> feedback = psm.getStandardOutput();

                    if (code != 0)
                    {
                        throw new IOException("code " + code + " returned.");
                    }

                    // 2 lines of output are expected:
                    // 1 - the sequence
                    // 2 - the dot bracket notation + MFE
                  //  if (feedback.size() < 2)
                   // {
                    //    throw new IOException("Didn't get expected results.");
                   // }

                    // Collect all the dot bracket notation and MFE lines and separate with "~"
                   // List<String> results = new ArrayList<>();
                    //for (int i = 1; i < feedback.size(); i += 2)
                    //{
                     //   results.add(feedback.get(i));
                   // }

                   // String message = Utils.join(results, "~");

                    logFinish(this, psm);

                    return "RNAplot complete";
                }
            },
    RNAPLOT
            {
                @Override
                public String run(String[] args) throws Exception
                {

      //RNAplot_cmd --pre \"$rnaplot_ps_macro\" < $RNAfold_out_file "
                    // launch EXE and grab stdin/stdout and stderr
                    String filePath = getToolPath("RNAplot") + " --pre " + args[0];
                    Process process = null;

                    if (!AppUtils.INSTANCE.getArchitecture().isWindows())
                    {
                        try
                        {
                            String command = _binaries_path + DIR_SEPARATOR + "RNAplotWrapper";

                            File myRNAFoldFile = new File(command);

                            LOGGER.log(Level.FINE, this.name() + ": WRAPPER PATH: {0}", command);

                            FileWriter outScriptFile = new FileWriter(myRNAFoldFile, false);
                            PrintWriter outScriptWriter = new PrintWriter(outScriptFile);

                            outScriptWriter.println("#!/bin/bash");

                            command = getToolPath("RNAplot") + " --pre " + args[0];
                            outScriptWriter.println(command);

                            LOGGER.log(Level.FINE, this.name() + ": instruction: {0}", command);

                            outScriptWriter.close();
                            outScriptFile.close();
                            JAVA_RUNTIME.exec("chmod +x " + myRNAFoldFile.getAbsolutePath());
                        }
                        catch (IOException ex)
                        {
                            LOGGER.log(Level.SEVERE, "RNAPLOT", ex);
                        }

                        process = JAVA_RUNTIME.exec(_binaries_path + DIR_SEPARATOR + "RNAplotWrapper");
                    }
                    else
                    {
                        process = JAVA_RUNTIME.exec(filePath);
                    }

                    logStart(this, filePath);

                    ProcessStreamManager psm = new ProcessStreamManager(process, "RNAPLOT", true);

                    // Enter data (first we have to converted to List<String>)
                    if (args.length < 3)
                    {
                        throw new IllegalArgumentException("Invalid nubmer of arguments.");
                    }

                    // Get rid of the first arg as it's only the post script modification argument
                    List<String> data = Arrays.asList(args).subList(1, args.length);
                    psm.enterData(data, "@");

                    // Wait for binary to complete and get output when done.
                    int code = psm.runInForeground(true);

                    if (code != 0)
                    {
                        throw new IOException("code " + code + " returned.");
                    }

                    logFinish(this, psm);
                    return "RNAfold completed";
                }
            },
    VELVET
            {
                @Override
                public String run(String[] args) throws Exception
                {
//      initialised();
//      
//      logStart( this, "" );
//      
//      new VelvetRunner( args ).run();      
//      
//      sendMessage( "Velvet finished " + LINE_SEPARATOR );
//      
//      logFinish( this, null );
                    throw new UnsupportedOperationException("Velvet is not currently required by the workbench.");
                }
            },
    MUSCLE
            {
                @Override
                public String run(String[] args) throws Exception
                {
//      initialised();
//      
//      logStart( this, "" );
//      
//      new MuscleRunner( args ).run();
//      
//      sendMessage( "Muscle finished " + LINE_SEPARATOR );
//      
//      logFinish( this, null );
                    throw new UnsupportedOperationException("Muscle is not currently required by the workbench.");
                }
            },
    RANDFOLD
            {
                @Override
                public String run(String[] args) throws Exception
                {

                    String result = "";

                    // launch EXE and grab stdin/stdout and stderr
                    String cl = getToolPath("randfold") + args[0];

                    logStart(this, cl);

                    Process process = JAVA_RUNTIME.exec(cl);
                    ProcessStreamManager psm = new ProcessStreamManager(process, "RANDFOLD", true);

 
                    List<String> data = Arrays.asList(args);
                    psm.enterData(data, "@");

                    // Wait for binary to complete and get output when done.
                    int code = psm.runInForeground(true);
                    List<String> feedback = psm.getStandardOutput();

                    if (code != 0)
                    {
                        throw new IOException("code " + code + " returned.");
                    }

                    // 1 line of output is expected...
                    if (feedback.size() < 1)
                    {
                        throw new IOException("Didn't get expected results.");
                    }

                    result = feedback.get(0);

                    logFinish(this, psm);
                    return result;
                }
            },
    PATMAN
            {
                @Override
                public String run(String[] args) throws Exception
                {

                    // launch EXE and grab stdin/stdout and stderr
                    String filePath = getToolPath("patman");

                    String command = filePath + " " + args[0];

                    logStart(this, command);

                    Process process = JAVA_RUNTIME.exec(command);

                    ProcessStreamManager psm = new ProcessStreamManager(process, "PATMAN", true);
                    int code = psm.runInForeground(false);

                    if (code != 0)
                    {
                        throw new IOException("code " + code + " returned.");
                    }

                    logFinish(this, psm);

                    return "patman finished";
                }
            },
    FASTQ_DUMP
            {
                @Override
                public String run(String[] args) throws Exception
                {
                    String filePath = "fastq-dump";
                    String command = filePath;

                    command += filePath + " " + args[0] + " " + "-O " + (new File(args[0])).getParent();
                    System.out.println(command);
                    Process process = JAVA_RUNTIME.exec(command);

                    ProcessStreamManager psm = new ProcessStreamManager(process, "FASTQ-DUMP", true);
                    int code = psm.runInForeground(false);

                    if (code != 0)
                    {
                        throw new IOException("code " + code + " returned.");
                    }
                    logFinish(this, psm);

                    return "fastq-dump finished ";
                }
            },
    BOWTIE
            {
                @Override
                public String run(String[] args) throws Exception
                {
                    Files.deleteIfExists(Paths.get(args[1]));
                    Files.createFile(Paths.get(args[1]));
                    // launch EXE and grab stdin/stdout and stderr
                    String filePath = "";

                    if (AppUtils.INSTANCE.getArchitecture().isWindows())
                    {
          //here is where we will need to replicate the work done by the Bowtie
                        //wrapper script. This will be detecting long and short index formats
                        //as well as potentially creating the indexes in the future
                        //for now, the workbench supports only the short format index on windows
                        filePath = getToolPath("bowtie" + DIR_SEPARATOR + "bowtie-align-s");
                    }
                    else
                    {
                        filePath = getToolPath("bowtie" + DIR_SEPARATOR + "bowtie");
                    }

                    String command = filePath + " " + args[0];

                    logStart(this, command);

                    Process process = JAVA_RUNTIME.exec(command);

                    ProcessStreamManager psm = new ProcessStreamManager(process, "BOWTIE", true);
                    int code = psm.runInForeground(Paths.get(args[1]));

                    if (code != 0)
                    {
                        throw new IOException("code " + code + " returned.");
                    }

                    logFinish(this, psm);

                    return "bowtie finished ";
                }
            },
    GHOSTSCRIPT
            {
                @Override
                public String run(String[] args) throws Exception
                {

                    // launch EXE and grab stdin/stdout and stderr
                    String command = getToolPath("gs") + " " + args[0];

                    logStart(this, command);

                    Process process = JAVA_RUNTIME.exec(command);
                    ProcessStreamManager psm = new ProcessStreamManager(process, "GHOSTSCRIPT", true);
//
//                    // Enter data (first we have to converted to List<String>)
//                    if (args.length < 2)
//                    {
//                        throw new IllegalArgumentException("Invalid nubmer of arguments.");
//                    }

  
                    List<String> data = Arrays.asList(args);
                    psm.enterData(data, LINE_SEPARATOR);

                    // Wait for binary to complete and get output when done.
                    int code = psm.runInForeground(true);

                    if (code != 0)
                    {

                        throw new IOException("code " + code + " returned.");
                    }
                    else
                    {
                        logFinish(this, psm);
                        return ("Ghostscript completed successfully" + LINE_SEPARATOR);
                    }

                }
            };

    /* The runtime */
    private final static Runtime JAVA_RUNTIME = Runtime.getRuntime();

    private static String _binaries_path = Tools.EXE_DIR;


    /**
     * The procedure for calling the binary. Must be implemented by every enum
     * instance.
     *
     * @param args The arguments describing how to run the binary
     * @throws Exception Thrown if there were any problems running the binary.
     */
    public abstract String run(String[] args) throws Exception;

    /**
     * Before running a binary the Binary manager must be initialised with
     * certain settings detailing where the binaries are located, the pipe to
     * send results back through and whether details of binary execution should
     * be printed to the console.
     *
     * @param binaries_path The location of the binaries
     * @param out The output stream to which results should be piped
     */


    private static void logStart(BinaryManager binary, String command_line)
    {
        LOGGER.log(Level.INFO, "{1}: executing command line: {2}", new Object[]
        {
            binary.name(), command_line
        });
    }

    private static void logFinish(BinaryManager binary, final ProcessStreamManager psm)
    {
        String returnCodeMsg = psm == null ? "" : "Return code: " + psm.getReturnCode();
        String feedbackMsg = psm == null ? "" : "Feedback size: " + (psm.getStandardOutput() == null ? "null" : Integer.toString(psm.getStandardOutput().size()));

        LOGGER.log(Level.INFO, "{1}: completed successfully. {2}; {3}", new Object[]
        {
            binary.name(), returnCodeMsg, feedbackMsg
        });
    }

    private static String getToolPath(String toolName)
    {
        String binariesDirectory = AppUtils.INSTANCE.getArchitecture().getBinariesDirectory();
        String path = _binaries_path + DIR_SEPARATOR + binariesDirectory + DIR_SEPARATOR + toolName + AppUtils.INSTANCE.getArchitecture().getExeExtension();

        return AppUtils.addQuotesIfNecessary(path);
    }


}
