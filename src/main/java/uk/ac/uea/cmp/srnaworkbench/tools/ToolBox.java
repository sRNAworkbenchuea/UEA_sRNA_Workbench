/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Rectangle2D;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.*;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.wizard.FX.HTMLWizardViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.*;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.Filter;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.*;
import uk.ac.uea.cmp.srnaworkbench.tools.mirprof.*;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationProcess;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationParams;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.Degradome;
import uk.ac.uea.cmp.srnaworkbench.tools.sequencealignment.SARunner;
import uk.ac.uea.cmp.srnaworkbench.tools.siloco.SiLoCoParams;
import uk.ac.uea.cmp.srnaworkbench.tools.siloco.SiLoCoProcess;
import uk.ac.uea.cmp.srnaworkbench.tools.tasi.*;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanRunner;
import uk.ac.uea.cmp.srnaworkbench.workflow.PreconfiguredWorkflows;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;

/**
 *
 * @author prb07qmu
 */
public enum ToolBox
{
  // The unknown tool (default)
  //
// The unknown tool (default)
  //
  UNKNOWN_TOOL( "unknown" )
  {
    @Override
    public void startTool( Map<String, String> argmap ) throws IOException
    {
      throw new IllegalArgumentException( "Error: The tool name is unrecognised." );
    }

    @Override
    public void printUsage()
    {
      System.out.println( USAGE_PREFIX + "<toolname> <options>" );
      System.out.println( "where" );

      ArrayList<String> toolNames = new ArrayList<String>();

      for ( ToolBox tool : ToolBox.values() )
      {
        if ( tool == UNKNOWN_TOOL || tool == DEBUG_TOOL )
        {
          continue;
        }

        toolNames.add( tool.getName() );
      }

      System.out.println( "  <toolname> is one of " + toolNames );
    }
  },
  //
  // The adaptor removal tool
  //
  ADAPTOR_TOOL( "adaptor" )
  {
    @Override
    public void startTool( Map<String, String> argmap ) throws IOException
    {
      boolean overwriteOutput = argmap.containsKey( "f" );
      
      File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
      File outFile = ToolBox.checkOutputFile( "out_file", argmap.get( "out_file" ), true, overwriteOutput );
      File paramsFile = ToolBox.checkInputFile( "params", argmap.get( "params" ), false );

      AdaptorRemoverParams params = ( paramsFile == null ? new AdaptorRemoverParams() : AdaptorRemoverParams.load( new AdaptorRemoverParams(), paramsFile ) );

      ToolBox.printParameters( params );

      AdaptorRemover ar = new AdaptorRemover( srnaFile, outFile, params );
      ar.run();
    }

    @Override
    public void printUsage()
    {
      System.out.println( USAGE_PREFIX + getName() + "  [-f] -srna_file srna-file-path  -out_file output-file-path  [ -params params-file-path ]\n -f = Force overwriting of output file" );
    }
  },
  //
  // The debug tool
  //
  DEBUG_TOOL( "debug" )
  {
    @Override
    public void startTool( Map<String, String> argmap ) throws IOException
    {
      // An aid to debugging the command line - command line options will be listed
      // The option 'sleep' causes the thread to sleep for a few seconds

      System.out.println( "Debugging command line. Option and value will be listed." );

      //uk.ac.uea.cmp.srnaworkbench.tools.vissr.tplots.TPlotFrame.main( null );

      for ( String key : argmap.keySet() )
      {
        String value = argmap.get( key );
        System.out.println( "Option: " + key + ", Value: " + value );
      }

      if ( argmap.containsKey( "sleep" ) )
      {
        final int SECONDS_TO_SLEEP = 20;
        System.out.println( "Workbench sleeping for " + SECONDS_TO_SLEEP + " seconds..." );

        try
        {
          TimeUnit.SECONDS.sleep( SECONDS_TO_SLEEP );
        }
        catch ( InterruptedException ex )
        {
          // shhh
        }
      }
    }

    @Override
    public void printUsage()
    {
      System.out.println( USAGE_PREFIX + getName() + "  [ -sleep [ list-of-parameters ] ]" );
    }
  },
  //
  // The PARESnip tool
  //
  PARESNIP_TOOL( "paresnip" )
  {
    @Override
    public void startTool( Map<String, String> argmap ) throws IOException
    {
      // Check files according to the order given in the usage
      boolean overwriteOutput = argmap.containsKey( "f" );

      // Check mandatory files first...
      File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
      File degradomeFile = ToolBox.checkInputFile( "deg_file", argmap.get( "deg_file" ), true );
      File transcriptomeFile = ToolBox.checkInputFile( "tran_file", argmap.get( "tran_file" ), true );
      File outFile = ToolBox.checkOutputFile( "out_file", argmap.get( "out_file" ), true, overwriteOutput );

      // Now check the optional files...
      File genomeFile = ToolBox.checkInputFile( "genome_file", argmap.get( "genome_file" ), false );
      File paramsFile = ToolBox.checkInputFile( "params", argmap.get( "params" ), false );

      Degradome d = new Degradome( srnaFile, degradomeFile, transcriptomeFile, genomeFile, outFile, paramsFile );
      d.runProcedure();
    }

    @Override
    public void printUsage()
    {
      System.out.println( USAGE_PREFIX + getName() + "  [-f] -srna_file srna-file-path  -deg_file degradome-file-path  -tran_file transcriptome-file-path  -out_file output-file-path  [ -genome_file genome-file-path  -params params-file-path ]\n -f = Force overwriting of output file" );
    }
  },
  //
  // The filter tool
  //
  FILTER_TOOL( "filter" )
  {
    @Override
    public void startTool( Map<String, String> argmap ) throws IOException
    {
      boolean overwriteOutput = argmap.containsKey( "f" );
      File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
      File outFile = ToolBox.checkOutputFile( "out_file", argmap.get( "out_file" ), true, overwriteOutput );
      File paramsFile = ToolBox.checkInputFile( "params", argmap.get( "params" ), false );

      FilterParams params = ( paramsFile == null ? new FilterParams() : FilterParams.load( new FilterParams(), paramsFile ) );
//       System.out.println();
//      System.out.println( "Parameters:" );
//      System.out.println( params.toString() );
//      System.out.println();

      ToolBox.printParameters( params );

      Filter filter = new Filter( srnaFile, outFile, Tools.getNextDirectory(), params );
      filter.run();
    }

    @Override
    public void printUsage()
    {
      System.out.println( USAGE_PREFIX + getName() + "  [-f] -srna_file srna-file-path  -out_file output-file-path  [ -params params-file-path ]" );
    }
  },
  //
  // The miRCat tool
  //
  MIRCAT_TOOL( "mircat" )
  {
    @Override
    public void startTool(Map<String, String> argmap) throws IOException
    {
        File srnaFile = ToolBox.checkInputFile("srna_file", argmap.get("srna_file"), true);
        File genomeFile = ToolBox.checkInputFile("genome", argmap.get("genome"), true);
        File paramsFile = ToolBox.checkInputFile("params", argmap.get("params"), false);

        // Check using workflow mode
        boolean workflowMode = argmap.containsKey("pcw");

        MiRCatParams params = (paramsFile == null ? new MiRCatParams() : MiRCatParams.load(new MiRCatParams(), paramsFile));

        int numToGenerate = params.getThreadCount();
        ToolBox.printParameters(params);

        if (workflowMode)
        {
            ArrayList<Path> sample1 = new ArrayList<>();
            sample1.add(srnaFile.toPath());
            Map<String, List<Path>> samples = new HashMap<>();
            samples.put("miRCATRun", sample1);
            DatabaseWorkflowModule.getInstance().insertRawData(samples, genomeFile.toPath(), null);

            final String OUTPUT_DIR_ARG = "output_directory";

            Path outputPath = Paths.get(Tools.miRCAT_DATA_Path + DIR_SEPARATOR + "default_mircat_output");
            // Implementing argument to change the default ouput directory for miRCat results.
            if (argmap.containsKey(OUTPUT_DIR_ARG))
            {
                outputPath = Paths.get(argmap.get(OUTPUT_DIR_ARG));
            }

            PreconfiguredWorkflows.createMirCatWorkflow(Rectangle2D.EMPTY, true, outputPath, numToGenerate);

            try
            {
                HTMLWizardViewController.setGenomePath(genomeFile.toPath());

                HTMLWizardViewController.configureWorkflowData();
            }
            catch (Exception ex)
            {
                Logger.getLogger(ToolBox.class.getName()).log(Level.SEVERE, null, ex);
            }

            WorkflowManager.getInstance().start();



        }
        else
        {

            Process_Hits_Patman mirCatProcess = new Process_Hits_Patman(params, srnaFile, genomeFile, Tools.getNextDirectory(), null);

            // Implementing argument to change the default ouput directory for miRCat results.
            final String OUTPUT_DIR_ARG = "output_directory";
            if (argmap.containsKey(OUTPUT_DIR_ARG))
            {
                mirCatProcess.setOutputDirectoryFilename(argmap.get(OUTPUT_DIR_ARG));
            }
            try
            {
                mirCatProcess.setupThreads(numToGenerate);
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage());
            }
            mirCatProcess.run();
        }
    }

    @Override
    public void printUsage()
    {
      System.out.println( USAGE_PREFIX + getName() + "  -srna_file srna-file-path  -genome genome-file-path  [ -params params-file-path ] [ -output_directory ouput-directory-path ] [-pcw use-workflow-mode]" );
    }
  },
  //
  // The SiLoCo tool
//  //
  SILOCO_TOOL( "siloco" )
  {
    @Override
    public void startTool( Map<String, String> argmap ) throws IOException
    {
      String fileListString = argmap.get( "srna_file_list" );
      if(fileListString == null || fileListString.isEmpty())
        throw new IllegalArgumentException("Error: The parameter srna_file_list must be specified." );
      String fileList[] = fileListString.split( ",");
      ArrayList<File> sampleFiles = new ArrayList<File>(fileList.length);
      int i = 0;
      for(String fileName : fileList)
      {
        sampleFiles.add(ToolBox.checkInputFile( "srna_file_list", fileName, true ));
        i++;
      }
      File genomeFile = ToolBox.checkInputFile( "genome", argmap.get( "genome" ), true );
      File paramsFile = ToolBox.checkInputFile( "params", argmap.get( "params" ), false );

      SiLoCoParams params = ( paramsFile == null ? new SiLoCoParams() : SiLoCoParams.load( new SiLoCoParams(), paramsFile ) );

      ToolBox.printParameters( params );

      SiLoCoProcess siLoCoProcess = new SiLoCoProcess( params, genomeFile, sampleFiles );
      siLoCoProcess.run();
    }

    @Override
    public void printUsage()
    {
      System.out.println( USAGE_PREFIX + getName() + "  -srna_file_list comma-seperated-srna-file-paths  -genome genome-file-path  [ -params params-file-path ]" );
    }
  },
  //
  // The miRProf tool
  //
  MIRPROF_TOOL( "mirprof" )
  {
    @Override
    public void startTool( Map<String, String> argmap ) throws IOException
    {
      boolean overwriteOutput = argmap.containsKey( "f" );
      
      //File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
      String fileListString = argmap.get( "srna_file_list" );
      if(fileListString == null || fileListString.isEmpty())
        throw new IllegalArgumentException("Error: The parameter srna_file_list must be specified." );
      String fileList[] = fileListString.split( ",");
      File sampleFiles[] = new File[fileList.length];
      int i = 0;
      for(String fileName : fileList)
      {
        sampleFiles[i] = ToolBox.checkInputFile( "srna_file_list", fileName, true );
        i++;
      }
      File mirbaseFile = ToolBox.checkInputFile( "mirbase_db", argmap.get( "mirbase_db" ), true );
      File outFile = ToolBox.checkOutputFile( "out_file", argmap.get( "out_file" ), true, overwriteOutput );
      File paramsFile = ToolBox.checkInputFile( "params", argmap.get( "params" ), false );
      File genomeFile = ToolBox.checkInputFile( "genome", argmap.get( "genome" ), false );

      MirprofParams params = ( paramsFile == null ) ? new MirprofParams() : MirprofParams.load( new MirprofParams(), paramsFile );

      ToolBox.printParameters( params );

      Mirprof mirprof = new Mirprof( Arrays.asList( sampleFiles ), genomeFile, mirbaseFile, outFile, Tools.getNextDirectory(), params );
      mirprof.run();
    }

    @Override
    public void printUsage()
    {
      System.out.println( USAGE_PREFIX + getName() + "  [-f] -srna_file_list comma-seperated-srna-file-paths  -mirbase_db mirbase-file-path  -out_file output-file-path  [ -params params-file-path -genome genome-file-path ]\n -f = Force overwriting of output file" );
    }
  },
  //
  // The tasi prediction tool
  //
  TASI_TOOL( "tasi" )
  {
    @Override
    public void startTool( Map<String, String> argmap ) throws IOException
    {
      boolean overwriteOutput = argmap.containsKey( "f" );
      
      File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
      File outFile = ToolBox.checkOutputFile( "out_file", argmap.get( "out_file" ), true, overwriteOutput );
      File paramsFile = ToolBox.checkInputFile( "params", argmap.get( "params" ), false );
      File genomeFile = ToolBox.checkInputFile( "genome", argmap.get( "genome" ), true );

      TasiParams params = ( paramsFile == null ) ? new TasiParams() : TasiParams.load( new TasiParams(), paramsFile );

      ToolBox.printParameters( params );
      params.setGenome( genomeFile );

      TasiAnalyser ta = new TasiAnalyser( srnaFile, outFile, Tools.getNextDirectory(), params );
      ta.run();
    }

    @Override
    public void printUsage()
    {
      System.out.println( USAGE_PREFIX + getName() + "  [-f] -srna_file srna-file-path -genome genome-file-path -out_file output-file-path  [ -params params-file-path ]\n -f = Force overwriting of output file" );
    }
  },
  // Normalisation tool
  NORMALISE_TOOL("normalise")
  {
    @Override
    public void startTool(Map<String, String> argmap) throws IOException
    {
      String fileListString = argmap.get( "srna_file_list" );
      if(fileListString == null || fileListString.isEmpty())
        throw new IllegalArgumentException("Error: The parameter srna_file_list must be specified." );
      String fileArray[] = fileListString.split( ",");
      int i = 0;
//      String sampleFiles[] = new String[fileArray.length]; 
      ArrayList<File> fileList = new ArrayList<>();
      for(String fileName : fileArray)
      {
        fileList.add( ToolBox.checkInputFile( "srna_file_list", fileName, true ));
        //sampleFiles[i] = ToolBox.checkInputFile( "srna_file_list", fileName, true ).getName();
        //i++;
        
      }
      
      ArrayList<ArrayList<File>> nestedFileList = new ArrayList<>();
      nestedFileList.add(fileList);
 
      File outDir = ToolBox.checkOutputFile( "out_dir", argmap.get( "out_dir" ), true, true );
      if(!outDir.isDirectory())
      {
        throw new IllegalArgumentException("Argument out_dir must be a directory");
      }
      File paramsFile = ToolBox.checkInputFile( "params", argmap.get( "params" ), false );
      File genomeFile = ToolBox.checkInputFile( "genome", argmap.get( "genome" ), true );
      
      boolean writeCSV = argmap.containsKey( "c" );
      
      try
      {
        NormalisationType normType = NormalisationType.valueOf( argmap.get( "normalisation_type") );

      
        NormalisationParams params = NormalisationParams.load(new NormalisationParams(), paramsFile);

        NormalisationProcess norm_engine = new NormalisationProcess(normType, nestedFileList, genomeFile, null, params, null, outDir );
        norm_engine.run();
        if(writeCSV)
        {
          norm_engine.getMatrix().writeToCsv( new File( outDir.getPath() + DIR_SEPARATOR + normType.toString() + "_out.csv" ), normType);
        }
      }
      catch(IllegalArgumentException e)
      {
        throw new IllegalArgumentException("Error: the normalisation_type defined is not known");
      }
      
      
    }
    @Override
    public void printUsage()
    {
      System.out.println( USAGE_PREFIX + getName() + "  -srna_file_list [srna-file,]+ -genome genome-file-path -out_dir output-directory  [ -params params-file-path ] [-c] write-csv" );
    }
  },
    //
  // The sequence alignment tool
  //
  SEQ_ALIGNMENT( "seq_align" )
  {
    @Override
    public void startTool( Map<String, String> argmap ) throws IOException
    {
      boolean overwriteOutput = argmap.containsKey( "f" );
      
      //File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
      String fileListString = argmap.get( "srna_file_list" );
      if(fileListString == null || fileListString.isEmpty())
        throw new IllegalArgumentException("Error: The parameter srna_file_list must be specified." );
      String fileList[] = fileListString.split( ",");
      File sampleFiles[] = new File[fileList.length];
      int i = 0;
      for(String fileName : fileList)
      {
        sampleFiles[i] = ToolBox.checkInputFile( "srna_file_list", fileName, true );
        i++;
      }
      File out_file = ToolBox.checkOutputFile( "out_file", argmap.get( "out_file" ), true, overwriteOutput );
      File paramsFile = ToolBox.checkInputFile( "params", argmap.get( "params" ), false );
      File genomeFile = ToolBox.checkInputFile( "genome", argmap.get( "genome" ), true );

      PatmanParams params  = ( paramsFile == null ) ? new PatmanParams() : PatmanParams.load( new PatmanParams(), paramsFile );

      ToolBox.printParameters( params );
   
//    LOGGER.log( Level.INFO,  + "Starting job: Short Reads: {0}; Long Reads: {1}; Out: {2}; Params: {3}", 
//      new Object[]{short_reads.getPath(), long_reads.getPath(), output.getPath(), pp.toString()} );
    
      if ( out_file.isDirectory() )
      {
        //String singleOutPath = out_file.getAbsolutePath() + DIR_SEPARATOR + FilenameUtils.removeExtension( in_file.getName() ) + "_Align.patman";

        for ( File inputFile : sampleFiles )
        {
//          PatmanRunner newRunner = new PatmanRunner( inputFile, genomeFile );
//          newRunner.runSequenceAligner( inputFile, long_reads, output_dir, params, null );
//          this.sa_runners.put( inputFile.getName(), newRunner );
        }
      }
      else
      {
        
      }

//      TasiAnalyser ta = new TasiAnalyser( srnaFile, outFile, Tools.getNextDirectory(), params );
//      ta.run();
    }

    @Override
    public void printUsage()
    {
      System.out.println( USAGE_PREFIX + getName() + "  [-f] -srna_file srna-file-path -genome genome-file-path -out_file output-file-path  [ -params params-file-path ]\n -f = Force overwriting of output file" );
    }
  },
      //
  // The sequence alignment tool
  //
  MIRCAT_2( "mircat2" )
  {
      
    @Override
    public void startTool( Map<String, String> argmap ) throws IOException
    {

      boolean overwriteOutput = argmap.containsKey( "f" );
      
      //File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
      String jsonConfigString = argmap.get( "config" );
      if(jsonConfigString == null || jsonConfigString.isEmpty())
        throw new IllegalArgumentException("Error: The parameter config must be specified." );
      
      PreconfiguredWorkflows.runCommandLineWorkflowMiRCat2("mircat2", new File(jsonConfigString));
           

       WorkflowManager.getInstance().start();


    }

    @Override
    public void printUsage()
    {

      System.out.println( USAGE_PREFIX + getName() + "  [-f] -config configuration file containing all required input for miRCAT 2 in JSON format"
              + LINE_SEPARATOR
              + "more specific information can be found at http://srna-workbench.cmp.uea.ac.uk/"
              + LINE_SEPARATOR
              + "an example of a miRCAT 2 configuration file can be found in: [install directory]/data/default_json_setup"
              + LINE_SEPARATOR 
              + "-f = Force overwriting of output file" );
    }
  };
  //
  // The tasi prediction tool
  //
//  SILOCO( "siloco" )
//  {
//    @Override
//    public void startTool( Map<String, String> argmap ) throws IOException
//    {
//      File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
//      File genomeFile = ToolBox.checkOutputFile( "genome_file", argmap.get( "genome_file" ), true );
//      File paramsFile = ToolBox.checkInputFile( "params", argmap.get( "params" ), false );
//
//      SiLoCoParams params = ( paramsFile == null ) ? new SiLoCoParams() : SiLoCoParams.load( new SiLoCoParams(), paramsFile );
//
//      ToolBox.printParameters( params );
//
//      SiLoCoProcess sp = new SiLoCoProcess(null);
//      sp.inputSampleData( new File[]
//        {
//          srnaFile
//        } );
//      sp.inputGenomeFile( genomeFile );
//      sp.inputParams( params );
//
//      sp.run();
//
//      //TODO Siloco currently doesn't dump data to file (AFAIK).  Need to implement this for command line use.
//    }
//
//    @Override
//    public void printUsage()
//    {
//      System.out.println( USAGE_PREFIX + getName() + "  -srna_file srna-file-path  -genome_file genome-file-path  [ -params params-file-path ]" );
//    }
//  };

  private static void printParameters( ToolParameters params )
  {
    if ( AppUtils.INSTANCE.verbose() )
    {
      System.out.println();
      System.out.println( "Parameters:" );
      System.out.println( params.toString() );
      System.out.println();
    }
  }

  /*
   * Instance methods and state
   */
  private final String _name;

  private ToolBox( String name )
  {
    _name = name;
  }

  public String getName()
  {
    return _name;
  }

  /**
   * Start the tool
   * @param argmap Map of command line arguments
   * @throws IOException
   */
  public abstract void startTool( Map<String, String> argmap ) throws IOException;

  /**
   * Explain how to run a tool
   */
  public abstract void printUsage();

  /* Static methods and state */
  private static final String USAGE_PREFIX = "Usage:\n  java -jar /path/to/Workbench.jar [-verbose] -tool ";
  private static final Map< String, ToolBox> TOOLS = new HashMap< String, ToolBox>();

  static
  {
    for ( ToolBox t : ToolBox.values() )
    {
      TOOLS.put( t._name, t );
    }
  }

  /**
   * Get a tool for a given name
   * @param toolName The name of the tool
   * @return The tool or the unknown tool
   */
  public static ToolBox getToolForName( String toolName )
  {
    if ( toolName != null )
    {
      toolName = toolName.toLowerCase();
    }

    ToolBox t = TOOLS.get( toolName );

    if ( t == null )
    {
      return UNKNOWN_TOOL;
    }

    return t;
  }

  /**
   * Check the validity of an input file. If a file is specified, then it is considered valid if
   * it exists and it's readable.
   *
   * @param parameterName
   * @param filename
   * @param isMandatory
   *
   * @return The File object, or null
   */
  private static File checkInputFile( String parameterName, String filename, boolean isMandatory )
  {
    if ( filename == null || filename.trim().isEmpty() )
    {
      if ( isMandatory )
      {
        throw new IllegalArgumentException( String.format( "Error: The parameter '%s' must be specified.", parameterName ) );
      }
      else
      {
        return null;
      }
    }

    // The filename is specified, check it's a proper file...
    //
    File f = new File( filename );

    boolean fileExistsAndIsOK = ( f.exists() && f.isFile() && f.canRead() );

    if ( ! fileExistsAndIsOK )
    {
      throw new IllegalArgumentException( String.format( "Error: The '%s' file, '%s', either does not exist or is unreadable.", parameterName, filename ) );
    }

    return f;
  }

  /**
   * Check the validity of an output file. If the file already exists then it is not overwritten.
   *
   * @param parameterName
   * @param filename
   * @param isMandatory
   *
   * @return The File object
   */
  private static File checkOutputFile( String parameterName, String filename, boolean isMandatory, boolean overwriteOutput )
  {
    if ( filename == null || filename.trim().isEmpty() )
    {
      if ( isMandatory )
      {
        throw new IllegalArgumentException( String.format( "Error: The parameter '%s' must be specified.", parameterName ) );
      }
      else
      {
        return null;
      }
    }

    // The filename is specified, check whether it exists...
    //
    File f = new File( filename );

    if ( f.exists() && !overwriteOutput )
    {
      throw new IllegalArgumentException( String.format( "Error: The '%s' file, '%s', already exists, please choose a different name for the file.", parameterName, filename ) );
    }

    return f;
  }
}
