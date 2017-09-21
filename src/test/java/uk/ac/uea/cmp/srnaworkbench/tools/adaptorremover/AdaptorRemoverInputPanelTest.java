/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.io.File;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;

/**
 *
 * @author w0445959
 */
public class AdaptorRemoverInputPanelTest
{
    
    public AdaptorRemoverInputPanelTest()
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
     * Test of setHD_OptionsEnabled method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testSetHD_OptionsEnabled()
    {
        System.out.println("setHD_OptionsEnabled");
        boolean enabled = false;
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        instance.setHD_OptionsEnabled(enabled);
        // TODO review the generated test code and remove the default call to fail.
        //pass("The test case is a prototype.");
    }

    /**
     * Test of setEnabled method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testSetEnabled()
    {
        System.out.println("setEnabled");
        boolean enabled = false;
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        instance.setEnabled(enabled);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of setIOPanelEnabled method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testSetIOPanelEnabled()
    {
        System.out.println("setIOPanelEnabled");
        boolean enabled = false;
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        instance.setIOPanelEnabled(enabled);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getConfig method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testGetConfig()
    {
        System.out.println("getConfig");
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        boolean expResult = false;
        boolean result = instance.getConfig();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of createParams method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testCreateParams()
    {
        System.out.println("createParams");
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        boolean expResult = false;
        boolean result = instance.createParams();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of setTextInput method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testSetTextInput()
    {
        System.out.println("setTextInput");
        String sRNAPath = "";
        String outputPath = "";
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        instance.setTextInput(sRNAPath, outputPath);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getInputFile method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testGetInputFile()
    {
        System.out.println("getInputFile");
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        File expResult = null;
        File result = instance.getInputFile();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getInputFiles method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testGetInputFiles()
    {
        System.out.println("getInputFiles");
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        ArrayList<File> expResult = null;
        ArrayList<File> result = instance.getInputFiles();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getOutputDir method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testGetOutputDir()
    {
        System.out.println("getOutputDir");
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        File expResult = null;
        File result = instance.getOutputDir();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testReset()
    {
        System.out.println("reset");
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        instance.reset();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getParams method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testGetParams()
    {
        System.out.println("getParams");
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        AdaptorRemoverParams expResult = null;
        AdaptorRemoverParams result = instance.getParams();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testUpdate()
    {
        System.out.println("update");
        ToolParameters params = null;
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        instance.update(params);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of showErrorDialog method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testShowErrorDialog()
    {
        System.out.println("showErrorDialog");
        String message = "";
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        instance.showErrorDialog(message);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getOverwriteConfirm method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testGetOverwriteConfirm()
    {
        System.out.println("getOverwriteConfirm");
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        boolean expResult = false;
        boolean result = instance.getOverwriteConfirm();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of addToHistory method, of class AdaptorRemoverInputPanel.
     */
    @Test
    public void testAddToHistory()
    {
        System.out.println("addToHistory");
        ArrayList<String> theInput = null;
        AdaptorRemoverInputPanel instance = new AdaptorRemoverInputPanel();
        instance.addToHistory(theInput);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
