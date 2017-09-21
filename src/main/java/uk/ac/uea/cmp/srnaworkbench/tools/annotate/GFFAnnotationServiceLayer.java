package uk.ac.uea.cmp.srnaworkbench.tools.annotate;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.LazyInitializationException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.Batcher;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.GFFDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AnnotationTypeDAO;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.GFF_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Unique_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.ChildBeforeParentException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.ToolService;
import uk.ac.uea.cmp.srnaworkbench.io.GFFFileReader;
import uk.ac.uea.cmp.srnaworkbench.utils.IteratingStopWatch;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;

/**
 * 
 * @author mka07yyu
 */
@Service("GFFAnnotationService")
@Transactional
public class GFFAnnotationServiceLayer{
    
    @Autowired
    AnnotationService service;// = (AnnotationService) DatabaseWorkflowModule.getInstance().getContext().getBean("AnnotationService");
    
    @Autowired
    GFFDAO gffDao;
    
    @Autowired
    AnnotationTypeDAO gffTypeDao;
       
    /**
     * Annotates all alignments with all
     * GFF entries in the database
     */
    public void annotateAlignments()
    {
        IteratingStopWatch sw = new IteratingStopWatch();
        sw.start();
        Annotation_Type_Entity defaultType;
        try {
            defaultType = service.getType("genome", "intergenic");
        } catch (ChildBeforeParentException ex) {
            Logger.getLogger(GFFAnnotationServiceLayer.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Can't find defaultType of reference genome and type integenic in database");
        }
        
        int row = 0;
        Session session = gffDao.getSessionFactory().openSession();
        Batcher batch = new Batcher(session, 1);
        
//        List list = session.createCriteria(Aligned_Sequences_Entity.class)
//                .addOrder(Order.asc("id.chrom"))
//                .addOrder(Order.asc("id.start")).addOrder(Order.asc("id.end")).list();
        
        ScrollableResults query = getQuery(session);
        sw.lap("Query sRNA alignments");
        boolean queryHasNext = query.next();
        
        // only annotate if there are queries
        if(queryHasNext)
        {
            // *** Initialise all fields *** //
            // get current query
            Aligned_Sequences_Entity curr_q = (Aligned_Sequences_Entity) query.get(0);
            // set current seqid to the seqid of the current query
            String currentChr = curr_q.getId().getChrom();
            
            ScrollableResults annot = getAnnotations(currentChr, session);          
            boolean annotHasNext = annot.next();
                    
            // For each query sequence
            while(queryHasNext)
            {
                // reset cached position to beginning
                int cachePos = 0;
             //   System.out.println("annotating: " + curr_q.getRna_seq() + " against " + currentChr); // chris added
                // Are we here because of a new seqid or no more annotations?
                if(!currentChr.equals(curr_q.getId().getChrom()))
                {
                    // set up the new seqid
                    currentChr = curr_q.getId().getChrom();
                    
                    // get new annotations for a new chromosome
                    annot.close();
                    annot = getAnnotations(currentChr, session);
                    annotHasNext = annot.next(); 
                }
                else if(!annotHasNext)
                {
                    // if no more annotations, the query should be fast-forwarded until there is a new seqid
                    resolveAnnotation(curr_q, defaultType);
                    batch.batchFlush();
                    queryHasNext = query.next();
                    if(queryHasNext){
                        curr_q = (Aligned_Sequences_Entity) query.get(0);
                    }
                    row++;
                }

                // while we are on the same seqid,
                //   - there is another query, 
                //   - the annotation results haves been initialised
                while(annotHasNext && currentChr.equals(curr_q.getId().getChrom()) && queryHasNext)
                {
              //      System.out.println("checking for annotation"); // chris added
//                    System.out.println("Query " + curr_q.getId());
                    // Scroll back through past annotations to check for hits.

                    // save current position
                    int currentPos = annot.getRowNumber();
                    // scroll back to cached position
                    annot.setRowNumber(cachePos);

                    // *** Check Cache *** //
                    // for each annotation between cached position and saved position
//                    System.out.println(" Checking cache from " + cachePos + " to " + currentPos );
                    for (int i = cachePos; i < currentPos; i++) {
                        GFF_Entity curr_a = (GFF_Entity) annot.get(0);
//                        System.out.println(" Annot " + annot.getRowNumber() + " " + curr_a.getId());

                        // If current annotation in cache is before the current query it
                        // no longer needs to be checked as an overlap candidate
                        if (cachePos == i && curr_a.getId().getEnd() < curr_q.getId().getStart()) 
                        {
                            cachePos = i + 1; // advance cached position past the current annotation
//                            System.out.println("  uncached");
                        } 
                        // Check for hit
                        else if (overlaps(curr_q.getId(), curr_a.getId())) {
                            // add overlap to database
//                            System.out.println("  Cached overlap");
//                              System.out.println("  Cached overlap" + " Q: " + curr_q.getId() + " A: " + curr_a.getId());
                            curr_a.addAnnotation(curr_q);
                            curr_q.addAnnotation(curr_a);
//                            batch.batchFlush();
                        }
                        // next annotation - no need to check this because we know it is there
                        annot.next();
                    }

                    GFF_Entity curr_a = (GFF_Entity) annot.get(0);

                    // *** Check New Hits *** //
                    // advance annotations until ahead of queries or annotations run out
//                    System.out.println(" Checking hits...");
                    while (annotHasNext && curr_a.getId().getStart() <= curr_q.getId().getEnd()) {
                        if (overlaps(curr_q.getId(), curr_a.getId())) {
                            // add overlap to database
                            curr_a.addAnnotation(curr_q);
                            curr_q.addAnnotation(curr_a);
//                            System.out.println("  New overlap" + " Q: " + curr_q.getId() + " A: " + curr_a.getId());
//                            batch.batchFlush();
                        }
                        
                        // cache only if end of annotation is still after start of overlap
                        if(cachePos == annot.getRowNumber() - 1 && curr_a.getId().getEnd() < curr_q.getId().getStart())
                        {
                            cachePos = annot.getRowNumber();
                        }

                        // next annotation
                        annotHasNext = annot.next();
                        if(annotHasNext)
                        {
                            curr_a = (GFF_Entity) annot.get(0);
//                            System.out.println(" Annot " + annot.getRowNumber() + " " + curr_a.getId());
                        }
                    }
                    
                    resolveAnnotation(curr_q, defaultType);
                    batch.batchFlush();
                    // next query
                    queryHasNext = query.next();
                    if(queryHasNext){
                        curr_q = (Aligned_Sequences_Entity) query.get(0);
                        row++;
                    }
                }

                // Code is here because:
                //  - There are no more queries
                //    - code will immediately exit

                //  - There are no more annotations for this seqid
                //  - The queries have hit a new chromosome.


            }
            sw.lap("Annotating");
        }
        else
        {
            LOGGER.log(Level.WARNING, "No aligned sequences were found for GFF annotation.");
        }

        batch.finish();
        session.close();
    }
    
    // Called after a successful annotation run on this row.
    // resolves annotations by accepting a single type to be added to the annotation
    // type row based on priorities
    public void resolveAnnotation(Aligned_Sequences_Entity ae, Annotation_Type_Entity defaultType) {

        Set<GFF_Entity> it = ae.getAnnotations();
        int lo = 0;
        Annotation_Type_Entity typeE = defaultType;
        for(GFF_Entity gff : it) {
            Annotation_Type_Entity thisType = gff.getType();
            int p = thisType.getPriority();
            if (p > lo) {
                lo = p;
                typeE = thisType;
            }
        }
        //    System.out.println("annotation found for this: " + typeE.getId().getType());
        ae.setAnnotationType(typeE);
      //  System.out.println("------ABC--------");

        // propogate this annotation to the unique sequence only if this annotation's
        // priority is GREATER THAN than the current annotation type for this unique sequence
        Unique_Sequences_Entity seq = ae.getAligned_seqeunce();
        Annotation_Type_Entity currentType = seq.getConsensus_annotation_type();
        if (typeE.getPriority() > currentType.getPriority()) {
            seq.setConsensus_annotation_type(typeE);
        }
    }
    
    private ScrollableResults getQuery(Session session)
    {
        ScrollableResults seqs = session.createCriteria(Aligned_Sequences_Entity.class)
            .addOrder(Order.asc("id.chrom"))
            .addOrder(Order.asc("id.start")).addOrder(Order.asc("id.end"))
            .scroll(ScrollMode.FORWARD_ONLY);
        return seqs;
    }
    
    private ScrollableResults getAnnotations(String seqid, Session session)
    {
        ScrollableResults annot = session.createCriteria(GFF_Entity.class)
                // restrict results to the seqid we are looking at
                .add(Restrictions.eq("alignment.chrom", seqid))
                .addOrder(Order.asc("alignment.chrom"))
                .addOrder(Order.asc("alignment.start")).addOrder(Order.asc("alignment.end"))
                .scroll(ScrollMode.SCROLL_INSENSITIVE);
        return annot;
    }
    
    private static boolean overlaps(Aligned_Sequences_Entity.Id a, Aligned_Sequences_Entity.Id b)
    {
        //    10   14
        //2            16
        
        int alength = a.getEnd() - a.getStart(); // 4
        int start = Math.max(a.getStart(), b.getStart()); // 10
        int end = Math.min(a.getEnd(), b.getEnd()); // 16
        int overlapAmount = end - start; // 6
        return (double)overlapAmount/alength > 0.5 && a.getStrand().equals(b.getStrand());
    }
    
    /**
     * Assesses the position of an Aligned_Sequence_Entity seq, relative to a GFF_Entity annot and
     * returns whether the entities overlap (0), or if seq < annot (-1), or if seq > annot (+1)
     * 
     * An overlap is accepted if 50% or more of seq overlaps annot
     * @return int indicating whether seq is downstream, overlapping, or upstream of annot
     */
    public static int annotationOverlap(Aligned_Sequences_Entity.Id seq, Aligned_Sequences_Entity.Id annot)
    {
        int chrComp = seq.getChrom().compareTo(annot.getChrom());
        switch(chrComp)
        {
            case -1: case 1:
                return chrComp;
            default:
                return rangeOverlap(seq.getStart(), seq.getEnd(), annot.getStart(), annot.getEnd());
        }
    }
    
    public static int rangeOverlap(int as, int ae, int bs, int be)
    {
        // determine smallest range and swap coords if b is smaller
        int al = (ae - as)+1;
        int bl = (be - bs)+1;
        if(bl < al)
        {
            int ts = as;
            int te = ae;
            int tl = al;
            as = bs;
            ae = be;
            al = bl;

            bs = ts;
            be = te;
            bl = tl;
        }
        
        if(ae < bs)
        {
            return -1;
        }
        
        if(as > be)
        {
            return 1;
        }
        
        if(ae >= bs && as <= be )
        {
            int overlap = (Math.min(ae, be) - Math.max(as, bs))+1;
            if(overlap >= al/2)
            {
                return 0;
            }
            else
            {
                if(as < bs)
                {
                    return -1;
                }
                if(ae > be)
                {
                    return 1;
                }
            }
        }
        throw new IllegalArgumentException("Flawed logic in overlap calculator. Ranges: A:" + as + "," +ae+" B:" + bs+","+be);
    }
}
