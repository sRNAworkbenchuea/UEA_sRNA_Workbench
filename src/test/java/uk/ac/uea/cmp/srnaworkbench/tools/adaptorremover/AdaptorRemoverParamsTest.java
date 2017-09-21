/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author w0445959
 */
public class AdaptorRemoverParamsTest
{
    
    public AdaptorRemoverParamsTest()
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
     * Test of getMinLength method, of class AdaptorRemoverParams.
     */
    @Test
    public void testGetMinLength()
    {
        System.out.println("getMinLength");
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        int expResult = 0;
        int result = instance.getMinLength();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxLength method, of class AdaptorRemoverParams.
     */
    @Test
    public void testGetMaxLength()
    {
        System.out.println("getMaxLength");
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        int expResult = 0;
        int result = instance.getMaxLength();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get3PrimeAdaptor method, of class AdaptorRemoverParams.
     */
    @Test
    public void testGet3PrimeAdaptor()
    {
        System.out.println("get3PrimeAdaptor");
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        String expResult = "";
        String result = instance.get3PrimeAdaptor();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get5PrimeAdaptor method, of class AdaptorRemoverParams.
     */
    @Test
    public void testGet5PrimeAdaptor()
    {
        System.out.println("get5PrimeAdaptor");
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        String expResult = "";
        String result = instance.get5PrimeAdaptor();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get3PrimeAdaptorLength method, of class AdaptorRemoverParams.
     */
    @Test
    public void testGet3PrimeAdaptorLength()
    {
        System.out.println("get3PrimeAdaptorLength");
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        int expResult = 0;
        int result = instance.get3PrimeAdaptorLength();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get5PrimeAdaptorLength method, of class AdaptorRemoverParams.
     */
    @Test
    public void testGet5PrimeAdaptorLength()
    {
        System.out.println("get5PrimeAdaptorLength");
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        int expResult = 0;
        int result = instance.get5PrimeAdaptorLength();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDiscardLog method, of class AdaptorRemoverParams.
     */
    @Test
    public void testGetDiscardLog()
    {
        System.out.println("getDiscardLog");
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        File expResult = null;
        File result = instance.getDiscardLog();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHD_Setting method, of class AdaptorRemoverParams.
     */
    @Test
    public void testGetHD_Setting()
    {
        System.out.println("getHD_Setting");
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        AdaptorRemoverParams.HD_Protocol expResult = null;
        AdaptorRemoverParams.HD_Protocol result = instance.getHD_Setting();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set3PrimeAdaptorLength method, of class AdaptorRemoverParams.
     */
    @Test
    public void testSet3PrimeAdaptorLength()
    {
        System.out.println("set3PrimeAdaptorLength");
        int adpt_seq_3_len = 0;
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        instance.set3PrimeAdaptorLength(adpt_seq_3_len);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set5PrimeAdaptorLength method, of class AdaptorRemoverParams.
     */
    @Test
    public void testSet5PrimeAdaptorLength()
    {
        System.out.println("set5PrimeAdaptorLength");
        int adpt_seq_5_len = 0;
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        instance.set5PrimeAdaptorLength(adpt_seq_5_len);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDiscardLog method, of class AdaptorRemoverParams.
     */
    @Test
    public void testSetDiscardLog_File()
    {
        System.out.println("setDiscardLog");
        File discard_log = null;
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        instance.setDiscardLog(discard_log);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDiscardLog method, of class AdaptorRemoverParams.
     */
    @Test
    public void testSetDiscardLog_String()
    {
        System.out.println("setDiscardLog");
        String discard_log_path = "";
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        instance.setDiscardLog(discard_log_path);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_HD_Protocol method, of class AdaptorRemoverParams.
     */
    @Test
    public void testSet_HD_Protocol()
    {
        System.out.println("set_HD_Protocol");
        AdaptorRemoverParams.HD_Protocol hd_prot = null;
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        instance.set_HD_Protocol(hd_prot);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLengthRange method, of class AdaptorRemoverParams.
     */
    @Test
    public void testSetLengthRange()
    {
        System.out.println("setLengthRange");
        int min_length = 0;
        int max_length = 0;
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        instance.setLengthRange(min_length, max_length);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set3PrimeAdaptor method, of class AdaptorRemoverParams.
     */
    @Test
    public void testSet3PrimeAdaptor()
    {
        System.out.println("set3PrimeAdaptor");
        String adpt_seq_3 = "";
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        instance.set3PrimeAdaptor(adpt_seq_3);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set5PrimeAdaptor method, of class AdaptorRemoverParams.
     */
    @Test
    public void testSet5PrimeAdaptor()
    {
        System.out.println("set5PrimeAdaptor");
        String adpt_seq_5 = "";
        AdaptorRemoverParams instance = new AdaptorRemoverParams();
        instance.set5PrimeAdaptor(adpt_seq_5);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class AdaptorRemoverParams.
     */
    @Test
    public void testMain()
    {
        System.out.println("main");
        String[] args = null;
        AdaptorRemoverParams.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
