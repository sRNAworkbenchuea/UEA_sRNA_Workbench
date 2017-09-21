/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import javax.swing.JPanel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author w0445959
 */
@Ignore("Prototype test")
public class AdaptorRemoverMainFrameTest
{
    
    public AdaptorRemoverMainFrameTest()
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
     * Test of shutdown method, of class AdaptorRemoverMainFrame.
     */
    @Test
    public void testShutdown()
    {
        System.out.println("shutdown");
        AdaptorRemoverMainFrame instance = new AdaptorRemoverMainFrame();
        instance.shutdown();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of runProcedure method, of class AdaptorRemoverMainFrame.
     */
    @Test
    public void testRunProcedure()
    {
        System.out.println("runProcedure");
        AdaptorRemoverMainFrame instance = new AdaptorRemoverMainFrame();
        instance.runProcedure();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of showAbundancePlot method, of class AdaptorRemoverMainFrame.
     */
    @Test
    public void testShowAbundancePlot()
    {
        System.out.println("showAbundancePlot");
        AdaptorRemoverMainFrame instance = new AdaptorRemoverMainFrame();
        instance.showAbundancePlot();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getParams method, of class AdaptorRemoverMainFrame.
     */
    @Test
    public void testGetParams()
    {
        System.out.println("getParams");
        AdaptorRemoverMainFrame instance = new AdaptorRemoverMainFrame();
        JPanel expResult = null;
        JPanel result = instance.getParamsPanel();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setShowingParams method, of class AdaptorRemoverMainFrame.
     */
    @Test
    public void testSetShowingParams()
    {
        System.out.println("setShowingParams");
        boolean newState = false;
        AdaptorRemoverMainFrame instance = new AdaptorRemoverMainFrame();
        instance.setShowingParams(newState);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getShowingParams method, of class AdaptorRemoverMainFrame.
     */
    @Test
    public void testGetShowingParams()
    {
        System.out.println("getShowingParams");
        AdaptorRemoverMainFrame instance = new AdaptorRemoverMainFrame();
        boolean expResult = false;
        boolean result = instance.getShowingParams();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class AdaptorRemoverMainFrame.
     */
    @Test
    public void testUpdate()
    {
        System.out.println("update");
        AdaptorRemoverMainFrame instance = new AdaptorRemoverMainFrame();
        instance.update();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRunningStatus method, of class AdaptorRemoverMainFrame.
     */
    @Test
    public void testSetRunningStatus()
    {
        System.out.println("setRunningStatus");
        boolean running = false;
        AdaptorRemoverMainFrame instance = new AdaptorRemoverMainFrame();
        instance.setRunningStatus(running);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of showErrorDialog method, of class AdaptorRemoverMainFrame.
     */
    @Test
    public void testShowErrorDialog()
    {
        System.out.println("showErrorDialog");
        String message = "";
        AdaptorRemoverMainFrame instance = new AdaptorRemoverMainFrame();
        instance.showErrorDialog(message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
