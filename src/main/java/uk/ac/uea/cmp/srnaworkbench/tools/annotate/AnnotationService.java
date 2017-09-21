package uk.ac.uea.cmp.srnaworkbench.tools.annotate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.GFFRecord;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AlignedSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AnnotationTypeDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AnnotationTypeKeywordDao;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.GFFDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.ReferenceSequenceSetDAO;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity.TypePK;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Keyword_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.GFF_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Reference_Sequence_Set_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.ChildBeforeParentException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.DuplicateReferenceException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.EmptyTableException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.AnnotationNotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.PoorIntegrityException;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

/**
 * This service layer looks after the slightly over-complicated relation between
 * Reference_Sequence_Set and Annotation_Type tables.
 * 
 * Priorities are assigned to new references and types by giving the latest
 * addition the highest priority so far. This means that priorities are dependent on
 * the order of which these entities are added to the database.
 * 
 * TODO: implement ways in which these priorities can be altered in the event
 * that a lower priority entity must be added after a higher priority entity
 * @author Matthew
 */
@Service("AnnotationService")
@Transactional
public class AnnotationService {

    @Autowired
    private AnnotationTypeDAO aTypeDao;
    
    @Autowired
    private ReferenceSequenceSetDAO rssDao;
    
    @Autowired
    private AnnotationTypeKeywordDao keywordDao;
    
    @Autowired
    private AlignedSequenceDAOImpl alignedDao;
    
    @Autowired
    private GFFDAO gffDao;
    
    private static final String TYPE_OTHER = "other";

    /**
     * Must be used in preference to createOrUpdate in the dao so
     * that the annotation aType correctly persists along with part
     * of its id: a Reference_Sequence_Set
     * @param aType an Annotation_Type_Entity with an id
     *  that constitutes a Reference_Sequence_Set_Entity, which is manually inserted
     * into the db before the Annotation_Type_Entity is.
     */
    public void saveOrUpdate(Annotation_Type_Entity aType)
    {
        rssDao.createOrUpdate(aType.getId().getReference());
        aTypeDao.createOrUpdate(aType);
    }
    
    public void addReference(String reference) throws DuplicateReferenceException
    {
        if(rssDao.read(reference) != null )
            throw new DuplicateReferenceException(reference);
        int priority;
        try {
            // this priority is the current top priority + 1
            priority = rssDao.getHighestPriority().getReferencePriority() + 1;
        } catch (EmptyTableException ex) {
            // This exception will trip for the first reference added.
            // This means this is the first ref to be added. Set priority to lowest
            priority = 0;
        }
        Reference_Sequence_Set_Entity rss = new Reference_Sequence_Set_Entity(reference, priority);
        
        // Add to this reference the default type
        Annotation_Type_Entity other = new Annotation_Type_Entity(rss, TYPE_OTHER, 0);
        rss.getAnnotationTypes().add(other);
        
        rssDao.create(rss);
    }
    
    /**
     * Add a type with a priority to for this reference sequence
     *
     * @param reference the reference id the type relates to. This must have been added
     *  in the database already, otherwise an exception is thrown
     * @param type
     * @param priority the priority of this type. 0 is lowest priority.
     * @throws uk.ac.uea.cmp.srnaworkbench.database.exceptions.ChildBeforeParentException
     * 
     */
    public void addType(String reference, String type) throws ChildBeforeParentException
    {
        // check there is a reference
        Reference_Sequence_Set_Entity ref = rssDao.read(reference);
        if (ref == null) {
            throw new ChildBeforeParentException(type, reference);
        }
        
        // retrieve priority
        int priority;
        try {
            // this priority is the current top priority + 1
            priority = aTypeDao.getHighestPriorityType(reference).getPriority() + 1;
        } catch (EmptyTableException ex) {
            // This exception will trip for the first reference added.
            // This means this is the first ref to be added. Set priority to lowest
            priority = 0;
        }

        Annotation_Type_Entity aType = new Annotation_Type_Entity(ref, type, priority);
        this.aTypeDao.createOrUpdate(aType);
        this.aTypeDao.flush();
    }
    
    /**
     * Add a keyword to a type. The keyword is matched against the complete
     * header to decide what type the aligned sequence is of. Default is "other"
     *
     * @param keyword the keyword id
     * @param reference the reference of the type this keyword relates to. Must already be in database
     * @param type the type this keyword relates to. Must already be in database
     * @throws uk.ac.uea.cmp.srnaworkbench.database.exceptions.ChildBeforeParentException
     */
    public void addTypeKeyword(String keyword, String reference, String type) throws ChildBeforeParentException
    {
        Annotation_Type_Entity aType = getType(reference, type);
        if(aType == null)
            throw new ChildBeforeParentException(keyword, type);
        
        Annotation_Type_Keyword_Entity kw = new Annotation_Type_Keyword_Entity(keyword, aType);
        this.keywordDao.create(kw);
        this.aTypeDao.flush();
    }
    
    public Annotation_Type_Entity getType(String reference, String type) throws ChildBeforeParentException
    {
        Reference_Sequence_Set_Entity ref = rssDao.read(reference);
        if (ref == null) {
            throw new ChildBeforeParentException(reference, type);
        }
        Annotation_Type_Entity aType = aTypeDao.read(new TypePK(ref, type));
        return(aType);
    }
    
    public List<String> getAllTypes()
    {
        Map<String, Integer> annotations = new HashMap<>();
        for(Annotation_Type_Entity e : this.aTypeDao.findAll())
        {
            annotations.put(e.getId().getType(), 1);
        }
        return new ArrayList<>(annotations.keySet());
    }
    
    List<Annotation_Type_Entity> getTypesByName(String type)
    {
        Session session = aTypeDao.getSessionFactory().openSession();
        List<Annotation_Type_Entity> types = session.createCriteria(Annotation_Type_Entity.class).add(Restrictions.eq("id.type", type)).list();
        session.close();
        return types;
    }
    
    public AnnotationSet getTypesForReference(String referenceSetName) throws AnnotationNotInDatabaseException
    {
        Session session = this.rssDao.getSessionFactory().openSession();
        List<Annotation_Type_Entity> types = session.createCriteria(Annotation_Type_Entity.class).add(Restrictions.eq("id.reference.referenceSetName", referenceSetName)).list();
        AnnotationSet annotationReferenceSet = new AnnotationSet(referenceSetName);
        for(Annotation_Type_Entity type : types)
        {
            annotationReferenceSet.addAnnotationType(type.getId().getType());
        }
        session.close();
        return annotationReferenceSet;
    }

    /**
     * Setting this means that there is only one type associated with this
     * reference. All other keywords/types will be ignored
     *
     * @param type
     * @param reference
     * @throws AnnotationNotInDatabaseException if the type
     *  or reference is not already in the database
     */
    public void setPrimaryType(String type, String reference) throws AnnotationNotInDatabaseException
    {
        Reference_Sequence_Set_Entity ref = rssDao.read(reference);
        if (ref == null)
            throw new AnnotationNotInDatabaseException(reference);
        Annotation_Type_Entity aType = aTypeDao.read(new TypePK(ref, type));
        if(aType == null)
            throw new AnnotationNotInDatabaseException(type);
        ref.setHasPrimaryType(true);
        ref.setPrimaryType(aType);
        rssDao.update(ref);
        rssDao.flush();
    }
    
    /**
     * Find a type by matching the identifier against available keywords in
     * the database
     * @param reference the reference set id
     * @param identifier the identifier of the annotation that we need a type for
     *  e.g. this could be a header for an RFAM sequence from a fasta file
     * @return The name of the type, or null if no type is found
     */
    public String findType(String reference, String identifier)
    {
        Session session = this.keywordDao.getSessionFactory().openSession();
        String ptype = this.getPrimaryType(reference);
        String type;
        if(ptype != null)
        {
            type = ptype;
        }
        else{
            type = (String) session.createSQLQuery("SELECT TYPE FROM (SELECT TOP 1 * FROM TYPE_KEYWORD TK "
                    + "WHERE :identifier LIKE CONCAT('%', KEYWORD, '%') "
                    + "AND REFERENCE=:reference)")
                    .setParameter("identifier", "5SrRNA").setParameter("reference", reference).uniqueResult();
        }
        session.close();
        return type;
    }
            
    /**
     * Get the type that matches the specified keyword that has the highest priority
     * @param reference
     * @param keyphrase
     * @return 
     * @throws AnnotationNotInDatabaseException if the keyword being used is not in the database
     * @throws PoorIntegrityException if the keyword has no types associated with it
     */
    @SuppressWarnings("null")
    public String getTypeByKeyword(String reference, String keyphrase) throws AnnotationNotInDatabaseException, PoorIntegrityException {
        Annotation_Type_Keyword_Entity kw = this.keywordDao.read(keyphrase);
        if(kw==null)
            throw new AnnotationNotInDatabaseException(keyphrase);
        Set<Annotation_Type_Entity> types = kw.getTypes();
        
        if(types.isEmpty())
            throw new PoorIntegrityException("Keyword element " + keyphrase + "has no types associated with it");
            
        Iterator<Annotation_Type_Entity> it = types.iterator();

        Annotation_Type_Entity bestType = null;
        while(it.hasNext())
        {
            Annotation_Type_Entity aType = it.next();
            if(bestType != null && aType.getPriority() > bestType.getPriority())
            {
                bestType = aType;
            }
        }
        
        // We have already checked that at least one type is associated with the keyword
        // so this can't be null. Thus the suppresswarnings above
        return bestType.getId().getType();
    }
    
    
    public String getPrimaryType(String referenceSetName) {
        Session session = rssDao.getSessionFactory().openSession();
        Reference_Sequence_Set_Entity rss = ((Reference_Sequence_Set_Entity) session.get(Reference_Sequence_Set_Entity.class, referenceSetName));
        String ptype;
        if(!rss.isHasSingleType()){
            ptype = null;
        }
        else{
            ptype = rss.getPrimaryType().getId().getType();
        }
        session.close();
        return ptype;
    }
    
    public void addPatmanAlignment(String referenceName, String seqid, PatmanEntry alignment, String type) throws ChildBeforeParentException
    {
        Aligned_Sequences_Entity aseq = new Aligned_Sequences_Entity(referenceName, seqid, alignment.getSequence(),
                alignment.getStart(), alignment.getEnd(), alignment.getSequenceStrand().getCode(), alignment.getMismatches());
        Reference_Sequence_Set_Entity rsse = rssDao.read(referenceName);
        if(rsse == null)
            throw new ChildBeforeParentException(type, referenceName);
        
        TypePK pk = new TypePK(rsse, type);
        Annotation_Type_Entity atype = this.aTypeDao.read(pk);
        
        // FIXME: add a better priority here
        if(atype == null)
        atype = new Annotation_Type_Entity(rsse, type, 0);
        
        aseq.setAnnotationType(atype);
        this.alignedDao.create(aseq);
        this.aTypeDao.flush();
    }
    
    /**
     * Save or update a Reference_Sequence_Set_Entity
     * @param rssType 
     */
    public void saveOrUpdate(Reference_Sequence_Set_Entity rssType)
    {
        rssDao.createOrUpdate(rssType);
    }
    
    public void printAnnotations()
    {
        Session session = this.rssDao.getSessionFactory().openSession();
        List<Reference_Sequence_Set_Entity> refs = session.createCriteria(Reference_Sequence_Set_Entity.class)
                .list();
        for(Reference_Sequence_Set_Entity ref : refs)
        {
            System.out.println("Reference: " + ref.getReferenceSetName() + ", priority " + ref.getReferencePriority());
            System.out.println("\t Types: " );
            for(Annotation_Type_Entity type : ref.getAnnotationTypes())
            {
                System.out.println("\ttype: " + type.getId().getType() + ", priority " + type.getPriority());
                System.out.println("\t\tKeywords:");
                for(Annotation_Type_Keyword_Entity keys : type.getKeywords())
                {
                    System.out.println("\t\tkey: " + keys.getKeyword());
                }
            }
        }
        session.close();
    }
    
    public void addGFFRecord(GFFRecord gff, String referenceSetName) 
    {
        Session session = this.aTypeDao.getSessionFactory().openSession();
        String type = gff.getType();
        
        try {
            Annotation_Type_Entity type_e = getType(referenceSetName, type);
            if(type_e == null){
                this.addType(referenceSetName, type);
                type_e = getType(referenceSetName, type);
            }
            
            GFF_Entity gffe = new GFF_Entity(gff, type_e);
            this.gffDao.create(gffe);
            this.gffDao.flush();
            
        } catch (ChildBeforeParentException ex) {
            Logger.getLogger(AnnotationService.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        session.close();
    }
            
    public static void main(String[] args)
    {
        AnnotationService as = (AnnotationService) DatabaseWorkflowModule.getInstance().getContext().getBean("AnnotationService");
        Session session = as.aTypeDao.getSessionFactory().openSession();
        
        Reference_Sequence_Set_Entity rss = new Reference_Sequence_Set_Entity("genome", 0);
        //as.rssDao.createOrUpdate(rss);
        Annotation_Type_Entity at = new Annotation_Type_Entity(rss, "exon",  0);
        
        rss.getAnnotationTypes().add(at);
//        as.saveOrUpdate(at);
        as.rssDao.createOrUpdate(rss);
        as.aTypeDao.flush();
        
        System.out.println(as.aTypeDao.read(at.getId()));
        
        session.close();
    }
    
    public void printGFFtable() {
        Session session = gffDao.getSessionFactory().openSession();
        ScrollableResults results = session.createCriteria(GFF_Entity.class).scroll(ScrollMode.FORWARD_ONLY);
        while (results.next()) {
            GFF_Entity gffe = (GFF_Entity) results.get(0);
            System.out.println(gffe.getRecord().toString());
        }
        session.close();
    }

    
    



    
}
