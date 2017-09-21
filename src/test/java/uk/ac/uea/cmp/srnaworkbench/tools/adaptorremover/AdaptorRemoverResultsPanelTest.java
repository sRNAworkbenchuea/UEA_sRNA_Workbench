/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.util.List;
import java.util.Map;
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
public class AdaptorRemoverResultsPanelTest
{
    
    public AdaptorRemoverResultsPanelTest()
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
     * Test of setOutputStatsTabWaiting method, of class AdaptorRemoverResultsPanel.
     */
    @Test
    public void testSetOutputStatsTabWaiting()
    {
        System.out.println("setOutputStatsTabWaiting");
        String title = "";
        boolean waiting = false;
        AdaptorRemoverResultsPanel instance = new AdaptorRemoverResultsPanel();
        instance.setOutputStatsTabWaiting(title, waiting);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setOutputLengthDistTabWaiting method, of class AdaptorRemoverResultsPanel.
     */
    @Test
    public void testSetOutputLengthDistTabWaiting()
    {
        System.out.println("setOutputLengthDistTabWaiting");
        String title = "";
        boolean waiting = false;
        AdaptorRemoverResultsPanel instance = new AdaptorRemoverResultsPanel();
        instance.setOutputLengthDistTabWaiting(title, waiting);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addOutputTab method, of class AdaptorRemoverResultsPanel.
     */
    @Test
    public void testAddOutputTab()
    {
        System.out.println("addOutputTab");
        String name = "";
        AdaptorRemoverResultsPanel instance = new AdaptorRemoverResultsPanel();
        instance.addOutputTab(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class AdaptorRemoverResultsPanel.
     */
    @Test
    public void testReset()
    {
        System.out.println("reset");
        AdaptorRemoverResultsPanel instance = new AdaptorRemoverResultsPanel();
        instance.reset();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fillLengthDistTable method, of class AdaptorRemoverResultsPanel.
     */
    @Test
    public void testFillLengthDistTable_Map()
    {
        System.out.println("fillLengthDistTable");
        Map<Integer, FilterStage> length_distribution = null;
        AdaptorRemoverResultsPanel instance = new AdaptorRemoverResultsPanel();
        instance.fillLengthDistTable(length_distribution);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fillLengthDistTable method, of class AdaptorRemoverResultsPanel.
     */
    @Test
    public void testFillLengthDistTable_String_Map()
    {
        System.out.println("fillLengthDistTable");
        String title = "";
        Map<Integer, FilterStage> length_distribution = null;
        AdaptorRemoverResultsPanel instance = new AdaptorRemoverResultsPanel();
        instance.fillLengthDistTable(title, length_distribution);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fillExecutionStatsTable method, of class AdaptorRemoverResultsPanel.
     */
    @Test
    public void testFillExecutionStatsTable_List()
    {
        System.out.println("fillExecutionStatsTable");
        List<FilterStage> statistics = null;
        AdaptorRemoverResultsPanel instance = new AdaptorRemoverResultsPanel();
        instance.fillExecutionStatsTable(statistics);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fillExecutionStatsTable method, of class AdaptorRemoverResultsPanel.
     */
    @Test
    public void testFillExecutionStatsTable_String_List()
    {
        System.out.println("fillExecutionStatsTable");
        String title = "";
        List<FilterStage> statistics = null;
        AdaptorRemoverResultsPanel instance = new AdaptorRemoverResultsPanel();
        instance.fillExecutionStatsTable(title, statistics);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
