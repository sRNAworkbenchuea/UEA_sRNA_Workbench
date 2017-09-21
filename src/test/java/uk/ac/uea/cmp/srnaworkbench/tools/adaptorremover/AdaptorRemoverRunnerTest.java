/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterStage;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;

/**
 *
 * @author w0445959
 */
public class AdaptorRemoverRunnerTest
{
    
    public AdaptorRemoverRunnerTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getResults method, of class AdaptorRemoverRunner.
     */
    @Test
    public void testGetResults()
    {
        System.out.println("getResults");
        AdaptorRemoverRunner instance = null;
        List<FilterStage> expResult = null;
        List<FilterStage> result = instance.getResults();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHDProt method, of class AdaptorRemoverRunner.
     */
    @Test
    public void testGetHDProt()
    {
        System.out.println("getHDProt");
        AdaptorRemoverRunner instance = null;
        AdaptorRemoverParams.HD_Protocol expResult = null;
        AdaptorRemoverParams.HD_Protocol result = instance.getHDProt();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLengths method, of class AdaptorRemoverRunner.
     */
    @Test
    public void testGetLengths()
    {
        System.out.println("getLengths");
        AdaptorRemoverRunner instance = null;
        Map<Integer, FilterStage> expResult = null;
        Map<Integer, FilterStage> result = instance.getLengths();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNewFileName method, of class AdaptorRemoverRunner.
     */
    @Test
    public void testGetNewFileName()
    {
        System.out.println("getNewFileName");
        AdaptorRemoverRunner instance = null;
        String expResult = "";
        String result = instance.getNewFileName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getData method, of class AdaptorRemoverRunner.
     */
    @Test
    public void testGetData()
    {
        System.out.println("getData");
        AdaptorRemoverRunner instance = null;
        FastaMap expResult = null;
        FastaMap result = instance.getData();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHDData method, of class AdaptorRemoverRunner.
     */
    @Test
    public void testGetHDData()
    {
        System.out.println("getHDData");
        AdaptorRemoverRunner instance = null;
        FastaMap expResult = null;
        FastaMap result = instance.getHDData();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeToFasta method, of class AdaptorRemoverRunner.
     */
    @Test
    public void testWriteToFasta()
    {
        System.out.println("writeToFasta");
        File output = null;
        AdaptorRemoverRunner instance = null;
        instance.writeToFasta(output);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of runAdaptorRemover method, of class AdaptorRemoverRunner.
     */
    @Test
    public void testRunAdaptorRemover()
    {
        System.out.println("runAdaptorRemover");
        File in_file = null;
        File out_file = null;
        AdaptorRemoverParams ar_params = null;
        StatusTracker tracker = null;
        AdaptorRemoverRunner instance = null;
        instance.runAdaptorRemover(in_file, out_file, ar_params, tracker);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isComplete method, of class AdaptorRemoverRunner.
     */
    @Test
    public void testIsComplete()
    {
        System.out.println("isComplete");
        AdaptorRemoverRunner instance = null;
        boolean expResult = false;
        boolean result = instance.isComplete();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
