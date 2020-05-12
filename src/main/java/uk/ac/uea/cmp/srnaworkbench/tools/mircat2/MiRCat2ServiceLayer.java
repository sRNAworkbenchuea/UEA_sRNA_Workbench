/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AlignedSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.SRNA_Locus_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.AlignedSequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.io.Chromosome;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.FX.MiRCat2SceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.classes.Patman;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author keu13sgu & Matthew Stocks 
 * CLI execution example: --tool mircat2 -config "/Developer/Applications/sRNAWorkbench/TestingData/miRCat2Testing/mircat2_test_configuration.json"
 */

@Service( "miRCat2ServiceLayer" )
@Transactional
public class MiRCat2ServiceLayer {
    
    @Autowired
    private AlignedSequenceServiceImpl alignedSeqImpl;
    
    @Autowired
    private AlignedSequenceDAOImpl alignedSeqDAO;
    
    HashSet<String> redundant;
    private final BinaryExecutor myExeMan;
    
//    static{
//        aligned_seqService = (AlignedSequenceServiceImpl) applicationContext.getBean("AlignedSequenceService");
//    }
    
  //  private GenomeManager genomeManager = null;
    
    public MiRCat2ServiceLayer(){
         //ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:configuration/app-config.xml");

        myExeMan = new BinaryExecutor();
        // Load our customer service bean
    }
    

    public void process(GenomeManager genomeManager, BufferedWriter outPatman, BufferedWriter outCSV, MiRCat2SceneController controller) throws IOException{
         
        Session session = alignedSeqDAO.getSessionFactory().openSession();
         redundant = this.getRedundancy(session);
         alignedSeqImpl.saveOrUpdate(Aligned_Sequences_Entity.NO_ALIGNMENT);
         
         ArrayList<String> chrs =  new ArrayList(genomeManager.getChromosomes().keySet());
         Collections.sort(chrs);
         
         AttributesExtracter ae;
         for(String s: chrs){
            Chromosome chrom = genomeManager.getChromosomes().get(s);
            String chromId = chrom.getOriginalHeader();               
            System.out.println(chromId);
            
           // Patman p = this.getAlignmentsForChromozome(chromId, session);
            RemoveNoiseManager rnm = new RemoveNoiseManager(session, chromId, chrom.getLength());
            Patman removeNoise = rnm.removeNoise();
//            session.flush();
//            session.close();

            if (removeNoise == null) {
                continue;
            }
            clearRedundant(removeNoise);
            if (!removeNoise.isEmpty()) {
                ae = new AttributesExtracter(removeNoise, myExeMan, session);
                ae.processSequences(genomeManager, chromId);
                ae.printResultsToFiles(outPatman, outCSV, controller);
             }  
        }
//        System.out.println("miRCAt2 finished processing");
         session.flush();
         session.close();

    }
    
    private void clearRedundant(Patman removeNoise) {
        
        for (int i = removeNoise.size() - 1; i >= 0 ; i--) {
            Aligned_Sequences_Entity s = removeNoise.get(i);
            if (!redundant.contains(s.getRna_seq())) {                             
                    removeNoise.remove(i);
            }
            
        }
    }
    
//    public Patman getAlignmentsForChromozome(String chr, Session session) {
//       // alignedSeqDao = (AlignedSequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("AlignedSequenceService");
//        return new Patman(this.alignedSeqImpl.findByChromosome(chr, session));
//    }
    
    public HashSet<String> getRedundancy(Session session){     
        return this.alignedSeqImpl.findSeqsWithAlignmentsLessThan(MiRCat2Params.REPEATS, session);
    }
    
        
        
    
   
    
   
    
    
}
