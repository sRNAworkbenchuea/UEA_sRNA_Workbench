/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterStage;

/**
 *
 * @author w0445959
 */
public class StreamAdaptorRemoverTest
{
    
    public StreamAdaptorRemoverTest()
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
     * Test of getFiveP_Lengths method, of class StreamAdaptorRemover.
     */
    @Test
    public void testGetFiveP_Lengths()
    {
        try
        {
            System.out.println("getFiveP_Lengths");
            StreamAdaptorRemover instance = new StreamAdaptorRemover();
            HashMap<Integer, FilterStage> expResult = null;
            HashMap<Integer, FilterStage> result = instance.getFiveP_Lengths();
            assertEquals(expResult, result);
            // TODO review the generated test code and remove the default call to fail.
            fail("The test case is a prototype.");
        }
        catch (Exception ex)
        {
            //Logger.getLogger(StreamAdaptorRemoverTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getThreeP_Lengths method, of class StreamAdaptorRemover.
     */
    @Test
    public void testGetThreeP_Lengths()
    {
        try
        {
            System.out.println("getThreeP_Lengths");
            StreamAdaptorRemover instance = new StreamAdaptorRemover();
            HashMap<Integer, FilterStage> expResult = null;
            HashMap<Integer, FilterStage> result = instance.getThreeP_Lengths();
            assertEquals(expResult, result);
            // TODO review the generated test code and remove the default call to fail.
            fail("The test case is a prototype.");
        }
        catch (Exception ex)
        {
            Logger.getLogger(StreamAdaptorRemoverTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getHD_Lengths method, of class StreamAdaptorRemover.
     */
    @Test
    public void testGetHD_Lengths()
    {
        try
        {
            System.out.println("getHD_Lengths");
            StreamAdaptorRemover instance = new StreamAdaptorRemover();
            HashMap<Integer, FilterStage> expResult = null;
            HashMap<Integer, FilterStage> result = instance.getHD_Lengths();
            assertEquals(expResult, result);
            // TODO review the generated test code and remove the default call to fail.
            fail("The test case is a prototype.");
        }
        catch (Exception ex)
        {
            Logger.getLogger(StreamAdaptorRemoverTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of process method, of class StreamAdaptorRemover.
     */
    @Test
    public void testProcess() throws Exception
    {
        System.out.println("process");
        StreamAdaptorRemover instance = new StreamAdaptorRemover();
        instance.process();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
