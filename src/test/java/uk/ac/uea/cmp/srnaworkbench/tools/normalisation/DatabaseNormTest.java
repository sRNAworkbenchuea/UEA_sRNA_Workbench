/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.normalisation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import uk.ac.uea.cmp.srnaworkbench.data.count.CountMatrix;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationServiceLayer;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.WF.NormalisationWorkflowServiceModule;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author kikum
 */
public class DatabaseNormTest {
    
    private static final DatabaseWorkflowModule.Datasets testDataInput = DatabaseWorkflowModule.Datasets.DATA_ATH_UNIT_TEST;
    NormalisationServiceLayer normService = (NormalisationServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("NormalisationService");
    SequenceServiceImpl seqService = (SequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("SequenceService");
    private final static String NORM_PATH = Tools.PROJECT_DIR + "/../" +DIR_SEPARATOR + "src" + DIR_SEPARATOR + "test" + DIR_SEPARATOR + "data" + DIR_SEPARATOR + "norm" + DIR_SEPARATOR;
    private final static String DIFF_OUTPUT = "differences.txt";
    private static final String COUNT_FILE = "counts.csv";

    
    public DatabaseNormTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        DatabaseWorkflowModule.getInstance().setDebugMode(true);
        DatabaseWorkflowModule.setTestData(testDataInput);
        DatabaseWorkflowModule.getInstance().run();
    }
    
    @AfterClass
    public static void tearDownClass() {
        
    }
    
    @Before
    public void setUp() {

    }
    
    @After
    public void tearDown() {
    }
    
    public void testNormalisation(NormalisationType norm) throws Exception
    {
        List<String> fileids = DatabaseWorkflowModule.getFileIDs();
        AnnotationSet mapped = AnnotationSet.getMappedSet();

        NormalisationWorkflowServiceModule normaliser = new NormalisationWorkflowServiceModule("test");
        normaliser.setSamples(fileids);
        normaliser.setAnnotations(mapped);
        normaliser.setNormalisationTypes(Arrays.asList(norm));
        normaliser.run();

        CountMatrix observed = normService.getDataMatrix(norm, fileids, mapped);
        CountMatrix expected = CountMatrix.fromCsv(new File(NORM_PATH + norm.getAbbrev() + "_" + COUNT_FILE));
        observed.writeMatrix(new File(NORM_PATH + norm.getAbbrev() + "_testresult.csv"));
        findMatrixDifferences(expected, observed, Paths.get(norm.getAbbrev() + DIFF_OUTPUT));
        assertEquals(observed, expected);
    }
        

    @Test
    public void testQuantileNormalisation() throws Exception
    {
        testNormalisation(NormalisationType.QUANTILE);
    }
    
    @Test
    public void testTCNormalisation() throws Exception
    {
        testNormalisation(NormalisationType.TOTAL_COUNT);
    }
    
    @Test
    public void testTMMNormalisation() throws Exception
    {
        testNormalisation(NormalisationType.TRIMMED_MEAN);
    }
    
    @Test
    public void testDESEQormalisation() throws Exception
    {
        testNormalisation(NormalisationType.DESEQ);
    }
    
    @Ignore("Bootstrapping is not currently testable")
    @Test
    public void testBootstrapNormalisation() throws Exception
    {
        testNormalisation(NormalisationType.BOOTSTRAP);
    }
    
    @Ignore
    @Test
    public void testUQormalisation() throws Exception
    {
        testNormalisation(NormalisationType.UPPER_QUARTILE);
    }
    
    @Test
    public void testPreprocess() throws Exception
    {
        AnnotationSet mapped = AnnotationSet.getMappedSet();
        CountMatrix observed = seqService.getDataMatrix(mapped);
        CountMatrix expected = CountMatrix.fromCsv(new File(NORM_PATH + COUNT_FILE));
        findMatrixDifferences(expected, observed, Paths.get("preprocessing_" + DIFF_OUTPUT));
        assertEquals(observed, expected);
    }
    
    public void findMatrixDifferences(CountMatrix expected, CountMatrix observed, Path output) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("file,sequence,expected,observed\n");
        if(!expected.equals(observed))
        {
            Map<String, Map<String, Double>> obsMap = observed.getMatrix();
            for(Entry<String, Map<String, Double>> expE : expected.getMatrix().entrySet())
            {
                String file = expE.getKey();
                Map<String, Double> thisFile = obsMap.get(file);
                if(thisFile == null)
                {
                    sb.append("File ").append(expE.getKey()).append(" is not in database").append("\n");
                }
                else
                {
                    Map<String, Double> expSeqs = expE.getValue();
                    for(Entry<String, Double> seqE : expSeqs.entrySet())
                    {
                        
                        String expSeq = seqE.getKey();
                        if(!thisFile.containsKey(expSeq))
                        {
                            sb.append(file).append(", ").append(expSeq).append(", ").append(seqE.getValue()).append(", ").append("no entry").append("\n");
                        }
                        else if(!thisFile.get(expSeq).equals(seqE.getValue()))
                        {
                            sb.append(file).append(", ").append(expSeq).append(", ").append(seqE.getValue()).append(", ").append(thisFile.get(expSeq)).append("\n");
                        }
                    }
                }
            }
            Files.write(output, sb.toString().getBytes());
        }
    }
    
}
