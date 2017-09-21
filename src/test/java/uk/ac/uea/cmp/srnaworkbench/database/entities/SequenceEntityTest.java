/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.entities;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.SequenceDAOImpl;
 
 
import org.junit.Test;
 
import org.springframework.beans.factory.annotation.Autowired;
 

/**
 *
 * @author w0445959
 */
public class SequenceEntityTest
{
    private SequenceDAOImpl dao;
    
    public SequenceEntityTest()
    {
    }
    @Autowired
    public void setDao(SequenceDAOImpl dao)
    {
        this.dao = dao;
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
     * Test of getSequence method, of class SequenceEntity.
     */
     @Test
    public void testCreateData() {
//        int expectedResult = 1;
//        SequenceEntity seq = new SequenceEntity();
//        seq.setSequence("ACTGACTG");
//        seq.setGenomeHitCount(1);
//        dao.saveSequence(seq);
//        Assert.assertEquals(expectedResult, dao.getAllSequences(new SequenceEntity()).size());
    }
    @Test
    public void testGetSequence()
    {
//        System.out.println("getSequence");
//        List<SequenceEntity> employeeList = dao.getAllSequences(new SequenceEntity());
//        Assert.assertEquals(1, employeeList.size());
//        SequenceEntity employeeExpected = employeeList.get(0);
//        SequenceEntity employeeResult = dao.getSequenceEntityBySeq(employeeExpected.getSequence());
//        
//        
//        Assert.assertEquals(employeeExpected.getSequence(), employeeResult.getSequence());

        
    }

    /**
     * Test of setSequence method, of class SequenceEntity.
     */
    @Test
    public void testSetSequence()
    {
//        System.out.println("setSequence");
//        List<SequenceEntity> employeeList = dao.getAllSequences(new SequenceEntity());
//        Assert.assertEquals(1, employeeList.size());
//        SequenceEntity employeeExpected = employeeList.get(0);
//        employeeExpected.setSequence("AAAAAAAAAAA");
//        dao.saveSequence(employeeExpected);
//        SequenceEntity employeeResult = dao.getSequenceEntityBySeq(employeeExpected.getSequence());
//        Assert.assertEquals(employeeExpected.getSequence(), employeeResult
//                .getSequence());
    }

    /**
     * Test of getGenomeHitCount method, of class SequenceEntity.
     */
    @Test
    public void testGetGenomeHitCount()
    {
//        System.out.println("getGenomeHitCount");
//        List<SequenceEntity> employeeList = dao.getAllSequences(new SequenceEntity());
//        Assert.assertEquals(1, employeeList.size());
//        SequenceEntity employeeExpected = employeeList.get(0);
//        SequenceEntity employeeResult = dao.getSequenceEntityBySeq(employeeExpected.getSequence());
//        
//        
//        Assert.assertEquals(employeeExpected.getGenomeHitCount(), employeeResult.getGenomeHitCount());
    }

    /**
     * Test of setGenomeHitCount method, of class SequenceEntity.
     */
    @Test
    public void testSetGenomeHitCount()
    {
//        System.out.println("setSequence");
//        List<SequenceEntity> employeeList = dao.getAllSequences(new SequenceEntity());
//        Assert.assertEquals(1, employeeList.size());
//        SequenceEntity employeeExpected = employeeList.get(0);
//        employeeExpected.setGenomeHitCount(2);
//        dao.saveSequence(employeeExpected);
//        SequenceEntity employeeResult = dao.getSequenceEntityBySeq(employeeExpected.getSequence());
//        Assert.assertEquals(2, employeeResult
//                .getGenomeHitCount());
    }
    
}
