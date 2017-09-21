/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.locus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.LocusDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.SRNA_Locus_Entity;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.MiRCatParams;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;

/**
 *
 * @author w0445959
 */
@Transactional
@Service("SRNA_LOCUS_SERVICE")
public class SRNA_Locus_Service
{

    @Autowired
    private LocusDAOImpl locusDAO;

    
    public SRNA_Locus_Service()
    {
  
    }

    public void buildLoci(MiRCatParams params)
    {
        Session session = locusDAO.getSessionFactory().openSession();
        String sql = ((HQLQuerySimple)WorkflowManager.getInstance().getInputData("srnaQuery").getContainer(1).getData()).eval(); 
        Query sQLQuery = session.createQuery(sql);
        //        .setResultTransformer(Transformers.aliasToBean(Aligned_Sequences_Entity.class));
        //sQLQuery.addEntity(Aligned_Sequences_Entity.class);
                //.setResultTransformer(Transformers.aliasToBean(Aligned_Sequences_Entity.class));
        
        ScrollableResults all_sequences = sQLQuery.scroll();
        
        String currentChromosome = "";
        long locusID = 0;
        
        //the set of aligned sequences used for each locus
        Set<Aligned_Sequences_Entity> sequences = new HashSet<>(0);
        
        //previous aligned sequence
        Aligned_Sequences_Entity prevAlignSeq = null;
        //SRNA_Locus_Entity currentLocus = null;
        boolean firstRun = true;
        int currentStartPos = Integer.MIN_VALUE;
        int currentEndPos  = Integer.MAX_VALUE;
       
        session.beginTransaction();
        int flushIndex = 0;

        while (all_sequences.next())
        {
            //Object get = all_sequences.get(0);

            Aligned_Sequences_Entity nextAlignSeq = (Aligned_Sequences_Entity) all_sequences.get(0);
            //if (nextAlignSeq.getRna_seq().length() >= params.getMinLength() && nextAlignSeq.getRna_seq().length() <= params.getMaxLength())
            {

                if (nextAlignSeq.getId().getChrom().equals(currentChromosome))//still on the same chromosome
                {

                    int distance = (nextAlignSeq.getId().getStart() - prevAlignSeq.getId().getEnd());

                    if (distance <= params.getClusterSentinel())//still in the same locus
                    {
                        sequences.add(nextAlignSeq);
                        currentEndPos = nextAlignSeq.getEnd();
                    }
                    else// persist old locus and start a new one
                    {
                        if (sequences.size() >= params.getMinLocusSize())
                        {

                            SRNA_Locus_Entity currentLocus = new SRNA_Locus_Entity(currentStartPos, currentEndPos, currentChromosome);
                            session.save(currentLocus);

                            for (Aligned_Sequences_Entity a_s : sequences)
                            {
                                currentLocus.addToSequences(a_s);
                            }

                            flushIndex++;
                            if (flushIndex >= 500)
                            {
                                session.flush();
                                flushIndex = 0;
                            }
                            locusID++;
                        }

                        currentStartPos = nextAlignSeq.getStart();
                        currentEndPos = nextAlignSeq.getEnd();
                        sequences.clear();
                        sequences.add(nextAlignSeq);

                    }
                }
                else
                {
                    //sequences.add(nextAlignSeq);
                    currentChromosome = nextAlignSeq.getChromosome();

                    currentStartPos = nextAlignSeq.getStart();
                    currentEndPos = nextAlignSeq.getEnd();
                    sequences.clear();
                    sequences.add(nextAlignSeq);

                }
            }


            prevAlignSeq = nextAlignSeq;

        }
        session.getTransaction().commit();

               
        session.flush();
        session.close();
        
        //printAllLoci();
        //searchLocus(10853128, 10853148, "-", "1 CHROMOSOME dumped from ADB: Feb/3/09 16:9; last updated: 2009-02-02",Aligned_Sequences_Entity.DEFAULT_REFERENCE_STRING);

    }
    
    public void searchLocus(int start, int end, String strand, String chrom, String reference)
    {
        System.out.println("PRINTING LOCI");
        Session session = locusDAO.getSessionFactory().openSession();
        
        Aligned_Sequences_Entity.Id id = new Aligned_Sequences_Entity.Id(start, end, chrom, strand, reference);
        
        Aligned_Sequences_Entity result = (Aligned_Sequences_Entity)session.get(Aligned_Sequences_Entity.class, id);

        if (result != null)
        {
            System.out.println("Found sequence");
            SRNA_Locus_Entity locus = result.getLocus_sequences();
            System.out.println("Locus Start: " + locus.getStart() + " End: " + locus.getEnd());

            for (Aligned_Sequences_Entity seq : locus.getSequences())
            {
                System.out.println(seq.getRna_seq());
            }
        }
        else
        {
            System.out.println("Locus Not Found...");
        }

        session.flush();
        session.clear();
        session.close();
    }
    
    public void printAllLoci()
    {
        System.out.println("PRINTING LOCI");
        Session session = locusDAO.getSessionFactory().openSession();
        
        //List<SRNA_Locus_Entity> findAll = this.locusDAO.findAll();
        List<SRNA_Locus_Entity> findAll = session.createCriteria(SRNA_Locus_Entity.class).list();
        for(SRNA_Locus_Entity locus : findAll)
        {
            System.out.println("locus ID: " + locus.getLocus_ID() + " start: " + locus.getStart() + " End: " + locus.getEnd() + " chromosome: " + locus.getChrom());
            System.out.println("Sequences: ");
            for(Aligned_Sequences_Entity aligned : locus.getSequences())
            {
                System.out.println("sequence: " + aligned.getRna_seq());
            }
        }
        
        session.flush();
        session.clear();
        session.close();
    }
}
