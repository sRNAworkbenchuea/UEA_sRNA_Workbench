/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat.DB;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.SRNA_Locus_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.exceptions.HairpinExtensionException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.IDDoesNotExistException;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.MiRCatParams;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.MiRCatLogger.MIRCAT_LOGGER;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.PredictionDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Precursor_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Prediction_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.PrecursorServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.PredictionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;

/**
 *
 * @author w0445959
 */
public class MiRNAPredictor implements Runnable
{

    private final Sequence_Entity topSequenceHit;
    private final SRNA_Locus_Entity locus;
    private final Aligned_Sequences_Entity topAlignment;
    private final MiRCatParams params;
    private final GenomeManager genomeManager;
    private final String chromID;
    private boolean outputLogging = false;
    private final BinaryExecutor myExeMan;
    
    private boolean fileMode = false;
    private String outputDir = "";

    //vars used to access result String array from RNA fold. String array is returned by method getStructure()
    private static final int FIRST_PART = 0;
    private static final int MODIFIED_REGION = 1;
    private static final int END_PART = 2;
    private static final int ORIGINAL_RNAFOLD_RESULT = 3;
    private static final int ORIGINAL_RNAFOLD_RESULT_NO_MOD = 4;
    private static final int TRUNCATED_WINDOW_SEQ = 5;
    private static final int RANDFOLD_MFE = 1;
    private static final int RANDFOLD_P_Val = 2;
    private boolean withrandfold = true;
    private int myThreadID;
    private String tempDirLocation;
    private final String filename;
    private PredictionDAOImpl predictionDAO;
    private PrecursorServiceImpl precursorServ;
    private PredictionServiceImpl predictionServ;
    private Session session;
    
    private int batchFlusher = 0;

    public MiRNAPredictor(SRNA_Locus_Entity locus, Sequence_Entity topSequenceHit, Aligned_Sequences_Entity topAlignment,
            MiRCatParams params, GenomeManager genomeManager, String chromID, boolean outputLogging,
            String tempDir, int myThreadID, String filename, boolean fileMode, String outputDir,
            PrecursorServiceImpl precServ, PredictionServiceImpl predServ, PredictionDAOImpl predictionDAO, Session session)
    {
        this.locus = locus;
        this.topSequenceHit = topSequenceHit;
        this.topAlignment = topAlignment;
        this.params = params;
        this.genomeManager = genomeManager;
        this.chromID = chromID;
        this.outputLogging = outputLogging;
        this.myExeMan = new BinaryExecutor();
        this.myThreadID = myThreadID;
        this.tempDirLocation = tempDir;
        this.filename = filename;
        this.fileMode = fileMode;
        this.outputDir = outputDir;
//        
//        this.precursorServ = precServ;
//        this.predictionServ = predServ;
        this.predictionDAO = predictionDAO;
        
        precursorServ = (PrecursorServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("PrecursorService");
        predictionServ = (PredictionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("PredictionService");

        this.session = session;//this.predictionDAO.getSessionFactory().openSession();
        
    

    }

    @Override
    public void run()
    {
        processWindows();
    }

    public void processWindows()
    {
        
        try
        {

            int currentStart = topAlignment.getStart();
            int newStart = 0;
            int currentEnd = topAlignment.getEnd();
            int newEnd = 0;

        //locus contains a top read that is a repeated element
            //not completely sure what the best thing to do at this point is,
            //for now intuition tells me that it should be ignored..
            if (currentStart < 0 && currentEnd < 0)
            {
                return;
            }

            float extend = params.getExtend();

            int[] currentPointer
                    =
                    {
                        0
                    };

//        System.out.println("topAlignment : " +  topAlignment);
//        System.out.println("top Sequence: " + topSequenceHit);
//        
//        for(Aligned_Sequences_Entity seq : locus.getSequences())
//        {
//            if(seq.getRna_seq().equals("TCGGACCAGGCTTCATCCCCC"))
//                System.out.println("");
//        }
            float extendAmount = 0.0f;

            ArrayList<String> flankingSequences = new ArrayList<>();
            ArrayList<String> processedHairpins = new ArrayList<>();
            HashMap<Float, ArrayList<String>> data = new HashMap<>();

            for (int retry = 0; retry < 14; retry++)
            {
                switch (retry)
                {

                    case 0:
                        newStart = Math.round(currentStart - (extend));
                        newEnd = Math.round(currentEnd + (extend));

                        extendAmount = extend;
                        break;
                    case 1:
                        newStart = (currentStart - Math.round(extend * 0.2f));
                        newEnd = (currentEnd + Math.round(extend * 1.8f));

                        extendAmount = extend * 0.2f;
                        break;
                    case 2:
                        newStart = (currentStart - Math.round(extend * 1.8f));
                        newEnd = (currentEnd + Math.round(extend * 0.2f));

                        extendAmount = extend * 1.8f;
                        break;
                    case 3:
                        newStart = (currentStart - Math.round(extend * 0.75f));
                        newEnd = (currentEnd + Math.round(extend * 0.75f));

                        extendAmount = extend * 0.75f;
                        break;
                    case 4:
                        newStart = (currentStart - Math.round(extend * 0.2f));
                        newEnd = (currentEnd + Math.round(extend * 0.8f));

                        extendAmount = extend * 0.2f;
                        break;
                    case 5:
                        newStart = (currentStart - Math.round(extend * 0.8f));
                        newEnd = (currentEnd + Math.round(extend * 0.2f));

                        extendAmount = extend * 0.8f;
                        break;
                    case 6:
                        newStart = (currentStart - Math.round(extend * 0.9f));
                        newEnd = (currentEnd + Math.round(extend * 0.1f));

                        extendAmount = extend * 0.9f;
                        break;
                    case 7:
                        newStart = (currentStart - Math.round(extend * 0.1f));
                        newEnd = (currentEnd + Math.round(extend * 0.9f));

                        extendAmount = extend * 0.1f;
                        break;
                    case 8:
                        newStart = (currentStart - Math.round(extend * 0.5f));
                        newEnd = (currentEnd + Math.round(extend * 0.5f));

                        extendAmount = extend * 0.5f;
                        break;
                    case 9:
                        newStart = (currentStart - Math.round(extend * 0.2f));
                        newEnd = (currentEnd + Math.round(extend * 0.7f));

                        extendAmount = extend * 0.2f;
                        break;
                    case 10:
                        newStart = (currentStart - Math.round(extend * 0.7f));
                        newEnd = (currentEnd + Math.round(extend * 0.2f));

                        extendAmount = extend * 0.7f;
                        break;
                    case 11:
                        newStart = (currentStart - Math.round(extend * 0.6f));
                        newEnd = (currentEnd + Math.round(extend * 0.4f));

                        extendAmount = extend * 0.6f;
                        break;
                    case 12:
                        newStart = (currentStart - Math.round(extend * 0.4f));
                        newEnd = (currentEnd + Math.round(extend * 0.6f));

                        extendAmount = extend * 0.4f;
                        break;
                    case 13:
                        if (!data.isEmpty())
                        {
                            if (this.outputLogging)
                            {

                                MIRCAT_LOGGER.log(Level.INFO, "reached window 14 and initial checks have succeeded...");

                            }
                            else
                            {
                                
                                updatePredictionsTable(newStart, newEnd, currentStart, currentEnd, topAlignment.getStrand(), chromID, data);
                            }

//                        if (myStatusLabel != null) {
//
//                            EventQueue.invokeLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    try {
//
//                                        myTextLog.remove(myTextLog.getLength() - processString.length(), processString.length());
//                                        myTextLog.insertString(myTextLog.getLength(), processString,
//                                                myTextLog.getStyle(Tools.initStyles[5]));
//
//                                    } catch (BadLocationException ex) {
//                                        LOGGER.log(Level.SEVERE, null, ex);
//                                    }
//                                }
//                            });
//                        }
//                        updateOutputModules(newStart, newEnd, currentStart, currentEnd, genome_hits,
//                                strand, fastaHeader,
//                                current_hit, rsq, originalRead,
//                                current_cluster, data);
                        }
                        else
                        {
                            if (this.outputLogging)
                            {

                                MIRCAT_LOGGER.log(Level.WARNING, "reached window 14 and all checks failed so ignoring");

                            }
                            else
                            {
                                
                            }
                        }
                        return;
                }

                if (newStart < 1)
                {
                    newStart = 1;
                }
                if (newEnd < 1)
                {
                    System.out.println("new end less than 1: " + newEnd + " START: " + newStart + " Window: " + retry);
                }

                String windowSequence = genomeManager.getDNAClamped(chromID, newStart, newEnd, this.topAlignment.getStrand());
                int calcOffset = 0;
                if (this.topAlignment.getStrand().equals("+")) //calcOffset = (currentStart - newStart) - 1;
                {
                    calcOffset = (currentStart - newStart); // correct offset - shouldn't need - 1!!
                }
                else
                {
                    calcOffset = newEnd - currentEnd;
                }
                int originalLength = (currentEnd - currentStart) + 1;

                processSingleWindow(windowSequence, originalLength, calcOffset, flankingSequences,
                        currentPointer, processedHairpins,
                        topAlignment.getStrand(), newStart, newEnd, data);

            }
        }
        catch (IOException | IDDoesNotExistException | HairpinExtensionException ex)
        {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MiRNAPredictor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private synchronized void processSingleWindow(String windowSeq, int originalLength, int startOffset,
            ArrayList<String> flankingSequences, int[] currentPointer, ArrayList<String> processedHairpins,
            String strand, int newStart, int newEnd, HashMap<Float, ArrayList<String>> data) throws IOException, HairpinExtensionException
    {
        String[] modifiedStructs = getStructure(windowSeq, originalLength, startOffset);
        if ((modifiedStructs != null) && (modifiedStructs[MODIFIED_REGION].length() <= 25))
        {

            ArrayList<Integer> pairs = gatherPairs(modifiedStructs[ORIGINAL_RNAFOLD_RESULT]);

            //to make this code neater, in fact this gathering stage could be completed for all windows
            //then the if statement below processed on the entires flanking sequences datastructure
            gatherFlanking_miRNA(modifiedStructs[MODIFIED_REGION], modifiedStructs[FIRST_PART], modifiedStructs[END_PART], flankingSequences);

            if (flankingSequences.size() > 0)
            {

                while (currentPointer[0] < flankingSequences.size())
                {
                    String extendedMiRNA = flankingSequences.get(currentPointer[0]);

                    //System.out.println("extendedHairpin: " + )
                    String fullStructure = modifiedStructs[FIRST_PART] + modifiedStructs[MODIFIED_REGION] + modifiedStructs[END_PART];
                    String checkBaseString = "";
                    int seqStartPos = -1;
                    if (extendedMiRNA.contains("<") || extendedMiRNA.contains("("))
                    {

                        checkBaseString = extendedMiRNA.replaceFirst("[\\-\\.]*", "");
                        seqStartPos = fullStructure.indexOf(checkBaseString);
                    }
                    else
                    {

                        int endBracketClose = extendedMiRNA.lastIndexOf(">");
                        int endBracketOpen = extendedMiRNA.lastIndexOf(")");

                        checkBaseString = extendedMiRNA.substring(0, (Math.max(endBracketOpen, endBracketClose) + 1));
                        seqStartPos = fullStructure.indexOf(checkBaseString) + checkBaseString.length() - 1;
                    }
                    if (seqStartPos >= 0)
                    {
                        int startPairData = pairs.get(seqStartPos);

                        String fullHairpin = modifiedStructs[ORIGINAL_RNAFOLD_RESULT].substring(pairs.indexOf(startPairData), pairs.lastIndexOf(startPairData) + 1);
                        String fullHairpinMarked = fullStructure.substring(pairs.indexOf(startPairData), pairs.lastIndexOf(startPairData) + 1);
                        int offsetStart = modifiedStructs[ORIGINAL_RNAFOLD_RESULT_NO_MOD].indexOf(modifiedStructs[ORIGINAL_RNAFOLD_RESULT]);
                        //int offsetEnd
                        String hairpinSequence = windowSeq.substring(offsetStart + pairs.indexOf(startPairData), offsetStart + pairs.lastIndexOf(startPairData) + 1);
                        int analyseResult = analyse_structure(fullHairpin);

                        if (analyseResult != 2)
                        {

                            if (!processedHairpins.contains(hairpinSequence))
                            {
                                processedHairpins.add(hairpinSequence);

                                this.withrandfold = false;
                                String[] reFold_results = refold(hairpinSequence, ">" + chromID);
                                this.withrandfold = true;

                                try
                                {
                                    float mfe = StringUtils.safeFloatParse(reFold_results[RANDFOLD_MFE].trim(), Float.MIN_VALUE);
                                    float amfe = (mfe / (float) hairpinSequence.length()) * 100.0f;

                                    LOGGER.log(Level.FINE, "amfe: {0}", amfe);

                                    if (amfe <= params.getMinEnergy())
                                    {
                                        ArrayList<String> hairpinData = new ArrayList<String>();
                                        hairpinData.add(fullHairpinMarked);
                                        hairpinData.add(fullStructure);
                                        hairpinData.add(hairpinSequence);
                                        if (strand.equals("+"))
                                        {
                                            hairpinData.add(Integer.toString(newStart + (offsetStart + pairs.indexOf(startPairData))));
                                        }
                                        else
                                        {
                                            hairpinData.add(Integer.toString(newEnd - (offsetStart + pairs.indexOf(startPairData))));
                                        }
                                        hairpinData.add(Integer.toString(modifiedStructs[MODIFIED_REGION].length()));
                                        hairpinData.add(modifiedStructs[ORIGINAL_RNAFOLD_RESULT]);
                                        hairpinData.add(modifiedStructs[TRUNCATED_WINDOW_SEQ]);
                                        hairpinData.add(Float.toString(mfe));
                                        //hairpinData.add(offsetStart + pairs.indexOf(startPairData));
                                        if (strand.equals("+"))
                                        {
                                            hairpinData.add(Integer.toString(newStart + (offsetStart + pairs.lastIndexOf(startPairData) + 1)));
                                        }
                                        else
                                        {
                                            hairpinData.add(Integer.toString(newEnd - (offsetStart + pairs.lastIndexOf(startPairData) + 1)));
                                        }
                                        hairpinData.add(Integer.toString(newStart));
                                        hairpinData.add(Integer.toString(newEnd));
                                        data.put(amfe, hairpinData);
                                    }
                                    else
                                    {
                                        if (outputLogging)
                                        {
                                            MIRCAT_LOGGER.log(Level.WARNING, "AMFE was too high to allow hairpin through: {0}", amfe);
                                        }
                                    }
                                }
                                catch (NumberFormatException e)
                                {
                                    LOGGER.log(Level.FINE, "rnafold returned no result on refold");
                                }

                                String spaces = "";
                                String otherSpaces = "";
                                for (int i = 0; i < offsetStart; i++)
                                {
                                    spaces += " ";
                                }
                                for (int i = 0; i < pairs.indexOf(startPairData); i++)
                                {
                                    otherSpaces += " ";
                                }

                                StringBuilder sb = new StringBuilder();

                                sb.append("extendedMiRNA           : ").append(extendedMiRNA).append(LINE_SEPARATOR);
                                sb.append("full structure          : ").append(fullStructure).append(LINE_SEPARATOR);
                                sb.append("original result         : ").append(modifiedStructs[ORIGINAL_RNAFOLD_RESULT]).append(LINE_SEPARATOR);
                                sb.append("full structure  spaced  : ").append(spaces).append(fullStructure).append(LINE_SEPARATOR);
                                sb.append("original result spaced  : ").append(spaces).append(modifiedStructs[ORIGINAL_RNAFOLD_RESULT]).append(LINE_SEPARATOR);
                                sb.append("original result no mods : ").append(modifiedStructs[ORIGINAL_RNAFOLD_RESULT_NO_MOD]).append(LINE_SEPARATOR);
                                sb.append("hairpin  spaced         : ").append(spaces).append(otherSpaces).append(fullHairpin).append(LINE_SEPARATOR);
                                sb.append("hairpin markup spaced   : ").append(spaces).append(otherSpaces).append(fullHairpinMarked).append(LINE_SEPARATOR);

                                sb.append("truncated window seq    : ").append(modifiedStructs[TRUNCATED_WINDOW_SEQ]).append(LINE_SEPARATOR);
                                sb.append("hairpin sequence spaced : ").append(spaces).append(otherSpaces).append(hairpinSequence).append(LINE_SEPARATOR);

                                sb.append("hairpin sequence        : ").append(hairpinSequence).append(LINE_SEPARATOR);;
                                sb.append("result of analysis      : ").append(analyseResult).append(LINE_SEPARATOR);;

                                LOGGER.log(Level.FINE, sb.toString());

                                if (outputLogging)
                                {
                                    spaces = "";
                                    otherSpaces = "";
                                    for (int i = 0; i < offsetStart; i++)
                                    {
                                        spaces += " ";
                                    }
                                    for (int i = 0; i < pairs.indexOf(startPairData); i++)
                                    {
                                        otherSpaces += " ";
                                    }
                                    MIRCAT_LOGGER.log(Level.WARNING, "extendedHairpin         : {0}", extendedMiRNA);
                                    MIRCAT_LOGGER.log(Level.WARNING, "full structure          : {0}", fullStructure);
                                    MIRCAT_LOGGER.log(Level.WARNING, "original result         : {0}", modifiedStructs[ORIGINAL_RNAFOLD_RESULT]);
                                    MIRCAT_LOGGER.log(Level.WARNING, "full structure  spaced  : {0}{1}", new Object[]
                                    {
                                        spaces, fullStructure
                                    });
                                    MIRCAT_LOGGER.log(Level.WARNING, "original result spaced  : {0}{1}", new Object[]
                                    {
                                        spaces, modifiedStructs[ORIGINAL_RNAFOLD_RESULT]
                                    });
                                    MIRCAT_LOGGER.log(Level.WARNING, "original result no mods : {0}", modifiedStructs[ORIGINAL_RNAFOLD_RESULT_NO_MOD]);
                                    MIRCAT_LOGGER.log(Level.WARNING, "hairpin  spaced         : {0}{1}{2}", new Object[]
                                    {
                                        spaces, otherSpaces, fullHairpin
                                    });
                                    MIRCAT_LOGGER.log(Level.WARNING, "hairpin markup spaced   : {0}{1}{2}", new Object[]
                                    {
                                        spaces, otherSpaces, fullHairpinMarked
                                    });

                                    MIRCAT_LOGGER.log(Level.WARNING, "truncated window seq    : {0}", modifiedStructs[TRUNCATED_WINDOW_SEQ]);
                                    MIRCAT_LOGGER.log(Level.WARNING, "hairpin sequence spaced : {0}{1}{2}", new Object[]
                                    {
                                        spaces, otherSpaces, hairpinSequence
                                    });

                                    MIRCAT_LOGGER.log(Level.WARNING, "hairpin sequence        : {0}", hairpinSequence);
                                    MIRCAT_LOGGER.log(Level.WARNING, "result of analysis      : {0}", analyseResult);

                                }

//
                                //System.out.println("amfe: " + amfe + " mfe: " + mfe);
                                //else {
//                                                    System.out.println("ignoring due to min energy being too high");
//                                                }
                            }
                        }
                        else
                        {
                            if (outputLogging)
                            {
                                switch (analyseResult)
                                {
                                    case 2:
                                        MIRCAT_LOGGER.log(Level.WARNING, "the hairpin failed due to length, structure or a high percentage of unpaired bases");
                                        break;
                                    case 3:
                                        MIRCAT_LOGGER.log(Level.WARNING, "the hairpin contained complex loops. Please re-run with this option enabled");
                                        break;
                                    default:
                                        MIRCAT_LOGGER.log(Level.INFO, "Succeeded tests on hairpin");
                                }
                            }
                        }
                    }
                    else
                    {
                        LOGGER.log(Level.WARNING, "problem with string locations");

                        if (outputLogging)
                        {
                            MIRCAT_LOGGER.log(Level.WARNING, "problem with string locations. This could be a bug");
                        }
                    }
                    currentPointer[0]++;
                }

            }
            else
            {

                if (outputLogging)
                {
                    MIRCAT_LOGGER.log(Level.INFO, "No flanking sequences were created for miRNA. Ignore");
                }
            }
        }
        else
        {
            //System.out.println("window structure has failed");

            if (outputLogging)
            {
                MIRCAT_LOGGER.log(Level.INFO, "Either RNAfold returned no result or the resulting sequence to be flanked was longer than 25. Ignore");
            }
        }
    }

    //Refolds sequence
    private String[] refold(String hairpinSequence, String fastaHeader) throws IOException
    {
        String[] resultArray = new String[3];

        // Create file
        Path filePath = Paths.get(this.tempDirLocation.toString() + DIR_SEPARATOR + myThreadID + "hairpinFile.fas");

        Files.write(filePath, (">" + fastaHeader + LINE_SEPARATOR).getBytes());

        Files.write(filePath, hairpinSequence.getBytes(), StandardOpenOption.APPEND);

        if (withrandfold)
        {
            String instruction = " -d " + filePath + " 100";
            String result = myExeMan.execRandFold(instruction);
            //System.out.println( "Result: " + result );
            if (result.contains(" Didn't get expected results"))
            {
                LOGGER.log(Level.INFO, "HAIRPIN: {0}", hairpinSequence);
                LOGGER.log(Level.INFO, "HEADER: {0}", fastaHeader);
            }
            resultArray = result.split("\t");

        }
        else
        {
            String instruction = " -C " + filePath;
            String result = myExeMan.execRNAFold(hairpinSequence, "");
            //System.out.println("result: " + result);
            resultArray = result.split(" ");
            String temp1 = resultArray[1].replaceAll("\\(", "");
            String temp2 = temp1.replaceAll("\\)", "");
            resultArray[1] = temp2;
//                for(String temp : resultArray)
//                    System.out.println(temp);
//                String result = rnaFold.run_RNAFold(seq, "");
//                System.out.println("result from refold : " + result);
        }
        //Close the output stream

        return resultArray;

    }

    private synchronized int analyse_structure(String toAnalyse)
    {

        String toAnalyseNoLeading = toAnalyse.replaceFirst("\\.*", "");

        int endBracketClose = toAnalyseNoLeading.lastIndexOf(")");
        int endBracketOpen = toAnalyseNoLeading.lastIndexOf("(");

        String toAnalyseNoGaps = toAnalyseNoLeading.substring(0, (Math.max(endBracketOpen, endBracketClose) + 1));

        //System.out.println("toanalyse no gaps: " + toAnalyseNoGaps);
        int result = 1;
        //System.out.println("toAnalyseNoGaps: " + toAnalyseNoGaps);
        //System.out.println("toAnalyse: " + toAnalyse);
        //System.out.println("toAnalyseNoLeading: " + toAnalyseNoLeading);
        Pattern p1 = Pattern.compile("^[\\<\\(\\.]{30,200}.{1,40}[\\>\\)\\.]{30,200}$");
        Matcher m1 = p1.matcher(toAnalyseNoGaps);
        Pattern p2 = Pattern.compile("^[\\<\\(\\.]+\\.+[\\>\\)\\.]+");
        Matcher m2 = p2.matcher(toAnalyseNoGaps);
        if ((m2.matches()) && (toAnalyseNoGaps.length() >= params.getMinHairpinLength()))
        {
            //System.out.println("hairpin is perfect.");
            result = 1;
        }
        else
        {
            if (m1.matches() && (toAnalyseNoGaps.length() >= params.getMinHairpinLength()))
            {

                if (params.getComplexLoops())
                {

                    result = 3;
                }
                else
                {
                    result = 2;
                }

                //System.out.println("reg ex has passed + " + result);
            }
            else
            {
                if (toAnalyse.length() < params.getMinHairpinLength())
                {
                    //System.out.println("hairpin is too short so failing");
                    result = 2;
                }
                else
                {
                    //System.out.println("none of the above so failing");
                    result = 2;
                }
            }
        }

        int bulge = countOccurrences(toAnalyseNoGaps, '.', '-');

        //int amountOfBrackets = countOccurrences(toAnalyseNoGaps, '(', ')');
        float no = ((float) params.getMaxUnpaired() / 100.0f) * (float) toAnalyseNoGaps.length();

        if (bulge > no)
        {
            //System.out.println("bulge code causing it to fail");
            result = 2;
        }

        return result;
    }

    private synchronized int countOccurrences(String miRNA, char bracket, char option)
    {
        int count = 0;
        for (int i = 0; i < miRNA.length(); i++)
        {
            if (!Character.isDigit(option))
            {
                if (miRNA.charAt(i) == bracket)
                {
                    count++;
                }
            }
            else
            {
                if ((miRNA.charAt(i) == bracket) || (miRNA.charAt(i) == option))
                {
                    count++;
                }

            }
        }
        return count;
    }

    private synchronized ArrayList<Integer> gatherPairs(String originalDotBracket)
    {
        //originalDotBracket = "(((((((....(((..((.(((.......(((.(((....)))..)))(.....).......))).))..))).)))))))....................(((((.(((........))).((((((((.((((((......))))))...)))))))).....)))))";
        //System.out.println("Result D: " + originalDotBracket);
        ArrayList<Integer> pairs = new ArrayList<>();

        LinkedList<Integer> structureStarts = new LinkedList<>();
        int currentOpen = 0, currentClose = 0;
        char openBracket = '(';
        char closeBracket = ')';
        char dot = '.';
        for (int i = 0; i < originalDotBracket.length(); i++)
        {
            if (originalDotBracket.charAt(i) == dot)
            {
                pairs.add(-1);
            }
            if (originalDotBracket.charAt(i) == openBracket)
            {
                structureStarts.addFirst(currentOpen);
                pairs.add(currentOpen);
                currentOpen++;

            }
            else
            {
                if (originalDotBracket.charAt(i) == closeBracket)
                {
                    try
                    {
                        currentClose = structureStarts.removeFirst();
                    }
                    catch (java.util.NoSuchElementException e)
                    {
                        MIRCAT_LOGGER.log(Level.SEVERE, "Error read:{0}", this.topSequenceHit.getRNA_Sequence());
                        MIRCAT_LOGGER.log(Level.SEVERE, "Error struct:{0}", originalDotBracket);
                        throw e;
                    }
                    pairs.add(currentClose);
                }
            }
        }
        return pairs;
    }

    private synchronized void gatherFlanking_miRNA(String miRNA, String firstPart, String endPart, ArrayList<String> extendedSeq) throws HairpinExtensionException
    {

        if (miRNA.length() == 25)
        {
            int[] direction
                    =
                    {
                        0
                    };
            if (checkValid_miRNA(miRNA, direction, false))
            {
                String extendedHairpin = "";
                if (direction[0] < 0)
                {
                    //System.out.println("going left");

                    extendedHairpin = extendSeq(miRNA, firstPart, -1);
                    //System.out.println("extended hairpin length 25 LEFT : " + extendedHairpin);
                }
                else
                {
                    if (direction[0] > 0)
                    {
                        //System.out.println("going right");
                        extendedHairpin = extendSeq(miRNA, endPart, 1);
                        //System.out.println("extended hairpin length 25 RIGHT : " + extendedHairpin);
                    }
                }

                if (!extendedSeq.contains(extendedHairpin))
                {
                    if (!extendedSeq.add(extendedHairpin))
                    {
                        LOGGER.log(Level.WARNING, "problem adding to extended miRNA list. Memory failure?");
                    }
                }
            }
        }
        else
        {
            int amountToAdd = 25 - miRNA.length() + 1;
            int flankAmount = 25 - miRNA.length();

            //flankA
            for (int i = 0;
                    (i < amountToAdd) && (i <= firstPart.length()) && (i <= endPart.length());
                    i++)
            {
                int usingFlankAmount = flankAmount - i;
                int leftSentinal = 0;
                int rightSentinal = i;
                int offset = 0;

                if (usingFlankAmount > firstPart.length())
                {
                    offset = usingFlankAmount - firstPart.length();
                    leftSentinal = firstPart.length() - usingFlankAmount + offset;

                }
                else
                {
                    if (usingFlankAmount > endPart.length())
                    {
                        offset = usingFlankAmount - endPart.length();
                        leftSentinal = firstPart.length() - usingFlankAmount - offset;

                    }
                    else
                    {
                        leftSentinal = firstPart.length() - usingFlankAmount;
                    }
                }

                try
                {
                    String addingStart = firstPart.substring(leftSentinal);
                    String addingEnd = endPart.substring(0, rightSentinal);
                    String newPotential_miRNA = addingStart + miRNA + addingEnd;

                    int[] direction
                            =
                            {
                                0
                            };
                    if (checkValid_miRNA(newPotential_miRNA, direction, false))
                    {
                        String extendedHairpin = "";
                        if (direction[0] < 0)
                        {
                            int subStr = firstPart.length() - addingStart.length();
                            extendedHairpin = extendSeq(newPotential_miRNA, firstPart.substring(0, subStr), -1);
                        }
                        else
                        {
                            if (direction[0] > 0)
                            {

                                extendedHairpin = extendSeq(newPotential_miRNA, endPart.substring(addingEnd.length(), endPart.length()), 1);

                            }
                            else
                            {
                                throw new HairpinExtensionException("Hairpin extension error, no hairpin created as neither left or right was available");
                            }
                        }

                        if (!extendedSeq.contains(extendedHairpin))
                        {
                            if (!extendedSeq.add(extendedHairpin))
                            {
                                LOGGER.log(Level.WARNING, "problem adding to extended miRNA list. Memory failure?");
                            }
                        }

                    }
                }
                catch (java.lang.StringIndexOutOfBoundsException e)
                {
                    // Should we do something here?
                }
            }
        }

    }

    private synchronized String extendSeq(String potential_miRNA, String extendSeq, int direction)
    {
        String extendedSequence = "";
        String extendedSequenceTemp = "";
        //String extendedSequenceTemp2 = "";
        //System.out.println("potential: " + potential_miRNA + "extendSeq: " + extendSeq);
        if (direction < 0)
        {
            int position = extendSeq.lastIndexOf(")");
            //System.out.println("found the first ) at: " + position);

            if (position > 0)
            {
                //System.out.println("replacing");
                String subSeq = extendSeq.substring(position + 1, extendSeq.length());
                extendedSequenceTemp = subSeq + potential_miRNA;

            }
            else
            {
                extendedSequenceTemp = extendSeq + potential_miRNA;
            }

            extendedSequence = extendedSequenceTemp;

//            int endBracketClose = extendedSequenceTemp2.lastIndexOf(")");
//            int endBracketOpen = extendedSequenceTemp2.lastIndexOf("(");
            //System.out.println("index of close at end: " + endBracketClose + " index of open at end " + endBracketOpen);
            //extendedSequence = extendedSequenceTemp2.substring(0, (Math.max(endBracketOpen, endBracketClose)+1));
        }
        else
        {
            int position = extendSeq.indexOf("(");
            //System.out.println("found the first ( at: " + position);

            if (position > 0)
            {
                String subSeq = extendSeq.substring(0, position);
                int endBracketClose = subSeq.lastIndexOf(")");
                int endBracketOpen = subSeq.lastIndexOf("(");
                //System.out.println("index of close at end: " + endBracketClose + " index of open at end " + endBracketOpen);
                //extendedSequence = extendedSequenceTemp2.substring(0, (Math.max(endBracketOpen, endBracketClose)+1));
                extendedSequenceTemp = potential_miRNA + subSeq.substring(0, (Math.max(endBracketOpen, endBracketClose) + 1));
            }
            else
            {
                if (position == 0)
                {
                    extendedSequenceTemp = potential_miRNA;
                }
                else
                {
                    extendedSequenceTemp = potential_miRNA + extendSeq;
                }
            }

            extendedSequence = extendedSequenceTemp;

        }

        //System.out.println("returned: " + extendedSequence);
        return extendedSequence;
    }

    private String[] getStructure(String windowSeq, int originalLength, int startOffset) throws IOException
    {

        String[] modifiedDotBracketArray = new String[6];
        String result = myExeMan.execRNAFold(windowSeq, "");

        if (result == null)
        {
            if (outputLogging)
            {

                MIRCAT_LOGGER.log(Level.WARNING, "RNAfold produced no structure so ignoring");

            }
            return null;
        }

        String[] results = result.split(" ");

        if ((results.length > 0) && (results[0].contains("(") || results[0].contains(")")))
        {

            int firstBracketOpen = results[0].indexOf("(");
            int firstBracketClose = results[0].indexOf(")");

            int lastBracketOpen = results[0].lastIndexOf("(");
            int lastBracketClose = results[0].lastIndexOf(")");

            modifiedDotBracketArray[TRUNCATED_WINDOW_SEQ] = windowSeq.substring(Math.min(firstBracketOpen, firstBracketClose), Math.max(lastBracketOpen, lastBracketClose) + 1);

            String newSeq = "";
            String startSeq = "";
            String endSeq = "";

            try
            {
                // cut out structure that aligns with mature sequence
                newSeq = results[0].substring(startOffset, (startOffset + (originalLength)));

                // cut out the beginning flanking sequence
                startSeq = results[0].substring(0, startOffset);

                // cut out the end flanking sequence
                //endSeq = results[0].substring( ( startOffset + ( originalLength - 1 ) ), results[0].length() );
                endSeq = results[0].substring((startOffset + (originalLength)), results[0].length());

            }
            catch (StringIndexOutOfBoundsException e)
            {
                MIRCAT_LOGGER.log(Level.SEVERE, "Window: {0}", windowSeq);
                MIRCAT_LOGGER.log(Level.SEVERE, "Result O: {0}", result);
                return null;
            }

            String first = newSeq.replace('(', '<');
            String second = first.replace(')', '>');
            String third = second.replace('.', '-');

            String startSeqNoLeadDots = startSeq.replaceFirst("\\.*", "");
            //System.out.println("sorted first string: " + startSeqNoLeadDots);
            modifiedDotBracketArray[FIRST_PART] = startSeqNoLeadDots;

            modifiedDotBracketArray[MODIFIED_REGION] = third;

            int endBracketClose = endSeq.lastIndexOf(")");
            int endBracketOpen = endSeq.lastIndexOf("(");
            String endSeqNoTrailDots = endSeq.substring(0, (Math.max(endBracketOpen, endBracketClose) + 1));
            modifiedDotBracketArray[END_PART] = endSeqNoTrailDots;

            modifiedDotBracketArray[ORIGINAL_RNAFOLD_RESULT] = startSeqNoLeadDots + newSeq + endSeqNoTrailDots;
            modifiedDotBracketArray[ORIGINAL_RNAFOLD_RESULT_NO_MOD] = results[0];

        }
        else//no structure was found
        {

            if (outputLogging)
            {

                MIRCAT_LOGGER.log(Level.WARNING, "RNAfold produced no structure so ignoring");

            }
            return null;
        }

        return modifiedDotBracketArray;
        //System.out.println(result);
    }

    private synchronized boolean checkValid_miRNA(String toCheck, int[] direction, boolean mirSTAR)
    {
        //toCheck = "<..<(<<.--<<.";
        //System.out.println("checking valid sequence: " + toCheck);
        boolean findOpenRound = toCheck.contains("(");
        boolean findOpenAngle = toCheck.contains("<");
        boolean findCloseRound = toCheck.contains(")");
        boolean findCloseAngle = toCheck.contains(">");
        if ((findOpenRound || findOpenAngle) && (findCloseRound || findCloseAngle))
        {

            //System.out.println("found open and close");
            if (outputLogging)
            {

                if (!mirSTAR)
                {
                    MIRCAT_LOGGER.log(Level.WARNING, "Found open and close bracket in miRNA so failed: {0}", toCheck);
                }
                else
                {
                    MIRCAT_LOGGER.log(Level.WARNING, "Found open and close bracket in miRNA* so failed: {0}", toCheck);
                }

            }
            return false;
        }

        int consectutiveGaps = 0;
        for (char checking : toCheck.toCharArray())
        {
            if ((checking == '.') || (checking == '-'))
            {
                consectutiveGaps++;
            }
            else
            {
                consectutiveGaps = 0;
            }
            if (consectutiveGaps > params.getMaxGaps())
            {
                //System.out.println("too many gaps");
                if (outputLogging)
                {
                    if (!mirSTAR)
                    {
                        MIRCAT_LOGGER.log(Level.WARNING, "Found too many consecutive unpaired bases in miRNA so failed: {0}", toCheck);
                    }
                    else
                    {
                        MIRCAT_LOGGER.log(Level.WARNING, "Found too many consecutive unpaired bases in miRNA* so failed: {0}", toCheck);
                    }
                }
                return false;
            }
        }
        int howMany = 0;
        if ((findOpenRound) || (findOpenAngle))
        {
            direction[0] = -1;
            int howManyRound = countOccurrences(toCheck, '(', '0');
            int howManyAngle = countOccurrences(toCheck, '<', '0');
            howMany = howManyRound + howManyAngle;
        }
        else
        {
            if ((findCloseRound) || (findCloseAngle))
            {
                direction[0] = 1;
                int howManyRound = countOccurrences(toCheck, ')', '0');
                int howManyAngle = countOccurrences(toCheck, '>', '0');
                howMany = howManyRound + howManyAngle;
            }
        }

        int min_paired_to_check = params.getMinPaired();
        if (mirSTAR)
        {
            min_paired_to_check -= 2;
        }
        if (howMany < min_paired_to_check)
        {
            //System.out.println("how many too low: " + howMany);
            if (outputLogging)
            {

                if (mirSTAR)
                {
                    MIRCAT_LOGGER.log(Level.WARNING, "Found too few paired bases in miRNA so failed: {0}", toCheck);
                }
                else
                {
                    MIRCAT_LOGGER.log(Level.WARNING, "Found too few paired bases in miRNA* so failed: {0}", toCheck);
                }

            }
            return false;
        }
        //System.out.println("succeeded tests so far. direction: " + direction[0]);
        return true;
    }

    private void updatePredictionsTable(int newStart, int newEnd, int currentStart, int currentEnd,
            String strand,
            String chromoID,
            HashMap<Float, ArrayList<String>> data) throws IOException
    {
//        if(currentStart == 78932 && currentEnd == 78952)
//        {
//            System.out.println("");
//        }
//        if(this.topSequenceHit.getRNA_Sequence().equals("TCGGACCAGGCTTCATCCCCC"))
//        {
//            System.out.println("");
//        }

        ArrayList<String> bestHit = new ArrayList<>();
        float currentMinEnergy = Float.MAX_VALUE;
        for (Entry<Float, ArrayList<String>> bestHits : data.entrySet())
        {
            if (bestHits.getKey() < currentMinEnergy)
            {
                bestHit = bestHits.getValue();
                currentMinEnergy = bestHits.getKey();
            }
        }

        ArrayList<Integer> pairs = gatherPairs(bestHit.get(5));
        ArrayList<String> mirSTAR_Sequences = new ArrayList<String>();
        boolean[] any_miRNA_pass
                =
                {
                    false, false
                };

        ArrayList<String> mirStarList = find_mir_star(bestHit.get(0), bestHit.get(1), bestHit.get(2),
                newStart, newEnd,
                pairs, Integer.parseInt(bestHit.get(4)),
                bestHit.get(6),
                mirSTAR_Sequences, any_miRNA_pass);
        
        if (any_miRNA_pass[0])
        {

            int hairpinStartIndex = 0;
            int hairpinEndIndex = 0;

            if (strand.equals("+"))
            {
                hairpinStartIndex = Integer.parseInt(bestHit.get(3));
                hairpinEndIndex = Integer.parseInt(bestHit.get(8)) - 1;
            }
            else
            {
                hairpinStartIndex = Integer.parseInt(bestHit.get(8));
                hairpinEndIndex = Integer.parseInt(bestHit.get(3)) - 1;
            }

            float totalHitsInHairpin = 0;
            float totalContainedIn_miRNA = 0;
            Set<Aligned_Sequences_Entity> sequences = locus.getSequences();
            for (Aligned_Sequences_Entity current_locus_seq : sequences)
            {
                int hitStartPos = current_locus_seq.getStart();
                int hitEndPos = current_locus_seq.getEnd();
                //if( (hitStartPos >= hairpinStartIndex-200) && (hitEndPos <= hairpinEndIndex+200))
                {

                    double abund = current_locus_seq.getAligned_seqeunce().getTotalCount();
                    totalHitsInHairpin += abund;
                    //check hit against miRNA
                    if ((hitStartPos >= currentStart - 2) && (hitEndPos <= currentEnd + 2))
                    {
                        totalContainedIn_miRNA += abund;
                        continue;
                    }

                    //check mirStars
                    searchMirSTAR:
                    for (String entry : mirSTAR_Sequences)
                    {
                        int savedStart, savedEnd;
                        if (strand.equals("+"))
                        {
                            savedStart = hairpinEndIndex - bestHit.get(2).indexOf(entry) - 21;
                            savedEnd = hairpinEndIndex - bestHit.get(2).indexOf(entry);
                        }
                        else
                        {
                            savedStart = hairpinStartIndex + bestHit.get(2).indexOf(entry);
                            savedEnd = hairpinStartIndex + bestHit.get(2).indexOf(entry) + 21;
                        }
                        if ((hitStartPos >= savedStart - 2) && (hitEndPos <= savedEnd + 2))
                        {
                            totalContainedIn_miRNA += abund;
                            break searchMirSTAR;
                        }
                    }
                }
            }
            float percentageOverlap = (totalContainedIn_miRNA / totalHitsInHairpin) * 100.0f;

            LOGGER.log(Level.FINE, "total found: {0} totalIn miRNA{1} percentage: {2}", new Object[]
            {
                totalHitsInHairpin, totalContainedIn_miRNA, percentageOverlap
            });

            if (percentageOverlap >= params.getMaxOverlapPercentage())
            {
//                if (myStatusLabel != null)
//                {
//                    try
//                    {
//                        myTextLog.remove(myTextLog.getLength() - processString.length(), processString.length());
//                        myTextLog.insertString(myTextLog.getLength(), processString,
//                                myTextLog.getStyle(Tools.initStyles[7]));
//                    }
//                    catch (BadLocationException ex)
//                    {
//                        LOGGER.log(Level.SEVERE, null, ex);
//                    }
//
//                    //myProgBar.setMaximum(14);
//                }
                this.withrandfold = true;
                String[] reFold_results = refold(bestHit.get(2), chromID);
                this.withrandfold = false;
                float mfe = Float.parseFloat(reFold_results[RANDFOLD_MFE].trim());
                float amfe = (mfe / (float) bestHit.get(2).length()) * 100.0f;
                float p_val = Float.parseFloat(reFold_results[RANDFOLD_P_Val].trim());
                if ((amfe < params.getMinEnergy()) && p_val < params.getPVal())
                {

                    String finalMarkedUpHairpin = "";
                    String mostAbundMirStar = "";

                    float hairpinGC = ((((float) SequenceUtils.DNA.getGCCount(bestHit.get(2)) / (float) bestHit.get(2).length())) * 100.0f);

                    if (fileMode)
                    {
                        LOGGER.log(Level.FINE, "updating file output");
                 

                        FileWriter outFile = new FileWriter(outputDir + DIR_SEPARATOR + "output.csv", true);

                        PrintWriter outputCSV = new PrintWriter(outFile);

                        FileWriter outHFile = new FileWriter(outputDir + DIR_SEPARATOR + "miRNA_hairpins.txt", true);

                        PrintWriter outputTXT = new PrintWriter(outHFile);

                        FileWriter outmiRNAFile = new FileWriter(outputDir + DIR_SEPARATOR + "miRNA.fa", true);

                        PrintWriter outputmiRNA = new PrintWriter(outmiRNAFile);
//
//                        // chris added code to check entry for mirbase 
                        String miRNA_ID = "";
//                        if (myMirBaseData != null && myMirBaseData.containsKey(current_hit.getSequence()))
//                        {
//                            HashMap<String, MirBaseHeader> tempMap = new HashMap<String, MirBaseHeader>();
//                            ArrayList<MirBaseHeader> mirBaseEntries = myMirBaseData.get(current_hit.getSequence());
//                            for (MirBaseHeader header : mirBaseEntries)
//                            {
//                                tempMap.put(header.getMircode().getFamily(), header);
//                            }
//                            for (String family : tempMap.keySet())
//                            {
//                                miRNA_ID += family;
//                            }
//                        }
//                        else
//                        {
//                            miRNA_ID += "N/A";
//                        }
//
                        if (!any_miRNA_pass[1])
                        {
                            finalMarkedUpHairpin = bestHit.get(0);

                            outputCSV.println(chromoID + "," + topAlignment.getStart() + "," + topAlignment.getEnd() + ","
                                    + topAlignment.getStrand()+ "," + topAlignment.getAligned_seqeunce().getTotalCount()+ "," + topAlignment.getRna_seq() 
                                    + "," + topAlignment.getRna_seq().length() + ","
                                    + topSequenceHit.getGenomeHitCount() + "," + finalMarkedUpHairpin.length() + "," + hairpinGC + "," + mfe + "," + amfe
                                    + ",NO," + miRNA_ID + "," + p_val);
                            outputCSV.close();
                            outFile.close();
                            //outputCSV[0].flush();
                        }
                        else
                        {
                            int currentMaxAbund = 0;
                            String mirStars = "";
                            for (String mirStar : mirStarList)
                            {
                                String[] mirStarSeqAndAbund = mirStar.split("\\(");

                                String tempAbun = mirStarSeqAndAbund[1].replace(")", "");
                                int currentAbund = Integer.parseInt(tempAbun.trim());
                                if (currentAbund > currentMaxAbund)
                                {
                                    currentMaxAbund = currentAbund;
                                    mostAbundMirStar = mirStarSeqAndAbund[0];
                                }
                                mirStars += mirStar + " ";
                            }

                            finalMarkedUpHairpin = markup_mirstar(bestHit.get(2), bestHit.get(0), mostAbundMirStar);

                            outputCSV.println(chromoID + "," + topAlignment.getStart() + "," + topAlignment.getEnd() + ","
                                    + topAlignment.getStrand()+ "," + topSequenceHit.getAbundance()+ "," + topAlignment.getRna_seq() 
                                    + "," + topAlignment.getRna_seq().length() + ","
                                    + topSequenceHit.getGenomeHitCount() + "," + finalMarkedUpHairpin.length() + "," + hairpinGC + "," + mfe + "," + amfe
                                    + "," + mirStars + "," + miRNA_ID + "," + p_val);

                            outputCSV.close();

                        }
                        outputmiRNA.println(">" + topSequenceHit.getRNA_Sequence() + "(" + topSequenceHit.getAbundance() + ")");
                        outputmiRNA.println(topSequenceHit.getRNA_Sequence());
                        outputmiRNA.close();
                        outmiRNAFile.close();

                        outputTXT.println(">" + topSequenceHit.getRNA_Sequence() + "_" + chromoID + "/" + topAlignment.getStart() + "-" + topAlignment.getEnd());
                        outputTXT.println(bestHit.get(2));
                        outputTXT.println(finalMarkedUpHairpin);

                        outputTXT.println();
                        outHFile.close();
                        outputTXT.close();

                    }
                    else
                    {
           
                        //Session openSession = this.predictionDAO.getSessionFactory().openSession();
                        int mature_offset_sIndex = bestHit.get(2).indexOf(topSequenceHit.getRNA_Sequence());
                        int precursor_sIndex = this.topAlignment.getId().getStart() - mature_offset_sIndex;
                        int precursor_eIndex = precursor_sIndex + bestHit.get(2).length() - 1;
                        
                        //create the pre-cursor
                        Precursor_Entity prec_e = new Precursor_Entity(bestHit.get(2), bestHit.get(0), this.chromID, precursor_sIndex, precursor_eIndex, topAlignment.getStrand(), StringUtils.safeDoubleParse(bestHit.get(7), 0));

                        //this.precursorServ.save(prec_e);                        
                        if (!any_miRNA_pass[1])
                        {
                            //finalMarkedUpHairpin = bestHit.get(0);
                           //Prediction_Entity pred_e = new Prediction_Entity("miRCat", prec_e, this.topAlignment, Aligned_Sequences_Entity.NO_ALIGNMENT);
                            Prediction_Entity pred_e = new Prediction_Entity("miRCat", prec_e, topAlignment, Aligned_Sequences_Entity.NO_ALIGNMENT);

                            this.predictionServ.save(pred_e);

                            //pred_e.setMature(topAlignment);


                        }
                        else
                        {
                            int currentMaxAbund = 0;

                            Criteria miRStarCrit = session.createCriteria(Aligned_Sequences_Entity.class);
                                   // .setResultTransformer(Transformers.aliasToBean(Aligned_Sequences_Entity.class));

                            for (String mirStar : mirStarList)
                            {
                                Criterion crit = Restrictions.eq("rna_sequence", mirStar);
                                miRStarCrit.add(crit);
                            }
                            
                            List<Aligned_Sequences_Entity> list = miRStarCrit.list();
                            for (Aligned_Sequences_Entity miRStarAlignment : list)
                            {
                                Prediction_Entity pred_e = new Prediction_Entity("miRCat", prec_e, this.topAlignment, miRStarAlignment);
                                this.predictionServ.save(pred_e);


                            }

                            

                            //finalMarkedUpHairpin = markup_mirstar(bestHit.get(2), bestHit.get(0), mostAbundMirStar);
                        }
//
//                        session.flush();
//                        session.close();

//                        
                        if (batchFlusher == 500)
                        {
                            session.flush();
                            session.clear();
                            batchFlusher = 0;
                        }
                        batchFlusher++;
//                        openSession.flush();
//                        openSession.clear();
//                        List<Prediction_Entity> list = openSession.createCriteria(Prediction_Entity.class).list();
//                        for(Prediction_Entity p : list)
//                        {
//                            System.out.println(p.getMature().getRna_seq());
//                        }
                        //openSession.close();
                    }
                }
                else
                {
                    if (outputLogging)
                    {
                        MIRCAT_LOGGER.log(Level.WARNING, "Failed due randfold AMFE: {0} being too high", amfe);
                    }
                }
            }
            else
            {
                if (outputLogging)
                {
                    MIRCAT_LOGGER.log(Level.WARNING, "Failed due percentage of overlapping sequences in cluster: {0} being too low", 
                            percentageOverlap);
                }
            }
        }
        else
        {
            if (outputLogging)
            {
                MIRCAT_LOGGER.log(Level.WARNING, "Failed because all miRNA* regions failed the validity test ");
            }
        }

    }

    private synchronized ArrayList<String> find_mir_star(String hairpinMarked, String fullStructure, String hairpinSequence,
            int newStart, int newEnd,
            ArrayList<Integer> pairs, int miRNALength, String windowSeq,
            ArrayList<String> sequences, boolean[] any_miRNA_pass) throws IOException
    {

        ArrayList<String> results = new ArrayList<>();

        int newStartPointer = 0, newEndPointer = 0;
        int miRNA_location = 0;
        int miRNA_locationEnd = 0;
        //offsetStart + pairs.indexOf(startPairData), offsetStart + pairs.lastIndexOf(startPairData)+1

        if (hairpinMarked.contains("<"))
        {
            miRNA_location = fullStructure.indexOf("<");
            miRNA_locationEnd = miRNA_location + miRNALength;
            int firstPair = pairs.get(miRNA_location);
            int secondPair = pairs.lastIndexOf(firstPair);

            int hairpinStartIndex = secondPair - miRNALength;

            newEndPointer = secondPair;
            newStartPointer = hairpinStartIndex;

        }
        else
        {
            if (hairpinMarked.contains(">"))
            {
                miRNA_location = fullStructure.lastIndexOf(">");
                miRNA_locationEnd = miRNA_location - miRNALength;
                int firstPair = pairs.get(miRNA_location);
                int secondPair = pairs.indexOf(firstPair);

                newEndPointer = secondPair + miRNALength;
                newStartPointer = secondPair;

            }
            else
            {

                System.out.println("Seems as if the data entered into find_mir_star is incorrect...");
                return null;
            }
        }

        ArrayList<String> seqToCheckWithPatman = new ArrayList<String>();
        ArrayList<String> mirSTAR_for_validation = new ArrayList<String>();
        for (int i = 0;
                (i < 4) && (newEndPointer + i < fullStructure.length()) && (newStartPointer - i >= 0);
                i++)
        {

            int startBack = newStartPointer - i;
            int endBack = newEndPointer - i;
            int startForward = newStartPointer + i;
            int endForward = newEndPointer + i;

            try
            {
                String addFirst = windowSeq.substring(startBack, endBack);
                if (!seqToCheckWithPatman.contains(addFirst))
                {
                    seqToCheckWithPatman.add(addFirst);
                }
                addFirst = windowSeq.substring(startForward, endForward);
                if (!seqToCheckWithPatman.contains(addFirst))
                {
                    seqToCheckWithPatman.add(addFirst);
                }

            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String temp_potential_miRSTAR_back = fullStructure.substring(startBack, endBack);
                String temp_potential_miRSTAR_forward = fullStructure.substring(startForward, endForward);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_back))
                {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_back);
                }
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_forward))
                {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_forward);
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            //seqToCheckWithPatmanForward[0] = windowSeq.substring(startForward, endForward);
            try
            {
                String toAdd = windowSeq.substring(startBack - 1, endBack);
                if (!seqToCheckWithPatman.contains(toAdd))
                {
                    seqToCheckWithPatman.add(toAdd);
                }

            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String temp_potential_miRSTAR_back = fullStructure.substring(startBack - 1, endBack);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_back))
                {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_back);
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String toAdd = windowSeq.substring(startForward - 1, endForward);
                if (!seqToCheckWithPatman.contains(toAdd))
                {
                    seqToCheckWithPatman.add(toAdd);
                }

            }
            catch (StringIndexOutOfBoundsException e)
            {
            }

            try
            {
                String temp_potential_miRSTAR_forward = fullStructure.substring(startForward - 1, endForward);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_forward))
                {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_forward);
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String toAdd = windowSeq.substring(startBack + 1, endBack);
                if (!seqToCheckWithPatman.contains(toAdd))
                {
                    seqToCheckWithPatman.add(toAdd);
                }

            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String temp_potential_miRSTAR_back = fullStructure.substring(startBack + 1, endBack);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_back))
                {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_back);
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String toAdd = windowSeq.substring(startForward + 1, endForward);
                if (!seqToCheckWithPatman.contains(toAdd))
                {
                    seqToCheckWithPatman.add(toAdd);
                }

            }
            catch (StringIndexOutOfBoundsException e)
            {
            }

            try
            {
                String temp_potential_miRSTAR_forward = fullStructure.substring(startForward + 1, endForward);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_forward))
                {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_forward);
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String toAdd = windowSeq.substring(startBack, endBack - 1);
                if (!seqToCheckWithPatman.contains(toAdd))
                {
                    seqToCheckWithPatman.add(toAdd);
                }

            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String temp_potential_miRSTAR_back = fullStructure.substring(startBack, endBack - 1);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_back))
                {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_back);
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String toAdd = windowSeq.substring(startForward, endForward - 1);
                if (!seqToCheckWithPatman.contains(toAdd))
                {
                    seqToCheckWithPatman.add(toAdd);
                }

            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String temp_potential_miRSTAR_forward = fullStructure.substring(startForward, endForward - 1);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_forward))
                {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_forward);
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
            }

            try
            {
                String toAdd = windowSeq.substring(startBack, endBack + 1);
                if (!seqToCheckWithPatman.contains(toAdd))
                {
                    seqToCheckWithPatman.add(toAdd);
                }

            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String temp_potential_miRSTAR_back = fullStructure.substring(startBack, endBack + 1);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_back))
                {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_back);
                }

            }
            catch (StringIndexOutOfBoundsException e)
            {
            }
            try
            {
                String toAdd = windowSeq.substring(startForward, endForward + 1);
                if (!seqToCheckWithPatman.contains(toAdd))
                {
                    seqToCheckWithPatman.add(toAdd);
                }

            }
            catch (StringIndexOutOfBoundsException e)
            {
            }

            try
            {
                String temp_potential_miRSTAR_forward = fullStructure.substring(startForward, endForward + 1);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_forward))
                {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_forward);
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
            }

        }
        boolean[] validation_results = validate_mir_star(seqToCheckWithPatman, mirSTAR_for_validation, results);
        if (validation_results[0])
        {
            any_miRNA_pass[0] = true;
            for (String sequence : seqToCheckWithPatman)
            {
                if (!sequences.contains(sequence))
                {
                    sequences.add(sequence);
                }
            }
            if (validation_results[1])
            {
                any_miRNA_pass[1] = true;

            }

        }
        return results;

    }

    private synchronized boolean[] validate_mir_star(ArrayList<String> sequences, ArrayList<String> miRNA_DB, 
            ArrayList<String> readBacks) throws IOException
    {
        boolean[] result
                =
                {
                    false, false
                };

        int[] direction
                =
                {
                    0
                };
        //System.out.println("checking mirSTARS: " + miRNA_DB.size());
        for (String miRNASTAR : miRNA_DB)
        {
            if (checkValid_miRNA(miRNASTAR, direction, true))
            {
                //System.out.println("valid continue");
                result[0] = true;
            }
        }
        int[] total
                =
                {
                    0
                };
        for (String sequence : sequences)
        {

            Aligned_Sequences_Entity locus_seq = checkLocusContains(sequence);

            if (locus_seq != null)
            {

                total[0] += locus_seq.getAligned_seqeunce().getTotalCount();
                Map<String, Sequence_Entity> sequenceRelationships = locus_seq.getAligned_seqeunce().getSequenceRelationships();

                String toAdd = sequence + "(" + sequenceRelationships.get(filename).getAbundance() + ")";
                readBacks.add(toAdd);
            }
        }

        if (total[0] > 0)
        {
            result[1] = true;
        }

        return result;

    }

    private Aligned_Sequences_Entity checkLocusContains(String seq)
    {
        for (Aligned_Sequences_Entity ent : locus.getSequences())
        {
            if (ent.getRna_seq().equals(seq))
            {
                return ent;
            }
        }
        return null;
    }
    
    private String markup_mirstar(String hairpinSequence, String hairpin, String mirStarSequence) 
    {
        String newHairpin = hairpin;

        //System.out.println("original: " + newHairpin + " hairpin: " + hairpin + " mirSTARSequence: " + mirStarSequence);
        int location = hairpinSequence.indexOf(mirStarSequence);
        if (location >= 0) {
            String firstChunk = hairpin.substring(0, location);
            String secondChunk = hairpin.substring(location, location + mirStarSequence.length());
            String thirdChunk = hairpin.substring(location + mirStarSequence.length(), hairpin.length());

            String firstReplace = "";
            if (secondChunk.contains("(")) {
                firstReplace = secondChunk.replace('(', '{');
            } else {
                firstReplace = secondChunk.replace(')', '}');
            }

            String markedUpMiRStar = firstReplace.replace('.', '=');

            //System.out.println("first chunk: " + firstChunk + " Second chunk: " + markedUpMiRStar + " thirdChunk: " + thirdChunk);
            newHairpin = firstChunk + markedUpMiRStar + thirdChunk;
            //System.out.println("final hairpin: " + newHairpin);
        }

        return newHairpin;
    }


}
